package kr.co.aim.messolution.extended.object.management.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.PhotoOffsetResult;
import kr.co.aim.messolution.extended.object.management.data.ReserveRepairProduct;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;

public class PhotoOffsetResultService extends CTORMService<PhotoOffsetResult> {
	
	public static Log logger = LogFactory.getLog(PhotoOffsetResultService.class);
	
	public List<PhotoOffsetResult> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<PhotoOffsetResult> result = super.select(condition, bindSet, PhotoOffsetResult.class);
		
		return result;
	}
	
	public PhotoOffsetResult selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(PhotoOffsetResult.class, isLock, keySet);
	}
	
	public PhotoOffsetResult create(EventInfo eventInfo, PhotoOffsetResult dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, PhotoOffsetResult dataInfo)
		throws CustomException
	{
		super.delete(dataInfo);
	}
	
	public PhotoOffsetResult modify(EventInfo eventInfo, PhotoOffsetResult dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void photoOffsetLimitCheck(String machineName, String offset) throws CustomException
	{
		PhotoOffsetResult offsetResult = null;
		try 
		{
			offsetResult = super.selectByKey(PhotoOffsetResult.class, false, new Object[] { machineName, offset });
		} 
		catch (Exception e) 
		{
			throw new CustomException("OFFSET-0002", offset);
		}

		if(offsetResult != null)
		{
			String sql = "SELECT ENUMNAME, ENUMVALUE FROM ENUMDEFVALUE  "
					+ " WHERE ENUMNAME = 'PhotoOffsetLimit' ";
			Map<String, Object> bindMap = new HashMap<String, Object>();

			List<Map<String, Object>> flaglist = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			
			String offsetLimit = flaglist.get(0).get("ENUMVALUE").toString();
			String lastUseTime = offsetResult.getLastUseTime().toString();
			SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String currentTime = transFormat.format(new Date());
			Date lastUseTimeDate = null;
			Date currentDate = null;
			try {
				lastUseTimeDate = transFormat.parse(lastUseTime);
				currentDate = transFormat.parse(currentTime);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			double gap = (double)(currentDate.getTime() - lastUseTimeDate.getTime()) / (double)(60 * 60 * 1000);
			
			if (gap >= Double.parseDouble(offsetLimit)) 
			{
				throw new CustomException("MASK-0101", offset);
			}
		}
	}
}
