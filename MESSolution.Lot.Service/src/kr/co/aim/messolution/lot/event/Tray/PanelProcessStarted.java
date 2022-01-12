package kr.co.aim.messolution.lot.event.Tray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EnumInfoUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameErrorSignal;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

public class PanelProcessStarted extends AsyncHandler 
{
	private static Log log = LogFactory.getLog(PanelProcessStarted.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String panelName = SMessageUtil.getBodyItemValue(doc, "PANELNAME", true);
		String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		String trayName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);

		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		String RecipeName =getMachineRecipeNameByLotList(panelName, machineName);
		
		Durable trayData = null;
		if(!StringUtil.equals(machineData.getMachineGroupName(), constMap.MachineGroup_SCRAPPACK))
		{
			trayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableDataForUpdate(trayName);
		}
		
		CommonValidation.checkMachineHold(machineData);
		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotDataForUpdate(panelName);
		if(!StringUtil.equals(machineData.getMachineGroupName(), constMap.MachineGroup_SCRAPPACK))
		{
			CommonValidation.checkLotState(lotData);
		}
		CommonValidation.checkLotProcessState(lotData);
		CommonValidation.checkLotHoldState(lotData);
		
		boolean deassignFlag = false;
		if(StringUtil.isNotEmpty(lotData.getCarrierName()))
			deassignFlag = true;
		if(!StringUtil.equals(machineData.getMachineGroupName(), constMap.MachineGroup_SCRAPPACK)&&deassignFlag)
		{
			CommonValidation.CheckDurableState(trayData);
			CommonValidation.CheckDurableHoldState(trayData);
			//CommonValidation.CheckDurableCleanState(trayData);
		}
		
		// TrackIn Panel 
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackIn", this.getEventUser(), this.getEventComment());
		Lot oldLotData = (Lot) ObjectUtil.copyTo(lotData);

		if(deassignFlag)
		{
			if (!lotData.getCarrierName().equals(trayName)&&!EnumInfoUtil.SorterOperationCondition.BRMA.getOperationMode().equals(machineData.getUdfs().get("OPERATIONMODE")))
			{
				//TRAY-0023:BC report message error: reported panel [{0}] does not belong to tray [{1}].
				throw new CustomException("TRAY-0023", panelName, trayName);
			}
			
			lotData.setCarrierName("");
			lotData.getUdfs().put("POSITION", "");
		}
		
		if(!StringUtil.equals(machineData.getMachineGroupName(), constMap.MachineGroup_SCRAPPACK))
		{
			lotData.setLotState(constMap.Lot_Released);
		}
		lotData.setLotProcessState(constMap.Lot_LoggedIn);
		lotData.setMachineName(machineName);
		lotData.setMachineRecipeName(machineRecipeName);
		lotData.setLastLoggedInTime(eventInfo.getEventTime());
		lotData.setLastLoggedInUser(eventInfo.getEventUser());
		lotData.setLastEventName(eventInfo.getEventName());
		lotData.setLastEventUser(eventInfo.getEventUser());
        lotData.setLastEventFlag(constMap.FLAG_N);
        lotData.setLastEventTime(eventInfo.getEventTime());
        lotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
        lotData.setLastEventComment(eventInfo.getEventComment());
        
        lotData.getUdfs().put("PORTNAME", portData.getKey().getPortName());
        lotData.getUdfs().put("PORTTYPE", portData.getUdfs().get("PORTTYPE"));
        lotData.getUdfs().put("PORTUSETYPE", portData.getUdfs().get("PORTUSETYPE"));
		
