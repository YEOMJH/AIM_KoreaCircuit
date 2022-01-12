package kr.co.aim.messolution.transportjob.event;

import java.util.List;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;

import org.jdom.Document;
import org.jdom.Element;

public class ActiveTransportJobReport extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX]
	 * 
	 * <Body>
	 *    <TRANSPORTJOBLIST>
	 *       <TRANSPORTJOB>
	 *          <TRANSPORTJOBNAME />
	 *          <CARRIERNAME />
	 *          <SOURCEMACHINENAME />
	 *          <SOURCEPOSITIONTYPE />
	 *          <SOURCEPOSITIONNAME />
	 *          <SOURCEZONENAME />
	 *          <CURRENTMACHINENAME />
	 *          <CURRENTPOSITIONTYPE />
	 *          <CURRENTPOSITIONNAME />
	 *          <CURRENTZONENAME />
	 *          <DESTINATIONMACHINENAME />
	 *          <DESTINATIONPOSITIONTYPE />
	 *          <DESTINATIONPOSITIONNAME />
	 *          <DESTINATIONZONENAME />
	 *          <PRIORITY />
	 *          <CARRIERSTATE />
	 *          <CARRIERTYPE />
	 *          <CLEANSTATE />
	 *          <LOTNAME />
	 *          <PRODUCTQUANTITY />
	 *          <TRANSFERSTATE />
	 *          <ALTERNATEFLAG />
	 *       </TRANSPORTJOB>
	 *    <TRANSPORTJOBLIST>
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ActiveTransportJobReport", getEventUser(), getEventComment(), "", "");
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
				String carrierName = transportJobE.getChildText("CARRIERNAME");
				String sourceMachineName = transportJobE.getChildText("SOURCEMACHINENAME");
				String sourcePositionType = transportJobE.getChildText("SOURCEPOSITIONTYPE");
				String sourcePositionName = transportJobE.getChildText("SOURCEPOSITIONNAME");
				String sourceZoneName = transportJobE.getChildText("SOURCEZONENAME");
				String currentMachineName = transportJobE.getChildText("CURRENTMACHINENAME");
				String currentPositionType = transportJobE.getChildText("CURRENTPOSITIONTYPE");
				String currentPositionName = transportJobE.getChildText("CURRENTPOSITIONNAME");
				String currentZoneName = transportJobE.getChildText("CURRENTZONENAME");
				String destinationMachineName = transportJobE.getChildText("DESTINATIONMACHINENAME");
				String destinationPositionType = transportJobE.getChildText("DESTINATIONPOSITIONTYPE");
				String destinationPositionName = transportJobE.getChildText("DESTINATIONPOSITIONNAME");
				String destinationZoneName = transportJobE.getChildText("DESTINATIONZONENAME");
				String priority = transportJobE.getChildText("PRIORITY");
				String carrierState = transportJobE.getChildText("CARRIERSTATE");
				String lotName = transportJobE.getChildText("LOTNAME");
				String productQuantity = transportJobE.getChildText("PRODUCTQUANTITY");
				String transferState = transportJobE.getChildText("TRANSFERSTATE");
				String alternateFlag = transportJobE.getChildText("ALTERNATEFLAG");

				TransportJobCommand transportJobCommandInfo = new TransportJobCommand();
				transportJobCommandInfo.setTransportJobName(transportJobName);
				transportJobCommandInfo.setCarrierName(carrierName);
				transportJobCommandInfo.setSourceMachineName(sourceMachineName);
				transportJobCommandInfo.setSourcePositionType(sourcePositionType);
				transportJobCommandInfo.setSourcePositionName(sourcePositionName);
				transportJobCommandInfo.setSourceZoneName(sourceZoneName);
				transportJobCommandInfo.setCurrentMachineName(currentMachineName);
				transportJobCommandInfo.setCurrentPositionType(currentPositionType);
				transportJobCommandInfo.setCurrentPositionName(currentPositionName);
				transportJobCommandInfo.setCurrentZoneName(currentZoneName);
				transportJobCommandInfo.setDestinationMachineName(destinationMachineName);
				transportJobCommandInfo.setDestinationPositionType(destinationPositionType);
				transportJobCommandInfo.setDestinationPositionName(destinationPositionName);
				transportJobCommandInfo.setDestinationZoneName(destinationZoneName);
				transportJobCommandInfo.setPriority(priority);
				transportJobCommandInfo.setCarrierState(carrierState);
				transportJobCommandInfo.setLotName(lotName);
				transportJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceUtil().setTransportJobCommandEventInfo(transportJobCommandInfo, eventInfo);

				try
				{
					transportJobCommandInfo.setProductQuantity(Long.valueOf(productQuantity));
				}
				catch (Exception e)
				{
					transportJobCommandInfo.setProductQuantity(0);
				}
				transportJobCommandInfo.setTransferState(transferState);
				transportJobCommandInfo.setAlternateFlag(alternateFlag);

				ExtendedObjectProxy.getTransportJobCommand().create(eventInfo, transportJobCommandInfo);
			}
		}
	}
}
