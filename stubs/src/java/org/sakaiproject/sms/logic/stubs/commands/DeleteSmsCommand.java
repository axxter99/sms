package org.sakaiproject.sms.logic.stubs.commands;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.sms.logic.incoming.SmsCommand;

public class DeleteSmsCommand implements SmsCommand {

	private static final Log LOG = LogFactory.getLog(DeleteSmsCommand.class);

	public String execute(String siteId, String userId, String mobileNr,
			String... body) {
		LOG.debug(getCommandKey() + " command called with parameters: ("
				+ siteId + ", " + userId + ", " + body + ")");
		return getCommandKey();
	}

	public String[] getAliases() {
		return new String[] { "D" };
	}

	public String getCommandKey() {
		return "DELETE";
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
}
