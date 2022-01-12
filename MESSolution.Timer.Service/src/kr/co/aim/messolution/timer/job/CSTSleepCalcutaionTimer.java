package kr.co.aim.messolution.timer.job;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.DirtyInfo;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class CSTSleepCalcutaionTimer implements Job, InitializingBean
{	
	private static Log log = LogFactory.getLog(CSTSleepCalcutaionTimer.class);
	
	@Override
	public void afterPropertiesSet() throws Exception {
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}
	
	
	public void execute(JobExecutionContext arg0) throws JobExecutionException 
	{	
		try
		{
			//Monitor the CST Clean Time
			monitorCSTSleepTime();
		}
		catch (CustomException e)
		{
			if (log.isDebugEnabled())
				log.error(e.errorDef.getLoc_errorMessage());
		}
	}
	
	
	public void monitorCSTSleepTime() throws CustomException
	{
		
		StringBuffer sqlBuffer = new StringBuffer()
								.append("WITH DUEDATEDURABLELIST AS                                                                        	    		\n")								
								.append("(SELECT Q.DURABLENAME,Q.TRANSPORTSTATE,Q.LASTCLEANTIME,Q.DURABLECLEANSTATE, Q.DURATIONUSEDLIMIT, 	    		\n")
								.append("           (Q.LASTCLEANTIME + Q.DURATIONUSEDLIMIT/24) warningTime,          					   				\n")
								.append("           SYSDATE CURRENTTIME                                                                   		 		\n")
								.append("        FROM DURABLE Q                                                           	                    		\n")
								.append("    WHERE 1=1                                                                            	       				\n")
								.append("       AND Q.LASTCLEANTIME IS NOT NULL                                                            				\n")
								.append("        AND Q.DURABLECLEANSTATE NOT IN ('Dirty', 'Repairing')                		  			   				\n")
								.append("        AND Q.DURABLESTATE NOT IN ('Scrapped','NotAvailable')                		  								   				\n")
								.append("        AND Q.DURABLETYPE IN ('SheetCST','OLEDGlassCST','FilmCST','TPGlassCST','EVAMaskCST','TFEMaskCST','MaskCST','Tray','CoverTray')		\n")
								.append(")                                                                                           					\n")
								.append("SELECT R.DURABLENAME,R.TRANSPORTSTATE,R.LASTCLEANTIME,R.DURATIONUSEDLIMIT,R.CURRENTTIME,R.DURATIONUSED 		\n")
								.append(",R.DURABLECLEANSTATE 																				    		\n")
								.append("FROM (                                                                                      					\n")
								.append("        SELECT DISTINCT L.DURABLENAME,L.LASTCLEANTIME,L.DURATIONUSEDLIMIT,L.CURRENTTIME,        				\n")
								.append("               CASE                                                                        					\n")
								.append("               WHEN L.currentTime > L.warningTime THEN 'Warning'                     							\n")
								.append("               ELSE ''                                                                      					\n")
								.append("               END AS DURATIONUSED, L.TRANSPORTSTATE,L.DURABLECLEANSTATE                           			\n")
								.append("            FROM DUEDATEDURABLELIST L                                                           				\n")
								.append("        WHERE 1=1                                                                           					\n")
								.append("    ) R                                                                                     					\n")
							    .append("  WHERE R.DURATIONUSED =  'Warning'	                                                                		\n");
		
		
		Object[] bindArray = new Object[0];
		
		List<ListOrderedMap> result; 
		
		try
		{
			result = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bindArray);
		}
		catch (FrameworkErrorSignal fe)
		{
			result = null;
			throw new CustomException("SYS-9999", fe.getMessage());
			//return;
		}
		
		EventInfo eventInfo = null;
		for (ListOrderedMap row : result)
		{
			if (eventInfo == null)
				//if(!CommonUtil.getValue(row, "DURABLETYPE").equalsIgnoreCase("EVAMask"))
				//	eventInfo = EventInfoUtil.makeEventInfo("Dirty", "MES", "EVAMask Sleep Monitoring service", null, null);
				//else
				
		    eventInfo = EventInfoUtil.makeEventInfo("Dirty", "MES", "CST Sleep Monitoring service", null, null);
			String durableName = CommonUtil.getValue(row, "DURABLENAME");
			String durationUsed = CommonUtil.getValue(row, "DURATIONUSED");
			
			Durable durCSTData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
			doActCSTTime(durCSTData,durationUsed,eventInfo);
		}
	}	
	
	private void doActCSTTime(Durable durCSTData,String durationUsed,EventInfo eventInfo)
		throws CustomException
	{
		try
		{
			//isolation
			GenericServiceProxy.getTxDataSourceManager().beginTransaction();
			{
				if ( StringUtils.equals(durationUsed, GenericServiceProxy.getConstantMap().QTIME_STATE_WARN) && StringUtils.equals(durCSTData.getDurableCleanState(), GenericServiceProxy.getConstantMap().Dur_Clean))
				{
					DirtyInfo dirtyData = MESDurableServiceProxy.getDurableInfoUtil().dirtyInfo(durCSTData, durCSTData.getUdfs().get("MACHINENAME"));
					SetEventInfo setEventInfo = new SetEventInfo();
					
						//Map<String, String> durableUdfs = dirtyData.getUdfs();					
						//durableUdfs.put("DURABLEHOLDSTATE", "Y");					
						//setEventInfo.setUdfs(durableUdfs);
						eventInfo.setReasonCode("HC-OverTime");
						eventInfo.setReasonCodeType("DirtyCST");//caixu 20200818
						MESDurableServiceProxy.getDurableServiceImpl().dirty(durCSTData, dirtyData, eventInfo);
						
				}
					
					//MESDurableServiceProxy.getDurableServiceImpl().setEvent(durCSTData, setEventInfo, eventInfo);
				
			}
			GenericServiceProxy.getTxDataSourceManager().commitTransaction();
		}
		catch (FrameworkErrorSignal fe)
		{
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			log.error(fe);
		}
		catch (Exception ex)
		{
			//safety gear
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			log.error(ex);
		}
		
		//unlock Lot
	}
}
