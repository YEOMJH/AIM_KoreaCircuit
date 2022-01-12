package kr.co.aim.messolution.durable.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.MaskMaterial;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class CancelCreateOLEDMaskLot extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> MaskList = SMessageUtil.getBodySequenceItemList(doc, "MASKLOTLIST", true);

		for (Element Mask : MaskList)
		{
			String maskLotName = SMessageUtil.getChildText(Mask, "MASKLOTNAME", true);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelCreateMaskLot", this.getEventUser(), this.getEventComment(), "", "");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			MaskLot dataInfo = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskLotName });

			ExtendedObjectProxy.getMaskLotService().remove(eventInfo, dataInfo);

			deassignMaskMaterial(eventInfo, maskLotName);
		}

		return doc;
	}

	private void deassignMaskMaterial(EventInfo eventInfo, String maskLotName) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT MASKLOTNAME, MATERIALTYPE, MATERIALNAME ");
		sql.append("  FROM CT_MASKMATERIAL ");
		sql.append(" WHERE MASKLOTNAME = :MASKLOTNAME ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("MASKLOTNAME", maskLotName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			for (int i = 0; i < result.size(); i++)
			{
				String materialType = ConvertUtil.getMapValueByName(result.get(i), "MATERIALTYPE");
				String materialName = ConvertUtil.getMapValueByName(result.get(i), "MATERIALNAME");

				deassignExecute(eventInfo, maskLotName, materialType, materialName);
			}
		}
	}

	private void deassignExecute(EventInfo eventInfo, String maskLotName, String materialType, String materialName) throws greenFrameDBErrorSignal, CustomException
	{
		eventInfo.setEventName("DeassignMaterial");

		MaskMaterial dataInfo = ExtendedObjectProxy.getMaskMaterialService().selectByKey(false, new Object[] { maskLotName, materialType, materialName });

		ExtendedObjectProxy.getMaskMaterialService().remove(eventInfo, dataInfo);
	}

}
