package kr.co.aim.messolution.fmb.service.impl;

import java.text.ParseException;
import java.util.List;

import javax.xml.namespace.QName;

import kr.co.aim.messolution.fmb.service.FmbScheduler;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

public class FmbSchedulerImpl implements FmbScheduler {
	private Scheduler scheduler;
	private String namespaceURI = "http://bpel.aim.co.kr/bpelj/";
	private List<Schedule> scheduleList;
	

	public FmbSchedulerImpl() {
		SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        // Retrieve a scheduler from schedule factory
        try {
			scheduler = schedulerFactory.getScheduler();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	public void start() throws SchedulerException, ParseException {
		scheduler.start();
		for (Schedule schedule : scheduleList) {
			String bpelName = schedule.getBpel();
			CronTrigger cronJobTrigger = new CronTrigger(bpelName,"fmb",schedule.getCron());
			JobDetail jobDetail = new JobDetail(bpelName,"fmb",BpelExcuteJob.class);
			jobDetail.getJobDataMap().put("bpel", bpelName);
			scheduler.scheduleJob(jobDetail, cronJobTrigger);
		}
	}
	public void setNamespaceURI(String namespaceURI) {
		this.namespaceURI = namespaceURI;
	}
	public void setScheduleList(List<Schedule> scheduleList) {
		this.scheduleList = scheduleList;
		
	}

}
