package kr.co.aim.greenframe.template.workflow;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

import javax.xml.namespace.QName;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenflow.core.BpelActivityListener;
import kr.co.aim.greenflow.core.BpelExecutionContext;
import kr.co.aim.greenflow.core.BpelExecutionContextAware;
import kr.co.aim.greenflow.core.BpelProcessManager;
import kr.co.aim.greenflow.core.activity.BpelProcess;
import kr.co.aim.greenframe.esb.tibco.DQListener;
import kr.co.aim.greenframe.event.BundleMessageEventAdaptor;
import kr.co.aim.greenframe.fos.greenflow.BpelExecutionEventAdaptor;
import kr.co.aim.greenframe.fos.greenflow.BpelExecutorService;
import kr.co.aim.greenframe.util.bundle.BundleUtil;
import kr.co.aim.greenframe.util.msg.MessageUtil;
import kr.co.aim.greenframe.util.support.InvokeUtils;
import kr.co.aim.greentrack.generic.GenericServiceProxy;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.jdom.Document;
import org.jdom.Element;
import org.osgi.framework.BundleContext;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.osgi.context.BundleContextAware;


// BpelExecutionContextAware Interface
public class BpelExecuter extends BpelExecutorService implements BundleContextAware, BpelExecutionContextAware, CommandProvider
{
	static Log log = LogFactory.getLog(BpelExecuter.class);
	
	/**
	 * @uml.property  name="context"
	 * @uml.associationEnd  
	 */
	private	ApplicationContext	context;
	
	// BpelExecutionContext save ThreadLocal add
	/**
	 * @uml.property  name="bpelExecutionContext"
	 * @uml.associationEnd  multiplicity="(0 -1)" elementType="kr.co.aim.greenflow.core.BpelExecutionContext"
	 */
	private ThreadLocal<BpelExecutionContext> bpelExecutionContext = new ThreadLocal<BpelExecutionContext>();
	
	//business process handler repository
	private HashMap<String, Object> classInstanceMap = new HashMap<String, Object>();
	
	//2021-11-03
	public Element WWT( String tempWorkWeight, String tempWorkTask ){
		
		if(tempWorkWeight == null || tempWorkWeight.isEmpty()){
			log.error("WorkWeight is Empty");
			return null;
		}
		
		if(tempWorkTask == null || tempWorkTask.isEmpty()){
			log.error("WorkTask is Empty");
			return null;
		}
		
		int WorkWeight = Integer.valueOf(tempWorkWeight);
		int WorkTask = Integer.valueOf(tempWorkTask);
		
		Object[] Services = BundleUtil.getServices(DQListener.class.getName());
		
		log.info(">>> ---------------------------------------- ");
		String selfServerName = System.getProperty("Seq");
		log.info("My ServerName = " + selfServerName);
		
		if(Services == null || Services.length == 0){
			log.warn("DQListener is Not");
		}
		
		Element WWTLISTELEMENT = new Element("WWTLIST");
		
		for(Object Service : Services){
			DQListener dqListener = 
					(DQListener)Service;
			
			String beanName = this.getBeanNameFromInstant(dqListener.getApplicationContext(), dqListener);
			log.info("BeanName = " + beanName + ", Class Name = " + dqListener.getClass().getName());
			
			Element WWTELEMENT = new Element("WWT");
			Element WWELEMENT = new Element("WW");
			Element WTELEMENT = new Element("WT");
			
			log.info("old Work Weight = " + String.valueOf(dqListener.getWorkerWeight()));
			dqListener.setWorkerWeight(WorkWeight);
			log.info("New Work Weight = " + String.valueOf(dqListener.getWorkerWeight()));
			
			log.info("old Work Task = " + String.valueOf(dqListener.getWorkerTasks()));
			dqListener.setWorkerTasks(WorkTask);
			log.info("New Work Task = " + String.valueOf(dqListener.getWorkerTasks()));
			
			WWELEMENT.setText(String.valueOf(dqListener.getWorkerWeight()));
			WTELEMENT.setText(String.valueOf(dqListener.getWorkerTasks()));
			
			WWTELEMENT.addContent(WWELEMENT);
			WWTELEMENT.addContent(WTELEMENT);
			
			WWTLISTELEMENT.addContent(WWTELEMENT);
		}
		
		return WWTLISTELEMENT;
	}
	
