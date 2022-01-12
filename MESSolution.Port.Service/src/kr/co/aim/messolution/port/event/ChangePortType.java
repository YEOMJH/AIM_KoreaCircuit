package kr.co.aim.messolution.port.event;

import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.data.PortSpec;
import kr.co.aim.greentrack.port.management.info.SetEventInfo;
import kr.co.aim.greentrack.portspec.management.info.ChangeSpecInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ChangePortType extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String sPortName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String sPortType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangePortType", this.getEventUser(), this.getEventComment(), null, null);

		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(sMachineName, sPortName);

		Map<String, String> udfs = CommonUtil.setNamedValueSequence(SMessageUtil.getBodyElement(doc), Port.class.getSimpleName());

		SetEventInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(udfs);
		MESPortServiceProxy.getPortServiceImpl().setEvent(portData, transitionInfo, eventInfo);

		if (StringUtils.isNotEmpty(sPortType))
		{
			// Change PortSpec table in DB
			PortSpec portSpec = MESPortServiceProxy.getPortServiceUtil().getPortSpecInfo(sMachineName, sPortName);
			ChangeSpecInfo changeSpecInfo = MESPortServiceProxy.getPortInfoUtil().changeSpecInfo(portSpec, sPortType, portSpec.getDescription(), portSpec.getFactoryName(), portSpec.getAreaName(),
					portSpec.getVendor(), portSpec.getModel(), portSpec.getSerialNo(), portSpec.getPortStateModelName());

			MESPortServiceProxy.getPortServiceImpl().changePortType(portSpec, changeSpecInfo, eventInfo);
		}

		return doc;
	}
}
