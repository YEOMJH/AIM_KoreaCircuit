package kr.co.aim.messolution.lot.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.jdom.Document;

public class OEDParameterDownload extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		
		String targetSubjectName = MESRecipeServiceProxy.getRecipeServiceUtil().getEASSubjectName(machineName);
		setHeaderItemValues(doc, machineData.getFactoryName(), machineName, targetSubjectName);
		GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "EISSender");
	}

	private void setHeaderItemValues(Document doc, String factoryName, String machineName, String targetSubjectName)
	{
		SMessageUtil.setItemValue(doc, "Header", "SHOPNAME", factoryName);
		SMessageUtil.setItemValue(doc, "Header", "MACHINENAME", machineName);
		SMessageUtil.setItemValue(doc, "Header", "SOURCESUBJECTNAME", GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("R2R"));
		SMessageUtil.setItemValue(doc, "Header", "TARGETSUBJECTNAME", targetSubjectName);
	}
}
