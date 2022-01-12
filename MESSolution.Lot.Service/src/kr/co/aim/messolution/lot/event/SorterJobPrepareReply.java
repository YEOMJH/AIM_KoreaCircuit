package kr.co.aim.messolution.lot.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.jdom.Document;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;

public class SorterJobPrepareReply extends AsyncHandler {

	private Log log = LogFactory.getLog(SorterJobPrepareReply.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String jobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);
		String Result = SMessageUtil.getBodyItemValue(doc, "RESULT", true);
		String ResultDescription = SMessageUtil.getBodyItemValue(doc, "RESULTDESCRIPTION", false);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("PrepareNG", getEventUser(), ResultDescription, null, null);

		if (StringUtil.equals(Result, "NG"))
		{
			MESLotServiceProxy.getLotServiceUtil().changeSortJobState(eventInfo, jobName, GenericServiceProxy.getConstantMap().SORT_JOBSTATE_RESERVED);
			log.info("Sorter Job Prepared Reply NG { " + ResultDescription + " }");
		}
		else
		{
			MESLotServiceProxy.getLotServiceUtil().changeSortJobState(eventInfo, jobName, GenericServiceProxy.getConstantMap().SORT_JOBSTATE_PREPARED);
			log.info("Sorter Job Prepared Reply OK !!");

			// get line machine
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

			Element bodyElement = MESLotServiceProxy.getLotServiceUtil().createSortJobBodyElement(machineName, jobName);

			// first removal of existing node would be duplicated
			doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
			// index of Body node is static
			doc.getRootElement().addContent(2, bodyElement);

			String targetSubjectName = CommonUtil.getValue(machineData.getUdfs(), "MCSUBJECTNAME");

			GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "EISSender");
		}

	}

}
