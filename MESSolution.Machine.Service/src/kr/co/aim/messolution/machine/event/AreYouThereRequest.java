package kr.co.aim.messolution.machine.event;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.eventHandler.SyncHandler;

public class AreYouThereRequest extends SyncHandler {

	@Override
	public Object doWorks(Document doc)
	{
		Element root = doc.getDocument().getRootElement();
		Element messageNameElement = root.getChild("Header").getChild("MESSAGENAME");
		messageNameElement.setText("AreYouThereReply");

		return doc;
	}
}
