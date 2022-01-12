package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DefectGroup;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class CreateDefectGroup extends SyncHandler {

	public static Log logger = LogFactory.getLog(CreateDefectGroup.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		List<Element> DEFECTGROUPLIST = SMessageUtil.getBodySequenceItemList(doc, "CREATEDEFECTGROUPLIST", true);

		for (Element DEFECT : DEFECTGROUPLIST)
		{

			String defectGroupName = SMessageUtil.getChildText(DEFECT, "DEFECTGROUPNAME", true);
			String defectCode = SMessageUtil.getChildText(DEFECT, "DEFECTCODE", true);
			String judge = SMessageUtil.getChildText(DEFECT, "JUDGE", true);
			String lowerQty = SMessageUtil.getChildText(DEFECT, "LOWERQTY", false);
			String upperQty = SMessageUtil.getChildText(DEFECT, "UPPERQTY", false);
			String useFlag = SMessageUtil.getChildText(DEFECT, "USEFLAG", true);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateDefectGroup", getEventUser(), getEventComment(), "", "");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			if (!checkExist(defectGroupName, defectCode, judge))
				throw new CustomException("DEFECTCODE-0003", defectGroupName, defectCode, judge);

			if (StringUtils.isNotEmpty(lowerQty) && StringUtils.isNotEmpty(upperQty))
			{
				String query = " DEFECTGROUPNAME = ? AND DEFECTCODE = ? ORDER BY defectGroupName ";
				Object[] bindList = new Object[] { defectGroupName, defectCode };

				try
				{
					List<DefectGroup> resultList = ExtendedObjectProxy.getDefectGroupServices().select(query, bindList);

					for (DefectGroup result : resultList)
					{
						if ((Integer.parseInt(upperQty) <= Integer.parseInt(result.getUpperQty())) && (Integer.parseInt(upperQty) >= Integer.parseInt(result.getLowerQty())))
							throw new CustomException("DEFECTCODE-0004", result.getLowerQty(), result.getUpperQty());

						if ((Integer.parseInt(lowerQty) <= Integer.parseInt(result.getUpperQty())) && (Integer.parseInt(lowerQty) >= Integer.parseInt(result.getLowerQty())))
							throw new CustomException("DEFECTCODE-0004", result.getLowerQty(), result.getUpperQty());
					}
				}
				catch (Exception e)
				{
					logger.info("Data not exist");
				}
			}

			DefectGroup dataInfo = new DefectGroup();

			dataInfo.setDefectGroupName(defectGroupName);
			dataInfo.setDefectCode(defectCode);
			dataInfo.setJudge(judge);

			if (StringUtil.isNotEmpty(lowerQty))
				dataInfo.setLowerQty(lowerQty);

			if (StringUtil.isNotEmpty(upperQty))
				dataInfo.setUpperQty(upperQty);

			dataInfo.setUseFlag(useFlag);
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

			ExtendedObjectProxy.getDefectGroupServices().create(eventInfo, dataInfo);
		}

		return doc;
	}

	private boolean checkExist(String defectGroupName, String defectCode, String judge) throws greenFrameDBErrorSignal, CustomException
	{
		try
		{
			ExtendedObjectProxy.getDefectGroupServices().selectByKey(false, new Object[] { defectGroupName, defectCode, judge });
			return false;
		}
		catch (Exception e)
		{
			return true;
		}
	}
}
