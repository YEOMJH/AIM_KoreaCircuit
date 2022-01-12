package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class TrayGroupDeassign extends SyncHandler {
	private static Log log = LogFactory.getLog(TrayGroupDeassign.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String coverName = SMessageUtil.getBodyItemValue(doc, "COVERNAME", true);
		String coverDurableType = SMessageUtil.getBodyItemValue(doc, "DURABLETYPE", true);
		String coverPosition = SMessageUtil.getBodyItemValue(doc, "COVERPOSITION", false);
		List<Element> trayListEl = SMessageUtil.getBodySequenceItemList(doc, "TRAYLIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);

		Durable coverTrayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(coverName);

		CommonValidation.CheckDurableState(coverTrayData);

		// Deassign Case

		eventInfo = EventInfoUtil.makeEventInfo("Deassign", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		List<Durable> trayList = MESDurableServiceProxy.getDurableServiceUtil().getTrayListByCoverName(coverName);

		for (Durable durable : trayList)
		{
			String trayName = durable.getKey().getDurableName();

			Durable trayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
			CommonValidation.CheckDurableState(trayData);
			CommonValidation.CheckDurableHoldState(trayData);

			List<Object[]> updateLotArgList = new ArrayList<Object[]>();
			List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();

			log.info("Get LotNameList Start");
			List<List<Map<String, Object>>> lotnameList = deassignTray(eventInfo, trayName);
			log.info("Get LotNameList End");

			if (lotnameList != null && lotnameList.size() > 0)
			{
				Map<String, String> udfs = new HashMap<String, String>();

				for (List<Map<String, Object>> lotData : lotnameList)
				{
					for (Map<String, Object> map : lotData)
					{
						String lotName = ConvertUtil.getMapValueByName(map, "LOTNAME");

						Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
						Lot oldLot = (Lot) ObjectUtil.copyTo(lot);

						List<Object> lotBindList = new ArrayList<Object>();
						lotBindList.add(eventInfo.getEventName());
						lotBindList.add(eventInfo.getEventTimeKey());
						lotBindList.add(eventInfo.getEventTime());
						lotBindList.add(eventInfo.getEventUser());
						lotBindList.add("");
						lotBindList.add("");
						lotBindList.add(lot.getKey().getLotName());
						updateLotArgList.add(lotBindList.toArray());

						// History
						lot.setLastLoggedInTime(eventInfo.getEventTime());
						lot.setLastLoggedInUser(eventInfo.getEventUser());
						lot.setLastEventName(eventInfo.getEventName());
						lot.setLastEventTime(eventInfo.getEventTime());
						lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
						lot.setLastEventComment(eventInfo.getEventComment());
						lot.setLastEventUser(eventInfo.getEventUser());
						lot.setCarrierName("");

						udfs.put("POSITION", "");
						lot.setUdfs(udfs);

						LotHistory lotHistory = new LotHistory();
						lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
						
						updateLotHistoryList.add(lotHistory);

					}
				}

				StringBuilder sqlLot = new StringBuilder();
				sqlLot.append("UPDATE LOT ");
				sqlLot.append("   SET LASTEVENTNAME = ?, ");
				sqlLot.append("       LASTEVENTTIMEKEY = ?, ");
				sqlLot.append("       LASTEVENTTIME = ?, ");
				sqlLot.append("       LASTEVENTUSER = ?, ");
				sqlLot.append("       POSITION = ?, ");
				sqlLot.append("       CARRIERNAME = ? ");
				sqlLot.append(" WHERE LOTNAME = ? ");

				log.info("Update LotNameList Start");
				MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlLot.toString(), updateLotArgList);
				log.info("Update LotNameList End");
				
				log.info("Insert LotHistory Start");
				try 
				{
					CommonUtil.executeBatch("insert", updateLotHistoryList);
				} 
				catch (Exception e) 
				{
					log.error(e.getMessage());
					throw new CustomException(e.getCause());
				}
				log.info("Insert LotHistory End");
			}

			eventInfo.setEventName("DeassignPanel");

			Map<String, String> udfs = new HashMap<>();

			udfs.put("COVERNAME", "");
			udfs.put("POSITION", "");
			udfs.put("BCRFLAG", "N");
			udfs.put("DURABLETYPE1", "Tray");

			Durable oldDurableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
			Durable durableData = (Durable) ObjectUtil.copyTo(oldDurableInfo);
			DurableSpec durableSpecData = GenericServiceProxy.getSpecUtil().getDurableSpec(durableData.getFactoryName(), durableData.getDurableSpecName(),
					durableData.getDurableSpecVersion());
			
			durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
			durableData.setLotQuantity(0);
			durableData.setCapacity(durableSpecData.getDefaultCapacity());;
			durableData.setLastEventName(eventInfo.getEventName());
			durableData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			durableData.setLastEventTime(eventInfo.getEventTime());
			durableData.setLastEventUser(eventInfo.getEventUser());
			durableData.setLastEventComment(eventInfo.getEventComment());
			durableData.setUdfs(udfs);

			DurableHistory durHistory = new DurableHistory();
			durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldDurableInfo, durableData, durHistory);

			log.info("Update TrayInfo Start");
			DurableServiceProxy.getDurableService().update(durableData);
			log.info("Update TrayInfo End");
			log.info("Insert TrayHistory Start");
			DurableServiceProxy.getDurableHistoryService().insert(durHistory);
			log.info("Insert TrayHistory End");
		}

		// Durable - CoverTray
		Durable oldCoverTrayInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(coverName);
		Durable coverTrayInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(coverName);
		DurableSpec durableSpecData = GenericServiceProxy.getSpecUtil().getDurableSpec(coverTrayInfo.getFactoryName(), coverTrayInfo.getDurableSpecName(),
				coverTrayInfo.getDurableSpecVersion());
		CommonValidation.CheckDurableState(coverTrayInfo);
		CommonValidation.CheckDurableHoldState(coverTrayInfo);

		eventInfo.setEventName("DeassignTray");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

		Map<String, String> udfs = new HashMap<>();

		udfs.put("COVERNAME", "");
		udfs.put("POSITION", "");
		udfs.put("BCRFLAG", "N");
		udfs.put("DURABLETYPE1", "Tray");
		coverTrayInfo.setDurableType("Tray");
		coverTrayInfo.setLotQuantity(0);
		coverTrayInfo.setCapacity(durableSpecData.getDefaultCapacity());
		coverTrayInfo.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
		coverTrayInfo.setLastEventName(eventInfo.getEventName());
		coverTrayInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
		coverTrayInfo.setLastEventTime(eventInfo.getEventTime());
		coverTrayInfo.setLastEventUser(eventInfo.getEventUser());
		coverTrayInfo.setLastEventComment(eventInfo.getEventComment());
		coverTrayInfo.setUdfs(udfs);

		DurableHistory durHistory = new DurableHistory();
		durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldCoverTrayInfo, coverTrayInfo, durHistory);

		DurableServiceProxy.getDurableService().update(coverTrayInfo);
		DurableServiceProxy.getDurableHistoryService().insert(durHistory);

		return doc;
	}

	private List<List<Map<String, Object>>> deassignTray(EventInfo eventInfo, String trayName) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT L.LOTNAME ");
		sql.append("  FROM DURABLE DR, LOT L ");
		sql.append(" WHERE DR.DURABLENAME = L.CARRIERNAME ");
		sql.append("   AND DR.FACTORYNAME = L.FACTORYNAME ");
		sql.append("   AND DR.DURABLESTATE = 'InUse' ");
		sql.append("   AND DR.DURABLETYPE = 'Tray' ");
		sql.append("   AND DR.COVERNAME IS NOT NULL ");
		sql.append("   AND L.LOTPROCESSSTATE = 'WAIT' ");
		sql.append("   AND L.LOTHOLDSTATE = 'N' ");
		sql.append("   AND L.LOTSTATE IN ('Released','Scrapped') ");
		sql.append("   AND L.CARRIERNAME = :CARRIERNAME ");

		List<List<Map<String, Object>>> lotNameList = new ArrayList<List<Map<String, Object>>>();

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("CARRIERNAME", trayName);

		// getLotData
		List<Map<String, Object>> lotNames = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		lotNameList.add(lotNames);
		return lotNameList;

	}

}
