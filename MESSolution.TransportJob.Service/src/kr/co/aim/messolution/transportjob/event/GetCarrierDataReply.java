package kr.co.aim.messolution.transportjob.event;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class GetCarrierDataReply extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX -> OIC]
	 * 
	 * <Body>
	 *    <CARRIERNAME />
	 *    <CURRENTMACHINENAME />
	 *    <CURRENTPOSITIONTYPE />
	 *	  <CURRENTPOSITIONNAME />
	 *	  <CURRENTZONENAME />
	 *	  <TRANSFERSTATE />
	 *	  <CARRIERSTATE />
	 *	  <CARRIERTYPE />
	 *	  <CLEANSTATE />
	 *	  <LOTNAME />
	 *	  <PRODUCTQUANTITY />
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("GetCarrierDataReply", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		try
		{
			String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
			String currentMachineName = SMessageUtil.getBodyItemValue(doc, "CURRENTMACHINENAME", false);
			String currentPositionType = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONTYPE", false);
			String currentPositionName = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONNAME", false);
			String currentZoneName = SMessageUtil.getBodyItemValue(doc, "CURRENTZONENAME", false);
			String transferState = SMessageUtil.getBodyItemValue(doc, "TRANSFERSTATE", false);

			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

			durableData = MESTransportServiceProxy.getTransportJobServiceUtil().changeCurrentCarrierLocation(durableData,
					currentMachineName, currentPositionType, currentPositionName, currentZoneName, transferState, "", eventInfo);
		}
		catch (Exception e)
		{
		}

		String originalSourceSubjectName = getOriginalSourceSubjectName();

		if (StringUtils.isNotEmpty(originalSourceSubjectName))
		{
			// Send Reply to OIC
			GenericServiceProxy.getESBServive().sendBySenderWithoutChangeReturnElement(originalSourceSubjectName, doc, "OICSender");
		}
	}
}
