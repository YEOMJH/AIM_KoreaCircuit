package kr.co.aim.greenframe.infra;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

public class SchedulerConfigurator implements InitializingBean {
	
	Log log = LogFactory.getLog(getClass());
	
	protected String schedulerName;
	protected String executionJobName;
	protected String cronExpression;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		
		log.info(String.format("scheduler configurator initializing for [%s]", schedulerName));
		
	}

	public String getSchedulerName() {
		return schedulerName;
	}

	public void setSchedulerName(String schedulerName) {
		this.schedulerName = schedulerName;
	}
	
	public String getExecutionJobName() {
		return executionJobName;
	}

	public void setExecutionJobName(String executionJobName) {
		this.executionJobName = executionJobName;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}
}
