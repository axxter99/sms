/***********************************************************************************
 * SmsCoreImpl.java
 * Copyright (c) 2008 Sakai Project/Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.sms.logic.smpp.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.hibernate.HibernateLogicLocator;
import org.sakaiproject.sms.logic.hibernate.exception.SmsAccountNotFoundException;
import org.sakaiproject.sms.logic.hibernate.exception.SmsTaskNotFoundException;
import org.sakaiproject.sms.logic.incoming.ParsedMessage;
import org.sakaiproject.sms.logic.incoming.SmsIncomingLogicManager;
import org.sakaiproject.sms.logic.incoming.SmsMessageParser;
import org.sakaiproject.sms.logic.parser.exception.ParseException;
import org.sakaiproject.sms.logic.smpp.SmsBilling;
import org.sakaiproject.sms.logic.smpp.SmsCore;
import org.sakaiproject.sms.logic.smpp.SmsSmpp;
import org.sakaiproject.sms.logic.smpp.SmsTaskValidationException;
import org.sakaiproject.sms.logic.smpp.exception.ReceiveIncomingSmsDisabledException;
import org.sakaiproject.sms.logic.smpp.exception.SmsSendDeniedException;
import org.sakaiproject.sms.logic.smpp.exception.SmsSendDisabledException;
import org.sakaiproject.sms.logic.smpp.util.MessageCatalog;
import org.sakaiproject.sms.logic.smpp.validate.SmsTaskValidator;
import org.sakaiproject.sms.model.hibernate.SmsAccount;
import org.sakaiproject.sms.model.hibernate.SmsConfig;
import org.sakaiproject.sms.model.hibernate.SmsMessage;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.sms.model.hibernate.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.model.hibernate.constants.SmsConst_SmscDeliveryStatus;
import org.sakaiproject.sms.model.hibernate.constants.SmsHibernateConstants;
import org.sakaiproject.sms.util.DateUtil;

/**
 * Handle all core logic regarding SMPP gateway communication.
 * 
 * @author etienne@psybergate.co.za
 * 
 */
public class SmsCoreImpl implements SmsCore {

	private static final Logger LOG = Logger.getLogger(SmsCoreImpl.class);
	private static final String THREAD_GROUP_NAME = "sms";

	private final ThreadGroup smsThreadGroup = new ThreadGroup(
			THREAD_GROUP_NAME);

	private SmsMessageParser smsMessageParser;

	public SmsMessageParser getSmsMessageParser() {
		return smsMessageParser;
	}

	public void setSmsMessageParser(SmsMessageParser smsMessageParser) {
		this.smsMessageParser = smsMessageParser;
	}

	private SmsTaskValidator smsTaskValidator;

	public SmsTaskValidator getSmsTaskValidator() {
		return smsTaskValidator;
	}

	public void setSmsTaskValidator(SmsTaskValidator smsTaskValidator) {
		this.smsTaskValidator = smsTaskValidator;
	}

	private HibernateLogicLocator hibernateLogicLocator;

	public HibernateLogicLocator getHibernateLogicLocator() {
		return hibernateLogicLocator;
	}

	public void setHibernateLogicLocator(
			HibernateLogicLocator hibernateLogicLocator) {
		this.hibernateLogicLocator = hibernateLogicLocator;
	}

	private SmsIncomingLogicManager smsIncomingLogicManager;

	public void setSmsIncomingLogicManager(
			SmsIncomingLogicManager smsIncomingLogicManager) {
		this.smsIncomingLogicManager = smsIncomingLogicManager;
	}

	public SmsSmpp smsSmpp = null;

	public SmsBilling smsBilling = null;

