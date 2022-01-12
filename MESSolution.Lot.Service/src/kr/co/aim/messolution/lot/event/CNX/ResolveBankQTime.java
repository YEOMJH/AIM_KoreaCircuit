package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.BankQueueTime;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class ResolveBankQTime extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> eleQTimeList = SMessageUtil.getBodySequenceItemList(doc, "QUEUETIMELIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		for (Element eleQTime : eleQTimeList)
		{
			String lotName = SMessageUtil.getChildText(eleQTime, "LOTNAME", true);
			String factoryName = SMessageUtil.getChildText(eleQTime, "FACTORYNAME", true);
			String bankType = SMessageUtil.getChildText(eleQTime, "BANKTYPE", true);
			String productSpecName = SMessageUtil.getChildText(eleQTime, "PRODUCTSPECNAME", true);
			String processFlowName = SMessageUtil.getChildText(eleQTime, "PROCESSFLOWNAME", true);
			String processOperationName = SMessageUtil.getChildText(eleQTime, "PROCESSOPERATIONNAME", true);

			this.resolveQTime(eventInfo, lotName);
		}
		
		return doc;
	}
	
	private void resolveQTime(EventInfo eventInfo, String lotName) throws CustomException
	{
		try
		{
			eventInfo.setEventName("Resolve");

			BankQueueTime QtimeData = ExtendedObjectProxy.getBankQueueTimeService().selectByKey(true, new Object[] { lotName});
			
			QtimeData.setResolveTime(eventInfo.getEventTime());
			QtimeData.setResolveUser(eventInfo.getEventUser());
			QtimeData.setQueueTimeState(GenericServiceProxy.getConstantMap().QTIME_STATE_CONFIRM);
			QtimeData.setLastEventName(eventInfo.getEventName());
			QtimeData.setLastEventTimekey(eventInfo.getEventTimeKey());
			QtimeData.setLastEventUser(eventInfo.getEventUser());

			ExtendedObjectProxy.getBankQueueTimeService().modify(eventInfo, QtimeData);
		}
		catch (greenFrameDBErrorSignal ne)
		{
			// ignore error to consider as not found exception signal
			throw new CustomException("BANK-0002");
		}
	}
}