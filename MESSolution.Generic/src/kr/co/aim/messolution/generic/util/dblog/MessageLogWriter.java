package kr.co.aim.messolution.generic.util.dblog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.object.log.MessageLogItems;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;

import xutil4j.collection.Queue;

public class MessageLogWriter implements Runnable {
	private Queue queue = new Queue();
	private boolean run = true;
	private Log log = LogFactory.getLog(MessageLogWriter.class);
	private String serverSequencedName = "";
	
	private String queryString = "INSERT INTO CT_MESSAGELOG "
			+ "		(SERVERNAME, EVENTNAME, EVENTUSER, TIMEKEY, TRANSACTIONID, IP, ORIGINALSOURCESUBJECTNAME, TARGETSUBJECTNAME, MESSAGELOG, MESSAGETYPE, LOTNAME, CARRIERNAME, PRODUCTNAME, MACHINENAME, UNITNAME, SUBUNITNAME)  "
			+ "VALUES (:serverName, :eventName, :eventUser, :timeKey, :transactionId, :ip, :originalSourceSubjectName, :targetSubjectName, :messageLog, :messageType, :lotName, :carrierName, :productName, :machineName, :unitName, :subUnitName)";
	
	public MessageLogWriter(String serverName, String processSequence)
	{
		this.serverSequencedName = String.format("%s%s", serverName, processSequence);
	}
	
	public void run() {
		List<MessageLogItems> dequeuedList = new ArrayList<MessageLogItems>();
		while(run)
		{
			synchronized (getQueue()) {
				Object obj = getQueue().dequeue();
				if(obj == null)
					continue;
				else
				{
					dequeuedList.add((MessageLogItems)obj);
					if(getQueue().size() > 0)
					{
						for(int i=0;i<getQueue().size();i++)
						{
							dequeuedList.add((MessageLogItems) getQueue().dequeue());
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
	
	private void writeToDB(List<MessageLogItems> messageLogs)
	{
		List<Map<String, String>> bindMapList = new ArrayList<Map<String,String>>() ;
		for(MessageLogItems messageLog : messageLogs)
		{
			if(messageLog == null)
				continue;
			Map<String, String> bindMap = new HashMap<String, String>();
			try {
				Element root = messageLog.getXml().getDocument().getRootElement();
				// Set Variable
				//String serverName = getServerName();
				String eventName = root.getChild("Header").getChildText("MESSAGENAME");
				String eventUser = root.getChild("Header").getChildText("EVENTUSER");
				String timeKey = TimeStampUtil.getCurrentEventTimeKey();
				String transactionId = root.getChild("Header").getChildText("TRANSACTIONID");
				String originalSourceSubjectName = SMessageUtil.getHeaderItemValue(messageLog.getXml(), "ORIGINALSOURCESUBJECTNAME", false);
				String targetSubjectName = SMessageUtil.getHeaderItemValue(messageLog.getXml(), "TARGETSUBJECTNAME", false);
				String fullMessage = JdomUtils.toString(messageLog.getXml());
	
				// Get DEFAULTFLAG
				
				bindMap.put("serverName", serverSequencedName);
				bindMap.put("eventName", eventName);
				bindMap.put("eventUser", eventUser);
				bindMap.put("timeKey", timeKey);
				bindMap.put("transactionId", transactionId);
				bindMap.put("ip", CommonUtil.getIp());
				bindMap.put("originalSourceSubjectName", originalSourceSubjectName);
				bindMap.put("targetSubjectName", targetSubjectName);
				bindMap.put("messageLog", fullMessage);
				bindMap.put("messageType", messageLog.getMessageType());
				
				//optional trace
				String lotName = "";
				try
				{
					lotName = root.getChild("Body").getChildText("LOTNAME");
				}
				catch (Exception ex)
				{
					//ignore
					lotName = "";
				}
				finally
				{
					bindMap.put("lotName", lotName);
				}
				
				String carrierName = "";
				try
				{
					carrierName = root.getChild("Body").getChildText("CARRIERNAME");
					
					if (carrierName == null)
					{
						carrierName = root.getChild("Body").getChildText("DURABLENAME");
					}
				}
				catch (Exception ex)
				{
					try
					{
						carrierName = root.getChild("Body").getChildText("DURABLENAME");
					}
					catch (Exception sex)
					{
						//ignore
						carrierName = "";
					}
				}
				finally
				{
					bindMap.put("carrierName", carrierName);
				}
				
				String productName = "";
				try
				{
					productName = root.getChild("Body").getChildText("PRODUCTNAME");
					
					if (productName == null)
					{
						productName = root.getChild("Body").getChildText("PRODUCTNAME");
					}
				}
				catch (Exception ex)
				{
					try
					{
						productName = root.getChild("Body").getChildText("PRODUCTNAME");
					}
					catch (Exception sex)
					{
						//ignore
						productName = "";
					}
				}
				finally
				{
					bindMap.put("productName", productName);
				}
				
				
				String machineName = "";
				try
				{
					machineName = root.getChild("Body").getChildText("MACHINENAME");
					
					if (machineName == null)
					{
						machineName = root.getChild("Body").getChildText("MACHINENAME");
					}
				}
				catch (Exception ex)
				{
					try
					{
						machineName = root.getChild("Body").getChildText("MACHINENAME");
					}
					catch (Exception sex)
					{
						//ignore
						machineName = "";
					}
				}
				finally
				{
					bindMap.put("machineName", machineName);
				}
				
				String unitName = "";
				try
				{
					unitName = root.getChild("Body").getChildText("UNITNAME");
					
					if (unitName == null)
					{
						unitName = root.getChild("Body").getChildText("UNITNAME");
					}
				}
				catch (Exception ex)
				{
					try
					{
						unitName = root.getChild("Body").getChildText("UNITNAME");
					}
					catch (Exception sex)
					{
						//ignore
						unitName = "";
					}
				}
				finally
				{
					bindMap.put("unitName", unitName);
				}
				
				String subUnitName = "";
				try
				{
					subUnitName = root.getChild("Body").getChildText("SUBUNITNAME");
					
					if (subUnitName == null)
					{
						subUnitName = root.getChild("Body").getChildText("SUBUNITNAME");
					}
				}
				catch (Exception ex)
				{
					try
					{
						subUnitName = root.getChild("Body").getChildText("SUBUNITNAME");
					}
					catch (Exception sex)
					{
						//ignore
						subUnitName = "";
					}
				}
				finally
				{
					bindMap.put("subUnitName", subUnitName);
				}
				
				
				
				bindMapList.add(bindMap);
				
				log.debug(String.format("TRX[%s] Message[%s]", transactionId, fullMessage));
				
			} catch (Exception e) {
				log.error(e);
			}
		}
		
		if(bindMapList.size() > 0)
		{
			try
			{
				if(bindMapList.size() == 1)
				{
					GenericServiceProxy.getSqlMesTemplate().update(queryString, bindMapList.get(0));
					
					log.debug(String.format("TRX[%s] inserted", bindMapList.get(0).get("transactionId")));
				}
				else
				{
					Map[] bindArray = bindMapList.toArray(new Map[bindMapList.size()]);					
					GenericServiceProxy.getSqlMesTemplate().updateBatch(queryString, bindArray);
					
					for (Map bindMap : bindMapList)
					{
						log.debug(String.format("TRX[%s] inserted", bindMap.get("transactionId")));
					}
				}
			}
			catch (Exception e)
			{
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