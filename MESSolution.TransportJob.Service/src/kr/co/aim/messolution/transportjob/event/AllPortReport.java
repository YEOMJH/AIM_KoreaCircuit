package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeAccessModeInfo;
import kr.co.aim.greentrack.port.management.info.MakePortStateByStateInfo;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

public class AllPortReport extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX -> FMC]
	 * 
	 * <Body>
	 *    <MACHINENAME />
	 *    <PORTTLIST>
	 *       <PORT>
	 *          <PORTNAME />
	 *          <PORTSTATE />
	 *          <PORTACCESSMODE />
	 *          <PORTTYPE />
	 *       </PORT>
	 *    <PORTTLIST>
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		List<Element> portList = SMessageUtil.getBodySequenceItemList(doc, "PORTLIST", true);

		for (Element elePort : portList)
		{
			try
			{
				String sPortName = SMessageUtil.getChildText(elePort, "PORTNAME", true);
				String sPortStateName = SMessageUtil.getChildText(elePort, "PORTSTATE", true);
				String sPortAccessMode = SMessageUtil.getChildText(elePort, "PORTACCESSMODE", true);
				String sPortType = SMessageUtil.getChildText(elePort, "PORTTYPE", true);

				EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
				Map<String, String> udfs = new HashMap<String, String>();

				Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(sMachineName, sPortName);

				// change port state
				eventInfo.setEventName("ChangeState");
				MakePortStateByStateInfo makePortStateByStateInfo = MESPortServiceProxy.getPortInfoUtil().makePortStateByStateInfo(portData, sPortStateName);
				MESPortServiceProxy.getPortServiceImpl().makePortStateByState(portData, makePortStateByStateInfo, eventInfo);

				// change port access mode
				eventInfo.setEventName("ChangeAccessMode");
				MakeAccessModeInfo makeAccessModeInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, sPortAccessMode);
				MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, makeAccessModeInfo, eventInfo);

				// change port type
				if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals(sPortType))
				{
					eventLog.warn(String.format("Attribute[%s] is still [%s]", "PORTTYPE", sPortType));
				}
				else
				{
					eventInfo.setEventName("ChangeType");

					SetEventInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(udfs);
					transitionInfo.getUdfs().put("PORTTYPE", sPortType);
					MESPortServiceProxy.getPortServiceImpl().setEvent(portData, transitionInfo, eventInfo);
				}
			}
			catch (CustomException ce)
			{
				eventLog.warn(ce.getLocalizedMessage());
			}
		}

		// success then report to FMB
		GenericServiceProxy.getESBServive().sendBySenderToFMB(doc);
	}
}
