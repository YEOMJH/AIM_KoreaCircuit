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

public class EducationResult extends SyncHandler {	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		List<Element> educationList = SMessageUtil.getBodySequenceItemList(doc, "EDUCATIONRESULTLIST", true);

		List<Object[]> updateUserArgsList = new ArrayList<Object[]>();
		List<Object[]> updateEducationArgsList = new ArrayList<Object[]>();

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("EducationResult", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		eventInfo.setEventComment("EducationResult");

		for (Element education : educationList)
		{
			String userID = SMessageUtil.getChildText(education, "USERID", true);
			String occupaTion = SMessageUtil.getChildText(education, "OCCUPATION", false);
			String detailMenuName = SMessageUtil.getChildText(education, "MENUNAME", true);
			
			List<Object> updateBindList = new ArrayList<Object>();
			
		
			updateBindList.add("W");
			updateBindList.add(userID);
			updateBindList.add(detailMenuName);
						
			
			updateUserArgsList.add(updateBindList.toArray());
			
			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE USERPROFILEMENU   ");
			sql.append("SET ACCESSFLAG = ? ");
			sql.append("WHERE USERID = ? ");
			sql.append("AND( MENUNAME IS NULL OR MENUNAME = ?) ");
			
			GenericServiceProxy.getSqlMesTemplate().updateBatch(sql.toString(), updateUserArgsList);
			
			

			List<Object> educationBindList = new ArrayList<Object>();
			
			educationBindList.add("Y");			
			educationBindList.add(occupaTion);
			educationBindList.add(eventInfo.getEventComment());
			educationBindList.add(eventInfo.getEventName());
			educationBindList.add(eventInfo.getEventUser());
			educationBindList.add(eventInfo.getEventTime());
			educationBindList.add(eventInfo.getEventTimeKey());
			educationBindList.add(userID);				
			educationBindList.add(detailMenuName);
				
					
			
			updateEducationArgsList.add(educationBindList.toArray());
			
			StringBuffer sql2 = new StringBuffer();
			sql2.append("UPDATE CT_EDUCATION    ");
			sql2.append("SET EDUCATIONFLAG = ?, OCCUPATION = ? ,  LASTEVENTCOMMENT = ?, LASTEVENTNAME = ?,    ");
			sql2.append("LASTEVENTUSER = ?, LASTEVENTTIME = ?, LASTEVENTTIMEKEY = ?    ");
			sql2.append("WHERE USERID = ?    ");
			sql2.append("AND ( MENUNAME IS NULL OR MENUNAME = ?)    ");
			
			GenericServiceProxy.getSqlMesTemplate().updateBatch(sql2.toString(), updateEducationArgsList);
		}
		
		
		return doc;
	}
}