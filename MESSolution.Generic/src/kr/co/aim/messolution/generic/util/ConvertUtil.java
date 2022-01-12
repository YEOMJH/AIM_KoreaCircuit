package kr.co.aim.messolution.generic.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.bundle.BundleUtil;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;



public class ConvertUtil {
	private static Log logger = LogFactory.getLog(ConvertUtil.class.getName());
	
	public static final String FORMAT_FULL 			= "yyyy/MM/dd_HH:mm:ss";	//"%Y/%m/%d_%T";
	public static final String FORMAT_DATE 			= "yyyy/MM/dd";				//"%Y/%m/%d";
	public static final String FORMAT_TIME 			= "HH:mm:ss";				//"%T";
	
	public static final String NONFORMAT_FULL		= "yyyyMMddHHmmss";			//"%Y%m%d%H%M%S";
	public static final String NONFORMAT_DATE 		= "yyyyMMdd";				//"%Y%m%d";
	public static final String NONFORMAT_DATE_HOUR 	= "yyyyMMddHH";				//"%Y%m%d%H";
	public static final String NONFORMAT_TIMEKEY 	= "yyyyMMddHHmmssSSSSSS";	//"%Y%m%d%H%M%S%06d";
	public static final String NONFORMAT_TIME 		= "HHmmss";					//"%H%M%S";
	public static final String NONFORMAT_MILITIME 	= "HHmmss.SSSSSS";			//"%H%M%S.%06d";
	public static final String FORMAT_MHS 			= "yyyy/MM/dd HH:mm:ss";		//"%Y/%m/%d %H:%M:%S;
	   
	public static final String PATTERN_FORMAT_FULL			= "\\d\\d\\d\\d/\\d\\d/\\d\\d_\\d\\d:\\d\\d:\\d\\d";
	public static final String PATTERN_FORMAT_DATE			= "\\d\\d\\d\\d/\\d\\d/\\d\\d";
	public static final String PATTERN_FORMAT_TIME			= "\\d\\d:\\d\\d:\\d\\d";
	public static final String PATTERN_NONFORMAT_FULL 		= "\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d";
	public static final String PATTERN_NONFORMAT_DATE		= "\\d\\d\\d\\d\\d\\d\\d\\d";
	public static final String PATTERN_NONFORMAT_DATE_HOUR 	= "\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d";
	public static final String PATTERN_NONFORMAT_TIMEKEY	= "\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d";
	public static final String PATTERN_NONFORMAT_TIME		= "\\d\\d\\d\\d\\d\\d";
	public static final String PATTERN_NONFORMAT_MILITIME	= "\\d\\d\\d\\d\\d\\d.\\d\\d\\d\\d\\d\\d";
	
	public static Map<String, Object> getMapByContainedVlue(List<Map<String, Object>> mapList,String keyName,String valueName)
	{		
		Map<String, Object> returnMap = new HashMap<String, Object>();
		if(mapList ==null || mapList.size()==0) return returnMap;
		
		for(Map<String,Object> map : mapList)
		{
			if (map ==null || map.size() ==0)  continue;
		    if(map.containsKey(keyName)&& valueName.equals(map.get(keyName)))
		    {
		    	return map;
		    }
		}
		
		return returnMap;
	}
	
	
	public static boolean containMapValue(List<Map<String,Object>> mapList,String value)
	{
		boolean isTrue = true ;
		if(mapList ==null || mapList.size()==0) return !isTrue;
		
		for(Map<String,Object> map : mapList)
		{
			if (map ==null || map.size() ==0)  continue;
		    if(map.containsValue(value))
		    {
		    	return isTrue;
		    }
		}
		
		return !isTrue;
	}
	
	public static int Object2Int(Object object)
	{
		if (String.valueOf(object).indexOf(".") > 0)
		{
			return (int)Double.parseDouble(String.valueOf(object));
		}
		else {
			return Integer.parseInt(String.valueOf(object));
		}
	}

	private static String getPattern(String timeStamp) {
		String pattern = null;
		
		if (timeStamp.matches(PATTERN_FORMAT_FULL)) {
			pattern =  FORMAT_FULL;
		} else if (timeStamp.matches(PATTERN_FORMAT_DATE)) {
			pattern =  FORMAT_DATE;
		} else if (timeStamp.matches(PATTERN_FORMAT_TIME)) {
			pattern =  FORMAT_TIME;
		} else if (timeStamp.matches(PATTERN_NONFORMAT_FULL)) {
			pattern =  NONFORMAT_FULL;
		} else if (timeStamp.matches(PATTERN_NONFORMAT_DATE)) {
			pattern =  NONFORMAT_DATE;
		} else if (timeStamp.matches(PATTERN_NONFORMAT_DATE_HOUR)) {
			pattern =  NONFORMAT_DATE_HOUR;
		} else if (timeStamp.matches(PATTERN_NONFORMAT_TIMEKEY)) {
			pattern =  NONFORMAT_TIMEKEY;
		} else if (timeStamp.matches(PATTERN_NONFORMAT_TIME)) {
			pattern =  NONFORMAT_TIME;
		} else if (timeStamp.matches(PATTERN_NONFORMAT_MILITIME)) {
			pattern =  NONFORMAT_MILITIME;
		} else {
			if (logger.isErrorEnabled())
				logger.error("Invalid Format of TimeStamp: " + timeStamp);
		}
		
		if (logger.isDebugEnabled())
			logger.debug("TimeStamp: " + timeStamp + ", Pattern: " + pattern);
		
		return pattern;
	}
	
