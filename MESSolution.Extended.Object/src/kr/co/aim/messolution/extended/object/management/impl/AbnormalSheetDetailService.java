package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.AbnormalSheetDetail;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AbnormalSheetDetailService extends CTORMService<AbnormalSheetDetail> {
	
	public static Log logger = LogFactory.getLog(AbnormalSheetDetailService.class);
	
	private final String historyEntity = "AbnormalSheetDetailHistory";
	
	public List<AbnormalSheetDetail> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<AbnormalSheetDetail> result = super.select(condition, bindSet, AbnormalSheetDetail.class);
		
		return result;
	}
	
	public AbnormalSheetDetail selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(AbnormalSheetDetail.class, isLock, keySet);
	}
	
	public AbnormalSheetDetail create(EventInfo eventInfo, AbnormalSheetDetail dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, AbnormalSheetDetail dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public AbnormalSheetDetail modify(EventInfo eventInfo, AbnormalSheetDetail dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 
}
