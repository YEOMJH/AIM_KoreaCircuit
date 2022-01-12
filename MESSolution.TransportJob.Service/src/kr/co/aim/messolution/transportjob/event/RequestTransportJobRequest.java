package kr.co.aim.messolution.transportjob.event;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskTransfer;
import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.messolution.transportjob.service.TransportJobServiceUtil;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class RequestTransportJobRequest extends AsyncHandler {

	/**
	 * MessageSpec [OIC -> TEX -> MCS]
	 * 
	 * <Body>
	 *    <TRANSPORTJOBNAME />
	 *    <CARRIERNAME />
	 *    <SOURCEMACHINENAME />
	 *    <SOURCEPOSITIONTYPE />
	 *    <SOURCEPOSITIONNAME />
	 *    <SOURCEZONENAME />
	 *    <DESTINATIONMACHINENAME />
	 *    <DESTINATIONPOSITIONTYPE />
	 *    <DESTINATIONPOSITIONNAME />
	 *    <DESTINATIONZONENAME />
	 *    <MATERIALLIST>
	 *       <MATERIAL>
	 *          <MATERIALNAME />
	 *          <QUANTITY />
	 *       </MATERIAL>
	 *    </MATERIALLIST>
	 *    <PRIORITY />
	 *    <CARRIERSTATE />
	 *    <CARRIERTYPE />
	 *    <CLEANSTATE />
	 *    <LOTNAME />
	 *    <PRODUCTQUANTITY />
	 * </Body>	 */
		@Override
	public void doWorks(Document doc) throws CustomException
	{
		// Set Variables
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TransportRequest", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		Element body = SMessageUtil.getBodyElement(doc);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String sourceMachineName = SMessageUtil.getBodyItemValue(doc, "SOURCEMACHINENAME", false);
		String sourcePositionType = SMessageUtil.getBodyItemValue(doc, "SOURCEPOSITIONTYPE", false);
		String sourcePositionName = SMessageUtil.getBodyItemValue(doc, "SOURCEPOSITIONNAME", false);
		String sourceZoneName = SMessageUtil.getBodyItemValue(doc, "SOURCEZONENAME", false);
		String destinationMachineName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONMACHINENAME", false);
		String destinationPositionType = SMessageUtil.getBodyItemValue(doc, "DESTINATIONPOSITIONTYPE", false);
		String destinationPositionName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONPOSITIONNAME", false);
		String destinationZoneName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONZONENAME", false);
		String priority = SMessageUtil.getBodyItemValue(doc, "PRIORITY", false);
		String messageName = SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", true);
		String jobState = TransportJobServiceUtil.getJobState(messageName, doc);
		String cancelState = TransportJobServiceUtil.getCancelState(messageName, doc);
		String changeState = TransportJobServiceUtil.getChangeState(messageName, doc);
		String transferState = GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_RESERVED;
		String alternateFlag = "N";
		String carrierState = "";
		String transportJobName = "";
		String transportJobType = "";
		String transportType = "Manual";

		// Set TransportJobType
		if (StringUtils.equals(messageName, GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_TRANSPORTJOBSTARTEDBYMCS))
			transportJobType = GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_MCS;
		else if (StringUtils.isNotEmpty(SMessageUtil.getHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", false)) && eventInfo.getEventUser().startsWith("V"))
			transportJobType = GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_OIC;
		else
			transportJobType = GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_RTD;

		// Get TransportJobName
		try
		{
			transportJobName = MESTransportServiceProxy.getTransportJobServiceUtil().generateTransportJobIdBySender(carrierName, transportJobType);
		}
		catch (Exception ex)
		{
			eventLog.error(ex);
		}
		finally
		{
			eventLog.debug("generated Job ID=" + transportJobName);
		}

		try
		{
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
			Machine destMachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(destinationMachineName);

			if (StringUtils.equals("Y", durableData.getUdfs().get("TRANSPORTLOCKFLAG")))
				throw new CustomException("DURABLE-0019", carrierName);

			// CO-FA-0021-01
			// CF类型的CST不搬送到3CSTK02
			if (StringUtils.equals("FilmCST", durableData.getDurableType()) && 
				StringUtils.equals("3CSTK02", destinationMachineName))
			{
				throw new CustomException("DURABLE-3001", carrierName);
			}
			
			// Mask Cleaner
			String[] MaskMachineGroup = { GenericServiceProxy.getConstantMap().MachineGroup_MaskOrgCleaner, 
					  					  GenericServiceProxy.getConstantMap().MachineGroup_MaskMetalCleaner};

			// Mantis : 0000411
			// Dirty Mask CST 卡控不能搬送至Mask Cleaner Unload Port
			Port destPortData = MESPortServiceProxy.getPortInfoUtil().getPortData(destinationMachineName, destinationPositionName);
			if (CommonUtil.equalsIn(durableData.getDurableType(), "MaskCST", "EVAMaskCST", "TFEMaskCST") &&
				Arrays.asList(MaskMachineGroup).contains(destMachineData.getMachineGroupName()) &&
				StringUtils.equals(destinationPositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT))
			{
				
				if (StringUtils.equals(GenericServiceProxy.getConstantMap().PORT_TYPE_PU, destPortData.getUdfs().get("PORTTYPE")) &&
				    StringUtils.equals(GenericServiceProxy.getConstantMap().Dur_Dirty, durableData.getDurableCleanState()))
				{
					// Cassette[{0}] State is Dirty
					throw new CustomException("CST-0008", carrierName);
				}
			}
			
			if (StringUtils.equals(destinationPositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT)
					&&!(destMachineData.getMachineGroupName().equals(GenericServiceProxy.getConstantMap().MachineGroup_2ndLamination)
							||destMachineData.getMachineGroupName().equals(GenericServiceProxy.getConstantMap().MachineGroup_1stLamination)
							||(destMachineData.getFactoryName().equals("POSTCELL")&&destMachineData.getMachineGroupName().equals("STK")
									&&destMachineData.getSuperMachineName().isEmpty()&&destPortData.getUdfs().get("PORTKIND").equals("MGV"))))
				checkReserved(destinationMachineName, destinationPositionName);

			// Set CarrierType & CleanState
			String carrierType = MESTransportServiceProxy.getTransportJobServiceUtil().getCarrierType(durableData);
			setBodyItem(body, "CARRIERTYPE", carrierType);
			setBodyItem(body, "CLEANSTATE", MESTransportServiceProxy.getTransportJobServiceUtil().getCleanState(durableData));

			// Set MaterialList
			doc = this.setMaterialListItem(doc, carrierName, carrierType);
			
			// Set TransportType
			if (StringUtils.equals(transportJobType, GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_RTD))
				transportType = "Auto";

			// Durable - SetEvent
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("TRANSPORTLOCKFLAG", "Y");
			setEventInfo.getUdfs().put("TRANSPORTTYPE", transportType);
			
			DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);

			// Get Lot ID from CST ID
			String lotName = MESLotServiceProxy.getLotInfoUtil().getLotNameByCarrierName(carrierName);

			double productQuantity = 0;
			try
			{
				Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
				productQuantity = lotData.getProductQuantity();
			}
			catch (Exception e)
			{
			}

			// Set CarrierState
			if (StringUtils.isEmpty(lotName))
				carrierState = GenericServiceProxy.getConstantMap().MCS_CARRIERSTATE_EMPTY;
			else
				carrierState = GenericServiceProxy.getConstantMap().MCS_CARRIERSTATE_FULL;
			
			if(CommonUtil.equalsIn(durableData.getDurableType(), "MaskCST", "EVAMaskCST", "TFEMaskCST"))
			{				
				carrierState = SMessageUtil.getBodyItemValue(doc, "CARRIERSTATE", false);;
			}

			if (StringUtils.equals(sourcePositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_VEHICLE)
					|| StringUtils.equals(sourcePositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_CRANE))
			{
				if (StringUtils.isNotEmpty(sourcePositionName))
				{
					sourcePositionName = StringUtils.substring(sourcePositionName, StringUtils.indexOf(sourcePositionName, '_') + 1);

					setBodyItem(body, "SOURCEPOSITIONNAME", sourcePositionName);
				}
			}

			// Change Source TransferState
			if (StringUtils.equals(sourcePositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT))
			{
				MESMachineServiceProxy.getMachineInfoUtil().getMachineData(sourceMachineName);
				Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(sourceMachineName, sourcePositionName);

				MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
				makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReservedToUnload);
				makeTranferStateInfo.setValidateEventFlag("N");

				PortServiceProxy.getPortService().makeTransferState(portData.getKey(), eventInfo, makeTranferStateInfo);
			}

			// Change Destination TransferState
			if (StringUtils.equals(destinationPositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT))
			{
				MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(destinationMachineName);
				Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(destinationMachineName, destinationPositionName);

				// AR-TF-0009-01
				// Only one return order can be issued on the port, including reservation.
				/*if(!StringUtils.equals("StorageMachine", machineSpecData.getMachineType()) &&
				   !GenericServiceProxy.getConstantMap().Port_ReadyToLoad.equals(portData.getTransferState()))
				{
					// TransferState of port is [{0}]. MachineName={1}, PortName={2}
					throw new CustomException("PORT-3001", portData.getTransferState(), portData.getKey().getMachineName(), portData.getKey().getPortName());
				}
				*/
				MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
				makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReservedToLoad);
				makeTranferStateInfo.setValidateEventFlag("N");

				PortServiceProxy.getPortService().makeTransferState(portData.getKey(), eventInfo, makeTranferStateInfo);
			}
			
			// 20201222	dhko	Add Validate
			// Verify that the ReserveMaskTransfer information is present when the CST is returned to the MaskSTK.
			//if (StringUtils.equals(GenericServiceProxy.getConstantMap().MachineGroup_MaskStocker, destMachineData.getMachineGroupName()))
			{
				try
				{
					String condition = "WHERE CARRIERNAME = ? ";
					List<ReserveMaskTransfer> reserveDataList = ExtendedObjectProxy.getReserveMaskTransferService().select(condition, new Object[] {});
					if (reserveDataList != null && reserveDataList.size() > 0)
					{
						// NeedReserveMask (machineName[{0}]portName[{1}]carrierName[{2}])
						throw new CustomException("MASK-0009", destinationMachineName, destinationPositionName, carrierName);
					}
				}
				catch (Exception ex)
				{
					eventLog.error("Not Found CT_RESERVEMASKTRANSFER Data.");
				}
			}
			
			// Change destination information if EQP is '3MSTK03'
			doc = this.changeDestinationInfo(doc);
			destinationMachineName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONMACHINENAME", false);
			destinationPositionType = SMessageUtil.getBodyItemValue(doc, "DESTINATIONPOSITIONTYPE", false);
			destinationPositionName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONPOSITIONNAME", false);
			destinationZoneName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONZONENAME", false);
			
			TransportJobCommand transportJobCommandInfo = new TransportJobCommand();
			transportJobCommandInfo.setTransportJobName(transportJobName);
			transportJobCommandInfo.setCarrierName(carrierName);
			transportJobCommandInfo.setTransportJobType(transportJobType);
			transportJobCommandInfo.setJobState(jobState);
			transportJobCommandInfo.setCancelState(cancelState);
			transportJobCommandInfo.setChangeState(changeState);
			transportJobCommandInfo.setAlternateFlag(alternateFlag);
			transportJobCommandInfo.setTransferState(transferState);
			transportJobCommandInfo.setPriority(priority);
			transportJobCommandInfo.setSourceMachineName(sourceMachineName);
			transportJobCommandInfo.setSourcePositionType(sourcePositionType);
			transportJobCommandInfo.setSourcePositionName(sourcePositionName);
			transportJobCommandInfo.setSourceZoneName(sourceZoneName);
			transportJobCommandInfo.setDestinationMachineName(destinationMachineName);
			transportJobCommandInfo.setDestinationPositionType(destinationPositionType);
			transportJobCommandInfo.setDestinationPositionName(destinationPositionName);
			transportJobCommandInfo.setDestinationZoneName(destinationZoneName);
			transportJobCommandInfo.setCurrentMachineName(sourceMachineName);
			transportJobCommandInfo.setCurrentPositionType(sourcePositionType);
			transportJobCommandInfo.setCurrentPositionName(sourcePositionName);
			transportJobCommandInfo.setCurrentZoneName(sourceZoneName);
			transportJobCommandInfo.setCarrierState(carrierState);
			transportJobCommandInfo.setLotName(lotName);
			transportJobCommandInfo.setProductQuantity((long) productQuantity);
			transportJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceUtil().setTransportJobCommandEventInfo(transportJobCommandInfo, eventInfo);
			transportJobCommandInfo.setLastEventResultCode(SMessageUtil.getReturnItemValue(doc, "RETURNCODE", false));
			transportJobCommandInfo.setLastEventResultText(SMessageUtil.getReturnItemValue(doc, "RETURNMESSAGE", false));

			try
			{
				ExtendedObjectProxy.getTransportJobCommand().create(eventInfo, transportJobCommandInfo);

				setBodyItem(body, "TRANSPORTJOBNAME", transportJobName);
			}
			catch (Exception e)
			{
				throw new CustomException("JOB-8011", e.getMessage());
			}

			// Set OriginalSourceSubjectName for Transport Job from RTD to MCS
			if (transportJobType.equals(GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_RTD))
				SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("TEMsvr"));

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

		//return doc;
	}
	private Document setMaterialListItem(Document doc, String carrierName, String carrierType) throws CustomException
	{
		Element bodyElement = SMessageUtil.getBodyElement(doc);
		if (bodyElement.getChild("MATERIALLIST") != null)
		{
			return doc;
		}
		
		Element materialListElement = new Element("MATERIALLIST");
		
		if (!GenericServiceProxy.getConstantMap().DURABLETYPE_FilmBox.equals(carrierType))
		{
			bodyElement.addContent(materialListElement);
			return doc;
		}
		
		try
		{
			List<Consumable> filmDataList = MESConsumableServiceProxy.getConsumableInfoUtil().getFilmListByBoxDESC(carrierName);
			for (Consumable filmData : filmDataList) 
			{
				Element materialElement = new Element("MATERIAL");
				JdomUtils.addElement(materialElement, "MATERIALNAME", filmData.getKey().getConsumableName());
				JdomUtils.addElement(materialElement, "QUANTITY", String.valueOf(filmData.getQuantity()));
			
				materialListElement.addContent(materialElement);
			}
			
			bodyElement.addContent(materialListElement);
			return doc;
		}
		catch (Exception ex)
		{
			bodyElement.addContent(materialListElement);
			return doc;
		}
	}
	
	private void setBodyItem(Element body, String itemName, String itemValue)
	{
		if (body.getChild(itemName) == null)
			JdomUtils.addElement(body, itemName, itemValue);
		else
			body.getChild(itemName).setText(itemValue);
	}

	private void checkReserved(String destinationMachineName, String destinationPositionName) throws CustomException
	{
		StringBuilder sql = new StringBuilder();

		sql.append("SELECT A.TRANSPORTJOBNAME ");
		sql.append("  FROM CT_TRANSPORTJOBCOMMAND A, DURABLE D,MACHINE M ");
		sql.append(" WHERE A.JOBSTATE IN ('Started', 'Accepted', 'Requested') ");
		sql.append("   AND (A.CANCELSTATE IS NULL OR A.CANCELSTATE NOT IN ('Completed')) ");
		sql.append("   AND A.CARRIERNAME = D.DURABLENAME ");
		sql.append("   AND D.TRANSPORTLOCKFLAG = 'Y' ");
		sql.append("   AND A.LASTEVENTTIME BETWEEN SYSDATE - 1 AND SYSDATE ");
		sql.append("   AND A.DESTINATIONMACHINENAME = :MACHINENAME ");
		sql.append("   AND A.DESTINATIONPOSITIONNAME = :PORTNAME ");
		sql.append("   AND M.MACHINENAME=:MACHINENAME");
		sql.append("   AND M.MACHINEGROUPNAME!='conveyor'");

		Map<String, Object> args = new HashMap<>();

		args.put("MACHINENAME", destinationMachineName);
		args.put("PORTNAME", destinationPositionName);

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			throw new CustomException("DURABLE-0021", destinationMachineName, destinationPositionName, ConvertUtil.getMapValueByName(result.get(0), "TRANSPORTJOBNAME"));
		}
	}
	
	private Document changeDestinationInfo(Document doc) throws CustomException
	{
		Element bodyElement = SMessageUtil.getBodyElement(doc);
		String destinationMachineName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONMACHINENAME", false);
		String destinationPositionType = SMessageUtil.getBodyItemValue(doc, "DESTINATIONPOSITIONTYPE", false);
		String destinationPositionName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONPOSITIONNAME", false);
		
		if (StringUtils.equals("3MSTK03", destinationMachineName) &&
			StringUtils.equals("PORT", destinationPositionType) &&
			(StringUtils.equals("P01", destinationPositionName) || StringUtils.equals("P02", destinationPositionName)))
		{
			String newDestinationMachineName = "3MCTK02";
			String newDestinationPositionName = "";
			if (StringUtils.equals("P01", destinationPositionName))
			{
				newDestinationPositionName = "P02";
			}
			else if (StringUtils.equals("P02", destinationPositionName))
			{
				newDestinationPositionName = "P01";
			}
			
			// Check New Destination State
			Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(newDestinationMachineName, newDestinationPositionName);
			if (!GenericServiceProxy.getConstantMap().PORT_TYPE_PU.equals(portData.getUdfs().get("PORTTYPE")))
			{
				// PortType of port is [{0}]. MachineName={1}, PortName={2}
				throw new CustomException("PORT-3003", portData.getUdfs().get("PORTTYPE"), portData.getKey().getMachineName(), portData.getKey().getPortName());
			}
			if (!GenericServiceProxy.getConstantMap().Port_UP.equals(portData.getPortStateName()))
			{
				// PortStateName of port is [{0}]. MachineName={1}, PortName={2}
				throw new CustomException("PORT-3004", portData.getPortStateName(), portData.getKey().getMachineName(), portData.getKey().getPortName());
			}
			
			this.setBodyItem(bodyElement, "DESTINATIONMACHINENAME", newDestinationMachineName);
			this.setBodyItem(bodyElement, "DESTINATIONPOSITIONTYPE", "PORT");
			this.setBodyItem(bodyElement, "DESTINATIONPOSITIONNAME", newDestinationPositionName);
			this.setBodyItem(bodyElement, "DESTINATIONZONENAME", "");
		}
		
		return doc;
	}
}
