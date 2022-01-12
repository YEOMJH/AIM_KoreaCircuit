package kr.co.aim.messolution.userprofile.event;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.userprofile.MESUserServiceProxy;

public class ChangePassword extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		String userId = SMessageUtil.getBodyItemValue(doc, "USERID", true);
		String oldPassword = SMessageUtil.getBodyItemValue(doc, "OLDPASSWORD", true);
		String newPassword = SMessageUtil.getBodyItemValue(doc, "NEWPASSWORD", true);
		
		//密码复杂度验证
		boolean passwordCheck = MESUserServiceProxy.getUserProfileServiceImpl().rexCheckPassword(newPassword);
		if(!passwordCheck)
		{
			throw new CustomException("PASSWORD-0001");
		}

		MESUserServiceProxy.getUserProfileServiceImpl().changePassword(userId, oldPassword, newPassword);

		Element returnElement = MESUserServiceProxy.getUserProfileServiceUtil().createUserProfileElement(userId, newPassword);

		// replace for reply
		doc.getRootElement().getChild(SMessageUtil.Body_Tag).setContent(returnElement);

		return doc;
	}
}