	//2021-11-03
	public String getBeanNameFromInstant(ApplicationContext ac, DQListener dq){
		
		try{
			String[] beanNames = ac.getBeanNamesForType(dq.getClass());
			
			for(String beanName : beanNames){
				Object obj = ac.getBean(beanName);
				
				if(dq.equals(obj)){
					return beanName;
				}
			}
		}catch(Exception e){
			return "";
		}
		
		return "";
	}
	
	/*
	//2021-11-03
	public void _WWTS( CommandInterpreter ci ){
		
		
        Object[] Services = BundleUtil.getServices(DQListener.class.getName());
		
		log.info(">>> ---------------------------------------- ");
		String selfServerName = System.getProperty("Seq");
		log.info("My ServerName = " + selfServerName);
		
		if(Services == null || Services.length == 0){
			log.warn("DQListener is Not");
			return;
		}
		
		for(Object Service : Services){
			
			DQListener dqListener = 
					(DQListener)Service;
			
			String beanName = this.getBeanNameFromInstant(dqListener.getApplicationContext(), dqListener);
			log.info("BeanName = " + beanName + ", Class Name = " + dqListener.getClass().getName());
			System.out.println("BeanName = " + beanName + ", Class Name = " + dqListener.getClass().getName());
			
			log.info("Work Weight = " + String.valueOf(dqListener.getWorkerWeight()));
			System.out.println("Work Weight = " + String.valueOf(dqListener.getWorkerWeight()));

			log.info("Work Task = " + String.valueOf(dqListener.getWorkerTasks()));
			System.out.println("Work Task = " + String.valueOf(dqListener.getWorkerTasks()));
		}
	}*/
	
	//2021-11-03
	public void _WW( CommandInterpreter ci ){
		
		String tempWorkWeight = ci.nextArgument();
		
		if(tempWorkWeight == null || tempWorkWeight.isEmpty()){
			log.error("WorkWeight is Empty");
			return;
		}
		
		int WorkWeight = Integer.valueOf(tempWorkWeight);

		Object[] Services = BundleUtil.getServices(DQListener.class.getName());
		
		log.info(">>> ---------------------------------------- ");
		ci.println(">>> ---------------------------------------- ");
		String selfServerName = System.getProperty("Seq");
		log.info("My ServerName = " + selfServerName);
		ci.println("My ServerName = " + selfServerName);
		if(Services == null || Services.length == 0){
			log.warn("DQListener is Not");
			ci.println("DQListener is Not");
		}
		
		for(Object Service : Services){
			
			DQListener dqListener = 
					(DQListener)Service;
			
			String beanName = this.getBeanNameFromInstant(dqListener.getApplicationContext(), dqListener);
			log.info("BeanName = " + beanName + ", Class Name = " + dqListener.getClass().getName());
			ci.println("BeanName = " + beanName + ", Class Name = " + dqListener.getClass().getName());
			
			log.info("old Work Weight = " + String.valueOf(dqListener.getWorkerWeight()));
			ci.println("old Work Weight = " + String.valueOf(dqListener.getWorkerWeight()));
			dqListener.setWorkerWeight(WorkWeight);
			log.info("New Work Weight = " + String.valueOf(dqListener.getWorkerWeight()));
			ci.println("New Work Weight = " + String.valueOf(dqListener.getWorkerWeight()));
			
			//setTibcoWW(selfServerName,beanName,String.valueOf(dqListener.getWorkerWeight()),String.valueOf(dqListener.getWorkerTasks()));
		}
	}
	
