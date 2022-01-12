package kr.co.aim.messolution.userprofile.event;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.userprofile.MESUserServiceProxy;
import kr.co.aim.messolution.userprofile.info.HistoryInfo;
import kr.co.aim.messolution.userprofile.management.data.UserInvisibleButton;
import kr.co.aim.messolution.userprofile.management.data.UserInvisibleButtonHist;
import kr.co.aim.messolution.userprofile.management.data.UserInvisibleButtonKey;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;

public class GrantButtonPermission extends SyncHandler {

	Log log = LogFactory.getLog(GrantButtonPermission.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String loginUser = SMessageUtil.getBodyItemValue(doc, "EVENTUSER", true);
		String userId = SMessageUtil.getBodyItemValue(doc, "USERID", true);
		List<Element> buttonElmentList = SMessageUtil.getBodySequenceItemList(doc, "BUTTONLIST", true);

		List<UserInvisibleButton> buttonDataList = null;

		try
		{
			buttonDataList = MESUserServiceProxy.getUserInvisibleButtonService().select("WHERE 1=1 AND USERID = ? ORDER BY MENUNAME ", new Object[] { userId });
		}
		catch (Exception ex)
		{
			if (ex instanceof NotFoundSignal)
				log.info(" UserInvisibleButton Data Information is Empty.Search by UserId = " + userId);
			else
				throw new CustomException(ex.getCause());
		}

		List<Element> btnDisableElementList = new ArrayList<>();
		List<Element> btnEnableElementList = new ArrayList<>();

		this.classificationByAccessMode(buttonElmentList, btnEnableElementList, btnDisableElementList);

		Map<String, Map<String, UserInvisibleButton>> menuButtonDataMap = new HashMap<>();
		menuButtonDataMap = this.makeMenuButtonDataMap(buttonDataList);

		if (btnDisableElementList.size() > 0)
		{
			for (Element subElement : btnDisableElementList)
			{
				String menuName = subElement.getChildText("MENUNAME");
				String buttonName = subElement.getChildText("BUTTONNAME");
				String factoryName = subElement.getChildText("FACTORYNAME");

				if (menuButtonDataMap.keySet().contains(menuName) && menuButtonDataMap.get(menuName).containsKey(buttonName))
				{
					log.info(String.format("Button [User:%s,Menu:%s,Button:%s] is already disabled. ", userId, menuName, buttonName));
				}
				else
				{
					UserInvisibleButton dataInfo = new UserInvisibleButton();
					dataInfo.setKey(new UserInvisibleButtonKey(userId, menuName, buttonName));
					dataInfo.getUdfs().put("FACTORYNAME", factoryName);

					MESUserServiceProxy.getUserInvisibleButtonService().insert(dataInfo);

					HistoryInfo histInfo = MESUserServiceProxy.getUserInvisibleButtonHistService().makeHistoryInfo(loginUser, "Insert", this.getEventComment());
					MESUserServiceProxy.getUserInvisibleButtonHistService().insertHistory(dataInfo, histInfo, UserInvisibleButtonHist.class);
				}
			}
		}

		if (btnEnableElementList.size() > 0)
		{
			for (Element subElement : btnEnableElementList)
			{
				String menuName = subElement.getChildText("MENUNAME");
				String buttonName = subElement.getChildText("BUTTONNAME");

				if (menuButtonDataMap.keySet().contains(menuName) && menuButtonDataMap.get(menuName).containsKey(buttonName))
				{
					UserInvisibleButton dataInfo = menuButtonDataMap.get(menuName).get(buttonName);
					MESUserServiceProxy.getUserInvisibleButtonService().delete(dataInfo.getKey());

					HistoryInfo histInfo = MESUserServiceProxy.getUserInvisibleButtonHistService().makeHistoryInfo(loginUser, "Delete", this.getEventComment());
					MESUserServiceProxy.getUserInvisibleButtonHistService().insertHistory(dataInfo, histInfo, UserInvisibleButtonHist.class);
				}
				else
				{
					log.info(String.format("Button [User:%s,Menu:%s,Button:%s] is already enabled. ", userId, menuName, buttonName));
				}
			}
		}

		return doc;
	}
	
	private Map<String,Map<String,UserInvisibleButton>> makeMenuButtonDataMap(List<UserInvisibleButton> buttonDataList)
	{
		if(buttonDataList == null || buttonDataList.size() ==0) return new HashMap<String,Map<String,UserInvisibleButton>>();
		
		Map<String,Map<String,UserInvisibleButton>> menuButtonDataMap = new HashMap<>();
		
		for(UserInvisibleButton uButton : buttonDataList)
		{
			String menuName = uButton.getKey().getMenuName();
			String buttonName = uButton.getKey().getButtonName();
			
			if (menuButtonDataMap.keySet().contains(menuName))
			{
				menuButtonDataMap.get(menuName).put(buttonName, uButton);
			}
			else
			{
				Map<String, UserInvisibleButton> buttonDataMap = new HashMap<>();
				buttonDataMap.put(buttonName, uButton);

				menuButtonDataMap.put(menuName, buttonDataMap);
			}
		}
		
		return menuButtonDataMap;
		
	}
	
	private void classificationByAccessMode(List<Element> sourceElementList,List<Element> enableElementList, List<Element> disableElementList)
	{
		for (Element sourceElement : sourceElementList)
		{
			String accessMode = sourceElement.getChildText("ACCESSMODE");

			if ("Enable".equals(accessMode))
			{
				enableElementList.add(sourceElement);
			}
			else if ("Disable".equals(accessMode))
			{
				disableElementList.add(sourceElement);
			}
			else
			{
				log.info(String.format("Invalid AccessMode[%s] type.", accessMode));
			}
		}
	}
	
}
