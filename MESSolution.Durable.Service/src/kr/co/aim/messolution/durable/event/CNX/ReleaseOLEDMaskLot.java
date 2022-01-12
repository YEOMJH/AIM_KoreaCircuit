package kr.co.aim.messolution.durable.event.CNX;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskFrame;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.NodeStack;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpecKey;

public class ReleaseOLEDMaskLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		List<Element> MASKLIST = SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", true);

		for (Element mask : MASKLIST)
		{
			String maskLotName = SMessageUtil.getChildText(mask, "MASKLOTNAME", true);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReleaseMask", this.getEventUser(), this.getEventComment(), "", "");

			eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());
			MaskLot dataInfo = null;
			try
			{
				dataInfo = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskLotName });
			}
			catch (greenFrameDBErrorSignal nfds)
			{
				throw new CustomException("MASKLOT-0001", maskLotName);
			}
			if(!dataInfo.getMaskLotState().equals("Created"))
			{
				throw new CustomException("MASKLOT-0006", maskLotName);
			}
			String processFlowName = dataInfo.getMaskProcessFlowName();
			String frameName = dataInfo.getFrameName();

			// Set First Operation
			ProcessOperationSpec targetOperationData = CommonUtil.getFirstOperation(factoryName, processFlowName);
			ProcessOperationSpecKey processOperationKey = targetOperationData.getKey();

			String targetOperationName = processOperationKey.getProcessOperationName();
			String targerOperationVer = processOperationKey.getProcessOperationVersion();
			String targetNodeId = NodeStack.getNodeID(factoryName, processFlowName, targetOperationName, targerOperationVer);

			dataInfo.setMaskLotProcessState(constantMap.MaskLotProcessState_Wait);
			dataInfo.setMaskLotState(constantMap.MaskLotState_Released);
			dataInfo.setMaskProcessOperationName(targetOperationName);
			dataInfo.setMaskProcessOperationVersion(targerOperationVer);
			dataInfo.setNodeStack(targetNodeId);
			dataInfo.setReasonCode("");
			dataInfo.setReasonCodeType("");
			dataInfo.setMaskLotName(maskLotName);
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventTimeKey(eventInfo.getLastEventTimekey());
			dataInfo.setLastEventUser(eventInfo.getEventUser());

			ExtendedObjectProxy.getMaskLotService().modify(eventInfo, dataInfo);

			MaskFrame frameData = null;
			try {
				frameData = ExtendedObjectProxy.getMaskFrameService().selectByKey(false, new Object[] { frameName });
			} catch (greenFrameDBErrorSignal nfds) {
				throw new CustomException("FRAME-0002", frameName);
			}
			frameData.setFrameState("Released");
			frameData.setLastEventComment(eventInfo.getEventComment());
			frameData.setLastEventName(eventInfo.getEventName());
			frameData.setLastEventTime(eventInfo.getEventTime());
			frameData.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
			frameData.setLastEventUser(eventInfo.getEventUser());

			ExtendedObjectProxy.getMaskFrameService().modify(eventInfo, frameData);

		}

		return doc;
	}

}
