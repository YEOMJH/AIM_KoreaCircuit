package kr.co.aim.messolution.userprofile.event;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.userprofile.MESUserServiceProxy;
import kr.co.aim.messolution.userprofile.info.HistoryInfo;
import kr.co.aim.messolution.userprofile.management.data.UserProfileMenuHistory;
import kr.co.aim.greenframe.util.bundle.BundleUtil;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.ExceptionKey;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.user.UserServiceProxy;
import kr.co.aim.greentrack.user.management.UserProfileMenuService;
import kr.co.aim.greentrack.user.management.data.UserGroupMenu;
import kr.co.aim.greentrack.user.management.data.UserGroupMenuKey;
import kr.co.aim.greentrack.user.management.data.UserProfile;
import kr.co.aim.greentrack.user.management.data.UserProfileKey;
import kr.co.aim.greentrack.user.management.data.UserProfileMenu;
import kr.co.aim.greentrack.user.management.data.UserProfileMenuKey;

public class GrantMenuPermission extends SyncHandler {

	Log log = LogFactory.getLog(GrantMenuPermission.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String userId = SMessageUtil.getBodyItemValue(doc, "USERID", true);
		String loginUser = SMessageUtil.getBodyItemValue(doc, "EVENTUSER", true);
		String validateMode = SMessageUtil.getBodyItemValue(doc, "VALIDATEMODE", false);
		List<Element> menuElementList = SMessageUtil.getBodySequenceItemList(doc, "MENULIST", false);
	
		List<UserProfileMenu> userMenuDataList = null;
		UserProfileMenuService uProfileMenuService = (UserProfileMenuService) BundleUtil.waitForServiceByBeanName(UserProfileMenuService.class.getSimpleName());

		try
		{
			userMenuDataList = uProfileMenuService.select("WHERE 1=1 AND USERID = ? AND UINAME = 'OIC' ", new Object[] { userId });
		}
		catch (Exception ex)
		{
			if (ex instanceof NotFoundSignal)
				log.info(String.format("UserProfileMenu Data Information is Empty. Search by UserId = " + userId));
			else
				throw new CustomException(ex.getCause());
		}
		
		Map<String,UserProfileMenu> profileMenuDataMap = this.makeProfileMenuDataMap(userMenuDataList);
		List<UserProfileMenu> remainMenuDataList = new ArrayList<>(profileMenuDataMap.values());
		
		for(Element subElement : menuElementList)
		{
			String menuName = subElement.getChildText("MENUNAME");
			String accessFlag = subElement.getChildText("ACCESSFLAG");
			String accessFactory = subElement.getChildText("ACCESSFACTORY");

			if ("Y".equals(validateMode))
				this.beforeInsertOrUpdate(userId, "OIC", menuName, accessFlag);
			
			if (profileMenuDataMap.containsKey(menuName))
			{
				UserProfileMenu dataInfo = profileMenuDataMap.get(menuName);

				if (!accessFlag.equals(profileMenuDataMap.get(menuName).getAccessFlag()) || !dataInfo.getUdfs().get("FACTORYNAME").equals(accessFactory))
				{
					try
					{
						dataInfo.setAccessFlag(accessFlag);
						dataInfo.getUdfs().put("FACTORYNAME", accessFactory);

						uProfileMenuService.update(dataInfo);

						HistoryInfo histInfo = MESUserServiceProxy.getUserInvisibleButtonHistService().makeHistoryInfo(loginUser, "Update", this.getEventComment());
						MESUserServiceProxy.getUserProfileMenuHistoryService().insertHistory(dataInfo, histInfo, UserProfileMenuHistory.class);
					}
					catch (Exception ex)
					{
						if (!(ex instanceof DuplicateNameSignal))
							throw new CustomException(ex.getCause());
					}
				}
			}
			else
			{
				try
				{
					UserProfileMenu dataInfo = new UserProfileMenu();
					dataInfo.setKey(new UserProfileMenuKey(userId, "OIC", menuName));
					dataInfo.setAccessFlag(accessFlag);
					dataInfo.getUdfs().put("FACTORYNAME", accessFactory);

					uProfileMenuService.insert(dataInfo);

					HistoryInfo histInfo = MESUserServiceProxy.getUserInvisibleButtonHistService().makeHistoryInfo(loginUser, "Insert", this.getEventComment());
					MESUserServiceProxy.getUserProfileMenuHistoryService().insertHistory(dataInfo, histInfo, UserProfileMenuHistory.class);
				}
				catch (Exception ex)
				{
					if (ex instanceof DuplicateNameSignal)
						log.info(String.format("▶Menu [UserId=%s ,UIName=%s,MenuName=%s] is already exists.", userId, "OIC", menuName));
					else
						throw new CustomException(ex.getCause());
				}
			}

			remainMenuDataList.remove(profileMenuDataMap.get(menuName));
		}

		if (remainMenuDataList != null && remainMenuDataList.size() > 0)
		{
			for (UserProfileMenu menuData : remainMenuDataList)
			{
				try
				{
					uProfileMenuService.delete(menuData.getKey());

					HistoryInfo histInfo = MESUserServiceProxy.getUserInvisibleButtonHistService().makeHistoryInfo(loginUser, "Delete", this.getEventComment());
					MESUserServiceProxy.getUserProfileMenuHistoryService().insertHistory(menuData, histInfo, UserProfileMenuHistory.class);
				}
				catch (Exception ex)
				{
					if (ex instanceof NotFoundSignal)
						log.info(String.format("▶Menu [UserId=%s ,UIName=%s,MenuName=%s] is already deleted.", userId, "OIC", menuData.getKey().getMenuName()));
					else
						throw new CustomException(ex.getCause());
				}
			}
		}

		return doc;
	}
	
