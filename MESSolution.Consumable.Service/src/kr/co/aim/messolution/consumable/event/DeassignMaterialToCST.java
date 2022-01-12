package kr.co.aim.messolution.consumable.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.MakeNotInUseInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;
import org.jdom.Element;

public class DeassignMaterialToCST extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", false);
		List<Element> materialElementList = SMessageUtil.getBodySequenceItemList(doc, "MATERIALLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeassignMaterialToCST", getEventUser(), getEventComment(), null, null);

		Map<String, String> udfs = new HashMap<>();

		for (Element element : materialElementList)
		{
			String materialName = element.getChildText("MATERIALNAME");
			String durableId = element.getChildText("DURABLEID");

			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableId);

			if (factoryName.equals("OLED"))
			{
				// Film Update
				SetEventInfo setEventInfoConsu = new SetEventInfo();
				setEventInfoConsu.getUdfs().put("CARRIERNAME", StringUtil.EMPTY);
				MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(materialName, setEventInfoConsu, eventInfo);

				// FilmCST Update
				MakeNotInUseInfo makeNotInUseInfo = new MakeNotInUseInfo();
				MESDurableServiceProxy.getDurableServiceImpl().makeNotInUse(durableData, makeNotInUseInfo, eventInfo);
			}
			else if (factoryName.equals("POSTCELL"))
			{
				Durable materialData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(materialName);

				// FPC Update
				udfs.clear();
				udfs.put("PALLETNAME", StringUtil.EMPTY);
				kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfoDur = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
				setEventInfoDur.setUdfs(udfs);
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(materialData, setEventInfoDur, eventInfo);

				// Pallet Update
				if (durableData.getLotQuantity() > 0)
					durableData.setLotQuantity(durableData.getLotQuantity() - 1);

				DurableServiceProxy.getDurableService().update(durableData);

				setEventInfoDur = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfoDur, eventInfo);
			}
		}

		return doc;
	}

}
