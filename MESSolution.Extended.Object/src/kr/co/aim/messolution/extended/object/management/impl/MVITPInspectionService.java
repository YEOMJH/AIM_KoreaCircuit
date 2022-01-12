package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MVITPInspection;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;

public class MVITPInspectionService extends CTORMService<MVITPInspection> {

	public static Log logger = LogFactory.getLog(MVITPInspection.class);

	public List<MVITPInspection> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<MVITPInspection> result = super.select(condition, bindSet, MVITPInspection.class);

		return result;
	}

	public MVITPInspection selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MVITPInspection.class, isLock, keySet);
	}

	public MVITPInspection create(EventInfo eventInfo, MVITPInspection dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void create(EventInfo eventInfo, List<MVITPInspection> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfoList);
	}

	public void remove(EventInfo eventInfo, MVITPInspection dataInfo) throws greenFrameDBErrorSignal
	{
		super.delete(dataInfo);
	}

	public MVITPInspection modify(EventInfo eventInfo, MVITPInspection dataInfo)
	{
		super.update(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void modify(EventInfo eventInfo, List<MVITPInspection> dataInfoList)
	{
		super.update(dataInfoList);
	}

	public List<MVITPInspection> getMVITPInspectionDataList(Long seq, String panelName)
	{
		String condition = " SEQ = ? AND PANELNAME = ? ";
		Object[] bindSet = new Object[] { seq, panelName };

		List<MVITPInspection> dataInfoList = new ArrayList<MVITPInspection>();

		try
		{
			dataInfoList = this.select(condition, bindSet);
		}
		catch (Exception e)
		{
			logger.info("MVITPInspection Data not exist");
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public void deleteMVITPInspectionDataList(EventInfo eventInfo, Long seq, String panelName)
	{
		List<MVITPInspection> dataInfoList = this.getMVITPInspectionDataList(seq, panelName);

		if (dataInfoList != null)
		{
			for (MVITPInspection dataInfo : dataInfoList)
				this.remove(eventInfo, dataInfo);
		}
	}

	public void createMVITPInspectionData(EventInfo eventInfo, long seq, String lotName, Element tpInspectionData)
	{
		logger.debug("seq : " + seq);
		logger.debug("TESTNAME : " + tpInspectionData.getChildText("TESTNAME"));
		logger.debug("TESTRESULT : " + tpInspectionData.getChildText("TESTRESULT"));

		MVITPInspection dataInfo = new MVITPInspection();
		dataInfo.setSeq(seq);
		dataInfo.setPanelName(lotName);
		dataInfo.setTestName(tpInspectionData.getChildText("TESTNAME"));
		dataInfo.setTestResult(tpInspectionData.getChildText("TESTRESULT"));
		dataInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());

		this.create(eventInfo, dataInfo);
	}
}
