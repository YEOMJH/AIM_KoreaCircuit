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

public class ReserveCSTTransferTimer implements Job, InitializingBean  {

	private static Log log = LogFactory.getLog(ReserveCSTTransferTimer.class);
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		try
		{
			//Monitor the CST Clean Time
			ReserveCSTTransferArray();
			ReserveCSTTransferTP();
			ReserveCSTTransferOLEDCN();
			ReserveCSTTransferOLEDCF();
		}
		catch (CustomException e)
		{
			if (log.isDebugEnabled())
				log.error(e.errorDef.getLoc_errorMessage());
		}
		
	}
	public void ReserveCSTTransferArray() throws CustomException
	{
		StringBuffer sqlBufferArray = new StringBuffer()
		                         .append(" SELECT C.DURABLENAME,C.DURABLESPECNAME,D.MACHINENAME,D.PORTNAME,D.ZONENAME,D.POSITIONTYPE,D.POSITIONNAME,D.DURABLETYPE,D.DURABLECLEANSTATE      \n")
		                         .append(" FROM DURABLE D, CT_DURABLERESERVETRANSFER C                                                                                                     \n")
		                         .append(" WHERE D.DURABLENAME=C.DURABLENAME AND  (TRANSPORTLOCKFLAG != 'Y' OR TRANSPORTLOCKFLAG IS NULL)                                                                                 \n")
		                         .append(" AND C.DURABLERESERVESTATE='Reserved' AND D.DURABLESTATE='Available' AND D.MACHINENAME IS NOT NULL                                               \n")
		                         .append(" AND D.MACHINENAME!='3ACC01'  AND D.DURABLESPECNAME='AN'                                                                                         \n")
		                         .append(" ORDER BY dbms_random.value()                                                                                                                    \n");
		                         
		Object[] bindArray= new Object[0];
		List<ListOrderedMap>result;
		try
		{
			result = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBufferArray.toString(), bindArray);
		}
		catch (FrameworkErrorSignal fe)
		{
			result = null;
			throw new CustomException("", fe.getMessage());
			//return;
		}
		
		//EventInfo eventInfo = null;
		if(result.size()>0&&!result.isEmpty())
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
			doCSTTransfer(durableName,durableSpec,sourceMachine,sourceZone,sourcePositionType,sourcePositionName,durableType,durableCleanState);
		}
	}
	public void ReserveCSTTransferTP() throws CustomException
	{
		StringBuffer sqlBufferTP = new StringBuffer()
		                         .append(" SELECT C.DURABLENAME,C.DURABLESPECNAME,D.MACHINENAME,D.PORTNAME,D.ZONENAME,D.POSITIONTYPE,D.POSITIONNAME,D.DURABLETYPE,D.DURABLECLEANSTATE      \n")
		                         .append(" FROM DURABLE D, CT_DURABLERESERVETRANSFER C                                                                                                     \n")
		                         .append(" WHERE D.DURABLENAME=C.DURABLENAME AND (TRANSPORTLOCKFLAG != 'Y' OR TRANSPORTLOCKFLAG IS NULL)                                                                              \n")
		                         .append(" AND C.DURABLERESERVESTATE='Reserved' AND D.DURABLESTATE='Available' AND D.MACHINENAME IS NOT NULL                                               \n")
		                         .append(" AND D.MACHINENAME!='3TCC01'  AND D.DURABLESPECNAME='TN'                                                                                         \n")
		                         .append(" ORDER BY dbms_random.value()                                                                                                                    \n");
		                         
		Object[] bindArray= new Object[0];
		List<ListOrderedMap>result;
		try
		{
			result = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBufferTP.toString(), bindArray);
		}
		catch (FrameworkErrorSignal fe)
		{
			result = null;
			throw new CustomException("", fe.getMessage());
			//return;
		}
		
		//EventInfo eventInfo = null;
		if(result.size()>0&&!result.isEmpty())
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
			doCSTTransfer(durableName,durableSpec,sourceMachine,sourceZone,sourcePositionType,sourcePositionName,durableType,durableCleanState);
		}
	}
	public void ReserveCSTTransferOLEDCN() throws CustomException
	{
		StringBuffer sqlBufferTP = new StringBuffer()
		                         .append(" SELECT C.DURABLENAME,C.DURABLESPECNAME,D.MACHINENAME,D.PORTNAME,D.ZONENAME,D.POSITIONTYPE,D.POSITIONNAME,D.DURABLETYPE,D.DURABLECLEANSTATE      \n")
		                         .append(" FROM DURABLE D, CT_DURABLERESERVETRANSFER C                                                                                                     \n")
		                         .append(" WHERE D.DURABLENAME=C.DURABLENAME  AND (TRANSPORTLOCKFLAG != 'Y' OR TRANSPORTLOCKFLAG IS NULL)                                                                                \n")
		                         .append(" AND C.DURABLERESERVESTATE='Reserved' AND D.DURABLESTATE='Available' AND D.MACHINENAME IS NOT NULL                                               \n")
		                         .append(" AND D.MACHINENAME!='3CEW01'  AND D.DURABLESPECNAME='CN'                                                                                         \n")
		                         .append(" ORDER BY dbms_random.value()                                                                                                                    \n");
		                         
		Object[] bindArray= new Object[0];
		List<ListOrderedMap>result;
		try
		{
			result = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBufferTP.toString(), bindArray);
		}
		catch (FrameworkErrorSignal fe)
		{
			result = null;
			throw new CustomException("", fe.getMessage());
			//return;
		}
		
		//EventInfo eventInfo = null;
		if(result.size()>0&&!result.isEmpty())
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
			doCSTTransfer(durableName,durableSpec,sourceMachine,sourceZone,sourcePositionType,sourcePositionName,durableType,durableCleanState);
		}
	}
	public void ReserveCSTTransferOLEDCF() throws CustomException
	{
		StringBuffer sqlBufferTP = new StringBuffer()
		                         .append(" SELECT C.DURABLENAME,C.DURABLESPECNAME,D.MACHINENAME,D.PORTNAME,D.ZONENAME,D.POSITIONTYPE,D.POSITIONNAME,D.DURABLETYPE,D.DURABLECLEANSTATE      \n")
		                         .append(" FROM DURABLE D, CT_DURABLERESERVETRANSFER C                                                                                                     \n")
		                         .append(" WHERE D.DURABLENAME=C.DURABLENAME  AND (TRANSPORTLOCKFLAG != 'Y' OR TRANSPORTLOCKFLAG IS NULL)                                                                                \n")
		                         .append(" AND C.DURABLERESERVESTATE='Reserved' AND D.DURABLESTATE='Available' AND D.MACHINENAME IS NOT NULL                                               \n")
		                         .append(" AND D.MACHINENAME!='3CEW01'  AND D.DURABLESPECNAME='CF'                                                                                         \n")
		                         .append(" ORDER BY dbms_random.value()                                                                                                                    \n");
		                         
		Object[] bindArray= new Object[0];
		List<ListOrderedMap>result;
		try
		{
			result = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBufferTP.toString(), bindArray);
		}
		catch (FrameworkErrorSignal fe)
		{
			result = null;
			throw new CustomException("", fe.getMessage());
			//return;
		}
		
		//EventInfo eventInfo = null;
		if(result.size()>0&&!result.isEmpty())
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
			doCSTTransfer(durableName,durableSpec,sourceMachine,sourceZone,sourcePositionType,sourcePositionName,durableType,durableCleanState);
		}
	}
	 private void doCSTTransfer(String durableName,String durableSpec,String sourceMachine, String sourceZone,String sourcePositionType,String sourcePositionName,String durableType,String durableCleanState )throws CustomException
	 {
	  try
		{
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
			
			if(durableSpec.equals("AN"))
				
			{
				destinationMachine="3ACC01";
				destinationPositionName="P01";
				
			}
           if(durableSpec.equals("TN"))
				
			{
				destinationMachine="3TCC01";
				destinationPositionName="P01";
				
			}
            if(durableSpec.equals("CN"))
				
			{
				destinationMachine="3CEW01";
				destinationPositionName="P01";
				
			}
            if(durableSpec.equals("CF"))
				
			{
				destinationMachine="3CEW01";
				destinationPositionName="P01";
				
			}
			
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
			
			Element lotNameElement = new Element("LOTNAME");
			lotNameElement.setText(lotName);
			bodyElement.addContent(lotNameElement);
			
			Element productQuantityElement = new Element("PRODUCTQUANTITY");
			productQuantityElement.setText(ProductQuantity);
			bodyElement.addContent(productQuantityElement);
			
			Element carrierStateElement = new Element("CARRIERSTATE");
			carrierStateElement.setText(durableState);
			bodyElement.addContent(carrierStateElement);
			
			Element priorityElement = new Element("PRIORITY");
			priorityElement.setText(priority);
			bodyElement.addContent(priorityElement);
			
			Element carrierTypeElement = new Element("CARRIERTYPE");
			carrierTypeElement.setText(carrierType);
			bodyElement.addContent(carrierTypeElement);
			
			Element cleanStateElement = new Element("CLEANSTATE");
			cleanStateElement.setText(priority);
			bodyElement.addContent(carrierCleanState);
			String targetSubject = GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("TEMsvr");
			Document Doc = SMessageUtil.createXmlDocument(bodyElement, "RequestTransportJobRequest",
					GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("TEMsvr"),
					targetSubject,
					"MES",
					"Reserve CST Transfer Time");
			SMessageUtil.setItemValue(Doc, "Header", "LANGUAGE","English");
			GenericServiceProxy.getESBServive().sendBySender(targetSubject, Doc, "TEMSender");
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
