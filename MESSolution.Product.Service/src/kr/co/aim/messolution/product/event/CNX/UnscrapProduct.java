package kr.co.aim.messolution.product.event.CNX;

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
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.data.LotHistoryKey;
import kr.co.aim.greentrack.lot.management.info.AssignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.MakeUnScrappedInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.lot.management.info.SplitInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductHistory;
import kr.co.aim.greentrack.product.management.data.ProductHistoryKey;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class UnscrapProduct extends SyncHandler {

	private static Log log = LogFactory.getLog(UnscrapProduct.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment(), "", "");

		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String processOperName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String lotprocessState = SMessageUtil.getBodyItemValue(doc, "OLDLOTPROCESSSTATE", true);
		String productType = SMessageUtil.getBodyItemValue(doc, "OLDPRODUCTTYPE", true);
		String newCarrierName = SMessageUtil.getBodyItemValue(doc, "NEWCARRIER", false);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", false);

		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		
		String saveNodeStack = "";

		// Get Pat
		boolean componentRunFlag = false;
		String stateCheck = "";

		for (Element product : productList)
		{
			Product productInfo = ProductServiceProxy.getProductService().selectByKey(new ProductKey(product.getChildText("PRODUCTNAME").toString()));

			if (StringUtil.isEmpty(saveNodeStack))
				saveNodeStack = productInfo.getNodeStack();

			ProductHistoryKey productHistoryKey = new ProductHistoryKey();
			productHistoryKey.setProductName(productInfo.getKey().getProductName());
			productHistoryKey.setTimeKey(productInfo.getLastEventTimeKey());
			List<ProductHistory> productHistoryList = ProductServiceProxy.getProductHistoryService().select(" productName = ? order by timekey desc ",
					new Object[] { productInfo.getKey().getProductName() });

			if (productHistoryList.get(0).getEventComment().equals("ComponentScrap"))
			{
				if (stateCheck.isEmpty())
				{
					stateCheck = productHistoryList.get(1).getProductProcessState();

					if (productHistoryList.get(1).getProductProcessState().equals("Processing"))
						componentRunFlag = true;
				}
				else
				{
					if (stateCheck.equals(productHistoryList.get(1).getProductProcessState()))
						continue;
					else
						throw new CustomException("PRODUCT-9016", productHistoryList.get(0).getKey().getProductName());
				}
			}
			else
			{
				break;
			}
		}

		List<Element> remainProductList = null;
		try
		{
			remainProductList = SMessageUtil.getBodySequenceItemList(doc, "REMAINPRODUCTLIST", true);
		}
		catch (Exception e)
		{

		}

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		String newLotName = "";

		List<Product> listProduct = MESProductServiceProxy.getProductServiceUtil().allScrappedProductsByLot(lotName);

		// 4.Update LotState 'Emptied' to 'Scrapped'
		lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		if (lotData.getLotState().equals("Emptied"))
		{
			List<Product> productDatas = ProductServiceProxy.getProductService().allProductsByLot(lotData.getKey().getLotName());

			for (Iterator<Product> iteratorProduct = productDatas.iterator(); iteratorProduct.hasNext();)
			{
				Product product = iteratorProduct.next();

				if (product.getProductState().equals("Scrapped"))
				{
					lotData.setLotState("Scrapped");
					lotData.setLotProcessState("WAIT");
					LotServiceProxy.getLotService().update(lotData);

					LotHistoryKey lotHistKey = new LotHistoryKey();
					lotHistKey.setLotName(lotData.getKey().getLotName());

					LotHistory lotHistoryData = LotServiceProxy.getLotHistoryService().selectLastOne(lotHistKey);
					lotHistoryData.setLotState("Scrapped");
					LotServiceProxy.getLotHistoryService().update(lotHistoryData);
					break;
				}
			}
		}

		if (StringUtil.equals(lotData.getProcessOperationName(), processOperName))
		{
			if (listProduct.size() != productList.size() && lotData.getLotProcessState().equals(lotprocessState))
			{
				this.UnScrapProduct(lotName, productList, eventInfo, lotprocessState);
				newLotName = this.SplitLot(lotName, productList, eventInfo, lotprocessState, processOperName, productType, remainProductList);
			}
			else
			{
				this.UnScrapProduct(lotName, productList, eventInfo, lotprocessState);

				// Split
				newLotName = this.SplitLot(lotName, productList, eventInfo, lotprocessState, processOperName, productType, remainProductList);
			}
		}

		if (!StringUtil.equals(lotData.getProcessOperationName(), processOperName))
		{
			this.UnScrapProduct(lotName, productList, eventInfo, lotprocessState);

			// Split
			newLotName = this.SplitLot(lotName, productList, eventInfo, lotprocessState, processOperName, productType, remainProductList);

		}
		eventInfo = EventInfoUtil.makeEventInfo("UnScrap", this.getEventUser(), this.getEventComment(), "", "");//caixu 2020/06/01 UnScrapProduct Modify UnScrap

		for (Element productE : productList)
		{
			String sProductName = productE.getChild("PRODUCTNAME").getText();
			Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(sProductName);
			Lot newLotDate = MESLotServiceProxy.getLotInfoUtil().getLotData(newLotName);
			try
			{
				updateProductData(newLotDate, sProductName, productData);
			}
			catch (Exception e)
			{
			}
		}

		if (componentRunFlag)
		{
			EventInfo changeEventInfo = EventInfoUtil.makeEventInfo("ChangeState", this.getEventUser(), this.getEventComment(), "", "");
			changeEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			ChangeState(changeEventInfo, newLotName);
		}

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

		if (lotData.getFactoryName().equals("POSTCELL"))
		{
			Product productDataForSpec = MESProductServiceProxy.getProductServiceUtil().getProductData(productList.get(0).getChildText("PRODUCTNAME").toString());
			ProductSpec productSpec = MESProductServiceProxy.getProductServiceUtil().getProductSpecByProductName(productDataForSpec);

			int xProductCount = Integer.parseInt(productSpec.getUdfs().get("PRODUCTCOUNTTOXAXIS"));
			int yProductCount = Integer.parseInt(productSpec.getUdfs().get("PRODUCTCOUNTTOYAXIS"));
			int incrementQty = (int) (productList.size() * xProductCount * yProductCount);

			MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfo, lotData.getProductRequestName(), 0, incrementQty);
		}

		// Check WO State
		//Modify by wangys 2020/11/26 Cancel auto Released
		ProductRequest newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());
		if ( newProductRequestData.getProductRequestState().equals("Completed"))
		{
			throw new CustomException("PRODUCTREQUEST-0004", newProductRequestData.getProductRequestState());
		}
		/*if (newProductRequestData.getProductRequestState().equals("Completed")
				&& (newProductRequestData.getPlanQuantity() > newProductRequestData.getFinishedQuantity() + newProductRequestData.getScrappedQuantity()))
		{
			EventInfo newEventInfo = eventInfo;
			newEventInfo.setEventName("Release");
			MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeReleased(newEventInfo, lotData.getProductRequestName());
		}*/

		if (lotData.getFactoryName().equals("ARRAY"))
		{
			if (productList.size() > 0)
			{
				for (Element product : productList)
				{
					List<Map<String, Object>> sqlForSelectGlassResult = MESLotServiceProxy.getLotServiceUtil().getGlassJudgeData(product.getChildText("PRODUCTNAME"));

					for (Map<String, Object> result : sqlForSelectGlassResult)
					{
						MESLotServiceProxy.getLotServiceUtil().updateGlassJudgeScrapFlag(eventInfo, result, "");
					}
				}
			}
		}

		if (newCarrierName != "")
		{
			List<ProductP> productPSequence = new ArrayList<ProductP>();
			List<Product> productDatas = new ArrayList<Product>();

			//Validation
			CommonValidation.checkEmptyCst(newCarrierName);
			CommonValidation.checkMultiLot(newCarrierName);

			productDatas = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(newLotName);

			for (Iterator<Product> iteratorProduct = productDatas.iterator(); iteratorProduct.hasNext();)
			{
				Product product = iteratorProduct.next();

				ProductP productP = new ProductP();

				productP.setProductName(product.getKey().getProductName());
				productP.setPosition(product.getPosition());

				productPSequence.add(productP);
			}

			AssignCarrierInfo createInfo = MESLotServiceProxy.getLotInfoUtil().AssignCarrierInfo(lotData, newCarrierName, productPSequence);
			EventInfo eventInfoForAssign = EventInfoUtil.makeEventInfo("AssignCarrier", getEventUser(), getEventComment(), "", "");

			Lot newLotData = new Lot();
			newLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(newLotName);

			MESLotServiceProxy.getLotServiceImpl().assignCarrier(newLotData, createInfo, eventInfoForAssign);
			MESLotServiceProxy.getLotServiceUtil().deleteScrapGlass(productPSequence);
			
		}

		// Sync ProductSpec
		boolean changeFlag = false;
		boolean nodeFlag = false;

		if (!StringUtil.isEmpty(newLotName))
		{
			Lot newLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(newLotName);
			Lot oldLot = (Lot) ObjectUtil.copyTo(newLotData);

			EventInfo eventInfoChangeSpec = EventInfoUtil.makeEventInfo("SyncData", this.getEventUser(), this.getEventComment(), "", "");
			eventInfoChangeSpec.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfoChangeSpec.setEventTime(TimeStampUtil.getCurrentTimestamp());

			boolean MQCFlag = false;
			
			ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(newLotData.getFactoryName(), newLotData.getProductSpecName(),
					GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
			
			if (CommonUtil.equalsIn(productSpecData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare"))
			{
				MQCFlag = true;
			}
			
			if (!newLotData.getProductSpecName().equals(productSpecName) && !MQCFlag)
			{
				newLotData.setProductSpecName(productSpecName);
				changeFlag = true;
			}

			if (!newLotData.getNodeStack().equals(saveNodeStack))
			{
				newLotData.setNodeStack(saveNodeStack);
				changeFlag = true;
				nodeFlag = true;
			}

			if (changeFlag)
			{
				MESLotServiceProxy.getLotServiceImpl().setEventForce(eventInfoChangeSpec, oldLot, newLotData);

				if (nodeFlag)
				{
					List<Product> newProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(newLotName);

					List<Product> updateProductList = new ArrayList<Product>();
					List<ProductHistory> updateProductListHistory = new ArrayList<ProductHistory>();

					for (Product product : newProductList)
					{
						Product oldProduct = (Product) ObjectUtil.copyTo(product);

						product.setNodeStack(saveNodeStack);
						product.setLastEventName(eventInfoChangeSpec.getEventName());
						product.setLastEventTime(eventInfoChangeSpec.getEventTime());
						product.setLastEventTimeKey(eventInfoChangeSpec.getEventTimeKey());
						product.setLastEventComment(eventInfoChangeSpec.getEventComment());
						product.setLastEventUser(eventInfoChangeSpec.getEventUser());

						ProductHistory productHistory = new ProductHistory();
						productHistory = ProductServiceProxy.getProductHistoryDataAdaptor().setHV(oldProduct, product, productHistory);

						updateProductList.add(product);
						updateProductListHistory.add(productHistory);
					}

					if (updateProductList.size() > 0)
					{
						log.debug("Insert Product, ProductHistory");

						try
						{
							CommonUtil.executeBatch("update", updateProductList);
							CommonUtil.executeBatch("insert", updateProductListHistory);
						}
						catch (Exception e)
						{
							log.error(e.getMessage());
							throw new CustomException(e.getCause());
						}
					}
				}
			}
		}

		// return NewLot
		List<Element> eleNewLot = new ArrayList<Element>();
		eleNewLot.add(setMakeNewLot(newLotName));
		XmlUtil.setSubChildren(SMessageUtil.getBodyElement(doc), "LOTNAME", eleNewLot);

		return doc;
	}

	
	//
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

	@SuppressWarnings("unchecked")
	public List<ListOrderedMap> getRecoveryProduct(String productName) throws CustomException
	{
		List<ListOrderedMap> productList;
		try
		{
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT * ");
			sql.append("  FROM (SELECT A.PRODUCTSTATE, ");
			sql.append("               A.PRODUCTPROCESSSTATE, ");
			sql.append("               A.PRODUCTHOLDSTATE, ");
			sql.append("               A.TIMEKEY, ");
			sql.append("               A.PRODUCTGRADE, ");
			sql.append("               A.NODESTACK, ");
			sql.append("               A.MACHINENAME, ");
			sql.append("               A.MACHINERECIPENAME, ");
			sql.append("               A.PROCESSFLOWNAME ");
			sql.append("          FROM PRODUCTHISTORY A, ");
			sql.append("               (SELECT TIMEKEY ");
			sql.append("                  FROM PRODUCTHISTORY ");
			sql.append("                 WHERE PRODUCTNAME = :PRODUCTNAME ");
			sql.append("                   AND EVENTNAME IN ('ScrapLot', 'Scrap Lot', 'ScrapProduct', 'Scrap Product', 'Scrap') ");
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

	private void UnScrapProduct(String lotName, List<Element> productList, EventInfo eventInfo, String lotProcessState) throws CustomException
	{
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmm");
		String time = sdf.format(date);
		// end
		List<ProductU> productUSequence = new ArrayList<ProductU>();
		productUSequence = MESLotServiceProxy.getLotInfoUtil().setProductUSequence(productList);

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		String reasoncode = lotData.getReasonCode();

		eventInfo = EventInfoUtil.makeEventInfo("UnScrap", this.getEventUser(), this.getEventComment(), "", reasoncode);//caixu 20200601 UnScrapProduct Modify UnScrap
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		log.info("get unscrap start time:" + time + String.valueOf(System.currentTimeMillis()));

		MakeUnScrappedInfo makeUnScrappedInfo = MESLotServiceProxy.getLotInfoUtil().makeUnScrappedInfo(lotData, lotProcessState, productList.size(), productUSequence);
		MESLotServiceProxy.getLotServiceImpl().makeUnScrapped(eventInfo, lotData, makeUnScrappedInfo);

		log.info("get unscrap end time:" + time + String.valueOf(System.currentTimeMillis()));
	}

	private String SplitLot(String lotName, List<Element> productList, EventInfo eventInfo, String lotprocessState, String processOperation, String productType, List<Element> remainProductList)
			throws CustomException
	{
		// 1.get ProcessFlow and Oper
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmm");
		String time = sdf.format(date);

		log.info("get lothistoryinfo start time:" + time + String.valueOf(System.currentTimeMillis()));
		List<ListOrderedMap> lotUList = this.getRecoveryLot(lotName);

		log.info("get lothistoryinfo end time:" + time + String.valueOf(System.currentTimeMillis()));
		String reworkState = CommonUtil.getValue(lotUList.get(0), "REWORKSTATE");
		String reworkCount = CommonUtil.getValue(lotUList.get(0), "REWORKCOUNT");
		String sNodeStack = CommonUtil.getValue(lotUList.get(0), "NODESTACK");
		String returnFlowName = CommonUtil.getValue(lotUList.get(0), "RETURNFLOWNAME");
		String returnOper = CommonUtil.getValue(lotUList.get(0), "RETURNOPERATIONNAME");
		String portName = CommonUtil.getValue(lotUList.get(0), "PORTNAME");
		String portType = CommonUtil.getValue(lotUList.get(0), "PORTTYPE");
		String portUseType = CommonUtil.getValue(lotUList.get(0), "PORTUSETYPE");
		String productProcessState = "";
		List<ListOrderedMap> productUListnew = this.getRecoveryProduct(productList.get(0).getChildText("PRODUCTNAME"));
		String machineRecipeName = CommonUtil.getValue(productUListnew.get(0), "MACHINERECIPENAME");
		String flowName = CommonUtil.getValue(productUListnew.get(0), "PROCESSFLOWNAME");
		String machineName = CommonUtil.getValue(productUListnew.get(0), "MACHINENAME");

		if (StringUtils.equals(lotprocessState, GenericServiceProxy.getConstantMap().Lot_LoggedOut))
			productProcessState = GenericServiceProxy.getConstantMap().Prod_Idle;
		else
			productProcessState = GenericServiceProxy.getConstantMap().Prod_Processing;

		// 2.Split
		String newLotName = "";
		eventInfo = EventInfoUtil.makeEventInfo("Split", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(lotData.getFactoryName(), lotData.getProductSpecName(),
				GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);

		List<Product> productDatas = ProductServiceProxy.getProductService().allProductsByLot(lotName);
		List<ProductP> productPSequence = MESLotServiceProxy.getLotInfoUtil().setProductPSequence(productList, lotName);
		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("LOTNAME", lotName);

		try
		{
			productDatas = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);
		}
		catch (Exception e)
		{
		}

		try
		{
			List<String> lstName = null;
			lstName = CommonUtil.generateNameByNamingRule("SplitLotNaming", nameRuleAttrMap, 1);
			newLotName = lstName.get(0);
		}
		catch (Exception ex)
		{
			new CustomException("LOT-9011", ex.getMessage());
		}

		if (productDatas.size() <= 1 && remainProductList != null)
		{
			String sourLotGrade = CommonUtil.judgeLotGradeByProductList(remainProductList, "PRODUCTGRADE");
			MESLotServiceProxy.getLotServiceImpl().updateLotData("LOTGRADE", sourLotGrade, lotName);
		}

		if (productList != null)
		{
			String desLotGrade = CommonUtil.judgeLotGradeByProductList(productList, "PRODUCTGRADE");
			MESLotServiceProxy.getLotServiceImpl().updateLotData("LOTGRADE", desLotGrade, newLotName);
		}

		log.info("get splitlot start time:" + time + String.valueOf(System.currentTimeMillis()));
		SplitInfo splitInfo = MESLotServiceProxy.getLotInfoUtil().splitLotInfo(lotData, newLotName, productPSequence, String.valueOf(productList.size()));

		MESLotServiceProxy.getLotServiceImpl().splitLot(eventInfo, lotData, splitInfo);

		if (CommonUtil.equalsIn(productSpecData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare"))
		{
			eventLog.info("ProductSpecGroup: " + productSpecData.getUdfs().get("PRODUCTSPECGROUP"));
			List<String> sProductList = MESLotServiceProxy.getLotServiceUtil().getProductListByElementList(productList);
			Lot newLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(newLotName);
			MESLotServiceProxy.getLotServiceUtil().createMQC(lotData, newLotData, sProductList, "", eventInfo);
		}

		// Fix SourceLot
		if (productDatas.size() <= 1 && remainProductList != null)
		{
			Map<String, Object> fixSourceLotMap = new HashMap<String, Object>();
			StringBuffer updateFixLot = new StringBuffer();
			updateFixLot.append("UPDATE LOT ");
			updateFixLot.append("   SET LOTSTATE = :LOTSTATE ");
			updateFixLot.append(" WHERE LOTNAME = :LOTNAME ");

			fixSourceLotMap.put("LOTSTATE", "Scrapped");
			fixSourceLotMap.put("LOTNAME", lotData.getKey().getLotName());
			GenericServiceProxy.getSqlMesTemplate().update(updateFixLot.toString(), fixSourceLotMap);

			fixSourceLotMap.clear();
			fixSourceLotMap = new HashMap<String, Object>();
			fixSourceLotMap.put("LOTSTATE", "Scrapped");
			fixSourceLotMap.put("LOTNAME", lotData.getKey().getLotName());

			StringBuffer fixSourceLotHistorySql = new StringBuffer();
			fixSourceLotHistorySql.append("UPDATE LOTHISTORY ");
			fixSourceLotHistorySql.append("   SET LOTSTATE = :LOTSTATE ");
			fixSourceLotHistorySql.append(" WHERE 1 = 1 ");
			fixSourceLotHistorySql.append("   AND LOTNAME = :LOTNAME ");
			fixSourceLotHistorySql.append("   AND TIMEKEY = (SELECT LASTEVENTTIMEKEY ");
			fixSourceLotHistorySql.append("                    FROM LOT ");
			fixSourceLotHistorySql.append("                   WHERE LOTNAME = :LOTNAME) ");

			try
			{
				GenericServiceProxy.getSqlMesTemplate().update(fixSourceLotHistorySql.toString(), fixSourceLotMap);
				log.info("get update lothistory table end time:" + time + String.valueOf(System.currentTimeMillis()));
			}
			catch (Exception ex)
			{
				throw new CustomException("SYS-8001", ex.getMessage());
			}
		}

		log.info("get splitlot end time:" + time + String.valueOf(System.currentTimeMillis()));
		log.info("get update lot table start time:" + String.valueOf(System.currentTimeMillis()));

		StringBuffer updatesql = new StringBuffer();
		updatesql.append("UPDATE LOT ");
		updatesql.append("   SET LOTPROCESSSTATE = :LOTPROCESSSTATE, ");
		updatesql.append("       PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME, ");
		updatesql.append("       PROCESSFLOWNAME = :PROCESSFLOWNAME, ");
		updatesql.append("       NODESTACK = :NODESTACK, ");
		updatesql.append("       REWORKSTATE = :REWORKSTATE, ");
		updatesql.append("       REWORKCOUNT = :REWORKCOUNT, ");
		updatesql.append("       RETURNFLOWNAME = :RETURNFLOWNAME, ");
		updatesql.append("       RETURNOPERATIONNAME = :RETURNOPERATIONNAME, ");
		updatesql.append("       MACHINENAME = :MACHINENAME, ");
		updatesql.append("       PORTNAME = :PORTNAME, ");
		updatesql.append("       PORTTYPE = :PORTTYPE, ");
		updatesql.append("       PORTUSETYPE = :PORTUSETYPE, ");
		updatesql.append("       PRODUCTTYPE = :PRODUCTTYPE, ");
		updatesql.append("       MACHINERECIPENAME = :MACHINERECIPENAME ");
		updatesql.append(" WHERE LOTNAME = :LOTNAME ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("LOTPROCESSSTATE", lotprocessState);
		bindMap.put("LOTNAME", newLotName);
		bindMap.put("PROCESSOPERATIONNAME", processOperation);
		bindMap.put("PROCESSFLOWNAME", flowName);
		bindMap.put("REWORKSTATE", reworkState);
		bindMap.put("REWORKCOUNT", reworkCount);
		bindMap.put("RETURNFLOWNAME", returnFlowName);
		bindMap.put("NODESTACK", sNodeStack);
		bindMap.put("RETURNOPERATIONNAME", returnOper);
		bindMap.put("MACHINENAME", machineName);
		bindMap.put("PORTNAME", portName);
		bindMap.put("PORTTYPE", portType);
		bindMap.put("PORTUSETYPE", portUseType);
		bindMap.put("PRODUCTTYPE", productType);
		bindMap.put("MACHINERECIPENAME", machineRecipeName);

		GenericServiceProxy.getSqlMesTemplate().update(updatesql.toString(), bindMap);

		log.info("get update lot table end time:" + time + String.valueOf(System.currentTimeMillis()));
		log.info("get update lothistory table start time:" + time + String.valueOf(System.currentTimeMillis()));

		bindMap.clear();
		bindMap = new HashMap<String, Object>();
		bindMap.put("LOTPROCESSSTATE", lotprocessState);
		bindMap.put("LOTNAME", newLotName);
		bindMap.put("PROCESSOPERATIONNAME", processOperation);
		bindMap.put("PROCESSFLOWNAME", flowName);
		bindMap.put("REWORKSTATE", reworkState);
		bindMap.put("REWORKCOUNT", reworkCount);
		bindMap.put("RETURNFLOWNAME", returnFlowName);
		bindMap.put("NODESTACK", sNodeStack);
		bindMap.put("RETURNOPERATIONNAME", returnOper);
		bindMap.put("MACHINENAME", machineName);
		bindMap.put("PORTNAME", portName);
		bindMap.put("PORTTYPE", portType);
		bindMap.put("PORTUSETYPE", portUseType);
		bindMap.put("PRODUCTTYPE", productType);
		bindMap.put("MACHINERECIPENAME", machineRecipeName);

		StringBuffer sqlstate = new StringBuffer();
		sqlstate.append("UPDATE LOTHISTORY ");
		sqlstate.append("   SET LOTPROCESSSTATE = :LOTPROCESSSTATE, ");
		sqlstate.append("       PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME, ");
		sqlstate.append("       PROCESSFLOWNAME = :PROCESSFLOWNAME, ");
		sqlstate.append("       NODESTACK = :NODESTACK, ");
		sqlstate.append("       REWORKSTATE = :REWORKSTATE, ");
		sqlstate.append("       REWORKCOUNT = :REWORKCOUNT, ");
		sqlstate.append("       RETURNFLOWNAME = :RETURNFLOWNAME, ");
		sqlstate.append("       RETURNOPERATIONNAME = :RETURNOPERATIONNAME, ");
		sqlstate.append("       MACHINENAME = :MACHINENAME, ");
		sqlstate.append("       PORTNAME = :PORTNAME, ");
		sqlstate.append("       PORTTYPE = :PORTTYPE, ");
		sqlstate.append("       PORTUSETYPE = :PORTUSETYPE, ");
		sqlstate.append("       PRODUCTTYPE = :PRODUCTTYPE, ");
		sqlstate.append("       MACHINERECIPENAME = :MACHINERECIPENAME ");
		sqlstate.append(" WHERE 1 = 1 ");
		sqlstate.append("   AND LOTNAME = :LOTNAME ");
		sqlstate.append("   AND TIMEKEY = (SELECT LASTEVENTTIMEKEY ");
		sqlstate.append("                    FROM LOT ");
		sqlstate.append("                   WHERE LOTNAME = :LOTNAME) ");

		try
		{
			GenericServiceProxy.getSqlMesTemplate().update(sqlstate.toString(), bindMap);
			log.info("get update lothistory table end time:" + time + String.valueOf(System.currentTimeMillis()));
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-8001", ex.getMessage());
		}

		if (lotData.getFactoryName().equals("POSTCELL"))
		{
			Lot newLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(newLotName);
			newLotData.setLotGrade("G");

			EventInfo eventInfoNew = EventInfoUtil.makeEventInfo("ChangeGrade", this.getEventUser(), this.getEventComment(), "", "");

			LotServiceProxy.getLotService().update(newLotData);

			// Set Event
			SetEventInfo setEventInfo = new SetEventInfo();
			LotServiceProxy.getLotService().setEvent(newLotData.getKey(), eventInfoNew, setEventInfo);
		}

		for (Element productE : productList)
		{
			String sProductName = productE.getChild("PRODUCTNAME").getText();
			Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(sProductName);

			log.info("get producthistory reworkstate table start time:" + String.valueOf(System.currentTimeMillis()));
			List<ListOrderedMap> productUList = this.getRecoveryProduct(sProductName);

			log.info("get producthistory reworkstate table end time:" + String.valueOf(System.currentTimeMillis()));

			productData.setProductState(CommonUtil.getValue(productUList.get(0), "PRODUCTSTATE"));
			productData.setProductProcessState(productProcessState);
			productData.setProductHoldState(CommonUtil.getValue(productUList.get(0), "PRODUCTHOLDSTATE"));
			productData.setProductGrade(CommonUtil.getValue(productUList.get(0), "PRODUCTGRADE"));
			ProductServiceProxy.getProductService().update(productData);

			log.info("update producthistory reworkstate table start time:" + time + String.valueOf(System.currentTimeMillis()));
			StringBuffer updatesql2 = new StringBuffer();
			updatesql2.append("UPDATE PRODUCT ");
			updatesql2.append("   SET REWORKSTATE = :REWORKSTATE, REWORKCOUNT = :REWORKCOUNT, NODESTACK = :NODESTACK ");
			updatesql2.append(" WHERE PRODUCTNAME = :PRODUCTNAME ");

			bindMap.clear();
			bindMap = new HashMap<String, Object>();
			bindMap.put("REWORKSTATE", reworkState);
			bindMap.put("REWORKCOUNT", reworkCount);
			bindMap.put("NODESTACK", sNodeStack);
			bindMap.put("PRODUCTNAME", sProductName);

			GenericServiceProxy.getSqlMesTemplate().update(updatesql2.toString(), bindMap);
			log.info("update producthistory reworkstate table end time:" + time + String.valueOf(System.currentTimeMillis()));

			log.info("update producthistory reworkstate table start time:" + time + String.valueOf(System.currentTimeMillis()));
			bindMap.clear();
			bindMap = new HashMap<String, Object>();
			bindMap.put("REWORKSTATE", reworkState);
			bindMap.put("REWORKCOUNT", reworkCount);
			bindMap.put("NODESTACK", sNodeStack);
			bindMap.put("PRODUCTNAME", sProductName);

			StringBuffer updatesql3 = new StringBuffer();
			updatesql3.append("UPDATE PRODUCTHISTORY ");
			updatesql3.append("   SET REWORKSTATE = :REWORKSTATE, REWORKCOUNT = :REWORKCOUNT, NODESTACK = :NODESTACK ");
			updatesql3.append(" WHERE 1 = 1 ");
			updatesql3.append("   AND PRODUCTNAME = :PRODUCTNAME ");
			updatesql3.append("   AND TIMEKEY = (SELECT LASTEVENTTIMEKEY ");
			updatesql3.append("                    FROM PRODUCT ");
			updatesql3.append("                   WHERE PRODUCTNAME = :PRODUCTNAME) ");

			try
			{
				GenericServiceProxy.getSqlMesTemplate().update(updatesql3.toString(), bindMap);
				log.info("update producthistory reworkstate table eend0 time:" + time + String.valueOf(System.currentTimeMillis()));
			}
			catch (Exception e)
			{
				throw new CustomException("SYS-8001", e.getMessage());
			}
		}
		return newLotName;
	}

	private void ChangeState(EventInfo eventInfo, String lotName)
	{
		try
		{
			List<Product> productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotName);

			for (Product productInfo : productList)
			{
				Product oldProductInfo = (Product) ObjectUtil.copyTo(productInfo);
				productInfo.setLastEventComment(eventInfo.getEventComment());
				productInfo.setLastEventName(eventInfo.getEventName());
				productInfo.setLastEventTime(eventInfo.getEventTime());
				productInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
				productInfo.setLastEventUser(eventInfo.getEventUser());
				productInfo.setProductProcessState("Processing");

				ProductServiceProxy.getProductService().update(productInfo);

				ProductHistory HistoryData = new ProductHistory();
				HistoryData = ProductServiceProxy.getProductHistoryDataAdaptor().setHV(oldProductInfo, productInfo, HistoryData);
				ProductServiceProxy.getProductHistoryService().insert(HistoryData);
			}

			Lot newLot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
			Lot oldLot = (Lot) ObjectUtil.copyTo(newLot);

			newLot.setLastEventComment(eventInfo.getEventComment());
			newLot.setLastEventName(eventInfo.getEventName());
			newLot.setLastEventTime(eventInfo.getEventTime());
			newLot.setLastEventTimeKey(eventInfo.getEventTimeKey());
			newLot.setLastEventUser(eventInfo.getEventUser());
			newLot.setLotProcessState("RUN");

			LotServiceProxy.getLotService().update(newLot);

			LotHistory HistoryData = new LotHistory();
			HistoryData = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, newLot, HistoryData);

			LotServiceProxy.getLotHistoryService().insert(HistoryData);
		}
		catch (Exception e)
		{
			log.info("Error occurred - ChangeState");
		}
	}

	@SuppressWarnings("unchecked")
	private List<ListOrderedMap> getRecoveryLot(String lotName) throws CustomException
	{
		List<ListOrderedMap> productList;
		try
		{
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT * ");
			sql.append("  FROM (SELECT A.LOTSTATE, ");
			sql.append("               A.PROCESSFLOWNAME, ");
			sql.append("               A.LOTPROCESSSTATE, ");
			sql.append("               A.LOTHOLDSTATE, ");
			sql.append("               A.TIMEKEY, ");
			sql.append("               A.LOTGRADE, ");
			sql.append("               A.REWORKSTATE, ");
			sql.append("               A.REWORKCOUNT, ");
			sql.append("               A.RETURNFLOWNAME, ");
			sql.append("               A.RETURNOPERATIONNAME, ");
			sql.append("               A.NODESTACK, ");
			sql.append("               A.MACHINENAME, ");
			sql.append("               A.PORTNAME, ");
			sql.append("               A.PORTTYPE, ");
			sql.append("               A.PORTUSETYPE, ");
			sql.append("               MACHINERECIPENAME ");
			sql.append("          FROM LOTHISTORY A, ");
			sql.append("               (SELECT TIMEKEY ");
			sql.append("                  FROM LOTHISTORY ");
			sql.append("                 WHERE LOTNAME = :LOTNAME ");
			sql.append("                   AND EVENTNAME IN ('ScrapProduct', 'Scrap Product', 'Scrap') ");
			sql.append("                   AND ROWNUM = '1' ");
			sql.append("                ORDER BY TIMEKEY DESC) B ");
			sql.append("         WHERE 1 = 1 ");
			sql.append("           AND A.LOTNAME = :LOTNAME ");
			sql.append("           AND A.TIMEKEY < B.TIMEKEY ");
			sql.append("        ORDER BY A.TIMEKEY DESC) ");
			sql.append(" WHERE 1 = 1 ");
			sql.append("   AND ROWNUM <= 5 ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LOTNAME", lotName);

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

	private void updateProductData(Lot lotData, String productName, Product productData)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE PRODUCTHISTORY ");
		sql.append("   SET CARRIERNAME = :CARRIERNAME, MANUALSCRAPOPERMACHINE = :MANUALSCRAPOPERMACHINE ");
		sql.append(" WHERE PRODUCTNAME = :PRODUCTNAME ");
		sql.append("   AND TIMEKEY = :TIMEKEY ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("CARRIERNAME", lotData.getCarrierName());
		args.put("MANUALSCRAPOPERMACHINE", "");
		args.put("PRODUCTNAME", productName);
		args.put("TIMEKEY", productData.getLastEventTimeKey());

		GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), args);

		StringBuffer sql2 = new StringBuffer();
		sql2.append("UPDATE PRODUCT ");
		sql2.append("   SET CARRIERNAME = :CARRIERNAME, MANUALSCRAPOPERMACHINE = :MANUALSCRAPOPERMACHINE ");
		sql2.append(" WHERE PRODUCTNAME = :PRODUCTNAME ");

		GenericServiceProxy.getSqlMesTemplate().update(sql2.toString(), args);
	}
}