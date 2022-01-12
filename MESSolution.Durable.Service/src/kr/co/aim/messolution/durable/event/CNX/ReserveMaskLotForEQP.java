package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskToEQP;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ReserveMaskLotForEQP extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);		
		List<Element> slotList = SMessageUtil.getBodySequenceItemList(doc, "SLOTLIST", true);
		String strMCSTransfer = SMessageUtil.getBodyItemValue(doc, "MCSTRANSFER", false);
		boolean mcsTransfer = StringUtil.isEmpty(strMCSTransfer) ? false : Boolean.parseBoolean(strMCSTransfer);
		String currentMachineName = SMessageUtil.getBodyItemValue(doc, "CURRENTMACHINENAME", true);
		String currentPortName = SMessageUtil.getBodyItemValue(doc, "CURRENTPORTNAME", false);
		String updateFlag = SMessageUtil.getBodyItemValue(doc, "UPDATEFLAG", false);
		List<String> maskList = new ArrayList<String>();

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReserveForEQP", this.getEventUser(), this.getEventComment(), "", "");
		String timeKey = TimeUtils.getCurrentEventTimeKey();
		eventInfo.setEventTimeKey(timeKey);
		eventInfo.setLastEventTimekey(timeKey);
		// Get MaskCST Data
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

		if(updateFlag.equals("Add"))
		{
			for (Element eleSlot : slotList)
			{
				String machineName = SMessageUtil.getChildText(eleSlot, "MACHINENAME", false);
				String unitName = SMessageUtil.getChildText(eleSlot, "UNITNAME", false);
				String msName = SMessageUtil.getChildText(eleSlot, "MSNAME", false);
				String subUnitName = SMessageUtil.getChildText(eleSlot, "SUBUNITNAME", false);
				String portName = SMessageUtil.getChildText(eleSlot, "PORTNAME", false);
				String position = SMessageUtil.getChildText(eleSlot, "POSITION", false);
				String maskLotName = SMessageUtil.getChildText(eleSlot, "MASKLOTNAME", false);
				String lineType = SMessageUtil.getChildText(eleSlot, "LINETYPE", false);
				String maskGroupName = SMessageUtil.getChildText(eleSlot, "MASKGROUPNAME", false);

				if (StringUtils.isNotEmpty(maskLotName))
					maskList.add(maskLotName);

				ReserveMaskToEQP originalReserved = null;
				try
				{
					originalReserved = ExtendedObjectProxy.getReserveMaskToEQPService().selectByKey(true, new Object[] { machineName, unitName, subUnitName, carrierName, position });
				}
				catch (greenFrameDBErrorSignal nfdes)
				{
				} // Not found reserved same stage

				if (originalReserved != null) // The stage is already reserved
				{
					// String originalPosition = originalReserved.getPosition();
					String originalMaskLotName = originalReserved.getMaskLotName();

					if (StringUtil.isNotEmpty(originalMaskLotName) && StringUtil.isNotEmpty(maskLotName) && !StringUtil.equals(originalMaskLotName, maskLotName))
						throw new CustomException("MASK-0019", maskLotName);

					originalReserved.setMaskLotName(maskLotName);
					originalReserved.setMsName(msName);
					originalReserved.setPortName(portName);
					originalReserved.setLineType(lineType);
					originalReserved.setMaskGroupName(maskGroupName);
					originalReserved.setLastEventName(eventInfo.getEventName());
					originalReserved.setLastEventTime(eventInfo.getEventTime());
					originalReserved.setLastEventTimekey(eventInfo.getLastEventTimekey());
					originalReserved.setLastEventUser(getEventUser());
					originalReserved.setLastEventComment(getEventComment());

					ExtendedObjectProxy.getReserveMaskToEQPService().modify(eventInfo, originalReserved);
				}
				else
				// The stage is already not reserved
				{
					ReserveMaskToEQP newReserved = new ReserveMaskToEQP();
					newReserved.setMachineName(machineName);
					newReserved.setUnitName(unitName);
					newReserved.setSubUnitName(subUnitName);
					newReserved.setCarrierName(carrierName);
					newReserved.setPosition(position);
					newReserved.setMaskLotName(maskLotName);
					newReserved.setMsName(msName);
					newReserved.setPortName(portName);
					newReserved.setLineType(lineType);
					newReserved.setMaskGroupName(maskGroupName);
					newReserved.setLastEventName(eventInfo.getEventName());
					newReserved.setLastEventTime(eventInfo.getEventTime());
					newReserved.setLastEventTimekey(eventInfo.getLastEventTimekey());
					newReserved.setLastEventUser(getEventUser());
					newReserved.setLastEventComment(getEventComment());

					ExtendedObjectProxy.getReserveMaskToEQPService().create(eventInfo, newReserved);
				}

				if (StringUtils.isEmpty(maskLotName))
					continue;

				MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskLotName });

				ProcessFlowKey processFlowKey = new ProcessFlowKey();
				processFlowKey.setFactoryName(maskLotData.getFactoryName());
				processFlowKey.setProcessFlowName(maskLotData.getMaskProcessFlowName());
				processFlowKey.setProcessFlowVersion(maskLotData.getMaskProcessFlowVersion());
				ProcessFlow processFlow = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
				String detailProcessFlowType = processFlow.getUdfs().get("DETAILPROCESSFLOWTYPE");

				ProcessOperationSpec processOperationData = CommonUtil.getProcessOperationSpec(maskLotData.getFactoryName(), maskLotData.getMaskProcessOperationName(),
						maskLotData.getMaskProcessOperationVersion());
				String detailProcessOperationType = processOperationData.getDetailProcessOperationType();

