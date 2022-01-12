package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.SampleMaskCount;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

public class SampleMaskCountService extends CTORMService<SampleMaskCount> {
	
	public static Log logger = LogFactory.getLog(SampleMaskCount.class);
	
	private final String historyEntity = "MaskLotHistory";
	
	public List<SampleMaskCount> select(String condition, Object[] bindSet)
			throws greenFrameDBErrorSignal
	{
		List<SampleMaskCount> result = super.select(condition, bindSet, SampleMaskCount.class);
		
		return result;
	}
	
	public SampleMaskCount selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(SampleMaskCount.class, isLock, keySet);
	}
	
	public SampleMaskCount create(SampleMaskCount dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void create(List<SampleMaskCount> dataInfoList)
			throws greenFrameDBErrorSignal
	{				
		super.insert(dataInfoList);
	}
	
	public void remove(SampleMaskCount dataInfo)
		throws greenFrameDBErrorSignal
	{		
		super.delete(dataInfo);
	}

	public SampleMaskCount modify(SampleMaskCount dataInfo)
	{
		super.update(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void modify(List<SampleMaskCount> dataInfoList)
	{
		super.update(dataInfoList);
	}
}
