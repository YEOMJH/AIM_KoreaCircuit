package kr.co.aim.messolution.machine.event;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.MakeCommunicationStateInfo;

public class CommunicationStateChanged extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String communicationName = SMessageUtil.getBodyItemValue(doc, "COMMUNICATIONSTATE", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeCommState", getEventUser(), getEventComment(), null, null);

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		
		// clear sorter runtime info(use for CoverTrayGroupInfoDownLoad)
		if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Sorter) && StringUtils.equals(machineData.getFactoryName(), "POSTCELL"))
		{
			if (!StringUtils.equals(machineData.getCommunicationState(), communicationName))
			{
				if (MESPortServiceProxy.getPortServiceUtil().AllPortIsEmpty(machineName) && !CommonValidation.checkProcessingLotOnEQP(machineData.getFactoryName(),machineName))
					ExtendedObjectProxy.getRunTimeMachineInfoService().remove(machineName);
			}
		}

		MakeCommunicationStateInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeCommunicationStateInfo(machineData, communicationName);

		try
		{
			MESMachineServiceProxy.getMachineServiceImpl().makeCommunicationState(machineData, transitionInfo, eventInfo);
		}
		catch (Exception ex)
		{
			if (ex instanceof CustomException)
				eventLog.info(((CustomException) ex).errorDef.getLoc_errorMessage());
			else
				eventLog.info(ex.getCause());
		}

		// success then report to FMB
		GenericServiceProxy.getESBServive().sendBySenderToFMB(doc);
	}
}
