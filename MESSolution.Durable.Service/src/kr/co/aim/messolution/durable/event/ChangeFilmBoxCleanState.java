package kr.co.aim.messolution.durable.event;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.DirtyInfo;
import kr.co.aim.greentrack.durable.management.info.RepairInfo;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;

public class ChangeFilmBoxCleanState extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String messageName = SMessageUtil.getMessageName(doc);
		String durableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		String durableCleanState = SMessageUtil.getBodyItemValue(doc, "DURABLECLEANSTATE", true);

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(messageName, getEventUser(), getEventComment(), "", "");

		if (durableCleanState.equals(GenericServiceProxy.getConstantMap().Dur_Clean))
		{
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("LASTCLEANTIME", eventInfo.getEventTime().toString());
			
			Timestamp sLastCleanTimeNew = TimeStampUtil.getTimestamp(eventInfo.getEventTime().toString());

			StringBuilder updateSql = new StringBuilder();
			updateSql.append("UPDATE DURABLE ");
			updateSql.append("   SET DURABLECLEANSTATE = :DURABLECLEANSTATE, LASTCLEANTIME = :LASTCLEANTIME ");
			updateSql.append(" WHERE DURABLENAME = :DURABLENAME ");

			Map<String, Object> updateMap = new HashMap<String, Object>();
			updateMap.put("DURABLENAME", durableName);
			updateMap.put("DURABLECLEANSTATE", durableCleanState);
			updateMap.put("LASTCLEANTIME", sLastCleanTimeNew);
			GenericServiceProxy.getSqlMesTemplate().update(updateSql.toString(), updateMap);
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
		}
		else if (durableCleanState.equals(GenericServiceProxy.getConstantMap().Dur_Dirty))
		{
			DirtyInfo dirtyInfo = new DirtyInfo();
			MESDurableServiceProxy.getDurableServiceImpl().dirty(durableData, dirtyInfo, eventInfo);
		}
		else if (durableCleanState.equals(GenericServiceProxy.getConstantMap().Dur_Repairing))
		{
			RepairInfo repairInfo = new RepairInfo();
			MESDurableServiceProxy.getDurableServiceImpl().repair(durableData, repairInfo, eventInfo);
		}

		return doc;
	}
}
