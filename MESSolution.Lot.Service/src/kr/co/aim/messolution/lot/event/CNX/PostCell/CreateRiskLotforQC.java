package kr.co.aim.messolution.lot.event.CNX.PostCell;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

public class CreateRiskLotforQC   extends SyncHandler{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		List<Element> riskLotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", false);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateriskLotForQC", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		String checkFlag= SMessageUtil.getBodyItemValue(doc, "CHECKFLAG", true);
		for (Element riskLot  : riskLotList)
		{
			String lotID = SMessageUtil.getChildText(riskLot, "LOTNAME", true);
			String factoryName = SMessageUtil.getChildText(riskLot, "FACTORYNAME", true);
			String productType= SMessageUtil.getChildText(riskLot, "PRODUCTTYPE", true);
			String reasonCode= SMessageUtil.getChildText(riskLot, "REASONCODE", true);
			
			this.CheckLotName(lotID);
			RiskLot riskLotInfo = new RiskLot();
			riskLotInfo.setLotName(lotID);
			riskLotInfo.setFactoryName(factoryName);
			riskLotInfo.setAllowableGrade("");
			riskLotInfo.setProductType(productType);
			riskLotInfo.setLastEventName(eventInfo.getEventName());
			riskLotInfo.setLastEventTime(eventInfo.getEventTime());
			riskLotInfo.setLastEventComment(eventInfo.getEventComment());
			riskLotInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
			riskLotInfo.setLastEventUser(eventInfo.getEventUser());
			riskLotInfo.setPickFlag("");
			riskLotInfo.setCreateUser(eventInfo.getEventUser());
			riskLotInfo.setReasonCode(reasonCode);
			riskLotInfo.setReasonCodeType("");
			riskLotInfo.setCheckFlag("N");
			
			ExtendedObjectProxy.getRiskLotService().create(eventInfo, riskLotInfo);	
		}	
		return doc;
		
	}
	private void CheckLotName(String lotID) throws CustomException
	{
		try
		{
			String condition = " WHERE 1=1 AND LOTNAME = ?";
			Object[] bindSet = new Object[] { lotID };
			List<RiskLot> riskLotList = null;
			riskLotList = ExtendedObjectProxy.getRiskLotService().select(condition, bindSet);
			if(riskLotList!=null &&riskLotList.size()>0)
			{
				throw new CustomException("JOB-8011", " Havent Date");
			}
		}
		catch (Exception ex)
		{
			
		}	
	}
}
