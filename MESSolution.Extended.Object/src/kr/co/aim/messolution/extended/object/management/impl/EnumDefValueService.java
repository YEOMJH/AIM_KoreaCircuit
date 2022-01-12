package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.EnumDefValue;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;


public class EnumDefValueService extends CTORMService<EnumDefValue> {
	
	public static Log logger = LogFactory.getLog(EnumDefValue.class);
	
	public void select(EnumDefValue dataInfo)
			throws greenFrameDBErrorSignal
	{
		 String sql = " SELECT ENUMNAME,ENUMVALUE,DESCRIPTION,DEFAULTFLAG,SEQ FROM ENUMDEFVALUE WHERE ENUMNAME=:ENUMNAME OR ENUMVALUE =:ENUMVALUE OR DESCRIPTION =:DESCRIPTION ";
			
			
		 Map<String, String> args = new HashMap<String, String>();			
		 args.put("ENUMNAME", dataInfo.getEnumName());
		 args.put("ENUMVALUE", dataInfo.getEnumValue());			
		 args.put("DESCRIPTION", dataInfo.getDescription());			
		 args.put("DEFAULTFLAG", dataInfo.getDefaultFlag());			
		 args.put("SEQ", dataInfo.getSeq());				
				
		 List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);						

	}
	
	public List<EnumDefValue> select(String condition, Object[] bindSet)
			throws CustomException
		{
			String sql = " SELECT ENUMNAME,ENUMVALUE,DESCRIPTION,DEFAULTFLAG,DISPLAYCOLOR,SEQ FROM ENUMDEFVALUE WHERE 1=1 AND " + condition;
			
			List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindSet);
			
			if (result.size() == 0)
				return null;
			List<EnumDefValue> result1 = this.transform(result);
			
			return result1;
		}
	

	public  boolean selectByKey(String enumName,String enumValue)
		throws greenFrameDBErrorSignal
	{
		 String sql = " SELECT ENUMNAME,ENUMVALUE,DESCRIPTION,DEFAULTFLAG,SEQ FROM ENUMDEFVALUE WHERE ENUMNAME=:ENUMNAME AND ENUMVALUE =:ENUMVALUE ";
								
		 Map<String, String> args = new HashMap<String, String>();			
		 args.put("ENUMNAME", enumName);				
		 args.put("ENUMVALUE", enumValue);				
						  
		 List<Object>  result = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().query(sql, null, args);	
			 
		 if (result.size() > 0)
		 {				
			 return true;
		 }
		 return false;
					
	}
	
	public void create(EnumDefValue dataInfo)
		throws greenFrameDBErrorSignal
	{
	
       String sql = " INSERT INTO ENUMDEFVALUE (ENUMNAME,ENUMVALUE,DESCRIPTION,DEFAULTFLAG,SEQ) "
       		+ "VALUES(:ENUMNAME,:ENUMVALUE,:DESCRIPTION,:DEFAULTFLAG,:SEQ) ";
		
		Map<String, String> args = new HashMap<String, String>();
		args.put("ENUMNAME", dataInfo.getEnumName());
		args.put("ENUMVALUE", dataInfo.getEnumValue());
		args.put("DESCRIPTION", dataInfo.getDescription());
		args.put("DEFAULTFLAG", dataInfo.getDefaultFlag());
		args.put("SEQ", dataInfo.getSeq());		
		
		GenericServiceProxy.getSqlMesTemplate().update(sql, args);	
	}
	
	/*public void create( List<EnumDefValue> dataInfoList)
			throws greenFrameDBErrorSignal
	{
		
			
	}*/
	
	public void modifyDescription(EnumDefValue dataInfo)
	{

		String sql = " UPDATE ENUMDEFVALUE SET DESCRIPTION =:DESCRIPTION WHERE 1=1 AND ENUMNAME =:ENUMNAME AND ENUMVALUE = :ENUMVALUE  ";
		
		Map<String, String> args = new HashMap<String, String>();
		args.put("ENUMNAME", dataInfo.getEnumName());
		args.put("ENUMVALUE", dataInfo.getEnumValue());
		args.put("DESCRIPTION", dataInfo.getDescription());
		
		GenericServiceProxy.getSqlMesTemplate().update(sql, args);					
	}
	
	public void create(String sEnumname,String sEnumvalue,String sDescription,String sDefaultFlag,String sDisColor,String sSeq,EventInfo eventInfo) throws CustomException 
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
		addEnumDefValueHistory( sEnumname, sEnumvalue, sDescription, sDefaultFlag, sDisColor , sSeq , eventInfo.getEventName(), eventInfo);
	}
	
	public void addEnumDefValueHistory(String sEnumname,String sEnumvalue,String sDescription,String sDefaultFlag,String sDisColor ,String sSeq ,String eventName, EventInfo eventInfo)
	{
		StringBuilder strsql1 = new  StringBuilder(StringUtils.EMPTY)
		.append("INSERT INTO CT_ENUMDEFVALUEHISTORY T ")
		.append("(T.TIMEKEY,T.EVENTUSER,T.EVENTNAME,T.ENUMNAME,T.ENUMVALUE,T.DESCRIPTION,T.DEFAULTFLAG,T.DISPLAYCOLOR,T.SEQ) ")
		.append("VALUES " )
		.append("(:TIMEKEY,:EVENTUSER,:EVENTNAME,:ENUMNAME,:ENUMVALUE,:DESCRIPTION,:DEFAULTFLAG,:DISPLAYCOLOR,:SEQ) ");
		
		Map<String,Object> bindMap12 = new HashMap<String,Object>();
		bindMap12.put("TIMEKEY", ConvertUtil.getCurrTimeKey());
		bindMap12.put("EVENTUSER", eventInfo.getEventUser());
		bindMap12.put("EVENTNAME", eventName);
		bindMap12.put("ENUMNAME", sEnumname);
		bindMap12.put("ENUMVALUE",sEnumvalue);
		bindMap12.put("DESCRIPTION", sDescription);				
		bindMap12.put("DEFAULTFLAG", sDefaultFlag);
		bindMap12.put("DISPLAYCOLOR", sDisColor);
		bindMap12.put("SEQ", sSeq);					
		GenericServiceProxy.getSqlMesTemplate().update(strsql1.toString(), bindMap12);
	}
	
	public void modify(EnumDefValue oldData,EnumDefValue newData,EventInfo eventInfo)
	{

		String sql = " UPDATE ENUMDEFVALUE SET ENUMVALUE =:NEWENUMVALUE,DESCRIPTION =:DESCRIPTION,DEFAULTFLAG =:DEFAULTFLAG,DISPLAYCOLOR =:DISPLAYCOLOR,SEQ =:SEQ WHERE 1=1 AND ENUMNAME =:ENUMNAME AND ENUMVALUE = :ENUMVALUE  ";
		
		Map<String, String> args = new HashMap<String, String>();
		args.put("ENUMNAME", oldData.getEnumName());
		args.put("ENUMVALUE", oldData.getEnumValue());
		
		args.put("NEWENUMVALUE", newData.getEnumValue());
		args.put("DESCRIPTION", newData.getDescription());
		args.put("DEFAULTFLAG", newData.getDefaultFlag());
		args.put("DISPLAYCOLOR", newData.getDisplayColor());
		args.put("SEQ", newData.getSeq());
		
		GenericServiceProxy.getSqlMesTemplate().update(sql, args);		
		
		addEnumDefValueHistory( oldData.getEnumName(), newData.getEnumValue(), newData.getDescription(), newData.getDefaultFlag(), newData.getDisplayColor() , newData.getSeq() , eventInfo.getEventName(), eventInfo);
	}
	

	public void remove(String EnumName,String EnumValue)
	{
		String sql = "DELETE FROM ENUMDEFVALUE WHERE ENUMNAME=:ENUMNAME AND ENUMVALUE =:ENUMVALUE";
		
		Map<String, String> args = new HashMap<String, String>();
		args.put("ENUMNAME", EnumName);
		args.put("ENUMVALUE", EnumValue);	
		
		GenericServiceProxy.getSqlMesTemplate().update(sql, args);					
	}
	
	public void removeEnumValuelist(String EnumName)
	{
		String sql = "DELETE FROM ENUMDEFVALUE WHERE ENUMNAME=:ENUMNAME";
		
		Map<String, String> args = new HashMap<String, String>();
		args.put("ENUMNAME", EnumName);	
		
		GenericServiceProxy.getSqlMesTemplate().update(sql, args);					
	}
	/*public void modify( List<EnumDefValue> dataInfoList)
	{
		
	}*/
	
	public String getEnumDefValueSeq(String enumName)
	{
		String sql = " SELECT NVL(MAX(SEQ) , 0) + 1 AS SEQ FROM ENUMDEFVALUE WHERE ENUMNAME = :ENUMNAME  ";
		String seq = "";
		Map<String, String> args = new HashMap<String, String>();
		args.put("ENUMNAME", enumName);
		
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);
		
		if (result.size() > 0)
		{
			seq = ConvertUtil.getMapValueByName(result.get(0), "SEQ");
		}
		
		return seq;
	}
	
	public String getEnumDefValueOldSeq(String enumName,String enumValue)
	{
		String sql = " SELECT SEQ FROM ENUMDEFVALUE WHERE ENUMNAME = :ENUMNAME AND ENUMVALUE = :ENUMVALUE ";
		String seq = "";
		Map<String, String> args = new HashMap<String, String>();
		args.put("ENUMNAME", enumName);
		args.put("ENUMVALUE", enumValue);
		
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);
		
		if (result.size() > 0)
		{
			seq = ConvertUtil.getMapValueByName(result.get(0), "SEQ");
		}
		
		return seq;
	}
	
	public boolean isExistEnumNameInfo(String EnumName,String EnumValue)
	{
		boolean isExist = false;

		String sql = "SELECT ENUMNAME,ENUMVALUE FROM ENUMDEFVALUE  WHERE ENUMNAME = :ENUMNAME AND ENUMVALUE =:ENUMVALUE ";
		Map<String, String> args = new HashMap<String, String>();
		args.put("ENUMNAME", EnumName);
		args.put("ENUMVALUE",EnumValue);

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

		if (result.size() > 0)
		{
			isExist = true;
		}

		return isExist;
	}
	
	public boolean isExistEnumNameInfoByEnumValue(String EnumValue)
	{
		boolean isExist = false;

		String sql = "SELECT ENUMNAME,ENUMVALUE FROM ENUMDEFVALUE  WHERE ENUMNAME = :ENUMNAME  ";
		Map<String, String> args = new HashMap<String, String>();
		args.put("ENUMNAME", EnumValue);

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

		if (result.size() > 0)
		{
			isExist = true;
		}

		return isExist;
	}
	
	public List<EnumDefValue> transform (List resultList)
	{
		if (resultList==null || resultList.size() == 0)
		{
			return null;
		}
		
		Object result = super.ormExecute( CTORMUtil.createDataInfo(EnumDefValue.class), resultList);

		if ((result instanceof List))
		{
			return (List) result;
		}

		List<EnumDefValue> resultSet = new ArrayList();
		resultSet.add((EnumDefValue) result);
		return resultSet;
	}
	
	public void modifyEnumSeq(String enumName,String enumValue,String seq)
	{

		String sql = " UPDATE ENUMDEFVALUE SET SEQ =:SEQ WHERE 1=1 AND ENUMNAME =:ENUMNAME AND ENUMVALUE = :ENUMVALUE  ";
		
		Map<String, String> args = new HashMap<String, String>();
		args.put("ENUMNAME", enumName);
		args.put("ENUMVALUE", enumValue);
		args.put("SEQ", seq);
		
		GenericServiceProxy.getSqlMesTemplate().update(sql, args);					
	}
}
