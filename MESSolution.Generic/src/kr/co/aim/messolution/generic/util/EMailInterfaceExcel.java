package kr.co.aim.messolution.generic.util;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.activation.*;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.util.TimeUtils;

//jinlj create by 2021/1/23

public class EMailInterfaceExcel {
private static Log log = LogFactory.getLog(EMailInterface.class);
	
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

	public EMailInterfaceExcel()
	{
		MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
		mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
		mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
		CommandMap.setDefaultCommandMap(mc);
	}

	public EMailInterfaceExcel(String smtpHostName, String smtpPort, String connectionTimeout, String socketIOTimeout)
	{
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

	public void postMail(
			String smtpHostName, String smtpPort, String connectionTimeout, String socketIOTimeout, 
			List<String> recipients, String subject, String message, String carrierName, ByteArrayOutputStream baos, String fromUserName, String fromUserMail, String authUserName, String authPwd) throws CustomException
	{
		this.setSmtpHostName(smtpHostName);
		this.setSmtpPort(smtpPort);
		this.setConnectionTimeout(connectionTimeout);
		this.setSocketIOTimeout(socketIOTimeout);
		

		postMail(recipients, subject, message, carrierName, baos, fromUserName, fromUserMail, authUserName, authPwd);
	}
	
    public void postMail(List<String> recipients, String subject, String message, String carrierName, ByteArrayOutputStream baos, String fromUserName, String fromUserMail, String authUserName, String authPwd) throws CustomException
    {
    	//carrierName = wen jian ming
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
        
    	try
		{
			Properties props = new Properties();
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
			
		    BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(message);
			
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);
			messageBodyPart = new MimeBodyPart();
			DataSource source = new ByteArrayDataSource(baos.toByteArray(), "application/msexcel");
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(MimeUtility.encodeText(carrierName + ".xls"));
			multipart.addBodyPart(messageBodyPart);		
			msg.setContent(multipart);

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
				Thread.currentThread().setContextClassLoader(javax.mail.Message.class.getClassLoader());
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

}