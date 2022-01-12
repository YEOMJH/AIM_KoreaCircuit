/**
 * 
 */
package kr.co.aim.greenframe.template.workflow;

import kr.co.aim.greenflow.core.activity.Activity;
import kr.co.aim.greenflow.core.activity.BpelProcess;
import kr.co.aim.greenframe.fos.greenflow.BpelExecutionEventAdaptor;
import kr.co.aim.greenframe.util.xml.JdomUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;
import org.jdom.Document;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author gksong
 * 
 */
public class CustomEventAdaptor extends BpelExecutionEventAdaptor implements
		ApplicationContextAware {

	private static Log log = LogFactory.getLog(CustomEventAdaptor.class);
	/**
	 * @uml.property  name="applicationContext"
	 * @uml.associationEnd  
	 */
	private ApplicationContext applicationContext;

	/**
	 * @param arg0
	 * @throws BeansException
	 * @uml.property  name="applicationContext"
	 */
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		applicationContext = arg0;
	}

	/*@Override
	public void onStart(Activity activity)
	{
		try 
		{
			if (activity instanceof BpelProcess) 
			{
				Object[] reqMessages = activity.getBpelExecutionContext().getReqMessages();
				if (reqMessages.length > 0)
				{
					if (reqMessages[0] instanceof Document)
					{
						Document doc = (Document) reqMessages[0];
						MDC.put("MES.MSGNAME", JdomUtils.getNodeText(doc, "//Message/Header/MESSAGENAME"));
						MDC.put("MES.TRXID", JdomUtils.getNodeText(doc, "//Message/Header/TRANSACTIONID"));
					}
				}
			}
		} 
		catch (Exception e) 
		{
			log.error("", e);
		} 
	}*/

	/*@Override
	public void onException(Activity activity, Exception arg1) {
		log.error(arg1);
		TxDataSourceManager txDataSourceManager = greenFrameServiceProxy
				.getTxDataSourceManager();

		if (txDataSourceManager.isAutoManaged()) {
			txDataSourceManager.rollbackTransaction();
		}
	}*/

	/*public void rollbackTransaction() {
		super.rollbackTransaction();
	}

	public void beginTransaction() {
		super.beginTransaction();
	}

	public void commitTransaction() {
		super.commitTransaction();
	}*/
	
	/* (non-Javadoc)
	 * @keep log trace at first thread replaced
	 * @see kr.co.aim.greenframe.fos.greenflow.BpelExecutionEventAdaptor#onStart(kr.co.aim.greenflow.core.activity.Activity)
	 */
	@Override
	public void onStart(Activity activity)
	{
		if (activity instanceof BpelProcess) 
		{
			Object[] reqMessages = activity.getBpelExecutionContext().getReqMessages();
			
			if (reqMessages != null && reqMessages.length > 0)
			{
				if (reqMessages[0] instanceof Document)
				{
					Document doc = (Document) reqMessages[0];
					
					//remapping cause of thread independency
					try {
						MDC.put("MES.MSGNAME", JdomUtils.getNodeText(doc, "//Message/Header/MESSAGENAME"));
					} catch (Exception e) {
						//error
					}
					try {
						MDC.put("MES.TRXID", JdomUtils.getNodeText(doc, "//Message/Header/TRANSACTIONID"));
					} catch (Exception e) {
						//error
					}
				}
			}
		}
		
		super.onStart(activity);
	}
}
