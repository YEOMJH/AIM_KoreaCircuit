package kr.co.aim.messolution.lot.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaterialProduct;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.SeparateInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductHistory;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductNSubProductPGQS;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class CuttingReport extends AsyncHandler {
	private static Log log = LogFactory.getLog(CuttingReport.class);
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		Element root = doc.getDocument().getRootElement();
		Element body = root.getChild("Body");

		// 1. Check Glass State(Released or Consumed)
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);

		// Validation
		CommonValidation.checkExistProductName(productName);

		// Get ProductData
		Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

		// validation.
		// CommonValidation.checkConsumedProduct(productData , productName);
		if (!productData.getProductState().equals(GenericServiceProxy.getConstantMap().Prod_InProduction))
		{
			throw new CustomException("PRODUCT-0018", productName);
		}

		// to find current Lot
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(productData.getLotName());
		CommonValidation.checkLotHoldState(lotData);

		if (lotData.getFactoryName().equals("OLED"))
		{
			//TP Offset
			kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
			setEventInfo.getUdfs().put("OFFSET", "");
			EventInfo eventInfoOffset = EventInfoUtil.makeEventInfo("SetOffset", getEventUser(), getEventComment(), "", "");
			MESProductServiceProxy.getProductServiceImpl().setEvent(productData, setEventInfo, eventInfoOffset);
			
			List<ProductNSubProductPGQS> productNSubProductPGQSSequence = MESLotServiceProxy.getLotServiceUtil().setProductNSubProductPGQSSequenceNew(body);

			double pQuantity = 1;

			// need to fix decision of Panel QTY
			SeparateInfo separateInfo = MESLotServiceProxy.getLotInfoUtil().separateInfo(lotData, productNSubProductPGQSSequence, pQuantity, productData);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("Cut", getEventUser(), getEventComment(), "", "");

			MESLotServiceProxy.getLotServiceImpl().CutLot(eventInfo, lotData, separateInfo);

			// Set LotName on ProductHistory for Consumed Product
			setLotNameOnProductHistory(productName, eventInfo.getEventTimeKey(), lotData.getKey().getLotName());
		}
		else if (lotData.getFactoryName().equals("POSTCELL"))
		{
			// Get Doc
			List<Element> subProductList = SMessageUtil.getBodySequenceItemList(doc, "SUBPRODUCTLIST", false);
			// String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String workTable = SMessageUtil.getBodyItemValue(doc, "WORKTABLENAME", false);
			ConstantMap constantMap = GenericServiceProxy.getConstantMap();

			// EventInfo
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("Cut", getEventUser(), getEventComment(), null, null);
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

			try
			{
				Durable workTableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(workTable);

				// Create MaterialProduct
				MaterialProduct dataInfo = new MaterialProduct();
				dataInfo.setTimeKey(eventInfo.getEventTimeKey());
				dataInfo.setProductName(productName);
				dataInfo.setLotName(productData.getLotName());
				dataInfo.setMaterialKind(GenericServiceProxy.getConstantMap().MaterialKind_Durable);
				dataInfo.setMaterialType(workTableData.getDurableType());
				dataInfo.setMaterialName(workTable);
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
			catch (Exception e)
			{
				eventLog.debug("Failed workTableData");
			}

			// ProductSpec Data
			Product productDataForSpec = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
			ProductSpec productSpec = MESProductServiceProxy.getProductServiceUtil().getProductSpecByProductName(productDataForSpec);

			// Panel x, y
			int xProductCount = Integer.parseInt(productSpec.getUdfs().get("PRODUCTCOUNTTOXAXIS"));
			int yProductCount = Integer.parseInt(productSpec.getUdfs().get("PRODUCTCOUNTTOYAXIS"))/2;//caixu 2021/04/20

			// SQL
			String queryStringLot = "INSERT INTO LOT (LOTNAME, PRODUCTREQUESTNAME, DUEDATE, PRODUCTIONTYPE, PRODUCTSPECNAME, PRODUCTSPECVERSION, ORIGINALLOTNAME, SOURCELOTNAME, ROOTLOTNAME, PARENTLOTNAME, PRODUCTTYPE, SUBPRODUCTUNITQUANTITY1, CREATEPRODUCTQUANTITY, CREATESUBPRODUCTQUANTITY , PRODUCTQUANTITY, SUBPRODUCTQUANTITY, FACTORYNAME, AREANAME, LOTSTATE, LOTPROCESSSTATE, LOTHOLDSTATE, LASTEVENTNAME, LASTEVENTTIMEKEY, LASTEVENTTIME, LASTEVENTUSER, LASTEVENTCOMMENT, LASTEVENTFLAG, CREATETIME, CREATEUSER, RELEASETIME, RELEASEUSER, LASTLOGGEDINTIME, LASTLOGGEDINUSER, PROCESSFLOWNAME, PROCESSFLOWVERSION, PROCESSOPERATIONNAME , PROCESSOPERATIONVERSION, MACHINENAME, REWORKSTATE, REWORKCOUNT, PORTNAME, PORTTYPE, PORTUSETYPE, BEFOREOPERATIONNAME, BEFOREOPERATIONVER, BEFOREFLOWNAME, LOTGRADE, NODESTACK, PRIORITY, ARRAYLOTNAME) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			String productQuery = " UPDATE PRODUCT SET LOTNAME = NULL, CARRIERNAME = NULL, DESTINATIONPRODUCTNAME = ?, MACHINENAME = ?, PRODUCTHOLDSTATE = NULL, "
					+ "PRODUCTSTATE = ?, PRODUCTPROCESSSTATE = ?, LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, LASTEVENTFLAG = ? WHERE PRODUCTNAME = ?";

			// Make Panel
			String destinationProductName = "";
			List<Object[]> updateLotArgList = new ArrayList<Object[]>();
			List<Lot> lotHistoryArgList = new ArrayList<Lot>();
			List<Lot> oldLotListHistory = new ArrayList<Lot>();

			List<Object[]> updateArgList = new ArrayList<Object[]>();
			List<ProductHistory> updateProductHistoryList = new ArrayList<ProductHistory>();

			for (Element subProduct : subProductList)
			{
				// New Panel
				List<Object> lotBindList = new ArrayList<Object>();

				if (StringUtils.isEmpty(destinationProductName))
				{
					destinationProductName = subProduct.getChildText("SUBPRODUCTNAME");
				}
				//caixu 2020/1/7 CheckPanelIDlength
				if(subProduct.getChildText("SUBPRODUCTNAME").length()<14)
				{
				
				 throw new CustomException("PRODUCT-9012", productName);
					
				}

				lotBindList.add(subProduct.getChildText("SUBPRODUCTNAME"));
				lotBindList.add(lotData.getProductRequestName());
				lotBindList.add(lotData.getDueDate());
				lotBindList.add(lotData.getProductionType());
				lotBindList.add(lotData.getProductSpecName());
				lotBindList.add(lotData.getProductSpecVersion());
				lotBindList.add(lotData.getOriginalLotName());
				lotBindList.add(productData.getLotName());
				lotBindList.add(productData.getKey().getProductName());
				lotBindList.add(productData.getLotName());
				lotBindList.add(constantMap.ProductType_Panel);
				lotBindList.add(0);
				lotBindList.add(1);
				lotBindList.add(0);
				lotBindList.add(1);
				lotBindList.add(0);
				lotBindList.add(lotData.getFactoryName());
				lotBindList.add(lotData.getAreaName());
				lotBindList.add(constantMap.Lot_Released);
				lotBindList.add(constantMap.Lot_Run);
				lotBindList.add(constantMap.Flag_N);
				lotBindList.add(eventInfo.getEventName());
				lotBindList.add(eventInfo.getEventTimeKey());
				lotBindList.add(eventInfo.getEventTime());
				lotBindList.add(eventInfo.getEventUser());
				lotBindList.add(eventInfo.getEventComment());
				lotBindList.add(constantMap.Flag_N);
				lotBindList.add(eventInfo.getEventTime());
				lotBindList.add(eventInfo.getEventUser());
				lotBindList.add(eventInfo.getEventTime());
				lotBindList.add(eventInfo.getEventUser());
				lotBindList.add(eventInfo.getEventTime());
				lotBindList.add(eventInfo.getEventUser());
				lotBindList.add(lotData.getProcessFlowName());
				lotBindList.add(lotData.getProcessFlowVersion());
				lotBindList.add(lotData.getProcessOperationName());
				lotBindList.add(lotData.getProcessOperationVersion());
				lotBindList.add(machineName);
				lotBindList.add(constantMap.Lot_NotInRework);
				lotBindList.add(0);
				lotBindList.add(lotData.getUdfs().get("PORTNAME"));
				lotBindList.add(lotData.getUdfs().get("PORTTYPE"));
				lotBindList.add(lotData.getUdfs().get("PORTUSETYPE"));
				lotBindList.add(lotData.getUdfs().get("BEFOREOPERATIONNAME"));
				lotBindList.add(lotData.getUdfs().get("BEFOREOPERATIONVER"));
				lotBindList.add(lotData.getUdfs().get("BEFOREFLOWNAME"));
				lotBindList.add(subProduct.getChildText("PRODUCTJUDGE"));
				lotBindList.add(lotData.getNodeStack());
				lotBindList.add(productData.getPriority());
				lotBindList.add(productData.getUdfs().get("ARRAYLOTNAME"));

				updateLotArgList.add(lotBindList.toArray());

				// History
				Lot newPanel = new Lot();

				newPanel.setKey(new LotKey(subProduct.getChildText("SUBPRODUCTNAME")));
				newPanel.setProductionType(lotData.getProductionType());
				newPanel.setProductSpecName(lotData.getProductSpecName());
				newPanel.setProductSpecVersion(lotData.getProductSpecVersion());
				newPanel.setOriginalLotName(lotData.getOriginalLotName());
				newPanel.setSourceLotName(lotData.getKey().getLotName());
				newPanel.setParentLotName(lotData.getKey().getLotName());
				newPanel.setRootLotName(productData.getKey().getProductName());
				newPanel.setProductType(constantMap.ProductType_Panel);
				newPanel.setSubProductUnitQuantity1(0);
				newPanel.setCreateProductQuantity(1);
				newPanel.setCreateSubProductQuantity(0);
				newPanel.setProductQuantity(1);
				newPanel.setSubProductQuantity(0);
				newPanel.setFactoryName(lotData.getFactoryName());
				newPanel.setAreaName(lotData.getAreaName());
				newPanel.setLotState(constantMap.Lot_Released);
				newPanel.setLotProcessState(constantMap.Lot_Run);
				newPanel.setLotHoldState(constantMap.Flag_N);
				newPanel.setLastLoggedInTime(eventInfo.getEventTime());
				newPanel.setLastLoggedInUser(eventInfo.getEventUser());
				newPanel.setProcessFlowName(lotData.getProcessFlowName());
				newPanel.setProcessFlowVersion(lotData.getProcessFlowVersion());
				newPanel.setProcessOperationName(lotData.getProcessOperationName());
				newPanel.setProcessOperationVersion(lotData.getProcessOperationVersion());
				newPanel.setMachineName(machineName);
				newPanel.setReworkState(constantMap.Lot_NotInRework);
				newPanel.setReworkCount(0);
				newPanel.setLotGrade(subProduct.getChildText("PRODUCTJUDGE"));
				newPanel.setNodeStack(lotData.getNodeStack());
				newPanel.setProductRequestName(lotData.getProductRequestName());
				newPanel.setDueDate(lotData.getDueDate());
				newPanel.setPriority(productData.getPriority());

				Map<String, String> lotUdfs = new HashMap<>();
				lotUdfs.put("PORTNAME", lotData.getUdfs().get("PORTNAME"));
				lotUdfs.put("PORTTYPE", lotData.getUdfs().get("PORTTYPE"));
				lotUdfs.put("PORTUSETYPE", lotData.getUdfs().get("PORTUSETYPE"));
				lotUdfs.put("BEFOREOPERATIONNAME", lotData.getUdfs().get("BEFOREOPERATIONNAME"));
				lotUdfs.put("BEFOREOPERATIONVER", lotData.getUdfs().get("BEFOREOPERATIONVER"));
				lotUdfs.put("BEFOREFLOWNAME", lotData.getUdfs().get("BEFOREFLOWNAME"));
				lotUdfs.put("ARRAYLOTNAME", productData.getUdfs().get("ARRAYLOTNAME"));
				newPanel.setUdfs(lotUdfs);

				lotHistoryArgList.add(newPanel);
			}
			
			Product oldProductData = (Product)ObjectUtil.copyTo(productData);
			
			List<Object> bindList = new ArrayList<Object>();
			bindList.add(destinationProductName);
			bindList.add(machineName);
			bindList.add(constantMap.Prod_Consumed);
			bindList.add("");
			bindList.add(eventInfo.getEventName());
			bindList.add(eventInfo.getEventTimeKey());
			bindList.add(eventInfo.getEventTime());
			bindList.add(eventInfo.getEventUser());
			bindList.add(eventInfo.getEventComment());
			bindList.add(constantMap.Flag_N);
			bindList.add(productData.getKey().getProductName());

			updateArgList.add(bindList.toArray());

			productData.setProductState(constantMap.Prod_Consumed);
			productData.setProductProcessState("");
			productData.setProductHoldState("");
			productData.setDestinationProductName(destinationProductName);
			productData.setCarrierName("");
			productData.setMachineName(machineName);
			productData.setLastEventName(eventInfo.getEventName());
			productData.setLastEventTime(eventInfo.getEventTime());
			productData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			productData.setLastEventComment(eventInfo.getEventComment());
			productData.setLastEventUser(eventInfo.getEventUser());

			ProductHistory productHistory = new ProductHistory();
			productHistory = ProductServiceProxy.getProductHistoryDataAdaptor().setHV(oldProductData, productData, productHistory);
			updateProductHistoryList.add(productHistory);
			
			MESLotServiceProxy.getLotServiceUtil().updateBatch(queryStringLot, updateLotArgList);
			MESLotServiceProxy.getLotServiceUtil().insertLotHistory(eventInfo, lotHistoryArgList, oldLotListHistory, "", "", "", "", "");
			MESLotServiceProxy.getLotServiceUtil().updateBatch(productQuery, updateArgList);
			try 
			{
				CommonUtil.executeBatch("insert", updateProductHistoryList);
			} 
			catch (Exception e) 
			{
				log.error(e.getMessage());
				throw new CustomException(e.getCause());
			}

			// Complete Lot
			String completeLotSQL = "";

			List<Object[]> updateCompleteLotArgList = new ArrayList<Object[]>();
			List<Lot> completeLotHistoryArgList = new ArrayList<Lot>();
			List<Lot> oldCompleteLotListHistory = new ArrayList<Lot>();

			oldCompleteLotListHistory.add(lotData);
			List<Object> sourcelotBindList = new ArrayList<Object>();

			Lot completeLot = lotData;
			Lot lotDataAfterCut = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());

			if (lotDataAfterCut.getProductQuantity() == 1)
			{
				completeLotSQL = "UPDATE LOT SET LOTSTATE = ?, LOTPROCESSSTATE = ?, PRODUCTQUANTITY = ?, SUBPRODUCTQUANTITY = ?, LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, LASTEVENTFLAG = ? WHERE LOTNAME = ?";
				sourcelotBindList.add(constantMap.Lot_Emptied);
				sourcelotBindList.add("");
				sourcelotBindList.add(0);
				sourcelotBindList.add(0);
				sourcelotBindList.add(eventInfo.getEventName());
				sourcelotBindList.add(eventInfo.getEventTimeKey());
				sourcelotBindList.add(eventInfo.getEventTime());
				sourcelotBindList.add(eventInfo.getEventUser());
				sourcelotBindList.add(eventInfo.getEventComment());
				sourcelotBindList.add(constantMap.Flag_N);
				sourcelotBindList.add(lotData.getKey().getLotName());

				completeLot.setKey(new LotKey(lotData.getKey().getLotName()));
				completeLot.setLotState(constantMap.Lot_Emptied);
				completeLot.setLotProcessState("");
				completeLot.setProductQuantity(0);
				completeLot.setSubProductQuantity(0);
			}
			else
			{
				completeLotSQL = "UPDATE LOT SET PRODUCTQUANTITY = ?, SUBPRODUCTQUANTITY = ?, LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, LASTEVENTFLAG = ? WHERE LOTNAME = ?";
				sourcelotBindList.add(lotData.getProductQuantity() - 1);
				double newSubProductQuantity=lotData.getSubProductQuantity() - (xProductCount * yProductCount);
				if(newSubProductQuantity<0){
					sourcelotBindList.add(0);	
				}else{
					sourcelotBindList.add(newSubProductQuantity);
				}
				sourcelotBindList.add(eventInfo.getEventName());
				sourcelotBindList.add(eventInfo.getEventTimeKey());
				sourcelotBindList.add(eventInfo.getEventTime());
				sourcelotBindList.add(eventInfo.getEventUser());
				sourcelotBindList.add(eventInfo.getEventComment());
				sourcelotBindList.add(constantMap.Flag_N);
				sourcelotBindList.add(lotData.getKey().getLotName());

				completeLot.setKey(new LotKey(lotData.getKey().getLotName()));
				completeLot.setProductQuantity(lotData.getProductQuantity() - 1);
				if(newSubProductQuantity<0){
					completeLot.setSubProductQuantity(0);	
				}else{
					completeLot.setSubProductQuantity(newSubProductQuantity);
				}
			}

			updateCompleteLotArgList.add(sourcelotBindList.toArray());
			completeLotHistoryArgList.add(completeLot);

			MESLotServiceProxy.getLotServiceUtil().updateBatch(completeLotSQL, updateCompleteLotArgList);
			MESLotServiceProxy.getLotServiceUtil().insertLotHistory(eventInfo, completeLotHistoryArgList, oldCompleteLotListHistory, "", "", "", "", "");

			if (!StringUtils.isEmpty(lotDataAfterCut.getCarrierName()))
			{
				if (lotDataAfterCut.getProductQuantity() == 1)
				{
					Lot deassignLotData = LotServiceProxy.getLotService().selectByKey(lotData.getKey());

					DurableKey durableKey = new DurableKey();
					durableKey.setDurableName(deassignLotData.getCarrierName());

					Durable durableData = DurableServiceProxy.getDurableService().selectByKey(durableKey);

					List<ProductU> productUSequence = new ArrayList<ProductU>();

					DeassignCarrierInfo createInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, durableData, productUSequence);
					eventInfo.setEventName("Deassign");

					MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, createInfo, eventInfo);
				}
			}
		}
	}

	private void setLotNameOnProductHistory(String productName, String timeKey, String lotName)
	{
		String sql = "UPDATE PRODUCTHISTORY SET LOTNAME = :LOTNAME WHERE PRODUCTNAME = :PRODUCTNAME AND TIMEKEY = :TIMEKEY AND PRODUCTSTATE = 'Consumed' ";

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("LOTNAME", lotName);
		args.put("PRODUCTNAME", productName);
		args.put("TIMEKEY", timeKey);

		try
		{
			int count = GenericServiceProxy.getSqlMesTemplate().update(sql, args);
			eventLog.debug(count + " Rows updated to set LotName: " + lotName);
		}
		catch (Exception e)
		{
			eventLog.debug("Failed to update set LotName: " + lotName);
		}
	}
}