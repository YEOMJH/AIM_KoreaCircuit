package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ReserveSkip extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String mainOperationName = SMessageUtil.getBodyItemValue(doc, "MAINOPERATIONNAME", true);
		String mainOperationVersion = SMessageUtil.getBodyItemValue(doc, "MAINOPERATIONVERSION", true);
		List<Element> operationList = SMessageUtil.getBodySequenceItemList(doc, "OPERATIONLIST", true);

		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);

		checkLotInfoForFisrtGlass(lotName);
		CommonValidation.checkDummyProductReserve(lotData);
		
		// AR-AMF-0030-01
		// Check the existence of MainReserveSkip data
		CommonValidation.checkMainReserveSkipData(lotData, mainOperationName, mainOperationVersion);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReserveSkip", getEventUser(), "", "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		// insert FutureCondition
		for (Element element : operationList)
		{
			String processOperationName = element.getChildText("PROCESSOPERATIONNAME");
			String processOperationVersion = element.getChildText("PROCESSOPERATIONVERSION");
			String processFlowName = element.getChildText("PROCESSFLOWNAME");
			String processFlowVersion = element.getChildText("PROCESSFLOWVERSION");

			// Check ProcessFlow
			if (!checkProcessFlow(lotData, processFlowName, processFlowVersion))
				throw new CustomException("LOT-0081");

			eventInfo.setEventComment(getFutureActionEventComment(processOperationName));

			ExtendedObjectProxy.getLotFutureActionService().insertLotFutureActionForSkip(eventInfo, lotName, factoryName, processFlowName, processFlowVersion, processOperationName,
					processOperationVersion, 0, "Skip", "", "skip", "System", "", "", "");
		}

		return doc;

	}

	private String checkLotInfoForFisrtGlass(String lotName) throws CustomException
	{
		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);

		String firstGlassFlag = lotData.getUdfs().get("FIRSTGLASSFLAG");
		String jobName = lotData.getUdfs().get("JOBNAME");

		if (StringUtils.isNotEmpty(firstGlassFlag) && StringUtils.isNotEmpty(jobName))
			throw new CustomException("LOT-0048", lotName);

		return lotName;
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
