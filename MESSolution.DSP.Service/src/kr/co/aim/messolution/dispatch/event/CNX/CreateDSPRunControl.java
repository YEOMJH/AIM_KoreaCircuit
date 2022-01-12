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

public class CreateDSPRunControl extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String processOperationVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "RECIPENAME", true);
		String useFlag = SMessageUtil.getBodyItemValue(doc, "USEFLAG", true);
		String useCountLimit = SMessageUtil.getBodyItemValue(doc, "USECOUNTLIMIT", true);
		String actionType = SMessageUtil.getBodyItemValue(doc, "ACTIONTYPE", false);
		
		if(!actionType.equals("ByProduct"))
			actionType = "MQC";

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateDSPRunControl", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		if (!checkExist(machineName, processOperationName, processOperationVersion))
		{
			throw new CustomException("MACHINE-0039", machineName, processOperationName, processOperationVersion);
		}

		ExtendedObjectProxy.getDSPRunControlService().createDSPRunControl(eventInfo, machineName, processOperationName, processOperationVersion, recipeName, factoryName, actionType, useFlag, 0,
				Integer.parseInt(useCountLimit));

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
