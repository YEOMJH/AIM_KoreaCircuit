package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class CreateTPTJRule extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String ruleName = SMessageUtil.getBodyItemValue(doc, "RULENAME", false);

		List<Element> operationList = SMessageUtil.getBodySequenceItemList(doc, "OPERATIONLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateTPTJRule", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		if (StringUtils.isEmpty(ruleName))
		{
			Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
			List<String> nameList = CommonUtil.generateNameByNamingRule("TPTJRuleNaming", nameRuleAttrMap, 1);
			ruleName = nameList.get(0);
		}

		int ruleNum = getRuleNum(factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion);

		for (Element operation : operationList)
		{
			List<Element> sampleList = SMessageUtil.getSubSequenceItemList(operation, "SAMPLELIST", true);
			ruleNum += 1;

			for (Element sample : sampleList)
			{
				String processOperationName = SMessageUtil.getChildText(sample, "PROCESSOPERATIONNAME", true);
				String processOperationVersion = SMessageUtil.getChildText(sample, "PROCESSOPERATIONVERSION", true);
				String sampleProcessFlowName = SMessageUtil.getChildText(sample, "SAMPLEPROCESSFLOWNAME", true);
				String sampleProcessFlowVersion = SMessageUtil.getChildText(sample, "SAMPLEPROCESSFLOWVERSION", true);
				String sampleOperationName = SMessageUtil.getChildText(sample, "SAMPLEOPERATIONNAME", true);
				String sampleOperationVersion = SMessageUtil.getChildText(sample, "SAMPLEOPERATIONVERSION", true);
				String firstFlowFlag = SMessageUtil.getChildText(sample, "FIRSTFLOWFLAG", false);
				if(StringUtils.equals(firstFlowFlag, "True"))
				{
					firstFlowFlag="Y";
				}
				else 
				{
					firstFlowFlag="";
				}

				ExtendedObjectProxy.getTPTJRuleService().insertTPTJRule(eventInfo, ruleName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion,
						processOperationName, processOperationVersion, sampleProcessFlowName, sampleProcessFlowVersion, sampleOperationName, sampleOperationVersion, ruleNum,firstFlowFlag);
			}
		}

		return doc;
	}

	private int getRuleNum(String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion)
	{
		String maxRuleNum = "";

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT MAX (RULENUM) AS MAXRULENUM ");
		sql.append("  FROM CT_TPTJRULE ");
		sql.append(" WHERE FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("   AND PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");
		sql.append("   AND PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("FACTORYNAME", factoryName);
		args.put("PRODUCTSPECNAME", productSpecName);
		args.put("PRODUCTSPECVERSION", productSpecVersion);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (sqlResult.size() > 0)
		{
			maxRuleNum = ConvertUtil.getMapValueByName(sqlResult.get(0), "MAXRULENUM");

			if (StringUtils.isEmpty(maxRuleNum))
				maxRuleNum = "0";
		}

		return Integer.parseInt(maxRuleNum);
	}

}
