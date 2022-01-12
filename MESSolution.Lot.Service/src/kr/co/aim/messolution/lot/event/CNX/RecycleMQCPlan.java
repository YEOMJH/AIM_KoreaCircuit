package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCPlan;
import kr.co.aim.messolution.extended.object.management.data.MQCPlanDetail;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class RecycleMQCPlan extends SyncHandler {

	public static Log logger = LogFactory.getLog(RecycleMQCPlan.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String jobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String recycleProcessFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String recycleProcessFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String positions = SMessageUtil.getBodyItemValue(doc, "POSITIONS", true);

		MQCPlan planData = ExtendedObjectProxy.getMQCPlanService().selectByKey(true, new Object[] { jobName });

		// MQC validation
		// throw new CustomException("MQC-0011");
		if (!planData.getMQCState().equals("Released"))
		{
			//MQC-0007: MQC plan is not held
			throw new CustomException("MQC-0007");
		}

		if (StringUtil.isEmpty(recycleProcessFlowName))
		{
			//MQC-0008:No appointed recycle flow on this MQC
			throw new CustomException("MQC-0008");
		}

		if (planData.getRecycleLimit() != 0)
		{
			if (planData.getRecycleCount() >= planData.getRecycleLimit())
				throw new CustomException("MQC-0002", lotName);
		}

		List<MQCPlan> resultList;
		try
		{
			resultList = ExtendedObjectProxy.getMQCPlanService().select("lotName = ? AND MQCState = ? AND jobName <> ?", new Object[] { planData.getLotName(), "Recycling", planData.getJobName() });
		}
		catch (Exception ne)
		{
			resultList = new ArrayList<MQCPlan>();
		}

		if (resultList.size() > 0)
		{
			//MQC-0009:Lot already has being in any MQC
			throw new CustomException("MQC-0009");
		}

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		CommonValidation.checkJobDownFlag(lotData);

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
		CommonValidation.checkLotProcessStateWait(lotData);
		CommonValidation.checkLotHoldState(lotData);
		CommonValidation.checkLotReworkState(lotData);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("RecycleMQC", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		List<Product> productList = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());
		List<ProductU> productUdfs = new ArrayList<ProductU>();
		{
			for (Product product : productList)
			{
				ProductU productU = new ProductU();
				productU.setProductName(product.getKey().getProductName());
				productU.setUdfs(product.getUdfs());

				productUdfs.add(productU);
			}
		}

		List<Object[]> batchArgs = new ArrayList<Object[]>();

		for (Element eleMQCPlan : SMessageUtil.getBodySequenceItemList(doc, "MQCPLANLIST", true))
		{
			String processOperationName = SMessageUtil.getChildText(eleMQCPlan, "PROCESSOPERATIONNAME", true);
			String processOperationVersion = SMessageUtil.getChildText(eleMQCPlan, "PROCESSOPERATIONVERSION", true);
			String sProductName = SMessageUtil.getChildText(eleMQCPlan, "PRODUCTNAME", true);
			String sPosition = SMessageUtil.getChildText(eleMQCPlan, "POSITION", true);

			// insert into CT_MQCPLANDETAIL_EXTENDED ProductData
			batchArgs.add(new Object[] { jobName, recycleProcessFlowName, recycleProcessFlowVersion, processOperationName, processOperationVersion, sProductName, sPosition, lotName,
					eventInfo.getEventName(), eventInfo.getEventTime(), eventInfo.getEventUser(), 0 });
		}

		// delete CT_MQCPLANDETAIL_EXTENDED Table (JobName Standard)
		ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().deleteMQCPlanDetail_ExtendedByFlow(jobName, recycleProcessFlowName, recycleProcessFlowVersion);

		StringBuilder sql = new StringBuilder();
		sql.setLength(0);
		sql.append("INSERT INTO CT_MQCPLANDETAIL_EXTENDED ");
		sql.append("   (JOBNAME, PROCESSFLOWNAME, PROCESSFLOWVERSION, PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION,  ");
		sql.append("    PRODUCTNAME, POSITION, LOTNAME, LASTEVENTNAME, LASTEVENTTIME,  ");
		sql.append("    LASTEVENTUSER, DUMMYUSEDCOUNT) ");
		sql.append(" VALUES ");
		sql.append("   (:JOBNAME, :PROCESSFLOWNAME, :PROCESSFLOWVERSION, :PROCESSOPERATIONNAME, :PROCESSOPERATIONVERSION, ");
		sql.append("    :PRODUCTNAME, :POSITION, :LOTNAME, :LASTEVENTNAME, :LASTEVENTTIME, ");
		sql.append("    :LASTEVENTUSER, :DUMMYUSEDCOUNT) ");

		if (batchArgs.size() > 0)
			GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().batchUpdate(sql.toString(), batchArgs);

		// create MQCPlanDetail
		List<MQCPlanDetail> planDetailList = new ArrayList<MQCPlanDetail>();

		for (Element eleMQCPlanDetail : SMessageUtil.getBodySequenceItemList(doc, "MQCPLANDETAILLIST", true))
		{
			String processOperationName = SMessageUtil.getChildText(eleMQCPlanDetail, "PROCESSOPERATIONNAME", true);
			String processOperationVersion = SMessageUtil.getChildText(eleMQCPlanDetail, "PROCESSOPERATIONVERSION", true);
			MQCPlanDetail planDetail = new MQCPlanDetail(jobName, recycleProcessFlowName, recycleProcessFlowVersion, processOperationName, processOperationVersion);
			{
				planDetail.setCarrierName(lotData.getCarrierName());
				planDetail.setLotName(lotData.getKey().getLotName());
				planDetail.setPosition(positions);
				planDetail.setLastEventComment(eventInfo.getEventComment());
				planDetail.setLastEventName(eventInfo.getEventName());
				planDetail.setLastEventTime(eventInfo.getEventTime());
				planDetail.setLastEventUser(eventInfo.getEventUser());
			}

			planDetailList.add(planDetail);
		}

		// delete CT_MQCPLANDETAIL Table (MQCRecycleFlow)
		ExtendedObjectProxy.getMQCPlanDetailService().deleteMQCPlanDetailByFlow(eventInfo, jobName, recycleProcessFlowName, recycleProcessFlowVersion);

		if (planDetailList.size() > 0)
			ExtendedObjectProxy.getMQCPlanDetailService().create(eventInfo, planDetailList);

		// first recycle node
		Node startNode = ProcessFlowServiceProxy.getProcessFlowService().getStartNode(new ProcessFlowKey(factoryName, recycleProcessFlowName, recycleProcessFlowVersion));
		Node toNodeStack = ProcessFlowServiceProxy.getProcessFlowService().getNextNode(startNode.getKey().getNodeId(), "Normal", "");

		String toNode = toNodeStack.getKey().getNodeId();

		String targetFlowName = toNodeStack.getProcessFlowName();
		String targetFlowVersion = toNodeStack.getProcessFlowVersion();
		String targetOperationName = toNodeStack.getNodeAttribute1();
		String targetOperationVer = toNodeStack.getNodeAttribute2();

		String[] step1 = StringUtils.split(lotData.getNodeStack(), ".");
		toNode = step1[0] + "." + toNode;

		// recycle MQC plan
		eventInfo.setEventName("RecycleMQC");
		ExtendedObjectProxy.getMQCPlanService().updateMQCPlanCount(eventInfo, planData, "Recycling");

		// move to MQC flow
		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo(lotData.getProductionType(), planData.getProductSpecName(), planData.getProductSpecVersion(), lotData.getProductSpec2Name(),
				lotData.getProductSpec2Version(), lotData.getProductRequestName(), lotData.getSubProductUnitQuantity1(), lotData.getSubProductUnitQuantity2(), lotData.getDueDate(),
				lotData.getPriority(), lotData.getFactoryName(), lotData.getAreaName(), lotData.getLotState(), lotData.getLotProcessState(), lotData.getLotHoldState(), targetFlowName,
				targetFlowVersion, targetOperationName, targetOperationVer, toNode, productUdfs);

		// set trace info
		changeSpecInfo.getUdfs().put("BEFOREFLOWNAME", lotData.getProcessFlowName());
		changeSpecInfo.getUdfs().put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());

		eventInfo.setEventName("RecycleMQC");
		Lot afterLot = MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);

		MESLotServiceProxy.getLotServiceImpl().deleteCtLotFutureActionByFlow(eventInfo, afterLot.getKey().getLotName(), afterLot.getFactoryName(), afterLot.getProcessFlowName(),
				afterLot.getProcessFlowVersion(), "1");

		return doc;
	}
}
