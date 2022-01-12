package kr.co.aim.messolution.machine.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.EQPProcessTimeConf;
import kr.co.aim.messolution.extended.object.management.data.EnumDefValue;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class SetProcessTime extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String changeType = SMessageUtil.getBodyItemValue(doc, "CHAGETYPE", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		
		if (changeType.equals("Delete"))
		{
			List<Element> ruleList = SMessageUtil.getBodySequenceItemList(doc, "RULELIST", true);
			for (Element rule : ruleList)
			{
				String enumName = SMessageUtil.getChildText(rule, "ENUMNAME", true);
				if(enumName.equals("ProcessingMonitor"))
				{
					String enumValue = SMessageUtil.getChildText(rule, "ENUMVALUE", true);
					
					ExtendedObjectProxy.getEnumDefValueService().remove(enumName,enumValue);
				}
				else
				{
					String factoryName = SMessageUtil.getChildText(rule, "FACTORYNAME", true);
					String machineName = SMessageUtil.getChildText(rule, "MACHINENAME", true);
					String processOperationName = SMessageUtil.getChildText(rule, "PROCESSOPERATIONNAME", true);
					String productSpecName = SMessageUtil.getChildText(rule, "PRODUCTSPECNAME", true);
					String subMachineName = SMessageUtil.getChildText(rule, "SUBMACHINENAME", true);
					eventInfo.setEventName("Delete");
						
					EQPProcessTimeConf dataInfo = new EQPProcessTimeConf();
					dataInfo.setFactoryName(factoryName);
					dataInfo.setMachineName(machineName);
					dataInfo.setProductSpecName(productSpecName);
					dataInfo.setProcessOperationName(processOperationName);
					dataInfo.setSubMachineName(subMachineName);
					dataInfo.setMonitorType(enumName);
						
					ExtendedObjectProxy.getEQPProcessTimeConfService().remove(eventInfo, dataInfo);
				}
			}
		}
		else if (changeType.equals("Create"))
		{
			String enumName = SMessageUtil.getBodyItemValue(doc, "ENUMNAME", true);
			
			if(enumName.equals("ProcessingMonitor"))
			{
				String description = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
				//String enumValue = SMessageUtil.getBodyItemValue(doc, "ENUMVALUE", true);
				String defaultFlag = SMessageUtil.getBodyItemValue(doc, "DEFAULTFLAG", false);
				String displayColor = SMessageUtil.getBodyItemValue(doc, "DISPLAYCOLOR", false);
				String seq = SMessageUtil.getBodyItemValue(doc, "SEQ", false);
				List<Element> enumValueList = SMessageUtil.getBodySequenceItemList(doc, "ENUMVALUELIST", true);
			
				for (Element eleValue : enumValueList)
				{
					String enumValue = SMessageUtil.getChildText(eleValue, "ENUMVALUE", true);
					
					String sql = " INSERT INTO ENUMDEFVALUE (ENUMNAME,ENUMVALUE,DESCRIPTION,DEFAULTFLAG,SEQ,DISPLAYCOLOR) "
							+ " VALUES(:ENUMNAME,:ENUMVALUE,:DESCRIPTION,:DEFAULTFLAG,:SEQ,:DISPLAYCOLOR) ";
				
					Map<String, String> args = new HashMap<String, String>();
				
					args.put("ENUMNAME", enumName);
					args.put("ENUMVALUE", enumValue);
					args.put("DESCRIPTION", description);
					args.put("DEFAULTFLAG", defaultFlag);
					args.put("SEQ", seq);
					args.put("DISPLAYCOLOR", displayColor);
				
					GenericServiceProxy.getSqlMesTemplate().update(sql, args);
				}
			}
			else
			{
				String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
				String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
				String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
				String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
				String emailType = SMessageUtil.getBodyItemValue(doc, "EMAILTYPE", true);
				String ruleTime = SMessageUtil.getBodyItemValue(doc, "RULETIME", true);
				
				List<Element> subMachineNameList = SMessageUtil.getBodySequenceItemList(doc, "SUBMACHINENAMELIST", true);
				eventInfo.setEventName("Create");
				
				for (Element subMachineNameE : subMachineNameList)
				{
					String subMachineName = SMessageUtil.getChildText(subMachineNameE, "SUBMACHINENAME", true);
					
					EQPProcessTimeConf dataInfo = new EQPProcessTimeConf();
					dataInfo.setFactoryName(factoryName);
					dataInfo.setMonitorType(enumName);
					dataInfo.setMachineName(machineName);
					dataInfo.setProductSpecName(productSpecName);
					dataInfo.setProcessOperationName(processOperationName);
					dataInfo.setSubMachineName(subMachineName);
					dataInfo.setEmailType(emailType);
					dataInfo.setRuleTime(ruleTime);
					dataInfo.setLastEventComment(eventInfo.getEventComment());
					dataInfo.setLastEventName(eventInfo.getEventName());
					dataInfo.setLastEventTime(eventInfo.getEventTime());
					dataInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
					dataInfo.setLastEventUser(eventInfo.getEventUser());
					
					ExtendedObjectProxy.getEQPProcessTimeConfService().create(eventInfo, dataInfo);
				}
			}
		}
		else if (changeType.equals("Modify"))
		{
			List<Element> ruleList = SMessageUtil.getBodySequenceItemList(doc, "RULELIST", true);
			for (Element rule : ruleList)
			{
				String enumName = SMessageUtil.getChildText(rule, "ENUMNAME", true);
				if(enumName.equals("ProcessingMonitor"))
				{
					
				}
				else
				{
					eventInfo.setEventName("Modify");
					String factoryName = SMessageUtil.getChildText(rule, "FACTORYNAME", true);
					String machineName = SMessageUtil.getChildText(rule, "MACHINENAME", true);
					String processOperationName = SMessageUtil.getChildText(rule, "PROCESSOPERATIONNAME", true);
					String productSpecName = SMessageUtil.getChildText(rule, "PRODUCTSPECNAME", true);
					String subMachineName = SMessageUtil.getChildText(rule, "SUBMACHINENAME", true);
					String emailType = SMessageUtil.getBodyItemValue(doc, "EMAILTYPE", false);
					String ruleTime = SMessageUtil.getBodyItemValue(doc, "RULETIME", false);
						
					EQPProcessTimeConf dataInfo = new EQPProcessTimeConf();
					dataInfo.setFactoryName(factoryName);
					dataInfo.setMachineName(machineName);
					dataInfo.setProductSpecName(productSpecName);
					dataInfo.setProcessOperationName(processOperationName);
					dataInfo.setSubMachineName(subMachineName);
					dataInfo.setMonitorType(enumName);
					dataInfo.setLastEventComment(eventInfo.getEventComment());
					dataInfo.setLastEventName(eventInfo.getEventName());
					dataInfo.setLastEventTime(eventInfo.getEventTime());
					dataInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
					dataInfo.setLastEventUser(eventInfo.getEventUser());
					
					if(emailType != null && !emailType.isEmpty())
						dataInfo.setEmailType(emailType);
					
					if(ruleTime != null && !ruleTime.isEmpty())
						dataInfo.setRuleTime(ruleTime);
						
					ExtendedObjectProxy.getEQPProcessTimeConfService().modify(eventInfo, dataInfo);
				}
			}
		}
		
		return doc;
	}

}
