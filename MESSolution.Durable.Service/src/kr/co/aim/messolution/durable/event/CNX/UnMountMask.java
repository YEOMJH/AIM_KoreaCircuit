package kr.co.aim.messolution.durable.event.CNX;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class UnMountMask extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnMountMask", this.getEventUser(), this.getEventComment(), "", "");

		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String sUnitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);

		Map<String, String> udfs = new HashMap<String, String>();
		udfs.put("MACHINENAME", sMachineName);
		udfs.put("UNITNAME", sUnitName);

		for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", false))
		{
			String sDurableName = SMessageUtil.getChildText(eledur, "DURABLENAME", true);

			// getMachineData
			CommonUtil.getMachineInfo(sMachineName);

			// getDurableData
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);

			// Validation
			/*
			if (StringUtils.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_Scrapped))
				throw new CustomException("MASK-0012", sDurableName, durableData.getDurableState());
			*/

			if (StringUtils.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_InUse))
				throw new CustomException("MASK-0026", sDurableName, durableData.getDurableState());

			/*
			if (!StringUtils.equals(durableData.getDurableCleanState(), GenericServiceProxy.getConstantMap().Dur_Clean))
				throw new CustomException("MASK-0025", sDurableName);
			*/

			if (StringUtils.equals(durableData.getUdfs().get("DURABLEHOLDSTATE"), "Y"))
				throw new CustomException("MASK-0013", sDurableName);

			if (!(StringUtils.equals(durableData.getDurableType(), "PhotoMask") || StringUtils.equals(durableData.getDurableType(), "Mask")))
				throw new CustomException("MASK-0027", durableData.getDurableType(), sDurableName);
			
			if(!StringUtils.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_Mounted))
			{
				throw new CustomException("MASK-0058", durableData.getDurableState());
			}
			
			if (MESDurableServiceProxy.getDurableServiceUtil().checkMaskIDLETimeOver(durableData))
			{
				durableData.setDurableCleanState(GenericServiceProxy.getConstantMap().Dur_Dirty);
			}

			udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_MOVING);
			udfs.put("RETICLESLOT", "");
			MESDurableServiceProxy.getDurableServiceImpl().mountPhotoMask(durableData, GenericServiceProxy.getConstantMap().Dur_UnMounted, udfs, eventInfo);
		}

		return doc;
	}

}
