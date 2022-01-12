package kr.co.aim.messolution.recipe.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.omg.CORBA.ExceptionList;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.extended.object.management.data.RecipeCheckResult;
import kr.co.aim.messolution.extended.object.management.data.RecipeCheckedMachine;
import kr.co.aim.messolution.extended.object.management.data.RecipeParameter;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class RecipeCheckRequestForTension extends SyncHandler {

	private static Log log = LogFactory.getLog(RecipeCheckRequestForTension.class);
	
	@Override
	public Document doWorks(Document doc) throws CustomException
	{
		try 
		{
			String result = SMessageUtil.getBodyItemValue(doc, "RESULT", false);
			String resultDesc = SMessageUtil.getBodyItemValue(doc, "RESULTDESCRIPTION", false);
			String version = SMessageUtil.getBodyItemValue(doc, "VERSION", false);
			List<Element> machineParamList = SMessageUtil.getBodySequenceItemList(doc, "PARAMETERLIST", false);
			
			this.prepareReply(doc);
			
			String mainMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", true);
			String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
			String PPID = SMessageUtil.getBodyItemValue(doc, "RECIPENAME", true);
			String recipeType = SMessageUtil.getBodyItemValue(doc, "RECIPETYPE", true);
			String unitName = SMessageUtil.getBodyItemValue(doc, "EQPNAME", false);
			String unitRecipeName = SMessageUtil.getBodyItemValue(doc, "EQPRECIPENAME", false);
			
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("UpdateRecipeCheckResult", getEventUser(), getEventComment(), null, null);
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			ConstantMap constMap = GenericServiceProxy.getConstantMap();
			
			if(result.equals("NG"))
			{
				doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckReplyForTension("NG",  doc, "Machine reported NG");			
				updateRecipeCheckResult(mainMachineName, subUnitName, result, resultDesc, eventInfo, unitName, maskName, PPID);
				return doc;
			}
			
			if(recipeType.equals("E"))
			{
				doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckReplyForTension("OK",  doc, "");
				return doc;
			}
			
			Recipe unitRecipe;
			
			try //Get Unit Recipe
			{ 
				unitRecipe = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] {unitName, unitRecipeName});
			}
			catch (Exception e)
			{
				doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckReplyForTension("NG",  doc, "Get Recipe[" + unitRecipeName + "] fail");
				updateRecipeCheckResult(mainMachineName, subUnitName, "NG", "Get Recipe[" + unitRecipeName + "] fail", eventInfo, unitName, maskName, PPID);
				return doc;
			}
			
			if(unitRecipe.getActiveState().equals(constMap.Spec_NotActive))
			{
				doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckReplyForTension("NG",  doc, unitRecipeName + " ActiveState is NotActive");
				updateRecipeCheckResult(mainMachineName, subUnitName, "NG", unitRecipeName + " ActiveState is NotActive", eventInfo, unitName, maskName, PPID);
				return doc;
			}
			
			if(!(unitRecipe.getINTFlag().equals("Y") && unitRecipe.getMFGFlag().equals("Y") && unitRecipe.getENGFlag().equals("Y"))) //Validate PPID Flag
			{
				doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckReplyForTension("NG",  doc, unitRecipeName + " Check Flag N");
				updateRecipeCheckResult(mainMachineName, subUnitName, "NG", unitRecipeName + " Flag is not Y", eventInfo, unitName, maskName, PPID);
				return doc;
			}
			
			if(unitRecipe.getVersionCheckFlag().equals("Y"))
			{
				if(unitRecipe.getVERSION().equals(version))
				{
					doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckReply("OK",  doc, "");
					updateRecipeCheckResult(mainMachineName, subUnitName, "OK", "", eventInfo, unitName, maskName, PPID);
				}
				else
				{
					doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckReply("NG",  doc, unitName + " has different recipe version[" + unitRecipeName +"]");
					updateRecipeCheckResult(mainMachineName, subUnitName, "NG", unitName + " has different recipe version[" + unitRecipeName +"]", eventInfo, unitName, maskName, PPID);
				}
			}
			else
			{
				List<RecipeParameter> mesParamList;
				int machineParamCount = machineParamList.size();
				int mesParamCount = 0;
				try
				{ 
		        	// Unit Param
					mesParamList = ExtendedObjectProxy.getRecipeParamService().select("machineName = ? AND recipeName = ?  ", new Object[] {unitName, unitRecipeName});
					mesParamCount = mesParamList.size();
				}
				catch (Exception e)
				{
					doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckReply("NG",  doc, unitRecipeName + " MES Parameter quantity 0");
					updateRecipeCheckResult(mainMachineName, subUnitName, "NG", unitRecipeName + " MES Parameter count 0", eventInfo, unitName, maskName, PPID);
					return doc;
				}
				
				if(machineParamCount != mesParamCount)
				{
					doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckReply("NG",  doc, unitRecipeName + " MES Parameter quantity is different from machine parameter quantity");
					updateRecipeCheckResult(mainMachineName, subUnitName, "NG", unitRecipeName + " MES Parameter quantity is different from machine parameter quantity", eventInfo, unitName, maskName, PPID);
					return doc;
				}
				
				for(RecipeParameter paramInfoMES : mesParamList)
				{
					if(paramInfoMES.getCheckFlag().equals("N"))
					{
						continue;
					}
					
					boolean isFound = false;
					for(Element paramInfoMachine : machineParamList)
					{
						String machineParamName = SMessageUtil.getChildText(paramInfoMachine, "PARAMETERNAME", true);
					    String machineParamValue = SMessageUtil.getChildText(paramInfoMachine, "VALUE", true);
					    
					    if(machineParamName.equals(paramInfoMES.getRecipeParameterName()))
					    {
					    	isFound = true;
					    	boolean valueFlag = false;
					    	if(paramInfoMES.getValidationType().equals("Range"))
					    	{
					    		double upperLimit = Double.parseDouble(paramInfoMES.getUpperLimit());
					    		double lowerLimit = Double.parseDouble(paramInfoMES.getLowerLimit());
					    		
					    		if(Double.parseDouble(machineParamValue) <= upperLimit && Double.parseDouble(machineParamValue) >= lowerLimit)
					    		{
					    			valueFlag = true;
					    		}
					    	}
					    	else
					    	{
					    		if(machineParamValue.equals(paramInfoMES.getTarget()))
					    		{
					    			valueFlag = true;
					    		}
					    	}
					    	
					    	if(!valueFlag)
					    	{
					    		doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckReply("NG",  doc, "Recipe[" + unitRecipeName + "] Parameter[" + machineParamName + "] is wrong");
								updateRecipeCheckResult(mainMachineName, subUnitName, "NG", "Recipe[" + unitRecipeName + "] Parameter[" + machineParamName + "] is wrong", eventInfo, unitName, maskName, PPID);
								return doc;
					    	}
					    	else
					    	{
					    		break;
					    	}
					    }
					}
					
					if(!isFound)
					{
						doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckReply("NG",  doc, "Recipe[" + unitRecipeName + "] Parameter[" + paramInfoMES.getRecipeParameterName() + "] found fail");
						updateRecipeCheckResult(mainMachineName, subUnitName, "NG", "Recipe[" + unitRecipeName + "] Parameter[" + paramInfoMES.getRecipeParameterName() + "] found fail", eventInfo, unitName, maskName, PPID);
						return doc;
					}
				}
				doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckReplyForTension("OK",  doc, "");
				updateRecipeCheckResult(mainMachineName, subUnitName, "OK", "", eventInfo, unitName, maskName, PPID);		
			}
			
			return doc;
		} 
		catch (CustomException ce)
		{
			SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
			SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", ce.errorDef.getLoc_errorMessage());
			throw new CustomException("SYS-0010", ce.errorDef.getLoc_errorMessage());
		}
		catch (Exception e)
		{
			SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
			SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", e.toString());
			throw new CustomException(e);
		}
	}
	
	private void prepareReply(Document doc) throws CustomException
	{
		String oldSourceSubjectName = SMessageUtil.getHeaderItemValue(doc, "SOURCESUBJECTNAME", false);
		String oldTargetSubjectName = SMessageUtil.getHeaderItemValue(doc, "TARGETSUBJECTNAME", false);

		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "RecipeCheckReplyForTension");
		SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", oldTargetSubjectName);
		SMessageUtil.setHeaderItemValue(doc, "SOURCESUBJECTNAME", oldTargetSubjectName);
		SMessageUtil.setHeaderItemValue(doc, "TARGETSUBJECTNAME", oldSourceSubjectName);
		
		try
		{
			Element bodyElement = SMessageUtil.getBodyElement(doc);
			
			bodyElement.removeChild("VERSION");
			bodyElement.removeChild("PARAMETERLIST");
			bodyElement.removeChild("RESULT");
			bodyElement.removeChild("RESULTDESCRIPTION");

			Element attResult = new Element("RESULT");
			attResult.setText("");
			bodyElement.addContent(attResult);
			
			Element attResultDesc = new Element("RESULTDESCRIPTION");
			attResultDesc.setText("");
			bodyElement.addContent(attResultDesc);
		}
		catch (Exception ex)
		{
			//RMS-013:RMSError:Reply message  writing failed
			throw new CustomException("RMS-013");
		}
	}
	
	private void updateRecipeCheckResult(String mainMachineName, String subUnitName, String result, String comment, EventInfo eventInfo, String unitName, String maskName, String PPID) throws greenFrameDBErrorSignal, CustomException
	{
		RecipeCheckResult recipeCheckResultLock = ExtendedObjectProxy.getRecipeCheckResultService().selectByKey(true, new Object[]{mainMachineName, subUnitName, maskName, PPID});
		
		recipeCheckResultLock.setUpdateTimeKey(ConvertUtil.getCurrTimeKey());
		recipeCheckResultLock.setCheckUnitQty(recipeCheckResultLock.getCheckUnitQty() + 1);
		
		if(result.equals("OK"))
		{
			if(recipeCheckResultLock.getCheckUnitQty() >= recipeCheckResultLock.getUnitQty())
			{
				recipeCheckResultLock.setResult("OK");
			}
		}
		else
		{
			recipeCheckResultLock.setResult("NG");
		}
		recipeCheckResultLock.setResultComment(comment);
		
		try
		{
			ExtendedObjectProxy.getRecipeCheckResultService().modify(eventInfo, recipeCheckResultLock, unitName);
		}
		catch (Exception ex)
		{}
	}
}
