package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.jdom.Document;

public class GetInventoryZoneDataRequest extends AsyncHandler {

	/**
	 * MessageSpec [OIC -> TEX -> MCS]
	 * 
	 * <Body>
	 *    <MACHINENAME />
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		try
		{
			String messageName = SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", true);
			doc.getRootElement().getChild("Header").getChild("EVENTCOMMENT").setText(messageName);

			// Send Message to MCS
			GenericServiceProxy.getESBServive().sendBySender(doc, "HIFSender");
		}
		catch (Exception e)
		{
			String originalSourceSubjectName = getOriginalSourceSubjectName();
			GenericServiceProxy.getESBServive().sendErrorBySender(originalSourceSubjectName, doc, getLanguage(), e, "OICSender");

			throw new CustomException(e);
		}
	}
}
