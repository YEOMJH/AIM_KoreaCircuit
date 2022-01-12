package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DummyProductReserve;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.jdom.Document;
import org.jdom.Element;

public class DeleteReserveDummyProduct extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteReserveDummyProduct", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		List<Element> operationList = SMessageUtil.getBodySequenceItemList(doc, "OPERATIONLIST", false);

		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);

		CommonValidation.checkLotHoldState(lotData);
		CommonValidation.checkLotState(lotData);
		CommonValidation.checkLotProcessStateWait(lotData);
		CommonValidation.checkSampleData(lotData);
		CommonValidation.checkFutureActionData(lotData);

		/*
		int currnetMaxSeq = getCurrendMaxSeq(doc);

		if (operationList.size() > 0)
		{
			List<DummyProductReserve> dataInfoList = ExtendedObjectProxy.getDummyProductReserveService().getDummyProductReserveDataList(lotName);

			for (DummyProductReserve dataInfo : dataInfoList)
			{
				int seq = Integer.parseInt(dataInfo.getSeq());

				if (seq > currnetMaxSeq)
					ExtendedObjectProxy.getDummyProductReserveService().remove(eventInfo, dataInfo);
			}
		}
		else
		{
			ExtendedObjectProxy.getDummyProductReserveService().deleteDummyProductReserve(eventInfo, lotName);
		}
		*/
		
		ExtendedObjectProxy.getDummyProductReserveService().deleteDummyProductReserve(eventInfo, lotName);
		
		return doc;
	}

	private int getCurrendMaxSeq(Document doc) throws CustomException
	{
		Element bodyElement = SMessageUtil.getBodyElement(doc);
		List<String> seqList = CommonUtil.makeList(bodyElement, "OPERATIONLIST", "SEQ");

		int maxSeq = 0;

		if (seqList.size() > 0)
			maxSeq = Integer.parseInt(seqList.get(seqList.size() - 1));

		return maxSeq;
	}
}
