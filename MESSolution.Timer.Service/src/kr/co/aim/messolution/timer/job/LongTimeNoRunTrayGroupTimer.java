package kr.co.aim.messolution.timer.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

public class LongTimeNoRunTrayGroupTimer implements Job, InitializingBean{
	

	private static Log log = LogFactory.getLog(LongTimeNoRunTrayGroupTimer.class);
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		log.debug(" LongTimeNoRunTrayGroupTimer-Start");

		try 
		{
			String sql = "SELECT * FROM ENUMDEFVALUE A WHERE A.ENUMNAME = 'LongTimeNoRunTrayGroupSwitch' AND A.ENUMVALUE = 'Switch' AND A.DEFAULTFLAG = 'Y'";
			Map<String, Object> bindMap = new HashMap<String, Object>();

			List<Map<String, Object>> switchFor = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			
			if(switchFor!=null && switchFor.size()>0)
				ReserveCSTTransfer();
			else
				log.debug(" LongTimeNoRunTrayGroupSwitch Is N");
		} 
		catch (CustomException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.debug(" LongTimeNoRunTrayGroupTimer-End");
		
	}
	public void ReserveCSTTransfer() throws CustomException
	{
		List<Map<String, Object>> result = null;
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT D.DURABLENAME, ");
		sql.append(" D.DURABLESPECNAME, ");
		sql.append(" D.DURABLETYPE, ");
		sql.append(" D.DURABLESTATE, ");
		sql.append(" D.DURABLEHOLDSTATE, ");
		sql.append(" D.TRANSPORTLOCKFLAG, ");
		sql.append(" D.MACHINENAME, ");
		sql.append(" D.PORTNAME, ");
		sql.append(" D.ZONENAME, ");
		sql.append(" D.POSITIONTYPE, ");
		sql.append(" D.POSITIONNAME ");
		sql.append(" FROM DURABLE D ");
		sql.append(" WHERE     D.DURABLENAME IN ");
		sql.append(" (SELECT DISTINCT B.COVERNAME ");
		sql.append(" FROM LOT A ");
		sql.append(" LEFT JOIN DURABLE B ON A.CARRIERNAME = B.DURABLENAME ");
		sql.append(" WHERE     A.FACTORYNAME = 'POSTCELL' ");
		sql.append(" AND A.CARRIERNAME IS NOT NULL ");
		sql.append(" AND A.LOTSTATE <> 'Shipped' ");
		sql.append(" AND A.LOTPROCESSSTATE = 'WAIT' ");
		sql.append(" AND A.PRODUCTTYPE = 'Panel' ");
		sql.append(" AND ROUND ((SYSDATE - A.LASTLOGGEDOUTTIME) * 24, 1) > '72' ");
		sql.append(" AND B.COVERNAME IS NOT NULL) ");
		sql.append(" AND (D.TRANSPORTLOCKFLAG != 'Y' OR D.TRANSPORTLOCKFLAG IS NULL) ");
		sql.append(" AND D.DURABLEHOLDSTATE != 'Y' ");
		sql.append(" AND D.MACHINENAME IS NOT NULL ");
		sql.append(" AND (D.WEIGHT < :LASTMOVETIME OR D.WEIGHT IS NULL)");
		sql.append(" ORDER BY DBMS_RANDOM.VALUE () ");

		Map<String, Object> bindMap = new HashMap<>();
		bindMap.put("LASTMOVETIME", TimeStampUtil.getCurrentEventTimeKey().substring(0, 8));
		
		try 
		{
			result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
		} 
		catch (Exception ex) 
		{
			log.info(ex.getCause());
			return ;
		}
		
		if(result!=null && result.size()>0)
		{
			Map<String, Object> row= result.get(0);
			EventInfo  eventInfo = EventInfoUtil.makeEventInfo("LongTimeNoRunTrayGroupTransfer", "MES", " Long Time No Run TrayGroup Transfer", null, null);
			String durableName =CommonUtil.getValue(row, "DURABLENAME");
			String durableSpec=CommonUtil.getValue(row, "DURABLESPECNAME");
			String sourceMachineName=CommonUtil.getValue(row, "MACHINENAME");
			
			if(sourceMachineName.isEmpty())
			{
				log.info("sourceMachineName Is Empty");
				return;
			}
			
			MachineSpec sourceMachine = null;
			try 
			{
				sourceMachine = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(sourceMachineName);
			} 
			catch (Exception e) 
			{
				log.info(e.getCause());
			}
			
			String sourcePositionType = "";
			String sourcePositionName = "";
			String sourceZoneName = "";
			
			if(sourceMachine!=null)
			{
				if(sourceMachine.getMachineType().equals("StorageMachine"))
				{
					sourcePositionType=CommonUtil.getValue(row, "POSITIONTYPE");
					sourceZoneName = CommonUtil.getValue(row, "ZONENAME");
					
					if(sourcePositionType.equals("PORT"))
					{
						sourcePositionName=CommonUtil.getValue(row, "PORTNAME");
					}
					else 
					{
						sourcePositionName=CommonUtil.getValue(row, "POSITIONNAME");
					}
				}
				else
				{
					sourcePositionType="PORT";
					sourcePositionName=CommonUtil.getValue(row, "PORTNAME");
					sourceZoneName = "";
				}
			}
			else
			{
				sourcePositionType="PORT";
				sourcePositionName=CommonUtil.getValue(row, "PORTNAME");
				sourceZoneName = "";
			}
			
			
			
			String desMachineName="3PFCV06";
			String desPositionType= "PORT";
			String desZoneName="";
			String durableTypeName=result.get(0).get("DURABLETYPE").toString();
			//getCarrierDataRequest(durableName);
			
			doTrayGroupTransfer(eventInfo,durableName,durableSpec,sourceMachineName,sourceZoneName,sourcePositionType,sourcePositionName,desMachineName,desPositionType,desZoneName,durableTypeName);	
		}
	}
	 private void doTrayGroupTransfer(EventInfo eventInfo, String durableName,String durableSpec,String sourceMachine,String sourceZoneName, String sourcePositionType,String sourcePositionName,String desMachineName,String desPositionType,String desZoneName,String durableType)throws CustomException
	 {
	  try
		{
		  GenericServiceProxy.getTxDataSourceManager().beginTransaction();
		  {
			String transportJobName =null;
			String lotName=null;
			String ProductQuantity=null;
			String durableState="EMPTY";
			String priority="51";		
			
			Durable traygroup = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("WEIGHT", TimeStampUtil.getCurrentEventTimeKey());
			
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(traygroup, setEventInfo , eventInfo);
			
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
			sourceZoneElement.setText(sourceZoneName);
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
			destinationPositionTypeElement.setText(desPositionType);
			bodyElement.addContent(destinationPositionTypeElement);
			
			Element destinationPositionNameElement = new Element("DESTINATIONPOSITIONNAME");
			destinationPositionNameElement.setText("P01");
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
			
			String targetSubject = GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("TEMsvr");
			Document Doc = SMessageUtil.createXmlDocument(bodyElement, "RequestTransportJobRequest",
					GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("TEMsvr"),
					targetSubject,
					"MES",
					"Long Time No Run TrayGroup Transfer Timer");
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
