package kr.co.aim.messolution.durable.event.IMS;

import java.util.List;

import org.jdom.Document;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import org.jdom.Element;

public class IMSMaskChangeInfoReply extends AsyncHandler{
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String maskName = SMessageUtil.getBodyItemValue(doc, "MASKID", true);
		String returnCode = SMessageUtil.getReturnItemValue(doc, "RETURNCODE", true);
		String returnMessage = SMessageUtil.getReturnItemValue(doc, "RETURNMESSAGE", false);
		
		Durable maskData=MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskName);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateReply", getEventUser(), returnMessage, "", "");

		SetEventInfo setEventInfo = new SetEventInfo();

		MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskData, setEventInfo, eventInfo);

	}

}
