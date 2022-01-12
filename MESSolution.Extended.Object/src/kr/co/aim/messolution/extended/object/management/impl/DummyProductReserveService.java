package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.DummyProductReserve;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DummyProductReserveService extends CTORMService<DummyProductReserve> {
	public static Log logger = LogFactory.getLog(ShieldSpecService.class);

	private final String historyEntity = "DummyProductReserveHist";

	public List<DummyProductReserve> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<DummyProductReserve> result = super.select(condition, bindSet, DummyProductReserve.class);

		return result;
	}

	public DummyProductReserve selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(DummyProductReserve.class, isLock, keySet);
	}

	public DummyProductReserve create(EventInfo eventInfo, DummyProductReserve dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, DummyProductReserve dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public DummyProductReserve modify(EventInfo eventInfo, DummyProductReserve dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public DummyProductReserve getDummyProductReserveData(String lotName, String seq)
	{
		DummyProductReserve dataInfo = new DummyProductReserve();
		Object[] keySet = new Object[] { lotName, seq };

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

	public List<DummyProductReserve> getDummyProductReserveDataList(String lotName)
	{
		List<DummyProductReserve> dataInfoList = new ArrayList<DummyProductReserve>();
		String condition = " LOTNAME = ? ";
		Object[] bindSet = new Object[] { lotName };

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

	public boolean checkDummyProductReserveData(String lotName)
	{
		List<DummyProductReserve> dataInfoList = this.getDummyProductReserveDataList(lotName);

		if (dataInfoList != null)
			return true;
		else
			return false;
	}

	public DummyProductReserve createDummyProductReserve(EventInfo eventInfo, String lotName, String seq, String factoryName, String productSpecName, String productSpecVersion,
			String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String productRequestName) throws CustomException
	{
		DummyProductReserve dataInfo = new DummyProductReserve();

		dataInfo.setLotName(lotName);
		dataInfo.setSeq(seq);
		dataInfo.setFactoryName(factoryName);
		dataInfo.setProductSpecName(productSpecName);
		dataInfo.setProductSpecVersion(productSpecVersion);
		dataInfo.setProcessFlowName(processFlowName);
		dataInfo.setProcessFlowVersion(processFlowVersion);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(processOperationVersion);
		dataInfo.setProductRequestName(productRequestName);
		dataInfo.setProcessingFlag("N");
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

		this.create(eventInfo, dataInfo);

		return dataInfo;
	}

	public int getMaxSeq(String lotName)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT MAX (TO_NUMBER (SEQ)) AS SEQ, LOTNAME ");
		sql.append("  FROM CT_DUMMYPRODUCTRESERVE ");
		sql.append(" WHERE LOTNAME = :LOTNAME ");
		sql.append("GROUP BY LOTNAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("LOTNAME", lotName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		int maxSeq = 0;

		if (sqlResult.size() > 0)
			maxSeq = Integer.parseInt(ConvertUtil.getMapValueByName(sqlResult.get(0), "SEQ"));

		return maxSeq;
	}

	public DummyProductReserve getMaxOperationName(String lotName)
	{
		String condition = " LOTNAME = ? ORDER BY SEQ DESC ";
		Object[] bindSet = new Object[] { lotName };

		List<DummyProductReserve> dataInfoList = new ArrayList<DummyProductReserve>();

		try
		{
			dataInfoList = this.select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList.get(0);
	}

	public void deleteDummyProductReserve(EventInfo eventInfo, String lotName, String seq)
	{
		DummyProductReserve dataInfo = this.getDummyProductReserveData(lotName, seq);

		this.remove(eventInfo, dataInfo);
	}

	public void deleteDummyProductReserve(EventInfo eventInfo, String lotName)
	{
		List<DummyProductReserve> dataInfoList = this.getDummyProductReserveDataList(lotName);

		if (dataInfoList != null)
		{
			for (DummyProductReserve dataInfo : dataInfoList)
				this.remove(eventInfo, dataInfo);
		}
	}

	public List<DummyProductReserve> getDummyProductReserveData(Lot lotData)
	{
		String condition = " LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? ";
		Object[] bindSet = new Object[] { lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(),
				lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion() };

		List<DummyProductReserve> dataInfoList = new ArrayList<DummyProductReserve>();

		try
		{
			dataInfoList = this.select(condition, bindSet);
		}
		catch (Exception e)
		{
			logger.info("DummyProductReserve data is not exist");
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public void checkProcessingFlag(EventInfo eventInfo, Lot lotData, Lot afterTrackOutLot) throws CustomException
	{
		List<DummyProductReserve> dataInfoList = this.getDummyProductReserveData(lotData);

		if (dataInfoList != null)
		{
			for (DummyProductReserve dataInfo : dataInfoList)
			{
				dataInfo.setProcessingFlag("Y");
				dataInfo.setLastEventName(eventInfo.getEventName());
				dataInfo.setLastEventUser(eventInfo.getEventUser());
				dataInfo.setLastEventTime(eventInfo.getEventTime());
				dataInfo.setLastEventComment(eventInfo.getEventComment());
				dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

				this.modify(eventInfo, dataInfo);
			}
			
			String condition = " LOTNAME = ? ";
			Object[] bindSet = new Object[] { lotData.getKey().getLotName() };

			List<DummyProductReserve> reserveList = this.select(condition, bindSet);

			boolean processingNFlag = false;
			
			for (DummyProductReserve reserveInfo : reserveList)
			{
				if(StringUtil.equals(reserveInfo.getProcessingFlag(), "N"))
				{
					processingNFlag = true;
					break;
				}
			}
			
			if(!processingNFlag)
			{
				// Set ReasonCode
				eventInfo.setReasonCodeType("HOLD");
				eventInfo.setReasonCode("HD100");
				eventInfo.setEventComment("DummyGlassOperationComplete,Can not Run nextOpertion");//2021/1/28 Add Comment
				// LotMultiHold
				MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, afterTrackOutLot, new HashMap<String, String>());
			}
		}
	}

	public void deleteDummyProductReserve(EventInfo eventInfo, Lot lotData)
	{
		try
		{
			String condition = " LOTNAME = ? ";
			Object[] bindSet = new Object[] { lotData.getKey().getLotName() };

			List<DummyProductReserve> dataInfoList = this.select(condition, bindSet);

			for (DummyProductReserve dataInfo : dataInfoList)
				this.delete(dataInfo);
		}
		catch (Exception e)
		{
			logger.info("DummyProductReserve Data is not exist");
		}
	}
}
