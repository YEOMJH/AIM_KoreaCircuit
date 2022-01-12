package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LotFutureAction;
import kr.co.aim.messolution.extended.object.management.data.MQCPlan;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.MakeNotOnHoldInfo;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class CreateSortJobForBufferCST extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> fromLotList = SMessageUtil.getBodySequenceItemList(doc, "FROMLOTLIST", true);

		String jobType = SMessageUtil.getBodyItemValue(doc, "JOBTYPE", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);

		int jobPriority = Integer.valueOf(SMessageUtil.getBodyItemValue(doc, "JOBNUMBER", true));

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateSortJobForBufferCST", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String jobName = machineName.substring(0, 6) + eventInfo.getEventTimeKey().substring(2, 14);

		ExtendedObjectProxy.getSortJobService().insertSortJob(eventInfo, jobName, constantMap.SORT_JOBSTATE_RESERVED, jobType, jobPriority + 1, "", "");

		Lot toLotData = null;
		List<String> insertedCarrierList = new ArrayList<String>();

		List<String> lotNameList = new ArrayList<String>();
		List<String> toLotList = new ArrayList<String>();

		String productOffset = "";

		for (Element fromLotE : fromLotList)
		{
			String fromLotName = fromLotE.getChild("LOTNAME").getText();
			String fromCarrierName = fromLotE.getChild("CARRIERNAME").getText();
			String fromPortName = fromLotE.getChild("PORTNAME").getText();

			lotNameList.add(fromLotName);
			
			Durable fromDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(fromCarrierName);
			Lot fromLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(fromLotName);

			ExtendedObjectProxy.getProductQTimeService().validateQTimeByLot(fromLotName);

			if (StringUtils.isNotEmpty(fromLotData.getUdfs().get("JOBNAME")))
				throw new CustomException("LOT-0123", fromLotData.getKey().getLotName(), fromLotData.getUdfs().get("JOBNAME"), "Create Sort Job For FirstGlass");

			// check lot current flow
			CommonValidation.IsSorterAvailableFlow(fromLotData);
						
			// Release Hold
			fromLotData = ReleaseHoldLotForSort(eventInfo, fromLotData);

			List<Element> sorterProductList = SMessageUtil.getSubSequenceItemList(fromLotE, "SORTERPRODUCTLIST", true);

			for (Element sorterProductE : sorterProductList)
			{
				String productName = sorterProductE.getChild("PRODUCTNAME").getText();
				String fromslotPosition = sorterProductE.getChild("FROMSLOTPOSITION").getText();
				String toslotPosition = sorterProductE.getChild("TOSLOTPOSITION").getText();
				String fromPosition = sorterProductE.getChild("FROMPOSITION").getText();
				String toCarrierName = sorterProductE.getChild("TOCARRIERNAME").getText();
				String toPortName = sorterProductE.getChild("TOPORTNAME").getText();
				String toPosition = sorterProductE.getChild("TOPOSITION").getText();
				String sortProductState = constantMap.SORT_SORTPRODUCTSTATE_READY;
				String turnFlag = sorterProductE.getChild("TURNFLAG").getText();
				String turnDegree = sorterProductE.getChild("TURNDEGREE").getText();
				String toLotName = MESLotServiceProxy.getLotInfoUtil().getLotNameByCarrierName(toCarrierName);

				Product productInfo = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
				if (StringUtil.isEmpty(productOffset))
				{
					if (StringUtil.isEmpty(productOffset))
					{
						if (productInfo.getUdfs().get("OFFSET") != null)
						{
							productOffset = productInfo.getUdfs().get("OFFSET").toString();
						}
					}
					else
					{
						if (!productOffset.equals(productInfo.getUdfs().get("OFFSET")))
						{
							throw new CustomException("OFFSET-0001", productInfo.getKey().getProductName());
						}
					}
				}

				CommonValidation.checkIFIProcessing(productName);

				if (StringUtil.isNotEmpty(toLotName))
					toLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(toLotName);

				ExtendedObjectProxy.getProductQTimeService().validateQTimeByLot(toLotName);
				Durable toDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(toCarrierName);

				if (!StringUtils.equals(toDurableData.getDurableType(), constantMap.CST_TYPE_BUFFER) && StringUtils.isNotEmpty(toLotName) && !toLotList.contains(toLotName))
					toLotList.add(toLotName);

				if (StringUtils.equals(fromDurableData.getDurableType(), constantMap.CST_TYPE_BUFFER) && !StringUtils.equals(toDurableData.getDurableType(), constantMap.CST_TYPE_BUFFER))
				{
					if (toLotData != null)
					{
						String[][] nodeResult = CommonUtil.getNodeInfo(fromLotData.getNodeStack().substring(0, fromLotData.getNodeStack().indexOf(".")));

						String processFlowName = nodeResult[0][1];
						String processOperationName = nodeResult[0][2];

						if (!fromLotData.getProductSpecName().equalsIgnoreCase(toLotData.getProductSpecName()) || !processFlowName.equalsIgnoreCase(toLotData.getProcessFlowName())
								|| !processOperationName.equalsIgnoreCase(toLotData.getProcessOperationName())
								|| !fromLotData.getProductRequestName().equalsIgnoreCase(toLotData.getProductRequestName()))
						{
							throw new CustomException("LOT-9013");
						}

						if (StringUtils.isNotEmpty(toLotData.getUdfs().get("JOBNAME")))
							throw new CustomException("LOT-0123", toLotData.getKey().getLotName(), toLotData.getUdfs().get("JOBNAME"), "This Lot is process in firstGlass");
					}
				}

				if (!insertedCarrierList.contains(toCarrierName))
				{
					ExtendedObjectProxy.getSortJobCarrierService().InsertSortJobCarrier(eventInfo, jobName, toCarrierName, toLotName, machineName, toPortName,
							constantMap.SORT_TRANSFERDIRECTION_TARGET, "", "");

					insertedCarrierList.add(toCarrierName);
				}

				ExtendedObjectProxy.getSortJobProductService().insertSortJobProduct(eventInfo, jobName, productName, machineName, fromLotName, fromCarrierName, fromPortName, fromPosition, toLotName,
						toCarrierName, toPortName, toPosition, sortProductState, turnFlag, "", turnDegree, "", fromslotPosition, toslotPosition, "", "");
			}

			if (!insertedCarrierList.contains(fromCarrierName))
			{
				ExtendedObjectProxy.getSortJobCarrierService().InsertSortJobCarrier(eventInfo, jobName, fromCarrierName, fromLotName, machineName, fromPortName,
						constantMap.SORT_TRANSFERDIRECTION_SOURCE, "", "");
				insertedCarrierList.add(fromCarrierName);
			}

			if (!StringUtils.equals(fromDurableData.getDurableType(), constantMap.CST_TYPE_BUFFER))
			{
				// Change Flow
				fromLotData = ChangeFlowForSort(eventInfo, fromLotData);
			}
		}

		for (String toLotName : toLotList)
		{
			lotNameList.add(toLotName);
			
			Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(toLotName);
			ChangeFlowForSort(eventInfo, lotData);
		}

		// 2020-11-17	dhko	ReserveLotHold if ProcessingInfo is 'B' in Product
		if (jobType.contains("Split"))
		{
			boolean isExist = false;
			for (String lotName : lotNameList)
			{
				if (!isExist)
				{
					isExist = MESLotServiceProxy.getLotServiceUtil().isAbortProductList(lotName);
				}				
			}
			
			if (isExist)
			{
				MESLotServiceProxy.getLotServiceImpl().makeReserveHoldByAbortProductList(eventInfo, lotNameList);
			}
		}
		
		return doc;
	}

	private Lot ChangeFlowForSort(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		// Mantis - 0000239
		eventInfo.setEventName("CreateSortJobforBufferCST");

		CommonValidation.checkJobDownFlag(lotData);

		ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);

		if (processFlowData.getProcessFlowType().equals("Sort"))
		{
			eventLog.info("Can not change flow for SortFlow. Because already in SortFlow");
			return lotData;
		}

		//Mantis - 0000400
		//if (!processFlowData.getProcessFlowType().equals("Main"))
		//	throw new CustomException("LOT-0310");
		
		if (processFlowData.getProcessFlowType().contains("MQC"))
		{
			if (!StringUtils.equals(processFlowData.getProcessFlowType(), "MQCPrepare"))
			{
				throw new CustomException("SORT-0010", processFlowData.getProcessFlowType());
			}
			else
			{
				List<MQCPlan> mqcPlanData = ExtendedObjectProxy.getMQCPlanService().getMQCPlanDataByLotName(lotData.getKey().getLotName());

				if (mqcPlanData != null)
					throw new CustomException("SORT-0011", lotData.getKey().getLotName());
			}
		}
		
		List<ProcessFlow> processFlowList = ProcessFlowServiceProxy.getProcessFlowService().select(" factoryName = ? and processFlowType = ? ", new Object[] { lotData.getFactoryName(), "Sort" });
		ProcessFlow sortProcessFlow = processFlowList.get(0);

		ProcessOperationSpec firstOperation = CommonUtil.getFirstOperation(lotData.getFactoryName(), sortProcessFlow.getKey().getProcessFlowName());

		if (StringUtil.equals(processFlowData.getProcessFlowType(), "Sort"))
			throw new CustomException("LOT-0111");

		ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSortSpecInfo(lotData, lotData.getAreaName(), lotData.getDueDate(), lotData.getFactoryName(),
				lotData.getLotHoldState(), lotData.getLotProcessState(), lotData.getLotState(), lotData.getPriority(), sortProcessFlow.getKey().getProcessFlowName(), "00001",
				firstOperation.getKey().getProcessOperationName(), "00001", lotData.getProductionType(), lotData.getProductRequestName(), lotData.getProductSpec2Name(),
				lotData.getProductSpec2Version(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getSubProductUnitQuantity1(), lotData.getSubProductUnitQuantity2());

		lotData = MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);

		// Reserve Hold
		SetFutureActionData(eventInfo, lotData);

		return lotData;
	}

	private Lot ReleaseHoldLotForSort(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		// Release Hold Lot
		if (lotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().Lot_OnHold))
		{
			eventInfo.setEventName("ReleaseHold");
			String reasonCode = lotData.getReasonCode();
			List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);

			MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(lotData, productUSequence, new HashMap<String, String>());
			lotData = LotServiceProxy.getLotService().makeNotOnHold(lotData.getKey(), eventInfo, makeNotOnHoldInfo);

			// Delete in LOTMULTIHOLD table
			MESLotServiceProxy.getLotServiceUtil().releaseMultiHold(lotData.getKey().getLotName(), reasonCode, lotData.getProcessOperationName());

			// Delete in PRODUCTMULTIHOLD table
			MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(lotData.getKey().getLotName(), reasonCode, lotData.getProcessOperationName());
		}

		return lotData;
	}

	private void SetFutureActionData(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		eventInfo.setEventName("SetFutureActionData");

		Map<String, String> lotDataUdfs = lotData.getUdfs();

		List<LotFutureAction> holdFutureActionDataList = getLotFutureActionDataListSorter(lotData.getKey().getLotName(), lotData.getFactoryName(), lotDataUdfs.get("RETURNFLOWNAME").toString(),
				lotDataUdfs.get("RETURNOPERATIONNAME").toString(), "hold");

		if (holdFutureActionDataList == null)
		{
			String factoryName = lotData.getFactoryName();
			String processFlowName = lotDataUdfs.get("RETURNFLOWNAME").toString();
			String processFlowVersion = lotData.getProcessFlowVersion();
			String processOperationName = lotDataUdfs.get("RETURNOPERATIONNAME").toString();
			String processOperationVersion = lotData.getProcessOperationVersion();
			String actionName = "hold";
			String actionType = "System";
			String reasonCodeType = "ReserveHoldLot";
			String reasonCode = "RH-SORT";
			String attribute1 = "";
			String attribute2 = "";
			String attribute3 = "";
			String beforeAction = "True";
			String afterAction = "False";
			String beforeActionComment = "CreateSortJob";
			String afterActionComment = "";
			String beforeActionUser = "";
			String afterActionUser = "";

			int position = 0;

			ExtendedObjectProxy.getLotFutureActionService().insertLotFutureAction(eventInfo, lotData.getKey().getLotName(), factoryName, processFlowName, processFlowVersion, processOperationName,
					processOperationVersion, position, reasonCode, reasonCodeType, actionName, actionType, attribute1, attribute2, attribute3, beforeAction, afterAction, beforeActionComment,
					afterActionComment, beforeActionUser, afterActionUser);

		}
	}

	private List<LotFutureAction> getLotFutureActionDataListSorter(String lotName, String factoryName, String processFlowName, String processOperationName, String actionName) throws CustomException
	{
		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSOPERATIONNAME = ? AND ACTIONNAME = ?";
		Object[] bindSet = new Object[] { lotName, factoryName, processFlowName, processOperationName, actionName };

		List<LotFutureAction> dataList = new ArrayList<LotFutureAction>();
		try
		{
			dataList = ExtendedObjectProxy.getLotFutureActionService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataList = null;
		}

		return dataList;
	}

}
