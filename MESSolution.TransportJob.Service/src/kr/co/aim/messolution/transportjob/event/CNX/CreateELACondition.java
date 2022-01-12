package kr.co.aim.messolution.transportjob.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ELACondition;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import sun.util.logging.resources.logging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Element;

import com.sun.istack.internal.logging.Logger;
public class CreateELACondition extends SyncHandler{
	private static Log logger = LogFactory.getLog(CreateELACondition.class);

	public Object doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc,"MACHINENAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String eventName = SMessageUtil.getBodyItemValue(doc, "EVENTNAME", true);
		List<Element> ucList = SMessageUtil.getBodySequenceItemList(doc, "UCLIST", false);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(eventName, getEventUser(), getEventComment(), null, null);
		if(eventName.equals("DeleteELACondition"))
		{
			ELACondition ELACondition = ExtendedObjectProxy.getELAConditionService().selectByKey(false, new Object[] {machineName,productSpecName});
			ExtendedObjectProxy.getELAConditionService().remove(eventInfo, ELACondition);
		}
		else if(eventName.contains("ELACondition"))
		{
			
			for(Element List : ucList)
			{       
				String UC1 = SMessageUtil.getChildText(List, "UC1", false);
				String UC2 = SMessageUtil.getChildText(List, "UC2", false);
				String UC3 = SMessageUtil.getChildText(List, "UC3", false);
				String UC4 = SMessageUtil.getChildText(List, "UC4", false);
				String UC5 = SMessageUtil.getChildText(List, "UC5", false);
				String UC6 = SMessageUtil.getChildText(List, "UC6", false);
				String UC1Value = SMessageUtil.getChildText(List, "UC1Value", false);
				String UC2Value = SMessageUtil.getChildText(List, "UC2Value", false);
				String UC3Value = SMessageUtil.getChildText(List, "UC3Value", false);
				String UC4Value = SMessageUtil.getChildText(List, "UC4Value", false);
				String UC5Value = SMessageUtil.getChildText(List, "UC5Value", false);
				String UC6lowerValue = SMessageUtil.getChildText(List, "UC6lowerValue", false);
				String UC6UpperValue = SMessageUtil.getChildText(List, "UC6UpperValue", false);
				String nfcEarly = SMessageUtil.getChildText(List, "NFCEARLY", true);
				String nfcLate = SMessageUtil.getChildText(List, "NFCLATE", true);
				String nfcMidLower = SMessageUtil.getChildText(List, "NFCMIDLOWER", true);
				String nfcMidUpper = SMessageUtil.getChildText(List, "NFCMIDUPPER", true);
			
				ELACondition ELACondition = new ELACondition(machineName,productSpecName);
				ELACondition.setMachineName(machineName);
				ELACondition.setProductSpecName(productSpecName);
				ELACondition.setUc1(Double.valueOf(UC1).longValue());
				ELACondition.setUc2((UC2==null?null:Double.valueOf(UC2).longValue()));
				ELACondition.setUc3((UC3==null?null:Double.valueOf(UC3).longValue()));
				ELACondition.setUc4((UC4==null?null:Double.valueOf(UC4).longValue()));
				ELACondition.setUc5((UC5==null?null:Double.valueOf(UC5).longValue()));
				ELACondition.setUc6((UC6==null?null:Double.valueOf(UC6).longValue()));
				ELACondition.setUc1Value(Double.valueOf(UC1Value).longValue());
				ELACondition.setUc2Value(Double.valueOf(UC2Value).longValue());
				ELACondition.setUc3Value((UC3Value==null?null:Double.valueOf(UC3Value).longValue()));
				ELACondition.setUc4Value((UC4Value==null?null:Double.valueOf(UC4Value).longValue()));
				ELACondition.setUc5Value((UC5Value==null?null:Double.valueOf(UC5Value).longValue()));
				ELACondition.setUc6LowerValue((UC6lowerValue==null?null:Double.valueOf(UC6lowerValue).longValue()));
				ELACondition.setUc6UpperValue((UC6UpperValue==null?null:Double.valueOf(UC6UpperValue).longValue()));	
				ELACondition.setLastEventComment(eventInfo.getEventComment());
				ELACondition.setLastEventName(eventInfo.getEventName());
				ELACondition.setLastEventUser(eventInfo.getEventUser());
				ELACondition.setLastEventTime(TimeStampUtil.getTimestamp(TimeUtils.getCurrentTime(TimeUtils.FORMAT_DEFAULT)));
				ELACondition.setLastEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				ELACondition.setNfcEarly(Double.valueOf(nfcEarly).longValue());
				ELACondition.setNfcLate(Double.valueOf(nfcLate).longValue());
				ELACondition.setNfcMidLower(Double.valueOf(nfcMidLower).longValue());
				ELACondition.setNfcMidUpper(Double.valueOf(nfcMidUpper).longValue());
				
				if(eventName.equals("CreateELACondition"))
				{
					
					ExtendedObjectProxy.getELAConditionService().create(eventInfo, ELACondition);
				}
				else
				{
					ELACondition oldELACondition = ExtendedObjectProxy.getELAConditionService().selectByKey(false, new Object[] {machineName,productSpecName});
					ExtendedObjectProxy.getELAConditionService().updateToNew(oldELACondition, ELACondition);
				}
				
				
			}
		
			
		}
		else if(eventName.contains("LTT"))
		{
			List<Element> machineList = SMessageUtil.getBodySequenceItemList(doc, "MACHINELIST", true);
			if(eventName.equals("LTT_V_RIGHT"))
			{
				LTT_V_Right(machineList);
			}
			else if(eventName.equals("LTT_V_LEFT"))
			{
				LTT_V_LEFT(machineList);
			}
			else if(eventName.equals("LTT_H_RIGHT"))
			{
				LTT_H_Right(machineList);
			}
			else if(eventName.equals("LTT_H_LEFT"))
			{
				LTT_H_LEFT(machineList);
			}
		}
		
