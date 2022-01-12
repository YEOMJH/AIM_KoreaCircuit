package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MVIPanelJudge;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.port.management.data.Port;

public class PostCellTrackInExcel extends SyncHandler {
	
	private static Log log = LogFactory.getLog(PostCellTrackInExcel.class);

	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();
		
		List<Object[]> updateDurableArgList = new ArrayList<Object[]>();
		List<Durable> durableHistoryArgList = new ArrayList<Durable>();
		List<Durable> oldDurableListHistory = new ArrayList<Durable>();

		List<String> trayList = new ArrayList<String>();
		
		List<String> lotList = CommonUtil.makeList(SMessageUtil.getBodyElement(doc), "PANELLIST", "PANELNAME");
		List<Lot> lotDataList = CommonUtil.getLotListByLotNameList(lotList);
		
		// Get Lot Quantity in Durable
		List<Map<String, Object>> durableResult = getLotQuantity(lotList);
		
		// Check Panel State
		String sql = "SELECT DISTINCT LOTHOLDSTATE, LOTSTATE, LOTPROCESSSTATE FROM LOT WHERE LOTNAME IN (:PANELLIST)";
		Map<String, Object> inquirybindMap = new HashMap<String, Object>();
		inquirybindMap.put("PANELLIST", lotList);
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, inquirybindMap);
		
		if(result.size() != 1)
		{
			throw new CustomException("PANEL-0001");
		}
		
		List<MVIPanelJudge> mviPanelList = new ArrayList<>();
		
		// Execute
		for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "PANELLIST", false))
		{
			String panelName = SMessageUtil.getChildText(eledur, "PANELNAME", true);
			String machineName = SMessageUtil.getChildText(eledur, "MACHINENAME", true);
			String recipeName = SMessageUtil.getChildText(eledur, "RECIPENAME", false);
			String portName = SMessageUtil.getChildText(eledur, "PORTNAME", true);
			String trayName = SMessageUtil.getChildText(eledur, "TRAYNAME", true);
			
			// Lot Action
			//-------------------------------------------------------------------------------------------------------------
			Lot lotData = new Lot();			
			for (Lot dataList : lotDataList)
			{
				if (StringUtils.equals(dataList.getKey().getLotName(), panelName))
				{
					lotData = dataList;
					break;
				}
			}
			
			Lot oldLotData = (Lot) ObjectUtil.copyTo(lotData);
			Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
			
			CheckMachineName(lotData, machineName);
			CommonValidation.checkLotGradeN(lotData);
			
			Map<String, String> portUdfs = portData.getUdfs();
			
			String portType = portUdfs.get("PORTTYPE");
			String portUseType = portUdfs.get("PORTUSETYPE");
			
			Map<String, String> lotUdfs = new HashMap<String, String>();
			lotUdfs.put("PORTNAME", portName);
			lotUdfs.put("PORTTYPE", portType);
			lotUdfs.put("PORTUSETYPE", portUseType);
			
			List<Object> lotBindList = setLotBindList(eventInfo, lotData, machineName, portName, portType, portUseType);
			updateLotArgList.add(lotBindList.toArray());

			lotData = setLotHistoryData(eventInfo, lotData, machineName, recipeName, lotUdfs);
			
			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLotData, lotData, lotHistory);
			
			updateLotHistoryList.add(lotHistory);
			
			List<Map<String, Object>> operationData = getOperationType(oldLotData.getFactoryName(), oldLotData.getProductSpecName(), 
					oldLotData.getProductSpecVersion(), oldLotData.getProcessFlowName() , oldLotData.getProcessFlowVersion(), 
					oldLotData.getProcessOperationName(), oldLotData.getProcessOperationVersion());
			
			if(operationData.get(0).get("DETAILPROCESSOPERATIONTYPE").equals("MVI"))
			{
				MVIPanelJudge mviData = new MVIPanelJudge();
				
				String seq = ExtendedObjectProxy.getMVIPanelJudgeService().getMVIPanelSeq(oldLotData.getKey().getLotName());
				
				mviData.setPanelName(oldLotData.getKey().getLotName());
				mviData.setSeq(Long.parseLong(seq));
				if(oldLotData.getLotGrade().equals("G"))
				{
					mviData.setBeforeGrade(oldLotData.getUdfs().get("LOTDETAILGRADE").toString());
				}
				else
				{
					mviData.setBeforeGrade(oldLotData.getLotGrade());
				}
				//mviData.setBeforeGrade(oldLotData.getUdfs().get("LOTDETAILGRADE").toString());
				mviData.setEventUser(eventInfo.getEventUser());
				mviData.setEventTime(eventInfo.getEventTime());
				
				mviPanelList.add(mviData);
			}
			//-------------------------------------------------------------------------------------------------------------
			
			
			// Durable Action
			//-------------------------------------------------------------------------------------------------------------
			if (!trayList.contains(trayName))
			{
				long lotQuantity = 0;
				
				for(int i = 0; i < durableResult.size(); i++)
				{	
					String durableName = ConvertUtil.getMapValueByName(durableResult.get(i), "CARRIERNAME");
					if (durableName.equals(trayName)) 
					{
						lotQuantity = Long.parseLong(ConvertUtil.getMapValueByName(durableResult.get(i), "QTY"));
						break;
					}
				}
				
				Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
				Durable oldDurableData = (Durable) ObjectUtil.copyTo(durableData);
				
				List<Object> durableBindList = setDurableBindList(eventInfo, durableData, lotQuantity);
				updateDurableArgList.add(durableBindList.toArray());
				
				durableData = setDurableHistoryData(eventInfo, durableData, lotQuantity);
				durableHistoryArgList.add(durableData);
				oldDurableListHistory.add(oldDurableData);
				
				trayList.add(trayName);
			}
			//-------------------------------------------------------------------------------------------------------------
		}
		
		panelAction(eventInfo, updateLotArgList, updateLotHistoryList);
		
		trayAction(eventInfo, updateDurableArgList, durableHistoryArgList, oldDurableListHistory);
		
		if(mviPanelList.size() > 0)
		{
			ExtendedObjectProxy.getMVIPanelJudgeService().create(eventInfo, mviPanelList);
		}
		
		return doc;
	}
	
	private List<Map<String, Object>> getLotQuantity(List<String> lotList)
	{
		String inquirysql = "SELECT CARRIERNAME, COUNT (LOTNAME) AS QTY "
						  + "FROM LOT "
						  + "WHERE LOTNAME IN (:LOTLIST) "
						  + "GROUP BY CARRIERNAME ";
		
		Map<String, Object> inquirybindMap = new HashMap<String, Object>();
		inquirybindMap.put("LOTLIST", lotList);
		
		List<Map<String, Object>> sqlResult = 
	       	GenericServiceProxy.getSqlMesTemplate().queryForList(inquirysql, inquirybindMap);
		
		return sqlResult;
	}
	
	private void CheckMachineName(Lot lotData, String machineName) throws CustomException
	{
		String inquirysql = "SELECT B.MACHINENAME, B.MACHINERECIPENAME "
						  + "FROM TPFOPOLICY A, POSMACHINE B "
						  + "WHERE A.CONDITIONID = B.CONDITIONID "
						  + "AND A.PROCESSFLOWNAME = ? "
						  + "AND A.PROCESSFLOWVERSION = ? "
						  + "AND A.PROCESSOPERATIONNAME = ? "
						  + "AND A.PROCESSOPERATIONVERSION = ? ";
		
	/*	Map<String, Object> inquirybindMap = new HashMap<String, Object>();
		inquirybindMap.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
		inquirybindMap.put("PROCESSFLOWVERSION", lotData.getProcessFlowVersion());
		inquirybindMap.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
		inquirybindMap.put("PROCESSOPERATIONVERSION", lotData.getProcessOperationVersion());*/
		
		/*List<Map<String, Object>> sqlResult = 
	       	GenericServiceProxy.getSqlMesTemplate().queryForList(inquirysql, inquirybindMap);*/
		 Object[] bindSet = new String[]{ lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),lotData.getProcessOperationName(),lotData.getProcessOperationVersion()};
		   List<ListOrderedMap> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(inquirysql, bindSet); 
		  
		
		if(sqlResult.size()>0)
		{  Boolean Flag =false;
			for(int i=0;i<sqlResult.size();i++){
				
				String posMachineName = ConvertUtil.getMapValueByName(sqlResult.get(i), "MACHINENAME");
				
				if(posMachineName.equals(machineName))
				{
					
					
					Flag =true ;
					 break;
					
					}
					
				 if(Flag ) 	break;
				
				}
				

			if  (!Flag ){ 
				throw new CustomException("PANEL-0002", lotData.getProcessOperationName(), lotData.getKey().getLotName(), machineName);
			
			
		}
			}
		else
		{
			throw new CustomException("PANEL-0003");	
		}
	}
	
	private List<Object> setLotBindList(EventInfo eventInfo, Lot lotData, String machineName, String portName, String portType, String portUseType)
	{
		List<Object> lotBindList = new ArrayList<Object>();
		
		lotBindList.add(GenericServiceProxy.getConstantMap().Lot_Released);
		lotBindList.add(GenericServiceProxy.getConstantMap().Lot_LoggedIn);
		lotBindList.add(eventInfo.getEventName());
		lotBindList.add(eventInfo.getEventTimeKey());
		lotBindList.add(eventInfo.getEventTime());
		lotBindList.add(eventInfo.getEventUser());
		lotBindList.add(eventInfo.getEventComment());
		lotBindList.add(GenericServiceProxy.getConstantMap().Flag_N);
		lotBindList.add(eventInfo.getEventTime());
		lotBindList.add(eventInfo.getEventUser());
		lotBindList.add(lotData.getProcessOperationName());
		lotBindList.add(lotData.getProcessOperationVersion());
		lotBindList.add(machineName);
		lotBindList.add(portName);
		lotBindList.add(portType);
		lotBindList.add(portUseType);
		lotBindList.add("");
		lotBindList.add("");
		lotBindList.add(lotData.getKey().getLotName());
		
		return lotBindList;
	}
	
	private Lot setLotHistoryData(EventInfo eventInfo,Lot lotData, String machineName, String recipeName, Map<String, String> lotUdfs)
	{
		lotData.setLotState(GenericServiceProxy.getConstantMap().Lot_Released);
		lotData.setLotProcessState(GenericServiceProxy.getConstantMap().Lot_LoggedIn);
		lotData.setLastLoggedInTime(eventInfo.getEventTime());
		lotData.setLastLoggedInUser(eventInfo.getEventUser());
		lotData.setProcessOperationName(lotData.getProcessOperationName());
		lotData.setProcessOperationVersion(lotData.getProcessOperationVersion());
		lotData.setMachineName(machineName);
		lotData.setMachineRecipeName(recipeName);
		lotData.setCarrierName("");
		lotData.setLastEventName(eventInfo.getEventName());
		lotData.setLastEventTime(eventInfo.getEventTime());
		lotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		lotData.setLastEventComment(eventInfo.getEventComment());
		lotData.setLastEventUser(eventInfo.getEventUser());
		lotData.setUdfs(lotUdfs);
		
		return lotData;
	}
	
	private void panelAction(EventInfo eventInfo,List<Object[]> updateLotArgList, List<LotHistory> updateLotHistoryList) throws CustomException
	{
		eventInfo.setEventName("TrackIn");
		
		StringBuilder sqlLot = new StringBuilder();
		sqlLot.append("UPDATE LOT ");
		sqlLot.append("   SET LOTSTATE = ?, ");
		sqlLot.append("       LOTPROCESSSTATE = ?, ");
		sqlLot.append("       LASTEVENTNAME = ?, ");
		sqlLot.append("       LASTEVENTTIMEKEY = ?, ");
		sqlLot.append("       LASTEVENTTIME = ?, ");
		sqlLot.append("       LASTEVENTUSER = ?, ");
		sqlLot.append("       LASTEVENTCOMMENT = ?, ");
		sqlLot.append("       LASTEVENTFLAG = ?, ");
		sqlLot.append("       LASTLOGGEDINTIME = ?, ");
		sqlLot.append("       LASTLOGGEDINUSER = ?, ");
		sqlLot.append("       PROCESSOPERATIONNAME = ?, ");
		sqlLot.append("       PROCESSOPERATIONVERSION = ?, ");
		sqlLot.append("       MACHINENAME = ?, ");
		sqlLot.append("       PORTNAME = ?, ");
		sqlLot.append("       PORTTYPE = ?, ");
		sqlLot.append("       PORTUSETYPE = ?, ");
		sqlLot.append("       POSITION = ?, ");
		sqlLot.append("       CARRIERNAME = ? ");
		sqlLot.append(" WHERE LOTNAME = ? ");

		MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlLot.toString(), updateLotArgList);
		try 
		{
			CommonUtil.executeBatch("insert", updateLotHistoryList);
		} 
		catch (Exception e) 
		{
			log.error(e.getMessage());
			throw new CustomException(e.getCause());
		}
	}

	private List<Object> setDurableBindList(EventInfo eventInfo, Durable durableData, long lotQuantity)
	{
		List<Object> durableBindList = new ArrayList<Object>();
		
		durableBindList.add(durableData.getLotQuantity() - lotQuantity);
		
		if(lotQuantity == durableData.getLotQuantity())
		{
			durableBindList.add(GenericServiceProxy.getConstantMap().Dur_Available);
			durableBindList.add("");
			durableBindList.add("");
		}
		else
		{
			Map<String, String> udfs = durableData.getUdfs();
			String coverName = udfs.get("COVERNAME");
			String positionName = udfs.get("POSITIONNAME");
			
			durableBindList.add(durableData.getDurableState());
			durableBindList.add(coverName);
			durableBindList.add(positionName);
		}
		
		durableBindList.add(eventInfo.getEventName());
		durableBindList.add(eventInfo.getEventTimeKey());
		durableBindList.add(eventInfo.getEventTime());
		durableBindList.add(eventInfo.getEventUser());
		durableBindList.add(eventInfo.getEventComment());
		durableBindList.add(GenericServiceProxy.getConstantMap().Flag_N);
		durableBindList.add(durableData.getKey().getDurableName());
		
		return durableBindList;
	}
	
	private Durable setDurableHistoryData(EventInfo eventInfo, Durable durableData, long lotQuantity)
	{

		Map<String, String> durableUdfs = new HashMap<String, String>();
		
		long quantity = durableData.getLotQuantity();
		
		durableData.setLotQuantity(durableData.getLotQuantity() - lotQuantity);
		
		if(lotQuantity == quantity)
		{
			durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
			durableUdfs.put("COVERNAME", "");
			durableUdfs.put("POSITIONNAME", "");
		}
		else
		{
			Map<String, String> udfs = durableData.getUdfs();
			String coverName = udfs.get("COVERNAME");
			String positionName = udfs.get("POSITIONNAME");

			durableData.setDurableState(durableData.getDurableState());
			durableUdfs.put("COVERNAME", coverName);
			durableUdfs.put("POSITIONNAME", positionName);
		}
		
		durableData.setUdfs(durableUdfs);
		
		return durableData;
	}
	
	private void trayAction(EventInfo eventInfo, List<Object[]> updateDurableArgList, List<Durable> durableHistoryArgList, List<Durable> oldDurableListHistory) throws CustomException
	{
		eventInfo.setEventName("Deassign");
		
		StringBuilder sqlLot = new StringBuilder();
		sqlLot.append("UPDATE DURABLE ");
		sqlLot.append("   SET LOTQUANTITY = ?, ");
		sqlLot.append("       DURABLESTATE = ?, ");
		sqlLot.append("       COVERNAME = ?, ");
		sqlLot.append("       POSITIONNAME = ?, ");
		sqlLot.append("       LASTEVENTNAME = ?, ");
		sqlLot.append("       LASTEVENTTIMEKEY = ?, ");
		sqlLot.append("       LASTEVENTTIME = ?, ");
		sqlLot.append("       LASTEVENTUSER = ?, ");
		sqlLot.append("       LASTEVENTCOMMENT = ?, ");
		sqlLot.append("       LASTEVENTFLAG = ? ");
		sqlLot.append(" WHERE DURABLENAME = ? ");

		MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlLot.toString(), updateDurableArgList);
		MESDurableServiceProxy.getDurableServiceUtil().insertDurableHistory(eventInfo, durableHistoryArgList, oldDurableListHistory);
	}

	private List<Map<String, Object>> getOperationType(String factoryname, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion)
	{
		String sql = "SELECT TP.PROCESSOPERATIONNAME, TP.PROCESSOPERATIONVERSION,  " +
				"     PS.DETAILPROCESSOPERATIONTYPE  " +
				"FROM TPFOPOLICY TP, PROCESSOPERATIONSPEC PS  " +
				"WHERE     TP.FACTORYNAME = :FACTORYNAME  " +
				"    AND TP.PRODUCTSPECNAME = :PRODUCTSPECNAME  " +
				"    AND TP.PRODUCTSPECVERSION = :PRODUCTSPECVERSION  " +
				"    AND TP.PROCESSFLOWNAME = :PROCESSFLOWNAME  " +
				"    AND TP.PROCESSFLOWVERSION = :PROCESSFLOWVERSION " +
				"    AND TP.PROCESSOPERATIONNAME = PS.PROCESSOPERATIONNAME  " +
				"    AND TP.PROCESSOPERATIONVERSION = PS.PROCESSOPERATIONVERSION  " +
				"    AND TP.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME " +
				"    AND TP.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ";

		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", factoryname);
		args.put("PRODUCTSPECNAME", productSpecName);
		args.put("PRODUCTSPECVERSION", productSpecVersion);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);
		args.put("PROCESSOPERATIONNAME", processOperationName);
		args.put("PROCESSOPERATIONVERSION", processOperationVersion);
		
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);
		
		return result;
	}
}
