package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class MoveTray extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String sourceTrayGroupName = SMessageUtil.getBodyItemValue(doc, "SOURCETRAYGROUPNAME", true);
		String sourceCoverPosition = SMessageUtil.getBodyItemValue(doc, "SOURCECOVERPOSITION", true);
		String destTrayGroupName = SMessageUtil.getBodyItemValue(doc, "DESTTRAYGROUPNAME", true);
		String destCoverPosition = SMessageUtil.getBodyItemValue(doc, "DESTCOVERPOSITION", true);
		List<Element> trayList = SMessageUtil.getBodySequenceItemList(doc, "TRAYLIST", true);

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		// EventInfo Deassign
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeassignTrayGroup", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

		String sqlDeassignTrayGroup = "UPDATE DURABLE SET POSITION = ?, COVERNAME=?, LASTEVENTNAME=?, LASTEVENTTIMEKEY=?, LASTEVENTTIME=?, LASTEVENTUSER=?, LASTEVENTCOMMENT=?, DURABLETYPE1=? "
				+ "WHERE DURABLENAME = ? ";

		List<Object[]> updateDurArgListDeassign = new ArrayList<Object[]>();
		List<Durable> updateTrayListDeassign = new ArrayList<Durable>();
		List<Durable> updateOldTrayListDeassign = new ArrayList<Durable>();
		List<Durable> trayDataForTrayGroup = new ArrayList<Durable>();

		int lotQty = 0;

		for (Element trayData : trayList)
		{

			Durable oldDurData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayData.getChildText("DURABLENAME"));
			Durable durData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayData.getChildText("DURABLENAME"));
			CommonValidation.CheckDurableState(oldDurData);

			if (!sourceTrayGroupName.equals(durData.getUdfs().get("COVERNAME")))
			{
				throw new CustomException("DURABLE-9009", durData.getUdfs().get("COVERNAME").toString());
			}

			lotQty += durData.getLotQuantity();

			// Durable
			List<Object> durBindList = new ArrayList<Object>();
			durBindList.add("");
			durBindList.add("");
			durBindList.add(eventInfo.getEventName());
			durBindList.add(eventInfo.getEventTimeKey());
			durBindList.add(eventInfo.getEventTime());
			durBindList.add(eventInfo.getEventUser());
			durBindList.add(eventInfo.getEventComment());
			durBindList.add("Tray");
			durBindList.add(trayData.getChildText("DURABLENAME"));

			updateDurArgListDeassign.add(durBindList.toArray());

			// History

			Map<String, String> durUdfs = new HashMap<>();
			durUdfs = durData.getUdfs();
			durUdfs.put("POSITION", "");
			durUdfs.put("COVERNAME", "");
			durUdfs.put("DURABLETYPE1", "Tray");
			durData.setUdfs(durUdfs);

			updateTrayListDeassign.add(durData);
			updateOldTrayListDeassign.add(oldDurData);
		}

		MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlDeassignTrayGroup, updateDurArgListDeassign);
		MESDurableServiceProxy.getDurableServiceUtil().insertDurableHistory(eventInfo, updateTrayListDeassign, updateOldTrayListDeassign);

		// Update CoverTray Deassign
		Durable sourceTrayGroup = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sourceTrayGroupName);
		Durable oldSourceTrayGroup = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sourceTrayGroupName);
		CommonValidation.CheckDurableState(sourceTrayGroup);

		sourceTrayGroup.setLotQuantity(sourceTrayGroup.getLotQuantity() - lotQty);
		sourceTrayGroup.setLastEventName(eventInfo.getEventName());
		sourceTrayGroup.setLastEventTimeKey(eventInfo.getEventTimeKey());
		sourceTrayGroup.setLastEventTime(eventInfo.getEventTime());
		sourceTrayGroup.setLastEventUser(eventInfo.getEventUser());
		sourceTrayGroup.setLastEventComment(eventInfo.getEventComment());

		Map<String, String> durUdfsDeassign = new HashMap<>();
		durUdfsDeassign = sourceTrayGroup.getUdfs();

		StringBuilder seqPosition = new StringBuilder();
		seqPosition.append("SELECT MAX(POSITION) ");
		seqPosition.append("  FROM DURABLE D ");
		seqPosition.append("  WHERE D.COVERNAME = :COVERNAME ");
		seqPosition.append("  AND D.DURABLETYPE ='Tray' ");
		seqPosition.append("  AND D.FACTORYNAME ='POSTCELL' ");

		Map<String, Object> bindMap1 = new HashMap<String, Object>();
		bindMap1.put("COVERNAME", sourceTrayGroupName);
		int maxPosition = GenericServiceProxy.getSqlMesTemplate().queryForInt(seqPosition.toString(), bindMap1);
		int coverTrayPosition = maxPosition + 1;
		String coverPositon = String.valueOf(coverTrayPosition);
		durUdfsDeassign.put("POSITION", coverPositon);

		StringBuilder seqTray = new StringBuilder();
		seqTray.append("SELECT DURABLENAME ");
		seqTray.append("  FROM DURABLE D ");
		seqTray.append("  WHERE D.COVERNAME = :COVERNAME ");
		seqTray.append("  AND D.DURABLETYPE ='Tray' ");
		seqTray.append("  AND D.FACTORYNAME ='POSTCELL' ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("COVERNAME", sourceTrayGroupName);
		List<Map<String, Object>> trayNameList = GenericServiceProxy.getSqlMesTemplate().queryForList(seqTray.toString(), bindMap);

		if (sourceTrayGroup.getLotQuantity() == 0 && trayNameList.size() == 0)
		// end
		{
			sourceTrayGroup.setDurableType("Tray");
			sourceTrayGroup.setDurableState("Available");
			durUdfsDeassign.put("POSITION", "");
			durUdfsDeassign.put("COVERNAME", "");
			durUdfsDeassign.put("DURABLETYPE1", "Tray");
		}

		sourceTrayGroup.setUdfs(durUdfsDeassign);

		DurableHistory durHistory = new DurableHistory();
		durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldSourceTrayGroup, sourceTrayGroup, durHistory);

		DurableServiceProxy.getDurableService().update(sourceTrayGroup);
		DurableServiceProxy.getDurableHistoryService().insert(durHistory);

		// Assign
		// EventInfo Assign
		eventInfo.setEventName("AssignTrayGroup");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

		List<Durable> updateTrayListAssign = new ArrayList<Durable>();
		List<Durable> updateOldTrayListAssign = new ArrayList<Durable>();
		List<Object[]> updateDurArgListAssign = new ArrayList<Object[]>();

		String sqlAssignTrayGroup = "UPDATE DURABLE SET POSITION = ?, COVERNAME=?, LASTEVENTNAME=?, LASTEVENTTIMEKEY=?, LASTEVENTTIME=?, LASTEVENTUSER=?, LASTEVENTCOMMENT=?, DURABLETYPE1=? "
				+ "WHERE DURABLENAME = ? ";

		for (Element trayData : trayList)
		{

			Durable oldDurData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayData.getChildText("DURABLENAME"));
			Durable durData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayData.getChildText("DURABLENAME"));

			String position = trayData.getChildText("POSITION");

			// Durable
			List<Object> durBindList = new ArrayList<Object>();
			durBindList.add(position);
			durBindList.add(destTrayGroupName);
			durBindList.add(eventInfo.getEventName());
			durBindList.add(eventInfo.getEventTimeKey());
			durBindList.add(eventInfo.getEventTime());
			durBindList.add(eventInfo.getEventUser());
			durBindList.add(eventInfo.getEventComment());
			durBindList.add("Tray");
			durBindList.add(trayData.getChildText("DURABLENAME"));

			updateDurArgListAssign.add(durBindList.toArray());

			// History
			Map<String, String> durUdfs = new HashMap<>();
			durUdfs = durData.getUdfs();
			durUdfs.put("POSITION", position);
			durUdfs.put("COVERNAME", destTrayGroupName);
			durUdfs.put("DURABLETYPE1", "Tray");

			durData.setUdfs(durUdfs);

			updateTrayListAssign.add(durData);
			updateOldTrayListAssign.add(oldDurData);
		}

		MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlAssignTrayGroup, updateDurArgListAssign);
		MESDurableServiceProxy.getDurableServiceUtil().insertDurableHistory(eventInfo, updateTrayListAssign, updateOldTrayListAssign);

		// Update CoverTray Assign
		Durable destTrayGroup = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(destTrayGroupName);
		Durable oldDestTrayGroup = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(destTrayGroupName);
		CommonValidation.CheckDurableState(destTrayGroup);

		destTrayGroup.setLotQuantity(destTrayGroup.getLotQuantity() + lotQty);
		destTrayGroup.setLastEventName(eventInfo.getEventName());
		destTrayGroup.setLastEventTimeKey(eventInfo.getEventTimeKey());
		destTrayGroup.setLastEventTime(eventInfo.getEventTime());
		destTrayGroup.setLastEventUser(eventInfo.getEventUser());
		destTrayGroup.setLastEventComment(eventInfo.getEventComment());

		Map<String, String> durUdfsAssign = new HashMap<>();
		durUdfsAssign = destTrayGroup.getUdfs();
		durUdfsAssign.put("POSITION", destCoverPosition);
		durUdfsAssign.put("DURABLETYPE1", destTrayGroup.getDurableType());
		destTrayGroup.setUdfs(durUdfsAssign);

		DurableHistory durHistoryAssign = new DurableHistory();
		durHistoryAssign = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldDestTrayGroup, destTrayGroup, durHistoryAssign);

		DurableServiceProxy.getDurableService().update(destTrayGroup);
		DurableServiceProxy.getDurableHistoryService().insert(durHistoryAssign);

		return doc;
	}
}