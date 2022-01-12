package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
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
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.master.EnumDef;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class CommonTrackIn extends SyncHandler {

	private static Log log = LogFactory.getLog(CommonTrackIn.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		Element body = SMessageUtil.getBodyElement(doc);

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
		String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
		String panelName = SMessageUtil.getBodyItemValue(doc, "PANELNAME", false);

		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackIn", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		// TrayList
		List<String> durableNameList = CommonUtil.makeList(body, "DURABLELIST", "DURABLENAME");
		
		//List<List<Map<String, Object>>> lotNotReleasedList = getPanelNotReleased(body, eventInfo, durableNameList);    //inquiry not released panel
		//List<List<Map<String, Object>>> lotRunList = getPanelProcessState(body, eventInfo, durableNameList);            //inquiry run panel
		// Tray Event - DeAssign
		
		
		List<List<Map<String, Object>>> lotnameList = deassignTrayAndMakeLotList(body, eventInfo, durableNameList,panelName);
		
		//Check ERPBOM 2020-10-15
		String firstLotName = ConvertUtil.getMapValueByName(lotnameList.get(0).get(0), "LOTNAME"); 
		
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(firstLotName);
		
		ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());
		
		/*if(StringUtil.isNotEmpty(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
		{
			MESConsumableServiceProxy.getConsumableServiceUtil().compareERPBOM(lotData.getFactoryName(), productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME"), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineName, lotData.getProductSpecName());
		}*/
		
		
		eventInfo.setEventName("TrackIn");

		List<MVIPanelJudge> mviPanelList = new ArrayList<>();
		String detailOperationType = "";
		
		if (lotnameList != null && lotnameList.size() > 0)
		{
			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("PORTNAME", portName);
			udfs.put("PORTTYPE", portType);
			udfs.put("PORTUSETYPE", portUseType);

			for (List<Map<String, Object>> list : lotnameList)
			{
				for (Map<String, Object> map : list)
				{
					String lotName = ConvertUtil.getMapValueByName(map, "LOTNAME");

					Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
					Lot oldLot = (Lot) ObjectUtil.copyTo(lot);
					
					CommonValidation.checkLotProcessState(lot);
					CommonValidation.checkLotHoldState(lot);
					CommonValidation.checkLotState(lot);
					
					if(detailOperationType.isEmpty())
					{
						List<Map<String, Object>> detailOperationInfo = getDetailOperationType(lot.getFactoryName(), lot.getProcessOperationName(), lot.getProcessOperationVersion());
						detailOperationType = detailOperationInfo.get(0).get("DETAILPROCESSOPERATIONTYPE").toString();
					}
					
					if(!detailOperationType.equals("SVI/MVI") && !detailOperationType.equals("MCT"))
					{
						CommonValidation.checkLotGradeN(lot);
					}
					
					Map<String,String> processLimitEnum = ExtendedObjectProxy.getPanelProcessCountService().getProcessLimitConfiguration("ProcessLimit");
					
					if (processLimitEnum != null && StringUtil.in(detailOperationType, processLimitEnum.keySet().toArray(new String[]{})));
					{
						ExtendedObjectProxy.getPanelProcessCountService().checkPanelProcessCount(lotName, detailOperationType);
					}

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
					lotBindList.add(lot.getProcessOperationName());
					lotBindList.add(lot.getProcessOperationVersion());
					lotBindList.add(machineName);
					lotBindList.add(portName);
					lotBindList.add(portType);
					lotBindList.add(portUseType);
					lotBindList.add("");
					lotBindList.add("");
					lotBindList.add(lot.getKey().getLotName());
					updateLotArgList.add(lotBindList.toArray());

					// History
					lot.setLotState(GenericServiceProxy.getConstantMap().Lot_Released);
					lot.setLotProcessState(GenericServiceProxy.getConstantMap().Lot_LoggedIn);
					lot.setLastLoggedInTime(eventInfo.getEventTime());
					lot.setLastLoggedInUser(eventInfo.getEventUser());
					lot.setProcessOperationName(lot.getProcessOperationName());
					lot.setProcessOperationVersion(lot.getProcessOperationVersion());
					lot.setMachineName(machineName);
					lot.setMachineRecipeName(machineRecipeName);
					lot.setCarrierName("");
					lot.setLastEventName(eventInfo.getEventName());
					lot.setLastEventTime(eventInfo.getEventTime());
					lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
					lot.setLastEventComment(eventInfo.getEventComment());
					lot.setLastEventUser(eventInfo.getEventUser());
					
					udfs.put("POSITION", "");				
					lot.setUdfs(udfs);

					LotHistory lotHistory = new LotHistory();
					lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
					
					updateLotHistoryList.add(lotHistory);
					/*
					List<Map<String, Object>> operationData = getOperationType(oldLot.getFactoryName(), oldLot.getProductSpecName(), 
							oldLot.getProductSpecVersion(), oldLot.getProcessFlowName() , oldLot.getProcessFlowVersion(), 
							oldLot.getProcessOperationName(), oldLot.getProcessOperationVersion());
					*/
					if(detailOperationType.equals("SVI/MVI"))
					{
						MVIPanelJudge mviData = new MVIPanelJudge();
						
						String seq = ExtendedObjectProxy.getMVIPanelJudgeService().getMVIPanelSeqV2(oldLot.getKey().getLotName());
						
						mviData.setPanelName(oldLot.getKey().getLotName());
						mviData.setSeq(Long.parseLong(seq));
						if(oldLot.getLotGrade().equals("G"))
						{
							mviData.setBeforeGrade(oldLot.getUdfs().get("LOTDETAILGRADE").toString());
						}
						else
						{
							mviData.setBeforeGrade(oldLot.getLotGrade());
						}
						mviData.setEventUser(eventInfo.getEventUser());
						mviData.setEventTime(eventInfo.getEventTime());
						mviData.setLastEventTimeKey(eventInfo.getEventTimeKey());
						mviData.setLastLoggedInTime(eventInfo.getEventTime());
						mviData.setLastLoggedOutTime(null);
						
						mviPanelList.add(mviData);
					}
				}
			}
			
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
			
			if(mviPanelList.size() > 0)
			{
				ExtendedObjectProxy.getMVIPanelJudgeService().create(eventInfo, mviPanelList);
			}
		}

		return doc;
	}

	private List<List<Map<String, Object>>> deassignTrayAndMakeLotList(Element body, EventInfo eventInfo, List<String> durableNameList,String panelName) throws CustomException
	{
	
	 List<List<Map<String, Object>>> lotNameList = new ArrayList<List<Map<String, Object>>>();
	 if(durableNameList.size()>0&&!durableNameList.isEmpty())
	 {
		 //{caixu 2020/11/26 MVI BCR Flag is Y TrackIn
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT L.LOTNAME ");
		sql.append("  FROM DURABLE DR, LOT L ");
		sql.append(" WHERE DR.DURABLENAME = L.CARRIERNAME ");
		sql.append("   AND DR.FACTORYNAME = L.FACTORYNAME ");
		sql.append("   AND DR.DURABLESTATE = 'InUse' ");
		sql.append("   AND DR.DURABLETYPE = 'Tray' ");
		sql.append("   AND DR.COVERNAME = :PROCESSGROUPNAME ");
		sql.append("   AND DR.COVERNAME IS NOT NULL ");
		sql.append("   AND L.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND L.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("   AND L.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND L.CARRIERNAME = :CARRIERNAME ");
		sql.append("UNION ALL ");
		sql.append("SELECT L.LOTNAME ");
		sql.append("  FROM DURABLE DR, LOT L ");
		sql.append("   WHERE DR.DURABLENAME = L.CARRIERNAME ");
		sql.append("   AND DR.FACTORYNAME = L.FACTORYNAME ");
		sql.append("   AND DR.DURABLESTATE = 'InUse' ");
		sql.append("   AND DR.DURABLETYPE = 'CoverTray' ");
		sql.append("   AND DR.COVERNAME = DURABLENAME ");
		sql.append("   AND DR.BCRFLAG='Y'");
		sql.append("   AND DR.COVERNAME = :PROCESSGROUPNAME ");
		sql.append("   AND DR.COVERNAME IS NOT NULL ");
		sql.append("   AND L.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND L.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("   AND L.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND L.CARRIERNAME = :CARRIERNAME ");//}
		String coverName = "";

		if (durableNameList.size() > 0)
		{
			String tsql = " SELECT COVERNAME FROM DURABLE WHERE DURABLENAME = :TRAYNAME ";

			Map<String, Object> bindMap2 = new HashMap<String, Object>();
			bindMap2.put("TRAYNAME", durableNameList.get(0).toString());

			List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(tsql, bindMap2);

			if (result.size() == 1)
			{
				coverName = ConvertUtil.getMapValueByName(result.get(0), "COVERNAME");
				CommonValidation.checkTrayGroupHoldState(coverName);
			}
		}

		for (String trayName : durableNameList)
		{
			// putParam
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("PROCESSGROUPNAME", body.getChildText("PROCESSGROUPNAME"));
			bindMap.put("FACTORYNAME", body.getChildText("FACTORYNAME"));
			bindMap.put("PRODUCTSPECNAME", body.getChildText("PRODUCTSPECNAME"));
			bindMap.put("PROCESSOPERATIONNAME", body.getChildText("PROCESSOPERATIONNAME"));
			bindMap.put("CARRIERNAME", trayName);

			// judge EventName
			if (StringUtils.equals("TrackIn", eventInfo.getEventName()))
			{
				bindMap.put("MACHINENAME", body.getChildText("MACHINENAME"));
			}

			// getLotData
			List<Map<String, Object>> lotNames = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

			lotNameList.add(lotNames);

			// judge LotData
			if (lotNameList != null && lotNameList.size() > 0)
			{
				eventInfo.setEventName("Deassign");

				// SetDeassignEventInfo
				Map<String, String> udfs = new HashMap<String, String>();
				udfs.put("COVERNAME", "");
				udfs.put("POSITION", "");
				udfs.put("BCRFLAG", "N");
				udfs.put("DURABLETYPE1", "Tray");
				// Durable - Tray
				Durable oldDurableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
				Durable durableInfo = (Durable) ObjectUtil.copyTo(oldDurableInfo);
				DurableSpec durableSpecData = GenericServiceProxy.getSpecUtil().getDurableSpec(durableInfo.getFactoryName(), durableInfo.getDurableSpecName(),
						durableInfo.getDurableSpecVersion());
				
				durableInfo.setLotQuantity(0);
				durableInfo.setCapacity(durableSpecData.getDefaultCapacity());
				durableInfo.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
				durableInfo.setLastEventName(eventInfo.getEventName());
				durableInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
				durableInfo.setLastEventTime(eventInfo.getEventTime());
				durableInfo.setLastEventUser(eventInfo.getEventUser());
				durableInfo.setLastEventComment(eventInfo.getEventComment());
				durableInfo.setUdfs(udfs);

				DurableHistory durHistory = new DurableHistory();
				durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldDurableInfo, durableInfo, durHistory);

				DurableServiceProxy.getDurableService().update(durableInfo);
				DurableServiceProxy.getDurableHistoryService().insert(durHistory);
			}
		}

		// Durable - CoverTray
		Durable oldDurableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(coverName);
		Durable durableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(coverName);
		DurableSpec durableSpecData = GenericServiceProxy.getSpecUtil().getDurableSpec(durableInfo.getFactoryName(), durableInfo.getDurableSpecName(),
				durableInfo.getDurableSpecVersion());
		
		eventInfo.setEventName("DeassignTray");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		Map<String, String> udfs = new HashMap<String, String>();
		udfs.put("COVERNAME", "");
		udfs.put("POSITION", "");
		udfs.put("BCRFLAG", "N");
		udfs.put("DURABLETYPE1", "Tray");
		durableInfo.setLotQuantity(0);
		durableInfo.setCapacity(durableSpecData.getDefaultCapacity());
		durableInfo.setDurableType("Tray");
		durableInfo.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
		durableInfo.setLastEventName(eventInfo.getEventName());
		durableInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
		durableInfo.setLastEventTime(eventInfo.getEventTime());
		durableInfo.setLastEventUser(eventInfo.getEventUser());
		durableInfo.setLastEventComment(eventInfo.getEventComment());
		durableInfo.setUdfs(udfs);

		DurableHistory durHistory = new DurableHistory();
		durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldDurableInfo, durableInfo, durHistory);

		DurableServiceProxy.getDurableService().update(durableInfo);
		DurableServiceProxy.getDurableHistoryService().insert(durHistory);
	    }
		else//MVI 临时作业修改
		{
			
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT L.LOTNAME ");
			sql.append("  FROM  LOT L ");
			sql.append(" WHERE  L.FACTORYNAME = :FACTORYNAME");
			sql.append("   AND L.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
			sql.append("   AND L.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
			sql.append("   AND L.LOTNAME = :LOTNAME ");
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("PROCESSGROUPNAME", body.getChildText("PROCESSGROUPNAME"));
			bindMap.put("FACTORYNAME", body.getChildText("FACTORYNAME"));
			bindMap.put("PRODUCTSPECNAME", body.getChildText("PRODUCTSPECNAME"));
			bindMap.put("PROCESSOPERATIONNAME", body.getChildText("PROCESSOPERATIONNAME"));
			bindMap.put("PROCESSOPERATIONNAME", body.getChildText("PROCESSOPERATIONNAME"));
			bindMap.put("LOTNAME", panelName);

			if (StringUtils.equals("TrackIn", eventInfo.getEventName()))
			{
				bindMap.put("MACHINENAME", body.getChildText("MACHINENAME"));
			}
			
			List<Map<String, Object>> lotNames = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
			lotNameList.add(lotNames);
		}
	    if (lotNameList == null || lotNameList.size() == 0)
		{
			throw new CustomException("TRAY-0001", durableNameList);
		}
	    else
	    {
		
			return lotNameList;
	    }
		
	}
	
	private List<Map<String, Object>> getDetailOperationType(String factoryname, String processOperationName, String processOperationVersion)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION, DETAILPROCESSOPERATIONTYPE ");
		sql.append("  FROM PROCESSOPERATIONSPEC ");
		sql.append(" WHERE PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		sql.append("   AND FACTORYNAME = :FACTORYNAME ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", factoryname);
		args.put("PROCESSOPERATIONNAME", processOperationName);
		args.put("PROCESSOPERATIONVERSION", processOperationVersion);
		
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		
		return result;
	}
}