	//2021-11-03
	public void _WT( CommandInterpreter ci ){
		
		String tempWorkTask = ci.nextArgument();
		
		if(tempWorkTask == null || tempWorkTask.isEmpty()){
			log.error("WorkTask is Empty");
			return;
		}
		
		int WorkTask = Integer.valueOf(tempWorkTask);

		Object[] Services = BundleUtil.getServices(DQListener.class.getName());

		log.info(">>> ---------------------------------------- ");
		ci.println(">>> ---------------------------------------- ");
		String selfServerName = System.getProperty("Seq");
		log.info("My ServerName = " + selfServerName);
		ci.println("My ServerName = " + selfServerName);
		
		if(Services == null || Services.length == 0){
			log.warn("DQListener is Not");
			ci.println("DQListener is Not");
		}
		
		for(Object Service : Services){
			
			DQListener dqListener = 
					(DQListener)Service;
			
			String beanName = this.getBeanNameFromInstant(dqListener.getApplicationContext(), dqListener);
			log.info("BeanName = " + beanName + ", Class Name = " + dqListener.getClass().getName());
			ci.println("BeanName = " + beanName + ", Class Name = " + dqListener.getClass().getName());
			
			log.info("old Work Task = " + String.valueOf(dqListener.getWorkerTasks()));
			ci.println("old Work Task = " + String.valueOf(dqListener.getWorkerTasks()));
			dqListener.setWorkerTasks(WorkTask);
			log.info("New Work Task = " + String.valueOf(dqListener.getWorkerTasks()));
			ci.println("New Work Task = " + String.valueOf(dqListener.getWorkerTasks()));
			
		}
	}
	
	private static Object[] dqServices = null;
	public static Object[] getDQServices()
	{
		if(dqServices == null)
			dqServices = BundleUtil.getServices(DQListener.class.getName());
		return dqServices;
	}
	
	/*
	//2021-11-03
	public void _WWT( CommandInterpreter ci ){
		
		String tempWorkWeight = ci.nextArgument();
		
		if(tempWorkWeight == null || tempWorkWeight.isEmpty()){
			log.error("WorkWeight is Empty");
			return;
		}
		
		String tempWorkTask = ci.nextArgument();
		
		if(tempWorkTask == null || tempWorkTask.isEmpty()){
			log.error("WorkTask is Empty");
			return;
		}
		
		int WorkWeight = Integer.valueOf(tempWorkWeight);
		int WorkTask = Integer.valueOf(tempWorkTask);
		
		Object[] Services = BundleUtil.getServices(DQListener.class.getName());
		
		log.info(">>> ---------------------------------------- ");
		String selfServerName = System.getProperty("Seq");
		log.info("My ServerName = " + selfServerName);
		
		if(Services == null || Services.length == 0){
			log.warn("DQListener is Not");
		}
		
		for(Object Service : Services){
			
			DQListener dqListener = 
					(DQListener)Service;
			
			String beanName = this.getBeanNameFromInstant(dqListener.getApplicationContext(), dqListener);
			log.info("BeanName = " + beanName + ", Class Name = " + dqListener.getClass().getName());
			
			log.info("old Work Weight = " + String.valueOf(dqListener.getWorkerWeight()));
			dqListener.setWorkerWeight(WorkWeight);
			log.info("New Work Weight = " + String.valueOf(dqListener.getWorkerWeight()));
			
			log.info("old Work Task = " + String.valueOf(dqListener.getWorkerTasks()));
			dqListener.setWorkerTasks(WorkTask);
			log.info("New Work Task = " + String.valueOf(dqListener.getWorkerTasks()));
				
			setTibcoWW(selfServerName,beanName,String.valueOf(dqListener.getWorkerWeight()),String.valueOf(dqListener.getWorkerTasks()));
		}
	}
	
	public void setTibcoWW(String server,String service,String workWeght,String workTask)
	{
		String sql1 = " SELECT * FROM CT_TIBWW WHERE SERVER = ? AND SERVICE = ?  ";
		String[] bindSet1 = new String[]{ server,service };
		
		List<ListOrderedMap> resultList1 = greenFrameServiceProxy.getSqlTemplate().queryForList(sql1, bindSet1);
		if(resultList1 !=null && resultList1.size() >0)
		{
			String sql2 = " UPDATE CT_TIBWW SET WORKWEIGHT = ?,WORKTASK = ?,SCHEDULEWEIGHT = ?,LASTEVENTTIME =SYSDATE,LASTEVENTTIMEKEY=? WHERE SERVER = ? AND SERVICE = ?  ";
			String[] bindSet2 = new String[]{ workWeght,workTask,System.getProperty("sw"),TimeStampUtil.getCurrentEventTimeKey(),server,service };
			
			greenFrameServiceProxy.getSqlTemplate().update(sql2, bindSet2);
		}
		else 
		{
			String sql3 = " INSERT INTO CT_TIBWW VALUES (?, ?, ?, ?, ?, SYSDATE, ?) ";
			String[] bindSet3 = new String[]{server,service,workWeght,workTask,System.getProperty("sw"),TimeStampUtil.getCurrentEventTimeKey()};
			
			greenFrameServiceProxy.getSqlTemplate().update(sql3, bindSet3);
		}
	}*/
	
