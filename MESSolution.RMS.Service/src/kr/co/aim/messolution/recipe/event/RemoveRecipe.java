package kr.co.aim.messolution.recipe.event;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.map.ListOrderedMap;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.extended.object.management.data.RecipeParameter;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class RemoveRecipe extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", false);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "RECIPENAME", true);
		
		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		
		MESRecipeServiceProxy.getRecipeServiceUtil().verifyUserPrivilege(getEventUser(), machineName);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);		
		Recipe recipeInfo = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] {machineName, recipeName});
		
		//remove parameters cascading
		for (Element eleParameter : SMessageUtil.getBodySequenceItemList(doc, "RECIPEPARAMETERLIST", false))
		{
			String recipeParamName = SMessageUtil.getChildText(eleParameter, "RECIPEPARAMETERNAME", true);
			String value = SMessageUtil.getChildText(eleParameter, "VALUE", true);
			
			//DELETE
			try
			{
				RecipeParameter recipeParamInfo = ExtendedObjectProxy.getRecipeParamService().selectByKey(false, new Object[] {machineName, recipeName, recipeParamName, value});
				
				if (recipeInfo.getRecipeType().equals("MAIN")) 
				{
					RemoveSub(recipeParamInfo.getRecipeParameterName(), recipeParamInfo.getValue(), eventInfo);
				}
				
				eventInfo.setEventName("Remove");								
				ExtendedObjectProxy.getRecipeParamService().remove(eventInfo, recipeParamInfo);
			}
			catch (Exception ex)
			{
				eventLog.error(String.format("RecipeParam[%s %s %s] could not be purged cause any problem", machineName, recipeName, recipeParamName));
			}
		}
		
		//purge single recipe
		eventInfo.setEventName("Remove");
		ExtendedObjectProxy.getRecipeService().remove(eventInfo, recipeInfo);	
		
		// Change EQ Recipe ActiveState
		if(recipeInfo.getRecipeType().equals("UNIT"))
		{
			try
			{
				StringBuffer getParameterSQL = new StringBuffer("").append("  SELECT *  ")
						.append("  FROM CT_RECIPEPARAMETER RP ")
						.append("  WHERE RP.RECIPEPARAMETERNAME = :RECIPEPARAMETERNAME ")
						.append("  	AND RP.VALUE = :VALUE ");
				
				Object[] bindSet = new String[] { recipeInfo.getMachineName(), recipeInfo.getRecipeName() };
				
				List<ListOrderedMap> paramList = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate()
						.queryForList(getParameterSQL.toString(), bindSet);
				
				ListOrderedMap data = paramList.get(0);
				
				String mainMachine = data.get("MACHINENAME").toString();
				String mainRecipe = data.get("RECIPENAME").toString();
					
				Recipe mainRecipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[]{mainMachine, mainRecipe});
				
				mainRecipeData.setRMSFlag("N");
				mainRecipeData.setINTFlag("N");
				mainRecipeData.setENGFlag("N");
				mainRecipeData.setActiveState(constMap.Spec_NotActive);
				mainRecipeData.setRecipeState(constMap.RECIPESTATE_UNAPPROVED);
				mainRecipeData.setLastEventName(eventInfo.getEventName());
				mainRecipeData.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
				mainRecipeData.setLastEventUser(eventInfo.getEventUser());
				mainRecipeData.setLastEventComment(eventInfo.getEventComment());
				mainRecipeData = ExtendedObjectProxy.getRecipeService().modify(eventInfo, mainRecipeData);
			}
			catch (Exception e)
			{
				eventLog.error(String.format("Main Recipe data null"));
			}
		}
		
		return doc;
	}
	
	public void RemoveSub(String unitName, String unitRecipeName, EventInfo eventInfo) throws CustomException 
	{
		eventInfo.setEventName("Remove");
		
		//Param Remove
		List<RecipeParameter> parameterList = new ArrayList<>();
		
		try
		{
			parameterList = ExtendedObjectProxy.getRecipeParamService().select("machineName = ? AND recipeName = ? ", new Object[] {unitName, unitRecipeName});
		}
		catch (Exception e)
		{
			eventLog.info(String.format(unitName + " " + unitRecipeName + " parameter size 0"));
		}
		
		if(parameterList.size() > 0)
		{
			ExtendedObjectProxy.getRecipeParamService().remove(eventInfo, parameterList);
		}
		
		//Recipe Remove
		Recipe unitRecipeInfo = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] { unitName, unitRecipeName });
		ExtendedObjectProxy.getRecipeService().remove(eventInfo, unitRecipeInfo);
	}
	
	public void RemoveSubWithOutUnit(String subUnitName, String subUnitRecipeName, EventInfo eventInfo) throws CustomException {

		//Param Remove
		StringBuffer getParameterSQL = new StringBuffer("").append("  SELECT *  ")
				.append("  FROM CT_RECIPEPARAMETER RP ")
				.append("  WHERE RP.MACHINENAME = :MACHINENAME ")
				.append("  	AND RP.RECIPENAME = :RECIPENAME ");
		
		Object[] bindSet = new String[] { subUnitName, subUnitRecipeName };
		
		List<ListOrderedMap> paramList = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(getParameterSQL.toString(), bindSet);
		
		eventInfo.setEventName("Remove");
		
		for (int i = 0; i < paramList.size(); i++) {
			ListOrderedMap data = paramList.get(i);
			
			String paramName = data.get("RECIPEPARAMETERNAME").toString();
			String value = data.get("VALUE").toString();
			RecipeParameter recipeParamInfo = ExtendedObjectProxy.getRecipeParamService().selectByKey(false, new Object[] {subUnitName, subUnitRecipeName, paramName, value});
			
			ExtendedObjectProxy.getRecipeParamService().remove(eventInfo, recipeParamInfo);	
		}
		
		//Recipe Remove
		Recipe unitRecipeInfo = ExtendedObjectProxy.getRecipeService().selectByKey(false,
				new Object[] { subUnitName, subUnitRecipeName });
		ExtendedObjectProxy.getRecipeService().remove(eventInfo, unitRecipeInfo);
	}
	
	/*
	public void RemovePPID(String superMachineName, String recipeName, EventInfo eventInfo) throws CustomException{
		
		StringBuffer sqlBuffer = new StringBuffer("").append("  SELECT R.MACHINENAME, R.RECIPENAME ")
				.append("  FROM (SELECT MACHINENAME FROM MACHINE M WHERE M.SUPERMACHINENAME = ?) ML, CT_RECIPE R ").append("  WHERE ML.MACHINENAME = R.MACHINENAME");
		String sqlStmt = sqlBuffer.toString();
		Object[] bindSet = new String[] { superMachineName };

		List<ListOrderedMap> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStmt, bindSet);
		
		String paramName = "";
		String machineName = "";
		
		for(int i = 0 ; i < sqlResult.size() ; i++)
		{
			ListOrderedMap data = sqlResult.get(i);
			machineName = data.get("MACHINENAME").toString();
			recipeName = data.get("RECIPENAME").toString();
			//List<RecipeParameter> recipeParamList = ExtendedObjectProxy.getRecipeParamService().select("MACHINENAME = ? AND RECIPENAME = ?", new Object[] {machineName, recipeName});
			
			StringBuffer sqlBuffer2 = new StringBuffer("").append(" SELECT MACHINENAME, RECIPENAME, RECIPEPARAMETERNAME ")
					.append(" FROM CT_RECIPEPARAMETER ").append(" WHERE MACHINENAME = ? AND RECIPENAME = ?");
			String sqlStmt2 = sqlBuffer2.toString();
			Object[] bindSet2 = new String[] { machineName, recipeName };

			List<ListOrderedMap> sqlResult2 = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStmt2, bindSet2);
			
			try
			{
				for(int j = 0 ; j < sqlResult2.size() ; j++)
				{
					ListOrderedMap dataParam = sqlResult2.get(j);
					RecipeParameter dataInfo = ExtendedObjectProxy.getRecipeParamService().selectByKey(false, new Object[] {machineName, recipeName, dataParam.get("RECIPEPARAMETERNAME").toString()});
					paramName = dataInfo.getRecipeParameterName();
					ExtendedObjectProxy.getRecipeParamService().remove(eventInfo, dataInfo);
					RemovePPID(machineName, recipeName, eventInfo);
				}
			}
			catch (Exception ex)
			{
				eventLog.error(String.format("RecipeParam[%s %s %s] could not be purged cause any problem", machineName, paramName ));
			}
			
			Recipe unitRecipeInfo = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] {machineName, recipeName});
			ExtendedObjectProxy.getRecipeService().remove(eventInfo, unitRecipeInfo);
		}
	} */
}
