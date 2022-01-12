package kr.co.aim.messolution.lot.event;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCRunControl;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.event.EventInfoExtended;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.ext.ConsumedMaterial;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.context.ApplicationContext;

public class LotProcessAbort extends AsyncHandler {

	private ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog(LotProcessAbort.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
		Element bodyElement = SMessageUtil.getBodyElement(doc);
		List<String> productNameList = CommonUtil.makeList(bodyElement, "PRODUCTLIST", "PRODUCTNAME");
		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		String newLotName = "";
		String lotJudge = "";

		// for common
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);

		String lotNameByCarrier = MESLotServiceProxy.getLotInfoUtil().getLotNameByCarrierName(carrierName);
		Lot lotDataByCarrier = new Lot();
		try
		{
			lotDataByCarrier = MESLotServiceProxy.getLotInfoUtil().getLotData(lotNameByCarrier);
		}
		catch (Exception e)
		{
		}

		Map<String, String> assignCarrierUdfs = new HashMap<String, String>();
		Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();

		List<String> srcLotList = new ArrayList<String>();
		List<String> destLotList = new ArrayList<String>();

		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);

		// 0. ProductCount of CST is to be 0 (Empty CST is out)
		if (productList.size() == 0)
		{
			if (lotDataByCarrier.getProductQuantity() > 0)
			{
				Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

				List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotDataByCarrier);
				DeassignCarrierInfo createInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotDataByCarrier, durableData, productUSequence);

				eventInfo = EventInfoUtil.makeEventInfo("DeassignCarrier", getEventUser(), getEventComment(), "", "");
				MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotDataByCarrier, createInfo, eventInfo);
			}

			return;
		}

		// Validation - Check duplicate reported glass ID and position
		CommonValidation.checkDuplicatedProductNameByProductList(productNameList);
		CommonValidation.checkDuplicatePosition(productList);
		
		//Validation - Check ProcessState Mantis 0000422
		List<Product> productDataList = getAllProductList(productNameList);
		if(productDataList.size() != productNameList.size())
		{
			throw new CustomException("LOT-0311", productNameList.size());
		}
		
		for(Product productInfo : productDataList)
		{
			if(!StringUtil.equals(productInfo.getProductProcessState(), GenericServiceProxy.getConstantMap().Prod_Processing))
			{
				//Get Product LotInfo
				Lot lotInfo = MESLotServiceProxy.getLotInfoUtil().getLotData(productInfo.getLotName());
				
				// Set ReasonCode
				eventInfo.setReasonCodeType("HOLD");
				eventInfo.setReasonCode("HD100");
				eventInfo.setEventComment("Lot is Wait，Can not LotProcessAbort，Please confirm");

				// LotMultiHold
				MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, lotInfo, new HashMap<String, String>());
				
				return;
			}
		}
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		Lot cancelTrackInLot = new Lot();

		// get LotList by ProductList
		List<Map<String, Object>> lotListByProductList = MESLotServiceProxy.getLotServiceUtil().getLotListByProductList(productList);

		// first Lot of LotList is Base Lot : much productQty Lot of LotList
		Lot baseLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotListByProductList.get(0).get("LOTNAME").toString());

		ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(baseLotData.getFactoryName(), baseLotData.getProcessOperationName(), baseLotData.getProcessOperationVersion());

		// Add Port Type BL -dkh,  Confirmed by xiaoxh
		if (portType.equals("PL")|| portType.equals("BL"))
		{
			if (productList.size() == lotDataByCarrier.getProductQuantity())
			{
				cancelTrackInLot = MESLotServiceProxy.getLotInfoUtil().getLotData(baseLotData.getKey().getLotName());
			}
			else
			{
				Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

				List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotDataByCarrier);
				DeassignCarrierInfo createInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotDataByCarrier, durableData, productUSequence);

				eventInfo = EventInfoUtil.makeEventInfo("DeassignCarrier", getEventUser(), getEventComment(), "", "");
				MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotDataByCarrier, createInfo, eventInfo);

				Lot newLotData = MESLotServiceProxy.getLotServiceUtil().createNewLot(eventInfo, baseLotData, carrierName, assignCarrierUdfs, lotListByProductList.size(), productList);
				newLotName = newLotData.getKey().getLotName();

				destLotList.add(newLotName);

				for (Map<String, Object> lotM : lotListByProductList)
				{
					String sLotName = CommonUtil.getValue(lotM, "LOTNAME");
					String sProductQuantity = CommonUtil.getValue(lotM, "PRODUCTQTY");
					Lot sLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(sLotName);

					srcLotList.add(sLotName);

					MESLotServiceProxy.getLotServiceUtil().transferProductsToLot(eventInfo, newLotData, portData, sLotName, sProductQuantity, deassignCarrierUdfs, productList);

					// inherit Sampling
					// if (isSampleLot)
					// {
					// MESLotServiceProxy.getLotServiceUtil().inheritforSampling(baseLotData, newLotData, productList, eventInfo);
					// }

					// Create MQC
					ProductSpec baseData = GenericServiceProxy.getSpecUtil().getProductSpec(sLotData.getFactoryName(), sLotData.getProductSpecName(),
							GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);

					if (CommonUtil.equalsIn(baseData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare"))
					{
						eventLog.info("ProductSpecGroup: " + baseData.getUdfs().get("PRODUCTSPECGROUP"));

						List<String> sProductList = MESLotServiceProxy.getLotServiceUtil().getProductListByElementList(productList);
						MESLotServiceProxy.getLotServiceUtil().createMQCWithReturnBank(sLotData, newLotData, sProductList, carrierName, eventInfo);
					}
				}

				cancelTrackInLot = MESLotServiceProxy.getLotInfoUtil().getLotData(newLotName);
			}
		}
		else if (portType.equals("PU"))
		{
			Lot newLotData = MESLotServiceProxy.getLotServiceUtil().createNewLot(eventInfo, baseLotData, carrierName, assignCarrierUdfs, lotListByProductList.size(), productList);
			newLotName = newLotData.getKey().getLotName();

			destLotList.add(newLotName);

			for (Map<String, Object> lotM : lotListByProductList)
			{
				String sLotName = CommonUtil.getValue(lotM, "LOTNAME");
				String sProductQuantity = CommonUtil.getValue(lotM, "PRODUCTQTY");

				srcLotList.add(sLotName);
				
				MESLotServiceProxy.getLotServiceUtil().transferProductsToLot(eventInfo, newLotData, portData, sLotName, sProductQuantity, deassignCarrierUdfs, productList);

				// Create MQC
				ProductSpec baseData = GenericServiceProxy.getSpecUtil().getProductSpec(baseLotData.getFactoryName(), baseLotData.getProductSpecName(),
						GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);

				if (CommonUtil.equalsIn(baseData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare"))
				{
					eventLog.info("ProductSpecGroup: " + baseData.getUdfs().get("PRODUCTSPECGROUP"));

					List<String> sProductList = MESLotServiceProxy.getLotServiceUtil().getProductListByElementList(productList);
					MESLotServiceProxy.getLotServiceUtil().createMQCWithReturnBank(baseLotData, newLotData, sProductList, carrierName, eventInfo);
				}
			}

			cancelTrackInLot = MESLotServiceProxy.getLotInfoUtil().getLotData(newLotName);
		}

		List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(SMessageUtil.getBodyElement(doc));

		deassignCarrierUdfs.clear();
		assignCarrierUdfs.clear();
		List<ConsumedMaterial> lotConsumedMaterail = new ArrayList<ConsumedMaterial>();

		// all sheet Lot in cutting
		if (StringUtil.equals(operationData.getDetailProcessOperationType(), "CUT"))
		{
			boolean isAllProductSheet = true;
			List<Product> allUnScrappedProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(cancelTrackInLot.getKey().getLotName());

			for (Product productData : allUnScrappedProductList)
			{
				if (!StringUtil.equals(productData.getProductType(), "Sheet"))
					isAllProductSheet = false;
			}

			if (isAllProductSheet)
			{
				String condition = "UPDATE LOT SET PRODUCTTYPE = 'Sheet' WHERE lotName = ? ";
				Object[] bindSet = new Object[] { cancelTrackInLot.getKey().getLotName() };
				kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().update(condition, bindSet);
			}
		}

		// Set Data(Sample, FutureAction) Transfer Product
		MESLotServiceProxy.getLotServiceUtil().transferProductSyncData(eventInfo, srcLotList, destLotList);
		
		if (portType.equals("PU"))
		{
			eventInfo = EventInfoUtil.makeEventInfo("TrackOut", getEventUser(), getEventComment(), "", "");
			eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

			lotJudge = CommonUtil.judgeLotGradeByProductList(productList, "PRODUCTJUDGE");

			cancelTrackInLot = MESLotServiceProxy.getLotServiceUtil().trackOutLotWithSampling(eventInfo, cancelTrackInLot, portData, carrierName, lotJudge, machineName, "", productPGSRCSequence,
					assignCarrierUdfs, deassignCarrierUdfs, new HashMap<String, String>());
		}
		else
		{
			eventInfo = EventInfoUtil.makeEventInfo("CancelTrackIn", getEventUser(), getEventComment(), "", "");
			eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

			//Return QTime Count
			ExtendedObjectProxy.getProductQTimeService().changeQTime(eventInfo, baseLotData.getKey().getLotName(), baseLotData.getFactoryName(), baseLotData.getProcessFlowName(), baseLotData.getProcessOperationName(), productList);
			cancelTrackInLot = MESLotServiceProxy.getLotServiceImpl().cancelTrackIn(eventInfo, cancelTrackInLot, productPGSRCSequence, assignCarrierUdfs, deassignCarrierUdfs, lotConsumedMaterail,
					carrierName);
		}

		// CT_LotFutureAction - Abort : [start]
		List<String> abortPositionList = new ArrayList<String>();
		for (ProductPGSRC productPGSRC : productPGSRCSequence)
		{
			Map<String, String> productUdfs = productPGSRC.getUdfs();
			if (StringUtils.equals(productUdfs.get("PROCESSINGINFO"), "B"))
			{
				abortPositionList.add(String.valueOf(productPGSRC.getPosition()));
			}
		}

		try
		{
			MESLotServiceProxy.getLotServiceImpl().deleteCtLotFutureAction(eventInfo, cancelTrackInLot.getKey().getLotName(), cancelTrackInLot.getFactoryName(), cancelTrackInLot.getProcessFlowName(),
					cancelTrackInLot.getProcessFlowVersion(), cancelTrackInLot.getProcessOperationName(), cancelTrackInLot.getProcessOperationVersion(), "1");
		}
		catch (CustomException e)
		{
			eventLog.info("Can't delete CtLotFutureAction:Abort Info not exist");
		}

		ExtendedObjectProxy.getLotFutureActionService().insertLotFutureActionForSkip(eventInfo, cancelTrackInLot.getKey().getLotName(), cancelTrackInLot.getFactoryName(),
				cancelTrackInLot.getProcessFlowName(), cancelTrackInLot.getProcessFlowVersion(), cancelTrackInLot.getProcessOperationName(), cancelTrackInLot.getProcessOperationVersion(), 1, "Abort",
				"Abort", "Abort", "Abort", machineName, CommonUtil.toStringWithoutBrackets(abortPositionList), "");

		// [end]

		// Mantis : 0000474
		// 2021-04-19	dhko	Update CT_MQCRUNCONTROL for DSP
		
		int actualProductQty = 0;
		for (ProductPGSRC productPGSRC : productPGSRCSequence)
		{
			Map<String, String> productUdfs = productPGSRC.getUdfs();
			if (!StringUtils.equals(productUdfs.get("PROCESSINGINFO"), "B") && !StringUtils.equals(productUdfs.get("PROCESSINGINFO"), "S"))
			{
				actualProductQty ++;
			}
		}
		
		if (actualProductQty > 0)
		{
			ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(baseLotData.getFactoryName(), baseLotData.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
			
			EventInfoExtended mqcRunControlEventInfo = new EventInfoExtended(eventInfo);
			mqcRunControlEventInfo.setEventName("TrackOut");
			mqcRunControlEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			
			String machineLockFlag = StringUtil.EMPTY;
			if (CommonUtil.equalsIn(baseLotData.getProductionType(), "P", "E"))
			{
				// Update ACTUALPRODUCTQTY
				mqcRunControlEventInfo.setEventComment("Update ActualProductQty. " + eventInfo.getEventComment());
				MQCRunControl dataInfo = ExtendedObjectProxy.getMQCRunControlService().updateActualProductQty(mqcRunControlEventInfo, machineName, baseLotData.getProcessOperationName(), baseLotData.getProcessOperationVersion(), actualProductQty);
				
				if (dataInfo != null)
				{
					String condition = " WHERE MACHINENAME = ? AND MQCPROCESSFLOWNAME = ? AND MQCPROCESSFLOWVERSION = ? ";
					List<MQCRunControl> checkActualProduct = null;
					
					try
					{
						checkActualProduct = ExtendedObjectProxy.getMQCRunControlService().select(condition, new Object[] { machineName, dataInfo.getMqcProcessFlowName(), dataInfo.getMqcProcessFlowVersion() });
					}
					catch (Exception e)
					{
						checkActualProduct =null;
					}
					
					if (checkActualProduct.size()>0)
					{
						double ratio = 0;
						for (MQCRunControl checkActualProductRow : checkActualProduct)
						{
							int maxProductQtyByChamber = (int) checkActualProductRow.getMaxProductQtyByChamber().intValue();
							int chamberQty = (int) checkActualProductRow.getChamberQty().intValue();
							int actualProductRow = (int) checkActualProductRow.getActualProductQty().intValue();
							ratio = ratio + new BigDecimal((float)actualProductRow / (maxProductQtyByChamber * chamberQty)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
						}
						if (ratio >= 1)
						{
							machineLockFlag = "Y";
						}
					}
				}
			}
			else if (CommonUtil.equalsIn(productSpecData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare"))
			{
				// Update MQCPROCESSQTY
				mqcRunControlEventInfo.setEventComment("Update MQCProcessQty. " + eventInfo.getEventComment());
				List<MQCRunControl> runControlDataList = ExtendedObjectProxy.getMQCRunControlService().updateMQCProcessQty(mqcRunControlEventInfo, machineName, baseLotData.getProcessFlowName(), baseLotData.getProcessFlowVersion(), actualProductQty);
				
				if (runControlDataList != null && runControlDataList.size() > 0) 
				{
					int actualMQCQty = 0;
					double needMQCQty = 0;
					
					for (MQCRunControl runControlData : runControlDataList) 
					{
						actualMQCQty = (int) runControlData.getMqcProcessQty().intValue();
						int maxMQCQtyByChamber = (int) runControlData.getMaxMQCQtyByChamber().intValue();
						int chamberQty = (int) runControlData.getChamberQty().intValue();
						int maxProductQtyByChamber = (int) runControlData.getMaxProductQtyByChamber().intValue();
						int actualProductRow = (int) runControlData.getActualProductQty().intValue();
						double ratio = new BigDecimal((float)actualProductRow / (maxProductQtyByChamber * chamberQty)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
						
						needMQCQty = needMQCQty + ratio * maxMQCQtyByChamber * chamberQty;
					}
					if (actualMQCQty > needMQCQty)
					{
						mqcRunControlEventInfo.setEventComment(
								"Initial ActualProductQty/MQCProcessQty. " + eventInfo.getEventComment());
						
						ExtendedObjectProxy.getMQCRunControlService().initialQtyByFlow(mqcRunControlEventInfo,
								machineName, baseLotData.getProcessFlowName(), baseLotData.getProcessFlowVersion());
						
						machineLockFlag = "N";
					}
				}
			}
			
			if (!StringUtil.isEmpty(machineLockFlag))
			{
				Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
				mqcRunControlEventInfo.setEventComment("ActualProductQty/MQCProcessQty Over. " + eventInfo.getEventComment());
				MESMachineServiceProxy.getMachineServiceImpl().changeMachineLockFlag(mqcRunControlEventInfo, machineData, machineLockFlag);
			}
		}
		
		if (!StringUtil.equals(cancelTrackInLot.getLotHoldState(), "Y"))
		{
			// Set ReasonCode
			eventInfo.setReasonCodeType("HOLD");
			eventInfo.setReasonCode("HD100");

			// LotMultiHold
			MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, cancelTrackInLot, new HashMap<String, String>());
		}

		// CompleteSortJobFlow
		ProcessFlow flowData = CommonUtil.getProcessFlowData(cancelTrackInLot.getFactoryName(), cancelTrackInLot.getProcessFlowName(), cancelTrackInLot.getProcessFlowVersion());

		if (StringUtils.equals(portType, "PL") && StringUtils.equals(flowData.getProcessFlowType(), "Sort"))
		{
			MESLotServiceProxy.getLotServiceUtil().completeSortFlow(eventInfo, cancelTrackInLot);
		}

		updateReservedLotState(cancelTrackInLot.getKey().getLotName(), machineName, cancelTrackInLot, eventInfo);
	}

	private void updateReservedLotState(String lotName, String machineName, Lot lotData, EventInfo eventInfo) throws CustomException
	{
		try
		{
			String condition = "machineName = ? and lotName =? and productSpecName =? and processOperationName =? and productRequestName =? and reserveState = ? ";
			Object bindSet[] = new Object[] { machineName, lotName, lotData.getProductSpecName(), lotData.getProcessOperationName(), lotData.getProductRequestName(), "Executing" };
			List<ReserveLot> reserveLot = ExtendedObjectProxy.getReserveLotService().select(condition, bindSet);

			reserveLot.get(0).setReserveState(GenericServiceProxy.getConstantMap().RESV_STATE_RESV);
			reserveLot.get(0).setInputTimeKey("");

			ExtendedObjectProxy.getReserveLotService().modify(eventInfo, reserveLot.get(0));

			/*
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
			}*/

		}
		catch (Exception e)
		{
			eventLog.info("Fail ReservedLot Updating");
		}
	}
	
	public List<Product> getAllProductList(List<String> productNameList) throws CustomException
	{
		if (productNameList == null || productNameList.size() == 0)
		{
			log.info("The incoming variable value is null or empty!!");
			return new ArrayList<Product>();
		}

		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		
		String sql = " SELECT * FROM PRODUCT "
				   + " WHERE 1=1 AND PRODUCTNAME IN (:PRODUCTLIST)  "
				   + " ORDER BY POSITION";

		Map<String,Object> bindMap = new HashMap<>();
		bindMap.put("PRODUCTLIST", productNameList);

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
			return new ArrayList<Product>();

		return ProductServiceProxy.getProductService().transform(resultList);
	}
}
