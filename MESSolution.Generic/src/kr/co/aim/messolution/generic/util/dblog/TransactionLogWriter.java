package kr.co.aim.messolution.generic.util.dblog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.object.log.TransactionLogItems;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;

import xutil4j.collection.Queue;

public class TransactionLogWriter implements Runnable {
	private Queue queue = new Queue();
	private boolean run = true;
	private Log log = LogFactory.getLog(TransactionLogWriter.class);
	private String serverSequencedName = "";
	
	private String queryString = "INSERT INTO CT_TRANSACTIONLOG "
			+ "		(SERVERNAME, EVENTNAME, EVENTUSER, TIMEKEY, TRANSACTIONID, IP, ELAPSEDTIME, RESULT) "
			+ "VALUES (:serverName, :eventName, :eventUser, :timeKey, :transactionId, :ip, :elapsedTime, :result)";
	
	public TransactionLogWriter(String serverName, String processSequence)
	{
		this.serverSequencedName = String.format("%s%s", serverName, processSequence);
	}
	
	public void run() {
		List<TransactionLogItems> dequeuedList = new ArrayList<TransactionLogItems>();
		while(run)
		{
			synchronized (getQueue()) {
				Object obj = getQueue().dequeue();
				if(obj == null)
					continue;
				else
				{
					dequeuedList.add((TransactionLogItems)obj);
					if(getQueue().size() > 0)
					{
						for(int i=0;i<getQueue().size();i++)
						{
							dequeuedList.add((TransactionLogItems) getQueue().dequeue());
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
	
	private void writeToDB(List<TransactionLogItems> transactionLogs)
	{
		List<Map<String, String>> bindMapList = new ArrayList<Map<String,String>>() ;
		for(TransactionLogItems transactionLog : transactionLogs)
		{
			if(transactionLog == null)
				continue;
			Map<String, String> bindMap = new HashMap<String, String>();
			try {
				Element root = transactionLog.getXml().getDocument().getRootElement();
				// Set Variable
				//String serverName = getServerName();
				String eventName = root.getChild("Header").getChildText("MESSAGENAME");
				String eventUser = root.getChild("Header").getChildText("EVENTUSER");
				String timeKey = TimeStampUtil.getCurrentEventTimeKey();
				String transactionId = root.getChild("Header").getChildText("TRANSACTIONID");
				String elapsedTime = Long.toString(transactionLog.getElapsedTime());
				String resultValue = GenericServiceProxy.getConstantMap().FLAG_Y;
				if(transactionLog.isResult())
					resultValue = GenericServiceProxy.getConstantMap().FLAG_Y;
				else
					resultValue = GenericServiceProxy.getConstantMap().FLAG_N;
				
				
				bindMap.put("serverName", serverSequencedName);
				bindMap.put("eventName", eventName);
				bindMap.put("eventUser", eventUser);
				bindMap.put("timeKey", timeKey);
				bindMap.put("transactionId", transactionId);
				bindMap.put("ip", CommonUtil.getIp());
				bindMap.put("elapsedTime", elapsedTime);
				bindMap.put("result", resultValue);			
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