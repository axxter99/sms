/***********************************************************************************
 * SmsService.java
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
package org.sakaiproject.sms.logic.smpp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.sakaiproject.sms.hibernate.model.SmsTask;

/**
 * This API allows for implementation of SMS services in an existing or new
 * Sakai tool. To sms-enabled a existing Sakai tool, the following guidelines
 * must be followed:
 * 
 * (1)Call sms.getPreliminaryTask to get a new sms task. (2)Display the sms
 * window. (3)User press the "continue" button. (4)Post UI values to smsTask
 * (liek the sms body). (5)call sms.validateTask(smsTask) and show any errors in
 * UI. (6)call sms.calculateGroupSize to calculate estimated group size on
 * smsTask. (7)Display estimated group size and cost in UI. (8)Change button
 * face to "Save". (9)User press the "Save" button. (10)call
 * sms.checkSufficientCredits. (11)Report insufficient credits in UI. (12)call
 * sms.insertTask(smsTask) for scheduler to handle.
 * 
 * 
 * @author etienne@psybergate.co.za
 * 
 */
public interface SmsService {

	/**
	 * Get a new sms task object with default values. The caller must supply a
	 * list of sakai delivery entities like /site/group1/
	 * 
	 * @param dateToSend
	 * @param messageBody
	 * @param sakaiSiteID
	 * @param sakaiToolId
	 * @param sakaiSenderID
	 * @param deliveryEntityList
	 * @return
	 */
	public SmsTask getPreliminaryTask(Date dateToSend, String messageBody,
			String sakaiSiteID, String sakaiToolId, String sakaiSenderID,
			List<String> deliveryEntityList);

	/**
	 * Get a new sms task object with default values. The caller must supply a
	 * list of mobile numbers.
	 * 
	 * @param dateToSend
	 * @param messageBody
	 * @param sakaiSiteID
	 * @param sakaiToolId
	 * @param sakaiSenderID
	 * @param deliveryMobileNumbers
	 * @return
	 */
	public SmsTask getPreliminaryTask(Date dateToSend, String messageBody,
			String sakaiSiteID, String sakaiToolId, String sakaiSenderID,
			Set<String> deliveryMobileNumbers);

	/**
	 * Get a new sms task object with default values. The caller must supply a
	 * single sakai group id.
	 * 
	 * @param sakaiGroupId
	 * @param dateToSend
	 * @param messageBody
	 * @param sakaiSiteId
	 * @param sakaiToolId
	 * @param sakaiSenderID
	 * @return
	 */
	public SmsTask getPreliminaryTask(String sakaiGroupId, Date dateToSend,
			String messageBody, String sakaiSiteId, String sakaiToolId,
			String sakaiSenderID);

	/**
	 * Get a new sms task object with default values. The caller must supply a
	 * list of Sakai user ID's.
	 * 
	 * @param sakaiUserIds
	 * @param dateToSend
	 * @param messageBody
	 * @param sakaiSiteId
	 * @param sakaiToolId
	 * @param sakaiSenderID
	 * @return
	 */
	public SmsTask getPreliminaryTask(Set<String> sakaiUserIds,
			Date dateToSend, String messageBody, String sakaiSiteId,
			String sakaiToolId, String sakaiSenderID);

	/**
	 * Return true of the account has the required credits available to send the
	 * messages. The account number is calculated using either the Sakai site or
	 * the Sakai user. If this returns false, then the UI must not allow the
	 * user to proceed.
	 * 
	 * @param sakaiSiteID
	 *            , (e.g. "!admin")
	 * @param sakaiUserID
	 *            the sakai user id
	 * @param creditsRequired
	 *            the credits required
	 * 
	 * @return true, if check sufficient credits
	 */
	public boolean checkSufficientCredits(String sakaiSiteID,
			String sakaiUserID, int creditsRequired);

	/**
	 * 
	 * Return true of the account has the required credits available. Take into
	 * account overdraft limits, if applicable.
	 * 
	 * @param smsTask
	 * @return
	 */
	public boolean checkSufficientCredits(SmsTask smsTask);

	/**
	 * Calculate the estimated group size. If this is not set on the task, the
	 * Persistence of the task will fail. Make sure to set ONLY ONE of
	 * deliveryEntityList, deliveryMobileNumbers, sakaiGroupId or sakaiUserIds
	 * before calling this.
	 * 
	 * @param smsTask
	 * @return
	 */
	public SmsTask calculateEstimatedGroupSize(SmsTask smsTask);

	/**
	 * Validate task. Validation must be done in any UI implementing the sms
	 * service. It is also done before a task is persisted (See
	 * SmsCore.insertTask).
	 * 
	 * @param smsTask
	 *            the sms task
	 * 
	 * @return the array list< string>
	 */
	public ArrayList<String> validateTask(SmsTask smsTask);
}