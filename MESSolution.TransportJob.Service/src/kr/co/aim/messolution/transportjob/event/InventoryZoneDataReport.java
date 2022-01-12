package kr.co.aim.messolution.transportjob.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.StockerZoneInfo;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class InventoryZoneDataReport extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX]
	 * 
	 * <Body>
	 *    <MACHINENAME />
	 *    <ZONELIST>
	 *       <ZONE>
	 *          <ZONENAME />
	 *          <TOTALCAPACITY />
	 *          <HIGHWATERMARK />
	 *          <PROHIBITEDSHEFCOUNT />
	 *          <USEDSHELFCOUNT />
	 *          <EMPTYSHELFCOUNT />
	 *       </ZONE>
	 *    </ZONELIST>
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("InventoryZoneDataReport", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		List<Element> zoneList = SMessageUtil.getBodySequenceItemList(doc, "ZONELIST", true);

		for (Element zoneE : zoneList)
		{
			String zoneName = zoneE.getChildText("ZONENAME");
			String totalCapacity = zoneE.getChildText("TOTALCAPACITY");
			String prohibitedShelfCount = zoneE.getChildText("PROHIBITEDSHELFCOUNT");
			String usedShelfCount = zoneE.getChildText("USEDSHELFCOUNT");
			String emptyShelfCount = zoneE.getChildText("EMPTYSHELFCOUNT");

			List<StockerZoneInfo> sqlResult = ExtendedObjectProxy.getStockerZoneInfo().select("MACHINENAME = ? AND ZONENAME = ?", new Object[] { machineName, zoneName });

			if (sqlResult.size() > 0)
			{
				StockerZoneInfo stockerZoneInfo = sqlResult.get(0);
				stockerZoneInfo.setTotalCapacity(totalCapacity);
				stockerZoneInfo.setUsedShelfCount(usedShelfCount);
				stockerZoneInfo.setEmptyShelfCount(emptyShelfCount);
				stockerZoneInfo.setProhibitedShelfCount(prohibitedShelfCount);
				stockerZoneInfo.setEventName(eventInfo.getEventName());
				stockerZoneInfo.setEventComment(eventInfo.getEventComment());
				stockerZoneInfo.setEventUser(eventInfo.getEventUser());
				stockerZoneInfo.setTimeKey(eventInfo.getEventTimeKey());

				try
				{
					ExtendedObjectProxy.getStockerZoneInfo().modify(eventInfo, stockerZoneInfo);
				}
				catch (Exception e)
				{
					throw new CustomException("JOB-8012", "MACHINENAME : " + machineName + ", " + "ZONENAME : " + zoneName);
				}
			}
			else
			{
				StockerZoneInfo stockerZoneInfo = new StockerZoneInfo();
				stockerZoneInfo.setMachineName(machineName);
				stockerZoneInfo.setZoneName(zoneName);
				stockerZoneInfo.setTotalCapacity(totalCapacity);
				stockerZoneInfo.setUsedShelfCount(usedShelfCount);
				stockerZoneInfo.setEmptyShelfCount(emptyShelfCount);
				stockerZoneInfo.setProhibitedShelfCount(prohibitedShelfCount);
				stockerZoneInfo.setEventName(eventInfo.getEventName());
				stockerZoneInfo.setEventComment(eventInfo.getEventComment());
				stockerZoneInfo.setEventUser(eventInfo.getEventUser());
				stockerZoneInfo.setTimeKey(eventInfo.getEventTimeKey());

				try
				{
					ExtendedObjectProxy.getStockerZoneInfo().create(eventInfo, stockerZoneInfo);
				}
				catch (Exception e)
				{
					throw new CustomException("JOB-8012", "MACHINENAME : " + machineName + ", " + "ZONENAME : " + zoneName);
				}
			}
		}
	}
}
