package kr.co.aim.messolution.consumable.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.MakeInUseInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class AssignMaterialToCST extends SyncHandler {

	final int FPCMaxCount = 10;

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", false);
		List<Element> materialElementList = SMessageUtil.getBodySequenceItemList(doc, "MATERIALLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("AssignMaterialToCST", getEventUser(), getEventComment(), null, null);

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
				setEventInfoConsu.getUdfs().put("CARRIERNAME", durableId);
				MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(materialName, setEventInfoConsu, eventInfo);

				// FilmCST Update
				MakeInUseInfo makeInUseInfo = new MakeInUseInfo();
				MESDurableServiceProxy.getDurableServiceImpl().makeInUse(durableData, makeInUseInfo, eventInfo);
			}
			else if (factoryName.equals("POSTCELL"))
			{
				Durable materialData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(materialName);

				if (!checkAssignCount(durableId))
					throw new CustomException("MATERIAL-0010");

				// FPC Update
				kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfoDur = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
				setEventInfoDur.getUdfs().put("PALLETNAME", durableId);
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(materialData, setEventInfoDur, eventInfo);

				// Pallet Update
				durableData.setLotQuantity(durableData.getLotQuantity() + 1);
				DurableServiceProxy.getDurableService().update(durableData);
				setEventInfoDur = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfoDur, eventInfo);
			}
		}

		return doc;
	}

	private boolean checkAssignCount(String palletName)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT 1 ");
		sql.append("  FROM DURABLE ");
		sql.append(" WHERE DURABLETYPE = 'FPC' ");
		sql.append("   AND PALLETNAME = :PALLETNAME ");
		
		Map<String, Object> args = new HashMap<>();
		args.put("PALLETNAME", palletName);

		@SuppressWarnings("unchecked")
		List<String> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() >= FPCMaxCount)
			return false;

		return true;
	}

}
