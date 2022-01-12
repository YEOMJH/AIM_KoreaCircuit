package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DSPProductRequestPlan;
import kr.co.aim.messolution.extended.object.management.data.ProductQueueTime;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.extended.object.management.data.TPOffsetAlignInfo;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.processgroup.MESProductServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.ext.ConsumedMaterial;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class CancelTrackInLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);

		String newLotName = "";

		// for common
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);

		Map<String, String> assignCarrierUdfs = new HashMap<String, String>();
		Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();

		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);

		MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);

		String portType = portData.getUdfs().get("PORTTYPE");
		String machineDesc = machineSpecData.getMachineGroupName();

		// Check validation for carrierName(portType = PL)
		if (StringUtil.equals(portData.getUdfs().get("PORTTYPE"), "PL"))
			CommonValidation.checkAvailableCST(carrierName, portType, machineDesc);

		Lot cancelTrackInLot = new Lot();

		// get LotList by ProductList
		List<Map<String, Object>> lotListByProductList = MESLotServiceProxy.getLotServiceUtil().getLotListByProductList(productList);

		// nothing to track out case
		if (lotListByProductList.size() < 1)
			new CustomException("LOT-9001", "PRODUCTLIST : " + CommonUtil.makeListForQuery(productList, "PRODUCTNAME"));

		// first Lot of LotList is Base Lot : much productQty Lot of LotList
		Lot baseLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotListByProductList.get(0).get("LOTNAME").toString());

		// Check ProductType for Cutting
		if (factoryName.equals("OLED"))
		{
			Element bodyElement = SMessageUtil.getBodyElement(doc);
			List<String> prodList = CommonUtil.makeList(bodyElement, "PRODUCTLIST", "PRODUCTNAME");
			ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(factoryName, baseLotData.getProcessOperationName(), baseLotData.getProcessOperationVersion());

			if (operationData.getDetailProcessOperationType().equals("CUT"))
				checkProductType(lotName, prodList);
		}

		ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(baseLotData.getFactoryName(), baseLotData.getProductSpecName(), baseLotData.getProductSpecVersion());

		// when Q-Glass Cutting TrackOut
		double subProductUnitQuantity1 = MESLotServiceProxy.getLotServiceUtil().convertSubProductQuantity(baseLotData, productSpecData, productList, lotListByProductList.size(), productList.size());

		if (lotListByProductList.size() == 1 && (productList.size() == baseLotData.getProductQuantity()))
		{// after update on subProductUnitQuantity then keep original
			cancelTrackInLot = MESLotServiceProxy.getLotInfoUtil().getLotData(baseLotData.getKey().getLotName());
			if (factoryName.equals("OLED"))
			{
				Element bodyElement = SMessageUtil.getBodyElement(doc);
				List<String> prodList = CommonUtil.makeList(bodyElement, "PRODUCTLIST", "PRODUCTNAME");
				ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(factoryName, baseLotData.getProcessOperationName(), baseLotData.getProcessOperationVersion());

				if (operationData.getDetailProcessOperationType().equals("CUT"))
				{
					checkProductType(lotName, prodList);

					String condition = "UPDATE LOT SET PRODUCTTYPE = 'Sheet' WHERE lotName = ? ";
					Object[] bindSet = new Object[] { cancelTrackInLot.getKey().getLotName() };
					kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().update(condition, bindSet);

				}
			}
		}
		else
		{// create and merge
			
			List<String> srcLotList = new ArrayList<>();
			List<String> destLotList = new ArrayList<>();
			
			Lot newLotData = MESLotServiceProxy.getLotServiceUtil().createNewLotWithSplit(eventInfo, baseLotData, productSpecData, subProductUnitQuantity1, "", assignCarrierUdfs);
			newLotName = newLotData.getKey().getLotName();
			destLotList.add(newLotName);
			
			List<Map<String, Object>> lotListByNoCancelProductList = MESLotServiceProxy.getLotServiceUtil().getLotListByProductList(productList);

			// nothing to track out case
			if (lotListByNoCancelProductList.size() < 1)
				new CustomException("LOT-9001", "PRODUCTLIST : " + CommonUtil.makeListForQuery(productList, "PRODUCTNAME"));

			for (Map<String, Object> lotM : lotListByNoCancelProductList)
			{
				String sLotName = CommonUtil.getValue(lotM, "LOTNAME");
				String sProductQuantity = CommonUtil.getValue(lotM, "PRODUCTQTY");

				srcLotList.add(sLotName);
				MESLotServiceProxy.getLotServiceUtil().transferProductsToLot(eventInfo, newLotData, portData, sLotName, sProductQuantity, deassignCarrierUdfs, productList);
			}

			cancelTrackInLot = MESLotServiceProxy.getLotInfoUtil().getLotData(newLotName);
			if (factoryName.equals("OLED"))
			{
				ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(factoryName, baseLotData.getProcessOperationName(), baseLotData.getProcessOperationVersion());

				if (operationData.getDetailProcessOperationType().equals("CUT"))
				{
					String condition = "UPDATE LOT SET PRODUCTTYPE = 'Sheet' WHERE lotName = ? ";
					Object[] bindSet = new Object[] { cancelTrackInLot.getKey().getLotName() };
					kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().update(condition, bindSet);
				}
			}

			Lot remainLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

			deassignCarrier(eventInfo, remainLotData);

			ProductSpec baseData = GenericServiceProxy.getSpecUtil().getProductSpec(baseLotData.getFactoryName(), baseLotData.getProductSpecName(),
					GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
			if (CommonUtil.equalsIn(baseData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare"))
			{
				eventLog.info("ProductSpecGroup: " + baseData.getUdfs().get("PRODUCTSPECGROUP"));

				List<String> sProductList = MESLotServiceProxy.getLotServiceUtil().getProductListByElementList(productList);
				MESLotServiceProxy.getLotServiceUtil().createMQCWithReturnBank(baseLotData, newLotData, sProductList, carrierName, eventInfo);
			}
			
			// sync TPTJ product 
			MESLotServiceProxy.getLotServiceUtil().syncTPTJData(eventInfo, srcLotList, destLotList);
		}

		//Decrease RecipeUsedTime
		
		//PhotoOffset	
		boolean isPhoto = false;
		
		if (CommonUtil.equalsIn(machineSpecData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Photo))
			isPhoto = true;
		
		String machineRecipeName = "";
		
		if(isPhoto)
		{
			if(machineSpecData.getFactoryName().equals("TP"))
			{
				String productOffset = "";
				
				String productName = productList.get(0).getChild("PRODUCTNAME").getText();
				
				Product productData = kr.co.aim.messolution.product.MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
				
				ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(factoryName, baseLotData.getProcessOperationName(), baseLotData.getProcessOperationVersion());
				
				if(productData.getUdfs().get("OFFSET") != null)
				{
					productOffset = productData.getUdfs().get("OFFSET").toString();
				}
				
				if((operationData.getUdfs().get("LAYERNAME").toString().equals("PEP1") || operationData.getUdfs().get("LAYERNAME").toString().equals("PEP0")))
				{
					machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(baseLotData.getFactoryName(),
							baseLotData.getProductSpecName(), baseLotData.getProductSpecVersion(), baseLotData.getProcessFlowName(), baseLotData.getProcessFlowVersion(), 
							baseLotData.getProcessOperationName(), baseLotData.getProcessOperationVersion(), machineSpecData.getKey().getMachineName(), true);
					
					if(StringUtil.isEmpty(machineRecipeName))
					{
						throw new CustomException("RMS-0005");
					}
					else{
						CommonValidation.checkRecipeV3(cancelTrackInLot.getFactoryName(), cancelTrackInLot.getProductSpecName(), cancelTrackInLot.getProductSpecVersion(), cancelTrackInLot.getProcessFlowName(), cancelTrackInLot.getProcessFlowVersion(),
								cancelTrackInLot.getProcessOperationName(), cancelTrackInLot.getProcessOperationVersion(), machineName, machineRecipeName, false);
					}
				}
				else if(((!operationData.getUdfs().get("LAYERNAME").toString().equals("PEP1")) && (!operationData.getUdfs().get("LAYERNAME").toString().equals("PEP0"))) && StringUtil.isEmpty(productOffset))
				{
					throw new CustomException("RMS-0005");
				}
				else if(((!operationData.getUdfs().get("LAYERNAME").toString().equals("PEP1")) && (!operationData.getUdfs().get("LAYERNAME").toString().equals("PEP0"))) && StringUtil.isNotEmpty(productOffset))
				{
					TPOffsetAlignInfo offsetInfo = ExtendedObjectProxy.getTPOffsetAlignInfoService().selectByKey(false, new Object[]{productOffset, operationData.getKey().getProcessOperationName(), operationData.getKey().getProcessOperationVersion(), machineSpecData.getKey().getMachineName()});
					
					machineRecipeName = offsetInfo.getRecipeName();
						
					String RMSFlag = offsetInfo.getRMSFlag();
						
					if (!StringUtils.equals(RMSFlag, "N"))
					{
						MESRecipeServiceProxy.getRecipeServiceUtil().checkRecipeOnCancelTrackInTime(machineName, machineRecipeName);
					}
				else
				{
					throw new CustomException("RMS-0005");
				}
				}
			}
		}
		else
		{
			if(!StringUtils.equals(factoryName,"POSTCELL")&&
					StringUtils.equals(machineSpecData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Sorter))
			{
				machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(cancelTrackInLot.getFactoryName(),
						cancelTrackInLot.getProductSpecName(), cancelTrackInLot.getProductSpecVersion(), cancelTrackInLot.getProcessFlowName(), cancelTrackInLot.getProcessFlowVersion(), 
						cancelTrackInLot.getProcessOperationName(), cancelTrackInLot.getProcessOperationVersion(), machineName, true);
				
				CommonValidation.checkRecipeV3(cancelTrackInLot.getFactoryName(), cancelTrackInLot.getProductSpecName(), cancelTrackInLot.getProductSpecVersion(), cancelTrackInLot.getProcessFlowName(), cancelTrackInLot.getProcessFlowVersion(),
						cancelTrackInLot.getProcessOperationName(), cancelTrackInLot.getProcessOperationVersion(), machineName, machineRecipeName, false);
			}
			
		}
		
		//Q-Time
		ExtendedObjectProxy.getProductQTimeService().changeQTime(eventInfo, baseLotData.getKey().getLotName(), baseLotData.getFactoryName(), baseLotData.getProcessFlowName(), baseLotData.getProcessOperationName(), productList);
		
		List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequenceForManualCancel(SMessageUtil.getBodyElement(doc));

		deassignCarrierUdfs.clear();
		assignCarrierUdfs.clear();
		List<ConsumedMaterial> lotConsumedMaterail = new ArrayList<ConsumedMaterial>();

		eventInfo = EventInfoUtil.makeEventInfo("CancelTrackIn", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		cancelTrackInLot = MESLotServiceProxy.getLotServiceImpl().cancelTrackIn(eventInfo, cancelTrackInLot, productPGSRCSequence, assignCarrierUdfs, deassignCarrierUdfs, lotConsumedMaterail,
				carrierName);


		// [V3_MES_121_004]DSP Run Control_V1.02
		MESLotServiceProxy.getLotServiceUtil().decreaseRunControlUseCount(eventInfo, machineName, lotName, productList.size());

		if (!StringUtil.equals(cancelTrackInLot.getLotHoldState(), "Y"))
		{
			// Set ReasonCode
			eventInfo.setReasonCodeType("HOLD");
			eventInfo.setReasonCode("HD100");

			// LotMultiHold
			MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, cancelTrackInLot, new HashMap<String, String>());
		}
		
		updateReservedLotState(lotName, machineName, cancelTrackInLot, eventInfo);

		String returnLotname = cancelTrackInLot.getKey().getLotName();

		Document rtnDoc = new Document();
		rtnDoc = (Document) doc.clone();
		rtnDoc = SMessageUtil.addItemToBody(rtnDoc, "CANCELLOTNAME", returnLotname);

		return rtnDoc;
	}

	private void deassignCarrier(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());

		if (StringUtils.equals(carrierData.getDurableState(), "InUse"))
		{
			eventInfo.setEventName("Deassign");

			List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);

			DeassignCarrierInfo deassignCarrierInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, carrierData, productUSequence);

			LotServiceProxy.getLotService().deassignCarrier(lotData.getKey(), eventInfo, deassignCarrierInfo);
		}
	}

	private void checkProductType(String lotName, List<String> productList) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT PRODUCTTYPE ");
		sql.append("  FROM PRODUCT ");
		sql.append(" WHERE LOTNAME = :LOTNAME ");
		sql.append("   AND PRODUCTNAME IN (:PRODUCTNAME) ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("LOTNAME", lotName);
		args.put("PRODUCTNAME", productList);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			if (result.size() > 1)
			{
				throw new CustomException("LOT-0072");
			}
			else
			{
				String productType = ConvertUtil.getMapValueByName(result.get(0), "PRODUCTTYPE");

				if (!productType.equals("Sheet"))
					throw new CustomException("LOT-0072");
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
			eventLog.info("Fail ReservedLot Updating");
		}
	}
	
	
	public void lockQTime(EventInfo eventInfo, String productName, String factoryName, String processFlowName, String processOperationName, String toFactoryName, String toProcessFlowName,
			String toProcessOperationName) throws CustomException
	{
		try
		{
			eventInfo.setEventName("Interlock");

			ProductQueueTime QtimeData = ExtendedObjectProxy.getProductQTimeService().selectByKey(true,
					new Object[] { productName, factoryName, processFlowName, processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName });
			QtimeData.setInterlockTime(eventInfo.getEventTime());
			QtimeData.setQueueTimeState(GenericServiceProxy.getConstantMap().QTIME_STATE_OVER);

			ExtendedObjectProxy.getProductQTimeService().modify(eventInfo, QtimeData);
		}
		catch (greenFrameDBErrorSignal ne)
		{
			// ignore error to consider as not found exception signal
			throw new CustomException("LOT-0201", productName, processOperationName, toProcessOperationName);
		}
	}
	
	public void warnQTime(EventInfo eventInfo, String productName, String factoryName, String processFlowName, String processOperationName, String toFactoryName, String toProcessFlowName,
			String toProcessOperationName) throws CustomException
	{
		try
		{
			eventInfo.setEventName("Warn");

			ProductQueueTime QtimeData = ExtendedObjectProxy.getProductQTimeService().selectByKey(true,
					new Object[] { productName, factoryName, processFlowName, processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName });
			QtimeData.setWarningTime(eventInfo.getEventTime());
			QtimeData.setQueueTimeState(GenericServiceProxy.getConstantMap().QTIME_STATE_WARN);

			ExtendedObjectProxy.getProductQTimeService().modify(eventInfo, QtimeData);
		}
		catch (greenFrameDBErrorSignal ne)
		{
			// ignore error to consider as not found exception signal
			throw new CustomException("LOT-0201", productName, processOperationName, toProcessOperationName);
		}
	}
	
	public void exitQTime(EventInfo eventInfo, ProductQueueTime QTimeData) throws CustomException
	{
		try
		{
			eventInfo.setEventName("Exit");

			QTimeData.setExitTime(eventInfo.getEventTime());
			ExtendedObjectProxy.getProductQTimeService().remove(eventInfo, QTimeData);
		}
		catch (greenFrameDBErrorSignal ne)
		{
			// ignore error to consider as not found exception signal
			throw new CustomException("LOT-0201", QTimeData.getProductName(), QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName());
		}
	}
}
