package kr.co.aim.messolution.timer.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ProductQueueTime;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;


public class ProductQtimeCalculationTimer implements Job, InitializingBean {
	private static Log log = LogFactory.getLog(ProductQtimeCalculationTimer.class);

	@Override
	public void afterPropertiesSet() throws Exception
	{
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}

	public void execute(JobExecutionContext arg0) throws JobExecutionException
	{
		try
		{
			// Monitor
			log.debug("monitorProductQTimeTimer-Start");
		//	monitorProductQTimeTimer();
			monitorProductQtimeByScheduler();
		}
		catch (CustomException e)
		{
			if (log.isDebugEnabled())
				log.error(e.errorDef.getLoc_errorMessage());
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.debug("monitorProductQTimeTimer-End");
	}
	
    private void monitorProductQtimeByScheduler() throws CustomException
    {
    	List<Map<String,Object>> QTimeDataList = getProductQtimeList();
    	
    	
    	List<List<Map<String,Object>>> QTimeDataListByLotName = getListByGroup(QTimeDataList);
    	
    	if(QTimeDataListByLotName ==null || QTimeDataListByLotName.size()==0)
        {
        	log.info("monitorProductQtimeByScheduler: Product QueueTiME Data Information is Empty!!");
        	return ;
        }
    	
    	/*if(QTimeDataList ==null || QTimeDataList.size()==0)
        {
        	log.info("monitorProductQtimeByScheduler: Product QueueTiME Data Information is Empty!!");
        	return ;
        }*/

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("monitorProductQTime", "MES", "Product Qtime Monitoring service", null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		for (List<Map<String,Object>> QTimeDataByLotName : QTimeDataListByLotName)
		{			
			try
			{
				for (Map<String,Object>  QTimeMap : QTimeDataByLotName)
				{
					GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
	
					// in form of millisecond
					Double expired = ConvertUtil.getDiffTime(ConvertUtil.getMapValueByName(QTimeMap, "ENTERTIME"), TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY));
					//// limit unit is hour and decimal form
					expired = (expired / 60 / 60);
	
					if (Double.compare(expired, Double.parseDouble(ConvertUtil.getMapValueByName(QTimeMap, "INTERLOCKDURATIONLIMIT"))) >= 0
							&& (GenericServiceProxy.getConstantMap().QTIME_STATE_WARN.equals(ConvertUtil.getMapValueByName(QTimeMap, "QUEUETIMESTATE")))
								|| GenericServiceProxy.getConstantMap().QTIME_STATE_IN.equals((ConvertUtil.getMapValueByName(QTimeMap, "QUEUETIMESTATE"))))
					{
						lockQTime(eventInfo,QTimeMap);
					}
				}
				
				String message = "<pre>===============AlarmInformation===============</pre>";
				message += "<pre>==============================================</pre>";
				message += "<pre>- EventName	: " + eventInfo.getEventName() + "</pre>";
				message += "<pre>- FactoryName	: " + ConvertUtil.getMapValueByName(QTimeDataByLotName.get(0), "FACTORYNAME") + "</pre>";
				message += "<pre>- LotName	: " + ConvertUtil.getMapValueByName(QTimeDataByLotName.get(0), "LOTNAME") + "</pre>";
				for (Map<String,Object>  QTimeMap : QTimeDataByLotName)
				{
					message += "<pre>- ProductName	: " + ConvertUtil.getMapValueByName(QTimeMap, "PRODUCTLIST") + "</pre>";
					message += "<pre>- RemainingTime	: " + ConvertUtil.getMapValueByName(QTimeMap, "DIFF") + " hour" + "</pre>";
				}
				message += "<pre>- FlowName	: " + ConvertUtil.getMapValueByName(QTimeDataByLotName.get(0), "PROCESSFLOWNAME") + "</pre>";
				message += "<pre>- OperationName	: " + ConvertUtil.getMapValueByName(QTimeDataByLotName.get(0), "PROCESSOPERATIONNAME") + "</pre>";
				message += "<pre>- ToFactoryName	: " + ConvertUtil.getMapValueByName(QTimeDataByLotName.get(0), "TOFACTORYNAME") + "</pre>";
				message += "<pre>- ToFlowName	: " + ConvertUtil.getMapValueByName(QTimeDataByLotName.get(0), "TOPROCESSFLOWNAME") + "</pre>";
				message += "<pre>- ToOperationName	: " + ConvertUtil.getMapValueByName(QTimeDataByLotName.get(0), "TOPROCESSOPERATIONNAME") + "</pre>";
				message += "<pre>==============================================</pre>";
	
				CommonUtil.sendAlarmEmailForFactory(ConvertUtil.getMapValueByName(QTimeDataByLotName.get(0), "TOFACTORYNAME"), "Q-TIME", message);
				//houxk 20210527
				sendToEm(ConvertUtil.getMapValueByName(QTimeDataByLotName.get(0), "FACTORYNAME"), message);
	
				GenericServiceProxy.getTxDataSourceManager().commitTransaction();
	
			}
			catch (Exception ex)
			{
				GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
	
				log.warn(String.format("Q-Time[%s %s %s] monitoring failed",ConvertUtil.getMapValueByName(QTimeDataByLotName.get(0), "PRODUCTLIST"), 
						                          							ConvertUtil.getMapValueByName(QTimeDataByLotName.get(0), "PROCESSOPERATIONNAME"),
						                          							ConvertUtil.getMapValueByName(QTimeDataByLotName.get(0), "TOPROCESSOPERATIONNAME")));
			}
		}
		
		/*for (Map<String,Object>  QTimeMap : QTimeDataList)
		{
			try
			{
				GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);

				// in form of millisecond
				Double expired = ConvertUtil.getDiffTime(ConvertUtil.getMapValueByName(QTimeMap, "ENTERTIME"), TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY));
				//// limit unit is hour and decimal form
				expired = (expired / 60 / 60);

				if (Double.compare(expired, Double.parseDouble(ConvertUtil.getMapValueByName(QTimeMap, "INTERLOCKDURATIONLIMIT"))) >= 0
						&& (GenericServiceProxy.getConstantMap().QTIME_STATE_WARN.equals(ConvertUtil.getMapValueByName(QTimeMap, "QUEUETIMESTATE")))
							|| GenericServiceProxy.getConstantMap().QTIME_STATE_IN.equals((ConvertUtil.getMapValueByName(QTimeMap, "QUEUETIMESTATE"))))
				{
					lockQTime(eventInfo,QTimeMap);
					
					String message = "<pre>===============AlarmInformation===============</pre>";
						message += "<pre>==============================================</pre>";
						message += "<pre>- EventName	: " + eventInfo.getEventName() + "</pre>";
						message += "<pre>- FactoryName	: " + ConvertUtil.getMapValueByName(QTimeMap, "FACTORYNAME") + "</pre>";
						message += "<pre>- ProductName	: " + ConvertUtil.getMapValueByName(QTimeMap, "PRODUCTLIST") + "</pre>";
						message += "<pre>- FlowName	: " + ConvertUtil.getMapValueByName(QTimeMap, "PROCESSFLOWNAME") + "</pre>";
						message += "<pre>- OperationName	: " + ConvertUtil.getMapValueByName(QTimeMap, "PROCESSOPERATIONNAME") + "</pre>";
						message += "<pre>- ToFactoryName	: " + ConvertUtil.getMapValueByName(QTimeMap, "TOFACTORYNAME") + "</pre>";
						message += "<pre>- ToFlowName	: " + ConvertUtil.getMapValueByName(QTimeMap, "TOPROCESSFLOWNAME") + "</pre>";
						message += "<pre>- ToOperationName	: " + ConvertUtil.getMapValueByName(QTimeMap, "TOPROCESSOPERATIONNAME") + "</pre>";
						message += "<pre>==============================================</pre>";
					
					// CommonUtil.sendAlarmEmail("Q001", "Q-TIME", message);
					CommonUtil.sendAlarmEmailForFactory(ConvertUtil.getMapValueByName(QTimeMap, "TOFACTORYNAME"), "Q-TIME", message);
				}

				GenericServiceProxy.getTxDataSourceManager().commitTransaction();

			}
			catch (Exception ex)
			{
				GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();

				log.warn(String.format("Q-Time[%s %s %s] monitoring failed",ConvertUtil.getMapValueByName(QTimeMap, "PRODUCTLIST"), 
						                          							ConvertUtil.getMapValueByName(QTimeMap, "PROCESSOPERATIONNAME"),
						                          							ConvertUtil.getMapValueByName(QTimeMap, "TOPROCESSOPERATIONNAME")));
			}
		}*/
    	
    	
    }
    
    private List<Map<String,Object>> getProductQtimeList()
    {
    	ConstantMap constantMap = GenericServiceProxy.getConstantMap();
    	List<Map<String,Object>> resultList = null;
    	
    	String sql = " SELECT C.FACTORYNAME , C.PROCESSFLOWNAME , C.PROCESSOPERATIONNAME ,C.QUEUETIMESTATE ,"
    			+ "           C.TOFACTORYNAME, C.TOPROCESSFLOWNAME,C.TOPROCESSOPERATIONNAME ,C.INTERLOCKDURATIONLIMIT,"
    			+ "           C.ENTERTIME, P.LOTNAME , ROUND((C.INTERLOCKDURATIONLIMIT - (SYSDATE - C.ENTERTIME)*24),2) AS DIFF,"
    			+ "           LISTAGG(C.PRODUCTNAME,',') WITHIN GROUP(ORDER BY C.PRODUCTNAME ASC) AS PRODUCTLIST"
    			+ " FROM CT_PRODUCTQUEUETIME C , PRODUCT P ,LOT L "
    			+ " WHERE 1=1  "
    			+ " AND C.FACTORYNAME = P.FACTORYNAME"
    			+ " AND C.PRODUCTNAME = P.PRODUCTNAME "
    			+ " AND P.LOTNAME = L.LOTNAME"
    			+ " AND L.LOTPROCESSSTATE = ? "
    			+ " AND C.QUEUETIMESTATE <> ? "
    			+ " AND C.QUEUETIMESTATE = ? "
    			//+ " AND C.LOTNAME = 'L2E8823C4AA' "  for test
    			+ " AND P.PRODUCTSTATE = ? "
    			+ " GROUP BY C.FACTORYNAME , C.PROCESSFLOWNAME , C.PROCESSOPERATIONNAME ,C.QUEUETIMESTATE ,"
    			+ " C.TOFACTORYNAME, C.TOPROCESSFLOWNAME,C.TOPROCESSOPERATIONNAME ,C.INTERLOCKDURATIONLIMIT,C.ENTERTIME,P.LOTNAME ";
		try
		{

			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new Object[] { constantMap.Lot_Wait,
					 																									 constantMap.QTIME_STATE_CONFIRM, 
					 																									 constantMap.QTIME_STATE_OVER, 
					 																									 constantMap.Prod_InProduction });

		}
		catch (Exception ex)
		{
			log.info(String.format("getProductQtimeList: ProductQtime Data Information is Empty!! [SQL:%s]", sql));
		}
		
		
    	return resultList;
    }
    
