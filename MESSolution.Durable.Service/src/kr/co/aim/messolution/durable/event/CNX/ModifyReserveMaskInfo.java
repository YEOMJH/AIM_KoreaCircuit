package kr.co.aim.messolution.durable.event.CNX;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskList;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class ModifyReserveMaskInfo extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		String messageName = SMessageUtil.getMessageName(doc);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Change", this.getEventUser(), this.getEventComment(), "", "");
		Element eleBody = SMessageUtil.getBodyElement(doc);

		if (eleBody != null)
		{

			for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", false))
			{

				String machineName = SMessageUtil.getChildText(eledur, "MACHINENAME", true);
				String portName = SMessageUtil.getChildText(eledur, "PORTNAME", true);
				String maskName = SMessageUtil.getChildText(eledur, "MASKNAME", true);
				String carrierName = SMessageUtil.getChildText(eledur, "CARRIERNAME", true);

				String position = SMessageUtil.getChildText(eledur, "MASKPOSITION", false);
				String subUnitName = SMessageUtil.getChildText(eledur, "SUBUNITNAME", false);
				String sslotNo = SMessageUtil.getChildText(eledur, "SSLOTNO", false);
				String maskThickNess = SMessageUtil.getChildText(eledur, "MASKTHICKNESS", false);
				String maskMagnet = SMessageUtil.getChildText(eledur, "MASKMAGNET", false);
				String OffSet_X = SMessageUtil.getChildText(eledur, "OFFSET_X", false);
				String OffSet_Y = SMessageUtil.getChildText(eledur, "OFFSET_Y", false);
				String OffSet_T = SMessageUtil.getChildText(eledur, "OFFSET_T", false);
				String CarrierType = SMessageUtil.getChildText(eledur, "CARRIERTYPE", false);

				ReserveMaskList reserveMaskdataInfo = new ReserveMaskList(machineName, portName, maskName);
				reserveMaskdataInfo.setCarrierName(carrierName);
				reserveMaskdataInfo.setPosition(position);
				reserveMaskdataInfo.setSubUnitName(subUnitName);
				reserveMaskdataInfo.setSSlotNo(sslotNo);
				reserveMaskdataInfo.setMaskThickNess(maskThickNess);
				reserveMaskdataInfo.setMaskMagnet(maskMagnet);
				reserveMaskdataInfo.setOffSet_X(OffSet_X);
				reserveMaskdataInfo.setOffSet_Y(OffSet_Y);
				reserveMaskdataInfo.setOffSet_T(OffSet_T);
				reserveMaskdataInfo.setCarrierType(CarrierType);
				// Excute
				ExtendedObjectProxy.getReserveMaskService().modify(eventInfo, reserveMaskdataInfo);
			}
		}
		return doc;
	}

}
