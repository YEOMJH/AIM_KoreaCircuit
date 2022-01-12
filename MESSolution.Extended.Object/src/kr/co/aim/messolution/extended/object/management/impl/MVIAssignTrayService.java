package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MVIAssignTray;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class MVIAssignTrayService extends CTORMService<MVIAssignTray> {
	public static Log logger = LogFactory.getLog(MVIAssignTrayService.class);

	public List<MVIAssignTray> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<MVIAssignTray> result = super.select(condition, bindSet, MVIAssignTray.class);

		return result;
	}

	public MVIAssignTray selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MVIAssignTray.class, isLock, keySet);
	}

	public MVIAssignTray create(EventInfo eventInfo, MVIAssignTray dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, MVIAssignTray dataInfo) throws greenFrameDBErrorSignal
	{
		super.delete(dataInfo);
	}

	public MVIAssignTray modify(EventInfo eventInfo, MVIAssignTray dataInfo)
	{
		super.update(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public MVIAssignTray createMVIAssignTrayData(EventInfo eventInfo, String machineName, String judge, String trayName, String seq, String productSpecName, String productSpecVersion,
			String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String productRequestName)
	{
		MVIAssignTray dataInfo = new MVIAssignTray();
		dataInfo.setTimekey(eventInfo.getEventTimeKey());
		dataInfo.setMachineName(machineName);
		dataInfo.setJudge(judge);
		dataInfo.setTrayName(trayName);
		dataInfo.setSeq(seq);
		dataInfo.setProductSpecName(productSpecName);
		dataInfo.setProductSpecVersion(productSpecVersion);
		dataInfo.setProcessFlowName(processFlowName);
		dataInfo.setProcessFlowVersion(processFlowVersion);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(processOperationVersion);
		dataInfo.setProductRequestName(productRequestName);
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventComment(eventInfo.getEventComment());

		dataInfo = this.create(eventInfo, dataInfo);

		return dataInfo;
	}

	public List<MVIAssignTray> getMVIAssignTrayData(String machineName, String judge)
	{
		String condition = " MACHINENAME = ? AND JUDGE = ? ";
		Object[] bindSet = new Object[] { machineName, judge };

		List<MVIAssignTray> dataInfoList = new ArrayList<MVIAssignTray>();

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

	public List<MVIAssignTray> getMVIAssignTrayData(String machineName, String judge, String trayName)
	{
		String condition = " MACHINENAME = ? AND JUDGE = ? AND TRAYNAME = ? ";
		Object[] bindSet = new Object[] { machineName, judge, trayName };

		List<MVIAssignTray> dataInfoList = new ArrayList<MVIAssignTray>();

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

	public void deleteMVIAssignTrayData(EventInfo eventInfo, String machineName, String judge)
	{
		List<MVIAssignTray> dataInfoList = this.getMVIAssignTrayData(machineName, judge);

		if (dataInfoList != null)
		{
			for (MVIAssignTray dataInfo : dataInfoList)
			{
				this.remove(eventInfo, dataInfo);
			}
		}
	}

	public void deleteMVIAssignTrayData(EventInfo eventInfo, String machineName, String judge, String trayName)
	{
		List<MVIAssignTray> dataInfoList = this.getMVIAssignTrayData(machineName, judge, trayName);

		if (dataInfoList != null)
		{
			for (MVIAssignTray dataInfo : dataInfoList)
			{
				this.remove(eventInfo, dataInfo);
			}
		}
	}
}
