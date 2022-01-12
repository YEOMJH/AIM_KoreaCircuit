package kr.co.aim.messolution.durable.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.NodeStack;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.messolution.generic.util.CommonValidation;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class ChangeOperationForOLEDMask extends SyncHandler {

	private static Log log = LogFactory.getLog(ChangeOperationForOLEDMask.class);

	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);

		String changeMaskProcessFlowName = SMessageUtil.getBodyItemValue(doc, "CHANGEMASKPROCESSFLOWNAME", true);
		String changeMaskProcessFlowNameVersion = SMessageUtil.getBodyItemValue(doc, "CHANGEPROCESSFLOWVERSION", true);
		String changeMaskProcessOperationName = SMessageUtil.getBodyItemValue(doc, "CHANGEPROCESSOPERATIONNAME", true);
		String changeMaskProcessOperationNameVersion = SMessageUtil.getBodyItemValue(doc, "CHANGEPROCESSOPERATIONVERSION", true);
		List<Element> MaskList = SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeOperationForOLEDMask", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		// get new NodeID
		String NodeId = NodeStack.getNodeID(factoryName, changeMaskProcessFlowName, changeMaskProcessFlowNameVersion, changeMaskProcessOperationName, changeMaskProcessOperationNameVersion);
		log.info("Get New nodeStack");

		if (!StringUtils.isEmpty(NodeId))
		{
			for (Element Mask : MaskList)
			{
				String maskLotName = SMessageUtil.getChildText(Mask, "MASKLOTNAME", true);
				MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });

				CommonValidation.checkMaskLotHoldState(maskLotData);
				
				CommonValidation.checkMaskLotProcessStateRun(maskLotData);
					
				CommonValidation.checkMaskLotState(maskLotData);
				
				CommonValidation.checkMaskLotReworkState(maskLotData);
				// Set Mask Info				
				maskLotData.setMaskProcessFlowName(changeMaskProcessFlowName);
				maskLotData.setMaskProcessFlowVersion(changeMaskProcessFlowNameVersion);
				maskLotData.setMaskProcessOperationName(changeMaskProcessOperationName);
				maskLotData.setMaskProcessOperationVersion(changeMaskProcessOperationNameVersion);
				maskLotData.setNodeStack(NodeId);
				maskLotData.setLastEventName(eventInfo.getEventName());
				maskLotData.setLastEventTime(eventInfo.getEventTime());
				maskLotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
				maskLotData.setLastEventUser(eventInfo.getEventUser());
				maskLotData.setLastEventComment(eventInfo.getEventComment());

				// update mask info and history
				ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotData);
			}

		}
		else
		{
			throw new CustomException("MASK-0051");
		}
		return doc;
	}
}
