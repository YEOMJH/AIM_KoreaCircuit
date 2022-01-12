package kr.co.aim.messolution.port.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EnumInfoUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.data.PortKey;
import kr.co.aim.greentrack.port.management.data.PortSpec;
import kr.co.aim.greentrack.port.management.data.PortSpecKey;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;

public class PortServiceUtil implements ApplicationContextAware {

	private ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog(PortServiceImpl.class);

	public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		// TODO Auto-generated method stub
		applicationContext = arg0;
	}
	
	public boolean AllPortIsEmpty(String machineName) throws CustomException
	{
		List<Port> portDataList = null;
		try
		{
			portDataList = PortServiceProxy.getPortService().select("WHERE 1=1 AND MACHINENAME = ? AND FULLSTATE = 'FULL' ", new Object[] { machineName });
		}
		catch (NotFoundSignal notFoundError)
		{
			return true;
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		if (portDataList == null || portDataList.size() == 0)
			return true;
		else
			return false;
	}
	
	public boolean checkMachinePortIsEmpty(String machineName,String exceptPortName) throws CustomException
	{
		List<Port> portDataList = null;
		try
		{
			portDataList = PortServiceProxy.getPortService().select("WHERE 1=1 AND MACHINENAME = ? AND FULLSTATE = 'FULL' AND PORTNAME <> ? ", new Object[] { machineName ,exceptPortName});
		}
		catch (NotFoundSignal notFoundError)
		{
			return true;
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		if (portDataList == null || portDataList.size() == 0)
			return true;
		else
			return false;
	}
	
	public void validateSorterPort(Machine machineData, Port portData) throws CustomException
	{
		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		
		String portName = portData.getKey().getPortName();
		String portType = portData.getUdfs().get("PORTTYPE");
		String operationMode = machineData.getUdfs().get("OPERATIONMODE");
		String cleanMode = machineData.getUdfs().get("CLEANMODEFLAG");
		
		// PORT-0001:Invalid Port Type: Port {0} cannot use {1} type.{2}
		if (!EnumInfoUtil.SorterPort.validate(portType, portName))
			throw new CustomException("PORT-0001", portName, portType, EnumInfoUtil.SorterPort.getPortNameList(portType).toString());
		
		// check available port for operation mode
		if (!EnumInfoUtil.SorterOperationCondition.getPortNameList(operationMode).contains(portName))
			throw new CustomException("SORTER-0003",portName,operationMode);
		
		// OperationMode = C-TRAY Port 1,2,3 must be Empty | CleanMode = 'Y' Port 2 must be Empty
		if (EnumInfoUtil.SorterOperationCondition.CTRAY.getOperationMode().equals(operationMode))
		{
			try
			{
				PortServiceProxy.getPortService().select("WHERE 1=1 AND PORTNAME IN ('P01','P02','P03') AND MACHINENAME = ? AND FULLSTATE = ? ",
														  new Object[] { machineData.getKey().getMachineName(), constMap.Port_FULL });
				
				//SORTER-0001:Port ({1}) must be empty in {1} operation mode. 
				throw new CustomException("SORTER-0001","01,02,03","C-TRAY");
			}
			catch (CustomException ce)
			{
				throw new CustomException("SYS-0010", ce.errorDef.getLoc_errorMessage());
			}
			catch (Exception ex)
			{
				if (!(ex instanceof NotFoundSignal))
					throw new CustomException(ex.getCause());
			}
		}
		else
		{
			if ("Y".equals(cleanMode))
			{
				//In Clean mode, {0} port  cannot be load tray.  PL = P02
				if (portData.getUdfs().get("PORTTYPE").equals("PL") && portData.getUdfs().get("FULLSTATE").equals(constMap.Port_FULL))
					throw new CustomException("SORTER-0002",portData.getUdfs().get("PORTTYPE"));
			}
			/*else
			{
				// CleanMode = N tray of Port2 is load first。
				if (portData.getUdfs().get("PORTTYPE").equals(constMap.PORT_TYPE_PL))
				{
					try
					{
						PortServiceProxy.getPortService().select("WHERE 1=1 AND PORTTYPE ='BL' AND MACHINENAME = ? AND FULLSTATE = ? ",
																  new Object[] { machineData.getKey().getMachineName(), constMap.Port_FULL });
						
						//SORTER-0004:(Cleanmod = N) {0} port must be loaded first.  
						throw new CustomException("SORTER-0004","PL");
					}
					catch (CustomException ce)
					{
						throw new CustomException("SYS-0010", ce.errorDef.getLoc_errorMessage());
					}
					catch (Exception ex)
					{
						if (!(ex instanceof NotFoundSignal))
							throw new CustomException(ex.getCause());
					}
				}
				else
				{
					try
					{
						boolean hasLoaderPort = false;
						List<Port> loaderPortList = this.getProtListByType(machineData.getKey().getMachineName(), "PL");

						for (Port loaderPort : loaderPortList)
						{
							if (loaderPort.getUdfs().get("FULLSTATE").equals(constMap.Port_FULL))
							{
								hasLoaderPort = true; break;
							}
						}

						//SORTER-0004:(Cleanmod = N) {0} port must be loaded first.  
						if (!hasLoaderPort)
							throw new CustomException("SORTER-0004", "PL");
					}
					catch (CustomException ce)
					{
						throw new CustomException("SYS-0010", ce.errorDef.getLoc_errorMessage());
					}					
				}
			}*/
		}
	}
	
	public List<Port> getProtListByType(String machineName,String portType) throws CustomException
	{
		if (log.isInfoEnabled())
		{
			log.info("machineName = " + machineName);
			log.info("portName = " + portType);
		}

		List<Port> portDataList = new ArrayList<>();
		try
		{
			portDataList = PortServiceProxy.getPortService().select("WHERE 1=1  AND MACHINENAME =? AND PORTTYPE = ?  ",
																		           new Object[] { machineName, portType });
		}
		catch (NotFoundSignal notFoundEx)
		{
			log.info(String.format("Port Data Information is not registered.condition by MachineName [%s] and PortType [%s].", machineName, portType));
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		return portDataList;
	}

	public Port getPortData(String machineName, String portName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		try
		{
			if (log.isInfoEnabled())
			{
				log.info("machineName = " + machineName);
				log.info("portName = " + portName);
			}
			PortKey portKey = new PortKey();
			portKey.setMachineName(machineName);
			portKey.setPortName(portName);

			Port portData = null;
			portData = PortServiceProxy.getPortService().selectByKey(portKey);

			return portData;
		}
		catch (Exception e)
		{
			throw new CustomException("PORT-9000", machineName, portName);
		}
	}

	public PortSpec getPortSpecInfo(String machineName, String portName) throws CustomException
	{
		try
		{
			PortSpecKey portSpecKey = new PortSpecKey();
			portSpecKey.setMachineName(machineName);
			portSpecKey.setPortName(portName);

			PortSpec portSpecData = null;

			portSpecData = PortServiceProxy.getPortSpecService().selectByKey(portSpecKey);

			return portSpecData;
		}
		catch (Exception e)
		{
			throw new CustomException("PORT-9000", machineName, portName);
		}
	}

	public void loadRequest(EventInfo eventInfo, String machineName, String portName) throws CustomException
	{
		// 1. Get Port Data.
		Port portData = CommonUtil.getPortInfo(machineName, portName);

		// 2. PortTransferState change ReadyToLoad.
		if (!StringUtils.equals(portData.getTransferState(), GenericServiceProxy.getConstantMap().Port_ReadyToLoad))
		{
			eventInfo = EventInfoUtil.makeEventInfo("ChangeTransferState", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);

			MakeTransferStateInfo makeTransferStateInfo = new MakeTransferStateInfo();
			makeTransferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToLoad);
			makeTransferStateInfo.setValidateEventFlag("N");
			
			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("FULLSTATE", GenericServiceProxy.getConstantMap().Port_EMPTY);
			makeTransferStateInfo.setUdfs(udfs);

			MESPortServiceProxy.getPortServiceImpl().makeTransferState(portData, makeTransferStateInfo, eventInfo);
		}

		if (!StringUtils.equals(portData.getUdfs().get("FULLSTATE"), GenericServiceProxy.getConstantMap().Port_EMPTY))
		{
			eventInfo = EventInfoUtil.makeEventInfo("ChangeFullState", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);

			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("FULLSTATE", GenericServiceProxy.getConstantMap().Port_EMPTY);

			MESPortServiceProxy.getPortServiceImpl().setEvent(portData, setEventInfo, eventInfo);
		}
		eventInfo = EventInfoUtil.makeEventInfo("ChangeLoadRequestTime", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("LOADREQUESTTIME", TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY));
		MESPortServiceProxy.getPortServiceImpl().setEvent(portData, setEventInfo, eventInfo);
	}

	public void loadComplete(EventInfo eventInfo, String machineName, String portName) throws CustomException
	{
		// 1. Get Port Data.
		Port portData = CommonUtil.getPortInfo(machineName, portName);

		// 2. PortTransferState change ReadyToProcess.
		if (!StringUtils.equals(portData.getTransferState(), GenericServiceProxy.getConstantMap().Port_ReadyToProcess))
		{
			eventInfo = EventInfoUtil.makeEventInfo("ChangeTransferState", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);

			MakeTransferStateInfo makeTransferStateInfo = new MakeTransferStateInfo();
			makeTransferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToProcess);
			makeTransferStateInfo.setValidateEventFlag("N");
			makeTransferStateInfo.getUdfs().put("FULLSTATE", GenericServiceProxy.getConstantMap().Port_FULL);

			MESPortServiceProxy.getPortServiceImpl().makeTransferState(portData, makeTransferStateInfo, eventInfo);
		}

		if (!StringUtils.equals(portData.getUdfs().get("FULLSTATE"), GenericServiceProxy.getConstantMap().Port_FULL))
		{
			eventInfo = EventInfoUtil.makeEventInfo("ChangeFullState", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);

			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("FULLSTATE", GenericServiceProxy.getConstantMap().Port_FULL);

			MESPortServiceProxy.getPortServiceImpl().setEvent(portData, setEventInfo, eventInfo);
		}
	}

	public void unLoadRequest(EventInfo eventInfo, String machineName, String portName) throws CustomException
	{
		// 1. Get Port Data.
		Port portData = CommonUtil.getPortInfo(machineName, portName);

		// 2. PortTransferState change ReadyToLoad.
		if (!StringUtils.equals(portData.getTransferState(), GenericServiceProxy.getConstantMap().Port_ReadyToUnload))
		{
			// eventInfo.setEventName("ChangeTransferState");
			eventInfo = EventInfoUtil.makeEventInfo("ChangeTransferState", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);

			MakeTransferStateInfo makeTransferStateInfo = new MakeTransferStateInfo();
			makeTransferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToUnload);
			makeTransferStateInfo.setValidateEventFlag("N");

			MESPortServiceProxy.getPortServiceImpl().makeTransferState(portData, makeTransferStateInfo, eventInfo);
		}

		if (!StringUtils.equals(portData.getUdfs().get("FULLSTATE"), GenericServiceProxy.getConstantMap().Port_FULL))
		{
			eventInfo = EventInfoUtil.makeEventInfo("ChangeFullState", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);

			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("FULLSTATE", GenericServiceProxy.getConstantMap().Port_FULL);

			MESPortServiceProxy.getPortServiceImpl().setEvent(portData, setEventInfo, eventInfo);
		}
	}

	public void unLoadComplete(EventInfo eventInfo, String machineName, String portName) throws CustomException
	{
		// 1. Get Port Data.
		Port portData = CommonUtil.getPortInfo(machineName, portName);

		// 2. PortTransferState change ReadyToLoad.
		if (!StringUtils.equals(portData.getTransferState(), GenericServiceProxy.getConstantMap().Port_ReadyToLoad))
		{
			// eventInfo.setEventName("ChangeTransferState");
			eventInfo = EventInfoUtil.makeEventInfo("ChangeTransferState", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);

			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("FULLSTATE", GenericServiceProxy.getConstantMap().Port_EMPTY);

			MakeTransferStateInfo makeTransferStateInfo = new MakeTransferStateInfo();
			makeTransferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToLoad);
			makeTransferStateInfo.setValidateEventFlag("N");
			makeTransferStateInfo.setUdfs(udfs);

			MESPortServiceProxy.getPortServiceImpl().makeTransferState(portData, makeTransferStateInfo, eventInfo);
		}

		if (!StringUtils.equals(portData.getUdfs().get("FULLSTATE"), GenericServiceProxy.getConstantMap().Port_EMPTY))
		{
			eventInfo = EventInfoUtil.makeEventInfo("ChangeFullState", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);

			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("FULLSTATE", GenericServiceProxy.getConstantMap().Port_EMPTY);

			MESPortServiceProxy.getPortServiceImpl().setEvent(portData, setEventInfo, eventInfo);
		}
	}

	public void portProcessing(EventInfo eventInfo, String machineName, String portName) throws CustomException
	{
		// 1. Get Port Data.
		Port portData = new Port();
		if (StringUtil.isNotEmpty(portName))
			portData = CommonUtil.getPortInfo(machineName, portName);

		if (!StringUtils.equals(portData.getTransferState(), GenericServiceProxy.getConstantMap().Port_Processing))
		{
			eventInfo = EventInfoUtil.makeEventInfo("ChangeTransferState", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);

			MakeTransferStateInfo makeTransferStateInfo = new MakeTransferStateInfo();
			makeTransferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_Processing);
			makeTransferStateInfo.setValidateEventFlag("N");
			makeTransferStateInfo.setUdfs(portData.getUdfs());

			MESPortServiceProxy.getPortServiceImpl().makeTransferState(portData, makeTransferStateInfo, eventInfo);
		}
	}

	public SetEventInfo MaskloadComplete(EventInfo eventInfo, String machineName, String portName, String portuseType, String portType) throws CustomException
	{
		Port portData = CommonUtil.getPortInfo(machineName, portName);

		eventInfo = EventInfoUtil.makeEventInfo("ChangeTransferState", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);

		MakeTransferStateInfo makeTransferStateInfo = new MakeTransferStateInfo();
		makeTransferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToProcess);
		makeTransferStateInfo.setValidateEventFlag("N");
		MESPortServiceProxy.getPortServiceImpl().makeTransferState(portData, makeTransferStateInfo, eventInfo);

		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("FULLSTATE", GenericServiceProxy.getConstantMap().Port_FULL);
		setEventInfo.getUdfs().put("PORTTYPE", portType);
		setEventInfo.getUdfs().put("PORTUSETYPE", portuseType);

		return setEventInfo;

	}

	public SetEventInfo MaskUnloadComplete(EventInfo eventInfo, String machineName, String portName, String portType) throws CustomException
	{
		Port portData = CommonUtil.getPortInfo(machineName, portName);

		eventInfo = EventInfoUtil.makeEventInfo("ChangeTransferState", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);

		MakeTransferStateInfo makeTransferStateInfo = new MakeTransferStateInfo();
		makeTransferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToUnload);
		makeTransferStateInfo.setValidateEventFlag("N");
		MESPortServiceProxy.getPortServiceImpl().makeTransferState(portData, makeTransferStateInfo, eventInfo);

		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("FULLSTATE", GenericServiceProxy.getConstantMap().Port_EMPTY);
		setEventInfo.getUdfs().put("PORTNAME", portName);
		setEventInfo.getUdfs().put("PORTTYPE", portType);

		return setEventInfo;

	}
}
