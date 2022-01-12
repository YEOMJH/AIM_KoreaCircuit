package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;

import org.jdom.Document;
import org.jdom.Element;

public class ChangeOperationForSort extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeFlowToSort", getEventUser(), getEventComment(), null, null);

		String toProcessFlowName = SMessageUtil.getBodyItemValue(doc, "TOPROCESSFLOWNAME", true);
		String toProcessOperationName = SMessageUtil.getBodyItemValue(doc, "TOPROCESSOPERATIONNAME", true);
		String toProcessOperationVer = SMessageUtil.getBodyItemValue(doc, "TOPROCESSOPERATIONVER", false);

		if (!StringUtil.isNotEmpty(toProcessOperationVer))
			toProcessOperationVer = "00001";

		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);

		for (Element lotE : lotList)
		{
			String lotName = SMessageUtil.getChildText(lotE, "LOTNAME", true);
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			CommonValidation.checkJobDownFlag(lotData);// ADD

			ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);

			if (StringUtil.equals(processFlowData.getProcessFlowType(), "Sort"))
				throw new CustomException("LOT-0111");

			ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSortSpecInfo(lotData, lotData.getAreaName(), lotData.getDueDate(), lotData.getFactoryName(),
					lotData.getLotHoldState(), lotData.getLotProcessState(), lotData.getLotState(), lotData.getPriority(), toProcessFlowName, "00001", toProcessOperationName, "00001",
					lotData.getProductionType(), lotData.getProductRequestName(), lotData.getProductSpec2Name(), lotData.getProductSpec2Version(), lotData.getProductSpecName(),
					lotData.getProductSpecVersion(), lotData.getSubProductUnitQuantity1(), lotData.getSubProductUnitQuantity2());
			lotData = MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
		}

		return doc;
	}
}
