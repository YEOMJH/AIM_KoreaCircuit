package kr.co.aim.messolution.pms.event;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.MaintenanceGroup;
import kr.co.aim.messolution.pms.management.data.MaintenanceGroupKey;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;

public class RemoveMaintenanceGroup extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String maintGroupName = SMessageUtil.getBodyItemValue(doc, "MAINTENANCEGROUPNAME", true);
		
		MaintenanceGroupKey groupKeyInfo = new MaintenanceGroupKey(maintGroupName);
		MaintenanceGroup groupData = PMSServiceProxy.getMaintGroupService().selectByKey(groupKeyInfo);
		
		String groupName = groupData.getKey().getMaintenanceGroupName();
		
		List<MachineSpec> machineList;
		try
		{
			machineList = MachineServiceProxy.getMachineSpecService().select("maintenanceGroupName = ?", new Object[] {groupName});
		}
		catch (NotFoundSignal ne)
		{
			machineList = new ArrayList<MachineSpec>();
		}
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Deassign", getEventUser(), getEventComment(), null, null);
		
		for (MachineSpec machineData : machineList)
		{
			try
			{
				machineData.getUdfs().put("MAINTENANCEGROUPNAME", "");
				
				MachineServiceProxy.getMachineSpecService().update(machineData);
			}
			//catch (CustomException ce)
			//{
			//	throw ce;
			//}
			catch (NotFoundSignal ne)
			{
				//PMS-0003: Group[{0}] appointment faied
				throw new CustomException("PMS-0003", groupName);
			}
			catch (FrameworkErrorSignal fe)
			{
				throw new CustomException("PMS-0003", groupName);
			}
		}
		
		PMSServiceProxy.getMaintGroupService().delete(groupKeyInfo);
		
		return doc;
	}

}
