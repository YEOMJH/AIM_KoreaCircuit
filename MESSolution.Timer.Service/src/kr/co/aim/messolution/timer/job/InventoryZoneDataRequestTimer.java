package kr.co.aim.messolution.timer.job;

import java.util.List;
import org.apache.commons.collections.OrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;

public class InventoryZoneDataRequestTimer implements Job, InitializingBean
{	
	private static Log log = LogFactory.getLog(InventoryZoneDataRequestTimer.class);
	
	@Override
	public void afterPropertiesSet() throws Exception {
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}
	
	
	@Override
	@SuppressWarnings("unchecked")
	public void execute(JobExecutionContext arg0) throws JobExecutionException 
	{	
		try
		{
			// Search StockerZoneInfo
			String sql = "SELECT DISTINCT MACHINENAME "
					   + "FROM CT_STOCKERZONEINFO "
					   + "ORDER BY MACHINENAME ";
			
			List<OrderedMap> resultDataList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, new Object[] {});
			if (resultDataList == null || resultDataList.size() == 0)
			{
				log.info("CT_STOCKERZONEINFO data is not exists");
				return ;
			}
			
			for (OrderedMap resultData : resultDataList) 
			{
				String machineName = resultData.get("MACHINENAME").toString();
				
				this.sendMessage(machineName);
				
				Thread.sleep(1);
			}
		}
		catch (Exception e)
		{
			if (log.isDebugEnabled())
				log.info(e.getMessage());
		}
	}

	private void sendMessage(String machineName) throws Exception
	{
		Element bodyElement = new Element("Body");
		
		Element machineNameElement = new Element("MACHINENAME");
		machineNameElement.setText(machineName);
		bodyElement.addContent(machineNameElement);
		
		Document doc = SMessageUtil.createXmlDocument(bodyElement, "GetInventoryZoneDataRequest",
				"",
				GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("TEMsvr"),
				"TimeScheduler",
				"InventoryZoneDataRequest Scheduler");
		
		GenericServiceProxy.getESBServive().sendBySender(doc, "TEMSender");
	}
}
