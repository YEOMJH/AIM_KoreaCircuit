package kr.co.aim.messolution.durable.event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.MaskStick;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class AssignMaskSheetReport extends AsyncHandler {

	private static Log log = LogFactory.getLog(AssignMaskSheetReport.class);
	private List<Object[]> updateArgList = new ArrayList<Object[]>();

	public void doWorks(Document doc) throws CustomException
	{
		// MACHINENAME
		// MASKLOTNAME
		// FRAMENAME
		// SHEETLIST
		// SHEET
		// SHEETNAME

		String messageName = SMessageUtil.getMessageName(doc);

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String maskLotName = SMessageUtil.getBodyItemValue(doc, "MASKLOTNAME", true);
		String frameName = SMessageUtil.getBodyItemValue(doc, "FRAMENAME", false);

		Element bodyElement = SMessageUtil.getBodyElement(doc);
		Element sheetListElement = bodyElement.getChild("SHEETLIST");

		EventInfo eventInfo = EventInfoUtil.makeEventInfo(messageName, this.getEventUser(), this.getEventComment(), null, null);
		MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });

		if (sheetListElement != null)
		{
			for (Iterator<?> iterator = sheetListElement.getChildren().iterator(); iterator.hasNext();)
			{
				Element sheet = (Element) iterator.next();
				String sheetName = sheet.getChildText("SHEETNAME");
				String position = sheet.getChildText("POSITION");
				MaskStick sheetData = ExtendedObjectProxy.getMaskStickService().selectByKey(true, new Object[] { sheetName });

				setArgs(maskLotData, "Sheet", sheetName, position, eventInfo);
			}

			if (updateArgList.size() > 0)
				MESDurableServiceProxy.getDurableServiceUtil().InsertCT_MaskMaterial(updateArgList);
			else
				log.error("Cannot insert into CT_MaskMaterial because insert item[Sheet] does not exist");
		}
	}

	private void setArgs(MaskLot maskLotData, String materialType, String materialName, String position, EventInfo eventInfo)
	{
		List<Object> bindList = new ArrayList<Object>();

		bindList.add(maskLotData.getMaskLotName());
		bindList.add(materialType);
		bindList.add(materialName);
		bindList.add(position);
		bindList.add(eventInfo.getEventComment());
		bindList.add(eventInfo.getEventName());
		bindList.add("TO_DATE(SUBSTR('" + eventInfo.getLastEventTimekey() + "',0,14),'YYYYMMDDHH24MISS')");
		bindList.add(eventInfo.getLastEventTimekey());
		bindList.add(eventInfo.getEventUser());

		updateArgList.add(bindList.toArray());
	}

}
