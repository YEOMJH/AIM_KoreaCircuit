package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DefectGroup;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class ModifyDefectGroup extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {

		List<Element> codeList = SMessageUtil.getBodySequenceItemList(doc, "DEFECTCODELIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyDefectGroup", getEventUser(), getEventComment(), "",
				"");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		for (Element code : codeList) {
			String defectGroupName = SMessageUtil.getChildText(code, "DEFECTGROUPNAME", true);
			String defectCode = SMessageUtil.getChildText(code, "DEFECTCODE", true);
			String judge = SMessageUtil.getChildText(code, "JUDGE", true);
			String lowerQty = SMessageUtil.getChildText(code, "LOWERQTY", false);
			String upperQty = SMessageUtil.getChildText(code, "UPPERQTY", false);
			String useFlag = SMessageUtil.getChildText(code, "USEFLAG", false);

			DefectGroup dataInfo = ExtendedObjectProxy.getDefectGroupServices().selectByKey(false,
					new Object[] { defectGroupName, defectCode, judge });

			if (StringUtils.isNotEmpty(lowerQty) && StringUtils.isNotEmpty(upperQty)) {

				String query = "DEFECTGROUPNAME = ? " 
								+ "AND DEFECTCODE = ? "
								+ "AND JUDGE != ? " 
								+ "ORDER BY defectGroupName";
				Object[] bindList = new Object[] { defectGroupName, defectCode,judge };

				List<DefectGroup> resultList = ExtendedObjectProxy.getDefectGroupServices().select(query, bindList);

				for (DefectGroup result : resultList) {

					if ((Integer.parseInt(upperQty) <= Integer.parseInt(result.getUpperQty()))
							&& (Integer.parseInt(upperQty) >= Integer.parseInt(result.getLowerQty()))) {
						throw new CustomException("DEFECTCODE-0004", result.getLowerQty(), result.getUpperQty());
					}

					if ((Integer.parseInt(lowerQty) <= Integer.parseInt(result.getUpperQty()))
							&& (Integer.parseInt(lowerQty) >= Integer.parseInt(result.getLowerQty()))) {
						throw new CustomException("DEFECTCODE-0004", result.getLowerQty(), result.getUpperQty());
					}
				}
			}
			dataInfo.setUseFlag(useFlag);
			dataInfo.setLowerQty(lowerQty);
			dataInfo.setUpperQty(upperQty);

			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

			dataInfo = ExtendedObjectProxy.getDefectGroupServices().modify(eventInfo, dataInfo);
		}

		return doc;
	}

}
