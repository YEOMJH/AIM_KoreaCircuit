package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class CreateMQCPlan extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String department = SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", true);
		String prepareProductSpecName = SMessageUtil.getBodyItemValue(doc, "PREPAREPRODUCTSPECNAME", true);
		String prepareProductSpecVersion = SMessageUtil.getBodyItemValue(doc, "PREPAREPRODUCTSPECVERSION", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String recycleFlowName = SMessageUtil.getBodyItemValue(doc, "RECYCLEFLOWNAME", false);
		String recycleFlowVersion = SMessageUtil.getBodyItemValue(doc, "RECYCLEFLOWVERSION", false);
		String recycleLimit = SMessageUtil.getBodyItemValue(doc, "RECYCLELIMIT", true);
		String usedLimit = SMessageUtil.getBodyItemValue(doc, "USEDLIMIT", true);

		List<Element> operationList = SMessageUtil.getBodySequenceItemList(doc, "OPERATIONLIST", true);
		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateMQC", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		String jobName = "MQC-" + lotName + "-" + eventInfo.getEventTimeKey();

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);

		List<Product> producList = LotServiceProxy.getLotService().allProducts(lotName);

		// check ProcessInfo
		List<String> productNameList = new ArrayList<>();
		for (Product productA : producList)
		{
			String productName = productA.getKey().getProductName();
			productNameList.add(productName);
		}
		CommonValidation.checkProductProcessInfobyString(productNameList);
		CommonValidation.checkLotState(lotData);
		CommonValidation.checkMQCFlow(processFlowData);
		CommonValidation.checkDummyProductReserve(lotData);

		ExtendedObjectProxy.getMQCPlanService().insertMQCPlan(eventInfo, jobName, getMaxSeq(lotName), lotData.getFactoryName(), productSpecName, productSpecVersion, processFlowName,
				processFlowVersion, lotData.getKey().getLotName(), department, recycleFlowName, recycleFlowVersion, prepareProductSpecName, prepareProductSpecVersion, Long.parseLong(recycleLimit));

		for (Element operation : operationList)
		{
			String processOperationName = SMessageUtil.getChildText(operation, "PROCESSOPERATIONNAME", true);
			String processOperationVersion = SMessageUtil.getChildText(operation, "PROCESSOPERATIONVERSION", true);
			String mqcReleaseFlag = SMessageUtil.getChildText(operation, "MQCRELEASEFLAG", true);
			String countingFlag = SMessageUtil.getChildText(operation, "COUNTINGFLAG", true);
			String machineRecipeName = SMessageUtil.getChildText(operation, "MACHINERECIPENAME", false);

			List<String> positionList = new ArrayList<String>();

			for (Element product : productList)
			{
				String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", true);
				String position = SMessageUtil.getChildText(product, "POSITION", true);

				checkUsedCount(processFlowName, processFlowVersion, productName, processOperationName, processOperationVersion, usedLimit);
				
				//Mantis-0000100
				checkProductUsedCount(productName, operationList.size(),countingFlag);

				positionList.add(position);

				ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().insertMQCPlanDetail_Extended(eventInfo, jobName, processFlowName, processFlowVersion, processOperationName,
						processOperationVersion, productName, position, lotName, "", "", "0",machineRecipeName, "");
			}

			ExtendedObjectProxy.getMQCPlanDetailService().insertMQCPlanDetail(eventInfo, jobName, processFlowName, processFlowVersion, processOperationName, processOperationVersion,
					lotData.getCarrierName(), lotData.getKey().getLotName(), CommonUtil.toStringWithoutBrackets(positionList), Long.parseLong(usedLimit), "", "", mqcReleaseFlag, countingFlag);
		}

		return doc;
	}

	private void checkUsedCount(String processFlowName, String processFlowVersion, String productName, String processOperationName, String processOperationVersion, String usedLimit)
			throws CustomException
	{
		// Check DummyUsedCount in CT_MQCPLANDETAIL_EXTENDED
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT MAX(C.DUMMYUSEDCOUNT) AS DUMMYUSEDCOUNT ");
		sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED C ");
		sql.append(" WHERE C.PRODUCTNAME = :PRODUCTNAME ");
		sql.append("   AND C.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND C.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND C.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND C.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);
		args.put("PRODUCTNAME", productName);
		args.put("PROCESSOPERATIONNAME", processOperationName);
		args.put("PROCESSOPERATIONVERSION", processOperationVersion);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			String dummyusedcount = ConvertUtil.getMapValueByName(result.get(0), "DUMMYUSEDCOUNT");

			if (StringUtils.isNotEmpty(dummyusedcount))
			{
				if (Long.parseLong(dummyusedcount) > Long.parseLong(usedLimit) - 1)
					throw new CustomException("LOT-0040", productName, usedLimit, dummyusedcount, processFlowName, processOperationName);
			}
		}
	}

	private int getMaxSeq(String lotName)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT NVL (MAX (SEQ), 0) + 1 AS MAXSEQ FROM CT_MQCPLAN WHERE LOTNAME = :LOTNAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("LOTNAME", lotName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		int seq = 1;

		if (result.size() > 0)
		{
			String sSeq = ConvertUtil.getMapValueByName(result.get(0), "MAXSEQ");
			seq = Integer.parseInt(sSeq);
		}

		return seq;
	}

	private void checkProductUsedCount(String productName, int operationCount,String countingFlag) throws CustomException
	{
		// Check DummyUsedCount in Product
		Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

		String mqcProductUsedLimit = productData.getUdfs().get("MQCPRODUCTUSEDLIMIT");
		String dummyUsedCount = productData.getUdfs().get("DUMMYUSEDCOUNT");
		
		if(StringUtils.isEmpty(dummyUsedCount))
			dummyUsedCount = "0";

		if(!StringUtils.equals(countingFlag, "Y"))
		{
			return;
		}
		
		if (StringUtils.isEmpty(mqcProductUsedLimit) || Integer.parseInt(mqcProductUsedLimit) < 1)
		{
			throw new CustomException("LOT-0151", productName);
		}

		if (Integer.parseInt(mqcProductUsedLimit) < Integer.parseInt(dummyUsedCount) + operationCount)
		{
			throw new CustomException("LOT-0152", mqcProductUsedLimit, productName, dummyUsedCount, operationCount);
		}
	}
}
