package kr.co.aim.messolution.extended.object.management.impl;
import org.apache.commons.logging.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.LogFactory;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.RiskLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class RiskLotService extends CTORMService<RiskLot> {

	public static Log logger = LogFactory.getLog(RiskLot.class);

	private final String historyEntity = "RiskLotHistory";
	
	public List<RiskLot> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<RiskLot> result = super.select(condition, bindSet, RiskLot.class);

		return result;
	}

	public RiskLot selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(RiskLot.class, isLock, keySet);
	}
	
	public List<RiskLot> getDataInfoListByLotNameList(List<String> lotNameList) throws CustomException
	{
		logger.info("getDataInfoListByLotNameList: Input LotNameList size is " + lotNameList ==null?0:lotNameList.size());
		
		if(lotNameList ==null || lotNameList.size()==0) return null;
		
		String sql = " SELECT * FROM CT_RISKLOT WHERE 1=1 AND LOTNAME IN (:LOTNAMELIST) AND PICKFLAG = 'Y' ";
		
		Map<String,Object> bindMap = new HashMap<>();
		bindMap.put("LOTNAMELIST", lotNameList);
		
		List<Map<String,Object>> resultList = null;
		
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}
		
	    if(resultList == null || resultList.size()==0) return null;
	    
	    return this.transform(resultList);
	}
	
	public RiskLot create(EventInfo eventInfo, RiskLot dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void create(EventInfo eventInfo, List<RiskLot> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfoList);

		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}
	
	public void remove(EventInfo eventInfo, RiskLot dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public RiskLot modify(EventInfo eventInfo, RiskLot dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void modify(EventInfo eventInfo, List<RiskLot> dataInfoList)
	{
		super.update(dataInfoList);

		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}
	
	public List<RiskLot> transform (List resultList)
	{
		if (resultList==null || resultList.size() == 0)
		{
			return null;
		}
		
		Object result = super.ormExecute( CTORMUtil.createDataInfo(RiskLot.class), resultList);

		if ((result instanceof List))
		{
			return (List) result;
		}

		List<RiskLot> resultSet = new ArrayList();
		resultSet.add((RiskLot) result);
		return resultSet;
	}
}
