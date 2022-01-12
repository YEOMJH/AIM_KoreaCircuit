package kr.co.aim.messolution.dispatch.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class CreateDSPControl extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String processOperationVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		String dspFlag = SMessageUtil.getBodyItemValue(doc, "DSPFLAG", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateDSPControl", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		if (!checkExist(machineName, processOperationName, processOperationVersion))
		{
			throw new CustomException("MACHINE-0039", machineName, processOperationName, processOperationVersion);
		}

		ExtendedObjectProxy.getDSPControlService().createDSPControl(eventInfo, machineName, processOperationName, processOperationVersion, factoryName, dspFlag, portName);
		
		return doc;
	}

	private boolean checkExist(String machineName, String processOperationName, String processOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		try
		{
			ExtendedObjectProxy.getDSPRunControlService().selectByKey(false, new Object[] { machineName, processOperationName, processOperationVersion });
			return false;
		}
		catch (Exception e)
		{
			return true;
		}
	}

}
