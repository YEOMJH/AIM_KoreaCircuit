package kr.co.aim.messolution.generic.util;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.Address;
import javax.mail.AuthenticationFailedException;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import com.sun.mail.handlers.multipart_mixed;
import com.sun.xml.internal.ws.util.StringUtils;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.EnumDefValue;
import kr.co.aim.messolution.extended.object.management.impl.EnumDefService;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.MailAttachmentGenerator.MailContentType;
import kr.co.aim.greenframe.util.sys.SystemPropHelper;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.GenericServiceProxy;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

class MailAuthenticator implements Serializable
{

	public  final String sql = "SELECT EDV.ENUMVALUE AS MAPKEY, CD.constantValue AS MAPVALUE " + SystemPropHelper.CR +
		                         " FROM ENUMDEF ED, ENUMDEFVALUE EDV, CONSTANTDEF CD " + SystemPropHelper.CR +
		                         " WHERE ED.ENUMNAME = EDV.ENUMNAME AND ED.ENUMNAME = 'MailServerInfo' AND EDV.ENUMVALUE = CD.CONSTANTNAME(+) ORDER BY ENUMVALUE" ;
	
	private String  SocketIOTimeout     ;
	private String  ConnectionTimeout   ;
	private String  Port                ;
	private String  Password            ;
	private String  HostName            ;
	private String  Account             ;
	private Map<String, Object> EnumMap;
	
	public String getSocketIOTimeout() {
		return SocketIOTimeout;
	}

	public void setSocketIOTimeout(String socketIOTimeout) {
		SocketIOTimeout = socketIOTimeout;
	}

	public String getConnectionTimeout() {
		return ConnectionTimeout;
	}

	public void setConnectionTimeout(String connectionTimeout) {
		ConnectionTimeout = connectionTimeout;
	}

	public String getPort() {
		return Port;
	}

	public void setPort(String port) {
		Port = port;
	}

	public String getPassword() {
		return Password;
	}

	public void setPassword(String password) {
		Password = password;
	}

	public String getHostName() {
		return HostName;
	}

	public void setHostName(String hostName) {
		HostName = hostName;
	}

	public String getAccount() {
		return Account;
	}

	public void setAccount(String account) {
		Account = account;
	}

	public Map<String, Object> getEnumMap() {
		return EnumMap;
	}

	public void setMailAuthenMap(Map<String, Object> enumMap) {
		EnumMap = enumMap;
	}

	@SuppressWarnings("unchecked")
	public MailAuthenticator() throws IllegalArgumentException, IllegalAccessException
	{

		List<Map<String, Object>> resultList = null;
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new Object[] {});
			if (resultList == null || resultList.size() == 0)
			{
				return;
			}
			
			Map<String, Object> enumMap = new HashMap<String, Object>();

			for (int i = 0; i < resultList.size(); i++)
			{
				ListOrderedMap orderMap = (ListOrderedMap) resultList.get(i);
				enumMap.put((String) orderMap.get("MAPKEY"), orderMap.get("MAPVALUE"));
			}

			Field[] fields = MailAuthenticator.class.getDeclaredFields();

			for (Field field : fields)
			{
				Class<?> type = field.getType();
				field.setAccessible(true);

				if("this$0".equals(field.getName())) continue;
				
				if (type.equals(Map.class) || (type.equals(HashMap.class)||"EnumMap".equals(field.getName())))
				{
					this.setMailAuthenMap(enumMap);
					continue;
				}

				field.set(this, ConvertUtil.getMapValueByName(enumMap, field.getName()));
			}
		}
		catch (FrameworkErrorSignal e)
		{
			e.printStackTrace();
		}

	}
}

public class EMailInterface implements InitializingBean
{
	private  static Log log = LogFactory.getLog(EMailInterface.class);
	
	private static MailAuthenticator mailAuthen = null;
	
	private String	smtpHostName		= "localhost";
	private String	smtpPort			= "25";
	private String	connectionTimeout	= "1000";
	private String	socketIOTimeout		= "1000";

	public String getSmtpHostName() 
	{
		return smtpHostName;
	}

	public void setSmtpHostName(String smtpHostName) 
	{
		this.smtpHostName = smtpHostName;
	}

	public String getSmtpPort() 
	{
		return smtpPort;
	}

