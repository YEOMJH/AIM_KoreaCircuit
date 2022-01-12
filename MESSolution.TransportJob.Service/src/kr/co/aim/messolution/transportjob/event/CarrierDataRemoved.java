package kr.co.aim.messolution.transportjob.event;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;

public class CarrierDataRemoved extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX]
	 * 
	 * <Body>
	 *    <CARRIERNAME />
	 *    <MACHINENAME />
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CarrierDataRemoved", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		carrierName = MESTransportServiceProxy.getTransportJobServiceUtil().unknownCarrierChangeName(carrierName);
		Durable durableData = CommonValidation.checkExistCarrier(carrierName);

		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("TRANSPORTSTATE", "");
		setEventInfo.getUdfs().put("POSITIONTYPE", "");
		setEventInfo.getUdfs().put("MACHINENAME", "");
		setEventInfo.getUdfs().put("UNITNAME", "");
		setEventInfo.getUdfs().put("PORTNAME", "");
		setEventInfo.getUdfs().put("POSITIONNAME", "");
		setEventInfo.getUdfs().put("ZONENAME", "");
		
		//TP FA wanglei需求
		if (carrierName.startsWith("TN") && durableData.getLotQuantity() == 0)
		{
			setEventInfo.getUdfs().put("DEPARTMENT", "FABAN");
		}
		
		//Lami 简活腾需求 20210726
		if(StringUtils.equals(machineName, "3FOCV01") && StringUtils.equals(durableData.getDurableType(), GenericServiceProxy.getConstantMap().DURABLETYPE_FilmBox))
		{
			durableData.setDurableCleanState("Dirty");
			DurableServiceProxy.getDurableService().update(durableData);
		}
		
		DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
	}
}