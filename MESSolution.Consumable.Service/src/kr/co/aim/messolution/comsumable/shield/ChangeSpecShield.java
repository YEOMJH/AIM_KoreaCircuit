package kr.co.aim.messolution.comsumable.shield;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ShieldLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

public class ChangeSpecShield extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		String shieldID = SMessageUtil.getBodyItemValue(doc, "SHIELDID", true);
		String line = SMessageUtil.getBodyItemValue(doc, "LINE", false);
		String setValue = SMessageUtil.getBodyItemValue(doc, "SET", false);
		String chamberNo = SMessageUtil.getBodyItemValue(doc, "CHAMBERNO", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeShieldSpec", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());

		ShieldLot shieldInfo = ExtendedObjectProxy.getShieldLotService().selectByKey(false, new Object[] { shieldID });
		
		if(StringUtil.isNotEmpty(shieldInfo.getCarrierName()))
		{
			throw new CustomException("SHIELD-0021");
		}
		
		CommonValidation.checkShieldLotHoldStateN(shieldInfo);
		CommonValidation.checkShieldLotProcessStateWait(shieldInfo);
		CommonValidation.CheckShieldState_Released(shieldInfo);
		
		if(StringUtil.isNotEmpty(line))
		{
			shieldInfo.setLine(Integer.parseInt(line));
		}
		
		if(StringUtil.isNotEmpty(setValue))
		{
			shieldInfo.setSetValue(setValue);
		}
		
		if(StringUtil.isNotEmpty(chamberNo))
		{
			shieldInfo.setChamberNo(chamberNo);
		}
		
		shieldInfo.setLastEventUser(eventInfo.getEventUser());
		shieldInfo.setLastEventTime(eventInfo.getEventTime());
		shieldInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
		shieldInfo.setLastEventName(eventInfo.getEventName());
		shieldInfo.setLastEventComment(eventInfo.getEventComment());
		
		ExtendedObjectProxy.getShieldLotService().modify(eventInfo, shieldInfo);
		
		return doc;
	}
}
