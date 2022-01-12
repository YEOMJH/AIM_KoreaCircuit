package kr.co.aim.messolution.transportjob.event;

import java.util.List;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
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
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class MaskBatchJobStartedByMCS extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX]
	 * 
	 * <Body>
	 *    <BATCHJOBNAME />
	 *    <CARRIERNAME />
	 *    <SOURCEMACHINENAME />
	 *    <SOURCEPOSITIONTYPE />
	 *    <SOURCEPOSITIONNAME />
	 *    <SOURCEZONENAME />
	 *    <DESTINATIONMACHINENAMEv
	 *    <DESTINATIONPOSITIONTYPE />
	 *    <DESTINATIONPOSITIONNAME />
	 *    <DESTINATIONZONENAME />
	 *    <TRANSPORTJOBLIST>
	 *       <TRANSPORTJOB>
	 *          <TRANSPORTJOBNAME />
	 *          <MASKNAME />
	 *          <SOURCECARRIERSLOTNO />
	 *          <DESTINATIONCARRIERSLOTNO />
	 *          <PRIORITY />
	 *          <MASKTYPE />
	 *       </TRANSPORTJOB>
	 *    </TRANSPORTJOBLIST>
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TransportStart", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		// Validation : Exist Carrier
		String batchJobName = SMessageUtil.getBodyItemValue(doc, "BATCHJOBNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String sourceMachineName = SMessageUtil.getBodyItemValue(doc, "SOURCEMACHINENAME", false);
		String sourcePositionType = SMessageUtil.getBodyItemValue(doc, "SOURCEPOSITIONTYPE", false);
		String sourcePositionName = SMessageUtil.getBodyItemValue(doc, "SOURCEPOSITIONNAME", false);
		String sourceZoneName = SMessageUtil.getBodyItemValue(doc, "SOURCEZONENAME", false);
		String destinationMachineName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONMACHINENAME", false);
		String destinationPositionType = SMessageUtil.getBodyItemValue(doc, "DESTINATIONPOSITIONTYPE", false);
		String destinationPositionName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONPOSITIONNAME", false);
		String destinationZoneName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONZONENAME", false);
		String returnCode = SMessageUtil.getReturnItemValue(doc, "RETURNCODE", true);
		String returnMessage = SMessageUtil.getReturnItemValue(doc, "RETURNMESSAGE", false);
		String transferState = SMessageUtil.getBodyItemValue(doc, "TRANSFERSTATE", false);
		String messageName = SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", true);
		String jobState = TransportJobServiceUtil.getJobState(messageName, doc);
		String cancelState = TransportJobServiceUtil.getCancelState(messageName, doc);
		String changeState = TransportJobServiceUtil.getChangeState(messageName, doc);
		String alternateFlag = "N";
		String priority = "";
		String carrierState = "";
		String transportJobType = GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_MCS;

		List<Element> transportJobList = SMessageUtil.getBodySequenceItemList(doc, "TRANSPORTJOBLIST", true);

		for (Element transportJobE : transportJobList)
		{
			String transportJobName = transportJobE.getChildText("TRANSPORTJOBNAME");
			String maskName = transportJobE.getChildText("MASKNAME");
			String sourceCarrierSlotNo = transportJobE.getChildText("SOURCECARRIERSLOTNO");
			String destinationCarrierSlotNo = transportJobE.getChildText("DESTINATIONCARRIERSLOTNO");
			priority = transportJobE.getChildText("PRIORITY");

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
			if (StringUtils.isNotEmpty(sourceCarrierSlotNo))
			{
				// Set CarrierSlotNo
				sourceCarrierSlotNo = ConvertUtil.toStringForIntTypeValue(sourceCarrierSlotNo);

				transportJobCommandInfo.setSourceCarrierName(carrierName);
				transportJobCommandInfo.setSourceCarrierSlotNo(sourceCarrierSlotNo);
			}
			transportJobCommandInfo.setDestinationMachineName(destinationMachineName);
			transportJobCommandInfo.setDestinationPositionType(destinationPositionType);
			transportJobCommandInfo.setDestinationPositionName(destinationPositionName);
			transportJobCommandInfo.setDestinationZoneName(destinationZoneName);
			if (StringUtils.isNotEmpty(destinationCarrierSlotNo))
			{
				// Set CarrierSlotNo
				destinationCarrierSlotNo = ConvertUtil.toStringForIntTypeValue(destinationCarrierSlotNo);

				transportJobCommandInfo.setDestinationCarrierName(carrierName);
				transportJobCommandInfo.setDestinationCarrierSlotNo(destinationCarrierSlotNo);
			}
			transportJobCommandInfo.setCurrentMachineName("");
			transportJobCommandInfo.setCurrentPositionType("");
			transportJobCommandInfo.setCurrentPositionName("");
			transportJobCommandInfo.setCurrentZoneName("");
			transportJobCommandInfo.setCarrierState(carrierState);
			transportJobCommandInfo.setLotName("");
			transportJobCommandInfo.setProductQuantity(transportJobList.size());
			transportJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceUtil().setTransportJobCommandEventInfo(transportJobCommandInfo, eventInfo);
			transportJobCommandInfo.setLastEventResultCode(returnCode);
			transportJobCommandInfo.setLastEventResultText(returnMessage);

			try
			{
				ExtendedObjectProxy.getTransportJobCommand().create(eventInfo, transportJobCommandInfo);
			}
			catch (Exception e)
			{
				throw new CustomException("JOB-8011", e.getMessage());
			}
		}

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		MESMachineServiceProxy.getMachineInfoUtil().getMachineData(destinationMachineName);

		if (StringUtils.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_Available))
		{
			carrierState = GenericServiceProxy.getConstantMap().MCS_CARRIERSTATE_EMPTY;
		}
		else
		{
			carrierState = GenericServiceProxy.getConstantMap().MCS_CARRIERSTATE_FULL;
		}

		// Update Carrier TransportType
		SetEventInfo setEventInfo = new SetEventInfo();
		if (StringUtils.equals(messageName, GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBEVENT_TRANSPORTJOBSTARTEDBYMCS))
		{
			setEventInfo.getUdfs().put("TRANSPORTTYPE", "Manual");
		}
		else if (StringUtils.isNotEmpty(SMessageUtil.getHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", false)))
		{
			transportJobType = GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_OIC;
		}

		TransportJobCommand transportJobCommandInfo = new TransportJobCommand();
		transportJobCommandInfo.setTransportJobName(batchJobName);
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
		transportJobCommandInfo.setCurrentMachineName("");
		transportJobCommandInfo.setCurrentPositionType("");
		transportJobCommandInfo.setCurrentPositionName("");
		transportJobCommandInfo.setCurrentZoneName("");
		transportJobCommandInfo.setCarrierState(carrierState);
		transportJobCommandInfo.setLotName("");
		transportJobCommandInfo.setProductQuantity(transportJobList.size());
		transportJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceUtil().setTransportJobCommandEventInfo(transportJobCommandInfo, eventInfo);
		transportJobCommandInfo.setLastEventResultCode(returnCode);
		transportJobCommandInfo.setLastEventResultText(returnMessage);

		try
		{
			ExtendedObjectProxy.getTransportJobCommand().create(eventInfo, transportJobCommandInfo);
		}
		catch (Exception e)
		{
			throw new CustomException("JOB-8011", e.getMessage());
		}

		DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
	}
}
