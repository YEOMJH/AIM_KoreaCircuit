package kr.co.aim.messolution.dispatch.event.CNX;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.RunBanRule;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class RunBanRuleManagement extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String mode = SMessageUtil.getBodyItemValue(doc, "MODE", true);
		EventInfo eventInfo = new EventInfo();
		eventInfo.setBehaviorName( "greenTrack" );
		eventInfo.setEventUser( getEventUser() );
		eventInfo.setEventComment( getEventComment() );
		eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());

		if (mode.equals("O"))
		{
			for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "RULELIST", false))
			{
				String factoryName = SMessageUtil.getChildText(eledur, "FACTORYNAME", false);
				String processFlowType = SMessageUtil.getChildText(eledur, "PROCESSFLOWTYPE", false);
				String processOperationName = SMessageUtil.getChildText(eledur, "PROCESSOPERATIONNAME", false);
				String processOperationVersion = SMessageUtil.getChildText(eledur, "PROCESSOPERATIONVERSION", false);
				String machineName = SMessageUtil.getChildText(eledur, "MACHINENAME", false);
				String unitName = SMessageUtil.getChildText(eledur, "UNITNAME", false);
				String subUnitName = SMessageUtil.getChildText(eledur, "SUBUNITNAME", false);
				String addFlag = SMessageUtil.getChildText(eledur, "ADDFLAG", false);
				String deleteFlag = SMessageUtil.getChildText(eledur, "DELETEFLAG", false);
				
				RunBanRule rule = new RunBanRule();
				try 
				{
					rule = ExtendedObjectProxy.getRunBanRuleService().selectByKey(false, new Object[] { factoryName, processFlowType, processOperationName, processOperationVersion, machineName, unitName, subUnitName });
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
					rule.setUnitName(unitName);
					rule.setSubUnitName(subUnitName);
					rule.setAddFlag(addFlag);
					rule.setDeleteFlag(deleteFlag);
					rule.setLastEventTime(eventInfo.getEventTime());
					rule.setLastEventUser(eventInfo.getEventUser());
					rule.setLastEventComment(eventInfo.getEventComment());
					rule.setLastEventTimeKey(TimeUtils.getCurrentEventTimeKey());
					
					ExtendedObjectProxy.getRunBanRuleService().insert(rule);
				}
				else
				{
					rule.setAddFlag(addFlag);
					rule.setDeleteFlag(deleteFlag);
					rule.setLastEventTime(eventInfo.getEventTime());
					rule.setLastEventUser(eventInfo.getEventUser());
					rule.setLastEventComment(eventInfo.getEventComment());
					rule.setLastEventTimeKey(TimeUtils.getCurrentEventTimeKey());
					
					ExtendedObjectProxy.getRunBanRuleService().update(rule);
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
				String unitName = SMessageUtil.getChildText(eledur, "UNITNAME", false);
				String subUnitName = SMessageUtil.getChildText(eledur, "SUBUNITNAME", false);
				
				RunBanRule rule = new RunBanRule();
				try 
				{
					rule = ExtendedObjectProxy.getRunBanRuleService().selectByKey(false, new Object[] { factoryName, processFlowType, processOperationName, processOperationVersion, machineName, unitName, subUnitName });
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
					rule.setUnitName(unitName);
					rule.setSubUnitName(subUnitName);
					
					ExtendedObjectProxy.getRunBanRuleService().delete(rule);
				}
			}
		}
		
		return doc;
	}
}
