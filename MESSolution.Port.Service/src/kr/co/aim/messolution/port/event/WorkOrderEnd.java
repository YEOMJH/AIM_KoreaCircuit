package kr.co.aim.messolution.port.event;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ProductRequestPlan;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;

public class WorkOrderEnd extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "WORKORDER", true);
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
		String lotName = "";

		if (lotList != null && lotList.size() > 0)
			lotName = lotList.get(0).getChildText("LOTNAME");
		else
			throw new CustomException("SYS-", "Lot does not exist in WorkOrder");

		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("WorkOrderEnd", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		// String => Timestemp
		Date date = new Date();
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
		try
		{
			date = sdf.parse(lotData.getUdfs().get("PLANDATE"));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		Timestamp planDate = new Timestamp(date.getTime());

		ExtendedObjectProxy.getProductRequestPlanService().makeCompletedProductRequestPlan(eventInfo, productRequestName, planDate, machineName);
	}
}