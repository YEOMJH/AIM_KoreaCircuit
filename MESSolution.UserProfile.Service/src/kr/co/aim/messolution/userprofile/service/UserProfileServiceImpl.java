package kr.co.aim.messolution.userprofile.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.user.UserServiceProxy;
import kr.co.aim.greentrack.user.management.data.UserProfileKey;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class UserProfileServiceImpl implements ApplicationContextAware{
	private ApplicationContext     	applicationContext;
	private static Log 				log = LogFactory.getLog(UserProfileServiceImpl.class);
			
	public void setApplicationContext(ApplicationContext arg0)throws BeansException 
	{
			applicationContext = arg0;
	}

	public void login(String userId,
				  	  String password,
					  String uiName,
					  String workStationName)
	throws kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, Exception
	{
		
		this.verifyUser(userId);
		
		this.verifyPassword(userId, password);
		
		try
		{
			UserServiceProxy.getUserProfileService().logIn(userId, password, uiName, workStationName);
		}
		catch ( Exception e)
		{
			log.error(e);
			this.logOut(userId, uiName, workStationName);
			
			UserServiceProxy.getUserProfileService().logIn(userId, password, uiName, workStationName);
		}
	}

	public void logOut(String userId,
					   String uiName, 
					   String workStationName)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		try{
			UserServiceProxy.getUserProfileService().logOut(userId, uiName, workStationName);
		}catch(Exception e){
			log.warn(e);
		}
	}

	public void changePassword(String userId, String oldPassword, String newPassword)
	{
		UserServiceProxy.getUserProfileService().changePassword(userId, oldPassword, newPassword);
	}

	public void verifyPassword(String userId,
							   String password)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, Exception
	
	{
        if ( !(UserServiceProxy.getUserProfileService().verifyPassword(userId, password)))
        {
        	throw new CustomException("USER-0002", userId, password);
        }
	}

	public void removeUserProfile(String userId){
		UserServiceProxy.getUserProfileService().removeUserProfile(userId);
	}

	public void verifyUser(String userId) throws Exception
	{
		UserProfileKey userProfileKey = new UserProfileKey();
		
		userProfileKey.setUserId( userId );
		
		try
		{
			UserServiceProxy.getUserProfileService().selectByKey( userProfileKey );
		}
		catch ( Exception e )
		{
			throw new CustomException("USER-0006", userId);
		}
	}
	
	public void changeShop(String factoryName, String areaName, String eventUser, String eventName) throws CustomException
	{
//		String sql = " SELECT UPPER(G.ACCESSFACTORY) AS ACCESSFACTORY, G.USERGROUPNAME "
//				   + " FROM USERPROFILE P, USERGROUP G "
//				   + " WHERE P.USERGROUPNAME = G.USERGROUPNAME "
//				   + " AND P.USERID = :USERNAME "
//				   + " UNION "
//				   + " SELECT DISTINCT UPPER (U.FACTORYNAME) AS ACCESSFACTORY, P.USERGROUPNAME "
//				   + " FROM USERPROFILEMENU U, USERPROFILE P "
//				   + " WHERE U.USERID = P.USERID "
//				   + " AND U.USERID = :USERNAME ";
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT UPPER (G.ACCESSFACTORY) AS ACCESSFACTORY, G.USERGROUPNAME ");
		sql.append("  FROM USERPROFILE P, USERGROUP G ");
		sql.append(" WHERE P.USERGROUPNAME = G.USERGROUPNAME ");
		sql.append("   AND P.USERID = :USERNAME ");
		sql.append("UNION ");
		sql.append("SELECT DISTINCT UPPER (U.FACTORYNAME) AS ACCESSFACTORY, P.USERGROUPNAME ");
		sql.append("  FROM USERPROFILEMENU U, USERPROFILE P ");
		sql.append(" WHERE U.USERID = P.USERID ");
		sql.append("   AND U.USERID = :USERNAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("USERNAME", eventUser);
		
//		Map<String, String> bindMap = new HashMap<String, String>();
//		bindMap.put("USERNAME", eventUser);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (sqlResult.size() > 0)
		{
			String userGroupName = ConvertUtil.getMapValueByName(sqlResult.get(0), "USERGROUPNAME");

			for (int k = 0; k < sqlResult.size(); k++)
			{
				String accessFactory = (String) sqlResult.get(k).get("ACCESSFACTORY");
				if (StringUtils.equals(accessFactory, factoryName))
				{
					return;
				}
				else if (StringUtils.equals(accessFactory, "ALL") || StringUtils.isEmpty(accessFactory))
				{
					return;
				}
				else if (accessFactory.contains(","))
				{
					String[] accessFactorys = StringUtils.split(accessFactory, ",");
					for (int i = 0; accessFactorys.length > i; i++)
					{
						if (StringUtils.equals(accessFactorys[i], factoryName))
						{
							return;
						}
					}
				}
			}

			throw new CustomException("USER-0001", eventUser, userGroupName, factoryName);
		}
		else
		{
			throw new CustomException("USER-0001", eventUser, "", factoryName);
		}
		
	}
	
	//大小写字母、数字、特殊字符至少3种，密码长度大于8位，小于30位
	public boolean rexCheckPassword(String input) {
		// 8-30 位，字母、数字、字符
		String regStr = "^(?![a-zA-Z]+$)(?![A-Z0-9]+$)(?![A-Z\\W_]+$)(?![a-z0-9]+$)(?![a-z\\W_]+$)(?![0-9\\W_]+$)[a-zA-Z0-9\\W_]{8,30}$";
		return input.matches(regStr);
		}	 
}
