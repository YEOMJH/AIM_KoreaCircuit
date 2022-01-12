package kr.co.aim.greenframe.infra;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class InfraServiceProxy implements ApplicationContextAware {
	private static Log log = LogFactory.getLog(InfraServiceProxy.class);
	private static ApplicationContext ac;

	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		// TODO Auto-generated method stub
		log.debug("Success Get AC");
		ac = arg0;
	}

	public static ApplicationContext getApplicationContext() {
		return ac;
	}

	public static Object getBeanService(String beanName) {
		return ac.getBean(beanName);
	}
}
