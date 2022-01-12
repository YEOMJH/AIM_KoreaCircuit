package kr.co.aim.messolution.port.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.DirtyInfo;
import kr.co.aim.greentrack.durable.management.info.IncrementTimeUsedInfo;
import kr.co.aim.greentrack.durable.management.info.SetAreaInfo;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class UnloadComplete extends AsyncHandler {

	private static Log log = LogFactory.getLog(UnloadComplete.class);
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Unload", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
		
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		
		Durable durableData = null;
		if (StringUtils.isNotEmpty(carrierName)&&!(machineName.equals("3ACI01")&&CommonUtil.equalsIn(portName, "P01","P02")))
		{
			try
			{
				durableData = DurableServiceProxy.getDurableService().selectByKey(new DurableKey(carrierName));
			}
			catch (Exception ex)
			{
				log.info("Durable data is not exists. CARRIERNAME=" + carrierName);
			}
		}
		
		if (StringUtils.isEmpty(carrierName) &&
			"POSTCELL".equals(machineData.getFactoryName()) && "FLC".equals(machineData.getMachineGroupName()) &&
			"BL".equals(portData.getUdfs().get("PORTTYPE")) && "Buffer".equals(portData.getUdfs().get("PORTKIND")))
		{
			// If the EQP is CellCut, and BL Buffer Port, <CARRIERNAME> is Empty, don't change durable data			
			log.info("FactoryName = 'POSTCELL' and MachineGroupName = 'FLC' and PortType = 'BL' and PortKind = 'Buffer'");
		}
		else if ("POSTCELL".equals(machineData.getFactoryName()) && "FLC".equals(machineData.getMachineGroupName()) &&
				 "BL".equals(portData.getUdfs().get("PORTTYPE")) && "Out".equals(portData.getUdfs().get("PORTKIND")))
		{
			if (durableData != null && "CoverTray".equals(durableData.getDurableType()))
			{
				// If the EQP is CellCut, and BL Out Port, and DurableType is CoverTray, don't change durable data
				log.info("FactoryName = 'POSTCELL' and MachineGroupName = 'FLC' and PortType = 'BL' and PortKind = 'Out' and DurableType = 'CoverTray'");
			}
			else
			{
				// If the EQP is CellCut, and BL Out Port, and DurableType is not CoverTray, clear durable data
				log.info("FactoryName = 'POSTCELL' and MachineGroupName = 'FLC' and PortType = 'BL' and PortKind = 'Out' and DurableType != 'CoverTray'");
				this.changeDurableInfo(eventInfo, portType, carrierName, "");
			}
		}
		else if ("POSTCELL".equals(machineData.getFactoryName()) && "FLC".equals(machineData.getMachineGroupName()) &&
				 "PU".equals(portData.getUdfs().get("PORTTYPE")) && "Buffer".equals(portData.getUdfs().get("PORTKIND")))
		{
			// If the EQP is CellCut, and PU Buffer Port, don't change durable data			
			log.info("FactoryName = 'POSTCELL' and MachineGroupName = 'FLC' and PortType = 'PU' and PortKind = 'Buffer'");
		}
		else
		{
			// change carrier TransferState & LocationInfo
			if(!(machineName.equals("3ACI01")&&CommonUtil.equalsIn(portName, "P01","P02")))
			this.changeDurableInfo(eventInfo, portType, carrierName, GenericServiceProxy.getConstantMap().Dur_MOVING);		
		}
		
		if(StringUtils.isNotEmpty(carrierName)
				&&(StringUtils.equals(machineData.getMachineGroupName(), "Unpacker")&&StringUtils.equals(portData.getUdfs().get("PORTTYPE"), "PL"))) 
		{
			Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(carrierName);
			if(StringUtils.equals(consumableData.getConsumableType(), "Crate"))
			{
				MESConsumableServiceProxy.getConsumableServiceUtil().changeConsumableLoadFlag(eventInfo, carrierName, "", "", "N");
			}
		}
		
		
		// change portInfo
		MESPortServiceProxy.getPortServiceUtil().unLoadComplete(eventInfo, machineName, portName);

		// append full state
		Element eleFullState = new Element("FULLSTATE");
		eleFullState.setText("EMPTY");
		SMessageUtil.getBodyElement(doc).addContent(eleFullState);
		
		// clear sorter runtime info(use for CoverTrayGroupInfoDownLoad)
		if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Sorter) && StringUtils.equals(machineData.getFactoryName(), "POSTCELL"))
		{
			if (MESPortServiceProxy.getPortServiceUtil().AllPortIsEmpty(machineName) && !CommonValidation.checkProcessingLotOnEQP(machineData.getFactoryName(),machineName))
				ExtendedObjectProxy.getRunTimeMachineInfoService().remove(machineName);
		}
		
		// success then report to FMB
		GenericServiceProxy.getESBServive().sendBySenderToFMB(doc);
	}
	
	private void changeDurableInfo(EventInfo eventInfo, String portType, String carrierName, String transportState) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		
		Map<String, String> udfs = new HashMap<String, String>();
		udfs.put("MACHINENAME", "");
		udfs.put("PORTNAME", "");
		udfs.put("TRANSPORTSTATE", transportState);
		udfs.put("POSITIONTYPE", "");
		udfs.put("POSITIONNAME", "");
		udfs.put("ZONENAME", "");
		
		if (StringUtils.equals(GenericServiceProxy.getConstantMap().PORT_TYPE_PU, portType) &&
			StringUtils.equals(GenericServiceProxy.getConstantMap().DURABLETYPE_PeelingFilmBox, durableData.getDurableType()))
		{
			kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
			setEventInfo.setUdfs(udfs);
			durableData.setAreaName("");
			durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
			DurableServiceProxy.getDurableService().update(durableData);
			
			DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
		}		
		else
		{
			SetAreaInfo setAreaInfo = new SetAreaInfo();
			setAreaInfo.setAreaName("");
			setAreaInfo.setUdfs(udfs);

			EventInfo setAreaEventInfo = EventInfoUtil.makeEventInfo(eventInfo.getEventName(), eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
			DurableServiceProxy.getDurableService().setArea(durableData.getKey(), setAreaEventInfo, setAreaInfo);					
		}
		
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
						setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MaterialLocation_OutEQP);
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
}