package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.TryRunControl;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class DeleteTryRunControl extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> DeleteMachineList = SMessageUtil.getBodySequenceItemList(doc, "DELETEMACHINELIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteTryRunControl", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		for (Element DeleteMachine : DeleteMachineList)
		{
			String machineName = SMessageUtil.getChildText(DeleteMachine, "MACHINENAME", true);
			String processOperationName = SMessageUtil.getChildText(DeleteMachine, "PROCESSOPERATIONNAME", true);
			String porcessOperationVersion = SMessageUtil.getChildText(DeleteMachine, "PROCESSOPERATIONVERSION", true);

			TryRunControl dataInfo = ExtendedObjectProxy.getTryRunControlService().selectByKey(false, new Object[] { machineName, processOperationName, porcessOperationVersion });
			
			ExtendedObjectProxy.getTryRunControlService().remove(eventInfo, dataInfo);
		}

		return doc;
	}

}
