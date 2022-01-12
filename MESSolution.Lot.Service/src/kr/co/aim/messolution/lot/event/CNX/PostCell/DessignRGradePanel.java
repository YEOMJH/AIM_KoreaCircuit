package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.management.impl.LotQueueTimeService;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
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
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;


public class DessignRGradePanel extends SyncHandler {

	public static Log logger = LogFactory.getLog(LotQueueTimeService.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "PANELLIST", true);
		
		for(Element element : lotList) 
		{
			String lotName = element.getChildText("LOTNAME");
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
			
			List<ProductU> productUSequence = new ArrayList<ProductU>();
			
			
			// 1.SourceLot Deassign With SourceCST
			if (StringUtil.isNotEmpty(lotData.getCarrierName()))
			{
				//Tray
				Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());
				
				DeassignCarrierInfo createInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, durableData, productUSequence);
				
				eventInfo.setEventName("Deassign Panel");
				
				//CoverTray
				Durable durableInfo = MESDurableServiceProxy.getDurableServiceUtil()
						.getDurableData(durableData.getUdfs().get("COVERNAME"));
				
				Durable olddurableInfo = (Durable)ObjectUtil.copyTo(durableInfo);

				durableInfo.setLotQuantity(durableInfo.getLotQuantity() - 1);
				
				if (durableInfo.getLotQuantity() == 0)
					durableInfo.setDurableState("Available");
				
				durableInfo.setLastEventName(eventInfo.getEventName());
				durableInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
				durableInfo.setLastEventTime(eventInfo.getEventTime());
				durableInfo.setLastEventUser(eventInfo.getEventUser());
				durableInfo.setLastEventComment(eventInfo.getEventComment());

				DurableHistory durHistory = new DurableHistory();
				durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(olddurableInfo, durableInfo,
						durHistory);

				DurableServiceProxy.getDurableService().update(durableInfo);
				DurableServiceProxy.getDurableHistoryService().insert(durHistory);

				MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, createInfo, eventInfo);
				lotData.setCarrierName("");
			}
		}
		return doc;
	}
	
	
}