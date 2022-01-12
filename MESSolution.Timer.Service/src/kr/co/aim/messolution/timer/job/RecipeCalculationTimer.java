package kr.co.aim.messolution.timer.job;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ProductQueueTime;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class RecipeCalculationTimer implements Job, InitializingBean {
	private static Log log = LogFactory.getLog(RecipeCalculationTimer.class);

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		try {
			// Monitor
			log.debug("monitorRecipeTimer-Start");
			monitorRecipeTimerByScheduler();
		} catch (CustomException e) {
			if (log.isDebugEnabled())
				log.error(e.errorDef.getLoc_errorMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.debug("monitorRecipeTimer-End");
	}

	private void monitorRecipeTimerByScheduler() throws CustomException
    {
    	
    	List<Map<String,Object>> RecipeDataList = getRecipeList();
    	List<List<Map<String,Object>>> RecipeDataListByMachineName = getListByGroup(RecipeDataList);    	
    	if(RecipeDataListByMachineName ==null || RecipeDataListByMachineName.size()==0)
        {
        	log.info("monitorRecipeListByScheduler: over idel time recipe Information is Empty!!");
        	return ;
        }
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("monitorRecipeTime", "MES", "Recipe time Monitoring service", null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));  	
	   for (List<Map<String, Object>> RecipeDataByMachineName : RecipeDataListByMachineName)
	   {
           boolean isFound = false;
		try
	    	{
        			String message="<pre>=======Recipe IdleTime Over=======</pre>";        			
        			message +="<pre>- EQP ID : " + ConvertUtil.getMapValueByName(RecipeDataByMachineName.get(0),"MACHINENAME") +"</pre>";
        			message +="<pre>=======================================================</pre>";
        			for (Map<String,Object>  RecipeMap : RecipeDataByMachineName)
        			{
        				//String machineName = ConvertUtil.getMapValueByName(RecipeMap2, "MACHINENAME");
        				Recipe recipeData;
		                 recipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] {ConvertUtil.getMapValueByName(RecipeDataByMachineName.get(0),"MACHINENAME"), ConvertUtil.getMapValueByName(RecipeMap, "RECIPENAME")});

		                 String specValue ;
		                 String interval;
		                    if(recipeData.getLastTrackOutTimeKey() == null || recipeData.getLastTrackOutTimeKey().equals(""))
		                     	{	               	    	
		                          	 specValue = Double.toString(recipeData.getDurationUsedLimit());		                     	
		                             interval = Double.toString(ConvertUtil.getDiffTime(TimeUtils.toTimeString(recipeData.getLastApporveTime(), TimeStampUtil.FORMAT_TIMEKEY), TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY)));
		                     	}
		                    else
		                    {
			                	Timestamp value = TimeUtils.getTimestampByTimeKey(recipeData.getLastTrackOutTimeKey());
			                	 specValue = Double.toString(recipeData.getDurationUsed());
			                	 interval = Double.toString(ConvertUtil.getDiffTime(TimeUtils.toTimeString(value, TimeStampUtil.FORMAT_TIMEKEY), TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY)));

		                    }
		                           if (Double.parseDouble(specValue)*60*60 < Double.parseDouble(interval))
		                           {   
		                        	   isFound = true;
		                        	   try
		                        	   {
		                        		message +="<pre>- recipeName : " + recipeData.getRecipeName() + "</pre>";
		                   				message +="<pre>- recipeType : " + recipeData.getRecipeType() + "</pre>";
		                   				message +="<pre>- idleTimeLimit : " + recipeData.getDurationUsed() + "H" +"</pre>";
		                   			    message +="<pre>- lastTrackOutTime : " + recipeData.getLastTrackOutTimeKey() + "</pre>";
		                   			    message +="<pre>- lastApproveTime : " + recipeData.getLastApporveTime() + "</pre>";
		                   				message +="<pre>- autoChangeFlag : " + recipeData.getAutoChangeFlag() + "</pre>";
		                   				message +="<pre>==============================================</pre>";
		                        	   }
		       		                  catch (Exception e)
		  		                       {
		  			                    e.getStackTrace();
		  		                       }
		                           }
		                           else
		                           {log.trace("S11ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss111111111111111111111ssssssssd RMS mail.");
		                        	   continue;
		                           }
//		               			message +="<pre>=======================================================</pre>";
//		            			message +="<pre>- Description	: " + "Recipe IdleTime Over,Need To Try Run,Please! " + "</pre>";
//		    	                log.trace("Start send RMS mail.");	
//		    	                //String machineName = ConvertUtil.getMapValueByName(RecipeMap, "MACHINENAME");
//		                    	CommonUtil.sendAlarmEmail(ConvertUtil.getMapValueByName(RecipeDataByMachineName.get(0),"MACHINENAME"), "RecipeidleTime", message);
//		    	                log.trace("Complete send RMS mail.");
		                     	}
        			message +="<pre>=======================================================</pre>";
        			message +="<pre>- Description	: " + "Recipe IdleTime Over,Need To Try Run,Please! " + "</pre>";
	                log.trace("Start send RMS mail.");	
	                //String machineName = ConvertUtil.getMapValueByName(RecipeMap, "MACHINENAME");
	                if(isFound)
	                {
	                	CommonUtil.sendAlarmEmail(ConvertUtil.getMapValueByName(RecipeDataByMachineName.get(0),"MACHINENAME"), "RecipeidleTime", message);
	                }
	                log.trace("Complete send RMS mail.");
                 }	    	
		catch (Exception ex)
		{
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
		}
	   }
    }
				
	private List<Map<String, Object>> getRecipeList() {
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		List<Map<String, Object>> resultList = null;

		StringBuffer sqlBuffer = new StringBuffer()
				.append("SELECT C.MACHINENAME , C.RECIPENAME , C.RECIPETYPE , C.TIMEUSEDLIMIT, C.DURATIONUSEDLIMIT , C.TOTALDURATIONUSED,      \n")
				.append("C.TOTALTIMEUSED, C.TIMEUSED ,C.LASTAPPROVETIME, C.LASTEVENTCOMMENT, C.LASTEVENTTIMEKEY, C.LASTEVENTUSER,        	   \n")
				.append("C.LASTTRACKOUTTIMEKEY,C.AUTOCHANGEFLAG , C.INTFLAG, C.ENGFLAG, C.RMSFLAG  FROM CT_RECIPE C        				       \n")
				.append(" WHERE 1=1                                                                   		                            		\n")
				.append(" AND C.RECIPETYPE='MAIN'                                                          	                            		\n")
				.append(" AND C.AUTOCHANGEFLAG IN ('ENGINT','INTENG')                                                                         	       		\n")
				.append(" AND C.DURATIONUSEDLIMIT <>'0'                                                                         				\n")
				.append("  AND C.INTFLAG='Y'               		  			   		                                                    		\n")
				.append("  AND C.MFGFLAG='Y'					                                                                    			\n")
		        .append(" AND C.DURATIONUSED <>'0'                                                                                   			\n");
		Object[] bindArray = new Object[0];

		// List<List<Map<String,Object>>> result;

		try {
			resultList = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate()
					.queryForList(sqlBuffer.toString(), bindArray);
		} catch (FrameworkErrorSignal fe) {
			resultList = null;
			log.info("monitorRecipeListByScheduler: over idle time Information is Empty!!");
		}

		return resultList;
	}

	private static List<List<Map<String, Object>>> getListByGroup(List<Map<String, Object>> RecipeDataList) {

		List<List<Map<String, Object>>> result = new ArrayList<List<Map<String, Object>>>();
		Map<String, List<Map<String, Object>>> RecipeDatamap = new TreeMap<String, List<Map<String, Object>>>();

		for (Map<String, Object> RecipeData : RecipeDataList) {
			if (RecipeDatamap.containsKey(ConvertUtil.getMapValueByName(RecipeData, "MACHINENAME"))) 
			{
				List<Map<String, Object>> t = RecipeDatamap.get(ConvertUtil.getMapValueByName(RecipeData, "MACHINENAME"));
				t.add(RecipeData);
				RecipeDatamap.put(ConvertUtil.getMapValueByName(RecipeData, "MACHINENAME"), t);
			}
			else 
			{
				List<Map<String, Object>> t = new ArrayList<Map<String, Object>>();
				t.add(RecipeData);
				RecipeDatamap.put(ConvertUtil.getMapValueByName(RecipeData, "MACHINENAME"), t);
			}
		}
		for (Entry<String, List<Map<String, Object>>> entry : RecipeDatamap.entrySet())
		{
			result.add(entry.getValue());
		}
		return result;
	}

}
