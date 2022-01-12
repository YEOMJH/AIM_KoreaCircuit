package kr.co.aim.messolution.recipe.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.extended.object.management.data.RecipeCheckResult;
import kr.co.aim.messolution.extended.object.management.data.RecipeParameter;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;

public class RecipeCheckStartRequestForTension extends SyncHandler {

	@Override
	public Document doWorks(Document doc) throws CustomException {
		try 
		{
			this.prepareReply(doc);
			
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", true);
			String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
			String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "RECIPENAME", true);
			String RMSFlag = "";
			boolean unitCheckFlagIsN = false;
			List<Recipe> recipeList = new ArrayList<Recipe>();
			
			ConstantMap constMap = GenericServiceProxy.getConstantMap();
			
			//Remove Last Data
			EventInfo eventInfoDel = EventInfoUtil.makeEventInfo("DeleteRecipeCheckResert", getEventUser(), getEventComment(), null, null);
			List<RecipeCheckResult> delInfoList = new ArrayList<RecipeCheckResult>();
			try
			{
				String condition = " WHERE MACHINENAME = ? AND PORTNAME = ? ";
				
				delInfoList = ExtendedObjectProxy.getRecipeCheckResultService().select(condition, new Object[]{machineName, subUnitName});
				
				for(int i = 0 ; i < delInfoList.size() ; i++)
				{
					ExtendedObjectProxy.getRecipeCheckResultService().remove(eventInfoDel, delInfoList.get(i), "MAIN");
				}
			}
			catch (Exception e)
			{
				eventLog.info("Delete Data is null");
			}
			
			// Search data
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

			// Check Mask
			MaskLot maskLotInfo = ExtendedObjectProxy.getMaskLotService().getMaskLotData(maskName);
			
			List<Map<String, Object>> policyData = PolicyUtil.getTRFOAbnormalRecipeInfo(maskLotInfo.getFactoryName(), maskLotInfo.getMaskSpecName(), maskLotInfo.getMaskProcessFlowName(), maskLotInfo.getMaskProcessFlowVersion(), 
					maskLotInfo.getMaskProcessOperationName(), maskLotInfo.getMaskProcessOperationVersion(), machineName, machineRecipeName);
			
			RMSFlag = policyData.get(0).get("RMSFLAG").toString();
			
			//Check machine hold
			CommonValidation.checkMachineHold(machineData);
			
			if(RMSFlag.equals("Y"))
			{
				Recipe mainRecipeInfo;
				
				try //Get PPID
				{ 
					mainRecipeInfo = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] {machineName, machineRecipeName});
				}
				catch (Exception e)
				{
					doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReplyForTension(machineRecipeName, "NG", recipeList, doc, machineRecipeName + " : PPID null");
					return doc;
				}
				
				if(!mainRecipeInfo.getActiveState().equals(constMap.Spec_Active)) //Validate PPID ActiveState
				{
					doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReplyForTension(machineRecipeName, "NG", recipeList, doc, machineRecipeName + " : NotActive Recipe");
					return doc;
				}
				
				if(!(mainRecipeInfo.getINTFlag().equals("Y") && mainRecipeInfo.getMFGFlag().equals("Y") && mainRecipeInfo.getENGFlag().equals("Y"))) //Validate PPID Flag
				{
					doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReplyForTension(machineRecipeName, "NG", recipeList, doc, machineRecipeName + " : Check Flag");
					return doc;
				}
				
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeActiveState", getEventUser(), getEventComment(), null, null);
				
				String autoChangeFlag = mainRecipeInfo.getAutoChangeFlag();
				
