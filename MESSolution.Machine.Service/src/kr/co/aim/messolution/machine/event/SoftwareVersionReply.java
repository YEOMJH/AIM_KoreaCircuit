package kr.co.aim.messolution.machine.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineKey;

public class SoftwareVersionReply extends AsyncHandler {

	Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName =SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String softwareVersion = SMessageUtil.getBodyItemValue(doc, "SOFTWAREVERSION", true);
//		String result = SMessageUtil.getBodyItemValue(doc, "RESULT", true);
//		String resultDescription = SMessageUtil.getBodyItemValue(doc, "RESULTDESCRIPTION", true);
		
		Element bodyElement = SMessageUtil.getBodyElement(doc);

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(StringUtil.isEmpty(unitName)?machineName:unitName);
		
		bodyElement.addContent(new Element("FACTORYNAME").setText(machineData.getFactoryName()));
		bodyElement.addContent(new Element("AREANAME").setText(machineData.getAreaName()));
		bodyElement.addContent(new Element("MACHINEGROUPNAME").setText(machineData.getMachineGroupName()));
		bodyElement.addContent(new Element("COMMUNICATIONSTATE").setText(machineData.getCommunicationState()));
		bodyElement.addContent(new Element("MACHINESTATENAME").setText(machineData.getMachineStateName()));
      
		// send to OIC
		GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");
	}
}
