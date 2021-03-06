/***********************************************************************************
 * PermissionDeniedProducer.java
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
package org.sakaiproject.sms.tool.producers;

import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;

public class PermissionDeniedProducer implements ViewComponentProducer{

	public static final String VIEW_ID = "permission_denied";
	
	public void fillComponents(UIContainer tofill, ViewParameters viewparams,
			ComponentChecker checker) {
		
		UIMessage.make(tofill, "page-title", "sms.permission-denied.title");
		UIMessage
				.make(tofill, "sms-abort-task-heading", "sms.permission-denied.title");
		UIMessage.make(tofill, "permission-denied-msg", "sms.permission-denied.msg");
	}

	public String getViewID() {
		return VIEW_ID;
	}

}
