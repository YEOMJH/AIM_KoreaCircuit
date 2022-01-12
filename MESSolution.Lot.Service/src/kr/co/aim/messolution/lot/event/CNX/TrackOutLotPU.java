package kr.co.aim.messolution.lot.event.CNX;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DSPProductRequestPlan;
import kr.co.aim.messolution.extended.object.management.data.MQCRunControl;
import kr.co.aim.messolution.extended.object.management.data.OffsetAlignInfo;
import kr.co.aim.messolution.extended.object.management.data.ProductQueueTime;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.extended.object.management.data.ReviewOperationInfo;
import kr.co.aim.messolution.extended.object.management.data.RunBanRule;
import kr.co.aim.messolution.extended.object.management.data.SampleLot;
import kr.co.aim.messolution.extended.object.management.data.SampleProduct;
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.event.EventInfoExtended;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.messolution.recipe.service.RecipeServiceUtil;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.data.PortSpec;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.NodeService;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processoperationspec.ProcessOperationSpecServiceProxy;
import kr.co.aim.greentrack.processoperationspec.management.ProcessOperationSpecService;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpecKey;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductHistory;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import sun.util.logging.resources.logging;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class TrackOutLotPU extends SyncHandler {
	private static Log log = LogFactory.getLog(TrackOutLotPU.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		Element bodyElement = SMessageUtil.getBodyElement(doc);

		List<String> srcLotList = new ArrayList<String>();
		List<String> destLotList = new ArrayList<String>();

		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String lotJudge = SMessageUtil.getBodyItemValue(doc, "LOTJUDGE", false);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);

		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		List<String> productNameList = CommonUtil.makeList(bodyElement, "PRODUCTLIST", "PRODUCTNAME");
		List<String> processedProductList=getProcessedProductList(productNameList);

		// Get Port Data
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		PortSpec portSpecData = MESPortServiceProxy.getPortServiceUtil().getPortSpecInfo(machineName, portName);

		// Get Lot, Machine Data
		Machine eqpData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		Lot baseLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		// Get OperationSpec Data
		ProcessOperationSpec beforeOperationSpecData = CommonUtil.getProcessOperationSpec(baseLotData.getFactoryName(), baseLotData.getProcessOperationName(),
				baseLotData.getProcessOperationVersion());

		// for common
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		if (StringUtils.equals(eventInfo.getEventComment(), "TrackOutLotPU"))
			eventInfo.setEventComment("TrackOutLot");

		// TK-OUT Lot ID
		String newLotName = "";

		// for auto-rework
		String reworkFlag = "";

		// Get RMSFlag
		//String RMSFlag = PolicyUtil.getPOSMachineRMSFlag(baseLotData.getFactoryName(), baseLotData.getProductSpecName(), baseLotData.getProductSpecVersion(), baseLotData.getProcessFlowName(),
		//		baseLotData.getProcessFlowVersion(), baseLotData.getProcessOperationName(), baseLotData.getProcessOperationVersion(), machineName);

		// Validation
		CommonValidation.checkMachineHold(eqpData);
		CommonValidation.checkJobDownFlag(baseLotData);
		CommonValidation.checkOriginalProduct(baseLotData);
		
		//TP ReviewStation Validation
		if(baseLotData.getProcessOperationName().equals("21209"))
		{
			TPReviewCheck(baseLotData);
		}

		MESLotServiceProxy.getLotServiceUtil().checkLotValidation(baseLotData);

		Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();
		Map<String, String> assignCarrierUdfs = new HashMap<String, String>();

		Lot trackOutLot = new Lot();
		srcLotList.add(lotName);

		Map<String, String> udfs = portData.getUdfs();
		udfs.put("PORTTYPE", portSpecData.getPortType());
		portData.setUdfs(udfs);

		// Port Type is PB
		if (baseLotData.getUdfs().get("PORTTYPE").equals("PB"))
		{
			trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(baseLotData.getKey().getLotName());
		}
		else
		{
			List<String> lotNameList = MESLotServiceProxy.getLotServiceUtil().getDistinctLotNameByProductName(productNameList);
			boolean changeLotFlag = MESLotServiceProxy.getLotServiceUtil().changeLotFlag(baseLotData, baseLotData.getFactoryName(), baseLotData.getProcessOperationName(),
					baseLotData.getProcessOperationVersion());

			// CST Dirty, Hold
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

			// Validation CST
			if (StringUtils.equals(portData.getUdfs().get("PORTTYPE"), "PU"))
			{
				CommonValidation.checkAvailableCst(durableData);
				CommonValidation.checkMultiLot(durableData.getKey().getDurableName());
			}
			
			// PU Case #1
			if (lotNameList.size() == 1 && (baseLotData.getProductQuantity() == productNameList.size()) && !changeLotFlag)
			{
				// No change to create lot
				trackOutLot = baseLotData;

				// MQC
				ProductSpec baseSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(baseLotData.getFactoryName(), baseLotData.getProductSpecName(),
						GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
				if (CommonUtil.equalsIn(baseSpecData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare"))
				{
					String jobName = MESLotServiceProxy.getLotServiceUtil().getMQCJobName(baseLotData);

					if (!jobName.isEmpty()) // Update CarrierName when CarrierName is changed in CT_MQCPLANDETAIL table
						ExtendedObjectProxy.getMQCPlanDetailService().UpdateCarrierChangedforMQCJob(eventInfo, baseLotData, jobName, carrierName);					
				}
			}
			else // PU Case #2 (Create and Merge)
			{
				double subProductUnitQty1 = baseLotData.getSubProductUnitQuantity1();

				try
				{
					subProductUnitQty1 = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productList.get(0).getChildText("PRODUCTNAME")).getSubProductUnitQuantity1();
				}
				catch (Exception e)
				{
				}

				ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(baseLotData.getFactoryName(), baseLotData.getProductSpecName(), baseLotData.getProductSpecVersion());

				// Create New Lot
				Lot newLotData = MESLotServiceProxy.getLotServiceUtil().createNewLotWithSplit(eventInfo, baseLotData, productSpecData, subProductUnitQty1, carrierName, assignCarrierUdfs);
				newLotName = newLotData.getKey().getLotName();

				// Transfer Products To Lot
				MESLotServiceProxy.getLotServiceUtil().transferProductsToLot(eventInfo, newLotData, portData, lotName, String.valueOf(productList.size()), deassignCarrierUdfs, productList);
				trackOutLot = newLotData;

				Lot emptiedLot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);

				// Deassign Carrier
				if (!StringUtils.equals(emptiedLot.getLotState(), "Released") || StringUtils.equals(emptiedLot.getLotState(), "Emptied"))
				{
					if (StringUtils.isNotEmpty(emptiedLot.getCarrierName()))
					{
						eventInfo.setEventName("DeassignCarrier");

						Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(emptiedLot.getCarrierName());

						List<ProductU> productUSequence = new ArrayList<ProductU>();

						DeassignCarrierInfo deassignCarrierInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(emptiedLot, carrierData, productUSequence);

						LotServiceProxy.getLotService().deassignCarrier(emptiedLot.getKey(), eventInfo, deassignCarrierInfo);
					}
				}

				// MQC
				if (CommonUtil.equalsIn(productSpecData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare"))
				{
					eventLog.info("ProductSpecGroup: " + productSpecData.getUdfs().get("PRODUCTSPECGROUP"));

					List<String> sProductList = MESLotServiceProxy.getLotServiceUtil().getProductListByElementList(productList);
					MESLotServiceProxy.getLotServiceUtil().createMQC(baseLotData, newLotData, sProductList, carrierName, eventInfo);
				}

				// Add Q-Time Clone Lot
				destLotList.add(newLotName);
			}
		}

		// MQC Dummy Used Count +
		// dummyUsedCountIncrease(trackOutLot, eventInfo);

		// For SPC
		if (beforeOperationSpecData.getUdfs().get("ISMAINOPERATION") != null && beforeOperationSpecData.getUdfs().get("ISMAINOPERATION").toString().equals("Y"))
			MESLotServiceProxy.getLotServiceUtil().setProcessingMainMachine(trackOutLot.getKey().getLotName());

		// Mapping Glass - Insert in CT_MATERIALPRODUCT
		MESLotServiceProxy.getLotServiceUtil().insertMaterialProductAll(eventInfo, eqpData, lotName, productNameList);

		// refined Lot logged in
		Lot beforeTrackOutLot = (Lot) ObjectUtil.copyTo(trackOutLot);

		ProcessOperationSpec operationSpec = ProcessOperationSpecServiceProxy.getProcessOperationSpecService()
				.selectByKey(new ProcessOperationSpecKey(trackOutLot.getFactoryName(), trackOutLot.getProcessOperationName(), trackOutLot.getProcessOperationVersion()));

		
		// Insert Photo Offset Data
		if (StringUtils.equals(eqpData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Photo))
		{
			OffsetAlignInfo offsetAlignInfo=new OffsetAlignInfo();
			try
			{
				offsetAlignInfo = ExtendedObjectProxy.getOffsetAlignInfoService().selectByKey(false, new Object[]{beforeTrackOutLot.getFactoryName(),beforeTrackOutLot.getProductSpecName() ,beforeTrackOutLot.getProductSpecVersion(), operationSpec.getUdfs().get("LAYERNAME").toString()});				
			}
			catch (Exception ex)
			{
				eventLog.info("offsetAlignInfo not found");
			}
			if (!StringUtil.isEmpty(offsetAlignInfo.getMainLayerStep()))
			{
				updatePhotoOffset(productNameList, machineName, operationSpec,Integer.parseInt(offsetAlignInfo.getMainLayerStep()));
			}
		}

		List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(bodyElement);

		if (baseLotData.getUdfs().get("PORTTYPE").equals("PB"))
		{
			if (trackOutLot.getProductQuantity() != productPGSRCSequence.size())
				throw new CustomException("LOT-1011");
		}

		// Judge Lot Grade
		lotJudge = CommonUtil.judgeLotGradeByProductList(productList, "PRODUCTJUDGE");

		ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(trackOutLot);

		if (StringUtils.equals(processFlowData.getProcessFlowType(), "BackUp"))
		{
			// Q-time By BackUp
			MESLotServiceProxy.getLotServiceUtil().isBackUpTrackOut(trackOutLot, eventInfo);
			
			// BackUp Sampling
			MESLotServiceProxy.getLotServiceUtil().setSamplingListDataForBackUpEQP(eventInfo, trackOutLot, eqpData, productList);
		}
		else
		{
			// Q-time By Lot
			ExtendedObjectProxy.getProductQTimeService().moveInQTimeByLot(eventInfo, trackOutLot.getKey().getLotName(), trackOutLot.getFactoryName(), trackOutLot.getProcessFlowName(),
					trackOutLot.getProcessFlowVersion(), trackOutLot.getProcessOperationName(), trackOutLot.getProcessOperationVersion(), trackOutLot.getUdfs().get("RETURNFLOWNAME"));
		}

		// Q-time
		ExtendedObjectProxy.getProductQTimeService().monitorProductQTime(eventInfo, trackOutLot.getKey().getLotName(), machineName);
		reworkFlag = ExtendedObjectProxy.getProductQTimeService().doProductQTimeAction(doc, trackOutLot.getKey().getLotName()).equalsIgnoreCase("rework") ? "Y" : reworkFlag;
		ExtendedObjectProxy.getProductQTimeService().exitQTimeByProductElement(eventInfo, trackOutLot.getKey().getLotName(), trackOutLot.getFactoryName(), trackOutLot.getProcessFlowName(), trackOutLot.getProcessOperationName(), productList);
		
		//Q-time重新计时
		if (trackOutLot.getFactoryName().equalsIgnoreCase("ARRAY") || trackOutLot.getFactoryName().equalsIgnoreCase("TP"))
		{
			if (processFlowData.getProcessFlowType().equals("Rework") && operationSpec.getProcessOperationType().equals("Production"))
			{
				int productionCount = 0;
				productionCount = this.getReworkProductionOperCount(trackOutLot);
				
				List<Map<String, Object>> beforeMainOper = this.getBeforeMainOperationNode(trackOutLot);

				if (beforeMainOper != null && beforeMainOper.size() > 0)
				{
					List<ProductQueueTime> QTimeDataList = ExtendedObjectProxy.getProductQTimeService().findProductQTimeByFromOper(trackOutLot.getKey().getLotName(),
							beforeMainOper.get(0).get("PROCESSFLOWNAME").toString(), beforeMainOper.get(0).get("NODEATTRIBUTE1").toString());
					
					EventInfo eventInfoForReworkQtime =(EventInfo) ObjectUtil.copyTo(eventInfo);
					eventInfoForReworkQtime.setEventName("TrackOutLotReEnterQtimeForRework");
					eventInfoForReworkQtime.setEventComment("Rework Production's Operation ReEnter Q-time");
					
					try 
					{
						this.modifyQtimeEnterTime(eventInfoForReworkQtime,productionCount,QTimeDataList,operationSpec);
					} 
					catch (Exception e) 
					{
						log.info("modify Q-time EnterTime Exception,skip Modify");
					}
				}
			}
		}
		
		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();

		if (!StringUtils.equals(processFlowData.getProcessFlowType(), "MQC"))
		{
			// if lot is FirstGlass child Lot, NG
			if (!StringUtils.isEmpty(trackOutLot.getUdfs().get("FIRSTGLASSFLAG")) && !StringUtils.isEmpty(trackOutLot.getUdfs().get("JOBNAME")))
			{
				if (trackOutLot.getUdfs().get("FIRSTGLASSFLAG").equals("N") && lotJudge.equals("N")) // delete sample lot/product
					MESLotServiceProxy.getLotServiceUtil().deleteSampleFirstGlass(eventInfo, trackOutLot.getUdfs().get("JOBNAME"), trackOutLot);
			}

			//sampleLotList = MESLotServiceProxy.getLotServiceUtil().deleteSamplingDataReturn(eventInfo, baseLotData, productList, false);
			sampleLotList = MESLotServiceProxy.getLotServiceUtil().deleteSrcSamplingDataReturn(eventInfo, srcLotList, productList, false);
		}
		else
		{
			// Increase MQC UsedCount
			// MESLotServiceProxy.getLotServiceUtil().increaseMQCUsedCount(productNameList, baseLotData.getProcessFlowName(), baseLotData.getProcessFlowVersion(),
			// baseLotData.getProcessOperationName(),
			// baseLotData.getProcessOperationVersion());

			MESLotServiceProxy.getLotServiceUtil().increateDummyUsedCount(eventInfo, trackOutLot);
		}

		// Check complete TPTJ Product Data.
		MESLotServiceProxy.getLotServiceUtil().deleteTPTJProductData(eventInfo, trackOutLot); // TPTJCase

		// Set Data(MainReserveSkip) New Lot - Lot AR-AMF-0030-01
		ExtendedObjectProxy.getMainReserveSkipService().syncMainReserveSkip(eventInfo, srcLotList, destLotList);

		SampleLot forceCheckData = new SampleLot();

		if (sampleLotList != null && sampleLotList.size() > 0)
			forceCheckData = sampleLotList.get(0);

		boolean forceFlag = false;
		boolean dummyProuctReserve = ExtendedObjectProxy.getDummyProductReserveService().checkDummyProductReserveData(trackOutLot.getKey().getLotName());
		boolean allDummyGlass = MESLotServiceProxy.getLotServiceUtil().allDummyGlass(trackOutLot);

		if (StringUtils.isEmpty(trackOutLot.getUdfs().get("FIRSTGLASSFLAG")))
		{
			if (StringUtils.isEmpty(trackOutLot.getUdfs().get("FIRSTGLASSALL")))
			{
				// FirstGlass check (Only child Lot)
				if (forceCheckData != null && StringUtils.isNotEmpty(forceCheckData.getForceSamplingFlag()))
				{
					forceFlag = true; // ForceSampling check
				}
				else if (!MESLotServiceProxy.getLotServiceUtil().checkMainReserveSkip(eventInfo, trackOutLot) // AR-AMF-0030-01
						&& !dummyProuctReserve && !allDummyGlass) // [V3_MES_121_037]TP NG,Dummy Supplement Scenario_V1.02
				{
					if (!StringUtils.equals(processFlowData.getProcessFlowType(), "BackUp"))
					{
						MESLotServiceProxy.getLotServiceUtil().setSamplingListData(eventInfo, trackOutLot, eqpData, productList); // Set Sampling Data
					}
				}
			}
		}

		// InlineSampling - Delete InlineSampling Data
		MESLotServiceProxy.getLotServiceUtil().deleteInlineSamplingData(eventInfo, trackOutLot, productList, machineName, true);

		// TFE CVD Chamber Sampling & TFE Down Sampling
		if ("EVA".equals(eqpData.getMachineGroupName()))
		{
			// Get TFE Down Sampling Rule (joined TFOMPOLICY & POSMACHINEDOWNSAMPLE)
			List<Map<String, Object>> machineDownSamplePolicyList = MESLotServiceProxy.getLotServiceUtil().getMachineDownSamplePolicyList(trackOutLot.getFactoryName(),
					trackOutLot.getProcessFlowName(), trackOutLot.getProcessFlowVersion(), trackOutLot.getProcessOperationName(), trackOutLot.getProcessOperationVersion(), 
					machineName, "", "", "", "");
			
			if (machineDownSamplePolicyList != null && machineDownSamplePolicyList.size() > 0)
			{
				EventInfoExtended machineDownSampleEventInfo = new EventInfoExtended(eventInfo);
				machineDownSampleEventInfo.setEventComment("[MachineDown] System MachineDownSampling Normal. " + eventInfo.getEventComment());
				machineDownSampleEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				
				MESLotServiceProxy.getLotServiceUtil().setMachineDownSamplingData(machineDownSampleEventInfo, eqpData, trackOutLot, machineDownSamplePolicyList.get(0));
			}
			
			// Get TFE CVD Chamber Sampling Rule (joined TFOMPOLICY & POSCHAMBERSAMPLE)
			List<Map<String, Object>> chamberSamplePolicyList = MESLotServiceProxy.getLotServiceUtil().getChamberSamplePolicyList(trackOutLot.getFactoryName(), trackOutLot.getProcessFlowName(),
					trackOutLot.getProcessFlowVersion(), trackOutLot.getProcessOperationName(), trackOutLot.getProcessOperationVersion(), trackOutLot.getMachineName(), "", "", "", "");

			if (chamberSamplePolicyList != null && chamberSamplePolicyList.size() > 0)
			{
				EventInfoExtended chamberSampleEventInfo = new EventInfoExtended(eventInfo);
				chamberSampleEventInfo.setEventComment("[Chamber] System ChamberSampling Normal. " + eventInfo.getEventComment());
				chamberSampleEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				
				for (Map<String, Object> chamberSamplePolicy : chamberSamplePolicyList)
				{
					if ("Y".equals(chamberSamplePolicy.get("TFESAMPLEFLAG")))
					{
						MESLotServiceProxy.getLotServiceUtil().setTFECVDChamberSamplingData(chamberSampleEventInfo, eqpData, trackOutLot, chamberSamplePolicy);
					}
				}				
			}
		}
		
		ProductSpec productSpecInfo = GenericServiceProxy.getSpecUtil().getProductSpec(baseLotData.getFactoryName(), baseLotData.getProductSpecName(), baseLotData.getProductSpecVersion());

		// For DSP
		/*
		 * try { if (StringUtils.equals(CommonUtil.getEnumDefValueStringByEnumName("FLAG_DSP_SetFlagStack"), "Y") && StringUtils.equals(baseLotData.getFactoryName(), "ARRAY"))
		 * SetFlagStack(productPGSRCSequence, baseLotData); } catch (Exception e) { eventLog.info("Error Occurred - SetFlagStack"); } try { if
		 * (StringUtils.equals(CommonUtil.getEnumDefValueStringByEnumName("Switch_DSP_AOILotJudge"), "Y") && StringUtils.equals(baseLotData.getFactoryName(), "ARRAY")) AOILotJudge(baseLotData,
		 * lotName); } catch (Exception e) { eventLog.info("Error Occurred - AOILotJudge"); }
		 */

		// Mantis : 0000474
		// 2021-04-19	dhko	Update CT_MQCRUNCONTROL for DSP
		ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(baseLotData.getFactoryName(), baseLotData.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
		
		EventInfoExtended mqcRunControlEventInfo = new EventInfoExtended(eventInfo);
		mqcRunControlEventInfo.setEventName("TrackOut");
		mqcRunControlEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		String machineLockFlag = StringUtil.EMPTY;
		if (CommonUtil.equalsIn(baseLotData.getProductionType(), "P", "E"))
		{
			// Update ACTUALPRODUCTQTY
			mqcRunControlEventInfo.setEventComment("Update ActualProductQty. " + eventInfo.getEventComment());
			MQCRunControl dataInfo = ExtendedObjectProxy.getMQCRunControlService().updateActualProductQty(mqcRunControlEventInfo, machineName, baseLotData.getProcessOperationName(), baseLotData.getProcessOperationVersion(), productPGSRCSequence.size());
			
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
						int maxProductQtyByChamber = checkActualProductRow.getMaxProductQtyByChamber().intValue();
						int chamberQty = checkActualProductRow.getChamberQty().intValue();
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
			List<MQCRunControl> runControlDataList = ExtendedObjectProxy.getMQCRunControlService().updateMQCProcessQty(mqcRunControlEventInfo,
					machineName, baseLotData.getProcessFlowName(), baseLotData.getProcessFlowVersion(), productPGSRCSequence.size());
			
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
		
		// TK OUT
		Lot afterTrackOutLot;

		// Array Review Station - If the Lot judge is P then goes to repair operation else skip.
		if ((StringUtils.equals(trackOutLot.getFactoryName(), "ARRAY")||StringUtils.equals(trackOutLot.getFactoryName(), "TP")) 
				&& (StringUtils.equals(beforeOperationSpecData.getDetailProcessOperationType(), "VIEW") ||  StringUtils.equals(beforeOperationSpecData.getDetailProcessOperationType(), "RP")))
			trackOutLot = getNextOperationAfterReviewStation(eventInfo, trackOutLot, lotJudge, true, forceFlag);

		// Make Logged Out
		if (forceFlag)
		{
			afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().trackOutLotForceSampling(eventInfo, trackOutLot, portData, carrierName, lotJudge, machineName, "", productPGSRCSequence,
					assignCarrierUdfs, deassignCarrierUdfs, new HashMap<String, String>());
		}
		else
		{
			afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().trackOutLotWithSampling(eventInfo, trackOutLot, portData, carrierName, lotJudge, machineName, "", productPGSRCSequence,
					assignCarrierUdfs, deassignCarrierUdfs, new HashMap<String, String>());
		}
		
		ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(baseLotData);	
		Machine machineData  = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		
		
		// If firstGlass Lot is not process hold. 2021-03-31 #0000464
		if ( StringUtils.isEmpty(trackOutLot.getUdfs().get("JOBNAME")))
		{
			if (StringUtils.isEmpty(trackOutLot.getUdfs().get("FIRSTGLASSALL")))
			{
				if (!StringUtils.equals(afterTrackOutLot.getLotHoldState(), "Y")
										&&(flowData.getProcessFlowType().equals("Main"))
											&&(!StringUtils.equals(machineData.getMachineGroupName(),"Unpacker")))
				{
					// Set ReasonCode
					String holdEventComment = "ManualTrackOut";
					eventLog.info(holdEventComment);
					
					eventInfo.setReasonCodeType("HOLD");
					eventInfo.setReasonCode("ManualTrackOut");
					eventInfo.setEventComment(holdEventComment);

					// LotMultiHold
					MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, afterTrackOutLot, new HashMap<String, String>());
					afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().getLotData(afterTrackOutLot.getKey().getLotName());
				}
			}
		}


		// Delete before operation sampling data all
		eventLog.info("Start delete before operation sampling data all");
		ProcessOperationSpecKey specKey = new ProcessOperationSpecKey(afterTrackOutLot.getFactoryName(), afterTrackOutLot.getProcessOperationName(), afterTrackOutLot.getProcessOperationVersion());
		ProcessOperationSpec afterOperSpec = ProcessOperationSpecServiceProxy.getProcessOperationSpecService().selectByKey(specKey);
		ProcessFlow currentFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(afterTrackOutLot);
		eventLog.info("ProcessOperationSpecData");
		eventLog.info("ProcessOperation - " + afterOperSpec.getKey().getProcessOperationName());
		eventLog.info("ProcessOperationType - " + afterOperSpec.getProcessOperationType());
		eventLog.info("IsMainOperation - " + afterOperSpec.getUdfs().get("ISMAINOPERATION").toString());
		eventLog.info("ProcessOperationGroup - " + afterOperSpec.getProcessOperationGroup());

		if (afterOperSpec.getProcessOperationType().equals("Production") && afterOperSpec.getUdfs().get("ISMAINOPERATION").toString().equals("Y")
				&& afterOperSpec.getProcessOperationGroup().equals("Normal"))
		{
			MESLotServiceProxy.getLotServiceUtil().deleteSrcLotBeforeOperationSamplingData(eventInfo, afterTrackOutLot, srcLotList);
		}

		eventLog.info("End delete before operation sampling data all");
		
		//update reworkCount
		MESLotServiceProxy.getLotServiceUtil().setProductReworkCount(baseLotData, processedProductList, beforeOperationSpecData);

		// Set NextOper ReworkFlag.
		if (StringUtils.equals(currentFlowData.getProcessFlowType(), "Rework"))
		{
			MESLotServiceProxy.getLotServiceUtil().setNextOperReworkFlag(eventInfo, afterTrackOutLot);
		}

		// Set Data(Sample, FutureAction) Transfer Product
		MESLotServiceProxy.getLotServiceUtil().transferProductSyncData(eventInfo, srcLotList, destLotList);

		// InlineSampling - Set InlineSampling Data
		MESLotServiceProxy.getLotServiceUtil().setInlineSamplingListData(eventInfo, afterTrackOutLot);

		// Complete Rework
		afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().completeRework(eventInfo, beforeTrackOutLot, afterTrackOutLot, productList);

		// FirstGlass
		afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().excuteFirstGlass(eventInfo, beforeTrackOutLot, afterTrackOutLot, reworkFlag);

		// Hold (Product Future Hold)
		MESLotServiceProxy.getLotServiceUtil().executePostActionByPrdocutFutureHold(eventInfo, beforeTrackOutLot,afterTrackOutLot, productNameList);
		// Hold & Skip
		afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().executePostAction(eventInfo, beforeTrackOutLot, afterTrackOutLot, forceFlag);
		
		//Clear ELANFC
		if(StringUtils.equals(beforeOperationSpecData.getDetailProcessOperationType(), "ELA"))
		{
			afterTrackOutLot=MESLotServiceProxy.getLotServiceUtil().clearELANFC(afterTrackOutLot);
		}
		
		//BaseLineFlag
		//Offline Review Skip 2020-07-17 request by yueke
		/*
		if(afterOperSpec.getDetailProcessOperationType().equals("VIEW"))
		{
			afterTrackOutLot = OfflineRSSkip(afterTrackOutLot);
		}
		*/
		// ReviewStation Skip by Lot count for ( Array and notInRework Flow and not forceSamfypling and only with Repair flow)
		if (afterTrackOutLot.getFactoryName().equalsIgnoreCase("ARRAY") && afterTrackOutLot.getReworkState().equalsIgnoreCase("NotInRework") && !forceFlag)
			afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().exceutePostShipOfReviewStation(eventInfo, beforeTrackOutLot, afterTrackOutLot);
		
		// ReviewStation reserve Issue Lot
		if(StringUtils.equals(beforeOperationSpecData.getDetailProcessOperationType(), "VIEW"))
		{
			afterTrackOutLot=MESLotServiceProxy.getLotServiceUtil().abnormalSheetReserveIssueLot(afterTrackOutLot,beforeOperationSpecData.getKey().getProcessOperationName(),eventInfo);
		}

		// Delete futureAction  
		MESLotServiceProxy.getLotServiceUtil().deleteFutureActionData(eventInfo, sampleLotList, beforeTrackOutLot, afterTrackOutLot);
		
		// Delete productFutureAction 
		MESLotServiceProxy.getLotServiceUtil().deleteProductFutureActionData(eventInfo, sampleLotList, beforeTrackOutLot, afterTrackOutLot);
		
		// Delete OriginalProductInfo data after SortFlow
		MESLotServiceProxy.getLotServiceUtil().deleteOriginalProductInfo(eventInfo, beforeTrackOutLot, afterTrackOutLot, productNameList);

		// [V3_MES_121_004]DSP Run Control_V1.02
		MESLotServiceProxy.getLotServiceUtil().runControlResetCountAndSendMail(eventInfo, machineName, beforeTrackOutLot, machineRecipeName, null);

		///BaseLineFlag = 'Y'
		CheckProductBaseLineFlag(productList,machineName,beforeTrackOutLot);
		
		if (CommonUtil.equalsIn(eqpData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Photo))
			afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().validateAndHoldLotAboutExposure(eventInfo, beforeTrackOutLot, afterTrackOutLot);

		if (StringUtils.equals(eqpData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Photo))
			insertPhotoData(eventInfo, machineName, machineRecipeName, productList);

		if (dummyProuctReserve)
			ExtendedObjectProxy.getDummyProductReserveService().checkProcessingFlag(eventInfo, beforeTrackOutLot, afterTrackOutLot);
		
		//TP Offset
		if(eqpData.getFactoryName().equals("TP") && StringUtils.equals(eqpData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Photo) && operationSpec.getUdfs().get("LAYERNAME").toString().equals("PEP1"))
		{
			MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
			String machineOffset = machineSpecData.getUdfs().get("OFFSETID").toString();
			for(Element productInfo : productList)
			{
				String productName = productInfo.getChild("PRODUCTNAME").toString();
				Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
				
				kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
				setEventInfo.getUdfs().put("OFFSET", machineOffset);
				
				MESProductServiceProxy.getProductServiceImpl().setEvent(productData, setEventInfo, eventInfo);
			}
		}
		
		String productOffset = "";
		
		//Get ProductData for TP Photo Offset ghhan
		if(eqpData.getFactoryName().equals("TP") && CommonUtil.equalsIn(eqpData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Photo))
		{
			List<ProductHistory> productHistoryList = ProductServiceProxy.getProductHistoryService().select(" productName = ? order by timekey desc ",
					new Object[] { productNameList.get(0)});
			
			for(ProductHistory productHistoryData : productHistoryList)
			{
				if(productHistoryData.getEventName().equals("TrackIn"))
				{
					productOffset = productHistoryData.getUdfs().get("OFFSET").toString();
					break;
				}
			}
		}
		
		if (MESRecipeServiceProxy.getRecipeServiceUtil().RMSFlagCheck("E", machineName, machineRecipeName, "", baseLotData.getProductSpecName(), baseLotData.getProcessFlowName(), baseLotData.getProcessOperationName(), productOffset))
		{
			MESRecipeServiceProxy.getRecipeServiceUtil().updateTrackOutTime(machineName, machineRecipeName, eventInfo);
		}
		
		// Update Recipe LastTrackOutTimekey
		//if (RMSFlag.equals("Y"))
		//{
		//	MESRecipeServiceProxy.getRecipeServiceUtil().updateTrackOutTime(machineName, machineRecipeName, eventInfo);
		//}
		
		// AutoShippingAction
		MESLotServiceProxy.getLotServiceUtil().autoShipLotAction(eventInfo, afterTrackOutLot);

		// return NewLot
		Document rtnDoc = new Document();
		rtnDoc = (Document) doc.clone();
		rtnDoc = SMessageUtil.addItemToBody(rtnDoc, "NEWLOTNAME", newLotName);

		updateReservedLotState(lotName, machineName, beforeTrackOutLot, eventInfo);
		
		//TrackOut Report for SAP////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(beforeTrackOutLot.getProductRequestName());
		
		try
		{
			String sapFlag=CommonUtil.getEnumDefValueStringByEnumName("SAPFLAG");
			if(StringUtil.isNotEmpty(sapFlag)&&StringUtil.equals(sapFlag, "Y")&&
					StringUtils.isNotEmpty(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
			{
				SuperProductRequest superWO = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
				
				MESConsumableServiceProxy.getConsumableServiceUtil().TrackOutERPBOMReportByMaterialProduct(eventInfo, baseLotData, superWO, machineName,productList.size());
			}
		}
		catch(Exception x)
		{
			eventLog.info("SAP Report Error");
		}

		if (StringUtil.isNotEmpty(baseLotData.getProductionType().toString())
				&& (StringUtil.equals(baseLotData.getProductionType(), "P")
						|| StringUtil.equals(baseLotData.getProductionType(), "E"))) 
		{
			try 
			{
				MESLotServiceProxy.getLotServiceImpl().sendEmailToMFG(eventInfo, baseLotData, lotName);
			} 
			catch (Exception e) 
			{
				eventLog.info(" Failed to send mail. ");
			}
		}
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		return rtnDoc;
	}

	private void modifyQtimeEnterTime(EventInfo eventInfoForReworkQtime, int productionCount, List<ProductQueueTime> qTimeDataList, ProcessOperationSpec operationSpec) throws CustomException 
	{
		if (productionCount == 1)
		{
			for (ProductQueueTime productQueueTime : qTimeDataList) 
			{
				productQueueTime.setEnterTime(eventInfoForReworkQtime.getEventTime());
				productQueueTime.setWarningTime(null);
				productQueueTime.setQueueTimeState(GenericServiceProxy.getConstantMap().QTIME_STATE_IN);

				ExtendedObjectProxy.getProductQTimeService().modify(eventInfoForReworkQtime, productQueueTime);
			}
		}
		else if (productionCount == 2)
		{
			if (operationSpec.getDetailProcessOperationType().equals("PH") || operationSpec.getDetailProcessOperationType().equals("STRIP"))
			{
				for (ProductQueueTime productQueueTime : qTimeDataList) 
				{
					productQueueTime.setEnterTime(eventInfoForReworkQtime.getEventTime());
					productQueueTime.setWarningTime(null);
					productQueueTime.setQueueTimeState(GenericServiceProxy.getConstantMap().QTIME_STATE_IN);

					ExtendedObjectProxy.getProductQTimeService().modify(eventInfoForReworkQtime, productQueueTime);
				}
			}
			else
			{
				log.info("Production ReworkOperCount Is 2,But Opera Is Not PH Or STRIP,So Not ReCount Q-Time");
			}
		}
		else
		{
			log.info("Production ReworkOperCount Is Not 1 Or 2,So Not ReCount Q-Time");
		}
		
	}

	private List<Map<String, Object>> getBeforeMainOperationNode(Lot trackOutLot) 
	{
		List<Map<String, Object>> beforeMainOper = null;
				
		String currentNode  = trackOutLot.getNodeStack(); //Get current NodeID
		String originalNode = currentNode.substring(0, currentNode.indexOf(".")); //Get original NodeID
		Node aNode = ProcessFlowServiceProxy.getNodeService().getNode(originalNode);
		
		String sqlForNode = "SELECT C.* FROM  NODE C,(SELECT A.FROMNODEID FROM  ARC A WHERE "
				+ "A.PROCESSFLOWNAME = :PROCESSFLOWNAME AND A.PROCESSFLOWVERSION = :PROCESSFLOWVERSION "
				+ "AND A.TONODEID = :TONODEID) B WHERE C.NODEID = B.FROMNODEID";
		
		Map<String, Object> bindMapForNode = new HashMap<String, Object>();
		bindMapForNode.put("PROCESSFLOWNAME", aNode.getProcessFlowName());
		bindMapForNode.put("PROCESSFLOWVERSION", aNode.getProcessFlowVersion());	
		bindMapForNode.put("TONODEID", aNode.getKey().getNodeId());
		
		
		try 
		{
			beforeMainOper = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlForNode, bindMapForNode);
		} 
		catch (Exception e) 
		{
			log.info("Can't Find beforeMainOper,So Not ReCount Q-Time");
			return null;
		}
		
		return beforeMainOper;
	}

	private int getReworkProductionOperCount(Lot trackOutLot) 
	{
		int productionCount = 0;
		String sql = "SELECT B.* FROM  NODE A ,PROCESSOPERATIONSPEC B WHERE A.FACTORYNAME = :FACTORYNAME AND A.PROCESSFLOWNAME = :PROCESSFLOWNAME "
				+ "AND A.NODETYPE = 'ProcessOperation' AND A.NODEATTRIBUTE1 = B.PROCESSOPERATIONNAME AND B.PROCESSOPERATIONTYPE = 'Production'";
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", trackOutLot.getFactoryName());
		bindMap.put("PROCESSFLOWNAME", trackOutLot.getProcessFlowName());			
		try 
		{
			List<Map<String, Object>> reworkOper = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			productionCount = reworkOper.size();
		} 
		catch (Exception e) 
		{
			log.info("Can't Find reworkOper,So Not ReCount Q-Time");
			return 0;
		}
		return productionCount;
	}

	private void updateFlagStack(Lot trackOutLot, List<String> productNameList, String machineName,String productSpec,ProductSpec productSpecInfo) throws CustomException
	{
		String sql = " SELECT ADDFLAG,DELETEFLAG ,DECODE(SUBUNITNAME,'-','LEVEL2','LEVEL3') AS UNITLEVEL " 
				   + " FROM CT_RUNBANRULE " 
				   + " WHERE 1=1 AND FACTORYNAME =?  AND PROCESSOPERATIONNAME = ? "
				   + " AND PROCESSOPERATIONVERSION = ? AND MACHINENAME = ? AND PROCESSFLOWTYPE =? " 
				   + " ORDER BY UNITLEVEL DESC ";

		Object[] bindSet = new Object[5];

		bindSet[0] = trackOutLot.getFactoryName();
		bindSet[1] = trackOutLot.getProcessOperationName();
		bindSet[2] = trackOutLot.getProcessOperationVersion();
		bindSet[3] = machineName;
		bindSet[4] = productSpec;

		List<Map<String, Object>> resultList = null;
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindSet);
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		if (resultList == null || resultList.size() == 0)
			return;

		Node nextNode = null;
		ProcessFlow processFlowData = null;
		List<Product> productDataList = null;
		ConstantMap constMap = GenericServiceProxy.getConstantMap();

		if (StringUtil.in(trackOutLot.getProductionType(), constMap.Pord_Production, constMap.Pord_Engineering, constMap.Pord_Test))
		{
			productDataList = MESProductServiceProxy.getProductServiceUtil().getRunbanProductList(productNameList);
		}
		else if (StringUtil.in(trackOutLot.getProductionType(), constMap.Pord_MQC, constMap.Pord_Dummy))
		{
			List<Map<String, Object>> mqcProductList = null;

			processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(trackOutLot);

			if (StringUtils.equals(processFlowData.getProcessFlowType(), "MQC"))
			{
				mqcProductList = ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().getMQCProduct(trackOutLot.getKey().getLotName());
			}
			else if (StringUtils.equals(processFlowData.getProcessFlowType(), "MQCRecycle"))
			{
				mqcProductList = ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().getMQCRecycleProduct(trackOutLot.getKey().getLotName());
			}

			if ((mqcProductList == null || mqcProductList.size() == 0)&&productSpecInfo.getUdfs().get("PRODUCTSPECGROUP").equals("MQC"))
			{
				throw new CustomException("LOT-0301", trackOutLot.getKey().getLotName(), trackOutLot.getProcessOperationName());
			}

			CommonUtil.makeListBySqlResult(mqcProductList, "PRODUCTNAME").retainAll(productNameList);
			productDataList = MESProductServiceProxy.getProductServiceUtil().getRunbanProductList(productNameList);
		}

		List<Product> updateProductList = new ArrayList<>();
		List<ProductHistory> updateHistList = new ArrayList<>();
		List<String[]> addFlagList=new ArrayList<String[]>();
		List<String[]> deleteFlagList=new ArrayList<String[]>();
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UpdateFlagStack", this.getEventUser(), this.getEventComment());
		
		for (int i = 0; i < resultList.size(); i++)
		{		
			String[] addFlags=StringUtil.split(CommonUtil.getValue(resultList.get(i), "ADDFLAG").toString(), ",");
			String[] deleteFlags=StringUtil.split(CommonUtil.getValue(resultList.get(i), "DELETEFLAG").toString(), ",");
			addFlagList.add(addFlags);
			deleteFlagList.add(deleteFlags);			
		}
		for (Product productData : productDataList)
		{
			Product oldData = (Product) ObjectUtil.copyTo(productData);
			
			List<String> flagStackList = new ArrayList<String>();
			CollectionUtils.addAll(flagStackList, org.springframework.util.StringUtils.commaDelimitedListToStringArray(productData.getUdfs().get("FLAGSTACK")));

			for(int i=0;i<addFlagList.size();i++)
			{
				for(int j=0;j<addFlagList.get(i).length;j++)
				{
					if (!flagStackList.contains(addFlagList.get(i)[j]))
					{
						flagStackList.add(addFlagList.get(i)[j]);
						productData.getUdfs().put("FLAGSTACK", org.springframework.util.StringUtils.collectionToCommaDelimitedString(flagStackList));
					}
				}
			}
			for(int i=0;i<deleteFlagList.size();i++)
			{
				for(int j=0;j<deleteFlagList.get(i).length;j++)
				{
					if (flagStackList.contains(deleteFlagList.get(i)[j]))
					{
						flagStackList.remove(deleteFlagList.get(i)[j]);
						productData.getUdfs().put("FLAGSTACK", org.springframework.util.StringUtils.collectionToCommaDelimitedString(flagStackList));
					}
				}
			}
	         
			productData.setLastEventName(eventInfo.getEventName());
			productData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			productData.setLastEventTime(eventInfo.getEventTime());
			productData.setLastEventUser(eventInfo.getEventUser());
			productData.setLastEventComment(eventInfo.getEventComment());

			ProductHistory dataHistory = ProductServiceProxy.getProductHistoryDataAdaptor().setHV(oldData, productData, new ProductHistory());

			updateProductList.add(productData);
			updateHistList.add(dataHistory);
		}

		try
		{
			if (updateProductList.size() < 0) return;

			CommonUtil.executeBatch("update", updateProductList, true);
			CommonUtil.executeBatch("insert", updateHistList, true);

			eventLog.info(String.format("Successfully update %s products.", updateProductList.size()));
		}
		catch (Exception e)
		{
			eventLog.error(e.getMessage());
			throw new CustomException(e.getCause());
		}
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
				String[] flagArray = StringUtils.split(productPGSRC.getUdfs().get("FLAGSTACK"), ",");

				List<String> flagList = new ArrayList<String>();
				flagList.add(flag);

				for (int i = 0; i < flagArray.length; i++)
				{
					String tempFlag = flagArray[i];

					if (StringUtils.isNotEmpty(tempFlag) && !flagList.contains(tempFlag))
						flagList.add(tempFlag);
				}

				for (Map<String, Object> map : result2)
				{
					String deleteFlag = ConvertUtil.getMapValueByName(map, "DELETEFLAG");

					if (flagList.contains(deleteFlag))
						flagList.remove(deleteFlag);
				}

				String flagStack = StringUtils.join(flagList, ",");

				productPGSRC.getUdfs().put("FLAGSTACK", flagStack.toString());
			}
		}
	}

	private void updateReservedLotState(String lotName, String machineName, Lot lotData, EventInfo eventInfo) throws CustomException
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

			long remainLotQty = productRequestPlan.get(0).getPlanLotQuantity() - 1;

			if (remainLotQty == 0)
				productRequestPlan.get(0).setPlanState("Completed");

			productRequestPlan.get(0).setPlanLotQuantity(remainLotQty);
			ExtendedObjectProxy.getDSPProductRequestPlanService().modify(eventInfo, productRequestPlan.get(0));
		}
		catch (Exception e)
		{
			eventLog.info("Fail ReservedLot Updating");
		}
	}

	//Mantis 0000394
	//2021-05-12 ForceSampling RepairSkip 
	private Lot getNextOperationAfterReviewStation(EventInfo eventInfo, Lot trackOutLot, String lotJudge, boolean firstCheck, boolean forceFlag) throws CustomException
	{
		// Array Review Station - If the Lot judge is P then goes to repair operation else skip.
		ProcessFlow currentFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(trackOutLot);

		if (CommonUtil.equalsIn(currentFlowData.getProcessFlowType(), "Inspection", "Sample"))
		{
			// get current operation data
			ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(trackOutLot.getFactoryName(), trackOutLot.getProcessOperationName(), trackOutLot.getProcessOperationVersion());

			// If current operation is Review Station
			if ((firstCheck && (StringUtils.equals(operationData.getDetailProcessOperationType(), "VIEW") || StringUtils.equals(operationData.getDetailProcessOperationType(), "RP")) && !StringUtils.equals(lotJudge, GenericServiceProxy.getConstantMap().LotGrade_P))
					|| !firstCheck || StringUtils.isNotEmpty(trackOutLot.getUdfs().get("JOBNAME")))
			{
				List<Map<String, Object>> skipFlows = CommonUtil.getEnumDefValueByEnumName("ArrayReviewStationSampleSkipFlow");
				if (skipFlows.size() > 0)
				{
					for (Map<String, Object> skipFlow : skipFlows)
					{
						if (ConvertUtil.getMapValueByName(skipFlow, "ENUMVALUE").equalsIgnoreCase(trackOutLot.getProcessFlowName()))
							return trackOutLot;
					}
				}
				Map<String, String> trackOutLotUdfs = trackOutLot.getUdfs();

				Node nextNode = new Node();
				
				if(forceFlag)
				{
					String[] nodeStackArray = StringUtil.split(trackOutLot.getNodeStack(), ".");
					nextNode = ProcessFlowServiceProxy.getNodeService().getNode(nodeStackArray[nodeStackArray.length - 2]);
				}
				else
				{
					nextNode = PolicyUtil.getNextOperation(trackOutLot);
				}

				ProcessOperationSpec nextOperationData = CommonUtil.getProcessOperationSpec(nextNode.getFactoryName(), nextNode.getNodeAttribute1(), nextNode.getNodeAttribute2());

				if (StringUtils.equals(nextOperationData.getDetailProcessOperationType(), "RP"))
				{
					// Next Operation
					ProcessFlowKey processFlowKey1 = new ProcessFlowKey();
					processFlowKey1.setFactoryName(nextNode.getFactoryName());
					processFlowKey1.setProcessFlowName(nextNode.getProcessFlowName());
					processFlowKey1.setProcessFlowVersion(nextNode.getProcessFlowVersion());

					ProcessFlow nextNodeFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey1);

					if (StringUtils.equals(nextNodeFlowData.getProcessFlowType(), "Main") && nextNode.getProcessFlowName().equals(CommonUtil.getValue(trackOutLotUdfs, "RETURNFLOWNAME"))
							&& nextNode.getNodeAttribute1().equals(CommonUtil.getValue(trackOutLotUdfs, "RETURNOPERATIONNAME")))
					{
						trackOutLotUdfs.put("RETURNFLOWNAME", "");
						trackOutLotUdfs.put("RETURNOPERATIONNAME", "");
					}
					else
					{
						String nodeStack = "";
						String currentNode = trackOutLot.getNodeStack();
						boolean nextForce = false;
						
						if(forceFlag)
						{
							String currentFlowName = trackOutLot.getProcessFlowName();
							String currentFlowVersion = trackOutLot.getProcessFlowVersion();
							String currentOperationName = trackOutLot.getProcessOperationName();
							String currentOperationVersion = trackOutLot.getProcessOperationVersion();
							
							nodeStack = currentNode.substring(0, currentNode.lastIndexOf("."));
							
							trackOutLot.setNodeStack(nodeStack);
							trackOutLot.setProcessFlowName(nextNode.getProcessFlowName());
							trackOutLot.setProcessFlowVersion(nextNode.getProcessFlowVersion());
							trackOutLot.setProcessOperationName(nextNode.getNodeAttribute1());
							trackOutLot.setProcessOperationVersion(nextNode.getNodeAttribute2());
							
							eventLog.info("Next force check");
							List<SampleLot> sampleLotList = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListByToInfo(trackOutLot.getKey().getLotName(), trackOutLot.getFactoryName(),
									trackOutLot.getProductSpecName(), trackOutLot.getProductSpecVersion(), trackOutLot.getProcessFlowName(), trackOutLot.getProcessFlowVersion(),
									trackOutLot.getProcessOperationName(), trackOutLot.getProcessOperationVersion());
							
							if(sampleLotList != null)
							{
								if(StringUtils.isNotEmpty(sampleLotList.get(0).getForceSamplingFlag()))
								{
									nextForce = true;
								}
							}
							eventInfo.setEventName("SkipRepairOperation");
							MESLotServiceProxy.getLotServiceUtil().deleteSamplingDataReturn(eventInfo, trackOutLot, false);
							
							trackOutLot.setProcessFlowName(currentFlowName);
							trackOutLot.setProcessFlowVersion(currentFlowVersion);
							trackOutLot.setProcessOperationName(currentOperationName);
							trackOutLot.setProcessOperationVersion(currentOperationVersion);
							
							if(nextForce)
							{
								Map<String, String> setUdfs = trackOutLot.getUdfs();
								setUdfs.put("RETURNFLOWNAME", sampleLotList.get(0).getReturnProcessFlowName());
								setUdfs.put("RETURNOPERATIONNAME", sampleLotList.get(0).getReturnOperationName());
								setUdfs.put("RETURNOPERATIONVER", sampleLotList.get(0).getReturnOperationVersion());
							}

							LotServiceProxy.getLotService().update(trackOutLot);
							
							eventLog.info("Skip Repair Operation after ReviewStation because the LotJudge is " + lotJudge);

							trackOutLot = getNextOperationAfterReviewStation(eventInfo, trackOutLot, lotJudge, false, nextForce);
						}
						else
						{
							if (StringUtils.contains(currentNode, "."))
							{
								String currentFlowName = trackOutLot.getProcessFlowName();
								String currentFlowVersion = trackOutLot.getProcessFlowVersion();
								String currentOperationName = trackOutLot.getProcessOperationName();
								String currentOperationVersion = trackOutLot.getProcessOperationVersion();

								String originalNode = currentNode.substring(0, currentNode.lastIndexOf("."));
								nodeStack = originalNode + "." + nextNode.getKey().getNodeId();

								trackOutLot.setNodeStack(nodeStack);
								trackOutLot.setProcessFlowName(nextNode.getProcessFlowName());
								trackOutLot.setProcessFlowVersion(nextNode.getProcessFlowVersion());
								trackOutLot.setProcessOperationName(nextNode.getNodeAttribute1());
								trackOutLot.setProcessOperationVersion(nextNode.getNodeAttribute2());

								MESLotServiceProxy.getLotServiceUtil().deleteSamplingDataReturn(eventInfo, trackOutLot, false);

								trackOutLot.setProcessFlowName(currentFlowName);
								trackOutLot.setProcessFlowVersion(currentFlowVersion);
								trackOutLot.setProcessOperationName(currentOperationName);
								trackOutLot.setProcessOperationVersion(currentOperationVersion);

								LotServiceProxy.getLotService().update(trackOutLot);
								eventLog.info("Skip Repair Operation after ReviewStation because the LotJudge is " + lotJudge);

								trackOutLot = getNextOperationAfterReviewStation(eventInfo, trackOutLot, lotJudge, false, nextForce);
							}
						}
					}
				}
			}
		}

		return trackOutLot;
	}

	public void AOILotJudge(Lot baseLotData, String lotName)
	{

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT M.MACHINEGROUPNAME ");
		sql.append("  FROM MACHINESPEC M ");
		sql.append(" WHERE 1 = 1 ");
		sql.append("   AND M.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND M.DETAILMACHINETYPE = 'MAIN' ");
		sql.append("   AND M.MACHINENAME = :MACHINENAME ");
		sql.append("ORDER BY M.MACHINENAME ASC ");

		Map<String, Object> args = new HashMap<>();

		args.put("FACTORYNAME", baseLotData.getFactoryName());
		args.put("MACHINENAME", baseLotData.getMachineName());

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), args);

		if (result.get(0).toString().equals("{MACHINEGROUPNAME=AutoMAC}") || result.get(0).toString().equals("{MACHINEGROUPNAME=ParticleCounter}"))
		{
			StringBuilder insql = new StringBuilder();
			insql.append("INSERT INTO CT_AOILOT (LOTNAME, FACTORYNAME, LOTJUDGE, EVENTUSER, EVENTTIME, TIMEKEY, MACHINENAME) ");
			insql.append("VALUES (:LOTNAME, :FACTORYNAME, 'N', :EVENTUSER, :EVENTTIME, :TIMEKEY, :MACHINENAME) ");

			String CurrentTime = TimeStampUtil.getCurrentTime(TimeStampUtil.FORMAT_DEFAULT);
			String timeKey = TimeStampUtil.getCurrentEventTimeKey();

			Map<String, Object> arg = new HashMap<String, Object>();
			arg.put("LOTNAME", lotName);
			arg.put("FACTORYNAME", baseLotData.getFactoryName());
			arg.put("MACHINENAME", baseLotData.getMachineName());
			arg.put("EVENTUSER", baseLotData.getLastEventUser());
			arg.put("EVENTTIME", CurrentTime);
			arg.put("TIMEKEY", timeKey);

			GenericServiceProxy.getSqlMesTemplate().update(insql.toString(), arg);

			SetEventInfo setEventInfo = new SetEventInfo();
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("AutoLotJudge-N", getEventUser(), getEventComment(), "", "");
			LotServiceProxy.getLotService().setEvent(baseLotData.getKey(), eventInfo, setEventInfo);
		}
	}

	// Insert PhotoMachineName, ExposureReipceName
	private void insertPhotoData(EventInfo eventInfo, String machineName, String recipeName, List<Element> productElementList)
	{
		try
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfo.setEventName("InsertPhotoMachineInfo");

			String exposureRecipeName = StringUtils.substring(recipeName, 13, 17);

			for (Element productElement : productElementList)
			{
				String productName = productElement.getChildText("PRODUCTNAME");

				Product productData = ProductServiceProxy.getProductService().selectByKey(new ProductKey(productName));

				kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
				setEventInfo.getUdfs().put("LASTPHOTOMACHINENAME", machineName);
				setEventInfo.getUdfs().put("EXPOSURERECIPENAME", exposureRecipeName);

				MESProductServiceProxy.getProductServiceImpl().setEvent(productData, setEventInfo, eventInfo);
			}
		}
		catch (Exception ex)
		{

		}
	}

	private void updatePhotoOffset(List<String> productNameList, String machineName, ProcessOperationSpec operationSpec,int currentStepOffset) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UpdateOffset", getEventUser(), getEventComment(), "", "", "");

		for (String productName : productNameList)
		{
			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

			String offset = productData.getUdfs().get("OFFSET").toString();
			MachineSpec machineSpec = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
			String machineOffset = machineSpec.getUdfs().get("OFFSETID").toString();
			if(StringUtils.isNotEmpty(offset)&&offset.length()>=currentStepOffset)
			{
				String tempOffset="";
				for(int i=0;i<offset.length();i++)
				{
					if(i!=(currentStepOffset-1))
						tempOffset+=offset.charAt(i);
					else if(currentStepOffset!=1)
					{
						tempOffset+=machineOffset;
					}
					else tempOffset=machineOffset;
				}
				offset=tempOffset;
			}
			else if((StringUtils.isNotEmpty(offset)&&offset.length()==currentStepOffset-1)
					||(StringUtils.isEmpty(offset)&&currentStepOffset==1))
			{
				offset+=machineOffset;
			}
			else if((StringUtils.isNotEmpty(offset)&&offset.length()< currentStepOffset)
					||(StringUtils.isEmpty(offset)&&currentStepOffset!=1))
			{
				for(int i=0;i<currentStepOffset;i++)
				{
					offset+="0";
					if(offset.length()==currentStepOffset-1)
					{
						offset+=machineOffset;
						break;
					}
				}
			}

			kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
			setEventInfo.getUdfs().put("OFFSET", offset);

			MESProductServiceProxy.getProductServiceImpl().setEvent(productData, setEventInfo, eventInfo);
		}
	}
	private void TPReviewCheck(Lot lotData) throws CustomException
	{
		List<Product> lotProductList = MESProductServiceProxy.getProductServiceUtil().allUnScrappedProductsByLot(lotData.getKey().getLotName());

		StringBuffer querySql = new StringBuffer();
		querySql.append("SELECT PRODUCTNAME, PROCESSOPERATIONNAME, MACHINENAME, PRODUCTJUDGE ");
		querySql.append("  FROM CT_REVIEWPRODUCTJUDGE ");
		querySql.append(" WHERE PRODUCTNAME = :PRODUCTNAME AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");

		for(Product productData : lotProductList)
		{
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("PRODUCTNAME", productData.getKey().getProductName());
			args.put("PROCESSOPERATIONNAME", "21220");

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(querySql.toString(), args);
			
			if(result.isEmpty())
			{
				throw new CustomException("LOT-0400", productData.getKey().getProductName());
			}
		}
	}
	
	private Lot OfflineRSSkip(Lot preLotData) throws CustomException
	{
		ReviewOperationInfo rsInfo = new ReviewOperationInfo();
		Lot postLotData = (Lot) ObjectUtil.copyTo(preLotData);
		
		try 
		{
			rsInfo = ExtendedObjectProxy.getReviewOperationInfoService().selectByKey(false, new Object[] {preLotData.getProcessOperationName() , preLotData.getProcessOperationVersion()});
		} 
		catch (Exception e) 
		{
			return preLotData;
		}
		
		if(!rsInfo.getOfflineFlag().equals("Y"))
		{
			return preLotData;
		}
		else
		{
			if (StringUtils.equals(preLotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_NotOnHold))
			{
				boolean mainFlow = false;
				while(!mainFlow)
				{
					EventInfo eventInfo = EventInfoUtil.makeEventInfo("RSOfflineSkip", this.getEventUser(), this.getEventComment(), "", "");
					Map<String, String> udfs = new HashMap<String, String>();
					ChangeSpecInfo skipInfo = MESLotServiceProxy.getLotInfoUtil().skipInfo(eventInfo, postLotData, udfs, new ArrayList<ProductU>());
					postLotData = MESLotServiceProxy.getLotServiceUtil().skip(eventInfo, postLotData, skipInfo, false);
					
					ProcessFlow flowSpec = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(new ProcessFlowKey(postLotData.getFactoryName(), postLotData.getProcessFlowName(), postLotData.getProcessFlowVersion()));
					
					if(flowSpec.getProcessFlowType().equals("Main"))
					{
						mainFlow = true;
					}
				}
			}
			else
			{
				return preLotData;
			}
		}
		
		return postLotData;
	}
	private void CheckProductBaseLineFlag(List<Element> productList, String machineName, Lot beforeTrackOutLot)throws CustomException
	{
		//Ct_ValicationProduct
		String ProductSpec = beforeTrackOutLot.getProductSpecName();
		String currentFlowName = beforeTrackOutLot.getProcessFlowName();
		String currentOperationName = beforeTrackOutLot.getProcessOperationName();
		String currentMachineName = machineName;
		for(Element productInfo : productList)
		{
			String productName = productInfo.getChildText("PRODUCTNAME");
			String sql= "SELECT PRODUCTNAME,ENGOPERATIONNAME,ENGMACHINENAME,TOFLOWNAME,SAMPLEOPERATIONNAME "
					+ "FROM CT_VALICATIONPRODUCT WHERE 1=1 "
					+ "AND  PRODUCTNAME = :PRODUCTNAME  "
					+ "AND PRODUCTSPECNAME = :PRODUCTSPECNAME "
					+ "AND PROCESSFLOWNAME = :PROCESSFLOWNAME "
					+ "AND ENGOPERATIONNAME = :ENGOPERATIONNAME "
					+ "AND ENGMACHINENAME= :ENGMACHINENAME";
		
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("PRODUCTNAME",productName);
			bindMap.put("PRODUCTSPECNAME",ProductSpec);
			bindMap.put("PROCESSFLOWNAME",currentFlowName);
			bindMap.put("ENGOPERATIONNAME",currentOperationName);
			bindMap.put("ENGMACHINENAME",currentMachineName);
			
			List<Map<String, Object>> result;
			try
			{
				result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			}
			catch (FrameworkErrorSignal fe)
			{
				result = null;
				throw new CustomException("SYS-9999", fe.getMessage());
			}
			if(result != null && result.size() > 0)
			{
				String sql2 = "UPDATE CT_VALICATIONPRODUCT set BASELINEFLAG = 'Y' "
						+ "WHERE PRODUCTNAME = :PRODUCTNAME  "
						+ "AND PRODUCTSPECNAME = :PRODUCTSPECNAME "
						+ "AND PROCESSFLOWNAME = :PROCESSFLOWNAME "
						+ "AND ENGOPERATIONNAME = :ENGOPERATIONNAME "
						+ "AND ENGMACHINENAME= :ENGMACHINENAME";
				GenericServiceProxy.getSqlMesTemplate().update(sql2, bindMap);
			}	
		}
	}
	
	private List<String> getProcessedProductList(List<String> productNameList) throws CustomException 
	{
		List<String> processedProductList=new ArrayList<String>();
		List<String> abortProductList=new ArrayList<String>();
		String sql= "SELECT PRODUCTNAME, "
				+ "    CASE WHEN PROCESSINGINFO='B' THEN 1 ELSE 0 END AS ABORTFLAG, "
				+ "    CASE WHEN REWORKFLAG='Y' THEN 1 ELSE 0 END AS REWORKFLAG  "
				+ "   FROM PRODUCT  "
				+ "   WHERE PRODUCTNAME IN (:PRODUCTNAMELIST)  "
				+ "        AND PRODUCTSTATE='InProduction' ";
	
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("PRODUCTNAMELIST",productNameList);
		
		List<Map<String, Object>> result= GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		if(result!=null&&result.size()>0)
		{
			for(int i=0;i<result.size();i++)
			{
				if(StringUtils.equals(result.get(i).get("ABORTFLAG").toString(), "1"))
				{
					abortProductList.add(result.get(i).get("PRODUCTNAME").toString());
				}
				if(StringUtils.equals(result.get(i).get("REWORKFLAG").toString(), "1"))
				{
					processedProductList.add(result.get(i).get("PRODUCTNAME").toString());
				}
			}
		}
		
		if(abortProductList.size()>0)
		{
			return abortProductList;
		}
		else
		{
			return processedProductList;
		}	
	}
}