	public void setSmtpPort(String smtpPort) 
	{
		this.smtpPort = smtpPort;
	}

	public String getConnectionTimeout() 
	{
		return connectionTimeout;
	}

	public void setConnectionTimeout(String connectionTimeout) 
	{
		this.connectionTimeout = connectionTimeout;
	}

	public String getSocketIOTimeout() 
	{
		return socketIOTimeout;
	}

	public void setSocketIOTimeout(String socketIOTimeout) 
	{
		this.socketIOTimeout = socketIOTimeout;
	}

	public EMailInterface()
	{
		MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
		mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
		mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
		CommandMap.setDefaultCommandMap(mc);
	}

	public EMailInterface(String smtpHostName, String smtpPort, String connectionTimeout, String socketIOTimeout)
	{
		StringBuffer sql = new StringBuffer();
		sql.append(
				"SELECT * FROM ENUMDEFVALUE L WHERE L.ENUMNAME = :ENUMNAME");
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("ENUMNAME", "V3MESMail_IF");
		
		List<Map<String, Object>> sqlResult = null;
		try 
		{
			sqlResult = GenericServiceProxy.getSqlMesTemplate()
					.queryForList(sql.toString(), args);
		} 
		catch(Exception ex)
		{
			sqlResult = null;
			log.info(ex.getCause());
		}
		
		if (sqlResult != null && sqlResult.size() > 0)
		{
			smtpHostName = ConvertUtil.getMapValueByName(sqlResult.get(0), "ENUMVALUE");
			smtpPort = ConvertUtil.getMapValueByName(sqlResult.get(0), "DESCRIPTION");
			connectionTimeout = ConvertUtil.getMapValueByName(sqlResult.get(0), "DEFAULTFLAG");
			socketIOTimeout = ConvertUtil.getMapValueByName(sqlResult.get(0), "DISPLAYCOLOR");
		}
		
		MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
		mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
		mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
		CommandMap.setDefaultCommandMap(mc);

		this.setSmtpHostName(smtpHostName);
		this.setSmtpPort(smtpPort);
		this.setConnectionTimeout(connectionTimeout);
		this.setSocketIOTimeout(socketIOTimeout);
	}
	public MailAuthenticator getMailAuthenticator() throws IllegalArgumentException, IllegalAccessException
	{
		if (mailAuthen == null)
			mailAuthen = new MailAuthenticator();

		return mailAuthen;
	}
	
	private void load() throws IllegalArgumentException, IllegalAccessException
	{
		this.getMailAuthenticator();
	}
	private void preCheck(String[] recipients ) throws CustomException
	{
		List<String> toUserMailList = new ArrayList<String>();

		if (recipients == null || recipients.length <= 0)
		{
			throw new CustomException("MAIL-0004");
		}
		else
		{
			for (String toUserMail : recipients)
			{
				if (toUserMail.isEmpty())
				{
					log.info("to usermail is null");
				}
				else
				{
					toUserMailList.add(toUserMail);
				}
			}

			if (toUserMailList == null || toUserMailList.size() <= 0)
			{
				throw new CustomException("MAIL-0004");
			}
		}
		if (mailAuthen == null || mailAuthen.getAccount().isEmpty())
		{
			throw new CustomException("MAIL-0005");
		}
		if (mailAuthen.getAccount() == null || mailAuthen.getAccount().isEmpty())
		{
			throw new CustomException("MAIL-0006");
		}
		if (mailAuthen.getPassword() == null || mailAuthen.getPassword().isEmpty())
		{
			throw new CustomException("MAIL-0007");
		}
		
	}
	 public boolean postMail(String[] recipients, String subject, String message) throws CustomException, Exception
	 {
		    this.preCheck(recipients);
		    
		    Exception ex = null;
			boolean error = false;

			Properties props = new Properties();
			props.put("mail.transport.protocol", "smtp");
			props.put("mail.smtp.host", mailAuthen.getHostName());
			props.put("mail.smtp.port", mailAuthen.getPort());
			props.put("mail.smtp.auth", "true");

			if (mailAuthen.getConnectionTimeout().isEmpty() == false)
			{
				props.put("mail.smtp.connectiontimeout", mailAuthen.getConnectionTimeout());
			}
			if (mailAuthen.getSocketIOTimeout().isEmpty() == false)
			{
				props.put("mail.smtp.timeout", this.mailAuthen.getSocketIOTimeout());
			}
			
			Session session = Session.getInstance(props);
		
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(mailAuthen.getAccount()));
			log.info("From address name :" + msg.getFrom()[0].toString());

