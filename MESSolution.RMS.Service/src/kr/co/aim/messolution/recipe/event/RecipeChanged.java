package kr.co.aim.messolution.recipe.event;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.extended.object.management.data.RecipeParameter;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskRecipe;
import kr.co.aim.messolution.extended.object.management.impl.POSMachineService;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.lot.event.LotProcessEnd;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;

public class RecipeChanged extends AsyncHandler {
	private static Log log = LogFactory.getLog(LotProcessEnd.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String PPCINFO = SMessageUtil.getBodyItemValue(doc, "PPCINFO", true);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "RECIPENAME", true);
		String version = SMessageUtil.getBodyItemValue(doc, "VERSION", false);
		String previousRecipeName = SMessageUtil.getBodyItemValue(doc, "PREVIOUSRECIPENAME", false);
		String recipeType = SMessageUtil.getBodyItemValue(doc, "RECIPETYPE", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String changeType = SMessageUtil.getBodyItemValue(doc, "CHANGETYPE", true);

		MachineSpec machineSpec = getBaseMachine(machineName, unitName);
		
		if(PPCINFO.equals("C"))
		{
			Recipe recipeInfo = null;
			try 
			{
				recipeInfo = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] {machineSpec.getKey().getMachineName(), recipeName});
			} 
			catch (Exception e) {}
			
			if(recipeInfo == null)
			{
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("EQPCreate", getEventUser(), getEventComment(), null, null);
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				createRecipe(eventInfo, recipeName, machineSpec, version);
			}
			
