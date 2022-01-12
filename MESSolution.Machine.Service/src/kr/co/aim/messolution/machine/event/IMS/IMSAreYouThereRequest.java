package kr.co.aim.messolution.machine.event.IMS;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.eventHandler.SyncHandler;

public class IMSAreYouThereRequest extends SyncHandler {

	@Override
	public Object doWorks(Document doc)
	{
		Element root = doc.getDocument().getRootElement();
		Element messageNameElement = root.getChild("Header").getChild("MESSAGENAME");
		messageNameElement.setText("IMSAreYouThereReply");

		return doc;
	}
}
