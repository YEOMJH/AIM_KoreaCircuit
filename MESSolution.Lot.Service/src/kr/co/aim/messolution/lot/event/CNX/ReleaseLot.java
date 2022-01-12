package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaterialProduct;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.GradeDefUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.product.service.ProductPP;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.MakeLoggedInInfo;
import kr.co.aim.greentrack.lot.management.info.MakeReleasedInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.data.PortSpec;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductC;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.IncrementReleasedQuantityByInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ReleaseLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String plPort = SMessageUtil.getBodyItemValue(doc, "PLPORT", true);
		String puPort = SMessageUtil.getBodyItemValue(doc, "PUPORT", true);

		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);

		// event information
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);

		for (Element lot : lotList)
		{
			String sLotName = SMessageUtil.getChildText(lot, "LOTNAME", true);
			String sCarrierName = SMessageUtil.getChildText(lot, "DURABLENAME", true);
			List<Element> productList = SMessageUtil.getSubSequenceItemList(lot, "PRODUCTLIST", true);
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			ReserveLot reserveLotData = ExtendedObjectProxy.getReserveLotService().select(" MACHINENAME = ? and LOTNAME = ? ", new Object[] { machineName, sLotName }).get(0);

			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(reserveLotData.getLotName());

			List<ProductPGS> productPGSSequence = this.setProductPGSSequence(factoryName, productList, (long) lotData.getSubProductUnitQuantity1(), lotData.getProductRequestName(), sLotName,
					machineName);

			// Validation
			checkcrate(factoryName, productSpecName, lotData.getProductSpecVersion(), productPGSSequence);

			MakeReleasedInfo releaseInfo = MESLotServiceProxy.getLotInfoUtil().makeReleasedInfo(lotData, machineData.getAreaName(), lotData.getNodeStack(), lotData.getProcessFlowName(),
					lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), lotData.getProductionType(), lotData.getUdfs(), "", lotData.getDueDate(),
					lotData.getPriority());
			
			releaseInfo.getUdfs().put("RELEASEQUANTITY", Integer.toString(productPGSSequence.size()));

			eventInfo.setEventName("Release");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			lotData = MESLotServiceProxy.getLotServiceImpl().releaseLot(eventInfo, lotData, releaseInfo, productPGSSequence);

			// Increase ReleaseQuantity
			incrementProductRequest(eventInfo, productRequestData, productPGSSequence);

			// Change ReserveLot State
			changeReserveLotState(eventInfo, reserveLotData);

			// Decrease Crate Qty
			decreaseCrateQuantity(eventInfo, lotData, productPGSSequence, machineName);

			// [V3_MES_121_004]DSP Run Control_V1.02
			//MESLotServiceProxy.getLotServiceUtil().increaseRunControlUseCount(eventInfo, machineName, lotData.getKey().getLotName(), true);
			
			// Track In Lot
			List<ProductC> productCSequence = MESLotServiceProxy.getLotInfoUtil().setProductCSequence(sLotName);
			Port plPortData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, plPort);
			PortSpec portSpecData = MESPortServiceProxy.getPortServiceUtil().getPortSpecInfo(machineName, plPort);

			Map<String, String> lotUdfs = new HashMap<String, String>();
			lotUdfs.put("PORTNAME", plPortData.getKey().getPortName());
			lotUdfs.put("PORTTYPE", portSpecData.getPortType().toString());
			lotUdfs.put("PORTUSETYPE", plPortData.getUdfs().get("PORTUSETYPE"));

			String recipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(),
					lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineName, false);

			MakeLoggedInInfo makeLoggedInInfo = MESLotServiceProxy.getLotInfoUtil().makeLoggedInInfo(machineName, recipeName, productCSequence, lotUdfs);

			eventInfo.setEventName("TrackIn");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			Lot trackInLot = MESLotServiceProxy.getLotServiceImpl().trackInLot(eventInfo, lotData, makeLoggedInInfo);

			// Track Out Lot
			List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(productList);
			if(StringUtils.equals(lotData.getProductionType(), "E")||StringUtils.equals(lotData.getProductionType(), "P")
					||StringUtils.equals(lotData.getProductionType(), "T"))
			{
				for(ProductPGSRC ProductPGSRCInfo:productPGSRCSequence)
				{
					ProductPGSRCInfo.getUdfs().put("LASTMAINFLOWNAME", lotData.getProcessFlowName());
					ProductPGSRCInfo.getUdfs().put("LASTMAINOPERNAME", lotData.getProcessOperationName());
				}
			}
			Port puPortData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, puPort);
			Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();
			Map<String, String> assignCarrierUdfs = new HashMap<String, String>();

			MESLotServiceProxy.getLotServiceUtil().setSamplingListData(eventInfo, trackInLot);

			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sCarrierName);
			
			// Validation
			CommonValidation.checkAvailableCst(durableData);
			CommonValidation.checkMultiLot(durableData.getKey().getDurableName());

			// TK OUT
			MESLotServiceProxy.getLotServiceUtil().trackOutLotWithSampling(eventInfo, trackInLot, puPortData, sCarrierName, lotData.getLotGrade(), machineName, "", productPGSRCSequence,
					assignCarrierUdfs, deassignCarrierUdfs, new HashMap<String, String>());
            //Send To SAP
			try
			{
				String sapFlag=CommonUtil.getEnumDefValueStringByEnumName("SAPFLAG");
				if(StringUtil.isNotEmpty(sapFlag)&&StringUtil.equals(sapFlag, "Y")&&
						StringUtils.isNotEmpty(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
				{
					SuperProductRequest superWO = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
					
					MESConsumableServiceProxy.getConsumableServiceUtil().TrackOutERPBOMReportByMaterialProduct(eventInfo, trackInLot, superWO, machineName,productList.size());
				}
			}
			catch(Exception x)
			{
				eventLog.info("SAP Report Error");
			}

			
			// [V3_MES_121_004]DSP Run Control_V1.02
			//MESLotServiceProxy.getLotServiceUtil().runControlResetCountAndSendMail(eventInfo, machineName, trackInLot, recipeName, null);
		}

		return doc;
	}

	private List<ProductPGS> setProductPGSSequence(String factoryName, List<Element> productList, long createSubProductQuantity, String productRequestName, String lotName, String machineName)
			throws CustomException
	{
		List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();

		int idx = 0;
		
		try
		{
			for (Element product : productList)
			{
				idx++;
				
				String sProductName = SMessageUtil.getChildText(product, "PRODUCTNAME", true);
				String sPosition = SMessageUtil.getChildText(product, "POSITION", true);
				String sCrateName = SMessageUtil.getChildText(product, "CRATENAME", true);

				ProductPP productInfo = new ProductPP();
				productInfo.setPosition(Long.parseLong(sPosition));
				productInfo.setProductGrade(GradeDefUtil.getGrade(GenericServiceProxy.getConstantMap().DEFAULT_FACTORY, GenericServiceProxy.getConstantMap().GradeType_Product, true).getGrade());
				productInfo.setProductName(sProductName);
				productInfo.setProductRequestName(productRequestName);
				productInfo.setSubProductGrades1("");
				productInfo.setSubProductQuantity1(createSubProductQuantity);

				productInfo.getUdfs().put("CRATENAME", sCrateName);
				productInfo.getUdfs().put("INITIALLOTNAME", lotName);
				productInfo.getUdfs().put("OLDPRODUCTREQUESTNAME", productRequestName);
				productInfo.getUdfs().put("MAINMACHINENAME", machineName);

				if (StringUtils.equals(factoryName, "ARRAY"))
					productInfo.getUdfs().put("ARRAYLOTNAME", lotName);

				productPGSSequence.add(productInfo);
			}
		}
		catch (Exception ex)
		{
			eventLog.info(String.format("[%d]th Product not set", idx));
		}

		return productPGSSequence;
	}

	private void changeReserveLotState(EventInfo eventInfo, ReserveLot reserveLotData) throws CustomException
	{
		reserveLotData.setReserveState(GenericServiceProxy.getConstantMap().RESV_STATE_END);
		reserveLotData.setInputTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		reserveLotData.setCompleteTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		try
		{
			ExtendedObjectProxy.getReserveLotService().modify(eventInfo, reserveLotData);
		}
		catch (greenFrameDBErrorSignal ne)
		{
			throw new CustomException("CRATE-8001", ne.getMessage());
		}
	}

	private void decreaseCrateQuantity(EventInfo eventInfo, Lot lotData, List<ProductPGS> productPGSSequence, String machineName) throws CustomException
	{
		Map<String, Object> crateMap = new HashMap<String, Object>();

		String oldCrateName = "";

		int count = 0;

		for (ProductPGS productPGS : productPGSSequence)
		{
			if (!oldCrateName.equals(CommonUtil.getValue(productPGS.getUdfs(), "CRATENAME")))
			{
				// initialize
				count = 0;
				oldCrateName = CommonUtil.getValue(productPGS.getUdfs(), "CRATENAME");
			}

			count++;
			crateMap.put(oldCrateName, count);

			eventInfo.setEventName("Release");
			insertMaterialProduct(eventInfo, productPGS, CommonUtil.getValue(productPGS.getUdfs(), "CRATENAME"), machineName);
		}

		for (String crateName : crateMap.keySet())
		{
			if (crateMap.get(crateName) != null)
			{
				int quantity = Integer.parseInt(crateMap.get(crateName).toString());

				eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
				decreaseCrateQuantity(eventInfo, lotData, crateName, quantity);

				// makeNotAvailable
				Consumable crateData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(crateName);
				if (crateData.getQuantity() == 0 && StringUtil.equals(crateData.getConsumableState(), "Available"))
				{
					eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), null, null);

					MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();
					makeNotAvailableInfo.setUdfs(crateData.getUdfs());
					MESConsumableServiceProxy.getConsumableServiceImpl().makeNotAvailable(crateData, makeNotAvailableInfo, eventInfo);
				}
			}
		}
	}

	private void decreaseCrateQuantity(EventInfo eventInfo, Lot lotData, String consumableName, double quantity) throws CustomException
	{
		eventInfo.setEventName("Consume");

		Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(consumableName);

		DecrementQuantityInfo transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().decrementQuantityInfo(lotData.getKey().getLotName(), lotData.getProcessOperationName(),
				lotData.getProcessOperationVersion(), "", TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()), quantity, lotData.getUdfs());

		MESConsumableServiceProxy.getConsumableServiceImpl().decrementQuantity(consumableData, transitionInfo, eventInfo);
	}

	private void incrementProductRequest(EventInfo eventInfo, ProductRequest productRequestData, List<ProductPGS> productPGSSequence) throws CustomException
	{
		int incrementQty = productPGSSequence.size();

		IncrementReleasedQuantityByInfo incrementReleasedQuantityByInfo = new IncrementReleasedQuantityByInfo();
		incrementReleasedQuantityByInfo.setQuantity(incrementQty);

		eventInfo.setEventName("IncreamentReleasedQuantity");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		ProductRequest newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementReleasedQuantityBy(productRequestData, incrementReleasedQuantityByInfo, eventInfo);

		if (productRequestData.getPlanQuantity() < Integer.parseInt(productRequestData.getUdfs().get("CREATEDQUANTITY")))
			throw new CustomException("PRODUCTREQUEST-0030");

		if (Integer.parseInt(productRequestData.getUdfs().get("CREATEDQUANTITY")) < newProductRequestData.getReleasedQuantity())
			throw new CustomException("PRODUCTREQUEST-0031");
	}

	private void checkcrate(String factoryName, String ProductSpecName, String productSpecVersion, List<ProductPGS> productPGSSequence) throws CustomException
	{
		List<String> crateList = new ArrayList<String>();

		for (ProductPGS productPGS : productPGSSequence)
		{
			String sCrateName = CommonUtil.getValue(productPGS.getUdfs(), "CRATENAME");

			if (!crateList.contains(sCrateName))
				crateList.add(sCrateName);
		}

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT PC.MATERIALSPECNAME ");
		sql.append("  FROM TPPOLICY T, POSBOM PC ");
		sql.append(" WHERE PC.CONDITIONID = T.CONDITIONID ");
		sql.append("   AND T.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND T.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("   AND T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("FACTORYNAME", factoryName);
		args.put("PRODUCTSPECNAME", ProductSpecName);
		args.put("PRODUCTSPECVERSION", productSpecVersion);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (sqlResult.size() == 0)
		{
			throw new CustomException("CRATE-0007");
		}
		else
		{
			List<String> materialSpecNameList = CommonUtil.makeListBySqlResult(sqlResult, "MATERIALSPECNAME");

			sql.setLength(0);
			sql.append("SELECT DISTINCT CONSUMABLESPECNAME FROM CONSUMABLE WHERE CONSUMABLENAME IN ( :CRATELIST ) ");

			args.clear();
			args.put("CRATELIST", crateList);

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> crateSpecResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (crateSpecResult.size() > 0)
			{
				List<String> crateSpecList = CommonUtil.makeListBySqlResult(crateSpecResult, "CONSUMABLESPECNAME");

				for (String crateSpecName : crateSpecList)
				{
					if (!materialSpecNameList.contains(crateSpecName))
						throw new CustomException("CRATE-0008");
				}
			}
		}
	}

	private void insertMaterialProduct(EventInfo eventInfo, ProductPGS productPGS, String consumableName, String machineName) throws CustomException
	{
		Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productPGS.getProductName());
		Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(consumableName);

		MaterialProduct dataInfo = new MaterialProduct();
		dataInfo.setTimeKey(eventInfo.getEventTimeKey());
		dataInfo.setProductName(productData.getKey().getProductName());
		dataInfo.setLotName(productData.getLotName());
		dataInfo.setMaterialKind(GenericServiceProxy.getConstantMap().MaterialKind_Consumable);
		dataInfo.setMaterialType(consumableData.getConsumableType());
		dataInfo.setMaterialName(consumableData.getKey().getConsumableName());
		dataInfo.setQuantity(1);
		dataInfo.setEventName(eventInfo.getEventName());
		dataInfo.setEventTime(eventInfo.getEventTime());
		dataInfo.setFactoryName(productData.getFactoryName());
		dataInfo.setProductSpecName(productData.getProductSpecName());
		dataInfo.setProductSpecVersion(productData.getProductSpecVersion());
		dataInfo.setProcessFlowName(productData.getProcessFlowName());
		dataInfo.setProcessFlowVersion(productData.getProcessFlowVersion());
		dataInfo.setProcessOperationName(productData.getProcessOperationName());

		dataInfo.setProcessOperationVersion(productData.getProcessOperationVersion());
		dataInfo.setMachineName(machineName);
		dataInfo.setMaterialLocationName("");

		ExtendedObjectProxy.getMaterialProductService().create(eventInfo, dataInfo);
	}
}
