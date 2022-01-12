package kr.co.aim.messolution.generic.eventHandler;

import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.user.UserServiceProxy;
import kr.co.aim.greentrack.user.management.data.UserProfile;
import kr.co.aim.greentrack.user.management.data.UserProfileKey;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public abstract class SyncHandler implements EventFactory {
	
	//transport parameter in form of String or Document
	private ThreadLocal<Object> replyDoc = new ThreadLocal<Object>();
	public Object getReplyDoc() {
		return replyDoc.get();
	}

	public void setReplyDoc(Object doc) {
		this.replyDoc.set(doc);
	}
	
	private ThreadLocal<String> replySubjectName = new ThreadLocal<String>();
	public String getReplySubjectName() {
		if (replySubjectName.get() != null)
			return replySubjectName.get();
		else
			return "";
	}

	public void setReplySubjectName(String replySubjectName) {
		this.replySubjectName.set(replySubjectName);
	}

	protected ThreadLocal<String> messageName = new ThreadLocal<String>();;
	public String getMessageName() {
		if (messageName.get() != null)
			return messageName.get();
		else
			return "";
	}
	public void setMessageName(String messageName) {
		this.messageName.set(messageName);
	}
	
	protected ThreadLocal<String> transactionId = new ThreadLocal<String>();;
	public String getTransactionId() {
		if (transactionId.get() != null)
			return transactionId.get();
		else
			return "";
	}
	public void setTransactionId(String transactionId) {
		this.transactionId.set(transactionId);
	}
	protected ThreadLocal<String> originalSourceSubjectName = new ThreadLocal<String>();;
	public String getOriginalSourceSubjectName() {
		if (originalSourceSubjectName.get() != null)
			return originalSourceSubjectName.get();
		else
			return "";
	}
	public void setOriginalSourceSubjectName(String originalSourceSubjectName) {
		this.originalSourceSubjectName.set(originalSourceSubjectName);
	}
	
	protected ThreadLocal<String> eventUser = new ThreadLocal<String>();;
	public String getEventUser() {
		if (eventUser.get() != null)
			return eventUser.get();
		else
			return "";
	}
	public void setEventUser(String eventUser) {
		this.eventUser.set(eventUser);
	}
	protected ThreadLocal<String> eventComment = new ThreadLocal<String>();;

	public String getEventComment()
	{
		if (eventComment.get() != null)
		{
			UserProfile userData = new UserProfile();

			try
			{
				userData = UserServiceProxy.getUserProfileService().selectByKey(new UserProfileKey(this.getEventUser()));
			}
			catch (Exception e)
			{
				userData = null;
			}

			if (userData != null)
			{
				return "[" + userData.getUdfs().get("DEPARTMENT") + " / " + userData.getUserName() + " / " + userData.getUdfs().get("PHONENUMBER") + "] " + eventComment.get();
			}
			else
			{
				return eventComment.get();
			}
		}
		else
			return "";
	}
	
	public String getReserveSampleEventComment(String fromOper, List<Map<String, Object>> toOperList)
	{
		String comment = "";

		if (toOperList.size() > 0 && StringUtils.isNotEmpty(fromOper))
		{
			int count = 0;
			comment = "[From : " + fromOper + " / To : ";
			
			for (Map<String, Object> toOper : toOperList)
			{
				count++;
				comment += toOper.get("PROCESSOPERATIONNAME");

				if (count != toOperList.size())
				{
					comment += ", ";
				}
			}

			comment += "]";
		}
		
		comment += eventComment.get();
		
		if (eventComment.get() != null)
		{
			UserProfile userData = new UserProfile();

			try
			{
				userData = UserServiceProxy.getUserProfileService().selectByKey(new UserProfileKey(this.getEventUser()));
			}
			catch (Exception e)
			{
				userData = null;
			}

			if (userData != null)
			{
				return "[" + userData.getUdfs().get("DEPARTMENT") + " / " + userData.getUserName() + " / " + userData.getUdfs().get("PHONENUMBER") + " ] " + comment;
			}
			else
			{
				return comment;
			}
		}
		else
			return "";
	}
	
	public String getFutureActionEventComment(String operation)
	{
		String comment = "";
		
//		if (operationList.size() > 0)
//		{
//			int count = 0;
//			comment = "[ReserveOper : ";
//			
//			for (String operaton : operationList)
//			{
//				count++;
//				comment += operaton;
//
//				if (count != operationList.size())
//				{
//					comment += ", ";
//				}
//			}
//
//			comment += "]";
//		}

		if (StringUtils.isNotEmpty(operation))
		{
			comment = "[ReserveOper : " + operation + "]";
		}
		
		comment += eventComment.get();

		if (eventComment.get() != null)
		{
			UserProfile userData = new UserProfile();

			try
			{
				userData = UserServiceProxy.getUserProfileService().selectByKey(new UserProfileKey(this.getEventUser()));
			}
			catch (Exception e)
			{
				userData = null;
			}

			if (userData != null)
			{
				return "[" + userData.getUdfs().get("DEPARTMENT") + " / " + userData.getUserName() + " / " + userData.getUdfs().get("PHONENUMBER") + " ] " + comment;
			}
			else
			{
				return comment;
			}
		}
		else
			return "";	
	}
	
	public void setEventComment(String eventComment) {
		this.eventComment.set(eventComment);
	}
	protected ThreadLocal<String> language = new ThreadLocal<String>();;
	public String getLanguage() {
		if (language.get() != null)
			return language.get();
		else
			return "";
	}
	public void setLanguage(String language) {
		this.language.set(language);
	}

	public SyncHandler()
	{
		//constructor did once
		init();
	}
	
	public void init()
	{
		//initialize for thread-safe
		setEventComment("");
		setEventUser("");
		setLanguage("");
		setMessageName("");
		setOriginalSourceSubjectName("");
		setReplyDoc(null);
		setReplySubjectName("");
		setTransactionId("");
	}

	@Override
	public void execute(Document doc) throws Exception
	{
		init();
		
		setEventUser(SMessageUtil.getHeaderItemValue(doc, "EVENTUSER", false));
		setLanguage(SMessageUtil.getHeaderItemValue(doc, "LANGUAGE", false));
		setMessageName(SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", false));
		setOriginalSourceSubjectName(SMessageUtil.getHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", false));
		setTransactionId(SMessageUtil.getHeaderItemValue(doc, "TRANSACTIONID", false));
		
		//default return to first sender
		setReplySubjectName(getOriginalSourceSubjectName());
		
		//keep message name on communication
		String eventCommentTmp = SMessageUtil.getHeaderItemValue(doc, "EVENTCOMMENT", false);
		setEventComment(eventCommentTmp.isEmpty()?getMessageName():eventCommentTmp);
		
		try
		{
			setReplyDoc(doWorks(doc));
			
			handleSync(getReplyDoc(), getReplySubjectName());
		}
		catch (Exception ex)
		{
			//give higher initiative to it in business implement
			//sync have to communicate with original stuff
			if (getReplyDoc() == null)
				setReplyDoc(doc);
			
			handleFault((Document) getReplyDoc(), getReplySubjectName(), ex);
			
			if (ex instanceof CustomException)
				throw ex;
			else
				throw new CustomException(ex);
		}
		finally
		{
			GenericServiceProxy.getMessageTraceService().recordMessageLog(doc, GenericServiceProxy.getConstantMap().INSERT_LOG_TYPE_SEND);
		}
	}
	
	public abstract Object doWorks(Document doc) throws CustomException;	

	@Override
	public void handleSync(Object doc, String sendSubjectName)
	{
		eventLog.debug("sync handler on start");

		if (doc != null && !StringUtil.isEmpty(sendSubjectName))
		{
			// send reply to destined subject
			if (doc instanceof Document)
				try
				{
					GenericServiceProxy.getESBServive().sendReply(sendSubjectName, (Document) doc);
				}
				catch (CustomException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			else if (doc instanceof String)
				GenericServiceProxy.getESBServive().sendReply(sendSubjectName, doc.toString());
			else
				return;
		}

		eventLog.debug("sync handler on end");
	}

	@Override
	public void handleFault(Document doc, String sendSubjectName, Exception ex)
	{
		eventLog.debug("fault handler on start");

		if (doc != null && !StringUtil.isEmpty(sendSubjectName) && ex != null)
		{
			GenericServiceProxy.getMessageTraceService().recordErrorMessageLog(doc, ex, StringUtils.EMPTY);
			
			// send reply to destined subject
			if (doc instanceof Document)
			{
				try
				{
					GenericServiceProxy.getESBServive().sendError(sendSubjectName, doc, getLanguage(), ex);
				}
				catch (CustomException e){}
			}
			else
			{
				return;
			}
		}

		eventLog.debug("fault handler on end");
	}
}