	private Map<String,UserProfileMenu> makeProfileMenuDataMap(List<UserProfileMenu> dataInfoList)
	{
		if (dataInfoList == null || dataInfoList.size() == 0) return new HashMap<String, UserProfileMenu>();

		Map<String, UserProfileMenu> profileMenuDataMap = new HashMap<>();

		for (UserProfileMenu dataInfo : dataInfoList)
		{
			profileMenuDataMap.put(dataInfo.getKey().getMenuName(), dataInfo);
		}
		
		return profileMenuDataMap;
	}
	
	private void checkAccessFlag(String accessFlag) throws FrameworkErrorSignal
	{
		if (StringUtils.equals(accessFlag, "R") == false
			&& StringUtils.equals(accessFlag, "W") == false
			&& StringUtils.equals(accessFlag, "X") == false)
		{
			throw new FrameworkErrorSignal(ExceptionKey.InvalidArguments_Exception, accessFlag, "R, W, X");
		}
	}
	
	private void beforeInsertOrUpdate(String userId, String uiName, String menuName, String accessFlag)throws FrameworkErrorSignal, NotFoundSignal
	{

		// 1. AccessFalg validation
		checkAccessFlag(accessFlag);

		// 2. UserProfile ��ȸ
		UserProfile userProfile = UserServiceProxy.getUserProfileService().selectByKey(new UserProfileKey(userId));

		// 3. UserGroupMenu ��ȸ
		try
		{
			UserGroupMenuKey key = new UserGroupMenuKey(userProfile.getUserGroupName(), uiName, menuName);
			UserGroupMenu userGroupMenu = UserServiceProxy.getUserGroupMenuService().selectByKey(key);
			if (StringUtils.equals(userGroupMenu.getAccessFlag(), accessFlag))
			{
				throw new FrameworkErrorSignal(ExceptionKey.User_DuplicateMenu, ObjectUtil.getString(key), menuName,
						userProfile.getUserGroupName());
			}
		} catch (NotFoundSignal e)
		{
			// UserGroupMenu �� ��� ���� �������� ����̴�.
		}
	}
}
