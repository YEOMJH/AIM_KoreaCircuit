package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
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
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;

public class TrayMappingAssign extends SyncHandler {

	private static Log log = LogFactory.getLog(TrayMappingAssign.class);
	
	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		//Get Doc
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", false);
		String durableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		String durableCapacity= SMessageUtil.getBodyItemValue(doc, "DURABLECAPACITY", true);
		int lotQty = lotList.size();
		
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		//EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Assign", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		//SQL
		String queryStringLot = "UPDATE LOT SET LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, "
				+ "LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, LASTEVENTFLAG = ?, POSITION = ?, CARRIERNAME = ? WHERE LOTNAME = ?";	
		
		//Make Panel
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();
		
		String operationName = null;
		String operationVersion = null;
		String lotGrade = null;
		
		for (Element lotData : lotList)
		{
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getChildText("LOTNAME"));
			Lot oldLot = (Lot)ObjectUtil.copyTo(lot);
			//CommonValidation.checkLotProcessState(lot);
			CommonValidation.checkLotHoldState(lot);
			
			if(!lot.getCarrierName().isEmpty())
			{
				throw new CustomException("PANEL-0005", lot.getKey().getLotName() ,lot.getCarrierName());
			}
			
			if(operationName == null)
			{
				operationName = oldLot.getProcessOperationName();
				operationVersion = oldLot.getProcessOperationVersion();
				lotGrade = oldLot.getLotGrade();
			}
			else
			{
				CommonValidation.checkSameOperation(operationName, oldLot.getProcessOperationName(), operationVersion, oldLot.getProcessOperationVersion());
				CommonValidation.checkSameLotGrade(lotGrade, oldLot.getLotGrade());
			}
			
			String position = lotData.getChildText("POSITION");
			
			List<Object> lotBindList = new ArrayList<Object>();
			
			lotBindList.add(eventInfo.getEventName());
			lotBindList.add(eventInfo.getEventTimeKey());
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(eventInfo.getEventComment());
			lotBindList.add(constantMap.Flag_N);
			lotBindList.add(position);
			lotBindList.add(durableName);
			lotBindList.add(lotData.getChildText("LOTNAME"));
			
			updateLotArgList.add(lotBindList.toArray());
			
			//History
			lot.setCarrierName(durableName);
			lot.setLastEventName(eventInfo.getEventName());
			lot.setLastEventTime(eventInfo.getEventTime());
			lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lot.setLastEventComment(eventInfo.getEventComment());
			lot.setLastEventUser(eventInfo.getEventUser());
			
			Map<String, String> lotUdfs = new HashMap<>();
			lotUdfs.put("POSITION", position);
			lot.setUdfs(lotUdfs);
			
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
		
		//Durable		
		Durable olddurableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
		Durable durableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
	    String trayCoverName = olddurableInfo.getUdfs().get("COVERNAME");
	    //{start caixu 2020/11/12 if Tray is Available and  hase  CoverTary Can not  Assign Panel
		if (olddurableInfo.getLotQuantity() == 0)
        {
          if(!StringUtil.isEmpty(trayCoverName)&&!trayCoverName.equals(durableName))
          {
             throw new CustomException("DURABLE-9014", "");
          }
        }//end}
        CommonValidation.CheckDurableState(olddurableInfo);
		CommonValidation.CheckDurableState(olddurableInfo);
		
		List<Map<String, Object>> lotListByTrayName = MESLotServiceProxy.getLotServiceUtil().getLotListByTray(durableName);
		int capacity= Integer.parseInt(durableCapacity);
		if(lotListByTrayName.size() > capacity)
		{
			throw new CustomException("DURABLE-1001", durableName);
		}
		
		durableInfo.setLotQuantity(durableInfo.getLotQuantity() + lotQty);
		durableInfo.setDurableState(constantMap.Dur_InUse);
		durableInfo.setLastEventName(eventInfo.getEventName());
		durableInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
		durableInfo.setLastEventTime(eventInfo.getEventTime());
		durableInfo.setLastEventUser(eventInfo.getEventUser());
		durableInfo.setLastEventComment(eventInfo.getEventComment());
		
		DurableHistory durHistory = new DurableHistory();
		durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(olddurableInfo, durableInfo, durHistory);
		
		DurableServiceProxy.getDurableService().update(durableInfo);
		DurableServiceProxy.getDurableHistoryService().insert(durHistory);
		
		
		//Change TrayGroup Quantity
		
		String coverName = durableInfo.getUdfs().get("COVERNAME");
		if(!StringUtil.isEmpty(coverName)&&!trayCoverName.equals(durableName)){
//			List<Durable> trayList = MESDurableServiceProxy.getDurableServiceUtil().getTrayListByCoverName(coverName);
//			for(Durable trayInfo : trayList){
//				totalLotQty+=trayInfo.getLotQuantity();
//			}
			Durable oldCoverInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(coverName);
			Durable coverInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(coverName);
			coverInfo.setLotQuantity(coverInfo.getLotQuantity()+lotQty);
			coverInfo.setDurableState(constantMap.Dur_InUse);
			coverInfo.setLastEventName(eventInfo.getEventName());
			coverInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
			coverInfo.setLastEventTime(eventInfo.getEventTime());
			coverInfo.setLastEventUser(eventInfo.getEventUser());
			coverInfo.setLastEventComment(eventInfo.getEventComment());
			
			DurableHistory coverHistory = new DurableHistory();
			coverHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldCoverInfo, coverInfo, coverHistory);
			
			DurableServiceProxy.getDurableService().update(coverInfo);
			DurableServiceProxy.getDurableHistoryService().insert(coverHistory);
		}
		
		return doc;
	}
}
