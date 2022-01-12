package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ProductFutureAction;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ProductFutureActionService extends CTORMService<ProductFutureAction> {

	public static Log logger = LogFactory.getLog(ProductFutureActionService.class);

	private final String historyEntity = "CT_ProductFutureActionHist";

	public List<ProductFutureAction> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<ProductFutureAction> result = super.select(condition, bindSet, ProductFutureAction.class);

		return result;
	}

	public ProductFutureAction selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ProductFutureAction.class, isLock, keySet);
	}

	public ProductFutureAction create(EventInfo eventInfo, ProductFutureAction dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, ProductFutureAction dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public ProductFutureAction modify(EventInfo eventInfo, ProductFutureAction dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public ProductFutureAction insertProductFutureAction(EventInfo eventInfo, String productName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion,String unitName, String reasonCode, String reasonCodeType, String actionName, String actionType,String lotName) throws CustomException
	{
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		ProductFutureAction dataInfo = new ProductFutureAction();
		dataInfo.setProductName(productName);
		dataInfo.setFactoryName(factoryName);
		dataInfo.setProcessFlowName(processFlowName);
		dataInfo.setProcessFlowVersion(processFlowVersion);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(processOperationVersion);
		dataInfo.setUnitName(unitName);
		dataInfo.setReasonCode(reasonCode);
		dataInfo.setReasonCodeType(reasonCodeType);
		dataInfo.setActionName(actionName);
		dataInfo.setActionType(actionType);
		dataInfo.setLotName(lotName);
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
		dataInfo.setLastEventTime(eventInfo.getEventTime());

		ProductFutureAction productFutureActionData = ExtendedObjectProxy.getProductFutureActionService().create(eventInfo, dataInfo);

		return productFutureActionData;
	}
	
	public void deleteProductFutureActionData(EventInfo eventInfo, String productName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion,String unitName, String reasonCode) throws CustomException
	{
		ProductFutureAction dataInfo = ExtendedObjectProxy.getProductFutureActionService().getProductFutureActionData(productName, factoryName, processFlowName, processFlowVersion, processOperationName,
				processOperationVersion,unitName, reasonCode);

		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		ExtendedObjectProxy.getProductFutureActionService().remove(eventInfo, dataInfo);
	}
	
	public void deleteProductFutureActionWithoutReaconCode(EventInfo eventInfo, String productName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion) throws CustomException
	{
		List<ProductFutureAction> dataInfoList = 
				ExtendedObjectProxy.getProductFutureActionService().getProductFutureActionDataListWithoutReasonCode(productName, factoryName, processFlowName, processFlowVersion,
				processOperationName, processOperationVersion);

		if (dataInfoList != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			for (ProductFutureAction dataInfo : dataInfoList)
			{
				ExtendedObjectProxy.getProductFutureActionService().remove(eventInfo, dataInfo);
			}
		}
	}
	
	public void deleteProductFutureActionWithReasonCodeType(EventInfo eventInfo, String productName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, String reasonCode, String reasonCodeType) throws CustomException
	{
		List<ProductFutureAction> dataInfoList = ExtendedObjectProxy.getProductFutureActionService().getLotFutureActionDataListWithReasonCodeType(productName, factoryName, processFlowName, processFlowVersion,
				processOperationName, processOperationVersion, reasonCode, reasonCodeType);

		if (dataInfoList != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			for (ProductFutureAction dataInfo : dataInfoList)
			{
				ExtendedObjectProxy.getProductFutureActionService().remove(eventInfo, dataInfo);
			}
		}
	}
	
	public ProductFutureAction updateProductFutureAction(EventInfo eventInfo, String productName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion,String unitName, String reasonCode, String reasonCodeType, String actionName, String actionType,String lotName) throws CustomException
	{
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		ProductFutureAction dataInfo = 
				ExtendedObjectProxy.getProductFutureActionService().getProductFutureActionData(productName, factoryName, 
						processFlowName, processFlowVersion, processOperationName, processOperationVersion,unitName, reasonCode);

		ProductFutureAction productFutureActionData = new ProductFutureAction();

		if (dataInfo != null)
		{
			dataInfo.setReasonCodeType(reasonCodeType);
			dataInfo.setActionName(actionName);
			dataInfo.setActionType(actionType);
			dataInfo.setLotName(lotName);
			dataInfo.setLastEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
			dataInfo.setLastEventTime(eventInfo.getEventTime());

			productFutureActionData = ExtendedObjectProxy.getProductFutureActionService().modify(eventInfo, dataInfo);
		}
		else
		{
			productFutureActionData = null;
		}

		return productFutureActionData;
	}
	
	public List<ProductFutureAction> getProductFutureActionDataListWithReasonCodeType(String productName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, String reasonCode, String reasonCodeType)
	{
		String condition = "PRODUCTNAME = ? AND FACTORYNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND REASONCODE = ? AND REASONCODETYPE = ?";
		Object[] bindSet = new Object[] { productName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, reasonCode, reasonCodeType };

		List<ProductFutureAction> dataList = new ArrayList<ProductFutureAction>();
		try
		{
			dataList = ExtendedObjectProxy.getProductFutureActionService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataList = null;
		}

		return dataList;
	}
	
	public List<ProductFutureAction> getProductFutureActionDataListWithoutReasonCode(String productName, String factoryName, String processFlowName, String processFlowVersion, 
			String processOperationName, String processOperationVersion)
	{
		String condition = "PRODUCTNAME = ? AND FACTORYNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? ";
		Object[] bindSet = new Object[] { productName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion };

		List<ProductFutureAction> dataList = new ArrayList<ProductFutureAction>();
		try
		{
			dataList = ExtendedObjectProxy.getProductFutureActionService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataList = null;
		}

		return dataList;
	}
	
	public List<ProductFutureAction> getLotFutureActionDataListWithReasonCodeType(String productName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, String reasonCode, String reasonCodeType)
	{
		String condition = "PRODUCTNAME = ? AND FACTORYNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND REASONCODE = ? AND REASONCODETYPE = ?";
		Object[] bindSet = new Object[] { productName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, reasonCode, reasonCodeType };

		List<ProductFutureAction> dataList = new ArrayList<ProductFutureAction>();
		try
		{
			dataList = ExtendedObjectProxy.getProductFutureActionService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataList = null;
		}

		return dataList;
	}

	
	public List<ProductFutureAction> getProductFutureActionDataList(String productName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, String reasonCode)
	{
		String condition = "PRODUCTNAME = ? AND FACTORYNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND REASONCODE = ?";
		Object[] bindSet = new Object[] { productName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, reasonCode };

		List<ProductFutureAction> dataList = new ArrayList<ProductFutureAction>();
		try
		{
			dataList = ExtendedObjectProxy.getProductFutureActionService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataList = null;
		}

		return dataList;
	}
	
	public ProductFutureAction getProductFutureActionData(String productName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion,
			String unitName,String reasonCode) throws CustomException
	{
		Object[] keySet = new Object[] { productName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion,unitName, reasonCode };

		ProductFutureAction dataInfo = new ProductFutureAction();

		try
		{
			dataInfo = ExtendedObjectProxy.getProductFutureActionService().selectByKey(false, keySet);
		}
		catch (Exception e)
		{
			dataInfo = null;
		}

		return dataInfo;
	}
	
	public List<ProductFutureAction> getProductFutureActionDataWithActionName(String productName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, String actionName)
	{
		String condition = "PRODUCTNAME = ? AND FACTORYNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND ACTIONNAME = ?";
		Object[] bindSet = new Object[] { productName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, actionName };

		List<ProductFutureAction> dataInfoList = new ArrayList<ProductFutureAction>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getProductFutureActionService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public List<ProductFutureAction> getProductFutureActionDataByProductList(List<String> productList)
	{
		String condition = "PRODUCTNAME IN (";
		int count = 0;

		for (String productName : productList)
		{
			condition += "'" + productName + "'";
			count += 1;

			if (count != productList.size())
			{
				condition += ",";
			}
		}

		condition += ")";

		Object[] bindSet = new Object[] {};

		List<ProductFutureAction> dataInfoList = new ArrayList<ProductFutureAction>();

		try
		{
			dataInfoList = ExtendedObjectProxy.getProductFutureActionService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList;
	}
	
	public List<ProductFutureAction> getProductFutureActionDataByProductName(String productName)
	{
		String condition = "PRODUCTNAME = ? ";
		Object[] bindSet = new Object[] { productName };

		List<ProductFutureAction> dataList = new ArrayList<ProductFutureAction>();
		try
		{
			dataList = ExtendedObjectProxy.getProductFutureActionService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataList = null;
		}

		return dataList;
	}
	
	public List<ProductFutureAction> getProductFutureActionDataListByProductName(String productName, String factoryName)
	{
		String condition = "PRODUCTNAME = ? AND FACTORYNAME = ?";
		Object[] bindSet = new Object[] { productName, factoryName };

		List<ProductFutureAction> dataList = new ArrayList<ProductFutureAction>();
		try
		{
			dataList = ExtendedObjectProxy.getProductFutureActionService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataList = null;
		}

		return dataList;
	}
	
}
