package kr.co.aim.messolution.pms.event;

import org.jdom.Document;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.MaintenanceSpec;
import kr.co.aim.messolution.pms.management.data.MaintenanceSpecKey;

public class ModifyMainenanceSpec extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String maintGroupName = SMessageUtil.getBodyItemValue(doc, "MAINTENANCEGROUPNAME", true);
		String maintSpecName = SMessageUtil.getBodyItemValue(doc, "MAINTENANCESPECNAME", true);
		//String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		
		MaintenanceSpecKey specKeyInfo = new MaintenanceSpecKey(maintGroupName, maintSpecName);
		MaintenanceSpec specData = PMSServiceProxy.getMaintSpecService().selectByKeyForUpdate(specKeyInfo);
		
		String maintType = SMessageUtil.getBodyItemValue(doc, "MAINTENANCETYPE", true);
		String cycleType = SMessageUtil.getBodyItemValue(doc, "CYCLETYPE", true);
		String cycleDuration = SMessageUtil.getBodyItemValue(doc, "CYCLEDURATION", true);
		String sExecuteDay = SMessageUtil.getBodyItemValue(doc, "EXEDAY", true);
		String sExecuteStartHour = SMessageUtil.getBodyItemValue(doc, "EXESTARTHOUR", true);
		String sExecuteDuration = SMessageUtil.getBodyItemValue(doc, "EXEDURATION", true);
		
		specData.setCycleType(cycleType);
		specData.setMaintenanceType(maintType);
		
		try
		{
			specData.setCycleDuration(Integer.parseInt(cycleDuration));
			specData.setExeDay(Integer.parseInt(sExecuteDay));
			specData.setExeDuration(Integer.parseInt(sExecuteDuration));
			specData.setExeStartHour(Integer.parseInt(sExecuteStartHour));
		}
		catch (NumberFormatException ne)
		{
			//PMS-0002: Invalid numeric format detected in message
			throw new CustomException("PMS-0002");
		}
		
		PMSServiceProxy.getMaintSpecService().update(specData);
		
		return doc;
	}

}
