package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskFrame;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.MaskSpec;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class CreateOLEDMaskLot extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		List<Element> MaskLotList = SMessageUtil.getBodySequenceItemList(doc, "MASKLOTLIST", true);
		String maskKind = SMessageUtil.getBodyItemValue(doc, "MASKKIND", true);

		for (Element eledur : MaskLotList)
		{
			String sfactoryName = SMessageUtil.getChildText(eledur, "FACTORYNAME", true);
			String sMaskLotName = SMessageUtil.getChildText(eledur, "MASKLOTNAME", true);
			String sMaskType = SMessageUtil.getChildText(eledur, "MASKTYPE", true);
			String sMaskSpecName = SMessageUtil.getChildText(eledur, "MASKSPECNAME", true);
			String sMaskSubSpecName = SMessageUtil.getChildText(eledur, "MASKSUBSPECNAME", true);
			String sProductiontype = SMessageUtil.getChildText(eledur, "PRODUCTIONTYPE", true);
			String sMaskLotJudge = SMessageUtil.getChildText(eledur, "MASKLOTJUDGE", true);
			String sFrameName = SMessageUtil.getChildText(eledur, "FRAMENAME", true);
			String sCleanUsedLimit = SMessageUtil.getChildText(eledur, "CLEANUSEDLIMIT", false);
			String sTimeUsedLimit = SMessageUtil.getChildText(eledur, "TIMEUSEDLIMIT", false);;
			String sMachineRecipeName = SMessageUtil.getChildText(eledur, "MACHINERECIPENAME", false);
			String sThickness = SMessageUtil.getChildText(eledur, "THICKNESS", true);
			String sDurationUsedLimit = SMessageUtil.getChildText(eledur, "DURATIONUSEDLIMIT", false);
			String sMagnet = SMessageUtil.getChildText(eledur, "MAGNET", false);
			String sPriority = SMessageUtil.getChildText(eledur, "PRIORITY", false);
			String sMaskFilmLayer=SMessageUtil.getChildText(eledur, "MASKFILMLAYER", true);

			MaskLot searchedMaskLot = null;
			try
			{
				searchedMaskLot = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { sMaskLotName });
			}
			catch (greenFrameDBErrorSignal nfdes)
			{
				// Not found same mask
			}

			if (searchedMaskLot != null)
				throw new CustomException("MASK-0028", sMaskLotName);

			List<MaskLot> maskLotList = new ArrayList<MaskLot>();
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateMask", this.getEventUser(), this.getEventComment(), "", "");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			MaskSpec MaskSpecInfo = ExtendedObjectProxy.getMaskSpecService().selectByKey(false, new Object[] { sfactoryName, sMaskSpecName });
			String maskProcessFlowName = MaskSpecInfo.getMaskProcessFlowName();
			String maskProcessFlowVersion = MaskSpecInfo.getMaskProcessFlowVersion();
       
			MaskLot dataInfo = new MaskLot();
			dataInfo.setFactoryName(sfactoryName);
			dataInfo.setMaskLotName(sMaskLotName);
			dataInfo.setMaskKind(maskKind);
			dataInfo.setMaskType(sMaskType);
			dataInfo.setProductionType(sProductiontype);
			dataInfo.setMaskLotJudge(sMaskLotJudge);
			dataInfo.setCleanState(constantMap.Dur_Dirty);
			dataInfo.setMaskSpecName(sMaskSpecName);
			dataInfo.setMaskSubSpecName(sMaskSubSpecName);
			dataInfo.setMaskLotHoldState(constantMap.Lot_NotOnHold);
			dataInfo.setMaskLotState(constantMap.CreateState);
			dataInfo.setFrameName(sFrameName);
			dataInfo.setMaskProcessFlowName(maskProcessFlowName);
			dataInfo.setMaskProcessFlowVersion(maskProcessFlowVersion);
			dataInfo.setMachineRecipeName(sMachineRecipeName);
			dataInfo.setMaskThickness(sThickness);
			dataInfo.setCleanUsedLimit(StringUtil.isEmpty(sCleanUsedLimit) ? (MaskSpecInfo.getCleanUsedLimit() == null ? 0 : MaskSpecInfo.getCleanUsedLimit()) : Float.parseFloat(sCleanUsedLimit));
			dataInfo.setTimeUsedLimit(StringUtil.isEmpty(sTimeUsedLimit) ? (MaskSpecInfo.getTimeUsedLimit() == null ? 0 : MaskSpecInfo.getTimeUsedLimit()) : Float.parseFloat(sTimeUsedLimit));
			dataInfo.setDurationUsedLimit(StringUtil.isEmpty(sDurationUsedLimit) ? (MaskSpecInfo.getDurationUsedLimit() == null ? 0 : MaskSpecInfo.getDurationUsedLimit()) : Float
					.parseFloat(sDurationUsedLimit));
			dataInfo.setMaskCycleCount(0);
			dataInfo.setMagnet(StringUtil.isEmpty(sMagnet) ? 0 : Float.parseFloat(sMagnet ));
			dataInfo.setPriority(StringUtil.isEmpty(sPriority) ? 5 : Long.parseLong(sPriority));
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
			dataInfo.setLastEventUser(eventInfo.getEventUser());
			dataInfo.setTimeUsed(0f);
			dataInfo.setReworkState(constantMap.Prod_NotInRework);
			dataInfo.setReworkCount(0);
			dataInfo.setMaskCleanCount(0);
			dataInfo.setMaskFilmLayer(sMaskFilmLayer);

			maskLotList.add(dataInfo);
			ExtendedObjectProxy.getMaskLotService().create(eventInfo, maskLotList);

			MaskFrame frameData = null;
			try {
				frameData = ExtendedObjectProxy.getMaskFrameService().selectByKey(false, new Object[] { sFrameName });
			} catch (greenFrameDBErrorSignal nfds) {
				throw new CustomException("FRAME-0002", sFrameName);
			}
			frameData.setMaskLotName(sMaskLotName);
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
