package kr.co.aim.messolution.generic.object.log;

import org.jdom.Document;

public class TransactionLogItems {
	private Document xml;
	private long elapsedTime;
	private boolean result = true;
	
	public TransactionLogItems(Document xml, long elapsedTime,boolean result)
	{
		this.setXml(xml);
		this.setElapsedTime(elapsedTime);
		this.setResult(result);
	}

	public Document getXml() {
		return xml;
	}

	public void setXml(Document xml) {
		this.xml = xml;
	}

	public long getElapsedTime() {
		return elapsedTime;
	}

	public void setElapsedTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}

	public boolean isResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}
}
