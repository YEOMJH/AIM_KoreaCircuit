package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DSPReserveOperation;
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

public class DSPReserveOperationToMachine extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> operationList = SMessageUtil.getBodySequenceItemList(doc, "OPERATIONLIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DSPReserveOperation", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);

		List<DSPReserveOperation> currentBook = getCurrentReserveWOList(sMachineName);

		for (Element eleLot : operationList)
		{
			String productSpecName = SMessageUtil.getChildText(eleLot, "PRODUCTSPECNAME", true);
			String productSpecVersion = SMessageUtil.getChildText(eleLot, "PRODUCTSPECVERSION", true);
			String processOperationName = SMessageUtil.getChildText(eleLot, "PROCESSOPERATIONNAME", true);
			String processOperationVersion = SMessageUtil.getChildText(eleLot, "PROCESSOPERATIONVERSION", true);
			String priority = SMessageUtil.getChildText(eleLot, "PRIORITY", true);
			String machineName = SMessageUtil.getChildText(eleLot, "MACHINENAME", true);

			DSPReserveOperation reserveData = null;

			try
			{
				eventInfo.setEventName("Modify");

				reserveData = ExtendedObjectProxy.getDSPReserveOperationService().selectByKey(true,
						new Object[] { machineName, productSpecName, productSpecVersion, processOperationName, processOperationVersion });

				if (reserveData.getPriority() != priority)
				{
					reserveData.setPriority(priority);
					reserveData.setLastEventComment(eventInfo.getEventComment());
					reserveData.setLastEventName(eventInfo.getEventName());
					reserveData.setLastEventTime(eventInfo.getEventTime());
					reserveData.setLastEventTimeKey(eventInfo.getEventTimeKey());
					reserveData.setLastEventUser(eventInfo.getEventUser());

					reserveData = ExtendedObjectProxy.getDSPReserveOperationService().modify(eventInfo, reserveData);
				}
			}
			catch (greenFrameDBErrorSignal ne)
			{
				if (ne.getErrorCode().equals(ErrorSignal.NotFoundSignal))
				{
					eventInfo.setEventName("Create");

					reserveData = new DSPReserveOperation();
					reserveData.setMachineName(sMachineName);
					reserveData.setProductSpecName(productSpecName);
					reserveData.setProductSpecVersion(productSpecVersion);
					reserveData.setProcessOperationName(processOperationName);
					reserveData.setProcessOperationVersion(processOperationVersion);
					reserveData.setPriority(priority);
					reserveData.setLastEventComment(eventInfo.getEventComment());
					reserveData.setLastEventName(eventInfo.getEventName());
					reserveData.setLastEventTime(eventInfo.getEventTime());
					reserveData.setLastEventTimeKey(eventInfo.getEventTimeKey());
					reserveData.setLastEventUser(eventInfo.getEventUser());

					reserveData = ExtendedObjectProxy.getDSPReserveOperationService().create(eventInfo, reserveData);
				}
			}
			finally
			{
				// remove delivered data
				for (int idx = 0; idx < currentBook.size(); idx++)
				{
					// Lot ID is unique
					if (reserveData != null && currentBook.get(idx).getProcessOperationName().equals(reserveData.getProcessOperationName())
							&& currentBook.get(idx).getProductSpecName().equals(reserveData.getProductSpecName()))
						currentBook.remove(idx);
				}
			}
		}

		// unassigned from book with no longer existing reservations
		for (DSPReserveOperation data : currentBook)
		{
			try
			{
				if (data.getTransferFlag().equals("Y"))
				{
					eventInfo.setEventName("Modify");

					data.setPriority("");
					data.setLastEventComment(eventInfo.getEventComment());
					data.setLastEventName(eventInfo.getEventName());
					data.setLastEventTime(eventInfo.getEventTime());
					data.setLastEventTimeKey(eventInfo.getEventTimeKey());
					data.setLastEventUser(eventInfo.getEventUser());

					ExtendedObjectProxy.getDSPReserveOperationService().modify(eventInfo, data);
				}
				else
				{
					eventInfo.setEventName("Remove");
					ExtendedObjectProxy.getDSPReserveOperationService().remove(eventInfo, data);
				}

			}
			catch (greenFrameDBErrorSignal ne)
			{
				eventLog.warn(String.format("WO[%s] has not been unassigned from Machine[%s]", data.getProcessOperationName(), data.getMachineName()));
			}
			catch (Exception ex)
			{
				eventLog.warn(String.format("WO[%s] has not been unassigned from Machine[%s]", data.getProcessOperationName(), data.getMachineName()));
			}
		}

		return doc;

	}

	private List<DSPReserveOperation> getCurrentReserveWOList(String machineName) throws CustomException
	{
		try
		{
			String query = "machineName = ? ORDER BY priority";
			Object[] bindList = new Object[] { machineName };

			List<DSPReserveOperation> result = ExtendedObjectProxy.getDSPReserveOperationService().select(query, bindList);

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

		return new ArrayList<DSPReserveOperation>();
	}
}
