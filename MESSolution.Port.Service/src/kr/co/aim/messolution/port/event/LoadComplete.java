package kr.co.aim.messolution.port.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.IncrementTimeUsedInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.data.PortSpec;

public class LoadComplete extends AsyncHandler {
	private static Log log = LogFactory.getLog(LoadComplete.class);
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Load", getEventUser(), getEventComment(), "", "");

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);

		// change portInfo
		MESPortServiceProxy.getPortServiceUtil().loadComplete(eventInfo, machineName, portName);

		PortSpec portData = MESPortServiceProxy.getPortServiceUtil().getPortSpecInfo(machineName, portName);
		MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
		if(StringUtils.isNotEmpty(carrierName)
				&&!(StringUtils.equals(machineSpecData.getMachineGroupName(), "Unpacker")&&StringUtils.equals(portData.getPortType(), "PL")))
		{
			// change carrier TransferState & LocationInfo
			MESDurableServiceProxy.getDurableServiceImpl().makeTransportStateOnEQP(eventInfo, carrierName, machineName, portName);
			
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
			
			//Update film TransportState
			if(CommonUtil.equalsIn(durableData.getDurableType(), GenericServiceProxy.getConstantMap().DURABLETYPE_PeelingFilmBox, GenericServiceProxy.getConstantMap().DURABLETYPE_FilmBox) && StringUtils.equals(durableData.getDurableState(), "InUse"))
			{
				try
				{
					List<Consumable> filmList = MESConsumableServiceProxy.getConsumableServiceUtil().getConsumableListByDurable(carrierName);
					if(filmList != null && filmList.size() > 0)
					{
						for (Consumable filmData : filmList)
						{	
							SetEventInfo setEventInfo = new SetEventInfo();
							setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MaterialLocation_OnEQP);
							ConsumableServiceProxy.getConsumableService().setEvent(filmData.getKey(), eventInfo, setEventInfo);					
						}
					}
				}
				catch(Exception e)
				{
					log.info("Update film TransportState error");
				}			
			}
		}
		else if(StringUtils.isNotEmpty(carrierName)
				&&(StringUtils.equals(machineSpecData.getMachineGroupName(), "Unpacker")&&StringUtils.equals(portData.getPortType(), "PL"))) 
		{
			Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(carrierName);
			if(StringUtils.equals(consumableData.getConsumableType(), "Crate"))
			{
				MESConsumableServiceProxy.getConsumableServiceUtil().changeConsumableLoadFlag(eventInfo, carrierName, machineName, portName, "Y");
			}
		}

		
		// append full state
		Element eleFullState = new Element("FULLSTATE");
		eleFullState.setText("FULL");
		SMessageUtil.getBodyElement(doc).addContent(eleFullState);

		// success then report to FMB
		GenericServiceProxy.getESBServive().sendBySenderToFMB(doc);
	}
}