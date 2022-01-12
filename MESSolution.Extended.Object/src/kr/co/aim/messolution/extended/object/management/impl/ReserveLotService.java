package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ReserveLotService extends CTORMService<ReserveLot> {

	public static Log logger = LogFactory.getLog(ReserveLotService.class);

	private final String historyEntity = "ReserveLotHistory";

	public List<ReserveLot> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<ReserveLot> result = super.select(condition, bindSet, ReserveLot.class);

		return result;
	}

	public ReserveLot selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ReserveLot.class, isLock, keySet);
	}

	public ReserveLot create(EventInfo eventInfo, ReserveLot dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, ReserveLot dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public ReserveLot modify(EventInfo eventInfo, ReserveLot dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public ReserveLot getFirstReserveLot(String machineName) throws CustomException
	{
		try
		{
			String condition = "reserveState = ? and machineName = ? order by position";
			Object bindSet[] = new Object[] { "Reserved", machineName};
			List<ReserveLot> pLotList = ExtendedObjectProxy.getReserveLotService().select(condition, bindSet);

			return pLotList.get(0);
		}
		catch (greenFrameDBErrorSignal ne)
		{
			throw new CustomException("LOT-4000");
		}
	}

	public ReserveLot getReserveLot(String machineName, String lotName) throws CustomException
	{
		try
		{
			String condition = "machineName = ? and lotName =? and reserveState = ? order by position, reserveTimekey";
			Object bindSet[] = new Object[] { machineName, lotName, "Executing" };
			List<ReserveLot> pLotList = ExtendedObjectProxy.getReserveLotService().select(condition, bindSet);

			return pLotList.get(0);
		}
		catch (greenFrameDBErrorSignal ne)
		{
			// Not exist reserved Lot information. MachineName = [{0}], LotName = [{1}], ReserveState = [{2}]
			throw new CustomException("LOT-3012", machineName, lotName, "Executing");
		}
	}

	public ReserveLot createReserveLot(EventInfo eventInfo, String lotName, String processOperationName, String processOperationVersion, String productSpecName, String productSpecVersion,
			String machineName, String factoryName, String productRequestName) throws greenFrameDBErrorSignal, CustomException
	{
		ReserveLot dataInfo = new ReserveLot();
		dataInfo.setLotName(lotName);
		dataInfo.setMachineName(machineName);
		dataInfo.setReserveState("Created");
		dataInfo.setProductSpecName(productSpecName);
		dataInfo.setProductSpecVersion(productSpecVersion);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(processOperationVersion);
		dataInfo.setReserveTimeKey(eventInfo.getEventTimeKey());
		dataInfo.setReserveUser(eventInfo.getEventUser());
		dataInfo.setFactoryName(factoryName);
		dataInfo.setProductRequestName(productRequestName);

		ReserveLot reserveLotData = ExtendedObjectProxy.getReserveLotService().create(eventInfo, dataInfo);

		return reserveLotData;
	}

	public List<ReserveLot> getReserveLotData(String lotName, String machineName)
	{
		String condition = " LOTNAME = ? AND MACHINENAME = ? ";
		Object[] bindSet = new Object[] { lotName, machineName };

		List<ReserveLot> dataInfoList = new ArrayList<ReserveLot>();

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

	public List<ReserveLot> cancelReserveLot(EventInfo eventInfo, String lotName, String machineName) throws CustomException
	{
		List<ReserveLot> dataInfoList = this.getReserveLotData(lotName, machineName);

		if (dataInfoList != null)
		{
			for (ReserveLot dataInfo : dataInfoList)
			{
				if (!StringUtils.equals(dataInfo.getReserveState(), "Reserved"))
					throw new CustomException("LOT-0323", dataInfo.getLotName(), dataInfo.getReserveState());

				dataInfo.setPosition(0);
				dataInfo.setReserveState("Created");
				dataInfo.setMachineName("");

				this.modify(eventInfo, dataInfo);
			}
		}

		return this.getReserveLotData(lotName, machineName);
	}
}
