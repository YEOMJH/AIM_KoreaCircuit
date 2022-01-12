package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class SaveProductionRule extends SyncHandler 
{
	private static Log log = LogFactory.getLog(SaveProductionRule.class);

	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		String priority = SMessageUtil.getBodyItemValue(doc, "PRIORITY", false);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", false);
		String processOperation = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATION", false);
		String DSPLock = SMessageUtil.getBodyItemValue(doc, "DSPLOCK", true);
		String ruleType = SMessageUtil.getBodyItemValue(doc, "RULETYPE", true);
		String eventTimekey = SMessageUtil.getBodyItemValue(doc, "EVENTTIMEKEY", false);	
		
		String currentTimekey = TimeUtils.getEventTimeKeyFromTimestamp(TimeStampUtil.getCurrentTimestamp());
		String eventUser = getEventUser();
		
		String existSQL = "SELECT EVENTTIMEKEY FROM EQPPRODUCTIONRULE WHERE EVENTTIMEKEY = :EVENTTIMEKEY";
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("EVENTTIMEKEY", eventTimekey);
		
		List<Map<String, Object>> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(existSQL, bindMap);
		
		if(sqlResult.size() > 0)
		{
			log.info("Start Update EQPProductionRule ");
			String updateSQL = "UPDATE EQPPRODUCTIONRULE SET FACTORYNAME = :FACTORYNAME, MACHINENAME = :MACHINENAME, " +
								"PORTNAME = :PORTNAME, PRIORITY = :PRIORITY, PRODUCTSPECNAME = :PRODUCTSPECNAME, " +
								"PROCESSOPERATION = :PROCESSOPERATION, DSPLOCK = :DSPLOCK, RULETYPE = :RULETYPE, " +
								"EVENTTIMEKEY = :CURRENTTIMEKEY, EVENTUSER = :EVENTUSER " +
								"WHERE EVENTTIMEKEY = :EVENTTIMEKEY";
			Map<String, Object> updateBindMap = new HashMap<String, Object>();
			updateBindMap.put("FACTORYNAME", factoryName);
			updateBindMap.put("MACHINENAME", machineName);
			updateBindMap.put("PORTNAME", portName);
			updateBindMap.put("PRIORITY", priority);
			updateBindMap.put("PRODUCTSPECNAME", productSpecName);
			updateBindMap.put("PROCESSOPERATION", processOperation);
			updateBindMap.put("DSPLOCK", DSPLock);
			updateBindMap.put("RULETYPE", ruleType);
			updateBindMap.put("CURRENTTIMEKEY", currentTimekey);
			updateBindMap.put("EVENTUSER", eventUser);
			updateBindMap.put("EVENTTIMEKEY", eventTimekey);
			
			if (kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().update(updateSQL, updateBindMap) > 0)
			{
				log.info("Update Success !");
			}
		}
		else 
		{
			log.info("Start Insert POSHold :");
			String insertSQL = "INSERT INTO EQPPRODUCTIONRULE (FACTORYNAME, MACHINENAME, PORTNAME, PRIORITY, PRODUCTSPECNAME, " +
								"PROCESSOPERATION, DSPLOCK, RULETYPE, EVENTTIMEKEY, EVENTUSER) VALUES (:FACTORYNAME, :MACHINENAME, :PORTNAME, :PRIORITY, " +
								":PRODUCTSPECNAME, :PROCESSOPERATION, :DSPLOCK, :RULETYPE, :CURRENTTIMEKEY, :EVENTUSER)";
			Map<String, Object> insertBindMap = new HashMap<String, Object>();
			insertBindMap.put("FACTORYNAME", factoryName);
			insertBindMap.put("MACHINENAME", machineName);
			insertBindMap.put("PORTNAME", portName);
			insertBindMap.put("PRIORITY", priority);
			insertBindMap.put("PRODUCTSPECNAME", productSpecName);
			insertBindMap.put("PROCESSOPERATION", processOperation);
			insertBindMap.put("DSPLOCK", DSPLock);
			insertBindMap.put("RULETYPE", ruleType);
			insertBindMap.put("CURRENTTIMEKEY", currentTimekey);
			insertBindMap.put("EVENTUSER", eventUser);
			
			if (kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().update(insertSQL, insertBindMap) > 0)
			{
				log.info("Insert Success !");
			}
		}
		
		return doc;		
	}
}
