package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.messolution.transportjob.service.TransportJobServiceUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.PortKey;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class TransportJobStartedByMCS extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX]
	 * 
	 * <Body>
	 *    <TRANSPORTJOBNAME />
	 *    <CARRIERNAME />
	 *    <SOURCEMACHINENAME />
	 *    <SOURCEPOSITIONTYPE />
	 *    <SOURCEPOSITIONNAME />
	 *    <SOURCEZONENAME />
	 *    <CURRENTMACHINENAME />
	 *    <CURRENTPOSITIONTYPE />
	 *    <CURRENTPOSITIONNAME />
	 *    <CURRENTZONENAME />
	 *    <DESTINATIONMACHINENAME />
	 *    <DESTINATIONPOSITIONTYPE />
	 *    <DESTINATIONPOSITIONNAME />
	 *    <DESTINATIONZONENAME />
	 *    <PRIORITY />
	 *    <CARRIERSTATE />
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
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String sourceMachineName = SMessageUtil.getBodyItemValue(doc, "SOURCEMACHINENAME", false);
		String sourcePositionType = SMessageUtil.getBodyItemValue(doc, "SOURCEPOSITIONTYPE", false);
		String sourcePositionName = SMessageUtil.getBodyItemValue(doc, "SOURCEPOSITIONNAME", false);
		String sourceZoneName = SMessageUtil.getBodyItemValue(doc, "SOURCEZONENAME", false);
		String currentMachineName = SMessageUtil.getBodyItemValue(doc, "CURRENTMACHINENAME", false);
		String currentPositionType = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONTYPE", false);
		String currentPositionName = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONNAME", false);
		String currentZoneName = SMessageUtil.getBodyItemValue(doc, "CURRENTZONENAME", false);
		String destinationMachineName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONMACHINENAME", false);
		String destinationPositionType = SMessageUtil.getBodyItemValue(doc, "DESTINATIONPOSITIONTYPE", false);
		String destinationPositionName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONPOSITIONNAME", false);
		String destinationZoneName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONZONENAME", false);
		String priority = SMessageUtil.getBodyItemValue(doc, "PRIORITY", false);
		String transferState = SMessageUtil.getBodyItemValue(doc, "TRANSFERSTATE", false);
		String returnCode = SMessageUtil.getReturnItemValue(doc, "RETURNCODE", false);
		String returnMessage = SMessageUtil.getReturnItemValue(doc, "RETURNMESSAGE", false);
		String messageName = SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", true);
		String jobState = TransportJobServiceUtil.getJobState(messageName, doc);
		String cancelState = TransportJobServiceUtil.getCancelState(messageName, doc);
		String changeState = TransportJobServiceUtil.getChangeState(messageName, doc);
		String alternateFlag = "N";

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		MESMachineServiceProxy.getMachineInfoUtil().getMachineData(destinationMachineName);

		String transportJobType = "";
		String transportType = StringUtils.EMPTY;
		if (StringUtils.equals(messageName, GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_TRANSPORTJOBSTARTEDBYMCS))
		{
			transportJobType = GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_MCS;
			transportType = "Manual";
		}
		else if (StringUtils.isNotEmpty(SMessageUtil.getHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", false)))
		{
			transportJobType = GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_OIC;
		}

		// Update Current Carrier Location
		durableData = MESTransportServiceProxy.getTransportJobServiceUtil().changeCurrentCarrierLocation(durableData,
				currentMachineName, currentPositionType, currentPositionName, currentZoneName, transferState, "Y", eventInfo, transportType);

		String lotName = "";
		Lot lotData = new Lot();
		try
		{
			lotName = MESLotServiceProxy.getLotInfoUtil().getLotNameByCarrierName(carrierName);
			lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		}
		catch (Exception e)
		{
		}

		double productQuantity = lotData.getProductQuantity();
		

		String carrierState = "";
		if (StringUtils.isEmpty(lotName))
		{
			carrierState = GenericServiceProxy.getConstantMap().MCS_CARRIERSTATE_EMPTY;
		}
		else
		{
			carrierState = GenericServiceProxy.getConstantMap().MCS_CARRIERSTATE_FULL;
		}

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
		transportJobCommandInfo.setCurrentMachineName(currentMachineName);
		transportJobCommandInfo.setCurrentPositionType(currentPositionType);
		transportJobCommandInfo.setCurrentPositionName(currentPositionName);
		transportJobCommandInfo.setCurrentZoneName(currentZoneName);
		transportJobCommandInfo.setCarrierState(carrierState);
		transportJobCommandInfo.setLotName(lotName);
		transportJobCommandInfo.setProductQuantity((long) productQuantity);
		transportJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceUtil().setTransportJobCommandEventInfo(transportJobCommandInfo, eventInfo);
		transportJobCommandInfo.setLastEventResultCode(returnCode);
		transportJobCommandInfo.setLastEventResultText(returnMessage);

		try
		{
			ExtendedObjectProxy.getTransportJobCommand().create(eventInfo, transportJobCommandInfo);
			SMessageUtil.setItemValue(doc, "Body", "TRANSPORTJOBNAME", transportJobName);
		}
		catch (Exception e)
		{
			throw new CustomException("JOB-8011", e.getMessage());
		}


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
