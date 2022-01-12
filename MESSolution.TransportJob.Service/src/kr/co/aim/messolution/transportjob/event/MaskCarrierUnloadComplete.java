package kr.co.aim.messolution.transportjob.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskToEQP;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskTransfer;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetAreaInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;

public class MaskCarrierUnloadComplete extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX -> FMC]
	 * 
	 * <Body>
	 *    <CARRIERNAME />
	 *    <MACHINENAME />
	 *    <PORTNAME />
	 * </Body>
	 */
	
	@SuppressWarnings("unchecked")
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Unload", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);

		// Get MachineSpec Data
		MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);

		// change carrier TransferState & LocationInfo
		Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

		Map<String, String> carrierUdfs = new HashMap<String, String>();
		carrierUdfs.put("MACHINENAME", "");
		carrierUdfs.put("PORTNAME", "");
		carrierUdfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_MOVING);
		carrierUdfs.put("POSITIONTYPE", "");
		carrierUdfs.put("POSITIONNAME", "");
		carrierUdfs.put("ZONENAME", "");

		SetAreaInfo setAreaInfo = new SetAreaInfo();
		setAreaInfo.setAreaName("");
		setAreaInfo.setUdfs(carrierUdfs);

		EventInfo setAreaEventInfo = EventInfoUtil.makeEventInfo(eventInfo.getEventName(), eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
		DurableServiceProxy.getDurableService().setArea(carrierData.getKey(), setAreaEventInfo, setAreaInfo);

		// change portInfo
		MESPortServiceProxy.getPortServiceUtil().unLoadComplete(eventInfo, machineName, portName);

		// append full state
		Element eleFullState = new Element("FULLSTATE");
		eleFullState.setText("EMPTY");
		SMessageUtil.getBodyElement(doc).addContent(eleFullState);

		// success then report to FMB
		GenericServiceProxy.getESBServive().sendBySenderToFMB(doc);

		// Remove CT_ReserveMaskTransfer/CT_ReserveMaskToEQP Data
		if (StringUtils.equals(carrierData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_InUse) &&
			StringUtils.equals(machineSpecData.getMachineType(), kr.co.aim.greentrack.generic.GenericServiceProxy.getConstantMap().Mac_StorageMachine))
		{
			try
			{
				String sql = "SELECT MASKLOTNAME FROM CT_RESERVEMASKTRANSFER WHERE CARRIERNAME = :CARRIERNAME AND MASKLOTNAME IS NOT NULL ";

				Map<String, String> args = new HashMap<String, String>();
				args.put("CARRIERNAME", carrierName);

				List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

				if (result.size() > 0)
				{
					List<ReserveMaskTransfer> transferList = ExtendedObjectProxy.getReserveMaskTransferService().select(" carrierName = ? ", new Object[] { carrierName });

					if (transferList.size() > 0)
					{
						ExtendedObjectProxy.getReserveMaskTransferService().remove(eventInfo, transferList);
					}
				}
			}
			catch (Exception e)
			{
				eventLog.info("Error Occurred - Delete CT_ReserveMaskTransfer");
			}
			
			try
			{
				String sqlSrting = "SELECT MASKLOTNAME FROM CT_RESERVEMASKTOEQP WHERE CARRIERNAME = :CARRIERNAME AND MASKLOTNAME IS NOT NULL ";

				Map<String, String> args = new HashMap<String, String>();
				args.put("CARRIERNAME", carrierName);

				List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlSrting, args);

				if (result.size() > 0)
				{
					List<ReserveMaskToEQP> transferList = ExtendedObjectProxy.getReserveMaskToEQPService().select(" carrierName = ? ", new Object[] { carrierName });

					if (transferList.size() > 0)
					{
						ExtendedObjectProxy.getReserveMaskToEQPService().remove(eventInfo, transferList);
					}
				}
			}
			catch (Exception e)
			{
				eventLog.info("Error Occurred - Delete CT_ReserveMaskToEQP");
			}
		}
	}
}