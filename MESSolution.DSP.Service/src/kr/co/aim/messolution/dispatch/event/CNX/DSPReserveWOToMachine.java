package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DSPReserveWO;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class DSPReserveWOToMachine extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> workorderList = SMessageUtil.getBodySequenceItemList(doc, "WORKORDERLIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DSPReserveWO", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);

		List<DSPReserveWO> currentBook = getCurrentReserveWOList(sMachineName);

		for (Element eleLot : workorderList)
		{
			String workorder = SMessageUtil.getChildText(eleLot, "WORKORDER", true);
			String priority = SMessageUtil.getChildText(eleLot, "PRIORITY", true);
			String machineName = SMessageUtil.getChildText(eleLot, "MACHINENAME", true);

			DSPReserveWO reserveData = null;

			try
			{
				eventInfo.setEventName("Modify");

				reserveData = ExtendedObjectProxy.getDSPReserveWOService().selectByKey(true, new Object[] { machineName, workorder });

				if (reserveData.getPriority() != Long.parseLong(priority))
				{
					reserveData.setPriority(Long.parseLong(priority));
					reserveData.setLastEventComment(eventInfo.getEventComment());
					reserveData.setLastEventName(eventInfo.getEventName());
					reserveData.setLastEventTime(eventInfo.getEventTime());
					reserveData.setLastEventTimeKey(eventInfo.getEventTimeKey());
					reserveData.setLastEventUser(eventInfo.getEventUser());

					reserveData = ExtendedObjectProxy.getDSPReserveWOService().modify(eventInfo, reserveData);
				}
			}
			catch (greenFrameDBErrorSignal ne)
			{
				if (ne.getErrorCode().equals(ErrorSignal.NotFoundSignal))
				{
					eventInfo.setEventName("Create");

					reserveData = new DSPReserveWO();
					reserveData.setMachineName(sMachineName);
					reserveData.setWorkorder(workorder);
					reserveData.setPriority(Long.parseLong(priority));
					reserveData.setLastEventComment(eventInfo.getEventComment());
					reserveData.setLastEventName(eventInfo.getEventName());
					reserveData.setLastEventTime(eventInfo.getEventTime());
					reserveData.setLastEventTimeKey(eventInfo.getEventTimeKey());
					reserveData.setLastEventUser(eventInfo.getEventUser());

					reserveData = ExtendedObjectProxy.getDSPReserveWOService().create(eventInfo, reserveData);
				}
			}
			finally
			{
				// remove delivered data
				for (int idx = 0; idx < currentBook.size(); idx++)
				{
					// Lot ID is unique
					if (reserveData != null && currentBook.get(idx).getWorkorder().equals(reserveData.getWorkorder()))
						currentBook.remove(idx);
				}
			}
		}

		// unassigned from book with no longer existing reservations
		for (DSPReserveWO data : currentBook)
		{
			try
			{
				eventInfo.setEventName("Remove");
				ExtendedObjectProxy.getDSPReserveWOService().remove(eventInfo, data);
			}
			catch (greenFrameDBErrorSignal ne)
			{
				eventLog.warn(String.format("WO[%s] has not been unassigned from Machine[%s]", data.getWorkorder(), data.getMachineName()));
			}
			catch (Exception ex)
			{
				eventLog.warn(String.format("WO[%s] has not been unassigned from Machine[%s]", data.getWorkorder(), data.getMachineName()));
			}
		}

		return doc;

	}

	private List<DSPReserveWO> getCurrentReserveWOList(String machineName) throws CustomException
	{
		try
		{
			String query = "machineName = ? ORDER BY priority";
			Object[] bindList = new Object[] { machineName };

			List<DSPReserveWO> result = ExtendedObjectProxy.getDSPReserveWOService().select(query, bindList);

			return result;
		}
		catch (greenFrameDBErrorSignal ex)
		{
			// throw new CustomException();
		}
		catch (Exception ex)
		{
			// throw new CustomException();
		}

		return new ArrayList<DSPReserveWO>();
	}
}
