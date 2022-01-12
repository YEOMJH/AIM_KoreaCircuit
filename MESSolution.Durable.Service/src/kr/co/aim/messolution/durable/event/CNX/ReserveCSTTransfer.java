package kr.co.aim.messolution.durable.event.CNX;
import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class ReserveCSTTransfer  extends SyncHandler{
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		List<Element> durableList = SMessageUtil.getBodySequenceItemList(doc, "DurableReserveTransferList", false);
		List<Object[]> updateinsertReserveCSTTransferList = new ArrayList<Object[]>();
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReserveCSTTransfer", getEventUser(), getEventComment(), "", "");
		Durable durableData=null;
		kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo =new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
		for (Element durable : durableList)
		{
			eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
			eventInfo.setEventTime(TimeUtils.getCurrentTimestamp());
			String durableName = SMessageUtil.getChildText(durable, "DURABLENAME", true);
		    durableData=MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
			String durableType=durableData.getDurableType();
			String durableSpec=durableData.getDurableSpecName();
			String durableSpecVersion=durableData.getDurableSpecVersion();
			String durableResevrState="Reserved";
			String durableState=durableData.getDurableState();
			String durableCleanSate=durableData.getDurableSpecName();
			
			List<Object> ReserveCSTTransferList = new ArrayList<Object>();
			ReserveCSTTransferList.add(durableName);
			ReserveCSTTransferList.add(durableType);
			ReserveCSTTransferList.add(durableSpec);
			ReserveCSTTransferList.add(durableSpecVersion);
			ReserveCSTTransferList.add(durableResevrState);
			ReserveCSTTransferList.add(durableState);
			ReserveCSTTransferList.add(durableCleanSate);
			ReserveCSTTransferList.add(eventInfo.getEventName());
			ReserveCSTTransferList.add(eventInfo.getEventTimeKey());
			ReserveCSTTransferList.add(eventInfo.getEventTime());
			ReserveCSTTransferList.add(eventInfo.getEventUser());
			ReserveCSTTransferList.add(eventInfo.getEventComment());
			updateinsertReserveCSTTransferList.add(ReserveCSTTransferList.toArray());
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
		}
		insertReserveCSTTransferData(updateinsertReserveCSTTransferList);
		return doc;
	}
	private void insertReserveCSTTransferData(List<Object[]> updateinsertReserveCSTTransferList) throws CustomException
	{
	 try
		{
		 StringBuffer sqlFA = new StringBuffer();
		 sqlFA.append("INSERT INTO CT_DURABLERESERVETRANSFER  ");
		 sqlFA.append(" (DURABLENAME,DURABLETYPE,DURABLESPECNAME,DURABLESPECVERSION,");
		 sqlFA.append(" DURABLERESERVESTATE,DURABLESTATE,DURABLECLEANSTATE,");
		 sqlFA.append(" LASTEVENTNAME,LASTEVENTTIMEKEY,LASTEVENTTIME,LASTEVENTUSER,LASTEVENTCOMMENT)");
		 sqlFA.append(" VALUES(?,?,?,?,?,?,?,?,?,?,?,?)");

		 MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlFA.toString(), updateinsertReserveCSTTransferList);
		 	
		}
		catch (CustomException e)
		{
			throw new CustomException("ReserveTransfer-00001");
		}
			
	}
}