	private void lockQTime(EventInfo sourceEventInfo, Map<String,Object> QtimeMap) throws CustomException
	{
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Interlock", sourceEventInfo.getEventUser(), sourceEventInfo.getEventComment(),sourceEventInfo.getEventTime());
        String[] productList = org.springframework.util.StringUtils.commaDelimitedListToStringArray(ConvertUtil.getMapValueByName(QtimeMap, "PRODUCTLIST"));
		
		String condition = " 1=1 AND FACTORYNAME = ? AND PROCESSFLOWNAME =? "
						 + " AND PROCESSOPERATIONNAME =? AND TOFACTORYNAME =? AND  TOPROCESSFLOWNAME =?  "
						 + " AND TOPROCESSOPERATIONNAME = ? " + CommonUtil.makeInString("PRODUCTNAME", productList, false);
		
		Object[] bindSet = new Object [] {ConvertUtil.getMapValueByName(QtimeMap, "FACTORYNAME"),
										  ConvertUtil.getMapValueByName(QtimeMap, "PROCESSFLOWNAME"),
										  ConvertUtil.getMapValueByName(QtimeMap, "PROCESSOPERATIONNAME"),
										  ConvertUtil.getMapValueByName(QtimeMap, "TOFACTORYNAME"),
										  ConvertUtil.getMapValueByName(QtimeMap, "TOPROCESSFLOWNAME"),
										  ConvertUtil.getMapValueByName(QtimeMap, "TOPROCESSOPERATIONNAME")};
		
		List<ProductQueueTime> resultList = null;
				
		try
		{
			resultList = ExtendedObjectProxy.getProductQTimeService().select(condition, bindSet);
		}
		catch (greenFrameDBErrorSignal ne)
		{
			// ignore error to consider as not found exception signal
			throw new CustomException("LOT-0201",ConvertUtil.getMapValueByName(QtimeMap, "PRODUCTLIST"),  
												 ConvertUtil.getMapValueByName(QtimeMap, "PROCESSOPERATIONNAME"),
												 ConvertUtil.getMapValueByName(QtimeMap, "TOPROCESSOPERATIONNAME"));
		}
		
		for (ProductQueueTime QtimeData : resultList)
		{
			QtimeData.setInterlockTime(eventInfo.getEventTime());
			QtimeData.setQueueTimeState(GenericServiceProxy.getConstantMap().QTIME_STATE_OVER);

			ExtendedObjectProxy.getProductQTimeService().modify(eventInfo, QtimeData);
		}

	}
    
