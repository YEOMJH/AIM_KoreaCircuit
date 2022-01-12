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
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.MakeNotOnHoldInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;

import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class CreateSortJob extends SyncHandler {
	private static Log log = LogFactory.getLog(CreateSortJob.class);
	String cleanMode = "";
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> fromLotList = SMessageUtil.getBodySequenceItemList(doc, "FROMLOTLIST", true);

		String jobType = SMessageUtil.getBodyItemValue(doc, "JOBTYPE", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		cleanMode = SMessageUtil.getBodyItemValue(doc, "CSTCLEANMODE", false);

		int jobPriority = Integer.valueOf(SMessageUtil.getBodyItemValue(doc, "JOBNUMBER", true));

		EventInfo eventInfo = EventInfoUtil.makeEventInfo_IgnoreHold("CreateSortJob", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		int sortProductQty=0;
		boolean exchangeCSTFlag=false;

		// Oven sorter check condition
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

		if (machineData.getMachineGroupName() != null && !StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Sorter))
			this.checkChangerOperationMode(machineData);
		
		for (Element fromLotE : fromLotList)
		{
			List<Element> sorterProductList = SMessageUtil.getSubSequenceItemList(fromLotE, "SORTERPRODUCTLIST", true);
		    for (Element sorterProductE : sorterProductList)
		    {
			    String toCarrierName = sorterProductE.getChild("TOCARRIERNAME").getText();
			    List<String> joblist = jobvalidaticon(toCarrierName);				
			    if (joblist !=null && joblist.size() >0 )		    			     
			    	throw new CustomException("LOT-0329");		     			    			    
			}
		}  

		String jobName = machineName.substring(0, 6) + eventInfo.getEventTimeKey().substring(2, 14);

		String reasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", false);
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODENAME", false);

		if (jobType.equals("SourceOnly"))
		{
			reasonCodeType = SMessageUtil.getBodyItemValue(doc, "RESPONSEFACTORY", false)+"-"+SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", false) + " - " + reasonCodeType;
			eventInfo.setReasonCode(reasonCode);
			eventInfo.setReasonCodeType(reasonCodeType);
		}

		List<String> lotNameList = new ArrayList<String>();
		List<String> toLotList = new ArrayList<String>();
		
		

		// Insert Sort JOB
		ExtendedObjectProxy.getSortJobService().insertSortJob(eventInfo, jobName, GenericServiceProxy.getConstantMap().SORT_JOBSTATE_RESERVED, jobType, jobPriority + 1, "", "");

		Lot toLotData = null;
		List<String> insertedCarrierList = new ArrayList<String>();

		for (Element fromLotE : fromLotList)
		{
			String fromLotName = fromLotE.getChild("LOTNAME").getText();
			String fromPortName = fromLotE.getChild("PORTNAME").getText();
			String fromCarrierName = fromLotE.getChild("CARRIERNAME").getText();
		    exchangeCSTFlag=false;

			lotNameList.add(fromLotName);
			
			Lot fromLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(fromLotName);

			if (StringUtils.isNotEmpty(fromLotData.getUdfs().get("JOBNAME")))
				throw new CustomException("LOT-0123", fromLotData.getKey().getLotName(), fromLotData.getUdfs().get("JOBNAME"), "This Lot is process in firstGlass");

			// check lot current flow
			CommonValidation.IsSorterAvailableFlow(fromLotData);
			
			// Release Hold
			//fromLotData = ReleaseHoldLotForSort(eventInfo, fromLotData);

			// Validate Q-time
			ExtendedObjectProxy.getProductQTimeService().validateQTimeByLot(fromLotName);

			List<Element> sorterProductList = SMessageUtil.getSubSequenceItemList(fromLotE, "SORTERPRODUCTLIST", true);
			sortProductQty+=sorterProductList.size();

			for (Element sorterProductE : sorterProductList)
			{
				String toCarrierName = sorterProductE.getChild("TOCARRIERNAME").getText();
				String productName = sorterProductE.getChild("PRODUCTNAME").getText();
				String fromPosition = sorterProductE.getChild("FROMPOSITION").getText();
				String toPortName = sorterProductE.getChild("TOPORTNAME").getText();
				String toPosition = sorterProductE.getChild("TOPOSITION").getText();
				String cutFlag = sorterProductE.getChild("CUTFLAG").getText();
				String turnFlag = sorterProductE.getChild("TURNFLAG").getText();
				String scrapFlag = sorterProductE.getChild("SCRAPFLAG").getText();
				String turnDegree = sorterProductE.getChild("TURNDEGREE").getText();
				String toSlotPosition = sorterProductE.getChild("TOSLOTPOSITION").getText();
				String fromSlotPosition = sorterProductE.getChild("FROMSLOTPOSITION").getText();
				String toLotName = MESLotServiceProxy.getLotInfoUtil().getLotNameByCarrierName(toCarrierName);
				String sortProductState = GenericServiceProxy.getConstantMap().SORT_SORTPRODUCTSTATE_READY;

				CommonValidation.checkIFIProcessing(productName);

				if (StringUtil.isNotEmpty(toLotName))
					toLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(toLotName);

				if (StringUtils.isNotEmpty(toLotName) && !toLotList.contains(toLotName))
					toLotList.add(toLotName);

				ExtendedObjectProxy.getProductQTimeService().validateQTimeByLot(toLotName);

				if (toLotData != null)
				{
					if (!fromLotData.getProductSpecName().equalsIgnoreCase(toLotData.getProductSpecName()) || !fromLotData.getProductRequestName().equalsIgnoreCase(toLotData.getProductRequestName())
							|| !fromLotData.getProcessFlowName().equalsIgnoreCase(toLotData.getProcessFlowName())
							|| !fromLotData.getProcessOperationName().equalsIgnoreCase(toLotData.getProcessOperationName()))
						throw new CustomException("LOT-9013");

					if (StringUtils.isNotEmpty(toLotData.getUdfs().get("JOBNAME")))
						throw new CustomException("LOT-0123", toLotData.getKey().getLotName(), toLotData.getUdfs().get("JOBNAME"), "This Lot is process in firstGlass");
				}

				if (!insertedCarrierList.contains(toCarrierName))
				{
					// Insert Sort Job Carrier (Target)
					ExtendedObjectProxy.getSortJobCarrierService().InsertSortJobCarrier(eventInfo, jobName, toCarrierName, toLotName, machineName, toPortName,
							GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_TARGET, "", "");
					insertedCarrierList.add(toCarrierName);
				}

				// Insert Sort Job Product
				ExtendedObjectProxy.getSortJobProductService().insertSortJobProduct(eventInfo, jobName, productName, machineName, fromLotName, fromCarrierName, fromPortName, fromPosition, toLotName,
						toCarrierName, toPortName, toPosition, sortProductState, cutFlag, turnFlag, scrapFlag, turnDegree, "", fromSlotPosition, toSlotPosition, reasonCodeType, reasonCode);
			}

			if (!insertedCarrierList.contains(fromCarrierName))
			{
				// Insert Sort Job Carrier (Source)
				ExtendedObjectProxy.getSortJobCarrierService().InsertSortJobCarrier(eventInfo, jobName, fromCarrierName, fromLotName, machineName, fromPortName,
						GenericServiceProxy.getConstantMap().SORT_TRANSFERDIRECTION_SOURCE, "", "");
				insertedCarrierList.add(fromCarrierName);
			}
			
			if(jobType.contains("Split")&&insertedCarrierList.size()==2&&((double)sortProductQty==fromLotData.getProductQuantity()))
			{
				exchangeCSTFlag=true;
			}

			// Change Flow
			fromLotData = ChangeFlowForSort(eventInfo, fromLotData,jobType,exchangeCSTFlag);
		}

		for (String toLotName : toLotList)
		{
			lotNameList.add(toLotName);
			
			Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(toLotName);
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

	private void checkChangerOperationMode(Machine machineData) throws CustomException
	{
		if (!StringUtils.equals(machineData.getUdfs().get("OPERATIONMODE").toUpperCase(), GenericServiceProxy.getConstantMap().SORT_OPERATIONMODE))
			throw new CustomException("SORT-0009", machineData.getKey().getMachineName(), machineData.getUdfs().get("OPERATIONMODE"));
	}

	private Lot ChangeFlowForSort(EventInfo eventInfo, Lot lotData,String jobType,boolean exchangeCSTFlag) throws CustomException
	{
		// Mantis - 0000239
		eventInfo.setEventName("CreateSortJob");
		CommonValidation.checkJobDownFlag(lotData);

		ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);

		if (processFlowData.getProcessFlowType().equals("Sort"))
		{
			eventLog.info("Can not change flow for SortFlow. Because already in SortFlow");
			return lotData;
		}

		// Mantis - 0000400
		//if (!processFlowData.getProcessFlowType().equals("Main"))
		//	throw new CustomException("LOT-0310");
		ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(lotData.getFactoryName(), lotData.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
		if (lotData.getProductionType().equals("M")||lotData.getProductionType().equals("D"))
		{
			if (!productSpecData.getUdfs().get("PROCESSFLOWTYPE").equals("TP-DUMMY")) 
			{
				if (!(jobType.equals("SourceOnly") || (jobType.contains("Split") && exchangeCSTFlag))) 
				{
					if (!StringUtils.equals(processFlowData.getProcessFlowType(), "MQCPrepare")) 
					{
						throw new CustomException("SORT-0010", processFlowData.getProcessFlowType());
					} 
					else 
					{
						List<MQCPlan> mqcPlanData = ExtendedObjectProxy.getMQCPlanService()
								.getMQCPlanDataByLotName(lotData.getKey().getLotName());

						if (mqcPlanData != null)
							throw new CustomException("SORT-0011", lotData.getKey().getLotName());
					}
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
		if (StringUtil.isEmpty(cleanMode) || !StringUtil.equals(cleanMode, "Y"))
			SetFutureActionData(eventInfo, lotData);

		return lotData;
	}

	private Lot ReleaseHoldLotForSort(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		// Release Hold Lot
		if (lotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().Lot_OnHold))
		{
			eventInfo.setEventName("ReleaseHold");
			//Map<String, String> udfs = lotData.getUdfs();
			String reasonCode = lotData.getReasonCode();
			List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);

			MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(lotData, productUSequence, new HashMap<String, String>());
			lotData = LotServiceProxy.getLotService().makeNotOnHold(lotData.getKey(), eventInfo, makeNotOnHoldInfo);

			// Delete in LOTMULTIHOLD table
			MESLotServiceProxy.getLotServiceUtil().releaseMultiHold(lotData.getKey().getLotName(), reasonCode, lotData.getProcessOperationName());

			// Delete in PRODUCTMULTIHOLD table
			MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(lotData.getKey().getLotName(), reasonCode, lotData.getProcessOperationName());

			// SetHoldState
			// MESLotServiceProxy.getLotServiceUtil().setHoldState(eventInfo, lotData);
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
		List<LotFutureAction> dataList = new ArrayList<LotFutureAction>();
		// Mantis : 0000460
		// 解hold后，cratesorterjob ,在插入sorterjob结束后预约hold时，查询是否存在预约hold，因缺失reasoncode的主键，，未创建sorterjob的预约hold
		try
		{
			String condition = "WHERE LOTNAME = ? "
							 + "  AND FACTORYNAME = ? "
							 + "  AND PROCESSFLOWNAME = ? "
							 + "  AND PROCESSFLOWVERSION = ? "
							 + "  AND PROCESSOPERATIONNAME = ? "
							 + "  AND PROCESSOPERATIONVERSION = ? "
							 + "  AND POSITION = ? "
							 + "  AND REASONCODE = ? "
							 + "  AND ACTIONNAME = ? ";
			
			dataList = ExtendedObjectProxy.getLotFutureActionService().select(condition, new Object[] { 
												lotName, factoryName, processFlowName, "00001", processOperationName, "00001",
												0, "RH-SORT", actionName });
		}
		catch (Exception ex)
		{
			dataList = null;
		}
		
		return dataList;
	}
	
	private List<String> jobvalidaticon(String toCarrierName)
	{
		List<Map<String,Object>> resultList = null;
		List<String> joblist = new ArrayList<String>();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT * FROM CT_SORTJOBCARRIER A, CT_SORTJOB B   "
				+ "WHERE A.JOBNAME = B.JOBNAME AND A.CARRIERNAME = :CARRIERNAME "
				+ "AND A.TRANSFERDIRECTION = 'TARGET' AND B.JOBTYPE = 'Split' AND B.JOBSTATE IN('RESERVED', 'CONFIRMED', 'STARTED')");
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("CARRIERNAME", toCarrierName);
		
       try
		{
    	   resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
    	   if (resultList.size() > 0) 
			{
				for (Map<String, Object> jobname : resultList)
				{
					String  sortjob= ConvertUtil.getMapValueByName(jobname, "JOBNAME");			
					joblist.add(sortjob);
				}
			}
		}    
        catch (Exception ex)
		{
    	   joblist = null;
		}
		
		return joblist;
	}

}
