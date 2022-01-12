package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.lot.event.CuttingReport;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductHistory;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class CellCutCutting extends SyncHandler {

	private static Log log = LogFactory.getLog(CellCutCutting.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		// Get Doc
		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		// EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Cut", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

		// Original Lot
		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
		double originalProductQty = lotData.getProductQuantity();

		// ProductSpec Data
		String productName = productList.get(0).getChildText("PRODUCTNAME");
		Product productDataForSpec = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
		ProductSpec productSpec = MESProductServiceProxy.getProductServiceUtil().getProductSpecByProductName(productDataForSpec);

		// Panel x, y
		int xProductCount = Integer.parseInt(productSpec.getUdfs().get("PRODUCTCOUNTTOXAXIS"));
		int yProductCount = Integer.parseInt(productSpec.getUdfs().get("PRODUCTCOUNTTOYAXIS"))/2;//caixu 2020/11/27 根据V3修改yProductCount获取规则

		// set for count
		int xCnt, yCnt;

		if (xProductCount > 14)
			xCnt = 65 + xProductCount + 2;
		else if (xProductCount > 8)
			xCnt = 65 + xProductCount + 1;
		else
			xCnt = 65 + xProductCount;

		if (yProductCount > 14)
			yCnt = 65 + yProductCount + 2;
		else if (yProductCount > 8)
			yCnt = 65 + yProductCount + 1;
		else
			yCnt = 65 + yProductCount;

		// Make Panel
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<Lot> lotHistoryArgList = new ArrayList<Lot>();
		List<Lot> oldLotListHistory = new ArrayList<Lot>();
		List<ProductHistory> updateProductHistoryList = new ArrayList<ProductHistory>();

		List<Object[]> updateArgList = new ArrayList<Object[]>();

		int productQty = productList.size();

		for (Element prdData : productList)
		{
			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(prdData.getChildText("PRODUCTNAME"));
			Product oldProductData = (Product)ObjectUtil.copyTo(productData);
			
			String panelGrade = productData.getSubProductGrades1();
			String destinationProductName = "";

			int panelGradeIndex = 0;
			for (int i = 65; i < yCnt; i++)
			{
				if (i == 73 || i == 79)
					continue;

				for (int j = 65; j < xCnt; j++)
				{
					if (j == 73 || j == 79)
						continue;

					// New Panel
					List<Object> lotBindList = new ArrayList<Object>();

					String newPanelName = productData.getKey().getProductName() + (char) i + (char) j + "0";

					if (StringUtils.isEmpty(destinationProductName))
						destinationProductName = newPanelName;

					lotBindList.add(newPanelName);
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

					if (StringUtils.isEmpty(lotData.getMachineName()))
						lotBindList.add(lotData.getUdfs().get("MAINMACHINENAME").toString());
					else
						lotBindList.add(lotData.getMachineName());

					lotBindList.add(constantMap.Lot_NotInRework);
					lotBindList.add(0);
					lotBindList.add(lotData.getUdfs().get("PORTNAME"));
					lotBindList.add(lotData.getUdfs().get("PORTTYPE"));
					lotBindList.add(lotData.getUdfs().get("PORTUSETYPE"));
					lotBindList.add(lotData.getUdfs().get("BEFOREOPERATIONNAME"));
					lotBindList.add(lotData.getUdfs().get("BEFOREOPERATIONVER"));
					lotBindList.add(lotData.getUdfs().get("BEFOREFLOWNAME"));
					lotBindList.add(Character.toString(panelGrade.charAt(panelGradeIndex)));
					lotBindList.add(lotData.getNodeStack());
					lotBindList.add(productData.getPriority());
					lotBindList.add(productData.getUdfs().get("ARRAYLOTNAME"));

					updateLotArgList.add(lotBindList.toArray());

					// History
					Lot newPanel = new Lot();
					newPanel.setKey(new LotKey(newPanelName));
					newPanel.setProductionType(lotData.getProductionType());
					newPanel.setProductSpecName(lotData.getProductSpecName());
					newPanel.setProductSpecVersion(lotData.getProductSpecVersion());
					newPanel.setOriginalLotName(lotData.getOriginalLotName());
					newPanel.setSourceLotName(lotName);
					newPanel.setParentLotName(lotName);
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

					if (StringUtils.isEmpty(lotData.getMachineName()))
						newPanel.setMachineName(lotData.getUdfs().get("MAINMACHINENAME").toString());
					else
						newPanel.setMachineName(lotData.getMachineName());

					newPanel.setReworkState(constantMap.Lot_NotInRework);
					newPanel.setReworkCount(0);
					newPanel.setLotGrade(Character.toString(panelGrade.charAt(panelGradeIndex++)));
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
			}

			List<Object> bindList = new ArrayList<Object>();
			bindList.add(destinationProductName);
			bindList.add(lotData.getMachineName());
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
			productData.setMachineName(lotData.getMachineName());
			productData.setLastEventName(eventInfo.getEventName());
			productData.setLastEventTime(eventInfo.getEventTime());
			productData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			productData.setLastEventComment(eventInfo.getEventComment());
			productData.setLastEventUser(eventInfo.getEventUser());

			ProductHistory productHistory = new ProductHistory();
			productHistory = ProductServiceProxy.getProductHistoryDataAdaptor().setHV(oldProductData, productData, productHistory);
			updateProductHistoryList.add(productHistory);
		}

		insertPanelData(eventInfo, updateLotArgList, lotHistoryArgList, oldLotListHistory);
		updateProductData(eventInfo, updateArgList, updateProductHistoryList);
		updateLotData(eventInfo, lotData, productQty, originalProductQty, xProductCount, yProductCount);

		return doc;
	}

	private void insertPanelData(EventInfo eventInfo, List<Object[]> updateLotArgList, List<Lot> lotHistoryArgList, List<Lot> oldLotListHistory) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("INSERT INTO LOT  ");
		sql.append("(LOTNAME, PRODUCTREQUESTNAME, DUEDATE, PRODUCTIONTYPE, PRODUCTSPECNAME, ");
		sql.append(" PRODUCTSPECVERSION, ORIGINALLOTNAME, SOURCELOTNAME, ROOTLOTNAME, PARENTLOTNAME, ");
		sql.append(" PRODUCTTYPE, SUBPRODUCTUNITQUANTITY1, CREATEPRODUCTQUANTITY, CREATESUBPRODUCTQUANTITY, PRODUCTQUANTITY, ");
		sql.append(" SUBPRODUCTQUANTITY, FACTORYNAME, AREANAME, LOTSTATE, LOTPROCESSSTATE, ");
		sql.append(" LOTHOLDSTATE, LASTEVENTNAME, LASTEVENTTIMEKEY, LASTEVENTTIME, LASTEVENTUSER, ");
		sql.append(" LASTEVENTCOMMENT, LASTEVENTFLAG, CREATETIME, CREATEUSER, RELEASETIME, ");
		sql.append(" RELEASEUSER, LASTLOGGEDINTIME, LASTLOGGEDINUSER, PROCESSFLOWNAME, PROCESSFLOWVERSION, ");
		sql.append(" PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION, MACHINENAME, REWORKSTATE, REWORKCOUNT, ");
		sql.append(" PORTNAME, PORTTYPE, PORTUSETYPE, BEFOREOPERATIONNAME, BEFOREOPERATIONVER, ");
		sql.append(" BEFOREFLOWNAME, LOTGRADE, NODESTACK, PRIORITY, ARRAYLOTNAME) ");
		sql.append("VALUES  ");
		sql.append("(?,?,?,?,?, ");
		sql.append(" ?,?,?,?,?, ");
		sql.append(" ?,?,?,?,?, ");
		sql.append(" ?,?,?,?,?, ");
		sql.append(" ?,?,?,?,?, ");
		sql.append(" ?,?,?,?,?, ");
		sql.append(" ?,?,?,?,?, ");
		sql.append(" ?,?,?,?,?, ");
		sql.append(" ?,?,?,?,?, ");
		sql.append(" ?,?,?,?,?) ");

		MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), updateLotArgList);
		MESLotServiceProxy.getLotServiceUtil().insertLotHistory(eventInfo, lotHistoryArgList, oldLotListHistory, "", "", "", "", "");
	}

	private void updateProductData(EventInfo eventInfo, List<Object[]> updateArgList, List<ProductHistory> updateProductHistoryList) throws CustomException
	{
		StringBuffer productQuery = new StringBuffer();
		productQuery.append("UPDATE PRODUCT ");
		productQuery.append("   SET LOTNAME = NULL, ");
		productQuery.append("       CARRIERNAME = NULL, ");
		productQuery.append("       DESTINATIONPRODUCTNAME = ?, ");
		productQuery.append("       MACHINENAME = ?, ");
		productQuery.append("       PRODUCTHOLDSTATE = NULL, ");
		productQuery.append("       PRODUCTSTATE = ?, ");
		productQuery.append("       PRODUCTPROCESSSTATE = ?, ");
		productQuery.append("       LASTEVENTNAME = ?, ");
		productQuery.append("       LASTEVENTTIMEKEY = ?, ");
		productQuery.append("       LASTEVENTTIME = ?, ");
		productQuery.append("       LASTEVENTUSER = ?, ");
		productQuery.append("       LASTEVENTCOMMENT = ?, ");
		productQuery.append("       LASTEVENTFLAG = ? ");
		productQuery.append(" WHERE PRODUCTNAME = ? ");

		MESLotServiceProxy.getLotServiceUtil().updateBatch(productQuery.toString(), updateArgList);
		try 
		{
			CommonUtil.executeBatch("insert", updateProductHistoryList);
		} 
		catch (Exception e) 
		{
			log.error(e.getMessage());
			throw new CustomException(e.getCause());
		}
	}

	private void updateLotData(EventInfo eventInfo, Lot lotData, int productQty, double originalProductQty, int xProductCount, int yProductCount) throws CustomException
	{

		// Complete Lot
		StringBuffer sql = new StringBuffer();

		List<Object[]> updateCompleteLotArgList = new ArrayList<Object[]>();
		List<Lot> completeLotHistoryArgList = new ArrayList<Lot>();
		List<Lot> oldCompleteLotListHistory = new ArrayList<Lot>();

		oldCompleteLotListHistory.add(lotData);
		List<Object> sourcelotBindList = new ArrayList<Object>();

		Lot completeLot = lotData;

		if (originalProductQty == productQty)
		{
			sql.append("UPDATE LOT ");
			sql.append("   SET LOTSTATE = ?, ");
			sql.append("       LOTPROCESSSTATE = ?, ");
			sql.append("       PRODUCTQUANTITY = ?, ");
			sql.append("       SUBPRODUCTQUANTITY = ?, ");
			sql.append("       LASTEVENTNAME = ?, ");
			sql.append("       LASTEVENTTIMEKEY = ?, ");
			sql.append("       LASTEVENTTIME = ?, ");
			sql.append("       LASTEVENTUSER = ?, ");
			sql.append("       LASTEVENTCOMMENT = ?, ");
			sql.append("       LASTEVENTFLAG = ? ");
			sql.append(" WHERE LOTNAME = ? ");

			sourcelotBindList.add(GenericServiceProxy.getConstantMap().Lot_Emptied);
			sourcelotBindList.add("");
			sourcelotBindList.add(0);
			sourcelotBindList.add(0);
			sourcelotBindList.add(eventInfo.getEventName());
			sourcelotBindList.add(eventInfo.getEventTimeKey());
			sourcelotBindList.add(eventInfo.getEventTime());
			sourcelotBindList.add(eventInfo.getEventUser());
			sourcelotBindList.add(eventInfo.getEventComment());
			sourcelotBindList.add(GenericServiceProxy.getConstantMap().Flag_N);
			sourcelotBindList.add(lotData.getKey().getLotName());

			completeLot.setKey(new LotKey(lotData.getKey().getLotName()));
			completeLot.setLotState(GenericServiceProxy.getConstantMap().Lot_Emptied);
			completeLot.setLotProcessState("");
			completeLot.setProductQuantity(0);
			completeLot.setSubProductQuantity(0);
		}
		else
		{
			sql.append("UPDATE LOT ");
			sql.append("   SET PRODUCTQUANTITY = ?, ");
			sql.append("       SUBPRODUCTQUANTITY = ?, ");
			sql.append("       LASTEVENTNAME = ?, ");
			sql.append("       LASTEVENTTIMEKEY = ?, ");
			sql.append("       LASTEVENTTIME = ?, ");
			sql.append("       LASTEVENTUSER = ?, ");
			sql.append("       LASTEVENTCOMMENT = ?, ");
			sql.append("       LASTEVENTFLAG = ? ");
			sql.append(" WHERE LOTNAME = ? ");

			sourcelotBindList.add(lotData.getProductQuantity() - productQty);
			sourcelotBindList.add(lotData.getSubProductQuantity() - (productQty * xProductCount * yProductCount));
			sourcelotBindList.add(eventInfo.getEventName());
			sourcelotBindList.add(eventInfo.getEventTimeKey());
			sourcelotBindList.add(eventInfo.getEventTime());
			sourcelotBindList.add(eventInfo.getEventUser());
			sourcelotBindList.add(eventInfo.getEventComment());
			sourcelotBindList.add(GenericServiceProxy.getConstantMap().Flag_N);
			sourcelotBindList.add(lotData.getKey().getLotName());

			completeLot.setKey(new LotKey(lotData.getKey().getLotName()));
			completeLot.setProductQuantity(lotData.getProductQuantity() - productQty);
			completeLot.setSubProductQuantity(lotData.getSubProductQuantity() - (productQty * xProductCount * yProductCount));
		}

		updateCompleteLotArgList.add(sourcelotBindList.toArray());
		completeLotHistoryArgList.add(completeLot);

		MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), updateCompleteLotArgList);
		MESLotServiceProxy.getLotServiceUtil().insertLotHistory(eventInfo, completeLotHistoryArgList, oldCompleteLotListHistory, "", "", "", "", "");

		if (originalProductQty == productQty)
		{
			Lot deassignLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getKey().getLotName());

			if (StringUtils.isNotEmpty(deassignLotData.getCarrierName()))
			{
				if (deassignLotData.getCarrierName().isEmpty())
					throw new CustomException("CST-0001", deassignLotData.getCarrierName());

				Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(deassignLotData.getCarrierName());

				DeassignCarrierInfo createInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, durableData, new ArrayList<ProductU>());
				eventInfo.setEventName("Deassign");

				MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, createInfo, eventInfo);
			}

			MESLotServiceProxy.getLotServiceImpl().updateLotData("LOTSTATE", GenericServiceProxy.getConstantMap().Lot_Emptied, lotData.getKey().getLotName());
		}
	}
}
