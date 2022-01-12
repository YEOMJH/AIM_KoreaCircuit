package kr.co.aim.messolution.transportjob.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class CancelMaskBatchJobReply extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX -> OIC]
	 * 
	 * <Body>
	 *    <BATCHJOBNAME />
	 *    <CARRIERNAME />
	 * </Body>
	 */
	
	@SuppressWarnings("unchecked")
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String returnCode = SMessageUtil.getReturnItemValue(doc, "RETURNCODE", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelAccept", getEventUser(), getEventComment(), "", "");
		if (!StringUtils.equals(returnCode, "0"))
		{
			eventInfo.setEventName("CancelReject");
		}
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String oriEventName = eventInfo.getEventName();

		String batchJobName = SMessageUtil.getBodyItemValue(doc, "BATCHJOBNAME", true);

		// Unlock Carrier from Job
		if (StringUtils.equals(returnCode, "0"))
		{
			try
			{
				String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
				
				Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
				
				Map<String, String> udfs = new HashMap<String, String>();
				
				eventInfo.setEventName("Unlock");
				SetEventInfo setEventInfo = MESDurableServiceProxy.getDurableInfoUtil().setEventInfo(udfs);
				setEventInfo.getUdfs().put("TRANSPORTLOCKFLAG", "N");
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(carrierData, setEventInfo, eventInfo);
			}
			catch (Exception ex)
			{
				eventLog.error("Unlock failed");
			}
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

		eventInfo.setEventName(oriEventName);

		for (Map<String, Object> map : result)
		{
			// Get Mask Transport Job
			String transportJobName = ConvertUtil.getMapValueByName(map, "TRANSPORTJOBNAME");

			// Update CT_TRANSPORTJOBCOMMAND
			MESTransportServiceProxy.getTransportJobServiceUtil().updateTransportJobCommand(transportJobName, doc, eventInfo);
		}

		String originalSourceSubjectName = getOriginalSourceSubjectName();

		if (StringUtils.isNotEmpty(originalSourceSubjectName))
		{
			// Send Reply to OIC
			GenericServiceProxy.getESBServive().sendBySenderWithoutChangeReturnElement(originalSourceSubjectName, doc, "OICSender");
		}
	}
}
