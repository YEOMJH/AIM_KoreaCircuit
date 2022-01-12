package kr.co.aim.messolution.generic.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.RowSetInternal;
import javax.sql.rowset.WebRowSet;
import javax.sql.rowset.spi.XmlReader;
import javax.xml.stream.XMLResolver;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.orm.GenSqlLobValue;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greenframe.util.bundle.BundleUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greenframe.util.xml.JdomUtils;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.jdbc.object.SqlUpdate;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.jdbc.support.lob.OracleLobHandler;

public class MessageHistoryUtil {
	private static Log log = LogFactory.getLog(MessageHistoryUtil.class);

	private LobHandler lobHandler = greenFrameServiceProxy.getLobHandler();
	
	final String MHU_TASK_RECEIVER = "RECV";
	final String MHU_TASK_WATCHER = "WTCH";
	final String MHU_TASK_ALL = "BOTH";

	public void setMessageHistory(String serverName, Document xml) {
		String messageName = "";
		String lotName = "";
		String carrierName = "";
		String machineName = "";
		String portName = "";
		String productSpecName = "";
		String processOperationName = "";
		String eventUser = "";

		// watch out upon heap
		String fullMessage = JdomUtils.toString(xml);

		byte[] byteStr = fullMessage.getBytes();
		if (byteStr.length > 4000) {
			fullMessage = new String(byteStr, 0, 3900);
		}

		try {
			Element root = xml.getDocument().getRootElement();

			Element elementHeader = root.getChild(SMessageUtil.Header_Tag);
			Element elementBody = root.getChild(SMessageUtil.Body_Tag);
			if (elementHeader != null) {
				if (elementHeader.getChild(SMessageUtil.MessageName_Tag) != null) {
					messageName = elementHeader.getChild(
							SMessageUtil.MessageName_Tag).getText();
				}

				if (elementBody != null) {
					if (elementBody.getChild("LOTNAME") != null) {
						lotName = elementBody.getChild("LOTNAME").getText();
					}
					if (elementBody.getChild("CARRIERNAME") != null) {
						carrierName = elementBody.getChild("CARRIERNAME")
								.getText();
					}
					if (elementBody.getChild("MACHINENAME") != null) {
						machineName = elementBody.getChild("MACHINENAME")
								.getText();
					}
					if (elementBody.getChild("PORTNAME") != null) {
						portName = elementBody.getChild("PORTNAME").getText();
					}
					if (elementBody.getChild("PRODUCTSPECNAME") != null) {
						productSpecName = elementBody.getChild(
								"PRODUCTSPECNAME").getText();
					}
					if (elementBody.getChild("PROCESSOPERATIONNAME") != null) {
						processOperationName = elementBody.getChild(
								"PROCESSOPERATIONNAME").getText();
					}
					if (elementBody.getChild("EVENTUSER") != null) {
						eventUser = elementBody.getChild("EVENTUSER").getText();
					}
				}
			} else {
				return;
			}

			try {

				String sql = "SELECT RECEIVEFLAG, EXPLICITCOMMIT FROM CT_MESSAGEDEF WHERE SERVERNAME = ? AND MESSAGENAME = ?";
				String[] bindSet = new String[] { serverName, messageName };

				String[][] sqlResult = GenericServiceProxy.getSqlMesTemplate()
						.queryForStringArray(sql, bindSet);

				if (sqlResult.length == 0 || sqlResult[0][0].equals("Y")) {
					String expliciCommit = "";
					if (sqlResult.length > 0) {
						expliciCommit = sqlResult[0][1].toString();
					}

					sql = "INSERT INTO CT_MESSAGEHISTORY (SERVERNAME, MESSAGENAME, TIMEKEY, LOTNAME, CARRIERNAME, MACHINENAME, PORTNAME, PRODUCTSPECNAME, PROCESSOPERATIONNAME, EVENTUSER, FULLMESSAGE) "
							+ "VALUES ( ?, ?, TO_CHAR(SYSTIMESTAMP, 'YYYYMMDDHH24MISSFF6'), ?, ?, ?, ?, ?, ?, ?, ?)";

					JdbcTemplate jdbcTemplate = greenFrameServiceProxy
							.getSqlTemplate().getJdbcTemplate();
					SqlUpdate sqlUpdate = new SqlUpdate(
							jdbcTemplate.getDataSource(), sql);
					sqlUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
					sqlUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
					sqlUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
					sqlUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
					sqlUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
					sqlUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
					sqlUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
					sqlUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
					sqlUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
					sqlUpdate.declareParameter(new SqlParameter(Types.CLOB));
					Object objFullMessage = new SqlLobValue(new StringReader(
							fullMessage), fullMessage.length(), lobHandler);
					sqlUpdate.compile();

					Object[] objects = new Object[] { serverName, messageName,
							lotName, carrierName, machineName, portName,
							productSpecName, processOperationName, eventUser,
							objFullMessage };

					sqlUpdate.update(objects);
					if (!expliciCommit.equals("N")) {
						jdbcTemplate.update("commit");
					}
				}
			} catch (Exception e) {
				log.error("Error MessageHistory : " + e.getMessage());

			}
		} catch (Exception e) {

		}
	}
	
