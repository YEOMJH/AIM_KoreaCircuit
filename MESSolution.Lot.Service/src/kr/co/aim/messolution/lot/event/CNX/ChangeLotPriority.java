package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ChangeLotPriority extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String lotPriority = SMessageUtil.getBodyItemValue(doc, "PRIORITY", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		String lotHoldState = lotData.getLotHoldState();
		String reasonCode = lotData.getReasonCode();
		String reasonCodeType = lotData.getReasonCodeType();

		if (!StringUtils.equals(processFlowName, lotData.getProcessFlowName()))
			throw new CustomException("LOT-0138", lotData.getProcessFlowName());

		if (!StringUtils.equals(processOperationName, lotData.getProcessOperationName()))
			throw new CustomException("LOT-0139", lotData.getProcessOperationName());

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeLotPriority", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		lotData = MESLotServiceProxy.getLotServiceUtil().ReleaseHoldLot(eventInfo, lotData);

		List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUSequence(lotName);
		eventInfo.setEventName("ChangeLotPriority");

		ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotData, lotData.getAreaName(), lotData.getDueDate(), lotData.getFactoryName(), lotData.getLotHoldState(),
				lotData.getLotProcessState(), lotData.getLotState(), ""/* nodeStack */, Long.parseLong(lotPriority), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
				lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), lotData.getProductionType(), lotData.getProductRequestName(), "", "", lotData.getProductSpecName(),
				lotData.getProductSpecVersion(), productUdfs, lotData.getSubProductUnitQuantity1(), lotData.getSubProductUnitQuantity2());

		MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);

		if (StringUtils.equals(lotHoldState, "Y"))
		{
			makeOnHoldForChangePriority(eventInfo, lotData, reasonCode, reasonCodeType);
		}

		return doc;
	}

	private void makeOnHoldForChangePriority(EventInfo eventInfo, Lot lotData, String reasonCode, String reasonCodeType) throws CustomException
	{
		eventInfo.setReasonCode(reasonCode);
		eventInfo.setReasonCodeType(reasonCodeType);
		eventInfo.setEventComment("ChangeLotPriority [" + lotData.getKey().getLotName() + "], " + eventInfo.getEventComment());

		Map<String, String> udfs = new HashMap<String, String>(); 

		if (StringUtil.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Released) && StringUtil.equals(lotData.getLotProcessState(), "WAIT"))
		{
			MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, lotData, udfs);
		}
		else
		{
			throw new CustomException("LOT-0113", lotData.getLotState(), lotData.getLotProcessState());
		}
	}
}
