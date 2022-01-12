package kr.co.aim.messolution.consumable.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class CheckFilm extends SyncHandler {
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String filmBoxName = SMessageUtil.getBodyItemValue(doc, "FILMBOXNAME", true);

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(filmBoxName);

		if (durableData.getLotQuantity() < 1)
			throw new CustomException("FILM-0001", filmBoxName);

		if (StringUtils.equals(durableData.getDurableCleanState(), GenericServiceProxy.getConstantMap().Dur_Dirty))
			throw new CustomException("FILM-0002", filmBoxName, GenericServiceProxy.getConstantMap().Dur_Dirty);

		Map<String, Object> consumableSpecData = checkFilmSpec(filmBoxName);

		String consumableSpecName = ConvertUtil.getMapValueByName(consumableSpecData, "CONSUMABLESPECNAME");
		String consumableSpecVersion = ConvertUtil.getMapValueByName(consumableSpecData, "CONSUMABLESPECVERSION");

		checkBomData(factoryName, consumableSpecName, consumableSpecVersion);

		return doc;
	}

	private Map<String, Object> checkFilmSpec(String filmBoxName) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT CONSUMABLESPECNAME, CONSUMABLESPECVERSION ");
		sql.append("  FROM CONSUMABLE ");
		sql.append(" WHERE CARRIERNAME = :CARRIERNAME ");
		sql.append("GROUP BY CONSUMABLESPECNAME, CONSUMABLESPECVERSION ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("CARRIERNAME", filmBoxName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (sqlResult.size() < 1)
		{
			throw new CustomException("FILM-0001", filmBoxName);
		}

		if (sqlResult.size() != 1)
		{
			throw new CustomException("FILM-0003", filmBoxName);
		}

		return sqlResult.get(0);
	}

	private void checkBomData(String factoryName, String consumableSpecName, String consumableSpecVersion) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT T.FACTORYNAME, ");
		sql.append("       T.PRODUCTSPECNAME, ");
		sql.append("       T.PRODUCTSPECVERSION, ");
		sql.append("       P.MATERIALFACTORYNAME, ");
		sql.append("       P.MATERIALSPECNAME, ");
		sql.append("       P.MATERIALSPECVERSION, ");
		sql.append("       P.MATERIALTYPE, ");
		sql.append("       P.USEFLAG ");
		sql.append("  FROM TPPOLICY T, POSBOM P ");
		sql.append(" WHERE T.CONDITIONID = P.CONDITIONID ");
		sql.append("   AND T.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND P.MATERIALFACTORYNAME = :MATERIALFACTORYNAME ");
		sql.append("   AND P.MATERIALSPECNAME = :CONSUMABLESPECNAME ");
		sql.append("   AND P.MATERIALSPECVERSION = :CONSUMABLESPECVERSION ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("FACTORYNAME", factoryName);
		args.put("MATERIALFACTORYNAME", factoryName);
		args.put("CONSUMABLESPECNAME", consumableSpecName);
		args.put("CONSUMABLESPECVERSION", consumableSpecVersion);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (sqlResult.size() < 1)
		{
			throw new CustomException("FILM-0004", consumableSpecName);
		}
	}
}
