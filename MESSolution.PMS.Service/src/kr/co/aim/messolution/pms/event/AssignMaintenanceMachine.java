package kr.co.aim.messolution.pms.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.MaintenanceGroup;
import kr.co.aim.messolution.pms.management.data.MaintenanceGroupKey;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;

public class AssignMaintenanceMachine extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String maintGroupName = SMessageUtil.getBodyItemValue(doc, "MAINTENANCEGROUPNAME", true);
		
		//EventInfo eventInfo = EventInfoUtil.makeEventInfo("Assign", getEventUser(), getEventComment(), null, null);
		
		MaintenanceGroupKey groupKeyInfo = new MaintenanceGroupKey(maintGroupName);
		MaintenanceGroup groupData = PMSServiceProxy.getMaintGroupService().selectByKey(groupKeyInfo);
		
		String groupName = groupData.getKey().getMaintenanceGroupName();
		Map<String, String> lateMemberList = new HashMap<String, String>();
		
		//group member
		List<MachineSpec> machineList;
		try
		{
			machineList = MachineServiceProxy.getMachineSpecService().select("maintenanceGroupName = ?", new Object[] {groupName});
		}
		catch (NotFoundSignal ne)
		{
			machineList = new ArrayList<MachineSpec>();
		}
		
		//compare with latest member list
		for (MachineSpec machineData : machineList)
		{
			boolean exist = false;
			
			for (Element eleMachine : SMessageUtil.getBodySequenceItemList(doc, "MACHINELIST", false))
			{
				String machineName = SMessageUtil.getChildText(eleMachine, "MACHINENAME", true);
				
				if (machineData.getKey().getMachineName().equals(machineName))
				{
					exist = true;
					break;
				}
			}
			
			if (exist)
			{//remain
				lateMemberList.put(machineData.getKey().getMachineName(), "E");
			}
			else
			{//additional
				lateMemberList.put(machineData.getKey().getMachineName(), "R");
			}
		}
		
		//compare with out-date member list
		for (Element eleMachine : SMessageUtil.getBodySequenceItemList(doc, "MACHINELIST", false))
		{
			String machineName = SMessageUtil.getChildText(eleMachine, "MACHINENAME", true);
			
			boolean isFound = false;
			
			for (String oldMachineName : lateMemberList.keySet())
			{
				if (oldMachineName.equals(machineName))
				{
					isFound = true;
					break;
				}
			}
			
			if (!isFound)
				lateMemberList.put(machineName, "N");
		}
		
		//proceed toward DB
		for (String lateMachineName : lateMemberList.keySet())
		{
			String flag = lateMemberList.get(lateMachineName);
			
			if (StringUtil.isNotEmpty(flag))
			{
				if (flag.equals("R"))
				{
					try
					{
						MachineSpec machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(lateMachineName);
						machineData.getUdfs().put("MAINTENANCEGROUPNAME", "");
						
						MachineServiceProxy.getMachineSpecService().update(machineData);
					}
					catch (Exception ex)
					{
						eventLog.error(ex);
					}
				}
				else if (flag.equals("N"))
				{
					try
					{
						MachineSpec machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(lateMachineName);
						machineData.getUdfs().put("MAINTENANCEGROUPNAME", groupName);
						
						MachineServiceProxy.getMachineSpecService().update(machineData);
					}
					catch (Exception ex)
					{
						eventLog.error(ex);
					}
				}
			}
		}
		
		return doc;
	}

}
