package org.sakaiproject.sms.entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.azeckoski.reflectutils.transcoders.JSONTranscoder;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.RESTful;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.entityprovider.search.Restriction;
import org.sakaiproject.entitybroker.entityprovider.search.Search;
import org.sakaiproject.entitybroker.exception.EntityException;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.sms.bean.SearchFilterBean;
import org.sakaiproject.sms.logic.external.ExternalLogic;
import org.sakaiproject.sms.logic.hibernate.SmsTaskLogic;
import org.sakaiproject.sms.logic.hibernate.exception.SmsSearchException;
import org.sakaiproject.sms.logic.hibernate.exception.SmsTaskNotFoundException;
import org.sakaiproject.sms.logic.smpp.SmsService;
import org.sakaiproject.sms.logic.smpp.SmsTaskValidationException;
import org.sakaiproject.sms.logic.smpp.exception.ReceiveIncomingSmsDisabledException;
import org.sakaiproject.sms.logic.smpp.exception.SmsSendDeniedException;
import org.sakaiproject.sms.logic.smpp.exception.SmsSendDisabledException;
import org.sakaiproject.sms.model.hibernate.SmsMessage;
import org.sakaiproject.sms.model.hibernate.SmsTask;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;

public class SmsTaskEntityProviderImpl implements SmsTaskEntityProvider, AutoRegisterEntityProvider, RESTful{

	private static Log log = LogFactory.getLog(SmsTaskEntityProvider.class);

	/**
	 *	Permission to send SMS messages in a site, therefore also create and manage the resulting tasks  
	 */
	private static String PERMISSION_SEND = ExternalLogic.SMS_SEND;
	
	private static String DATE_FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss";

	/**
	 * Inject services
	 */
	private SmsTaskLogic smsTaskLogic;
	public void setSmsTaskLogic(SmsTaskLogic smsTaskLogic) {
		this.smsTaskLogic = smsTaskLogic;
	}

	private SmsService smsService;
	public void setSmsService(SmsService smsService) {
		this.smsService = smsService;
	}

	private DeveloperHelperService developerHelperService;
	public void setDeveloperHelperService(
			DeveloperHelperService developerHelperService) {
		this.developerHelperService = developerHelperService;
	}
	
	private ExternalLogic externalLogic;
	public void setExternalLogic(ExternalLogic externalLogic) {
		this.externalLogic = externalLogic;
	}

	/**
	 * Implemented
	 */

	public String getEntityPrefix() {
		return ENTITY_PREFIX;
	}


	public Object getSampleEntity() {
		return new SmsTask();
	}

	public Object getEntity(EntityReference ref) {
		String id = ref.getId();
		if (id == null)
				return new SmsTask();

		SmsTask task = smsTaskLogic.getSmsTask(new Long(id));
		 if (task == null) {
	            throw new IllegalArgumentException("No sms task found for the given reference: " + ref);
	        }

		 String currentUserId = developerHelperService.getCurrentUserId();
		 boolean allowedSend = false;
		 if (! developerHelperService.isEntityRequestInternal(ref+"")) {
	            // not an internal request so we require user to be logged in
	            if (currentUserId == null) {
	                throw new SecurityException("User must be logged in in order to access sms task: " + ref);
	            } else {
	                String userReference = developerHelperService.getCurrentUserReference();
	                allowedSend = developerHelperService.isUserAllowedInEntityReference(userReference, PERMISSION_SEND, SiteService.siteReference(task.getSakaiSiteId()));
	                if (!allowedSend) {
	                    throw new SecurityException("User ("+userReference+") not allowed to access sms task: " + ref);
	                }
	            }
		 }
		 task = anonymiseTask(task);
		return task;
	}

	private SmsTask anonymiseTask(SmsTask task) {
		Set<SmsMessage> messages = task.getSmsMessages();
		if (messages == null)
			return task;
		
		Set<SmsMessage> redacted = new HashSet<SmsMessage>();
		Iterator<SmsMessage> iterator = messages.iterator();
		while (iterator.hasNext()) {
			SmsMessage message = iterator.next();
			SmsMessage redMessage = new SmsMessage();
			redMessage.setStatusCode(message.getStatusCode());
			redMessage.setSakaiUserId(message.getSakaiUserId());
			redMessage.setDateDelivered(message.getDateDelivered());
			redMessage.setFailReason(message.getFailReason());
			redacted.add(redMessage);
		}
		task.setSmsMessages(redacted);
		return task;
	}

