package kr.co.aim.messolution.recipe.event;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.extended.object.management.data.RecipeParameter;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.extended.object.management.impl.POSMachineService;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;

public class SyncRecipeParameter extends SyncHandler 
{
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String mainMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME",false);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
			
		MESRecipeServiceProxy.getRecipeServiceUtil().verifyUserPrivilege(getEventUser(), mainMachineName);
		
		MachineSpec machine = getBaseMachine(mainMachineName, unitName);
		
		ConstantMap constMap = GenericServiceProxy.getConstantMap();

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(),getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
        
		for (Element eleRecipe : SMessageUtil.getBodySequenceItemList(doc,"RECIPELIST", false))
		{
			String rcpFlag = SMessageUtil.getChildText(eleRecipe, "JOBFLAG", true);
			String recipeName = SMessageUtil.getChildText(eleRecipe,"RECIPENAME", true);
			String machineNameOfRecipe = SMessageUtil.getChildText(eleRecipe,"MACHINENAME", true);
			String version = SMessageUtil.getChildText(eleRecipe,"VERSION", false);

			try
			{
				if (rcpFlag.equals("C"))
					createRecipe(eventInfo, recipeName, machine, version);
				else if (rcpFlag.equals("E"))
				{
					List<Object[]> updateParamArgList = new ArrayList<Object[]>();
					List<Object[]> updateParamHistArgList = new ArrayList<Object[]>();
					List<Object[]> createParamArgList = new ArrayList<Object[]>();
					List<Object[]> createParamHistArgList = new ArrayList<Object[]>();
					
					StringBuffer sqlBufferUpdate = new StringBuffer("").append(" UPDATE CT_RECIPEPARAMETER ").append("   SET  VALUE = ?, ").append("   OLDVALUE = ? ").append("  WHERE ROWNUM=1 ")
							.append("	 AND MACHINENAME= ? ").append("    AND RECIPENAME = ? ").append("    AND RECIPEPARAMETERNAME = ? ");
					
					StringBuffer sqlBufferHistory = new StringBuffer("").append(" INSERT INTO CT_RECIPEPARAMETERHISTORY VALUES (?, ?,  ").append("   ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");
					String sqlParam = sqlBufferUpdate.toString();
					String sqlParamHist = sqlBufferHistory.toString();
					
					String sqlParamCreate = "INSERT INTO CT_RECIPEPARAMETER(MACHINENAME, RECIPENAME, RECIPEPARAMETERNAME, VALUE) VALUES(?, ?, ?, ?)";
					String sqlParamCreateHistory = "INSERT INTO CT_RECIPEPARAMETERHISTORY(MACHINENAME, RECIPENAME, RECIPEPARAMETERNAME, VALUE, TIMEKEY, EVENTNAME, EVENTUSER) VALUES(?, ?, ?, ?, ?, ?, ?)";
					
					List<RecipeParameter> paramList = new ArrayList<RecipeParameter>();
					
					try
					{
						paramList = ExtendedObjectProxy.getRecipeParamService().select("machineName = ? AND recipeName = ? ",
								new Object[] { machine.getKey().getMachineName(), recipeName });
					}
					catch (Exception e)
					{
						
					}
					
					boolean INTFlag = false;
					
					for (Element eleParameter : SMessageUtil.getSubSequenceItemList(eleRecipe, "RECIPEPARAMETERLIST", false)) 
					{
						String paraFlag = SMessageUtil.getChildText(eleParameter,"JOBFLAG", false);
						String recipeParamName = SMessageUtil.getChildText(eleParameter, "RECIPEPARAMETERNAME", false);
						String paramValue = SMessageUtil.getChildText(eleParameter,"VALUE", false);

						try 
						{
							if (paraFlag.equals("C"))
							{
								if(!INTFlag)
								{
									INTFlag = true;
								}
								
								List<Object> paramBindList = new ArrayList<Object>();
								List<Object> paramHistBindList = new ArrayList<Object>();
								
								//RecipeParam
								paramBindList.add(machine.getKey().getMachineName());
								paramBindList.add(recipeName);
								paramBindList.add(recipeParamName);
								paramBindList.add(paramValue);

								//RecipeParamHistory
								paramHistBindList.add(machine.getKey().getMachineName());
								paramHistBindList.add(recipeName);
								paramHistBindList.add(recipeParamName);
								paramHistBindList.add(paramValue);
								paramHistBindList.add(eventInfo.getEventTimeKey());
								paramHistBindList.add("Sync-Create");
								paramHistBindList.add(eventInfo.getEventUser());
								
								createParamArgList.add(paramBindList.toArray());
								createParamHistArgList.add(paramHistBindList.toArray());
							}	
							else if (paraFlag.equals("M"))
							{
								List<Object> paramBindList = new ArrayList<Object>();
								List<Object> paramHistBindList = new ArrayList<Object>();				

								RecipeParameter oldRecipeParamInfo = new RecipeParameter();
								
								for(RecipeParameter param : paramList)
								{
									if(param.getMachineName().equals(machine.getKey().getMachineName()) && param.getRecipeName().equals(recipeName) && param.getRecipeParameterName().equals(recipeParamName))
									{
										if(!INTFlag)
										{
											if(!param.getValidationType().equals("Range"))
											{
												INTFlag = true;
											}
											else
											{
												if(StringUtil.isEmpty(param.getUpperLimit()) || StringUtil.isEmpty(param.getLowerLimit()))
												{
													INTFlag = true;
												}
												else
												{
													double upperLimit = Double.parseDouble(param.getUpperLimit());
													double lowerLimit = Double.parseDouble(param.getLowerLimit());
													double modifyValue = Double.parseDouble(paramValue);
													
													if(modifyValue > upperLimit || modifyValue < lowerLimit)
													{
														INTFlag = true;
													}
												}
											}
										}
										
										oldRecipeParamInfo = param;
										break;
									}
								}
								
								RecipeParameter newRecipeParamInfo = (RecipeParameter) ObjectUtil.copyTo(oldRecipeParamInfo);
								newRecipeParamInfo.setValue(paramValue);
								newRecipeParamInfo.setOLDVALUE(oldRecipeParamInfo.getValue());

								eventInfo.setEventName("Sync-Update");

								//RecipeParam
								paramBindList.add(newRecipeParamInfo.getValue());
								paramBindList.add(newRecipeParamInfo.getOLDVALUE());
								paramBindList.add(machine.getKey().getMachineName());
								paramBindList.add(recipeName);
								paramBindList.add(recipeParamName);

								//RecipeParamHistory
								paramHistBindList.add(newRecipeParamInfo.getMachineName());
								paramHistBindList.add(newRecipeParamInfo.getRecipeName());
								paramHistBindList.add(newRecipeParamInfo.getRecipeParameterName());
								paramHistBindList.add(eventInfo.getEventTimeKey());
								paramHistBindList.add(eventInfo.getEventName());
								paramHistBindList.add(newRecipeParamInfo.getValue());
								paramHistBindList.add(newRecipeParamInfo.getValidationType());
								paramHistBindList.add(newRecipeParamInfo.getTarget());
								paramHistBindList.add(newRecipeParamInfo.getLowerLimit());
								paramHistBindList.add(newRecipeParamInfo.getUpperLimit());
								paramHistBindList.add(newRecipeParamInfo.getResult());
								paramHistBindList.add(newRecipeParamInfo.getOLDVALUE());
								paramHistBindList.add(newRecipeParamInfo.getOLDVALIDATIONTYPE());
								paramHistBindList.add(newRecipeParamInfo.getOLDTARGET());
								paramHistBindList.add(newRecipeParamInfo.getOLDLOWERLIMIT());
								paramHistBindList.add(newRecipeParamInfo.getOLDUPPERLIMIT());
								paramHistBindList.add(eventInfo.getEventUser());
								paramHistBindList.add(newRecipeParamInfo.getCheckFlag());
								
								updateParamArgList.add(paramBindList.toArray());
								updateParamHistArgList.add(paramHistBindList.toArray());
							}
								
						} catch (Exception ex)
						{
							eventLog.error(ex.getCause().getMessage());
						}
					} //ParameterFor
					if(createParamArgList.size() > 0)
					{
						MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlParamCreate, createParamArgList);
						MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlParamCreateHistory, createParamHistArgList);
					}
					if(updateParamArgList.size() > 0)
					{
						MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlParam, updateParamArgList);
						MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlParamHist, updateParamHistArgList);
					}

					String condition = " WHERE MACHINENAME = ? AND RECIPENAME = ?  ";
					Object[] bindSet = new Object[] { machineNameOfRecipe,recipeName };					
					
					List<Recipe> recipeList = ExtendedObjectProxy.getRecipeService().select(condition, bindSet);
					
					List<Recipe> updateDataList = new ArrayList<Recipe>();

					//List<Map<String, Object>> notChangedMachine =CommonUtil.getEnumDefValueByEnumName("RMSSPECIALMACHINE");
					for(Recipe dataInfo : recipeList)
					{
						//dataInfo.setRMSFlag("N"); POSMachine 변경
						
						if(dataInfo.getVERSION().equals(version))
							continue;
						
						dataInfo.setENGFlag("N");
						dataInfo.setRMSFlag("N");
						dataInfo.setVERSION(version);
						dataInfo.setActiveState(constMap.Spec_NotActive);
						if(INTFlag)
						{
							dataInfo.setINTFlag("N");
						}
						/*
						if(notChangedMachine.size()>0)
						{					
							for (Map<String, Object> enumdef : notChangedMachine)
							{
								if (dataInfo.getMachineName().equals(enumdef.get("ENUMVALUE")))
								{ 
									break;
								}
								else 
								{
									dataInfo.setINTFlag("N");
									
								}
							}
						}
						else 
						{
							dataInfo.setINTFlag("N");
						}
						*/	
						updateDataList.add(dataInfo);
					}
					eventInfo.setEventName("Sync");
					
					if(updateDataList.size() > 0)
					{
						ExtendedObjectProxy.getRecipeService().update(updateDataList);
						ExtendedObjectProxy.getRecipeService().addHistory(eventInfo, "RecipeHistory", updateDataList, LogFactory.getLog(Recipe.class));
					}
				}
			} 
			catch (Exception ex) 
			{
				eventLog.error(ex.getCause().getMessage());
			}
		}//RecipeFor
		return doc;
	}


	private MachineSpec getBaseMachine(String machineName, String unitName) throws CustomException 
	{
		String keyName = "";

		if (!StringUtil.isEmpty(unitName))
			keyName = unitName;
		else if (!StringUtil.isEmpty(machineName))
			keyName = machineName;

		MachineSpec machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(keyName);

		return machineData;
	}
	
	private Recipe createRecipe(EventInfo eventInfo, String recipeName,MachineSpec machineInfo, String version) throws CustomException 
	{
		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		
		Recipe recipeInfo = new Recipe(machineInfo.getKey().getMachineName(),recipeName);
		recipeInfo.setRecipeState("Created");
		recipeInfo.setActiveState(constMap.Spec_NotActive);
		recipeInfo.setRecipeType(machineInfo.getDetailMachineType());
		recipeInfo.setDurationUsedLimit(0);
		recipeInfo.setTimeUsedLimit(0);
		recipeInfo.setTotalTimeUsed(0);
		recipeInfo.setTimeUsed(0);
		recipeInfo.setINTFlag("N");
		recipeInfo.setMFGFlag("Y");//MFGFlag Default = Y;
		recipeInfo.setENGFlag("N");
		recipeInfo.setRMSFlag("N");
		recipeInfo.setVERSION(version);
		recipeInfo.setVersionCheckFlag("N");
		recipeInfo.setUnitCheckFlag("N");
		
		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		recipeInfo.setLastChangeTime(currentTime);
		recipeInfo.setLastModifiedTime(currentTime);
		System.out.println(currentTime);
		String  LASTCHANGETIME = new SimpleDateFormat("yyyyMMddHHmmss").format(currentTime);
		// history trace
		eventInfo.setEventName("Create");
		recipeInfo.setLastEventName(eventInfo.getEventName());
		recipeInfo.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		recipeInfo.setLastEventUser(eventInfo.getEventUser());
		recipeInfo.setLastEventComment(eventInfo.getEventComment());
		recipeInfo.setLastModifiedTime(currentTime);
		
		recipeInfo = ExtendedObjectProxy.getRecipeService().create(eventInfo,recipeInfo);
		return recipeInfo;
	}
}