	public void recordEventResult(Document xml, long elapse, Exception error) {
		// whenever called, generate orphan as inner resource
		if (getWorkFlag().equals(MHU_TASK_WATCHER) || getWorkFlag().equals(MHU_TASK_ALL))
			new Watcher(xml, elapse, error).start();
	}
	
	public void recordMessageLog(Document xml) {
		// whenever called, generate orphan as inner resource
		if (getWorkFlag().equals(MHU_TASK_RECEIVER) || getWorkFlag().equals(MHU_TASK_ALL))
			new Receiver(xml).start();
	}
	
	public void recordErrorMessageLog(Document xml, Exception error, String emptyFlag) {
			//new ErrorMessage(xml, error, emptyFlag).start();
			GenericServiceProxy.getDBLogWriter().writErrorMessageLog(xml, error, emptyFlag);
	}
	
	public void recordMessageLog(Document xml, String messageType) {
		// whenever called, generate orphan as inner resource
		// if watchLever contains "MSG", insert CT_MESSAGELOG
		if (!getInsertMessageFlagList().contains(GenericServiceProxy.getConstantMap().INSERT_LOG_NONE)
				&& getInsertMessageFlagList().contains(GenericServiceProxy.getConstantMap().INSERT_LOG_MESSAGE))
		{
			//new MessageLog(xml, messageType).start();
			GenericServiceProxy.getDBLogWriter().writeMessageLog(xml, messageType);
		}
	}
	
	public void recordTranscationLog(Document xml, long elapse, boolean result) {
		// whenever called, generate orphan as inner resource
		// if watchLever contains "TRX", insert CT_TRANSACTIONLOG
		if (!getInsertMessageFlagList().contains(GenericServiceProxy.getConstantMap().INSERT_LOG_NONE)
				&& getInsertMessageFlagList().contains(GenericServiceProxy.getConstantMap().INSERT_LOG_TRANSATION))
		{
			//new TransactionLog(xml, elapse, result).start();
			GenericServiceProxy.getDBLogWriter().writeTransaction(xml, elapse, result);
		}
	}


	class Receiver extends Thread {
		private Document xml;

		public Receiver(Document xml) {
			super();

			this.xml = xml;
		}

		public void run() {
			GenericServiceProxy.getTxDataSourceManager().beginTransaction(
					PropagationBehavior.PROPAGATION_REQUIRES_NEW);

			try
			{
				// watch out upon heap
				String fullMessage = JdomUtils.toString(xml);
				{
					//should to be changed to LOB
					byte[] byteStr = fullMessage.getBytes();
					if (byteStr.length > 4000)
						fullMessage = new String(byteStr, 0, 3900);
				}
				
				String serverName = getServerName();
				String messageName = SMessageUtil.getHeaderItemValue(xml, "MESSAGENAME", false);
				String eventUser = SMessageUtil.getHeaderItemValue(xml, "EVENTUSER", false);
				String ip = CommonUtil.getIp();
				
				//use existing SQL template
				
				String sql = "SELECT serverName, eventName FROM CT_MESSAGELIST WHERE SERVERNAME = ? AND eventName = ?";
				String[] bindSet = new String[] { serverName, messageName };
				
				//as primitive
				List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().getJdbcTemplate().queryForList(sql, bindSet);

				if (sqlResult.size() > 0)
				{
					//system time at time
					String timeKey = TimeStampUtil.getCurrentEventTimeKey();
					
					sql = new StringBuffer("INSERT INTO CT_MESSAGELOG (SERVERNAME, EVENTNAME, TIMEKEY, EVENTUSER, MESSAGELOG, IP)")
								.append(" ")
								.append("VALUES (?, ?, ?, ?, ?, ?)").toString();
					
					Object[] objects = new Object[] { new StringBuilder(serverName).append(getProcessSequence()).toString(),
														messageName, timeKey, eventUser, fullMessage, ip};
					
					GenericServiceProxy.getSqlMesTemplate().update(sql, objects); 
				}

				GenericServiceProxy.getTxDataSourceManager()
						.commitTransaction();
			}
			catch (Exception ex)
			{
				GenericServiceProxy.getTxDataSourceManager()
						.rollbackTransaction();

				log.error(ex);
			}
		}
	}

