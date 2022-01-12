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
import org.jdom.Element;
public class DeleteAbnormalEQP extends SyncHandler{
	private static Log log = LogFactory.getLog(DeleteAbnormalEQP.class);
	@Override	
	public Object doWorks(Document doc) throws CustomException
	{
		String rabnormalName = SMessageUtil.getBodyItemValue(doc, "ABNORMALNAME", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteAbnormalEQP", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		
		AbnormalEQP dtAbnormal = ExtendedObjectProxy.getAbnormalEQPService().selectByKey(false, new String[]{rabnormalName});
		
		if(!dtAbnormal.getAbnormalState().contains("Created"))
		{
			log.info("abnormalState is not Created");
			
			//CUSTOM-0027:abnormalState is not Created
			throw new CustomException("CUSTOM-0027");
		}
		
		ExtendedObjectProxy.getAbnormalEQPService().remove(eventInfo,dtAbnormal);
		return doc;		
	}
	
}
