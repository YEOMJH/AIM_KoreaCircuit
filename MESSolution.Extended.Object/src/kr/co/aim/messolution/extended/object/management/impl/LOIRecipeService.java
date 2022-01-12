package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.LOIRecipe;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LOIRecipeService extends CTORMService<LOIRecipe> {

	public static Log logger = LogFactory.getLog(LOIRecipeService.class);

	private final String historyEntity = "LOIRECIPEHIS";

	public List<LOIRecipe> select(String condition, Object[] bindSet) throws CustomException
	{
		List<LOIRecipe> result = super.select(condition, bindSet, LOIRecipe.class);

		return result;
	}

	public LOIRecipe selectByKey(boolean isLock, Object[] keySet) throws CustomException
	{
		return super.selectByKey(LOIRecipe.class, isLock, keySet);
	}

	public LOIRecipe create(EventInfo eventInfo, LOIRecipe dataInfo) throws CustomException
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, LOIRecipe dataInfo) throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public LOIRecipe modify(EventInfo eventInfo, LOIRecipe dataInfo) throws CustomException
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