	private void monitorProductQTimeTimer() throws CustomException
	{
		List<ProductQueueTime> QTimeDataList = findProductQTime();

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("monitorProductQTime", "MES", "Product Qtime Monitoring service", null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		for (ProductQueueTime QTimeData : QTimeDataList)
		{
			try
			{
				GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);

				// in form of millisecond
				Double expired = ConvertUtil.getDiffTime(TimeUtils.toTimeString(QTimeData.getEnterTime(), TimeStampUtil.FORMAT_TIMEKEY), TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY));
				//// limit unit is hour and decimal form
				expired = (expired / 60 / 60);

				if (Double.compare(expired, Double.parseDouble(QTimeData.getInterlockDurationLimit())) >= 0
						&& (QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_WARN)
								|| QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_IN)))
				{
					lockQTime(eventInfo, QTimeData.getProductName(), QTimeData.getFactoryName(), QTimeData.getProcessFlowName(), QTimeData.getProcessOperationName(), QTimeData.getToFactoryName(),
							QTimeData.getToProcessFlowName(), QTimeData.getToProcessOperationName());
					
					String message = "<pre>===============AlarmInformation===============</pre>";
			  	  	  	  message += "<pre>==============================================</pre>";
			  	  	  	  message += "<pre>- EventName	: " + eventInfo.getEventName() + "</pre>";
			  	  	  	  message += "<pre>- FactoryName	: " + QTimeData.getFactoryName() + "</pre>";
			  	  	  	  message += "<pre>- ProductName	: " + QTimeData.getProductName() + "</pre>";
			  	  	  	  message += "<pre>- FlowName	: " + QTimeData.getProcessFlowName() + "</pre>";
			  	  	  	  message += "<pre>- OperationName	: " + QTimeData.getProcessOperationName() + "</pre>";
			  	  	  	  message += "<pre>- ToFactoryName	: " + QTimeData.getToFactoryName() + "</pre>";
			  	  	  	  message += "<pre>- ToFlowName	: " + QTimeData.getToProcessFlowName() + "</pre>";
			  	  	  	  message += "<pre>- ToOperationName	: " + QTimeData.getToProcessOperationName() + "</pre>";
			  	  	  	  message += "<pre>==============================================</pre>";
					
					CommonUtil.sendAlarmEmail("Q001", "Q-TIME", message);
				}

