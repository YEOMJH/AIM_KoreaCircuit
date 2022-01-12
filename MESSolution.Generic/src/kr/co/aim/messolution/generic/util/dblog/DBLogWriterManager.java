package kr.co.aim.messolution.generic.util.dblog;

import kr.co.aim.messolution.generic.object.log.ErrorMessageLogItems;
import kr.co.aim.messolution.generic.object.log.MessageLogItems;
import kr.co.aim.messolution.generic.object.log.TransactionLogItems;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;


public class DBLogWriterManager  {

	private String serverName;
	private String processSequence;
	private MessageLogWriter messageLogWriter;
	private ErrorMessageLogWriter errorMessageLogWriter;
	private TransactionLogWriter transactionLogWriter;
	private Thread messageLogThread;
	private Thread errorMessageLogThread;
	private Thread transactionLogThread;
	private static Log log = LogFactory.getLog(DBLogWriterManager.class);
	;
	public void init()
	{
		this.serverName = getServerName();
		this.processSequence = getProcessSequence();
		
		messageLogWriter = new MessageLogWriter(serverName, processSequence);
		messageLogThread = new Thread(messageLogWriter);
		messageLogThread.setDaemon(true);
		messageLogThread.setName("MESSAGELOGGING");
		messageLogThread.start();
		
		errorMessageLogWriter = new ErrorMessageLogWriter(serverName, processSequence);
		errorMessageLogThread = new Thread(errorMessageLogWriter);
		errorMessageLogThread.setDaemon(true);
		errorMessageLogThread.setName("ERRORMESSAGELOGGING");
		errorMessageLogThread.start();
		
		transactionLogWriter = new TransactionLogWriter(serverName, processSequence);
		transactionLogThread = new Thread(transactionLogWriter);
		transactionLogThread.setDaemon(true);
		transactionLogThread.setName("TRANSACTIONLOGGING");
		transactionLogThread.start();
		
	}
	
	public void restart()
	{
		
	}
	
	public void writeMessageLog(Document xml, String messageType)
	{
		messageLogWriter.getQueue().enqueue(new MessageLogItems(xml,messageType));
	}
	
	public void writErrorMessageLog(Document xml, Exception error, String emptyFlag)
	{
		errorMessageLogWriter.getQueue().enqueue(new ErrorMessageLogItems(xml, error, emptyFlag));
	}
	
	public void writeTransaction(Document xml, long elaspedTime, boolean result)
	{
		transactionLogWriter.getQueue().enqueue(new TransactionLogItems(xml, elaspedTime, result));
	}
	
	
	private String getServerName()
	{
		String serverNameTemp = "";
		try
		{
			if (System.getProperty("svr") != null)
				serverNameTemp = new StringBuilder().append(System.getProperty("svr")).toString();
		}
		catch (Exception ex)
		{
			log.warn("NO subject for process");
		}
		
		return serverNameTemp;
	}
	
	private String getProcessSequence()
	{
		try
		{
			if (System.getProperty("Seq") != null)
				return System.getProperty("Seq");
		}
		catch (Exception ex)
		{
			log.warn("NO subject for process");
		}
		
		return "";
	}
}
