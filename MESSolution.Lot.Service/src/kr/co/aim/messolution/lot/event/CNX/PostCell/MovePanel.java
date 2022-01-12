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

public class MovePanel extends SyncHandler {

	private static Log log = LogFactory.getLog(MovePanel.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String sourceDurableName = SMessageUtil.getBodyItemValue(doc, "SOURCEDURABLENAME", true);
		String destDurableName = SMessageUtil.getBodyItemValue(doc, "TARGETDURABLENAME", true);
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "TARGETPANELLIST", true);
		
		Durable TrayInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sourceDurableName);
		Durable destTrayInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(destDurableName);
		
		CommonValidation.CheckDurableState(TrayInfo);
		CommonValidation.CheckDurableHoldState(TrayInfo);
		CommonValidation.CheckEmptyCoverName(TrayInfo);
		CommonValidation.CheckDurableState(destTrayInfo);
		CommonValidation.CheckDurableHoldState(destTrayInfo);
		CommonValidation.CheckEmptyCoverName(destTrayInfo);

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		//EventInfo Deassign
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Deassign", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		// SQL
		String queryStringLot = "UPDATE LOT SET LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, "
				+ "LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, LASTEVENTFLAG = ?, POSITION = ?, CARRIERNAME = ? WHERE LOTNAME = ?";

		// Deassign Panel
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();

		for (Element lotData : lotList) {
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getChildText("LOTNAME"));
			Lot oldLot = (Lot)ObjectUtil.copyTo(lot);
			
			if(!lot.getCarrierName().equals(sourceDurableName))
			{
				throw new CustomException("PANEL-0005", lot.getKey().getLotName(), lot.getCarrierName());
			}

			List<Object> lotBindList = new ArrayList<Object>();

			lotBindList.add(eventInfo.getEventName());
			lotBindList.add(eventInfo.getEventTimeKey());
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(eventInfo.getEventComment());
			lotBindList.add(constantMap.Flag_N);
			lotBindList.add("");
			lotBindList.add("");
			lotBindList.add(lotData.getChildText("LOTNAME"));

			updateLotArgList.add(lotBindList.toArray());

			// History
			lot.setCarrierName("");
			lot.setLastEventName(eventInfo.getEventName());
			lot.setLastEventTime(eventInfo.getEventTime());
			lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lot.setLastEventComment(eventInfo.getEventComment());
			lot.setLastEventUser(eventInfo.getEventUser());
			Map<String, String> lotUdfs = new HashMap<>();
			lotUdfs.put("POSITION", "");
			lot.setUdfs(lotUdfs);

			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
			
			updateLotHistoryList.add(lotHistory);
		}
		MESLotServiceProxy.getLotServiceUtil().updateBatch(queryStringLot, updateLotArgList);
		try 
		{
			CommonUtil.executeBatch("insert", updateLotHistoryList);
		} 
		catch (Exception e) 
		{
			log.error(e.getMessage());
			throw new CustomException(e.getCause());
		}

		// Durable Deassign
		Durable durableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sourceDurableName);
		SetEventInfo setEventInfo = new SetEventInfo();
		
		durableInfo.setLotQuantity(durableInfo.getLotQuantity() - lotList.size());
		
		if(durableInfo.getLotQuantity() == 0)
		{
			durableInfo.setDurableState(constantMap.Dur_Available);
			setEventInfo.getUdfs().put("BCRFLAG", "");
		}
		
		DurableServiceProxy.getDurableService().update(durableInfo);
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableInfo, setEventInfo, eventInfo);
	
		// EventInfo Assign
		eventInfo.setEventName("Assign");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

		// SQL Assign
		String queryStringLotAssign = "UPDATE LOT SET LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, "
				+ "LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, LASTEVENTFLAG = ?, POSITION = ?, CARRIERNAME = ? WHERE LOTNAME = ?";

		// Assign Panel
		List<Object[]> updateLotArgListAssign = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryListAssign = new ArrayList<LotHistory>();

		for (Element lotData : lotList) {
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getChildText("LOTNAME"));
			Lot oldLot = (Lot)ObjectUtil.copyTo(lot);

			String position = lotData.getChildText("POSITION");

			List<Object> lotBindList = new ArrayList<Object>();

			lotBindList.add(eventInfo.getEventName());
			lotBindList.add(eventInfo.getEventTimeKey());
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(eventInfo.getEventComment());
			lotBindList.add(constantMap.Flag_N);
			lotBindList.add(position);
			lotBindList.add(destDurableName);
			lotBindList.add(lotData.getChildText("LOTNAME"));

			updateLotArgListAssign.add(lotBindList.toArray());

			// History Assign
			lot.setCarrierName(destDurableName);
			lot.setLastEventName(eventInfo.getEventName());
			lot.setLastEventTime(eventInfo.getEventTime());
			lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lot.setLastEventComment(eventInfo.getEventComment());
			lot.setLastEventUser(eventInfo.getEventUser());
			Map<String, String> lotUdfs = new HashMap<>();
			lotUdfs.put("POSITION", position);
			lot.setUdfs(lotUdfs);

			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
			
			updateLotHistoryListAssign.add(lotHistory);
		}
		MESLotServiceProxy.getLotServiceUtil().updateBatch(queryStringLotAssign, updateLotArgListAssign);
		try 
		{
			CommonUtil.executeBatch("insert", updateLotHistoryListAssign);
		} 
		catch (Exception e) 
		{
			log.error(e.getMessage());
			throw new CustomException(e.getCause());
		}

		// Durable Assign
		Durable olddurableInfoAssign = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(destDurableName);
		Durable durableInfoAssign = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(destDurableName);

		durableInfoAssign.setLotQuantity(durableInfoAssign.getLotQuantity() + lotList.size());
		durableInfoAssign.setLastEventName(eventInfo.getEventName());
		durableInfoAssign.setLastEventTimeKey(eventInfo.getEventTimeKey());
		durableInfoAssign.setLastEventTime(eventInfo.getEventTime());
		durableInfoAssign.setLastEventUser(eventInfo.getEventUser());
		durableInfoAssign.setLastEventComment(eventInfo.getEventComment());
		durableInfoAssign.setDurableState("InUse");

		DurableHistory durHistoryAssign = new DurableHistory();
		durHistoryAssign = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(olddurableInfoAssign, durableInfoAssign, durHistoryAssign);

		DurableServiceProxy.getDurableService().update(durableInfoAssign);
		DurableServiceProxy.getDurableHistoryService().insert(durHistoryAssign);
		
		return doc;
	}
}