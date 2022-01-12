package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaterialProduct;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.GradeDefUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.product.service.ProductPP;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.CreateInfo;
import kr.co.aim.greentrack.lot.management.info.MakeReleasedInfo;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.IncrementReleasedQuantityByInfo;

public class CreateLotForTP extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String sFactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String sProductSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String sProductSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String sProcessFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String sProcessFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String sProductRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);

		List<Element> eleLotList = new ArrayList<Element>();
		List<Element> LotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
		ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(sFactoryName, sProductSpecName, sProductSpecVersion);
		ProductRequest workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(sProductRequestName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		String namingRule = "";

		if (StringUtils.equals(productSpecData.getProductionType(), "P"))
			namingRule = "ProductionLotNaming";
		else
			namingRule = "LotNaming";

		for (Element Lot : LotList)
		{
			String sProductQty = SMessageUtil.getChildText(Lot, "PRODUCTQUANTITY", true);
			String sDurableName = SMessageUtil.getChildText(Lot, "DURABLENAME", true);
			String sDueDate = SMessageUtil.getChildText(Lot, "DUEDATE", true);

			Timestamp tDueDate = TimeUtils.getTimestamp(sDueDate);

			double sProcessSubQuantity1 = productSpecData.getSubProductUnitQuantity1() / 2;
			double sPlanSubProductQuantity = Double.parseDouble(sProductQty) * sProcessSubQuantity1;

			if (StringUtils.isEmpty(productSpecData.getUdfs().get("PRODUCTCODE")))
				throw new CustomException("NAMING-0002", productSpecData.getKey().getProductSpecName());

			Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
			nameRuleAttrMap.put("PRODUCTCODE", productSpecData.getUdfs().get("PRODUCTCODE"));
			nameRuleAttrMap.put("PRODUCTIONTYPE", productSpecData.getProductionType());
			nameRuleAttrMap.put("PRODUCTSPECTYPE", productSpecData.getUdfs().get("PRODUCTSPECTYPE"));

			List<String> lotNameList = CommonUtil.generateNameByNamingRule(namingRule, nameRuleAttrMap, 1);

			if (lotNameList.size() < 1)
			{
				throw new CustomException("SYS-0110");
			}

			String newLotName = lotNameList.get(0);
			Map<String, String> workOrderudfs = workOrderData.getUdfs();

			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("PLANPRODUCTQUANTITY", sProductQty);
			udfs.put("PLANSUBPRODUCTQUANTITY", String.valueOf((int) sPlanSubProductQuantity));
			udfs.put("AUTOSHIPPINGFLAG", workOrderudfs.get("AUTOSHIPPINGFLAG"));

			CreateInfo createInfo = MESLotServiceProxy.getLotInfoUtil().newCreateInfo(tDueDate, sFactoryName, newLotName, "", 4, sProcessFlowName, sProcessFlowVersion, "", "", "",
					productSpecData.getProductionType(), ConvertUtil.toDecimal(productSpecData.getProductQuantity(), sProductQty), "", "", "", productSpecData.getKey().getProductSpecName(),
					productSpecData.getKey().getProductSpecVersion(), productSpecData.getProductType(), productSpecData.getSubProductType(), sProductRequestName, sProcessSubQuantity1, 0, udfs);

			// Create Lot
			Lot lotData = MESLotServiceProxy.getLotServiceImpl().createLot(eventInfo, createInfo);

			List<Element> eleProductList = SMessageUtil.getSubSequenceItemList(Lot, "PRODUCTLIST", true);
			List<Element> productList = setProductList(lotData.getKey().getLotName(), eleProductList, sProductQty);

			List<ProductPGS> productPGSSequence = 
					this.setProductPGSSequence(sFactoryName, productList, (long) lotData.getSubProductUnitQuantity1(), lotData.getProductRequestName(), lotData.getKey().getLotName());

			MakeReleasedInfo releaseInfo = MESLotServiceProxy.getLotInfoUtil().makeReleasedInfo(lotData, lotData.getAreaName(), lotData.getNodeStack(), lotData.getProcessFlowName(),
					lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), lotData.getProductionType(), new HashMap<String, String>(), sDurableName,
					lotData.getDueDate(), lotData.getPriority());
			
			releaseInfo.getUdfs().put("RELEASEQUANTITY", Integer.toString(productPGSSequence.size()));
			eventInfo.setEventName("Release");
			lotData = MESLotServiceProxy.getLotServiceImpl().releaseLot(eventInfo, lotData, releaseInfo, productPGSSequence);

			// Return LotList
			eleLotList.add(setCreatedLotList(lotData));

			this.incrementProductRequest(eventInfo, workOrderData, productPGSSequence, sProductRequestName);
			this.decreaseCrateQuantity(eventInfo, lotData, productPGSSequence);
		}

		XmlUtil.setSubChildren(SMessageUtil.getBodyElement(doc), "NEWLOTLIST", eleLotList);

		return doc;
	}

	private List<Element> setProductList(String lotName, List<Element> eleProductList, String sProductQty) throws CustomException
	{
		int prodQty = Integer.parseInt(sProductQty);
		String prodHead = lotName.substring(0, 9);
		int count = prodQty;

		for (Element product : eleProductList)
		{
			String productName = "";

			if (count < 10)
			{
				productName = prodHead + "0" + count;
			}
			else
			{
				productName = prodHead + count;
			}

			count -= 1;

			product.getChild("PRODUCTNAME").setText(productName);
		}

		return eleProductList;
	}

	private List<ProductPGS> setProductPGSSequence(String factoryName, List<Element> productList, long createSubProductQuantity, String productRequestName, String lotName) throws CustomException
	{
		List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();

		for (Element product : productList)
		{
			String sProductName = SMessageUtil.getChildText(product, "PRODUCTNAME", true);
			String sPosition = SMessageUtil.getChildText(product, "POSITION", true);
			String sSlotPosition = SMessageUtil.getChildText(product, "SLOTPOSITION", true);
			String sCrateName = SMessageUtil.getChildText(product, "CRATENAME", true);

			ProductPP productInfo = new ProductPP();
			productInfo.setPosition(Long.parseLong(sPosition));
			productInfo.setProductGrade(GradeDefUtil.getGrade("TP", GenericServiceProxy.getConstantMap().GradeType_Product, true).getGrade());
			productInfo.setProductName(sProductName);
			productInfo.setProductRequestName(productRequestName);
			productInfo.setSubProductGrades1("");
			productInfo.setSubProductQuantity1(createSubProductQuantity);

			productInfo.getUdfs().put("CRATENAME", sCrateName);
			productInfo.getUdfs().put("SLOTPOSITION", sSlotPosition);
			productInfo.getUdfs().put("INITIALLOTNAME", lotName);
			productInfo.getUdfs().put("OLDPRODUCTREQUESTNAME", productRequestName);

			productPGSSequence.add(productInfo);
		}
		return productPGSSequence;
	}

	private void incrementProductRequest(EventInfo eventInfo, ProductRequest productRequestData, List<ProductPGS> productPGSSequence, String productRequestName) throws CustomException
	{
		int incrementQty = productPGSSequence.size();
		int createQuantity = Integer.parseInt(productRequestData.getUdfs().get("CREATEDQUANTITY"));

		IncrementReleasedQuantityByInfo incrementReleasedQuantityByInfo = new IncrementReleasedQuantityByInfo();
		incrementReleasedQuantityByInfo.setQuantity(incrementQty);

		Map<String, String> productRequestUdfs = productRequestData.getUdfs();
		productRequestUdfs.put("CREATEDQUANTITY", Integer.toString(createQuantity + incrementQty));
		incrementReleasedQuantityByInfo.setUdfs(productRequestUdfs);

		eventInfo.setEventName("IncreamentQuantity");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementReleasedQuantityBy(productRequestData, incrementReleasedQuantityByInfo, eventInfo);
	}

	private Element setCreatedLotList(Lot lotData)
	{
		Element eleLot = new Element("LOT");

		try
		{
			XmlUtil.addElement(eleLot, "LOTNAME", lotData.getKey().getLotName());
			XmlUtil.addElement(eleLot, "DURABLENAME", lotData.getCarrierName());
			XmlUtil.addElement(eleLot, "PRODUCTSPECNAME", lotData.getProductSpecName());
			XmlUtil.addElement(eleLot, "PRODUCTSPECVERSION", lotData.getProductSpecVersion());
			XmlUtil.addElement(eleLot, "PROCESSFLOWNAME", lotData.getProcessFlowName());
			XmlUtil.addElement(eleLot, "PROCESSFLOWVERSION", lotData.getProcessFlowVersion());
			XmlUtil.addElement(eleLot, "PRODUCTIONTYPE", lotData.getProductionType());
			XmlUtil.addElement(eleLot, "PRODUCTQUANTITY", Integer.toString((int) lotData.getProductQuantity()));
			XmlUtil.addElement(eleLot, "DUEDATE", lotData.getDueDate().toString());
		}
		catch (Exception ex)
		{
			eventLog.warn(String.format("Scribing Lot[%s] is failed so that skip", lotData.getKey().getLotName()));
		}

		return eleLot;
	}

	private void decreaseCrateQuantity(EventInfo eventInfo, Lot lotData, List<ProductPGS> productPGSSequence) throws CustomException
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
			insertMaterialProduct(eventInfo, productPGS, CommonUtil.getValue(productPGS.getUdfs(), "CRATENAME"));
		}

		for (String crateName : crateMap.keySet())
		{
			if (crateMap.get(crateName) != null)
			{
				int quantity = Integer.parseInt(crateMap.get(crateName).toString());

				eventInfo = EventInfoUtil.makeEventInfo("AdjustQty", getEventUser(), getEventComment(), null, null);
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

	private void insertMaterialProduct(EventInfo eventInfo, ProductPGS productPGS, String consumableName) throws CustomException
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
		dataInfo.setMachineName("");
		dataInfo.setMaterialLocationName("");

		ExtendedObjectProxy.getMaterialProductService().create(eventInfo, dataInfo);
	}
}
