package kr.co.aim.messolution.datacollection.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kr.co.aim.messolution.datacollection.service.DataCollectionServiceImpl;
import kr.co.aim.messolution.datacollection.service.DataCollectionServiceUtil;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import org.jdom.Document;
import org.jdom.Element;

public class SPCPolicyCopy extends SyncHandler {
 
	@Override
	public Object doWorks(Document doc)throws CustomException  
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("SPCPolicyCopy", getEventUser(), getEventComment(), null, null);
 
		String factoryName 			= SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName 			= SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String copyProductSpecName 			= SMessageUtil.getBodyItemValue(doc, "COPYPRODUCTSPECNAME", true);

		try 
		{
			for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "POLICYLIST", false))
			{
				String oriProductSpecName = SMessageUtil.getChildText(eledur, "ORIPRODUCTSPECNAME", false);
				String processOperationName = SMessageUtil.getChildText(eledur, "PROCESSOPERATIONNAME", false);
				String machineName = SMessageUtil.getChildText(eledur, "MACHINENAME", false);
				String dcSpecName = SMessageUtil.getChildText(eledur, "DCSPECNAME", false);
				
				Boolean Flag = DataCollectionServiceUtil.insertPolicy(factoryName, copyProductSpecName, processOperationName, machineName, dcSpecName);
				
				if(Flag)
				{
					List<Map<String, Object>> spcControlSpecNameList = this.getSPCControlSpec(oriProductSpecName, processOperationName, machineName, dcSpecName);

					if(spcControlSpecNameList.size() > 0)
					{
						for(Map<String, Object> row : spcControlSpecNameList)
						{
							String spcControlSpecName =  CommonUtil.getValue(row, "SPCCONTROLSPECNAME");
							String spcDcSpecName =  CommonUtil.getValue(row, "DCSPECNAME");
							String spcControlGroupType =  CommonUtil.getValue(row, "CONTROLGROUPTYPE");
							String spcTotalControlCount =  CommonUtil.getValue(row, "TOTALCONTROLCOUNT");
							String spcFactoryName =  CommonUtil.getValue(row, "FACTORYNAME");
							String spcProductSpecVersion =  CommonUtil.getValue(row, "PRODUCTSPECVERSION");
							String spcProcessFlowName =  CommonUtil.getValue(row, "PROCESSFLOWNAME");
							String spcProcessFlowVersion =  CommonUtil.getValue(row, "PROCESSFLOWVERSION");
							String spcProcessOperationName =  CommonUtil.getValue(row, "PROCESSOPERATIONNAME");
							String spcProcessOperationVersion =  CommonUtil.getValue(row, "PROCESSOPERATIONVERSION");
							String spcMachineName =  CommonUtil.getValue(row, "MACHINENAME");
							String spcMachineRecipeName =  CommonUtil.getValue(row, "MACHINERECIPENAME");
							String spcReferenceponame =  CommonUtil.getValue(row, "REFERENCEPONAME");
							String spcReferencepoversion =  CommonUtil.getValue(row, "REFERENCEPOVERSION");
							String spcReferenceMachineFlag =  CommonUtil.getValue(row, "REFERENCEMACHINEFLAG");
							String spcReferenceMachineRecipeFlag =  CommonUtil.getValue(row, "REFERENCEMACHINERECIPEFLAG");
							
							String SequenceId = DataCollectionServiceUtil.getNextSequenceId();
							
							String newSpcControlSpecName = copyProductSpecName + "-" + spcProcessOperationName + "-" + spcMachineName + "-" + SequenceId;
							
							String eventUser= this.getEventUser();
							
							DataCollectionServiceImpl.insertSPCControlSpec(spcControlSpecName, 
																			newSpcControlSpecName,
																			spcDcSpecName, 
																			spcControlGroupType, 
																			spcTotalControlCount, 
																			spcFactoryName, 
																			copyProductSpecName, 
																			spcProductSpecVersion, 
																			spcProcessFlowName, 
																			spcProcessFlowVersion, 
																			spcProcessOperationName, 
																			spcProcessOperationVersion, 
																			spcMachineName, 
																			spcMachineRecipeName, 
																			spcReferenceponame, 
																			spcReferencepoversion, 
																			spcReferenceMachineFlag, 
																			spcReferenceMachineRecipeFlag,
																			eventUser);
							
							List<Map<String, Object>> spcControlSpecItemList = this.getSPCControlSpecItem(spcControlSpecName);
							if(spcControlSpecItemList.size() > 0)
							{
								DataCollectionServiceImpl.insertSPCControlSpecItem(spcControlSpecItemList,newSpcControlSpecName,eventUser);
							}
							else
							{
								eventLog.info("Data has not been found in MES_SPCCONTROLSPECITEM");
							}
							
							List<Map<String, Object>> spcControlSpecChartList = this.getSPCControlSpecChart(spcControlSpecName);
							if(spcControlSpecChartList.size() > 0)
							{
								DataCollectionServiceImpl.insertSPCControlSpecChart(spcControlSpecChartList,newSpcControlSpecName,eventUser);
							}
							else
							{
								eventLog.info("Data has not been found in MES_SPCCONTROLSPECCHART");
							}
	
							List<Map<String, Object>> spcControlSpecRuleList = this.getSPCControlSpecRule(spcControlSpecName);
							if(spcControlSpecRuleList.size() > 0)
							{
								DataCollectionServiceImpl.insertSPCControlSpecRule(spcControlSpecRuleList,newSpcControlSpecName,eventUser);
							}
							else
							{
								eventLog.info("Data has not been found in MES_SPCCONTROLSPECRULE");
							}
							
							List<Map<String, Object>> spcControlSpecRuleGroupList = this.getSPCControlSpecRuleGroup(spcControlSpecName);
							
							if(spcControlSpecRuleGroupList.size() > 0)
							{
								DataCollectionServiceImpl.insertSPCControlSpecRuleGroup(spcControlSpecRuleGroupList,newSpcControlSpecName,eventUser);
							}
							else
							{
								eventLog.info("Data has not been found in MES_SPCCONTROLSPECRULEGROUP");
							}
							
							List<Map<String, Object>> SPCControlSpecCapbility = this.getSPCControlSpecCapbility(spcControlSpecName);
							
							if(SPCControlSpecCapbility.size() > 0)
							{
								DataCollectionServiceImpl.insertSPCControlSpecCapbility(SPCControlSpecCapbility,newSpcControlSpecName);
							}
							else
							{
								eventLog.info("Data has not been found in MES_SPCCONTROLSPECCAPABILITY");
							}
						}
					}
					else
					{
						eventLog.info("Data has not been found in MES_SPCCONTROLSPEC");
					}
				}
				else
				{
					eventLog.info("Insert SPC Policy Table Fail");
				}
			}
		}
		catch (Exception e)
		{
			eventLog.error(e);
		}
		
		doc = SMessageUtil.addItemToBody(doc, "SPCPolicyCopyRelust", "Success");
		
		return doc;
	}
	
	
	public static List<Map<String, Object>> getSPCControlSpec(String productSpecName, String processOperationName, String machineName, String dcSpecName) throws CustomException
	{
		String sql = "SELECT SPCCONTROLSPECNAME,DCSPECNAME,CONTROLGROUPTYPE,TOTALCONTROLCOUNT,FACTORYNAME,PRODUCTSPECNAME, " +
						" PRODUCTSPECVERSION,PROCESSFLOWNAME,PROCESSFLOWVERSION,PROCESSOPERATIONNAME,PROCESSOPERATIONVERSION, " +
						" MACHINENAME,MACHINERECIPENAME,REFERENCEPONAME,REFERENCEPOVERSION,REFERENCEMACHINEFLAG,REFERENCEMACHINERECIPEFLAG "+
						" FROM MES_SPCCONTROLSPEC WHERE PRODUCTSPECNAME= :PRODUCTSPECNAME AND PROCESSOPERATIONNAME= :PROCESSOPERATIONNAME " +
						" AND MACHINENAME=:MACHINENAME AND DCSPECNAME= :DCSPECNAME ";
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("PRODUCTSPECNAME", productSpecName);
		bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		bindMap.put("MACHINENAME", machineName);
		bindMap.put("DCSPECNAME", dcSpecName);

		List<Map<String, Object>> spcControlSpecNameList= GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
		
		return spcControlSpecNameList;

	}
	
	public static List<Map<String, Object>> getSPCControlSpecItem(String spcControlSpecName) throws CustomException
	{
		String sql = "SELECT SPCCONTROLSPECNAME,ITEMNAME,CONTROLCHARTTYPE,DATATYPE,DERIVEDTYPE,DERIVEDEXPRESSION,CLCREATIONCYCLE," +
							" CLAUTOUPDATEFLAG,LASTCLCREATEDTIME,SPECLIMITTYPE,TARGET,UPPERSPECLIMIT,LOWERSPECLIMIT,UPPERSCREENLIMIT," +
							" LOWERSCREENLIMIT,SCREENLIMITREMOVEOPTION,OOCREMOVEOPTION " +
							" FROM MES_SPCCONTROLSPECITEM WHERE SPCCONTROLSPECNAME = :SPCCONTROLSPECNAME ";
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("SPCCONTROLSPECNAME", spcControlSpecName);
			
		List<Map<String, Object>> SPCControlSpecItemList=GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
		
		return SPCControlSpecItemList;
	}
	
	public static List<Map<String, Object>> getSPCControlSpecChart(String spcControlSpecName) throws CustomException
	{
		String sql = "SELECT SPCCONTROLSPECNAME,ITEMNAME,CHARTNAME,DESCRIPTION,CENTERLINE,UPPERCONTROLLIMIT,LOWERCONTROLLIMIT, " +
							"UPPERQUALITYLIMIT,LOWERQUALITYLIMIT FROM MES_SPCCONTROLSPECCHART WHERE SPCCONTROLSPECNAME = :SPCCONTROLSPECNAME ";
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("SPCCONTROLSPECNAME", spcControlSpecName);
		
		List<Map<String, Object>> SPCControlSpecChartList=GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
		
		return SPCControlSpecChartList;

	}
	
	public static List<Map<String, Object>> getSPCControlSpecRule(String spcControlSpecName) throws CustomException
	{
		String sql = "SELECT SPCCONTROLSPECNAME,ITEMNAME,CHARTNAME,SPCCONTROLRULENAME,UPDATETIME " +
						" FROM MES_SPCCONTROLSPECRULE WHERE SPCCONTROLSPECNAME = :SPCCONTROLSPECNAME ";
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("SPCCONTROLSPECNAME", spcControlSpecName);
		
		List<Map<String, Object>> SPCControlSpecRuleList=GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
		
		return SPCControlSpecRuleList;
	}
	
	public static List<Map<String, Object>> getSPCControlSpecRuleGroup(String spcControlSpecName) throws CustomException
	{
		String sql = "SELECT SPCCONTROLSPECNAME,ITEMNAME,CHARTNAME,SPCCONTROLRULEGROUPNAME,UPDATETIME " +
						" FROM MES_SPCCONTROLSPECRULEGROUP WHERE SPCCONTROLSPECNAME = :SPCCONTROLSPECNAME ";
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("SPCCONTROLSPECNAME", spcControlSpecName);

		List<Map<String, Object>> SPCControlSpecRuleGroupList=GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
		
		return SPCControlSpecRuleGroupList;
	}
	
	public static List<Map<String, Object>> getSPCControlSpecCapbility(String spcControlSpecName) throws CustomException
	{
		String sql = "SELECT SPCCONTROLSPECNAME,ITEMNAME,CREATIONCYCLE,LASTCREATEDTIME,UPDATETIME " +
						" FROM MES_SPCCONTROLSPECCAPABILITY WHERE SPCCONTROLSPECNAME = :SPCCONTROLSPECNAME ";
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("SPCCONTROLSPECNAME", spcControlSpecName);
				
		List<Map<String, Object>> SPCControlSpecCapbilityList=GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().queryForList(sql, bindMap);
		
		return SPCControlSpecCapbilityList;
	}
}