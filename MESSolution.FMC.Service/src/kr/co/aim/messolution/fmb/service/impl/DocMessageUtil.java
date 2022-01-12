package kr.co.aim.messolution.fmb.service.impl;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

public class DocMessageUtil {
	static public void setReturnElement(Document doc, String returnCode,
			String returnMessage) {
		Element resultNode = getReturnElement(doc);
		
		resultNode.getChild("returncode").setText(returnCode);
		resultNode.getChild("returnmessage").setText(returnMessage);
	}

	static public Element getReturnElement(Document doc) {
		Element resultNode = null;
		try {
			resultNode = (Element) XPath.selectSingleNode(doc, "//return");
		} catch (JDOMException e) {
			e.printStackTrace();
		}
		if(resultNode == null) {
			resultNode = new Element("return");
			resultNode.addContent(new Element("returncode").setText(String.valueOf(1)));
			resultNode.addContent(new Element("returnmessage").setText("not find return element"));
			doc.getRootElement().addContent(resultNode);
		}
		return resultNode;
	}
	static public String getReturnCode(Document doc) {
		Element resultNode = getReturnElement(doc);
		return resultNode.getChild("returncode").getText();
	}
	static public String getReturnMessage(Document doc) {
		Element resultNode = getReturnElement(doc);
		return resultNode.getChild("returnmessage").getText();
	}
}