	public SmsTask calculateEstimatedGroupSize(SmsTask smsTask) {
		int groupSize = 0;
		Set<SmsMessage> messages = hibernateLogicLocator.getExternalLogic()
				.getSakaiGroupMembers(smsTask, false);
		groupSize = messages.size();
		smsTask.setGroupSizeEstimate(groupSize);
		// one sms always costs one credit
		smsTask.setCreditEstimate(groupSize);
		smsTask.setCostEstimate(smsBilling.convertCreditsToAmount(groupSize)
				.doubleValue());
		return smsTask;
	}

	/**
	 * Thread to handle all processing of tasks.
	 * 
	 * @author void
	 * 
	 */
	private class ProcessThread implements Runnable {

		SmsTask smsTask;

		ProcessThread(SmsTask smsTask) {
			this.smsTask = smsTask;
			Thread t = new Thread(smsThreadGroup, this);
			t.start();

		}

		public void run() {
			Work();
		}

		public void Work() {
			processTask(smsTask);
		}
	}

	/**
	 * Method sets the sms Messages on the task and calculates the actual group
	 * size.
	 * 
	 * @param smsTask
	 * @return
	 */
	private SmsTask calculateActualGroupSize(SmsTask smsTask) {
		Set<SmsMessage> messages = hibernateLogicLocator.getExternalLogic()
				.getSakaiGroupMembers(smsTask, true);
		smsTask.setSmsMessagesOnTask(messages);
		smsTask.setGroupSizeActual(messages.size());
		return smsTask;
	}

	/*
	 * Enables or disables the debug Information
	 * 
	 * @param debug
	 */
	public void setLoggingLevel(Level level) {
		LOG.setLevel(level);

	}

	public SmsTask getNextSmsTask() {
		return hibernateLogicLocator.getSmsTaskLogic().getNextSmsTask();

	}

	public SmsTask getPreliminaryTask(Date dateToSend, String messageBody,
			String sakaiSiteID, String sakaiToolId, String sakaiSenderID,
			Set<String> deliveryMobileNumbers) {

		return getPreliminaryTask(null, deliveryMobileNumbers, null,
				dateToSend, messageBody, sakaiSiteID, sakaiToolId,
				sakaiSenderID, null);
	}

	public SmsTask getPreliminaryTask(Set<String> sakaiUserIds,
			Date dateToSend, String messageBody, String sakaiSiteID,
			String sakaiToolId, String sakaiSenderID) {

		return getPreliminaryTask(null, null, sakaiUserIds, dateToSend,
				messageBody, sakaiSiteID, sakaiToolId, sakaiSenderID, null);
	}

	public SmsTask getPreliminaryTask(String deliverGroupId, Date dateToSend,
			String messageBody, String sakaiSiteID, String sakaiToolId,
			String sakaiSenderID) {
		return getPreliminaryTask(deliverGroupId, null, null, dateToSend,
				messageBody, sakaiSiteID, sakaiToolId, sakaiSenderID, null);
	}

	public SmsTask getPreliminaryTask(Date dateToSend, String messageBody,
			String sakaiSiteID, String sakaiToolId, String sakaiSenderID,
			List<String> deliveryEntityList) {
		return getPreliminaryTask(null, null, null, dateToSend, messageBody,
				sakaiSiteID, sakaiToolId, sakaiSenderID, deliveryEntityList);
	}

	// ONLY FOR UNIT TESTS
	public SmsTask getPreliminaryTestTask() {
		return getPreliminaryTask(null, null, null, new Date(), "",
				"SakaiSiteID", "", "SakaiUserID", null);
	}

