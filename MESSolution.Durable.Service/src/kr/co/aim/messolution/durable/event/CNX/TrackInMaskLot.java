package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.CheckOffset;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskRecipe;
import kr.co.aim.messolution.extended.object.management.impl.ReserveMaskRecipeService;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class TrackInMaskLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		List<Element> maskLotList = SMessageUtil.getBodySequenceItemList(doc, "MASKLOTLIST", false);
		String maskProcessFlowName = SMessageUtil.getBodyItemValue(doc, "MASKPROCESSFLOWNAME", true);
		String maskProcessFlowVersion = SMessageUtil.getBodyItemValue(doc, "MASKPROCESSFLOWVERSION", true);
		String maskProcessOperationName = SMessageUtil.getBodyItemValue(doc, "MASKPROCESSOPERATIONNAME", true);
		String maskSpecName = SMessageUtil.getBodyItemValue(doc, "MASKSPECNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String carrierState = SMessageUtil.getBodyItemValue(doc, "CARRIERSTATE", false);
		String eventName = SMessageUtil.getBodyItemValue(doc, "EVENTNAME", false);
		String machineRecipeName = "";

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		ProcessFlow flowData=getProcessFlowData(maskProcessFlowName,maskProcessFlowVersion);

		CommonValidation.checkMachineHold(machineData);
		
		boolean singleFlag = false;
		
		if(StringUtil.isNotEmpty(carrierName))
		{
			Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
			if (StringUtils.equals(carrierData.getDurableType(), "1M") && StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_EVA))
			{
				throw new CustomException("MASK-0084");
			}
		}
		else
		{
			singleFlag = true;
		}

		for (int i = 0; i < maskLotList.size(); i++)
		{
			machineRecipeName="";
			String maskLotName = maskLotList.get(i).getChildText("MASKLOTNAME");
			MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });

			// Check Mask Hold State
			if (maskLotData.getMaskLotHoldState().equals(GenericServiceProxy.getConstantMap().MaskLotHoldState_OnHold))
				throw new CustomException("MASK-0013", maskLotName);
			if (maskLotData.getMaskLotProcessState().equals(GenericServiceProxy.getConstantMap().MaskLotProcessState_Run))
				throw new CustomException("MASK-0079");

			if(singleFlag)
			{
				if(StringUtil.isNotEmpty(maskLotData.getCarrierName()))
						throw new CustomException("MASK-1000");
			}
			
			ProcessOperationSpec processOperationData = CommonUtil.getProcessOperationSpec(maskLotData.getFactoryName(), maskLotData.getMaskProcessOperationName(),
					maskLotData.getMaskProcessOperationVersion());

			if (!StringUtil.equalsIgnoreCase(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_EVA))
			{
				if (StringUtil.equals(flowData.getProcessFlowType(), "MQC")&&(StringUtils.equals(machineData.getMachineGroupName(), "MaskOrgCleaner")
						||StringUtils.equals(machineData.getMachineGroupName(), "MaskMetalCleaner")))
				{
					HashMap<String,String> MQCList = getMQCMachineRecipeName(maskLotName);
					if (MQCList.size()>0)
					{
						machineRecipeName = MQCList.get("RECIPENAME").toString();
						if(!MQCList.get("MACHINENAME").equals(machineName))
						{
							throw new CustomException("MACHINE-0115", machineName,MQCList.get("MACHINENAME").toString());
						}
					}
					else
					{
						throw new CustomException("MACHINE-0114", "");
					}
				}
				else
				{
					try
					{
						ReserveMaskRecipeService reserveMaskRecipeService = ExtendedObjectProxy.getReserveMaskRecipeService();
						Object[] keySet = new Object[] { maskLotName, maskLotData.getMaskSpecName(), maskLotData.getMaskProcessFlowName(), maskLotData.getMaskProcessFlowVersion(),
								maskLotData.getMaskProcessOperationName(), maskLotData.getMaskProcessOperationVersion(), machineName};
						ReserveMaskRecipe reserveMaskRecipe = reserveMaskRecipeService.selectByKey(true, keySet);

						machineRecipeName = reserveMaskRecipe.getRecipeName();
					}
					catch (greenFrameDBErrorSignal nfds)
					{
						machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipeForOLEDMask(maskLotData.getFactoryName(), maskLotData.getMaskSpecName(),
								maskLotData.getMaskProcessFlowName(), maskLotData.getMaskProcessFlowVersion(), maskLotData.getMaskProcessOperationName(),
								maskLotData.getMaskProcessOperationVersion(), machineName);
					}
				}
			}

			EventInfo eventInfoTrackInMask = EventInfoUtil.makeEventInfo(eventName, this.getEventUser(), this.getEventComment(), "", "", "Y");
			eventInfoTrackInMask.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			ExtendedObjectProxy.getMaskLotService().maskMakeLoggedIn(eventInfoTrackInMask, maskLotName, machineData, portName, maskProcessOperationName, machineRecipeName);

			// get next operation
			Node nextNode = PolicyUtil.getNextOLEDMaskOperation(maskLotData);
			// Check stocker operation
			if (StringUtil.equals(nextNode.getNodeType(), constantMap.Node_End) && StringUtil.equals(processOperationData.getDetailProcessOperationType(), "MASKSTOCKER"))
				throw new CustomException("MASK-0043");

			if (StringUtil.equals(processOperationData.getDetailProcessOperationType(), "EVA") && StringUtil.equals(maskLotData.getCleanState(), "Dirty"))
				throw new CustomException("DURABLE-9013", maskLotName);

			if (processOperationData.getProcessOperationType().equals(constantMap.Mac_InspectUnit))
			{
				CheckOffset checkOffsetInfo = new CheckOffset();
				checkOffsetInfo.setMaskLotName(maskLotName);
				checkOffsetInfo.setMaskSpecName(maskSpecName);
				checkOffsetInfo.setMaskProcessFlowName(maskProcessFlowName);
				checkOffsetInfo.setMaskProcessOperationName(maskProcessOperationName);
				checkOffsetInfo.setCheckFlag(constantMap.Flag_N);
				checkOffsetInfo.setLastEventUser(eventInfoTrackInMask.getEventUser());

				boolean offSetExist = true;
				try
				{
					ExtendedObjectProxy.getCheckOffsetService().selectByKey(true, new Object[] { maskLotName, maskSpecName, maskProcessFlowName, maskProcessOperationName });
				}
				catch (greenFrameDBErrorSignal nfdes)
				{
					offSetExist = false;
				}

				if (offSetExist)
					ExtendedObjectProxy.getCheckOffsetService().modify(eventInfoTrackInMask, checkOffsetInfo);
				else
					ExtendedObjectProxy.getCheckOffsetService().create(eventInfoTrackInMask, checkOffsetInfo);
			}
		}

		EventInfo eventInfoDeassignCarrierMask = EventInfoUtil.makeEventInfo("DeassignCarrierMask", this.getEventUser(), this.getEventComment(), "", "", "Y");
		eventInfoDeassignCarrierMask.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		if (StringUtil.isNotEmpty(carrierName))
			MESDurableServiceProxy.getDurableServiceImpl().durableStateChangeAfterOLEDMaskLotProcess(carrierName, carrierState, String.valueOf(maskLotList.size()), eventInfoDeassignCarrierMask);

		return doc;
	}
	
	private HashMap<String,String> getMQCMachineRecipeName(String maskLotName) throws CustomException
	{
		String machineRecipeName = "";
		String machineName = "";
		HashMap<String,String> MQCList = new HashMap<String,String>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT L.MASKLOTNAME, L.POSITION, D.RECIPENAME, D.MACHINENAME  ");
		sql.append("  FROM CT_MASKMQCPLAN M,  ");
		sql.append("       CT_MASKMQCPLANDETAIL D,  ");
		sql.append("       CT_MASKLOT L ");
		sql.append(" WHERE M.JOBNAME = D.JOBNAME  ");
		sql.append("   AND M.MQCSTATE = 'Released'  ");
		sql.append("   AND M.MASKLOTNAME = :MASKLOTNAME  ");
		sql.append("   AND M.MASKLOTNAME = D.MASKLOTNAME ");
		sql.append("   AND L.MASKLOTNAME = D.MASKLOTNAME ");
		sql.append("   AND M.MASKPROCESSFLOWNAME = L.MASKPROCESSFLOWNAME ");
		sql.append("   AND M.MASKPROCESSFLOWVERSION = L.MASKPROCESSFLOWVERSION ");
		sql.append("   AND D.MASKPROCESSFLOWNAME = L.MASKPROCESSFLOWNAME ");
		sql.append("   AND D.MASKPROCESSFLOWVERSION = L.MASKPROCESSFLOWVERSION ");
		sql.append("   AND D.MASKPROCESSOPERATIONNAME = L.MASKPROCESSOPERATIONNAME ");
		sql.append("   AND D.MASKPROCESSOPERATIONVERSION = L.MASKPROCESSOPERATIONVERSION ");
		sql.append(" ORDER BY L.POSITION ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("MASKLOTNAME", maskLotName);

		try
		{
			List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (result.size() > 0)
			{
				machineRecipeName = ConvertUtil.getMapValueByName(result.get(0), "RECIPENAME");
				machineName = ConvertUtil.getMapValueByName(result.get(0), "MACHINENAME");
				MQCList.put("RECIPENAME", machineRecipeName);
				MQCList.put("MACHINENAME",machineName);
			}
		}
		catch (Exception e)
		{

		}

		return MQCList;
	}
	
	private ProcessFlow getProcessFlowData(String  processFlowname,String processFlowVersion) throws CustomException
	{
		ProcessFlowKey processFlowKey = new ProcessFlowKey();
		processFlowKey.setFactoryName("OLED");
		processFlowKey.setProcessFlowName(processFlowname);
		processFlowKey.setProcessFlowVersion(processFlowVersion);
		ProcessFlow processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);

		return processFlowData;
	}
}
