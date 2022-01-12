package kr.co.aim.messolution.userprofile.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.user.UserServiceProxy;
import kr.co.aim.greentrack.user.management.data.UserGroup;
import kr.co.aim.greentrack.user.management.data.UserGroupKey;
import kr.co.aim.greentrack.user.management.data.UserProfile;
import kr.co.aim.greentrack.user.management.data.UserProfileKey;


public class UserProfileServiceUtil implements ApplicationContextAware {

	private ApplicationContext     	applicationContext;
	private static Log 				log = LogFactory.getLog(UserProfileServiceImpl.class);
	
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		applicationContext = arg0;
	}
	
	public Element createUserProfileElement(String userId, String password)
		throws FrameworkErrorSignal, NotFoundSignal
	{
        
		UserProfileKey userProfileKey = new UserProfileKey();
		userProfileKey.setUserId( userId );

		UserProfile userProfileData = null;
		userProfileData  = UserServiceProxy.getUserProfileService().selectByKey( userProfileKey );
		
		UserGroup dataInfo = UserServiceProxy.getUserGroupService().selectByKey(new UserGroupKey(userProfileData.getUserGroupName()));
		
		Element element = null;
		String node = null;

		node = "USERPROFILE";
		element = new Element(node);		

		Element elmUserId = new Element("USERID");
		elmUserId.setText(userProfileData.getKey().getUserId());
		element.addContent(elmUserId);
		
		Element elmPassword = new Element("PASSWORD");
		elmPassword.setText(password);
		element.addContent(elmPassword);
		
		Element elmUserName = new Element("USERNAME");
		elmUserName.setText(userProfileData.getUserName());
		element.addContent(elmUserName);

		Element elmUserGroupName = new Element("USERGROUPNAME");
		elmUserGroupName.setText(userProfileData.getUserGroupName());
		element.addContent(elmUserGroupName);
		
		Element isAdminGroup = new Element("ADMINGROUP");
		isAdminGroup.setText("Y".equals(dataInfo.getUdfs().get("ADMINGROUP"))?"Y":"N");
		element.addContent(isAdminGroup);
		
		Element isAdmin = new Element("ADMINFLAG");
		isAdmin.setText("Y".equals(userProfileData.getUdfs().get("ADMINFLAG"))?"Y":"N");
		element.addContent(isAdmin);
		
		Element CENTER = new Element("CENTER");
		CENTER.setText(userProfileData.getUdfs().get("CENTER"));
		element.addContent(CENTER);
		
		Element DEPARTMENT = new Element("DEPARTMENT");
		DEPARTMENT.setText(userProfileData.getUdfs().get("DEPARTMENT"));
		element.addContent(DEPARTMENT);
		
		Element GROUPNAME = new Element("GROUPNAME");
		GROUPNAME.setText(userProfileData.getUdfs().get("GROUPNAME"));
		element.addContent(GROUPNAME);
		
		return element;
	}
	
	public void validateFactoryAccessible(String factoryName, String userId, String separatorChar) throws CustomException
	{
		UserProfile userData = this.getUser(userId);
		
		String strFactorys = CommonUtil.getValue(userData.getUdfs(), "AVAILFACTORYNAME");
		
		if (strFactorys.length() < 1)
		{
			//no assigned & old user, allows to all
			log.warn("allowable access for any factory not yet set");
			return;
		}
		else if (factoryName.isEmpty())
		{
			//old client version, allows to all
			log.warn("client not send request including FACTORYNAME");
			return;
		}
		
		String[] loginFactorys = StringUtil.split(factoryName.trim(), separatorChar);
		String[] arrFactorys = StringUtil.split(strFactorys.trim(), separatorChar);
		
		boolean isFound = false;
		
		for(String loginFactoryName : loginFactorys)
		{
			for (String accessFactoryName : arrFactorys)
			{
				if (accessFactoryName.equalsIgnoreCase("ALL"))
				{
					log.warn("enable to access for all factories");
					isFound = true;
					break;
				}
				else if (accessFactoryName.equalsIgnoreCase(loginFactoryName))
				{
					isFound = true;
					break;
				}
				
				isFound = false;
			}
			
			if (!isFound)
			{
				factoryName = loginFactoryName;
				
				throw new CustomException("USER-0004", userId, factoryName);
			}
		}
	}

	public UserProfile getUser(String userId) throws CustomException
	{
		UserProfileKey userProfileKey = new UserProfileKey();
		
		userProfileKey.setUserId(userId);
		
		try
		{
			UserProfile userData = UserServiceProxy.getUserProfileService().selectByKey(userProfileKey);
			
			return userData;
		}
		catch ( Exception e )
		{
			throw new CustomException("USER-0006", userId);
		}
	}
	
	public UserProfile getUserData(String userId)
	{
		UserProfile dataInfo = new UserProfile();
		
		UserProfileKey userProfileKey = new UserProfileKey();
		userProfileKey.setUserId(userId);
		
		try
		{
			dataInfo = UserServiceProxy.getUserProfileService().selectByKey(userProfileKey);
		}
		catch (Exception e)
		{
			dataInfo = null;
		}
		
		return dataInfo;
	}

}
