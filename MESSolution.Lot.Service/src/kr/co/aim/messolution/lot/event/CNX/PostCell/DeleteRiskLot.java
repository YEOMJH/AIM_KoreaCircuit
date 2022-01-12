package kr.co.aim.messolution.lot.event.CNX.PostCell;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.RiskLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
public class DeleteRiskLot  extends SyncHandler{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		List<Element> riskLotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", false);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteRiskLot", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		for (Element riskLot  : riskLotList)
		{
			String lotID = SMessageUtil.getChildText(riskLot, "LOTNAME", true);
			RiskLot riskLotInfo = new RiskLot();
			riskLotInfo =  getRiskLotInfo(lotID);
			riskLotInfo.setLastEventName(eventInfo.getEventName());
			riskLotInfo.setLastEventTime(eventInfo.getEventTime());
			riskLotInfo.setLastEventComment(eventInfo.getEventComment());
			riskLotInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
			riskLotInfo.setLastEventUser(eventInfo.getEventUser());
			ExtendedObjectProxy.getRiskLotService().remove(eventInfo, riskLotInfo);
		}	
		return doc;
	}

	private RiskLot getRiskLotInfo(String lotID) {
		
		RiskLot LotInfo = new RiskLot();

		try
		{
			LotInfo = ExtendedObjectProxy.getRiskLotService().selectByKey(false, new Object[] { lotID });
		}
		catch (Exception e)
		{
			LotInfo = null;
		}

		return LotInfo;
	}

	private void DeleteLotInfo(String lotID) {
		String sql= "DELETE CT_RISKLOT WHERE LOTNAME = :LOTNAME";
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("LOTNAME",lotID);
		GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
	}

	
}

