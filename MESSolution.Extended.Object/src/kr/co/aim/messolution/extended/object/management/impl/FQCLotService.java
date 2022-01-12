package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.FQCLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FQCLotService extends CTORMService<FQCLot> {

	public static Log logger = LogFactory.getLog(FQCLot.class);

	private final String historyEntity = "FQCLotHistory";

	public List<FQCLot> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<FQCLot> result = super.select(condition, bindSet, FQCLot.class);

		return result;
	}

	public FQCLot selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(FQCLot.class, isLock, keySet);
	}

	public FQCLot create(EventInfo eventInfo, FQCLot dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void create(EventInfo eventInfo, List<FQCLot> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfoList);

		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}

	public void remove(EventInfo eventInfo, FQCLot dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public FQCLot modify(EventInfo eventInfo, FQCLot dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void modify(EventInfo eventInfo, List<FQCLot> dataInfoList)
	{
		super.update(dataInfoList);

		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}

	public FQCLot getFQCLotData(String fqcLotName, long seq)
	{
		FQCLot dataInfo = new FQCLot();

		try
		{
			dataInfo = this.selectByKey(false, new Object[] { fqcLotName, seq });
		}
		catch (Exception e)
		{
			logger.info("FQCLotData is not exist ( " + fqcLotName + ", " + seq + " )");
			dataInfo = null;
		}

		return dataInfo;
	}

	public String getFQCLotSeq(String lotName)
	{
		String sql = " SELECT NVL(MAX(SEQ) , 0) + 1 AS SEQ FROM CT_FQCLOT WHERE FQCLOTNAME = :FQCLOTNAME ";
		String seq = "";

		Map<String, String> args = new HashMap<String, String>();
		args.put("FQCLOTNAME", lotName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

		if (result.size() > 0)
		{
			seq = ConvertUtil.getMapValueByName(result.get(0), "SEQ");
		}

		return seq;
	}
   // getFirstFQCLotData Modify 
	public List<FQCLot> getFirstFQCLotData(String fqcLotName)
	{
		String condition = " LOTSTATE = 'Released' AND FQCLOTNAME = ? ORDER BY seq DESC ";
		Object[] bindSet = new Object[] { fqcLotName };

		List<FQCLot> dataInfoList = new ArrayList<FQCLot>();

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

	public FQCLot updateFQCLotData(EventInfo eventInfo, String fqcLotName, long seq, String fqcResult)
	{
		FQCLot dataInfo = this.getFQCLotData(fqcLotName, seq);

		if (dataInfo != null)
		{
			dataInfo.setFqcResult(fqcResult);
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
			dataInfo.setLastEventUser(eventInfo.getEventUser());

			dataInfo = this.modify(eventInfo, dataInfo);
		}

		return dataInfo;
	}

	public FQCLot createFQCLotData(EventInfo eventInfo, String trayGroupName, long seq, String productSpecName, int trayQuantity, int panelQuantity, String panelGrade, String sampleRule,
			String machineName) throws greenFrameDBErrorSignal, CustomException
	{
		FQCLot dataInfo = new FQCLot();
		dataInfo.setFqcLotName(trayGroupName);
		dataInfo.setSeq(seq);
		dataInfo.setProductSpec(productSpecName);
		dataInfo.setTrayQuantity(trayQuantity);
		dataInfo.setPanelQuantity(panelQuantity);
		dataInfo.setPanelGrade(panelGrade);
		dataInfo.setLotState("Released");
		dataInfo.setSampleRule(sampleRule);
		dataInfo.setMachineName(machineName);
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setCreateUser(eventInfo.getEventUser());
		dataInfo.setCreateTime(eventInfo.getEventTime());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
		dataInfo.setLastEventUser(eventInfo.getEventUser());

		ExtendedObjectProxy.getFQCLotService().create(eventInfo, dataInfo);

		return dataInfo;
	}

	public FQCLot completeFQCLotData(EventInfo eventInfo, String fqcLotName, long seq) throws greenFrameDBErrorSignal, NumberFormatException, CustomException
	{
		FQCLot dataInfo = this.getFQCLotData(fqcLotName, seq);

		if (dataInfo != null)
		{
			dataInfo.setLotState(GenericServiceProxy.getConstantMap().Lot_Completed);
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
			dataInfo.setLastEventUser(eventInfo.getEventUser());
		}
		else
		{
			throw new CustomException("FQC-0001", fqcLotName + ", " + seq);
		}

		ExtendedObjectProxy.getFQCLotService().modify(eventInfo, dataInfo);

		return dataInfo;
	}
	
	public String getCurrentFQCLotSeq(String lotName)
	{
		String sql = " SELECT MAX(SEQ) AS SEQ FROM CT_FQCLOT WHERE FQCLOTNAME = :FQCLOTNAME ";
		String seq = "";

		Map<String, String> args = new HashMap<String, String>();
		args.put("FQCLOTNAME", lotName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

		if (result.size() > 0)
		{
			seq = ConvertUtil.getMapValueByName(result.get(0), "SEQ");
		}

		return seq;
	}
}
