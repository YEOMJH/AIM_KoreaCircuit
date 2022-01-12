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
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ReserveMQCPlan extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String jobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String department = SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", false);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String recycleFlowName = SMessageUtil.getBodyItemValue(doc, "RECYCLEFLOWNAME", false);
		String recycleFlowVersion = SMessageUtil.getBodyItemValue(doc, "RECYCLEFLOWVERSION", false);
		String sRecycleLimit = SMessageUtil.getBodyItemValue(doc, "RECYCLELIMIT", false);
		String positions = SMessageUtil.getBodyItemValue(doc, "POSITIONS", true);

		List<Element> mqcPlanList = SMessageUtil.getBodySequenceItemList(doc, "MQCPLANLIST", true);
		List<Element> mqcPlanDetailList = SMessageUtil.getBodySequenceItemList(doc, "MQCPLANDETAILLIST", true);

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReserveMQC", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		MQCPlan planData = ExtendedObjectProxy.getMQCPlanService().selectByKey(false, new Object[] { jobName });

		if (!planData.getMQCState().equals("Created"))
		{
			//MQC-0007: MQC plan is not held
			throw new CustomException("MQC-0007");
		}

		CommonValidation.checkLotState(lotData);

		String processOperationName = "";
		String processOperationVersion = "";

		List<Object[]> batchArgs = new ArrayList<Object[]>();

		for (Element eleMQCPlan : mqcPlanList)
		{
			String sProcessOperationName = SMessageUtil.getChildText(eleMQCPlan, "PROCESSOPERATIONNAME", true);
			String sProcessOperationVersion = SMessageUtil.getChildText(eleMQCPlan, "PROCESSOPERATIONVERSION", true);
			String sProductName = SMessageUtil.getChildText(eleMQCPlan, "PRODUCTNAME", true);
			String sPosition = SMessageUtil.getChildText(eleMQCPlan, "POSITION", true);
			String sRecipeName = SMessageUtil.getChildText(eleMQCPlan, "RECIPENAME", true);
			String sMachineName = SMessageUtil.getChildText(eleMQCPlan, "MACHINENAME", true);

			processOperationName = sProcessOperationName;
			processOperationVersion = sProcessOperationVersion;

			if (StringUtils.isEmpty(sRecycleLimit))
				sRecycleLimit = "0";

			batchArgs.add(new Object[] { jobName, processFlowName, processFlowVersion, sProcessOperationName, sProcessOperationVersion, sProductName, sPosition, sMachineName, sRecipeName, lotName,
					eventInfo.getEventName(), eventInfo.getEventTime(), eventInfo.getEventUser(), 0 });

			// Delete CT_MQCPLANDETAIL_EXTENDED Table (JobName Standard)
			ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().deleteMQCPlanDetail_ExtendedWithoutProductName(jobName, processFlowName, processFlowVersion, sProcessOperationName,
					sProcessOperationVersion);
		}

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO CT_MQCPLANDETAIL_EXTENDED ");
		sql.append("   (JOBNAME, PROCESSFLOWNAME, PROCESSFLOWVERSION, PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION,  ");
		sql.append("    PRODUCTNAME, POSITION, MACHINENAME, RECIPENAME, LOTNAME, LASTEVENTNAME, LASTEVENTTIME,  ");
		sql.append("    LASTEVENTUSER, DUMMYUSEDCOUNT) ");
		sql.append(" VALUES ");
		sql.append("   (:JOBNAME, :PROCESSFLOWNAME, :PROCESSFLOWVERSION, :PROCESSOPERATIONNAME, :PROCESSOPERATIONVERSION, ");
		sql.append("    :PRODUCTNAME, :POSITION, :MACHINENAME, :RECIPENAME, :LOTNAME, :LASTEVENTNAME, :LASTEVENTTIME, ");
		sql.append("    :LASTEVENTUSER, :DUMMYUSEDCOUNT) ");

		if (batchArgs.size() > 0)
		{
			GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().batchUpdate(sql.toString(), batchArgs);
		}

		try
		{
			long seq = planData.getSeq();
			ExtendedObjectProxy.getMQCPlanService().delete(planData);

			planData = ExtendedObjectProxy.getMQCPlanService().insertMQCPlan(eventInfo, jobName, seq, lotData.getFactoryName(), productSpecName, productSpecVersion, processFlowName,
					processFlowVersion, lotData.getKey().getLotName(), department, recycleFlowName, recycleFlowVersion, lotData.getProductSpecName(), lotData.getProductSpecVersion(),
					Long.parseLong(sRecycleLimit));
		}
		catch (Exception e)
		{
		}

		// Delete CT_PlanDetail Table
		try
		{
			Object[] keySet = new Object[] { jobName, processFlowName, processFlowVersion, processOperationName, processOperationVersion };

			List<MQCPlanDetail> planDetailList = ExtendedObjectProxy.getMQCPlanDetailService().select(
					" jobName = ? and processFlowName = ? and processFlowVersion = ? and processOperationName = ? and processOperationVersion = ?", keySet);
			ExtendedObjectProxy.getMQCPlanDetailService().delete(planDetailList);
		}
		catch (Exception e)
		{
		}

		// create MQCPlan
		// all components are mandatory
		for (Element eleMQCPlanDetail : mqcPlanDetailList)
		{
			String sProcessOperationName = SMessageUtil.getChildText(eleMQCPlanDetail, "PROCESSOPERATIONNAME", true);
			String sProcessOperationVersion = SMessageUtil.getChildText(eleMQCPlanDetail, "PROCESSOPERATIONVERSION", true);
			String sDummyUsedLimit = SMessageUtil.getChildText(eleMQCPlanDetail, "USEDLIMIT", true);
			String recipeName = SMessageUtil.getChildText(eleMQCPlanDetail, "RECIPENAME", true);

			ExtendedObjectProxy.getMQCPlanDetailService().insertMQCPlanDetail(eventInfo, jobName, processFlowName, processFlowVersion, sProcessOperationName, sProcessOperationVersion,
					Long.parseLong(sDummyUsedLimit), lotData.getKey().getLotName(), lotData.getCarrierName(), positions, recipeName, "");

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
}
