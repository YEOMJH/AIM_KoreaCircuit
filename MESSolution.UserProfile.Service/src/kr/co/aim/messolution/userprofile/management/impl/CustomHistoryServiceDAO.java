package kr.co.aim.messolution.userprofile.management.impl;


import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.userprofile.info.HistoryInfo;
import kr.co.aim.messolution.userprofile.service.CustomHistoryService;
import kr.co.aim.greenframe.orm.info.DataInfo;
import kr.co.aim.greenframe.orm.info.KeyInfo;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.common.CommonUndoHistoryServiceDAO;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class CustomHistoryServiceDAO<KEY extends KeyInfo, DATA extends DataInfo> extends CommonUndoHistoryServiceDAO<KEY, DATA> implements CustomHistoryService<KEY, DATA> {

	public HistoryInfo makeHistoryInfo(String eventUser,String eventName,String eventComment)
	{
		HistoryInfo histInfo = new HistoryInfo();
		
		histInfo.setTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		histInfo.setEventName(eventName);
		histInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		histInfo.setEventUser(eventUser);
		histInfo.setEventComment(eventComment);
		
		return histInfo;
	}
	
	public boolean insertHistory(DataInfo dataInfo, HistoryInfo histInfo,Class<DATA> historyClass) throws DuplicateNameSignal, FrameworkErrorSignal, CustomException
	{
		if(dataInfo ==null || dataInfo.getKey() ==null || histInfo ==null) return false;
		
		if(!historyClass.getSimpleName().toUpperCase().endsWith("HISTORY") && !historyClass.getSimpleName().toUpperCase().endsWith("HIST"))
		return false;
		
		String endStr = historyClass.getSimpleName().replace(dataInfo.getClass().getSimpleName(), "");
		if(!StringUtil.in(endStr.toUpperCase(), "HISTORY","HIST")) return false;
		
		Object historyObject =  this.getDataInfo(dataInfo, historyClass,histInfo);
		this.insert((DATA)historyObject);
        	
		return true;
	}
	
	@Override
	public <DATA> DATA getDataInfo(DataInfo sourceData, Class<DATA> clazz, HistoryInfo histInfo) throws CustomException
	{
		Object tClass = null;

		try
		{
			tClass = Class.forName(clazz.getName()).newInstance();
			ObjectUtil.copyField(sourceData, tClass);
			KeyInfo sKey = ObjectUtil.getKeyInfo(sourceData);
			KeyInfo tKey = ObjectUtil.getKeyInfo(tClass);

			Field keyField = tKey.getClass().getDeclaredField("timeKey");
			keyField.setAccessible(true);
			keyField.set(tKey, histInfo.getTimeKey());
			
			ObjectUtil.copyField(sKey, tKey);
			ObjectUtil.setFieldValue(tClass, "key", tKey);
			
			Field [] dataField = tClass.getClass().getDeclaredFields(); 
			
			boolean isExtendedC = true;
			
			for(Field fieldInfo : dataField)
			{
				if(StringUtil.in(fieldInfo.getName(),"eventName","eventUser","eventTime","eventComment"))
				{
					fieldInfo.setAccessible(true);
					
					Field sField = histInfo.getClass().getDeclaredField(fieldInfo.getName());
					sField.setAccessible(true);
					
					ObjectUtil.copyFieldValue(fieldInfo, tClass, sField.get(histInfo));
					isExtendedC = false;
				}
			}
			
			if(isExtendedC) 
			{
				Map<String,Object> udfs = new HashMap<>();
				udfs.put("EVENTNAME",histInfo.getEventName());
				udfs.put("EVENTUSER",histInfo.getEventUser());
				udfs.put("EVENTTIME",histInfo.getEventTime());
				udfs.put("EVENTCOMMENT",histInfo.getEventComment());
				
				if (tClass instanceof UdfAccessor)
				{
					try
					{
						Method getMethod = tClass.getClass().getMethod("getUdfs");
						Map<String,Object> udfValue = (Map<String, Object>) getMethod.invoke(tClass, null);
						udfValue.putAll(udfs);
						
						Method setMethod =tClass.getClass().getMethod("setUdfs", Map.class);
						setMethod.invoke(tClass, udfValue);
					}
					catch (Exception e)
					{
					}
				}
			}
			if (tClass instanceof UdfAccessor)
			{
				try
				{
					Method getMethod = tClass.getClass().getMethod("getUdfs");
					Map<String, String> udfValue = (Map<String, String>) getMethod.invoke(tClass, null);
					udfValue.putAll(histInfo.getUdfs());

					Method setMethod = tClass.getClass().getMethod("setUdfs", Map.class);
					setMethod.invoke(tClass, udfValue);
				}
				catch (Exception e)
				{
				}
			}

		}
		catch (Exception ex)
		{
			String errorMessage = ex.getMessage();
			throw new CustomException("SYS-0010", errorMessage);
		}

		return (DATA) tClass;
	}

}
