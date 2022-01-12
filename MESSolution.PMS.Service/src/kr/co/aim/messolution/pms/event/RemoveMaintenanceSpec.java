package kr.co.aim.messolution.pms.event;

import org.jdom.Document;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.MaintenanceSpec;
import kr.co.aim.messolution.pms.management.data.MaintenanceSpecKey;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;

public class RemoveMaintenanceSpec extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String maintGroupName = SMessageUtil.getBodyItemValue(doc, "MAINTENANCEGROUPNAME", true);
		String maintSpecName = SMessageUtil.getBodyItemValue(doc, "MAINTENANCESPECNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		
		MaintenanceSpecKey specKeyInfo = new MaintenanceSpecKey(maintGroupName, maintSpecName);
		MaintenanceSpec specData = PMSServiceProxy.getMaintSpecService().selectByKeyForUpdate(specKeyInfo);
		
		//purge spec items
		try
		{
			PMSServiceProxy.getMaintSpecItemService().delete("maintenanceGroupName = ? AND maintenanceSpecName = ?", new Object[] {maintGroupName, maintSpecName});
		}
		catch (CustomException ce)
		{
			eventLog.error(ce);
		}
		catch (NotFoundSignal ne)
		{
			eventLog.error(ne);
		}
		catch (FrameworkErrorSignal ex)
		{
			eventLog.error(ex);
		}
		
		PMSServiceProxy.getMaintSpecService().delete(specKeyInfo);
		
		return doc;
	}

}
