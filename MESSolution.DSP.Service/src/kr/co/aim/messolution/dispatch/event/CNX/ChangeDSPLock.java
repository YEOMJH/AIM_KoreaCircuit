package kr.co.aim.messolution.dispatch.event.CNX;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.collections.map.ListOrderedMap;
import org.jdom.Document;

public class ChangeDSPLock extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String dSPLock = SMessageUtil.getBodyItemValue(doc, "DSPLOCK", true);
		String eventUser = SMessageUtil.getBodyItemValue(doc, "EVENTUSER", true);

		try
		{
			StringBuffer sqla = new StringBuffer();
			sqla.append("SELECT MACHINENAME, PROCESSOPERATION, PRODUCTSPECTYPE, DSPLOCK ");
			sqla.append("  FROM EQPPRODUCTIONRULE ");
			sqla.append(" WHERE MACHINENAME = ? ");
			sqla.append("   AND RULETYPE = 'Chamber' ");
			
			Object[] bindArray = new Object[] { machineName };
			
			@SuppressWarnings("unchecked")
			List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqla.toString(), bindArray);

			for (ListOrderedMap row : sqlResult)
			{
				String operationname = CommonUtil.getValue(row, "PROCESSOPERATION");

				if (!StringUtil.equals((String) row.get("DSPLOCK"), dSPLock))
				{
					StringBuffer updateSql = new StringBuffer();
					updateSql.append("UPDATE EQPPRODUCTIONRULE ");
					updateSql.append("   SET DSPLOCK = :DSPFLAG ");
					updateSql.append(" WHERE MACHINENAME = :MACHINENAME ");
					updateSql.append("   AND PROCESSOPERATION = :PROCESSOPERATION ");
					updateSql.append("   AND RULETYPE = 'Chamber' ");

					Map<String, String> bindMap2 = new HashMap<String, String>();
					bindMap2.put("DSPFLAG", dSPLock);
					bindMap2.put("MACHINENAME", machineName);
					bindMap2.put("PROCESSOPERATION", operationname);

					GenericServiceProxy.getSqlMesTemplate().update(updateSql.toString(), bindMap2);

					Format format = new SimpleDateFormat("yyyyMMddHHmmssSSSSSS");
					String timekey = format.format(new Date());

					StringBuffer insertSql = new StringBuffer();
					insertSql.append("INSERT ");
					insertSql.append("  INTO EQPPRODUCTIONRULEHISTORY  ");
					insertSql.append("  (MACHINENAME, PRIORITY, PROCESSOPERATION, PRODUCTSPECTYPE, DSPLOCK, ");
					insertSql.append("   TIMEKEY, EVENTUSER) ");
					insertSql.append("VALUES  ");
					insertSql.append("  (:MACHINENAME, '', :PROCESSOPERATION, :PRODUCTSPECTYPE, :DSPFLAG, ");
					insertSql.append("   :TIMEKEY, :EVENTUSER) ");

					Map<String, String> bindMap3 = new HashMap<String, String>();
					bindMap3.put("MACHINENAME", machineName);
					bindMap3.put("PROCESSOPERATION", operationname);
					bindMap3.put("PRODUCTSPECTYPE", (String) row.get("PRODUCTSPECTYPE"));
					bindMap3.put("DSPFLAG", dSPLock);
					bindMap3.put("TIMEKEY", timekey);
					bindMap3.put("EVENTUSER", eventUser);

					GenericServiceProxy.getSqlMesTemplate().update(insertSql.toString(), bindMap3);
				}
			}
		}
		catch (Exception ex)
		{
			throw new CustomException("DSP-0001", "Can't Find DSP Rule");
		}
		return doc;
	}
}
