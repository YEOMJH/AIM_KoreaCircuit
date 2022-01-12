package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.TFEDownProduct;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class TFEDownProductService extends CTORMService<TFEDownProduct> {
	
	public static Log logger = LogFactory.getLog(TFEDownProductService.class);
	
	public List<TFEDownProduct> select(String condition, Object[] bindSet) throws CustomException
	{
		List<TFEDownProduct> result = super.select(condition, bindSet, TFEDownProduct.class);

		return result;
	}

	public TFEDownProduct selectByKey(boolean isLock, Object[] keySet) throws CustomException
	{
		return super.selectByKey(TFEDownProduct.class, isLock, keySet);
	}

	public TFEDownProduct create(EventInfo eventInfo, TFEDownProduct dataInfo) throws CustomException
	{
		super.insert(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, TFEDownProduct dataInfo) throws CustomException
	{
		super.delete(dataInfo);
	}

	public TFEDownProduct modify(EventInfo eventInfo, TFEDownProduct dataInfo) throws CustomException
	{
		super.update(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public List<TFEDownProduct> tranform (List resultList)
	{
		if (resultList == null || resultList.size() == 0)
		{
			return null;
		}
		Object result = super.ormExecute(CTORMUtil.createDataInfo(TFEDownProduct.class), resultList);

		if ((result instanceof List))
		{
			return (List) result;
		}

		List<TFEDownProduct> resultSet = new ArrayList();
		resultSet.add((TFEDownProduct) result);
		return resultSet;
	}
}
