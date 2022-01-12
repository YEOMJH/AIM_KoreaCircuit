package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.TPTJRule;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TPTJRuleService extends CTORMService<TPTJRule> {

	public static Log logger = LogFactory.getLog(TPTJRuleService.class);

	private final String historyEntity = "TPTJRuleHist";

	public List<TPTJRule> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<TPTJRule> result = super.select(condition, bindSet, TPTJRule.class);

		return result;
	}

	public TPTJRule selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(TPTJRule.class, isLock, keySet);
	}

	public TPTJRule create(EventInfo eventInfo, TPTJRule dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, TPTJRule dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public TPTJRule modify(EventInfo eventInfo, TPTJRule dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public TPTJRule getTPTJRuleData(EventInfo eventInfo, String ruleName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion, String sampleProcessFlowName, String sampleProcessFlowVersion, String sampleOperationName, String sampleOperationVersion)
			throws greenFrameDBErrorSignal, CustomException
	{
		Object[] keySet = new Object[] { ruleName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion,
				sampleProcessFlowName, sampleProcessFlowVersion, sampleOperationName, sampleOperationVersion };

		TPTJRule TPTJData = new TPTJRule();

		try
		{
			TPTJData = ExtendedObjectProxy.getTPTJRuleService().selectByKey(false, keySet);
		}
		catch (Exception e)
		{
			TPTJData = null;
		}

		return TPTJData;
	}

	public List<TPTJRule> getTPTJRuleData(Lot lotData, Map<String, Object> samplePolicy) throws CustomException
	{
		List<TPTJRule> TPTJRuleDataList = new ArrayList<TPTJRule>();
		try
		{
			String condition = "factoryName =? and productSpecName =? and processFlowName = ? and processOperationName = ? and sampleProcessFlowName = ? and sampleOperationName = ? ";
			Object bindSet[] = new Object[] { lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(),
					(String) samplePolicy.get("TOPROCESSFLOWNAME"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME") };

			TPTJRuleDataList = ExtendedObjectProxy.getTPTJRuleService().select(condition, bindSet);

		}
		catch (Exception e)
		{
			TPTJRuleDataList = null;
		}
		return TPTJRuleDataList;
	}
	
	public List<TPTJRule> getTPTJRuleDataForFirstFlow(Lot lotData, Map<String, Object> samplePolicy) throws CustomException
	{
		List<TPTJRule> TPTJRuleDataList = new ArrayList<TPTJRule>();
		try
		{
			String condition = "factoryName =? and productSpecName =? and processFlowName = ? and processOperationName = ? and sampleProcessFlowName = ? and sampleOperationName = ? AND firstFlowFlag='Y' ";
			Object bindSet[] = new Object[] { lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(),
					(String) samplePolicy.get("TOPROCESSFLOWNAME"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME") };

			TPTJRuleDataList = ExtendedObjectProxy.getTPTJRuleService().select(condition, bindSet);

		}
		catch (Exception e)
		{
			TPTJRuleDataList = null;
		}
		return TPTJRuleDataList;
	}

	public List<TPTJRule> getTPTJRuleDataBySync(Lot lotData, Map<String, Object> samplePolicy) throws CustomException
	{
		List<TPTJRule> TPTJRuleDataList = new ArrayList<TPTJRule>();
		try
		{
			String condition = "factoryName =? and productSpecName =? and processFlowName = ? and processOperationName = ? and sampleProcessFlowName = ? and sampleOperationName = ? ";
			Object bindSet[] = new Object[] { lotData.getFactoryName(), lotData.getProductSpecName(), (String) samplePolicy.get("PROCESSFLOWNAME"), (String) samplePolicy.get("PROCESSOPERATIONNAME"),
					(String) samplePolicy.get("TOPROCESSFLOWNAME"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME") };

			TPTJRuleDataList = ExtendedObjectProxy.getTPTJRuleService().select(condition, bindSet);

		}
		catch (Exception e)
		{
			TPTJRuleDataList = null;
		}
		return TPTJRuleDataList;
	}

	public TPTJRule insertTPTJRule(EventInfo eventInfo, String ruleName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion, String sampleProcessFlowName, String sampleProcessFlowVersion, String sampleOperationName, String sampleOperationVersion,
			int ruleNum,String firstFlowFlag) throws greenFrameDBErrorSignal, CustomException
	{
		TPTJRule dataInfo = new TPTJRule();
		dataInfo.setRuleName(ruleName);
		dataInfo.setFactoryName(factoryName);
		dataInfo.setProductSpecName(productSpecName);
		dataInfo.setProductSpecVersion(productSpecVersion);
		dataInfo.setProcessFlowName(processFlowName);
		dataInfo.setProcessFlowVersion(processFlowVersion);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(processOperationVersion);
		dataInfo.setSampleProcessFlowName(sampleProcessFlowName);
		dataInfo.setSampleProcessFlowVersion(sampleProcessFlowVersion);
		dataInfo.setSampleOperationName(sampleOperationName);
		dataInfo.setSampleOperationVersion(sampleOperationVersion);
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setRuleNum(ruleNum);
		dataInfo.setFirstFlowFlag(firstFlowFlag);

		TPTJRule ruleData = ExtendedObjectProxy.getTPTJRuleService().create(eventInfo, dataInfo);

		return ruleData;
	}

	public void removeTPTJRule(EventInfo eventInfo, String ruleName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion, String sampleProcessFlowName, String sampleProcessFlowVersion, String sampleOperationName, String sampleOperationVersion)
			throws greenFrameDBErrorSignal, CustomException
	{
		TPTJRule dataInfo = ExtendedObjectProxy.getTPTJRuleService().getTPTJRuleData(eventInfo, ruleName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion,
				processOperationName, processOperationVersion, sampleProcessFlowName, sampleProcessFlowVersion, sampleOperationName, sampleOperationVersion);

		if (dataInfo != null)
		{
			ExtendedObjectProxy.getTPTJRuleService().remove(eventInfo, dataInfo);
		}
	}
}
