package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.NodeStack;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.SampleMask;
import kr.co.aim.messolution.extended.object.management.data.TPFOPolicy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;

public class ModifyMaskCount extends SyncHandler {

	private static Log log = LogFactory.getLog(ModifyMaskCount.class);

	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String maskSpecName = SMessageUtil.getBodyItemValue(doc, "MASKSPECNAME", true);
		List<Element> MaskList = SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", true);
		// List<String> maskList = new ArrayList<String>();

		for (Element Mask : MaskList)
		{
			String maskLotName = SMessageUtil.getChildText(Mask, "MASKLOTNAME", true);
			MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });

			String MaskCleanCount = SMessageUtil.getChildText(Mask, "MASKCLEANCOUNT", false);
			String TimeUsed = SMessageUtil.getChildText(Mask, "TIMEUSED", false);
			String ReworkCount = SMessageUtil.getChildText(Mask, "REWORKCOUNT", false);
			String MaskRepairCount = SMessageUtil.getChildText(Mask, "MASKREPAIRCOUNT", false);

			//Validation
			CommonValidation.checkMaskLotHoldState(maskLotData);
			CommonValidation.checkMaskLotProcessStateRun(maskLotData);
			CommonValidation.checkMaskLotState(maskLotData);
			
			// Set Mask Info
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyMaskCount", this.getEventUser(), this.getEventComment(), "", "");
			maskLotData.setMaskCleanCount(StringUtil.isEmpty(MaskCleanCount) ? (maskLotData.getMaskCleanCount() == null ? 0 : maskLotData.getMaskCleanCount()) : Integer.valueOf(MaskCleanCount));
			maskLotData.setTimeUsed(StringUtil.isEmpty(TimeUsed) ? (maskLotData.getTimeUsed() == null ? 0 : maskLotData.getTimeUsed()) : Float.valueOf(TimeUsed));
			maskLotData.setReworkCount(StringUtil.isEmpty(ReworkCount) ? (maskLotData.getReworkCount() == null ? 0 : maskLotData.getReworkCount()) : Integer.valueOf(ReworkCount));
			maskLotData.setMaskRepairCount(StringUtil.isEmpty(MaskRepairCount) ? (maskLotData.getMaskRepairCount() == null ? 0 : maskLotData.getMaskRepairCount()) : Integer.valueOf(MaskRepairCount));
			String timeKey = TimeUtils.getCurrentEventTimeKey();
			eventInfo.setEventTimeKey(timeKey);
			eventInfo.setLastEventTimekey(timeKey);

			eventInfo.setEventUser(getEventUser());
			eventInfo.setEventName(eventInfo.getEventName());
			eventInfo.setEventComment(eventInfo.getEventComment());

			// update mask info and history
			ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotData);
		}

		return doc;
	}
}
