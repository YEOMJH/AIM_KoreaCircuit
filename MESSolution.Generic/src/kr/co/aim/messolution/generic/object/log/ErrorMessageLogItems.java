package kr.co.aim.messolution.generic.object.log;

import org.jdom.Document;

public class ErrorMessageLogItems {
	private Document xml;
	private Exception error;
	private String emptyFlag;

	public ErrorMessageLogItems(Document xml, Exception error, String emptyFlag)
	{
		this.setXml(xml);
		this.setError(error);
		this.setEmptyFlag(emptyFlag);
	}
	public Document getXml() {
		return xml;
	}
	public void setXml(Document xml) {
		this.xml = xml;
	}
	public Exception getError() {
		return error;
	}
	public void setError(Exception error) {
		this.error = error;
	}
	public String getEmptyFlag() {
		return emptyFlag;
	}
	public void setEmptyFlag(String emptyFlag) {
		this.emptyFlag = emptyFlag;
	}
}
