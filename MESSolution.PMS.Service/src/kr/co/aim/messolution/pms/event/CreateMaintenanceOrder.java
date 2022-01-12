package kr.co.aim.messolution.pms.event;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.MaintenanceOrder;
import kr.co.aim.messolution.pms.management.data.MaintenanceOrderItem;
import kr.co.aim.messolution.pms.management.data.MaintenanceOrderItemKey;
import kr.co.aim.messolution.pms.management.data.MaintenanceOrderKey;
import kr.co.aim.messolution.pms.management.data.MaintenanceSpec;
import kr.co.aim.messolution.pms.management.data.MaintenanceSpecKey;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;

public class CreateMaintenanceOrder extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {

		String maintGroupName = SMessageUtil.getBodyItemValue(doc, "MAINTENANCEGROUPNAME", true);
		String maintSpecName = SMessageUtil.getBodyItemValue(doc, "MAINTENANCESPECNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String planStartTime = SMessageUtil.getBodyItemValue(doc, "PLANSTARTTIME", true);
		String planEndTime = SMessageUtil.getBodyItemValue(doc, "PLANENDTIME", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), null, null);
		
		MaintenanceSpecKey specKey = new MaintenanceSpecKey(maintGroupName, maintSpecName);
		MaintenanceSpec specInfo = PMSServiceProxy.getMaintSpecService().selectByKey(specKey);
		
		MachineSpec machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
		
		String maintOrderName = machineName + "-" + maintSpecName;
		
		//Order
		MaintenanceOrderKey orderKey = new MaintenanceOrderKey(maintOrderName);
		MaintenanceOrder orderData = new MaintenanceOrder(orderKey);
		
		orderData.setCreateTime(eventInfo.getEventTime());
		orderData.setCreateUser(eventInfo.getEventUser());

		orderData.setMachineName(machineName);
		orderData.setMaintenanceGroupName(CommonUtil.getValue(machineData.getUdfs(), "MAINTENANCEGROUPNAME"));
		orderData.setMaintenanceOrderState("Created");
		orderData.setMaintenanceSpecName(maintSpecName);
		orderData.setMaintenanceType(specInfo.getMaintenanceType());
		orderData.setPlanStartTime(TimeUtils.getTimestamp(planStartTime));
		orderData.setPlanEndTime(TimeUtils.getTimestamp(planEndTime));
		
		PMSServiceProxy.getOrderService().insert(orderData);
		
		for (Element eleItem : SMessageUtil.getBodySequenceItemList(doc, "MAINTENANCEORDERITEMLIST", false))
		{
			if(!eleItem.equals(null))
			{
				try
				{
					String sPosition = SMessageUtil.getChildText(eleItem, "POSITION", true);
					long position = Long.parseLong(sPosition);
					
					String item = SMessageUtil.getChildText(eleItem, "MAINTENANCEITEM", false);
					String note = SMessageUtil.getChildText(eleItem, "MAINTENANCENOTE", false);
					
					MaintenanceOrderItemKey itemKey = new MaintenanceOrderItemKey(maintOrderName, position);
					MaintenanceOrderItem itemData = new MaintenanceOrderItem(itemKey);
					
					itemData.setMaintenanceFlag("N");
					itemData.setMaintenanceItem(item);
					itemData.setMaintenanceNote(note);
					
					PMSServiceProxy.getOrderItemService().insert(itemData);
				}
				catch (Exception ex)
				{
					eventLog.error(ex);
				}
			}
		}
		
		return doc;
	}

}
