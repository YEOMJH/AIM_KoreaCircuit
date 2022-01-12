package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCPlan;
import kr.co.aim.messolution.extended.object.management.data.ProductQueueTime;
import kr.co.aim.messolution.extended.object.management.impl.LotQueueTimeService;
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
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.MakeScrappedInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class ScrapLot extends SyncHandler {

	public static Log logger = LogFactory.getLog(LotQueueTimeService.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment(), "", "");
		Element eleBody = SMessageUtil.getBodyElement(doc);

		String sLotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String sReasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", true);
		String sOperationName = SMessageUtil.getBodyItemValue(doc, "STEPNO", false);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String sUnitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String sSubUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
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

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(sLotName);
		
		CommonValidation.checkLotProcessState(lotData);
		CommonValidation.checkJobDownFlag(lotData);
		CommonValidation.checkLotHoldState(lotData);

		LotKey lotKey = lotData.getKey();
		List<ProductU> productUSequence = new ArrayList<ProductU>();
		productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllUnScrapProductUSequence(lotData);

		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		List<String> productNameList = CommonUtil.makeList(SMessageUtil.getBodyElement(doc), "PRODUCTLIST", "PRODUCTNAME");

		Element e = productList.get(0);
		String productName = e.getChild("PRODUCTNAME").getText();
		Product productdata = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);

		String sLotProcessState = lotData.getLotProcessState();
		String sProductProcessState = productdata.getProductProcessState();

		// 1.SourceLot Deassign With SourceCST
		if (StringUtil.isNotEmpty(lotData.getCarrierName()))
		{
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());

			DeassignCarrierInfo createInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, durableData, productUSequence);

			eventInfo = EventInfoUtil.makeEventInfo("Deassign", getEventUser(), getEventComment(), "", "");
			MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, createInfo, eventInfo);
		}

		ProductSpec baseData = GenericServiceProxy.getSpecUtil().getProductSpec(lotData.getFactoryName(), lotData.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);

		if (CommonUtil.equalsIn(baseData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare"))
		{
			eventLog.info(" Scraped Lot [" + lotData + "]  is sent to the Bank for MQC Job.");
			// EventInfo eventInfo , MQCPlan planData, Lot lotData
			MESLotServiceProxy.getLotServiceUtil().changeReturnMQCBank(eventInfo, lotData);

			// After being sent to the MQC bank, Delete MQC Job
			eventLog.info("After being sent to the MQC bank, Delete MQC Job. Scraped Lot : [" + lotData + "]");
			MESLotServiceProxy.getLotServiceUtil().removeMQCJob(eventInfo, lotData);
		}

		// delete MQCJob
		if (lotData.getProductionType().equals("M") || lotData.getProductionType().equals("D"))
			deleteMQCJob(sLotName);

		// 2.Scrap
		eventInfo = EventInfoUtil.makeEventInfo("Scrap", this.getEventUser(), this.getEventComment(), "", "");//caixu 2020/6/1 ScrapLotModifyScrap
		lotData.setLotGrade("S");
		MESLotServiceProxy.getLotServiceImpl().updateLotData("LOTGRADE", "S", sLotName);
		double productQuantity = lotData.getProductQuantity();

		if (eleBody != null)
		{
			productUSequence = new ArrayList<ProductU>();

			for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false))
			{
				String sProductName = SMessageUtil.getChildText(eledur, "PRODUCTNAME", false);

				Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(sProductName);

				if (StringUtil.equals(productData.getProductState(), "Scrapped"))
					throw new CustomException("PRODUCT-0005", sProductName);

				productData.setProductGrade("S");
				productData.getUdfs().put("MANUALSCRAPOPERMACHINE", manualScrap);
				ProductServiceProxy.getProductService().update(productData);

				if (lotData.getFactoryName().equals("ARRAY") || lotData.getFactoryName().equals("OLED") || lotData.getFactoryName().equals("TP"))
				{
					String reasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", true);
					eventInfo.setReasonCode(sReasonCode);
					eventInfo.setReasonCodeType(SMessageUtil.getBodyItemValue(doc, "RESPONSEFACTORY", true)+"-"+SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", true) + " - " + reasonCodeType);
				}

				ProductU productU = new ProductU();
				productU.setProductName(sProductName);
				productUSequence.add(productU);
			}

			MakeScrappedInfo makeLotScrappedInfo = MESLotServiceProxy.getLotInfoUtil().makeScrappedInfo(lotData, productQuantity, productUSequence);
			LotServiceProxy.getLotService().makeScrapped(lotKey, eventInfo, makeLotScrappedInfo);
			deleteScrapProduct(productUSequence);//caixu 2020/10/12 delete CT_OASCRAPPRODUCTNAME
		}

		// OLED work order quantity distinguishes sheet and Glass
		if (lotData.getFactoryName().equals("ARRAY") || lotData.getFactoryName().equals("TP"))
			MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfo, lotData.getProductRequestName(), productList.size(), 0);

		if (lotData.getFactoryName().equals("OLED"))
		{
			if (lotData.getProductType().equals("Sheet"))
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfo, lotData.getProductRequestName(), (productList.size()) * 2, 0);
			else
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfo, lotData.getProductRequestName(), productList.size(), 0);
		}
		
        //modify by wangys 2020/11/26 Cancel Auto CompleteWO 
		/*ProductRequest newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());

		if (newProductRequestData.getPlanQuantity() == newProductRequestData.getFinishedQuantity() + newProductRequestData.getScrappedQuantity())
		{
			EventInfo newEventInfo = eventInfo;
			newEventInfo.setEventName("Completed");
			newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompleted(newEventInfo, lotData.getProductRequestName());
		}*/

		// Delete Q-TIME
		/*
		if (lotData.getFactoryName().equals("ARRAY") || lotData.getFactoryName().equals("OLED") || lotData.getFactoryName().equals("TP"))
			exitQTimeByLot(eventInfo, sLotName);
		*/

		if (lotData.getFactoryName().equals("ARRAY"))
		{
			if (productUSequence.size() > 0)
			{
				for (ProductU productU : productUSequence)
				{
					List<Map<String, Object>> sqlForSelectGlassResult = MESLotServiceProxy.getLotServiceUtil().getGlassJudgeData(productU.getProductName());

					for (Map<String, Object> result : sqlForSelectGlassResult)
					{
						MESLotServiceProxy.getLotServiceUtil().updateGlassJudgeScrapFlag(eventInfo, result, "Y");
					}
				}
			}
		}

		updateProductProcessState(sLotName, productList, sLotProcessState, sProductProcessState);
		ExtendedObjectProxy.getSampleProductService().deleteSampleProductList(eventInfo, productNameList);

		return doc;
	}

	private void deleteScrapProduct(List<ProductU> productUSequence) throws CustomException {
		// TODO Auto-generated method stub
		String queryStrigScrapProduct="DELETE FROM CT_OASCRAPPRODUCTNAME WHERE PRODUCTNAME=:PRODUCTNAME";
		List<Object[]> deleteScrapListProduct= new ArrayList<Object[]>();
		for(ProductU productData : productUSequence)
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

	// DeleteMQCJob
	private void deleteMQCJob(String sLotName) throws CustomException
	{
		List<MQCPlan> mqcPlanDataList = ExtendedObjectProxy.getMQCPlanService().getMQCPlanDataByLotName(sLotName);

		if (mqcPlanDataList != null)
		{
			for (MQCPlan jobList : mqcPlanDataList)
			{
				String jobName = jobList.getJobName();

				MQCPlan planData = ExtendedObjectProxy.getMQCPlanService().selectByKey(true, new Object[] { jobName });

				// MQC validation
				if (!planData.getMQCState().equalsIgnoreCase("Suspending") && !planData.getMQCState().equalsIgnoreCase("Created"))
				{
					//MQC-0011: MQC plan must be on hold
					throw new CustomException("MQC-0011");
				}

				Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(sLotName);

				// Lot validation
				if (!lotData.getLotState().equals(GenericServiceProxy.getConstantMap().Lot_Released))
					throw new CustomException("LOT-0016", lotData.getKey().getLotName(), lotData.getLotState());

				if (!lotData.getLotProcessState().equals(GenericServiceProxy.getConstantMap().Lot_WaitingToLogin))
					throw new CustomException("LOT-0016", lotData.getKey().getLotName(), lotData.getLotProcessState());

				if (!lotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().Flag_N))
					throw new CustomException("LOT-0016", lotData.getKey().getLotName(), lotData.getLotHoldState());

				if (!lotData.getReworkState().equals(GenericServiceProxy.getConstantMap().Lot_NotInRework))
					throw new CustomException("LOT-0016", lotData.getKey().getLotName(), lotData.getReworkState());

				EventInfo eventInfo = EventInfoUtil.makeEventInfo("Remove", getEventUser(), getEventComment(), null, null);

				ExtendedObjectProxy.getMQCPlanDetailService().deleteMQCPlanDetailByJobName(eventInfo, jobName);

				ExtendedObjectProxy.getMQCPlanService().deleteMQCPlanData(eventInfo, planData);
			}
		}
	}

	private void updateProductProcessState(String lotName, List<Element> productList, String lotProcessState, String strProductProcessState)
	{
		for (Element productE : productList)
		{
			String sProductName = productE.getChild("PRODUCTNAME").getText();

			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE PRODUCT ");
			sql.append("   SET PRODUCTPROCESSSTATE = :PRODUCTPROCESSSTATE ");
			sql.append(" WHERE PRODUCTNAME = :PRODUCTNAME ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("PRODUCTPROCESSSTATE", strProductProcessState);
			bindMap.put("PRODUCTNAME", sProductName);

			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);

			StringBuffer sql2 = new StringBuffer();
			sql2.append("UPDATE PRODUCTHISTORY ");
			sql2.append("   SET PRODUCTPROCESSSTATE = :PRODUCTPROCESSSTATE ");
			sql2.append(" WHERE PRODUCTNAME = :PRODUCTNAME ");
			sql2.append("   AND TIMEKEY = (SELECT LASTEVENTTIMEKEY ");
			sql2.append("                    FROM PRODUCT ");
			sql2.append("                   WHERE PRODUCTNAME = :PRODUCTNAME) ");

			GenericServiceProxy.getSqlMesTemplate().update(sql2.toString(), bindMap);
		}
		String strLotName = lotName;

		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE LOT ");
		sql.append("   SET LOTPROCESSSTATE = :LOTPROCESSSTATE ");
		sql.append(" WHERE LOTNAME = :LOTNAME ");

		Map<String, Object> bindMap2 = new HashMap<String, Object>();
		bindMap2.put("LOTPROCESSSTATE", lotProcessState);
		bindMap2.put("LOTNAME", strLotName);

		GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap2);

		StringBuffer sql2 = new StringBuffer();
		sql2.append("UPDATE LOTHISTORY ");
		sql2.append("   SET LOTPROCESSSTATE = :LOTPROCESSSTATE ");
		sql2.append(" WHERE LOTNAME = :LOTNAME ");
		sql2.append("   AND TIMEKEY = (SELECT LASTEVENTTIMEKEY ");
		sql2.append("                    FROM LOT ");
		sql2.append("                   WHERE LOTNAME = :LOTNAME) ");

		GenericServiceProxy.getSqlMesTemplate().update(sql2.toString(), bindMap2);
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