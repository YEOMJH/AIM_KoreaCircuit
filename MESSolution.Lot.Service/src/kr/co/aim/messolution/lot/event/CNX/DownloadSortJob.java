package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SortJob;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;

import org.jdom.Document;
import org.jdom.Element;

public class DownloadSortJob extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String jobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);

		List<SortJob> sortJobList = ExtendedObjectProxy.getSortJobService().getSortJobList(jobName);

		if (sortJobList == null)
		{
			//SORT-0013:Sorter Job already confirmed
			throw new CustomException("SORT-0013");
		}

		Element bodyElement = MESLotServiceProxy.getLotServiceUtil().createSortJobBodyElement(machineName, jobName);

		// first removal of existing node would be duplicated
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		// index of Body node is static
		doc.getRootElement().addContent(2, bodyElement);

		// from CNX to PEM
		String targetSubjectName = GenericServiceProxy.getESBServive().getSendSubject("PEMsvr");

		GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "PEMSender");
	}
}
