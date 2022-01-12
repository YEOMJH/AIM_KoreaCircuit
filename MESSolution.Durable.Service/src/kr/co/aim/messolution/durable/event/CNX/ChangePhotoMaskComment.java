package kr.co.aim.messolution.durable.event.CNX;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineKey;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ChangePhotoMaskComment extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String sMaskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Change", this.getEventUser(), this.getEventComment(), "", "");

		// getDurableData
		Durable durMaskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sMaskName);
		
		/*
		if (StringUtils.equals(durMaskData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_Scrapped))
			throw new CustomException("MASK-0012", sMaskName, durMaskData.getDurableState());
		*/
		SetEventInfo setEventInfo = new SetEventInfo();
		
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durMaskData, setEventInfo, eventInfo);
		
		return doc;
	}
}
