package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupHistory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class SplitBox extends SyncHandler {

	private static Log log = LogFactory.getLog(SplitBox.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		// Get Doc
		List<Element> srcLotList = SMessageUtil.getBodySequenceItemList(doc, "SRCLOTLIST", false);
		List<Element> desLotList = SMessageUtil.getBodySequenceItemList(doc, "DESLOTLIST", false);
		String desProcessGroupName = SMessageUtil.getBodyItemValue(doc, "DESPROCESSGROUPNAME", true);
		String srcProcessGroupName = SMessageUtil.getBodyItemValue(doc, "SRCPROCESSGROUPNAME", true);

		int srcLotQty = srcLotList.size();
		int desLotQty = desLotList.size();

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		// EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("SplitBox", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

		// Make Panel
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();

		for (Element lotData : desLotList)
		{
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getChildText("LOTNAME"));
			Lot oldLot = (Lot) ObjectUtil.copyTo(lot);

			List<Object> lotBindList = new ArrayList<Object>();
			lotBindList.add(eventInfo.getEventName());
			lotBindList.add(eventInfo.getEventTimeKey());
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(eventInfo.getEventComment());
			lotBindList.add(constantMap.Flag_N);
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(desProcessGroupName);
			lotBindList.add(lot.getKey().getLotName());

			updateLotArgList.add(lotBindList.toArray());

			// History
			lot.setLastLoggedInTime(eventInfo.getEventTime());
			lot.setLastLoggedInUser(eventInfo.getEventUser());
			lot.setLastLoggedOutTime(eventInfo.getEventTime());
			lot.setLastLoggedOutUser(eventInfo.getEventUser());
			lot.setProcessGroupName(desProcessGroupName);
			lot.setLastEventName(eventInfo.getEventName());
			lot.setLastEventTime(eventInfo.getEventTime());
			lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lot.setLastEventComment(eventInfo.getEventComment());
			lot.setLastEventUser(eventInfo.getEventUser());

			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
			
			updateLotHistoryList.add(lotHistory);
		}

		updateLotData(eventInfo, updateLotArgList, updateLotHistoryList);

		srcLotQty = getLotQuantity(srcProcessGroupName);
		desLotQty = getLotQuantity(desProcessGroupName);

		// SourceBox
		ProcessGroup processGroup = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getProcessGroupData(srcProcessGroupName);
		ProcessGroup oldProcessGroup = (ProcessGroup) ObjectUtil.copyTo(processGroup);
		processGroup.setMaterialQuantity(srcLotQty);
		processGroup.setLastEventName(eventInfo.getEventName());
		processGroup.setLastEventTimeKey(eventInfo.getEventTimeKey());
		processGroup.setLastEventTime(eventInfo.getEventTime());
		processGroup.setLastEventUser(eventInfo.getEventUser());
		processGroup.setLastEventComment(eventInfo.getEventComment());
		processGroup.setLastEventFlag("N");

		ProcessGroupHistory processGroupHistory = new ProcessGroupHistory();
		processGroupHistory = ProcessGroupServiceProxy.getProcessGroupHistoryDataAdaptor().setHV(oldProcessGroup, processGroup, processGroupHistory);
		ProcessGroupServiceProxy.getProcessGroupService().update(processGroup);
		ProcessGroupServiceProxy.getProcessGroupHistoryService().insert(processGroupHistory);

		// TargetBox
		ProcessGroup desProcessGroup = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getProcessGroupData(desProcessGroupName);
		ProcessGroup desOldProcessGroup = (ProcessGroup) ObjectUtil.copyTo(desProcessGroup);
		desProcessGroup.setMaterialQuantity(desLotQty);
		desProcessGroup.setLastEventName(eventInfo.getEventName());
		desProcessGroup.setLastEventTimeKey(eventInfo.getEventTimeKey());
		desProcessGroup.setLastEventTime(eventInfo.getEventTime());
		desProcessGroup.setLastEventUser(eventInfo.getEventUser());
		desProcessGroup.setLastEventComment(eventInfo.getEventComment());
		desProcessGroup.setLastEventFlag("N");

		ProcessGroupHistory desProcessGroupHistory = new ProcessGroupHistory();
		desProcessGroupHistory = ProcessGroupServiceProxy.getProcessGroupHistoryDataAdaptor().setHV(desOldProcessGroup, desProcessGroup, desProcessGroupHistory);
		ProcessGroupServiceProxy.getProcessGroupService().update(desProcessGroup);
		ProcessGroupServiceProxy.getProcessGroupHistoryService().insert(desProcessGroupHistory);
		return doc;
	}

	private void updateLotData(EventInfo eventInfo, List<Object[]> updateLotArgList, List<LotHistory> updateLotHistoryList) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE LOT ");
		sql.append("   SET LASTEVENTNAME = ?, ");
		sql.append("       LASTEVENTTIMEKEY = ?, ");
		sql.append("       LASTEVENTTIME = ?, ");
		sql.append("       LASTEVENTUSER = ?, ");
		sql.append("       LASTEVENTCOMMENT = ?, ");
		sql.append("       LASTEVENTFLAG = ?, ");
		sql.append("       LASTLOGGEDINTIME = ?, ");
		sql.append("       LASTLOGGEDINUSER = ?, ");
		sql.append("       LASTLOGGEDOUTTIME = ?, ");
		sql.append("       LASTLOGGEDOUTUSER = ?, ");
		sql.append("       PROCESSGROUPNAME = ? ");
		sql.append(" WHERE LOTNAME = ? ");

		MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), updateLotArgList);
		try 
		{
			CommonUtil.executeBatch("insert", updateLotHistoryList);
		} 
		catch (Exception e) 
		{
			log.error(e.getMessage());
			throw new CustomException(e.getCause());
		}
	}

	private int getLotQuantity(String processGroupName)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT COUNT (*) LOTQUANTITY ");
		sql.append("  FROM LOT ");
		sql.append(" WHERE PROCESSGROUPNAME = :PROCESSGROUPNAME ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("PROCESSGROUPNAME", processGroupName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		int lotQuantity = 0;

		if (result.size() > 0)
			lotQuantity = Integer.parseInt(ConvertUtil.getMapValueByName(result.get(0), "LOTQUANTITY"));

		return lotQuantity;
	}

}
