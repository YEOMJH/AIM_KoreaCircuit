package kr.co.aim.messolution.transportjob.event;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.DirtyInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.PortKey;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

import com.sun.istack.internal.logging.Logger;

public class TransportJobCompleted extends AsyncHandler {

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
	 *    <REASONCODE />
	 *    <REASONCOMMENT />
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		try
		{
			eventLog.info("Start Thread Sleep for history.");
			Thread.sleep(200);
			eventLog.info("Stop Thread Sleep for history.");
		}
		catch (InterruptedException e)
		{
		}
    
		// Validation : Exist Carrier
		String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String currentMachineName = SMessageUtil.getBodyItemValue(doc, "CURRENTMACHINENAME", false);
		String currentPositionType = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONTYPE", false);
		String currentPositionName = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONNAME", false);
		String currentZoneName = SMessageUtil.getBodyItemValue(doc, "CURRENTZONENAME", false);
		String transferState = SMessageUtil.getBodyItemValue(doc, "TRANSFERSTATE", false);
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);
		String reasonComment = SMessageUtil.getBodyItemValue(doc, "REASONCOMMENT", false);
		String returnCode = SMessageUtil.getReturnItemValue(doc, "RETURNCODE", false);
		String returnMessage = SMessageUtil.getReturnItemValue(doc, "RETURNMESSAGE", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TransportComplete", getEventUser(), returnCode.equals("0")?reasonComment:returnCode+": "+returnMessage, "", reasonCode);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		Durable durableData = new Durable();
		try
		{
			durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		}
		catch (Exception e)
		{
			throw new CustomException("CST-0001", carrierName);
		}

		if (StringUtils.equals(durableData.getDurableType(), GenericServiceProxy.getConstantMap().DURABLETYPE_FilmBox))
		{
			DeassignMaterialToCST(durableData, carrierName);
		}
		//delete DSPReserveLot
		if(StringUtils.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_InUse)&&(durableData.getFactoryName().equals("ARRAY")||durableData.getFactoryName().equals("TP")||durableData.getFactoryName().equals("OLED"))){
			String lotName = MESLotServiceProxy.getLotInfoUtil().getLotNameByCarrierName(carrierName);
			if (StringUtil.isNotEmpty(lotName)){
				Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
				if(lotData!=null){
					deleteCSTReserve( lotData.getKey().getLotName(),lotData.getProductSpecName(),lotData.getProcessOperationName(),lotData.getProductRequestName());
					
				}
				
			}
		}
		// update Current Carrier Location
		durableData = MESTransportServiceProxy.getTransportJobServiceUtil().changeCurrentCarrierLocation(durableData, currentMachineName, currentPositionType, currentPositionName, currentZoneName,
				transferState, "N", eventInfo);
		
		{// CSTCleaner not online，ChangeCSTCleanState，CSTCleaner Online need Delte
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(currentMachineName);
			if ("Cassette Cleaner".equals(machineData.getMachineGroupName()) || StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_CSTCleaner)
					|| StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_MaskCSTCleaner))
			{
			deleteCSTTransfer(carrierName);//delete CT_DURABLERESERVETRANSFER  20200816 caixu
		    }
		}
		// Update CT_TRANSPORTJOBCOMMAND
		MESTransportServiceProxy.getTransportJobServiceUtil().updateTransportJobCommand(transportJobName, doc, eventInfo);

		List<TransportJobCommand> sqlResult = ExtendedObjectProxy.getTransportJobCommand().select("TRANSPORTJOBNAME = ?", new Object[] { transportJobName });

		// update Port TransferState
		String sourceMachineName = sqlResult.get(0).getSourceMachineName();
		String sourcePositionType = sqlResult.get(0).getSourcePositionType();
		String sourcePositionName = sqlResult.get(0).getSourcePositionName();
		String destinationMachineName = sqlResult.get(0).getDestinationMachineName();
		String destinationPositionType = sqlResult.get(0).getDestinationPositionType();
		String destinationPositionName = sqlResult.get(0).getDestinationPositionName();
		/*
		 * 屏体CST返回OLED后自动变Dirty add by xiaoxh 2021/1/13
		 */
		MESTransportServiceProxy.getTransportJobServiceUtil().postCellToOLEDDirty(destinationMachineName, currentMachineName, sourceMachineName, carrierName);

		try
		{
			if (StringUtils.equals(currentMachineName, sourceMachineName) && StringUtils.equals(currentPositionName, sourcePositionName)
					&& StringUtils.equals(sourcePositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT))
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

			//MachineSpec dMachineSpec = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(destinationMachineName);
			if (StringUtils.equals(destinationPositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT))
			{
				CommonValidation.checkExistMachine(destinationMachineName);

				CommonValidation.checkExistPort(destinationMachineName, destinationPositionName);

				PortKey portKey = new PortKey();
				portKey.setMachineName(destinationMachineName);
				portKey.setPortName(destinationPositionName);

				MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
				if (currentMachineName.equals(destinationMachineName) && currentPositionType.equals(destinationPositionType) && currentPositionName.equals(destinationPositionName))
				{
					makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToProcess);
				}
				else
				{
					makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToLoad);
				}
				makeTranferStateInfo.setValidateEventFlag("N");
				PortServiceProxy.getPortService().makeTransferState(portKey, eventInfo, makeTranferStateInfo);
			}
