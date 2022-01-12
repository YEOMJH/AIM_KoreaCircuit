package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.FirstGlassJob;
import kr.co.aim.messolution.extended.object.management.data.SampleLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotMultiHold;
import kr.co.aim.greentrack.lot.management.data.LotMultiHoldKey;
import kr.co.aim.greentrack.lot.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class CreateFirstGlassJob extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateFirstGlassJob", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		eventInfo.setEventComment("CreateFirstGlassJob: " + eventInfo.getEventComment());

		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String jobName = "FG-" + lotName + "-" + TimeStampUtil.getCurrentEventTimeKey();
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String processOperationVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String toProcessFlowName = SMessageUtil.getBodyItemValue(doc, "TOPROCESSFLOWNAME", true);
		String toProcessFlowVersion = SMessageUtil.getBodyItemValue(doc, "TOPROCESSFLOWVERSION", true);
		String toProcessOperationName = SMessageUtil.getBodyItemValue(doc, "TOPROCESSOPERATIONNAME", true);
		String toProcessOperationVersion = SMessageUtil.getBodyItemValue(doc, "TOPROCESSOPERATIONVERSION", true);
		String jobState = SMessageUtil.getBodyItemValue(doc, "JOBSTATE", false);
		String judge = SMessageUtil.getBodyItemValue(doc, "JUDGE", false);
		String toProcessFlowType = SMessageUtil.getBodyItemValue(doc, "TOPROCESSFLOWTYPE", false);
		String firstGlassAll = SMessageUtil.getBodyItemValue(doc, "FIRSTGLASSALL", false);
		String offset = SMessageUtil.getBodyItemValue(doc, "OFFSET", false);

		List<FirstGlassJob> firstGlassJobList = null;

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		CommonValidation.checkLotGrade(lotData);
		CommonValidation.checkJobDownFlag(lotData);
		CommonValidation.checkLotProcessStateWait(lotData);

		List<Product> producList = LotServiceProxy.getLotService().allProducts(lotName);// getproductlist
		// check ProcessInfo
		List<String> productNameList = new ArrayList<>();
		for (Product productA : producList)
		{
			String productName = productA.getKey().getProductName();
			productNameList.add(productName);
		}
		CommonValidation.checkProductProcessInfobyString(productNameList);

		try
		{
			String condition = " WHERE 1=1 AND jobName = ?";
			Object[] bindSet = new Object[] { jobName };
			firstGlassJobList = ExtendedObjectProxy.getFirstGlassJobService().select(condition, bindSet);
		}
		catch (Exception ex)
		{

		}

		if (firstGlassJobList != null)
		{
			throw new CustomException();
		}

		// 1.check for exist job
		List<FirstGlassJob> JobList = null;
		try
		{
			String condition = "WHERE 1=1 AND JOBSTATE NOT IN ( 'Completed','Canceled') AND LOTNAME = ? ";
			Object[] bindSet = new Object[] { lotName };
			JobList = ExtendedObjectProxy.getFirstGlassJobService().select(condition, bindSet);
		}
		catch (Exception ex)
		{

		}
		boolean bWork = true;
		if (JobList != null)
		{
			if (JobList.size() > 0)
			{
				bWork = false;
				throw new CustomException("JOB-8011", " jobstate is not Completed");
			}
		}

		if (bWork)
		{
			// next operation search
			Lot postLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			
			ProcessFlow toProcessFlowData = CommonUtil.getProcessFlowData(lotData.getFactoryName(), toProcessFlowName, toProcessFlowVersion);
			MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
			Node currentNode = null;
			Node toNodeStack = null;
			
			
			if(StringUtils.equals(machineSpecData.getMachineGroupName(), "ADryEtch") || StringUtils.equals(machineSpecData.getMachineGroupName(), "DryEtch"))    
			{
				if(StringUtils.equals(toProcessFlowData.getProcessFlowType(), "Main"))
				{
					currentNode = ProcessFlowServiceProxy.getProcessFlowService().getNode(factoryName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);
					toNodeStack = ProcessFlowServiceProxy.getProcessFlowService().getNextNode(currentNode.getKey().getNodeId(), "Normal", "");
				}
				else
				{
					currentNode = ProcessFlowServiceProxy.getProcessFlowService().getNode(factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion);
					toNodeStack = ProcessFlowServiceProxy.getProcessFlowService().getNextNode(currentNode.getKey().getNodeId(), "Normal", "");
				}
			}
			else if (StringUtils.equals(machineSpecData.getMachineGroupName(), "Sputter"))
			{
				toNodeStack = ProcessFlowServiceProxy.getProcessFlowService().getNode(factoryName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);
			}
			else
			{
				currentNode = ProcessFlowServiceProxy.getProcessFlowService().getNode(factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion);
				toNodeStack = ProcessFlowServiceProxy.getProcessFlowService().getNextNode(currentNode.getKey().getNodeId(), "Normal", "");
			}

			String returnProcessFlowName = toNodeStack.getProcessFlowName();
			String returnProcessFlowVersion = toNodeStack.getProcessFlowVersion();
			String returnProcessOperationName = toNodeStack.getNodeAttribute1();
			String returnProcessOperationVersion = toNodeStack.getNodeAttribute2();

			// 2.FirstGlassJob
			FirstGlassJob dataInfo = new FirstGlassJob();
			dataInfo.setJobName(jobName);
			dataInfo.setLotName(lotName);
			dataInfo.setFactoryName(factoryName);
			dataInfo.setProductSpecName(productSpecName);
			dataInfo.setProductSpecVersion(productSpecVersion);
			dataInfo.setProcessFlowName(processFlowName);
			dataInfo.setProcessFlowVersion(processFlowVersion);
			dataInfo.setProcessOperationName(processOperationName);
			dataInfo.setProcessOperationVersion(processOperationVersion);
			dataInfo.setMachineName(machineName);
			dataInfo.setToProcessFlowName(toProcessFlowName);
			dataInfo.setToProcessFlowVersion(toProcessFlowVersion);
			dataInfo.setToProcessOperationName(toProcessOperationName);
			dataInfo.setToProcessOperationVersion(toProcessOperationVersion);
			dataInfo.setJobState(jobState);
			dataInfo.setJudge(judge);
			dataInfo.setCreateTime(eventInfo.getEventTime());
			// TOPROCESSFLOWTYPE
			dataInfo.setToProcessFlowType(toProcessFlowType);
			// return operation
			dataInfo.setReturnProcessFlowName(returnProcessFlowName);
			dataInfo.setReturnProcessFlowVersion(returnProcessFlowVersion);
			dataInfo.setReturnProcessOperationName(returnProcessOperationName);
			dataInfo.setReturnProcessOperationVersion(returnProcessOperationVersion);
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
			dataInfo.setLastEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setOffset(offset);

			ExtendedObjectProxy.getFirstGlassJobService().create(eventInfo, dataInfo);

			// 3.FirstGlassJobHistory

			// Update jobName to Lot
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("JOBNAME", jobName);
			setEventInfo.getUdfs().put("FIRSTGLASSALL", firstGlassAll);
			postLotData = LotServiceProxy.getLotService().setEvent(postLotData.getKey(), eventInfo, setEventInfo);

			// Delete SampleFlow
			// get SampleLotData(CT_SAMPLELOT)
			List<SampleLot> sampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListByInfo(postLotData.getKey().getLotName(), postLotData.getFactoryName(),
					postLotData.getProductSpecName(), postLotData.getProductSpecVersion(), processFlowName, processFlowVersion, processOperationName, processOperationVersion);

			// if alreay exist, remove sampling Lot in CT_SAMPLELOT, CT_SAMPLEPRODUCT Table
			if (sampleLot != null)
			{
				// if exist, delete previous SamplingProduct Data(CT_SAMPLEPRODUCT)
				ExtendedObjectProxy.getSampleProductService().deleteSampleProductByLotNameAndInfo(eventInfo, lotName, postLotData.getFactoryName(), postLotData.getProductSpecName(),
						postLotData.getProductSpecVersion(), processFlowName, processFlowVersion, processOperationName, processOperationVersion);

				ExtendedObjectProxy.getSampleLotService().deleteSampleLotDataByInfo(eventInfo, postLotData.getKey().getLotName(), postLotData.getFactoryName(), postLotData.getProductSpecName(),
						postLotData.getProductSpecVersion(), processFlowName, processFlowVersion, processOperationName, processOperationVersion);
			}

			if (StringUtils.equals(postLotData.getUdfs().get("FIRSTGLASSALL"), "Y"))
			{
				// if Job's FromOperation is same Lot's ProcessOepration, immediately Hold
				if (StringUtils.equals(processOperationName, postLotData.getProcessOperationName()) && StringUtils.equals(processOperationVersion, postLotData.getProcessOperationVersion()))
				{
					// Lot Hold
					if (postLotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().Lot_NotOnHold))
					{
						eventInfo.setReasonCode("FirstGlassHold");
						eventInfo.setReasonCodeType("FirstGlass");

						lotMultiHold(eventInfo, postLotData, new HashMap<String, String>());
					}
				}
				// if Job's FromOperation is not same Lot's ProcessOepration, Reserve Future Hold
				else
				{
					eventInfo = EventInfoUtil.makeEventInfo("InsertFutureHold", getEventUser(), getEventComment(), "", "");
					eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
					eventInfo.setEventComment("ReserveOper:" + postLotData.getProcessOperationName() + "." + eventInfo.getEventComment());

					String beforeActionComment = eventInfo.getEventComment();
					String beforeActionUser = eventInfo.getEventUser();

					MESLotServiceProxy.getLotServiceImpl().insertCtLotFutureMultiHoldActionForAfter(eventInfo, lotName, factoryName, processFlowName, processFlowVersion, processOperationName,
							processOperationVersion, "0", "hold", "System", "FirstGlass", "FirstGlassHold", "", "", "", "True", "False", beforeActionComment, "", beforeActionUser, "", "Insert", "",
							"");
				}
			}
			else
			{
				// if Job's FromOperation is same Lot's ProcessOepration, immediately Hold
				if (StringUtils.equals(processOperationName, postLotData.getProcessOperationName()) && StringUtils.equals(processOperationVersion, postLotData.getProcessOperationVersion()))
				{
					// Mother Lot Hold
					if (postLotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().Lot_NotOnHold))
					{
						eventInfo.setReasonCode("FirstGlassHold");
						eventInfo.setReasonCodeType("FirstGlass");

						lotMultiHold(eventInfo, postLotData, new HashMap<String, String>());
					}
				}
				// if Job's FromOperation is not same Lot's ProcessOepration, Reserve Future Hold
				else
				{
					eventInfo = EventInfoUtil.makeEventInfo("InsertFutureHold", getEventUser(), getEventComment(), "", "");
					eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
					eventInfo.setEventComment("ReserveOper:" + postLotData.getProcessOperationName() + "." + eventInfo.getEventComment());

					String beforeActionComment = eventInfo.getEventComment();
					String beforeActionUser = eventInfo.getEventUser();

					MESLotServiceProxy.getLotServiceImpl().insertCtLotFutureMultiHoldActionForAfter(eventInfo, lotName, factoryName, processFlowName, processFlowVersion, processOperationName,
							processOperationVersion, "0", "hold", "System", "FirstGlass", "FirstGlassHold", "", "", "", "True", "False", beforeActionComment, "", beforeActionUser, "", "Insert", "",
							"");
				}
			}
		}

		return doc;
	}

	public void lotMultiHold(EventInfo eventInfo, Lot lotData, Map<String, String> udfs) throws CustomException
	{
		// Set EventInfo
		eventInfo = EventInfoUtil.makeEventInfo("CreateFirstGlassJob", eventInfo.getEventUser(), eventInfo.getEventComment(), eventInfo.getReasonCodeType(), eventInfo.getReasonCode());

		// Update HoldState for execution MultiHold
		if (StringUtil.equals(lotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_OnHold))
		{
			// Update LotHoldState - N
			String sql = "UPDATE LOT SET LOTHOLDSTATE = :LOTHOLDSTATE WHERE LOTNAME = :LOTNAME ";
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LOTHOLDSTATE", GenericServiceProxy.getConstantMap().Lot_NotOnHold);
			bindMap.put("LOTNAME", lotData.getKey().getLotName());

			GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);

			// Update ProductHoldState - N
			sql = "UPDATE PRODUCT SET PRODUCTHOLDSTATE = :PRODUCTHOLDSTATE WHERE LOTNAME = :LOTNAME ";
			bindMap.clear();
			bindMap.put("PRODUCTHOLDSTATE", GenericServiceProxy.getConstantMap().Prod_NotOnHold);
			bindMap.put("LOTNAME", lotData.getKey().getLotName());

			GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		}

		// Get ProductUSequence
		List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);

		// Set MakeOnHoldInfo
		MakeOnHoldInfo makeOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeOnHoldInfo(productUSequence, new HashMap<String, String>());
		LotServiceProxy.getLotService().makeOnHold(lotData.getKey(), eventInfo, makeOnHoldInfo);

		// Set Udfs - PROCESSOPERATIONNAME to Insert OperationName MultiHold
		udfs.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());

		// Insert into LOTMULTIHOLD table
		LotMultiHoldKey holdKey = new LotMultiHoldKey();
		holdKey.setLotName(lotData.getKey().getLotName());
		holdKey.setReasonCode(eventInfo.getReasonCode());

		LotMultiHold holdData = new LotMultiHold();
		holdData.setKey(holdKey);
		holdData.setEventName(eventInfo.getEventName());
		holdData.setEventTime(eventInfo.getEventTime());
		holdData.setEventComment(eventInfo.getEventComment());
		holdData.setEventUser(eventInfo.getEventUser());
		holdData.setUdfs(udfs);

		try
		{
			LotServiceProxy.getLotMultiHoldService().insert(holdData);
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("LOT-0002", holdKey.getLotName(), holdKey.getReasonCode());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("LOT-9999", fe.getMessage());
		}

		// insert in PRODUCTMULTIHOLD table
		MESProductServiceProxy.getProductServiceImpl().setProductMultiHold(eventInfo, lotData.getKey().getLotName(), udfs);
	}
}
