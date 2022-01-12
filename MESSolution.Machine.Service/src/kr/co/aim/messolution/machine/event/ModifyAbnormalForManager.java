package kr.co.aim.messolution.machine.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AbnormalEQP;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;

import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
public class ModifyAbnormalForManager extends SyncHandler{
	private static Log log = LogFactory.getLog(ModifyAbnormalForManager.class);
	@Override	
	public Object doWorks(Document doc) throws CustomException
	{
		String abnormalName = SMessageUtil.getBodyItemValue(doc, "ABNORMALNAME", true);
		String reason = SMessageUtil.getBodyItemValue(doc, "REASON", true);
		String process = SMessageUtil.getBodyItemValue(doc, "PROCESS", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyAbnormalForManager", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		
		AbnormalEQP abnormalData = ExtendedObjectProxy.getAbnormalEQPService().selectByKey(false, new String[]{abnormalName});
		
		abnormalData.setReason(reason);
		abnormalData.setProcess(process);
		abnormalData.setConfirmFlag("Y");
		abnormalData.setLastEventComment(eventInfo.getEventComment());
		abnormalData.setLastEventName(eventInfo.getEventName());
		abnormalData.setLastEventTime(eventInfo.getEventTime());
		abnormalData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		abnormalData.setLastEventUser(eventInfo.getEventUser());
		
		abnormalData = ExtendedObjectProxy.getAbnormalEQPService().modify(eventInfo, abnormalData);	
		
		return doc;
	}
	
}
