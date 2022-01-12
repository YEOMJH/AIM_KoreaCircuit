package kr.co.aim.messolution.userprofile.event;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.userprofile.MESUserServiceProxy;

public class ChangeShop extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String messageName = SMessageUtil.getMessageName(doc);
		
		String factoryName = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Body_Tag + "/", doc, "FACTORYNAME");
		String areaName = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Body_Tag + "/", doc, "AREANAME");
		
		try
		{
			MESUserServiceProxy.getUserProfileServiceImpl().changeShop(factoryName, areaName, getEventUser(), messageName);
			
		}
		catch (Exception e) {
			//reference on non-standard error handling
			throw new CustomException(e);
		}
		
		return doc;
	}

}
