package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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

public class SecondSurfaceInspectionTrackIn extends SyncHandler {

	private static Log log = LogFactory.getLog(SecondSurfaceInspectionTrackIn.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String trayGroupName = SMessageUtil.getBodyItemValue(doc, "TRAYGROUPNAME", true);

		List<Lot> updateLotArgList = new ArrayList<Lot>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		// EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackIn", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		List<Map<String, Object>> lotList = MESLotServiceProxy.getLotServiceUtil().getLotListByTrayGroup(trayGroupName);
		
		List<Lot> lotDataList = getLotDataList(lotList);
		
		for (Lot lot : lotDataList) 
		{
			Lot oldLot = (Lot)ObjectUtil.copyTo(lot);

			List<Map<String, Object>> detailOperationInfo = getDetailOperationType(lot.getFactoryName(), lot.getProcessOperationName(), lot.getProcessOperationVersion());
			String detailOperationType = detailOperationInfo.get(0).get("DETAILPROCESSOPERATIONTYPE").toString();
			
			if(!(detailOperationType.equals("SurfaceTest") && StringUtils.equals(lot.getProcessOperationName(), "35020")))
			{
				throw new CustomException("PANEL-0002", lot.getProcessOperationName(), lot.getKey().getLotName());
			}
			
			CommonValidation.checkLotProcessState(lot);
			CommonValidation.checkLotHoldState(lot);
			
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

			updateLotArgList.add(lot);

			// History
			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
			
			updateLotHistoryList.add(lotHistory);
		}

		try 
		{
			CommonUtil.executeBatch("update", updateLotArgList);
			CommonUtil.executeBatch("insert", updateLotHistoryList);
		} 
		catch (Exception e) 
		{
			log.error(e.getMessage());
			throw new CustomException(e.getCause());
		}

		return doc;
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
				!lotData.getProductRequestName().equals(groupLotData.getProductRequestName()))
			{
				throw new CustomException("LOT-0020", lotData.getKey().getLotName(), groupLotData.getKey().getLotName());
			}
		}
		
		return lotDataList;
	}
}
