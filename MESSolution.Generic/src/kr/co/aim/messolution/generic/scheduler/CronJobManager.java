package kr.co.aim.messolution.generic.scheduler;

import java.util.Map;

import kr.co.aim.greenframe.infra.InfraServiceProxy;
import kr.co.aim.greenframe.infra.SchedulerConfigurator;
import kr.co.aim.greenframe.util.bundle.BundleUtil;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;

public class CronJobManager extends AsyncHandler
{
    Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String jobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);
		String jobFlag = SMessageUtil.getBodyItemValue(doc, "JOBFLAG", true);
		String forceFlag = SMessageUtil.getBodyItemValue(doc, "FORCESTOP", false);

		SchedulerFactory schedulerFactory = new org.quartz.impl.StdSchedulerFactory();

		try
		{
			Scheduler scheduler = schedulerFactory.getScheduler();

			if (!scheduler.isStarted())
			{
				log.info("Scheduler service is stoped.");
				return;
			}

			if (!this.validateJobName(jobName))
			{
				this.printAvailableJobInfo();
				
				//CUSTOM-0011: Invalid JobName[{0}]:Not registered on configuration file.
				throw new CustomException("CUSTOM-0011", jobName);
			}

			if (jobFlag.equals("Start"))
			{
				if (this.IsStartedJob(scheduler, jobName))
					log.info(String.format("Job[%s] is already Started.", jobName));
				else
					this.setJobToStart(scheduler, jobName);
			}
			else if (jobFlag.equals("Stop"))
			{
				if(this.StringIsNotNullOrEmpty(forceFlag ))
				{
					this.stopJob(scheduler, jobName);
					return;
				}
				
				if (this.IsStopedJob(scheduler, jobName))
					log.info(String.format("Job[%s] is already stoped.", jobName));
				else
					this.setJobToStop(scheduler, jobName);
			}
			else
			{
				//CUSTOM-0019: Invalid Job Flag.
				throw new CustomException("CUSTOM-0019");
			}
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}
	}
	
	private boolean StringIsNotNullOrEmpty(String str)
	{
		return str == null || str.isEmpty() || str.trim().isEmpty() ? false:true;
	}
	
	private void  setJobToStart(Scheduler scheduler,String jobName) throws CustomException
	{
		int jobState = this.getJobState(scheduler, jobName);
		Trigger trigger = this.getScheduleJob(scheduler, jobName);

		try
		{
			if (trigger.STATE_PAUSED == jobState)
			{
				this.releaseJob(scheduler, jobName);
			}
			else if (trigger.STATE_NONE == jobState)
			{
				this.startJob(scheduler, jobName);
			}
			else if (trigger.STATE_COMPLETE == jobState || trigger.STATE_BLOCKED == jobState)
			{
				this.stopJob(scheduler, jobName);

				Thread.sleep(1000);

				this.startJob(scheduler, jobName);
			}
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}
	}
	
	private void  setJobToStop(Scheduler scheduler,String jobName) throws CustomException
	{
		int jobState = this.getJobState(scheduler, jobName);

		Trigger trigger = this.getScheduleJob(scheduler, jobName);

		try
		{
			if (trigger.STATE_NORMAL == jobState)
			{
				this.pauseJob(scheduler, jobName);
			}
			else if (trigger.STATE_COMPLETE == jobState || trigger.STATE_BLOCKED == jobState)
			{
				this.stopJob(scheduler, jobName);
			}
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}
	}
	
	private boolean validateJobName(String jobName) throws CustomException
	{
		if (jobName == null || jobName.isEmpty())
			throw new CustomException("Schedule job value is null or empty.");

		Map<String, SchedulerConfigurator> jobMap = InfraServiceProxy.getApplicationContext().getBeansOfType(SchedulerConfigurator.class);

		if (jobMap == null || jobMap.size() == 0)
			throw new CustomException("SYS-0010", "No schedule job information was found.");

		for (String keyStr : jobMap.keySet())
		{
			if (jobMap.get(keyStr).getSchedulerName().equals(jobName)) return true;
		}

		return false;
	}
	
	private void printAvailableJobInfo() throws CustomException
	{
		Map<String, SchedulerConfigurator> jobMap = InfraServiceProxy.getApplicationContext().getBeansOfType(SchedulerConfigurator.class);

		if (jobMap == null || jobMap.size() == 0)
			throw new CustomException("SYS-0010", "No schedule job information was found.");

		for (String keyStr : jobMap.keySet())
		log.info("Registered Job: " + jobMap.get(keyStr).getSchedulerName());
	}
	
	private int getJobState (Scheduler scheduler,String jobName) throws CustomException
	{
		try
		{
			return scheduler.getTriggerState(jobName, scheduler.getJobGroupNames()[0].toString());
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}
	}
	
	private boolean IsStartedJob(Scheduler scheduler,String jobName) throws CustomException
	{
		int jobState =  this.getJobState(scheduler, jobName);
		Trigger trigger = this.getScheduleJob(scheduler, jobName);
		
		return trigger.STATE_NORMAL == jobState ? true : false;
	}
	
	private boolean IsStopedJob(Scheduler scheduler,String jobName) throws CustomException
	{
		int jobState =  this.getJobState(scheduler, jobName);
		Trigger trigger = this.getScheduleJob(scheduler, jobName);
		
		return trigger.STATE_PAUSED == jobState || trigger.STATE_NONE == jobState ? true : false;
	}
	
	private void pauseJob(Scheduler scheduler,String jobName) throws CustomException
	{
		try
		{
			scheduler.pauseJob(jobName, scheduler.getJobGroupNames()[0].toString());
		
			log.info(String.format("Successfully paused the job[%s].",jobName));
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}
	}
	
	private void releaseJob(Scheduler scheduler,String jobName) throws CustomException
	{
		try
		{
			scheduler.resumeJob(jobName, scheduler.getJobGroupNames()[0].toString());
		
			log.info(String.format("Successfully release the job[%s].",jobName));
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}
	}
	
	private Trigger getScheduleJob(Scheduler scheduler, String jobName) throws CustomException
	{
		try
		{
			return scheduler.getTrigger(jobName, scheduler.getJobGroupNames()[0].toString());
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}
	}
	
	private void stopJob(Scheduler scheduler,String jobName) throws CustomException
	{
		try
		{
			scheduler.deleteJob(jobName, scheduler.getJobGroupNames()[0].toString());
		
			log.info(String.format("Successfully stop the job[%s].",jobName));
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}
	}
	
	private void startJob(Scheduler scheduler,String jobName) throws CustomException
	{
		try
		{
			String schGroup = scheduler.getJobGroupNames()[0].toString();
			SchedulerConfigurator jobConfig = this.getJobConfigurator(jobName);

			Job jobObject = (Job) BundleUtil.getServiceByBeanName(jobConfig.getExecutionJobName());

			JobDetail jobDetail = new JobDetail(jobConfig.getSchedulerName(), schGroup, jobObject.getClass());

			CronTrigger trigger = new CronTrigger(jobConfig.getSchedulerName(), schGroup);
			trigger.setCronExpression(jobConfig.getCronExpression());
			scheduler.scheduleJob(jobDetail, trigger);
			
			log.info(String.format("Successfully start the job[%s].",jobName));
		}
		catch (Exception ex)
		{
			if (ex instanceof CustomException)
				throw (CustomException) ex;
			else
				log.info(ex.getCause());
		}
	}
	
	private SchedulerConfigurator getJobConfigurator(String jobName) throws CustomException
	{
		Map<String, SchedulerConfigurator> jobMap = InfraServiceProxy.getApplicationContext().getBeansOfType(SchedulerConfigurator.class);

		if (jobMap == null || jobMap.size() == 0)
		{
			//CUSTOM-0012: No schedule job information was found.
			throw new CustomException("CUSTOM-0012");
		}

		for (String keyStr : jobMap.keySet())
		{
			if (jobMap.get(keyStr).getSchedulerName().equals(jobName)) return jobMap.get(keyStr);
		}
		
		//CUSTOM-0013: Unregistered job [{0}] information.
		throw new CustomException("CUSTOM-0013",jobName);
	}
}
