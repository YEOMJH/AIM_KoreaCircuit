package kr.co.aim.messolution.lot.event;

import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class MaskStateReport extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		// Get Messige Data
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		List<Element> unitElementList = SMessageUtil.getBodySequenceItemList(doc, "UNITLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Report", getEventUser(), getEventComment(), "", "");

		for (Element eUnit : unitElementList)
		{
			String sUnitName = SMessageUtil.getChildText(eUnit, "UNITNAME", true);
			List<Element> eMaskElement = SMessageUtil.getSubSequenceItemList(eUnit, "MASKLIST", true);
			List<Element> eSubUnitElement = SMessageUtil.getSubSequenceItemList(eUnit, "SUBUNITLIST", false);

			for (Element eMask : eMaskElement)
			{
				String sUnitMaskName = SMessageUtil.getChildText(eMask, "MASKNAME", true);
				String sUnitMaskStateName = SMessageUtil.getChildText(eMask, "MASKSTATENAME", true);
				String sUnitMaskUsedCount = SMessageUtil.getChildText(eMask, "MASKUSEDCOUNT", true);

				changeMaskState(sUnitMaskName, sUnitMaskStateName, machineName, sUnitName, "", sUnitMaskUsedCount, eventInfo);
			}

			for (Element eSubUnit : eSubUnitElement)
			{
				String sSubUnitUnitName = SMessageUtil.getChildText(eSubUnit, "SUBUNITNAME", true);
				List<Element> eSubUnitMaskElement = SMessageUtil.getSubSequenceItemList(eSubUnit, "MASKLIST", true);

				for (Element eSubUnitMask : eSubUnitMaskElement)
				{
					String sSubUnitMaskName = SMessageUtil.getChildText(eSubUnitMask, "MASKNAME", true);
					String sSubUnitMaskStateName = SMessageUtil.getChildText(eSubUnitMask, "MASKSTATENAME", true);
					String sSubUnitMaskUsedCount = SMessageUtil.getChildText(eSubUnitMask, "MASKUSEDCOUNT", true);

					changeMaskState(sSubUnitMaskName, sSubUnitMaskStateName, machineName, sUnitName, sSubUnitUnitName, sSubUnitMaskUsedCount, eventInfo);
				}
			}
		}
	}

	private void changeMaskState(String sUnitMaskName, String sUnitMaskStateName, String machineName, String sUnitName, String sSubUnitName, String sUnitMaskUsedCount, EventInfo eventInfo)
			throws CustomException
	{
		// Get mask data
		Durable maskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sUnitMaskName);
		EventInfo sEventInfo = EventInfoUtil.makeEventInfo("Change", getEventUser(), getEventComment(), "", "");

		if (sUnitMaskStateName.equals("Mount"))
		{
			// Set DurableState
			maskData.setDurableState("Mount");
			DurableServiceProxy.getDurableService().update(maskData);

			// Do Assign
			SetEventInfo assignMaskInfo = MESDurableServiceProxy.getDurableInfoUtil().setAssignMaskInfo(maskData.getDurableType(), machineName, sUnitName, sSubUnitName, "");

			MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskData, assignMaskInfo, sEventInfo);

		}
		else
		{
			// Set DurableState
			maskData.setDurableState("Umount");
			DurableServiceProxy.getDurableService().update(maskData);

			// Do Deassign
			SetEventInfo setEventInfo = MESDurableServiceProxy.getDurableInfoUtil().setDeassignMaskInfo(maskData.getDurableType(), "", "", "");
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(maskData, setEventInfo, sEventInfo);
		}
	}
}
