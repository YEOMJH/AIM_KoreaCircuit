package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.Organic;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.orm.SQLLogUtil;
import kr.co.aim.greenframe.orm.info.access.UdfAccessor;
import kr.co.aim.greentrack.generic.GenericServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class OrganicService extends CTORMService<Organic> {
	
	public static Log logger = LogFactory.getLog(OrganicService.class);
	
	private final String historyEntity = "";
	
	public List<Organic> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<Organic> result = super.select(condition, bindSet, Organic.class);
		
		return result;
	}
	
	public Organic selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		//return super.selectByKey(Organic.class, isLock, keySet);
		Object dataInfo = CTORMUtil.createDataInfo(Organic.class);

		String tableName = CTORMUtil.getTableNameByClassName(Organic.class);

		String sql = CTORMUtil.getSql(Organic.class, tableName);
		sql = CTORMUtil.getKeySql(sql, dataInfo);

		// suffix for lock
		if (isLock)
			sql = new StringBuffer(sql).append(" FOR UPDATE").toString();

		String param = CommonUtil.toStringFromCollection(keySet);

		if (!CTORMUtil.validateKeyParam(dataInfo, keySet).isEmpty())
			throw new greenFrameDBErrorSignal(ErrorSignal.NullPointKeySignal, param, SQLLogUtil.getLogFormatSqlStatement(sql, param, CTORMUtil.getLogger()));

		// generate bind parameter by sequence
		// query with just one
		List resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, keySet);

		// case not found
		/*if (resultList.size() == 0)
		{
			throw new greenFrameDBErrorSignal(ErrorSignal.NotFoundSignal, param, SQLLogUtil.getLogFormatSqlStatement(sql, param, CTORMUtil.getLogger()));
		}*/

		// refine result data
		Object result = ormExecute(CTORMUtil.createDataInfo(Organic.class), resultList);

		// return
		return (Organic) result;
		/*if (result instanceof UdfAccessor)
		{
			return (Organic) result;
		}
		else
		{
			throw new greenFrameDBErrorSignal(ErrorSignal.CouldNotMatchData, param, SQLLogUtil.getLogFormatSqlStatement(sql, param, CTORMUtil.getLogger()), keySet.toString());
		}*/
	}
	
	public boolean create(EventInfo eventInfo, Organic dataInfo)
		throws CustomException
	{
		
		super.insert(dataInfo);
				
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return true;
	}
	
	public void remove(EventInfo eventInfo, Organic dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public Organic modify(EventInfo eventInfo, Organic dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
