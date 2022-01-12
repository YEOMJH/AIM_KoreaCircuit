package kr.co.aim.messolution.generic.object.log;

import org.jdom.Document;

public class MessageLogItems {
	private Document xml;
	private String messageType;
	
	public MessageLogItems(Document document, String messageType)
	{
		this.xml = document;
		this.messageType = messageType;
	}
	
	public Document getXml() {
		return xml;
	}
	public void setXml(Document xml) {
		this.xml = xml;
	}
	public String getMessageType() {
		return messageType;
	}
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
}
