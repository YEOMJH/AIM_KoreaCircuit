package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DummyProductAssign;
import kr.co.aim.messolution.extended.object.management.data.MQCPlan;
import kr.co.aim.messolution.extended.object.management.data.MQCPlanDetail;
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
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.AssignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.ChangeGradeInfo;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.MakeScrappedInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.IncrementReleasedQuantityByInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class SplitLotForDummy extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String durableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", false);

		List<String> srcLotNameList = new ArrayList<String>();
		List<String> destLotNameList = new ArrayList<String>();

		List<Element> eleLotList = new ArrayList<Element>();

		srcLotNameList.add(lotName);

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("SplitLotForDummy", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		// validation
		CommonValidation.checkJobDownFlag(lotData);
		CommonValidation.checkLotState(lotData);
		CommonValidation.checkLotProcessState(lotData);
		CommonValidation.checkLotHoldState(lotData);
		CommonValidation.checkOriginalProduct(lotData);

		if (!durableName.isEmpty())
		{
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
			CommonValidation.CheckDurableHoldState(durableData);
		}

		if (!lotData.getProcessGroupName().isEmpty())
			throw new CustomException("LOT-0093");

		// CM-CM-0013-01
		// Split operation not possible if the array is MQC.
		if (StringUtils.equals(lotData.getFactoryName(), "ARRAY") && "M".equals(lotData.getProductionType()))
		{
			// Split operation not possible if the array is MQC.
			throw new CustomException("LOT-3001");
		}

		Element eleBody = SMessageUtil.getBodyElement(doc);
		List<Element> eleDurableList = SMessageUtil.getSubSequenceItemList(eleBody, "DURABLELIST", true);

		List<Element> elSourceProductList = new ArrayList<Element>();
		try
		{
			elSourceProductList = SMessageUtil.getSubSequenceItemList(eleBody, "SOURCEPRODUCTLIST", true);
		}
		catch (Exception e)
		{
			elSourceProductList = null;
		}

		ProductSpec baseData = GenericServiceProxy.getSpecUtil().getProductSpec(factoryName, lotData.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);

		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("LOTNAME", lotName);

		for (Element eleDurable : eleDurableList)
		{
			String desDurableName = SMessageUtil.getChildText(eleDurable, "DURABLENAME", true);

			Durable desDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(desDurableName);

			CommonValidation.CheckDurableHoldState(desDurableData);

			List<Element> eleProductList = SMessageUtil.getSubSequenceItemList(eleDurable, "PRODUCTLIST", true);
			List<String> prodList = new ArrayList<String>();

			List<ProductP> productPSequence = new ArrayList<ProductP>();
			String productProductionType = null;

			for (Element eleProduct : eleProductList)
			{
				String productName = SMessageUtil.getChildText(eleProduct, "PRODUCTNAME", true);
				int position = Integer.parseInt(SMessageUtil.getChildText(eleProduct, "POSITION", true));
				String slotPosition = SMessageUtil.getChildText(eleProduct, "SLOTPOSITION", true);
				Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);

				ProductP productP = new ProductP();
				productP.setProductName(productData.getKey().getProductName());
				productP.setPosition(position);
				productP.getUdfs().put("SLOTPOSITION", slotPosition);
				productPSequence.add(productP);

				productProductionType = productData.getProductionType();

				if (StringUtils.isNotEmpty(productName))
					prodList.add(productName);
			}

			Lot destLotData = null;
			try
			{
				destLotData = MESLotServiceProxy.getLotInfoUtil().getLotInfoBydurableName(desDurableName);
			}
			catch (CustomException ce)
			{
			}

			if (destLotData == null)
			{
				// Create GarbageLotName List
				String newLotName = CommonUtil.generateNameByNamingRule("SplitLotNaming", nameRuleAttrMap, 1).get(0);

				eventInfo.setEventName("Create");
				destLotData = MESLotServiceProxy.getLotServiceUtil().createWithParentLotAndProductProductionType(eventInfo, newLotName, lotData, productProductionType, "", false,
						new HashMap<String, String>(), lotData.getUdfs());

				destLotNameList.add(newLotName);
			}

			if (elSourceProductList != null)
			{
				String sourLotGrade = CommonUtil.judgeLotGradeByProductList(elSourceProductList, "PRODUCTJUDGE");
				MESLotServiceProxy.getLotServiceImpl().updateLotData("LOTGRADE", sourLotGrade, lotName);
			}

			if (eleProductList != null)
			{
				String destLotGrade = CommonUtil.judgeLotGradeByProductList(eleProductList, "PRODUCTJUDGE");
				MESLotServiceProxy.getLotServiceImpl().updateLotData("LOTGRADE", destLotGrade, destLotData.getKey().getLotName());
			}

			UpdateDestLotData(destLotData, eleProductList.size(), lotData, prodList);

			// Split Lot
			TransferProductsToLotInfo transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(destLotData.getKey().getLotName(), eleProductList.size(), productPSequence,
					lotData.getUdfs(), new HashMap<String, String>());
			transitionInfo.setEmptyFlag("N");
			eventInfo.setEventName("Split");
			lotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotData, transitionInfo);

			// Recovery DummyProductAssign ReturnInfo
			destLotData = RecoverySpec(eventInfo, productPSequence, destLotData);

			decrementQuantity(eventInfo, lotData, productPSequence.size());
			incrementQuantity(eventInfo, destLotData, productPSequence.size());

			 //modify by wangys 2020/11/26 Cancel Auto CompleteWO 
			/*ProductRequest newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(destLotData.getProductRequestName());
			if (newProductRequestData.getPlanQuantity() == newProductRequestData.getFinishedQuantity() + newProductRequestData.getScrappedQuantity())
			{
				EventInfo newEventInfo = eventInfo;
				newEventInfo.setEventName("Completed");
				newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompleted(newEventInfo, destLotData.getProductRequestName());
			}*/

			eleLotList.add(setCreatedLotList(destLotData));

			// Mantis - 0000043
			// Assign Carrier
			AssignCarrierInfo AssigncreateInfo = MESLotServiceProxy.getLotInfoUtil().AssignCarrierInfo(destLotData, desDurableName, productPSequence);

			eventInfo.setEventName("AssignCarrier");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			MESLotServiceProxy.getLotServiceImpl().assignCarrier(destLotData, AssigncreateInfo, eventInfo);

			// Hold DummyLot
			holdLot(destLotData);

			// Create MQC
			if (CommonUtil.equalsIn(baseData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare"))
			{
				eventLog.info("ProductSpecGroup: " + baseData.getUdfs().get("PRODUCTSPECGROUP"));
				createMQC(lotData, destLotData, prodList, desDurableName, eventInfo);
			}
		}

		// Set Data(Sample, FutureAction) Transfer Product
		MESLotServiceProxy.getLotServiceUtil().transferProductSyncData(eventInfo, srcLotNameList, destLotNameList);

		// Set Data(MainReserveSkip) New Lot - Lot AR-AMF-0030-01
		ExtendedObjectProxy.getMainReserveSkipService().syncMainReserveSkip(eventInfo, srcLotNameList, destLotNameList);

		// Make Emptied
		if (lotData.getProductQuantity() == 0)
		{
			Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();
			if (StringUtil.isNotEmpty(lotData.getCarrierName()))
			{
				Durable sLotDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());
				deassignCarrierUdfs = sLotDurableData.getUdfs();
				DeassignCarrierInfo deassignCarrierInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, sLotDurableData, new ArrayList<ProductU>());
				// Deassign Carrier
				eventInfo.setEventName("DeassignCarrier");
				MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, deassignCarrierInfo, eventInfo);
			}

			eventInfo.setEventName("MakeEmptied");
			MESLotServiceProxy.getLotServiceImpl().MakeEmptied(eventInfo, lotData, new ArrayList<ProductU>(), deassignCarrierUdfs);
			deassignCarrierUdfs.clear();

			// Remove MQC Job When Source Lot is Emptied
			if (CommonUtil.equalsIn(baseData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare"))
			{
				eventLog.info("ProductSpecGroup: " + baseData.getUdfs().get("PRODUCTSPECGROUP"));
				removeMQCJob(eventInfo, lotData);
			}
		}

		// 2020-11-15	dhko	LotHold if ProcessingInfo is 'B' in Product
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		MESLotServiceProxy.getLotServiceImpl().makeOnHoldByAbortProductList(eventInfo, srcLotNameList);
		MESLotServiceProxy.getLotServiceImpl().makeOnHoldByAbortProductList(eventInfo, destLotNameList);
		
		XmlUtil.setSubChildren(SMessageUtil.getBodyElement(doc), "SPLITLOTLIST", eleLotList);

		return doc;
	}

	private void UpdateDestLotData(Lot DestLotData, double ProductQty, Lot SrcLotData, List<String> prodList)
	{
		try
		{
			// update LOT Table
			StringBuffer insertSql = new StringBuffer();
			insertSql.append("UPDATE LOT ");
			insertSql.append("   SET REWORKCOUNT = :REWORKCOUNT, REWORKSTATE = :REWORKSTATE, REWORKNODEID = :REWORKNODEID ");
			insertSql.append(" WHERE LOTNAME = :LOTNAME ");

			Map<String, Object> updateBindMap = new HashMap<String, Object>();

			int maxReworkCount = getMaxReworkCount(prodList);

			updateBindMap.put("REWORKCOUNT", maxReworkCount);
			updateBindMap.put("REWORKSTATE", SrcLotData.getReworkState());
			updateBindMap.put("REWORKNODEID", SrcLotData.getReworkNodeId());
			updateBindMap.put("LOTNAME", DestLotData.getKey().getLotName());

			GenericServiceProxy.getSqlMesTemplate().update(insertSql.toString(), updateBindMap);

			// update LOTHISTORY Table
			StringBuffer histSql = new StringBuffer();
			histSql.append("UPDATE LOTHISTORY ");
			histSql.append("   SET REWORKCOUNT = :REWORKCOUNT, REWORKSTATE = :REWORKSTATE, REWORKNODEID = :REWORKNODEID ");
			histSql.append(" WHERE LOTNAME = :LOTNAME ");
			histSql.append("   AND TIMEKEY = :TIMEKEY ");

			Map<String, Object> hisbindMap = new HashMap<String, Object>();

			hisbindMap.put("REWORKCOUNT", maxReworkCount);
			hisbindMap.put("REWORKSTATE", SrcLotData.getReworkState());
			hisbindMap.put("REWORKNODEID", SrcLotData.getReworkNodeId());
			hisbindMap.put("LOTNAME", DestLotData.getKey().getLotName());
			hisbindMap.put("TIMEKEY", SrcLotData.getLastEventTimeKey());

			GenericServiceProxy.getSqlMesTemplate().update(histSql.toString(), hisbindMap);
		}
		catch (Exception e)
		{
		}
	}

	@SuppressWarnings("unchecked")
	private void createMQC(Lot sourceLotData, Lot destinationLotData, List<String> prodList, String destCarrierName, EventInfo eventInfo) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT JOBNAME, MQCSTATE ");
		sql.append("  FROM CT_MQCPLAN ");
		sql.append(" WHERE MQCSTATE IN ('Released', 'Recycling') ");
		sql.append("   AND LOTNAME = :LOTNAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("LOTNAME", sourceLotData.getKey().getLotName());

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			String oldJobName = ConvertUtil.getMapValueByName(result.get(0), "JOBNAME");
			String oldJobState = ConvertUtil.getMapValueByName(result.get(0), "MQCSTATE");
			String jobName = "MQC-" + destinationLotData.getKey().getLotName() + "-" + TimeStampUtil.getCurrentEventTimeKey();

			boolean isRecycleMQC = false;
			boolean isMQCwithReserveEQP = false;

			isRecycleMQC = isRecycleMQC(oldJobName);
			isMQCwithReserveEQP = isMQCwithReserveEQP(oldJobName);

			MQCPlan oldPlanData = ExtendedObjectProxy.getMQCPlanService().selectByKey(false, new Object[] { oldJobName });

			// Create MQCPlan
			MQCPlan planData = (MQCPlan) ObjectUtil.copyTo(oldPlanData);
			planData = ExtendedObjectProxy.getMQCPlanService().insertMQCPlan(eventInfo, planData, jobName, 1, destinationLotData.getKey().getLotName(), oldJobState);

			// Create MQCPlanDetail
			List<MQCPlanDetail> destPlanDetailList = new ArrayList<MQCPlanDetail>();
			List<MQCPlanDetail> oldPlanDetailList = new ArrayList<MQCPlanDetail>();
			List<MQCPlanDetail> srcPlanDetailList = new ArrayList<MQCPlanDetail>();

			sql.setLength(0);
			sql.append("SELECT DISTINCT P.POSITION, P.PRODUCTNAME");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P ");
			sql.append(" WHERE E.PRODUCTNAME = P.PRODUCTNAME ");
			sql.append("   AND E.JOBNAME = :OLDJOBNAME ");
			sql.append("   AND P.PRODUCTNAME IN ( :PRODUCTLIST ) ");
			sql.append("ORDER BY TO_NUMBER(P.POSITION) ");

			args.clear();
			args.put("OLDJOBNAME", oldJobName);
			args.put("PRODUCTLIST", prodList);

			List<Map<String, Object>> positionResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (positionResult.size() > 0)
			{
				List<String> positionList = CommonUtil.makeListBySqlResult(positionResult, "POSITION");
				String actualPosition = CommonUtil.toStringWithoutBrackets(positionList);
				String modifyPositionforMainMQC = getModifyPositionforSourceMQCJob(oldJobName, prodList, isRecycleMQC);
				String modifyPositionforRecycle = getModifyPositionforSourceRecycleMQCJob(oldJobName, prodList, isRecycleMQC);
				String modifyDestPositionforMain = getModifyPositionforDestMQCJob(oldJobName, jobName, prodList, isRecycleMQC);
				String modifyDestPositionforRecycle = getModifyPositionforDestRecycleMQCJob(oldJobName, jobName, prodList, isRecycleMQC);

				sql.setLength(0);
				sql.append("SELECT DISTINCT ");
				sql.append("       D.JOBNAME, ");
				sql.append("       D.PROCESSFLOWNAME, ");
				sql.append("       D.PROCESSFLOWVERSION, ");
				sql.append("       D.PROCESSOPERATIONNAME, ");
				sql.append("       D.PROCESSOPERATIONVERSION, ");
				sql.append("       E.MACHINENAME, ");
				sql.append("       E.RECIPENAME ");
				sql.append("  FROM CT_MQCPLANDETAIL D, CT_MQCPLANDETAIL_EXTENDED E ");
				sql.append(" WHERE D.JOBNAME = E.JOBNAME ");
				sql.append("   AND D.PROCESSFLOWNAME = E.PROCESSFLOWNAME ");
				sql.append("   AND D.PROCESSFLOWVERSION = E.PROCESSFLOWVERSION ");
				sql.append("   AND D.PROCESSOPERATIONNAME = E.PROCESSOPERATIONNAME ");
				sql.append("   AND D.PROCESSOPERATIONVERSION = E.PROCESSOPERATIONVERSION ");
				sql.append("   AND D.JOBNAME = :OLDJOBNAME ");
				sql.append("   AND E.PRODUCTNAME IN ( :PRODUCTLIST ) ");

				result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

				if (result.size() > 0)
				{
					if (!isRecycleMQC) // MQC Main Flow
					{
						for (Map<String, Object> map : result)
						{
							String processFlowName = ConvertUtil.getMapValueByName(map, "PROCESSFLOWNAME");
							String processFlowVersion = ConvertUtil.getMapValueByName(map, "PROCESSFLOWVERSION");
							String processOperationName = ConvertUtil.getMapValueByName(map, "PROCESSOPERATIONNAME");
							String processOperationVersion = ConvertUtil.getMapValueByName(map, "PROCESSOPERATIONVERSION");
							String machineName = ConvertUtil.getMapValueByName(map, "MACHINENAME");
							String recipeName = ConvertUtil.getMapValueByName(map, "RECIPENAME");

							MQCPlanDetail oldPlanDetail = ExtendedObjectProxy.getMQCPlanDetailService().selectByKey(false,
									new Object[] { oldJobName, processFlowName, processFlowVersion, processOperationName, processOperationVersion });

							if (!machineName.isEmpty() || !recipeName.isEmpty())
							{
								oldPlanDetail.setMachineName(machineName);
								oldPlanDetail.setRecipeName(recipeName);
							}

							oldPlanDetailList.add(oldPlanDetail);
						}

						for (MQCPlanDetail oldPlanDetail : oldPlanDetailList)
						{
							MQCPlanDetail planDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);
							planDetail.setJobName(jobName);
							planDetail.setLotName(destinationLotData.getKey().getLotName());
							planDetail.setCarrierName(destCarrierName);
							planDetail.setLastEventTime(eventInfo.getEventTime());
							planDetail.setLastEventName(eventInfo.getEventName());
							planDetail.setLastEventUser(eventInfo.getEventUser());
							planDetail.setLastEventComment(eventInfo.getEventComment());
							planDetail.setPosition(actualPosition);

							destPlanDetailList.add(planDetail);

							MQCPlanDetail SrcPlanDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);

							SrcPlanDetail.setLastEventTime(eventInfo.getEventTime());
							SrcPlanDetail.setLastEventName(eventInfo.getEventName());
							SrcPlanDetail.setLastEventUser(eventInfo.getEventUser());
							SrcPlanDetail.setLastEventComment(eventInfo.getEventComment());
							if (isMQCwithReserveEQP)
							{
								String actualPositionforReserveEQP = getActualPositionforReserveEQP(oldJobName, prodList, planDetail.getProcessFlowName(), planDetail.getProcessOperationName());
								SrcPlanDetail.setPosition(actualPositionforReserveEQP);
							}
							else
							{
								SrcPlanDetail.setPosition(modifyPositionforMainMQC);
							}

							srcPlanDetailList.add(SrcPlanDetail);
						}

					}
					else
					// MQC Recycle Flow
					{
						boolean OnlyRecycleFlag = true;

						for (Map<String, Object> map : result)
						{
							String processFlowName = ConvertUtil.getMapValueByName(map, "PROCESSFLOWNAME");
							String processFlowVersion = ConvertUtil.getMapValueByName(map, "PROCESSFLOWVERSION");
							String processOperationName = ConvertUtil.getMapValueByName(map, "PROCESSOPERATIONNAME");
							String processOperationVersion = ConvertUtil.getMapValueByName(map, "PROCESSOPERATIONVERSION");

							ProcessFlowKey processFlowKey = new ProcessFlowKey();
							processFlowKey.setFactoryName(sourceLotData.getFactoryName());
							processFlowKey.setProcessFlowName(processFlowName);
							processFlowKey.setProcessFlowVersion(processFlowVersion);
							ProcessFlow processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);

							if (StringUtil.equalsIgnoreCase(processFlowData.getProcessFlowType(), "MQC"))
								OnlyRecycleFlag = false;

							MQCPlanDetail oldPlanDetail = ExtendedObjectProxy.getMQCPlanDetailService().selectByKey(false,
									new Object[] { oldJobName, processFlowName, processFlowVersion, processOperationName, processOperationVersion });

							oldPlanDetailList.add(oldPlanDetail);
						}

						for (MQCPlanDetail oldPlanDetail : oldPlanDetailList)
						{
							if (OnlyRecycleFlag)
							{
								eventLog.info("Transfer product from SourceLot is Only in Recycle Flow");
								MQCPlanDetail DestPlanDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);

								ProcessFlowKey destProcessFlowKey = new ProcessFlowKey();
								destProcessFlowKey.setFactoryName(destinationLotData.getFactoryName());
								destProcessFlowKey.setProcessFlowName(DestPlanDetail.getProcessFlowName());
								destProcessFlowKey.setProcessFlowVersion(DestPlanDetail.getProcessFlowVersion());
								ProcessFlow DestPlanProcessFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(destProcessFlowKey);

								DestPlanDetail.setJobName(jobName);
								DestPlanDetail.setLotName(destinationLotData.getKey().getLotName());
								DestPlanDetail.setCarrierName(destCarrierName);
								DestPlanDetail.setLastEventTime(eventInfo.getEventTime());
								DestPlanDetail.setLastEventName(eventInfo.getEventName());
								DestPlanDetail.setLastEventUser(eventInfo.getEventUser());
								DestPlanDetail.setLastEventComment(eventInfo.getEventComment());

								if (StringUtil.equalsIgnoreCase(DestPlanProcessFlowData.getProcessFlowType(), "MQC"))
									DestPlanDetail.setPosition(modifyDestPositionforMain); // set Positions for MainMQCFlow
								else
									DestPlanDetail.setPosition(modifyDestPositionforRecycle); // set Positions for RecycleFlow

								destPlanDetailList.add(DestPlanDetail);

								MQCPlanDetail SrcPlanDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);

								SrcPlanDetail.setLastEventTime(eventInfo.getEventTime());
								SrcPlanDetail.setLastEventName(eventInfo.getEventName());
								SrcPlanDetail.setLastEventUser(eventInfo.getEventUser());
								SrcPlanDetail.setLastEventComment(eventInfo.getEventComment());
								SrcPlanDetail.setPosition(modifyPositionforRecycle);

								srcPlanDetailList.add(SrcPlanDetail);
							}
							else
							{
								eventLog.info("Transfer product from SourceLot is not Only in Recycle Flow");
								MQCPlanDetail DestPlanDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);

								ProcessFlowKey destProcessFlowKey = new ProcessFlowKey();
								destProcessFlowKey.setFactoryName(destinationLotData.getFactoryName());
								destProcessFlowKey.setProcessFlowName(DestPlanDetail.getProcessFlowName());
								destProcessFlowKey.setProcessFlowVersion(DestPlanDetail.getProcessFlowVersion());
								ProcessFlow DestPlanProcessFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(destProcessFlowKey);

								DestPlanDetail.setJobName(jobName);
								DestPlanDetail.setLotName(destinationLotData.getKey().getLotName());
								DestPlanDetail.setCarrierName(destCarrierName);
								DestPlanDetail.setLastEventTime(eventInfo.getEventTime());
								DestPlanDetail.setLastEventName(eventInfo.getEventName());
								DestPlanDetail.setLastEventUser(eventInfo.getEventUser());
								DestPlanDetail.setLastEventComment(eventInfo.getEventComment());

								if (StringUtil.equalsIgnoreCase(DestPlanProcessFlowData.getProcessFlowType(), "MQC"))
									DestPlanDetail.setPosition(modifyDestPositionforMain); // set Positions for MainMQCFlow
								else
									DestPlanDetail.setPosition(modifyDestPositionforRecycle); // set Positions for RecycleFlow

								destPlanDetailList.add(DestPlanDetail);

								MQCPlanDetail SrcPlanDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);

								ProcessFlowKey SrcProcessFlowKey = new ProcessFlowKey();
								SrcProcessFlowKey.setFactoryName(sourceLotData.getFactoryName());
								SrcProcessFlowKey.setProcessFlowName(SrcPlanDetail.getProcessFlowName());
								SrcProcessFlowKey.setProcessFlowVersion(SrcPlanDetail.getProcessFlowVersion());
								ProcessFlow SrcPlanProcessFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(SrcProcessFlowKey);

								SrcPlanDetail.setLastEventTime(eventInfo.getEventTime());
								SrcPlanDetail.setLastEventName(eventInfo.getEventName());
								SrcPlanDetail.setLastEventUser(eventInfo.getEventUser());
								SrcPlanDetail.setLastEventComment(eventInfo.getEventComment());

								if (StringUtil.equalsIgnoreCase(SrcPlanProcessFlowData.getProcessFlowType(), "MQC"))
								{
									// set Positions for MainMQCFlow
									if (isMQCwithReserveEQP)
									{
										String actualPositionforReserveEQP = getActualPositionforReserveEQP(oldJobName, prodList, SrcPlanDetail.getProcessFlowName(),
												SrcPlanDetail.getProcessOperationName());
										SrcPlanDetail.setPosition(actualPositionforReserveEQP);
									}
									else
									{
										SrcPlanDetail.setPosition(modifyPositionforMainMQC);
									}
								}
								else
								{
									// set Positions for RecycleFlow
									SrcPlanDetail.setPosition(modifyPositionforRecycle);
								}

								srcPlanDetailList.add(SrcPlanDetail);
							}
						}

					}

					if (destPlanDetailList.size() > 0)
					{
						ExtendedObjectProxy.getMQCPlanDetailService().create(eventInfo, destPlanDetailList);
						eventLog.info(destPlanDetailList.size() + " Rows inserted into CT_MQCPlanDetail");
					}

					if (srcPlanDetailList.size() > 0)
					{
						// Modify Source MQC Job in CT_MQCPlanDetail by Source Transfer ProductList
						try
						{
							for (MQCPlanDetail PlanDetail : srcPlanDetailList)
								ExtendedObjectProxy.getMQCPlanDetailService().modify(eventInfo, PlanDetail);

							eventLog.info(srcPlanDetailList.size() + " Rows Modified into CT_MQCPlanDetail for Source MQC Job");
						}
						catch (Exception ex)
						{
							eventLog.info("Error : Rows Modified into CT_MQCPlanDetail");
						}
					}

					int rows = 0;
					for (String SourceProduct : prodList)
					{
						for (Map<String, Object> map : positionResult)
						{
							String productName = ConvertUtil.getMapValueByName(map, "PRODUCTNAME");
							String position = ConvertUtil.getMapValueByName(map, "POSITION");

							if (productName.equalsIgnoreCase(SourceProduct))
							{
								// Update MQCPlanDetail_Extended
								sql.setLength(0);
								sql.append("INSERT INTO CT_MQCPLANDETAIL_EXTENDED ");
								sql.append("        (JOBNAME, PROCESSFLOWNAME, PROCESSFLOWVERSION, PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION,  ");
								sql.append("         PRODUCTNAME, POSITION, LOTNAME, FORBIDDENCODE, OLDFORBIDDENCODE,  ");
								sql.append("         LASTEVENTNAME, LASTEVENTTIME, LASTEVENTUSER, DUMMYUSEDCOUNT, RECIPENAME,  ");
								sql.append("         MACHINENAME) ");
								sql.append(" (SELECT :JOBNAME, PROCESSFLOWNAME, PROCESSFLOWVERSION, PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION,  ");
								sql.append("         PRODUCTNAME, :POSITION, :LOTNAME, FORBIDDENCODE, OLDFORBIDDENCODE,  ");
								sql.append("         :EVENTNAME, :EVENTTIME, :EVENTUSER, DUMMYUSEDCOUNT, RECIPENAME,  ");
								sql.append("         MACHINENAME FROM CT_MQCPLANDETAIL_EXTENDED ");
								sql.append("   WHERE JOBNAME = :OLDJOBNAME ");
								sql.append("     AND PRODUCTNAME = :PRODUCTNAME ) ");

								args.clear();
								args.put("JOBNAME", jobName);
								args.put("POSITION", position);
								args.put("LOTNAME", destinationLotData.getKey().getLotName());
								args.put("EVENTNAME", eventInfo.getEventName());
								args.put("EVENTTIME", eventInfo.getEventTime().toString());
								args.put("EVENTUSER", eventInfo.getEventUser());
								args.put("OLDJOBNAME", oldJobName);
								args.put("PRODUCTNAME", productName);
								// args.put("PRODUCTLIST", prodList);

								GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), args);
								rows++;
							}
						}
					}
					eventLog.info(rows + " Rows inserted into CT_MQCPlanDetail_Extended");

					// Delete Source MQC Job in MQCPlanDetail_Extended by Source ProductList
					ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().deleteMQCPlanDetail_ExtendedByProdList(oldJobName, prodList);

					eventLog.info("Rows deleted into CT_MQCPlanDetail_Extended by Soruce MQC Job");

					// Lot without MQCJob is sent to MQC Bank.
					if (!ExistMQCProductbyLot(sourceLotData, oldJobName))
					{
						eventLog.info("Source Lot [" + sourceLotData + "] does not have MQC Product.");
						// EventInfo eventInfo , MQCPlan planData, Lot lotData
						changeReturnMQCBank(eventInfo, sourceLotData, oldJobName);

						// After being sent to the MQC bank, Delete MQC Job
						if (CommonUtil.equalsIn(sourceLotData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare"))
						{
							eventLog.info("After being sent to the MQC bank, Delete MQC Job. Source Lot : [" + sourceLotData + "]");
							removeMQCJob(eventInfo, sourceLotData);
						}
					}
				}
				else
				{
					eventLog.info("Not exist MQC Operation Info by Product" + prodList);
				}
			}
			else
			{
				eventLog.info("Not exist MQC Product Info by Product" + prodList);

				// Lot without MQCJob is sent to MQC Bank.
				if (!ExistMQCProductbyLot(destinationLotData, jobName))
				{
					eventLog.info("Destination Lot [" + destinationLotData + "] does not have MQC Product.");
					changeReturnMQCBank(eventInfo, destinationLotData, jobName);

					// After being sent to the MQC bank, Delete MQC Job
					if (CommonUtil.equalsIn(destinationLotData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare"))
					{
						eventLog.info("After being sent to the MQC bank, Delete MQC Job. Destination Lot : [" + destinationLotData + "]");
						removeMQCJob(eventInfo, destinationLotData);
					}
				}
			}
		}
		else
		{
			eventLog.info("Not exist Release/Recycling MQC Info by Lot: " + sourceLotData.getKey().getLotName());
		}
	}

	private int getMaxReworkCount(List<String> prodList) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT MAX (REWORKCOUNT) AS MAXREWORKCOUNT ");
		sql.append("  FROM PRODUCT ");
		sql.append(" WHERE PRODUCTNAME IN (:PRODUCTLIST) ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("PRODUCTLIST", prodList);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		String maxReworkCount = "";

		if (result.size() > 0)
			maxReworkCount = ConvertUtil.getMapValueByName(result.get(0), "MAXREWORKCOUNT");

		if (StringUtils.isEmpty(maxReworkCount))
			maxReworkCount = "0";

		return Integer.parseInt(maxReworkCount);
	}

	private String getModifyPositionforSourceMQCJob(String ScrMQCJobName, List<String> SrcTransferProductList, boolean isRecycleMQC) throws CustomException
	{
		String modifyPosition = "";

		if (!isRecycleMQC) // Main MQC Flow
		{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT DISTINCT E.POSITION, E.PRODUCTNAME");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P   ");
			sql.append(" WHERE 1=1 ");
			sql.append("   AND E.JOBNAME = :SRCJOBNAME ");
			sql.append("   AND P.PRODUCTNAME NOT IN ( :SOURCEPRODUCTLIST )   ");
			sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME ");
			sql.append("ORDER BY TO_NUMBER(POSITION) ");

			Map<String, Object> args = new HashMap<String, Object>();
			args.put("SRCJOBNAME", ScrMQCJobName);
			args.put("SOURCEPRODUCTLIST", SrcTransferProductList);

			try
			{
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> positionResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

				if (positionResult.size() > 0)
				{
					eventLog.info("Get Positions Info in CT_MQCPlanDetail by Source Transfer Product");
					List<String> positionList = CommonUtil.makeListBySqlResult(positionResult, "POSITION");
					modifyPosition = CommonUtil.toStringWithoutBrackets(positionList);
				}
			}
			catch (Exception ex)
			{
				eventLog.info("Error : Get Positions Info in CT_MQCPlanDetail by Source Transfer Product : " + ScrMQCJobName);
			}
		}
		else
		// Recycle MQC Flow
		{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT DISTINCT E.POSITION, E.PRODUCTNAME ");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P ");
			sql.append(" WHERE 1 = 1 ");
			sql.append("   AND E.JOBNAME = :SRCJOBNAME ");
			sql.append("   AND P.PRODUCTNAME NOT IN ( :SOURCEPRODUCTLIST) ");
			sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME ");
			sql.append("   AND E.PROCESSFLOWNAME NOT IN (SELECT PROCESSFLOWNAME ");
			sql.append("                                   FROM PROCESSFLOW ");
			sql.append("                                  WHERE PROCESSFLOWTYPE = 'MQCRecycle') ");
			sql.append("ORDER BY TO_NUMBER(POSITION) ");

			Map<String, Object> args = new HashMap<String, Object>();
			args.put("SRCJOBNAME", ScrMQCJobName);
			args.put("SOURCEPRODUCTLIST", SrcTransferProductList);

			try
			{
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> positionResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

				if (positionResult.size() > 0)
				{
					eventLog.info("Get Positions Info in CT_MQCPlanDetail by Source Transfer Product");
					List<String> positionList = CommonUtil.makeListBySqlResult(positionResult, "POSITION");
					modifyPosition = CommonUtil.toStringWithoutBrackets(positionList);
				}
			}
			catch (Exception ex)
			{
				eventLog.info("Error : Get Positions Info in CT_MQCPlanDetail by Source Transfer Product : " + ScrMQCJobName);
			}

		}

		return modifyPosition;
	}

	private String getModifyPositionforSourceRecycleMQCJob(String ScrMQCJobName, List<String> SrcTransferProductList, boolean isRecycleMQC) throws CustomException
	{
		String modifyPosition = "";

		if (isRecycleMQC)
		{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT DISTINCT E.POSITION, E.PRODUCTNAME ");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P ");
			sql.append(" WHERE 1 = 1 ");
			sql.append("   AND E.JOBNAME = :SRCJOBNAME ");
			sql.append("   AND P.PRODUCTNAME NOT IN ( :SOURCEPRODUCTLIST) ");
			sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME ");
			sql.append("   AND E.PROCESSFLOWNAME NOT IN (SELECT PROCESSFLOWNAME ");
			sql.append("                                   FROM PROCESSFLOW ");
			sql.append("                                  WHERE PROCESSFLOWTYPE = 'MQC') ");
			sql.append("ORDER BY TO_NUMBER(POSITION) ");

			Map<String, Object> args = new HashMap<String, Object>();
			args.put("SRCJOBNAME", ScrMQCJobName);
			args.put("SOURCEPRODUCTLIST", SrcTransferProductList);

			try
			{
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> positionResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

				if (positionResult.size() > 0)
				{
					eventLog.info("Get Positions Info in CT_MQCPlanDetail by Source Transfer Product");
					List<String> positionList = CommonUtil.makeListBySqlResult(positionResult, "POSITION");
					modifyPosition = CommonUtil.toStringWithoutBrackets(positionList);
				}
			}
			catch (Exception ex)
			{
				eventLog.info("Error : Get Positions Info in CT_MQCPlanDetail by Source Transfer Product : " + ScrMQCJobName);
			}
		}

		return modifyPosition;
	}

	private String getModifyPositionforDestMQCJob(String SrcMQCJobName, String DestMQCJobName, List<String> SrcTransferProductList, boolean isRecycleMQC) throws CustomException
	{
		String modifyPosition = "";

		if (!isRecycleMQC) // Main MQC Flow
		{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT DISTINCT P.POSITION, P.PRODUCTNAME ");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P   ");
			sql.append(" WHERE 1=1 ");
			sql.append("   AND E.JOBNAME = :SRCJOBNAME ");
			sql.append("   AND P.PRODUCTNAME IN ( :SRCPRODUCTLIST )   ");
			sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME ");
			sql.append("ORDER BY TO_NUMBER(POSITION) ");

			Map<String, Object> args = new HashMap<String, Object>();
			args.put("SRCJOBNAME", SrcMQCJobName);
			args.put("SRCPRODUCTLIST", SrcTransferProductList);

			try
			{
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> positionResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

				if (positionResult.size() > 0)
				{
					eventLog.info("Get Positions Info in CT_MQCPlanDetail by Destination Product");
					List<String> positionList = CommonUtil.makeListBySqlResult(positionResult, "POSITION");
					modifyPosition = CommonUtil.toStringWithoutBrackets(positionList);
				}
			}
			catch (Exception ex)
			{
				eventLog.info("Error : Get Positions Info in CT_MQCPlanDetail by Destination Product [jobName : " + DestMQCJobName + "]");
			}
		}
		else
		// Recycle MQC Flow
		{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT DISTINCT P.POSITION , P.PRODUCTNAME");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P ");
			sql.append(" WHERE 1 = 1 ");
			sql.append("   AND E.JOBNAME = :SRCJOBNAME ");
			sql.append("   AND P.PRODUCTNAME IN ( :SRCPRODUCTLIST) ");
			sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME ");
			sql.append("   AND E.PROCESSFLOWNAME IN (SELECT PROCESSFLOWNAME ");
			sql.append("                                FROM PROCESSFLOW ");
			sql.append("                               WHERE PROCESSFLOWTYPE = 'MQC') ");
			sql.append("ORDER BY TO_NUMBER(POSITION) ");

			Map<String, Object> args = new HashMap<String, Object>();
			args.put("SRCJOBNAME", SrcMQCJobName);
			args.put("SRCPRODUCTLIST", SrcTransferProductList);

			try
			{
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> positionResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

				if (positionResult.size() > 0)
				{
					eventLog.info("Get Positions Info in CT_MQCPlanDetail by Destination Product");
					List<String> positionList = CommonUtil.makeListBySqlResult(positionResult, "POSITION");
					modifyPosition = CommonUtil.toStringWithoutBrackets(positionList);
				}
			}
			catch (Exception ex)
			{
				eventLog.info("Error : Get Positions Info in CT_MQCPlanDetail by Destination Product [jobName : " + DestMQCJobName + "]");
			}

		}

		return modifyPosition;
	}

	private String getModifyPositionforDestRecycleMQCJob(String SrcMQCJobName, String DestMQCJobName, List<String> SrcTransferProductList, boolean isRecycleMQC) throws CustomException
	{
		String modifyPosition = "";

		if (isRecycleMQC)
		{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT DISTINCT P.POSITION , P.PRODUCTNAME");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P ");
			sql.append(" WHERE 1 = 1 ");
			sql.append("   AND E.JOBNAME = :SRCJOBNAME ");
			sql.append("   AND P.PRODUCTNAME IN ( :SRCPRODUCTLIST) ");
			sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME ");
			sql.append("   AND E.PROCESSFLOWNAME NOT IN (SELECT PROCESSFLOWNAME ");
			sql.append("                                   FROM PROCESSFLOW ");
			sql.append("                                  WHERE PROCESSFLOWTYPE = 'MQC') ");
			sql.append("ORDER BY TO_NUMBER(POSITION) ");

			Map<String, Object> args = new HashMap<String, Object>();
			args.put("SRCJOBNAME", SrcMQCJobName);
			args.put("SRCPRODUCTLIST", SrcTransferProductList);

			try
			{
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> positionResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

				if (positionResult.size() > 0)
				{
					eventLog.info("Get Positions Info in CT_MQCPlanDetail by Dest Product");
					List<String> positionList = CommonUtil.makeListBySqlResult(positionResult, "POSITION");
					modifyPosition = CommonUtil.toStringWithoutBrackets(positionList);
				}
			}
			catch (Exception ex)
			{
				eventLog.info("Error : Get Positions Info in CT_MQCPlanDetail by Dest Product [jobName : " + DestMQCJobName + "]");
			}
		}

		return modifyPosition;
	}

	private boolean isRecycleMQC(String sourceMQCJobName) throws CustomException
	{
		List<MQCPlan> mqcPlanData = ExtendedObjectProxy.getMQCPlanService().getRecyclingMQCPlanDataByLotName(sourceMQCJobName);

		if (mqcPlanData != null)
		{
			return true;
		}
		else
		{
			eventLog.info("Error : isRecycleMQC Check Query LotName : " + sourceMQCJobName);
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	private void changeReturnMQCBank(EventInfo eventInfo, Lot lotData, String jobName) throws CustomException
	{
		String currentNode = lotData.getNodeStack();
		String originalNode = currentNode.substring(0, currentNode.lastIndexOf("."));

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT NODEID, PROCESSFLOWNAME, PROCESSFLOWVERSION, NODEATTRIBUTE1, NODEATTRIBUTE2, FACTORYNAME ");
		sql.append("  FROM NODE ");
		sql.append(" WHERE NODEID = :NODEID ");
		sql.append("   AND NODETYPE = 'ProcessOperation' ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("NODEID", originalNode);

		List<Map<String, Object>> orginalNodeResult = null;
		try
		{
			orginalNodeResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		}
		catch (Exception e)
		{
			eventLog.info("Error occured - There is no ProcessFlow/ProcessOperation information for the organalNode.");
		}

		if (orginalNodeResult.size() > 0)
		{
			lotData.setNodeStack(originalNode);
			lotData.setProcessFlowName(ConvertUtil.getMapValueByName(orginalNodeResult.get(0), "PROCESSFLOWNAME"));
			lotData.setProcessFlowVersion(ConvertUtil.getMapValueByName(orginalNodeResult.get(0), "PROCESSFLOWVERSION"));
			lotData.setProcessOperationName(ConvertUtil.getMapValueByName(orginalNodeResult.get(0), "NODEATTRIBUTE1"));
			lotData.setProcessOperationVersion(ConvertUtil.getMapValueByName(orginalNodeResult.get(0), "NODEATTRIBUTE2"));
		}
		else
		{
			eventLog.info("Error occured - There is no ProcessFlow/ProcessOperation information for the organalNode.");
		}

		MQCPlan planData = ExtendedObjectProxy.getMQCPlanService().selectByKey(false, new Object[] { jobName });

		String FirstOperationName = "";
		String FirstOperationVer = "";

		Node targetNode = MESLotServiceProxy.getLotServiceUtil().getFirstNode(planData.getJobName(), planData.getFactoryName(), planData.getProcessFlowName(), planData.getProcessFlowVersion());

		if (targetNode == null)
		{
			FirstOperationName = planData.getReturnOperationName();
			FirstOperationVer = planData.getReturnOperationVersion();
		}
		else
		{
			FirstOperationName = targetNode.getNodeAttribute1();
			FirstOperationVer = targetNode.getNodeAttribute2();
		}

		try
		{
			if (StringUtils.isNotEmpty(planData.getPrepareSpecName()) && StringUtils.isNotEmpty(planData.getPrepareSpecVersion()))
			{
				// ChangeSpec
				ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo(lotData.getProductionType(), planData.getPrepareSpecName(), planData.getPrepareSpecVersion(), lotData.getProductSpec2Name(),
						lotData.getProductSpec2Version(), lotData.getProductRequestName(), lotData.getSubProductUnitQuantity1(), lotData.getSubProductUnitQuantity2(), lotData.getDueDate(),
						lotData.getPriority(), lotData.getFactoryName(), lotData.getAreaName(), lotData.getLotState(), lotData.getLotProcessState(), lotData.getLotHoldState(),
						lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), lotData.getNodeStack(),
						new ArrayList<ProductU>());

				changeSpecInfo.setUdfs(lotData.getUdfs());

				eventInfo.setEventName("SuspendMQC");
				MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);

				eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
			}

			// ChangeSpec: Return to MQC PrepareSpec or Not
			eventInfo.setEventName("SuspendMQC");
			ExtendedObjectProxy.getMQCPlanService().updateMQCPlanReturnOper(eventInfo, planData, "Suspending", FirstOperationName, FirstOperationVer);
		}
		catch (Exception e)
		{
			eventLog.info("Error occured - Suspending MQC Lot for Split Lot");
		}

	}

	boolean ExistMQCProductbyLot(Lot LotData, String JobName) throws CustomException
	{
		boolean ExistJobfiag = false;

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT E.PRODUCTNAME  ");
		sql.append("  FROM CT_MQCPLAN M, CT_MQCPLANDETAIL D, CT_MQCPLANDETAIL_EXTENDED E    ");
		sql.append(" WHERE MQCSTATE IN ('Released','Recycling')   ");
		sql.append("   AND M.JOBNAME = D.JOBNAME  ");
		sql.append("   AND D.JOBNAME = E.JOBNAME  ");
		sql.append("   AND M.JOBNAME = E.JOBNAME ");
		sql.append("   AND M.LOTNAME = :LOTNAME  ");
		sql.append("   AND M.JOBNAME = :JOBNAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("LOTNAME", LotData.getKey().getLotName());
		args.put("JOBNAME", JobName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
			ExistJobfiag = true;

		return ExistJobfiag;
	}

	private void removeMQCJob(EventInfo eventInfo, Lot sourceLotData) throws CustomException
	{
		List<MQCPlan> jobList = null;

		try
		{
			jobList = ExtendedObjectProxy.getMQCPlanService().select(" LotName = ? ", new Object[] { sourceLotData.getKey().getLotName() });
		}
		catch (Exception e)
		{
		}

		if (jobList != null)
		{
			for (MQCPlan planData : jobList)
			{
				String jobName = planData.getJobName();
				eventInfo = EventInfoUtil.makeEventInfo("RemoveMQC", getEventUser(), getEventComment(), null, null);

				eventLog.info(" Start delete MQCJob from  CT_MQCPLANDETAIL_EXTENDED");
				ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().deleteMQCPlanDetail_ExtendedByJobName(jobName);

				eventLog.info(" End delete MQCJob from  CT_MQCPLANDETAIL_EXTENDED");

				List<MQCPlanDetail> DetailPlanList;
				try
				{
					DetailPlanList = ExtendedObjectProxy.getMQCPlanDetailService().select("jobName = ?", new Object[] { jobName });
				}
				catch (Exception ex)
				{
					eventLog.error("No details for this MQC plan");
					DetailPlanList = new ArrayList<MQCPlanDetail>();
				}

				// purge dependent plans
				for (MQCPlanDetail detailPlanData : DetailPlanList)
				{
					ExtendedObjectProxy.getMQCPlanDetailService().deleteMQCPlanDetail(eventInfo, detailPlanData);
				}

				ExtendedObjectProxy.getMQCPlanService().deleteMQCPlanData(eventInfo, planData);

				try
				{
					SetEventInfo setEventInfo = MESLotServiceProxy.getLotInfoUtil().setEventInfo(sourceLotData, 0, new ArrayList<ProductU>());
					LotServiceProxy.getLotService().setEvent(sourceLotData.getKey(), eventInfo, setEventInfo);
				}
				catch (Exception e)
				{
				}
			}
		}
	}

	private boolean isMQCwithReserveEQP(String sourceMQCJobName)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT M.JOBNAME  ");
		sql.append("  FROM CT_MQCPLAN M, CT_MQCPLANDETAIL D   ");
		sql.append(" WHERE MQCSTATE IN ('Released','Recycling')  ");
		sql.append("   AND M.JOBNAME = D.JOBNAME ");
		sql.append("   AND D.MACHINENAME IS NOT NULL ");
		sql.append("   AND D.RECIPENAME IS NOT NULL ");
		sql.append("   AND M.JOBNAME = :JOBNAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("JOBNAME", sourceMQCJobName);

		try
		{
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (result.size() > 0)
				return true;
		}
		catch (Exception ex)
		{
			eventLog.info("Error : isMQCwithReserveEQP Check Query LotName : " + sourceMQCJobName);
			return false;
		}

		return false;
	}

	private String getActualPositionforReserveEQP(String ScrMQCJobName, List<String> SrcTransferProductList, String ProcessFlowName, String ProcessOperationName) throws CustomException
	{
		String modifyPosition = "";

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT P.POSITION, P.PRODUCTNAME   ");
		sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P   ");
		sql.append(" WHERE E.PRODUCTNAME = P.PRODUCTNAME  ");
		sql.append("   AND E.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND E.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME   ");
		sql.append("   AND E.JOBNAME = :SRCJOBNAME   ");
		sql.append("   AND P.PRODUCTNAME NOT IN ( :SOURCEPRODUCTLIST )   ");
		sql.append("ORDER BY TO_NUMBER(POSITION)  ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("SRCJOBNAME", ScrMQCJobName);
		args.put("SOURCEPRODUCTLIST", SrcTransferProductList);
		args.put("PROCESSFLOWNAME", ProcessFlowName);
		args.put("PROCESSOPERATIONNAME", ProcessOperationName);

		try
		{
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> positionResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (positionResult.size() > 0)
			{
				eventLog.info("Get Positions Info in CT_MQCPlanDetail by Source Transfer Product");
				List<String> positionList = CommonUtil.makeListBySqlResult(positionResult, "POSITION");
				modifyPosition = CommonUtil.toStringWithoutBrackets(positionList);
			}
		}
		catch (Exception ex)
		{
			eventLog.info("Error : Get Positions Info in CT_MQCPlanDetail by Source Transfer Product [jobName : " + ScrMQCJobName + "]");
		}

		return modifyPosition;
	}

	private Element setCreatedLotList(Lot lotData)
	{
		Element eleLot = new Element("LOT");

		try
		{
			XmlUtil.addElement(eleLot, "LOTNAME", lotData.getKey().getLotName());
			XmlUtil.addElement(eleLot, "DURABLENAME", lotData.getCarrierName());
			XmlUtil.addElement(eleLot, "PRODUCTSPECNAME", lotData.getProductSpecName());
			XmlUtil.addElement(eleLot, "PRODUCTSPECVERSION", lotData.getProductSpecVersion());
			XmlUtil.addElement(eleLot, "PROCESSFLOWNAME", lotData.getProcessFlowName());
			XmlUtil.addElement(eleLot, "PROCESSFLOWVERSION", lotData.getProcessFlowVersion());
			XmlUtil.addElement(eleLot, "PRODUCTIONTYPE", lotData.getProductionType());
			XmlUtil.addElement(eleLot, "PRODUCTQUANTITY", Integer.toString((int) lotData.getProductQuantity()));
			XmlUtil.addElement(eleLot, "DUEDATE", lotData.getDueDate().toString());
		}
		catch (Exception ex)
		{
			eventLog.warn(String.format("Scribing Lot[%s] is failed so that skip", lotData.getKey().getLotName()));
		}

		return eleLot;
	}

	private Lot RecoverySpec(EventInfo eventInfo, List<ProductP> productPSequence, Lot lotData) throws CustomException
	{
		lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getKey().getLotName());

		DummyProductAssign dummyProdAssignData = ExtendedObjectProxy.getDummyProductAssignService().getDummyProductAssignData(productPSequence.get(0).getProductName());

		String returnProductSpecName = dummyProdAssignData.getReturnProductSpecName();
		String returnProductSpecVersion = dummyProdAssignData.getReturnProductSpecVersion();
		String returnProcessFlowName = dummyProdAssignData.getReturnProcessFlowName();
		String returnProcessFlowVersion = dummyProdAssignData.getReturnProcessFlowVersion();
		String returnProductRequestName = dummyProdAssignData.getReturnProductRequestName();

		ProductSpec specData = GenericServiceProxy.getSpecUtil().getProductSpec(lotData.getFactoryName(), returnProductSpecName, returnProductSpecVersion);
		String nodeStack = CommonUtil.getNodeStack(lotData.getFactoryName(), returnProcessFlowName, returnProcessFlowVersion, lotData.getProcessOperationName(), lotData.getProcessOperationVersion());
		List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUSequence(lotData.getKey().getLotName());
		
		ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotData, lotData.getAreaName(), lotData.getDueDate(), lotData.getFactoryName(), lotData.getLotHoldState(),
				lotData.getLotProcessState(), lotData.getLotState(), nodeStack, lotData.getPriority(), returnProcessFlowName, returnProcessFlowVersion, lotData.getProcessOperationName(),
				lotData.getProcessOperationVersion(), lotData.getProcessOperationName(), lotData.getProductionType(), returnProductRequestName, "", "", returnProductSpecName,
				returnProductSpecVersion, productUdfs, specData.getSubProductUnitQuantity1(), specData.getSubProductUnitQuantity2());

		changeSpecInfo.getUdfs().put("OLDPRODUCTREQUESTNAME", lotData.getProductRequestName());
		
		eventInfo = EventInfoUtil.makeEventInfo("ChangeOper", getEventUser(), getEventComment(), "", "");

		Lot newLotData = MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);

		List<ProductPGS> productPGSList = new ArrayList<ProductPGS>();
		for (ProductP productP : productPSequence)
		{
			String productName = productP.getProductName();
			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

			List<DummyProductAssign> dataInfoList = ExtendedObjectProxy.getDummyProductAssignService().getDummyProductAssignRecoverInfo(productName, newLotData);

			if (dataInfoList != null)
			{
				for (DummyProductAssign dataInfo : dataInfoList)
					ExtendedObjectProxy.getDummyProductAssignService().remove(eventInfo, dataInfo);
			}
			else
			{
				throw new CustomException("LOT-0397", productName);
			}

			ProductPGS productPGS = new ProductPGS();

			productPGS.setProductName(productData.getKey().getProductName());
			productPGS.setProductGrade("S");
			productPGS.setPosition(productData.getPosition());

			productPGSList.add(productPGS);
		}

		// Change Grade "S"
		eventInfo.setEventName("ChangeGrade");
		ChangeGradeInfo changeGradeInfo = MESLotServiceProxy.getLotInfoUtil().changeGradeInfo(newLotData, "S", productPGSList);
		newLotData = MESLotServiceProxy.getLotServiceImpl().ChangeGrade(eventInfo, newLotData, changeGradeInfo);

		return newLotData;
	}

	private void decrementQuantity(EventInfo eventInfo, Lot lotData, int incrementQty) throws CustomException
	{
		ProductRequest workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());

		IncrementReleasedQuantityByInfo incrementReleasedQuantityByInfo = new IncrementReleasedQuantityByInfo();
		incrementReleasedQuantityByInfo.setQuantity(-incrementQty);

		int createdQty = Integer.parseInt(workOrderData.getUdfs().get("CREATEDQUANTITY")) - incrementQty;

		incrementReleasedQuantityByInfo.getUdfs().put("CREATEDQUANTITY", Integer.toString(createdQty));


		// Increment Release Qty
		eventInfo.setEventName("DecreamentQuantity");
		workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementReleasedQuantityBy(workOrderData, incrementReleasedQuantityByInfo, eventInfo);
	}

	private void holdLot(Lot lotData) throws CustomException
	{
		// Hold
		EventInfo scrapEventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment(), "", "");
		scrapEventInfo.setReasonCode("Return[TPtoDummy]");
		scrapEventInfo.setReasonCodeType("System");
		scrapEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		Map<String, String> udfs = new HashMap<String, String>();

		if (StringUtil.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Released) && StringUtil.equals(lotData.getLotProcessState(), "WAIT"))
		{
			MESLotServiceProxy.getLotServiceImpl().lotMultiHold(scrapEventInfo, lotData, udfs);
		}
		else
		{
			throw new CustomException("LOT-0113", lotData.getLotState(), lotData.getLotProcessState());
		}
	}
	
	private void incrementQuantity(EventInfo eventInfo, Lot lotData, int incrementQty) throws CustomException
	{
		ProductRequest workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());

		IncrementReleasedQuantityByInfo incrementReleasedQuantityByInfo = new IncrementReleasedQuantityByInfo();
		incrementReleasedQuantityByInfo.setQuantity(incrementQty);

		int createdQty = Integer.parseInt(workOrderData.getUdfs().get("CREATEDQUANTITY")) + incrementQty;

		incrementReleasedQuantityByInfo.getUdfs().put("CREATEDQUANTITY", Integer.toString(createdQty));

		// Increment Release Qty
		eventInfo.setEventName("IncreamentQuantity");
		workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementReleasedQuantityBy(workOrderData, incrementReleasedQuantityByInfo, eventInfo);

		if (workOrderData.getPlanQuantity() < workOrderData.getReleasedQuantity())
			throw new CustomException("PRODUCTREQUEST-0026", String.valueOf(workOrderData.getPlanQuantity()), String.valueOf(workOrderData.getReleasedQuantity()));
	}
}