			InternetAddress[] addressTo = new InternetAddress[recipients.length];
			for (int i = 0; i < recipients.length; i++)
			{
				addressTo[i] = new InternetAddress(recipients[i]);
				log.info("addressTo :  " + addressTo[i]);
			}

			msg.setRecipients(RecipientType.TO, addressTo);
			msg.setSubject(subject);
			log.info("subject: " + subject);

			msg.setText(message);
			msg.setContent(message, "text/html;charset=utf-8"); 
			msg.setSentDate(new Date());
			msg.saveChanges();
			log.info("message: " + message);

			Transport trans = session.getTransport();

			try
			{
				trans.connect(mailAuthen.getAccount(), mailAuthen.getPassword());
				trans.sendMessage(msg, msg.getAllRecipients());
				log.info("[Mail Send OK]");
			}
			catch (MessagingException mex)
			{
				error = true;
				ex = mex;
				log.info(mex.toString());
				if (ex instanceof IllegalStateException)
				{
					log.info(String.format("already connected!![UserName:%s , Password:%s]", mailAuthen.getAccount(),org.apache.commons.lang.StringUtils.repeat("*", mailAuthen.getPassword().length())));
				}
				else if (ex instanceof AuthenticationFailedException)
				{
					log.info(String.format("Authentication Failed Exception!![AuthUser:%s ,AuthPwd:%s]",mailAuthen.getAccount(),org.apache.commons.lang.StringUtils.repeat("*", mailAuthen.getPassword().length())));
				}
				else if (ex instanceof SendFailedException)
				{
					SendFailedException sfex = (SendFailedException) ex;
					Address[] invalid = sfex.getInvalidAddresses();
					StringBuilder strBuilder = new StringBuilder();

					if (invalid != null)
					{
						log.info("   ** Invalid Addresses [S] **   ");
						if (invalid != null)
						{
							for (int i = 0; i < invalid.length; i++)
							{
								log.info("         " + invalid[i]);
								strBuilder.append(invalid[i] + ",");
							}
						}
						log.info("    ** Invalid Addresses [E] **   ");
					}

					Address[] validUnsent = sfex.getValidUnsentAddresses();
					if (validUnsent != null)
					{
						log.info("   ** ValidUnsent Addresses [S] **   ");
						if (validUnsent != null)
						{
							for (int i = 0; i < validUnsent.length; i++)
							{
								log.info("         " + validUnsent[i]);
								strBuilder.append(validUnsent[i] + ",");
							}
						}
						log.info("   ** ValidUnsent Addresses [E] **   ");
					}

					Address[] validSent = sfex.getValidSentAddresses();
					if (validSent != null)
					{
						log.info("   ** ValidSent Addresses [S] **   ");
						if (validSent != null)
						{
							for (int i = 0; i < validSent.length; i++)
								log.info("         " + validSent[i]);
						}
						log.info("   ** ValidSent Addresses [E] **   ");
					}

				}

			}
			finally
			{
				trans.close();
				log.info("Close transport connection!!");
			}
			