	public void setBundleContext(BundleContext bundleContext) 
	{
		Hashtable properties = new Hashtable();
		bundleContext.registerService(CommandProvider.class.getName(), this, properties);
	}
	
	public void terminateImmediately()
	{
		log.info("***************************************************************************************");
		System.exit(0);
	}
	
	public void terminateAfterManagement(String serverName){
		
		String selfServerName = System.getProperty("Seq");
		log.info("***************************************************************************************");
		log.info("Receive ServerName = " + serverName);
		log.info("My ServerName = " + selfServerName);
		
		if(serverName.equals(selfServerName)){
		
			log.info("[1/2] Closeing Transport .....");
			BundleMessageEventAdaptor bundleMessageEventAdaptor = (BundleMessageEventAdaptor)BundleUtil.getBundleServiceClass(BundleMessageEventAdaptor.class);
			bundleMessageEventAdaptor.terminate();
			log.info("[2/2] Closeing Transport .....");
			
			log.info("[1/2] Checking alive bpel or running bpel .....");
			BpelProcessManager BpelProcessManager = (BpelProcessManager)BundleUtil.getBundleServiceClass(BpelProcessManager.class);
			boolean isrunningBpel = false;
			int bpelCheckCount = 2;
			
			String nameCheck = serverName.substring(0, 6);
			
			if(nameCheck.toUpperCase().equals("MSGSVR")){
				bpelCheckCount = 3; 
			}
			
			for (int i=0; i<3; i++)
			{
				log.info("Bpel Size = " + BpelProcessManager.getRunningBpelProcessSize());
				while (BpelProcessManager.getRunningBpelProcessSize() > bpelCheckCount)
				{
					if (!isrunningBpel) {
						log.info("Waiting for completing running bpel .....");
						isrunningBpel = true;
					}
					try {
						Thread.sleep(500);
					} catch (Exception e) {}
				}
				
				try {
					Thread.sleep(330);
				} catch (Exception e) {}
			}
			
			if (isrunningBpel){
				log.info("All running bpel are completed .....");
			}
			
			log.info("[2/2] Checking alive bpel or running bpel .....");
			log.info("***************************************************************************************");
			log.info("********************           Terminated Good bye           **************************");
			log.info("***************************************************************************************");
			System.exit(0);
		}else{
			log.warn("Receive ServerName is not same my Name");
		}
	}
	
	public void setApplicationContext(ApplicationContext ctx) throws BeansException
	{
		context = ctx;
	}
	
