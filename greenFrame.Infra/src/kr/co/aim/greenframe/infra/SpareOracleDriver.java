package kr.co.aim.greenframe.infra;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import oracle.jdbc.OracleDriver;

public class SpareOracleDriver extends OracleDriver implements InitializingBean {

	Log logger = LogFactory.getLog(SpareOracleDriver.class);
	
	@Override
	public void afterPropertiesSet() throws Exception {
		
		logger.debug(this.getClass().getName() + "is loaded");
		
	}
}
