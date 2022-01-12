package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;

public class MaintainEnumDefValueData extends SyncHandler
{
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String sEnumvalue = SMessageUtil.getBodyItemValue(doc, "ENUMVALUE", true);
		String sDefaultFlag = SMessageUtil.getBodyItemValue(doc, "DEFAULTFLAG", false);
		String sDescription = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
		String sSeq = SMessageUtil.getBodyItemValue(doc, "SEQ", false);
		String sDisColor = SMessageUtil.getBodyItemValue(doc, "DISPLAYCOLOR", false);
		String sOperation = SMessageUtil.getBodyItemValue(doc, "sOperation", false);
		String sEnumname = SMessageUtil.getBodyItemValue(doc, "ENUMNAME", false);
		String sUpdateValue = SMessageUtil.getBodyItemValue(doc, "UPDATEVALUE", false);
		String sUpdateDesc = SMessageUtil.getBodyItemValue(doc, "UPDATEDESC", false);
		String sUpdateFlag = SMessageUtil.getBodyItemValue(doc, "UPDATEFLAG", false);
		String selectUpdateEnumsql = "SELECT ENUMNAME,ENUMVALUE FROM ENUMDEFVALUE A WHERE A.ENUMNAME='UpdateEnum' AND A.ENUMVALUE =:ENUMVALUE";
		Map<String,Object> bindSet1 = new HashMap<String,Object>();
		bindSet1.put("ENUMVALUE"	, sEnumname);		
		List<Map<String, Object>> sqlResult1 = GenericServiceProxy.getSqlMesTemplate().queryForList(selectUpdateEnumsql, bindSet1);
		
