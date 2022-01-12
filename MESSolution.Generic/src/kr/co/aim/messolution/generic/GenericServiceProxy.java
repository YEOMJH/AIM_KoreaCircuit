package kr.co.aim.messolution.generic;

import java.util.HashMap;

import kr.co.aim.greenframe.esb.GenericSender;
import kr.co.aim.greenframe.infra.EventConfigurator;
import kr.co.aim.greenframe.infra.InfraServiceProxy;
import kr.co.aim.greenframe.transaction.TxDataSourceManager;
import kr.co.aim.greenframe.util.bundle.BundleUtil;
import kr.co.aim.greentrack.generic.orm.SqlMesTemplate;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.esb.ESBService;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.master.ErrorDefMap;
import kr.co.aim.messolution.generic.master.MessageLogger;
import kr.co.aim.messolution.generic.util.EMailInterface;
import kr.co.aim.messolution.generic.util.MailAttachmentGenerator;
import kr.co.aim.messolution.generic.util.MessageHistoryUtil;
import kr.co.aim.messolution.generic.util.SMSInterface;
import kr.co.aim.messolution.generic.util.SpecUtil;
import kr.co.aim.messolution.generic.util.dblog.DBLogWriterManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class GenericServiceProxy extends MESStackTrace implements ApplicationContextAware {
	private static Log log = LogFactory.getLog(GenericServiceProxy.class);
	private static ApplicationContext ac;
	
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		log.debug("Success Get AC");
		ac = arg0;
	}

	public static ApplicationContext getApplicationContext()
	{
		return ac;
	}
	
	public static MailAttachmentGenerator getMailAttachmentGeneratorSerivce()
	{
		return (MailAttachmentGenerator)BundleUtil.getBundleServiceClass(MailAttachmentGenerator.class);
	}
	
	public static EMailInterface getMailSerivce()
	{
		return (EMailInterface)BundleUtil.getBundleServiceClass(EMailInterface.class);
	}
	
	public static SMSInterface getSMSInterface()
	{
		return (SMSInterface)BundleUtil.getBundleServiceClass(SMSInterface.class);
	}

	
	public static TxDataSourceManager getTxDataSourceManager()
	{
		return (TxDataSourceManager)BundleUtil.getBundleServiceClass(TxDataSourceManager.class);
	}

	public static ConstantMap getConstantMap()
	{
		
		return (ConstantMap)BundleUtil.getBundleServiceClass(ConstantMap.class);
	}
	
	public static ErrorDefMap getErrorDefMap()
	{
		
		return (ErrorDefMap)BundleUtil.getBundleServiceClass(ErrorDefMap.class);	
	}
	
	public static GenericSender getGenericSender(String senderName)
	{
		
		if(log.isInfoEnabled()){
			log.info("senderName = " + senderName);
		}
		
		BundleUtil.getBundleServiceClass(InfraServiceProxy.class);
		return (GenericSender) InfraServiceProxy.getBeanService(senderName);
	}
	
	public static ESBService getESBServive()
	{
		
		return (ESBService)BundleUtil.getBundleServiceClass(ESBService.class);	
	}
	
	public static MessageLogger getMessageLogService()
	{
		
		return (MessageLogger) BundleUtil.getBundleServiceClass(MessageLogger.class);	
	}
	
	public static MessageHistoryUtil getMessageTraceService()
	{
		return (MessageHistoryUtil) BundleUtil.getBundleServiceClass(MessageHistoryUtil.class);
	}
	
	public static SpecUtil getSpecUtil()
	{
		return (SpecUtil) BundleUtil.getBundleServiceClass(SpecUtil.class);
	}
	
	public static Object executeMethod(Log eventLogger, String beanName, String methodName, Object... args)
		throws CustomException
	{
		return executeMethodMonitor(eventLogger, beanName, methodName, args);
	}

	public static HashMap<String, String> getSenderByMessage()
	{
		BundleUtil.getBundleServiceClass(InfraServiceProxy.class);

		Object eventConfig = null;
		HashMap<String, String> senderByMessage = new HashMap<String, String>();

		try
		{
			eventConfig = InfraServiceProxy.getBeanService("EventMapConfig");
		}
		catch (Exception e)
		{
			
		}

		if (eventConfig != null)
		{
			senderByMessage = (HashMap<String, String>) ((EventConfigurator) eventConfig).getSenderByMessage();
		}

		return senderByMessage;
	}

	public static HashMap<String, String> getEventClassMap()
	{
		BundleUtil.getBundleServiceClass(InfraServiceProxy.class);
		
		EventConfigurator config = ((EventConfigurator) InfraServiceProxy.getBeanService("EventMapConfig"));
		
		return (HashMap<String, String>) config.getClassMap();
	}

	public static SqlMesTemplate getDcolQueryTemplate()
	{
		return (SqlMesTemplate)BundleUtil.getServiceByBeanName("DColQueryTemplate");
	}
	
	public static QueryTemplate getSqlMesTemplate()
	{
		return (QueryTemplate) ac.getBean("QueryTemplate");
	}
	
	public static DBLogWriterManager getDBLogWriter()
	{
		return (DBLogWriterManager) ac.getBean("DBLogWriterManager");
	}
	
}
