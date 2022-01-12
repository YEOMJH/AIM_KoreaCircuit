package kr.co.aim.messolution.durable.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class AssignMask extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> durableList = SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("AssignMask", this.getEventUser(), this.getEventComment(), "", "");

		for (Element eledur : durableList)
		{
			String sDurableName = SMessageUtil.getChildText(eledur, "DURABLENAME", true);
			String sDurableType = SMessageUtil.getChildText(eledur, "DURABLETYPE", true);
			String smachineName = SMessageUtil.getChildText(eledur, "MACHINENAME", true);
			String sUnitName = SMessageUtil.getChildText(eledur, "UNITNAME", true);

			// getDurableData
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);

			// Validation
			if (!StringUtils.equals(durableData.getUdfs().get("MACHINENAME"), ""))
				throw new CustomException("MASK-0002" + "sDurableName is " + sDurableName);

			SetEventInfo assignMaskInfo = MESDurableServiceProxy.getDurableInfoUtil().setAssignMaskInfo(sDurableType, smachineName, sUnitName, "", "");

			MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, assignMaskInfo, eventInfo);
		}

		return doc;
	}
}
