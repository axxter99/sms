/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.sms.model.hibernate.constants;

//TODO move to sms_core project later on

public class SmsConst_DeliveryStatus {
	/**
	 * The gateway reported back that the message was successfully delivered to
	 * the mobile number.
	 */
	public final static String STATUS_DELIVERED = "D";


	/**
	 * The gateway reported back that the message was successfully delivered to
	 * the mobile number.
	 */
	public final static String STATUS_ABORT = "A";

	/**
	 * The gateway reported back that the message was successfully delivered to
	 * the mobile number.
	 */
	public final static String STATUS_LATE = "L";

	/**
	 * This indicates that there is no outstanding messages to be send on the
	 * task.
	 */
	public final static String STATUS_TASK_COMPLETED = "C";

	/**
	 * The message has successfully sent to the gateway. We know nothing more
	 * about the message status at this stage.
	 */
	public final static String STATUS_SENT = "S";

	/**
	 * An error occurred when we tried to delivery the message to the gateway.
	 */
	public final static String STATUS_ERROR = "E";

	/**
	 * Pending message are awaiting delivery to the gateway.
	 */
	public final static String STATUS_PENDING = "P";

	/**
	 * The gateway connection was down during a sent attempt to the gateway. If
	 * maximum retry count has not been reached, then we reschedule the
	 * delivery.
	 */
	public final static String STATUS_RETRY = "R";

	/**
	 * The gateway connection went down during the delivery of task messages.
	 * Not all messages could be sent to gateway. Mark task as incomplete aand
	 * try to re-sent the rest of the messages later.
	 */
	public final static String STATUS_INCOMPLETE = "I";
	/**
	 * The maximum retry count of the task has been reached, mark taska s failed
	 * and do nothing further with it.
	 */
	public final static String STATUS_FAIL = "F";

	/**
	 * The task is busy when its messages are being sent to the gateway.
	 */
	public final static String STATUS_BUSY = "B";

	/**
	 * The task is expired when its max time to live is to old to be processed.
	 */
	public final static String STATUS_EXPIRE = "X";

	/**
	 * The message did not receive a delivery report within the timeout period.
	 */
	public final static String STATUS_TIMEOUT = "T";
	
	/**
	 * The create message workflow was started by the user but never finished. Task is a draft.
	 */
	public final static String STATUS_DRAFT = "DR";

}