	private SmsTask getPreliminaryTask(String deliverGroupId,
			Set<String> mobileNumbers, Set<String> sakaiUserIds,
			Date dateToSend, String messageBody, String sakaiSiteID,
			String sakaiToolId, String sakaiSenderID,
			List<String> deliveryEntityList) {
		SmsConfig siteConfig = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSystemSmsConfig();
		SmsConfig systemConfig = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSystemSmsConfig();

		SmsTask smsTask = new SmsTask();
		try {
			smsTask.setSmsAccountId(smsBilling.getAccountID(sakaiSiteID,
					sakaiSenderID));
		} catch (SmsAccountNotFoundException e) {
			LOG.error("Sms account not found  for sakaiSiteID:=" + sakaiSiteID
					+ " sakaiSenderID:= " + sakaiSenderID);
			LOG.error(e);
			return null;
		}
		smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);
		smsTask.setSakaiSiteId(sakaiSiteID);
		smsTask.setMessageTypeId(SmsHibernateConstants.MESSAGE_TYPE_OUTGOING);
		smsTask.setSakaiToolId(sakaiToolId);
		smsTask.setSenderUserName(hibernateLogicLocator.getExternalLogic()
				.getSakaiUserDisplayName(sakaiSenderID));
		smsTask.setSenderUserId(sakaiSenderID);
		smsTask.setDeliveryGroupName(deliverGroupId);
		smsTask.setDeliveryGroupId(deliverGroupId);
		smsTask.setDateCreated(new Date());
		smsTask.setDateToSend(dateToSend);
		smsTask.setAttemptCount(0);
		smsTask.setMessageBody(messageBody);
		smsTask.setMaxTimeToLive(siteConfig.getSmsTaskMaxLifeTime());
		Calendar cal = Calendar.getInstance();
		cal.setTime(smsTask.getDateToSend());
		cal.add(Calendar.SECOND, smsTask.getMaxTimeToLive());
		// TODO, DateToExpire must be set from the UI as well
		smsTask.setDateToExpire(cal.getTime());
		smsTask.setDelReportTimeoutDuration(systemConfig
				.getDelReportTimeoutDuration());
		smsTask.setDeliveryMobileNumbersSet(mobileNumbers);
		smsTask.setDeliveryEntityList(deliveryEntityList);
		smsTask.setCreditCost(smsBilling.convertCreditsToAmount(1));
		return smsTask;
	}

	// We answer back by creating a new sms task with one sms message attached
	// to it. The task will then be handled like any other MO task.
	private SmsTask getPreliminaryMOTask(String mobilenumber,
			String SakaiUserId, Date dateToSend, String sakaiSiteID,
			String sakaiToolId, String sakaiSenderID) {
		Set<String> number = new HashSet<String>();
		number.add(mobilenumber);
		SmsTask smsTask = getPreliminaryTask(dateToSend, "", sakaiSiteID,
				sakaiToolId, sakaiSenderID, number);
		if (smsTask != null) {
			smsTask
					.setMessageTypeId(SmsHibernateConstants.MESSAGE_TYPE_INCOMING);
			smsTask.setGroupSizeEstimate(1);
			smsTask.setGroupSizeActual(1);
			smsTask.setCreditEstimate(1);
		}
		return smsTask;
	}

	public SmsBilling getSmsBilling() {
		return smsBilling;
	}

	public SmsSmpp getSmsSmpp() {
		return smsSmpp;
	}

	public void init() {

	}

	public SmsTask insertTask(SmsTask smsTask)
			throws SmsTaskValidationException, SmsSendDeniedException,
			SmsSendDisabledException, ReceiveIncomingSmsDisabledException {

		if (!hibernateLogicLocator.getExternalLogic().isUserAllowedInLocation(
				smsTask.getSenderUserId(), ExternalLogic.SMS_SEND,
				smsTask.getSakaiSiteId())) {
			throw new SmsSendDeniedException();
		}
		if (!hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSmsConfigBySakaiSiteId(smsTask.getSakaiSiteId())
				.isSendSmsEnabled()) {
			throw new SmsSendDisabledException(smsTask);
		}
		if (smsTask.getMessageTypeId() == SmsHibernateConstants.MESSAGE_TYPE_INCOMING) {

			if (!hibernateLogicLocator
					.getSmsConfigLogic()
					.getOrCreateSmsConfigBySakaiSiteId(smsTask.getSakaiSiteId())
					.isReceiveIncomingEnabled()) {
				throw new ReceiveIncomingSmsDisabledException(smsTask);
			}
		}

		ArrayList<String> errors = new ArrayList<String>();
		errors.addAll(smsTaskValidator.validateInsertTask(smsTask));
		if (errors.size() > 0) {
			// Do not persist, just throw exception
			SmsTaskValidationException validationException = new SmsTaskValidationException(
					errors,
					(MessageCatalog
							.getMessage("messages.sms.errors.task.validationFailed")));
			LOG.error(MessageCatalog
					.getMessage("messages.sms.errors.task.validationFailed")
					+ ": " + validationException.getErrorMessagesAsBlock());
			throw validationException;
		}

		// we set the date again due to time laps between getPreliminaryTask and
		// insertask
		smsTask.setDateCreated(DateUtil.getCurrentDate());
		// We do this because if there the invalid values in the task then the
		// checkSufficientCredits() will throw unexpected exceptions. Check for
		// sufficient credit only if the task is valid
		errors.clear();
		errors.addAll(smsTaskValidator.checkSufficientCredits(smsTask));
		if (errors.size() > 0) {
			SmsTaskValidationException validationException = new SmsTaskValidationException(
					errors,
					MessageCatalog
							.getMessage("messages.sms.errors.task.validationFailed"));
			smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_FAIL);
			smsTask
					.setFailReason(validationException
							.getErrorMessagesAsBlock());
			LOG.error(MessageCatalog
					.getMessage("messages.sms.errors.task.validationFailed")
					+ ": " + validationException.getErrorMessagesAsBlock());
			throw validationException;
		}
		hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);
		smsBilling.reserveCredits(smsTask);
		tryProcessTaskRealTime(smsTask);
		return smsTask;
	}

	public void processIncomingMessage(String smsMessagebody,
			String mobileNumber) {

		String smsMessageReplyBody = "";
		ParsedMessage parsedMessage = null;
		try {
			parsedMessage = smsMessageParser.parseMessage(smsMessagebody);
			if (parsedMessage != null) {
				String toolReplyBody = smsIncomingLogicManager.process(
						parsedMessage, mobileNumber);
				if (toolReplyBody != null) {
					smsMessageReplyBody = toolReplyBody;
					LOG.info("Tool " + parsedMessage.getTool()
							+ " answered back with: " + toolReplyBody);
				}
			} else {
				smsMessageReplyBody = "No tool found.";
				// TODO What do we do when we cannot figure out the site or tool
				// from the sms ? And how do we figure out which account to
				// bill. We must still answer the user even if the sms body is
				// empty, at least with some help on the possible commands
				// he/she can send
				parsedMessage = new ParsedMessage("TOOL", "!admin", "admin",
						"COMMAND");

			}
		} catch (ParseException e) {
			// TODO add a proper validation reason.
			smsMessageReplyBody = "Message invalid.";
		}

		SmsMessage smsMessage = new SmsMessage(mobileNumber,
				smsMessageReplyBody);
		if (parsedMessage == null) {
			parsedMessage = new ParsedMessage("TOOL", "!admin", "admin",
					"COMMAND");
		}
		// TODO Who will be the sakai user that will "send" the reply
		SmsTask smsTask = getPreliminaryMOTask(smsMessage.getMobileNumber(),
				"admin", new Date(), parsedMessage.getSite(), parsedMessage
						.getTool(), "admin");

		if (smsTask == null) {
			return;
		}
		smsMessage.setSmsTask(smsTask);
		// TODO: who must te sakai user Id be for te reply?
		smsMessage.setSakaiUserId("admin");
		Set<SmsMessage> smsMessages = new HashSet<SmsMessage>();
		smsMessage.setMessageReplyBody(smsMessageReplyBody);
		smsMessage.setMessageBody(smsMessagebody);
		smsMessages.add(smsMessage);
		smsTask.setSmsMessagesOnTask(smsMessages);

		// process all MO in real-time for now
		try {
			// insertTesk will process task immediately because its date to
			// process is now
			insertTask(smsTask);
		} catch (SmsTaskValidationException e) {
			LOG.error(getExceptionStackTraceAsString(e));
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SmsSendDeniedException e) {
			LOG.error(getExceptionStackTraceAsString(e));
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SmsSendDisabledException e) {
			LOG.error(getExceptionStackTraceAsString(e));
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ReceiveIncomingSmsDisabledException e) {
			// TODO Auto-generated catch block
			LOG.error(getExceptionStackTraceAsString(e));
			e.printStackTrace();
		}

	}

	public void processNextTask() {
		SmsTask smsTask = hibernateLogicLocator.getSmsTaskLogic()
				.getNextSmsTask();
		int maxThreadCount = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSystemSmsConfig().getMaxActiveThreads();
		LOG.info("Number of active Threads :" + getThreadCount(smsThreadGroup)
				+ "/" + maxThreadCount);
		if (smsTask != null) {
			new ProcessThread(smsTask);
		}
	}

	public void processTask(SmsTask smsTask) {
		try {
			SmsConfig systemConfig = hibernateLogicLocator.getSmsConfigLogic()
					.getOrCreateSystemSmsConfig();
			smsTask.setDateProcessed(new Date());
			smsTask.setAttemptCount((smsTask.getAttemptCount()) + 1);

			if (smsTask.getDateToExpire().before(new Date())) {
				smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_EXPIRE);
				smsTask.setStatusForMessages(
						SmsConst_DeliveryStatus.STATUS_PENDING,
						SmsConst_DeliveryStatus.STATUS_EXPIRE);
				sendEmailNotification(smsTask,
						SmsHibernateConstants.TASK_NOTIFICATION_FAILED);
				smsBilling.cancelPendingRequest(smsTask.getId());
				smsTask.setFailReason(MessageCatalog
						.getMessage("messages.taskExpired"));
				hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);
				return;
			}

			smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_BUSY);
			hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);
			if (smsTask.getAttemptCount() < systemConfig.getSmsRetryMaxCount()) {
				if ((!smsTask.getMessageTypeId().equals(
						SmsHibernateConstants.MESSAGE_TYPE_INCOMING) && smsTask
						.getAttemptCount() <= 1)) {
					calculateActualGroupSize(smsTask);
					hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(
							smsTask);
				}
				// ========================== Do the actual sending to the
				// gateway

				String submissionStatus = smsSmpp
						.sendMessagesToGateway(smsTask
								.getMessagesWithStatus(SmsConst_DeliveryStatus.STATUS_PENDING));

				smsTask = hibernateLogicLocator.getSmsTaskLogic().getSmsTask(
						smsTask.getId());
				smsTask.setStatusCode(submissionStatus);

				if (smsTask.getStatusCode().equals(
						SmsConst_DeliveryStatus.STATUS_INCOMPLETE)
						|| smsTask.getStatusCode().equals(
								SmsConst_DeliveryStatus.STATUS_RETRY)) {
					Calendar now = Calendar.getInstance();
					now.add(Calendar.SECOND, +(systemConfig
							.getSmsRetryScheduleInterval()));
					smsTask
							.rescheduleDateToSend(new Date(now
									.getTimeInMillis()));
				}

			} else {
				smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_FAIL);
				smsTask.setStatusForMessages(
						SmsConst_DeliveryStatus.STATUS_PENDING,
						SmsConst_DeliveryStatus.STATUS_FAIL);
				sendEmailNotification(smsTask,
						SmsHibernateConstants.TASK_NOTIFICATION_FAILED);
				smsTask.setFailReason((MessageCatalog.getMessage(
						"messages.taskRetryFailure", String
								.valueOf(systemConfig.getSmsRetryMaxCount()))));
				smsBilling.cancelPendingRequest(smsTask.getId());
			}
			hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);
		} catch (Exception e) {
			LOG.error(getExceptionStackTraceAsString(e));
			smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_FAIL);
			smsTask.setFailReason(e.toString());
			smsBilling.settleCreditDifference(smsTask);
			hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);
			sendEmailNotification(smsTask,
					SmsHibernateConstants.TASK_NOTIFICATION_EXCEPTION,
					getExceptionStackTraceAsString(e));

		}

	}

	private String getExceptionStackTraceAsString(Exception exception) {
		StringWriter sw = new StringWriter();
		exception.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

	public void processTimedOutDeliveryReports() {
		List<SmsMessage> smsMessages = hibernateLogicLocator
				.getSmsMessageLogic().getSmsMessagesWithStatus(null,
						SmsConst_DeliveryStatus.STATUS_SENT);

		if (smsMessages != null) {
			for (SmsMessage message : smsMessages) {
				SmsTask task = message.getSmsTask();
				if (task.getDateProcessed() != null) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(task.getDateProcessed());
					cal
							.add(Calendar.SECOND, task
									.getDelReportTimeoutDuration());
					if (cal.getTime().before(new Date())) {
						message
								.setStatusCode(SmsConst_DeliveryStatus.STATUS_TIMEOUT);
						hibernateLogicLocator.getSmsMessageLogic()
								.persistSmsMessage(message);
						hibernateLogicLocator.getSmsTaskLogic()
								.incrementMessagesProcessed(
										message.getSmsTask());
					}

				}
			}
		}

	}

	public boolean sendNotificationEmail(SmsTask smsTask, String toAddress,
			String subject, String body) {
		// TODO Call sakai service to send the email
		hibernateLogicLocator.getExternalLogic().sendEmail(smsTask, toAddress,
				subject, body);
		return true;
	}

	/**
	 * Send a email notification out.
	 * 
	 * @param smsTask
	 *            the sms task
	 * @param taskMessageType
	 *            the task message type
	 * 
	 * @return true, if successful
	 */
	private boolean sendEmailNotification(SmsTask smsTask,
			Integer taskMessageType) {
		return sendEmailNotification(smsTask, taskMessageType, "");
	}

	/**
	 * Send a email notification out.
	 * 
	 * @param smsTask
	 * @param taskMessageType
	 * @param additionInformation
	 * @return
	 */
	private boolean sendEmailNotification(SmsTask smsTask,
			Integer taskMessageType, String additionInformation) {

		String subject = null;
		String body = null;
		String toAddress = null;

		SmsConfig configSite = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSmsConfigBySakaiSiteId(smsTask.getSakaiSiteId());
		SmsConfig configSystem = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSystemSmsConfig();
		// Get the balance available to calculate the available credit.
		SmsAccount account = hibernateLogicLocator.getSmsAccountLogic()
				.getSmsAccount(smsTask.getSmsAccountId());
		if (account == null) {
			return false;
		}
		Long credits = account.getCredits();

		if (!account.getAccountEnabled()) {
			credits = 0L;
		} else if (account.getOverdraftLimit() != null) {
			// Add the overdraft to the available balance
			credits += account.getOverdraftLimit();
		}

		String creditsAvailable = credits + "";
		String creditsRequired = smsTask.getCreditEstimate() + "";
		toAddress = configSite.getNotificationEmail();
		if (taskMessageType
				.equals(SmsHibernateConstants.TASK_NOTIFICATION_STARTED)) {
			subject = MessageCatalog.getMessage(
					"messages.notificationSubjectStarted", smsTask.getId()
							.toString());
			body = MessageCatalog.getMessage(
					"messages.notificationBodyStarted", creditsRequired,
					creditsAvailable);

		} else if (taskMessageType
				.equals(SmsHibernateConstants.TASK_NOTIFICATION_SENT)) {
			subject = MessageCatalog.getMessage(
					"messages.notificationSubjectSent", smsTask.getId()
							.toString());
			body = MessageCatalog.getMessage("messages.notificationBodySent",
					creditsRequired, creditsAvailable);

		} else if (taskMessageType
				.equals(SmsHibernateConstants.TASK_NOTIFICATION_EXCEPTION)) {
			subject = MessageCatalog.getMessage(
					"messages.notificationSubjectException", smsTask.getId()
							.toString());
			body = additionInformation;

		} else if (taskMessageType
				.equals(SmsHibernateConstants.ACCOUNT_OVERDRAFT_LIMIT_EXCEEDED)) {
			subject = MessageCatalog.getMessage(
					"messages.notificationSubjectOverdraftLimitExceeded",
					smsTask.getId().toString());
			body = MessageCatalog.getMessage(
					"messages.notificationOverdraftLimitExceeded", String
							.valueOf(account.getCredits()), String.valueOf((-1)
							* (account.getOverdraftLimit() + account
									.getCredits())));

		}

		else if (taskMessageType
				.equals(SmsHibernateConstants.TASK_NOTIFICATION_EXPIRED)) {
			subject = MessageCatalog.getMessage(
					"messages.notificationSubjectExpired", smsTask.getId()
							.toString());
			body = MessageCatalog
					.getMessage("messages.notificationBodyExpired");

		} else if (taskMessageType
				.equals(SmsHibernateConstants.TASK_NOTIFICATION_COMPLETED)) {
			subject = MessageCatalog.getMessage(
					"messages.notificationSubjectCompleted", smsTask.getId()
							.toString());
			body = MessageCatalog.getMessage(
					"messages.notificationBodyCompleted", String
							.valueOf(smsTask.getMessagesProcessed()), String
							.valueOf(smsTask.getMessagesDelivered()));

		} else if (taskMessageType
				.equals(SmsHibernateConstants.TASK_NOTIFICATION_ABORTED)) {
			subject = MessageCatalog.getMessage(
					"messages.notificationSubjectAborted", smsTask.getId()
							.toString());
			body = MessageCatalog.getMessage(
					"messages.notificationBodyAborted", smsTask
							.getSenderUserName());

		} else if (taskMessageType
				.equals(SmsHibernateConstants.TASK_NOTIFICATION_FAILED)) {
			subject = MessageCatalog.getMessage(
					"messages.notificationSubjectFailed", smsTask.getId()
							.toString());
			body = MessageCatalog.getMessage("messages.notificationBodyFailed",
					String.valueOf(configSystem.getSmsRetryMaxCount()));

		} else if (taskMessageType
				.equals(SmsHibernateConstants.TASK_INSUFFICIENT_CREDITS)) {
			subject = MessageCatalog
					.getMessage("messages.notificationSubjectTaskInsufficientCredits");
			body = MessageCatalog.getMessage(
					"messages.notificationBodyTaskInsufficientCredits", String
							.valueOf(account.getOverdraftLimit()), String
							.valueOf(account.getOverdraftLimit()
									+ account.getCredits()));
			if (toAddress == null || toAddress.length() == 0) {

				toAddress = hibernateLogicLocator.getExternalLogic()
						.getSakaiSiteContactEmail();
			}
			if (toAddress == null || toAddress.length() == 0) {
				return false;
			}
		}
		boolean systemNotification = sendNotificationEmail(smsTask, toAddress,
				subject, body);
		boolean ownerNotification = sendNotificationEmail(smsTask,
				hibernateLogicLocator.getExternalLogic()
						.getSakaiEmailAddressForUserId(
								smsTask.getSenderUserId()), subject, body);

		return (systemNotification && ownerNotification);

	}

	public void setSmsBilling(SmsBilling smsBilling) {
		this.smsBilling = smsBilling;
	}

	public void setSmsSmpp(SmsSmpp smsSmpp) {
		this.smsSmpp = smsSmpp;
	}

	public synchronized void tryProcessTaskRealTime(SmsTask smsTask) {

		// TODO also check number of process threads
		if (smsTask.getDateToSend().getTime() <= System.currentTimeMillis()) {
			LOG.info("Number of active SMS threads :"
					+ getThreadCount(smsThreadGroup));
			int maxThreadCount = hibernateLogicLocator.getSmsConfigLogic()
					.getOrCreateSystemSmsConfig().getMaxActiveThreads();
			if (getThreadCount(smsThreadGroup) < maxThreadCount) {
				new ProcessThread(smsTask);
			} else {
				LOG
						.info("Maximum allowed SMS threads of "
								+ maxThreadCount
								+ " reached. Task will be scheduled for later processing.");
			}
		}
	}

	public void checkAndSetTasksCompleted() {

		List<SmsTask> smsTasks = hibernateLogicLocator.getSmsTaskLogic()
				.checkAndSetTasksCompleted();

		for (SmsTask smsTask : smsTasks) {
			smsBilling.settleCreditDifference(smsTask);
			checkOverdraft(smsTask);
			if (smsTask.getMessageTypeId() == SmsHibernateConstants.MESSAGE_TYPE_OUTGOING) {
				sendEmailNotification(smsTask,
						SmsHibernateConstants.TASK_NOTIFICATION_COMPLETED);
			}
		}
	}

	private void checkOverdraft(SmsTask smsTask) {
		SmsAccount account = hibernateLogicLocator.getSmsAccountLogic()
				.getSmsAccount(smsTask.getSmsAccountId());
		if (account.getOverdraftLimit() != null) {
			if ((account.getCredits()) < (-1 * account.getOverdraftLimit())) {
				sendEmailNotification(smsTask,
						SmsHibernateConstants.ACCOUNT_OVERDRAFT_LIMIT_EXCEEDED);
			}
		}
	}

	public void processVeryLateDeliveryReports() {
		List<SmsMessage> messages = hibernateLogicLocator.getSmsMessageLogic()
				.getSmsMessagesWithStatus(null,
						SmsConst_DeliveryStatus.STATUS_LATE);

		for (SmsMessage smsMessage : messages) {

			if ((smsMessage.getSmscDeliveryStatusCode()) != SmsConst_SmscDeliveryStatus.DELIVERED) {
				smsMessage.setStatusCode(SmsConst_DeliveryStatus.STATUS_FAIL);
			} else {
				smsMessage
						.setStatusCode(SmsConst_DeliveryStatus.STATUS_DELIVERED);
				hibernateLogicLocator.getSmsTaskLogic()
						.incrementMessagesDelivered(smsMessage.getSmsTask());
				smsBilling.debitLateMessage(smsMessage);
			}
			hibernateLogicLocator.getSmsMessageLogic().persistSmsMessage(
					smsMessage);
		}

	}

	public void abortPendingTask(Long smsTaskID)
			throws SmsTaskNotFoundException {
		SmsTask smsTask = hibernateLogicLocator.getSmsTaskLogic().getSmsTask(
				smsTaskID);
		if (smsTask == null) {
			throw new SmsTaskNotFoundException();
		} else {
			smsBilling.cancelPendingRequest(smsTaskID);
			smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_ABORT);
			smsTask.setStatusForMessages(
					SmsConst_DeliveryStatus.STATUS_PENDING,
					SmsConst_DeliveryStatus.STATUS_ABORT);
			smsTask.setFailReason(MessageCatalog
					.getMessage("messages.taskAborted"));
			hibernateLogicLocator.getSmsTaskLogic().persistSmsTask(smsTask);
			sendEmailNotification(smsTask,
					SmsHibernateConstants.TASK_NOTIFICATION_ABORTED);
		}

	}

	/**
	 * Counts all the acctive threads in a threadGroup
	 * 
	 * @param threadgroup
	 * @return
	 */
	private int getThreadCount(ThreadGroup threadgroup) {
		return threadgroup.activeCount();

	}

}
