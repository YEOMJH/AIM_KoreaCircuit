package kr.co.aim.messolution.userprofile.event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

public class ModifyUserMenu extends SyncHandler{

	@Override
	public Object doWorks(Document doc) throws CustomException {
		Element menuList = SMessageUtil.getBodySequenceItem(doc, "MENULIST", true);
		String mainView = SMessageUtil.getBodyItemValue(doc, "MAINVIEW", true);
		String secondView = SMessageUtil.getBodyItemValue(doc, "SECONDVIEW", true);
		String currentUserID = SMessageUtil.getBodyItemValue(doc, "USERID", true);
		String shop = SMessageUtil.getBodyItemValue(doc, "SHOPID", true);
		String sql = "";
		if (menuList != null)
		{
			for ( Iterator iteratorLotList = menuList.getChildren().iterator(); iteratorLotList.hasNext();)
			{
				Element lotE = (Element) iteratorLotList.next();
				
				String menuName        = SMessageUtil.getChildText(lotE, "MENUNAME", true);
				String factoryName    = SMessageUtil.getChildText(lotE, "FACTORY", true);
				
				//delete sql
				sql = "DELETE FROM CT_USERDETAILMENU " +
							 "WHERE USERID = :USERID  AND FACTORY = :FACTORY AND MENUNAME = :MENUNAME";
				Map<String,Object> bindMap = new HashMap<String,Object>();
				bindMap.put("USERID", currentUserID);
				bindMap.put("FACTORY", factoryName);
				bindMap.put("MENUNAME", menuName);
				
				int sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
			}
		}
		
		//inquiry sql
		 sql =  "SELECT CT.MENUNAME, CT.FACTORY, MU.SUPERMENUNAME " +
				"FROM CT_USERDETAILMENU CT , MENU MU " +
				"WHERE CT.MENUNAME = MU.MENUNAME " +
				"AND MU.UINAME = 'OIC' AND CT.USERID = :USERID " +
				"AND MU.SUPERMENUNAME = :SUPERMENUNAME AND CT.FACTORY = :FACTORY ";
		 Map<String,Object> bindSet = new HashMap<String,Object>();
		 bindSet.put("USERID", currentUserID);
		 bindSet.put("SUPERMENUNAME", secondView);
		 bindSet.put("FACTORY", shop);
		 
		 List<Map<String, Object>> SecondResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindSet);
		 if(SecondResult.size() == 0)
		 {
			 sql = "DELETE FROM CT_USERDETAILMENU " +
			 "WHERE USERID = :USERID  AND FACTORY = :FACTORY AND MENUNAME = :MENUNAME";
			 Map<String,Object> bindMap = new HashMap<String,Object>();
			 bindMap.put("USERID", currentUserID);
			 bindMap.put("FACTORY", shop);
			 bindMap.put("MENUNAME", secondView);
				
			 int sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		 }
		 
		 sql =  "SELECT CT.MENUNAME, CT.FACTORY, MU.SUPERMENUNAME " +
			"FROM CT_USERDETAILMENU CT , MENU MU " +
			"WHERE CT.MENUNAME = MU.MENUNAME " +
			"AND MU.UINAME = 'OIC' AND CT.USERID = :USERID " +
			"AND MU.SUPERMENUNAME = :SUPERMENUNAME AND CT.FACTORY = :FACTORY ";
		 bindSet.clear();
		 bindSet.put("USERID", currentUserID);
		 bindSet.put("SUPERMENUNAME", mainView);
		 bindSet.put("FACTORY", shop);
		 
		 List<Map<String, Object>> MainResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindSet);
		 if(MainResult.size() == 0)
		 {
			 sql = "DELETE FROM CT_USERDETAILMENU " +
			 "WHERE USERID = :USERID  AND FACTORY = :FACTORY AND MENUNAME = :MENUNAME";
			 Map<String,Object> bindMap = new HashMap<String,Object>();
			 bindMap.put("USERID", currentUserID);
			 bindMap.put("FACTORY", shop);
			 bindMap.put("MENUNAME", mainView);
				
			 int sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		 }
		return doc;
	}

}
