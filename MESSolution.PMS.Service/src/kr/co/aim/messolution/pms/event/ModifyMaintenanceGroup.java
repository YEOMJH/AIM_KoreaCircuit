package kr.co.aim.messolution.pms.event;

import org.jdom.Document;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.MaintenanceGroup;
import kr.co.aim.messolution.pms.management.data.MaintenanceGroupKey;

public class ModifyMaintenanceGroup extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String maintGroupName = SMessageUtil.getBodyItemValue(doc, "MAINTENANCEGROUPNAME", true);
		String description = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
		
		MaintenanceGroupKey groupKeyInfo = new MaintenanceGroupKey(maintGroupName);
		MaintenanceGroup groupData = PMSServiceProxy.getMaintGroupService().selectByKey(groupKeyInfo);
		
		if (!description.isEmpty())
			groupData.setDescription(description);
		
		PMSServiceProxy.getMaintGroupService().update(groupData);
		
		return doc;
	}

}