		if(sqlResult1.size()>0)
		{		
			if(sOperation.equals("Insert"))
			{
				String insertsql = "INSERT INTO ENUMDEFVALUE(ENUMNAME,ENUMVALUE,DESCRIPTION,DEFAULTFLAG,DISPLAYCOLOR,SEQ) VALUES(:ENUMNAME,:ENUMVALUE,:DESCRIPTION,:DEFAULTFLAG,:DISPLAYCOLOR,:SEQ)";
				Map<String,Object> bindinsert = new HashMap<String,Object>();
				bindinsert.put("ENUMNAME"	, sEnumname);
				bindinsert.put("ENUMVALUE"	, sEnumvalue);
				bindinsert.put("DESCRIPTION"	, sDescription);
				bindinsert.put("DEFAULTFLAG"	, sDefaultFlag);
				bindinsert.put("DISPLAYCOLOR"	, sDisColor);
				bindinsert.put("SEQ"	, sSeq);
				try
				{
					GenericServiceProxy.getSqlMesTemplate().update(insertsql, bindinsert);
				}
				catch(Exception e)
				{
					throw new CustomException("SYS-8001", e.getMessage());
				}
				//add history
				addEnumDefValueHistory( sEnumname, sEnumvalue, sDescription, sDefaultFlag, sDisColor , sSeq , "Insert");
				
				return doc;
			}
			else if(sOperation.equals("Delete"))
			{
				Map<String,Object> bindDelete = new HashMap<String,Object>();
				String delectsql = "DELETE FROM ENUMDEFVALUE where ENUMNAME=:ENUMNAME and ENUMVALUE=:ENUMVALUE and DEFAULTFLAG=:DEFAULTFLAG AND DESCRIPTION=:DESCRIPTION";
				bindDelete.put("ENUMNAME"	, sEnumname);
				bindDelete.put("ENUMVALUE"	, sEnumvalue);						
				bindDelete.put("DEFAULTFLAG"	, sDefaultFlag);		
				bindDelete.put("DESCRIPTION"	, sDescription);				
				try
				{
					GenericServiceProxy.getSqlMesTemplate().update(delectsql, bindDelete);
				}
				catch(Exception e)
				{
					throw new CustomException("SYS-8001", e.getMessage());
				}
				//add history
				addEnumDefValueHistory( sEnumname, sEnumvalue, sDescription, sDefaultFlag, sDisColor , sSeq , "Delete");
				
				return doc;
			}			
		}
		if(sOperation.equals("Update"))
		{
			String sql = "UPDATE  ENUMDEFVALUE A  SET A.ENUMVALUE=:ENUMVALUE,A.DESCRIPTION=:DESCRIPTION,A.DEFAULTFLAG=:DEFAULTFLAG,A.DISPLAYCOLOR=:DISPLAYCOLOR,A.SEQ=:SEQ" +
	        		"  WHERE A.ENUMNAME=:ENUMNAME  AND A.ENUMVALUE =:UPDATEVALUE  ";
	        
			Map<String,Object> bindMap = new HashMap<String,Object>();
			bindMap.put("ENUMVALUE"	, sEnumvalue);
			bindMap.put("DEFAULTFLAG"	, sDefaultFlag);
			bindMap.put("DESCRIPTION"	, sDescription);
			bindMap.put("SEQ"    , sSeq);
			bindMap.put("DISPLAYCOLOR", sDisColor);
			bindMap.put("UPDATEVALUE", sUpdateValue);
			//Where 
//			if(!sUpdateFlag.isEmpty())
//			{
//				sql += " AND A.DEFAULTFLAG=:UPDATEFLAG ";
//				bindMap.put("UPDATEFLAG"	, sUpdateFlag);
//			}
//			if(!sUpdateDesc.isEmpty())
//			{
//				sql += " AND A.DESCRIPTION =:UPDATEDESC ";
//				bindMap.put("UPDATEDESC", sUpdateDesc);
//			}	
//			if(!sUpdateValue.isEmpty())
//			{
//				sql += " AND A.ENUMVALUE =:UPDATEVALUE ";
//				bindMap.put("UPDATEVALUE", sUpdateValue);
//			}	
			bindMap.put("ENUMNAME", sEnumname);
			
			try
			{
				GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
			}
			catch(Exception e)
			{
				throw new CustomException("SYS-8001", e.getMessage());
			}
			//add history
			addEnumDefValueHistory( sEnumname, sEnumvalue, sDescription, sDefaultFlag, sDisColor , sSeq , "Update");
		}
        
		
		return doc;
	}
	
	public void addEnumDefValueHistory(String sEnumname,String sEnumvalue,String sDescription,String sDefaultFlag,String sDisColor ,String sSeq ,String eventName)
	{
		StringBuilder strsql1 = new  StringBuilder(StringUtils.EMPTY)
		.append("INSERT INTO CT_ENUMDEFVALUEHISTORY T ")
		.append("(T.TIMEKEY,T.EVENTUSER,T.EVENTNAME,T.ENUMNAME,T.ENUMVALUE,T.DESCRIPTION,T.DEFAULTFLAG,T.DISPLAYCOLOR,T.SEQ) ")
		.append("VALUES " )
		.append("(:TIMEKEY,:EVENTUSER,:EVENTNAME,:ENUMNAME,:ENUMVALUE,:DESCRIPTION,:DEFAULTFLAG,:DISPLAYCOLOR,:SEQ) ");
		
		Map<String,Object> bindMap12 = new HashMap<String,Object>();
		bindMap12.put("TIMEKEY", ConvertUtil.getCurrTimeKey());
		bindMap12.put("EVENTUSER", super.getEventUser());
		bindMap12.put("EVENTNAME", eventName);
		bindMap12.put("ENUMNAME", sEnumname);
		bindMap12.put("ENUMVALUE",sEnumvalue);
		bindMap12.put("DESCRIPTION", sDescription);				
		bindMap12.put("DEFAULTFLAG", sDefaultFlag);
		bindMap12.put("DISPLAYCOLOR", sDisColor);
		bindMap12.put("SEQ", sSeq);					
		GenericServiceProxy.getSqlMesTemplate().update(strsql1.toString(), bindMap12);
	}
}
