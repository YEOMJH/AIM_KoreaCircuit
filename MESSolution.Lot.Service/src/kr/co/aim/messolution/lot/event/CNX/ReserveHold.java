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
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ReserveHold extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String reserveSample = SMessageUtil.getBodyItemValue(doc, "RESERVESAMPLE", true);
		String mailFlag = SMessageUtil.getBodyItemValue(doc, "MAILFLAG", false);
		String requestDepartment = SMessageUtil.getBodyItemValue(doc, "REQUESTDEPARTMENT", false);
		String owner = SMessageUtil.getBodyItemValue(doc, "OWNER", false);

		List<Element> operationList = SMessageUtil.getBodySequenceItemList(doc, "OPERATIONLIST", true);
		List<Element> ReasonCodeList = SMessageUtil.getBodySequenceItemList(doc, "REASONCODELIST", true);

		String currentProductSpec = SMessageUtil.getBodyItemValue(doc, "CURRENTPRODUCTSPEC", true);
		String currentProcessFlow = SMessageUtil.getBodyItemValue(doc, "CURRENTPROCESSFLOW", true);
		String currentProcessOperation = SMessageUtil.getBodyItemValue(doc, "CURRENTPROCESSOPERATION", true);

		EventInfo eventInfo = new EventInfo();
		boolean actionFlag = false;

		// Validation
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		CommonValidation.checkDummyProductReserve(lotData);
		
		if (!StringUtils.equals(currentProductSpec, lotData.getProductSpecName()))
			throw new CustomException("LOT-0137", lotData.getProductSpecName());

		if (!StringUtils.equals(currentProcessFlow, lotData.getProcessFlowName()))
			throw new CustomException("LOT-0138", lotData.getProcessFlowName());

		if (!StringUtils.equals(currentProcessOperation, lotData.getProcessOperationName()))
			throw new CustomException("LOT-0139", lotData.getProcessOperationName());

		// Check ProcessFlow
		if (!checkProcessFlow(lotData, processFlowName, processFlowVersion))
			throw new CustomException("LOT-0081");

		for (Element operation : operationList)
		{
			String processOperationName = operation.getChildText("PROCESSOPERATIONNAME");
			String processOperationVersion = operation.getChildText("PROCESSOPERATIONVERSION");

			MESLotServiceProxy.getLotServiceUtil().checkMainReserveData(lotData, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion);

			for (Element code : ReasonCodeList)
			{
				String reasonCode = code.getChildText("REASONCODE");
				String reasonCodeType = code.getChildText("REASONCODETYPE")+ "[" + requestDepartment + "," + owner + "]";
				String beforeAction = code.getChildText("BEFOREACTION");
				String afterAction = code.getChildText("AFTERACTION");
				String permanentHold = code.getChildText("PERMANENTHOLD");
				String beforeActionComment = code.getChildText("BEFOREACTIONCOMMENT");
				String afterActionComment = code.getChildText("AFTERACTIONCOMMENT");
				
				String beforeActionUser = "";
				String afterActionUser = "";
				String beforeMailFlag = "";
				String afterMailFlag = "";

				// Check ReserveHoldList
				if (!checkReserveHoldList(lotName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, reasonCode, beforeAction, afterAction))
				{
					String actionType = checkActionType(lotName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, reasonCode);

					// Modify Set Before and After Comment
					if (StringUtils.equals(actionType, "Insert"))
					{
						eventInfo = EventInfoUtil.makeEventInfo(actionType + "FutureHold", getEventUser(), getFutureActionEventComment(processOperationName), null, null);
						eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

						// String actionFlag
						if (StringUtils.equals(beforeAction, "True"))
						{
							beforeActionUser = getEventUser();
							beforeMailFlag = mailFlag;
							//Mantis - 0000028
							beforeActionComment = getEventComment();
						}
						else
							beforeActionComment = "";

						if (StringUtils.equals(afterAction, "True"))
						{
							afterActionUser = getEventUser();
							afterMailFlag = mailFlag;
							//Mantis - 0000028
							afterActionComment = getEventComment();
						}
						else
							afterActionComment = "";

						ExtendedObjectProxy.getLotFutureActionService().insertLotFutureAction(eventInfo, lotName, factoryName, processFlowName, processFlowVersion, processOperationName,
								processOperationVersion, 0, reasonCode, reasonCodeType, "hold", "System", "", "", permanentHold, beforeAction, afterAction, beforeActionComment, afterActionComment,
								beforeActionUser, afterActionUser, beforeMailFlag, afterMailFlag);

						actionFlag = true;
					}
					else if (StringUtils.equals(actionType, "Update"))
					{
						String beforeUserByLot = "";
						String afterUserByLot = "";
						String beforeActionByLot = "";
						String afterActionByLot = "";
						String beforeActionCommentByLot = "";
						String afterActionCommentByLot = "";
						String beforeMailFlagByLot = "";
						String afterMailFlagByLot = "";

						List<LotFutureAction> reserveLotData = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataList(lotName, factoryName, processFlowName, processFlowVersion,
								processOperationName, processOperationVersion, 0, reasonCode);

						if (reserveLotData != null)
						{
							if (reserveLotData.size() == 1)
							{
								beforeActionByLot = reserveLotData.get(0).getBeforeAction();
								afterActionByLot = reserveLotData.get(0).getAfterAction();
								beforeUserByLot = reserveLotData.get(0).getBeforeActionUser();
								afterUserByLot = reserveLotData.get(0).getAfterActionUser();
								beforeActionCommentByLot = reserveLotData.get(0).getBeforeActionComment();
								afterActionCommentByLot = reserveLotData.get(0).getAfterActionComment();
								beforeMailFlagByLot = reserveLotData.get(0).getBeforeMailFlag();
								afterMailFlagByLot = reserveLotData.get(0).getAfterMailFlag();
							}
						}

						eventInfo = EventInfoUtil.makeEventInfo(actionType + "FutureHold", getEventUser(), getFutureActionEventComment(processOperationName), null, null);
						eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

						if (StringUtils.equals(beforeActionByLot, "False"))
						{
							if (StringUtils.equals(beforeAction, "True"))
							{
								beforeActionUser = getEventUser();
								beforeMailFlag = mailFlag;
								//Mantis - 0000028
								beforeActionComment = getEventComment();
							}
							else
							{
								beforeAction = beforeActionByLot;
								beforeActionUser = beforeUserByLot;
								beforeActionComment = beforeActionCommentByLot;
								beforeMailFlag = beforeMailFlagByLot;
							}
						}
						else
						{
							beforeAction = beforeActionByLot;
							beforeActionUser = beforeUserByLot;
							beforeActionComment = beforeActionCommentByLot;
							beforeMailFlag = beforeMailFlagByLot;
						}

						if (StringUtils.equals(afterActionByLot, "False"))
						{
							if (StringUtils.equals(afterAction, "True"))
							{
								afterActionUser = getEventUser();
								afterMailFlag = mailFlag;
								//Mantis - 0000028
								afterActionComment = getEventComment();
							}
							else
							{
								afterAction = afterActionByLot;
								afterActionUser = afterUserByLot;
								afterActionComment = afterActionCommentByLot;
								afterMailFlag = afterMailFlagByLot;
							}
						}
						else
						{
							afterAction = afterActionByLot;
							afterActionUser = afterUserByLot;
							afterActionComment = afterActionCommentByLot;
							afterMailFlag = afterMailFlagByLot;
						}

						ExtendedObjectProxy.getLotFutureActionService().updateLotFutureActionWithReasonCodeType(eventInfo, lotName, factoryName, processFlowName, processFlowVersion,
								processOperationName, processOperationVersion, 0, reasonCode, reasonCodeType, "hold", "System", "", "", permanentHold, beforeAction, afterAction, beforeActionComment,
								afterActionComment, beforeActionUser, afterActionUser, beforeMailFlag, afterMailFlag);

						actionFlag = true;
					}
				}
			}

			ProcessFlow processFlowData = CommonUtil.getProcessFlowData(lotData.getFactoryName(), processFlowName, processFlowVersion);

			if (StringUtils.equals(processFlowData.getProcessFlowType(), "Inspection") || StringUtils.equals(processFlowData.getProcessFlowType(), "Sample"))
			{
				if (StringUtils.equals(reserveSample, "Y"))
					MESLotServiceProxy.getLotServiceUtil()
							.setSamplingDataByFutureAction(eventInfo, lotData, processFlowName, processFlowVersion, processOperationName, processOperationVersion, "hold");
			}
		}

		if (!actionFlag)
			throw new CustomException("LOT-0083");

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

	private boolean checkReserveHoldList(String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion,
			String reasonCode, String beforeAction, String afterAction) throws CustomException
	{
		boolean checkFlag = true;

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT LOTNAME, REASONCODE, BEFOREACTION, AFTERACTION ");
		sql.append("  FROM CT_LOTFUTUREACTION ");
		sql.append(" WHERE LOTNAME = :LOTNAME ");
		sql.append("   AND FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		sql.append("   AND POSITION = '0' ");
		sql.append("   AND REASONCODE = :REASONCODE ");
		sql.append("   AND BEFOREACTION = :BEFOREACTION ");
		sql.append("UNION ");
		sql.append("SELECT LOTNAME, REASONCODE, BEFOREACTION, AFTERACTION ");
		sql.append("  FROM CT_LOTFUTUREACTION ");
		sql.append(" WHERE LOTNAME = :LOTNAME ");
		sql.append("   AND FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		sql.append("   AND POSITION = '0' ");
		sql.append("   AND REASONCODE = :REASONCODE ");
		sql.append("   AND AFTERACTION = :AFTERACTION ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("LOTNAME", lotName);
		args.put("FACTORYNAME", factoryName);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);
		args.put("PROCESSOPERATIONNAME", processOperationName);
		args.put("PROCESSOPERATIONVERSION", processOperationVersion);
		args.put("REASONCODE", reasonCode);
		args.put("BEFOREACTION", beforeAction);
		args.put("AFTERACTION", afterAction);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (sqlResult.size() > 0)
			checkFlag = true;
		else
			checkFlag = false;

		return checkFlag;
	}

	private String checkActionType(String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String reasonCode)
			throws CustomException
	{
		String actionType = "";

		List<LotFutureAction> dataInfo = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataList(lotName, factoryName, processFlowName, processFlowVersion, processOperationName,
				processOperationVersion, 0, reasonCode);

		if (dataInfo != null)
		{
			actionType = "Update";
		}
		else
		{
			actionType = "Insert";
		}

		return actionType;
	}
}
