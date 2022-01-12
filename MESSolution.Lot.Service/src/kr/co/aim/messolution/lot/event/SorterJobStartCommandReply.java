package kr.co.aim.messolution.lot.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SortJobCarrier;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.management.data.Port;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class SorterJobStartCommandReply extends AsyncHandler {

	private Log log = LogFactory.getLog(SorterJobStartCommandReply.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String jobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);
		String Result = SMessageUtil.getBodyItemValue(doc, "RESULT", true);
		String ResultDescription = SMessageUtil.getBodyItemValue(doc, "RESULTDESCRIPTION", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Confirm", getEventUser(), getEventComment(), null, null);
		
		try
		{
			String originalSourceSubjectName = getOriginalSourceSubjectName();
			
			if (!StringUtils.equals(Result, "OK"))
			{
				log.info("Sorter Job start command  reply NG .[ " + ResultDescription + " ]");
                throw new CustomException("SYS-0010",ResultDescription);
			}
			
			MESLotServiceProxy.getLotServiceUtil().changeSortJobState(eventInfo, jobName, GenericServiceProxy.getConstantMap().SORT_JOBSTATE_CONFIRMED);
			log.info("Sorter Job start command Reply OK !!");
			
			String transferCSTFlag = CommonUtil.getEnumDefValueStringByEnumName("SorterJobCSTTransferFlag");
			if (StringUtils.equals(transferCSTFlag, "Y"))
			{
				boolean isChecked = true;
				
				List<Map<String, String>> transportJobMapList = new ArrayList<Map<String,String>>();
				
				List<SortJobCarrier> sortJobCarrierList = ExtendedObjectProxy.getSortJobCarrierService().getSortJobCarrierListByJobName(jobName);
				for (SortJobCarrier sortJobCarrier : sortJobCarrierList) 
				{
					String carrierName = sortJobCarrier.getCarrierName();
					String destinationMachineName = sortJobCarrier.getMachineName();
					String destinationPositionName = sortJobCarrier.getPortName();
					
					// Check if Destination Port is empty
					List<Durable> carrierDataList = this.getCarrierDataByDestinationPort(destinationMachineName, destinationPositionName);
					if (carrierDataList == null || carrierDataList.size() == 0)
					{
						Map<String, String> transportJobMap = this.createTransportJobMap(eventInfo, carrierName, destinationMachineName, destinationPositionName, originalSourceSubjectName);
						if (transportJobMap == null)
						{
							continue;
						}
						
						transportJobMapList.add(transportJobMap);
					}
					else if (carrierDataList != null && carrierDataList.size() == 1)
					{
						if (StringUtils.equals(carrierName, carrierDataList.get(0).getKey().getDurableName()))
						{
							log.info("The target CST has already been loaded.");
							continue;
						}
						else
						{
							log.info("Another CST has already been loaded. Carrier=" + carrierName + ",LoadedCarrier=" + carrierDataList.get(0).getKey().getDurableName());
							isChecked = false;
						}
					}
					else
					{
						log.info("Destination Port is not Empty. CarrierCount=" + carrierDataList.size());
						continue;
					}
				}
				
				if (isChecked)
				{
					// Send TransportJob Message to TEX
					if (transportJobMapList != null && transportJobMapList.size() > 0)
					{
						for (Map<String, String> transportJobMap : transportJobMapList) 
						{
							MESLotServiceProxy.getLotServiceUtil().RequestTransportJob(transportJobMap.get("CARRIERNAME"), 
									transportJobMap.get("SOURCEMACHINENAME"), transportJobMap.get("SOURCEPOSITIONTYPE"), transportJobMap.get("SOURCEPOSITIONNAME"), transportJobMap.get("SOURCEZONENAME"), 
									transportJobMap.get("DESTINATIONMACHINENAME"), transportJobMap.get("DESTINATIONPOSITIONTYPE"), transportJobMap.get("DESTINATIONPOSITIONNAME"), transportJobMap.get("DESTINATIONZONENAME"), 
									transportJobMap.get("LOTNAME"), transportJobMap.get("PRODUCTQUANTITY"), transportJobMap.get("CARRIERSTATE"), transportJobMap.get("PRIORITY"),
									transportJobMap.get("EVENTUSER"), transportJobMap.get("EVENTCOMMENT"), transportJobMap.get("ORIGINALSOURCESUBJECTNAME"));
						}
					}
				}
				else
				{
					// Send SorterJobCancel Message to EQP
					this.SorterJobCancelCommand(eventInfo, machineName, jobName);
					
					// Send Error Reply to OIC
					ResultDescription = "Another CST has already been loaded. Send SorterJobCancelCommand Message to EQP.";
					throw new CustomException("SYS-0010", ResultDescription);
				}
			}
		}
		catch (Exception ex)
		{
			CustomException custEx = null;
			if (ex instanceof CustomException)
				custEx = (CustomException) ex;
			else
				custEx = new CustomException("SYS-0010", ex.getMessage());

			interruptDowork(custEx, doc);
		}
		
		// send to OIC
		if (doc.getRootElement().getChild(SMessageUtil.Return_Tag) == null)
			GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");
		else
			GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), JdomUtils.toString(doc), "OICSender");

	}
	
	/*
	 * 2020-12-15	dhko	Add Function
	 */
	private List<Durable> getCarrierDataByDestinationPort(String machineName, String portName)
	{
		try
		{
			String condition = "WHERE 1 = 1 "
							 + "  AND MACHINENAME = ? "
							 + "  AND PORTNAME = ? ";
			
			return DurableServiceProxy.getDurableService().select(condition, new Object[] { machineName, portName });
		}
		catch(Exception ex)
		{
			return null;
		}
	}
	
	/*
	 * 2020-12-15	dhko	Add Function
	 */
	private Map<String, String> createTransportJobMap(EventInfo eventInfo, String carrierName, String destinationMachineName, 
			String destinationPositionName, String originalSourceSubjectName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		// Set Source Location Info
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		
		String sourceMachineName = durableData.getUdfs().get("MACHINENAME");
		if (StringUtils.isEmpty(sourceMachineName))
		{
			log.info("SourceMachineName is Empty by CST: " + carrierName);
			return null;
		}
	
		// Source Location Info
		String sourcePositionType = StringUtils.EMPTY;
		String sourcePositionName = StringUtils.EMPTY;
		String sourceZoneName = StringUtils.EMPTY;
		
		MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(sourceMachineName);
		if (StringUtils.equals(machineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_StorageMachine))
		{
			sourceZoneName = durableData.getUdfs().get("ZONENAME");
			sourcePositionType = durableData.getUdfs().get("POSITIONTYPE");

			if (StringUtils.equals(sourcePositionType, "PORT"))
			{
				sourcePositionName = durableData.getUdfs().get("PORTNAME");
			}
			else
			{
				sourcePositionName = durableData.getUdfs().get("POSITIONNAME");
			}
		}
		else
		{
			sourceZoneName = "";
			sourcePositionType = "PORT";
			sourcePositionName = durableData.getUdfs().get("PORTNAME");
		}
		
		// Destination EQP Status
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(destinationMachineName);

		if (!CommonUtil.equalsIn(machineData.getMachineStateName(), "IDLE", "RUN"))
		{
			log.info("DestinationMachine State is " + machineData.getMachineStateName());
			return null;
		}

		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(destinationMachineName, destinationPositionName);

		if (!StringUtils.equals(portData.getPortStateName(), "UP"))
		{
			log.info("DestinationPort State is " + portData.getPortStateName());
			return null;
		}

		// Lot Info
		Map<String, Object> lotInfo = getLotInfo(carrierName);

		String lotName = ConvertUtil.getMapValueByName(lotInfo, "LOTNAME");
		String productQuantity = ConvertUtil.getMapValueByName(lotInfo, "PRODUCTQUANTITY");
		
		// Create RequestTransportJobRequest Map
		Map<String, String> transportJobMap = new HashMap<String, String>();
		transportJobMap.put("CARRIERNAME", carrierName);
		transportJobMap.put("SOURCEMACHINENAME", sourceMachineName);
		transportJobMap.put("SOURCEPOSITIONTYPE", sourcePositionType);
		transportJobMap.put("SOURCEPOSITIONNAME", sourcePositionName);
		transportJobMap.put("SOURCEZONENAME", sourceZoneName);
		transportJobMap.put("DESTINATIONMACHINENAME", destinationMachineName);
		transportJobMap.put("DESTINATIONPOSITIONTYPE", "PORT");
		transportJobMap.put("DESTINATIONPOSITIONNAME", destinationPositionName);
		transportJobMap.put("DESTINATIONZONENAME", "");
		transportJobMap.put("LOTNAME", lotName);
		transportJobMap.put("PRODUCTQUANTITY", productQuantity);
		transportJobMap.put("CARRIERSTATE", "FULL");
		transportJobMap.put("PRIORITY", "50");
		transportJobMap.put("EVENTUSER", getEventUser());
		transportJobMap.put("EVENTCOMMENT", "Transport For Sort. " + eventInfo.getEventComment());
		transportJobMap.put("ORIGINALSOURCESUBJECTNAME", originalSourceSubjectName);
		
		return transportJobMap;
	}
	
	/*
	 * 2020-12-15	dhko	Add Function
	 */
	private void SorterJobCancelCommand(EventInfo eventInfo, String machineName, String jobName) throws Exception
	{
		Element bodyElement = new Element(SMessageUtil.Body_Tag);
		bodyElement.addContent(new Element("MACHINENAME").setText(machineName));
		bodyElement.addContent(new Element("JOBNAME").setText(jobName));
		
		Document document = SMessageUtil.createXmlDocument(bodyElement, "SorterJobCancelCommand", GenericServiceProxy.getESBServive().getSendSubject("PEXsvr"), "", eventInfo.getEventUser(), eventInfo.getEventComment());
		
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		String targetSubjectName = machineData.getUdfs().get("MCSUBJECTNAME");
		GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, document, "EISSender");
	}
	
	private void interruptDowork(CustomException customEx, Document doc) throws CustomException
	{
		String language = "English";

		try
		{
			language = JdomUtils.getNodeText(doc, "//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Header_Tag + "/" + "LANGUAGE");
		}
		catch (Exception ex)
		{
		}

		String errorCode = customEx.errorDef.getErrorCode();
		String errorMessage = customEx.errorDef.getEng_errorMessage();

		if ("Chinese".equals(language))
		{
			errorMessage = customEx.errorDef.getCha_errorMessage();
		}
		else if ("Korean".equals(language))
		{
			errorMessage = customEx.errorDef.getKor_errorMessage();
		}

		Element returnElement = doc.getRootElement().getChild(SMessageUtil.Return_Tag);

		if (returnElement == null)
		{
			returnElement = new Element(SMessageUtil.Return_Tag);
			returnElement.addContent(new Element(SMessageUtil.Result_ReturnCode));
			returnElement.addContent(new Element(SMessageUtil.Result_ErrorMessage));
			doc.getRootElement().addContent(returnElement);
		}

		returnElement.getChild(SMessageUtil.Result_ReturnCode).setText(errorCode);
		returnElement.getChild(SMessageUtil.Result_ErrorMessage).setText(errorMessage);
		
		// send to OIC
		GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), JdomUtils.toString(doc), "OICSender");
		throw customEx;
	}

	private Map<String, Object> getLotInfo(String carrierName)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT MIN (LOTNAME) AS LOTNAME, SUM (PRODUCTQUANTITY) AS PRODUCTQUANTITY ");
		sql.append("  FROM LOT ");
		sql.append(" WHERE CARRIERNAME = :CARRIERNAME ");
		sql.append("   AND LOTSTATE = 'Released' ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("CARRIERNAME", carrierName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		return result.get(0);
	}
}
