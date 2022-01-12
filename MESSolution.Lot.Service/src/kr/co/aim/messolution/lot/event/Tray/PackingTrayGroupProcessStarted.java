package kr.co.aim.messolution.lot.event.Tray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

public class PackingTrayGroupProcessStarted extends AsyncHandler 
{
	private static Log log = LogFactory.getLog(PackingTrayGroupProcessStarted.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
		String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
		String coverTrayName = SMessageUtil.getBodyItemValue(doc, "COVERTRAYNAME", true);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);
		String workType = "";
		
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		Durable trayGroupData = MESDurableServiceProxy.getDurableServiceUtil().getDurableDataForUpdate(coverTrayName);
		List<Durable> trayList = MESDurableServiceProxy.getDurableServiceUtil().getSubTrayListByCoverTray(coverTrayName, false);
		List<Lot> lotDataList = new ArrayList<Lot>();
		
		if(trayList != null && trayList.size() > 0)
		{
			List<Lot> gradeCheckLot = MESLotServiceProxy.getLotInfoUtil().getLotListBydurableName(trayList.get(0).getKey().getDurableName());
			if(StringUtil.equals(gradeCheckLot.get(0).getLotGrade(), "S"))
			{
				workType = "S";
				lotDataList = MESLotServiceProxy.getLotServiceUtil().getLotListByTrayList(trayList, false);
			}
			else
			{
				workType = "G";
			}
		}
		
		CommonValidation.checkMachineHold(machineData);
		CommonValidation.CheckDurableState(trayGroupData);
		CommonValidation.CheckDurableHoldState(trayGroupData);
		
		if(!trayGroupData.getDurableType().equals("CoverTray"))
			throw new CustomException("DURABLE-0009", coverTrayName);
		
		// Deassign Tray from TrayGroup
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeassignTrayGroup", getEventUser(), getEventComment());
		
		List<Durable> updateDeassignTrayGroupList = new ArrayList<>();
		List<DurableHistory> updateDeassignTrayGroupHistList = new ArrayList<>();
		
		if(StringUtil.equals(workType, "G") || StringUtil.equals(workType, "S"))
		{
			for (Durable durableData : trayList)
			{
				CommonValidation.CheckDurableState(durableData);
				CommonValidation.CheckDurableHoldState(durableData);
				// CommonValidation.CheckDurableCleanState(durableData);

				Durable oldDurableData = (Durable) ObjectUtil.copyTo(durableData);

				// Deassign Tray group
				durableData.getUdfs().put("COVERNAME", "");
				durableData.getUdfs().put("POSITION", "");
				if(StringUtil.equals(workType, "S"))
				{
					durableData.setDurableState(constantMap.Dur_Available);
					durableData.setLotQuantity(0);
					durableData.getUdfs().put("BCRFLAG", "N");
				}
				durableData.setLastEventName(eventInfo.getEventName());
				durableData.setLastEventTimeKey(eventInfo.getEventTimeKey());
				durableData.setLastEventTime(eventInfo.getEventTime());
				durableData.setLastEventUser(eventInfo.getEventUser());
				durableData.setLastEventComment(eventInfo.getEventComment());

				DurableHistory durableHist = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldDurableData, durableData, new DurableHistory());

				updateDeassignTrayGroupList.add(durableData);
				updateDeassignTrayGroupHistList.add(durableHist);
			}
			
			if (updateDeassignTrayGroupList.size() > 0)
			{
				try
				{
					CommonUtil.executeBatch("update", updateDeassignTrayGroupList, true);
					CommonUtil.executeBatch("insert", updateDeassignTrayGroupHistList, true);

					log.info(String.format("▶Successfully update %s pieces of trays data。(From Port: %s )", updateDeassignTrayGroupList.size(), portType));
				}
				catch (Exception e)
				{
					log.error(e.getMessage());
					throw new CustomException(e.getCause());
				}
			}
		}
		
		if(StringUtil.equals(workType, "S"))
		{
			//Panel
			List<Lot> updateLotArgList = new ArrayList<Lot>();
			List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();
			
			for (Lot lot : lotDataList)
			{
				Lot oldLot = (Lot) ObjectUtil.copyTo(lot);
				
				CommonValidation.checkLotProcessStateWait(lot);
				CommonValidation.checkLotStateScrapped(lot);
				
				lot.setCarrierName("");
				lot.setMachineName(machineName);
				lot.setLastEventName(eventInfo.getEventName());
				lot.setLastEventTime(eventInfo.getEventTime());
				lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
				lot.setLastEventComment(eventInfo.getEventComment());
				lot.setLastEventUser(eventInfo.getEventUser());
				
				Map<String, String> lotUdf = new HashMap<>();
				lotUdf.put("PORTNAME", portName);
				lotUdf.put("PORTTYPE", portType);
				lotUdf.put("PORTUSETYPE", portUseType);
				lotUdf.put("POSITION", "");
				lot.setUdfs(lotUdf);

				LotHistory lotHistory = new LotHistory();
				lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
				
				updateLotArgList.add(lot);
				updateLotHistoryList.add(lotHistory);
			}
			
			//Update PanelInfo
			if(updateLotArgList.size() > 0)
			{
				log.debug("Insert Lot, LotHistory");
				try
				{
					CommonUtil.executeBatch("update", updateLotArgList, true);
					CommonUtil.executeBatch("insert", updateLotHistoryList, true);
				}
				catch (Exception e)
				{
					log.error(e.getMessage());
					throw new CustomException(e.getCause());
				}
			}
			
			// TrayGroup initialize
			trayGroupData.setDurableType("Tray");
			trayGroupData.setLotQuantity(0);
			trayGroupData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
			DurableServiceProxy.getDurableService().update(trayGroupData);
			
			SetEventInfo setEventInfo = new SetEventInfo();
			
			setEventInfo.getUdfs().put("COVERNAME", "");
			setEventInfo.getUdfs().put("POSITION", "");
			setEventInfo.getUdfs().put("BCRFLAG", "N");
			setEventInfo.getUdfs().put("DURABLETYPE1", "Tray");
			
			DurableServiceProxy.getDurableService().setEvent(trayGroupData.getKey(), eventInfo, setEventInfo);
		}
		

	}
}
