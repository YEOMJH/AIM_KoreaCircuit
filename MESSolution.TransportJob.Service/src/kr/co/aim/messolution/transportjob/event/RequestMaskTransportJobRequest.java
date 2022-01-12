package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.messolution.transportjob.service.TransportJobServiceUtil;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class RequestMaskTransportJobRequest extends AsyncHandler {

	/**
	 * MessageSpec [OIC -> TEX -> MCS]
	 * 
	 * <Body>
	 *    <TRANSPORTJOBNAME />
	 *    <MASKNAME />
	 *    <SOURCEMACHINENAME />
	 *    <SOURCEPOSITIONTYPE />
	 *    <SOURCEPOSITIONNAME />
	 *    <SOURCEZONENAME />
	 *    <SOURCECARRIERNAME />
	 *    <SOURCECARRIERSLOTNO />
	 *    <DESTINATIONMACHINENAME />
	 *    <DESTINATIONPOSITIONTYPE />
	 *    <DESTINATIONPOSITIONNAME />
	 *    <DESTINATIONZONENAME />
	 *    <DESTINATIONCARRIERNAME />
	 *    <DESTINATIONCARRIERSLOTNO />
	 *    <PRIORITY />
	 *    <MASKTYPE />
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		// Set Variables
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TransportRequest", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", false);
		String sourceMachineName = SMessageUtil.getBodyItemValue(doc, "SOURCEMACHINENAME", false);
		String sourcePositionType = SMessageUtil.getBodyItemValue(doc, "SOURCEPOSITIONTYPE", false);
		String sourcePositionName = SMessageUtil.getBodyItemValue(doc, "SOURCEPOSITIONNAME", false);
		String sourceZoneName = SMessageUtil.getBodyItemValue(doc, "SOURCEZONENAME", false);
		String destinationMachineName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONMACHINENAME", false);
		String destinationPositionType = SMessageUtil.getBodyItemValue(doc, "DESTINATIONPOSITIONTYPE", false);
		String destinationPositionName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONPOSITIONNAME", false);
		String destinationZoneName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONZONENAME", false);
		String priority = SMessageUtil.getBodyItemValue(doc, "PRIORITY", false);
		String maskType = SMessageUtil.getBodyItemValue(doc, "MASKTYPE", false);
		String messageName = SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", true);
		String jobState = TransportJobServiceUtil.getJobState(messageName, doc);
		String cancelState = TransportJobServiceUtil.getCancelState(messageName, doc);
		String changeState = TransportJobServiceUtil.getChangeState(messageName, doc);
		String transferState = GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_RESERVED;
		String alternateFlag = "N";
		String transportJobName = "";
		String transportJobType = "";
		
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

		// Set TransportJobType
		if (StringUtils.equals(messageName, GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_TRANSPORTJOBSTARTEDBYMCS))
		{
			transportJobType = GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_MCS;
		}
		else if (StringUtils.isNotEmpty(SMessageUtil.getHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", false)))
		{
			transportJobType = GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_OIC;
		}
		else
		{
			transportJobType = GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_RTD;
			//由于PPA设备需要Aging，优先派送搬往Mask PPA设备的搬送Job，故将优先级调至81
			if(StringUtils.isNotEmpty(destinationMachineName))
			{
				Machine destMachineData =MESMachineServiceProxy.getMachineInfoUtil().getMachineData(destinationMachineName);
				if(StringUtils.equals(destMachineData.getMachineGroupName(),GenericServiceProxy.getConstantMap().MachineGroup_MaskPPA))
				{
					priority = "81";
				}
			}
		}

		try
		{
			MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskName });
			if (StringUtils.equals("MOVING", maskLotData.getTransferState()))
			{
				// This masklot is moving. MaskLot=[{0}]
				throw new CustomException("MASKLOT-0001", maskName);
			}
			
			// Mantis : 0000412
			// Mask CST搬送功能起始地与实际不符时，MCS不执行动作
			if (StringUtils.isEmpty(sourceMachineName) ||
				!StringUtils.equals(sourceMachineName, maskLotData.getMachineName()))
			{
				// SourceMachine information in Message and currentMachine information in MaskLot do not match.
				// SourceMachine=[{0}], CurrentMachine=[{1}]
				throw new CustomException("MASKLOT-3001", sourceMachineName, maskLotData.getMachineName());
			}
			else if (StringUtils.equals(GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_SHELF, sourcePositionType) &&
					 (StringUtils.isEmpty(sourceZoneName) || 
					  !StringUtils.equals(maskLotData.getZoneName(), sourceZoneName)))
			{
				// SourcePosition information in Message and currentZone information in MaskLot do not match.
				// SourceMachine=[{0}], CurrentMachine=[{1}]
				throw new CustomException("MASKLOT-3002", sourceZoneName, maskLotData.getZoneName());
			}
			else if (StringUtils.equals(GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT, sourcePositionType) &&
					(StringUtils.isEmpty(sourcePositionName) || 
					 !StringUtils.equals(maskLotData.getPortName(), sourcePositionName)))
			{
				// SourcePosition information in Message and currentPort information in MaskLot do not match.
				// SourceMachine=[{0}], CurrentMachine=[{1}]
				throw new CustomException("MASKLOT-3003", sourcePositionName, maskLotData.getPortName());
			}
			
			// Set MaskType
			SMessageUtil.setItemValue(doc, "Body", "MASKTYPE", StringUtils.isNotEmpty(maskType) ? maskType : maskLotData.getMaskType());
			
			// Set Priority
			SMessageUtil.setItemValue(doc, "Body", "PRIORITY", priority);

			// Set TransferState
			ExtendedObjectProxy.getMaskLotService().setTransferState(eventInfo, maskLotData, GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_MOVING);
			
			TransportJobCommand transportJobCommandInfo = new TransportJobCommand();
			transportJobCommandInfo.setTransportJobName(transportJobName);
			transportJobCommandInfo.setCarrierName("");
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
			transportJobCommandInfo.setCarrierState(GenericServiceProxy.getConstantMap().MCS_CARRIERSTATE_EMPTY);
			transportJobCommandInfo.setLotName(maskName);
			transportJobCommandInfo.setProductQuantity(1);
			transportJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceUtil().setTransportJobCommandEventInfo(transportJobCommandInfo, eventInfo);
			transportJobCommandInfo.setLastEventResultCode(SMessageUtil.getReturnItemValue(doc, "RETURNCODE", false));
			transportJobCommandInfo.setLastEventResultText(SMessageUtil.getReturnItemValue(doc, "RETURNMESSAGE", false));

			try
			{
				ExtendedObjectProxy.getTransportJobCommand().create(eventInfo, transportJobCommandInfo);
				
				SMessageUtil.setItemValue(doc, "Body", "TRANSPORTJOBNAME", transportJobName);
			}
			catch (Exception e)
			{
				throw new CustomException("JOB-8011", e.getMessage());
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
				MESMachineServiceProxy.getMachineInfoUtil().getMachineData(destinationMachineName);
				Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(destinationMachineName, destinationPositionName);

				MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
				makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReservedToLoad);
				makeTranferStateInfo.setValidateEventFlag("N");

				PortServiceProxy.getPortService().makeTransferState(portData.getKey(), eventInfo, makeTranferStateInfo);
			}

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
	}
}