	return doc;
	}
	private void LTT_V_Right(List<Element> machineList) throws CustomException
	{
		try
		{
			StringBuffer sqlForSelect=new StringBuffer();
			sqlForSelect.append(" SELECT ENUMVALUE,DESCRIPTION FROM ENUMDEFVALUE  ");
			sqlForSelect.append("   WHERE ENUMNAME='LTTMappling' AND ENUMVALUE =:ENUMVALUE ");
			sqlForSelect.append("  AND DESCRIPTION=:DESCRIPTION ");
			
			StringBuffer sqlForDelete=new StringBuffer();
			sqlForDelete.append(" DELETE ENUMDEFVALUE WHERE ENUMNAME='LTTMappling' AND ENUMVALUE =:ENUMVALUE AND DESCRIPTION=:DESCRIPTION ");
					
			for(Element machine:machineList)
			{
				String machineName=SMessageUtil.getChildText(machine, "MACHINENAME", true);
				Map<String, String> bindMap = new HashMap<String, String>();
				bindMap.put("DESCRIPTION", "LTT_V");
				bindMap.put("ENUMVALUE", machineName);
				List<Map<String, Object>> aoiFlowSelect = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlForSelect.toString(), bindMap);
				if(aoiFlowSelect!=null&&aoiFlowSelect.size()>0)
				{
					GenericServiceProxy.getSqlMesTemplate().update(sqlForDelete.toString(), bindMap);
				}

			}
		}
		catch(FrameworkErrorSignal f)
		{
			throw new CustomException("SYS-0100","Please refresh data");
		}
		catch (Exception e) 
		{
			throw new CustomException("SYS-0100","Please refresh data");
		}
	}
	
	private void LTT_V_LEFT(List<Element> machineList) throws CustomException
	{
		try
		{
			StringBuffer sqlForSelect=new StringBuffer();
			sqlForSelect.append(" SELECT ENUMVALUE,DESCRIPTION FROM ENUMDEFVALUE  ");
			sqlForSelect.append("   WHERE ENUMNAME='LTTMappling' AND ENUMVALUE =:ENUMVALUE ");
			sqlForSelect.append("  AND DESCRIPTION=:DESCRIPTION ");
			
			StringBuffer sqlForDelete=new StringBuffer();
			sqlForDelete.append(" INSERT INTO ENUMDEFVALUE(ENUMNAME,ENUMVALUE,DESCRIPTION,DEFAULTFLAG,DISPLAYCOLOR,SEQ)  ");
			sqlForDelete.append(" VALUES('LTTMappling',:ENUMVALUE,:DESCRIPTION,'','','')  ");
					
			for(Element machine:machineList)
			{
				String machineName=SMessageUtil.getChildText(machine, "MACHINENAME", true);
				Map<String, String> bindMap = new HashMap<String, String>();
				bindMap.put("DESCRIPTION", "LTT_V");
				bindMap.put("ENUMVALUE", machineName);
				List<Map<String, Object>> aoiFlowSelect = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlForSelect.toString(), bindMap);
				if(aoiFlowSelect!=null&&aoiFlowSelect.size()<1)
				{
					GenericServiceProxy.getSqlMesTemplate().update(sqlForDelete.toString(), bindMap);
				}

			}
		}
		catch(FrameworkErrorSignal f)
		{
			throw new CustomException("SYS-0100","Please refresh data");
		}
		catch (Exception e) 
		{
			throw new CustomException("SYS-0100","Please refresh data");
		}
	}
	
	private void LTT_H_LEFT(List<Element> machineList) throws CustomException
	{
		try
		{
			StringBuffer sqlForSelect=new StringBuffer();
			sqlForSelect.append(" SELECT ENUMVALUE,DESCRIPTION FROM ENUMDEFVALUE  ");
			sqlForSelect.append("   WHERE ENUMNAME='LTTMappling' AND ENUMVALUE =:ENUMVALUE ");
			sqlForSelect.append("  AND DESCRIPTION=:DESCRIPTION ");
			
			StringBuffer sqlForDelete=new StringBuffer();
			sqlForDelete.append(" INSERT INTO ENUMDEFVALUE(ENUMNAME,ENUMVALUE,DESCRIPTION,DEFAULTFLAG,DISPLAYCOLOR,SEQ)  ");
			sqlForDelete.append(" VALUES('LTTMappling',:ENUMVALUE,:DESCRIPTION,'','','')  ");
					
			for(Element machine:machineList)
			{
				String machineName=SMessageUtil.getChildText(machine, "MACHINENAME", true);
				Map<String, String> bindMap = new HashMap<String, String>();
				bindMap.put("DESCRIPTION", "LTT_H");
				bindMap.put("ENUMVALUE", machineName);
				List<Map<String, Object>> aoiFlowSelect = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlForSelect.toString(), bindMap);
				if(aoiFlowSelect!=null&&aoiFlowSelect.size()<1)
				{
					GenericServiceProxy.getSqlMesTemplate().update(sqlForDelete.toString(), bindMap);
				}

			}
		}
		catch(FrameworkErrorSignal f)
		{
			throw new CustomException("SYS-0100","Please refresh data");
		}
		catch (Exception e) 
		{
			throw new CustomException("SYS-0100","Please refresh data");
		}
	}
	
	private void LTT_H_Right(List<Element> machineList) throws CustomException
	{
		try
		{
			StringBuffer sqlForSelect=new StringBuffer();
			sqlForSelect.append(" SELECT ENUMVALUE,DESCRIPTION FROM ENUMDEFVALUE  ");
			sqlForSelect.append("   WHERE ENUMNAME='LTTMappling' AND ENUMVALUE =:ENUMVALUE ");
			sqlForSelect.append("  AND DESCRIPTION=:DESCRIPTION ");
			
			StringBuffer sqlForDelete=new StringBuffer();
			sqlForDelete.append(" DELETE ENUMDEFVALUE WHERE ENUMNAME='LTTMappling' AND ENUMVALUE =:ENUMVALUE AND DESCRIPTION=:DESCRIPTION ");
					
			for(Element machine:machineList)
			{
				String machineName=SMessageUtil.getChildText(machine, "MACHINENAME", true);
				Map<String, String> bindMap = new HashMap<String, String>();
				bindMap.put("DESCRIPTION", "LTT_H");
				bindMap.put("ENUMVALUE", machineName);
				List<Map<String, Object>> aoiFlowSelect = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlForSelect.toString(), bindMap);
				if(aoiFlowSelect!=null&&aoiFlowSelect.size()>0)
				{
					GenericServiceProxy.getSqlMesTemplate().update(sqlForDelete.toString(), bindMap);
				}

			}
		}
		catch(FrameworkErrorSignal f)
		{
			throw new CustomException("SYS-0100","Please refresh data");
		}
		catch (Exception e) 
		{
			throw new CustomException("SYS-0100","Please refresh data");
		}
	}
}