	public String createEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {
		SmsTask task = (SmsTask) entity;
		Date simpleDate = new Date();
		if (task.getDateCreated() == null)
			task.setDateCreated(simpleDate);

		User user = UserDirectoryService.getCurrentUser();
		
		// Assert task properties not set by EB casting at SmsTask task = (SmsTask) entity.
		setPropertyFromParams(task, params);
		
         if (!SecurityService.unlock(PERMISSION_SEND, SiteService.siteReference(task.getSakaiSiteId()))) {
        	 throw new SecurityException("User " + user.getEid() + " not allowed to create SMS task in site " + task.getSakaiSiteId());
         }
         
         smsService.calculateEstimatedGroupSize(task);
		
		if (!smsService.checkSufficientCredits(task.getSakaiSiteId(), task.getSenderUserId(), task.getGroupSizeEstimate(),false)) {
			throw new EntityException("User "+ task.getSenderUserId() +" has insufficient credit to send SMS task", ref.getReference(), 406); //406 - NOT ACCEPTABLE
		}

		try {
			smsService.insertTask(task);
		} catch (SmsTaskValidationException e) {
			throw new EntityException("Task parameters failed validation", ref.getReference(), 400); //400 - ERROR
		} catch (SmsSendDeniedException e) {
			throw new EntityException("Not allowed to send", ref.getReference(), 405); //405 - METHOD NOT ALLOWED	
		} catch (SmsSendDisabledException e) {
			throw new EntityException("SMS Send Disabled", ref.getReference(), 401); //401 - UNAUTHORIZED	
		} catch (ReceiveIncomingSmsDisabledException e) {
			throw new EntityException("Incoming SMS disabled", ref.getReference(), 401); //401 - UNAUTHORIZED			
		}
		
		// Success
		
		return task.getId().toString();
	}

	public void updateEntity(EntityReference ref, Object entity,
			Map<String, Object> params) {
		String id = ref.getId();
		if (id == null) {
			throw new IllegalArgumentException("The reference must include an id for updates (id is currently null)");
		}
        String userReference = developerHelperService.getCurrentUserReference();
        if (userReference == null) {
            throw new SecurityException("anonymous user cannot update task: " + ref);
        }


        SmsTask current = smsTaskLogic.getSmsTask(new Long(id));
        if (current == null) {
            throw new IllegalArgumentException("No sms task found to update for the given reference: " + ref);
        }


        SmsTask task = (SmsTask) entity;
        //Assert task properties not set by EB casting at SmsTask task = (SmsTask) entity.
        setPropertyFromParams(task, params);
        
        boolean allowedSend = developerHelperService.isUserAllowedInEntityReference(userReference, PERMISSION_SEND, SiteService.siteReference(task.getSakaiSiteId()));
        if (!allowedSend) {
            throw new SecurityException("User ("+userReference+") not allowed to access sms task: " + ref);
        }

        developerHelperService.copyBean(task, current, 0, new String[] {"id", "creationDate"}, true);
        //TODO validate the task
        smsTaskLogic.persistSmsTask(task);
	}

	public void deleteEntity(EntityReference ref, Map<String, Object> params) {
		String id = ref.getId();
		if (id == null)
			throw new IllegalArgumentException("The reference must include an id for deletes (id is currently null)");

		SmsTask task = smsTaskLogic.getSmsTask(Long.valueOf(id));
		if (task == null) {
            throw new IllegalArgumentException("No task found for the given reference: " + ref);
        }

		String userReference = developerHelperService.getCurrentUserReference();
		boolean allowedSend = developerHelperService.isUserAllowedInEntityReference(userReference, PERMISSION_SEND, SiteService.siteReference(task.getSakaiSiteId()));
        if (!allowedSend) {
            throw new SecurityException("User ("+userReference+") not allowed to access sms task: " + ref);
        }

		smsTaskLogic.deleteSmsTask(task);
	}

    public String[] getHandledOutputFormats() {
        return new String[] {Formats.XML, Formats.JSON};
    }

    public String[] getHandledInputFormats() {
        return new String[] {Formats.XML, Formats.JSON, Formats.HTML};
    }


