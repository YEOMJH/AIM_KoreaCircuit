package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.FQCInspection;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class FQCInspectionService extends CTORMService<FQCInspection> {
	public static Log logger = LogFactory.getLog(FQCInspectionService.class);

	public List<FQCInspection> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<FQCInspection> result = super.select(condition, bindSet, FQCInspection.class);

		return result;
	}

	public FQCInspection selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(FQCInspection.class, isLock, keySet);
	}

	public FQCInspection create(EventInfo eventInfo, FQCInspection dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, FQCInspection dataInfo) throws greenFrameDBErrorSignal
	{
		super.delete(dataInfo);
	}

	public FQCInspection modify(EventInfo eventInfo, FQCInspection dataInfo)
	{
		super.update(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public FQCInspection getFQCInspectionData(long seq, String panelName, String defectCode)
	{
		FQCInspection dataInfo = new FQCInspection();

		try
		{
			dataInfo = this.selectByKey(false, new Object[] { seq, panelName, defectCode });
		}
		catch (Exception e)
		{
			logger.info("FQCLotData is not exist ( " + seq + ", " + panelName + ", " + defectCode + " )");
			dataInfo = null;
		}

		return dataInfo;
	}

	public FQCInspection createFQCInspection(EventInfo eventInfo, long seq, String panelName, String defectCode, String defectOrder, String area, int quantity)
	{
		FQCInspection dataInfo = new FQCInspection();
		dataInfo.setSeq(seq);
		dataInfo.setPanelName(panelName);
		dataInfo.setDefectCode(defectCode);
		dataInfo.setDefectOrder(defectOrder);
		dataInfo.setArea(area);
		dataInfo.setQuantity(quantity);
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

		dataInfo = this.create(eventInfo, dataInfo);

		return dataInfo;
	}

	public void deleteFQCInspection(EventInfo eventInfo, long seq, String panelName, String defectCode)
	{
		FQCInspection dataInfo = this.getFQCInspectionData(seq, panelName, defectCode);

		if (dataInfo != null)
			this.remove(eventInfo, dataInfo);
	}

}
