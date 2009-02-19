/***********************************************************************************
 * SmsSchedulerImpl.java
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

import org.sakaiproject.sms.logic.hibernate.HibernateLogicLocator;
import org.sakaiproject.sms.logic.smpp.SmsCore;
import org.sakaiproject.sms.logic.smpp.SmsScheduler;
import org.sakaiproject.sms.model.hibernate.SmsConfig;
import org.springframework.util.Assert;

public class SmsSchedulerImpl implements SmsScheduler {

	SmsConfig smsConfig = null;

	private boolean schedulerEnabled = true;// for unit testing only

	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger
			.getLogger(SmsSmppImpl.class);

	public SmsCore smsCore = null;

	public SmsSchedulerThread smsSchedulerThread = null;

	public SmsSchedulerImpl() {

	}

	private HibernateLogicLocator hibernateLogicLocator=null;

	public HibernateLogicLocator getHibernateLogicLocator() {
		return hibernateLogicLocator;
	}

	public void setHibernateLogicLocator(
			HibernateLogicLocator hibernateLogicLocator) {
		this.hibernateLogicLocator = hibernateLogicLocator;
	}

	public void init() {
		Assert.notNull(smsCore);
		smsConfig = hibernateLogicLocator.getSmsConfigLogic()
				.getOrCreateSystemSmsConfig();
		smsSchedulerThread = new SmsSchedulerThread();
		System.out.println("Init of SmsScheduler complete");
	}

	public SmsCore getSmsCore() {
		return smsCore;
	}

	public void setSmsCore(SmsCore smsCore) {
		this.smsCore = smsCore;
	}

	private class SmsSchedulerThread implements Runnable {

		boolean stopScheduler = false;

		SmsSchedulerThread() {
			Thread t = new Thread(this);
			t.start();
		}

		public void run() {
			Work();
		}

		public void Work() {
			while (true) {
				if (stopScheduler) {
					return;
				}
				
				LOG.info("Searching for tasks to process");
				smsCore.processNextTask();
				smsCore.processTimedOutDeliveryReports();
				smsCore.checkAndSetTasksCompleted();
				smsCore.processVeryLateDeliveryReports();
				
				try {
					Thread.sleep(smsConfig.getSchedulerInterval() * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}

		}
	}

	public void setInterval(int seconds) {
		smsConfig.setSchedulerInterval(seconds);
	}

	public void startSmsScheduler() {
		smsSchedulerThread = new SmsSchedulerThread();

	}

	public void stopSmsScheduler() {
		smsSchedulerThread.stopScheduler = true;

	}

	public boolean isSchedulerEnabled() {
		return schedulerEnabled;
	}

	public void setSchedulerEnabled(boolean schedulerEnabled) {
		this.schedulerEnabled = schedulerEnabled;
	}

}
