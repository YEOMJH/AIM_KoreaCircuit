package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

import org.jdom.Document;
import org.jdom.Element;

public class CancelCreateLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);

		for (Element eleLot : lotList)
		{
			try
			{
				String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
				Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

				if (!lotData.getLotState().equals(GenericServiceProxy.getConstantMap().Lot_Created))
					throw new CustomException("LOT-0016", lotData.getKey().getLotName(), lotData.getLotState());

				try
				{
					List<ReserveLot> result = ExtendedObjectProxy.getReserveLotService().select(" lotName = ? ", new Object[] { lotName });

					for (ReserveLot dataInfo : result)
					{
						eventInfo.setEventName("CancelCreate");
						ExtendedObjectProxy.getReserveLotService().remove(eventInfo, dataInfo);
					}
				}
				catch (Exception ex)
				{
				}

				//Mantis 0000041
				SetEventInfo setEventInfo = new SetEventInfo();
				LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);

				//LotServiceProxy.getLotService().remove(lotData.getKey());
				removeLot(lotData);

				//Mantis 0000041
				ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);
				eventInfo.setEventName("DecrementCreatedQuantity");
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().DecrementCreatedQuantityBy(productRequestData, Integer.parseInt(lotData.getUdfs().get("PLANPRODUCTQUANTITY")), eventInfo);
			}
			catch (Exception ex)
			{
				eventLog.warn(ex);
			}
		}

		return doc;
	}

	private void removeLot(Lot lotData)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("DELETE FROM LOT ");
		sql.append(" WHERE LOTNAME = :LOTNAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("LOTNAME", lotData.getKey().getLotName());

		GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), args);
	}
}
