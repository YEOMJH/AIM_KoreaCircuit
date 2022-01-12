package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.BankInfo;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class ModifyBankInfo extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> bankList = SMessageUtil.getBodySequenceItemList(doc, "BANKLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyBankInfo", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		for (Element bank : bankList)
		{
			try
			{
				String factoryName = SMessageUtil.getChildText(bank, "FACTORYNAME", true);
				String toFactoryName = SMessageUtil.getChildText(bank, "TOFACTORYNAME", true);
				String bankType = SMessageUtil.getChildText(bank, "BANKTYPE", true);
				String description = SMessageUtil.getChildText(bank, "DESCRIPTION", false);
				String wipLimit = SMessageUtil.getChildText(bank, "WIPLIMIT", true);
				String alarmType = SMessageUtil.getChildText(bank, "ALARMTYPE", true);
	

				BankInfo bankInfo = ExtendedObjectProxy.getBankInfoService().selectByKey(true, new Object[] { factoryName, toFactoryName, bankType });
				
				bankInfo.setWipLimit(wipLimit);
				bankInfo.setDescription(description);
				bankInfo.setAlarmType(alarmType);
				bankInfo.setLastEventComment(eventInfo.getEventComment());
				bankInfo.setLastEventTime(eventInfo.getEventTime());
				bankInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
				bankInfo.setLastEventUser(eventInfo.getEventUser());
				
				ExtendedObjectProxy.getBankInfoService().modify(eventInfo, bankInfo);
			}
			catch(Exception ex)
			{
			    throw new CustomException(ex.getCause());	
			}
		}

		return doc;
	}
}

