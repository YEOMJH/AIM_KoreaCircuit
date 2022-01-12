package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MQCRunControl;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sun.xml.internal.fastinfoset.stax.EventLocation;

public class MQCRunControlService extends CTORMService<MQCRunControl> {
	public static Log logger = LogFactory.getLog(MQCRunControlService.class);

	private final String historyEntity = "MQCRunControlHist";

	public List<MQCRunControl> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<MQCRunControl> result = super.select(condition, bindSet, MQCRunControl.class);

		return result;
	}

	public MQCRunControl selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MQCRunControl.class, isLock, keySet);
	}

	public MQCRunControl create(EventInfo eventInfo, MQCRunControl dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, MQCRunControl dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public MQCRunControl modify(EventInfo eventInfo, MQCRunControl dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public MQCRunControl createMQCRunControl(EventInfo eventInfo, String machineName, String processOperationName, String processOperationVersion, String factoryName,
			String maxProductQtyByChamber, String maxMQCQtyByChamber, String chamberQty, String mqcProcessFlowName, String mqcProcessFlowVersion)
	{
		MQCRunControl dataInfo = new MQCRunControl();
		dataInfo.setMachineName(machineName);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(processOperationVersion);
		dataInfo.setActualProductQty(0);
		dataInfo.setFactoryName(factoryName);
		dataInfo.setMaxProductQtyByChamber(StringUtil.isEmpty(maxProductQtyByChamber) ? 0 : Integer.valueOf(maxProductQtyByChamber));
		dataInfo.setMaxMQCQtyByChamber(StringUtil.isEmpty(maxMQCQtyByChamber) ? 0 : Integer.valueOf(maxMQCQtyByChamber));
		dataInfo.setChamberQty(StringUtil.isEmpty(chamberQty) ? 0 : Integer.valueOf(chamberQty));
		dataInfo.setMqcProcessFlowName(mqcProcessFlowName);
		dataInfo.setMqcProcessFlowVersion(mqcProcessFlowVersion);
		dataInfo.setMqcProcessQty(0);
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
		
		dataInfo = this.create(eventInfo, dataInfo);

		return dataInfo;
	}

	public MQCRunControl updateActualProductQty(EventInfo eventInfo, String machineName, String processOperationName, String processOperationVersion, int actualProductQty)
			throws greenFrameDBErrorSignal, CustomException
	{
		MQCRunControl dataInfo = null;
		
		try
		{
			dataInfo = ExtendedObjectProxy.getMQCRunControlService().selectByKey(false, new Object[] { machineName, processOperationName, processOperationVersion });
			
			int oldActualProductQty = dataInfo.getActualProductQty() == null ? 0 : Integer.parseInt(dataInfo.getActualProductQty().toString());
			
			dataInfo.setActualProductQty(oldActualProductQty + actualProductQty);
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
			
			dataInfo = ExtendedObjectProxy.getMQCRunControlService().modify(eventInfo, dataInfo);
		}
		catch(Exception ex)
		{
			dataInfo = null;
			logger.info("updateActualProductQty : " + ex.getCause());
		}
		
		return dataInfo;
	}
	
	public List<MQCRunControl> updateMQCProcessQty(EventInfo eventInfo, String machineName, String mqcFlowName, String mqcFlowVersion, int mqcProcessQty)
			throws greenFrameDBErrorSignal, CustomException
	{
		List<MQCRunControl> newDataInfoList = new ArrayList<MQCRunControl>();
		
		try
		{
			String condition = "WHERE 1 = 1 "
							 + "  AND MACHINENAME = ? "
							 + "  AND MQCPROCESSFLOWNAME = ? "
							 + "  AND MQCPROCESSFLOWVERSION = ? ";
			
			List<MQCRunControl> dataInfoList = ExtendedObjectProxy.getMQCRunControlService().select(condition, new Object[] { machineName, mqcFlowName, mqcFlowVersion });
			if (dataInfoList == null || dataInfoList.size() == 0)
			{
				logger.info("updateMQCProcessQty : No Exist Data.");
				return null;
			}
			
			
			
			for (MQCRunControl dataInfo : dataInfoList) 
			{
				int oldMqcProcessQty = dataInfo.getMqcProcessQty() == null ? 0 : Integer.parseInt(dataInfo.getMqcProcessQty().toString());
				
				dataInfo.setMqcProcessQty(oldMqcProcessQty + mqcProcessQty);
				dataInfo.setLastEventName(eventInfo.getEventName());
				dataInfo.setLastEventUser(eventInfo.getEventUser());
				dataInfo.setLastEventTime(eventInfo.getEventTime());
				dataInfo.setLastEventComment(eventInfo.getEventComment());
				dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
				
				dataInfo = ExtendedObjectProxy.getMQCRunControlService().modify(eventInfo, dataInfo);
				
				newDataInfoList.add(dataInfo);
			}
			
			return newDataInfoList;
		}
		catch(Exception ex)
		{
			newDataInfoList = null;
			logger.info("updateMQCProcessQty : " + ex.getCause());
		}
		
		return newDataInfoList;
	}

	public void initialQty(EventInfo eventInfo, String machineName, String processOperationName, String processOperationVersion)
			throws greenFrameDBErrorSignal, CustomException
	{
		try
		{
			MQCRunControl dataInfo = ExtendedObjectProxy.getMQCRunControlService().selectByKey(false, new Object[] { machineName, processOperationName, processOperationVersion });
			
			dataInfo.setActualProductQty(0);
			dataInfo.setMqcProcessQty(0);
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
			
			dataInfo = ExtendedObjectProxy.getMQCRunControlService().modify(eventInfo, dataInfo);
		}
		catch(Exception ex)
		{
			logger.info("initialQty : " + ex.getCause());
		}
	}
	
	public void initialQtyByFlow(EventInfo eventInfo, String machineName, String processFlowName, String processFlowVersion)
			throws greenFrameDBErrorSignal, CustomException
	{
		try
		{
			String condition = " WHERE MACHINENAME = ? AND MQCPROCESSFLOWNAME = ? AND MQCPROCESSFLOWVERSION = ? ";
			List<MQCRunControl> dataInfoList = ExtendedObjectProxy.getMQCRunControlService().select(condition, new Object[] { machineName, processFlowName, processFlowVersion });
			for (MQCRunControl dataInfo : dataInfoList)
			{
				dataInfo.setActualProductQty(0);
				dataInfo.setMqcProcessQty(0);
				dataInfo.setLastEventName(eventInfo.getEventName());
				dataInfo.setLastEventUser(eventInfo.getEventUser());
				dataInfo.setLastEventTime(eventInfo.getEventTime());
				dataInfo.setLastEventComment(eventInfo.getEventComment());
				dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
			
				ExtendedObjectProxy.getMQCRunControlService().modify(eventInfo, dataInfo);
			}
		}
		catch(Exception ex)
		{
			logger.info("initialQty : " + ex.getCause());
		}
	}
	
	public void deleteMQCRunControlData(EventInfo eventInfo, String machineName, String processOperationName, String processOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		MQCRunControl dataInfo = ExtendedObjectProxy.getMQCRunControlService().selectByKey(false, new Object[] { machineName, processOperationName, processOperationVersion });
		this.remove(eventInfo, dataInfo);
	}
}
