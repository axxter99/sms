/***********************************************************************************
 * SmsMessageLogicImpl.java
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

package org.sakaiproject.sms.logic.impl.hibernate;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.sakaiproject.sms.hibernate.bean.SearchFilterBean;
import org.sakaiproject.sms.hibernate.bean.SearchResultContainer;
import org.sakaiproject.sms.hibernate.dao.SmsDao;
import org.sakaiproject.sms.hibernate.logic.SmsMessageLogic;
import org.sakaiproject.sms.hibernate.logic.impl.exception.SmsSearchException;
import org.sakaiproject.sms.hibernate.model.SmsConfig;
import org.sakaiproject.sms.hibernate.model.SmsMessage;
import org.sakaiproject.sms.hibernate.model.SmsTask;
import org.sakaiproject.sms.hibernate.model.constants.SmsConst_DeliveryStatus;
import org.sakaiproject.sms.hibernate.model.constants.SmsHibernateConstants;
import org.sakaiproject.sms.hibernate.util.DateUtil;
import org.sakaiproject.sms.hibernate.util.HibernateUtil;

/**
 * The data service will handle all sms Message database transactions for the
 * sms tool in Sakai.
 * 
 * @author julian@psybergate.com
 * @version 1.0
 * @created 25-Nov-2008
 */
public class SmsMessageLogicImpl extends SmsDao implements SmsMessageLogic {

	/**
	 * Leave this as protected to try and prevent the random instantiation of
	 * this class.
	 * <p>
	 * Use LogicFactory.java to get instances of logic classes.
	 */
	protected SmsMessageLogicImpl() {

	}

	/**
	 * Deletes and the given entity from the DB
	 */
	public void deleteSmsMessage(SmsMessage smsMessage) {
		delete(smsMessage);
	}

	/**
	 * Gets all the sms Message records
	 * 
	 * @return List of SmsMessage objects
	 */
	public List<SmsMessage> getAllSmsMessages() {
		Session s = HibernateUtil.getSession();
		Query query = s.createQuery("from SmsMessage");
		List<SmsMessage> messages = query.list();
		return messages;
	}

	/**
	 * Gets a search results for all SmsMessages that match the specified
	 * criteria
	 * 
	 * @param searchBean
	 * @return Search result container
	 * @throws SmsSearchException
	 */
	public List<SmsMessage> getAllSmsMessagesForCriteria(
			SearchFilterBean searchBean) throws SmsSearchException {
		return getSmsMessagesForCriteria(searchBean);
	}

	/**
	 * ======================== ONLY FOR TESTING ==================== A new sms
	 * message factory method. Only used for testing.
	 * 
	 * This method will instantiate a SmsTask and return a SmsMessage with the
	 * associated SmsTask object set on it.
	 * <p>
	 * The message will not be persisted.
	 * 
	 * @param mobileNumber
	 *            the mobile number
	 * @param messageBody
	 *            the message body
	 * 
	 * @return the new sms message instance test
	 */
	public SmsMessage getNewTestSmsMessageInstance(String mobileNumber,
			String messageBody) {
		SmsTask smsTask = new SmsTask();
		smsTask.setSakaiSiteId("sakaiSiteId");
		smsTask.setSmsAccountId(1l);
		smsTask.setDateCreated(new Date(System.currentTimeMillis()));
		smsTask.setDateToSend(new Date(System.currentTimeMillis()));
		smsTask.setDateProcessed(new Date(System.currentTimeMillis()));
		smsTask.setStatusCode(SmsConst_DeliveryStatus.STATUS_SENT);
		smsTask.setAttemptCount(2);
		smsTask.setMessageBody("messageBody");
		smsTask.setSenderUserName("senderUserName");
		smsTask.setMaxTimeToLive(1);
		smsTask.setDelReportTimeoutDuration(1);
		smsTask.setGroupSizeEstimate(1);
		smsTask.setGroupSizeActual(1);
		smsTask.setDeliveryUserId("sakaiUserID");
		smsTask.setDeliveryGroupId("SakaiGroupID");
		smsTask.setDeliveryGroupName("SakaiGroupName");
		smsTask.setCreditEstimate(1);

		HibernateLogicFactory.getTaskLogic().persistSmsTask(smsTask);

		SmsMessage smsMessage = new SmsMessage();
		smsMessage.setSmsTask(smsTask);
		smsMessage.setMobileNumber(mobileNumber);
		smsMessage.setMessageBody(messageBody);
		smsMessage.setSmscMessageId("smscMessageId");
		smsMessage.setSakaiUserId("sakaiUserId");
		smsMessage.setStatusCode(SmsConst_DeliveryStatus.STATUS_PENDING);

		smsTask.getSmsMessages().add(smsMessage);

		return smsMessage;
	}

	/**
	 * Gets a search results container housing the result set for a particular
	 * displayed page
	 * 
	 * @param searchBean
	 * @return Search result container
	 * @throws SmsSearchException
	 */
	public SearchResultContainer<SmsMessage> getPagedSmsMessagesForCriteria(
			SearchFilterBean searchBean) throws SmsSearchException {

		List<SmsMessage> messages = getSmsMessagesForCriteria(searchBean);
		SearchResultContainer<SmsMessage> con = new SearchResultContainer<SmsMessage>(
				getPageSize());
		con.setTotalResultSetSize(new Long(messages.size()));
		con.calculateAndSetPageResults(messages, searchBean.getCurrentPage());
		log.debug(con.toString());

		return con;
	}

