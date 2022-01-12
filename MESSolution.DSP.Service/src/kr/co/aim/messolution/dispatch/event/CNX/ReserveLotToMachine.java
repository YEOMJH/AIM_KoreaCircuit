package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
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

public class ReserveLotToMachine extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> reserveLotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangePosition", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		List<ReserveLot> currentBook = getCurrentReserveLotList(sMachineName);

		for (Element eleLot : reserveLotList)
		{
			String sLotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
			String sPosition = SMessageUtil.getChildText(eleLot, "POSITION", true);
			String sProductSpecName = SMessageUtil.getChildText(eleLot, "PRODUCTSPECNAME", true);
			String sProductSpecVersion = SMessageUtil.getChildText(eleLot, "PRODUCTSPECVERSION", true);
			String sProcessOperationName = SMessageUtil.getChildText(eleLot, "PROCESSOPERATIONNAME", true);
			String sProcessOperationVersion = SMessageUtil.getChildText(eleLot, "PROCESSOPERATIONVERSION", true);
			String sPriorityPass = SMessageUtil.getChildText(eleLot, "PRIORITYPASS", false);
			String sProductRequestName = SMessageUtil.getChildText(eleLot, "PRODUCTREQUESTNAME", true);
			String sFactoryName = SMessageUtil.getChildText(eleLot, "FACTORYNAME", true);

			ReserveLot reserveData = null;

			try
			{
				eventInfo.setEventName("Modify");

				reserveData = ExtendedObjectProxy.getReserveLotService().selectByKey(true, new Object[] {sLotName, sProcessOperationName, sProcessOperationVersion });

				// it is meaning
				if (reserveData.getPosition() != Long.parseLong(sPosition) || !reserveData.getPriorityPass().equals(sPriorityPass))
				{
					reserveData.setPosition(Long.parseLong(sPosition));
					reserveData.setPriorityPass(sPriorityPass);
					reserveData.setReserveTimeKey(eventInfo.getEventTimeKey());

					reserveData = ExtendedObjectProxy.getReserveLotService().modify(eventInfo, reserveData);
				}

			}
			catch (greenFrameDBErrorSignal ne)
			{
				if (reserveData==null)
				{
					eventInfo.setEventName("Create");

					reserveData = new ReserveLot(sMachineName, sLotName);
					reserveData.setReserveState(GenericServiceProxy.getConstantMap().RESV_STATE_RESV);
					reserveData.setMachineName(sMachineName);
					reserveData.setProcessOperationName(sProcessOperationName);
					reserveData.setProcessOperationVersion(sProcessOperationVersion);
					reserveData.setProductSpecName(sProductSpecName);
					reserveData.setProductSpecVersion(sProductSpecVersion);
					reserveData.setPosition(Long.parseLong(sPosition));
					reserveData.setProductRequestName(sProductRequestName);
					reserveData.setFactoryName(sFactoryName);
					reserveData.setReserveTimeKey(eventInfo.getEventTimeKey());
					reserveData.setReserveUser(eventInfo.getEventUser());
					reserveData.setPriorityPass(sPriorityPass);

					reserveData = ExtendedObjectProxy.getReserveLotService().create(eventInfo, reserveData);
				}
			}
			finally
			{
				// remove delivered data
				for (int idx = 0; idx < currentBook.size(); idx++)
				{
					// Lot ID is unique
					if (reserveData != null && currentBook.get(idx).getLotName().equals(reserveData.getLotName())
							&& currentBook.get(idx).getProcessOperationName().equals(reserveData.getProcessOperationName())
							&& currentBook.get(idx).getProcessOperationVersion().equals(reserveData.getProcessOperationVersion()))
						currentBook.remove(idx);
				}
			}
		}

		// unassigned from book with no longer existing reservations
		for (ReserveLot data : currentBook)
		{
			try
			{
				eventInfo.setEventName("Remove");
				ExtendedObjectProxy.getReserveLotService().remove(eventInfo, data);
			}
			catch (greenFrameDBErrorSignal ne)
			{
				eventLog.warn(String.format("Lot[%s] has not been unassigned from Machine[%s]", data.getLotName(), data.getMachineName()));
			}
			catch (Exception ex)
			{
				eventLog.warn(String.format("Lot[%s] has not been unassigned from Machine[%s]", data.getLotName(), data.getMachineName()));
			}
		}

		return doc;

	}

	private List<ReserveLot> getCurrentReserveLotList(String machineName) throws CustomException
	{
		try
		{
			String query = "machineName = ? AND reserveState = ?  AND PROCESSOPERATIONNAME!='-' AND PROCESSOPERATIONVERSION!='-' ORDER BY position ";
			Object[] bindList = new Object[] { machineName, "Reserved" };

			List<ReserveLot> result = ExtendedObjectProxy.getReserveLotService().select(query, bindList);

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

		return new ArrayList<ReserveLot>();
	}
}
