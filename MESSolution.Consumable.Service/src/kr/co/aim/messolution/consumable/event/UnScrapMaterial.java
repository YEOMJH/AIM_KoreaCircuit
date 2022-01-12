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
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.MakeAvailableInfo;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class UnScrapMaterial extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String materialKind = SMessageUtil.getBodyItemValue(doc, "MATERIALKIND", false);
		List<Element> materialElementList = SMessageUtil.getBodySequenceItemList(doc, "MATERIALLIST", true);

		Map<String, String> udfs = new HashMap<String, String>();

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnScrapMaterial", getEventUser(), getEventComment(), null, null);

		for (Element element : materialElementList)
		{
			String materialName = element.getChildText("MATERIALNAME");

			if (materialKind.equals("Consumable"))
			{
				Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);

				if (!GenericServiceProxy.getConstantMap().Cons_NotAvailable.equals(consumableData.getConsumableState()))
					throw new CustomException("MATERIAL-9018", materialName, consumableData.getConsumableState());

				// LAMI Film assigned check
				if (consumableData.getConsumableType().equals("BottomLamination") || consumableData.getConsumableType().equals("TopLamination"))
				{
					if (!consumableData.getUdfs().get("CARRIERNAME").isEmpty())
						throw new CustomException("MATERIAL-9022", materialName, consumableData.getUdfs().get("CARRIERNAME"));
				}
				if (consumableData.getConsumableType().equals("PR"))
				{
					udfs.put("SCRAPFLAG", "N");
				}

				// Consumable Update
				MakeAvailableInfo makeAvailableInfo = new MakeAvailableInfo();
				makeAvailableInfo.setUdfs(udfs);
				MESConsumableServiceProxy.getConsumableServiceImpl().makeAvailable(consumableData, makeAvailableInfo, eventInfo);

			}
			else if (materialKind.equals("Durable"))
			{
				Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(materialName);

				if (!GenericServiceProxy.getConstantMap().Dur_NotAvailable.equals(durableData.getDurableState()))
					throw new CustomException("MATERIAL-9018", materialName, durableData.getDurableState());
				// Crucible assigned check
				if (durableData.getDurableType().equals("Crucible"))
				{
					StringBuffer inquirysql = new StringBuffer();
					inquirysql.append("SELECT * ");
					inquirysql.append("  FROM DURABLE D, CT_CRUCIBLELOT CL ");
					inquirysql.append(" WHERE D.DURABLENAME = CL.DURABLENAME ");
					inquirysql.append("   AND D.DURABLENAME = :DURABLENAME ");

					Map<String, String> inquirybindMap = new HashMap<String, String>();
					inquirybindMap.put("DURABLENAME", materialName);

					@SuppressWarnings("unchecked")
					List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(inquirysql.toString(), inquirybindMap);

					if (sqlResult != null && sqlResult.size() > 0)
						throw new CustomException("MATERIAL-9021", materialName);
				}

				// FPC assigned check
				if (durableData.getDurableType().equals("FPC"))
				{
					if (!durableData.getUdfs().get("COVERNAME").isEmpty())
						throw new CustomException("MATERIAL-9022", materialName, durableData.getUdfs().get("COVERNAME"));
				}

				// Durable Update
				kr.co.aim.greentrack.durable.management.info.MakeAvailableInfo makeAvailableInfo = new kr.co.aim.greentrack.durable.management.info.MakeAvailableInfo();
				MESDurableServiceProxy.getDurableServiceImpl().makeAvailable(durableData, makeAvailableInfo, eventInfo);
			}
		}

		return doc;
	}

}
