package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LotFutureAction;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.lot.service.LotServiceImpl;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class CancelReserveSkip extends SyncHandler {

	private static Log log = LogFactory.getLog(LotServiceImpl.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		List<Element> operationList = SMessageUtil.getBodySequenceItemList(doc, "OPERATIONLIST", true);

		String comment = "";
		String operationNameList = "";
		
		for (Element operationName : operationList)
		{
			String processOperationName = operationName.getChildText("PROCESSOPERATIONNAME") + " ";
			operationNameList += processOperationName;
		}
		
		comment = "CancelResreveSkipOper:" + operationNameList + " " + getEventComment();

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelReserveSkip", getEventUser(), comment, null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);

		for (Element element : operationList)
		{
			String processOperationName = element.getChildText("PROCESSOPERATIONNAME");
			String processOperationVersion = element.getChildText("PROCESSOPERATIONVERSION");
			String processFlowName = element.getChildText("PROCESSFLOWNAME");
			String processFlowVersion = element.getChildText("PROCESSFLOWVERSION");

			// Check ProcessFlow
			if (!checkProcessFlow(lotData, processFlowName, processFlowVersion))
				throw new CustomException("LOT-0081");

			deleteCtLotFutureAction(eventInfo, lotName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, "0");
		}

		return doc;
	}

	public void deleteCtLotFutureAction(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, String position) throws CustomException
	{
		log.info("Delete LotFutureAction Start.");
		List<LotFutureAction> lotFutureActionList = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataWithActionName(lotName, factoryName, processFlowName, processFlowVersion,
				processOperationName, processOperationVersion, Integer.parseInt(position), "skip");

		if (lotFutureActionList == null)
			throw new CustomException("JOB-9001", lotName);

		eventInfo.setEventTimeKey(StringUtil.isEmpty(eventInfo.getEventTimeKey()) ? TimeStampUtil.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()) : eventInfo.getEventTimeKey());

		ExtendedObjectProxy.getLotFutureActionService().deleteLotFutureActionWithActionName(eventInfo, lotName, factoryName, processFlowName, processFlowVersion, processOperationName,
				processOperationVersion, Integer.parseInt(position), "skip");
		
		log.info("Delete LotFutureAction End.");
	}

	private boolean checkProcessFlow(Lot lotData, String processFlowName, String processFlowVersion)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT DISTINCT PF.PROCESSFLOWNAME, PF.PROCESSFLOWVERSION  ");
		sql.append("   FROM TPFOPOLICY TPFO, PROCESSFLOW PF ");
		sql.append("   WHERE TPFO.FACTORYNAME = :FACTORYNAME  ");
		sql.append("    AND TPFO.PRODUCTSPECNAME = :PRODUCTSPECNAME  ");
		sql.append("    AND TPFO.PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");
		sql.append("    AND PF.FACTORYNAME = TPFO.FACTORYNAME ");
		sql.append("    AND PF.PROCESSFLOWNAME = TPFO.PROCESSFLOWNAME ");
		sql.append("    AND PF.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION ");
		sql.append("    AND PF.PROCESSFLOWTYPE IN ('Inspection', 'Sample') ");
		sql.append("    AND PF.ACTIVESTATE='Active' ");
		sql.append("ORDER BY PF.PROCESSFLOWNAME, PF.PROCESSFLOWVERSION ");

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
}
