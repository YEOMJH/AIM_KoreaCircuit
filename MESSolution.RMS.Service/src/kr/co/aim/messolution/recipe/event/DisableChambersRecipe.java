package kr.co.aim.messolution.recipe.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.map.ListOrderedMap;
import org.jdom.Document;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class DisableChambersRecipe extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DisableChambersRecipe", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", false);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "RECIPENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String chamberName = SMessageUtil.getBodyItemValue(doc, "CHAMBERNAME", true);
		String checkFlag = SMessageUtil.getBodyItemValue(doc, "CHECKFLAG", true);

		StringBuffer sqlBuffer = new StringBuffer();
		sqlBuffer.append(" SELECT * ");
		sqlBuffer.append("   FROM CT_RECIPEPARAMETER RP ");
		sqlBuffer.append("  WHERE RP.MACHINENAME = ? ");
		sqlBuffer.append("	 AND RP.RECIPENAME = ");
		sqlBuffer.append("   (SELECT R.VALUE ");
		sqlBuffer.append("   FROM CT_RECIPEPARAMETER R ");
		sqlBuffer.append("   WHERE     R.MACHINENAME = ? ");
		sqlBuffer.append("   AND R.RECIPEPARAMETERNAME = ? ");
		sqlBuffer.append("   AND R.RECIPENAME = ?) ");
		sqlBuffer.append("   AND RP.RECIPEPARAMETERNAME LIKE :CHAMBERNAME ");

		String sqlStmt = sqlBuffer.toString();
		Object[] bindSet = new String[] { unitName, machineName, unitName, recipeName, chamberName + "%" };

		List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStmt, bindSet);

		if (sqlResult.size() > 0) {
			StringBuffer sql = new StringBuffer();
			sql.append(" UPDATE CT_RECIPEPARAMETER RP  ");
			sql.append(" SET RP.CHECKFLAG = ? ");
			sql.append(" WHERE     RP.MACHINENAME = ? ");
			sql.append(" AND RP.RECIPENAME = ");
			sql.append(" (SELECT R.VALUE ");
			sql.append(" FROM CT_RECIPEPARAMETER R ");
			sql.append(" WHERE     R.MACHINENAME = ? ");
			sql.append(" AND R.RECIPEPARAMETERNAME = ? ");
			sql.append(" AND R.RECIPENAME = ?) ");
			sql.append(" AND RP.RECIPEPARAMETERNAME LIKE ? ");

			Object[] bindSet1 = new String[] { checkFlag, unitName, machineName, unitName, recipeName,
					chamberName + "%" };
			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindSet1);

			List<Object[]> updateHisList = new ArrayList<Object[]>();

			for (ListOrderedMap parameter : sqlResult) {
				String unitRecipeName = (String) sqlResult.get(0).get("RECIPENAME");
				String recipeParameterName = CommonUtil.getValue(parameter, "RECIPEPARAMETERNAME");
				String value = CommonUtil.getValue(parameter, "VALUE");
				String validationType = CommonUtil.getValue(parameter, "VALIDATIONTYPE");
				String target = CommonUtil.getValue(parameter, "TARGET");
				String lowerLimit = CommonUtil.getValue(parameter, "LOWERLIMIT");
				String upperLimit = CommonUtil.getValue(parameter, "UPPERLIMIT");
				String result = CommonUtil.getValue(parameter, "RESULT");

				List<Object> updateList = new ArrayList<Object>();

				updateList.add(unitName);
				updateList.add(unitRecipeName);
				updateList.add(recipeParameterName);
				updateList.add(eventInfo.getEventTimeKey());
				updateList.add(eventInfo.getEventName());
				updateList.add(value);
				updateList.add(validationType);
				updateList.add(target);
				updateList.add(lowerLimit);
				updateList.add(upperLimit);
				updateList.add(result);
				updateList.add(eventInfo.getEventUser());
				updateList.add(checkFlag);

				updateHisList.add(updateList.toArray());
			}
			StringBuilder sqlHis = new StringBuilder();
			sqlHis.append("INSERT INTO CT_RECIPEPARAMETERHISTORY ");
			sqlHis.append("   (MACHINENAME, RECIPENAME, RECIPEPARAMETERNAME, TIMEKEY, EVENTNAME, VALUE, ");
			sqlHis.append("    VALIDATIONTYPE, TARGET, LOWERLIMIT, UPPERLIMIT, RESULT, EVENTUSER, CHECKFLAG) ");
			sqlHis.append(" VALUES ");
			sqlHis.append("   ( ?,?,?,?,?,?,?,?,?,?,?,?,? )");

			GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlHis.toString(), updateHisList);
		}
		return doc;
	}
}
