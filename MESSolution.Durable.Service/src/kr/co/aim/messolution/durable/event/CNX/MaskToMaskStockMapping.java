package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

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

public class MaskToMaskStockMapping extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String maskStockName = SMessageUtil.getBodyItemValue(doc, "MASKSTOCKNAME", true);
		List<Element> maskLotList = SMessageUtil.getBodySequenceItemList(doc, "MASKLOTLIST", false);
		List<MaskLot> maskLotListToBeModified = new ArrayList<MaskLot>();

		// Mapped out
		String condition = " WHERE MASKSTOCK = ? ";
		Object[] bindSet = new Object[] { maskStockName };
		try
		{
			List<MaskLot> oldMaskLotList = ExtendedObjectProxy.getMaskLotService().select(condition, bindSet);
			
			for (int j = 0; j < oldMaskLotList.size(); j++)
			{
				String oldMaskName = oldMaskLotList.get(j).getMaskLotName();
				boolean isMatched = false;

				for (int i = 0; i < maskLotList.size(); i++)
				{
					String maskLotName = maskLotList.get(i).getChildText("MASKLOTNAME");
					if (StringUtil.equals(oldMaskName, maskLotName))
					{
						isMatched = true;
						break;
					}
				}

				if (isMatched == false)
				{
					MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { oldMaskName });
					maskLotData.setMaskStock("");
					maskLotListToBeModified.add(maskLotData);
				}
			}
		}
		catch (greenFrameDBErrorSignal nfdes)
		{
		}

		// Newly mapped in
		for (int i = 0; i < maskLotList.size(); i++)
		{
			String maskLotName = maskLotList.get(i).getChildText("MASKLOTNAME");

			MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskLotName });

			if (StringUtil.isEmpty(maskLotData.getMaskStock()))
			{
				maskLotData.setMaskStock(maskStockName);
				maskLotListToBeModified.add(maskLotData);
			}
		}

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("MaskToMaskStockMapping", this.getEventUser(), this.getEventComment(), "", "", "Y");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotListToBeModified);

		return doc;
	}
}