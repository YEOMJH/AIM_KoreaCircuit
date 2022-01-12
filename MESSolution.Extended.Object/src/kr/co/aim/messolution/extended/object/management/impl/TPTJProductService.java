package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.TPTJCount;
import kr.co.aim.messolution.extended.object.management.data.TPTJProduct;
import kr.co.aim.messolution.extended.object.management.data.TPTJRule;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TPTJProductService extends CTORMService<TPTJProduct> {

	private static Log log = LogFactory.getLog(TPTJProductService.class);
	public static Log logger = LogFactory.getLog(TPTJProductService.class);

	private final String historyEntity = "TPTJProductHist";

	public List<TPTJProduct> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<TPTJProduct> result = super.select(condition, bindSet, TPTJProduct.class);

		return result;
	}

	public TPTJProduct selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(TPTJProduct.class, isLock, keySet);
	}

	public TPTJProduct create(EventInfo eventInfo, TPTJProduct dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, TPTJProduct dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}
	
	
	public TPTJProduct getDataInfo(String productName,String lotName,String factoryName,String productSpecName,String productSpecVersion,String processflowName,String processflowVersion,String processOperationName, String processOperationVer,
																																		 String sampleFlowName, String sampleFlowVer,String sampleOperName, String sampleOperVer)
	{
		TPTJProduct dataInfo = null;

		try
		{
			dataInfo = this.selectByKey(false, new Object[] { productName, lotName, factoryName, productSpecName, productSpecVersion, processflowName, processflowVersion, processOperationName,processOperationVer,
																																	  sampleFlowName,sampleFlowVer,sampleOperName,sampleOperVer });
		}
		catch (Exception ex)
		{
			log.info(ex.toString());
			return null;
		}

		return dataInfo;
	}
	
	public TPTJProduct getDataInfo(Object[] keySet) throws CustomException
	{
		TPTJProduct dataInfo = null;

		try
		{
			dataInfo = this.selectByKey(false, keySet);
		}
		catch (greenFrameDBErrorSignal dbError)
		{
			if (dbError.getErrorCode().equals(ErrorSignal.NotFoundSignal))
				return null;
			else
				throw new CustomException(dbError);
		}
		catch (Exception ex)
		{
			throw new CustomException(ex);
		}

		return dataInfo;
	}

	public void insertTPTJProduct(EventInfo eventInfo, Lot lotData, TPTJRule TPTJRuleData, Map<String, Object> samplePolicy, String productName, String position, String tptjSamplePosition)
			throws CustomException
	{
		try
		{
			log.info("Insert TPTJ Product Data : TrackOut");
			eventInfo.setEventName("InsertTPTJData");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			TPTJProduct tptjProductData = new TPTJProduct();
			tptjProductData.setProductName(productName);
			tptjProductData.setLotName(lotData.getKey().getLotName());
			tptjProductData.setFactoryName(lotData.getFactoryName());
			tptjProductData.setProductSpecName(lotData.getProductSpecName());
			tptjProductData.setProductSpecVersion(lotData.getProductSpecVersion());
			tptjProductData.setProcessFlowName(lotData.getProcessFlowName());
			tptjProductData.setProcessFlowVersion(lotData.getProcessFlowVersion());
			tptjProductData.setProcessOperationName(lotData.getProcessOperationName());
			tptjProductData.setProcessOperationVersion(lotData.getProcessOperationVersion());
			tptjProductData.setSampleProcessFlow((String) samplePolicy.get("TOPROCESSFLOWNAME"));
			tptjProductData.setSampleProcessFlowVersion((String) samplePolicy.get("TOPROCESSFLOWVERSION"));
			tptjProductData.setSampleOperationName((String) samplePolicy.get("TOPROCESSOPERATIONNAME"));
			tptjProductData.setSampleOperationVersion((String) samplePolicy.get("TOPROCESSOPERATIONVERSION"));
			tptjProductData.setLastEventName(eventInfo.getEventName());
			tptjProductData.setLastEventUser(eventInfo.getEventUser());
			tptjProductData.setLastEventTime(eventInfo.getEventTime());
			tptjProductData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			tptjProductData.setLastEventComment(eventInfo.getEventComment());
			tptjProductData.setPosition(Long.valueOf(position));
			tptjProductData.setTptjSamplePosition(tptjSamplePosition);
			tptjProductData.setRuleNum(TPTJRuleData.getRuleNum());
			tptjProductData.setRuleName(TPTJRuleData.getRuleName());

			tptjProductData = ExtendedObjectProxy.getTPTJProductService().create(eventInfo, tptjProductData);
		}
		catch (Exception e)
		{
			log.info("Fail Insert TPTJProductData : ", e);
		}
	}

	public TPTJProduct modify(EventInfo eventInfo, TPTJProduct dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public List<TPTJProduct> getTPTJProductListByRuleFlow(Lot lotData, TPTJRule ruleData)
	{
		List<TPTJProduct> TPTJProList = new ArrayList<TPTJProduct>();

		String condition = "lotName = ? and factoryName =? and productSpecName =? and processFlowName = ? and ruleNum = ? and ruleName = ? order by productName";
		Object bindSet[] = new Object[] { lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), ruleData.getProcessFlowName(), ruleData.getRuleNum(),
				ruleData.getRuleName() };

		try
		{
			TPTJProList = ExtendedObjectProxy.getTPTJProductService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			TPTJProList = null;
		}
		
		return TPTJProList;
	}

	public List<TPTJProduct> getTPTJProductListByLotFlow(Lot lotData, TPTJRule ruleData)
	{
		List<TPTJProduct> TPTJProList = new ArrayList<TPTJProduct>();

		String condition = "lotName = ? and factoryName =? and productSpecName =? and processFlowName = ? and ruleNum = ? and ruleName = ? order by productName";
		Object bindSet[] = new Object[] { lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), ruleData.getRuleNum(),
				ruleData.getRuleName() };

		try
		{
			TPTJProList = ExtendedObjectProxy.getTPTJProductService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			TPTJProList = null;
		}

		return TPTJProList;
	}
	
	public List<TPTJProduct> getTPTJProductListByCount(String lotName,String factoryName,String productSpecName,String processflowName, TPTJCount tptjCountData)
	{
		List<TPTJProduct> TPTJProList = null;

		try
		{
			String condition = "lotName = ? and factoryName = ? and productSpecName = ? and processFlowName = ? and ruleName = ? and ruleNum = ? ";
			Object bindSet[] = new Object[] { lotName, factoryName, productSpecName, processflowName, tptjCountData.getRuleName(), tptjCountData.getRuleNum() };
			TPTJProList = ExtendedObjectProxy.getTPTJProductService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			TPTJProList = null;
		}

		return TPTJProList;
	}
	
	
	public List<TPTJProduct> getTPTJProductListByCount(List<TPTJProduct> TPTJProList, Lot lotData, TPTJCount tptjCountData)
	{
		try
		{
			// Delete TPTJProduct Data
			String condition = "lotName = ? and factoryName = ? and productSpecName = ? and processFlowName = ? and ruleName = ? and ruleNum = ? ";
			Object bindSet[] = new Object[] { lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), tptjCountData.getRuleName(),
					tptjCountData.getRuleNum() };
			TPTJProList = ExtendedObjectProxy.getTPTJProductService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			TPTJProList = null;
		}

		return TPTJProList;
	}
	
	public boolean checkTPTJStartFlow(TPTJRule TPTJRule, Lot lotData, Map<String, Object> samplePolicyM)
	{
		String condition = "lotName = ? and factoryName =? and productSpecName =? and processFlowName = ? and processOperationName = ? and sampleProcessFlowname = ? and sampleOperationName = ? and ruleNum = ? and ruleName = ? order by productName";
		Object bindSet[] = new Object[] { lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), TPTJRule.getProcessFlowName(), TPTJRule.getProcessOperationName(),
				lotData.getProcessFlowName(), lotData.getProcessOperationName(), TPTJRule.getRuleNum(), TPTJRule.getRuleName() };

		List<TPTJProduct> TPTJProdList = new ArrayList<TPTJProduct>();

		try
		{
			TPTJProdList = ExtendedObjectProxy.getTPTJProductService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			TPTJProdList = null;
		}

		boolean checkFlag = false;

		if (TPTJProdList != null)
		{
			checkFlag = true;
		}

		return checkFlag;
	}

}