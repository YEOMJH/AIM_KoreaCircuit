package kr.co.aim.messolution.transportjob.event;

import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;

import org.jdom.Document;

public class TransportJobTerminatedByMCS extends AsyncHandler {

	/**
	 * MessageSpc [MCS -> TEX]
	 * 
	 * <Body>
	 *    <TRANSPORTJOBNAME />
	 *    <CARRIERNAME />
	 *    <CURRENTMACHINENAME />
	 *    <CURRENTPOSITIONTYPE />
	 *    <CURRENTPOSITIONNAME />
	 *    <CURRENTZONENAME />
	 *    <CARRIERSTATE />
	 *    <TRANSFERSTATE />
	 *    <ALTERNATEFLAG />
	 *    <REASONCODE />
	 *    <REASONCOMMENT />
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		// Validation : Exist Carrier
		String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String currentMachineName = SMessageUtil.getBodyItemValue(doc, "CURRENTMACHINENAME", false);
		String currentPositionType = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONTYPE", false);
		String currentPositionName = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONNAME", false);
		String currentZoneName = SMessageUtil.getBodyItemValue(doc, "CURRENTZONENAME", false);
		String transferState = SMessageUtil.getBodyItemValue(doc, "TRANSFERSTATE", false);
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);
		String reasonComment = SMessageUtil.getBodyItemValue(doc, "REASONCOMMENT", false);
		String returnCode = SMessageUtil.getReturnItemValue(doc, "RETURNCODE", false);
		String returnMessage = SMessageUtil.getReturnItemValue(doc, "RETURNMESSAGE", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TransportTerminate", getEventUser(), 
				returnCode.equals("0")?reasonComment:returnMessage, "", returnCode.equals("0")?reasonCode:returnCode);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		
		// update Current Carrier Location
		durableData = MESTransportServiceProxy.getTransportJobServiceUtil().changeCurrentCarrierLocation(durableData,
				currentMachineName, currentPositionType, currentPositionName, currentZoneName, transferState, "N", eventInfo);
		
		// Update CT_TRANSPORTJOBCOMMAND
		MESTransportServiceProxy.getTransportJobServiceUtil().updateTransportJobCommand(transportJobName, doc, eventInfo);

		List<TransportJobCommand> sqlResult = ExtendedObjectProxy.getTransportJobCommand().select("TRANSPORTJOBNAME = ?", new Object[] { transportJobName });
		MESTransportServiceProxy.getTransportJobServiceUtil().checkExistTransportJobCommand(sqlResult, transportJobName);

		// update Port TransferState
		String sourceMachineName = sqlResult.get(0).getSourceMachineName();
		String sourcePositionType = sqlResult.get(0).getSourcePositionType();
		String sourcePositionName = sqlResult.get(0).getSourcePositionName();
		String destinationMachineName = sqlResult.get(0).getDestinationMachineName();
		String destinationPositionType = sqlResult.get(0).getDestinationPositionType();
		String destinationPositionName = sqlResult.get(0).getDestinationPositionName();

		// change Port TransferState
		if (StringUtil.equals(currentMachineName, sourceMachineName) &&
			StringUtil.equals(currentPositionName, sourcePositionName) &&
			StringUtil.equals(sourcePositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT))
		{
			if (StringUtil.equals(sourcePositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT))
			{
				ChangePortTransferState(sourceMachineName, sourcePositionName, "ReadyToUnload");
			}
		}

		if (StringUtil.equals(destinationPositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT))
		{
			ChangePortTransferState(destinationMachineName, destinationPositionName, "ReadyToLoad");
		}
	}

	private void ChangePortTransferState(String sMachineName, String sPortName, String sTransferStateName) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeTransferState", this.getEventUser(), "TransportComplete", "", "");

		try
		{
			Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(sMachineName, sPortName);

			MakeTransferStateInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().makeTransferStateInfo(portData, sTransferStateName);

			MESPortServiceProxy.getPortServiceImpl().makeTransferState(portData, transitionInfo, eventInfo);
		}
		catch (CustomException e)
		{
			throw new CustomException("ChangePortTransferStateError", sMachineName, sPortName);
		}
	}
}
