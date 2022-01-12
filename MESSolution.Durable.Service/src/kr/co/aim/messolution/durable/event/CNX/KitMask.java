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
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;

public class KitMask extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("KitMask", this.getEventUser(), this.getEventComment(), "", "");

		String durableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);

		Map<String, String> udfs = new HashMap<String, String>();
		udfs.put("MACHINENAME", machineName);
		udfs.put("UNITNAME", unitName);

		CommonUtil.getMachineInfo(machineName);

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
		
		MESDurableServiceProxy.getDurableServiceUtil().checkMaskIDLETime(durableData, eventInfo);

		List<Durable> kittedMasks = null;

		String condition = " MACHINENAME = ? AND UNITNAME = ? AND DURABLETYPE = ? AND DURABLESTATE = ? AND TRANSPORTSTATE = ? AND RETICLESLOT IS NULL"; //
		Object[] bindSet = new Object[] { machineName, unitName, "PhotoMask", "InUse", "OnEQP" };
		try
		{
			kittedMasks = DurableServiceProxy.getDurableService().select(condition, bindSet);
		}
		catch (Exception de)
		{
		}

		if (kittedMasks != null)
		{
			for (Durable mask : kittedMasks)
			{
				if (!StringUtil.equals(mask.getKey().getDurableName(), durableName))
					throw new CustomException("MASK-0031");
			}
		}

		MESDurableServiceProxy.getDurableServiceImpl().KitMask(durableData, constantMap.Dur_InUse, "OnEQP", "", udfs, eventInfo);

		return doc;
	}

}
