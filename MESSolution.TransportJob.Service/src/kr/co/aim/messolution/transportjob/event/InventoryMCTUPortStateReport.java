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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class InventoryMCTUPortStateReport extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX -> FMC]
	 * 
	 * <Body>
	 *    <PORTLIST>
	 *       <PORT>
	 *          <MACHINENAME />
	 *          <PORTNAME />
	 *          <PORTTRANSFERSTATE />
	 *          <CARRIERNAME />
	 *          <PORTFULLEMPTY />
	 *          <PORTACCESSMODE />
	 *       </PORT>
	 *    </PORTLIST>
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		List<Element> portList = SMessageUtil.getBodySequenceItemList(doc, "PORTLIST", true); // MCT Port - Buffer

		for (Element elePort : portList)
		{
			try
			{
				String sMachineName = SMessageUtil.getChildText(elePort, "MACHINENAME", true);
				String sPortName = SMessageUtil.getChildText(elePort, "PORTNAME", true);
				String sPortTransferState = SMessageUtil.getChildText(elePort, "PORTTRANSFERSTATE", true); // LC | UC | UP | DOWN
				String sPortFullState = SMessageUtil.getChildText(elePort, "PORTFULLEMPTY", false);
				String sPortAccessMode = SMessageUtil.getChildText(elePort, "PORTACCESSMODE", false);

				EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);

				Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(sMachineName, sPortName);

				if (StringUtils.equals(sPortTransferState, "LC"))
				{
					MESPortServiceProxy.getPortServiceUtil().loadRequest(eventInfo, sMachineName, sPortName);
				}
				else if (StringUtils.equals(sPortTransferState, "UC"))
				{
					MESPortServiceProxy.getPortServiceUtil().unLoadComplete(eventInfo, sMachineName, sPortName);
				}
				else if (CommonUtil.equalsIn(sPortTransferState, "UP", "DOWN"))
				{
					// change port state
					eventInfo.setEventName("ChangeState");
					MakePortStateByStateInfo makePortStateByStateInfo = MESPortServiceProxy.getPortInfoUtil().makePortStateByStateInfo(portData, sPortTransferState);
					MESPortServiceProxy.getPortServiceImpl().makePortStateByState(portData, makePortStateByStateInfo, eventInfo);
				}

				// change port full state
				MESPortServiceProxy.getPortServiceImpl().changeFullState(sPortFullState, portData, eventInfo);

				eventInfo.setEventName("ChangeAccessMode");
				MakeAccessModeInfo makeAccessModeInfo = MESPortServiceProxy.getPortInfoUtil().makeAccessModeInfo(portData, sPortAccessMode);
				MESPortServiceProxy.getPortServiceImpl().makeAccessMode(portData, makeAccessModeInfo, eventInfo);
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