				if(autoChangeFlag.equals("INTENG")||autoChangeFlag.equals("ENGINT"))
				{
					SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date lastTrackOutTime = null;
					String currentTime = transFormat.format(new Date());
					Date currentDate = null;
					boolean firstTrackInFlag = false;
					
					try 
					{
						currentDate = transFormat.parse(currentTime);
						
						if(StringUtil.isNotEmpty(mainRecipeInfo.getLastTrackOutTimeKey()))
						{
							SimpleDateFormat beforeTransFormat = new SimpleDateFormat("yyyyMMddHHmmss");
							Date tempDate = beforeTransFormat.parse(mainRecipeInfo.getLastTrackOutTimeKey());
							//String trackOutTimeKey = mainRecipeInfo.getLastTrackOutTimeKey().substring(0, 14);
							String transDate = transFormat.format(tempDate);
							lastTrackOutTime = transFormat.parse(transDate);	
						}
					} 
					catch (ParseException e) 
					{}
					
					if(lastTrackOutTime != null)
					{
						double gap = (double)(currentDate.getTime() - lastTrackOutTime.getTime()) / (double)(60 * 60 * 1000);
						
						if (gap >= mainRecipeInfo.getDurationUsedLimit())
						{
							doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReplyForTension(machineRecipeName, "NG", recipeList, doc, machineRecipeName + " : DurationUsedLimit Over");
							mainRecipeInfo.setActiveState(constMap.Spec_NotActive);
							mainRecipeInfo.setRecipeState(constMap.RECIPESTATE_UNAPPROVED);
						    mainRecipeInfo.setINTFlag("N");
							mainRecipeInfo.setENGFlag("N");
							mainRecipeInfo.setRMSFlag("N");					
							ExtendedObjectProxy.getRecipeService().modify(eventInfo, mainRecipeInfo);
							return doc;
						}
					}
					else
					{
						firstTrackInFlag = true;
					}
					
					if(firstTrackInFlag)
					{
						Date lastApproveTime = null;
						try 
						{
							lastApproveTime = transFormat.parse(mainRecipeInfo.getLastApporveTime().toString());
						} 
						catch (ParseException e) 
						{
							doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReplyForTension(machineRecipeName, "NG", recipeList, doc, "LastApproveTime null");	
							return doc;
						}
						
						double gap = (double)(currentDate.getTime() - lastApproveTime.getTime()) / (double)(60 * 60 * 1000);
						
						if (gap >= mainRecipeInfo.getDurationUsedLimit())
						{
							doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReplyForTension(machineRecipeName, "NG", recipeList, doc, machineRecipeName + " : DurationUsedLimit Over");
							mainRecipeInfo.setActiveState(constMap.Spec_NotActive);
							mainRecipeInfo.setRecipeState(constMap.RECIPESTATE_UNAPPROVED);
							mainRecipeInfo.setINTFlag("N");
							mainRecipeInfo.setENGFlag("N");
							mainRecipeInfo.setRMSFlag("N");

							ExtendedObjectProxy.getRecipeService().modify(eventInfo, mainRecipeInfo);
							return doc;
						}
					}
					
					if(mainRecipeInfo.getTimeUsed() + 1 > mainRecipeInfo.getTimeUsedLimit())
					{
						doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReplyForTension(machineRecipeName, "NG", recipeList, doc, machineRecipeName + " : TimeUsedLimit Over");
						mainRecipeInfo.setActiveState(constMap.Spec_NotActive);
						mainRecipeInfo.setINTFlag("N");
						mainRecipeInfo.setENGFlag("N");
						mainRecipeInfo.setRMSFlag("N");
						
						mainRecipeInfo.setRecipeState(constMap.RECIPESTATE_UNAPPROVED);
						ExtendedObjectProxy.getRecipeService().modify(eventInfo, mainRecipeInfo);
						return doc;
					}
					
					if (eventInfo.getEventTime().compareTo(mainRecipeInfo.getMaxDurationUsedLimit()) == 1)
					{
						doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReplyForTension(machineRecipeName, "NG", recipeList, doc, machineRecipeName + " : MaxDurationUsedLimit Over");
						mainRecipeInfo.setActiveState(constMap.Spec_NotActive);
						mainRecipeInfo.setRecipeState(constMap.RECIPESTATE_UNAPPROVED);
						mainRecipeInfo.setINTFlag("N");
						mainRecipeInfo.setENGFlag("N");
						mainRecipeInfo.setRMSFlag("N");
						
						ExtendedObjectProxy.getRecipeService().modify(eventInfo, mainRecipeInfo);
						return doc;
					}
				}
				
