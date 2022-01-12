package kr.co.aim.messolution.durable.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskGroup;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class MaskGroupManagement extends SyncHandler {
	private  Log log = LogFactory.getLog(MaskGroupManagement.class);
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{   
		
		List<Element> MaskGroupList = SMessageUtil.getBodySequenceItemList(doc, "MASKGROUPLIST", false);
		String updateFlag = SMessageUtil.getBodyItemValue(doc, "UPDATEFLAG", true);
		String timeKey = TimeStampUtil.getCurrentEventTimeKey();

		for (Element Mask : MaskGroupList )
		{   
			String maskGroupName = SMessageUtil.getChildText(Mask, "MASKGROUPNAME", true);	
			String maskLotName = SMessageUtil.getChildText(Mask, "MASKLOTNAME", true);	
			String machineName = SMessageUtil.getChildText(Mask, "MACHINENAME", true);
			String unitName = SMessageUtil.getChildText(Mask, "UNITNAME", true);
			String msName = SMessageUtil.getChildText(Mask, "MSNAME", true);
			String portName = SMessageUtil.getChildText(Mask, "PORTNAME", true);	
			String subUnitName = SMessageUtil.getChildText(Mask, "SUBUNITNAME", true);
			String lineType = SMessageUtil.getChildText(Mask, "LINETYPE", true);
			String cstSlot = SMessageUtil.getChildText(Mask, "CSTSLOT", true);
			if(updateFlag.equals("Add"))
			{
				MaskGroup maskGroupData = new MaskGroup();
				try
				{
					EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyMaskGroup", this.getEventUser(), this.getEventComment(), "", "", "");
					eventInfo.setLastEventTimekey(timeKey);
					maskGroupData = ExtendedObjectProxy.getMaskGroupService().selectByKey(false,new Object[]{maskLotName});
					maskGroupData.setMaskGroupName(maskGroupName);
					maskGroupData.setMachineName(machineName);
					maskGroupData.setUnitName(unitName);
					maskGroupData.setMSName(msName);
					maskGroupData.setPortName(portName);
					maskGroupData.setSubUnitName(subUnitName);
					maskGroupData.setLineType(lineType);
					maskGroupData.setCSTSlot(cstSlot);
					maskGroupData.setLastEventTimekey(eventInfo.getLastEventTimekey());
					ExtendedObjectProxy.getMaskGroupService().modify(eventInfo, maskGroupData);
				}
				catch(Exception ex)
				{
					EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateMaskGroup", this.getEventUser(), this.getEventComment(), "", "", "");
					eventInfo.setLastEventTimekey(timeKey);
					
					MaskGroup dataInfo = new MaskGroup();
					dataInfo.setMaskGroupName(maskGroupName);
					dataInfo.setMaskLotName(maskLotName);
					dataInfo.setMachineName(machineName);
					dataInfo.setUnitName(unitName);
					dataInfo.setMSName(msName);
					dataInfo.setPortName(portName);
					dataInfo.setSubUnitName(subUnitName);
					dataInfo.setLineType(lineType);
					dataInfo.setCSTSlot(cstSlot);
					dataInfo.setLastEventName(eventInfo.getEventName());
					dataInfo.setLastEventTime(eventInfo.getEventTime());
					dataInfo.setLastEventTimekey(eventInfo.getLastEventTimekey());
					dataInfo.setLastEventUser(eventInfo.getEventUser());
					dataInfo.setLastEventComment(eventInfo.getEventComment());			
					
					ExtendedObjectProxy.getMaskGroupService().create(eventInfo, dataInfo);
				}
			}
			else if (updateFlag.equals("Delete"))
			{
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteMaskGroup", this.getEventUser(), this.getEventComment(), "", "", "");
				eventInfo.setLastEventTimekey(timeKey);
				MaskGroup maskGroupData = ExtendedObjectProxy.getMaskGroupService().selectByKey(false,new Object[]{maskLotName});
				ExtendedObjectProxy.getMaskGroupService().remove(eventInfo, maskGroupData);
			}		
		}
		return doc;
	}
	
}
