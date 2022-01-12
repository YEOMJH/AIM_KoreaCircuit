package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCPlan;
import kr.co.aim.messolution.extended.object.management.data.MQCPlanDetail;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

public class CompleteRecycleMQCPlan extends SyncHandler {

	public static Log log = LogFactory.getLog(CompleteRecycleMQCPlan.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String jobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);

		MQCPlan planData = ExtendedObjectProxy.getMQCPlanService().selectByKey(true, new Object[] { jobName });

		{// MQC validation
			if (!planData.getMQCState().equalsIgnoreCase("Recycling"))
			{
				//MQC-0005: MQC plan should be Recycling
				throw new CustomException("MQC-0005");
			}
		}

		String lotName = planData.getLotName();
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
		CommonValidation.checkLotProcessState(lotData);
		CommonValidation.checkLotHoldState(lotData);
		CommonValidation.checkLotReworkState(lotData);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);

		Node FirstNode = MESLotServiceProxy.getLotServiceUtil().getFirstNode(planData.getJobName(), planData.getFactoryName(), planData.getReturnFlowName(), planData.getReturnFlowVersion());

		String[] step1 = StringUtils.split(lotData.getNodeStack(), ".");
		String targetNodeStack = step1[0];

		Node targetNode = ProcessFlowServiceProxy.getProcessFlowService().getNode(targetNodeStack);

		String targetFlowName = targetNode.getProcessFlowName();
		String targetFlowVersion = targetNode.getProcessFlowVersion();
		String targetOperationName = targetNode.getNodeAttribute1();
		String targetOperationVersion = targetNode.getNodeAttribute2();
		String preProductSpecName = planData.getPrepareSpecName();
		String preProductSpecVersion = planData.getPrepareSpecVersion();
		String returnFlowName = FirstNode.getProcessFlowName(); // MQC Main Flow
		String returnFlowVersion = FirstNode.getProcessFlowVersion(); // MQC Main FlowVersion
		String returnOperationName = FirstNode.getNodeAttribute1(); // MQC Main Flow First OperationName
		String returnOperationVersion = FirstNode.getNodeAttribute2(); // MQC Main Flow First OperationVersion

		log.info("Start - Remove Recycle MQC Job");
		RemoveRecycleMQCJob(eventInfo, lotData);
		log.info("Completed - Remove Recycle MQC Job");

		eventInfo.setEventName("CompleteRecycleMQC");
		planData = ExtendedObjectProxy.getMQCPlanService().updateMQCPlanReturnInfo(eventInfo, planData, "Suspending", returnFlowName, returnFlowVersion, returnOperationName, returnOperationVersion);

		// move to MQC main flow except already in there
		if (!lotData.getProcessFlowName().equals(targetFlowName) && !lotData.getProcessOperationName().equals(targetOperationName))
		{
			ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo(lotData.getProductionType(), preProductSpecName, preProductSpecVersion, lotData.getProductSpec2Name(), lotData.getProductSpec2Version(),
					lotData.getProductRequestName(), lotData.getSubProductUnitQuantity1(), lotData.getSubProductUnitQuantity2(), lotData.getDueDate(), lotData.getPriority(), lotData.getFactoryName(),
					lotData.getAreaName(), lotData.getLotState(), lotData.getLotProcessState(), lotData.getLotHoldState(), targetFlowName, targetFlowVersion, targetOperationName,
					targetOperationVersion, targetNodeStack, new ArrayList<ProductU>());

			changeSpecInfo.setUdfs(lotData.getUdfs());
			changeSpecInfo.getUdfs().put("RETURNFLOWNAME", "");
			changeSpecInfo.getUdfs().put("RETURNOPERATIONNAME", "");
			changeSpecInfo.getUdfs().put("RETURNOPERATIONVER", "");
			changeSpecInfo.getUdfs().put("BEFOREFLOWNAME", returnFlowName);
			changeSpecInfo.getUdfs().put("BEFOREOPERATIONNAME", returnOperationName);

			eventInfo.setEventName("CompleteRecycleMQC");
			MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
		}
		else
		{
			try
			{
				SetEventInfo setEventInfo = MESLotServiceProxy.getLotInfoUtil().setEventInfo(lotData, 0, new ArrayList<ProductU>());
				LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
			}
			catch (Exception e)
			{
			}
		}

		return doc;
	}

	private void RemoveRecycleMQCJob(EventInfo eventInfo, Lot sourceLotData) throws CustomException
	{
		List<MQCPlan> jobList;
		List<MQCPlanDetail> PlanDetailList = new ArrayList<MQCPlanDetail>();

		try
		{
			jobList = ExtendedObjectProxy.getMQCPlanService().select("lotName = ? AND MQCstate = ?", new Object[] { sourceLotData.getKey().getLotName(), "Recycling" });
		}
		catch (greenFrameDBErrorSignal de)
		{
			if (de.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(de.getDataKey(), de.getSql());
			else
				throw new CustomException("SYS-8001", de.getSql());
		}

		if (jobList != null)
		{
			// recycling Lot is only one
			MQCPlan planData = jobList.get(0);
			String jobName = planData.getJobName();

			eventInfo.setEventName("RemoveRecycleMQC");

			StringBuilder sql = new StringBuilder();
			Map<String, Object> args = new HashMap<String, Object>();
			String MQCRecycleFlow = "";
			String MQCRecycleFlowVer = "";
			
			sql.append("SELECT DISTINCT ");
			sql.append("       D.JOBNAME, D.PROCESSFLOWNAME, D.PROCESSFLOWVERSION, D.PROCESSOPERATIONNAME, D.PROCESSOPERATIONVERSION ");
			sql.append("  FROM CT_MQCPLANDETAIL D, CT_MQCPLANDETAIL_EXTENDED E ");
			sql.append(" WHERE D.JOBNAME = E.JOBNAME ");
			sql.append("   AND D.PROCESSFLOWNAME = E.PROCESSFLOWNAME ");
			sql.append("   AND D.PROCESSFLOWVERSION = E.PROCESSFLOWVERSION ");
			sql.append("   AND D.PROCESSOPERATIONNAME = E.PROCESSOPERATIONNAME ");
			sql.append("   AND D.PROCESSOPERATIONVERSION = E.PROCESSOPERATIONVERSION ");
			sql.append("   AND D.JOBNAME = :JOBNAME ");
			sql.append("   AND E.PROCESSFLOWNAME IN (SELECT PROCESSFLOWNAME ");
			sql.append("                               FROM PROCESSFLOW ");
			sql.append("                              WHERE PROCESSFLOWTYPE = 'MQCRecycle') ");

			args.clear();
			args.put("JOBNAME", planData.getJobName());

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (result.size() > 0)
			{
				log.info("Exist MQC Recycle Job from CT_MQCPLANDETAIL_EXTENDED");
				MQCRecycleFlow = result.get(0).get("PROCESSFLOWNAME").toString();
				MQCRecycleFlowVer = result.get(0).get("PROCESSFLOWVERSION").toString();
				
				for (Map<String, Object> map : result)
				{
					String processFlowName = ConvertUtil.getMapValueByName(map, "PROCESSFLOWNAME");
					String processFlowVersion = ConvertUtil.getMapValueByName(map, "PROCESSFLOWVERSION");
					String processOperationName = ConvertUtil.getMapValueByName(map, "PROCESSOPERATIONNAME");
					String processOperationVersion = ConvertUtil.getMapValueByName(map, "PROCESSOPERATIONVERSION");

					MQCPlanDetail PlanDetail = ExtendedObjectProxy.getMQCPlanDetailService().selectByKey(false,
							new Object[] { planData.getJobName(), processFlowName, processFlowVersion, processOperationName, processOperationVersion });

					PlanDetailList.add(PlanDetail);
				}
			}
			else
			{
				log.info("Not exist MQC Recycle Job from CT_MQCPLANDETAIL_EXTENDED");
			}

			// Delete MQCPlanDetail_Extended Only Recycle MQC
			log.info(" Start delete MQC Recycle Job from  CT_MQCPLANDETAIL_EXTENDED");
			
			ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().deleteMQCPlanDetail_ExtendedByFlow(jobName, MQCRecycleFlow, MQCRecycleFlowVer);
			
			log.info(" End delete MQC Recycle Job from  CT_MQCPLANDETAIL_EXTENDED");

			// Delete CT_MQCPLANDETAIL Only Recycle MQC
			log.info(" Start delete MQC Recycle Job from  CT_MQCPLANDETAIL");
			for (MQCPlanDetail detailPlanData : PlanDetailList)
			{
				ExtendedObjectProxy.getMQCPlanDetailService().deleteMQCPlanDetail(eventInfo, detailPlanData);
			}
			log.info(" End delete MQC Recycle Job from CT_MQCPLANDETAIL");

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
