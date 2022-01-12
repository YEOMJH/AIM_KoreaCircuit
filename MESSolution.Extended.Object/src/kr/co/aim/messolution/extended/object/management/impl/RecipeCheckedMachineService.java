package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.RecipeCheckResult;
import kr.co.aim.messolution.extended.object.management.data.RecipeCheckedMachine;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;


public class RecipeCheckedMachineService extends CTORMService<RecipeCheckedMachine> {
	
	public static Log logger = LogFactory.getLog(RecipeCheckedMachine.class);
	
	public List<RecipeCheckedMachine> select(String condition, Object[] bindSet)
			throws greenFrameDBErrorSignal
	{
		List<RecipeCheckedMachine> result = super.select(condition, bindSet, RecipeCheckedMachine.class);
		
		return result;
	}
	
	public RecipeCheckedMachine selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(RecipeCheckedMachine.class, isLock, keySet);
	}
	
	public RecipeCheckedMachine create(EventInfo eventInfo, RecipeCheckedMachine dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void create(EventInfo eventInfo, List<RecipeCheckedMachine> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfoList);
	}

	public void remove(EventInfo eventInfo, RecipeCheckedMachine dataInfo)
		throws greenFrameDBErrorSignal
	{	
		super.delete(dataInfo);
	}
	
	public void remove(EventInfo eventInfo, List<RecipeCheckedMachine> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.delete(dataInfoList);
	}

	public RecipeCheckedMachine modify(EventInfo eventInfo, RecipeCheckedMachine dataInfo)
	{
		super.update(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void modify(EventInfo eventInfo, List<RecipeCheckedMachine> dataInfoList)
	{
		super.update(dataInfoList);
	}
}
