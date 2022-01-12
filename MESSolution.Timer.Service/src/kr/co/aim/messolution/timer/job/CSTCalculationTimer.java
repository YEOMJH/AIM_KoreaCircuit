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

public class CSTCalculationTimer implements Job, InitializingBean
{	
	private static Log log = LogFactory.getLog(CSTCalculationTimer.class);
	
	@Override
	public void afterPropertiesSet() throws Exception {
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}
	
	
	public void execute(JobExecutionContext arg0) throws JobExecutionException 
	{	
		try
		{
			monitorCSTCount();
		}
		catch (CustomException e)
		{
			if (log.isDebugEnabled())
				log.error(e.errorDef.getLoc_errorMessage());
		}
	}

	
	public void monitorCSTCount() throws CustomException
	{
		//purpose is to view all Q-time expired in FAB
		StringBuffer sqlBuffer = new StringBuffer()
									.append("WITH FV AS																																																 \n")
									.append("(SELECT V.machineName, SUM(V.totalCapacity) STKTOTALCAPACITY, SUM(V.CSTQTY) EMPTYCSTQTY, '0' FULLCSTQTY   \n")
									.append("        FROM (SELECT Z.machineName, Z.zoneName, Z.totalCapacity, COUNT(D.durableName) CSTQTY              \n")
									.append("                FROM CT_STOCKERZONEINFO Z,                                                                \n")
									.append("                     (SELECT D.factoryName, D.durableName, D.durableType, D.durableSpecName,              \n")
									.append("                               D.durableState, D.durableCleanState, D.transportState,                     \n")
									.append("                               D.machineName, D.positionName, D.zoneName                                  \n")
									.append("                            FROM Durable D                                                                \n")
									.append("                        WHERE D.transportState = 'INSTK'                                                  \n")
									.append("                            AND D.durableType IN ('SheetCST', 'GlassCST')                                 \n")
									.append("                            AND D.machineName IS NOT NULL                                                 \n")
									.append("                            AND D.zoneName IS NOT NULL) D                                                 \n")
									.append("              WHERE Z.machineName = D.machineName(+)                                                      \n")
									.append("                AND Z.zoneName = D.zoneName(+)                                                            \n")
									.append("            GROUP BY Z.machineName, Z.zoneName, Z.totalCapacity) V                                        \n")
									.append("    GROUP BY V.machineName)                                                                               \n")
									.append("SELECT S.factoryName, S.areaName, S.machineName, V.STKTOTALCAPACITY, V.EMPTYCSTQTY, V.FULLCSTQTY          \n")
									.append("    FROM MachineSpec S, FV V                                                                              \n")
									.append("  WHERE S.machineType = 'StorageMachine'                                                                  \n")
									.append("    AND S.machineName = V.machineName                                                                     \n")
									.append("ORDER BY S.factoryName, S.areaName, S.machineName                                                         \n");

		
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
				
				Element eleCount = new Element("STKTOTALCAPACITY");
				eleCount.setText(CommonUtil.getValue(row, "STKTOTALCAPACITY"));
				eleMachine.addContent(eleCount);
				
				Element eleCount2 = new Element("EMPTYCSTQTY");
				eleCount2.setText(CommonUtil.getValue(row, "EMPTYCSTQTY"));
				eleMachine.addContent(eleCount2);
				
				Element eleCount3 = new Element("FULLCSTQTY");
				eleCount3.setText(CommonUtil.getValue(row, "FULLCSTQTY"));
				eleMachine.addContent(eleCount3);
			}
		}
		
		try
		{
			String targetSubject = GenericServiceProxy.getESBServive().getSendSubject("FMB");
			
			Document requestDoc = SMessageUtil.createXmlDocument(eleBody, "PublishSTKCSTCounts",
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
