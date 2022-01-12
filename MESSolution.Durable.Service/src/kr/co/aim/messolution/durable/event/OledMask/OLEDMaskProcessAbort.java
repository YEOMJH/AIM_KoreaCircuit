package kr.co.aim.messolution.durable.event.OledMask;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.MaskMultiHold;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class OLEDMaskProcessAbort extends AsyncHandler {

	private static Log log = LogFactory.getLog(OLEDMaskProcessAbort.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String maskLotName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String position = SMessageUtil.getBodyItemValue(doc, "POSITION", false);

		Element bodyElement = doc.getRootElement().getChild(SMessageUtil.Body_Tag);

		MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().getMaskLotData(true, maskLotName);
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

		// MaskCleaner Buffer
		if (StringUtils.isEmpty(portName) && (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_MaskOrgCleaner) 
											|| StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_MaskMetalCleaner)))
		{
			// update processingInfo
			maskLotData.setProcessIngInfo(bodyElement.getChildText("PROCESSINGINFO"));
			ExtendedObjectProxy.getMaskLotService().update(maskLotData);
						
			// Cancel Track In Mask Lot
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelTrackInMask", getEventUser(), getEventComment());
			ExtendedObjectProxy.getMaskLotService().maskCancelTrackInWithOutCarrier(eventInfo, maskLotName,position);

			// hold mask lot
			EventInfo holdEventInfo = EventInfoUtil.makeEventInfo("HoldMask", this.getEventUser(), this.getEventComment(), "SYSTEM", "CancelTrackInMask");
		    ExtendedObjectProxy.getMaskLotService().maskMultiHold(holdEventInfo, maskLotName);
		}
		else
		{
			Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);

			if (StringUtil.equals(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), "PU"))
			{
				if (!carrierName.equals(StringUtil.EMPTY))
				{
					throw new CustomException("MASK-0032", maskLotName);
				}

				EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOutMask", this.getEventUser(), this.getEventComment());

				maskLotData = ExtendedObjectProxy.getMaskLotService().TrackOutMaskLot(eventInfo, maskLotData, bodyElement, "N");
				ExtendedObjectProxy.getMaskLotService().executePostAction(eventInfo, maskLotData);
			}
			else
			{
				// Cancel Track In Mask Lot
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelTrackInMask", getEventUser(), getEventComment());
				
				// update processingInfo
				maskLotData.setProcessIngInfo(bodyElement.getChildText("PROCESSINGINFO"));
				ExtendedObjectProxy.getMaskLotService().update(maskLotData);
				
				if (!CommonValidation.isNullOrEmpty(carrierName) && !carrierName.equals(maskLotName))
				{
					ExtendedObjectProxy.getMaskLotService().maskCancelTrackIn(eventInfo, maskLotName, carrierName, position);
				}
				else
				{
					ExtendedObjectProxy.getMaskLotService().maskCancelTrackInWithOutCarrier(eventInfo, maskLotName,position);
				}

				// hold mask lot
				EventInfo holdEventInfo = EventInfoUtil.makeEventInfo("HoldMask", this.getEventUser(), this.getEventComment(), "SYSTEM", "CancelTrackInMask");
				ExtendedObjectProxy.getMaskLotService().maskMultiHold(holdEventInfo, maskLotName);
			}
		}
	}

	private void checkMaskMultiHold(String masklotname, String reasonCode) throws CustomException
	{
		StringBuffer inquirysql = new StringBuffer();
		inquirysql.append(" SELECT MASKLOTNAME ");
		inquirysql.append(" FROM CT_MASKMULTIHOLD ");
		inquirysql.append(" WHERE MASKLOTNAME=:MASKLOTNAME ");
		inquirysql.append(" AND REASONCODE=:REASONCODE ");

		Map<String, String> inquirybindMap = new HashMap<String, String>();
		inquirybindMap.put("MASKLOTNAME", masklotname);
		inquirybindMap.put("REASONCODE", reasonCode);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(inquirysql.toString(), inquirybindMap);
		if (sqlResult != null && sqlResult.size() > 0)
		{
			throw new CustomException("MASK-0082", masklotname, reasonCode);
		}
	}

	private void MultiHoldMaskLot(String maskLotName, MaskLot maskLotData, EventInfo eventInfo) throws CustomException
	{
		MaskMultiHold maskMultiHold = new MaskMultiHold();
		maskMultiHold.setMaskLotName(maskLotName);
		maskMultiHold.setFactoryName(maskLotData.getFactoryName());
		maskMultiHold.setMaskProcessOperationName(maskLotData.getMaskProcessOperationName());
		maskMultiHold.setMaskProcessOperationVersion(maskLotData.getMaskProcessOperationVersion());
		maskMultiHold.setReasonCode(eventInfo.getReasonCode());
		maskMultiHold.setReasonCodeType(eventInfo.getReasonCodeType());
		maskMultiHold.setLastEventComment(eventInfo.getEventComment());
		maskMultiHold.setLastEventName(eventInfo.getEventName());
		maskMultiHold.setLastEventTime(eventInfo.getEventTime());
		maskMultiHold.setLastEventUser(eventInfo.getEventUser());

		ExtendedObjectProxy.getMaskMultiHoldService().create(eventInfo, maskMultiHold);
	}
}
