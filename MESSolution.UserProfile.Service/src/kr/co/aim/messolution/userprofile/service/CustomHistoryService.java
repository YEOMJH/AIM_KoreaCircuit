package kr.co.aim.messolution.userprofile.service;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.userprofile.info.HistoryInfo;
import kr.co.aim.greenframe.orm.info.DataInfo;
import kr.co.aim.greenframe.orm.info.KeyInfo;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.CommonHistoryService;

public interface CustomHistoryService <KEY extends KeyInfo,DATA extends DataInfo> extends CommonHistoryService<KEY,DATA>
{
	public boolean insertHistory(DataInfo dataInfo ,HistoryInfo histInfo,Class<DATA> historyClass ) throws DuplicateNameSignal,FrameworkErrorSignal, CustomException;
	
	public <DATA> DATA getDataInfo(DataInfo sourceData,Class<DATA> clazz,HistoryInfo histInfo) throws CustomException;
	
	public HistoryInfo makeHistoryInfo(String eventUser,String eventName,String eventComment);
}
