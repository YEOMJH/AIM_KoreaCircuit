package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class CreateChangeRunControl extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String useFlag = SMessageUtil.getBodyItemValue(doc, "USEFLAG", true);
		String useCountLimit = SMessageUtil.getBodyItemValue(doc, "USECOUNTLIMIT", true);

		List<Element> operationList = SMessageUtil.getBodySequenceItemList(doc, "OPERATIONLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateChangeRunControl", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		for (Element operation : operationList)
		{
			String processOperationName = SMessageUtil.getChildText(operation, "PROCESSOPERATIONNAME", true);
			String processOperationVersion = SMessageUtil.getChildText(operation, "PROCESSOPERATIONVERSION", true);

			if (!checkExist(machineName, processOperationName, processOperationVersion))
			{
				throw new CustomException("MACHINE-0041", machineName, processOperationName, processOperationVersion);
			}

			ExtendedObjectProxy.getChangeRunControlService().createChangeRunControl(eventInfo, machineName, processOperationName, processOperationVersion, factoryName, "ChangeOperaion", "N", useFlag,
					0, Integer.parseInt(useCountLimit));
		}

		return doc;
	}

	private boolean checkExist(String machineName, String processOperationName, String processOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		try
		{
			ExtendedObjectProxy.getChangeRunControlService().selectByKey(false, new Object[] { machineName, processOperationName, processOperationVersion });
			return false;
		}
		catch (Exception e)
		{
			return true;
		}
	}
}
