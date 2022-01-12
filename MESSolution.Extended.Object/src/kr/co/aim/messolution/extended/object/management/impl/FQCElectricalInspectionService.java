package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.FQCElectricalInspection;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;

public class FQCElectricalInspectionService extends CTORMService<FQCElectricalInspection> {

	public static Log logger = LogFactory.getLog(FQCElectricalInspection.class);

	public List<FQCElectricalInspection> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<FQCElectricalInspection> result = super.select(condition, bindSet, FQCElectricalInspection.class);

		return result;
	}

	public FQCElectricalInspection selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(FQCElectricalInspection.class, isLock, keySet);
	}

	public FQCElectricalInspection create(EventInfo eventInfo, FQCElectricalInspection dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void create(EventInfo eventInfo, List<FQCElectricalInspection> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfoList);
	}

	public void remove(EventInfo eventInfo, FQCElectricalInspection dataInfo) throws greenFrameDBErrorSignal
	{
		super.delete(dataInfo);
	}

	public FQCElectricalInspection modify(EventInfo eventInfo, FQCElectricalInspection dataInfo)
	{
		super.update(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void modify(EventInfo eventInfo, List<FQCElectricalInspection> dataInfoList)
	{
		super.update(dataInfoList);
	}

	public List<FQCElectricalInspection> getFQCElectricalInspectionData(String fqcLotName, long seq, String lotName)
	{
		String condition = " FQCLOTNAME = ? AND SEQ = ? AND PANELNAME = ? ";
		Object[] bindSet = new Object[] { fqcLotName, seq, lotName };

		List<FQCElectricalInspection> dataInfoList = new ArrayList<FQCElectricalInspection>();

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

	public void deleteFQCElectricalInspectionData(String fqcLotName, long seq, String lotName)
	{
		List<FQCElectricalInspection> dataInfoList = this.getFQCElectricalInspectionData(fqcLotName, seq, lotName);

		if (dataInfoList != null)
			this.delete(dataInfoList);
	}

	public void createFQCElectricalInspectionData(EventInfo eventInfo, String fqcLotName, long seq, String lotName, Element defectData) throws greenFrameDBErrorSignal, CustomException
	{
		FQCElectricalInspection dataInfo = new FQCElectricalInspection();
		dataInfo.setFqcLotName(fqcLotName);
		dataInfo.setSeq(seq);
		dataInfo.setPanelName(lotName);
		dataInfo.setDefectCode(defectData.getChildText("DEFECT_CODE"));
		dataInfo.setDefectOrder(defectData.getChildText("DEFECT_ORDER"));
		dataInfo.setArea(defectData.getChildText("AREA"));
		dataInfo.setPatternName(defectData.getChildText("PATTERNNAME"));

		if (StringUtils.isNotEmpty(defectData.getChildText("JNDNAME")))
			dataInfo.setJNDName(defectData.getChildText("JNDNAME"));

		if (StringUtils.isNotEmpty(defectData.getChildText("QUANTITY")))
			dataInfo.setQuantity(Long.parseLong(defectData.getChildText("QUANTITY")));

		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

		ExtendedObjectProxy.getFQCElectricalInspectionService().create(eventInfo, dataInfo);
	}
}
