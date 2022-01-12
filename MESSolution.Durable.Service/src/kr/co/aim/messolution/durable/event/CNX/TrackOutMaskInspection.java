package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.CheckOffset;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.NodeKey;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;

import org.jdom.Document;
import org.jdom.Element;

public class TrackOutMaskInspection extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String durableName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String processGroupName = SMessageUtil.getBodyItemValue(doc, "MASKGROUPNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "MASKPROCESSOPERATIONNAME", true);
		String nextOperationName = "";
		String nextOperationVersion = "";
		String nextNodeStack = "";
		List<Element> maskList = SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOut", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
		
		ProcessGroup processGroupData = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getProcessGroupData(processGroupName);

		List<MaskLot> dataInfoList = new ArrayList<MaskLot>();

		List<String> maskLotNameList = CommonUtil.makeList(SMessageUtil.getBodyElement(doc), "MASKLIST", "MASKLOTNAME");

		// Check SpecName, OperationName, ProcessFlowName
		List<Map<String, Object>> result = getMaskLotList(maskLotNameList);

		if (result.size() > 0)
		{
			if (result.size() == 1)
			{
				// OK
				for (Element mask : maskList)
				{
					String maskLotName = SMessageUtil.getChildText(mask, "MASKLOTNAME", true);
					String position = SMessageUtil.getChildText(mask, "POSITION", true);
					String maskSpecName = SMessageUtil.getChildText(mask, "MASKSPECNAME", true);
					String processFlowName = SMessageUtil.getChildText(mask, "MASKPROCESSFLOWNAME", true);

					// Check Offset
					ProcessOperationSpec OperationInfo = CommonUtil.getFirstOperation(factoryName, processFlowName);

					if (OperationInfo.getProcessOperationType().equals(constantMap.Mac_InspectUnit))
					{
						CheckOffset offsetInfo = ExtendedObjectProxy.getCheckOffsetService().selectByKey(false, new Object[] { maskLotName, maskSpecName, processFlowName, processOperationName });

						if (offsetInfo.getCheckFlag().equals(constantMap.Flag_N))
							throw new CustomException("CHANGEOFFSET-0001", maskLotName);
					}

					MaskLot dataInfo = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskLotName });
					if (dataInfo.getCarrierName() == durableName)
					{
						// Error
						throw new CustomException("MASKINSPECTION-0001", maskLotName);
					}
					else if (!StringUtil.isEmpty(dataInfo.getCarrierName()))
					{
						// Error 2
						throw new CustomException("MASKINSPECTION-0001", maskLotName);
					}
					else
					{
						dataInfo.setCarrierName(durableName);
						dataInfo.setPosition(position);
						dataInfo.setReasonCode("");
						dataInfo.setReasonCodeType("");
						dataInfo.setMaskLotProcessState("WAIT");
						dataInfo.setMachineName("");
						dataInfo.setPortName("");
						dataInfo.setMaskGroupName(processGroupName);
						dataInfo.setLastEventComment(eventInfo.getEventComment());
						dataInfo.setLastEventName(eventInfo.getEventName());
						dataInfo.setLastEventTime(eventInfo.getEventTime());
						dataInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
						dataInfo.setLastEventUser(eventInfo.getEventUser());
						dataInfo.setLastLoggedOutTime(eventInfo.getEventTime());
						dataInfo.setLastLoggedOutUser(eventInfo.getEventUser());

						// set Next Operation
						Node nextNode = PolicyUtil.getNextOLEDMaskOperation(dataInfo);

						NodeKey nodekey = nextNode.getKey();
						nextNodeStack = nodekey.getNodeId();
						nextOperationName = nextNode.getNodeAttribute1();
						nextOperationVersion = nextNode.getNodeAttribute2();

						dataInfo.setNodeStack(nextNodeStack);
						dataInfo.setMaskProcessOperationName(nextOperationName);
						dataInfo.setMaskProcessOperationVersion(nextOperationVersion);

						dataInfoList.add(dataInfo);
					}

				}
				ExtendedObjectProxy.getMaskLotService().modify(eventInfo, dataInfoList);

				int lotQty = ExtendedObjectProxy.getMaskLotService().assignedMaskLotQtyByCarrier(durableName);

				// Assign MaskGroup

				processGroupData.setMaterialQuantity(lotQty);

				MESProcessGroupServiceProxy.getProcessGroupServiceImpl().update(processGroupData);

				kr.co.aim.greentrack.processgroup.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.processgroup.management.info.SetEventInfo();
				MESProcessGroupServiceProxy.getProcessGroupServiceImpl().setEvent(processGroupData, setEventInfo, eventInfo);

				// Assign Carrier
				if (durableData.getCapacity() < durableData.getLotQuantity() + lotQty)
					throw new CustomException("MASKINSPECTION-0002", durableName);

				durableData.setLotQuantity(durableData.getLotQuantity() + lotQty);
				durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
				DurableServiceProxy.getDurableService().update(durableData);

				SetEventInfo setEventInfo1 = new SetEventInfo();
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo1, eventInfo);
			}
		}

		return doc;
	}

	private List<Map<String, Object>> getMaskLotList(List<String> maskLotNameList)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT DISTINCT MASKSPECNAME, MASKPROCESSOPERATIONNAME, MASKPROCESSFLOWNAME ");
		sql.append("  FROM CT_MASKLOT ");
		sql.append(" WHERE MASKLOTNAME IN (:MASKLOTLIST) ");

		Map<String, Object> inquirybindMap = new HashMap<String, Object>();
		inquirybindMap.put("MASKLOTLIST", maskLotNameList);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), inquirybindMap);

		return result;
	}

}
