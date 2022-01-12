package kr.co.aim.messolution.durable.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.SplitInfo;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.jdom.Document;
import org.jdom.Element;

public class SendtoBufferCST extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String lotNameSourceA = SMessageUtil.getBodyItemValue(doc, "SOURCELOTNAME", true);
		String durableNameDestination = SMessageUtil.getBodyItemValue(doc, "DESTDURABLENAME", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		Lot lotDataSourceA = MESLotServiceProxy.getLotInfoUtil().getLotData(lotNameSourceA);

		Durable destDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableNameDestination);

		ProductSpec baseData = GenericServiceProxy.getSpecUtil().getProductSpec(factoryName, lotDataSourceA.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);

		// validation
		CommonValidation.CheckDurableHoldState(destDurableData);
		CommonValidation.CheckDurableCleanState(destDurableData);

		CommonValidation.checkLotState(lotDataSourceA);
		CommonValidation.checkLotProcessState(lotDataSourceA);
		CommonValidation.checkLotHoldState(lotDataSourceA);

		if (destDurableData.getLotQuantity() != 0)
		{
			List<Lot> destLotList = MESLotServiceProxy.getLotInfoUtil().getLotListBydurableName(durableNameDestination);

			// Validation Lot Check(ProductSpec, BeforeflowName, BeforeOperationName)
			for (Lot destLot : destLotList)
			{
				if (lotDataSourceA.getProductSpecName().equals(destLot.getProductSpecName()) && lotDataSourceA.getUdfs().get("RETURNFLOWNAME").equals(destLot.getUdfs().get("RETURNFLOWNAME"))
						&& lotDataSourceA.getUdfs().get("RETURNOPERATIONNAME").equals(destLot.getUdfs().get("RETURNOPERATIONNAME")))
				{
					throw new CustomException("LOT-9012");
				}

			}
		}

		if (!durableNameDestination.isEmpty())
		{
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableNameDestination);
			CommonValidation.CheckDurableHoldState(durableData);
		}

		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("LOTNAME", lotNameSourceA);
		nameRuleAttrMap.put("PRODUCTIONTYPE", baseData.getProductionType());
		nameRuleAttrMap.put("PRODUCTSPECTYPE", baseData.getUdfs().get("PRODUCTSPECTYPE"));

		eventInfo.setEventName("Split");
		String splitLotName = "";

		try
		{
			List<String> lstName = CommonUtil.generateNameByNamingRule("SplitLotNaming", nameRuleAttrMap, 1);
			splitLotName = lstName.get(0);
		}
		catch (Exception ex)
		{
			new CustomException("LOT-9011", ex.getMessage());
		}

		Element eleBody = SMessageUtil.getBodyElement(doc);
		List<Element> eleSourceDurableList = SMessageUtil.getSubSequenceItemList(eleBody, "SOURCE_DURABLELIST", true);

		List<Element> eleDestiProductList = null;
		try
		{
			eleDestiProductList = SMessageUtil.getSubSequenceItemList(eleBody, "DESTINATIONPRODUCTLIST", true);

		}
		catch (Exception e)
		{
		}

		List<Element> eleTargetProductList = null;
		try
		{
			eleTargetProductList = SMessageUtil.getSubSequenceItemList(eleBody, "TARGETPRODUCTLIST", true);

		}
		catch (Exception e)
		{
		}

		List<ProductP> DestproductTSequence = new ArrayList<ProductP>();

		for (Element eleSourceDurable : eleSourceDurableList)
		{
			String durableName = SMessageUtil.getChildText(eleSourceDurable, "DURABLENAME", true);

			Durable sourceDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);

			CommonValidation.CheckDurableHoldState(sourceDurableData);

			List<Element> eleProductList = SMessageUtil.getSubSequenceItemList(eleSourceDurable, "PRODUCTLIST", true);

			List<Element> eleSrcProductList = null;
			try
			{
				eleSrcProductList = SMessageUtil.getSubSequenceItemList(eleBody, durableName, false);
			}
			catch (Exception e)
			{

			}

			List<ProductP> productTSequence = new ArrayList<ProductP>();

			for (Element eleProduct : eleTargetProductList)
			{
				String productName = SMessageUtil.getChildText(eleProduct, "PRODUCTNAME", true);
				String slotPosition = SMessageUtil.getChildText(eleProduct, "SLOTPOSITION", false);
				int position = Integer.parseInt(SMessageUtil.getChildText(eleProduct, "POSITION", true));
				Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);

				ProductP productP = new ProductP();
				productP.setProductName(productData.getKey().getProductName());
				productP.setPosition(position);
				productP.getUdfs().put("SLOTPOSITION", slotPosition);
				productTSequence.add(productP);
			}

			DestproductTSequence = productTSequence;

			Lot sourceLotData = null;
			try
			{
				sourceLotData = MESLotServiceProxy.getLotInfoUtil().getLotInfoBydurableName(durableName);
			}
			catch (CustomException ce)
			{
			}

			if (eleSrcProductList != null)
			{
				String sourLotGrade = CommonUtil.judgeLotGradeByProductList(eleSrcProductList, "PRODUCTJUDGE");

				MESLotServiceProxy.getLotServiceImpl().updateLotData("LOTGRADE", sourLotGrade, sourceLotData.getKey().getLotName());
			}

			if (sourceLotData.getProductQuantity() == 0)
			{
				Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();
				if (StringUtil.isNotEmpty(sourceLotData.getCarrierName()))
				{
					Durable sLotDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sourceLotData.getCarrierName());
					deassignCarrierUdfs = sLotDurableData.getUdfs();
					DeassignCarrierInfo deassignCarrierInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(sourceLotData, sLotDurableData, new ArrayList<ProductU>());
					// Deassign Carrier
					eventInfo.setEventName("DeassignCarrier");
					MESLotServiceProxy.getLotServiceImpl().deassignCarrier(sourceLotData, deassignCarrierInfo, eventInfo);
				}

				eventInfo.setEventName("MakeEmptied");
				MESLotServiceProxy.getLotServiceImpl().MakeEmptied(eventInfo, sourceLotData, new ArrayList<ProductU>(), deassignCarrierUdfs);
				deassignCarrierUdfs.clear();
			}
		}

		SplitInfo splitInfo = MESLotServiceProxy.getLotInfoUtil().splitInfo(lotDataSourceA, durableNameDestination, splitLotName, DestproductTSequence, String.valueOf(eleTargetProductList.size()));

		Lot DestLot = MESLotServiceProxy.getLotServiceImpl().splitLot(eventInfo, lotDataSourceA, splitInfo);

		XmlUtil.setChildText(SMessageUtil.getBodyElement(doc), "DESTLOTNAME", DestLot.getKey().getLotName());
		XmlUtil.setChildText(SMessageUtil.getBodyElement(doc), "DESTLOTHOLDSTATE", DestLot.getLotHoldState());
		XmlUtil.setChildText(SMessageUtil.getBodyElement(doc), "DESTLOTSTATE", DestLot.getLotState());
		XmlUtil.setChildText(SMessageUtil.getBodyElement(doc), "DESTLOTGRADE", DestLot.getLotGrade());
		XmlUtil.setChildText(SMessageUtil.getBodyElement(doc), "DESTPRODUCTQTY", String.valueOf((int) DestLot.getProductQuantity()));
		XmlUtil.setChildText(SMessageUtil.getBodyElement(doc), "DESTPRODUCTSPEC", DestLot.getProductSpecName());

		return doc;
	}

}