package kr.co.aim.messolution.generic.util.dblog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.object.log.ErrorMessageLogItems;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import xutil4j.collection.Queue;

public class ErrorMessageLogWriter implements Runnable {
	private Queue queue = new Queue();
	private boolean run = true;
	private Log log = LogFactory.getLog(ErrorMessageLogWriter.class);
	private String serverSequencedName = "";
	
	private String queryString = "INSERT INTO CT_ERRORMESSAGELOG "
			+ "		(SERVERNAME, EVENTNAME, EVENTUSER, TIMEKEY, TRANSACTIONID, IP, ERRORMESSAGE, MESSAGELOG) "
			+ "VALUES (:serverName, :eventName, :eventUser, :timeKey, :transactionId, :ip, :errorMessage, :messageLog) ";
	
	public ErrorMessageLogWriter(String serverName, String processSequence)
	{
		this.serverSequencedName = String.format("%s%s", serverName, processSequence);
	}
	
	public void run() {
		List<ErrorMessageLogItems> dequeuedList = new ArrayList<ErrorMessageLogItems>();
		while(run)
		{
			synchronized (getQueue()) {
				Object obj = getQueue().dequeue();
				if(obj == null)
					continue;
				else
				{
					dequeuedList.add((ErrorMessageLogItems)obj);
					if(getQueue().size() > 0)
					{
						for(int i=0;i<getQueue().size();i++)
						{
							dequeuedList.add((ErrorMessageLogItems) getQueue().dequeue());
						}
					}
					else
					{
						
					}
				}
			}
			writeToDB(dequeuedList);
			dequeuedList.clear();			
		}
		
	}
	
	private void writeToDB(List<ErrorMessageLogItems> errorMessageLogs)
	{
		List<Map<String, String>> bindMapList = new ArrayList<Map<String,String>>() ;
		for(ErrorMessageLogItems errorMessageLog : errorMessageLogs)
		{
			if(errorMessageLog == null)
				continue;
			Map<String, String> bindMap = new HashMap<String, String>();
			try {
				//Element root = errorLog.getXml().getDocument().getRootElement();
				// Set Variable
				//String serverName = getServerName();
				String eventName = SMessageUtil.getHeaderItemValue(errorMessageLog.getXml(), "MESSAGENAME", false);
				String eventUser = SMessageUtil.getHeaderItemValue(errorMessageLog.getXml(), "EVENTUSER", false);
				String timeKey = TimeStampUtil.getCurrentEventTimeKey();
				String transactionId = SMessageUtil.getHeaderItemValue(errorMessageLog.getXml(), "TRANSACTIONID", false);
				String fullMessage = JdomUtils.toString(errorMessageLog.getXml());
				
				// uncontrollable if not standardized form
				String errorMessage = StringUtils.EMPTY;
				if (errorMessageLog.getError() != null) {
					if (errorMessageLog.getError() instanceof CustomException)
						errorMessage = ((CustomException) errorMessageLog.getError()).errorDef
								.getLoc_errorMessage();
					else
						errorMessage = errorMessageLog.getError().getMessage();
//						errorMessage = error.getCause().getMessage();
				}
				
				bindMap.put("serverName", serverSequencedName);
				bindMap.put("eventName", eventName);
				bindMap.put("eventUser", eventUser);
				bindMap.put("timeKey", timeKey);
				bindMap.put("transactionId", transactionId);
				bindMap.put("ip", CommonUtil.getIp());
				bindMap.put("errorMessage", errorMessage);
				bindMap.put("messageLog", fullMessage);
				//bindMap.put("emptyFlag", errorMessageLog.getEmptyFlag());				
				bindMapList.add(bindMap);
			} catch (Exception e) {
				log.error(e);
			}
		}
		
		if(bindMapList.size() > 0)
		{
			try
			{
				if(bindMapList.size() == 1)
					GenericServiceProxy.getSqlMesTemplate().update(queryString, bindMapList.get(0));
				else
				{
					Map[] bindArray = bindMapList.toArray(new Map[bindMapList.size()]);					
					GenericServiceProxy.getSqlMesTemplate().updateBatch(queryString, bindArray);
				}
			}catch (Exception e) {
				log.error(e);
			}
		}
	}

	public Queue getQueue() {
		return queue;
	}

	public void setQueue(Queue queue) {
		this.queue = queue;
	}
}