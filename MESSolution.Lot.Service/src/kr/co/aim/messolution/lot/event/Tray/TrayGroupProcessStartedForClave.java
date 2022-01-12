package kr.co.aim.messolution.lot.event.Tray;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
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
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

public class TrayGroupProcessStartedForClave extends AsyncHandler 
{
	private static Log log = LogFactory.getLog(TrayGroupProcessStartedForClave.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
		String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
		String trayGroupName = SMessageUtil.getBodyItemValue(doc, "TRAYGROUPNAME", true);
		String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", true);
		
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		Durable trayGroupData = MESDurableServiceProxy.getDurableServiceUtil().getDurableDataForUpdate(trayGroupName);
		
		CommonValidation.checkMachineHold(machineData);
		CommonValidation.CheckDurableState(trayGroupData);
		CommonValidation.CheckDurableHoldState(trayGroupData);
		
		if(!trayGroupData.getDurableType().equals("CoverTray"))
			throw new CustomException("DURABLE-0009", trayGroupName);

		List<Durable> trayList = MESDurableServiceProxy.getDurableServiceUtil().getSubTrayListByCoverTray(trayGroupName,true);
		
		if (trayList == null || trayList.size()==0 )
		{
			// TRAY-0009:No tray data was found for the {0} tray group.
			new CustomException("TRAY-0009", trayGroupName);
		}
		
		// process start
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrayGroupProcessStarted", this.getEventUser(), this.getEventComment());
		EventInfo trackInEventInfo = EventInfoUtil.makeEventInfo("TrackIn", this.getEventUser(), this.getEventComment());
		
		List<Durable> updatTrayDataList = new ArrayList<>(trayList.size());
		List<DurableHistory> updatTrayHistList = new ArrayList<>(trayList.size());
		
		//sub tray list + cover tray
		trayList.add(trayGroupData);
		for (Durable durableData : trayList) 
		{
			CommonValidation.CheckDurableState(durableData);
			CommonValidation.CheckDurableHoldState(durableData);
			//CommonValidation.CheckDurableCleanState(durableData);

			Durable oldDurableData = (Durable) ObjectUtil.copyTo(durableData);

			durableData.getUdfs().put("MACHINENAME", machineName);
			durableData.getUdfs().put("PORTNAME", portName);
			durableData.setLastEventName(eventInfo.getEventName());
			durableData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			durableData.setLastEventTime(eventInfo.getEventTime());
			durableData.setLastEventUser(eventInfo.getEventUser());
			durableData.setLastEventComment(eventInfo.getEventComment());

			DurableHistory durableHist = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldDurableData, durableData, new DurableHistory());

			updatTrayDataList.add(durableData);
			updatTrayHistList.add(durableHist);
		}
		
		try
		{
			CommonUtil.executeBatch("update", updatTrayDataList, true);
			CommonUtil.executeBatch("insert", updatTrayHistList, true);
			
			log.info(String.format("▶Successfully update %s pieces of trays.", updatTrayDataList.size()));
		}
		catch (Exception e)
		{
			log.error(e.getMessage());
			throw new CustomException(e.getCause());
		}
		
		//all tray list - cover tray
		trayList.remove(trayList.size()-1);
		List<Lot> lotDataList = MESLotServiceProxy.getLotServiceUtil().getLotListByTrayList(trayList,true);

		// DURABLE-9004:No panel assigned to TrayGroup[{0}]
		if (lotDataList == null || lotDataList.size() <= 0)
			throw new CustomException("DURABLE-9004", trayGroupName);
		
		//panel common check
		panelCommonCheck(lotDataList, trayList);
		
		List<Lot> updateLotList = new ArrayList<>(lotDataList.size());
		List<LotHistory> updateHistList = new ArrayList<>(lotDataList.size());

		for (Lot lotData : lotDataList)
		{
			Lot oldLot = (Lot) ObjectUtil.copyTo(lotData);
			
			lotData.setMachineName(machineName);
			lotData.setMachineRecipeName(machineRecipeName);
			//panelCommonCheck  has Check  Lot_Released
			//lotData.setLotState(GenericServiceProxy.getConstantMap().Lot_Released);
			lotData.setLotProcessState(GenericServiceProxy.getConstantMap().Lot_LoggedIn);
			lotData.setLastLoggedInTime(trackInEventInfo.getEventTime());
			lotData.setLastLoggedInUser(trackInEventInfo.getEventUser());
			lotData.setLastEventName(trackInEventInfo.getEventName());
			lotData.setLastEventTimeKey(trackInEventInfo.getEventTimeKey());
			lotData.setLastEventTime(trackInEventInfo.getEventTime());
			lotData.setLastEventUser(trackInEventInfo.getEventUser());
			lotData.setLastEventComment(trackInEventInfo.getEventComment());

			lotData.getUdfs().put("PORTNAME", portData.getKey().getPortName());
			lotData.getUdfs().put("PORTTYPE", portData.getUdfs().get("PORTTYPE"));
			lotData.getUdfs().put("PORTUSETYPE", portData.getUdfs().get("PORTUSETYPE"));
			
			LotHistory lotHist = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lotData, new LotHistory());

			updateLotList.add(lotData);
			updateHistList.add(lotHist);
		}

		try
		{
			CommonUtil.executeBatch("update", updateLotList, true);
			CommonUtil.executeBatch("insert", updateHistList, true);

			log.info(String.format("▶Successfully update %s pieces of panels.", updateLotList.size()));
		}
		catch (Exception e)
		{
			log.error(e.getMessage());
			throw new CustomException(e.getCause());
		}
	}
	
	public void panelCommonCheck(List<Lot> lotDataList ,List<Durable> trayList) throws CustomException
	{
		Set set = new HashSet<>();
		
		for (Lot lotData : lotDataList)
		{
			CommonValidation.checkLotState(lotData);
			CommonValidation.checkLotProcessState(lotData);
			CommonValidation.checkLotHoldState(lotData);

			set.add(lotData.getCarrierName());
		}
		
		if(set.size() != trayList.size())
		throw new CustomException("SYY-0010", String.format("The tray group [CoverName =%s] contains empty trays.",trayList.get(0).getUdfs().get("COVERNAME")));
	}

}
