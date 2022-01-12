package kr.co.aim.messolution.transportjob.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.messolution.transportjob.service.TransportJobServiceUtil;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;

import org.apache.commons.collections.OrderedMap;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class RequestMaskBatchJobRequest extends SyncHandler {

	/**
	 * MessageSpec [TEX -> MCS]
	 * 
	 * <Body>
	 *    <BATCHJOBNAME />
	 *    <CARRIERNAME />
	 *    <MACHINENAME />
	 *    <SOURCEPOSITIONTYPE />
	 *    <SOURCEPOSITIONNAME />
	 *    <DESTINATIONPOSITIONTYPE />
	 *    <DESTINATIONPOSITIONNAME />
	 *    <TRANSPORTJOBLIST>
	 *       <TRANSPORTJOB>
	 *          <TRANSPORTJOBNAME />
	 *          <MASKNAME />
	 *          <SOURCEZONENAME />
	 *          <SOURCECARRIERSLOTNO />
	 *          <DESTINATIONZONENAME />
	 *          <DESTINATIONCARRIERSLOTNO />
	 *          <PRIORITY />
	 *          <MASKTYPE />
	 *       <TRANSPORTJOB>
	 *    <TRANSPORTJOBLIST>
	 * </body>
	 */
	
	@SuppressWarnings("unchecked")
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		// Set Variables
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TransportRequest", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		Element bodyElement = SMessageUtil.getBodyElement(doc);
		Element returnElement = doc.getRootElement().getChild("Return");
		String currentCarrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);

		String sourcePositionType = "";
		String sourcePositionName = "";
		String sourceCarrierName = "";
		String sourceCarrierSlotNo = "";
		String destinationPositionType = "";
		String destinationPositionName = "";
		String destinationCarrierName = "";
		String destinationCarrierSlotNo = "";
		String currentCarrierSlotNo = "";
		String priority = "50";
		String maskType = "";
		String messageName = SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", true);
		String jobState = TransportJobServiceUtil.getJobState(messageName, doc);
		String cancelState = TransportJobServiceUtil.getCancelState(messageName, doc);
		String changeState = TransportJobServiceUtil.getChangeState(messageName, doc);
		String transferState = GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_RESERVED;
		String alternateFlag = "N";
		String carrierState = "";
		String batchJobName = "";
		String transportJobName = "";
		String transportJobType = "";
		String transportType = "Manual";
		Element newBodyElement = null;
		Element transportJobList = new Element("TRANSPORTJOBLIST");
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

		try
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "RequestMaskBatchJobRequest");

			// Set TransportJobType
			if (StringUtils.isNotEmpty(SMessageUtil.getHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", false)))
			{
				transportJobType = GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_OIC;
			}
			else
			{
				transportJobType = GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_RTD;
			}

			// Get MaskCST Data
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(currentCarrierName);

			if (!CommonUtil.equalsIn(durableData.getDurableType(), "EVAMaskCST", "TFEMaskCST", "MaskCST"))
			{
				throw new CustomException("DURABLE-0014", durableData.getDurableType());
			}

			if (StringUtils.equals("Y", durableData.getUdfs().get("TRANSPORTLOCKFLAG")))
			{
				throw new CustomException("DURABLE-0015", currentCarrierName);
			}

			MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);

			if (!StringUtils.equals(machineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_StorageMachine))
			{
				throw new CustomException("MACHINE-0037", machineSpecData.getMachineType());
			}

			if (StringUtils.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_Available))
			{
				// Mask Transfer from Stocker to CST
				// Reserve Mask Transfer
				StringBuilder sql = new StringBuilder();
				sql.append("SELECT MASKLOTNAME, POSITION ");
				sql.append("  FROM CT_RESERVEMASKTRANSFER ");
				sql.append(" WHERE CARRIERNAME = :CARRIERNAME ");
				sql.append("   AND MASKLOTNAME IS NOT NULL ");
				sql.append("ORDER BY POSITION ");

				Map<String, String> args = new HashMap<String, String>();
				args.put("CARRIERNAME", currentCarrierName);

				result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

				if (result.size() == 0)
				{
					// Reserve Mask to EQP (2CEE01)
					sql.setLength(0);
					sql.append("SELECT MASKLOTNAME, POSITION ");
					sql.append("  FROM CT_RESERVEMASKTOEQP ");
					sql.append(" WHERE CARRIERNAME = :CARRIERNAME ");
					sql.append("   AND MASKLOTNAME IS NOT NULL ");
					sql.append("ORDER BY POSITION ");

					result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
				}

				if (result.size() == 0)
				{
					throw new CustomException("DURABLE-0016", currentCarrierName);
				}

				carrierState = GenericServiceProxy.getConstantMap().MCS_CARRIERSTATE_EMPTY;

				sourcePositionType = "SHELF";
				sourcePositionName = "";
				sourceCarrierName = "";
				destinationCarrierName = currentCarrierName;
				destinationPositionType = "PORT";
				destinationPositionName = portName;
			}
			else if (StringUtils.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_InUse))
			{
				// Mask Transfer from CST to CST
				StringBuilder sql = new StringBuilder();
				sql.append("SELECT MASKLOTNAME, POSITION ");
				sql.append("  FROM CT_MASKLOT ");
				sql.append(" WHERE CARRIERNAME = :CARRIERNAME ");
				sql.append("ORDER BY POSITION ");

				Map<String, String> args = new HashMap<String, String>();
				args.put("CARRIERNAME", currentCarrierName);

				result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args); 

				if (result.size() == 0)
				{
					throw new CustomException("DURABLE-0017", currentCarrierName);
				}

				carrierState = GenericServiceProxy.getConstantMap().MCS_CARRIERSTATE_FULL;

				sourcePositionType = "PORT";
				sourcePositionName = portName;
				sourceCarrierName = currentCarrierName;
				destinationCarrierName = "";
				destinationPositionType = "SHELF";
				destinationPositionName = "";
			}
			else
			{
				throw new CustomException("DURABLE-0018", durableData.getDurableState());
			}

			// Get BatchJobName
			batchJobName = MESTransportServiceProxy.getTransportJobServiceUtil().generateBatchTransportJobId(currentCarrierName);

			TransportJobCommand transportJobCommandInfo = new TransportJobCommand();
			transportJobCommandInfo.setTransportJobName(batchJobName);
			transportJobCommandInfo.setCarrierName(currentCarrierName);
			transportJobCommandInfo.setTransportJobType(transportJobType);
			transportJobCommandInfo.setJobState(jobState);
			transportJobCommandInfo.setCancelState(cancelState);
			transportJobCommandInfo.setChangeState(changeState);
			transportJobCommandInfo.setAlternateFlag(alternateFlag);
			transportJobCommandInfo.setTransferState(transferState);
			transportJobCommandInfo.setPriority(priority);
			transportJobCommandInfo.setSourceMachineName(machineName);
			transportJobCommandInfo.setSourcePositionType(sourcePositionType);
			transportJobCommandInfo.setSourcePositionName(sourcePositionName);
			transportJobCommandInfo.setSourceZoneName("");
			transportJobCommandInfo.setSourceCarrierName("");
			transportJobCommandInfo.setSourceCarrierSlotNo("");
			transportJobCommandInfo.setDestinationMachineName(machineName);
			transportJobCommandInfo.setDestinationPositionType(destinationPositionType);
			transportJobCommandInfo.setDestinationPositionName(destinationPositionName);
			transportJobCommandInfo.setDestinationZoneName("");
			transportJobCommandInfo.setDestinationCarrierName("");
			transportJobCommandInfo.setDestinationCarrierSlotNo("");
			transportJobCommandInfo.setCurrentMachineName(machineName);
			transportJobCommandInfo.setCurrentPositionType("PORT");
			transportJobCommandInfo.setCurrentPositionName(portName);
			transportJobCommandInfo.setCurrentZoneName("");
			transportJobCommandInfo.setCurrentCarrierName("");
			transportJobCommandInfo.setCurrentCarrierSlotNo("");
			transportJobCommandInfo.setCarrierState(carrierState);
			transportJobCommandInfo.setLotName("");
			transportJobCommandInfo.setProductQuantity(result.size());
			transportJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceUtil().setTransportJobCommandEventInfo(transportJobCommandInfo, eventInfo);
			transportJobCommandInfo.setLastEventResultCode(SMessageUtil.getReturnItemValue(doc, "RETURNCODE", false));
			transportJobCommandInfo.setLastEventResultText(SMessageUtil.getReturnItemValue(doc, "RETURNMESSAGE", false));

			try
			{
				ExtendedObjectProxy.getTransportJobCommand().create(eventInfo, transportJobCommandInfo);
			}
			catch (Exception e)
			{
				throw new CustomException("JOB-8011", e.getMessage());
			}

			// Set TransportType
			if (StringUtils.equals(transportJobType, GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_RTD))
			{
				transportType = "Auto";
			}

			// Durable - SetEvent
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("TRANSPORTLOCKFLAG", "Y");
			setEventInfo.getUdfs().put("TRANSPORTTYPE", transportType);
			DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);

			// Set Body
			newBodyElement = generateBodyTemplate(batchJobName, currentCarrierName, machineName, sourcePositionType, sourcePositionName, destinationPositionType, destinationPositionName);

			// Set BatchJob List
			for (Map<String, Object> map : result)
			{
				String maskName = ConvertUtil.getMapValueByName(map, "MASKLOTNAME");

				if (StringUtils.isNotEmpty(sourceCarrierName))
				{
					sourceCarrierSlotNo = ConvertUtil.getMapValueByName(map, "POSITION");
					currentCarrierSlotNo = ConvertUtil.getMapValueByName(map, "POSITION");
				}
				else
				{
					destinationCarrierSlotNo = ConvertUtil.getMapValueByName(map, "POSITION");
				}

				// Get TransportJobName
				try
				{
					transportJobName = MESTransportServiceProxy.getTransportJobServiceUtil().generateTransportJobIdBySender(maskName, transportJobType);
				}
				catch (Exception ex)
				{
					eventLog.error(ex);
				}
				finally
				{
					eventLog.debug("generated Job ID=" + transportJobName);
				}

				MaskLot maskData = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskName });
				maskType = maskData.getMaskType();

				String zoneName = null;

				if (StringUtils.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_InUse))
				{
					zoneName = this.getZoneNameByMaskStockZone(maskName, machineName);
				}

				transportJobCommandInfo = new TransportJobCommand();
				transportJobCommandInfo.setBatchJobName(batchJobName);
				transportJobCommandInfo.setTransportJobName(transportJobName);
				transportJobCommandInfo.setCarrierName(maskName);
				transportJobCommandInfo.setTransportJobType(transportJobType);
				transportJobCommandInfo.setJobState(jobState);
				transportJobCommandInfo.setCancelState(cancelState);
				transportJobCommandInfo.setChangeState(changeState);
				transportJobCommandInfo.setAlternateFlag(alternateFlag);
				transportJobCommandInfo.setTransferState(transferState);
				transportJobCommandInfo.setPriority(priority);
				transportJobCommandInfo.setSourceMachineName(machineName);
				transportJobCommandInfo.setSourcePositionType(sourcePositionType);
				transportJobCommandInfo.setSourcePositionName(sourcePositionName);
				transportJobCommandInfo.setSourceZoneName("");
				transportJobCommandInfo.setSourceCarrierName(sourceCarrierName);
				transportJobCommandInfo.setSourceCarrierSlotNo(sourceCarrierSlotNo);
				transportJobCommandInfo.setDestinationMachineName(machineName);
				transportJobCommandInfo.setDestinationPositionType(destinationPositionType);
				transportJobCommandInfo.setDestinationPositionName(destinationPositionName);
				transportJobCommandInfo.setDestinationZoneName(zoneName);
				transportJobCommandInfo.setDestinationCarrierName(destinationCarrierName);
				transportJobCommandInfo.setDestinationCarrierSlotNo(destinationCarrierSlotNo);
				transportJobCommandInfo.setCurrentMachineName(machineName);
				transportJobCommandInfo.setCurrentPositionType("PORT");
				transportJobCommandInfo.setCurrentPositionName(portName);
				transportJobCommandInfo.setCurrentZoneName("");
				transportJobCommandInfo.setCurrentCarrierName(currentCarrierName);
				transportJobCommandInfo.setCurrentCarrierSlotNo(currentCarrierSlotNo);
				transportJobCommandInfo.setCarrierState(carrierState);
				transportJobCommandInfo.setLotName(maskName);
				transportJobCommandInfo.setProductQuantity(1);
				transportJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceUtil().setTransportJobCommandEventInfo(transportJobCommandInfo, eventInfo);
				transportJobCommandInfo.setLastEventResultCode(SMessageUtil.getReturnItemValue(doc, "RETURNCODE", false));
				transportJobCommandInfo.setLastEventResultText(SMessageUtil.getReturnItemValue(doc, "RETURNMESSAGE", false));

				try
				{
					ExtendedObjectProxy.getTransportJobCommand().create(eventInfo, transportJobCommandInfo);
				}
				catch (Exception e)
				{
					throw new CustomException("JOB-8011", e.getMessage());
				}

				// Set TransportJobList
				Element transportJob = new Element("TRANSPORTJOB");

				JdomUtils.addElement(transportJob, "TRANSPORTJOBNAME", transportJobName);
				JdomUtils.addElement(transportJob, "MASKNAME", maskName);
				JdomUtils.addElement(transportJob, "SOURCEZONENAME", "");
				JdomUtils.addElement(transportJob, "SOURCECARRIERSLOTNO", sourceCarrierSlotNo);
				JdomUtils.addElement(transportJob, "DESTINATIONZONENAME", zoneName);
				JdomUtils.addElement(transportJob, "DESTINATIONCARRIERSLOTNO", destinationCarrierSlotNo);
				JdomUtils.addElement(transportJob, "PRIORITY", priority);
				JdomUtils.addElement(transportJob, "MASKTYPE", maskType);

				transportJobList.addContent(transportJob);
			}

			newBodyElement.removeChild("TRANSPORTJOBLIST");
			newBodyElement.addContent(transportJobList);

			bodyElement.detach();
			returnElement.detach();

			doc.getRootElement().addContent(newBodyElement);

			// Set OriginalSourceSubjectName for Transport Job from RTD to MCS
			if (transportJobType.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_RTD))
			{
				SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("TEMsvr"));
			}

			// Send Message to MCS
			String replySubject = GenericServiceProxy.getESBServive().getSendSubject("MCS");
			GenericServiceProxy.getESBServive().sendReplyBySender(replySubject, doc, "HIFSender");
		}
		catch (Exception e)
		{
			if (StringUtils.equals(transportJobType, GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_OIC))
			{
				String originalSourceSubjectName = getOriginalSourceSubjectName();
				GenericServiceProxy.getESBServive().sendErrorBySender(originalSourceSubjectName, doc, getLanguage(), e, "OICSender");
			}

			throw new CustomException(e);
		}

		return null;
	}

	private Element generateBodyTemplate(String batchJobName, String carrierName, String machineName, String sourcePositionType, String sourcePositionName, String destinationPositionType,
			String destinationPositionName)
	{
		Element newBodyElement = new Element("Body");

		JdomUtils.addElement(newBodyElement, "BATCHJOBNAME", batchJobName);
		JdomUtils.addElement(newBodyElement, "CARRIERNAME", carrierName);
		JdomUtils.addElement(newBodyElement, "MACHINENAME", machineName);
		JdomUtils.addElement(newBodyElement, "SOURCEPOSITIONTYPE", sourcePositionType);
		JdomUtils.addElement(newBodyElement, "SOURCEPOSITIONNAME", sourcePositionName);
		JdomUtils.addElement(newBodyElement, "DESTINATIONPOSITIONTYPE", destinationPositionType);
		JdomUtils.addElement(newBodyElement, "DESTINATIONPOSITIONNAME", destinationPositionName);
		JdomUtils.addElement(newBodyElement, "TRANSPORTJOBLIST", null);

		return newBodyElement;
	}

	/* 
	 * 2020-12-08	dhko	Modify Table
	 * Find MaskStocker Zone
	 */
	@SuppressWarnings("unchecked")
	private String getZoneNameByMaskStockZone(String maskLotName, String machineName)
	{
		String sql = "SELECT DISTINCT SZ.STOCKERNAME, SZ.ZONENAME, SZ.ZONETYPE "
				   + "FROM CT_MASKSTOCKZONE SZ, "
				   + "( "
				   + "    SELECT L.MASKTYPE, L.MASKKIND, "
				   + "           CASE WHEN NVL(S.WEIGHTTYPE, 'Normal') = 'Heavy' THEN 'Heavy' "
				   + "                WHEN L.CLEANSTATE = 'Dirty' THEN 'Dirty' "
				   + "                ELSE 'Clean' "
				   + "           END AS ZONETYPE "
				   + "    FROM CT_MASKLOT L, CT_MASKSPEC S "
				   + "    WHERE 1 = 1 "
				   + "      AND L.MASKLOTNAME = :MASKLOTNAME "
				   + ") T "
				   + "WHERE 1 = 1 "
				   + "  AND SZ.STOCKERNAME = :STOCKERNAME "
				   + "  AND T.MASKTYPE = SZ.MASKTYPE "
				   + "  AND T.MASKKIND = SZ.MASKKIND "
				   + "  AND T.ZONETYPE = SZ.ZONETYPE ";
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("MASKLOTNAME", maskLotName);
		bindMap.put("STOCKERNAME", machineName);

		List<OrderedMap> resultDataList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		if (resultDataList == null || resultDataList.size() == 0)
		{
			return StringUtils.EMPTY;
		}
		
		Random random = new Random();
		int rnd = random.nextInt(resultDataList.size());
		
		return ConvertUtil.getMapValueByName(resultDataList.get(rnd), "ZONENAME");
	}
}
