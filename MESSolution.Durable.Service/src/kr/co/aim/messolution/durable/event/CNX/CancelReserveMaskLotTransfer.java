package kr.co.aim.messolution.durable.event.CNX;

import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskTransfer;
import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class CancelReserveMaskLotTransfer extends SyncHandler {
	public Object doWorks(Document doc) throws CustomException
	{
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		List<Element> MaskList = SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", true);

		//Start 20210317 houxk
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		if(durableData.getUdfs().get("TRANSPORTLOCKFLAG") != null && durableData.getUdfs().get("TRANSPORTLOCKFLAG").equals("Y"))
		{
			throw new CustomException("MACHINE-0049");
		}
//		List<TransportJobCommand> commandList = ExtendedObjectProxy.getTransportJobCommand().select(" WHERE 1=1 AND CARRIERNAME =? ", new Object[] { carrierName });
//
//		if (commandList != null && commandList.size() > 0)
//		{
//			for (TransportJobCommand command : commandList)
//			{
//				if (StringUtil.in(command.getJobState(), "Started", "Requested", "Accepted")&&!StringUtil.in(command.getCancelState(), "Completed")&&!StringUtil.in(command.getTransportJobType(), "MCS"))
//					throw new CustomException("MACHINE-0032", command.getDestinationMachineName(), command.getDestinationPositionName(), command.getTransportJobName());
//			}
//		}
		//End
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelReserveMaskLotTransfer", this.getEventUser(), this.getEventComment());
		
		for (Element Mask : MaskList)
		{
			String position = SMessageUtil.getChildText(Mask, "POSITION", true);

			ReserveMaskTransfer ReserveMaskLotInfo = ExtendedObjectProxy.getReserveMaskTransferService().selectByKey(true, new Object[] { carrierName, position });
			
			ExtendedObjectProxy.getReserveMaskTransferService().remove(eventInfo, ReserveMaskLotInfo);

		}
		return doc;
	}

}