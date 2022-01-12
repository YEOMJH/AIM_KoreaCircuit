package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.product.management.data.Product;

import org.jdom.Document;
import org.jdom.Element;

public class ResolveQueueTime extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = "";
		List<String> lotNameList = new ArrayList<>();
		List<Element> eleQTimeList = SMessageUtil.getBodySequenceItemList(doc, "QUEUETIMELIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);

		for (Element eleQTime : eleQTimeList)
		{
			String productName = SMessageUtil.getChildText(eleQTime, "PRODUCTNAME", true);
			String factoryName = SMessageUtil.getChildText(eleQTime, "FACTORYNAME", true);
			String processFlowName = SMessageUtil.getChildText(eleQTime, "PROCESSFLOWNAME", true);
			String processOperationName = SMessageUtil.getChildText(eleQTime, "PROCESSOPERATIONNAME", true);
			String toFactoryName = SMessageUtil.getChildText(eleQTime, "TOFACTORYNAME", true);
			String toProcessFlowName = SMessageUtil.getChildText(eleQTime, "TOPROCESSFLOWNAME", true);
			String toOperationName = SMessageUtil.getChildText(eleQTime, "TOPROCESSOPERATIONNAME", true);
			String reasonCode = SMessageUtil.getChildText(eleQTime, "REASONCODE", false);
			String department = SMessageUtil.getChildText(eleQTime, "DEPARTMENT", false);

			ExtendedObjectProxy.getProductQTimeService().resolveQTime(eventInfo, productName, factoryName, processFlowName, processOperationName, toFactoryName, toProcessFlowName, toOperationName, reasonCode,department);

			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
			lotName = productData.getLotName();

			if (!lotNameList.contains(lotName))
				lotNameList.add(lotName);
		}

		for (String lotNameByProduct : lotNameList)
		{
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotNameByProduct);
			setEventForce(eventInfo, lotData);
		}

		return doc;
	}

	private void setEventForce(EventInfo eventInfo, Lot lotData)
	{
		try
		{
			Lot newLotData = (Lot) ObjectUtil.copyTo(lotData);
			newLotData.setLastEventComment(eventInfo.getEventComment());
			newLotData.setLastEventName(eventInfo.getEventName());
			newLotData.setLastEventTime(eventInfo.getEventTime());
			newLotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			newLotData.setLastEventUser(eventInfo.getEventUser());
			newLotData.setReasonCode("");
			newLotData.setReasonCodeType("");

			LotServiceProxy.getLotService().update(newLotData);

			LotHistory HistoryData = new LotHistory();
			HistoryData = LotServiceProxy.getLotHistoryDataAdaptor().setHV(lotData, newLotData, HistoryData);

			LotServiceProxy.getLotHistoryService().insert(HistoryData);
		}
		catch (Exception e)
		{
			eventLog.info("Error occurred - setEventForce");
		}
	}
}