			if (error) throw new CustomException("MAIL-0002",ex.getCause());        
			return true;
	}
	 
	 public boolean sendAttachmentMail (String[] recipients, String subject, String message,DataSource dataSource,String fileName) throws CustomException
	 {
		if (!(dataSource instanceof ByteArrayDataSource))throw new CustomException("MAIL-0008");
		((ByteArrayDataSource) dataSource).setName(fileName);
        boolean SendOK = true;
        
		try {
			SendOK = this.postAttachmentMail(recipients, subject, message, dataSource);
		} catch (CustomException customException) {
			throw customException;
		} catch (Exception ex) {
			throw new CustomException(ex.getCause());
		}
		return SendOK;
	 }
	 public boolean postAttachmentMail(String[] recipients, String subject, String message,DataSource dataSource) throws CustomException, AddressException, MessagingException, UnsupportedEncodingException
	 {
		    if(!(dataSource instanceof ByteArrayDataSource)) throw new CustomException("MAIL-0008");
		    	
		    this.preCheck(recipients);
		    Thread.currentThread().setContextClassLoader(javax.mail.Message.class.getClassLoader());
		    
		    Exception ex = null;
			boolean error = false;
			Properties props = new Properties();
			props.put("mail.transport.protocol", "smtp");
			props.put("mail.smtp.host", mailAuthen.getHostName());
			props.put("mail.smtp.port", mailAuthen.getPort());
			props.put("mail.smtp.auth", "true");

			if (mailAuthen.getConnectionTimeout().isEmpty() == false)
			{
				props.put("mail.smtp.connectiontimeout", mailAuthen.getConnectionTimeout());
			}
			if (mailAuthen.getSocketIOTimeout().isEmpty() == false)
			{
				props.put("mail.smtp.timeout", this.mailAuthen.getSocketIOTimeout());
			}
			System.setProperty("mail.mime.splitlongparameters","false");
			
			Session session = Session.getInstance(props);
		
			MimeMessage msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(mailAuthen.getAccount()));
			log.info("From address name :" + msg.getFrom()[0].toString());

			InternetAddress[] addressTo = new InternetAddress[recipients.length];
			for (int i = 0; i < recipients.length; i++)
			{
				addressTo[i] = new InternetAddress(recipients[i]);
				log.info("addressTo :  " + addressTo[i]);
			}

			msg.setRecipients(RecipientType.TO, addressTo);
			msg.setSubject(subject);
			log.info("subject: " + subject);
			msg.setSentDate(new Date());
			MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(message);
            
            MimeBodyPart attachmentBodyPart = new MimeBodyPart();
            attachmentBodyPart.setDataHandler(new DataHandler(dataSource));
            attachmentBodyPart.setFileName(MimeUtility.encodeText(StringUtil.isEmpty(dataSource.getName())?TimeStampUtil.getCurrentEventTimeKey() + MailContentType.getExtensionByContentType(dataSource.getContentType())
            																							  : dataSource.getName()));
            
            MimeMultipart multiPart = new MimeMultipart();
            
            multiPart.addBodyPart(messageBodyPart);
            multiPart.addBodyPart(attachmentBodyPart);
            
            msg.setContent(multiPart);
            msg.saveChanges();
			log.info("message: " + message);

			Transport trans = session.getTransport();
			try
			{
				trans.connect(mailAuthen.getAccount(), mailAuthen.getPassword());
				trans.sendMessage(msg, msg.getAllRecipients());
				log.info("[Mail Send OK]");
			}
			catch (MessagingException mex)
			{
				error = true;
				ex = mex;
				log.info(mex.toString());
				if (ex instanceof IllegalStateException)
				{
					log.info(String.format("already connected!![UserName:%s , Password:%s]", mailAuthen.getAccount(),org.apache.commons.lang.StringUtils.repeat("*", mailAuthen.getPassword().length())));
				}
				else if (ex instanceof AuthenticationFailedException)
				{
					log.info(String.format("Authentication Failed Exception!![AuthUser:%s ,AuthPwd:%s]",mailAuthen.getAccount(),org.apache.commons.lang.StringUtils.repeat("*", mailAuthen.getPassword().length())));
				}
				else if (ex instanceof SendFailedException)
				{
					SendFailedException sfex = (SendFailedException) ex;
					Address[] invalid = sfex.getInvalidAddresses();
					StringBuilder strBuilder = new StringBuilder();

					if (invalid != null)
					{
						log.info("   ** Invalid Addresses [S] **   ");
						if (invalid != null)
						{
							for (int i = 0; i < invalid.length; i++)
							{
								log.info("         " + invalid[i]);
								strBuilder.append(invalid[i] + ",");
							}
						}
						log.info("    ** Invalid Addresses [E] **   ");
					}

					Address[] validUnsent = sfex.getValidUnsentAddresses();
					if (validUnsent != null)
					{
						log.info("   ** ValidUnsent Addresses [S] **   ");
						if (validUnsent != null)
						{
							for (int i = 0; i < validUnsent.length; i++)
							{
								log.info("         " + validUnsent[i]);
								strBuilder.append(validUnsent[i] + ",");
							}
						}
						log.info("   ** ValidUnsent Addresses [E] **   ");
					}

					Address[] validSent = sfex.getValidSentAddresses();
					if (validSent != null)
					{
						log.info("   ** ValidSent Addresses [S] **   ");
						if (validSent != null)
						{
							for (int i = 0; i < validSent.length; i++)
								log.info("         " + validSent[i]);
						}
						log.info("   ** ValidSent Addresses [E] **   ");
					}

				}

			}
			finally
			{
				trans.close();
				log.info("Close transport connection!!");
			}
			
			if (error) throw new CustomException("MAIL-0002",ex.getCause());    
			return true;
	}

	public void postMail(
			String smtpHostName, String smtpPort, String connectionTimeout, String socketIOTimeout, 
			List<String> recipients, String subject, String message, String fromUserName, String fromUserMail, String authUserName, String authPwd) throws CustomException
	{
		this.setSmtpHostName(smtpHostName);
		this.setSmtpPort(smtpPort);
		this.setConnectionTimeout(connectionTimeout);
		this.setSocketIOTimeout(socketIOTimeout);
		

		postMail(recipients, subject, message, fromUserName, fromUserMail, authUserName, authPwd);
	}
	
    public void postMail(List<String> recipients, String subject, String message, String fromUserName, String fromUserMail, String authUserName, String authPwd) throws CustomException
    {
    	StringBuffer sql = new StringBuffer();
		sql.append(
				"SELECT * FROM ENUMDEFVALUE L WHERE L.ENUMNAME = :ENUMNAME");
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("ENUMNAME", "V3MESMail_PWD");
		
		List<Map<String, Object>> sqlResult = null;
		try 
		{
			sqlResult = GenericServiceProxy.getSqlMesTemplate()
					.queryForList(sql.toString(), args);
		} 
		catch(Exception ex)
		{
			sqlResult = null;
			log.info(ex.getCause());
		}
		
		if (sqlResult != null && sqlResult.size() > 0)
		{
			fromUserName = ConvertUtil.getMapValueByName(sqlResult.get(0), "ENUMVALUE");
			fromUserMail = ConvertUtil.getMapValueByName(sqlResult.get(0), "DESCRIPTION");
			authUserName = ConvertUtil.getMapValueByName(sqlResult.get(0), "DEFAULTFLAG");
			authPwd = ConvertUtil.getMapValueByName(sqlResult.get(0), "DISPLAYCOLOR");
		}
		
    	List<String> toUserMailList = new ArrayList<String>();
		
    	if(recipients == null || recipients.size() <= 0)
    	{
    		throw new CustomException("MAIL-0004");
    	}
    	else
    	{
    		for(String toUserMail : recipients)
    		{
    			if(toUserMail.isEmpty())
    			{
    				log.info("to usermail is null");
    			}
    			else
    			{
    				toUserMailList.add(toUserMail);
    			}
    		}
    		
    		if(toUserMailList == null || toUserMailList.size() <= 0)
        	{
        		throw new CustomException("MAIL-0004");
        	}
    	}
    	if(fromUserMail == null || fromUserMail.isEmpty())
    	{
    		throw new CustomException("MAIL-0005");
    	}
    	if(authUserName == null || authUserName.isEmpty())
    	{
    		throw new CustomException("MAIL-0006");
    	}
    	if(authPwd == null || authPwd.isEmpty())
    	{
    		throw new CustomException("MAIL-0007");
    	}
    	
        String charSet = "UTF-8";
    	
    	log.info("SMTP Settings");
//		Properties props = System.getProperties();
//		props.remove("mail.transport.protocol");
//		props.remove("mail.smtp.host");
//		props.remove("mail.smtp.port");
//		props.remove("mail.smtp.auth");
//		props.remove("mail.smtp.connectiontimeout");
//		props.remove("mail.smtp.timeout");
//		props.remove("mail.smtp.starttls.enable");
//		props.remove("mail.smtp.ssl.enable");
//		props.remove("mail.smtp.ssl.trust");
        
    	try
		{
			Properties props = new Properties();
			// props.put("mail.transport.protocol", "smtp");
			props.put("mail.smtp.host", this.getSmtpHostName());
			props.put("mail.smtp.port", this.getSmtpPort());
			props.put("mail.smtp.auth", "true");
			if (this.getConnectionTimeout().isEmpty() == false)
			{
				props.put("mail.smtp.connectiontimeout", this.getConnectionTimeout());
			}
			if (this.getSocketIOTimeout().isEmpty() == false)
			{
				props.put("mail.smtp.timeout", this.getSocketIOTimeout());
			}
			// props.put("mail.smtp.starttls.enable", "true");
			// props.put("mail.smtp.ssl.enable", "true");
			// props.put("mail.smtp.ssl.trust", this.smtpHostName);

			log.info("Create Session, MimeMessage");
			Session sess = Session.getInstance(props);
			MimeMessage msg = new MimeMessage(sess);

			log.info("Sent Mail Time");
			msg.setSentDate(TimeUtils.getCurrentTimestamp());

			log.info("set Sender");
			InternetAddress fromUserInfo = new InternetAddress();
			fromUserInfo.setAddress(fromUserMail);
			if (fromUserName.isEmpty() == false)
			{
				fromUserInfo.setPersonal(fromUserName);
			}
			msg.setFrom(fromUserInfo);

			log.info("set Receiver List");
			InternetAddress[] addressTo = new InternetAddress[toUserMailList.size()];
			for (int i = 0; i < toUserMailList.size(); i++)
			{
				addressTo[i] = new InternetAddress(toUserMailList.get(i).toString());
			}
			msg.setRecipients(Message.RecipientType.TO, addressTo);
			log.info("set Title");
			msg.setSubject(subject, charSet);

			log.info("set Contents");
			msg.setText(message, charSet);

			log.info("set Header");
			msg.setHeader("Content-Type", "text/html;charset=UTF-8");
			msg.saveChanges();

			log.info("connecting...");
			Transport tr = sess.getTransport("smtp");
			try
			{
				tr.connect(smtpHostName, authUserName, authPwd);
			}
			catch (Exception ex)
			{
				tr.close();

				if (ex.getCause() == null || ex.getMessage() == null)
				{
					throw new CustomException("MAIL-0001");
				}
				else
				{
					throw new CustomException("MAIL-0003", ex.getMessage());
				}
			}

			try
			{
				log.info("sending...");
				tr.sendMessage(msg, msg.getAllRecipients());
			}catch(SendFailedException se)
			{
				log.warn("First sent failed");
				Address[] unSentAddresses = se.getValidUnsentAddresses();
				msg.setRecipients(Message.RecipientType.TO, unSentAddresses);
				log.warn("Second sending...");
				tr.sendMessage(msg, msg.getAllRecipients());
			}
			catch (Exception ex)
			{
	        	tr.close();
	        	
	        	if(ex.getCause() == null || ex.getMessage() == null)
	        	{
	        		throw new CustomException("MAIL-0002", ex.getMessage());
	        	}
	        	else
	        	{
	        		throw new CustomException("MAIL-0003", ex.getMessage());
	        	}
			}
//			InternetAddress[] recipient = new InternetAddress[toUserMailList.size()];
/*
			for (int i = 0; i < toUserMailList.size(); i++)
			{
				try
				{
//					recipient[i] = new InternetAddress(toUserMailList.get(i).toString());
					msg.setRecipients(Message.RecipientType.TO, toUserMailList.get(i).toString());
					tr.sendMessage(msg, msg.getAllRecipients());
				}
				catch(SendFailedException se)
				{
					se.getValidSentAddresses();
					se.getInvalidAddresses();
					se.getValidUnsentAddresses();
				}
				catch (Exception ex)
				{
					if (ex.getCause() == null || ex.getMessage() == null)
					{
						log.info("Failed - sendMessage");
					}
					else
					{
						log.info(String.format("Cannot send Email by the reason [%s] and E-Mail [%s]", ex.getMessage(), toUserMailList.get(i)));
					}
				}
			}
*/
			tr.close();

			log.info("To Mail : " + toUserMailList.toString() + "...");
			log.info("From Mail : " + fromUserMail);
			log.info("Subject : " + subject);
			log.info("[Completed]");
		}
    	catch (CustomException cex)
    	{
    		throw cex;
    	}
    	catch (Exception ex)
    	{
    		throw new CustomException("MAIL-0003", ex.getMessage());
    	}
    }

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}
  
}