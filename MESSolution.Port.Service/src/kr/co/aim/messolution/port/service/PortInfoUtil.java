package kr.co.aim.messolution.port.service;

import java.sql.Timestamp;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;

import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.info.MakeCompletedInfo;
import kr.co.aim.greentrack.port.PortServiceProxy;

import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.data.PortKey;
import kr.co.aim.greentrack.port.management.data.PortSpec;
import kr.co.aim.greentrack.port.management.data.PortSpecKey;
import kr.co.aim.greentrack.port.management.info.MakeAccessModeInfo;
import kr.co.aim.greentrack.port.management.info.MakeE10StateInfo;
import kr.co.aim.greentrack.port.management.info.MakePortStateInfo;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;
import kr.co.aim.greentrack.port.management.info.MakePortStateByStateInfo;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;
import kr.co.aim.greentrack.port.management.info.UndoInfo;
import kr.co.aim.greentrack.portspec.management.info.ChangeSpecInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonValidation;

public class PortInfoUtil implements ApplicationContextAware {
	private ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog(PortInfoUtil.class);

	public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		applicationContext = arg0;
	}

	public MakeE10StateInfo makeE10StateInfo(Port portData, String e10MachineStateName)
	{
		MakeE10StateInfo makeE10StateInfo = new MakeE10StateInfo();
		makeE10StateInfo.setE10State(e10MachineStateName);
		makeE10StateInfo.setValidateEventFlag("N");

		return makeE10StateInfo;
	}

	public MakeAccessModeInfo makeAccessModeInfo(Port portData, String accessMode)
	{
		MakeAccessModeInfo makeAccessModeInfo = new MakeAccessModeInfo();
		makeAccessModeInfo.setAccessMode(accessMode);
		makeAccessModeInfo.setValidateEventFlag("N");

		return makeAccessModeInfo;
	}

	public MakeTransferStateInfo makeTransferStateInfo(Port portData, String transferState)
	{
		MakeTransferStateInfo makeTransferStateInfo = new MakeTransferStateInfo();
		makeTransferStateInfo.setTransferState(transferState);
		makeTransferStateInfo.setValidateEventFlag("N");

		return makeTransferStateInfo;
	}

	public MakePortStateInfo makePortStateInfo(Port portData, String portEventName)
	{
		MakePortStateInfo makePortStateInfo = new MakePortStateInfo();
		makePortStateInfo.setPortEventName(portEventName);

		return makePortStateInfo;
	}

	public ChangeSpecInfo changeSpecInfo(PortSpec portSpecData, String portType, String description, String factoryName, String areaName, String vendor, String model, String serialNo,
			String portStateModelName)
	{
		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();

		changeSpecInfo.setPortType(portType);
		changeSpecInfo.setDescription(description);
		changeSpecInfo.setFactoryName(factoryName);
		changeSpecInfo.setAreaName(areaName);
		changeSpecInfo.setVendor(vendor);
		changeSpecInfo.setModel(model);
		changeSpecInfo.setSerialNo(serialNo);
		changeSpecInfo.setPortStateModelName(portStateModelName);

		return changeSpecInfo;
	}

	public SetEventInfo setEventInfo(Map<String, String> portUdfs)
	{
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.setUdfs(portUdfs);

		return setEventInfo;
	}

	public UndoInfo undoInfo(Port portData, String eventName, Timestamp eventTime, String eventTimeKey, String eventUser, String lastEventTimeKey)
	{
		UndoInfo undoInfo = new UndoInfo();
		undoInfo.setEventName(eventName);
		undoInfo.setEventTime(eventTime);
		undoInfo.setEventTimeKey(eventTimeKey);
		undoInfo.setEventUser(eventUser);
		undoInfo.setLastEventTimeKey(lastEventTimeKey);

		return undoInfo;
	}

	public Port getPortData(String machineName, String portName) throws CustomException
	{
		try
		{
			PortKey portKey = new PortKey();
			portKey.setMachineName(machineName);
			portKey.setPortName(portName);

			Port portData = null;
			portData = PortServiceProxy.getPortService().selectByKey(portKey);

			return portData;
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("PORT-9001", machineName, portName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("PORT-9999", fe.getMessage());
		}
	}

	public MakePortStateByStateInfo makePortStateByStateInfo(Port portData, String portStateName) throws CustomException
	{
		MakePortStateByStateInfo makePortStateByStateInfo = new MakePortStateByStateInfo();
		makePortStateByStateInfo.setPortStateName(portStateName);
		makePortStateByStateInfo.setValidateEventFlag("Y");

		return makePortStateByStateInfo;
	}
}
