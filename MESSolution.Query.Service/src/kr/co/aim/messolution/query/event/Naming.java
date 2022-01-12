package kr.co.aim.messolution.query.event;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.query.MESQueryServiceProxy;

public class Naming extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String sQuantity = SMessageUtil.getBodyItemValue(doc, "QUANTITY", true);
		String ruleName = SMessageUtil.getBodyItemValue(doc, "RULENAME", true);
		
		Element eleNameRuleAttr;
		try
		{
			//eleNameRuleAttr = XmlUtil.getNode(doc, "//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Body_Tag);
			eleNameRuleAttr = SMessageUtil.getBodyElement(doc);
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-0001", SMessageUtil.Body_Tag);
		}
		
		return MESQueryServiceProxy.getQueryServiceImpl().getName(ruleName, Long.parseLong(sQuantity), eleNameRuleAttr, doc);
	}

}
