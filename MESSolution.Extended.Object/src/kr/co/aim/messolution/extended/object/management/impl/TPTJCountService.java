package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.TPTJCount;
import kr.co.aim.messolution.extended.object.management.data.TPTJRule;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TPTJCountService extends CTORMService<TPTJCount> {

	private static Log log = LogFactory.getLog(TPTJCountService.class);
	public static Log logger = LogFactory.getLog(TPTJCountService.class);

	private final String historyEntity = "";

	public List<TPTJCount> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<TPTJCount> result = super.select(condition, bindSet, TPTJCount.class);

		return result;
	}

	public TPTJCount selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(TPTJCount.class, isLock, keySet);
	}

	public TPTJCount create(EventInfo eventInfo, TPTJCount dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, TPTJCount dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public TPTJCount modify(EventInfo eventInfo, TPTJCount dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	
	public TPTJCount getDataInfo(String lotName,String ruleName,Number ruleNum,String factoryName,String productSpecName,String productSpecVersion,String processflowName,String processflowVersion)
	{
		TPTJCount dataInfo = null;

		try
		{
			dataInfo = this.selectByKey(false, new Object[] { lotName, ruleName, ruleNum, factoryName, productSpecName, productSpecVersion, processflowName, processflowVersion });
		}
		catch (Exception ex)
		{
			log.info(ex.toString());
			return null;
		}

		return dataInfo;
	}

	public List<TPTJCount> getTPTJCountDataList(String lotName,String factoryName, String productSpecName, String processflowname)
	{
		String condition = "lotName = ? and factoryName =? and productSpecName =? and processFlowName = ? and deleteFlag = 'N' and completeFlag is null";
		Object bindSet[] = new Object[] { lotName, factoryName, productSpecName, processflowname };

		List<TPTJCount> TPTJCountDataList = new ArrayList<TPTJCount>();

		try
		{
			TPTJCountDataList = ExtendedObjectProxy.getTPTJCountService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			TPTJCountDataList = null;
		}

		return TPTJCountDataList;
	}

	public List<TPTJCount> getTPTJCountDataList(Lot lotData)
	{
		String condition = "lotName = ? and factoryName =? and productSpecName =? and processFlowName = ? and deleteFlag = 'Y' and completeFlag is null";
		Object bindSet[] = new Object[] { lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName() };

		List<TPTJCount> TPTJCountDataList = new ArrayList<TPTJCount>();

		try
		{
			TPTJCountDataList = ExtendedObjectProxy.getTPTJCountService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			TPTJCountDataList = null;
		}

		return TPTJCountDataList;
	}

	public void insertTPTJCount(EventInfo eventInfo, Lot lotData, TPTJRule TPTJRuleData) throws CustomException
	{
		try
		{
			log.info("Insert TPTJ Count Data : TrackOut");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			String condition = "factoryName =? and productSpecName =? and processFlowName = ? and ruleName = ? and ruleNum = ? ";
			Object bindSet[] = new Object[] { lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), TPTJRuleData.getRuleName(), TPTJRuleData.getRuleNum() };

			List<TPTJRule> TPTJRuleDataList = new ArrayList<TPTJRule>();

			try
			{
				TPTJRuleDataList = ExtendedObjectProxy.getTPTJRuleService().select(condition, bindSet);
			}
			catch (Exception e)
			{
				TPTJRuleDataList = null;
			}

			int operationQty = 0;

			if (TPTJRuleDataList != null && TPTJRuleDataList.size() > 0)
			{
				operationQty = TPTJRuleDataList.size();
			}

			TPTJCount tptjCountData = new TPTJCount();
			tptjCountData.setLotName(lotData.getKey().getLotName());
			tptjCountData.setRuleName(TPTJRuleData.getRuleName());
			tptjCountData.setRuleNum(TPTJRuleData.getRuleNum());
			tptjCountData.setFactoryName(lotData.getFactoryName());
			tptjCountData.setProductSpecName(lotData.getProductSpecName());
			tptjCountData.setProductSpecVersion(lotData.getProductSpecVersion());
			tptjCountData.setProcessFlowName(lotData.getProcessFlowName());
			tptjCountData.setProcessFlowVersion(lotData.getProcessFlowVersion());
			tptjCountData.setOperationQty(String.valueOf(operationQty));
			tptjCountData.setProcessCount("1");
			tptjCountData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			tptjCountData.setDeleteFlag("N");

			tptjCountData = ExtendedObjectProxy.getTPTJCountService().create(eventInfo, tptjCountData);
		}
		catch (Exception e)
		{
			log.info("Fail Insert TPTJCountData : ", e);
		}
	}

	public void syncTPTJCount(EventInfo eventInfo, Lot lotData, TPTJRule TPTJRuleData) throws CustomException
	{
		try
		{
			log.info("Update TPTJ Count Data : TrackOut");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			String condition = "lotName = ? and factoryName =? and productSpecName =? and processFlowName = ? and ruleName = ? and ruleNum = ? ";
			Object bindSet[] = new Object[] { lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), TPTJRuleData.getRuleName(),
					TPTJRuleData.getRuleNum() };

			List<TPTJCount> TPTJCountDataList = ExtendedObjectProxy.getTPTJCountService().select(condition, bindSet);
			TPTJCount tptjCountData = new TPTJCount();

			for (TPTJCount countData : TPTJCountDataList)
			{
				int incrementCount = Integer.parseInt(countData.getProcessCount()) + 1;

				countData.setProcessCount(String.valueOf(incrementCount));
				countData.setLastEventTimeKey(eventInfo.getEventTimeKey());

				tptjCountData = ExtendedObjectProxy.getTPTJCountService().modify(eventInfo, countData);
			}

			if (StringUtil.equals(tptjCountData.getOperationQty(), tptjCountData.getProcessCount()))
			{
				tptjCountData.setDeleteFlag("Y");
				ExtendedObjectProxy.getTPTJCountService().modify(eventInfo, tptjCountData);
			}
		}
		catch (Exception e)
		{
			log.info("Fail Update TPTJCountData : ", e);
		}
	}

}
