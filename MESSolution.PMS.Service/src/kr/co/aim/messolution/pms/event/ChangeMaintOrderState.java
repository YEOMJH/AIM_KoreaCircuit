package kr.co.aim.messolution.pms.event;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.MaintenanceOrder;
import kr.co.aim.messolution.pms.management.data.MaintenanceOrderItem;
import kr.co.aim.messolution.pms.management.data.MaintenanceOrderItemKey;
import kr.co.aim.messolution.pms.management.data.MaintenanceOrderKey;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class ChangeMaintOrderState extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String maintOrderName = SMessageUtil.getBodyItemValue(doc, "MAINTENANCEORDERNAME", true);
		String maintStateName = SMessageUtil.getBodyItemValue(doc, "MAINTENANCESTATENAME", true);
		String ChangeMaintStateName = SMessageUtil.getBodyItemValue(doc, "CHANGESTATENAME", true);
		long position = 0;
		String maintItem = "";
		String maintFlag = "";
		String maintNote = "";
		
		for (Element eleMaintSpec : SMessageUtil.getBodySequenceItemList(doc, "MAINTENANCEORDERITEMLIST", false))
		{
			String sPosition = SMessageUtil.getChildText(eleMaintSpec, "POSITION", false);
			
			try
			{
				position = Integer.parseInt(sPosition);
				maintItem = SMessageUtil.getChildText(eleMaintSpec, "MAINTENANCEITEM", true);
				maintFlag = SMessageUtil.getChildText(eleMaintSpec, "MAINTENANCEFLAG", true);
				
				if(eleMaintSpec.getChildText("MAINTENANCENOTE") == "")
				{
					maintNote = "";
				}
				else
				{
					maintNote = SMessageUtil.getChildText(eleMaintSpec, "MAINTENANCENOTE", true);
				}
			}
			catch (Exception ex)
			{
				//eventLog.error(ex);
			    //PMS-0001:[{0}]th maintenance item creation is failed
				throw new CustomException("PMS-0001", sPosition);
			}
		
			MaintenanceOrderKey orderKey = new MaintenanceOrderKey(maintOrderName);
			MaintenanceOrder orderData = PMSServiceProxy.getOrderService().selectByKey(orderKey);
			
			MaintenanceOrderItemKey orderItemKey = new MaintenanceOrderItemKey(maintOrderName, position);
			MaintenanceOrderItem orderItemData = PMSServiceProxy.getOrderItemService().selectByKey(orderItemKey);
			
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("Change", getEventUser(), getEventComment(), null, null);
			orderData.setMaintenanceOrderState(ChangeMaintStateName);
			
			if (ChangeMaintStateName.equals("Ordered"))
			{
				orderData.setOrderTime(eventInfo.getEventTime());
				orderData.setOrderUser(eventInfo.getEventUser());
			}
			else if (ChangeMaintStateName.equals("Executing"))
			{
				orderData.setStartTime(eventInfo.getEventTime());
				orderData.setStartUser(eventInfo.getEventUser());
			}
			else if (ChangeMaintStateName.equals("Completed"))
			{
				maintFlag = "Y";
				orderData.setEndTime(eventInfo.getEventTime());
				orderData.setEndUser(eventInfo.getEventUser());
			}
			else if (ChangeMaintStateName.equals("Canceled"))
			{
				orderData.setMaintenanceOrderState(ChangeMaintStateName);
			}
			else if (ChangeMaintStateName.equals("Aborted"))
			{
				orderData.setMaintenanceOrderState(ChangeMaintStateName);
			}
			
			PMSServiceProxy.getOrderService().update(orderData);
			
			orderItemData.setMaintenanceItem(maintItem);
			orderItemData.setMaintenanceFlag(maintFlag);
			orderItemData.setMaintenanceNote(maintNote);
			PMSServiceProxy.getOrderItemService().update(orderItemData);
		}
		return doc;
	}

}