        LotHistory lotHist = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLotData, lotData, new LotHistory());
        
        LotServiceProxy.getLotService().update(lotData);
        LotServiceProxy.getLotHistoryService().insert(lotHist);
        
        // BRMA Mode : panel already deassigned from tray
        if(EnumInfoUtil.SorterOperationCondition.BRMA.getOperationMode().equals(machineData.getUdfs().get("OPERATIONMODE"))) return;
        
		// Deassign panel from tray
        
        if(deassignFlag)
        {
        	eventInfo.setEventName("DeassignPanel");
    		Durable oldDataInfo = (Durable) ObjectUtil.copyTo(trayData);

    		long lotQty = trayData.getLotQuantity() - 1;

    		if (lotQty >= 0)
    		{
    			trayData.setLotQuantity(lotQty);
    		}
    		else
    		{
				// TRAY-0024: The tray LotQty does not match with the actual quantity of panels.
				throw new CustomException("TRAY-0024");
    		}
    		
    		if (lotQty == 0)
    			trayData.setDurableState(constMap.Dur_Available);
    		
    		trayData.setLastEventName(eventInfo.getEventName());
    		trayData.setLastEventTimeKey(eventInfo.getEventTimeKey());
    		trayData.setLastEventTime(eventInfo.getEventTime());
    		trayData.setLastEventUser(eventInfo.getEventUser());
    		trayData.setLastEventComment(eventInfo.getEventComment());

    		DurableHistory durHistory = new DurableHistory();
    		durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldDataInfo, trayData, durHistory);

    		DurableServiceProxy.getDurableService().update(trayData);
    		DurableServiceProxy.getDurableHistoryService().insert(durHistory);
        }
	}
	public static String getMachineRecipeNameByLotList(String lotName, String machineName) throws CustomException
	{
		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotDataForUpdate(lotName);
		String sql = " SELECT DISTINCT P.MACHINERECIPENAME"
				   + " FROM LOT L, TPFOPOLICY T,POSMACHINE P"
				   + " WHERE 1=1 AND L.LOTNAME=:LOTNAME "
				   + " AND L.FACTORYNAME = T.FACTORYNAME "
				   + " AND L.PRODUCTSPECNAME = T.PRODUCTSPECNAME "
				   + " AND L.PRODUCTSPECVERSION = T.PRODUCTSPECVERSION"
				   + " AND L.PROCESSFLOWNAME = T.PROCESSFLOWNAME "
				   + " AND L.PROCESSFLOWVERSION = T.PROCESSFLOWVERSION"
				   + " AND L.PROCESSOPERATIONNAME = T.PROCESSOPERATIONNAME "
				   + " AND L.PROCESSOPERATIONVERSION = T.PROCESSOPERATIONVERSION "
				   + " AND T.CONDITIONID = P.CONDITIONID "
				   + " AND P.MACHINENAME = :MACHINENAME ";
		
		Map<String,Object> bindMap = new HashMap<>();
		bindMap.put("LOTNAME", lotName);
		bindMap.put("MACHINENAME", machineName);

		List<Map<String,Object>> resultList = null;
		
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		}
		catch (Exception ex)
		{
			if (ex instanceof greenFrameErrorSignal)
				throw new CustomException("SYS-9999", "POSMachine", ex.getMessage());
			else
				throw new CustomException(ex.getCause());
		}

		if (resultList == null || resultList.size() == 0)
		{
			//RMS-042: No recipe registered on machine [{0}] was found.Policy condition by [T= {1},P= {2},F= {3},O= {4}]
			throw new CustomException("RMS-042", machineName,lotData.getFactoryName(),lotData.getProductSpecName(),lotData.getProcessFlowName(),lotData.getProcessOperationName());
		}
		else if (resultList.size() > 1)
		{
			//RMS-043 : The materials to be prepared for operation on machine[{0}] include multiple recieps.
			throw new CustomException("RMS-043", machineName);
		}
		else if (ConvertUtil.getMapValueByName(resultList.get(0), "MACHINERECIPENAME").isEmpty())
		{
			//RMS-044: There is wrong recipe registration information on the machine [{0}], the value is empty or null.Policy condition by [T= {1},P= {2},F= {3},O= {4}]
			throw new CustomException("RMS-044", machineName,lotData.getFactoryName(),lotData.getProductSpecName(),lotData.getProcessFlowName(),lotData.getProcessOperationName());
		}
		
		return String.valueOf(resultList.get(0).get("MACHINERECIPENAME"));
	}
}
