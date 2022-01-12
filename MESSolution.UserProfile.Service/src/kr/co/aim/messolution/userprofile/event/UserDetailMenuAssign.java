package kr.co.aim.messolution.userprofile.event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.userprofile.MESUserServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class UserDetailMenuAssign extends SyncHandler {
	public static Log log = LogFactory.getLog(UserDetailMenuAssign.class);
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String messageName = SMessageUtil.getMessageName(doc);
		Element menuList = SMessageUtil.getBodySequenceItem(doc, "MENULIST", true);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UserDetailMenuAssign", getEventUser(), getEventComment(), "", "");
		
		if ( menuList != null)
		{
			for(Iterator iteratorLotList = menuList.getChildren().iterator(); iteratorLotList.hasNext();)
			{
				Element menuE = (Element) iteratorLotList.next();
				String userID     = SMessageUtil.getChildText(menuE, "USERID", true);
				String uiName     = SMessageUtil.getChildText(menuE, "UINAME", true);
				String menuName    = SMessageUtil.getChildText(menuE, "MENUNAME", true);
				String factoryName = SMessageUtil.getChildText(menuE, "FACTORYNAME", true);
				String levelNO     = SMessageUtil.getChildText(menuE, "LEVELNO", true);
								
				String sql = " INSERT INTO CT_USERDETAILMENU (USERID, UINAME, MENUNAME, FACTORY, LEVELNO, TIMEKEY) "
							+ " VALUES (:USERID, :UINAME, :MENUNAME, :FACTORY, :LEVELNO, :TIMEKEY) ";
				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("USERID", userID);
				bindMap.put("UINAME", uiName);
				bindMap.put("MENUNAME", menuName);
				bindMap.put("FACTORY", factoryName);
				bindMap.put("LEVELNO", levelNO);
				bindMap.put("TIMEKEY", TimeStampUtil.getCurrentEventTimeKey());
				
				String sqlQuery = " SELECT USERID, UINAME, MENUNAME, FACTORY, LEVELNO FROM CT_USERDETAILMENU "
									+ " WHERE USERID = :USERID AND UINAME = :UINAME "
									+ " AND MENUNAME = :MENUNAME AND FACTORY = :FACTORY AND LEVELNO = :LEVELNO";
				List<Map<String, Object>> Result = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlQuery, bindMap);
				if( Result.size() > 0)
				{
					log.info("Already exists! ");
					continue;
				}
				try
				{
					int i = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
				}
				catch(Exception e)
				{
					throw new CustomException(e);
				}
			}
		}
		return doc;
	}
}