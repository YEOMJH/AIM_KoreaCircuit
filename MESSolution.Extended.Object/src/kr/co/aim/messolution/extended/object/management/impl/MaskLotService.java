package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MaskFrame;
import kr.co.aim.messolution.extended.object.management.data.MaskFutureAction;
import kr.co.aim.messolution.extended.object.management.data.MaskGroup;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.MaskMultiHold;
import kr.co.aim.messolution.extended.object.management.data.MaskStick;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskRecipe;
import kr.co.aim.messolution.extended.object.management.data.SampleMask;
import kr.co.aim.messolution.extended.object.management.data.SampleMaskCount;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.NodeStack;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.PortSpec;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.NodeKey;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpecKey;

public class MaskLotService extends CTORMService<MaskLot> {
	private static Log log = LogFactory.getLog(MaskLotService.class);
	
	public static Log logger = LogFactory.getLog(MaskLot.class);
	
	private final String historyEntity = "MaskLotHistory";

	ConstantMap constantMap = GenericServiceProxy.getConstantMap();
	
	public List<MaskLot> select(String condition, Object[] bindSet)
			throws greenFrameDBErrorSignal
	{
		List<MaskLot> result = super.select(condition, bindSet, MaskLot.class);
		
		return result;
	}
	
	public MaskLot selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MaskLot.class, isLock, keySet);
	}
	
	public MaskLot create(EventInfo eventInfo, MaskLot dataInfo)
		throws greenFrameDBErrorSignal
	{
		dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void create(EventInfo eventInfo, List<MaskLot> dataInfoList)
			throws greenFrameDBErrorSignal
	{		
		dataInfoList = setDataFromEventInfo(eventInfo, dataInfoList);
		
		super.insert(dataInfoList);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}
	
	public void remove(EventInfo eventInfo, MaskLot dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}

	public MaskLot modify(EventInfo eventInfo, MaskLot dataInfo)
	{
		dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void modify(EventInfo eventInfo, List<MaskLot> dataInfoList)
	{
		dataInfoList = setDataFromEventInfo(eventInfo, dataInfoList);
		
		super.update(dataInfoList);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}
	
	private MaskLot setDataFromEventInfo(EventInfo eventInfo, MaskLot dataInfo)
	{
		String eventTimekey = null;
		if(StringUtils.isNotEmpty(eventInfo.getLastEventTimekey()))
		{
			eventTimekey = eventInfo.getLastEventTimekey();
		}
		else if(StringUtils.isNotEmpty(eventInfo.getEventTimeKey()))
		{
			eventTimekey = eventInfo.getEventTimeKey();
		}
		else
		{
			eventTimekey = TimeStampUtil.getCurrentEventTimeKey();
		}
		
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventTimeKey(eventTimekey);
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		
		return dataInfo;
	}
	
	private List<MaskLot> setDataFromEventInfo(EventInfo eventInfo, List<MaskLot> dataInfoList)
	{
		for(MaskLot dataInfo : dataInfoList)
		{
			dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		}
		
		return dataInfoList;
	}
	
	public void setTransferState(EventInfo eventInfo, MaskLot maskLotData, String transferState) throws CustomException
	{
		// Set TransferState change info
		maskLotData.setTransferState(transferState);
		
		// Set event info
		maskLotData.setLastEventUser(eventInfo.getEventUser());
		maskLotData.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		maskLotData.setLastEventName(eventInfo.getEventName());
		maskLotData.setLastEventComment(eventInfo.getEventComment());
		maskLotData.setLastEventTime(eventInfo.getEventTime());
		maskLotData.setReasonCode(eventInfo.getReasonCode());
		maskLotData.setReasonCodeType(eventInfo.getReasonCodeType());

		ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotData);
	}
	
	public static List<String> makeList(List<MaskLot> valueList)
	{
		if(valueList == null || valueList.size() == 0)
		{
			return new ArrayList<String>();
		}
		
		List<String> selectionList = new ArrayList<>();
		
		for(MaskLot dataInfo: valueList)
		{
			selectionList.add(dataInfo.getMaskLotName());
		}
		
		return selectionList;
	}
	public List<MaskLot> getMaskLotByCarrier(String carrierName , boolean allowNullResult) throws CustomException
	{
		List<MaskLot> maskLotList = new ArrayList<MaskLot>();
		
		if(StringUtils.isEmpty(carrierName)) return maskLotList ;
		
		try
		{
			maskLotList = maskLotList = this.select(" WHERE 1=1 AND CARRIERNAME = ?  ORDER BY POSITION ",  new Object[] { carrierName });
		}
		catch (Exception ex)
		{
			if ( ex instanceof greenFrameDBErrorSignal && ((greenFrameDBErrorSignal) ex).getErrorCode().equals(ErrorSignal.NotFoundSignal))
			{
				if(allowNullResult)
				log.info("Could not find MaskLot information!! search by carrierName = [" + carrierName + "].");
				else 
					throw new CustomException("COMM-1000", "CT_MASKLOT","CarrierName = " +  carrierName);
			}
			else 
				throw new CustomException(ex.getCause());
		}
		
		return maskLotList;
	}

	@SuppressWarnings("unchecked")
	public int assignedMaskLotQtyByCarrier(String carrierName)
	{
		log.info("MaskLotQty by Carrier");
		int iLotQty = 0;
		String lotQty = "0";
		String sql = " SELECT COUNT(MASKLOTNAME) AS LOTQTY FROM CT_MASKLOT WHERE CARRIERNAME = :CARRIERNAME ";
		
		Map<String, String> args = new HashMap<String, String>();
		args.put("CARRIERNAME", carrierName);
		
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);
		
		if (result.size() > 0)
		{
			lotQty = ConvertUtil.getMapValueByName(result.get(0), "LOTQTY");
			iLotQty = Integer.parseInt(lotQty);
			
			log.info("lotQty : " + lotQty);
		}
		
		return iLotQty;
	}
	
	public MaskLot maskStateChange(EventInfo eventInfo, String maskLotName, String maskLotState)
			throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		// Get Mask Lot Data.
		MaskLot inputMaskLot = 
				ExtendedObjectProxy.getMaskLotService()
				.selectByKey(true, new Object[] { maskLotName });
		MaskLot outputMaskLot = 
				ExtendedObjectProxy.getMaskLotService()
				.selectByKey(true, new Object[] { maskLotName });
		
		// Set MaskState Change Info
		outputMaskLot.setMaskLotState(maskLotState);
		// Set EventInfo
		outputMaskLot.setLastEventUser(eventInfo.getEventUser());
		outputMaskLot.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		outputMaskLot.setLastEventName(eventInfo.getEventName());
		outputMaskLot.setLastEventComment(eventInfo.getEventComment());
		outputMaskLot.setLastEventTime(eventInfo.getEventTime());
		outputMaskLot.setReasonCode(eventInfo.getReasonCode());
		outputMaskLot.setReasonCodeType(eventInfo.getReasonCodeType());

		if (maskLotState.equals(constantMap.MaskLotState_Shipped))
		{
			outputMaskLot.setMaskLotProcessState("");
		}
		else if (maskLotState.equals(constantMap.MaskLotState_Created))
		{
			outputMaskLot.setMaskLotProcessState("");			
			outputMaskLot.setNodeStack("");
			outputMaskLot.setMaskProcessOperationName("");
			outputMaskLot.setLastEventName("CancelReleaseMask");
			outputMaskLot.setLastEventComment("CancelReleaseMask");			
			
		}
		else if (maskLotState.equals(constantMap.MaskLotState_Released))
		{
			if (inputMaskLot.getMaskLotState()
					.equals(constantMap.MaskLotState_Scrapped))
			{
				outputMaskLot.setMaskLotJudge("G");
			}
			outputMaskLot.setMaskLotProcessState(constantMap.MaskLotProcessState_Wait);
		}
		else if (maskLotState.equals(constantMap.MaskLotState_Scrapped))
		{
			outputMaskLot.setMaskLotProcessState("");
			outputMaskLot.setMaskLotJudge("S");
		}

		MaskLot returnedMaskLot = 
				ExtendedObjectProxy.getMaskLotService()
				.modify(eventInfo, outputMaskLot);
		
		return returnedMaskLot;
	}

	public void deassignMaskGroupByScrap(EventInfo eventInfo, MaskLot maskLotData)
			throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		// Set Mask Info
		MaskLot inputMaskLot = 
				ExtendedObjectProxy.getMaskLotService()
				.selectByKey(true, new Object[] { maskLotData.getMaskLotName() });
		
		ProcessGroup processGroupData = 
				MESProcessGroupServiceProxy.getProcessGroupServiceUtil()
				.getProcessGroupData(maskLotData.getMaskGroupName());
		ProcessGroup oldProcessGroupData = 
				MESProcessGroupServiceProxy.getProcessGroupServiceUtil()
				.getProcessGroupData(maskLotData.getMaskGroupName());
		
		maskLotData.setMaskGroupName("");
		processGroupData.setMaterialQuantity((oldProcessGroupData.getMaterialQuantity() - 1));

		processGroupData.setLastEventUser(eventInfo.getEventUser());
		processGroupData.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		processGroupData.setLastEventName(eventInfo.getEventName());
		processGroupData.setLastEventComment(eventInfo.getEventComment());
		processGroupData.setLastEventTime(eventInfo.getEventTime());

		int groupResult = MESProcessGroupServiceProxy.getProcessGroupServiceImpl().update(processGroupData);

		MaskLot maskResult = ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotData);

		if (groupResult == 1 && maskResult != null)
		{
			MESProcessGroupServiceProxy.getProcessGroupServiceImpl()
				.insertHistory(oldProcessGroupData, processGroupData);
		}
	}

	public static void checkOLEDMaskLotCleanState(MaskLot maskLotData, Machine machineData) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		boolean checkThrow = false;

		// MaskCleanState should be clean without Exchanger.
			if (maskLotData.getCleanState().equals(constantMap.Dur_Dirty)) {
				checkThrow = true;
			} else {
				checkThrow = false;
			}

		// When cleanMask , MaskCleanState should be dirty
		if (machineData.getUdfs().get("OPERATIONMODE").equals("Packer")) {
			if (maskLotData.getCleanState().equals(constantMap.Dur_Clean)) {
				checkThrow = true;
			}
		}

		if (checkThrow) {
			throw new CustomException("DURABLE-9013", maskLotData.getMaskLotName());
		}
	}
	
	public void deAssignMaskLotCarrier(EventInfo eventInfo, String maskLotName, String durableName) 
			throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		// Get Mask Lot Data.
		MaskLot inputMaskLot = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });
		MaskLot outputMaskLot = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });
		
		// Deassign Carrier Name
		if (!StringUtils.isEmpty(durableName))
		{
			if (!outputMaskLot.getCarrierName().equals(durableName)) {
				throw new CustomException("DURABLE-9011", maskLotName, outputMaskLot.getCarrierName(), durableName);
			}

			outputMaskLot.setCarrierName("");
			outputMaskLot.setPosition("");

			String timeKey = TimeUtils.getCurrentEventTimeKey();

			// Set EventInfo
			eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
			eventInfo.setEventTimeKey(timeKey);

			outputMaskLot.setLastEventUser(eventInfo.getEventUser());
			outputMaskLot.setLastEventTimeKey(eventInfo.getEventTimeKey());
			outputMaskLot.setLastEventName("DeassignCarrierMask");	
			outputMaskLot.setLastEventComment("DeassignCarrierMask");	
			outputMaskLot.setLastEventTime(eventInfo.getEventTime());

			MaskLot result = ExtendedObjectProxy.getMaskLotService().modify(eventInfo, outputMaskLot);
		}
	}
	
	public void AssignMaskLotCarrier(EventInfo eventInfo, String maskLotName, String durableName, String position) 
			throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		Durable durableData = 
				MESDurableServiceProxy.getDurableServiceUtil()
				.getDurableData(durableName);
		
		// Get Mask Lot Data.
		MaskLot inputMaskLot = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });
		MaskLot outputMaskLot = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });
		
		// Assign Carrier Name
		if (!StringUtils.isEmpty(durableName))
		{
			outputMaskLot.setCarrierName(durableName);
			outputMaskLot.setPosition(position);

			String timeKey = TimeUtils.getCurrentEventTimeKey();

			// Set EventInfo
			eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
			eventInfo.setEventTimeKey(timeKey);

			outputMaskLot.setLastEventUser(eventInfo.getEventUser());
			outputMaskLot.setLastEventTimeKey(eventInfo.getEventTimeKey());
			outputMaskLot.setLastEventName("AssignCarrierMask");	
			outputMaskLot.setLastEventComment("AssignCarrierMask");	
			outputMaskLot.setLastEventTime(eventInfo.getEventTime());

			MaskLot result = ExtendedObjectProxy.getMaskLotService().modify(eventInfo, outputMaskLot);
		}
	}
	
	public MaskLot makeReleasedAndLoggedIn(EventInfo eventInfo , MaskLot maskLot, String machineName, String recipeName,String carrierName) throws CustomException
	{
		eventInfo.setEventName("MaskMakeReleased");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		maskLot = this.makeReleased(eventInfo, maskLot, machineName, carrierName);
		
		eventInfo.setEventName("MaskTrackIn");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		return this.makeLoggedIn(eventInfo, maskLot, machineName, recipeName);
	}
	
	private MaskLot makeReleased(EventInfo eventInfo ,MaskLot maskLot , String machineName, String carrierName ) throws CustomException
	{
		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		
		if(!maskLot.getMaskLotState().equals(constMap.MaskLotState_Created)||!StringUtils.isEmpty(maskLot.getMaskLotProcessState()) 
		   || maskLot.getMaskLotHoldState().equals(constMap.MaskLotHoldState_OnHold))
		{
			//MASK-0108:Can not do MakeReleased at Mask State({0},{1},{2}).
			throw new CustomException("MASK-0108",maskLot.getMaskLotState(),maskLot.getMaskLotProcessState(),maskLot.getMaskLotHoldState());
		}
		
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		
		// Modify 20200708 dkh
		ProcessOperationSpec targetOperationData = CommonUtil.getFirstOperation(maskLot.getFactoryName(), maskLot.getMaskProcessFlowName());
		ProcessOperationSpecKey processOperationKey = targetOperationData.getKey();
		String targetOperationName = processOperationKey.getProcessOperationName();
		String targerOperationVer = processOperationKey.getProcessOperationVersion();
		String targetNodeId = NodeStack.getNodeID(maskLot.getFactoryName(),  maskLot.getMaskProcessFlowName(), targetOperationName, targerOperationVer);
		
		maskLot.setMaskLotState(constMap.MaskLotState_Released);
        maskLot.setMaskLotProcessState(constMap.MaskLotProcessState_Wait);
		maskLot.setAreaName(machineData.getAreaName());
		maskLot.setCarrierName(carrierName);
		
		maskLot.setNodeStack(targetNodeId);
		maskLot.setMaskProcessOperationName(targetOperationName);
		maskLot.setMaskProcessOperationVersion(targerOperationVer);
		
		maskLot.setLastEventName(eventInfo.getEventName());
		maskLot.setLastEventUser(eventInfo.getEventUser());
		maskLot.setLastEventTime(eventInfo.getEventTime());
		maskLot.setLastEventTimeKey(eventInfo.getEventTimeKey());
		maskLot.setLastEventComment(eventInfo.getEventComment());
		
		String frameName = maskLot.getFrameName();
		MaskFrame frameData = null;
		try {
			frameData = ExtendedObjectProxy.getMaskFrameService().selectByKey(false, new Object[] { frameName });
		} catch (greenFrameDBErrorSignal nfds) {
			throw new CustomException("FRAME-0002", frameName);
		}
		frameData.setFrameState("Released");
		frameData.setLastEventComment(eventInfo.getEventComment());
		frameData.setLastEventName(eventInfo.getEventName());
		frameData.setLastEventTime(eventInfo.getEventTime());
		frameData.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		frameData.setLastEventUser(eventInfo.getEventUser());

		ExtendedObjectProxy.getMaskFrameService().modify(eventInfo, frameData);

		return this.modify(eventInfo, maskLot);
	}
	
	private MaskLot makeLoggedIn(EventInfo eventInfo,MaskLot maskLot,String machineName,String recipeName) throws CustomException
	{
		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		eventInfo.setEventName("TrackInMask");
		
		if(!maskLot.getMaskLotState().equals(constMap.MaskLotState_Released)||!maskLot.getMaskLotProcessState().equals(constMap.MaskLotProcessState_Wait) 
		   || maskLot.getMaskLotHoldState().equals(constMap.MaskLotHoldState_OnHold))
		{
			//MASK-0109: Can not do makeLoggedIn at Mask State({0},{1},{2}).
			throw new CustomException("MASK-0109",maskLot.getMaskLotState(),maskLot.getMaskLotProcessState(),maskLot.getMaskLotHoldState());
		}
		
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		
        maskLot.setMaskLotProcessState(constMap.MaskLotProcessState_Run);
		maskLot.setAreaName(machineData.getAreaName());
		maskLot.setMachineName(machineName);
		maskLot.setMachineRecipeName(recipeName);
		maskLot.setJobDownFlag("Y");

		maskLot.setLastEventName(eventInfo.getEventName());
		maskLot.setLastEventUser(eventInfo.getEventUser());
		maskLot.setLastEventTime(eventInfo.getEventTime());
		//maskLot.setLastEventTimeKey(eventInfo.getEventTimeKey());
		maskLot.setLastEventComment(eventInfo.getEventComment());
		maskLot.setLastLoggedInUser(eventInfo.getEventUser());
		maskLot.setLastLoggedInTime(eventInfo.getEventTime());
		
		return this.modify(eventInfo, maskLot);
	}
	
	public MaskLot TrackOutMaskLot(EventInfo originalEventInfo,MaskLot trackOutMaskLot , Element maskElement,String skipFlag ) throws CustomException
	{		
		log.info("Start TrackOut MaskLot: " + trackOutMaskLot.getMaskLotName());
		boolean specHoldFlag = false;
		Boolean countHoldFlag=false;
		Boolean codeHoldFlag=false;
		String holdReasonCode = "";
		Boolean seriousAlarmByProcessingInfoF = false; 
		
        Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(maskElement.getChildText("MACHINENAME"));
		String processingInfo = maskElement.getChildText("PROCESSINGINFO");
		if(StringUtils.equals(processingInfo, "F")&&!StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_EVA))
		{
			seriousAlarmByProcessingInfoF = true;
			log.info("[MaskLotName,ProcessingInfo]: " + trackOutMaskLot.getMaskLotName() + ", " + processingInfo);
		}
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOutMask", originalEventInfo.getEventUser(), originalEventInfo.getEventComment());
        ConstantMap constMap = GenericServiceProxy.getConstantMap();
		

		ProcessFlow processFlowData = ExtendedObjectProxy.getMaskLotService().getProcessFlowData(trackOutMaskLot);
		ProcessOperationSpec processOperationData = CommonUtil.getProcessOperationSpec(trackOutMaskLot.getFactoryName(), trackOutMaskLot.getMaskProcessOperationName(),
																					   trackOutMaskLot.getMaskProcessOperationVersion());


		if(!trackOutMaskLot.getMaskLotState().equals(constMap.MaskLotState_Released)||!trackOutMaskLot.getMaskLotProcessState().equals(constMap.MaskLotProcessState_Run) 
				   || trackOutMaskLot.getMaskLotHoldState().equals(constMap.MaskLotHoldState_OnHold))
		{
			//MASK-0110: Can not do makeLoggedOut at Mask State({0},{1},{2}).
			throw new CustomException("MASK-0110",trackOutMaskLot.getMaskLotState(),trackOutMaskLot.getMaskLotProcessState(),trackOutMaskLot.getMaskLotHoldState());
		}

		trackOutMaskLot.setReasonCode("");
		trackOutMaskLot.setReasonCodeType("");
		trackOutMaskLot.setJobDownFlag("");
		trackOutMaskLot.setMaskLotProcessState(constMap.MaskLotProcessState_Wait);
		trackOutMaskLot.setAreaName(machineData.getAreaName());
		trackOutMaskLot.setMachineName(maskElement.getChildText("MACHINENAME"));
		trackOutMaskLot.setMachineRecipeName(maskElement.getChildText("MASKRECIPENAME"));
		trackOutMaskLot.setCarrierName(maskElement.getChildText("CARRIERNAME"));
		trackOutMaskLot.setPosition(maskElement.getChildText("POSITION"));
		trackOutMaskLot.setPortName(maskElement.getChildText("PORTNAME"));
		trackOutMaskLot.setPortType(maskElement.getChildText("PORTTYPE"));
		trackOutMaskLot.setMaskLotJudge(maskElement.getChildText("MASKJUDGE"));
		if(StringUtils.equalsIgnoreCase(processOperationData.getDetailProcessOperationType(), "TENSION"))
		{
			trackOutMaskLot.setMaskFlowState(maskElement.getChildText("MASKFLOWSTATE"));
		}
		trackOutMaskLot.setProcessIngInfo(maskElement.getChildText("PROCESSINGINFO"));
		
