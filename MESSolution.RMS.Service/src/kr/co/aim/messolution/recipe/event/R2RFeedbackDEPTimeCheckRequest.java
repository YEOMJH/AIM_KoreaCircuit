package kr.co.aim.messolution.recipe.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.extended.object.management.data.RecipeParameter;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;

/*
 * R2R -> MES
 * 
 * Change Message Name : DepoParameterSpecCheck
 */

public class R2RFeedbackDEPTimeCheckRequest extends SyncHandler 
{
    Log log = LogFactory.getLog(this.getClass());
	@Override
	public Document doWorks(Document doc) throws CustomException
	{
		try
		{
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "R2RFeedbackDEPTimeCheckReply");
			
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", true);
			String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
			String recipeName = SMessageUtil.getBodyItemValue(doc, "RECIPENAME", true);
			List<Element> recipeParaElementList = SMessageUtil.getBodySequenceItemList(doc, "RECIPEPARALIST", false);
			
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			CommonValidation.checkMachineHold(machineData);
			
			// check machine and Unit recipe info 
			Recipe mainRecipe = ExtendedObjectProxy.getRecipeService().getRecipeData(machineName, machineRecipeName);
			Recipe unitRecipe = ExtendedObjectProxy.getRecipeService().getRecipeData(unitName, recipeName);
			
			// check machine parameter 
			ExtendedObjectProxy.getRecipeParamService().getRecipeParameterData(machineName, machineRecipeName, unitName, recipeName);
			
			// get Unit parameter List
			List<RecipeParameter> uintParaList = ExtendedObjectProxy.getRecipeParamService().getRecipeParaList(unitName, recipeName);
			
			Map<String,RecipeParameter> recipeParaDataMap = this.makeRecipeParaDataMap(uintParaList);

			boolean checkResult = true;
			for (Element paraElement : recipeParaElementList)
			{
				String paraName = paraElement.getChildText("PARANAME");
				String paraValue = paraElement.getChildText("PARAVALUE");

				RecipeParameter paraData = recipeParaDataMap.get(paraName);

				if (paraData == null)
				{
					checkResult = false;
					paraElement.addContent(new Element("RESULT").setText("NG"));
					log.info(String.format("Parameter[%s] not registered on machine.", paraName));
				}
				else
				{
					if ("Range".equals(paraData.getValidationType()))
					{
						String lowerLimit = paraData.getLowerLimit();
						String upperLimit = paraData.getUpperLimit();

						if (CommonValidation.isNumeric(paraValue, lowerLimit, upperLimit))
						{
							if (Double.parseDouble(lowerLimit) < Double.parseDouble(paraValue) && Double.parseDouble(upperLimit) > Double.parseDouble(paraValue))
							{
								paraElement.addContent(new Element("RESULT").setText("OK"));
							}
							else
							{
								checkResult = false;
								paraElement.addContent(new Element("RESULT").setText("NG"));
								
								log.info(String.format("Range Validation Fail: Out of range. [BC:%s , MES: Upper(%s) , Lower(%s)]",paraValue,upperLimit,lowerLimit));
							}
						}
						else
						{
							checkResult = false;
							paraElement.addContent(new Element("RESULT").setText("NG"));
							
							log.info(String.format("Parameter value is not numeric .[BC:%s , MES: Upper(%s) , Lower(%s)]",paraValue,upperLimit,lowerLimit));
						}

					}
					else if ("Target".equals(paraData.getValidationType()))
					{
						String targetValue = paraData.getValue();

						if (CommonValidation.isNumeric(paraValue, targetValue))
						{
							if (Double.parseDouble(targetValue) == Double.parseDouble(paraValue))
							{
								paraElement.addContent(new Element("RESULT").setText("OK"));
							}
							else
							{
								checkResult = false;
								paraElement.addContent(new Element("RESULT").setText("NG"));
								
								log.info(String.format("Target Validation Fail:Inconsistent values.[BC:%s , MES:%s]",paraValue,targetValue));
							}
						}
						else
						{
							checkResult = false;
							paraElement.addContent(new Element("RESULT").setText("NG"));
							
							log.info(String.format("Parameter value is not numeric.[BC:%s , MES:%s]",paraValue,targetValue));
						}
					}
					else
					{
						// RECIPE-0008: ERROR: Exist validationType is not Target or Range ,Please Check!! [recipeParameterName: {0}]
						throw new CustomException("RECIPE-0008", paraName);
					}
				}
			}
			
			if (checkResult)
				setResultItemValue(doc, "OK", "");
			else
				setResultItemValue(doc, "NG", "Recipe parameter check fail.");
			
		}
		catch (CustomException ce)
		{
			setResultItemValue(doc, "NG", ce.errorDef.getLoc_errorMessage());
			throw ce;
		}
		catch (Exception ex)
		{
			setResultItemValue(doc, "NG", ex.getMessage());
			throw new CustomException(ex.getCause());
		}

		return doc;
	}
	
	private Document setResultItemValue(Document doc,String result, String resultDescription) throws CustomException
	{
		Element bodyElement = doc.getRootElement().getChild(SMessageUtil.Body_Tag);
		bodyElement.addContent(new Element("RESULT").setText(result));
		bodyElement.addContent(new Element("RESULTDESCRIPTION").setText(resultDescription));

		return doc;
	}
	
	private Map<String,RecipeParameter> makeRecipeParaDataMap(List<RecipeParameter> uintParaList)
	{
		Map<String, RecipeParameter> recipeParaDataMap = new HashMap<>();

		for (RecipeParameter unitPara : uintParaList)
			recipeParaDataMap.put(unitPara.getRecipeParameterName(), unitPara);
		
		return recipeParaDataMap;
	}
}
