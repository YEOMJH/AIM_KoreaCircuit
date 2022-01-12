package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.GenericServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class RemoveTPTJRule extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);

		List<Element> ruleList = SMessageUtil.getBodySequenceItemList(doc, "RULELIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("RemoveTPTJRule", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		String checkRuleNum = "";

		for (Element rule : ruleList)
		{
			String processOperationName = rule.getChildText("PROCESSOPERATIONNAME");
			String processOperationVersion = rule.getChildText("PROCESSOPERATIONVERSION");
			String sampleProcessFlowName = rule.getChildText("SAMPLEPROCESSFLOWNAME");
			String sampleProcessFlowVersion = rule.getChildText("SAMPLEPROCESSFLOWVERSION");
			String sampleOperationName = rule.getChildText("SAMPLEOPERATIONNAME");
			String sampleOperationVersion = rule.getChildText("SAMPLEOPERATIONVERSION");
			String ruleName = rule.getChildText("RULENAME");
			String ruleNum = rule.getChildText("RULENUM");

			if (!StringUtils.equals(checkRuleNum, ruleNum))
			{
				List<Map<String, Object>> ruleProductList = getTPTJProductList(factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, ruleName, ruleNum);

				if (ruleProductList.size() > 0)
				{
					String errorMsg = getErrorMsg(ruleProductList);
					//throw new CustomException("SYS-0010", errorMsg);
					eventLog.info(errorMsg);
				}

				checkRuleNum = ruleNum;
			}

			ExtendedObjectProxy.getTPTJRuleService().removeTPTJRule(eventInfo, ruleName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName,
					processOperationVersion, sampleProcessFlowName, sampleProcessFlowVersion, sampleOperationName, sampleOperationVersion);
		}

		return doc;
	}

	private List<Map<String, Object>> getTPTJProductList(String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion, String ruleName,
			String ruleNum)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT PROCESSOPERATIONNAME, ");
		sql.append("       PROCESSOPERATIONVERSION, ");
		sql.append("       SAMPLEPROCESSFLOWNAME, ");
		sql.append("       SAMPLEPROCESSFLOWVERSION, ");
		sql.append("       SAMPLEOPERATIONNAME, ");
		sql.append("       SAMPLEOPERATIONVERSION ");
		sql.append("  FROM CT_TPTJPRODUCT ");
		sql.append(" WHERE FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("   AND PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");
		sql.append("   AND PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND RULENAME = :RULENAME ");
		sql.append("   AND RULENUM = :RULENUM ");
		sql.append("GROUP BY PROCESSOPERATIONNAME, ");
		sql.append("         PROCESSOPERATIONVERSION, ");
		sql.append("         SAMPLEPROCESSFLOWNAME, ");
		sql.append("         SAMPLEPROCESSFLOWVERSION, ");
		sql.append("         SAMPLEOPERATIONNAME, ");
		sql.append("         SAMPLEOPERATIONVERSION ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("FACTORYNAME", factoryName);
		args.put("PRODUCTSPECNAME", productSpecName);
		args.put("PRODUCTSPECVERSION", productSpecVersion);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);
		args.put("RULENAME", ruleName);
		args.put("RULENUM", ruleNum);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		return sqlResult;
	}

	private String getErrorMsg(List<Map<String, Object>> ruleProductList)
	{
		String msg = "Data exists in the process -> Info : ";

		for (Map<String, Object> ruleProduct : ruleProductList)
		{
			String processOperationName = ConvertUtil.getMapValueByName(ruleProduct, "PROCESSOPERATIONNAME");
			String sampleProcessFlowName = ConvertUtil.getMapValueByName(ruleProduct, "SAMPLEPROCESSFLOWNAME");
			String sampleOperationName = ConvertUtil.getMapValueByName(ruleProduct, "SAMPLEOPERATIONNAME");

			msg += "<OPERATION[" + processOperationName + "], SAMPLEFLOW[" + sampleProcessFlowName + "], SAMPLEOPERATION[" + sampleOperationName + "]> ";
		}

		return msg;
	}
}
