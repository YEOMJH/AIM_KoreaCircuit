package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.GradeDefUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.service.ProductPP;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.CreateInfo;
import kr.co.aim.greentrack.lot.management.info.MakeReleasedInfo;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.IncrementReleasedQuantityByInfo;

public class CreateLotForPostCell extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		
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
          
			double sProcessSubQuantity1 = 95;
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
					productSpecData.getKey().getProductSpecVersion(), "Glass", productSpecData.getSubProductType(), sProductRequestName, sProcessSubQuantity1, 0, udfs);

			// Create Lot
			Lot lotData = MESLotServiceProxy.getLotServiceImpl().createLot(eventInfo, createInfo);

			List<Element> eleProductList = SMessageUtil.getSubSequenceItemList(Lot, "PRODUCTLIST", true);

			List<ProductPGS> productPGSSequence = 
					this.setProductPGSSequence(sFactoryName, eleProductList, (long) lotData.getSubProductUnitQuantity1(), lotData.getProductRequestName(), lotData.getKey().getLotName());

			MakeReleasedInfo releaseInfo = MESLotServiceProxy.getLotInfoUtil().makeReleasedInfo(lotData, lotData.getAreaName(), lotData.getNodeStack(), lotData.getProcessFlowName(),
					lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), lotData.getProductionType(), new HashMap<String, String>(), sDurableName,
					lotData.getDueDate(), lotData.getPriority());
			
			releaseInfo.getUdfs().put("RELEASEQUANTITY", Integer.toString(productPGSSequence.size()));
			eventInfo.setEventName("Release");
			lotData = MESLotServiceProxy.getLotServiceImpl().releaseLot(eventInfo, lotData, releaseInfo, productPGSSequence);

			// Return LotList
			eleLotList.add(setCreatedLotList(lotData));

			this.incrementProductRequest(eventInfo, workOrderData, productPGSSequence, sProductRequestName);
		}

		XmlUtil.setSubChildren(SMessageUtil.getBodyElement(doc), "NEWLOTLIST", eleLotList);
		return doc;
	}
	private List<ProductPGS> setProductPGSSequence(String factoryName, List<Element> productList, long createSubProductQuantity, String productRequestName, String lotName) throws CustomException
	{
		List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();

		for (Element product : productList)
		{
			String sProductName = SMessageUtil.getChildText(product, "PRODUCTNAME", true);
			String sPosition = SMessageUtil.getChildText(product, "POSITION", true);
			ProductPP productInfo = new ProductPP();
			productInfo.setPosition(Long.parseLong(sPosition));
			productInfo.setProductGrade("G");
			productInfo.setProductName(sProductName);
			productInfo.setProductRequestName(productRequestName);
			productInfo.setSubProductGrades1("GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG");
			productInfo.setSubProductQuantity1(createSubProductQuantity);

			productInfo.getUdfs().put("INITIALLOTNAME", lotName);
			productInfo.getUdfs().put("OLDPRODUCTREQUESTNAME", productRequestName);

			productPGSSequence.add(productInfo);
		}
		return productPGSSequence;
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
	private void incrementProductRequest(EventInfo eventInfo, ProductRequest productRequestData, List<ProductPGS> productPGSSequence, String productRequestName) throws CustomException
	{
		int incrementQty = productPGSSequence.size()*95;
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



}
