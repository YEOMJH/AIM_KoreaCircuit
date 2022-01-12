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

public class AutoClaveTrackIn extends SyncHandler {

	private static Log log = LogFactory.getLog(AutoClaveTrackIn.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
		String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
		String trayGroupName = SMessageUtil.getBodyItemValue(doc, "PROCESSGROUPNAME", true);

		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		// EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackIn", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());


		// SQL
		String queryStringLot = "UPDATE LOT SET LOTPROCESSSTATE = ?, LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, "
				+ "LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, LASTEVENTFLAG = ?, LASTLOGGEDINTIME = ?, LASTLOGGEDINUSER = ?, "
				+ "PORTNAME = ?, PORTTYPE = ?, PORTUSETYPE = ?, MACHINENAME = ? WHERE LOTNAME = ?";
		
		List<Map<String, Object>> lotList = MESLotServiceProxy.getLotServiceUtil().getLotListByTrayGroup(trayGroupName);

		List<Lot> lotDataList = getLotDataList(lotList);
		
		for (Lot lot : lotDataList) 
		{
			Lot oldLot = (Lot)ObjectUtil.copyTo(lot);

			CommonValidation.checkLotGradeN(lot);
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
			lotBindList.add(portName);
			lotBindList.add(portType);
			lotBindList.add(portUseType);
			lotBindList.add(machineName);
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
			lotUdf.put("PORTNAME", portName);
			lotUdf.put("PORTTYPE", portType);
			lotUdf.put("PORTUSETYPE", portUseType);
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
				!lotData.getProductRequestName().equals(groupLotData.getProductRequestName()) ||
				!lotData.getUdfs().get("LOTDETAILGRADE").equals(groupLotData.getUdfs().get("LOTDETAILGRADE")))
			{
				throw new CustomException("LOT-0020", lotData.getKey().getLotName(), groupLotData.getKey().getLotName());
			}
		}
		
		return lotDataList;
	}
}
