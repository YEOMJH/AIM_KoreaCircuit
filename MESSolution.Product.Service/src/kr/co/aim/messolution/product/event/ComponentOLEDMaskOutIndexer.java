package kr.co.aim.messolution.product.event;

import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.MaskLotComponentHistory;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;

public class ComponentOLEDMaskOutIndexer extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		/*
		 	MACHINENAME 
			UNITNAME
			SUBUNITNAME
			MASKGROUPNAME
			MASKNAME
			FROMSLOTID
			TOSLOTID
			PORTNAME
			CARRIERNAME

		 */
		
		
		String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true );
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true );
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true );
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false );
//		String maskGroupName = SMessageUtil.getBodyItemValue(doc, "MASKGROUPNAME", false );
		String fromSlotId = SMessageUtil.getBodyItemValue(doc, "FROMSLOTID", false );
		String toSlotId = SMessageUtil.getBodyItemValue(doc, "TOSLOTID", false );
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false );
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false );

		//Check Mask Lot
		MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[]{maskName});
		
	
		//Check Machine 
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
				
	
		//Insert to MASKLOTCOMPONENTHISTORY 
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ComponentOLEDMaskOutIndexer", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
				
				
		MaskLotComponentHistory dataInfo = new MaskLotComponentHistory();
		dataInfo.setTimeKey(eventInfo.getEventTimeKey());
		dataInfo.setMaskLotName( maskName );
		dataInfo.setEventName(eventInfo.getEventName());
		dataInfo.setMaskLotJudge( maskLotData.getMaskLotJudge() );
//		dataInfo.setMaskGroupName( maskGroupName );
		dataInfo.setMachineName( machineName );
		dataInfo.setCarrierName( carrierName );
		dataInfo.setMaterialLocationName( unitName );
		dataInfo.setFromSlotId( fromSlotId );
		dataInfo.setToSlotId( toSlotId );
		dataInfo.setEventTime( eventInfo.getEventTime() );
		dataInfo.setEventUser( eventInfo.getEventUser() );
		dataInfo.setFactoryName( maskLotData.getFactoryName() );
		dataInfo.setMaskProcessOperationName( maskLotData.getMaskProcessOperationName() );
		dataInfo.setMaskProcessFlowName( maskLotData.getMaskProcessFlowName() );
		dataInfo.setMaskSpecName( maskLotData.getMaskSpecName() );
		dataInfo.setProductionType( maskLotData.getProductionType() );

		ExtendedObjectProxy.getMaskLotComponentHistoryService().create(eventInfo, dataInfo);

	}
}
