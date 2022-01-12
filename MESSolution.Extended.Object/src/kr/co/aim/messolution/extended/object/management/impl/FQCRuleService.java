package kr.co.aim.messolution.extended.object.management.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.FQCRule;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FQCRuleService extends CTORMService<FQCRule> {

	public static Log logger = LogFactory.getLog(FQCRule.class);

	private final String historyEntity = "FQCRuleHistory";

	public List<FQCRule> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<FQCRule> result = super.select(condition, bindSet, FQCRule.class);

		return result;
	}

	public FQCRule selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(FQCRule.class, isLock, keySet);
	}

	public FQCRule create(EventInfo eventInfo, FQCRule dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, FQCRule dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public FQCRule modify(EventInfo eventInfo, FQCRule dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public String getFQCRuleSeq()
	{
		String sql = " SELECT NVL(MAX(SEQ) , 0) + 1 AS SEQ FROM CT_FQCRULE ";
		String seq = "";
		Map<String, String> args = new HashMap<String, String>();

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

		if (result.size() > 0)
			seq = ConvertUtil.getMapValueByName(result.get(0), "SEQ");

		return seq;
	}

	public FQCRule getFQCRuleData(Long seq) throws greenFrameDBErrorSignal, NumberFormatException, CustomException
	{

		FQCRule fqcRule = new FQCRule();

		try
		{
			fqcRule = this.selectByKey(false, new Object[] { seq });
		}
		catch (Exception e)
		{
			fqcRule = null;
		}

		return fqcRule;
	}

	public FQCRule getFQCRuleData(String sampleRule) throws CustomException
	{
		FQCRule dataInfo = new FQCRule();

		try
		{
			dataInfo = this.selectByKey(false, new Object[] { Long.parseLong(sampleRule) });
		}
		catch (greenFrameDBErrorSignal ex)
		{
			throw new CustomException("LOT-0320", sampleRule);
		}

		return dataInfo;
	}
}