	public void setBpelExecutionContext(BpelExecutionContext bpelExecutionContext )
	{
		this.bpelExecutionContext.set( bpelExecutionContext );
	}
	public BpelExecutionContext getBpelExecutionContext()
	{
		return this.bpelExecutionContext.get();
	}
	
	public void executeWFListener(Document document)
	{
		Object[] arguments = new Object[]{document};		
        this.executeWF(arguments, false, SMessageUtil.getListenerBpelName(document));
	}
	
	public void executeWF(Document document)
	{
		Object[] arguments = new Object[]{document};		
        this.executeWF(arguments, false, SMessageUtil.getBpelName(document));
	}
	
	public void executeWF(Document document, String bpelName)
	{
		Object[] arguments = new Object[]{document};		
        this.executeWF(arguments, false, bpelName);
	}
	
	public void SyncExcuteWF(Document document, String bpelName)
	{
		Object[] arguments = new Object[]{document};		
        this.executeWF(arguments, false, bpelName);
	}
	
	public void executeProcess(Object[] arguments, boolean parallel, String bpelName)
	{
		BpelProcess process = null;
		try {
			if (bpelName == null) { 
				for (int i=0; i<arguments.length; i++)
				{
					if (arguments[i] instanceof Document)
					{
						String bpel = MessageUtil.getBpelName((Document)arguments[i]);
						if (bpel != null && bpel.length() > 0) {
							if (!bpel.toLowerCase().endsWith(".bpel"))
									bpelName = bpel + ".bpel";
							else
								bpelName = bpel;
							break;
						}
					}
				}
				if (bpelName == null)
					bpelName = this.getBpelRepository().getRootBpelName();
			}
			
			
			QName name = new QName(DEFAULT_TARGETNAMESPACE, bpelName);
			process = newBpelProcess(name);
			try {
				BpelActivityListener listener = (BpelActivityListener)context.getBean("Modeler");
				process.addBpelActivityListener(listener);
			} catch (Exception ex) {
				//log.error(ex);
				process.addBpelActivityListener((BpelExecutionEventAdaptor)context.getBean("BpelExecutionEventAdaptor"));
			}
			
		
			process.execute(arguments, parallel);

		} catch(Exception ex){
			log.error("Could not execute " + bpelName, ex);
			((BpelExecutionEventAdaptor)context.getBean("BpelExecutionEventAdaptor")).onException(process, ex);
		}
	}
	