//			if (StringUtils.equals(dMachineSpec.getMachineType(), GenericServiceProxy.getConstantMap().Mac_StorageMachine)
//					&& StringUtils.equals(destinationPositionType, GenericServiceProxy.getConstantMap().MCS_POSITIONTYPE_PORT))
//			{
//				CommonValidation.checkExistMachine(destinationMachineName);
//
//				CommonValidation.checkExistPort(destinationMachineName, destinationPositionName);
//
//				PortKey portKey = new PortKey();
//				portKey.setMachineName(destinationMachineName);
//				portKey.setPortName(destinationPositionName);
//
//				MakeTransferStateInfo makeTranferStateInfo = new MakeTransferStateInfo();
//				if (currentMachineName.equals(destinationMachineName) && currentPositionType.equals(destinationPositionType) && currentPositionName.equals(destinationPositionName))
//				{
//					makeTranferStateInfo.setTransferState(GenericServiceProxy.getConstantMap().Port_ReadyToProcess);
//				}
//				makeTranferStateInfo.setValidateEventFlag("N");
//
//				PortServiceProxy.getPortService().makeTransferState(portKey, eventInfo, makeTranferStateInfo);
//			}
		}
		catch (Exception ex)
		{
			eventLog.error("Port transfer state change failed");
			eventLog.error(ex.getMessage());
		}
	}
	 private void deleteCSTTransfer(String durableName )throws CustomException
	 {
	 String sql = " Delete CT_DURABLERESERVETRANSFER WHERE DURABLENAME = :DURABLENAME ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("DURABLENAME", durableName);

		try
		{
			GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		}
		catch (Exception e)
		{
			eventLog.error("CT_DURABLERESERVETRANSFER failed");
		}
	 }
	 private void deleteCSTReserve(String lotName,String productSpecName,String processOperationName,String productRequestName )throws CustomException
	 {
	    String sql = " Delete CT_RESERVELOT WHERE LOTNAME = :LOTNAME AND PRODUCTSPECNAME=:PRODUCTSPECNAME AND PROCESSOPERATIONNAME=:PROCESSOPERATIONNAME AND PRODUCTREQUESTNAME=:PRODUCTREQUESTNAME AND RESERVESTATE='Reserved'";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("LOTNAME", lotName);
		bindMap.put("PRODUCTSPECNAME", productSpecName);
		bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		bindMap.put("PRODUCTREQUESTNAME", productRequestName);

		try
		{
			GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		}
		catch (Exception e)
		{
			eventLog.error("CT_RESERVELOT  failed");
		}
	 }

	@SuppressWarnings("unchecked")
	private void DeassignMaterialToCST(Durable durableData, String carrierName)
	{
		eventLog.info("DeassignMaterialToCST Start,FilmBox is " + carrierName);
		
		try
		{
			String sql = " SELECT CONSUMABLENAME FROM CONSUMABLE WHERE CARRIERNAME = :CARRIERNAME ";

			Map<String, String> args = new HashMap<String, String>();
			args.put("CARRIERNAME", carrierName);

			List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);
			String materialName = ConvertUtil.getMapValueByName(result.get(0), "CONSUMABLENAME");

			Consumable film = MESConsumableServiceProxy.getConsumableInfoUtil().getMaterialData(materialName);

			if (result.size() == 1 && film.getQuantity() == 0)
			{

				EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeassignMaterialToCST", getEventUser(), getEventComment(), null, null);
				eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

				SetEventInfo setEventInfoConsu = new SetEventInfo();
				setEventInfoConsu.getUdfs().put("CARRIERNAME", StringUtils.EMPTY);

				// Film Update
				MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(materialName, setEventInfoConsu, eventInfo);

				// FilmCST Update
				kr.co.aim.greentrack.durable.management.info.MakeNotInUseInfo makeNotInUseInfo = new kr.co.aim.greentrack.durable.management.info.MakeNotInUseInfo();
				MESDurableServiceProxy.getDurableServiceImpl().makeNotInUse(durableData, makeNotInUseInfo, eventInfo);

				eventLog.info("Error occurred - DeassignMaterialToCST Success ,FilmBox is" + carrierName);
			}
		}
		catch (Exception e)
		{
			eventLog.info("Error occurred - DeassignMaterialToCST Fail,FilmBox is " + carrierName);
		}

		eventLog.info("DeassignMaterialToCST End ,FilmBox is" + carrierName);
	}

}
