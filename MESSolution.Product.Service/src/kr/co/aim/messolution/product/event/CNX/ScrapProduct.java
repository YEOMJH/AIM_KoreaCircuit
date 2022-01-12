package kr.co.aim.messolution.product.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ProductQueueTime;
import kr.co.aim.messolution.extended.object.management.impl.LotQueueTimeService;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.MakeScrappedInfo;
import kr.co.aim.greentrack.lot.management.info.SplitInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class ScrapProduct extends SyncHandler {

	public static Log logger = LogFactory.getLog(LotQueueTimeService.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment(), "", "");

		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String sReasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", true);
		String sReasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", true);
		String sOperationName = SMessageUtil.getBodyItemValue(doc, "STEPNO", false);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String responseFactory=SMessageUtil.getBodyItemValue(doc, "RESPONSEFACTORY", false);
		String sDeparment=SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", true);
		String sUnitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String sSubUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		List<String> productNameList = CommonUtil.makeList(SMessageUtil.getBodyElement(doc), "PRODUCTLIST", "PRODUCTNAME");
		String manualScrap = "";

		if (StringUtil.isNotEmpty(sOperationName))
		{
			if (StringUtil.isNotEmpty(sSubUnitName))
				manualScrap = sOperationName + "-" + sSubUnitName;
			else if (StringUtil.isNotEmpty(sUnitName))
				manualScrap = sOperationName + "-" + sUnitName;
			else if (StringUtil.isNotEmpty(sMachineName))
				manualScrap = sOperationName + "-" + sMachineName;
			else
				manualScrap = sOperationName;
		}

		String newLotName = "";

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		CommonValidation.checkLotProcessState(lotData);
		CommonValidation.checkJobDownFlag(lotData);
		CommonValidation.checkLotHoldState(lotData);
		
		Element e = productList.get(0);
		String productName = e.getChild("PRODUCTNAME").getText();
		Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);

		String sLotProcessState = lotData.getLotProcessState();
		String sProductProcessState = productData.getProductProcessState();
		List<Object[]> updateScrapProduct= new ArrayList<Object[]>();
		List<ProductP> productScrap = new ArrayList<ProductP>();
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		if (productList.size() == lotData.getProductQuantity())
		{
			List<ProductU> productUSequence = new ArrayList<ProductU>();
			productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllUnScrapProductUSequence(lotData);

			// 1.SourceLot Deassign With SourceCST
			lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			MESLotServiceProxy.getLotServiceImpl().updateLotData("LOTGRADE", "S", lotName);
			if (StringUtil.isNotEmpty(lotData.getCarrierName()))
			{
				Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());

				DeassignCarrierInfo createInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, durableData, productUSequence);

				eventInfo = EventInfoUtil.makeEventInfo("Deassign", getEventUser(), getEventComment(), "", "");
				MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, createInfo, eventInfo);
			}

			ProductSpec baseData = GenericServiceProxy.getSpecUtil()
					.getProductSpec(lotData.getFactoryName(), lotData.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);

			if (CommonUtil.equalsIn(baseData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare"))
			{
				eventLog.info(" Scraped Lot [" + lotData + "]  is sent to the Bank for MQC Job.");
				// EventInfo eventInfo , MQCPlan planData, Lot lotData
				MESLotServiceProxy.getLotServiceUtil().changeReturnMQCBank(eventInfo, lotData);

				// After being sent to the MQC bank, Delete MQC Job
				eventLog.info("After being sent to the MQC bank, Delete MQC Job. Scraped Lot : [" + lotData + "]");
				MESLotServiceProxy.getLotServiceUtil().removeMQCJob(eventInfo, lotData);
			}

			// 2.ScrapProduct
			for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false))
			{
				String sProductName = SMessageUtil.getChildText(eledur, "PRODUCTNAME", false);

				productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(sProductName);
				productData.setProductGrade("S");
				productData.getUdfs().put("MANUALSCRAPOPERMACHINE", manualScrap);
				ProductServiceProxy.getProductService().update(productData);
				
				List<Object> scrapProduct = new ArrayList<Object>();
				scrapProduct.add(sProductName);
				scrapProduct.add(lotName);
				scrapProduct.add(lotData.getProductionType());
				scrapProduct.add("S");
				scrapProduct.add(constantMap.Lot_Scrapped);
				scrapProduct.add(eventInfo.getEventTimeKey());
				scrapProduct.add(eventInfo.getEventTime());
				scrapProduct.add(eventInfo.getEventUser());
				scrapProduct.add(sReasonCodeType);
				scrapProduct.add(sReasonCode);
				scrapProduct.add(sDeparment);
				scrapProduct.add(sOperationName);
				scrapProduct.add(sMachineName);
				scrapProduct.add(sUnitName);
				scrapProduct.add(sSubUnitName);
				scrapProduct.add(eventInfo.getEventComment());
				updateScrapProduct.add(scrapProduct.toArray());
				
				ProductP productP = new ProductP();
				productP.setProductName(sProductName);
				productScrap.add(productP);

			}
            
			eventInfo = EventInfoUtil.makeEventInfo("Scrap", this.getEventUser(), this.getEventComment(), "", "");//caixu 2020/06/01 ScrapProduct Modify Scrap
			lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllUnScrapProductUSequence(lotData);

			if (lotData.getFactoryName().equals("ARRAY") || lotData.getFactoryName().equals("OLED") || lotData.getFactoryName().equals("TP"))
			{
				String reasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", true);
				eventInfo.setReasonCode(sReasonCode);
				eventInfo.setReasonCodeType(responseFactory+"-"+SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", true) + " - " + reasonCodeType);
			}
			else
			{
				eventInfo.setReasonCode(sReasonCode);
			}

			MakeScrappedInfo makeLotScrappedInfo = MESLotServiceProxy.getLotInfoUtil().makeScrappedInfo(lotData, lotData.getProductQuantity(), productUSequence);
			LotServiceProxy.getLotService().makeScrapped(lotData.getKey(), eventInfo, makeLotScrappedInfo);
			MESLotServiceProxy.getLotServiceUtil().InsertScrapProduct(updateScrapProduct);
			deleteScrapProduct(productScrap);
			// Keep SourceLotID
			newLotName = lotName;

		}
		else
		{ // 1.Split
			eventInfo = EventInfoUtil.makeEventInfo("Split", getEventUser(), getEventComment(), "", "");

			ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(lotData.getFactoryName(), lotData.getProductSpecName(),
					GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);

			List<ProductP> productPSequence = MESLotServiceProxy.getLotInfoUtil().setProductPSequence(productList, lotName);

			Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
			nameRuleAttrMap.put("LOTNAME", lotName);
			try
			{
				List<String> lstName = CommonUtil.generateNameByNamingRule("SplitLotNaming", nameRuleAttrMap, 1);
				newLotName = lstName.get(0);
			}
			catch (Exception ex)
			{
				new CustomException("LOT-9011", ex.getMessage());
			}

			List<Element> remainProductList = SMessageUtil.getBodySequenceItemList(doc, "REMAINPRODUCTLIST", true);

			String destLotGrade = "S";

			String sourLotGrade = CommonUtil.judgeLotGradeByProductList(remainProductList, "PRODUCTGRADE");
			MESLotServiceProxy.getLotServiceImpl().updateLotData("LOTGRADE", sourLotGrade, lotName);

			SplitInfo splitInfo = MESLotServiceProxy.getLotInfoUtil().splitLotInfo(lotData, newLotName, productPSequence, String.valueOf(productList.size()));
			MESLotServiceProxy.getLotServiceImpl().splitLot(eventInfo, lotData, splitInfo);
			MESLotServiceProxy.getLotServiceImpl().updateLotData("LOTGRADE", destLotGrade, newLotName);

			lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);

			// 2.ScrapProduct

			for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false))
			{
				String sProductName = SMessageUtil.getChildText(eledur, "PRODUCTNAME", false);

				productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(sProductName);
				productData.setProductGrade("S");
				productData.getUdfs().put("MANUALSCRAPOPERMACHINE", manualScrap);
				ProductServiceProxy.getProductService().update(productData);
				List<Object> scrapProduct = new ArrayList<Object>();
				
				scrapProduct.add(sProductName);
				scrapProduct.add(newLotName);
				scrapProduct.add(lotData.getProductionType());
				scrapProduct.add("S");
				scrapProduct.add(constantMap.Lot_Scrapped);
				scrapProduct.add(eventInfo.getEventTimeKey());
				scrapProduct.add(eventInfo.getEventTime());
				scrapProduct.add(eventInfo.getEventUser());
				scrapProduct.add(sReasonCodeType);
				scrapProduct.add(sReasonCode);
				scrapProduct.add(sDeparment);
				scrapProduct.add(sOperationName);
				scrapProduct.add(sMachineName);
				scrapProduct.add(sUnitName);
				scrapProduct.add(sSubUnitName);
				scrapProduct.add(eventInfo.getEventComment());
				updateScrapProduct.add(scrapProduct.toArray());
				
				ProductP productP = new ProductP();
				productP.setProductName(sProductName);
				productScrap.add(productP);

			}

			Lot newLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(newLotName);
			LotKey lotKey = newLotData.getKey();

			List<ProductU> productUSequence = new ArrayList<ProductU>();
			productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(newLotData);

			eventInfo = EventInfoUtil.makeEventInfo("Scrap", this.getEventUser(), this.getEventComment(), "", "");//caixu 2020/06/01 ScrapProduct Modify Scrap

			if (lotData.getFactoryName().equals("ARRAY") || lotData.getFactoryName().equals("OLED") || lotData.getFactoryName().equals("TP"))
			{
				String reasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", true);
				eventInfo.setReasonCode(sReasonCode);
				eventInfo.setReasonCodeType(responseFactory+"-"+SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", true) + " - " + reasonCodeType);
			}
			else
			{
				eventInfo.setReasonCode(sReasonCode);
			}

			MakeScrappedInfo makeLotScrappedInfo = MESLotServiceProxy.getLotInfoUtil().makeScrappedInfo(newLotData, newLotData.getProductQuantity(), productUSequence);
			LotServiceProxy.getLotService().makeScrapped(lotKey, eventInfo, makeLotScrappedInfo);
			MESLotServiceProxy.getLotServiceUtil().InsertScrapProduct(updateScrapProduct);
			deleteScrapProduct(productScrap);

			// Create MQC
			if (CommonUtil.equalsIn(productSpecData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare"))
			{
				eventLog.info("ProductSpecGroup: " + productSpecData.getUdfs().get("PRODUCTSPECGROUP"));
				MESLotServiceProxy.getLotServiceUtil().createMQCWithReturnBank(lotData, newLotData, productNameList, "", eventInfo);
			}

			// 3.return NewLot
			List<Element> eleNewLot = new ArrayList<Element>();
			eleNewLot.add(setMakeNewLot(newLotName));
			XmlUtil.setSubChildren(SMessageUtil.getBodyElement(doc), "LOTNAME", eleNewLot);
		}

		if (lotData.getFactoryName().equals("ARRAY") || lotData.getFactoryName().equals("TP"))
			MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfo, lotData.getProductRequestName(), productList.size(), 0);

		if (lotData.getFactoryName().equals("OLED"))
		{
			if (lotData.getProductType().equals("Sheet"))
			{
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfo, lotData.getProductRequestName(), (productList.size()) * 2, 0);
			}
			else
			{
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfo, lotData.getProductRequestName(), productList.size(), 0);
			}
		}

		if (lotData.getFactoryName().equals("POSTCELL"))
		{
			Product productDataForSpec = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
			ProductSpec productSpec = MESProductServiceProxy.getProductServiceUtil().getProductSpecByProductName(productDataForSpec);

			int xProductCount = Integer.parseInt(productSpec.getUdfs().get("PRODUCTCOUNTTOXAXIS"));
			int yProductCount = Integer.parseInt(productSpec.getUdfs().get("PRODUCTCOUNTTOYAXIS"));
			int incrementQty = (int) (productList.size() * xProductCount * yProductCount);

			MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfo, lotData.getProductRequestName(), incrementQty, 0);
		}
		
		//modify by wangys 2020/11/26 Cancel Auto CompleteWO
		/*ProductRequest newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());

		if (newProductRequestData.getPlanQuantity() == newProductRequestData.getFinishedQuantity() + newProductRequestData.getScrappedQuantity())
		{
			EventInfo newEventInfo = eventInfo;
			newEventInfo.setEventName("Complete");
			newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompleted(newEventInfo, lotData.getProductRequestName());
		}*/

		// Delete Q-TIME
		/*
		if (lotData.getFactoryName().equals("ARRAY") || lotData.getFactoryName().equals("OLED") || lotData.getFactoryName().equals("TP"))
			exitQTimeByLot(eventInfo, newLotName);
		*/
		if (lotData.getFactoryName().equals("ARRAY"))
		{
			if (productList.size() > 0)
			{
				for (Element product : productList)
				{
					List<Map<String, Object>> sqlForSelectGlassResult = MESLotServiceProxy.getLotServiceUtil().getGlassJudgeData(product.getChildText("PRODUCTNAME"));

					for (Map<String, Object> result : sqlForSelectGlassResult)
					{
						MESLotServiceProxy.getLotServiceUtil().updateGlassJudgeScrapFlag(eventInfo, result, "Y");
					}
				}
			}
		}
		
		updateProductProcessState(newLotName, productList, sLotProcessState, sProductProcessState);
		ExtendedObjectProxy.getSampleProductService().deleteSampleProduct(eventInfo, productNameList);

		return doc;
	}
     //caixu 2020 10 12 delete CT_OASCRAPPRODUCTNAME
	private void deleteScrapProduct(List<ProductP> productScrap) throws CustomException {
		// TODO Auto-generated method stub
		String queryStrigScrapProduct="DELETE FROM CT_OASCRAPPRODUCTNAME WHERE PRODUCTNAME=:PRODUCTNAME";
		List<Object[]> deleteScrapListProduct= new ArrayList<Object[]>();
		for(ProductP productData : productScrap)
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

	private Element setMakeNewLot(String newLotName)

	{
		Element eleNewLot = new Element("LOTNAME");
		try
		{
			XmlUtil.addElement(eleNewLot, "NEWLOTNAME", newLotName);

		}
		catch (Exception ex)
		{
			eventLog.warn(String.format("MakeNewLot[%s] is failed ", newLotName));
		}

		return eleNewLot;
	}

	private void updateProductProcessState(String lotName, List<Element> productList, String lotProcessState, String strProductProcessState)
	{

		for (Element productE : productList)
		{
			String sProductName = productE.getChild("PRODUCTNAME").getText();

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("PRODUCTPROCESSSTATE", strProductProcessState);
			bindMap.put("PRODUCTNAME", sProductName);

			StringBuffer updatesql = new StringBuffer();
			updatesql.append("UPDATE PRODUCT ");
			updatesql.append("   SET PRODUCTPROCESSSTATE = :PRODUCTPROCESSSTATE ");
			updatesql.append(" WHERE PRODUCTNAME = :PRODUCTNAME ");

			GenericServiceProxy.getSqlMesTemplate().update(updatesql.toString(), bindMap);

			StringBuffer updatesq2 = new StringBuffer();
			updatesq2.append("UPDATE PRODUCTHISTORY ");
			updatesq2.append("   SET PRODUCTPROCESSSTATE = :PRODUCTPROCESSSTATE ");
			updatesq2.append(" WHERE PRODUCTNAME = :PRODUCTNAME ");
			updatesq2.append("   AND TIMEKEY = (SELECT LASTEVENTTIMEKEY ");
			updatesq2.append("                    FROM PRODUCT ");
			updatesq2.append("                   WHERE PRODUCTNAME = :PRODUCTNAME) ");

			GenericServiceProxy.getSqlMesTemplate().update(updatesq2.toString(), bindMap);
		}

		String strLotName = lotName;
		Map<String, Object> bindMap2 = new HashMap<String, Object>();
		bindMap2.put("LOTPROCESSSTATE", lotProcessState);
		bindMap2.put("LOTNAME", strLotName);

		StringBuffer updatesql1 = new StringBuffer();
		updatesql1.append("UPDATE LOT ");
		updatesql1.append("   SET LOTPROCESSSTATE = :LOTPROCESSSTATE ");
		updatesql1.append(" WHERE LOTNAME = :LOTNAME ");

		GenericServiceProxy.getSqlMesTemplate().update(updatesql1.toString(), bindMap2);

		StringBuffer updatesql2 = new StringBuffer();
		updatesql2.append("UPDATE LOTHISTORY ");
		updatesql2.append("   SET LOTPROCESSSTATE = :LOTPROCESSSTATE ");
		updatesql2.append(" WHERE LOTNAME = :LOTNAME ");
		updatesql2.append("   AND TIMEKEY = (SELECT LASTEVENTTIMEKEY ");
		updatesql2.append("                    FROM LOT ");
		updatesql2.append("                   WHERE LOTNAME = :LOTNAME) ");

		GenericServiceProxy.getSqlMesTemplate().update(updatesql2.toString(), bindMap2);
	}

	private void exitQTimeByLot(EventInfo eventInfo, String lotName) throws CustomException
	{
		List<ProductQueueTime> QTimeDataList;

		try
		{
			QTimeDataList = ExtendedObjectProxy.getProductQTimeService().select("productName in (select productName from product where lotName = ?)", new Object[] { lotName });
		}
		catch (Exception ex)
		{
			QTimeDataList = new ArrayList<ProductQueueTime>();
		}

		for (ProductQueueTime QTimeData : QTimeDataList)
		{
			try
			{
				// enter into Q time
				exitQTime(eventInfo, QTimeData);
			}
			catch (Exception ex)
			{
				// Q-time process is optional
				if (logger.isWarnEnabled())
				{
					logger.warn(String.format("Q-time OUT process for Product[%s] to Operation[%s] is failed at Operation[%s]", QTimeData.getProductName(), QTimeData.getToProcessOperationName(),
							QTimeData.getProcessOperationName()));
				}
			}
		}
	}

	private void exitQTime(EventInfo eventInfo, ProductQueueTime QTimeData) throws CustomException
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