package kr.co.aim.messolution.durable.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskToEQP;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

import com.sun.xml.internal.ws.util.StringUtils;

public class ChangeOLEDMaskLot extends SyncHandler {
	public Object doWorks(Document doc) throws CustomException
	{
		// Mask Info
		String maskLotName = SMessageUtil.getBodyItemValue(doc, "MASKLOTNAME", true);
		String cleanUsedLimit = SMessageUtil.getBodyItemValue(doc, "CLEANUSEDLIMIT", false);
		String timeUsedLimit = SMessageUtil.getBodyItemValue(doc, "TIMEUSEDLIMIT", false);
		String timeUsed = SMessageUtil.getBodyItemValue(doc, "TIMEUSED", false);
		String offSetX = SMessageUtil.getBodyItemValue(doc, "INITIALOFFSETX", false);
		String offSetY = SMessageUtil.getBodyItemValue(doc, "INITIALOFFSETY", false);
		String offSetT = SMessageUtil.getBodyItemValue(doc, "INITIALOFFSETTHETA", false);
		String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);
		String thickness = SMessageUtil.getBodyItemValue(doc, "THICKNESS", false);
		String durationUsedLimit = SMessageUtil.getBodyItemValue(doc, "DURATIONUSEDLIMIT", false);
		String magnet = SMessageUtil.getBodyItemValue(doc, "MAGNET", false);
		String priority = SMessageUtil.getBodyItemValue(doc, "PRIORITY", true);
		String maskLotState = SMessageUtil.getBodyItemValue(doc, "MASKLOTSTATE", true);
		String maskLotJudge = SMessageUtil.getBodyItemValue(doc, "MASKLOTJUDGE", true);
		//add MaskFlowState --Loong
		String maskFlowState = SMessageUtil.getBodyItemValue(doc, "MASKFLOWSTATE", false);

		// Reserve Info
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String position = SMessageUtil.getBodyItemValue(doc, "POSITION", false);
		
		//TFE Offset
		String offsetX1 = SMessageUtil.getBodyItemValue(doc, "TFEOFFSETX1", false);
		String offsetY1 = SMessageUtil.getBodyItemValue(doc, "TFEOFFSETY1", false);
		String offsetX2 = SMessageUtil.getBodyItemValue(doc, "TFEOFFSETX2", false);
		String offsetY2 = SMessageUtil.getBodyItemValue(doc, "TFEOFFSETY2", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeOLEDMaskLot", this.getEventUser(), this.getEventComment(), "", "");
		String timeKey = TimeUtils.getCurrentEventTimeKey();
		eventInfo.setEventTimeKey(timeKey);
		eventInfo.setLastEventTimekey(timeKey);

		MaskLot maskLotData = null;

		try
		{
			maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });
		}
		catch (greenFrameDBErrorSignal nfdes)
		{
			throw new CustomException("DURABLE-5050", maskLotName);
		}
		//Validation MaskHold&MaskRun --Loong
		if(StringUtil.equals(maskLotData.getMaskLotHoldState(), "Y")||!StringUtil.equals(maskLotData.getMaskLotProcessState(), "WAIT"))
		{					
			throw new CustomException("MASKLOT-00001", maskLotName);
		}

		// Set Mask Info
		maskLotData.setCleanUsedLimit(Float.parseFloat(cleanUsedLimit));
		maskLotData.setTimeUsedLimit(Float.parseFloat(timeUsedLimit));
		maskLotData.setTimeUsed(Float.parseFloat(timeUsed));
		maskLotData.setInitialOffSetX(offSetX);
		maskLotData.setInitialOffSetY(offSetY);
		maskLotData.setInitialOffSetTheta(offSetT);
		maskLotData.setMachineRecipeName(machineRecipeName);
		maskLotData.setMaskThickness(thickness);
		maskLotData.setDurationUsedLimit(Float.parseFloat(durationUsedLimit));
		maskLotData.setMagnet(Float.parseFloat(magnet));
		maskLotData.setPriority(Float.parseFloat(priority));
		maskLotData.setMaskLotState(maskLotState);
		maskLotData.setMaskLotJudge(maskLotJudge);
		//add MaskFlowState
		maskLotData.setMaskFlowState(maskFlowState);
		maskLotData.setTFEOFFSETX1(offsetX1);
		maskLotData.setTFEOFFSETY1(offsetY1);
		maskLotData.setTFEOFFSETX2(offsetX2);
		maskLotData.setTFEOFFSETY2(offsetY2);

		ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotData);

		if (StringUtil.isEmpty(position))
			return doc;

		String condition = " WHERE MASKLOTNAME = ? ";
		Object[] bindSet = new Object[] { maskLotName };
		List<ReserveMaskToEQP> originalReservedSearchedByMask = null;
		try
		{
			originalReservedSearchedByMask = ExtendedObjectProxy.getReserveMaskToEQPService().select(condition, bindSet);
		}
		catch (greenFrameDBErrorSignal nfdes)
		{
		}

		if (originalReservedSearchedByMask != null)
		{
			for (ReserveMaskToEQP reserved : originalReservedSearchedByMask)
			{
				if (StringUtil.equals(machineName, reserved.getMachineName()) && StringUtil.equals(unitName, reserved.getUnitName()) && StringUtil.equals(subUnitName, reserved.getSubUnitName())
						&& StringUtil.equals(position, reserved.getPosition()))
					continue;
				
				ExtendedObjectProxy.getReserveMaskToEQPService().remove(eventInfo, reserved);
			}
		}

		if (StringUtil.isEmpty(position))
			throw new CustomException("MASK-0050");

		if (StringUtil.isEmpty(machineName) || StringUtil.isEmpty(unitName) || StringUtil.isEmpty(subUnitName) || StringUtil.isEmpty(carrierName))
			return doc;

		// Set Reserve Info
		ReserveMaskToEQP originalReserved = null;
		try
		{
			originalReserved = ExtendedObjectProxy.getReserveMaskToEQPService().selectByKey(true, new Object[] { machineName, unitName, subUnitName, carrierName, position });
		}
		catch (greenFrameDBErrorSignal nfdes)
		{
		} // Not found reserved same stage

		if (originalReserved != null) // The stage is already reserved
		{
			originalReserved.setMaskLotName(maskLotName);
			originalReserved.setLastEventName(eventInfo.getEventName());
			originalReserved.setLastEventTime(eventInfo.getEventTime());
			originalReserved.setLastEventTimekey(eventInfo.getLastEventTimekey());
			originalReserved.setLastEventUser(getEventUser());
			originalReserved.setLastEventComment(getEventComment());

			ExtendedObjectProxy.getReserveMaskToEQPService().modify(eventInfo, originalReserved);
		}
		else
		// The stage is already not reserved
		{
			ReserveMaskToEQP newReserved = new ReserveMaskToEQP();
			newReserved.setMachineName(machineName);
			newReserved.setUnitName(unitName);
			newReserved.setSubUnitName(subUnitName);
			newReserved.setCarrierName(carrierName);
			newReserved.setPosition(position);
			newReserved.setMaskLotName(maskLotName);
			newReserved.setLastEventName(eventInfo.getEventName());
			newReserved.setLastEventTime(eventInfo.getEventTime());
			newReserved.setLastEventTimekey(eventInfo.getLastEventTimekey());
			newReserved.setLastEventUser(getEventUser());
			newReserved.setLastEventComment(getEventComment());

			ExtendedObjectProxy.getReserveMaskToEQPService().create(eventInfo, newReserved);
		}

		return doc;
	}
}