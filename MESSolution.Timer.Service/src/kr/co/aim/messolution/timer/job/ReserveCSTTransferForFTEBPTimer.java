package kr.co.aim.messolution.timer.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

public class ReserveCSTTransferForFTEBPTimer implements Job, InitializingBean{
	private static Log log = LogFactory.getLog(ReserveCSTTransferForFTEBPTimer.class);
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		try
		{
			log.info("Reserve CST Transfer For FTEBP Timer Start");
			//Get EQP List
			StringBuffer sql1 = new StringBuffer();
			sql1.append(" SELECT * ");
			sql1.append(" FROM MACHINESPEC ");
			sql1.append(" WHERE MACHINENAME LIKE ? ");
			sql1.append(" AND MACHINENAME NOT IN ('3MFTE15') ");
			sql1.append(" ORDER BY MACHINENAME ASC ");
			
			Object[] bindArray1 = new Object[]{"3MFTE%"};	
			
			List<Map<String, Object>> result1 = GenericServiceProxy.getSqlMesTemplate().queryForList(sql1.toString(), bindArray1);
			
			if(result1 !=null && result1.size()>0)
			{
				for(Map<String, Object> machine : result1)
				{
					if(!checkCSTInfo(machine.get("MACHINENAME").toString(),"BP03"))
					{
						ReserveCSTTransferForTFEBP(machine.get("MACHINENAME").toString(),"BP03");
					}
				}
			}	
			log.info("Reserve CST Transfer For FTEBP Timer End");
		}
		catch (CustomException e)
		{
			if (log.isDebugEnabled())
				log.error(e.errorDef.getLoc_errorMessage());
		}
	}
	
	public boolean checkCSTInfo(String machineName, String portName)
	{
		//check exist CST or not 
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT *  ");
		sql.append(" FROM DURABLE  ");
		sql.append(" WHERE MACHINENAME =? ");
		sql.append(" AND PORTNAME=? ");

		Object[] bindArray = new Object[]{machineName,portName};
		
		List<ListOrderedMap> result = null;

		try
		{
			result = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindArray);
		}
		catch (FrameworkErrorSignal fe)
		{
			log.info("DB Error,Return");
			return true;
		}
		if(result!=null&&result.size()>0)
		{
			log.info("Already Exist CST On "+machineName+","+portName+",Return");
			return true;
		}			
		else 
		{
			log.info("There Is No CST On "+machineName+","+portName+",Continue");
			//check exist transport job or not 
			StringBuffer sql2 = new StringBuffer();
			sql2.append(" SELECT * FROM CT_TRANSPORTJOBCOMMAND ");
			sql2.append(" WHERE DESTINATIONMACHINENAME =? ");
			sql2.append(" AND  DESTINATIONPOSITIONNAME =? ");
			sql2.append(" AND JOBSTATE IN('Requested','Accepted','Started') ");
			sql2.append(" AND (CANCELSTATE IS NULL OR CANCELSTATE NOT IN ('Completed')) ");

			Object[] bindArray2 = new Object[]{machineName,portName};
			
			List<ListOrderedMap> result2 = null;

			try
			{
				result2 = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql2.toString(), bindArray2);
			}
			catch (FrameworkErrorSignal fe)
			{
				log.info("DB Error,Return");
				return true;
			}		
			if(result2!=null&&result2.size()>0)
			{
				log.info("Already Exist TransportJob To "+machineName+","+portName+",Return");
				return true;
			}		
			else
			{
				log.info("There Is No TransportJob To "+machineName+","+portName+",Continue");
				return false;
			}
				
		}		
	}
	
	public void ReserveCSTTransferForTFEBP(String machineName, String portName) throws CustomException
	{
		StringBuffer sqlBufferTP = new StringBuffer()
		                         .append(" SELECT D.DURABLENAME,C.DURABLESPECNAME,D.MACHINENAME,D.PORTNAME,D.ZONENAME,D.POSITIONTYPE,D.POSITIONNAME,D.DURABLETYPE,D.DURABLECLEANSTATE      \n")
		                         .append(" FROM DURABLE D, DURABLESPEC C                                                                                                                   \n")
		                         .append(" WHERE D.DURABLESPECNAME=C.DURABLESPECNAME  AND (D.TRANSPORTLOCKFLAG != 'Y' OR D.TRANSPORTLOCKFLAG IS NULL)                                      \n")
		                         .append(" AND D.DURABLESTATE='Available' AND D.DURABLECLEANSTATE='Clean'  AND C.DURABLETYPE IN('EVAMaskCST','MaskCST')                                    \n")
		                         .append(" AND (D.DURABLEHOLDSTATE IS NULL OR D.DURABLEHOLDSTATE <> 'Y')                                                                                   \n")
		                         .append(" AND D.MACHINENAME IN ('3MCTK01','3MCTK02')                                                                                                      \n")
		                         .append(" ORDER BY dbms_random.value()                                                                                                                    \n");
		                         
		Object[] bindArray= new Object[0];
		List<ListOrderedMap>result;
		try
		{
			result = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBufferTP.toString(), bindArray);
		}
		catch (FrameworkErrorSignal fe)
		{
			log.info("DB Error,Return");
			throw new CustomException("", fe.getMessage());
		}
		
		if(result!=null && result.size()>0)
		{									
			Map<String, Object> row= result.get(0);
			EventInfo  eventInfo = EventInfoUtil.makeEventInfo("CSTTransfer", "MES", "Reserve CST Transfer service", null, null);
			String durableName =CommonUtil.getValue(row, "DURABLENAME");
			String durableSpec=CommonUtil.getValue(row, "DURABLESPECNAME");
			String sourceMachine=CommonUtil.getValue(row, "MACHINENAME");
			String sourceZone=null;
			String sourcePositionType=null;
			String sourcePositionName=null;
			if(!CommonUtil.getValue(row, "ZONENAME").isEmpty())
			{
			 sourceZone=result.get(0).get("ZONENAME").toString();
			}
			if(!CommonUtil.getValue(row, "POSITIONTYPE").isEmpty())
			{
			 sourcePositionType=result.get(0).get("POSITIONTYPE").toString();
			}
	        if(!CommonUtil.getValue(row, "PORTNAME").isEmpty())
	        {
	         sourcePositionName=result.get(0).get("PORTNAME").toString();
	        }
	        if(!CommonUtil.getValue(row, "POSITIONNAME").isEmpty())
	        {
	         sourcePositionName=result.get(0).get("POSITIONNAME").toString();
	        }
			String durableType=result.get(0).get("DURABLETYPE").toString();
			String durableCleanState=result.get(0).get("DURABLECLEANSTATE").toString();
			//getCarrierDataRequest(durableName);
			doCSTTransfer(durableName,durableSpec,sourceMachine,sourceZone,sourcePositionType,sourcePositionName,durableType,durableCleanState,machineName,portName);
		}
		else
		{
			log.info("There Is No Available CST,Return");
		}
	}
	
	 private void doCSTTransfer(String durableName,String durableSpec,String sourceMachine, String sourceZone,String sourcePositionType,String sourcePositionName,String durableType,String durableCleanState, String machineName, String portName )throws CustomException
	 {
	  try
		{
		  log.info("Start Reserve CST:"+durableName+" To "+machineName+","+portName);
		  
		  GenericServiceProxy.getTxDataSourceManager().beginTransaction();
		  {
			String transportJobName =null;
			String destinationMachine="";
			String destinationZone=null;
			String destinationType="PORT";
			String destinationPositionName="";
			String lotName=null;
			String ProductQuantity=null;
			String durableState="EMPTY";
			String priority="80";
			String carrierType=durableType;
			String carrierCleanState=durableCleanState;
						
		    destinationMachine = machineName;
			destinationPositionName = portName;
							       
			
			Element bodyElement = new Element("Body");
			Element transportJobElement = new Element("TRANSPORTJOBNAME");
			transportJobElement.setText(transportJobName);
			bodyElement.addContent(transportJobElement);	
			
			Element carrierNameElement = new Element("CARRIERNAME");
			carrierNameElement.setText(durableName);
			bodyElement.addContent(carrierNameElement);	
			
			Element sourceNameElement = new Element("SOURCEMACHINENAME");
			sourceNameElement.setText(sourceMachine);
			bodyElement.addContent(sourceNameElement);	
			

			Element sourceZoneElement = new Element("SOURCEZONENAME");
			sourceZoneElement.setText(sourceZone);
			bodyElement.addContent(sourceZoneElement);	
			
			Element sourcePostionElement = new Element("SOURCEPOSITIONTYPE");
			sourcePostionElement.setText(sourcePositionType);
			bodyElement.addContent(sourcePostionElement);	
			
			Element sourcePostionNameElement = new Element("SOURCEPOSITIONNAME");
			sourcePostionNameElement.setText(sourcePositionName);
			bodyElement.addContent(sourcePostionNameElement);
			
			Element destinationMachineNameElement = new Element("DESTINATIONMACHINENAME");
			destinationMachineNameElement.setText(destinationMachine);
			bodyElement.addContent(destinationMachineNameElement);

			Element destinationZoneElement = new Element("DESTINATIONZONENAME");
			destinationZoneElement.setText(destinationZone);
			bodyElement.addContent(destinationZoneElement);
			
			Element destinationPositionTypeElement = new Element("DESTINATIONPOSITIONTYPE");
			destinationPositionTypeElement.setText(destinationType);
			bodyElement.addContent(destinationPositionTypeElement);
			
			Element destinationPositionNameElement = new Element("DESTINATIONPOSITIONNAME");
			destinationPositionNameElement.setText(destinationPositionName);
			bodyElement.addContent(destinationPositionNameElement);									
			
			Element priorityElement = new Element("PRIORITY");
			priorityElement.setText(priority);
			bodyElement.addContent(priorityElement);
			
			Element carrierStateElement = new Element("CARRIERSTATE");
			carrierStateElement.setText(durableState);
			bodyElement.addContent(carrierStateElement);
			
			Element carrierTypeElement = new Element("CARRIERTYPE");
			carrierTypeElement.setText(carrierType);
			bodyElement.addContent(carrierTypeElement);
			
			Element cleanStateElement = new Element("CLEANSTATE");
			cleanStateElement.setText(carrierCleanState);
			bodyElement.addContent(cleanStateElement);
			
			Element lotNameElement = new Element("LOTNAME");
			lotNameElement.setText(lotName);
			bodyElement.addContent(lotNameElement);
			
			Element productQuantityElement = new Element("PRODUCTQUANTITY");
			productQuantityElement.setText(ProductQuantity);
			bodyElement.addContent(productQuantityElement);	
			
			String targetSubject = GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("TEMsvr");
			Document Doc = SMessageUtil.createXmlDocument(bodyElement, "RequestTransportJobRequest",
					GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("TEMsvr"),
					targetSubject,
					"MES",
					"Reserve CST Transfer For FTEBP Timer");
			SMessageUtil.setItemValue(Doc, "Header", "LANGUAGE","English");
			GenericServiceProxy.getESBServive().sendBySender(targetSubject, Doc, "TEMSender");
		  }
			GenericServiceProxy.getTxDataSourceManager().commitTransaction();
			
			log.info("Complete Reserve CST:"+durableName+" To "+machineName+","+portName);
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
