package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class MaskStageToMaskMapping extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		String stageName = SMessageUtil.getBodyItemValue(doc, "STAGENAME", true);
		List<Element> maskLotList = SMessageUtil.getBodySequenceItemList(doc, "MASKLOTLIST", false);
		List<MaskLot> dataInfoList = new ArrayList<MaskLot>();

		String condition = " WHERE RESERVESTAGE = ? ";
		Object[] bindSet = new Object[] { stageName };
		List<MaskLot> reservedMaskLotList = null;
		List<MaskLot> notReservedMaskLotList = new ArrayList<MaskLot>();

		try
		{
			reservedMaskLotList = ExtendedObjectProxy.getMaskLotService().select(condition, bindSet);
		}
		catch (greenFrameDBErrorSignal nfds)
		{
			reservedMaskLotList = new ArrayList<MaskLot>();
		}

		for (Element eleMaskLot : maskLotList)
		{
			String maskLotName = SMessageUtil.getChildText(eleMaskLot, "MASKLOTNAME", false);

			MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });
			String originalReserveStage = maskLotData.getReserveStage();
			if (StringUtil.equals(originalReserveStage, stageName))
				continue;
			maskLotData.setReserveStage(stageName);
			dataInfoList.add(maskLotData);
		}

		for (MaskLot reserved : reservedMaskLotList)
		{
			if (reserved == null)
				continue;
			Boolean addFlag = true;
			for (Element eleMaskLot : maskLotList)
			{
				String maskLotName = SMessageUtil.getChildText(eleMaskLot, "MASKLOTNAME", false);

				if (StringUtil.equals(reserved.getMaskLotName(), maskLotName))
					addFlag = false;
			}

			if (addFlag)
				notReservedMaskLotList.add(reserved);
		}

		for (MaskLot maskLot : notReservedMaskLotList)
		{
			maskLot.setReserveStage(null);
		}

		dataInfoList.addAll(notReservedMaskLotList);

		if (dataInfoList.size() == 0)
			throw new CustomException("MASK-0040");

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("MaskStageToMaskMapping", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		ExtendedObjectProxy.getMaskLotService().modify(eventInfo, dataInfoList);

		return doc;
	}
}