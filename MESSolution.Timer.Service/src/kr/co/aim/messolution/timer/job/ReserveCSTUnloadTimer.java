package kr.co.aim.messolution.timer.job;

import java.util.ArrayList;
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

public class ReserveCSTUnloadTimer implements Job, InitializingBean{
	

	private static Log log = LogFactory.getLog(ReserveCSTUnloadTimer.class);
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.debug(" ReserveCSTUnloadTimer-Start");
		//Monitor the CST Clean Time
		List<String> arrayDurableType=new ArrayList<String>(); arrayDurableType.add("SheetCST");
		List<String> arrayDurableSpec=new ArrayList<String>(); arrayDurableSpec.add("AN");
		List<String> oledDurableType=new ArrayList<String>(); oledDurableType.add("OLEDGlassCST");oledDurableType.add("FilmCST");
		List<String> oledDurableSpec=new ArrayList<String>(); oledDurableSpec.add("CN");oledDurableSpec.add("CF");
		List<String> tpDurableType=new ArrayList<String>(); tpDurableType.add("TPGlassCST");
		List<String> tpDurableSpec=new ArrayList<String>(); tpDurableSpec.add("TN");
		
		//ReserveCSTTransfer("ARRAY",arrayDurableType,arrayDurableSpec,"3ACC01","P02","3ASTK08","P02");
		//ReserveCSTTransfer("OLED",oledDurableType,oledDurableSpec,"3CEW01","P02","3CSTK04","P04");
		//ReserveCSTTransfer("TP",tpDurableType,tpDurableSpec,"3TCC01","P02","3TSTK06","P03");
		log.debug(" ReserveCSTUnloadTimer-End");
		
	}
	public void ReserveCSTTransfer(String factoryName,List<String> durableType,List<String> durableSpecName,String machineName,String portName,String targetMachineName,String targetZoneName) throws CustomException
	{
		List<Map<String, Object>> result = null;
		String sql = "SELECT D.DURABLENAME,D.DURABLESPECNAME,D.DURABLETYPE,D.DURABLESTATE,D.DURABLEHOLDSTATE,D.TRANSPORTLOCKFLAG,D.MACHINENAME,D.PORTNAME,D.DURABLECLEANSTATE "
				+ " FROM DURABLE D "
				+ " WHERE     FACTORYNAME = :FACTORYNAME "
		        + " AND DURABLETYPE IN (:DURABLETYPE) "
		        + " AND DURABLESPECNAME IN (:DURABLESPECNAME) "
		        + " AND DURABLESTATE = 'Available' "
		        + " AND (TRANSPORTLOCKFLAG != 'Y' OR TRANSPORTLOCKFLAG IS NULL)"
		        + " AND MACHINENAME = :MACHINENAME "
		        + " AND PORTNAME=:PORTNAME "
		        + " ORDER BY DBMS_RANDOM.VALUE() ";

		Map<String, Object> bindMap = new HashMap<>();
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("DURABLETYPE", durableType);
		bindMap.put("DURABLESPECNAME", durableSpecName);
		bindMap.put("MACHINENAME", machineName);
		bindMap.put("PORTNAME", portName);
		
		try 
		{
			result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		} 
		catch (Exception ex) 
		{
			log.info(ex.getCause());
			return ;
		}
		List<Map<String, Object>> resultTargetZone = null;
		String sqlTargetZon = "SELECT S.ZONENAME"
				+ " FROM CT_STOCKERZONEINFO S "
				+ " WHERE S.MACHINENAME=:MACHINENAME AND S.TOTALCAPACITY!=S.USEDSHELFCOUNT  "
		        + " ORDER BY dbms_random.value()";
		       

		Map<String, Object> bindMapTargetZon = new HashMap<>();
		bindMapTargetZon.put("MACHINENAME", targetMachineName);
		try 
		{
			resultTargetZone = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlTargetZon, bindMapTargetZon);
		} 
		catch (Exception ex) 
		{
			log.info(ex.getCause());
			return ;
		}
		if(resultTargetZone.size()>0&&!resultTargetZone.isEmpty()&&!factoryName.equals("ARRAY"))
		{
			Map<String, Object> row= resultTargetZone.get(0);
			targetZoneName=CommonUtil.getValue(row, "ZONENAME");
		}
		if(result.size()>0&&!result.isEmpty())
		{
			Map<String, Object> row= result.get(0);
			EventInfo  eventInfo = EventInfoUtil.makeEventInfo("CSTUnloadTransfer", "MES", " CST Unload Transfer service", null, null);
			String durableName =CommonUtil.getValue(row, "DURABLENAME");
			String durableSpec=CommonUtil.getValue(row, "DURABLESPECNAME");
			String sourceMachineName=CommonUtil.getValue(row, "MACHINENAME");
			String sourcePositionType="PORT";
			String sourcePositionName=CommonUtil.getValue(row, "PORTNAME");
			String desMachineName=targetMachineName;
			String desPositionType="SHELF";
			String desZoneName=targetZoneName;
			String durableTypeName=result.get(0).get("DURABLETYPE").toString();
			String durableCleanState=result.get(0).get("DURABLECLEANSTATE").toString();
			//getCarrierDataRequest(durableName);
			
			
		    doCSTARRAYTransfer(durableName,durableSpec,sourceMachineName,sourcePositionType,sourcePositionName,desMachineName,desPositionType,desZoneName,durableTypeName,durableCleanState);	
				
			
			
		}
	}
	 private void doCSTARRAYTransfer(String durableName,String durableSpec,String sourceMachine, String sourcePositionType,String sourcePositionName,String desMachineName,String desPositionType,String desZoneName,String durableType,String durableCleanState )throws CustomException
	 {
	  try
		{
		  GenericServiceProxy.getTxDataSourceManager().beginTransaction();
		  {
			String transportJobName =null;
			String lotName=null;
			String ProductQuantity=null;
			String durableState="EMPTY";
			String priority="80";		
			
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
			sourceZoneElement.setText("");
			bodyElement.addContent(sourceZoneElement);	
			
			Element sourcePostionElement = new Element("SOURCEPOSITIONTYPE");
			sourcePostionElement.setText(sourcePositionType);
			bodyElement.addContent(sourcePostionElement);	
			
			Element sourcePostionNameElement = new Element("SOURCEPOSITIONNAME");
			sourcePostionNameElement.setText(sourcePositionName);
			bodyElement.addContent(sourcePostionNameElement);
			
			Element destinationMachineNameElement = new Element("DESTINATIONMACHINENAME");
			destinationMachineNameElement.setText(desMachineName);
			bodyElement.addContent(destinationMachineNameElement);

			Element destinationZoneElement = new Element("DESTINATIONZONENAME");
			destinationZoneElement.setText("");
			bodyElement.addContent(destinationZoneElement);
			
			Element destinationPositionTypeElement = new Element("DESTINATIONPOSITIONTYPE");
			destinationPositionTypeElement.setText("PORT");
			bodyElement.addContent(destinationPositionTypeElement);
			
			Element destinationPositionNameElement = new Element("DESTINATIONPOSITIONNAME");
			destinationPositionNameElement.setText(desZoneName);
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
			carrierTypeElement.setText(durableType);
			bodyElement.addContent(carrierTypeElement);
			
			Element cleanStateElement = new Element("CLEANSTATE");
			cleanStateElement.setText(durableCleanState);
			bodyElement.addContent(cleanStateElement);
			String targetSubject = GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("TEMsvr");
			Document Doc = SMessageUtil.createXmlDocument(bodyElement, "RequestTransportJobRequest",
					GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("TEMsvr"),
					targetSubject,
					"MES",
					"CST Unload Transfer Time");
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
	
	 private void doCSTTransfer(String durableName,String durableSpec,String sourceMachine, String sourcePositionType,String sourcePositionName,String desMachineName,String desPositionType,String desZoneName,String durableType,String durableCleanState )throws CustomException
	 {
	  try
		{
		  GenericServiceProxy.getTxDataSourceManager().beginTransaction();
		  {
			String transportJobName =null;
			String lotName=null;
			String ProductQuantity=null;
			String durableState="EMPTY";
			String priority="80";		
			
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
			sourceZoneElement.setText("");
			bodyElement.addContent(sourceZoneElement);	
			
			Element sourcePostionElement = new Element("SOURCEPOSITIONTYPE");
			sourcePostionElement.setText(sourcePositionType);
			bodyElement.addContent(sourcePostionElement);	
			
			Element sourcePostionNameElement = new Element("SOURCEPOSITIONNAME");
			sourcePostionNameElement.setText(sourcePositionName);
			bodyElement.addContent(sourcePostionNameElement);
			
			Element destinationMachineNameElement = new Element("DESTINATIONMACHINENAME");
			destinationMachineNameElement.setText(desMachineName);
			bodyElement.addContent(destinationMachineNameElement);

			Element destinationZoneElement = new Element("DESTINATIONZONENAME");
			destinationZoneElement.setText(desZoneName);
			bodyElement.addContent(destinationZoneElement);
			
			Element destinationPositionTypeElement = new Element("DESTINATIONPOSITIONTYPE");
			destinationPositionTypeElement.setText(desPositionType);
			bodyElement.addContent(destinationPositionTypeElement);
			
			Element destinationPositionNameElement = new Element("DESTINATIONPOSITIONNAME");
			destinationPositionNameElement.setText("");
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
			carrierTypeElement.setText(durableType);
			bodyElement.addContent(carrierTypeElement);
			
			Element cleanStateElement = new Element("CLEANSTATE");
			cleanStateElement.setText(durableCleanState);
			bodyElement.addContent(cleanStateElement);
			String targetSubject = GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("TEMsvr");
			Document Doc = SMessageUtil.createXmlDocument(bodyElement, "RequestTransportJobRequest",
					GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("TEMsvr"),
					targetSubject,
					"MES",
					"CST Unload Transfer Time");
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
