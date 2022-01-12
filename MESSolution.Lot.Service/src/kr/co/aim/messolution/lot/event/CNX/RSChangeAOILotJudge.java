package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class RSChangeAOILotJudge extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);

		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);

		if (StringUtils.equals(lotData.getLotGrade(), "G") && machineName.equals("3ATV01") || machineName.equals("3CTV01") || machineName.equals("3TTV01"))
		{
			StringBuilder AOIsql = new StringBuilder();
			AOIsql.append("SELECT LOTJUDGE ");
			AOIsql.append("  FROM CT_AOILOT ");
			AOIsql.append(" WHERE TIMEKEY = (SELECT MAX (TIMEKEY) ");
			AOIsql.append("                    FROM CT_AOILOT ");
			AOIsql.append("                   WHERE LOTNAME = :LOTNAME) ");

			Map<String, Object> AOIargs = new HashMap<>();
			AOIargs.put("LOTNAME", lotName);

			List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(AOIsql.toString(), AOIargs);

			if (result != null && result.size() > 0 && result.get(0).toString().equals("{LOTJUDGE=N}"))
				MESLotServiceProxy.getLotServiceImpl().RSchangeAOILotJudge(lotName);
		}

		return doc;

	}
}
