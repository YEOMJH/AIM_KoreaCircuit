package kr.co.aim.messolution.recipe.event;

import org.jdom.Document;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;

public class RecipeParameterRequest extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);

		// from PEX to EQP
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

		// MES-EAP protocol
		SMessageUtil.setHeaderItemValue(doc, "EVENTUSER", machineData.getKey().getMachineName());

		String targetSubjectName = CommonUtil.getValue(machineData.getUdfs(), "MCSUBJECTNAME");

		setHeaderItemValues(doc, machineData.getFactoryName(), machineName, targetSubjectName);

		GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "EISSender");

		//return doc;
	}

	private void setHeaderItemValues(Document doc, String factoryName, String machineName, String targetSubjectName)
	{
		SMessageUtil.setItemValue(doc, "Header", "SHOPNAME", factoryName);
		SMessageUtil.setItemValue(doc, "Header", "MACHINENAME", machineName);
		SMessageUtil.setItemValue(doc, "Header", "SOURCESUBJECTNAME", GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("RMSsvr"));
		SMessageUtil.setItemValue(doc, "Header", "TARGETSUBJECTNAME", targetSubjectName);
	}
}
