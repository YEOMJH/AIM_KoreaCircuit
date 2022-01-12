package kr.co.aim.messolution.recipe.event;

import java.sql.Timestamp;
import java.util.ArrayList;
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
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EMailInterface;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;

public class RecipeParameterReplyByServer extends AsyncHandler 
{
	private static Log log = LogFactory.getLog(RecipeParameterReplyByServer.class);
	
	@Override
	public void doWorks(Document doc) throws CustomException 
	{
		String mainMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME",true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "RECIPENAME", true);
		String version = SMessageUtil.getBodyItemValue(doc, "VERSION", false);
		String recipeType = SMessageUtil.getBodyItemValue(doc, "RECIPETYPE", true);
		
		MachineSpec machineName = getBaseMachine(mainMachineName, unitName);
		
		ConstantMap constMap = GenericServiceProxy.getConstantMap();

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(),getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		
		String sqlParamCreate = "INSERT INTO CT_RECIPEPARAMETER(MACHINENAME, RECIPENAME, RECIPEPARAMETERNAME, VALUE) VALUES(?, ?, ?, ?)";
		String sqlParamCreateHistory = "INSERT INTO CT_RECIPEPARAMETERHISTORY(MACHINENAME, RECIPENAME, RECIPEPARAMETERNAME, VALUE, TIMEKEY, EVENTNAME, EVENTUSER) VALUES(?, ?, ?, ?, ?, ?, ?)";
		
		StringBuffer sqlBufferUpdate = new StringBuffer("").append(" UPDATE CT_RECIPEPARAMETER ").append("   SET  VALUE = ?, ").append("   OLDVALUE = ? ").append("  WHERE ROWNUM=1 ")
				.append("	 AND MACHINENAME= ? ").append("    AND RECIPENAME = ? ").append("    AND RECIPEPARAMETERNAME = ? ");
		StringBuffer sqlBufferHistory = new StringBuffer("").append(" INSERT INTO CT_RECIPEPARAMETERHISTORY VALUES (?, ?,  ").append("   ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");
		
		//String sqlParamRemove = "DELETE FROM CT_RECIPEPARAMETER WHERE MACHINENAME = ? AND RECIPENAME = ? AND RECIPEPARAMETERNAME = ?";

		List<RecipeParameter> mesParamList = null;
		//List<RecipeParameter> mesRemoveParamList = new ArrayList<RecipeParameter>();
		
		List<Object[]> updateParamArgList = new ArrayList<Object[]>();
		List<Object[]> updateParamHistArgList = new ArrayList<Object[]>();
		List<Object[]> createParamArgList = new ArrayList<Object[]>();
		List<Object[]> createParamHistArgList = new ArrayList<Object[]>();
		
		try
		{ 
			mesParamList = ExtendedObjectProxy.getRecipeParamService().select("machineName = ? AND recipeName = ?  ", new Object[] {machineName.getKey().getMachineName(), recipeName});
		}
		catch (Exception e)
		{}
		int mesParameterQty=0;
		int eqpParameterQty=0;
		try 
		{
			mesParameterQty=mesParamList.size();
		} catch (Exception e) 
		{
			mesParameterQty=0;
		}
		try 
		{
			eqpParameterQty=SMessageUtil.getBodySequenceItemList(doc, "PARAMETERLIST", false).size();
		} catch (Exception e) 
		{
			eqpParameterQty=0;
		}
		
		boolean activeFlag = true;
		int checkQty = 0;
		int rangeEditQty = 0;
		boolean mailFlag = false;
		boolean recipeRangeEditNotChangeFlag = false;
		
		if (CommonUtil.getEnumDefValueStringByEnumNameAndEnumValue("RecipeRangeEditNotChangeFlag", unitName).equals(unitName))
			recipeRangeEditNotChangeFlag = true;
		
		String emailInfo = "";
		
		for (Element eleParameter : SMessageUtil.getBodySequenceItemList(doc, "PARAMETERLIST", false)) 
		{
			String recipeParamName = SMessageUtil.getChildText(eleParameter, "PARAMETERNAME", true);
			String paramValue = SMessageUtil.getChildText(eleParameter,"VALUE", true);

			boolean isFound = false;
			
			if(mesParamList != null)
			{
				for(RecipeParameter mesRecipeParam : mesParamList)
				{
					if(mesRecipeParam.getRecipeParameterName().equals(recipeParamName))
					{
						List<Object> paramBindList = new ArrayList<Object>();
						List<Object> paramHistBindList = new ArrayList<Object>();	
						
						isFound = true;
						boolean parameterMailFlag = false;
						String spec = mesRecipeParam.getTarget();
						
						if(paramValue.equals(mesRecipeParam.getValue()))
						{
							break;
						}
						
						//CheckFlag为N时决定是否修改Recipe激活状态
						if (mesRecipeParam.getCheckFlag().isEmpty() || !mesRecipeParam.getCheckFlag().equals("N"))
						{
							if(!mesRecipeParam.getValidationType().isEmpty())
							{
								mailFlag = true;
								parameterMailFlag = true;
								activeFlag = false;
								checkQty ++;
							}
							
							if (mesRecipeParam.getValidationType().equals("Range"))
							{
								double upperLimit = Double.parseDouble(mesRecipeParam.getUpperLimit());
					    		double lowerLimit = Double.parseDouble(mesRecipeParam.getLowerLimit());
					    		spec = mesRecipeParam.getLowerLimit() + "-" + mesRecipeParam.getUpperLimit();
					    		
					    		if(Double.parseDouble(paramValue) <= upperLimit && Double.parseDouble(paramValue) >= lowerLimit)
					    		{
					    			rangeEditQty ++;
					    		}
							}
						}
						else 
						{
							mailFlag = true;
							parameterMailFlag = true;
							spec = mesRecipeParam.getLowerLimit() + "-" + mesRecipeParam.getUpperLimit();
						}
								
						String oldValue = mesRecipeParam.getValue();
						
						if (parameterMailFlag) 
						{
							String ttType = "";
							if(mesRecipeParam.getValidationType().isEmpty())
								ttType = "高频参数";
							else
								ttType = mesRecipeParam.getValidationType();
							
							emailInfo += "<pre> Modify Parameter：" + mesRecipeParam.getRecipeParameterName() + "  ValidationType： " + ttType + "  规格： " + spec + "  OldValue："+ oldValue + "  NewValue："+ paramValue +"</pre>";
						}
						
						eventInfo.setEventName("EQPModify");
						
						//RecipeParam
						paramBindList.add(paramValue);
						paramBindList.add(mesRecipeParam.getValue());
						paramBindList.add(machineName.getKey().getMachineName());
						paramBindList.add(recipeName);
						paramBindList.add(recipeParamName);
	
						//RecipeParamHistory
						paramHistBindList.add(mesRecipeParam.getMachineName());
						paramHistBindList.add(mesRecipeParam.getRecipeName());
						paramHistBindList.add(mesRecipeParam.getRecipeParameterName());
						paramHistBindList.add(eventInfo.getEventTimeKey());
						paramHistBindList.add(eventInfo.getEventName());
						paramHistBindList.add(paramValue);
						paramHistBindList.add(mesRecipeParam.getValidationType());
						paramHistBindList.add(mesRecipeParam.getTarget());
						paramHistBindList.add(mesRecipeParam.getLowerLimit());
						paramHistBindList.add(mesRecipeParam.getUpperLimit());
						paramHistBindList.add(mesRecipeParam.getResult());
						paramHistBindList.add(oldValue);
						paramHistBindList.add(mesRecipeParam.getValidationType());
						paramHistBindList.add(mesRecipeParam.getTarget());
						paramHistBindList.add(mesRecipeParam.getLowerLimit());
						paramHistBindList.add(mesRecipeParam.getUpperLimit());
						paramHistBindList.add(eventInfo.getEventUser());
						paramHistBindList.add(mesRecipeParam.getCheckFlag());
						
						updateParamArgList.add(paramBindList.toArray());
						updateParamHistArgList.add(paramHistBindList.toArray());
						break;
					}
				}
			}
			
			if(!isFound)
			{
				eventInfo.setEventName("EQPCreate");
				activeFlag = false;
				emailInfo += "<pre> Create New Parameter : " + recipeParamName + "   Value: " + paramValue +"</pre>";
				
				List<Object> paramBindList = new ArrayList<Object>();
				List<Object> paramHistBindList = new ArrayList<Object>();
				
				//RecipeParam
				paramBindList.add(machineName.getKey().getMachineName());
				paramBindList.add(recipeName);
				paramBindList.add(recipeParamName);
				paramBindList.add(paramValue);

				//RecipeParamHistory
				paramHistBindList.add(machineName.getKey().getMachineName());
				paramHistBindList.add(recipeName);
				paramHistBindList.add(recipeParamName);
				paramHistBindList.add(paramValue);
				paramHistBindList.add(eventInfo.getEventTimeKey());
				paramHistBindList.add(eventInfo.getEventName());
				paramHistBindList.add(eventInfo.getEventUser());
				
				createParamArgList.add(paramBindList.toArray());
				createParamHistArgList.add(paramHistBindList.toArray());
			}
		}
		/*
		for (RecipeParameter mesRecipeParam : mesParamList) 
		{
			boolean isFound = false;
			
			for(Element eleParameter : SMessageUtil.getBodySequenceItemList(doc, "PARAMETERLIST", false))
			{
				String recipeParamName = SMessageUtil.getChildText(eleParameter, "PARAMETERNAME", true);
				
				if(mesRecipeParam.getRecipeParameterName().equals(recipeParamName))
				{
					isFound = true;
					break;
				}
			}
			
			if(!isFound)
			{
				mesRemoveParamList.add(mesRecipeParam);
			}
		}
		*/
		boolean updateFlag = false;
		
		if(createParamArgList.size() > 0)
		{
			MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlParamCreate, createParamArgList);
			MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlParamCreateHistory, createParamHistArgList);
			updateFlag = true;
		}
		if(updateParamArgList.size() > 0)
		{
			MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlBufferUpdate.toString(), updateParamArgList);
			MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlBufferHistory.toString(), updateParamHistArgList);
			updateFlag = true;
		}
		/*
		if(mesRemoveParamList.size() > 0)
		{
			//eventInfo.setEventName("EQPRemove");
			//ExtendedObjectProxy.getRecipeParamService().remove(eventInfo, mesRemoveParamList);
			updateFlag = true;
		}
		*/
		if(updateFlag || mesParameterQty != eqpParameterQty)
		{
			eventInfo.setEventName("ParameterChange");
			//RecipeCheckFlag 为Y时，checkFlag为N也会将Recipe置为非激活状态
			if (!activeFlag || CommonUtil.getEnumDefValueStringByEnumName("RecipeCheckFlag").equals("Y"))
			{
				if (!(recipeRangeEditNotChangeFlag && checkQty == rangeEditQty))
				{
					changeRecipeFlag(eventInfo, recipeName, machineName);
				}
				else 
				{
					changeENGRecipeFlag(eventInfo, recipeName, machineName);
				}
			}
		}
		
		if (mailFlag)
		{
			List<String> recipeInfo = new ArrayList<String>();
			//开关RecipeChangeEmail为Y时，发邮件
			if (CommonUtil.getEnumDefValueStringByEnumName("RecipeChangeEmail").equals("Y"))
			{
				if (recipeType.equals("U"))
				{
					List<RecipeParameter> recipeList = ExtendedObjectProxy.getRecipeParamService().select("RECIPEPARAMETERNAME = ? AND VALUE = ?  ", new Object[] {unitName, recipeName});
					if (recipeList != null && !recipeList.isEmpty())
					{
						for (RecipeParameter recipeLists:recipeList)
						{
							recipeInfo.add(recipeLists.getRecipeName());
						}
					}
				}
				sendMail(machineName.getKey().getMachineName(), recipeName,mesParameterQty,eqpParameterQty,emailInfo,recipeInfo);
			}
		}
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
	
	private Recipe changeRecipeFlag(EventInfo eventInfo, String recipeName,MachineSpec machineInfo) throws CustomException 
	{
		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		
		Recipe recipeInfo = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] {machineInfo.getKey().getMachineName(), recipeName});
		recipeInfo.setActiveState(constMap.Spec_NotActive);
		recipeInfo.setINTFlag("N");
		recipeInfo.setMFGFlag("Y");
		recipeInfo.setENGFlag("N");
		recipeInfo.setRMSFlag("N");
		
		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		recipeInfo.setLastChangeTime(currentTime);
		recipeInfo.setLastModifiedTime(currentTime);
		// history trace
		recipeInfo.setLastEventName(eventInfo.getEventName());
		recipeInfo.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		recipeInfo.setLastEventUser(eventInfo.getEventUser());
		recipeInfo.setLastEventComment(eventInfo.getEventComment());
		
		recipeInfo = ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipeInfo);
		return recipeInfo;
	}
	
	private Recipe changeENGRecipeFlag(EventInfo eventInfo, String recipeName,MachineSpec machineInfo) throws CustomException 
	{
		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		
		Recipe recipeInfo = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] {machineInfo.getKey().getMachineName(), recipeName});
		recipeInfo.setActiveState(constMap.Spec_NotActive);
		recipeInfo.setMFGFlag("Y");
		recipeInfo.setENGFlag("N");
		recipeInfo.setRMSFlag("N");
		
		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		recipeInfo.setLastChangeTime(currentTime);
		recipeInfo.setLastModifiedTime(currentTime);
		// history trace
		recipeInfo.setLastEventName(eventInfo.getEventName());
		recipeInfo.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		recipeInfo.setLastEventUser(eventInfo.getEventUser());
		recipeInfo.setLastEventComment(eventInfo.getEventComment());
		
		recipeInfo = ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipeInfo);
		return recipeInfo;
	}
	
	public void sendMail(String machineName, String recipeName,int mesParameterQty,int eqpParameterQty, String emailInfo, List<String> recipeInfo)
	{
		List<Map<String,Object>> resultList = null;
		List<String> emailList = new ArrayList<String>();
		StringBuffer userList = new StringBuffer();
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT MS.RMSDEPARTMENT, MS.FACTORYNAME FROM MACHINESPEC MS WHERE MS.MACHINENAME = :MACHINENAME");
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("MACHINENAME", machineName);
	
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		}
		catch(Exception ex)
		{
		    log.info(ex.getCause());
		}
		
		try 
		{
			if (resultList.size() > 0) 
			{
				String departmentAll = resultList.get(0).get("RMSDEPARTMENT").toString();

				List<String> department =  CommonUtil.splitStringDistinct(",",departmentAll);

				StringBuffer sql1 = new StringBuffer();
				sql1.append(
						"SELECT A.* FROM CT_ALARMUSERGROUP A,USERPROFILE U WHERE A.ALARMGROUPNAME = 'RecipeChanged' AND A.USERID = U.USERID AND A.DEPARTMENT = :DEPARTMENT");
				Map<String, Object> args1 = new HashMap<String, Object>();

				for (String department1 : department) 
				{
					if (department1.equals("OTHER-INT-PIE"))
						department1 = department1 + "-" + resultList.get(0).get("FACTORYNAME").toString();
					
					args1.put("DEPARTMENT", department1);
					List<Map<String, Object>> sqlResult1 = GenericServiceProxy.getSqlMesTemplate()
							.queryForList(sql1.toString(), args1);
					
					if (sqlResult1.size() > 0) 
					{
						for (Map<String, Object> user : sqlResult1)
						{
							String eMail = ConvertUtil.getMapValueByName(user, "EMAIL");
							emailList.add(eMail);
							userList.append(ConvertUtil.getMapValueByName(user, "USERID") + ",");
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			log.error("Not Found the Department of "+ machineName);
			log.error("Failed to send mail.");
		}
		
		StringBuffer message = new StringBuffer();
		if(emailList !=null && emailList.size()>0)
		{
			message.append("<pre>===============RecipeChanged===============</pre>");
			message.append("<pre> MachineName : " + machineName + "</pre>");
			if (recipeInfo.size()>0)
				message.append("<pre> MES PPID List : " + recipeInfo.toString() + "</pre>");
			
			message.append("<pre> RecipeName : " + recipeName + "</pre>");
			message.append("<pre> MESParameterQty : " + mesParameterQty + "</pre>");
			message.append("<pre> EQPParameterQty : " + eqpParameterQty + "</pre>");
			message.append(emailInfo);
		 
			try
			{
				EMailInterface ei = new EMailInterface("mail.visionox.com", "25", "1000", "1000");
				ei.postMail(emailList,  " RecipeChanged ", message.toString(), "V3MES", "V3MES@visionox.com", "V3MES", "vis@2019");
			}
			catch (Exception e)
			{
				log.error("Failed to send mail.");
			}
		}	
		if (userList.length() > 0)
		{
			sendToEM(userList,message.toString());
		}
	}


	private void sendToEM(StringBuffer userList, String messageInfo) 
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("RecipeChanged", "MES", "", "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		try
		{																
			String[] userGroup = userList.toString().split(",");				
			String title = " RecipeChanged ";
			String detailtitle = "${}CIM系统消息通知";
			String url = "";
			
			StringBuffer info = new StringBuffer();
			info.append("<pre>======================= RecipeChanged =======================</pre>");
			info.append(messageInfo);
			info.append("<pre>=============================End=============================</pre>");			
			
			String message = info.toString();
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userGroup, title, detailtitle, message, url);
			//log.info("eMobile Send Success!");	
			
			StringBuffer weChatInfo = new StringBuffer();
			weChatInfo.append("<pre>==== RecipeChanged ====</pre>");
			weChatInfo.append(messageInfo);
			weChatInfo.append("<pre>====AlarmInfoEnd====</pre>");
			
			String weChatMessage = weChatInfo.toString();
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userGroup, title, detailtitle, weChatMessage, "");
			//log.info("WeChat Send Success!");	
		}
		catch (Exception e)
		{
			log.info("eMobile or WeChat Send Error : " + e.getCause());	
		}
	}
}
