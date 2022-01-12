package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.LotQueueTime;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LotQueueTimeService extends CTORMService<LotQueueTime> {

	public static Log logger = LogFactory.getLog(LotQueueTimeService.class);
	
	private final String historyEntity = "LotQueueTimeHist";
	
	public List<LotQueueTime> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<LotQueueTime> result = super.select(condition, bindSet, LotQueueTime.class);
		
		return result;
	}
	
	public LotQueueTime selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(LotQueueTime.class, isLock, keySet);
	}
	
	public LotQueueTime create(EventInfo eventInfo, LotQueueTime dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, LotQueueTime dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public LotQueueTime modifyToNew(EventInfo eventInfo, LotQueueTime oldData, LotQueueTime newData)
	{
		super.updateToNew(oldData, newData);
		
		super.addHistory(eventInfo, this.historyEntity, newData, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(newData).toArray());
	}
	
	public LotQueueTime modify(EventInfo eventInfo, LotQueueTime dataInfo)
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public boolean isInQTime(String lotName)
	{
		boolean result = false;
		
		try
		{
			List<LotQueueTime> resultList = ExtendedObjectProxy.getQTimeService().select("lotName = ?", new Object[] {lotName});
			
			if (resultList.size() > 0)
				result = true;
		}
		catch (greenFrameDBErrorSignal ne)
		{
			result = false;
		}
		catch (CustomException ce)
		{
			result = false;
		}
		
		return result;
	}

	public List<LotQueueTime> findQTimeByLot(String lotName) throws CustomException
	{
		List<LotQueueTime> resultList;

		try
		{
			resultList = ExtendedObjectProxy.getQTimeService().select("lotName = ? AND queueTimeState <> ?",
					new Object[] { lotName, GenericServiceProxy.getConstantMap().QTIME_STATE_CONFIRM });
		}
		catch (Exception ex)
		{
			resultList = new ArrayList<LotQueueTime>();
		}

		return resultList;
	}

	public void monitorQTime(EventInfo eventInfo, String lotName) throws CustomException
	{
		List<LotQueueTime> QTimeDataList = findQTimeByLot(lotName);

		for (LotQueueTime QTimeData : QTimeDataList)
		{
			try
			{
				GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);

				// in form of millisecond
				Double expired = ConvertUtil.getDiffTime(TimeUtils.toTimeString(QTimeData.getEnterTime(), TimeStampUtil.FORMAT_TIMEKEY), TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY));
				//// limit unit is hour and decimal form
				expired = (expired / 60 / 60);

				if (Double.compare(expired, Double.parseDouble(QTimeData.getInterlockDurationLimit())) >= 0
						&& (QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_WARN)
								|| QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_IN)))
				{
					lockQTime(eventInfo, QTimeData.getLotName(), QTimeData.getFactoryName(), QTimeData.getProcessFlowName(), QTimeData.getProcessOperationName(), QTimeData.getToFactoryName(),
							QTimeData.getToProcessFlowName(), QTimeData.getToProcessOperationName());
				}

				else if (Double.compare(expired, Double.parseDouble(QTimeData.getWarningDurationLimit())) >= 0
						&& QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_IN))
				{
					warnQTime(eventInfo, QTimeData.getLotName(), QTimeData.getFactoryName(), QTimeData.getProcessFlowName(), QTimeData.getProcessOperationName(), QTimeData.getToFactoryName(),
							QTimeData.getToProcessFlowName(), QTimeData.getToProcessOperationName());
				}

				GenericServiceProxy.getTxDataSourceManager().commitTransaction();

			}
			catch (Exception ex)
			{
				GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();

				logger.warn(String.format("Q-Time[%s %s %s] monitoring failed", QTimeData.getLotName(), QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName()));
			}
		}
	}

	public void processQTime(EventInfo eventInfo, String lotName, String factoryName, String productSpecName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion)
		throws CustomException
	{
		moveInQTime(eventInfo, lotName, factoryName, productSpecName, processFlowName, processFlowVersion, processOperationName, processOperationVersion);
		exitQTime(eventInfo, lotName, factoryName, processFlowName, processOperationName);
	}
	
	public void moveInQTime(EventInfo eventInfo, String lotName, String factoryName, String productSpecName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion)
		throws CustomException
	{
		List<ListOrderedMap> QTimePolicyList = PolicyUtil.getQTimeSpec(factoryName, productSpecName, processFlowName, processFlowVersion, processOperationName, processOperationVersion);
		
		for (ListOrderedMap QtimePolicy : QTimePolicyList)
		{
			String toFactoryName = CommonUtil.getValue(QtimePolicy, "TOFACTORYNAME");
			String toProcessFlowName = CommonUtil.getValue(QtimePolicy, "TOPROCESSFLOWNAME");
			String toProcessOperationName = CommonUtil.getValue(QtimePolicy, "TOPROCESSOPERATIONNAME");
			String warningLimit = CommonUtil.getValue(QtimePolicy, "WARNINGDURATIONLIMIT");
			String interLockLimit = CommonUtil.getValue(QtimePolicy, "INTERLOCKDURATIONLIMIT");
			
			try
			{
				//enter into Q time
				moveInQTime(eventInfo, lotName, factoryName, processFlowName, processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName, warningLimit, interLockLimit, false);
			}
			catch (Exception ex)
			{
				//Q-time process is optional
				if (logger.isWarnEnabled())
					logger.warn(String.format("Q-time IN process for Lot[%s] to Operation[%s] is failed at Operation[%s]", lotName, toProcessOperationName, processOperationName));
			}
		}
	}
	
	public void exitQTime(EventInfo eventInfo, String lotName, String toFactoryName, String toProcessFlowName, String toProcessOperationName)
		throws CustomException
	{
		List<LotQueueTime> QTimeDataList;
		
		try
		{
			QTimeDataList = ExtendedObjectProxy.getQTimeService().select("lotName = ? AND toFactoryName = ? AND toProcessFlowName = ? AND toProcessOperationName = ?",
																			new Object[] {lotName, toFactoryName, toProcessFlowName, toProcessOperationName});
		}
		catch (Exception ex)
		{
			QTimeDataList = new ArrayList<LotQueueTime>();
		}
		
		for (LotQueueTime QTimeData : QTimeDataList)
		{
			try
			{				
				//enter into Q time
				exitQTime(eventInfo, QTimeData);
			}
			catch (Exception ex)
			{
				//Q-time process is optional
				if (logger.isWarnEnabled())
					logger.warn(String.format("Q-time OUT process for Lot[%s] to Operation[%s] is failed at Operation[%s]",
								QTimeData.getLotName(), QTimeData.getToProcessOperationName(), QTimeData.getProcessOperationName()));
			}
		}
	}
	
	public void validateQTime(EventInfo eventInfo, String lotName)
		throws CustomException
	{
		List<LotQueueTime> QTimeDataList = findQTimeByLot(lotName);;
		
		for (LotQueueTime QTimeData : QTimeDataList)
		{
			validateQTime(QTimeData);
		}
		

	}
	
	public void validateQTime(LotQueueTime QTimeData)
		throws CustomException
	{
		
		//compare
		if (QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_OVER))
		{
			//interlock action prior to log-in
			throw new CustomException("LOT-0203", QTimeData.getLotName(), QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName());
		}
		else if (QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_WARN))
		{
			//interlock action prior to log-in
			logger.warn(String.format("Q-Time[%s %s %s] would be expired soon",
							QTimeData.getLotName(), QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName()));
		}
	}

	public String doActionQTime(LotQueueTime QTimeData)
		throws CustomException
	{
		if (QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_OVER))
		{
			//do interlock action
			try
			{
				return "REWORK";
			}
			catch (Exception ex)
			{
				logger.warn(String.format("Q-Time[%s %s %s] interlock action failed",
								QTimeData.getLotName(), QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName()));
			}
		}
		else if (QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_WARN))
		{
			//do warning action
			logger.warn(String.format("Q-Time[%s %s %s] is warning to interlock",
							QTimeData.getLotName(), QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName()));
			
			return "HOLD";
		}
		
		return "";
	}
	
	public void resolveQTime(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processOperationName, String toFactoryName, String toProcessFlowName, String toProcessOperationName)
		throws CustomException
	{
		try
		{
			eventInfo.setEventName("Resolve");
			
			LotQueueTime QtimeData = ExtendedObjectProxy.getQTimeService().selectByKey(true, new Object[] {lotName, factoryName, processFlowName, processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName});
			QtimeData.setResolveTime(eventInfo.getEventTime());
			QtimeData.setResolveUser(eventInfo.getEventUser());
			QtimeData.setQueueTimeState(GenericServiceProxy.getConstantMap().QTIME_STATE_CONFIRM);
			
			ExtendedObjectProxy.getQTimeService().modify(eventInfo, QtimeData);
		}
		catch (greenFrameDBErrorSignal ne)
		{
			//ignore error to consider as not found exception signal
			throw new CustomException("LOT-0201", lotName, processOperationName, toProcessOperationName);
		}
	}

	public void exitQTime(EventInfo eventInfo, LotQueueTime QTimeData)
		throws CustomException
	{
		try
		{
			eventInfo.setEventName("Exit");
			
			QTimeData.setExitTime(eventInfo.getEventTime());
			ExtendedObjectProxy.getQTimeService().remove(eventInfo, QTimeData);
		}
		catch (greenFrameDBErrorSignal ne)
		{
			//ignore error to consider as not found exception signal
			throw new CustomException("LOT-0201", QTimeData.getLotName(), QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName());
		}
	}
	
	public void moveInQTime(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processOperationName, 
			                String toFactoryName, String toProcessFlowName, String toProcessOperationName,
							String warningLimit, String interLockLimit, boolean dummyFlag)
		throws CustomException
	{
		LotQueueTime QtimeData = new LotQueueTime(lotName, factoryName, processFlowName, processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName);
		
		QtimeData.setEnterTime(eventInfo.getEventTime());
		QtimeData.setWarningDurationLimit(warningLimit);
		QtimeData.setInterlockDurationLimit(interLockLimit);
		QtimeData.setQueueTimeState(GenericServiceProxy.getConstantMap().QTIME_STATE_IN);
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		Map<String, String> udfs = lotData.getUdfs();
		
		try
		{
			List<LotQueueTime> LotQTimeDataList = ExtendedObjectProxy.getQTimeService().select("lotName = ? AND factoryName = ? AND processFlowName = ? AND processOperationName = ? AND toFactoryName = ? AND toProcessFlowName = ? AND toProcessOperationName = ?",
																		new Object[] {lotName, factoryName, processFlowName, processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName});

			eventInfo.setEventName("Update");
			try
			{

				if(udfs.get("RETURNFLOWNAME").equals(""))
				{
					ExtendedObjectProxy.getQTimeService().modify(eventInfo, QtimeData);
				}
				
				else
				{
					if(toProcessFlowName.equals(udfs.get("RETURNFLOWNAME")))
					{
						ExtendedObjectProxy.getQTimeService().modify(eventInfo, QtimeData);
					}
					if(toProcessFlowName.equals(processFlowName))
					{
						ExtendedObjectProxy.getQTimeService().modify(eventInfo, QtimeData);
					}
				}
				
			}
			catch (greenFrameDBErrorSignal ne)
			{
				throw new CustomException("LOT-0201", lotName, processOperationName, toProcessOperationName);
			}
		}
		catch (greenFrameDBErrorSignal ne)
		{
			eventInfo.setEventName("Enter");
			
			try
			{
				if(udfs.get("RETURNFLOWNAME").equals(""))
				{
					ExtendedObjectProxy.getQTimeService().create(eventInfo, QtimeData);
				}
				
				else
				{
					if(toProcessFlowName.equals(udfs.get("RETURNFLOWNAME")))
					{
						ExtendedObjectProxy.getQTimeService().create(eventInfo, QtimeData);
					}
					if(toProcessFlowName.equals(processFlowName))
					{
						ExtendedObjectProxy.getQTimeService().create(eventInfo, QtimeData);
					}
				}
			}
			catch (greenFrameDBErrorSignal ne1)
			{
				//ignore error to consider as duplicated exception signal
				throw new CustomException("LOT-0202", lotName, processOperationName, toProcessOperationName);
			}
		}
	}
	
	public void warnQTime(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processOperationName, 
			String toFactoryName, String toProcessFlowName, String toProcessOperationName)
		throws CustomException
	{
		try
		{
			eventInfo.setEventName("Warn");
			
			LotQueueTime QtimeData = ExtendedObjectProxy.getQTimeService().selectByKey(true, new Object[] {lotName, factoryName, processFlowName, processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName});
			QtimeData.setWarningTime(eventInfo.getEventTime());
			QtimeData.setQueueTimeState(GenericServiceProxy.getConstantMap().QTIME_STATE_WARN);
			
			ExtendedObjectProxy.getQTimeService().modify(eventInfo, QtimeData);
		}
		catch (greenFrameDBErrorSignal ne)
		{
			//ignore error to consider as not found exception signal
			throw new CustomException("LOT-0201", lotName, processOperationName, toProcessOperationName);
		}
	}
	
	public void lockQTime(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processOperationName, String toFactoryName, String toProcessFlowName, String toProcessOperationName)
		throws CustomException
	{
		try
		{
			eventInfo.setEventName("Interlock");
			
			LotQueueTime QtimeData = ExtendedObjectProxy.getQTimeService().selectByKey(true, new Object[] {lotName, factoryName, processFlowName, processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName});
			QtimeData.setInterlockTime(eventInfo.getEventTime());
			QtimeData.setQueueTimeState(GenericServiceProxy.getConstantMap().QTIME_STATE_OVER);
			
			ExtendedObjectProxy.getQTimeService().modify(eventInfo, QtimeData);
		}
		catch (greenFrameDBErrorSignal ne)
		{
			//ignore error to consider as not found exception signal
			throw new CustomException("LOT-0201", lotName, processOperationName, toProcessOperationName);
		}
	}
	
	public void checkPcellQtime(Lot lotData)
		throws CustomException
	{
		List<ListOrderedMap> QTimePolicyList = PolicyUtil.getQTimeSpec(
				lotData.getFactoryName(), lotData.getProductSpecName(), 
				lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());
		
		for(ListOrderedMap QTimePolicy : QTimePolicyList)
		{
			String processOperationName = CommonUtil.getValue(QTimePolicy, "PROCESSOPERATIONNAME");
			String toFactoryName = CommonUtil.getValue(QTimePolicy, "TOFACTORYNAME");
			String toProcessFlowName = CommonUtil.getValue(QTimePolicy, "TOPROCESSFLOWNAME");
			String toProcessOperationName = CommonUtil.getValue(QTimePolicy, "TOPROCESSOPERATIONNAME");
			String interLockLimit = CommonUtil.getValue(QTimePolicy, "INTERLOCKDURATIONLIMIT");
			
			if(interLockLimit.isEmpty())
				return;
			
			//TODO: Check toFactory&toProcessFlow
			if(StringUtil.equals(processOperationName, toProcessOperationName))
			{
				Double expired = ConvertUtil.getDiffTime(
						TimeUtils.toTimeString(lotData.getLastLoggedInTime(), TimeStampUtil.FORMAT_TIMEKEY),
						TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY));
				
				//CurrentTime - TrackInTime
				expired = (expired / 60);
				
				if (Double.compare(expired, Double.parseDouble(interLockLimit)) >= 0)
				{
					throw new CustomException("LOT-0204", lotData.getKey().getLotName(), 
							TimeUtils.toTimeString(lotData.getLastLoggedInTime(), TimeStampUtil.FORMAT_TIMEKEY),
							interLockLimit);
				}
			}
		}
	}
	
	public void cloneQTime(EventInfo eventInfo, String sourceLotName, List<String> destLotNameList)
	{
		try
		{
			List<LotQueueTime> QTimeDataList = findQTimeByLot(sourceLotName);
			
			for (LotQueueTime QTimeData : QTimeDataList)
			{

				for (String destLotName : destLotNameList)
				{
					QTimeData.setLotName(destLotName);
					try 
					{
						ExtendedObjectProxy.getQTimeService().create(eventInfo, QTimeData);
					}
					catch (Exception e) 
					{
						logger.warn("Clone Q-Time Error! Lot:" + destLotName + " From " + sourceLotName);
					}
				}
			}
		}
		catch (Exception ex)
		{				
			logger.warn("Clone Q-Time Error!");
		}
	}
	
	public void mergeQTime(EventInfo eventInfo, String destLotName, List<String> sourceLotNameList)
	{
		try
		{
			//Get oldest QTime
			StringBuffer sqlBuffer = new StringBuffer()
			.append(" WITH MINQTIME ")
			.append("      AS (  SELECT Q.FACTORYNAME, ")
			.append("                   Q.PROCESSFLOWNAME, ")
			.append("                   Q.PROCESSOPERATIONNAME, ")
			.append("                   Q.TOFACTORYNAME, ")
			.append("                   Q.TOPROCESSFLOWNAME, ")
			.append("                   Q.TOPROCESSOPERATIONNAME, ")
			.append("                   MIN (Q.ENTERTIME) AS ENTERTIME ")
			.append("              FROM CT_LOTQUEUETIME Q ")
			.append("              WHERE Q.LOTNAME IN (:LOTNAMELIST) ")
			.append("          GROUP BY Q.FACTORYNAME, ")
			.append("                   Q.PROCESSFLOWNAME, ")
			.append("                   Q.PROCESSOPERATIONNAME, ")
			.append("                   Q.TOFACTORYNAME, ")
			.append("                   Q.TOPROCESSFLOWNAME, ")
			.append("                   Q.TOPROCESSOPERATIONNAME) ")
			.append(" SELECT Q.LOTNAME, Q.FACTORYNAME, Q.PROCESSFLOWNAME, Q.PROCESSOPERATIONNAME, Q.TOFACTORYNAME, Q.TOPROCESSFLOWNAME, Q.TOPROCESSOPERATIONNAME ")
			.append("   FROM CT_LOTQUEUETIME Q, MINQTIME M ")
			.append("  WHERE     Q.FACTORYNAME = M.FACTORYNAME ")
			.append("        AND Q.PROCESSFLOWNAME = M.PROCESSFLOWNAME ")
			.append("        AND Q.PROCESSOPERATIONNAME = M.PROCESSOPERATIONNAME ")
			.append("        AND Q.TOFACTORYNAME = M.TOFACTORYNAME ")
			.append("        AND Q.TOPROCESSFLOWNAME = M.TOPROCESSFLOWNAME ")
			.append("        AND Q.TOPROCESSOPERATIONNAME = M.TOPROCESSOPERATIONNAME ")
			.append("        AND Q.ENTERTIME = M.ENTERTIME ");

			Object[] bindArray = new Object[0];
			
			List<ListOrderedMap> result;
			
			try
			{
				HashMap<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("LOTNAMELIST", sourceLotNameList);
				result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bindMap);
			}
			catch (FrameworkErrorSignal fe)
			{
				result = null;
				throw new CustomException("SYS-9999", fe.getMessage());
			}

			for (ListOrderedMap row : result)
			{
				String lotName = CommonUtil.getValue(row, "LOTNAME");
				String factoryName = CommonUtil.getValue(row, "FACTORYNAME");
				String processFlowName = CommonUtil.getValue(row, "PROCESSFLOWNAME");
				String processOperationName = CommonUtil.getValue(row, "PROCESSOPERATIONNAME");
				String toFactoryName = CommonUtil.getValue(row, "TOFACTORYNAME");
				String toProcessFlowName = CommonUtil.getValue(row, "TOPROCESSFLOWNAME");
				String toProcessOperationName = CommonUtil.getValue(row, "TOPROCESSOPERATIONNAME");
				
				//Get QTime Data
				LotQueueTime QtimeData = ExtendedObjectProxy.getQTimeService().selectByKey(true, new Object[] {lotName, factoryName, processFlowName, processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName});
				
				//Set New LotName
				QtimeData.setLotName(destLotName);
				
				//Create
				eventInfo.setEventName("Create");
				try
				{
					ExtendedObjectProxy.getQTimeService().create(eventInfo, QtimeData);
				}
				catch (greenFrameDBErrorSignal ne1)
				{
					//ignore error to consider as duplicated exception signal
					throw new CustomException("LOT-0202", lotName, processOperationName, toProcessOperationName);
				}
			}
		}
		catch (Exception ex)
		{				
			logger.warn("Merge Q-Time Error!");
		}
	}
}
