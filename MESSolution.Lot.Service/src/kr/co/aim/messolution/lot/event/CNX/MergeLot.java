package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
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
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class MergeLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String destLotName = SMessageUtil.getBodyItemValue(doc, "DEST_LOTNAME", true);
		String destinationDurableName = SMessageUtil.getBodyItemValue(doc, "DEST_DURABLENAME", false);

		Lot desLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(destLotName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		List<String> srcLotList = new ArrayList<String>();
		List<String> destLotList = new ArrayList<String>();

		destLotList.add(destLotName);

		// validation
		CommonValidation.checkLotState(desLotData);
		CommonValidation.checkLotProcessState(desLotData);
		CommonValidation.checkLotHoldState(desLotData);
		CommonValidation.checkJobDownFlag(desLotData);
		CommonValidation.checkOriginalProduct(desLotData);
		
		// 2020-11-14	dhko	Add Validation
		CommonValidation.checkProcessInfobyString(destLotName);
		
		if (!desLotData.getProcessGroupName().isEmpty())
			throw new CustomException("LOT-0093");

		if (!destinationDurableName.isEmpty())
		{
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(destinationDurableName);
			CommonValidation.CheckDurableHoldState(durableData);
		}

		// CM-CM-0013-01
		// Split operation not possible if the array is MQC.
		if (StringUtils.equals(desLotData.getFactoryName(), "ARRAY") && "M".equals(desLotData.getProductionType()))
		{
			// Only can Merge at Prepare Flow && no MQCPlan
			ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(desLotData);
			if (!StringUtils.equals(processFlowData.getProcessFlowType(), "MQCPrepare"))
			{
				throw new CustomException("SORT-0010", processFlowData.getProcessFlowType());
			}
			else
			{
				List<MQCPlan> mqcPlanData = ExtendedObjectProxy.getMQCPlanService().getMQCPlanDataByLotName(desLotData.getKey().getLotName());

				if (mqcPlanData != null)
					throw new CustomException("SORT-0011", desLotData.getKey().getLotName());
			}			
		}

		Element eleBody = SMessageUtil.getBodyElement(doc);
		List<Element> eleSourceDurableList = SMessageUtil.getSubSequenceItemList(eleBody, "SOURCE_DURABLELIST", true);

		List<Element> eleDestiProductList = null;
		try
		{
			eleDestiProductList = SMessageUtil.getSubSequenceItemList(eleBody, "DESTINATIONPRODUCTLIST", true);

		}
		catch (Exception e)
		{
		}

		List<String> prodList = new ArrayList<String>();

		for (Element eleSourceDurable : eleSourceDurableList)
		{
			String durableName = SMessageUtil.getChildText(eleSourceDurable, "DURABLENAME", true);

			Durable sourceDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);

			CommonValidation.CheckDurableHoldState(sourceDurableData);

			List<Element> eleProductList = SMessageUtil.getSubSequenceItemList(eleSourceDurable, "PRODUCTLIST", true);

			List<Element> eleSrcProductList = null;

			try
			{
				eleSrcProductList = SMessageUtil.getSubSequenceItemList(eleBody, durableName, false);
			}
			catch (Exception e)
			{
			}

			List<ProductP> productPSequence = new ArrayList<ProductP>();
			for (Element eleProduct : eleProductList)
			{
				String productName = SMessageUtil.getChildText(eleProduct, "PRODUCTNAME", true);
				int position = Integer.parseInt(SMessageUtil.getChildText(eleProduct, "POSITION", true));
				String slotPosition = SMessageUtil.getChildText(eleProduct, "SLOTPOSITION", false);
				Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);

				Map<String, String> udfs = new HashMap<String, String>();
				
				if (StringUtils.isNotEmpty(slotPosition))
				{
					udfs.put("SLOTPOSITION", slotPosition);
				}

				ProductP productP = new ProductP();
				productP.setProductName(productData.getKey().getProductName());
				productP.setPosition(position);
				productP.setUdfs(udfs);
				productPSequence.add(productP);

				if (StringUtils.isNotEmpty(productName))
					prodList.add(productName);
			}

			Lot sourceLotData = null;
			try
			{
				sourceLotData = MESLotServiceProxy.getLotInfoUtil().getLotInfoBydurableName(durableName);
			}
			catch (CustomException ce)
			{
			}

			if (sourceLotData != null)
			{
				CommonValidation.checkJobDownFlag(sourceLotData);
				CommonValidation.checkOriginalProduct(desLotData);
				
				// 2020-11-14	dhko	Add Validation
				CommonValidation.checkProcessInfobyString(sourceLotData.getKey().getLotName());
			}

			if (eleSrcProductList != null)
			{
				String sourLotGrade = CommonUtil.judgeLotGradeByProductList(eleSrcProductList, "PRODUCTJUDGE");
				MESLotServiceProxy.getLotServiceImpl().updateLotData("LOTGRADE", sourLotGrade, sourceLotData.getKey().getLotName());
			}

			if (eleDestiProductList != null)
			{
				String desLotGrade = CommonUtil.judgeLotGradeByProductList(eleDestiProductList, "PRODUCTJUDGE");
				MESLotServiceProxy.getLotServiceImpl().updateLotData("LOTGRADE", desLotGrade, destLotName);
			}

			// Merge Lot
			TransferProductsToLotInfo transitionInfo = MESLotServiceProxy.getLotInfoUtil().transferProductsToLotInfo(desLotData.getKey().getLotName(), eleProductList.size(), productPSequence,
					desLotData.getUdfs(), new HashMap<String, String>());

			eventInfo.setEventName("Merge");
			sourceLotData = MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, sourceLotData, transitionInfo);

			srcLotList.add(sourceLotData.getKey().getLotName());

			for (Element eleSourceProduct : eleProductList)
			{
				String productName = SMessageUtil.getChildText(eleSourceProduct, "PRODUCTNAME", true);

				// update CT_PRODUCTQUEUETIME Table
				StringBuffer sql = new StringBuffer();
				sql.append("UPDATE CT_PRODUCTQUEUETIME ");
				sql.append("   SET LOTNAME = :LOTNAME ");
				sql.append(" WHERE PRODUCTNAME = :PRODUCTNAME ");

				Map<String, Object> args = new HashMap<String, Object>();
				args.put("LOTNAME", destLotName);
				args.put("PRODUCTNAME", productName);

				GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), args);
			}

			ProductSpec baseData = GenericServiceProxy.getSpecUtil().getProductSpec(factoryName, sourceLotData.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);

			if (CommonUtil.equalsIn(baseData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare"))
			{
				eventLog.info("ProductSpecGroup: " + baseData.getUdfs().get("PRODUCTSPECGROUP"));
				boolean ExistMQCJobSrcLot = false;
				boolean ExistMQCJobDestLot = false;

				ExistMQCJobSrcLot = ExistMQCJobLot(sourceLotData);
				ExistMQCJobDestLot = ExistMQCJobLot(desLotData);

				List<String> destProductList = getProductListByElementList(eleDestiProductList);

				// Check Exist Job (SrcLot & DestLot)
				if (ExistMQCJobSrcLot && ExistMQCJobDestLot)
				{
					eventLog.info("exist MQC Job Info by ScrLot, DestLot");
					// inherit MQCJob (SrcLot -> DestLot)
					inheritSourceLotMQCJob(sourceLotData, desLotData, prodList, destProductList, destinationDurableName, eventInfo);
				}
				else
				{
					eventLog.info("Not exist to inherit MQC Job Info by ScrLot");
				}
			}

			if (sourceLotData.getProductQuantity() == 0)
			{
				Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();
				Durable sLotDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sourceLotData.getCarrierName());
				deassignCarrierUdfs = sLotDurableData.getUdfs();

				if (StringUtil.isNotEmpty(sourceLotData.getCarrierName()))
				{
					eventInfo.setEventTimeKey(ConvertUtil.getCurrTimeKey());
					DeassignCarrierInfo deassignCarrierInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(sourceLotData, sLotDurableData, new ArrayList<ProductU>());

					// Deassign Carrier
					eventInfo.setEventName("DeassignCarrier");
					MESLotServiceProxy.getLotServiceImpl().deassignCarrier(sourceLotData, deassignCarrierInfo, eventInfo);
				}

				// Make Emptied
				eventInfo.setEventTimeKey(ConvertUtil.getCurrTimeKey());
				eventInfo.setEventName("MakeEmptied");
				MESLotServiceProxy.getLotServiceImpl().MakeEmptied(eventInfo, sourceLotData, new ArrayList<ProductU>(), deassignCarrierUdfs);

				deassignCarrierUdfs.clear();

				// Remove MQC Job When Source Lot is Emptied
				if (CommonUtil.equalsIn(baseData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare"))
				{
					eventLog.info("ProductSpecGroup: " + baseData.getUdfs().get("PRODUCTSPECGROUP"));
					removeMQCJob(sourceLotData);
				}
			}
		}

		// Set Data(Sample, FutureAction) Transfer Product
		MESLotServiceProxy.getLotServiceUtil().transferProductSyncData(eventInfo, srcLotList, destLotList);

		// Set Data(MainReserveSkip) New Lot - Lot AR-AMF-0030-01
		ExtendedObjectProxy.getMainReserveSkipService().syncMainReserveSkip(eventInfo, srcLotList, destLotList);

		return doc;
	}

	public List<Element> getProductElementListByListProduct(List<Product> ProductList, List<Element> productElList)
	{
		List<Element> resultList = new ArrayList<>();

		resultList.addAll(productElList);

		for (Product product : ProductList)
		{
			Element productEl = new Element("PRODUCT");

			Element productNameEl = new Element("PRODUCTNAME");
			productNameEl.setText(product.getKey().getProductName());

			Element productGradeEl = new Element("PRODUCTJUDGE");
			productGradeEl.setText(product.getProductGrade());

			productEl.addContent(productNameEl);
			productEl.addContent(productGradeEl);

			resultList.add(productEl);
		}

		return resultList;
	}

	public List<String> getProductListByElementList(List<Element> eleDestProductList) throws CustomException
	{
		List<String> resultList = new ArrayList<>();

		for (Element eleProduct : eleDestProductList)
		{
			String productName = SMessageUtil.getChildText(eleProduct, "PRODUCTNAME", true);

			if (StringUtils.isNotEmpty(productName))
			{
				resultList.add(productName);
			}
		}

		return resultList;
	}

	public List<Product> remainProductList(List<Product> sourceList, List<Element> destinationList)
	{
		for (Iterator iterator = sourceList.iterator(); iterator.hasNext();)
		{
			Product product = (Product) iterator.next();
			for (Element elementDest : destinationList)
			{
				if (product.getKey().getProductName().equals(elementDest.getChildText("PRODUCTNAME")))
				{
					iterator.remove();
					break;
				}
			}
		}

		return sourceList;
	}

	boolean ExistMQCJobLot(Lot LotData) throws CustomException
	{
		boolean ExistJobfiag = false;

		List<MQCPlan> mqcPlanData = ExtendedObjectProxy.getMQCPlanService().getMQCPlanDataByLotNameMultiState(LotData.getKey().getLotName());

		if (mqcPlanData != null)
			ExistJobfiag = true;

		return ExistJobfiag;
	}

	private void inheritSourceLotMQCJob(Lot sourceLotData, Lot destinationLotData, List<String> SrcTransferProductList, List<String> DestProductList, String destCarrierName, EventInfo eventInfo)
			throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		Map<String, Object> args = new HashMap<String, Object>();

		String SrcMQCJobName = getMQCJobName(sourceLotData);
		String DestMQCJobName = getMQCJobName(destinationLotData);

		if (!SrcMQCJobName.isEmpty() && !DestMQCJobName.isEmpty())
		{
			boolean isRecycleMQC = false;
			boolean isMQCwithReserveEQP = false;

			isRecycleMQC = isRecycleMQC(SrcMQCJobName);
			isMQCwithReserveEQP = isMQCwithReserveEQP(SrcMQCJobName);

			// Update MQCPlanDetail
			List<MQCPlanDetail> dPlanDetailList = new ArrayList<MQCPlanDetail>();
			List<MQCPlanDetail> sPlanDetailList = new ArrayList<MQCPlanDetail>();
			List<MQCPlanDetail> srcPlanDetailList = new ArrayList<MQCPlanDetail>();
			List<MQCPlanDetail> destPlanDetailList = new ArrayList<MQCPlanDetail>();

			sql.setLength(0);
			sql.append("SELECT DISTINCT P.POSITION, P.PRODUCTNAME  ");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P  ");
			sql.append(" WHERE E.PRODUCTNAME = P.PRODUCTNAME  ");
			sql.append("   AND E.JOBNAME = :DESTJOBNAME  ");
			sql.append("   AND P.PRODUCTNAME IN ( :DESTPRODUCTLIST )  ");
			sql.append("UNION ");
			sql.append("SELECT DISTINCT P.POSITION, P.PRODUCTNAME  ");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P  ");
			sql.append(" WHERE E.PRODUCTNAME = P.PRODUCTNAME  ");
			sql.append("   AND E.JOBNAME = :SRCJOBNAME  ");
			sql.append("   AND P.PRODUCTNAME IN ( :SOURCEPRODUCTLIST )  ");
			sql.append("ORDER BY POSITION ");

			args.clear();
			args.put("DESTJOBNAME", DestMQCJobName);
			args.put("DESTPRODUCTLIST", DestProductList);
			args.put("SRCJOBNAME", SrcMQCJobName);
			args.put("SOURCEPRODUCTLIST", SrcTransferProductList);

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> positionResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (positionResult.size() > 0)
			{
				eventLog.info("exist MQC Product Info by Product");
				List<String> positionList = CommonUtil.makeListBySqlResult(positionResult, "POSITION");
				String actualPosition = CommonUtil.toStringWithoutBrackets(positionList);
				String modifySrcPositionforMain = getModifyPositionforSourceMQCJob(SrcMQCJobName, SrcTransferProductList, isRecycleMQC);
				String modifySrcPositionforRecycle = getModifyPositionforSourceRecycleMQCJob(SrcMQCJobName, SrcTransferProductList, isRecycleMQC);
				String modifyDestPositionforMain = getModifyPositionforDestMQCJob(SrcMQCJobName, DestMQCJobName, SrcTransferProductList, DestProductList, isRecycleMQC);
				String modifyDestPositionforRecycle = getModifyPositionforDestRecycleMQCJob(SrcMQCJobName, DestMQCJobName, SrcTransferProductList, DestProductList, isRecycleMQC);

				sql.setLength(0);
				sql.append("SELECT DISTINCT  ");
				sql.append("       D.JOBNAME,  ");
				sql.append("       D.PROCESSFLOWNAME,  ");
				sql.append("       D.PROCESSFLOWVERSION,  ");
				sql.append("       D.PROCESSOPERATIONNAME,  ");
				sql.append("       D.PROCESSOPERATIONVERSION, ");
				sql.append("       E.MACHINENAME, ");
				sql.append("       E.RECIPENAME ");
				sql.append("  FROM CT_MQCPLANDETAIL D, CT_MQCPLANDETAIL_EXTENDED E  ");
				sql.append(" WHERE D.JOBNAME = E.JOBNAME  ");
				sql.append("   AND D.PROCESSFLOWNAME = E.PROCESSFLOWNAME  ");
				sql.append("   AND D.PROCESSFLOWVERSION = E.PROCESSFLOWVERSION  ");
				sql.append("   AND D.PROCESSOPERATIONNAME = E.PROCESSOPERATIONNAME  ");
				sql.append("   AND D.PROCESSOPERATIONVERSION = E.PROCESSOPERATIONVERSION  ");
				sql.append("   AND D.JOBNAME = :SRCJOBNAME  ");
				sql.append("   AND E.PRODUCTNAME IN ( :PRODUCTLIST )  ");

				args.clear();
				args.put("SRCJOBNAME", SrcMQCJobName);
				args.put("PRODUCTLIST", SrcTransferProductList);

				@SuppressWarnings("unchecked")
				List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

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

							MQCPlanDetail destPlanDetail = ExtendedObjectProxy.getMQCPlanDetailService().selectByKey(false,
									new Object[] { DestMQCJobName, processFlowName, processFlowVersion, processOperationName, processOperationVersion });

							if (!machineName.isEmpty() || !recipeName.isEmpty())
							{
								destPlanDetail.setMachineName(machineName);
								destPlanDetail.setRecipeName(recipeName);
							}

							destPlanDetailList.add(destPlanDetail);

							MQCPlanDetail srcPlanDetail = ExtendedObjectProxy.getMQCPlanDetailService().selectByKey(false,
									new Object[] { SrcMQCJobName, processFlowName, processFlowVersion, processOperationName, processOperationVersion });

							srcPlanDetailList.add(srcPlanDetail);
						}

						for (MQCPlanDetail oldPlanDetail : destPlanDetailList)
						{
							MQCPlanDetail planDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);
							planDetail.setJobName(DestMQCJobName);
							planDetail.setLotName(destinationLotData.getKey().getLotName());
							planDetail.setCarrierName(destCarrierName);
							planDetail.setLastEventTime(eventInfo.getEventTime());
							planDetail.setLastEventName(eventInfo.getEventName());
							planDetail.setLastEventUser(eventInfo.getEventUser());
							planDetail.setLastEventComment(eventInfo.getEventComment());

							if (isMQCwithReserveEQP)
							{
								String actualPositionforReserveEQP = getActualPositionforReserveEQP(SrcMQCJobName, SrcTransferProductList, DestMQCJobName, DestProductList,
										planDetail.getProcessFlowName(), planDetail.getProcessOperationName());
								planDetail.setPosition(actualPositionforReserveEQP);
							}
							else
							{
								planDetail.setPosition(actualPosition);
							}

							dPlanDetailList.add(planDetail);
						}

						for (MQCPlanDetail oldPlanDetail : srcPlanDetailList)
						{
							MQCPlanDetail SrcPlanDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);

							SrcPlanDetail.setLastEventTime(eventInfo.getEventTime());
							SrcPlanDetail.setLastEventName(eventInfo.getEventName());
							SrcPlanDetail.setLastEventUser(eventInfo.getEventUser());
							SrcPlanDetail.setLastEventComment(eventInfo.getEventComment());
							SrcPlanDetail.setPosition(modifySrcPositionforMain);

							sPlanDetailList.add(SrcPlanDetail);
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

							MQCPlanDetail destPlanDetail = ExtendedObjectProxy.getMQCPlanDetailService().selectByKey(false,
									new Object[] { DestMQCJobName, processFlowName, processFlowVersion, processOperationName, processOperationVersion });

							destPlanDetailList.add(destPlanDetail);

							MQCPlanDetail srcPlanDetail = ExtendedObjectProxy.getMQCPlanDetailService().selectByKey(false,
									new Object[] { SrcMQCJobName, processFlowName, processFlowVersion, processOperationName, processOperationVersion });

							srcPlanDetailList.add(srcPlanDetail);
						}

						if (OnlyRecycleFlag)
						{
							eventLog.info("Transfer product from SourceLot is Only in Recycle Flow");
							for (MQCPlanDetail oldPlanDetail : destPlanDetailList)
							{
								MQCPlanDetail planDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);
								planDetail.setJobName(DestMQCJobName);
								planDetail.setLotName(destinationLotData.getKey().getLotName());
								planDetail.setCarrierName(destCarrierName);
								planDetail.setLastEventTime(eventInfo.getEventTime());
								planDetail.setLastEventName(eventInfo.getEventName());
								planDetail.setLastEventUser(eventInfo.getEventUser());
								planDetail.setLastEventComment(eventInfo.getEventComment());
								planDetail.setPosition(modifyDestPositionforRecycle);

								dPlanDetailList.add(planDetail);
							}

							for (MQCPlanDetail oldPlanDetail : srcPlanDetailList)
							{
								MQCPlanDetail SrcPlanDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);

								SrcPlanDetail.setLastEventTime(eventInfo.getEventTime());
								SrcPlanDetail.setLastEventName(eventInfo.getEventName());
								SrcPlanDetail.setLastEventUser(eventInfo.getEventUser());
								SrcPlanDetail.setLastEventComment(eventInfo.getEventComment());
								SrcPlanDetail.setPosition(modifySrcPositionforRecycle);

								sPlanDetailList.add(SrcPlanDetail);
							}
						}
						else
						{
							eventLog.info("Transfer product from SourceLot is not Only in Recycle Flow");
							for (MQCPlanDetail oldPlanDetail : destPlanDetailList)
							{
								MQCPlanDetail DestPlanDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);

								ProcessFlowKey processFlowKey = new ProcessFlowKey();
								processFlowKey.setFactoryName(destinationLotData.getFactoryName());
								processFlowKey.setProcessFlowName(DestPlanDetail.getProcessFlowName());
								processFlowKey.setProcessFlowVersion(DestPlanDetail.getProcessFlowVersion());
								ProcessFlow DestPlanProcessFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);

								DestPlanDetail.setJobName(DestMQCJobName);
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

								dPlanDetailList.add(DestPlanDetail);

							}

							for (MQCPlanDetail oldPlanDetail : srcPlanDetailList)
							{
								MQCPlanDetail SrcPlanDetail = (MQCPlanDetail) ObjectUtil.copyTo(oldPlanDetail);

								ProcessFlowKey processFlowKey = new ProcessFlowKey();
								processFlowKey.setFactoryName(destinationLotData.getFactoryName());
								processFlowKey.setProcessFlowName(SrcPlanDetail.getProcessFlowName());
								processFlowKey.setProcessFlowVersion(SrcPlanDetail.getProcessFlowVersion());
								ProcessFlow SrcPlanProcessFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);

								SrcPlanDetail.setLastEventTime(eventInfo.getEventTime());
								SrcPlanDetail.setLastEventName(eventInfo.getEventName());
								SrcPlanDetail.setLastEventUser(eventInfo.getEventUser());
								SrcPlanDetail.setLastEventComment(eventInfo.getEventComment());

								if (StringUtil.equalsIgnoreCase(SrcPlanProcessFlowData.getProcessFlowType(), "MQC"))
									SrcPlanDetail.setPosition(modifySrcPositionforMain); // set Positions for MainMQCFlow
								else
									SrcPlanDetail.setPosition(modifySrcPositionforRecycle); // set Positions for RecycleFlow

								sPlanDetailList.add(SrcPlanDetail);
							}
						}
					}

					if (sPlanDetailList.size() > 0)
					{
						// Modify Source MQC Job in CT_MQCPlanDetail by Source Transfer ProductList
						try
						{
							for (MQCPlanDetail PlanDetail : sPlanDetailList)
							{
								ExtendedObjectProxy.getMQCPlanDetailService().modify(eventInfo, PlanDetail);
							}

							eventLog.info(sPlanDetailList.size() + " Rows Modified into CT_MQCPlanDetail for Source MQC Job");
						}
						catch (Exception ex)
						{
							eventLog.info("Error : Rows Modified into CT_MQCPlanDetail");
						}
					}

					if (dPlanDetailList.size() > 0)
					{
						// Update MQCPlanDetail
						try
						{
							for (MQCPlanDetail PlanDetail : dPlanDetailList)
							{
								ExtendedObjectProxy.getMQCPlanDetailService().modify(eventInfo, PlanDetail);
							}
							eventLog.info(dPlanDetailList.size() + " Rows modified into CT_MQCPlanDetail for Destination MQC Job");
						}
						catch (Exception ex)
						{
							eventLog.info("Error : Rows inserted into CT_MQCPlanDetail");
						}
					}

					int InsRows = 0;
					for (String SourceProduct : SrcTransferProductList)
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
								args.put("JOBNAME", DestMQCJobName);
								args.put("LOTNAME", destinationLotData.getKey().getLotName());
								args.put("POSITION", position);
								args.put("EVENTNAME", eventInfo.getEventName());
								args.put("EVENTTIME", eventInfo.getEventTime().toString());
								args.put("EVENTUSER", eventInfo.getEventUser());
								args.put("OLDJOBNAME", SrcMQCJobName);
								args.put("PRODUCTNAME", productName);

								GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), args);
								InsRows++;
							}
						}
					}

					eventLog.info(InsRows + " Rows inserted into CT_MQCPlanDetail_Extended");

					// Delete Source MQC Job in MQCPlanDetail_Extended by Source Transfer ProductList
					ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().deleteMQCPlanDetail_ExtendedByProdList(SrcMQCJobName, SrcTransferProductList);

					// Lot without MQCJob is sent to MQC Bank.
					if (!ExistMQCProductbyLot(sourceLotData, SrcMQCJobName))
					{
						eventLog.info("Source Lot [" + sourceLotData + "] does not have MQC Product.");
						// EventInfo eventInfo , MQCPlan planData, Lot lotData
						changeReturnMQCBank(eventInfo, sourceLotData, SrcMQCJobName);

						// After being sent to the MQC bank, Delete MQC Job
						if (CommonUtil.equalsIn(sourceLotData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare"))
						{
							eventLog.info("After being sent to the MQC bank, Delete MQC Job. Source Lot : [" + sourceLotData + "]");
							removeMQCJob(sourceLotData);
						}

					}
				}
				else
				{
					eventLog.info("Not exist MQC Operation Info by Product" + SrcTransferProductList);
				}
			}
			else
			{
				eventLog.info("Not exist MQC Product Info by Product of MergeLot[SrcLot :" + sourceLotData.getKey().getLotName() + ", DestLot :" + destinationLotData.getKey().getLotName() + "]");
			}
		}
		else
		{
			eventLog.info("Not exist Release/Recycling MQC Info by SrcLot[" + sourceLotData.getKey().getLotName() + "] or DestLot[" + destinationLotData.getKey().getLotName() + "]");
		}

	}

	private String getMQCJobName(Lot lotData) throws CustomException
	{
		List<MQCPlan> mqcPlanData = ExtendedObjectProxy.getMQCPlanService().getMQCPlanDataByLotNameMultiState(lotData.getKey().getLotName());

		String jobName = "";

		if (mqcPlanData != null)
			jobName = mqcPlanData.get(0).getJobName();
		else
			eventLog.info("Not exist Release/Recycling MQC Job Info by Lot: " + lotData.getKey().getLotName());

		return jobName;
	}

	private void removeMQCJob(Lot sourceLotData) throws CustomException
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
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("RemoveMQC", getEventUser(), getEventComment(), null, null);

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
					kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = MESLotServiceProxy.getLotInfoUtil().setEventInfo(sourceLotData, 0, new ArrayList<ProductU>());
					LotServiceProxy.getLotService().setEvent(sourceLotData.getKey(), eventInfo, setEventInfo);
				}
				catch (Exception e)
				{
				}
			}
		}
	}

	private String getModifyPositionforSourceMQCJob(String ScrMQCJobName, List<String> SrcTransferProductList, boolean isRecycleMQC) throws CustomException
	{
		String modifyPosition = "";

		if (!isRecycleMQC) // Main MQC Flow
		{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT DISTINCT E.POSITION, E.PRODUCTNAME ");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P   ");
			sql.append(" WHERE 1=1 ");
			sql.append("   AND E.JOBNAME = :SRCJOBNAME ");
			sql.append("   AND P.PRODUCTNAME NOT IN ( :SOURCEPRODUCTLIST )   ");
			sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME ");
			sql.append("ORDER BY TO_NUMBER(E.POSITION) ");

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
				eventLog.info("Error : Get Positions Info in CT_MQCPlanDetail by Source Transfer Product : " + ScrMQCJobName + "]");
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
				eventLog.info("Error : Get Positions Info in CT_MQCPlanDetail by Source Transfer Product [jobName : " + ScrMQCJobName + "]");
			}
		}
		return modifyPosition;
	}

	private String getModifyPositionforDestMQCJob(String SrcMQCJobName, String DestMQCJobName, List<String> SrcTransferProductList, List<String> DestProductList, boolean isRecycleMQC)
			throws CustomException
	{
		String modifyPosition = "";

		if (!isRecycleMQC) // Main MQC Flow
		{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT DISTINCT E.POSITION, E.PRODUCTNAME ");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P   ");
			sql.append(" WHERE 1=1 ");
			sql.append("   AND E.JOBNAME = :DESTJOBNAME ");
			sql.append("   AND P.PRODUCTNAME NOT IN ( :DESTPRODUCTLIST )   ");
			sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME ");
			sql.append("ORDER BY TO_NUMBER(POSITION) ");

			Map<String, Object> args = new HashMap<String, Object>();
			args.put("DESTJOBNAME", DestMQCJobName);
			args.put("DESTPRODUCTLIST", DestProductList);

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
			sql.append("SELECT DISTINCT P.POSITION, P.PRODUCTNAME ");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P ");
			sql.append(" WHERE 1 = 1 ");
			sql.append("   AND E.JOBNAME = :DESTJOBNAME ");
			sql.append("   AND P.PRODUCTNAME IN ( :DESTPRODUCTLIST) ");
			sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME ");
			sql.append("   AND E.PROCESSFLOWNAME IN (SELECT PROCESSFLOWNAME ");
			sql.append("                                FROM PROCESSFLOW ");
			sql.append("                               WHERE PROCESSFLOWTYPE = 'MQC') ");
			sql.append("UNION ");
			sql.append("SELECT DISTINCT P.POSITION, P.PRODUCTNAME ");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P ");
			sql.append(" WHERE 1 = 1 ");
			sql.append("   AND E.JOBNAME = :SRCJOBNAME ");
			sql.append("   AND P.PRODUCTNAME IN ( :DESTPRODUCTLIST) ");
			sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME ");
			sql.append("   AND E.PROCESSFLOWNAME IN (SELECT PROCESSFLOWNAME ");
			sql.append("                                FROM PROCESSFLOW ");
			sql.append("                               WHERE PROCESSFLOWTYPE = 'MQC') ");
			sql.append("ORDER BY POSITION ");

			Map<String, Object> args = new HashMap<String, Object>();
			args.put("SRCJOBNAME", SrcMQCJobName);
			// args.put("SRCPRODUCTLIST", SrcTransferProductList);
			args.put("DESTJOBNAME", DestMQCJobName);
			args.put("DESTPRODUCTLIST", DestProductList);

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

	private String getModifyPositionforDestRecycleMQCJob(String SrcMQCJobName, String DestMQCJobName, List<String> SrcTransferProductList, List<String> DestProductList, boolean isRecycleMQC)
			throws CustomException
	{
		String modifyPosition = "";

		if (isRecycleMQC)
		{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT DISTINCT P.POSITION, P.PRODUCTNAME ");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P ");
			sql.append(" WHERE 1 = 1 ");
			sql.append("   AND E.JOBNAME = :DESTJOBNAME ");
			sql.append("   AND P.PRODUCTNAME IN ( :DESTPRODUCTLIST) ");
			sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME ");
			sql.append("   AND E.PROCESSFLOWNAME NOT IN (SELECT PROCESSFLOWNAME ");
			sql.append("                                   FROM PROCESSFLOW ");
			sql.append("                                  WHERE PROCESSFLOWTYPE = 'MQC') ");
			sql.append("UNION ");
			sql.append("SELECT DISTINCT P.POSITION, P.PRODUCTNAME ");
			sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P ");
			sql.append(" WHERE 1 = 1 ");
			sql.append("   AND E.JOBNAME = :SRCJOBNAME ");
			sql.append("   AND P.PRODUCTNAME IN ( :DESTPRODUCTLIST) ");
			sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME ");
			sql.append("   AND E.PROCESSFLOWNAME NOT IN (SELECT PROCESSFLOWNAME ");
			sql.append("                                   FROM PROCESSFLOW ");
			sql.append("                                  WHERE PROCESSFLOWTYPE = 'MQC') ");
			sql.append("ORDER BY POSITION ");

			Map<String, Object> args = new HashMap<String, Object>();
			args.put("SRCJOBNAME", SrcMQCJobName);
			args.put("DESTJOBNAME", DestMQCJobName);
			args.put("DESTPRODUCTLIST", DestProductList);

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

	private String getActualPositionforReserveEQP(String ScrMQCJobName, List<String> SrcTransferProductList, String DestMQCJobName, List<String> DestProductList, String ProcessFlowName,
			String ProcessOperationName) throws CustomException
	{
		String modifyPosition = "";

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT P.POSITION, P.PRODUCTNAME   ");
		sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P   ");
		sql.append(" WHERE E.PRODUCTNAME = P.PRODUCTNAME ");
		sql.append("   AND E.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND E.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME  ");
		sql.append("   AND E.JOBNAME = :DESTJOBNAME   ");
		sql.append("   AND P.PRODUCTNAME IN ( :DESTPRODUCTLIST )   ");
		sql.append("UNION  ");
		sql.append("SELECT DISTINCT P.POSITION, P.PRODUCTNAME   ");
		sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED E, PRODUCT P   ");
		sql.append(" WHERE E.PRODUCTNAME = P.PRODUCTNAME  ");
		sql.append("   AND E.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND E.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME   ");
		sql.append("   AND E.JOBNAME = :SRCJOBNAME   ");
		sql.append("   AND P.PRODUCTNAME IN ( :SOURCEPRODUCTLIST )   ");
		sql.append("ORDER BY POSITION  ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("SRCJOBNAME", ScrMQCJobName);
		args.put("SOURCEPRODUCTLIST", SrcTransferProductList);
		args.put("DESTJOBNAME", DestMQCJobName);
		args.put("DESTPRODUCTLIST", DestProductList);
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
}