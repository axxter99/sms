package org.sakaiproject.sms.logic.hibernate.test;

import java.util.List;

import org.sakaiproject.sms.hibernate.logic.impl.HibernateLogicFactory;
import org.sakaiproject.sms.hibernate.model.SmsConfig;
import org.sakaiproject.sms.hibernate.model.constants.SmsHibernateConstants;
import org.sakaiproject.sms.hibernate.util.HibernateUtil;
import org.sakaiproject.sms.util.AbstractBaseTestCase;

/**
 * Some basic crud tests on sms tool configuration.
 */
public class SmsConfigTest extends AbstractBaseTestCase {

	/** The insert sms config. */
	private static SmsConfig insertSmsConfig;

	static {
		HibernateUtil.setTestConfiguration(true);
		HibernateUtil.createSchema();
		insertSmsConfig = new SmsConfig();
		insertSmsConfig.setSakaiSiteId("sakaiSiteId2");
		insertSmsConfig.setSakaiToolId("sakaiToolId");
		insertSmsConfig.setNotificationEmail("notification@Email.Address");
		insertSmsConfig.setSendSmsEnabled(false);
	}

	/**
	 * Instantiates a new sms config test.
	 */
	public SmsConfigTest() {
	}

	/**
	 * Instantiates a new sms config test.
	 * 
	 * @param name
	 *            the name
	 */
	public SmsConfigTest(String name) {
		super(name);
	}

	/**
	 * Test insert sms config.
	 */
	public void testInsertSmsConfig() {
		HibernateLogicFactory.getConfigLogic()
				.persistSmsConfig(insertSmsConfig);
		// Check the record was created on the DB... an id will be assigned.
		assertTrue("Object not persisted", insertSmsConfig.exists());
	}

	/**
	 * Test get sms config by id.
	 */
	public void testGetSmsConfigById() {
		SmsConfig getSmsConfig = HibernateLogicFactory.getConfigLogic()
				.getSmsConfig(insertSmsConfig.getId());
		assertTrue("Object not persisted", insertSmsConfig.exists());
		assertNotNull(getSmsConfig);
		assertEquals(insertSmsConfig, getSmsConfig);
		assertTrue("Boolean property problem",
				getSmsConfig.isSendSmsEnabled() == false);
	}

	/**
	 * Test update sms config.
	 */
	public void testUpdateSmsConfig() {
		SmsConfig smsConfig = HibernateLogicFactory.getConfigLogic()
				.getSmsConfig(insertSmsConfig.getId());
		smsConfig.setSakaiSiteId("newSakaiSiteId");
		HibernateLogicFactory.getConfigLogic().persistSmsConfig(smsConfig);
		smsConfig = HibernateLogicFactory.getConfigLogic().getSmsConfig(
				insertSmsConfig.getId());
		assertEquals("newSakaiSiteId", smsConfig.getSakaiSiteId());
	}

	/**
	 * Test get sms configs.
	 */
	public void testGetSmsConfigs() {
		List<SmsConfig> confs = HibernateLogicFactory.getConfigLogic()
				.getAllSmsConfig();
		assertNotNull("Returned collection is null", confs);
		assertTrue("No records returned", confs.size() > 0);
	}

	/**
	 * Test get sms config by sakia site id.
	 */
	public void testGetSmsConfigBySakiaSiteId() {
		String testId = "testGetSmsConfigBySakiaSiteId";

		SmsConfig insertSmsConfig = new SmsConfig();
		insertSmsConfig.setSakaiSiteId(testId);
		insertSmsConfig.setSakaiToolId("testGetSmsConfigBySakiaSiteId");
		insertSmsConfig.setNotificationEmail("notification@Email.Address");
		insertSmsConfig.setSendSmsEnabled(false);
		HibernateLogicFactory.getConfigLogic()
				.persistSmsConfig(insertSmsConfig);
		assertTrue("Object not created correctly", insertSmsConfig.exists());

		try {
			SmsConfig conf = HibernateLogicFactory.getConfigLogic()
					.getOrCreateSmsConfigBySakaiSiteId(testId);
			assertNotNull("Object not found", conf);
			assertEquals("Incorrect object returned", conf, insertSmsConfig);
		} finally {
			HibernateLogicFactory.getConfigLogic().deleteSmsConfig(
					insertSmsConfig);
		}
	}

	/**
	 * Test get sms config by sakia tool id.
	 */
	public void testGetSmsConfigBySakiaToolId() {
		String testId = "testGetSmsConfigBySakiaToolId";

		SmsConfig insertSmsConfig = new SmsConfig();
		insertSmsConfig.setSakaiSiteId("testGetSmsConfigBySakiaToolId");
		insertSmsConfig.setSakaiToolId(testId);
		insertSmsConfig.setNotificationEmail("notification@Email.Address");
		insertSmsConfig.setSendSmsEnabled(false);
		HibernateLogicFactory.getConfigLogic()
				.persistSmsConfig(insertSmsConfig);
		assertTrue("Object not created correctly", insertSmsConfig.exists());

		try {
			SmsConfig conf = HibernateLogicFactory.getConfigLogic()
					.getSmsConfigBySakaiToolId(testId);
			assertNotNull("Object not found", conf);
			assertEquals("Incorrect object returned", conf, insertSmsConfig);

			conf = HibernateLogicFactory.getConfigLogic()
					.getSmsConfigBySakaiToolId("SomeOtherId");
			assertNull("No object should be found", conf);

		} finally {
			HibernateLogicFactory.getConfigLogic().deleteSmsConfig(
					insertSmsConfig);
		}
	}

	/**
	 * Test delete sms config.
	 */
	public void testFindByIdDevMode() {
		SmsConfig getSmsConfig = HibernateLogicFactory.getConfigLogic()
				.getOrCreateSmsConfigBySakaiSiteId(
						SmsHibernateConstants.SMS_DEV_DEFAULT_SAKAI_SITE_ID);
		assertNotNull(getSmsConfig);

	}

	/**
	 * Test delete sms config.
	 */
	public void testDeleteSmsConfig() {
		HibernateLogicFactory.getConfigLogic().deleteSmsConfig(insertSmsConfig);
		SmsConfig getSmsConfig = HibernateLogicFactory.getConfigLogic()
				.getSmsConfig(insertSmsConfig.getId());
		assertNull(getSmsConfig);
		assertNull("Object not removed", getSmsConfig);
	}

}
