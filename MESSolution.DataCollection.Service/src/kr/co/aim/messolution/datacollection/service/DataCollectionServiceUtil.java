package kr.co.aim.messolution.datacollection.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.datacollection.management.data.DCData;
import kr.co.aim.greentrack.datacollection.management.sql.SqlStatement;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.product.management.data.Product;

import org.jdom.Element;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class DataCollectionServiceUtil implements ApplicationContextAware
{
	private ApplicationContext		applicationContext;
	private static Log				log = LogFactory.getLog(DataCollectionServiceUtil.class);
	
	public void setApplicationContext( ApplicationContext arg0 ) throws BeansException
	{
		applicationContext = arg0;
	}

	private String getItemName(String dcSpecName, String siteName)
	{
		String itemName = "";
		
		String args = "%"+siteName+"%";
		
		String sql = "SELECT ITEMNAME FROM DCSPECITEM " +
				     " WHERE DCSPECNAME = ? " +
				     "   AND SITENAMES LIKE '"+args+"'";
		
		String[] bindSet = new String[]{ dcSpecName };
		
		List resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindSet);
		
		if(resultList.size() > 0)
		{
			ListOrderedMap dcSpecList = (ListOrderedMap) resultList.get(0);
			itemName = dcSpecList.get("ITEMNAME").toString();	
		}
		
		return itemName;
	}
	
	private String convertSiteValue(String siteValue)
	{
		double preNumber = 0;
		double exponential = 0;
		double cValue = 0;
		
		if(siteValue.contains("E+")){
			preNumber = Double.valueOf(siteValue.substring(0, siteValue.indexOf("E+"))).doubleValue();
			exponential = Double.valueOf(siteValue.substring(siteValue.indexOf("E+")+1, siteValue.length())).doubleValue();
		
			cValue = preNumber * Math.pow(10, exponential);
			System.out.println(preNumber);
			System.out.println(exponential);
			System.out.println(cValue);
			
		}else if(siteValue.contains("E-")){
			preNumber = Double.valueOf(siteValue.substring(0, siteValue.indexOf("E-"))).doubleValue();
			exponential = Double.valueOf(siteValue.substring(siteValue.indexOf("E-")+1, siteValue.length())).doubleValue();
			
			cValue = preNumber * Math.pow(10, exponential);
			System.out.println(preNumber);
			System.out.println(exponential);
			System.out.println(cValue);
		}
		
		return String.valueOf(cValue).toString();
	}
	
	public static List<Map<String, Object>> getLotData (String lotName)
	{
		String sql = "SELECT LOTNAME, FACTORYNAME, PRODUCTSPECNAME, PRODUCTSPECVERSION, PROCESSFLOWNAME, PROCESSFLOWVERSION, PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION, LOTPROCESSSTATE"
				   + " FROM LOT WHERE 1=1 AND LOTNAME = :lotName ";
		
		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("lotName", lotName);
		List<Map<String, Object>> lotData = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return lotData;
	}
	
	public static List<Map<String, Object>> getProductData (String productName)
	{
		String sql = "SELECT PRODUCTNAME, LOTNAME, FACTORYNAME, PRODUCTSPECNAME, PRODUCTSPECVERSION, PROCESSFLOWNAME, PROCESSFLOWVERSION, PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION, PRODUCTPROCESSSTATE "
				   + " FROM PRODUCT WHERE 1=1 AND PRODUCTNAME = :productName ";
		
		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("productName", productName);
		List<Map<String, Object>> productData = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return productData;
	}
	
	public static String getMachineFactory (String machineName)
	{
		String sql = "SELECT FACTORYNAME FROM MACHINE WHERE 1=1 AND MACHINENAME = :machineName";
		
		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("machineName", machineName);
		List<Map<String, Object>> machineData = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return (String)machineData.get(0).get("FACTORYNAME"); 
	}
	
	public static String getNextDCDataId() 
	{
		List<ListOrderedMap> result = GenericServiceProxy.getDcolQueryTemplate().queryForList(SqlStatement.SELECT_NEXT_DCDATAID, new Object[] {});

		return result.get(0).getValue(0).toString();
	}
	
	public static String getCurrDcDataId()
	{
		String sql = "SELECT DCDATAID.CURRVAL FROM DUAL";
		
		List<ListOrderedMap> result = GenericServiceProxy.getDcolQueryTemplate().queryForList(sql, new Object[] {});
		
		return result.get(0).getValue(0).toString();
	}
	
	public List<Map<String, Object>>  getDCSiteName() throws CustomException
	{
		String sql = "SELECT SITENAME,DESCRIPTION,XMIN,XMAX,YMIN,YMAX FROM DCSITE ORDER BY SITENAME";
		
		Object[] bindSet = new Object[] {};
		
		List<Map<String, Object>> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindSet);
		
		return sqlResult;
	}
	
	public List<Map<String, Object>>  getDCSpecItemName(String productSpecName) throws CustomException
	{
		String sql = "SELECT ITEMNAME,TARGET,LOWERSPECLIMIT,UPPERSPECLIMIT FROM DCSPECITEM WHERE DCSPECNAME =? ORDER BY ITEMNAME";
		
		Object[] bindSet = new Object[] {productSpecName};
		
		List<Map<String, Object>> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindSet);
		
		return sqlResult;
	}
	
	public List<Map<String, Object>>  getDCSpecItemNameByColor(String productSpecName, String color) throws CustomException
	{
		String sql = "SELECT ITEMNAME,TARGET, LOWERSPECLIMIT, UPPERSPECLIMIT FROM DCSPECITEM " +
				     "WHERE DCSPECNAME = :DCSPECNAME AND ITEMNAME LIKE :ITEMNAME ORDER BY ITEMNAME";
		
		Map<String,Object> bindMap = new HashMap<String,Object>();
		bindMap.put("DCSPECNAME", productSpecName);
		bindMap.put("ITEMNAME", color + "%");
		
		List<Map<String, Object>> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return sqlResult;
	}
	
	//This function is useless, Because X,Y limit is unnecessary now.
	public List<ListOrderedMap>getXYLimit(String X,String Y,String siteName,String XMIN,String XMAX,String YMIN,String YMAX) throws CustomException
	{
		String sql = "SELECT CASE WHEN :X > :XMIN and :X < :XMAX  and :Y >:YMIN and :Y < :YMAX THEN"
				+ " DECODE(SITENAME,'La-01',SITENAME,'Lb-01',SITENAME,'Lb-02',SITENAME,'Lc-01',SITENAME,'Lc-02',SITENAME,'Ld-01',SITENAME,'Ld-02',SITENAME,0)"
				+ " WHEN :X <= :XMIN and :Y < :YMIN  THEN  DECODE(SITENAME,'Le-01',SITENAME,0)"
				+ " WHEN :X >= :XMAX and :Y >= :YMAX THEN  DECODE(SITENAME,'Le-02',SITENAME,0)"
				+ " ELSE 'NotExist' END RESULT"
				+ " FROM DCSITE WHERE SITENAME =:SITENAME";
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		
		bindMap.put("X", X);
		bindMap.put("Y", Y);
		bindMap.put("XMIN", XMIN);
		bindMap.put("XMAX", XMAX);
		bindMap.put("YMIN", YMIN);
		bindMap.put("YMAX", YMAX);
		bindMap.put("SITENAME", siteName);
		
		List<ListOrderedMap>  sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return sqlResult;
	}
	
	public List<ListOrderedMap> getDCSpecGrade(String L,String LOWERSPECLIMIT,String UPPERSPECLIMIT, String dcItemName, String productSpecName) throws CustomException
	{
		//start modify SQL for add Grade S ,delete Grade Y
		String sql = "SELECT "
				+ "CASE "
				+ "	WHEN TO_NUMBER(:L) > TO_NUMBER(:LOWERSPECLIMIT) and TO_NUMBER(:L) <= TO_NUMBER(:UPPERSPECLIMIT) "
				+ " 	 THEN DECODE(TARGET,  "
				+ " 	'A1',TARGET,  "
				+ " 	'B1',TARGET,  "
				+ "		'B2',TARGET,  "
				+ "		'C1',TARGET,0)"
				+ "	WHEN TO_NUMBER(:L) <= TO_NUMBER(:LOWERSPECLIMIT) "
				+ "		THEN DECODE(TARGET,'S',TARGET,0)   "
				+ "	WHEN TO_NUMBER(:L) >  TO_NUMBER(:UPPERSPECLIMIT) "
				+ "		THEN DECODE(TARGET,'C2',TARGET,0)   "
				+ " 	ELSE 'NotExist' END RESULT "
				+ " FROM DCSPECITEM WHERE ITEMNAME =:ITEMNAME"
				+ "	AND DCSPECNAME =:DCSPECNAME ";
		//end modify SQL for add Grade S ,delete Grade Y
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		
		bindMap.put("L", L);
		bindMap.put("LOWERSPECLIMIT", LOWERSPECLIMIT);
		bindMap.put("UPPERSPECLIMIT", UPPERSPECLIMIT);
		bindMap.put("ITEMNAME", dcItemName);
		bindMap.put("DCSPECNAME", productSpecName);
		
		List<ListOrderedMap>  sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return sqlResult;
	}
	public static List<Map<String, Object>> getLotProcessOperation(String lotName) throws CustomException
	{
		String sql = "SELECT * FROM (SELECT a.PROCESSOPERATIONNAME, MACHINENAME " +
				"FROM LOTHISTORY a,PROCESSOPERATIONSPEC b " +
				"WHERE LOTNAME = :lotName " +
				"AND EVENTNAME = 'TrackIn' " +
				"AND a.FACTORYNAME=b.FACTORYNAME " +
				"AND a.PROCESSOPERATIONNAME=b.PROCESSOPERATIONNAME " +
				"AND b.PROCESSOPERATIONTYPE ='Production' " +
				"ORDER BY TIMEKEY DESC) WHERE ROWNUM=1 ";

		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("lotName", lotName);
		List<Map<String, Object>> lotProcessOperation = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return lotProcessOperation;
		
	}
	
	public static String getNextAlarmId() 
	{
		List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList("SELECT ALARMID.NEXTVAL FROM DUAL", new Object[] {});

		return result.get(0).getValue(0).toString();
	}
	
	public static void insertAlarm(String lastEventName,
									String lotName, 
									String machineName, 
									String reasonCode, 
									String porcessOpertation, 
									String comment ,
									String factoryName,
									String processFlowName,
									String productSpecName) 
	{
		
		String timeKey = ConvertUtil.getCurrTimeKey();
		try 
		{
			String sql = "INSERT INTO ALARM( 		" +
							  "ALARMID,             " +
			                 "ALARMTIMEKEY,         " +
			                 "ALARMTYPE,            " +
			                 "ALARMLEVEL,           " +
			                 "DESCRIPTION,          " +
			                 "LASTEVENTNAME,        " +
			                 "LASTEVENTTIMEKEY,     " +
			                 "LASTEVENTTIME,  		" +
			                 "LASTEVENTUSER,       	" +
			                 "LASTEVENTCOMMENT,     " +
			                 "ALARMSTATE,          	" +
			                 "LOTNAME,             	" +
			                 "MACHINENAME,          " +
			                 "REASONCODE,           " +
			                 "PROCESSOPERATIONNAME, " +
			                 "FACTORYNAME,         	" +
			                 "PROCESSFLOWNAME, 		" +
			                 "PRODUCTSPECNAME      	" +
						" ) 						" +
						" VALUES(					" +
						    ":ALARMID,              " +
							":ALARMTIMEKEY,         " +
							":ALARMTYPE,            " +
							":ALARMLEVEL,           " +
							":DESCRIPTION,          " +
							":LASTEVENTNAME,        " +
							":LASTEVENTTIMEKEY,     " +
							"sysdate,     			" +
							":LASTEVENTUSER,  		" +
							":LASTEVENTCOMMENT,     " +
							":ALARMSTATE,           " +
							":LOTNAME,             	" +
							":MACHINENAME,          " +
							":REASONCODE,           " +
							":PROCESSOPERATIONNAME, " +
							":FACTORYNAME,          " +
							":PROCESSFLOWNAME, 		" +
							":PRODUCTSPECNAME       " +
						")";					
			
			// 02. Set bindMap and performs the query.
			Map<String, String> bindMap = new HashMap<String, String>();
			
			bindMap.put("ALARMID",getNextAlarmId());
			bindMap.put("ALARMTIMEKEY",timeKey);   
			bindMap.put("ALARMTYPE","SPC");   
			bindMap.put("ALARMLEVEL","Serious");   
			bindMap.put("DESCRIPTION","RuleOut");    
			bindMap.put("LASTEVENTNAME",lastEventName); 
			bindMap.put("LASTEVENTTIMEKEY",timeKey);   
			bindMap.put("LASTEVENTUSER","MES");   
			bindMap.put("LASTEVENTCOMMENT", comment);   
			bindMap.put("ALARMSTATE","Issue");   
			bindMap.put("LOTNAME", lotName);   
			bindMap.put("MACHINENAME", machineName);   
			bindMap.put("REASONCODE", reasonCode);   
			bindMap.put("PROCESSOPERATIONNAME", porcessOpertation);
			bindMap.put("FACTORYNAME", factoryName);
			bindMap.put("PROCESSFLOWNAME", processFlowName);
			bindMap.put("PRODUCTSPECNAME", productSpecName);
			
			GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		}
		catch (Exception e)
		{
			log.info("InsertAlarm Table Fail");
		}
	}
	
	public static boolean insertPolicy(String factoryName, String productSpecName, String processOperationName, String machineName, String dcSpecName) 
	{
		try 
		{
			String sql = "INSERT INTO TPOMPOLICY( " +
							  "FACTORYNAME,               " +
			                 "PRODUCTSPECNAME,           " +
			                 "PRODUCTSPECVERSION,              " +
			                 "PROCESSOPERATIONNAME,           " +
			                 "PROCESSOPERATIONVERSION,               " +
			                 "MACHINENAME,           " +
			                 "CONDITIONID,     " +
			                 "DCSPECNAME,  " +
			                 "DCSPECVERSION       " +
						" ) " +
						" VALUES(" +
						    ":FACTORYNAME,               " +
							":PRODUCTSPECNAME,           " +
							":PRODUCTSPECVERSION,              " +
							":PROCESSOPERATIONNAME,           " +
							":PROCESSOPERATIONVERSION,               " +
							":MACHINENAME,           " +
							":CONDITIONID,           " +
							":DCSPECNAME,     " +
							":DCSPECVERSION  " +
						")";					
			
			Map<String, String> bindMap = new HashMap<String, String>();
			
			bindMap.put("FACTORYNAME", factoryName);
			bindMap.put("PRODUCTSPECNAME", productSpecName);
			bindMap.put("PRODUCTSPECVERSION", "00001");
			bindMap.put("PROCESSOPERATIONNAME", processOperationName);
			bindMap.put("PROCESSOPERATIONVERSION", "00001");
			bindMap.put("MACHINENAME", machineName);
			bindMap.put("CONDITIONID", factoryName+productSpecName+processOperationName+machineName);
			bindMap.put("DCSPECNAME", dcSpecName);
			bindMap.put("DCSPECVERSION", "00001");
	
			GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
			log.info("InsertSPCPolicy Table Success");
			return true;
		}
		catch (Exception e)
		{
			log.info("InsertSPCPolicy Table Fail");
			return false;
		}
	}
	
	public static List<Map<String, Object>> getProcessOperationSpecData (Product productdata, String processOperationName, String unitName)
	{
		String sql = "SELECT FACTORYNAME,PROCESSOPERATIONNAME,CHECKSTATE,PROCESSOPERATIONTYPE FROM PROCESSOPERATIONSPEC " +
					" WHERE CHECKSTATE = 'CheckedIn' AND PROCESSOPERATIONTYPE= 'Production' " +
					" AND FACTORYNAME=:FACTORYNAME AND PROCESSOPERATIONNAME=:PROCESSOPERATIONNAME";

		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", productdata.getFactoryName());
		
		String exp = "EXP";
		int index=unitName.indexOf(exp);
		if(index != -1)
		{
			bindMap.put("PROCESSOPERATIONNAME", productdata.getProcessOperationName());
		}
		else
		{
			bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		}

		List<Map<String, Object>> processOperationSpecData = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return processOperationSpecData;
	}
	
	public static String getNextSequenceId() 
	{
		String sql = "SELECT ALARMID.NEXTVAL FROM DUAL";
		
		List<ListOrderedMap> result = GenericServiceProxy.getDcolQueryTemplate().queryForList(sql, new Object[] {});

		return result.get(0).getValue(0).toString();
	}
	
}
