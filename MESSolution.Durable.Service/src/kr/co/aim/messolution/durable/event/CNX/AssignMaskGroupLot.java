package kr.co.aim.messolution.durable.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.EVAReserveSpec;
import kr.co.aim.messolution.extended.object.management.data.MaskGroupList;
import kr.co.aim.messolution.extended.object.management.data.MaskGroupLot;
import kr.co.aim.messolution.extended.object.management.impl.EVAReserveSpecService;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

public class AssignMaskGroupLot extends SyncHandler {
	private  Log log = LogFactory.getLog(AssignMaskGroupLot.class);
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{   
		String actionName = SMessageUtil.getBodyItemValue(doc, "ACTIONNAME", true);
		List<Element> MaskLotList = SMessageUtil.getBodySequenceItemList(doc, "MASKLOTLIST", false);

		for (Element maskLot : MaskLotList )
		{   
			String maskGroupName = SMessageUtil.getChildText(maskLot, "MASKGROUPNAME", true);	
			String maskLotName = SMessageUtil.getChildText(maskLot, "MASKLOTNAME", true);	
			String maskType = SMessageUtil.getChildText(maskLot, "MASKTYPE", true);
			String stage = SMessageUtil.getChildText(maskLot, "STAGE", true);

			if (StringUtil.equals(actionName, "Save"))
			{
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("AssignMaskGroupLot", this.getEventUser(), this.getEventComment(), "", "", "");
				eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());
				
				MaskGroupLot dataInfo = new MaskGroupLot();
				
				try
				{
					dataInfo = ExtendedObjectProxy.getMaskGroupLotService().selectByKey(false, new Object[] { maskGroupName, maskLotName});
					
					dataInfo.setMaskType(maskType);
					dataInfo.setStage(stage);
					dataInfo.setLastEventName(eventInfo.getEventName());
					dataInfo.setLastEventTime(eventInfo.getEventTime());
					dataInfo.setLastEventTimekey(eventInfo.getLastEventTimekey());
					dataInfo.setLastEventUser(eventInfo.getEventUser());
					dataInfo.setLastEventComment(eventInfo.getEventComment());
					
					ExtendedObjectProxy.getMaskGroupLotService().modify(eventInfo, dataInfo);
				}
				catch (Exception e)
				{
					dataInfo.setMaskGroupName(maskGroupName);
					dataInfo.setMaskLotName(maskLotName);
					dataInfo.setMaskType(maskType);
					dataInfo.setStage(stage);
					dataInfo.setLastEventName(eventInfo.getEventName());
					dataInfo.setLastEventTime(eventInfo.getEventTime());
					dataInfo.setLastEventTimekey(eventInfo.getLastEventTimekey());
					dataInfo.setLastEventUser(eventInfo.getEventUser());
					dataInfo.setLastEventComment(eventInfo.getEventComment());			
					
					ExtendedObjectProxy.getMaskGroupLotService().create(eventInfo, dataInfo);
				}
				
			}
			else if(StringUtil.equals(actionName, "Delete"))
			{
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeassignMaskGroupLot", this.getEventUser(), this.getEventComment(), "", "", "");
				eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());
				
				MaskGroupLot dataInfo = ExtendedObjectProxy.getMaskGroupLotService().selectByKey(false, new Object[] { maskGroupName, maskLotName});
				ExtendedObjectProxy.getMaskGroupLotService().remove(eventInfo, dataInfo);
			}
		}
		return doc;
	}
	
}
