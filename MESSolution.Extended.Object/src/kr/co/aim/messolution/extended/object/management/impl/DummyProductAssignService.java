package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.DummyProductAssign;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;

public class DummyProductAssignService extends CTORMService<DummyProductAssign> {
	public static Log logger = LogFactory.getLog(DummyProductAssignService.class);

	private final String historyEntity = "DummyProductAssignHist";

	public List<DummyProductAssign> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<DummyProductAssign> result = super.select(condition, bindSet, DummyProductAssign.class);

		return result;
	}

	public DummyProductAssign selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(DummyProductAssign.class, isLock, keySet);
	}

	public DummyProductAssign create(EventInfo eventInfo, DummyProductAssign dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, DummyProductAssign dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public DummyProductAssign modify(EventInfo eventInfo, DummyProductAssign dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public DummyProductAssign getDummyProductAssignData(String productName)
	{
		DummyProductAssign dataInfo = new DummyProductAssign();

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

	public DummyProductAssign createDummyProductAssign(EventInfo eventInfo, Product productData, Lot srcLotData, Lot destLotData)
	{
		DummyProductAssign dataInfo = new DummyProductAssign();
		dataInfo.setProductName(productData.getKey().getProductName());
		dataInfo.setFactoryName(productData.getFactoryName());
		dataInfo.setProductSpecName(destLotData.getProductSpecName());
		dataInfo.setProductSpecVersion(destLotData.getProductSpecVersion());
		dataInfo.setProductRequestName(destLotData.getProductRequestName());
		dataInfo.setReturnProductSpecName(srcLotData.getProductSpecName());
		dataInfo.setReturnProductSpecVersion(srcLotData.getProductSpecVersion());
		dataInfo.setProcessFlowName(destLotData.getProcessFlowName());
		dataInfo.setProcessFlowVersion(destLotData.getProcessFlowVersion());
		dataInfo.setProcessOperationName(destLotData.getProcessOperationName());
		dataInfo.setProcessOperationVersion(destLotData.getProcessOperationVersion());
		dataInfo.setReturnProcessFlowName(srcLotData.getProcessFlowName());
		dataInfo.setReturnProcessFlowVersion(srcLotData.getProcessFlowVersion());
		dataInfo.setReturnProcessOperationName(srcLotData.getProcessOperationName());
		dataInfo.setReturnProcessOperationVersion(srcLotData.getProcessOperationVersion());
		dataInfo.setReturnProductRequestName(srcLotData.getProductRequestName());
		dataInfo.setOriginalLotName(srcLotData.getKey().getLotName());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

		this.create(eventInfo, dataInfo);

		return dataInfo;
	}

	public DummyProductAssign createDummyProductAssignReturnInfo(EventInfo eventInfo, Product productData, Lot srcLotData, Lot destLotData, String srcProductRequestName, String srcProductSpecName)
	{
		logger.info("Insert DummyProductAssign / ProdutName : " + productData.getKey().getProductName());

		DummyProductAssign dataInfo = new DummyProductAssign();
		dataInfo.setProductName(productData.getKey().getProductName());
		dataInfo.setFactoryName(productData.getFactoryName());

		dataInfo.setProductSpecName(destLotData.getProductSpecName());
		dataInfo.setProductSpecVersion(destLotData.getProductSpecVersion());
		dataInfo.setProcessFlowName(destLotData.getUdfs().get("RETURNFLOWNAME"));
		dataInfo.setProcessFlowVersion("00001");
		dataInfo.setProcessOperationName(destLotData.getUdfs().get("RETURNOPERATIONNAME"));
		dataInfo.setProcessOperationVersion("00001");
		dataInfo.setProductRequestName(destLotData.getProductRequestName());

		dataInfo.setReturnProductSpecName(srcProductSpecName);
		dataInfo.setReturnProductSpecVersion("00001");
		dataInfo.setReturnProcessFlowName(srcLotData.getUdfs().get("RETURNFLOWNAME"));
		dataInfo.setReturnProcessFlowVersion("00001");
		dataInfo.setReturnProcessOperationName(srcLotData.getUdfs().get("RETURNOPERATIONNAME"));
		dataInfo.setReturnProcessOperationVersion("00001");
		dataInfo.setReturnProductRequestName(srcProductRequestName);

		dataInfo.setOriginalLotName(srcLotData.getKey().getLotName());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

		this.create(eventInfo, dataInfo);

		return dataInfo;
	}

	public List<DummyProductAssign> getDummyProductAssignRecoverInfo(String productName, Lot lotData)
	{
		List<DummyProductAssign> dataInfoList = new ArrayList<DummyProductAssign>();
		String condition = " PRODUCTNAME = ? AND RETURNPRODUCTSPECNAME = ? AND RETURNPRODUCTSPECVERSION = ? AND RETURNPROCESSFLOWNAME = ? AND RETURNPROCESSFLOWVERSION = ? AND RETURNPRODUCTREQUESTNAME = ? ";
		Object[] bindSet = new Object[] { productName, lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
				lotData.getProductRequestName() };

		try
		{
			dataInfoList = this.select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public boolean checkDummyProductAssign(List<Element> productList) throws CustomException
	{
		for (Element product : productList)
		{
			String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", true);
			
			try
			{
				this.selectByKey(false, new Object[] { productName });
			}
			catch (Exception e)
			{
				logger.info("DummyProductAssign Data is not exist / ProductName : " + productName);
				return false;
			}
		}

		return true;
	}
}
