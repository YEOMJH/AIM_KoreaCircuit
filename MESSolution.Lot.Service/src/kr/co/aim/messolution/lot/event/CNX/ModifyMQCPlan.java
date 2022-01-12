package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCPlan;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ModifyMQCPlan extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String jobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);
		String department = SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String recycleFlowName = SMessageUtil.getBodyItemValue(doc, "RECYCLEFLOWNAME", false);
		String recycleFlowVersion = SMessageUtil.getBodyItemValue(doc, "RECYCLEFLOWVERSION", false);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String positions = SMessageUtil.getBodyItemValue(doc, "POSITIONS", true);
		String sRecycleLimit = SMessageUtil.getBodyItemValue(doc, "RECYCLELIMIT", false);

		List<Element> mqcPlanList = SMessageUtil.getBodySequenceItemList(doc, "MQCPLANLIST", true);
		List<Element> mqcPlanDetailList = SMessageUtil.getBodySequenceItemList(doc, "MQCPLANDETAILLIST", true);

		if (StringUtils.isEmpty(sRecycleLimit))
			sRecycleLimit = "0";

		long recycleLimit = Long.parseLong(sRecycleLimit);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyMQC", getEventUser(), getEventComment(), null, null);
		MQCPlan planData = ExtendedObjectProxy.getMQCPlanService().selectByKey(false, new Object[] { jobName });

		// MQC Department validation
		if (!planData.getDepartment().equals(department))
		{
			//MQC-0006: Other departments can't manage Lot.
			throw new CustomException("MQC-0006");
		}

		if (!planData.getMQCState().equals("Created") && !planData.getMQCState().equals("Suspending"))
		{
			//MQC-0007: MQC plan is not held
			throw new CustomException("MQC-0007");
		}

		if (!StringUtil.isEmpty(planData.getReturnFlowName()) && !StringUtil.isEmpty(planData.getReturnOperationName()))
			throw new CustomException("LOT-0027", planData.getJobName().toString());

		String prepareProductSpecName = planData.getPrepareSpecName();
		String prepareProductSpecVersion = planData.getPrepareSpecVersion();

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(planData.getLotName());

		CommonValidation.checkLotState(lotData);
		CommonValidation.checkLotHoldState(lotData);
		CommonValidation.checkLotReworkState(lotData);

		if (!lotData.getLotProcessState().equals(GenericServiceProxy.getConstantMap().Lot_WaitingToLogin))
			throw new CustomException("LOT-0016", lotData.getKey().getLotName(), lotData.getLotProcessState());

		List<Object[]> batchArgs = new ArrayList<Object[]>();

		StringBuilder sqlCnt = new StringBuilder();
		sqlCnt.append("SELECT NVL (MAX (DUMMYUSEDCOUNT), 0) AS DUMMYUSEDCOUNT ");
		sqlCnt.append("  FROM CT_MQCPLANDETAIL_EXTENDED ");
		sqlCnt.append(" WHERE PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sqlCnt.append("   AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sqlCnt.append("   AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sqlCnt.append("   AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		sqlCnt.append("   AND PRODUCTNAME = :PRODUCTNAME ");

		for (Element eleMQCPlan : mqcPlanList)
		{
			String sProductName = SMessageUtil.getChildText(eleMQCPlan, "PRODUCTNAME", true);
			String sPosition = SMessageUtil.getChildText(eleMQCPlan, "POSITION", true);
			String sProcessOperationName = SMessageUtil.getChildText(eleMQCPlan, "PROCESSOPERATIONNAME", true);
			String sProcessOperationVersion = SMessageUtil.getChildText(eleMQCPlan, "PROCESSOPERATIONVERSION", true);

			String dummyUsedCount = "0";

			Map<String, Object> args = new HashMap<String, Object>();
			args.put("PROCESSFLOWNAME", processFlowName);
			args.put("PROCESSFLOWVERSION", processFlowVersion);
			args.put("PROCESSOPERATIONNAME", sProcessOperationName);
			args.put("PROCESSOPERATIONVERSION", sProcessOperationVersion);
			args.put("PRODUCTNAME", sProductName);

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlCnt.toString(), args);

			if (result.size() > 0)
			{
				dummyUsedCount = ConvertUtil.getMapValueByName(result.get(0), "DUMMYUSEDCOUNT");
			}

			batchArgs.add(new Object[] { jobName, processFlowName, processFlowVersion, sProcessOperationName, sProcessOperationVersion, sProductName, sPosition, planData.getLotName(),
					eventInfo.getEventName(), eventInfo.getEventTime(), eventInfo.getEventUser(), Integer.parseInt(dummyUsedCount) });
		}

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO CT_MQCPLANDETAIL_EXTENDED ");
		sql.append("   (JOBNAME, PROCESSFLOWNAME, PROCESSFLOWVERSION, PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION,  ");
		sql.append("    PRODUCTNAME, POSITION, LOTNAME, LASTEVENTNAME, LASTEVENTTIME,  ");
		sql.append("    LASTEVENTUSER, DUMMYUSEDCOUNT) ");
		sql.append(" VALUES ");
		sql.append("   (:JOBNAME, :PROCESSFLOWNAME, :PROCESSFLOWVERSION, :PROCESSOPERATIONNAME, :PROCESSOPERATIONVERSION, ");
		sql.append("    :PRODUCTNAME, :POSITION, :LOTNAME, :LASTEVENTNAME, :LASTEVENTTIME, ");
		sql.append("    :LASTEVENTUSER, :DUMMYUSEDCOUNT) ");

		// delete CT_MQCPLANDETAIL_EXTENDED Table (JobName Standard)
		ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().deleteMQCPlanDetail_ExtendedByJobName(jobName);

		if (batchArgs.size() > 0)
		{
			GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().batchUpdate(sql.toString(), batchArgs);
		}

		// Delete CT_MQCPlan Table
		try
		{
			long seq = planData.getSeq();
			ExtendedObjectProxy.getMQCPlanService().delete(planData);

			planData = ExtendedObjectProxy.getMQCPlanService().insertMQCPlan(eventInfo, jobName, seq, lotData.getFactoryName(), productSpecName, productSpecVersion, processFlowName,
					processFlowVersion, lotData.getKey().getLotName(), department, recycleFlowName, recycleFlowVersion, prepareProductSpecName, prepareProductSpecVersion, recycleLimit);
		}
		catch (Exception e)
		{
		}

		// Delete CT_PlanDetail Table
		ExtendedObjectProxy.getMQCPlanDetailService().deleteMQCPlanDetailByJobName(eventInfo, jobName);

		// create MQCPlan
		// all components are mandatory
		for (Element eleMQCPlanDetail : mqcPlanDetailList)
		{
			String processOperationName = SMessageUtil.getChildText(eleMQCPlanDetail, "PROCESSOPERATIONNAME", true);
			String processOperationVersion = SMessageUtil.getChildText(eleMQCPlanDetail, "PROCESSOPERATIONVERSION", true);
			String sDummyUsedLimit = SMessageUtil.getChildText(eleMQCPlanDetail, "USEDLIMIT", true);
			String recipeName = SMessageUtil.getChildText(eleMQCPlanDetail, "RECIPENAME", false);
			String mQCReleaseFlag = SMessageUtil.getChildText(eleMQCPlanDetail, "MQCRELEASEFLAG", true);

			ExtendedObjectProxy.getMQCPlanDetailService().insertMQCPlanDetail(eventInfo, jobName, processFlowName, processFlowVersion, processOperationName, processOperationVersion,
					Long.parseLong(sDummyUsedLimit), lotData.getKey().getLotName(), lotData.getCarrierName(), positions, recipeName, mQCReleaseFlag);
		}

		return doc;
	}
}
