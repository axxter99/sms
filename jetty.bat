xcopy impl\target\test-classes\org\sakaiproject\sms\*   tool\target\sms_ui\WEB-INF\classes\org\sakaiproject\sms\* /e /y
cd tool
copy test-resources\web.xml target\sms_ui\WEB-INF\web.xml /y
copy test-resources\components.xml target\sms_ui\WEB-INF\components.xml /y 
mvn -o -Dmaven.test.skip=true jetty:run-exploded