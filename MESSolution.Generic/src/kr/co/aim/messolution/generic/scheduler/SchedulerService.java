package kr.co.aim.messolution.generic.scheduler;

import java.util.HashMap;

import kr.co.aim.greenframe.infra.InfraServiceProxy;
import kr.co.aim.greenframe.infra.SchedulerConfigurator;
import kr.co.aim.greenframe.util.bundle.BundleUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;

import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.springframework.beans.factory.InitializingBean;

public class SchedulerService implements InitializingBean
{
	/**
	 * @uml.property  name="schedulerFactory"
	 * @uml.associationEnd  multiplicity="(1 1)"
	 */
	private SchedulerFactory 	schedulerFactory;
	/**
	 * @uml.property  name="scheduler"
	 * @uml.associationEnd  
	 */
	private Scheduler 			scheduler;
	/**
	 * @uml.property  name="cronExpression"
	 */
	private String 			 	cronExpression;
	/**
	 * @uml.property  name="repeatInterval"
	 */
	private long 				repeatInterval = 0;
	/**
	 * @uml.property  name="repeatCount"
	 */
	private int 				repeatCount = 0;
	
	
	/**
	 * @uml.property  name="scheduleName"
	 */
	private String 				scheduleName = "B3SCHEDULE";
	/**
	 * @uml.property  name="scheduleGroup"
	 */
	private String 				scheduleGroup = TimeStampUtil.getCurrentTime();
	/**
	 * @uml.property  name="jobClass"
	 * @uml.associationEnd  
	 */
	private Job					jobClass; 				
	
	
	
	/**
	 * @return
	 * @uml.property  name="repeatInterval"
	 */
	public long getRepeatInterval()
	{
		return repeatInterval;
	}

	/**
	 * @param repeatInterval
	 * @uml.property  name="repeatInterval"
	 */
	public void setRepeatInterval(long repeatInterval)
	{
		this.repeatInterval = repeatInterval;
	}

	/**
	 * @return
	 * @uml.property  name="repeatCount"
	 */
	public int getRepeatCount()
	{
		return repeatCount;
	}

	/**
	 * @param repeatCount
	 * @uml.property  name="repeatCount"
	 */
	public void setRepeatCount(int repeatCount)
	{
		this.repeatCount = repeatCount;
	}

	/**
	 * @return
	 * @uml.property  name="schedulerFactory"
	 */
	public SchedulerFactory getSchedulerFactory()
	{
		return schedulerFactory;
	}

	/**
	 * @param schedulerFactory
	 * @uml.property  name="schedulerFactory"
	 */
	public void setSchedulerFactory(SchedulerFactory schedulerFactory)
	{
		this.schedulerFactory = schedulerFactory;
	}

	/**
	 * @return
	 * @uml.property  name="scheduler"
	 */
	public Scheduler getScheduler()
	{
		return scheduler;
	}

	/**
	 * @param scheduler
	 * @uml.property  name="scheduler"
	 */
	public void setScheduler(Scheduler scheduler)
	{
		this.scheduler = scheduler;
	}

	/**
	 * @return
	 * @uml.property  name="cronExpression"
	 */
	public String getCronExpression()
	{
		return cronExpression;
	}

	/**
	 * @param cronExpression
	 * @uml.property  name="cronExpression"
	 */
	public void setCronExpression(String cronExpression)
	{
		this.cronExpression = cronExpression;
	}

	/**
	 * @return
	 * @uml.property  name="scheduleName"
	 */
	public String getScheduleName()
	{
		return scheduleName;
	}

	/**
	 * @param scheduleName
	 * @uml.property  name="scheduleName"
	 */
	public void setScheduleName(String scheduleName)
	{
		this.scheduleName = scheduleName;
	}

	/**
	 * @return
	 * @uml.property  name="scheduleGroup"
	 */
	public String getScheduleGroup()
	{
		return scheduleGroup;
	}

	/**
	 * @param scheduleGroup
	 * @uml.property  name="scheduleGroup"
	 */
	public void setScheduleGroup(String scheduleGroup)
	{
		this.scheduleGroup = scheduleGroup;
	}

	/**
	 * @return
	 * @uml.property  name="jobClass"
	 */
	public Job getJobClass()
	{
		return jobClass;
	}

	/**
	 * @param jobClass
	 * @uml.property  name="jobClass"
	 */
	public void setJobClass(Job jobClass)
	{
		this.jobClass = jobClass;
	}

	public SchedulerService()
	{
		schedulerFactory = new org.quartz.impl.StdSchedulerFactory();
	}

	public void afterPropertiesSet()
			throws Exception
	{
		HashMap<String, SchedulerConfigurator> result = (HashMap<String, SchedulerConfigurator>)InfraServiceProxy.getApplicationContext().getBeansOfType(SchedulerConfigurator.class);
		
		if (result != null && result.size() > 0)
		{
			Scheduler scheduler = schedulerFactory.getScheduler();
			scheduler.start();
			
			for (String keyName : result.keySet())
			{
				try
				{
					SchedulerConfigurator cfg = result.get(keyName);
					
					if (cfg != null)
					{
						Job jobObject = (Job) BundleUtil.getServiceByBeanName(cfg.getExecutionJobName());
						
						JobDetail jobDetail = new JobDetail(cfg.getSchedulerName(), scheduleGroup, jobObject.getClass());
						
						if (this.repeatInterval == 0 && this.repeatCount == 0)
						{
							CronTrigger trigger = new CronTrigger(cfg.getSchedulerName(), scheduleGroup);
							trigger.setCronExpression(cfg.getCronExpression());
							scheduler.scheduleJob(jobDetail, trigger);
						}
						else
						{
							SimpleTrigger trigger = new SimpleTrigger(cfg.getSchedulerName(), scheduleGroup);
							trigger.setRepeatCount(this.repeatCount);
							trigger.setRepeatInterval(this.repeatInterval);
							scheduler.scheduleJob(jobDetail, trigger);
						}
					}
				}
				catch (Exception ex)
				{
					//proceed to next
				}
			}
		}
	}
	
}
