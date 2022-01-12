package kr.co.aim.messolution.machine.event;

import java.util.Map;

import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ProductIdleTime;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;

public class CreateMachineIDLETime extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		String factoryName=SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String checkIdleTime=SMessageUtil.getBodyItemValue(doc, "CHECKIDLETIME", true);
		String  maxIdleTime=SMessageUtil.getBodyItemValue(doc, "MAXIDLETIME", true);
		MachineSpec machineData=MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
		
	    EventInfo eventInfo =new EventInfo();
	    eventInfo= EventInfoUtil.makeEventInfo("ModifyMachineIdleTimeInfo", getEventUser(), getEventComment(), "", "");
	    Map<String ,String>Machineudfs=machineData.getUdfs();
	    Machineudfs.put("CHECKIDLETIME", checkIdleTime);
	    Machineudfs.put("MAXIDLETIME", maxIdleTime);
	    MESMachineServiceProxy.getMachineServiceImpl().setEventMachineSpec(eventInfo, machineData, Machineudfs);
		return doc;
	}

}