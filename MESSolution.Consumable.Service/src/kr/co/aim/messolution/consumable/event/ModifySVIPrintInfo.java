package kr.co.aim.messolution.consumable.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SVIPickInfo;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class ModifySVIPrintInfo extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{

		List<Element> MACHINELIST = SMessageUtil.getBodySequenceItemList(doc, "MODIFYSETPRINTINFOLIST", true);

		for (Element MACHINE : MACHINELIST)
		{

				String timeKey = SMessageUtil.getChildText(MACHINE, "TIMEKEY", true);
				String downLoadFlag = SMessageUtil.getChildText(MACHINE, "DOWNLOADFLAG", true);

				EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifySVIPrintInfo", getEventUser(), getEventComment(), "", "");
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				SVIPickInfo pickInfo = ExtendedObjectProxy.getSVIPickInfoService().selectByKey(false, new Object[] { timeKey });

				StringBuffer inquirysql = new StringBuffer();
				inquirysql.append("SELECT M.MACHINENAME ");
				inquirysql.append("FROM MACHINE M,CT_SVIPICKINFO C ");
				inquirysql.append("WHERE M.MACHINENAME=C.MACHINENAME ");
				inquirysql.append("AND M.MACHINENAME=:MACHINENAME ");
				inquirysql.append("AND M.MACHINESTATENAME='RUN' ");
				inquirysql.append("AND C.DOWNLOADFLAG='Y' ");
				Map<String, String> inquirybindMap1 = new HashMap<String, String>();
				inquirybindMap1.put("MACHINENAME", pickInfo.getMachineName());
				List<Map<String, Object>> sqlResult6 = GenericServiceProxy.getSqlMesTemplate().queryForList(inquirysql.toString(), inquirybindMap1);

				if (!sqlResult6.isEmpty())
				{
					throw new CustomException("DEFECTCODE-0006", pickInfo.getTimeKey());
				}

				if (pickInfo.getDownLoadFlag().equals(downLoadFlag))
				{
					throw new CustomException("DEFECTCODE-0005", pickInfo.getTimeKey());
				}

				pickInfo.setDownLoadFlag(downLoadFlag);
				ExtendedObjectProxy.getSVIPickInfoService().modify(eventInfo, pickInfo);
		}
		return doc;
	}
}
