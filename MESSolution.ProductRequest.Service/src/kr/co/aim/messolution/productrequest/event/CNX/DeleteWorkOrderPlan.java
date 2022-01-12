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

import org.jdom.Document;
import org.jdom.Element;

public class DeleteWorkOrderPlan extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);

		List<Element> planList = SMessageUtil.getBodySequenceItemList(doc, "PLANLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeletePlan", getEventUser(), getEventComment(), null, null);

		for (Element plan : planList)
		{
			String productRequestName = SMessageUtil.getChildText(plan, "PRODUCTREQUESTNAME", true);
			String sPlanDate = SMessageUtil.getChildText(plan, "PLANDATE", true);

			Date date = new Date();
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");
			try
			{
				date = dateFormat.parse(sPlanDate);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			Timestamp planDate = new Timestamp(date.getTime());

			ExtendedObjectProxy.getProductRequestPlanService().deleteProductRequestPlan(eventInfo, productRequestName, planDate, machineName);
		}

		return doc;
	}
}
