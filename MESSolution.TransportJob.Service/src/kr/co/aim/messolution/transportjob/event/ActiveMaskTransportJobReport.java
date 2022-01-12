package kr.co.aim.messolution.transportjob.event;

import java.util.List;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;

import org.jdom.Document;
import org.jdom.Element;

public class ActiveMaskTransportJobReport extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX]
	 * 
	 * <Body>
	 *    <TRANSPORTJOBLIST>
	 *       <TRANSPORTJOB>
	 *          <TRANSPORTJOBNAME />
	 *          <MASKNAME />
	 *          <SOURCEMACHINENAME />
	 *          <SOURCEPOSITIONTYPE />
	 *          <SOURCEPOSITIONNAME />
	 *          <SOURCEZONENAME />
	 *          <SOURCECARRIERNAME />
	 *          <SOURCECARRIERSLOTNO />
	 *          <CURRENTMACHINENAME />
	 *          <CURRENTPOSITIONTYPE />
	 *          <CURRENTPOSITIONNAME />
	 *          <CURRENTZONENAME />
	 *          <CURRENTCARRIERNAME />
	 *          <CURRENTCARRIERSLOTNO />
	 *          <DESTINATIONMACHINENAME />
	 *          <DESTINATIONPOSITIONTYPE />
	 *          <DESTINATIONPOSITIONNAME />
	 *          <DESTINATIONZONENAME />
	 *          <DESTINATIONCARRIERNAME />
	 *          <DESTINATIONCARRIERSLOTNO />
	 *          <PRIORITY />
	 *          <MASKTYPE />
	 *          <TRANSFERSTATE />
	 *          <ALTERNATEFLAG />
	 *       </TRANSPORTJOB>
	 *    </TRANSPORTJOBLIST>
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ActiveMaskTransportJobReport", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		List<Element> transportJobList = SMessageUtil.getBodySequenceItemList(doc, "TRANSPORTJOBLIST", false);

		if (transportJobList.size() == 0)
		{
			eventLog.info("No job reported");
			return;
		}

		for (Element transportJobE : transportJobList)
		{
			String transportJobName = transportJobE.getChildText("TRANSPORTJOBNAME");
			List<TransportJobCommand> sqlResult = ExtendedObjectProxy.getTransportJobCommand().select("TRANSPORTJOBNAME = ?", new Object[] { transportJobName });

			if (sqlResult.size() == 0)
			{
				String maskName = transportJobE.getChildText("MASKNAME");
				String sourceMachineName = transportJobE.getChildText("SOURCEMACHINENAME");
				String sourcePositionType = transportJobE.getChildText("SOURCEPOSITIONTYPE");
				String sourcePositionName = transportJobE.getChildText("SOURCEPOSITIONNAME");
				String sourceZoneName = transportJobE.getChildText("SOURCEZONENAME");
				String sourceCarrierName = transportJobE.getChildText("SOURCECARRIERNAME");
				String sourceCarrierSlotNo = transportJobE.getChildText("SOURCECARRIERSLOTNO");
				String currentMachineName = transportJobE.getChildText("CURRENTMACHINENAME");
				String currentPositionType = transportJobE.getChildText("CURRENTPOSITIONTYPE");
				String currentPositionName = transportJobE.getChildText("CURRENTPOSITIONNAME");
				String currentZoneName = transportJobE.getChildText("CURRENTZONENAME");
				String currentCarrierName = transportJobE.getChildText("CURRENTCARRIERNAME");
				String currentCarrierSlotNo = transportJobE.getChildText("CURRENTCARRIERSLOTNO");
				String destinationMachineName = transportJobE.getChildText("DESTINATIONMACHINENAME");
				String destinationPositionType = transportJobE.getChildText("DESTINATIONPOSITIONTYPE");
				String destinationPositionName = transportJobE.getChildText("DESTINATIONPOSITIONNAME");
				String destinationZoneName = transportJobE.getChildText("DESTINATIONZONENAME");
				String destinationCarrierName = transportJobE.getChildText("DESTINATIONCARRIERNAME");
				String destinationCarrierSlotNo = transportJobE.getChildText("DESTINATIONCARRIERSLOTNO");
				String priority = transportJobE.getChildText("PRIORITY");
				String transferState = transportJobE.getChildText("TRANSFERSTATE");
				String alternateFlag = transportJobE.getChildText("ALTERNATEFLAG");

				// Set CarrierSlotNo
				sourceCarrierSlotNo = ConvertUtil.toStringForIntTypeValue(sourceCarrierSlotNo);
				currentCarrierSlotNo = ConvertUtil.toStringForIntTypeValue(currentCarrierSlotNo);
				destinationCarrierSlotNo = ConvertUtil.toStringForIntTypeValue(destinationCarrierSlotNo);

				TransportJobCommand transportJobCommandInfo = new TransportJobCommand();
				transportJobCommandInfo.setTransportJobName(transportJobName);
				transportJobCommandInfo.setCarrierName(maskName);
				transportJobCommandInfo.setSourceMachineName(sourceMachineName);
				transportJobCommandInfo.setSourcePositionType(sourcePositionType);
				transportJobCommandInfo.setSourcePositionName(sourcePositionName);
				transportJobCommandInfo.setSourceZoneName(sourceZoneName);
				transportJobCommandInfo.setSourceCarrierName(sourceCarrierName);
				transportJobCommandInfo.setSourceCarrierSlotNo(sourceCarrierSlotNo);
				transportJobCommandInfo.setCurrentMachineName(currentMachineName);
				transportJobCommandInfo.setCurrentPositionType(currentPositionType);
				transportJobCommandInfo.setCurrentPositionName(currentPositionName);
				transportJobCommandInfo.setCurrentZoneName(currentZoneName);
				transportJobCommandInfo.setCurrentCarrierName(currentCarrierName);
				transportJobCommandInfo.setCurrentCarrierSlotNo(currentCarrierSlotNo);
				transportJobCommandInfo.setDestinationMachineName(destinationMachineName);
				transportJobCommandInfo.setDestinationPositionType(destinationPositionType);
				transportJobCommandInfo.setDestinationPositionName(destinationPositionName);
				transportJobCommandInfo.setDestinationZoneName(destinationZoneName);
				transportJobCommandInfo.setDestinationCarrierName(destinationCarrierName);
				transportJobCommandInfo.setDestinationCarrierSlotNo(destinationCarrierSlotNo);
				transportJobCommandInfo.setPriority(priority);
				transportJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceUtil().setTransportJobCommandEventInfo(transportJobCommandInfo, eventInfo);

				transportJobCommandInfo.setProductQuantity(0);
				transportJobCommandInfo.setTransferState(transferState);
				transportJobCommandInfo.setAlternateFlag(alternateFlag);

				ExtendedObjectProxy.getTransportJobCommand().create(eventInfo, transportJobCommandInfo);
			}
		}
	}
}
