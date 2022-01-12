package kr.co.aim.messolution.lot.event.CNX;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.AssignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.MakeUnScrappedInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class UnscrapLot extends SyncHandler {

	private static Log log = LogFactory.getLog(UnscrapLot.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnScrap", this.getEventUser(), this.getEventComment(), "", "");//caixu 20200601 UnScrapLot Change UnScrap

		String sLotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		String newCarrierName = SMessageUtil.getBodyItemValue(doc, "NEWCARRIER", false);
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(sLotName);

		CommonValidation.checkJobDownFlag(lotData);

		lotData.setLotGrade("G");
		LotKey lotKey = lotData.getKey();
		double productQuantity = lotData.getProductQuantity();
		List<ProductU> productUSequence = new ArrayList<ProductU>();

		productUSequence = MESLotServiceProxy.getLotInfoUtil().setProductUSequence(productList);
		MakeUnScrappedInfo makeUnScrappedInfo = MESLotServiceProxy.getLotInfoUtil().makeUnScrappedInfo(lotData, lotData.getLotProcessState(), productQuantity, productUSequence);

		String reasonCode = lotData.getReasonCode();
		eventInfo.setReasonCode(reasonCode);

		// Execute
		LotServiceProxy.getLotService().makeUnScrapped(lotKey, eventInfo, makeUnScrappedInfo);

		this.UpdateTabel(productList);

		// change scrap qty
		if (lotData.getFactoryName().equals("ARRAY") || lotData.getFactoryName().equals("TP"))
			MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfo, lotData.getProductRequestName(), 0, productList.size());

		if (lotData.getFactoryName().equals("OLED"))
		{
			if (lotData.getProductType().equals("Sheet"))
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfo, lotData.getProductRequestName(), 0, (productList.size()) * 2);
			else
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfo, lotData.getProductRequestName(), 0, productList.size());
		}

		// Check WO State
		
		ProductRequest newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());
		if ( newProductRequestData.getProductRequestState().equals("Completed"))
		{
			throw new CustomException("PRODUCTREQUEST-0004", newProductRequestData.getProductRequestState());
		}
		//Modify by wangys 2020/11/26 Cancel auto Released
		/*if (newProductRequestData.getProductRequestState().equals("Completed")
				&& (newProductRequestData.getPlanQuantity() > newProductRequestData.getFinishedQuantity() + newProductRequestData.getScrappedQuantity()))
		{
			EventInfo newEventInfo = eventInfo;
			newEventInfo.setEventName("Release");
			MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeReleased(newEventInfo, lotData.getProductRequestName());
		}*/

		if (productUSequence.size() > 0)
		{
			if (lotData.getFactoryName().equals("ARRAY"))
			{
				for (ProductU productU : productUSequence)
				{
					List<Map<String, Object>> sqlForSelectGlassResult = MESLotServiceProxy.getLotServiceUtil().getGlassJudgeData(productU.getProductName());

					for (Map<String, Object> result : sqlForSelectGlassResult)
					{
						MESLotServiceProxy.getLotServiceUtil().updateGlassJudgeScrapFlag(eventInfo, result, "");
					}
				}
			}

			List<ProductP> productPSequence = new ArrayList<ProductP>();
			List<Product> productDatas = new ArrayList<Product>();

			productDatas = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(sLotName);

			for (Iterator<Product> iteratorProduct = productDatas.iterator(); iteratorProduct.hasNext();)
			{
				Product product = iteratorProduct.next();

				ProductP productP = new ProductP();

				productP.setProductName(product.getKey().getProductName());
				productP.setPosition(product.getPosition());
				productPSequence.add(productP);
			}

			//Validation
			CommonValidation.checkEmptyCst(newCarrierName);
			CommonValidation.checkMultiLot(newCarrierName);
			
			AssignCarrierInfo createInfo = MESLotServiceProxy.getLotInfoUtil().AssignCarrierInfo(lotData, newCarrierName, productPSequence);
			EventInfo eventInfoForAssign = EventInfoUtil.makeEventInfo("AssignCarrier", getEventUser(), getEventComment(), "", "");

			MESLotServiceProxy.getLotServiceImpl().assignCarrier(lotData, createInfo, eventInfoForAssign);
		}

		return doc;
	}

	@SuppressWarnings("unchecked")
	public List<ListOrderedMap> getRecoveryProduct(String productName) throws CustomException
	{
		List<ListOrderedMap> productList;
		try
		{
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT * ");
			sql.append("  FROM (SELECT A.PRODUCTSTATE, A.PRODUCTPROCESSSTATE, A.PRODUCTHOLDSTATE, A.TIMEKEY, A.PRODUCTGRADE ");
			sql.append("          FROM PRODUCTHISTORY A, ");
			sql.append("               (SELECT TIMEKEY ");
			sql.append("                  FROM PRODUCTHISTORY ");
			sql.append("                 WHERE PRODUCTNAME = :PRODUCTNAME ");
			sql.append("                   AND EVENTNAME IN ('Scrap', 'ScrapLot', 'ScrapProduct') ");
			sql.append("                   AND ROWNUM = '1' ");
			sql.append("                ORDER BY TIMEKEY DESC) B ");
			sql.append("         WHERE 1 = 1 ");
			sql.append("           AND A.PRODUCTNAME = :PRODUCTNAME ");
			sql.append("           AND A.TIMEKEY < B.TIMEKEY ");
			sql.append("        ORDER BY A.TIMEKEY DESC) ");
			sql.append(" WHERE 1 = 1 ");
			sql.append("   AND ROWNUM <= 5 ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("PRODUCTNAME", productName);

			productList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
		}
		catch (greenFrameDBErrorSignal de)
		{
			productList = null;

			if (de.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(de.getDataKey(), de.getSql());
			else
				throw new CustomException("SYS-8001", de.getSql());
		}
		return productList;
	}

	private void UpdateTabel(List<Element> productList) throws CustomException
	{
		// get time yyyymmddhhmm
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmm");
		String time = sdf.format(date);

		for (Element productE : productList)
		{
			String sProductName = productE.getChild("PRODUCTNAME").getText();
			Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(sProductName);
			log.info("get producthistoryinfo start time:" + time + String.valueOf(System.currentTimeMillis()));
			List<ListOrderedMap> productUList = this.getRecoveryProduct(sProductName);
			log.info("get producthistoryinfo end time:" + time + String.valueOf(System.currentTimeMillis()));
			productData.setProductState(CommonUtil.getValue(productUList.get(0), "PRODUCTSTATE"));
			productData.setProductProcessState(CommonUtil.getValue(productUList.get(0), "PRODUCTPROCESSSTATE"));
			productData.setProductHoldState(CommonUtil.getValue(productUList.get(0), "PRODUCTHOLDSTATE"));
			productData.setProductGrade(CommonUtil.getValue(productUList.get(0), "PRODUCTGRADE"));
			productData.getUdfs().put("MANUALSCRAPOPERMACHINE", "");
			ProductServiceProxy.getProductService().update(productData);

			log.info("get start update product time:" + time + String.valueOf(System.currentTimeMillis()));
			Map<String, Object> bindMap = new HashMap<String, Object>();
			log.info("get start end product time:" + time + String.valueOf(System.currentTimeMillis()));

			log.info("get start update producthistory for judge and state time:" + time + String.valueOf(System.currentTimeMillis()));
			bindMap.clear();
			bindMap = new HashMap<String, Object>();
			bindMap.put("PRODUCTPROCESSSTATE", productData.getProductProcessState());
			bindMap.put("PRODUCTNAME", sProductName);

			StringBuffer sqlstate = new StringBuffer();
			sqlstate.append("UPDATE PRODUCTHISTORY ");
			sqlstate.append("   SET PRODUCTPROCESSSTATE = :PRODUCTPROCESSSTATE ");
			sqlstate.append(" WHERE 1 = 1 ");
			sqlstate.append("   AND PRODUCTNAME = :PRODUCTNAME ");
			sqlstate.append("   AND TIMEKEY = (SELECT LASTEVENTTIMEKEY ");
			sqlstate.append("                    FROM PRODUCT ");
			sqlstate.append("                   WHERE PRODUCTNAME = :PRODUCTNAME) ");

			try
			{
				GenericServiceProxy.getSqlMesTemplate().update(sqlstate.toString(), bindMap);
				log.info("get end update producthistory for judge and state time:" + time + String.valueOf(System.currentTimeMillis()));
			}
			catch (Exception e)
			{
				throw new CustomException("SYS-8001", e.getMessage());
			}
		}
	}
}