				GenericServiceProxy.getTxDataSourceManager().commitTransaction();

			}
			catch (Exception ex)
			{
				GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();

				log.warn(String.format("Q-Time[%s %s %s] monitoring failed", QTimeData.getProductName(), QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName()));
			}
		}
	}

	private List<ProductQueueTime> findProductQTime() throws CustomException
	{
		List<ProductQueueTime> resultList;

		try
		{
			resultList = ExtendedObjectProxy.getProductQTimeService().select("productName in (select productName from product where PRODUCTSTATE = 'InProduction') AND queueTimeState <> ? ",
					new Object[] { GenericServiceProxy.getConstantMap().QTIME_STATE_CONFIRM });
		}
		catch (Exception ex)
		{
			resultList = new ArrayList<ProductQueueTime>();
		}

		return resultList;
	}
	
	private void lockQTime(EventInfo eventInfo, String productName, String factoryName, String processFlowName, String processOperationName, String toFactoryName, String toProcessFlowName,
			String toProcessOperationName) throws CustomException
	{
		try
		{
			eventInfo.setEventName("Interlock");

			ProductQueueTime QtimeData = ExtendedObjectProxy.getProductQTimeService().selectByKey(true,
					new Object[] { productName, factoryName, processFlowName, processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName });
			QtimeData.setInterlockTime(eventInfo.getEventTime());
			QtimeData.setQueueTimeState(GenericServiceProxy.getConstantMap().QTIME_STATE_OVER);

			ExtendedObjectProxy.getProductQTimeService().modify(eventInfo, QtimeData);
		}
		catch (greenFrameDBErrorSignal ne)
		{
			// ignore error to consider as not found exception signal
			throw new CustomException("LOT-0201", productName, processOperationName, toProcessOperationName);
		}
	}
	
	private static List<List<Map<String,Object>>> getListByGroup(List<Map<String,Object>> QTimeDataList) 
	{
        
		List<List<Map<String,Object>>> result = new ArrayList<List<Map<String,Object>>>();
        Map<String, List<Map<String,Object>>> QTimeDatamap = new TreeMap<String, List<Map<String,Object>>>();
 
        for (Map<String,Object> QTimeData : QTimeDataList) 
        {
            if (QTimeDatamap.containsKey(ConvertUtil.getMapValueByName(QTimeData, "LOTNAME")))
            {
                List<Map<String,Object>> t = QTimeDatamap.get(ConvertUtil.getMapValueByName(QTimeData, "LOTNAME"));
                t.add(QTimeData);
                QTimeDatamap.put(ConvertUtil.getMapValueByName(QTimeData, "LOTNAME"), t);
            } 
            else
            {
            	List<Map<String,Object>> t = new ArrayList<Map<String,Object>>();
                t.add(QTimeData);
                QTimeDatamap.put(ConvertUtil.getMapValueByName(QTimeData, "LOTNAME"), t);
            }
        }
        for (Entry<String, List<Map<String,Object>>> entry : QTimeDatamap.entrySet()) 
        {
            result.add(entry.getValue());
        }
        return result;
    }
	
	public void sendToEm(String factoryName, String message)
	{
		String userList[] = getUserList(factoryName,"OverProductQTime");	
		if(userList == null || userList.length ==0) return;
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ProductQtimeCalculationTimer", "MES", "", "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		try
		{	
			log.info("OverProductQTimeAlarm Start Send To Emobile & Wechat");	
						
			String title = "QTime超时提醒";
			String detailtitle = "${}CIM系统消息通知";
			String url = "";
									
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userList, title, detailtitle, message, url);
									
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userList, title, detailtitle, message, "");
		}
		catch (Exception e)
		{
			log.info("eMobile or WeChat Send Error : " + e.getCause());	
		} 
	}
	//AlarmGroup = OverProductQTime && factoryName
	private String[] getUserList(String factoryName, String alarmGroupName)
	{
		String sql = " SELECT DISTINCT  UP.USERID "
				   + " FROM CT_ALARMGROUP AG , CT_ALARMUSERGROUP AU  , USERPROFILE UP"
				   + " WHERE AG.ALARMGROUPNAME = AU.ALARMGROUPNAME "
				   + " AND AU.USERID = UP.USERID "
				   + " AND UP.USERID IS NOT NULL "
				   + " AND AU.ALARMGROUPNAME = :ALARMGROUPNAME"
				   + " AND AU.USERLEVEL = :USERLEVEL";
		
		List<Map<String, Object>> resultList = null;
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new String[] { alarmGroupName, factoryName });
		}
		catch (Exception ex)
		{
			log.info(ex.getCause());
		}

		if(resultList ==null || resultList.size()==0) return null;
		
		return CommonUtil.makeListBySqlResult(resultList, "USERID").toArray(new String[] {});
	}
}
