package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.CreateInfo;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class CreateLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		String cycleno = SMessageUtil.getBodyItemValue(doc, "CYCLENO", true);
		String PRIORITY = SMessageUtil.getBodyItemValue(doc, "PRIORITY", true);
		String lotNote = SMessageUtil.getBodyItemValue(doc, "LOTNOTE", false);
		long nPRIORITY = Long.parseLong(PRIORITY);

		List<Element> LotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
		List<Element> eleLotList = new ArrayList<Element>();

		ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(factoryName, productSpecName, productSpecVersion);
		ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());

		String namingRule = "";

		if (StringUtils.equals(productSpecData.getProductionType(), "P"))
			namingRule = "ProductionLotNaming";
		else
			namingRule = "LotNaming";

		for (Element Lot : LotList)
		{
			String productQuantity = SMessageUtil.getChildText(Lot, "PRODUCTQUANTITY", true);
			String dueDate = SMessageUtil.getChildText(Lot, "DUEDATE", true);
			String experimenter = SMessageUtil.getChildText(Lot, "EXPERIMENTER", false);

			// convert
			Timestamp tDueDate = TimeUtils.getTimestamp(dueDate);

			double sProcessSubQuantity1 = productSpecData.getSubProductUnitQuantity1();
			double sPlanSubProductQuantity = Double.parseDouble(productQuantity) * sProcessSubQuantity1;

			if (StringUtils.isEmpty(productSpecData.getUdfs().get("PRODUCTCODE")))
				throw new CustomException("NAMING-0002", productSpecData.getKey().getProductSpecName());

			Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
			nameRuleAttrMap.put("PRODUCTCODE", productSpecData.getUdfs().get("PRODUCTCODE"));
			nameRuleAttrMap.put("PRODUCTIONTYPE", productSpecData.getProductionType());
			nameRuleAttrMap.put("PRODUCTSPECTYPE", productSpecData.getUdfs().get("PRODUCTSPECTYPE"));

			List<String> lotNameList = CommonUtil.generateNameByNamingRule(namingRule, nameRuleAttrMap, 1);

			if (lotNameList.size() < 1)
				throw new CustomException("SYS-0110");

			String newLotName = lotNameList.get(0);
			Map<String, String> productRequestUdfs = productRequestData.getUdfs();

			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("PLANPRODUCTQUANTITY", productQuantity);
			udfs.put("PLANSUBPRODUCTQUANTITY", String.valueOf((int) sPlanSubProductQuantity));
			udfs.put("AUTOSHIPPINGFLAG", productRequestUdfs.get("AUTOSHIPPINGFLAG"));
			udfs.put("CYCLENO", cycleno);
			udfs.put("EXPERIMENTER", experimenter);
			udfs.put("LOTNOTE", lotNote);

			if (StringUtils.equals(factoryName, "ARRAY"))
				udfs.put("ARRAYLOTNAME", newLotName);

			CreateInfo createInfo = MESLotServiceProxy.getLotInfoUtil().newCreateInfo(tDueDate, factoryName, newLotName, "", nPRIORITY, processFlowName, processFlowVersion, "", "", "",
					productSpecData.getProductionType(), ConvertUtil.toDecimal(productSpecData.getProductQuantity(), productQuantity), "", "", "", productSpecData.getKey().getProductSpecName(),
					productSpecData.getKey().getProductSpecVersion(), productSpecData.getProductType(), productSpecData.getSubProductType(), productRequestName,
					productSpecData.getSubProductUnitQuantity1(), 0, udfs);

			eventInfo.setEventName("Create");
			Lot newLot = MESLotServiceProxy.getLotServiceImpl().createLot(eventInfo, createInfo);

			ExtendedObjectProxy.getReserveLotService().createReserveLot(eventInfo, newLotName, "-", "-", productSpecName, productSpecVersion, "", factoryName, productRequestName);

			eventInfo.setEventName("IncreaseCreatedQuantity");
			productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementCreatedQuantityBy(productRequestData, Integer.parseInt(productQuantity), eventInfo);

			if (productRequestData.getPlanQuantity() < Integer.parseInt(productRequestData.getUdfs().get("CREATEDQUANTITY")))
			{
				throw new CustomException("PRODUCTREQUEST-0030");
			}

			eleLotList.add(setCreatedLotList(newLot));
		}

		// call by value so that reply would be modified
		XmlUtil.setSubChildren(SMessageUtil.getBodyElement(doc), "NEWLOTLIST", eleLotList);

		return doc;
	}

	private Element setCreatedLotList(Lot lotData)
	{
		Element eleLot = new Element("LOT");

		try
		{

			XmlUtil.addElement(eleLot, "FACTORYNAME", lotData.getFactoryName());
			XmlUtil.addElement(eleLot, "LOTNAME", lotData.getKey().getLotName());
			XmlUtil.addElement(eleLot, "PRIORITY", Long.toString(lotData.getPriority()));
			XmlUtil.addElement(eleLot, "CYCLENO", lotData.getUdfs().get("CYCLENO"));
			XmlUtil.addElement(eleLot, "PRODUCTSPECNAME", lotData.getProductSpecName());
			XmlUtil.addElement(eleLot, "PRODUCTSPECVERSION", lotData.getProductSpecVersion());
			XmlUtil.addElement(eleLot, "PRODUCTIONTYPE", lotData.getProductionType());
			XmlUtil.addElement(eleLot, "PRODUCTQUANTITY", String.valueOf((long) lotData.getCreateProductQuantity()));
			XmlUtil.addElement(eleLot, "DUEDATE", TimeStampUtil.toTimeString(lotData.getDueDate()));
		}
		catch (Exception ex)
		{
			eventLog.warn(String.format("Scribing Lot[%s] is failed so that skip", lotData.getKey().getLotName()));
		}

		return eleLot;
	}

}
