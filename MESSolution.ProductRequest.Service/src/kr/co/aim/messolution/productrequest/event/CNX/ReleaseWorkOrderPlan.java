package kr.co.aim.messolution.productrequest.event.CNX;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class ReleaseWorkOrderPlan extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> planList = SMessageUtil.getBodySequenceItemList(doc, "PLANLIST", true);

		for (Element plan : planList)
		{
			String productRequestName = SMessageUtil.getChildText(plan, "PRODUCTREQUESTNAME", true);
			String sPlanDate = SMessageUtil.getChildText(plan, "PLANDATE", true);
			String machineName = SMessageUtil.getChildText(plan, "MACHINENAME", true);

			Date date = new Date();
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:00:00.0");
			try
			{
				date = dateFormat.parse(sPlanDate);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			Timestamp planDate = new Timestamp(date.getTime());

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReleasePlan", getEventUser(), getEventComment(), null, null);
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			ExtendedObjectProxy.getProductRequestPlanService().makeReleasedProductRequestPlan(eventInfo, productRequestName, planDate, machineName);
		}

		return doc;
	}

}
