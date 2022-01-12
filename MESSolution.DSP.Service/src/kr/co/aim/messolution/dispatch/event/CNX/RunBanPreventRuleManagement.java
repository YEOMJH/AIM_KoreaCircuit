package kr.co.aim.messolution.dispatch.event.CNX;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.RunBanPreventRule;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class RunBanPreventRuleManagement extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String mode = SMessageUtil.getBodyItemValue(doc, "MODE", true);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("RunBanPreventRuleManagement", getEventUser(), getEventComment(), "", "");

		if (mode.equals("O"))
		{
			for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "RULELIST", false))
			{
				String factoryName = SMessageUtil.getChildText(eledur, "FACTORYNAME", false);
				String processFlowType = SMessageUtil.getChildText(eledur, "PROCESSFLOWTYPE", false);
				String processOperationName = SMessageUtil.getChildText(eledur, "PROCESSOPERATIONNAME", false);
				String processOperationVersion = SMessageUtil.getChildText(eledur, "PROCESSOPERATIONVERSION", false);
				String machineName = SMessageUtil.getChildText(eledur, "MACHINENAME", false);
				String flag = SMessageUtil.getChildText(eledur, "FLAG", false);
				String value = SMessageUtil.getChildText(eledur, "VALUE", false);
				
				RunBanPreventRule rule = new RunBanPreventRule();
				try 
				{
					rule = ExtendedObjectProxy.getRunBanPreventRuleService().selectByKey(false, new Object[] { factoryName, processFlowType, processOperationName, processOperationVersion, machineName, flag });
				} 
				catch (Exception e) 
				{
				}
				
				if (rule == null || rule.getProcessFlowType().isEmpty())
				{
					rule.setFactoryName(factoryName);
					rule.setProcessFlowType(processFlowType);
					rule.setProcessOperationName(processOperationName);
					rule.setProcessOperationVersion(processOperationVersion);
					rule.setMachineName(machineName);
					rule.setFlag(flag);
					rule.setValue(value);
					rule.setLastEventTime(eventInfo.getEventTime());
					rule.setLastEventUser(eventInfo.getEventUser());
					rule.setLastEventComment(eventInfo.getEventComment());
					rule.setLastEventTimeKey(TimeUtils.getCurrentEventTimeKey());
					
					ExtendedObjectProxy.getRunBanPreventRuleService().insert(rule);
				}
				else
				{
					rule.setValue(value);
					rule.setLastEventTime(eventInfo.getEventTime());
					rule.setLastEventUser(eventInfo.getEventUser());
					rule.setLastEventComment(eventInfo.getEventComment());
					rule.setLastEventTimeKey(TimeUtils.getCurrentEventTimeKey());
					
					ExtendedObjectProxy.getRunBanPreventRuleService().update(rule);
				}
			}
		}
		else
		{
			for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "RULELIST", false))
			{
				String factoryName = SMessageUtil.getChildText(eledur, "FACTORYNAME", false);
				String processFlowType = SMessageUtil.getChildText(eledur, "PROCESSFLOWTYPE", false);
				String processOperationName = SMessageUtil.getChildText(eledur, "PROCESSOPERATIONNAME", false);
				String processOperationVersion = SMessageUtil.getChildText(eledur, "PROCESSOPERATIONVERSION", false);
				String machineName = SMessageUtil.getChildText(eledur, "MACHINENAME", false);
				String flag = SMessageUtil.getChildText(eledur, "FLAG", false);
				
				RunBanPreventRule rule = new RunBanPreventRule();
				try 
				{
					rule = ExtendedObjectProxy.getRunBanPreventRuleService().selectByKey(false, new Object[] { factoryName, processFlowType, processOperationName, processOperationVersion, machineName, flag });
				} 
				catch (Exception e) 
				{
				}
				
				if (rule != null)
				{
					rule.setFactoryName(factoryName);
					rule.setProcessFlowType(processFlowType);
					rule.setProcessOperationName(processOperationName);
					rule.setProcessOperationVersion(processOperationVersion);
					rule.setMachineName(machineName);
					rule.setFlag(flag);
					
					ExtendedObjectProxy.getRunBanPreventRuleService().remove(eventInfo, rule);
				}
			}
		}
		
		return doc;
	}
}
