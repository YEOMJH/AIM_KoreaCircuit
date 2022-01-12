package kr.co.aim.messolution.transportjob.event;

import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.PortKey;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;

import org.jdom.Document;

public class TransportJobCancelCompleted extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX]
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
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelComplete", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		// Validation : Exist Carrier
		String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String currentMachineName = SMessageUtil.getBodyItemValue(doc, "CURRENTMACHINENAME", false);
		String currentPositionType = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONTYPE", false);
		String currentPositionName = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONNAME", false);
		String currentZoneName = SMessageUtil.getBodyItemValue(doc, "CURRENTZONENAME", false);
		String transferState = SMessageUtil.getBodyItemValue(doc, "TRANSFERSTATE", false);

		Durable durableData = new Durable();
		try
		{
			durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		}
		catch (Exception e)
		{
			throw new CustomException("CST-0001", carrierName);
		}

		// Update Current Carrier Location
		durableData = MESTransportServiceProxy.getTransportJobServiceUtil().changeCurrentCarrierLocation(durableData,
				currentMachineName, currentPositionType, currentPositionName, currentZoneName, transferState, "N", eventInfo);
		
		// Update CT_TRANSPORTJOBCOMMAND
		MESTransportServiceProxy.getTransportJobServiceUtil().updateTransportJobCommand(transportJobName, doc, eventInfo);

		List<TransportJobCommand> sqlResult = ExtendedObjectProxy.getTransportJobCommand().select("TRANSPORTJOBNAME = ?", new Object[] { transportJobName });
		MESTransportServiceProxy.getTransportJobServiceUtil().checkExistTransportJobCommand(sqlResult, transportJobName);

		// Update Port TransferState
		String sourceMachineName = sqlResult.get(0).getSourceMachineName();
		String sourcePositionType = sqlResult.get(0).getSourcePositionType();
		String sourcePositionName = sqlResult.get(0).getSourcePositionName();
		String destinationMachineName = sqlResult.get(0).getDestinationMachineName();
		String destinationPositionType = sqlResult.get(0).getDestinationPositionType();
		String destinationPositionName = sqlResult.get(0).getDestinationPositionName();

		try
		{
			if (StringUtil.equals(currentMachineName, sourceMachineName) &&
				StringUtil.equals(currentPositionName, sourcePositionName) &&
				StringUtil.equals(sourcePositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT))
			{
				CommonValidation.checkExistMachine(sourceMachineName);
				CommonValidation.checkExistPort(sourceMachineName, sourcePositionName);

				PortKey portKey = new PortKey();
				portKey.setMachineName(sourceMachineName);
				portKey.setPortName(sourcePositionName);

				MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
				makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToUnload);
				makeTranferStateInfo.setValidateEventFlag("N");

				PortServiceProxy.getPortService().makeTransferState(portKey, eventInfo, makeTranferStateInfo);
			}

			if (StringUtil.equals(destinationPositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT))
			{
				CommonValidation.checkExistMachine(destinationMachineName);

				CommonValidation.checkExistPort(destinationMachineName, destinationPositionName);

				PortKey portKey = new PortKey();
				portKey.setMachineName(destinationMachineName);
				portKey.setPortName(destinationPositionName);

				MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
				makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToLoad);
				makeTranferStateInfo.setValidateEventFlag("N");

				PortServiceProxy.getPortService().makeTransferState(portKey, eventInfo, makeTranferStateInfo);
			}
		}
		catch (Exception ex)
		{
			eventLog.error("Port transfer state change failed");
			eventLog.error(ex.getMessage());
		}
	}
}
