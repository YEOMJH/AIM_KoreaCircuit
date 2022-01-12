package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.FQCOpticalInspection;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;

public class FQCOpticalInspectionService extends CTORMService<FQCOpticalInspection> {

	public static Log logger = LogFactory.getLog(FQCOpticalInspection.class);

	public List<FQCOpticalInspection> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<FQCOpticalInspection> result = super.select(condition, bindSet, FQCOpticalInspection.class);

		return result;
	}

	public FQCOpticalInspection selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(FQCOpticalInspection.class, isLock, keySet);
	}

	public FQCOpticalInspection create(EventInfo eventInfo, FQCOpticalInspection dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void create(EventInfo eventInfo, List<FQCOpticalInspection> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfoList);
	}

	public void remove(EventInfo eventInfo, FQCOpticalInspection dataInfo) throws greenFrameDBErrorSignal
	{
		super.delete(dataInfo);
	}

	public FQCOpticalInspection modify(EventInfo eventInfo, FQCOpticalInspection dataInfo)
	{
		super.update(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void modify(EventInfo eventInfo, List<FQCOpticalInspection> dataInfoList)
	{
		super.update(dataInfoList);
	}

	public List<FQCOpticalInspection> getFQCOpticalInspectionData(String fqcLotName, long seq, String lotName)
	{
		String condition = " FQCLOTNAME = ? AND SEQ = ? AND PANELNAME = ? ";
		Object[] bindSet = new Object[] { fqcLotName, seq, lotName };

		List<FQCOpticalInspection> dataInfoList = new ArrayList<FQCOpticalInspection>();

		try
		{
			dataInfoList = this.select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public void deleteFQCOpticalInspectionData(String fqcLotName, long seq, String lotName)
	{
		List<FQCOpticalInspection> dataInfoList = this.getFQCOpticalInspectionData(fqcLotName, seq, lotName);

		if (dataInfoList != null)
			this.delete(dataInfoList);
	}

	public FQCOpticalInspection createFQCOpticalInspectionData(EventInfo eventInfo, String fqcLotName, long seq, String lotName, Element defectData) throws greenFrameDBErrorSignal, CustomException
	{
		FQCOpticalInspection dataInfo = new FQCOpticalInspection();
		dataInfo.setFqcLotName(fqcLotName);
		dataInfo.setSeq(seq);
		dataInfo.setPanelName(lotName);
		dataInfo.setStart_Time(defectData.getChildText("START_TIME"));
		dataInfo.setBrightness(defectData.getChildText("BRIGHTNESS"));
		dataInfo.setX(defectData.getChildText("X"));
		dataInfo.setY(defectData.getChildText("Y"));
		dataInfo.setStop_Time(defectData.getChildText("STOP_TIME"));
		dataInfo.setVr(defectData.getChildText("Vr"));
		dataInfo.setVg(defectData.getChildText("Vg"));
		dataInfo.setVb(defectData.getChildText("Vb"));
		dataInfo.setI(defectData.getChildText("I"));
		dataInfo.setX_Aft(defectData.getChildText("X_AFT"));
		dataInfo.setY_Aft(defectData.getChildText("Y_AFT"));
		dataInfo.setL_Aft(defectData.getChildText("L_AFT"));
		dataInfo.setVr_Aft(defectData.getChildText("Vr_AFT"));
		dataInfo.setVg_Aft(defectData.getChildText("Vg_AFT"));
		dataInfo.setVb_Aft(defectData.getChildText("Vb_AFT"));
		dataInfo.setI_Aft(defectData.getChildText("I_AFT"));
		dataInfo.setResult(defectData.getChildText("Result"));
		dataInfo.setColor(defectData.getChildText("COLOR"));
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

		dataInfo = ExtendedObjectProxy.getFQCOpticalInspectionService().create(eventInfo, dataInfo);

		return dataInfo;
	}
}
