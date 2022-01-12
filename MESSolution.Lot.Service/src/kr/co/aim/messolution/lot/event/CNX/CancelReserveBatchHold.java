package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LotFutureAction;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class CancelReserveBatchHold extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", false);
		String processOperationVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelReserveHold", this.getEventUser(), this.getEventComment(), "", "");

		for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", false))
		{
			String lotName = SMessageUtil.getChildText(eledur, "LOTNAME", true);
			String reasonCode = SMessageUtil.getChildText(eledur, "REASONCODE", true);
			String actionName = SMessageUtil.getChildText(eledur, "ACTIONNAME", true);

			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

			// Check ProcessFlow
			if (!checkProcessFlow(lotData, processFlowName, processFlowVersion))
			{
				throw new CustomException("LOT-0081");
			}

			if (actionName.equals("hold"))
			{
				//AR-TF-0001-01
				LotFutureAction futureAction = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionData(lotName, factoryName, processFlowName, processFlowVersion, processOperationName,
						processOperationVersion, 0, reasonCode);

				if (futureAction != null)
				{
					String beforeAction = futureAction.getBeforeAction();
					String afterAction = futureAction.getAfterAction();

					if (StringUtils.equals(beforeAction, "True"))
					{
						MESLotServiceProxy.getLotServiceUtil().checkDepartment(futureAction.getBeforeActionUser(), this.getEventUser(), lotData.getKey().getLotName());
					}

					if (StringUtils.equals(afterAction, "True"))
					{
						MESLotServiceProxy.getLotServiceUtil().checkDepartment(futureAction.getAfterActionUser(), this.getEventUser(), lotData.getKey().getLotName());
					}

					MESLotServiceProxy.getLotServiceImpl().deleteCtLotFutureActionbyReasonCode(eventInfo, lotName, factoryName, processFlowName, processFlowVersion, processOperationName,
							processOperationVersion, "0", reasonCode);
				}
			}
		}
		return doc;
	}

	private boolean checkProcessFlow(Lot lotData, String processFlowName, String processFlowVersion)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT DISTINCT PF.PROCESSFLOWNAME, PF.DESCRIPTION, PF.PROCESSFLOWVERSION, PF.PROCESSFLOWTYPE ");
		sql.append("  FROM TPFOPOLICY TPFO, PROCESSFLOW PF ");
		sql.append(" WHERE TPFO.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND TPFO.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("   AND TPFO.PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");
		sql.append("   AND PF.FACTORYNAME = TPFO.FACTORYNAME ");
		sql.append("   AND PF.PROCESSFLOWNAME = TPFO.PROCESSFLOWNAME ");
		sql.append("   AND PF.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION ");
		sql.append("   AND PF.PROCESSFLOWTYPE IN ('Inspection', 'Sample', 'Main', 'MQC', 'MQCPrepare','MQCRecycle') ");
		sql.append("   AND PF.ACTIVESTATE = 'Active' ");
		sql.append("ORDER BY NVL (LENGTH (TRIM (PF.PROCESSFLOWNAME)), 0) DESC, PF.PROCESSFLOWNAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("FACTORYNAME", lotData.getFactoryName());
		args.put("PRODUCTSPECNAME", lotData.getProductSpecName());
		args.put("PRODUCTSPECVERSION", lotData.getProductSpecVersion());

		List<Map<String, Object>> result = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), args);

		boolean checkFlag = false;

		if (result.size() > 0)
		{
			for (Map<String, Object> flowList : result)
			{
				String queryFlowName = ConvertUtil.getMapValueByName(flowList, "PROCESSFLOWNAME");
				String queryFlowVersion = ConvertUtil.getMapValueByName(flowList, "PROCESSFLOWVERSION");

				if (StringUtils.equals(processFlowName, queryFlowName) && StringUtils.equals(processFlowVersion, queryFlowVersion))
				{
					checkFlag = true;
					break;
				}
			}
		}

		return checkFlag;
	}
}
