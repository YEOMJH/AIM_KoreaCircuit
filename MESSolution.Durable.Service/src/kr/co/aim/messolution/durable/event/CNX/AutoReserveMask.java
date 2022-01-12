package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.EvaLineSchedule;
import kr.co.aim.messolution.extended.object.management.data.EvaLineWo;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskTransfer;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class AutoReserveMask extends SyncHandler {
	private static Log log = LogFactory.getLog(AutoReserveMask.class);
	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub

		
		String machineName =SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String lineType =SMessageUtil.getBodyItemValue(doc, "LINETYPE", true);
		String productSpecName =SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productRequestName =SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		String seq =SMessageUtil.getBodyItemValue(doc, "SEQ", true);
		String buttonType=SMessageUtil.getBodyItemValue(doc, "BUTTONTYPE", true);
		String dspFlag=SMessageUtil.getBodyItemValue(doc, "DSPFLAG", false);
		String MaskType=SMessageUtil.getBodyItemValue(doc, "MASKTYPE", true);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("OICAutoReserveMask", this.getEventUser(), this.getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		//
		String condition= " WHERE SEQ=? AND  USEFLAG = ? AND MACHINENAME =? AND LINETYPE = ?AND STATE IN ('Ready','Started') ORDER BY SEQ ASC ";		
		 List<EvaLineSchedule>evaLineScheduleList=ExtendedObjectProxy.getEvaLineScheduleService().select(condition, new Object[]{seq,"Y",machineName,lineType});
		 if(!evaLineScheduleList.get(0).getProductRequestName().equals(productRequestName)&&Integer.parseInt(seq)>1)
		 {
				for(int j=0;j<Integer.parseInt(seq);j++)
				{
					String seqState= evaLineScheduleList.get(j).getState();
					if(!seqState.equals("Complete"))
					{
						throw new CustomException("WOSchedulingToEVA-001","");
					}
				}
		 }
		/*	List<Map<String,Object>> bufferMachineResult=getBufferMachine(unitName,lineType);
			String bufferMachineName=String.valueOf(bufferMachineResult.get(0).get("BUFFERMACHINENAME"));
			MachineSpec bufferMachineSpec=MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(bufferMachineName);
			String ms_lineType=bufferMachineSpec.getUdfs().get("LINETYPE");
			//3
			EVAReserveSpec evaReserveSpec = ExtendedObjectProxy.getEVAReserveSpecService().selectByKey(true, new Object[]{bufferMachineName});*/
		
			
		if(buttonType.equals("Reserve")){
		
				 EvaLineSchedule nowEvaLineSchedule= ExtendedObjectProxy.getEvaLineScheduleService().selectByKey(true, new Object[]{machineName,lineType,evaLineScheduleList.get(0).getPlanStartDate(),productRequestName});
				 if(nowEvaLineSchedule.getState().equals("Ready"))
				 {
					 CheckReserveState(productSpecName,productRequestName,seq);
					 List<Map<String,Object>>maskGroup=checkLotGetMaskGroup(productSpecName,MaskType);
					 List<Map<String,Object>>unitMachineList=getUnitMachine();
					 List<ReserveMaskTransfer>reserveInfoList=new ArrayList<>();
					 if(unitMachineList.size()>maskGroup.size())
					 {
						 
						throw new CustomException("MASK-0023","");
					 }
					 else
					 {
						 
						 Map<String,List<String>>unitListmap=new ListOrderedMap();
						for(Map<String,Object>toUnitMachineMap:unitMachineList)
						{
							if(unitListmap.containsKey(toUnitMachineMap.get("MACHINENAME")))
							{
								unitListmap.get(toUnitMachineMap.get("MACHINENAME")).add(ConvertUtil.getMapValueByName(toUnitMachineMap, "SUBSUBUNITNAME"));
							}
							else
							{
								List<String> subUnitList = new ArrayList<>();
								subUnitList.add(ConvertUtil.getMapValueByName(toUnitMachineMap, "SUBSUBUNITNAME"));
								unitListmap.put(toUnitMachineMap.get("MACHINENAME").toString(), subUnitList);
							}
							
						}
						
						
							List<Map<String,Object>>carrierList=new ArrayList<Map<String,Object>>();
							carrierList=getCarrierList("EVAMaskCST", "Available", "Clean");
							/*if(evaReserveSpec.getMaskKind().equals("EVA"))
							{
								carrierList=getCarrierList("EVAMaskCST", "Available", "Clean");
							}
							else if(evaReserveSpec.getMaskKind().equals("TFE"))
							{
								carrierList=getCarrierList("TFEMaskCST", "Available", "Clean");
							}*/
							int allUnitCount=0;
							int carrierCount=0;
							String oldUnitName = null;
							for(String keyInfo :unitListmap.keySet())
							{
								String unitname=keyInfo.toString();
								List<String>allUnit=unitListmap.get(keyInfo);
								int jCount =0;
								for(int j=0;j<allUnit.size();j++)
								{
									String subUnitName=allUnit.get(j).substring(0,14);
									jCount++;
									if(jCount==3) jCount+=1;
									//maskGroup.get(i).get(key)
									List<Map<String,Object>> bufferMachineResult=getBufferMachine(unitname,lineType);
									String bufferMachineName=String.valueOf(bufferMachineResult.get(0).get("BUFFERMACHINENAME"));
									if(StringUtil.isEmpty(oldUnitName))
									{
										oldUnitName = bufferMachineName;
									}
									if((allUnitCount!=0&&allUnitCount%4==0)||(!oldUnitName.equals(bufferMachineName)))
									{
										carrierCount++;
										
									}
									ReserveMaskTransfer reserveMaskTransferInfo = new ReserveMaskTransfer(
											ConvertUtil.getMapValueByName(
													carrierList.get(carrierCount), "DURABLENAME"), 
											String.valueOf(jCount),
											ConvertUtil.getMapValueByName(
													maskGroup.get(allUnitCount), "MASKLOTNAME"),
											bufferMachineName, "Y",
											ConvertUtil.getMapValueByName(
													maskGroup.get(allUnitCount), "MASKGROUPNAME"),
											dspFlag,
											productSpecName,
											productRequestName,
											Integer.parseInt(seq),
											machineName,
											allUnit.get(j),
											unitname, 
											subUnitName,
											eventInfo.getEventName(),
											eventInfo.getEventTimeKey(),
											eventInfo.getEventComment(),
											eventInfo.getEventTime(),
											eventInfo.getEventUser());
									//ExtendedObjectProxy.getReserveMaskTransferService().i.create(eventInfo, reserveMaskTransferInfo);
									reserveInfoList.add(reserveMaskTransferInfo);
									allUnitCount++;
									
									oldUnitName = bufferMachineName;
								}
							}
							try {
								ExtendedObjectProxy.getReserveMaskTransferService().insert(reserveInfoList);
								ExtendedObjectProxy.getReserveMaskTransferService().addHistory(eventInfo, "CT_RESERVEMASKTRANSFERHIST", reserveInfoList, log);
							} catch (Exception e) {
								// TODO Auto-generated catch block
								throw new CustomException("MASK-0024","");
							}
					 }
				 }
				 else if(nowEvaLineSchedule.getState().equals("Started"))
				 {
					throw new CustomException("MASK-0024","");
				 }
				 else
				 {
					throw new CustomException("MASK-0024","");
				 }
				 return doc;
		 
		}
		else if(buttonType.equals("Finished"))
		{
			 CheckReserveState(productSpecName,productRequestName,seq);
			 List<Map<String,Object>>evaLineWOList=getEvaLineWo(productSpecName, productRequestName, seq, machineName);
			 if(evaLineWOList.size()>0)
			 { 
				List<EvaLineWo>modifyEvaWOList=new ArrayList<>();
				List<EvaLineWo>EvaWOList =ExtendedObjectProxy.getEvaLineWoService().select("WHERE PRODUCTSPECNAME=? AND PRODUCTREQUESTNAME=? AND SEQ =?AND MACHINENAME=?", new Object[]{productSpecName,productRequestName,seq,machineName});	
				for(EvaLineWo evaLineWO:EvaWOList)
				{
					evaLineWO.setState("Finished");
					modifyEvaWOList.add(evaLineWO);
				}
				ExtendedObjectProxy.getEvaLineWoService().modify(eventInfo, modifyEvaWOList);
				
				ExtendedObjectProxy.getEvaLineWoService().delete(modifyEvaWOList);
			 }
			 List<EvaLineSchedule> EvaLineScheduleList= ExtendedObjectProxy.getEvaLineScheduleService().select("WHERE MACHINENAME=? AND LINETYPE=? AND PRODUCTSPECNAME =? AND SEQ =? AND PRODUCTREQUESTNAME=? ", new Object[]{machineName,lineType,productSpecName,seq,productRequestName});
			 for(EvaLineSchedule eavLineSchedule:EvaLineScheduleList)
			 {
				 eavLineSchedule.setState("Completed");
				 ExtendedObjectProxy.getEvaLineScheduleService().modify(eventInfo, eavLineSchedule);
			 }
			 
			/* List<ReserveMaskTransfer>reserveMaskTransferList= ExtendedObjectProxy.getReserveMaskTransferService().select("WHERE PRODUCTSPECNAME=? AND PRODUCTREQUESTNAME=? AND SEQ=? ", new Object[]{productSpecName,productRequestName,seq});
			 ExtendedObjectProxy.getReserveMaskTransferService().delete(reserveMaskTransferList);
			 */
		}
		return doc;
		
		
	}
	
	public void CheckReserveState(String prodcutSpecName,String productRequestName,String seq) throws greenFrameDBErrorSignal, CustomException
	{
	StringBuilder sql = new StringBuilder();
	sql.append(" SELECT CARRIERNAME, POSITION ");
	sql.append(" FROM CT_RESERVEMASKTRANSFER ");
	sql.append(" WHERE PRODUCTSPECNAME = :PRODUCTSPECNAME ");
	sql.append(" AND PRODUCTREQUESTNAME = :PRODUCTREQUESTNAME ");
	sql.append(" AND SEQ = :SEQ ");
	
	Map<String,Object>bindMap=new HashMap<String,Object>();
	//machineName,lineType,productSpecName,productRequestName 
	
	bindMap.put("PRODUCTSPECNAME", prodcutSpecName);
	bindMap.put("PRODUCTREQUESTNAME", productRequestName);	
	bindMap.put("SEQ",seq);
	
	@SuppressWarnings("unchecked")
	List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
		if(sqlResult.size()>0)
		{
			List<ReserveMaskTransfer> dataInfoList=ExtendedObjectProxy.getReserveMaskTransferService().select("WHERE PRODUCTSPECNAME=? AND PRODUCTREQUESTNAME=? AND SEQ=? ", new Object[]{prodcutSpecName,productRequestName,seq});
	/*
			StringBuilder removeSql = new StringBuilder();
			removeSql.append(" DELETE CT_RESERVEMASKTRANSFER ") ;
			removeSql.append(" WHERE PRODUCTSPECNAME = :PRODUCTSPECNAME ");
			removeSql.append(" AND PRODUCTREQUESTNAME = :PRODUCTREQUESTNAME ");
			removeSql.append(" AND SEQ = :SEQ ");

			Map<String,Object>removeMap=new HashMap<String,Object>();
			//machineName,lineType,productSpecName,productRequestName 
			
			removeMap.put("PRODUCTSPECNAME", prodcutSpecName);
			removeMap.put("PRODUCTREQUESTNAME", productRequestName);	
			removeMap.put("SEQ", seq);
		
			//greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(sql, bindMap);	
			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);*/
			ExtendedObjectProxy.getReserveMaskTransferService().delete(dataInfoList);
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteReserveMask", this.getEventUser(), this.getEventComment(), null, null);
			ExtendedObjectProxy.getReserveMaskTransferService().addHistory(eventInfo, "CT_RESERVEMASKTRANSFERHIST", dataInfoList, log);;
			log.info("Delete CT_RESERVEMASKTRANSFER Success."); 
			
		}
		
	}
	public List<Map<String, Object>>  getEvaLineWo(String productSpecName,String productRequestName,String seq,String machineName)
	{
	StringBuilder sql = new StringBuilder();
	sql.append(" SELECT UNITNAME, LINETYPE, SEQ ");
	sql.append(" FROM CT_EVALINEWO ");
	sql.append(" WHERE PRODUCTSPECNAME = :PRODUCTSPECNAME ");
	sql.append(" AND PRODUCTREQUESTNAME = :PRODUCTREQUESTNAME ");
	sql.append(" AND SEQ = :SEQ ");
	sql.append(" AND MACHINENAME = :MACHINENAME ");

	Map<String,Object>bindMap=new HashMap<String,Object>();
	//machineName,lineType,productSpecName,productRequestName 
	bindMap.put("PRODUCTSPECNAME", productSpecName);
	bindMap.put("PRODUCTREQUESTNAME", productRequestName);
	bindMap.put("SEQ", seq);
	bindMap.put("MACHINENAME", machineName);


	@SuppressWarnings("unchecked")
	List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
	return sqlResult;
	}
	

	public List<Map<String,Object>>getCarrierList(String durableType,String durableState,String duracleanState)
	{
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT  DURABLENAME");
		sql.append(" FROM DURABLE ");
		sql.append(" WHERE DURABLETYPE=:DURABLETYPE ");
		sql.append(" AND DURABLESTATE=:DURABLESTATE ");
		sql.append(" AND DURABLECLEANSTATE=:DURABLECLEANSTATE ");
		sql.append(" AND TRANSPORTSTATE=:TRANSPORTSTATE ");
		sql.append(" AND TRANSPORTLOCKFLAG!=:TRANSPORTLOCKFLAG ");
		sql.append(" AND DURABLEHOLDSTATE!=:DURABLEHOLDSTATE ");
		Map<String,Object>bindMap=new HashMap<String,Object>();
		//machineName,lineType,productSpecName,productRequestName 
		
		bindMap.put("DURABLETYPE", durableType);
		bindMap.put("DURABLESTATE", durableState);
		bindMap.put("DURABLECLEANSTATE", duracleanState);
		bindMap.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_INSTK);
		bindMap.put("TRANSPORTLOCKFLAG", "Y");
		bindMap.put("DURABLEHOLDSTATE", "Y");
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
		return sqlResult;
			
	}
	
	public List<Map<String, Object>> getBufferMachine(String unitName,String lineType)
	{

	StringBuilder sql = new StringBuilder();
	sql.append(" SELECT BUFFERMACHINENAME ");
	sql.append(" FROM CT_CONNECTEDBUFFERPORT ");
	sql.append(" WHERE UNITNAME =:UNITNAME ");
	sql.append(" AND LINETYPE =:LINETYPE ");
	
	Map<String,Object>bindMap=new HashMap<String,Object>();
	//machineName,lineType,productSpecName,productRequestName 
	bindMap.put("UNITNAME", unitName);
	bindMap.put("LINETYPE", lineType);	
	
	@SuppressWarnings("unchecked")
	List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
	return sqlResult;
	
	
	}
	
	
	public List<Map<String, Object>> getUnitMachine()
	{

	StringBuilder sql = new StringBuilder();
	sql.append("  SELECT A.MACHINENAME , ");
	sql.append(" C.MACHINENAME AS SUBSUBUNITNAME ");
	sql.append(" FROM MACHINESPEC A, MACHINESPEC B, MACHINESPEC C, MACHINE D ,CT_MASKGROUPLOT E");
	sql.append(" WHERE A. MACHINENAME = B.SUPERMACHINENAME ");
    sql.append("  AND B.MACHINENAME=C.SUPERMACHINENAME ");
	sql.append(" AND C.MACHINENAME = D.MACHINENAME");
	sql.append(" AND C.MACHINENAME = E.STAGE");
	sql.append(" AND D.STAGEUSEFLAG = 'Y' ");
	sql.append(" AND A.MACHINEGROUPNAME = 'EVA' ");
	sql.append(" AND A.MACHINEGROUPNAME = B.MACHINEGROUPNAME  ");
	sql.append(" AND B.MACHINEGROUPNAME = C.MACHINEGROUPNAME  ");
	sql.append(" AND A.DETAILMACHINETYPE = 'UNIT'  ");
	sql.append(" AND B.DETAILMACHINETYPE = 'SUBUNIT'  ");
	sql.append(" AND A.MACHINETYPE = 'ProductionMachine' ");
	sql.append(" AND B.MACHINETYPE = 'ProductionMachine' ");
	sql.append(" ORDER BY  A.MACHINENAME,C.MACHINENAME  ");
	Map<String,Object>bindMap=new HashMap<String,Object>();
	//machineName,lineType,productSpecName,productRequestName 

	@SuppressWarnings("unchecked")
	List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
	return sqlResult;
	
	
	}
	
	public List<Map<String, Object>> checkLotGetMaskGroup(String prodcuctSpecName,String MaskType)
	{

	StringBuilder sql = new StringBuilder();
	sql.append(" SELECT A.MASKGROUPNAME, A.MASKLOTNAME");
	sql.append(" FROM CT_MASKGROUPLOT A, CT_MASKLOT L ,CT_MASKGROUPLIST B ");
	sql.append(" WHERE A.MASKGROUPNAME=B.MASKGROUPNAME ");
	sql.append("  AND B.PRODUCTSPECNAME=:PRODUCTSPECNAME");
	sql.append("  AND A.MASKTYPE=:MASKTYPE");
	sql.append("  AND B.USEFLAG='Y' ");
	sql.append("  AND A.MASKLOTNAME = L.MASKLOTNAME ");
	sql.append("  AND L.MASKLOTPROCESSSTATE = 'WAIT' ");
	sql.append("  AND L.MASKTYPE=A.MASKTYPE ");
	sql.append("   AND L.RESERVESTAGE=A.STAGE ");
	sql.append("  AND L.CLEANSTATE = 'Clean' ");
	sql.append("  AND L.MASKLOTHOLDSTATE = 'N' ");
	sql.append("  ORDER BY B.PRIORITY ASC ");
	Map<String,Object>bindMap=new HashMap<String,Object>();
	//machineName,lineType,productSpecName,productRequestName 
	bindMap.put("PRODUCTSPECNAME", prodcuctSpecName);
	bindMap.put("MASKTYPE", MaskType);
	@SuppressWarnings("unchecked")
	List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
	return sqlResult;
	
	
	}
	
	
}
