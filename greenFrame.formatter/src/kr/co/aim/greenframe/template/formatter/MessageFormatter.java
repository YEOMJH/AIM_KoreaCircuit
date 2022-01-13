package kr.co.aim.greenframe.template.formatter;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.event.AbstractBundleEventTemplate;
import kr.co.aim.greenframe.util.xml.JdomUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.tibco.tibrv.TibrvMsg;

public class MessageFormatter extends AbstractBundleEventTemplate {
	private static Log log = LogFactory.getLog(MessageFormatter.class);

	private String dataField_TAG = "xmlData";

	public MessageFormatter()
	{

	} 

	public void onBundleMessage(String beanName, Object data)
	{
		Document document = null;

		if (data instanceof TibrvMsg)
		{
			document = SMessageUtil.getDocumentFromTibrvMsg((TibrvMsg) data, dataField_TAG);
		}

		try
		{
			if (StringUtils.equals(System.getProperty("svr"), "TEXsvr") && JdomUtils.getNode(document, "//ENVELOP") != null)
			{
				log.info("Message from DSP");
				document = setMessageFormatForDSP(document);
			}
		}
		catch (Exception e1)
		{
			log.error(e1.getMessage());
		}
		
		// set log header
		try
		{
			MDC.put("MES.MSGNAME", JdomUtils.getNodeText(document, "//Message/Header/MESSAGENAME"));
		}
		catch (Exception e2)
		{
			log.error(e2.getMessage());
		}

		try
		{
			MDC.put("MES.TRXID", JdomUtils.getNodeText(document, "//Message/Header/TRANSACTIONID"));
		}
		catch (Exception e1)
		{
			log.error(e1.getMessage());
		}

		log.info("[" + beanName + "] execute");

		GenericServiceProxy.getMessageTraceService().recordMessageLog(document, GenericServiceProxy.getConstantMap().INSERT_LOG_TYPE_RECEIVE);
		   
		StringBuilder strBuilder = new StringBuilder("RCRQ : ");
		try
		{
			strBuilder.append(MDC.get("MES.MSGNAME")).append(" ").append(MDC.get("MES.TRXID")).append(" ");

			log.info(strBuilder.toString());
		}
		catch (Exception e)
		{
			if (log.isDebugEnabled())
				e.printStackTrace();
			log.error(e.getMessage());
		}

		String originalMessage = JdomUtils.toString(document);
		String hidePasswordForLog = "";
		try
		{
			hidePasswordForLog = hidePasswordForLog(document, originalMessage);
		}
		catch (CustomException e)
		{
			hidePasswordForLog = originalMessage;
		}

		GenericServiceProxy.getMessageLogService().getLog().debug(new StringBuffer("RCRQ : ").append(hidePasswordForLog).toString());

		Object[] arguments = new Object[] { document };

		execute(arguments);
	}

	public String toCompactString(Document document)
	{
		XMLOutputter out = new XMLOutputter();
		Format format = Format.getCompactFormat();
		out.setFormat(format);

		return out.outputString(document.getRootElement());
	}

	public String hidePasswordForLog(Document doc, String sReplyMsg) throws CustomException
	{
		boolean isChanged = false;
		String inputPassword = "";
		String hiddenPassword = "";
		String messageName = SMessageUtil.getMessageName(doc);

		if (StringUtils.equals(messageName, "UserLogin") || StringUtils.equals(messageName, "ChangePassword"))
		{
			Document newDoc = (Document) doc.clone();

			try
			{
				inputPassword = JdomUtils.getNodeText(doc, "//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Header_Tag + "/PASSWORD");
			}
			catch (Exception e)
			{
			}

			if (StringUtils.isNotEmpty(inputPassword))
			{
				hiddenPassword = StringUtils.repeat("*", inputPassword.length());
				newDoc.getRootElement().getChild(SMessageUtil.Header_Tag).getChild("PASSWORD").setText(hiddenPassword);

				isChanged = true;
			}

			try
			{
				inputPassword = JdomUtils.getNodeText(doc, "//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Body_Tag + "/PASSWORD");
			}
			catch (Exception e)
			{
			}

			if (StringUtils.isNotEmpty(inputPassword))
			{
				hiddenPassword = StringUtils.repeat("*", inputPassword.length());
				newDoc.getRootElement().getChild(SMessageUtil.Body_Tag).getChild("PASSWORD").setText(hiddenPassword);

				isChanged = true;
			}

			try
			{
				inputPassword = JdomUtils.getNodeText(doc, "//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Body_Tag + "/NEWPASSWORD");
			}
			catch (Exception e)
			{
			}

			if (StringUtils.isNotEmpty(inputPassword))
			{
				hiddenPassword = StringUtils.repeat("*", inputPassword.length());
				newDoc.getRootElement().getChild(SMessageUtil.Body_Tag).getChild("NEWPASSWORD").setText(hiddenPassword);

				isChanged = true;
			}

			try
			{
				inputPassword = JdomUtils.getNodeText(doc, "//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Body_Tag + "/OLDPASSWORD");
			}
			catch (Exception e)
			{
			}

			if (StringUtils.isNotEmpty(inputPassword))
			{
				hiddenPassword = StringUtils.repeat("*", inputPassword.length());
				newDoc.getRootElement().getChild(SMessageUtil.Body_Tag).getChild("OLDPASSWORD").setText(hiddenPassword);

				isChanged = true;
			}

			if (isChanged)
			{
				sReplyMsg = JdomUtils.toString(newDoc);
			}
		}

		return sReplyMsg;
	}

	private Document setMessageFormatForDSP(Document document)
	{
		try
		{
			Element messageE = document.getRootElement();

			if (messageE != null)
			{
				if (StringUtils.equals(document.getRootElement().getName(), SMessageUtil.DSPMessage_Tag))
				{
					document.getRootElement().setName(SMessageUtil.Message_Tag);
					
					if(document.getRootElement().getChild(SMessageUtil.Header_Tag).getChild("NAME") != null)
					{
						document.getRootElement().getChild(SMessageUtil.Header_Tag).getChild("NAME").setName(SMessageUtil.MessageName_Tag);
					}
					
					if(document.getRootElement().getChild(SMessageUtil.Header_Tag).getChild("TransactionId") != null)
					{
						document.getRootElement().getChild(SMessageUtil.Header_Tag).getChild("TransactionId").setName("TRANSACTIONID");
					}
					
					if(document.getRootElement().getChild("DataLayer") != null)
					{
						document.getRootElement().getChild("DataLayer").setName(SMessageUtil.Body_Tag);
					}
					
					if(document.getRootElement().getChild("ReturnSet") != null)
					{
						document.getRootElement().getChild("ReturnSet").setName(SMessageUtil.Result_Name_Tag);
						
						if(document.getRootElement().getChild(SMessageUtil.Result_Name_Tag).getChild("ReturnCode") != null)
						{
							document.getRootElement().getChild(SMessageUtil.Result_Name_Tag).getChild("ReturnCode").setName(SMessageUtil.Result_ReturnCode);
						}
						if(document.getRootElement().getChild(SMessageUtil.Result_Name_Tag).getChild("ReturnMessage") != null)
						{
							document.getRootElement().getChild(SMessageUtil.Result_Name_Tag).getChild("ReturnMessage").setName(SMessageUtil.Result_ErrorMessage);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			log.info("Error Occurred - setMessageFormatForDSP");
		}

		return document;
	}
}