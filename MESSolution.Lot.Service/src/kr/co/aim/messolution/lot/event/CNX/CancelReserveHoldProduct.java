package kr.co.aim.messolution.lot.event.CNX;

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
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductFutureAction;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class CancelReserveHoldProduct extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false);
/*
		for (Element eledur : productList)
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelReserveHoldProduct", this.getEventUser(), this.getEventComment(), "", "");

			String sFactory = SMessageUtil.getChildText(eledur, "FACTORYNAME", true);
			String sProcessFlowName = SMessageUtil.getChildText(eledur, "PROCESSFLOWNAME", true);
			String sProcessFlowVersion = SMessageUtil.getChildText(eledur, "PROCESSFLOWVERSION", true);
			String sProcessOperName = SMessageUtil.getChildText(eledur, "PROCESSOPERATIONNAME", true);
			String sProcessOperVersion = SMessageUtil.getChildText(eledur, "PROCESSOPERATIONVERSION", true);
			String sProductName = SMessageUtil.getChildText(eledur, "PRODUCTNAME", true);
			String sReasonCode = SMessageUtil.getChildText(eledur, "REASONCODE", true);
			String sActionName = SMessageUtil.getChildText(eledur, "ACTIONNAME", true);
			String sActionType = SMessageUtil.getChildText(eledur, "ACTIONTYPE", true);
			//String sPosition = SMessageUtil.getChildText(eledur, "POSITION", true);
			String beforeActionUser = "";
			String afterActionUser = "";
			String beforeAction = "";
			String afterAction = "";
			String beforeActionComment = "";
			String afterActionComment = "";
			String reasonCodeType = "";
			String beforeMailFlag = "";
			String afterMailFlag = "";

			Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(sProductName);
			String lotName = productData.getLotName();
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

			List<kr.co.aim.messolution.extended.object.management.data.ProductFutureAction> reserveProductData = 
					ExtendedObjectProxy.getProductFutureActionService().getProductFutureActionDataList(sProductName, sFactory, sProcessFlowName, sProcessFlowVersion,
					sProcessOperName, sProcessOperVersion, sReasonCode);

			if (reserveProductData != null)
			{
				if (reserveProductData.size() == 1)
				{
					beforeActionUser = reserveProductData.get(0).getBeforeActionUser();
					afterActionUser = reserveProductData.get(0).getAfterActionUser();
					beforeAction = reserveProductData.get(0).getBeforeAction();
					afterAction = reserveProductData.get(0).getAfterAction();
					beforeActionComment = reserveProductData.get(0).getBeforeActionComment();
					afterActionComment = reserveProductData.get(0).getAfterActionComment();
					reasonCodeType = reserveProductData.get(0).getReasonCodeType();
					beforeMailFlag = reserveProductData.get(0).getBeforeMailFlag();
					afterMailFlag = reserveProductData.get(0).getAfterMailFlag();
				}
			}

			// Check ProcessFlow
			if (!checkProcessFlow(lotData, sProcessFlowName, sProcessFlowVersion))
				throw new CustomException("LOT-0081");

			if (StringUtil.equals(sReasonCode, "FirstGlassHold"))
				throw new CustomException("LOT-0108");

			if (StringUtils.equals(sActionName, "hold"))
			{
				if (StringUtils.equals(sActionType, "Before"))
				{
					//AR-TF-0001-01
					MESLotServiceProxy.getLotServiceUtil().checkDepartment(beforeActionUser, this.getEventUser(), lotData.getKey().getLotName());

					if (afterAction.equals("False"))
					{
						// delete
						ExtendedObjectProxy.getProductFutureActionService().deleteProductFutureActionData(eventInfo, sProductName, sFactory, sProcessFlowName, sProcessFlowVersion, sProcessOperName,
								sProcessOperVersion, sReasonCode);
					}
					else
					{
						// update
						eventInfo = EventInfoUtil.makeEventInfo("CancelReserveHold", this.getEventUser(), this.getEventComment(), null, null);
						ExtendedObjectProxy.getProductFutureActionService()
								.updateProductFutureActionWithReasonCodeType(eventInfo, sProductName, sFactory, sProcessFlowName, sProcessFlowVersion, sProcessOperName, sProcessOperVersion, sReasonCode,
										reasonCodeType, "hold", "System", "", "", "", "False", afterAction, "", afterActionComment, "", afterActionUser, "", afterMailFlag);
					}
				}
				else if (StringUtils.equals(sActionType, "After"))
				{
					//AR-TF-0001-01
					MESLotServiceProxy.getLotServiceUtil().checkDepartment(afterActionUser, this.getEventUser(), lotData.getKey().getLotName());
					
					if (StringUtils.equals(beforeAction, "False"))
					{
						// delete
						ExtendedObjectProxy.getProductFutureActionService().deleteProductFutureActionData(eventInfo, sProductName, sFactory, sProcessFlowName, sProcessFlowVersion, sProcessOperName,
								sProcessOperVersion, sReasonCode);
					}
					else
					{
						// update
						ExtendedObjectProxy.getProductFutureActionService()
						.updateProductFutureActionWithReasonCodeType(eventInfo, sProductName, sFactory, sProcessFlowName, sProcessFlowVersion, sProcessOperName, sProcessOperVersion, sReasonCode,
								reasonCodeType, "hold", "System", "", "", "", "False", afterAction, "", afterActionComment, "", afterActionUser, "", afterMailFlag);
					}
				}
			}
		}

		for (Element product : productList)
		{
			EventInfo sampleEventInfo = EventInfoUtil.makeEventInfo("CancelReserveHold", this.getEventUser(), this.getEventComment(), null, null);
			sampleEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", true);
			String factoryName = SMessageUtil.getChildText(product, "FACTORYNAME", true);
			String processFlowName = SMessageUtil.getChildText(product, "PROCESSFLOWNAME", true);
			String processFlowVersion = SMessageUtil.getChildText(product, "PROCESSFLOWVERSION", true);
			String processOperationName = SMessageUtil.getChildText(product, "PROCESSOPERATIONNAME", true);
			String processOperationVersion = SMessageUtil.getChildText(product, "PROCESSOPERATIONVERSION", true);
			String actionName = SMessageUtil.getChildText(product, "ACTIONNAME", true);

			if (checkFutureAction(productName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, actionName))
			{
				Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);
				String lotName = productData.getLotName();
				Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
				MESLotServiceProxy.getLotServiceUtil().checkSampleDataByCancelReserveHold(sampleEventInfo, lotData, processFlowName, processFlowVersion, processOperationName, processOperationVersion);
			}
		}
*/
		return doc;
	}

	private boolean checkProcessFlow(Lot lotData, String processFlowName, String processFlowVersion)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT DISTINCT PF.PROCESSFLOWNAME, PF.DESCRIPTION, PF.PROCESSFLOWVERSION, PF.PROCESSFLOWTYPE ");
		sql.append("  FROM TPFOPOLICY TPFO, PROCESSFLOW PF ");
		sql.append(" WHERE TPFO.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND TPFO.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("   AND TPFO.PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");
		sql.append("   AND PF.FACTORYNAME = TPFO.FACTORYNAME ");
		sql.append("   AND PF.PROCESSFLOWNAME = TPFO.PROCESSFLOWNAME ");
		sql.append("   AND PF.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION ");
		sql.append("   AND PF.PROCESSFLOWTYPE IN ('Inspection', 'Sample', 'Main', 'MQC', 'MQCPrepare','MQCRecycle','Rework') ");
		sql.append("   AND PF.ACTIVESTATE = 'Active' ");
		sql.append("ORDER BY NVL (LENGTH (TRIM (PF.PROCESSFLOWNAME)), 0) DESC, PF.PROCESSFLOWNAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("FACTORYNAME", lotData.getFactoryName());
		args.put("PRODUCTSPECNAME", lotData.getProductSpecName());
		args.put("PRODUCTSPECVERSION", lotData.getProductSpecVersion());

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		boolean checkFlag = false;

		if (result.size() > 0)
		{
			for (Map<String, Object> flowList : result)
			{
				String queryFlowName = ConvertUtil.getMapValueByName(flowList, "PROCESSFLOWNAME");
				String queryFlowVersion = ConvertUtil.getMapValueByName(flowList, "PROCESSFLOWVERSION");

				if (StringUtils.equals(processFlowName, queryFlowName) && StringUtils.equals(processFlowVersion, queryFlowVersion))
				{
					checkFlag = true;
					break;
				}
			}
		}

		return checkFlag;
	}

	private boolean checkFutureAction(String productName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion,
			String actionName) throws CustomException
	{
		boolean checkFlag = true;

		ProcessFlow processFlowData = CommonUtil.getProcessFlowData(factoryName, processFlowName, processFlowVersion);

		if (StringUtils.equals(processFlowData.getProcessFlowType(), "Inspection") || StringUtils.equals(processFlowData.getProcessFlowType(), "Sample"))
		{
			List<kr.co.aim.messolution.extended.object.management.data.ProductFutureAction> productFutureActionData = ExtendedObjectProxy.getProductFutureActionService().getProductFutureActionDataWithActionName(productName, factoryName, processFlowName, processFlowVersion,
					processOperationName, processOperationVersion, actionName);

			if (productFutureActionData != null)
				checkFlag = false;
		}
		else
		{
			checkFlag = false;
		}

		return checkFlag;
	}

}
