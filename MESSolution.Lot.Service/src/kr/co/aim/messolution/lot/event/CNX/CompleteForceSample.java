package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SampleLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class CompleteForceSample extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String completeFlowName = SMessageUtil.getBodyItemValue(doc, "COMPLETEFLOWNAME", true);
		String completeOperationName = SMessageUtil.getBodyItemValue(doc, "COMPLETEOPERATIONNAME", true);
		String completeFlowVersion = "00001";
		String completeOperationVersion = "00001";

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		CommonValidation.checkJobDownFlag(lotData);
		CommonValidation.checkLotProcessStateWait(lotData);
		CommonValidation.checkLotHoldState(lotData);
		checkForceFlowList(lotData);

		List<Product> producList = LotServiceProxy.getLotService().allProducts(lotName);

		// check ProcessInfo
		List<String> productNameList = new ArrayList<>();

		for (Product productA : producList)
		{
			String productName = productA.getKey().getProductName();
			productNameList.add(productName);
		}

		CommonValidation.checkProductProcessInfobyString(productNameList);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo_IgnoreHold("CompleteForceSample", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUSequence(lotName);

		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();

		changeSpecInfo.setAreaName(lotData.getAreaName());
		changeSpecInfo.setDueDate(lotData.getDueDate());
		changeSpecInfo.setFactoryName(lotData.getFactoryName());
		changeSpecInfo.setLotHoldState(lotData.getLotHoldState());
		changeSpecInfo.setLotProcessState(lotData.getLotProcessState());
		changeSpecInfo.setLotState(lotData.getLotState());
		changeSpecInfo.setNodeStack("");
		changeSpecInfo.setPriority(lotData.getPriority());
		changeSpecInfo.setProcessFlowName(completeFlowName);
		changeSpecInfo.setProcessFlowVersion(completeFlowVersion);
		changeSpecInfo.setProcessOperationName(completeOperationName);
		changeSpecInfo.setProcessOperationVersion(completeOperationVersion);
		changeSpecInfo.setProductionType(lotData.getProductionType());
		changeSpecInfo.setProductRequestName(lotData.getProductRequestName());
		changeSpecInfo.setProductSpec2Name(lotData.getProductSpec2Name());
		changeSpecInfo.setProductSpec2Version(lotData.getProductSpec2Version());
		changeSpecInfo.setProductSpecName(lotData.getProductSpecName());
		changeSpecInfo.setProductSpecVersion(lotData.getProductSpecVersion());
		changeSpecInfo.setProductUSequence(productUdfs);
		changeSpecInfo.setSubProductUnitQuantity1(lotData.getSubProductUnitQuantity1());
		changeSpecInfo.setSubProductUnitQuantity2(lotData.getSubProductUnitQuantity2());

		changeSpecInfo.getUdfs().put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());
		changeSpecInfo.getUdfs().put("BEFOREFLOWNAME", lotData.getProcessFlowName());
		
		List<Map<String, Object>> completeNodeResult = getReturnNode(changeSpecInfo);

		if (completeNodeResult.size() < 1)
		{
			throw new CustomException("Node-0001", lotData.getProductSpecName(), changeSpecInfo.getProcessFlowName(), changeSpecInfo.getProcessOperationName());
		}
		else
		{
			String sToBeNodeStack = ConvertUtil.getMapValueByName(completeNodeResult.get(0), "NODEID");
			String factoryName = ConvertUtil.getMapValueByName(completeNodeResult.get(0), "FACTORYNAME");
			String processFlowName = ConvertUtil.getMapValueByName(completeNodeResult.get(0), "PROCESSFLOWNAME");

			String returnFlowName = "";
			String returnOperationName = "";

			ProcessFlow returnFlowData = CommonUtil.getProcessFlowData(factoryName, processFlowName, "00001");

			// After doing Rework -> ForceSampling
			String nodeStack = lotData.getNodeStack();

			if (StringUtil.isNotEmpty(nodeStack) && nodeStack.indexOf(".") > 0)
			{
				String[] nodeList = StringUtil.split(nodeStack, ".");

				for (int i = nodeList.length - 1; i >= 0; i--)
				{
					List<Map<String, Object>> nodeInfo = getNodeInfo(nodeList[i]);

					String nodeFactoryName = ConvertUtil.getMapValueByName(nodeInfo.get(0), "FACTORYNAME");
					String nodeFlowName = ConvertUtil.getMapValueByName(nodeInfo.get(0), "PROCESSFLOWNAME");
					String nodeFlowVersion = ConvertUtil.getMapValueByName(nodeInfo.get(0), "PROCESSFLOWVERSION");
					String nodeOperationName = ConvertUtil.getMapValueByName(nodeInfo.get(0), "NODEATTRIBUTE1");
					String nodeOperationVersion = ConvertUtil.getMapValueByName(nodeInfo.get(0), "NODEATTRIBUTE2");

					List<SampleLot> sampleLotData = ExtendedObjectProxy.getSampleLotService().getForceSampleLotDataListByToInfo(lotName, nodeFactoryName, lotData.getProductSpecName(),
							lotData.getProductSpecVersion(), nodeFlowName, nodeFlowVersion, nodeOperationName, nodeOperationVersion, "Y");

					if (sampleLotData != null)
					{
						returnFlowName = sampleLotData.get(0).getReturnProcessFlowName();
						returnOperationName = sampleLotData.get(0).getReturnOperationName();

						nodeStack = nodeStack.substring(0, nodeStack.lastIndexOf("."));

						deleteSampleData(eventInfo, sampleLotData);

						if (!StringUtils.equals(completeFlowName, returnFlowName) || !StringUtils.equals(completeOperationName, returnOperationName))
							ExtendedObjectProxy.getLotFutureActionService().deleteLotFutureActionWithoutReaconCode(eventInfo, lotName, nodeFactoryName, returnFlowName, "00001", returnOperationName,
									"00001", 0);
					}
					else
					{
						returnFlowName = nodeFlowName;
						returnOperationName = nodeOperationName;
					}
				}

				if ((StringUtil.equals(returnFlowData.getProcessFlowType(), "Main") || StringUtil.equals(returnFlowData.getProcessFlowType(), "MQC")))
				{
					changeSpecInfo.getUdfs().put("RETURNFLOWNAME", "");
					changeSpecInfo.getUdfs().put("RETURNOPERATIONNAME", "");
				}
				else
				{
					changeSpecInfo.getUdfs().put("RETURNFLOWNAME", returnFlowName);
					changeSpecInfo.getUdfs().put("RETURNOPERATIONNAME", returnOperationName);
				}
			}

			changeSpecInfo.setNodeStack(nodeStack);
		}

		// 2021-02-04	dhko	Flow/Oper Information Calibration
		String nodeStack = changeSpecInfo.getNodeStack();
		if (StringUtil.isNotEmpty(nodeStack))
		{
			String nodeId = nodeStack;
			if (nodeId.indexOf(".") > 0)
			{
				String[] nodeIdList = StringUtil.split(nodeStack, ".");
				nodeId = nodeIdList[nodeIdList.length - 1];
			}
			
			List<Map<String, Object>> nodeInfo = getNodeInfo(nodeId);
			
			changeSpecInfo.setProcessFlowName(ConvertUtil.getMapValueByName(nodeInfo.get(0), "PROCESSFLOWNAME"));
			changeSpecInfo.setProcessFlowVersion(ConvertUtil.getMapValueByName(nodeInfo.get(0), "PROCESSFLOWVERSION"));
			changeSpecInfo.setProcessOperationName(ConvertUtil.getMapValueByName(nodeInfo.get(0), "NODEATTRIBUTE1"));
			changeSpecInfo.setProcessOperationVersion(ConvertUtil.getMapValueByName(nodeInfo.get(0), "NODEATTRIBUTE2"));
		}
		
		Lot afterLotData = MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);

		afterLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());

		// Set NextOper ReworkFlag.
		ProcessFlow currentFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(afterLotData);
		if (StringUtils.equals(currentFlowData.getProcessFlowType(), "Rework"))
			MESLotServiceProxy.getLotServiceUtil().setNextOperReworkFlag(eventInfo, afterLotData);

		// Skip
		afterLotData = MESLotServiceProxy.getLotServiceUtil().executePostAction(eventInfo, lotData, afterLotData, false);

		// Hold
		// Mantis - 0000018
		// makeOnHoldForCompleteForceSample(eventInfo, afterLotData);

		return doc;
	}

	int getNodeStackCount(String str, char c)
	{
		int count = 1;
		for (int i = 0; i < str.length(); i++)
		{
			if (str.charAt(i) == c)
				count++;
		}
		return count;
	}

	private List<Map<String, Object>> getReturnNode(ChangeSpecInfo changeSpecInfo)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT NODEID, PROCESSFLOWNAME, NODEATTRIBUTE1, FACTORYNAME ");
		sql.append("  FROM NODE ");
		sql.append(" WHERE FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND NODEATTRIBUTE1 = :PROCESSOPERATIONNAME ");
		sql.append("   AND NODEATTRIBUTE2 = :PROCESSOPERATIONVERSION ");
		sql.append("   AND NODETYPE = 'ProcessOperation' ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("FACTORYNAME", changeSpecInfo.getFactoryName());
		args.put("PROCESSFLOWNAME", changeSpecInfo.getProcessFlowName());
		args.put("PROCESSFLOWVERSION", changeSpecInfo.getProcessFlowVersion());
		args.put("PROCESSOPERATIONNAME", changeSpecInfo.getProcessOperationName());
		args.put("PROCESSOPERATIONVERSION", changeSpecInfo.getProcessOperationVersion());

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		return result;
	}

	private List<Map<String, Object>> getNodeInfo(String returnForOriginalNode)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT NODEID, PROCESSFLOWNAME, PROCESSFLOWVERSION, NODEATTRIBUTE1, NODEATTRIBUTE2, FACTORYNAME ");
		sql.append("  FROM NODE ");
		sql.append(" WHERE NODEID = :NODEID ");
		sql.append("   AND NODETYPE = 'ProcessOperation' ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("NODEID", returnForOriginalNode);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		return result;
	}

	private void deleteSampleData(EventInfo eventInfo, List<SampleLot> sampleLotData) throws CustomException
	{
		for (SampleLot sampleLot : sampleLotData)
		{
			ExtendedObjectProxy.getSampleProductService().deleteSampleProductByLotName(eventInfo, sampleLot.getLotName(), sampleLot.getFactoryName(), sampleLot.getProductSpecName(),
					sampleLot.getProductSpecVersion(), sampleLot.getProcessFlowName(), sampleLot.getProcessFlowVersion(), sampleLot.getProcessOperationName(), sampleLot.getProcessOperationVersion(),
					sampleLot.getMachineName(), sampleLot.getToProcessFlowName(), sampleLot.getToProcessFlowVersion(), sampleLot.getToProcessOperationName(), sampleLot.getToProcessOperationVersion());

			ExtendedObjectProxy.getSampleLotService().remove(eventInfo, sampleLot);
		}
	}

	private void makeOnHoldForCompleteForceSample(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		eventInfo.setEventName("Hold");
		eventInfo.setReasonCode("SYSTEM");
		eventInfo.setEventComment("Complete Force Sample [" + lotData.getKey().getLotName() + "], " + eventInfo.getEventComment());

		Map<String, String> udfs = new HashMap<String, String>();

		if (StringUtil.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Released) && StringUtil.equals(lotData.getLotProcessState(), "WAIT"))
		{
			MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, lotData, udfs);
		}
		else
		{
			throw new CustomException("LOT-0113", lotData.getLotState(), lotData.getLotProcessState());
		}
	}

	private void checkForceFlowList(Lot lotData) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("WITH SAMPLE ");
		sql.append("     AS (SELECT FACTORYNAME, ");
		sql.append("                LOTNAME, ");
		sql.append("                PRODUCTSPECNAME, ");
		sql.append("                PRODUCTSPECVERSION, ");
		sql.append("                PROCESSFLOWNAME, ");
		sql.append("                PROCESSFLOWVERSION, ");
		sql.append("                PROCESSOPERATIONNAME, ");
		sql.append("                PROCESSOPERATIONVERSION, ");
		sql.append("                TOPROCESSFLOWNAME, ");
		sql.append("                TOPROCESSFLOWVERSION, ");
		sql.append("                TOPROCESSOPERATIONNAME, ");
		sql.append("                TOPROCESSOPERATIONVERSION ");
		sql.append("           FROM CT_SAMPLELOT ");
		sql.append("          WHERE LOTNAME = :LOTNAME ");
		sql.append("            AND TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME ");
		sql.append("            AND TOPROCESSFLOWVERSION = :TOPROCESSFLOWVERSION ");
		sql.append("            AND TOPROCESSOPERATIONNAME = :TOPROCESSOPERATIONNAME ");
		sql.append("            AND TOPROCESSOPERATIONVERSION = :TOPROCESSOPERATIONVERSION) ");
		sql.append("SELECT CS.TOPROCESSFLOWNAME, ");
		sql.append("       CS.TOPROCESSFLOWVERSION, ");
		sql.append("       CS.TOPROCESSOPERATIONNAME, ");
		sql.append("       CS.TOPROCESSOPERATIONVERSION, ");
		sql.append("       CS.RETURNPROCESSFLOWNAME, ");
		sql.append("       CS.RETURNPROCESSFLOWVERSION, ");
		sql.append("       CS.RETURNOPERATIONNAME, ");
		sql.append("       CS.RETURNOPERATIONVERSION ");
		sql.append("  FROM CT_SAMPLELOT CS, SAMPLE S ");
		sql.append(" WHERE CS.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND CS.LOTNAME = :LOTNAME ");
		sql.append("   AND CS.PRODUCTSPECNAME = S.PRODUCTSPECNAME ");
		sql.append("   AND CS.PRODUCTSPECVERSION = S.PRODUCTSPECVERSION ");
		sql.append("   AND CS.PROCESSFLOWNAME = S.PROCESSFLOWNAME ");
		sql.append("   AND CS.PROCESSFLOWVERSION = S.PROCESSFLOWVERSION ");
		sql.append("   AND CS.PROCESSOPERATIONNAME = S.PROCESSOPERATIONNAME ");
		sql.append("   AND CS.PROCESSOPERATIONVERSION = S.PROCESSOPERATIONVERSION ");
		sql.append("   AND CS.FORCESAMPLINGFLAG = 'Y' ");
		sql.append("   AND CS.MANUALSAMPLEFLAG = 'Y' ");
		sql.append("ORDER BY CS.PRIORITY ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("LOTNAME", lotData.getKey().getLotName());
		args.put("TOPROCESSFLOWNAME", lotData.getProcessFlowName());
		args.put("TOPROCESSFLOWVERSION", lotData.getProcessFlowVersion());
		args.put("TOPROCESSOPERATIONNAME", lotData.getProcessOperationName());
		args.put("TOPROCESSOPERATIONVERSION", lotData.getProcessOperationVersion());
		args.put("FACTORYNAME", lotData.getFactoryName());

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (sqlResult.size() < 1)
		{
			throw new CustomException("LOT-0302");
		}
	}
}
