package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.OriginalProductInfo;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class OriginalProductInfoService extends CTORMService<OriginalProductInfo> {
	public static Log logger = LogFactory.getLog(OriginalProductInfoService.class);

	private final String historyEntity = "OriginalProductInfoHist";

	public List<OriginalProductInfo> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<OriginalProductInfo> result = super.select(condition, bindSet, OriginalProductInfo.class);

		return result;
	}

	public OriginalProductInfo selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(OriginalProductInfo.class, isLock, keySet);
	}

	public OriginalProductInfo create(EventInfo eventInfo, OriginalProductInfo dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, OriginalProductInfo dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public OriginalProductInfo modify(EventInfo eventInfo, OriginalProductInfo dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public OriginalProductInfo insertOriginalProductInfo(EventInfo eventInfo, String productName, String lotName, String factoryName, String productSpecName, String productSpecVersion,
			String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, long position, String slotPosition, String carrierName)
	{
		OriginalProductInfo dataInfo = new OriginalProductInfo();

		dataInfo.setProductName(productName);
		dataInfo.setLotName(lotName);
		dataInfo.setFactoryName(factoryName);
		dataInfo.setProductSpecName(productSpecName);
		dataInfo.setProductSpecVersion(productSpecVersion);
		dataInfo.setProcessFlowName(processFlowName);
		dataInfo.setProcessFlowVersion(processFlowVersion);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(processOperationVersion);
		dataInfo.setPosition(position);
		dataInfo.setSlotPosition(slotPosition);
		dataInfo.setCarrierName(carrierName);
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
		dataInfo.setLastEventUser(eventInfo.getEventUser());

		dataInfo = this.create(eventInfo, dataInfo);

		return dataInfo;
	}

	public OriginalProductInfo getOriginalProductInfoList(String productName)
	{
		OriginalProductInfo dataInfo = new OriginalProductInfo();

		try
		{
			dataInfo = this.selectByKey(false, new Object[] { productName });
		}
		catch (Exception e)
		{
			dataInfo = null;
		}

		return dataInfo;
	}

	public void deleteOriginalProductInfo(EventInfo eventInfo, String productName)
	{
		OriginalProductInfo dataInfo = this.getOriginalProductInfoList(productName);

		if (dataInfo != null)
			this.remove(eventInfo, dataInfo);
	}

	public void insertOriginalProductInfo(EventInfo eventInfo, Product productData) throws CustomException
	{
		OriginalProductInfo dataInfo = new OriginalProductInfo();

		try
		{
			dataInfo = ExtendedObjectProxy.getOriginalProductInfoService().selectByKey(false, new Object[] { productData.getKey().getProductName() });
		}
		catch (Exception e)
		{
			dataInfo = null;
		}

		if (dataInfo != null)
		{
			if (StringUtils.isEmpty(productData.getUdfs().get("CHANGESHOPLOTNAME"))
					&& (!StringUtils.equals(dataInfo.getLotName(), productData.getLotName()) || !StringUtils.equals(dataInfo.getCarrierName(), productData.getCarrierName())))
			{
				throw new CustomException("LOT-0305", productData.getKey().getProductName(), dataInfo.getCarrierName(), dataInfo.getLotName());
			}
		}
		else
		{
			ExtendedObjectProxy.getOriginalProductInfoService()
					.insertOriginalProductInfo(eventInfo, productData.getKey().getProductName(), productData.getLotName(), productData.getFactoryName(), productData.getProductSpecName(),
							productData.getProductSpecVersion(), productData.getProcessFlowName(), productData.getProcessFlowVersion(), productData.getProcessOperationName(),
							productData.getProcessOperationVersion(), productData.getPosition(), productData.getUdfs().get("SLOTPOSITION"), productData.getCarrierName());

		}
	}

}
