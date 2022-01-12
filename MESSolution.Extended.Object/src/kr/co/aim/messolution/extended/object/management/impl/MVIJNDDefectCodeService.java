package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MVIJNDDefectCode;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MVIJNDDefectCodeService extends CTORMService<MVIJNDDefectCode> {

	public static Log logger = LogFactory.getLog(MVIJNDDefectCode.class);

	private final String historyEntity = "MVIJNDDefectCodeHist";

	public List<MVIJNDDefectCode> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<MVIJNDDefectCode> result = super.select(condition, bindSet, MVIJNDDefectCode.class);

		return result;
	}

	public MVIJNDDefectCode selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MVIJNDDefectCode.class, isLock, keySet);
	}

	public MVIJNDDefectCode create(EventInfo eventInfo, MVIJNDDefectCode dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, MVIJNDDefectCode dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public MVIJNDDefectCode modify(EventInfo eventInfo, MVIJNDDefectCode dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public MVIJNDDefectCode getMVIJNDDefectCodeData(String productSpecName, String defectCode, String superDefectCode, String pattern, String jndName, String panelGrade)
	{
		MVIJNDDefectCode dataInfo = new MVIJNDDefectCode();

		try
		{
			dataInfo = this.selectByKey(false, new String[] { productSpecName, defectCode, superDefectCode, pattern, jndName, panelGrade });
		}
		catch (Exception e)
		{
			dataInfo = null;
		}

		return dataInfo;
	}

	public MVIJNDDefectCode CreateMVIJNDDefectCode(EventInfo eventInfo, String productSpecName, String defectCode, String defectCodeDescription, String superDefectCode,
			String superDefectCodeDescription, String pattern, String jndName, String sign, String panelGrade)
	{
		MVIJNDDefectCode dataInfo = new MVIJNDDefectCode();
		dataInfo.setProductSpecName(productSpecName);
		dataInfo.setDefectCode(defectCode);
		dataInfo.setDefectCodeDescription(defectCodeDescription);
		dataInfo.setSuperDefectCode(superDefectCode);
		dataInfo.setSuperDefectCodeDescription(superDefectCodeDescription);
		dataInfo.setPattern(pattern);
		dataInfo.setJndName(jndName);
		dataInfo.setSign(sign);
		dataInfo.setPanelGrade(panelGrade);
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
		dataInfo.setLastEventComment(eventInfo.getEventComment());

		this.create(eventInfo, dataInfo);

		return dataInfo;
	}

	public MVIJNDDefectCode ModifySign(EventInfo eventInfo, String productSpecName, String defectCode, String superDefectCode, String pattern, String jndName, String panelGrade, String sign)
	{
		MVIJNDDefectCode dataInfo = this.getMVIJNDDefectCodeData(productSpecName, defectCode, superDefectCode, pattern, jndName, panelGrade);

		if (dataInfo != null)
		{
			dataInfo.setSign(sign);
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
			dataInfo.setLastEventComment(eventInfo.getEventComment());

			this.modify(eventInfo, dataInfo);
		}

		return dataInfo;
	}

	public void deleteMVIJNDDefectCode(EventInfo eventInfo, String productSpecName, String defectCode, String superDefectCode, String pattern, String jndName, String panelGrade)
	{
		MVIJNDDefectCode dataInfo = this.getMVIJNDDefectCodeData(productSpecName, defectCode, superDefectCode, pattern, jndName, panelGrade);
		
		if(dataInfo!=null)
			this.remove(eventInfo, dataInfo);
	}

}