//				if (!StringUtil.equalsIgnoreCase(detailProcessFlowType, "Eva") && !detailProcessOperationType.toUpperCase().startsWith("EVA"))
//				{
//					throw new CustomException("MASK-0053", maskLotName, maskLotData.getMaskProcessFlowName(), maskLotData.getMaskProcessOperationName());
//				}
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
					
					checkReserveMaskToEQP(carrierName);
					
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
						String destPortName = getDestPortName(machineNameByMask);

						if (StringUtils.isNotEmpty(destPortName))
						{
							bodyElement = requestTransportJobRequestBody(SMessageUtil.getBodyElement(doc), durableData, machineNameByMask, destPortName);
						}
						else
						{
							throw new CustomException("MACHINE-0027");
						}
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
			if(slotList.size()>0)
			{
				this.addDurableHistory("ReserveMaskByMaskGroup", durableData);
			}
		}
		else if(updateFlag.equals("Delete"))
		{
			eventInfo.setEventName("CancelReserveByMaskGroup");
			for (Element eleSlot : slotList)
			{
				String machineName = SMessageUtil.getChildText(eleSlot, "MACHINENAME", false);
				String unitName = SMessageUtil.getChildText(eleSlot, "UNITNAME", false);				
				String subUnitName = SMessageUtil.getChildText(eleSlot, "SUBUNITNAME", false);				
				String position = SMessageUtil.getChildText(eleSlot, "POSITION", false);	
				
				ReserveMaskToEQP originalReserved = null;
				try
				{
					originalReserved = ExtendedObjectProxy.getReserveMaskToEQPService().selectByKey(true, new Object[] { machineName, unitName, subUnitName, carrierName, position });
				}
				catch (greenFrameDBErrorSignal nfdes)
				{
				} // Not found reserved same stage

				if (originalReserved != null) // The stage is already reserved
				{
					ExtendedObjectProxy.getReserveMaskToEQPService().remove(eventInfo, originalReserved);					
				}
			}
			if(slotList.size()>0)
			{
				this.addDurableHistory("CancelReserveByMaskGroup", durableData);
			}
		}

		return doc;
	}

	private Map<String, Object> isOnStocker(List<String> maskList) throws CustomException
	{
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
					isOnStocker = true;
			}
		}

		resultMap.put("isOnStocker", isOnStocker);
		resultMap.put("machineNameByMask", machineNameByMask);

		return resultMap;
	}

	private String getDestPortName(String destMachineName)
	{
		String destPositionName = "";
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT PORTNAME, ACCESSMODE ");
		sql.append("  FROM PORT ");
		sql.append(" WHERE MACHINENAME = :MACHINENAME ");
		sql.append("   AND PORTNAME LIKE 'P%' ");
		sql.append("   AND PORTSTATENAME = 'UP' ");
		sql.append("   AND TRANSFERSTATE = 'ReadyToLoad' ");
		sql.append("ORDER BY LASTEVENTTIME ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("MACHINENAME", destMachineName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
			destPositionName = ConvertUtil.getMapValueByName(result.get(0), "PORTNAME");

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

		if (StringUtil.equals(sourcePositionType, "PORT"))
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

		Element carrierTypeElement = new Element("CARRIERTYPE");
		carrierTypeElement.setText(MESTransportServiceProxy.getTransportJobServiceUtil().getCarrierType(durableData));
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

	private void checkReserveMaskToEQP(String carrierName) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT MACHINENAME, MASKLOTNAME, POSITION ");
		sql.append("  FROM CT_RESERVEMASKTOEQP ");
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