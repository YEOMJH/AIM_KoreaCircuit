package kr.co.aim.messolution.lot.event;

import org.jdom.Document;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class SorterJobAborted extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Abort", getEventUser(), getEventComment(), null, null);

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String jobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);

		MESLotServiceProxy.getLotServiceUtil().changeSortJobState(eventInfo, jobName, GenericServiceProxy.getConstantMap().SORT_JOBSTATE_ABORT);

	}

}