	public static int getNextSequence(String sequenceName)
	{
		String sql = "SELECT " + sequenceName + ".NEXTVAL FROM DUAL";
		
		Object[] bind = new Object[] { sequenceName };

		String[][] result = null;
		result = GenericServiceProxy.getSqlMesTemplate().queryForStringArray(sql, bind);

		int iSequence = ConvertUtil.Object2Int(result[0][0]);
		
		return iSequence;
	}
	
	public static String toString(Timestamp sqlTimeStamp) {
		if (sqlTimeStamp == null) {
			if (logger.isErrorEnabled())
				logger.error("Input argument is null.");
			
			return "";
		}
		
		
		Calendar calendar = Calendar.getInstance();
		Date timeData = new Date(sqlTimeStamp.getTime());
		calendar.setTime(timeData);
		
		SimpleDateFormat formatter = new SimpleDateFormat(ConvertUtil.NONFORMAT_FULL);
		String timeStamp = formatter.format(calendar.getTime());
		
		if (logger.isDebugEnabled())
			logger.debug("Converted to plain string formatted TimeStamp: " + timeStamp);
		
		return timeStamp;
	}
	
	public static long toDecimal(double defaultValue, String value)
	{
		if (StringUtil.isEmpty(value))
		{
			if (logger.isDebugEnabled())
				logger.error("Input argument is null so that return default");
			
			return (long) defaultValue;
		}
		
		try
		{
			//downcasting
			return (long) Double.parseDouble(value);
		}
		catch (Exception ex)
		{
			if (logger.isDebugEnabled())
				logger.error("Converting exception from String to Double so return default");
			
			return (long) defaultValue;
		}
	}
	
	public static String getCurrTime() {
		return ConvertUtil.getCurrTime(ConvertUtil.NONFORMAT_FULL);
	}	

	public static String getCurrTime(String format) {
		return new SimpleDateFormat(format).format(new Date());
	}

	public static String getUserCurrTime(Date date, String format) {
		return new SimpleDateFormat(format).format(date);
	}

	public static String getCurrTimeKey(){
		
		kr.co.aim.greentrack.generic.util.TimeUtils timeUtils = (kr.co.aim.greentrack.generic.util.TimeUtils)
							BundleUtil.getBundleServiceClass(kr.co.aim.greentrack.generic.util.TimeUtils.class);
		
//		timeUtils.setUseSequence4TimeKey(true);	
		return timeUtils.getCurrentEventTimeKey();
	}

	public static Timestamp convertToTimeStamp(String timeKey){
		
		if(timeKey.matches(PATTERN_NONFORMAT_TIMEKEY)){
			
			kr.co.aim.greentrack.generic.util.TimeUtils timeUtils = (kr.co.aim.greentrack.generic.util.TimeUtils)
			BundleUtil.getBundleServiceClass(kr.co.aim.greentrack.generic.util.TimeUtils.class);
	
//			timeUtils.setUseSequence4TimeKey(true);
			
			return timeUtils.getTimestampByTimeKey(timeKey);
		}else{
			Date tempDate = toDate(timeKey);
			return new Timestamp(tempDate.getTime());
		}
	
	}

	public static Timestamp getCurrTimeStampSQL() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		
		return new Timestamp(calendar.getTimeInMillis());
	}
	
	public static Date toDate(Timestamp timeStamp) {
		if (timeStamp == null) {
			if (logger.isErrorEnabled())
				logger.error("Input argument is null.");
			
			return null;
		}
		Date date = new Date(timeStamp.getTime());
		return date;		
	}

	public static Date toDate(String timeStamp) {
		if (logger.isDebugEnabled())
			logger.debug("Input date string: " + timeStamp);
		
		String pattern = getPattern(timeStamp);
		if (pattern == null) {
			if (logger.isErrorEnabled())
				logger.error("Unknown TimeStamp Format: " + timeStamp);
			
			return null;
		}
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		
		try {
			return formatter.parse(timeStamp);
		} catch (ParseException e) {
			if (logger.isFatalEnabled())
				logger.fatal("Failed to parse TimeStamp: " + timeStamp, e);
			
			return null;
		}
	}

	public static String toStringForIntTypeValue(String intTypeValue)
	{
		if (StringUtils.isNotEmpty(intTypeValue))
		{
			int number = 0;
			try
			{
				number = Integer.parseInt(intTypeValue);
			}
			catch (Exception e)
			{
			}

			if (number > 0)
			{
				intTypeValue = String.valueOf(number);
			}
		}

		return intTypeValue;
	}

	public static Double getDiffTime(String fromTime, String toTime) {
		Date fromDate = toDate(fromTime);
		Date toDate = toDate(toTime);
		
		if (fromDate == null || toDate == null)
			return -1.0;
		return ( toDate.getTime() - fromDate.getTime()) / 1000.0;
	}

	public static String getMapValueByName(Map<String, Object> map, String name)
	{
		String value = "";

		if (map != null && map.size() > 0 && map.containsKey(name) && map.get(name) != null)
		{
			value = String.valueOf(map.get(name));
		}

		return value;
	}
}
