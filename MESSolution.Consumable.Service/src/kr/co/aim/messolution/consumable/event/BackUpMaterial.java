package kr.co.aim.messolution.consumable.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class BackUpMaterial extends SyncHandler{
	
	public Object doWorks(Document doc) throws CustomException{
	
		String materialSpecName = SMessageUtil.getBodyItemValue(doc, "MATERIALSPECNAME", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		StringBuffer sql1=new StringBuffer();
		sql1.append(" SELECT DISTINCT FACTORYNAME,PRODUCTREQUESTNAME,PROCESSOPERATIONNAME,SUBSTITUTEGROUP  ");
		sql1.append("  FROM CT_ERPBOM ");
		sql1.append(" WHERE MATERIALSPECNAME = :MATERIALSPECNAME AND SUBSTITUTEGROUP IS NOT NULL ");
		
		Map<String, String> bindMap1 = new HashMap<String, String>();
		bindMap1.put("MATERIALSPECNAME", materialSpecName);
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql1.toString(), bindMap1);
		for (int i = 0; i < result.size(); i++) 
		{
			try
			{
				StringBuffer sql2 = new StringBuffer();
				sql2.append("UPDATE CT_ERPBOM ");
				sql2.append("   SET KITFLAG='' , ");
				sql2.append("       LASTEVENTNAME='UnKitBackUpMaterial', ");
				sql2.append("       LASTEVENTTIMEKEY=:LASTEVENTTIMEKEY, ");
				sql2.append("       LASTEVENTTIME=:LASTEVENTTIME, ");
				sql2.append("       LASTEVENTUSER=:LASTEVENTUSER, ");
				sql2.append("       LASTEVENTCOMMENT=:LASTEVENTCOMMENT ");
				sql2.append("       WHERE FACTORYNAME=:FACTORYNAME ");
				sql2.append("       AND PRODUCTREQUESTNAME=:PRODUCTREQUESTNAME");
				sql2.append("       AND PROCESSOPERATIONNAME=:PROCESSOPERATIONNAME");
				sql2.append("       AND SUBSTITUTEGROUP=:SUBSTITUTEGROUP");

				Map<String, Object> bindMap2 = new HashMap<String, Object>();
				bindMap2.put("LASTEVENTTIMEKEY", eventInfo.getEventTimeKey());
				bindMap2.put("LASTEVENTTIME", eventInfo.getEventTime());
				bindMap2.put("LASTEVENTUSER", eventInfo.getEventUser());
				bindMap2.put("LASTEVENTCOMMENT", eventInfo.getEventComment());
				bindMap2.put("FACTORYNAME", result.get(i).get("FACTORYNAME").toString());
				bindMap2.put("PRODUCTREQUESTNAME", result.get(i).get("PRODUCTREQUESTNAME").toString());
				bindMap2.put("PROCESSOPERATIONNAME", result.get(i).get("PROCESSOPERATIONNAME").toString());
				bindMap2.put("SUBSTITUTEGROUP", result.get(i).get("SUBSTITUTEGROUP").toString());

				

				GenericServiceProxy.getSqlMesTemplate().update(sql2.toString(), bindMap2);
			}
			catch (Exception e)
			{
				throw new CustomException("SYS-8001", "Update Failed: " + e.toString());
			}
		}
		
		
		try
		{
			StringBuffer sql3 = new StringBuffer();
			sql3.append("UPDATE CT_ERPBOM ");
			sql3.append("   SET KITFLAG='Y' , ");
			sql3.append("       LASTEVENTNAME='KitBackUpMaterial', ");
			sql3.append("       LASTEVENTTIMEKEY=:LASTEVENTTIMEKEY, ");
			sql3.append("       LASTEVENTTIME=:LASTEVENTTIME, ");
			sql3.append("       LASTEVENTUSER=:LASTEVENTUSER, ");
			sql3.append("       LASTEVENTCOMMENT=:LASTEVENTCOMMENT ");
			sql3.append("       WHERE MATERIALSPECNAME=:MATERIALSPECNAME ");
			sql3.append("       AND SUBSTITUTEGROUP IS NOT NULL ");

			Map<String, Object> bindMap3 = new HashMap<String, Object>();
			bindMap3.put("LASTEVENTTIMEKEY", eventInfo.getEventTimeKey());
			bindMap3.put("LASTEVENTTIME", eventInfo.getEventTime());
			bindMap3.put("LASTEVENTUSER", eventInfo.getEventUser());
			bindMap3.put("LASTEVENTCOMMENT", eventInfo.getEventComment());
			bindMap3.put("MATERIALSPECNAME", materialSpecName);

			

			GenericServiceProxy.getSqlMesTemplate().update(sql3.toString(), bindMap3);
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", "Update Failed: " + e.toString());
		}
		
		
		
		return doc;
	}

}
