package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DSPProductRequestPlan;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.product.management.data.ProductSpec;

import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class DSPPlanWorkOrder extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String planDate = TimeStampUtil.getCurrentTime(TimeStampUtil.FORMAT_DEFAULT);
		String planName = "PLAN" + "-" + TimeStampUtil.getCurrentTime(TimeStampUtil.FORMAT_SIMPLE_DEFAULT);

		List<Element> planEList = SMessageUtil.getBodySequenceItemList(doc, "PLANLIST", true);

		List<ReserveLot> reserveLotList = new ArrayList<ReserveLot>();

		long priority = getPriority(factoryName, productSpecName, processFlowName);

		// ProductSpec info
		ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(factoryName, productSpecName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);

		for (Element planE : planEList)
		{
			String productRequestName = SMessageUtil.getChildText(planE, "PRODUCTREQUESTNAME", true);
			String positionPlan = SMessageUtil.getChildText(planE, "POSITION", true);
			String planLotQuantity = SMessageUtil.getChildText(planE, "PLANLOTQUANTITY", true);
			String planSheetQuantity = SMessageUtil.getChildText(planE, "PLANSHEETQUANTITY", true);

			List<Element> operationEList = SMessageUtil.getSubSequenceItemList(planE, "OPERATIONLIST", true);
			List<Element> lotEList = SMessageUtil.getSubSequenceItemList(planE, "LOTLIST", true);

			List<String> lotNameList = new ArrayList<>();
			List<String> operationList = new ArrayList<>();

			for (Element operationE : operationEList)
			{
				String machineName = SMessageUtil.getChildText(operationE, "MACHINENAME", true);
				String processOperationName = SMessageUtil.getChildText(operationE, "PROCESSOPERATIONNAME", true);

				operationList.add(processOperationName);

				DSPProductRequestPlan productRequestPlan = new DSPProductRequestPlan();

				productRequestPlan.setPlanName(planName);
				productRequestPlan.setProductRequestName(productRequestName);
				productRequestPlan.setProductSpecName(productSpecName);
				productRequestPlan.setProductSpecVersion(GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
				productRequestPlan.setProcessOperationName(processOperationName);
				productRequestPlan.setProcessOperationVersion(GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
				productRequestPlan.setProcessFlowName(processFlowName);
				productRequestPlan.setProcessFlowVersion(GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
				productRequestPlan.setMachineName(machineName);
				productRequestPlan.setPlanDate(planDate);
				productRequestPlan.setPlanLotQuantity(Integer.parseInt(planLotQuantity));
				productRequestPlan.setCreateLotQuantity(Integer.parseInt(planLotQuantity));
				productRequestPlan.setPlanSheetQuantity(Integer.parseInt(planSheetQuantity));
				productRequestPlan.setCreateSheetQuantity(Integer.parseInt(planSheetQuantity));
				productRequestPlan.setFactoryName(factoryName);
				productRequestPlan.setPosition(Long.parseLong(positionPlan));
				productRequestPlan.setPlanState("Created");
				productRequestPlan.setCreateUser(eventInfo.getEventUser());
				productRequestPlan.setCreateTime(eventInfo.getEventTime());
				productRequestPlan.setLastEventName(eventInfo.getEventName());
				productRequestPlan.setLastEventUser(eventInfo.getEventUser());
				productRequestPlan.setLastEventTime(eventInfo.getEventTime());
				productRequestPlan.setLastEventTimeKey(eventInfo.getEventTimeKey());
				productRequestPlan.setLastEventComment(eventInfo.getEventComment());
				productRequestPlan.setProductRequestHoldState("N");
				productRequestPlan.setPriority(priority);

				ExtendedObjectProxy.getDSPProductRequestPlanService().create(eventInfo, productRequestPlan);

				for (Element lotE : lotEList)
				{
					String lotName = SMessageUtil.getChildText(lotE, "LOTNAME", true);
					String positionLot = SMessageUtil.getChildText(lotE, "POSITION", true);

					lotNameList.add(lotName);

					// make ReserveLot
					ReserveLot reserveLot = new ReserveLot();
					reserveLot.setReserveUser(eventInfo.getEventUser());
					reserveLot.setReserveTimeKey(eventInfo.getEventTimeKey());
					reserveLot.setReserveState("Reserved");
					reserveLot.setProductSpecVersion(productSpecData.getKey().getProductSpecVersion());
					reserveLot.setProductSpecName(productSpecData.getKey().getProductSpecName());
					reserveLot.setProductRequestName(productRequestName);
					reserveLot.setProcessOperationVersion(GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
					reserveLot.setProcessOperationName(processOperationName);
					reserveLot.setPosition(Long.parseLong(positionLot));
					reserveLot.setPlanDate(planDate);
					reserveLot.setMachineName(machineName);
					reserveLot.setLotName(lotName);
					reserveLot.setFactoryName(factoryName);

					reserveLotList.add(reserveLot);

				}
			}

			// Validation
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT * ");
			sql.append("  FROM CT_RESERVELOT A ");
			sql.append(" WHERE A.LOTNAME IN (:LOTNAME) ");
			sql.append("   AND A.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
			sql.append("   AND A.PROCESSOPERATIONNAME IN (:PROCESSOPERATIONNAME) ");
			sql.append("   AND A.RESERVESTATE = :RESERVESTATE ");
			sql.append("   AND A.FACTORYNAME = :FACTORYNAME ");
			sql.append("   AND A.PRODUCTREQUESTNAME = :PRODUCTREQUESTNAME ");

			Map<String, Object> args = new HashMap<>();
			args.put("FACTORYNAME", factoryName);
			args.put("RESERVESTATE", "Reserved");
			args.put("PRODUCTSPECNAME", productSpecName);
			args.put("PRODUCTREQUESTNAME", productRequestName);
			args.put("PROCESSOPERATIONNAME", operationList);

			List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

			if (lotNameList.size() > 800)
			{
				for (int i = 0; lotNameList.size() / 800 > i - 1; i++)
				{
					int toIndex = i * 800 + 799;

					if (toIndex > lotNameList.size())
						toIndex = lotNameList.size();

					args.put("LOTNAME", lotNameList.subList(i * 800, toIndex));
					result = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), args);

					if (result.size() > 0)
						throw new CustomException("DSP-0002");
				}
			}
			else
			{
				args.put("LOTNAME", lotNameList);
				result = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), args);

				if (result.size() > 0)
					throw new CustomException("DSP-0002");
			}
		}

		// insert into CT_RESERVELOT Table
		ExtendedObjectProxy.getReserveLotService().insert(reserveLotList);

		// insert into CT_RESERVELOT History Table
		ExtendedObjectProxy.getReserveLotService().addHistory(eventInfo, "ReserveLotHistory", reserveLotList, LogFactory.getLog(ReserveLot.class));

		return doc;
	}

	private long getPriority(String factoryName, String productSpecName, String processFlowName)
	{
		long priority = 1;

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * ");
		sql.append("  FROM (SELECT A.PLANNAME, ");
		sql.append("               A.PRIORITY, ");
		sql.append("               A.FACTORYNAME, ");
		sql.append("               A.PRODUCTSPECNAME, ");
		sql.append("               A.PRODUCTSPECVERSION, ");
		sql.append("               A.PROCESSFLOWNAME, ");
		sql.append("               A.PROCESSFLOWVERSION, ");
		sql.append("               DECODE (MAX (A.PLANSTATE), MIN (A.PLANSTATE), MAX (A.PLANSTATE), 'Processing') PLANSTATE ");
		sql.append("          FROM CT_DSPPRODUCTREQUESTPLAN A ");
		sql.append("         WHERE A.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("           AND A.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("           AND A.FACTORYNAME = :FACTORYNAME ");
		sql.append("        GROUP BY A.PLANNAME, ");
		sql.append("                 A.PRIORITY, ");
		sql.append("                 A.PRODUCTSPECNAME, ");
		sql.append("                 A.PRODUCTSPECVERSION, ");
		sql.append("                 A.PROCESSFLOWNAME, ");
		sql.append("                 A.PROCESSFLOWVERSION, ");
		sql.append("                 A.FACTORYNAME) ");
		sql.append(" WHERE PLANSTATE <> 'Completed' ");
		sql.append("ORDER BY PRIORITY DESC ");


		Map<String, Object> args = new HashMap<>();

		args.put("FACTORYNAME", factoryName);
		args.put("PRODUCTSPECNAME", productSpecName);
		args.put("PROCESSFLOWNAME", processFlowName);

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), args);

		if (result != null && result.size() > 0)
			priority = Long.parseLong(result.get(0).get("PRIORITY").toString()) + 1;

		return priority;
	}
}
