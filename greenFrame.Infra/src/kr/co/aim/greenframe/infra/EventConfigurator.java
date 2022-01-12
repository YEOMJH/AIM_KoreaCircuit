package kr.co.aim.greenframe.infra;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

public class EventConfigurator implements InitializingBean {
	
	Log log = LogFactory.getLog(getClass());
	
	/**
	 * @uml.property name="classMap"
	 */
	HashMap<String, String> classMap = new HashMap<String, String>();
	
	public HashMap<String, String> getClassMap() {
		return classMap;
	}

	public void setClassMap(HashMap classMap) {
		this.classMap = classMap;
	}

	/**
	 * @uml.property name="SenderByMessage"
	 */
	HashMap<String, String> senderByMessage = new HashMap<String, String>();
	
	public HashMap<String, String> getSenderByMessage() {
		return senderByMessage;
	}

	public void setSenderByMessage(HashMap senderByMessage) {
		this.senderByMessage = senderByMessage;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		
		log.info(String.format("event configurator initializing for [%s]", System.getProperty("svr")));
		
		if (getClassMap() != null)
			log.info(String.format("ClassMap load result : %d rows", getClassMap().size()));
		else
			log.info("XML bean read unsuccessfully");
			
		//log.info("XML bean read successfully");
	}
	
//	public void load()
//	{
//		log.info("Initiation for ClassMap");
//		
//		setClassMap(null);
//		setClassMap((HashMap) InfraServiceProxy.getBeanService("ClassMap"));
//		
//		log.info(String.format("ClassMap load result : %d rows", getClassMap().size()));
//	}

}