	public void executeWF(Object[] arguments, boolean parallelMode, String bpelName)
	{
		BpelProcess process = null;
		
		try
		{
			if (bpelName == null) { 
				for (int i=0; i<arguments.length; i++)
				{
					if (arguments[i] instanceof Document)
					{
						String bpel = MessageUtil.getBpelName((Document)arguments[i]);
						if (bpel != null && bpel.length() > 0) {
							if (!bpel.toLowerCase().endsWith(".bpel"))
									bpelName = bpel + ".bpel";
							else
								bpelName = bpel;
							break;
						}
					}
				}
				if (bpelName == null)
					bpelName = this.getBpelRepository().getRootBpelName();
			}
			QName name = new QName(DEFAULT_TARGETNAMESPACE, bpelName);
			
			//no need to gain BPEL context on engine
			try
			{
				process = newBpelProcess(name);
				
				try
				{
					BpelActivityListener listener = (BpelActivityListener)context.getBean("Modeler");
					process.addBpelActivityListener(listener);
				}
				catch (Exception ex)
				{
					//log.error(ex);
				}

				process.setParent( getBpelExecutionContext().getRunningActivity());
				process.setSuperProcess( getBpelExecutionContext().getBpelProcess());
				process.addBpelActivityListener( getBpelExecutionContext().getRunningActivity());
				
				//open arch transaction
				//GenericServiceProxy.getTxDataSourceManager().beginTransaction();
				{
					//BPEL has internal exception handling
					process.execute(arguments, parallelMode, false, getBpelExecutionContext().getBpelProcess().getOriginatorBpelProcess());
				}
			}
			catch (Exception ex)
			{//only considering that BPEL not found
				if (log.isDebugEnabled())
					log.debug(String.format("Could not find [%s], then do class invocation", bpelName));
				
				log.info("===============================================================================================");
				log.info(new StringBuffer("***** EVENT STARTED :: ").append(bpelName).append("@").append(getBpelExecutionContext().getBpelProcess().getId()).toString());
				log.info("===============================================================================================");

				if (System.getProperty("svrType") != null && System.getProperty("svrType").toString().equals("M"))
				{
					//message delivery service
//					invokeDispatchService(arguments);
					invokeDispatchService(bpelName, arguments);
				}
				else
				{
					//direct event loading
					invokeEventService(bpelName, arguments); 
				}

				long elapse = calculateElapsed();
				
				log.info("===============================================================================================");
				log.info("***** EVENT COMPLETED :: " + summaryData(log.isInfoEnabled(), true, bpelName, elapse));
				log.info("===============================================================================================");
				
				//record result on independent thread
				//if (true && arguments.length > 0 && arguments[0] instanceof Document)
				// 	kr.co.aim.messolution.generic.GenericServiceProxy.getMessageTraceService().recordEventResult((Document) arguments[0], elapse, null);
				if (true && arguments.length > 0 && arguments[0] instanceof Document)
					kr.co.aim.messolution.generic.GenericServiceProxy.getMessageTraceService().recordTranscationLog((Document) arguments[0], elapse, false);
				
				log.info("pararell end");
			}
			//catch (Exception ex)
			//{
			//	log.error("Could not execute " + bpelName, ex);
			//	((BpelExecutionEventAdaptor)context.getBean("BpelExecutionEventAdaptor")).onException(process, ex);
			//}
		}
		catch (Exception ex)
		{
			//rollback on BPEL exception
			//GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			
			//log.error("Could not execute " + bpelName, ex);
			//((BpelExecutionEventAdaptor)context.getBean("BpelExecutionEventAdaptor")).onException(process, ex);
			
			//for uncontrollable error not expected reply
			//log.error(ex);
			
			long elapse = calculateElapsed();
			
			log.info("===============================================================================================");
			log.info("***** EVENT COMPLETED :: " + summaryData(log.isInfoEnabled(), false, bpelName, elapse));
			log.info("===============================================================================================");
			
			//record result on independent thread
			//if (true && arguments.length > 0 && arguments[0] instanceof Document)
			//	kr.co.aim.messolution.generic.GenericServiceProxy.getMessageTraceService().recordEventResult((Document) arguments[0], elapse, ex);
			if (true && arguments.length > 0 && arguments[0] instanceof Document)
				kr.co.aim.messolution.generic.GenericServiceProxy.getMessageTraceService().recordTranscationLog((Document) arguments[0], elapse, false);
			
			//cut transaction manager
			GenericServiceProxy.getTxDataSourceManager().rollbackAllTransactions();
			
			log.info("pararell end");
		}
		finally
		{
			//commit message request
			//GenericServiceProxy.getTxDataSourceManager().commitTransaction();
			
			//doAlarmRequest();
			
			//executeAction();
			
			this.bpelExecutionContext.remove();
		}
	}
	
