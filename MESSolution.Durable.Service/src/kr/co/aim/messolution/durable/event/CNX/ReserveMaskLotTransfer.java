package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskTransfer;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class ReserveMaskLotTransfer extends SyncHandler {

	private static Log log = LogFactory.getLog(ReserveMaskLotTransfer.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		List<Element> slotList = SMessageUtil.getBodySequenceItemList(doc, "SLOTLIST", true);
		String strMCSTransfer = SMessageUtil.getBodyItemValue(doc, "MCSTRANSFER", false);
		boolean mcsTransfer = StringUtils.isEmpty(strMCSTransfer) ? false : Boolean.parseBoolean(strMCSTransfer);
		String currentMachineName = SMessageUtil.getBodyItemValue(doc, "CURRENTMACHINENAME", false);
		String currentPortName = SMessageUtil.getBodyItemValue(doc, "CURRENTPORTNAME", false);
		List<String> maskList = new ArrayList<String>();

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReserveMaskLotTransfer", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());
		// Get MaskCST Data
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

		for (Element eleSlot : slotList)
		{
			String position = SMessageUtil.getChildText(eleSlot, "POSITION", true);
			String maskLotName = SMessageUtil.getChildText(eleSlot, "MASKLOTNAME", false);

			if (StringUtils.isNotEmpty(maskLotName))
				maskList.add(maskLotName);

			ReserveMaskTransfer originalReserved = null;
			try
			{
				originalReserved = ExtendedObjectProxy.getReserveMaskTransferService().selectByKey(true, new Object[] { carrierName, position });
			}
			catch (greenFrameDBErrorSignal nfdes)
			{
			} // Not found reserved same stage

			if (originalReserved != null) // The stage is already reserved
			{
				originalReserved.setMaskLotName(maskLotName);
				originalReserved.setMachineName(machineName);
				originalReserved.setLastEventName(eventInfo.getEventName());
				originalReserved.setLastEventTime(eventInfo.getEventTime());
				originalReserved.setLastEventTimeKey(eventInfo.getLastEventTimekey());
				originalReserved.setLastEventUser(getEventUser());
				originalReserved.setLastEventComment(getEventComment());

				ExtendedObjectProxy.getReserveMaskTransferService().modify(eventInfo, originalReserved);
			}
			else
			// The stage is already not reserved
			{
				ReserveMaskTransfer newReserved = new ReserveMaskTransfer();
				newReserved.setCarrierName(carrierName);
				newReserved.setPosition(position);
				newReserved.setMaskLotName(maskLotName);
				newReserved.setMachineName(machineName);
				newReserved.setLastEventName(eventInfo.getEventName());
				newReserved.setLastEventTime(eventInfo.getEventTime());
				newReserved.setLastEventTimeKey(eventInfo.getLastEventTimekey());
				newReserved.setLastEventUser(getEventUser());
				newReserved.setLastEventComment(getEventComment());

				ExtendedObjectProxy.getReserveMaskTransferService().create(eventInfo, newReserved);
			}
		}
		
		if(slotList.size()>0)
		{
			this.addDurableHistory("ReserveMaskLotTransfer", durableData);			
		}

		if (mcsTransfer)
		{			
			if (durableData.getUdfs().get("DURABLEHOLDSTATE").equals("Y"))
				//DURABLE-0007:Check DurableHoldState !!
				throw new CustomException("DURABLE-0007");

			if (!CommonUtil.equalsIn(durableData.getDurableType(), "EVAMaskCST", "TFEMaskCST", "MaskCST"))
				throw new CustomException("MASK-0054", durableData.getDurableType());

			if (StringUtils.equals("Y", durableData.getUdfs().get("TRANSPORTLOCKFLAG")))
				throw new CustomException("MASK-0055", carrierName);

			MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(currentMachineName);

			if (!StringUtils.equals(machineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_StorageMachine))
				throw new CustomException("MASK-0056", machineSpecData.getMachineType());

			currentMachineName = durableData.getUdfs().get("MACHINENAME");

			Map<String, Object> resultMap = isOnStocker(maskList);
			boolean isOnStocker = (boolean) resultMap.get("isOnStocker");
			String machineNameByMask = (String) resultMap.get("machineNameByMask");
			String messageName = "";

			if (StringUtils.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_Available))
			{
				// Mask Transfer from Stocker to CST
				checkReservedMask(carrierName);
				
				if (isOnStocker && StringUtils.indexOf(currentMachineName, "3MSTK") > -1)
				{
					messageName = "RequestMaskBatchJobRequest";
				}
				else
				{
					if (StringUtils.indexOf(machineNameByMask, "3MSTK") > -1)
					{
						messageName = "RequestTransportJobRequest";
					}
					else
					{
						throw new CustomException("MASK-0057", machineNameByMask);
					}
				}
			}
			else
			{
				throw new CustomException("MASK-0058", durableData.getDurableState());
			}

			String destPortName = getDestPortName(machineNameByMask);
			CommonValidation.checkIsOrNotTransferJob(machineNameByMask, destPortName);

			if (StringUtils.isNotEmpty(messageName))
			{
				Element bodyElement = new Element(SMessageUtil.Body_Tag);

				if (StringUtils.equals(messageName, "RequestMaskBatchJobRequest"))
				{
					Element attMachineName = new Element("MACHINENAME");
					attMachineName.setText(currentMachineName);
					bodyElement.addContent(attMachineName);

					Element attPortName = new Element("PORTNAME");
					attPortName.setText(currentPortName);
					bodyElement.addContent(attPortName);

					Element attCarrierName = new Element("CARRIERNAME");
					attCarrierName.setText(carrierName);
					bodyElement.addContent(attCarrierName);
				}
				else
				{

					if (StringUtils.isNotEmpty(destPortName))
						bodyElement = requestTransportJobRequestBody(SMessageUtil.getBodyElement(doc), durableData, machineNameByMask, destPortName);
					else
						throw new CustomException("MACHINE-0027");
				}

				try
				{
					String originalSourceSubjectName = getOriginalSourceSubjectName();

					Document docMCSTransfer = SMessageUtil.createXmlDocument(bodyElement, messageName, originalSourceSubjectName, "TEX", eventInfo.getEventUser(), eventInfo.getEventComment());

					String targetSubjectName = GenericServiceProxy.getESBServive().getSendSubject("TEMsvr");
					GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, docMCSTransfer, "TEMSender");
				}
				catch (Exception e)
				{
					throw new CustomException("OLEDMASK-0003");
				}
			}
		}

		return doc;
	}

	private Map<String, Object> isOnStocker(List<String> maskList) throws CustomException
	{
		log.info("isOnStocker start");

		Map<String, Object> resultMap = new HashMap<String, Object>();

		boolean isOnStocker = false;
		String machineNameByMask = "";

		if (maskList.size() > 0)
		{
			String sql = "SELECT DISTINCT MACHINENAME FROM CT_MASKLOT WHERE MASKLOTNAME IN ( :MASKLIST ) ";

			Map<String, Object> args = new HashMap<String, Object>();
			args.put("MASKLIST", maskList);

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

			if (result.size() == 1)
			{
				machineNameByMask = ConvertUtil.getMapValueByName(result.get(0), "MACHINENAME");

				if (StringUtils.indexOf(machineNameByMask, "3MSTK") > -1)
				{
					isOnStocker = true;
				}
			}
		}

		resultMap.put("isOnStocker", isOnStocker);
		resultMap.put("machineNameByMask", machineNameByMask);

		log.info("isOnStocker =" + isOnStocker);
		log.info("machineNameByMask =" + machineNameByMask);
		log.info("isOnStocker end");

		return resultMap;
	}

	private String getDestPortName(String destMachineName) throws CustomException
	{
		String destPositionName = "";
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT P.PORTNAME, P.ACCESSMODE ");
		sql.append("    FROM PORT P ");
		sql.append("   WHERE     P.MACHINENAME = :MACHINENAME ");
		sql.append("          AND (P.PORTNAME LIKE 'P%' OR P.PORTNAME LIKE 'BP%') ");
		sql.append("           AND P.PORTSTATENAME = 'UP' ");
		sql.append("           AND P.TRANSFERSTATE = 'ReadyToLoad' ");
		sql.append("           AND P.PORTNAME NOT IN ( ");
		sql.append("         SELECT A.DESTINATIONPOSITIONNAME ");
		sql.append("            FROM CT_TRANSPORTJOBCOMMAND A, DURABLE D ");
		sql.append("          WHERE     A.JOBSTATE IN ('Started', 'Accepted', 'Requested') ");
		sql.append("                AND (   A.CANCELSTATE IS NULL OR A.CANCELSTATE NOT IN ('Completed')) ");
		sql.append("                AND A.CARRIERNAME = D.DURABLENAME ");
		sql.append("                AND D.TRANSPORTLOCKFLAG = 'Y' ");
		sql.append("                AND A.DESTINATIONPOSITIONNAME IS NOT NULL ");
		sql.append("                AND A.LASTEVENTTIME BETWEEN SYSDATE - 1 AND SYSDATE ");
		sql.append("                 AND A.DESTINATIONMACHINENAME = :MACHINENAME) ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("MACHINENAME", destMachineName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			int j = 0;
			Random random = new Random();

			for (int i = 0; i < 4; i++)
			{
				j = random.nextInt(result.size());
			}

			log.info("j=" + j);

			destPositionName = ConvertUtil.getMapValueByName(result.get(j), "PORTNAME");

		}
		else
		{
			throw new CustomException("MASK-0060", destMachineName);
		}

		log.info("destPositionName =" + destPositionName);
		return destPositionName;

	}

	private Element requestTransportJobRequestBody(Element originalBodyElement, Durable durableData, String destMachineName, String destPortName) throws CustomException
	{
		String sourcePositionType = durableData.getUdfs().get("POSITIONTYPE");
		String sourcePositionName = "";

		// set BODY Element
		Element bodyElement = new Element(SMessageUtil.Body_Tag);

		Element jobNameElement = new Element("TRANSPORTJOBNAME");
		jobNameElement.setText("");
		bodyElement.addContent(jobNameElement);

		Element carrierNameElement = new Element("CARRIERNAME");
		carrierNameElement.setText(durableData.getKey().getDurableName());
		bodyElement.addContent(carrierNameElement);

		Element sourceMachineNameElement = new Element("SOURCEMACHINENAME");
		sourceMachineNameElement.setText(durableData.getUdfs().get("MACHINENAME"));
		bodyElement.addContent(sourceMachineNameElement);

		Element sourcePositionTypeElement = new Element("SOURCEPOSITIONTYPE");
		sourcePositionTypeElement.setText(sourcePositionType);
		bodyElement.addContent(sourcePositionTypeElement);

		if (StringUtils.equals(sourcePositionType, "PORT"))
			sourcePositionName = durableData.getUdfs().get("PORTNAME");
		else
			sourcePositionName = durableData.getUdfs().get("POSITIONNAME");

		Element sourcePositionNameElement = new Element("SOURCEPOSITIONNAME");
		sourcePositionNameElement.setText(sourcePositionName);
		bodyElement.addContent(sourcePositionNameElement);

		Element soruceZoneNameElement = new Element("SOURCEZONENAME");
		soruceZoneNameElement.setText(durableData.getUdfs().get("ZONENAME"));
		bodyElement.addContent(soruceZoneNameElement);

		Element destMachineNameElement = new Element("DESTINATIONMACHINENAME");
		destMachineNameElement.setText(destMachineName);
		bodyElement.addContent(destMachineNameElement);

		Element destPositionTypeElement = new Element("DESTINATIONPOSITIONTYPE");
		destPositionTypeElement.setText("PORT");
		bodyElement.addContent(destPositionTypeElement);

		Element destPositionNameElement = new Element("DESTINATIONPOSITIONNAME");
		destPositionNameElement.setText(destPortName);
		bodyElement.addContent(destPositionNameElement);

		Element destZoneNameElement = new Element("DESTINATIONZONENAME");
		destZoneNameElement.setText("");
		bodyElement.addContent(destZoneNameElement);

		Element priorityElement = new Element("PRIORITY");
		priorityElement.setText("50");
		bodyElement.addContent(priorityElement);

		String carrierState = "";
		if (durableData.getDurableState().equals("Available"))
			carrierState = GenericServiceProxy.getConstantMap().MCS_CARRIERSTATE_EMPTY;
		else if(durableData.getDurableState().equals("InUse"))
			carrierState = GenericServiceProxy.getConstantMap().MCS_CARRIERSTATE_FULL;
		Element carrierStateElement = new Element("CARRIERSTATE");
		carrierStateElement.setText(carrierState);
		bodyElement.addContent(carrierStateElement);

		String carrierType = MESTransportServiceProxy.getTransportJobServiceUtil().getCarrierType(durableData);
		Element carrierTypeElement = new Element("CARRIERTYPE");
		carrierTypeElement.setText(carrierType);
		bodyElement.addContent(carrierTypeElement);

		Element cleanStateElement = new Element("CLEANSTATE");
		cleanStateElement.setText(MESTransportServiceProxy.getTransportJobServiceUtil().getCleanState(durableData));
		bodyElement.addContent(cleanStateElement);

		Element lotNameElement = new Element("LOTNAME");
		lotNameElement.setText("");
		bodyElement.addContent(lotNameElement);

		Element productQuantityElement = new Element("PRODUCTQUANTITY");
		productQuantityElement.setText("0");
		bodyElement.addContent(productQuantityElement);

		return bodyElement;
	}

	private void checkReservedMask(String carrierName) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT MACHINENAME, MASKLOTNAME, POSITION ");
		sql.append("  FROM CT_RESERVEMASKTRANSFER ");
		sql.append(" WHERE CARRIERNAME = :CARRIERNAME ");
		sql.append("   AND MASKLOTNAME IS NOT NULL ");
		sql.append("ORDER BY POSITION ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("CARRIERNAME", carrierName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() == 0)
			throw new CustomException("MASK-0059", carrierName);
	}
	
	private void addDurableHistory(String eventName,Durable durableData)throws CustomException
	{
		EventInfo eventInfo = new EventInfo();
		eventInfo.setEventName(eventName);
		eventInfo.setEventUser(this.getEventUser());
		eventInfo.setEventComment(this.getEventComment());
		// Durable - SetEvent
		SetEventInfo setEventInfo = new SetEventInfo();

		DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
	}
}