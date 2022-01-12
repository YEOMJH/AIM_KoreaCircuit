package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ChangeRecipe;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ChangeRecipeService extends CTORMService<ChangeRecipe>{
public static Log logger = LogFactory.getLog(ChangeRecipe.class);
	
	private final String historyEntity = "ChangeRecipeHistory";
	
	public List<ChangeRecipe> select(String condition, Object[] bindSet)
			throws greenFrameDBErrorSignal
		{
			List<ChangeRecipe> result = super.select(condition, bindSet, ChangeRecipe.class);
			
			return result;
		}
		
		public ChangeRecipe selectByKey(boolean isLock, Object[] keySet)
			throws greenFrameDBErrorSignal
		{
			return super.selectByKey(ChangeRecipe.class, isLock, keySet);
		}
		
		public ChangeRecipe create(EventInfo eventInfo, ChangeRecipe dataInfo)
			throws greenFrameDBErrorSignal
		{
			super.insert(dataInfo);

			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		
		public void remove(EventInfo eventInfo, ChangeRecipe dataInfo)
			throws greenFrameDBErrorSignal
		{
			super.delete(dataInfo);
		}
		
		public ChangeRecipe modify(EventInfo eventInfo, ChangeRecipe dataInfo)
		{
			super.update(dataInfo);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		} 
}

