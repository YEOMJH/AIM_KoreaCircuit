package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.IPQCLot;
import kr.co.aim.messolution.extended.object.management.data.IPQCPanelJudge;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
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

public class IPQCTrackIn extends SyncHandler {

	private static Log log = LogFactory.getLog(IPQCTrackIn.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
		String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
		String trayGroupName = SMessageUtil.getBodyItemValue(doc, "FQCLOTNAME", true);
		String trayQty = SMessageUtil.getBodyItemValue(doc, "TRAYQUANTITY", true);
		String panelQty = SMessageUtil.getBodyItemValue(doc, "PANELQUANTITY", true);
		String productSpec = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String panelGrade = SMessageUtil.getBodyItemValue(doc, "PANELGRADE", true);
		String sampleRule = SMessageUtil.getBodyItemValue(doc, "SAMPLERULE", true);
		
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
				+ "PORTNAME = ?, PORTTYPE = ?, PORTUSETYPE = ?, MACHINENAME = ? WHERE LOTNAME = ?";
		
		List<Map<String, Object>> lotList = MESLotServiceProxy.getLotServiceUtil().getLotListByTrayGroup(trayGroupName);

		List<IPQCPanelJudge> ipqcPanelList = new ArrayList<>();
		
		String seq = ExtendedObjectProxy.getIPQCLotService().getIPQCLotSeq(trayGroupName);
		
		for (Map<String, Object> lotData : lotList) {
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.get("LOTNAME").toString());
			Lot oldLot = (Lot)ObjectUtil.copyTo(lot);
			
			//CommonValidation.checkLotGradeN(lot);
			
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
			lot.setMachineName(machineName);
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
			
			IPQCPanelJudge dataInfo = new IPQCPanelJudge();
			dataInfo.setIpqcLotName(trayGroupName);
			dataInfo.setSeq(Long.parseLong(seq));
			dataInfo.setPanelName(lot.getKey().getLotName());
			if(panelGrade.equals("S"))
			{
				dataInfo.setBeforeGrade("S");
			}
			else
			{
				dataInfo.setBeforeGrade(lot.getUdfs().get("LOTDETAILGRADE").toString());
			}
			dataInfo.setEventUser(eventInfo.getEventName());
			dataInfo.setEventTime(eventInfo.getEventTime());
			
			ipqcPanelList.add(dataInfo);
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
		
		IPQCLot ipqcLot = new IPQCLot();	
		
		ipqcLot.setIpqcLotName(trayGroupName);
		ipqcLot.setSeq(Long.parseLong(seq));
		ipqcLot.setProductSpec(productSpec);
		ipqcLot.setTrayQuantity(Integer.parseInt(trayQty));
		ipqcLot.setPanelQuantity(Integer.parseInt(panelQty));
		ipqcLot.setPanelGrade(panelGrade);
		ipqcLot.setLotState("Released");
		ipqcLot.setSampleRule(sampleRule);
		ipqcLot.setMachineName(machineName);
		ipqcLot.setLastEventComment(eventInfo.getEventComment());
		ipqcLot.setCreateUser(eventInfo.getEventUser());
		ipqcLot.setCreateTime(eventInfo.getEventTime());
		ipqcLot.setLastEventName(eventInfo.getEventName());
		ipqcLot.setLastEventTime(eventInfo.getEventTime());
		ipqcLot.setLastEventTimeKey(eventInfo.getEventTimeKey());
		ipqcLot.setLastEventUser(eventInfo.getEventUser());
		
		ExtendedObjectProxy.getIPQCLotService().create(eventInfo, ipqcLot);
		ExtendedObjectProxy.getIPQCPanelJudgeService().create(eventInfo, ipqcPanelList);
		
		return doc;
	}
}