				List<RecipeParameter> paramList;
		        
		        try //Get PPID Param
				{ 
					paramList = ExtendedObjectProxy.getRecipeParamService().select("machineName = ? AND recipeName = ?  ", new Object[] {machineName, machineRecipeName});
				}
				catch (Exception e)
				{
					doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReplyForTension(machineRecipeName, "NG", recipeList, doc, machineRecipeName + " : Parameter null");
					return doc;
				}
				
		        for(RecipeParameter paramInfo : paramList)
		        {
		        	if(!paramInfo.getCheckFlag().equals("N"))
		        	{
		        		Recipe unitRecipeInfo;
		        		
		        		try //Get PPID
		    			{ 
		        			unitRecipeInfo = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] {paramInfo.getRecipeParameterName(), paramInfo.getValue()});
		    			}
		        		catch (Exception e)
		    			{
		    				doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReplyForTension(machineRecipeName, "NG", recipeList, doc, paramInfo.getRecipeParameterName() + " : Recipe is Null");
		    				return doc;
		    			}
		        		
		        		if(!StringUtil.in("Y", unitRecipeInfo.getVersionCheckFlag(),unitRecipeInfo.getUnitCheckFlag()))
		        			unitCheckFlagIsN = true;

		        		recipeList.add(unitRecipeInfo);
		        	}
		        }
				
				long unitCount = recipeList.size();
				
				if(unitCheckFlagIsN)
				{
					doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReplyForTension(machineRecipeName, "NG", recipeList, doc, "UintCheckFlag and VersionCheckFlag is N");
					return doc;
				}
				
				if(unitCount == 0)
				{
					doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReplyForTension(machineRecipeName, "SKIP", recipeList, doc, "All of PPID parameter CheckFlag N");
					return doc;
				}
				
				//MainRecipeCheck
				RecipeCheckResult recipeCheckResult = new RecipeCheckResult();
				
				recipeCheckResult.setMachineName(machineName);
				recipeCheckResult.setPortName(subUnitName);
				recipeCheckResult.setCarrierName(maskName);
				recipeCheckResult.setCheckLevel("");
				recipeCheckResult.setRecipeName(machineRecipeName);
				recipeCheckResult.setOriginalSubjectName("");
				recipeCheckResult.setUnitQty(unitCount);
				recipeCheckResult.setCheckUnitQty(0);
				recipeCheckResult.setSubUnitQty(0);
				recipeCheckResult.setCheckSubUnitQty(0);
				recipeCheckResult.setCreateTimeKey(ConvertUtil.getCurrTimeKey());
				
				EventInfo eventInfoCreate = EventInfoUtil.makeEventInfo("InsertRecipeCheckResult", getEventUser(), getEventComment(), null, null);
				eventInfoCreate.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				
				ExtendedObjectProxy.getRecipeCheckResultService().create(eventInfoCreate, recipeCheckResult, "MAIN");
				
				doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReplyForTension(machineRecipeName, "OK", recipeList, doc, "");
			}
			else
			{
				doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReplyForTension(machineRecipeName, "SKIP", recipeList, doc, "RMSFlag = N");
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

		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "RecipeCheckStartReplyForTension");
		SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", oldTargetSubjectName);
		SMessageUtil.setHeaderItemValue(doc, "SOURCESUBJECTNAME", oldTargetSubjectName);
		SMessageUtil.setHeaderItemValue(doc, "TARGETSUBJECTNAME", oldSourceSubjectName);
		
		try
		{
			Element bodyElement = SMessageUtil.getBodyElement(doc);
			{
				Element attUnitList = new Element("UNITLIST");
				bodyElement.addContent(attUnitList);
				
				Element attResult = new Element("RESULT");
				attResult.setText("");
				bodyElement.addContent(attResult);
				
				Element attResultDesc = new Element("RESULTDESCRIPTION");
				attResultDesc.setText("");
				bodyElement.addContent(attResultDesc);
			}
		}
		catch (Exception ex)
		{
			//RMS-013:RMSError:Reply message  writing failed
			throw new CustomException("RMS-013");
		}
	}
}
