package kr.co.aim.messolution.userprofile.event;

import java.util.HashMap;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.userprofile.MESUserServiceProxy;
import kr.co.aim.greentrack.generic.GenericServiceProxy;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.ExceptionKey;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.user.UserServiceProxy;
import kr.co.aim.greentrack.user.management.data.UserProfile;
import kr.co.aim.greentrack.user.management.data.UserProfileKey;

public class UserLogIn extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String uiName = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Body_Tag + "/", doc, "UINAME");
		String userId = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Body_Tag + "/", doc, "USERID");
		String password = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Body_Tag + "/", doc, "PASSWORD");
		String workStationName = SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Body_Tag + "/", doc, "WORKSTATIONNAME");
		String localName = SMessageUtil.getBodyItemValue(doc, "LOCALNAME", false);
		
		String version = SMessageUtil.getBodyItemValue(doc, "LOGGEDINUIVERSION", false);
		
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", false);
		
		MESUserServiceProxy.getUserProfileServiceUtil().validateFactoryAccessible(factoryName, userId, ",");
		
		try
		{
			try
			{
				MESUserServiceProxy.getUserProfileServiceImpl().login(userId, password, uiName, workStationName);
			}
			catch (FrameworkErrorSignal fe)
			{
				if (fe.getErrorCode().equals(ExceptionKey.User_AlreadyLoggedIn))
				{
					this.purgeGarbageLoggedIn(userId, uiName);
					//indeed, third trial
					MESUserServiceProxy.getUserProfileServiceImpl().login(userId, password, uiName, workStationName);
				}
			}
		}
		catch (Exception e) {
			//reference on non-standard error handling
			throw new CustomException("USER-0003", userId, password);
		}
		
		//update version 
		if (StringUtil.isNotEmpty(version))
			executePostAction(userId, uiName, workStationName, version, localName);
		
		Element returnElement = MESUserServiceProxy.getUserProfileServiceUtil().createUserProfileElement(userId, password);
		
		//replace for reply
		doc.getRootElement().getChild(SMessageUtil.Body_Tag).setContent(returnElement);
		
		return doc;
	}
	
	private void executePostAction(String userId, String uiName, String workStationName, String version, String localName)
		throws CustomException
	{
		try
		{
			String sql = "UPDATE UserLoggedIn SET LOGGEDINUIVERSION = ? , LOCALNAME = ? WHERE userId = ? AND uiName = ? AND workStationName = ?";
			
			Object[] bindList = new Object[] {version,localName, userId, uiName, workStationName };
			
			kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().update(sql, bindList);
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("SYS-9999", "LogIn", de.getMessage());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "LogIn", fe.getMessage());
		}
	}
	
	private void purgeGarbageLoggedIn(String userName, String uiName)
		throws CustomException
	{
		String sql = "DELETE UserLoggedIn WHERE userId = ? AND uiName = ?";
		
		Object[] bindArray = new Object[] {userName, uiName};
		
		try
		{
			GenericServiceProxy.getSqlMesTemplate().update(sql, bindArray);
		}
		catch (Exception ex)
		{
			eventLog.error(ex);
		}
	}
}
