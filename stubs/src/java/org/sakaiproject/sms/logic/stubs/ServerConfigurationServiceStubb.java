package org.sakaiproject.sms.logic.stubs;

import java.util.List;
import java.util.Map;

import org.sakaiproject.component.api.ServerConfigurationService;

public class ServerConfigurationServiceStubb implements ServerConfigurationService {

	public String getAccessPath() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAccessUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean getBoolean(String name, boolean dflt) {
		return dflt;
	}

	@SuppressWarnings("unchecked")
	public List getDefaultTools(String category) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getGatewaySiteId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getHelpUrl(String helpContext) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getInt(String name, int dflt) {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getLoggedOutUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPortalUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getRawProperty(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSakaiHomePath() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getServerId() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getServerIdInstance() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getServerInstance() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getServerName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getServerUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getString(String name) {
		return null;
	}

	public String getString(String name, String dflt) {
		
		return dflt;
	}

	public String[] getStrings(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> getToolCategories(String category) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, List<String>> getToolCategoriesAsMap(String category) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	public List getToolOrder(String category) {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getToolToCategoryMap(String category) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getToolUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("unchecked")
	public List getToolsRequired(String category) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUserHomeUrl() {
		// TODO Auto-generated method stub
		return null;
	}

}
