package kr.co.aim.messolution.lot.event.Tray;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.machine.management.data.Machine;

public class TrayGroupScrapReport extends AsyncHandler 
{
	private static Log log = LogFactory.getLog(TrayGroupScrapReport.class);

	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String trayGroupName = SMessageUtil.getBodyItemValue(doc, "TRAYGROUPNAME", true);
		String scrapCode = SMessageUtil.getBodyItemValue(doc, "SCRAPCODE", false);
		String scrapText = SMessageUtil.getBodyItemValue(doc, "SCRAPTEXT", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrayGroupScrap", this.getEventUser(), this.getEventComment() + ":" + scrapText, null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayGroupName);

		List<Durable> trayDataList = MESDurableServiceProxy.getDurableServiceUtil().getSubTrayListByCoverTray(trayGroupName,true);

		if (trayDataList == null || trayDataList.size()==0 )
		{
			// TRAY-0009:No tray data was found for the {0} tray group.
			new CustomException("TRAY-0009", trayGroupName);
		}

		List<Lot> lotDataList = MESLotServiceProxy.getLotServiceUtil().getLotListByTrayList(trayDataList,true);

		// DURABLE-9004:No panel assigned to TrayGroup[{0}]
		if (lotDataList == null || lotDataList.size() == 0)
			throw new CustomException("DURABLE-9004", trayGroupName);

		List<Lot> updateLotList = new ArrayList<>(lotDataList.size());
		List<LotHistory> updateHistList = new ArrayList<>(lotDataList.size());

		for (Lot lotData : lotDataList)
		{
			Lot oldLot = (Lot) ObjectUtil.copyTo(lotData);

			lotData.setLotState(GenericServiceProxy.getConstantMap().Lot_Scrapped);
			lotData.setLotGrade("S");
			lotData.setMachineName(machineName);
			lotData.setReasonCode(scrapCode);
			lotData.setLastEventName(eventInfo.getEventName());
			lotData.setLastEventTime(eventInfo.getEventTime());
			lotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lotData.setLastEventUser(eventInfo.getEventUser());
			lotData.setLastEventComment(eventInfo.getEventComment());

			LotHistory lotHist = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lotData, new LotHistory());

			updateLotList.add(lotData);
			updateHistList.add(lotHist);
		}

		try
		{
			CommonUtil.executeBatch("update", updateLotList, true);
			CommonUtil.executeBatch("insert", updateHistList, true);

			log.info(String.format("▶Successfully scrap %s pieces of panels.", updateLotList.size()));
		}
		catch (Exception e)
		{
			log.error(e.getMessage());
			throw new CustomException(e.getCause());
		}

	}
}