package kr.co.aim.messolution.recipe.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;

public class CurrentRecipeChanged extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		Log log = LogFactory.getLog(CurrentRecipeChanged.class);
		
		String machineName		= SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName		= SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String oldRecipeName    = SMessageUtil.getBodyItemValue(doc, "OLDRECIPENAME", true);
		String recipeName       = SMessageUtil.getBodyItemValue(doc, "RECIPENAME", true);
		String recipeType       = SMessageUtil.getBodyItemValue(doc, "RECIPETYPE", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CurrentRecipeChanged", getEventUser(), getEventComment(), null, null);
		MachineSpec machineSpec = getBaseMachine(machineName, unitName);
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineSpec.getKey().getMachineName());
		
		log.info("Current Recipe has been changed. MACHINENAME=" + machineName + " OLDRECIPENAME=" + oldRecipeName + " NEWRECIPENAME=" + recipeName );
		
		SetEventInfo setEventInfo = new SetEventInfo();
		
		MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setEventInfo, eventInfo);
	}
	
	private MachineSpec getBaseMachine(String machineName, String unitName) throws CustomException 
	{
		String keyName = "";

		if (!StringUtil.isEmpty(unitName))
			keyName = unitName;
		else if (!StringUtil.isEmpty(machineName))
			keyName = machineName;

		MachineSpec machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(keyName);

		return machineData;
	}
}
