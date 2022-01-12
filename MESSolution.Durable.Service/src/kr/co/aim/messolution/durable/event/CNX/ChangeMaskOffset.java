package kr.co.aim.messolution.durable.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.CheckOffset;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;

import org.jdom.Document;

public class ChangeMaskOffset extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		String maskLotName = SMessageUtil.getBodyItemValue(doc, "MASKLOTNAME", true);
		String initialOffSetX = SMessageUtil.getBodyItemValue(doc, "INITIALOFFSETX", false);
		String initialOffSetY = SMessageUtil.getBodyItemValue(doc, "INITIALOFFSETY", false);
		String initialOffSetTheta = SMessageUtil.getBodyItemValue(doc, "INITIALOFFSETTHETA", false);
		String maskSpecName = SMessageUtil.getBodyItemValue(doc, "MASKSPECNAME", true);
		String maskProcessFlowName = SMessageUtil.getBodyItemValue(doc, "MASKPROCESSFLOWNAME", true);
		String maskProcessOperationName = SMessageUtil.getBodyItemValue(doc, "MASKPROCESSOPERATIONNAME", true);
		
		String offsetX1 = SMessageUtil.getBodyItemValue(doc, "TFEOFFSETX1", false);
		String offsetY1 = SMessageUtil.getBodyItemValue(doc, "TFEOFFSETY1", false);
		String offsetX2 = SMessageUtil.getBodyItemValue(doc, "TFEOFFSETX2", false);
		String offsetY2 = SMessageUtil.getBodyItemValue(doc, "TFEOFFSETY2", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeOffset", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());

		// Change Offset
		MaskLot dataInfo = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });
		dataInfo.setInitialOffSetX(initialOffSetX);
		dataInfo.setInitialOffSetY(initialOffSetY);
		dataInfo.setInitialOffSetTheta(initialOffSetTheta);
		dataInfo.setTFEOFFSETX1(offsetX1);
		dataInfo.setTFEOFFSETY1(offsetY1);
		dataInfo.setTFEOFFSETX2(offsetX2);
		dataInfo.setTFEOFFSETY2(offsetY2);
		dataInfo.setReasonCode("");
		dataInfo.setReasonCodeType("");
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventTimeKey(eventInfo.getLastEventTimekey());
		dataInfo.setLastEventUser(eventInfo.getEventUser());

		ExtendedObjectProxy.getMaskLotService().modify(eventInfo, dataInfo);

		String factoryName = dataInfo.getFactoryName();
		String maskOperationName = dataInfo.getMaskProcessOperationName();
		String maskOperationVersion = dataInfo.getMaskProcessOperationVersion();

		ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(factoryName, maskOperationName, maskOperationVersion);

		if (operationData.getProcessOperationType().equals("Inspection") && dataInfo.getMaskLotProcessState().equals("RUN"))
		{
			// Change Flag
			CheckOffset offsetInfo = ExtendedObjectProxy.getCheckOffsetService().selectByKey(true, new Object[] { maskLotName, maskSpecName, maskProcessFlowName, maskProcessOperationName });
			offsetInfo.setCheckFlag(constantMap.Flag_Y);
			offsetInfo.setLastEventUser(eventInfo.getEventUser());

			ExtendedObjectProxy.getCheckOffsetService().modify(eventInfo, offsetInfo);
		}

		return doc;
	}

}
