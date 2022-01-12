package kr.co.aim.messolution.transportjob.event.CNX;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import org.jdom.Document;
import org.jdom.Element;


public class CancelTransportJobReserve extends SyncHandler {

	@Override
	
	public Object doWorks(Document doc) throws CustomException
	{
	
		List<Element> durableList = SMessageUtil.getBodySequenceItemList(doc, "CSTLIST", true);
		List<Object[]> CancelReserveCSTTransferList= new ArrayList<Object[]>();
		
		for (Element durable : durableList)
			{
				String durableName = SMessageUtil.getChildText(durable, "DURABLENAME", true);
				List<Object> ReserveCSTTransferList = new ArrayList<Object>();
				ReserveCSTTransferList.add(durableName);
				CancelReserveCSTTransferList.add(ReserveCSTTransferList.toArray());
			}
		
		TransportJobReserve(CancelReserveCSTTransferList);
		return doc;
	}
		public void TransportJobReserve(List<Object[]> CancelReserveCSTTransferList )throws CustomException
		{
			try
			{
			 String queryStrigDeleteReserve="DELETE FROM CT_DURABLERESERVETRANSFER WHERE DURABLENAME=:DURABLENAME";

			 MESLotServiceProxy.getLotServiceUtil().updateBatch(queryStrigDeleteReserve, CancelReserveCSTTransferList);
			 	
			}
			catch (CustomException e)
			{
				throw new CustomException("ReserveTransfer-00001");
			}
		
		}
    }

	
		

