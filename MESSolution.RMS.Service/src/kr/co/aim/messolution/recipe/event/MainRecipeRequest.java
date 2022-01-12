package kr.co.aim.messolution.recipe.event;

import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.esb.ESBService;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;

public class MainRecipeRequest extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		// String targetMachineName = SMessageUtil.getBodyItemValue(doc, "TARGETNAME" , true);
		String recipeType = SMessageUtil.getBodyItemValue(doc, "RECIPETYPE", true);

		// get line machine
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

		// MES-EAP protocol
		String eventUser = machineData.getKey().getMachineName().substring(0, 6);
		SMessageUtil.setHeaderItemValue(doc, "EVENTUSER", eventUser);

		// String targetSubjectName = CommonUtil.getValue(machineData.getUdfs(), "MCSUBJECTNAME");
		String targetSubjectName = MESRecipeServiceProxy.getRecipeServiceUtil().getEASSubjectName(machineName);

		setHeaderItemValues(doc, machineData.getFactoryName(), machineName, targetSubjectName);

		// Log log = LogFactory.getLog(MainRecipeRequest.class);
		// log.info(JdomUtils.toString(doc));

		GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "EISSender");
	}

	private void setHeaderItemValues(Document doc, String factoryName, String machineName, String targetSubjectName)
	{
		SMessageUtil.setItemValue(doc, "Header", "SHOPNAME", factoryName);
		SMessageUtil.setItemValue(doc, "Header", "MACHINENAME", machineName);
		SMessageUtil.setItemValue(doc, "Header", "SOURCESUBJECTNAME", GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("RMSsvr"));
		SMessageUtil.setItemValue(doc, "Header", "TARGETSUBJECTNAME", targetSubjectName);
	}
}
