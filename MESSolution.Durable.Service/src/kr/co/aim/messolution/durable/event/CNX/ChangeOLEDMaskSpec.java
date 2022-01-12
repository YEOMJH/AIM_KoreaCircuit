package kr.co.aim.messolution.durable.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.NodeStack;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class ChangeOLEDMaskSpec extends SyncHandler {

	private static Log log = LogFactory.getLog(ChangeOLEDMaskSpec.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String maskLotName = SMessageUtil.getBodyItemValue(doc, "MASKLOTNAME", true);
		String newMaskSpecName = SMessageUtil.getBodyItemValue(doc, "MASKSPECNAME", true);
		String newMaskProcessFlowName = SMessageUtil.getBodyItemValue(doc, "MASKPROCESSFLOWNAME", true);
		String newMaskProcessFlowVersion = SMessageUtil.getBodyItemValue(doc, "MASKPROCESSFLOWVERSION", true);
		String newMaskProcessOperationName = SMessageUtil.getBodyItemValue(doc, "MASKPROCESSOPERATIONNAME", true);
		String newMaskProcessOperationVersion = SMessageUtil.getBodyItemValue(doc, "MASKPROCESSOPERATIONVERSION", true);

		MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });

		// Get New nodeStack
		String NodeId = NodeStack.getNodeID(factoryName, newMaskProcessFlowName, newMaskProcessFlowVersion, newMaskProcessOperationName, newMaskProcessOperationVersion);
		log.info("Get New nodeStack");

		if (!StringUtils.isEmpty(NodeId))
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeProductSpec", getEventUser(), getEventComment(), "", "");
			eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

			// Set Mask Info
			maskLotData.setMaskSpecName(newMaskSpecName);
			maskLotData.setMaskProcessFlowName(newMaskProcessFlowName);
			maskLotData.setMaskProcessFlowVersion(newMaskProcessFlowVersion);
			maskLotData.setMaskProcessOperationName(newMaskProcessOperationName);
			maskLotData.setMaskProcessOperationVersion(newMaskProcessOperationVersion);
			maskLotData.setNodeStack(NodeId);
			maskLotData.setLastEventName(eventInfo.getEventName());
			maskLotData.setLastEventTime(eventInfo.getEventTime());
			maskLotData.setLastEventTimeKey(eventInfo.getLastEventTimekey());
			maskLotData.setLastEventUser(eventInfo.getEventUser());
			maskLotData.setLastEventComment(eventInfo.getEventComment());

			// update mask info and history
			ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotData);
		}
		else
		{
			throw new CustomException("MASK-0051");
		}

		return doc;
	}

}
