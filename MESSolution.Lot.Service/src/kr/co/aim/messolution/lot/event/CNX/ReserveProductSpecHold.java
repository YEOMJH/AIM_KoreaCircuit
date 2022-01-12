package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class ReserveProductSpecHold extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		Element eleBody = SMessageUtil.getBodyElement(doc);
		List<Element> eleReserveInfoList = SMessageUtil.getSubSequenceItemList(eleBody, "RESERVEINFOLIST", true);

		for (Element eleReserveInfo : eleReserveInfoList)
		{
			String factoryName = SMessageUtil.getChildText(eleReserveInfo, "FACTORYNAME", true);
			String productSpecName = SMessageUtil.getChildText(eleReserveInfo, "PRODUCTSPECNAME", true);
			String processFlowName = SMessageUtil.getChildText(eleReserveInfo, "PROCESSFLOWNAME", true);
			String processOperationName = SMessageUtil.getChildText(eleReserveInfo, "PROCESSOPERATIONNAME", true);
			String lotPriority = SMessageUtil.getChildText(eleReserveInfo, "LOTPRIORITY", true);
			String subProductionType = SMessageUtil.getChildText(eleReserveInfo, "SUBPRODUCTIONTYPE", true);
			String eventComment = SMessageUtil.getChildText(eleReserveInfo, "EVENTCOMMENT", true);
			String reasonCode = SMessageUtil.getChildText(eleReserveInfo, "REASONCODE", true);
			String reasonCodeTyoe = SMessageUtil.getChildText(eleReserveInfo, "REASONCODETYPE", true);
			String lastEventUser = SMessageUtil.getChildText(eleReserveInfo, "EVENTUSER", true);
			String actionType = SMessageUtil.getChildText(eleReserveInfo, "ACTIONTYPE", true);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
			eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

			if (actionType.equalsIgnoreCase("INSERT"))
			{
				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("FACTORYNAME", factoryName);
				bindMap.put("PRODUCTSPECNAME", productSpecName);
				bindMap.put("PROCESSFLOWNAME", processFlowName);
				bindMap.put("PROCESSOPERATIONNAME", processOperationName);
				bindMap.put("REASONCODE", reasonCode);
				bindMap.put("REASONCODETYPE", reasonCodeTyoe);
				bindMap.put("EVENTCOMMENT", eventComment);
				bindMap.put("LASTEVENTUSER", eventInfo.getEventUser());
				bindMap.put("EVENTUSER", lastEventUser);
				bindMap.put("LASTEVENTTIME", eventInfo.getEventTime());
				bindMap.put("LASTEVENTNAME", "Insert");

				StringBuffer querySql = new StringBuffer();
				querySql.append("SELECT 1 ");
				querySql.append("  FROM CT_RESERVEPRODUCTSPEC ");
				querySql.append(" WHERE FACTORYNAME = :FACTORYNAME ");
				querySql.append("   AND PRODUCTSPECNAME = :PRODUCTSPECNAME ");
				querySql.append("   AND PROCESSFLOWNAME = :PROCESSFLOWNAME ");
				querySql.append("   AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
				querySql.append("   AND LOTPRIORITY = :LOTPRIORITY ");
				querySql.append("   AND SUBPRODUCTIONTYPE = :SUBPRODUCTIONTYPE ");
				querySql.append("   AND REASONCODE = :REASONCODE ");
				querySql.append("   AND REASONCODETYPE = :REASONCODETYPE ");

				StringBuffer insertSql = new StringBuffer();
				insertSql.append("INSERT INTO CT_RESERVEPRODUCTSPEC  ");
				insertSql.append(" (FACTORYNAME, PRODUCTSPECNAME, PROCESSFLOWNAME, PROCESSOPERATIONNAME, LOTPRIORITY, ");
				insertSql.append("  SUBPRODUCTIONTYPE, REASONCODE, REASONCODETYPE, EVENTCOMMENT, LASTEVENTUSER, ");
				insertSql.append("  LASTEVENTNAME, LASTEVENTTIME) ");
				insertSql.append("VALUES  ");
				insertSql.append(" (:FACTORYNAME, :PRODUCTSPECNAME, :PROCESSFLOWNAME, :PROCESSOPERATIONNAME, :LOTPRIORITY, ");
				insertSql.append("  :SUBPRODUCTIONTYPE, :REASONCODE, :REASONCODETYPE, :EVENTCOMMENT, :LASTEVENTUSER, ");
				insertSql.append("  :LASTEVENTNAME, :LASTEVENTTIME) ");

				String[] priorityList = lotPriority.split(",");
				String[] subProductionTypeList = subProductionType.split(",");

				for (int i = 0; i < priorityList.length; i++)
				{
					for (int j = 0; j < subProductionTypeList.length; j++)
					{
						String p = priorityList[i];
						String s = subProductionTypeList[j];

						bindMap.put("LOTPRIORITY", p);
						bindMap.put("SUBPRODUCTIONTYPE", s);

						int queryResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(querySql.toString(), bindMap);
						if (queryResult > 0)
						{
							throw new CustomException("LOT-1012");
						}
						try
						{
							int result = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(insertSql.toString(), bindMap);
						}
						catch (Exception e)
						{
							throw new CustomException("SYS-8001", "insert into CT_RESERVEPRODUCTSPEC error : " + e.toString());
						}
					}
				}

			}
			else if (actionType.equalsIgnoreCase("DELETE"))
			{
				try
				{
					StringBuffer sql = new StringBuffer();
					sql.append("DELETE FROM CT_RESERVEPRODUCTSPEC ");
					sql.append(" WHERE FACTORYNAME = :FACTORYNAME ");
					sql.append("   AND PRODUCTSPECNAME = :PRODUCTSPECNAME ");
					sql.append("   AND PROCESSFLOWNAME = :PROCESSFLOWNAME ");
					sql.append("   AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
					sql.append("   AND LOTPRIORITY = :LOTPRIORITY ");
					sql.append("   AND SUBPRODUCTIONTYPE = :SUBPRODUCTIONTYPE ");
					sql.append("   AND REASONCODE = :REASONCODE ");
					sql.append("   AND REASONCODETYPE = :REASONCODETYPE ");

					Map<String, Object> bindMap = new HashMap<String, Object>();
					bindMap.put("FACTORYNAME", factoryName);
					bindMap.put("PRODUCTSPECNAME", productSpecName);
					bindMap.put("PROCESSFLOWNAME", processFlowName);
					bindMap.put("PROCESSOPERATIONNAME", processOperationName);
					bindMap.put("LOTPRIORITY", lotPriority);
					bindMap.put("SUBPRODUCTIONTYPE", subProductionType);
					bindMap.put("REASONCODE", reasonCode);
					bindMap.put("REASONCODETYPE", reasonCodeTyoe);

					int result = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(sql.toString(), bindMap);
				}
				catch (Exception e)
				{
					throw new CustomException("SYS-8001", "delete into CT_RESERVEPRODUCTSPEC error : " + e.toString());
				}
			}
		}

		return doc;

	}
}
