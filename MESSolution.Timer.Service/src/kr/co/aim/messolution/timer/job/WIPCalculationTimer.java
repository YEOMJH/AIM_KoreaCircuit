package kr.co.aim.messolution.timer.job;

import java.util.List;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;

public class WIPCalculationTimer implements Job, InitializingBean
{	
	private static Log log = LogFactory.getLog(WIPCalculationTimer.class);
	
	@Override
	public void afterPropertiesSet() throws Exception {
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}
	
	
	public void execute(JobExecutionContext arg0) throws JobExecutionException 
	{	
		try
		{
			monitorWIPCount();
		}
		catch (CustomException e)
		{
			if (log.isDebugEnabled())
				log.error(e.errorDef.getLoc_errorMessage());
		}
	}

	
	public void monitorWIPCount() throws CustomException
	{
		//purpose is to view all Q-time expired in FAB
		StringBuffer sqlBuffer = new StringBuffer()
									.append("SELECT S.factoryName, S.areaName, S.machineName, NVL(SUM(R.productQuantity),0) RUNNINGCOUNT  \n")
									.append("    FROM MachineSpec S, Machine M,                                                    \n")
									.append("         (SELECT L.lotName, L.machineName, COUNT(P.productName) productQuantity       \n")
									.append("                FROM Lot L, Product P                                                 \n")
									.append("            WHERE L.lotName = P.lotName                                               \n")
									.append("                AND L.lotState = 'Released'                                           \n")
									.append("                AND P.productState = 'InProduction'                                   \n")
									.append("                AND L.machineName IS NOT NULL                                         \n")
									.append("            GROUP BY L.lotName, L.machineName) R                                      \n")
									.append("WHERE S.factoryName = M.factoryName                                                   \n")
									.append("    AND S.machineName = M.machineName                                                 \n")
									.append("    AND S.machineType = 'ProductionMachine'                                           \n")
									.append("    AND S.detailMachineType = 'MAIN'                                                  \n")
									.append("    AND S.machineName = R.machineName(+)                                              \n")
									.append("GROUP BY S.factoryName, S.areaName, S.machineName                                     \n")
									.append("ORDER BY S.factoryName, S.areaName, S.machineName                                     \n");

		
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
		
		this.publish(result);
	}
	
	
	private void publish(List<ListOrderedMap> machineList)
		throws CustomException
	{
		//generate hold request message
		Element eleBody = new Element(SMessageUtil.Body_Tag);
		{
			Element eleMachineList = new Element("DATALIST");
			eleBody.addContent(eleMachineList);
			
			for (ListOrderedMap row : machineList)
			{
				Element eleMachine = new Element("DATA");
				eleMachineList.addContent(eleMachine);
				
				Element eleMachineName = new Element("MACHINENAME");
				eleMachineName.setText(CommonUtil.getValue(row, "MACHINENAME"));
				eleMachine.addContent(eleMachineName);
				
				Element eleCount = new Element("RUNNINGCOUNT");
				eleCount.setText(CommonUtil.getValue(row, "RUNNINGCOUNT"));
				eleMachine.addContent(eleCount);
			}
		}
		
		try
		{
			String targetSubject = GenericServiceProxy.getESBServive().getSendSubject("FMB");
			
			Document requestDoc = SMessageUtil.createXmlDocument(eleBody, "PublishWIPCounts",
					"",//SMessageUtil.getHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", false),
					targetSubject,
					"MES",
					"Regular WIP counter sent");
	
			GenericServiceProxy.getESBServive().sendBySender(targetSubject, requestDoc, "FMBSender");
		}
		catch (Exception ex)
		{
			log.error(ex);
		}
	}
}
