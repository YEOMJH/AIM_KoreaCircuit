package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMServiceNoCT;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.POSSample;
import kr.co.aim.messolution.extended.object.management.data.STKConfig;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class POSSampleService extends CTORMServiceNoCT<POSSample>
{

	public static Log logger = LogFactory.getLog(POSSampleService.class);
	
	private final String historyEntity = "POSSAMPLEHISTORY";
	
	public POSSample selectByKey(boolean isLock, Object keySet)
	throws greenFrameDBErrorSignal
	{
		return super.selectByKey(POSSample.class, isLock, keySet);
	}	
	
	public List<POSSample> select(String condition, Object[] bindSet)
	throws greenFrameDBErrorSignal
	{
		List<POSSample> result = super.select(condition, bindSet, POSSample.class);
		
		return result;
	}
	
	public void modify(POSSample dataInfo)
	throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		
	}
	
	public void create(POSSample dataInfo)
	throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
	}
	
	public POSSample create(EventInfo eventInfo, POSSample dataInfo)
			throws CustomException
	{
		super.insert(dataInfo);
			
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
		return dataInfo;
	}
	
	public void remove(EventInfo eventInfo, POSSample dataInfo)
			throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
		super.delete(dataInfo);
	}
		
	
	public POSSample modify(EventInfo eventInfo, POSSample dataInfo)
			throws CustomException
	{
		super.update(dataInfo);
			
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
