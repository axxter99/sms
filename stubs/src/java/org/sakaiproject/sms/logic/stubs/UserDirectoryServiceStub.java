package org.sakaiproject.sms.logic.stubs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.entity.api.HttpAccess;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserAlreadyDefinedException;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserEdit;
import org.sakaiproject.user.api.UserIdInvalidException;
import org.sakaiproject.user.api.UserLockedException;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.UserPermissionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class UserDirectoryServiceStub implements UserDirectoryService {

	public UserEdit addUser(String arg0, String arg1)
			throws UserIdInvalidException, UserAlreadyDefinedException,
			UserPermissionException {
		// TODO Auto-generated method stub
		return null;
	}

	public User addUser(String arg0, String arg1, String arg2, String arg3,
			String arg4, String arg5, String arg6, ResourceProperties arg7)
			throws UserIdInvalidException, UserAlreadyDefinedException,
			UserPermissionException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean allowAddUser() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean allowRemoveUser(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean allowUpdateUser(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean allowUpdateUserEmail(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean allowUpdateUserName(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean allowUpdateUserPassword(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean allowUpdateUserType(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public User authenticate(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public void cancelEdit(UserEdit arg0) {
		// TODO Auto-generated method stub

	}

	public void commitEdit(UserEdit arg0) throws UserAlreadyDefinedException {
		// TODO Auto-generated method stub

	}

	public int countSearchUsers(String arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int countUsers() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void destroyAuthentication() {
		// TODO Auto-generated method stub

	}

	public UserEdit editUser(String arg0) throws UserNotDefinedException,
			UserPermissionException, UserLockedException {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection findUsersByEmail(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public User getAnonymousUser() {
		// TODO Auto-generated method stub
		return null;
	}

	public User getCurrentUser() {
		// TODO Auto-generated method stub
		return null;
	}

	public User getUser(String arg0) throws UserNotDefinedException {
		UserEdit u = new UserStubb();
		u.setId(arg0);
		u.setEid(arg0);
		return u;
	}

	public User getUserByEid(String arg0) throws UserNotDefinedException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUserEid(String arg0) throws UserNotDefinedException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getUserId(String arg0) throws UserNotDefinedException {
		// TODO Auto-generated method stub
		return null;
	}

	public List getUsers() {
		UserEdit u = new UserStubb();
		List<User>  ret = new ArrayList<User>();
		ret.add(u);
		return ret;
	}

	public List getUsers(Collection arg0) {
		List ret = new ArrayList<User>();
		Iterator<String> it = arg0.iterator();
		while (it.hasNext()) {
			String val = it.next();
			UserEdit u = new UserStubb();
			u.setId(val);
			u.setEid(val);
			ret.add(u);
		}
		return ret;
	}

	public List getUsers(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<User> getUsersByEids(Collection<String> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public UserEdit mergeUser(Element arg0) throws UserIdInvalidException,
			UserAlreadyDefinedException, UserPermissionException {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeUser(UserEdit arg0) throws UserPermissionException {
		// TODO Auto-generated method stub

	}

	public List searchUsers(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	public String userReference(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String archive(String arg0, Document arg1, Stack arg2, String arg3,
			List arg4) {
		// TODO Auto-generated method stub
		return null;
	}

	public Entity getEntity(Reference arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection getEntityAuthzGroups(Reference arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getEntityDescription(Reference arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public ResourceProperties getEntityResourceProperties(Reference arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getEntityUrl(Reference arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public HttpAccess getHttpAccess() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	public String merge(String arg0, Element arg1, String arg2, String arg3,
			Map arg4, Map arg5, Set arg6) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean parseEntityReference(String arg0, Reference arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean willArchiveMerge() {
		// TODO Auto-generated method stub
		return false;
	}

}