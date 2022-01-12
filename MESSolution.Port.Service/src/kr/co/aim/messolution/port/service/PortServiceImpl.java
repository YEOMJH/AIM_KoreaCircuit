package kr.co.aim.messolution.port.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.data.PortSpec;
import kr.co.aim.greentrack.port.management.info.MakeAccessModeInfo;
import kr.co.aim.greentrack.port.management.info.MakeE10StateInfo;
import kr.co.aim.greentrack.port.management.info.MakePortStateByStateInfo;
import kr.co.aim.greentrack.port.management.info.MakePortStateInfo;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;
import kr.co.aim.greentrack.port.management.info.UndoInfo;
import kr.co.aim.greentrack.portspec.management.info.ChangeSpecInfo;

public class PortServiceImpl implements ApplicationContextAware {

	private ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog(PortServiceImpl.class);

	public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		applicationContext = arg0;
	}

	public void makeAccessMode(Port portData, MakeAccessModeInfo makeAccessModeInfo, EventInfo eventInfo) throws CustomException
	{
		String machineName = portData.getKey().getMachineName();
		String portName = portData.getKey().getPortName();
		String accessMode = makeAccessModeInfo.getAccessMode();

		try
		{
			accessMode = StringUtils.upperCase(accessMode);
			makeAccessModeInfo.setAccessMode(accessMode);

			if (!StringUtils.equals(portData.getAccessMode(), accessMode))
			{
				PortServiceProxy.getPortService().makeAccessMode(portData.getKey(), eventInfo, makeAccessModeInfo);
				log.info("Event Name = " + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
			}
			else
			{
				log.info("AccessMode is Same. DB=" + portData.getAccessMode() + " DATA=" + accessMode);
			}
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("PORT-9003", machineName, portName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("PORT-9999", fe.getMessage());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("PORT-9001", machineName, portName);
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("PORT-9002", machineName, portName);
		}
	}

	public void changePortType(PortSpec portSpec, ChangeSpecInfo changeSpecInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{

		// Same Value Check
		if (!StringUtils.equals(portSpec.getPortType(), changeSpecInfo.getPortType()))
		{
			PortServiceProxy.getPortSpecService().changeSpec(portSpec.getKey(), changeSpecInfo, eventInfo.getEventUser(), eventInfo.getEventComment());
			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		}
		else
		{
			log.info("PortType is Same. DB : " + portSpec.getPortType() + " DATA : " + changeSpecInfo.getPortType());
		}

	}

	public void makPortState(Port portData, MakePortStateInfo makePortStateInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		// Same Value Check
		if (!StringUtils.equals(portData.getPortStateName(), makePortStateInfo.getPortEventName()))
		{
			PortServiceProxy.getPortService().makePortState(portData.getKey(), eventInfo, makePortStateInfo);
			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		}
		else
		{
			log.info("PortState is Same. DB : " + portData.getPortStateName() + " DATA : " + makePortStateInfo.getPortEventName());
		}
	}

	public void makeE10state(Port portData, MakeE10StateInfo makeE10StateInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		// Same Value Check
		if (!StringUtils.equals(portData.getE10State(), makeE10StateInfo.getE10State()))
		{
			PortServiceProxy.getPortService().makeE10State(portData.getKey(), eventInfo, makeE10StateInfo);
			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		}
		else
		{
			log.info("E10State is Same. DB : " + portData.getE10State() + " DATA : " + makeE10StateInfo.getE10State());
		}
	}

	public void makePortStateByState(Port portData, MakePortStateByStateInfo makePortStateByStateInfo, EventInfo eventInfo) throws CustomException
	{
		String machineName = portData.getKey().getMachineName();
		String portName = portData.getKey().getPortName();

		try
		{
			// Same Value Check
			if (!StringUtils.equals(portData.getPortStateName(), makePortStateByStateInfo.getPortStateName()))
			{
				PortServiceProxy.getPortService().makePortStateByState(portData.getKey(), eventInfo, makePortStateByStateInfo);

				log.info("EventName=" + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
			}
			else
			{
				log.info("portStateName is Same. DB=" + portData.getPortStateName() + " DATA=" + makePortStateByStateInfo.getPortStateName());
			}
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("PORT-9003", machineName, portName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("PORT-9999", fe.getMessage());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("PORT-9001", machineName, portName);
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("PORT-9002", machineName, portName);
		}
	}

	public void makeTransferState(Port portData, MakeTransferStateInfo makeTransferStateInfo, EventInfo eventInfo) throws CustomException
	{
		String machineName = portData.getKey().getMachineName();
		String portName = portData.getKey().getPortName();

		try
		{
			// Same Value Check
			if (!StringUtils.equals(portData.getTransferState(), makeTransferStateInfo.getTransferState()))
			{
				PortServiceProxy.getPortService().makeTransferState(portData.getKey(), eventInfo, makeTransferStateInfo);
				log.info("EventName=" + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
			}
			else
			{
				log.info("transferState is Same. DB=" + portData.getTransferState() + " DATA=" + makeTransferStateInfo.getTransferState());
			}
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("PORT-9003", machineName, portName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("PORT-9999", fe.getMessage());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("PORT-9001", machineName, portName);
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("PORT-9002", machineName, portName);
		}
	}

	public void setEvent(Port portData, SetEventInfo setEventInfo, EventInfo eventInfo) throws CustomException
	{
		String machineName = portData.getKey().getMachineName();
		String portName = portData.getKey().getPortName();

		try
		{
			PortServiceProxy.getPortService().setEvent(portData.getKey(), eventInfo, setEventInfo);
			log.info("EventName=" + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("PORT-9003", machineName, portName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("PORT-9999", fe.getMessage());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("PORT-9001", machineName, portName);
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("PORT-9002", machineName, portName);
		}
	}

	public void undo(Port portData, UndoInfo undoInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		PortServiceProxy.getPortService().undo(portData.getKey(), eventInfo, undoInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void changeFullState(String fullState, Port portData, EventInfo eventInfo)
	{
		try
		{
			if (StringUtils.equals(fullState, GenericServiceProxy.getConstantMap().Port_EMPTY) || StringUtils.equals(fullState, GenericServiceProxy.getConstantMap().Port_FULL))
			{
				if (!StringUtils.equals(fullState, portData.getUdfs().get("FULLSTATE")))
				{
					Map<String, String> udfs = new HashMap<String, String>();
					eventInfo.setEventName("ChangeFullState");
					
					SetEventInfo setEventInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(udfs);
					setEventInfo.getUdfs().put("FULLSTATE", fullState);
					setEvent(portData, setEventInfo, eventInfo);
				}
			}
		}
		catch (Exception e)
		{
			log.info("Cannot Change Port FullState " + portData.getKey().getMachineName() + ", " + portData.getKey().getPortName());
		}
	}

}