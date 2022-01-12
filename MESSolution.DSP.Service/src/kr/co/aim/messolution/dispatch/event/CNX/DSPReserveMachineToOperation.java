package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DSPReserveOperation;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class DSPReserveMachineToOperation extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> operationList = SMessageUtil.getBodySequenceItemList(doc, "OPERATIONLIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DSPReserveMachineToOperation", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String sProductSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String sProductSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String sProcessOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String sProcessOperationVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", true);

		List<DSPReserveOperation> currentBook = getCurrentReserveWOList(sProcessOperationName, sProcessOperationVersion, sProductSpecName, sProductSpecVersion);

		for (Element eleLot : operationList)
		{
			String productSpecName = SMessageUtil.getChildText(eleLot, "PRODUCTSPECNAME", true);
			String productSpecVersion = SMessageUtil.getChildText(eleLot, "PRODUCTSPECVERSION", true);
			String processOperationName = SMessageUtil.getChildText(eleLot, "PROCESSOPERATIONNAME", true);
			String processOperationVersion = SMessageUtil.getChildText(eleLot, "PROCESSOPERATIONVERSION", true);
			String machineName = SMessageUtil.getChildText(eleLot, "MACHINENAME", true);

			DSPReserveOperation reserveData = null;

			try
			{
				reserveData = ExtendedObjectProxy.getDSPReserveOperationService().selectByKey(true,
						new Object[] { machineName, productSpecName, productSpecVersion, processOperationName, processOperationVersion });

				if (StringUtil.isEmpty(reserveData.getTransferFlag()))
				{
					reserveData.setTransferFlag("Y");
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
					reserveData.setMachineName(machineName);
					reserveData.setProductSpecName(productSpecName);
					reserveData.setProductSpecVersion(productSpecVersion);
					reserveData.setProcessOperationName(processOperationName);
					reserveData.setProcessOperationVersion(processOperationVersion);
					reserveData.setTransferFlag("Y");
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
					if (reserveData != null && currentBook.get(idx).getMachineName().equals(reserveData.getMachineName()))
						currentBook.remove(idx);
				}
			}
		}

		// unassigned from book with no longer existing reservations
		for (DSPReserveOperation data : currentBook)
		{
			try
			{
				if (StringUtil.isNotEmpty(data.getPriority()))
				{
					eventInfo.setEventName("Modify");

					data.setTransferFlag("");
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
				eventLog.warn(String.format("Machine[%s] has not been unassigned from Operation[%s]", data.getMachineName(), data.getProcessOperationName()));
			}
			catch (Exception ex)
			{
				eventLog.warn(String.format("Machine[%s] has not been unassigned from Operation[%s]", data.getMachineName(), data.getProcessOperationName()));
			}
		}

		return doc;

	}

	private List<DSPReserveOperation> getCurrentReserveWOList(String processOperationName, String processOperationVersion, String productSpecName, String productSpecVersion) throws CustomException
	{
		try
		{
			String query = "processOperationName = ? and processOperationVersion = ? and productSpecName = ? and productSpecVersion = ? ";
			Object[] bindList = new Object[] { processOperationName, processOperationVersion, productSpecName, productSpecVersion };

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