	public void invokeEventService(String eventServiceName, Object[] argumemts) throws Exception
	{
		//parsing event name
		if (eventServiceName.contains(".bpel"))
			eventServiceName = eventServiceName.replaceFirst(".bpel", "");
		
		try 
		{
			//Object classNameObject = kr.co.aim.messolution.generic.GenericServiceProxy.getConstantMap().getClassDefsMap().get(eventServiceName);
			//141126 by swcho : improved stability
			Object classNameObject = kr.co.aim.messolution.generic.GenericServiceProxy.getEventClassMap().get(eventServiceName);
			
			String className = "";
			
			if (classNameObject != null)
				className = classNameObject.toString();
			
			Object instance = classInstanceMap.get(eventServiceName);
			
			//prepared to GC
			//150309 by swcho : improved class binding
			if (instance == null)
			{
				log.warn(String.format("class [%s] is required to load", eventServiceName));
				
				Object loadObject = null;
				
				try
				{
					loadObject = InvokeUtils.newInstance(className, null, null);
					
					if (loadObject == null)
						throw new Exception(String.format("class [%s] not found", className));
				}
				catch (Exception ex)
				{
					log.error(ex);
					
					log.warn(String.format("try to reload class [%s]", className));
					//reload
					loadObject = InvokeUtils.newInstance(className, null, null);
				}
				finally
				{
					classInstanceMap.put(eventServiceName, loadObject);
				}
			}
			
			//if (greenFrameServiceProxy.getTxDataSourceManager().isAutoManaged())
			//{
			//	greenFrameServiceProxy.getTxDataSourceManager().beginTransaction();
			//	
			//	//setApplicationInfo(getProcessName(), eventServiceName);
			//}
			
			if (argumemts.length > 0 && argumemts[0] instanceof Document)
			{
				InvokeUtils.invokeMethod(classInstanceMap.get(eventServiceName), "execute", new Object[] {(Document) argumemts[0]});
			}
			else
			{
				//log.error(String.format("Class[%s] is not defined", className));
				throw new Exception(String.format("Class[%s] is not defined", className));
			}
							
			//if (greenFrameServiceProxy.getTxDataSourceManager().isAutoManaged())
			//{
			//	//setApplicationInfo(getProcessName() , "");
			//	greenFrameServiceProxy.getTxDataSourceManager().commitTransaction();
			//}
		}
		catch (InvocationTargetException e)
		{
			Throwable targetEx = e.getTargetException();
			if (targetEx instanceof CustomException)
			{
				CustomException customException = (CustomException) e.getTargetException();

				if ("UndefinedCode".equals(customException.errorDef.getErrorCode()))
				{
					if (customException.errorDef.getLoc_errorMessage().contains("RuntimeException"))
					{
						this.printTraceLog("RuntimeException", customException.getStackTrace());
					}
					else
					{
						this.printTraceLog("UndefinedCode", customException.getStackTrace());
					}
					
				}
				else
				{
					this.printTraceLog("CustomException", customException.getStackTrace());
				}
			}
			else
			{
				this.printTraceLog("InvocationTargetException", targetEx.getStackTrace());
			}

			throw (Exception) e.getCause();
		}
		catch (Exception e)
		{
			// setApplicationInfo(getProcessName(), "");
			// error in logic in form of standardized exceptiopn
			log.error(e);

			throw (Exception) e.getCause();
		}
		
		//log.info(JdomUtils.toString(replyDoc));
	}
	
	public void printTraceLog(String errorType,StackTraceElement[] traceElements )
	{
		StringBuilder error = new StringBuilder();
		for(StackTraceElement element :  traceElements)
		{
			if(element.getClassName().contains("kr.co.aim.messolution")&& !element.getClassName().contains("eventHandler.SyncHandler"))
				error.append(element.toString() + System.lineSeparator());
		}
		log.error(String.format("%s: %s",errorType,StringUtil.removeEnd(error.toString(), System.lineSeparator())));
	}

	public void invokeDispatchService(String eventServiceName, Object[] argumemts) throws Exception
	{
		try
		{
			// parsing event name
			if (StringUtils.contains(eventServiceName, ".bpel"))
			{
				eventServiceName = eventServiceName.replaceFirst(".bpel", "");
			}

			String senderName = "";
			Object senderNameObject = kr.co.aim.messolution.generic.GenericServiceProxy.getSenderByMessage().get(eventServiceName);

			if (senderNameObject != null)
			{
				senderName = senderNameObject.toString();

				if (StringUtils.isNotEmpty(senderName))
				{
					SMessageUtil.addItemToHeader((Document) argumemts[0], "SENDER", senderName);
				}
			}

			invokeDispatchService(argumemts);
		}
		catch (Exception e)
		{
			log.error(e);

			throw (Exception) e.getCause();
		}
	}

