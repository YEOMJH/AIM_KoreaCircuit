package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;

public class MaskUnloadRequest extends AsyncHandler {

	/**
	 * MessageSpec [PEX -> TEX -> MCS]
	 * 
	 * <Body>
	 *    <MACHINENAME />
	 *    <MASKNAME />
	 *    <BUFFERSLOTPOSITION />
	 *    <PORTNAME />
	 *    <PORTTYPE />
	 *    <PORTUSETYPE />
	 *    <PORTACCESSMODE />
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		// send to RTD
		Document rtnDoc = (Document) doc.clone();
		SMessageUtil.setHeaderItemValue(rtnDoc, "NAME", "Unload_Request");

		// 2020-11-17	dhko	Add <CARRIERNAME> Element
		String maskName = SMessageUtil.getBodyItemValue(rtnDoc, "MASKNAME", true);
		SMessageUtil.setItemValue(rtnDoc, "Body", "CARRIERNAME", maskName);
		
		String replySubject = GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("RTD");
		GenericServiceProxy.getESBServive().sendReplyBySender(replySubject, rtnDoc, "DSPSender");
	}
}

