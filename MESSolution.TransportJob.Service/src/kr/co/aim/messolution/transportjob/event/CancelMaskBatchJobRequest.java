package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class CancelMaskBatchJobRequest extends SyncHandler {

	/**
	 * MessageSpec [TEX -> MCS]
	 * 
	 * <Body>
	 *    <BATCHJOBNAME />
	 *    <CARRIERNAME />
	 * </Body>
	 */
	
	@SuppressWarnings("unchecked")
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelRequest", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String batchJobName = SMessageUtil.getBodyItemValue(doc, "BATCHJOBNAME", true);
		String messageName = SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", true);

		TransportJobCommand transprotJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceUtil().getTransportJobInfo(batchJobName);

		if (StringUtils.equals(transprotJobCommandInfo.getJobState(), "Completed") || StringUtils.equals(transprotJobCommandInfo.getJobState(), "Terminated")
				|| StringUtils.equals(transprotJobCommandInfo.getJobState(), "Rejected"))
		{
			throw new CustomException("JOB-2011", transprotJobCommandInfo.getJobState());
		}

		// Update CT_TRANSPORTJOBCOMMAND
		MESTransportServiceProxy.getTransportJobServiceUtil().updateTransportJobCommand(batchJobName, doc, eventInfo);

		// Get Mask TransportJobList by BatchJob
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT TRANSPORTJOBNAME ");
		sql.append("  FROM CT_TRANSPORTJOBCOMMAND ");
		sql.append(" WHERE BATCHJOBNAME = :BATCHJOBNAME ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("BATCHJOBNAME", batchJobName);

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		for (Map<String, Object> map : result)
		{
			// Get Mask Transport Job
			String transportJobName = ConvertUtil.getMapValueByName(map, "TRANSPORTJOBNAME");

			// Update CT_TRANSPORTJOBCOMMAND
			MESTransportServiceProxy.getTransportJobServiceUtil().updateTransportJobCommand(transportJobName, doc, eventInfo);
		}

		doc.getRootElement().getChild("Header").getChild("EVENTCOMMENT").setText(messageName);
		String replySubject = GenericServiceProxy.getESBServive().getSendSubject("MCS");
		GenericServiceProxy.getESBServive().sendReplyBySender(replySubject, doc, "HIFSender");

		return doc;
	}
}
