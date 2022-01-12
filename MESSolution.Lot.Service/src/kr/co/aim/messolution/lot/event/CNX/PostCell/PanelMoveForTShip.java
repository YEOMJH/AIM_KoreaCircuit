package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
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
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;


public class PanelMoveForTShip  extends SyncHandler {
private static Log log = LogFactory.getLog(MovePanel.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		//String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String sourceDurableName = SMessageUtil.getBodyItemValue(doc, "SOURCEDURABLENAME", true);
		String destDurableName = SMessageUtil.getBodyItemValue(doc, "TARGETDURABLENAME", true);
		
		Durable TrayInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sourceDurableName);
		Durable destTrayInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(destDurableName);
		
		CommonValidation.CheckDurableState(TrayInfo);
		CommonValidation.CheckDurableHoldState(TrayInfo);
		//CommonValidation.CheckEmptyCoverName(TrayInfo);
		CommonValidation.CheckDurableState(destTrayInfo);
		CommonValidation.CheckDurableHoldState(destTrayInfo);
		//CommonValidation.CheckEmptyCoverName(destTrayInfo);
		CommonValidation.checkTrayLotQty(destDurableName);
		
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();		
		//EventInfo Deassign
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("PanelMove", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		//
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();
		//LOTLIST
		List<Map<String, Object>> lotList = MESLotServiceProxy.getLotServiceUtil().getLotListByTrayGroup(sourceDurableName);
		if (lotList.size() < 1)
		{
			throw new CustomException("PROCESSGROUP-0004", sourceDurableName);
		}
		for (Map<String, Object> lotData : lotList)
		{
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.get("LOTNAME").toString());
			Lot oldLot = (Lot) ObjectUtil.copyTo(lot);
			CommonValidation.checkLotShippedState(lot);
			CommonValidation.checkLotState(lot);	
			//
			List<Object> lotBindList = new ArrayList<Object>();
			lotBindList.add(destDurableName);
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
			lotBindList.add(lot.getKey().getLotName());

			updateLotArgList.add(lotBindList.toArray());
			
			lot.setCarrierName(destDurableName);
			lot.setLastLoggedInTime(eventInfo.getEventTime());
			lot.setLastLoggedInUser(eventInfo.getEventUser());
			lot.setLastLoggedOutTime(eventInfo.getEventTime());
			lot.setLastLoggedOutUser(eventInfo.getEventUser());
			lot.setLastEventName(eventInfo.getEventName());
			lot.setLastEventTime(eventInfo.getEventTime());
			lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lot.setLastEventComment(eventInfo.getEventComment());
			lot.setLastEventUser(eventInfo.getEventUser());
			
			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);	
			updateLotHistoryList.add(lotHistory);
		}
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE LOT ");
		sql.append("   SET ");
		sql.append("       CARRIERNAME = ?, ");
		sql.append("       LASTEVENTNAME = ?, ");
		sql.append("       LASTEVENTTIMEKEY = ?, ");
		sql.append("       LASTEVENTTIME = ?, ");
		sql.append("       LASTEVENTUSER = ?, ");
		sql.append("       LASTEVENTCOMMENT = ?, ");
		sql.append("       LASTEVENTFLAG = ?, ");
		sql.append("       LASTLOGGEDINTIME = ?, ");
		sql.append("       LASTLOGGEDINUSER = ?, ");
		sql.append("       LASTLOGGEDOUTTIME = ?, ");
		sql.append("       LASTLOGGEDOUTUSER = ? ");
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
		Durable durableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sourceDurableName);
		long Qty = durableInfo.getLotQuantity();
		durableInfo.setLotQuantity(0);
		durableInfo.setDurableState(constantMap.Dur_Available);
		
		SetEventInfo setEventInfo = new SetEventInfo();
		DurableServiceProxy.getDurableService().update(durableInfo);
		eventInfo.setEventName("Deassign");
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableInfo, setEventInfo, eventInfo);
		
		eventInfo.setEventName("Assign");
		Durable NewdurableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(destDurableName);
		NewdurableInfo.setLotQuantity(Qty);
		NewdurableInfo.setDurableState(constantMap.Dur_InUse);
		DurableServiceProxy.getDurableService().update(NewdurableInfo);	
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(NewdurableInfo, setEventInfo, eventInfo);
		
		
		return doc;
	}
}
