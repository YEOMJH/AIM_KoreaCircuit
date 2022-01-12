package kr.co.aim.messolution.durable.event.OledMask;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class OLEDMaskCSTProcessCanceled extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		List<Element> maskList = SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("FailTrackInMask", getEventUser(), getEventComment(), "", "");

		//common check
		List<MaskLot> maskLotList = ExtendedObjectProxy.getMaskLotService().getMaskLotByCarrier(carrierName, true);
		checkDataConsistency(maskLotList,maskList);

		for (MaskLot maskLotData : maskLotList)
		{
			if (maskLotData.getMaskLotProcessState().equals("RUN"))
			{
				//MASK-0102: Mask[{0}] is already started.
				throw new CustomException("MASK-0102", maskLotData.getMaskLotName());
			}
			
			ExtendedObjectProxy.getMaskLotService().setJobFlag(eventInfo, maskLotData.getMaskLotName(), "N");
		}

		// Update CANCELINFOFLAG for DSP
		DurableKey durableKey = new DurableKey(carrierName);
		kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfoDur = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
		setEventInfoDur.getUdfs().put("CANCELINFOFLAG", "Y");
		eventInfo.setEventName("FailTrackInMask");
		DurableServiceProxy.getDurableService().setEvent(durableKey, eventInfo, setEventInfoDur);
	}
	
	private void checkDataConsistency(List<MaskLot> maskDataList,List<Element> maskElementList) throws CustomException
	{
		List<String> mesMaskList = ExtendedObjectProxy.getMaskLotService().makeList(maskDataList);
		List<String> bcMaskList = CommonUtil.makeList(maskElementList, "MASKNAME");
		
		if (maskDataList.size() > 0 && "Y".equals(maskDataList.get(0).getJobDownFlag()))
		{
			if (!(maskDataList.size() == maskElementList.size() && bcMaskList.containsAll(mesMaskList)))
			{
				//MASK-0107: BC Reported Mask Quantity different with Mask Quantity of MES .
				throw new CustomException("MASK-0107");
			}
		}
	}
	
}
