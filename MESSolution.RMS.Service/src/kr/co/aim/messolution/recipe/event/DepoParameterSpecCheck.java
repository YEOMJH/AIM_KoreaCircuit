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
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;

/*
 * R2R -> MES
 * 
 * MACHINENAME
 * PROCESSOPERATIONNAME
 * MACHINERECIPENAME
 * PRODUCTSPECNAME
 * UNITNAME
 * SUBUNITNAME
 * R2RMODE
 * UNITRECIPENAME
 * RECIPEPARALIST
 *    PARA
 *       PARANAME
 *       PARAVALUE
 */

public class DepoParameterSpecCheck extends SyncHandler 
{
    Log log = LogFactory.getLog(this.getClass());
    
	@Override
	public Document doWorks(Document doc) throws CustomException
	{
		try
		{
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "DepoParameterSpecCheckReply");
			
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", true);
			String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
			String unitRecipeName = SMessageUtil.getBodyItemValue(doc, "UNITRECIPENAME", true);
			List<Element> recipeParaElementList = SMessageUtil.getBodySequenceItemList(doc, "RECIPEPARALIST", false);
			
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			CommonValidation.checkMachineHold(machineData);

			// check machine and Unit recipe info 
			Recipe mainRecipe = ExtendedObjectProxy.getRecipeService().getRecipeData(machineName, machineRecipeName);
			Recipe unitRecipe = ExtendedObjectProxy.getRecipeService().getRecipeData(unitName, unitRecipeName);
			
			// check machine parameter 
			ExtendedObjectProxy.getRecipeParamService().getRecipeParameterData(machineName, machineRecipeName, unitName, unitRecipeName);
			
			// get Unit parameter List
			List<RecipeParameter> uintParaList = ExtendedObjectProxy.getRecipeParamService().getRecipeParaList(unitName, unitRecipeName);
			
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
			
			doc = this.createReplyMessage(doc, checkResult);
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
	
	private Document createReplyMessage(Document doc, boolean checkResult)
	{
		Element originalHeaderElement = doc.getRootElement().getChild(SMessageUtil.Header_Tag);
		Element originalBodyElement = doc.getRootElement().getChild(SMessageUtil.Body_Tag);
		
		Element rootElement = new Element(SMessageUtil.Message_Tag);
		
		Element headerElement = (Element)originalHeaderElement.clone();
		headerElement.getChild("MESSAGENAME").setText("DepoParameterSpecCheckReply");
		headerElement.getChild("TRANSACTIONID").setText(TimeUtils.getCurrentEventTimeKey());
		
		rootElement.addContent(headerElement);
		
		Element bodyElement = new Element(SMessageUtil.Body_Tag);
		bodyElement.addContent(new Element("MACHINENAME").setText(originalBodyElement.getChildText("MACHINENAME")));
		bodyElement.addContent(new Element("UNITNAME").setText(originalBodyElement.getChildText("UNITNAME")));
		bodyElement.addContent(new Element("SUBUNITNAME").setText(originalBodyElement.getChildText("SUBUNITNAME")));
		bodyElement.addContent(new Element("MACHINERECIPENAME").setText(originalBodyElement.getChildText("MACHINERECIPENAME")));
		bodyElement.addContent(new Element("UNIQUEKEY").setText(originalHeaderElement.getChildText("TRANSACTIONID")));
		bodyElement.addContent(new Element("MODE").setText(originalBodyElement.getChildText("R2R_MODE")));
		bodyElement.addContent(new Element("RMS_CHECK_RESULT").setText(checkResult ? "OK" : "NG"));
		
		rootElement.addContent(bodyElement);
		
		return new Document(rootElement);
	}
	
	private Document setResultItemValue(Document doc,String result, String resultDescription) throws CustomException
	{
		Element headerElement = doc.getRootElement().getChild(SMessageUtil.Header_Tag);
		String uniqueKey = headerElement.getChildText("TRANSACTIONID");
		
		Element bodyElement = doc.getRootElement().getChild(SMessageUtil.Body_Tag);
		bodyElement.addContent(new Element("UNIQUEKEY").setText(uniqueKey));
		bodyElement.addContent(new Element("MODE").setText(bodyElement.getChildText("R2RMODE")));
		bodyElement.addContent(new Element("RMS_CHECK_RESULT").setText(result));
		
		bodyElement.removeChild("PROCESSOPERATIONNAME");
		bodyElement.removeChild("PRODUCTSPECNAME");
		bodyElement.removeChild("R2RMODE");
		bodyElement.removeChild("UNITRECIPENAME");
		bodyElement.removeChild("UNITRECIPENAME");
		bodyElement.removeChild("RECIPEPARALIST");

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
