package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MVIUserDefectHist;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MVIUserDefectHistService extends CTORMService<MVIUserDefectHist> {
	public static Log logger = LogFactory.getLog(MVIUserDefectHistService.class);

	public List<MVIUserDefectHist> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<MVIUserDefectHist> result = super.select(condition, bindSet, MVIUserDefectHist.class);

		return result;
	}

	public MVIUserDefectHist selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MVIUserDefectHist.class, isLock, keySet);
	}

	public MVIUserDefectHist create(EventInfo eventInfo, MVIUserDefectHist dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, MVIUserDefectHist dataInfo) throws greenFrameDBErrorSignal
	{
		super.delete(dataInfo);
	}

	public MVIUserDefectHist modify(EventInfo eventInfo, MVIUserDefectHist dataInfo)
	{
		super.update(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public List<MVIUserDefectHist> getMVIUserDefectHistData(String userName)
	{
		String condition = " EVENTUSER = ? ";
		Object[] bindSet = new Object[] { userName };

		List<MVIUserDefectHist> dataInfoList = new ArrayList<MVIUserDefectHist>();

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

	public void deleteMVIUserDefectHistData(EventInfo eventInfo, String eventUser)
	{
		List<MVIUserDefectHist> dataInfoList = this.getMVIUserDefectHistData(eventUser);

		if (dataInfoList != null)
		{
			for (MVIUserDefectHist dataInfo : dataInfoList)
				this.remove(eventInfo, dataInfo);
		}
	}

	public void insertMVIUserDefectHistData(EventInfo eventInfo, Lot lotData, String machineName, ProductRequest productRequestData, String judge, String defectCode)
	{
		MVIUserDefectHist dataInfo = new MVIUserDefectHist();
		dataInfo.setPanelName(lotData.getKey().getLotName());
		dataInfo.setEventUser(eventInfo.getEventUser());
		dataInfo.setTimekey(eventInfo.getEventTimeKey());
		dataInfo.setMachineName(machineName);
		dataInfo.setProductSpecName(lotData.getProductSpecName());
		dataInfo.setProductSpecVersion(lotData.getProductSpecVersion());
		dataInfo.setDefectCode(defectCode);
		dataInfo.setPanelJudge(judge);
		dataInfo.setSubProductionType(productRequestData.getUdfs().get("SUBPRODUCTIONTYPE"));
		dataInfo.setProcessOperationName(lotData.getProcessOperationName());
		
		this.create(eventInfo, dataInfo);
	}
}
