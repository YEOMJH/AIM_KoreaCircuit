package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.OffsetAlignInfo;
import kr.co.aim.messolution.extended.object.management.data.PhotoOffsetResult;
import kr.co.aim.messolution.extended.object.management.data.ReserveRepairProduct;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;

public class OffsetAlignInfoService extends CTORMService<OffsetAlignInfo> {
	
	public static Log logger = LogFactory.getLog(OffsetAlignInfoService.class);
	
	public List<OffsetAlignInfo> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<OffsetAlignInfo> result = super.select(condition, bindSet, OffsetAlignInfo.class);
		
		return result;
	}
	
	public OffsetAlignInfo selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(OffsetAlignInfo.class, isLock, keySet);
	}
	
	public OffsetAlignInfo create(EventInfo eventInfo, OffsetAlignInfo dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, OffsetAlignInfo dataInfo)
		throws CustomException
	{
		super.delete(dataInfo);
	}
	
	public OffsetAlignInfo modify(EventInfo eventInfo, OffsetAlignInfo dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
