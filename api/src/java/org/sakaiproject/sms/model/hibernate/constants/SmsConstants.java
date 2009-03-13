/***********************************************************************************
 * SmsHibernateConstants.java
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
package org.sakaiproject.sms.model.hibernate.constants;

/**
 * Constants class for SMS
 * 
 * @author Julian Wyngaard
 * @version 1.0
 * @created 08-Dec-2008
 */
public class SmsConstants {

	/** The Constant SORT_ASC. */
	public static final String SORT_ASC = "asc";

	/** The Constant SORT_DESC. */
	public static final String SORT_DESC = "desc";

	/** The Constant DEFAULT_PAGE_SIZE. */
	public static final int DEFAULT_PAGE_SIZE = 20;

	/** The Constant READ_LIMIT. */
	public static final int READ_LIMIT = 100;

	/** The Constant MESSAGE_TYPE_SYSTEM_ORIGINATING. */
	public static final Integer MESSAGE_TYPE_SYSTEM_ORIGINATING = 0;

	/** The Constant MESSAGE_TYPE_MOBILE_ORIGINATING. */
	public static final Integer MESSAGE_TYPE_MOBILE_ORIGINATING = 1;

	/**
	 * Used to indicate if the Task must be processed immediately.
	 */
	// public static final Integer SMS_TASK_TYPE_PROCESS_NOW = 1;
	/**
	 * Used to indicate if the Task must be processed by the scheduler.
	 */
	// public static final Integer SMS_TASK_TYPE_PROCESS_SCHEDULED = 0;
	/**
	 * Used to indicate if the system is running in development mode.
	 */
	public static boolean SMS_DEV_MODE = false;

	/**
	 * The Sakai site id used to store global system settings
	 */
	public static final String SMS_SYSTEM_SAKAI_SITE_ID = "";

	/**
	 * The default interval to look for new tasks to process.
	 */
	public static final int SCHEDULER_INTERVAL = 30;
	/**
	 * The default testing sakai tool id
	 */
	public static final String SMS_DEV_DEFAULT_SAKAI_TOOL_ID = "SakaiToolID";

	/** Enable or disable sms functionality for the site or tool. */
	public static final boolean SMS_ENABLED_FOR_SITE = true;

	/**
	 * The email address that will receive the notification of errors or
	 * insufficient credits.
	 */
	public static final String NOTIFICATION_EMAIL = "notification@instution.com";

	/**
	 * The maximum amount of time an smsTask can be retried.
	 */
	public static final Integer MAXIMUM_RETRY_COUNT = 5;

	/**
	 * The message may only be delivered in the time frame defined by
	 * [dateToSend] + [MAXIMUMTASKLIFETIME]
	 */
	public static final Integer MAXIMUM_TASK_LIFETIME = 60 * 60 * 24;

	/**
	 * The time to wait before retrying an smsTask.
	 */
	public static final Integer RETRY_SCHEDULE_INTERVAL = 60 * 5;

	/**
	 * Default paging size
	 */
	public static final Integer PAGING_SIZE = 200;

	/**
	 * The default overdraft limit
	 */
	public static final Long OVERDRAFT_LIMIT = 1000L;

	/**
	 * The default initial balance for an account
	 */
	public static final Long INITIAL_CREDITS = 0L;

	/** Max sms length */
	public static final int MAX_SMS_LENGTH = 160;

	/** Max mobile number length. */
	public static final int MAX_MOBILE_NR_LENGTH = 20;

	/** The Constant catalogue message not found */
	public static final String CATALOG_MESSAGE_NOT_FOUND = "<Property not found>";

	/** The Constant catalogue message not found */
	public static final String PROPERTY_NOT_FOUND = "<Property not found>";

	/** The Constant catalogue message not found */
	public static final String PROPERTY_FILE_NOT_FOUND = "<Property file not found>";

	/**
	 * The delivery report timeout duration.
	 */
	public static final int DEL_REPORT_TIMEOUT_DURATION = 1800;

	/** The Constant for task notification expired. */
	public static final int TASK_NOTIFICATION_EXPIRED = 1;

	/** The Constant for task notification sent */
	public static final int TASK_NOTIFICATION_SENT = 2;

	/** The Constant for task notification started */
	public static final int TASK_NOTIFICATION_STARTED = 3;

	/** The Constant for task notification started */
	public static final int TASK_NOTIFICATION_COMPLETED = 4;

	/** The Constant for task notification started */
	public static final int TASK_INSUFFICIENT_CREDITS = 5;

	/** The Constant TASK_NOTIFICATION_ABORTED. */
	public static final int TASK_NOTIFICATION_ABORTED = 6;

	/** The Constant TASK_NOTIFICATION_FAILED, after all retry attempts. */
	public static final int TASK_NOTIFICATION_FAILED = 7;

	/**
	 * The Constant TASK_NOTIFICATION_EXCEPTION, default exception notification.
	 */
	public static final int TASK_NOTIFICATION_EXCEPTION = 8;

	/** The Constant for smsAccount ACCOUNT_OVERDRAFT_LIMIT_EXCEEDED. */
	public static final int ACCOUNT_OVERDRAFT_LIMIT_EXCEEDED = 9;

	/** The Constant for smsAccount ACCOUNT_OVERDRAFT_LIMIT_EXCEEDED_MO. */
	public static final int ACCOUNT_OVERDRAFT_LIMIT_EXCEEDED_MO = 10;

	/** The smsc_id is used to uniquely identify the smsc. */
	public static final String SMSC_ID = "1";

	public static final Float COST_OF_CREDIT = 1.5f;

	/** The default max time to live */
	public static final Integer DEFAULT_MAX_TIME_TO_LIVE = 1;

	/** The default max active threads **/
	public static final int DEFAULT_MAX_ACTIVE_THREADS = 10;

	/** The Constant INSUFFICIENT_CREDIT_MESSAGE. */
	public static final String INSUFFICIENT_CREDIT_MESSAGE = "Insufficient credit.";

	// use the site account for all transactions rather than the user's own
	// account
	public static final Boolean DEFAULT_ACCOUNT_USE_SITE_ACCOUNT = true;

}