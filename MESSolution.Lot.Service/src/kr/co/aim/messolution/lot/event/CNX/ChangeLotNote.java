package kr.co.aim.messolution.lot.event.CNX;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ChangeLotNote extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String lotNote = SMessageUtil.getBodyItemValue(doc, "LOTNOTE", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeLotNote", this.getEventUser(), this.getEventComment(), "", "");

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		CommonValidation.checkLotProcessState(lotData);

		if (!StringUtils.equals(productSpecName, lotData.getProductSpecName()))
			throw new CustomException("LOT-0137", lotData.getProductSpecName());

		if (!StringUtils.equals(processFlowName, lotData.getProcessFlowName()))
			throw new CustomException("LOT-0138", lotData.getProcessFlowName());

		if (!StringUtils.equals(processOperationName, lotData.getProcessOperationName()))
			throw new CustomException("LOT-0139", lotData.getProcessOperationName());

		SetEventInfo setEventInfo = new SetEventInfo();

		setEventInfo.getUdfs().put("LOTNOTE", lotNote);
		LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);

		return doc;
	}
}
