package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ChangeRunControl;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ChangeRunControlService extends CTORMService<ChangeRunControl> {
	public static Log logger = LogFactory.getLog(ChangeRunControlService.class);

	private final String historyEntity = "ChangeRunControlHist";

	public List<ChangeRunControl> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<ChangeRunControl> result = super.select(condition, bindSet, ChangeRunControl.class);

		return result;
	}

	public ChangeRunControl selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ChangeRunControl.class, isLock, keySet);
	}

	public ChangeRunControl create(EventInfo eventInfo, ChangeRunControl dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, ChangeRunControl dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public ChangeRunControl modify(EventInfo eventInfo, ChangeRunControl dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public ChangeRunControl getChangeRunControl(String machineName, String processOperationName, String processOperationVersion)
	{
		ChangeRunControl dataInfo = new ChangeRunControl();

		try
		{
			dataInfo = this.selectByKey(false, new String[] { machineName, processOperationName, processOperationVersion });
		}
		catch (Exception e)
		{
			dataInfo = null;
			logger.info("ChangeRunControlData not exist");
		}

		return dataInfo;
	}

	public ChangeRunControl createChangeRunControl(EventInfo eventInfo, String machineName, String processOperationName, String processOperationVersion, String factoryName, String actionType,
			String startFlag, String useFlag, int useCount, int useCountLimit)
	{
		ChangeRunControl dataInfo = new ChangeRunControl();
		dataInfo.setMachineName(machineName);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(processOperationVersion);
		dataInfo.setFactoryName(factoryName);
		dataInfo.setActionType(actionType);
		dataInfo.setStartFlag(startFlag);
		dataInfo.setUseFlag(useFlag);
		dataInfo.setUseCount(useCount);
		dataInfo.setUseCountLimit(useCountLimit);
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

		dataInfo = this.create(eventInfo, dataInfo);

		return dataInfo;
	}

	public void deleteChangeRunControlData(EventInfo eventInfo, String machineName, String processOperationName, String processOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		ChangeRunControl dataInfo = this.selectByKey(false, new Object[] { machineName, processOperationName, processOperationVersion });
		this.remove(eventInfo, dataInfo);
	}

	public void increaseUseCount(EventInfo eventInfo, String machineName, String processOperationName, String processOperationVersion, boolean validation) throws greenFrameDBErrorSignal,
			CustomException
	{
		ChangeRunControl downLoadData = this.getChangeRunControl(machineName, processOperationName, processOperationVersion);

		if (downLoadData == null)
			return;

		String startFlag = downLoadData.getStartFlag();
		String useFlag = downLoadData.getUseFlag();

		logger.info("Data(" + machineName + ", " + processOperationName + ", " + processOperationVersion + ") / StartFlag : " + startFlag + " / UseFlag : " + useFlag);

		// Increase UseCount
		if (StringUtils.equals(startFlag, "Y") && StringUtils.equals(useFlag, "Y"))
		{
			if (validation && downLoadData.getUseCount().intValue() + 1 > downLoadData.getUseCountLimit().intValue())
			{
				logger.info("ChagneRunControl Data Over: " + machineName + ", " + processOperationName + ", " + processOperationVersion);
				//throw new CustomException("MACHINE-0040", machineName + ", " + processOperationName + ", " + processOperationVersion, "ChangeRunControl");
			}

			this.increaseUseCount(eventInfo, downLoadData);
		}

		// Check OtherChagneRunControl
		if (StringUtils.equals(startFlag, "N") && StringUtils.equals(useFlag, "Y"))
		{
			checkOtherChangeRunControl(eventInfo, downLoadData, machineName, processOperationName, processOperationVersion, validation);
		}
	}

	public void checkOtherChangeRunControl(EventInfo eventInfo, ChangeRunControl downLoadData, String machineName, String processOperationName, String processOperationVersion, boolean validation)
			throws CustomException
	{
		String condition = " MACHINENAME = ? AND (PROCESSOPERATIONNAME <> ? OR PROCESSOPERATIONVERSION <> ?) ";
		Object[] bindSet = new Object[] { machineName, processOperationName, processOperationVersion };

		List<ChangeRunControl> dataInfoList = this.select(condition, bindSet);
		
		if (dataInfoList != null && dataInfoList.isEmpty())
		{
			for (ChangeRunControl dataInfo : dataInfoList)
			{
				String startFlag = dataInfo.getStartFlag();
				String useFlag = dataInfo.getUseFlag();

				logger.info("Data(" + machineName + ", " + dataInfo.getProcessOperationName() + ", " + dataInfo.getProcessOperationVersion() + ") / StartFlag : " + startFlag + " / UseFlag : " + useFlag);

				if (StringUtils.equals(startFlag, "Y"))
				{
					if (validation && StringUtils.equals(useFlag, "Y") && dataInfo.getUseCount().intValue() < dataInfo.getUseCountLimit().intValue())
					{
						logger.info("ChagneRunControl Data Not Over: " + machineName + ", " + dataInfo.getProcessOperationName() + ", " + dataInfo.getProcessOperationVersion() + " Reset ChagneRunControl Data ");
						//throw new CustomException("MACHINE-0042", machineName + ", " + processOperationName + ", " + processOperationVersion, dataInfo.getUseCount(), dataInfo.getUseCountLimit());
					}
						
					// Reset Other ChagneRunControl Data
					resetUseCount(eventInfo, dataInfo);
				}
			}
		}
		// Start Down ChangeRunConstrol Data
		startUseCount(eventInfo, downLoadData);
	}

	public void startUseCount(EventInfo eventInfo, ChangeRunControl dataInfo)
	{
		logger.info("Start Data");

		dataInfo.setStartFlag("Y");
		dataInfo.setUseCount(1);
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

		this.modify(eventInfo, dataInfo);
	}

	public void resetUseCount(EventInfo eventInfo, ChangeRunControl dataInfo)
	{
		logger.info("Reset Data");

		dataInfo.setStartFlag("N");
		dataInfo.setUseCount(0);
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

		this.modify(eventInfo, dataInfo);
	}

	public void increaseUseCount(EventInfo eventInfo, ChangeRunControl dataInfo)
	{
		dataInfo.setUseCount(dataInfo.getUseCount().intValue() + 1);
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

		this.modify(eventInfo, dataInfo);
	}

	public void sendEmailByTrackOut(String machineName, Lot lotData)
	{
		try
		{
			ChangeRunControl dataInfo = ExtendedObjectProxy.getChangeRunControlService().selectByKey(false,
					new Object[] { machineName, lotData.getProcessOperationName(), lotData.getProcessOperationVersion() });

			if (dataInfo.getUseCount().intValue() == dataInfo.getUseCountLimit().intValue())
			{
				String[] mailList = CommonUtil.getEmailListByAlarmGroup("ChangeRunControlAlarm");

				if (mailList == null || mailList.length == 0)
					return;

				String message = "<pre>=======================AlarmInformation=======================</pre>";
				message += "<pre>==============================================================</pre>";
				message += "<pre>- RunControlAlarm			: " + "ChangeRunControlAlarm" + "</pre>";
				message += "<pre>- MachineName				: " + machineName + "</pre>";
				message += "<pre>- ProcessOperationName		: " + lotData.getProcessOperationName() + "</pre>";
				message += "<pre>- ProcessOperationVersion	: " + lotData.getProcessOperationVersion() + "</pre>";
				message += "<pre>- UseCount					: " + dataInfo.getUseCount() + "</pre>";
				message += "<pre>- UseCountLimit			: " + dataInfo.getUseCountLimit() + "</pre>";
				message += "<pre>==============================================================</pre>";

				try
				{
					GenericServiceProxy.getMailSerivce().postMail(mailList, this.getClass().getSimpleName(), message);
				}
				catch (Exception ex)
				{
					if (ex instanceof CustomException)
					{
						logger.info(((CustomException) ex).errorDef.getEng_errorMessage());
						// CommonUtil.sendSMSWhenPostMailFail(message);
					}
					else
					{
						throw new CustomException(ex.getCause());
					}
				}

			}
		}
		catch (Exception e)
		{
			logger.info("ChangeRunControlData not exist");
		}
	}
}
