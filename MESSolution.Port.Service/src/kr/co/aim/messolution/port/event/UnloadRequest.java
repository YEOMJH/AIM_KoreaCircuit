package kr.co.aim.messolution.port.event;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.DirtyInfo;
import kr.co.aim.greentrack.durable.management.info.IncrementDurationUsedInfo;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.data.PortSpec;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.OrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class UnloadRequest extends AsyncHandler {

	private static Log log = LogFactory.getLog(UnloadRequest.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnloadRequest", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);

		if (StringUtils.isEmpty(carrierName) &&
			"POSTCELL".equals(machineData.getFactoryName()) && "FLC".equals(machineData.getMachineGroupName()) &&
			"BL".equals(portData.getUdfs().get("PORTTYPE")) && "Buffer".equals(portData.getUdfs().get("PORTKIND")))
		{
			// If the EQP is CellCut, and BL Buffer Port, <CARRIERNAME> is Empty, don't to anything
			log.info("FactoryName = 'POSTCELL' and MachineGroupName = 'FLC' and PortType = 'BL' and PortKind = 'Buffer'");
			return ;
		}
		
		// change carrier TransferState & LocationInfo
		if (StringUtils.isNotEmpty(carrierName) && 
			"POSTCELL".equals(machineData.getFactoryName()) && "FLC".equals(machineData.getMachineGroupName()) &&
			"PL".equals(portData.getUdfs().get("PORTTYPE")))
		{
			// REQID : C0-FA-0016-01
			// If the EQP is CellCut, and PL Port, CleanState of Durable change to Dirty
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
			if (GenericServiceProxy.getConstantMap().Dur_Available.equals(durableData.getDurableState()))
			{
				MESDurableServiceProxy.getDurableServiceImpl().makeTransportStateOnEQPAndDirty(eventInfo, carrierName, machineName, portName);
			}
			else
			{
				MESDurableServiceProxy.getDurableServiceImpl().makeTransportStateOnEQP(eventInfo, carrierName, machineName, portName);
			}
		}
		else
		{
			MESDurableServiceProxy.getDurableServiceImpl().makeTransportStateOnEQP(eventInfo, carrierName, machineName, portName);			
		}

		// change portInfo
		MESPortServiceProxy.getPortServiceUtil().unLoadRequest(eventInfo, machineName, portName);

		try
		{
			if ("POSTCELL".equals(machineData.getFactoryName()) && "FLC".equals(machineData.getMachineGroupName()) &&
				"BL".equals(portData.getUdfs().get("PORTTYPE")) && "Out".equals(portData.getUdfs().get("PORTKIND")))
			{
				// If the EQP is CellCut, and BL Out Port, don't message send to TEMsvr
				log.info("FactoryName = 'POSTCELL' and MachineGroupName = 'FLC' and PortType = 'BL' and PortKind = 'Buffer'");	
			}
			else if ("POSTCELL".equals(machineData.getFactoryName()) && "FLC".equals(machineData.getMachineGroupName()) &&
				"PU".equals(portData.getUdfs().get("PORTTYPE")) && "Buffer".equals(portData.getUdfs().get("PORTKIND")))
			{
				// If the EQP is CellCut, and PU Buffer Port, don't message send to TEMsvr
				log.info("FactoryName = 'POSTCELL' and MachineGroupName = 'FLC' and PortType = 'PU' and PortKind = 'Buffer'");
			}
			else
			{
				// send to TEMsvr
				String replySubject = GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("TEMsvr");
				GenericServiceProxy.getESBServive().sendReplyBySender(replySubject, doc, "TEMSender");				
			}
            
//			// Mantis : 0000440
//			// 所有Mask CST 当DurationUsed>DurationUsedLimit，MaskCST变Dirty
//			eventInfo.setEventName("MonitorCSTDurationUsed");
//			this.incrementDurationUsed(eventInfo, carrierName);
			
			//2020/12/22 add by xiaoxh
			//********************************TP CST Full or Empty Auto Transport to 3CSTK02****************//
			this.sendRequestTransportJobRequest(carrierName, machineData, doc);
			//***********************************add end***************************************************//
		}
		catch (Exception e)
		{
		}
		
		
	}
	
	/**
	 * Mantis : 0000440
	 * 所有Mask CST 当DurationUsed>DurationUsedLimit，MaskCST变Dirty
	 * 
	 * 2021-01-07	dhko	Add Function
	 */
	@SuppressWarnings("unchecked")
	private void incrementDurationUsed(EventInfo eventInfo, String carrierName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		String sql = "SELECT DURABLENAME, "
				   + "       NVL(DURATIONUSED, 0) AS DURATIONUSED, "
				   + "       NVL(DURATIONUSEDLIMIT, 0) AS DURATIONUSEDLIMIT, "
				   + "       CASE WHEN LASTCLEANTIME IS NULL THEN 0 "
				   + "            ELSE ROUND((SYSDATE - LASTCLEANTIME) * 24 * 60) "
				   + "       END AS DURATIONTIME "
				   + "FROM DURABLE "
				   + "WHERE 1 = 1 "
				   + "  AND DURABLENAME = ? "
				   + "  AND DURABLETYPE IN('MaskCST','EVAMaskCST','TFEMaskCST') ";
		
		List<OrderedMap> resultDataList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, new Object[] { carrierName });
		if (resultDataList == null || resultDataList.size() == 0)
		{
			return ;
		}
		
		double currentDurationUsed = Double.valueOf(resultDataList.get(0).get("DURATIONUSED").toString());
		double durationTime = Double.valueOf(resultDataList.get(0).get("DURATIONTIME").toString());
		double durationUsedLimit = Double.valueOf(resultDataList.get(0).get("DURATIONUSEDLIMIT").toString());
		
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		
		IncrementDurationUsedInfo incrementDurationUsedInfo = new IncrementDurationUsedInfo();
		incrementDurationUsedInfo.setDurationUsed(durationTime - currentDurationUsed);
		
		MESDurableServiceProxy.getDurableServiceImpl().incrementDurationUsed(durableData, incrementDurationUsedInfo, eventInfo);
		
		durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		
		// DurationUsed >= DurationUsedLimit = Dirty
		if (durationUsedLimit != 0 && currentDurationUsed >= durationUsedLimit)
		{
			DirtyInfo dirtyInfo = new DirtyInfo();
			dirtyInfo.setUdfs(new HashMap<String, String>());
			
			eventInfo.setEventName("dirty");
			MESDurableServiceProxy.getDurableServiceImpl().dirty(durableData, dirtyInfo, eventInfo);
		}
	}
	
	public void sendRequestTransportJobRequest(String carrierName,Machine machineData,Document doc) throws CustomException
	{
		Boolean autoTransport = false;
		String [] machineList = new String [] {"3CIS01","3CIS02","3CIS03"};
		if(machineData.getFactoryName().equals("OLED")&&Arrays.asList(machineList).contains(machineData.getKey().getMachineName()))
		{
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
			String durableType = durableData.getDurableType();
			String durableState = durableData.getDurableState();
			if(durableType.equals("TPGlassCST"))
			{
				Lot lotDataByCarrier = new Lot();
				if(durableState.equals("Available"))
				{
					autoTransport=true;
					
				}
				else if(durableState.equals("InUse"))
				{				
					String lotNameByCarrier = MESLotServiceProxy.getLotServiceUtil().getLotInfoBydurableNameForFisrtGlass(carrierName);
					try{lotDataByCarrier = MESLotServiceProxy.getLotServiceUtil().getLotData(lotNameByCarrier);}catch (Exception e){}
					if(lotDataByCarrier !=null&&lotDataByCarrier.getProductQuantity()==60)
					{
						autoTransport=true;
					}
				}
				if(autoTransport)	
				{
					SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "RequestTransportJobRequest");
					Element newBody = generateReturnBodyTemplate(SMessageUtil.getBodyElement(doc),durableData, lotDataByCarrier);
					doc.getRootElement().getChild(SMessageUtil.Body_Tag).detach();
					doc.getRootElement().addContent(2,newBody);
//					String replySubject = GenericServiceProxy.getESBServive().getSendSubject("MCS");
//					GenericServiceProxy.getESBServive().sendReplyBySender(replySubject, doc, "HIFSender");
					String replySubject = GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("TEMsvr");
					GenericServiceProxy.getESBServive().sendReplyBySender(replySubject, doc, "TEMSender");			
				}
				
			}			
		}
		
	}
	
	private Element generateReturnBodyTemplate(Element bodyElement, Durable durableData,Lot lotData) throws CustomException
	{
		Element body = new Element(SMessageUtil.Body_Tag);
		String transportJobType = GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_OIC;
		String transportJobName = "";
		try
		{
			transportJobName = MESTransportServiceProxy.getTransportJobServiceUtil().generateTransportJobIdBySender(durableData.getKey().getDurableName(), transportJobType);
		}
		catch (Exception ex)
		{
			eventLog.error(ex);
		}
		finally
		{
			eventLog.debug("generated Job ID=" + transportJobName);
		}

		JdomUtils.addElement(body, "TRANSPORTJOBNAME", transportJobName);
		JdomUtils.addElement(body, "CARRIERNAME", durableData.getKey().getDurableName());
		JdomUtils.addElement(body, "SOURCEMACHINENAME", bodyElement.getChildText("MACHINENAME"));
		JdomUtils.addElement(body, "SOURCEPOSITIONTYPE", "PORT");
		JdomUtils.addElement(body, "SOURCEPOSITIONNAME", bodyElement.getChildText("PORTNAME"));
		JdomUtils.addElement(body, "SOURCEZONENAME", StringUtils.EMPTY);
		JdomUtils.addElement(body, "DESTINATIONMACHINENAME", "3CSTK02");
		JdomUtils.addElement(body, "DESTINATIONPOSITIONTYPE", "SHELF");
		JdomUtils.addElement(body, "DESTINATIONZONENAME", bodyElement.getChildText("MACHINENAME")+"_Z01");
		JdomUtils.addElement(body, "MATERIALLIST", StringUtils.EMPTY);
		JdomUtils.addElement(body, "PRIORITY", "51");
		if(durableData.getDurableState().equals("Available"))
			JdomUtils.addElement(body, "CARRIERSTATE",  "EMPTY");		
		else 	
			JdomUtils.addElement(body, "CARRIERSTATE",  "FULL");
		JdomUtils.addElement(body, "CARRIERTYPE", "GlassCST");
		JdomUtils.addElement(body, "CLEANSTATE", durableData.getDurableCleanState());
		if(lotData!=null)
		{
			JdomUtils.addElement(body, "LOTNAME", lotData.getKey().getLotName());
			JdomUtils.addElement(body, "PRODUCTQUANTITY", String.valueOf(lotData.getProductQuantity()));
		}
			
		else
		{
			JdomUtils.addElement(body, "LOTNAME", StringUtils.EMPTY);
			JdomUtils.addElement(body, "PRODUCTQUANTITY", "0");
		}
		
		return body;
	}
}
