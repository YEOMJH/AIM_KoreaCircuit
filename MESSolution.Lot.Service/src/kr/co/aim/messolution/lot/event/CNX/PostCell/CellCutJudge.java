package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;

public class CellCutJudge extends SyncHandler {

	private static Log log = LogFactory.getLog(CellCutJudge.class);
	
	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		//Get Doc
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", false);
		String lotGrade = SMessageUtil.getBodyItemValue(doc, "LOTGRADE", true);
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", true);
		String reasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", true);
		
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		//EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CellCutJudge", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		String lotUpdateQuery = " UPDATE LOT SET LOTGRADE = ?, REASONCODE = ?, REASONCODETYPE = ?, LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, LASTEVENTFLAG = ? WHERE LOTNAME = ?";
		
		//Update
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();
		
		for (Element lotData : lotList)
		{	
			//update
			List<Object> lotBindList = new ArrayList<Object>();
			String lotName = lotData.getChildText("LOTNAME");
			
			lotBindList.add(lotGrade);
			lotBindList.add(reasonCode);
			lotBindList.add(reasonCodeType);
			lotBindList.add(eventInfo.getEventName());
			lotBindList.add(eventInfo.getEventTimeKey());
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(eventInfo.getEventComment());
			lotBindList.add(constantMap.Flag_N);
			lotBindList.add(lotName);
			
			updateLotArgList.add(lotBindList.toArray());
			
			//History
			
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
			Lot oldLot = (Lot)ObjectUtil.copyTo(lot);
			
			lot.setLotGrade(lotGrade);
			lot.setReasonCode(reasonCode);
			lot.setReasonCodeType(reasonCodeType);
			lot.setLastEventName(eventInfo.getEventName());
			lot.setLastEventTime(eventInfo.getEventTime());
			lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lot.setLastEventComment(eventInfo.getEventComment());
			lot.setLastEventUser(eventInfo.getEventUser());
			
			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
			
			updateLotHistoryList.add(lotHistory);
		}
		MESLotServiceProxy.getLotServiceUtil().updateBatch(lotUpdateQuery, updateLotArgList);
		try 
		{
			CommonUtil.executeBatch("insert", updateLotHistoryList);
		} 
		catch (Exception e) 
		{
			log.error(e.getMessage());
			throw new CustomException(e.getCause());
		}
		
		return doc;
	}
}
