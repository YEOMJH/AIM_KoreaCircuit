package kr.co.aim.messolution.transportjob.event;

import java.util.Arrays;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.messolution.transportjob.service.TransportJobServiceUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.PortKey;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class MaskTransportJobStartedByMCS extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX]
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
	 *    <CURRENTMACHINENAME />
	 *    <CURRENTPOSITIONTYPE />
	 *    <CURRENTPOSITIONNAME />
	 *    <CURRENTZONENAME />
	 *    <CURRENTCARRIERNAME />
	 *    <CURRENTCARRIERSLOTNO />
	 *    <DESTINATIONMACHINENAME />
	 *    <DESTINATIONPOSITIONTYPE />
	 *    <DESTINATIONPOSITIONNAME />
	 *    <DESTINATIONZONENAME />
	 *    <DESTINATIONCARRIERNAME />
	 *    <DESTINATIONCARRIERSLOTNO />
	 *    <PRIORITY />
	 *    <MASKTYPE />
	 *    <TRANSFERSTATE />
	 *    <ALTERNATEFLAG />
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TransportStart", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		// Validation : Exist Carrier
		String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);
		String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String sourceMachineName = SMessageUtil.getBodyItemValue(doc, "SOURCEMACHINENAME", false);
		String sourcePositionType = SMessageUtil.getBodyItemValue(doc, "SOURCEPOSITIONTYPE", false);
		String sourcePositionName = SMessageUtil.getBodyItemValue(doc, "SOURCEPOSITIONNAME", false);
		String sourceZoneName = SMessageUtil.getBodyItemValue(doc, "SOURCEZONENAME", false);
		String sourceCarrierName = SMessageUtil.getBodyItemValue(doc, "SOURCECARRIERNAME", false);
		String sourceCarrierSlotNo = SMessageUtil.getBodyItemValue(doc, "SOURCECARRIERSLOTNO", false);
		String currentMachineName = SMessageUtil.getBodyItemValue(doc, "CURRENTMACHINENAME", false);
		String currentPositionType = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONTYPE", false);
		String currentPositionName = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONNAME", false);
		String currentZoneName = SMessageUtil.getBodyItemValue(doc, "CURRENTZONENAME", false);
		String currentCarrierName = SMessageUtil.getBodyItemValue(doc, "CURRENTCARRIERNAME", false);
		String currentCarrierSlotNo = SMessageUtil.getBodyItemValue(doc, "CURRENTCARRIERSLOTNO", false);
		String destinationMachineName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONMACHINENAME", false);
		String destinationPositionType = SMessageUtil.getBodyItemValue(doc, "DESTINATIONPOSITIONTYPE", false);
		String destinationPositionName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONPOSITIONNAME", false);
		String destinationZoneName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONZONENAME", false);
		String destinationCarrierName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONCARRIERNAME", false);
		String destinationCarrierSlotNo = SMessageUtil.getBodyItemValue(doc, "DESTINATIONCARRIERSLOTNO", false);
		String priority = SMessageUtil.getBodyItemValue(doc, "PRIORITY", false);
		String transferState = SMessageUtil.getBodyItemValue(doc, "TRANSFERSTATE", false);
		String returnCode = SMessageUtil.getReturnItemValue(doc, "RETURNCODE", true);
		String returnMessage = SMessageUtil.getReturnItemValue(doc, "RETURNMESSAGE", false);
		String messageName = SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", true);
		String jobState = TransportJobServiceUtil.getJobState(messageName, doc);
		String cancelState = TransportJobServiceUtil.getCancelState(messageName, doc);
		String changeState = TransportJobServiceUtil.getChangeState(messageName, doc);
		String alternateFlag = "N";
		

		MaskLot maskData = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskName });
		MESMachineServiceProxy.getMachineInfoUtil().getMachineData(destinationMachineName);

		// Set CarrierSlotNo
		sourceCarrierSlotNo = ConvertUtil.toStringForIntTypeValue(sourceCarrierSlotNo);
		currentCarrierSlotNo = ConvertUtil.toStringForIntTypeValue(currentCarrierSlotNo);
		destinationCarrierSlotNo = ConvertUtil.toStringForIntTypeValue(destinationCarrierSlotNo);

		// Update Carrier TransportType

		String transportJobType = GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_MCS;
		String carrierState = GenericServiceProxy.getConstantMap().MCS_CARRIERSTATE_FULL;

		TransportJobCommand transportJobCommandInfo = new TransportJobCommand();
		transportJobCommandInfo.setTransportJobName(transportJobName);
		transportJobCommandInfo.setCarrierName(maskName);
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
		transportJobCommandInfo.setSourceCarrierName(sourceCarrierName);
		transportJobCommandInfo.setSourceCarrierSlotNo(sourceCarrierSlotNo);
		transportJobCommandInfo.setDestinationMachineName(destinationMachineName);
		transportJobCommandInfo.setDestinationPositionType(destinationPositionType);
		transportJobCommandInfo.setDestinationPositionName(destinationPositionName);
		transportJobCommandInfo.setDestinationZoneName(destinationZoneName);
		transportJobCommandInfo.setDestinationCarrierName(destinationCarrierName);
		transportJobCommandInfo.setDestinationCarrierSlotNo(destinationCarrierSlotNo);
		transportJobCommandInfo.setCurrentMachineName(currentMachineName);
		transportJobCommandInfo.setCurrentPositionType(currentPositionType);
		transportJobCommandInfo.setCurrentPositionName(currentPositionName);
		transportJobCommandInfo.setCurrentZoneName(currentZoneName);
		transportJobCommandInfo.setCurrentCarrierName(currentCarrierName);
		transportJobCommandInfo.setCurrentCarrierSlotNo(currentCarrierSlotNo);
		transportJobCommandInfo.setCarrierState(carrierState);
		transportJobCommandInfo.setLotName(maskName);
		transportJobCommandInfo.setProductQuantity(1);
		transportJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceUtil().setTransportJobCommandEventInfo(transportJobCommandInfo, eventInfo);
		transportJobCommandInfo.setLastEventResultCode(returnCode);
		transportJobCommandInfo.setLastEventResultText(returnMessage);

		try
		{
			ExtendedObjectProxy.getTransportJobCommand().create(eventInfo, transportJobCommandInfo);

			doc = SMessageUtil.addItemToBody(doc, "TRANSPORTJOBNAME", transportJobName);
		}
		catch (Exception e)
		{
			throw new CustomException("JOB-8011", e.getMessage());
		}
		
		Machine sourceMachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(sourceMachineName);
		
		// Mask Inspection MachineGroup
		String[] MaskMachineGroup = { GenericServiceProxy.getConstantMap().MachineGroup_MaskInitialInspection, 
									  GenericServiceProxy.getConstantMap().MachineGroup_MaskMacro,
									  GenericServiceProxy.getConstantMap().MachineGroup_MaskAOI,
									  GenericServiceProxy.getConstantMap().MachineGroup_MaskPPA,
									  GenericServiceProxy.getConstantMap().MachineGroup_MaskRepair };

		if(!Arrays.asList(MaskMachineGroup).contains(sourceMachineData.getMachineGroupName()) && 
		   StringUtils.equals(sourcePositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT) && 
		   StringUtils.equals(destinationPositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_SHELF))
		{
			if(StringUtils.isEmpty(maskData.getCarrierName()) || StringUtils.equals(currentCarrierName, maskData.getCarrierName()))
			{
				throw new CustomException("MASK-0068", maskName, currentCarrierName);
			}
		}
		
		// Change Mask Location
		maskData = MESTransportServiceProxy.getTransportJobServiceUtil().changeCurrentMaskLocation(maskData,
				currentMachineName, currentPositionType, currentPositionName, currentZoneName,
				currentCarrierName, currentCarrierSlotNo, transferState, "", eventInfo,returnCode,returnMessage);

		// Update Port TransferState (Source Port Already Unloaded)
		if (StringUtils.equals(destinationPositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT))
		{
			PortKey portKey = new PortKey();
			portKey.setMachineName(destinationMachineName);
			portKey.setPortName(destinationPositionName);

			MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
			makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReservedToLoad);
			makeTranferStateInfo.setValidateEventFlag("N");
			PortServiceProxy.getPortService().makeTransferState(portKey, eventInfo, makeTranferStateInfo);
		}
	}
}
