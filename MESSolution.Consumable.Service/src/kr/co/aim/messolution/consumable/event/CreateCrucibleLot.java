package kr.co.aim.messolution.consumable.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.CrucibleLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class CreateCrucibleLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String crucibleName = SMessageUtil.getBodyItemValue(doc, "CRUCIBLENAME", true);

		List<CrucibleLot> dataInfoList = ExtendedObjectProxy.getCrucibleLotService().getCrucibleLotList(crucibleName);

		if (dataInfoList!=null)
		{
			StringBuilder crucibleLotNames = new StringBuilder();

			for (CrucibleLot dataInfo : dataInfoList)
			{
				crucibleLotNames.append(dataInfo.getCrucibleLotName());
				crucibleLotNames.append(' ');
			}

			throw new CustomException("MATERIAL-9004", crucibleLotNames);
		}

		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("FACTORYNAME", factoryName);
		String newCrucibleLotName = "";
		List<String> lstName = CommonUtil.generateNameByNamingRule("CrucibleLotNaming", nameRuleAttrMap, 1);
		newCrucibleLotName = lstName.get(0);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), "", "", "");
		eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());

		CrucibleLot dataInfo = new CrucibleLot();
		dataInfo.setCrucibleLotName(newCrucibleLotName);
		dataInfo.setDurableName(crucibleName);
		dataInfo.setCrucibleLotState(GenericServiceProxy.getConstantMap().Lot_Created);
		dataInfo.setCreateTime(eventInfo.getEventTime());
		dataInfo.setFactoryName(factoryName);
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventTimekey(eventInfo.getLastEventTimekey());
		dataInfo.setLastEventUser(getEventUser());
		dataInfo.setLastEventComment(getEventComment());

		CrucibleLot createdCrucibleLot = ExtendedObjectProxy.getCrucibleLotService().create(eventInfo, dataInfo);

		SMessageUtil.addItemToBody(doc, "NEWCRUCIBLELOTNAME", createdCrucibleLot.getCrucibleLotName());
		SMessageUtil.addItemToBody(doc, "NEWCRUCIBLELOTSTATE", createdCrucibleLot.getCrucibleLotState());
		SMessageUtil.addItemToBody(doc, "NEWCRUCIBLEWEIGHT", Double.toString(createdCrucibleLot.getWeight()));

		return doc;
	}

}
