package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;

public class ReserveDummyProduct extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);

		List<Element> operationList = SMessageUtil.getBodySequenceItemList(doc, "OPERATIONLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReserveDummyProduct", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
		
		CommonValidation.checkLotHoldState(lotData);
		CommonValidation.checkLotState(lotData);
		CommonValidation.checkLotProcessStateWait(lotData);
		CommonValidation.checkSampleData(lotData);
		CommonValidation.checkFutureActionData(lotData);
		
		for (Element operation : operationList)
		{
			String seq = SMessageUtil.getChildText(operation, "SEQ", true);
			String processOperationName = SMessageUtil.getChildText(operation, "PROCESSOPERATIONNAME", true);
			String processOperationVersion = SMessageUtil.getChildText(operation, "PROCESSOPERATIONVERSION", true);

			ExtendedObjectProxy.getDummyProductReserveService().createDummyProductReserve(eventInfo, lotName, seq, factoryName, productSpecName, productSpecVersion, processFlowName,
					processFlowVersion, processOperationName, processOperationVersion, productRequestName);
		}

		return doc;
	}

}
