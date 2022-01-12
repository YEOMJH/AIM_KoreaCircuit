package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MainReserveSkip;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MainReserveSkipService extends CTORMService<MainReserveSkip> {

	public static Log logger = LogFactory.getLog(MainReserveSkipService.class);

	private final String historyEntity = "MainReserveSkipHist";

	public List<MainReserveSkip> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<MainReserveSkip> result = super.select(condition, bindSet, MainReserveSkip.class);

		return result;
	}

	public MainReserveSkip selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MainReserveSkip.class, isLock, keySet);
	}

	public MainReserveSkip create(EventInfo eventInfo, MainReserveSkip dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, MainReserveSkip dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public MainReserveSkip modify(EventInfo eventInfo, MainReserveSkip dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public List<MainReserveSkip> getMainReserveSkipDataList(String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion)
	{
		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion };

		List<MainReserveSkip> dataList = new ArrayList<MainReserveSkip>();
		try
		{
			dataList = ExtendedObjectProxy.getMainReserveSkipService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataList = null;
		}

		return dataList;
	}

	public MainReserveSkip getMainResrveSkipData(String lotName, String processOperationName, String processOperationVersion)
	{
		MainReserveSkip dataInfo = new MainReserveSkip();

		try
		{
			dataInfo = ExtendedObjectProxy.getMainReserveSkipService().selectByKey(false, new Object[] { lotName, processOperationName, processOperationVersion });
		}
		catch (Exception e)
		{
			dataInfo = null;
		}

		return dataInfo;
	}

	public List<MainReserveSkip> getMainResrveSkipDataByLotName(String lotName)
	{
		String condition = "LOTNAME = ?";
		Object[] bindSet = new Object[] { lotName };

		List<MainReserveSkip> dataList = new ArrayList<MainReserveSkip>();
		try
		{
			dataList = ExtendedObjectProxy.getMainReserveSkipService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataList = null;
		}

		return dataList;
	}

	public MainReserveSkip createMainReserveSkip(EventInfo eventInfo, String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName,
			String processFlowVersion, String processOperationName, String processOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		MainReserveSkip dataInfo = new MainReserveSkip();
		dataInfo.setLotName(lotName);
		dataInfo.setFactoryName(factoryName);
		dataInfo.setProductSpecName(productSpecName);
		dataInfo.setProductSpecVersion(productSpecVersion);
		dataInfo.setProcessFlowName(processFlowName);
		dataInfo.setProcessFlowVersion(processFlowVersion);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(processOperationVersion);
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
		dataInfo.setLastEventTime(eventInfo.getEventTime());

		dataInfo = ExtendedObjectProxy.getMainReserveSkipService().create(eventInfo, dataInfo);

		return dataInfo;
	}

	// AR-AMF-0030-01
	public void syncMainReserveSkip(EventInfo eventInfo, List<String> srcLotList, List<String> destLotList) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		if (srcLotList.size() > 0 && destLotList.size() > 0)
		{
			logger.info("SyncMainReserveSkip");

			for (String srcLot : srcLotList)
			{
				List<MainReserveSkip> srcReserveList = ExtendedObjectProxy.getMainReserveSkipService().getMainResrveSkipDataByLotName(srcLot);

				if (srcReserveList != null)
				{
					for (String destLot : destLotList)
					{
						for (MainReserveSkip srcReserve : srcReserveList)
						{
							String factoryName = srcReserve.getFactoryName();
							String productSpecName = srcReserve.getProductSpecName();
							String productSpecVersion = srcReserve.getProductSpecVersion();
							String processFlowName = srcReserve.getProcessFlowName();
							String processFlowVersion = srcReserve.getProcessFlowVersion();
							String processOperationName = srcReserve.getProcessOperationName();
							String processOperationVersion = srcReserve.getProcessOperationVersion();

							MainReserveSkip dataInfo = ExtendedObjectProxy.getMainReserveSkipService().getMainResrveSkipData(destLot, processOperationName, processOperationVersion);

							if (dataInfo == null)
							{
								logger.info("Insert MainReserveData lotName : " + destLot + ", ProcessOperationName : " + processOperationName);

								eventInfo.setEventName("CreateMainReserveSkip");
								ExtendedObjectProxy.getMainReserveSkipService().createMainReserveSkip(eventInfo, destLot, factoryName, productSpecName, productSpecVersion, processFlowName,
										processFlowVersion, processOperationName, processOperationVersion);
							}

							List<Product> productList = new ArrayList<Product>();

							try
							{
								productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(srcLot);
							}
							catch (Exception e)
							{
								productList = null;
							}

							if (productList == null)
							{
								logger.info("SourceLot[" + srcLot + "] MainReserveSkip Data Delete");

								eventInfo.setEventName("DeleteMainReserveSkip");
								ExtendedObjectProxy.getMainReserveSkipService().remove(eventInfo, srcReserve);
							}
						}
					}
				}
			}
		}
	}
}
