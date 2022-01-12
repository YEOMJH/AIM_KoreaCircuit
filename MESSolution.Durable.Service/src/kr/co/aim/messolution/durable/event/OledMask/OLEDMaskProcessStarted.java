package kr.co.aim.messolution.durable.event.OledMask;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;

public class OLEDMaskProcessStarted extends AsyncHandler {

	private static Log log = LogFactory.getLog(OLEDMaskProcessStarted.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		String maskLotName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "MASKRECIPENAME",false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackInMask", getEventUser(), getEventComment(), "", "");
	
		//common check
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		CommonValidation.checkMachineHold(machineData);
		
		MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().getMaskLotData(maskLotName);
		
		//******************************2021/09/08 由于设备/BC消息上报错误，清洗机不对OLEDMaskProcessStarted进行处理**********************************//
		if(StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_MaskOrgCleaner) || 
				StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_MaskMetalCleaner))
		{
			log.info("Error message:Machine is:"+machineName+",but message is OLEDMaskProcessStarted" );
			return;
		}

		if (!StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_MaskUnpacker))
		{
			ExtendedObjectProxy.getMaskLotService().CheckCommonTrackIn(maskLotData);
			
			log.info(String.format("entry to [%s] at [%d]", "getMachineRecipe", System.currentTimeMillis()));
			log.info("Level [D] Recipe validation behavior begins");

			ListOrderedMap instruction = PolicyUtil.getMachineRecipeNameForOLEDMask(maskLotData.getFactoryName(), maskLotData.getMaskSpecName(), maskLotData.getMaskProcessFlowName()
					, maskLotData.getMaskProcessFlowVersion(), maskLotData.getMaskProcessOperationName(), maskLotData.getMaskProcessOperationVersion(), machineName);

		}

		
		if (StringUtil.isNotEmpty(portName))
			portName = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName).getKey().getPortName();

		// trackIn masklot
		if (!(StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_MaskUnpacker) && machineData.getUdfs().get("OPERATIONMODE").equals("PACKING")))
			ExtendedObjectProxy.getMaskLotService().maskMakeLoggedIn(eventInfo, maskLotName, machineData, portName, maskLotData.getMaskProcessOperationName(), recipeName);

		if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_EVA))
		{
			eventInfo.setEventName("DeassignCarrierMask");
			MESDurableServiceProxy.getDurableServiceImpl().durableStateChangeAfterOLEDMaskLotProcess(maskLotData.getCarrierName(), "DEASSIGN", "1", eventInfo);
		}
		
		//change portInfo
		MESPortServiceProxy.getPortServiceUtil().portProcessing(eventInfo, machineName, portName);

	}

}
