package kr.co.aim.messolution.durable.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskMaterial;
import kr.co.aim.messolution.extended.object.management.data.MaskStick;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class AssignOLEDMaskSheet extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		String maskLotName = SMessageUtil.getBodyItemValue(doc, "MASKLOTNAME", true);
		List<Element> SHEETLIST = SMessageUtil.getBodySequenceItemList(doc, "SHEETLIST", true);

		StringBuffer inquirysql = new StringBuffer();
		inquirysql.append("SELECT MASKLOTSTATE, MASKLOTHOLDSTATE ");
		inquirysql.append("  FROM CT_MASKLOT ");
		inquirysql.append(" WHERE MASKLOTSTATE = 'Released' ");
		inquirysql.append("   AND MASKLOTHOLDSTATE = 'N' ");
		inquirysql.append("   AND MASKLOTNAME = :MASKLOTNAME ");

		Map<String, String> inquirybindMap = new HashMap<String, String>();
		inquirybindMap.put("MASKLOTNAME", maskLotName);
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(inquirysql.toString(), inquirybindMap);
		if (sqlResult.size() > 0)
		{
			for (Element Sheet : SHEETLIST)
			{
				String sheetName = SMessageUtil.getChildText(Sheet, "SHEETNAME", true);

				EventInfo eventInfo = EventInfoUtil.makeEventInfo("AttacheSheet", this.getEventUser(), this.getEventComment(), "", "");

				eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());
				MaskStick dataInfo = ExtendedObjectProxy.getMaskStickService().selectByKey(false, new Object[] { sheetName });
				dataInfo.setStickState(constantMap.SheetState_Attached);
				dataInfo.setMaskLotName(maskLotName);
				dataInfo.setLastEventComment(eventInfo.getEventComment());
				dataInfo.setLastEventName(eventInfo.getEventName());
				dataInfo.setLastEventTime(eventInfo.getEventTime());
				dataInfo.setLastEventTimeKey(eventInfo.getLastEventTimekey());
				dataInfo.setLastEventUser(eventInfo.getEventUser());

				ExtendedObjectProxy.getMaskStickService().modify(eventInfo, dataInfo);

				MaskMaterial MaterialdataInfo = new MaskMaterial();
				MaterialdataInfo.setMaskLotName(maskLotName);
				MaterialdataInfo.setMaterialType(constantMap.MaterialType_Sheet);
				MaterialdataInfo.setMaterialName(sheetName);
				MaterialdataInfo.setLastEventComment(eventInfo.getEventComment());
				MaterialdataInfo.setLastEventName(eventInfo.getEventName());
				MaterialdataInfo.setLastEventTime(eventInfo.getEventTime());
				MaterialdataInfo.setLastEventTimeKey(eventInfo.getLastEventTimekey());
				MaterialdataInfo.setLastEventUser(eventInfo.getEventUser());

				ExtendedObjectProxy.getMaskMaterialService().create(eventInfo, MaterialdataInfo);

			}
		}

		return doc;
	}

}
