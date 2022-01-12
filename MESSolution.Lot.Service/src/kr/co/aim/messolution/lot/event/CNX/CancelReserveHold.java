package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LotFutureAction;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class CancelReserveHold extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> LotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", false);

		for (Element eledur : LotList)
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelReserveHold", this.getEventUser(), this.getEventComment(), "", "");

			String sFactory = SMessageUtil.getChildText(eledur, "FACTORYNAME", true);
			String sProcessFlowName = SMessageUtil.getChildText(eledur, "PROCESSFLOWNAME", true);
			String sProcessFlowVersion = SMessageUtil.getChildText(eledur, "PROCESSFLOWVERSION", true);
			String sProcessOperName = SMessageUtil.getChildText(eledur, "PROCESSOPERATIONNAME", true);
			String sProcessOperVersion = SMessageUtil.getChildText(eledur, "PROCESSOPERATIONVERSION", true);
			String sLotName = SMessageUtil.getChildText(eledur, "LOTNAME", true);
			String sReasonCode = SMessageUtil.getChildText(eledur, "REASONCODE", true);
			String sActionName = SMessageUtil.getChildText(eledur, "ACTIONNAME", true);
			String sActionType = SMessageUtil.getChildText(eledur, "ACTIONTYPE", true);
			String beforeActionUser = "";
			String afterActionUser = "";
			String beforeAction = "";
			String afterAction = "";
			String beforeActionComment = "";
			String afterActionComment = "";
			String reasonCodeType = "";
			String beforeMailFlag = "";
			String afterMailFlag = "";

			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(sLotName);

			List<LotFutureAction> reserveLotData = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataList(sLotName, sFactory, sProcessFlowName, sProcessFlowVersion,
					sProcessOperName, sProcessOperVersion, 0, sReasonCode);

			if (reserveLotData != null)
			{
				if (reserveLotData.size() == 1)
				{
					beforeActionUser = reserveLotData.get(0).getBeforeActionUser();
					afterActionUser = reserveLotData.get(0).getAfterActionUser();
					beforeAction = reserveLotData.get(0).getBeforeAction();
					afterAction = reserveLotData.get(0).getAfterAction();
					beforeActionComment = reserveLotData.get(0).getBeforeActionComment();
					afterActionComment = reserveLotData.get(0).getAfterActionComment();
					reasonCodeType = reserveLotData.get(0).getReasonCodeType();
					beforeMailFlag = reserveLotData.get(0).getBeforeMailFlag();
					afterMailFlag = reserveLotData.get(0).getAfterMailFlag();
				}
			}

			// Check ProcessFlow
			if (!checkProcessFlow(lotData, sProcessFlowName, sProcessFlowVersion))
				throw new CustomException("LOT-0081");

			if (StringUtil.equals(sReasonCode, "FirstGlassHold"))
				throw new CustomException("LOT-0108");

			if (StringUtils.equals(sActionName, "hold"))
			{
				if (StringUtils.equals(sActionType, "Before"))
				{
					//AR-TF-0001-01
					MESLotServiceProxy.getLotServiceUtil().checkDepartment(beforeActionUser, this.getEventUser(), lotData.getKey().getLotName());

					if (afterAction.equals("False"))
					{
						// delete
						ExtendedObjectProxy.getLotFutureActionService().deleteLotFutureActionData(eventInfo, sLotName, sFactory, sProcessFlowName, sProcessFlowVersion, sProcessOperName,
								sProcessOperVersion, 0, sReasonCode);
					}
					else
					{
						// update
						eventInfo = EventInfoUtil.makeEventInfo("CancelReserveHold", this.getEventUser(), this.getEventComment(), null, null);
						ExtendedObjectProxy.getLotFutureActionService()
								.updateLotFutureActionWithReasonCodeType(eventInfo, sLotName, sFactory, sProcessFlowName, sProcessFlowVersion, sProcessOperName, sProcessOperVersion, 0, sReasonCode,
										reasonCodeType, "hold", "System", "", "", "", "False", afterAction, "", afterActionComment, "", afterActionUser, "", afterMailFlag);
					}
				}
				else if (StringUtils.equals(sActionType, "After"))
				{
					//AR-TF-0001-01
					MESLotServiceProxy.getLotServiceUtil().checkDepartment(afterActionUser, this.getEventUser(), lotData.getKey().getLotName());
					
					if (StringUtils.equals(beforeAction, "False"))
					{
						// delete
						ExtendedObjectProxy.getLotFutureActionService().deleteLotFutureActionData(eventInfo, sLotName, sFactory, sProcessFlowName, sProcessFlowVersion, sProcessOperName,
								sProcessOperVersion, 0, sReasonCode);
					}
					else
					{
						// update
						eventInfo = EventInfoUtil.makeEventInfo("CancelReserveHold", this.getEventUser(), this.getEventComment(), null, null);
						ExtendedObjectProxy.getLotFutureActionService().updateLotFutureActionWithReasonCodeType(eventInfo, sLotName, sFactory, sProcessFlowName, sProcessFlowVersion, sProcessOperName,
								sProcessOperVersion, 0, sReasonCode, reasonCodeType, "hold", "System", "", "", "", beforeAction, "False", beforeActionComment, "", beforeActionUser, "",
								beforeMailFlag, "");
					}
				}
			}
		}

		for (Element lot : LotList)
		{
			EventInfo sampleEventInfo = EventInfoUtil.makeEventInfo("CancelReserveHold", this.getEventUser(), this.getEventComment(), null, null);
			sampleEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			String lotName = SMessageUtil.getChildText(lot, "LOTNAME", true);
			String factoryName = SMessageUtil.getChildText(lot, "FACTORYNAME", true);
			String processFlowName = SMessageUtil.getChildText(lot, "PROCESSFLOWNAME", true);
			String processFlowVersion = SMessageUtil.getChildText(lot, "PROCESSFLOWVERSION", true);
			String processOperationName = SMessageUtil.getChildText(lot, "PROCESSOPERATIONNAME", true);
			String processOperationVersion = SMessageUtil.getChildText(lot, "PROCESSOPERATIONVERSION", true);
			String actionName = SMessageUtil.getChildText(lot, "ACTIONNAME", true);

			if (checkFutureAction(lotName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, actionName))
			{
				Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
				MESLotServiceProxy.getLotServiceUtil().checkSampleDataByCancelReserveHold(sampleEventInfo, lotData, processFlowName, processFlowVersion, processOperationName, processOperationVersion);
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
		sql.append("   AND PF.PROCESSFLOWTYPE IN ('Inspection', 'Sample', 'Main', 'MQC', 'MQCPrepare','MQCRecycle','Rework') ");
		sql.append("   AND PF.ACTIVESTATE = 'Active' ");
		sql.append("ORDER BY NVL (LENGTH (TRIM (PF.PROCESSFLOWNAME)), 0) DESC, PF.PROCESSFLOWNAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("FACTORYNAME", lotData.getFactoryName());
		args.put("PRODUCTSPECNAME", lotData.getProductSpecName());
		args.put("PRODUCTSPECVERSION", lotData.getProductSpecVersion());

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

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

	private boolean checkFutureAction(String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion,
			String actionName) throws CustomException
	{
		boolean checkFlag = true;

		ProcessFlow processFlowData = CommonUtil.getProcessFlowData(factoryName, processFlowName, processFlowVersion);

		if (StringUtils.equals(processFlowData.getProcessFlowType(), "Inspection") || StringUtils.equals(processFlowData.getProcessFlowType(), "Sample"))
		{
			List<LotFutureAction> lotFutureActionData = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataWithActionName(lotName, factoryName, processFlowName, processFlowVersion,
					processOperationName, processOperationVersion, 0, actionName);

			if (lotFutureActionData != null)
				checkFlag = false;
		}
		else
		{
			checkFlag = false;
		}

		return checkFlag;
	}

}
