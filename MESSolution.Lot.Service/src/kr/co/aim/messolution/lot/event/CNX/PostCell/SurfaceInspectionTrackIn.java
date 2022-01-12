package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;

public class SurfaceInspectionTrackIn extends SyncHandler {

	private static Log log = LogFactory.getLog(SurfaceInspectionTrackIn.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String trayGroupName = SMessageUtil.getBodyItemValue(doc, "TRAYGROUPNAME", true);

		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		// EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackIn", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

		// SQL
		String queryStringLot = "UPDATE LOT SET LOTPROCESSSTATE = ?, LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, "
				+ "LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, LASTEVENTFLAG = ?, LASTLOGGEDINTIME = ?, LASTLOGGEDINUSER = ?, "
				+ "PORTNAME = ?, PORTTYPE = ?, PORTUSETYPE = ? WHERE LOTNAME = ?";
		
		List<Map<String, Object>> lotList = MESLotServiceProxy.getLotServiceUtil().getLotListByTrayGroup(trayGroupName);
		if (lotList == null || lotList.size() == 0)
		{
			//TRAY-0008:TrayGroup is Empty. TrayGroupName = {0}
			throw new CustomException("TRAY-0008" , trayGroupName);
		}
		
		List<Lot> lotDataList = getLotDataList(lotList);
		
		for (Lot lot : lotDataList) 
		{
			Lot oldLot = (Lot)ObjectUtil.copyTo(lot);

			List<Map<String, Object>> detailOperationInfo = getDetailOperationType(lot.getFactoryName(), lot.getProcessOperationName(), lot.getProcessOperationVersion());
			String detailOperationType = detailOperationInfo.get(0).get("DETAILPROCESSOPERATIONTYPE").toString();
			
			if(!(detailOperationType.equals("SecondSurfaceTest") || detailOperationType.equals("SurfaceTest") || detailOperationType.equals("FQCSurfaceTest")))
			{
				throw new CustomException("PANEL-0002", lot.getProcessOperationName(), lot.getKey().getLotName());
			}
			if(detailOperationType.equals("SecondSurfaceTest")||detailOperationType.equals("FQCSurfaceTest") )
			{
				CommonValidation.checkLotGradeNotG(lot);	
			}
			if(detailOperationType.equals("SurfaceTest"))
			{
				checkLotGrade(lot);	
			}
				
			
			CommonValidation.checkLotProcessState(lot);
			CommonValidation.checkLotHoldState(lot);
			
			List<Object> lotBindList = new ArrayList<Object>();
			
			lotBindList.add(constantMap.Lot_Run);
			lotBindList.add(eventInfo.getEventName());
			lotBindList.add(eventInfo.getEventTimeKey());
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(eventInfo.getEventComment());
			lotBindList.add(constantMap.Flag_N);
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add("");
			lotBindList.add("");
			lotBindList.add("");
			lotBindList.add(lot.getKey().getLotName());

			updateLotArgList.add(lotBindList.toArray());

			// History
			lot.setLotProcessState(constantMap.Lot_Run);
			lot.setLastLoggedInTime(eventInfo.getEventTime());
			lot.setLastLoggedInUser(eventInfo.getEventUser());
			lot.setLastEventName(eventInfo.getEventName());
			lot.setLastEventTime(eventInfo.getEventTime());
			lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lot.setLastEventComment(eventInfo.getEventComment());
			lot.setLastEventUser(eventInfo.getEventUser());

			Map<String, String> lotUdf = new HashMap<>();
			lotUdf = lot.getUdfs();
			lotUdf.put("PORTNAME", "");
			lotUdf.put("PORTTYPE", "");
			lotUdf.put("PORTUSETYPE", "");
			lot.setUdfs(lotUdf);

			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
			
			updateLotHistoryList.add(lotHistory);
		}
		MESLotServiceProxy.getLotServiceUtil().updateBatch(queryStringLot, updateLotArgList);
		try 
		{
			CommonUtil.executeBatch("insert", updateLotHistoryList);
		} 
		catch (Exception e) 
		{
			log.error(e.getMessage());
			throw new CustomException(e.getCause());
		}

		return doc;
	}
	
	public static void checkLotGrade(Lot lotData) throws CustomException
	{
		if (!lotData.getLotGrade().equals("G")&&!lotData.getLotGrade().equals("R"))
		{
			throw new CustomException("LOT-0321", lotData.getKey().getLotName(), lotData.getLotGrade());
		}
	}
	private List<Map<String, Object>> getDetailOperationType(String factoryname, String processOperationName, String processOperationVersion)
	{
		String sql = "SELECT PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION, DETAILPROCESSOPERATIONTYPE  " +
				"FROM PROCESSOPERATIONSPEC " +
				"WHERE PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME " +
				"    AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION " +
				"    AND FACTORYNAME = :FACTORYNAME ";

		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", factoryname);
		args.put("PROCESSOPERATIONNAME", processOperationName);
		args.put("PROCESSOPERATIONVERSION", processOperationVersion);
		
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);
		
		return result;
	}
	
	private List<Lot> getLotDataList(List<Map<String, Object>> lotList) throws CustomException
	{
		String condition = "WHERE LOTNAME IN(";
		for (Map<String, Object> lotMap : lotList) 
		{
			String lotName = lotMap.get("LOTNAME").toString();
			
			condition += "'" + lotName + "',";
		}
		condition = condition.substring(0, condition.length() - 1) + ")";
		
		List<Lot> lotDataList = LotServiceProxy.getLotService().select(condition, new Object[] { });
		
		Lot groupLotData = lotDataList.get(0);
		
		for (Lot lotData : lotDataList) 
		{
			if (!lotData.getFactoryName().equals(groupLotData.getFactoryName()) ||
				!lotData.getProductSpecName().equals(groupLotData.getProductSpecName()) ||
				!lotData.getProductSpecVersion().equals(groupLotData.getProductSpecVersion()) ||
				!lotData.getProcessFlowName().equals(groupLotData.getProcessFlowName()) ||
				!lotData.getProcessFlowVersion().equals(groupLotData.getProcessFlowVersion()) ||
				!lotData.getProcessOperationName().equals(groupLotData.getProcessOperationName()) ||
				!lotData.getProcessOperationVersion().equals(groupLotData.getProcessOperationVersion()) ||
				!lotData.getProductionType().equals(groupLotData.getProductionType()) ||
				!lotData.getProductRequestName().equals(groupLotData.getProductRequestName()))
			{
				throw new CustomException("LOT-0020", lotData.getKey().getLotName(), groupLotData.getKey().getLotName());
			}
		}
		
		return lotDataList;
	}
}
