package kr.co.aim.messolution.durable.event.OledMask;

import java.util.List;

import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.MaskMaterial;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class OLEDMaskPackingReport extends AsyncHandler {

	private static Log log = LogFactory.getLog(OLEDMaskPackingReport.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String maskLotName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String boxName = SMessageUtil.getBodyItemValue(doc, "BOXNAME", true);

		// common check
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		CommonValidation.checkMachineHold(machineData);
		
		MaskLot maskLotData=  ExtendedObjectProxy.getMaskLotService().getMaskLotData(maskLotName);
		
		// cancel create
		if (maskLotData.getMaskLotState().equals(GenericServiceProxy.getConstantMap().Lot_Created))
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelCreateMaskLot", this.getEventUser(), this.getEventComment(), "", "");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			maskLotData.setBoxName(boxName);
			ExtendedObjectProxy.getMaskLotService().remove(eventInfo, maskLotData);
		}
		else
		{
			// set packing info
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("AssignBox", this.getEventUser(), this.getEventComment(), "", "");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			maskLotData.setBoxName(boxName);
			ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotData);
		}
	}
	
	private void checkMaskLotState(MaskLot maskLotData) throws CustomException
	{
		ConstantMap constMap = GenericServiceProxy.getConstantMap();

		if (!maskLotData.getMaskLotState().equals(constMap.MaskLotState_Released))
		{
			throw new CustomException("MASK-0026", maskLotData.getMaskLotName(), maskLotData.getMaskLotProcessState());
		}
		if (!maskLotData.getMaskLotProcessState().equals(constMap.MaskLotProcessState_Run))
		{
			throw new CustomException("MASK-0026", maskLotData.getMaskLotName(), maskLotData.getMaskLotProcessState());
		}
		if (maskLotData.getMaskLotHoldState().equals(constMap.MaskLotHoldState_OnHold))
		{
			throw new CustomException("MASK-0013", maskLotData.getMaskLotName());
		}
	}
	
	private void deassignMaskMaterial(EventInfo eventInfo, String maskLotName) throws CustomException
	{
		List<MaskMaterial> materialDataList = null;

		try
		{
			materialDataList = ExtendedObjectProxy.getMaskMaterialService().select(" WHERE 1=1 MASKLOTNAME = :MASKLOTNAME", new Object[] { maskLotName });
		}
		catch (greenFrameDBErrorSignal dbError)
		{
			if (dbError.getErrorCode().equals(ErrorSignal.NotFoundSignal))
			{
				log.info("MaskMaterial Data Information Is Not Exist. Search by MaskLotName = " + maskLotName);
				return;
			}

			throw new CustomException(dbError.getCause());
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		ExtendedObjectProxy.getMaskMaterialService().remove(eventInfo, materialDataList);
	}
}
