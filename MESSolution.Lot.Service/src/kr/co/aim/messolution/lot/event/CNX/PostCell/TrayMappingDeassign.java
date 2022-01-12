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
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;

public class TrayMappingDeassign extends SyncHandler {

	private static Log log = LogFactory.getLog(CancelTrackIn.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		// Get Doc
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", false);
		String durableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		// EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Deassign", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

		// SQL
		String queryStringLot = "UPDATE LOT SET LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, "
				+ "LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, LASTEVENTFLAG = ?, POSITION = ?, CARRIERNAME = ? WHERE LOTNAME = ?";

		// Make Panel
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();
		Map<String, String> udfs = new HashMap<String, String>();
		for (Element lotData : lotList)
		{
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getChildText("LOTNAME"));
			Lot oldLot = (Lot)ObjectUtil.copyTo(lot);
			CommonValidation.checkLotProcessState(lot);
			CommonValidation.checkLotHoldState(lot);
			String oldCarrierName = lot.getCarrierName();
			if (!oldCarrierName.equals(durableName))
			{
				throw new CustomException("PROCESSGROUP-0009", lot.getKey(), oldCarrierName, durableName);
			}

			if (lot.getCarrierName().isEmpty())
			{
				throw new CustomException("DURABLE-9003", "");
			}

			List<Object> lotBindList = new ArrayList<Object>();

			lotBindList.add(eventInfo.getEventName());
			lotBindList.add(eventInfo.getEventTimeKey());
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(eventInfo.getEventComment());
			lotBindList.add(constantMap.Flag_N);
			lotBindList.add("");
			lotBindList.add("");
			lotBindList.add(lotData.getChildText("LOTNAME"));

			updateLotArgList.add(lotBindList.toArray());

			// History
			lot.setLastEventName(eventInfo.getEventName());
			lot.setLastEventTime(eventInfo.getEventTime());
			lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lot.setLastEventComment(eventInfo.getEventComment());
			lot.setLastEventUser(eventInfo.getEventUser());
			lot.setCarrierName("");
			
			Map<String, String> lotUdfs = new HashMap<>();
			lotUdfs.put("POSITION", "");
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

		// Durable
		Durable olddurableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
		Durable durableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
		DurableSpec durableSpecData = GenericServiceProxy.getSpecUtil().getDurableSpec(durableInfo.getFactoryName(), durableInfo.getDurableSpecName(),
				durableInfo.getDurableSpecVersion());
		String trayCoverName = olddurableInfo.getUdfs().get("COVERNAME");
		CommonValidation.CheckDurableState(olddurableInfo);

		if (durableInfo.getLotQuantity() - lotList.size() == 0)
		{//{started caixu 2020/11/12  if Tray is InUse and  hase  CoverTary Can not  Dssign Panel
		  if(!StringUtil.isEmpty(trayCoverName)&&!trayCoverName.equals(durableName))
	      {
	        throw new CustomException("DURABLE-9015", "");
	      }//End}
		  udfs.put("BCRFLAG", constantMap.Flag_N);
		  udfs.put("COVERNAME", "");
	      udfs.put("POSITION", "");
		  durableInfo.setDurableState(constantMap.Dur_Available);
		  durableInfo.setCapacity(durableSpecData.getDefaultCapacity());
		  durableInfo.setDurableType("Tray");
		  durableInfo.setUdfs(udfs);
		}
		durableInfo.setLotQuantity(durableInfo.getLotQuantity() - lotList.size());
		durableInfo.setLastEventName(eventInfo.getEventName());
		durableInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
		durableInfo.setLastEventTime(eventInfo.getEventTime());
		durableInfo.setLastEventUser(eventInfo.getEventUser());
		durableInfo.setLastEventComment(eventInfo.getEventComment());

		DurableHistory durHistory = new DurableHistory();
		durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(olddurableInfo, durableInfo, durHistory);

		DurableServiceProxy.getDurableService().update(durableInfo);
		DurableServiceProxy.getDurableHistoryService().insert(durHistory);
		return doc;
	}
}
