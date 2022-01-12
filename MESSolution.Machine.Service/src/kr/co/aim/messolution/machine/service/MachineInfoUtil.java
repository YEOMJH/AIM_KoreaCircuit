package kr.co.aim.messolution.machine.service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EnumInfoUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineKey;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;
import kr.co.aim.greentrack.machine.management.info.MakeCommunicationStateInfo;
import kr.co.aim.greentrack.machine.management.info.MakeE10StateInfo;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateInfo;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;
import kr.co.aim.greentrack.machine.management.info.UndoInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class MachineInfoUtil implements ApplicationContextAware {

	private ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog(MachineInfoUtil.class);
	
	public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		// TODO Auto-generated method stub
		applicationContext = arg0;
	}
	
	// TODO(PLY): add custom exception
	public static void validateSorterOperation(String operationMode) throws CustomException
	{
		log.info("validateSorterOperation: Sorter Operation mode is " + operationMode);

		//CUSTOM-0017:Ivalid Operation mode[{0}].
		if (operationMode == null || operationMode.isEmpty())
			throw new CustomException("CUSTOM-0017",operationMode);

		if (!EnumInfoUtil.SorterOperationCondition.validate(operationMode))
			throw new CustomException("CUSTOM-0017",operationMode);
	}

	public MakeMachineStateInfo makeMachineStateInfo(Machine machineData, String machineEventName)
	{
		// 1. Validation

		MakeMachineStateInfo makeMachineStateInfo = new MakeMachineStateInfo();

		makeMachineStateInfo.setMachineEventName(machineEventName);

		return makeMachineStateInfo;
	}

	public MakeCommunicationStateInfo makeCommunicationStateInfo(Machine machineData, String communicationState)
	{
		// 1. Validation

		MakeCommunicationStateInfo makeCommunicationState = new MakeCommunicationStateInfo();

		makeCommunicationState.setCommunicationState(communicationState);
		makeCommunicationState.setValidateEventFlag("Y");

		return makeCommunicationState;
	}

	public MakeE10StateInfo makeE10StateInfo(Machine machineData, String state)
	{
		// 1. Validation

		MakeE10StateInfo makeE10StateInfo = new MakeE10StateInfo();

		makeE10StateInfo.setE10State(state);
		makeE10StateInfo.setValidateEventFlag("Y");

		return makeE10StateInfo;
	}

	public MakeMachineStateByStateInfo makeMachineStateByStateInfo(Machine machineData, String machineStateName) throws CustomException
	{
		// 1. Validation
		CommonValidation.checkNotNull("MachineStateName", machineStateName);
		CommonValidation.checkStateModelEvent("MACHINE", machineData.getKey().getMachineName(), "", machineData.getMachineStateName(), machineStateName);

		MakeMachineStateByStateInfo makeMachineStateByStateInfo = new MakeMachineStateByStateInfo();

		makeMachineStateByStateInfo.setMachineStateName(machineStateName);
		makeMachineStateByStateInfo.setValidateEventFlag("Y");

		return makeMachineStateByStateInfo;
	}

	public SetEventInfo setEventInfo(Map<String, String> udfs)
	{
		SetEventInfo setEventInfo = new SetEventInfo();

		setEventInfo.setUdfs(udfs);

		return setEventInfo;
	}

	// undoInfo
	public UndoInfo undoInfo(Machine machineData, String eventName, Timestamp eventTime, String eventTimeKey, String eventUser, String lastEventTimeKey) throws InvalidStateTransitionSignal,
			FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		UndoInfo undoInfo = new UndoInfo();

		undoInfo.setEventName(eventName);
		undoInfo.setEventTime(eventTime);
		undoInfo.setEventTimeKey(eventTimeKey);
		undoInfo.setEventUser(eventUser);
		undoInfo.setEventTimeKey(lastEventTimeKey);

		return undoInfo;

	}

	public Machine getMachineData(String machineName) throws CustomException
	{
		MachineKey keyInfo = new MachineKey();
		keyInfo.setMachineName(machineName);

		Machine dataInfo;

		try
		{
			dataInfo = MachineServiceProxy.getMachineService().selectByKey(keyInfo);
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("MACHINE-9001", machineName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("MACHINE-9999", fe.getMessage());
		}

		return dataInfo;
	}

	public MachineSpec getMachineSpec(String machineName) throws CustomException
	{
		MachineSpecKey machineSpecKey = new MachineSpecKey();
		machineSpecKey.setMachineName(machineName);

		MachineSpec machineSpecData;
		try
		{
			machineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(machineSpecKey);
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("MACHINE-9001", machineName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("MACHINE-9999", fe.getMessage());
		}

		return machineSpecData;
	}
	
	public String[] getAlarmUserIdByUnitName(String unitName) throws CustomException
	{
		String[] userGroup = null;
		StringBuffer userList = new StringBuffer();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT DISTINCT B.USERID FROM ENUMDEFVALUE A ,CT_ALARMUSERGROUP B  ");
		sql.append(" WHERE A.ENUMNAME = 'EQPDepartment' AND A.DEFAULTFLAG = 'Y' ");
		sql.append(" AND A.ENUMVALUE = :ENUMVALUE AND A.DESCRIPTION = B.DEPARTMENT AND B.ALARMGROUPNAME = 'EQPDepartment' ");
		sql.append(" AND (A.DISPLAYCOLOR = B.RANGE OR B.RANGE = 'ALL')");
		sql.append(" AND B.USERID LIKE 'V00%' ");
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("ENUMVALUE", unitName);
		List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(),args);

		if(sqlResult != null && sqlResult.size()>0)
		{
			for (int i = 0; i < sqlResult.size(); i++) {
				userList = userList.append(sqlResult.get(i).get("USERID").toString()+";");
			}
			userGroup = userList.toString().split(";") ;
		}
		else
		{
			userGroup = null ;
		}
		
		return userGroup;
	}
}