//		if (processFlowData.getUdfs().get("DETAILPROCESSFLOWTYPE").equals("Eva") && processOperationData.getProcessOperationType().equals(GenericServiceProxy.getConstantMap().Mac_InspectUnit))
//		{
//			trackOutMaskLot.setEvaOffSetTheta(maskElement.getChildText("INSPECTION_OFFSET_THETA"));
//			trackOutMaskLot.setEvaOffSetX(maskElement.getChildText("INSPECTION_OFFSET_X"));
//			trackOutMaskLot.setEvaOffSetY(maskElement.getChildText("INSPECTION_OFFSET_Y"));
//		}
//		if (processFlowData.getUdfs().get("DETAILPROCESSFLOWTYPE").equals("Creation") && processOperationData.getProcessOperationType().equals(GenericServiceProxy.getConstantMap().Mac_InspectUnit))
//		{
//			trackOutMaskLot.setInitialOffSetTheta(maskElement.getChildText("INSPECTION_OFFSET_THETA"));
//			trackOutMaskLot.setInitialOffSetX(maskElement.getChildText("INSPECTION_OFFSET_X"));
//			trackOutMaskLot.setInitialOffSetY(maskElement.getChildText("INSPECTION_OFFSET_Y"));
//		}
		if (processFlowData.getUdfs().get("DETAILPROCESSFLOWTYPE").equals("Tension") && processOperationData.getProcessOperationType().equals(GenericServiceProxy.getConstantMap().Mac_InspectUnit))
		{
			trackOutMaskLot.setTensionJudge(maskElement.getChildText("MASKJUDGE"));
		}

		//EQP Can not update MaskThinkness 2021/5/19
