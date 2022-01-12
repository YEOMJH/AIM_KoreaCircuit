package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.TryRunControl;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TryRunControlService extends CTORMService<TryRunControl> {
	public static Log logger = LogFactory.getLog(TryRunControlService.class);
	private final String historyEntity = "TryRunControlHist";

	public List<TryRunControl> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<TryRunControl> result = super.select(condition, bindSet, TryRunControl.class);

		return result;
	}

	public TryRunControl selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(TryRunControl.class, isLock, keySet);
	}

	public TryRunControl create(EventInfo eventInfo, TryRunControl dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, TryRunControl dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public TryRunControl modify(EventInfo eventInfo, TryRunControl dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void increaseUseCount(EventInfo eventInfo, String machineName, Lot lotData, boolean validation) throws CustomException
	{
		// MantisID : 0000348
		// 当UseCount=UseCountLimint后，新的Lot 可以TrackIn Sucess，没有报错
		TryRunControl dataInfo = null;
		
		try
		{
			dataInfo = ExtendedObjectProxy.getTryRunControlService().selectByKey(false,
					new Object[] { machineName, lotData.getProcessOperationName(), lotData.getProcessOperationVersion() });
		}
		catch (Exception e)
		{
			logger.info("TryRunControlData not exist");
			return ;
		}
		
		String lotDataProcessOperationName = lotData.getProcessOperationName();
		String lotDataProcessOperationVersion = lotData.getProcessOperationVersion();
		String lotDataProductSpecName = lotData.getProductSpecName();
		String lotDataProdcutSpecVersion = lotData.getProductSpecVersion();
		String lotDataProcessFlowName = lotData.getProcessFlowName();
		String lotDataProcessFlowVersion = lotData.getProcessFlowVersion();

		if (StringUtils.isNotEmpty(dataInfo.getProductSpecName()) && StringUtils.isNotEmpty(dataInfo.getProductSpecVersion()))
		{
			String productSpecName = dataInfo.getProductSpecName();
			String productSpecVersion = dataInfo.getProductSpecVersion();

			if (!StringUtils.equals(productSpecName, lotDataProductSpecName) || !StringUtils.equals(productSpecVersion, lotDataProdcutSpecVersion))
			{
				logger.info("MachineName : " + machineName + ", OperationName + " + lotDataProcessOperationName + ", OperationVersion" + lotDataProcessOperationVersion
						+ "ProcessOperationName or ProcessOperation is mismatching");
				return;
			}
		}

		if (StringUtils.isNotEmpty(dataInfo.getProcessFlowName()) && StringUtils.isNotEmpty(dataInfo.getProcessFlowVersion()))
		{
			String processFlowName = dataInfo.getProcessFlowName();
			String processFlowVersion = dataInfo.getProcessFlowVersion();

			if (!StringUtils.equals(processFlowName, lotDataProcessFlowName) || !StringUtils.equals(processFlowVersion, lotDataProcessFlowVersion))
			{
				logger.info("MachineName : " + machineName + ", OperationName + " + lotDataProcessOperationName + ", OperationVersion" + lotDataProcessOperationVersion
						+ "ProcessFlowName or ProcessFlowVersion is mismatching");
				return;
			}
		}

		if (StringUtils.equals(dataInfo.getUseFlag(), "Y"))
		{
			if (validation && dataInfo.getUseCount().intValue() + 1 > dataInfo.getUseCountLimit().intValue())
				throw new CustomException("MACHINE-0040", machineName + ", " + lotDataProcessOperationName + ", " + lotDataProcessOperationVersion, "TryRunControl");

			dataInfo.setUseCount(dataInfo.getUseCount().intValue() + 1);
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

			this.modify(eventInfo, dataInfo);
		}
	}

	public void decreaseUseCount(EventInfo eventInfo, String machineName, Lot lotData)

	{
		try
		{
			TryRunControl dataInfo = ExtendedObjectProxy.getTryRunControlService().selectByKey(false,
					new Object[] { machineName, lotData.getProcessOperationName(), lotData.getProcessOperationVersion() });

			String lotDataProcessOperationName = lotData.getProcessOperationName();
			String lotDataProcessOperationVersion = lotData.getProcessOperationVersion();
			String lotDataProductSpecName = lotData.getProductSpecName();
			String lotDataProdcutSpecVersion = lotData.getProductSpecVersion();
			String lotDataProcessFlowName = lotData.getProcessFlowName();
			String lotDataProcessFlowVersion = lotData.getProcessFlowVersion();

			if (StringUtils.isNotEmpty(dataInfo.getProductSpecName()) && StringUtils.isNotEmpty(dataInfo.getProductSpecVersion()))
			{
				String productSpecName = dataInfo.getProductSpecName();
				String productSpecVersion = dataInfo.getProductSpecVersion();

				if (!StringUtils.equals(productSpecName, lotDataProductSpecName) || !StringUtils.equals(productSpecVersion, lotDataProdcutSpecVersion))
				{
					logger.info("MachineName : " + machineName + ", OperationName + " + lotDataProcessOperationName + ", OperationVersion" + lotDataProcessOperationVersion
							+ "ProcessOperationName or ProcessOperation is mismatching");
					return;
				}
			}

			if (StringUtils.isNotEmpty(dataInfo.getProcessFlowName()) && StringUtils.isNotEmpty(dataInfo.getProcessFlowVersion()))
			{
				String processFlowName = dataInfo.getProcessFlowName();
				String processFlowVersion = dataInfo.getProcessFlowVersion();

				if (!StringUtils.equals(processFlowName, lotDataProcessFlowName) || !StringUtils.equals(processFlowVersion, lotDataProcessFlowVersion))
				{
					logger.info("MachineName : " + machineName + ", OperationName + " + lotDataProcessOperationName + ", OperationVersion" + lotDataProcessOperationVersion
							+ "ProcessFlowName or ProcessFlowVersion is mismatching");
					return;
				}
			}

			if (StringUtils.equals(dataInfo.getUseFlag(), "Y"))
			{
				// MantisID : 0000348
				// CancelTrackIn，UseCount-1，不能将UseCount置为负数。最小为0，为0后UseCount不再减1
				int useCount = dataInfo.getUseCount().intValue() - 1;;
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
		}
		catch (Exception e)
		{
			logger.info("TryRunControlData not exist");
		}
	}

	public void sendEmailByTrackOut(EventInfo eventInfo, String machineName, Lot lotData)
	{
		try
		{
			TryRunControl dataInfo = ExtendedObjectProxy.getTryRunControlService().selectByKey(false,
					new Object[] { machineName, lotData.getProcessOperationName(), lotData.getProcessOperationVersion() });

			if (dataInfo.getUseCount().intValue() == dataInfo.getUseCountLimit().intValue())
			{
				Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
				MESMachineServiceProxy.getMachineServiceImpl().changeMachineLockFlag(eventInfo, machineData, "Y");

				String[] mailList = CommonUtil.getEmailListByAlarmGroup("TryRunControlAlarm");

				if (mailList == null || mailList.length == 0)
					return;

				String message = "<pre>=======================AlarmInformation=======================</pre>";
				message += "<pre>==============================================================</pre>";
				message += "<pre>- RunControlAlarm			: " + "TryRunControlAlarm" + "</pre>";
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
			logger.info("TryRunControlData not exist");
		}
	}
}
