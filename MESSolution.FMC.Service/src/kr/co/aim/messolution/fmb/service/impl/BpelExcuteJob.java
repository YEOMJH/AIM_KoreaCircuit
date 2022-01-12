package kr.co.aim.messolution.fmb.service.impl;

import kr.co.aim.greenframe.template.workflow.WorkflowServiceProxy;

import org.jdom.Document;
import org.jdom.Element;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class BpelExcuteJob implements Job{

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String bpel = (String) context.getJobDetail().getJobDataMap().get("bpel");
		Document document = new Document();
		Element message = new Element("message");
		Element header = new Element("header");
		header.addContent(new Element("messagename").setText(bpel));
		Element body = new Element("body");
		
		message.addContent(header);
		message.addContent(body);
		document.setRootElement(message);
		
		
		Object[] object = {document};
		WorkflowServiceProxy.getBpelExecuter().executeProcess( object, false, bpel);
	}

}
