package kr.co.aim.messolution.generic.master;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

public class MessageLogger implements InitializingBean {

	private Log logger;
	
	public void afterPropertiesSet() throws Exception
	{
		//once loaded
		logger = LogFactory.getLog(MessageLogger.class);
		
		logger.info("Message logger bean registered");
	}
	
	public Log getLog()
	{
		return logger;
	}
}