	public List<?> getEntities(EntityReference ref, Search search) {
		String currentUser = developerHelperService.getCurrentUserReference();
        if (currentUser == null) {
            throw new SecurityException("Anonymous users cannot view votes: " + ref);
        }
        Restriction userRes = search.getRestrictionByProperty("userId");
        String userId = null;
        if (userRes == null || userRes.getSingleValue() == null) {
        	userId = developerHelperService.getCurrentUserId();
        	if (userId == null) {
                throw new SecurityException("User must be logged in in order to access sms task: " + ref);
        	}

        }
        try {
        	List<SmsTask> tasks = smsTaskLogic.getAllSmsTasksForCriteria(new SearchFilterBean());
			return tasks;
		} catch (SmsSearchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		return null;
	}
	
	//Custom action to handle /sms-task/abort-task
	@EntityCustomAction(action=CUSTOM_ACTION_ABORT,viewKey=EntityView.VIEW_EDIT)
	public void abort(EntityReference ref, Map<String, Object> params){
		String id = ref.getId();
		if (id == null){
			throw new EntityException("The reference must include an id for task aborts (id is currently null)", ref.getReference(), 406); //406 - NOT ACCEPTABLE
		}
		SmsTask task = smsTaskLogic.getSmsTask(Long.valueOf(id));
		if (task == null) {
			//404 - NOT FOUND (resource not found, URL is invalid in some way, id or action are invalid) 
            throw new EntityException("No task found for the given reference: " + ref, ref.getReference(), 404); 
        }

		String userReference = developerHelperService.getCurrentUserReference();
		boolean allowedSend = developerHelperService.isUserAllowedInEntityReference(userReference, PERMISSION_SEND, SiteService.siteReference(task.getSakaiSiteId()));
        if (!allowedSend) {
            throw new SecurityException("User ("+userReference+") not allowed to access sms task: " + ref);
        }

		try {
			log.info("Starting abort process for task:" + task.getId());
			smsService.abortPendingTask(task.getId());
		} catch (SmsTaskNotFoundException e) {
			//405 - METHOD NOT ALLOWED (the method is not supported for this entity type) 
			throw new EntityException("No task found for id: " + ref + " in the pending/processing queue. May indicate that it's already been processed.", ref.getReference(), 405); 
		}
	}

	//Custom action to handle /sms-task/calculate
	@EntityCustomAction(action=CUSTOM_ACTION_CALCULATE,viewKey=EntityView.VIEW_NEW)
	public String calculate(EntityReference ref, Map<String, Object> params) {
		SmsTask smsTask = (SmsTask) getSampleEntity();
		//Build smsTask object with received parameters
		setPropertyFromParams(smsTask, params);
		
		String userReference = developerHelperService.getCurrentUserReference();
		String senderId = EntityReference.getIdFromRef(userReference);
		
		//Set sensitive sender info now
		smsTask.setSenderUserId(senderId);
		try {
			smsTask.setSenderUserName(UserDirectoryService.getUser(senderId).getDisplayName());
			log.info("User with id="+ senderId +" and name: "+UserDirectoryService.getUser(senderId).getDisplayName() + " attempting to save a new SMS.");
		} catch (UserNotDefinedException e) {
			//Don't exit just in case the sender id is valid but no name is set. Setting new task will fail at validation stage if this sender is not legit
		}
		
		
		if (smsTask.getSakaiSiteId() == null){
			throw new IllegalArgumentException("sakaiSiteId cannot be null");
		}
		if (smsTask.getDeliveryEntityList() == null && smsTask.getSakaiUserIdsList() == null && smsTask.getDeliveryMobileNumbersSet() == null ){
			throw new IllegalArgumentException("At least one of these parameters need to be set: deliveryMobileNumbersSet or sakaiUserIds or deliveryEntityList");
		}

		 boolean allowedSend = developerHelperService.isUserAllowedInEntityReference(userReference, PERMISSION_SEND, SiteService.siteReference(smsTask.getSakaiSiteId()));
         if (!allowedSend) {
             throw new SecurityException("User ("+userReference+") not allowed to access sms task: " + ref);
         }

         smsService.calculateEstimatedGroupSize(smsTask);
       
		return JSONTranscoder.makeJSON(smsTask);
	}
	
	//Custom action to handle /sms-task/memberships
	@EntityCustomAction(action=CUSTOM_ACTION_USERS,viewKey=EntityView.VIEW_SHOW)
	public List<User> memberships(EntityReference ref, Map<String, Object> params) {
		
		String currentUser = developerHelperService.getCurrentUserReference();
        if (currentUser == null) {
            throw new SecurityException("Anonymous users cannot view votes: " + ref);
        }
		
		String siteId = ref.getId();
		String userId = developerHelperService.getCurrentUserId();
		
		if (!SecurityService.unlock(userId, PERMISSION_SEND, SiteService.siteReference(siteId))) {
			throw new SecurityException("User " + userId + " not allowed to view memberships in site " + siteId);
		}
		
        List<User> usersCloned = new ArrayList<User>();
		List<User> usersFull = externalLogic.getUsersWithMobileNumbersOnly(siteId);
	
		for ( User user : usersFull ){
			//trim down user object to only show essential non-sensitive properties
			User clone = developerHelperService.cloneBean(user, 0, new String[] {} );
			usersCloned.add(clone);
		}
		
		 // handle the sorting
        Collections.sort(usersCloned, new SortNameComparator());
        
    	return usersCloned;
	}
	
	private void setPropertyFromParams(SmsTask task, Map<String, Object> params) {

		//Assert date params to task object. Default EB casting to task doesn't set these. SMS-28
		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_ISO8601);
		
		//Save copy boolean and only set it when all params have been iterated and sender ids set
		boolean copy = false;

		Iterator<Entry<String, Object>> selector = params.entrySet().iterator();
		while ( selector.hasNext() ) {
			Entry<String, Object> pairs = selector.next();
			String paramsKey = pairs.getKey();
			String paramsValue = pairs.getValue().toString();

			if( "sakaiSiteId".equals(paramsKey) && ! "".equals( paramsValue)){
	    		task.setSakaiSiteId(paramsValue.toString());
	    	}else 
	    	if( "deliveryEntityList".equals(paramsKey) && ! "".equals( paramsValue)){
	    		task.setDeliveryEntityList(Arrays.asList(paramsValue.split(",")));
	    	}else 
	        if( "sakaiUserIds".equals(paramsKey) && ! "".equals( paramsValue)){
	        	List<String> tempListValues = Arrays.asList(paramsValue.split(","));
	    		Set<String> tempSetValues = new HashSet<String>(tempListValues);
	        	task.setSakaiUserIdsList(tempSetValues);
	    	}else 
	    	if( "deliveryMobileNumbersSet".equals(paramsKey) && ! "".equals( paramsValue)){
	    		List<String> tempListValues = Arrays.asList(paramsValue.split(","));
	    		Set<String> tempSetValues = new HashSet<String>(tempListValues);
	    		task.setDeliveryMobileNumbersSet(tempSetValues);
	    	}else
	    	if( "dateToSend".equals(paramsKey) ){
    			try {
					task.setDateToSend( formatter.parse(paramsValue) );
				} catch (ParseException e) {
					e.printStackTrace();
				}
	    	}else
	    	if( "dateToExpire".equals(paramsKey) ){
				try {
					task.setDateToExpire( formatter.parse(paramsValue) );
				} catch (ParseException e) {
					e.printStackTrace();
				}
	    	}else
			if( "copyMe".equals(paramsKey) ){
	    		copy = Boolean.parseBoolean(paramsValue);	
	    	}
		}
        
		// Set user info - only allow sending as someone else if admin
		User user = UserDirectoryService.getCurrentUser();

		task.setSenderUserName(user.getDisplayName());
		task.setSenderUserId(user.getId());
		
		// Set copy me status
		Set<String> newUserIds = new HashSet<String>();
		if (copy) {
			newUserIds.add(task.getSenderUserId());
			if (task.getSakaiUserIdsList() != null) {
				newUserIds.addAll(task.getSakaiUserIdsList());
				task.setSakaiUserIdsList(newUserIds);
			} else {
				task.setSakaiUserIdsList(newUserIds);
			}
		} else {
			if (task.getSakaiUserIdsList().contains(task.getSenderUserId())) {
				newUserIds = task.getSakaiUserIdsList();
				newUserIds.remove(task.getSenderUserId());
				task.setSakaiUserIdsList(newUserIds);
			}
		}

	}
	
	public static class SortNameComparator implements Comparator<User> {
        public static final long serialVersionUID = 31L;
        public int compare(User o1, User o2) {
            return o1.getSortName().compareTo(o2.getSortName());
        }
    }
}