			//2020-07-14
			//Check RMSFlag request by yueke
			if(RMSFlagCheck(recipeType, machineName, recipeName, unitName))
			{
				//2020-07-15 RecipeType = 'RECIPE' Can not send ParameterRequqestByserver by dkh
				if(changeType.equals("PARAMETER")){
					RecipeParameterRequestByServer(machineSpec, recipeType, unitName, recipeName, machineName);
				}
				
			}
		}
		else if(PPCINFO.equals("D"))
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("EQPDelete", getEventUser(), getEventComment(), null, null);
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());				
			
			removeRecipe(eventInfo, recipeName, machineSpec);

		}
		else if (PPCINFO.equals("M"))
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("EQPModify", getEventUser(), getEventComment(), null, null);
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			
			//Modify in RecipeParameterReplyByServer
		    //modifyRecipe(eventInfo, recipeName, machineSpec, version);
		    
		    //2020-07-14
			//Check RMSFlag request by yueke
			if(RMSFlagCheck(recipeType, machineName, recipeName, unitName))
			{
				//2020-07-15 RecipeType = 'RECIPE' Can not send ParameterRequqestByserver by dkh
				if(changeType.equals("PARAMETER")){
					RecipeParameterRequestByServer(machineSpec, recipeType, unitName, recipeName, machineName);
				}
				
			}
		}
		else if (PPCINFO.equals("E"))
		{
			if(!recipeName.equals(previousRecipeName))
			{
				Recipe previousRecipeInfo = null;
				try 
				{
					previousRecipeInfo = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] {machineSpec.getKey().getMachineName(), previousRecipeName});
				}
				catch (Exception e) {}
				
				Recipe recipeInfo = null;
				try 
				{
					recipeInfo = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] {machineSpec.getKey().getMachineName(), recipeName});
				}
				catch (Exception e) {}
				
				if(previousRecipeInfo == null)
				{
					EventInfo eventInfo = EventInfoUtil.makeEventInfo("EQPCreate", getEventUser(), getEventComment(), null, null);
					eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
					
					if(recipeInfo == null)
					{
						createRecipe(eventInfo, recipeName, machineSpec, version);
					}
				}
				else
				{
					EventInfo eventInfo = EventInfoUtil.makeEventInfo("EQPCreate", getEventUser(), getEventComment(), null, null);
					eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
					
					if(recipeInfo == null)
					{
						createRecipe(eventInfo, recipeName, machineSpec, version);
					}
					
					eventInfo.setEventName("EQPDelete");
					eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());				
					
					removeRecipe(eventInfo, previousRecipeName, machineSpec);
				}
			}
			
			 //2020-07-14
			//Check RMSFlag request by yueke
			if(RMSFlagCheck(recipeType, machineName, previousRecipeName, unitName))
			{
				//2020-07-15 RecipeType = 'RECIPE' Can not send ParameterRequqestByserver by dkh
				if(changeType.equals("PARAMETER")){
					RecipeParameterRequestByServer(machineSpec, recipeType, unitName, recipeName, machineName);
				}
				/*try 
				{
					sendMail(machineName, previousRecipeName, recipeType, PPCINFO, unitName);
				} 
				catch (Exception e) {}*/
			}
		}
	}
	
	public void sendMail(String machineName, String previousRecipeName, String recipeType, String pPCINFO, String unitName)
	{
		if (pPCINFO.equals("C") || pPCINFO.isEmpty())
			return;
		String changeAction = pPCINFO.equals("M") ? "修改":"删除";
	    
		List<Map<String,Object>> resultList = null;
	    String sql ="SELECT EMAIL FROM USERPROFILE WHERE DEPARTMENT IN ('INT', 'ENG')";
	    
		Map<String,Object> bindMap = new HashMap<>();
	
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		}catch(Exception ex)
		{
		    log.info(ex.getCause());
		}
		
		if(resultList !=null && resultList.size()>0)
		{
			String message = "<pre>===============RecipeChanged===============</pre>";
			message += "<pre> MachineName : " + machineName + "</pre>";
			if (pPCINFO.equals("M") && recipeType.equals("U"))
			{
				message += "<pre> UnitName : " + unitName + "</pre>";
			}
			message += "<pre> 操作类型 : " + changeAction + "</pre>";
			if (pPCINFO.equals("M") && recipeType.equals("E"))
			message += "<pre> RecipeName : " + previousRecipeName + "</pre>";

			List<String> userList = CommonUtil.makeListBySqlResult(resultList, "EMAIL");
			try 
			{
				GenericServiceProxy.getMailSerivce().postMail(userList.toArray(new String[] {}),
						this.getClass().getSimpleName(), message);
			} 
			catch (Exception e)
			{
				log.error("Failed to send mail.");
				e.printStackTrace();
			}
		}	
	}
	
	private Recipe createRecipe(EventInfo eventInfo, String recipeName, MachineSpec machineInfo, String version) throws CustomException 
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
	
	private Recipe modifyRecipe(EventInfo eventInfo, String recipeName,MachineSpec machineInfo, String version) throws CustomException 
	{
		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		
		Recipe recipeInfo = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] {machineInfo.getKey().getMachineName(), recipeName});
		if(!version.equals(recipeInfo.getVERSION()))
		{
			recipeInfo.setActiveState(constMap.Spec_NotActive);
			recipeInfo.setINTFlag("N");
			recipeInfo.setMFGFlag("Y");//MFGFlag Default = Y;
			recipeInfo.setENGFlag("N");
			recipeInfo.setRMSFlag("N");
			recipeInfo.setVERSION(version);
			
			Timestamp currentTime = new Timestamp(System.currentTimeMillis());
			recipeInfo.setLastChangeTime(currentTime);
			recipeInfo.setLastModifiedTime(currentTime);
			// history trace
			recipeInfo.setLastEventName(eventInfo.getEventName());
			recipeInfo.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
			recipeInfo.setLastEventUser(eventInfo.getEventUser());
			recipeInfo.setLastEventComment(eventInfo.getEventComment());
			
			recipeInfo = ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipeInfo);
		}

		return recipeInfo;
	}
	
	private void removeRecipe(EventInfo eventInfo, String recipeName,MachineSpec machineInfo) throws CustomException 
	{
		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		Recipe recipeInfo = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] {machineInfo.getKey().getMachineName(), recipeName});
		
		recipeInfo.setRecipeState("Unapproved");
		recipeInfo.setActiveState(constMap.Spec_NotActive);
		recipeInfo.setINTFlag("N");
		recipeInfo.setMFGFlag("Y");//MFGFlag Default = Y;
		recipeInfo.setENGFlag("N");
		recipeInfo.setRMSFlag("N");
		
		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		recipeInfo.setLastChangeTime(currentTime);
		recipeInfo.setLastModifiedTime(currentTime);
		// history trace
		eventInfo.setEventName("EQPRemove");
		recipeInfo.setLastEventName(eventInfo.getEventName());
		recipeInfo.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		recipeInfo.setLastEventUser(eventInfo.getEventUser());
		recipeInfo.setLastEventComment(eventInfo.getEventComment());
		

		ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipeInfo);
	}
	
	public void RecipeParameterRequestByServer(MachineSpec machineSpec, String recipeType, String unitName, String recipeName, String mainMachineName) throws CustomException
	{
		try
		{
			Element bodyElement = new Element(SMessageUtil.Body_Tag);
			{
				Element attMachineName = new Element("MACHINENAME");
				attMachineName.setText(mainMachineName);
				bodyElement.addContent(attMachineName);
				
				Element attRecipeType = new Element("RECIPETYPE");
				attRecipeType.setText(recipeType);
				bodyElement.addContent(attRecipeType);
				
				Element attUnitName = new Element("UNITNAME");
				attUnitName.setText(unitName);
				bodyElement.addContent(attUnitName);
				
				Element attRecipeName = new Element("RECIPENAME");
				attRecipeName.setText(recipeName);
				bodyElement.addContent(attRecipeName);
			}
			
			Document doc = SMessageUtil.createXmlDocument(bodyElement, "RecipeParameterRequestByServer", "", "", mainMachineName, "");
			
			// bypass for TIB rv protocol
			String targetSubject = MESRecipeServiceProxy.getRecipeServiceUtil().getEASSubjectName(machineSpec.getKey().getMachineName());
			SMessageUtil.setItemValue(doc, "Header", "SHOPNAME", machineSpec.getFactoryName());
			SMessageUtil.setItemValue(doc, "Header", "MACHINENAME", SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true));
			SMessageUtil.setItemValue(doc, "Header", "SOURCESUBJECTNAME", GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("RMSsvr"));
			SMessageUtil.setItemValue(doc, "Header", "TARGETSUBJECTNAME", targetSubject);
			
			GenericServiceProxy.getESBServive().sendBySender(targetSubject, doc, "EISSender");
			
			//record message log 
			GenericServiceProxy.getMessageTraceService().recordMessageLog(doc, GenericServiceProxy.getConstantMap().INSERT_LOG_TYPE_SEND);
		}
		catch (CustomException ce)
		{
			throw ce;
		}
		catch (Exception ex)
		{
			//RMS-012: RMSError:Generate request message fail Machine[{0}]
			throw new CustomException("RMS-012", machineSpec.getKey().getMachineName());
		}
	}
	
	private boolean RMSFlagCheck(String recipeType, String machineName, String recipeName, String unitName ) throws CustomException
	{
		boolean checkFlag = false;
		
		List<String> recipeList = new ArrayList<>();
		
		if(recipeType.equals("U"))
		{
			List<RecipeParameter> recipeParam = null;
			
			try 
			{
				recipeParam = ExtendedObjectProxy.getRecipeParamService().select("machineName = ? AND recipeParameterName = ? AND value = ?  ", new Object[] {machineName, unitName, recipeName});
			} 
			catch (Exception e) 
			{
				log.info(e.getCause());
				return checkFlag;
			}
			
			for(RecipeParameter paramInfo : recipeParam)
			{
				recipeList.add(paramInfo.getRecipeName());
			}
		}
		else
		{
			recipeList.add(recipeName);
		}
		
		MachineSpec machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);

		if (machineData.getFactoryName().equals("TP") && StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Photo))
		{
			List<Map<String, Object>> resultList = null;
			String sql = "SELECT P.MACHINENAME, P.MACHINERECIPENAME, P.RMSFLAG FROM POSMACHINE P WHERE P.MACHINENAME = :MACHINENAME AND P.MACHINERECIPENAME  IN (:MACHINERECIPENAME) AND P.RMSFLAG = 'Y' "
					+ "UNION "
					+ "SELECT MACHINENAME, RECIPENAME AS MACHINERECIPENAME, RMSFLAG FROM CT_TPOFFSETALIGNINFO WHERE MACHINENAME = :MACHINENAME AND RECIPENAME IN (:MACHINERECIPENAME) AND RMSFLAG = 'Y'";

			Map<String, Object> bindMap = new HashMap<>();
			bindMap.put("MACHINENAME", machineName);
			bindMap.put("MACHINERECIPENAME", recipeList);
			
			try 
			{
				resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			} 
			catch (Exception ex) 
			{
				log.info(ex.getCause());
				return checkFlag;
			}
			if (resultList != null && resultList.size() > 0) {
				checkFlag = true;
			}
		}
		else if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_MaskPPA) 
				/*|| StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_MaskOrgCleaner) 
				|| StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_MaskMetalCleaner)*/)
		{
			List<ReserveMaskRecipe> reserveMaskRecipe = null;
			
			String sql = "SELECT RM.MASKLOTNAME, RM.MASKSPECNAME, RM.PROCESSFLOWNAME, RM.PROCESSOPERATIONNAME, RM.MACHINENAME, RM.RECIPENAME, RM.RMSFLAG FROM CT_RESERVEMASKRECIPE RM WHERE RM.MACHINENAME = :MACHINENAME AND RM.RECIPENAME IN (:MACHINERECIPENAME) AND RM.RMSFLAG = 'Y' ";

			Map<String, Object> bindMap = new HashMap<>();
			bindMap.put("MACHINENAME", machineName);
			bindMap.put("MACHINERECIPENAME", recipeList);
			
			try 
			{
				reserveMaskRecipe = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			} 
			catch (Exception ex) 
			{
				log.info(ex.getCause());
				return checkFlag;
			}
			
			if (reserveMaskRecipe != null && reserveMaskRecipe.size() > 0) {
				checkFlag = true;
			}
		}
		else if(StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_MaskTension))
		{
			List<Map<String, Object>> resultList = null;
			
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT MACHINENAME, MACHINERECIPENAME, RMSFLAG FROM POSABNORMALMACHINE WHERE MACHINENAME  = :MACHINENAME AND MACHINERECIPENAME IN (:MACHINERECIPENAME) AND RMSFLAG = 'Y'");
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("MACHINENAME", machineName);
			bindMap.put("MACHINERECIPENAME", recipeList);
			
			try 
			{
				resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
			} 
			catch (Exception ex) {
				log.info(ex.getCause());
				return checkFlag;
			}
			if (resultList != null && resultList.size() > 0) {
				checkFlag = true;
			}
		}		
		else
		{
			List<Map<String, Object>> resultList = null;
			String sql = "SELECT P.MACHINENAME, P.MACHINERECIPENAME, P.RMSFLAG FROM POSMACHINE P WHERE P.MACHINENAME = :MACHINENAME AND (P.MACHINERECIPENAME IN (:MACHINERECIPENAME) OR P.ECRECIPENAME IN (:MACHINERECIPENAME)) AND P.RMSFLAG = 'Y'";

			Map<String, Object> bindMap = new HashMap<>();
			bindMap.put("MACHINENAME", machineName);
			bindMap.put("MACHINERECIPENAME", recipeList);
			
			try 
			{
				resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			} 
			catch (Exception ex) {
				log.info(ex.getCause());
				return checkFlag;
			}
			if (resultList != null && resultList.size() > 0) {
				checkFlag = true;
			}
		}
		
		return checkFlag;
	}
}
