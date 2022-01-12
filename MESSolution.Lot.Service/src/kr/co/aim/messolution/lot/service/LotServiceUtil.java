package kr.co.aim.messolution.lot.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.sun.org.apache.bcel.internal.generic.NEW;

import kr.co.aim.messolution.alarm.MESAlarmServiceProxy;
import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AbnormalSheetDetail;
import kr.co.aim.messolution.extended.object.management.data.DSPProductRequestPlan;
import kr.co.aim.messolution.extended.object.management.data.DummyProductAssign;
import kr.co.aim.messolution.extended.object.management.data.DummyProductReserve;
import kr.co.aim.messolution.extended.object.management.data.ELACondition;
import kr.co.aim.messolution.extended.object.management.data.FirstGlassJob;
import kr.co.aim.messolution.extended.object.management.data.InlineSampleLot;
import kr.co.aim.messolution.extended.object.management.data.InlineSampleLotCount;
import kr.co.aim.messolution.extended.object.management.data.InlineSampleProduct;
import kr.co.aim.messolution.extended.object.management.data.LotFutureAction;
import kr.co.aim.messolution.extended.object.management.data.MQCPlan;
import kr.co.aim.messolution.extended.object.management.data.MQCPlanDetail;
import kr.co.aim.messolution.extended.object.management.data.MQCPlanDetail_Extended;
import kr.co.aim.messolution.extended.object.management.data.MainReserveSkip;
import kr.co.aim.messolution.extended.object.management.data.MaterialProduct;
import kr.co.aim.messolution.extended.object.management.data.OriginalProductInfo;
import kr.co.aim.messolution.extended.object.management.data.ProductFutureAction;
import kr.co.aim.messolution.extended.object.management.data.ProductQueueTime;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.extended.object.management.data.ReworkProduct;
import kr.co.aim.messolution.extended.object.management.data.SampleLot;
import kr.co.aim.messolution.extended.object.management.data.SampleLotCount;
import kr.co.aim.messolution.extended.object.management.data.SampleProduct;
import kr.co.aim.messolution.extended.object.management.data.SortJob;
import kr.co.aim.messolution.extended.object.management.data.SortJobCarrier;
import kr.co.aim.messolution.extended.object.management.data.SortJobProduct;
import kr.co.aim.messolution.extended.object.management.data.TFEDownProduct;
import kr.co.aim.messolution.extended.object.management.data.TPTJCount;
import kr.co.aim.messolution.extended.object.management.data.TPTJProduct;
import kr.co.aim.messolution.extended.object.management.data.TPTJRule;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.event.EventInfoExtended;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.GradeDefUtil;
import kr.co.aim.messolution.generic.util.NodeStack;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.product.service.ProductServiceUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.orm.ObjectAttributeDef;
import kr.co.aim.greenframe.util.bundle.BundleUtil;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.data.LotMultiHold;
import kr.co.aim.greentrack.lot.management.info.AssignProductsInfo;
import kr.co.aim.greentrack.lot.management.info.ChangeGradeInfo;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.CreateWithParentLotInfo;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.DeassignProductsInfo;
import kr.co.aim.greentrack.lot.management.info.MakeLoggedOutInfo;
import kr.co.aim.greentrack.lot.management.info.MakeNotInReworkInfo;
import kr.co.aim.greentrack.lot.management.info.MakeNotOnHoldInfo;
import kr.co.aim.greentrack.lot.management.info.MakeShippedInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processoperationspec.ProcessOperationSpecServiceProxy;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpecKey;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductNSubProductPGQS;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGQS;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;
import kr.co.aim.greentrack.product.management.info.ext.ProductRU;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.IncrementFinishedQuantityByInfo;

public class LotServiceUtil implements ApplicationContextAware {
	private static Log log = LogFactory.getLog(LotServiceUtil.class);

	private ApplicationContext applicationContext;

	public LotServiceUtil()
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		// TODO Auto-generated method stub
		applicationContext = arg0;
	}
	
	public void syncTPTJData(EventInfo eventInfo, List<String> srcLotList, List<String> destLotList) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		log.info("Ready to Start Sync TPTJ Data");
		
		if (srcLotList.size() > 0 && destLotList.size() > 0)
		{
			log.info("Start Sync TPTJ Data");
			
			for (String srcLotName : srcLotList)
			{
				// Only Production Operation.
				Lot srcLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(srcLotName);
				String srcNodeStack = srcLotData.getNodeStack();
				String mainNodeId = srcNodeStack.contains(".") ? srcNodeStack.substring(0, srcNodeStack.indexOf('.')) : srcNodeStack;

				Node mainNodeData = ProcessFlowServiceProxy.getNodeService().getNode(mainNodeId);
				MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(mainNodeData);
				CommonUtil.getProcessOperationSpec(mainNodeData.getFactoryName(), mainNodeData.getNodeAttribute1(), mainNodeData.getNodeAttribute2());

				List<TPTJProduct> TPTJProductDataList = new ArrayList<TPTJProduct>();
				List<TPTJCount> TPTJCountDataList = ExtendedObjectProxy.getTPTJCountService().getTPTJCountDataList(srcLotData.getKey().getLotName(), srcLotData.getFactoryName(),
						                                                                                           srcLotData.getProductSpecName(), mainNodeData.getProcessFlowName());
               
				if (TPTJCountDataList != null)
				{
					for (TPTJCount TPTJCountData : TPTJCountDataList)
					{
						TPTJProductDataList = ExtendedObjectProxy.getTPTJProductService().getTPTJProductListByCount(srcLotData.getKey().getLotName(), srcLotData.getFactoryName(),
								                                                                                    srcLotData.getProductSpecName(), mainNodeData.getProcessFlowName(), TPTJCountData);
						if (TPTJProductDataList != null)
						{
							for (String destLotName : destLotList)
							{
								int moveCount = 0;
								boolean hasSynced = false;
								
								List<Product> destProductDataList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(destLotName);
								List<String> destProductNameList = new ArrayList<String>();

								for (Product product : destProductDataList)
								{
									destProductNameList.add(product.getKey().getProductName());
								}
								
								if(destProductNameList.size() ==0) continue;
								
								for(TPTJProduct TPTJProductData :TPTJProductDataList )
								{
									// Split TPTJProduct data
									if (destProductNameList.contains((TPTJProductData.getProductName())))
									{
										String sql = " UPDATE CT_TPTJPRODUCT  SET LOTNAME = ? "
												   + " WHERE  1=1  AND PRODUCTNAME=? AND LOTNAME=? AND FACTORYNAME=? AND PRODUCTSPECNAME=? AND PRODUCTSPECVERSION=? "
												   + " AND PROCESSFLOWNAME=? AND PROCESSFLOWVERSION=? AND PROCESSOPERATIONNAME=? AND PROCESSOPERATIONVERSION=? "
												   + " AND SAMPLEPROCESSFLOWNAME=? AND SAMPLEPROCESSFLOWVERSION=? AND SAMPLEOPERATIONNAME=? AND SAMPLEOPERATIONVERSION=? ";
										
										Object[] bindSet = new Object[14];
										bindSet[0] = destLotName;
										bindSet[1] = TPTJProductData.getProductName();
										bindSet[2] = TPTJProductData.getLotName();
										bindSet[3] = TPTJProductData.getFactoryName();
										bindSet[4] = TPTJProductData.getProductSpecName();
										bindSet[5] = TPTJProductData.getProductSpecVersion();
										bindSet[6] = TPTJProductData.getProcessFlowName();
										bindSet[7] = TPTJProductData.getProcessFlowVersion();
										bindSet[8] = TPTJProductData.getProcessOperationName();
										bindSet[9] = TPTJProductData.getProcessOperationVersion();
										bindSet[10] = TPTJProductData.getSampleProcessFlowName();
										bindSet[11] = TPTJProductData.getSampleProcessFlowVersion();
										bindSet[12] = TPTJProductData.getSampleOperationName();
										bindSet[13] = TPTJProductData.getSampleOperationVersion();
										
										try
										{
											GenericServiceProxy.getSqlMesTemplate().update(sql, bindSet);
											
											bindSet[2] = destLotName;
											Object[] destBindSet = ArrayUtils.remove(bindSet, 0);
											TPTJProduct refereshData =  ExtendedObjectProxy.getTPTJProductService().getDataInfo(destBindSet);
											
											refereshData.setLastEventName("SplitTPTJData");
											refereshData.setLastEventUser(eventInfo.getEventUser());
											refereshData.setLastEventTime(eventInfo.getEventTime());
											refereshData.setLastEventTimeKey(eventInfo.getEventTimeKey());
											refereshData.setLastEventComment(String.format("Product [%s] split from [%s] Lot.", TPTJProductData.getProductName(), srcLotName));
                                            
											EventInfo extendedInfo =  new EventInfoExtended(eventInfo);
											extendedInfo.setEventName("SplitTPTJData");
											extendedInfo.setEventComment(String.format("Product [%s] split from [%s] Lot.", TPTJProductData.getProductName(), srcLotName));
											
											ExtendedObjectProxy.getTPTJProductService().modify(extendedInfo, refereshData);
											log.info(String.format("TPTJ Product[%s]:FromLot[%s] -> ToLot[%s] Success.", TPTJProductData.getProductName(), srcLotName, destLotName));
											
											moveCount++;
											hasSynced = true;
										}
										catch (Exception ex)
										{
											log.info("Fail to split TPTJProductData : ", ex);
										}
									}
								}
							
								if(hasSynced)
								{
									TPTJCount dTPTJCountData = ExtendedObjectProxy.getTPTJCountService().getDataInfo(destLotName, TPTJCountData.getRuleName(), TPTJCountData.getRuleNum(), TPTJCountData.getFactoryName(), 
											 																		TPTJCountData.getProductSpecName(), TPTJCountData.getProductSpecVersion(), TPTJCountData.getProcessFlowName(), TPTJCountData.getProcessFlowVersion());
									// Create TPTJCount data for destination lot  
									if (dTPTJCountData == null)
									{
										dTPTJCountData = new TPTJCount();
										dTPTJCountData.setLotName(destLotName);
										dTPTJCountData.setRuleName(TPTJCountData.getRuleName());
										dTPTJCountData.setRuleNum(TPTJCountData.getRuleNum());
										dTPTJCountData.setFactoryName(TPTJCountData.getFactoryName());
										dTPTJCountData.setProductSpecName(TPTJCountData.getProductSpecName());
										dTPTJCountData.setProductSpecVersion(TPTJCountData.getProductSpecVersion());
										dTPTJCountData.setProcessFlowName(TPTJCountData.getProcessFlowName());
										dTPTJCountData.setProcessFlowVersion(TPTJCountData.getProcessFlowVersion());
										dTPTJCountData.setOperationQty(TPTJCountData.getOperationQty());
										dTPTJCountData.setProcessCount(TPTJCountData.getProcessCount());
										dTPTJCountData.setDeleteFlag(TPTJCountData.getDeleteFlag());
										dTPTJCountData.setCompleteFlag(TPTJCountData.getCompleteFlag());
										dTPTJCountData.setLastEventTimeKey(eventInfo.getEventTimeKey());

										try
										{
											ExtendedObjectProxy.getTPTJCountService().create(eventInfo, dTPTJCountData);
										}
										catch (Exception ex)
										{
											log.info("Fail to insert TPTJCountData : ", ex);
										}
									}
								}
								
								if(moveCount>0 && moveCount == TPTJProductDataList.size())
								{
									// Delete TPTJCount data for source lot  
									ExtendedObjectProxy.getTPTJCountService().remove(eventInfo, TPTJCountData);
									log.info(String.format("Delete TPTJCountData.[RuleName=%s,RuleNum=%s,LotName=%s]",TPTJCountData.getRuleName(),TPTJCountData.getRuleName(),TPTJCountData.getLotName()));
								}
							}
						}
					}
				}
			}
		}
		log.info("End Sync TPTJQ Data");
	}
	
	/*
	 * 2020-11-15	dhko	Add Function
	 */
	public boolean isAbortProductList(String lotName)
	{
		// Determine if ProcessingInfo has 'B' in Product
		try
		{
			String condition = "WHERE 1 = 1 "
							 + "  AND LOTNAME = ? "
							 + "  AND PRODUCTSTATE = 'InProduction' "
							 + "  AND PROCESSINGINFO = 'B' ";
			
			List<Product> productDataList = ProductServiceProxy.getProductService().select(condition, new Object[] { lotName });
			if (productDataList != null && productDataList.size() > 0 )
			{
				return true;
			}
		}
		catch (NotFoundSignal nfs)
		{
			log.info("Product data is not exists. PRODUCTSTATE = 'InProduction' AND PROCESSINGINFO = 'B' ");
		}
		catch (Exception ex)
		{
			log.info("Product data is not exists. " + ex.getCause());
		}
		
		return false;
	}
	
	public List<Product> getProductDataListByLotName(String lotName,boolean throwException) throws CustomException
	{
		if (log.isInfoEnabled())
			log.info("Input LotName is " + lotName);
		
		List<Product> productList = null;

		try
		{
			productList = ProductServiceProxy.getProductService().select(" WHERE 1=1 AND LOTNAME = ? ", new Object[] { lotName });
		}
		catch (NotFoundSignal notFoundEx)
		{
			if (throwException)
				throw new CustomException("COMM-1000", "Lot", "LotName =" + lotName);
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		return productList;
	}
	
	public List<Lot> getLotDataListByPanelNameList(List<String> panelNameList,boolean forUpdate) throws CustomException
	{
		if (panelNameList == null || panelNameList.size() == 0)
		{
			log.info("The incoming variable value is null or empty!!");
			return null;
		}

		String sql = " SELECT * FROM LOT WHERE 1=1 AND LOTNAME IN ( :PANELLIST ) ";
		Map<String, Object> bindMap = new HashMap<>();
		bindMap.put("PANELLIST", panelNameList);

		if (forUpdate)
		{
			try
			{
				GenericServiceProxy.getSqlMesTemplate().queryForList(" SELECT 1 FROM LOT WHERE 1=1 AND LOTNAME IN (:PANELLIST) FOR UPDATE NOWAIT ", bindMap);
				forUpdate = false;
			}
			catch (Exception ex)
			{
				log.info("▶Resource busy: resource is in use by another program.");
			}

			if (forUpdate) sql += " FOR UPDATE ";
		}

		List<Map<String, Object>> resultList = null;

		try
		{
			resultList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, bindMap);
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		if (resultList == null || resultList.size() == 0)
			throw new CustomException("COMM-1000", "Lot","LotList: " + org.springframework.util.StringUtils.collectionToDelimitedString(panelNameList.subList(0, panelNameList.size() > 10 ? 10 : panelNameList.size()), ",") + "...");

		List<Lot> lotDataList = LotServiceProxy.getLotService().transform(resultList);

		if (panelNameList.size() != lotDataList.size())
		{
			List<String> notFoundPanel = ListUtils.subtract(panelNameList, CommonUtil.makeToStringList(lotDataList));

			if (notFoundPanel.size() > 0)
				throw new CustomException("COMM-1000", "Lot","LotList: " + org.springframework.util.StringUtils.collectionToDelimitedString(notFoundPanel.subList(0, notFoundPanel.size() > 10 ? 10 : notFoundPanel.size()), ",") + "...");
		}

		return lotDataList;
	}
	
	public Lot getLotDataForUpdate(String lotName) throws CustomException
	{
		try
		{
			Lot lotData = LotServiceProxy.getLotService().selectByKeyForUpdate(new LotKey(lotName));
			return lotData;
		}
		catch (NotFoundSignal notFoundEx)
		{
			throw new CustomException("LOT-9000", lotName);
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}
	}
	
	public ProcessOperationSpec getProcessOperationSpecData(Lot lotData) throws CustomException
	{
		if (lotData == null)
		{
			log.info("The incoming variable value is null or empty!!");
			return null;
		}
		
		ProcessOperationSpecKey keyInfo = new ProcessOperationSpecKey();
		keyInfo.setFactoryName(lotData.getFactoryName());
		keyInfo.setProcessOperationName(lotData.getProcessOperationName());
		keyInfo.setProcessOperationVersion(lotData.getProcessOperationVersion());

		ProcessOperationSpec dataInfo = null;

		try
		{
			dataInfo = ProcessOperationSpecServiceProxy.getProcessOperationSpecService().selectByKey(keyInfo);
		}
		catch (NotFoundSignal notFoundEx)
		{
			throw new CustomException("COMM-1000", "ProcessOperationSpec", "LotName = " + lotData.getKey().getLotName());
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		return dataInfo;
	}
	
	public List<Lot> getLotListByTrayName(String trayName , boolean setLock) throws CustomException
	{
		String condition = " WHERE 1=1 AND CARRIERNAME = ? ORDER BY POSITION ";

		if (setLock)
		{
			try
			{
				GenericServiceProxy.getSqlMesTemplate().queryForList(" SELECT 1 FROM LOT WHERE 1=1 AND CARRIERNAME = ? FOR UPDATE NOWAIT ", new Object[] { trayName });
				setLock = false;
			}
			catch (Exception ex)
			{
				log.info("▶Resource busy: resource is in use by another program.");
			}

			if (setLock)
				condition += " FOR UPDATE ";
		}

		List<Lot> lotDataList = new ArrayList<>();

		try
		{
			lotDataList = LotServiceProxy.getLotService().select(condition, new Object[] { trayName });
		}
		catch (Exception ex)
		{
			if (ex instanceof NotFoundSignal)
				throw new CustomException("COMM-1000", "LOT", "CarrierName = " + trayName);
			else
				throw new CustomException(ex.getCause());
		}

		return lotDataList;
	}
	public List<Lot> getLotListByTrayList(List<Durable> trayDataList,boolean setLock) throws CustomException
	{
		String sql = " SELECT * FROM LOT WHERE 1=1 AND CARRIERNAME IN (:CARRIERLIST) ";
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("CARRIERLIST", CommonUtil.makeToStringList(trayDataList));

		List<Map<String, Object>> resultList = null;

		if (setLock)
		{
			try
			{
				GenericServiceProxy.getSqlMesTemplate().queryForList(" SELECT 1 FROM LOT WHERE 1=1 AND CARRIERNAME IN (:CARRIERLIST) FOR UPDATE NOWAIT ", bindMap);
				setLock = false;
			}
			catch (Exception ex)
			{
				log.info("▶Resource busy: resource is in use by another program.");
			}

			if (setLock) sql += " FOR UPDATE ";
		}
		
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		}
		catch (Exception ex)
		{
			new CustomException("SYS-0010", ex.getMessage());
		}

		return resultList == null ? null : LotServiceProxy.getLotService().transform(resultList);
	}
	
	public void PanelHoldByLotList(EventInfo eventInfo, List<Lot> lotList) throws CustomException
	{
		List<Lot> updateLotList = new ArrayList<Lot>();
		List<LotHistory> updateLotHistory = new ArrayList<LotHistory>();

		// Panel
		for (Lot lotData : lotList)
		{
			Lot oldLot = (Lot) ObjectUtil.copyTo(lotData);

			lotData.setLotHoldState("Y");
			lotData.setLastEventName(eventInfo.getEventName());
			lotData.setLastEventTime(eventInfo.getEventTime());
			lotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lotData.setLastEventComment(eventInfo.getEventComment());
			lotData.setLastEventUser(eventInfo.getEventUser());

			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lotData, lotHistory);

			updateLotList.add(lotData);
			updateLotHistory.add(lotHistory);
		}

		if (updateLotList.size() > 0)
		{
			log.debug("Insert Lot, LotHistory");
			try
			{
				CommonUtil.executeBatch("update", updateLotList,true);
				CommonUtil.executeBatch("insert", updateLotHistory,true);
			}
			catch (Exception e)
			{
				log.error(e.getMessage());
				throw new CustomException(e.getCause());
			}
		}
	}

	public List<Lot> getRunningLotListByMachineName(String machineName) throws CustomException
	{
		List<Lot> lotList = null;
		try {
			
			lotList = LotServiceProxy.getLotService().select(" WHERE 1=1 AND MACHINENAME =? AND LOTPROCESSSTATE ='RUN' AND LOTSTATE ='Released' ", new Object[] { machineName });

		} catch (NotFoundSignal notFoundEx) {
			log.info(String.format(" Lot data list is Empty. search by condition[MachineName=%s,LotProcessState = RUN]",machineName));
		} catch (Exception ex) {
			log.info(ex.getMessage());
			throw new CustomException(ex.getCause());
		}
		
		return lotList;
	}
	
	public void checkMaterialQTime(Lot lotData, String machineName) throws CustomException
	{
		String productionType = lotData.getProductionType();

		if (StringUtils.equals(productionType, "M") || StringUtils.equals(productionType, "D"))
			return;

		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		Map<String, Object> bindMap = new HashMap<>();

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT DECODE (NVL (COUNT (1), 0), 0, 'TRUE', 'FALSE') EXITFLAG, B.CONSUMABLENAME, B.EXPIRATIONDATE ");
		sql.append("  FROM CONSUMABLESPEC A, CONSUMABLE B ");
		sql.append(" WHERE A.FACTORYNAME = B.FACTORYNAME ");
		sql.append("   AND A.CONSUMABLESPECNAME = B.CONSUMABLESPECNAME ");
		sql.append("   AND A.CONSUMABLESPECVERSION = B.CONSUMABLESPECVERSION ");
		sql.append("   AND A.CONSUMABLETYPE = B.CONSUMABLETYPE ");
		sql.append("   AND B.CONSUMABLESTATE = :CONSUMABLESTATE ");
		sql.append("   AND B.TRANSPORTSTATE = :TRANSPORTSTATE ");
		sql.append("   AND B.MACHINENAME = :MACHINENAME ");
		sql.append("   AND SYSDATE > B.EXPIRATIONDATE ");
		sql.append("GROUP BY B.CONSUMABLENAME, B.EXPIRATIONDATE ");

		bindMap.put("CONSUMABLESTATE", constMap.Cons_InUse);
		bindMap.put("TRANSPORTSTATE", constMap.Cons_TransportState_OnEQP);
		bindMap.put("MACHINENAME", machineName);

		List<Map<String, Object>> resultList = null;
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		}
		catch (Exception ex)
		{
			log.debug(ex.getCause());
		}

		if (resultList.size() > 0 && ConvertUtil.getMapValueByName(resultList.get(0), "EXITFLAG") == "FALSE")
		{
			String tempStr = "";

			for (Map<String, Object> result : resultList)
				tempStr += ConvertUtil.getMapValueByName(result, "CONSUMABLENAME") + "|" + ConvertUtil.getMapValueByName(result, "EXPIRATIONDATE") + ",";

			throw new CustomException("MATERIAL-001", machineName, tempStr.substring(0, tempStr.length() - 1));
		}
	}

	public Lot getLotData(String lotName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		try
		{
			LotKey lotKey = new LotKey();
			lotKey.setLotName(lotName);
			Lot lotData = LotServiceProxy.getLotService().selectByKey(lotKey);

			return lotData;

		}
		catch (Exception e)
		{
			throw new CustomException("LOT-9000", lotName);
		}
	}

	public List<String> getDistinctLotNameByProductName(List<String> productList) throws CustomException
	{
		List<String> lotList = new ArrayList<String>();

		if (productList != null && productList.size() > 0)
		{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT DISTINCT L.LOTNAME ");
			sql.append("  FROM PRODUCT P, LOT L ");
			sql.append(" WHERE L.LOTNAME = P.LOTNAME ");
			sql.append("   AND P.PRODUCTNAME IN ( :PRODUCTLIST ) ");
			sql.append("   AND P.PRODUCTSTATE = 'InProduction' ");
			sql.append("   AND L.LOTSTATE = 'Released' ");
			sql.append(" ORDER BY L.LOTNAME ");

			Map<String, Object> bind = new HashMap<String, Object>();
			bind.put("PRODUCTLIST", productList);

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bind);

			lotList = CommonUtil.makeListBySqlResult(sqlResult, "LOTNAME");
		}

		return lotList;
	}

	public List<ProductPGS> setProductPGSSequence(org.jdom.Document doc) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		if (doc == null)
		{
			log.error("doc is null");
		}
		
		List<ProductPGS> productPGSList = new ArrayList<ProductPGS>();
		ProductServiceUtil productServiceUtil = (ProductServiceUtil) BundleUtil.getBundleServiceClass(ProductServiceUtil.class);

		Element root = doc.getDocument().getRootElement();

		List<Product> productDatas = new ArrayList<Product>();

		String lotName = root.getChild("Body").getChildText("LOTNAME");

		LotKey lotKey = new LotKey();
		lotKey.setLotName(lotName);
		Lot lotData = LotServiceProxy.getLotService().selectByKey(lotKey);

		if (lotName == null || lotName.equals("") == true)
		{
			String carrierName = root.getChild("Body").getChildText("CARRIERNAME");
			if (carrierName != null && carrierName != "")
			{

				String condition = "WHERE carrierName = ? AND productState != ? AND productState != ? ORDER BY position ";

				Object[] bindSet = new Object[] { carrierName, GenericServiceProxy.getConstantMap().Prod_Scrapped, GenericServiceProxy.getConstantMap().Prod_Consumed };

				try
				{
					ProductServiceProxy.getProductService().select(condition, bindSet);
				}
				catch (NotFoundSignal e)
				{
				}
			}
		}
		else
		{
			try
			{
				productDatas = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);
			}
			catch (NotFoundSignal e)
			{
			}
		}

		Element element = root.getChild("Body").getChild("PRODUCTLIST");
		if (element != null)
		{
			for (Iterator iterator = element.getChildren().iterator(); iterator.hasNext();)
			{
				Element productE = (Element) iterator.next();

				String productName = productE.getChild("PRODUCTNAME").getText();

				ProductKey productkey = new ProductKey();
				productkey.setProductName(productName);

				Product productData = ProductServiceProxy.getProductService().selectByKey(productkey);

				Boolean searchFlag = false;

				String subProductQty = ""; // PEX

				for (Iterator<Product> iteratorProduct = productDatas.iterator(); iteratorProduct.hasNext();)
				{
					Product product = iteratorProduct.next();
					if (productName.equals(product.getKey().getProductName()))
					{
						subProductQty = String.valueOf(product.getSubProductQuantity()); // PEX
						searchFlag = true;
						break;
					}
				}

				if (productDatas.size() == 0 || searchFlag == true)
				{
					String subProductQuantity1 = "";
					String subProductQuantity2 = "";

					String position = productE.getChild("POSITION").getText();

					String productGrade = "";
					if (productE.getChild("PRODUCTGRADE") != null)
					{
						productGrade = productE.getChild("PRODUCTGRADE").getText();
					}

					String subProductGrades1 = "";
					if (productE.getChild("SUBPRODUCTGRADES1") != null)
					{
						subProductGrades1 = productE.getChild("SUBPRODUCTGRADES1").getText();
					}
					else
					{
						subProductGrades1 = productData.getSubProductGrades1();
					}

					String subProductGrades2 = "";
					if (productE.getChild("SUBPRODUCTGRADES2") != null)
					{
						subProductGrades2 = productE.getChild("SUBPRODUCTGRADES2").getText();
					}
					else
					{
						subProductGrades1 = productData.getSubProductGrades1();
					}

					if (productE.getChild("SUBPRODUCTQUANTITY1") != null)
						subProductQuantity1 = productE.getChild("SUBPRODUCTQUANTITY1").getText();
					else
						subProductQuantity1 = subProductQty;

					if (productE.getChild("SUBPRODUCTQUANTITY2") != null)
						subProductQuantity2 = productE.getChild("SUBPRODUCTQUANTITY2").getText();
					else
						subProductQuantity2 = "";

					ProductPGS productPGS = new ProductPGS();

					productPGS.setProductName(productName);
					if (position.equals("") != true)
						productPGS.setPosition(Long.valueOf(position));
					productPGS.setProductGrade(productGrade);
					productPGS.setSubProductGrades1(subProductGrades1);
					productPGS.setSubProductGrades2(subProductGrades2);
					if (subProductQuantity1.equals("") != true)
						productPGS.setSubProductQuantity1(Double.valueOf(subProductQuantity1));
					if (subProductQuantity2.equals("") != true)
						productPGS.setSubProductQuantity2(Double.valueOf(subProductQuantity2));

					productPGS.setUdfs(productServiceUtil.setNamedValueSequence(productName, productE));

					productPGSList.add(productPGS);
				}
			}
		}
		return productPGSList;
	}

	public List<ProductRU> setProductRUSequence(List<String> reworkFlagProdList) throws FrameworkErrorSignal, NotFoundSignal
	{
		List<ProductRU> productRUList = new ArrayList<ProductRU>();
		/*
		if (doc == null)
		{
			log.error("doc is null");
		}
		
		List<String> productNameList = new ArrayList<String>();
		
		ProductServiceUtil productServiceUtil = (ProductServiceUtil) BundleUtil.getBundleServiceClass(ProductServiceUtil.class);
		
		Element root = doc.getDocument().getRootElement();

		List<Product> productDatas = new ArrayList<Product>();

		String lotName = root.getChild("Body").getChildText("LOTNAME");

		if (lotName == null || lotName == "")
		{
			String carrierName = root.getChild("Body").getChildText("CARRIERNAME");

			if (carrierName != null && carrierName != "")
			{
				String condition = "WHERE carrierName = ? AND productState != ? AND productState != ? ORDER BY position ";
				Object[] bindSet = new Object[] { carrierName, GenericServiceProxy.getConstantMap().Prod_Scrapped, GenericServiceProxy.getConstantMap().Prod_Consumed };

				productDatas = ProductServiceProxy.getProductService().select(condition, bindSet);
			}
		}
		else
		{
			try
			{
				productDatas = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);
			}
			catch (Exception e)
			{
				log.error(e);
				productDatas = ProductServiceProxy.getProductService().allProductsByLot(lotName);
			}
		}*/

		for (String productName : reworkFlagProdList)
		{
			Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);
			
			ProductRU productRU = new ProductRU();
			productRU.setProductName(productName);
			productRU.getUdfs().put("REWORKFLAG", "Y");

			productRUList.add(productRU);
		}
		
		return productRUList;
	}

	public List<Map<String, Object>> getReworkReturnNodeInfo(String factoryName, String returnProcessFlowName, String returnOperationName) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT NODEID ");
		sql.append("  FROM NODE ");
		sql.append(" WHERE FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND NODEATTRIBUTE1 = :PROCESSOPERATIONNAME ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", factoryName);
		args.put("PROCESSFLOWNAME", returnProcessFlowName);
		args.put("PROCESSOPERATIONNAME", returnOperationName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		return result;
	}

	public String getBeforeOperationName(String processFlowName, String processOperationName) throws CustomException
	{
		String nodeId = "";

		Map<String, Object> bindMap = new HashMap<String, Object>();
		List<Map<String, Object>> sqlResult = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> sqlRst = new ArrayList<Map<String, Object>>();

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT NODEID ");
		sql.append("  FROM NODE ");
		sql.append(" WHERE NODEATTRIBUTE1 = :PROCESSOPERATIONNAME ");
		sql.append("   AND PROCESSFLOWNAME = :PROCESSFLOWNAME ");

		bindMap.clear();
		bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		bindMap.put("PROCESSFLOWNAME", processFlowName);

		sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		if (sqlResult.size() > 0)
		{
			String nodeName = (String) sqlResult.get(0).get("NODEID");

			StringBuffer nodeSql = new StringBuffer();
			nodeSql.append("SELECT N.NODEATTRIBUTE1 ");
			nodeSql.append("  FROM NODE N ");
			nodeSql.append(" WHERE 1 = 1 ");
			nodeSql.append("   AND N.NODEID = (SELECT FROMNODEID ");
			nodeSql.append("                     FROM ARC ");
			nodeSql.append("                    WHERE TONODEID = :NODEID ");
			nodeSql.append("                      AND PROCESSFLOWNAME = :PROCESSFLOWNAME) ");

			bindMap.clear();
			bindMap.put("NODEID", nodeName);
			bindMap.put("PROCESSFLOWNAME", processFlowName);

			sqlRst = GenericServiceProxy.getSqlMesTemplate().queryForList(nodeSql.toString(), bindMap);

			if (sqlRst.size() > 0)
			{
				nodeId = (String) sqlRst.get(0).get("NODEATTRIBUTE1");
			}
		}

		return nodeId;
	}

	public Map<String, String> setNamedValueSequence(String lotName, Element element) throws FrameworkErrorSignal, NotFoundSignal
	{
		Map<String, String> namedValueMap = new HashMap<String, String>();

		LotKey lotKey = new LotKey(lotName);

		Lot lotData = null;

		lotData = LotServiceProxy.getLotService().selectByKey(lotKey);

		List<ObjectAttributeDef> objectAttributeDefs = greenFrameServiceProxy.getObjectAttributeMap().getAttributeNames("Lot", "ExtendedC");

		log.info("UDF SIZE=" + lotData.getUdfs().size());

		namedValueMap = lotData.getUdfs();

		String tempElementUdfValue = "";

		for (int i = 0; i < objectAttributeDefs.size(); i++)
		{
			if (element != null)
			{
				tempElementUdfValue = element.getChildText(objectAttributeDefs.get(i).getAttributeName());
				if (tempElementUdfValue != null)
				{
					namedValueMap.put(objectAttributeDefs.get(i).getAttributeName(), tempElementUdfValue);
				}
			}

		}
		log.info("UDF SIZE=" + namedValueMap.size());
		return namedValueMap;
	}

	public List<ProductU> setProductUSequence(org.jdom.Document doc) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		if (doc == null)
		{
			log.error("xml is null");
		}

		List<ProductU> productUList = new ArrayList<ProductU>();
		ProductServiceUtil productServiceUtil = (ProductServiceUtil) BundleUtil.getBundleServiceClass(ProductServiceUtil.class);

		Element root = doc.getDocument().getRootElement();

		List<Product> productDatas = new ArrayList<Product>();

		String lotName = root.getChild("Body").getChildText("LOTNAME");
		if (lotName == null || lotName == "")
		{
			String carrierName = root.getChild("Body").getChildText("CARRIERNAME");
			if (carrierName != null && carrierName != "")
			{

				String condition = "WHERE carrierName = ? AND productState != ? AND productState != ? ORDER BY position ";

				Object[] bindSet = new Object[] { carrierName, GenericServiceProxy.getConstantMap().Prod_Scrapped, GenericServiceProxy.getConstantMap().Prod_Consumed };
				try
				{
					productDatas = ProductServiceProxy.getProductService().select(condition, bindSet);
				}
				catch (Exception e)
				{

				}
			}
		}
		else
		{
			try
			{
				productDatas = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);
			}
			catch (Exception e)
			{
				log.error(e);
				productDatas = ProductServiceProxy.getProductService().allProductsByLot(lotName);
			}

		}

		Element element = root.getChild("Body").getChild("PRODUCTLIST");

		if (element != null)
		{
			for (Iterator iterator = element.getChildren().iterator(); iterator.hasNext();)
			{
				Element productE = (Element) iterator.next();
				String productName = productE.getChild("PRODUCTNAME").getText();

				ProductU productU = new ProductU();

				productU.setProductName(productName);
				productU.getUdfs().put("REWORKFLAG", "");

				productUList.add(productU);
			}
		}
		else
		{
			for (Iterator<Product> iteratorProduct = productDatas.iterator(); iteratorProduct.hasNext();)
			{
				Product product = iteratorProduct.next();
				product.getUdfs().put("REWORKFLAG", "");
				
				ProductU productU = new ProductU();
				productU.setProductName(product.getKey().getProductName());
				productU.setUdfs(product.getUdfs());

				productUList.add(productU);
			}
		}

		return productUList;
	}

	public List<ProductU> setProductUSequence(String lotName) throws CustomException
	{
		List<ProductU> productUList = new ArrayList<ProductU>();

		List<Product> productDatas = new ArrayList<Product>();

		try
		{
			productDatas = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);
		}
		catch (Exception e)
		{
			log.error(e);
			productDatas = ProductServiceProxy.getProductService().allProductsByLot(lotName);
		}

		for (Product product : productDatas)
		{
			product.getUdfs().put("REWORKFLAG", "");
			
			ProductU productU = new ProductU();
			
			productU.setProductName(product.getKey().getProductName());
			productU.setUdfs(product.getUdfs());

			productUList.add(productU);
		}

		return productUList;
	}

	private void decrementCrateQuantity(EventInfo eventInfo, Lot lotData, String consumableName, double quantity) throws CustomException
	{
		eventInfo.setEventName("Consume");

		Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(consumableName);

		DecrementQuantityInfo transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().decrementQuantityInfo(lotData.getKey().getLotName(), lotData.getProcessOperationName(),
				lotData.getProcessOperationVersion(), "", TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()), quantity, consumableData.getUdfs());

		MESConsumableServiceProxy.getConsumableServiceImpl().decrementQuantity(consumableData, transitionInfo, eventInfo);
	}

	public String getSamplingFlag(Product productData, List<SampleLot> sampleLot) throws CustomException
	{
		String samplingFlag = GenericServiceProxy.getConstantMap().FLAG_N;

		if (sampleLot != null && sampleLot.size() > 0)
		{
			// Get PRODUCTSAMPLEPOSITION
			String productSamplePositions = sampleLot.get(0).getActualSamplePosition();
			if (StringUtils.isEmpty(productSamplePositions))
				return samplingFlag;

			productSamplePositions = productSamplePositions.replaceAll(" ", ""); // remove blank
			List<String> productSamplePositionList = CommonUtil.splitString(",", productSamplePositions); // delimeter is ','

			for (String productSamplePosition : productSamplePositionList)
			{
				int k = Integer.valueOf(productSamplePosition);

				if (k == productData.getPosition())
				{
					samplingFlag = GenericServiceProxy.getConstantMap().FLAG_Y;
					break;
				}
			}
		}

		// just determine sampling

		return samplingFlag;
	}
	
	public String getSamplingFlag(Product productData, List<SampleLot> sampleLot,boolean isCancel) throws CustomException
	{
		String samplingFlag = GenericServiceProxy.getConstantMap().FLAG_N;
		if (isCancel)
		{
			if (CommonUtil.getValue(productData.getUdfs(), "PROCESSINGINFO").equals("B"))
				samplingFlag = GenericServiceProxy.getConstantMap().FLAG_Y;
			else
				samplingFlag = GenericServiceProxy.getConstantMap().FLAG_N;
		}
		else
		{
			if (sampleLot != null && sampleLot.size() > 0)
			{
				// Get PRODUCTSAMPLEPOSITION
				String productSamplePositions = sampleLot.get(0).getActualSamplePosition();
				if (StringUtils.isEmpty(productSamplePositions))
					return samplingFlag;

				productSamplePositions = productSamplePositions.replaceAll(" ", ""); // remove blank
				List<String> productSamplePositionList = CommonUtil.splitString(",", productSamplePositions); // delimeter is ','

				for (String productSamplePosition : productSamplePositionList)
				{
					int k = Integer.valueOf(productSamplePosition);

					if (k == productData.getPosition())
					{
						samplingFlag = GenericServiceProxy.getConstantMap().FLAG_Y;
						break;
					}
				}
			}

		}

		return samplingFlag;
	}

	public String getSelectionFlag(boolean isCancel, Product productData) throws CustomException
	{
		String samplingFlag = GenericServiceProxy.getConstantMap().FLAG_N;

		if (isCancel)
		{
			if (CommonUtil.getValue(productData.getUdfs(), "PROCESSINGINFO").equals("B"))
				samplingFlag = GenericServiceProxy.getConstantMap().FLAG_Y;
			else
				samplingFlag = GenericServiceProxy.getConstantMap().FLAG_N;
		}
		else
		// normal case
		{
			samplingFlag = GenericServiceProxy.getConstantMap().FLAG_Y;
		}

		return samplingFlag;
	}

	public String getSortJobFlag(Product productData, List<ListOrderedMap> sortJobList) throws CustomException
	{
		String samplingFlag = GenericServiceProxy.getConstantMap().FLAG_N;

		for (ListOrderedMap sortJob : sortJobList)
		{
			String fromProductName = CommonUtil.getValue(sortJob, "PRODUCTNAME");
			String fromPosition = CommonUtil.getValue(sortJob, "FROMPOSITION");
			String fromSlotPosition = CommonUtil.getValue(sortJob, "FROMSLOTPOSITION");

			String slotPosition = productData.getUdfs().get("SLOTPOSITION") == null ? StringUtils.EMPTY : productData.getUdfs().get("SLOTPOSITION").toString();
			// convert
			long position = 0;
			try
			{
				position = Long.parseLong(fromPosition);
			}
			catch (Exception ex)
			{
				log.warn("Position parsing failed");
			}

			if (productData.getKey().getProductName().equals(fromProductName) && productData.getPosition() == position && slotPosition.equals(fromSlotPosition))
			{
				samplingFlag = GenericServiceProxy.getConstantMap().FLAG_Y;

				break;
			}
		}

		return samplingFlag;
	}

	public String getTurnDegree(Product productData, List<ListOrderedMap> sortJobList) throws CustomException
	{
		String turnDegree = StringUtils.EMPTY;

		for (ListOrderedMap sortJob : sortJobList)
		{
			String fromProductName = CommonUtil.getValue(sortJob, "PRODUCTNAME");
			String fromPosition = CommonUtil.getValue(sortJob, "FROMPOSITION");
			String fromSlotPosition = CommonUtil.getValue(sortJob, "FROMSLOTPOSITION");

			String slotPosition = productData.getUdfs().get("SLOTPOSITION") == null ? StringUtils.EMPTY : productData.getUdfs().get("SLOTPOSITION").toString();
			// convert
			long position = 0;
			try
			{
				position = Long.parseLong(fromPosition);
			}
			catch (Exception ex)
			{
				log.warn("Position parsing failed");
			}

			if (productData.getKey().getProductName().equals(fromProductName) && productData.getPosition() == position && slotPosition.equals(fromSlotPosition))
			{
				turnDegree = CommonUtil.getValue(sortJob, "TURNDEGREE");
				if (turnDegree.equals(""))
					turnDegree = "0";
				break;
			}
		}

		return turnDegree;
	}

	public List<Map<String, Object>> getLotListByProductList(List<Element> productList) throws CustomException
	{
		String bindProductList = CommonUtil.makeListForQuery(productList, "PRODUCTNAME");
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT LOTNAME, COUNT (LOTNAME) AS PRODUCTQTY ");
		sql.append("  FROM PRODUCT ");
		sql.append(" WHERE PRODUCTNAME IN ");
		sql.append(" (" + bindProductList + ") ");
		sql.append("GROUP BY LOTNAME ");
		sql.append("ORDER BY PRODUCTQTY DESC ");

		Map<String, Object> bindMap = new HashMap<String, Object>();

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlLotList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		return sqlLotList;
	}

	public List<Map<String, Object>> getLotListByProductList(List<Element> productList, String carrierName) throws CustomException
	{
		String bindProductList = CommonUtil.makeListForQuery(productList, "PRODUCTNAME");

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT LOTNAME, ");
		sql.append("       COUNT (PRODUCTNAME) AS PRODUCTQTY, ");
		sql.append("       CARRIERNAME, ");
		sql.append("       DECODE (CARRIERNAME, :CARRIERNAME, 'Y', 'N') ISSAMECST ");
		sql.append("  FROM PRODUCT ");
		sql.append(" WHERE PRODUCTNAME IN (" + bindProductList + ")");
		sql.append("GROUP BY LOTNAME, CARRIERNAME ");
		sql.append("ORDER BY PRODUCTQTY DESC ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("CARRIERNAME", carrierName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlLotList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		return sqlLotList;
	}

	public List<Element> getNoReportedProductListInLot(String lotName, List<Element> productList) throws CustomException
	{
		String bindProductList = CommonUtil.makeListForQuery(productList, "PRODUCTNAME");

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT PRODUCTNAME, LOTNAME, CARRIERNAME, POSITION ");
		sql.append("  FROM PRODUCT ");
		sql.append(" WHERE LOTNAME = :LOTNAME ");
		sql.append("   AND PRODUCTSTATE NOT IN ('Scrapped', 'Consumed') ");

		if (productList.size() > 0)
		{
			sql.append("   AND PRODUCTNAME NOT IN ");
			sql.append("(" + bindProductList + ") ");
			sql.append("ORDER BY POSITION ");
		}

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("LOTNAME", lotName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlProductList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		List<Element> productListElement = new ArrayList<Element>();
		for (Map<String, Object> productData : sqlProductList)
		{
			Element productElement = new Element("PRODUCT");

			Element lotNameElement = new Element("LOTNAME");
			lotNameElement.setText((String) productData.get("LOTNAME"));
			productElement.addContent(lotNameElement);

			Element productNameElement = new Element("PRODUCTNAME");
			productNameElement.setText((String) productData.get("PRODUCTNAME"));
			productElement.addContent(productNameElement);

			Element positionElement = new Element("POSITION");
			positionElement.setText(String.valueOf(productData.get("POSITION")));
			productElement.addContent(positionElement);

			productListElement.add(productElement);
		}
		return productListElement;
	}

	public boolean isCuttingTrackOut(List<Element> productList) throws CustomException
	{
		boolean isCuttingTrackOut = false;

		if (productList.size() == 0)
			throw new CustomException("LOT-9001", "PRODUCTLIST : " + CommonUtil.makeListForQuery(productList, "PRODUCTNAME"));

		String firstProductType = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productList.get(0).getChildText("PRODUCTNAME")).getProductType();

		for (Element productE : productList)
		{
			String productName = productE.getChildText("PRODUCTNAME");

			Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);

			if (!StringUtils.equals(productData.getProductType(), firstProductType))
				throw new CustomException("PRODUCT-0019");

			if (StringUtils.equals(productData.getProductType(), "Glass"))
			{
				isCuttingTrackOut = true;
			}
		}

		return isCuttingTrackOut;
	}

	public double convertSubProductQuantity(Lot baseLotData, ProductSpec productSpecData, List<Element> productList, int lotQuantity, int productQuantity) throws CustomException
	{
		double subProductUnitQuantity1 = baseLotData.getSubProductUnitQuantity1();

		// when Half Glass Cutting TrackOut
		if (!StringUtils.equals(baseLotData.getFactoryName(), "POSTCELL") && MESLotServiceProxy.getLotServiceUtil().isCuttingTrackOut(productList))
		{
			if(isHalfCutFlag(baseLotData))
				subProductUnitQuantity1 = Double.parseDouble(productSpecData.getUdfs().get("PRODUCTCOUNTTOXAXIS")) * Double.parseDouble(productSpecData.getUdfs().get("PRODUCTCOUNTTOYAXIS")) / 2;
		}

		return subProductUnitQuantity1;
	}

	public boolean changeLotFlag(Lot lotData, String factoryName, String processOperationName, String processOperationVersion) throws CustomException
	{
		boolean changeLotFlag = false;
		ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);

		if (flowData.getProcessFlowType().equals("Main"))
		{
			try
			{
				StringBuilder sql = new StringBuilder();
				sql.append("SELECT CHANGELOTNAME ");
				sql.append("  FROM PROCESSOPERATIONSPEC ");
				sql.append(" WHERE FACTORYNAME = :FACTORYNAME ");
				sql.append("   AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
				sql.append("   AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");

				Map<String, String> args = new HashMap<String, String>();
				args.put("FACTORYNAME", factoryName);
				args.put("PROCESSOPERATIONNAME", processOperationName);
				args.put("PROCESSOPERATIONVERSION", processOperationVersion);

				@SuppressWarnings("unchecked")
				List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

				if (result.size() > 0)
				{
					String changeLotName = ConvertUtil.getMapValueByName(result.get(0), "CHANGELOTNAME");

					if (StringUtils.equals(changeLotName, "Y"))
					{
						changeLotFlag = true;
					}
				}
			}
			catch (Exception e)
			{
			}
		}

		return changeLotFlag;
	}

	public Lot createNewLotWithSplit(EventInfo eventInfo, Lot baseLotData, ProductSpec productSpecData, double subProductUnitQuantity1, String carrierName, Map<String, String> assignCarrierUdfs)
			throws CustomException
	{
		String newLotName = "";
		
		String namingRule = "";

		if (StringUtils.equals(productSpecData.getProductionType(), "P"))
			namingRule = "ProductionLotNaming";
		else
			namingRule = "LotNaming";

		if (StringUtils.isEmpty(productSpecData.getUdfs().get("PRODUCTCODE")))
			throw new CustomException("NAMING-0002", productSpecData.getKey().getProductSpecName());
		
		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("PRODUCTCODE", productSpecData.getUdfs().get("PRODUCTCODE"));
		nameRuleAttrMap.put("PRODUCTIONTYPE", baseLotData.getProductionType());
		nameRuleAttrMap.put("PRODUCTSPECTYPE", productSpecData.getUdfs().get("PRODUCTSPECTYPE"));

		try
		{
			List<String> lstName = CommonUtil.generateNameByNamingRule(namingRule, nameRuleAttrMap, 1);
			newLotName = lstName.get(0);
		}
		catch (Exception ex)
		{
			new CustomException("LOT-9011", ex.getMessage());
		}

		List<ProductP> productPSequence = new ArrayList<ProductP>();
		CreateWithParentLotInfo createWithParentLotInfo = MESLotServiceProxy.getLotInfoUtil().createWithParentLotInfo(baseLotData.getAreaName(), "Y", assignCarrierUdfs, carrierName,
				baseLotData.getDueDate(), baseLotData.getFactoryName(), baseLotData.getLastLoggedInTime(), baseLotData.getLastLoggedInUser(), baseLotData.getLastLoggedOutTime(),
				baseLotData.getLastLoggedOutUser(), baseLotData.getLotGrade(), baseLotData.getLotHoldState(), newLotName, baseLotData.getLotProcessState(), baseLotData.getLotState(),
				baseLotData.getMachineName(), baseLotData.getMachineRecipeName(), baseLotData.getNodeStack(), baseLotData.getOriginalLotName(), baseLotData.getPriority(),
				baseLotData.getProcessFlowName(), baseLotData.getProcessFlowVersion(), baseLotData.getProcessGroupName(), baseLotData.getProcessOperationName(),
				baseLotData.getProcessOperationVersion(), baseLotData.getProductionType(), productPSequence, 0, baseLotData.getProductRequestName(), baseLotData.getProductSpec2Name(),
				baseLotData.getProductSpec2Version(), baseLotData.getProductSpecName(), baseLotData.getProductSpecVersion(), baseLotData.getProductType(), baseLotData.getReworkCount(), "",
				baseLotData.getReworkNodeId(), baseLotData.getRootLotName(), baseLotData.getKey().getLotName(), baseLotData.getSubProductType(), subProductUnitQuantity1,
				baseLotData.getSubProductUnitQuantity2(), baseLotData);

		// Set ReturnInfo
		createWithParentLotInfo.getUdfs().put("RETURNFLOWNAME", baseLotData.getUdfs().get("RETURNFLOWNAME"));
		createWithParentLotInfo.getUdfs().put("RETURNOPERATIONNAME", baseLotData.getUdfs().get("RETURNOPERATIONNAME"));
		createWithParentLotInfo.getUdfs().put("RETURNOPERATIONVER", baseLotData.getUdfs().get("RETURNOPERATIONVERSION"));
		createWithParentLotInfo.getUdfs().put("ARRAYLOTNAME", baseLotData.getUdfs().get("ARRAYLOTNAME"));
		createWithParentLotInfo.getUdfs().put("LASTFACTORYNAME", baseLotData.getUdfs().get("LASTFACTORYNAME"));
		createWithParentLotInfo.getUdfs().put("RECEIVELOTNAME", baseLotData.getUdfs().get("RECEIVELOTNAME"));
		createWithParentLotInfo.getUdfs().put("LOTNOTE", baseLotData.getUdfs().get("LOTNOTE"));
		
		eventInfo = EventInfoUtil.makeEventInfo("Create", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);

		Lot newLotData = MESLotServiceProxy.getLotServiceImpl().createWithParentLot(eventInfo, newLotName, createWithParentLotInfo);

		return newLotData;
	}

	public Lot createPostCellNewLot(EventInfo eventInfo, Lot baseLotData, String newLotName, ProductSpec productSpecData, double subProductUnitQuantity1, String carrierName,
			Map<String, String> assignCarrierUdfs) throws CustomException
	{
		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("PRODUCTIONTYPE", baseLotData.getProductionType());
		nameRuleAttrMap.put("PRODUCTSPECTYPE", productSpecData.getUdfs().get("PRODUCTSPECTYPE"));

		try
		{
			List<String> lstName = CommonUtil.generateNameByNamingRule("PostCellLotNaming", nameRuleAttrMap, 1);
			newLotName = lstName.get(0);
		}
		catch (Exception ex)
		{
			new CustomException("LOT-9011", ex.getMessage());
		}

		List<ProductP> productPSequence = new ArrayList<ProductP>();
		CreateWithParentLotInfo createWithParentLotInfo = MESLotServiceProxy.getLotInfoUtil().createWithParentLotInfo(baseLotData.getAreaName(), "Y", assignCarrierUdfs, carrierName,
				baseLotData.getDueDate(), baseLotData.getFactoryName(), baseLotData.getLastLoggedInTime(), baseLotData.getLastLoggedInUser(), baseLotData.getLastLoggedOutTime(),
				baseLotData.getLastLoggedOutUser(), baseLotData.getLotGrade(), baseLotData.getLotHoldState(), newLotName, baseLotData.getLotProcessState(), baseLotData.getLotState(),
				baseLotData.getMachineName(), baseLotData.getMachineRecipeName(), baseLotData.getNodeStack(), baseLotData.getOriginalLotName(), baseLotData.getPriority(),
				baseLotData.getProcessFlowName(), baseLotData.getProcessFlowVersion(), baseLotData.getProcessGroupName(), baseLotData.getProcessOperationName(),
				baseLotData.getProcessOperationVersion(), baseLotData.getProductionType(), productPSequence, 0, baseLotData.getProductRequestName(), baseLotData.getProductSpec2Name(),
				baseLotData.getProductSpec2Version(), baseLotData.getProductSpecName(), baseLotData.getProductSpecVersion(), baseLotData.getProductType(), baseLotData.getReworkCount(), "",
				baseLotData.getReworkNodeId(), baseLotData.getRootLotName(), baseLotData.getKey().getLotName(), baseLotData.getSubProductType(), subProductUnitQuantity1,
				baseLotData.getSubProductUnitQuantity2(), baseLotData);
		
		// Set ReturnInfo
		createWithParentLotInfo.getUdfs().put("RETURNFLOWNAME", baseLotData.getUdfs().get("RETURNFLOWNAME"));
		createWithParentLotInfo.getUdfs().put("RETURNOPERATIONNAME", baseLotData.getUdfs().get("RETURNOPERATIONNAME"));
		createWithParentLotInfo.getUdfs().put("RETURNOPERATIONVER", baseLotData.getUdfs().get("RETURNOPERATIONVERSION"));
		createWithParentLotInfo.getUdfs().put("LOTNOTE", baseLotData.getUdfs().get("LOTNOTE"));
		
		eventInfo = EventInfoUtil.makeEventInfo("Create", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);

		Lot newLotData = MESLotServiceProxy.getLotServiceImpl().createWithParentLot(eventInfo, newLotName, createWithParentLotInfo);

		return newLotData;
	}

	public Lot createNewLot(EventInfo eventInfo, Lot srcLotData, String carrierName, Map<String, String> assignCarrierUdfs, int lotCountByProductList, List<Element> productList)
			throws CustomException
	{
		String newLotName = "";

		ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(srcLotData.getFactoryName(), srcLotData.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
		
		String namingRuleName = "";
		
		if (StringUtils.equals(productSpecData.getProductionType(), "P"))
			namingRuleName = "ProductionLotNaming";
		else
			namingRuleName = "LotNaming";

		if (StringUtils.isEmpty(productSpecData.getUdfs().get("PRODUCTCODE")))
			throw new CustomException("NAMING-0002", productSpecData.getKey().getProductSpecName());
		
		ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(srcLotData);

		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("PRODUCTCODE", productSpecData.getUdfs().get("PRODUCTCODE"));
		nameRuleAttrMap.put("PRODUCTIONTYPE", srcLotData.getProductionType());
		nameRuleAttrMap.put("PRODUCTSPECTYPE", productSpecData.getUdfs().get("PRODUCTSPECTYPE"));

		if (StringUtils.equals(srcLotData.getFactoryName(), "POSTCELL") || StringUtils.equals(processFlowData.getProcessFlowType(), "Sort"))
		{
			namingRuleName = "SplitLotNaming";
			nameRuleAttrMap.put("LOTNAME", srcLotData.getKey().getLotName());
		}

		try
		{
			List<String> lstName = CommonUtil.generateNameByNamingRule(namingRuleName, nameRuleAttrMap, 1);
			newLotName = lstName.get(0);
		}
		catch (Exception ex)
		{
			new CustomException("LOT-9011", ex.getMessage());
		}

		// when Q-Glass Cutting TrackOut
		double subProductUnitQuantity1 = MESLotServiceProxy.getLotServiceUtil().convertSubProductQuantity(srcLotData, productSpecData, productList, lotCountByProductList, productList.size());

		List<ProductP> productPSequence = new ArrayList<ProductP>();
		CreateWithParentLotInfo createWithParentLotInfo = MESLotServiceProxy.getLotInfoUtil().createWithParentLotInfo(srcLotData.getAreaName(), "Y", assignCarrierUdfs, carrierName,
				srcLotData.getDueDate(), srcLotData.getFactoryName(), srcLotData.getLastLoggedInTime(), srcLotData.getLastLoggedInUser(), srcLotData.getLastLoggedOutTime(),
				srcLotData.getLastLoggedOutUser(), srcLotData.getLotGrade(), srcLotData.getLotHoldState(), newLotName, srcLotData.getLotProcessState(), srcLotData.getLotState(),
				srcLotData.getMachineName(), srcLotData.getMachineRecipeName(), srcLotData.getNodeStack(), srcLotData.getOriginalLotName(), srcLotData.getPriority(), srcLotData.getProcessFlowName(),
				srcLotData.getProcessFlowVersion(), srcLotData.getProcessGroupName(), srcLotData.getProcessOperationName(), srcLotData.getProcessOperationVersion(), srcLotData.getProductionType(),
				productPSequence, 0, srcLotData.getProductRequestName(), srcLotData.getProductSpec2Name(), srcLotData.getProductSpec2Version(), srcLotData.getProductSpecName(),
				srcLotData.getProductSpecVersion(), srcLotData.getProductType(), srcLotData.getReworkCount(),
				srcLotData.getReworkState().equals(GenericServiceProxy.getConstantMap().Lot_InRework) ? "Y" : "N", srcLotData.getReworkNodeId(), srcLotData.getRootLotName(),
				srcLotData.getKey().getLotName(), srcLotData.getSubProductType(), subProductUnitQuantity1, srcLotData.getSubProductUnitQuantity2(), srcLotData);

		// Set ReturnInfo
		createWithParentLotInfo.getUdfs().put("RETURNFLOWNAME", srcLotData.getUdfs().get("RETURNFLOWNAME"));
		createWithParentLotInfo.getUdfs().put("RETURNOPERATIONNAME", srcLotData.getUdfs().get("RETURNOPERATIONNAME"));
		createWithParentLotInfo.getUdfs().put("RETURNOPERATIONVER", srcLotData.getUdfs().get("RETURNOPERATIONVERSION"));
		createWithParentLotInfo.getUdfs().put("ARRAYLOTNAME", srcLotData.getUdfs().get("ARRAYLOTNAME"));
		createWithParentLotInfo.getUdfs().put("MERGEABLEFLAG", srcLotData.getUdfs().get("MERGEABLEFLAG"));
		createWithParentLotInfo.getUdfs().put("LASTFACTORYNAME", srcLotData.getUdfs().get("LASTFACTORYNAME"));
		createWithParentLotInfo.getUdfs().put("RECEIVELOTNAME", srcLotData.getUdfs().get("RECEIVELOTNAME"));
		createWithParentLotInfo.getUdfs().put("LOTNOTE", srcLotData.getUdfs().get("LOTNOTE"));
		
		eventInfo = EventInfoUtil.makeEventInfo("Create", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);

		Lot newLotData = MESLotServiceProxy.getLotServiceImpl().createWithParentLot(eventInfo, newLotName, createWithParentLotInfo);

		return newLotData;
	}

	public void transferProductsToLot(EventInfo eventInfo, Lot newLotData, Port portData, String srcLotName, String sProductQuantity, Map<String, String> deassignCarrierUdfs, List<Element> productList)
			throws CustomException
	{
		// transferProductsToLot(Assign the Products To New Lot)
		eventInfo = EventInfoUtil.makeEventInfo("TransferProduct", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);

		double dProductQuantity = Double.parseDouble(sProductQuantity);
		Lot srcLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(srcLotName);

		List<ProductP> productPSequence = MESLotServiceProxy.getLotInfoUtil().setProductPSequence(productList, srcLotName);
		
		MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, srcLotData, newLotData.getKey().getLotName(), dProductQuantity, productPSequence, "N", newLotData.getUdfs(),
				deassignCarrierUdfs, srcLotData.getUdfs());

		srcLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(srcLotName);

		if (srcLotData.getProductQuantity() == 0)
		{
			if (StringUtils.isNotEmpty(srcLotData.getCarrierName()))
			{
				Durable sLotDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(srcLotData.getCarrierName());
				deassignCarrierUdfs = sLotDurableData.getUdfs();
			}

			eventInfo.setEventName("MakeEmptied");
			MESLotServiceProxy.getLotServiceImpl().MakeEmptied(eventInfo, srcLotData, new ArrayList<ProductU>(), deassignCarrierUdfs);
			deassignCarrierUdfs.clear();
		}
	}

	//2021-04-07 ghhan Mantis 0000471
	public void transferProductsToLotForBuffer(EventInfo eventInfo, Lot newLotData, Port portData, String srcLotName, String sProductQuantity, Map<String, String> deassignCarrierUdfs, List<Element> productList)
			throws CustomException
	{
		// transferProductsToLot(Assign the Products To New Lot)
		EventInfo LPSEventInfo = EventInfoUtil.makeEventInfo_BufferMix("TransferProduct", eventInfo.getEventUser(), eventInfo.getEventComment(), eventInfo.getReasonCodeType(), eventInfo.getReasonCode());

		double dProductQuantity = Double.parseDouble(sProductQuantity);
		Lot srcLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(srcLotName);

		List<ProductP> productPSequence = MESLotServiceProxy.getLotInfoUtil().setProductPSequence(productList, srcLotName);
		
		MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(LPSEventInfo, srcLotData, newLotData.getKey().getLotName(), dProductQuantity, productPSequence, "N", newLotData.getUdfs(),
				deassignCarrierUdfs, srcLotData.getUdfs());

		srcLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(srcLotName);

		if (srcLotData.getProductQuantity() == 0)
		{
			if (StringUtils.isNotEmpty(srcLotData.getCarrierName()))
			{
				Durable sLotDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(srcLotData.getCarrierName());
				deassignCarrierUdfs = sLotDurableData.getUdfs();
			}

			eventInfo.setEventName("MakeEmptied");
			MESLotServiceProxy.getLotServiceImpl().MakeEmptied(eventInfo, srcLotData, new ArrayList<ProductU>(), deassignCarrierUdfs);
			deassignCarrierUdfs.clear();
		}
	}
	
	public void transferProductsToLotForDummy(EventInfo eventInfo, Lot newLotData, Port portData, String srcLotName, String sProductQuantity, Map<String, String> deassignCarrierUdfs,
			List<Element> productList, Lot destLotData, String offSet, String srcProductRequestName, String srcProductSpecName) throws CustomException
	{
		// transferProductsToLot(Assign the Products To New Lot)
		eventInfo = EventInfoUtil.makeEventInfo("TransferProduct", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);

		double dProductQuantity = Double.parseDouble(sProductQuantity);
		Lot srcLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(srcLotName);

		List<ProductP> productPSequence = MESLotServiceProxy.getLotInfoUtil().setProductPSequenceForDummy(eventInfo, productList, srcLotData, destLotData, offSet, srcProductRequestName, srcProductSpecName);

		MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, srcLotData, newLotData.getKey().getLotName(), dProductQuantity, productPSequence, "N", newLotData.getUdfs(),
				deassignCarrierUdfs, srcLotData.getUdfs());

		srcLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(srcLotName);

		if (srcLotData.getProductQuantity() == 0)
		{
			if (StringUtils.isNotEmpty(srcLotData.getCarrierName()))
			{
				Durable sLotDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(srcLotData.getCarrierName());
				deassignCarrierUdfs = sLotDurableData.getUdfs();
			}

			eventInfo.setEventName("MakeEmptied");
			MESLotServiceProxy.getLotServiceImpl().MakeEmptied(eventInfo, srcLotData, new ArrayList<ProductU>(), deassignCarrierUdfs);
			deassignCarrierUdfs.clear();
		}

		// Increase Dest WorkOrder Quantity
		MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementQuantity(eventInfo, destLotData, productPSequence.size());
	}

	public Lot completeRework(EventInfo eventInfo, Lot beforeTrackOutLot, Lot afterTrackOutLot, List<Element> productList) throws CustomException
	{
		// Complete Rework
		ProcessFlow beforeTrackOutPFData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(beforeTrackOutLot);
		ProcessFlow afterTrackOutPFData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(afterTrackOutLot);

		if ((StringUtils.equals(afterTrackOutLot.getReworkState(), "InRework") && StringUtils.equals(beforeTrackOutPFData.getProcessFlowType(), "Rework") 
				&& !StringUtils.equals(afterTrackOutPFData.getProcessFlowType(), "Rework"))
				|| (StringUtils.equals(afterTrackOutLot.getReworkState(), "InRework") && StringUtils.equals(beforeTrackOutPFData.getProcessFlowType(), "Strip") 
						&& !StringUtils.equals(afterTrackOutPFData.getProcessFlowType(), "Strip")))
		{
			// eventInfo.setEventName("CompleteRework");
			eventInfo = EventInfoUtil.makeEventInfo("CompleteRework", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);

			List<ProductU> productU = MESLotServiceProxy.getLotServiceUtil().setProductUSequence(afterTrackOutLot.getKey().getLotName());

			MakeNotInReworkInfo makeNotInReworkInfo = MESLotServiceProxy.getLotInfoUtil().makeNotInReworkInfo(afterTrackOutLot, eventInfo, afterTrackOutLot.getKey().getLotName(),
					afterTrackOutLot.getProcessFlowName(), afterTrackOutLot.getProcessOperationName(), afterTrackOutLot.getProcessOperationVersion(), new HashMap<String, String>(), productU);

			Lot completeReworkLot = MESLotServiceProxy.getLotServiceImpl().completeRework(eventInfo, afterTrackOutLot, makeNotInReworkInfo);

			MESLotServiceProxy.getLotServiceUtil().deleteSamplingForCompleteRework(eventInfo, beforeTrackOutLot, productList, false);
			
			try
			{
				eventInfo.setEventName("ChangeGrade");
				MESLotServiceProxy.getLotServiceUtil().recoverLotGrade(eventInfo, completeReworkLot, "R", "G");
			}
			catch (Exception ex)
			{
				log.warn("Judge recovery is failed");
			}

			try
			{
				ExtendedObjectProxy.getMQCPlanService().makeWaiting(eventInfo, completeReworkLot);
			}
			catch (Exception ex)
			{
				log.warn("post MQC processing is failed");
			}
			
			if (StringUtils.isEmpty(completeReworkLot.getUdfs().get("JOBNAME"))) //Add by jhyeom, 21-03-05, Mantis #0000464
			{
				eventInfo.setEventName("Hold");
				eventInfo.setReasonCode("SYSTEM-REWORK");
				eventInfo.setEventComment("Complete Rework [" + completeReworkLot.getKey().getLotName() + "], " + eventInfo.getEventComment());
				
				Map<String, String> udfs = new HashMap<String, String>();
				
				if (StringUtil.equals(completeReworkLot.getLotState(), GenericServiceProxy.getConstantMap().Lot_Released) && StringUtil.equals(completeReworkLot.getLotProcessState(), "WAIT"))
				{
					completeReworkLot = MESLotServiceProxy.getLotServiceImpl().lotMultiHoldR(eventInfo, completeReworkLot, udfs);
				}
				else
				{
					throw new CustomException("LOT-0113", completeReworkLot.getLotState(), completeReworkLot.getLotProcessState());
				}
			}
			

			return completeReworkLot;
		}
		else
		{
			return afterTrackOutLot;
		}
	}

	public Lot completeRework(EventInfo eventInfo, Lot afterTrackOutLot, List<Element> productList) throws CustomException
	{
		if (StringUtils.equals(afterTrackOutLot.getReworkState(), "InRework"))
		{
			Map<String, String> udfs = new HashMap<String, String>();
			List<ProductU> productU = MESLotServiceProxy.getLotServiceUtil().setProductUSequence(afterTrackOutLot.getKey().getLotName());

			List<SampleLot> sampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListBySpec(afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
					afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion());

			MakeNotInReworkInfo makeNotInReworkInfo = MESLotServiceProxy.getLotInfoUtil().makeNotInReworkInfo(afterTrackOutLot, eventInfo, afterTrackOutLot.getKey().getLotName(),
					sampleLot.get(0).getReturnProcessFlowName(), sampleLot.get(0).getReturnOperationName(), sampleLot.get(0).getReturnOperationVersion(), udfs, productU);

			Lot completeReworkLot = MESLotServiceProxy.getLotServiceImpl().completeRework(eventInfo, afterTrackOutLot, makeNotInReworkInfo);

			try
			{
				eventInfo.setEventName("ChangeGrade");
				MESLotServiceProxy.getLotServiceUtil().recoverLotGradeForPL(eventInfo, completeReworkLot, "G");
			}
			catch (Exception ex)
			{
				log.warn("Judge recovery is failed");
			}

			return completeReworkLot;
		}
		else
		{
			return afterTrackOutLot;
		}
	}
	
	public NodeStack getLotNodeStackForProcessEnd (Node sampleNode, Lot trackOutLot) throws CustomException
	{
		// Move to Sampling Flow
		String returnNodeId = NodeStack.getNodeID(sampleNode.getFactoryName(), sampleNode.getUdfs().get("RETURNPROCESSFLOWNAME"), sampleNode.getUdfs().get("RETURNPROCESSFLOWVERSION"), sampleNode.getUdfs().get("RETURNOPERATIONNAME"), sampleNode.getUdfs().get("RETURNOPERATIONVERSION"));

		String currentNodeStack = trackOutLot.getNodeStack();
		String[] nodeStackArray = StringUtils.split(currentNodeStack, ".");

		String sampleNodeId = sampleNode.getKey().getNodeId();
		NodeStack lotNodeStack = new NodeStack();

		if (nodeStackArray.length > 2)
		{
			nodeStackArray[nodeStackArray.length - 1] = sampleNodeId;
			nodeStackArray[nodeStackArray.length - 2] = returnNodeId;

			for (int idx = 0; nodeStackArray.length > idx; idx++)
			{
				lotNodeStack.add(nodeStackArray[idx]);
			}
		}
		else
		{
			lotNodeStack = NodeStack.stringToNodeStack(returnNodeId);
			lotNodeStack.add(sampleNodeId);
		}
		
		return lotNodeStack;
	}
	

	public Lot trackOutLotWithSampling(EventInfo eventInfo, Lot trackOutLot, Port portData, String carrierName, String lotJudge, String machineName, String reworkFlag,
			List<ProductPGSRC> productPGSRCSequence, Map<String, String> assignCarrierUdfs, Map<String, String> deassignCarrierUdfs, Map<String, String> udfs) throws CustomException
	{
		boolean reworkCheckFlag = false;
		
		Map<String, String> trackOutLotUdfs = trackOutLot.getUdfs();
		
		udfs.put("PORTNAME", portData.getKey().getPortName());
		udfs.put("PORTTYPE", CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"));
		udfs.put("PORTUSETYPE", CommonUtil.getValue(portData.getUdfs(), "PORTUSETYPE"));
		udfs.put("BEFOREOPERATIONNAME", trackOutLot.getProcessOperationName());
		udfs.put("BEFOREFLOWNAME", trackOutLot.getProcessFlowName());

		String nodeStack               = "";
		String processFlowName 		   = "";
		String processFlowVersion      = "";
		String processOperationName    = "";
		String processOperationVersion = "";

		
		//Get Next NodeInfo (Main, Rework, Strip, Inspection(Multi Operation)) 
		//if CurrentFlow is Sample → Get FirstNode 
		Node nextNode = PolicyUtil.getNextOperation(trackOutLot); 
		
		//Get Next SampleNodeInfo (Sample(Inspection))
		Node sampleNode = getSamplingInfo(trackOutLot, nextNode); 
		
		
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		if (sampleNode != null)
		{
			// Move to Sampling Flow
			NodeStack lotNodeStack = getLotNodeStackForProcessEnd(sampleNode, trackOutLot);
			
			// Set NextInfo
			nodeStack = NodeStack.nodeStackToString(lotNodeStack); //Set SampleNodeID for makeLoggedOut (Ex. NodeID.NodeID)
			processFlowName = sampleNode.getProcessFlowName();
			processFlowVersion = sampleNode.getProcessFlowVersion();
			processOperationName = sampleNode.getNodeAttribute1();
			processOperationVersion = sampleNode.getNodeAttribute2();
			
			// Set ReturnInfo
			udfs.put("RETURNFLOWNAME", sampleNode.getUdfs().get("RETURNPROCESSFLOWNAME").toString());
			udfs.put("RETURNOPERATIONNAME", sampleNode.getUdfs().get("RETURNOPERATIONNAME"));
			udfs.put("RETURNOPERATIONVER", sampleNode.getUdfs().get("RETURNOPERATIONVERSION"));
		}
		else
		{
			// Get ReserveRepair Node Data
//			Node reserveRepairNode = ExtendedObjectProxy.getReserveRepairProductService().getReserveRepairNodeData(trackOutLot);
			
			// Get ProcessFlow Data
			ProcessFlow nextNodeFlowData   = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(nextNode);
			ProcessFlow currentFlowData    = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(trackOutLot);
			ProcessFlow reworkNodeFlowData = new ProcessFlow();

			if (StringUtils.equals(currentFlowData.getProcessFlowType(), "Rework") || StringUtils.equals(currentFlowData.getProcessFlowType(), "Strip"))
				reworkNodeFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(nextNode);

			
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// Case #1  finishing Rework or Strip 
			if ((StringUtils.equals(currentFlowData.getProcessFlowType(), "Rework") 
					&& nextNode.getProcessFlowName().equals(CommonUtil.getValue(trackOutLotUdfs, "RETURNFLOWNAME")) && nextNode.getNodeAttribute1().equals(CommonUtil.getValue(trackOutLotUdfs, "RETURNOPERATIONNAME"))) 
						|| (StringUtils.equals(currentFlowData.getProcessFlowType(), "Strip") 
								&& nextNode.getProcessFlowName().equals(CommonUtil.getValue(trackOutLotUdfs, "RETURNFLOWNAME")) && nextNode.getNodeAttribute1().equals(CommonUtil.getValue(trackOutLotUdfs, "RETURNOPERATIONNAME"))))
			{
				String currentNode  = trackOutLot.getNodeStack(); //Get current NodeID
				String originalNode = currentNode.substring(0, currentNode.lastIndexOf(".")); //Get original NodeID
				String[][] orginalNodeResult = CommonUtil.getNodeInfo(originalNode); //Get original NodeInfo
				nodeStack = originalNode; //Set NodeID for makeLoggedOut(TrackOut)

				// Set ReturnInfo
				if (orginalNodeResult.length > 0)
				{
					udfs.put("RETURNFLOWNAME", orginalNodeResult[0][1]);
					udfs.put("RETURNOPERATIONNAME", orginalNodeResult[0][2]);
					udfs.put("RETURNOPERATIONVER", orginalNodeResult[0][3]);
				}
				
				if(StringUtils.equals(currentFlowData.getProcessFlowType(), "Rework"))
				{
					reworkCheckFlag = true;
				}
			}
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// Case #2  beginning ReworkFlow or StripFlow
			else if ((StringUtils.equals(currentFlowData.getProcessFlowType(), "Rework") && StringUtils.equals(reworkNodeFlowData.getProcessFlowType(), "Rework"))
							|| ((StringUtils.equals(currentFlowData.getProcessFlowType(), "Strip") && StringUtils.equals(reworkNodeFlowData.getProcessFlowType(), "Strip"))))
			{
				String currentNode = trackOutLot.getNodeStack(); //Get current NodeID
				String originalNode = currentNode.substring(0, currentNode.lastIndexOf(".")); //Get original NodeID
				nodeStack = originalNode + "." + nextNode.getKey().getNodeId(); //Set NodeID for makeLoggedOut(TrackOut) / Make ReworkNode or StripNode
			}
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// Case #3  Return MainFlow
			else if (StringUtils.equals(nextNodeFlowData.getProcessFlowType(), "Main") 
						&& nextNode.getProcessFlowName().equals(CommonUtil.getValue(trackOutLotUdfs, "RETURNFLOWNAME"))
							&& nextNode.getNodeAttribute1().equals(CommonUtil.getValue(trackOutLotUdfs, "RETURNOPERATIONNAME")))
			{
				nodeStack = "";
				udfs.put("RETURNFLOWNAME", "");
				udfs.put("RETURNOPERATIONNAME", "");
				udfs.put("RETURNOPERATIONVER", "");
				udfs.put("BACKUPMAINOPERNAME", "");
				udfs.put("BACKUPMAINFLOWNAME", "");
			}
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// Case #4  MQC
			else if (StringUtils.equals(nextNodeFlowData.getProcessFlowType(), "MQC"))
			{
				nextNode = skipOperForMQC(eventInfo, trackOutLot, nextNode.getNodeAttribute1(), nextNode.getNodeAttribute2());

				if (nextNode != null)
				{
					nodeStack = nextNode.getKey().getNodeId();
					processFlowName = nextNode.getProcessFlowName();
					processFlowVersion = nextNode.getProcessFlowVersion();
					processOperationName = nextNode.getNodeAttribute1();
					processOperationVersion = nextNode.getNodeAttribute2();
				}
				else
				{
					// MQC Flow bug fixed - TrackOut from Sorter Flow to MQC flow (Set ReturnFlow, ReturnOperation).
					if (StringUtils.equals(currentFlowData.getProcessFlowType(), "Sort"))
					{
						// Set Return Info
						String returnFlow = "";
						String returnOperName = "";
						String returnOperVersion = "";
						String[] nodeStackArray = StringUtils.split(trackOutLot.getNodeStack(), ".");

						int idx = nodeStackArray.length;

						if (idx > 2)
						{
							try
							{
								Node returnNode = ProcessFlowServiceProxy.getNodeService().getNode(nodeStackArray[0]);
								returnFlow = returnNode.getProcessFlowName();
								returnOperName = returnNode.getNodeAttribute1();
								returnOperVersion = returnNode.getNodeAttribute2();

								udfs.put("RETURNFLOWNAME", returnFlow);
								udfs.put("RETURNOPERATIONNAME", returnOperName);
								udfs.put("RETURNOPERATIONVER", returnOperVersion);
							}
							catch (Exception e)
							{
							}
						}
					}
				}
			}
			else if(ExtendedObjectProxy.getDummyProductReserveService().checkDummyProductReserveData(trackOutLot.getKey().getLotName()))
			{
				nextNode = getNextNodeForDummy(eventInfo, trackOutLot);
				nodeStack = nextNode.getKey().getNodeId();
			}
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// Case #5  CO-INS-0017-01 ReserveRepair
//			else if(reserveRepairNode != null)
//			{
//				// Move to Repair Flow
//				NodeStack lotNodeStack = getLotNodeStackForProcessEnd(reserveRepairNode, trackOutLot);
//				
//				// Set NextInfo
//				nodeStack = NodeStack.nodeStackToString(lotNodeStack); //Set RepairNodeId for makeLoggedOut (Ex. NodeID.NodeID)
//				processFlowName = reserveRepairNode.getProcessFlowName();
//				processFlowVersion = reserveRepairNode.getProcessFlowVersion();
//				processOperationName = reserveRepairNode.getNodeAttribute1();
//				processOperationVersion = reserveRepairNode.getNodeAttribute2();
//				
//				// Set ReturnInfo
//				udfs.put("RETURNFLOWNAME", reserveRepairNode.getUdfs().get("RETURNPROCESSFLOWNAME"));
//				udfs.put("RETURNOPERATIONNAME", reserveRepairNode.getUdfs().get("RETURNOPERATIONNAME"));
//				udfs.put("RETURNOPERATIONVER", reserveRepairNode.getUdfs().get("RETURNOPERATIONVERSION"));
//			}
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// Case #6  Normal (Set NodeID is Null → NextNode will be found by API(MakeLoggedOut))  
			else
			{
				if (nextNode.getProcessFlowName().equals(CommonUtil.getValue(trackOutLotUdfs, "RETURNFLOWNAME")) && nextNode.getNodeAttribute1().equals(CommonUtil.getValue(trackOutLotUdfs, "RETURNOPERATIONNAME")))
				{
					String currentNode = trackOutLot.getNodeStack();

					if (StringUtils.isNotEmpty(currentNode) && currentNode.indexOf(".") > 0)
					{
						Map<String, String> mqcRecycleFlow = isMQCRecycle(eventInfo, trackOutLot, nextNodeFlowData.getProcessFlowType());
						eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
						eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

						// MQC
						if (mqcRecycleFlow != null)
						{
							Node returnMQCNode = setReturnNodeStackForRecycleMQC(eventInfo, trackOutLot, trackOutLot.getKey().getLotName(), mqcRecycleFlow.get("RECYCLEFLOWNAME"), mqcRecycleFlow.get("RECYCLEFLOWVERSION"), nextNode);

							if (returnMQCNode != null)
							{
								nodeStack = returnMQCNode.getKey().getNodeId();
								processFlowName = returnMQCNode.getProcessFlowName();
								processFlowVersion = returnMQCNode.getProcessFlowVersion();
								processOperationName = returnMQCNode.getNodeAttribute1();
								processOperationVersion = returnMQCNode.getNodeAttribute2();
							}
						}
						else
						{ 
							String originalNode = currentNode.substring(0, currentNode.lastIndexOf("."));

							if (originalNode.lastIndexOf(".") > -1)
							{
								String[] nodeStackArray = StringUtils.split(originalNode, ".");
								originalNode = nodeStackArray[nodeStackArray.length - 2];
							}

							String[][] orginalNodeResult = CommonUtil.getNodeInfo(originalNode); //Get original NodeInfo
							
							if (orginalNodeResult.length > 0)
							{
								udfs.put("RETURNFLOWNAME", orginalNodeResult[0][1]);
								udfs.put("RETURNOPERATIONNAME", orginalNodeResult[0][2]);
								udfs.put("RETURNOPERATIONVER", orginalNodeResult[0][3]);
							}
						}
					}
				}
				else //Set NodeID is Null → NextNode will be found by API(MakeLoggedOut)
				{
					nodeStack = "";
				}
			}
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		
		
		
		// rework grade should go to rework
		MakeLoggedOutInfo makeLoggedOutInfo = MESLotServiceProxy.getLotInfoUtil().makeLoggedOutInfo(trackOutLot, trackOutLot.getAreaName(), 
				assignCarrierUdfs, carrierName, "", deassignCarrierUdfs, StringUtils.isEmpty(lotJudge) ? trackOutLot.getLotGrade() : lotJudge, 
						machineName, "", nodeStack, processFlowName, processFlowVersion, processOperationName, processOperationVersion, productPGSRCSequence, 
							reworkFlag, reworkFlag.equals(GenericServiceProxy.getConstantMap().Flag_Y) ? nodeStack : "", udfs);

		eventInfo.setEventName("TrackOut");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		// TrackOut
		Lot afterTrackOutLot = MESLotServiceProxy.getLotServiceImpl().trackOutLot(eventInfo, trackOutLot, makeLoggedOutInfo);

		//mark by yueke 20210824
		/*
		if(reworkCheckFlag)
		{
			MESProductServiceProxy.getProductServiceUtil().checkReworkCountLimitForLotEnd(eventInfo, trackOutLot);
		}
		*/
		
		return afterTrackOutLot;
	}

	public Lot trackOutLotForceSampling(EventInfo eventInfo, Lot trackOutLot, Port portData, String carrierName, String lotJudge, String machineName, String reworkFlag,
			List<ProductPGSRC> productPGSRCSequence, Map<String, String> assignCarrierUdfs, Map<String, String> deassignCarrierUdfs, Map<String, String> udfs) throws CustomException
	{
		String currentNode  = trackOutLot.getNodeStack();
		String originalNode = currentNode.substring(0, currentNode.lastIndexOf("."));
		Map<String, String> trackOutLotUdfs = trackOutLot.getUdfs();
		
		String nodeStack = "";

		udfs.put("PORTNAME", portData.getKey().getPortName());
		udfs.put("PORTTYPE", CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"));
		udfs.put("PORTUSETYPE", CommonUtil.getValue(portData.getUdfs(), "PORTUSETYPE"));
		udfs.put("BEFOREOPERATIONNAME", trackOutLot.getProcessOperationName());
		udfs.put("BEFOREFLOWNAME", trackOutLot.getProcessFlowName());

		// check force sampling operation
		List<Map<String, Object>> forceSamplingList = GetNextForceSampling(trackOutLot);

		if (forceSamplingList.size() < 1)
		{
			trackOutLot.setProcessFlowName(trackOutLotUdfs.get("RETURNFLOWNAME"));
			trackOutLot.setProcessOperationName(trackOutLotUdfs.get("RETURNOPERATIONNAME"));
			trackOutLot.setProcessOperationVersion(trackOutLotUdfs.get("RETURNOPERATIONVER"));
			trackOutLot.setNodeStack(originalNode);
			//trackOutLot.setUdfs(trackOutLotUdfs);

			nodeStack = trackOutLot.getNodeStack();

			int n = -1;
			n = nodeStack.indexOf(".");
			log.info("nodeStack.indexOf(.)" + n);

			if (n != -1)
			{
				// Node check = PolicyUtil.getNextOperation(trackOutLot);
				Node check = null;

				// Set Return Info
				String[] nodeStackArray = StringUtils.split(trackOutLot.getNodeStack(), ".");

				int idx = nodeStackArray.length;
				log.info("nodeStackArray.length" + idx);

				if (idx > 1)
				{
					try
					{
						check = ProcessFlowServiceProxy.getNodeService().getNode(nodeStackArray[idx - 2]);
					}
					catch (Exception e)
					{
						
					}
				}

				if (idx > 1 && check != null)
				{
					udfs.put("RETURNFLOWNAME", check.getProcessFlowName());
					udfs.put("RETURNOPERATIONNAME", check.getNodeAttribute1());
					udfs.put("RETURNOPERATIONVER", check.getNodeAttribute2());
				}
				else
				{
					udfs.put("RETURNFLOWNAME", "");
					udfs.put("RETURNOPERATIONNAME", "");
					udfs.put("RETURNOPERATIONVER", "");
				}
			}
			else
			{
				udfs.put("RETURNFLOWNAME", "");
				udfs.put("RETURNOPERATIONNAME", "");
				udfs.put("RETURNOPERATIONVER", "");
			}
			trackOutLot.setUdfs(trackOutLotUdfs);

			log.info("ReturnFlow:ReturnOper[" + trackOutLotUdfs.get("RETURNFLOWNAME") + ":" + trackOutLotUdfs.get("RETURNOPERATIONNAME") + "]");
		}
		else
		{
			Node nextNode = ProcessFlowServiceProxy.getNodeService().getNode(trackOutLot.getFactoryName(), forceSamplingList.get(0).get("PROCESSFLOWNAME").toString(),
					forceSamplingList.get(0).get("PROCESSFLOWVERSION").toString(), "ProcessOperation", forceSamplingList.get(0).get("OPERATIONNAME").toString(),
					forceSamplingList.get(0).get("OPERATIONVERSION").toString());

			// MultiForceSample Case
			//==============================================================================
			String checkNode = originalNode;
			if(StringUtils.contains(originalNode, "."))
				checkNode = originalNode.substring(originalNode.lastIndexOf(".") + 1, originalNode.length());

			boolean exist = false;
			
			for (Map<String, Object> force : forceSamplingList)
			{
				String nodeId = ConvertUtil.getMapValueByName(force, "NODEID");
				
				log.info("checkNode : " + checkNode + "nodeId" + nodeId);
				if(StringUtils.equals(nodeId, checkNode))
				{
					nextNode = ProcessFlowServiceProxy.getNodeService().getNode(nodeId);
					exist = true;
					break;
				}
			}
			//==============================================================================
			
			trackOutLot.setProcessFlowName(nextNode.getProcessFlowName());
			trackOutLot.setProcessOperationName(nextNode.getNodeAttribute1());
			trackOutLot.setProcessOperationVersion(nextNode.getNodeAttribute2());
			trackOutLot.setNodeStack(originalNode + "." + nextNode.getKey().getNodeId());
			//trackOutLot.setUdfs(trackOutLotUdfs);

			if (exist)
			{
				nodeStack = originalNode;
				String nextNodeId = originalNode.substring(originalNode.lastIndexOf(".") - 16, originalNode.lastIndexOf("."));
				Node returnNode = ProcessFlowServiceProxy.getNodeService().getNode(nextNodeId);

				udfs.put("RETURNFLOWNAME", returnNode.getProcessFlowName());
				udfs.put("RETURNOPERATIONNAME", returnNode.getNodeAttribute1());
				udfs.put("RETURNOPERATIONVER", returnNode.getNodeAttribute2());
			}
			else
				nodeStack = originalNode + "." + nextNode.getKey().getNodeId();
		}

		String processFlowName = trackOutLot.getProcessFlowName();
		String processFlowVersion = trackOutLot.getProcessFlowVersion();
		String processOperationName = trackOutLot.getProcessOperationName();
		String processOperationVersion = trackOutLot.getProcessOperationVersion();

		// rework grade should go to rework
		MakeLoggedOutInfo makeLoggedOutInfo = MESLotServiceProxy.getLotInfoUtil().makeLoggedOutInfo(trackOutLot, trackOutLot.getAreaName(), assignCarrierUdfs, carrierName, "", deassignCarrierUdfs,
				StringUtils.isEmpty(lotJudge) ? trackOutLot.getLotGrade() : lotJudge, machineName, "", nodeStack, processFlowName, processFlowVersion, processOperationName, processOperationVersion,
				productPGSRCSequence, reworkFlag, reworkFlag.equals(GenericServiceProxy.getConstantMap().Flag_Y) ? nodeStack : "", udfs);

		eventInfo.setEventName("TrackOut");

		Lot afterTrackOutLot = MESLotServiceProxy.getLotServiceImpl().trackOutLot(eventInfo, trackOutLot, makeLoggedOutInfo);

		return afterTrackOutLot;
	}

	public Node getSamplingInfo(Lot lotData, Node nextNode) throws CustomException
	{
		if (nextNode == null)
			return nextNode;

		Node sampleNode = null;

		ProcessFlowKey processFlowKey1 = new ProcessFlowKey();
		processFlowKey1.setFactoryName(nextNode.getFactoryName());
		processFlowKey1.setProcessFlowName(nextNode.getProcessFlowName());
		processFlowKey1.setProcessFlowVersion(nextNode.getProcessFlowVersion());

		// Current Lot is not Rework.
		if (!StringUtils.equals(lotData.getReworkState(), GenericServiceProxy.getConstantMap().Lot_InRework))
		{
			if (!StringUtils.isEmpty(lotData.getUdfs().get("FIRSTGLASSFLAG")) && !StringUtils.isEmpty(lotData.getUdfs().get("JOBNAME")))
			{
				sampleNode = getReserveSampleLotData(lotData, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), nextNode.getProcessFlowName(),
						nextNode.getProcessFlowVersion(), nextNode.getNodeAttribute1(), nextNode.getNodeAttribute2(), lotData.getProcessOperationName(), lotData.getUdfs().get("RETURNOPERATIONNAME"));
			}
			else
			{
				sampleNode = getReserveSampleLotData(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), nextNode.getProcessFlowName(),
							nextNode.getProcessFlowVersion(), nextNode.getNodeAttribute1(), nextNode.getNodeAttribute2(), lotData.getProcessOperationName(), lotData.getUdfs().get("RETURNOPERATIONNAME"));
			}
		}
		
		return sampleNode;
	}

	public List<Map<String, Object>> getSamplePolicyList(String factoryName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion,
			String machineName, String toProcessFlowName, String toProcessFlowVersion, String toProcessOperationName, String toProcessOperationVersion) throws CustomException
	{
		Map<String, Object> bindMap = new HashMap<String, Object>();

		StringBuilder sqlAddProcessOperationName = new StringBuilder();
		String sqlAddToMachineName = "";
		String sqlAddToProcessFlowName = "";
		String sqlAddToProcessFlowVersion = "";
		String sqlAddToProcessOperationName = "";
		String sqlAddToProcessOperationVersion = "";

		if (StringUtils.isNotEmpty(processOperationName))
		{
			bindMap.put("PROCESSOPERATIONNAME", processOperationName);
			sqlAddProcessOperationName.append("    AND T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		}
		if (StringUtils.isNotEmpty(processOperationVersion))
		{
			bindMap.put("PROCESSOPERATIONVERSION", processOperationVersion);
			sqlAddProcessOperationName.append("    AND T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		}
		if (StringUtils.isNotEmpty(machineName))
		{
			bindMap.put("MACHINENAME", machineName);
			sqlAddToMachineName = "    AND T.MACHINENAME IN ('NA', :MACHINENAME) ";
		}
		if (StringUtils.isNotEmpty(toProcessFlowName))
		{
			bindMap.put("TOPROCESSFLOWNAME", toProcessFlowName);
			sqlAddToProcessFlowName = "    AND P.TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME ";
		}
		if (StringUtils.isNotEmpty(toProcessFlowVersion))
		{
			bindMap.put("TOPROCESSFLOWVERSION", toProcessFlowVersion);
			sqlAddToProcessFlowVersion = "    AND P.TOPROCESSFLOWVERSION = :TOPROCESSFLOWVERSION ";
		}
		if (StringUtils.isNotEmpty(toProcessOperationName))
		{
			bindMap.put("TOPROCESSOPERATIONNAME", toProcessOperationName);
			sqlAddToProcessOperationName = "    AND P.TOPROCESSOPERATIONNAME = :TOPROCESSOPERATIONNAME ";
		}
		if (StringUtils.isNotEmpty(toProcessOperationVersion))
		{
			bindMap.put("TOPROCESSOPERATIONVERSION", toProcessOperationVersion);
			sqlAddToProcessOperationVersion = "    AND P.TOPROCESSOPERATIONVERSION = :TOPROCESSOPERATIONVERSION ";
		}

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT FACTORYNAME, ");
		sql.append("       PROCESSFLOWNAME, ");
		sql.append("       PROCESSFLOWVERSION, ");
		sql.append("       PROCESSOPERATIONNAME, ");
		sql.append("       PROCESSOPERATIONVERSION, ");
		sql.append("       MACHINENAME, ");
		sql.append("       TOPROCESSFLOWNAME, ");
		sql.append("       TOPROCESSFLOWVERSION, ");
		sql.append("       TOPROCESSOPERATIONNAME, ");
		sql.append("       TOPROCESSOPERATIONVERSION, ");
		sql.append("       LOTSAMPLINGCOUNT, ");
		sql.append("       PRODUCTSAMPLINGCOUNT, ");
		sql.append("       PRODUCTSAMPLINGPOSITION, ");
		sql.append("       FLOWPRIORITY, ");
		sql.append("       RETURNOPERATIONNAME, ");
		sql.append("       RETURNOPERATIONVER ");
		sql.append("  FROM TFOMPOLICY T, POSSAMPLE P ");
		sql.append(" WHERE T.CONDITIONID = P.CONDITIONID ");
		sql.append("   AND T.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append(sqlAddProcessOperationName);
		sql.append(sqlAddToMachineName);
		sql.append(sqlAddToProcessFlowName);
		sql.append(sqlAddToProcessFlowVersion);
		sql.append(sqlAddToProcessOperationName);
		sql.append(sqlAddToProcessOperationVersion);
		sql.append(" ORDER BY FLOWPRIORITY ASC ");

		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PROCESSFLOWNAME", processFlowName);
		bindMap.put("PROCESSFLOWVERSION", processFlowVersion);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		return sqlResult;
	}

	public int getSamplePriority(String factoryName, String fromProcessFlow, String fromProcessFlowVer, String fromOperationName, String fromOperationVer, String toProcessFlow, String toProcessFlowVer)
			throws CustomException
	{
		int priorityResult = -1;

		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT POS.FLOWPRIORITY ");
		sql.append(" FROM POSSAMPLE POS, TFOMPOLICY TF ");
		sql.append(" WHERE POS.CONDITIONID = TF.CONDITIONID ");
		sql.append("   AND TF.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND TF.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND TF.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND TF.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND TF.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		sql.append("   AND POS.TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME ");
		sql.append("   AND POS.TOPROCESSFLOWVERSION = :TOPROCESSFLOWVERSION ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("FACTORYNAME", factoryName);
		args.put("PROCESSFLOWNAME", fromProcessFlow);
		args.put("PROCESSFLOWVERSION", fromProcessFlowVer);
		args.put("PROCESSOPERATIONNAME", fromOperationName);
		args.put("PROCESSOPERATIONVERSION", fromOperationVer);
		args.put("TOPROCESSFLOWNAME", toProcessFlow);
		args.put("TOPROCESSFLOWVERSION", toProcessFlowVer);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			String priority = ConvertUtil.getMapValueByName(result.get(0), "FLOWPRIORITY");
			priorityResult = Integer.parseInt(priority);
		}

		return priorityResult;
	}

	public boolean isSampleFlow(String factoryName, String processFlowName, String processFlowVersion) throws CustomException
	{
		boolean isSampleFlow = false;
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT PROCESSFLOWNAME ");
		sql.append("  FROM PROCESSFLOW ");
		sql.append(" WHERE FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND PROCESSFLOWTYPE IN ('Sample', 'Inspection', 'FirstGlass') ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", factoryName);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			isSampleFlow = true;
		}

		return isSampleFlow;
	}
	
	public boolean isSampleFlow(MachineSpec machineSpecData, String factoryName, String processFlowName, String processFlowVersion) throws CustomException
	{
		boolean isSampleFlow = false;
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT P.PROCESSFLOWNAME ");
		sql.append("  FROM PROCESSFLOW P, MACHINESPEC MS ");
		sql.append(" WHERE P.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND P.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND P.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND P.PROCESSFLOWTYPE IN ('Sample', 'Inspection', 'FirstGlass', 'Main') ");
		sql.append("   AND MS.MACHINEGROUPNAME = :MACHINEGROUPNAME ");
		
		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", factoryName);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);
		args.put("MACHINEGROUPNAME", machineSpecData.getMachineGroupName());

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			isSampleFlow = true;
		}

		return isSampleFlow;
	}

	public List<Map<String, Object>> operationList(String factoryName, String processFlowName, String processFlowVersion) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT QL.FACTORYNAME, ");
		sql.append("       QL.PROCESSOPERATIONNAME, ");
		sql.append("       QL.PROCESSOPERATIONVERSION, ");
		sql.append("       PO.DESCRIPTION PROCESSOPERATIONDESC, ");
		sql.append("       QL.PROCESSFLOWNAME, ");
		sql.append("       QL.PROCESSFLOWVERSION, ");
		sql.append("       PO.DETAILPROCESSOPERATIONTYPE, ");
		sql.append("       QL.NODEID ");
		sql.append("  FROM PROCESSOPERATIONSPEC PO, ");
		sql.append("       (SELECT LEVEL LV, ");
		sql.append("               N.FACTORYNAME, ");
		sql.append("               N.NODEATTRIBUTE1 PROCESSOPERATIONNAME, ");
		sql.append("               N.NODEATTRIBUTE2 PROCESSOPERATIONVERSION, ");
		sql.append("               N.PROCESSFLOWNAME, ");
		sql.append("               N.PROCESSFLOWVERSION, ");
		sql.append("               N.NODEID ");
		sql.append("          FROM ARC A, NODE N, PROCESSFLOW PF ");
		sql.append("         WHERE 1 = 1 ");
		sql.append("           AND N.NODETYPE = 'ProcessOperation' ");
		sql.append("           AND N.FACTORYNAME = :FACTORYNAME ");
		sql.append("           AND N.FACTORYNAME = PF.FACTORYNAME ");
		sql.append("           AND PF.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("           AND PF.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("           AND N.PROCESSFLOWNAME = PF.PROCESSFLOWNAME ");
		sql.append("           AND N.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION ");
		sql.append("           AND N.PROCESSFLOWNAME = A.PROCESSFLOWNAME ");
		sql.append("           AND A.FROMNODEID = N.NODEID ");
		sql.append("        START WITH N.NODETYPE = 'Start' ");
		sql.append("        CONNECT BY NOCYCLE A.FROMNODEID = PRIOR A.TONODEID AND A.FACTORYNAME = :FACTORYNAME) QL ");
		sql.append(" WHERE 1 = 1 ");
		sql.append("   AND PO.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND PO.PROCESSOPERATIONNAME = QL.PROCESSOPERATIONNAME ");
		sql.append("   AND PO.PROCESSOPERATIONVERSION = QL.PROCESSOPERATIONVERSION ");
		sql.append("ORDER BY QL.LV ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", factoryName);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> operationList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		return operationList;
	}

	public List<Map<String, Object>> GetNextForceSampling(Lot lotInfo) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT QL.FACTORYNAME FACTORY, ");
		sql.append("       QL.PROCESSOPERATIONNAME OPERATIONNAME, ");
		sql.append("       QL.PROCESSOPERATIONVERSION OPERATIONVERSION, ");
		sql.append("       DECODE (TP.DESCRIPTION, NULL, PO.DESCRIPTION, TP.DESCRIPTION) AS OPERATIONDESC, ");
		sql.append("       QL.PROCESSFLOWNAME, ");
		sql.append("       QL.PROCESSFLOWVERSION, ");
		sql.append("       PO.PROCESSOPERATIONTYPE, ");
		sql.append("       QL.NODEID ");
		sql.append("  FROM PROCESSOPERATIONSPEC PO, ");
		sql.append("       (SELECT LEVEL LV, ");
		sql.append("               N.FACTORYNAME, ");
		sql.append("               N.NODEATTRIBUTE1 PROCESSOPERATIONNAME, ");
		sql.append("               N.NODEATTRIBUTE2 PROCESSOPERATIONVERSION, ");
		sql.append("               N.PROCESSFLOWNAME, ");
		sql.append("               N.PROCESSFLOWVERSION, ");
		sql.append("               N.NODEID ");
		sql.append("          FROM ARC A, NODE N, PROCESSFLOW PF ");
		sql.append("         WHERE 1 = 1 ");
		sql.append("           AND N.NODETYPE = 'ProcessOperation' ");
		sql.append("           AND PF.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("           AND PF.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("           AND N.FACTORYNAME = :FACTORYNAME ");
		sql.append("           AND N.PROCESSFLOWNAME = PF.PROCESSFLOWNAME ");
		sql.append("           AND N.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION ");
		sql.append("           AND N.PROCESSFLOWNAME = A.PROCESSFLOWNAME ");
		sql.append("           AND N.FACTORYNAME = PF.FACTORYNAME ");
		sql.append("           AND A.FROMNODEID = N.NODEID ");
		sql.append("           AND N.NODEATTRIBUTE1 IN (SELECT SM.TOPROCESSOPERATIONNAME ");
		sql.append("                                      FROM CT_SAMPLELOT SM ");
		sql.append("                                     WHERE SM.LOTNAME = :LOTNAME ");
		sql.append("                                       AND SM.FORCESAMPLINGFLAG = 'Y') ");
		sql.append("        START WITH N.NODETYPE = 'Start' ");
		sql.append("        CONNECT BY NOCYCLE A.FROMNODEID = PRIOR A.TONODEID ");
		sql.append("               AND A.FACTORYNAME = :FACTORYNAME) QL, ");
		sql.append("       (SELECT * ");
		sql.append("          FROM TPFOPOLICY ");
		sql.append("         WHERE PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("           AND PRODUCTSPECVERSION = :PRODUCTSPECVERSION) TP ");
		sql.append(" WHERE 1 = 1 ");
		sql.append("   AND PO.PROCESSOPERATIONNAME = QL.PROCESSOPERATIONNAME ");
		sql.append("   AND PO.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND QL.PROCESSFLOWNAME = TP.PROCESSFLOWNAME(+) ");
		sql.append("   AND QL.PROCESSFLOWVERSION = TP.PROCESSFLOWVERSION(+) ");
		sql.append("   AND QL.PROCESSOPERATIONNAME = TP.PROCESSOPERATIONNAME(+) ");
		sql.append("   AND QL.FACTORYNAME = TP.FACTORYNAME(+) ");
		sql.append("ORDER BY QL.LV ");

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("LOTNAME", lotInfo.getKey().getLotName());
		bindMap.put("FACTORYNAME", lotInfo.getFactoryName());
		bindMap.put("PRODUCTSPECNAME", lotInfo.getProductSpecName());
		bindMap.put("PRODUCTSPECVERSION", lotInfo.getProductSpecVersion());
		bindMap.put("PROCESSFLOWNAME", lotInfo.getProcessFlowName());
		bindMap.put("PROCESSFLOWVERSION", lotInfo.getProcessFlowVersion());

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		return sqlResult;
	}

	public Node getReserveSampleLotData(String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion, String oldProcessOperationName, String returnProcessOperationName) throws CustomException
	{
		Node nextNode = null;
		Map<String, String> udfs = new HashMap<String, String>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT FACTORYNAME, ");
		sql.append("       MACHINENAME, ");
		sql.append("       TOPROCESSFLOWNAME, ");
		sql.append("       TOPROCESSFLOWVERSION, ");
		sql.append("       ACTUALPRODUCTCOUNT, ");
		sql.append("       ACTUALSAMPLEPOSITION, ");
		sql.append("       PRIORITY, ");
		sql.append("       RETURNPROCESSFLOWNAME, ");
		sql.append("       RETURNPROCESSFLOWVERSION, ");
		sql.append("       RETURNOPERATIONNAME, ");
		sql.append("       RETURNOPERATIONVERSION, ");
		sql.append("       FORCESAMPLINGFLAG ");
		sql.append("  FROM CT_SAMPLELOT ");
		sql.append(" WHERE 1 = 1 ");
		sql.append("   AND LOTNAME = :LOTNAME ");
		sql.append("   AND FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("   AND PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");
		sql.append("   AND PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND LOTSAMPLEFLAG = :LOTSAMPLEFLAG ");
		sql.append("   AND NVL(FORCESAMPLINGFLAG,'N') = 'N' ");
		sql.append("   AND (PROCESSOPERATIONNAME = :OLDPROCESSOPERATIONNAME ");
		sql.append("     OR RETURNOPERATIONNAME = :RETURNPROCESSOPERATIONNAME) ");
		sql.append("GROUP BY FACTORYNAME, ");
		sql.append("         MACHINENAME, ");
		sql.append("         TOPROCESSFLOWNAME, ");
		sql.append("         TOPROCESSFLOWVERSION, ");
		sql.append("         ACTUALPRODUCTCOUNT, ");
		sql.append("         ACTUALSAMPLEPOSITION, ");
		sql.append("         PRIORITY, ");
		sql.append("         RETURNPROCESSFLOWNAME, ");
		sql.append("         RETURNPROCESSFLOWVERSION, ");
		sql.append("         RETURNOPERATIONNAME, ");
		sql.append("         RETURNOPERATIONVERSION, ");
		sql.append("         FORCESAMPLINGFLAG ");
		sql.append("ORDER BY PRIORITY ");

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("LOTNAME", lotName);
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PRODUCTSPECNAME", productSpecName);
		bindMap.put("PRODUCTSPECVERSION", productSpecVersion);
		bindMap.put("PROCESSFLOWNAME", processFlowName);
		bindMap.put("PROCESSFLOWVERSION", processFlowVersion);
		bindMap.put("LOTSAMPLEFLAG", "Y");
		bindMap.put("OLDPROCESSOPERATIONNAME", oldProcessOperationName);
		bindMap.put("RETURNPROCESSOPERATIONNAME", returnProcessOperationName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		if (sqlResult.size() > 0)
		{
			String toFlowName = ConvertUtil.getMapValueByName(sqlResult.get(0), "TOPROCESSFLOWNAME");
			String toFlowVersion = ConvertUtil.getMapValueByName(sqlResult.get(0), "TOPROCESSFLOWVERSION");

			if (StringUtils.isNotEmpty(toFlowName) && StringUtils.isNotEmpty(toFlowVersion))
			{
				try
				{
					Node startNode = ProcessFlowServiceProxy.getProcessFlowService().getStartNode(new ProcessFlowKey(factoryName, toFlowName, toFlowVersion));
					nextNode = ProcessFlowServiceProxy.getProcessFlowService().getNextNode(startNode.getKey().getNodeId(), "Normal", "");

					String returnFlowName = ConvertUtil.getMapValueByName(sqlResult.get(0), "RETURNPROCESSFLOWNAME");
					String returnFlowVersion = ConvertUtil.getMapValueByName(sqlResult.get(0), "RETURNPROCESSFLOWVERSION");
					String returnOperName = ConvertUtil.getMapValueByName(sqlResult.get(0), "RETURNOPERATIONNAME");
					String returnOperVersion = ConvertUtil.getMapValueByName(sqlResult.get(0), "RETURNOPERATIONVERSION");

					udfs.put("RETURNPROCESSFLOWNAME", returnFlowName);
					udfs.put("RETURNPROCESSFLOWVERSION", returnFlowVersion);
					udfs.put("RETURNOPERATIONNAME", returnOperName);
					udfs.put("RETURNOPERATIONVERSION", returnOperVersion);

					nextNode.setUdfs(udfs);
				}
				catch (Exception e)
				{
				}
			}
		}

		return nextNode;
	}
	
	public Node getReserveSampleLotData(Lot lotData, String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion, String oldProcessOperationName, String returnProcessOperationName) throws CustomException
	{
		Node nextNode = null;
		Map<String, String> udfs = new HashMap<String, String>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT FACTORYNAME, ");
		sql.append("       MACHINENAME, ");
		sql.append("       TOPROCESSFLOWNAME, ");
		sql.append("       TOPROCESSFLOWVERSION, ");
		sql.append("       ACTUALPRODUCTCOUNT, ");
		sql.append("       ACTUALSAMPLEPOSITION, ");
		sql.append("       PRIORITY, ");
		sql.append("       RETURNPROCESSFLOWNAME, ");
		sql.append("       RETURNPROCESSFLOWVERSION, ");
		sql.append("       RETURNOPERATIONNAME, ");
		sql.append("       RETURNOPERATIONVERSION, ");
		sql.append("       FORCESAMPLINGFLAG, ");
		sql.append("       TOPROCESSOPERATIONNAME, ");
		sql.append("       TOPROCESSOPERATIONVERSION ");
		sql.append("  FROM CT_SAMPLELOT ");
		sql.append(" WHERE 1 = 1 ");
		sql.append("   AND LOTNAME = :LOTNAME ");
		sql.append("   AND FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("   AND PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");
		sql.append("   AND PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND LOTSAMPLEFLAG = :LOTSAMPLEFLAG ");
		sql.append("   AND PRIORITY > 0 "); //For TP PVD FirsrGlass
		sql.append("   AND (PROCESSOPERATIONNAME = :OLDPROCESSOPERATIONNAME ");
		sql.append("     OR RETURNOPERATIONNAME = :RETURNPROCESSOPERATIONNAME) ");
		sql.append("GROUP BY FACTORYNAME, ");
		sql.append("         MACHINENAME, ");
		sql.append("         TOPROCESSFLOWNAME, ");
		sql.append("         TOPROCESSFLOWVERSION, ");
		sql.append("         ACTUALPRODUCTCOUNT, ");
		sql.append("         ACTUALSAMPLEPOSITION, ");
		sql.append("         PRIORITY, ");
		sql.append("         RETURNPROCESSFLOWNAME, ");
		sql.append("         RETURNPROCESSFLOWVERSION, ");
		sql.append("         RETURNOPERATIONNAME, ");
		sql.append("         RETURNOPERATIONVERSION, ");
		sql.append("       FORCESAMPLINGFLAG, ");
		sql.append("       TOPROCESSOPERATIONNAME, ");
		sql.append("       TOPROCESSOPERATIONVERSION ");
		sql.append("ORDER BY PRIORITY ");

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("LOTNAME", lotName);
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PRODUCTSPECNAME", productSpecName);
		bindMap.put("PRODUCTSPECVERSION", productSpecVersion);
		bindMap.put("PROCESSFLOWNAME", processFlowName);
		bindMap.put("PROCESSFLOWVERSION", processFlowVersion);
		bindMap.put("LOTSAMPLEFLAG", "Y");
		bindMap.put("OLDPROCESSOPERATIONNAME", oldProcessOperationName);
		bindMap.put("RETURNPROCESSOPERATIONNAME", returnProcessOperationName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		if (sqlResult.size() > 0)
		{
			String toFlowName = ConvertUtil.getMapValueByName(sqlResult.get(0), "TOPROCESSFLOWNAME");
			String toFlowVersion = ConvertUtil.getMapValueByName(sqlResult.get(0), "TOPROCESSFLOWVERSION");
			String toOperationName = ConvertUtil.getMapValueByName(sqlResult.get(0), "TOPROCESSOPERATIONNAME");
			String toOpetationVer = ConvertUtil.getMapValueByName(sqlResult.get(0), "TOPROCESSOPERATIONVERSION");
			
			if (!StringUtils.isEmpty(lotData.getUdfs().get("FIRSTGLASSFLAG")) && !StringUtils.isEmpty(lotData.getUdfs().get("JOBNAME")))
			{
				try
				{
					String targetNodeId = NodeStack.getNodeID(factoryName, toFlowName, toOperationName, toOpetationVer);
					nextNode = ProcessFlowServiceProxy.getNodeService().getNode(targetNodeId);

					String returnFlowName = ConvertUtil.getMapValueByName(sqlResult.get(0), "RETURNPROCESSFLOWNAME");
					String returnFlowVersion = ConvertUtil.getMapValueByName(sqlResult.get(0), "RETURNPROCESSFLOWVERSION");
					String returnOperName = ConvertUtil.getMapValueByName(sqlResult.get(0), "RETURNOPERATIONNAME");
					String returnOperVersion = ConvertUtil.getMapValueByName(sqlResult.get(0), "RETURNOPERATIONVERSION");

					udfs.put("RETURNPROCESSFLOWNAME", returnFlowName);
					udfs.put("RETURNPROCESSFLOWVERSION", returnFlowVersion);
					udfs.put("RETURNOPERATIONNAME", returnOperName);
					udfs.put("RETURNOPERATIONVERSION", returnOperVersion);

					nextNode.setUdfs(udfs);
				}
				catch (Exception e)
				{
				}
			}
		}

		return nextNode;
	}

	public Node getReserveSampleLotData(Lot lotData, Map<String, Object> samplePolicyM) throws CustomException
	{
		Node nextNode = null;
		Map<String, String> udfs = new HashMap<String, String>();

		List<SampleLot> sampleLotList = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListWithOutMachineName(lotData.getKey().getLotName(), lotData.getFactoryName(),
				lotData.getProductSpecName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(),
				lotData.getProcessFlowVersion(), samplePolicyM.get("TOPROCESSFLOWNAME").toString(), samplePolicyM.get("TOPROCESSFLOWVERSION").toString(),
				samplePolicyM.get("TOPROCESSOPERATIONNAME").toString(), samplePolicyM.get("TOPROCESSOPERATIONVERSION").toString());

		if (sampleLotList != null)
		{
			String toFlowName = sampleLotList.get(0).getToProcessFlowName();
			String toFlowVersion = sampleLotList.get(0).getToProcessFlowVersion();

			if (StringUtils.isNotEmpty(toFlowName) && StringUtils.isNotEmpty(toFlowVersion))
			{
				try
				{
					Node startNode = ProcessFlowServiceProxy.getProcessFlowService().getStartNode(new ProcessFlowKey(lotData.getFactoryName(), toFlowName, toFlowVersion));
					nextNode = ProcessFlowServiceProxy.getProcessFlowService().getNextNode(startNode.getKey().getNodeId(), "Normal", "");

					String returnFlowName = sampleLotList.get(0).getReturnProcessFlowName();
					String returnFlowVersion = sampleLotList.get(0).getReturnProcessFlowVersion();
					String returnOperName = sampleLotList.get(0).getReturnOperationName();
					String returnOperVersion = sampleLotList.get(0).getReturnOperationVersion();

					udfs.put("RETURNPROCESSFLOWNAME", returnFlowName);
					udfs.put("RETURNPROCESSFLOWVERSION", returnFlowVersion);
					udfs.put("RETURNOPERATIONNAME", returnOperName);
					udfs.put("RETURNOPERATIONVERSION", returnOperVersion);

					nextNode.setUdfs(udfs);
				}
				catch (Exception e)
				{
				}
			}
		}

		return nextNode;
	}

	public List<SampleLot> setSamplingData(EventInfo eventInfo, Lot lotData, Map<String, Object> samplePolicy) throws CustomException
	{
		log.info("Start setSamplingData: PROCESSFLOWNAME(" + lotData.getProcessFlowName() + "),PROCESSOPERNAME(" + lotData.getProcessOperationName() + "),TOPROCESSFLOWNAME("
				+ (String) samplePolicy.get("TOPROCESSFLOWNAME") + "),TOPROCESSOPERATIONNAME(" + (String) samplePolicy.get("TOPROCESSOPERATIONNAME") + ")");

		// 1. Synchronize LotSampleCount & set Sampling Lot Data
		String sampleFlag = "N";

		// get SampleLotData(CT_SAMPLELOT)
		List<SampleLot> sampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListWithOutMachineName(lotData.getKey().getLotName(), lotData.getFactoryName(),
				lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(),
				lotData.getProcessOperationVersion(), (String) samplePolicy.get("TOPROCESSFLOWNAME"), (String) samplePolicy.get("TOPROCESSFLOWVERSION"),
				(String) samplePolicy.get("TOPROCESSOPERATIONNAME"), (String) samplePolicy.get("TOPROCESSOPERATIONVERSION"));

		// 0. Set SampleLotCount
		// get SamplingLotCount(CT_SAMPLELOTCOUNT)
		List<SampleLotCount> sampleLotCount = ExtendedObjectProxy.getSampleLotCountService().getSampleLotCountWithoutToFlow(lotData.getFactoryName(), lotData.getProductSpecName(),
				lotData.getProcessFlowName(), lotData.getProcessOperationName(), (String) samplePolicy.get("MACHINENAME"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"));

		// Synchronize LotSampleCount with SamplePolicy
		if (sampleLotCount != null)
		{
			ExtendedObjectProxy.getSampleLotCountService().updateSampleLotCountWithoutToFlow(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(),
					lotData.getProcessOperationName(), (String) samplePolicy.get("MACHINENAME"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"), (String) samplePolicy.get("LOTSAMPLINGCOUNT"),
					sampleLotCount.get(0).getCurrentLotCount(), sampleLotCount.get(0).getTotalLotCount());
		}
		else
		{
			ExtendedObjectProxy.getSampleLotCountService().insertSampleLotCount(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(),
					lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), (String) samplePolicy.get("MACHINENAME"),
					(String) samplePolicy.get("TOPROCESSFLOWNAME"), (String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"),
					(String) samplePolicy.get("TOPROCESSOPERATIONVERSION"), (String) samplePolicy.get("LOTSAMPLINGCOUNT"), "0", "0");
		}

		sampleLotCount = ExtendedObjectProxy.getSampleLotCountService().getSampleLotCountWithoutToFlow(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(),
				lotData.getProcessOperationName(), (String) samplePolicy.get("MACHINENAME"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"));

		double lotSampleCount = Double.parseDouble(sampleLotCount.get(0).getLotSampleCount());
		double currentLotCount = Double.parseDouble(sampleLotCount.get(0).getCurrentLotCount());
		double totalLotCount = Double.parseDouble(sampleLotCount.get(0).getTotalLotCount());

		if (currentLotCount % lotSampleCount == 0)
		{
			currentLotCount = 1;
			totalLotCount++;

			sampleFlag = "Y";
		}
		else
		{
			currentLotCount = currentLotCount + 1;
			totalLotCount++;
		}

		// set SampleLotCount(CT_SAMPLELOTCOUNT)
		ExtendedObjectProxy.getSampleLotCountService().updateSampleLotCountWithoutToFlow(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(),
				lotData.getProcessOperationName(), (String) samplePolicy.get("MACHINENAME"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"), sampleLotCount.get(0).getLotSampleCount(),
				CommonUtil.StringValueOf(currentLotCount), CommonUtil.StringValueOf(totalLotCount));

		// Insert Sample Lot EventInfo
		EventInfoExtended samplEventInfo = new EventInfoExtended(eventInfo);
		boolean allScrapflag = false;

		if (sampleLot == null)
		{
			if (!StringUtils.equals(sampleFlag, "Y"))
				return sampleLot;
			// 1. set Sampling Product Data

			// parcing the PRODUCTSAMPLEPOSITION
			String productSamplePositions = (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION");
			log.info("setSamplingData:ProductSamplePositions(" + productSamplePositions + ")");

			// Random Sampling Position
			if (productSamplePositions.equals("Random"))
			{
				List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());

				if (productList.size() < Integer.valueOf((String) samplePolicy.get("PRODUCTSAMPLINGCOUNT")))
				{
					productSamplePositions = "ALL";
				}
				else
				{
					Collections.shuffle(productList);
					String temp = "";
					for (int i = 0; i < Integer.valueOf((String) samplePolicy.get("PRODUCTSAMPLINGCOUNT")); i++)
					{
						String position = productList.get(i).getPosition() + ",";
						temp += position;
					}
					productSamplePositions = temp.substring(0, (temp.length() - 1));
				}
			}

			if(StringUtil.isNotEmpty(productSamplePositions))
			{
				productSamplePositions = productSamplePositions.replaceAll(" ", ""); // remove blank
			}
			
			if (StringUtils.equals(productSamplePositions, "All") || StringUtils.equals(productSamplePositions, "ALL"))
			{
				productSamplePositions = CommonUtil.makeProductSamplingPositionList(Integer.valueOf((String) samplePolicy.get("PRODUCTSAMPLINGCOUNT")));
			}
			List<String> productSamplePositionList = CommonUtil.splitString(",", productSamplePositions); // delimeter is ','
			List<String> exceptBackpPositionList = CommonUtil.copyToStringList(productSamplePositionList);
			List<String> actualSamplePositionList = new ArrayList<String>();

			List<String> notExistPositionList = new ArrayList<String>();

			for (String productSamplePosition : productSamplePositionList)
			{
				log.info("setSamplingData:productSamplePosition(" + productSamplePosition + ")");

				// get SamplingProduct Data
				String sql = " SELECT PRODUCTNAME FROM PRODUCT WHERE LOTNAME = :lotName AND POSITION = :position ";
				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("lotName", lotData.getKey().getLotName());
				bindMap.put("position", productSamplePosition);

				@SuppressWarnings("unchecked")
				List<Map<String, Object>> sampleProductResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

				if (sampleProductResult.size() > 0) // Exist the Product for productSamplePosition
				{
					// set SamplingProduct Data(CT_SAMPLEPRODUCT)
					ExtendedObjectProxy.getSampleProductService().insertSampleProduct(samplEventInfo, (String) sampleProductResult.get(0).get("PRODUCTNAME"), lotData.getKey().getLotName(),
							lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
							lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), (String) samplePolicy.get("MACHINENAME"), (String) samplePolicy.get("TOPROCESSFLOWNAME"),
							(String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"), (String) samplePolicy.get("TOPROCESSOPERATIONVERSION"), sampleFlag,
							(String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"), productSamplePositions, productSamplePosition, "", "");

					actualSamplePositionList.add(productSamplePosition);
				}
				else
				// No exist the Product for productSamplePosition, find new Product(backupProduct) for that.
				{
					notExistPositionList.add(productSamplePosition);
				}
			}

			if (!notExistPositionList.isEmpty())
			{
				List<Product> backupProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());

				int j = 0;
				int i = 0;

				for (; i < backupProductList.size(); i++)
				{
					Product backupProduct = backupProductList.get(i);
					if (backupProduct.getPosition() > Long.parseLong(notExistPositionList.get(j)) && !exceptBackpPositionList.contains(CommonUtil.StringValueOf(backupProduct.getPosition())))
					{
						ExtendedObjectProxy.getSampleProductService().insertSampleProduct(samplEventInfo, backupProduct.getKey().getProductName(), lotData.getKey().getLotName(),
								lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
								lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), lotData.getMachineName(), (String) samplePolicy.get("TOPROCESSFLOWNAME"),
								(String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"), (String) samplePolicy.get("TOPROCESSOPERATIONVERSION"),
								sampleFlag, (String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"),
								CommonUtil.StringValueOf(backupProduct.getPosition()), "", "");

						// except for new Product(backupProduct)
						exceptBackpPositionList.add(CommonUtil.StringValueOf(backupProduct.getPosition()));
						actualSamplePositionList.add(CommonUtil.StringValueOf(backupProduct.getPosition()));
						j++;
					}

					if (j == notExistPositionList.size())
					{
						break;
					}
				}

				if (j != notExistPositionList.size())
				{
					for (i--; i >= 0; i--)
					{
						Product backupProduct = backupProductList.get(i);
						if (backupProduct.getPosition() < Long.parseLong(notExistPositionList.get(j)) && !exceptBackpPositionList.contains(CommonUtil.StringValueOf(backupProduct.getPosition())))
						{
							ExtendedObjectProxy.getSampleProductService().insertSampleProduct(samplEventInfo, backupProduct.getKey().getProductName(), lotData.getKey().getLotName(),
									lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
									lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), lotData.getMachineName(), (String) samplePolicy.get("TOPROCESSFLOWNAME"),
									(String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"), (String) samplePolicy.get("TOPROCESSOPERATIONVERSION"),
									sampleFlag, (String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"),
									CommonUtil.StringValueOf(backupProduct.getPosition()), "", "");

							// except for new Product(backupProduct)
							exceptBackpPositionList.add(CommonUtil.StringValueOf(backupProduct.getPosition()));
							actualSamplePositionList.add(CommonUtil.StringValueOf(backupProduct.getPosition()));
							j++;
						}

						if (j == notExistPositionList.size())
						{
							break;
						}
					}
				}
				allScrapflag = true;
				notExistPositionList.clear();
			}

			if (allScrapflag)
			{
				if (samplEventInfo.getEventComment().length() > 200)
				{
					samplEventInfo.getEventComment().substring(0, 201);
				}
				samplEventInfo.setEventComment("[Sample001]System sample all scrapped. " + samplEventInfo.getEventComment());
			}
			else
			{
				samplEventInfo.setEventComment("[Sample000]System sample Normal. " + samplEventInfo.getEventComment());
			}

			// set SampleLotData(CT_SAMPLELOT)
			ExtendedObjectProxy.getSampleLotService().insertSampleLot(samplEventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
					lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
					lotData.getMachineName(), (String) samplePolicy.get("TOPROCESSFLOWNAME"), (String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"),
					(String) samplePolicy.get("TOPROCESSOPERATIONVERSION"), sampleFlag, sampleLotCount.get(0).getLotSampleCount(), CommonUtil.StringValueOf(currentLotCount),
					CommonUtil.StringValueOf(totalLotCount), (String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"),
					String.valueOf(actualSamplePositionList.size()), CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "", "",
					Integer.valueOf(samplePolicy.get("FLOWPRIORITY").toString()), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
					(String) samplePolicy.get("RETURNOPERATIONNAME"), (String) samplePolicy.get("RETURNOPERATIONVER"), "");

			sampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataList(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
					lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
					lotData.getMachineName(), (String) samplePolicy.get("TOPROCESSFLOWNAME"), (String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"),
					(String) samplePolicy.get("TOPROCESSOPERATIONVERSION"));
		}
		else if (sampleLot != null && sampleLot.size() > 0)
		{
			log.info("Start integration Sampling");

			if (!StringUtils.equals(sampleFlag, "Y"))
				return sampleLot;

			String oldPosition = sampleLot.get(0).getActualSamplePosition();
			List<String> oldProductSamplePositionList = new ArrayList<String>();

			if (oldPosition == null || oldPosition.trim().length() == 0)
			{
				oldPosition = sampleLot.get(0).getProductSamplePosition();

				if (oldPosition == null || oldPosition.trim().length() == 0)
				{
					log.info("Get Sample oldPosition faild ");
					return sampleLot;
				}

				oldProductSamplePositionList = CommonUtil.splitString(",", oldPosition); // delimeter is ','
			}
			else
			{
				oldProductSamplePositionList = CommonUtil.splitString(", ", oldPosition); // delimeter is ', '
			}

			String productSamplePositions = (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION");

			// Random Sampling Position
			if (productSamplePositions.equals("Random"))
			{
				List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());

				if (productList.size() < Integer.valueOf((String) samplePolicy.get("PRODUCTSAMPLINGCOUNT")))
				{
					productSamplePositions = "ALL";
				}
				else
				{
					Collections.shuffle(productList);
					String temp = "";
					for (int i = 0; i < Integer.valueOf((String) samplePolicy.get("PRODUCTSAMPLINGCOUNT")); i++)
					{
						String position = productList.get(i).getPosition() + ",";
						temp += position;
					}
					productSamplePositions = temp.substring(0, (temp.length() - 1));
				}
			}

			productSamplePositions = productSamplePositions.replaceAll(" ", ""); // remove blank
			if (StringUtils.equals(productSamplePositions, "All") || StringUtils.equals(productSamplePositions, "ALL"))
			{
				productSamplePositions = CommonUtil.makeProductSamplingPositionList(Integer.valueOf((String) samplePolicy.get("PRODUCTSAMPLINGCOUNT")));
			}

			List<String> productSamplePositionList = CommonUtil.splitString(",", productSamplePositions); // delimeter is ','

			productSamplePositionList.removeAll(oldProductSamplePositionList);
			productSamplePositionList.addAll(oldProductSamplePositionList);
			Collections.sort(productSamplePositionList, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2)
				{
					Integer d1 = Integer.parseInt(o1);
					Integer d2 = Integer.parseInt(o2);

					return d1.compareTo(d2);
				}
			});
			if (productSamplePositionList.equals(oldProductSamplePositionList))
			{
				log.info("The integration Position as same as DB Position");
				return sampleLot;
			}

			log.info("Get integration Position Success");

			Lot deleteSampleLotData = new Lot();
			deleteSampleLotData.setKey(lotData.getKey());
			deleteSampleLotData.setFactoryName(lotData.getFactoryName());
			deleteSampleLotData.setProductSpecName(lotData.getProductSpecName());
			deleteSampleLotData.setProcessFlowName(lotData.getProcessFlowName());
			deleteSampleLotData.setProcessOperationName(sampleLot.get(0).getToProcessOperationName());
			MESLotServiceProxy.getLotServiceUtil().deleteSamplingData(eventInfo, deleteSampleLotData, new ArrayList<Element>(), true);

			List<String> actualSamplePositionList = new ArrayList<String>();

			for (String productSamplePosition : productSamplePositionList)
			{
				// get SamplingProduct Data
				String sql = " SELECT PRODUCTNAME FROM PRODUCT WHERE LOTNAME = :lotName AND POSITION = :position ";
				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("lotName", lotData.getKey().getLotName());
				bindMap.put("position", productSamplePosition);

				@SuppressWarnings("unchecked")
				List<Map<String, Object>> sampleProductResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

				if (sampleProductResult.size() > 0) // Exist the Product for productSamplePosition
				{
					// set SamplingProduct Data(CT_SAMPLEPRODUCT)
					ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, (String) sampleProductResult.get(0).get("PRODUCTNAME"), lotData.getKey().getLotName(),
							lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
							lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), lotData.getMachineName(), (String) samplePolicy.get("TOPROCESSFLOWNAME"),
							(String) samplePolicy.get("TOPROCESSFLOWNAME"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"), (String) samplePolicy.get("TOPROCESSOPERATIONVERSION"), sampleFlag,
							(String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"), productSamplePosition, "", "");

					actualSamplePositionList.add(productSamplePosition);
				}
			}

			samplEventInfo.setEventComment("[Sample002]Manual sample exist. " + samplEventInfo.getEventComment());

			// set SampleLotData(CT_SAMPLELOT)
			ExtendedObjectProxy.getSampleLotService().insertSampleLot(samplEventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
					lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
					lotData.getMachineName(), (String) samplePolicy.get("TOPROCESSFLOWNAME"), (String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"),
					(String) samplePolicy.get("TOPROCESSOPERATIONVERSION"), sampleFlag, sampleLotCount.get(0).getLotSampleCount(), CommonUtil.StringValueOf(currentLotCount),
					CommonUtil.StringValueOf(totalLotCount), (String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"),
					String.valueOf(actualSamplePositionList.size()), CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "", "",
					Integer.valueOf(ConvertUtil.getMapValueByName(samplePolicy, "FLOWPRIORITY")), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
					(String) samplePolicy.get("RETURNOPERATIONNAME"), (String) samplePolicy.get("RETURNOPERATIONVER"), "");

			sampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataList(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
					lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
					lotData.getMachineName(), (String) samplePolicy.get("TOPROCESSFLOWNAME"), (String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"),
					(String) samplePolicy.get("TOPROCESSOPERATIONVERSION"));
		}

		return sampleLot;
	}

	public void setInlineSamplingListData(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		log.info(" Start setInlineSamplingListData ");
		eventInfo = EventInfoUtil.makeEventInfo("InlineSampling", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT ");
		sql.append("       T.FACTORYNAME ");
		sql.append("      ,T.PROCESSFLOWNAME ");
		sql.append("      ,T.PROCESSFLOWVERSION ");
		sql.append("      ,T.PROCESSOPERATIONNAME ");
		sql.append("      ,T.PROCESSOPERATIONVERSION ");
		sql.append("      ,T.MACHINENAME ");
		sql.append("      ,P.LOTSAMPLINGCOUNT ");
		sql.append("      ,P.PRODUCTSAMPLINGPOSITION ");
		sql.append("      ,P.PRODUCTSAMPLINGCOUNT ");
		sql.append("  FROM TFOMPOLICY T, POSINLINESAMPLE P, POSINLINEMACHINE M ");
		sql.append(" WHERE T.CONDITIONID = P.CONDITIONID ");
		sql.append("   AND T.CONDITIONID = M.CONDITIONID ");
		sql.append("   AND P.UNITNAME = M.UNITNAME ");
		sql.append("   AND T.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		sql.append("   AND NVL (P.LOTSAMPLINGCOUNT, '0') != '0' ");
		sql.append("   AND NVL (P.PRODUCTSAMPLINGCOUNT, '0') != '0' ");
		sql.append("   AND PRODUCTSAMPLINGPOSITION IS NOT NULL ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("FACTORYNAME", lotData.getFactoryName());
		args.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
		args.put("PROCESSFLOWVERSION", lotData.getProcessFlowVersion());
		args.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
		args.put("PROCESSOPERATIONVERSION", lotData.getProcessOperationVersion());

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> policyResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (policyResult.size() > 0)
		{
			String productSamplePositions = "";
			List<String> productSamplePositionList = new ArrayList<String>();

			int iLotSampleMaxCount = 0;
			String lotSampleCount = ConvertUtil.getMapValueByName(policyResult.get(0), "LOTSAMPLINGCOUNT");

			for (Map<String, Object> lotSampleCountMap : policyResult)
			{
				lotSampleCount = ConvertUtil.getMapValueByName(lotSampleCountMap, "LOTSAMPLINGCOUNT");

				if (StringUtils.equals(lotSampleCount, "0") || StringUtils.isEmpty(lotSampleCount))
				{
					continue;
				}
				else
				{
					int tempLotSampleCount = 0;
					try
					{
						tempLotSampleCount = Integer.parseInt(lotSampleCount);
					}
					catch (Exception e)
					{
					}

					if (tempLotSampleCount > iLotSampleMaxCount)
					{
						iLotSampleMaxCount = tempLotSampleCount;
					}
				}
			}

			for (Map<String, Object> policy : policyResult)
			{
				String sProductSamplePositions = "";
				if (StringUtils.equals(ConvertUtil.getMapValueByName(policy, "PRODUCTSAMPLINGPOSITION"), "All") || StringUtils.equals(ConvertUtil.getMapValueByName(policy, "PRODUCTSAMPLINGPOSITION"), "ALL"))
				{
					sProductSamplePositions = CommonUtil.makeProductSamplingPositionList(Integer.valueOf((String) policy.get("PRODUCTSAMPLINGCOUNT")));
				}
				else
				{
					sProductSamplePositions = ConvertUtil.getMapValueByName(policy, "PRODUCTSAMPLINGPOSITION");
				}
				productSamplePositions = productSamplePositions + sProductSamplePositions + ",";
			}

			productSamplePositionList = CommonUtil.splitStringDistinct(",", productSamplePositions); // delimeter is ','

			List<String> machineList = getPossibleMachineList(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(),
					lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());

			if (machineList.size() > 0)
			{
				for (String machineName : machineList)
				{
					String sampleFlag = "N";

					List<InlineSampleLot> insertInlineSampleLotList = new ArrayList<InlineSampleLot>();
					List<InlineSampleLot> updateInlineSampleLotList = new ArrayList<InlineSampleLot>();
					List<InlineSampleProduct> insertInlineSampleProductList = new ArrayList<InlineSampleProduct>();
					List<InlineSampleProduct> updateInlineSampleProductList = new ArrayList<InlineSampleProduct>();

					log.info("Inline Sampling");

					sql.setLength(0);
					sql.append("SELECT T.LOTSAMPLECOUNT, T.CURRENTLOTCOUNT, T.TOTALLOTCOUNT ");
					sql.append("  FROM CT_INLINESAMPLELOTCOUNT T ");
					sql.append(" WHERE T.FACTORYNAME = :FACTORYNAME ");
					sql.append("   AND T.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
					sql.append("   AND T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");
					sql.append("   AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
					sql.append("   AND T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
					sql.append("   AND T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
					sql.append("   AND T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
					sql.append("   AND T.MACHINENAME = :MACHINENAME ");

					args.put("PRODUCTSPECNAME", lotData.getProductSpecName());
					args.put("PRODUCTSPECVERSION", lotData.getProductSpecVersion());
					args.put("MACHINENAME", machineName);
					@SuppressWarnings("unchecked")
					List<Map<String, Object>> sampleLotCountResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
					// GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

					int iLotSampleCount = iLotSampleMaxCount;
					int iCurrentLotCount = 0;
					int iTotalLotCount = 0;
					String action = "UPDATE";
					String productAction = "UPDATE";
					List<String> actualSamplePositionList = new ArrayList<String>();

					if (sampleLotCountResult.size() > 0)
					{
						String currentLotCount = ConvertUtil.getMapValueByName(sampleLotCountResult.get(0), "CURRENTLOTCOUNT");
						String totalLotCount = ConvertUtil.getMapValueByName(sampleLotCountResult.get(0), "TOTALLOTCOUNT");

						try
						{
							iLotSampleCount = Integer.parseInt(lotSampleCount);
							iCurrentLotCount = Integer.parseInt(currentLotCount);
							iTotalLotCount = Integer.parseInt(totalLotCount);
						}
						catch (Exception e)
						{
							//SAMPLE-0004: InlineSampleLotCount data covert fail
							throw new CustomException("SAMPLE-0004");
						}

						log.info("Lot Sample Count = " + lotSampleCount);
						log.info("Current Lot Count = " + currentLotCount);

						if (iLotSampleCount == 1)
						{
							// Reserve InlineSampling
							log.info("Reserve InlineSampling");
							iCurrentLotCount = 1;
							sampleFlag = "Y";
						}
						else if (iLotSampleCount == 0)
						{
							log.info("Skip InlineSampling");
							iCurrentLotCount = iCurrentLotCount + 1;
						}
						else if (iCurrentLotCount % iLotSampleCount == 0)
						{
							// Reserve InlineSampling
							log.info("Reserve InlineSampling");
							iCurrentLotCount = 1;
							sampleFlag = "Y";
						}
						else
						{
							log.info("Skip InlineSampling");
							iCurrentLotCount = iCurrentLotCount + 1;
						}

						// Update CT_INLINESAMPLELOTCOUNT
						InlineSampleLotCount inlineSampleLotCount = new InlineSampleLotCount();
						try
						{
							inlineSampleLotCount = ExtendedObjectProxy.getInlineSampleLotCountService().selectByKey(
									false,
									new Object[] { lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(),
											lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineName });

							inlineSampleLotCount.setCurrentLotCount(String.valueOf(iCurrentLotCount));
							inlineSampleLotCount.setTotalLotCount(String.valueOf(Integer.parseInt(totalLotCount) + 1));

							ExtendedObjectProxy.getInlineSampleLotCountService().modify(eventInfo, inlineSampleLotCount);
						}
						catch (Exception e)
						{
						}
					}
					else
					{
						// Insert CT_INLINESAMPLELOTCOUNT
						InlineSampleLotCount inlineSampleLotCount = new InlineSampleLotCount();
						inlineSampleLotCount.setFactoryName(lotData.getFactoryName());
						inlineSampleLotCount.setProductSpecName(lotData.getProductSpecName());
						inlineSampleLotCount.setProductSpecVersion(lotData.getProductSpecVersion());
						inlineSampleLotCount.setProcessFlowName(lotData.getProcessFlowName());
						inlineSampleLotCount.setProcessFlowVersion(lotData.getProcessFlowVersion());
						inlineSampleLotCount.setProcessOperationName(lotData.getProcessOperationName());
						inlineSampleLotCount.setProcessOperationVersion(lotData.getProcessOperationVersion());
						inlineSampleLotCount.setMachineName(machineName);
						inlineSampleLotCount.setLotSampleCount(String.valueOf(iLotSampleMaxCount)/* lotSampleCount */);
						inlineSampleLotCount.setCurrentLotCount("1");
						inlineSampleLotCount.setTotalLotCount("1");

						ExtendedObjectProxy.getInlineSampleLotCountService().create(eventInfo, inlineSampleLotCount);

						sampleFlag = "Y";
					}

					if (!StringUtils.equals(sampleFlag, "Y"))
						return;

					InlineSampleLot inlineSampleLot = new InlineSampleLot();
					try
					{
						inlineSampleLot = ExtendedObjectProxy.getInlineSampleLotService().selectByKey(
								false,
								new Object[] { lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(),
										lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineName });
					}
					catch (Exception e)
					{
						inlineSampleLot.setLotName(lotData.getKey().getLotName());
						inlineSampleLot.setFactoryName(lotData.getFactoryName());
						inlineSampleLot.setProductSpecName(lotData.getProductSpecName());
						inlineSampleLot.setProductSpecVersion(lotData.getProductSpecVersion());
						inlineSampleLot.setProcessFlowName(lotData.getProcessFlowName());
						inlineSampleLot.setProcessFlowVersion(lotData.getProcessFlowVersion());
						inlineSampleLot.setProcessOperationName(lotData.getProcessOperationName());
						inlineSampleLot.setProcessOperationVersion(lotData.getProcessOperationVersion());
						inlineSampleLot.setMachineName(machineName);
						action = "INSERT";
					}

					StringBuffer sqlP = new StringBuffer();
					sqlP.append("SELECT PRODUCTNAME, POSITION ");
					sqlP.append("  FROM PRODUCT ");
					sqlP.append(" WHERE LOTNAME = :LOTNAME ");
					sqlP.append("   AND POSITION IN (:POSITION) ");
					sqlP.append("ORDER BY POSITION ");

					Map<String, Object> bindMap = new HashMap<String, Object>();
					bindMap.put("LOTNAME", lotData.getKey().getLotName());
					bindMap.put("POSITION", productSamplePositionList);

					@SuppressWarnings("unchecked")
					List<Map<String, Object>> sampleProductResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlP.toString(), bindMap);

					for (Map<String, Object> sampleProduct : sampleProductResult) // Exist the Product for productSamplePosition
					{
						String productName = ConvertUtil.getMapValueByName(sampleProduct, "PRODUCTNAME");
						String position = ConvertUtil.getMapValueByName(sampleProduct, "POSITION");

						InlineSampleProduct inlineSampleProduct = new InlineSampleProduct();

						try
						{
							inlineSampleProduct = ExtendedObjectProxy.getInlineSampleProductService().selectByKey(
									false,
									new Object[] { productName, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(),
											lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineName });

							productAction = "UPDATE";
						}
						catch (Exception e)
						{
							inlineSampleProduct.setProductName(productName);
							inlineSampleProduct.setLotName(lotData.getKey().getLotName());
							inlineSampleProduct.setFactoryName(lotData.getFactoryName());
							inlineSampleProduct.setProductSpecName(lotData.getProductSpecName());
							inlineSampleProduct.setProductSpecVersion(lotData.getProductSpecVersion());
							inlineSampleProduct.setProcessFlowName(lotData.getProcessFlowName());
							inlineSampleProduct.setProcessFlowVersion(lotData.getProcessFlowVersion());
							inlineSampleProduct.setProcessOperationName(lotData.getProcessOperationName());
							inlineSampleProduct.setProcessOperationVersion(lotData.getProcessOperationVersion());
							inlineSampleProduct.setMachineName(machineName);
							productAction = "INSERT";
						}

						String inspectionFlag = getInspectionFlagForInlineSample(lotData, machineName, productName);

						inlineSampleProduct.setActualSamplePosition(position);
						inlineSampleProduct.setInspectionFlag(inspectionFlag);
						inlineSampleProduct.setProductSampleFlag("Y");
						inlineSampleProduct.setEventComment(eventInfo.getEventComment());
						inlineSampleProduct.setEventUser(eventInfo.getEventUser());

						if (!actualSamplePositionList.contains(position))
						{
							actualSamplePositionList.add(position);
						}

						if (StringUtils.equals(productAction, "INSERT"))
						{
							insertInlineSampleProductList.add(inlineSampleProduct);
						}
						else if (StringUtils.equals(productAction, "UPDATE"))
						{
							updateInlineSampleProductList.add(inlineSampleProduct);
						}
					}

					inlineSampleLot.setLotSampleFlag("Y");
					inlineSampleLot.setLotSampleCount(String.valueOf(iLotSampleMaxCount));
					inlineSampleLot.setCurrentLotCount(String.valueOf(iCurrentLotCount));
					inlineSampleLot.setTotalLotCount(String.valueOf(iTotalLotCount));
					inlineSampleLot.setProductSampleCount(String.valueOf(productSamplePositionList.size()));
					inlineSampleLot.setProductSampleposition(CommonUtil.toStringWithoutBrackets(productSamplePositionList));
					inlineSampleLot.setActualProductCount(String.valueOf(actualSamplePositionList.size()));
					inlineSampleLot.setActualSampleposition(CommonUtil.toStringWithoutBrackets(actualSamplePositionList));
					inlineSampleLot.setManualSampleFlag("N");
					inlineSampleLot.setEventUser(eventInfo.getEventUser());
					inlineSampleLot.setEventComment(eventInfo.getEventComment());
					inlineSampleLot.setLotGrade(lotData.getLotGrade());
					inlineSampleLot.setPriority(1);

					if (StringUtils.equals(action, "INSERT"))
					{
						insertInlineSampleLotList.add(inlineSampleLot);
					}
					else if (StringUtils.equals(action, "UPDATE"))
					{
						updateInlineSampleLotList.add(inlineSampleLot);
					}

					if (insertInlineSampleLotList.size() > 0)
					{
						ExtendedObjectProxy.getInlineSampleLotService().create(eventInfo, insertInlineSampleLotList);
					}
					if (updateInlineSampleLotList.size() > 0)
					{
						ExtendedObjectProxy.getInlineSampleLotService().modify(eventInfo, updateInlineSampleLotList);
					}
					if (insertInlineSampleProductList.size() > 0)
					{
						ExtendedObjectProxy.getInlineSampleProductService().create(eventInfo, insertInlineSampleProductList);
					}
					if (updateInlineSampleProductList.size() > 0)
					{
						ExtendedObjectProxy.getInlineSampleProductService().modify(eventInfo, updateInlineSampleProductList);
					}
				}
			}
		}
	}

	public List<String> getPossibleMachineList(String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion) throws CustomException
	{
		List<String> machineList = new ArrayList<String>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT P.MACHINENAME ");
		sql.append("   FROM TPFOPOLICY T, POSMACHINE P ");
		sql.append("   WHERE T.CONDITIONID = P.CONDITIONID ");
		sql.append("   AND T.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND T.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("   AND T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");
		sql.append("   AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		sql.append("   ORDER BY P.MACHINENAME ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", factoryName);
		args.put("PRODUCTSPECNAME", productSpecName);
		args.put("PRODUCTSPECVERSION", productSpecVersion);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);
		args.put("PROCESSOPERATIONNAME", processOperationName);
		args.put("PROCESSOPERATIONVERSION", processOperationVersion);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			machineList = CommonUtil.makeListBySqlResult(result, "MACHINENAME");
		}

		return machineList;
	}

	public List<Map<String, Object>> getInlineMachineList(String factoryName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion,
			String machineName) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT P.UNITNAME, P.SEQ ");
		sql.append("  FROM TFOMPOLICY T, POSINLINEMACHINE P ");
		sql.append(" WHERE T.CONDITIONID = P.CONDITIONID ");
		sql.append("   AND T.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		sql.append("   AND T.MACHINENAME = :MACHINENAME ");
		sql.append("ORDER BY P.SEQ DESC ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", factoryName);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);
		args.put("PROCESSOPERATIONNAME", processOperationName);
		args.put("PROCESSOPERATIONVERSION", processOperationVersion);
		args.put("MACHINENAME", machineName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			return result;
		}
		else
			return null;
	}

	public String getInspectionFlagForInlineSample(Lot lotData, String machineName, String productName) throws CustomException
	{
		String inspectionFlag = "";

		List<Map<String, Object>> inLineMachineList = getInlineMachineList(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(),
				lotData.getProcessOperationVersion(), machineName);
		if (inLineMachineList == null || inLineMachineList.size() == 0)
		{
			return null;
		}
		int count = 1;
		if (inLineMachineList.get(0).get("SEQ") != null)
		{
			count = Integer.valueOf(inLineMachineList.get(0).get("SEQ").toString());
		}
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT P.PRODUCTNAME, LV.POSITION, P.PRODUCTSPECNAME, NULL AS MACHINENAME, ");
		for (int i = 1; i <= count; i++)
		{
			sql.append("       MIN (UNIT" + i + ") ");
			if (i < count)
				sql.append(" || ");
		}
		sql.append("  AS INSPECTIONFLAG ");
		sql.append("  FROM (SELECT ");
		for (int i = 1; i <= count; i++)
		{
			sql.append(" CASE WHEN SEQ = '" + i + "' THEN 'O' ELSE 'X' END AS UNIT" + i + ", ");
		}
		sql.append("               SEQ, ");
		sql.append("               EACHPRODUCTSAMPLINGPOSITION ");
		sql.append("          FROM (SELECT PM.SEQ, ");
		sql.append("                       PS.UNITNAME, ");
		sql.append("                       REGEXP_SUBSTR (PS.PRODUCTSAMPLINGPOSITION, '[^,]+', 1, LV) AS EACHPRODUCTSAMPLINGPOSITION ");
		sql.append("                  FROM TFOMPOLICY T, ");
		sql.append("                       POSINLINEMACHINE PM, ");
		sql.append("                       POSINLINESAMPLE PS, ");
		sql.append("                       (SELECT LEVEL AS LV FROM DUAL CONNECT BY LEVEL <= 26) ");
		sql.append("                 WHERE T.FACTORYNAME = :FACTORYNAME ");
		sql.append("                   AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("                   AND T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("                   AND T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("                   AND T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		sql.append("                   AND T.MACHINENAME = :MACHINENAME ");
		sql.append("                   AND T.CONDITIONID = PM.CONDITIONID ");
		sql.append("                   AND PM.CONDITIONID = PS.CONDITIONID ");
		sql.append("                   AND PS.PRODUCTSAMPLINGPOSITION IS NOT NULL ");
		sql.append("                   AND PS.LOTSAMPLINGCOUNT IS NOT NULL ");
		sql.append("                   AND PS.LOTSAMPLINGCOUNT != '0' ");
		sql.append("                   AND PM.UNITNAME = PS.UNITNAME ");
		sql.append("                   AND LV <= LENGTH (PS.PRODUCTSAMPLINGPOSITION) - LENGTH (REPLACE (PS.PRODUCTSAMPLINGPOSITION, ',')) + 1)) M, ");
		sql.append("       (SELECT LEVEL AS POSITION FROM DUAL CONNECT BY NOCYCLE LEVEL <= 26) LV, PRODUCT P ");
		sql.append(" WHERE M.EACHPRODUCTSAMPLINGPOSITION(+) = LV.POSITION ");
		sql.append("   AND LV.POSITION = P.POSITION ");
		sql.append("   AND P.PRODUCTNAME = :PRODUCTNAME ");
		sql.append("GROUP BY EACHPRODUCTSAMPLINGPOSITION, P.PRODUCTNAME, LV.POSITION, P.PRODUCTSPECNAME ");
		sql.append("ORDER BY LV.POSITION ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", lotData.getFactoryName());
		args.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
		args.put("PROCESSFLOWVERSION", lotData.getProcessFlowVersion());
		args.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
		args.put("PROCESSOPERATIONVERSION", lotData.getProcessOperationVersion());
		args.put("MACHINENAME", machineName);
		args.put("PRODUCTNAME", productName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			inspectionFlag = ConvertUtil.getMapValueByName(result.get(0), "INSPECTIONFLAG");
			log.info(" InspectionFlag = " + inspectionFlag);
		}
		return inspectionFlag;
	}

	public void setSamplingListData(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		eventInfo = EventInfoUtil.makeEventInfo("Sampling", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		// 0. get all sampling Flow List (joined TFOMPOLICY & POSSAMPLE)
		List<Map<String, Object>> samplePolicyList = MESLotServiceProxy.getLotServiceUtil().getSamplePolicyList(lotData.getFactoryName(), lotData.getProcessFlowName(),
				lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), lotData.getMachineName(), "", "", "", "");

		for (Map<String, Object> samplePolicyM : samplePolicyList)
		{
			if (checkReserveSampling(lotData, samplePolicyM))
			{
				MESLotServiceProxy.getLotServiceUtil().setSamplingData(eventInfo, lotData, samplePolicyM);

				log.info("");
			}
		}
	}

	public void setSamplingListData(EventInfo eventInfo, Lot lotData, Machine machineData, List<Element> productListElement) throws CustomException
	{
		eventInfo = EventInfoUtil.makeEventInfo("Sampling", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		// 0. get all sampling Flow List (joined TFOMPOLICY & POSSAMPLE)
		List<Map<String, Object>> samplePolicyList = MESLotServiceProxy.getLotServiceUtil().getSamplePolicyList(lotData.getFactoryName(), lotData.getProcessFlowName(),
				lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), lotData.getMachineName(), "", "", "", "");
		
		String LTTSamplingFlag="";
		ELACondition dataInfo  = null;	
		if(StringUtils.equals(machineData.getMachineGroupName(), "ELA"))
		{		
			log.info("Start LTT Sampling Validaiton");
			try
			{
			    dataInfo = ExtendedObjectProxy.getELAConditionService().selectByKey(false, new Object[] { machineData.getKey().getMachineName(), lotData.getProductSpecName() });
				if (dataInfo.getNfcEarly() != null && StringUtils.isNotEmpty(lotData.getUdfs().get("ELANFC"))&&
						Double.parseDouble(lotData.getUdfs().get("ELANFC")) < dataInfo.getNfcEarly().doubleValue())
				{
					LTTSamplingFlag="Early";
				}
				if (dataInfo.getNfcLate() != null && StringUtils.isNotEmpty(lotData.getUdfs().get("ELANFC"))&&
						Double.parseDouble(lotData.getUdfs().get("ELANFC")) > dataInfo.getNfcLate().doubleValue())
				{
					LTTSamplingFlag="Late";
				}
				if (dataInfo.getNfcMidLower() != null &&dataInfo.getNfcMidUpper() != null&& StringUtils.isNotEmpty(lotData.getUdfs().get("ELANFC"))&&
						Double.parseDouble(lotData.getUdfs().get("ELANFC")) >= dataInfo.getNfcMidLower().doubleValue()
						&&Double.parseDouble(lotData.getUdfs().get("ELANFC")) <= dataInfo.getNfcMidUpper().doubleValue())
				{
					LTTSamplingFlag="Mid";
				}
			}
			catch (Exception ex)
			{
	           log.info("LTT Sampling not found ELACondition");
			}
			log.info("End LTT Sampling Validaiton,LTTSamplingFlag : "+LTTSamplingFlag);
		}
		
		for (Map<String, Object> samplePolicyM : samplePolicyList)
		{
			ProcessOperationSpec operationSpecData = 
					CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), (String) samplePolicyM.get("TOPROCESSOPERATIONNAME"),(String) samplePolicyM.get("TOPROCESSOPERATIONVERSION"));
			
			if(StringUtils.equals(machineData.getMachineGroupName(), "ELA")
					&&(StringUtils.equals(operationSpecData.getDetailProcessOperationType(), "LTT_V")
							||StringUtils.equals(operationSpecData.getDetailProcessOperationType(), "LTT_H"))&&StringUtils.isNotEmpty(LTTSamplingFlag))
			{
				
				List<Element> newProductListElement = CommonUtil.makeElementProdListForDummy(productListElement);
				newProductListElement = CommonUtil.makeElementProdListForFirstGlass(productListElement,lotData.getKey().getLotName());
			    MESLotServiceProxy.getLotServiceUtil().setSamplingDataForLTT(eventInfo, lotData, samplePolicyM, newProductListElement,machineData, operationSpecData.getDetailProcessOperationType(),dataInfo,LTTSamplingFlag);
			    log.info("ELA Set Sampling List Data END");				
			}
			else if (checkReserveSampling(lotData, samplePolicyM))
			{
				List<Element> newProductListElement = CommonUtil.makeElementProdListForDummy(productListElement);
				newProductListElement = CommonUtil.makeElementProdListForFirstGlass(productListElement,lotData.getKey().getLotName());
				MESLotServiceProxy.getLotServiceUtil().setSamplingData(eventInfo, lotData, samplePolicyM, newProductListElement);
				log.info("Normal Set Sampling List Data END");
			}
		}

		// 1. get all chamberSampling Flow List (joined TFOMPOLICY & POSCHAMBERSAMPLE)
		List<Map<String, Object>> chamberSamplePolicyList = MESLotServiceProxy.getLotServiceUtil().getChamberSamplePolicyList(lotData.getFactoryName(), lotData.getProcessFlowName(),
				lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), lotData.getMachineName(), "", "", "", "");

		EventInfoExtended chamberSampleEventInfo = new EventInfoExtended(eventInfo);
		chamberSampleEventInfo.setEventComment("[Chamber] System ChamberSampling Normal. " + eventInfo.getEventComment());
		chamberSampleEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		for (Map<String, Object> chamberSamplePolicyM : chamberSamplePolicyList)
		{
			if ("N".equals(chamberSamplePolicyM.get("TFESAMPLEFLAG")))
			{
				// ChamberSamping Case
				MESLotServiceProxy.getLotServiceUtil().setChamberSamplingData(chamberSampleEventInfo, lotData, machineData, chamberSamplePolicyM, lotData.getFactoryName(), lotData.getProductSpecName(),
						lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());				
			}
		}
	}

	public boolean checkReserveSampling(Lot lotdata, Map<String, Object> samplePolicyM) throws CustomException
	{
		Node sampleNode = new Node();

		sampleNode = MESLotServiceProxy.getLotServiceUtil().getReserveSampleLotData(lotdata, samplePolicyM);

		if (sampleNode != null && sampleNode.getProcessFlowName().equals(samplePolicyM.get("TOPROCESSFLOWNAME"))
				&& sampleNode.getProcessFlowVersion().equals(samplePolicyM.get("TOPROCESSFLOWVERSION")) && sampleNode.getNodeAttribute1().equals(samplePolicyM.get("TOPROCESSOPERATIONNAME"))
				&& sampleNode.getNodeAttribute2().equals(samplePolicyM.get("TOPROCESSOPERATIONVERSION")))
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	public void deleteInlineSamplingData(EventInfo eventInfo, Lot lotData, List<Element> productList, String machineName, boolean isManual)
	{
		InlineSampleLot inlineSampleLot = new InlineSampleLot();

		try
		{
			inlineSampleLot = ExtendedObjectProxy.getInlineSampleLotService().selectByKey(
					false,
					new Object[] { lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(),
							lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineName });
			try
			{
				ExtendedObjectProxy.getInlineSampleLotService().remove(eventInfo, inlineSampleLot);
			}
			catch (Exception e)
			{
				log.info("Error occured to delete InlineSampleLot Data");
			}
		}
		catch (Exception e)
		{
			log.info("Not exist InlineSampleLot Data");
		}

		try
		{
			List<InlineSampleProduct> InlineSampleProductList = ExtendedObjectProxy.getInlineSampleProductService().select(
					" lotName = ? and factoryName = ? and productSpecName = ? and productSpecVersion = ? "
							+ "and processFlowName = ? and processFlowVersion = ? and processOperationName = ? and processOperationVersion = ? and machineName = ? ",
					new Object[] { lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(),
							lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineName });
			try
			{
				ExtendedObjectProxy.getInlineSampleProductService().remove(eventInfo, InlineSampleProductList);
			}
			catch (Exception e)
			{
				log.info("Error occured to delete InlineSampleProduct Data");
			}
		}
		catch (Exception e)
		{
			log.info("Not exist InlineSampleProduct Data");
		}
	}

	public void deleteSamplingData(EventInfo eventInfo, Lot afterTrackOutLot, List<Element> productList, boolean isManual) throws CustomException
	{
		List<SampleLot> sampleLotList = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListByToInfo(afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
				afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), afterTrackOutLot.getProcessFlowName(), afterTrackOutLot.getProcessFlowVersion(),
				afterTrackOutLot.getProcessOperationName(), afterTrackOutLot.getProcessOperationVersion());

		if (sampleLotList != null)
		{
			List<SampleProduct> sampleProductList = new ArrayList<SampleProduct>();

			if (isManual)
			{
				ExtendedObjectProxy.getSampleProductService().deleteSampleProductByLotNameAndToInfo(eventInfo, afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
						afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), sampleLotList.get(0).getToProcessFlowName(), sampleLotList.get(0).getToProcessFlowVersion(),
						sampleLotList.get(0).getToProcessOperationName(), sampleLotList.get(0).getToProcessOperationVersion());
			}
			else
			{
				sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListByLotNameAndToInfo(afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
						afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), afterTrackOutLot.getProcessFlowName(), afterTrackOutLot.getProcessFlowVersion(),
						afterTrackOutLot.getProcessOperationName(), afterTrackOutLot.getProcessOperationVersion());

				for (SampleProduct sampleProduct : sampleProductList)
				{
					for (Element productE : productList)
					{
						try
						{
							String p = productE.getChildText("PRODUCTNAME").toString();
							String sp = sampleProduct.getProductName();

							if (p.equals(sp))
							{
								ExtendedObjectProxy.getSampleProductService().deleteSampleProductByToInfo(eventInfo, productE.getChildText("PRODUCTNAME"), afterTrackOutLot.getKey().getLotName(),
										afterTrackOutLot.getFactoryName(), afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(),
										sampleLotList.get(0).getToProcessFlowName(), sampleLotList.get(0).getToProcessFlowVersion(), sampleLotList.get(0).getToProcessOperationName(),
										sampleLotList.get(0).getToProcessOperationVersion());
							}
						}
						catch (Exception e)
						{
						}
					}
				}
			}

			sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListByLotNameAndToInfo(afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
					afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), afterTrackOutLot.getProcessFlowName(), afterTrackOutLot.getProcessFlowVersion(),
					afterTrackOutLot.getProcessOperationName(), afterTrackOutLot.getProcessOperationVersion());

			if (sampleProductList == null)
			{
				ExtendedObjectProxy.getSampleLotService().deleteSampleLotDataByToInfo(eventInfo, afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
						afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), sampleLotList.get(0).getToProcessFlowName(), sampleLotList.get(0).getToProcessFlowVersion(),
						sampleLotList.get(0).getToProcessOperationName(), sampleLotList.get(0).getToProcessOperationVersion());
			}
		}
	}

	public List<SampleLot> deleteSamplingDataReturn(EventInfo eventInfo, Lot afterTrackOutLot, List<Element> productList, boolean isManual) throws CustomException
	{
		eventInfo.setEventName("TrackOut");
		
		List<SampleLot> sampleLotList = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListByToInfo(afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
				afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), afterTrackOutLot.getProcessFlowName(), afterTrackOutLot.getProcessFlowVersion(),
				afterTrackOutLot.getProcessOperationName(), afterTrackOutLot.getProcessOperationVersion());

		if (sampleLotList != null)
		{
			List<SampleProduct> sampleProductList = new ArrayList<SampleProduct>();

			if (isManual)
			{
				ExtendedObjectProxy.getSampleProductService().deleteSampleProductByLotNameAndToInfo(eventInfo, afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
						afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), sampleLotList.get(0).getToProcessFlowName(), sampleLotList.get(0).getToProcessFlowVersion(),
						sampleLotList.get(0).getToProcessOperationName(), sampleLotList.get(0).getToProcessOperationVersion());
			}
			else
			{
				sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListByLotNameAndToInfo(afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
						afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), afterTrackOutLot.getProcessFlowName(), afterTrackOutLot.getProcessFlowVersion(),
						afterTrackOutLot.getProcessOperationName(), afterTrackOutLot.getProcessOperationVersion());

				for (SampleProduct sampleProductM : sampleProductList)
				{
					for (Element productE : productList)
					{
						try
						{
							String p = productE.getChildText("PRODUCTNAME").toString();
							String sp = sampleProductM.getProductName();

							if (p.equals(sp))
							{
								ExtendedObjectProxy.getSampleProductService().deleteSampleProductByToInfo(eventInfo, productE.getChildText("PRODUCTNAME"), afterTrackOutLot.getKey().getLotName(),
										afterTrackOutLot.getFactoryName(), afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(),
										sampleLotList.get(0).getToProcessFlowName(), sampleLotList.get(0).getToProcessFlowVersion(), sampleLotList.get(0).getToProcessOperationName(),
										sampleLotList.get(0).getToProcessOperationVersion());
							}
						}
						catch (Exception e)
						{
						}
					}
				}
			}

			sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListByLotNameAndToInfo(afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
					afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), afterTrackOutLot.getProcessFlowName(), afterTrackOutLot.getProcessFlowVersion(),
					afterTrackOutLot.getProcessOperationName(), afterTrackOutLot.getProcessOperationVersion());

			if (sampleProductList == null)
			{
				ExtendedObjectProxy.getSampleLotService().deleteSampleLotDataByToInfo(eventInfo, afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
						afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), sampleLotList.get(0).getToProcessFlowName(), sampleLotList.get(0).getToProcessFlowVersion(),
						sampleLotList.get(0).getToProcessOperationName(), sampleLotList.get(0).getToProcessOperationVersion());
			}
		}

		return sampleLotList;
	}

	public List<SampleLot> deleteSamplingDataReturn(EventInfo eventInfo, Lot afterTrackOutLot, boolean isManual) throws CustomException
	{
		List<SampleLot> sampleLotList = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListByToInfo(afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
				afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), afterTrackOutLot.getProcessFlowName(), afterTrackOutLot.getProcessFlowVersion(),
				afterTrackOutLot.getProcessOperationName(), afterTrackOutLot.getProcessOperationVersion());

		List<SampleProduct> sampleProductList = new ArrayList<SampleProduct>();

		if (sampleLotList != null)
		{
			if (isManual)
			{
				ExtendedObjectProxy.getSampleProductService().deleteSampleProductByLotNameAndToInfo(eventInfo, afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
						afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), sampleLotList.get(0).getToProcessFlowName(), sampleLotList.get(0).getToProcessFlowVersion(),
						sampleLotList.get(0).getToProcessOperationName(), sampleLotList.get(0).getToProcessOperationVersion());
			}
			else
			{
				sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListByLotNameAndToInfo(afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
						afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), afterTrackOutLot.getProcessFlowName(), afterTrackOutLot.getProcessFlowVersion(),
						afterTrackOutLot.getProcessOperationName(), afterTrackOutLot.getProcessOperationVersion());

				for (SampleProduct sampleProduct : sampleProductList)
				{
					try
					{
						ExtendedObjectProxy.getSampleProductService().deleteSampleProductByToInfo(eventInfo, sampleProduct.getProductName(), afterTrackOutLot.getKey().getLotName(),
								afterTrackOutLot.getFactoryName(), afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), sampleLotList.get(0).getToProcessFlowName(),
								sampleLotList.get(0).getToProcessFlowVersion(), sampleLotList.get(0).getToProcessOperationName(), sampleLotList.get(0).getToProcessOperationVersion());
					}
					catch (Exception e)
					{
					}
				}
			}
		}

		sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListByLotNameAndToInfo(afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
				afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), afterTrackOutLot.getProcessFlowName(), afterTrackOutLot.getProcessFlowVersion(),
				afterTrackOutLot.getProcessOperationName(), afterTrackOutLot.getProcessOperationVersion());

		if (sampleProductList == null)
		{
			if (sampleLotList != null)
			{
				ExtendedObjectProxy.getSampleLotService().deleteSampleLotDataByToInfo(eventInfo, afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
						afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), sampleLotList.get(0).getToProcessFlowName(), sampleLotList.get(0).getToProcessFlowVersion(),
						sampleLotList.get(0).getToProcessOperationName(), sampleLotList.get(0).getToProcessOperationVersion());
			}
		}
		return sampleLotList;
	}

	public void deleteSamplingDataBetweenTwoOperations(EventInfo eventInfo, Lot lotData, List<Product> productDataList, ProcessOperationSpec beforeOperationSpecData,
			ProcessOperationSpec newOperationSpecData) throws CustomException
	{
		String factoryName = lotData.getFactoryName();
		String lotName = lotData.getKey().getLotName();
		String productSpecName = lotData.getProductSpecName();
		String productSpecVersion = lotData.getProductSpecVersion();
		String processFlowName = lotData.getProcessFlowName();
		String processFlowVersion = lotData.getProcessFlowVersion();
		String beforeOperationName = beforeOperationSpecData.getKey().getProcessOperationName();
		String beforeOperationVersion = beforeOperationSpecData.getKey().getProcessOperationVersion();
		String newOperationName = newOperationSpecData.getKey().getProcessOperationName();
		String newOperationVersion = newOperationSpecData.getKey().getProcessOperationVersion();
		List<Map<String, Object>> operationList = operationList(factoryName, processFlowName, processFlowVersion);

		boolean continueFlag = true;
		boolean breakFlag = false;
		for (Map<String, Object> operation : operationList)
		{
			String processOperationName = ConvertUtil.getMapValueByName(operation, "PROCESSOPERATIONNAME");
			String processOperationVersion = ConvertUtil.getMapValueByName(operation, "PROCESSOPERATIONVERSION");

			if (StringUtils.equals(beforeOperationName, processOperationName) && StringUtils.equals(beforeOperationVersion, processOperationVersion))
			{
				continueFlag = false;
			}
			else if (StringUtils.equals(newOperationName, processOperationName) && StringUtils.equals(newOperationVersion, processOperationVersion))
			{
				breakFlag = true;
			}

			if (breakFlag)
				break;
			if (continueFlag)
				continue;

			// Handling if lot is rework state.
			// If the lot is rework state, ProcessOperationName of SampleLot is 'ProcessOperationName' of the lot before rework.
			// SampleProduct is 'ProcessFlowName.ProcessOperationName' of the lot before rework.

			List<SampleLot> sampleLotList = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListByInfo(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
					lotData.getProductSpecVersion(), processFlowName, processFlowVersion, processOperationName, processOperationVersion);

			if (sampleLotList == null || sampleLotList.size() == 0)
			{
				continue;
			}

			for (SampleLot sampleLot : sampleLotList)
			{
				for (Product productData : productDataList)
				{
					try
					{
						String productName = productData.getKey().getProductName();

						ExtendedObjectProxy.getSampleProductService().deleteSampleProductWithoutMachineName(eventInfo, productName, lotName, factoryName, productSpecName, productSpecVersion,
								processFlowName, processFlowVersion, processOperationName, processOperationVersion, sampleLot.getToProcessFlowName(), sampleLot.getToProcessFlowVersion(),
								sampleLot.getToProcessOperationName(), sampleLot.getToProcessOperationVersion());
					}
					catch (Exception e)
					{
					}
				}
			}

			List<SampleProduct> sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListByLotName(lotName, factoryName, productSpecName, productSpecVersion,
					processFlowName, processFlowVersion, processOperationName, processOperationVersion, sampleLotList.get(0).getToProcessFlowName(), sampleLotList.get(0).getToProcessFlowVersion(),
					sampleLotList.get(0).getToProcessOperationName(), sampleLotList.get(0).getToProcessOperationVersion());

			if (sampleProductList == null)
			{
				for (SampleLot sampleLot : sampleLotList)
				{
					ExtendedObjectProxy.getSampleLotService().deleteSampleLotDataByToInfo(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
							lotData.getProductSpecVersion(), sampleLot.getToProcessFlowName(), sampleLot.getToProcessFlowVersion(), sampleLot.getToProcessOperationName(),
							sampleLot.getToProcessOperationVersion());
				}
			}
		}
	}

	public void deleteSamplingForCompleteRework(EventInfo eventInfo, Lot afterTrackOutLot, List<Element> productList, boolean isManual) throws CustomException
	{
		List<SampleLot> sampleLotList = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListByToFlow(afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
				afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), afterTrackOutLot.getProcessFlowName(), afterTrackOutLot.getProcessFlowVersion());

		if (sampleLotList != null)
		{
			for (SampleLot sampleLot : sampleLotList)
			{
				List<SampleProduct> sampleProductList = new ArrayList<SampleProduct>();

				if(StringUtils.equals(sampleLot.getForceSamplingFlag(), GenericServiceProxy.getConstantMap().FLAG_Y))
					throw new CustomException("LOT-3008");
				
				if (isManual)
				{
					ExtendedObjectProxy.getSampleProductService().deleteSampleProductByToFlow(eventInfo, afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
							afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), sampleLot.getToProcessFlowName(), sampleLot.getToProcessFlowVersion());
				}
				else
				{
					sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListByLotNameAndToInfo(afterTrackOutLot.getKey().getLotName(),
							afterTrackOutLot.getFactoryName(), afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), afterTrackOutLot.getProcessFlowName(),
							afterTrackOutLot.getProcessFlowVersion(), sampleLot.getToProcessOperationName(), sampleLot.getToProcessOperationVersion());

					for (SampleProduct sampleProduct : sampleProductList)
					{
						for (Element productE : productList)
						{
							try
							{
								if (productE.getChildText("PRODUCTNAME").equals(sampleProduct.getProductName()))
								{
									ExtendedObjectProxy.getSampleProductService().deleteSampleProductByToInfo(eventInfo, productE.getChildText("PRODUCTNAME"), afterTrackOutLot.getKey().getLotName(),
											afterTrackOutLot.getFactoryName(), afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), sampleLot.getToProcessFlowName(),
											sampleLot.getToProcessFlowVersion(), sampleLot.getToProcessOperationName(), sampleLot.getToProcessOperationVersion());
								}
							}
							catch (Exception e)
							{
							}
						}
					}
				}

				sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListByLotNameAndToInfo(afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
						afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), afterTrackOutLot.getProcessFlowName(), afterTrackOutLot.getProcessFlowVersion(),
						sampleLot.getToProcessOperationName(), sampleLot.getToProcessOperationVersion());

				if (sampleProductList == null)
				{
					ExtendedObjectProxy.getSampleLotService().deleteSampleLotDataByToInfo(eventInfo, afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
							afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), sampleLot.getToProcessFlowName(), sampleLot.getToProcessFlowVersion(),
							sampleLot.getToProcessOperationName(), sampleLot.getToProcessOperationVersion());
				}
			}
		}
	}

	public void deleteSamplingForCompleteForceSampling(EventInfo eventInfo, Lot afterTrackOutLot, List<Element> productList, boolean isManual) throws CustomException
	{
		List<SampleLot> sampleLotList = ExtendedObjectProxy.getSampleLotService().getForceSampleLotDataAll(afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
				afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), afterTrackOutLot.getProcessFlowName(), afterTrackOutLot.getProcessFlowVersion());

		if (sampleLotList.size() > 0)
		{
			for (SampleLot sampleLotM : sampleLotList)
			{
				List<SampleProduct> sampleProductList = new ArrayList<SampleProduct>();

				if (isManual)
				{
					ExtendedObjectProxy.getSampleProductService().deleteSampleProductByLotNameAndToInfo(eventInfo, afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
							afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), sampleLotM.getToProcessFlowName(), sampleLotM.getToProcessFlowVersion(),
							sampleLotM.getToProcessOperationName(), sampleLotM.getToProcessOperationVersion());
				}
				else
				{
					sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListByLotNameAndToInfo(afterTrackOutLot.getKey().getLotName(),
							afterTrackOutLot.getFactoryName(), afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), sampleLotM.getToProcessFlowName(),
							sampleLotM.getToProcessFlowVersion(), sampleLotM.getToProcessOperationName(), sampleLotM.getToProcessOperationVersion());

					for (SampleProduct sampleProductM : sampleProductList)
					{
						for (Element productE : productList)
						{
							try
							{
								if (StringUtils.equals(productE.getChildText("PRODUCTNAME"), sampleProductM.getProductName()))
								{
									ExtendedObjectProxy.getSampleProductService().deleteSampleProductByToInfo(eventInfo, productE.getChildText("PRODUCTNAME"), afterTrackOutLot.getKey().getLotName(),
											afterTrackOutLot.getFactoryName(), afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), sampleLotM.getToProcessFlowName(),
											sampleLotM.getToProcessFlowVersion(), sampleLotM.getToProcessOperationName(), sampleLotM.getToProcessOperationVersion());
								}
							}
							catch (Exception e)
							{
							}
						}
					}
				}

				sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListByLotNameAndToInfo(afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
						afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), sampleLotM.getToProcessFlowName(), sampleLotM.getToProcessFlowVersion(),
						sampleLotM.getToProcessOperationName(), sampleLotM.getToProcessOperationVersion());

				if (sampleProductList == null)
				{
					ExtendedObjectProxy.getSampleLotService().deleteSampleLotDataByToInfo(eventInfo, afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
							afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), sampleLotM.getToProcessFlowName(), sampleLotM.getToProcessFlowVersion(),
							sampleLotM.getToProcessOperationName(), sampleLotM.getToProcessOperationVersion());
				}
			}

		}
	}

	public Element createSortJobBodyElement(String machineName, String jobName) throws CustomException
	{
		Element bodyElement = new Element("Body");

		// MACHINENAME
		Element machineNameElement = new Element("MACHINENAME");
		machineNameElement.setText(machineName);
		bodyElement.addContent(machineNameElement);

		// JOBNAME
		Element jobNameElement = new Element("JOBNAME");
		jobNameElement.setText(jobName);
		bodyElement.addContent(jobNameElement);

		// FROMLOTLIST
		bodyElement.addContent(this.createSortJobFromLotInfoElement(jobName));

		return bodyElement;
	}

	public Element createSortJobFromLotInfoElement(String jobName) throws CustomException
	{
		List<SortJobCarrier> sortCarrierList = ExtendedObjectProxy.getSortJobCarrierService().getSortJobCarrierListByJobName(jobName);

		Element fromLotListElement = new Element("FROMLOTLIST");

		for (SortJobCarrier fromLot : sortCarrierList)
		{
			String fromLotName = fromLot.getLotName();
			String fromPortName = fromLot.getPortName();
			String fromCarrierName = fromLot.getCarrierName();

			Element fromLotElement = new Element("FROMLOT");

			// LOTNAME
			Element lotNameElement = new Element("LOTNAME");
			lotNameElement.setText(fromLotName);
			fromLotElement.addContent(lotNameElement);

			// PORTNAME
			Element portNameElement = new Element("PORTNAME");
			portNameElement.setText(fromPortName);
			fromLotElement.addContent(portNameElement);

			// CARRIERNAME
			Element carrierNameElement = new Element("CARRIERNAME");
			carrierNameElement.setText(fromCarrierName);
			fromLotElement.addContent(carrierNameElement);

			// SORTERPRODUCTLIST
			fromLotElement.addContent(createSortJobProductInfoElement(jobName, fromCarrierName));

			fromLotListElement.addContent(fromLotElement);
		}

		return fromLotListElement;
	}

	public Element createSortJobProductInfoElement(String jobName, String CarrierName) throws CustomException
	{
		List<SortJobProduct> sortJobProductList = ExtendedObjectProxy.getSortJobProductService().getSortJobProductListByFromCarrier(jobName, CarrierName);

		Element sorterProductListElement = new Element("SORTERPRODUCTLIST");

		for (SortJobProduct sortJobProduct : sortJobProductList)
		{
			Element soterProductElement = new Element("SORTERPRODUCT");

			// PRODUCTNAME
			Element productNameElement = new Element("PRODUCTNAME");
			productNameElement.setText(sortJobProduct.getProductName());
			soterProductElement.addContent(productNameElement);

			// FROMPOSITION
			Element fromPositionElement = new Element("FROMPOSITION");
			fromPositionElement.setText(sortJobProduct.getFromPosition());
			soterProductElement.addContent(fromPositionElement);

			// FROMSLOTPOSITION
			Element fromSlotPositionElement = new Element("FROMSLOTPOSITION");
			fromSlotPositionElement.setText(sortJobProduct.getFromSlotPosition());
			soterProductElement.addContent(fromSlotPositionElement);

			// TOPOSITION
			Element toPortNameElement = new Element("TOPORTNAME");
			toPortNameElement.setText(sortJobProduct.getToPortName());
			soterProductElement.addContent(toPortNameElement);

			// TOCARRIERNAME
			Element toCarrierNameElement = new Element("TOCARRIERNAME");
			toCarrierNameElement.setText(sortJobProduct.getToCarrierName());
			soterProductElement.addContent(toCarrierNameElement);

			// TOPOSITION
			Element toPositionElement = new Element("TOPOSITION");
			toPositionElement.setText(sortJobProduct.getToPosition());
			soterProductElement.addContent(toPositionElement);

			// TOSLOTPOSITION
			Element toSlotPositionElement = new Element("TOSLOTPOSITION");
			toSlotPositionElement.setText(sortJobProduct.getToSlotPosition());
			soterProductElement.addContent(toSlotPositionElement);

			// TURNFLAG
			Element turnFlagElement = new Element("TURNFLAG");
			turnFlagElement.setText(sortJobProduct.getTurnFlag());
			soterProductElement.addContent(turnFlagElement);

			// TURNDEGREE
			Element turnDegreeElement = new Element("TURNDEGREE");
			turnDegreeElement.setText(sortJobProduct.getTurnDegree());
			soterProductElement.addContent(turnDegreeElement);

			// SCRAPFLAG
			Element scrapFlagElement = new Element("SCRAPFLAG");
			scrapFlagElement.setText(sortJobProduct.getScrapFlag());
			soterProductElement.addContent(scrapFlagElement);

			sorterProductListElement.addContent(soterProductElement);
		}

		return sorterProductListElement;
	}

	public void changeSortJobState(EventInfo eventInfo, String jobName, String jobState ) throws CustomException
	{
		List<SortJob> sortJobData = ExtendedObjectProxy.getSortJobService().getSortJobList(jobName);

		if (sortJobData == null)
			throw new CustomException("JOB-9001", jobName);

		String previousJobState = ExtendedObjectProxy.getSortJobService().getPreJobState(jobState);

		// SORT-0004:Can not do process ({0}) at Sort job state.[Current:{1} ->ChangeTo: {2} ]
		if (!sortJobData.get(0).getJobState().equals(previousJobState))
			throw new CustomException("SORT-0004", "ChangeSortJobState", sortJobData.get(0).getJobState(), jobState);

		ExtendedObjectProxy.getSortJobService().updateSortJobWithCreateTime(eventInfo, jobName, jobState, sortJobData.get(0).getJobType(), "", "", "", sortJobData.get(0).getCreateTime());
	}

	public Lot skip(EventInfo eventInfo, Lot lotData, ChangeSpecInfo skipInfo, boolean forceFlag) throws CustomException
	{
		Lot preLotData = (Lot) ObjectUtil.copyTo(lotData);

		Lot movingLot = MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, skipInfo);

		movingLot = MESLotServiceProxy.getLotServiceUtil().excuteFirstGlass(eventInfo, preLotData, movingLot, null);

		movingLot = this.executePostAction(eventInfo, preLotData, movingLot, forceFlag);

		return movingLot;
	}

	public Lot executeReserveAction(EventInfo eventInfo, Lot preLotData, Lot postLotData) throws CustomException
	{
		List<SampleLot> sampleLotList = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListByToInfoForForceSampling(preLotData.getKey().getLotName(), preLotData.getFactoryName(),
				preLotData.getProductSpecName(), preLotData.getProductSpecVersion(), preLotData.getProcessFlowName(), preLotData.getProcessFlowVersion(),
				preLotData.getProcessOperationName(), preLotData.getProcessOperationVersion());
		
		SampleLot forceCheckData = new SampleLot();
		boolean forceFlag = false;
		
		if (sampleLotList != null && sampleLotList.size() > 0)
		{
			forceCheckData = sampleLotList.get(0);
		}
		
		if (forceCheckData != null && StringUtils.isNotEmpty(forceCheckData.getForceSamplingFlag()))
		{
			forceFlag = true; // ForceSampling check
		}
		
		// AR-Photo-0032-01
		// ReserveHold sends an e-mail when it arrives at the process.
		MESAlarmServiceProxy.getAlarmServiceUtil().sendMailForReserveHold(postLotData);
		
		// ReserveHold Execute - hold by beforeAction & Skip
		List<LotFutureAction> reserveData = ExtendedObjectProxy.getLotFutureActionService()
				.getLotFutureActionDataListWithoutReasonCode(postLotData.getKey().getLotName(), postLotData.getFactoryName(), postLotData.getProcessFlowName(), postLotData.getProcessFlowVersion(),
						postLotData.getProcessOperationName(), postLotData.getProcessOperationVersion(), 0);

		// ReserveHold Execute - hold by beforeAction & Skip
		if (reserveData != null)
		{
			if (reserveData.size() == 1)
			{
				String actionName = reserveData.get(0).getActionName();
				String beforeAction = reserveData.get(0).getBeforeAction();
				String afterAction = reserveData.get(0).getAfterAction();
				String permanentHold = reserveData.get(0).getAttribute3();
				String reasonCode = reserveData.get(0).getReasonCode();
				
				if (StringUtils.equals(actionName, "hold") && StringUtils.equals(beforeAction, "True"))
				{
					if(!reasonCode.equals(preLotData.getReasonCode()))
					{
						for (int i = 0; i < reserveData.size(); i++)
						{
							if (StringUtils.equals(reserveData.get(i).getBeforeActionUser(), reserveData.get(i).getAfterActionUser()) && StringUtils.equals(reserveData.get(i).getBeforeActionUser(), ""))
							{
								eventInfo = EventInfoUtil.makeEventInfo("SetFutureHold", reserveData.get(i).getLastEventUser(),"ReserveHold: "+ reserveData.get(i).getLastEventComment(), reserveData.get(i)
										.getReasonCodeType(), reserveData.get(i).getReasonCode());
							}
							else
							{
								eventInfo = EventInfoUtil.makeEventInfo("SetFutureHold", reserveData.get(i).getBeforeActionUser(),"ReserveHold: "+ reserveData.get(i).getBeforeActionComment(), reserveData.get(i)
										.getReasonCodeType(), reserveData.get(i).getReasonCode());
							}

							postLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(postLotData.getKey().getLotName());
							MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, postLotData, new HashMap<String, String>());
						}

						if(!permanentHold.equals("True"))
						{
							if (!StringUtils.equals(afterAction, "True"))
							{
								eventInfo.setEventName("Delete");

								ExtendedObjectProxy.getLotFutureActionService().deleteLotFutureActionWithoutReaconCode(eventInfo, postLotData.getKey().getLotName(), postLotData.getFactoryName(),
										postLotData.getProcessFlowName(), postLotData.getProcessFlowVersion(), postLotData.getProcessOperationName(), postLotData.getProcessOperationVersion(), 0);
							}
							else
							{
								eventInfo.setEventName("Update");

								MESLotServiceProxy.getLotServiceImpl().insertCtLotFutureMultiHoldActionForAfter(eventInfo, postLotData.getKey().getLotName(), postLotData.getFactoryName(),
										postLotData.getProcessFlowName(), postLotData.getProcessFlowVersion(), postLotData.getProcessOperationName(), postLotData.getProcessOperationVersion(),
										reserveData.get(0).getPosition().toString(), actionName, reserveData.get(0).getActionType(), reserveData.get(0).getReasonCodeType(),
										reserveData.get(0).getReasonCode(), reserveData.get(0).getAttribute1(), reserveData.get(0).getAttribute2(), reserveData.get(0).getAttribute3(), "False", afterAction,
										"", reserveData.get(0).getAfterActionComment(), "", reserveData.get(0).getAfterActionUser(), "Update", "", "");
							}
						}
					}
					
					
				}
				else if (StringUtils.equals(actionName, "skip"))
				{
					if(!forceFlag)
					{
						postLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(postLotData.getKey().getLotName());
						preLotData = (Lot) ObjectUtil.copyTo(postLotData);

						if (StringUtils.isNotEmpty(postLotData.getUdfs().get("FIRSTGLASSFLAG")) && StringUtils.isNotEmpty(postLotData.getUdfs().get("JOBNAME")))
						{
							log.info("Pass the SkipAction - FirstGlass");
						}
						else
						{
							if (StringUtils.equals(postLotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_NotOnHold))
							{
								String eventComment = reserveData.get(0).getLastEventComment();

								if (StringUtils.isNotEmpty(eventComment))
								{
									if (eventComment.substring(0, 5).equalsIgnoreCase("Split"))
									{
										eventInfo.setEventComment(eventComment);
									}
								}

								Map<String, String> udfs = new HashMap<String, String>();
								eventInfo = EventInfoUtil.makeEventInfo("Skip", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
								ChangeSpecInfo skipInfo = MESLotServiceProxy.getLotInfoUtil().skipInfo(eventInfo, postLotData, udfs, new ArrayList<ProductU>());
								postLotData = MESLotServiceProxy.getLotServiceUtil().skip(eventInfo, postLotData, skipInfo, forceFlag);
							}
							else
							{
								log.info("Pass the SkipAction - LotHoldState is Y");
							}
						}

						try
						{
							eventInfo.setEventName("Delete");

							ExtendedObjectProxy.getLotFutureActionService().deleteLotFutureActionWithoutReaconCode(eventInfo, preLotData.getKey().getLotName(), preLotData.getFactoryName(),
									preLotData.getProcessFlowName(), preLotData.getProcessFlowVersion(), preLotData.getProcessOperationName(), preLotData.getProcessOperationVersion(), 0);

							if (StringUtils.isNotEmpty(postLotData.getUdfs().get("FIRSTGLASSFLAG")) && StringUtils.isNotEmpty(postLotData.getUdfs().get("JOBNAME")))
							{
								log.info("Pass the deleteSamplingDataReturn - FirstGlass");
							}
							else
							{
								if (StringUtils.equals(postLotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_NotOnHold))
								{
									MESLotServiceProxy.getLotServiceUtil().deleteSamplingDataReturn(eventInfo, preLotData, false);
								}
								else
								{
									log.info("Pass the deleteSamplingDataReturn - LotHoldState is Y");
								}
							}
						}
						catch (Exception e)
						{
							log.info("Failed action : Delete LotFutureAction[Skip]");
						}
					}
					else
					{
						log.info("ForceSampling is not SKIP");
					}
				}
			}
			else
			{
				for (int i = 0; i < reserveData.size(); i++)
				{
					String actionName = reserveData.get(i).getActionName();
					String beforeAction = reserveData.get(i).getBeforeAction();
					String afterAction = reserveData.get(i).getAfterAction();
					String permanentHold = reserveData.get(i).getAttribute3();
					String reasonCode = reserveData.get(i).getReasonCode();
					
					if (StringUtils.equals(actionName, "hold")&&!preLotData.getReasonCode().equals(reasonCode))
					{
						List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(postLotData);

						if (StringUtils.equals(reserveData.get(i).getBeforeActionUser(), reserveData.get(i).getAfterActionUser()) && StringUtils.equals(reserveData.get(i).getBeforeActionUser(), ""))
						{
							eventInfo = EventInfoUtil.makeEventInfo("SetFutureHold", reserveData.get(i).getLastEventUser(),"ReserveHold: "+ reserveData.get(i).getLastEventComment(), reserveData.get(i)
									.getReasonCodeType(), reserveData.get(i).getReasonCode());
						}
						else
						{
							eventInfo = EventInfoUtil.makeEventInfo("SetFutureHold", reserveData.get(i).getBeforeActionUser(),"ReserveHold: "+ reserveData.get(i).getBeforeActionComment(), reserveData.get(i)
									.getReasonCodeType(), reserveData.get(i).getReasonCode());
						}
						postLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(postLotData.getKey().getLotName());

						if (StringUtils.equals(beforeAction, "True"))
						{
							MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, postLotData, new HashMap<String, String>());
						}

						if(!permanentHold.equals("True"))
						{
							// hold, skip delete all on operation
							if (!StringUtils.equals(afterAction, "True"))
							{
								eventInfo.setEventName("Delete");

								MESLotServiceProxy.getLotServiceImpl().insertCtLotFutureMultiHoldActionForAfter(eventInfo, postLotData.getKey().getLotName(), postLotData.getFactoryName(),
										postLotData.getProcessFlowName(), postLotData.getProcessFlowVersion(), postLotData.getProcessOperationName(), postLotData.getProcessOperationVersion(),
										reserveData.get(i).getPosition().toString(), actionName, reserveData.get(i).getActionType(), reserveData.get(i).getReasonCodeType(),
										reserveData.get(i).getReasonCode(), reserveData.get(i).getAttribute1(), reserveData.get(i).getAttribute2(), reserveData.get(i).getAttribute3(), beforeAction,
										afterAction, reserveData.get(i).getBeforeActionComment(), reserveData.get(i).getAfterActionComment(), reserveData.get(i).getBeforeActionUser(),
										reserveData.get(i).getAfterActionUser(), "Delete", "", "");
							}
							else
							{
								eventInfo.setEventName("Update");

								MESLotServiceProxy.getLotServiceImpl().insertCtLotFutureMultiHoldActionForAfter(eventInfo, postLotData.getKey().getLotName(), postLotData.getFactoryName(),
										postLotData.getProcessFlowName(), postLotData.getProcessFlowVersion(), postLotData.getProcessOperationName(), postLotData.getProcessOperationVersion(),
										reserveData.get(i).getPosition().toString(), actionName, reserveData.get(i).getActionType(), reserveData.get(i).getReasonCodeType(),
										reserveData.get(i).getReasonCode(), reserveData.get(i).getAttribute1(), reserveData.get(i).getAttribute2(), reserveData.get(i).getAttribute3(), "False",
										afterAction, "", reserveData.get(i).getAfterActionComment(), "", reserveData.get(i).getAfterActionUser(), "Update", "", "");
							}
						}						
					}
				}
			}
		}

		postLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(postLotData.getKey().getLotName());

		return postLotData;
	}

	public Lot executePostAction(EventInfo eventInfo, Lot preLotData, Lot postLotData, boolean forceFlag) throws CustomException
	{
		if(!forceFlag)
		{
			List<SampleLot> sampleLotList = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListByToInfoForForceSampling(preLotData.getKey().getLotName(), preLotData.getFactoryName(),
				preLotData.getProductSpecName(), preLotData.getProductSpecVersion(), preLotData.getProcessFlowName(), preLotData.getProcessFlowVersion(),
				preLotData.getProcessOperationName(), preLotData.getProcessOperationVersion());
			
			SampleLot forceCheckData = new SampleLot();

			if (sampleLotList != null && sampleLotList.size() > 0)
			{
				forceCheckData = sampleLotList.get(0);
			}
			
			if (forceCheckData != null && StringUtils.isNotEmpty(forceCheckData.getForceSamplingFlag()))
			{
				forceFlag = true; // ForceSampling check
			}
		}

		// AR-Photo-0032-01
		// ReserveHold sends an e-mail when it arrives at the process.
		MESAlarmServiceProxy.getAlarmServiceUtil().sendMailForReserveHold(postLotData);
		
		// ReserveHold Execute - hold by afterAction
		List<LotFutureAction> reserveDataByPreLot = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataListWithoutReasonCode(preLotData.getKey().getLotName(),
				preLotData.getFactoryName(), preLotData.getProcessFlowName(), preLotData.getProcessFlowVersion(), preLotData.getProcessOperationName(), preLotData.getProcessOperationVersion(), 0);

		// ReserveHold Execute - hold by beforeAction & Skip
		List<LotFutureAction> reserveData = ExtendedObjectProxy.getLotFutureActionService()
				.getLotFutureActionDataListWithoutReasonCode(postLotData.getKey().getLotName(), postLotData.getFactoryName(), postLotData.getProcessFlowName(), postLotData.getProcessFlowVersion(),
						postLotData.getProcessOperationName(), postLotData.getProcessOperationVersion(), 0);

		// ReserveHold Execute - hold by afterAction
		if (reserveDataByPreLot != null)
		{
			if (reserveDataByPreLot.size() == 1)
			{
				String actionName = reserveDataByPreLot.get(0).getActionName();
				String beforeAction = reserveDataByPreLot.get(0).getBeforeAction();
				String afterAction = reserveDataByPreLot.get(0).getAfterAction();
				String reasonCode = reserveDataByPreLot.get(0).getReasonCode();
				String permanentHold = reserveDataByPreLot.get(0).getAttribute3();
				
				if (StringUtils.equals(actionName, "hold") && StringUtils.equals(afterAction, "True"))
				{
					for (int i = 0; i < reserveDataByPreLot.size(); i++)
					{
						if (StringUtils.equals(reserveDataByPreLot.get(i).getBeforeActionUser(), reserveDataByPreLot.get(i).getAfterActionUser())
								&& StringUtils.equals(reserveDataByPreLot.get(i).getAfterActionUser(), ""))
						{
							eventInfo = EventInfoUtil.makeEventInfo("SetFutureHold", reserveDataByPreLot.get(i).getLastEventUser(), "ReserveHold: "+reserveDataByPreLot.get(i).getLastEventComment(),
									reserveDataByPreLot.get(i).getReasonCodeType(), reserveDataByPreLot.get(i).getReasonCode());
						}
						else
						{
							eventInfo = EventInfoUtil.makeEventInfo("SetFutureHold", reserveDataByPreLot.get(i).getAfterActionUser(), "ReserveHold: "+reserveDataByPreLot.get(i).getAfterActionComment(),
									reserveDataByPreLot.get(i).getReasonCodeType(), reserveDataByPreLot.get(i).getReasonCode());
						}

						Map<String, String> udfs = new HashMap<String, String>();
						if (StringUtils.equals(reasonCode, "SPC"))
						{
							udfs.put("ACTIONTYPE", "SPC");
							udfs.put("ALARMCODE", reserveDataByPreLot.get(0).getAlarmCode());
						}
						
						Lot tempLotData = (Lot) ObjectUtil.copyTo(postLotData);
						tempLotData.setProcessOperationName(preLotData.getProcessOperationName());
						MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, tempLotData, udfs);
					}

					if(!StringUtils.equals(permanentHold, "True"))
					{
						if (!StringUtils.equals(beforeAction, "True"))
						{
							eventInfo.setEventName("Delete");
							ExtendedObjectProxy.getLotFutureActionService().deleteLotFutureActionWithoutReaconCode(eventInfo, preLotData.getKey().getLotName(), preLotData.getFactoryName(),
									preLotData.getProcessFlowName(), preLotData.getProcessFlowVersion(), preLotData.getProcessOperationName(), preLotData.getProcessOperationVersion(), 0);
						}
						else
						{
							eventInfo.setEventName("Update");

							MESLotServiceProxy.getLotServiceImpl().insertCtLotFutureMultiHoldActionForAfter(eventInfo, preLotData.getKey().getLotName(), preLotData.getFactoryName(),
									preLotData.getProcessFlowName(), preLotData.getProcessFlowVersion(), preLotData.getProcessOperationName(), preLotData.getProcessOperationVersion(),
									reserveDataByPreLot.get(0).getPosition().toString(), actionName, reserveDataByPreLot.get(0).getActionType(), reserveDataByPreLot.get(0).getReasonCodeType(),
									reserveDataByPreLot.get(0).getReasonCode(), reserveDataByPreLot.get(0).getAttribute1(), reserveDataByPreLot.get(0).getAttribute2(),
									reserveDataByPreLot.get(0).getAttribute3(), beforeAction, "False", reserveDataByPreLot.get(0).getBeforeActionComment(), "",
									reserveDataByPreLot.get(0).getBeforeActionUser(), "", "Update", reserveDataByPreLot.get(0).getBeforeMailFlag(), "");
						}
					}
					
				}
			}
			else
			{
				for (int i = 0; i < reserveDataByPreLot.size(); i++)
				{
					String actionName = reserveDataByPreLot.get(i).getActionName();
					String beforeAction = reserveDataByPreLot.get(i).getBeforeAction();
					String afterAction = reserveDataByPreLot.get(i).getAfterAction();
					String reasonCode = reserveDataByPreLot.get(i).getReasonCode();
					String permanentHold = reserveDataByPreLot.get(i).getAttribute3();
					
					if (StringUtils.equals(actionName, "hold"))
					{
						if (StringUtils.equals(reserveDataByPreLot.get(i).getBeforeActionUser(), reserveDataByPreLot.get(i).getAfterActionUser())
								&& StringUtils.equals(reserveDataByPreLot.get(i).getAfterActionUser(), ""))
						{
							eventInfo = EventInfoUtil.makeEventInfo("SetFutureHold", reserveDataByPreLot.get(i).getLastEventUser(),"ReserveHold: "+ reserveDataByPreLot.get(i).getLastEventComment(),
									reserveDataByPreLot.get(i).getReasonCodeType(), reserveDataByPreLot.get(i).getReasonCode());
						}
						else
						{
							eventInfo = EventInfoUtil.makeEventInfo("SetFutureHold", reserveDataByPreLot.get(i).getAfterActionUser(),"ReserveHold: "+ reserveDataByPreLot.get(i).getAfterActionComment(),
									reserveDataByPreLot.get(i).getReasonCodeType(), reserveDataByPreLot.get(i).getReasonCode());

							postLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(postLotData.getKey().getLotName());
						}

						if (StringUtils.equals(afterAction, "True"))
						{
							Map<String, String> udfs = new HashMap<String, String>();
							if (StringUtils.equals(reasonCode, "SPC"))
							{
								udfs.put("ACTIONTYPE", "SPC");
								udfs.put("ALARMCODE", reserveDataByPreLot.get(i).getAlarmCode());
							}

							Lot tempLotData = (Lot) ObjectUtil.copyTo(postLotData);
							tempLotData.setProcessOperationName(preLotData.getProcessOperationName());
							MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, tempLotData, udfs);
						}
						//永久Hold
						if(!StringUtils.equals(permanentHold, "True"))
						{
							// hold, skip delete all on operation
							if (!StringUtils.equals(beforeAction, "True"))
							{
								eventInfo.setEventName("Delete");

								MESLotServiceProxy.getLotServiceImpl().insertCtLotFutureMultiHoldActionForAfter(eventInfo, preLotData.getKey().getLotName(), preLotData.getFactoryName(),
										preLotData.getProcessFlowName(), preLotData.getProcessFlowVersion(), preLotData.getProcessOperationName(), preLotData.getProcessOperationVersion(),
										reserveDataByPreLot.get(i).getPosition().toString(), actionName, reserveDataByPreLot.get(i).getActionType(), reserveDataByPreLot.get(i).getReasonCodeType(),
										reserveDataByPreLot.get(i).getReasonCode(), reserveDataByPreLot.get(i).getAttribute1(), reserveDataByPreLot.get(i).getAttribute2(),
										reserveDataByPreLot.get(i).getAttribute3(), beforeAction, afterAction, "", reserveDataByPreLot.get(i).getAfterActionComment(), "",
										reserveDataByPreLot.get(i).getAfterActionUser(), "Delete", "", "");
							}
							else
							{
								eventInfo.setEventName("Update");

								MESLotServiceProxy.getLotServiceImpl().insertCtLotFutureMultiHoldActionForAfter(eventInfo, preLotData.getKey().getLotName(), preLotData.getFactoryName(),
										preLotData.getProcessFlowName(), preLotData.getProcessFlowVersion(), preLotData.getProcessOperationName(), preLotData.getProcessOperationVersion(),
										reserveDataByPreLot.get(i).getPosition().toString(), actionName, reserveDataByPreLot.get(i).getActionType(), reserveDataByPreLot.get(i).getReasonCodeType(),
										reserveDataByPreLot.get(i).getReasonCode(), reserveDataByPreLot.get(i).getAttribute1(), reserveDataByPreLot.get(i).getAttribute2(),
										reserveDataByPreLot.get(i).getAttribute3(), beforeAction, "False", reserveDataByPreLot.get(i).getBeforeActionComment(), "",
										reserveDataByPreLot.get(i).getBeforeActionUser(), "", "Update", reserveDataByPreLot.get(0).getBeforeMailFlag(), "");
							}
						}
						
					}
				}
			}
		}

		postLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(postLotData.getKey().getLotName());
		
		// ReserveHold Execute - hold by beforeAction & Skip
		if (reserveData != null/* && StringUtils.equals(postLotData.getLotHoldState(), "N")*/)
		{
			if (reserveData.size() == 1)
			{
				String actionName = reserveData.get(0).getActionName();
				String beforeAction = reserveData.get(0).getBeforeAction();
				String afterAction = reserveData.get(0).getAfterAction();
				String permanentHold = reserveData.get(0).getAttribute3();

				if (StringUtils.equals(actionName, "hold") && StringUtils.equals(beforeAction, "True"))
				{
					for (int i = 0; i < reserveData.size(); i++)
					{
						if (StringUtils.equals(reserveData.get(i).getBeforeActionUser(), reserveData.get(i).getAfterActionUser()) && StringUtils.equals(reserveData.get(i).getBeforeActionUser(), ""))
						{
							eventInfo = EventInfoUtil.makeEventInfo("SetFutureHold", reserveData.get(i).getLastEventUser(),"ReserveHold: "+ reserveData.get(i).getLastEventComment(), reserveData.get(i)
									.getReasonCodeType(), reserveData.get(i).getReasonCode());
						}
						else
						{
							eventInfo = EventInfoUtil.makeEventInfo("SetFutureHold", reserveData.get(i).getBeforeActionUser(),"ReserveHold: "+ reserveData.get(i).getBeforeActionComment(), reserveData.get(i)
									.getReasonCodeType(), reserveData.get(i).getReasonCode());
						}

						postLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(postLotData.getKey().getLotName());
						MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, postLotData, new HashMap<String, String>());
					}
					
					//永久Hold
					if(!StringUtils.equals(permanentHold, "True"))
					{
						if (!StringUtils.equals(afterAction, "True"))
						{
							eventInfo.setEventName("Delete");

							ExtendedObjectProxy.getLotFutureActionService().deleteLotFutureActionWithoutReaconCode(eventInfo, postLotData.getKey().getLotName(), postLotData.getFactoryName(),
									postLotData.getProcessFlowName(), postLotData.getProcessFlowVersion(), postLotData.getProcessOperationName(), postLotData.getProcessOperationVersion(), 0);
						}
						else
						{
							eventInfo.setEventName("Update");

							MESLotServiceProxy.getLotServiceImpl().insertCtLotFutureMultiHoldActionForAfter(eventInfo, postLotData.getKey().getLotName(), postLotData.getFactoryName(),
									postLotData.getProcessFlowName(), postLotData.getProcessFlowVersion(), postLotData.getProcessOperationName(), postLotData.getProcessOperationVersion(),
									reserveData.get(0).getPosition().toString(), actionName, reserveData.get(0).getActionType(), reserveData.get(0).getReasonCodeType(),
									reserveData.get(0).getReasonCode(), reserveData.get(0).getAttribute1(), reserveData.get(0).getAttribute2(), reserveData.get(0).getAttribute3(), "False", afterAction,
									"", reserveData.get(0).getAfterActionComment(), "", reserveData.get(0).getAfterActionUser(), "Update", "", reserveData.get(0).getAfterMailFlag());
						}
					}

					
				}
				else if (StringUtils.equals(actionName, "skip") && StringUtils.equals(postLotData.getLotHoldState(), "N"))
				{
					if(!forceFlag)
					{
						postLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(postLotData.getKey().getLotName());
						preLotData = (Lot) ObjectUtil.copyTo(postLotData);

						if (StringUtils.isNotEmpty(postLotData.getUdfs().get("FIRSTGLASSFLAG")) && StringUtils.isNotEmpty(postLotData.getUdfs().get("JOBNAME")))
						{
							log.info("Pass the SkipAction - FirstGlass");
						}
						else
						{
							if (StringUtils.equals(postLotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_NotOnHold))
							{
								String eventComment = reserveData.get(0).getLastEventComment();
								Lot checkLotData = postLotData;
								
								Map<String, String> udfs = new HashMap<String, String>();
								eventInfo = EventInfoUtil.makeEventInfo("Skip", eventInfo.getEventUser(), eventComment, null, null);
								ChangeSpecInfo skipInfo = MESLotServiceProxy.getLotInfoUtil().skipInfo(eventInfo, postLotData, udfs, new ArrayList<ProductU>());
								postLotData = MESLotServiceProxy.getLotServiceUtil().skip(eventInfo, postLotData, skipInfo, false);
								
								//AR-Photo-0030-01 : After Main Operation skip action.  
								if(StringUtils.equals(postLotData.getLastEventName(), "Skip"))
								{
									ProcessFlow checkFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(checkLotData);
									
									if(StringUtils.equals(checkFlowData.getProcessFlowType(), "Main"))
									{
										eventInfo.setEventName("Hold");
										eventInfo.setReasonCode("SYSTEM");
										eventInfo.setEventComment("Main Operation(" +checkLotData.getProcessOperationName() + ") Skip. ");
										
										MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, postLotData, postLotData.getUdfs());
										postLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(postLotData.getKey().getLotName());
									}
								}
								
							}
							else
							{
								log.info("Pass the SkipAction - LotHoldState is Y");
							}
						}

						try
						{
							eventInfo.setEventName("Delete");

							ExtendedObjectProxy.getLotFutureActionService().deleteLotFutureActionWithoutReaconCode(eventInfo, preLotData.getKey().getLotName(), preLotData.getFactoryName(),
									preLotData.getProcessFlowName(), preLotData.getProcessFlowVersion(), preLotData.getProcessOperationName(), preLotData.getProcessOperationVersion(), 0);

							if (StringUtils.isNotEmpty(postLotData.getUdfs().get("FIRSTGLASSFLAG")) && StringUtils.isNotEmpty(postLotData.getUdfs().get("JOBNAME")))
							{
								log.info("Pass the deleteSamplingDataReturn - FirstGlass");
							}
							else
							{
								if (StringUtils.equals(postLotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_NotOnHold))
								{
									MESLotServiceProxy.getLotServiceUtil().deleteSamplingDataReturn(eventInfo, preLotData, false);
								}
								else
								{
									log.info("Pass the deleteSamplingDataReturn - LotHoldState is Y");
								}
							}
						}
						catch (Exception e)
						{
							log.info("Failed action : Delete LotFutureAction[Skip]");
						}
					}
					else
					{
						log.info("ForceSampling is not SKIP");
					}
				}
			}
			else
			{
				for (int i = 0; i < reserveData.size(); i++)
				{
					String actionName = reserveData.get(i).getActionName();
					String beforeAction = reserveData.get(i).getBeforeAction();
					String afterAction = reserveData.get(i).getAfterAction();
					String permanentHold = reserveData.get(i).getAttribute3();


					if (StringUtils.equals(actionName, "hold"))
					{
						if (StringUtils.equals(reserveData.get(i).getBeforeActionUser(), reserveData.get(i).getAfterActionUser()) && StringUtils.equals(reserveData.get(i).getBeforeActionUser(), ""))
						{
							eventInfo = EventInfoUtil.makeEventInfo("SetFutureHold", reserveData.get(i).getLastEventUser(), "ReserveHold: "+reserveData.get(i).getLastEventComment(), reserveData.get(i)
									.getReasonCodeType(), reserveData.get(i).getReasonCode());
						}
						else
						{
							eventInfo = EventInfoUtil.makeEventInfo("SetFutureHold", reserveData.get(i).getBeforeActionUser(),"ReserveHold: "+reserveData.get(i).getBeforeActionComment(), reserveData.get(i)
									.getReasonCodeType(), reserveData.get(i).getReasonCode());
						}

						postLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(postLotData.getKey().getLotName());

						if (StringUtils.equals(beforeAction, "True"))
						{
							MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, postLotData, new HashMap<String, String>());
						}
						
						//永久Hold
						if(!StringUtils.equals(permanentHold, "True"))
						{
							// hold, skip delete all on operation
							if (!StringUtils.equals(afterAction, "True"))
							{
								eventInfo.setEventName("Delete");
								MESLotServiceProxy.getLotServiceImpl().insertCtLotFutureMultiHoldActionForAfter(eventInfo, postLotData.getKey().getLotName(), postLotData.getFactoryName(),
										postLotData.getProcessFlowName(), postLotData.getProcessFlowVersion(), postLotData.getProcessOperationName(), postLotData.getProcessOperationVersion(),
										reserveData.get(i).getPosition().toString(), actionName, reserveData.get(i).getActionType(), reserveData.get(i).getReasonCodeType(),
										reserveData.get(i).getReasonCode(), reserveData.get(i).getAttribute1(), reserveData.get(i).getAttribute2(), reserveData.get(i).getAttribute3(), beforeAction,
										afterAction, reserveData.get(i).getBeforeActionComment(), reserveData.get(i).getAfterActionComment(), reserveData.get(i).getBeforeActionUser(),
										reserveData.get(i).getAfterActionUser(), "Delete", "", reserveData.get(0).getAfterMailFlag());

							}
							else
							{
								eventInfo.setEventName("Update");

								MESLotServiceProxy.getLotServiceImpl().insertCtLotFutureMultiHoldActionForAfter(eventInfo, postLotData.getKey().getLotName(), postLotData.getFactoryName(),
										postLotData.getProcessFlowName(), postLotData.getProcessFlowVersion(), postLotData.getProcessOperationName(), postLotData.getProcessOperationVersion(),
										reserveData.get(i).getPosition().toString(), actionName, reserveData.get(i).getActionType(), reserveData.get(i).getReasonCodeType(),
										reserveData.get(i).getReasonCode(), reserveData.get(i).getAttribute1(), reserveData.get(i).getAttribute2(), reserveData.get(i).getAttribute3(), "False",
										afterAction, "", reserveData.get(i).getAfterActionComment(), "", reserveData.get(i).getAfterActionUser(), "Update", "", reserveData.get(0).getAfterMailFlag());
							}
						}	
					}
				}
			}
		}

		// ReserveProductSpecHold
		{
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT R.FACTORYNAME, ");
			sql.append("       R.PRODUCTSPECNAME, ");
			sql.append("       R.PROCESSFLOWNAME, ");
			sql.append("       R.PROCESSOPERATIONNAME, ");
			sql.append("       R.EVENTCOMMENT, ");
			sql.append("       R.LASTEVENTUSER, ");
			sql.append("       R.LASTEVENTTIME, ");
			sql.append("       R.REASONCODE, ");
			sql.append("       R.REASONCODETYPE, ");
			sql.append("       L.PRIORITY, ");
			sql.append("       L.LOTNAME ");
			sql.append("  FROM CT_RESERVEPRODUCTSPEC R, LOT L, PRODUCTREQUEST W ");
			sql.append(" WHERE 1 = 1 ");
			sql.append("   AND L.FACTORYNAME = R.FACTORYNAME ");
			sql.append("   AND L.PRODUCTSPECNAME = R.PRODUCTSPECNAME ");
			sql.append("   AND L.PROCESSFLOWNAME = R.PROCESSFLOWNAME ");
			sql.append("   AND L.PROCESSOPERATIONNAME = R.PROCESSOPERATIONNAME ");
			sql.append("   AND L.PRIORITY = R.LOTPRIORITY ");
			sql.append("   AND LOTNAME = :LOTNAME ");
			sql.append("   AND L.PRODUCTREQUESTNAME = W.PRODUCTREQUESTNAME ");

			Map<String, Object> bindMapA = new HashMap<String, Object>();
			bindMapA.put("LOTNAME", postLotData.getKey().getLotName());

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMapA);
			if (sqlResult.size() > 0)
			{
				postLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(postLotData.getKey().getLotName());
				// List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(postLotData);

				eventInfo = EventInfoUtil.makeEventInfo("SetFutureHold", sqlResult.get(0).get("LASTEVENTUSER").toString(),"ReserveHold: "+ sqlResult.get(0).get("EVENTCOMMENT").toString(),
						sqlResult.get(0).get("REASONCODETYPE").toString(), sqlResult.get(0).get("REASONCODE").toString());

				// MakeOnHoldInfo makeOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeOnHoldInfo(productUSequence, postLotData.getUdfs());
				MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, postLotData, new HashMap<String, String>());
			}

		}

		postLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(postLotData.getKey().getLotName());

		return postLotData;
	}
	
	public Lot recoverLotGrade(EventInfo eventInfo, Lot lotData, String fromGrade, String toGrade) throws CustomException
	{
		List<Product> productList = ProductServiceProxy.getProductService().select("lotName = ? AND productState = ? AND productGrade = ?",
				new Object[] { lotData.getKey().getLotName(), GenericServiceProxy.getConstantMap().Prod_InProduction, fromGrade });

		List<ProductPGS> productPGSSequence = MESLotServiceProxy.getLotInfoUtil().getProductPGSSequence(lotData.getKey().getLotName(), toGrade, productList, new HashMap<String, String>());

		ChangeGradeInfo changeGradeInfo = MESLotServiceProxy.getLotInfoUtil().changeGradeInfo(lotData, toGrade, productPGSSequence);

		lotData = MESLotServiceProxy.getLotServiceImpl().ChangeGrade(eventInfo, lotData, changeGradeInfo);

		return lotData;
	}

	public Lot recoverLotGradeForPL(EventInfo eventInfo, Lot lotData, String toGrade) throws CustomException
	{
		List<Product> productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());

		List<ProductPGS> productPGSSequence = MESLotServiceProxy.getLotInfoUtil().getProductPGSSequence(lotData.getKey().getLotName(), toGrade, productList, new HashMap<String, String>());

		ChangeGradeInfo changeGradeInfo = MESLotServiceProxy.getLotInfoUtil().changeGradeInfo(lotData, toGrade, productPGSSequence);

		lotData = MESLotServiceProxy.getLotServiceImpl().ChangeGrade(eventInfo, lotData, changeGradeInfo);

		return lotData;
	}

	public void transferProductsToRunSheet(EventInfo eventInfo, Lot newLotData, String srcLotName, String sProductQuantity, Map<String, String> deassignCarrierUdfs, List<Element> productList)
			throws CustomException
	{
		eventInfo = EventInfoUtil.makeEventInfo("TransferProduct", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);

		double dProductQuantity = Double.parseDouble(sProductQuantity);
		Lot srcLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(srcLotName);

		List<ProductP> productPSequence = MESLotServiceProxy.getLotInfoUtil().setProductPSequence(productList, srcLotName);

		MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, srcLotData, newLotData.getKey().getLotName(), dProductQuantity, productPSequence, "N", newLotData.getUdfs(),
				deassignCarrierUdfs, srcLotData.getUdfs());

		srcLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(srcLotName);

		if (srcLotData.getProductQuantity() == 0)
		{
			if (StringUtils.isNotEmpty(srcLotData.getCarrierName()))
			{
				Durable sLotDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(srcLotData.getCarrierName());
				deassignCarrierUdfs = sLotDurableData.getUdfs();
			}

			MESLotServiceProxy.getLotServiceImpl().MakeEmptied(eventInfo, srcLotData, new ArrayList<ProductU>(), deassignCarrierUdfs);
			deassignCarrierUdfs.clear();
		}
	}

	public void deleteSortJob(EventInfo eventInfo, String jobName) throws CustomException
	{
		SortJob sortJobData = ExtendedObjectProxy.getSortJobService().getSortJobData(jobName);

		if (sortJobData == null)
			throw new CustomException("JOB-9001", jobName);


		if (!StringUtils.equals(sortJobData.getJobState(), "RESERVED"))
			throw new CustomException("LOT-0110", jobName);
		
		ExtendedObjectProxy.getSortJobService().deleteSortJob(eventInfo, jobName);
	}

	public void deleteSortJobProduct(EventInfo eventInfo, String jobName, List<String> productNameList) throws CustomException
	{
		List<SortJobProduct> sortJobProductData = ExtendedObjectProxy.getSortJobProductService().getSortJobProductByList(jobName, productNameList);

		if (sortJobProductData == null)
		{
			throw new CustomException("JOB-9001", jobName);
		}

		ExtendedObjectProxy.getSortJobProductService().deleteSortJobProductByList(eventInfo, jobName, productNameList);
	}

	public boolean isExistAbortedProduct(List<Element> productList) throws CustomException
	{
		boolean isExistAbortedProduct = false;

		for (Element productE : productList)
		{
			String processingInfo = productE.getChildText("PROCESSINGINFO");

			if (StringUtils.equals(processingInfo, "B"))
			{
				isExistAbortedProduct = true;
				return isExistAbortedProduct;
			}
		}

		return isExistAbortedProduct;
	}

	public void validationLotGrade(Lot lotData) throws CustomException
	{
		ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);

		if (flowData.getProcessFlowType().equals(GenericServiceProxy.getConstantMap().ProcessFlowType_Main)
				&& !StringUtils.equals(lotData.getLotGrade(), GenericServiceProxy.getConstantMap().LotGrade_G)
				&& !(StringUtils.equals(lotData.getLotGrade(), GenericServiceProxy.getConstantMap().LotGrade_P) && StringUtils.equals(lotData.getProcessOperationName(),"21400")))//应整合夏宏志需求21400站点不卡控P等级
		{
			throw new CustomException("LOT-0070", lotData.getKey().getLotName());
		}
	}

	public void RequestTransportJob(String CSTName, String sourceMachineName, String sourcePositionType, String sourcePositionName, String sourceZoneName, String destinationMachineName,
			String destinationPositionType, String destinationPositionName, String destinationZoneName, String lotName, String productQuantity, String carrierState, String priority, String eventUser,
			String eventComment, String originalSourceSubjectName) throws CustomException
	{
		Element eleBody = new Element(SMessageUtil.Body_Tag);

		Element eleTransportJobName = new Element("TRANSPORTJOBNAME");
		eleBody.addContent(eleTransportJobName);

		Element eleCarrierName = new Element("CARRIERNAME");
		eleCarrierName.setText(CSTName);
		eleBody.addContent(eleCarrierName);

		Element eleSourceMachineName = new Element("SOURCEMACHINENAME");
		eleSourceMachineName.setText(sourceMachineName);
		eleBody.addContent(eleSourceMachineName);

		Element eleSourceZoneName = new Element("SOURCEZONENAME");
		eleSourceZoneName.setText(sourceZoneName);
		eleBody.addContent(eleSourceZoneName);

		Element eleSourcePositionType = new Element("SOURCEPOSITIONTYPE");
		eleSourcePositionType.setText(sourcePositionType);
		eleBody.addContent(eleSourcePositionType);

		Element eleSourcePositionName = new Element("SOURCEPOSITIONNAME");
		eleSourcePositionName.setText(sourcePositionName);
		eleBody.addContent(eleSourcePositionName);

		Element eleDestinationMachineName = new Element("DESTINATIONMACHINENAME");
		eleDestinationMachineName.setText(destinationMachineName);
		eleBody.addContent(eleDestinationMachineName);

		Element eleDestinationZoneName = new Element("DESTINATIONZONENAME");
		eleDestinationZoneName.setText(destinationZoneName);
		eleBody.addContent(eleDestinationZoneName);

		Element eleDestinationPositionType = new Element("DESTINATIONPOSITIONTYPE");
		eleDestinationPositionType.setText(destinationPositionType);
		eleBody.addContent(eleDestinationPositionType);

		Element eleDestinationPositionName = new Element("DESTINATIONPOSITIONNAME");
		eleDestinationPositionName.setText(destinationPositionName);
		eleBody.addContent(eleDestinationPositionName);

		Element eleLotName = new Element("LOTNAME");
		eleLotName.setText(lotName);
		eleBody.addContent(eleLotName);

		Element eleProductQuantity = new Element("PRODUCTQUANTITY");
		eleProductQuantity.setText(productQuantity);
		eleBody.addContent(eleProductQuantity);

		Element eleCarrierState = new Element("CARRIERSTATE");
		eleCarrierState.setText(carrierState);
		eleBody.addContent(eleCarrierState);

		Element elePriority = new Element("PRIORITY");
		elePriority.setText(priority);
		eleBody.addContent(elePriority);

		try
		{
			String targetSubject = GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("TEMsvr");
			Document requestDoc = SMessageUtil.createXmlDocument(eleBody, "RequestTransportJobRequest", originalSourceSubjectName, targetSubject, eventUser, eventComment);

			log.info("Start " + eventComment);
			GenericServiceProxy.getESBServive().sendBySender(targetSubject, requestDoc, "TEMSender");
		}
		catch (Exception ex)
		{
			log.error(ex);
		}
	}

	public List<Map<String, Object>> getUnScrapProductData(String productName) throws CustomException
	{
		List<Map<String, Object>> sqlLotList = null;
		try
		{
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT PRODUCTNAME, PRODUCTTYPE, PRODUCTSPECNAME, PRODUCTIONTYPE ");
			sql.append("  FROM PRODUCT ");
			sql.append(" WHERE PRODUCTNAME = :PRODUCTNAME ");
			sql.append("   AND PRODUCTSTATE = 'InProduction' ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("PRODUCTNAME", productName);

			sqlLotList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
		}
		catch (Exception err)
		{

		}
		return sqlLotList;
	}

	public List<Map<String, Object>> getSrcLotListByProductList(List<Element> productList) throws CustomException
	{
		String bindProductList = CommonUtil.makeListForQuery(productList, "PRODUCTNAME");
		String sql = "SELECT LOTNAME, COUNT(PRODUCTNAME) AS PRODUCTQTY " + " FROM PRODUCT WHERE PRODUCTNAME IN (" + bindProductList + ") GROUP BY LOTNAME" + " ORDER BY PRODUCTQTY DESC";

		Map<String, Object> bindMap = new HashMap<String, Object>();

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlLotList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		return sqlLotList;
	}

	public void updateBatch(String queryString, List<Object[]> updateArgList) throws CustomException
	{
		// Update Batch
		if (updateArgList.size() > 0)
		{
			try
			{
				if (updateArgList.size() == 1)
				{
					GenericServiceProxy.getSqlMesTemplate().update(queryString, updateArgList.get(0));
				}
				else
				{
					GenericServiceProxy.getSqlMesTemplate().updateBatch(queryString, updateArgList);
				}
			}
			catch (Exception e)
			{
				throw new CustomException();
			}
		}
	}

	public void insertBatchProductHistory(EventInfo eventInfo, List<Product> productList, List<Product> oldProductList) throws CustomException
	{
		// History insert Query & insertArgList
		String queryStringHistory = "INSERT INTO PRODUCTHISTORY (PRODUCTNAME,TIMEKEY,EVENTTIME,EVENTNAME,PRODUCTIONTYPE,PRODUCTSPECNAME,PRODUCTSPECVERSION,PROCESSGROUPNAME,PRODUCTREQUESTNAME,ORIGINALPRODUCTNAME,SOURCEPRODUCTNAME,LOTNAME,POSITION,PRODUCTTYPE,SUBPRODUCTTYPE,SUBPRODUCTUNITQUANTITY1,SUBPRODUCTUNITQUANTITY2,SUBPRODUCTQUANTITY,SUBPRODUCTQUANTITY1,SUBPRODUCTQUANTITY2,PRODUCTGRADE,SUBPRODUCTGRADES1,DUEDATE,PRIORITY,FACTORYNAME,AREANAME,PRODUCTSTATE,PRODUCTPROCESSSTATE,PRODUCTHOLDSTATE,EVENTUSER,EVENTCOMMENT,EVENTFLAG,LASTIDLETIME,LASTIDLEUSER,REASONCODE,REASONCODETYPE,PROCESSFLOWNAME,PROCESSFLOWVERSION,PROCESSOPERATIONNAME,PROCESSOPERATIONVERSION,NODESTACK,REWORKSTATE,REWORKCOUNT,CONSUMEDPRODUCTNAME,SYSTEMTIME,CANCELFLAG,VCRPRODUCTNAME,OLDLOTNAME,OLDPROCESSOPERATIONNAME,DESTINATIONFACTORYNAME,OLDSUBPRODUCTQUANTITY,OLDSUBPRODUCTQUANTITY1,OLDSUBPRODUCTQUANTITY2,DESTINATIONPRODUCTNAME,OLDPRODUCTIONTYPE,OLDPRODUCTSPECNAME,OLDPRODUCTSPECVERSION,OLDPRODUCTTYPE,OLDSUBPRODUCTTYPE,OLDDESTINATIONFACTORYNAME,OLDAREANAME,OLDPROCESSFLOWNAME,OLDPROCESSFLOWVERSION,CRATENAME,PAIRPRODUCTNAME,PROCESSINGINFO,DUMMYUSEDCOUNT,EXPOSURERECIPENAME,REASONCODEDEPARTMENT,SLOTPOSITION,LASTPROCESSINGTIME,LASTPROCESSINGUSER,MACHINENAME,MATERIALLOCATIONNAME,REWORKNODEID,CARRIERNAME,TRANSPORTGROUPNAME,OLDPROCESSOPERATIONVERSION,OLDFACTORYNAME,OLDPRODUCTREQUESTNAME,BEFOREPROCESSMACHINE,BEFOREPROCESSOPERATION, ARRAYLOTNAME) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		List<Object[]> insertArgListHistory = new ArrayList<Object[]>();

		for (Product productData : productList)
		{
			EventInfo newEventInfo = EventInfoUtil.makeEventInfo(eventInfo.getEventName(), eventInfo.getEventUser(), eventInfo.getEventComment(), eventInfo.getReasonCodeType(),
					eventInfo.getReasonCode());
			newEventInfo.setEventTimeKey(eventInfo.getEventTimeKey());
			newEventInfo.setEventTime(eventInfo.getEventTime());

			// Get OldProductData
			Product oldProduct = new Product();
			if (oldProductList.size() > 0)
			{
				for (Product oldProductData : oldProductList)
				{
					if (productData.getKey().getProductName().equals(oldProductData.getKey().getProductName()))
					{
						oldProduct = oldProductData;
					}
				}
			}

			// insert History
			List<Object> bindList = new ArrayList<Object>();
			bindList.add(productData.getKey().getProductName());
			bindList.add(newEventInfo.getEventTimeKey());
			bindList.add(newEventInfo.getEventTime());
			bindList.add(newEventInfo.getEventName());
			bindList.add(productData.getProductionType());
			bindList.add(productData.getProductSpecName());
			bindList.add(productData.getProductSpecVersion());
			bindList.add(productData.getProcessGroupName());
			bindList.add(productData.getProductRequestName());
			bindList.add(productData.getOriginalProductName());
			bindList.add(productData.getSourceProductName());
			bindList.add(productData.getLotName());
			bindList.add(productData.getPosition());
			bindList.add(productData.getProductType());
			bindList.add(productData.getSubProductType());
			bindList.add(productData.getSubProductUnitQuantity1());
			bindList.add(productData.getSubProductUnitQuantity2());
			bindList.add(productData.getSubProductQuantity());
			bindList.add(productData.getSubProductQuantity1());
			bindList.add(productData.getSubProductQuantity2());
			bindList.add(productData.getProductGrade());
			bindList.add(productData.getSubProductGrades1());
			bindList.add(productData.getDueDate());
			bindList.add(productData.getPriority());
			bindList.add(productData.getFactoryName());
			bindList.add(productData.getAreaName());
			bindList.add(productData.getProductState());
			bindList.add(productData.getProductProcessState());
			bindList.add(productData.getProductHoldState());
			bindList.add(newEventInfo.getEventUser());
			bindList.add(newEventInfo.getEventComment());
			bindList.add("N");
			bindList.add(newEventInfo.getEventTime());
			bindList.add(newEventInfo.getEventUser());
			if (StringUtils.isNotEmpty(newEventInfo.getReasonCode()))
				bindList.add(newEventInfo.getReasonCode());
			else
				bindList.add(productData.getReasonCode());
			if (StringUtils.isNotEmpty(newEventInfo.getReasonCodeType()))
				bindList.add(newEventInfo.getReasonCodeType());
			else
				bindList.add(productData.getReasonCodeType());
			bindList.add(productData.getProcessFlowName());
			bindList.add(productData.getProcessFlowVersion());
			bindList.add(productData.getProcessOperationName());
			bindList.add(productData.getProcessOperationVersion());
			bindList.add(productData.getNodeStack());
			bindList.add(productData.getReworkState());
			bindList.add(productData.getReworkCount());
			bindList.add(productData.getReworkNodeId());
			bindList.add(newEventInfo.getEventTime());
			bindList.add(productData.getTransportGroupName());
			bindList.add(productData.getUdfs().get("VCRPRODUCTNAME"));
			bindList.add(oldProduct.getLotName());
			bindList.add(oldProduct.getProcessOperationName());
			bindList.add(productData.getDestinationFactoryName());
			bindList.add(oldProduct.getSubProductQuantity());
			bindList.add(oldProduct.getSubProductQuantity1());
			bindList.add(oldProduct.getSubProductQuantity2());
			bindList.add(productData.getDestinationProductName());
			bindList.add(oldProduct.getProductionType());
			bindList.add(oldProduct.getProductSpecName());
			bindList.add(oldProduct.getProductSpecVersion());
			bindList.add(oldProduct.getProductType());
			bindList.add(oldProduct.getSubProductType());
			bindList.add(oldProduct.getDestinationFactoryName());
			bindList.add(oldProduct.getAreaName());
			bindList.add(oldProduct.getProcessFlowName());
			bindList.add(oldProduct.getProcessFlowVersion());
			bindList.add(productData.getUdfs().get("CRATENAME"));
			bindList.add(productData.getUdfs().get("PAIRPRODUCTNAME"));
			bindList.add(productData.getUdfs().get("PROCESSINGINFO"));
			bindList.add(productData.getUdfs().get("DUMMYUSEDCOUNT"));
			bindList.add(productData.getUdfs().get("EXPOSURERECIPENAME"));
			bindList.add(productData.getUdfs().get("REASONCODEDEPARTMENT"));
			bindList.add(productData.getUdfs().get("SLOTPOSITION"));
			bindList.add(productData.getLastProcessingTime());
			bindList.add(productData.getLastProcessingUser());
			bindList.add(productData.getMachineName());
			bindList.add(productData.getMaterialLocationName());
			bindList.add(productData.getReworkNodeId());
			bindList.add(productData.getCarrierName());
			bindList.add(productData.getTransportGroupName());
			bindList.add(oldProduct.getProcessOperationVersion());
			bindList.add(oldProduct.getFactoryName());
			bindList.add(oldProduct.getProductRequestName());
			bindList.add(oldProduct.getMachineName());
			bindList.add(oldProduct.getProcessOperationName());
			bindList.add(productData.getUdfs().get("ARRAYLOTNAME"));

			insertArgListHistory.add(bindList.toArray());

		}

		// Update Batch
		if (insertArgListHistory.size() > 0)
		{
			try
			{
				if (insertArgListHistory.size() == 1)
				{
					GenericServiceProxy.getSqlMesTemplate().update(queryStringHistory, insertArgListHistory.get(0));
				}
				else
				{
					updateBatch(queryStringHistory, insertArgListHistory);
				}
			}
			catch (Exception e)
			{
				throw new CustomException();
			}
		}
	}
	//caixu 2020/06/09
		public void InsertScrapProduct(List<Object[]> scrapProductList )throws CustomException
		{
			
			String queryStrigScrapProduct="INSERT INTO CT_SCRAPPRODUCT(PRODUCTNAME,LOTNAME,PRODUCTTYPE,PRODUCTGRADE,PRODUCTSTATE,LASTEVENTTIMEKEY,LASTEVENTTIME,LASTEVENTUSER,REASONCODETYPE,REASONCODE,SCRAPDEPARTMENT,SCRAPOPERATIONNAME,SCRAPMACHINENAME,SCRAPUNITNAME,SCRAPSUBUNITNAME,LASTEVENTCOMMENT)VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			try
			{
			MESLotServiceProxy.getLotServiceUtil().updateBatch(queryStrigScrapProduct, scrapProductList);
			}
			catch (Exception e)
			{
			  throw new CustomException();
			}
			
		}
		public void deleteScrapProduct(List<Lot> lotList )throws CustomException
		{
			
			String queryStrigScrapProduct="DELETE FROM CT_SCRAPPRODUCT WHERE PRODUCTNAME=:PRODUCTNAME";
			List<Object[]> deleteScrapListProduct= new ArrayList<Object[]>();
			for(Lot lotData : lotList)
			{
				List<Object> bindList = new ArrayList<Object>();
				bindList.add(lotData.getKey().getLotName());
				deleteScrapListProduct.add(bindList.toArray());
			}
			try
			{
			MESLotServiceProxy.getLotServiceUtil().updateBatch(queryStrigScrapProduct, deleteScrapListProduct);
			}
			catch (Exception e)
			{
			  throw new CustomException();
			}
			
		}
		//20200618 add deleteScrapGlass
		public void deleteScrapGlass(List<ProductP> lotList )throws CustomException
		{
			
			String queryStrigScrapProduct="DELETE FROM CT_SCRAPPRODUCT WHERE PRODUCTNAME=:PRODUCTNAME";
			List<Object[]> deleteScrapListProduct= new ArrayList<Object[]>();
			for(ProductP productData : lotList)
			{
				List<Object> bindList = new ArrayList<Object>();
				bindList.add(productData.getProductName());
				deleteScrapListProduct.add(bindList.toArray());
			}
			try
			{
			MESLotServiceProxy.getLotServiceUtil().updateBatch(queryStrigScrapProduct, deleteScrapListProduct);
			}
			catch (Exception e)
			{
			  throw new CustomException();
			}
			
		}

	public void insertLotHistory(EventInfo eventInfo, List<Lot> lotList, List<Lot> oldLotList, String consumerLotName, String consumerTimeKey, String consumedLotName, String consumedDurableName,
			String consumedConsumableName) throws CustomException
	{
		// History insert Query & insertArgList
		StringBuffer queryStringHistory = new StringBuffer();
		queryStringHistory.append("INSERT INTO LOTHISTORY  ");
		queryStringHistory.append("(LOTNAME, TIMEKEY, EVENTTIME, EVENTNAME, PRODUCTIONTYPE, ");
		queryStringHistory.append(" PRODUCTSPECNAME, PRODUCTSPECVERSION, PROCESSGROUPNAME, PRODUCTREQUESTNAME, ORIGINALLOTNAME, ");
		queryStringHistory.append(" SOURCELOTNAME, DESTINATIONLOTNAME, ROOTLOTNAME, PARENTLOTNAME, CARRIERNAME, ");
		queryStringHistory.append(" PRODUCTTYPE, SUBPRODUCTTYPE, SUBPRODUCTUNITQUANTITY1, SUBPRODUCTUNITQUANTITY2, PRODUCTQUANTITY, ");
		queryStringHistory.append(" SUBPRODUCTQUANTITY, SUBPRODUCTQUANTITY1, OLDSUBPRODUCTQUANTITY2, SUBPRODUCTQUANTITY2, LOTGRADE, ");
		queryStringHistory.append(" DUEDATE, PRIORITY, FACTORYNAME, DESTINATIONFACTORYNAME, AREANAME, ");
		queryStringHistory.append(" LOTSTATE, LOTPROCESSSTATE, LOTHOLDSTATE, EVENTUSER, EVENTCOMMENT, ");
		queryStringHistory.append(" EVENTFLAG, LASTLOGGEDINTIME, LASTLOGGEDINUSER, LASTLOGGEDOUTTIME, LASTLOGGEDOUTUSER, ");
		queryStringHistory.append(" REASONCODE, REASONCODETYPE, PROCESSFLOWNAME, PROCESSFLOWVERSION, PROCESSOPERATIONNAME, ");
		queryStringHistory.append(" PROCESSOPERATIONVERSION, NODESTACK, MACHINENAME, MACHINERECIPENAME, REWORKSTATE, ");
		queryStringHistory.append(" REWORKCOUNT, REWORKNODEID, CONSUMERLOTNAME, CONSUMERTIMEKEY, CONSUMEDLOTNAME, ");
		queryStringHistory.append(" CONSUMEDDURABLENAME, CONSUMEDCONSUMABLENAME, SYSTEMTIME, CANCELFLAG, CANCELTIMEKEY, ");
		queryStringHistory.append(" BRANCHENDNODEID, RETURNFLOWNAME, RETURNOPERATIONNAME, RETURNOPERATIONVER, PORTNAME, ");
		queryStringHistory.append(" PORTTYPE, PORTUSETYPE, BEFOREOPERATIONNAME, BEFOREOPERATIONVER, BEFOREFLOWNAME, ");
		queryStringHistory.append(" DSPFLAG, OLDPRODUCTREQUESTNAME, RELEASEQUANTITY, LASTFACTORYNAME, OLDPRODUCTQUANTITY, ");
		queryStringHistory.append(" OLDSUBPRODUCTQUANTITY, OLDSUBPRODUCTQUANTITY1, OLDPROCESSOPERATIONNAME, OLDPRODUCTTYPE, OLDSUBPRODUCTTYPE, ");
		queryStringHistory.append(" OLDPRODUCTSPECNAME, OLDPRODUCTSPECVERSION, OLDPROCESSFLOWNAME, OLDPROCESSFLOWVERSION, OLDPRODUCTIONTYPE, ");
		queryStringHistory.append(" OLDFACTORYNAME, OLDAREANAME, OLDPROCESSOPERATIONVERSION, OLDDESTINATIONFACTORYNAME, POSITION, ");
		queryStringHistory.append(" LOTDETAILGRADE, ARRAYLOTNAME) ");
		queryStringHistory.append("VALUES  ");
		queryStringHistory.append("(?,?,?,?,?, ");
		queryStringHistory.append(" ?,?,?,?,?, ");
		queryStringHistory.append(" ?,?,?,?,?, ");
		queryStringHistory.append(" ?,?,?,?,?, ");
		queryStringHistory.append(" ?,?,?,?,?, ");
		queryStringHistory.append(" ?,?,?,?,?, ");
		queryStringHistory.append(" ?,?,?,?,?, ");
		queryStringHistory.append(" ?,?,?,?,?, ");
		queryStringHistory.append(" ?,?,?,?,?, ");
		queryStringHistory.append(" ?,?,?,?,?, ");
		queryStringHistory.append(" ?,?,?,?,?, ");
		queryStringHistory.append(" ?,?,?,?,?, ");
		queryStringHistory.append(" ?,?,?,?,?, ");
		queryStringHistory.append(" ?,?,?,?,?, ");
		queryStringHistory.append(" ?,?,?,?,?, ");
		queryStringHistory.append(" ?,?,?,?,?, ");
		queryStringHistory.append(" ?,?,?,?,?, ");
		queryStringHistory.append(" ?,?,?,?,?, ");
		queryStringHistory.append(" ?,?) ");

		List<Object[]> insertArgListHistory = new ArrayList<Object[]>();

		for (Lot lotData : lotList)
		{
			EventInfo newEventInfo = EventInfoUtil.makeEventInfo(eventInfo.getEventName(), eventInfo.getEventUser(), eventInfo.getEventComment(), eventInfo.getReasonCodeType(),
					eventInfo.getReasonCode());
			newEventInfo.setEventTimeKey(eventInfo.getEventTimeKey());
			newEventInfo.setEventTime(eventInfo.getEventTime());
			newEventInfo.setReasonCode(lotData.getReasonCode());
			Lot oldLot = new Lot();
			if (oldLotList.size() > 0)
			{
				for (Lot oldLotData : oldLotList)
				{
					if (lotData.getKey().getLotName().equals(oldLotData.getKey().getLotName()))
					{
						oldLot = oldLotData;
					}
				}
			}

			// insert History
			List<Object> bindList = new ArrayList<Object>();
			bindList.add(lotData.getKey().getLotName());
			bindList.add(newEventInfo.getEventTimeKey());
			bindList.add(newEventInfo.getEventTime());
			bindList.add(newEventInfo.getEventName());
			bindList.add(lotData.getProductionType());
			bindList.add(lotData.getProductSpecName());
			bindList.add(lotData.getProductSpecVersion());
			bindList.add(lotData.getProcessGroupName());
			bindList.add(lotData.getProductRequestName());
			bindList.add(lotData.getOriginalLotName());
			bindList.add(lotData.getSourceLotName());
			bindList.add(lotData.getDestinationLotName());
			bindList.add(lotData.getRootLotName());
			bindList.add(lotData.getParentLotName());
			bindList.add(lotData.getCarrierName());
			bindList.add(lotData.getProductType());
			bindList.add(lotData.getSubProductType());
			bindList.add(lotData.getSubProductUnitQuantity1());
			bindList.add(lotData.getSubProductUnitQuantity2());
			bindList.add(lotData.getProductQuantity());
			bindList.add(lotData.getSubProductQuantity());
			bindList.add(lotData.getSubProductQuantity1());
			bindList.add(oldLot.getSubProductQuantity2());
			bindList.add(lotData.getSubProductQuantity2());
			bindList.add(lotData.getLotGrade());
			bindList.add(lotData.getDueDate());
			bindList.add(lotData.getPriority());
			bindList.add(lotData.getFactoryName());
			bindList.add(lotData.getDestinationFactoryName());
			bindList.add(lotData.getAreaName());
			bindList.add(lotData.getLotState());
			bindList.add(lotData.getLotProcessState());
			bindList.add(lotData.getLotHoldState());
			bindList.add(newEventInfo.getEventUser());
			bindList.add(newEventInfo.getEventComment());
			bindList.add("N");
			bindList.add(lotData.getLastLoggedInTime());
			bindList.add(lotData.getLastLoggedInUser());
			bindList.add(lotData.getLastLoggedOutTime());
			bindList.add(lotData.getLastLoggedOutUser());
			bindList.add(newEventInfo.getReasonCode());
			bindList.add(newEventInfo.getReasonCodeType());
			bindList.add(lotData.getProcessFlowName());
			bindList.add(lotData.getProcessFlowVersion());
			bindList.add(lotData.getProcessOperationName());
			bindList.add(lotData.getProcessOperationVersion());
			bindList.add(lotData.getNodeStack());
			bindList.add(lotData.getMachineName());
			bindList.add(lotData.getMachineRecipeName());
			bindList.add(lotData.getReworkState());
			bindList.add(lotData.getReworkCount());
			bindList.add(lotData.getReworkNodeId());
			bindList.add(consumerLotName);
			bindList.add(consumerTimeKey);
			bindList.add(consumedLotName);
			bindList.add(consumedDurableName);
			bindList.add(consumedConsumableName);
			bindList.add(newEventInfo.getEventTime());
			bindList.add("A");
			bindList.add("");
			bindList.add(lotData.getBranchEndNodeId());
			bindList.add(lotData.getUdfs().get("RETURNFLOWNAME"));
			bindList.add(lotData.getUdfs().get("RETURNOPERATIONNAME"));
			bindList.add(lotData.getUdfs().get("RETURNOPERATIONVER"));
			bindList.add(lotData.getUdfs().get("PORTNAME"));
			bindList.add(lotData.getUdfs().get("PORTTYPE"));
			bindList.add(lotData.getUdfs().get("PORTUSETYPE"));
			bindList.add(lotData.getUdfs().get("BEFOREOPERATIONNAME"));
			bindList.add(lotData.getUdfs().get("BEFOREOPERATIONVER"));
			bindList.add(lotData.getUdfs().get("BEFOREFLOWNAME"));
			bindList.add(lotData.getUdfs().get("DSPFLAG"));
			bindList.add(lotData.getUdfs().get("OLDPRODUCTREQUESTNAME"));
			bindList.add(lotData.getUdfs().get("RELEASEQUANTITY"));
			bindList.add(lotData.getUdfs().get("LASTFACTORYNAME"));
			bindList.add(oldLot.getProductQuantity());
			bindList.add(oldLot.getSubProductQuantity());
			bindList.add(oldLot.getSubProductQuantity1());
			bindList.add(oldLot.getProcessOperationName());
			bindList.add(oldLot.getProductType());
			bindList.add(oldLot.getSubProductType());
			bindList.add(oldLot.getProductSpecName());
			bindList.add(oldLot.getProductSpecVersion());
			bindList.add(oldLot.getProcessFlowName());
			bindList.add(oldLot.getProcessFlowVersion());
			bindList.add(oldLot.getProductionType());
			bindList.add(oldLot.getFactoryName());
			bindList.add(oldLot.getAreaName());
			bindList.add(oldLot.getProcessOperationVersion());
			bindList.add(oldLot.getDestinationFactoryName());
			bindList.add(lotData.getUdfs().get("POSITION"));
			bindList.add(lotData.getUdfs().get("LOTDETAILGRADE"));
			bindList.add(lotData.getUdfs().get("ARRAYLOTNAME"));

			insertArgListHistory.add(bindList.toArray());
		}

		if (insertArgListHistory.size() > 0)
		{
			try
			{
				if (insertArgListHistory.size() == 1)
					GenericServiceProxy.getSqlMesTemplate().update(queryStringHistory.toString(), insertArgListHistory.get(0));
				else
					GenericServiceProxy.getSqlMesTemplate().updateBatch(queryStringHistory.toString(), insertArgListHistory);
			}
			catch (Exception e)
			{
				throw new CustomException();
			}
		}
	}

	public List<Map<String, Object>> getLotListByTrayGroup(String trayGroupName) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT L.* ");
		sql.append("  FROM DURABLE D, LOT L ");
		sql.append(" WHERE D.COVERNAME = :TRAYGROUPNAME ");
		sql.append("   AND D.DURABLENAME = L.CARRIERNAME ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("TRAYGROUPNAME", trayGroupName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		return sqlResult;
	}

	public List<Lot> getLotListByProcessGroup(String processGroupName) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT L.LOTNAME ");
		sql.append("  FROM LOT L, PROCESSGROUP P ");
		sql.append(" WHERE L.PROCESSGROUPNAME = P.PROCESSGROUPNAME ");
		sql.append("   AND P.PROCESSGROUPTYPE = 'InnerPacking' ");
		sql.append("   AND L.PROCESSGROUPNAME = :PROCESSGROUPNAME ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("PROCESSGROUPNAME", processGroupName);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
		
		List<Lot> lotDataList = new ArrayList<Lot>();
		
		if(sqlResult != null && sqlResult.size() > 0)
		{
			String condition = "WHERE LOTNAME IN(";
			for (Map<String, Object> lotNameMap : sqlResult) 
			{
				String lotName = lotNameMap.get("LOTNAME").toString();
				
				condition += "'" + lotName + "',";
			}
			condition = condition.substring(0, condition.length() - 1) + ")";
			
			lotDataList = LotServiceProxy.getLotService().select(condition, new Object[] { });
		}

		return lotDataList;
	}

	public List<Lot> getScrapLotListByProcessGroup(String processGroupName) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT L.LOTNAME ");
		sql.append("  FROM LOT L, PROCESSGROUP P ");
		sql.append(" WHERE L.PROCESSGROUPNAME = P.PROCESSGROUPNAME ");
		sql.append("   AND P.PROCESSGROUPTYPE = 'ScrapPacking' ");
		sql.append("   AND L.PROCESSGROUPNAME = :PROCESSGROUPNAME ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("PROCESSGROUPNAME", processGroupName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		List<Lot> lotDataList = new ArrayList<Lot>();
		
		if(sqlResult != null && sqlResult.size() > 0)
		{
			String condition = "WHERE LOTNAME IN(";
			for (Map<String, Object> lotNameMap : sqlResult) 
			{
				String lotName = lotNameMap.get("LOTNAME").toString();
				
				condition += "'" + lotName + "',";
			}
			condition = condition.substring(0, condition.length() - 1) + ")";
			
			lotDataList = LotServiceProxy.getLotService().select(condition, new Object[] { });
		}

		return lotDataList;
	}

	public Lot createWithParentLotAndProductProductionType(EventInfo eventInfo, String newLotName, Lot parentlotData, String productProductionType, String newCarrierName, boolean deassignFlag,
			Map<String, String> assignCarrierUdfs, Map<String, String> udfs) throws CustomException
	{
		CreateWithParentLotInfo createWithParentLotInfo = MESLotServiceProxy.getLotInfoUtil().createWithParentLotInfo(parentlotData.getAreaName(), deassignFlag ? "N" : "Y", assignCarrierUdfs,
				deassignFlag ? "" : newCarrierName, parentlotData.getDueDate(), parentlotData.getFactoryName(), parentlotData.getLastLoggedInTime(), parentlotData.getLastLoggedInUser(),
				parentlotData.getLastLoggedOutTime(), parentlotData.getLastLoggedOutUser(), parentlotData.getLotGrade(), parentlotData.getLotHoldState(), newLotName,
				parentlotData.getLotProcessState(), parentlotData.getLotState(), parentlotData.getMachineName(), parentlotData.getMachineRecipeName(), parentlotData.getNodeStack(),
				parentlotData.getOriginalLotName(), parentlotData.getPriority(), parentlotData.getProcessFlowName(), parentlotData.getProcessFlowVersion(), parentlotData.getProcessGroupName(),
				parentlotData.getProcessOperationName(), parentlotData.getProcessOperationVersion(), productProductionType, new ArrayList<ProductP>(), 0, parentlotData.getProductRequestName(),
				parentlotData.getProductSpec2Name(), parentlotData.getProductSpec2Version(), parentlotData.getProductSpecName(), parentlotData.getProductSpecVersion(), parentlotData.getProductType(),
				parentlotData.getReworkCount(), "", parentlotData.getReworkNodeId(), parentlotData.getRootLotName(), parentlotData.getKey().getLotName(), parentlotData.getSubProductType(),
				parentlotData.getSubProductUnitQuantity1(), parentlotData.getSubProductUnitQuantity2(), udfs, parentlotData);

		// Set ReturnInfo
		createWithParentLotInfo.getUdfs().put("RETURNFLOWNAME", parentlotData.getUdfs().get("RETURNFLOWNAME"));
		createWithParentLotInfo.getUdfs().put("RETURNOPERATIONNAME", parentlotData.getUdfs().get("RETURNOPERATIONNAME"));
		createWithParentLotInfo.getUdfs().put("RETURNOPERATIONVER", parentlotData.getUdfs().get("RETURNOPERATIONVERSION"));
		
		Lot newLotData = MESLotServiceProxy.getLotServiceImpl().createWithParentLot(eventInfo, newLotName, createWithParentLotInfo);

		return newLotData;
	}

	public Element createSortJobPrepareBodyElement(String jobName) throws CustomException
	{
		List<SortJobCarrier> jobCarrier = ExtendedObjectProxy.getSortJobCarrierService().getSortJobCarrierListByJobName(jobName);

		Element bodyElement = new Element("Body");

		// MACHINENAME
		Element machineNameElement = new Element("MACHINENAME");
		machineNameElement.setText(jobCarrier.get(0).getMachineName());
		bodyElement.addContent(machineNameElement);

		// JOBNAME
		Element jobNameElement = new Element("JOBNAME");
		jobNameElement.setText(jobName);
		bodyElement.addContent(jobNameElement);

		// PORTLIST
		bodyElement.addContent(this.createSortJobFromPortInfoElement(jobCarrier));

		return bodyElement;
	}

	public Map<String, String> isMQCRecycle(EventInfo eventInfo, Lot lotData, String nextProcessFlowType) throws CustomException
	{
		boolean isMQCRecycle = false;
		Map<String, String> recycleFlow = null;

		if (StringUtils.equals(nextProcessFlowType, "MQCPrepare"))
		{
			MQCPlan planData = null;

			try
			{
				planData = ExtendedObjectProxy.getMQCPlanService().select(" lotName = ? AND MQCstate in ( ?, ? ) ", new Object[] { lotData.getKey().getLotName(), "Released", "Recycling" }).get(0);
			}
			catch (Exception e)
			{
			}

			if (planData != null)
			{
				if (StringUtils.isNotEmpty(planData.getRecycleFlowName()) && StringUtils.isNotEmpty(planData.getRecycleFlowVersion()))
				{
					isMQCRecycle = true;

					recycleFlow = new HashMap<String, String>();
					recycleFlow.put("RECYCLEFLOWNAME", planData.getRecycleFlowName());
					recycleFlow.put("RECYCLEFLOWVERSION", planData.getRecycleFlowVersion());
				}

				if (isMQCRecycle)
				{
					// MQCJob: MQCState - Released
					// NodeStack: ReturnNodeId.RecycleNodeId
					eventInfo.setEventName("RecycleMQC");

					try
					{
						planData = ExtendedObjectProxy.getMQCPlanService().updateMQCPlanCount(eventInfo, planData, "Recycling");

						SetEventInfo setEventInfo = new SetEventInfo();
						LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
					}
					catch (Exception e)
					{
						log.info("Error occured - Recycle MQC Lot");
						recycleFlow = null;
					}
				}
				else
				{
					String FirstOperationName = "";
					String FirstOperationVer = "";

					Node targetNode = getFirstNode(planData.getJobName(), planData.getFactoryName(), planData.getProcessFlowName(), planData.getProcessFlowVersion());

					if (targetNode == null)
					{
						FirstOperationName = planData.getReturnOperationName();
						FirstOperationVer = planData.getReturnOperationVersion();
					}
					else
					{
						FirstOperationName = targetNode.getNodeAttribute1();
						FirstOperationVer = targetNode.getNodeAttribute2();
					}

					ProcessFlow currentFlowData = null;
					try
					{
						currentFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);

						if (StringUtils.equals(currentFlowData.getProcessFlowType(), "MQCRecycle"))
						{
							log.info("currentFlowData is Recycle MQC");
							boolean isLastMQCRecycle = false;
							try
							{
								isLastMQCRecycle = checkNextMQCRecycleNode(lotData);
							}
							catch (Exception e)
							{
								log.info("Error occured - Recycle MQC NextNode");
							}

							// Last MQC Recycling Operation
							if (isLastMQCRecycle)
							{
								log.info("This Lot is in Last MQC Recycling Operation");
								RemoveRecycleMQCJob(eventInfo, lotData);
								log.info("Completed - Remove Recycle MQC Job");
							}

						}
					}
					catch (Exception e)
					{
						log.info("Error occured - Recycle MQC currentFlowData");
					}

					// MQCJob: 1. Delete or 2. Change State to Completed | Suspending
					// ChangeSpec: Return to MQC PrepareSpec or Not

					try
					{
						if (StringUtils.isNotEmpty(planData.getPrepareSpecName()) && StringUtils.isNotEmpty(planData.getPrepareSpecVersion()))
						{
							ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo(lotData.getProductionType(), planData.getPrepareSpecName(), planData.getPrepareSpecVersion(),
									lotData.getProductSpec2Name(), lotData.getProductSpec2Version(), lotData.getProductRequestName(), lotData.getSubProductUnitQuantity1(),
									lotData.getSubProductUnitQuantity2(), lotData.getDueDate(), lotData.getPriority(), lotData.getFactoryName(), lotData.getAreaName(), lotData.getLotState(),
									lotData.getLotProcessState(), lotData.getLotHoldState(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(),
									lotData.getProcessOperationVersion(), lotData.getNodeStack(), new ArrayList<ProductU>());

							changeSpecInfo.setUdfs(lotData.getUdfs());

							eventInfo.setEventName("SuspendMQC");
							MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);

							eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
						}

						eventInfo.setEventName("SuspendMQC");
						ExtendedObjectProxy.getMQCPlanService().updateMQCPlanReturnOper(eventInfo, planData, "Suspending", FirstOperationName, FirstOperationVer);
					}
					catch (Exception e)
					{
						log.info("Error occured - Suspending MQC Lot");
					}
				}
			}
		}

		return recycleFlow;
	}

	public Node setReturnNodeStackForRecycleMQC(EventInfo eventInfo, Lot lotData, String lotName, String recycleFlowName, String recycleFlowVersion, Node nextNode) throws CustomException
	{
		MQCPlan planData = ExtendedObjectProxy.getMQCPlanService().getMQCPlanDataByLotNameMultiState(lotName).get(0);

		if (planData != null)
		{
			Node startNode = ProcessFlowServiceProxy.getProcessFlowService().getStartNode(new ProcessFlowKey(planData.getFactoryName(), recycleFlowName, recycleFlowVersion));
			Node targetNode = ProcessFlowServiceProxy.getProcessFlowService().getNextNode(startNode.getKey().getNodeId(), "Normal", "");

			String nextNodeStack = new StringBuilder(nextNode.getKey().getNodeId()).append(".").append(targetNode.getKey().getNodeId()).toString();
			targetNode.getKey().setNodeId(nextNodeStack);

			return targetNode;
		}

		return nextNode;
	}

	public Element createSortJobFromPortInfoElement(List<SortJobCarrier> jobCarrierList) throws CustomException
	{
		Element portListElement = new Element("PORTLIST");
		for (SortJobCarrier jobList : jobCarrierList)
		{
			Element portElement = new Element("PRODUCT");

			// PORTNAME
			Element portNameElement = new Element("PORTNAME");
			portNameElement.setText(jobList.getPortName());
			portElement.addContent(portNameElement);

			// CARRIERNAME
			Element carrierNameElement = new Element("CARRIERNAME");
			carrierNameElement.setText(jobList.getCarrierName());
			portElement.addContent(carrierNameElement);

			portListElement.addContent(portElement);
		}

		return portListElement;
	}

	public Node skipOperForMQC(EventInfo eventInfo, Lot lotData, String nextOper, String nextOperVer) throws CustomException
	{
		Node nextNode = null;

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT QL.PROCESSOPERATIONNAME,  ");
		sql.append("       QL.PROCESSOPERATIONVERSION,  ");
		sql.append("       PO.DESCRIPTION,  ");
		sql.append("       PO.PROCESSOPERATIONTYPE  ");
		sql.append("  FROM PROCESSOPERATIONSPEC PO,  ");
		sql.append("       (SELECT LEVEL LV,  ");
		sql.append("               N.FACTORYNAME,  ");
		sql.append("               N.NODEATTRIBUTE1 AS PROCESSOPERATIONNAME,  ");
		sql.append("               N.NODEATTRIBUTE2 AS PROCESSOPERATIONVERSION,  ");
		sql.append("               N.PROCESSFLOWNAME,  ");
		sql.append("               N.PROCESSFLOWVERSION,  ");
		sql.append("               N.NODEID  ");
		sql.append("          FROM ARC A, NODE N, PROCESSFLOW PF  ");
		sql.append("         WHERE 1 = 1  ");
		sql.append("           AND N.NODETYPE = 'ProcessOperation'  ");
		sql.append("           AND PF.PROCESSFLOWNAME = :PROCESSFLOWNAME  ");
		sql.append("           AND PF.PROCESSFLOWVERSION = :PROCESSFLOWVERSION  ");
		sql.append("           AND N.FACTORYNAME = :FACTORYNAME  ");
		sql.append("           AND N.PROCESSFLOWNAME = PF.PROCESSFLOWNAME  ");
		sql.append("           AND N.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION  ");
		sql.append("           AND N.PROCESSFLOWNAME = A.PROCESSFLOWNAME  ");
		sql.append("           AND N.FACTORYNAME = PF.FACTORYNAME  ");
		sql.append("           AND A.FROMNODEID = N.NODEID  ");
		sql.append("        START WITH N.NODETYPE = 'Start'  ");
		sql.append("        CONNECT BY NOCYCLE A.FROMNODEID = PRIOR A.TONODEID AND A.FACTORYNAME = :FACTORYNAME) QL  ");
		sql.append(" WHERE 1 = 1  ");
		sql.append("   AND PO.PROCESSOPERATIONNAME = QL.PROCESSOPERATIONNAME  ");
		sql.append("   AND PO.FACTORYNAME = :FACTORYNAME  ");
		sql.append("ORDER BY QL.LV  ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", lotData.getFactoryName());
		args.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
		args.put("PROCESSFLOWVERSION", lotData.getProcessFlowVersion());

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			for (int i = 0; result.size() > i; i++)
			{
				Map<String, Object> map = result.get(i);

				String operByList = ConvertUtil.getMapValueByName(map, "PROCESSOPERATIONNAME");
				String operVerByList = ConvertUtil.getMapValueByName(map, "PROCESSOPERATIONVERSION");

				if (StringUtils.equals(operByList, nextOper))
				{
					sql.setLength(0);
					sql.append("SELECT P.FACTORYNAME, ");
					sql.append("       D.PROCESSFLOWNAME, ");
					sql.append("       D.PROCESSFLOWVERSION, ");
					sql.append("       D.PROCESSOPERATIONNAME, ");
					sql.append("       D.PROCESSOPERATIONVERSION ");
					sql.append("  FROM CT_MQCPLAN P, CT_MQCPLANDETAIL D ");
					sql.append(" WHERE P.JOBNAME = D.JOBNAME ");
					sql.append("   AND P.LOTNAME = :LOTNAME ");
					sql.append("   AND P.MQCSTATE = 'Released' ");
					sql.append("   AND D.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
					sql.append("   AND D.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
					sql.append("   AND D.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
					sql.append("   AND D.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
					sql.append("ORDER BY P.FACTORYNAME, D.PROCESSOPERATIONNAME, D.PROCESSOPERATIONVERSION ");

					args.put("LOTNAME", lotData.getKey().getLotName());
					args.put("PROCESSOPERATIONNAME", nextOper);
					args.put("PROCESSOPERATIONVERSION", operVerByList);

					List<Map<String, Object>> resultMQC = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

					if (resultMQC.size() > 0)
					{
						Node nodeStack = ProcessFlowServiceProxy.getNodeService().getNode(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), "ProcessOperation",
								operByList, operVerByList);

						if (nodeStack != null)
						{
							log.info("Process Next Oper");

							String[] step1 = StringUtils.split(lotData.getNodeStack(), ".");

							String tempNode = step1[0] + "." + nodeStack.getKey().getNodeId();
							nodeStack.getKey().setNodeId(tempNode);

							return nodeStack;
						}
					}
					else
					{
						if (result.size() != i + 1)
						{
							try
							{
								String nextOperByList = ConvertUtil.getMapValueByName(result.get(i + 1), "PROCESSOPERATIONNAME");
								String nextOperVerByList = ConvertUtil.getMapValueByName(result.get(i + 1), "PROCESSOPERATIONVERSION");

								Node nodeStack = skipOperForMQC(eventInfo, lotData, nextOperByList, nextOperVerByList);

								if (nodeStack != null)
								{
									return nodeStack;
								}
							}
							catch (Exception e)
							{
								log.debug("TEMP");
							}
						}
						else
						{
							// Move to Recycle FLow or Bank for MQC
							Map<String, String> lotUdfs = lotData.getUdfs();
							String currentNode = lotData.getNodeStack();

							if (StringUtils.isNotEmpty(currentNode) && currentNode.indexOf(".") > 0)
							{
								Node nextStepNode = ProcessFlowServiceProxy.getNodeService().getNode(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
										"ProcessOperation", nextOper, operVerByList);

								String[] step1 = StringUtils.split(currentNode, ".");

								String tempNode = step1[0] + "." + nextStepNode.getKey().getNodeId();
								lotData.setNodeStack(tempNode);
								try
								{
									nextNode = PolicyUtil.getNextOperation(lotData);
								}
								catch (Exception e)
								{
									return null;
								}

								ProcessFlowKey processFlowKey1 = new ProcessFlowKey();
								processFlowKey1.setFactoryName(nextNode.getFactoryName());
								processFlowKey1.setProcessFlowName(nextNode.getProcessFlowName());
								processFlowKey1.setProcessFlowVersion(nextNode.getProcessFlowVersion());
								ProcessFlow nextNodeFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey1);

								Map<String, String> mqcRecycleFlow = isMQCRecycle(eventInfo, lotData, nextNodeFlowData.getProcessFlowType());
								eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
								eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

								if (mqcRecycleFlow != null)
								{
									Node returnMQCNode = setReturnNodeStackForRecycleMQC(eventInfo, lotData, lotData.getKey().getLotName(), mqcRecycleFlow.get("RECYCLEFLOWNAME"),
											mqcRecycleFlow.get("RECYCLEFLOWVERSION"), nextNode);

									if (returnMQCNode != null)
									{
										nextNode.getKey().setNodeId(returnMQCNode.getKey().getNodeId());
										nextNode.setProcessFlowName(returnMQCNode.getProcessFlowName());
										nextNode.setProcessFlowVersion(returnMQCNode.getProcessFlowVersion());
										nextNode.setNodeAttribute1(returnMQCNode.getNodeAttribute1());
										nextNode.setNodeAttribute2(returnMQCNode.getNodeAttribute2());
									}
								}
								else
								{
									String originalNode = currentNode.substring(0, currentNode.lastIndexOf("."));

									if (originalNode.lastIndexOf(".") > -1)
									{
										originalNode = originalNode.substring(0, originalNode.lastIndexOf("."));
									}

									String sql1 = "SELECT NODEID, PROCESSFLOWNAME, NODEATTRIBUTE1, NODEATTRIBUTE2, FACTORYNAME FROM NODE WHERE NODEID = ? AND NODETYPE = 'ProcessOperation' ";
									Object[] bind = new Object[] { originalNode };

									String[][] orginalNodeResult = null;
									try
									{
										orginalNodeResult = GenericServiceProxy.getSqlMesTemplate().queryForStringArray(sql1, bind);
									}
									catch (Exception e)
									{
									}

									if (orginalNodeResult.length > 0)
									{
										lotUdfs.put("RETURNFLOWNAME", orginalNodeResult[0][1]);
										lotUdfs.put("RETURNOPERATIONNAME", orginalNodeResult[0][2]);
										lotUdfs.put("RETURNOPERATIONVER", orginalNodeResult[0][3]);
									}
								}
							}
						}
					}
				}
				else
				{
					continue;
				}
			}
		}

		return nextNode;
	}

	public Node getNextNodeForDummy(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		Node nextNode = null;

		String condition = " LOTNAME = ? ";
		Object[] bindSet = new Object[] { lotData.getKey().getLotName()};
		
		List<DummyProductReserve> dummyProductList = ExtendedObjectProxy.getDummyProductReserveService().select(condition, bindSet);
		
		int currentSeq = 0;
		
		for(DummyProductReserve reserveData : dummyProductList)
		{
			if(StringUtil.equals(reserveData.getProcessOperationName(), lotData.getProcessOperationName()))
			{
				currentSeq = Integer.parseInt(reserveData.getSeq());
			}
		}
		
		if(currentSeq == dummyProductList.size())
		{
			nextNode = PolicyUtil.getNextOperation(lotData); 
		}
		else
		{
			String nextOperation = "";
			String nextOperationVer = "";
			
			for(DummyProductReserve reserveData : dummyProductList)
			{
				if(StringUtil.equals(String.valueOf(currentSeq + 1), reserveData.getSeq()))
				{
					nextOperation = reserveData.getProcessOperationName();
					nextOperationVer = reserveData.getProcessOperationVersion();
					break;
				}
			}
			
			nextNode = ProcessFlowServiceProxy.getNodeService().getNode(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), "ProcessOperation",
					nextOperation, nextOperationVer);
		}

		return nextNode;
	}
	
	public boolean isExistAbortedAlarmFailProduct(List<Element> productList) throws CustomException
	{
		boolean isExistAlarmFailProduct = false;

		for (Element productE : productList)
		{
			String processingInfo = productE.getChildText("PROCESSINGINFO");

			if (StringUtils.equals(processingInfo, "F"))
			{
				isExistAlarmFailProduct = true;
				return isExistAlarmFailProduct;
			}
		}

		return isExistAlarmFailProduct;
	}
 
	public String checkReworkCountLimit(Lot lotData) throws CustomException
	{
		String reworkType = "";

		// Check Rework Count Limit
		ProcessFlowKey processFlowKey = new ProcessFlowKey();
		processFlowKey.setFactoryName(lotData.getFactoryName());
		processFlowKey.setProcessFlowName(lotData.getProcessFlowName());
		processFlowKey.setProcessFlowVersion(lotData.getProcessFlowVersion());
		ProcessFlow processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);

		if (StringUtils.equals(processFlowData.getProcessFlowType(), GenericServiceProxy.getConstantMap().Arc_Rework))
		{

			StringBuffer strSQL = new StringBuffer();
			strSQL.append("SELECT P.REWORKTYPE, P.REWORKCOUNTLIMIT ");
			strSQL.append("  FROM TFOPOLICY T, POSALTERPROCESSOPERATION P ");
			strSQL.append(" WHERE T.CONDITIONID = P.CONDITIONID ");
			strSQL.append("   AND T.FACTORYNAME = :FACTORYNAME ");
			strSQL.append("   AND P.TOPROCESSFLOWNAME = :PROCESSFLOWNAME ");
			strSQL.append("   AND P.TOPROCESSOPERATIONVERSION = :PROCESSFLOWVERSION ");
			strSQL.append("   AND P.CONDITIONNAME = 'Rework' ");
			strSQL.append("   AND P.REWORKTYPE IS NOT NULL ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("FACTORYNAME", lotData.getFactoryName());
			bindMap.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
			bindMap.put("PROCESSFLOWVERSION", lotData.getProcessFlowVersion());

			List<Map<String, Object>> posAlterList = new ArrayList<Map<String, Object>>();

			posAlterList = GenericServiceProxy.getSqlMesTemplate().queryForList(strSQL.toString(), bindMap);
			long reworkCountLimit = 0;

			if (posAlterList.size() > 0)
			{
				try
				{
					reworkCountLimit = Long.valueOf((String) posAlterList.get(0).get("REWORKCOUNTLIMIT"));
				}
				catch (Exception e)
				{
					log.info("ReworkCountLimit : " + ConvertUtil.getMapValueByName(posAlterList.get(0), "REWORKCOUNTLIMIT"));
				}

				reworkType = posAlterList.get(0).get("REWORKTYPE").toString();

				StringBuffer sql = new StringBuffer();
				sql.append("SELECT SL.ACTUALSAMPLEPOSITION ");
				sql.append("  FROM CT_SAMPLELOT SL, LOT L ");
				sql.append(" WHERE L.LOTNAME = :LOTNAME ");
				sql.append("   AND SL.LOTNAME = L.LOTNAME ");
				sql.append("   AND SL.FACTORYNAME = L.FACTORYNAME ");
				sql.append("   AND SL.PRODUCTSPECNAME = L.PRODUCTSPECNAME ");
				sql.append("   AND SL.PRODUCTSPECVERSION = L.PRODUCTSPECVERSION ");
				sql.append("   AND SL.TOPROCESSFLOWNAME = L.PROCESSFLOWNAME ");
				sql.append("   AND SL.TOPROCESSFLOWVERSION = L.PROCESSFLOWVERSION ");
				sql.append("   AND SL.TOPROCESSOPERATIONNAME = L.PROCESSOPERATIONNAME ");
				sql.append("   AND SL.TOPROCESSOPERATIONVERSION = L.PROCESSOPERATIONVERSION ");

				Map<String, Object> args = new HashMap<String, Object>();
				args.put("LOTNAME", lotData.getKey().getLotName());

				List<Map<String, Object>> positionList = new ArrayList<Map<String, Object>>();
				positionList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

				if (positionList.size() > 0)
				{
					String position = (String) positionList.get(0).get("ACTUALSAMPLEPOSITION");
					String[] positionArr = position.split(", ");

					String condition = "";
					for (int i = 0; i < positionArr.length; i++)
					{
						if (i == 0)
							condition += " AND POSITION IN (";

						condition += "'" + positionArr[i] + "'";

						if (i != positionArr.length - 1)
							condition += ", ";

						else
						{
							condition += ")";
						}
					}

					List<Product> productList = ProductServiceProxy.getProductService().select("  LOTNAME = ? " + condition, new Object[] { lotData.getKey().getLotName() });

					for (Product product : productList)
					{
						try
						{
							List<ReworkProduct> reworkProductList = ExtendedObjectProxy.getReworkProductService().select(
									"  PRODUCTNAME = ? " + "AND FACTORYNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? ",
									new Object[] { product.getKey().getProductName(), lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), });

							//Add Validation, if ReworkCountLimit is null by dkh
							if(reworkProductList.get(0).getReworkCountLimit().isEmpty()){
								
								throw new CustomException("PRODUCT-0202", reworkProductList.get(0).getProductName());
							}
							
							if (reworkProductList.size() > 0)
							{
								// check reworCountLimit
								if (reworkCountLimit > 0 && reworkProductList.get(0).getReworkCount() > Integer.parseInt(reworkProductList.get(0).getReworkCountLimit()))
								{
									throw new CustomException("PRODUCT-0201", reworkProductList.get(0).getProductName());
								}
							}
							else
							{
								throw new CustomException("PRODUCT-9100", reworkProductList.get(0).getProductName());
							}

						}
						catch (greenFrameDBErrorSignal ex)
						{

						}
					}
				}
			}
		}

		return reworkType;
	}

	public Lot splitFirstGlassLot(EventInfo eventInfo, List<Element> productList, Lot lotData, String jobName) throws CustomException
	{
		log.debug(String.format("[ELAP]Lot composition start at [%s]", System.currentTimeMillis()));
		log.info(String.format("Lot[%s] is doing split with ProductQuantity[%d]", lotData.getKey().getLotName(), productList.size()));

		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("LOTNAME", lotData.getKey().getLotName());

		String newLotName = CommonUtil.generateNameByNamingRule("SplitLotNaming", nameRuleAttrMap, 1).get(0);

		eventInfo.setEventName("Create");
		Map<String, String> udf = new HashMap<String, String>();
		udf.put("FIRSTGLASSFLAG", "N");
		udf.put("JOBNAME", jobName);

		Lot childLot = MESLotServiceProxy.getLotServiceUtil().createWithParentLotAndProductProductionType(eventInfo, newLotName, lotData, lotData.getProductionType(), lotData.getCarrierName(), false,
				new HashMap<String, String>(), udf);

		List<ProductP> productPSequence = MESLotServiceProxy.getLotInfoUtil().setProductPSequence(productList);

		TransferProductsToLotInfo transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(childLot.getKey().getLotName(), productList.size(), productPSequence, udf,
				new HashMap<String, String>());

		// Split Lot
		eventInfo.setEventName("Split");
		lotData.getUdfs().put("JOBNAME", jobName);
		lotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotData, transitionInfo);

		childLot = MESLotServiceProxy.getLotInfoUtil().getLotData(childLot.getKey().getLotName());

		log.info(String.format("Lot[%s] of ProductQuantity[%f] in Carrier[%s]", childLot.getKey().getLotName(), childLot.getProductQuantity(), childLot.getCarrierName()));
		log.debug(String.format("[ELAP]Lot composition end at [%s]", System.currentTimeMillis()));

		return childLot;
	}

	public void increaseMQCUsedCount(List<String> productNameList, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion)
			throws CustomException
	{
		if (productNameList.size() > 0)
		{
			StringBuilder sql = new StringBuilder();
			sql.append("UPDATE CT_MQCPLANDETAIL_EXTENDED ");
			sql.append("   SET DUMMYUSEDCOUNT = NVL (DUMMYUSEDCOUNT, 0) + 1 ");
			sql.append(" WHERE PROCESSFLOWNAME = :PROCESSFLOWNAME ");
			sql.append("   AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
			sql.append("   AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
			sql.append("   AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
			sql.append("   AND PRODUCTNAME IN ( :PRODUCTLIST) ");
			sql.append("   AND JOBNAME IN (SELECT C.JOBNAME ");
			sql.append("                     FROM CT_MQCPLAN C, ");
			sql.append("                          CT_MQCPLANDETAIL_EXTENDED E, ");
			sql.append("                          PRODUCT P ");
			sql.append("                    WHERE C.MQCSTATE = 'Released' ");
			sql.append("                      AND C.JOBNAME = E.JOBNAME ");
			sql.append("                      AND P.PRODUCTNAME = E.PRODUCTNAME ");
			sql.append("                      AND P.PRODUCTNAME IN ( :PRODUCTLIST)) ");

			Map<String, Object> args = new HashMap<String, Object>();
			args.put("PROCESSFLOWNAME", processFlowName);
			args.put("PROCESSFLOWVERSION", processFlowVersion);
			args.put("PROCESSOPERATIONNAME", processOperationName);
			args.put("PROCESSOPERATIONVERSION", processOperationVersion);
			args.put("PRODUCTLIST", productNameList);

			try
			{
				GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), args);
			}
			catch (Exception e)
			{
				log.info(" Error occured - update dummy used count for MQC ");
			}
		}
	}

	public void autoShipLotAction(EventInfo eventInfo, Lot LotData) throws CustomException
	{
		if (StringUtils.equals(LotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Released)
				&& StringUtils.equals(LotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_NotOnHold))
		{
			String productRequest=LotData.getProductRequestName();
			ProductRequest productRequestData=MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequest);//caixu 2020/07/02 AutoModify ProductRequest
			Map<String, String> autoLotudfs = productRequestData.getUdfs();
			ProcessOperationSpec processOperSpec = CommonUtil.getProcessOperationSpec(LotData.getFactoryName(), LotData.getProcessOperationName(), LotData.getProcessOperationVersion());
			String detailProcessOperationType = processOperSpec.getDetailProcessOperationType();

			if (detailProcessOperationType.equals("SHIP") || detailProcessOperationType.equals("TSPSHIP"))
			{
				if (autoLotudfs.get("AUTOSHIPPINGFLAG").equals("Y"))
				{
					if (LotData.getLotGrade().equals("G"))
					{
						String destFactoryname = getDestFactoryName(LotData);

						if (StringUtils.isNotEmpty(destFactoryname))
						{
							executeAutoShip(eventInfo, LotData, destFactoryname);
						}
						else
						{
							log.info(" Skip Auto Shipping because Destination Factory is not only 1 ");
						}

					}
					else
					{
						log.info(" LotGrade is not G ");
					}
				}
			}
		}
	}

	public void executeAutoShip(EventInfo eventInfo, Lot lotData, String destFactoryName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		String lotName = lotData.getKey().getLotName();
		List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUSequence(lotName);

		// Set OldProductRequestName for CancelReceive
		MakeShippedInfo makeShippedInfo = MESLotServiceProxy.getLotInfoUtil().makeShippedInfo(lotData, lotData.getAreaName(), "", destFactoryName, productUdfs);
		makeShippedInfo.getUdfs().put("OLDPRODUCTREQUESTNAME", lotData.getProductRequestName());
		
		eventInfo.setEventName("Ship");
		MESLotServiceProxy.getLotServiceImpl().shipLot(eventInfo, lotData, makeShippedInfo);

		String productRequestName = lotData.getProductRequestName();

		ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);

		IncrementFinishedQuantityByInfo incrementFinishedQuantityByInfo = new IncrementFinishedQuantityByInfo();
		incrementFinishedQuantityByInfo.setQuantity((long) lotData.getProductQuantity());

		// Increment Work Order Finished Quantity
		eventInfo.setEventName("IncreamentQuantity");
		productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementFinishedQuantityBy(productRequestData, incrementFinishedQuantityByInfo, eventInfo);

		//modify by wangys 2020/11/26 Cancel Auto CompleteWO 
		/*if (productRequestData.getPlanQuantity() == productRequestData.getFinishedQuantity() + productRequestData.getScrappedQuantity())
		{
			MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompleted(eventInfo, lotData.getProductRequestName());
		}*/
	}

	public String getDestFactoryName(Lot afterTrackOutLot) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT DISTINCT PF.TOFACTORYNAME AS DESTINATIONFACTORYNAME ");
		sql.append("  FROM TPPOLICY T, POSFACTORYRELATION PF ");
		sql.append(" WHERE T.CONDITIONID = PF.CONDITIONID ");
		sql.append("   AND T.FACTORYNAME = :SOURCEFACTORYNAME ");
		sql.append("   AND T.PRODUCTSPECNAME = :SOURCEPRODUCTSPECNAME ");
		sql.append("   AND PF.SHIPUNIT = :SHIPUNIT ");

		Map<String, String> inquirybindMap = new HashMap<String, String>();
		inquirybindMap.put("SOURCEFACTORYNAME", afterTrackOutLot.getFactoryName());
		inquirybindMap.put("SOURCEPRODUCTSPECNAME", afterTrackOutLot.getProductSpecName());
		inquirybindMap.put("SHIPUNIT", "Lot");

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), inquirybindMap);

		if (sqlResult.size() == 0)
		{
			throw new CustomException("SYS-1151");
		}
		else if (sqlResult.size() > 1)
		{
			return "";
		}

		String destFactoryName = CommonUtil.getValue(sqlResult.get(0), "DESTINATIONFACTORYNAME");

		return destFactoryName;
	}

	public void reserveFirstGlass(EventInfo eventInfo, FirstGlassJob jobData, Lot lotData, List<Element> flowList) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{

		String toProcessFlowType = jobData.getToProcessFlowType(); // Sampling or FirstGlass

		String lotName = lotData.getKey().getLotName();
		String machineName = jobData.getMachineName();
		String fromProcessFlowName = jobData.getProcessFlowName();
		String fromProcessFlowVersion = jobData.getProcessFlowVersion();
		String fromProcessOperationName = jobData.getProcessOperationName();
		String fromProcessOperationVersion = jobData.getProcessOperationVersion();

		String returnProcessFlowName = jobData.getReturnProcessFlowName();
		String returnProcessFlowVersion = jobData.getReturnProcessFlowVersion();
		String returnProcessOperationName = jobData.getReturnProcessOperationName();
		String returnProcessOperationVersion = jobData.getReturnProcessOperationVersion();

		MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
		
		if (flowList.size() < 1)
		{
			//FIRSTGLASS-0020: FirstGlass Flow doesn't exist
			throw new CustomException("FIRSTGLASS-0020");
		}

		for (Element flowMap : flowList)
		{
			boolean dryEtchFlag = false;
			String toProcessFlowName = SMessageUtil.getChildText(flowMap, "TOPROCESSFLOWNAME", false);
			String toProcessFlowVersion = SMessageUtil.getChildText(flowMap, "TOPROCESSFLOWVERSION", false);
			machineName = SMessageUtil.getChildText(flowMap, "MACHINENAME", false);

			// FirstGlassFlow exist only one about Operation. but SampleFlow has over one.
			int flowPriority = Integer.parseInt((StringUtil.equals(toProcessFlowType, "FirstGlass")) ? "1" : SMessageUtil.getChildText(flowMap, "FLOWPRIORITY", false));

			if(StringUtils.equals(machineSpecData.getMachineGroupName(), "ADryEtch") || StringUtils.equals(machineSpecData.getMachineGroupName(), "DryEtch")
					|| StringUtils.equals(machineSpecData.getMachineGroupName(), "Sputter"))
			{
				boolean isFirstGlassFlow = MESLotServiceProxy.getLotServiceUtil().isSampleFlow(machineSpecData, lotData.getFactoryName(), toProcessFlowName, toProcessFlowVersion);
				
				if (!isFirstGlassFlow)
				{
					throw new CustomException("FIRSTGLASS-0003", toProcessFlowName);
				}
				
				flowPriority = Integer.parseInt(SMessageUtil.getChildText(flowMap, "FLOWPRIORITY", false));
				dryEtchFlag = true;
			}
			else
			{
				boolean isFirstGlassFlow = MESLotServiceProxy.getLotServiceUtil().isSampleFlow(lotData.getFactoryName(), toProcessFlowName, toProcessFlowVersion);
				
				if (!isFirstGlassFlow)
				{
					throw new CustomException("FIRSTGLASS-0003", toProcessFlowName);
				}
			}

			if(dryEtchFlag)
			{
				String toProcessOperationName = SMessageUtil.getChildText(flowMap, "TOPROCESSOPERATIONNAME", false);
				String toProcessOperationVersion = SMessageUtil.getChildText(flowMap, "TOPROCESSOPERATIONVERSION", false);
				
				// 1. set Sampling Lot Data
				// get SampleLotData(CT_SAMPLELOT)
				List<SampleLot> sampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataList(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
						lotData.getProductSpecVersion(), fromProcessFlowName, fromProcessFlowVersion, fromProcessOperationName, fromProcessOperationVersion, machineName, toProcessFlowName,
						toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);

				// if alreay exist, remove sampling Lot in CT_SAMPLELOT, CT_SAMPLEPRODUCT Table
				if (sampleLot != null)
				{
					// if exist, delete previous SamplingProduct Data(CT_SAMPLEPRODUCT)
					ExtendedObjectProxy.getSampleProductService().deleteSampleProductByLotName(eventInfo, lotName, lotData.getFactoryName(), lotData.getProductSpecName(),
							lotData.getProductSpecVersion(), fromProcessFlowName, fromProcessFlowVersion, fromProcessOperationName, fromProcessOperationVersion, machineName, toProcessFlowName,
							toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);

					ExtendedObjectProxy.getSampleLotService().deleteSampleLotWithOutMachineName(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
							lotData.getProductSpecVersion(), fromProcessFlowName, fromProcessFlowVersion, fromProcessOperationName, fromProcessOperationVersion, toProcessFlowName,
							toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);
				}

				// Insert sampling Lot Data in CT_SAMPLELOT, CT_SAMPLEPRODUCT Table
				List<Product> allUnScrappedProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);
				List<String> actualSamplePositionList = new ArrayList<String>();
				for (Product unScrappedProductE : allUnScrappedProductList)
				{
					// Validation
					if (!unScrappedProductE.getLotName().equals(lotName))
						throw new CustomException("LOT-0014", lotName, unScrappedProductE.getLotName(), unScrappedProductE.getKey().getProductName());

					actualSamplePositionList.add(String.valueOf(unScrappedProductE.getPosition()));

				}

				// insert SampleProduct & make actualSamplePositionList
				for (Product product : allUnScrappedProductList)
				{
					// set SamplingProduct Data(CT_SAMPLEPRODUCT)
					ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, product.getKey().getProductName(), lotName, lotData.getFactoryName(), lotData.getProductSpecName(),
							lotData.getProductSpecVersion(), fromProcessFlowName, fromProcessFlowVersion, fromProcessOperationName, fromProcessOperationVersion, machineName, toProcessFlowName,
							toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion, "Y", String.valueOf(actualSamplePositionList.size()),
							CommonUtil.toStringWithoutBrackets(actualSamplePositionList), String.valueOf(product.getPosition()), "", "Y");
				}

				// set SampleLotData(CT_SAMPLELOT)
				ExtendedObjectProxy.getSampleLotService().insertSampleLot(eventInfo, lotName, lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(),
						fromProcessFlowName, fromProcessFlowVersion, fromProcessOperationName, fromProcessOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion,
						toProcessOperationName, toProcessOperationVersion, "Y", "1", "1", "1", "1", CommonUtil.toStringWithoutBrackets(actualSamplePositionList),
						String.valueOf(actualSamplePositionList.size()), CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "Y", "", flowPriority, returnProcessFlowName,
						returnProcessFlowVersion, returnProcessOperationName, returnProcessOperationVersion, "");
			}
			else
			{
				List<Map<String, Object>> operationList = MESLotServiceProxy.getLotServiceUtil().operationList(lotData.getFactoryName(), toProcessFlowName, toProcessFlowVersion);

				for (Map<String, Object> operMap : operationList)
				{
					String toProcessOperationName = ConvertUtil.getMapValueByName(operMap, "PROCESSOPERATIONNAME");
					String toProcessOperationVersion = ConvertUtil.getMapValueByName(operMap, "PROCESSOPERATIONVERSION");

					// 1. set Sampling Lot Data
					// get SampleLotData(CT_SAMPLELOT)
					List<SampleLot> sampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataList(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
							lotData.getProductSpecVersion(), fromProcessFlowName, fromProcessFlowVersion, fromProcessOperationName, fromProcessOperationVersion, machineName, toProcessFlowName,
							toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);

					// if alreay exist, remove sampling Lot in CT_SAMPLELOT, CT_SAMPLEPRODUCT Table
					if (sampleLot != null)
					{
						// if exist, delete previous SamplingProduct Data(CT_SAMPLEPRODUCT)
						ExtendedObjectProxy.getSampleProductService().deleteSampleProductByLotName(eventInfo, lotName, lotData.getFactoryName(), lotData.getProductSpecName(),
								lotData.getProductSpecVersion(), fromProcessFlowName, fromProcessFlowVersion, fromProcessOperationName, fromProcessOperationVersion, machineName, toProcessFlowName,
								toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);

						ExtendedObjectProxy.getSampleLotService().deleteSampleLotWithOutMachineName(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
								lotData.getProductSpecVersion(), fromProcessFlowName, fromProcessFlowVersion, fromProcessOperationName, fromProcessOperationVersion, toProcessFlowName,
								toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);
					}

					// Insert sampling Lot Data in CT_SAMPLELOT, CT_SAMPLEPRODUCT Table
					List<Product> allUnScrappedProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);
					List<String> actualSamplePositionList = new ArrayList<String>();
					for (Product unScrappedProductE : allUnScrappedProductList)
					{
						// Validation
						if (!unScrappedProductE.getLotName().equals(lotName))
							throw new CustomException("LOT-0014", lotName, unScrappedProductE.getLotName(), unScrappedProductE.getKey().getProductName());

						actualSamplePositionList.add(String.valueOf(unScrappedProductE.getPosition()));

					}

					// insert SampleProduct & make actualSamplePositionList
					for (Product product : allUnScrappedProductList)
					{
						// set SamplingProduct Data(CT_SAMPLEPRODUCT)
						ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, product.getKey().getProductName(), lotName, lotData.getFactoryName(), lotData.getProductSpecName(),
								lotData.getProductSpecVersion(), fromProcessFlowName, fromProcessFlowVersion, fromProcessOperationName, fromProcessOperationVersion, machineName, toProcessFlowName,
								toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion, "Y", String.valueOf(actualSamplePositionList.size()),
								CommonUtil.toStringWithoutBrackets(actualSamplePositionList), String.valueOf(product.getPosition()), "", "Y");
					}

					// set SampleLotData(CT_SAMPLELOT)
					ExtendedObjectProxy.getSampleLotService().insertSampleLot(eventInfo, lotName, lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(),
							fromProcessFlowName, fromProcessFlowVersion, fromProcessOperationName, fromProcessOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion,
							toProcessOperationName, toProcessOperationVersion, "Y", "1", "1", "1", "1", CommonUtil.toStringWithoutBrackets(actualSamplePositionList),
							String.valueOf(actualSamplePositionList.size()), CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "Y", "", flowPriority, returnProcessFlowName,
							returnProcessFlowVersion, returnProcessOperationName, returnProcessOperationVersion, "");
				}
			}
		}
	}

	public void deleteSampleFirstGlass(EventInfo eventInfo, String jobName, Lot lotData) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		FirstGlassJob jobData = ExtendedObjectProxy.getFirstGlassJobService().selectByKey(false, new Object[] { jobName });
		String toProcessFlowType = jobData.getToProcessFlowType(); // Sampling or FirstGlass

		String lotName = lotData.getKey().getLotName();
		String machineName = jobData.getMachineName();
		String fromProcessFlowName = jobData.getProcessFlowName();
		String fromProcessFlowVersion = jobData.getProcessFlowVersion();
		String fromProcessOperationName = jobData.getProcessOperationName();
		String fromProcessOperationVersion = jobData.getProcessOperationVersion();

		List<Map<String, Object>> flowList = new ArrayList<Map<String, Object>>();

		if (StringUtils.equals(toProcessFlowType, "Sampling"))
		{
			flowList = MESLotServiceProxy.getLotServiceUtil().getSamplePolicyList(lotData.getFactoryName(), fromProcessFlowName, fromProcessFlowVersion, fromProcessOperationName,
					fromProcessOperationVersion, machineName, "", "", "", "");
		}
		else if (StringUtils.equals(toProcessFlowType, "FirstGlass"))
		{
			flowList = MESLotServiceProxy.getLotServiceUtil().getFirstGlassflowList(lotData.getFactoryName(), fromProcessFlowName, fromProcessFlowVersion, fromProcessOperationName,
					fromProcessOperationVersion, machineName);

		}
		else
		{
			//FIRSTGLASS-0021:FirstGlass Job : toProcessFlowType is null
			throw new CustomException("FIRSTGLASS-0021");
		}

		if (flowList.size() < 1)
		{
			//FIRSTGLASS-0020: FirstGlass Flow doesn't exist
			throw new CustomException("FIRSTGLASS-0020");
		}

		for (Map<String, Object> flowMap : flowList)
		{
			String toProcessFlowName = ConvertUtil.getMapValueByName(flowMap, "TOPROCESSFLOWNAME");
			String toProcessFlowVersion = ConvertUtil.getMapValueByName(flowMap, "TOPROCESSFLOWVERSION");
			machineName = ConvertUtil.getMapValueByName(flowMap, "MACHINENAME");

			// FirstGlassFlow exist only one about Operation. but SampleFlow has over one.
			boolean isFirstGlassFlow = MESLotServiceProxy.getLotServiceUtil().isSampleFlow(lotData.getFactoryName(), toProcessFlowName, toProcessFlowVersion);

			if (!isFirstGlassFlow)
			{
				throw new CustomException("FIRSTGLASS-0003", toProcessFlowName);
			}

			List<Map<String, Object>> operationList = MESLotServiceProxy.getLotServiceUtil().operationList(lotData.getFactoryName(), toProcessFlowName, toProcessFlowVersion);

			for (Map<String, Object> operMap : operationList)
			{
				String toProcessOperationName = ConvertUtil.getMapValueByName(operMap, "PROCESSOPERATIONNAME");
				String toProcessOperationVersion = ConvertUtil.getMapValueByName(operMap, "PROCESSOPERATIONVERSION");

				// 1. set Sampling Lot Data
				// get SampleLotData(CT_SAMPLELOT)
				List<SampleLot> sampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataList(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
						lotData.getProductSpecVersion(), fromProcessFlowName, fromProcessFlowVersion, fromProcessOperationName, fromProcessOperationVersion, machineName, toProcessFlowName,
						toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);

				// if alreay exist, remove sampling Lot in CT_SAMPLELOT, CT_SAMPLEPRODUCT Table
				if (sampleLot != null)
				{
					// if exist, delete previous SamplingProduct Data(CT_SAMPLEPRODUCT)
					ExtendedObjectProxy.getSampleProductService().deleteSampleProductByLotName(eventInfo, lotName, lotData.getFactoryName(), lotData.getProductSpecName(),
							lotData.getProductSpecVersion(), fromProcessFlowName, fromProcessFlowVersion, fromProcessOperationName, fromProcessOperationVersion, machineName, toProcessFlowName,
							toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);

					ExtendedObjectProxy.getSampleLotService().deleteSampleLotWithOutMachineName(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
							lotData.getProductSpecVersion(), fromProcessFlowName, fromProcessFlowVersion, fromProcessOperationName, fromProcessOperationVersion, toProcessFlowName,
							toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);
				}
			}
		}
	}

	public List<Map<String, Object>> getFirstGlassflowList(String factoryName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion,
			String machineName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		StringBuilder sql = new StringBuilder();

		sql.append("SELECT P.TOPROCESSFLOWNAME, ");
		sql.append("	P.TOPROCESSFLOWVERSION, ");
		sql.append("	P.TOPROCESSOPERATIONNAME, ");
		sql.append("	P.TOPROCESSOPERATIONVERSION, ");
		sql.append("	T.MACHINENAME  ");
		sql.append("FROM TFOMPOLICY T, POSFIRSTGLASS P ");
		sql.append("WHERE T.CONDITIONID = P.CONDITIONID ");
		sql.append("	AND T.FACTORYNAME = :FACTORYNAME ");
		sql.append("	AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("	AND T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("	AND T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("	AND T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		sql.append("	AND T.MACHINENAME = :MACHINENAME ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", factoryName);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);
		args.put("PROCESSOPERATIONNAME", processOperationName);
		args.put("PROCESSOPERATIONVERSION", processOperationVersion);
		args.put("MACHINENAME", machineName);

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		return result;
	}

	public String getProcessFlowTypeByNodeId(String nodeId) throws CustomException
	{
		String processFlowType = "";
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT P.PROCESSFLOWTYPE ");
		sql.append("  FROM PROCESSFLOW P, NODE N ");
		sql.append(" WHERE P.FACTORYNAME = N.FACTORYNAME ");
		sql.append("   AND P.PROCESSFLOWNAME = N.PROCESSFLOWNAME ");
		sql.append("   AND P.PROCESSFLOWVERSION = N.PROCESSFLOWVERSION ");
		sql.append("   AND N.NODEID = :NODEID ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("NODEID", nodeId);

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			processFlowType = ConvertUtil.getMapValueByName(result.get(0), "PROCESSFLOWTYPE");
		}

		return processFlowType;
	}

	public void setRecipeChangeInfo(String machineName, List<String> productRecipeList, EventInfo eventInfo) throws CustomException
	{
		Recipe recipeData = new Recipe();

		HashSet<String> hashSetRecipeList = new HashSet<>();
		for (int i = 0; i < productRecipeList.size(); i++)
		{
			hashSetRecipeList.add(productRecipeList.get(i));
		}

		for (String pRecipeListElement : hashSetRecipeList)
		{

			String recipeName = pRecipeListElement;
			try
			{
				recipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] { machineName, recipeName });
				if (recipeData.getAutoChangeFlag().equals("INTENG") || recipeData.getAutoChangeFlag().equals("ENGINT"))
				{
					log.info("UpdateTrackOutTime!");
					eventInfo.setEventName("UpdateTrackOutTime");
					eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

					recipeData.setLastTrackOutTimeKey(eventInfo.getEventTimeKey());
					recipeData.setLastEventName(eventInfo.getEventName());
					recipeData.setLastEventComment(eventInfo.getEventComment());
					recipeData.setLastEventName(eventInfo.getEventName());
					recipeData.setLastEventTimeKey(eventInfo.getEventTimeKey());
					recipeData.setLastEventUser(eventInfo.getEventUser());
					ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipeData);
				}
				else
				{
					log.info("PPID AutoChangeFlag = N !");
					continue;
				}

			}
			catch (Exception e)
			{
				log.info("PPID had not sync!");
				continue;
			}
		}
	}

	public void setProcessingMainMachine(String lotName) throws CustomException
	{
		String condition = "UPDATE PRODUCT SET MAINMACHINENAME = MACHINENAME WHERE lotName = ?";
		Object[] bindSet = new Object[] { lotName };
		GenericServiceProxy.getSqlMesTemplate().update(condition, bindSet);
	}

	public List<Lot> getLotListOnTheEQP(String factoryName, String machineName) throws CustomException
	{
		List<Lot> lotList = new ArrayList<Lot>();
		try
		{
			lotList = LotServiceProxy.getLotService().select(" FACTORYNAME = :FACTORYNAME AND LOTSTATE = :LOTSTATE AND LOTPROCESSSTATE = :LOTPROCESSSTATE AND MACHINENAME = :MACHINENAME ",
					new Object[] { factoryName, GenericServiceProxy.getConstantMap().Lot_Released, GenericServiceProxy.getConstantMap().Lot_LoggedIn, machineName });
		}
		catch (Exception e)
		{
			log.info("No lot exist on the EQP: " + machineName);
		}

		return lotList;
	}

	public Lot mergeFirstGlassChildLot(EventInfo eventInfo, FirstGlassJob jobData, Lot lotData, boolean checkAllSplitChildLot) throws CustomException
	{
		log.info("Start mergeFirstGlassChildLot");
		String jobName = jobData.getJobName();

		List<Lot> childLotDataList = new ArrayList<Lot>();
		try
		{// Find final Completed child lot
			childLotDataList = LotServiceProxy.getLotService().select(" JOBNAME = ? AND FIRSTGLASSFLAG = ? AND PRODUCTQUANTITY > 0 AND LOTSTATE <> 'Emptied'", new Object[] { jobName, "Y" });
		}
		catch (Exception e)
		{
		}

		// if final Completed child lot exist, merge
		if (childLotDataList.size() > 0)
		{
			Lot childLotData = childLotDataList.get(0);
			String prevChildLotName = childLotData.getKey().getLotName();

			if (StringUtils.equals(jobData.getReturnProcessFlowName(), lotData.getProcessFlowName()) && StringUtils.equals(jobData.getReturnProcessFlowVersion(), lotData.getProcessFlowVersion())
					&& StringUtils.equals(jobData.getReturnProcessOperationName(), lotData.getProcessOperationName())
					&& StringUtils.equals(jobData.getReturnProcessOperationVersion(), lotData.getProcessOperationVersion())
					&& StringUtils.equals(lotData.getProcessFlowName(), childLotData.getProcessFlowName()) && StringUtils.equals(lotData.getProcessFlowVersion(), childLotData.getProcessFlowVersion())
					&& StringUtils.equals(lotData.getProcessOperationName(), childLotData.getProcessOperationName())
					&& StringUtils.equals(lotData.getProcessOperationVersion(), childLotData.getProcessOperationVersion()))
			{

				List<Product> productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(prevChildLotName);

				List<ProductP> productPSequence = new ArrayList<ProductP>();
				for (Product productData : productList)
				{
					ProductP productP = new ProductP();
					productP.setProductName(productData.getKey().getProductName());
					productP.setPosition(productData.getPosition());
					productP.setUdfs(productData.getUdfs());
					productPSequence.add(productP);

				}

				// Release Final Completed Child Lot
				if (StringUtils.equals(childLotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_OnHold))
				{
					eventInfo.setEventName("ReleaseHold");

					Map<String, String> udfs = new HashMap<String, String>();
					List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(childLotData);
					MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(childLotData, productUSequence, udfs);
					childLotData = LotServiceProxy.getLotService().makeNotOnHold(childLotData.getKey(), eventInfo, makeNotOnHoldInfo);

					// delete in LOTMULTIHOLD table
					releaseMultiHold(prevChildLotName, "FirstGlassHold", childLotData.getProcessOperationName());

					// delete in PRODUCTMULTIHOLD table
					MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(prevChildLotName, "FirstGlassHold", childLotData.getProcessOperationName());

					// setHoldState
					setHoldState(eventInfo, childLotData);
				}

				// Release Lot
				if (StringUtils.equals(lotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_OnHold))
				{
					eventInfo.setEventName("ReleaseHold");

					Map<String, String> udfs = new HashMap<String, String>();
					List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
					MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(lotData, productUSequence, udfs);
					lotData = LotServiceProxy.getLotService().makeNotOnHold(lotData.getKey(), eventInfo, makeNotOnHoldInfo);

					// delete in LOTMULTIHOLD table
					releaseMultiHold(lotData.getKey().getLotName(), "FirstGlassHold", lotData.getProcessOperationName());

					// delete in PRODUCTMULTIHOLD table
					MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(lotData.getKey().getLotName(), "FirstGlassHold", lotData.getProcessOperationName());

					// setHoldState
					setHoldState(eventInfo, lotData);
				}

				// Merge Lot
				TransferProductsToLotInfo transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(lotData.getKey().getLotName(), productList.size(), productPSequence,
						lotData.getUdfs(), new HashMap<String, String>());
				eventInfo.setEventName("Merge");
				childLotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, childLotData, transitionInfo);

				Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();
				if (StringUtils.isNotEmpty(childLotData.getCarrierName()))
				{
					Durable sLotDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(childLotData.getCarrierName());
					deassignCarrierUdfs = sLotDurableData.getUdfs();
					DeassignCarrierInfo deassignCarrierInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(childLotData, sLotDurableData, new ArrayList<ProductU>());
					// Deassign Carrier
					eventInfo.setEventName("DeassignCarrier");
					MESLotServiceProxy.getLotServiceImpl().deassignCarrier(childLotData, deassignCarrierInfo, eventInfo);
				}

				eventInfo.setEventName("MakeEmptied");
				MESLotServiceProxy.getLotServiceImpl().MakeEmptied(eventInfo, childLotData, new ArrayList<ProductU>(), deassignCarrierUdfs);
				deassignCarrierUdfs.clear();

				// Complete job and remove JobName at lot if Lot is satisfied conditions below
				// 1. This Lot is MotherLot.
				// 2. Not exist Mother & All ChildLot are split.
				if ((StringUtils.isNotEmpty(lotData.getUdfs().get("JOBNAME")) && StringUtils.isEmpty(lotData.getUdfs().get("FIRSTGLASSFLAG"))) || checkAllSplitChildLot)
				{
					String sql = "UPDATE LOT SET JOBNAME = '' WHERE JOBNAME = :JOBNAME ";

					Map<String, Object> bindMap = new HashMap<String, Object>();
					bindMap.put("JOBNAME", jobData.getJobName());
					kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);

					try
					{
						// Delete Job Name
						kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();

						Map<String, String> udfs = new HashMap<>();
						udfs.put("JOBNAME", "");
						udfs.put("FIRSTGLASSFLAG", "");
						setEventInfo.setUdfs(udfs);
						eventInfo.setEventName("CompleteFirstGlassJob");
						lotData = LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
					}
					catch (Exception e)
					{
						log.info("Error Occurred - SetEvent.CompleteFirstGlassJob: " + lotData.getKey().getLotName());
					}

					// Update Job State
					eventInfo.setEventName("Complete");
					jobData.setJobState("Completed");
					jobData.setLastEventComment(eventInfo.getEventComment());
					jobData.setLastEventTime(eventInfo.getEventTime());
					jobData.setLastEventTimekey(eventInfo.getEventTimeKey());
					jobData.setLastEventUser(eventInfo.getEventUser());
					ExtendedObjectProxy.getFirstGlassJobService().modify(eventInfo, jobData);

					// Check Grade.
					List<Product> mLotProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());
					boolean isExistGrade = false;
					String lotGrade = "";
					for (Product productData : mLotProductList)
					{
						if (StringUtils.equals(productData.getProductGrade(), "R"))
						{
							isExistGrade = true;
							lotGrade = "R";
							break;
						}
					}

					if (isExistGrade)
					{
						eventInfo.setEventName("ChangeGrade");
						List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();

						ChangeGradeInfo changeGradeInfo = MESLotServiceProxy.getLotInfoUtil().changeGradeInfo(lotData, lotGrade, productPGSSequence);

						lotData = MESLotServiceProxy.getLotServiceImpl().ChangeGrade(eventInfo, lotData, changeGradeInfo);
					}
				}
			}
			else
			{
				//LOT-0052: Can't Merge because Job's Return Info is not same Lot's Info
				throw new CustomException("LOT-0052");
			}
		}

		lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());

		return lotData;

	}

	public Lot mergeFirstGlassMotherLot(EventInfo eventInfo, FirstGlassJob jobData, Lot lotData, boolean checkAllSplitChildLot) throws CustomException
	{
		String jobName = jobData.getJobName();

		List<Lot> mLotDataList = new ArrayList<Lot>();
		try
		{
			// Find final Completed child lot
			mLotDataList = LotServiceProxy.getLotService().select(" JOBNAME = ? AND FIRSTGLASSFLAG is null AND PRODUCTQUANTITY > 0 AND LOTSTATE <> 'Emptied'", new Object[] { jobName });
		}
		catch (Exception e)
		{
			return lotData;
		}

		List<Lot> childLotDataList = new ArrayList<Lot>();
		try
		{
			// Find final Completed child lot
			childLotDataList = LotServiceProxy.getLotService().select(" JOBNAME = ? AND FIRSTGLASSFLAG = ? AND LOTNAME <> ? AND PRODUCTQUANTITY > 0 AND LOTSTATE <> 'Emptied'",
					new Object[] { jobName, "Y", lotData.getKey().getLotName() });
		}
		catch (Exception e)
		{
		}

		// if final Completed child lot exist, merge
		if (mLotDataList.size() > 0)
		{
			Lot mLotData = mLotDataList.get(0);
			String prevMotherLotName = mLotData.getKey().getLotName();
			String stripLotName = lotData.getKey().getLotName();

			if (StringUtils.equals(jobData.getProcessFlowName(), lotData.getProcessFlowName()) && StringUtils.equals(jobData.getProcessFlowVersion(), lotData.getProcessFlowVersion())
					&& StringUtils.equals(jobData.getProcessOperationName(), lotData.getProcessOperationName())
					&& StringUtils.equals(jobData.getProcessOperationVersion(), lotData.getProcessOperationVersion()) && StringUtils.equals(lotData.getProcessFlowName(), mLotData.getProcessFlowName())
					&& StringUtils.equals(lotData.getProcessFlowVersion(), mLotData.getProcessFlowVersion()) && StringUtils.equals(lotData.getProcessOperationName(), mLotData.getProcessOperationName())
					&& StringUtils.equals(lotData.getProcessOperationVersion(), mLotData.getProcessOperationVersion()))
			{

				List<Product> productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(stripLotName);

				List<ProductP> productPSequence = new ArrayList<ProductP>();
				for (Product productData : productList)
				{
					ProductP productP = new ProductP();
					productP.setProductName(productData.getKey().getProductName());
					productP.setPosition(productData.getPosition());
					productP.setUdfs(productData.getUdfs());
					productPSequence.add(productP);
				}

				// Release Mother Lot
				if (StringUtils.equals(mLotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_OnHold))
				{
					eventInfo.setEventName("ReleaseHold");

					Map<String, String> udfs = new HashMap<String, String>();
					List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(mLotData);
					MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(mLotData, productUSequence, udfs);
					mLotData = LotServiceProxy.getLotService().makeNotOnHold(mLotData.getKey(), eventInfo, makeNotOnHoldInfo);

					// delete in LOTMULTIHOLD table
					releaseMultiHold(prevMotherLotName, "FirstGlassHold", mLotData.getProcessOperationName());

					// delete in PRODUCTMULTIHOLD table
					MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(prevMotherLotName, "FirstGlassHold", mLotData.getProcessOperationName());

					// setHoldState
					setHoldState(eventInfo, mLotData);
				}

				// Merge Lot
				TransferProductsToLotInfo transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(prevMotherLotName, productList.size(), productPSequence, mLotData.getUdfs(),
						new HashMap<String, String>());
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				eventInfo.setEventName("Merge");
				lotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotData, transitionInfo);

				// Completed FirstGlass.
				this.updateStripLotInformation(eventInfo, lotData);

				if (childLotDataList.size() == 0)
				{
					// Complete job and remove JobName at lot if Lot is satisfied conditions below
					// 1. This Lot is MotherLot.
					// 2. Not exist Mother & All ChildLot are split.
					if ((StringUtils.isNotEmpty(mLotData.getUdfs().get("JOBNAME")) && StringUtils.isEmpty(mLotData.getUdfs().get("FIRSTGLASSFLAG"))))
					{
						String sql = "UPDATE LOT SET JOBNAME = '' WHERE JOBNAME = :JOBNAME ";

						Map<String, Object> bindMap = new HashMap<String, Object>();
						bindMap.put("JOBNAME", jobData.getJobName());
						kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);

						try
						{
							// Delete Job Name
							kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();

							Map<String, String> udfs = new HashMap<>();
							udfs.put("JOBNAME", "");
							udfs.put("FIRSTGLASSFLAG", "");
							setEventInfo.setUdfs(udfs);
							eventInfo.setEventName("CompleteFirstGlassJob");
							mLotData = LotServiceProxy.getLotService().setEvent(mLotData.getKey(), eventInfo, setEventInfo);
						}
						catch (Exception e)
						{
							log.info("Error Occurred - SetEvent.CompleteFirstGlassJob: " + mLotData.getKey().getLotName());
						}

						// Update Job State
						eventInfo.setEventName("Complete");
						jobData.setJobState("Completed");
						jobData.setLastEventComment(eventInfo.getEventComment());
						jobData.setLastEventTime(eventInfo.getEventTime());
						jobData.setLastEventTimekey(eventInfo.getEventTimeKey());
						jobData.setLastEventUser(eventInfo.getEventUser());
						ExtendedObjectProxy.getFirstGlassJobService().modify(eventInfo, jobData);

						// Check Grade.
						if (!StringUtils.equals(mLotData.getLotGrade(), "G"))
						{
							eventInfo.setEventName("ChangeGrade");
							List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();

							ChangeGradeInfo changeGradeInfo = MESLotServiceProxy.getLotInfoUtil().changeGradeInfo(mLotData, "G", productPGSSequence);

							mLotData = MESLotServiceProxy.getLotServiceImpl().ChangeGrade(eventInfo, mLotData, changeGradeInfo);
						}
					}
				}

				eventInfo.setReasonCodeType("StripHOLD");
				eventInfo.setReasonCode("StripHOLD");
				eventInfo.setEventComment("FirstGlassHold:After Strip,Please call Photo Enginner(8364)");

				// LotMultiHold
				MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, mLotData, new HashMap<String, String>());
			}
			else
			{
				//LOT-0052: Can't Merge because Job's Return Info is not same Lot's Info
				throw new CustomException("LOT-0052");
			}
		}

		lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());

		return lotData;

	}

	public void updateStripLotInformation(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		eventInfo.setEventName("ChangeFirstGlassFlag");

		// Set Event
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("FIRSTGLASSFLAG", "Y");

		// 21-03-23 Modify by jhyeom (Return lotData)
		lotData = LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);

		Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();
		if (StringUtils.isNotEmpty(lotData.getCarrierName()))
		{
			Durable sLotDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());
			deassignCarrierUdfs = sLotDurableData.getUdfs();
			DeassignCarrierInfo deassignCarrierInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, sLotDurableData, new ArrayList<ProductU>());
			// Deassign Carrier
			eventInfo.setEventName("DeassignCarrier");
			MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, deassignCarrierInfo, eventInfo);
		}

		eventInfo.setEventName("MakeEmptied");
		MESLotServiceProxy.getLotServiceImpl().MakeEmptied(eventInfo, lotData, new ArrayList<ProductU>(), deassignCarrierUdfs);
		deassignCarrierUdfs.clear();
	}

	public void releaseMultiHold(String lotName, String reasonCode, String processOperationName) throws CustomException
	{
		try
		{
			LotServiceProxy.getLotMultiHoldService().delete(" lotname = ? and reasoncode = ? and processoperationname = ?", new Object[] { lotName, reasonCode, processOperationName });
		}
		catch (NotFoundSignal ne)
		{
			new CustomException("LOT-9999", lotName);
		}
		catch (FrameworkErrorSignal fe)
		{
			new CustomException("LOT-9999", fe.getMessage());
		}
	}

	public void setHoldState(EventInfo eventInfo, Lot lotData)
	{
		try
		{
			List<LotMultiHold> multiHoldList = LotServiceProxy.getLotMultiHoldService().select(" lotname = ? ", new Object[] { lotData.getKey().getLotName() });

			if (multiHoldList.size() > 0)
			{
				// Lot - LotHoldState
				lotData.setLotHoldState(GenericServiceProxy.getConstantMap().Lot_OnHold);
				LotServiceProxy.getLotService().update(lotData);

				// LotHistory - LotHoldState
				String sql = "UPDATE LOTHISTORY SET LOTHOLDSTATE = :LOTHOLDSTATE WHERE LOTNAME = :LOTNAME AND TIMEKEY = :TIMEKEY ";

				Map<String, String> args = new HashMap<String, String>();
				args.put("LOTHOLDSTATE", GenericServiceProxy.getConstantMap().Lot_OnHold);
				args.put("LOTNAME", lotData.getKey().getLotName());
				args.put("TIMEKEY", eventInfo.getEventTimeKey());

				GenericServiceProxy.getSqlMesTemplate().update(sql, args);

				// Product - ProductHoldState
				sql = "UPDATE PRODUCT SET PRODUCTHOLDSTATE = :PRODUCTHOLDSTATE WHERE LOTNAME = :LOTNAME ";

				args = new HashMap<String, String>();
				args.put("PRODUCTHOLDSTATE", GenericServiceProxy.getConstantMap().Prod_OnHold);
				args.put("LOTNAME", lotData.getKey().getLotName());
				args.put("TIMEKEY", eventInfo.getEventTimeKey());

				GenericServiceProxy.getSqlMesTemplate().update(sql, args);

				// ProductHistory - ProductHoldState
				sql = "UPDATE PRODUCTHISTORY SET PRODUCTHOLDSTATE = :PRODUCTHOLDSTATE WHERE PRODUCTNAME IN (SELECT PRODUCTNAME FROM PRODUCT WHERE LOTNAME = :LOTNAME) AND TIMEKEY = :TIMEKEY ";
				GenericServiceProxy.getSqlMesTemplate().update(sql, args);
			}
		}
		catch (Exception e)
		{
		}
	}

	public Lot excuteFirstGlass(EventInfo eventInfo, Lot beforeTrackOutLot, Lot afterTrackOutLot, String reworkFlag) throws CustomException
	{
		ProcessFlow beforeTrackOutPFData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(beforeTrackOutLot);

		FirstGlassJob jobData = null;
		try
		{
			Object[] bindSet = new Object[] { afterTrackOutLot.getUdfs().get("JOBNAME") };
			jobData = ExtendedObjectProxy.getFirstGlassJobService().selectByKey(false, bindSet);
		}
		catch (Exception ex)
		{
			log.info("This Lot is not FirstGlass");
			return afterTrackOutLot;
		}

		try
		{
			// If Mother Lot, Merge with ChildLot
			if (StringUtils.isNotEmpty(afterTrackOutLot.getUdfs().get("JOBNAME")) && StringUtils.isEmpty(afterTrackOutLot.getUdfs().get("FIRSTGLASSFLAG")))
			{
				afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().mergeFirstGlassChildLot(eventInfo, jobData, afterTrackOutLot, false);
			}

			// If Strip Lot, Merge with MotherLot
			if (StringUtils.isNotEmpty(afterTrackOutLot.getUdfs().get("JOBNAME")) && StringUtils.isNotEmpty(afterTrackOutLot.getUdfs().get("FIRSTGLASSFLAG"))
					&& StringUtils.equals(afterTrackOutLot.getUdfs().get("FIRSTGLASSFLAG"), "N") && StringUtils.equals(beforeTrackOutPFData.getProcessFlowType(), "Strip"))
			{
				afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().mergeFirstGlassMotherLot(eventInfo, jobData, afterTrackOutLot, false);
			}

			afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().getLotData(afterTrackOutLot.getKey().getLotName());
		}
		catch (Exception ex)
		{
			log.warn("excuteFirstGlass is failed");
		}

		return afterTrackOutLot;
	}

	public ArrayList<String> checkPhotoMask(Lot lotData, String machineName) throws CustomException
	{
		ArrayList<String> photoMaskList = new ArrayList<String>();
		
		int inUseCount = 0;
		
		String productionType = lotData.getProductionType();
		if (StringUtils.equals(productionType, "M") || StringUtils.equals(productionType, "D"))
		{
			return null;
		}

		List<ListOrderedMap> policyMask = PolicyUtil.getPhotoMaskPolicy(lotData, machineName);
		List<ListOrderedMap> assignedMaskToMachine = MESDurableServiceProxy.getDurableServiceUtil().getAssignedPhotoMaskList(machineName);

		boolean areTherePolicyOfAllMasks = false;
		for (ListOrderedMap assignedMask : assignedMaskToMachine)
		{
			String assignedMaskName = CommonUtil.getValue(assignedMask, "DURABLENAME");

			Durable assignedMaskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(assignedMaskName);

			if (!CommonUtil.equalsIn(assignedMaskData.getDurableState(), "Mounted", "InUse", "Prepare"))
			{
				throw new CustomException("MASK-0065", assignedMaskData.getDurableState(), assignedMaskName);
			}
			
			if(assignedMaskData.getDurableState().equals(GenericServiceProxy.getConstantMap().Dur_InUse))
			{
				inUseCount++;
			}
			
			// boolean isTherePolicyOfMask = false;
			if (policyMask.size() > 0)
			{
				if(lotData.getFactoryName().equals("ARRAY"))
				{
					if(policyMask.size() != 1)
					{
						throw new CustomException("MASK-0071", policyMask.size());
					}
				}
				else if(lotData.getFactoryName().equals("TP"))
				{
					if(policyMask.size() > 3)
					{
						throw new CustomException("MASK-0071", policyMask.size());
					}
				}
				
				if(lotData.getFactoryName().equals("ARRAY"))
				{
					if (!areTherePolicyOfAllMasks)
					{
						for (ListOrderedMap policy : policyMask)
						{
							String maskNameInPolicy = CommonUtil.getValue(policy, "MASKNAME");

							if (assignedMaskName.equals(maskNameInPolicy))
							{
								photoMaskList.add(assignedMaskName);
								//2020-01-15 Add
								if (!StringUtils.equals(assignedMaskData.getDurableCleanState(), GenericServiceProxy.getConstantMap().Dur_Clean))
								{
									throw new CustomException("MASK-0025", assignedMaskData);
								}
								if (StringUtils.equals(assignedMaskData.getUdfs().get("DURABLEHOLDSTATE"), "Y"))
								{
									throw new CustomException("MASK-0013", assignedMaskData.getKey().getDurableName());
								}
								String cleanUsedLimit = assignedMaskData.getUdfs().get("CLEANUSEDLIMIT").toString();
								String lastCleanTime = assignedMaskData.getUdfs().get("LASTCLEANTIME").toString();
								SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
								String currentTime = transFormat.format(new Date());
								Date lastCleanTimeDate = null;
								Date currentDate = null;
								try {
									lastCleanTimeDate = transFormat.parse(lastCleanTime);
									currentDate = transFormat.parse(currentTime);
								} catch (ParseException e) {
									e.printStackTrace();
								}
								//long gapTest = currentDate.getTime() - lastCleanTimeDate.getTime();
								double gap = (double)(currentDate.getTime() - lastCleanTimeDate.getTime()) / (double)(60 * 60 * 1000);
								
								if (gap >= Double.parseDouble(cleanUsedLimit)) 
								{
									throw new CustomException("MASK-0070", assignedMaskData.getKey().getDurableName());
								}
								
								// isTherePolicyOfMask = true;
								areTherePolicyOfAllMasks = true;
								
								break;
							}
						}
					}
				}
				else if(lotData.getFactoryName().equals("TP")) //for 3 mask send
				{
					for (ListOrderedMap policy : policyMask)
					{
						String maskNameInPolicy = CommonUtil.getValue(policy, "MASKNAME");

						if (assignedMaskName.equals(maskNameInPolicy))
						{
							photoMaskList.add(assignedMaskName);
							//2020-01-15 Add
							if (!StringUtils.equals(assignedMaskData.getDurableCleanState(), GenericServiceProxy.getConstantMap().Dur_Clean))
							{
								throw new CustomException("MASK-0025", assignedMaskData);
							}
							if (StringUtils.equals(assignedMaskData.getUdfs().get("DURABLEHOLDSTATE"), "Y"))
							{
								throw new CustomException("MASK-0013", assignedMaskData.getKey().getDurableName());
							}
							String cleanUsedLimit = assignedMaskData.getUdfs().get("CLEANUSEDLIMIT").toString();
							String lastCleanTime = assignedMaskData.getUdfs().get("LASTCLEANTIME").toString();
							SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							String currentTime = transFormat.format(new Date());
							Date lastCleanTimeDate = null;
							Date currentDate = null;
							try {
								lastCleanTimeDate = transFormat.parse(lastCleanTime);
								currentDate = transFormat.parse(currentTime);
							} catch (ParseException e) {
								e.printStackTrace();
							}
							//long gapTest = currentDate.getTime() - lastCleanTimeDate.getTime();
							double gap = (double)(currentDate.getTime() - lastCleanTimeDate.getTime()) / (double)(60 * 60 * 1000);
							
							if (gap >= Double.parseDouble(cleanUsedLimit)) 
							{
								throw new CustomException("MASK-0070", assignedMaskData.getKey().getDurableName());
							}
							
							// isTherePolicyOfMask = true;
							areTherePolicyOfAllMasks = true;
							
							break;
						}
					}
				}
				
				
			}
			else
			{
				throw new CustomException("MASK-0066", lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName);
			}
		}

		if (!areTherePolicyOfAllMasks)
			throw new CustomException("DURABLE-0005", machineName);
		/*
		if (inUseCount > 1)
		{
			throw new CustomException("MASK-0072", inUseCount);
		}
		*/
		return photoMaskList;
	}

	public void insertMaterialProductAll(EventInfo eventInfo, Machine eqpData, String lotName, List<String> productNameList) throws CustomException
	{
		try
		{
			List<Consumable> consumableDataList = ConsumableServiceProxy.getConsumableService().select(
					" CONSUMABLESTATE = 'InUse' AND TRANSPORTSTATE = 'OnEQP' " + " AND MATERIALLOCATIONNAME IS NOT NULL AND MACHINENAME LIKE ? "
							+ " AND (SEQ IS NULL OR SEQ=1) ORDER BY KITTIME ASC",
					new Object[] { eqpData.getKey().getMachineName()+"%" });

			if (consumableDataList != null && consumableDataList.size() > 0)
			{
				for (Consumable consumable : consumableDataList)
				{
					List<MaterialProduct> dataInfoList = new ArrayList<MaterialProduct>();

					for (String productName : productNameList)
					{
						Product product = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);

						MaterialProduct materialProduct = new MaterialProduct();
						materialProduct.setTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
						materialProduct.setProductName(productName);
						materialProduct.setLotName(lotName);
						materialProduct.setMaterialKind(GenericServiceProxy.getConstantMap().MaterialKind_Consumable);
						materialProduct.setMaterialType(consumable.getConsumableType());
						materialProduct.setMaterialName(consumable.getKey().getConsumableName());
						materialProduct.setQuantity(consumable.getQuantity());
						materialProduct.setEventName("TrackOut");
						materialProduct.setEventTime(eventInfo.getEventTime());
						materialProduct.setFactoryName(consumable.getFactoryName());
						materialProduct.setProductSpecName(product.getProductSpecName());
						materialProduct.setProductSpecVersion(product.getProductSpecVersion());
						materialProduct.setProcessFlowName(product.getProcessFlowName());
						materialProduct.setProcessFlowVersion(product.getProcessFlowVersion());
						materialProduct.setProcessOperationName(product.getProcessOperationName());
						materialProduct.setProcessOperationVersion(product.getProcessOperationVersion());
						materialProduct.setMachineName(eqpData.getKey().getMachineName());
						materialProduct.setMaterialLocationName(consumable.getMaterialLocationName());

						dataInfoList.add(materialProduct);

					}

					ExtendedObjectProxy.getMaterialProductService().insert(dataInfoList);
					log.info(String.format("鈻禡aterialProduct Data [LotName=%s,Location=%s] insert was successful!!", lotName, consumable.getMaterialLocationName()));
				}
			}

		}
		catch (NotFoundSignal ne)
		{
			log.info(String.format("Not Found any material that has been kitted to the machine [%s].", eqpData.getKey().getMachineName()));
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("LOT-9999", fe.getMessage());
		}
	}

	public List<Map<String, Object>> getTrayListByTrayGroup(String trayGroupName) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT D.DURABLENAME, ");
		sql.append("       D.LOTQUANTITY, ");
		sql.append("       D.DURABLESTATE, ");
		sql.append("       D.DURABLEHOLDSTATE, ");
		sql.append("       D.DURABLECLEANSTATE, ");
		sql.append("       D.COVERNAME, ");
		sql.append("       D.POSITION ");
		sql.append("  FROM DURABLE D ");
		sql.append(" WHERE D.COVERNAME = :TRAYGROUPNAME ");
		sql.append("ORDER BY D.POSITION / 1 DESC, D.DURABLENAME ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("TRAYGROUPNAME", trayGroupName);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		return sqlResult;
	}

	public List<Map<String, Object>> getLotListByTray(String trayName) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT L.LOTNAME, ");
		sql.append("       L.PRODUCTREQUESTNAME, ");
		sql.append("       L.PRODUCTSPECNAME, ");
		sql.append("       L.PRODUCTSPECVERSION, ");
		sql.append("       L.PROCESSFLOWNAME, ");
		sql.append("       L.PROCESSFLOWVERSION, ");
		sql.append("       L.PROCESSOPERATIONNAME, ");
		sql.append("       L.PROCESSOPERATIONVERSION, ");
		sql.append("       L.CARRIERNAME, ");
		sql.append("       L.PRODUCTIONTYPE, ");
		sql.append("       L.PRODUCTTYPE, ");
		sql.append("       L.POSITION ");
		sql.append("  FROM LOT L ");
		sql.append(" WHERE L.CARRIERNAME = :TRAYNAME ");
		sql.append(" ORDER BY L.POSITION ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("TRAYNAME", trayName);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		return sqlResult;
	}

	public List<Lot> getLotDataListByTray(String trayName) throws CustomException
	{
		String condition = " CARRIERNAME = ? ";
		Object[] bindSet = new Object[] { trayName };

		List<Lot> dataInfoList = new ArrayList<Lot>();

		try
		{
			dataInfoList = LotServiceProxy.getLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList;
	}
	
	public Node getFirstNode(String jobName, String factoryName, String processFlowName, String processFlowVersion) throws CustomException
	{
		Node targetNode = null;

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT QL.NODEID ");
		sql.append("  FROM PROCESSOPERATIONSPEC PO, ");
		sql.append("       (SELECT LEVEL LV, ");
		sql.append("               N.FACTORYNAME, ");
		sql.append("               N.NODEATTRIBUTE1 AS PROCESSOPERATIONNAME, ");
		sql.append("               N.NODEATTRIBUTE2 AS PROCESSOPERATIONVERSION, ");
		sql.append("               N.PROCESSFLOWNAME, ");
		sql.append("               N.PROCESSFLOWVERSION, ");
		sql.append("               N.NODEID ");
		sql.append("          FROM ARC A, NODE N, PROCESSFLOW PF ");
		sql.append("         WHERE 1 = 1 ");
		sql.append("           AND N.NODETYPE = 'ProcessOperation' ");
		sql.append("           AND PF.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("           AND PF.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("           AND N.FACTORYNAME = :FACTORYNAME ");
		sql.append("           AND N.PROCESSFLOWNAME = PF.PROCESSFLOWNAME ");
		sql.append("           AND N.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION ");
		sql.append("           AND N.PROCESSFLOWNAME = A.PROCESSFLOWNAME ");
		sql.append("           AND N.FACTORYNAME = PF.FACTORYNAME ");
		sql.append("           AND A.FROMNODEID = N.NODEID ");
		sql.append("        START WITH N.NODETYPE = 'Start' ");
		sql.append("        CONNECT BY NOCYCLE A.FROMNODEID = PRIOR A.TONODEID AND A.FACTORYNAME = :FACTORYNAME) QL, ");
		sql.append("       (SELECT DISTINCT P.FACTORYNAME, ");
		sql.append("               D.PROCESSFLOWNAME, ");
		sql.append("               D.PROCESSFLOWVERSION, ");
		sql.append("               D.PROCESSOPERATIONNAME, ");
		sql.append("               D.PROCESSOPERATIONVERSION ");
		sql.append("          FROM CT_MQCPLAN P, CT_MQCPLANDETAIL D ");
		sql.append("         WHERE P.JOBNAME = D.JOBNAME ");
		sql.append("           AND P.JOBNAME = :JOBNAME ");
		sql.append("           AND P.MQCSTATE IN ('Released','Recycling') ");
		sql.append("           AND D.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("           AND D.PROCESSFLOWVERSION = :PROCESSFLOWVERSION) P ");
		sql.append(" WHERE 1 = 1 ");
		sql.append("   AND PO.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND PO.PROCESSOPERATIONNAME = QL.PROCESSOPERATIONNAME ");
		sql.append("   AND PO.PROCESSOPERATIONVERSION = QL.PROCESSOPERATIONVERSION ");
		sql.append("   AND PO.FACTORYNAME = P.FACTORYNAME ");
		sql.append("   AND QL.FACTORYNAME = P.FACTORYNAME ");
		sql.append("   AND QL.PROCESSFLOWNAME = P.PROCESSFLOWNAME ");
		sql.append("   AND QL.PROCESSFLOWVERSION = P.PROCESSFLOWVERSION ");
		sql.append("   AND PO.PROCESSOPERATIONNAME = P.PROCESSOPERATIONNAME ");
		sql.append("   AND PO.PROCESSOPERATIONVERSION = P.PROCESSOPERATIONVERSION ");
		sql.append("ORDER BY QL.LV ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("FACTORYNAME", factoryName);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);
		args.put("JOBNAME", jobName);

		try
		{
			List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (result.size() > 0)
			{
				String nodeId = ConvertUtil.getMapValueByName(result.get(0), "NODEID");
				targetNode = ProcessFlowServiceProxy.getProcessFlowService().getNode(nodeId);
			}
		}
		catch (Exception ex)
		{
			log.warn("MQC FirstNode Query is failed");
		}

		return targetNode;
	}

	public Lot validateAndHoldLotAboutExposure(EventInfo eventInfo, Lot beforeTrackOutLot, Lot afterTrackOutLot) throws CustomException
	{
		List<MaterialProduct> materialProductList = null;
		try
		{
			materialProductList = MESLotServiceProxy.getLotInfoUtil().getMaterialProductList_PhotoMask(beforeTrackOutLot);
		}
		catch (greenFrameDBErrorSignal e)
		{
			// Not found material product information for Lot
			// return afterTrackOutLot;
			return holdLotAboutExposure(eventInfo, afterTrackOutLot);
		}

		List<ListOrderedMap> policyMaskList = null;
		try
		{
			policyMaskList = PolicyUtil.getPhotoMaskPolicy(beforeTrackOutLot, beforeTrackOutLot.getMachineName());
		}
		catch (greenFrameDBErrorSignal e)
		{
			// Not found policy information.
			return holdLotAboutExposure(eventInfo, afterTrackOutLot);
		}

		boolean passedPolicy = false;
		for (MaterialProduct materialProduct : materialProductList)
		{
			for (ListOrderedMap policy : policyMaskList)
			{
				String maskNameInPolicy = CommonUtil.getValue(policy, "MASKNAME");
				if (StringUtils.equals(materialProduct.getMaterialName(), maskNameInPolicy))
				{
					passedPolicy = true;
					break;
				}
			}

			if (passedPolicy)
				break;
		}

		if (!passedPolicy)
		{
			// The mask's exposure record is not suitable for policy information
			return holdLotAboutExposure(eventInfo, afterTrackOutLot);
		}

		return afterTrackOutLot; // All mask's exposure records are suitable for policy information.
	}

	private Lot holdLotAboutExposure(EventInfo eventInfo, Lot afterTrackOutLot) throws CustomException
	{
		// Save original reason info, comment
		String oriReasonCodeType = eventInfo.getReasonCodeType();
		String oriReasonCode = eventInfo.getEventComment();
		String oriEventComment = eventInfo.getEventComment();
		String holdEventComment = "Product Spec and Mask information are not matched in policy information.";
		log.info(holdEventComment);

		// Set reason, comment
		eventInfo.setReasonCodeType("HOLD");
		eventInfo.setReasonCode("SYSTEM-POLICY");
		eventInfo.setEventComment(holdEventComment);

		// LotMultiHold
		MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, afterTrackOutLot, new HashMap<String, String>());
		afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().getLotData(afterTrackOutLot.getKey().getLotName());

		eventInfo.setReasonCodeType(oriReasonCodeType);
		eventInfo.setReasonCode(oriReasonCode);
		eventInfo.setEventComment(oriEventComment);

		return afterTrackOutLot;
	}

	public List<Map<String, Object>> getScrapLotListByTrayGroup(String trayGroupName) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT L.* ");
		sql.append("  FROM DURABLE D, LOT L ");
		sql.append(" WHERE D.COVERNAME = :TRAYGROUPNAME ");
		sql.append("   AND D.DURABLENAME = L.CARRIERNAME ");
		sql.append("   AND L.LOTSTATE = 'Scrapped' ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("TRAYGROUPNAME", trayGroupName);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		return sqlResult;
	}

	public List<Map<String, Object>> getScarpLotListByTray(String trayName) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT L.LOTNAME, ");
		sql.append("       L.PRODUCTREQUESTNAME, ");
		sql.append("       L.PRODUCTSPECNAME, ");
		sql.append("       L.PRODUCTSPECVERSION, ");
		sql.append("       L.PROCESSFLOWNAME, ");
		sql.append("       L.PROCESSFLOWVERSION, ");
		sql.append("       L.PROCESSOPERATIONNAME, ");
		sql.append("       L.PROCESSOPERATIONVERSION, ");
		sql.append("       L.CARRIERNAME, ");
		sql.append("       L.PRODUCTIONTYPE, ");
		sql.append("       L.PRODUCTTYPE, ");
		sql.append("       L.POSITION ");
		sql.append("  FROM LOT L ");
		sql.append(" WHERE L.CARRIERNAME = :TRAYNAME ");
		sql.append(" AND L.LOTSTATE='Scrapped' ");
		sql.append(" ORDER BY L.POSITION ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("TRAYNAME", trayName);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		return sqlResult;
	}

	public boolean checkNextMQCRecycleNode(Lot lotData) throws CustomException
	{
		boolean isLastOperation = false;

		String[] nodeStackArray = StringUtils.split(lotData.getNodeStack(), ".");
		int idx = nodeStackArray.length;

		Node nextNode = null;

		try
		{
			nextNode = ProcessFlowServiceProxy.getProcessFlowService().getNextNode(nodeStackArray[idx], "Normal", "");

			if (StringUtils.isEmpty(nextNode.getNodeAttribute1()) || StringUtils.isEmpty(nextNode.getProcessFlowName()))
			{
				log.info("It is last node");
				isLastOperation = true;
			}

		}
		catch (Exception ex)
		{
			log.info("It is last node");
			isLastOperation = true;
		}

		return isLastOperation;
	}

	public List<SampleLot> syncSamplingData(EventInfo eventInfo, List<SampleLot> sampleLot, Lot lotData, List<Product> ProductList) throws CustomException
	{
		List<String> positionList = new ArrayList<String>();
		for (Product product : ProductList)
		{
			positionList.add(String.valueOf(product.getPosition()).toString());
		}

		List<SampleLot> sampleLotList = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListByToInfo(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
				lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());

		if (sampleLotList.size() > 0)
		{
			String lotSampleCount = sampleLotList.get(0).getLotSampleCount();
			String actualSamplePosition = sampleLotList.get(0).getActualSamplePosition();

			if (!StringUtils.equals(lotSampleCount, "0")) // LotSampleCount(CT_SampleLot) is not '0'.
			{
				List<Map<String, Object>> samplePolicyList = MESLotServiceProxy.getLotServiceUtil().getSamplePolicyList(sampleLotList.get(0).getFactoryName(),
						sampleLotList.get(0).getProcessFlowName(), sampleLotList.get(0).getProcessFlowVersion(), sampleLotList.get(0).getProcessOperationName(),
						sampleLotList.get(0).getProcessOperationVersion(), sampleLotList.get(0).getMachineName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
						lotData.getProcessOperationName(), lotData.getProcessOperationVersion());

				for (Map<String, Object> samplePolicyM : samplePolicyList)
				{
					log.info("Start syncSamplingData : Normal Case");

					// String productSamplePositions = (String) samplePolicyM.get("PRODUCTSAMPLINGPOSITION");
					String productSamplePositions = actualSamplePosition;
					log.info("setSamplingData:ProductSamplePositions(" + productSamplePositions + ")");

					List<String> TPTJProductList = new ArrayList<String>();

					// TPTJ Rule Search.
					List<TPTJRule> TPTJRuleDataList = ExtendedObjectProxy.getTPTJRuleService().getTPTJRuleDataBySync(lotData, samplePolicyM);

					if (TPTJRuleDataList != null)
					{
						for (TPTJRule TPTJRule : TPTJRuleDataList)
						{
							log.info(" TPTJ Rule Name = " + TPTJRule.getRuleName());
							List<TPTJProduct> TPTJProList = ExtendedObjectProxy.getTPTJProductService().getTPTJProductListByRuleFlow(lotData, TPTJRule);

							if (TPTJProList != null)
							{
								if (!ExtendedObjectProxy.getTPTJProductService().checkTPTJStartFlow(TPTJRule, lotData, samplePolicyM))
								{
									for (TPTJProduct TPTJProductData : TPTJProList)
										TPTJProductList.add(TPTJProductData.getProductName());
								}
							}

							break;
						}
					}

					// Parcing the 'PRODUCTSAMPLEPOSITION'
					productSamplePositions = MESLotServiceProxy.getLotServiceUtil().getProductSamplePositions(lotData, samplePolicyM, positionList, productSamplePositions);

					List<String> productSamplePositionList = CommonUtil.splitString(",", productSamplePositions); // delimeter is ','
					List<String> exceptBackpPositionList = CommonUtil.copyToStringList(productSamplePositionList);
					List<String> actualSamplePositionList = new ArrayList<String>();
					List<String> notExistPositionList = new ArrayList<String>();
					List<String> tptjPositionList = new ArrayList<String>();
					List<String> filterProductSamplePositionList = new ArrayList<String>();
					List<String> notExistTPTJProductList = new ArrayList<String>();

					List<SampleProduct> oldsampleProductList=ExtendedObjectProxy.getSampleProductService().getSampleProductDataListByLotNameAndToInfo(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
							lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());
					
					// Delete Sample Product Data
					MESLotServiceProxy.getLotServiceUtil().deleteSamplingProductBySync(eventInfo, lotData);

					eventInfo.setEventComment("Sync Sampling Data(" + lotData.getProcessOperationName() + ").  " + eventInfo.getEventComment());

					// Set TPTJ Sample Product Data.
					for (String tptjProductName : TPTJProductList)
					{
						// get SamplingProduct Data
						String sql = " SELECT PRODUCTNAME, POSITION FROM PRODUCT WHERE PRODUCTNAME = :productName AND LOTNAME = :lotName ";
						Map<String, Object> bindMap = new HashMap<String, Object>();
						bindMap.put("productName", tptjProductName);
						bindMap.put("lotName", lotData.getKey().getLotName());

						@SuppressWarnings("unchecked")
						List<Map<String, Object>> tptjProductResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

						if (tptjProductResult.size() > 0)
						{
							List<SampleProduct> sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListWithoutMachineName(
									(String) tptjProductResult.get(0).get("PRODUCTNAME"), lotData.getKey().getLotName(), sampleLotList.get(0).getFactoryName(),
									sampleLotList.get(0).getProductSpecName(), sampleLotList.get(0).getProductSpecVersion(), sampleLotList.get(0).getProcessFlowName(),
									sampleLotList.get(0).getProcessFlowVersion(), sampleLotList.get(0).getProcessOperationName(), sampleLotList.get(0).getProcessOperationVersion(),
									lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());

							if (sampleProductList == null)
							{
								// set SamplingProduct Data(CT_SAMPLEPRODUCT)
								ExtendedObjectProxy.getSampleProductService().insertSampleProductWithFlag(oldsampleProductList,eventInfo, (String) tptjProductResult.get(0).get("PRODUCTNAME"), lotData.getKey().getLotName(),
										lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), (String) samplePolicyM.get("PROCESSFLOWNAME"),
										(String) samplePolicyM.get("PROCESSFLOWVERSION"), (String) samplePolicyM.get("PROCESSOPERATIONNAME"), (String) samplePolicyM.get("PROCESSOPERATIONVERSION"),
										(String) samplePolicyM.get("MACHINENAME"), (String) samplePolicyM.get("TOPROCESSFLOWNAME"), (String) samplePolicyM.get("TOPROCESSFLOWVERSION"),
										(String) samplePolicyM.get("TOPROCESSOPERATIONNAME"), (String) samplePolicyM.get("TOPROCESSOPERATIONVERSION"), "Y",
										(String) samplePolicyM.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicyM.get("PRODUCTSAMPLINGPOSITION"),
										String.valueOf(tptjProductResult.get(0).get("POSITION")), "");

								actualSamplePositionList.add(String.valueOf(tptjProductResult.get(0).get("POSITION")));
								tptjPositionList.add(String.valueOf(tptjProductResult.get(0).get("POSITION")));
							}
							else
							{
								ExtendedObjectProxy.getSampleProductService().updateSampleProduct(eventInfo, (String) tptjProductResult.get(0).get("PRODUCTNAME"), lotData.getKey().getLotName(),
										sampleLotList.get(0).getFactoryName(), sampleLotList.get(0).getProductSpecName(), sampleLotList.get(0).getProductSpecVersion(),
										sampleLotList.get(0).getProcessFlowName(), sampleLotList.get(0).getProcessFlowVersion(), sampleLotList.get(0).getProcessOperationName(),
										sampleLotList.get(0).getProcessOperationVersion(), (String) samplePolicyM.get("MACHINENAME"), (String) samplePolicyM.get("TOPROCESSFLOWNAME"),
										(String) samplePolicyM.get("TOPROCESSFLOWVERSION"), (String) samplePolicyM.get("TOPROCESSOPERATIONNAME"),
										(String) samplePolicyM.get("TOPROCESSOPERATIONVERSION"), "Y", (String) samplePolicyM.get("PRODUCTSAMPLINGCOUNT"),
										(String) samplePolicyM.get("PRODUCTSAMPLINGPOSITION"), String.valueOf(tptjProductResult.get(0).get("POSITION")), "", "");

								actualSamplePositionList.add(String.valueOf(tptjProductResult.get(0).get("POSITION")));
								tptjPositionList.add(String.valueOf(tptjProductResult.get(0).get("POSITION")));
							}
						}
						else
							notExistTPTJProductList.add(tptjProductName);
					}

					// Filter Sample Position
					if (tptjPositionList.size() > 0)
					{
						for (String productSamplePosition : productSamplePositionList)
						{
							for (String tptjPosition : tptjPositionList)
							{
								if (!StringUtils.equals(tptjPosition, productSamplePosition) && !tptjPositionList.contains(productSamplePosition))
								{
									filterProductSamplePositionList.add(productSamplePosition);
									break;
								}
							}
						}
					}
					else
					{
						filterProductSamplePositionList = productSamplePositionList;
					}

					for (String productSamplePosition : filterProductSamplePositionList)
					{
						log.info("setSamplingData:productSamplePosition(" + productSamplePosition + ")");
						boolean existCheck = false;

						for (Product productData : ProductList)
						{
							String position = String.valueOf(productData.getPosition()).toString();

							if (StringUtils.equals(position, productSamplePosition))
							{
								String productName = productData.getKey().getProductName();

								// get SamplingProduct Data
								String sql = " SELECT PRODUCTNAME FROM PRODUCT WHERE PRODUCTNAME = :productName ";
								Map<String, Object> bindMap = new HashMap<String, Object>();
								bindMap.put("lotName", lotData.getKey().getLotName());
								bindMap.put("productName", productName);

								@SuppressWarnings("unchecked")
								List<Map<String, Object>> sampleProductResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

								if (sampleProductResult.size() > 0) // Exist the Product for productSamplePosition
								{
									List<SampleProduct> sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListWithoutMachineName(
											(String) sampleProductResult.get(0).get("PRODUCTNAME"), lotData.getKey().getLotName(), sampleLotList.get(0).getFactoryName(),
											sampleLotList.get(0).getProductSpecName(), sampleLotList.get(0).getProductSpecVersion(), sampleLotList.get(0).getProcessFlowName(),
											sampleLotList.get(0).getProcessFlowVersion(), sampleLotList.get(0).getProcessOperationName(), sampleLotList.get(0).getProcessOperationVersion(),
											lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());

									if (sampleProductList == null)
									{
										// set SamplingProduct Data(CT_SAMPLEPRODUCT)
										ExtendedObjectProxy.getSampleProductService().insertSampleProductWithFlag(oldsampleProductList,eventInfo, (String) sampleProductResult.get(0).get("PRODUCTNAME"),
												lotData.getKey().getLotName(), sampleLotList.get(0).getFactoryName(), sampleLotList.get(0).getProductSpecName(),
												sampleLotList.get(0).getProductSpecVersion(), sampleLotList.get(0).getProcessFlowName(), sampleLotList.get(0).getProcessFlowVersion(),
												sampleLotList.get(0).getProcessOperationName(), sampleLotList.get(0).getProcessOperationVersion(), (String) samplePolicyM.get("MACHINENAME"),
												(String) samplePolicyM.get("TOPROCESSFLOWNAME"), (String) samplePolicyM.get("TOPROCESSFLOWVERSION"),
												(String) samplePolicyM.get("TOPROCESSOPERATIONNAME"), (String) samplePolicyM.get("TOPROCESSOPERATIONVERSION"), "Y",
												(String) samplePolicyM.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicyM.get("PRODUCTSAMPLINGPOSITION"), productSamplePosition, "");

										actualSamplePositionList.add(productSamplePosition);

										existCheck = true;
										break;
									}
									else
									{
										ExtendedObjectProxy.getSampleProductService().updateSampleProduct(eventInfo, (String) sampleProductResult.get(0).get("PRODUCTNAME"),
												lotData.getKey().getLotName(), sampleLotList.get(0).getFactoryName(), sampleLotList.get(0).getProductSpecName(),
												sampleLotList.get(0).getProductSpecVersion(), sampleLotList.get(0).getProcessFlowName(), sampleLotList.get(0).getProcessFlowVersion(),
												sampleLotList.get(0).getProcessOperationName(), sampleLotList.get(0).getProcessOperationVersion(), (String) samplePolicyM.get("MACHINENAME"),
												(String) samplePolicyM.get("TOPROCESSFLOWNAME"), (String) samplePolicyM.get("TOPROCESSFLOWVERSION"),
												(String) samplePolicyM.get("TOPROCESSOPERATIONNAME"), (String) samplePolicyM.get("TOPROCESSOPERATIONVERSION"), "Y",
												(String) samplePolicyM.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicyM.get("PRODUCTSAMPLINGPOSITION"), productSamplePosition, "", "");

										actualSamplePositionList.add(productSamplePosition);
										existCheck = true;
										break;
									}
								}
							}
						}
						if (!existCheck) // No exist the Product for productSamplePosition, find new Product(backupProduct) for that.
						{
							notExistPositionList.add(productSamplePosition);
						}
					}

					if (!notExistPositionList.isEmpty())
					{
						List<Product> backupProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());

						int j = 0;
						int i = 0;

						for (; i < backupProductList.size(); i++)
						{
							Product backupProduct = backupProductList.get(i);

							for (Product productData : ProductList)
							{
								String productName = productData.getKey().getProductName();

								if (StringUtils.equals(backupProduct.getKey().getProductName(), productName))
								{
									String position = String.valueOf(productData.getPosition()).toString();

									long notExistPosition = 0;
									try
									{
										notExistPosition = Long.parseLong(notExistPositionList.get(j));
									}
									catch (Exception e)
									{
										break;
									}

									if (Long.valueOf(position) > notExistPosition && !exceptBackpPositionList.contains(position) && !actualSamplePositionList.contains(position))
									{
										List<SampleProduct> sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListWithoutMachineName(
												backupProduct.getKey().getProductName(), lotData.getKey().getLotName(), sampleLotList.get(0).getFactoryName(),
												sampleLotList.get(0).getProductSpecName(), sampleLotList.get(0).getProductSpecVersion(), sampleLotList.get(0).getProcessFlowName(),
												sampleLotList.get(0).getProcessFlowVersion(), sampleLotList.get(0).getProcessOperationName(), sampleLotList.get(0).getProcessOperationVersion(),
												lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());

										if (sampleProductList == null)
										{
											// set SamplingProduct Data(CT_SAMPLEPRODUCT)
											ExtendedObjectProxy.getSampleProductService().insertSampleProductWithFlag(oldsampleProductList,eventInfo, backupProduct.getKey().getProductName(), lotData.getKey().getLotName(),
													sampleLotList.get(0).getFactoryName(), sampleLotList.get(0).getProductSpecName(), sampleLotList.get(0).getProductSpecVersion(),
													sampleLotList.get(0).getProcessFlowName(), sampleLotList.get(0).getProcessFlowVersion(), sampleLotList.get(0).getProcessOperationName(),
													sampleLotList.get(0).getProcessOperationVersion(), (String) samplePolicyM.get("MACHINENAME"), (String) samplePolicyM.get("TOPROCESSFLOWNAME"),
													(String) samplePolicyM.get("TOPROCESSFLOWVERSION"), (String) samplePolicyM.get("TOPROCESSOPERATIONNAME"),
													(String) samplePolicyM.get("TOPROCESSOPERATIONVERSION"), "Y", (String) samplePolicyM.get("PRODUCTSAMPLINGCOUNT"),
													(String) samplePolicyM.get("PRODUCTSAMPLINGPOSITION"), position, "");

											// except for new Product(backupProduct)
											exceptBackpPositionList.add(position);
											actualSamplePositionList.add(position);
											j++;
											break;
										}
										else
										{
											ExtendedObjectProxy.getSampleProductService().updateSampleProduct(eventInfo, backupProduct.getKey().getProductName(), lotData.getKey().getLotName(),
													sampleLotList.get(0).getFactoryName(), sampleLotList.get(0).getProductSpecName(), sampleLotList.get(0).getProductSpecVersion(),
													sampleLotList.get(0).getProcessFlowName(), sampleLotList.get(0).getProcessFlowVersion(), sampleLotList.get(0).getProcessOperationName(),
													sampleLotList.get(0).getProcessOperationVersion(), (String) samplePolicyM.get("MACHINENAME"), (String) samplePolicyM.get("TOPROCESSFLOWNAME"),
													(String) samplePolicyM.get("TOPROCESSFLOWVERSION"), (String) samplePolicyM.get("TOPROCESSOPERATIONNAME"),
													(String) samplePolicyM.get("TOPROCESSOPERATIONVERSION"), "Y", (String) samplePolicyM.get("PRODUCTSAMPLINGCOUNT"),
													(String) samplePolicyM.get("PRODUCTSAMPLINGPOSITION"), position, "", "");

											// except for new Product(backupProduct)
											exceptBackpPositionList.add(position);
											actualSamplePositionList.add(position);
											j++;
										}
									}
									if (j == notExistPositionList.size())
									{
										break;
									}

									break;
								}
							}
						}

						if (j != notExistPositionList.size())
						{
							i = 0;

							for (; i < backupProductList.size(); i++)
							{
								Product backupProduct = backupProductList.get(i);

								for (Product productData : ProductList)
								{
									String productName = productData.getKey().getProductName();

									if (StringUtils.equals(backupProduct.getKey().getProductName(), productName))
									{
										String position = String.valueOf(productData.getPosition()).toString();

										long notExistPosition = 0;
										try
										{
											notExistPosition = Long.parseLong(notExistPositionList.get(j));
										}
										catch (Exception e)
										{
											break;
										}

										if (Long.valueOf(position) < notExistPosition && !exceptBackpPositionList.contains(position) && !actualSamplePositionList.contains(position))
										{
											List<SampleProduct> sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListWithoutMachineName(
													backupProduct.getKey().getProductName(), lotData.getKey().getLotName(), sampleLotList.get(0).getFactoryName(),
													sampleLotList.get(0).getProductSpecName(), sampleLotList.get(0).getProductSpecVersion(), sampleLotList.get(0).getProcessFlowName(),
													sampleLotList.get(0).getProcessFlowVersion(), sampleLotList.get(0).getProcessOperationName(), sampleLotList.get(0).getProcessOperationVersion(),
													lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());

											if (sampleProductList == null)
											{
												// set SamplingProduct Data(CT_SAMPLEPRODUCT)
												ExtendedObjectProxy.getSampleProductService().insertSampleProductWithFlag(oldsampleProductList,eventInfo, backupProduct.getKey().getProductName(), lotData.getKey().getLotName(),
														sampleLotList.get(0).getFactoryName(), sampleLotList.get(0).getProductSpecName(), sampleLotList.get(0).getProductSpecVersion(),
														sampleLotList.get(0).getProcessFlowName(), sampleLotList.get(0).getProcessFlowVersion(), sampleLotList.get(0).getProcessOperationName(),
														sampleLotList.get(0).getProcessOperationVersion(), (String) samplePolicyM.get("MACHINENAME"), (String) samplePolicyM.get("TOPROCESSFLOWNAME"),
														(String) samplePolicyM.get("TOPROCESSFLOWVERSION"), (String) samplePolicyM.get("TOPROCESSOPERATIONNAME"),
														(String) samplePolicyM.get("TOPROCESSOPERATIONVERSION"), "Y", (String) samplePolicyM.get("PRODUCTSAMPLINGCOUNT"),
														(String) samplePolicyM.get("PRODUCTSAMPLINGPOSITION"), position, "");

												// except for new Product(backupProduct)
												exceptBackpPositionList.add(position);
												actualSamplePositionList.add(position);
												j++;
												break;
											}
										}

										if (j == notExistPositionList.size())
										{
											break;
										}

										break;
									}
								}
							}
						}
						notExistPositionList.clear();
					}
					// TPTJ BackUp
					if (!notExistTPTJProductList.isEmpty())
					{
						int j = 0;
						List<Product> backupProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());

						for (int i = 0; i < backupProductList.size(); i++)
						{
							Product backupProduct = backupProductList.get(i);
							String position = String.valueOf(backupProduct.getPosition());

							if (!actualSamplePositionList.contains(position))
							{
								ExtendedObjectProxy.getSampleProductService().insertSampleProductWithFlag(oldsampleProductList,eventInfo, backupProduct.getKey().getProductName(), lotData.getKey().getLotName(),
										lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), sampleLotList.get(0).getProcessFlowName(),
										sampleLotList.get(0).getProcessFlowVersion(), sampleLotList.get(0).getProcessOperationName(), sampleLotList.get(0).getProcessOperationVersion(),
										(String) samplePolicyM.get("MACHINENAME"), (String) samplePolicyM.get("TOPROCESSFLOWNAME"), (String) samplePolicyM.get("TOPROCESSFLOWVERSION"),
										(String) samplePolicyM.get("TOPROCESSOPERATIONNAME"), (String) samplePolicyM.get("TOPROCESSOPERATIONVERSION"), "Y",
										(String) samplePolicyM.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicyM.get("PRODUCTSAMPLINGPOSITION"), position, "");

								actualSamplePositionList.add(position);
								j++;
							}

							if (j == notExistTPTJProductList.size())
							{
								break;
							}
						}
					}

					if (!actualSamplePositionList.isEmpty())
					{
						// update sampleLot
						sampleLot = ExtendedObjectProxy.getSampleLotService().updateSampleLotReturnList(eventInfo, lotData.getKey().getLotName(), sampleLotList.get(0).getFactoryName(),
								sampleLotList.get(0).getProductSpecName(), sampleLotList.get(0).getProductSpecVersion(), sampleLotList.get(0).getProcessFlowName(),
								sampleLotList.get(0).getProcessFlowVersion(), sampleLotList.get(0).getProcessOperationName(), sampleLotList.get(0).getProcessOperationVersion(),
								(String) samplePolicyM.get("MACHINENAME"), (String) samplePolicyM.get("TOPROCESSFLOWNAME"), (String) samplePolicyM.get("TOPROCESSFLOWVERSION"),
								(String) samplePolicyM.get("TOPROCESSOPERATIONNAME"), (String) samplePolicyM.get("TOPROCESSOPERATIONVERSION"), "Y", (String) samplePolicyM.get("LOTSAMPLECOUNT"), "",
								"", (String) samplePolicyM.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicyM.get("PRODUCTSAMPLINGPOSITION"), String.valueOf(actualSamplePositionList.size()),
								CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "", "", samplePolicyM.get("FLOWPRIORITY").toString(),
								(String) samplePolicyM.get("RETURNPROCESSFLOWNAME"), (String) samplePolicyM.get("RETURNPROCESSFLOWVERSION"), (String) samplePolicyM.get("RETURNOPERATIONNAME"),
								(String) samplePolicyM.get("RETURNOPERATIONVER"), "");
					}
				}
			}
			else
			// Only TPTJ Case. LotSampleCount(CT_SampleLot) is '0'.
			{
				List<Map<String, Object>> samplePolicyList = MESLotServiceProxy.getLotServiceUtil().getSamplePolicyList(sampleLotList.get(0).getFactoryName(),
						sampleLotList.get(0).getProcessFlowName(), sampleLotList.get(0).getProcessFlowVersion(), sampleLotList.get(0).getProcessOperationName(),
						sampleLotList.get(0).getProcessOperationVersion(), sampleLotList.get(0).getMachineName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
						lotData.getProcessOperationName(), lotData.getProcessOperationVersion());

				for (Map<String, Object> samplePolicyM : samplePolicyList)
				{
					log.info("Start syncSamplingData : Only TPTJ Case");

					List<String> TPTJProductList = new ArrayList<String>();

					// TPTJ Rule Search.
					List<TPTJRule> TPTJRuleDataList = ExtendedObjectProxy.getTPTJRuleService().getTPTJRuleDataBySync(lotData, samplePolicyM);

					if (TPTJRuleDataList != null)
					{
						for (TPTJRule TPTJRule : TPTJRuleDataList)
						{
							log.info(" TPTJ Rule Name = " + TPTJRule.getRuleName());
							List<TPTJProduct> TPTJProList = ExtendedObjectProxy.getTPTJProductService().getTPTJProductListByRuleFlow(lotData, TPTJRule);

							if (TPTJProList != null)
							{
								if (!ExtendedObjectProxy.getTPTJProductService().checkTPTJStartFlow(TPTJRule, lotData, samplePolicyM))
								{
									for (TPTJProduct TPTJProductData : TPTJProList)
										TPTJProductList.add(TPTJProductData.getProductName());

									// Delete Sample Product Data
									MESLotServiceProxy.getLotServiceUtil().deleteSamplingProductBySync(eventInfo, lotData);
								}
							}

							break;
						}
					}

					eventInfo.setEventComment("Sync Sampling Data(" + lotData.getProcessOperationName() + "). Only TPTJ Case : " + eventInfo.getEventComment());

					List<String> actualSamplePositionList = new ArrayList<String>();
					List<String> tptjPositionList = new ArrayList<String>();
					List<String> notExistTPTJProductList = new ArrayList<String>();

					// Set TPTJ Sample Product Data.
					for (String tptjProductName : TPTJProductList)
					{
						// get SamplingProduct Data
						String sql = " SELECT PRODUCTNAME, POSITION FROM PRODUCT WHERE PRODUCTNAME = :productName AND LOTNAME = :lotName ";
						Map<String, Object> bindMap = new HashMap<String, Object>();
						bindMap.put("productName", tptjProductName);
						bindMap.put("lotName", lotData.getKey().getLotName());

						@SuppressWarnings("unchecked")
						List<Map<String, Object>> tptjProductResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

						if (tptjProductResult.size() > 0)
						{
							List<SampleProduct> sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListWithoutMachineName(
									(String) tptjProductResult.get(0).get("PRODUCTNAME"), lotData.getKey().getLotName(), sampleLotList.get(0).getFactoryName(),
									sampleLotList.get(0).getProductSpecName(), sampleLotList.get(0).getProductSpecVersion(), sampleLotList.get(0).getProcessFlowName(),
									sampleLotList.get(0).getProcessFlowVersion(), sampleLotList.get(0).getProcessOperationName(), sampleLotList.get(0).getProcessOperationVersion(),
									lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());

							if (sampleProductList == null)
							{
								// set SamplingProduct Data(CT_SAMPLEPRODUCT)
								ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, (String) tptjProductResult.get(0).get("PRODUCTNAME"), lotData.getKey().getLotName(),
										lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), (String) samplePolicyM.get("PROCESSFLOWNAME"),
										(String) samplePolicyM.get("PROCESSFLOWVERSION"), (String) samplePolicyM.get("PROCESSOPERATIONNAME"), (String) samplePolicyM.get("PROCESSOPERATIONVERSION"),
										(String) samplePolicyM.get("MACHINENAME"), (String) samplePolicyM.get("TOPROCESSFLOWNAME"), (String) samplePolicyM.get("TOPROCESSFLOWVERSION"),
										(String) samplePolicyM.get("TOPROCESSOPERATIONNAME"), (String) samplePolicyM.get("TOPROCESSOPERATIONVERSION"), "Y",
										(String) samplePolicyM.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicyM.get("PRODUCTSAMPLINGPOSITION"),
										String.valueOf(tptjProductResult.get(0).get("POSITION")), "", "");

								actualSamplePositionList.add(String.valueOf(tptjProductResult.get(0).get("POSITION")));
								tptjPositionList.add(String.valueOf(tptjProductResult.get(0).get("POSITION")));
							}
							else
							{
								ExtendedObjectProxy.getSampleProductService().updateSampleProduct(eventInfo, (String) tptjProductResult.get(0).get("PRODUCTNAME"), lotData.getKey().getLotName(),
										sampleLotList.get(0).getFactoryName(), sampleLotList.get(0).getProductSpecName(), sampleLotList.get(0).getProductSpecVersion(),
										sampleLotList.get(0).getProcessFlowName(), sampleLotList.get(0).getProcessFlowVersion(), sampleLotList.get(0).getProcessOperationName(),
										sampleLotList.get(0).getProcessOperationVersion(), (String) samplePolicyM.get("MACHINENAME"), (String) samplePolicyM.get("TOPROCESSFLOWNAME"),
										(String) samplePolicyM.get("TOPROCESSFLOWVERSION"), (String) samplePolicyM.get("TOPROCESSOPERATIONNAME"),
										(String) samplePolicyM.get("TOPROCESSOPERATIONVERSION"), "Y", (String) samplePolicyM.get("PRODUCTSAMPLINGCOUNT"),
										(String) samplePolicyM.get("PRODUCTSAMPLINGPOSITION"), String.valueOf(tptjProductResult.get(0).get("POSITION")), "", "");

								actualSamplePositionList.add(String.valueOf(tptjProductResult.get(0).get("POSITION")));
								tptjPositionList.add(String.valueOf(tptjProductResult.get(0).get("POSITION")));
							}
						}
						else
							notExistTPTJProductList.add(tptjProductName);
					}

					// TPTJ BackUp
					if (!notExistTPTJProductList.isEmpty())
					{
						int j = 0;
						List<Product> backupProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());

						for (int i = 0; i < backupProductList.size(); i++)
						{
							Product backupProduct = backupProductList.get(i);
							String position = String.valueOf(backupProduct.getPosition());

							if (!actualSamplePositionList.contains(position))
							{
								ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, backupProduct.getKey().getProductName(), lotData.getKey().getLotName(),
										lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), (String) samplePolicyM.get("PROCESSFLOWNAME"),
										(String) samplePolicyM.get("PROCESSFLOWVERSION"), (String) samplePolicyM.get("PROCESSOPERATIONNAME"), (String) samplePolicyM.get("PROCESSOPERATIONVERSION"),
										(String) samplePolicyM.get("MACHINENAME"), (String) samplePolicyM.get("TOPROCESSFLOWNAME"), (String) samplePolicyM.get("TOPROCESSFLOWVERSION"),
										(String) samplePolicyM.get("TOPROCESSOPERATIONNAME"), (String) samplePolicyM.get("TOPROCESSOPERATIONVERSION"), "Y",
										(String) samplePolicyM.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicyM.get("PRODUCTSAMPLINGPOSITION"), position, "", "");

								actualSamplePositionList.add(position);
								j++;
							}

							if (j == notExistTPTJProductList.size())
							{
								break;
							}
						}
					}

					if (!actualSamplePositionList.isEmpty())
					{
						// update sampleLot
						sampleLot = ExtendedObjectProxy.getSampleLotService().updateSampleLotReturnList(eventInfo, lotData.getKey().getLotName(), sampleLotList.get(0).getFactoryName(),
								sampleLotList.get(0).getProductSpecName(), sampleLotList.get(0).getProductSpecVersion(), sampleLotList.get(0).getProcessFlowName(),
								sampleLotList.get(0).getProcessFlowVersion(), sampleLotList.get(0).getProcessOperationName(), sampleLotList.get(0).getProcessOperationVersion(),
								(String) samplePolicyM.get("MACHINENAME"), (String) samplePolicyM.get("TOPROCESSFLOWNAME"), (String) samplePolicyM.get("TOPROCESSFLOWVERSION"),
								(String) samplePolicyM.get("TOPROCESSOPERATIONNAME"), (String) samplePolicyM.get("TOPROCESSOPERATIONVERSION"), "Y", (String) samplePolicyM.get("LOTSAMPLECOUNT"), "",
								"", (String) samplePolicyM.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicyM.get("PRODUCTSAMPLINGPOSITION"), String.valueOf(actualSamplePositionList.size()),
								CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "", "", samplePolicyM.get("FLOWPRIORITY").toString(),
								(String) samplePolicyM.get("RETURNPROCESSFLOWNAME"), (String) samplePolicyM.get("RETURNPROCESSFLOWVERSION"), (String) samplePolicyM.get("RETURNOPERATIONNAME"),
								(String) samplePolicyM.get("RETURNOPERATIONVER"), "");
					}
				}
			}
		}

		// ChamberSampling
		List<Map<String, Object>> chamberSamplePolicyList = MESLotServiceProxy.getLotServiceUtil().getChamberSamplePolicyList(sampleLotList.get(0).getFactoryName(),
				sampleLotList.get(0).getProcessFlowName(), sampleLotList.get(0).getProcessFlowVersion(), sampleLotList.get(0).getProcessOperationName(),
				sampleLotList.get(0).getProcessOperationVersion(), sampleLotList.get(0).getMachineName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
				lotData.getProcessOperationName(), lotData.getProcessOperationVersion());

		if (chamberSamplePolicyList.size() > 0)
		{
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(sampleLotList.get(0).getMachineName());

			EventInfoExtended chamberSampleEventInfo = new EventInfoExtended(eventInfo);
			chamberSampleEventInfo.setEventComment("[SyncChamber] Sync System ChamberSampling. " + eventInfo.getEventComment());
			chamberSampleEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			for (Map<String, Object> chamberSamplePolicyM : chamberSamplePolicyList)
			{
				// ChamberSamping Case
				sampleLot = MESLotServiceProxy.getLotServiceUtil().setChamberSamplingData(chamberSampleEventInfo, lotData, machineData, chamberSamplePolicyM, sampleLotList.get(0).getFactoryName(),
						lotData.getProductSpecName(), lotData.getProductSpecVersion(), sampleLotList.get(0).getProcessFlowName(), sampleLotList.get(0).getProcessFlowVersion(),
						sampleLotList.get(0).getProcessOperationName(), sampleLotList.get(0).getProcessOperationVersion());
			}
		}
		return sampleLot;
	}

	public void deleteSamplingProductBySync(EventInfo eventInfo, Lot afterTrackOutLot) throws CustomException
	{
		log.info("Start deleteSamplingProductBySync");
		List<SampleLot> sampleLotList = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListByToInfo(afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
				afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), afterTrackOutLot.getProcessFlowName(), afterTrackOutLot.getProcessFlowVersion(),
				afterTrackOutLot.getProcessOperationName(), afterTrackOutLot.getProcessOperationVersion());

		if (sampleLotList != null)
		{
			ExtendedObjectProxy.getSampleProductService().deleteSampleProductByLotNameAndToInfo(eventInfo, afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(),
					afterTrackOutLot.getProductSpecName(), afterTrackOutLot.getProductSpecVersion(), sampleLotList.get(0).getToProcessFlowName(), sampleLotList.get(0).getToProcessFlowVersion(),
					sampleLotList.get(0).getToProcessOperationName(), sampleLotList.get(0).getToProcessOperationVersion());
		}
	}

	public void RemoveRecycleMQCJob(EventInfo eventInfo, Lot sourceLotData) throws CustomException
	{
		List<MQCPlan> jobList;
		List<MQCPlanDetail> PlanDetailList = new ArrayList<MQCPlanDetail>();

		try
		{
			jobList = ExtendedObjectProxy.getMQCPlanService().select("lotName = ? AND MQCstate = ?", new Object[] { sourceLotData.getKey().getLotName(), "Recycling" });
		}
		catch (greenFrameDBErrorSignal de)
		{
			if (de.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(de.getDataKey(), de.getSql());
			else
				throw new CustomException("SYS-8001", de.getSql());
		}

		if (jobList != null)
		{
			// recycling Lot is only one
			MQCPlan planData = jobList.get(0);
			String jobName = planData.getJobName();

			eventInfo.setEventName("RemoveRecycleMQC");

			StringBuilder sql = new StringBuilder();
			Map<String, Object> args = new HashMap<String, Object>();
			String MQCRecycleFlow = "";
			String MQCRecycleFlowVersion = "";

			sql.append("SELECT DISTINCT ");
			sql.append("       D.JOBNAME, D.PROCESSFLOWNAME, D.PROCESSFLOWVERSION, D.PROCESSOPERATIONNAME, D.PROCESSOPERATIONVERSION ");
			sql.append("  FROM CT_MQCPLANDETAIL D, CT_MQCPLANDETAIL_EXTENDED E ");
			sql.append(" WHERE D.JOBNAME = E.JOBNAME ");
			sql.append("   AND D.PROCESSFLOWNAME = E.PROCESSFLOWNAME ");
			sql.append("   AND D.PROCESSFLOWVERSION = E.PROCESSFLOWVERSION ");
			sql.append("   AND D.PROCESSOPERATIONNAME = E.PROCESSOPERATIONNAME ");
			sql.append("   AND D.PROCESSOPERATIONVERSION = E.PROCESSOPERATIONVERSION ");
			sql.append("   AND D.JOBNAME = :JOBNAME ");
			sql.append("   AND E.PROCESSFLOWNAME IN (SELECT PROCESSFLOWNAME ");
			sql.append("                               FROM PROCESSFLOW ");
			sql.append("                              WHERE PROCESSFLOWTYPE = 'MQCRecycle') ");

			args.clear();
			args.put("JOBNAME", planData.getJobName());

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (result.size() > 0)
			{
				log.info("Exist MQC Recycle Job from CT_MQCPLANDETAIL_EXTENDED");
				MQCRecycleFlow = result.get(0).get("PROCESSFLOWNAME").toString();
				MQCRecycleFlowVersion = result.get(0).get("PROCESSFLOWVERSION").toString();

				for (Map<String, Object> map : result)
				{
					String processFlowName = ConvertUtil.getMapValueByName(map, "PROCESSFLOWNAME");
					String processFlowVersion = ConvertUtil.getMapValueByName(map, "PROCESSFLOWVERSION");
					String processOperationName = ConvertUtil.getMapValueByName(map, "PROCESSOPERATIONNAME");
					String processOperationVersion = ConvertUtil.getMapValueByName(map, "PROCESSOPERATIONVERSION");

					MQCPlanDetail PlanDetail = ExtendedObjectProxy.getMQCPlanDetailService().selectByKey(false,
							new Object[] { planData.getJobName(), processFlowName, processFlowVersion, processOperationName, processOperationVersion });

					PlanDetailList.add(PlanDetail);
				}
			}
			else
			{
				log.info("Not exist MQC Recycle Job from CT_MQCPLANDETAIL_EXTENDED");
			}

			// Delete MQCPlanDetail_Extended Only Recycle MQC
			log.info(" Start delete MQC Recycle Job from  CT_MQCPLANDETAIL_EXTENDED");
			ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().deleteMQCPlanDetail_ExtendedByFlow(jobName, MQCRecycleFlow, MQCRecycleFlowVersion);

			log.info(" End delete MQC Recycle Job from  CT_MQCPLANDETAIL_EXTENDED");

			log.info(" Start delete MQC Recycle Job from  CT_MQCPLANDETAIL");
			// Delete CT_MQCPLANDETAIL Only Recycle MQC
			for (MQCPlanDetail detailPlanData : PlanDetailList)
			{
				ExtendedObjectProxy.getMQCPlanDetailService().deleteMQCPlanDetail(eventInfo, detailPlanData);
			}
			log.info(" End delete MQC Recycle Job from  CT_MQCPLANDETAIL");

			try
			{
				kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = MESLotServiceProxy.getLotInfoUtil().setEventInfo(sourceLotData, 0, new ArrayList<ProductU>());
				LotServiceProxy.getLotService().setEvent(sourceLotData.getKey(), eventInfo, setEventInfo);
			}
			catch (Exception e)
			{
			}

		}
	}

	public void createMQC(Lot sourceLotData, Lot destinationLotData, List<String> prodList, String destCarrierName, EventInfo eventInfo) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		Map<String, Object> args = new HashMap<String, Object>();
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

		List<MQCPlan> mqcPlanData = ExtendedObjectProxy.getMQCPlanService().getMQCPlanDataByLotNameMultiState(sourceLotData.getKey().getLotName());

		if (mqcPlanData != null)
		{
			String oldJobName = mqcPlanData.get(0).getJobName();
			String oldJobState = mqcPlanData.get(0).getMQCState();

			String jobName = "MQC-" + destinationLotData.getKey().getLotName() + "-" + TimeStampUtil.getCurrentEventTimeKey();

			boolean isRecycleMQC = false;
			boolean isMQCwithReserveEQP = false;

			isRecycleMQC = isRecycleMQC(oldJobName);
			isMQCwithReserveEQP = isMQCwithReserveEQP(oldJobName);

			// Create MQCPlanDetail
			List<MQCPlanDetail> destPlanDetailList = new ArrayList<MQCPlanDetail>();
			List<MQCPlanDetail> oldPlanDetailList = new ArrayList<MQCPlanDetail>();
			List<MQCPlanDetail> srcPlanDetailList = new ArrayList<MQCPlanDetail>();

			sql.setLength(0);
			sql.append("SELECT DISTINCT P.POSITION, P.PRODUCTNAME");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P ");
			sql.append(" WHERE E.PRODUCTNAME = P.PRODUCTNAME ");
			sql.append("   AND E.JOBNAME = :OLDJOBNAME ");
			sql.append("   AND P.PRODUCTNAME IN ( :PRODUCTLIST ) ");
			sql.append("ORDER BY TO_NUMBER(P.POSITION) ");

			args.clear();
			args.put("OLDJOBNAME", oldJobName);
			args.put("PRODUCTLIST", prodList);

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> positionResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (positionResult.size() > 0)
			{
				MQCPlan oldPlanData = ExtendedObjectProxy.getMQCPlanService().selectByKey(false, new Object[] { oldJobName });

				// Create MQCPlan
				MQCPlan planData = (MQCPlan) ObjectUtil.copyTo(oldPlanData);
				planData = ExtendedObjectProxy.getMQCPlanService().insertMQCPlan(eventInfo, planData, jobName, 1, destinationLotData.getKey().getLotName(), oldJobState);

				List<String> positionList = CommonUtil.makeListBySqlResult(positionResult, "POSITION");
				String actualPosition = CommonUtil.toStringWithoutBrackets(positionList);
				String modifyPositionforMainMQC = getModifyPositionforSourceMQCJob(oldJobName, prodList, isRecycleMQC);
				String modifyPositionforRecycle = getModifyPositionforSourceRecycleMQCJob(oldJobName, prodList, isRecycleMQC);
				String modifyDestPositionforMain = getModifyPositionforDestMQCJob(oldJobName, jobName, prodList, isRecycleMQC);
				String modifyDestPositionforRecycle = getModifyPositionforDestRecycleMQCJob(oldJobName, jobName, prodList, isRecycleMQC);

				sql.setLength(0);
				sql.append("SELECT DISTINCT ");
				sql.append("       D.JOBNAME, ");
				sql.append("       D.PROCESSFLOWNAME, ");
				sql.append("       D.PROCESSFLOWVERSION, ");
				sql.append("       D.PROCESSOPERATIONNAME, ");
				sql.append("       D.PROCESSOPERATIONVERSION ");
				sql.append("  FROM CT_MQCPLANDETAIL D, CT_MQCPLANDETAIL_EXTENDED E ");
				sql.append(" WHERE D.JOBNAME = E.JOBNAME ");
				sql.append("   AND D.PROCESSFLOWNAME = E.PROCESSFLOWNAME ");
				sql.append("   AND D.PROCESSFLOWVERSION = E.PROCESSFLOWVERSION ");
				sql.append("   AND D.PROCESSOPERATIONNAME = E.PROCESSOPERATIONNAME ");
				sql.append("   AND D.PROCESSOPERATIONVERSION = E.PROCESSOPERATIONVERSION ");
				sql.append("   AND D.JOBNAME = :OLDJOBNAME ");
				sql.append("   AND E.PRODUCTNAME IN ( :PRODUCTLIST ) ");

				result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

				if (result.size() > 0)
				{
					if (!isRecycleMQC) // MQC Main Flow
					{
						for (Map<String, Object> map : result)
						{
							String processFlowName = ConvertUtil.getMapValueByName(map, "PROCESSFLOWNAME");
							String processFlowVersion = ConvertUtil.getMapValueByName(map, "PROCESSFLOWVERSION");
							String processOperationName = ConvertUtil.getMapValueByName(map, "PROCESSOPERATIONNAME");
							String processOperationVersion = ConvertUtil.getMapValueByName(map, "PROCESSOPERATIONVERSION");

							MQCPlanDetail oldPlanDetail = ExtendedObjectProxy.getMQCPlanDetailService().selectByKey(false,
									new Object[] { oldJobName, processFlowName, processFlowVersion, processOperationName, processOperationVersion });

							oldPlanDetailList.add(oldPlanDetail);
						}

						for (MQCPlanDetail oldPlanDetail : oldPlanDetailList)
						{
							MQCPlanDetail planDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);
							planDetail.setJobName(jobName);
							planDetail.setLotName(destinationLotData.getKey().getLotName());
							planDetail.setCarrierName(destCarrierName);
							planDetail.setLastEventTime(eventInfo.getEventTime());
							planDetail.setLastEventName(eventInfo.getEventName());
							planDetail.setLastEventUser(eventInfo.getEventUser());
							planDetail.setLastEventComment(eventInfo.getEventComment());
							planDetail.setPosition(actualPosition);

							destPlanDetailList.add(planDetail);

							MQCPlanDetail SrcPlanDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);

							SrcPlanDetail.setLastEventTime(eventInfo.getEventTime());
							SrcPlanDetail.setLastEventName(eventInfo.getEventName());
							SrcPlanDetail.setLastEventUser(eventInfo.getEventUser());
							SrcPlanDetail.setLastEventComment(eventInfo.getEventComment());
							if (isMQCwithReserveEQP)
							{
								String actualPositionforReserveEQP = getActualPositionforReserveEQP(oldJobName, prodList, planDetail.getProcessFlowName(), planDetail.getProcessOperationName());
								SrcPlanDetail.setPosition(actualPositionforReserveEQP);
							}
							else
							{
								SrcPlanDetail.setPosition(modifyPositionforMainMQC);
							}

							srcPlanDetailList.add(SrcPlanDetail);
						}

					}
					else
					// MQC Recycle Flow
					{
						boolean OnlyRecycleFlag = true;

						for (Map<String, Object> map : result)
						{
							String processFlowName = ConvertUtil.getMapValueByName(map, "PROCESSFLOWNAME");
							String processFlowVersion = ConvertUtil.getMapValueByName(map, "PROCESSFLOWVERSION");
							String processOperationName = ConvertUtil.getMapValueByName(map, "PROCESSOPERATIONNAME");
							String processOperationVersion = ConvertUtil.getMapValueByName(map, "PROCESSOPERATIONVERSION");

							ProcessFlowKey processFlowKey = new ProcessFlowKey();
							processFlowKey.setFactoryName(sourceLotData.getFactoryName());
							processFlowKey.setProcessFlowName(processFlowName);
							processFlowKey.setProcessFlowVersion(processFlowVersion);
							ProcessFlow processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);

							if (StringUtils.equalsIgnoreCase(processFlowData.getProcessFlowType(), "MQC"))
							{
								OnlyRecycleFlag = false;
							}

							MQCPlanDetail oldPlanDetail = ExtendedObjectProxy.getMQCPlanDetailService().selectByKey(false,
									new Object[] { oldJobName, processFlowName, processFlowVersion, processOperationName, processOperationVersion });

							oldPlanDetailList.add(oldPlanDetail);
						}

						for (MQCPlanDetail oldPlanDetail : oldPlanDetailList)
						{
							if (OnlyRecycleFlag)
							{
								log.info("Transfer product from SourceLot is Only in Recycle Flow");
								MQCPlanDetail DestPlanDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);

								ProcessFlowKey destProcessFlowKey = new ProcessFlowKey();
								destProcessFlowKey.setFactoryName(destinationLotData.getFactoryName());
								destProcessFlowKey.setProcessFlowName(DestPlanDetail.getProcessFlowName());
								destProcessFlowKey.setProcessFlowVersion(DestPlanDetail.getProcessFlowVersion());
								ProcessFlow DestPlanProcessFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(destProcessFlowKey);

								DestPlanDetail.setJobName(jobName);
								DestPlanDetail.setLotName(destinationLotData.getKey().getLotName());
								DestPlanDetail.setCarrierName(destCarrierName);
								DestPlanDetail.setLastEventTime(eventInfo.getEventTime());
								DestPlanDetail.setLastEventName(eventInfo.getEventName());
								DestPlanDetail.setLastEventUser(eventInfo.getEventUser());
								DestPlanDetail.setLastEventComment(eventInfo.getEventComment());
								// planDetail.setPosition(actualPosition);
								if (StringUtils.equalsIgnoreCase(DestPlanProcessFlowData.getProcessFlowType(), "MQC"))
								{
									// set Positions for MainMQCFlow
									DestPlanDetail.setPosition(modifyDestPositionforMain);
								}
								else
								{
									// set Positions for RecycleFlow
									DestPlanDetail.setPosition(modifyDestPositionforRecycle);
								}

								destPlanDetailList.add(DestPlanDetail);

								MQCPlanDetail SrcPlanDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);

								SrcPlanDetail.setLastEventTime(eventInfo.getEventTime());
								SrcPlanDetail.setLastEventName(eventInfo.getEventName());
								SrcPlanDetail.setLastEventUser(eventInfo.getEventUser());
								SrcPlanDetail.setLastEventComment(eventInfo.getEventComment());
								SrcPlanDetail.setPosition(modifyPositionforRecycle);

								srcPlanDetailList.add(SrcPlanDetail);
							}
							else
							{
								log.info("Transfer product from SourceLot is not Only in Recycle Flow");
								MQCPlanDetail DestPlanDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);

								ProcessFlowKey destProcessFlowKey = new ProcessFlowKey();
								destProcessFlowKey.setFactoryName(destinationLotData.getFactoryName());
								destProcessFlowKey.setProcessFlowName(DestPlanDetail.getProcessFlowName());
								destProcessFlowKey.setProcessFlowVersion(DestPlanDetail.getProcessFlowVersion());
								ProcessFlow DestPlanProcessFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(destProcessFlowKey);

								DestPlanDetail.setJobName(jobName);
								DestPlanDetail.setLotName(destinationLotData.getKey().getLotName());
								DestPlanDetail.setCarrierName(destCarrierName);
								DestPlanDetail.setLastEventTime(eventInfo.getEventTime());
								DestPlanDetail.setLastEventName(eventInfo.getEventName());
								DestPlanDetail.setLastEventUser(eventInfo.getEventUser());
								DestPlanDetail.setLastEventComment(eventInfo.getEventComment());
								if (StringUtils.equalsIgnoreCase(DestPlanProcessFlowData.getProcessFlowType(), "MQC"))
								{
									// set Positions for MainMQCFlow
									DestPlanDetail.setPosition(modifyDestPositionforMain);
								}
								else
								{
									// set Positions for RecycleFlow
									DestPlanDetail.setPosition(modifyDestPositionforRecycle);
								}

								destPlanDetailList.add(DestPlanDetail);

								MQCPlanDetail SrcPlanDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);

								ProcessFlowKey SrcProcessFlowKey = new ProcessFlowKey();
								SrcProcessFlowKey.setFactoryName(sourceLotData.getFactoryName());
								SrcProcessFlowKey.setProcessFlowName(SrcPlanDetail.getProcessFlowName());
								SrcProcessFlowKey.setProcessFlowVersion(SrcPlanDetail.getProcessFlowVersion());
								ProcessFlow SrcPlanProcessFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(SrcProcessFlowKey);

								SrcPlanDetail.setLastEventTime(eventInfo.getEventTime());
								SrcPlanDetail.setLastEventName(eventInfo.getEventName());
								SrcPlanDetail.setLastEventUser(eventInfo.getEventUser());
								SrcPlanDetail.setLastEventComment(eventInfo.getEventComment());
								if (StringUtils.equalsIgnoreCase(SrcPlanProcessFlowData.getProcessFlowType(), "MQC"))
								{
									// set Positions for MainMQCFlow
									if (isMQCwithReserveEQP)
									{
										String actualPositionforReserveEQP = getActualPositionforReserveEQP(oldJobName, prodList, SrcPlanDetail.getProcessFlowName(),
												SrcPlanDetail.getProcessOperationName());
										SrcPlanDetail.setPosition(actualPositionforReserveEQP);
									}
									else
									{
										SrcPlanDetail.setPosition(modifyPositionforMainMQC);
									}
								}
								else
								{
									// set Positions for RecycleFlow
									SrcPlanDetail.setPosition(modifyPositionforRecycle);
								}

								srcPlanDetailList.add(SrcPlanDetail);
							}
						}

					}

					if (destPlanDetailList.size() > 0)
					{
						ExtendedObjectProxy.getMQCPlanDetailService().create(eventInfo, destPlanDetailList);
						log.info(destPlanDetailList.size() + " Rows inserted into CT_MQCPlanDetail");
					}

					if (srcPlanDetailList.size() > 0)
					{
						// Modify Source MQC Job in CT_MQCPlanDetail by Source Transfer ProductList
						try
						{
							for (MQCPlanDetail PlanDetail : srcPlanDetailList)
							{
								ExtendedObjectProxy.getMQCPlanDetailService().modify(eventInfo, PlanDetail);
							}
							log.info(srcPlanDetailList.size() + " Rows Modified into CT_MQCPlanDetail for Source MQC Job");
						}
						catch (Exception ex)
						{
							log.info("Error : Rows Modified into CT_MQCPlanDetail");
						}
					}

					int rows = 0;
					for (String SourceProduct : prodList)
					{
						for (Map<String, Object> map : positionResult)
						{
							String productName = ConvertUtil.getMapValueByName(map, "PRODUCTNAME");
							String position = ConvertUtil.getMapValueByName(map, "POSITION");

							if (productName.equalsIgnoreCase(SourceProduct))
							{
								// Update MQCPlanDetail_Extended
								sql.setLength(0);
								sql.append("INSERT INTO CT_MQCPLANDETAIL_EXTENDED ");
								sql.append("        (JOBNAME, PROCESSFLOWNAME, PROCESSFLOWVERSION, PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION,  ");
								sql.append("         PRODUCTNAME, POSITION, LOTNAME, FORBIDDENCODE, OLDFORBIDDENCODE,  ");
								sql.append("         LASTEVENTNAME, LASTEVENTTIME, LASTEVENTUSER, DUMMYUSEDCOUNT, RECIPENAME,  ");
								sql.append("         MACHINENAME) ");
								sql.append(" (SELECT :JOBNAME, PROCESSFLOWNAME, PROCESSFLOWVERSION, PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION,  ");
								sql.append("         PRODUCTNAME, :POSITION, :LOTNAME, FORBIDDENCODE, OLDFORBIDDENCODE,  ");
								sql.append("         :EVENTNAME, :EVENTTIME, :EVENTUSER, DUMMYUSEDCOUNT, RECIPENAME,  ");
								sql.append("         MACHINENAME FROM CT_MQCPLANDETAIL_EXTENDED ");
								sql.append("   WHERE JOBNAME = :OLDJOBNAME ");
								sql.append("     AND PRODUCTNAME = :PRODUCTNAME ) ");

								args.clear();
								args.put("JOBNAME", jobName);
								args.put("POSITION", position);
								args.put("LOTNAME", destinationLotData.getKey().getLotName());
								args.put("EVENTNAME", eventInfo.getEventName());
								args.put("EVENTTIME", eventInfo.getEventTime().toString());
								args.put("EVENTUSER", eventInfo.getEventUser());
								args.put("OLDJOBNAME", oldJobName);
								args.put("PRODUCTNAME", productName);

								GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), args);
								rows++;
							}
						}
					}
					log.info(rows + " Rows inserted into CT_MQCPlanDetail_Extended");

					// Delete Source MQC Job in MQCPlanDetail_Extended by Source ProductList
					sql.setLength(0);
					sql.append("DELETE FROM CT_MQCPLANDETAIL_EXTENDED ");
					sql.append(" WHERE JOBNAME = :SRCJOBNAME ");
					sql.append("   AND PRODUCTNAME IN ( :SOURCEPRODUCTLIST ) ");

					args.clear();
					args.put("SRCJOBNAME", oldJobName);
					args.put("SOURCEPRODUCTLIST", prodList);

					int delRows = GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), args);
					log.info(delRows + " Rows deleted into CT_MQCPlanDetail_Extended by Soruce MQC Job");
				
					// Lot without MQCJob is sent to MQC Bank.
//					if (!ExistMQCProductbyLot(sourceLotData, oldJobName))
//					{
//						log.info("Source Lot [" + sourceLotData + "] does not have MQC Product.");
//						// EventInfo eventInfo , MQCPlan planData, Lot lotData
//						// After being sent to the MQC bank, Delete MQC Job
//						// changeReturnMQCBank(eventInfo, sourceLotData, oldJobName);
//						sourceLotChangeMQCSpecToBank(sourceLotData, eventInfo);
//						log.info("After being sent to the MQC bank, Delete MQC Job. Source Lot : [" + sourceLotData + "]");
//						MESLotServiceProxy.getLotServiceUtil().removeMQCJob(eventInfo, sourceLotData);
//
//					}

				}
				else
				{
					log.info("Not exist MQC Operation Info by Product" + prodList);
				}
			}
			else
			{
				//Move to Source Lot Track Out Logic to Execute 2021/5/29 xiaoxh
				log.info("Not exist MQC Product Info by Product" + prodList);

				// Lot without data in CT_MQCPLANDETAIL_EXTENDED , change spec
				if (!ExistMQCProductbyLot(destinationLotData))
				{
					changeMQCSpecToBank(sourceLotData, eventInfo, destinationLotData);
				}
			}
		}
		else
		{
			log.info("Not exist Release/Recycling MQC Info by Lot: " + sourceLotData.getKey().getLotName());
		}
	}

	public String getModifyPositionforSourceMQCJob(String ScrMQCJobName, List<String> SrcTransferProductList, boolean isRecycleMQC) throws CustomException
	{
		String modifyPosition = "";

		if (!isRecycleMQC) // Main MQC Flow
		{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT DISTINCT E.POSITION, E.PRODUCTNAME ");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P   ");
			sql.append(" WHERE 1=1 ");
			sql.append("   AND E.JOBNAME = :SRCJOBNAME ");
			sql.append("   AND P.PRODUCTNAME NOT IN ( :SOURCEPRODUCTLIST )   ");
			sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME ");
			sql.append("ORDER BY TO_NUMBER(POSITION) ");

			Map<String, Object> args = new HashMap<String, Object>();
			args.put("SRCJOBNAME", ScrMQCJobName);
			args.put("SOURCEPRODUCTLIST", SrcTransferProductList);

			try
			{
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> positionResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

				if (positionResult.size() > 0)
				{
					log.info("Get Positions Info in CT_MQCPlanDetail by Source Transfer Product");
					List<String> positionList = CommonUtil.makeListBySqlResult(positionResult, "POSITION");
					modifyPosition = CommonUtil.toStringWithoutBrackets(positionList);
				}
			}
			catch (Exception ex)
			{
				log.info("Error : Get Positions Info in CT_MQCPlanDetail by Source Transfer Product : " + ScrMQCJobName);
			}
		}
		else
		// Recycle MQC Flow
		{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT DISTINCT E.POSITION, E.PRODUCTNAME ");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P ");
			sql.append(" WHERE 1 = 1 ");
			sql.append("   AND E.JOBNAME = :SRCJOBNAME ");
			sql.append("   AND P.PRODUCTNAME NOT IN ( :SOURCEPRODUCTLIST) ");
			sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME ");
			sql.append("   AND E.PROCESSFLOWNAME NOT IN (SELECT PROCESSFLOWNAME ");
			sql.append("                                   FROM PROCESSFLOW ");
			sql.append("                                  WHERE PROCESSFLOWTYPE = 'MQCRecycle') ");
			sql.append("ORDER BY TO_NUMBER(POSITION) ");

			Map<String, Object> args = new HashMap<String, Object>();
			args.put("SRCJOBNAME", ScrMQCJobName);
			args.put("SOURCEPRODUCTLIST", SrcTransferProductList);

			try
			{
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> positionResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

				if (positionResult.size() > 0)
				{
					log.info("Get Positions Info in CT_MQCPlanDetail by Source Transfer Product");
					List<String> positionList = CommonUtil.makeListBySqlResult(positionResult, "POSITION");
					modifyPosition = CommonUtil.toStringWithoutBrackets(positionList);
				}
			}
			catch (Exception ex)
			{
				log.info("Error : Get Positions Info in CT_MQCPlanDetail by Source Transfer Product : " + ScrMQCJobName);
			}

		}

		return modifyPosition;
	}

	public String getModifyPositionforSourceRecycleMQCJob(String ScrMQCJobName, List<String> SrcTransferProductList, boolean isRecycleMQC) throws CustomException
	{
		String modifyPosition = "";

		if (isRecycleMQC)
		{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT DISTINCT E.POSITION, E.PRODUCTNAME ");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P ");
			sql.append(" WHERE 1 = 1 ");
			sql.append("   AND E.JOBNAME = :SRCJOBNAME ");
			sql.append("   AND P.PRODUCTNAME NOT IN ( :SOURCEPRODUCTLIST) ");
			sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME ");
			sql.append("   AND E.PROCESSFLOWNAME NOT IN (SELECT PROCESSFLOWNAME ");
			sql.append("                                   FROM PROCESSFLOW ");
			sql.append("                                  WHERE PROCESSFLOWTYPE = 'MQC') ");
			sql.append("ORDER BY TO_NUMBER(POSITION) ");

			Map<String, Object> args = new HashMap<String, Object>();
			args.put("SRCJOBNAME", ScrMQCJobName);
			args.put("SOURCEPRODUCTLIST", SrcTransferProductList);

			try
			{
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> positionResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

				if (positionResult.size() > 0)
				{
					log.info("Get Positions Info in CT_MQCPlanDetail by Source Transfer Product");
					List<String> positionList = CommonUtil.makeListBySqlResult(positionResult, "POSITION");
					modifyPosition = CommonUtil.toStringWithoutBrackets(positionList);
				}
			}
			catch (Exception ex)
			{
				log.info("Error : Get Positions Info in CT_MQCPlanDetail by Source Transfer Product : " + ScrMQCJobName);
			}
		}

		return modifyPosition;
	}

	public String getModifyPositionforDestMQCJob(String SrcMQCJobName, String DestMQCJobName, List<String> SrcTransferProductList, boolean isRecycleMQC) throws CustomException
	{
		String modifyPosition = "";

		if (!isRecycleMQC) // Main MQC Flow
		{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT DISTINCT P.POSITION, P.PRODUCTNAME ");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P   ");
			sql.append(" WHERE 1=1 ");
			sql.append("   AND E.JOBNAME = :SRCJOBNAME ");
			sql.append("   AND P.PRODUCTNAME IN ( :SRCPRODUCTLIST )   ");
			sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME ");
			sql.append("ORDER BY TO_NUMBER(POSITION) ");

			Map<String, Object> args = new HashMap<String, Object>();
			args.put("SRCJOBNAME", SrcMQCJobName);
			args.put("SRCPRODUCTLIST", SrcTransferProductList);

			try
			{
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> positionResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

				if (positionResult.size() > 0)
				{
					log.info("Get Positions Info in CT_MQCPlanDetail by Destination Product");
					List<String> positionList = CommonUtil.makeListBySqlResult(positionResult, "POSITION");
					modifyPosition = CommonUtil.toStringWithoutBrackets(positionList);
				}
			}
			catch (Exception ex)
			{
				log.info("Error : Get Positions Info in CT_MQCPlanDetail by Destination Product [jobName : " + DestMQCJobName + "]");
			}
		}
		else
		// Recycle MQC Flow
		{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT DISTINCT P.POSITION, P.PRODUCTNAME ");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P ");
			sql.append(" WHERE 1 = 1 ");
			sql.append("   AND E.JOBNAME = :SRCJOBNAME ");
			sql.append("   AND P.PRODUCTNAME IN ( :SRCPRODUCTLIST) ");
			sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME ");
			sql.append("   AND E.PROCESSFLOWNAME IN (SELECT PROCESSFLOWNAME ");
			sql.append("                                FROM PROCESSFLOW ");
			sql.append("                               WHERE PROCESSFLOWTYPE = 'MQC') ");
			sql.append("ORDER BY TO_NUMBER(POSITION) ");

			Map<String, Object> args = new HashMap<String, Object>();
			args.put("SRCJOBNAME", SrcMQCJobName);
			args.put("SRCPRODUCTLIST", SrcTransferProductList);

			try
			{
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> positionResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

				if (positionResult.size() > 0)
				{
					log.info("Get Positions Info in CT_MQCPlanDetail by Destination Product");
					List<String> positionList = CommonUtil.makeListBySqlResult(positionResult, "POSITION");
					modifyPosition = CommonUtil.toStringWithoutBrackets(positionList);
				}
			}
			catch (Exception ex)
			{
				log.info("Error : Get Positions Info in CT_MQCPlanDetail by Destination Product [jobName : " + DestMQCJobName + "]");
			}

		}

		return modifyPosition;
	}

	public String getModifyPositionforDestRecycleMQCJob(String SrcMQCJobName, String DestMQCJobName, List<String> SrcTransferProductList, boolean isRecycleMQC) throws CustomException
	{
		String modifyPosition = "";

		if (isRecycleMQC)
		{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT DISTINCT P.POSITION, P.PRODUCTNAME ");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P ");
			sql.append(" WHERE 1 = 1 ");
			sql.append("   AND E.JOBNAME = :SRCJOBNAME ");
			sql.append("   AND P.PRODUCTNAME IN ( :SRCPRODUCTLIST) ");
			sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME ");
			sql.append("   AND E.PROCESSFLOWNAME NOT IN (SELECT PROCESSFLOWNAME ");
			sql.append("                                   FROM PROCESSFLOW ");
			sql.append("                                  WHERE PROCESSFLOWTYPE = 'MQC') ");
			sql.append("ORDER BY TO_NUMBER(POSITION) ");

			Map<String, Object> args = new HashMap<String, Object>();
			args.put("SRCJOBNAME", SrcMQCJobName);
			args.put("SRCPRODUCTLIST", SrcTransferProductList);

			try
			{
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> positionResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

				if (positionResult.size() > 0)
				{
					log.info("Get Positions Info in CT_MQCPlanDetail by Dest Product");
					List<String> positionList = CommonUtil.makeListBySqlResult(positionResult, "POSITION");
					modifyPosition = CommonUtil.toStringWithoutBrackets(positionList);
				}
			}
			catch (Exception ex)
			{
				log.info("Error : Get Positions Info in CT_MQCPlanDetail by Dest Product [jobName : " + DestMQCJobName + "]");
			}
		}

		return modifyPosition;
	}

	public boolean isRecycleMQC(String sourceMQCJobName) throws CustomException
	{
		List<MQCPlan> mqcPlanData = ExtendedObjectProxy.getMQCPlanService().getMQCPlanDataByJobName(sourceMQCJobName);

		if (mqcPlanData != null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	public List<String> getProductListByElementList(List<Element> eleDestProductList) throws CustomException
	{
		List<String> resultList = new ArrayList<>();

		for (Element eleProduct : eleDestProductList)
		{
			String productName = SMessageUtil.getChildText(eleProduct, "PRODUCTNAME", true);

			if (StringUtils.isNotEmpty(productName))
			{
				resultList.add(productName);
			}
		}

		return resultList;
	}

	public void changeReturnMQCBank(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		Map<String, Object> args = new HashMap<String, Object>();

		List<MQCPlan> mqcPlanData = ExtendedObjectProxy.getMQCPlanService().getMQCPlanDataByLotNameMultiState(lotData.getKey().getLotName());

		if (mqcPlanData != null)
		{
			String jobName = mqcPlanData.get(0).getJobName();

			String currentNode = lotData.getNodeStack();
			String originalNode = currentNode.substring(0, currentNode.lastIndexOf("."));

			sql.setLength(0);
			sql.append("SELECT NODEID, PROCESSFLOWNAME, PROCESSFLOWVERSION, NODEATTRIBUTE1, NODEATTRIBUTE2, FACTORYNAME ");
			sql.append("  FROM NODE ");
			sql.append(" WHERE NODEID = :NODEID ");
			sql.append("   AND NODETYPE = 'ProcessOperation' ");

			args.clear();
			args.put("NODEID", originalNode);

			List<Map<String, Object>> orginalNodeResult = null;
			try
			{
				orginalNodeResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
			}
			catch (Exception e)
			{
				log.info("Error occured - There is no ProcessFlow/ProcessOperation information for the organalNode.");
			}

			if (orginalNodeResult.size() > 0)
			{
				lotData.setNodeStack(originalNode);
				lotData.setProcessFlowName(ConvertUtil.getMapValueByName(orginalNodeResult.get(0), "PROCESSFLOWNAME"));
				lotData.setProcessFlowVersion(ConvertUtil.getMapValueByName(orginalNodeResult.get(0), "PROCESSFLOWVERSION"));
				lotData.setProcessOperationName(ConvertUtil.getMapValueByName(orginalNodeResult.get(0), "NODEATTRIBUTE1"));
				lotData.setProcessOperationVersion(ConvertUtil.getMapValueByName(orginalNodeResult.get(0), "NODEATTRIBUTE2"));
			}
			else
			{
				log.info("Error occured - There is no ProcessFlow/ProcessOperation information for the organalNode.");
			}

			MQCPlan planData = ExtendedObjectProxy.getMQCPlanService().selectByKey(false, new Object[] { jobName });

			String FirstOperationName = "";
			String FirstOperationVer = "";

			Node targetNode = MESLotServiceProxy.getLotServiceUtil().getFirstNode(planData.getJobName(), planData.getFactoryName(), planData.getProcessFlowName(), planData.getProcessFlowVersion());

			if (targetNode == null)
			{
				FirstOperationName = planData.getReturnOperationName();
				FirstOperationVer = planData.getReturnOperationVersion();
			}
			else
			{
				FirstOperationName = targetNode.getNodeAttribute1();
				FirstOperationVer = targetNode.getNodeAttribute2();
			}

			try
			{
				if (StringUtils.isNotEmpty(planData.getPrepareSpecName()) && StringUtils.isNotEmpty(planData.getPrepareSpecVersion()))
				{
					// ChangeSpec
					ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo(lotData.getProductionType(), planData.getPrepareSpecName(), planData.getPrepareSpecVersion(), lotData.getProductSpec2Name(),
							lotData.getProductSpec2Version(), lotData.getProductRequestName(), lotData.getSubProductUnitQuantity1(), lotData.getSubProductUnitQuantity2(), lotData.getDueDate(),
							lotData.getPriority(), lotData.getFactoryName(), lotData.getAreaName(), lotData.getLotState(), lotData.getLotProcessState(), lotData.getLotHoldState(),
							lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), lotData.getNodeStack(),
							new ArrayList<ProductU>());

					changeSpecInfo.setUdfs(lotData.getUdfs());

					eventInfo.setEventName("SuspendMQC");
					MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);

					eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
				}

				// ChangeSpec: Return to MQC PrepareSpec or Not
				eventInfo.setEventName("SuspendMQC");
				ExtendedObjectProxy.getMQCPlanService().updateMQCPlanReturnOper(eventInfo, planData, "Suspending", FirstOperationName, FirstOperationVer);
			}
			catch (Exception e)
			{
				log.info("Error occured - Suspending MQC Lot for Split Lot");
			}

		}
	}

	public void changeReturnMQCBank(EventInfo eventInfo, Lot lotData, String jobName) throws CustomException
	{
		String currentNode = lotData.getNodeStack();
		String originalNode = currentNode.substring(0, currentNode.lastIndexOf("."));

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT NODEID, PROCESSFLOWNAME, PROCESSFLOWVERSION, NODEATTRIBUTE1, NODEATTRIBUTE2, FACTORYNAME ");
		sql.append("  FROM NODE ");
		sql.append(" WHERE NODEID = :NODEID ");
		sql.append("   AND NODETYPE = 'ProcessOperation' ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("NODEID", originalNode);

		List<Map<String, Object>> orginalNodeResult = null;
		try
		{
			orginalNodeResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		}
		catch (Exception e)
		{
			log.info("Error occured - There is no ProcessFlow/ProcessOperation information for the organalNode.");
		}

		if (orginalNodeResult.size() > 0)
		{
			lotData.setNodeStack(originalNode);
			lotData.setProcessFlowName(ConvertUtil.getMapValueByName(orginalNodeResult.get(0), "PROCESSFLOWNAME"));
			lotData.setProcessFlowVersion(ConvertUtil.getMapValueByName(orginalNodeResult.get(0), "PROCESSFLOWVERSION"));
			lotData.setProcessOperationName(ConvertUtil.getMapValueByName(orginalNodeResult.get(0), "NODEATTRIBUTE1"));
			lotData.setProcessOperationVersion(ConvertUtil.getMapValueByName(orginalNodeResult.get(0), "NODEATTRIBUTE2"));
		}
		else
		{
			log.info("Error occured - There is no ProcessFlow/ProcessOperation information for the organalNode.");
		}

		MQCPlan planData = ExtendedObjectProxy.getMQCPlanService().selectByKey(false, new Object[] { jobName });

		String FirstOperationName = "";
		String FirstOperationVer = "";

		Node targetNode = MESLotServiceProxy.getLotServiceUtil().getFirstNode(planData.getJobName(), planData.getFactoryName(), planData.getProcessFlowName(), planData.getProcessFlowVersion());

		if (targetNode == null)
		{
			FirstOperationName = planData.getReturnOperationName();
			FirstOperationVer = planData.getReturnOperationVersion();
		}
		else
		{
			FirstOperationName = targetNode.getNodeAttribute1();
			FirstOperationVer = targetNode.getNodeAttribute2();
		}

		try
		{
			if (StringUtils.isNotEmpty(planData.getPrepareSpecName()) && StringUtils.isNotEmpty(planData.getPrepareSpecVersion()))
			{
				// ChangeSpec
				ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo(lotData.getProductionType(), planData.getPrepareSpecName(), planData.getPrepareSpecVersion(), lotData.getProductSpec2Name(),
						lotData.getProductSpec2Version(), lotData.getProductRequestName(), lotData.getSubProductUnitQuantity1(), lotData.getSubProductUnitQuantity2(), lotData.getDueDate(),
						lotData.getPriority(), lotData.getFactoryName(), lotData.getAreaName(), lotData.getLotState(), lotData.getLotProcessState(), lotData.getLotHoldState(),
						lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), lotData.getNodeStack(),
						new ArrayList<ProductU>());

				changeSpecInfo.setUdfs(lotData.getUdfs());

				eventInfo.setEventName("SuspendMQC");
				MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);

				eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
			}

			// ChangeSpec: Return to MQC PrepareSpec or Not
			eventInfo.setEventName("SuspendMQC");
			ExtendedObjectProxy.getMQCPlanService().updateMQCPlanReturnOper(eventInfo, planData, "Suspending", FirstOperationName, FirstOperationVer);
		}
		catch (Exception e)
		{
			log.info("Error occured - Suspending MQC Lot for Split Lot");
		}
	}

	public boolean ExistMQCProductbyLot(Lot LotData, String JobName) throws CustomException
	{
		boolean ExistJobfiag = false;

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT E.PRODUCTNAME  ");
		sql.append("  FROM CT_MQCPLAN M, CT_MQCPLANDETAIL D, CT_MQCPLANDETAIL_EXTENDED E    ");
		sql.append(" WHERE MQCSTATE IN ('Released','Recycling')   ");
		sql.append("   AND M.JOBNAME = D.JOBNAME  ");
		sql.append("   AND D.JOBNAME = E.JOBNAME  ");
		sql.append("   AND M.JOBNAME = E.JOBNAME ");
		sql.append("   AND M.LOTNAME = :LOTNAME  ");
		sql.append("   AND M.JOBNAME = :JOBNAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("LOTNAME", LotData.getKey().getLotName());
		args.put("JOBNAME", JobName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			ExistJobfiag = true;
		}

		return ExistJobfiag;
	}

	public boolean isMQCwithReserveEQP(String sourceMQCJobName)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT M.JOBNAME  ");
		sql.append("  FROM CT_MQCPLAN M, CT_MQCPLANDETAIL D   ");
		sql.append(" WHERE MQCSTATE IN ('Released','Recycling')  ");
		sql.append("   AND M.JOBNAME = D.JOBNAME ");
		sql.append("   AND D.MACHINENAME IS NOT NULL ");
		sql.append("   AND D.RECIPENAME IS NOT NULL ");
		sql.append("   AND M.JOBNAME = :JOBNAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("JOBNAME", sourceMQCJobName);

		try
		{
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (result.size() > 0)
			{
				return true;
			}
		}
		catch (Exception ex)
		{
			log.info("Error : isMQCwithReserveEQP Check Query LotName : " + sourceMQCJobName);
			return false;
		}

		return false;
	}

	public String getActualPositionforReserveEQP(String ScrMQCJobName, List<String> SrcTransferProductList, String ProcessFlowName, String ProcessOperationName) throws CustomException
	{
		String modifyPosition = "";

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT P.POSITION, P.PRODUCTNAME   ");
		sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P   ");
		sql.append(" WHERE E.PRODUCTNAME = P.PRODUCTNAME  ");
		sql.append("   AND E.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND E.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME   ");
		sql.append("   AND E.JOBNAME = :SRCJOBNAME   ");
		sql.append("   AND P.PRODUCTNAME NOT IN ( :SOURCEPRODUCTLIST )   ");
		sql.append("ORDER BY TO_NUMBER(POSITION)  ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("SRCJOBNAME", ScrMQCJobName);
		args.put("SOURCEPRODUCTLIST", SrcTransferProductList);
		args.put("PROCESSFLOWNAME", ProcessFlowName);
		args.put("PROCESSOPERATIONNAME", ProcessOperationName);

		try
		{
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> positionResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (positionResult.size() > 0)
			{
				log.info("Get Positions Info in CT_MQCPlanDetail by Source Transfer Product");
				List<String> positionList = CommonUtil.makeListBySqlResult(positionResult, "POSITION");
				modifyPosition = CommonUtil.toStringWithoutBrackets(positionList);
			}
		}
		catch (Exception ex)
		{
			log.info("Error : Get Positions Info in CT_MQCPlanDetail by Source Transfer Product [jobName : " + ScrMQCJobName + "]");
		}

		return modifyPosition;
	}

	public void removeMQCJob(EventInfo eventInfo, Lot sourceLotData) throws CustomException
	{
		List<MQCPlan> jobList = null;

		try
		{
			jobList = ExtendedObjectProxy.getMQCPlanService().select(" LotName = ? ", new Object[] { sourceLotData.getKey().getLotName() });
		}
		catch (Exception e)
		{
		}

		if (jobList != null)
		{
			for (MQCPlan planData : jobList)
			{
				String jobName = planData.getJobName();
				eventInfo = EventInfoUtil.makeEventInfo("RemoveMQC", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);

				log.info(" Start delete MQCJob from  CT_MQCPLANDETAIL_EXTENDED");
				ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().deleteMQCPlanDetail_ExtendedByJobName(jobName);

				log.info(" End delete MQCJob from  CT_MQCPLANDETAIL_EXTENDED");

				List<MQCPlanDetail> DetailPlanList;
				try
				{
					DetailPlanList = ExtendedObjectProxy.getMQCPlanDetailService().select("jobName = ?", new Object[] { jobName });
				}
				catch (Exception ex)
				{
					log.error("No details for this MQC plan");
					DetailPlanList = new ArrayList<MQCPlanDetail>();
				}

				// purge dependent plans
				for (MQCPlanDetail detailPlanData : DetailPlanList)
				{
					ExtendedObjectProxy.getMQCPlanDetailService().deleteMQCPlanDetail(eventInfo, detailPlanData);
				}

				ExtendedObjectProxy.getMQCPlanService().deleteMQCPlanData(eventInfo, planData);

				try
				{
					SetEventInfo setEventInfo = MESLotServiceProxy.getLotInfoUtil().setEventInfo(sourceLotData, 0, new ArrayList<ProductU>());
					LotServiceProxy.getLotService().setEvent(sourceLotData.getKey(), eventInfo, setEventInfo);
				}
				catch (Exception e)
				{
				}
			}
		}
	}

	public List<SampleLot> setSamplingData(EventInfo eventInfo, Lot lotData, Map<String, Object> samplePolicy, List<Element> productListElement) throws CustomException
	{
		boolean allScrapflag = false;
		String sampleFlag = "N";

		log.info("Start setSamplingData: PROCESSFLOWNAME(" + lotData.getProcessFlowName() + "),PROCESSOPERNAME(" + lotData.getProcessOperationName() + "),TOPROCESSFLOWNAME("
				+ (String) samplePolicy.get("TOPROCESSFLOWNAME") + "),TOPROCESSOPERATIONNAME(" + (String) samplePolicy.get("TOPROCESSOPERATIONNAME") + ")");

		List<String> positionList = CommonUtil.makeList(productListElement, "POSITION");
		
		// ////////////////////////////////////////////////////////////////////////////////////////////////
		// ///////////////////// Synchronize LotSampleCount //////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////////////////////////////////

		// Get SampleLotData(CT_SAMPLELOT)
		List<SampleLot> sampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataByPolicy(lotData, samplePolicy);

		// Get SamplingLotCount(CT_SAMPLELOTCOUNT)
		List<SampleLotCount> sampleLotCount = ExtendedObjectProxy.getSampleLotCountService().getSampleLotCountByPolicy(lotData, samplePolicy);

		// Synchronize LotSampleCount with SamplePolicy
		if (sampleLotCount != null)
			ExtendedObjectProxy.getSampleLotCountService().updateSampleLotCountDataByPolicy(lotData, samplePolicy, sampleLotCount);
		else
			ExtendedObjectProxy.getSampleLotCountService().insertSampleLotCountByPolicy(lotData, samplePolicy);

		// Research
		sampleLotCount = ExtendedObjectProxy.getSampleLotCountService().getSampleLotCountByPolicy(lotData, samplePolicy);

		double lotSampleCount = Double.parseDouble(sampleLotCount.get(0).getLotSampleCount());
		double currentLotCount = Double.parseDouble(sampleLotCount.get(0).getCurrentLotCount());
		double totalLotCount = Double.parseDouble(sampleLotCount.get(0).getTotalLotCount());

		if (currentLotCount % lotSampleCount == 0)
		{
			currentLotCount = 1;
			totalLotCount++;
			sampleFlag = "Y";
		}
		else
		{
			currentLotCount = currentLotCount + 1;
			totalLotCount++;
		}

		// Set SampleLotCount(CT_SAMPLELOTCOUNT)
		ExtendedObjectProxy.getSampleLotCountService().updateSampleLotCountDataByPolicy(lotData, samplePolicy, sampleLotCount, currentLotCount, totalLotCount);

		// ////////////////////////////////////////////////////////////////////////////////////////////////
		// ///////////////////////// Set LotSampleData ///////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////////////////////////////////

		// Insert Sample Lot EventInfo
		EventInfoExtended sampleEventInfo = new EventInfoExtended(eventInfo);
		EventInfoExtended tptjEventInfo = new EventInfoExtended(eventInfo);

		if (sampleLot == null)
		{
			List<String> TPTJProductList = new ArrayList<String>();
			TPTJRule TPTJRuleData = new TPTJRule();

			boolean TPTJCheckFlag = false;
			boolean TPTJInsertFlag = false;

			// TPTJ Rule Search.
			List<TPTJRule> TPTJRuleDataList = ExtendedObjectProxy.getTPTJRuleService().getTPTJRuleData(lotData, samplePolicy);

			// Samping Validation
			if (!StringUtils.equals(sampleFlag, "Y"))
			{
				if (TPTJRuleDataList != null)
					this.setTPTJSampleDataWhenNoSampleCount(tptjEventInfo, samplePolicy, TPTJRuleDataList, productListElement, positionList, lotData, sampleLotCount, currentLotCount, totalLotCount);

				return sampleLot;
			}

			if (TPTJRuleDataList != null)
			{
				for (TPTJRule TPTJRule : TPTJRuleDataList)
				{
					log.info(" TPTJ Rule Name = " + TPTJRule.getRuleName());
					TPTJRuleData = TPTJRule;

					List<TPTJProduct> TPTJProList = ExtendedObjectProxy.getTPTJProductService().getTPTJProductListByLotFlow(lotData, TPTJRule);

					if (TPTJProList != null)
					{
						for (TPTJProduct TPTJProductData : TPTJProList)
							TPTJProductList.add(TPTJProductData.getProductName());

						TPTJCheckFlag = true;
					}
					else
					{
						if(StringUtils.equals(TPTJRule.getFirstFlowFlag(), "Y"))
						{
							TPTJInsertFlag = true;
						}
					}

					break;
				}
			}

			// Parcing the 'PRODUCTSAMPLEPOSITION'
			String productSamplePositions = this.getProductSamplePositions(lotData, samplePolicy, positionList);

			List<String> productSamplePositionList = CommonUtil.splitString(",", productSamplePositions); // delimeter is ','
			List<String> exceptBackpPositionList = CommonUtil.copyToStringList(productSamplePositionList);
			List<String> actualSamplePositionList = new ArrayList<String>();

			// Set productSampingData
			if (TPTJCheckFlag)
			{// TPTJ Start
				actualSamplePositionList = this.setTPTJProductSamplingData(sampleEventInfo, tptjEventInfo, productListElement, lotData, TPTJRuleData, samplePolicy, TPTJProductList,
						productSamplePositionList, exceptBackpPositionList, sampleFlag, allScrapflag);
			}
			else
			{// Normal Start
				actualSamplePositionList = this.setNormalProductSampingData(sampleEventInfo, tptjEventInfo, productListElement, lotData, TPTJRuleData, samplePolicy, productSamplePositionList,
						exceptBackpPositionList, sampleFlag, TPTJInsertFlag, allScrapflag);
			}

			if (allScrapflag)
			{
				if (sampleEventInfo.getEventComment().length() > 200)
				{
					sampleEventInfo.getEventComment().substring(0, 201);
				}
				sampleEventInfo.setEventComment("[Sample001]System sample all scrapped. " + sampleEventInfo.getEventComment());
			}
			else
			{
				sampleEventInfo.setEventComment("[Sample000]System sample Normal. " + sampleEventInfo.getEventComment());
			}

			ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);

			// for BackUpFlow
			if (StringUtils.equals(processFlowData.getProcessFlowType(), "BackUp"))
			{
				// set SampleLotData(CT_SAMPLELOT) for backupFlow
				ExtendedObjectProxy.getSampleLotService().insertSampleLotForBackUp(tptjEventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
						lotData.getProductSpecVersion(), lotData.getUdfs().get("BACKUPMAINFLOWNAME"), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
						lotData.getMachineName(), (String) samplePolicy.get("TOPROCESSFLOWNAME"), (String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"),
						(String) samplePolicy.get("TOPROCESSOPERATIONVERSION"), sampleFlag, sampleLotCount.get(0).getLotSampleCount(), CommonUtil.StringValueOf(currentLotCount),
						CommonUtil.StringValueOf(totalLotCount), (String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"),
						String.valueOf(actualSamplePositionList.size()), CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "", "",
						Integer.valueOf(samplePolicy.get("FLOWPRIORITY").toString()), lotData.getUdfs().get("BACKUPMAINFLOWNAME") , lotData.getProcessFlowVersion(),
						(String) samplePolicy.get("RETURNOPERATIONNAME"), (String) samplePolicy.get("RETURNOPERATIONVER"), "" , "Y");
			}
			else
			{
				// set SampleLotData(CT_SAMPLELOT)
				ExtendedObjectProxy.getSampleLotService().insertSampleLot(tptjEventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
						lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
						lotData.getMachineName(), (String) samplePolicy.get("TOPROCESSFLOWNAME"), (String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"),
						(String) samplePolicy.get("TOPROCESSOPERATIONVERSION"), sampleFlag, sampleLotCount.get(0).getLotSampleCount(), CommonUtil.StringValueOf(currentLotCount),
						CommonUtil.StringValueOf(totalLotCount), (String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"),
						String.valueOf(actualSamplePositionList.size()), CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "", "",
						Integer.valueOf(samplePolicy.get("FLOWPRIORITY").toString()), lotData.getProcessFlowName() , lotData.getProcessFlowVersion(),
						(String) samplePolicy.get("RETURNOPERATIONNAME"), (String) samplePolicy.get("RETURNOPERATIONVER"), "");
			}

			// Refresh SampleLotData
			sampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataByPolicy(lotData, samplePolicy);
		}
		else
		{
			log.info("Start integration Sampling");

			if (!StringUtils.equals(sampleFlag, "Y"))
				return sampleLot;

			String oldPosition = sampleLot.get(0).getActualSamplePosition();
			List<String> oldProductSamplePositionList = new ArrayList<String>();

			if (oldPosition == null || oldPosition.trim().length() == 0)
			{
				oldPosition = sampleLot.get(0).getProductSamplePosition();

				if (oldPosition == null || oldPosition.trim().length() == 0)
				{
					log.info("Get Sample oldPosition faild ");
					return sampleLot;
				}

				oldProductSamplePositionList = CommonUtil.splitString(",", oldPosition); // delimeter is ','
			}
			else
			{
				oldProductSamplePositionList = CommonUtil.splitString(", ", oldPosition); // delimeter is ', '
			}

			String productSamplePositions = (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION");

			// Random Sampling Position
			if (productSamplePositions.equals("Random"))
			{
				List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());

				if (productList.size() < Integer.valueOf((String) samplePolicy.get("PRODUCTSAMPLINGCOUNT")))
				{
					productSamplePositions = "ALL";
				}
				else
				{
					Collections.shuffle(productList);
					String temp = "";
					for (int i = 0; i < Integer.valueOf((String) samplePolicy.get("PRODUCTSAMPLINGCOUNT")); i++)
					{
						String position = productList.get(i).getPosition() + ",";
						temp += position;
					}
					productSamplePositions = temp.substring(0, (temp.length() - 1));
				}
			}

			productSamplePositions = productSamplePositions.replaceAll(" ", ""); // remove blank
			if (StringUtils.equals(productSamplePositions, "All") || StringUtils.equals(productSamplePositions, "ALL"))
			{
				productSamplePositions = CommonUtil.makeProductSamplingPositionList(Integer.valueOf((String) samplePolicy.get("PRODUCTSAMPLINGCOUNT")));
			}

			List<String> productSamplePositionList = CommonUtil.splitString(",", productSamplePositions); // delimeter is ','

			productSamplePositionList.removeAll(oldProductSamplePositionList);
			productSamplePositionList.addAll(oldProductSamplePositionList);
			Collections.sort(productSamplePositionList, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2)
				{
					Integer d1 = Integer.parseInt(o1);
					Integer d2 = Integer.parseInt(o2);

					return d1.compareTo(d2);
				}
			});
			if (productSamplePositionList.equals(oldProductSamplePositionList))
			{
				log.info("The integration Position as same as DB Position");
				return sampleLot;
			}

			log.info("Get integration Position Success");
			
			 List<SampleProduct> sampleProductList=ExtendedObjectProxy.getSampleProductService().getSampleProductDataListByLotNameAndToInfo(lotData.getKey().getLotName(), lotData.getFactoryName(),
					 lotData.getProductSpecName(), lotData.getProductSpecVersion(), sampleLot.get(0).getToProcessFlowName(), sampleLot.get(0).getToProcessFlowVersion(),
					 sampleLot.get(0).getToProcessOperationName(), sampleLot.get(0).getToProcessOperationVersion());

			Lot deleteSampleLotData = new Lot();
			deleteSampleLotData.setKey(lotData.getKey());
			deleteSampleLotData.setFactoryName(lotData.getFactoryName());
			deleteSampleLotData.setProductSpecName(lotData.getProductSpecName());
			deleteSampleLotData.setProductSpecVersion(lotData.getProductSpecVersion());
			deleteSampleLotData.setProcessFlowName(sampleLot.get(0).getToProcessFlowName());
			deleteSampleLotData.setProcessFlowVersion(sampleLot.get(0).getToProcessFlowVersion());
			deleteSampleLotData.setProcessOperationName(sampleLot.get(0).getToProcessOperationName());
			deleteSampleLotData.setProcessOperationVersion(sampleLot.get(0).getToProcessOperationVersion());
			MESLotServiceProxy.getLotServiceUtil().deleteSamplingData(eventInfo, deleteSampleLotData, new ArrayList<Element>(), true);

			List<String> actualSamplePositionList = new ArrayList<String>();

			for (String productSamplePosition : productSamplePositionList)
			{
				// get SamplingProduct Data
				String sql = " SELECT PRODUCTNAME FROM PRODUCT WHERE LOTNAME = :lotName AND POSITION = :position ";
				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("lotName", lotData.getKey().getLotName());
				bindMap.put("position", productSamplePosition);

				@SuppressWarnings("unchecked")
				List<Map<String, Object>> sampleProductResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

				if (sampleProductResult.size() > 0) // Exist the Product for productSamplePosition
				{
					String oldmanualSampleFlag="";
					String oldforceSamplingFlag="";
					String olddepartment="";
					String oldproductSampleCount="";
					String oldproductSamplePosition="";
					String oldReasonCode="";
					for(SampleProduct sampleProductInfo:sampleProductList)
					{
						if(StringUtils.equals((String) sampleProductResult.get(0).get("PRODUCTNAME"), sampleProductInfo.getProductName()))
						{
							oldmanualSampleFlag=sampleProductInfo.getManualSampleFlag();
							oldforceSamplingFlag=sampleProductInfo.getForceSamplingFlag();
							olddepartment=sampleProductInfo.getDepartment();
							oldproductSampleCount=sampleProductInfo.getProductSampleCount();
							oldproductSamplePosition=sampleProductInfo.getProductSamplePosition();
							//oldReasonCode=sampleProductInfo.getReasonCode();
							break;
						}
					}
					// set SamplingProduct Data(CT_SAMPLEPRODUCT)
					ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, (String) sampleProductResult.get(0).get("PRODUCTNAME"), lotData.getKey().getLotName(),
							lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
							lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), lotData.getMachineName(), (String) samplePolicy.get("TOPROCESSFLOWNAME"),
							(String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"), (String) samplePolicy.get("TOPROCESSOPERATIONVERSION"), sampleFlag,
							(String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"), productSamplePosition, "", oldmanualSampleFlag,oldforceSamplingFlag,olddepartment,oldReasonCode);

					actualSamplePositionList.add(productSamplePosition);
				}
			}

			sampleEventInfo.setEventComment("[Sample002]Manual sample exist. " + sampleEventInfo.getEventComment());

			ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);

			// for BackUpFlow
			if (StringUtils.equals(processFlowData.getProcessFlowType(), "BackUp"))
			{
				// set SampleLotData(CT_SAMPLELOT) for backupFlow
				ExtendedObjectProxy.getSampleLotService().insertSampleLotForBackUp(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
						lotData.getProductSpecVersion(), lotData.getUdfs().get("BACKUPMAINFLOWNAME"), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
						lotData.getMachineName(), (String) samplePolicy.get("TOPROCESSFLOWNAME"), (String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"),
						(String) samplePolicy.get("TOPROCESSOPERATIONVERSION"), sampleFlag, sampleLotCount.get(0).getLotSampleCount(), CommonUtil.StringValueOf(currentLotCount),
						CommonUtil.StringValueOf(totalLotCount), (String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"),
						String.valueOf(actualSamplePositionList.size()), CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "", "",
						Integer.valueOf(ConvertUtil.getMapValueByName(samplePolicy, "FLOWPRIORITY")), lotData.getUdfs().get("BACKUPMAINFLOWNAME") , lotData.getProcessFlowVersion(),
						(String) samplePolicy.get("RETURNOPERATIONNAME"), (String) samplePolicy.get("RETURNOPERATIONVER"), "", "Y");
			}
			else
			{
				// set SampleLotData(CT_SAMPLELOT)
				ExtendedObjectProxy.getSampleLotService().insertSampleLot(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
						lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
						lotData.getMachineName(), (String) samplePolicy.get("TOPROCESSFLOWNAME"), (String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"),
						(String) samplePolicy.get("TOPROCESSOPERATIONVERSION"), sampleFlag, sampleLotCount.get(0).getLotSampleCount(), CommonUtil.StringValueOf(currentLotCount),
						CommonUtil.StringValueOf(totalLotCount), (String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"),
						String.valueOf(actualSamplePositionList.size()), CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "", "",
						Integer.valueOf(ConvertUtil.getMapValueByName(samplePolicy, "FLOWPRIORITY")), lotData.getProcessFlowName() , lotData.getProcessFlowVersion(),
						(String) samplePolicy.get("RETURNOPERATIONNAME"), (String) samplePolicy.get("RETURNOPERATIONVER"), "");
			}
			
			sampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataList(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
					lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
					lotData.getMachineName(), (String) samplePolicy.get("TOPROCESSFLOWNAME"), (String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"),
					(String) samplePolicy.get("TOPROCESSOPERATIONVERSION"));

		}

		return sampleLot;
	}
	
	public List<SampleLot> setSamplingDataForLTT(EventInfo eventInfo, Lot lotData, Map<String, Object> samplePolicy, List<Element> productListElement,Machine machineData,
			String detailOperationType,ELACondition elaCondition,String LTTSamplingFlag) throws CustomException
	{	
		boolean allScrapflag = false;
		String sampleFlag = "N";
		String earlyFlag=elaCondition.getEarlyFlag();
		String midFlag=elaCondition.getMidFlag();
		String lateFlag=elaCondition.getLateFlag();

		log.info("Start setSamplingData: PROCESSFLOWNAME(" + lotData.getProcessFlowName() + "),PROCESSOPERNAME(" + lotData.getProcessOperationName() + "),TOPROCESSFLOWNAME("
				+ (String) samplePolicy.get("TOPROCESSFLOWNAME") + "),TOPROCESSOPERATIONNAME(" + (String) samplePolicy.get("TOPROCESSOPERATIONNAME") + ")");
		
		log.info("EarlyFlag="+earlyFlag+",MidFlag="+lateFlag+",LateFlag="+lateFlag);

		List<String> positionList = CommonUtil.makeList(productListElement, "POSITION");
		
		// ////////////////////////////////////////////////////////////////////////////////////////////////
		// ///////////////////// Synchronize LotSampleCount //////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////////////////////////////////

		// Get SampleLotData(CT_SAMPLELOT)
		List<SampleLot> sampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataByPolicy(lotData, samplePolicy);
		
		log.info("ELA Machine->LTT DetailOperationTypeCheck");
		
		String sqlForValidation= "SELECT * FROM ENUMDEFVALUE A "
				+ "WHERE A.ENUMNAME='LTTMappling' "
				+ "AND A.ENUMVALUE=:ENUMVALUE  "
				+ "AND A.DESCRIPTION=:DESCRIPTION ";
	
		
		Map<String, Object> bindMapForValidation = new HashMap<String, Object>();
		bindMapForValidation.put("ENUMVALUE",machineData.getKey().getMachineName());
		bindMapForValidation.put("DESCRIPTION",detailOperationType);
		
		List<Map<String, Object>> LTTMappling;
		try
		{
			LTTMappling = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlForValidation, bindMapForValidation);
		}
		catch (FrameworkErrorSignal fe)
		{
			return sampleLot;
		}
		
		if(LTTMappling==null||LTTMappling.size()<1)
		{
			return sampleLot;
		}
		//every stage only one CST
		if(StringUtils.equals(LTTSamplingFlag, "Early")&&StringUtils.isEmpty(earlyFlag))
		{
			sampleFlag="Y";
			elaCondition.setEarlyFlag("True");
			elaCondition.setMidFlag("");
			elaCondition.setLateFlag("");
		}
		else if(StringUtils.equals(LTTSamplingFlag, "Mid")&&StringUtils.isEmpty(midFlag))
		{
			sampleFlag="Y";
			elaCondition.setEarlyFlag("");
			elaCondition.setMidFlag("True");
			elaCondition.setLateFlag("");
		}
		else if(StringUtils.equals(LTTSamplingFlag, "Late")&&StringUtils.isEmpty(lateFlag))
		{
			sampleFlag="Y";
			elaCondition.setEarlyFlag("");			
			elaCondition.setMidFlag("");
			elaCondition.setLateFlag("True");
		}
		else 
		{
			return sampleLot;
		}
		ExtendedObjectProxy.getELAConditionService().update(elaCondition);		

		// Get SamplingLotCount(CT_SAMPLELOTCOUNT)
		List<SampleLotCount> sampleLotCount = ExtendedObjectProxy.getSampleLotCountService().getSampleLotCountByPolicy(lotData, samplePolicy);

		// Synchronize LotSampleCount with SamplePolicy
		if (sampleLotCount != null)
			ExtendedObjectProxy.getSampleLotCountService().updateSampleLotCountDataByPolicy(lotData, samplePolicy, sampleLotCount);
		else
			ExtendedObjectProxy.getSampleLotCountService().insertSampleLotCountByPolicy(lotData, samplePolicy);

		// Research
		sampleLotCount = ExtendedObjectProxy.getSampleLotCountService().getSampleLotCountByPolicy(lotData, samplePolicy);


		double currentLotCount = Double.parseDouble(sampleLotCount.get(0).getCurrentLotCount());
		double totalLotCount = Double.parseDouble(sampleLotCount.get(0).getTotalLotCount());

		currentLotCount = 1;
		totalLotCount++;
		sampleFlag = "Y";

		// Set SampleLotCount(CT_SAMPLELOTCOUNT)
		ExtendedObjectProxy.getSampleLotCountService().updateSampleLotCountDataByPolicy(lotData, samplePolicy, sampleLotCount, currentLotCount, totalLotCount);

		// ////////////////////////////////////////////////////////////////////////////////////////////////
		// ///////////////////////// Set LotSampleData ///////////////////////////////////////////////////
		// ////////////////////////////////////////////////////////////////////////////////////////////////

		// Insert Sample Lot EventInfo
		EventInfoExtended sampleEventInfo = new EventInfoExtended(eventInfo);
		EventInfoExtended tptjEventInfo = new EventInfoExtended(eventInfo);

		if (sampleLot == null)
		{
			// Parcing the 'PRODUCTSAMPLEPOSITION'
			String productSamplePositions = "1,30";

			List<String> productSamplePositionList = CommonUtil.splitString(",", productSamplePositions); // delimeter is ','
			List<String> exceptBackpPositionList = CommonUtil.copyToStringList(productSamplePositionList);
			List<String> actualSamplePositionList = new ArrayList<String>();

			// Set productSampingData
			actualSamplePositionList = this.setNormalProductSampingData(sampleEventInfo, tptjEventInfo, productListElement, lotData,new TPTJRule(), samplePolicy, productSamplePositionList,
						exceptBackpPositionList, sampleFlag, false, allScrapflag);

			if (allScrapflag)
			{
				if (sampleEventInfo.getEventComment().length() > 200)
				{
					sampleEventInfo.getEventComment().substring(0, 201);
				}
				sampleEventInfo.setEventComment("[Sample001]System sample all scrapped. " + sampleEventInfo.getEventComment());
			}
			else
			{
				sampleEventInfo.setEventComment("[Sample000]System sample Normal. " + sampleEventInfo.getEventComment());
			}

			ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);

			// for BackUpFlow
			if (StringUtils.equals(processFlowData.getProcessFlowType(), "BackUp"))
			{
				// set SampleLotData(CT_SAMPLELOT) for backupFlow
				ExtendedObjectProxy.getSampleLotService().insertSampleLotForBackUp(tptjEventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
						lotData.getProductSpecVersion(), lotData.getUdfs().get("BACKUPMAINFLOWNAME"), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
						lotData.getMachineName(), (String) samplePolicy.get("TOPROCESSFLOWNAME"), (String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"),
						(String) samplePolicy.get("TOPROCESSOPERATIONVERSION"), sampleFlag, sampleLotCount.get(0).getLotSampleCount(), CommonUtil.StringValueOf(currentLotCount),
						CommonUtil.StringValueOf(totalLotCount), (String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"),
						String.valueOf(actualSamplePositionList.size()), CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "", "",
						Integer.valueOf(samplePolicy.get("FLOWPRIORITY").toString()), lotData.getUdfs().get("BACKUPMAINFLOWNAME") , lotData.getProcessFlowVersion(),
						(String) samplePolicy.get("RETURNOPERATIONNAME"), (String) samplePolicy.get("RETURNOPERATIONVER"), "" , "Y");
			}
			else
			{
				// set SampleLotData(CT_SAMPLELOT)
				ExtendedObjectProxy.getSampleLotService().insertSampleLot(tptjEventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
						lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
						lotData.getMachineName(), (String) samplePolicy.get("TOPROCESSFLOWNAME"), (String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"),
						(String) samplePolicy.get("TOPROCESSOPERATIONVERSION"), sampleFlag, sampleLotCount.get(0).getLotSampleCount(), CommonUtil.StringValueOf(currentLotCount),
						CommonUtil.StringValueOf(totalLotCount), (String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"),
						String.valueOf(actualSamplePositionList.size()), CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "", "",
						Integer.valueOf(samplePolicy.get("FLOWPRIORITY").toString()), lotData.getProcessFlowName() , lotData.getProcessFlowVersion(),
						(String) samplePolicy.get("RETURNOPERATIONNAME"), (String) samplePolicy.get("RETURNOPERATIONVER"), "");
			}

			// Refresh SampleLotData
			sampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataByPolicy(lotData, samplePolicy);
		}
		else
		{
			log.info("Start integration Sampling");

			if (!StringUtils.equals(sampleFlag, "Y"))
				return sampleLot;

			String oldPosition = sampleLot.get(0).getActualSamplePosition();
			List<String> oldProductSamplePositionList = new ArrayList<String>();

			if (oldPosition == null || oldPosition.trim().length() == 0)
			{
				oldPosition = sampleLot.get(0).getProductSamplePosition();

				if (oldPosition == null || oldPosition.trim().length() == 0)
				{
					log.info("Get Sample oldPosition faild ");
					return sampleLot;
				}

				oldProductSamplePositionList = CommonUtil.splitString(",", oldPosition); // delimeter is ','
			}
			else
			{
				oldProductSamplePositionList = CommonUtil.splitString(", ", oldPosition); // delimeter is ', '
			}

			String productSamplePositions = "1,30";

			productSamplePositions = productSamplePositions.replaceAll(" ", ""); // remove blank

			List<String> productSamplePositionList = CommonUtil.splitString(",", productSamplePositions); // delimeter is ','

			productSamplePositionList.removeAll(oldProductSamplePositionList);
			productSamplePositionList.addAll(oldProductSamplePositionList);
			Collections.sort(productSamplePositionList, new Comparator<String>() {
				@Override
				public int compare(String o1, String o2)
				{
					Integer d1 = Integer.parseInt(o1);
					Integer d2 = Integer.parseInt(o2);

					return d1.compareTo(d2);
				}
			});
			if (productSamplePositionList.equals(oldProductSamplePositionList))
			{
				log.info("The integration Position as same as DB Position");
				return sampleLot;
			}

			log.info("Get integration Position Success");

			Lot deleteSampleLotData = new Lot();
			deleteSampleLotData.setKey(lotData.getKey());
			deleteSampleLotData.setFactoryName(lotData.getFactoryName());
			deleteSampleLotData.setProductSpecName(lotData.getProductSpecName());
			deleteSampleLotData.setProcessFlowName(lotData.getProcessFlowName());
			deleteSampleLotData.setProcessOperationName(sampleLot.get(0).getToProcessOperationName());
			MESLotServiceProxy.getLotServiceUtil().deleteSamplingData(eventInfo, deleteSampleLotData, new ArrayList<Element>(), true);

			List<String> actualSamplePositionList = new ArrayList<String>();

			for (String productSamplePosition : productSamplePositionList)
			{
				// get SamplingProduct Data
				String sql = " SELECT PRODUCTNAME FROM PRODUCT WHERE LOTNAME = :lotName AND POSITION = :position ";
				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("lotName", lotData.getKey().getLotName());
				bindMap.put("position", productSamplePosition);

				@SuppressWarnings("unchecked")
				List<Map<String, Object>> sampleProductResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

				if (sampleProductResult.size() > 0) // Exist the Product for productSamplePosition
				{
					// set SamplingProduct Data(CT_SAMPLEPRODUCT)
					ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, (String) sampleProductResult.get(0).get("PRODUCTNAME"), lotData.getKey().getLotName(),
							lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
							lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), lotData.getMachineName(), (String) samplePolicy.get("TOPROCESSFLOWNAME"),
							(String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"), (String) samplePolicy.get("TOPROCESSOPERATIONVERSION"), sampleFlag,
							(String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"), "", productSamplePosition, "");

					actualSamplePositionList.add(productSamplePosition);
				}
			}

			sampleEventInfo.setEventComment("[Sample002]Manual sample exist. " + sampleEventInfo.getEventComment());

			ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);

			// for BackUpFlow
			if (StringUtils.equals(processFlowData.getProcessFlowType(), "BackUp"))
			{
				// set SampleLotData(CT_SAMPLELOT) for backupFlow
				ExtendedObjectProxy.getSampleLotService().insertSampleLotForBackUp(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
						lotData.getProductSpecVersion(), lotData.getUdfs().get("BACKUPMAINFLOWNAME"), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
						lotData.getMachineName(), (String) samplePolicy.get("TOPROCESSFLOWNAME"), (String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"),
						(String) samplePolicy.get("TOPROCESSOPERATIONVERSION"), sampleFlag, sampleLotCount.get(0).getLotSampleCount(), CommonUtil.StringValueOf(currentLotCount),
						CommonUtil.StringValueOf(totalLotCount), (String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"),
						String.valueOf(actualSamplePositionList.size()), CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "", "",
						Integer.valueOf(ConvertUtil.getMapValueByName(samplePolicy, "FLOWPRIORITY")), lotData.getUdfs().get("BACKUPMAINFLOWNAME") , lotData.getProcessFlowVersion(),
						(String) samplePolicy.get("RETURNOPERATIONNAME"), (String) samplePolicy.get("RETURNOPERATIONVER"), "", "Y");
			}
			else
			{
				// set SampleLotData(CT_SAMPLELOT)
				ExtendedObjectProxy.getSampleLotService().insertSampleLot(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
						lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
						lotData.getMachineName(), (String) samplePolicy.get("TOPROCESSFLOWNAME"), (String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"),
						(String) samplePolicy.get("TOPROCESSOPERATIONVERSION"), sampleFlag, sampleLotCount.get(0).getLotSampleCount(), CommonUtil.StringValueOf(currentLotCount),
						CommonUtil.StringValueOf(totalLotCount), (String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"),
						String.valueOf(actualSamplePositionList.size()), CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "", "",
						Integer.valueOf(ConvertUtil.getMapValueByName(samplePolicy, "FLOWPRIORITY")), lotData.getProcessFlowName() , lotData.getProcessFlowVersion(),
						(String) samplePolicy.get("RETURNOPERATIONNAME"), (String) samplePolicy.get("RETURNOPERATIONVER"), "");
			}
			
			sampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataList(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
					lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
					lotData.getMachineName(), (String) samplePolicy.get("TOPROCESSFLOWNAME"), (String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"),
					(String) samplePolicy.get("TOPROCESSOPERATIONVERSION"));

		}

		return sampleLot;
	}

	public void deleteTPTJProductData(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		eventInfo.setEventName("DeleteTPTJData");

		// Only Production Operation.
		ProcessFlow processFlowData = CommonUtil.getProcessFlowData(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion());

		ProcessOperationSpec processOperationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName());

		// Validation Inspection Flow&Operation
		if (StringUtils.equals(processFlowData.getProcessFlowType(), "Inspection") && StringUtils.equals(processOperationData.getProcessOperationType(), "Inspection"))
			return;

		List<TPTJProduct> TPTJProList = new ArrayList<TPTJProduct>();

		List<TPTJCount> TPTJCountDataList = ExtendedObjectProxy.getTPTJCountService().getTPTJCountDataList(lotData);

		TPTJCount tptjCountData = new TPTJCount();

		if (TPTJCountDataList != null)
		{
			// Multi TPTJRule End Case
			for (TPTJCount countData : TPTJCountDataList)
			{
				tptjCountData = countData;

				TPTJProList = ExtendedObjectProxy.getTPTJProductService().getTPTJProductListByCount(TPTJProList, lotData, tptjCountData);

				if (TPTJProList != null)
				{
					for (TPTJProduct TPTJProductData : TPTJProList)
					{
						log.info("Start deleteTPTJProductData(RuleNum:" + String.valueOf(TPTJProductData.getRuleNum()) + ", ProductName:" + TPTJProductData.getProductName().toString() + ") : TrackOut");
						ExtendedObjectProxy.getTPTJProductService().remove(eventInfo, TPTJProductData);
					}

					// set CompleteFlag.
					tptjCountData.setCompleteFlag("Y");
					ExtendedObjectProxy.getTPTJCountService().modify(eventInfo, tptjCountData);
				}
			}
		}
	}

	public String getProductSamplePositions(Lot lotData, Map<String, Object> samplePolicy, List<String> positionList) throws CustomException
	{
		String productSamplePositions = (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION");
		log.info("setSamplingData:ProductSamplePositions(" + productSamplePositions + ")");

		// Random Sampling Position
		if (productSamplePositions.equals("Random"))
		{
			List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());

			if (productList.size() < Integer.valueOf((String) samplePolicy.get("PRODUCTSAMPLINGCOUNT")))
			{
				productSamplePositions = "ALL";
			}
			else
			{
				Collections.shuffle(positionList);
				String temp = "";

				for (int i = 0; i < Integer.valueOf((String) samplePolicy.get("PRODUCTSAMPLINGCOUNT")); i++)
				{
					String position = positionList.get(i) + ",";
					temp += position;
				}

				productSamplePositions = temp.substring(0, (temp.length() - 1));
			}
		}

		if(StringUtil.isNotEmpty(productSamplePositions))
		{
			productSamplePositions = productSamplePositions.replaceAll(" ", ""); // remove blank
		}
		
		if (StringUtils.equals(productSamplePositions, "All") || StringUtils.equals(productSamplePositions, "ALL"))
		{
			productSamplePositions = CommonUtil.makeProductSamplingPositionList(Integer.valueOf((String) samplePolicy.get("PRODUCTSAMPLINGCOUNT")));
		}

		return productSamplePositions;
	}

	public String getProductSamplePositions(Lot lotData, Map<String, Object> samplePolicy, List<String> positionList, String samplePositions) throws CustomException
	{
		String productSamplePositions = samplePositions;
		String[] args = productSamplePositions.split(",");
		log.info("setSamplingData:ProductSamplePositions(" + productSamplePositions + ")");

		// Random Sampling Position
		if (productSamplePositions.equals("Random"))
		{
			List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());

			if (productList.size() < args.length)
			{
				productSamplePositions = "ALL";
			}
			else
			{
				Collections.shuffle(positionList);
				String temp = "";

				for (int i = 0; i < args.length ; i++)
				{
					String position = positionList.get(i) + ",";
					temp += position;
				}

				productSamplePositions = temp.substring(0, (temp.length() - 1));
			}
		}

		if(StringUtil.isNotEmpty(productSamplePositions))
		{
			productSamplePositions = productSamplePositions.replaceAll(" ", ""); // remove blank
		}
		
		if (StringUtils.equals(productSamplePositions, "All") || StringUtils.equals(productSamplePositions, "ALL"))
		{
			productSamplePositions = CommonUtil.makeProductSamplingPositionList(Integer.valueOf((String) samplePolicy.get("PRODUCTSAMPLINGCOUNT")));
		}

		return productSamplePositions;
	}
	
	public List<String> setNormalProductSampingData(EventInfo eventInfo, EventInfo tptjEventInfo, List<Element> productListElement, Lot lotData, TPTJRule TPTJRuleData,
			Map<String, Object> samplePolicy, List<String> productSamplePositionList, List<String> exceptBackpPositionList, String sampleFlag, boolean TPTJInsertFlag, boolean allScrapflag)
			throws CustomException
	{
		log.info("Start setNormalProductSampingData : TrackOut");
		List<String> actualSamplePositionList = new ArrayList<String>();
		List<String> notExistPositionList = new ArrayList<String>();

		// Set TPTJCount.
		if (TPTJInsertFlag)
			ExtendedObjectProxy.getTPTJCountService().insertTPTJCount(tptjEventInfo, lotData, TPTJRuleData);

		for (String productSamplePosition : productSamplePositionList)
		{
			log.info("setSamplingData:productSamplePosition(" + productSamplePosition + ")");

			boolean existCheck = false;

			for (Element productElement : productListElement)
			{
				String position = productElement.getChildText("POSITION");

				if (StringUtils.equals(position, productSamplePosition))
				{
					String productName = productElement.getChildText("PRODUCTNAME");

					Product productData = new Product();

					try
					{
						productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
					}
					catch (Exception e)
					{
						productData = null;
					}

					if (productData != null) // Exist the Product for productSamplePosition
					{
						// set SamplingProduct Data(CT_SAMPLEPRODUCT)
						ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, productData.getKey().getProductName(), lotData.getKey().getLotName(), lotData.getFactoryName(),
								lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(),
								lotData.getProcessOperationVersion(), (String) samplePolicy.get("MACHINENAME"), (String) samplePolicy.get("TOPROCESSFLOWNAME"),
								(String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"), (String) samplePolicy.get("TOPROCESSOPERATIONVERSION"),
								sampleFlag, (String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"), productSamplePosition, "", "");

						// When TPTJRuleFlag(StandardFlag) is Y.
						if (TPTJInsertFlag)
						{
							ExtendedObjectProxy.getTPTJProductService().insertTPTJProduct(tptjEventInfo, lotData, TPTJRuleData, samplePolicy, productName, productSamplePosition,
									(String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"));
						}

						actualSamplePositionList.add(productSamplePosition);

						existCheck = true;
						break;
					}
				}
			}

			if (!existCheck) // No exist the Product for productSamplePosition, find new Product(backupProduct) for that.
			{
				notExistPositionList.add(productSamplePosition);
			}
		}

		if (!notExistPositionList.isEmpty())
		{
			List<Product> backupProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());

			int j = 0;
			int i = 0;

			for (; i < backupProductList.size(); i++)
			{
				Product backupProduct = backupProductList.get(i);

				for (Element productElement : productListElement)
				{
					String productName = productElement.getChildText("PRODUCTNAME");

					if (StringUtils.equals(backupProduct.getKey().getProductName(), productName))
					{
						String position = productElement.getChildText("POSITION");

						long notExistPosition = 0;
						try
						{
							notExistPosition = Long.parseLong(notExistPositionList.get(j));
						}
						catch (Exception e)
						{
							break;
						}

						if (Long.valueOf(position) > notExistPosition && !exceptBackpPositionList.contains(position))
						{
							ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, backupProduct.getKey().getProductName(), lotData.getKey().getLotName(),
									lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
									lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), lotData.getMachineName(), (String) samplePolicy.get("TOPROCESSFLOWNAME"),
									(String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"), (String) samplePolicy.get("TOPROCESSOPERATIONVERSION"),
									sampleFlag, (String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"), position, "", "");

							// When TPTJRuleFlag(StandardFlag) is Y.
							if (TPTJInsertFlag)
							{
								ExtendedObjectProxy.getTPTJProductService().insertTPTJProduct(tptjEventInfo, lotData, TPTJRuleData, samplePolicy, productName, position,
										(String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"));
							}

							// except for new Product(backupProduct)
							exceptBackpPositionList.add(position);
							actualSamplePositionList.add(position);
							j++;
						}

						if (j == notExistPositionList.size())
						{
							break;
						}

						break;
					}
				}
			}

			if (j != notExistPositionList.size())
			{
				for (i--; i >= 0; i--)
				{
					Product backupProduct = backupProductList.get(i);

					for (Element productElement : productListElement)
					{
						String productName = productElement.getChildText("PRODUCTNAME");

						if (StringUtils.equals(backupProduct.getKey().getProductName(), productName))
						{
							String position = productElement.getChildText("POSITION");

							long notExistPosition = 0;
							try
							{
								notExistPosition = Long.parseLong(notExistPositionList.get(j));
							}
							catch (Exception e)
							{
								break;
							}

							if (Long.valueOf(position) < notExistPosition && !exceptBackpPositionList.contains(position))
							{
								ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, backupProduct.getKey().getProductName(), lotData.getKey().getLotName(),
										lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
										lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), lotData.getMachineName(), (String) samplePolicy.get("TOPROCESSFLOWNAME"),
										(String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"), (String) samplePolicy.get("TOPROCESSOPERATIONVERSION"),
										sampleFlag, (String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"), position, "", "");

								// When TPTJRuleFlag(StandardFlag) is Y.
								if (TPTJInsertFlag)
								{
									ExtendedObjectProxy.getTPTJProductService().insertTPTJProduct(tptjEventInfo, lotData, TPTJRuleData, samplePolicy, productName, position,
											(String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"));
								}

								// except for new Product(backupProduct)
								exceptBackpPositionList.add(position);
								actualSamplePositionList.add(position);
								j++;
							}

							if (j == notExistPositionList.size())
							{
								break;
							}

							break;
						}
					}
				}
			}
			allScrapflag = true;
			notExistPositionList.clear();
		}

		return actualSamplePositionList;
	}

	public List<String> setTPTJProductSamplingData(EventInfo eventInfo, EventInfo tptjEventInfo, List<Element> productListElement, Lot lotData, TPTJRule TPTJRuleData,
			Map<String, Object> samplePolicy, List<String> TPTJProductList, List<String> productSamplePositionList, List<String> exceptBackpPositionList, String sampleFlag, boolean allScrapflag)
			throws CustomException
	{
		log.info("Start setTPTJProductSamplingData : TrackOut");
		List<String> actualSamplePositionList = new ArrayList<String>();
		List<String> notExistPositionList = new ArrayList<String>();
		List<String> notExistTPTJProductList = new ArrayList<String>();
		List<String> tptjPositionList = new ArrayList<String>();
		List<String> filterProductSamplePositionList = new ArrayList<String>();

		// Sync TPTJ Count.
		ExtendedObjectProxy.getTPTJCountService().syncTPTJCount(tptjEventInfo, lotData, TPTJRuleData);

		// Set TPTJ Sample Product Data.
		for (String tptjProductName : TPTJProductList)
		{
			log.info("setTPTJNormalData : productName(" + tptjProductName + ") ");
			// get SamplingProduct Data

			String condition = "PRODUCTNAME = :PRODUCTNAME AND LOTNAME = :LOTNAME";
			Object[] bindSet = new Object[] { tptjProductName, lotData.getKey().getLotName() };

			List<Product> tptjProductResult = new ArrayList<Product>();

			try
			{
				tptjProductResult = ProductServiceProxy.getProductService().select(condition, bindSet);
			}
			catch (Exception e)
			{
				tptjProductResult = null;
			}

			boolean checkMsgFlag = false;

			if (tptjProductResult != null && tptjProductResult.size() > 0)
			{
				for (Element productElement : productListElement)
				{
					String position = productElement.getChildText("POSITION");
					String productName = productElement.getChildText("PRODUCTNAME");

					if (StringUtils.equals(tptjProductResult.get(0).getKey().getProductName(), productName)
							&& StringUtils.equals(String.valueOf(tptjProductResult.get(0).getPosition()), position))
					{
						// set SamplingProduct Data(CT_SAMPLEPRODUCT)
						ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, tptjProductResult.get(0).getKey().getProductName(), lotData.getKey().getLotName(),
								lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
								lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), (String) samplePolicy.get("MACHINENAME"), (String) samplePolicy.get("TOPROCESSFLOWNAME"),
								(String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"), (String) samplePolicy.get("TOPROCESSOPERATIONVERSION"),
								sampleFlag, (String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"),
								String.valueOf(tptjProductResult.get(0).getPosition()), "", "");

						actualSamplePositionList.add(String.valueOf(tptjProductResult.get(0).getPosition()));
						tptjPositionList.add(String.valueOf(tptjProductResult.get(0).getPosition()));

						checkMsgFlag = true;
						break;
					}
				}
			}

			if (!checkMsgFlag)
				notExistTPTJProductList.add(tptjProductName);
		}

		// Filter Sample Position
		for (String productSamplePosition : productSamplePositionList)
		{
			for (String tptjPosition : tptjPositionList)
			{
				if (!StringUtils.equals(tptjPosition, productSamplePosition) && !tptjPositionList.contains(productSamplePosition))
				{
					filterProductSamplePositionList.add(productSamplePosition);
					break;
				}
			}
		}

		// Normal
		for (String productSamplePosition : filterProductSamplePositionList)
		{
			log.info("setSamplingData:productSamplePosition(" + productSamplePosition + ")");
			boolean existCheck = false;

			for (Element productElement : productListElement)
			{
				String position = productElement.getChildText("POSITION");

				if (StringUtils.equals(position, productSamplePosition))
				{
					String productName = productElement.getChildText("PRODUCTNAME");

					// get SamplingProduct Data
					String sql = " SELECT PRODUCTNAME FROM PRODUCT WHERE PRODUCTNAME = :productName ";
					Map<String, Object> bindMap = new HashMap<String, Object>();
					bindMap.put("lotName", lotData.getKey().getLotName());
					bindMap.put("productName", productName);

					@SuppressWarnings("unchecked")
					List<Map<String, Object>> sampleProductResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

					if (sampleProductResult.size() > 0) // Exist the Product for productSamplePosition
					{
						// set SamplingProduct Data(CT_SAMPLEPRODUCT)
						ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, (String) sampleProductResult.get(0).get("PRODUCTNAME"), lotData.getKey().getLotName(),
								lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
								lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), (String) samplePolicy.get("MACHINENAME"), (String) samplePolicy.get("TOPROCESSFLOWNAME"),
								(String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"), (String) samplePolicy.get("TOPROCESSOPERATIONVERSION"),
								sampleFlag, (String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"), productSamplePosition, "", "");

						actualSamplePositionList.add(productSamplePosition);

						existCheck = true;
						break;
					}
				}
			}
			// No exist the Product for productSamplePosition, find new Product(backupProduct) for that.
			if (!existCheck)
				notExistPositionList.add(productSamplePosition);
		}

		if (!notExistPositionList.isEmpty())
		{
			log.info("setSamplingData : BackUp Rule 1 ");
			List<Product> backupProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());

			int j = 0;
			int i = 0;

			for (; i < backupProductList.size(); i++)
			{
				Product backupProduct = backupProductList.get(i);

				for (Element productElement : productListElement)
				{
					String productName = productElement.getChildText("PRODUCTNAME");

					if (StringUtils.equals(backupProduct.getKey().getProductName(), productName))
					{
						String position = productElement.getChildText("POSITION");

						long notExistPosition = 0;
						try
						{
							notExistPosition = Long.parseLong(notExistPositionList.get(j));
						}
						catch (Exception e)
						{
							break;
						}

						if (Long.valueOf(position) > notExistPosition && !exceptBackpPositionList.contains(position) && !actualSamplePositionList.contains(position))
						{
							ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, backupProduct.getKey().getProductName(), lotData.getKey().getLotName(),
									lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
									lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), lotData.getMachineName(), (String) samplePolicy.get("TOPROCESSFLOWNAME"),
									(String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"), (String) samplePolicy.get("TOPROCESSOPERATIONVERSION"),
									sampleFlag, (String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"), position, "", "");

							// except for new Product(backupProduct)
							exceptBackpPositionList.add(position);
							actualSamplePositionList.add(position);
							j++;
						}

						if (j == notExistPositionList.size())
						{
							break;
						}

						break;
					}
				}
			}

			if (j != notExistPositionList.size())
			{
				log.info("setSamplingData : BackUp Rule 2 ");
				for (i--; i >= 0; i--)
				{
					Product backupProduct = backupProductList.get(i);

					for (Element productElement : productListElement)
					{
						String productName = productElement.getChildText("PRODUCTNAME");

						if (StringUtils.equals(backupProduct.getKey().getProductName(), productName))
						{
							String position = productElement.getChildText("POSITION");

							long notExistPosition = 0;
							try
							{
								notExistPosition = Long.parseLong(notExistPositionList.get(j));
							}
							catch (Exception e)
							{
								break;
							}

							if (Long.valueOf(position) < notExistPosition && !exceptBackpPositionList.contains(position) && !actualSamplePositionList.contains(position))
							{
								ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, backupProduct.getKey().getProductName(), lotData.getKey().getLotName(),
										lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
										lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), lotData.getMachineName(), (String) samplePolicy.get("TOPROCESSFLOWNAME"),
										(String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"), (String) samplePolicy.get("TOPROCESSOPERATIONVERSION"),
										sampleFlag, (String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"), position, "", "");

								// except for new Product(backupProduct)
								exceptBackpPositionList.add(position);
								actualSamplePositionList.add(position);
								j++;
							}

							if (j == notExistPositionList.size())
							{
								break;
							}

							break;
						}
					}
				}
			}
			allScrapflag = true;
			notExistPositionList.clear();
		}

		// TPTJ BackUp
		if (!notExistTPTJProductList.isEmpty())
		{
			log.info("setTPTJNormalData : BackUp Rule 1 ");
			int j = 0;
			List<Product> backupProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());

			for (Product backupProduct : backupProductList)
			{
				for (Element productElement : productListElement)
				{
					String productName = productElement.getChildText("PRODUCTNAME");

					if (StringUtils.equals(backupProduct.getKey().getProductName(), productName))
					{
						String position = productElement.getChildText("POSITION");

						if (!actualSamplePositionList.contains(position) && notExistTPTJProductList.contains(productName))
						{
							ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, backupProduct.getKey().getProductName(), lotData.getKey().getLotName(),
									lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
									lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), lotData.getMachineName(), (String) samplePolicy.get("TOPROCESSFLOWNAME"),
									(String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"), (String) samplePolicy.get("TOPROCESSOPERATIONVERSION"),
									sampleFlag, (String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"), position, "", "");

							actualSamplePositionList.add(position);
							j++;
						}

						if (j == notExistTPTJProductList.size())
						{
							break;
						}
					}
				}
			}

			if (j != notExistTPTJProductList.size())
			{
				log.info("setTPTJNormalData : BackUp Rule 2 ");

				for (Element productElement : productListElement)
				{
					String productName = productElement.getChildText("PRODUCTNAME");
					String position = productElement.getChildText("POSITION");

					if (!actualSamplePositionList.contains(position))
					{
						ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, productName, lotData.getKey().getLotName(), lotData.getFactoryName(),
								lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(),
								lotData.getProcessOperationVersion(), lotData.getMachineName(), (String) samplePolicy.get("TOPROCESSFLOWNAME"), (String) samplePolicy.get("TOPROCESSFLOWVERSION"),
								(String) samplePolicy.get("TOPROCESSOPERATIONNAME"), (String) samplePolicy.get("TOPROCESSOPERATIONVERSION"), sampleFlag,
								(String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"), position, "", "");

						actualSamplePositionList.add(position);
						j++;
					}

					if (j == notExistTPTJProductList.size())
					{
						break;
					}
				}
			}
		}

		return actualSamplePositionList;
	}

	public void cloneLotFutureAction(EventInfo eventInfo, Lot SourceLotData, String DestLotName)
	{
		try
		{
			try
			{
				List<LotFutureAction> futureActionResult = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataListByLotName(SourceLotData.getKey().getLotName(),
						SourceLotData.getFactoryName());

				if (futureActionResult != null)
				{
					log.info("Lot[" + SourceLotData.getKey().getLotName() + "] have reserved Future Action");

					for (LotFutureAction futureAction : futureActionResult)
					{
						String factoryName = futureAction.getFactoryName();
						String processFlowName = futureAction.getProcessFlowName();
						String processFlowVersion = futureAction.getProcessFlowVersion();
						String processOperationName = futureAction.getProcessOperationName();
						String processOperationVersion = futureAction.getProcessOperationVersion();
						int position = Integer.parseInt(futureAction.getPosition().toString());
						String actionName = futureAction.getActionName();
						String actionType = futureAction.getActionType();
						String eventName = futureAction.getLastEventName();
						String reasonCodeType = futureAction.getReasonCodeType();
						String reasonCode = futureAction.getReasonCode();
						String attribute1 = futureAction.getAttribute1();
						String attribute2 = futureAction.getAttribute2();
						String attribute3 = futureAction.getAttribute3();
						String beforeAction = futureAction.getBeforeAction();
						String afterAction = futureAction.getAfterAction();
						String beforeActionComment = futureAction.getBeforeActionComment();
						String afterActionComment = futureAction.getAfterActionComment();
						String afterActionUser = futureAction.getAfterActionUser();
						String beforeActionUser = futureAction.getBeforeActionUser();

						if (StringUtils.isNotEmpty(beforeActionComment))
						{
							if (!beforeActionComment.substring(0, 5).equalsIgnoreCase("Split"))
							{
								beforeActionComment = "Split," + beforeActionComment;
							}
						}
						else if ((StringUtils.isEmpty(beforeActionComment) && beforeAction.equalsIgnoreCase("True"))
								|| (StringUtils.isEmpty(beforeActionComment) && eventName.equalsIgnoreCase("skip")))
						{
							beforeActionComment = "Split," + beforeActionComment;
						}
						else
						{
							beforeActionComment = "";
						}

						if (StringUtils.isNotEmpty(afterActionComment))
						{
							if (!afterActionComment.substring(0, 5).equalsIgnoreCase("Split"))
							{
								afterActionComment = "Split," + afterActionComment;
							}
						}
						else if ((StringUtils.isEmpty(afterActionComment) && afterAction.equalsIgnoreCase("True")) || (StringUtils.isEmpty(afterActionComment) && eventName.equalsIgnoreCase("skip")))
						{
							afterActionComment = "Split," + afterActionComment;
						}
						else
						{
							afterActionComment = "";
						}
						eventInfo.setEventTimeKey(ConvertUtil.getCurrTimeKey());
						ExtendedObjectProxy.getLotFutureActionService().insertLotFutureAction(eventInfo, DestLotName, factoryName, processFlowName, processFlowVersion, processOperationName,
								processOperationVersion, position, reasonCode, reasonCodeType, actionName, actionType, attribute1, attribute2, attribute3, beforeAction, afterAction,
								beforeActionComment, afterActionComment, beforeActionUser, afterActionUser);
					}
				}
			}
			catch (Exception ex)
			{
				log.info("Error : Lot[" + SourceLotData.getKey().getLotName() + "] do not have reserved Future Action.");
			}
		}
		catch (Exception ex)
		{
			log.info("Reserve Skip or Reserve Hold Clone Error!");
		}
	}

	public boolean PostCellDeleteLotFutureAction(EventInfo eventInfo, Lot lotData, String processOperationName, String processOperationVersion) throws CustomException
	{
		List<LotFutureAction> FutureActionResult = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataListPostCell(lotData.getKey().getLotName(), lotData.getFactoryName(),
				lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), processOperationName, processOperationVersion);

		if (FutureActionResult != null)
		{
			log.info("Lot[" + lotData.getKey().getLotName() + "] have reserved Future Action");

			ExtendedObjectProxy.getLotFutureActionService().deleteLotFutureActionDataPostCell(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProcessFlowName(),
					lotData.getProcessFlowVersion(), processOperationName, processOperationVersion);

			return true;

		}
		else
		{
			return false;
		}
	}

	public void PanelHoldByTray(EventInfo eventInfo, String trayName) throws CustomException
	{
		List<Map<String, Object>> lotList = MESLotServiceProxy.getLotServiceUtil().getLotListByTray(trayName);

		List<Lot> updateLotList = new ArrayList<Lot>();
		List<LotHistory> updateLotHistory = new ArrayList<LotHistory>();

		for (Map<String, Object> lotData : lotList)
		{
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.get("LOTNAME").toString());
			Lot oldLot = (Lot) ObjectUtil.copyTo(lot);

			lot.setLotHoldState("Y");
			lot.setLastEventName(eventInfo.getEventName());
			lot.setLastEventTime(eventInfo.getEventTime());
			lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lot.setLastEventComment(eventInfo.getEventComment());
			lot.setLastEventUser(eventInfo.getEventUser());

			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);

			updateLotList.add(lot);
			updateLotHistory.add(lotHistory);
		}

		if (updateLotList.size() > 0)
		{
			log.debug("Insert Lot, LotHistory");
			try
			{
				CommonUtil.executeBatch("update", updateLotList);
				CommonUtil.executeBatch("insert", updateLotHistory);
			}
			catch (Exception e)
			{
				log.error(e.getMessage());
				throw new CustomException(e.getCause());
			}
		}

		// Durable
		Durable durableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
		Durable olddurableInfo = (Durable) ObjectUtil.copyTo(durableInfo);

		durableInfo.setLastEventName(eventInfo.getEventName());
		durableInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
		durableInfo.setLastEventTime(eventInfo.getEventTime());
		durableInfo.setLastEventUser(eventInfo.getEventUser());
		durableInfo.setLastEventComment(eventInfo.getEventComment());

		Map<String, String> durUdf = new HashMap<>();
		durUdf.put("DURABLEHOLDSTATE", "Y");
		durableInfo.setUdfs(durUdf);

		DurableHistory durHistory = new DurableHistory();
		durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(olddurableInfo, durableInfo, durHistory);

		DurableServiceProxy.getDurableService().update(durableInfo);
		DurableServiceProxy.getDurableHistoryService().insert(durHistory);
	}

	public void PanelHoldByTrayGroup(EventInfo eventInfo, String trayGroupName) throws CustomException
	{
		List<Map<String, Object>> lotList = MESLotServiceProxy.getLotServiceUtil().getLotListByTrayGroup(trayGroupName);

		List<Lot> updateLotList = new ArrayList<Lot>();
		List<LotHistory> updateLotHistory = new ArrayList<LotHistory>();

		List<Durable> updateDurList = new ArrayList<Durable>();
		List<DurableHistory> updateDurHistory = new ArrayList<DurableHistory>();

		// Panel
		for (Map<String, Object> lotData : lotList)
		{
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.get("LOTNAME").toString());
			Lot oldLot = (Lot) ObjectUtil.copyTo(lot);

			lot.setLotHoldState("Y");
			lot.setLastEventName(eventInfo.getEventName());
			lot.setLastEventTime(eventInfo.getEventTime());
			lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lot.setLastEventComment(eventInfo.getEventComment());
			lot.setLastEventUser(eventInfo.getEventUser());

			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);

			updateLotList.add(lot);
			updateLotHistory.add(lotHistory);
		}

		if (updateLotList.size() > 0)
		{
			log.debug("Insert Lot, LotHistory");
			try
			{
				CommonUtil.executeBatch("update", updateLotList);
				CommonUtil.executeBatch("insert", updateLotHistory);
			}
			catch (Exception e)
			{
				log.error(e.getMessage());
				throw new CustomException(e.getCause());
			}
		}

		// Tray
		List<Durable> trayList = MESDurableServiceProxy.getDurableServiceUtil().getTrayListByCoverName(trayGroupName);

		for (Durable durData : trayList)
		{
			Durable oldDurableInfo = (Durable) ObjectUtil.copyTo(durData);

			durData.setLastEventName(eventInfo.getEventName());
			durData.setLastEventTime(eventInfo.getEventTime());
			durData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			durData.setLastEventComment(eventInfo.getEventComment());
			durData.setLastEventUser(eventInfo.getEventUser());

			Map<String, String> durUdf = new HashMap<>();
			durUdf.put("DURABLEHOLDSTATE", "Y");
			durData.setUdfs(durUdf);

			DurableHistory durHistory = new DurableHistory();
			durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldDurableInfo, durData, durHistory);

			updateDurList.add(durData);
			updateDurHistory.add(durHistory);
		}

		// TrayGroup
		Durable trayGroupInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayGroupName);
		Durable oldTrayGroupInfo = (Durable) ObjectUtil.copyTo(trayGroupInfo);

		trayGroupInfo.setLastEventName(eventInfo.getEventName());
		trayGroupInfo.setLastEventTime(eventInfo.getEventTime());
		trayGroupInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
		trayGroupInfo.setLastEventComment(eventInfo.getEventComment());
		trayGroupInfo.setLastEventUser(eventInfo.getEventUser());

		Map<String, String> durUdf = new HashMap<>();
		durUdf.put("DURABLEHOLDSTATE", "Y");
		trayGroupInfo.setUdfs(durUdf);

		DurableHistory durHistory = new DurableHistory();
		durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldTrayGroupInfo, trayGroupInfo, durHistory);

		updateDurList.add(trayGroupInfo);
		updateDurHistory.add(durHistory);

		if (updateDurList.size() > 0)
		{
			log.debug("Insert Dur, DurHistory");
			try
			{
				CommonUtil.executeBatch("update", updateDurList);
				CommonUtil.executeBatch("insert", updateDurHistory);
			}
			catch (Exception e)
			{
				log.error(e.getMessage());
			}
		}
	}

	public void PanelHoldByPanelList(EventInfo eventInfo, List<Lot> lotList) throws CustomException
	{
		List<Lot> updateLotList = new ArrayList<Lot>();
		List<LotHistory> updateLotHistory = new ArrayList<LotHistory>();

		for (Lot lotData : lotList)
		{
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getKey().getLotName());
			Lot oldLot = (Lot) ObjectUtil.copyTo(lot);

			lot.setLotHoldState("Y");
			lot.setLastEventName(eventInfo.getEventName());
			lot.setLastEventTime(eventInfo.getEventTime());
			lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lot.setLastEventComment(eventInfo.getEventComment());
			lot.setLastEventUser(eventInfo.getEventUser());

			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);

			updateLotList.add(lot);
			updateLotHistory.add(lotHistory);
		}

		if (updateLotList.size() > 0)
		{
			log.debug("Insert Lot, LotHistory");
			try
			{
				CommonUtil.executeBatch("update", updateLotList);
				CommonUtil.executeBatch("insert", updateLotHistory);
			}
			catch (Exception e)
			{
				log.error(e.getMessage());
				throw new CustomException(e.getCause());
			}
		}
	}

	public boolean ExistMQCJobLot(Lot LotData) throws CustomException
	{
		boolean ExistJobfiag = false;

		List<MQCPlan> mqcPlanData = ExtendedObjectProxy.getMQCPlanService().getMQCPlanDataByLotNameMultiState(LotData.getKey().getLotName());

		if (mqcPlanData != null)
		{
			ExistJobfiag = true;
		}

		return ExistJobfiag;
	}

	public void inheritSourceLotMQCJob(Lot sourceLotData, Lot destinationLotData, List<String> SrcTransferProductList, List<String> DestProductList, String destCarrierName, EventInfo eventInfo)
			throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		Map<String, Object> args = new HashMap<String, Object>();

		String SrcMQCJobName = getMQCJobName(sourceLotData);
		String DestMQCJobName = getMQCJobName(destinationLotData);

		if (!SrcMQCJobName.isEmpty() && !DestMQCJobName.isEmpty())
		{
			boolean isRecycleMQC = false;
			boolean isMQCwithReserveEQP = false;

			isRecycleMQC = isRecycleMQC(SrcMQCJobName);
			isMQCwithReserveEQP = isMQCwithReserveEQP(SrcMQCJobName);

			// Update MQCPlanDetail
			List<MQCPlanDetail> dPlanDetailList = new ArrayList<MQCPlanDetail>();
			List<MQCPlanDetail> sPlanDetailList = new ArrayList<MQCPlanDetail>();
			List<MQCPlanDetail> srcPlanDetailList = new ArrayList<MQCPlanDetail>();
			List<MQCPlanDetail> destPlanDetailList = new ArrayList<MQCPlanDetail>();

			sql.setLength(0);
			sql.append("SELECT DISTINCT P.POSITION, P.PRODUCTNAME  ");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P  ");
			sql.append(" WHERE E.PRODUCTNAME = P.PRODUCTNAME  ");
			sql.append("   AND E.JOBNAME = :DESTJOBNAME  ");
			sql.append("   AND P.PRODUCTNAME IN ( :DESTPRODUCTLIST )  ");
			sql.append("UNION ");
			sql.append("SELECT DISTINCT P.POSITION, P.PRODUCTNAME  ");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P  ");
			sql.append(" WHERE E.PRODUCTNAME = P.PRODUCTNAME  ");
			sql.append("   AND E.JOBNAME = :SRCJOBNAME  ");
			sql.append("   AND P.PRODUCTNAME IN ( :SOURCEPRODUCTLIST )  ");
			sql.append("ORDER BY POSITION ");

			args.clear();
			args.put("DESTJOBNAME", DestMQCJobName);
			args.put("DESTPRODUCTLIST", DestProductList);
			args.put("SRCJOBNAME", SrcMQCJobName);
			args.put("SOURCEPRODUCTLIST", SrcTransferProductList);

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> positionResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (positionResult.size() > 0)
			{
				log.info("exist MQC Product Info by Product");
				List<String> positionList = CommonUtil.makeListBySqlResult(positionResult, "POSITION");
				String actualPosition = CommonUtil.toStringWithoutBrackets(positionList);
				String modifySrcPositionforMain = getModifyPositionforSourceMQCJob(SrcMQCJobName, SrcTransferProductList, isRecycleMQC);
				String modifySrcPositionforRecycle = getModifyPositionforSourceRecycleMQCJob(SrcMQCJobName, SrcTransferProductList, isRecycleMQC);
				String modifyDestPositionforMain = getModifyPositionforDestMQCJob(SrcMQCJobName, DestMQCJobName, SrcTransferProductList, DestProductList, isRecycleMQC);
				String modifyDestPositionforRecycle = getModifyPositionforDestRecycleMQCJob(SrcMQCJobName, DestMQCJobName, SrcTransferProductList, DestProductList, isRecycleMQC);

				sql.setLength(0);
				sql.append("SELECT DISTINCT  ");
				sql.append("       D.JOBNAME,  ");
				sql.append("       D.PROCESSFLOWNAME,  ");
				sql.append("       D.PROCESSFLOWVERSION,  ");
				sql.append("       D.PROCESSOPERATIONNAME,  ");
				sql.append("       D.PROCESSOPERATIONVERSION ");
				sql.append("  FROM CT_MQCPLANDETAIL D, CT_MQCPLANDETAIL_EXTENDED E  ");
				sql.append(" WHERE D.JOBNAME = E.JOBNAME  ");
				sql.append("   AND D.PROCESSFLOWNAME = E.PROCESSFLOWNAME  ");
				sql.append("   AND D.PROCESSFLOWVERSION = E.PROCESSFLOWVERSION  ");
				sql.append("   AND D.PROCESSOPERATIONNAME = E.PROCESSOPERATIONNAME  ");
				sql.append("   AND D.PROCESSOPERATIONVERSION = E.PROCESSOPERATIONVERSION  ");
				sql.append("   AND D.JOBNAME = :SRCJOBNAME  ");
				sql.append("   AND E.PRODUCTNAME IN ( :PRODUCTLIST )  ");

				args.clear();
				args.put("SRCJOBNAME", SrcMQCJobName);
				args.put("PRODUCTLIST", SrcTransferProductList);

				@SuppressWarnings("unchecked")
				List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

				if (result.size() > 0)
				{
					if (!isRecycleMQC) // MQC Main Flow
					{

						for (Map<String, Object> map : result)
						{
							String processFlowName = ConvertUtil.getMapValueByName(map, "PROCESSFLOWNAME");
							String processFlowVersion = ConvertUtil.getMapValueByName(map, "PROCESSFLOWVERSION");
							String processOperationName = ConvertUtil.getMapValueByName(map, "PROCESSOPERATIONNAME");
							String processOperationVersion = ConvertUtil.getMapValueByName(map, "PROCESSOPERATIONVERSION");

							MQCPlanDetail destPlanDetail = ExtendedObjectProxy.getMQCPlanDetailService().selectByKey(false,
									new Object[] { DestMQCJobName, processFlowName, processFlowVersion, processOperationName, processOperationVersion });

							destPlanDetailList.add(destPlanDetail);

							MQCPlanDetail srcPlanDetail = ExtendedObjectProxy.getMQCPlanDetailService().selectByKey(false,
									new Object[] { SrcMQCJobName, processFlowName, processFlowVersion, processOperationName, processOperationVersion });

							srcPlanDetailList.add(srcPlanDetail);
						}

						for (MQCPlanDetail oldPlanDetail : destPlanDetailList)
						{
							MQCPlanDetail planDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);
							planDetail.setJobName(DestMQCJobName);
							planDetail.setLotName(destinationLotData.getKey().getLotName());
							planDetail.setCarrierName(destCarrierName);
							planDetail.setLastEventTime(eventInfo.getEventTime());
							planDetail.setLastEventName(eventInfo.getEventName());
							planDetail.setLastEventUser(eventInfo.getEventUser());
							planDetail.setLastEventComment(eventInfo.getEventComment());
							if (isMQCwithReserveEQP)
							{
								String actualPositionforReserveEQP = getActualPositionforReserveEQP(SrcMQCJobName, SrcTransferProductList, DestMQCJobName, DestProductList,
										planDetail.getProcessFlowName(), planDetail.getProcessOperationName());
								planDetail.setPosition(actualPositionforReserveEQP);
							}
							else
							{
								planDetail.setPosition(actualPosition);
							}

							dPlanDetailList.add(planDetail);

						}

						for (MQCPlanDetail oldPlanDetail : srcPlanDetailList)
						{
							MQCPlanDetail SrcPlanDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);

							SrcPlanDetail.setLastEventTime(eventInfo.getEventTime());
							SrcPlanDetail.setLastEventName(eventInfo.getEventName());
							SrcPlanDetail.setLastEventUser(eventInfo.getEventUser());
							SrcPlanDetail.setLastEventComment(eventInfo.getEventComment());
							SrcPlanDetail.setPosition(modifySrcPositionforMain);

							sPlanDetailList.add(SrcPlanDetail);
						}
					}
					else
					// MQC Recycle Flow
					{
						boolean OnlyRecycleFlag = true;

						for (Map<String, Object> map : result)
						{
							String processFlowName = ConvertUtil.getMapValueByName(map, "PROCESSFLOWNAME");
							String processFlowVersion = ConvertUtil.getMapValueByName(map, "PROCESSFLOWVERSION");
							String processOperationName = ConvertUtil.getMapValueByName(map, "PROCESSOPERATIONNAME");
							String processOperationVersion = ConvertUtil.getMapValueByName(map, "PROCESSOPERATIONVERSION");

							ProcessFlowKey processFlowKey = new ProcessFlowKey();
							processFlowKey.setFactoryName(sourceLotData.getFactoryName());
							processFlowKey.setProcessFlowName(processFlowName);
							processFlowKey.setProcessFlowVersion(processFlowVersion);
							ProcessFlow processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);

							if (StringUtils.equalsIgnoreCase(processFlowData.getProcessFlowType(), "MQC"))
							{
								OnlyRecycleFlag = false;
							}

							MQCPlanDetail destPlanDetail = ExtendedObjectProxy.getMQCPlanDetailService().selectByKey(false,
									new Object[] { DestMQCJobName, processFlowName, processFlowVersion, processOperationName, processOperationVersion });

							destPlanDetailList.add(destPlanDetail);

							MQCPlanDetail srcPlanDetail = ExtendedObjectProxy.getMQCPlanDetailService().selectByKey(false,
									new Object[] { SrcMQCJobName, processFlowName, processFlowVersion, processOperationName, processOperationVersion });

							srcPlanDetailList.add(srcPlanDetail);
						}
						if (OnlyRecycleFlag)
						{
							log.info("Transfer product from SourceLot is Only in Recycle Flow");
							for (MQCPlanDetail oldPlanDetail : destPlanDetailList)
							{
								MQCPlanDetail planDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);
								planDetail.setJobName(DestMQCJobName);
								planDetail.setLotName(destinationLotData.getKey().getLotName());
								planDetail.setCarrierName(destCarrierName);
								planDetail.setLastEventTime(eventInfo.getEventTime());
								planDetail.setLastEventName(eventInfo.getEventName());
								planDetail.setLastEventUser(eventInfo.getEventUser());
								planDetail.setLastEventComment(eventInfo.getEventComment());
								planDetail.setPosition(modifyDestPositionforRecycle);

								dPlanDetailList.add(planDetail);

							}

							for (MQCPlanDetail oldPlanDetail : srcPlanDetailList)
							{
								MQCPlanDetail SrcPlanDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);

								SrcPlanDetail.setLastEventTime(eventInfo.getEventTime());
								SrcPlanDetail.setLastEventName(eventInfo.getEventName());
								SrcPlanDetail.setLastEventUser(eventInfo.getEventUser());
								SrcPlanDetail.setLastEventComment(eventInfo.getEventComment());
								SrcPlanDetail.setPosition(modifySrcPositionforRecycle);

								sPlanDetailList.add(SrcPlanDetail);
							}
						}
						else
						{
							log.info("Transfer product from SourceLot is not Only in Recycle Flow");
							for (MQCPlanDetail oldPlanDetail : destPlanDetailList)
							{
								MQCPlanDetail DestPlanDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);

								ProcessFlowKey processFlowKey = new ProcessFlowKey();
								processFlowKey.setFactoryName(destinationLotData.getFactoryName());
								processFlowKey.setProcessFlowName(DestPlanDetail.getProcessFlowName());
								processFlowKey.setProcessFlowVersion(DestPlanDetail.getProcessFlowVersion());
								ProcessFlow DestPlanProcessFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);

								DestPlanDetail.setJobName(DestMQCJobName);
								DestPlanDetail.setLotName(destinationLotData.getKey().getLotName());
								DestPlanDetail.setCarrierName(destCarrierName);
								DestPlanDetail.setLastEventTime(eventInfo.getEventTime());
								DestPlanDetail.setLastEventName(eventInfo.getEventName());
								DestPlanDetail.setLastEventUser(eventInfo.getEventUser());
								DestPlanDetail.setLastEventComment(eventInfo.getEventComment());
								if (StringUtils.equalsIgnoreCase(DestPlanProcessFlowData.getProcessFlowType(), "MQC"))
								{
									// set Positions for MainMQCFlow
									DestPlanDetail.setPosition(modifyDestPositionforMain);
								}
								else
								{
									// set Positions for RecycleFlow
									DestPlanDetail.setPosition(modifyDestPositionforRecycle);
								}

								dPlanDetailList.add(DestPlanDetail);

							}

							for (MQCPlanDetail oldPlanDetail : srcPlanDetailList)
							{
								MQCPlanDetail SrcPlanDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);

								ProcessFlowKey processFlowKey = new ProcessFlowKey();
								processFlowKey.setFactoryName(destinationLotData.getFactoryName());
								processFlowKey.setProcessFlowName(SrcPlanDetail.getProcessFlowName());
								processFlowKey.setProcessFlowVersion(SrcPlanDetail.getProcessFlowVersion());
								ProcessFlow SrcPlanProcessFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);

								SrcPlanDetail.setLastEventTime(eventInfo.getEventTime());
								SrcPlanDetail.setLastEventName(eventInfo.getEventName());
								SrcPlanDetail.setLastEventUser(eventInfo.getEventUser());
								SrcPlanDetail.setLastEventComment(eventInfo.getEventComment());
								if (StringUtils.equalsIgnoreCase(SrcPlanProcessFlowData.getProcessFlowType(), "MQC"))
								{
									// set Positions for MainMQCFlow
									SrcPlanDetail.setPosition(modifySrcPositionforMain);
								}
								else
								{
									// set Positions for RecycleFlow
									SrcPlanDetail.setPosition(modifySrcPositionforRecycle);
								}

								sPlanDetailList.add(SrcPlanDetail);
							}

						}

					}

					if (sPlanDetailList.size() > 0)
					{
						// Modify Source MQC Job in CT_MQCPlanDetail by Source Transfer ProductList
						try
						{
							for (MQCPlanDetail PlanDetail : sPlanDetailList)
							{
								ExtendedObjectProxy.getMQCPlanDetailService().modify(eventInfo, PlanDetail);
							}
							log.info(sPlanDetailList.size() + " Rows Modified into CT_MQCPlanDetail for Source MQC Job");
						}
						catch (Exception ex)
						{
							log.info("Error : Rows Modified into CT_MQCPlanDetail");
						}
					}

					if (dPlanDetailList.size() > 0)
					{
						// Update MQCPlanDetail
						try
						{
							for (MQCPlanDetail PlanDetail : dPlanDetailList)
							{
								ExtendedObjectProxy.getMQCPlanDetailService().modify(eventInfo, PlanDetail);
							}
							log.info(dPlanDetailList.size() + " Rows modified into CT_MQCPlanDetail for Destination MQC Job");
						}
						catch (Exception ex)
						{
							log.info("Error : Rows inserted into CT_MQCPlanDetail");
						}
					}

					int InsRows = 0;
					for (String SourceProduct : SrcTransferProductList)
					{
						for (Map<String, Object> map : positionResult)
						{
							String productName = ConvertUtil.getMapValueByName(map, "PRODUCTNAME");
							String position = ConvertUtil.getMapValueByName(map, "POSITION");

							if (productName.equalsIgnoreCase(SourceProduct))
							{
								// Update MQCPlanDetail_Extended
								sql.setLength(0);
								sql.append("INSERT INTO CT_MQCPLANDETAIL_EXTENDED ");
								sql.append("        (JOBNAME, PROCESSFLOWNAME, PROCESSFLOWVERSION, PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION,  ");
								sql.append("         PRODUCTNAME, POSITION, LOTNAME, FORBIDDENCODE, OLDFORBIDDENCODE,  ");
								sql.append("         LASTEVENTNAME, LASTEVENTTIME, LASTEVENTUSER, DUMMYUSEDCOUNT, RECIPENAME,  ");
								sql.append("         MACHINENAME) ");
								sql.append(" (SELECT :JOBNAME, PROCESSFLOWNAME, PROCESSFLOWVERSION, PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION,  ");
								sql.append("         PRODUCTNAME, :POSITION, :LOTNAME, FORBIDDENCODE, OLDFORBIDDENCODE,  ");
								sql.append("         :EVENTNAME, :EVENTTIME, :EVENTUSER, DUMMYUSEDCOUNT, RECIPENAME,  ");
								sql.append("         MACHINENAME FROM CT_MQCPLANDETAIL_EXTENDED ");
								sql.append("   WHERE JOBNAME = :OLDJOBNAME ");
								sql.append("     AND PRODUCTNAME = :PRODUCTNAME ) ");

								args.clear();
								args.put("JOBNAME", DestMQCJobName);
								args.put("LOTNAME", destinationLotData.getKey().getLotName());
								args.put("POSITION", position);
								args.put("EVENTNAME", eventInfo.getEventName());
								args.put("EVENTTIME", eventInfo.getEventTime().toString());
								args.put("EVENTUSER", eventInfo.getEventUser());
								args.put("OLDJOBNAME", SrcMQCJobName);
								args.put("PRODUCTNAME", productName);

								GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), args);
								InsRows++;
							}
						}
					}
					log.info(InsRows + " Rows inserted into CT_MQCPlanDetail_Extended");

					// Delete Source MQC Job in MQCPlanDetail_Extended by Source Transfer ProductList
					ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().deleteMQCPlanDetail_ExtendedByProdList(SrcMQCJobName, SrcTransferProductList);

					log.info("Rows deleted into CT_MQCPlanDetail_Extended by Soruce MQC Job");

				}
				else
				{
					log.info("Not exist MQC Operation Info by Product" + SrcTransferProductList);
				}
			}
			else
			{
				log.info("Not exist MQC Product Info by Product of MergeLot[SrcLot :" + sourceLotData.getKey().getLotName() + ", DestLot :" + destinationLotData.getKey().getLotName() + "]");
			}
		}

		else
		{
			log.info("Not exist Release/Recycling MQC Info by SrcLot[" + sourceLotData.getKey().getLotName() + "] or DestLot[" + destinationLotData.getKey().getLotName() + "]");
		}

	}

	public String getActualPositionforReserveEQP(String ScrMQCJobName, List<String> SrcTransferProductList, String DestMQCJobName, List<String> DestProductList, String ProcessFlowName,
			String ProcessOperationName) throws CustomException
	{
		String modifyPosition = "";

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT P.POSITION, P.PRODUCTNAME   ");
		sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P   ");
		sql.append(" WHERE E.PRODUCTNAME = P.PRODUCTNAME ");
		sql.append("   AND E.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND E.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME  ");
		sql.append("   AND E.JOBNAME = :DESTJOBNAME   ");
		sql.append("   AND P.PRODUCTNAME IN ( :DESTPRODUCTLIST )   ");
		sql.append("UNION  ");
		sql.append("SELECT DISTINCT P.POSITION, P.PRODUCTNAME   ");
		sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P   ");
		sql.append(" WHERE E.PRODUCTNAME = P.PRODUCTNAME  ");
		sql.append("   AND E.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND E.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME   ");
		sql.append("   AND E.JOBNAME = :SRCJOBNAME   ");
		sql.append("   AND P.PRODUCTNAME IN ( :SOURCEPRODUCTLIST )   ");
		sql.append("ORDER BY POSITION  ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("SRCJOBNAME", ScrMQCJobName);
		args.put("SOURCEPRODUCTLIST", SrcTransferProductList);
		args.put("DESTJOBNAME", DestMQCJobName);
		args.put("DESTPRODUCTLIST", DestProductList);
		args.put("PROCESSFLOWNAME", ProcessFlowName);
		args.put("PROCESSOPERATIONNAME", ProcessOperationName);

		try
		{
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> positionResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (positionResult.size() > 0)
			{
				log.info("Get Positions Info in CT_MQCPlanDetail by Source Transfer Product");
				List<String> positionList = CommonUtil.makeListBySqlResult(positionResult, "POSITION");
				modifyPosition = CommonUtil.toStringWithoutBrackets(positionList);
			}
		}
		catch (Exception ex)
		{
			log.info("Error : Get Positions Info in CT_MQCPlanDetail by Source Transfer Product [jobName : " + ScrMQCJobName + "]");
		}

		return modifyPosition;
	}

	public String getModifyPositionforDestMQCJob(String SrcMQCJobName, String DestMQCJobName, List<String> SrcTransferProductList, List<String> DestProductList, boolean isRecycleMQC)
			throws CustomException
	{
		String modifyPosition = "";

		if (!isRecycleMQC) // Main MQC Flow
		{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT DISTINCT E.POSITION, E.PRODUCTNAME ");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P   ");
			sql.append(" WHERE 1=1 ");
			sql.append("   AND E.JOBNAME = :DESTJOBNAME ");
			sql.append("   AND P.PRODUCTNAME NOT IN ( :DESTPRODUCTLIST )   ");
			sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME ");
			sql.append("ORDER BY TO_NUMBER(POSITION) ");

			Map<String, Object> args = new HashMap<String, Object>();
			args.put("DESTJOBNAME", DestMQCJobName);
			args.put("DESTPRODUCTLIST", DestProductList);

			try
			{
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> positionResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

				if (positionResult.size() > 0)
				{
					log.info("Get Positions Info in CT_MQCPlanDetail by Destination Product");
					List<String> positionList = CommonUtil.makeListBySqlResult(positionResult, "POSITION");
					modifyPosition = CommonUtil.toStringWithoutBrackets(positionList);
				}
			}
			catch (Exception ex)
			{
				log.info("Error : Get Positions Info in CT_MQCPlanDetail by Destination Product [jobName : " + DestMQCJobName + "]");
			}
		}
		else
		// Recycle MQC Flow
		{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT DISTINCT P.POSITION, P.PRODUCTNAME ");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P ");
			sql.append(" WHERE 1 = 1 ");
			sql.append("   AND E.JOBNAME = :DESTJOBNAME ");
			sql.append("   AND P.PRODUCTNAME IN ( :DESTPRODUCTLIST) ");
			sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME ");
			sql.append("   AND E.PROCESSFLOWNAME IN (SELECT PROCESSFLOWNAME ");
			sql.append("                                FROM PROCESSFLOW ");
			sql.append("                               WHERE PROCESSFLOWTYPE = 'MQC') ");
			sql.append("UNION ");
			sql.append("SELECT DISTINCT P.POSITION, P.PRODUCTNAME ");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P ");
			sql.append(" WHERE 1 = 1 ");
			sql.append("   AND E.JOBNAME = :SRCJOBNAME ");
			sql.append("   AND P.PRODUCTNAME IN ( :DESTPRODUCTLIST) ");
			sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME ");
			sql.append("   AND E.PROCESSFLOWNAME IN (SELECT PROCESSFLOWNAME ");
			sql.append("                                FROM PROCESSFLOW ");
			sql.append("                               WHERE PROCESSFLOWTYPE = 'MQC') ");
			sql.append("ORDER BY POSITION ");

			Map<String, Object> args = new HashMap<String, Object>();
			args.put("SRCJOBNAME", SrcMQCJobName);
			args.put("DESTJOBNAME", DestMQCJobName);
			args.put("DESTPRODUCTLIST", DestProductList);

			try
			{
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> positionResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

				if (positionResult.size() > 0)
				{
					log.info("Get Positions Info in CT_MQCPlanDetail by Destination Product");
					List<String> positionList = CommonUtil.makeListBySqlResult(positionResult, "POSITION");
					modifyPosition = CommonUtil.toStringWithoutBrackets(positionList);
				}
			}
			catch (Exception ex)
			{
				log.info("Error : Get Positions Info in CT_MQCPlanDetail by Destination Product [jobName : " + DestMQCJobName + "]");
			}

		}

		return modifyPosition;
	}

	public String getModifyPositionforDestRecycleMQCJob(String SrcMQCJobName, String DestMQCJobName, List<String> SrcTransferProductList, List<String> DestProductList, boolean isRecycleMQC)
			throws CustomException
	{
		String modifyPosition = "";

		if (isRecycleMQC)
		{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT DISTINCT P.POSITION, P.PRODUCTNAME ");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P ");
			sql.append(" WHERE 1 = 1 ");
			sql.append("   AND E.JOBNAME = :DESTJOBNAME ");
			sql.append("   AND P.PRODUCTNAME IN ( :DESTPRODUCTLIST) ");
			sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME ");
			sql.append("   AND E.PROCESSFLOWNAME NOT IN (SELECT PROCESSFLOWNAME ");
			sql.append("                                   FROM PROCESSFLOW ");
			sql.append("                                  WHERE PROCESSFLOWTYPE = 'MQC') ");
			sql.append("UNION ");
			sql.append("SELECT DISTINCT P.POSITION, P.PRODUCTNAME ");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P ");
			sql.append(" WHERE 1 = 1 ");
			sql.append("   AND E.JOBNAME = :SRCJOBNAME ");
			sql.append("   AND P.PRODUCTNAME IN ( :DESTPRODUCTLIST) ");
			sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME ");
			sql.append("   AND E.PROCESSFLOWNAME NOT IN (SELECT PROCESSFLOWNAME ");
			sql.append("                                   FROM PROCESSFLOW ");
			sql.append("                                  WHERE PROCESSFLOWTYPE = 'MQC') ");
			sql.append("ORDER BY POSITION ");

			Map<String, Object> args = new HashMap<String, Object>();
			args.put("SRCJOBNAME", SrcMQCJobName);
			args.put("DESTJOBNAME", DestMQCJobName);
			args.put("DESTPRODUCTLIST", DestProductList);

			try
			{
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> positionResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

				if (positionResult.size() > 0)
				{
					log.info("Get Positions Info in CT_MQCPlanDetail by Dest Product");
					List<String> positionList = CommonUtil.makeListBySqlResult(positionResult, "POSITION");
					modifyPosition = CommonUtil.toStringWithoutBrackets(positionList);
				}
			}
			catch (Exception ex)
			{
				log.info("Error : Get Positions Info in CT_MQCPlanDetail by Dest Product [jobName : " + DestMQCJobName + "]");
			}
		}

		return modifyPosition;
	}

	public String getMQCJobName(Lot lotData) throws CustomException
	{
		String JobName = "";
		List<MQCPlan> mqcPlanData = ExtendedObjectProxy.getMQCPlanService().getMQCPlanDataByLotNameMultiState(lotData.getKey().getLotName());

		if (mqcPlanData != null)
		{
			JobName = mqcPlanData.get(0).getJobName();
		}
		else
		{
			log.info("Not exist Release/Recycling MQC Job Info by Lot: " + lotData.getKey().getLotName());
		}

		return JobName;
	}

	public List<String> convertProductList(List<ProductP> productPSequence) throws Exception
	{
		List<String> productList = new ArrayList<String>();

		for (ProductP product : productPSequence)
		{
			String productName = product.getProductName();

			if (StringUtils.isNotEmpty(productName))
			{
				productList.add(product.getProductName());
			}
		}

		return productList;
	}

	public boolean ExistMQCProductbyLot(Lot LotData) throws CustomException
	{
		boolean ExistJobfiag = false;

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT E.PRODUCTNAME   ");
		sql.append("  FROM CT_MQCPLAN M, CT_MQCPLANDETAIL D, CT_MQCPLANDETAIL_EXTENDED E     ");
		sql.append(" WHERE MQCSTATE IN ('Released','Recycling')    ");
		sql.append("   AND M.JOBNAME = D.JOBNAME   ");
		sql.append("   AND D.JOBNAME = E.JOBNAME   ");
		sql.append("   AND M.JOBNAME = E.JOBNAME  ");
		sql.append("   AND M.LOTNAME = :LOTNAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("LOTNAME", LotData.getKey().getLotName());

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			ExistJobfiag = true;
		}

		return ExistJobfiag;
	}

	public void SentToMQCBankWithoutMQCproduct(EventInfo eventInfo, Lot LotData) throws CustomException
	{
		boolean isExistMQCProductbyLot = MESLotServiceProxy.getLotServiceUtil().ExistMQCProductbyLot(LotData);

		if (!isExistMQCProductbyLot)
		{
			log.info("Lot [" + LotData + "] does not have MQC Product.");
			if (!LotData.getLotState().equals("Emptied"))
			{
				// sent to MQC Bank
				MESLotServiceProxy.getLotServiceUtil().changeReturnMQCBank(eventInfo, LotData);
			}

			// After being sent to the MQC bank, Delete MQC Job
			//log.info("After being sent to the MQC bank, Delete MQC Job. Source Lot : [" + LotData + "]");
			//MESLotServiceProxy.getLotServiceUtil().removeMQCJob(eventInfo, LotData);
		}
	}

	public void createMQCWithReturnBank(Lot sourceLotData, Lot destinationLotData, List<String> prodList, String destCarrierName, EventInfo eventInfo) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		Map<String, Object> args = new HashMap<String, Object>();
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

		List<MQCPlan> mqcPlanData = ExtendedObjectProxy.getMQCPlanService().getMQCPlanDataByLotNameMultiState(sourceLotData.getKey().getLotName());

		if (mqcPlanData != null)
		{
			String oldJobName = mqcPlanData.get(0).getJobName();
			String oldJobState = mqcPlanData.get(0).getMQCState();

			String jobName = "MQC-" + destinationLotData.getKey().getLotName() + "-" + TimeStampUtil.getCurrentEventTimeKey();

			boolean isRecycleMQC = false;
			boolean isMQCwithReserveEQP = false;

			isRecycleMQC = isRecycleMQC(oldJobName);
			isMQCwithReserveEQP = isMQCwithReserveEQP(oldJobName);

			MQCPlan oldPlanData = ExtendedObjectProxy.getMQCPlanService().selectByKey(false, new Object[] { oldJobName });

			// Create MQCPlan
			MQCPlan planData = (MQCPlan) ObjectUtil.copyTo(oldPlanData);
			planData = ExtendedObjectProxy.getMQCPlanService().insertMQCPlan(eventInfo, planData, jobName, 1, destinationLotData.getKey().getLotName(), oldJobState);

			// Create MQCPlanDetail
			List<MQCPlanDetail> destPlanDetailList = new ArrayList<MQCPlanDetail>();
			List<MQCPlanDetail> oldPlanDetailList = new ArrayList<MQCPlanDetail>();
			List<MQCPlanDetail> srcPlanDetailList = new ArrayList<MQCPlanDetail>();

			sql.setLength(0);
			sql.append("SELECT DISTINCT P.POSITION, P.PRODUCTNAME");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P ");
			sql.append(" WHERE E.PRODUCTNAME = P.PRODUCTNAME ");
			sql.append("   AND E.JOBNAME = :OLDJOBNAME ");
			sql.append("   AND P.PRODUCTNAME IN ( :PRODUCTLIST ) ");
			sql.append("ORDER BY TO_NUMBER(P.POSITION) ");

			args.clear();
			args.put("OLDJOBNAME", oldJobName);
			args.put("PRODUCTLIST", prodList);

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> positionResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (positionResult.size() > 0)
			{
				List<String> positionList = CommonUtil.makeListBySqlResult(positionResult, "POSITION");
				String actualPosition = CommonUtil.toStringWithoutBrackets(positionList);
				String modifyPositionforMainMQC = getModifyPositionforSourceMQCJob(oldJobName, prodList, isRecycleMQC);
				String modifyPositionforRecycle = getModifyPositionforSourceRecycleMQCJob(oldJobName, prodList, isRecycleMQC);
				String modifyDestPositionforMain = getModifyPositionforDestMQCJob(oldJobName, jobName, prodList, isRecycleMQC);
				String modifyDestPositionforRecycle = getModifyPositionforDestRecycleMQCJob(oldJobName, jobName, prodList, isRecycleMQC);

				sql.setLength(0);
				sql.append("SELECT DISTINCT ");
				sql.append("       D.JOBNAME, ");
				sql.append("       D.PROCESSFLOWNAME, ");
				sql.append("       D.PROCESSFLOWVERSION, ");
				sql.append("       D.PROCESSOPERATIONNAME, ");
				sql.append("       D.PROCESSOPERATIONVERSION ");
				sql.append("  FROM CT_MQCPLANDETAIL D, CT_MQCPLANDETAIL_EXTENDED E ");
				sql.append(" WHERE D.JOBNAME = E.JOBNAME ");
				sql.append("   AND D.PROCESSFLOWNAME = E.PROCESSFLOWNAME ");
				sql.append("   AND D.PROCESSFLOWVERSION = E.PROCESSFLOWVERSION ");
				sql.append("   AND D.PROCESSOPERATIONNAME = E.PROCESSOPERATIONNAME ");
				sql.append("   AND D.PROCESSOPERATIONVERSION = E.PROCESSOPERATIONVERSION ");
				sql.append("   AND D.JOBNAME = :OLDJOBNAME ");
				sql.append("   AND E.PRODUCTNAME IN ( :PRODUCTLIST ) ");

				result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

				if (result.size() > 0)
				{
					if (!isRecycleMQC) // MQC Main Flow
					{
						for (Map<String, Object> map : result)
						{
							String processFlowName = ConvertUtil.getMapValueByName(map, "PROCESSFLOWNAME");
							String processFlowVersion = ConvertUtil.getMapValueByName(map, "PROCESSFLOWVERSION");
							String processOperationName = ConvertUtil.getMapValueByName(map, "PROCESSOPERATIONNAME");
							String processOperationVersion = ConvertUtil.getMapValueByName(map, "PROCESSOPERATIONVERSION");

							MQCPlanDetail oldPlanDetail = ExtendedObjectProxy.getMQCPlanDetailService().selectByKey(false,
									new Object[] { oldJobName, processFlowName, processFlowVersion, processOperationName, processOperationVersion });

							oldPlanDetailList.add(oldPlanDetail);
						}

						for (MQCPlanDetail oldPlanDetail : oldPlanDetailList)
						{
							MQCPlanDetail planDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);
							planDetail.setJobName(jobName);
							planDetail.setLotName(destinationLotData.getKey().getLotName());
							planDetail.setCarrierName(destCarrierName);
							planDetail.setLastEventTime(eventInfo.getEventTime());
							planDetail.setLastEventName(eventInfo.getEventName());
							planDetail.setLastEventUser(eventInfo.getEventUser());
							planDetail.setLastEventComment(eventInfo.getEventComment());
							planDetail.setPosition(actualPosition);

							destPlanDetailList.add(planDetail);

							MQCPlanDetail SrcPlanDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);

							SrcPlanDetail.setLastEventTime(eventInfo.getEventTime());
							SrcPlanDetail.setLastEventName(eventInfo.getEventName());
							SrcPlanDetail.setLastEventUser(eventInfo.getEventUser());
							SrcPlanDetail.setLastEventComment(eventInfo.getEventComment());
							if (isMQCwithReserveEQP)
							{
								String actualPositionforReserveEQP = getActualPositionforReserveEQP(oldJobName, prodList, planDetail.getProcessFlowName(), planDetail.getProcessOperationName());
								SrcPlanDetail.setPosition(actualPositionforReserveEQP);
							}
							else
							{
								SrcPlanDetail.setPosition(modifyPositionforMainMQC);
							}

							srcPlanDetailList.add(SrcPlanDetail);
						}

					}
					else
					// MQC Recycle Flow
					{
						boolean OnlyRecycleFlag = true;

						for (Map<String, Object> map : result)
						{
							String processFlowName = ConvertUtil.getMapValueByName(map, "PROCESSFLOWNAME");
							String processFlowVersion = ConvertUtil.getMapValueByName(map, "PROCESSFLOWVERSION");
							String processOperationName = ConvertUtil.getMapValueByName(map, "PROCESSOPERATIONNAME");
							String processOperationVersion = ConvertUtil.getMapValueByName(map, "PROCESSOPERATIONVERSION");

							ProcessFlowKey processFlowKey = new ProcessFlowKey();
							processFlowKey.setFactoryName(sourceLotData.getFactoryName());
							processFlowKey.setProcessFlowName(processFlowName);
							processFlowKey.setProcessFlowVersion(processFlowVersion);
							ProcessFlow processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);

							if (StringUtils.equalsIgnoreCase(processFlowData.getProcessFlowType(), "MQC"))
							{
								OnlyRecycleFlag = false;
							}

							MQCPlanDetail oldPlanDetail = ExtendedObjectProxy.getMQCPlanDetailService().selectByKey(false,
									new Object[] { oldJobName, processFlowName, processFlowVersion, processOperationName, processOperationVersion });

							oldPlanDetailList.add(oldPlanDetail);
						}

						for (MQCPlanDetail oldPlanDetail : oldPlanDetailList)
						{
							if (OnlyRecycleFlag)
							{
								log.info("Transfer product from SourceLot is Only in Recycle Flow");
								MQCPlanDetail DestPlanDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);

								ProcessFlowKey destProcessFlowKey = new ProcessFlowKey();
								destProcessFlowKey.setFactoryName(destinationLotData.getFactoryName());
								destProcessFlowKey.setProcessFlowName(DestPlanDetail.getProcessFlowName());
								destProcessFlowKey.setProcessFlowVersion(DestPlanDetail.getProcessFlowVersion());
								ProcessFlow DestPlanProcessFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(destProcessFlowKey);

								DestPlanDetail.setJobName(jobName);
								DestPlanDetail.setLotName(destinationLotData.getKey().getLotName());
								DestPlanDetail.setCarrierName(destCarrierName);
								DestPlanDetail.setLastEventTime(eventInfo.getEventTime());
								DestPlanDetail.setLastEventName(eventInfo.getEventName());
								DestPlanDetail.setLastEventUser(eventInfo.getEventUser());
								DestPlanDetail.setLastEventComment(eventInfo.getEventComment());

								if (StringUtils.equalsIgnoreCase(DestPlanProcessFlowData.getProcessFlowType(), "MQC"))
								{
									// set Positions for MainMQCFlow
									DestPlanDetail.setPosition(modifyDestPositionforMain);
								}
								else
								{
									// set Positions for RecycleFlow
									DestPlanDetail.setPosition(modifyDestPositionforRecycle);
								}

								destPlanDetailList.add(DestPlanDetail);

								MQCPlanDetail SrcPlanDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);

								SrcPlanDetail.setLastEventTime(eventInfo.getEventTime());
								SrcPlanDetail.setLastEventName(eventInfo.getEventName());
								SrcPlanDetail.setLastEventUser(eventInfo.getEventUser());
								SrcPlanDetail.setLastEventComment(eventInfo.getEventComment());
								SrcPlanDetail.setPosition(modifyPositionforRecycle);

								srcPlanDetailList.add(SrcPlanDetail);
							}
							else
							{
								log.info("Transfer product from SourceLot is not Only in Recycle Flow");
								MQCPlanDetail DestPlanDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);

								ProcessFlowKey destProcessFlowKey = new ProcessFlowKey();
								destProcessFlowKey.setFactoryName(destinationLotData.getFactoryName());
								destProcessFlowKey.setProcessFlowName(DestPlanDetail.getProcessFlowName());
								destProcessFlowKey.setProcessFlowVersion(DestPlanDetail.getProcessFlowVersion());
								ProcessFlow DestPlanProcessFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(destProcessFlowKey);

								DestPlanDetail.setJobName(jobName);
								DestPlanDetail.setLotName(destinationLotData.getKey().getLotName());
								DestPlanDetail.setCarrierName(destCarrierName);
								DestPlanDetail.setLastEventTime(eventInfo.getEventTime());
								DestPlanDetail.setLastEventName(eventInfo.getEventName());
								DestPlanDetail.setLastEventUser(eventInfo.getEventUser());
								DestPlanDetail.setLastEventComment(eventInfo.getEventComment());
								if (StringUtils.equalsIgnoreCase(DestPlanProcessFlowData.getProcessFlowType(), "MQC"))
								{
									// set Positions for MainMQCFlow
									DestPlanDetail.setPosition(modifyDestPositionforMain);
								}
								else
								{
									// set Positions for RecycleFlow
									DestPlanDetail.setPosition(modifyDestPositionforRecycle);
								}

								destPlanDetailList.add(DestPlanDetail);

								MQCPlanDetail SrcPlanDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);

								ProcessFlowKey SrcProcessFlowKey = new ProcessFlowKey();
								SrcProcessFlowKey.setFactoryName(sourceLotData.getFactoryName());
								SrcProcessFlowKey.setProcessFlowName(SrcPlanDetail.getProcessFlowName());
								SrcProcessFlowKey.setProcessFlowVersion(SrcPlanDetail.getProcessFlowVersion());
								ProcessFlow SrcPlanProcessFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(SrcProcessFlowKey);

								SrcPlanDetail.setLastEventTime(eventInfo.getEventTime());
								SrcPlanDetail.setLastEventName(eventInfo.getEventName());
								SrcPlanDetail.setLastEventUser(eventInfo.getEventUser());
								SrcPlanDetail.setLastEventComment(eventInfo.getEventComment());
								if (StringUtils.equalsIgnoreCase(SrcPlanProcessFlowData.getProcessFlowType(), "MQC"))
								{
									// set Positions for MainMQCFlow
									if (isMQCwithReserveEQP)
									{
										String actualPositionforReserveEQP = getActualPositionforReserveEQP(oldJobName, prodList, SrcPlanDetail.getProcessFlowName(),
												SrcPlanDetail.getProcessOperationName());
										SrcPlanDetail.setPosition(actualPositionforReserveEQP);
									}
									else
									{
										SrcPlanDetail.setPosition(modifyPositionforMainMQC);
									}
								}
								else
								{
									// set Positions for RecycleFlow
									SrcPlanDetail.setPosition(modifyPositionforRecycle);
								}

								srcPlanDetailList.add(SrcPlanDetail);
							}
						}

					}

					if (destPlanDetailList.size() > 0)
					{
						ExtendedObjectProxy.getMQCPlanDetailService().create(eventInfo, destPlanDetailList);
						log.info(destPlanDetailList.size() + " Rows inserted into CT_MQCPlanDetail");
					}

					if (srcPlanDetailList.size() > 0)
					{
						// Modify Source MQC Job in CT_MQCPlanDetail by Source Transfer ProductList
						try
						{
							for (MQCPlanDetail PlanDetail : srcPlanDetailList)
							{
								ExtendedObjectProxy.getMQCPlanDetailService().modify(eventInfo, PlanDetail);
							}
							log.info(srcPlanDetailList.size() + " Rows Modified into CT_MQCPlanDetail for Source MQC Job");
						}
						catch (Exception ex)
						{
							log.info("Error : Rows Modified into CT_MQCPlanDetail");
						}
					}

					int rows = 0;
					for (String SourceProduct : prodList)
					{
						for (Map<String, Object> map : positionResult)
						{
							String productName = ConvertUtil.getMapValueByName(map, "PRODUCTNAME");
							String position = ConvertUtil.getMapValueByName(map, "POSITION");

							if (productName.equalsIgnoreCase(SourceProduct))
							{
								// Update MQCPlanDetail_Extended
								sql.setLength(0);
								sql.append("INSERT INTO CT_MQCPLANDETAIL_EXTENDED ");
								sql.append("        (JOBNAME, PROCESSFLOWNAME, PROCESSFLOWVERSION, PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION,  ");
								sql.append("         PRODUCTNAME, POSITION, LOTNAME, FORBIDDENCODE, OLDFORBIDDENCODE,  ");
								sql.append("         LASTEVENTNAME, LASTEVENTTIME, LASTEVENTUSER, DUMMYUSEDCOUNT, RECIPENAME,  ");
								sql.append("         MACHINENAME) ");
								sql.append(" (SELECT :JOBNAME, PROCESSFLOWNAME, PROCESSFLOWVERSION, PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION,  ");
								sql.append("         PRODUCTNAME, :POSITION, :LOTNAME, FORBIDDENCODE, OLDFORBIDDENCODE,  ");
								sql.append("         :EVENTNAME, :EVENTTIME, :EVENTUSER, DUMMYUSEDCOUNT, RECIPENAME,  ");
								sql.append("         MACHINENAME FROM CT_MQCPLANDETAIL_EXTENDED ");
								sql.append("   WHERE JOBNAME = :OLDJOBNAME ");
								sql.append("     AND PRODUCTNAME = :PRODUCTNAME ) ");

								args.clear();
								args.put("JOBNAME", jobName);
								args.put("POSITION", position);
								args.put("LOTNAME", destinationLotData.getKey().getLotName());
								args.put("EVENTNAME", eventInfo.getEventName());
								args.put("EVENTTIME", eventInfo.getEventTime().toString());
								args.put("EVENTUSER", eventInfo.getEventUser());
								args.put("OLDJOBNAME", oldJobName);
								args.put("PRODUCTNAME", productName);

								GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), args);
								rows++;
							}
						}
					}
					log.info(rows + " Rows inserted into CT_MQCPlanDetail_Extended");

					// Delete Source MQC Job in MQCPlanDetail_Extended by Source ProductList
					ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().deleteMQCPlanDetail_ExtendedByProdList(oldJobName, prodList);

					log.info("Rows deleted into CT_MQCPlanDetail_Extended by Soruce MQC Job");

					// Lot without MQCJob is sent to MQC Bank.
					if (!ExistMQCProductbyLot(sourceLotData, oldJobName))
					{
						log.info("Source Lot [" + sourceLotData + "] does not have MQC Product.");
						// EventInfo eventInfo , MQCPlan planData, Lot lotData
						changeReturnMQCBank(eventInfo, sourceLotData, oldJobName);

						// After being sent to the MQC bank, Delete MQC Job
						if (CommonUtil.equalsIn(sourceLotData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare"))
						{
							log.info("After being sent to the MQC bank, Delete MQC Job. Source Lot : [" + sourceLotData + "]");
							MESLotServiceProxy.getLotServiceUtil().removeMQCJob(eventInfo, sourceLotData);
						}
					}

				}
				else
				{
					log.info("Not exist MQC Operation Info by Product" + prodList);
				}
			}
			else
			{
				log.info("Not exist MQC Product Info by Product" + prodList);

				// Lot without MQCJob is sent to MQC Bank.
				if (!ExistMQCProductbyLot(destinationLotData, jobName))
				{
					log.info("Destination Lot [" + destinationLotData + "] does not have MQC Product.");
					changeReturnMQCBank(eventInfo, destinationLotData, jobName);

					// After being sent to the MQC bank, Delete MQC Job
					if (CommonUtil.equalsIn(destinationLotData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare"))
					{
						log.info("After being sent to the MQC bank, Delete MQC Job. Destination Lot : [" + destinationLotData + "]");
						MESLotServiceProxy.getLotServiceUtil().removeMQCJob(eventInfo, destinationLotData);
					}
				}
			}
		}
		else
		{
			log.info("Not exist Release/Recycling MQC Info by Lot: " + sourceLotData.getKey().getLotName());
		}
	}

	public void UpdatePositionforMQCJob(EventInfo eventInfo, Lot LotData, String JobName, List<Element> productList) throws CustomException
	{
		List<String> sProductList = MESLotServiceProxy.getLotServiceUtil().getProductListByElementList(productList);
		List<String> sPositionList = MESLotServiceProxy.getLotServiceUtil().getProductPositionByElementList(productList);

		StringBuilder sql = new StringBuilder();
		Map<String, Object> args = new HashMap<String, Object>();

		sql.setLength(0);
		sql.append("SELECT DISTINCT P.POSITION, P.PRODUCTNAME");
		sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P ");
		sql.append(" WHERE E.PRODUCTNAME = P.PRODUCTNAME ");
		sql.append("   AND E.JOBNAME = :JOBNAME ");
		sql.append("   AND P.PRODUCTNAME IN ( :PRODUCTLIST ) ");
		sql.append("ORDER BY TO_NUMBER(P.POSITION) ");

		args.clear();
		args.put("JOBNAME", JobName);
		args.put("PRODUCTLIST", sProductList);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> positionResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (positionResult.size() > 0)
		{
			String actualPosition = CommonUtil.toStringWithoutBrackets(sPositionList);

			ExtendedObjectProxy.getMQCPlanDetailService().UpdatePositionChangedforMQCJob(eventInfo, LotData, JobName, actualPosition);

			// update position in CT_MQCPLANDETAIL_EXTENDED Table
			for (Element productE : productList)
			{
				String product = productE.getChildText("PRODUCTNAME");
				String position = productE.getChildText("POSITION");

				for (Map<String, Object> map : positionResult)
				{
					String productName = ConvertUtil.getMapValueByName(map, "PRODUCTNAME");

					if (productName.equalsIgnoreCase(product))
					{
						// Update MQCPlanDetail_Extended
						ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().updateMQCPlanDetail_ExtendedPosition(JobName, LotData.getKey().getLotName(), productName, position);
					}
				}
			}
		}
	}

	public List<String> getProductPositionByElementList(List<Element> eleDestProductList) throws CustomException
	{
		List<String> resultList = new ArrayList<>();

		for (Element eleProduct : eleDestProductList)
		{
			String position = SMessageUtil.getChildText(eleProduct, "POSITION", true);

			if (StringUtils.isNotEmpty(position))
			{
				resultList.add(position);
			}
		}

		return resultList;
	}

	public void setTPTJSampleDataWhenNoSampleCount(EventInfo eventInfo, Map<String, Object> samplePolicy, List<TPTJRule> TPTJRuleDataList, List<Element> productListElement, List<String> positionList,
			Lot lotData, List<SampleLotCount> sampleLotCount, double currentLotCount, double totalLotCount) throws CustomException
	{
		List<String> TPTJProductList = new ArrayList<String>();
		String ruleName = "";
		eventInfo.setEventComment("[TPTJ] System TPTJ Normal (No Sample Count). " + eventInfo.getEventComment());

//		String productSamplePositions = this.getProductSamplePositions(lotData, samplePolicy, positionList);
//		List<String> productSamplePositionList = CommonUtil.splitString(",", productSamplePositions); // delimeter is ','

		if (TPTJRuleDataList != null)
		{
			for (TPTJRule TPTJRule : TPTJRuleDataList)
			{
				log.info(" TPTJ Rule Name = " + TPTJRule.getRuleName());

				ruleName = TPTJRule.getRuleName();

				List<TPTJProduct> TPTJProList = ExtendedObjectProxy.getTPTJProductService().getTPTJProductListByLotFlow(lotData, TPTJRule);

				if (TPTJProList == null)
				{
					log.info("No TPTJProduct Data ! (RuleName:" + ruleName + ", RuleNum:" + String.valueOf(TPTJRule.getRuleNum()) + " , LotName:" + lotData.getKey().getLotName() + ")");
				}

				if (TPTJProList != null)
				{
					for (TPTJProduct TPTJProductData : TPTJProList)
						TPTJProductList.add(TPTJProductData.getProductName());

					// Sync TPTJ Count.
					ExtendedObjectProxy.getTPTJCountService().syncTPTJCount(eventInfo, lotData, TPTJRule);
				}

				break;
			}
		}

		List<String> actualSamplePositionList = new ArrayList<String>();
		List<String> notExistTPTJProductList = new ArrayList<String>();
		List<String> tptjPositionList = new ArrayList<String>();

		if (TPTJProductList.size() > 0)
		{
			// Set TPTJ Sample Product Data.
			for (String tptjProductName : TPTJProductList)
			{
				log.info("Start setTPTJSampleDataWhenNoSampleCount");
				log.info("setTPTJNormalRule : productName(" + tptjProductName + ") ");

				String sql = " SELECT PRODUCTNAME, POSITION FROM PRODUCT WHERE PRODUCTNAME = :productName AND LOTNAME = :lotName ";
				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("productName", tptjProductName);
				bindMap.put("lotName", lotData.getKey().getLotName());

				@SuppressWarnings("unchecked")
				List<Map<String, Object>> tptjProductResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

				boolean checkMsgFlag = false;

				if (tptjProductResult.size() > 0)
				{
					for (Element productElement : productListElement)
					{
						String position = productElement.getChildText("POSITION");
						String productName = productElement.getChildText("PRODUCTNAME");

						if (StringUtils.equals((String) tptjProductResult.get(0).get("PRODUCTNAME"), productName)
								&& StringUtils.equals(String.valueOf(tptjProductResult.get(0).get("POSITION")), position))
						{
							// set SamplingProduct Data(CT_SAMPLEPRODUCT)
							ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, (String) tptjProductResult.get(0).get("PRODUCTNAME"), lotData.getKey().getLotName(),
									lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
									lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), (String) samplePolicy.get("MACHINENAME"), (String) samplePolicy.get("TOPROCESSFLOWNAME"),
									(String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"), (String) samplePolicy.get("TOPROCESSOPERATIONVERSION"),
									"Y", (String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"),
									String.valueOf(tptjProductResult.get(0).get("POSITION")), "", "");

							actualSamplePositionList.add(String.valueOf(tptjProductResult.get(0).get("POSITION")));
							tptjPositionList.add(String.valueOf(tptjProductResult.get(0).get("POSITION")));

							checkMsgFlag = true;
							break;
						}
					}
				}

				if (!checkMsgFlag)
					notExistTPTJProductList.add(tptjProductName);
			}

			// TPTJ BackUp
			if (!notExistTPTJProductList.isEmpty())
			{
				log.info("setTPTJNormalData : BackUp Rule 1 ");
				int j = 0;
				List<Product> backupProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());

				for (Product backupProduct : backupProductList)
				{
					for (Element productElement : productListElement)
					{
						String productName = productElement.getChildText("PRODUCTNAME");

						if (StringUtils.equals(backupProduct.getKey().getProductName(), productName))
						{
							String position = productElement.getChildText("POSITION");

							if (!actualSamplePositionList.contains(position) && notExistTPTJProductList.contains(productName))
							{
								ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, backupProduct.getKey().getProductName(), lotData.getKey().getLotName(),
										lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
										lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), lotData.getMachineName(), (String) samplePolicy.get("TOPROCESSFLOWNAME"),
										(String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"), (String) samplePolicy.get("TOPROCESSOPERATIONVERSION"),
										"Y", (String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"), position, "", "");

								actualSamplePositionList.add(position);
								j++;
							}
							if (j == notExistTPTJProductList.size())
								break;
						}
					}
				}

				if (j != notExistTPTJProductList.size())
				{
					log.info("setTPTJNormalData : BackUp Rule 2 ");

					for (Element productElement : productListElement)
					{
						String productName = productElement.getChildText("PRODUCTNAME");
						String position = productElement.getChildText("POSITION");

						if (!actualSamplePositionList.contains(position))
						{
							ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, productName, lotData.getKey().getLotName(), lotData.getFactoryName(),
									lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(),
									lotData.getProcessOperationVersion(), lotData.getMachineName(), (String) samplePolicy.get("TOPROCESSFLOWNAME"), (String) samplePolicy.get("TOPROCESSFLOWVERSION"),
									(String) samplePolicy.get("TOPROCESSOPERATIONNAME"), (String) samplePolicy.get("TOPROCESSOPERATIONVERSION"), "Y",
									(String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"), position, "", "");

							actualSamplePositionList.add(position);
							j++;
						}

						if (j == notExistTPTJProductList.size())
							break;
					}
				}
			}

			if (!actualSamplePositionList.isEmpty())
			{
				log.info("setTPTJNormalData : Set Sample Lot ");

				// set SampleLotData(CT_SAMPLELOT)
				ExtendedObjectProxy.getSampleLotService().insertSampleLot(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
						lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
						lotData.getMachineName(), (String) samplePolicy.get("TOPROCESSFLOWNAME"), (String) samplePolicy.get("TOPROCESSFLOWVERSION"),
						(String) samplePolicy.get("TOPROCESSOPERATIONNAME"), (String) samplePolicy.get("TOPROCESSOPERATIONVERSION"), "Y", sampleLotCount.get(0).getLotSampleCount(),
						CommonUtil.StringValueOf(currentLotCount), CommonUtil.StringValueOf(totalLotCount), (String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"),
						(String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"), String.valueOf(actualSamplePositionList.size()), CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "", "",
						Integer.valueOf(samplePolicy.get("FLOWPRIORITY").toString()), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
						(String) samplePolicy.get("RETURNOPERATIONNAME"), (String) samplePolicy.get("RETURNOPERATIONVER"), "");
			}
		}
	}

	public void isBackUpTrackOut(Lot lotData, EventInfo eventInfo)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT T.PROCESSOPERATIONNAME,T.PROCESSOPERATIONVERSION,T.PROCESSFLOWNAME,T.PROCESSFLOWVERSION   ");
		sql.append("  FROM POSALTERPROCESSOPERATION P,TFOPOLICY T   ");
		sql.append(" WHERE 1=1 ");
		sql.append("   AND P.CONDITIONID = T.CONDITIONID ");
		sql.append("   AND P.CONDITIONNAME='BackUp'  ");
		sql.append("   AND TOPROCESSFLOWNAME= :BPPROCESSFLOWNAME ");
		sql.append("   AND TOPROCESSFLOWVERSION= :BPTOPROCESSFLOWVERSION ");
		sql.append("   AND TOPROCESSOPERATIONNAME= :BPTOPROCESSOPERATIONNAME ");
		sql.append("   AND TOPROCESSOPERATIONVERSION= :BPTOPROCESSOPERATIONVERSION ");
		sql.append("   AND RETURNPROCESSFLOWNAME= :REROCESSFLOWNAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("BPPROCESSFLOWNAME", lotData.getProcessFlowName());
		args.put("BPTOPROCESSFLOWVERSION", lotData.getProcessFlowVersion());
		args.put("BPTOPROCESSOPERATIONNAME", lotData.getProcessOperationName());
		args.put("BPTOPROCESSOPERATIONVERSION", lotData.getProcessOperationVersion());
		args.put("REROCESSFLOWNAME", lotData.getUdfs().get("RETURNFLOWNAME"));

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), args);
		try
		{
			if (result.size() == 1)
			{
				String yprocessOperationName = (String) result.get(0).get("PROCESSOPERATIONNAME");
				String yprocessOperationVersion = (String) result.get(0).get("PROCESSOPERATIONVERSION");
				String yprocessFlowName = (String) result.get(0).get("PROCESSFLOWNAME");
				String yprocessFlowVersion = (String) result.get(0).get("PROCESSFLOWVERSION");

				ExtendedObjectProxy.getProductQTimeService().moveInQTimeByLotBackUp(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), yprocessFlowName, yprocessFlowVersion,
						yprocessOperationName, yprocessOperationVersion, lotData.getUdfs().get("RETURNFLOWNAME"));
			}
			else
			{
				log.info("BcakUpFlow info is not Available,Check Policy");
			}
		}
		catch (Exception e)
		{
			log.info("no BackUp flow can use");
		}
	}

	public void isBackUpTrackIn(Lot lotData, EventInfo eventInfo)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT T.PROCESSOPERATIONNAME,T.PROCESSOPERATIONVERSION,T.PROCESSFLOWNAME,T.PROCESSFLOWVERSION   ");
		sql.append("  FROM POSALTERPROCESSOPERATION P,TFOPOLICY T   ");
		sql.append(" WHERE 1=1 ");
		sql.append("   AND P.CONDITIONID = T.CONDITIONID ");
		sql.append("   AND P.CONDITIONNAME='BackUp'  ");
		sql.append("   AND TOPROCESSFLOWNAME= :BPPROCESSFLOWNAME ");
		sql.append("   AND TOPROCESSFLOWVERSION= :BPTOPROCESSFLOWVERSION ");
		sql.append("   AND TOPROCESSOPERATIONNAME= :BPTOPROCESSOPERATIONNAME ");
		sql.append("   AND TOPROCESSOPERATIONVERSION= :BPTOPROCESSOPERATIONVERSION ");
		sql.append("   AND RETURNPROCESSFLOWNAME= :REROCESSFLOWNAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("BPPROCESSFLOWNAME", lotData.getProcessFlowName());
		args.put("BPTOPROCESSFLOWVERSION", lotData.getProcessFlowVersion());
		args.put("BPTOPROCESSOPERATIONNAME", lotData.getProcessOperationName());
		args.put("BPTOPROCESSOPERATIONVERSION", lotData.getProcessOperationVersion());
		args.put("REROCESSFLOWNAME", lotData.getUdfs().get("RETURNFLOWNAME"));

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), args);
		try
		{
			if (result.size() == 1)
			{
				String yprocessOperationName = (String) result.get(0).get("PROCESSOPERATIONNAME");
				String yprocessFlowName = (String) result.get(0).get("PROCESSFLOWNAME");
				ExtendedObjectProxy.getProductQTimeService().exitQTimeByLot(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), yprocessFlowName, yprocessOperationName);
			}
			else
			{
				log.info("BcakUpFlow info is not Available,Check Policy");
			}
		}
		catch (Exception e)
		{
			log.info("no BackUp flow can use");
		}
	}

	public boolean isSampleLot(String lotName)
	{
		boolean isSampleLot = false;

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT CS.LOTNAME   ");
		sql.append("  FROM CT_SAMPLELOT CS, LOT L   ");
		sql.append(" WHERE 1=1 ");
		sql.append("   AND CS.LOTNAME = :LOTNAME ");
		sql.append("   AND L.LOTSTATE = :LOTSTATE  ");
		sql.append("   AND CS.LOTNAME = L.LOTNAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("LOTNAME", lotName);
		args.put("LOTSTATE", GenericServiceProxy.getConstantMap().Lot_Released);

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			isSampleLot = true;
		}

		return isSampleLot;
	}

	public boolean isSampleLot(String lotName, String factoryName)
	{
		boolean isSampleLot = false;

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT CS.LOTNAME   ");
		sql.append("  FROM CT_SAMPLELOT CS, LOT L   ");
		sql.append(" WHERE 1=1 ");
		sql.append("   AND CS.LOTNAME = :LOTNAME ");
		sql.append("   AND L.LOTSTATE = :LOTSTATE  ");
		sql.append("   AND CS.FACTORYNAME = :FACTORYNAME  ");
		sql.append("   AND CS.LOTNAME = L.LOTNAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("LOTNAME", lotName);
		args.put("LOTSTATE", GenericServiceProxy.getConstantMap().Lot_Released);
		args.put("FACTORYNAME", factoryName);

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			isSampleLot = true;
		}

		return isSampleLot;
	}

	public void inheritforSampling(Lot sourceLotData, Lot destinationLotData, List<Element> prodList, EventInfo eventInfo) throws CustomException
	{
		log.info("Start inherit Sampling");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		List<String> srcProductList = new ArrayList<String>();
		List<String> destProductList = new ArrayList<String>();

		String srcLotName = sourceLotData.getKey().getLotName();
		String destLotName = destinationLotData.getKey().getLotName();

		srcProductList = getProductList(srcLotName);
		destProductList = getProductList(destLotName);

		List<String> sProductList = MESLotServiceProxy.getLotServiceUtil().getProductListByElementList(prodList);

		// Update CT_SAMPLEPRODUCT (SrcLot -> DestLot)
		log.info("Start Update CT_SAMPLEPRODUCT Table");
		List<SampleProduct> sampleProductDataList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListByProductList(srcLotName, sProductList);

		int rows = 0;
		int UpdateRows = 0;
		int delRows = 0;
		int InsRows = 0;

		if (sampleProductDataList != null)
		{
			for (SampleProduct sampleProduct : sampleProductDataList)
			{
				String productName = sampleProduct.getProductName();
				String factoryName = sampleProduct.getFactoryName();
				String productSpecName = sampleProduct.getProductSpecName();
				String productSpecVersion = sampleProduct.getProductSpecVersion();
				String processFlowName = sampleProduct.getProcessFlowName();
				String processFlowVersion = sampleProduct.getProcessFlowVersion();
				String processOperationName = sampleProduct.getProcessOperationName();
				String processOperationVersion = sampleProduct.getProcessOperationVersion();
				String toProcessFlowName = sampleProduct.getToProcessFlowName();
				String toProcessFlowVersion = sampleProduct.getToProcessFlowVersion();
				String toProcessOperationName = sampleProduct.getToProcessOperationName();
				String toProcessOperationVersion = sampleProduct.getToProcessOperationVersion();

				for (Element element : prodList)
				{
					String sProductName = element.getChildText("PRODUCTNAME");
					String sPosition = element.getChildText("POSITION");

					if (sProductName.equalsIgnoreCase(productName))
					{
						List<SampleProduct> sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListWithoutMachineName(sProductName, srcLotName, factoryName,
								productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion, toProcessFlowName, toProcessFlowVersion,
								toProcessOperationName, toProcessOperationVersion);

						for (SampleProduct sampleProd : sampleProductList)
						{
							ExtendedObjectProxy.getSampleProductService().updateSampleProductKey(sampleProd, eventInfo, "", destLotName, "", "", "", "", "", "", "", "", "", "", "", "", "", "", "",
									sPosition, "", "");
						}

						rows++;
						break;
					}
				}

			}
			log.info(rows + " Rows Updated into CT_SAMPLEPRODUCT");
			log.info("End Update CT_SAMPLEPRODUCT Table");

			// get CT_SAMPLELOT (Standard SourceLot)
			log.info("Start get CT_SAMPLELOT Table by SourceLot");

			List<SampleLot> sampleLotListBySepc = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListBySpec(srcLotName, sourceLotData.getFactoryName(), sourceLotData.getProductSpecName(),
					sourceLotData.getProductSpecVersion());

			if (sampleLotListBySepc != null)
			{
				for (SampleLot sampleLot : sampleLotListBySepc)
				{
					String factoryName = sampleLot.getFactoryName();
					String productSpecName = sampleLot.getProductSpecName();
					String productSpecVersion = sampleLot.getProductSpecVersion();
					String processFlowName = sampleLot.getProcessFlowName();
					String processFlowVersion = sampleLot.getProcessFlowVersion();
					String processOperationName = sampleLot.getProcessOperationName();
					String processOperationVersion = sampleLot.getProcessOperationVersion();
					String toProcessFlowName = sampleLot.getToProcessFlowName();
					String toProcessFlowVersion = sampleLot.getToProcessFlowVersion();
					String toProcessOperationName = sampleLot.getToProcessOperationName();
					String toProcessOperationVersion = sampleLot.getToProcessOperationVersion();

					// Get CT_SAMPLEPRODUCT Data
					List<String> actualSamplePositionList = getSampleProductPositionData(srcLotName, srcProductList, factoryName, productSpecName, processFlowName, processOperationName,
							toProcessFlowName, toProcessOperationName);

					if (actualSamplePositionList.size() > 0)
					{
						// Get ACTUALPRODUCTCOUNT, ACTUALSAMPLEPOSITION value
						String actualSamplePosition = CommonUtil.toStringWithoutBrackets(actualSamplePositionList);
						String actualProductCount = Integer.toString(actualSamplePositionList.size());

						log.info("Start Update CT_SAMPLELOT Table for SourceLot : " + srcLotName);

						// UPDATE ACTUALPRODUCTCOUNT, ACTUALSAMPLEPOSITION in CT_SAMPLELOT (standard SourceLot)
						ExtendedObjectProxy.getSampleLotService().updateSampleLotWithoutMachineName(eventInfo, srcLotName, factoryName, productSpecName, productSpecVersion, toProcessFlowName,
								toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion,
								"", "", "", "", "", "", actualProductCount, actualSamplePosition, "", "", "", "", "", "", "", "");

						UpdateRows++;
					}
					else
					{
						// Delete Sample Data in CT_SAMPLELOT (standard SourceLot)
						ExtendedObjectProxy.getSampleLotService().deleteSampleLotWithOutMachineName(eventInfo, srcLotName, factoryName, productSpecName, productSpecVersion, processFlowName,
								processFlowVersion, processOperationName, processOperationVersion, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);

						delRows++;
					}
				}

				log.info(UpdateRows + " Rows Updated into CT_SAMPLELOT for SourceLot : " + srcLotName);
				log.info(delRows + " Rows Deleted into CT_SAMPLELOT for SourceLot : " + srcLotName);

				log.info("End Update CT_SAMPLELOT Table for SourceLot : " + srcLotName);
			}
			else
			{
				log.info("Not exist Sampling Info in CT_SAMPLELOT [" + srcLotName + "]");
			}

			// Update CT_SAMPLELOT (Standard DestLot)
			log.info("Start get CT_SAMPLELOT Table by DestLot");

			if (sampleLotListBySepc != null)
			{
				for (SampleLot sampleLot : sampleLotListBySepc)
				{
					String factoryName = sampleLot.getFactoryName();
					String productSpecName = sampleLot.getProductSpecName();
					String productSpecVersion = sampleLot.getProductSpecVersion();
					String processFlowName = sampleLot.getProcessFlowName();
					String processFlowVersion = sampleLot.getProcessFlowVersion();
					String processOperationName = sampleLot.getProcessOperationName();
					String processOperationVersion = sampleLot.getProcessOperationVersion();
					String toProcessFlowName = sampleLot.getToProcessFlowName();
					String toProcessFlowVersion = sampleLot.getToProcessFlowVersion();
					String toProcessOperationName = sampleLot.getToProcessOperationName();
					String toProcessOperationVersion = sampleLot.getToProcessOperationVersion();

					// Get CT_SAMPLEPRODUCT Data
					List<String> actualSamplePositionList = getSampleProductPositionData(destLotName, destProductList, factoryName, productSpecName, processFlowName, processOperationName,
							toProcessFlowName, toProcessOperationName);

					if (actualSamplePositionList.size() > 0)
					{
						// Get ACTUALPRODUCTCOUNT, ACTUALSAMPLEPOSITION value
						String actualSamplePosition = CommonUtil.toStringWithoutBrackets(actualSamplePositionList);
						String actualProductCount = Integer.toString(actualSamplePositionList.size());

						log.info("Start Insert CT_SAMPLELOT Table for DestinationLot : " + destLotName);

						// INSERT ACTUALPRODUCTCOUNT, ACTUALSAMPLEPOSITION in CT_SAMPLELOT (standard DestLot)
						ExtendedObjectProxy.getSampleLotService().insertSampleLot(eventInfo, destLotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion,
								processOperationName, processOperationVersion, sampleLot.getMachineName(), toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion,
								sampleLot.getLotSampleFlag(), sampleLot.getLotSampleCount(), sampleLot.getCurrentLotCount(), sampleLot.getTotalLotCount(), sampleLot.getProductSampleCount(),
								sampleLot.getProductSamplePosition(), actualProductCount, actualSamplePosition, sampleLot.getManualSampleFlag(), "",
								Integer.parseInt(sampleLot.getPriority().toString()), sampleLot.getReturnProcessFlowName(), sampleLot.getReturnProcessFlowVersion(),
								sampleLot.getReturnOperationName(), sampleLot.getReturnOperationVersion(), sampleLot.getForceSamplingFlag());

						InsRows++;
					}
				}

				log.info(InsRows + " Rows Insert into CT_SAMPLELOT for DestinationLot : " + destLotName);
				log.info("End Insert CT_SAMPLELOT Table for DestLot : " + destLotName);
			}
			else
			{
				log.info("Not exist Sampling Info in CT_SAMPLELOT [" + destLotName + "]");
			}
		}
		else
		{
			log.info("Not exist Sampling product in CT_SAMPLEPRODUCT" + sProductList);
		}

	}

	public boolean getCutProductInfo(List<Product> cutProdList, String srcLotName)
	{
		boolean checkFlag = true;

		for (Product cutProd : cutProdList)
		{
			if (StringUtils.equals(cutProd.getLotName(), srcLotName))
			{
				log.info("Product[" + cutProd.getKey().getProductName() + "] did not proceed.");
				checkFlag = false;
				break;
			}
		}

		return checkFlag;
	}

	public List<String> getProductList(String lotName) throws CustomException
	{
		List<String> resultList = new ArrayList<>();

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT PRODUCTNAME ");
		sql.append("  FROM PRODUCT ");
		sql.append(" WHERE LOTNAME = :LOTNAME ");
		sql.append("   AND PRODUCTSTATE != 'Scrapped' ");
		sql.append("   AND PRODUCTSTATE != 'Consumed' ");
		sql.append("ORDER BY POSITION ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("LOTNAME", lotName);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (sqlResult.size() > 0)
		{
			for (Map<String, Object> result : sqlResult)
			{
				String productName = ConvertUtil.getMapValueByName(result, "PRODUCTNAME");
				if (StringUtils.isNotEmpty(productName))
				{
					resultList.add(productName);
				}
			}
		}

		return resultList;
	}

	public List<String> getSampleProductPositionData(String lotName, List<String> productList, String factoryName, String productSpecName, String processFlowName, String processOperationName,
			String toProcessFlowName, String toProcessOperationName) throws CustomException
	{
		List<String> ActualSamplePositionList = new ArrayList<>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT CS.ACTUALSAMPLEPOSITION, CS.PRODUCTNAME    ");
		sql.append("  FROM CT_SAMPLEPRODUCT CS, PRODUCT P     ");
		sql.append(" WHERE 1 = 1     ");
		sql.append("   AND CS.PRODUCTNAME = P.PRODUCTNAME  ");
		sql.append("   AND CS.LOTNAME = P.LOTNAME  ");
		sql.append("   AND P.LOTNAME = :LOTNAME ");
		sql.append("   AND CS.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND CS.PRODUCTSPECNAME = :PRODUCTSPECNAME  ");
		sql.append("   AND CS.PROCESSFLOWNAME = :PROCESSFLOWNAME  ");
		sql.append("   AND CS.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME    ");
		sql.append("   AND CS.TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME     ");
		sql.append("   AND CS.TOPROCESSOPERATIONNAME = :TOPROCESSOPERATIONNAME      ");
		sql.append("   AND P.PRODUCTNAME IN ( :PRODUCTLIST )   ");
		sql.append("ORDER BY TO_NUMBER(ACTUALSAMPLEPOSITION)   ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("LOTNAME", lotName);
		args.put("FACTORYNAME", factoryName);
		args.put("PRODUCTSPECNAME", productSpecName);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSOPERATIONNAME", processOperationName);
		args.put("TOPROCESSFLOWNAME", toProcessFlowName);
		args.put("TOPROCESSOPERATIONNAME", toProcessOperationName);
		args.put("PRODUCTLIST", productList);

		try
		{
			List<Map<String, Object>> positionResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (positionResult.size() > 0)
			{
				log.info("Get Position Info Info in CT_SAMPLEPRODUCT by ProductList");

				ActualSamplePositionList = CommonUtil.makeListBySqlResult(positionResult, "ACTUALSAMPLEPOSITION");
			}
		}
		catch (Exception ex)
		{
			log.info("Error : Get Positions Info in CT_SAMPLEPRODUCT by ProductList");
		}

		return ActualSamplePositionList;
	}

	public void modifySampleDataPositionforInherit(EventInfo eventInfo, Lot lotData, List<Element> productList) throws CustomException
	{
		try
		{
			log.info("Modify SampleData Position Start.");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			List<String> sProductList = MESLotServiceProxy.getLotServiceUtil().getProductListByElementList(productList);
			String lotName = lotData.getKey().getLotName();

			List<SampleProduct> sampleProductDataList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListByProductList(lotName, sProductList);

			int UpdateRows = 0;
			int delRows = 0;

			if (sampleProductDataList != null)
			{
				for (SampleProduct sampleProduct : sampleProductDataList)
				{
					String productName = sampleProduct.getProductName();
					String factoryName = sampleProduct.getFactoryName();
					String productSpecName = sampleProduct.getProductSpecName();
					String productSpecVersion = sampleProduct.getProductSpecVersion();
					String processFlowName = sampleProduct.getProcessFlowName();
					String processFlowVersion = sampleProduct.getProcessFlowVersion();
					String processOperationName = sampleProduct.getProcessOperationName();
					String processOperationVersion = sampleProduct.getProcessOperationVersion();
					String toProcessFlowName = sampleProduct.getToProcessFlowName();
					String toProcessFlowVersion = sampleProduct.getToProcessFlowVersion();
					String toProcessOperationName = sampleProduct.getToProcessOperationName();
					String toProcessOperationVersion = sampleProduct.getToProcessOperationVersion();

					for (Element element : productList)
					{
						String sProductName = element.getChildText("PRODUCTNAME");
						String sPosition = element.getChildText("POSITION");

						if (sProductName.equalsIgnoreCase(productName))
						{
							ExtendedObjectProxy.getSampleProductService().updateSampleProductWithoutMachineName(eventInfo, sProductName, lotName, factoryName, productSpecName, productSpecVersion,
									processFlowName, processFlowVersion, processOperationName, processOperationVersion, toProcessFlowName, toProcessFlowVersion, toProcessOperationName,
									toProcessOperationVersion, "", "", "", "", sPosition, "");

							break;
						}
					}

				}

				List<SampleLot> sampleLotListBySepc = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListBySpec(lotName, lotData.getFactoryName(), lotData.getProductSpecName(),
						lotData.getProductSpecVersion());

				if (sampleLotListBySepc != null)
				{
					for (SampleLot sampleLot : sampleLotListBySepc)
					{
						String factoryName = sampleLot.getFactoryName();
						String productSpecName = sampleLot.getProductSpecName();
						String productSpecVersion = sampleLot.getProductSpecVersion();
						String processFlowName = sampleLot.getProcessFlowName();
						String processFlowVersion = sampleLot.getProcessFlowVersion();
						String processOperationName = sampleLot.getProcessOperationName();
						String processOperationVersion = sampleLot.getProcessOperationVersion();
						String toProcessFlowName = sampleLot.getToProcessFlowName();
						String toProcessFlowVersion = sampleLot.getToProcessFlowVersion();
						String toProcessOperationName = sampleLot.getToProcessOperationName();
						String toProcessOperationVersion = sampleLot.getToProcessOperationVersion();

						// Get CT_SAMPLEPRODUCT Data
						List<String> actualSamplePositionList = getSampleProductPositionData(lotName, sProductList, factoryName, productSpecName, processFlowName, processOperationName,
								toProcessFlowName, toProcessOperationName);

						if (actualSamplePositionList.size() > 0)
						{
							// Get ACTUALPRODUCTCOUNT, ACTUALSAMPLEPOSITION value
							String actualSamplePosition = CommonUtil.toStringWithoutBrackets(actualSamplePositionList);
							String actualProductCount = Integer.toString(actualSamplePositionList.size());

							log.info("Start Update CT_SAMPLELOT Table for SourceLot : " + lotData.getKey().getLotName());

							// UPDATE ACTUALPRODUCTCOUNT, ACTUALSAMPLEPOSITION in CT_SAMPLELOT (standard SourceLot)
							ExtendedObjectProxy.getSampleLotService().updateSampleLotWithoutMachineName(eventInfo, lotName, factoryName, productSpecName, productSpecVersion, processFlowName,
									processFlowVersion, processOperationName, processOperationVersion, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion, "",
									"", "", "", "", "", actualProductCount, actualSamplePosition, "", "", "", "", "", "", "", "");

							UpdateRows++;
						}
						else
						{
							// Delete Sample Data in CT_SAMPLELOT (standard SourceLot)
							ExtendedObjectProxy.getSampleLotService().deleteSampleLotWithOutMachineName(eventInfo, lotName, factoryName, productSpecName, productSpecVersion, processFlowName,
									processFlowVersion, processOperationName, processOperationVersion, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);

							delRows++;
						}
					}

					log.info(UpdateRows + " Rows Updated into CT_SAMPLELOT for LotData : " + lotName);
					log.info(delRows + " Rows Deleted into CT_SAMPLELOT for LotData : " + lotName);
				}
				log.info("Modify SampleLot Position End.");
			}
		}
		catch (Exception e)
		{
			log.info("Modify SampleLot Position Error !");
		}

	}

	public List<Map<String, Object>> getChamberSamplePolicyList(String factoryName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion,
			String machineName, String toProcessFlowName, String toProcessFlowVersion, String toProcessOperationName, String toProcessOperationVersion) throws CustomException
	{
		Map<String, Object> bindMap = new HashMap<String, Object>();

		String sqlAddToProcessFlowName = "";
		String sqlAddToProcessFlowVersion = "";
		String sqlAddToProcessOperationName = "";
		String sqlAddToProcessOperationVersion = "";

		if (StringUtils.isNotEmpty(toProcessFlowName))
		{
			bindMap.put("TOPROCESSFLOWNAME", toProcessFlowName);
			sqlAddToProcessFlowName = "    AND C.TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME ";
		}
		if (StringUtils.isNotEmpty(toProcessFlowVersion))
		{
			bindMap.put("TOPROCESSFLOWVERSION", toProcessFlowVersion);
			sqlAddToProcessFlowVersion = "    AND C.TOPROCESSFLOWVERSION = :TOPROCESSFLOWVERSION ";
		}
		if (StringUtils.isNotEmpty(toProcessOperationName))
		{
			bindMap.put("TOPROCESSOPERATIONNAME", toProcessOperationName);
			sqlAddToProcessOperationName = "    AND C.TOPROCESSOPERATIONNAME = :TOPROCESSOPERATIONNAME ";
		}
		if (StringUtils.isNotEmpty(toProcessOperationVersion))
		{
			bindMap.put("TOPROCESSOPERATIONVERSION", toProcessOperationVersion);
			sqlAddToProcessOperationVersion = "    AND C.TOPROCESSOPERATIONVERSION = :TOPROCESSOPERATIONVERSION ";
		}

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT T.FACTORYNAME, ");
		sql.append("       T.PROCESSFLOWNAME, ");
		sql.append("       T.PROCESSFLOWVERSION, ");
		sql.append("       T.PROCESSOPERATIONNAME, ");
		sql.append("       T.PROCESSOPERATIONVERSION, ");
		sql.append("       T.MACHINENAME, ");
		sql.append("       C.TOPROCESSFLOWNAME, ");
		sql.append("       C.TOPROCESSFLOWVERSION, ");
		sql.append("       C.TOPROCESSOPERATIONNAME, ");
		sql.append("       C.TOPROCESSOPERATIONVERSION, ");
		sql.append("       C.FLOWPRIORITY, ");
		sql.append("       C.CHAMBERSAMPLECOUNT, ");
		sql.append("       C.RETURNOPERATIONNAME, ");
		sql.append("       C.RETURNOPERATIONVER, ");
		sql.append("       NVL(C.TFESAMPLEFLAG, 'N') AS TFESAMPLEFLAG ");
		sql.append("  FROM TFOMPOLICY T, POSCHAMBERSAMPLE C ");
		sql.append(" WHERE T.CONDITIONID = C.CONDITIONID ");
		sql.append("   AND T.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		sql.append("   AND T.MACHINENAME = :MACHINENAME ");
		sql.append(sqlAddToProcessFlowName);
		sql.append(sqlAddToProcessFlowVersion);
		sql.append(sqlAddToProcessOperationName);
		sql.append(sqlAddToProcessOperationVersion);
		sql.append(" ORDER BY C.FLOWPRIORITY ASC ");

		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PROCESSFLOWNAME", processFlowName);
		bindMap.put("PROCESSFLOWVERSION", processFlowVersion);
		bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		bindMap.put("PROCESSOPERATIONVERSION", processOperationVersion);
		bindMap.put("MACHINENAME", machineName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		return sqlResult;
	}

	public List<SampleLot> setChamberSamplingData(EventInfo eventInfo, Lot lotData, Machine machineData, Map<String, Object> chamberSamplePolicy, String factoryName, String productSpecName,
			String productSpecVersion, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion) throws CustomException
	{
		String actualSamplePosition = "";
		List<String> actualSamplePositionList = new ArrayList<String>();
		List<String> chamberList = new ArrayList<String>();
		List<String> productList = new ArrayList<String>();

		int chamberSampleCount = Integer.valueOf((String) chamberSamplePolicy.get("CHAMBERSAMPLECOUNT"));

		// Get SamplingRule
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT MACHINENAME ");
		sql.append("  FROM MACHINESPEC ");
		sql.append(" WHERE MACHINEGROUPNAME = :MACHINEGROUPNAME ");
		sql.append("   AND CHAMBERYN = 'Y' ");
		sql.append("   AND SUPERMACHINENAME IN (SELECT MACHINENAME ");
		sql.append("                              FROM MACHINESPEC ");
		sql.append("                             WHERE SUPERMACHINENAME = :MACHINENAME) ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("MACHINEGROUPNAME", machineData.getMachineGroupName());
		args.put("MACHINENAME", machineData.getKey().getMachineName());

		List<Map<String, Object>> productResult = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			chamberList = CommonUtil.makeListBySqlResult(result, "MACHINENAME");
			log.info("ChamberSampling ChamberList : " + chamberList);
		}

		int chamberCount = 0;
		for (String chamber : chamberList)
		{
			chamberCount++;

			sql.setLength(0);
			sql.append("SELECT DISTINCT C.PRODUCTNAME ");
			sql.append("  FROM CT_COMPONENTINCHAMBERHIST C, MACHINESPEC S, PRODUCT P ");
			sql.append(" WHERE C.PRODUCTNAME = P.PRODUCTNAME ");
			sql.append("   AND P.LOTNAME = :LOTNAME ");
			sql.append("   AND C.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
			sql.append("   AND C.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
			sql.append("   AND C.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
			sql.append("   AND C.MACHINENAME = :MACHINENAME ");
			sql.append("   AND C.MACHINENAME = S.MACHINENAME ");
			sql.append("   AND S.MACHINEGROUPNAME IN ('CVD', 'PVD', 'DRE') ");
			sql.append("   AND C.MATERIALLOCATIONNAME IN (:MATERIALLOCATIONNAME) ");
			sql.append("   AND C.EVENTNAME = 'ComponentOutSubUnit' ");
			sql.append("   AND P.PRODUCTSTATE NOT IN ('Scrapped', 'Consumed') ");
			sql.append("ORDER BY C.PRODUCTNAME ");

			args.clear();
			args.put("LOTNAME", lotData.getKey().getLotName());
			args.put("PRODUCTSPECNAME", productSpecName);
			args.put("PROCESSFLOWNAME", processFlowName);
			args.put("PROCESSOPERATIONNAME", processOperationName);
			args.put("MACHINENAME", machineData.getKey().getMachineName());
			args.put("MATERIALLOCATIONNAME", chamber);

			productResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (productResult.size() > 0)
			{
				int count = 0;
				boolean insertFlag = true;
				List<String> chamProdList = new ArrayList<String>();

				productList = CommonUtil.makeListBySqlResult(productResult, "PRODUCTNAME");
				log.info("ChamberSampling [" + String.valueOf(chamberCount) + "] ChamberName : " + chamber);
				log.info("ChamberSampling ProductList : " + productList);

				for (String productName : productList)
				{
					Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

					// Get SampleLotData(CT_SAMPLEPRODUCT)
					List<SampleProduct> sampleProductData = ExtendedObjectProxy.getSampleProductService().getSampleProductDataList(productName, productData.getLotName(), productData.getFactoryName(),
							productData.getProductSpecName(), productData.getProductSpecVersion(), (String) chamberSamplePolicy.get("PROCESSFLOWNAME"),
							(String) chamberSamplePolicy.get("PROCESSFLOWVERSION"), (String) chamberSamplePolicy.get("PROCESSOPERATIONNAME"),
							(String) chamberSamplePolicy.get("PROCESSOPERATIONVERSION"), machineData.getKey().getMachineName(), (String) chamberSamplePolicy.get("TOPROCESSFLOWNAME"),
							(String) chamberSamplePolicy.get("TOPROCESSFLOWVERSION"), (String) chamberSamplePolicy.get("TOPROCESSOPERATIONNAME"),
							(String) chamberSamplePolicy.get("TOPROCESSOPERATIONVERSION"));

					if (sampleProductData.size() > 0)
					{
						count++;

						if (count == chamberSampleCount)
						{
							log.info("Complete ChamberSampling : Not Insert [ChamberName : " + chamber + "]");
							insertFlag = false;
							break;
						}
					}
					else
					{
						chamProdList.add(productName);
					}
				}

				if (insertFlag)
				{
					log.info("ChamberSampling ChamProductList : " + chamProdList);

					for (String productName : chamProdList)
					{
						Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

						// Get SampleLotData(CT_SAMPLEPRODUCT)
						List<SampleProduct> sampleProductData = ExtendedObjectProxy.getSampleProductService().getSampleProductDataList(productName, productData.getLotName(),
								productData.getFactoryName(), productData.getProductSpecName(), productData.getProductSpecVersion(), (String) chamberSamplePolicy.get("PROCESSFLOWNAME"),
								(String) chamberSamplePolicy.get("PROCESSFLOWVERSION"), (String) chamberSamplePolicy.get("PROCESSOPERATIONNAME"),
								(String) chamberSamplePolicy.get("PROCESSOPERATIONVERSION"), machineData.getKey().getMachineName(), (String) chamberSamplePolicy.get("TOPROCESSFLOWNAME"),
								(String) chamberSamplePolicy.get("TOPROCESSFLOWVERSION"), (String) chamberSamplePolicy.get("TOPROCESSOPERATIONNAME"),
								(String) chamberSamplePolicy.get("TOPROCESSOPERATIONVERSION"));

						if (sampleProductData.size() < 1)
						{
							ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, productName, productData.getLotName(), productData.getFactoryName(),
									productData.getProductSpecName(), productData.getProductSpecVersion(), (String) chamberSamplePolicy.get("PROCESSFLOWNAME"),
									(String) chamberSamplePolicy.get("PROCESSFLOWVERSION"), (String) chamberSamplePolicy.get("PROCESSOPERATIONNAME"),
									(String) chamberSamplePolicy.get("PROCESSOPERATIONVERSION"), machineData.getKey().getMachineName(), (String) chamberSamplePolicy.get("TOPROCESSFLOWNAME"),
									(String) chamberSamplePolicy.get("TOPROCESSFLOWVERSION"), (String) chamberSamplePolicy.get("TOPROCESSOPERATIONNAME"),
									(String) chamberSamplePolicy.get("TOPROCESSOPERATIONVERSION"), "Y", "", "", String.valueOf(productData.getPosition()), "", "");

							actualSamplePositionList.add(String.valueOf(productData.getPosition()));
						}

						count++;

						if (chamberSampleCount == count)
						{
							log.info("Complete ChamberSampling : Insert Action [ChamberName : " + chamber + "]");
							break;
						}
					}
				}
			}
		}

		// Get SampleLotData(CT_SAMPLELOT)
		List<SampleLot> sampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListWithOutMachineName(lotData.getKey().getLotName(), factoryName, productSpecName, productSpecVersion,
				processFlowName, processFlowVersion, processOperationName, processOperationVersion, (String) chamberSamplePolicy.get("TOPROCESSFLOWNAME"),
				(String) chamberSamplePolicy.get("TOPROCESSFLOWVERSION"), (String) chamberSamplePolicy.get("TOPROCESSOPERATIONNAME"), (String) chamberSamplePolicy.get("TOPROCESSOPERATIONVERSION"));

		if (sampleLot == null)
		{
			// set SampleLotData(CT_SAMPLELOT)
			ExtendedObjectProxy.getSampleLotService().insertSampleLot(eventInfo, lotData.getKey().getLotName(), factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion,
					processOperationName, processOperationVersion, lotData.getMachineName(), (String) chamberSamplePolicy.get("TOPROCESSFLOWNAME"),
					(String) chamberSamplePolicy.get("TOPROCESSFLOWVERSION"), (String) chamberSamplePolicy.get("TOPROCESSOPERATIONNAME"),
					(String) chamberSamplePolicy.get("TOPROCESSOPERATIONVERSION"), "Y", "", "", "", "", "", String.valueOf(actualSamplePositionList.size()),
					CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "", "", Integer.valueOf(chamberSamplePolicy.get("FLOWPRIORITY").toString()), lotData.getProcessFlowName(),
					lotData.getProcessFlowVersion(), (String) chamberSamplePolicy.get("RETURNOPERATIONNAME"), (String) chamberSamplePolicy.get("RETURNOPERATIONVER"), "");
		}
		else
		{
			actualSamplePosition = sampleLot.get(0).getActualSamplePosition();

			if (actualSamplePositionList.size() > 0)
			{
				actualSamplePosition = actualSamplePosition.concat(", " + CommonUtil.toStringWithoutBrackets(actualSamplePositionList));
			}

			int actualProductCount = Integer.valueOf(String.valueOf(actualSamplePositionList.size())) + Integer.valueOf(sampleLot.get(0).getActualProductCount());

			ExtendedObjectProxy.getSampleLotService().updateSampleLot(eventInfo, lotData.getKey().getLotName(), sampleLot.get(0).getFactoryName(), sampleLot.get(0).getProductSpecName(),
					sampleLot.get(0).getProductSpecVersion(), sampleLot.get(0).getProcessFlowName(), sampleLot.get(0).getProcessFlowVersion(), sampleLot.get(0).getProcessOperationName(),
					sampleLot.get(0).getProcessOperationVersion(), machineData.getKey().getMachineName(), sampleLot.get(0).getToProcessFlowName(), sampleLot.get(0).getToProcessFlowVersion(),
					sampleLot.get(0).getToProcessOperationName(), sampleLot.get(0).getToProcessOperationVersion(), sampleLot.get(0).getLotSampleFlag(), sampleLot.get(0).getLotSampleCount(),
					sampleLot.get(0).getCurrentLotCount(), sampleLot.get(0).getTotalLotCount(), sampleLot.get(0).getProductSampleCount(), sampleLot.get(0).getProductSamplePosition(),
					String.valueOf(actualProductCount), actualSamplePosition, sampleLot.get(0).getManualSampleFlag(), "", sampleLot.get(0).getPriority().toString(),
					sampleLot.get(0).getReturnProcessFlowName(), sampleLot.get(0).getReturnProcessFlowVersion(), sampleLot.get(0).getReturnOperationName(),
					sampleLot.get(0).getReturnOperationVersion(), "");
		}

		// Refresh SampleLotData
		sampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListWithOutMachineName(lotData.getKey().getLotName(), factoryName, productSpecName, productSpecVersion, processFlowName,
				processFlowVersion, processOperationName, processOperationVersion, (String) chamberSamplePolicy.get("TOPROCESSFLOWNAME"), (String) chamberSamplePolicy.get("TOPROCESSFLOWVERSION"),
				(String) chamberSamplePolicy.get("TOPROCESSOPERATIONNAME"), (String) chamberSamplePolicy.get("TOPROCESSOPERATIONVERSION"));

		return sampleLot;
	}

	public Lot createNewLotforOLEDtoTPShopChange(EventInfo eventInfo, Lot srcLotData, String carrierName, Map<String, String> assignCarrierUdfs, int lotCountByProductList, List<Element> productList)
			throws CustomException
	{
		String newLotName = "";
		String namingRuleName = "";

		ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(srcLotData.getFactoryName(), srcLotData.getProductSpecName(),
				GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);

		if (StringUtils.equals(productSpecData.getProductionType(), "P"))
			namingRuleName = "ProductionLotNaming";
		else
			namingRuleName = "LotNaming";

		if (StringUtils.isEmpty(productSpecData.getUdfs().get("PRODUCTCODE")))
			throw new CustomException("NAMING-0002", productSpecData.getKey().getProductSpecName());
		
		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("PRODUCTCODE", productSpecData.getUdfs().get("PRODUCTCODE"));
		nameRuleAttrMap.put("PRODUCTIONTYPE", srcLotData.getProductionType());
		
		// Request by caixu
		nameRuleAttrMap.put("PRODUCTSPECTYPE", "F");

		try
		{
			List<String> lstName = CommonUtil.generateNameByNamingRule(namingRuleName, nameRuleAttrMap, 1);
			newLotName = lstName.get(0);
		}
		catch (Exception ex)
		{
			new CustomException("LOT-9011", ex.getMessage());
		}

		// when Q-Glass Cutting TrackOut
		double subProductUnitQuantity1 = MESLotServiceProxy.getLotServiceUtil().convertSubProductQuantity(srcLotData, productSpecData, productList, lotCountByProductList, productList.size());

		List<ProductP> productPSequence = new ArrayList<ProductP>();
		CreateWithParentLotInfo createWithParentLotInfo = MESLotServiceProxy.getLotInfoUtil().createWithParentLotInfo(srcLotData.getAreaName(), "Y", assignCarrierUdfs, carrierName,
				srcLotData.getDueDate(), srcLotData.getFactoryName(), srcLotData.getLastLoggedInTime(), srcLotData.getLastLoggedInUser(), srcLotData.getLastLoggedOutTime(),
				srcLotData.getLastLoggedOutUser(), srcLotData.getLotGrade(), srcLotData.getLotHoldState(), newLotName, srcLotData.getLotProcessState(), srcLotData.getLotState(),
				srcLotData.getMachineName(), srcLotData.getMachineRecipeName(), srcLotData.getNodeStack(), srcLotData.getOriginalLotName(), srcLotData.getPriority(), srcLotData.getProcessFlowName(),
				srcLotData.getProcessFlowVersion(), srcLotData.getProcessGroupName(), srcLotData.getProcessOperationName(), srcLotData.getProcessOperationVersion(), srcLotData.getProductionType(),
				productPSequence, 0, srcLotData.getProductRequestName(), srcLotData.getProductSpec2Name(), srcLotData.getProductSpec2Version(), srcLotData.getProductSpecName(),
				srcLotData.getProductSpecVersion(), srcLotData.getProductType(), srcLotData.getReworkCount(),
				srcLotData.getReworkState().equals(GenericServiceProxy.getConstantMap().Lot_InRework) ? "Y" : "N", srcLotData.getReworkNodeId(), srcLotData.getRootLotName(),
				// baseLotData.getSourceLotName(), baseLotData.getSubProductType(),
				srcLotData.getKey().getLotName(), srcLotData.getSubProductType(), subProductUnitQuantity1, srcLotData.getSubProductUnitQuantity2(), srcLotData);

		// Set ReturnInfo
		createWithParentLotInfo.getUdfs().put("RETURNFLOWNAME", srcLotData.getUdfs().get("RETURNFLOWNAME"));
		createWithParentLotInfo.getUdfs().put("RETURNOPERATIONNAME", srcLotData.getUdfs().get("RETURNOPERATIONNAME"));
		createWithParentLotInfo.getUdfs().put("RETURNOPERATIONVER", srcLotData.getUdfs().get("RETURNOPERATIONVERSION"));
		createWithParentLotInfo.getUdfs().put("ARRAYLOTNAME", srcLotData.getUdfs().get("ARRAYLOTNAME"));
		
		eventInfo = EventInfoUtil.makeEventInfo("Create", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);

		Lot newLotData = MESLotServiceProxy.getLotServiceImpl().createWithParentLot(eventInfo, newLotName, createWithParentLotInfo);

		return newLotData;
	}

	public List<Map<String, Object>> getSortJobDatabyLotName(String lotName) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT J.JOBNAME, J.JOBSTATE, C.TRANSFERDIRECTION, C.CARRIERNAME, C.LOTNAME  ");
		sql.append("  FROM CT_SORTJOB J, CT_SORTJOBCARRIER C, DURABLE D  ");
		sql.append(" WHERE J.JOBNAME = C.JOBNAME ");
		sql.append("   AND C.CARRIERNAME = D.DURABLENAME ");
		sql.append("   AND J.JOBNAME IN (SELECT J.JOBNAME  ");
		sql.append("                       FROM CT_SORTJOB J, CT_SORTJOBCARRIER C  ");
		sql.append("                      WHERE J.JOBNAME = C.JOBNAME  ");
		sql.append("                        AND C.LOTNAME = :LOTNAME   ");
		sql.append("                        AND J.JOBSTATE = 'STARTED'  ");
		sql.append("                        AND J.JOBTYPE = 'Merge (OLED to TP)'   ");
		sql.append("                        AND ROWNUM = 1)  ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("LOTNAME", lotName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		return sqlResult;
	}

	public Lot exceutePostShipOfReviewStation(EventInfo eventInfo, Lot preLotData, Lot postLotData) throws CustomException
	{
		// Only have Repair ReviewStation flow will skip
		// select rule from enum
		ProcessOperationSpec processOperationData = CommonUtil.getProcessOperationSpec(postLotData.getFactoryName(), postLotData.getProcessOperationName(), postLotData.getProcessOperationVersion());
		// current operation is ReviewStation and next operation is Repair
		if (processOperationData.getDetailProcessOperationType().equalsIgnoreCase("VIEW"))
		{
			int iRuleCount = Integer.parseInt(CommonUtil.getEnumDefValueStringByEnumName("ArrayReviewStationSampleSkipCount"));
			List<Map<String, Object>> skipFlows = CommonUtil.getEnumDefValueByEnumName("ArrayReviewStationSampleSkipFlow");
			if (skipFlows.size() > 0)
			{
				for (Map<String, Object> skipFlow : skipFlows)
				{
					if (ConvertUtil.getMapValueByName(skipFlow, "ENUMVALUE").equalsIgnoreCase(postLotData.getProcessFlowName()))
					{
						StringBuilder sSelectSQL = new StringBuilder();
						sSelectSQL.append("SELECT REVIEWLOTSAMPLECOUNT ");
						sSelectSQL.append("  FROM CT_REVIEWSAMPLEACTION  ");
						sSelectSQL.append(" WHERE FACTORYNAME = :FACTORYNAME  ");
						sSelectSQL.append("  AND PRODUCTSPECNAME = :PRODUCTSPECNAME  ");
						sSelectSQL.append("  AND PROCESSFLOWNAME = :PROCESSFLOWNAME  ");
						sSelectSQL.append("  AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME  ");

						Map<String, Object> args = new HashMap<String, Object>();
						args.put("FACTORYNAME", postLotData.getFactoryName());
						args.put("PRODUCTSPECNAME", postLotData.getProductSpecName());
						args.put("PROCESSFLOWNAME", postLotData.getProcessFlowName());
						args.put("PROCESSOPERATIONNAME", postLotData.getProcessOperationName());

						@SuppressWarnings("unchecked")
						List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sSelectSQL.toString(), args);

						if (result.size() > 0)
						{
							int intLotCount = Integer.parseInt(ConvertUtil.getMapValueByName(result.get(0), "REVIEWLOTSAMPLECOUNT"));

							if (intLotCount >= iRuleCount)// not skip and set count = 1
							{
								args.put("REVIEWLOTSAMPLECOUNT", 1);
								StringBuilder updateSQL = new StringBuilder();
								updateSQL.append(" UPDATE CT_REVIEWSAMPLEACTION  ");
								updateSQL.append(" SET REVIEWLOTSAMPLECOUNT = :REVIEWLOTSAMPLECOUNT ");
								updateSQL.append(" WHERE FACTORYNAME = :FACTORYNAME  ");
								updateSQL.append("  AND PRODUCTSPECNAME = :PRODUCTSPECNAME  ");
								updateSQL.append("  AND PROCESSFLOWNAME = :PROCESSFLOWNAME  ");
								updateSQL.append("  AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME  ");
								GenericServiceProxy.getSqlMesTemplate().update(updateSQL.toString(), args);

							}
							else
							// skip
							{
								args.put("REVIEWLOTSAMPLECOUNT", intLotCount + 1);
								StringBuilder updateSQL = new StringBuilder();
								updateSQL.append(" UPDATE CT_REVIEWSAMPLEACTION  ");
								updateSQL.append(" SET REVIEWLOTSAMPLECOUNT = :REVIEWLOTSAMPLECOUNT ");
								updateSQL.append(" WHERE FACTORYNAME = :FACTORYNAME  ");
								updateSQL.append("  AND PRODUCTSPECNAME = :PRODUCTSPECNAME  ");
								updateSQL.append("  AND PROCESSFLOWNAME = :PROCESSFLOWNAME  ");
								updateSQL.append("  AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME  ");
								GenericServiceProxy.getSqlMesTemplate().update(updateSQL.toString(), args);

								// Ship action
								postLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(postLotData.getKey().getLotName());
								preLotData = (Lot) ObjectUtil.copyTo(postLotData);

								if (StringUtils.isNotEmpty(postLotData.getUdfs().get("FIRSTGLASSFLAG")) && StringUtils.isNotEmpty(postLotData.getUdfs().get("JOBNAME")))
								{
									log.info("Pass the SkipAction - FirstGlass");
								}
								else
								{
									if (StringUtils.equals(postLotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_NotOnHold))
									{
										Map<String, String> udfs = new HashMap<String, String>();
										String eventComment = "ReviewStation Sample Skip";
										eventInfo.setEventComment(eventComment);
										eventInfo.setEventName("Skip");
										ChangeSpecInfo skipInfo = MESLotServiceProxy.getLotInfoUtil().skipInfo(eventInfo, postLotData, udfs, new ArrayList<ProductU>());
										postLotData = MESLotServiceProxy.getLotServiceUtil().skip(eventInfo, postLotData, skipInfo, false);
									}
									else
									{
										log.info("Pass the SkipAction - LotHoldState is Y");
									}
								}
							}
						}
						else
						// not skip and insert first
						{
							args.put("REVIEWLOTSAMPLECOUNT", 1);
							StringBuilder insertSQL = new StringBuilder();
							insertSQL.append(" INSERT INTO CT_REVIEWSAMPLEACTION  ");
							insertSQL.append(" ( FACTORYNAME, PRODUCTSPECNAME, PROCESSFLOWNAME, PROCESSOPERATIONNAME, REVIEWLOTSAMPLECOUNT) ");
							insertSQL.append(" VALUES ( :FACTORYNAME, :PRODUCTSPECNAME, :PROCESSFLOWNAME, :PROCESSOPERATIONNAME, :REVIEWLOTSAMPLECOUNT) ");
							GenericServiceProxy.getSqlMesTemplate().update(insertSQL.toString(), args);
						}
					}
				}
			}
		}

		postLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(postLotData.getKey().getLotName());

		return postLotData;
	}

	public void deleteBeforeOperationSamplingData(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		log.info("Start delete before operation sampling data all ***************************");
		
		List<SampleLot> sampleLotList = ExtendedObjectProxy.getSampleLotService().getSampleLotDataList(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
				lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());

		if (sampleLotList != null)
		{
			for (SampleLot sampleLot : sampleLotList)
			{
				log.info("Sample Lot Info");
				log.info("LotName - " + sampleLot.getLotName() + ", ProcessFlowName - " + sampleLot.getProcessFlowName() + ", ProcessOperationName - " + sampleLot.getProcessOperationName() + 
						", ToProcessFlowName - " + sampleLot.getToProcessFlowName() + ", ToProcessOperationName - " + sampleLot.getToProcessOperationName());

				List<SampleProduct> sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListByLotName(lotData.getKey().getLotName(), lotData.getFactoryName(),
						lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), sampleLot.getProcessOperationName(),
						sampleLot.getProcessOperationVersion(), sampleLot.getMachineName(), sampleLot.getToProcessFlowName(), sampleLot.getToProcessFlowVersion(),
						sampleLot.getToProcessOperationName(), sampleLot.getToProcessOperationVersion());

				if (sampleProductList != null)
				{
					log.info("Sample Lot List Count - " + String.valueOf(sampleProductList.size()));
					for (SampleProduct sampleProduct : sampleProductList)
					{
						try
						{
							ExtendedObjectProxy.getSampleProductService().deleteSampleProductByToInfo(eventInfo, sampleProduct.getProductName(), lotData.getKey().getLotName(),
									lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), sampleProduct.getToProcessFlowName(),
									sampleProduct.getToProcessFlowVersion(), sampleProduct.getToProcessOperationName(), sampleProduct.getToProcessOperationVersion());
							
							log.info("Delete sample Product (" + sampleProduct.getProductName() + ")");
						}
						catch (Exception e)
						{
							log.info("SampleProduct Delete Fail");
							e.printStackTrace();
						}
					}
				}

				try
				{
					ExtendedObjectProxy.getSampleLotService().deleteSampleLotDataByToInfo(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
							lotData.getProductSpecVersion(), sampleLot.getToProcessFlowName(), sampleLot.getToProcessFlowVersion(), sampleLot.getToProcessOperationName(), sampleLot.getToProcessOperationVersion());
					
					log.info("Delete sample Lot (" + sampleLot.getLotName() + ")");
				}
				catch (Exception e)
				{
					log.info("SampleLot Delete Fail (TrackOut)");
					e.printStackTrace();
				}
			}
		}
		
		log.info("End delete before operation sampling data all ***************************");
	}

	public String doubleRunOperationCheck(Product productData, Lot lotData, String samplingFlag) throws CustomException
	{
		log.info("Double Run Operation Check Start");
		log.info("ProductName : " + productData.getKey().getProductName());

		String lotOperation = lotData.getProcessOperationName();

		String productLastMainOperation = productData.getUdfs().get("LASTMAINOPERNAME").toString();

		List<Map<String, Object>> sqlResult = getMainOperationList(lotData);

		if (sqlResult != null & sqlResult.size() > 0)
		{
			int lotMainOperationIndex = -1;
			int productMainOperationIndex = -1;

			for (int i = 0; i < sqlResult.size(); i++)
			{
				if (sqlResult.get(i).get("PROCESSOPERATIONNAME").equals(lotOperation))
				{
					lotMainOperationIndex = i;

					log.info("Lot OperationName : " + sqlResult.get(lotMainOperationIndex).get("PROCESSOPERATIONNAME").toString());
					log.info("Lot Operation Index : " + lotMainOperationIndex);
				}

				if (sqlResult.get(i).get("PROCESSOPERATIONNAME").equals(productLastMainOperation))
				{
					productMainOperationIndex = i;

					log.info("Product LastOperationName : " + productLastMainOperation);
					log.info("Product LastOperation Index : " + productMainOperationIndex);
				}

				if (lotMainOperationIndex != -1 && productMainOperationIndex != -1)
				{
					break;
				}
			}

			if (productMainOperationIndex >= lotMainOperationIndex && !productData.getUdfs().get("PROCESSINGINFO").equals("B"))
			{
				samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
				log.info("Change SampleFlag - " + productData.getKey().getProductName() + "  SampleFlag - " + samplingFlag);
			}
		}

		log.info("Double Run Operation Check End");

		return samplingFlag;
	}

	public List<Map<String, Object>> getMainOperationList(Lot lotData) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT DISTINCT ");
		sql.append("       QL.LV, ");
		sql.append("       QL.PROCESSOPERATIONNAME, ");
		sql.append("       DECODE (TP.DESCRIPTION, NULL, PO.DESCRIPTION, TP.DESCRIPTION) AS DESCRIPTION, ");
		sql.append("       PO.DETAILPROCESSOPERATIONTYPE, ");
		sql.append("       PO.PROCESSOPERATIONVERSION, ");
		sql.append("       QL.PROCESSFLOWNAME, ");
		sql.append("       QL.PROCESSFLOWVERSION, ");
		sql.append("       TP.PRODUCTSPECNAME, ");
		sql.append("       TP.PRODUCTSPECVERSION, ");
		sql.append("       PO.PROCESSOPERATIONTYPE ");
		sql.append("  FROM PROCESSOPERATIONSPEC PO, ");
		sql.append("       (SELECT LEVEL LV, ");
		sql.append("               N.FACTORYNAME, ");
		sql.append("               N.NODEATTRIBUTE1 PROCESSOPERATIONNAME, ");
		sql.append("               N.NODEATTRIBUTE2 PROCESSOPERATIONVERSION, ");
		sql.append("               N.PROCESSFLOWNAME, ");
		sql.append("               N.PROCESSFLOWVERSION, ");
		sql.append("               N.NODEID ");
		sql.append("          FROM ARC A, NODE N, PROCESSFLOW PF ");
		sql.append("         WHERE 1 = 1 ");
		sql.append("           AND N.NODETYPE = 'ProcessOperation' ");
		sql.append("           AND PF.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("           AND N.FACTORYNAME = :FACTORYNAME ");
		sql.append("           AND N.PROCESSFLOWNAME = PF.PROCESSFLOWNAME ");
		sql.append("           AND N.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION ");
		sql.append("           AND N.PROCESSFLOWNAME = A.PROCESSFLOWNAME ");
		sql.append("           AND N.FACTORYNAME = PF.FACTORYNAME ");
		sql.append("           AND A.FROMNODEID = N.NODEID ");
		sql.append("        START WITH N.NODETYPE = 'Start' ");
		sql.append("        CONNECT BY NOCYCLE A.FROMNODEID = PRIOR A.TONODEID ");
		sql.append("               AND A.FACTORYNAME = :FACTORYNAME) QL, ");
		sql.append("       (SELECT T.FACTORYNAME, ");
		sql.append("               T.PRODUCTSPECNAME, ");
		sql.append("               T.PRODUCTSPECVERSION, ");
		sql.append("               T.PROCESSFLOWNAME, ");
		sql.append("               T.PROCESSFLOWVERSION, ");
		sql.append("               T.PROCESSOPERATIONNAME, ");
		sql.append("               T.PROCESSOPERATIONVERSION, ");
		sql.append("               T.DESCRIPTION, ");
		sql.append("               P.MACHINENAME, ");
		sql.append("               P.MACHINERECIPENAME ");
		sql.append("          FROM TPFOPOLICY T, POSMACHINE P ");
		sql.append("         WHERE T.CONDITIONID = P.CONDITIONID ");
		sql.append("           AND T.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("           AND T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");
		sql.append("           AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("           AND T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("        UNION ALL ");
		sql.append("        SELECT T.FACTORYNAME, ");
		sql.append("               T.MASKSPECNAME AS PRODUCTSPECNAME, ");
		sql.append("               NULL AS PRODUCTSPECVERSION, ");
		sql.append("               T.PROCESSFLOWNAME, ");
		sql.append("               T.PROCESSFLOWVERSION, ");
		sql.append("               T.PROCESSOPERATIONNAME, ");
		sql.append("               T.PROCESSOPERATIONVERSION, ");
		sql.append("               NULL AS DESCRIPTION, ");
		sql.append("               P.MACHINENAME, ");
		sql.append("               P.MACHINERECIPENAME ");
		sql.append("          FROM TRFOPOLICY T, POSMACHINE P ");
		sql.append("         WHERE T.CONDITIONID = P.CONDITIONID ");
		sql.append("           AND T.MASKSPECNAME = :PRODUCTSPECNAME ");
		sql.append("           AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("           AND T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION) TP ");
		sql.append(" WHERE 1 = 1 ");
		sql.append("   AND PO.PROCESSOPERATIONNAME = QL.PROCESSOPERATIONNAME ");
		sql.append("   AND PO.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND QL.PROCESSFLOWNAME = TP.PROCESSFLOWNAME(+) ");
		sql.append("   AND QL.PROCESSFLOWVERSION = TP.PROCESSFLOWVERSION(+) ");
		sql.append("   AND QL.PROCESSOPERATIONNAME = TP.PROCESSOPERATIONNAME(+) ");
		sql.append("   AND QL.FACTORYNAME = TP.FACTORYNAME(+) ");
		sql.append("ORDER BY LV ");

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
		bindMap.put("PROCESSFLOWVERSION", "00001");
		bindMap.put("FACTORYNAME", lotData.getFactoryName());
		bindMap.put("PRODUCTSPECNAME", lotData.getProductSpecName());
		bindMap.put("PRODUCTSPECVERSION", lotData.getProductSpecVersion());

		// GetMainOperationList
		List<Map<String, Object>> sqlResult;
		try
		{
			sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
		}
		catch (Exception e)
		{
			throw new CustomException("LOT-0901");
		}

		return sqlResult;

	}

	public void setSamplingDataByFutureAction(EventInfo eventInfo, Lot lotData, String toProcessFlowName, String toProcessFlowVersion, String toProcessOperationName, String toProcessOperationVersion,
			String actionName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		eventInfo.setEventComment("SetSampleDataByFutureAction(" + actionName + ") : ReserveFlow - " + toProcessFlowName + ", ReserveOper - " + toProcessOperationName);

		String nodeStack = lotData.getNodeStack();
		String mainNodeStack = "";

		if (nodeStack.lastIndexOf(".") > -1)
		{
			String[] nodeStackArray = StringUtils.split(nodeStack, ".");
			mainNodeStack = nodeStackArray[0];
		}
		else
		{
			mainNodeStack = nodeStack;
		}

		Node mainNodeData = ProcessFlowServiceProxy.getNodeService().getNode(mainNodeStack);

		String mainFlowName = mainNodeData.getProcessFlowName();
		String mainFlowVersion = mainNodeData.getProcessFlowVersion();

		log.info("MainFlowName : " + mainFlowName);

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT T.FACTORYNAME,  ");
		sql.append("       T.PROCESSFLOWNAME,  ");
		sql.append("       T.PROCESSFLOWVERSION,  ");
		sql.append("       T.PROCESSOPERATIONNAME,  ");
		sql.append("       T.PROCESSOPERATIONVERSION,  ");
		sql.append("       P.TOPROCESSFLOWNAME,  ");
		sql.append("       P.TOPROCESSFLOWVERSION,  ");
		sql.append("       P.TOPROCESSOPERATIONNAME,  ");
		sql.append("       P.TOPROCESSOPERATIONVERSION,  ");
		sql.append("       P.FLOWPRIORITY,  ");
		sql.append("       P.LOTSAMPLINGCOUNT,  ");
		sql.append("       P.PRODUCTSAMPLINGCOUNT,  ");
		sql.append("       P.PRODUCTSAMPLINGPOSITION,  ");
		sql.append("       P.RETURNOPERATIONNAME,  ");
		sql.append("       P.RETURNOPERATIONVER  ");
		sql.append("  FROM TFOMPOLICY T, POSSAMPLE P  ");
		sql.append(" WHERE T.CONDITIONID = P.CONDITIONID  ");
		sql.append("   AND T.FACTORYNAME = :FACTORYNAME  ");
		sql.append("   AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME  ");
		sql.append("   AND T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION  ");
		sql.append("   AND P.TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME  ");
		sql.append("   AND P.TOPROCESSFLOWVERSION = :TOPROCESSFLOWVERSION  ");
		sql.append("   AND P.TOPROCESSOPERATIONNAME = :TOPROCESSOPERATIONNAME  ");
		sql.append("   AND P.TOPROCESSOPERATIONVERSION = :TOPROCESSOPERATIONVERSION  ");
		sql.append("ORDER BY T.MACHINENAME  ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("FACTORYNAME", lotData.getFactoryName());
		args.put("PROCESSFLOWNAME", mainFlowName);
		args.put("PROCESSFLOWVERSION", mainFlowVersion);
		args.put("TOPROCESSFLOWNAME", toProcessFlowName);
		args.put("TOPROCESSFLOWVERSION", toProcessFlowVersion);
		args.put("TOPROCESSOPERATIONNAME", toProcessOperationName);
		args.put("TOPROCESSOPERATIONVERSION", toProcessOperationVersion);

		List<Map<String, Object>> samplePolicy = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (samplePolicy.size() > 0)
		{
			List<SampleLot> sampleSrcLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataList(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
					lotData.getProductSpecVersion(), mainFlowName, mainFlowVersion, (String) samplePolicy.get(0).get("PROCESSOPERATIONNAME"),
					(String) samplePolicy.get(0).get("PROCESSOPERATIONVERSION"), "NA", toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);

			if (sampleSrcLot == null)
			{
				List<String> positionList = CommonUtil.getPositionList(lotData);
				List<Product> prodList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotData.getKey().getLotName());

				String productSamplePositions = this.getProductSamplePositions(lotData, samplePolicy.get(0), positionList);

				List<String> productSamplePositionList = CommonUtil.splitString(",", productSamplePositions);
				List<String> exceptBackpPositionList = CommonUtil.copyToStringList(productSamplePositionList);
				List<String> actualSamplePositionList = new ArrayList<String>();

				// set SampleProduct(CT_SAMPLEPRODUCT)
				actualSamplePositionList = setProductSamplingDataByFutureAction(eventInfo, productSamplePositionList, exceptBackpPositionList, prodList, lotData, samplePolicy.get(0));

				// set SampleLotData(CT_SAMPLELOT)
				ExtendedObjectProxy.getSampleLotService().insertSampleLot(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
						lotData.getProductSpecVersion(), mainFlowName, mainFlowVersion, (String) samplePolicy.get(0).get("PROCESSOPERATIONNAME"),
						(String) samplePolicy.get(0).get("PROCESSOPERATIONVERSION"), "NA", (String) samplePolicy.get(0).get("TOPROCESSFLOWNAME"),
						(String) samplePolicy.get(0).get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get(0).get("TOPROCESSOPERATIONNAME"),
						(String) samplePolicy.get(0).get("TOPROCESSOPERATIONVERSION"), "Y", "", "", "", "", "", String.valueOf(actualSamplePositionList.size()),
						CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "Y", "", Integer.valueOf(samplePolicy.get(0).get("FLOWPRIORITY").toString()), mainFlowName,
						mainFlowVersion, (String) samplePolicy.get(0).get("RETURNOPERATIONNAME"), (String) samplePolicy.get(0).get("RETURNOPERATIONVER"), "");
			}
		}
	}

	public List<String> setProductSamplingDataByFutureAction(EventInfo eventInfo, List<String> productSamplePositionList, List<String> exceptBackpPositionList, List<Product> prodList, Lot lotData,
			Map<String, Object> samplePolicy) throws CustomException
	{
		List<String> actualSamplePositionList = new ArrayList<String>();
		List<String> notExistPositionList = new ArrayList<String>();

		for (String productSamplePosition : productSamplePositionList)
		{
			log.info("setSamplingData:productSamplePosition(" + productSamplePosition + ")");

			boolean existCheck = false;

			for (Product prod : prodList)
			{
				String position = Long.toString(prod.getPosition());

				if (StringUtils.equals(position, productSamplePosition))
				{
					String productName = prod.getKey().getProductName();
					Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

					// get SamplingProduct Data
					List<SampleProduct> sampleProductData = ExtendedObjectProxy.getSampleProductService().getManualSampleProductData(productName, productData.getLotName(),
							productData.getFactoryName(), productData.getProductSpecName(), productData.getProductSpecVersion(), (String) samplePolicy.get("PROCESSFLOWNAME"),
							(String) samplePolicy.get("PROCESSFLOWVERSION"), (String) samplePolicy.get("PROCESSOPERATIONNAME"), (String) samplePolicy.get("PROCESSOPERATIONVERSION"), "NA",
							(String) samplePolicy.get("TOPROCESSFLOWNAME"), (String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"),
							(String) samplePolicy.get("TOPROCESSOPERATIONVERSION"), "Y");

					if (sampleProductData == null) // Exist the Product for productSamplePosition
					{
						// set SamplingProduct Data(CT_SAMPLEPRODUCT)
						ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, productName, lotData.getKey().getLotName(), lotData.getFactoryName(),
								lotData.getProductSpecName(), lotData.getProductSpecVersion(), (String) samplePolicy.get("PROCESSFLOWNAME"), (String) samplePolicy.get("PROCESSFLOWVERSION"),
								(String) samplePolicy.get("PROCESSOPERATIONNAME"), (String) samplePolicy.get("PROCESSOPERATIONVERSION"), "NA", (String) samplePolicy.get("TOPROCESSFLOWNAME"),
								(String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"), (String) samplePolicy.get("TOPROCESSOPERATIONVERSION"), "Y",
								(String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"), (String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"), productSamplePosition, "", "Y");

						actualSamplePositionList.add(productSamplePosition);

						existCheck = true;
						break;
					}
				}
			}

			if (!existCheck)
			{
				notExistPositionList.add(productSamplePosition);
			}
		}

		if (!notExistPositionList.isEmpty())
		{
			List<Product> backupProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());

			int j = 0;
			int i = 0;

			for (; i < backupProductList.size(); i++)
			{
				Product backupProduct = backupProductList.get(i);

				for (Product prod : prodList)
				{
					String productName = prod.getKey().getProductName();

					if (StringUtils.equals(backupProduct.getKey().getProductName(), productName))
					{
						String position = Long.toString(prod.getPosition());
						long notExistPosition = 0;

						try
						{
							notExistPosition = Long.parseLong(notExistPositionList.get(j));
						}
						catch (Exception e)
						{
							break;
						}

						List<SampleProduct> sampleProductData = ExtendedObjectProxy.getSampleProductService().getManualSampleProductData(productName, backupProduct.getKey().getProductName(),
								lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), (String) samplePolicy.get("PROCESSFLOWNAME"),
								(String) samplePolicy.get("PROCESSFLOWVERSION"), (String) samplePolicy.get("PROCESSOPERATIONNAME"), (String) samplePolicy.get("PROCESSOPERATIONVERSION"), "NA",
								(String) samplePolicy.get("TOPROCESSFLOWNAME"), (String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"),
								(String) samplePolicy.get("TOPROCESSOPERATIONVERSION"), "Y");

						if (Long.valueOf(position) > notExistPosition && !exceptBackpPositionList.contains(position) && sampleProductData == null)
						{
							ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, backupProduct.getKey().getProductName(), lotData.getKey().getLotName(),
									lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), (String) samplePolicy.get("PROCESSFLOWNAME"),
									(String) samplePolicy.get("PROCESSFLOWVERSION"), (String) samplePolicy.get("PROCESSOPERATIONNAME"), (String) samplePolicy.get("PROCESSOPERATIONVERSION"), "NA",
									(String) samplePolicy.get("TOPROCESSFLOWNAME"), (String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"),
									(String) samplePolicy.get("TOPROCESSOPERATIONVERSION"), "Y", (String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"),
									(String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"), position, "", "Y");

							// except for new Product(backupProduct)
							exceptBackpPositionList.add(position);
							actualSamplePositionList.add(position);
							j++;
						}

						if (j == notExistPositionList.size())
						{
							break;
						}

						break;
					}
				}
			}

			if (j != notExistPositionList.size())
			{
				for (i--; i >= 0; i--)
				{
					Product backupProduct = backupProductList.get(i);

					for (Product prod : prodList)
					{
						String productName = prod.getKey().getProductName();

						if (StringUtils.equals(backupProduct.getKey().getProductName(), productName))
						{
							String position = Long.toString(prod.getPosition());
							long notExistPosition = 0;

							try
							{
								notExistPosition = Long.parseLong(notExistPositionList.get(j));
							}
							catch (Exception e)
							{
								break;
							}

							List<SampleProduct> sampleProductData = ExtendedObjectProxy.getSampleProductService().getManualSampleProductData(productName, backupProduct.getKey().getProductName(),
									lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), (String) samplePolicy.get("PROCESSFLOWNAME"),
									(String) samplePolicy.get("PROCESSFLOWVERSION"), (String) samplePolicy.get("PROCESSOPERATIONNAME"), (String) samplePolicy.get("PROCESSOPERATIONVERSION"), "NA",
									(String) samplePolicy.get("TOPROCESSFLOWNAME"), (String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"),
									(String) samplePolicy.get("TOPROCESSOPERATIONVERSION"), "Y");

							if (Long.valueOf(position) < notExistPosition && !exceptBackpPositionList.contains(position) && sampleProductData == null)
							{
								ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, backupProduct.getKey().getProductName(), lotData.getKey().getLotName(),
										lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), (String) samplePolicy.get("PROCESSFLOWNAME"),
										(String) samplePolicy.get("PROCESSFLOWVERSION"), (String) samplePolicy.get("PROCESSOPERATIONNAME"), (String) samplePolicy.get("PROCESSOPERATIONVERSION"), "NA",
										(String) samplePolicy.get("TOPROCESSFLOWNAME"), (String) samplePolicy.get("TOPROCESSFLOWVERSION"), (String) samplePolicy.get("TOPROCESSOPERATIONNAME"),
										(String) samplePolicy.get("TOPROCESSOPERATIONVERSION"), "Y", (String) samplePolicy.get("PRODUCTSAMPLINGCOUNT"),
										(String) samplePolicy.get("PRODUCTSAMPLINGPOSITION"), position, "", "Y");

								// except for new Product(backupProduct)
								exceptBackpPositionList.add(position);
								actualSamplePositionList.add(position);
								j++;
							}

							if (j == notExistPositionList.size())
							{
								break;
							}

							break;
						}
					}
				}
			}

			notExistPositionList.clear();
		}

		return actualSamplePositionList;
	}

	public void checkSampleDataByCancelReserveHold(EventInfo eventInfo, Lot lotData, String toProcessFlowName, String toProcessFlowVersion, String toProcessOperationName,
			String toProcessOperationVersion) throws CustomException
	{
		List<SampleLot> sampleLotList = getSampleLotListByCancelFutureAction(lotData, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);

		if (sampleLotList != null)
		{
			for (SampleLot sampleLot : sampleLotList)
			{
				String lotName = sampleLot.getLotName();
				String factoryName = sampleLot.getFactoryName();
				String productSpecName = sampleLot.getProductSpecName();
				String productSpecVersion = sampleLot.getProductSpecVersion();
				String processFlowName = sampleLot.getProcessFlowName();
				String processFlowVersion = sampleLot.getProcessFlowVersion();
				String processOperationName = sampleLot.getProcessOperationName();
				String processOperationVersion = sampleLot.getProcessOperationVersion();
				String machineName = sampleLot.getMachineName();
				String returnProcessFlowName = sampleLot.getReturnProcessFlowName();
				String returnProcessFlowVersion = sampleLot.getReturnProcessFlowVersion();
				String returnProcessOperationName = sampleLot.getReturnOperationName();
				String returnProcessOperationVersion = sampleLot.getReturnOperationVersion();

				log.info("SampleLotName : " + lotName);
				List<SampleProduct> sampleProdList = ExtendedObjectProxy.getSampleProductService().getSampleProductListByComment(sampleLot);

				for (SampleProduct sampleProd : sampleProdList)
				{
					String productName = sampleProd.getProductName();

					log.info("SampleProductName : " + productName);
					List<SampleProduct> sampleProductData = ExtendedObjectProxy.getSampleProductService().getSampleProductDataList(productName, lotName, factoryName, productSpecName,
							productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion,
							toProcessOperationName, toProcessOperationVersion);

					if (sampleProductData != null)
					{
						log.info("Delete SampleProduct : " + productName);
						ExtendedObjectProxy.getSampleProductService().deleteSampleProduct(eventInfo, productName, lotName, factoryName, productSpecName, productSpecVersion, returnProcessFlowName,
								returnProcessFlowVersion, returnProcessOperationName, returnProcessOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName,
								toProcessOperationVersion);
					}
				}

				log.info("Delete SampleLot : " + lotName);
				ExtendedObjectProxy.getSampleLotService().deleteSampleLotWithOutMachineName(eventInfo, lotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion,
						processOperationName, processOperationVersion, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);
			}
		}
	}

	public List<SampleLot> getSampleLotListByCancelFutureAction(Lot lotData, String toProcessFlowName, String toProcessFlowVersion, String toProcessOperationName, String toProcessOperationVersion)
			throws greenFrameDBErrorSignal, CustomException
	{
		String nodeStack = lotData.getNodeStack();
		String mainNodeStack = "";

		if (nodeStack.lastIndexOf(".") > -1)
		{
			String[] nodeStackArray = StringUtils.split(nodeStack, ".");
			mainNodeStack = nodeStackArray[0];
		}
		else
		{
			mainNodeStack = nodeStack;
		}

		Node mainNodeData = ProcessFlowServiceProxy.getNodeService().getNode(mainNodeStack);

		String mainFlowName = mainNodeData.getProcessFlowName();
		String mainFlowVersion = mainNodeData.getProcessFlowVersion();

		List<SampleLot> sampleLotList = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListByComment(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
				lotData.getProductSpecVersion(), mainFlowName, mainFlowVersion, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion,
				"SetSampleDataByFutureAction%");

		return sampleLotList;
	}

	public Document generateOPCallSend(Document doc, String machineName, String machineRecipeName, String photoRecipeName, String carrierName, String lotName) throws CustomException
	{
		try
		{
			Element bodyElement = new Element(SMessageUtil.Body_Tag);
			{
				Element attMachineName = new Element("MACHINENAME");
				attMachineName.setText(machineName);
				bodyElement.addContent(attMachineName);

				Element attMachineRecipeName = new Element("MACHINERECIPENAME");
				attMachineRecipeName.setText(machineRecipeName);
				bodyElement.addContent(attMachineRecipeName);

				Element attPhotoRecipeName = new Element("PHOTORECIPENAME");
				attPhotoRecipeName.setText(photoRecipeName);
				bodyElement.addContent(attPhotoRecipeName);

				Element attCarrierName = new Element("CARRIERNAME");
				attCarrierName.setText(carrierName);
				bodyElement.addContent(attCarrierName);
			}

			Document newdoc = SMessageUtil.createXmlDocument(bodyElement, "OPCallByPhotoRecipe", "", "", "MES", "LotInfoDownloadRequest");

			newdoc = SMessageUtil.addReturnToMessage(newdoc, "0", "'" + carrierName + "'" + "," + "'" + lotName + "'" + "," + "NA=" + "'" + photoRecipeName + "'" + "," + "ACPD=" + "'"
					+ machineRecipeName + "'");

			return newdoc;
		}
		catch (Exception ex)
		{
			throw new CustomException("OP Call failed !!");
		}
	}

	public void mergeSampleData(EventInfo eventInfo, Lot srcLotData, Lot destLotData) throws CustomException
	{
		log.info("Merge SampleData Start");

		List<SampleLot> sampleSrcLotData = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListBySpec(srcLotData.getKey().getLotName(), srcLotData.getFactoryName(),
				srcLotData.getProductSpecName(), srcLotData.getProductSpecName());

		List<SampleLot> sampleDestLotData = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListBySpec(destLotData.getKey().getLotName(), destLotData.getFactoryName(),
				destLotData.getProductSpecName(), destLotData.getProductSpecName());

		if (sampleDestLotData != null)
		{
			sortSampleProdList(eventInfo, destLotData, sampleDestLotData);
		}

		if (sampleSrcLotData != null)
		{
			if (sampleDestLotData != null)
			{
				for (SampleLot srcSampleLot : sampleSrcLotData)
				{
					String factoryName = srcSampleLot.getFactoryName();
					String productSpecName = srcSampleLot.getProductSpecName();
					String productSpecVersion = srcSampleLot.getProductSpecVersion();
					String processFlowName = srcSampleLot.getProcessFlowName();
					String processFlowVersion = srcSampleLot.getProcessFlowVersion();
					String processOperationName = srcSampleLot.getProcessOperationName();
					String processOperationVersion = srcSampleLot.getProcessOperationVersion();
					String machineName = srcSampleLot.getMachineName();
					String toProcessFlowName = srcSampleLot.getToProcessFlowName();
					String toProcessFlowVersion = srcSampleLot.getToProcessFlowVersion();
					String toProcessOperationName = srcSampleLot.getToProcessOperationName();
					String toProcessOperationVersion = srcSampleLot.getToProcessOperationVersion();

					List<SampleLot> destSampleLotList = ExtendedObjectProxy.getSampleLotService().getSampleLotDataList(destLotData.getKey().getLotName(), factoryName, productSpecName,
							productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion,
							toProcessOperationName, toProcessOperationVersion);

					if (destSampleLotList != null)
					{
						for (SampleLot destSampleLot : destSampleLotList)
						{
							mergeSampleDataDestExistCase(eventInfo, srcLotData, destLotData, srcSampleLot, destSampleLot);
						}
					}
					else
					{
						mergeSampleDataDestNotExistCase(eventInfo, srcLotData, destLotData, sampleSrcLotData, sampleDestLotData);
					}
				}
			}
			else
			{
				mergeSampleDataDestNotExistCase(eventInfo, srcLotData, destLotData, sampleSrcLotData, sampleDestLotData);
			}
		}

		log.info("Merge SampleData End");
	}

	public void sortSampleProdList(EventInfo eventInfo, Lot destLotData, List<SampleLot> sampleDestLotData) throws CustomException
	{
		log.info("Start Sort DestSampleData");
		for (SampleLot destSampleLot : sampleDestLotData)
		{
			String factoryName = destSampleLot.getFactoryName();
			String productSpecName = destSampleLot.getProductSpecName();
			String productSpecVersion = destSampleLot.getProductSpecVersion();
			String processFlowName = destSampleLot.getProcessFlowName();
			String processFlowVersion = destSampleLot.getProcessFlowVersion();
			String processOperationName = destSampleLot.getProcessOperationName();
			String processOperationVersion = destSampleLot.getProcessOperationVersion();
			String machineName = destSampleLot.getMachineName();
			String toProcessFlowName = destSampleLot.getToProcessFlowName();
			String toProcessFlowVersion = destSampleLot.getToProcessFlowVersion();
			String toProcessOperationName = destSampleLot.getToProcessOperationName();
			String toProcessOperationVersion = destSampleLot.getToProcessOperationVersion();
			String returnProcessFlowName = destSampleLot.getReturnProcessFlowName();
			String returnProcessFlowVersion = destSampleLot.getReturnProcessFlowVersion();
			String returnProcessOperationName = destSampleLot.getReturnOperationName();
			String returnProcessOperationVersion = destSampleLot.getReturnOperationVersion();
			String lotSampleFlag = destSampleLot.getLotSampleFlag();
			String lotSampleCount = destSampleLot.getLotSampleCount();
			String currentLotCount = destSampleLot.getCurrentLotCount();
			String totalLotCount = destSampleLot.getTotalLotCount();
			String productSampleCount = destSampleLot.getProductSampleCount();
			String productSamplePosition = destSampleLot.getProductSamplePosition();
			String priority = destSampleLot.getPriority().toString();
			String manualSampleFlag = destSampleLot.getManualSampleFlag();

			List<String> actualSamplePositionList = new ArrayList<String>();
			boolean sampleFlag = false;

			List<SampleProduct> destSampleProdList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListByLotName(destLotData.getKey().getLotName(), factoryName, productSpecName,
					productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion,
					toProcessOperationName, toProcessOperationVersion);

			if (destSampleProdList != null)
			{
				for (SampleProduct destSampleProd : destSampleProdList)
				{
					String productName = destSampleProd.getProductName();
					Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

					String sampleProdPosition = destSampleProd.getActualSamplePosition();

					if (!StringUtils.equals(Long.toString(productData.getPosition()), sampleProdPosition))
					{
						log.info("Sort ProductName : " + productName + ", Position : " + productData.getPosition() + ", SampleProductPosition : " + sampleProdPosition);

						ExtendedObjectProxy.getSampleProductService().updateSampleProduct(eventInfo, productName, destLotData.getKey().getLotName(), factoryName, productSpecName, productSpecVersion,
								processFlowName, processFlowVersion, processOperationName, processOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName,
								toProcessOperationVersion, "", "", "", Long.toString(productData.getPosition()), "", "");

						sampleFlag = true;
						actualSamplePositionList.add(Long.toString(productData.getPosition()));
					}
				}
			}

			if (sampleFlag)
			{
				actualSamplePositionList = CommonUtil.sortActualSamplePosition(actualSamplePositionList);

				ExtendedObjectProxy.getSampleLotService().updateSampleLot(eventInfo, destLotData.getKey().getLotName(), factoryName, productSpecName, productSpecVersion, processFlowName,
						processFlowVersion, processOperationName, processOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion,
						lotSampleFlag, lotSampleCount, currentLotCount, totalLotCount, productSampleCount, productSamplePosition, String.valueOf(actualSamplePositionList.size()),
						CommonUtil.toStringWithoutBrackets(actualSamplePositionList), manualSampleFlag, "", priority, returnProcessFlowName, returnProcessFlowVersion, returnProcessOperationName,
						returnProcessOperationVersion, "");
			}
		}
	}

	public void mergeSampleDataDestNotExistCase(EventInfo eventInfo, Lot srcLotData, Lot destLotData, List<SampleLot> sampleSrcLotData, List<SampleLot> sampleDestLotData) throws CustomException
	{
		for (SampleLot srcSampleLot : sampleSrcLotData)
		{
			String factoryName = srcSampleLot.getFactoryName();
			String productSpecName = srcSampleLot.getProductSpecName();
			String productSpecVersion = srcSampleLot.getProductSpecVersion();
			String processFlowName = srcSampleLot.getProcessFlowName();
			String processFlowVersion = srcSampleLot.getProcessFlowVersion();
			String processOperationName = srcSampleLot.getProcessOperationName();
			String processOperationVersion = srcSampleLot.getProcessOperationVersion();
			String machineName = srcSampleLot.getMachineName();
			String toProcessFlowName = srcSampleLot.getToProcessFlowName();
			String toProcessFlowVersion = srcSampleLot.getToProcessFlowVersion();
			String toProcessOperationName = srcSampleLot.getToProcessOperationName();
			String toProcessOperationVersion = srcSampleLot.getToProcessOperationVersion();
			String returnProcessFlowName = srcSampleLot.getReturnProcessFlowName();
			String returnProcessFlowVersion = srcSampleLot.getReturnProcessFlowVersion();
			String returnProcessOperationName = srcSampleLot.getReturnOperationName();
			String returnProcessOperationVersion = srcSampleLot.getReturnOperationVersion();
			String lotSampleFlag = srcSampleLot.getLotSampleFlag();
			String lotSampleCount = srcSampleLot.getLotSampleCount();
			String currentLotCount = srcSampleLot.getCurrentLotCount();
			String totalLotCount = srcSampleLot.getTotalLotCount();
			String productSampleCount = srcSampleLot.getProductSampleCount();
			String productSamplePosition = srcSampleLot.getProductSamplePosition();
			String priority = srcSampleLot.getPriority().toString();
			String manualSampleFlag = srcSampleLot.getManualSampleFlag();
			String actualSamplePosition = srcSampleLot.getActualSamplePosition();

			List<SampleProduct> srcSampleProdList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListByLotName(srcLotData.getKey().getLotName(), factoryName, productSpecName,
					productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion,
					toProcessOperationName, toProcessOperationVersion);

			List<String> actualSamplePositionList = new ArrayList<String>();
			List<String> deleteSamplePositionList = new ArrayList<String>();
			boolean sampleFlag = false;

			if (srcSampleProdList != null)
			{
				for (SampleProduct srcSampleProd : srcSampleProdList)
				{
					String productName = srcSampleProd.getProductName();
					Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
					String actualSampleProdPosition = srcSampleProd.getActualSamplePosition();

					if (StringUtils.equals(productData.getLotName(), destLotData.getKey().getLotName()))
					{
						List<SampleProduct> sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListWithoutMachineName(productName, srcLotData.getKey().getLotName(),
								factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion, toProcessFlowName,
								toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);

						for (SampleProduct sampleProd : sampleProductList)
						{
							ExtendedObjectProxy.getSampleProductService().updateSampleProductKey(sampleProd, eventInfo, "", destLotData.getKey().getLotName(), "", "", "", "", "", "", "", "", "", "",
									"", "", "", "", "", actualSamplePosition, "", "");
						}

						actualSamplePositionList.add(Long.toString(productData.getPosition()));

						sampleFlag = true;

						if (!deleteSamplePositionList.contains(actualSampleProdPosition))
						{
							deleteSamplePositionList.add(actualSampleProdPosition);
						}
					}
				}
			}

			if (sampleFlag)
			{
				List<String> insertactualPositionList = CommonUtil.sortActualSamplePosition(actualSamplePositionList);

				List<SampleLot> destSampleLotList = ExtendedObjectProxy.getSampleLotService().getSampleLotDataList(destLotData.getKey().getLotName(), factoryName, productSpecName, productSpecVersion,
						processFlowName, processFlowVersion, processOperationName, processOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName,
						toProcessOperationVersion);

				if (destSampleLotList == null)
				{
					ExtendedObjectProxy.getSampleLotService().insertSampleLot(eventInfo, destLotData.getKey().getLotName(), factoryName, productSpecName, productSpecVersion, processFlowName,
							processFlowVersion, processOperationName, processOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion,
							lotSampleFlag, lotSampleCount, currentLotCount, totalLotCount, productSampleCount, productSamplePosition, String.valueOf(insertactualPositionList.size()),
							CommonUtil.toStringWithoutBrackets(insertactualPositionList), manualSampleFlag, "", Integer.parseInt(priority), returnProcessFlowName, returnProcessFlowVersion,
							returnProcessOperationName, returnProcessOperationVersion, "");

				}

				String actualProdCount = srcSampleLot.getActualProductCount();

				if (deleteSamplePositionList.size() == Integer.parseInt(actualProdCount))
				{
					log.info("SourceLot is Empty");
					ExtendedObjectProxy.getSampleLotService().deleteSampleLotWithOutMachineName(eventInfo, srcLotData.getKey().getLotName(), factoryName, productSpecName, productSpecVersion,
							processFlowName, processFlowVersion, processOperationName, processOperationVersion, toProcessFlowName, toProcessFlowVersion, toProcessOperationName,
							toProcessOperationVersion);
				}
				else
				{
					log.info("Update Source SampleLot");

					String srcActualPosition = srcSampleLot.getActualSamplePosition();

					List<String> afterActualPositionList = new ArrayList<String>();

					log.info("actualSamplePosition : " + srcActualPosition);
					if (srcActualPosition.lastIndexOf(",") > -1)
					{
						String[] actualPositionArray = StringUtils.split(srcActualPosition, ",");
						String oriSamplePosition = "";

						for (int i = 0; i < actualPositionArray.length; i++)
						{
							oriSamplePosition = actualPositionArray[i].trim();

							if (deleteSamplePositionList.size() > 0)
							{
								if (!deleteSamplePositionList.contains(oriSamplePosition))
								{
									afterActualPositionList.add(oriSamplePosition);
								}
							}
							else
							{
								afterActualPositionList.add(oriSamplePosition);
							}
						}
					}
					else
					{
						afterActualPositionList.add(srcActualPosition);
					}

					log.info("AfterActualPosition : " + afterActualPositionList);

					ExtendedObjectProxy.getSampleLotService().updateSampleLot(eventInfo, srcLotData.getKey().getLotName(), factoryName, productSpecName, productSpecVersion, processFlowName,
							processFlowVersion, processOperationName, processOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion,
							lotSampleFlag, lotSampleCount, currentLotCount, totalLotCount, productSampleCount, productSamplePosition, String.valueOf(afterActualPositionList.size()),
							CommonUtil.toStringWithoutBrackets(afterActualPositionList), manualSampleFlag, "", priority, returnProcessFlowName, returnProcessFlowVersion, returnProcessOperationName,
							returnProcessOperationVersion, "");
				}
			}
		}

	}

	public void mergeSampleDataDestExistCase(EventInfo eventInfo, Lot srcLotData, Lot destLotData, SampleLot sampleSrcLotData, SampleLot sampleDestLotData) throws CustomException
	{
		log.info("Dest SampleData Exist");

		String factoryName = sampleSrcLotData.getFactoryName();
		String productSpecName = sampleSrcLotData.getProductSpecName();
		String productSpecVersion = sampleSrcLotData.getProductSpecVersion();
		String processFlowName = sampleSrcLotData.getProcessFlowName();
		String processFlowVersion = sampleSrcLotData.getProcessFlowVersion();
		String processOperationName = sampleSrcLotData.getProcessOperationName();
		String processOperationVersion = sampleSrcLotData.getProcessOperationVersion();
		String machineName = sampleSrcLotData.getMachineName();
		String toProcessFlowName = sampleSrcLotData.getToProcessFlowName();
		String toProcessFlowVersion = sampleSrcLotData.getToProcessFlowVersion();
		String toProcessOperationName = sampleSrcLotData.getToProcessOperationName();
		String toProcessOperationVersion = sampleSrcLotData.getToProcessOperationVersion();
		String returnProcessFlowName = sampleSrcLotData.getReturnProcessFlowName();
		String returnProcessFlowVersion = sampleSrcLotData.getReturnProcessFlowVersion();
		String returnProcessOperationName = sampleSrcLotData.getReturnOperationName();
		String returnProcessOperationVersion = sampleSrcLotData.getReturnOperationVersion();
		String lotSampleFlag = sampleSrcLotData.getLotSampleFlag();
		String lotSampleCount = sampleSrcLotData.getLotSampleCount();
		String currentLotCount = sampleSrcLotData.getCurrentLotCount();
		String totalLotCount = sampleSrcLotData.getTotalLotCount();
		String productSampleCount = sampleSrcLotData.getProductSampleCount();
		String productSamplePosition = sampleSrcLotData.getProductSamplePosition();
		String priority = sampleSrcLotData.getPriority().toString();
		String manualSampleFlag = sampleSrcLotData.getManualSampleFlag();
		String actualProductCount = sampleSrcLotData.getActualProductCount();
		String actualSamplePosition = sampleSrcLotData.getActualSamplePosition();

		List<SampleProduct> srcSampleProdList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListByLotName(srcLotData.getKey().getLotName(), factoryName, productSpecName,
				productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName,
				toProcessOperationVersion);

		List<String> addSamplePositionList = new ArrayList<String>();
		List<String> deleteSamplePositionList = new ArrayList<String>();
		boolean sampleFlag = false;

		for (SampleProduct srcSampleProd : srcSampleProdList)
		{
			String productName = srcSampleProd.getProductName();
			String sampleProdPosition = srcSampleProd.getActualSamplePosition();

			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

			if (StringUtils.equals(productData.getLotName(), destLotData.getKey().getLotName()))
			{
				List<SampleProduct> sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListWithoutMachineName(productName, srcLotData.getKey().getLotName(),
						factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion, toProcessFlowName, toProcessFlowVersion,
						toProcessOperationName, toProcessOperationVersion);

				for (SampleProduct sampleProd : sampleProductList)
				{
					ExtendedObjectProxy.getSampleProductService().updateSampleProductKey(sampleProd, eventInfo, "", destLotData.getKey().getLotName(), "", "", "", "", "", "", "", "", "", "", "", "",
							"", "", "", actualSamplePosition, "", "");
				}

				addSamplePositionList.add(Long.toString(productData.getPosition()));

				sampleFlag = true;

				if (!deleteSamplePositionList.contains(sampleProdPosition))
				{
					deleteSamplePositionList.add(sampleProdPosition);
				}
			}
		}

		if (sampleFlag)
		{
			// Modify or Delete SourceLot SampleData
			if (deleteSamplePositionList.size() > 0)
			{
				List<String> afterActualPositionList = new ArrayList<String>();

				if (Integer.parseInt(actualProductCount) > deleteSamplePositionList.size())
				{
					log.info("Modify Source SampleLot Data - DeletePositionList : " + deleteSamplePositionList);

					if (StringUtils.contains(actualSamplePosition, ","))
					{
						String[] actualPositionArray = StringUtils.split(actualSamplePosition, ",");

						for (int i = 0; i < actualPositionArray.length; i++)
						{
							for (String deletePosition : deleteSamplePositionList)
							{
								if (!StringUtils.equals(actualPositionArray[i].trim(), deletePosition))
								{
									if (!afterActualPositionList.contains(actualPositionArray[i].trim()))
									{
										afterActualPositionList.add(actualPositionArray[i].trim());
									}
								}
							}
						}
					}

					afterActualPositionList = CommonUtil.sortActualSamplePosition(afterActualPositionList);

					// Modify
					ExtendedObjectProxy.getSampleLotService().updateSampleLot(eventInfo, srcLotData.getKey().getLotName(), factoryName, productSpecName, productSpecVersion, processFlowName,
							processFlowVersion, processOperationName, processOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion,
							lotSampleFlag, lotSampleCount, currentLotCount, totalLotCount, productSampleCount, productSamplePosition, String.valueOf(afterActualPositionList.size()),
							CommonUtil.toStringWithoutBrackets(afterActualPositionList), manualSampleFlag, "", priority, returnProcessFlowName, returnProcessFlowVersion, returnProcessOperationName,
							returnProcessOperationVersion, "");
				}
				else
				{
					log.info("Delete Source SampleLot Data");

					// Delete
					ExtendedObjectProxy.getSampleLotService().deleteSampleLotWithOutMachineName(eventInfo, srcLotData.getKey().getLotName(), factoryName, productSpecName, productSpecVersion,
							processFlowName, processFlowVersion, processOperationName, processOperationVersion, toProcessFlowName, toProcessFlowVersion, toProcessOperationName,
							toProcessOperationVersion);
				}
			}

			// Modify DestLot SampleData
			if (addSamplePositionList.size() > 0)
			{
				log.info("Modify Dest SampleLot Data - AddPositionList : " + addSamplePositionList);

				List<String> afterActualPositionList = new ArrayList<String>();
				String destActualSamplePosition = sampleDestLotData.getActualSamplePosition();

				if (StringUtils.contains(destActualSamplePosition, ","))
				{
					String[] actualPositionArray = StringUtils.split(destActualSamplePosition, ",");

					for (int i = 0; i < actualPositionArray.length; i++)
					{
						afterActualPositionList.add(actualPositionArray[i].trim());
					}

					for (String addSamplePosition : addSamplePositionList)
					{
						if (!afterActualPositionList.contains(addSamplePosition))
						{
							afterActualPositionList.add(addSamplePosition);
						}
					}
				}
				else
				{
					if (!StringUtils.isEmpty(destActualSamplePosition))
					{
						afterActualPositionList.add(destActualSamplePosition);
					}

					for (String addSamplePosition : addSamplePositionList)
					{
						if (!afterActualPositionList.contains(addSamplePosition))
						{
							afterActualPositionList.add(addSamplePosition);
						}
					}
				}

				afterActualPositionList = CommonUtil.sortActualSamplePosition(afterActualPositionList);

				// Modify
				ExtendedObjectProxy.getSampleLotService().updateSampleLot(eventInfo, destLotData.getKey().getLotName(), factoryName, productSpecName, productSpecVersion, processFlowName,
						processFlowVersion, processOperationName, processOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion,
						lotSampleFlag, lotSampleCount, currentLotCount, totalLotCount, productSampleCount, productSamplePosition, String.valueOf(afterActualPositionList.size()),
						CommonUtil.toStringWithoutBrackets(afterActualPositionList), manualSampleFlag, "", priority, returnProcessFlowName, returnProcessFlowVersion, returnProcessOperationName,
						returnProcessOperationVersion, "");
			}
		}
	}

	public void mergeFutureActionData(EventInfo eventInfo, Lot sourceLotData, Lot destLotData) throws CustomException
	{
		log.info("Start MergeFutureAction");
		if (StringUtils.isEmpty(eventInfo.getEventName()))
		{
			eventInfo.setEventName("MergeFutureAction");
		}

		sourceLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(sourceLotData.getKey().getLotName());
		List<LotFutureAction> sourceHoldFutureActionDataList = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataListWithLotNameActionName(sourceLotData.getKey().getLotName(),
				"hold");

		if (sourceHoldFutureActionDataList != null)
		{
			for (LotFutureAction sourceFutureActionData : sourceHoldFutureActionDataList)
			{
				String factoryName = sourceFutureActionData.getFactoryName();
				String processFlowName = sourceFutureActionData.getProcessFlowName();
				String processFlowVersion = sourceFutureActionData.getProcessFlowVersion();
				String processOperationName = sourceFutureActionData.getProcessOperationName();
				String processOperationVersion = sourceFutureActionData.getProcessOperationVersion();
				int position = Integer.parseInt(sourceFutureActionData.getPosition().toString());
				String actionName = sourceFutureActionData.getActionName();
				String actionType = sourceFutureActionData.getActionType();
				String reasonCodeType = sourceFutureActionData.getReasonCodeType();
				String reasonCode = sourceFutureActionData.getReasonCode();
				String attribute1 = sourceFutureActionData.getAttribute1();
				String attribute2 = sourceFutureActionData.getAttribute2();
				String attribute3 = sourceFutureActionData.getAttribute3();
				String beforeAction = sourceFutureActionData.getBeforeAction();
				String afterAction = sourceFutureActionData.getAfterAction();
				String beforeActionComment = sourceFutureActionData.getBeforeActionComment();
				String afterActionComment = sourceFutureActionData.getAfterActionComment();
				String beforeActionUser = sourceFutureActionData.getBeforeActionUser();
				String afterActionUser = sourceFutureActionData.getAfterActionUser();

				List<LotFutureAction> destFutureActionDataList = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataWithActionName(destLotData.getKey().getLotName(), factoryName,
						processFlowName, processFlowVersion, processOperationName, processOperationVersion, position, reasonCode, "hold");

				if (destFutureActionDataList == null)
				{
					log.info("Insert DestLot FutureActionData");
					eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
					ExtendedObjectProxy.getLotFutureActionService().insertLotFutureAction(eventInfo, destLotData.getKey().getLotName(), factoryName, processFlowName, processFlowVersion,
							processOperationName, processOperationVersion, position, reasonCode, reasonCodeType, actionName, actionType, attribute1, attribute2, attribute3, beforeAction, afterAction,
							beforeActionComment, afterActionComment, beforeActionUser, afterActionUser);
				}

				if (StringUtils.equals(sourceLotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Emptied))
				{
					log.info("Delete SourceFutureActionData - SourceLot State : " + sourceLotData.getLotState());
					eventInfo.setEventName("Delete");
					ExtendedObjectProxy.getLotFutureActionService().deleteLotFutureActionWithoutReaconCode(eventInfo, sourceLotData.getKey().getLotName(), factoryName, processFlowName,
							processFlowVersion, processOperationName, processOperationVersion, position);
				}
			}
		}
	}

	public void cloneLotFutureAction(EventInfo eventInfo, Lot SourceLotData, String DestLotName, List<Map<String, Object>> lotListByProductList)
	{
		try
		{
			log.info("LotListByProductList size : " + lotListByProductList.size());

			if (lotListByProductList.size() > 1)
			{
				for (Map<String, Object> lot : lotListByProductList)
				{
					String sourceLotName = ConvertUtil.getMapValueByName(lot, "LOTNAME");
					Lot sourceLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(sourceLotName);

					List<LotFutureAction> sourceHoldFutureActionDataList = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataListWithLotNameActionName(
							sourceLotData.getKey().getLotName(), "hold");

					if (sourceHoldFutureActionDataList != null)
					{
						for (LotFutureAction sourceFutureActionData : sourceHoldFutureActionDataList)
						{
							String factoryName = sourceFutureActionData.getFactoryName();
							String processFlowName = sourceFutureActionData.getProcessFlowName();
							String processFlowVersion = sourceFutureActionData.getProcessFlowVersion();
							String processOperationName = sourceFutureActionData.getProcessOperationName();
							String processOperationVersion = sourceFutureActionData.getProcessOperationVersion();
							int position = Integer.parseInt(sourceFutureActionData.getPosition().toString());
							String actionName = sourceFutureActionData.getActionName();
							String actionType = sourceFutureActionData.getActionType();
							String reasonCodeType = sourceFutureActionData.getReasonCodeType();
							String reasonCode = sourceFutureActionData.getReasonCode();
							String attribute1 = sourceFutureActionData.getAttribute1();
							String attribute2 = sourceFutureActionData.getAttribute2();
							String attribute3 = sourceFutureActionData.getAttribute3();
							String beforeAction = sourceFutureActionData.getBeforeAction();
							String afterAction = sourceFutureActionData.getAfterAction();
							String beforeActionComment = sourceFutureActionData.getBeforeActionComment();
							String afterActionComment = sourceFutureActionData.getAfterActionComment();
							String beforeActionUser = sourceFutureActionData.getBeforeActionUser();
							String afterActionUser = sourceFutureActionData.getAfterActionUser();

							List<LotFutureAction> destFutureActionDataList = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataListWithReasonCodeType(DestLotName, factoryName,
									processFlowName, processFlowVersion, processOperationName, processOperationVersion, position, reasonCode, "hold");

							if (destFutureActionDataList == null)
							{
								log.info("Insert DestLot FutureActionData");
								eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
								ExtendedObjectProxy.getLotFutureActionService().insertLotFutureAction(eventInfo, DestLotName, factoryName, processFlowName, processFlowVersion, processOperationName,
										processOperationVersion, position, reasonCode, reasonCodeType, actionName, actionType, attribute1, attribute2, attribute3, beforeAction, afterAction,
										beforeActionComment, afterActionComment, beforeActionUser, afterActionUser);
							}

							if (StringUtils.equals(sourceLotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Emptied))
							{
								log.info("Delete SourceFutureActionData - SourceLot State : " + sourceLotData.getLotState());
								eventInfo.setEventName("Delete");
								ExtendedObjectProxy.getLotFutureActionService().deleteLotFutureActionWithoutReaconCode(eventInfo, sourceLotData.getKey().getLotName(), factoryName, processFlowName,
										processFlowVersion, processOperationName, processOperationVersion, position);
							}
						}
					}
				}

				List<Map<String, Object>> skipFutureActionList = checkFutureSkipInfo(lotListByProductList);

				for (Map<String, Object> skipFutureAction : skipFutureActionList)
				{
					int count = Integer.parseInt(ConvertUtil.getMapValueByName(skipFutureAction, "COUNT"));
					String factoryName = ConvertUtil.getMapValueByName(skipFutureAction, "FACTORYNAME");
					String processFlowName = ConvertUtil.getMapValueByName(skipFutureAction, "PROCESSFLOWNAME");
					String processFlowVersion = ConvertUtil.getMapValueByName(skipFutureAction, "PROCESSFLOWVERSION");
					String processOperationName = ConvertUtil.getMapValueByName(skipFutureAction, "PROCESSOPERATIONNAME");
					String processOperationVersion = ConvertUtil.getMapValueByName(skipFutureAction, "PROCESSOPERATIONVERSION");
					int position = Integer.parseInt(ConvertUtil.getMapValueByName(skipFutureAction, "POSITION"));
					String reasonCode = ConvertUtil.getMapValueByName(skipFutureAction, "REASONCODE");

					if (count == lotListByProductList.size())
					{
						List<LotFutureAction> srcSkipFutureActionDataList = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataWithActionName(
								ConvertUtil.getMapValueByName(lotListByProductList.get(0), "LOTNAME"), factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion,
								position, reasonCode, "skip");

						if (srcSkipFutureActionDataList.size() > 0)
						{
							for (LotFutureAction srcSkipFutureActionData : srcSkipFutureActionDataList)
							{
								String actionName = srcSkipFutureActionData.getActionName();
								String actionType = srcSkipFutureActionData.getActionType();
								String reasonCodeType = srcSkipFutureActionData.getReasonCodeType();
								String attribute1 = srcSkipFutureActionData.getAttribute1();
								String attribute2 = srcSkipFutureActionData.getAttribute2();
								String attribute3 = srcSkipFutureActionData.getAttribute3();
								String beforeAction = srcSkipFutureActionData.getBeforeAction();
								String afterAction = srcSkipFutureActionData.getAfterAction();
								String beforeActionComment = srcSkipFutureActionData.getBeforeActionComment();
								String afterActionComment = srcSkipFutureActionData.getAfterActionComment();
								String beforeActionUser = srcSkipFutureActionData.getBeforeActionUser();
								String afterActionUser = srcSkipFutureActionData.getAfterActionUser();

								List<LotFutureAction> destFutureActionDataList = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataWithActionName(DestLotName, factoryName,
										processFlowName, processFlowVersion, processOperationName, processOperationVersion, position, reasonCode, "skip");

								if (destFutureActionDataList.size() < 1)
								{
									log.info("Insert DestLot FutureActionData");
									eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
									ExtendedObjectProxy.getLotFutureActionService().insertLotFutureAction(eventInfo, DestLotName, factoryName, processFlowName, processFlowVersion,
											processOperationName, processOperationVersion, position, reasonCode, reasonCodeType, actionName, actionType, attribute1, attribute2, attribute3,
											beforeAction, afterAction, beforeActionComment, afterActionComment, beforeActionUser, afterActionUser);
								}
							}
						}
					}
				}
			}
			else
			{
				if (StringUtils.isEmpty(SourceLotData.getKey().getLotName()))
				{
					String sourceLotName = ConvertUtil.getMapValueByName(lotListByProductList.get(0), "LOTNAME");
					SourceLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(sourceLotName);
				}

				try
				{
					List<LotFutureAction> futureActionResult = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataListByLotName(SourceLotData.getKey().getLotName(),
							SourceLotData.getFactoryName());

					if (futureActionResult != null)
					{
						log.info("Lot[" + SourceLotData.getKey().getLotName() + "] have reserved Future Action");

						for (LotFutureAction futureAction : futureActionResult)
						{
							String factoryName = futureAction.getFactoryName();
							String processFlowName = futureAction.getProcessFlowName();
							String processFlowVersion = futureAction.getProcessFlowVersion();
							String processOperationName = futureAction.getProcessOperationName();
							String processOperationVersion = futureAction.getProcessOperationVersion();
							int position = Integer.parseInt(futureAction.getPosition().toString());
							String actionName = futureAction.getActionName();
							String actionType = futureAction.getActionType();
							String eventName = futureAction.getLastEventName();
							String reasonCodeType = futureAction.getReasonCodeType();
							String reasonCode = futureAction.getReasonCode();
							String attribute1 = futureAction.getAttribute1();
							String attribute2 = futureAction.getAttribute2();
							String attribute3 = futureAction.getAttribute3();
							String beforeAction = futureAction.getBeforeAction();
							String afterAction = futureAction.getAfterAction();
							String beforeActionComment = futureAction.getBeforeActionComment();
							String afterActionComment = futureAction.getAfterActionComment();
							String beforeActionUser = futureAction.getBeforeActionUser();
							String afterActionUser = futureAction.getAfterActionUser();

							if (StringUtils.isNotEmpty(beforeActionComment))
							{
								if (!beforeActionComment.substring(0, 5).equalsIgnoreCase("Split"))
								{
									beforeActionComment = "Split," + beforeActionComment;
								}
							}
							else if ((StringUtils.isEmpty(beforeActionComment) && beforeAction.equalsIgnoreCase("True"))
									|| (StringUtils.isEmpty(beforeActionComment) && eventName.equalsIgnoreCase("skip")))
							{
								beforeActionComment = "Split," + beforeActionComment;
							}
							else
							{
								beforeActionComment = "";
							}

							if (StringUtils.isNotEmpty(afterActionComment))
							{
								if (!afterActionComment.substring(0, 5).equalsIgnoreCase("Split"))
								{
									afterActionComment = "Split," + afterActionComment;
								}
							}
							else if ((StringUtils.isEmpty(afterActionComment) && afterAction.equalsIgnoreCase("True"))
									|| (StringUtils.isEmpty(afterActionComment) && eventName.equalsIgnoreCase("skip")))
							{
								afterActionComment = "Split," + afterActionComment;
							}
							else
							{
								afterActionComment = "";
							}

							eventInfo.setEventTimeKey(ConvertUtil.getCurrTimeKey());
							ExtendedObjectProxy.getLotFutureActionService().insertLotFutureAction(eventInfo, DestLotName, factoryName, processFlowName, processFlowVersion, processOperationName,
									processOperationVersion, position, reasonCode, reasonCodeType, actionName, actionType, attribute1, attribute2, attribute3, beforeAction, afterAction,
									beforeActionComment, afterActionComment, beforeActionUser, afterActionUser);
						}
					}
				}
				catch (Exception ex)
				{
					log.info("Error : Lot[" + SourceLotData.getKey().getLotName() + "] do not have reserved Future Action.");
				}
			}
		}
		catch (Exception ex)
		{
			log.info("Reserve Skip or Reserve Hold Clone Error!");
		}

	}

	public void changeMQCSpecToBank(Lot sourceLotData, EventInfo eventInfo, Lot lotData) throws CustomException
	{
		String currentNode = lotData.getNodeStack();
		String originalNode = currentNode.substring(0, currentNode.lastIndexOf("."));

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT NODEID, PROCESSFLOWNAME, PROCESSFLOWVERSION, NODEATTRIBUTE1, NODEATTRIBUTE2, FACTORYNAME ");
		sql.append("  FROM NODE ");
		sql.append(" WHERE NODEID = :NODEID ");
		sql.append("   AND NODETYPE = 'ProcessOperation' ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("NODEID", originalNode);

		List<Map<String, Object>> orginalNodeResult = null;
		try
		{
			orginalNodeResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		}
		catch (Exception e)
		{
			log.info("Error occured - There is no ProcessFlow/ProcessOperation information for the organalNode.");
		}

		if (orginalNodeResult.size() > 0)
		{
			List<MQCPlan> mqcPlanData = ExtendedObjectProxy.getMQCPlanService().getReleasedMQCPlanDataByLotName(sourceLotData.getKey().getLotName());

			String prepareSpecName = "";
			String prepareSpecVersion = "";

			if (mqcPlanData != null)
			{
				prepareSpecName = mqcPlanData.get(0).getPrepareSpecName();
				prepareSpecVersion = mqcPlanData.get(0).getPrepareSpecVersion();
			}

			lotData.setNodeStack(originalNode);
			lotData.setProcessFlowName(ConvertUtil.getMapValueByName(orginalNodeResult.get(0), "PROCESSFLOWNAME"));
			lotData.setProcessFlowVersion(ConvertUtil.getMapValueByName(orginalNodeResult.get(0), "PROCESSFLOWVERSION"));
			lotData.setProcessOperationName(ConvertUtil.getMapValueByName(orginalNodeResult.get(0), "NODEATTRIBUTE1"));
			lotData.setProcessOperationVersion(ConvertUtil.getMapValueByName(orginalNodeResult.get(0), "NODEATTRIBUTE2"));
			lotData.setProductSpecName(prepareSpecName);
			lotData.setProductSpecVersion(prepareSpecVersion);

			// add
			ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo(lotData.getProductionType(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProductSpec2Name(),
					lotData.getProductSpec2Version(), lotData.getProductRequestName(), lotData.getSubProductUnitQuantity1(), lotData.getSubProductUnitQuantity2(), lotData.getDueDate(),
					lotData.getPriority(), lotData.getFactoryName(), lotData.getAreaName(), lotData.getLotState(), lotData.getLotProcessState(), lotData.getLotHoldState(),
					lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), lotData.getNodeStack(),
					new ArrayList<ProductU>());
			changeSpecInfo.setUdfs(lotData.getUdfs());
			try
			{
				MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
			}
			catch (CustomException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			log.info("Error occured - There is no ProcessFlow/ProcessOperation information for the organalNode.");
		}
	}

	public void sourceLotChangeMQCSpecToBank(Lot sourceLotData, EventInfo eventInfo) throws CustomException
	{

		String currentNode = sourceLotData.getNodeStack();
		String originalNode = currentNode.substring(0, currentNode.lastIndexOf("."));

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT NODEID, PROCESSFLOWNAME, PROCESSFLOWVERSION, NODEATTRIBUTE1, NODEATTRIBUTE2, FACTORYNAME ");
		sql.append("  FROM NODE ");
		sql.append(" WHERE NODEID = :NODEID ");
		sql.append("   AND NODETYPE = 'ProcessOperation' ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("NODEID", originalNode);

		List<Map<String, Object>> orginalNodeResult = null;
		try
		{
			orginalNodeResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		}
		catch (Exception e)
		{
			log.info("Error occured - There is no ProcessFlow/ProcessOperation information for the organalNode.");
		}

		if (orginalNodeResult.size() > 0)
		{
			List<MQCPlan> mqcPlanData = ExtendedObjectProxy.getMQCPlanService().getReleasedMQCPlanDataByLotName(sourceLotData.getKey().getLotName());

			String prepareSpecName = "";
			String prepareSpecVersion = "";

			if (mqcPlanData != null)
			{
				prepareSpecName = mqcPlanData.get(0).getPrepareSpecName();
				prepareSpecVersion = mqcPlanData.get(0).getPrepareSpecVersion();
			}

			sourceLotData.setNodeStack(originalNode);
			sourceLotData.setProcessFlowName(ConvertUtil.getMapValueByName(orginalNodeResult.get(0), "PROCESSFLOWNAME"));
			sourceLotData.setProcessFlowVersion(ConvertUtil.getMapValueByName(orginalNodeResult.get(0), "PROCESSFLOWVERSION"));
			sourceLotData.setProcessOperationName(ConvertUtil.getMapValueByName(orginalNodeResult.get(0), "NODEATTRIBUTE1"));
			sourceLotData.setProcessOperationVersion(ConvertUtil.getMapValueByName(orginalNodeResult.get(0), "NODEATTRIBUTE2"));
			sourceLotData.setProductSpecName(prepareSpecName);
			sourceLotData.setProductSpecVersion(prepareSpecVersion);
			sourceLotData.setLotProcessState("WAIT");

			// add
			ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo(sourceLotData.getProductionType(), sourceLotData.getProductSpecName(), sourceLotData.getProductSpecVersion(),
					sourceLotData.getProductSpec2Name(), sourceLotData.getProductSpec2Version(), sourceLotData.getProductRequestName(), sourceLotData.getSubProductUnitQuantity1(),
					sourceLotData.getSubProductUnitQuantity2(), sourceLotData.getDueDate(), sourceLotData.getPriority(), sourceLotData.getFactoryName(), sourceLotData.getAreaName(),
					sourceLotData.getLotState(), sourceLotData.getLotProcessState(), sourceLotData.getLotHoldState(), sourceLotData.getProcessFlowName(), sourceLotData.getProcessFlowVersion(),
					sourceLotData.getProcessOperationName(), sourceLotData.getProcessOperationVersion(), sourceLotData.getNodeStack(), new ArrayList<ProductU>());
			changeSpecInfo.setUdfs(sourceLotData.getUdfs());
			try
			{
				MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, sourceLotData, changeSpecInfo);
			}
			catch (CustomException e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			log.info("Error occured - There is no ProcessFlow/ProcessOperation information for the organalNode.");
		}
	}

	public static List<Map<String, Object>> checkFutureSkipInfo(List<Map<String, Object>> lotListByProductList)
	{
		List<String> lotList = new ArrayList<String>();

		for (Map<String, Object> lot : lotListByProductList)
		{
			String lotName = ConvertUtil.getMapValueByName(lot, "LOTNAME");

			if (!lotList.contains(lotName))
			{
				lotList.add(lotName);
			}
		}

		log.info("Lotlist : " + lotList);
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT COUNT (*) AS COUNT, ");
		sql.append("       FACTORYNAME, ");
		sql.append("       PROCESSFLOWNAME, ");
		sql.append("       PROCESSFLOWVERSION, ");
		sql.append("       PROCESSOPERATIONNAME, ");
		sql.append("       PROCESSOPERATIONVERSION, ");
		sql.append("       POSITION, ");
		sql.append("       REASONCODE ");
		sql.append("  FROM CT_LOTFUTUREACTION ");
		sql.append(" WHERE LOTNAME IN (:LOTLIST) ");
		sql.append("   AND ACTIONNAME = :ACTIONNAME ");
		sql.append("GROUP BY FACTORYNAME, ");
		sql.append("         PROCESSFLOWNAME, ");
		sql.append("         PROCESSFLOWVERSION, ");
		sql.append("         PROCESSOPERATIONNAME, ");
		sql.append("         PROCESSOPERATIONVERSION, ");
		sql.append("         POSITION, ");
		sql.append("         REASONCODE ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("LOTLIST", lotList);
		args.put("ACTIONNAME", "skip");

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		return sqlResult;
	}

	public static List<ProductPGQS> setProductPGQSSequenceNew(Element element, String productName) throws CustomException
	{

		Element ListElement = element.getChild("SUBPRODUCTLIST");

		Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
		ProductSpec productSpecData = MESProductServiceProxy.getProductServiceUtil().getProductSpecByProductName(productData);

		int gradeposition = 0;

		int x = Integer.valueOf(productSpecData.getUdfs().get("PRODUCTCOUNTTOXAXIS"));
		int y = Integer.valueOf(productSpecData.getUdfs().get("PRODUCTCOUNTTOYAXIS"));

		List<ProductPGQS> productPGQSSequence = new ArrayList<ProductPGQS>();

		if (element != null)
		{
			for (Iterator iterator = ListElement.getChildren().iterator(); iterator.hasNext();)
			{
				gradeposition++;

				Element subProductE = (Element) iterator.next();

				String subProductName = subProductE.getChild("SUBPRODUCTNAME").getText();
				long position = Long.parseLong(subProductE.getChild("POSITION").getText());
				String subProductGrade = subProductE.getChild("PRODUCTJUDGE").getText();
				String subProductGrades1 = "";
				String subProductGrades2 = "";
				double subProductUnitQuantity1 = (x * y) / 2;
				double subProductUnitQuantity2 = 0;
				double subProductQuantity1 = (x * y) / 2;
				double subProductQuantity2 = 0;

				if (productData.getSubProductGrades1().isEmpty())
					subProductGrades1 = GradeDefUtil.generateGradeSequence(productData.getFactoryName(), "SubProduct", true, (Integer.valueOf(x * y / 2)));
				else if (Long.compare(position, 1) == 0)
					subProductGrades1 = productData.getSubProductGrades1().substring(0, (Integer.valueOf(x * y / 2)));
				else if (Long.compare(position, 2) == 0)
					subProductGrades1 = productData.getSubProductGrades1().substring((Integer.valueOf(x * y / 2)), (Integer.valueOf(x * y)));
				else
					subProductGrades1 = GradeDefUtil.generateGradeSequence(productData.getFactoryName(), "SubProduct", true, (Integer.valueOf(x * y / 2)));

				ProductPGQS productPGQS = new ProductPGQS();
				productPGQS.setProductName(subProductName);
				productPGQS.setPosition(position);
				productPGQS.setProductGrade(subProductGrade);
				productPGQS.setSubProductGrades1(subProductGrades1);
				productPGQS.setSubProductGrades2(subProductGrades2);
				productPGQS.setSubProductUnitQuantity1(subProductUnitQuantity1);
				productPGQS.setSubProductUnitQuantity2(subProductUnitQuantity2);
				productPGQS.setSubProductQuantity1(subProductQuantity1);
				productPGQS.setSubProductQuantity2(subProductQuantity2);
				
				productPGQS.getUdfs().put("CRATENAME", productData.getUdfs().get("CRATENAME"));
				productPGQS.getUdfs().put("ARRAYLOTNAME", productData.getUdfs().get("ARRAYLOTNAME"));
				productPGQS.getUdfs().put("MAINMACHINENAME", productData.getUdfs().get("MAINMACHINENAME"));

				productPGQSSequence.add(productPGQS);
			}
		}

		return productPGQSSequence;
	}

	public static List<ProductNSubProductPGQS> setProductNSubProductPGQSSequenceNew(Element element) throws CustomException
	{

		List<ProductNSubProductPGQS> productNSubProductPGQSSequence = new ArrayList<ProductNSubProductPGQS>();

		String productName = element.getChildText("PRODUCTNAME");

		List<ProductPGQS> productPGQSSequence = setProductPGQSSequenceNew(element, productName);

		Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
		ProductNSubProductPGQS productNSubProductPGQS = new ProductNSubProductPGQS();
		productNSubProductPGQS.setProductName(productName);
		productNSubProductPGQS.setUdfs(productData.getUdfs());
		productNSubProductPGQS.setSubProductPGQSSequence(productPGQSSequence);

		productNSubProductPGQSSequence.add(productNSubProductPGQS);

		return productNSubProductPGQSSequence;
	}

	public List<Map<String, Object>> getSampleLotDataForSkip(String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion) throws CustomException
	{
		Map<String, String> bindMap = new HashMap<String, String>();
		StringBuilder sqlAddconditionString = new StringBuilder();

		log.info(" Start getSampleLotDataForSkip Qry ");

		if (!StringUtils.isEmpty(processFlowName))
		{
			bindMap.put("PROCESSFLOWNAME", processFlowName);
			sqlAddconditionString.append("   AND CS.TOPROCESSFLOWNAME = :PROCESSFLOWNAME ");
		}
		if (!StringUtils.isEmpty(processFlowVersion))
		{
			bindMap.put("PROCESSFLOWVERSION", processFlowVersion);
			sqlAddconditionString.append("   AND CS.TOPROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		}
		
		/*if (!StringUtil.isEmpty(processOperationName))
		{
			bindMap.put("PROCESSOPERATIONNAME", processOperationName);
			sqlAddconditionString.append("   AND CS.TOPROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		}*/
		if (!StringUtils.isEmpty(processOperationVersion))
		{
			bindMap.put("PROCESSOPERATIONVERSION", processOperationVersion);
			sqlAddconditionString.append("   AND CS.TOPROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		}

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT CS.LOTNAME, ");
		sql.append("       CS.FACTORYNAME, ");
		sql.append("       CS.PRODUCTSPECNAME, ");
		sql.append("       CS.PRODUCTSPECVERSION, ");
		sql.append("       CS.PROCESSFLOWNAME, ");
		sql.append("       CS.PROCESSFLOWVERSION, ");
		sql.append("       CS.PROCESSOPERATIONNAME, ");
		sql.append("       CS.PROCESSOPERATIONVERSION, ");
		sql.append("       CS.MACHINENAME, ");
		sql.append("       CS.TOPROCESSFLOWNAME, ");
		sql.append("       CS.TOPROCESSFLOWVERSION, ");
		sql.append("       CS.TOPROCESSOPERATIONNAME, ");
		sql.append("       CS.TOPROCESSOPERATIONVERSION, ");
		sql.append("       CS.LOTSAMPLEFLAG, ");
		sql.append("       CS.LOTSAMPLECOUNT, ");
		sql.append("       CS.CURRENTLOTCOUNT, ");
		sql.append("       CS.TOTALLOTCOUNT, ");
		sql.append("       CS.PRODUCTSAMPLECOUNT, ");
		sql.append("       CS.PRODUCTSAMPLEPOSITION, ");
		sql.append("       CS.ACTUALPRODUCTCOUNT, ");
		sql.append("       CS.ACTUALSAMPLEPOSITION, ");
		sql.append("       CS.LASTEVENTCOMMENT, ");
		sql.append("       CS.MANUALSAMPLEFLAG, ");
		sql.append("       CS.RETURNPROCESSFLOWNAME, ");
		sql.append("       CS.RETURNPROCESSFLOWVERSION, ");
		sql.append("       CS.RETURNOPERATIONNAME, ");
		sql.append("       CS.RETURNOPERATIONVERSION, ");
		sql.append("       CS.PRIORITY, ");
		sql.append("       CS.FORCESAMPLINGFLAG ");
		sql.append("  FROM CT_SAMPLELOT CS, ");
		sql.append("       (SELECT LEVEL LV, ");
		sql.append("               N.FACTORYNAME, ");
		sql.append("               N.NODEATTRIBUTE1 PROCESSOPERATIONNAME, ");
		sql.append("               N.NODEATTRIBUTE2 PROCESSOPERATIONVERSION, ");
		sql.append("               N.PROCESSFLOWNAME, ");
		sql.append("               N.PROCESSFLOWVERSION, ");
		sql.append("               N.NODEID ");
		sql.append("          FROM ARC A, NODE N, PROCESSFLOW PF ");
		sql.append("         WHERE 1 = 1 ");
		sql.append("           AND N.NODETYPE = 'ProcessOperation' ");
		sql.append("           AND PF.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("           AND N.FACTORYNAME = :FACTORYNAME ");
		sql.append("           AND N.PROCESSFLOWNAME = PF.PROCESSFLOWNAME ");
		sql.append("           AND N.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION ");
		sql.append("           AND N.PROCESSFLOWNAME = A.PROCESSFLOWNAME ");
		sql.append("           AND N.FACTORYNAME = PF.FACTORYNAME ");
		sql.append("           AND A.FROMNODEID = N.NODEID ");
		sql.append("        START WITH N.NODETYPE = 'Start' ");
		sql.append("        CONNECT BY NOCYCLE A.FROMNODEID = PRIOR A.TONODEID ");
		sql.append("               AND A.FACTORYNAME = :FACTORYNAME) QL ");
		sql.append(" WHERE 1 = 1 ");
		sql.append("   AND CS.TOPROCESSOPERATIONNAME = QL.PROCESSOPERATIONNAME ");
		sql.append("   AND CS.LOTNAME = :LOTNAME ");
		sql.append("   AND CS.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND CS.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("   AND CS.PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");
		sql.append(sqlAddconditionString);
		sql.append("ORDER BY QL.LV ");
		
		bindMap.put("LOTNAME", lotName);
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PRODUCTSPECNAME", productSpecName);
		bindMap.put("PRODUCTSPECVERSION", productSpecVersion);

		List<Map<String, Object>> sqlResult = new ArrayList<Map<String, Object>>();
		try
		{
			sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
		}
		catch (Exception e)
		{
			log.info("empty sample Lot");
		}

		return sqlResult;
	}

	public void transferProductSyncData(EventInfo eventInfo, List<String> srcLotList, List<String> destLotList) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		if (srcLotList.size() > 0 && destLotList.size() > 0)
		{
			log.info("Start Sync Sample Data");
			
			// Sync SampleData
			if (checkSampleData(srcLotList, destLotList))
			{
				for (String srcLotName : srcLotList)
				{
					Lot srcLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(srcLotName);
					List<SampleLot> srcSampleLotList = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListBySpec(srcLotData.getKey().getLotName(), srcLotData.getFactoryName(),
							srcLotData.getProductSpecName(), srcLotData.getProductSpecVersion());

					if (srcSampleLotList != null)
					{
						for (SampleLot srcSampleLot : srcSampleLotList)
						{
							String factoryName = srcSampleLot.getFactoryName();
							String productSpecName = srcSampleLot.getProductSpecName();
							String productSpecVersion = srcSampleLot.getProductSpecVersion();
							String processFlowName = srcSampleLot.getProcessFlowName();
							String processFlowVersion = srcSampleLot.getProcessFlowVersion();
							String processOperationName = srcSampleLot.getProcessOperationName();
							String processOperationVersion = srcSampleLot.getProcessOperationVersion();
							String machineName = srcSampleLot.getMachineName();
							String toProcessFlowName = srcSampleLot.getToProcessFlowName();
							String toProcessFlowVersion = srcSampleLot.getToProcessFlowVersion();
							String toProcessOperationName = srcSampleLot.getToProcessOperationName();
							String toProcessOperationVersion = srcSampleLot.getToProcessOperationVersion();

							for (String destLotName : destLotList)
							{
								Lot destLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(destLotName);
								SampleLot destSampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotData(destLotData.getKey().getLotName(), factoryName, productSpecName,
										productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion,
										toProcessOperationName, toProcessOperationVersion);

								srcSampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotData(srcSampleLot.getLotName(), factoryName, productSpecName, productSpecVersion, processFlowName,
										processFlowVersion, processOperationName, processOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName,
										toProcessOperationVersion);

								if (destSampleLot != null)
								{
									syncSampleDataExistCase(eventInfo, srcLotData, destLotData, srcSampleLot, destSampleLot);
								}
								else
								{
									syncSampleDataNotExistCase(eventInfo, srcLotData, destLotData, srcSampleLot);
								}
							}
						}
					}
				}
			}
			
			log.info("End Sync Sample Data");

			log.info("Start Sync FutureAction Data");
			
			// Sync FutureActionData
			if (checkFutureActionData(srcLotList))
			{
				for (String srcLotName : srcLotList)
				{
					Lot srcLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(srcLotName);
					List<LotFutureAction> srcFutureActionDataList = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataListByLotName(srcLotData.getKey().getLotName(),
							srcLotData.getFactoryName());

					if (srcFutureActionDataList != null)
					{
						for (LotFutureAction srcFutureActionData : srcFutureActionDataList)
						{
							String factoryName = srcFutureActionData.getFactoryName();
							String processFlowName = srcFutureActionData.getProcessFlowName();
							String processFlowVersion = srcFutureActionData.getProcessFlowVersion();
							String processOperationName = srcFutureActionData.getProcessOperationName();
							String processOperationVersion = srcFutureActionData.getProcessOperationVersion();
							int position = Integer.parseInt(srcFutureActionData.getPosition().toString());
							String reasonCode = srcFutureActionData.getReasonCode();

							for (String destLotName : destLotList)
							{
								LotFutureAction destFutureActionData = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionData(destLotName, factoryName, processFlowName,
										processFlowVersion, processOperationName, processOperationVersion, position, reasonCode);

								if (destFutureActionData != null)
								{
									if (StringUtils.equals(destFutureActionData.getActionName(), GenericServiceProxy.getConstantMap().FUTUREACTIONNAME_HOLD))
									{
										syncLotFutureActionDataExistCase(eventInfo, srcFutureActionData, destFutureActionData);
									}
								}
								else
								{
									ExtendedObjectProxy.getLotFutureActionService().insertLotFutureActionForSkip(eventInfo, destLotName, factoryName, processFlowName, processFlowVersion,
											processOperationName, processOperationVersion, position, reasonCode, srcFutureActionData.getReasonCodeType(), srcFutureActionData.getActionName(),
											srcFutureActionData.getActionType(), srcFutureActionData.getAttribute1(), srcFutureActionData.getAttribute2(), srcFutureActionData.getAttribute3(),
											srcFutureActionData.getBeforeAction(), srcFutureActionData.getAfterAction(), srcFutureActionData.getBeforeActionComment(),
											srcFutureActionData.getAfterActionComment(), srcFutureActionData.getBeforeActionUser(), srcFutureActionData.getAfterActionUser(),
											srcFutureActionData.getBeforeMailFlag(), srcFutureActionData.getAfterMailFlag(), srcFutureActionData.getLastEventComment());
								}
							}
						}
					}
				}
			}
			log.info("End Sync FutureAction Data");
			
			// sync TPTJ product 
			syncTPTJData(eventInfo, srcLotList, destLotList);
		}
		
		log.info("Start Sync ProductQueueTime Data");
		
		if (destLotList.size() > 0)
		{
			// Sync ProductQueueTime
			for (String destLotName : destLotList)
			{
				List<String> destProdList = MESLotServiceProxy.getLotServiceUtil().getProductList(destLotName);

				for (String destProd : destProdList)
				{
					List<ProductQueueTime> qtimeData = new ArrayList<ProductQueueTime>();

					try
					{
						qtimeData = ExtendedObjectProxy.getProductQTimeService().getProductQueueTimeData(destProd);
					}
					catch (Exception e)
					{
						qtimeData = null;
					}

					if (qtimeData != null)
					{
						for (ProductQueueTime qtime : qtimeData)
						{
							if (!StringUtils.equals(qtime.getLotName(), destLotName))
							{
								qtime.setLotName(destLotName);
								ExtendedObjectProxy.getProductQTimeService().modify(eventInfo, qtime);
							}
						}
					}
				}
			}
		}
		
		log.info("End Sync ProductQueueTime Data");
	}
	
	public void syncSampleDataExistCase(EventInfo eventInfo, Lot srcLotData, Lot destLotData, SampleLot srcSampleLotData, SampleLot destSampleLotData) throws CustomException
	{
		log.info("Dest SampleData Exist");

		String factoryName = srcSampleLotData.getFactoryName();
		String productSpecName = srcSampleLotData.getProductSpecName();
		String productSpecVersion = srcSampleLotData.getProductSpecVersion();
		String processFlowName = srcSampleLotData.getProcessFlowName();
		String processFlowVersion = srcSampleLotData.getProcessFlowVersion();
		String processOperationName = srcSampleLotData.getProcessOperationName();
		String processOperationVersion = srcSampleLotData.getProcessOperationVersion();
		String machineName = srcSampleLotData.getMachineName();
		String toProcessFlowName = srcSampleLotData.getToProcessFlowName();
		String toProcessFlowVersion = srcSampleLotData.getToProcessFlowVersion();
		String toProcessOperationName = srcSampleLotData.getToProcessOperationName();
		String toProcessOperationVersion = srcSampleLotData.getToProcessOperationVersion();
		String returnProcessFlowName = srcSampleLotData.getReturnProcessFlowName();
		String returnProcessFlowVersion = srcSampleLotData.getReturnProcessFlowVersion();
		String returnProcessOperationName = srcSampleLotData.getReturnOperationName();
		String returnProcessOperationVersion = srcSampleLotData.getReturnOperationVersion();
		String lotSampleFlag = srcSampleLotData.getLotSampleFlag();
		String lotSampleCount = srcSampleLotData.getLotSampleCount();
		String currentLotCount = srcSampleLotData.getCurrentLotCount();
		String totalLotCount = srcSampleLotData.getTotalLotCount();
		String productSampleCount = srcSampleLotData.getProductSampleCount();
		String productSamplePosition = srcSampleLotData.getProductSamplePosition();
		String priority = srcSampleLotData.getPriority().toString();
		String manualSampleFlag = srcSampleLotData.getManualSampleFlag();
		String actualProductCount = srcSampleLotData.getActualProductCount();
		String actualSamplePosition = srcSampleLotData.getActualSamplePosition();
		String forceSampleFlag = srcSampleLotData.getForceSamplingFlag();

		List<SampleProduct> srcSampleProdList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListByLotName(srcLotData.getKey().getLotName(), factoryName, productSpecName,
				productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName,
				toProcessOperationVersion);

		List<String> addSamplePositionList = new ArrayList<String>();
		List<String> deleteSamplePositionList = new ArrayList<String>();
		boolean sampleFlag = false;

		for (SampleProduct srcSampleProd : srcSampleProdList)
		{
			String productName = srcSampleProd.getProductName();
			String sampleProdPosition = srcSampleProd.getActualSamplePosition();

			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

			if (StringUtils.equals(productData.getLotName(), destLotData.getKey().getLotName()))
			{
				List<SampleProduct> sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListWithMachineName(productName, srcLotData.getKey().getLotName(),
						factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion, toProcessFlowName, toProcessFlowVersion,
						toProcessOperationName, toProcessOperationVersion,machineName);
				if(sampleProductList!=null)
				{
					for (SampleProduct sampleProd : sampleProductList)
					{
						ExtendedObjectProxy.getSampleProductService().updateSampleProductKey(sampleProd, eventInfo, "", destLotData.getKey().getLotName(), "", "", "", "", "", "", "", "", "", "", "", "",
								"", "", "", Long.toString(productData.getPosition()), "", "");
					}
				}
				
				addSamplePositionList.add(Long.toString(productData.getPosition()));

				sampleFlag = true;

				if (!deleteSamplePositionList.contains(sampleProdPosition))
				{
					deleteSamplePositionList.add(sampleProdPosition);
				}
			}
		}

		if (sampleFlag)
		{
			// Modify or Delete SourceLot SampleData
			if (deleteSamplePositionList.size() > 0)
			{
				if (Integer.parseInt(actualProductCount) > deleteSamplePositionList.size())
				{
					log.info("Modify Source SampleLot Data - DeletePositionList : " + deleteSamplePositionList);

					List<String> afterActualPositionList = new ArrayList<String>();

					if (actualSamplePosition.lastIndexOf(",") > -1)
					{
						String[] actualPositionArray = StringUtils.split(actualSamplePosition, ",");
						String oriSamplePosition = "";

						for (int i = 0; i < actualPositionArray.length; i++)
						{
							oriSamplePosition = actualPositionArray[i].trim();

							if (deleteSamplePositionList.size() > 0)
							{
								if (!deleteSamplePositionList.contains(oriSamplePosition))
								{
									afterActualPositionList.add(oriSamplePosition);
								}
							}
							else
							{
								afterActualPositionList.add(oriSamplePosition);
							}
						}
					}
					else
					{
						afterActualPositionList.add(actualSamplePosition);
					}

					
//					if (StringUtils.contains(actualSamplePosition, ","))
//					{
//						String[] actualPositionArray = StringUtil.split(actualSamplePosition, ",");
//
//						for (int i = 0; i < actualPositionArray.length; i++)
//						{
//							for (String deletePosition : deleteSamplePositionList)
//							{
//								if (!StringUtils.equals(actualPositionArray[i].trim(), deletePosition))
//								{
//									if (!afterActualPositionList.contains(actualPositionArray[i].trim()))
//									{
//										afterActualPositionList.add(actualPositionArray[i].trim());
//									}
//								}
//							}
//						}
//					}

					afterActualPositionList = CommonUtil.sortActualSamplePosition(afterActualPositionList);

					// Modify
					ExtendedObjectProxy.getSampleLotService().updateSampleLot(eventInfo, srcLotData.getKey().getLotName(), factoryName, productSpecName, productSpecVersion, processFlowName,
							processFlowVersion, processOperationName, processOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion,
							lotSampleFlag, lotSampleCount, currentLotCount, totalLotCount, productSampleCount, productSamplePosition, String.valueOf(afterActualPositionList.size()),
							CommonUtil.toStringWithoutBrackets(afterActualPositionList), manualSampleFlag, "", priority, returnProcessFlowName, returnProcessFlowVersion, returnProcessOperationName,
							returnProcessOperationVersion, forceSampleFlag);
				}
				else
				{
					log.info("Delete Source SampleLot Data");

					// Delete
					ExtendedObjectProxy.getSampleLotService().deleteSampleLotWithOutMachineName(eventInfo, srcLotData.getKey().getLotName(), factoryName, productSpecName, productSpecVersion,
							processFlowName, processFlowVersion, processOperationName, processOperationVersion, toProcessFlowName, toProcessFlowVersion, toProcessOperationName,
							toProcessOperationVersion);
				}
			}

			// Modify DestLot SampleData
			if (addSamplePositionList.size() > 0)
			{
				log.info("Modify Dest SampleLot Data - AddPositionList : " + addSamplePositionList);

				List<String> afterActualPositionList = new ArrayList<String>();
				String destActualSamplePosition = destSampleLotData.getActualSamplePosition();

				if (StringUtils.contains(destActualSamplePosition, ","))
				{
					String[] actualPositionArray = StringUtils.split(destActualSamplePosition, ",");

					for (int i = 0; i < actualPositionArray.length; i++)
					{
						afterActualPositionList.add(actualPositionArray[i].trim());
					}

					for (String addSamplePosition : addSamplePositionList)
					{
						if (!afterActualPositionList.contains(addSamplePosition))
						{
							afterActualPositionList.add(addSamplePosition);
						}
					}
				}
				else
				{
					if (!StringUtils.isEmpty(destActualSamplePosition))
					{
						afterActualPositionList.add(destActualSamplePosition);
					}

					for (String addSamplePosition : addSamplePositionList)
					{
						if (!afterActualPositionList.contains(addSamplePosition))
						{
							afterActualPositionList.add(addSamplePosition);
						}
					}
				}

				afterActualPositionList = CommonUtil.sortActualSamplePosition(afterActualPositionList);

				// Modify
				ExtendedObjectProxy.getSampleLotService().updateSampleLot(eventInfo, destLotData.getKey().getLotName(), factoryName, productSpecName, productSpecVersion, processFlowName,
						processFlowVersion, processOperationName, processOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion,
						lotSampleFlag, lotSampleCount, currentLotCount, totalLotCount, productSampleCount, productSamplePosition, String.valueOf(afterActualPositionList.size()),
						CommonUtil.toStringWithoutBrackets(afterActualPositionList), manualSampleFlag, "", priority, returnProcessFlowName, returnProcessFlowVersion, returnProcessOperationName,
						returnProcessOperationVersion, forceSampleFlag);
			}
		}
	}
	
	public void syncSampleDataNotExistCase(EventInfo eventInfo, Lot srcLotData, Lot destLotData, SampleLot srcSampleLot) throws CustomException
	{
		String factoryName = srcSampleLot.getFactoryName();
		String productSpecName = srcSampleLot.getProductSpecName();
		String productSpecVersion = srcSampleLot.getProductSpecVersion();
		String processFlowName = srcSampleLot.getProcessFlowName();
		String processFlowVersion = srcSampleLot.getProcessFlowVersion();
		String processOperationName = srcSampleLot.getProcessOperationName();
		String processOperationVersion = srcSampleLot.getProcessOperationVersion();
		String machineName = srcSampleLot.getMachineName();
		String toProcessFlowName = srcSampleLot.getToProcessFlowName();
		String toProcessFlowVersion = srcSampleLot.getToProcessFlowVersion();
		String toProcessOperationName = srcSampleLot.getToProcessOperationName();
		String toProcessOperationVersion = srcSampleLot.getToProcessOperationVersion();
		String returnProcessFlowName = srcSampleLot.getReturnProcessFlowName();
		String returnProcessFlowVersion = srcSampleLot.getReturnProcessFlowVersion();
		String returnProcessOperationName = srcSampleLot.getReturnOperationName();
		String returnProcessOperationVersion = srcSampleLot.getReturnOperationVersion();
		String lotSampleFlag = srcSampleLot.getLotSampleFlag();
		String lotSampleCount = srcSampleLot.getLotSampleCount();
		String currentLotCount = srcSampleLot.getCurrentLotCount();
		String totalLotCount = srcSampleLot.getTotalLotCount();
		String productSampleCount = srcSampleLot.getProductSampleCount();
		String productSamplePosition = srcSampleLot.getProductSamplePosition();
		String priority = srcSampleLot.getPriority().toString();
		String manualSampleFlag = srcSampleLot.getManualSampleFlag();
		String forceSamplingFlag = srcSampleLot.getForceSamplingFlag();
		String completeFlowName = srcSampleLot.getCompleteFlowName();
		String completeFlowVersion = srcSampleLot.getCompleteFlowVersion();
		String completeOperationName = srcSampleLot.getCompleteOperationName();
		String completeOperationVersion = srcSampleLot.getCompleteOperationVersion();

		List<SampleProduct> srcSampleProdList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListByLotName(srcLotData.getKey().getLotName(), factoryName, productSpecName,
				productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName,
				toProcessOperationVersion);

		List<String> actualSamplePositionList = new ArrayList<String>();
		List<String> deleteSamplePositionList = new ArrayList<String>();
		boolean sampleFlag = false;

		if (srcSampleProdList != null)
		{
			for (SampleProduct srcSampleProd : srcSampleProdList)
			{
				String productName = srcSampleProd.getProductName();
				Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
				String actualSampleProdPosition = srcSampleProd.getActualSamplePosition();

				if (StringUtils.equals(productData.getLotName(), destLotData.getKey().getLotName()))
				{
					List<SampleProduct> sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListWithMachineName(productName, srcLotData.getKey().getLotName(),
							factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion, toProcessFlowName,
							toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion, machineName);

					if(sampleProductList!=null)
					{
						for (SampleProduct sampleProd : sampleProductList)
						{
							ExtendedObjectProxy.getSampleProductService().updateSampleProductKey(sampleProd, eventInfo, "", destLotData.getKey().getLotName(), "", "", "", "", "", "", "", "", "", "", "",
									"", "", "", "", Long.toString(productData.getPosition()), "", "");
						}
					}
					
					if(!actualSamplePositionList.contains(Long.toString(productData.getPosition())))
					{
						actualSamplePositionList.add(Long.toString(productData.getPosition()));
					}

					sampleFlag = true;

					if (!deleteSamplePositionList.contains(actualSampleProdPosition))
					{
						deleteSamplePositionList.add(actualSampleProdPosition);
					}
				}
			}
		}

		if (sampleFlag)
		{
			
			
			List<String> insertactualPositionList = CommonUtil.sortActualSamplePosition(actualSamplePositionList);

			List<SampleLot> destSampleLotList = ExtendedObjectProxy.getSampleLotService().getSampleLotDataList(destLotData.getKey().getLotName(), factoryName, productSpecName, productSpecVersion,
					processFlowName, processFlowVersion, processOperationName, processOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName,
					toProcessOperationVersion);

			if (destSampleLotList == null)
			{
				ExtendedObjectProxy.getSampleLotService().insertSampleLot(eventInfo, destLotData.getKey().getLotName(), factoryName, productSpecName, productSpecVersion, processFlowName,
						processFlowVersion, processOperationName, processOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion,
						lotSampleFlag, lotSampleCount, currentLotCount, totalLotCount, productSampleCount, productSamplePosition, String.valueOf(insertactualPositionList.size()),
						CommonUtil.toStringWithoutBrackets(insertactualPositionList), manualSampleFlag, "", Integer.parseInt(priority), returnProcessFlowName, returnProcessFlowVersion,
						returnProcessOperationName, returnProcessOperationVersion, forceSamplingFlag, completeFlowName, completeFlowVersion, completeOperationName, completeOperationVersion);
			}
			
			String actualProdCount = srcSampleLot.getActualProductCount();

			if (deleteSamplePositionList.size() == Integer.parseInt(actualProdCount))
			{
				log.info("SourceLot is Empty");
				ExtendedObjectProxy.getSampleLotService().deleteSampleLotWithOutMachineName(eventInfo, srcLotData.getKey().getLotName(), factoryName, productSpecName, productSpecVersion,
						processFlowName, processFlowVersion, processOperationName, processOperationVersion, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);
			}
			else
			{
				log.info("Update Source SampleLot");

				String srcActualPosition = srcSampleLot.getActualSamplePosition();

				List<String> afterActualPositionList = new ArrayList<String>();

				log.info("actualSamplePosition : " + srcActualPosition);
				if (srcActualPosition.lastIndexOf(",") > -1)
				{
					String[] actualPositionArray = StringUtils.split(srcActualPosition, ",");
					String oriSamplePosition = "";

					for (int i = 0; i < actualPositionArray.length; i++)
					{
						oriSamplePosition = actualPositionArray[i].trim();

						if (deleteSamplePositionList.size() > 0)
						{
							if (!deleteSamplePositionList.contains(oriSamplePosition))
							{
								afterActualPositionList.add(oriSamplePosition);
							}
						}
						else
						{
							afterActualPositionList.add(oriSamplePosition);
						}
					}
				}
				else
				{
					afterActualPositionList.add(srcActualPosition);
				}

				log.info("AfterActualPosition : " + afterActualPositionList);

				ExtendedObjectProxy.getSampleLotService().updateSampleLot(eventInfo, srcLotData.getKey().getLotName(), factoryName, productSpecName, productSpecVersion, processFlowName,
						processFlowVersion, processOperationName, processOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion,
						lotSampleFlag, lotSampleCount, currentLotCount, totalLotCount, productSampleCount, productSamplePosition, String.valueOf(afterActualPositionList.size()),
						CommonUtil.toStringWithoutBrackets(afterActualPositionList), manualSampleFlag, "", priority, returnProcessFlowName, returnProcessFlowVersion, returnProcessOperationName,
						returnProcessOperationVersion, forceSamplingFlag);
			}
		}
	}

	public void syncLotFutureActionDataExistCase(EventInfo eventInfo, LotFutureAction srcFutureData, LotFutureAction destFutureData) throws NumberFormatException, CustomException
	{
		String srcBeforeAction = srcFutureData.getBeforeAction();
		String srcBeforeActionComment = srcFutureData.getBeforeActionComment();
		String srcBeforeActionUser = srcFutureData.getBeforeActionUser();
		
		String srcAfterAction = srcFutureData.getAfterAction();
		String srcAfterActionComment = srcFutureData.getAfterActionComment();
		String srcAfterActionUser = srcFutureData.getAfterActionUser();
		
		String beforeAction = destFutureData.getBeforeAction();
		String beforeActionComment = destFutureData.getBeforeActionComment();
		String beforeActionUser = destFutureData.getBeforeActionUser();
		
		String afterAction = destFutureData.getAfterAction();
		String afterActionComment = destFutureData.getAfterActionComment();
		String afterActionUser = destFutureData.getAfterActionUser();
		
		boolean checkFlag = false;
		
		if (beforeAction != "True" && srcBeforeAction != beforeAction)
		{
			beforeAction = srcBeforeAction;
			beforeActionComment = srcBeforeActionComment;
			beforeActionUser = srcBeforeActionUser;
			
			checkFlag = true;
		}
		
		if (afterAction != "True" && srcAfterAction != afterAction)
		{
			afterAction = srcAfterAction;
			afterActionComment = srcAfterActionComment;
			afterActionUser = srcAfterActionUser;
			
			checkFlag = true;
		}

		if (checkFlag)
		{
			ExtendedObjectProxy.getLotFutureActionService().updateLotFutureAction(eventInfo, destFutureData.getLotName(), destFutureData.getFactoryName(), destFutureData.getProcessFlowName(),
					destFutureData.getProcessFlowVersion(), destFutureData.getProcessOperationName(), destFutureData.getProcessOperationVersion(),
					Integer.parseInt(destFutureData.getPosition().toString()), destFutureData.getReasonCode(), destFutureData.getReasonCodeType(), destFutureData.getActionName(),
					destFutureData.getActionType(), destFutureData.getAttribute1(), destFutureData.getAttribute2(), destFutureData.getAttribute3(), beforeAction, afterAction, beforeActionComment,
					afterActionComment, beforeActionUser, afterActionUser);
			
			Lot srcLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(srcFutureData.getLotName());

			if (StringUtils.equals(srcLotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Emptied))
			{
					ExtendedObjectProxy.getLotFutureActionService().remove(eventInfo, srcFutureData);
			}
		}
	}

	public void syncSamplePosition(EventInfo eventInfo, Lot lotData, List<Element> productList) throws CustomException
	{
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		List<SampleLot> sampleLotList = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListBySpec(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion());

		if (sampleLotList != null)
		{
			for (SampleLot sampleLot : sampleLotList)
			{
				String lotName = sampleLot.getLotName();
				String factoryName = sampleLot.getFactoryName();
				String productSpecName = sampleLot.getProductSpecName();
				String productSpecVersion = sampleLot.getProductSpecVersion();
				String processFlowName = sampleLot.getProcessFlowName();
				String processFlowVersion = sampleLot.getProcessFlowVersion();
				String processOperationName = sampleLot.getProcessOperationName();
				String processOperationVersion = sampleLot.getProcessOperationVersion();
				String machineName = sampleLot.getMachineName();
				String toProcessFlowName = sampleLot.getToProcessFlowName();
				String toProcessFlowVersion = sampleLot.getToProcessFlowVersion();
				String toProcessOperationName = sampleLot.getToProcessOperationName();
				String toProcessOperationVersion = sampleLot.getToProcessOperationVersion();

				List<String> actualSamplePosition = new ArrayList<String>();

				for (Element product : productList)
				{
					String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", true);
					String position = SMessageUtil.getChildText(product, "POSITION", true);

					List<SampleProduct> sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListByLotName(lotName, factoryName, productSpecName, productSpecVersion,
							processFlowName, processFlowVersion, processOperationName, processOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName,
							toProcessOperationVersion);

					if (sampleProductList != null)
					{
						for (SampleProduct sampleProduct : sampleProductList)
						{
							if (StringUtils.equals(productName, sampleProduct.getProductName()) && !StringUtils.equals(position, sampleProduct.getActualSamplePosition()))
							{
								ExtendedObjectProxy.getSampleProductService().updateSampleProduct(eventInfo, productName, lotName, factoryName, productSpecName, productSpecVersion, processFlowName,
										processFlowVersion, processOperationName, processOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName,
										toProcessOperationVersion, "", "", "", position, "", "");
							}

							actualSamplePosition.add(position);
						}
					}
				}

				if (actualSamplePosition.size() > 0)
				{
					actualSamplePosition = CommonUtil.sortActualSamplePosition(actualSamplePosition);

					ExtendedObjectProxy.getSampleLotService().updateSampleLot(eventInfo, lotName, factoryName, productSpecName, productSpecVersion, toProcessFlowName, toProcessFlowVersion,
							toProcessOperationName, toProcessOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion, "", "", "", "",
							"", "", String.valueOf(actualSamplePosition.size()), CommonUtil.toStringWithoutBrackets(actualSamplePosition), "", "", "", "", "", "", "", "");
				}
			}
		}
	}

	public boolean checkSampleData(List<String> srcLotList, List<String> destLotList) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		List<String> prodNameList = new ArrayList<String>();

		for (String destLotName : destLotList)
		{
			List<Product> productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(destLotName);

			for (Product product : productList)
			{
				prodNameList.add(product.getKey().getProductName());
			}
		}

		List<SampleProduct> sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataByLotAndProductList(srcLotList, prodNameList);

		if (sampleProductList != null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean checkFutureActionData(List<String> srcLotList) throws CustomException
	{
		List<LotFutureAction> lotFutureActionData = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataByLotList(srcLotList);

		if (lotFutureActionData != null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public List<Map<String, Object>> getGlassJudgeData(String sheetName) throws CustomException
	{
		StringBuffer sqlForSelectGlass = new StringBuffer();
		sqlForSelectGlass.append("SELECT SCRAPFLAG, GLASSNAME, GLASSJUDGE, SHEETNAME, PROCESSFLOWNAME, PROCESSFLOWVERSION, PROCESSOPERATIONNAME ");
		sqlForSelectGlass.append("  FROM CT_GLASSJUDGE ");
		sqlForSelectGlass.append(" WHERE SHEETNAME = :SHEETNAME ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("SHEETNAME", sheetName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlForSelectGlassResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlForSelectGlass.toString(), bindMap);

		return sqlForSelectGlassResult;
	}

	public void updateGlassJudgeScrapFlag(EventInfo eventInfo, Map<String, Object> result, String scrapFlag) throws CustomException
	{
		StringBuffer sqlForUpdateScrapFlag = new StringBuffer();
		sqlForUpdateScrapFlag.append("UPDATE CT_GLASSJUDGE ");
		sqlForUpdateScrapFlag.append("   SET SCRAPFLAG = :SCRAPFLAG ");
		sqlForUpdateScrapFlag.append(" WHERE GLASSNAME = :GLASSNAME ");

		Map<String, Object> bindMap1 = new HashMap<String, Object>();
		bindMap1.put("SCRAPFLAG", scrapFlag);
		bindMap1.put("GLASSNAME", result.get("GLASSNAME"));

		GenericServiceProxy.getSqlMesTemplate().update(sqlForUpdateScrapFlag.toString(), bindMap1);

		StringBuffer sqlForInsertGlassJudgeHist = new StringBuffer();
		sqlForInsertGlassJudgeHist.append("INSERT INTO CT_GLASSJUDGEHISTORY ");
		sqlForInsertGlassJudgeHist.append("  (GLASSNAME, TIMEKEY, GLASSJUDGE, SHEETNAME, PROCESSFLOWNAME, PROCESSFLOWVERSION, ");
		sqlForInsertGlassJudgeHist.append("   EVENTNAME, EVENTUSER, EVENTTIME, EVENTCOMMENT, PROCESSOPERATIONNAME,SCRAPFLAG) ");
		sqlForInsertGlassJudgeHist.append("VALUES ");
		sqlForInsertGlassJudgeHist.append("  (:GLASSNAME, :TIMEKEY, :GLASSJUDGE, :SHEETNAME, :PROCESSFLOWNAME, :PROCESSFLOWVERSION, ");
		sqlForInsertGlassJudgeHist.append("   :EVENTNAME, :EVENTUSER , :EVENTTIME, :EVENTCOMMENT, :PROCESSOPERATIONNAME,:SCRAPFLAG ) ");

		Map<String, Object> bindMap11 = new HashMap<String, Object>();
		bindMap11.put("GLASSNAME", result.get("GLASSNAME"));
		bindMap11.put("GLASSJUDGE", result.get("GLASSJUDGE"));
		bindMap11.put("SHEETNAME", result.get("SHEETNAME"));
		bindMap11.put("PROCESSFLOWNAME", result.get("PROCESSFLOWNAME"));
		bindMap11.put("PROCESSFLOWVERSION", result.get("PROCESSFLOWVERSION"));
		bindMap11.put("EVENTNAME", eventInfo.getEventName());
		bindMap11.put("EVENTUSER", eventInfo.getEventUser());
		bindMap11.put("EVENTTIME", eventInfo.getEventTime());
		bindMap11.put("EVENTCOMMENT", eventInfo.getEventComment());
		bindMap11.put("PROCESSOPERATIONNAME", result.get("PROCESSOPERATIONNAME"));
		bindMap11.put("TIMEKEY", TimeStampUtil.getCurrentEventTimeKey());
		bindMap11.put("SCRAPFLAG", scrapFlag);

		GenericServiceProxy.getSqlMesTemplate().update(sqlForInsertGlassJudgeHist.toString(), bindMap11);
	}
	
	public boolean judgeFirstGlassLot (Lot lotData, boolean flag) throws CustomException
	{
		try
		{
			if (StringUtils.isNotEmpty(lotData.getUdfs().get("JOBNAME")))
				flag = true;
		}
		catch (Exception e)
		{}
		
		return flag;
	}
	
	@SuppressWarnings("unchecked")
	public Lot getLotDataForBuffer(Durable durableData, String machineName, String portName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		Lot lotData = new Lot();
		String carrierName = durableData.getKey().getDurableName();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT J.JOBNAME, J.JOBSTATE, C.TRANSFERDIRECTION, C.CARRIERNAME, C.LOTNAME ");
		sql.append("  FROM CT_SORTJOB J, CT_SORTJOBCARRIER C, DURABLE D ");
		sql.append(" WHERE J.JOBNAME = C.JOBNAME ");
		sql.append("   AND J.JOBNAME IN (SELECT J.JOBNAME ");
		sql.append("                       FROM CT_SORTJOB J, CT_SORTJOBCARRIER C ");
		sql.append("                      WHERE J.JOBNAME = C.JOBNAME ");
		sql.append("                        AND C.CARRIERNAME = :CARRIERNAME ");
		sql.append("                        AND C.MACHINENAME = :MACHINENAME ");
		sql.append("                        AND C.PORTNAME = :PORTNAME ");
		sql.append("                        AND J.JOBSTATE = 'STARTED' ");
		sql.append("                        AND ROWNUM = 1) ");
		sql.append("   AND C.CARRIERNAME = D.DURABLENAME ");
		sql.append("   AND D.DURABLETYPE = 'BufferCST' ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("CARRIERNAME", carrierName);
		args.put("MACHINENAME", machineName);
		args.put("PORTNAME", portName);

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			String jobName = ConvertUtil.getMapValueByName(result.get(0), "JOBNAME");
			log.info("BufferCST Transfer. JobID: " + jobName);

			if (StringUtils.equals(durableData.getDurableType(), GenericServiceProxy.getConstantMap().CST_TYPE_BUFFER))
			{
				sql.setLength(0);
				sql.append("SELECT C.LOTNAME, J.JOBNAME, C.MACHINENAME, C.PORTNAME ");
				sql.append("  FROM CT_SORTJOB J, CT_SORTJOBCARRIER C ");
				sql.append(" WHERE J.JOBNAME = C.JOBNAME ");
				sql.append("   AND J.JOBSTATE = 'STARTED' ");
				sql.append("   AND J.JOBNAME = :JOBNAME ");
				sql.append("   AND C.CARRIERNAME = :CARRIERNAME ");

				args.put("JOBNAME", jobName);

				result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

				if (result.size() > 0)
				{
					String lotName = ConvertUtil.getMapValueByName(result.get(0), "LOTNAME");

					if (StringUtils.isNotEmpty(lotName))
					{
						lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
					}
					else
					{
						lotData = new Lot();
					}
				}
			}
			else
			{
				lotData = MESLotServiceProxy.getLotInfoUtil().getLotInfoBydurableName(carrierName);
			}
		}
		else
		{
			lotData = MESLotServiceProxy.getLotInfoUtil().getLotInfoBydurableName(carrierName);
		}

		return lotData;
	}
	
	public String getLotNameForSorter(Lot lotData, Durable durableData, String lotName, String machineName, String portName) throws CustomException
	{
		try
		{
			lotData = MESLotServiceProxy.getLotServiceUtil().getLotDataForBuffer(durableData, machineName, portName);
			
			if (lotData != null && lotData.getKey() != null)
			{
				lotName = lotData.getKey().getLotName();
			}
			else
			{
				lotName = "";
			}
		}
		catch (Exception e)
		{
		}
		
		return lotName;
	}
	
	public Lot checkProcessGlass(EventInfo eventInfo, Document doc, Lot oriLotData, Lot tklotData, Durable durableData, String oriSlotSel, String machineName, String machineRecipe,
			Machine machineData, ProcessOperationSpec operationSpecData, String oldMqcSpecName, boolean forceFlag) throws CustomException
	{
		boolean FHoldFlag = true;
		String slotSelHoldComment = "";
		String recipeHoldComment  = "";
		List<String> wJudgePositionList = new ArrayList<String>();
		List<String> fProcessinfoPositionList = new ArrayList<String>();
		
		MachineSpec machineDataSpec = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);

		String sql = "SELECT ENUMVALUE FROM ENUMDEFVALUE WHERE ENUMNAME = :ENUMNAME AND ENUMVALUE = :ENUMVALUE";
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("ENUMNAME", "ProcessingInfoFMachineGroupNotHold");
		bindMap.put("ENUMVALUE", machineDataSpec.getMachineGroupName() + machineDataSpec.getFactoryName());
		
		@SuppressWarnings("unchecked")
		List<Map<String, String>> groupResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		if (groupResult.size() > 0)
		{
			FHoldFlag = false;
		}

		if ((StringUtils.equalsIgnoreCase(machineData.getCommunicationState(), GenericServiceProxy.getConstantMap().Mac_OnLineLocal) && StringUtils.equals(operationSpecData.getProcessOperationType(),"Production"))
				|| (StringUtils.equalsIgnoreCase(machineData.getCommunicationState(), GenericServiceProxy.getConstantMap().Mac_OnLineRemote) && StringUtils.equals(operationSpecData.getProcessOperationType(), "Production")))
		{
			if (!StringUtils.equals(operationSpecData.getDetailProcessOperationType(), "SORT")
					&& !StringUtils.equals(operationSpecData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().SORT_OLEDtoTP)
					&& !StringUtils.equals(operationSpecData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().SORT_TPtoOLED))
			{
				// not Sorter EQ == normal EQ & Production Operation
				StringBuffer slotMapTemp = new StringBuffer();
				StringBuffer processingInfoMapTemp = new StringBuffer();
				StringBuffer oriSlotselTemp = new StringBuffer();

				for (long i = 0; i < durableData.getCapacity(); i++)
				{
					slotMapTemp.append(GenericServiceProxy.getConstantMap().PRODUCT_NOT_IN_SLOT);
					processingInfoMapTemp.append(GenericServiceProxy.getConstantMap().PRODUCT_NOT_IN_SLOT);
					oriSlotselTemp.append(GenericServiceProxy.getConstantMap().PRODUCT_NOT_IN_SLOT);
				}

				List<Element> lstDownloadProduct = SMessageUtil.getSubSequenceItemList(SMessageUtil.getBodyElement(doc), "PRODUCTLIST", false);

				for (Element eleProduct : lstDownloadProduct)
				{
					String sPosition = SMessageUtil.getChildText(eleProduct, "POSITION", true);
					String sSlotPosition = SMessageUtil.getChildText(eleProduct, "SLOTPOSITION", false);
					String sProcessingInfo = SMessageUtil.getChildText(eleProduct, "PROCESSINGINFO", false);
					String sProductJudge = SMessageUtil.getChildText(eleProduct, "PRODUCTJUDGE", false);
					String samplingFlag = SMessageUtil.getChildText(eleProduct, "SAMPLINGFLAG", false);

					if (StringUtils.equals(sProductJudge, "W"))
					{
						wJudgePositionList.add(sPosition);
					}
					if (StringUtils.equals(sProcessingInfo, "F"))
					{
						fProcessinfoPositionList.add(sPosition);
					}

					int position;

					try
					{
						position = Integer.parseInt(sPosition);
					}
					catch (Exception ex)
					{
						position = 0;
					}

					if (durableData.getCapacity() > 30 && StringUtils.isNotEmpty(sSlotPosition))
					{
						if (StringUtils.equals(sSlotPosition, "A"))
						{
							position = (Integer.parseInt(sPosition) * 2) - 1;
						}
						else if (StringUtils.equals(sSlotPosition, "B"))
						{
							position = (Integer.parseInt(sPosition) * 2);
						}
					}

					if (sProcessingInfo.equalsIgnoreCase("N") || sProcessingInfo.equalsIgnoreCase("L") || sProcessingInfo.equalsIgnoreCase("F"))
					{
						slotMapTemp.replace(position - 1, position, GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT);
						processingInfoMapTemp.replace(position - 1, position, sProcessingInfo);
					}
					else
					{
						slotMapTemp.replace(position - 1, position, GenericServiceProxy.getConstantMap().PRODUCT_NOT_IN_SLOT);
						if (StringUtils.isEmpty(sProcessingInfo))
							sProcessingInfo = "X";

						processingInfoMapTemp.replace(position - 1, position, sProcessingInfo);
					}

					if (samplingFlag.equalsIgnoreCase("Y"))
					{
						oriSlotselTemp.replace(position - 1, position, GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT);
					}

				}

				if (!(oriSlotselTemp.toString()).equals(slotMapTemp.toString()))
				{
					// lotHoldFlag = true;
					slotSelHoldComment = " (oriSlotSel = '" + oriSlotselTemp + "' , processSlotSel = '" + slotMapTemp + "' , processingInfo = '" + processingInfoMapTemp + "') ";
					String oriEventComment = eventInfo.getEventComment();
					String holdEventComment = "Start Lot Hold" + slotSelHoldComment;
					log.info(holdEventComment);

					// Set ReasonCode
					eventInfo.setReasonCodeType("HOLD");
					eventInfo.setReasonCode("SYSTEM");
					eventInfo.setEventComment(holdEventComment);

					// LotMultiHold
					MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, tklotData, new HashMap<String, String>());
					tklotData = MESLotServiceProxy.getLotServiceUtil().getLotData(tklotData.getKey().getLotName());

					eventInfo.setEventComment(oriEventComment);
				}
				// MES Recipe
				ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(oriLotData);
				
				// MES Recipe
				String productSpecName = "";
				if (StringUtils.isNotEmpty(oldMqcSpecName))
				{
					productSpecName = oldMqcSpecName;
				}
				else
				{
					productSpecName = oriLotData.getProductSpecName();
				}
				
				String oriMachineRecipe = this.getMachineRecipe(oriLotData.getFactoryName(), productSpecName, oriLotData.getProductSpecVersion(), oriLotData.getProcessFlowName(),
						oriLotData.getProcessFlowVersion(), oriLotData.getProcessOperationName(), oriLotData.getProcessOperationVersion(), machineName, false);
				// Check Recipe
				if (!oriMachineRecipe.equals(machineRecipe) && (!StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Sorter)))
				{
					// lotHoldFlag = true;
					recipeHoldComment = " (MachineRecipe = '" + machineRecipe + "', MESRecipe = '" + oriMachineRecipe + "') ";
					String oriEventComment = eventInfo.getEventComment();
					String holdEventComment = "Start Lot Hold" + recipeHoldComment;
					log.info(holdEventComment);

					// Set ReasonCode
					eventInfo.setReasonCodeType("HOLD");
					eventInfo.setReasonCode("SYSTEM1");
					eventInfo.setEventComment(holdEventComment);

					// LotMultiHold
					MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, tklotData, new HashMap<String, String>());
					tklotData = MESLotServiceProxy.getLotServiceUtil().getLotData(tklotData.getKey().getLotName());

					eventInfo.setEventComment(oriEventComment);
				}

				if (StringUtils.isEmpty(machineRecipe) && (!StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Sorter)))
				{
					// lotHoldFlag = true;
					recipeHoldComment = " (MachineRecipe = '" + machineRecipe + "', MESRecipe = '" + oriMachineRecipe + "') ";
					String oriEventComment = eventInfo.getEventComment();
					String holdEventComment = "Start Lot Hold" + recipeHoldComment;
					log.info(holdEventComment);

					// Set ReasonCode
					eventInfo.setReasonCodeType("HOLD");
					eventInfo.setReasonCode("SYSTEM2");
					eventInfo.setEventComment(holdEventComment);

					// LotMultiHold
					MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, tklotData, new HashMap<String, String>());
					tklotData = MESLotServiceProxy.getLotServiceUtil().getLotData(tklotData.getKey().getLotName());

					eventInfo.setEventComment(oriEventComment);
				}

				// Productjudge has W TrackOutLot Hold
				if (wJudgePositionList.size() > 0)
				{
					String oriEventComment = eventInfo.getEventComment();
					String holdEventComment = " (Slot = '" + wJudgePositionList + "', Productjudge is W) ";
					log.info(holdEventComment);

					// Set ReasonCode
					eventInfo.setReasonCodeType("HOLD");
					eventInfo.setReasonCode("SYSTEM3");
					eventInfo.setEventComment(holdEventComment);

					// LotMultiHold
					MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, tklotData, new HashMap<String, String>());
					tklotData = MESLotServiceProxy.getLotServiceUtil().getLotData(tklotData.getKey().getLotName());

					eventInfo.setEventComment(oriEventComment);
				}

				// ProcessInfo has F TrackOutLot Hold
				if (fProcessinfoPositionList.size() > 0 && FHoldFlag)
				{

					String oriEventComment = eventInfo.getEventComment();
					String holdEventComment = " (Slot = '" + fProcessinfoPositionList + "', ProcessInfo Is F) ";
					log.info(holdEventComment);

					// Set ReasonCode
					eventInfo.setReasonCodeType("HOLD");
					eventInfo.setReasonCode("SYSTEM4");
					eventInfo.setEventComment(holdEventComment);

					// LotMultiHold
					MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, tklotData, new HashMap<String, String>());
					tklotData = MESLotServiceProxy.getLotServiceUtil().getLotData(tklotData.getKey().getLotName());

					eventInfo.setEventComment(oriEventComment);
				}
			}
			else
			{ // Sort EQ read VCR mismatch
				String sProductionType = oriLotData.getProductionType();
				if (sProductionType.equalsIgnoreCase("E") || sProductionType.equalsIgnoreCase("P"))
				{
					List<Element> listOfProcessEndProductList = SMessageUtil.getSubSequenceItemList(SMessageUtil.getBodyElement(doc), "PRODUCTLIST", false);

					for (Element eleProduct : listOfProcessEndProductList)
					{
						String sMESProductName = SMessageUtil.getChildText(eleProduct, "PRODUCTNAME", true);
						String sVCRProductName = SMessageUtil.getChildText(eleProduct, "VCRPRODUCTNAME", false);

						if (StringUtils.isEmpty(sVCRProductName) || StringUtils.equalsIgnoreCase(sMESProductName, sVCRProductName))
						{
							continue;
						}
						else
						{
							log.info("Mismatched VCRProductName by EQP " + sVCRProductName + ", ProductName is " + sMESProductName);
							eventInfo.setReasonCodeType("HOLD");
							eventInfo.setReasonCode("SYSTEM5");
							eventInfo.setEventComment("Mismatched VCRProductName by EQP " + sVCRProductName + ", ProductName is " + sMESProductName);

							// LotMultiHold
							MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, tklotData, new HashMap<String, String>());
							tklotData = MESLotServiceProxy.getLotServiceUtil().getLotData(tklotData.getKey().getLotName());

							break;
						}
					}
				}
			}

		}
		else if((StringUtils.equalsIgnoreCase(machineData.getCommunicationState(), GenericServiceProxy.getConstantMap().Mac_OnLineLocal) && StringUtils.equals(operationSpecData.getProcessOperationType(),"Inspection"))
				|| (StringUtils.equalsIgnoreCase(machineData.getCommunicationState(), GenericServiceProxy.getConstantMap().Mac_OnLineRemote) && StringUtils.equals(operationSpecData.getProcessOperationType(), "Inspection")))
		{
			List<Element> lstDownloadProduct = SMessageUtil.getSubSequenceItemList(SMessageUtil.getBodyElement(doc), "PRODUCTLIST", false);
			
			for (Element eleProduct : lstDownloadProduct)
			{
				String sPosition = SMessageUtil.getChildText(eleProduct, "POSITION", true);
				String sProcessingInfo = SMessageUtil.getChildText(eleProduct, "PROCESSINGINFO", false);
				
				if (StringUtils.equals(sProcessingInfo, "F"))
				{
					fProcessinfoPositionList.add(sPosition);
				}
			}
			
			// ProcessInfo has F TrackOutLot Hold
			if (fProcessinfoPositionList.size() > 0 && FHoldFlag)
			{
				String oriEventComment = eventInfo.getEventComment();
				String holdEventComment = " (Slot = '" + fProcessinfoPositionList + "', ProcessInfo Is F) ";
				log.info(holdEventComment);

				// Set ReasonCode
				eventInfo.setReasonCodeType("HOLD");
				eventInfo.setReasonCode("SYSTEM4");
				eventInfo.setEventComment(holdEventComment);

				// LotMultiHold
				MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, tklotData, new HashMap<String, String>());
				tklotData = MESLotServiceProxy.getLotServiceUtil().getLotData(tklotData.getKey().getLotName());

				eventInfo.setEventComment(oriEventComment);
			}
		}

		if (operationSpecData.getKey().getProcessOperationName().equals("21220") && !forceFlag)
		{
			List<Element> lstDownloadProduct = SMessageUtil.getSubSequenceItemList(SMessageUtil.getBodyElement(doc), "PRODUCTLIST", false);
			List<String> nJudgePositionList = new ArrayList<String>();
			List<String> nJudgeProductList = new ArrayList<String>();

			for (Element eleProduct : lstDownloadProduct)
			{
				String sPosition = SMessageUtil.getChildText(eleProduct, "POSITION", true);
				String sProductJudge = SMessageUtil.getChildText(eleProduct, "PRODUCTJUDGE", false);
				String sProductName = SMessageUtil.getChildText(eleProduct, "PRODUCTNAME", false);

				if (StringUtils.equals(sProductJudge, "N"))
				{
					nJudgePositionList.add(sPosition);
					nJudgeProductList.add(sProductName);
				}
			}
			if (nJudgePositionList.size() > 0)
			{
				String oriEventComment = eventInfo.getEventComment();
				String holdEventComment = " TFE AOI (Slot = '" + nJudgePositionList + "', Judge Is N) ";
				String beforeAction = "False";
				String afterAction = "True";
				String actionType = "Insert";
				log.info(holdEventComment);

				// Set ReasonCode
				eventInfo.setReasonCodeType("HOLD");
				eventInfo.setReasonCode("HL-ENG");
				eventInfo.setEventComment(holdEventComment);

				MESLotServiceProxy.getLotServiceImpl().insertCtLotFutureMultiHoldActionForAfter(eventInfo, tklotData.getKey().getLotName(), tklotData.getFactoryName(), tklotData.getProcessFlowName(),
						"00001", tklotData.getProcessOperationName(), "00001", "0", "hold", "System", "HOLD", "HL-ENG", "", "", "", beforeAction, afterAction, "", holdEventComment, "",
						eventInfo.getEventUser(), actionType, "", "");

				tklotData = MESLotServiceProxy.getLotServiceUtil().getLotData(tklotData.getKey().getLotName());

				eventInfo.setEventComment(oriEventComment);
			}
		}

		return tklotData;
	}
	
	public String getMachineRecipe(String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, String machineName, boolean isVerified) throws CustomException
	{
		ListOrderedMap instruction = PolicyUtil.getMachineRecipeName(factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName,
				processOperationVersion, machineName);

		String designatedRecipeName = CommonUtil.getValue(instruction, "MACHINERECIPENAME");

		return designatedRecipeName;
	}
	
	public boolean checkCompleteReworkFlag(List<Element> productElementList) throws CustomException
	{
		boolean completeReworkFlag = true;

		for (Element productElement : productElementList)
		{
			String grade = SMessageUtil.getChildText(productElement, "PRODUCTJUDGE", false);

			if (StringUtils.equals(grade, "R"))
			{
				completeReworkFlag = false;
			}
		}

		return completeReworkFlag;
	}
	
	public String getLotInfoBydurableNameForFisrtGlass(String carrierName) throws CustomException
	{
		List<Map<String, Object>> lotList;
		String lotName = "";

		String sql = "SELECT LOTNAME FROM LOT WHERE CARRIERNAME = :CARRIERNAME AND LOTSTATE = :LOTSTATE AND FIRSTGLASSFLAG = 'N' AND JOBNAME IS NOT NULL";

		Map<String, String> args = new HashMap<String, String>();
		args.put("CARRIERNAME", carrierName);
		args.put("LOTSTATE", GenericServiceProxy.getConstantMap().Lot_Released);

		lotList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

		if (lotList.size() > 0 && lotList != null)
			lotName = lotList.get(0).get("LOTNAME").toString();
		else
		{
			sql = "SELECT LOTNAME FROM LOT WHERE CARRIERNAME = :CARRIERNAME AND LOTSTATE = :LOTSTATE AND FIRSTGLASSFLAG IS NULL AND JOBNAME IS NOT NULL";

			lotList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

			if (lotList.size() > 0 && lotList != null)
				lotName = lotList.get(0).get("LOTNAME").toString();
			else
			{
				sql = "SELECT LOTNAME FROM LOT WHERE CARRIERNAME = :CARRIERNAME AND LOTSTATE = :LOTSTATE ";
				lotList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

				if (lotList.size() > 0 && lotList != null)
					lotName = lotList.get(0).get("LOTNAME").toString();
			}
		}

		return lotName;
	}
	
	public List<Product> getProductListByCST(String carrierName)
	{
		List<Product> productList = new ArrayList<Product>();
		try
		{
			productList = ProductServiceProxy.getProductService().select(" WHERE carrierName = ? AND productState != ? AND productState != ? ORDER BY position ", new Object[] { carrierName, GenericServiceProxy.getConstantMap().Prod_Scrapped, GenericServiceProxy.getConstantMap().Prod_Consumed });
		}
		catch (Exception e)
		{

		}
		return productList;
	}
	
	public void checkLotStatus(List<String> productList) throws CustomException
	{
		if (productList.size() > 0)
		{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT DISTINCT L.LOTNAME, L.LOTSTATE, L.LOTPROCESSSTATE, L.LOTHOLDSTATE ");
			sql.append("  FROM LOT L, PRODUCT P ");
			sql.append(" WHERE L.LOTNAME = P.LOTNAME ");
			sql.append("   AND P.PRODUCTNAME IN ( :PRODUCTLIST ) ");
			sql.append("   AND ( (L.LOTSTATE != 'Released' OR L.LOTSTATE IS NULL) ");
			sql.append("      OR (L.LOTPROCESSSTATE != 'RUN' OR L.LOTPROCESSSTATE IS NULL) ");
			sql.append("      OR (L.LOTHOLDSTATE != 'N' OR L.LOTHOLDSTATE IS NULL) ) ");
			sql.append("ORDER BY L.LOTNAME, L.LOTPROCESSSTATE ");

			Map<String, Object> args = new HashMap<String, Object>();
			args.put("PRODUCTLIST", productList);

			List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (result.size() > 0)
			{
				String lotName = ConvertUtil.getMapValueByName(result.get(0), "LOTNAME");
				String lotState = ConvertUtil.getMapValueByName(result.get(0), "LOTSTATE");
				String lotProcessState = ConvertUtil.getMapValueByName(result.get(0), "LOTPROCESSSTATE");
				String lotHoldState = ConvertUtil.getMapValueByName(result.get(0), "LOTHOLDSTATE");

				if (!StringUtils.equals(lotState, GenericServiceProxy.getConstantMap().Lot_Released))
				{
					throw new CustomException("LOT-0016", lotName, lotState);
				}
				else if (!StringUtils.equals(lotProcessState, GenericServiceProxy.getConstantMap().Lot_Run))
				{
					throw new CustomException("LOT-0044", lotName);
				}
				else if (!StringUtils.equals(lotHoldState, GenericServiceProxy.getConstantMap().Lot_NotOnHold))
				{
					throw new CustomException("LOT-9015", lotName, lotHoldState);
				}
				else
				{
					throw new CustomException("LOT-0150", lotName, lotState, lotProcessState, lotHoldState);
				}
			}
		}
	}
	
	public List<ProductPGSRC> setProductPGSRCSequenceForFirstGlass(Element bodyElement, String currentLotName, boolean isInReworkFlow, EventInfo eventInfo) throws CustomException
	{
		String machineName = SMessageUtil.getChildText(bodyElement, "MACHINENAME", true);
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		
		boolean isSorter = false;
		if (CommonUtil.equalsIn(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Sorter, GenericServiceProxy.getConstantMap().MachineGroup_CellSorter) ||
				GenericServiceProxy.getConstantMap().SORT_OPERATIONMODE.equals(machineData.getUdfs().get("OPERATIONMODE")))
		{
			isSorter = true;
		}
		
		List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();
		List<Element> productElementList = SMessageUtil.getSubSequenceItemList(bodyElement, "PRODUCTLIST", true);

		Lot baseLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(currentLotName);
		ProcessOperationSpec operationSpecData = CommonUtil.getProcessOperationSpec(baseLotData.getFactoryName(), baseLotData.getProcessOperationName(), baseLotData.getProcessOperationVersion());

		for (Element productElement : productElementList)
		{
			String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

			if (StringUtils.equals(productData.getLotName(), currentLotName))
			{
				ProductPGSRC productPGSRC = new ProductPGSRC();
				productPGSRC.setProductName(productName);

				String position = SMessageUtil.getChildText(productElement, "POSITION", true);
				productPGSRC.setPosition(Long.valueOf(position));

				String productGrade = productData.getProductGrade();
				String subProductGrade = SMessageUtil.getChildText(productElement, "SUBPRODUCTJUDGES", false);
				String slotPosition = SMessageUtil.getChildText(productElement, "SLOTPOSITION", false);

				productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
				productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());

				productPGSRC.setReworkFlag("N");

				// Consumable ignored
				productPGSRC.setConsumedMaterialSequence(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());

				String processingInfo = SMessageUtil.getChildText(productElement, "PROCESSINGINFO", false);

				if (isInReworkFlow && StringUtils.equals(productGrade, GenericServiceProxy.getConstantMap().ProductGrade_G))
				{
					// Do not change ProductGrade when EQP reports G grade in Rework Flow
					productGrade = productData.getProductGrade();
				}
				else
				{
					////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					//Mantis 0000441
					if (operationSpecData.getDetailProcessOperationType().equals("AT"))
					{
						if (!StringUtils.equals(processingInfo, "B"))
						{
							//productGrade = CommonUtil.judgeProductGradeBySubProductGrade(subProductGrade);
							//productPGSRC.setSubProductGrades1(subProductGrade);

							String subProductGradesMachine = subProductGrade;
							String subProductGradesMES = productData.getSubProductGrades1();

							if (!(subProductGradesMES.isEmpty() || subProductGradesMES.equals("")) && !(subProductGradesMachine.isEmpty() || subProductGradesMachine.equals(""))
									&& subProductGradesMES.length() == subProductGradesMachine.length())
							{
								List <String> gradeResult = MESLotServiceProxy.getLotInfoUtil().makeSubProductGradeList(subProductGradesMES, subProductGradesMachine, productData.getProductType(), productData.getFactoryName(), productName, eventInfo, productData.getProcessOperationName(), productData.getProcessFlowName());

								productPGSRC.setSubProductGrades1(gradeResult.get(0));
								productGrade = gradeResult.get(1);
							}
							else
							{
								log.info("EQ report SUBPRODUCTJUDGES error.");
								productPGSRC.setSubProductGrades1(productData.getSubProductGrades1());
							}
						}
					}
					else if(StringUtil.equals(operationSpecData.getKey().getProcessOperationName(), "21220") && StringUtil.equals(GenericServiceProxy.getConstantMap().MachineGroup_AOI, machineData.getMachineGroupName()))
					{
						if (!StringUtils.equals(processingInfo, "B"))
						{
							String subProductGradesMachine = SMessageUtil.getChildText(productElement, "SUBPRODUCTJUDGES", true);
							productPGSRC.setSubProductGrades1(subProductGradesMachine);
						}
					}
					else if(StringUtil.equals(operationSpecData.getDetailProcessOperationType(), "RP"))
					{
						if (!StringUtils.equals(processingInfo, "B"))
						{
							String subProductGradesMachine = SMessageUtil.getChildText(productElement, "SUBPRODUCTJUDGES", true);
							String gradesList = SMessageUtil.getChildText(productElement, "SUBPRODUCTJUDGES", false);
							String subProductGradesMES = productData.getSubProductGrades1();
							
							if (!(subProductGradesMES.isEmpty() || subProductGradesMES.equals("")) && !(subProductGradesMachine.isEmpty() || subProductGradesMachine.equals(""))
									&& subProductGradesMES.length() == subProductGradesMachine.length())
							{
								List <String> gradeResult = MESLotServiceProxy.getLotInfoUtil().makeSubProductGradeListForRepair(subProductGradesMES, subProductGradesMachine, productData.getFactoryName(), eventInfo, machineName,gradesList,productData);
								
								productPGSRC.setSubProductGrades1(gradeResult.get(0));
								productGrade = gradeResult.get(1);
							}
							else
							{
								log.info("EQ report SUBPRODUCTJUDGES error.");
								productPGSRC.setSubProductGrades1(productData.getSubProductGrades1());
							}
						}
					}
					////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				}

				productPGSRC.setProductGrade(productGrade);

				String productRecipe = SMessageUtil.getChildText(productElement, "PRODUCTRECIPE", false);

				productPGSRC.getUdfs().put("PRODUCTRECIPE", productRecipe);
				productPGSRC.getUdfs().put("SLOTPOSITION", slotPosition);
				
				String turnFlag = SMessageUtil.getChildText(productElement, "TURNFLAG", false);
				if(isSorter)
				{
					int turnCount=0;
	                if(StringUtils.isNotEmpty(productData.getUdfs().get("TURNCOUNT")))
	                {
	                	turnCount= Integer.parseInt(productData.getUdfs().get("TURNCOUNT"));
	                }
					if(StringUtils.equals(turnFlag, "Y"))
					{
		                turnCount+=1;
					}
	                productPGSRC.getUdfs().put("TURNCOUNT", Integer.toString(turnCount));
				}

				// 2020-11-17	dhko	0000406	
				// Sorter设备上报的LotProcsessEnd/LotProcessAbort消息都不再继承Sorter设备的ProcessingInfo
				if (!isSorter)
				{
					productPGSRC.getUdfs().put("PROCESSINGINFO", processingInfo);
				}

				if(StringUtils.isEmpty(processingInfo))
				{
					processingInfo = productData.getUdfs().get("PROCESSINGINFO");
				}
				
				//For LastMainOperation Check
				log.info("LastMainOperationName before set : " + productData.getUdfs().get("LASTMAINOPERNAME"));
				ProcessFlow currentFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(productData);
				if (StringUtils.equals(currentFlowData.getProcessFlowType(), "Main") && !CommonUtil.equalsIn(processingInfo, "B", "S"))
				{
					productPGSRC.getUdfs().put("LASTMAINFLOWNAME", productData.getProcessFlowName());
					productPGSRC.getUdfs().put("LASTMAINOPERNAME", productData.getProcessOperationName());
				}
				log.info("LastMainOperationName after set : " + productPGSRC.getUdfs().get("LASTMAINOPERNAME"));

				productPGSRCSequence.add(productPGSRC);
			}
		}

		return productPGSRCSequence;
	}

	public List<ProductPGSRC> setProductPGSRCSequenceForRepair(Element bodyElement, boolean isInReworkFlow, EventInfo eventInfo) throws CustomException
	{
		String machineName = SMessageUtil.getChildText(bodyElement, "MACHINENAME", true);
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		
		boolean isSorter = false;
		if (CommonUtil.equalsIn(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Sorter, GenericServiceProxy.getConstantMap().MachineGroup_CellSorter) ||
				GenericServiceProxy.getConstantMap().SORT_OPERATIONMODE.equals(machineData.getUdfs().get("OPERATIONMODE")))
		{
			isSorter = true;
		}
		
		List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();
		List<Element> productElementList = SMessageUtil.getSubSequenceItemList(bodyElement, "PRODUCTLIST", true);

		for (Element productElement : productElementList)
		{
			String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

			ProductPGSRC productPGSRC = new ProductPGSRC();
			productPGSRC.setProductName(productName);

			String position = SMessageUtil.getChildText(productElement, "POSITION", true);
			productPGSRC.setPosition(Long.valueOf(position));
			//String productGrade = SMessageUtil.getChildText(productElement, "PRODUCTJUDGE", false);
			String productGrade = productData.getProductGrade();
			//String receiveSubProductGrade = SMessageUtil.getChildText(productElement, "SUBPRODUCTJUDGES", false);

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			String subProductGradesMachine = SMessageUtil.getChildText(productElement, "SUBPRODUCTJUDGES", true);
			String gradesList = SMessageUtil.getChildText(productElement, "SUBPRODUCTGRADES", false);
			String subProductGradesMES = productData.getSubProductGrades1();
			String finalSubProductGrade = "";
			
			if (!(subProductGradesMES.isEmpty() || subProductGradesMES.equals("")) && !(subProductGradesMachine.isEmpty() || subProductGradesMachine.equals(""))
					&& subProductGradesMES.length() == subProductGradesMachine.length())
			{
				List <String> gradeResult = MESLotServiceProxy.getLotInfoUtil().makeSubProductGradeListForRepair(subProductGradesMES, subProductGradesMachine, productData.getFactoryName(), eventInfo, machineName,gradesList,productData);
				
				finalSubProductGrade = gradeResult.get(0);
				productGrade = gradeResult.get(1);
			}
			else
			{
				log.info("EQ report SUBPRODUCTJUDGES error.");
				finalSubProductGrade = productData.getSubProductGrades1();
			}
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
			/*
			String beforeSubProductGrade = productData.getSubProductGrades1();
			String finalSubProductGrade = "";

			if (!(beforeSubProductGrade.isEmpty() || beforeSubProductGrade.equals("")) && !(receiveSubProductGrade.isEmpty() || receiveSubProductGrade.equals(""))
					&& beforeSubProductGrade.length() == receiveSubProductGrade.length())
			{
				for (int i = 0; i < productData.getSubProductQuantity1(); i++)
				{
					// Repair : P<G<N<S
					if (beforeSubProductGrade.substring(i, i + 1).equalsIgnoreCase("P"))
					{
						finalSubProductGrade += receiveSubProductGrade.substring(i, i + 1);
					}
					else if (beforeSubProductGrade.substring(i, i + 1).equalsIgnoreCase("G")
							&& (receiveSubProductGrade.substring(i, i + 1).equalsIgnoreCase("N") || receiveSubProductGrade.substring(i, i + 1).equalsIgnoreCase("S")))
					{
						finalSubProductGrade += receiveSubProductGrade.substring(i, i + 1);
					}
					else if (beforeSubProductGrade.substring(i, i + 1).equalsIgnoreCase("N") && receiveSubProductGrade.substring(i, i + 1).equalsIgnoreCase("S"))
					{
						finalSubProductGrade += receiveSubProductGrade.substring(i, i + 1);
					}
					else
					{
						// default is before panelJudge(include EQ report not in S\N\P\G )
						finalSubProductGrade += beforeSubProductGrade.substring(i, i + 1);
					}
				}
			}
			else
			{
				log.info("EQ report SUBPRODUCTJUDGES error.");
				finalSubProductGrade = beforeSubProductGrade;
			}
			*/
			
			String processingInfo = SMessageUtil.getChildText(productElement, "PROCESSINGINFO", false);

			/*
			if (isInReworkFlow)
			{
				// Do not change ProductGrade when EQP reports //G// grade in Rework Flow
				productGrade = productData.getProductGrade();
			}
			else if (!StringUtils.equals(processingInfo, "B") && !(finalSubProductGrade.isEmpty() || finalSubProductGrade.equalsIgnoreCase("")))
			{
				if (GenericServiceProxy.getConstantMap().ProductType_Sheet.equals(productData.getProductType()))
				{
					// Array productGrade need summary two halfglass judge
					String subProductJudge = "";

					subProductJudge = finalSubProductGrade.substring(0, finalSubProductGrade.length() / 2);
					String GlassAJudge = CommonUtil.judgeProductGradeByHalfCutSubProductGrade(subProductJudge);

					subProductJudge = finalSubProductGrade.substring(finalSubProductGrade.length() / 2, finalSubProductGrade.length());
					String GlassBJudge = CommonUtil.judgeProductGradeByHalfCutSubProductGrade(subProductJudge);

					if (GlassAJudge.equalsIgnoreCase("N") && GlassBJudge.equalsIgnoreCase("N"))
						productGrade = "N";
					else if (GlassAJudge.equalsIgnoreCase("P") || GlassBJudge.equalsIgnoreCase("P"))
						productGrade = "P";
					else
						productGrade = "G";
				}
				else if (GenericServiceProxy.getConstantMap().ProductType_Glass.equals(productData.getProductType()))
				{
					// OLED TP: productGrade equals glassJudge rule
					productGrade = CommonUtil.judgeProductGradeByHalfCutSubProductGrade(finalSubProductGrade);
				}

			}
			else
				productGrade = productData.getProductGrade();
			*/

			productPGSRC.setSubProductGrades1(finalSubProductGrade);

			productPGSRC.setProductGrade(productGrade);
			productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
			productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());
			productPGSRC.setReworkFlag("N");

			// Consumable ignored
			productPGSRC.setConsumedMaterialSequence(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());

			String productRecipe = SMessageUtil.getChildText(productElement, "PRODUCTRECIPE", false);

			String slotPosition = SMessageUtil.getChildText(productElement, "SLOTPOSITION", false);
			productPGSRC.getUdfs().put("SLOTPOSITION", slotPosition);
			productPGSRC.getUdfs().put("PRODUCTRECIPE", productRecipe);
			
			// 2020-11-17	dhko	0000406	
			// Sorter设备上报的LotProcsessEnd/LotProcessAbort消息都不再继承Sorter设备的ProcessingInfo
			if (!isSorter)
			{
				productPGSRC.getUdfs().put("PROCESSINGINFO", processingInfo);
			}

			if(StringUtils.isEmpty(processingInfo))
			{
				processingInfo = productData.getUdfs().get("PROCESSINGINFO");
			}
			
			//For LastMainOperation Check
			log.info("LastMainOperationName before set : " + productData.getUdfs().get("LASTMAINOPERNAME"));
			ProcessFlow currentFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(productData);
			if (StringUtils.equals(currentFlowData.getProcessFlowType(), "Main") && !CommonUtil.equalsIn(processingInfo, "B", "S"))
			{
				productPGSRC.getUdfs().put("LASTMAINFLOWNAME", productData.getProcessFlowName());
				productPGSRC.getUdfs().put("LASTMAINOPERNAME", productData.getProcessOperationName());
			}
			log.info("LastMainOperationName after set : " + productPGSRC.getUdfs().get("LASTMAINOPERNAME"));

			productPGSRCSequence.add(productPGSRC);
		}

		return productPGSRCSequence;

	}

	public List<ProductPGSRC> setProductPGSRCSequenceForArrayTest(Element bodyElement, boolean isInReworkFlow, EventInfo eventInfo) throws CustomException
	{
		String machineName = SMessageUtil.getChildText(bodyElement, "MACHINENAME", true);
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		
		boolean isSorter = false;
		if (CommonUtil.equalsIn(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Sorter, GenericServiceProxy.getConstantMap().MachineGroup_CellSorter) ||
				GenericServiceProxy.getConstantMap().SORT_OPERATIONMODE.equals(machineData.getUdfs().get("OPERATIONMODE")))
		{
			isSorter = true;
		}
		
		List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();
		List<Element> productElementList = SMessageUtil.getSubSequenceItemList(bodyElement, "PRODUCTLIST", true);

		for (Element productElement : productElementList)
		{
			String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

			ProductPGSRC productPGSRC = new ProductPGSRC();
			productPGSRC.setProductName(productName);

			String position = SMessageUtil.getChildText(productElement, "POSITION", true);
			productPGSRC.setPosition(Long.valueOf(position));
			//String productGrade = SMessageUtil.getChildText(productElement, "PRODUCTJUDGE", false);
			String productGrade = productData.getProductGrade();
			String subProductGrade = SMessageUtil.getChildText(productElement, "SUBPRODUCTJUDGES", true);

			String processingInfo = SMessageUtil.getChildText(productElement, "PROCESSINGINFO", false);

			if (isInReworkFlow && StringUtils.equals(productGrade, GenericServiceProxy.getConstantMap().ProductGrade_G))
			{
				// Do not change ProductGrade when EQP reports G grade in Rework Flow
				productGrade = productData.getProductGrade();
			}
			else
			{
				if (!StringUtils.equals(processingInfo, "B"))
				{
					////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					//Mantis 0000441
					//productGrade = CommonUtil.judgeProductGradeBySubProductGrade(subProductGrade);
					//productPGSRC.setSubProductGrades1(subProductGrade);
					
					String subProductGradesMachine = subProductGrade;
					String subProductGradesMES = productData.getSubProductGrades1();
					
					if (!(subProductGradesMES.isEmpty() || subProductGradesMES.equals("")) && !(subProductGradesMachine.isEmpty() || subProductGradesMachine.equals(""))
							&& subProductGradesMES.length() == subProductGradesMachine.length())
					{
						List <String> gradeResult = MESLotServiceProxy.getLotInfoUtil().makeSubProductGradeList(subProductGradesMES, subProductGradesMachine, productData.getProductType(), productData.getFactoryName(), productName, eventInfo, productData.getProcessOperationName(), productData.getProcessFlowName());
						
						productPGSRC.setSubProductGrades1(gradeResult.get(0));
						productGrade = gradeResult.get(1);
					}
					else
					{
						log.info("EQ report SUBPRODUCTJUDGES error.");
						productPGSRC.setSubProductGrades1(productData.getSubProductGrades1());
					}
					////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				}
			}

			productPGSRC.setProductGrade(productGrade);
			productPGSRC.setSubProductQuantity1(productData.getSubProductQuantity1());
			productPGSRC.setSubProductQuantity2(productData.getSubProductQuantity2());
			productPGSRC.setReworkFlag("N");

			// Consumable ignored
			productPGSRC.setConsumedMaterialSequence(new ArrayList<kr.co.aim.greentrack.product.management.info.ext.ConsumedMaterial>());

			String productRecipe = SMessageUtil.getChildText(productElement, "PRODUCTRECIPE", false);

			String slotPosition = SMessageUtil.getChildText(productElement, "SLOTPOSITION", false);
			String measurePanelQty = SMessageUtil.getChildText(productElement, "MEASUREPANELQTY", false);
			String ngPanelQty = SMessageUtil.getChildText(productElement, "NGPANELQTY", false);
			
			productPGSRC.getUdfs().put("SLOTPOSITION", slotPosition);
			productPGSRC.getUdfs().put("PRODUCTRECIPE", productRecipe);
			productPGSRC.getUdfs().put("ATMEASUREPANELQTY", measurePanelQty);
			productPGSRC.getUdfs().put("ATNGPANELQTY", ngPanelQty);

			// 2020-11-17	dhko	0000406	
			// Sorter设备上报的LotProcsessEnd/LotProcessAbort消息都不再继承Sorter设备的ProcessingInfo
			if (!isSorter)
			{
				productPGSRC.getUdfs().put("PROCESSINGINFO", processingInfo);
			}

			if(StringUtils.isEmpty(processingInfo))
			{
				processingInfo = productData.getUdfs().get("PROCESSINGINFO");
			}
			
			//For LastMainOperation Check
			log.info("LastMainOperationName before set : " + productData.getUdfs().get("LASTMAINOPERNAME"));
			ProcessFlow currentFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(productData);
			if (StringUtils.equals(currentFlowData.getProcessFlowType(), "Main") && !CommonUtil.equalsIn(processingInfo, "B", "S"))
			{
				productPGSRC.getUdfs().put("LASTMAINFLOWNAME", productData.getProcessFlowName());
				productPGSRC.getUdfs().put("LASTMAINOPERNAME", productData.getProcessOperationName());
			}
			log.info("LastMainOperationName after set : " + productPGSRC.getUdfs().get("LASTMAINOPERNAME"));
			productPGSRCSequence.add(productPGSRC);
		}

		return productPGSRCSequence;
	}
	
	public boolean isBufferCSTSortJob(String machineName, String portName, String carrierName)
	{
		boolean isBufferCSTSortJob = false;

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT J.JOBNAME, J.JOBSTATE, C.TRANSFERDIRECTION, C.CARRIERNAME, C.LOTNAME ");
		sql.append("  FROM CT_SORTJOB J, CT_SORTJOBCARRIER C, DURABLE D ");
		sql.append(" WHERE J.JOBNAME = C.JOBNAME ");
		sql.append("   AND J.JOBNAME IN (SELECT J.JOBNAME ");
		sql.append("                       FROM CT_SORTJOB J, CT_SORTJOBCARRIER C ");
		sql.append("                      WHERE J.JOBNAME = C.JOBNAME ");
		sql.append("                        AND C.CARRIERNAME = :CARRIERNAME ");
		sql.append("                        AND C.MACHINENAME = :MACHINENAME ");
		sql.append("                        AND C.PORTNAME = :PORTNAME ");
		sql.append("                        AND J.JOBSTATE = 'STARTED' ");
		sql.append("                        AND ROWNUM = 1) ");
		sql.append("   AND C.CARRIERNAME = D.DURABLENAME ");
		sql.append("   AND D.DURABLETYPE = 'BufferCST' ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("CARRIERNAME", carrierName);
		args.put("MACHINENAME", machineName);
		args.put("PORTNAME", portName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			isBufferCSTSortJob = true;
			String jobName = ConvertUtil.getMapValueByName(result.get(0), "JOBNAME");
			log.info("BufferCST Transfer. JobID: " + jobName);
		}
		else
		{
			log.info("Normal Transfer");
		}

		return isBufferCSTSortJob;
	}

	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getLotListByProductListForBufferCST(List<String> productNameList, String machineName, String portName, String carrierName) throws CustomException
	{
		List<Map<String, Object>> sqlLotList = new ArrayList<Map<String, Object>>();
		ConstantMap constMap = GenericServiceProxy.getConstantMap();

		if (productNameList.size() > 0)
		{
			StringBuilder preSql = new StringBuilder();
			preSql.append("SELECT J.JOBNAME, C.TRANSFERDIRECTION ");
			preSql.append("  FROM CT_SORTJOB J, CT_SORTJOBCARRIER C, DURABLE D ");
			preSql.append(" WHERE J.JOBNAME = C.JOBNAME ");
			preSql.append(" AND C.CARRIERNAME = :CARRIERNAME ");
			preSql.append(" AND C.MACHINENAME = :MACHINENAME ");
			preSql.append(" AND C.PORTNAME = :PORTNAME ");
			preSql.append(" AND J.JOBSTATE = 'STARTED' ");
			preSql.append(" AND C.CARRIERNAME = D.DURABLENAME ");
			preSql.append(" AND D.DURABLETYPE = 'BufferCST' ");
			preSql.append(" ORDER BY J.LASTEVENTTIMEKEY DESC ");
			
			Map<String, String> args = new HashMap<String, String>();
			args.put("CARRIERNAME", carrierName);
			args.put("MACHINENAME", machineName);
			args.put("PORTNAME", portName);

			List<Map<String, Object>> resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(preSql.toString(), args);

			if (resultList != null && resultList.size() > 0) 
			{
				String jobName = ConvertUtil.getMapValueByName(resultList.get(0), "JOBNAME");
				String transDir = ConvertUtil.getMapValueByName(resultList.get(0), "TRANSFERDIRECTION");
				log.info("BufferCST Job Info: JobID = " + jobName + " , TransferDirection = " + transDir);

				StringBuffer sql = new StringBuffer();

				if (constMap.SORT_TRANSFERDIRECTION_SOURCE.equals(transDir)) 
				{
					sql.append(" SELECT LOTNAME, ");
					sql.append("       COUNT (PRODUCTNAME) AS PRODUCTQTY, ");
					sql.append("       CARRIERNAME, ");
					sql.append("       DECODE (CARRIERNAME, :CARRIERNAME, 'Y', 'N') ISSAMECST ");
					sql.append(" FROM PRODUCT ");
					sql.append(" WHERE PRODUCTNAME IN (:PRODUCTNAMELIST)");
					sql.append(" GROUP BY LOTNAME, CARRIERNAME ");
					sql.append(" ORDER BY PRODUCTQTY DESC ");
				} 
				else if (constMap.SORT_TRANSFERDIRECTION_TARGET.equals(transDir)) 
				{
					sql.append("SELECT PR.LOTNAME, ");
					sql.append("       COUNT (PR.PRODUCTNAME) AS PRODUCTQTY, ");
					sql.append("       PR.CARRIERNAME, ");
					sql.append("       CASE PR.CARRIERNAME WHEN :CARRIERNAME THEN 'Y' ELSE 'N' END AS ISSAMECST ");
					sql.append(" FROM  CT_SORTJOBPRODUCT P, PRODUCT PR ");
					sql.append(" WHERE P.JOBNAME = :JOBNAME ");
					sql.append("   AND P.PRODUCTNAME IN ( :PRODUCTNAMELIST ) ");
					sql.append("   AND P.PRODUCTNAME = PR.PRODUCTNAME ");
					sql.append(" GROUP BY PR.LOTNAME, PR.CARRIERNAME ");
					sql.append(" ORDER BY PRODUCTQTY DESC ");
				} 
				else 
				{
					log.info("Invalid TransferDirection Information.");
					return sqlLotList;
				}

				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("PRODUCTNAMELIST", productNameList);
				bindMap.put("CARRIERNAME", carrierName);
				bindMap.put("JOBNAME", jobName);

				sqlLotList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
			}
		}

		return sqlLotList;
	}

	public List<Map<String, Object>> getLotListByProductListForFirstGlass(List<String> productNameList, String carrierName) throws CustomException
	{
		List<Map<String, Object>> sqlLotList = new ArrayList<Map<String, Object>>();

		if (productNameList.size() > 0)
		{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT L.LOTNAME, ");
			sql.append("       COUNT (P.PRODUCTNAME) AS PRODUCTQTY, ");
			sql.append("       L.CARRIERNAME, ");
			sql.append("       DECODE (L.CARRIERNAME, :CARRIERNAME, 'Y', 'N') AS ISSAMECST, ");
			sql.append("       L.FIRSTGLASSFLAG ");
			sql.append("  FROM PRODUCT P, LOT L ");
			sql.append(" WHERE P.PRODUCTNAME IN ( :PRODUCTNAMELIST ) ");
			sql.append("   AND P.LOTNAME = L.LOTNAME ");
			sql.append("   AND (L.FIRSTGLASSFLAG IS NULL OR L.FIRSTGLASSFLAG = 'N') ");
			sql.append("GROUP BY L.LOTNAME, L.CARRIERNAME, L.FIRSTGLASSFLAG ");
			sql.append("ORDER BY L.FIRSTGLASSFLAG ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("PRODUCTNAMELIST", productNameList);
			bindMap.put("CARRIERNAME", carrierName);

			sqlLotList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
		}

		return sqlLotList;
	}

	public void SetFlagStack(List<ProductPGSRC> productPGSRCSequence, Lot lotData)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT FLAG ");
		sql.append("  FROM CT_OPERATIONFLAGCONDITION ");
		sql.append(" WHERE FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND MACHINENAME = :MACHINENAME ");
		sql.append("   AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");

		Map<String, Object> args = new HashMap<>();

		args.put("FACTORYNAME", lotData.getFactoryName());
		args.put("MACHINENAME", lotData.getMachineName());
		args.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
		args.put("PROCESSOPERATIONVERSION", lotData.getProcessOperationVersion());

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			String flag = ConvertUtil.getMapValueByName(result.get(0), "FLAG");
			String sql2 = "SELECT DISTINCT DELETEFLAG FROM CT_OPERATIONFLAGCONDITION WHERE MACHINENAME = :MACHINENAME AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ";

			Map<String, Object> args2 = new HashMap<>();
			args2.put("MACHINENAME", lotData.getMachineName());
			args2.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());

			List<Map<String, Object>> result2 = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql2, args2);

			for (ProductPGSRC productPGSRC : productPGSRCSequence)
			{
				if (!CommonUtil.equalsIn(productPGSRC.getUdfs().get("PROCESSINGINFO"), "B", "S"))
				{
					String[] flagArray = StringUtils.split(productPGSRC.getUdfs().get("FLAGSTACK"), ",");

					List<String> flagList = new ArrayList<String>();

					flagList.add(flag);

					for (int i = 0; i < flagArray.length; i++)
					{
						String tempFlag = flagArray[i];

						if (StringUtils.isNotEmpty(tempFlag) && !flagList.contains(tempFlag))
						{
							flagList.add(tempFlag);
						}
					}

					for (Map<String, Object> map : result2)
					{
						String deleteFlag = ConvertUtil.getMapValueByName(map, "DELETEFLAG");

						if (flagList.contains(deleteFlag))
						{
							flagList.remove(deleteFlag);
						}
					}

					String flagStack = StringUtils.join(flagList, ",");

					productPGSRC.getUdfs().put("FLAGSTACK", flagStack.toString());
				}
			}
		}
	}

	public String judgeLotGradeByProductList(List<ProductPGSRC> productPGSRCSequence)
	{
		String lotJudge = "";

		// grade[0] : N, grade[1] : R, grade[2] : P, grade[3] : S, grade[4] : G
		// Priority : S(all) > N > R > P > G(all)
		int[] grade = { 0, 0, 0, 0, 0 };

		for (ProductPGSRC productPGSRC : productPGSRCSequence)
		{
			String productGrade = productPGSRC.getProductGrade();

			if (productGrade.equals("N"))
			{
				grade[0] += 1;
			}
			else if (productGrade.equals("R"))
			{
				grade[1] += 1;
			}
			else if (productGrade.equals("P"))
			{
				grade[2] += 1;
			}
			else if (productGrade.equals("S"))
			{
				grade[3] += 1;
			}
			else if (productGrade.equals("G"))
			{
				grade[4] += 1;
			}

		}

		if (grade[0] > 0)
		{
			lotJudge = "N";
		}
		else if (grade[1] > 0)
		{
			lotJudge = "R";
		}
		else if (grade[2] > 0)
		{
			lotJudge = "P";
		}
		else if (grade[3] > 0)
		{
			lotJudge = "S";
		}
		else
		{
			lotJudge = "G";
		}

		return lotJudge;
	}

	public void updateReservedLotStateByTrackOut(String lotName, String machineName, Lot lotData, EventInfo eventInfo) throws CustomException
	{
		try
		{
			String condition = "machineName = ? and lotName =? and productSpecName =? and processOperationName =? and productRequestName =? and reserveState = ? ";
			Object bindSet[] = new Object[] { machineName, lotName, lotData.getProductSpecName(), lotData.getProcessOperationName(), lotData.getProductRequestName(), "Executing" };
			List<ReserveLot> reserveLot = ExtendedObjectProxy.getReserveLotService().select(condition, bindSet);

			reserveLot.get(0).setReserveState(GenericServiceProxy.getConstantMap().RESV_STATE_END);
			reserveLot.get(0).setCompleteTimeKey(eventInfo.getEventTimeKey());

			ExtendedObjectProxy.getReserveLotService().modify(eventInfo, reserveLot.get(0));

			condition = "productRequestName = ? and productSpecName = ? and processFlowName = ? and processOperationName = ? and  machineName = ? and planDate = ? ";
			bindSet = new Object[] { lotData.getProductRequestName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName,
					reserveLot.get(0).getPlanDate() };
			List<DSPProductRequestPlan> productRequestPlan = ExtendedObjectProxy.getDSPProductRequestPlanService().select(condition, bindSet);

			/*
			long remainLotQty = productRequestPlan.get(0).getPlanLotQuantity() - 1;
			if (remainLotQty == 0)
			{
				productRequestPlan.get(0).setPlanState("Completed");
			}
			productRequestPlan.get(0).setPlanLotQuantity(remainLotQty);
			ExtendedObjectProxy.getDSPProductRequestPlanService().modify(eventInfo, productRequestPlan.get(0)); */
		}
		catch (Exception e)
		{
			log.info("Fail ReservedLot Updating");
		}
	}

	public boolean isInReworkFlow(List<String> productNameList)
	{
		boolean isInReworkFlow = false;

		if (productNameList.size() > 0)
		{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT DISTINCT F.PROCESSFLOWTYPE ");
			sql.append("  FROM PRODUCT P, PROCESSFLOW F ");
			sql.append(" WHERE P.PRODUCTNAME IN ( :PRODUCTNAMELIST) ");
			sql.append("   AND P.PRODUCTSTATE = :PRODUCTSTATE ");
			sql.append("   AND P.FACTORYNAME = F.FACTORYNAME ");
			sql.append("   AND P.PROCESSFLOWNAME = F.PROCESSFLOWNAME ");
			sql.append("   AND P.PROCESSFLOWVERSION = F.PROCESSFLOWVERSION ");
			sql.append("   AND F.PROCESSFLOWTYPE = :PROCESSFLOWTYPE ");

			Map<String, Object> args = new HashMap<String, Object>();
			args.put("PRODUCTNAMELIST", productNameList);
			args.put("PRODUCTSTATE", GenericServiceProxy.getConstantMap().Prod_InProduction);
			args.put("PROCESSFLOWTYPE", "Rework");

			List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), args);

			if (result.size() > 0)
			{
				isInReworkFlow = true;
			}
		}

		return isInReworkFlow;
	}

	public void updateReservedLotStateByCancelTrackIn(String lotName, String machineName, Lot lotData, EventInfo eventInfo) throws CustomException
	{
		try
		{
			String condition = "machineName = ? and lotName =? and productSpecName =? and processOperationName =? and productRequestName =? and reserveState = ? ";
			Object bindSet[] = new Object[] { machineName, lotName, lotData.getProductSpecName(), lotData.getProcessOperationName(), lotData.getProductRequestName(), "Executing" };
			List<ReserveLot> reserveLot = ExtendedObjectProxy.getReserveLotService().select(condition, bindSet);

			reserveLot.get(0).setReserveState(GenericServiceProxy.getConstantMap().RESV_STATE_RESV);
			reserveLot.get(0).setInputTimeKey("");

			ExtendedObjectProxy.getReserveLotService().modify(eventInfo, reserveLot.get(0));

			condition = "planDate = ? and productSpecName =? and processOperationName =? and productRequestName =? and reserveState = ? ";
			bindSet = new Object[] { reserveLot.get(0).getPlanDate(), lotData.getProductSpecName(), lotData.getProcessOperationName(), lotData.getProductRequestName(), "Executing" };
			List<ReserveLot> reserveLotList = ExtendedObjectProxy.getReserveLotService().select(condition, bindSet);

			if (reserveLotList.size() == 0)
			{
				condition = "productRequestName = ? and productSpecName = ? and processFlowName = ? and processOperationName = ? and  machineName = ? and planDate = ? ";
				bindSet = new Object[] { lotData.getProductRequestName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName,
						reserveLot.get(0).getPlanDate() };
				List<DSPProductRequestPlan> productRequestPlan = ExtendedObjectProxy.getDSPProductRequestPlanService().select(condition, bindSet);

				productRequestPlan.get(0).setPlanState("Created");
				ExtendedObjectProxy.getDSPProductRequestPlanService().modify(eventInfo, productRequestPlan.get(0));
			}

		}
		catch (Exception e)
		{
			log.info("Fail ReservedLot Updating");
		}
	}
	
	public void checkLotValidation (Lot lotData) throws CustomException
	{
		CommonValidation.checkLotState(lotData);
		CommonValidation.checkLotHoldState(lotData);
		CommonValidation.checkLotProcessStateRun(lotData);
	}
	
	public List<Map<String, Object>> getLotListByProductList (List<Element> productList, List<String> productNameList, Port portData, 
			boolean isBufferCST, boolean isBufferCSTSortJob, boolean isFirstGlass, 
			String machineName, String portName, String carrierName) throws CustomException
	{
		List<Map<String, Object>> lotListByProductList = null;
		
		try
		{
			// get LotList by ProductList
			if (StringUtils.equals(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), "PU"))
				lotListByProductList = MESLotServiceProxy.getLotServiceUtil().getSrcLotListByProductList(productList);
			else if (isBufferCST && isBufferCSTSortJob)
				lotListByProductList = getLotListByProductListForBufferCST(productNameList, machineName, portName, carrierName);
			else if (isFirstGlass)
				lotListByProductList = getLotListByProductListForFirstGlass(productNameList, carrierName);
			else
				lotListByProductList = MESLotServiceProxy.getLotServiceUtil().getLotListByProductList(productList, carrierName);
		}
		catch(Exception ex)
		{
		}
		
		return lotListByProductList;
	}

	public void dummyUsedCountIncrease(Lot lotData, EventInfo eventInfo) throws CustomException
	{
		List<MQCPlan> planData = ExtendedObjectProxy.getMQCPlanService().MQCPlanDataByLot(lotData.getKey().getLotName());
		if (planData != null)
		{
			List<MQCPlanDetail> planOperationList = ExtendedObjectProxy.getMQCPlanDetailService().MQCPlanDetailData(planData.get(0).getJobName(), lotData.getProcessOperationName());
			if (planOperationList != null)
			{
				MQCPlanDetail planOperationData = planOperationList.get(0);
				String[] positionArray = StringUtils.split(planOperationData.getPosition(), ",");
				List<Product> productDataList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());
				for (Product productData : productDataList)
				{
					Map<String, String> udf = productData.getUdfs();
					for (String sPosition : positionArray)
					{
						if (productData.getPosition() == Long.parseLong(sPosition))
						{
							// increase dummy used count
							String sDummyUsedCount = CommonUtil.getValue(udf, "DUMMYUSEDCOUNT");

							long dummyUsedCount = 0;

							if (StringUtils.isEmpty(sDummyUsedCount))
								dummyUsedCount++;
							else
							{
								dummyUsedCount = Long.parseLong(sDummyUsedCount) + 1;
							}

							kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
							setEventInfo.getUdfs().put("DUMMYUSEDCOUNT", String.valueOf(dummyUsedCount));

							eventInfo.setEventName("MQCProductUsedCountIncrease");
							MESProductServiceProxy.getProductServiceImpl().setEvent(productData, setEventInfo, eventInfo);

							break;
						}
					}
				}
			}
		}
	}
	
	public Lot ReleaseHoldLot(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		// Release Hold Lot
		if (lotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().Lot_OnHold))
		{
			eventInfo.setEventName("ReleaseHold");
			Map<String, String> udfs = new HashMap<String, String>();
			String reasonCode = lotData.getReasonCode();
			List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
				
			MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(lotData, productUSequence, udfs);
			lotData = LotServiceProxy.getLotService().makeNotOnHold(lotData.getKey(), eventInfo, makeNotOnHoldInfo);
	
			//Delete in LOTMULTIHOLD table
			MESLotServiceProxy.getLotServiceUtil().releaseMultiHold(lotData.getKey().getLotName(), reasonCode , lotData.getProcessOperationName());
				
			//Delete in PRODUCTMULTIHOLD table
			MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(lotData.getKey().getLotName(), reasonCode , lotData.getProcessOperationName());
		}
		
		return lotData;
	}

	public void increateDummyUsedCount(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		List<MQCPlan> mqcPlanData = ExtendedObjectProxy.getMQCPlanService().MQCPlanDataByLot(lotData.getKey().getLotName());
		
		if (mqcPlanData != null)
		{
			for (MQCPlan mqcPlan : mqcPlanData)
			{
				// Check CountingFlag of MQCPlanDetail List
				List<MQCPlanDetail> checkCountingList = ExtendedObjectProxy.getMQCPlanDetailService().MQCPlanDetailData(mqcPlan.getJobName(), lotData.getProcessFlowName(),
						lotData.getProcessOperationVersion(), "Y", "Y");

				if (checkCountingList != null)
				{
					increaseDummyUsedCountByCountingFlag(eventInfo, lotData, mqcPlan, "Y");
				}
				else
				{
					increaseDummyUsedCountByCountingFlag(eventInfo, lotData, mqcPlan, "N");
				}
			}
		}
	}
	
	public void increaseDummyUsedCountByCountingFlag(EventInfo eventInfo, Lot lotData, MQCPlan mqcPlan, String countingFlag) throws CustomException
	{
		List<Product> productDataList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());
		List<MQCPlanDetail> mqcPlanDetailList = ExtendedObjectProxy.getMQCPlanDetailService().MQCPlanDetailData(mqcPlan.getJobName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
				lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), "Y", countingFlag);

		if (mqcPlanDetailList != null)
		{
			for (MQCPlanDetail mqcPlanDetail : mqcPlanDetailList)
			{
				List<MQCPlanDetail_Extended> extendedList = ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().getMQCPlanDetail_ExtendedWithoutProductName(mqcPlanDetail.getJobName(),
						mqcPlanDetail.getProcessFlowName(), mqcPlanDetail.getProcessFlowVersion(), mqcPlanDetail.getProcessOperationName(), mqcPlanDetail.getProcessOperationVersion());

				String[] positionArray = StringUtils.split(mqcPlanDetail.getPosition(), ",");

				if (extendedList != null)
				{
					for (MQCPlanDetail_Extended extended : extendedList)
					{
						for (String detailPosition : positionArray)
						{
							if (StringUtils.equals(extended.getPosition(), detailPosition.trim()))
							{
								// Increate DummyUsedCount in MQCPlanDetail_Extended
								ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().increateDummyUsedCount(extended);

								// Increate DummyUsedCount in Product
								MESProductServiceProxy.getProductServiceUtil().increateDummyUsedCount(eventInfo, productDataList, extended.getPosition());
							}
						}
					}
				}
			}
		}
	}
	
	//AR-TF-0001-01
	public void checkDepartment(String holdEventUser, String nowEventUser, String lotName) throws CustomException
	{
		String holdUserDepartment = "";
		String nowUserDepartment = "";

		String inquirysql = " SELECT DEPARTMENT FROM USERPROFILE  WHERE USERID = :USERID  "
				+ " UNION ALL "
				+ "SELECT DEPARTMENT FROM USERPROFILEHISTORY WHERE USERID = :USERID AND EVENTNAME != 'Remove' ";

		Map<String, String> nowUserbindMap = new HashMap<String, String>();
		nowUserbindMap.put("USERID", nowEventUser);

		List<Map<String, Object>> nowUserResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(inquirysql, nowUserbindMap);
		if (!nowUserResult.isEmpty())
		{
			nowUserDepartment = ConvertUtil.getMapValueByName(nowUserResult.get(0), "DEPARTMENT");
		}else
		{
			log.info("Can't Find HoldUserDepartment, But Only MFGLeader Can Release It");
		}
		
		Map<String, String> userbindMap = new HashMap<String, String>();
		userbindMap.put("USERID", holdEventUser);
		
		List<Map<String, Object>> userResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(inquirysql, userbindMap);
		if(userResult == null || userResult.size() == 0)
		{
			log.info("Unable to find department information in UserProfileHistory. UserID={" + holdEventUser + "}");
		}
		else
		{
			holdUserDepartment = ConvertUtil.getMapValueByName(userResult.get(0), "DEPARTMENT");
		}
		
		if (!checkReleaseFlag(nowEventUser, nowUserDepartment))
		{
			if (!StringUtils.equals(nowUserDepartment, holdUserDepartment))
			{
				if (StringUtils.isNotEmpty(holdUserDepartment))
				{
					throw new CustomException("LOT-0212", lotName, holdUserDepartment);
				}
				else
				{
					throw new CustomException("LOT-0213", lotName);
				}
			}
		}
	}
	
	//hankun
	public void checkDepartmentInfo(String holdEventUser, String nowEventUser, String lotName) throws CustomException
	{
		String holdUserDepartment = "";
		String nowUserDepartment = "";

		String inquirysql = " SELECT DEPARTMENT FROM USERPROFILE WHERE USERID = :USERID ";

		Map<String, String> nowUserbindMap = new HashMap<String, String>();
		nowUserbindMap.put("USERID", nowEventUser);

		List<Map<String, Object>> nowUserResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(inquirysql, nowUserbindMap);
		if (nowUserResult.isEmpty())
		{
			log.info("Can't Find UserDepartment, But Only MFGLeader Can Release It");
		}
		else
		{
			nowUserDepartment = ConvertUtil.getMapValueByName(nowUserResult.get(0), "DEPARTMENT");
		}
		
		Map<String, String> userbindMap = new HashMap<String, String>();
		userbindMap.put("USERID", holdEventUser);
		
		List<Map<String, Object>> userResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(inquirysql, userbindMap);
		if(userResult == null || userResult.size() == 0)
		{
			String inquirysqlH = " SELECT DEPARTMENT FROM USERPROFILEHISTORY WHERE USERID = :USERID AND EVENTNAME = 'Remove' ORDER BY TIMEKEY DESC";
			
			userResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(inquirysqlH, userbindMap);	
			if(!userResult.isEmpty())
			{
				holdUserDepartment = ConvertUtil.getMapValueByName(userResult.get(0), "DEPARTMENT");
			}
			else
			{
				log.info("Unable to find department information in UserProfile. UserID={" + holdEventUser + "}");
			}
		}
		else
		{
			holdUserDepartment = ConvertUtil.getMapValueByName(userResult.get(0), "DEPARTMENT");
		}
		
		if (!checkReleaseFlag(nowEventUser, nowUserDepartment))
		{
			if (!StringUtils.equals(nowUserDepartment, holdUserDepartment))
			{
				if (StringUtils.isNotEmpty(holdUserDepartment))
				{
					throw new CustomException("LOT-0212", lotName, holdUserDepartment);
				}
				else
				{
					throw new CustomException("LOT-0213", lotName);
				}
			}
		}
	}

	//AR-TF-0001-01
	public boolean checkReleaseFlag(String eventUserId, String holdUserDepartment)
	{
		boolean isRelease = false;
		String usergroupsql = " SELECT U.MFGRELEASEFLAG FROM USERGROUP U, USERPROFILE P WHERE P.USERGROUPNAME=U.USERGROUPNAME AND P.USERID= :USERID ";

		Map<String, String> nowUserGroupMap = new HashMap<String, String>();
		nowUserGroupMap.put("USERID", eventUserId);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> nowUserGroupResult = GenericServiceProxy.getSqlMesTemplate().queryForList(usergroupsql, nowUserGroupMap);
		String nowMFGReleaseFlag = ConvertUtil.getMapValueByName(nowUserGroupResult.get(0), "MFGRELEASEFLAG");

		if (StringUtils.equals(nowMFGReleaseFlag, "ALL"))
		{
			isRelease = true;
		}
		else if (!nowMFGReleaseFlag.isEmpty())
		{
			String[] line = nowMFGReleaseFlag.split(",");
			for (String s : line)
			{
				if (StringUtils.equals(s, holdUserDepartment))
				{
					isRelease = true;
					break;
				}
				else
				{
					isRelease = false;
				}
			}
		}

		return isRelease;
	}
	
	// AR-AMF-0030-01
	public void checkMainReserveData(Lot lotData, String factoryName, String toProcessFlowName, String toProcessFlowVersion, String toProcessOperationName, String toProcessOperationVersion)
			throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT T.FACTORYNAME, ");
		sql.append("       T.PROCESSFLOWNAME, ");
		sql.append("       T.PROCESSFLOWVERSION, ");
		sql.append("       T.PROCESSOPERATIONNAME, ");
		sql.append("       T.PROCESSOPERATIONVERSION ");
		sql.append("  FROM TFOMPOLICY T, POSSAMPLE P, LOT L, PROCESSFLOW PF ");
		sql.append(" WHERE T.CONDITIONID = P.CONDITIONID ");
		sql.append("   AND L.FACTORYNAME = PF.FACTORYNAME ");
		sql.append("   AND L.PROCESSFLOWNAME = PF.PROCESSFLOWNAME ");
		sql.append("   AND L.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION ");
		sql.append("   AND T.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND T.PROCESSFLOWNAME = (SELECT PROCESSFLOWNAME ");
		sql.append("                              FROM NODE ");
		sql.append("                             WHERE NODEID = SUBSTR (L.NODESTACK, 1, 16)) ");
		sql.append("   AND T.PROCESSFLOWVERSION = (SELECT PROCESSFLOWVERSION ");
		sql.append("                                 FROM NODE ");
		sql.append("                                WHERE NODEID = SUBSTR (L.NODESTACK, 1, 16)) ");
		sql.append("   AND L.LOTNAME = :LOTNAME ");
		sql.append("   AND P.TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME ");
		sql.append("   AND P.TOPROCESSFLOWVERSION = :TOPROCESSFLOWVERSION ");
		sql.append("   AND P.TOPROCESSOPERATIONNAME = :TOPROCESSOPERATIONNAME ");
		sql.append("   AND P.TOPROCESSOPERATIONVERSION = :TOPROCESSOPERATIONVERSION ");
		sql.append("GROUP BY T.FACTORYNAME, ");
		sql.append("         T.PROCESSFLOWNAME, ");
		sql.append("         T.PROCESSFLOWVERSION, ");
		sql.append("         T.PROCESSOPERATIONNAME, ");
		sql.append("         T.PROCESSOPERATIONVERSION ");
		sql.append("UNION ");
		sql.append("SELECT T.FACTORYNAME, ");
		sql.append("       T.PROCESSFLOWNAME, ");
		sql.append("       T.PROCESSFLOWVERSION, ");
		sql.append("       T.PROCESSOPERATIONNAME, ");
		sql.append("       T.PROCESSOPERATIONVERSION ");
		sql.append("  FROM TFOPOLICY T, POSALTERPROCESSOPERATION P, LOT L, PROCESSFLOW PF ");
		sql.append(" WHERE T.CONDITIONID = P.CONDITIONID ");
		sql.append("   AND L.FACTORYNAME = PF.FACTORYNAME ");
		sql.append("   AND L.PROCESSFLOWNAME = PF.PROCESSFLOWNAME ");
		sql.append("   AND L.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION ");
		sql.append("   AND T.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND T.PROCESSFLOWNAME = (SELECT PROCESSFLOWNAME ");
		sql.append("                              FROM NODE ");
		sql.append("                             WHERE NODEID = SUBSTR (L.NODESTACK, 1, 16)) ");
		sql.append("   AND T.PROCESSFLOWVERSION = (SELECT PROCESSFLOWVERSION ");
		sql.append("                                 FROM NODE ");
		sql.append("                                WHERE NODEID = SUBSTR (L.NODESTACK, 1, 16)) ");
		sql.append("   AND L.LOTNAME = :LOTNAME ");
		sql.append("   AND P.TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME ");
		sql.append("   AND P.TOPROCESSFLOWVERSION = :TOPROCESSFLOWVERSION ");
		sql.append("   AND P.TOPROCESSOPERATIONNAME = :TOPROCESSOPERATIONNAME ");
		sql.append("   AND P.TOPROCESSOPERATIONVERSION = :TOPROCESSOPERATIONVERSION ");
		sql.append("GROUP BY T.FACTORYNAME, ");
		sql.append("         T.PROCESSFLOWNAME, ");
		sql.append("         T.PROCESSFLOWVERSION, ");
		sql.append("         T.PROCESSOPERATIONNAME, ");
		sql.append("         T.PROCESSOPERATIONVERSION ");


		Map<String, Object> args = new HashMap<String, Object>();
		args.put("FACTORYNAME", factoryName);
		args.put("LOTNAME", lotData.getKey().getLotName());
		args.put("TOPROCESSFLOWNAME", toProcessFlowName);
		args.put("TOPROCESSFLOWVERSION", toProcessFlowVersion);
		args.put("TOPROCESSOPERATIONNAME", toProcessOperationName);
		args.put("TOPROCESSOPERATIONVERSION", toProcessOperationVersion);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			for (Map<String, Object> row : result)
			{
				String mainOpeationName = ConvertUtil.getMapValueByName(row, "PROCESSOPERATIONNAME");
				String mainOpeationVersion = ConvertUtil.getMapValueByName(row, "PROCESSOPERATIONVERSION");

				CommonValidation.checkMainReserveSkipData(lotData, mainOpeationName, mainOpeationVersion);
			}
		}
	}

	// AR-AMF-0030-01
	public boolean checkMainReserveSkip(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		log.info("Check MainReserveSkip Data [lotName : " + lotData.getKey().getLotName() + ", OperationName : " + lotData.getProcessOperationName());

		boolean skipFlag = false;

		List<MainReserveSkip> dataInfoList = ExtendedObjectProxy.getMainReserveSkipService().getMainReserveSkipDataList(lotData.getKey().getLotName(), lotData.getFactoryName(),
				lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(),
				lotData.getProcessOperationVersion());

		if (dataInfoList != null)
		{
			skipFlag = true;

			for (MainReserveSkip dataInfo : dataInfoList)
			{
				eventInfo.setEventName("DeleteMainReserveSkip");
				ExtendedObjectProxy.getMainReserveSkipService().remove(eventInfo, dataInfo);
			}
		}

		return skipFlag;
	}
	
	public void setNextOperReworkFlag (EventInfo eventInfo, Lot newLotData) throws CustomException
	{
		try
		{
			List<SampleLot> sampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListForNextNode(
					newLotData.getKey().getLotName(), newLotData.getFactoryName(), newLotData.getProductSpecName(), newLotData.getProductSpecVersion(), 
					newLotData.getProcessFlowName(), newLotData.getProcessFlowVersion(), newLotData.getProcessOperationName(), newLotData.getProcessOperationVersion());
		
			if (sampleLot != null)
			{
				List<SampleProduct> sampleProduct =  ExtendedObjectProxy.getSampleProductService().getSampleProductDataListByLotNameAndToInfo(
						newLotData.getKey().getLotName(), newLotData.getFactoryName(), newLotData.getProductSpecName(), newLotData.getProductSpecVersion(), 
						newLotData.getProcessFlowName(), newLotData.getProcessFlowVersion(), newLotData.getProcessOperationName(), newLotData.getProcessOperationVersion());
			
				for(SampleProduct productData : sampleProduct)
				{
					// Update Product ReworkFlag
					String sql = "UPDATE PRODUCT SET REWORKFLAG = :REWORKFLAG WHERE PRODUCTNAME = :PRODUCTNAME";
					Map<String, Object> bindMap = new HashMap<String, Object>();
					bindMap.put("REWORKFLAG", "Y");
					bindMap.put("PRODUCTNAME", productData.getProductName());
	
					GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
	
					// Update ProductHistory ReworkFlag
					String sql2 = "UPDATE PRODUCTHISTORY SET REWORKFLAG = :REWORKFLAG WHERE PRODUCTNAME = :PRODUCTNAME AND TIMEKEY = :TIMEKEY";
					Map<String, Object> bindMap2 = new HashMap<String, Object>();
					bindMap2.put("REWORKFLAG", "Y");
					bindMap2.put("PRODUCTNAME", productData.getProductName());
					bindMap2.put("TIMEKEY", eventInfo.getEventTimeKey());
				
					GenericServiceProxy.getSqlMesTemplate().update(sql2, bindMap2);
				}
			}
		}
		catch(Exception e)
		{log.info("Faild Set ReworkFlag");}
	}

	public void deleteOriginalProductInfo(EventInfo eventInfo, Lot beforeLotData, Lot afterLotData, List<String> prodList) throws CustomException
	{
		ProcessFlow beforeFlow = CommonUtil.getProcessFlowData(beforeLotData.getFactoryName(), beforeLotData.getProcessFlowName(), beforeLotData.getProcessFlowVersion());
		ProcessFlow afterFlow = CommonUtil.getProcessFlowData(afterLotData.getFactoryName(), afterLotData.getProcessFlowName(), afterLotData.getProcessFlowVersion());

		if (StringUtils.equals(afterLotData.getFactoryName(), "TP") && StringUtils.equals(beforeFlow.getProcessFlowType(), "Sort"))
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			for (String prod : prodList)
			{
				List<OriginalProductInfo> dataInfoList = new ArrayList<OriginalProductInfo>();

				String condition = " PRODUCTNAME = ? AND CARRIERNAME = ? ";
				Object[] bindSet = new Object[] { prod, afterLotData.getCarrierName() };

				try
				{
					dataInfoList = ExtendedObjectProxy.getOriginalProductInfoService().select(condition, bindSet);
				}
				catch (Exception e)
				{
					dataInfoList = null;
				}

				if (dataInfoList != null)
				{
					for (OriginalProductInfo dataInfo : dataInfoList)
						ExtendedObjectProxy.getOriginalProductInfoService().remove(eventInfo, dataInfo);
					
					Product prodData = MESProductServiceProxy.getProductServiceUtil().getProductData(prod);
					
					kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
					Map<String, String> udfs = setEventInfo.getUdfs();
					udfs.put("CHANGESHOPLOTNAME", "");

					MESProductServiceProxy.getProductServiceImpl().setEvent(prodData, setEventInfo, eventInfo);
				}
			}
		}
	}
	
	public int getOperationLevel(String factoryName, String processFlowName, String processOperationName)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT LEVEL LV, ");
		sql.append("       N.FACTORYNAME, ");
		sql.append("       N.NODEATTRIBUTE1 PROCESSOPERATIONNAME, ");
		sql.append("       N.NODEATTRIBUTE2 PROCESSOPERATIONVERSION, ");
		sql.append("       N.PROCESSFLOWNAME, ");
		sql.append("       N.PROCESSFLOWVERSION, ");
		sql.append("       N.NODEID ");
		sql.append("  FROM ARC A, NODE N, PROCESSFLOW PF ");
		sql.append(" WHERE 1 = 1 ");
		sql.append("   AND N.NODETYPE = 'ProcessOperation' ");
		sql.append("   AND PF.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND N.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION ");
		sql.append("   AND N.PROCESSFLOWNAME = A.PROCESSFLOWNAME ");
		sql.append("   AND N.FACTORYNAME = PF.FACTORYNAME ");
		sql.append("   AND A.FROMNODEID = N.NODEID ");
		sql.append("   AND N.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND N.PROCESSFLOWNAME = PF.PROCESSFLOWNAME ");
		sql.append("   AND N.NODEATTRIBUTE1 = :PROCESSOPERATIONNAME ");
		sql.append("START WITH N.NODETYPE = 'Start' ");
		sql.append("CONNECT BY NOCYCLE A.FROMNODEID = PRIOR A.TONODEID ");
		sql.append("               AND A.FACTORYNAME = :FACTORYNAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("FACTORYNAME", factoryName);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSOPERATIONNAME", processOperationName);
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		
		int level = 0;
		
		if(sqlResult.size()>0)
			level = Integer.parseInt(ConvertUtil.getMapValueByName(sqlResult.get(0), "LV"));
		
		return level;
	}
	
	public void increaseRunControlUseCount(EventInfo eventInfo, String machineName, String lotName, boolean validation, int actualProductQty) throws greenFrameDBErrorSignal, CustomException
	{
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
		ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);

		if (!StringUtils.contains(flowData.getProcessFlowType(), "MQC"))
		{
			// DSPRunControl
			ExtendedObjectProxy.getDSPRunControlService().increaseUseCount(eventInfo, machineName, lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), validation, actualProductQty);

			// TryRunControl
			ExtendedObjectProxy.getTryRunControlService().increaseUseCount(eventInfo, machineName, lotData, validation);

			// ChangeRunControl
			ExtendedObjectProxy.getChangeRunControlService().increaseUseCount(eventInfo, machineName, lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), validation);
		}
	}
	
	public void decreaseRunControlUseCount(EventInfo eventInfo, String machineName, String lotName, int actualProductQty) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
		ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);

		if (!StringUtils.contains(flowData.getProcessFlowType(), "MQC"))
		{
			// DSPRunControl
			ExtendedObjectProxy.getDSPRunControlService().decreaseUseCount(eventInfo, machineName, lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), actualProductQty);

			// TryRunControl
			ExtendedObjectProxy.getTryRunControlService().decreaseUseCount(eventInfo, machineName, lotData);
		}
	}
	
	public void sendMailByRunControl(EventInfo eventInfo, String machineName, Lot beforeTrackOutLot) throws CustomException
	{
		// DSPRunControl
		ExtendedObjectProxy.getDSPRunControlService().sendEmailByTrackOut(machineName, beforeTrackOutLot);

		// TryRunControl
		ExtendedObjectProxy.getTryRunControlService().sendEmailByTrackOut(eventInfo, machineName, beforeTrackOutLot);

		// ChangeRunControl
		ExtendedObjectProxy.getChangeRunControlService().sendEmailByTrackOut(machineName, beforeTrackOutLot);
	}
  //20210425 CAIXU DSPRunControl Reset Count in MQC Modify
	public void runControlResetCountAndSendMail(EventInfo eventInfo, String machineName, Lot lotData, String lotRecipeName, List<String> prodRecipeList) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		eventInfo.setEventName("TrackOut");
		
		// Only DSPRunControl Reset Count in MQC
		ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);

		if (StringUtils.contains(flowData.getProcessFlowType(), "MQC"))
		{
			if (StringUtils.isNotEmpty(lotRecipeName))
			{
				ExtendedObjectProxy.getDSPRunControlService().resetCount(eventInfo, machineName, lotRecipeName);
			}

			if (prodRecipeList != null && prodRecipeList.size() > 0)
			{
				for (String prodRecipe : prodRecipeList)
				{
					ExtendedObjectProxy.getDSPRunControlService().resetCount(eventInfo, machineName, prodRecipe);
				}
			}
		}
		
		//Send mail
		MESLotServiceProxy.getLotServiceUtil().sendMailByRunControl(eventInfo, machineName, lotData);
	}
	
	public String getFirstOffSet(Lot lotData) throws CustomException
	{
		List<String> prodList = MESLotServiceProxy.getLotServiceUtil().getProductList(lotData.getKey().getLotName());

		String offSet = "";

		for (String prod : prodList)
		{
			Product prodData = MESProductServiceProxy.getProductServiceUtil().getProductData(prod);

			if (StringUtils.isEmpty(offSet) && StringUtils.isNotBlank(prodData.getUdfs().get("OFFSET")))
			{
				offSet = prodData.getUdfs().get("OFFSET");
				break;
			}
		}

		return offSet;
	}

	public void transferProductForDummy(EventInfo eventInfo, Lot sourceLotData, Lot destLotData, List<Element> productList, Map<String, String> deassignCarrierUdfs, Port portData,
			String sProductQuantity) throws CustomException
	{
//		ProductSpec specData = GenericServiceProxy.getSpecUtil().getProductSpec(destLotData.getFactoryName(), destLotData.getProductSpecName(), destLotData.getProductSpecVersion());

//		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();
//		changeSpecInfo.setAreaName(sourceLotData.getAreaName());
//		changeSpecInfo.setDueDate(sourceLotData.getDueDate());
//		changeSpecInfo.setFactoryName(sourceLotData.getFactoryName());
//		changeSpecInfo.setLotHoldState(sourceLotData.getLotHoldState());
//		changeSpecInfo.setLotProcessState(sourceLotData.getLotProcessState());
//		changeSpecInfo.setLotState(sourceLotData.getLotState());
//		changeSpecInfo.setNodeStack(destLotData.getNodeStack());
//		changeSpecInfo.setPriority(sourceLotData.getPriority());
//		changeSpecInfo.setProcessFlowName(destLotData.getProcessFlowName());
//		changeSpecInfo.setProcessFlowVersion(destLotData.getProcessFlowVersion());
//		changeSpecInfo.setProcessOperationName(sourceLotData.getProcessOperationName());
//		changeSpecInfo.setProcessOperationVersion(sourceLotData.getProcessOperationVersion());
//		changeSpecInfo.setProductionType(specData.getProductionType());
//		changeSpecInfo.setProductRequestName(destLotData.getProductRequestName());
//		changeSpecInfo.setProductSpec2Name(destLotData.getProductSpec2Name());
//		changeSpecInfo.setProductSpec2Version(destLotData.getProductSpec2Version());
//		changeSpecInfo.setProductSpecName(specData.getKey().getProductSpecName());
//		changeSpecInfo.setProductSpecVersion(specData.getKey().getProductSpecVersion());
//		changeSpecInfo.setSubProductUnitQuantity1(specData.getSubProductUnitQuantity1());
//		changeSpecInfo.setSubProductUnitQuantity2(specData.getSubProductUnitQuantity2());
//
//		Map<String, String> udfs = sourceLotData.getUdfs();
//		udfs.put("OLDPRODUCTREQUESTNAME", srcProductRequestName);
//
//		changeSpecInfo.setUdfs(udfs);
//
//		eventInfo.setEventName("ChangeSpecForDummy");
//		sourceLotData = LotServiceProxy.getLotService().changeSpec(sourceLotData.getKey(), eventInfo, changeSpecInfo);
//
//		// Decrease Source Quantity
//		MESWorkOrderServiceProxy.getProductRequestServiceImpl().decrementQuantity(eventInfo, sourceLotData, (int) sourceLotData.getProductQuantity());
//
		// TransferProductsToLot (Merge)
//		MESLotServiceProxy.getLotServiceUtil().transferProductsToLotForDummy(eventInfo, destLotData, portData, sourceLotData.getKey().getLotName(), sProductQuantity, deassignCarrierUdfs, productList,
//				destLotData, offSet, srcProductRequestName, srcProductSpecName);
		
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		String offSet = MESLotServiceProxy.getLotServiceUtil().getFirstOffSet(destLotData);
		String srcProductRequestName = sourceLotData.getProductRequestName();
		String srcProductSpecName = sourceLotData.getProductSpecName();

		List<ProductP> productPSequence = MESLotServiceProxy.getLotInfoUtil().setProductPSequenceForDummy(eventInfo, productList, sourceLotData, destLotData, offSet, srcProductRequestName, srcProductSpecName);

		// Deassign Product
		log.info("Deassigned LotName : " + sourceLotData.getKey().getLotName() + " ProductCount : " + productPSequence.size());
		eventInfo.setEventName("DeassignProducts");
		DeassignProductsInfo deassignProductsInfo = new DeassignProductsInfo("", productPSequence.size(), productPSequence, "");
		sourceLotData = LotServiceProxy.getLotService().deassignProducts(sourceLotData.getKey(), eventInfo, deassignProductsInfo);

		MESLotServiceProxy.getLotServiceUtil().makeEmptiedForDummy(eventInfo, sourceLotData);
		MESWorkOrderServiceProxy.getProductRequestServiceImpl().decrementQuantity(eventInfo, sourceLotData, (int) sourceLotData.getProductQuantity());
		
		List<Product> destProductDataList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(destLotData.getKey().getLotName());
		
		String lastMainFlow = destProductDataList.get(0).getUdfs().get("LASTMAINFLOWNAME");
		String lastMainOper = destProductDataList.get(0).getUdfs().get("LASTMAINOPERNAME");

		for (ProductP prodP : productPSequence)
		{
			String productName = prodP.getProductName();

			log.info("Deassign Product : " + productName);

			Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);
			ProductSpec specData = GenericServiceProxy.getSpecUtil().getProductSpec(destLotData.getFactoryName(), destLotData.getProductSpecName(), destLotData.getProductSpecVersion());
			
			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("OLDPRODUCTREQUESTNAME", productData.getProductRequestName());
			udfs.put("LASTMAINFLOWNAME", lastMainFlow);
			udfs.put("LASTMAINOPERNAME", lastMainOper);
			
			// ChangeSpec Product
			kr.co.aim.greentrack.product.management.info.ChangeSpecInfo changeSpecInfo = MESProductServiceProxy.getProductInfoUtil().changeSpecInfo(productData, productData.getAreaName(),
					productData.getCarrierName(), productData.getDueDate(), productData.getFactoryName(), "", productData.getLotName(), destLotData.getNodeStack(), productData.getPosition(),
					productData.getPriority(), destLotData.getProcessFlowName(), destLotData.getProcessFlowVersion(), destLotData.getProcessOperationName(), destLotData.getProcessOperationVersion(),
					productData.getProductHoldState(), specData.getProductionType(), productData.getProductProcessState(), destLotData.getProductRequestName(), specData.getProductSpec2Name(),
					specData.getProductSpec2Version(), specData.getKey().getProductSpecName(), specData.getKey().getProductSpecVersion(), productData.getProductState(),
					destLotData.getSubProductUnitQuantity1(), destLotData.getSubProductUnitQuantity2(), udfs);

			eventInfo.setEventName("ChangeSpecForDummy");
			MESProductServiceProxy.getProductServiceImpl().changeSpec(eventInfo, productData, changeSpecInfo);
			productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);
		}

		// Assign Product
		log.info("Assigned Lot : " + destLotData.getKey().getLotName() + "ProductCount : " + productPSequence.size());
		eventInfo.setEventName("AssignProducts");
		AssignProductsInfo assignProductsInfo = new AssignProductsInfo("", productPSequence.size(), productPSequence, "", "N");
		destLotData = LotServiceProxy.getLotService().assignProducts(destLotData.getKey(), eventInfo, assignProductsInfo);
		
		MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementQuantity(eventInfo, destLotData, productPSequence.size());
	}
	
	public Lot RecoverySpec(EventInfo oriEventInfo, List<Element> productList, Lot lotData) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo_IgnoreHold(oriEventInfo.getEventName(),oriEventInfo.getEventUser(), oriEventInfo.getEventComment(), null, null);
		
		lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getKey().getLotName());
		String baseProductName = SMessageUtil.getChildText(productList.get(0), "PRODUCTNAME", true);

		DummyProductAssign dummyProdAssignData = ExtendedObjectProxy.getDummyProductAssignService().getDummyProductAssignData(baseProductName);

		String returnProductSpecName = dummyProdAssignData.getReturnProductSpecName();
		String returnProductSpecVersion = dummyProdAssignData.getReturnProductSpecVersion();
		String returnProcessFlowName = dummyProdAssignData.getReturnProcessFlowName();
		String returnProcessFlowVersion = dummyProdAssignData.getReturnProcessFlowVersion();
		String returnProductRequestName = dummyProdAssignData.getReturnProductRequestName();
		String returnProcessOperationName=dummyProdAssignData.getReturnProcessOperationName();
		String returnProcessOperationVersion=dummyProdAssignData.getReturnProcessOperationVersion();
		ProductSpec specData = GenericServiceProxy.getSpecUtil().getProductSpec(lotData.getFactoryName(), returnProductSpecName, returnProductSpecVersion);
		String nodeStack = CommonUtil.getNodeStack(lotData.getFactoryName(), returnProcessFlowName, returnProcessFlowVersion, returnProcessOperationName, returnProcessOperationVersion);

		List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUSequenceForDummy(lotData.getKey().getLotName());

		Map<String, String> udfs = new HashMap<String, String>();
		udfs.put("OLDPRODUCTREQUESTNAME", lotData.getProductRequestName());
		
		ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeDummySpecInfo(lotData, lotData.getAreaName(), lotData.getDueDate(), lotData.getFactoryName(), lotData.getLotHoldState(),
				lotData.getLotProcessState(), lotData.getLotState(), nodeStack, lotData.getPriority(), returnProcessFlowName, returnProcessFlowVersion,
				returnProcessOperationName, returnProcessOperationVersion, lotData.getProcessOperationName(), specData.getProductionType(), returnProductRequestName, "", "",
				returnProductSpecName, returnProductSpecVersion, productUdfs, specData.getSubProductUnitQuantity1(), specData.getSubProductUnitQuantity2());

		eventInfo.setEventName("RecoveryDummySpec");
		Lot newLotData = MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);

		List<ProductPGS> productPGSList = new ArrayList<ProductPGS>();
		for (Element product : productList)
		{
			String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", true);
			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

			List<DummyProductAssign> dataInfoList = ExtendedObjectProxy.getDummyProductAssignService().getDummyProductAssignRecoverInfo(productName, newLotData);

			if (dataInfoList != null)
			{
				for (DummyProductAssign dataInfo : dataInfoList)
					ExtendedObjectProxy.getDummyProductAssignService().remove(eventInfo, dataInfo);
			}
			else
			{
				throw new CustomException("LOT-0397", productName);
			}

			
		}

        eventInfo.setEventComment("DummyComplete,ScrapDummyProduct");
        eventInfo.setReasonCode("DummyHold");
		MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, newLotData, udfs);
		
		
		return newLotData;
	}
	
	public Lot updateSubProductQuantity(ProductSpec productSpecData, Lot lotData, long s1) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		List<Product> sequence = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());

		for (Product product : sequence)
		{
			log.info("Update SubProductQuantity Product : " + product.getKey().getProductName() + "SubProductQuantity : " + s1);
			product.setSubProductQuantity(s1);
			product.setSubProductQuantity1(s1);
			ProductServiceProxy.getProductService().update(product);
		}

		// calulate lot
		double[] result = this.calculateLotAllQuantity(lotData.getKey().getLotName());

		log.info("Update SubProductQuantity Lot : " + lotData.getKey().getLotName() + "SubProductQuantity : " + result[1] + ", " + result[2] + ", " + result[3]);
		lotData.setSubProductQuantity(result[1]);
		lotData.setSubProductQuantity1(result[2]);
		lotData.setSubProductQuantity2(result[3]);
		LotServiceProxy.getLotService().update(lotData);

		lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getKey().getLotName());

		return lotData;
	}
	
	public double[] calculateLotAllQuantity(String lotName) throws FrameworkErrorSignal, NotFoundSignal
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT COUNT (*), SUM (SUBPRODUCTQUANTITY), SUM (SUBPRODUCTQUANTITY1), SUM (SUBPRODUCTQUANTITY2) ");
		sql.append("  FROM PRODUCT ");
		sql.append(" WHERE LOTNAME = :LOTNAME ");
		sql.append("   AND PRODUCTSTATE NOT IN ('Scrapped', 'Shipped', 'Consumed') ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("LOTNAME", lotName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() == 0)
		{
			String keyString = "lotName:" + lotName + ",Scrapped:" + GenericServiceProxy.getConstantMap().Prod_Scrapped + ",Shipped:" + GenericServiceProxy.getConstantMap().Prod_Shipped
					+ ",Consumed:" + GenericServiceProxy.getConstantMap().Prod_Consumed;

			throw new NotFoundSignal(keyString, sql.toString());
		}

		ListOrderedMap orderMap = (ListOrderedMap) result.get(0);

		double[] quantityResult = new double[4];
		for (int i = 0; i < 4; i++)
		{
			quantityResult[i] = orderMap.getValue(i) == null ? 0 : ((BigDecimal) orderMap.getValue(i)).doubleValue();
		}

		return quantityResult;
	}
	
	public boolean allDummyGlass(Lot lotData) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		List<Product> prodList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotData.getKey().getLotName());

		for (Product prod : prodList)
		{
			String dummyGlassFlag = prod.getUdfs().get("DUMMYGLASSFLAG");

			if (!StringUtils.equals(dummyGlassFlag, "Y"))
				return false;
		}
		
		return true;
	}

	public void makeEmptiedForDummy(EventInfo eventInfo, Lot sourceLotData) throws CustomException
	{
		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(sourceLotData.getKey().getLotName());

		if (lotData.getProductQuantity() == 0)
		{
			if (StringUtil.isNotEmpty(lotData.getCarrierName()))
			{
				Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();
				Durable sLotDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());
				deassignCarrierUdfs = sLotDurableData.getUdfs();
				
				eventInfo.setEventTimeKey(ConvertUtil.getCurrTimeKey());
				DeassignCarrierInfo deassignCarrierInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, sLotDurableData, new ArrayList<ProductU>());

				// Deassign Carrier
				eventInfo.setEventName("DeassignCarrier");
				MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, deassignCarrierInfo, eventInfo);
			

				// Make Emptied
				lotData.getUdfs().put("MERGEABLEFLAG", "");

				eventInfo.setEventTimeKey(ConvertUtil.getCurrTimeKey());
				eventInfo.setEventName("MakeEmptied");
				MESLotServiceProxy.getLotServiceImpl().MakeEmptied(eventInfo, lotData, new ArrayList<ProductU>(), deassignCarrierUdfs);
			}
			ExtendedObjectProxy.getDummyProductReserveService().deleteDummyProductReserve(eventInfo, lotData);
		}
	}
	
	public void completeSortFlow(EventInfo eventInfo, List<SortJobCarrier> sortJobCarrierList) throws CustomException
	{	
		for (SortJobCarrier sortJobCarrier : sortJobCarrierList)
		{
			Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(sortJobCarrier.getLotName());
			ProcessFlow flowData = CommonUtil.getProcessFlowData(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion());

			if (!StringUtils.equals(flowData.getProcessFlowType(), "Sort"))
				continue;

			log.info("Lot : " + lotData.getKey().getLotName() + " / ReturnFlow : " + lotData.getUdfs().get("RETURNFLOWNAME") + " / ReturnOperation : " + lotData.getUdfs().get("RETURNOPERATIONNAME"));

			ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();
			changeSpecInfo.setAreaName(lotData.getAreaName());
			changeSpecInfo.setDueDate(lotData.getDueDate());
			changeSpecInfo.setFactoryName(lotData.getFactoryName());
			changeSpecInfo.setLotHoldState(lotData.getLotHoldState());
			changeSpecInfo.setLotProcessState(lotData.getLotProcessState());
			changeSpecInfo.setLotState(lotData.getLotState());
			changeSpecInfo.setNodeStack("");
			changeSpecInfo.setPriority(lotData.getPriority());
			changeSpecInfo.setProcessFlowName(lotData.getUdfs().get("RETURNFLOWNAME"));
			changeSpecInfo.setProcessFlowVersion("00001");
			changeSpecInfo.setProcessOperationName(lotData.getUdfs().get("RETURNOPERATIONNAME"));
			changeSpecInfo.setProcessOperationVersion("00001");
			changeSpecInfo.setProductionType(lotData.getProductionType());
			changeSpecInfo.setProductRequestName(lotData.getProductRequestName());
			changeSpecInfo.setProductSpec2Name(lotData.getProductSpec2Name());
			changeSpecInfo.setProductSpec2Version(lotData.getProductSpec2Version());
			changeSpecInfo.setProductSpecName(lotData.getProductSpecName());
			changeSpecInfo.setProductSpecVersion(lotData.getProductSpecVersion());
			changeSpecInfo.setProductUSequence(this.setProductUSequence(lotData.getKey().getLotName()));
			changeSpecInfo.setSubProductUnitQuantity1(lotData.getSubProductUnitQuantity1());
			changeSpecInfo.setSubProductUnitQuantity2(lotData.getSubProductUnitQuantity2());

			Map<String, String> lotUdfs = new HashMap<String, String>();

			lotUdfs.put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());
			lotUdfs.put("BEFOREFLOWNAME", lotData.getProcessFlowName());

			StringBuffer sql = new StringBuffer();
			sql.append("SELECT NODEID, PROCESSFLOWNAME, NODEATTRIBUTE1, FACTORYNAME ");
			sql.append("  FROM NODE ");
			sql.append(" WHERE FACTORYNAME = ? ");
			sql.append("   AND PROCESSFLOWNAME = ? ");
			sql.append("   AND PROCESSFLOWVERSION = ? ");
			sql.append("   AND NODEATTRIBUTE1 = ? ");
			sql.append("   AND NODEATTRIBUTE2 = ? ");
			sql.append("   AND NODETYPE = 'ProcessOperation' ");

			Object[] bind = new Object[] { changeSpecInfo.getFactoryName(), changeSpecInfo.getProcessFlowName(), changeSpecInfo.getProcessFlowVersion(), changeSpecInfo.getProcessOperationName(),
					changeSpecInfo.getProcessOperationVersion() };

			String[][] returnNodeResult = null;
			try
			{
				returnNodeResult = GenericServiceProxy.getSqlMesTemplate().queryForStringArray(sql.toString(), bind);
			}
			catch (Exception e)
			{
			}

			if (returnNodeResult.length == 0)
			{
				throw new CustomException("Node-0001", lotData.getProductSpecName(), changeSpecInfo.getProcessFlowName(), changeSpecInfo.getProcessOperationName());
			}
			else
			{
				String sToBeNodeStack = (String) returnNodeResult[0][0];

				ProcessFlowKey processFlowKey = new ProcessFlowKey();
				processFlowKey.setFactoryName((String) returnNodeResult[0][3]);
				processFlowKey.setProcessFlowName((String) returnNodeResult[0][1]);
				processFlowKey.setProcessFlowVersion("00001");
				ProcessFlow returnFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);

				if (StringUtil.equals(returnFlowData.getProcessFlowType(), "Main") || StringUtil.equals(returnFlowData.getProcessFlowType(), "MQCPrepare"))
				{
					lotUdfs.put("RETURNFLOWNAME", "");
					lotUdfs.put("RETURNOPERATIONNAME", "");
				}
				else
				{
					String currentNode = lotData.getNodeStack();
					if (StringUtil.isNotEmpty(currentNode) && currentNode.indexOf(".") > 0)
					{
						int NodeStackCount = CommonUtil.getNodeStackCount(currentNode, '.');
						String originalNode = currentNode.substring(0, currentNode.lastIndexOf("."));

						String returnForOriginalNode = originalNode;

						if (NodeStackCount >= 3)
						{
							if (returnForOriginalNode.lastIndexOf(".") > -1)
								returnForOriginalNode = returnForOriginalNode.substring(0, returnForOriginalNode.lastIndexOf("."));

							returnForOriginalNode = returnForOriginalNode.substring(returnForOriginalNode.lastIndexOf(".") + 1, returnForOriginalNode.length());
						}
						else
						{
							if (returnForOriginalNode.lastIndexOf(".") > -1)
								returnForOriginalNode = returnForOriginalNode.substring(0, returnForOriginalNode.lastIndexOf("."));
						}

						StringBuffer sql2 = new StringBuffer();
						sql2.append("SELECT NODEID, PROCESSFLOWNAME, NODEATTRIBUTE1, FACTORYNAME ");
						sql2.append("  FROM NODE ");
						sql2.append(" WHERE NODEID = ? ");
						sql2.append("   AND NODETYPE = 'ProcessOperation' ");

						bind = new Object[] { returnForOriginalNode };

						String[][] orginalNodeResult = null;
						try
						{
							orginalNodeResult = GenericServiceProxy.getSqlMesTemplate().queryForStringArray(sql2.toString(), bind);
						}
						catch (Exception e)
						{
						}

						if (orginalNodeResult.length > 0)
						{
							lotUdfs.put("RETURNFLOWNAME", (String) orginalNodeResult[0][1]);
							lotUdfs.put("RETURNOPERATIONNAME", (String) orginalNodeResult[0][2]);

							sToBeNodeStack = originalNode; // + "." + sToBeNodeStack;
						}
					}
				}

				changeSpecInfo.setNodeStack(sToBeNodeStack);
			}

			changeSpecInfo.setUdfs(lotUdfs);
			MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
		}

	}
	
	public void completeSortFlow(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		log.info("Lot : " + lotData.getKey().getLotName() + " / ReturnFlow : " + lotData.getUdfs().get("RETURNFLOWNAME") + " / ReturnOperation : " + lotData.getUdfs().get("RETURNOPERATIONNAME"));

		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();
		changeSpecInfo.setAreaName(lotData.getAreaName());
		changeSpecInfo.setDueDate(lotData.getDueDate());
		changeSpecInfo.setFactoryName(lotData.getFactoryName());
		changeSpecInfo.setLotHoldState(lotData.getLotHoldState());
		changeSpecInfo.setLotProcessState(lotData.getLotProcessState());
		changeSpecInfo.setLotState(lotData.getLotState());
		changeSpecInfo.setNodeStack("");
		changeSpecInfo.setPriority(lotData.getPriority());
		changeSpecInfo.setProcessFlowName(lotData.getUdfs().get("RETURNFLOWNAME"));
		changeSpecInfo.setProcessFlowVersion("00001");
		changeSpecInfo.setProcessOperationName(lotData.getUdfs().get("RETURNOPERATIONNAME"));
		changeSpecInfo.setProcessOperationVersion("00001");
		changeSpecInfo.setProductionType(lotData.getProductionType());
		changeSpecInfo.setProductRequestName(lotData.getProductRequestName());
		changeSpecInfo.setProductSpec2Name(lotData.getProductSpec2Name());
		changeSpecInfo.setProductSpec2Version(lotData.getProductSpec2Version());
		changeSpecInfo.setProductSpecName(lotData.getProductSpecName());
		changeSpecInfo.setProductSpecVersion(lotData.getProductSpecVersion());
		changeSpecInfo.setProductUSequence(this.setProductUSequence(lotData.getKey().getLotName()));
		changeSpecInfo.setSubProductUnitQuantity1(lotData.getSubProductUnitQuantity1());
		changeSpecInfo.setSubProductUnitQuantity2(lotData.getSubProductUnitQuantity2());

		Map<String, String> lotUdfs = new HashMap<String, String>();

		lotUdfs.put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());
		lotUdfs.put("BEFOREFLOWNAME", lotData.getProcessFlowName());

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT NODEID, PROCESSFLOWNAME, NODEATTRIBUTE1, FACTORYNAME ");
		sql.append("  FROM NODE ");
		sql.append(" WHERE FACTORYNAME = ? ");
		sql.append("   AND PROCESSFLOWNAME = ? ");
		sql.append("   AND PROCESSFLOWVERSION = ? ");
		sql.append("   AND NODEATTRIBUTE1 = ? ");
		sql.append("   AND NODEATTRIBUTE2 = ? ");
		sql.append("   AND NODETYPE = 'ProcessOperation' ");

		Object[] bind = new Object[] { changeSpecInfo.getFactoryName(), changeSpecInfo.getProcessFlowName(), changeSpecInfo.getProcessFlowVersion(), changeSpecInfo.getProcessOperationName(),
				changeSpecInfo.getProcessOperationVersion() };

		String[][] returnNodeResult = null;
		try
		{
			returnNodeResult = GenericServiceProxy.getSqlMesTemplate().queryForStringArray(sql.toString(), bind);
		}
		catch (Exception e)
		{
		}

		if (returnNodeResult.length == 0)
		{
			throw new CustomException("Node-0001", lotData.getProductSpecName(), changeSpecInfo.getProcessFlowName(), changeSpecInfo.getProcessOperationName());
		}
		else
		{
			String sToBeNodeStack = (String) returnNodeResult[0][0];

			ProcessFlowKey processFlowKey = new ProcessFlowKey();
			processFlowKey.setFactoryName((String) returnNodeResult[0][3]);
			processFlowKey.setProcessFlowName((String) returnNodeResult[0][1]);
			processFlowKey.setProcessFlowVersion("00001");
			ProcessFlow returnFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);

			if (StringUtil.equals(returnFlowData.getProcessFlowType(), "Main") || StringUtil.equals(returnFlowData.getProcessFlowType(), "MQCPrepare"))
			{
				lotUdfs.put("RETURNFLOWNAME", "");
				lotUdfs.put("RETURNOPERATIONNAME", "");
			}
			else
			{
				String currentNode = lotData.getNodeStack();
				if (StringUtil.isNotEmpty(currentNode) && currentNode.indexOf(".") > 0)
				{
					int NodeStackCount = CommonUtil.getNodeStackCount(currentNode, '.');
					String originalNode = currentNode.substring(0, currentNode.lastIndexOf("."));

					String returnForOriginalNode = originalNode;

					if (NodeStackCount >= 3)
					{
						if (returnForOriginalNode.lastIndexOf(".") > -1)
							returnForOriginalNode = returnForOriginalNode.substring(0, returnForOriginalNode.lastIndexOf("."));

						returnForOriginalNode = returnForOriginalNode.substring(returnForOriginalNode.lastIndexOf(".") + 1, returnForOriginalNode.length());
					}
					else
					{
						if (returnForOriginalNode.lastIndexOf(".") > -1)
							returnForOriginalNode = returnForOriginalNode.substring(0, returnForOriginalNode.lastIndexOf("."));
					}

					StringBuffer sql2 = new StringBuffer();
					sql2.append("SELECT NODEID, PROCESSFLOWNAME, NODEATTRIBUTE1, FACTORYNAME ");
					sql2.append("  FROM NODE ");
					sql2.append(" WHERE NODEID = ? ");
					sql2.append("   AND NODETYPE = 'ProcessOperation' ");

					bind = new Object[] { returnForOriginalNode };

					String[][] orginalNodeResult = null;
					try
					{
						orginalNodeResult = GenericServiceProxy.getSqlMesTemplate().queryForStringArray(sql2.toString(), bind);
					}
					catch (Exception e)
					{
					}

					if (orginalNodeResult.length > 0)
					{
						lotUdfs.put("RETURNFLOWNAME", (String) orginalNodeResult[0][1]);
						lotUdfs.put("RETURNOPERATIONNAME", (String) orginalNodeResult[0][2]);

						sToBeNodeStack = originalNode; // + "." + sToBeNodeStack;
					}
				}
			}

			changeSpecInfo.setNodeStack(sToBeNodeStack);
		}

		changeSpecInfo.setUdfs(lotUdfs);
		MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
	}
	
	public void deleteSortJob(EventInfo eventInfo, List<String> srcLotList, List<String> destLotList, String jobType) throws CustomException
	{
		List<String> lotNameList = new ArrayList<String>(srcLotList);
		lotNameList.addAll(destLotList);

		for (String lotName : lotNameList)
		{
			List<Map<String, Object>> jobNameList = ExtendedObjectProxy.getSortJobService().getSortJobNameList(lotName, jobType);

			if (jobNameList.size() > 0)
			{
				for (Map<String, Object> job : jobNameList)
				{
					String jobName = ConvertUtil.getMapValueByName(job, "JOBNAME");

					ExtendedObjectProxy.getSortJobService().deleteSortJob(eventInfo, jobName);
					ExtendedObjectProxy.getSortJobProductService().deleteSortJobProduct(eventInfo, jobName);
					ExtendedObjectProxy.getSortJobCarrierService().deleteSortJobCarrier(eventInfo, jobName);
				}
			}
		}
	}
	
	public List<Map<String, Object>> getOperationByDetailOperType(String factoryname, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion,
			String detailOperType)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT TP.PROCESSOPERATIONNAME, TP.PROCESSOPERATIONVERSION, PS.DETAILPROCESSOPERATIONTYPE ");
		sql.append("  FROM TPFOPOLICY TP, PROCESSOPERATIONSPEC PS ");
		sql.append(" WHERE TP.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND TP.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("   AND TP.PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");
		sql.append("   AND TP.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND TP.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND TP.PROCESSOPERATIONNAME = PS.PROCESSOPERATIONNAME ");
		sql.append("   AND TP.PROCESSOPERATIONVERSION = PS.PROCESSOPERATIONVERSION ");
		sql.append("   AND PS.DETAILPROCESSOPERATIONTYPE = :DETAILPROCESSOPERATIONTYPE ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", factoryname);
		args.put("PRODUCTSPECNAME", productSpecName);
		args.put("PRODUCTSPECVERSION", productSpecVersion);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);
		args.put("DETAILPROCESSOPERATIONTYPE", detailOperType);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		return result;
	}
	
	public boolean isHalfCutFlag(Lot sourceLotData) throws CustomException
	{
		 ProcessOperationSpec operationSpecData = CommonUtil.getProcessOperationSpec(sourceLotData.getFactoryName(), sourceLotData.getProcessOperationName(), sourceLotData.getProcessOperationVersion());
		 boolean isHalfCutFlag = false;
		  
		 if(operationSpecData.getDetailProcessOperationType().equals("CUT") && sourceLotData.getFactoryName().equals("OLED"))
		 {
			 isHalfCutFlag = true;
		 }
		  
		 return isHalfCutFlag;
	 }
	
	public List<Map<String, Object>> getMachineDownSamplePolicyList(String factoryName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion,
			String machineName, String toProcessFlowName, String toProcessFlowVersion, String toProcessOperationName, String toProcessOperationVersion) throws CustomException
	{
		Map<String, Object> bindMap = new HashMap<String, Object>();

		String sqlAddToProcessFlowName = "";
		String sqlAddToProcessFlowVersion = "";
		String sqlAddToProcessOperationName = "";
		String sqlAddToProcessOperationVersion = "";

		if (StringUtils.isNotEmpty(toProcessFlowName))
		{
			bindMap.put("TOPROCESSFLOWNAME", toProcessFlowName);
			sqlAddToProcessFlowName = "    AND M.TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME ";
		}
		if (StringUtils.isNotEmpty(toProcessFlowVersion))
		{
			bindMap.put("TOPROCESSFLOWVERSION", toProcessFlowVersion);
			sqlAddToProcessFlowVersion = "    AND M.TOPROCESSFLOWVERSION = :TOPROCESSFLOWVERSION ";
		}
		if (StringUtils.isNotEmpty(toProcessOperationName))
		{
			bindMap.put("TOPROCESSOPERATIONNAME", toProcessOperationName);
			sqlAddToProcessOperationName = "    AND M.TOPROCESSOPERATIONNAME = :TOPROCESSOPERATIONNAME ";
		}
		if (StringUtils.isNotEmpty(toProcessOperationVersion))
		{
			bindMap.put("TOPROCESSOPERATIONVERSION", toProcessOperationVersion);
			sqlAddToProcessOperationVersion = "    AND M.TOPROCESSOPERATIONVERSION = :TOPROCESSOPERATIONVERSION ";
		}

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT T.FACTORYNAME, ");
		sql.append("       T.PROCESSFLOWNAME, ");
		sql.append("       T.PROCESSFLOWVERSION, ");
		sql.append("       T.PROCESSOPERATIONNAME, ");
		sql.append("       T.PROCESSOPERATIONVERSION, ");
		sql.append("       T.MACHINENAME, ");
		sql.append("       M.TOPROCESSFLOWNAME, ");
		sql.append("       M.TOPROCESSFLOWVERSION, ");
		sql.append("       M.TOPROCESSOPERATIONNAME, ");
		sql.append("       M.TOPROCESSOPERATIONVERSION, ");
		sql.append("       M.RETURNOPERATIONNAME, ");
		sql.append("       M.RETURNOPERATIONVER ");
		sql.append("  FROM TFOMPOLICY T, POSMACHINEDOWNSAMPLE M ");
		sql.append(" WHERE T.CONDITIONID = M.CONDITIONID ");
		sql.append("   AND T.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		sql.append("   AND T.MACHINENAME = :MACHINENAME ");
		sql.append(sqlAddToProcessFlowName);
		sql.append(sqlAddToProcessFlowVersion);
		sql.append(sqlAddToProcessOperationName);
		sql.append(sqlAddToProcessOperationVersion);

		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PROCESSFLOWNAME", processFlowName);
		bindMap.put("PROCESSFLOWVERSION", processFlowVersion);
		bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		bindMap.put("PROCESSOPERATIONVERSION", processOperationVersion);
		bindMap.put("MACHINENAME", machineName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		return sqlResult;
	}
	
	public void setMachineDownSamplingData(EventInfo eventInfo, Machine machineData, Lot lotData, Map<String, Object> machineDownSamplePolicyList) throws CustomException
	{
		String processFlowName = (String) machineDownSamplePolicyList.get("PROCESSFLOWNAME");
		String processFlowVersion = (String) machineDownSamplePolicyList.get("PROCESSFLOWVERSION");
		String processOperationName = (String) machineDownSamplePolicyList.get("PROCESSOPERATIONNAME");
		String processOperationVersion = (String) machineDownSamplePolicyList.get("PROCESSOPERATIONVERSION");
		String toProcessFlowName = (String) machineDownSamplePolicyList.get("TOPROCESSFLOWNAME");
		String toProcessFlowVersion = (String) machineDownSamplePolicyList.get("TOPROCESSFLOWVERSION");
		String toProcessOperationName = (String) machineDownSamplePolicyList.get("TOPROCESSOPERATIONNAME");
		String toProcessOperationVersion = (String) machineDownSamplePolicyList.get("TOPROCESSOPERATIONVERSION");
		String returnOperationName = (String) machineDownSamplePolicyList.get("RETURNOPERATIONNAME");
		String returnOperationVersion = (String) machineDownSamplePolicyList.get("RETURNOPERATIONVER");
		
		try
		{
			// Get TFE Down Product
			String condition = " WHERE 1 = 1 "
					   		 + "   AND LOTNAME = ? "
					   		 + "   AND FACTORYNAME = ? "
					   		 + "   AND PRODUCTSPECNAME = ? "
					   		 + "   AND PRODUCTSPECVERSION = ? "
					   		 + "   AND PROCESSFLOWNAME = ? "
					   		 + "   AND PROCESSFLOWVERSION = ? "
					   		 + "   AND PROCESSOPERATIONNAME = ? "
					   		 + "   AND PROCESSOPERATIONVERSION = ? "
					   		 + "   AND MACHINENAME = ? ";
			
			List<TFEDownProduct> downProductDataList = ExtendedObjectProxy.getTFEDownProductService().select(condition, new Object[] {
															lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(),
															lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
															machineData.getKey().getMachineName() });
			
			List<String> actualSamplePositionList = new ArrayList<String>();
			
			for (TFEDownProduct downProductData : downProductDataList) 
			{
				String productName = downProductData.getProductName();
				
				Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

				// Get SampleLotData(CT_SAMPLEPRODUCT)
				List<SampleProduct> sampleProductData = ExtendedObjectProxy.getSampleProductService().getSampleProductDataList(productName, productData.getLotName(),
						productData.getFactoryName(), productData.getProductSpecName(), productData.getProductSpecVersion(), processFlowName,
						processFlowVersion, processOperationName, processOperationVersion, machineData.getKey().getMachineName(), 
						toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);

				if (sampleProductData == null || sampleProductData.size() == 0)
				{
					ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, productName, productData.getLotName(), productData.getFactoryName(),
							productData.getProductSpecName(), productData.getProductSpecVersion(), processFlowName,
							processFlowVersion, processOperationName, processOperationVersion, machineData.getKey().getMachineName(), toProcessFlowName,
							toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion, "Y", "", "", String.valueOf(productData.getPosition()), "", "");

					actualSamplePositionList.add(String.valueOf(productData.getPosition()));
				}			
			}
			
			// Get SampleLotData(CT_SAMPLELOT)
			List<SampleLot> sampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListWithOutMachineName(lotData.getKey().getLotName(), 
					lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), processFlowName, processFlowVersion,
					processOperationName, processOperationVersion, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);

			if (sampleLot == null)
			{
				// set SampleLotData(CT_SAMPLELOT)
				ExtendedObjectProxy.getSampleLotService().insertSampleLot(eventInfo, lotData.getKey().getLotName(), 
						lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), processFlowName, processFlowVersion,
						processOperationName, processOperationVersion, lotData.getMachineName(), toProcessFlowName, toProcessFlowVersion, toProcessOperationName,
						toProcessOperationVersion, "Y", "", "", "", "", "", String.valueOf(actualSamplePositionList.size()),
						CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "", "", 0, lotData.getProcessFlowName(),
						lotData.getProcessFlowVersion(), returnOperationName, returnOperationVersion, "");
			}
			else
			{
				String actualSamplePosition = "";
				actualSamplePosition = sampleLot.get(0).getActualSamplePosition();

				if (actualSamplePositionList.size() > 0)
				{
					if (StringUtil.isEmpty(actualSamplePosition))
					{
						actualSamplePosition = CommonUtil.toStringWithoutBrackets(actualSamplePositionList);
					}
					else
					{
						actualSamplePosition = actualSamplePosition.concat(", " + CommonUtil.toStringWithoutBrackets(actualSamplePositionList));	
					}
				}

				int actualProductCount = Integer.valueOf(String.valueOf(actualSamplePositionList.size())) + Integer.valueOf(sampleLot.get(0).getActualProductCount());

				ExtendedObjectProxy.getSampleLotService().updateSampleLot(eventInfo, lotData.getKey().getLotName(), sampleLot.get(0).getFactoryName(), sampleLot.get(0).getProductSpecName(),
						sampleLot.get(0).getProductSpecVersion(), sampleLot.get(0).getProcessFlowName(), sampleLot.get(0).getProcessFlowVersion(), sampleLot.get(0).getProcessOperationName(),
						sampleLot.get(0).getProcessOperationVersion(), machineData.getKey().getMachineName(), sampleLot.get(0).getToProcessFlowName(), sampleLot.get(0).getToProcessFlowVersion(),
						sampleLot.get(0).getToProcessOperationName(), sampleLot.get(0).getToProcessOperationVersion(), sampleLot.get(0).getLotSampleFlag(), sampleLot.get(0).getLotSampleCount(),
						sampleLot.get(0).getCurrentLotCount(), sampleLot.get(0).getTotalLotCount(), sampleLot.get(0).getProductSampleCount(), sampleLot.get(0).getProductSamplePosition(),
						String.valueOf(actualProductCount), actualSamplePosition, sampleLot.get(0).getManualSampleFlag(), "", sampleLot.get(0).getPriority().toString(),
						sampleLot.get(0).getReturnProcessFlowName(), sampleLot.get(0).getReturnProcessFlowVersion(), sampleLot.get(0).getReturnOperationName(),
						sampleLot.get(0).getReturnOperationVersion(), "");
			}
		}
		catch (NotFoundSignal nfs)
		{
			log.info("+++ TFEDown Product Data is not exists");
			return ;
		}
		catch (Exception ex)
		{
			log.info("+++ setMachineDownSamplingData Fail : " + ex.getCause());
			return ;
		}
	}
	
	@SuppressWarnings("unchecked")
	public void setTFECVDChamberSamplingData(EventInfo eventInfo, Machine machineData, Lot lotData, Map<String, Object> chamberSamplePolicy) throws CustomException
	{
		// Get Chamber History
		String sql = "SELECT H.PRODUCTNAME, M.MACHINEGROUPNAME, H.MATERIALLOCATIONNAME "
				   + "  FROM CT_COMPONENTINCHAMBERHIST H, "
				   + "  ( "
				   + "    SELECT MS.MACHINENAME, MS.MACHINEGROUPNAME "
				   + "      FROM MACHINESPEC MS, MACHINESPEC US "
				   + "     WHERE 1 = 1 "
				   + "       AND MS.CHAMBERYN = 'Y' "
				   + "       AND US.MACHINEGROUPNAME IN('CVD1','CVD2','IJP') "
				   + "       AND US.SUPERMACHINENAME = :MACHINENAME "
				   + "       AND US.MACHINENAME = MS.SUPERMACHINENAME "
				   + "  ) M "
				   + " WHERE 1 = 1 "
				   + "   AND H.LOTNAME = :LOTNAME "
				   + "   AND H.EVENTNAME = 'ComponentOutSubUnit' "
				   + "   AND H.FACTORYNAME = :FACTORYNAME "
				   + "   AND H.PRODUCTSPECNAME = :PRODUCTSPECNAME "
				   + "   AND H.PRODUCTSPECVERSION = :PRODUCTSPECVERSION "
				   + "   AND H.PROCESSFLOWNAME = :PROCESSFLOWNAME "
				   + "   AND H.PROCESSFLOWVERSION = :PROCESSFLOWVERSION "
				   + "   AND H.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME "
				   + "   AND H.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION "
				   + "   AND H.MACHINENAME = :MACHINENAME "
				   + "   AND M.MACHINENAME = H.MATERIALLOCATIONNAME ";
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("LOTNAME", lotData.getKey().getLotName());
		bindMap.put("FACTORYNAME", lotData.getFactoryName());
		bindMap.put("PRODUCTSPECNAME", lotData.getProductSpecName());
		bindMap.put("PRODUCTSPECVERSION", lotData.getProductSpecVersion());
		bindMap.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
		bindMap.put("PROCESSFLOWVERSION", lotData.getProcessFlowVersion());
		bindMap.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
		bindMap.put("PROCESSOPERATIONVERSION", lotData.getProcessOperationVersion());
		bindMap.put("MACHINENAME", machineData.getKey().getMachineName());
		
		List<Map<String, Object>> resultDataList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		if (resultDataList == null || resultDataList.size() == 0)
		{
			log.info("+++ Chamber History Data is not exists");
			return ;
		}
		
		List<String> productNameList = new ArrayList<String>();
		
		// Map<ProductName, ChamberName>
		Map<String, String> chamberDataMap = new HashMap<String, String>();
		
		// List<ProductName>
		List<String> ijpDataList1 = new ArrayList<String>();
		List<String> ijpDataList2 = new ArrayList<String>();

		for (Map<String, Object> resultData : resultDataList) 
		{
			String productName = resultData.get("PRODUCTNAME").toString();
			String machineGroupName = resultData.get("MACHINEGROUPNAME").toString();
			String materialLocationName = resultData.get("MATERIALLOCATIONNAME").toString();
			
			if (productNameList.indexOf(productName) == -1)
			{
				productNameList.add(productName);
			}
			
			if ("CVD1".equals(machineGroupName) ||
				"CVD2".equals(machineGroupName))
			{
				boolean bSkip = false;
				for (String productNameKey : chamberDataMap.keySet()) 
				{
					if (chamberDataMap.get(productNameKey).equals(materialLocationName) ||
						productNameKey.equals(productName))
					{
						bSkip = true;
						break;
					}
				}
				
				if (bSkip)
				{
					continue;
				}
				
				chamberDataMap.put(productName, materialLocationName);
			}
			else if ("IJP".equals(machineGroupName))
			{
				if (materialLocationName.contains("PR1") &&
					ijpDataList1.indexOf(productName) == -1)
				{
					ijpDataList1.add(productName);
				}
				else if (materialLocationName.contains("PR2") &&
						 ijpDataList2.indexOf(productName) == -1)
				{
					ijpDataList2.add(productName);
				}
			}
		}
		
		List<String> sampleProductNameList = new ArrayList<>(chamberDataMap.keySet());
		List<String> actualSamplePositionList = new ArrayList<String>();
		
		String sampleMaxCount = StringUtils.EMPTY;
		List<Map<String, Object>> sampleMaxCountDataList = CommonUtil.getEnumDefValueByEnumName("TFECVDSamplingMaxGlassCount");
		if (sampleMaxCountDataList != null && sampleMaxCountDataList.size() > 0)
		{
			sampleMaxCount = sampleMaxCountDataList.get(0).get("ENUMVALUE").toString();
		}
		sampleProductNameList = this.calculateIJPRate(productNameList, sampleProductNameList, ijpDataList1, ijpDataList2, sampleMaxCount);
		
		if (sampleProductNameList.size() > 0)
		{
			for (String productName : sampleProductNameList)
			{
				Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

				// Get SampleLotData(CT_SAMPLEPRODUCT)
				List<SampleProduct> sampleProductData = ExtendedObjectProxy.getSampleProductService().getSampleProductDataList(productName, productData.getLotName(),
						productData.getFactoryName(), productData.getProductSpecName(), productData.getProductSpecVersion(), (String) chamberSamplePolicy.get("PROCESSFLOWNAME"),
						(String) chamberSamplePolicy.get("PROCESSFLOWVERSION"), (String) chamberSamplePolicy.get("PROCESSOPERATIONNAME"),
						(String) chamberSamplePolicy.get("PROCESSOPERATIONVERSION"), machineData.getKey().getMachineName(), (String) chamberSamplePolicy.get("TOPROCESSFLOWNAME"),
						(String) chamberSamplePolicy.get("TOPROCESSFLOWVERSION"), (String) chamberSamplePolicy.get("TOPROCESSOPERATIONNAME"),
						(String) chamberSamplePolicy.get("TOPROCESSOPERATIONVERSION"));

				if (sampleProductData == null || sampleProductData.size() == 0)
				{
					ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, productName, productData.getLotName(), productData.getFactoryName(),
							productData.getProductSpecName(), productData.getProductSpecVersion(), (String) chamberSamplePolicy.get("PROCESSFLOWNAME"),
							(String) chamberSamplePolicy.get("PROCESSFLOWVERSION"), (String) chamberSamplePolicy.get("PROCESSOPERATIONNAME"),
							(String) chamberSamplePolicy.get("PROCESSOPERATIONVERSION"), machineData.getKey().getMachineName(), (String) chamberSamplePolicy.get("TOPROCESSFLOWNAME"),
							(String) chamberSamplePolicy.get("TOPROCESSFLOWVERSION"), (String) chamberSamplePolicy.get("TOPROCESSOPERATIONNAME"),
							(String) chamberSamplePolicy.get("TOPROCESSOPERATIONVERSION"), "Y", "", "", String.valueOf(productData.getPosition()), "", "");

					actualSamplePositionList.add(String.valueOf(productData.getPosition()));
				}
			}
			
			// Get SampleLotData(CT_SAMPLELOT)
			List<SampleLot> sampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListWithOutMachineName(lotData.getKey().getLotName(), 
					lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), (String) chamberSamplePolicy.get("PROCESSFLOWNAME"), (String) chamberSamplePolicy.get("PROCESSFLOWVERSION"),
					(String) chamberSamplePolicy.get("PROCESSOPERATIONNAME"), (String) chamberSamplePolicy.get("PROCESSOPERATIONVERSION"), (String) chamberSamplePolicy.get("TOPROCESSFLOWNAME"),
					(String) chamberSamplePolicy.get("TOPROCESSFLOWVERSION"), (String) chamberSamplePolicy.get("TOPROCESSOPERATIONNAME"), (String) chamberSamplePolicy.get("TOPROCESSOPERATIONVERSION"));
	
			if (sampleLot == null)
			{
				// set SampleLotData(CT_SAMPLELOT)
				ExtendedObjectProxy.getSampleLotService().insertSampleLot(eventInfo, lotData.getKey().getLotName(), 
						lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), (String) chamberSamplePolicy.get("PROCESSFLOWNAME"), (String) chamberSamplePolicy.get("PROCESSFLOWVERSION"),
						(String) chamberSamplePolicy.get("PROCESSOPERATIONNAME"), (String) chamberSamplePolicy.get("PROCESSOPERATIONVERSION"), lotData.getMachineName(), (String) chamberSamplePolicy.get("TOPROCESSFLOWNAME"),
						(String) chamberSamplePolicy.get("TOPROCESSFLOWVERSION"), (String) chamberSamplePolicy.get("TOPROCESSOPERATIONNAME"),
						(String) chamberSamplePolicy.get("TOPROCESSOPERATIONVERSION"), "Y", "", "", "", "", "", String.valueOf(actualSamplePositionList.size()),
						CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "", "", Integer.valueOf(chamberSamplePolicy.get("FLOWPRIORITY").toString()), lotData.getProcessFlowName(),
						lotData.getProcessFlowVersion(), (String) chamberSamplePolicy.get("RETURNOPERATIONNAME"), (String) chamberSamplePolicy.get("RETURNOPERATIONVER"), "");
			}
			else
			{
				String actualSamplePosition = "";
				actualSamplePosition = sampleLot.get(0).getActualSamplePosition();
	
				if (actualSamplePositionList.size() > 0)
				{
					if (StringUtil.isEmpty(actualSamplePosition))
					{
						actualSamplePosition = CommonUtil.toStringWithoutBrackets(actualSamplePositionList);
					}
					else
					{
						actualSamplePosition = actualSamplePosition.concat(", " + CommonUtil.toStringWithoutBrackets(actualSamplePositionList));	
					}
				}
	
				int actualProductCount = Integer.valueOf(String.valueOf(actualSamplePositionList.size())) + Integer.valueOf(sampleLot.get(0).getActualProductCount());
	
				ExtendedObjectProxy.getSampleLotService().updateSampleLot(eventInfo, lotData.getKey().getLotName(), sampleLot.get(0).getFactoryName(), sampleLot.get(0).getProductSpecName(),
						sampleLot.get(0).getProductSpecVersion(), sampleLot.get(0).getProcessFlowName(), sampleLot.get(0).getProcessFlowVersion(), sampleLot.get(0).getProcessOperationName(),
						sampleLot.get(0).getProcessOperationVersion(), machineData.getKey().getMachineName(), sampleLot.get(0).getToProcessFlowName(), sampleLot.get(0).getToProcessFlowVersion(),
						sampleLot.get(0).getToProcessOperationName(), sampleLot.get(0).getToProcessOperationVersion(), sampleLot.get(0).getLotSampleFlag(), sampleLot.get(0).getLotSampleCount(),
						sampleLot.get(0).getCurrentLotCount(), sampleLot.get(0).getTotalLotCount(), sampleLot.get(0).getProductSampleCount(), sampleLot.get(0).getProductSamplePosition(),
						String.valueOf(actualProductCount), actualSamplePosition, sampleLot.get(0).getManualSampleFlag(), "", sampleLot.get(0).getPriority().toString(),
						sampleLot.get(0).getReturnProcessFlowName(), sampleLot.get(0).getReturnProcessFlowVersion(), sampleLot.get(0).getReturnOperationName(),
						sampleLot.get(0).getReturnOperationVersion(), "");
			}	
		}
	}
	
	public List<String> calculateIJPRate(List<String> productNameList, List<String> sampleProductNameList, List<String> ijpDataList1, List<String> ijpDataList2, String sampleMaxCount)
	{
		double ijp1 = 0;
		double ijp2 = 0;
		for (String productName : sampleProductNameList) 
		{
			if (ijpDataList1.indexOf(productName) > -1)
			{
				ijp1++;
			}
			
			if (ijpDataList2.indexOf(productName) > -1)
			{
				ijp2++;
			}
		}
		
		double ijpRate1 = Double.valueOf(ijp1/(ijp1+ijp2)*100);
		if (ijpRate1 >= 30 && ijpRate1 <= 70)
		{
			return sampleProductNameList;
		}
		
		if (StringUtils.isEmpty(sampleMaxCount))
		{
			if (ijpRate1 < 30)
			{
				boolean isExist = false;
				for (String productName : productNameList) 
				{
					if (ijpDataList1.indexOf(productName) > -1 && 
						sampleProductNameList.indexOf(productName) == -1)
					{
						isExist = true;
						sampleProductNameList.add(productName);
						break;
					}
				}
				
				if (!isExist)
				{
					return sampleProductNameList;
				}
				
				this.calculateIJPRate(productNameList, sampleProductNameList, ijpDataList1, ijpDataList2, sampleMaxCount);
			}
			else if (ijpRate1 > 70)
			{
				boolean isExist = false;
				for (String productName : productNameList) 
				{
					if (ijpDataList2.indexOf(productName) > -1 && 
						sampleProductNameList.indexOf(productName) == -1)
					{
						isExist = true;
						sampleProductNameList.add(productName);
						break;
					}
				}
				
				if (!isExist)
				{
					return sampleProductNameList;
				}
				
				this.calculateIJPRate(productNameList, sampleProductNameList, ijpDataList1, ijpDataList2, sampleMaxCount);
			}
		}
		else
		{
			if (ijp1 + ijp2 >= Double.valueOf(sampleMaxCount))
			{
				return sampleProductNameList;
			}
			
			if (ijpRate1 < 30)
			{
				boolean isExist = false;
				for (String productName : productNameList) 
				{
					if (ijpDataList1.indexOf(productName) > -1 && 
						sampleProductNameList.indexOf(productName) == -1)
					{
						isExist = true;
						sampleProductNameList.add(productName);
						break;
					}
				}
				
				if (!isExist)
				{
					for (String productName : productNameList) 
					{
						if (ijpDataList2.indexOf(productName) > -1 && 
							sampleProductNameList.indexOf(productName) == -1)
						{
							isExist = true;
							sampleProductNameList.add(productName);
							break;
						}
					}
					
					if (!isExist)
					{
						return sampleProductNameList;	
					}
				}
				
				this.calculateIJPRate(productNameList, sampleProductNameList, ijpDataList1, ijpDataList2, sampleMaxCount);
			}
			
			if (ijpRate1 > 70)
			{
				boolean isExist = false;
				for (String productName : productNameList) 
				{
					if (ijpDataList2.indexOf(productName) > -1 && 
						sampleProductNameList.indexOf(productName) == -1)
					{
						isExist = true;
						sampleProductNameList.add(productName);
						break;
					}
				}
				
				if (!isExist)
				{
					for (String productName : productNameList) 
					{
						if (ijpDataList1.indexOf(productName) > -1 && 
							sampleProductNameList.indexOf(productName) == -1)
						{
							isExist = true;
							sampleProductNameList.add(productName);
							break;
						}
					}
					
					if (!isExist)
					{
						return sampleProductNameList;	
					}
				}
				
				this.calculateIJPRate(productNameList, sampleProductNameList, ijpDataList1, ijpDataList2, sampleMaxCount);
			}
		}
		
		return sampleProductNameList;
	}
	public double changeToSAPProductRequestQty(ProductRequest productRequestData,double productQuantity,ProductSpec productSpecData)
	{
		String factoryName=productRequestData.getFactoryName();
		String productType=productRequestData.getUdfs().get("PRODUCTTYPE");
		if(StringUtil.equals(productType, "SHT")&&StringUtil.equals(factoryName, "ARRAY"))
		{
			productQuantity=productQuantity;
		}
		else if(StringUtil.equals(productType, "SHT")&&(StringUtil.equals(factoryName, "OLED")||StringUtil.equals(factoryName, "TP")))
		{
			productQuantity=productQuantity*2;
		}
		else if (StringUtil.equals(productType, "SHT")&&(StringUtil.equals(factoryName, "POSTCELL")||StringUtil.equals(factoryName, "MODULE"))) 
		{
			productQuantity=productQuantity*Long.parseLong(productSpecData.getUdfs().get("PRODUCTCOUNTTOXAXIS"))*Long.parseLong(productSpecData.getUdfs().get("PRODUCTCOUNTTOYAXIS"));
		}
		else if (StringUtil.equals(productType, "PCS")&&StringUtil.equals(factoryName, "ARRAY")) 
		{
			productQuantity=productQuantity/Long.parseLong(productSpecData.getUdfs().get("PRODUCTCOUNTTOXAXIS"))/Long.parseLong(productSpecData.getUdfs().get("PRODUCTCOUNTTOYAXIS"));
		}
		else if (StringUtil.equals(productType, "PCS")&&(StringUtil.equals(factoryName, "OLED")||StringUtil.equals(factoryName, "TP")))
		{
			productQuantity=productQuantity/Long.parseLong(productSpecData.getUdfs().get("PRODUCTCOUNTTOXAXIS"))/Long.parseLong(productSpecData.getUdfs().get("PRODUCTCOUNTTOYAXIS"))*2;
		}
		else if (StringUtil.equals(productType, "PCS")&&(StringUtil.equals(factoryName, "POSTCELL")||StringUtil.equals(factoryName, "MODULE"))) 
		{
			productQuantity=productQuantity;
		}
		return productQuantity;
	}
	
	public void setSamplingListDataForBackUpEQP(EventInfo eventInfo, Lot lotData, Machine machineData, List<Element> productListElement) throws CustomException
	{
		eventInfo = EventInfoUtil.makeEventInfo("Sampling", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineData.getKey().getMachineName());
		
		if(StringUtils.equals(machineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_ProductionMachine))
		{		
			// get all sampling Flow List (joined TFOMPOLICY & POSSAMPLE)
			List<Map<String, Object>> samplePolicyList = MESLotServiceProxy.getLotServiceUtil().getSamplePolicyList(lotData.getFactoryName(), lotData.getUdfs().get("BACKUPMAINFLOWNAME"),
					lotData.getProcessFlowVersion(), lotData.getUdfs().get("BACKUPMAINOPERNAME"), lotData.getProcessOperationVersion(), lotData.getMachineName(), "", "", "", "");
	
			for (Map<String, Object> samplePolicyM : samplePolicyList)
			{
				if (MESLotServiceProxy.getLotServiceUtil().checkReserveSampling(lotData, samplePolicyM))
				{
					List<Element> newProductListElement = CommonUtil.makeElementProdListForDummy(productListElement);
					newProductListElement = CommonUtil.makeElementProdListForFirstGlass(productListElement,lotData.getKey().getLotName());
					MESLotServiceProxy.getLotServiceUtil().setSamplingData(eventInfo, lotData, samplePolicyM, newProductListElement);
				}
			}
		}
	}
	
	public void deleteFutureActionData (EventInfo eventInfo, List<SampleLot> sampleLotList, Lot beforeTrackOutLot, Lot afterTrackOutLot) throws CustomException
	{
		log.info(" Start deleteFutureActionData (TrackOut) ");
		
		eventInfo.setEventName("Delete");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		// Get ProcessFlowData
		ProcessFlow beforeProcessFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(beforeTrackOutLot);
		ProcessFlow afterProcessFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(afterTrackOutLot);
		
		// When sample operation, remove future data.
		ExtendedObjectProxy.getLotFutureActionService().deleteLotFutureActionWithoutReaconCode(
				eventInfo, beforeTrackOutLot.getKey().getLotName(), beforeTrackOutLot.getFactoryName(), 
					beforeTrackOutLot.getProcessFlowName(), beforeTrackOutLot.getProcessFlowVersion(), beforeTrackOutLot.getProcessOperationName(), beforeTrackOutLot.getProcessOperationVersion(), 0);
		
		
		
		// When arrive main operation, remove remaining future data.
		if( StringUtils.equals(afterProcessFlowData.getProcessFlowType(), "Main"))
		{
			String factoryName = "";
			String processFlowName = "";
			String processFlowVersion = "";
			String processOperationName = "";
			String processOperationVersion = "";

			if(StringUtils.equals(beforeProcessFlowData.getProcessFlowType(), "Main"))
			{
				factoryName = beforeTrackOutLot.getFactoryName();
				processFlowName = beforeTrackOutLot.getProcessFlowName();
				processFlowVersion = beforeTrackOutLot.getProcessFlowVersion();
				processOperationName = beforeTrackOutLot.getProcessOperationName();
				processOperationVersion = beforeTrackOutLot.getProcessOperationVersion();
			}
			else //When the operation that arrived is not Main.
			{
				if (sampleLotList != null && sampleLotList.size() > 0)
				{
					for (SampleLot sampleLot : sampleLotList)
					{
						factoryName = sampleLot.getFactoryName();
						processFlowName = sampleLot.getProcessFlowName();
						processFlowVersion = sampleLot.getProcessFlowVersion();
						processOperationName = sampleLot.getProcessOperationName();
						processOperationVersion = sampleLot.getProcessOperationVersion();
					}
				}
			}
			
			if(StringUtils.isNotEmpty(processFlowName))
			{
				Map<String, String> bindMap = new HashMap<String, String>();

				StringBuilder sql = new StringBuilder();
				sql.append(" SELECT DISTINCT LF.PROCESSOPERATIONNAME, LF.ACTIONNAME, LF.PROCESSFLOWNAME, LF.LOTNAME, LF.FACTORYNAME, LF.PROCESSFLOWVERSION, LF.PROCESSOPERATIONVERSION");
				sql.append("  FROM CT_LOTFUTUREACTION LF,");
				sql.append("  (SELECT TP.FACTORYNAME, PS.CONDITIONID, PS.TOPROCESSFLOWNAME, PS.TOPROCESSFLOWVERSION,");
				sql.append("  PS.TOPROCESSOPERATIONNAME, PS.TOPROCESSOPERATIONVERSION");
				sql.append("  FROM TFOMPOLICY TP, POSSAMPLE PS");
				sql.append("  WHERE 1 = 1");
				sql.append("  AND TP.FACTORYNAME = :FACTORYNAME");
				sql.append("  AND TP.PROCESSFLOWNAME = :PROCESSFLOWNAME");
				sql.append("  AND TP.PROCESSFLOWVERSION = :PROCESSFLOWVERSION");
				sql.append("  AND TP.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME");
				sql.append("  AND TP.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION");
				sql.append("  AND PS.CONDITIONID = TP.CONDITIONID");
				sql.append("  ) S" );
				sql.append(" WHERE 1 = 1");
				sql.append("   AND LF.LOTNAME = :LOTNAME");
				sql.append("   AND S.FACTORYNAME = LF.FACTORYNAME");
				sql.append("   AND S.TOPROCESSFLOWNAME = LF.PROCESSFLOWNAME");
				sql.append("   AND S.TOPROCESSFLOWVERSION = LF.PROCESSFLOWVERSION");
				//sql.append("   AND S.TOPROCESSOPERATIONNAME = LF.PROCESSOPERATIONNAME");
				//sql.append("   AND S.TOPROCESSOPERATIONVERSION = LF.PROCESSOPERATIONVERSION");

				bindMap.put("FACTORYNAME", factoryName);
				bindMap.put("PROCESSFLOWNAME", processFlowName);
				bindMap.put("PROCESSFLOWVERSION", processFlowVersion);
				bindMap.put("PROCESSOPERATIONNAME", processOperationName);
				bindMap.put("PROCESSOPERATIONVERSION", processOperationVersion);
				bindMap.put("LOTNAME", afterTrackOutLot.getKey().getLotName());

				@SuppressWarnings("unchecked")
				List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
				
				if (sqlResult.size() > 0)
				{
					for (Map<String, Object> resultData : sqlResult)
					{
						ExtendedObjectProxy.getLotFutureActionService().deleteLotFutureActionWithoutReaconCode(
								eventInfo, resultData.get("LOTNAME").toString(), resultData.get("FACTORYNAME").toString(), 
									resultData.get("PROCESSFLOWNAME").toString(), resultData.get("PROCESSFLOWVERSION").toString(), 
										resultData.get("PROCESSOPERATIONNAME").toString(), resultData.get("PROCESSOPERATIONVERSION").toString(), 0);
					}
				}
			}
		}
		
		log.info(" End deleteFutureActionData (TrackOut) ");
	}
	
	public Lot checkProductJudgeAndHold(EventInfo eventInfo, List<Element> productList, Lot lotData) throws CustomException
	{
		boolean checkFlag = false;
		String productName = "";
		String productJudge = "";
		
		for (Element product : productList)
		{
			productName = SMessageUtil.getChildText(product, "PRODUCTNAME", true);
			productJudge = SMessageUtil.getChildText(product, "PRODUCTJUDGE", true);

			if(StringUtils.equals(productJudge, GenericServiceProxy.getConstantMap().ProductGrade_S))
			{
				checkFlag = true;
				break;
			}
		}

		if(checkFlag)
		{
			eventInfo.setEventName("Hold");
			eventInfo.setReasonCode("SYSTEM");
			eventInfo.setEventComment("Product("+ productName +") Judge is (" +productJudge+ "). Hold Lot("+lotData.getKey().getLotName()+") // " + eventInfo.getEventComment());

			Map<String, String> udfs = new HashMap<String, String>();

			if (StringUtil.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Released) && StringUtil.equals(lotData.getLotProcessState(), "WAIT"))
			{
				MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, lotData, udfs);
				lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getKey().getLotName());
			}
			else
			{
				throw new CustomException("LOT-0113", lotData.getLotState(), lotData.getLotProcessState());
			}
		}
		
		return lotData;
	}
	
	// sync productFutureData
	public boolean checkProductFutureActionData(String productName) throws CustomException
	{
		List<ProductFutureAction> lotFutureActionData = ExtendedObjectProxy.getProductFutureActionService().getProductFutureActionDataByProductName(productName);

		if (lotFutureActionData != null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	// sync productFutureData
	public List<String> makeProductNameList (List<String> lotNameList) throws CustomException
	{
		List<String> productNameList = new ArrayList<String>();
		
		if (lotNameList.size() > 0 )
		{
			for (String lotName : lotNameList)
			{
				List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);
				
				for (Product product : productList)
				{
					String productName = product.getKey().getProductName();
					productNameList.add(productName);
				}
			}
		}
		
		return productNameList;
	}
	
	public void executePostActionByPrdocutFutureHold(EventInfo eventInfo, Lot postLotData,Lot afterTrackOutLot,List<String> productNameList) throws CustomException
	{
		log.info("Start executePostActionByPrdocutFutureHold");
		String newEventComment="SPCHOLD: {";
		boolean reserveFlag=false;//spcHold Flag(站后Hold)
		
		for(String productName :productNameList)
		{
			try
			{
				List<ProductFutureAction> actionDataList =ExtendedObjectProxy.getProductFutureActionService().select(" PRODUCTNAME=? AND FACTORYNAME=? AND PROCESSFLOWNAME=? "
						+ " AND PROCESSFLOWVERSION=? AND PROCESSOPERATIONNAME=? AND PROCESSOPERATIONVERSION=? AND REASONCODETYPE!='RS-DE'  AND LASTEVENTUSER = 'SPC' ", new Object[]{
								productName,postLotData.getFactoryName(),postLotData.getProcessFlowName(),postLotData.getProcessFlowVersion(),
								postLotData.getProcessOperationName(),postLotData.getProcessOperationVersion()});
				for(ProductFutureAction actionData:actionDataList)
				{
					newEventComment+=actionData.getLastEventComment();
					/*if(StringUtils.equals(postLotData.getFactoryName(), "OLED"))
					{
						newEventComment+=actionData.getLastEventComment();
					}
					else
					{
						newEventComment+=productName+",";
					}*/
					
				}
				reserveFlag=true;
			}
			catch(greenFrameDBErrorSignal n)
			{
				log.info("Not Found ProductFutureActoin(SPC 预约Hold): "+productName);
			}
			
			try
			{
				List<ProductFutureAction> actionDataList =ExtendedObjectProxy.getProductFutureActionService().select(" PRODUCTNAME=? AND FACTORYNAME=? AND PROCESSFLOWNAME=? "
						+ " AND PROCESSFLOWVERSION=? AND PROCESSOPERATIONNAME=? AND PROCESSOPERATIONVERSION=? AND REASONCODETYPE!='RS-DE'  AND LASTEVENTUSER != 'SPC' ", new Object[]{
								productName,postLotData.getFactoryName(),postLotData.getProcessFlowName(),postLotData.getProcessFlowVersion(),
								postLotData.getProcessOperationName(),postLotData.getProcessOperationVersion()});

				for(ProductFutureAction actionData:actionDataList)
				{
					String newEventCommentOver = actionData.getReasonCodeType() + actionData.getLastEventComment();
					
					try
					{
						if(newEventCommentOver.length()>=3950)
						{
							newEventCommentOver = "Comment Over 4000 bytes! " + newEventCommentOver.substring(0, 3900);
						}
						
						EventInfo eventInfoForOverTime = (EventInfo) ObjectUtil.copyTo(eventInfo);
						eventInfoForOverTime.setEventComment(newEventCommentOver);
						ExtendedObjectProxy.getLotFutureActionService().insertLotFutureAction(eventInfoForOverTime, postLotData.getKey().getLotName(), postLotData.getFactoryName(), postLotData.getProcessFlowName(),
								postLotData.getProcessFlowVersion(), postLotData.getProcessOperationName(), postLotData.getProcessOperationVersion(), 0, actionData.getReasonCode(), "HOLD", "hold", "System", "",
								"", "", "False", "True", "", eventInfoForOverTime.getEventComment(), "", eventInfoForOverTime.getEventUser(), "", "", "");
					}
		            catch(Exception e)
					{
		            	log.error("End executePostActionByPrdocutFutureHold :Insert LotFutureAction failed");
					}
				}
			}
			catch(greenFrameDBErrorSignal n)
			{
				log.info("Not Found ProductFutureActoin(Normal 预约Hold): "+productName);
			}
		}
		newEventComment+="}";
		if(newEventComment.length()>=3950)
		{
			newEventComment="SPC Comment Over 4000bytes,Please confirm by email"+newEventComment.substring(0, 3900);
		}
		if(reserveFlag)
		{
			try
			{
				EventInfo eventInfoForSPC = (EventInfo) ObjectUtil.copyTo(eventInfo);
				eventInfoForSPC.setEventComment(newEventComment);
				eventInfoForSPC.setEventUser("SPC");
				ExtendedObjectProxy.getLotFutureActionService().insertLotFutureAction(eventInfoForSPC, postLotData.getKey().getLotName(), postLotData.getFactoryName(), postLotData.getProcessFlowName(),
						postLotData.getProcessFlowVersion(), postLotData.getProcessOperationName(), postLotData.getProcessOperationVersion(), 0, "SPC", "HOLD", "hold", "System", "",
						"", "", "False", "True", "", eventInfoForSPC.getEventComment(), "", eventInfoForSPC.getEventUser(), "", "", "");
			}
            catch(Exception e)
			{
            	log.error("End executePostActionByPrdocutFutureHold :Insert LotFutureAction failed");
			}
		}
		
		//if RSMeter SPCHold,then SPCHOLD DryEtch by PVD SunHuiYan
		if(!StringUtils.equals(postLotData.getFactoryName(), "ARRAY")||!reserveFlag)
		{
			return;
		}
		log.info("executePostActionByPrdocutFutureHold: RS-DE ");
		newEventComment="SPCHOLD For RS Meter：{";
		reserveFlag=false;
		String dryOper="";
		ProcessFlow mainFlowData = MESLotServiceProxy.getLotInfoUtil().getMainProcessFlowData(postLotData);
		String sqlForDEOper= "SELECT N.NODEATTRIBUTE1,N.PROCESSFLOWNAME FROM ENUMDEFVALUE EN,NODE N "
				+ " WHERE EN.ENUMVALUE=:ENUMVALUE "
				+ " AND EN.DESCRIPTION=N.NODEATTRIBUTE1  "
				+ " AND N.PROCESSFLOWNAME=:PROCESSFLOWNAME "
		        + " AND EN.ENUMNAME='SPCRSOperMappling' ";
			
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("ENUMVALUE",postLotData.getProcessOperationName());
		bindMap.put("PROCESSFLOWNAME",mainFlowData.getKey().getProcessFlowName());
		
		List<Map<String, Object>> DEOperList = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlForDEOper, bindMap);
		if(DEOperList!=null&&DEOperList.size()>0)
		{
			dryOper=DEOperList.get(0).get("NODEATTRIBUTE1").toString();
		}
		else
		{
			return;
		}
		for(String productName :productNameList)
		{
			try
			{
				List<ProductFutureAction> actionDataList =ExtendedObjectProxy.getProductFutureActionService().select(" PRODUCTNAME=? AND FACTORYNAME=? AND PROCESSFLOWNAME=? "
						+ " AND PROCESSFLOWVERSION=? AND PROCESSOPERATIONNAME=? AND PROCESSOPERATIONVERSION=? AND REASONCODETYPE='RS-DE' ", new Object[]{
								productName,afterTrackOutLot.getFactoryName(),mainFlowData.getKey().getProcessFlowName(),"00001",dryOper,"00001"});
				for(ProductFutureAction actionData:actionDataList)
				{
					newEventComment+=productName;					
				}
				reserveFlag=true;
				for(ProductFutureAction productAction:actionDataList)
				{
					ExtendedObjectProxy.getProductFutureActionService().remove(eventInfo, productAction);
				}
			}
			catch(greenFrameDBErrorSignal n)
			{
				log.info("Not Found ProductFutureActoin: "+productName);
			}
		}
		newEventComment+="}";
		if(reserveFlag)
		{
			try
			{
				EventInfo eventInfoForSPC = (EventInfo) ObjectUtil.copyTo(eventInfo);
				eventInfoForSPC.setEventComment(newEventComment);
				eventInfoForSPC.setEventUser("SPC");
				ExtendedObjectProxy.getLotFutureActionService().insertLotFutureAction(eventInfoForSPC, afterTrackOutLot.getKey().getLotName(), afterTrackOutLot.getFactoryName(), mainFlowData.getKey().getProcessFlowName(),
						"00001",dryOper, "00001", 0, "SPC-RS", "HOLD", "hold", "System", "",
						"", "", "True", "False", eventInfoForSPC.getEventComment(), "", eventInfoForSPC.getEventUser(), "", "", "", "");
			}
            catch(Exception e)
			{
            	log.error("End executePostActionByPrdocutFutureHold :Insert LotFutureAction failed");
			}
		}
		log.info("End executePostActionByPrdocutFutureHold");

		return ;
	}
	
	public void deleteProductFutureActionData (EventInfo eventInfo, List<SampleLot> sampleLotList, Lot beforeTrackOutLot, Lot afterTrackOutLot) throws CustomException
	{
		log.info(" Start deleteProductFutureActionData (TrackOut) ");
		
		try
		{
			List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(afterTrackOutLot.getKey().getLotName());
			for (Product productData : productList)
			{
				if(checkProductFutureActionData(productData.getKey().getProductName()))
				{	
					eventInfo.setEventName("Delete");
					eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
					
					// Get ProcessFlowData
					ProcessFlow beforeProcessFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(beforeTrackOutLot);
					ProcessFlow afterProcessFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(afterTrackOutLot);
					
					// When sample operation, remove future data.
					ExtendedObjectProxy.getProductFutureActionService().deleteProductFutureActionWithoutReaconCode(
							eventInfo, productData.getKey().getProductName(), beforeTrackOutLot.getFactoryName(), 
								beforeTrackOutLot.getProcessFlowName(), beforeTrackOutLot.getProcessFlowVersion(), beforeTrackOutLot.getProcessOperationName(), beforeTrackOutLot.getProcessOperationVersion());
					
				}
			}		
			log.info(" End deleteFutureActionData (TrackOut) ");
		}		
        catch(FrameworkErrorSignal f)
		{
        	return;
		}
		catch(NotFoundSignal n)
		{
			return;
		}
		

	}
	
	public List<Map<String, Object>> getOLEDTPSortJobData(String lotName,String durableName) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT J.JOBNAME, J.JOBSTATE, C.TRANSFERDIRECTION, C.CARRIERNAME, C.LOTNAME  ");
		sql.append("  FROM CT_SORTJOB J, CT_SORTJOBCARRIER C, DURABLE D  ");
		sql.append(" WHERE J.JOBNAME = C.JOBNAME ");
		sql.append("   AND C.CARRIERNAME = D.DURABLENAME ");
		sql.append("   AND J.JOBNAME IN (SELECT J.JOBNAME  ");
		sql.append("                       FROM CT_SORTJOB J, CT_SORTJOBCARRIER C  ");
		sql.append("                      WHERE J.JOBNAME = C.JOBNAME  ");
		sql.append("                        AND C.LOTNAME = :LOTNAME   ");
		sql.append("                        AND J.JOBSTATE = 'STARTED'  ");
		sql.append("                        AND J.JOBTYPE IN ('Merge (OLED to TP)','Split (TP to OLED)')   ");
		sql.append("                        AND ROWNUM = 1)  ");
		sql.append("   AND C.TRANSFERDIRECTION = 'SOURCE'  ");
		sql.append("   AND C.CARRIERNAME = :CARRIERNAME  ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("LOTNAME", lotName);
		bindMap.put("CARRIERNAME", durableName);


		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);


		return sqlResult;
	}
	
	public String getDummyGlassQty(Lot lot) {
		try
		{
			StringBuffer sql = new StringBuffer();
			sql.append(" SELECT COUNT (P.PRODUCTNAME)     DUMMYGLASSQTY ");
			sql.append("   FROM PRODUCT P ");
			sql.append("  WHERE     P.LOTNAME = :LOTNAME ");
			sql.append("        AND P.DUMMYGLASSFLAG = 'Y' ");
			sql.append("        AND P.PRODUCTSTATE = 'InProduction' ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LOTNAME", lot.getKey().getLotName());
			
			List<Map<String, Object>> sqlLotList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
			String dummyGlassQty = CommonUtil.getValue(sqlLotList.get(0), "DUMMYGLASSQTY");
			
			return dummyGlassQty;
		}
		catch(Exception ex)
		{
			return null;
		}
	}
	
	public void deleteSrcLotBeforeOperationSamplingData(EventInfo eventInfo, Lot lotData ,List<String>srcLotList) throws CustomException
	{
		log.info("Start delete before operation sampling data all ***************************");
		
		if(srcLotList.size()>0)
		{
			for(String srcLotName : srcLotList)
			{
				List<SampleLot> sampleLotList = ExtendedObjectProxy.getSampleLotService().getSampleLotDataList(srcLotName, lotData.getFactoryName(), lotData.getProductSpecName(),
						lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());

				if (sampleLotList != null)
				{
					for (SampleLot sampleLot : sampleLotList)
					{
						log.info("Sample Lot Info");
						log.info("LotName - " + sampleLot.getLotName() + ", ProcessFlowName - " + sampleLot.getProcessFlowName() + ", ProcessOperationName - " + sampleLot.getProcessOperationName() + 
								", ToProcessFlowName - " + sampleLot.getToProcessFlowName() + ", ToProcessOperationName - " + sampleLot.getToProcessOperationName());
						
						String actualProductCount = sampleLot.getActualProductCount();
						String actualSamplePosition = sampleLot.getActualSamplePosition();

						List<SampleProduct> sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListByLotName(srcLotName, lotData.getFactoryName(),
								lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), sampleLot.getProcessOperationName(),
								sampleLot.getProcessOperationVersion(), sampleLot.getMachineName(), sampleLot.getToProcessFlowName(), sampleLot.getToProcessFlowVersion(),
								sampleLot.getToProcessOperationName(), sampleLot.getToProcessOperationVersion());

						List<String> deleteSamplePositionList = new ArrayList<String>();
						boolean sampleFlag = false;
						
						if (sampleProductList != null)
						{
							log.info("Sample Lot List Count - " + String.valueOf(sampleProductList.size()));
							for (SampleProduct sampleProduct : sampleProductList)
							{
								String productName = sampleProduct.getProductName();
								String sampleProdPosition = sampleProduct.getActualSamplePosition();
								Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
								if(StringUtils.equals(productData.getLotName(), lotData.getKey().getLotName()))
								{
									try
									{
										ExtendedObjectProxy.getSampleProductService().deleteSampleProductByToInfo(eventInfo, sampleProduct.getProductName(), srcLotName,
												lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), sampleProduct.getToProcessFlowName(),
												sampleProduct.getToProcessFlowVersion(), sampleProduct.getToProcessOperationName(), sampleProduct.getToProcessOperationVersion());
										
										log.info("Delete sample Product (" + sampleProduct.getProductName() + ")");
									}
									catch (Exception e)
									{
										log.info("SampleProduct Delete Fail");
										e.printStackTrace();
									}
									
									sampleFlag = true;

									if (!deleteSamplePositionList.contains(sampleProdPosition))
									{
										deleteSamplePositionList.add(sampleProdPosition);
									}
								}								
							}
						}
						
						// Modify or Delete SourceLot SampleData
						if (sampleFlag)
						{							
							if (deleteSamplePositionList.size() > 0)
							{
								if (Integer.parseInt(actualProductCount) > deleteSamplePositionList.size())
								{
									log.info("Modify Source SampleLot Data - DeletePositionList : " + deleteSamplePositionList);

									List<String> afterActualPositionList = new ArrayList<String>();

									if (actualSamplePosition.lastIndexOf(",") > -1)
									{
										String[] actualPositionArray = StringUtils.split(actualSamplePosition, ",");
										String oriSamplePosition = "";

										for (int i = 0; i < actualPositionArray.length; i++)
										{
											oriSamplePosition = actualPositionArray[i].trim();

											if (deleteSamplePositionList.size() > 0)
											{
												if (!deleteSamplePositionList.contains(oriSamplePosition))
												{
													afterActualPositionList.add(oriSamplePosition);
												}
											}
											else
											{
												afterActualPositionList.add(oriSamplePosition);
											}
										}
									}
									else
									{
										afterActualPositionList.add(actualSamplePosition);
									}

									afterActualPositionList = CommonUtil.sortActualSamplePosition(afterActualPositionList);

									// Modify
									sampleLot.setActualProductCount(String.valueOf(afterActualPositionList.size()));
									sampleLot.setActualSamplePosition(CommonUtil.toStringWithoutBrackets(afterActualPositionList));
									ExtendedObjectProxy.getSampleLotService().modify(eventInfo, sampleLot);									
								}
								else
								{
									try
									{
										log.info("Delete Source SampleLot Data");

										// Delete
										ExtendedObjectProxy.getSampleLotService().deleteSampleLotWithOutMachineName(eventInfo, srcLotName, sampleLot.getFactoryName(), sampleLot.getProductSpecName(), sampleLot.getProductSpecVersion(),
												sampleLot.getProcessFlowName(), sampleLot.getProcessFlowVersion(), sampleLot.getProcessOperationName(), sampleLot.getProcessOperationVersion(), sampleLot.getToProcessFlowName(), sampleLot.getToProcessFlowVersion(), sampleLot.getToProcessOperationName(),
												sampleLot.getToProcessOperationVersion());
										
										log.info("Delete sample Lot (" + sampleLot.getLotName() + ")");
									}
									catch (Exception e)
									{
										log.info("SampleLot Delete Fail (TrackOut)");
										e.printStackTrace();
									}
									
								}
							}
						}
					}
				}
			}
		}		
		log.info("End delete before operation sampling data all ***************************");
	}
	
	public List<SampleLot> deleteSrcSamplingDataReturn(EventInfo eventInfo, List<String>srcLotList, List<Element> productList, boolean isManual) throws CustomException
	{
		eventInfo.setEventName("TrackOut");
		
		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();
		for(String srcLotName : srcLotList)
		{
			Lot srcLotData = new Lot();
			try
			{
				srcLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(srcLotName);
			}
			catch(Exception ex)
			{
				log.info("srcLot:"+srcLotName+" not Exist");
			}
			
			sampleLotList = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListByToInfo(srcLotData.getKey().getLotName(), srcLotData.getFactoryName(),
					srcLotData.getProductSpecName(), srcLotData.getProductSpecVersion(), srcLotData.getProcessFlowName(), srcLotData.getProcessFlowVersion(),
					srcLotData.getProcessOperationName(), srcLotData.getProcessOperationVersion());

			if (sampleLotList != null)
			{
				List<SampleProduct> sampleProductList = new ArrayList<SampleProduct>();
				List<String> deleteSamplePositionList = new ArrayList<String>();
				boolean sampleFlag = false;
				
				String factoryName = sampleLotList.get(0).getFactoryName();
				String productSpecName = sampleLotList.get(0).getProductSpecName();
				String productSpecVersion = sampleLotList.get(0).getProductSpecVersion();
				String processFlowName = sampleLotList.get(0).getProcessFlowName();
				String processFlowVersion = sampleLotList.get(0).getProcessFlowVersion();
				String processOperationName = sampleLotList.get(0).getProcessOperationName();
				String processOperationVersion = sampleLotList.get(0).getProcessOperationVersion();
				String machineName = sampleLotList.get(0).getMachineName();
				String toProcessFlowName = sampleLotList.get(0).getToProcessFlowName();
				String toProcessFlowVersion = sampleLotList.get(0).getToProcessFlowVersion();
				String toProcessOperationName = sampleLotList.get(0).getToProcessOperationName();
				String toProcessOperationVersion = sampleLotList.get(0).getToProcessOperationVersion();
				String returnProcessFlowName = sampleLotList.get(0).getReturnProcessFlowName();
				String returnProcessFlowVersion = sampleLotList.get(0).getReturnProcessFlowVersion();
				String returnProcessOperationName = sampleLotList.get(0).getReturnOperationName();
				String returnProcessOperationVersion = sampleLotList.get(0).getReturnOperationVersion();
				String lotSampleFlag = sampleLotList.get(0).getLotSampleFlag();
				String lotSampleCount = sampleLotList.get(0).getLotSampleCount();
				String currentLotCount = sampleLotList.get(0).getCurrentLotCount();
				String totalLotCount = sampleLotList.get(0).getTotalLotCount();
				String productSampleCount = sampleLotList.get(0).getProductSampleCount();
				String productSamplePosition = sampleLotList.get(0).getProductSamplePosition();
				String priority = sampleLotList.get(0).getPriority().toString();
				String manualSampleFlag = sampleLotList.get(0).getManualSampleFlag();	
				String actualProductCount = sampleLotList.get(0).getActualProductCount();
				String actualSamplePosition = sampleLotList.get(0).getActualSamplePosition();
			
				if (isManual)
				{
					ExtendedObjectProxy.getSampleProductService().deleteSampleProductByLotNameAndToInfo(eventInfo, srcLotData.getKey().getLotName(), srcLotData.getFactoryName(),
							srcLotData.getProductSpecName(), srcLotData.getProductSpecVersion(), sampleLotList.get(0).getToProcessFlowName(), sampleLotList.get(0).getToProcessFlowVersion(),
							sampleLotList.get(0).getToProcessOperationName(), sampleLotList.get(0).getToProcessOperationVersion());
				}
				else
				{
					sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListByLotNameAndToInfo(srcLotData.getKey().getLotName(), srcLotData.getFactoryName(),
							srcLotData.getProductSpecName(), srcLotData.getProductSpecVersion(), srcLotData.getProcessFlowName(), srcLotData.getProcessFlowVersion(),
							srcLotData.getProcessOperationName(), srcLotData.getProcessOperationVersion());

					for (SampleProduct sampleProductM : sampleProductList)
					{
						String sampleProdPosition = sampleProductM.getActualSamplePosition();
						for (Element productE : productList)
						{
							try
							{
								String p = productE.getChildText("PRODUCTNAME").toString();
								String sp = sampleProductM.getProductName();

								if (p.equals(sp))
								{
									ExtendedObjectProxy.getSampleProductService().deleteSampleProductByToInfo(eventInfo, productE.getChildText("PRODUCTNAME"), srcLotData.getKey().getLotName(),
											srcLotData.getFactoryName(), srcLotData.getProductSpecName(), srcLotData.getProductSpecVersion(),
											sampleLotList.get(0).getToProcessFlowName(), sampleLotList.get(0).getToProcessFlowVersion(), sampleLotList.get(0).getToProcessOperationName(),
											sampleLotList.get(0).getToProcessOperationVersion());
									
									sampleFlag = true;

									if (!deleteSamplePositionList.contains(sampleProdPosition))
									{
										deleteSamplePositionList.add(sampleProdPosition);
									}
									break;
								}
							}
							catch (Exception e)
							{
							}
						}

					}
				}
				
				if (sampleFlag)
				{
					// Modify or Delete SourceLot SampleData
					if (deleteSamplePositionList.size() > 0)
					{
						if (Integer.parseInt(actualProductCount) > deleteSamplePositionList.size())
						{
							log.info("Modify Source SampleLot Data - DeletePositionList : " + deleteSamplePositionList);

							List<String> afterActualPositionList = new ArrayList<String>();

							if (actualSamplePosition.lastIndexOf(",") > -1)
							{
								String[] actualPositionArray = StringUtils.split(actualSamplePosition, ",");
								String oriSamplePosition = "";

								for (int i = 0; i < actualPositionArray.length; i++)
								{
									oriSamplePosition = actualPositionArray[i].trim();

									if (deleteSamplePositionList.size() > 0)
									{
										if (!deleteSamplePositionList.contains(oriSamplePosition))
										{
											afterActualPositionList.add(oriSamplePosition);
										}
									}
									else
									{
										afterActualPositionList.add(oriSamplePosition);
									}
								}
							}
							else
							{
								afterActualPositionList.add(actualSamplePosition);
							}


							afterActualPositionList = CommonUtil.sortActualSamplePosition(afterActualPositionList);

							// Modify
							ExtendedObjectProxy.getSampleLotService().updateSampleLot(eventInfo, srcLotData.getKey().getLotName(), factoryName, productSpecName, productSpecVersion, processFlowName,
									processFlowVersion, processOperationName, processOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion,
									lotSampleFlag, lotSampleCount, currentLotCount, totalLotCount, productSampleCount, productSamplePosition, String.valueOf(afterActualPositionList.size()),
									CommonUtil.toStringWithoutBrackets(afterActualPositionList), manualSampleFlag, "", priority, returnProcessFlowName, returnProcessFlowVersion, returnProcessOperationName,
									returnProcessOperationVersion, "");
						}
						else
						{
							log.info("Delete Source SampleLot Data");

							// Delete
							ExtendedObjectProxy.getSampleLotService().deleteSampleLotWithOutMachineName(eventInfo, srcLotData.getKey().getLotName(), factoryName, productSpecName, productSpecVersion,
									processFlowName, processFlowVersion, processOperationName, processOperationVersion, toProcessFlowName, toProcessFlowVersion, toProcessOperationName,
									toProcessOperationVersion);
						}
					}

				sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListByLotNameAndToInfo(srcLotData.getKey().getLotName(), srcLotData.getFactoryName(),
						srcLotData.getProductSpecName(), srcLotData.getProductSpecVersion(), srcLotData.getProcessFlowName(), srcLotData.getProcessFlowVersion(),
						srcLotData.getProcessOperationName(), srcLotData.getProcessOperationVersion());

				if (sampleProductList == null)
					{
						ExtendedObjectProxy.getSampleLotService().deleteSampleLotDataByToInfo(eventInfo, srcLotData.getKey().getLotName(), srcLotData.getFactoryName(),
							srcLotData.getProductSpecName(), srcLotData.getProductSpecVersion(), sampleLotList.get(0).getToProcessFlowName(), sampleLotList.get(0).getToProcessFlowVersion(),
							sampleLotList.get(0).getToProcessOperationName(), sampleLotList.get(0).getToProcessOperationVersion());
					}
				}
			}
		}
		return sampleLotList;
	}
	
	public Lot clearELANFC(Lot lotData)
	{
		try
		{		
			StringBuffer sql=new StringBuffer();
			sql.append(" UPDATE LOT SET ELANFC='' WHERE LOTNAME=:LOTNAME ");
			
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("LOTNAME", lotData.getKey().getLotName());
			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
			
			lotData=MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
		}
		catch(FrameworkErrorSignal f)
		{
			return lotData;
		}
		catch (Exception e) 
		{
			return lotData;
		}
		
		return lotData;
	}
	
	public Lot abnormalSheetReserveIssueLot(Lot lotData,String processOperationName,EventInfo eventInfo)
	{
		try
		{	
			log.info("AbnormalSheet reserve issuelot Start");
			boolean issueFlag = ExtendedObjectProxy.getEnumDefValueService().isExistEnumNameInfo("AbnormalSheetIssueOper", processOperationName);
			if(!issueFlag)
			{
				return lotData;
			}

			StringBuffer sql = new StringBuffer();
			sql.append("SELECT A.PRODUCTNAME FROM CT_ABNORMALSHEETDETAIL A,PRODUCT B ");
			sql.append("  WHERE A.PRODUCTNAME = B.PRODUCTNAME ");
			sql.append("  AND A.ISSUEFLAG='true' ");
			sql.append("  AND A.RSPROCESSOPERATIONNAME=:RSPROCESSOPERATIONNAME ");
			sql.append("  AND B.LOTNAME=:LOTNAME ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("RSPROCESSOPERATIONNAME", processOperationName);
			bindMap.put("LOTNAME", lotData.getKey().getLotName());

			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
			
			if(sqlResult!=null&&sqlResult.size()>0)
			{
				EventInfo eventInfoIssue = EventInfoUtil.makeEventInfo("RSIssueLot", eventInfo.getEventUser(),"RS AbnormalSheet IssueLot", "", "");
				Map<String, String> udfs = lotData.getUdfs();
				
				if (udfs.get("LOTISSUESTATE").equals(GenericServiceProxy.getConstantMap().Lot_IssueReleased)|| udfs.get("LOTISSUESTATE").isEmpty()) 
				{
					// Check Lot State
					if (StringUtil.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Released)) 
					{
						// Set Lot Issue State
						SetEventInfo setEventInfo = new SetEventInfo();
						setEventInfo.getUdfs().put("LOTISSUESTATE", "Y");

						lotData = LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfoIssue, setEventInfo);

						// Set Product Issue State
						kr.co.aim.greentrack.product.management.info.SetEventInfo setProductEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
						List<Product> productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotData.getKey().getLotName());

						for (Product product : productList) 
						{
							Map<String, String> udfspro = new HashMap<String, String>();
							udfspro.put("ISSUESTATE", "Y");
							udfspro.put("ISSUETIME", TimeStampUtil.toTimeString(eventInfoIssue.getEventTime()));
							udfspro.put("ISSUEUSER", eventInfoIssue.getEventUser());

							setProductEventInfo.setUdfs(udfspro);
							ProductServiceProxy.getProductService().setEvent(product.getKey(), eventInfoIssue,setProductEventInfo);
						}
					} 
				} 
				else 
				{
					// This Lot is already IssueLot.
					log.info(" This Lot is already IssueLot. ");
					lotData=LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfoIssue, new SetEventInfo());
				}
			}
		}
		catch(FrameworkErrorSignal f)
		{
			return lotData;
		}
		catch (Exception e) 
		{
			return lotData;
		}
		
		return lotData;
	}
	
	public void setProductReworkCount(Lot preLotData, List<String> productList,ProcessOperationSpec processOperationData) throws CustomException
	{
		ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(preLotData);
		if(!StringUtils.equals(processFlowData.getProcessFlowType(), "Rework")&&!StringUtils.equals(processFlowData.getProcessFlowType(), "Strip"))
		{
			return;
		}
		if(!StringUtils.equals(processOperationData.getProcessOperationType(), "Production"))
		{
			return;
		}
		
		long reworkCountLimit = 0;
		String reworkCountLimitByEnum=CommonUtil.getColumnByEnumNameAndEnumValue("ReworkCountLimit", preLotData.getFactoryName(), "DESCRIPTION");
		if(StringUtils.isNotEmpty(reworkCountLimitByEnum))
		{
			reworkCountLimit = Long.valueOf(reworkCountLimitByEnum);
		}
		else
		{
			StringBuffer strSQL = new StringBuffer();
			strSQL.append("SELECT P.REWORKTYPE, P.REWORKCOUNTLIMIT ");
			strSQL.append("  FROM TFOPOLICY T, POSALTERPROCESSOPERATION P ");
			strSQL.append(" WHERE T.CONDITIONID = P.CONDITIONID ");
			strSQL.append("   AND T.FACTORYNAME = :FACTORYNAME ");
			strSQL.append("   AND T.PROCESSFLOWNAME = :RETURNPROCESSFLOWNAME ");
			strSQL.append("   AND T.PROCESSOPERATIONNAME = :RETURNPROCESSOPERATIONNAME ");
			strSQL.append("   AND P.TOPROCESSFLOWNAME = :PROCESSFLOWNAME ");
			strSQL.append("   AND P.CONDITIONNAME = 'Rework' ");
			strSQL.append("   AND P.REWORKTYPE IS NOT NULL ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("FACTORYNAME", preLotData.getFactoryName());
			bindMap.put("RETURNPROCESSFLOWNAME", preLotData.getUdfs().get("RETURNFLOWNAME"));
			bindMap.put("RETURNPROCESSOPERATIONNAME", preLotData.getUdfs().get("RETURNOPERATIONNAME"));
			bindMap.put("PROCESSFLOWNAME", preLotData.getProcessFlowName());

			List<Map<String, Object>> posAlterList = new ArrayList<Map<String, Object>>();

			posAlterList = GenericServiceProxy.getSqlMesTemplate().queryForList(strSQL.toString(), bindMap);
			if(posAlterList!=null &&posAlterList.size()>0)
			{
				try
				{
					reworkCountLimit = Long.valueOf((String) posAlterList.get(0).get("REWORKCOUNTLIMIT"));
				}
				catch (Exception e)
				{
					log.info("ReworkCountLimit : " + ConvertUtil.getMapValueByName(posAlterList.get(0), "REWORKCOUNTLIMIT"));
					reworkCountLimit=100;
				}
			}
		}
		
        for(String productName:productList)
        {
        	try
        	{
        		ReworkProduct reworkProduct = ExtendedObjectProxy.getReworkProductService().selectByKey(false,
        				new Object[] { productName, processOperationData.getKey().getProcessOperationName() });
				reworkProduct.setReworkCount(reworkProduct.getReworkCount() + 1);
				reworkProduct.setActualReworkCount(reworkProduct.getActualReworkCount()+1);
				ExtendedObjectProxy.getReworkProductService().update(reworkProduct);
        	}
        	catch (greenFrameDBErrorSignal ex)
			{        		
				ReworkProduct reworkProduct = new ReworkProduct();
				reworkProduct.setProductName(productName);
				reworkProduct.setFactoryName(preLotData.getFactoryName());
				reworkProduct.setProcessFlowName(preLotData.getProcessFlowName());
				reworkProduct.setProcessFlowVersion("00001");
				reworkProduct.setProcessOperationName(preLotData.getProcessOperationName());
				reworkProduct.setProcessOperationVersion(preLotData.getProcessOperationVersion());
				reworkProduct.setReworkType(preLotData.getProcessOperationName());
				reworkProduct.setReworkCount(1);
				reworkProduct.setReworkCountLimit(Long.toString(reworkCountLimit));
				reworkProduct.setActualReworkCount(1);;

				ExtendedObjectProxy.getReworkProductService().insert(reworkProduct);
			}
        }
		
	}

}