package kr.co.aim.messolution.durable.event.CNX;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ImportMaskAbnormalList extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<String> maskIdList = new ArrayList<String>();
		List<Object[]> batchArgs = new ArrayList<Object[]>();
		List<Element> maskList = SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ImportMaskAbnormalList", this.getEventUser(), this.getEventComment(), "", "");
		String timeKey = TimeStampUtil.getCurrentEventTimeKey();

		for (Element mask : maskList)
		{
			String maskName = SMessageUtil.getChildText(mask, "MASKNAME", true);
			String chamberName = SMessageUtil.getChildText(mask, "CHAMBERNAME", false);
			String checkDate = SMessageUtil.getChildText(mask, "CHECKDATE", false);
			String lossSituation = SMessageUtil.getChildText(mask, "LOSSSITUATION", false);
			String cause = SMessageUtil.getChildText(mask, "CAUSE", false);
			String processing = SMessageUtil.getChildText(mask, "PROCESSING", false);
			String result = SMessageUtil.getChildText(mask, "RESULT", false);
			String remark = SMessageUtil.getChildText(mask, "REMARK", false);
			Timestamp checkedTime = null;

			if (maskIdList.contains(maskName))
				timeKey = TimeStampUtil.getCurrentEventTimeKey();
			else
				maskIdList.add(maskName);

			if (StringUtils.isNotEmpty(checkDate))
			{
				try
				{
					checkedTime = TimeUtils.getTimestamp(checkDate);
				}
				catch (Exception e)
				{
				}
			}

			batchArgs.add(new Object[] { maskName, timeKey, chamberName, checkedTime, lossSituation, cause, processing, result, remark, eventInfo.getEventUser() });
		}

		if (batchArgs.size() > 0)
		{
			StringBuilder sql = new StringBuilder();
			sql.append("INSERT INTO CT_MASKABNORMALLIST ");
			sql.append("   (MASKLOTNAME, TIMEKEY, CHAMBERNAME, CHECKEDTIME, LOSSSITUATION,  ");
			sql.append("    CAUSE, PROCESSING, RESULT, REMARK, CREATEUSER,  ");
			sql.append("    CREATETIME) ");
			sql.append(" VALUES ");
			sql.append("   (:MASKLOTNAME, :TIMEKEY, :CHAMBERNAME, :CHECKEDTIME, :LOSSSITUATION, ");
			sql.append("    :CAUSE, :PROCESSING, :RESULT, :REMARK, :CREATEUSER, ");
			sql.append("    SYSDATE) ");

			GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().batchUpdate(sql.toString(), batchArgs);
		}

		return doc;
	}

}
