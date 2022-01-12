package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class MaskTransportJobTerminatedByMCS extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX]
	 * 
	 * <Body>
	 *    <TRANSPORTJOBNAME />
	 *    <MASKNAME />
	 *    <CURRENTMACHINENAME />
	 *    <CURRENTPOSITIONTYPE />
	 *    <CURRENTPOSITIONNAME />
	 *    <CURRENTZONENAME />
	 *    <CURRENTCARRIERNAME />
	 *    <CURRENTCARRIERSLOTNO />
	 *    <MASKTYPE />
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
		String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", false);
		String currentMachineName = SMessageUtil.getBodyItemValue(doc, "CURRENTMACHINENAME", false);
		String currentPositionType = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONTYPE", false);
		String currentPositionName = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONNAME", false);
		String currentZoneName = SMessageUtil.getBodyItemValue(doc, "CURRENTZONENAME", false);
		String currentCarrierName = SMessageUtil.getBodyItemValue(doc, "CURRENTCARRIERNAME", false);
		String currentCarrierSlotNo = SMessageUtil.getBodyItemValue(doc, "CURRENTCARRIERSLOTNO", false);
		String transferState = SMessageUtil.getBodyItemValue(doc, "TRANSFERSTATE", false);
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);
		String reasonComment = SMessageUtil.getBodyItemValue(doc, "REASONCOMMENT", false);
		String returnCode = SMessageUtil.getReturnItemValue(doc, "RETURNCODE", false);
		String returnMessage = SMessageUtil.getReturnItemValue(doc, "RETURNMESSAGE", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TransportTerminate", getEventUser(), 
				returnCode.equals("0")?reasonComment:returnMessage, "", returnCode.equals("0")?reasonCode:returnCode);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		// Update CT_TRANSPORTJOBCOMMAND
		MESTransportServiceProxy.getTransportJobServiceUtil().updateTransportJobCommand(transportJobName, doc, eventInfo);

		// MaskLot
		MaskLot maskData = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskName });

		// Set CarrierSlotNo
		currentCarrierSlotNo = ConvertUtil.toStringForIntTypeValue(currentCarrierSlotNo);

		if(transferState.equals("MOVING"))
		{
			transferState="INSTK";
		}
		// Change Mask Location
		maskData = MESTransportServiceProxy.getTransportJobServiceUtil().changeCurrentMaskLocation(maskData,
				currentMachineName, currentPositionType, currentPositionName, currentZoneName,
				currentCarrierName, currentCarrierSlotNo, transferState, "", eventInfo,returnCode,returnMessage);
		
		List<TransportJobCommand> sqlResult = ExtendedObjectProxy.getTransportJobCommand().select("TRANSPORTJOBNAME = ?", new Object[] { transportJobName });
		MESTransportServiceProxy.getTransportJobServiceUtil().checkExistTransportJobCommand(sqlResult, transportJobName);

		// update Port TransferState
		String sourceMachineName = sqlResult.get(0).getSourceMachineName();
		String sourcePositionType = sqlResult.get(0).getSourcePositionType();
		String sourcePositionName = sqlResult.get(0).getSourcePositionName();
		String destinationMachineName = sqlResult.get(0).getDestinationMachineName();
		String destinationPositionType = sqlResult.get(0).getDestinationPositionType();
		String destinationPositionName = sqlResult.get(0).getDestinationPositionName();
		String batchJobName = sqlResult.get(0).getBatchJobName();

		// change Port TransferState
		if (StringUtils.equals(currentMachineName, sourceMachineName) &&
				StringUtils.equals(currentPositionName, sourcePositionName) &&
					StringUtils.equals(sourcePositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT))
		{
			ChangePortTransferState(sourceMachineName, sourcePositionName, "ReadyToUnload");
		}

		if (StringUtils.equals(destinationPositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT)&&StringUtils.isEmpty(batchJobName))
		{
			ChangePortTransferState(destinationMachineName, destinationPositionName, "ReadyToLoad");
		}
		
		//Mask未搬到最高优先级STK则EM推送
		boolean checkFlag = MESTransportServiceProxy.getTransportJobServiceUtil().checkTransportToFirstStock(maskData, transportJobName, eventInfo);
		if(!checkFlag)
		{
			try
			{				
				MESTransportServiceProxy.getTransportJobServiceUtil().sendToEmMaskSTK("Mask", "MaskTranspotToFirstSTK", maskData);						
			}
			catch (Exception e)
			{
				eventLog.info("eMobile or WeChat Send Error");	
			}
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
