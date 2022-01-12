package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;

import org.jdom.Document;

public class ConfirmAOILotJudge extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String LotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String ChangeAOILotJudge = SMessageUtil.getBodyItemValue(doc, "AOILOTJUDGE", true);
		String EventUser = SMessageUtil.getBodyItemValue(doc, "EVENTUSER", true);
		String CurrentTime = TimeStampUtil.getCurrentTime(TimeStampUtil.FORMAT_DEFAULT);
		String timeKey = TimeStampUtil.getCurrentEventTimeKey();

		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE CT_AOILOT ");
		sql.append("   SET LOTJUDGE = :AOILOTJUDGE, ");
		sql.append("       EVENTUSER = :EVENTUSER, ");
		sql.append("       EVENTNAME = :EVENTNAME, ");
		sql.append("       EVENTTIME = :EVENTTIME, ");
		sql.append("       TIMEKEY = :TIMEKEY ");
		sql.append(" WHERE TIMEKEY = (SELECT MAX (L.TIMEKEY) ");
		sql.append("                    FROM CT_AOILOT L ");
		sql.append("                   WHERE L.LOTNAME = :LOTNAME) ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("LOTNAME", LotName);
		bindMap.put("AOILOTJUDGE", ChangeAOILotJudge);
		bindMap.put("EVENTUSER", EventUser);
		bindMap.put("EVENTNAME", "ChangeLotJudge");
		bindMap.put("EVENTTIME", CurrentTime);
		bindMap.put("TIMEKEY", timeKey);

		GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(LotName);

		SetEventInfo setEventInfo = new SetEventInfo();

		if (ChangeAOILotJudge.equals("Y"))
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeLotJudge-Y", getEventUser(), getEventComment(), "", "");
			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
		}
		else
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeLotJudge-N", getEventUser(), getEventComment(), "", "");
			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
		}

		return doc;

	}
}
