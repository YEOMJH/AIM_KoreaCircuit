package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.DSPRunControl;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DSPRunControlService extends CTORMService<DSPRunControl> {
	public static Log logger = LogFactory.getLog(DSPRunControlService.class);

	private final String historyEntity = "DSPRunControlHist";

	public List<DSPRunControl> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<DSPRunControl> result = super.select(condition, bindSet, DSPRunControl.class);

		return result;
	}

	public DSPRunControl selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(DSPRunControl.class, isLock, keySet);
	}

	public DSPRunControl create(EventInfo eventInfo, DSPRunControl dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, DSPRunControl dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public DSPRunControl modify(EventInfo eventInfo, DSPRunControl dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public DSPRunControl createDSPRunControl(EventInfo eventInfo, String machineName, String processOperationName, String processOperationVersion, String recipeName, String factoryName,
			String actionType, String useFlag, int useCount, int useCountLimit)
	{
		DSPRunControl dataInfo = new DSPRunControl();
		dataInfo.setMachineName(machineName);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(processOperationVersion);
		dataInfo.setRecipeName(recipeName);
		dataInfo.setFactoryName(factoryName);
		dataInfo.setActionType(actionType);
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

	public DSPRunControl updateUseCount(EventInfo eventInfo, String machineName, String processOperationName, String processOperationVersion, String useFlag, int useCount, int useCountLimit)
			throws greenFrameDBErrorSignal, CustomException
	{
		DSPRunControl dataInfo = ExtendedObjectProxy.getDSPRunControlService().selectByKey(false, new Object[] { machineName, processOperationName, processOperationVersion });

		if (!StringUtils.isEmpty(useFlag))
			dataInfo.setUseFlag(useFlag);

		dataInfo.setUseCount(useCount);
		dataInfo.setUseCountLimit(useCountLimit);
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

		dataInfo = ExtendedObjectProxy.getDSPRunControlService().modify(eventInfo, dataInfo);

		return dataInfo;
	}

	public void deleteDSPRunControlData(EventInfo eventInfo, String machineName, String processOperationName, String processOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		DSPRunControl dataInfo = ExtendedObjectProxy.getDSPRunControlService().selectByKey(false, new Object[] { machineName, processOperationName, processOperationVersion });
		this.remove(eventInfo, dataInfo);
	}

	public void increaseUseCount(EventInfo eventInfo, String machineName, String processOperationName, String processOperationVersion, boolean validation, int actualProductQty) throws greenFrameDBErrorSignal,
			CustomException
	{
		// MantisID : 0000348
		// 当UseCount=UseCountLimint后，新的Lot 可以TrackIn Sucess，没有报错
		DSPRunControl dataInfo = null;
		
		try
		{
			dataInfo = ExtendedObjectProxy.getDSPRunControlService().selectByKey(false, new Object[] { machineName, processOperationName, processOperationVersion });			
		}
		catch (Exception ex)
		{
			logger.info("DSPRunControlData not exist");
			return ;
		}
		
		if (StringUtils.equals(dataInfo.getUseFlag(), "Y"))
		{
			if (!dataInfo.getActionType().equals("ByProduct") && validation && dataInfo.getUseCount().intValue() + 1 > dataInfo.getUseCountLimit().intValue())
				throw new CustomException("MACHINE-0040", machineName + ", " + processOperationName + ", " + processOperationVersion, "DSPRunControl");
			else if (dataInfo.getActionType().equals("ByProduct") && validation && dataInfo.getUseCount().intValue()/* + actualProductQty*/ > dataInfo.getUseCountLimit().intValue())
				throw new CustomException("MACHINE-0040", machineName + ", " + processOperationName + ", " + processOperationVersion, "DSPRunControl");

			if (dataInfo.getActionType().equals("ByProduct")) 
			{
				dataInfo.setUseCount(dataInfo.getUseCount().intValue() + actualProductQty);
			}
			else 
			{
				dataInfo.setUseCount(dataInfo.getUseCount().intValue() + 1);
			}
			
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

			this.modify(eventInfo, dataInfo);
		}
		else
		{
			logger.info("MachineName : " + machineName + ", OperationName + " + processOperationName + ", OperationVersion" + processOperationVersion + "UseFlag : N");
		}
	}

	public void decreaseUseCount(EventInfo eventInfo, String machineName, String processOperationName, String processOperationVersion, int actualProductQty) throws greenFrameDBErrorSignal, CustomException
	{
		try
		{
			DSPRunControl dataInfo = ExtendedObjectProxy.getDSPRunControlService().selectByKey(false, new Object[] { machineName, processOperationName, processOperationVersion });

			if (StringUtils.equals(dataInfo.getUseFlag(), "Y"))
			{
				// MantisID : 0000348
				// CancelTrackIn，UseCount-1，不能将UseCount置为负数。最小为0，为0后UseCount不再减1
				int useCount = 0;
				
				if (dataInfo.getActionType().equals("ByProduct"))
					useCount = dataInfo.getUseCount().intValue() - actualProductQty;
				else
					useCount = dataInfo.getUseCount().intValue() - 1;
				
				if (useCount < 0)
				{
					useCount = 0;
				}
				
				dataInfo.setUseCount(useCount);
				dataInfo.setLastEventName(eventInfo.getEventName());
				dataInfo.setLastEventUser(eventInfo.getEventUser());
				dataInfo.setLastEventTime(eventInfo.getEventTime());
				dataInfo.setLastEventComment(eventInfo.getEventComment());
				dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

				this.modify(eventInfo, dataInfo);
			}
			else
			{
				logger.info("MachineName : " + machineName + ", OperationName + " + processOperationName + ", OperationVersion" + processOperationVersion + "UseFlag : N");
			}
		}
		catch (Exception e)
		{
			logger.info("DSPRunControlData not exist");
		}
	}
     
	public void resetCount(EventInfo eventInfo, String machineName, Lot lotData, String recipeName)
	{
		try
		{
			String condition = " MACHINENAME = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND RECIPENAME = ? ";
			Object[] bindSet = new Object[] { machineName, lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), recipeName };

			List<DSPRunControl> dataInfoList = this.select(condition, bindSet);

			for (DSPRunControl dataInfo : dataInfoList)
			{
				if (dataInfo.getUseCount().intValue() != 0)
				{
					dataInfo.setUseCount(0);
					dataInfo.setLastEventName(eventInfo.getEventName());
					dataInfo.setLastEventUser(eventInfo.getEventUser());
					dataInfo.setLastEventTime(eventInfo.getEventTime());
					dataInfo.setLastEventComment(eventInfo.getEventComment());
					dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

					this.modify(eventInfo, dataInfo);
				}
			}
		}
		catch (Exception e)
		{
			logger.info("DSPRunControlData not exist");
		}
	}
	//DSP RunControl
	public void resetCount(EventInfo eventInfo, String machineName, String recipeName)
	{
		try
		{
			String condition = " MACHINENAME = ? AND RECIPENAME = ? ";
			Object[] bindSet = new Object[] { machineName, recipeName };

			List<DSPRunControl> dataInfoList = this.select(condition, bindSet);

			for (DSPRunControl dataInfo : dataInfoList)
			{
				if (dataInfo.getUseCount().intValue() != 0)
				{
					dataInfo.setUseCount(0);
					dataInfo.setLastEventName(eventInfo.getEventName());
					dataInfo.setLastEventUser(eventInfo.getEventUser());
					dataInfo.setLastEventTime(eventInfo.getEventTime());
					dataInfo.setLastEventComment(eventInfo.getEventComment());
					dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

					this.modify(eventInfo, dataInfo);
				}
			}
		}
		catch (Exception e)
		{
			logger.info("DSPRunControlData not exist");
		}
	}
	public void sendEmailByTrackOut(String machineName, Lot lotData)
	{
		try
		{
			DSPRunControl dataInfo = ExtendedObjectProxy.getDSPRunControlService().selectByKey(false,
					new Object[] { machineName, lotData.getProcessOperationName(), lotData.getProcessOperationVersion() });

			if (dataInfo.getUseCount().intValue() == dataInfo.getUseCountLimit().intValue())
			{
				String[] mailList = CommonUtil.getEmailListByAlarmGroup("DSPRunControlAlarm");

				if (mailList == null || mailList.length == 0)
					return;

				String message = "<pre>=======================AlarmInformation=======================</pre>";
				message += "<pre>==============================================================</pre>";
				message += "<pre>- RunControlAlarm			: " + "DSPRunControlAlarm" + "</pre>";
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
			logger.info("DSPRunControlData not exist");
		}
	}
}
