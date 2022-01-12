package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.management.impl.LotQueueTimeService;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;


public class TrayGroupHold extends SyncHandler {

	public static Log logger = LogFactory.getLog(TrayGroupHold.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String eventName = SMessageUtil.getBodyItemValue(doc, "EVENTNAME", true);
		
		String trayGroupName = SMessageUtil.getBodyItemValue(doc, "TRAYGROUPNAME", true);
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);
		String reasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", false);
		
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		Durable trayGroupData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayGroupName);
		
		EventInfo eventInfo = new EventInfo();
		
		if(eventName.equals("TrayGroupHold"))//caixu 2020/11/18 modify release to TrayGoupReleaseHold
		{
			eventInfo = EventInfoUtil.makeEventInfo("TrayGroupHold", this.getEventUser(), this.getEventComment(), "", "");
			CommonValidation.CheckDurableHoldState(trayGroupData);
		}
		else
		{
			eventInfo = EventInfoUtil.makeEventInfo("TrayGoupReleaseHold", this.getEventUser(), this.getEventComment(), "", "");
			CommonValidation.CheckDurableNotHoldState(trayGroupData);
		}
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		List<Durable> trayList = MESDurableServiceProxy.getDurableServiceUtil().getTrayListByCoverName(trayGroupName);
		List<Map<String, Object>> lotList = MESLotServiceProxy.getLotServiceUtil().getLotListByTrayGroup(trayGroupName);
		
		String queryStringDurable = "UPDATE DURABLE " +
				"SET DURABLEHOLDSTATE=?, REASONCODE=?, REASONCODETYPE=?, LASTEVENTNAME=?, LASTEVENTTIMEKEY=?, LASTEVENTTIME=?, LASTEVENTUSER=?, LASTEVENTCOMMENT=? " +
				"WHERE DURABLENAME = ? " ;
		
		String queryStringLot = "UPDATE LOT SET LOTHOLDSTATE = ?, LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, "
				+ "LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, REASONCODE = ?, REASONCODETYPE = ? WHERE LOTNAME = ?";
		
		List<Object[]> updateDurableArgList = new ArrayList<Object[]>();
		
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();
		if(trayList!=null&&trayList.size()>0)
		{
		//Durable	
			for(Durable trayData : trayList)
			{				
				Durable oldDurable = trayData;
				Map<String, String> durUdf = new HashMap<>();
				
				List<Object> durableBindList = new ArrayList<Object>();
				if(eventName.equals("TrayGroupHold"))
				{
					//CommonValidation.CheckDurableHoldState(trayData);// caixu 2020/11/18 delete Check
					durableBindList.add(constantMap.DURABLE_HOLDSTATE_Y);
					trayData.setReasonCode(reasonCode);
					trayData.setReasonCodeType(reasonCodeType);
					durUdf.put("DURABLEHOLDSTATE", constantMap.DURABLE_HOLDSTATE_Y);
				}
				else
				{
					//CommonValidation.CheckDurableNotHoldState(trayData);// caixu 2020/11/18 delete Check
					durableBindList.add(constantMap.DURABLE_HOLDSTATE_N);
					trayData.setReasonCode("");
					trayData.setReasonCodeType("");
					durUdf.put("DURABLEHOLDSTATE", constantMap.DURABLE_HOLDSTATE_N);
				}		
				
				durableBindList.add(reasonCode);
				durableBindList.add(reasonCodeType);
				durableBindList.add(eventInfo.getEventName());
				durableBindList.add(eventInfo.getEventTimeKey());
				durableBindList.add(eventInfo.getEventTime());
				durableBindList.add(eventInfo.getEventUser());
				durableBindList.add(eventInfo.getEventComment());
				durableBindList.add(trayData.getKey().getDurableName());
				updateDurableArgList.add(durableBindList.toArray());
				
				//History
				trayData.setUdfs(durUdf);
				trayData.setLastEventName(eventInfo.getEventName());
				trayData.setLastEventTimeKey(eventInfo.getEventTimeKey());
				trayData.setLastEventTime(eventInfo.getEventTime());
				trayData.setLastEventUser(eventInfo.getEventUser());
				trayData.setLastEventComment(eventInfo.getEventComment());
				
				DurableHistory durHistory = new DurableHistory();
				durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldDurable, trayData, durHistory);
				
				DurableServiceProxy.getDurableHistoryService().insert(durHistory);
			}
		}
		
		//Lot
		for(Map<String, Object> lotMap : lotList) 
		{		
			String lotName = lotMap.get("LOTNAME").toString();
			
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			Lot oldLot = (Lot) ObjectUtil.copyTo(lotData);	
			List<Object> lotBindList = new ArrayList<Object>();
			
			if(eventName.equals("TrayGroupHold"))
			{
				//CommonValidation.checkLotHoldState(lotData);// caixu 2020/11/18 delete Check
				lotBindList.add(constantMap.Flag_Y);
				lotData.setLotHoldState(constantMap.Flag_Y);
				lotData.setReasonCode(reasonCode);
				lotData.setReasonCodeType(reasonCodeType);
			}
			else
			{
				//CommonValidation.checkLotNotHoldState(lotData);// caixu 2020/11/18 delete Check
				lotBindList.add(constantMap.Flag_N);
				lotData.setLotHoldState(constantMap.Flag_N);
				lotData.setReasonCode("");
				lotData.setReasonCodeType("");
			}
			
			
			lotBindList.add(eventInfo.getEventName());
			lotBindList.add(eventInfo.getEventTimeKey());
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(eventInfo.getEventComment());
			lotBindList.add(reasonCode);
			lotBindList.add(reasonCodeType);
			lotBindList.add(lotData.getKey().getLotName());
			
			updateLotArgList.add(lotBindList.toArray());
			
			//History
			lotData.setLastEventName(eventInfo.getEventName());
			lotData.setLastEventTime(eventInfo.getEventTime());
			lotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lotData.setLastEventComment(eventInfo.getEventComment());
			lotData.setLastEventUser(eventInfo.getEventUser());
			
			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lotData, lotHistory);
			
			updateLotHistoryList.add(lotHistory);
		}
		MESLotServiceProxy.getLotServiceUtil().updateBatch(queryStringDurable, updateDurableArgList);
		
		MESLotServiceProxy.getLotServiceUtil().updateBatch(queryStringLot, updateLotArgList);
		try 
		{
			CommonUtil.executeBatch("insert", updateLotHistoryList);
		} 
		catch (Exception e) 
		{
			logger.error(e.getMessage());
			throw new CustomException(e.getCause());
		}
		
		// TrayGroup
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		Durable olddurableInfo = trayGroupData;

		Map<String, String> durUdf = new HashMap<>();
		if(eventName.equals("TrayGroupHold"))
		{
			trayGroupData.setReasonCode(reasonCode);
			trayGroupData.setReasonCodeType(reasonCodeType);
			durUdf.put("DURABLEHOLDSTATE", constantMap.DURABLE_HOLDSTATE_Y);
		}
		else
		{
			trayGroupData.setReasonCode("");
			trayGroupData.setReasonCodeType("");
			durUdf.put("DURABLEHOLDSTATE", constantMap.DURABLE_HOLDSTATE_N);
		}
		trayGroupData.setUdfs(durUdf);
		trayGroupData.setLastEventName(eventInfo.getEventName());
		trayGroupData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		trayGroupData.setLastEventTime(eventInfo.getEventTime());
		trayGroupData.setLastEventUser(eventInfo.getEventUser());
		trayGroupData.setLastEventComment(eventInfo.getEventComment());

		DurableHistory durHistory = new DurableHistory();
		durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(olddurableInfo, trayGroupData, durHistory);

		DurableServiceProxy.getDurableService().update(trayGroupData);
		DurableServiceProxy.getDurableHistoryService().insert(durHistory);
		
		return doc;
	}
}