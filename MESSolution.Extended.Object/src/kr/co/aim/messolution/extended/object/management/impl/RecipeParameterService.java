package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.RecipeParameter;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class RecipeParameterService extends CTORMService<RecipeParameter> {
	
	public static Log logger = LogFactory.getLog(RecipeParameterService.class);
	
	private final String historyEntity = "RecipeParameterHistory";
	
	public List<RecipeParameter> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<RecipeParameter> result = super.select(condition, bindSet, RecipeParameter.class);
		
		return result;
	}
	
	public RecipeParameter selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(RecipeParameter.class, isLock, keySet);
	}
	
	public RecipeParameter create(EventInfo eventInfo, RecipeParameter dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void create(EventInfo eventInfo, List<RecipeParameter> dataInfoList)
			throws greenFrameDBErrorSignal
	{		
		super.insert(dataInfoList);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}
	
	public void remove(EventInfo eventInfo, RecipeParameter dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public void remove(EventInfo eventInfo, List<RecipeParameter> dataInfoList)
	{	
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
		//2021-03-31 ghhan Change Batch
		//super.delete(dataInfoList);
		super.deleteBatch(dataInfoList);
	}
	
	public RecipeParameter modify(EventInfo eventInfo, RecipeParameter dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void modify(EventInfo eventInfo, List<RecipeParameter> dataInfoList)
	{	
		super.update(dataInfoList);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}
	
	public RecipeParameter getRecipeParameterData(String machineName, String recipeName, String parameterName, String value) throws CustomException
	{
		if(logger.isInfoEnabled())
		logger.info(String.format(" Input arguments: MachineName [%s] , RecipeName [%s], ParameterName [%s], Value [%s].",machineName,recipeName,parameterName,value));
		
		RecipeParameter dataInfo = null;
		
		try
		{
			dataInfo = super.selectByKey(RecipeParameter.class, false, new Object[] { machineName, recipeName ,parameterName,value});
		}
		catch (greenFrameDBErrorSignal greenEx)
		{
            if(greenEx.getErrorCode().equals(ErrorSignal.NotFoundSignal))
            	throw new CustomException("COMM-1000", "CT_RecipeParameter",String.format("MachineName = %s , RecipeName = %s, ParameterName [%s], Value [%s] ",machineName,recipeName,parameterName,value));
            else 
            	throw new CustomException(greenEx.getCause());
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}
		
		return dataInfo;
	}
	
	public List<RecipeParameter> getRecipeParaList(String machineName, String recipeName) throws CustomException
	{
		if(logger.isInfoEnabled())
		logger.info(String.format(" Input arguments: MachineName [%s] , RecipeName [%s].",machineName,recipeName));
		
		List<RecipeParameter> dataInfoList = null;
		
		try
		{
			dataInfoList = super.select(" WHERE 1=1 AND MACHINENAME = ? AND RECIPENAME = ? ", new Object[]{machineName,recipeName}, RecipeParameter.class);
		}
		catch (greenFrameDBErrorSignal greenEx)
		{
            if(greenEx.getErrorCode().equals(ErrorSignal.NotFoundSignal))
            	throw new CustomException("COMM-1000", "CT_RecipeParameter",String.format("MachineName = %s , RecipeName = %s ",machineName,recipeName));
            else 
            	throw new CustomException(greenEx.getCause());
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}
		
		logger.info(String .format(" Recipe [%s] Parameter size is %s .",recipeName,dataInfoList.size()));
		
		return dataInfoList;
	}
}
