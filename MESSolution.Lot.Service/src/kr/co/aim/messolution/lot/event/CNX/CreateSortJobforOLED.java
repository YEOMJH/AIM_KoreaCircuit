package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import kr.co.aim.greenframe.greenFrameServiceProxy;
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

import org.apache.commons.collections.OrderedMap;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class CreateSortJobforOLED extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> fromLotList = SMessageUtil.getBodySequenceItemList(doc, "FROMLOTLIST", true);

		String jobType = SMessageUtil.getBodyItemValue(doc, "JOBTYPE", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);

		// Mantis : 0000414
		// CreateSortJobForOLED功能中，当SortType为Merge（OLED->TP）或Spilt（TP->OLED）时，卡控当前Flow必须为Main Flow，当当前flow不为MainFlow时，报错提醒：请确认当前Flow是否为Main Flow！。
		// 当SortType为Merge（OLED->OLED）、Merge（OLED->OLED）或Spilt（OLED->OLED）、Spilt（TP->TP）时不进行此卡控
		this.checkProcessFlowType(jobType, fromLotList);
		
		int jobPriority = Integer.valueOf(SMessageUtil.getBodyItemValue(doc, "JOBNUMBER", true));

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateSortJobforOLED", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		int sortProductQty=0;
		boolean exchangeCSTFlag=false;

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		String jobName = machineName.substring(0, 6) + eventInfo.getEventTimeKey().substring(2, 14);

		String reasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", false);
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODENAME", false);

		List<String> lotNameList = new ArrayList<String>();

		if (jobType.contains("SourceOnly"))
		{
			reasonCodeType = SMessageUtil.getBodyItemValue(doc, "RESPONSEFACTORY", false)+"-"+SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", false) + " - " + reasonCodeType;
			eventInfo.setReasonCode(reasonCode);
			eventInfo.setReasonCodeType(reasonCodeType);
		}

		// Insert Sort JOB
		ExtendedObjectProxy.getSortJobService().insertSortJob(eventInfo, jobName, constantMap.SORT_JOBSTATE_RESERVED, jobType, jobPriority + 1, "", "");

		Lot toLotData = null;
		List<String> insertedCarrierList = new ArrayList<String>();

		String productOffset = "";

		for (Element fromLotE : fromLotList)
		{
			String fromLotName = fromLotE.getChild("LOTNAME").getText();
			String fromPortName = fromLotE.getChild("PORTNAME").getText();
			String fromCarrierName = fromLotE.getChild("CARRIERNAME").getText();

			Lot fromLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(fromLotName);

			if (StringUtils.isNotEmpty(fromLotData.getUdfs().get("JOBNAME")))
				throw new CustomException("LOT-0123", fromLotData.getKey().getLotName(), fromLotData.getUdfs().get("JOBNAME"), "This Lot is process in firstGlass");

			// ChangeSortFlow Flag - Mantis 0000353
			boolean changeSortFlag = true;

			ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(fromLotData.getFactoryName(), fromLotData.getProcessOperationName(), fromLotData.getProcessOperationVersion());

			if (StringUtils.equals(operationData.getDetailProcessOperationType(), constantMap.SORT_OLEDtoTP)
					||  StringUtils.equals(operationData.getDetailProcessOperationType(), constantMap.SORT_TPtoOLED))
			{
				changeSortFlag = false;
			}

			// check lot current flow
			CommonValidation.IsSorterAvailableFlow(fromLotData);
						
			// Release Hold
			fromLotData = ReleaseHoldLotForSort(eventInfo, fromLotData);

			// Validate Q-time
			ExtendedObjectProxy.getProductQTimeService().validateQTimeByLot(fromLotName);

			List<Element> sorterProductList = SMessageUtil.getSubSequenceItemList(fromLotE, "SORTERPRODUCTLIST", true);
			sortProductQty+=sorterProductList.size();

			//Start 20210517 houxk
			if (StringUtils.equals(jobType, "Merge (OLED to TP)"))
			{
				Element firstLotInfo = fromLotList.get(0);
				String firstLotName = SMessageUtil.getChildText(firstLotInfo, "LOTNAME", true);
				Lot firstLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(firstLotName);
				
				if (!fromLotData.getProductSpecName().equalsIgnoreCase(firstLotData.getProductSpecName()) || !fromLotData.getProductRequestName().equalsIgnoreCase(firstLotData.getProductRequestName())
						|| !fromLotData.getProcessFlowName().equalsIgnoreCase(firstLotData.getProcessFlowName())
						|| !fromLotData.getProcessOperationName().equalsIgnoreCase(firstLotData.getProcessOperationName()))
				{
					throw new CustomException("LOT-9014");
				}
			}
			//End
			
			for (Element sorterProductE : sorterProductList)
			{
				String toCarrierName = sorterProductE.getChild("TOCARRIERNAME").getText();
				String productName = sorterProductE.getChild("PRODUCTNAME").getText();
				String fromslotPosition = sorterProductE.getChild("FROMSLOTPOSITION").getText();
				String toslotPosition = sorterProductE.getChild("TOSLOTPOSITION").getText();
				String fromPosition = sorterProductE.getChild("FROMPOSITION").getText();
				String toPortName = sorterProductE.getChild("TOPORTNAME").getText();
				String toPosition = sorterProductE.getChild("TOPOSITION").getText();
				String cutFlag = sorterProductE.getChild("CUTFLAG").getText();
				String turnFlag = sorterProductE.getChild("TURNFLAG").getText();
				String scrapFlag = sorterProductE.getChild("SCRAPFLAG").getText();
				String turnDegree = sorterProductE.getChild("TURNDEGREE").getText();
				String toLotName = MESLotServiceProxy.getLotInfoUtil().getLotNameByCarrierName(toCarrierName);
				String sortProductState = constantMap.SORT_SORTPRODUCTSTATE_READY;

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

				if (changeSortFlag && StringUtils.isNotEmpty(toLotName) && !lotNameList.contains(toLotName))
					lotNameList.add(toLotName);

				ExtendedObjectProxy.getProductQTimeService().validateQTimeByLot(toLotName);

				if (toLotData != null)
				{
					if (!fromLotData.getProductSpecName().equalsIgnoreCase(toLotData.getProductSpecName()) || !fromLotData.getProductRequestName().equalsIgnoreCase(toLotData.getProductRequestName())
							|| !fromLotData.getProcessFlowName().equalsIgnoreCase(toLotData.getProcessFlowName())
							|| !fromLotData.getProcessOperationName().equalsIgnoreCase(toLotData.getProcessOperationName()))
					{
						throw new CustomException("LOT-9013");
					}

					if (StringUtils.isNotEmpty(toLotData.getUdfs().get("JOBNAME")))
						throw new CustomException("LOT-0123", toLotData.getKey().getLotName(), toLotData.getUdfs().get("JOBNAME"), "This Lot is process in firstGlass");
				}

				if (!insertedCarrierList.contains(toCarrierName))
				{
					ExtendedObjectProxy.getSortJobCarrierService().InsertSortJobCarrier(eventInfo, jobName, toCarrierName, toLotName, machineName, toPortName,
							constantMap.SORT_TRANSFERDIRECTION_TARGET, "", "");

					insertedCarrierList.add(toCarrierName);
				}

				ExtendedObjectProxy.getSortJobProductService().insertSortJobProduct(eventInfo, jobName, productName, machineName, fromLotName, fromCarrierName, fromPortName, fromPosition, toLotName,
						toCarrierName, toPortName, toPosition, sortProductState, cutFlag, turnFlag, scrapFlag, turnDegree, "", fromslotPosition, toslotPosition, reasonCodeType, reasonCode);
			}

			if (!insertedCarrierList.contains(fromCarrierName))
			{
				ExtendedObjectProxy.getSortJobCarrierService().InsertSortJobCarrier(eventInfo, jobName, fromCarrierName, fromLotName, machineName, fromPortName,
						constantMap.SORT_TRANSFERDIRECTION_SOURCE, "", "");
				insertedCarrierList.add(fromCarrierName);
			}

			if (changeSortFlag && !lotNameList.contains(fromLotData.getKey().getLotName()))
				lotNameList.add(fromLotData.getKey().getLotName());
			
			if(jobType.contains("Split")&&insertedCarrierList.size()==2&&((double)sortProductQty==fromLotData.getProductQuantity()))
			{
				exchangeCSTFlag=true;
			}
		}

		for (String lotName : lotNameList)
		{
			Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
			ChangeFlowForSort(eventInfo, lotData,jobType,exchangeCSTFlag);
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

	/*
	 * Mantis : 0000414
	 * CreateSortJobForOLED功能中，当SortType为Merge（OLED->TP）或Spilt（TP->OLED）时，卡控当前Flow必须为Main Flow，
	 * 当当前flow不为MainFlow时，报错提醒：请确认当前Flow是否为Main Flow！。
	 * 当SortType为Merge（OLED->OLED）、Merge（OLED->OLED）或Spilt（OLED->OLED）、Spilt（TP->TP）时不进行此卡控
	 */
	@SuppressWarnings("unchecked")
	private void checkProcessFlowType(String jobType, List<Element> fromLotList) throws CustomException
	{
		if (!CommonUtil.equalsIn(jobType, "Merge (OLED to TP)", "Split (TP to OLED)"))
		{
			return ;
		}
		
		List<String> lotNameList = new ArrayList<String>();
		
		for (Element fromLotE : fromLotList)
		{
			String fromLotName = fromLotE.getChild("LOTNAME").getText();
			
			lotNameList.add(fromLotName);
			
			List<Element> sorterProductList = SMessageUtil.getSubSequenceItemList(fromLotE, "SORTERPRODUCTLIST", true);
			
			for (Element sorterProductE : sorterProductList)
			{
				String toCarrierName = sorterProductE.getChild("TOCARRIERNAME").getText();
				String toLotName = MESLotServiceProxy.getLotInfoUtil().getLotNameByCarrierName(toCarrierName);
				
				lotNameList.add(toLotName);
			}
		}
		
		if (lotNameList == null || lotNameList.size() == 0)
		{
			return ;
		}
		
		String sql = "SELECT F.PROCESSFLOWTYPE, L.LOTNAME "
				   + "FROM LOT L, PROCESSFLOW F "
				   + "WHERE 1 = 1 "
				   + "  AND L.LOTNAME IN(:LOTNAMELIST) "
				   + "  AND F.PROCESSFLOWTYPE != 'Main' "
				   + "  AND F.FACTORYNAME = L.FACTORYNAME "
				   + "  AND F.PROCESSFLOWNAME = L.PROCESSFLOWNAME "
				   + "  AND F.PROCESSFLOWVERSION = L.PROCESSFLOWVERSION ";
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("LOTNAMELIST", lotNameList);
		
		List<OrderedMap> resultDataList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, bindMap);
		if (resultDataList != null && resultDataList.size() > 0)
		{
			// This Lot is not in main processflow
			throw new CustomException("LOT-0310");
		}
	}
	
	private Lot ChangeFlowForSort(EventInfo eventInfo, Lot lotData,String jobType,boolean exchangeCSTFlag) throws CustomException
	{
		// Mantis - 0000239
		eventInfo.setEventName("CreateSortJobforOLED");

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
			
		if (lotData.getProductionType().equals("M")||lotData.getProductionType().equals("D"))
		{
			if(!(jobType.equals("SourceOnly")||(jobType.contains("Split")&&exchangeCSTFlag)))
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
