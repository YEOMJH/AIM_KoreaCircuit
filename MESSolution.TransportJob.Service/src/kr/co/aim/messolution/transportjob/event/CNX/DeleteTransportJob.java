package kr.co.aim.messolution.transportjob.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import java.awt.Event;

import org.jdom.Document;


public class DeleteTransportJob extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String transportJob = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteTransportJob", getEventUser(), getEventComment(), "", "");
		
		TransportJobCommand transportJobCommand = ExtendedObjectProxy.getTransportJobCommand().selectByKey(false, new Object[] {transportJob});

		ExtendedObjectProxy.getTransportJobCommand().remove(eventInfo, transportJobCommand);
		
		return doc;
	}

}