	class Watcher extends Thread {
		private Document xml;
		private long elapse;
		private Exception error;

		public Watcher(Document xml, long elapse, Exception error) {
			super();

			this.xml = xml;
			this.elapse = elapse;
			this.error = error;
		}

		public void run() {
			GenericServiceProxy.getTxDataSourceManager().beginTransaction(
					PropagationBehavior.PROPAGATION_REQUIRES_NEW);

			try
			{
				String serverName = getServerName();
				String messageName = SMessageUtil.getHeaderItemValue(xml, "MESSAGENAME", false);
				String eventUser = SMessageUtil.getHeaderItemValue(xml, "EVENTUSER", false);

				String errorMessage;
				
				//uncontrollable if not standardized form
				if (error != null)
				{
					if (error instanceof CustomException)
						errorMessage = ((CustomException) error).errorDef.getLoc_errorMessage();
					else
						errorMessage = error.getCause().getMessage();
				}
				else//successful case
				{
					errorMessage = "";
				}
				
				String sql = new StringBuffer("INSERT INTO CT_ERRORMESSAGELOG (TIMEKEY, EVENTNAME, EVENTUSER, IP, FACTORYNAME, ERRORMESSAGE, MESSAGE, TARGETSUBJECTNAME)")
									.append(" ")
									.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?)")
									.toString();
				
				JdbcTemplate jdbcTemplate = GenericServiceProxy.getSqlMesTemplate().getJdbcTemplate();
				
				SqlUpdate sqlUpdate = new SqlUpdate(jdbcTemplate.getDataSource(), sql);
				{
					sqlUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
					sqlUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
					sqlUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
					sqlUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
					sqlUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
					sqlUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
					sqlUpdate.declareParameter(new SqlParameter(Types.CLOB));
					sqlUpdate.declareParameter(new SqlParameter(Types.VARCHAR));
				}
				
				sqlUpdate.compile();
				
				//system time at time
				String timeKey = TimeStampUtil.getCurrentEventTimeKey();
				
				Object[] objects = new Object[] {timeKey, messageName, eventUser, CommonUtil.getIp(),
													String.valueOf(elapse), errorMessage,
													new GenSqlLobValue(JdomUtils.toString(xml), lobHandler),
													new StringBuilder(serverName).append(getProcessSequence()).toString()};

				sqlUpdate.update(objects);

				GenericServiceProxy.getTxDataSourceManager()
						.commitTransaction();
			}
			catch (Exception ex)
			{
				GenericServiceProxy.getTxDataSourceManager()
						.rollbackTransaction();

				log.error(ex);
			}
		}
	}
	
	
	private String getServerName()
	{
		String serverName = "";
		
		try
		{
			if (System.getProperty("svr") != null)
				serverName = new StringBuilder().append(System.getProperty("svr")).toString();
		}
		catch (Exception ex)
		{
			log.warn("NO subject for process");
		}
		
		return serverName;
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
	
	private String getWorkFlag()
	{
		try
		{
			if (System.getProperty("watchLevel") != null)
				return System.getProperty("watchLevel");
		}
		catch (Exception ex)
		{
			
		}
		
		return "";
	}
	
	private List<String> getInsertMessageFlagList()
	{
		List<String> list = new ArrayList<String>();
		
		try
		{
			if (System.getProperty("watchLevel") != null)
			{
				String[] watchLevelArg = System.getProperty("watchLevel").split("\\/");
				
				for(String i : watchLevelArg)
				{
					list.add(i);
				}
			}
		}
		catch (Exception ex)
		{
			
		}
		
		return list;
	}
}