//		String maskThick = maskElement.getChildText("MASKTHICKNESS") == null ? "":maskElement.getChildText("MASKTHICKNESS") ;
//		
//		if (StringUtils.isNotEmpty(maskThick))
//		{
//			if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_MaskPPA) || StringUtils.equals(machineData.getMachineGroupName(), "TENSION")
//			   || (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_EVA)  && StringUtils.equals(trackOutMaskLot.getMaskKind(), "EVA")))
//			{
//				trackOutMaskLot.setMaskThickness(String.valueOf(Double.valueOf(maskThick) / 10000));
//			}
//			else
//			{
//				trackOutMaskLot.setMaskThickness(maskThick);
//			}
//		}
		
		log.info("DetailProcessOperationType : " + processOperationData.getDetailProcessOperationType());
		if (StringUtils.equals(trackOutMaskLot.getMaskType(), "FMM") && processOperationData.getProcessOperationType().equals("Production"))
		{
			trackOutMaskLot.setCleanState("Dirty");
			trackOutMaskLot.setCleanFlag("N");
		}
		
		if (StringUtils.equalsIgnoreCase(processOperationData.getDetailProcessOperationType(), "CLN"))
		{
			trackOutMaskLot.setCleanState("Clean");
			trackOutMaskLot.setCleanFlag("Y");
			trackOutMaskLot.setCleanStartTimekey(trackOutMaskLot.getCleanStartTimekey());
			trackOutMaskLot.setCleanEndTimekey(eventInfo.getEventTimeKey());
			trackOutMaskLot.setMaskCleanCount(trackOutMaskLot.getMaskCleanCount().intValue() + 1);
			trackOutMaskLot.setCleanTime(eventInfo.getEventTime());
			trackOutMaskLot.setLastCleanTimekey(eventInfo.getLastEventTimekey());
			trackOutMaskLot.setTimeUsed(0f);
			//add by cjl for clear AOICount and RepairCount 20201015
			if(StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_MaskOrgCleaner) || 
					StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_MaskMetalCleaner))
			{
				trackOutMaskLot.setAoiCount(0);
				trackOutMaskLot.setMaskRepairCount(0);				
			}
		}
		else if (StringUtils.equalsIgnoreCase(processOperationData.getDetailProcessOperationType(), "TENSION") || StringUtils.equalsIgnoreCase(processOperationData.getDetailProcessOperationType(), "EVA"))
		{
			// Mantis : 0000440
			// 只有EVA Mask在蒸镀或Tension下机才会变Dirty，TFE Mask不需要
			if (StringUtils.equalsIgnoreCase("EVA", trackOutMaskLot.getMaskKind()))
			{
				trackOutMaskLot.setCleanState("Dirty");
				trackOutMaskLot.setCleanFlag("N");
			}
			
			// compare assigned stick spec with mask subspec 
			if (StringUtils.equalsIgnoreCase(processOperationData.getDetailProcessOperationType(), "TENSION"))
			{
				List<String> maskSubSpecList = ExtendedObjectProxy.getMaskSubSpecService().getMaskSubSpecList(trackOutMaskLot.getMaskSpecName());
				List<MaskStick> stickDataList = ExtendedObjectProxy.getMaskStickService().getAssignedStickList(trackOutMaskLot.getMaskLotName());
				
				for (MaskStick stickData : stickDataList)
				{
					String stickSpec = stickData.getStickName().substring(1, 9);

					if (!maskSubSpecList.contains(stickSpec.substring(0,6)))
					{
						specHoldFlag = true;
						break;
					}
				}
			}
		}	
		else if(processOperationData.getDetailProcessOperationType().equals("MASKMACRO") || processOperationData.getDetailProcessOperationType().equals("MASKPPA")) //add by cjl 20201103
		{
			if(maskElement.getChildText("MASKJUDGE") == null || (!maskElement.getChildText("MASKJUDGE").equals("G") && !maskElement.getChildText("MASKJUDGE").equals("N")))
			{			
				//TODO ERROR
				String maskJudgeReport = maskElement.getChildText("MASKJUDGE");
				throw new CustomException("MASKLOTJUDGE-00001",maskJudgeReport);   //MaskLotJudge[{0}] is null or not G or N !
			}
			else if(maskElement.getChildText("MASKJUDGE").equals("N"))
			{
				codeHoldFlag = true;
				holdReasonCode ="Macro Judge Is N";
			}
		}
		else if (processOperationData.getDetailProcessOperationType().equals("MASKAOI") )
		{
			int aoiCount = trackOutMaskLot.getAoiCount()==null?0:trackOutMaskLot.getAoiCount().intValue();						
			
			//1.if maskLotJudge report is null or not G/N ,then error.
			if(maskElement.getChildText("MASKJUDGE") == null || (!maskElement.getChildText("MASKJUDGE").equals("G") && !maskElement.getChildText("MASKJUDGE").equals("N")))
			{			
				//TODO ERROR
				String maskJudgeReport = maskElement.getChildText("MASKJUDGE");
				throw new CustomException("MASKLOTJUDGE-00001",maskJudgeReport);   //MaskLotJudge[{0}] is null or not G or N !
			}
			else if(maskElement.getChildText("MASKJUDGE").equals("G"))
			{
				if(maskElement.getChildText("AOICODE") != null && StringUtils.isNotEmpty(maskElement.getChildText("AOICODE")) )
				{			
					//TODO ERROR
					String aoiCodeReport = maskElement.getChildText("AOICODE");
					throw new CustomException("MASKAOICODE-00002",aoiCodeReport); //when MaskJudge is G ,MaskAOICode[{0}] should be null !
				}
				trackOutMaskLot.setAoiCount(aoiCount +1);	
			}
			else if(maskElement.getChildText("MASKJUDGE").equals("N"))
			{
				if(maskElement.getChildText("AOICODE") == null || (!maskElement.getChildText("AOICODE").equals("1") && !maskElement.getChildText("AOICODE").equals("0")) )
				{			
					//TODO ERROR
					String aoiCodeReport = maskElement.getChildText("MASKJUDGE");
					throw new CustomException("MASKAOICODE-00001",aoiCodeReport); //MaskAOICode[{0}] is null or not 1 or 0 !
				}
				else if(maskElement.getChildText("AOICODE").equals("1") || maskElement.getChildText("AOICODE").equals("0"))
				{
					if(maskElement.getChildText("AOICODE").equals("1"))
					{
						trackOutMaskLot.setAoiCode(maskElement.getChildText("AOICODE"));
						codeHoldFlag = true;
						holdReasonCode = "AOI Code Is 1";
					}
					else if(maskElement.getChildText("AOICODE").equals("0"))
					{
						trackOutMaskLot.setAoiCode(maskElement.getChildText("AOICODE"));
					}						
					
					trackOutMaskLot.setMaskLotJudge(maskElement.getChildText("MASKJUDGE").toString());
					trackOutMaskLot.setAoiCount(aoiCount +1);															
				}
			}					
		}
		else if (processOperationData.getDetailProcessOperationType().equals("MASKREPAIR") )
		{			
			//1.if MaskRepairCOUNT >=2 ,then set countHoldFlag . //20201013 by cjl
			int repairCount = trackOutMaskLot.getMaskRepairCount() ==null ? 0 : trackOutMaskLot.getMaskRepairCount().intValue();	
			
			if((maskElement.getChildText("MASKJUDGE").equals("N")&&(repairCount+1)>=2)||(maskElement.getChildText("MASKJUDGE").equals("G")&&repairCount>=2)||(maskElement.getChildText("MASKJUDGE").equals("P")&&(repairCount+1)>=2))
			{
				countHoldFlag = true;
			}
			
			//2.if maskLotJudge report is null or not G/N ,then error. //20201013 by cjl
			if(maskElement.getChildText("MASKJUDGE") == null || (!maskElement.getChildText("MASKJUDGE").equals("G") && !maskElement.getChildText("MASKJUDGE").equals("N")
					&&  !maskElement.getChildText("MASKJUDGE").equals("P")))
			{			
				//TODO ERROR
				String maskJudgeReport = maskElement.getChildText("MASKJUDGE");
				throw new CustomException("MASKLOTJUDGE-00002",maskJudgeReport);   //MaskLotJudge[{0}] is null or not G ,N or P!
			}
			else if(maskElement.getChildText("MASKJUDGE").equals("G") || maskElement.getChildText("MASKJUDGE").equals("P"))
			{
				if(maskElement.getChildText("REPAIRCODE") != null && StringUtils.isNotEmpty(maskElement.getChildText("REPAIRCODE")))
				{			
					//TODO ERROR
					String repairCodeReport = maskElement.getChildText("REPAIRCODE");
					throw new CustomException("MASKREPAIRCODE-00002",repairCodeReport); //when MaskJudge is G or P ,REPAIRCODE[{0}] should be null !
				}
				trackOutMaskLot.setMaskRepairCount(repairCount +1);		
			}
			else if(maskElement.getChildText("MASKJUDGE").equals("N"))
			{
				if(maskElement.getChildText("REPAIRCODE") == null || (!maskElement.getChildText("REPAIRCODE").equals("1") && !maskElement.getChildText("REPAIRCODE").equals("0")) )
				{			
					//TODO ERROR
					String repairCodeReport = maskElement.getChildText("REPAIRCODE");
					throw new CustomException("MASKREPAIRCODE-00001",repairCodeReport); //MaskREPAIRCODE[{0}] is null or not 1 or 0 !
				}
				else if(maskElement.getChildText("REPAIRCODE").equals("1") || maskElement.getChildText("REPAIRCODE").equals("0"))
				{
					if(maskElement.getChildText("REPAIRCODE").equals("1"))
					{
						trackOutMaskLot.setRepairCode(maskElement.getChildText("REPAIRCODE"));
						codeHoldFlag = true; 
						holdReasonCode = "Repair Code Is 1";
					}
					else if(maskElement.getChildText("REPAIRCODE").equals("0"))
					{
						trackOutMaskLot.setRepairCode(maskElement.getChildText("REPAIRCODE"));
					}						
					
					trackOutMaskLot.setMaskLotJudge(maskElement.getChildText("MASKJUDGE").toString());
					trackOutMaskLot.setMaskRepairCount(repairCount +1);															
				}
			}	
			//Start 20210428 houxk
//			trackOutMaskLot.setCleanTime(eventInfo.getEventTime());
//			trackOutMaskLot.setLastCleanTimekey(eventInfo.getLastEventTimekey());
			//End
		}

		if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_EVA))
		{
			trackOutMaskLot.setMaskCycleCount(trackOutMaskLot.getMaskCycleCount().intValue()+1);
			//trackOutMaskLot.setChamberName(maskElement.getChildText("CHAMBERNAME"));
		}				

		String magnet = maskElement.getChildText("MAGNET");
		if (magnet != null && !magnet.isEmpty())
		{
			trackOutMaskLot.setMagnet(Float.valueOf(maskElement.getChildText("MAGNET")));
		}
		trackOutMaskLot.setLastEventComment(eventInfo.getEventComment());
		trackOutMaskLot.setLastEventName(eventInfo.getEventName());
		trackOutMaskLot.setLastEventTime(eventInfo.getEventTime());
		trackOutMaskLot.setLastEventTimeKey(eventInfo.getEventTimeKey());
		trackOutMaskLot.setLastEventUser(eventInfo.getEventUser());
		trackOutMaskLot.setLastLoggedOutTime(eventInfo.getEventTime());
		trackOutMaskLot.setLastLoggedOutUser(eventInfo.getEventUser());
		
		try
		{
			// If the mask recipe was reserved, add history mask recipe (in CT_ReserveMaskRecipeHist).
			log.info("Check ReserveMaskReciepeService");
			Object[] keySet = new Object[] { trackOutMaskLot.getMaskLotName(), trackOutMaskLot.getMaskSpecName(), trackOutMaskLot.getMaskProcessFlowName(),
											 trackOutMaskLot.getMaskProcessFlowVersion(),trackOutMaskLot.getMaskProcessOperationName(), trackOutMaskLot.getMaskProcessOperationVersion(),trackOutMaskLot.getMachineName() };
			
			ReserveMaskRecipe reservedRecipe = ExtendedObjectProxy.getReserveMaskRecipeService().selectByKey(true, keySet);

			if (reservedRecipe != null)
			{
				eventInfo.setEventName("UseReserveMaskRecipe");
				String historyEntity = ExtendedObjectProxy.getReserveMaskRecipeService().getHistoryEntity();
				ExtendedObjectProxy.getReserveMaskRecipeService().addHistory(eventInfo, historyEntity, reservedRecipe, ReserveMaskRecipeService.getLogger());
			}
		}
		catch (greenFrameDBErrorSignal nfds)
		{
		}

		// get next operation
		log.info("Get NextNode");
		Node nextNode = ExtendedObjectProxy.getExtendedProcessFlowUtil().getNextNode(trackOutMaskLot);

		NodeKey nodekey = nextNode.getKey();
		String nextNodeStack = nodekey.getNodeId();
		String nextProcessFlowName = nextNode.getProcessFlowName();
		String nextProcessFlowVersion = nextNode.getProcessFlowVersion();
		String nextOperationName = nextNode.getNodeAttribute1();
		String nextOperationVersion = nextNode.getNodeAttribute2();


		// If current flow is Sampling Flow
		log.info("ProcessFlowType : " + processFlowData.getProcessFlowType());
		if (StringUtils.equals(processFlowData.getProcessFlowType(), "Sample"))
		{
			SampleMask sampleMask = ExtendedObjectProxy.getSampleMaskService().getSampleMask(true, trackOutMaskLot.getMaskLotName(), trackOutMaskLot.getFactoryName(), trackOutMaskLot.getMaskSpecName(),
																								   trackOutMaskLot.getMaskProcessFlowName(), trackOutMaskLot.getMaskProcessFlowVersion(), 
																								   trackOutMaskLot.getMaskProcessOperationName(), trackOutMaskLot.getMaskProcessOperationVersion());

			if (sampleMask == null)
			{
				throw new CustomException("OLEDMASK-0011", trackOutMaskLot.getMaskLotName(), trackOutMaskLot.getMaskProcessOperationName());
			}

			// If current operation is last operation of Sampling Flow
			if (StringUtils.isEmpty(nextOperationName) && StringUtils.equals(nextNode.getNodeType(), GenericServiceProxy.getConstantMap().Node_End))
			{
				log.info("If current operation is last operation of Sampling Flow");
				String newNodeStack = CommonUtil.getNodeStack(trackOutMaskLot.getFactoryName(), sampleMask.getReturnProcessFlowName(), sampleMask.getReturnProcessFlowVersion(),
																			                    sampleMask.getReturnOperationName(), sampleMask.getReturnOperationVersion());

				nextNodeStack = newNodeStack;
				nextProcessFlowName = sampleMask.getReturnProcessFlowName();
				nextProcessFlowVersion = sampleMask.getReturnProcessFlowVersion();
				nextOperationName = sampleMask.getReturnOperationName();
				nextOperationVersion = sampleMask.getReturnOperationVersion();

				ExtendedObjectProxy.getSampleMaskService().remove(eventInfo, sampleMask);
			}
			else
			{
				// If current operation is not last operation of Sampling Flow
				log.info("If current operation is not last operation of Sampling Flow");
				SampleMask nextSampleMask = ExtendedObjectProxy.getSampleMaskService().getSampleMask(true, trackOutMaskLot.getMaskLotName(), trackOutMaskLot.getFactoryName(), trackOutMaskLot.getMaskSpecName(),
																										   nextProcessFlowName, nextProcessFlowVersion, nextOperationName, nextOperationVersion);

				if (nextSampleMask != null)
				{
					ExtendedObjectProxy.getSampleMaskService().remove(eventInfo, nextSampleMask);
				}

				SampleMask newSampleMask = new SampleMask(trackOutMaskLot.getMaskLotName(), trackOutMaskLot.getFactoryName(), trackOutMaskLot.getMaskSpecName(),
														  nextProcessFlowName, nextProcessFlowVersion, nextOperationName,nextOperationVersion, 
														  sampleMask.getReturnProcessFlowName(), sampleMask.getReturnProcessFlowVersion(), sampleMask.getReturnOperationName(),sampleMask.getReturnOperationVersion(),
														  eventInfo.getEventName(), eventInfo.getLastEventTimekey(), eventInfo.getEventTime(), eventInfo.getEventUser(),eventInfo.getEventComment());

				ExtendedObjectProxy.getSampleMaskService().remove(eventInfo, sampleMask);
				ExtendedObjectProxy.getSampleMaskService().create(eventInfo, newSampleMask);
			}

			trackOutMaskLot.setNodeStack(nextNodeStack);
			trackOutMaskLot.setMaskProcessFlowName(nextProcessFlowName);
			trackOutMaskLot.setMaskProcessFlowVersion(nextProcessFlowVersion);
			trackOutMaskLot.setMaskProcessOperationName(nextOperationName); // MASKPROCESSOPERATIONNAME move to next operation
			trackOutMaskLot.setMaskProcessOperationVersion(nextOperationVersion);
		}
		else if (StringUtils.equals(nextNode.getNodeType(), GenericServiceProxy.getConstantMap().Node_End)
				&& StringUtils.equals(trackOutMaskLot.getReworkState(), GenericServiceProxy.getConstantMap().Lot_InRework))
		{
			// If this operation is last operation of the Rework Flow
			log.info("If this operation is last operation of the Rework Flow");
			String returnNodeStack = trackOutMaskLot.getReturnSequenceId();
			Node returnNodeData = ProcessFlowServiceProxy.getProcessFlowService().getNode(returnNodeStack);

			trackOutMaskLot.setNodeStack(returnNodeStack);
			trackOutMaskLot.setMaskProcessFlowName(returnNodeData.getProcessFlowName());
			trackOutMaskLot.setMaskProcessFlowVersion(returnNodeData.getProcessFlowVersion());
			trackOutMaskLot.setMaskProcessOperationName(returnNodeData.getNodeAttribute1());
			trackOutMaskLot.setMaskProcessOperationVersion(returnNodeData.getNodeAttribute2());
			trackOutMaskLot.setReturnSequenceId("");
			trackOutMaskLot.setReworkState(GenericServiceProxy.getConstantMap().Lot_NotInRework);
		}
		else
		{
			// Skip the mask inspection operation according to sample policy.
			log.info("Skip the mask inspection operation according to sample policy");
			log.info("Check SkipFlag");
			if (skipFlag == null)
			{
				skipFlag = ExtendedObjectProxy.getMaskLotService().getSkipFlagForSampling(trackOutMaskLot);
				log.info("SkipFlag : " + skipFlag);
			}

			Map<String, Object> samplePolicy = ExtendedObjectProxy.getMaskLotService().getMaskSamplePolicyByMask(trackOutMaskLot);
			if (StringUtils.equals(skipFlag, "Y") && samplePolicy != null)
			{
				log.info("Skip Operation");
				eventInfo.setEventComment("Skipped Operation Name: [" + (String) samplePolicy.get("TOPROCESSOPERATIONNAME") + "]");
				String newProcessFlowName = (String) samplePolicy.get("TOPROCESSFLOWNAME");
				String newProcessFlowVersion = (String) samplePolicy.get("TOPROCESSFLOWVERSION");
				if (!StringUtils.equals(trackOutMaskLot.getMaskProcessFlowName(), newProcessFlowName) || !StringUtils.equals(trackOutMaskLot.getMaskProcessFlowVersion(), newProcessFlowVersion))
				{
					throw new CustomException("SAMPLE-0001");
				}

				String newProcessOperationName = (String) samplePolicy.get("RETURNOPERATIONNAME");
				String newProcessOperationVersion = (String) samplePolicy.get("RETURNOPERATIONVERSION");

				String newNodeStack = CommonUtil.getNodeStack(trackOutMaskLot.getFactoryName(), newProcessFlowName, newProcessFlowVersion, newProcessOperationName, newProcessOperationVersion);

				nextNodeStack = newNodeStack;
				nextOperationName = newProcessOperationName;
				nextOperationVersion = newProcessOperationVersion;
			}

			trackOutMaskLot.setNodeStack(nextNodeStack);
			trackOutMaskLot.setMaskProcessFlowName(nextProcessFlowName);
			trackOutMaskLot.setMaskProcessFlowVersion(nextProcessFlowVersion);
			trackOutMaskLot.setMaskProcessOperationName(nextOperationName); // MASKPROCESSOPERATIONNAME move to next operation
			trackOutMaskLot.setMaskProcessOperationVersion(nextOperationVersion);	
			
		}

		// modify 20200708 dkh
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventName("TrackOutMask");
		
		//add by cjl ,if RepairCount >= 2 , hold in STK ; if judge is N &&  AOI/REPAIR CODE='1' then hold in STK
		MaskLot returnMaskLotData  = ExtendedObjectProxy.getMaskLotService().modify(eventInfo, trackOutMaskLot);
		
		if(seriousAlarmByProcessingInfoF)
		{
			EventInfo holdEventInfo = EventInfoUtil.makeEventInfo("HoldMask", originalEventInfo.getEventUser(), originalEventInfo.getEventComment(), "SYSTEM", "AlarmHold");
			returnMaskLotData = ExtendedObjectProxy.getMaskLotService().maskMultiHold(holdEventInfo, trackOutMaskLot);
		}
	   
		if (countHoldFlag)
		{
			EventInfo holdEventInfo = EventInfoUtil.makeEventInfo("HoldMask", originalEventInfo.getEventUser(), originalEventInfo.getEventComment(), "SYSTEM", "RepairCountHold");
			returnMaskLotData = ExtendedObjectProxy.getMaskLotService().maskMultiHold(holdEventInfo, trackOutMaskLot);
		}
		
		if (codeHoldFlag)
		{
			EventInfo holdEventInfo = EventInfoUtil.makeEventInfo("HoldMask", originalEventInfo.getEventUser(), originalEventInfo.getEventComment(), "SYSTEM", holdReasonCode);
			returnMaskLotData = ExtendedObjectProxy.getMaskLotService().maskMultiHold(holdEventInfo, trackOutMaskLot);
		}
		
		if (specHoldFlag)
		{
			EventInfo holdEventInfo = EventInfoUtil.makeEventInfo("HoldMask", originalEventInfo.getEventUser(), originalEventInfo.getEventComment(), "SYSTEM", "DiffSpecHold");
			returnMaskLotData = ExtendedObjectProxy.getMaskLotService().maskMultiHold(holdEventInfo, trackOutMaskLot);
		}
		
		// Mantis : 0000440
		// TFE Mask在OLEDMaskCSTProcessEnd时，如果MaskUseCount超过MaskUseCountLimit，则Hold，若达到90% MaskUseCountLimit时则邮件通知TFE工程师 ----功能待实现
		if (StringUtil.equals(returnMaskLotData.getMaskKind(), "TFE"))
		{
			Float maskUseCount = returnMaskLotData.getTimeUsed();
			Float maskUseCountLimit = returnMaskLotData.getTimeUsedLimit();
			if (maskUseCountLimit < maskUseCount)
			{
				EventInfo holdEventInfo = EventInfoUtil.makeEventInfo("HoldMask", originalEventInfo.getEventUser(), originalEventInfo.getEventComment(), "SYSTEM", "MaskUseCountHold");
				returnMaskLotData = ExtendedObjectProxy.getMaskLotService().maskMultiHold(holdEventInfo, trackOutMaskLot);
			}
			
			Float maskUseCountRate = (maskUseCount / maskUseCountLimit) * 100;
			if (maskUseCountRate >= 90)
			{
				String message = "<pre>===============AlarmInformation===============</pre>"
	  	  	  	  			   + "<pre>==============================================</pre>"
	  	  	  	  			   + "<pre>- EventName			: " + eventInfo.getEventName() + "</pre>"
	  	  	  	  			   + "<pre>- MaskLotName		: " + returnMaskLotData.getMaskLotName() + "</pre>"
	  	  	  	  			   + "<pre>- MaskUseCount		: " + maskUseCount + "</pre>"
	  	  	  	  			   + "<pre>- MaskUseCountLimit	: " + maskUseCountLimit + "</pre>"
	  	  	  	  			   + "<pre>- MaskUseCountRate	: " + maskUseCountRate + "</pre>"
	  	  	  	  			   + "<pre>==============================================</pre>";
	  	  	  	  
				CommonUtil.sendAlarmEmail("MaskTrackOut", "MaskUseCountAlarm", message);
			}
		}
		
		return returnMaskLotData;		
	}

	public void maskMakeLoggedIn(EventInfo eventInfo, String maskLotName, Machine machineData, String portName, String processOperationName, String machineRecipeName) 
			throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		// Get Mask Lot Data.
		MaskLot inputMaskLot = 
				ExtendedObjectProxy.getMaskLotService()
				.selectByKey(true, new Object[] { maskLotName });
		MaskLot outputMaskLot = 
				ExtendedObjectProxy.getMaskLotService()
				.selectByKey(true, new Object[] { maskLotName });
	
		ProcessOperationSpec processOperationData = CommonUtil.getProcessOperationSpec(outputMaskLot.getFactoryName(), outputMaskLot.getMaskProcessOperationName(), outputMaskLot.getMaskProcessOperationVersion());

		PortSpec portSpecData = new PortSpec();
				
		if(StringUtils.isNotEmpty(portName))
			portSpecData = CommonUtil.getPortSpecInfo(machineData.getKey().getMachineName(), portName);
		
		if (processOperationName == null || processOperationName.length() == 0)
			processOperationName = outputMaskLot.getMaskProcessOperationName();
		
		String factoryName = inputMaskLot.getFactoryName();
		String processFlowName = inputMaskLot.getMaskProcessFlowName();
		
		// MakeLoggedIn Info.
		outputMaskLot.setMaskLotProcessState(constantMap.MaskLotProcessState_Run);
		outputMaskLot.setMaskLotState(constantMap.Lot_Released);
		outputMaskLot.setMachineName(machineData.getKey().getMachineName());
		outputMaskLot.setMachineRecipeName(machineRecipeName);
		outputMaskLot.setPortName(portName);
		outputMaskLot.setPortType(portSpecData.getPortType());
		outputMaskLot.setAreaName(machineData.getAreaName());
		outputMaskLot.setMaskProcessOperationName(processOperationName);
		outputMaskLot.setJobDownFlag("");
		
		//set MaskGroupName
		if(StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_EVA))
		{
			MaskGroup maskGroupData = null;
			try
			{
				maskGroupData =  ExtendedObjectProxy.getMaskGroupService().selectByKey(false, new Object[]{maskLotName});
			}
			catch(Exception ex)
			{
				log.info("Mask Group not Exist!");
			}			
			if(maskGroupData!=null)
			{
				String maskGroupName = maskGroupData.getMaskGroupName();
				outputMaskLot.setMaskGroupName(maskGroupName);
			}		
		}
								
		if(StringUtils.equalsIgnoreCase(processOperationData.getDetailProcessOperationType(), "CLN"))
		{	
			outputMaskLot.setCleanStartTimekey(eventInfo.getEventTimeKey());
			outputMaskLot.setCleanEndTimekey(null);
		}
		else if(processOperationData.getDetailProcessOperationType().equals("MASKAOI")||
				processOperationData.getDetailProcessOperationType().equals("MASKREPAIR"))
		{
			outputMaskLot.setAoiCode(null);
			outputMaskLot.setRepairCode(null);
			outputMaskLot.setCleanStartTimekey(null);
			outputMaskLot.setCleanEndTimekey(null);
		}
		else
		{
			outputMaskLot.setCleanStartTimekey(null);
			outputMaskLot.setCleanEndTimekey(null);
		}
		
		// deassign CST
		if (!StringUtils.equals(portSpecData.getPortType(), constantMap.PORT_TYPE_PB))
		{
			outputMaskLot.setCarrierName("");
			outputMaskLot.setPosition("");
		}
		
		// Set EventInfo
		outputMaskLot.setLastEventUser(eventInfo.getEventUser());
		outputMaskLot.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		outputMaskLot.setLastEventName(eventInfo.getEventName());	
		outputMaskLot.setLastEventComment(eventInfo.getEventComment());	
		outputMaskLot.setLastEventTime(TimeStampUtil.getCurrentTimestamp());
		outputMaskLot.setLastLoggedInTime(eventInfo.getEventTime());
		outputMaskLot.setLastLoggedInUser(eventInfo.getEventUser());
		
		MaskLot result = ExtendedObjectProxy.getMaskLotService().modify(eventInfo, outputMaskLot);
	}		
	
	public void maskCancelTrackInWithOutCarrier(EventInfo eventInfo, String maskLotName, String position) 
			throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		// Get Mask Lot Data.
		MaskLot outputMaskLot = 
				ExtendedObjectProxy.getMaskLotService()
				.selectByKey(true, new Object[] { maskLotName });
		
		// Cancel Track In Info.
		outputMaskLot.setMaskLotProcessState(constantMap.MaskLotProcessState_Wait);
		outputMaskLot.setMaskLotState(constantMap.Lot_Released);
		outputMaskLot.setMachineName("");
		outputMaskLot.setPortName("");
		outputMaskLot.setAreaName("");		
		
		if(!CommonValidation.isNullOrEmpty(position) && CommonValidation.isNumeric(position))
		outputMaskLot.setPosition(position);
		
		// Set EventInfo
		outputMaskLot.setLastEventUser(eventInfo.getEventUser());
		outputMaskLot.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		outputMaskLot.setLastEventName(eventInfo.getEventName());	
		outputMaskLot.setLastEventComment(eventInfo.getEventComment());	
		outputMaskLot.setLastEventTime(TimeStampUtil.getCurrentTimestamp());
		
	    ExtendedObjectProxy.getMaskLotService().modify(eventInfo, outputMaskLot);				
	}
		
	public void maskCancelTrackIn(EventInfo eventInfo, String maskLotName, String carrierName, String position)
			throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		// Get Mask Lot Data.
		MaskLot inputMaskLot = 
				ExtendedObjectProxy.getMaskLotService()
				.selectByKey(true, new Object[] { maskLotName });
		MaskLot outputMaskLot = 
				ExtendedObjectProxy.getMaskLotService()
				.selectByKey(true, new Object[] { maskLotName });
		
		String factoryName = inputMaskLot.getFactoryName();
		String processFlowName = inputMaskLot.getMaskProcessFlowName();
		
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		
		// Cancel Track In Info.
		outputMaskLot.setMaskLotProcessState(constantMap.MaskLotProcessState_Wait);
		outputMaskLot.setMaskLotState(constantMap.Lot_Released);
		outputMaskLot.setMachineName("");
		outputMaskLot.setPortName("");
		outputMaskLot.setAreaName("");
		
		//Assign CST
		outputMaskLot.setCarrierName(carrierName);
		outputMaskLot.setPosition(position);
		
		// Set EventInfo
		outputMaskLot.setLastEventUser(eventInfo.getEventUser());
		outputMaskLot.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		outputMaskLot.setLastEventName(eventInfo.getEventName());	
		outputMaskLot.setLastEventComment(eventInfo.getEventComment());	
		outputMaskLot.setLastEventTime(TimeStampUtil.getCurrentTimestamp());
		
		MaskLot result = ExtendedObjectProxy.getMaskLotService().modify(eventInfo, outputMaskLot);
		
		int lotQty = ExtendedObjectProxy.getMaskLotService().assignedMaskLotQtyByCarrier(carrierName);
		
		// Assign Carrier
		if (durableData.getCapacity() < lotQty)
		{
			// error
			throw new CustomException("MASKINSPECTION-0002", carrierName);
		}
		durableData.setLotQuantity(lotQty);
		durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
		
		if (StringUtil.equals(outputMaskLot.getCleanState(), GenericServiceProxy.getConstantMap().Dur_Dirty))
			durableData.setDurableCleanState(GenericServiceProxy.getConstantMap().Dur_Dirty);
		
		Map<String, String> udfsDurable = new HashMap<String, String>();
		durableData.setUdfs(udfsDurable);
		DurableServiceProxy.getDurableService().update(durableData);
		
		SetEventInfo setEventInfo1 = new SetEventInfo();
		EventInfo eventInfo1 = EventInfoUtil.makeEventInfo("Assign", eventInfo.getEventUser(), eventInfo.getEventComment(), "", "");
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo1, eventInfo1);
	}	
	
	public static void CheckCommonTrackIn(MaskLot maskLotData) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		if (!maskLotData.getMaskLotState().equals(constantMap.MaskLotState_Released)) {
			throw new CustomException("MASK-0026", maskLotData.getMaskLotName(), maskLotData.getMaskLotProcessState());
		}

		if (!maskLotData.getMaskLotProcessState().equals(constantMap.MaskLotProcessState_Wait)) {
			throw new CustomException("MASK-0026", maskLotData.getMaskLotName(), maskLotData.getMaskLotProcessState());
		}
		
		if (maskLotData.getCleanState().equals(constantMap.Dur_Dirty)) {
			throw new CustomException("MASK-0025", maskLotData.getMaskLotName());
		}

		if (maskLotData.getMaskLotHoldState().equals(constantMap.MaskLotHoldState_OnHold)) {
			throw new CustomException("MASK-0013", maskLotData.getMaskLotName());
		}
		
		if (maskLotData.getTimeUsedLimit() <= maskLotData.getTimeUsed()) {
			throw new CustomException("MASK-0030", maskLotData.getMaskLotName());
		}
	}

	public MaskLot setJobFlag(EventInfo eventInfo, String maskLotName, String jobDownFlag) throws CustomException
	{
		// Get Mask Lot Data.
		MaskLot outputMaskLot = 
				ExtendedObjectProxy.getMaskLotService()
				.selectByKey(true, new Object[] { maskLotName });
		
		// Set EventInfo
		outputMaskLot.setLastEventUser(eventInfo.getEventUser());
		outputMaskLot.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		outputMaskLot.setLastEventName(eventInfo.getEventName());
		outputMaskLot.setLastEventComment(eventInfo.getEventComment());
		outputMaskLot.setLastEventTime(eventInfo.getEventTime());
		outputMaskLot.setReasonCode(eventInfo.getReasonCode());
		outputMaskLot.setReasonCodeType(eventInfo.getReasonCodeType());

		if(StringUtils.isNotEmpty(jobDownFlag))
			outputMaskLot.setJobDownFlag(jobDownFlag);
		
		MaskLot returnedMaskLot = 
				ExtendedObjectProxy.getMaskLotService()
				.modify(eventInfo, outputMaskLot);
		
		return returnedMaskLot;
	}
	
	public ProcessFlow getProcessFlowData(MaskLot maskData) throws CustomException 
	{
		ProcessFlowKey processFlowKey = new ProcessFlowKey();
		processFlowKey.setFactoryName(maskData.getFactoryName());
		processFlowKey.setProcessFlowName(maskData.getMaskProcessFlowName());
		processFlowKey.setProcessFlowVersion(maskData.getMaskProcessFlowVersion());
		ProcessFlow processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
		
		return processFlowData;
	}
	
	public boolean IsOperChangeCase(MaskLot maskLotData)
	{
		boolean returnFlag = true;
		
		if (!maskLotData.getCleanState().equals("Clean"))returnFlag = false;
		if (!maskLotData.getMaskLotHoldState().equals("N"))returnFlag = false;
		if (!maskLotData.getMaskLotState().equals("Released"))returnFlag = false;
		if (!maskLotData.getMaskLotProcessState().equals("WAIT"))returnFlag = false;
		if (!(maskLotData.getTimeUsed() < maskLotData.getTimeUsedLimit()))returnFlag = false;
		
		return returnFlag;
	}
	
	public void changeSpec(EventInfo eventInfo,MaskLot maskLotData,String toProcessFlowName, String toProcessFlowVersion,String toProcessOperationName,String toProcessOperationVersion) throws CustomException
	{
		String nodeId = getNodeId(maskLotData,toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);
		
		maskLotData.setMaskProcessFlowName(toProcessFlowName);
		maskLotData.setMaskProcessFlowVersion(toProcessFlowVersion);
		maskLotData.setMaskProcessOperationName(toProcessOperationName);
		maskLotData.setMaskProcessOperationVersion(toProcessOperationVersion);
		maskLotData.setNodeStack(nodeId);
		maskLotData.setLastEventName(eventInfo.getEventName());
		maskLotData.setLastEventTime(eventInfo.getEventTime());
		maskLotData.setLastEventTimeKey(eventInfo.getLastEventTimekey());
		maskLotData.setLastEventUser(eventInfo.getEventUser());
		maskLotData.setLastEventComment(eventInfo.getEventComment());

		ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotData);
	}
	
	private String getNodeId(MaskLot maskLotData,String toProcessFlowName, String toProcessFlowVersion,String toProcessOperationName,String toProcessOperationVersion) throws CustomException
	{
		String nodeId = NodeStack.getNodeID(maskLotData.getFactoryName(), toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);
		
		if(StringUtils.EMPTY.equals(nodeId))
			throw new CustomException("OLEDMASK-0014",String.format("FactoryName=%s,FlowName=%s,OperationName=%s", maskLotData.getFactoryName(), toProcessFlowName,toProcessOperationName));
		
		Node nodeData =  ProcessFlowServiceProxy.getNodeService().getNode(nodeId);
		
		if(nodeData.getNodeType().equals("Start"))
		{
			nodeId = getFirstOperationNode(maskLotData.getFactoryName(),toProcessFlowName, toProcessFlowVersion);
		}
		
		if(StringUtils.EMPTY.equals(nodeId))
			throw new CustomException("OLEDMASK-0014",String.format("FactoryName=%s,FlowName=%s,FlowVersion=%s", maskLotData.getFactoryName(), toProcessFlowName,toProcessFlowVersion));
		
	   return nodeId;
	}
	private String getFirstOperationNode(String factoryName,String processFlowName, String processFlowVersion)
	{
		if (StringUtil.in(StringUtils.EMPTY, factoryName,processFlowName, processFlowVersion))
		{
			log.info("getFirstOperationNode: The incoming argument value is Empty or Null!!.");
			return "";
		}

		String sql =" SELECT A.TONODEID FROM NODE N, ARC A  "
				  + " WHERE A.ARCTYPE = 'Normal'"
				  + " AND A.PROCESSFLOWNAME = N.PROCESSFLOWNAME "
				  + " AND A.PROCESSFLOWVERSION = N.PROCESSFLOWVERSION "
				  + " AND A.FACTORYNAME = N.FACTORYNAME "
				  + " AND A.FROMNODEID = N.NODEID"
				  + " AND N.FACTORYNAME = :FACTORYNAME"
				  + " AND N.PROCESSFLOWNAME = :PROCESSFLOWNAME"
				  + " AND N.PROCESSFLOWVERSION = :PROCESSFLOWVERSION"
				  + " AND N.NODETYPE = 'Start'";
		
		Map<String,Object> bindMap = new HashMap<>();
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PROCESSFLOWNAME",processFlowName);
		bindMap.put("PROCESSFLOWVERSION",processFlowVersion);
		
		List<Map<String,Object>> resultList = null;
		
		try
		{
			resultList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, bindMap);
		}
		catch (Exception ex)
		{
			log.info(ex.getCause());
			return "";
		}
		
		if(resultList ==null || resultList.size()==0) return "";
		return ConvertUtil.getMapValueByName(resultList.get(0), "TONODEID");
	}
	
	public MaskLot executePostAction(EventInfo eventInfo, MaskLot maskData) throws CustomException
	{
		try
		{
			log.info("Execute PostAction");
			
			List<MaskFutureAction> futureActionList = ExtendedObjectProxy.getMaskFutureActionService().getMaskFutureActionList(maskData.getMaskLotName(), maskData.getFactoryName(),
					maskData.getMaskProcessFlowName(), maskData.getMaskProcessFlowVersion(), maskData.getMaskProcessOperationName(), maskData.getMaskProcessOperationVersion(), 0);

			for (MaskFutureAction futureAction : futureActionList)
			{
				if (StringUtils.equalsIgnoreCase(futureAction.getActionName(), "hold"))
				{
					String eventUser = futureAction.getLastEventUser();
					String eventComment = futureAction.getLastEventComment();
					String reasonCodeType = futureAction.getReasonCodeType();
					String reasonCode = futureAction.getReasonCode();

					log.info("HoldMask in ExecutePostAction case 2");
					eventInfo = EventInfoUtil.makeEventInfo("HoldMask", eventUser, eventComment, reasonCodeType, reasonCode);
					eventInfo.setLastEventTimekey(TimeUtils.getCurrentEventTimeKey());
					maskData = holdMask(eventInfo, maskData);

					try
					{
						MaskMultiHold maskMultiHold = ExtendedObjectProxy.getMaskMultiHoldService().selectByKey(false,
								new Object[] { maskData.getMaskLotName(), maskData.getFactoryName(), reasonCode });

						if (maskMultiHold != null) // If multi hold is duplicated.
						{
							throw new CustomException("OLEDMASK-0007", "Mask: " + maskData.getMaskLotName() + ", Factory: " + maskData.getFactoryName() + ", Operation: "
									+ maskData.getMaskProcessOperationName() + ", Reason Code Type: " + reasonCodeType + ", Reason Code: " + reasonCode);
						}
					}
					catch (greenFrameDBErrorSignal nfdes) // If multi hold is not duplicated, insert new multi hold and remove the future action.
					{
						MaskMultiHold multiHold = new MaskMultiHold(maskData.getMaskLotName(), maskData.getFactoryName(), maskData.getMaskProcessOperationName(),
								maskData.getMaskProcessOperationVersion(), reasonCodeType, reasonCode, eventInfo.getEventTime(), eventInfo.getEventName(), eventInfo.getEventUser(), eventInfo.getEventComment());

						ExtendedObjectProxy.getMaskMultiHoldService().insert(multiHold);
						eventInfo.setEventName("Delete");
						ExtendedObjectProxy.getMaskFutureActionService().remove(eventInfo, futureAction);
					}
				}
			}						
		}
		catch (greenFrameDBErrorSignal nfdes)
		{
		}
		
		log.info("Check SPC Reserve Hold Flag");
		if(StringUtils.isNotEmpty(maskData.getSpcReserveHoldFlag()))
		{
			log.info("SPCReserveHoldFlag is not null, then HoldMask");
			
			String eventUser = "SPC";
			String eventComment = "SPCHOLD";
			String reasonCodeType = "HOLD";
			String reasonCode = maskData.getSpcReserveHoldFlag();
			eventInfo = EventInfoUtil.makeEventInfo("HoldMask", eventUser, eventComment, reasonCodeType, reasonCode);
			eventInfo.setLastEventTimekey(TimeUtils.getCurrentEventTimeKey());
			//HoldMask
			try
			{					
				maskData.setMaskLotHoldState("Y");
				maskData.setSpcReserveHoldFlag("");
				maskData.setReasonCode(eventInfo.getReasonCode());
				maskData.setReasonCodeType(eventInfo.getReasonCodeType());
				maskData.setLastEventComment(eventInfo.getEventComment());
				maskData.setLastEventName(eventInfo.getEventName());
				maskData.setLastEventTime(eventInfo.getEventTime());
				maskData.setLastEventTimeKey(eventInfo.getLastEventTimekey());
				maskData.setLastEventUser(eventInfo.getEventUser());

				ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskData);
			}
			catch(Exception e)
			{
				log.info("MaskLot Update Error");
			}
			//Inser into multi hold
			try
			{
				MaskMultiHold maskMultiHold = ExtendedObjectProxy.getMaskMultiHoldService().selectByKey(false,
						new Object[] { maskData.getMaskLotName(), maskData.getFactoryName(), reasonCode });
			}
			catch (greenFrameDBErrorSignal nfdes) // If multi hold is not duplicated, insert new multi hold.
			{
				MaskMultiHold multiHold = new MaskMultiHold(maskData.getMaskLotName(), maskData.getFactoryName(), maskData.getMaskProcessOperationName(),
						maskData.getMaskProcessOperationVersion(), reasonCodeType, reasonCode, eventInfo.getEventTime(), eventInfo.getEventName(), eventInfo.getEventUser(), eventInfo.getEventComment());

				ExtendedObjectProxy.getMaskMultiHoldService().insert(multiHold);
			}
		}
		
		if (!"Y".equals(maskData.getMaskLotHoldState()))
		{
			ProcessFlow processFlow = getProcessFlowData(maskData);
			String detailProcessFlowType = processFlow.getUdfs().get("DETAILPROCESSFLOWTYPE");
			if (StringUtils.equalsIgnoreCase(detailProcessFlowType, "Eva") || StringUtils.equalsIgnoreCase(detailProcessFlowType, "Tension"))
			{
				// get next node
				Node nextNode = ExtendedObjectProxy.getExtendedProcessFlowUtil().getNextNode(maskData);
				String nextOperationName = nextNode.getNodeAttribute1();

				if (StringUtils.isEmpty(nextOperationName) && StringUtils.equals(nextNode.getNodeType(), GenericServiceProxy.getConstantMap().Node_End))
				{
					ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(maskData.getFactoryName(), maskData.getMaskProcessOperationName(),maskData.getMaskProcessOperationVersion());

					if (StringUtils.equalsIgnoreCase(operationData.getDetailProcessOperationType(), "MASKSTOCKER") && IsOperChangeCase(maskData))
					{
						String sql = " SELECT P.TOPROCESSFLOWNAME , P.TOPROCESSFLOWVERSION , P.TOPROCESSOPERATIONNAME, "
								+ "       P.TOPROCESSOPERATIONVERSION ,P.MAXRECYCLECOUNT,P.FLOWCHANGEFLAG ,P.RECYCLEFLAG" 
								+ " FROM TRPOLICY T , POSMASKFLOWRELATION P " 
								+ " WHERE 1=1 "
								+ " AND T.CONDITIONID = P.CONDITIONID  " 
								+ " AND T.FACTORYNAME = :FACTORYNAME " 
								+ " AND T.MASKSPECNAME = :MASKSPECNAME " 
								+ " AND P.PROCESSFLOWNAME = :PROCESSFLOWNAME "
								+ " AND P.PROCESSFLOWVERSION = :PROCESSFLOWVERSION " 
								+ " AND (P.FLOWCHANGEFLAG = 'Y' OR RECYCLEFLAG ='Y') ";

						Map<String, Object> bindMap = new HashMap<>();
						bindMap.put("FACTORYNAME", maskData.getFactoryName());
						bindMap.put("MASKSPECNAME", maskData.getMaskSpecName());
						bindMap.put("PROCESSFLOWNAME", maskData.getMaskProcessFlowName());
						bindMap.put("PROCESSFLOWVERSION", maskData.getMaskProcessFlowVersion());

						List<Map<String, Object>> resultList = null;

						try
						{
							resultList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, bindMap);
						}
						catch (Exception ex)
						{
							if (ex instanceof greenFrameDBErrorSignal)
								throw new CustomException(ex.getCause());
						}

						if (resultList == null || resultList.size() == 0)
						{
							log.info("Mask auto change operation information is not registered or Flag is not On. ");
						}
						else
						{
							String toProcessFlowName = ConvertUtil.getMapValueByName(resultList.get(0), "TOPROCESSFLOWNAME");
							String toProcessFlowVersion = ConvertUtil.getMapValueByName(resultList.get(0), "TOPROCESSFLOWVERSION");
							String toProcessOperationName = ConvertUtil.getMapValueByName(resultList.get(0), "TOPROCESSOPERATIONNAME");
							String toProcessOperationVersion = ConvertUtil.getMapValueByName(resultList.get(0), "TOPROCESSOPERATIONVERSION");
							String flowChangeFlag = ConvertUtil.getMapValueByName(resultList.get(0), "FLOWCHANGEFLAG");
							String recycleFlag = ConvertUtil.getMapValueByName(resultList.get(0), "RECYCLEFLAG");
							String maxRecycleCount = ConvertUtil.getMapValueByName(resultList.get(0), "MAXRECYCLECOUNT");

							if (StringUtil.in(StringUtils.EMPTY, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion))
								throw new CustomException("OLEDMASK-0015", "POSMASKFLOWRELATION", toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);

							ProcessFlow toProcessFlow = getProcessFlowData(maskData);

							if ("Y".equals(flowChangeFlag))
							{
								// auto change flow
								if (!detailProcessFlowType.equals("Tension") || !toProcessFlow.getUdfs().get("DETAILPROCESSFLOWTYPE").equals("Eva"))
									throw new CustomException("OLEDMASK-0013", "POSMASKFLOWRELATION", "ChangeFlow", maskData.getMaskProcessFlowName(), toProcessFlow, detailProcessFlowType,toProcessFlow.getUdfs().get("DETAILPROCESSFLOWTYPE"));

								eventInfo.setEventName("AutoChangeFlow");
								changeSpec(eventInfo, maskData, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);
							}
							else
							{
								// Recyle at EVA Flow
								if (!detailProcessFlowType.equals("Eva") || !toProcessFlow.getUdfs().get("DETAILPROCESSFLOWTYPE").equals("Eva"))
									throw new CustomException("OLEDMASK-0013", "POSMASKFLOWRELATION", "ChangeOperation", maskData.getMaskProcessFlowName(), toProcessFlow, detailProcessFlowType,toProcessFlow.getUdfs().get("DETAILPROCESSFLOWTYPE"));

								if ((maskData.getMaskRepairCount() == null ? 0 : maskData.getMaskRepairCount().intValue()) > Integer.parseInt(maxRecycleCount))
								{
									log.info("Mask RepairCount has exceeded the MaxRecycleCount.");
								}
								else
								{
									eventInfo.setEventName("AutoChangeOperation");
									changeSpec(eventInfo, maskData, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);
								}
							}
						}
					}
				}
			}
		}else 
		{
			log.info(String.format("Mask [%s] state is hold. so it does not do AutoSpecChange." ,maskData.getMaskLotName()));
		}
		
		return ExtendedObjectProxy.getMaskLotService().getMaskLotData(false, maskData.getMaskLotName());
	}
	
	public MaskLot holdMask(EventInfo eventInfo, MaskLot maskData) throws CustomException
	{
		String reasonCodeType = eventInfo.getReasonCodeType();
		String reasonCode = eventInfo.getReasonCode();
		maskData.setMaskLotHoldState(constantMap.MaskLotHoldState_OnHold);
		maskData.setReasonCodeType(reasonCodeType);
		maskData.setReasonCode(reasonCode);
		maskData.setLastEventComment(eventInfo.getEventComment());
		maskData.setLastEventName(eventInfo.getEventName());
		maskData.setLastEventTime(eventInfo.getEventTime());
		maskData.setLastEventTimeKey(eventInfo.getLastEventTimekey());
		maskData.setLastEventUser(eventInfo.getEventUser());
		
		maskData = ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskData);
		
		return maskData;
	}
	
	public MaskLot maskMultiHold(EventInfo eventInfo, String maskLotName) throws CustomException
	{
		MaskLot maskData = ExtendedObjectProxy.getMaskLotService().getMaskLotData(false, maskLotName);
		
		StringBuffer inquirysql = new StringBuffer();
		inquirysql.append(" SELECT MASKLOTNAME ");
		inquirysql.append(" FROM CT_MASKMULTIHOLD ");
		inquirysql.append(" WHERE MASKLOTNAME=:MASKLOTNAME ");
		inquirysql.append(" AND REASONCODE=:REASONCODE ");
		
		Map<String, String> inquirybindMap = new HashMap<String, String>();
		inquirybindMap.put("MASKLOTNAME", maskData.getMaskLotName());
		inquirybindMap.put("REASONCODE", eventInfo.getReasonCode());
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(inquirysql.toString(), inquirybindMap);
		if(sqlResult!=null&& sqlResult.size()>0)
		{
			throw new CustomException("MASK-0082", maskData.getMaskLotName(),eventInfo.getReasonCode());
		}
		
		MaskMultiHold maskMultiHold =new MaskMultiHold();
		maskMultiHold.setMaskLotName( maskData.getMaskLotName());
		maskMultiHold.setFactoryName(maskData.getFactoryName());
		maskMultiHold.setMaskProcessOperationName(maskData.getMaskProcessOperationName());
		maskMultiHold.setMaskProcessOperationVersion(maskData.getMaskProcessOperationVersion());
		maskMultiHold.setReasonCode(eventInfo.getReasonCode());
		maskMultiHold.setReasonCodeType(eventInfo.getReasonCodeType());
		maskMultiHold.setLastEventComment(eventInfo.getEventComment());
		maskMultiHold.setLastEventName(eventInfo.getEventName());
		maskMultiHold.setLastEventTime(eventInfo.getEventTime());
		maskMultiHold.setLastEventUser(eventInfo.getEventUser());
		
		ExtendedObjectProxy.getMaskMultiHoldService().create(eventInfo, maskMultiHold);
		
		maskData.setMaskLotHoldState(constantMap.MaskLotHoldState_OnHold);
		maskData.setReasonCodeType(eventInfo.getReasonCodeType());
		maskData.setReasonCode(eventInfo.getReasonCode());
		maskData.setLastEventComment(eventInfo.getEventComment());
		maskData.setLastEventName(eventInfo.getEventName());
		maskData.setLastEventTime(eventInfo.getEventTime());
		maskData.setLastEventTimeKey(eventInfo.getLastEventTimekey());
		maskData.setLastEventUser(eventInfo.getEventUser());
		
		maskData = ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskData);
		
		return maskData;
	}
	
	public MaskLot maskMultiHold(EventInfo eventInfo, MaskLot maskData) throws CustomException
	{
		StringBuffer inquirysql = new StringBuffer();
		inquirysql.append(" SELECT MASKLOTNAME ");
		inquirysql.append(" FROM CT_MASKMULTIHOLD ");
		inquirysql.append(" WHERE MASKLOTNAME=:MASKLOTNAME ");
		inquirysql.append(" AND REASONCODE=:REASONCODE ");
		
		Map<String, String> inquirybindMap = new HashMap<String, String>();
		inquirybindMap.put("MASKLOTNAME", maskData.getMaskLotName());
		inquirybindMap.put("REASONCODE", eventInfo.getReasonCode());
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(inquirysql.toString(), inquirybindMap);
		if(sqlResult!=null&& sqlResult.size()>0)
		{
			throw new CustomException("MASK-0082", maskData.getMaskLotName(),eventInfo.getReasonCode());
		}
		
		MaskMultiHold maskMultiHold =new MaskMultiHold();
		maskMultiHold.setMaskLotName( maskData.getMaskLotName());
		maskMultiHold.setFactoryName(maskData.getFactoryName());
		maskMultiHold.setMaskProcessOperationName(maskData.getMaskProcessOperationName());
		maskMultiHold.setMaskProcessOperationVersion(maskData.getMaskProcessOperationVersion());
		maskMultiHold.setReasonCode(eventInfo.getReasonCode());
		maskMultiHold.setReasonCodeType(eventInfo.getReasonCodeType());
		maskMultiHold.setLastEventComment(eventInfo.getEventComment());
		maskMultiHold.setLastEventName(eventInfo.getEventName());
		maskMultiHold.setLastEventTime(eventInfo.getEventTime());
		maskMultiHold.setLastEventUser(eventInfo.getEventUser());
		
		ExtendedObjectProxy.getMaskMultiHoldService().create(eventInfo, maskMultiHold);
		
		maskData.setMaskLotHoldState(constantMap.MaskLotHoldState_OnHold);
		maskData.setReasonCodeType(eventInfo.getReasonCodeType());
		maskData.setReasonCode(eventInfo.getReasonCode());
		maskData.setLastEventComment(eventInfo.getEventComment());
		maskData.setLastEventName(eventInfo.getEventName());
		maskData.setLastEventTime(eventInfo.getEventTime());
		maskData.setLastEventTimeKey(eventInfo.getLastEventTimekey());
		maskData.setLastEventUser(eventInfo.getEventUser());
		
		maskData = ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskData);
		
		return maskData;
	}
	
	public String getSkipFlagForSampling(MaskLot maskLotData) throws CustomException
	{
		String skipFlag = "N";
		SampleMaskCountService sampleCountSvc = ExtendedObjectProxy.getSampleMaskCountService();

		String flowName = maskLotData.getMaskProcessFlowName();
		String flowVer = maskLotData.getMaskProcessFlowVersion();
		String operName = maskLotData.getMaskProcessOperationName();
		String operVer = maskLotData.getMaskProcessOperationVersion();
		String machineName = maskLotData.getMachineName();
		Map<String, Object> samplePolicy = getMaskSamplePolicyByMask(maskLotData);
		
		if(samplePolicy == null) return "N";
		
		//Get SamplingMaskCount(CT_SAMPLEMASKCOUNT)
		Object[] keySet = new Object[] { flowName, flowVer, operName, operVer, machineName };
		SampleMaskCount sampleCountData = null;
		
		try
		{
			sampleCountData = sampleCountSvc.selectByKey(true, keySet);
		}
		catch(greenFrameDBErrorSignal nfdes)
		{
			sampleCountData = new SampleMaskCount(flowName, flowVer, operName, operVer, machineName);
			sampleCountData.setCurrentCarrierCount(0);
			sampleCountData.setTotalCarrierCount(0);
			sampleCountSvc.create(sampleCountData);
		}
		
		sampleCountData.setToProcessFlowName((String)samplePolicy.get("TOPROCESSFLOWNAME"));
		sampleCountData.setToProcessFlowVersion((String)samplePolicy.get("TOPROCESSFLOWVERSION"));
		sampleCountData.setToProcessOperationName((String)samplePolicy.get("TOPROCESSOPERATIONNAME"));
		sampleCountData.setToProcessOperationVersion((String)samplePolicy.get("TOPROCESSOPERATIONVERSION"));
		sampleCountData.setReturnOperationName((String)samplePolicy.get("RETURNOPERATIONNAME"));
		sampleCountData.setReturnOperationVersion((String)samplePolicy.get("RETURNOPERATIONVERSION"));
		sampleCountData.setPerCarrier(Integer.parseInt(samplePolicy.get("PERCARRIER").toString()));
		
		int perCarrier = sampleCountData.getPerCarrier();
		int currentCarrierCount = sampleCountData.getCurrentCarrierCount();
		int totalCarrierCount = sampleCountData.getTotalCarrierCount();
		
		if(currentCarrierCount % perCarrier == 0)
		{
			currentCarrierCount = 1;
			skipFlag = "N";
		}
		else
		{
			currentCarrierCount++;
			skipFlag = "Y";
		}
		totalCarrierCount++;
		
		sampleCountData.setCurrentCarrierCount(currentCarrierCount);
		sampleCountData.setTotalCarrierCount(totalCarrierCount);
		sampleCountSvc.modify(sampleCountData);
		
		return skipFlag;
	}
	
	public Map<String, Object> getMaskSamplePolicyByMask(MaskLot maskLotData) throws CustomException
	{
		String flowName = maskLotData.getMaskProcessFlowName();
		String flowVer = maskLotData.getMaskProcessFlowVersion();
		String operName = maskLotData.getMaskProcessOperationName();
		String operVer = maskLotData.getMaskProcessOperationVersion();
		String machineName = maskLotData.getMachineName();
		
		// Get mask sampling flow (joined TFOMPOLICY & POSMASKSAMPLE)
		// It would(should) be just one policy as by DB structure.
		List<Map<String, Object>> maskSamplePolicyList = getMaskSamplePolicyList(flowName, flowVer, operName, operVer, machineName, "", "", "", "");

		if(maskSamplePolicyList.size() == 0)
			return null;
		else if(maskSamplePolicyList.size() > 1)
			throw new CustomException("MASK-0063");
		
		Map<String, Object> samplePolicy = maskSamplePolicyList.get(0);
		return samplePolicy;
	}
	public MaskLot checkMaskLot(MaskLot maskLotData) throws CustomException//CAIXU 20200317
	{
		if (StringUtils.equals(maskLotData.getMaskLotHoldState(), GenericServiceProxy.getConstantMap().MaskLotHoldState_OnHold))
			throw new CustomException("MASK-0013", maskLotData.getMaskLotName());
		
		//MaskLotState Check
		if (!StringUtils.equals(maskLotData.getMaskLotState(), GenericServiceProxy.getConstantMap().MaskLotState_Released))
			throw new CustomException("MASK-0026", maskLotData.getMaskLotName());

		if(!StringUtils.equals(maskLotData.getMaskLotProcessState(), "WAIT"))
			throw new CustomException("MASK-0014");
		return maskLotData;
	}
	private List<Map<String, Object>> getMaskSamplePolicyList(String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String machineName, String toProcessFlowName, String toProcessFlowVersion, String toProcessOperationName, String toProcessOperationVersion)
	{
		Map<String, Object> bindMap = new HashMap<String, Object>();

		StringBuilder sqlAddProcessOperationName = new StringBuilder();
		String sqlAddToMachineName = "";
		String sqlAddToProcessFlowName = "";
		String sqlAddToProcessFlowVersion = "";
		String sqlAddToProcessOperationName = "";
		String sqlAddToProcessOperationVersion = "";
		
		if (StringUtils.isNotEmpty(processOperationName))
		{
			bindMap.put("PROCESSOPERATIONNAME", processOperationName);
			sqlAddProcessOperationName.append("    AND T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		}
		if (StringUtils.isNotEmpty(processOperationVersion))
		{
			bindMap.put("PROCESSOPERATIONVERSION", processOperationVersion);
			sqlAddProcessOperationName.append("    AND T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		}
		if (StringUtils.isNotEmpty(machineName))
		{
			bindMap.put("MACHINENAME", machineName);
			sqlAddToMachineName = "    AND T.MACHINENAME IN ('NA', :MACHINENAME) ";
		}
		if (StringUtils.isNotEmpty(toProcessFlowName))
		{
			bindMap.put("TOPROCESSFLOWNAME", toProcessFlowName);
			sqlAddToProcessFlowName = "    AND P.TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME ";
		}
		if (StringUtils.isNotEmpty(toProcessFlowVersion))
		{
			bindMap.put("TOPROCESSFLOWVERSION", toProcessFlowVersion);
			sqlAddToProcessFlowVersion = "    AND P.TOPROCESSFLOWVERSION = :TOPROCESSFLOWVERSION ";
		}
		if (StringUtils.isNotEmpty(toProcessOperationName))
		{
			bindMap.put("TOPROCESSOPERATIONNAME", toProcessOperationName);
			sqlAddToProcessOperationName = "    AND P.TOPROCESSOPERATIONNAME = :TOPROCESSOPERATIONNAME ";
		}
		if (StringUtils.isNotEmpty(toProcessOperationVersion))
		{
			bindMap.put("TOPROCESSOPERATIONVERSION", toProcessOperationVersion);
			sqlAddToProcessOperationVersion = "    AND P.TOPROCESSOPERATIONVERSION = :TOPROCESSOPERATIONVERSION ";
		}
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT PROCESSFLOWNAME, ");
		sql.append("       PROCESSOPERATIONNAME, ");
		sql.append("       MACHINENAME, ");
		sql.append("       TOPROCESSFLOWNAME, ");
		sql.append("       TOPROCESSFLOWVERSION, ");
		sql.append("       TOPROCESSOPERATIONNAME, ");
		sql.append("       TOPROCESSOPERATIONVERSION, ");
		sql.append("       FLOWPRIORITY, ");
		sql.append("       PERCARRIER, ");
		sql.append("       MASKCOUNT, ");
		sql.append("       MASKPOSITION, ");
		sql.append("       RETURNOPERATIONNAME, ");
		sql.append("       RETURNOPERATIONVERSION ");
		sql.append("  FROM TFOMPOLICY T, POSMASKSAMPLE P ");
		sql.append(" WHERE T.CONDITIONID = P.CONDITIONID ");
		sql.append("   AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append(sqlAddProcessOperationName);
		sql.append(sqlAddToMachineName);
		sql.append(sqlAddToProcessFlowName);
		sql.append(sqlAddToProcessFlowVersion);
		sql.append(sqlAddToProcessOperationName);
		sql.append(sqlAddToProcessOperationVersion);
		sql.append(" ORDER BY FLOWPRIORITY ASC ");

		bindMap.put("PROCESSFLOWNAME", processFlowName);
		bindMap.put("PROCESSFLOWVERSION", processFlowVersion);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		return sqlResult;
	}

	public List<Map<String, Object>> getMaskRecipe(String factoryName, String maskSpecName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String machineName)
	{		
		String sql = " SELECT PM.MACHINERECIPENAME, PM.CHECKLEVEL " +
				" FROM TRFOPOLICY TR, POSMACHINE PM " +
				" WHERE TR.CONDITIONID = PM.CONDITIONID " +
				"    AND TR.FACTORYNAME = :FACTORYNAME " +
				"    AND TR.MASKSPECNAME = :MASKSPECNAME " +
				"    AND TR.PROCESSFLOWNAME = :PROCESSFLOWNAME " +
				"    AND TR.PROCESSFLOWVERSION = :PROCESSFLOWVERSION " +
				"    AND TR.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME " +
				"    AND TR.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION " +
				"    AND PM.MACHINENAME = :MACHINENAME " ;
		
		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", factoryName);
		args.put("MASKSPECNAME", maskSpecName);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);
		args.put("PROCESSOPERATIONNAME", processOperationName);
		args.put("PROCESSOPERATIONVERSION", processOperationVersion);
		args.put("MACHINENAME", machineName);
		
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);
		
		return result;
	}
	
	public List<Map<String, Object>> getMaskRecipeV2(String factoryName, String maskSpecName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String machineName)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT PM.MACHINERECIPENAME, PM.RMSFLAG, PM.ECRECIPEFLAG, PM.ECRECIPENAME, PM.MASKCYCLETARGET ");
		sql.append("  FROM TRFOPOLICY TR, POSMACHINE PM ");
		sql.append(" WHERE TR.CONDITIONID = PM.CONDITIONID ");
		sql.append("   AND TR.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND TR.MASKSPECNAME = :MASKSPECNAME ");
		sql.append("   AND TR.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND TR.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND TR.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND TR.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		sql.append("   AND PM.MACHINENAME = :MACHINENAME ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", factoryName);
		args.put("MASKSPECNAME", maskSpecName);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);
		args.put("PROCESSOPERATIONNAME", processOperationName);
		args.put("PROCESSOPERATIONVERSION", processOperationVersion);
		args.put("MACHINENAME", machineName);

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		return result;
	}

	public List<Map<String, Object>> getMaskSubSpecRecipe(String factoryName, String maskSpecName, String maskSubSpecName, String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion, String machineName)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT PM.MACHINERECIPENAME, PM.RMSFLAG, PM.ECRECIPEFLAG, PM.ECRECIPENAME, PM.MASKCYCLETARGET ");
		sql.append("  FROM TRSFOPOLICY TR, POSMACHINE PM ");
		sql.append(" WHERE TR.CONDITIONID = PM.CONDITIONID ");
		sql.append("   AND TR.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND TR.MASKSPECNAME = :MASKSPECNAME ");
		sql.append("   AND TR.MASKSUBSPECNAME = :MASKSUBSPECNAME ");
		sql.append("   AND TR.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND TR.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND TR.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND TR.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		sql.append("   AND PM.MACHINENAME = :MACHINENAME ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", factoryName);
		args.put("MASKSPECNAME", maskSpecName);
		args.put("MASKSUBSPECNAME", maskSubSpecName);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);
		args.put("PROCESSOPERATIONNAME", processOperationName);
		args.put("PROCESSOPERATIONVERSION", processOperationVersion);
		args.put("MACHINENAME", machineName);

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		return result;
	}

	public List<Map<String, Object>> getTrfoPolicyAndMachine(String factoryName, String maskSpecName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String machineName)
	{		
		String sql = 
			" SELECT TR.FACTORYNAME, TR.MASKSPECNAME, TR.PROCESSFLOWNAME, " + 
				"TR.PROCESSFLOWVERSION, TR.PROCESSOPERATIONNAME, " +
				"TR.PROCESSOPERATIONVERSION, TR.CONDITIONID, " +
				"PM.MACHINENAME, PM.ROLLTYPE, PM.MACHINERECIPENAME, PM.INT, PM.MFG, " +
				"PM.AUTOCHANGEFLAG, PM.AUTOCHANGETIME, PM.AUTOCHANGELOTQUANTITY, " +
				"PM.CHECKLEVEL, PM.DISPATCHSTATE, PM.DISPATCHPRIORITY " +
			" FROM TRFOPOLICY TR, POSMACHINE PM " +
			" WHERE TR.CONDITIONID = PM.CONDITIONID " +
			"    AND TR.FACTORYNAME = :FACTORYNAME " +
			"    AND TR.MASKSPECNAME = :MASKSPECNAME " +
			"    AND TR.PROCESSFLOWNAME = :PROCESSFLOWNAME " +
			"    AND TR.PROCESSFLOWVERSION = :PROCESSFLOWVERSION " +
			"    AND TR.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME " +
			"    AND TR.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION " +
			"    AND PM.MACHINENAME = :MACHINENAME " ;
		
		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", factoryName);
		args.put("MASKSPECNAME", maskSpecName);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);
		args.put("PROCESSOPERATIONNAME", processOperationName);
		args.put("PROCESSOPERATIONVERSION", processOperationVersion);
		args.put("MACHINENAME", machineName);
		
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);
		
		return result;
	}

	public List<Map<String, Object>> getConnectedInformationBetweenPortAndMaskStock(String machineName, String portName, String maskSpecName)
	{
		String sql = 
			"SELECT L.MACHINENAME, C.UNITNAME, C.PORTTYPE, P.PORTNAME, L.LINETYPE, " + 
			"       C.LINETYPE MASKSTOCKTYPE, MASKSTOCK.MACHINENAME MASKSTOCKNAME, TP.PRODUCTSPECNAME, MS.MASKSPECNAME " +
			"  FROM CT_EVALINEPRODUCT L, TPPOLICY TP, POSMASKSPEC MS, PORT P, CT_CONNECTEDBUFFERPORT C, MACHINE MASKSTOCK " +
			" WHERE L.PRODUCTSPECNAME = TP.PRODUCTSPECNAME AND TP.PRODUCTSPECVERSION = '00001' " +
			"       AND TP.CONDITIONID = MS.CONDITIONID AND P.MACHINENAME = :MACHINENAME " +
			"       AND P.PORTNAME = :PORTNAME AND P.PORTKIND = 'MASK' AND P.PORTNAME = C.PORTNAME " +
			"       AND P.MACHINENAME = C.MACHINENAME AND MS.MASKSPECNAME = :MASKSPECNAME " +
			"       AND C.UNITNAME = MASKSTOCK.SUPERMACHINENAME AND MASKSTOCK.MACHINEGROUPNAME = 'MASKSTOCK' " +
			"       AND C.LINETYPE LIKE '%' || L.LINETYPE || '%' " +
			"       AND C.LINETYPE = " +
			"              (CASE " +
			"                  WHEN MASKSTOCK.MACHINENAME LIKE '%MS1' THEN 'A' " +
			"                  WHEN MASKSTOCK.MACHINENAME LIKE '%MS3' THEN 'A' " +
			"                  WHEN MASKSTOCK.MACHINENAME LIKE '%MS5' THEN 'A' " +
			"                  WHEN MASKSTOCK.MACHINENAME LIKE '%MS7' THEN 'A' " +
			"                  WHEN MASKSTOCK.MACHINENAME LIKE '%MS9' THEN 'A' " +
			"                  WHEN MASKSTOCK.MACHINENAME LIKE '%MS2' THEN 'B' " +
			"                  WHEN MASKSTOCK.MACHINENAME LIKE '%MS4' THEN 'B' " +
			"                  WHEN MASKSTOCK.MACHINENAME LIKE '%MS6' THEN 'B' " +
			"                  WHEN MASKSTOCK.MACHINENAME LIKE '%MS8' THEN 'B' " +
			"                  WHEN MASKSTOCK.MACHINENAME LIKE '%MSA' THEN 'B' " +
			"                  ELSE 'AB' " +
			"               END) " +
			"ORDER BY L.MACHINENAME, C.UNITNAME, P.PORTNAME, MASKSTOCKNAME";
			
		Map<String, String> args = new HashMap<String, String>();
		args.put("MACHINENAME", machineName);
		args.put("PORTNAME", portName);
		args.put("MASKSPECNAME", maskSpecName);
		
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);
		
		return result;
	}
	
	public MaskLot changeOperationMask(EventInfo eventInfo, MaskLot maskLotData, String sFactoryName, String newOperationName, String nodeStack) throws CustomException
	{
		//Shop validation
		if(!StringUtils.equals(sFactoryName, "OLED"))
			throw new CustomException("OLEDMASK-0002", maskLotData.getMaskLotName());
			
		//MaskLotHoldState Check
		if (StringUtils.equals(maskLotData.getMaskLotHoldState(), GenericServiceProxy.getConstantMap().MaskLotHoldState_OnHold))
			throw new CustomException("MASK-0013", maskLotData.getMaskLotName());
		
		//MaskLotState Check
		if (!StringUtils.equals(maskLotData.getMaskLotState(), GenericServiceProxy.getConstantMap().MaskLotState_Released))
			throw new CustomException("MASK-0026", maskLotData.getMaskLotName());

		if(!StringUtils.equals(maskLotData.getMaskLotProcessState(), "WAIT"))
			throw new CustomException("MASK-0014");
		if(StringUtils.equals(maskLotData.getJobDownFlag(), "Y"))//CHECK jOBDownloadFlag
			throw new CustomException("MASKJOBFlag-0001", maskLotData.getMaskLotName());
		
		//if current flow is Sampling Flow
		ProcessFlow processFlowData = ExtendedObjectProxy.getMaskLotService().getProcessFlowData(maskLotData);
		if(StringUtils.equals(processFlowData.getProcessFlowType(), "Sample"))
		{
			String factoryName = sFactoryName;
			String maskSpecName = maskLotData.getMaskSpecName();
			SampleMask sampleMask = ExtendedObjectProxy.getSampleMaskService()
					.getSampleMask(true, maskLotData.getMaskLotName(), sFactoryName, maskSpecName,
							maskLotData.getMaskProcessFlowName(),
							maskLotData.getMaskProcessFlowVersion(),
							maskLotData.getMaskProcessOperationName(),
							maskLotData.getMaskProcessOperationVersion());
			
			if (sampleMask == null)
			{
				throw new CustomException("OLEDMASK-0011", maskLotData.getMaskLotName(), maskLotData.getMaskProcessOperationName());
			}
			
			SampleMask nextSampleMask = ExtendedObjectProxy.getSampleMaskService()
					.getSampleMask(true, maskLotData.getMaskLotName(), factoryName, maskSpecName,
							maskLotData.getMaskProcessFlowName(),
							maskLotData.getMaskProcessFlowVersion(), newOperationName, "00001");
			
			if(nextSampleMask != null)
			{
				ExtendedObjectProxy.getSampleMaskService().remove(eventInfo, nextSampleMask);
			}
			
			SampleMask newSampleMask = new SampleMask(maskLotData.getMaskLotName(), factoryName,
					maskSpecName, maskLotData.getMaskProcessFlowName(),
					maskLotData.getMaskProcessFlowVersion(), newOperationName, "00001",
					sampleMask.getReturnProcessFlowName(),
					sampleMask.getReturnProcessFlowVersion(),
					sampleMask.getReturnOperationName(),
					sampleMask.getReturnOperationVersion(), eventInfo.getEventName(),
					eventInfo.getLastEventTimekey(), eventInfo.getEventTime(),
					eventInfo.getEventUser(), eventInfo.getEventComment());
			
			ExtendedObjectProxy.getSampleMaskService().remove(eventInfo, sampleMask);
			ExtendedObjectProxy.getSampleMaskService().create(eventInfo, newSampleMask);
		}
		
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		maskLotData.setMaskProcessOperationName(newOperationName);
		maskLotData.setNodeStack(nodeStack);
		maskLotData.setReasonCode("");
		maskLotData.setReasonCodeType("");
		maskLotData.setLastEventUser(eventInfo.getEventUser());
		maskLotData.setLastEventTimeKey(eventInfo.getLastEventTimekey());
		maskLotData.setLastEventName(eventInfo.getEventName());
		maskLotData.setLastEventComment(eventInfo.getEventComment());
		maskLotData.setLastEventTime(eventInfo.getEventTime());
	
		String sql = "SELECT NODEID FROM NODE " + " WHERE FACTORYNAME = ? "
				+ "   AND PROCESSFLOWNAME = ? "
				+ "   AND PROCESSFLOWVERSION = ? "
				+ "   AND NODEATTRIBUTE1 = ? " + "   AND NODEATTRIBUTE2 = ? "
				+ "   AND NODETYPE = 'ProcessOperation' ";

		Object[] bind = new Object[] { maskLotData.getFactoryName(),
				maskLotData.getMaskProcessFlowName(), maskLotData.getMaskProcessFlowVersion(), 
				newOperationName, maskLotData.getMaskProcessOperationVersion()};

		String[][] result = null;
		result = GenericServiceProxy.getSqlMesTemplate().queryForStringArray(sql, bind);

		if (result.length == 0) 
		{
			log.info("Not Found NodeID");
			throw new CustomException("MASK-0064", maskLotData.getMaskProcessFlowName(), newOperationName);
		} 
		else
		{
			String sToBeNodeStack = maskLotData.getNodeStack();
			String sNodeStack = maskLotData.getNodeStack();
		
			if (sNodeStack.contains("."))
			{
				String sCurNode = StringUtil.getLastValue(sNodeStack, ".");
				sNodeStack = sNodeStack.substring(0,sNodeStack.length() - sCurNode.length()- 1);
			
				sToBeNodeStack = sNodeStack + "." + result[0][0];
			}
			else
			{
				sToBeNodeStack = result[0][0];
			}
			maskLotData.setNodeStack(sToBeNodeStack);
		}

		maskLotData = ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotData);
		return ExtendedObjectProxy.getMaskLotService().executePostAction(eventInfo, maskLotData);	//FutureAction: Hold
	}
	
	public static Log getMaskLotLogger()
	{
		return logger;
	}

	public String getMaskLotHistoryEntity()
	{
		return historyEntity;
	}
	public MaskLot getMaskLotData(boolean isLock ,String maskLotName) throws CustomException
	{
		MaskLot dataInfo = getMaskLotData(maskLotName);

		if (isLock)
			dataInfo = this.selectByKey(isLock, new Object[] { maskLotName });

		return dataInfo;
	}
	
	public MaskLot getMaskLotData(String maskLotName) throws CustomException
	{
		if (StringUtils.isEmpty(maskLotName))
		{
			//SYSTEM-0010:{0}: The incoming variable value can not be empty!!
			throw new CustomException("SYSTEM-0010",Thread.currentThread().getStackTrace()[1].getMethodName());
		}

		MaskLot maskLot = null;
		try
		{
			maskLot = this.selectByKey(false, new Object[] { maskLotName });
		}
		catch (greenFrameDBErrorSignal dbEx)
		{
			if (dbEx.getErrorCode().equals(ErrorSignal.NotFoundSignal))
				throw new CustomException("COMM-1000", "CT_MASKLOT","MaskLotName = " +  maskLotName);
			else 
				throw new CustomException(dbEx.getCause());
		}
		catch(Exception ex)
		{
			throw new CustomException(ex.getCause());
		}
		return maskLot;
	}
}
