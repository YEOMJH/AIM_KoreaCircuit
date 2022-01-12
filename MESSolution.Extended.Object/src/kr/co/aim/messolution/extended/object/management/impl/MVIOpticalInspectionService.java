package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MVIOpticalInspection;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;

public class MVIOpticalInspectionService extends CTORMService<MVIOpticalInspection> {

	public static Log logger = LogFactory.getLog(MVIOpticalInspection.class);

	public List<MVIOpticalInspection> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<MVIOpticalInspection> result = super.select(condition, bindSet, MVIOpticalInspection.class);

		return result;
	}

	public MVIOpticalInspection selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MVIOpticalInspection.class, isLock, keySet);
	}

	public MVIOpticalInspection create(EventInfo eventInfo, MVIOpticalInspection dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void create(EventInfo eventInfo, List<MVIOpticalInspection> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfoList);
	}

	public void remove(EventInfo eventInfo, MVIOpticalInspection dataInfo) throws greenFrameDBErrorSignal
	{
		super.delete(dataInfo);
	}

	public MVIOpticalInspection modify(EventInfo eventInfo, MVIOpticalInspection dataInfo)
	{
		super.update(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void modify(EventInfo eventInfo, List<MVIOpticalInspection> dataInfoList)
	{
		super.update(dataInfoList);
	}

	public List<MVIOpticalInspection> getMVIOpticalInspectionDataList(Long seq, String panelName)
	{
		String condition = " SEQ = ? AND PANELNAME = ? ";
		Object[] bindSet = new Object[] { seq, panelName };

		List<MVIOpticalInspection> dataInfoList = new ArrayList<MVIOpticalInspection>();

		try
		{
			dataInfoList = this.select(condition, bindSet);
		}
		catch (Exception e)
		{
			logger.info("MVIOpticalInspection Data not exist");
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public void deleteMVIOpticalInspectionDataList(EventInfo eventInfo, long seq, String panelName)
	{
		List<MVIOpticalInspection> dataInfoList = this.getMVIOpticalInspectionDataList(seq, panelName);

		if (dataInfoList != null)
		{
			for (MVIOpticalInspection dataInfo : dataInfoList)
				this.remove(eventInfo, dataInfo);
		}
	}

	public MVIOpticalInspection createMVIOpticalInspectionData(EventInfo eventInfo, long seq, String panelName, Element defectData, String colorGamut)
	{
		MVIOpticalInspection dataInfo = new MVIOpticalInspection();
		dataInfo.setPanelName(panelName);
		dataInfo.setSeq(seq);
		dataInfo.setColor(defectData.getChildText("COLOR"));
		dataInfo.setStart_Time(TimeUtils.getTimestamp(defectData.getChildText("START_TIME")));
		dataInfo.setEnd_Time(TimeUtils.getTimestamp(defectData.getChildText("END_TIME")));
		dataInfo.setLuminance(defectData.getChildText("LUMINANCE"));
		dataInfo.setX(defectData.getChildText("X"));
		dataInfo.setY(defectData.getChildText("Y"));
		dataInfo.setI(defectData.getChildText("I"));
		dataInfo.setEfficiency(defectData.getChildText("EFFICIENCY"));
		dataInfo.setLuminance_Aft(defectData.getChildText("LUMINANCE_AFT"));
		dataInfo.setX_Aft(defectData.getChildText("X_AFT"));
		dataInfo.setY_Aft(defectData.getChildText("Y_AFT"));
		dataInfo.setI_Aft(defectData.getChildText("I_AFT"));
		dataInfo.setEfficiency_Aft(defectData.getChildText("EFFICIENCY_AFT"));
		dataInfo.setResult(defectData.getChildText("RESULT"));
		dataInfo.setColorGamut(colorGamut);
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

		return this.create(eventInfo, dataInfo);
	}
}
