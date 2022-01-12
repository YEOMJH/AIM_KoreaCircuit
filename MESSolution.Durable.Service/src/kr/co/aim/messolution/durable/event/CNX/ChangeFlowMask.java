package kr.co.aim.messolution.durable.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class ChangeFlowMask extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		String maskLotName = SMessageUtil.getBodyItemValue(doc, "MASKLOTNAME", true);
		String newFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String newFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String newOperName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String newOperVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeFlowMask", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });

		// MaskLotProcessState Check
		if (maskLotData.getMaskLotProcessState().equalsIgnoreCase(constantMap.MaskLotProcessState_Run))
			throw new CustomException("OLEDMASK-0002", new Object[] { constantMap.MaskLotProcessState_Run + "(" + maskLotName + ")" });

		// MaskLotHoldState Check
		if (StringUtil.equals(maskLotData.getMaskLotHoldState(), constantMap.MaskLotHoldState_OnHold))
			throw new CustomException("MASK-0013", maskLotData.getMaskLotName());

		// MaskLotState Check
		if (!StringUtil.equals(maskLotData.getMaskLotState(), constantMap.MaskLotState_Released))
			throw new CustomException("MASK-0026", maskLotData.getMaskLotName(), maskLotData.getMaskLotState());
		//Check DownloadFlag
		if(StringUtil.equals(maskLotData.getJobDownFlag(), "Y"))
			throw new CustomException("MASKJOBFlag-0001", maskLotData.getMaskLotName());
		// If the mask is assigned to a carrier and the carrier has the other mask(s), deassign the mask from the carrier.
		// Because different operations can not exist in a carrier.
		if (StringUtil.isNotEmpty(maskLotData.getCarrierName()))
		{
			List<Map<String, Object>> result = getMaskLotCount(maskLotData.getCarrierName());

			int lotQty = Integer.parseInt(ConvertUtil.getMapValueByName(result.get(0), "LOTQTY"));
			if (lotQty != 1)
				deassignOLEDMaskLot(maskLotData.getCarrierName(), maskLotData);
		}

		String newNodeStack = CommonUtil.getNodeStack(maskLotData.getFactoryName(), newFlowName, newFlowVersion, newOperName, newOperVersion);
		maskLotData.setNodeStack(newNodeStack);
		maskLotData.setMaskProcessFlowName(newFlowName);
		maskLotData.setMaskProcessFlowVersion(newFlowVersion);
		maskLotData.setMaskProcessOperationName(newOperName);
		maskLotData.setMaskProcessOperationVersion(newOperVersion);

		ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotData);
		ExtendedObjectProxy.getMaskLotService().executePostAction(eventInfo, maskLotData);
		return doc;
	}

	private void deassignOLEDMaskLot(String durableName, MaskLot maskLotData) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeassignCarrierMask", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);

		maskLotData.setCarrierName("");
		maskLotData.setPosition("");
		maskLotData.setReasonCode("");
		maskLotData.setReasonCodeType("");
		maskLotData.setLastEventComment(eventInfo.getEventComment());
		maskLotData.setLastEventName(eventInfo.getEventName());
		maskLotData.setLastEventTime(eventInfo.getEventTime());
		maskLotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		maskLotData.setLastEventUser(eventInfo.getEventUser());

		ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotData);
		int lotQty = ExtendedObjectProxy.getMaskLotService().assignedMaskLotQtyByCarrier(durableName);

		// Set LotQuantity as MaskLotList & makeAvailable
		durableData.setLotQuantity(lotQty);

		if (lotQty == 0)
			durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);

		DurableServiceProxy.getDurableService().update(durableData);

		// Durable - SetEvent
		SetEventInfo setEventInfo = new SetEventInfo();
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
	}

	private List<Map<String, Object>> getMaskLotCount(String carrierName)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT COUNT (MASKLOTNAME) AS LOTQTY ");
		sql.append("  FROM CT_MASKLOT ");
		sql.append(" WHERE CARRIERNAME = :CARRIERNAME ");
		
		Map<String, String> args = new HashMap<String, String>();
		args.put("CARRIERNAME", carrierName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		return result;
	}
}
