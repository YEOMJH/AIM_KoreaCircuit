package kr.co.aim.messolution.extended.object.management.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.EnumDef;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;


public class EnumDefService extends CTORMService<EnumDef> {
	
	public static Log logger = LogFactory.getLog(EnumDef.class);
	
	/*public List<EnumDef> select(String condition, Object[] bindSet)
			throws greenFrameDBErrorSignal
	{
		List<EnumDef> result = super.select(condition, bindSet, EnumDef.class);
		
		return result;
	}
	*/

	
	public boolean selectByKey(String enumName) throws greenFrameDBErrorSignal {
		String sql = " SELECT ENUMNAME,DESCRIPTION,ACCESSTYPE,USAGE,CONSTANTFLAG FROM ENUMDEF WHERE ENUMNAME=:ENUMNAME ";

		Map<String, String> args = new HashMap<String, String>();
		args.put("ENUMNAME", enumName);


		List<Object> result = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().query(sql, null, args);

		if (result.size() > 0) {
			return true;
		}
		return false;

	}
	
	public void create(EnumDef dataInfo) throws greenFrameDBErrorSignal {
		String sql = " INSERT INTO ENUMDEF(ENUMNAME ,DESCRIPTION ,ACCESSTYPE,USAGE,CONSTANTFLAG)"
				+ "VALUES (:ENUMNAME ,:DESCRIPTION ,:ACCESSTYPE,:USAGE,:CONSTANTFLAG) ";

		Map<String, String> args = new HashMap<String, String>();
		args.put("ENUMNAME", dataInfo.getEnumName());
		args.put("DESCRIPTION", dataInfo.getDescription());
		args.put("ACCESSTYPE", dataInfo.getAccessType());
		args.put("USAGE", dataInfo.getUsage());
		args.put("CONSTANTFLAG", dataInfo.getConstantFlag());

		GenericServiceProxy.getSqlMesTemplate().update(sql, args);
	}

/*	public void create(List<EnumDef> dataInfoList)
			throws greenFrameDBErrorSignal
	{
		
			
	}*/
	
	public void remove(String enumName) throws greenFrameDBErrorSignal {

		String sql = "DELETE FROM ENUMDEF WHERE ENUMNAME=:ENUMNAME ";

		Map<String, String> args = new HashMap<String, String>();
		args.put("ENUMNAME", enumName);

		GenericServiceProxy.getSqlMesTemplate().update(sql, args);
	}

	public void modify(String enumName,String description )
	{

		String sql = " UPDATE ENUMDEF SET DESCRIPTION = :DESCRIPTION   WHERE 1=1 AND ENUMNAME =:ENUMNAME ";
		
		Map<String, String> args = new HashMap<String, String>();
		args.put("ENUMNAME", enumName);	
		args.put("DESCRIPTION", description);
		
		GenericServiceProxy.getSqlMesTemplate().update(sql, args);	
	}
	
	/*public void modify(List<EnumDef> dataInfoList)
	{
		
	}*/
	
	public boolean isExistEnumName(String EnumName)
	{
		boolean isExist = false;

		String sql = "SELECT ENUMNAME FROM ENUMDEF ED WHERE ED.ENUMNAME = :ENUMNAME ";
		Map<String, String> args = new HashMap<String, String>();
		args.put("ENUMNAME", EnumName);

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

		if (result.size() > 0)
		{
			isExist = true;
		}

		return isExist;
	}


	

}
