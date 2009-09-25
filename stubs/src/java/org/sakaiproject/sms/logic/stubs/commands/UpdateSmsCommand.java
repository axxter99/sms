package org.sakaiproject.sms.logic.stubs.commands;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.logic.incoming.SmsCommand;

public class UpdateSmsCommand implements SmsCommand {

	private static final Log LOG = LogFactory.getLog(UpdateSmsCommand.class);

	public String execute(String siteId, String userId, String mobileNr,
			String... body) {
		String concatBody = "";
		for (String arg : body) {
			if (concatBody.equals("")) {
				concatBody += arg;
			} else {
				concatBody += "," + arg;
			}
		}
		LOG.debug(getCommandKey() + " command called with parameters: ("
				+ siteId + ", " + userId + ", " + concatBody + ")");
		return getCommandKey();
	}

	public String[] getAliases() {
		return new String[] { "U" };
	}

	public String getCommandKey() {
		return "UPDATE";
	}

	public String getHelpMessage() {
		return getCommandKey() + " HELP";
	}

	public int getBodyParameterCount() {
		return 1;
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isVisible() {
		return true;
	}

	public boolean requiresSiteId() {
		// TODO Auto-generated method stub
		return false;
	}
}
