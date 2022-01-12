package kr.co.aim.messolution.alarm.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class AlarmServiceImpl implements ApplicationContextAware {
	
	private ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog("AlarmServiceImpl");

	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		applicationContext = arg0;
	}
}