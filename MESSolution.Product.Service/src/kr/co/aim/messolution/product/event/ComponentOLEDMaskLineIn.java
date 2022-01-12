package kr.co.aim.messolution.product.event;

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

import org.jdom.Document;

public class ComponentOLEDMaskLineIn extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		/*
		 	MACHINENAME 
			UNITNAME
			SUBUNITNAME
			MASKNAME
			FRAMENAME
		 */
		String machineName = SMessageUtil.getBodyItemValue( doc, "MACHINENAME", true );
		String unitName = SMessageUtil.getBodyItemValue( doc, "UNITNAME", true );
		String subUnitName = SMessageUtil.getBodyItemValue( doc, "SUBUNITNAME", false );
		String maskName = SMessageUtil.getBodyItemValue( doc, "MASKNAME", true );
		String frameName = SMessageUtil.getBodyItemValue( doc, "FRAMENAME", false );

		//Check Mask
		MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey( false, new Object[]{ maskName });

		//Check Machine
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);

		//Insert to Component History
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ComponentOLEDMaskLineIn", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		MaskLotComponentHistory dataInfo = new MaskLotComponentHistory();
		dataInfo.setTimeKey(eventInfo.getEventTimeKey());
		dataInfo.setMachineName( machineName );
		dataInfo.setMaskLotName( maskName );
		dataInfo.setEventName(eventInfo.getEventName());
		dataInfo.setMaskLotJudge( maskLotData.getMaskLotJudge() );
//		dataInfo.setMaskGroupName( maskGroupName );
		dataInfo.setMaterialLocationName( unitName );
		dataInfo.setEventTime( eventInfo.getEventTime() );
		dataInfo.setEventUser( eventInfo.getEventUser() );
		dataInfo.setFactoryName( maskLotData.getFactoryName() );
		dataInfo.setMaskProcessOperationName( maskLotData.getMaskProcessOperationName() );
		dataInfo.setMaskProcessFlowName( maskLotData.getMaskProcessFlowName() );
		dataInfo.setMaskSpecName( maskLotData.getMaskSpecName() );
		dataInfo.setProductionType( maskLotData.getProductionType() );
		dataInfo.setFrameName(frameName);
		
		ExtendedObjectProxy.getMaskLotComponentHistoryService().create(eventInfo, dataInfo);

	}
}

