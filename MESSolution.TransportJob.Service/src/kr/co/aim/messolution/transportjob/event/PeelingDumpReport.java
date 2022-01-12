package kr.co.aim.messolution.transportjob.event;

import java.util.HashMap;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.MakeAvailableInfo;
import kr.co.aim.greentrack.durable.management.info.MakeNotInUseInfo;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class PeelingDumpReport extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX]
	 * 
	 * <Body>
	 *    <MACHINENAME />
	 *    <PORTNAME />
	 *    <CARRIERNAME />
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("PeelingDumpReport", getEventUser(), getEventComment(), "", "");

		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		if (GenericServiceProxy.getConstantMap().Dur_Available.equals(durableData.getDurableState()))
		{
			eventLog.info("DurableState is Available");
			return;
		}
		
		//MakeNotInUseInfo makeNotInUsedInfo = new MakeNotInUseInfo();
		//makeNotInUsedInfo.setUdfs(new HashMap<String, String>());
		
		this.changeDurableInfo(eventInfo,carrierName);	
		//DurableServiceProxy.getDurableService().makeNotInUse(durableData.getKey(), eventInfo, makeNotInUsedInfo);	
	}
	
	private void changeDurableInfo(EventInfo eventInfo,String carrierName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);		
		
		if (StringUtils.equals(GenericServiceProxy.getConstantMap().DURABLETYPE_PeelingFilmBox, durableData.getDurableType()))
		{
			SetEventInfo setEventInfo = new SetEventInfo();
			double timeUsed = durableData.getTimeUsed() + 1;
			durableData.setTimeUsed(timeUsed);
			durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);		
			if (durableData.getTimeUsedLimit() <= durableData.getTimeUsed())
			{
				durableData.setDurableCleanState("Dirty");
			}
			DurableServiceProxy.getDurableService().update(durableData);
			
			DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);			
		}	
	}
	
}
