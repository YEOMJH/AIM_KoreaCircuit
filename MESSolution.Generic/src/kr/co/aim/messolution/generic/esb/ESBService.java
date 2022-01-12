package kr.co.aim.messolution.generic.esb;


import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.object.ErrorDef;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenflow.exception.BpelException;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.esb.IRequester;
import kr.co.aim.greenframe.exception.greenFrameErrorSignal;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class ESBService implements ApplicationContextAware {
	
	private static Log log = LogFactory.getLog(ESBService.class);
	private ApplicationContext	applicationContext;
	public static final String 		SUCCESS = "0";
	private Map<String, String> sendSubjectMap = new Hashtable<String, String>();
	
	//global destination
	private ThreadLocal<String> replySubjectName = new ThreadLocal<String>();
	
	public void init()
	{
		sendSubjectMap.put("CNXsvr", makeCustomServerLocalSubject("CNXsvr"));
		sendSubjectMap.put("PEXsvr", makeCustomServerLocalSubject("PEXsvr"));
		sendSubjectMap.put("TEXsvr", makeCustomServerLocalSubject("TEXsvr"));
		sendSubjectMap.put("RMSsvr", makeCustomServerLocalSubject("RMSsvr"));
		sendSubjectMap.put("QRYsvr", makeCustomServerLocalSubject("QRYsvr"));
		sendSubjectMap.put("TEMsvr", makeCustomServerLocalSubject("TEMsvr"));
		sendSubjectMap.put("FMCsvr", makeCustomServerLocalSubject("FMCsvr"));
		sendSubjectMap.put("DSPsvr", makeCustomServerLocalSubject("RTD"));
		sendSubjectMap.put("CNMsvr", makeCustomServerLocalSubject("CNMsvr"));
		sendSubjectMap.put("EDCsvr", makeCustomServerLocalSubject("EDCsvr"));
		sendSubjectMap.put("PEMsvr", makeCustomServerLocalSubject("PEMsvr"));
		sendSubjectMap.put("SPC", makeCustomServerLocalSubject("SPC"));
		sendSubjectMap.put("FDC", makeCustomServerLocalSubject("FDC"));
		sendSubjectMap.put("MCS", makeCustomServerLocalSubject("MCS"));
		sendSubjectMap.put("PMS", makeCustomServerLocalSubject("PMS"));
		sendSubjectMap.put("R2R", makeCustomServerLocalSubject("R2R"));
		sendSubjectMap.put("IFS", makeCustomServerLocalSubject("IFS"));
		sendSubjectMap.put("FMB", makeCustomServerLocalSubject("FMB"));
	}
	
	public String getReplySubject()
	{
		String result = this.replySubjectName.get();
		
		if (StringUtil.isEmpty(result))
			return "";
		else
			return result;
	}
	
	public void setReplySubject(String replySubjectName)
	{
		this.replySubjectName.set(replySubjectName);
	}
	
	public String getSendSubject(String serverName)
	{
		if(log.isInfoEnabled()){
			log.debug("serverName = " + serverName);
		}
		
		return sendSubjectMap.get(serverName);
	}
	
	public String makeCustomServerLocalSubject(String serverName)
	{
		//_LOCAL.KRK0100.DEV.MES.MES.QRYsvr
		//A1.SO.G1.PROD.MES.QRYsvr
		
		if(log.isInfoEnabled()){
			log.debug("serverName = " + serverName);
		}
		
		StringBuffer serverSubject = new StringBuffer();
		

		if(StringUtils.equals(serverName, "CNXsvr") ||
				StringUtils.equals(serverName, "PEXsvr") ||
				StringUtils.equals(serverName, "TEXsvr") ||
				StringUtils.equals(serverName, "EDCsvr"))
		{
			serverSubject.append("_LOCAL.").
						  append(System.getProperty("location")).
				          append(".").append(System.getProperty("factory")).
			              append(".").append(System.getProperty("cim")).
			              append(".").append(System.getProperty("mode")).
			              append(".").append(System.getProperty("shop")).
			              append(".").append(serverName);
		}
		else if (StringUtils.equals("MCS", serverName))
		{
			serverSubject.append(System.getProperty("location")).
						  append(".").append(System.getProperty("factory")).
						  append(".").append(GenericServiceProxy.getConstantMap().Subject_MCS).
						  append(".").append(System.getProperty("mode")).
			              append(".").append(System.getProperty("shop")).
						  append(".").append("HIFsvr");
		}
		else if(StringUtils.equals("RTD", serverName))
		{
			serverSubject.append("_LOCAL.").
			  append(System.getProperty("location")).
			  append(".").append(System.getProperty("factory")).
			  append(".").append(GenericServiceProxy.getConstantMap().Subject_RTD).
			  append(".").append(System.getProperty("mode")).
              append(".").append(System.getProperty("shop")).
			  append(".").append("DSPsvr");
		}
		else if(StringUtil.equals("PMS", serverName))
		{
			serverSubject.append(System.getProperty("location")).
			append(".").append(System.getProperty("factory")).
			append(".").append(serverName).
			append(".").append(System.getProperty("mode")).
			append(".").append(System.getProperty("shop")).
			append(".").append("NM");
		}
		else if(StringUtil.in(serverName,"FDC","SPC"))
		{
			// HEFEI.V3.ServerName.PRD.FAB.*
			serverSubject.append(System.getProperty("location")).
			append(".").append(System.getProperty("factory")).
			append(".").append(serverName).
			append(".").append(System.getProperty("mode")).
			append(".").append(System.getProperty("shop")).
			append(".").append("All");
		}
		else if(StringUtil.in(serverName,"R2R"))
		{
			// HEFEI.V3.ServerName.PRD.FAB.*
			serverSubject.append(System.getProperty("location")).
			append(".").append(System.getProperty("factory")).
			append(".").append(serverName).
			append(".").append(System.getProperty("mode")).
			append(".").append(System.getProperty("shop")).
			append(".").append("*");
		}
		else if(StringUtils.equals("IFS", serverName))
		{
			// HEFEI.V3.MES.PRD.FAB.IFSsvr
			serverSubject.append(System.getProperty("location")).
			append(".").append(System.getProperty("factory")).
			append(".").append(System.getProperty("cim")).
			append(".").append(System.getProperty("mode")).
			append(".").append(System.getProperty("shop")).
			append(".").append("IFSsvr");
		}
		else if(StringUtils.equals("FMB", serverName))
		{
			// HEFEI.V3.MES.PRD.FAB.IFSsvr
			serverSubject.append(System.getProperty("location")).
			append(".").append(System.getProperty("factory")).
			append(".").append(System.getProperty("cim")).
			append(".").append(System.getProperty("mode")).
			append(".").append(System.getProperty("shop")).
			append(".").append("FMBsvr");
		}
		else
		{
			serverSubject.
			  	append(System.getProperty("location")).
	            append(".").append(System.getProperty("factory")).
	            append(".").append(System.getProperty("cim")).
	            append(".").append(System.getProperty("mode")).
	            append(".").append(System.getProperty("shop")).
	            append(".").append(serverName);
		}
				
		log.info("Maked SubjectName=" + serverSubject.toString());
		
		return serverSubject.toString(); 
	}
	
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		
		log.debug("Adujusting bundle dependency");
		init();
	}
	
	public Document sendRequest(String subject, Document doc) throws Exception
	{
		if(log.isInfoEnabled()){
			log.debug("subject = " + subject);
		}
		
		String sendMsg = JdomUtils.toString(doc);
		String reply = (String) greenFrameServiceProxy.getGenericSender().sendRequest(subject, sendMsg);
		Document createDocument = JdomUtils.loadText(reply);
		return createDocument;
	}
	
	public Document sendRequestTimeOut(String subject, Document doc, int timeOut) throws Exception
	{
		if(log.isInfoEnabled()){
			log.debug("subject = " + subject);
		}
		
		String sendMsg = JdomUtils.toString(doc);
		String reply = (String) greenFrameServiceProxy.getGenericSender().sendRequest(subject, sendMsg, timeOut);
		Document createDocument = JdomUtils.loadText(reply);
		return createDocument;
	}
	
	public Document sendRequestBySender(String subject, Document doc, String senderName) throws Exception
	{
		if(log.isInfoEnabled()){
			log.debug("subject = " + subject);
			log.debug("senderName = " + senderName);
		}
		
		String sendMsg = JdomUtils.toString(doc);
		String reply = (String)GenericServiceProxy.getGenericSender(senderName).sendRequest(subject, sendMsg);
		log.debug(reply);
		Document createDocument = JdomUtils.loadText(reply);
		
		return createDocument;
	}
	
	public Document sendRequestBySenderTimeOut(String subject, Document doc, String senderName, int timeOut) throws Exception
	{
		if(log.isInfoEnabled()){
			log.debug("subject = " + subject);
			log.debug("senderName = " + senderName);
		}
		
		String sendMsg = JdomUtils.toString(doc);
		String reply = (String)GenericServiceProxy.getGenericSender(senderName).sendRequest(subject, sendMsg, timeOut);
		log.debug(reply);
		Document createDocument = JdomUtils.loadText(reply);
		
		return createDocument;
	}
	
	public void send(String subject, Document doc ) throws CustomException
	{
		if(log.isInfoEnabled()){
			log.debug("subject = " + subject);
		}
		
		String sendMsg = JdomUtils.toString(doc);
	    greenFrameServiceProxy.getGenericSender().send(subject, sendMsg);

		String hidePasswordForLog = hidePasswordForLog(doc, sendMsg);

		log.debug(" SEND : Subject=" + subject);
		log.debug(" SEND : Message=" + hidePasswordForLog);
	}
	
	public void sendBySender(Document doc, String senderName) throws CustomException
	{
		String sendMsg = JdomUtils.toString(doc);
		GenericServiceProxy.getGenericSender(senderName).send(sendMsg);

		String hidePasswordForLog = hidePasswordForLog(doc, sendMsg);

		log.debug("senderName = " + senderName);
		log.debug(" SEND : Message=" + hidePasswordForLog);
	}
	
	public void sendBySender(String subject, Document doc, String senderName)
	{
		if(log.isInfoEnabled()){
			log.debug("subject = " + subject);
			log.debug("senderName = " + senderName);
		}
		
		// add return  
		SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, SUCCESS);
		//get success report
		SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage,
			SMessageUtil.getElement("//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Result_Name_Tag + "/", doc, SMessageUtil.Result_ErrorMessage));
		
		String sendMsg = JdomUtils.toString(doc);
		GenericServiceProxy.getGenericSender(senderName).send(subject, sendMsg);
		
		log.debug(" SEND : Subject=" + subject);
		log.debug(" SEND : Message=" + sendMsg);
	}

	public void sendBySenderWithoutChangeReturnElement(String subject, Document doc, String senderName)
	{
		if (log.isInfoEnabled())
		{
			log.debug("subject = " + subject);
			log.debug("senderName = " + senderName);
		}

		String sendMsg = JdomUtils.toString(doc);
		GenericServiceProxy.getGenericSender(senderName).send(subject, sendMsg);

		log.debug(" SEND : Subject=" + subject);
	}

	public void sendBySender(String subject, Document doc, String senderName, String returnCode, String returnMsg)
	{
		if(log.isInfoEnabled()){
			log.debug("subject = " + subject);
			log.debug("senderName = " + senderName);
		}
		
		// add return  
		SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, returnCode);
		SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, returnMsg);
		
		String sendMsg = JdomUtils.toString(doc);
		GenericServiceProxy.getGenericSender(senderName).send(subject, sendMsg);
		log.debug(" SEND : Subject=" + subject);
		log.debug(" SEND : Message=" + sendMsg);
	}
	
	public void sendBySender(String subject, String sSendMsg, String senderName)
	{
		if(log.isInfoEnabled()){
			log.debug("subject = " + subject);
			log.debug("senderName = " + senderName);
		}
		
		GenericServiceProxy.getGenericSender(senderName).send(subject, sSendMsg);
		log.debug(" SEND : Subject=" + subject);
	}
	
	public void send(String beanName, String subject, Document doc )
	{
		if(log.isInfoEnabled()){
			log.debug("beanName = " + beanName);
			log.debug("subject = " + subject);
		}
		
		String sendMsg = doc.toString();
		IRequester requester = (IRequester)this.applicationContext.getBean(beanName);
		requester.send(subject, doc.toString());
		
		log.debug(" SEND : Subject=" + subject);
		log.debug(" SEND : Message=" + sendMsg);
		
	}
	
	public void sendReply( String replySubject )
	{
		if(log.isInfoEnabled()){
			log.debug("replySubject = " + replySubject);
		}
		
		String sReplyMsg = SMessageUtil.replyXMLMessage(SUCCESS );

		greenFrameServiceProxy.getGenericSender().setDataField("xmlData");
		greenFrameServiceProxy.getGenericSender().reply(replySubject, sReplyMsg);
   
   log.debug(" SERP : Subject=" + replySubject);
   log.debug(" SERP : Message=" + sReplyMsg);
	}
	
	public void sendReplyBySender( String replySubject , String senderName)
	{
		if(log.isInfoEnabled()){
			log.debug("replySubject = " + replySubject);
			log.debug("senderName = " + senderName);
		}
		
	   String sReplyMsg = SMessageUtil.replyXMLMessage(SUCCESS );

	   GenericServiceProxy.getGenericSender(senderName).setDataField("xmlData");
	   GenericServiceProxy.getGenericSender(senderName).reply(replySubject, sReplyMsg);
	   
	   log.debug(" SERP : Subject=" + replySubject);
	   log.debug(" SERP : Message=" + sReplyMsg);
	}
	
	public void sendReply( String replySubject, String sReplyMsg )
	{
	   greenFrameServiceProxy.getGenericSender().setDataField("xmlData");
	   greenFrameServiceProxy.getGenericSender().reply(replySubject, sReplyMsg);

	   log.debug(" SERP : Subject=" + replySubject);
	   GenericServiceProxy.getMessageLogService().getLog().debug(" SERP : Message=" + sReplyMsg);
	}
	
	public void sendReplyBySender( String replySubject, String sReplyMsg , String senderName)
	{
		GenericServiceProxy.getGenericSender(senderName).setDataField("xmlData");
		GenericServiceProxy.getGenericSender(senderName).reply(replySubject, sReplyMsg);

	   log.debug(" SERP : Subject=" + replySubject);
	   GenericServiceProxy.getMessageLogService().getLog().debug(" SERP : Message=" + sReplyMsg);
	}


	public void sendReply(String replySubject, Document doc) throws CustomException
	{
		String sReplyMsg = null;

		try
		{
			// keep reason code even though failed
			if (StringUtil.isEmpty(SMessageUtil.getReturnItemValue(doc, SMessageUtil.Result_ReturnCode, false)))
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, SUCCESS);

			String customComment = SMessageUtil.getReturnItemValue(doc, SMessageUtil.Result_ErrorMessage, false);

			if (!StringUtil.isEmpty(customComment))
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, customComment);
			else
				SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, "");
		}
		catch (CustomException ce)
		{
			log.debug(ce.getMessage());
			SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, "");
		}

		sReplyMsg = JdomUtils.toString(doc);

		String hidePasswordForLog = hidePasswordForLog(doc, sReplyMsg);

		greenFrameServiceProxy.getGenericSender().setDataField("xmlData");
		greenFrameServiceProxy.getGenericSender().reply(replySubject, hidePasswordForLog);

		log.debug(" SERP : Subject=" + replySubject);
		GenericServiceProxy.getMessageLogService().getLog().debug(" SERP : Message=" + sReplyMsg);
	}

	public void sendReplyBySender( String replySubject, Document doc , String senderName) 
	{
	   String sReplyMsg = null;
	   SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, SUCCESS);
	   SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, "");
	  
	   sReplyMsg = JdomUtils.toString(doc);

	   GenericServiceProxy.getGenericSender(senderName).setDataField("xmlData");
	   GenericServiceProxy.getGenericSender(senderName).reply(replySubject, sReplyMsg);

	   log.debug(" SERP : Subject=" + replySubject);
	   GenericServiceProxy.getMessageLogService().getLog().debug(" SERP : Message=" + sReplyMsg);
	}

	public void sendReplyBySenderWithoutChangeReturnElement(String replySubject, Document doc, String senderName)
	{
		if (log.isInfoEnabled())
		{
			log.debug("replySubject = " + replySubject);
			log.debug("senderName = " + senderName);
		}

		String sReplyMsg = JdomUtils.toString(doc);

		GenericServiceProxy.getGenericSender(senderName).setDataField(SMessageUtil.DataFieldName);
		GenericServiceProxy.getGenericSender(senderName).reply(replySubject, sReplyMsg);

		log.debug(" SERP : Subject=" + replySubject);
		log.debug(" SERP : Message=" + sReplyMsg);
	}

	public void sendReplyBySender( String replySubject, String MsgName, Document doc , String senderName) 
	{
	   String sReplyMsg = null;
	   SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, SUCCESS);
	   SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, "");
	   
	   SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME",       MsgName);
	  
	   sReplyMsg = JdomUtils.toString(doc);

	   GenericServiceProxy.getGenericSender(senderName).setDataField("xmlData");
	   GenericServiceProxy.getGenericSender(senderName).reply(replySubject, sReplyMsg);

	   log.debug(" SERP : Subject=" + replySubject);
	   GenericServiceProxy.getMessageLogService().getLog().debug(" SERP : Message=" + sReplyMsg);
	}

	public void sendError(String replySubject, Document doc, String lanuage, Exception e) throws CustomException
	{
		if (log.isInfoEnabled())
		{
			log.debug("replySubject = " + replySubject);
		}

		if (e instanceof BpelException)
		{
			e = ((BpelException) e).getNativeException();
		}
		String sReplyMsg = this.makeSendError(replySubject, doc, lanuage, e);

		greenFrameServiceProxy.getGenericSender().setDataField("xmlData");

		if (StringUtils.isNotEmpty(replySubject) == true)
		{
			greenFrameServiceProxy.getGenericSender().reply(replySubject, sReplyMsg);
		}

		String hidePasswordForLog = hidePasswordForLog(doc, sReplyMsg);

		log.error(" SERP : Subject=" + replySubject);
		log.error(" SERP : Message=" + hidePasswordForLog);
	}

	private String makeSendError(String replySubject, Document doc, String lanuage, Exception e){
		
		String errorCode = "";
		String korErrorMsg = "";
		String engErrorMsg = "";
		String chaErrorMsg = "";
		String locErrorMsg = "";
		
		Throwable orgEx = e;
		
		Throwable targetEx;
		if ( orgEx.getCause() instanceof InvocationTargetException )
			targetEx = ((InvocationTargetException) orgEx.getCause()).getTargetException();
		else if ( orgEx.getCause() instanceof CustomException )
			targetEx = ((CustomException) orgEx.getCause());
		else if ( orgEx.getCause() instanceof NotFoundSignal )
			targetEx = ((NotFoundSignal) orgEx.getCause());
		else if ( orgEx.getCause() instanceof DuplicateNameSignal )
			targetEx = ((DuplicateNameSignal) orgEx.getCause());
		else if ( orgEx.getCause() instanceof FrameworkErrorSignal )
			targetEx = ((FrameworkErrorSignal) orgEx.getCause());
		else if ( orgEx.getCause() instanceof InvalidStateTransitionSignal )
			targetEx = ((InvalidStateTransitionSignal) orgEx.getCause());
		else if ( orgEx.getCause() instanceof greenFrameErrorSignal )
			targetEx = ((greenFrameErrorSignal) orgEx.getCause());
		else
			targetEx = orgEx;
		
		if ( targetEx instanceof NotFoundSignal )
		{
			errorCode = "NotFoundSignal";
		
			korErrorMsg = 
					MessageFormat.format("[{0}] {1}", ((NotFoundSignal)targetEx).getErrorCode(), ((NotFoundSignal)targetEx).getMessage() );
			engErrorMsg = 
					MessageFormat.format("[{0}] {1}", ((NotFoundSignal)targetEx).getErrorCode(), ((NotFoundSignal)targetEx).getMessage() );
			chaErrorMsg = 
					MessageFormat.format("[{0}] {1}", ((NotFoundSignal)targetEx).getErrorCode(), ((NotFoundSignal)targetEx).getMessage() );
			locErrorMsg = 
					MessageFormat.format("[{0}] {1}", ((NotFoundSignal)targetEx).getErrorCode(), ((NotFoundSignal)targetEx).getMessage() );
			
		}
		else if ( targetEx instanceof DuplicateNameSignal )
		{
			errorCode = "DuplicateNameSignal";

			korErrorMsg =
					MessageFormat.format("[{0}] {1}", ((DuplicateNameSignal)targetEx).getErrorCode(), ((DuplicateNameSignal)targetEx).getMessage() );
			engErrorMsg = 
					MessageFormat.format("[{0}] {1}", ((DuplicateNameSignal)targetEx).getErrorCode(), ((DuplicateNameSignal)targetEx).getMessage() );
			chaErrorMsg = 
					MessageFormat.format("[{0}] {1}", ((DuplicateNameSignal)targetEx).getErrorCode(), ((DuplicateNameSignal)targetEx).getMessage() );
			locErrorMsg = 
					MessageFormat.format("[{0}] {1}", ((DuplicateNameSignal)targetEx).getErrorCode(), ((DuplicateNameSignal)targetEx).getMessage() );
		}
		else if ( targetEx instanceof FrameworkErrorSignal)
		{
			errorCode = "FrameworkErrorSignal";
		
			korErrorMsg =
					MessageFormat.format("[{0}] {1}", ((FrameworkErrorSignal)targetEx).getErrorCode(), ((FrameworkErrorSignal)targetEx).getMessage() );
			engErrorMsg = 
					MessageFormat.format("[{0}] {1}", ((FrameworkErrorSignal)targetEx).getErrorCode(), ((FrameworkErrorSignal)targetEx).getMessage() );
			chaErrorMsg = 
					MessageFormat.format("[{0}] {1}", ((FrameworkErrorSignal)targetEx).getErrorCode(), ((FrameworkErrorSignal)targetEx).getMessage() );
			locErrorMsg = 
					MessageFormat.format("[{0}] {1}", ((FrameworkErrorSignal)targetEx).getErrorCode(), ((FrameworkErrorSignal)targetEx).getMessage() );
		}
		else if ( targetEx instanceof InvalidStateTransitionSignal)
		{
			errorCode = "InvalidStateTransitionSignal";
			
			korErrorMsg =
					MessageFormat.format("[{0}] {1}", ((InvalidStateTransitionSignal)targetEx).getErrorCode(), ((InvalidStateTransitionSignal)targetEx).getMessage() );
			engErrorMsg = 
					MessageFormat.format("[{0}] {1}", ((InvalidStateTransitionSignal)targetEx).getErrorCode(), ((InvalidStateTransitionSignal)targetEx).getMessage() );
			chaErrorMsg = 
					MessageFormat.format("[{0}] {1}", ((InvalidStateTransitionSignal)targetEx).getErrorCode(), ((InvalidStateTransitionSignal)targetEx).getMessage() );
			locErrorMsg = 
					MessageFormat.format("[{0}] {1}", ((InvalidStateTransitionSignal)targetEx).getErrorCode(), ((InvalidStateTransitionSignal)targetEx).getMessage() );
		}
		else if ( targetEx instanceof CustomException )
		{
			if (((CustomException)targetEx).errorDef != null)
			{
				errorCode = ((CustomException)targetEx).errorDef.getErrorCode();
				ErrorDef errorDef = ((CustomException)targetEx).errorDef;
				
				korErrorMsg = errorDef.getKor_errorMessage();
				engErrorMsg = errorDef.getEng_errorMessage();
				chaErrorMsg = errorDef.getCha_errorMessage();
				locErrorMsg = errorDef.getLoc_errorMessage();
			}
			else
			{
				errorCode = "UndefinedCode";
				korErrorMsg = targetEx.getMessage();engErrorMsg = targetEx.getMessage();chaErrorMsg = targetEx.getMessage();locErrorMsg = targetEx.getMessage();
			}
		}
		else if ( targetEx instanceof greenFrameErrorSignal )
		{
			errorCode = "greenFrameErrorSignal";
			ErrorDef errorDef = GenericServiceProxy.getErrorDefMap().getErrorDef(errorCode);
			
			if (errorDef != null)
			{
				korErrorMsg =
					MessageFormat.format(errorDef.getKor_errorMessage(), ((greenFrameErrorSignal)targetEx).getErrorCode(), ((greenFrameErrorSignal)targetEx).getMessage() );
				engErrorMsg = 
					MessageFormat.format(errorDef.getEng_errorMessage(), ((greenFrameErrorSignal)targetEx).getErrorCode(), ((greenFrameErrorSignal)targetEx).getMessage() );
				chaErrorMsg = 
					MessageFormat.format(errorDef.getCha_errorMessage(), ((greenFrameErrorSignal)targetEx).getErrorCode(), ((greenFrameErrorSignal)targetEx).getMessage() );
				locErrorMsg = 
					MessageFormat.format(errorDef.getLoc_errorMessage(), ((greenFrameErrorSignal)targetEx).getErrorCode(), ((greenFrameErrorSignal)targetEx).getMessage() );
			}
		}
		else if (targetEx instanceof NullPointerException)
		{
			errorCode = "NullValue";
			
			korErrorMsg = "System field has null";
			engErrorMsg = "System filed has null";
			chaErrorMsg = "System filed has null";
			locErrorMsg = "System filed has null"; 
		}
		else 
		{
			errorCode = "UndefinedCode";
			ErrorDef errorDef = GenericServiceProxy.getErrorDefMap().getErrorDef("UndefinedCode");
			
			if (errorDef != null)
			{
				korErrorMsg =
					MessageFormat.format(errorDef.getKor_errorMessage(), targetEx.getMessage());
				engErrorMsg =
					MessageFormat.format(errorDef.getEng_errorMessage(), targetEx.getMessage());
				chaErrorMsg =
					MessageFormat.format(errorDef.getCha_errorMessage(), targetEx.getMessage());
				locErrorMsg = 
					MessageFormat.format(errorDef.getLoc_errorMessage(), targetEx.getMessage());
			}
		}
		
	   String sReplyMsg = null;
	   SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ReturnCode, errorCode);
	   
	   if ( lanuage.equals("KOR") == true )
		   SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, korErrorMsg);
	   else if ( lanuage.equals("ENG") == true )
		   SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, engErrorMsg);
	   else if ( lanuage.equals("CHA") == true )
		   SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, chaErrorMsg);
	   else
		   SMessageUtil.setResultItemValue(doc, SMessageUtil.Result_ErrorMessage, locErrorMsg);
	   sReplyMsg = JdomUtils.toString(doc);
	   
	   return sReplyMsg;
	}
	public void sendErrorBySender( String replySubject, Document doc, String lanuage, Exception e, String senderName)
	{
		if(e instanceof BpelException){
			e = ((BpelException)e).getNativeException();
		}
	   String sReplyMsg = this.makeSendError(replySubject, doc, lanuage, e);

	   GenericServiceProxy.getGenericSender(senderName).setDataField("xmlData");
	   if(StringUtils.isNotEmpty(replySubject) == true)
		   GenericServiceProxy.getGenericSender(senderName).reply(replySubject, sReplyMsg);

	   log.error(" SERP : Subject=" + replySubject);
	   log.error(" SERP : Message=" + sReplyMsg);
	}
	
	public Document sendBySenderReturnMessage(String replySubject, Document doc, Element element, String senderName, String returnMsg) throws Exception
	{
		if(log.isInfoEnabled()){
			log.debug("replySubject = " + replySubject);
			log.debug("senderName = " + senderName);
		}
		
		String sReplyMsg = null;
		
		Element message = new Element( SMessageUtil.Message_Tag );  
		
		Element oriheader = doc.getDocument().getRootElement().getChild("Header");
		
		Element header = new Element("Header");
		
		Element messagename = new Element("MESSAGENAME");
		messagename.setText(oriheader.getChildText("MESSAGENAME"));
		header.addContent(messagename);
		
		Element shopName = new Element("SHOPNAME");
		shopName.setText(oriheader.getChildText("SHOPNAME"));
		header.addContent(shopName);
		
		Element machineName = new Element("MACHINENAME");
		machineName.setText(oriheader.getChildText("MACHINENAME"));
		header.addContent(machineName);
		
		Element transactionid = new Element("TRANSACTIONID");
		transactionid.setText(oriheader.getChildText("TRANSACTIONID"));
		header.addContent(transactionid);
		
		Element originalSourceSubjectName = new Element("ORIGINALSOURCESUBJECTNAME");
		originalSourceSubjectName.setText(oriheader.getChildText("ORIGINALSOURCESUBJECTNAME"));
		header.addContent(originalSourceSubjectName);
		
		Element toolId = new Element("TOOLID");
		toolId.setText(oriheader.getChildText("TOOLID"));
		header.addContent(toolId);
		
		Element processFlowId = new Element("PROCESSFLOWID");
		processFlowId.setText(oriheader.getChildText("PROCESSFLOWID"));
		header.addContent(processFlowId);
		
		Element systemByTes = new Element("SYSTEMBYTES");
		systemByTes.setText(oriheader.getChildText("SYSTEMBYTES"));
		header.addContent(systemByTes);
		
		Element eventUser = new Element("EVENTUSER");
		eventUser.setText(oriheader.getChildText("EVENTUSER"));
		header.addContent(eventUser);
		
		Element eventComment = new Element("EVENTCOMMENT");
		eventComment.setText(oriheader.getChildText("EVENTCOMMENT"));
		header.addContent(eventComment);
				
		message.addContent(header);
		
		if ( element != null )
		{
			message.addContent(element);
		}

		Element returnElement = new Element("Return");
		
		Element returnCode = new Element("RETURNCODE");
		returnCode.setText(SUCCESS);
		returnElement.addContent(returnCode);
		
		Element returnMessage = new Element("RETURNMESSAGE");
		returnMessage.setText(returnMsg);
		returnElement.addContent(returnMessage);
		
		message.addContent(returnElement);
		
		Document replyDoc = new Document( message ); 
		   
		sReplyMsg = JdomUtils.toString(replyDoc);
		
		GenericServiceProxy.getGenericSender(senderName).setDataField("xmlData");
		GenericServiceProxy.getGenericSender(senderName).reply(replySubject, sReplyMsg);

		String hidePasswordForLog = hidePasswordForLog(replyDoc, sReplyMsg);

		log.debug(" SEND : Subject=" + replySubject);
		log.debug(" SEND : Message=" + hidePasswordForLog);

		return replyDoc;
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
	
	public void sendBySenderToFMB(Document doc)
	{
		String subject = this.getSendSubject("FMB");
		
		if(log.isInfoEnabled()){
			log.debug("subject = " + subject);
		}
		
		String sendMsg = JdomUtils.toString(doc);
		GenericServiceProxy.getGenericSender("FMBSender").send(subject, sendMsg);
		log.debug(" SEND : Subject=" + subject);
		log.debug(" SEND : Message=" + sendMsg);
	}
}