	public void invokeDispatchService(Object[] argumemts) throws Exception
	{
		try 
		{
			String eventServiceName = "dispatching";
			String className = "kr.co.aim.messolution.generic.event.Dispatch";
			
			Object instance = classInstanceMap.get(eventServiceName);
			
			//prepared to GC
			if (instance == null)
			{
				log.warn(String.format("class [%s] is required to load", eventServiceName));
				
				Object loadObject = null;
				
				try
				{
					loadObject = InvokeUtils.newInstance(className, null, null);
					
					if (loadObject == null)
						throw new Exception(String.format("class [%s] not found", className));
				}
				catch (Exception ex)
				{
					log.error(ex);
					
					log.warn(String.format("try to reload class [%s]", className));
					//reload
					loadObject = InvokeUtils.newInstance(className, null, null);
				}
				finally
				{
					classInstanceMap.put(eventServiceName, loadObject);
				}
			}
			
			if (argumemts.length > 0 && argumemts[0] instanceof Document)
			{
				InvokeUtils.invokeMethod(classInstanceMap.get(eventServiceName), "execute", new Object[] {(Document) argumemts[0]});
			}
			else
			{
				//log.error(String.format("Class[%s] is not defined", className));
				throw new Exception(String.format("Class[%s] is not defined", className));
			}
		}
		catch (Exception e)
		{
			log.error(e);
			
			throw (Exception) e.getCause();
		}
	}
	
	private String summaryData(boolean isDebug, boolean result, String eventName, long time)
	{
		StringBuffer stringbuffer = new StringBuffer();
		if (isDebug && result)
		{
			stringbuffer.append(eventName).append("@")
						.append(getBpelExecutionContext().getBpelProcess().getId()).append(" <SUCCESS> ");
			stringbuffer.append(" Elapsed Time = ").append(time).append(" miliseconds");
		}
		else if (isDebug)
		{
			stringbuffer.append(eventName).append("@")
						.append(getBpelExecutionContext().getBpelProcess().getId()).append(" <FAIL> ");
			stringbuffer.append(" Elapsed Time = ").append(time).append(" miliseconds");
		}
		
		return stringbuffer.toString();
	}
	
	private long calculateElapsed()
	{
		return new Date(System.currentTimeMillis()).getTime()
				- getBpelExecutionContext().getBpelProcess().getStartTime().getTime();
	}
	
	//2021-11-03
	public String getHelp() 
	{
		return "---MES Process Management---" + System.getProperty( "line.separator" ) +
				"\tshutdownMES - Shutdown MES Process(" + System.getProperty("Seq") + ")" + System.getProperty( "line.separator" ) +
				"\tstopBpelAutoRead - Stop Auto Read ability(" + System.getProperty("Seq") + ")" + System.getProperty( "line.separator" ) +
				"\tstartBpelAutoRead - Start Auto Read ability(" + System.getProperty("Seq") + ")" + System.getProperty( "line.separator" ) +
				"\tWW - Work Weight Change ability(" + System.getProperty("Seq") + "), EX) WW 10" + System.getProperty( "line.separator" ) +
				"\tWT - Work Task Change ability(" + System.getProperty("Seq") + "), EX) WT 5" + System.getProperty( "line.separator" ) +
				"\tWWT - Work Weight and Task Change ability(" + System.getProperty("Seq") + "), EX) WWT 10 5" + System.getProperty( "line.separator" ) +
				"\tWWTS - Work Weight and Task Status(" + System.getProperty("Seq") + "), EX) WWTS" + System.getProperty( "line.separator" ) +
				"\treload (All or specific service)";
	}
}