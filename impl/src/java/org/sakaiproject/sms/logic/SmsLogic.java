/***********************************************************************************
 * SmsDao.java - created by Sakai App Builder -AZ
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
package org.sakaiproject.sms.logic;

import java.util.Date;

import org.apache.log4j.Logger;

public class SmsLogic extends BaseLogic {

	/** The log. */
	protected static Logger log = Logger.getLogger(SmsLogic.class);

	/**
	 * Gets the current Date.
	 * 
	 * @return the current Date
	 */
	protected Date getCurrentDate() {
		return new Date(System.currentTimeMillis());
	}

}