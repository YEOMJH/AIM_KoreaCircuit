package kr.co.aim.messolution.durable.event.IMS;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

public class IMSCancelCreateMaskSendReply extends AsyncHandler{
	
	private static Log log = LogFactory.getLog(IMSCancelCreateMaskSendReply.class);
	@Override
	public void doWorks(Document doc) throws CustomException
	{			
		log.info("PhotoMaskCancelFiled");
	}

}
