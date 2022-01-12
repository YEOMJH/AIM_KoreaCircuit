package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MVIElectricalInspection;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;

public class MVIElectricalInspectionService extends CTORMService<MVIElectricalInspection> {

	public static Log logger = LogFactory.getLog(MVIElectricalInspection.class);

	public List<MVIElectricalInspection> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<MVIElectricalInspection> result = super.select(condition, bindSet, MVIElectricalInspection.class);

		return result;
	}

	public MVIElectricalInspection selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MVIElectricalInspection.class, isLock, keySet);
	}

	public MVIElectricalInspection create(EventInfo eventInfo, MVIElectricalInspection dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void create(EventInfo eventInfo, List<MVIElectricalInspection> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfoList);
	}

	public void remove(EventInfo eventInfo, MVIElectricalInspection dataInfo) throws greenFrameDBErrorSignal
	{
		super.delete(dataInfo);
	}

	public MVIElectricalInspection modify(EventInfo eventInfo, MVIElectricalInspection dataInfo)
	{
		super.update(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void modify(EventInfo eventInfo, List<MVIElectricalInspection> dataInfoList)
	{
		super.update(dataInfoList);
	}

	public List<MVIElectricalInspection> getMVIElectricalInspectionDataList(Long seq, String panelName)
	{
		String condition = " SEQ = ? AND PANELNAME = ? ";
		Object[] bindSet = new Object[] { seq, panelName };

		List<MVIElectricalInspection> dataInfoList = new ArrayList<MVIElectricalInspection>();

		try
		{
			dataInfoList = this.select(condition, bindSet);
		}
		catch (Exception e)
		{
			logger.info("MVIElectricalInspection Data not exist");
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public void deleteMVIElectricalInspectionDataList(EventInfo eventInfo, Long seq, String panelName)
	{
		List<MVIElectricalInspection> dataInfoList = this.getMVIElectricalInspectionDataList(seq, panelName);

		if (dataInfoList != null)
		{
			for (MVIElectricalInspection dataInfo : dataInfoList)
				this.remove(eventInfo, dataInfo);
		}
	}

	public MVIElectricalInspection createMVIElectricalInspectionData(EventInfo eventInfo, long seq, String lotName, Element defectData)
	{
		logger.debug("seq : " + seq);
		logger.debug("defectCode : " + defectData.getChildText("DEFECT_CODE"));
		logger.debug("defectOrder : " + defectData.getChildText("DEFECT_ORDER"));
		logger.debug("area" + defectData.getChildText("AREA"));
		logger.debug("quantity" + defectData.getChildText("QUANTITY"));

		MVIElectricalInspection dataInfo = new MVIElectricalInspection();
		dataInfo.setSeq(seq);
		dataInfo.setPanelName(lotName);
		dataInfo.setDefectCode(defectData.getChildText("DEFECT_CODE"));
		dataInfo.setDefectOrder(defectData.getChildText("DEFECT_ORDER"));
		dataInfo.setArea(defectData.getChildText("AREA"));
		dataInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());

		if (StringUtils.isNotEmpty(defectData.getChildText("PATTERNNAME")))
			dataInfo.setPatternName(defectData.getChildText("PATTERNNAME"));

		if (StringUtils.isNotEmpty(defectData.getChildText("QUANTITY")))
			dataInfo.setQuantity(Long.parseLong(defectData.getChildText("QUANTITY")));

		if (StringUtils.isNotEmpty(defectData.getChildText("JNDNAME")))
			dataInfo.setJndName(defectData.getChildText("JNDNAME"));

		return this.create(eventInfo, dataInfo);
	}
}
