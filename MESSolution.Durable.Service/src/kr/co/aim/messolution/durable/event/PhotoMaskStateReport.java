package kr.co.aim.messolution.durable.event;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class PhotoMaskStateReport extends AsyncHandler {
	private static Log log = LogFactory.getLog(PhotoMaskStateReport.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{

		String smachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String sunitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeStateReport", getEventUser(), getEventComment(), "", "");

		Element eleBody = SMessageUtil.getBodyElement(doc);

		if (eleBody != null)
		{
			for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", false))
			{
				String sDurableName = SMessageUtil.getChildText(eledur, "MASKNAME", false);
				String sTransferState = SMessageUtil.getChildText(eledur, "TRANSPORTSTATE", false);

				// getDurableData
				Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);
				
				// Validation
				if (StringUtils.equals(durableData.getUdfs().get("MACHINENAME"), ""))
				{
					throw new CustomException("MASK-0002" + " MachineName is " + smachineName);
				}

				// SetEvent Info create
				SetEventInfo setEventInfo = new SetEventInfo();
				
				setEventInfo.getUdfs().put("MACHINENAME", smachineName);
				setEventInfo.getUdfs().put("UNITNAME", sunitName);
				setEventInfo.getUdfs().put("TRANSPORTSTATE", sTransferState);

				// Excute greenTrack API call- setEvent
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);

				log.info("DurableName = " + sDurableName + "Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
			}
		}
	}

}
