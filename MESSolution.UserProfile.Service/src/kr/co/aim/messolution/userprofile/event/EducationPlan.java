package kr.co.aim.messolution.userprofile.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ShieldLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import kr.co.aim.messolution.userprofile.MESUserServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.user.management.data.UserProfile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class EducationPlan extends SyncHandler {	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		List<Element> educationList = SMessageUtil.getBodySequenceItemList(doc, "EDUCATIONLIST", true);

		List<Object[]> updateUserArgsList = new ArrayList<Object[]>();
		List<Object[]> insertEduArgsList = new ArrayList<Object[]>();	
		List<Object[]> updateGroupEduArgsList = new ArrayList<Object[]>();
		List<Object[]> updateEducationArgsList = new ArrayList<Object[]>();

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("EducationPlan", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		eventInfo.setEventComment("EducationPlan");

		for (Element education : educationList)
		{
			String userID = SMessageUtil.getChildText(education, "USERID", true);
			String educationFlag = SMessageUtil.getChildText(education, "EDUCATIONFLAG", true);
			String detailMenuName = SMessageUtil.getChildText(education, "MENUNAME", true);	
			String occuPation=SMessageUtil.getChildText(education, "OCCUPATION", false);	
			String departMent = SMessageUtil.getChildText(education, "DEPARTMENT", false);
			String factoryName;
			
			
			List<Object> updateBindList = new ArrayList<Object>();
			List<Object> insertBindList = new ArrayList<Object>();			
			List<Object> updateGroupBindList = new ArrayList<Object>();

			// GET USER FACTORY
			StringBuffer inquirysql6 = new StringBuffer();
			inquirysql6.append("SELECT FACTORYNAME ");
			inquirysql6.append("  FROM USERGROUPMENU ");
			inquirysql6.append("  WHERE USERGROUPNAME = ? ");
			inquirysql6.append(" AND UINAME = 'OIC' ");
			inquirysql6.append(" AND MENUNAME = ? ");

			Map<String, String> inquirybindMap6 = new HashMap<String, String>();
			inquirybindMap6.put("USERGROUPNAME", departMent);			
			inquirybindMap6.put("MENUNAME", detailMenuName);
			
			List<Map<String, Object>> sqlResult6 = GenericServiceProxy.getSqlMesTemplate().queryForList(inquirysql6.toString(), inquirybindMap6);
			
			if (sqlResult6 != null && sqlResult6.size() > 0){
				factoryName = sqlResult6.get(0).get("FACTORYNAME").toString();
			}else{
				factoryName ="";
			}
			// CT_EDUCATION
			StringBuffer inquirysql = new StringBuffer();
			inquirysql.append("SELECT FACTORYNAME, MENUNAME, USERID ");
			inquirysql.append("  FROM CT_EDUCATION ");
			inquirysql.append(" WHERE MENUNAME = :MENUNAME ");
			inquirysql.append(" AND USERID = :USERID ");

			Map<String, String> inquirybindMap = new HashMap<String, String>();
			inquirybindMap.put("MENUNAME", detailMenuName);
			inquirybindMap.put("USERID", userID);
			
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(inquirysql.toString(), inquirybindMap);
			
			StringBuffer inquirysql5 = new StringBuffer();
			inquirysql5.append("SELECT  USERID,DEPARTMENT ");
			inquirysql5.append("  FROM USERPROFILE ");
			inquirysql5.append(" WHERE USERID = :USERID ");

			Map<String, String> inquirybindMap5 = new HashMap<String, String>();
			inquirybindMap5.put("USERID", userID);
			
			List<Map<String, Object>> sqlResult5 = GenericServiceProxy.getSqlMesTemplate().queryForList(inquirysql5.toString(), inquirybindMap5);

			if (sqlResult5 != null && sqlResult5.size() > 0){
				if (sqlResult != null && sqlResult.size()>0 ){
					
					List<Object> educationBindList = new ArrayList<Object>();
							
					educationBindList.add(educationFlag);
					educationBindList.add(eventInfo.getEventComment());
					educationBindList.add(eventInfo.getEventName());
					educationBindList.add(eventInfo.getEventUser());
					educationBindList.add(eventInfo.getEventTime());
					educationBindList.add(eventInfo.getEventTimeKey());
					educationBindList.add(userID);				
					educationBindList.add(detailMenuName);
							
					
					updateEducationArgsList.add(educationBindList.toArray());
					
				}else
				{
					
					insertBindList.add(factoryName);
					insertBindList.add(detailMenuName);
					insertBindList.add(userID);
					insertBindList.add(sqlResult5.get(0).get("DEPARTMENT").toString());
					insertBindList.add(educationFlag);
					insertBindList.add(occuPation);
					insertBindList.add(eventInfo.getEventComment());
					insertBindList.add(eventInfo.getEventName());
					insertBindList.add(eventInfo.getEventUser());
					insertBindList.add(eventInfo.getEventTime());
					insertBindList.add(eventInfo.getEventTimeKey());
					
					insertEduArgsList.add(insertBindList.toArray());	
				}
			}
			else
			{
				throw new CustomException("EDUCATIONFLAG-0001", userID);
			}
		}

		if(updateEducationArgsList.size()>0){
			
			StringBuffer sql3 = new StringBuffer();
			sql3.append("UPDATE CT_EDUCATION    ");
			sql3.append("SET EDUCATIONFLAG = ?,LASTEVENTCOMMENT = ?, LASTEVENTNAME = ?,    ");
			sql3.append("LASTEVENTUSER = ?, LASTEVENTTIME = ?, LASTEVENTTIMEKEY = ?    ");
			sql3.append("WHERE USERID = ?    ");
			sql3.append("AND MENUNAME = ?    ");
			
			GenericServiceProxy.getSqlMesTemplate().updateBatch(sql3.toString(), updateEducationArgsList);	
		}
		if(insertEduArgsList.size()>0){
			StringBuffer sql2 = new StringBuffer();
			sql2.append("INSERT INTO CT_EDUCATION ");
			sql2.append("(FACTORYNAME, MENUNAME, USERID,  DEPARTMENT, "); 
			sql2.append("EDUCATIONFLAG, OCCUPATION,LASTEVENTCOMMENT, LASTEVENTNAME, LASTEVENTUSER, LASTEVENTTIME, "); 
			sql2.append ("   LASTEVENTTIMEKEY)" );
			sql2.append(" VALUES  ");
			sql2.append("(?,?,?,?,?,?,?,?,?,?,?)"); 
			
			GenericServiceProxy.getSqlMesTemplate().updateBatch(sql2.toString(), insertEduArgsList);
		}
		return doc;
	}
}