package kr.co.aim.messolution.durable.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class UnKitMask extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> durableList = SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", false);

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnkitMask", this.getEventUser(), this.getEventComment(), "", "");

		for (Element eledur : durableList)
		{
			String durableName = SMessageUtil.getChildText(eledur, "DURABLENAME", true);
			String position = SMessageUtil.getChildText(eledur, "POSITION", true);
			String machineName = SMessageUtil.getChildText(eledur, "MACHINENAME", false);
			String UnitName = SMessageUtil.getChildText(eledur, "UNITNAME", false);

			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("MACHINENAME", machineName);
			udfs.put("UNITNAME", UnitName);

			CommonUtil.getMachineInfo(machineName);

			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
			
			if (MESDurableServiceProxy.getDurableServiceUtil().checkMaskIDLETimeOver(durableData))
			{
				durableData.setDurableCleanState(GenericServiceProxy.getConstantMap().Dur_Dirty);
			}

			MESDurableServiceProxy.getDurableServiceImpl().KitMask(durableData, constantMap.Dur_Mounted, "OnEQP", position, udfs, eventInfo);
		}

		return doc;
	}

}