	private int getPageSize() {
		SmsConfig smsConfig = HibernateLogicFactory.getConfigLogic()
				.getOrCreateSmsConfigBySakaiSiteId(
						SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID);
		if (smsConfig == null)
			return SmsHibernateConstants.DEFAULT_PAGE_SIZE;
		else
			return smsConfig.getPagingSize();
	}

	/**
	 * Gets a SmsMessage entity for the given id
	 * 
	 * @param Long
	 *            sms Message id
	 * @return sms Message
	 */
	public SmsMessage getSmsMessage(Long smsMessageId) {
		return (SmsMessage) findById(SmsMessage.class, smsMessageId);
	}

	/**
	 * Returns a message for the given smsc message id or null if nothing found
	 * 
	 * @param smsc
	 *            message id
	 * @return sms message
	 */
	public SmsMessage getSmsMessageBySmscMessageId(String smscMessageId,
			String smscID) {
		Session s = HibernateUtil.getSession();
		Query query = s
				.createQuery("from SmsMessage mes where mes.smscMessageId = :smscMessageId and mes.smscId = :smscID");
		query.setParameter("smscMessageId", smscMessageId, Hibernate.STRING);
		query.setParameter("smscID", smscID, Hibernate.STRING);
		List<SmsMessage> messages = query.list();
		if (messages != null && messages.size() > 0) {
			return messages.get(0);
		}
		return null;
	}

	private List<SmsMessage> getSmsMessagesForCriteria(
			SearchFilterBean searchBean) throws SmsSearchException {
		Criteria crit = HibernateUtil.getSession().createCriteria(
				SmsMessage.class).createAlias("smsTask", "smsTask");

		log.debug(searchBean.toString());
		List<SmsMessage> messages = new ArrayList<SmsMessage>();
		try {
			// Message status
			if (searchBean.getStatus() != null
					&& !searchBean.getStatus().trim().equals("")) {
				crit.add(Restrictions.ilike("statusCode", searchBean
						.getStatus()));
			}

			// Sakai tool name
			if (searchBean.getToolName() != null
					&& !searchBean.getToolName().trim().equals("")) {
				crit.add(Restrictions.ilike("smsTask.sakaiToolName", searchBean
						.getToolName(), MatchMode.ANYWHERE));
			}

			// Date to send start
			if (searchBean.getDateFrom() != null) {
				Date date = DateUtil.getDateFromStartDateString(searchBean
						.getDateFrom());
				crit.add(Restrictions.ge("dateDelivered", date));
			}

			// Date to send end
			if (searchBean.getDateTo() != null) {
				Date date = DateUtil.getDateFromEndDateString(searchBean
						.getDateTo());
				crit.add(Restrictions.le("dateDelivered", date));
			}

			// Sender name
			if (searchBean.getSender() != null
					&& !searchBean.getSender().trim().equals("")) {
				crit.add(Restrictions.ilike("smsTask.senderUserName",
						searchBean.getSender(), MatchMode.ANYWHERE));
			}

			// Mobile number
			if (searchBean.getNumber() != null
					&& !searchBean.getNumber().trim().equals("")) {
				crit.add(Restrictions.ilike("mobileNumber", searchBean
						.getNumber()));
			}

			// Ordering
			if (searchBean.getOrderBy() != null
					&& !searchBean.getOrderBy().trim().equals("")) {
				crit.addOrder((searchBean.sortAsc() ? Order.asc(searchBean
						.getOrderBy()) : Order.desc(searchBean.getOrderBy())));
			}

			crit.setMaxResults(SmsHibernateConstants.READ_LIMIT);

		} catch (ParseException e) {
			throw new SmsSearchException(e);
		} catch (Exception e) {
			throw new SmsSearchException(e);
		}

		messages = crit.list();
		HibernateUtil.closeSession();
		return messages;
	}

	/**
	 * Gets a list of SmsMessage objects for the specified and specified status
	 * code(s).
	 * 
	 * It will ignore the smsTaskId if it is passed as null and return all
	 * smsMessages with the specified status code(s).
	 * 
	 * @param sms
	 *            task id
	 * @param statusCode
	 *            (s)
	 * @return List<SmsMessage> - sms messages
	 */
	public List<SmsMessage> getSmsMessagesWithStatus(Long smsTaskId,
			String... statusCodes) {
		List<SmsMessage> messages = new ArrayList<SmsMessage>();

		// Return empty list if no status codes were passed in
		if (statusCodes.length > 0) {

			StringBuilder hql = new StringBuilder();
			hql.append(" from SmsMessage message where 1=1  ");
			if (smsTaskId != null) {
				hql.append(" and message.smsTask.id = :smsTaskId ");
			}
			hql.append(" and message.statusCode IN (:statusCodes) ");

			log.debug("getSmsTasksFilteredByMessageStatus() HQL: "
					+ hql.toString());
			Query query = HibernateUtil.getSession()
					.createQuery(hql.toString());
			query
					.setParameterList("statusCodes", statusCodes,
							Hibernate.STRING);
			if (smsTaskId != null) {
				query.setParameter("smsTaskId", smsTaskId);
			}
			messages = query.list();
			HibernateUtil.closeSession();

		}
		return messages;
	}

	/**
	 * This method will persists the given object.
	 * 
	 * If the object is a new entity then it will be created on the DB. If it is
	 * an existing entity then the record will be updated on the DB.
	 * 
	 * @param sms
	 *            Message to be persisted
	 */
	public void persistSmsMessage(SmsMessage smsMessage) {
		persist(smsMessage);
	}

}