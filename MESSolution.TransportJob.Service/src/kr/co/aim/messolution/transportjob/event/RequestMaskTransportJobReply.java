package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
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
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class RequestMaskTransportJobReply extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX -> OIC]
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
		String returnCode = SMessageUtil.getReturnItemValue(doc, "RETURNCODE", false);

		EventInfo eventInfo = new EventInfo();
		if (StringUtils.equals(returnCode, "0"))
		{
			eventInfo = EventInfoUtil.makeEventInfo("TransportAccept", "MCS", getEventComment(), "", "");
		}
		else
		{
			eventInfo = EventInfoUtil.makeEventInfo("TransportReject", "MCS", getEventComment(), "", "");
		}
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		// Validation : Exist Carrier
		String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);
		String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskName });

		String sourceMachineName = SMessageUtil.getBodyItemValue(doc, "SOURCEMACHINENAME", false);
		String sourcePositionType = SMessageUtil.getBodyItemValue(doc, "SOURCEPOSITIONTYPE", false);
		String sourcePositionName = SMessageUtil.getBodyItemValue(doc, "SOURCEPOSITIONNAME", false);
		String destinationMachineName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONMACHINENAME", false);
		String destinationPositionType = SMessageUtil.getBodyItemValue(doc, "DESTINATIONPOSITIONTYPE", false);
		String destinationPositionName = SMessageUtil.getBodyItemValue(doc, "DESTINATIONPOSITIONNAME", false);

		// Update CT_TRANSPORTJOBCOMMAND
		MESTransportServiceProxy.getTransportJobServiceUtil().updateTransportJobCommand(transportJobName, doc, eventInfo);

		String jobState = TransportJobServiceUtil.getJobState(SMessageUtil.getMessageName(doc), doc);

		if (jobState.equals(GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Rejected))
		{
			// Update mask lot transferstate
			ExtendedObjectProxy.getMaskLotService().setTransferState(eventInfo, maskLotData, GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_INSTK);
	
			// Change Port Transfer State
			try
			{
				if (StringUtils.equals(destinationPositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT))
				{
					ChangePortTransferState(destinationMachineName, destinationPositionName, "ReadyToLoad",eventInfo);
				}
				if (StringUtils.equals(sourcePositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT))
				{
					ChangePortTransferState(sourceMachineName, sourcePositionName, "ReadyToUnload",eventInfo);
				}
			}
			catch (Exception ex)
			{
				eventLog.error("Port transfer state change failed");
				eventLog.error(ex.getMessage());
			}

			// Set ReturnMessage - add [MCS Exception]
			String returnMessage = SMessageUtil.getReturnItemValue(doc, "RETURNMESSAGE", false);
			SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, "[MCS Exception] \n" + returnMessage);
		}

		String originalSourceSubjectName = getOriginalSourceSubjectName();

		// Send Reply to OIC
		GenericServiceProxy.getESBServive().sendBySenderWithoutChangeReturnElement(originalSourceSubjectName, doc, "OICSender");
	}

	private void ChangePortTransferState(String sMachineName, String sPortName, String sTransferStateName,EventInfo eventInfo) throws CustomException
	{
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
