package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.WOPatternFilmInfo;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;

public class WOPatternFilmInfoService extends CTORMService<WOPatternFilmInfo> {
	public static Log logger = LogFactory.getLog(WOPatternFilmInfoService.class);

	private final String historyEntity = "WOPatternFilmInfoHistory";

	public List<WOPatternFilmInfo> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<WOPatternFilmInfo> result = super.select(condition, bindSet, WOPatternFilmInfo.class);

		return result;
	}

	public WOPatternFilmInfo selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(WOPatternFilmInfo.class, isLock, keySet);
	}

	public WOPatternFilmInfo create(EventInfo eventInfo, WOPatternFilmInfo dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, WOPatternFilmInfo dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public WOPatternFilmInfo modify(EventInfo eventInfo, WOPatternFilmInfo dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public WOPatternFilmInfo getWOPatternFilmInfoData(String productSpecName,String productSpecVersion,String processFlowName,String processFlowVersion,String processOperationName,String processOperationVersion,String machineName,String  productRequestName)
	{
		Object[] keySet = new Object[] {productSpecName, productSpecVersion,processFlowName,processFlowVersion,processOperationName,processOperationVersion,machineName, productRequestName};
		WOPatternFilmInfo dataInfo = new WOPatternFilmInfo();

		try
		{
			dataInfo = this.selectByKey(false, keySet);
		}
		catch (Exception e)
		{
			dataInfo = null;
		}

		return dataInfo;
	}

	public List<WOPatternFilmInfo> getWOPatternFilmInfoData(String materialSpecName, String materialSpecVersion)
	{
		String condition = " MATERIALSPECNAME = ? AND MATERIALSPECVERSION = ? ";
		Object[] bindSet = new Object[] { materialSpecName, materialSpecVersion };
		List<WOPatternFilmInfo> dataInfo = new ArrayList<WOPatternFilmInfo>();

		try
		{
			dataInfo = this.select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfo = null;
		}

		return dataInfo;
	}

	public List<WOPatternFilmInfo> getWOPatternFilmInfoData(Lot lotData,String MachineName)
	{
		String condition = " PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND PRODUCTREQUESTNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND MACHINENAME=?";
		Object[] bindSet = new Object[] { lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProductRequestName(), lotData.getProcessFlowName(),
				lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),MachineName };
		List<WOPatternFilmInfo> dataInfo = new ArrayList<WOPatternFilmInfo>();

		try
		{
			dataInfo = this.select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfo = null;
		}

		return dataInfo;
	}
}
