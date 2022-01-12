package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MainReserveSkip;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.jdom.Document;
import org.jdom.Element;

public class CreateMainReserveSkip extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion= SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		List<Element> operationList = SMessageUtil.getBodySequenceItemList(doc, "OPERATIONLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateMainReserveSkip", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		
		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
		
		CommonValidation.checkDummyProductReserve(lotData);
		
		for (Element operation : operationList)
		{
			String processOperationName = operation.getChildText("PROCESSOPERATIONNAME");
			String processOperationVersion = operation.getChildText("PROCESSOPERATIONVERSION");

			MainReserveSkip mainReserveSkipData = ExtendedObjectProxy.getMainReserveSkipService().getMainResrveSkipData(lotName, processOperationName, processOperationVersion);

			if (mainReserveSkipData == null)
			{
				ExtendedObjectProxy.getMainReserveSkipService().createMainReserveSkip(eventInfo, lotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion,
						processOperationName, processOperationVersion);
			}
			else
			{
				throw new CustomException("LOT-0214", lotName, processOperationName, processOperationVersion);
			}
		}

		return doc;
	}

}
