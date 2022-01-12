package kr.co.aim.messolution.durable.event.OledMask;

import java.util.List;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

public class OLEDMaskCSTProcessStarted extends AsyncHandler 
{
	private static Log log = LogFactory.getLog(OLEDMaskCSTProcessStarted.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		List<Element> maskLotList = SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackInMask", getEventUser(), getEventComment(), "", "");
		
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		CommonValidation.checkMachineHold(machineData);
		
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);

		for (int i = 0; i < maskLotList.size(); i++)
		{
			String maskLotName = maskLotList.get(i).getChildText("MASKNAME");
			String machineRecipeName = maskLotList.get(i).getChildText("MASKRECIPENAME");
			MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });
			
			log.info(String.format("entry to [%s] at [%d]", "getMachineRecipe", System.currentTimeMillis()));
			log.info("Level [D] Recipe validation behavior begins");

			ListOrderedMap instruction = PolicyUtil.getMachineRecipeNameForOLEDMask(maskLotData.getFactoryName(), maskLotData.getMaskSpecName(), maskLotData.getMaskProcessFlowName()
					, maskLotData.getMaskProcessFlowVersion(), maskLotData.getMaskProcessOperationName(), maskLotData.getMaskProcessOperationVersion(), machineName);


			// Track In Mask Lot
			ExtendedObjectProxy.getMaskLotService().maskMakeLoggedIn(eventInfo, maskLotName, machineData, portName, maskLotData.getMaskProcessOperationName(), machineRecipeName);
		}

		// DeAssign Carrier if the mask is assigned to a carrier.
		EventInfo eventInfoDeassignCarrierMask = EventInfoUtil.makeEventInfo("DeassignCarrierMask", this.getEventUser(), this.getEventComment(), "", "", "Y");

		MESDurableServiceProxy.getDurableServiceImpl().durableStateChangeAfterOLEDMaskLotProcess(carrierName, "DEASSIGN", String.valueOf(maskLotList.size()), eventInfoDeassignCarrierMask);

		// change portInfo
		MESPortServiceProxy.getPortServiceUtil().portProcessing(eventInfo, machineName, portName);

		Durable cstData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		if (cstData.getUdfs().get("CANCELINFOFLAG").equals("Y"))
		{
			kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfoDur = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
			setEventInfoDur.getUdfs().put("CANCELINFOFLAG", "");

			DurableServiceProxy.getDurableService().setEvent(cstData.getKey(), eventInfo, setEventInfoDur);
		}

	}

}
