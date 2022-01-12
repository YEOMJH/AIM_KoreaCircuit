package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;

import org.jdom.Document;

public class DestinationChanged extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX]
	 * 
	 * <Body>
	 *    <TRANSPORTJOBNAME />
	 *    <CARRIERNAME />
	 *    <OLDDESTINATIONMACHINENAME />
	 *    <OLDDESTINATIONPOSITIONTYPE />
	 *    <OLDDESTINATIONPOSITIONNAME />
	 *    <OLDDESTINATIONZONENAME />
	 *    <NEWDESTINATIONMACHINENAME />
	 *    <NEWDESTINATIONPOSITIONTYPE />
	 *    <NEWDESTINATIONPOSITIONNAME />
	 *    <NEWDESTINATIONZONENAME />
	 *    <PRIORITY />
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Change", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);

		// Validation : Exist Carrier
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

		// Update CT_TRANSPORTJOBCOMMAND
		MESTransportServiceProxy.getTransportJobServiceUtil().updateTransportJobCommand(transportJobName, doc, eventInfo);

		// Update OldDestination Port TransferState to ReadyToLoad
		String oldDestinationMachineName = SMessageUtil.getBodyItemValue(doc, "OLDDESTINATIONMACHINENAME", false);
		String oldDestinationPositionType = SMessageUtil.getBodyItemValue(doc, "OLDDESTINATIONPOSITIONTYPE", false);
		String oldDestinationPositionName = SMessageUtil.getBodyItemValue(doc, "OLDDESTINATIONPOSITIONNAME", false);

		if ("SHELF".equals(oldDestinationPositionType))
		{
			eventLog.info("+++ OldDestinationPositionType is SHELF");
			return ;
		}
		
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(oldDestinationMachineName, oldDestinationPositionName);
		String transferState = GenericServiceProxy.getConstantMap().Port_ReadyToLoad;

		MakeTransferStateInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().makeTransferStateInfo(portData, transferState);
		MESPortServiceProxy.getPortServiceImpl().makeTransferState(portData, transitionInfo, eventInfo);
	}
}
