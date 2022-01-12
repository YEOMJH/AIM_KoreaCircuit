package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ProductQueueTime;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class ProductQueueTimeService extends CTORMService<ProductQueueTime> {

	public static Log logger = LogFactory.getLog(ProductQueueTimeService.class);

	private final String historyEntity = "ProductQueueTimeHist";

	public List<ProductQueueTime> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<ProductQueueTime> result = super.select(condition, bindSet, ProductQueueTime.class);

		return result;
	}

	public ProductQueueTime selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ProductQueueTime.class, isLock, keySet);
	}

	public ProductQueueTime create(EventInfo eventInfo, ProductQueueTime dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, ProductQueueTime dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public ProductQueueTime modifyToNew(EventInfo eventInfo, ProductQueueTime oldData, ProductQueueTime newData)
	{
		super.updateToNew(oldData, newData);

		super.addHistory(eventInfo, this.historyEntity, newData, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(newData).toArray());
	}

	public ProductQueueTime modify(EventInfo eventInfo, ProductQueueTime dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void modify(EventInfo eventInfo, List<ProductQueueTime> dataInfoList)
	{
		super.update(dataInfoList);

		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}
	
	public void moveInQTimeByProduct(EventInfo eventInfo,Product productData, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
																			  String processOperationVersion, String returnFlowName ) throws CustomException
	{
		List<ListOrderedMap> QTimePolicyList = PolicyUtil.getQTimeSpec(factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion);

		for (ListOrderedMap QtimePolicy : QTimePolicyList)
		{
			String toFactoryName = CommonUtil.getValue(QtimePolicy, "TOFACTORYNAME");
			String toProcessFlowName = CommonUtil.getValue(QtimePolicy, "TOPROCESSFLOWNAME");
			String toProcessOperationName = CommonUtil.getValue(QtimePolicy, "TOPROCESSOPERATIONNAME");
			String warningLimit = CommonUtil.getValue(QtimePolicy, "WARNINGDURATIONLIMIT");
			String interLockLimit = CommonUtil.getValue(QtimePolicy, "INTERLOCKDURATIONLIMIT");

			try
			{
				String productName = productData.getKey().getProductName();

				ProductQueueTime QtimeData = new ProductQueueTime(productName, factoryName, processFlowName, processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName);
				QtimeData.setEnterTime(eventInfo.getEventTime());
				QtimeData.setWarningDurationLimit(warningLimit);
				QtimeData.setInterlockDurationLimit(interLockLimit);
				QtimeData.setQueueTimeState(GenericServiceProxy.getConstantMap().QTIME_STATE_IN);
				QtimeData.setLotName(productData.getLotName());

				try
				{
					List<ProductQueueTime> productQTimeDataList = ExtendedObjectProxy.getProductQTimeService().select("productName = ? AND factoryName = ? AND processFlowName = ? AND processOperationName = ? AND toFactoryName = ? AND toProcessFlowName = ? AND toProcessOperationName = ? and rowNum = 1 ",
																														new Object[] { productName, factoryName, processFlowName, processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName });

					eventInfo.setEventName("Update");
					try
					{
						if (StringUtils.isEmpty(returnFlowName))
						{
							ExtendedObjectProxy.getProductQTimeService().modify(eventInfo, QtimeData);
						}
						else
						{
							if (StringUtils.equals(toProcessFlowName, returnFlowName))
							{
								ExtendedObjectProxy.getProductQTimeService().modify(eventInfo, QtimeData);
							}
							else if (toProcessFlowName.equals(processFlowName))
							{
								ExtendedObjectProxy.getProductQTimeService().modify(eventInfo, QtimeData);
							}
						}
					}
					catch (greenFrameDBErrorSignal ne)
					{
						throw new CustomException("LOT-0201", productName, processOperationName, toProcessOperationName);
					}
				}
				catch (greenFrameDBErrorSignal ne)
				{
					eventInfo.setEventName("Enter");

					try
					{
						if (StringUtils.isEmpty(returnFlowName))
						{
							ExtendedObjectProxy.getProductQTimeService().create(eventInfo, QtimeData);
						}
						else
						{
							if (StringUtils.equals(toProcessFlowName, returnFlowName))
							{
								ExtendedObjectProxy.getProductQTimeService().create(eventInfo, QtimeData);
							}
							else if (StringUtils.equals(toProcessFlowName, processFlowName))
							{
								ExtendedObjectProxy.getProductQTimeService().create(eventInfo, QtimeData);
							}
						}
					}
					catch (greenFrameDBErrorSignal ne1)
					{
						// ignore error to consider as duplicated exception signal
						throw new CustomException("LOT-0202", productName, processOperationName, toProcessOperationName);
					}
				}
			}
			catch (Exception ex)
			{
				// Q-time process is optional
				if (logger.isWarnEnabled())
					logger.warn(String.format("Q-time IN process for Product[%s] to Operation[%s] is failed at Operation[%s]", productData.getKey().getProductName(), toProcessOperationName, processOperationName));
			}
		}
	}
	
	public List<ProductQueueTime> findOverProductQTimeByLot(String lotName) throws CustomException
	{
		List<ProductQueueTime> resultList;

		try
		{
			resultList = ExtendedObjectProxy.getProductQTimeService().select("productName in (select productName from product where lotname = ?) AND queueTimeState <> ? ",
					new Object[] { lotName, GenericServiceProxy.getConstantMap().QTIME_STATE_OVER });
		}
		catch (Exception ex)
		{
			resultList = new ArrayList<ProductQueueTime>();
		}

		return resultList;
	}

	public boolean isInQTime(String productName)
	{
		boolean result = false;

		try
		{
			List<ProductQueueTime> resultList = ExtendedObjectProxy.getProductQTimeService().select("productName = ?", new Object[] { productName });

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

	public String doProductQTimeAction(Document doc, String lotName) throws CustomException
	{
		String reworkFlag = GenericServiceProxy.getConstantMap().Flag_N;
		List<ProductQueueTime> QTimeDataList = findProductQTimeByLot(lotName);

		String resultType = "";

		for (ProductQueueTime QTimeData : QTimeDataList)
		{
			resultType = ExtendedObjectProxy.getProductQTimeService().doActionQTime(QTimeData);

			// Q-time interval is only one by structure
			if (!StringUtil.isEmpty(resultType))
				break;
		}

		if (resultType.equalsIgnoreCase("hold"))
		{

		}
		else if (resultType.equalsIgnoreCase("rework"))
		{
			// compatible for makeLoggedOut API
			// not allowed Lot already in rework
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

			if (lotData.getReworkState().equals(GenericServiceProxy.getConstantMap().Lot_NotInRework))
			{
				reworkFlag = GenericServiceProxy.getConstantMap().Flag_Y;
			}
		}

		return reworkFlag;
	}

	public List<ProductQueueTime> findProductQTimeByLot(String lotName) throws CustomException
	{
		List<ProductQueueTime> resultList;

		try
		{
			resultList = ExtendedObjectProxy.getProductQTimeService().select("productName in (select productName from product where lotname = ?) AND queueTimeState <> ? ",
					new Object[] { lotName, GenericServiceProxy.getConstantMap().QTIME_STATE_CONFIRM });
		}
		catch (Exception ex)
		{
			resultList = new ArrayList<ProductQueueTime>();
		}

		return resultList;
	}
	
	public List<ProductQueueTime> findProductQTimeByFromOper(String lotName,String processFlowName,String processOperationName) throws CustomException
	{
		List<ProductQueueTime> resultList;

		try
		{
			resultList = ExtendedObjectProxy.getProductQTimeService().select("productName in (select productName from product where lotname = ? and productgrade = 'R') AND queueTimeState in (?,?) AND  processFlowName = ? AND processOperationName = ?",
					new Object[] { lotName, GenericServiceProxy.getConstantMap().QTIME_STATE_IN, GenericServiceProxy.getConstantMap().QTIME_STATE_WARN, processFlowName, processOperationName });
		}
		catch (Exception ex)
		{
			resultList = new ArrayList<ProductQueueTime>();
		}

		return resultList;
	}

	public List<ProductQueueTime> getProductQueueTimeData(String productName)
	{
		List<ProductQueueTime> resultList = new ArrayList<ProductQueueTime>();

		String condition = " PRODUCTNAME = ? ";
		Object[] bindSet = new Object[] { productName };

		try
		{
			resultList = ExtendedObjectProxy.getProductQTimeService().select(condition, bindSet);
		}
		catch (Exception ex)
		{
			resultList = null;
		}

		return resultList;
	}

	public List<ProductQueueTime> findQTimeByLot(String lotName) throws CustomException
	{
		List<ProductQueueTime> resultList;

		try
		{
			resultList = ExtendedObjectProxy.getProductQTimeService().select("productName in (select productName from product where lotName = ?) AND queueTimeState <> ?",
					new Object[] { lotName, GenericServiceProxy.getConstantMap().QTIME_STATE_CONFIRM });
		}
		catch (Exception ex)
		{
			resultList = new ArrayList<ProductQueueTime>();
		}

		return resultList;
	}

	public List<ProductQueueTime> findQTimeByProduct(String productName) throws CustomException
	{
		List<ProductQueueTime> resultList;

		try
		{
			resultList = ExtendedObjectProxy.getProductQTimeService().select("productName = ? AND queueTimeState <> ?",
					new Object[] { productName, GenericServiceProxy.getConstantMap().QTIME_STATE_CONFIRM });
		}
		catch (Exception ex)
		{
			resultList = new ArrayList<ProductQueueTime>();
		}

		return resultList;
	}

	public void monitorProductQTime(EventInfo eventInfo, String lotName, String machineName) throws CustomException
	{
		List<ProductQueueTime> QTimeDataList = findProductQTimeByLot(lotName);

		for (ProductQueueTime QTimeData : QTimeDataList)
		{
			try
			{
				GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);

				// in form of millisecond
				Double expired = ConvertUtil.getDiffTime(TimeUtils.toTimeString(QTimeData.getEnterTime(), TimeStampUtil.FORMAT_TIMEKEY), TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY));
				// // limit unit is hour and decimal form
				expired = (expired / 60 / 60);

				if (Double.compare(expired, Double.parseDouble(QTimeData.getInterlockDurationLimit())) >= 0
						&& (QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_WARN) || QTimeData.getQueueTimeState().equals(
								GenericServiceProxy.getConstantMap().QTIME_STATE_IN)))
				{
					lockQTime(eventInfo, QTimeData.getProductName(), QTimeData.getFactoryName(), QTimeData.getProcessFlowName(), QTimeData.getProcessOperationName(), QTimeData.getToFactoryName(),
							QTimeData.getToProcessFlowName(), QTimeData.getToProcessOperationName());

					String message = "<pre>===============AlarmInformation===============</pre>";
					message += "<pre>==============================================</pre>";
					message += "<pre>- EventName	: " + eventInfo.getEventName() + "</pre>";
					message += "<pre>- FactoryName	: " + QTimeData.getFactoryName() + "</pre>";
					message += "<pre>- ProductName	: " + QTimeData.getProductName() + "</pre>";
					message += "<pre>- FlowName	: " + QTimeData.getProcessFlowName() + "</pre>";
					message += "<pre>- OperationName	: " + QTimeData.getProcessOperationName() + "</pre>";
					message += "<pre>- ToFactoryName	: " + QTimeData.getToFactoryName() + "</pre>";
					message += "<pre>- ToFlowName	: " + QTimeData.getToProcessFlowName() + "</pre>";
					message += "<pre>- ToOperationName	: " + QTimeData.getToProcessOperationName() + "</pre>";
					message += "<pre>==============================================</pre>";

					CommonUtil.sendAlarmEmailForFactoryAndEQP(machineName, QTimeData.getToFactoryName(), "Q-TIME", message);
				}

				else if (Double.compare(expired, Double.parseDouble(QTimeData.getWarningDurationLimit())) >= 0
						&& QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_IN))
				{
					warnQTime(eventInfo, QTimeData.getProductName(), QTimeData.getFactoryName(), QTimeData.getProcessFlowName(), QTimeData.getProcessOperationName(), QTimeData.getToFactoryName(),
							QTimeData.getToProcessFlowName(), QTimeData.getToProcessOperationName());
				}

				GenericServiceProxy.getTxDataSourceManager().commitTransaction();

			}
			catch (Exception ex)
			{
				GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();

				logger.warn(String.format("Q-Time[%s %s %s] monitoring failed", QTimeData.getProductName(), QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName()));
			}
		}
	}

	public Lot monitorProductQTimeByChangeProductSpec(EventInfo eventInfo, List<ProductQueueTime> QTimeDataList, Lot lotData) throws CustomException
	{
		for (ProductQueueTime QTimeData : QTimeDataList)
		{
			try
			{
				GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
				
				// in form of millisecond
				Double expired = ConvertUtil.getDiffTime(TimeUtils.toTimeString(QTimeData.getEnterTime(), TimeStampUtil.FORMAT_TIMEKEY), TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY));
				// // limit unit is hour and decimal form
				expired = (expired / 60 / 60);

				if (Double.compare(expired, Double.parseDouble(QTimeData.getInterlockDurationLimit())) >= 0
						&& (QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_WARN) || QTimeData.getQueueTimeState().equals(
								GenericServiceProxy.getConstantMap().QTIME_STATE_IN)))
				{
					lockQTime(eventInfo, QTimeData.getProductName(), QTimeData.getFactoryName(), QTimeData.getProcessFlowName(), QTimeData.getProcessOperationName(), QTimeData.getToFactoryName(),
							QTimeData.getToProcessFlowName(), QTimeData.getToProcessOperationName());
				}
				else if (Double.compare(expired, Double.parseDouble(QTimeData.getWarningDurationLimit())) >= 0
						&& QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_IN))
				{
					warnQTime(eventInfo, QTimeData.getProductName(), QTimeData.getFactoryName(), QTimeData.getProcessFlowName(), QTimeData.getProcessOperationName(), QTimeData.getToFactoryName(),
							QTimeData.getToProcessFlowName(), QTimeData.getToProcessOperationName());
				}
				
				GenericServiceProxy.getTxDataSourceManager().commitTransaction();
			}
			catch (Exception ex)
			{
				GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
				logger.warn(String.format("Q-Time[%s %s %s] monitoring failed", QTimeData.getProductName(), QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName()));
			}
		}
		
//		List<ProductQueueTime> interLockList = new ArrayList<ProductQueueTime>();
//
//		String condition = " PRODUCTNAME IN (SELECT PRODUCTNAME FROM PRODUCT WHERE LOTNAME = ?) AND QUEUETIMESTATE = ? ";
//		Object[] bindSet = new Object[]{ lotData.getKey().getLotName(), GenericServiceProxy.getConstantMap().QTIME_STATE_OVER };
//		
//		try
//		{
//			interLockList = ExtendedObjectProxy.getProductQTimeService().select(condition, bindSet);
//		}
//		catch (Exception ex)
//		{
//			logger.info(String.format("HoldWhenQTimeOver: QTimeDataList is Empty!! Condition By:[LotName=%s]", lotData.getKey().getLotName()));
//		}
//		
//		if (interLockList != null && interLockList.size() > 0)
//		{
//			eventInfo = EventInfoUtil.makeEventInfo("ChangeProductSpec", eventInfo.getEventUser(), eventInfo.getEventComment(), "ChangeProductSpec", "RH-ExceedQtime");
//			MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, lotData, new HashMap<String, String>());
//		}
		
		lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getKey().getLotName());
		
		return lotData;
	}

	public void updateQTimeDataByChangeProductSpec(EventInfo eventInfo, List<ProductQueueTime> qTimeDataList, Lot lotData, Lot oldLotData) throws CustomException
	{
		for (ProductQueueTime qTimeData : qTimeDataList)
		{
			String processOperationName = qTimeData.getProcessOperationName();

			List<Map<String, Object>> adjustPolicy = getAdjustPolicyList(lotData, oldLotData, processOperationName);

			if (adjustPolicy.size() > 0)
			{
				String toProcessFlowName = ConvertUtil.getMapValueByName(adjustPolicy.get(0), "TOPROCESSFLOWNAME");
				String toProcessOperationName = ConvertUtil.getMapValueByName(adjustPolicy.get(0), "TOPROCESSOPERATIONNAME");
				String changeProcessFlowName = ConvertUtil.getMapValueByName(adjustPolicy.get(0), "CHANGEPROCESSFLOWNAME");
				String changeProcessOperationName = ConvertUtil.getMapValueByName(adjustPolicy.get(0), "CHANGEPROCESSOPERATIONNAME");
				String warningDurationLimit = ConvertUtil.getMapValueByName(adjustPolicy.get(0), "WARNINGDURATIONLIMIT");
				String interlockDurationLimit = ConvertUtil.getMapValueByName(adjustPolicy.get(0), "INTERLOCKDURATIONLIMIT");
				
				// Modify QtimeData
				ProductQueueTime oldQTimeData = (ProductQueueTime) ObjectUtil.copyTo(qTimeData);
				
				qTimeData.setProcessFlowName(toProcessFlowName);
				qTimeData.setProcessOperationName(toProcessOperationName);
				qTimeData.setToProcessFlowName(changeProcessFlowName);
				qTimeData.setToProcessOperationName(changeProcessOperationName);
				qTimeData.setWarningDurationLimit(warningDurationLimit);
				qTimeData.setInterlockDurationLimit(interlockDurationLimit);
				
				ExtendedObjectProxy.getProductQTimeService().modifyToNew(eventInfo, oldQTimeData, qTimeData);
			}
		}
		
		Lot newlotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getKey().getLotName());
		List<ProductQueueTime> newQTimeDataList = ExtendedObjectProxy.getProductQTimeService().findProductQTimeByLot(newlotData.getKey().getLotName());
		
		monitorProductQTimeByChangeProductSpec(eventInfo, newQTimeDataList, newlotData);
	}
	
	public List<Map<String, Object>> getAdjustPolicyList(Lot lotData, Lot oldLotData, String toProcessOperationName)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT P.TOFACTORYNAME, ");
		sql.append("       P.TOPROCESSFLOWNAME, ");
		sql.append("       P.TOPROCESSFLOWVERSION, ");
		sql.append("       P.TOPROCESSOPERATIONNAME, ");
		sql.append("       P.TOPROCESSOPERATIONVERSION, ");
		sql.append("       P.CHANGEPROCESSFLOWNAME, ");
		sql.append("       P.CHANGEPROCESSFLOWVERSION, ");
		sql.append("       P.CHANGEPROCESSOPERATIONNAME, ");
		sql.append("       P.CHANGEPROCESSOPERATIONVERSION, ");
		sql.append("       P.WARNINGDURATIONLIMIT, ");
		sql.append("       P.INTERLOCKDURATIONLIMIT ");
		sql.append("  FROM TFOPOLICY T, POSADJUSTQUEUETIME P ");
		sql.append(" WHERE T.CONDITIONID = P.CONDITIONID ");
		sql.append("   AND T.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		sql.append("   AND P.TOFACTORYNAME = :TOFACTORYNAME ");
		sql.append("   AND P.TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME ");
		sql.append("   AND P.TOPROCESSFLOWVERSION = :TOPROCESSFLOWVERSION ");
		sql.append("   AND P.TOPROCESSOPERATIONNAME = :TOPROCESSOPERATIONNAME ");
		sql.append("   AND P.TOPROCESSOPERATIONVERSION = :TOPROCESSOPERATIONVERSION ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("FACTORYNAME", oldLotData.getFactoryName());
		args.put("PROCESSFLOWNAME", oldLotData.getProcessFlowName());
		args.put("PROCESSFLOWVERSION", oldLotData.getProcessFlowVersion());
		args.put("PROCESSOPERATIONNAME", oldLotData.getProcessOperationName());
		args.put("PROCESSOPERATIONVERSION", oldLotData.getProcessOperationVersion());
		args.put("TOFACTORYNAME", lotData.getFactoryName());
		args.put("TOPROCESSFLOWNAME", lotData.getProcessFlowName());
		args.put("TOPROCESSFLOWVERSION", "00001");
		args.put("TOPROCESSOPERATIONNAME", toProcessOperationName);
		args.put("TOPROCESSOPERATIONVERSION", "00001");
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		
		return sqlResult;
	} 
	
	public void monitorQTime(EventInfo eventInfo, String productName) throws CustomException
	{
		List<ProductQueueTime> QTimeDataList = findQTimeByProduct(productName);

		for (ProductQueueTime QTimeData : QTimeDataList)
		{
			try
			{
				GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);

				// in form of millisecond
				Double expired = ConvertUtil.getDiffTime(TimeUtils.toTimeString(QTimeData.getEnterTime(), TimeStampUtil.FORMAT_TIMEKEY), TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY));
				// // limit unit is hour and decimal form
				expired = (expired / 60 / 60);

				if (Double.compare(expired, Double.parseDouble(QTimeData.getInterlockDurationLimit())) >= 0
						&& (QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_WARN) || QTimeData.getQueueTimeState().equals(
								GenericServiceProxy.getConstantMap().QTIME_STATE_IN)))
				{
					lockQTime(eventInfo, QTimeData.getProductName(), QTimeData.getFactoryName(), QTimeData.getProcessFlowName(), QTimeData.getProcessOperationName(), QTimeData.getToFactoryName(),
							QTimeData.getToProcessFlowName(), QTimeData.getToProcessOperationName());
				}

				else if (Double.compare(expired, Double.parseDouble(QTimeData.getWarningDurationLimit())) >= 0
						&& QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_IN))
				{
					warnQTime(eventInfo, QTimeData.getProductName(), QTimeData.getFactoryName(), QTimeData.getProcessFlowName(), QTimeData.getProcessOperationName(), QTimeData.getToFactoryName(),
							QTimeData.getToProcessFlowName(), QTimeData.getToProcessOperationName());
				}

				GenericServiceProxy.getTxDataSourceManager().commitTransaction();

			}
			catch (Exception ex)
			{
				GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();

				logger.warn(String.format("Q-Time[%s %s %s] monitoring failed", QTimeData.getProductName(), QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName()));
			}
		}
	}

	public void reserveHoldWhenQTimeOver(Lot lotData, EventInfo eventInfo, String reasonCode, String reasonCodeType, String reasonComment)
	{

		List<ProductQueueTime> QTimeDataList = null;

		try
		{
			QTimeDataList = ExtendedObjectProxy.getProductQTimeService().select(
					" productName in (select productName from product where lotName = ?) AND queueTimeState = ?" + " AND toFactoryName = ? AND toProcessFlowName = ? AND toProcessOperationName = ? ",
					new Object[] { lotData.getKey().getLotName(), GenericServiceProxy.getConstantMap().QTIME_STATE_OVER, lotData.getFactoryName(), lotData.getProcessFlowName(),
							lotData.getProcessOperationName() });
		}
		catch (Exception ex)
		{
			logger.info(String.format("reserveHoldWhenQTimeOver: QTimeDataList is Empty!! Condition By:[LotName=%s,T=%s,F=%s,O=%s]", lotData.getKey().getLotName(), lotData.getFactoryName(),
					lotData.getProcessFlowName(), lotData.getProcessOperationName()));

			return;
		}

		if (QTimeDataList != null && QTimeDataList.size() > 0)
		{
			try
			{
				MESLotServiceProxy.getLotServiceImpl().insertCtLotFutureMultiHoldActionForAfter(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProcessFlowName(),
						lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), "0", "hold", "System", reasonCodeType, reasonCode, "", "", "",
						"False", "True", "", reasonComment, "", eventInfo.getEventUser(), "Insert", "", "");
			}
			catch (Exception e)
			{
				logger.info("reserveHoldWhenQTimeOver:Reserve Hold Information is already exists.");
			}

		}

	}

	public void processQTimeByLot(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, String returnFlowName) throws CustomException
	{
		moveInQTimeByLot(eventInfo, lotName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, returnFlowName);
		exitQTimeByLot(eventInfo, lotName, factoryName, processFlowName, processOperationName);
	}

	public void moveInQTimeByLot(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, String returnFlowName) throws CustomException
	{
		List<ListOrderedMap> QTimePolicyList = PolicyUtil.getQTimeSpec(factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion);

		for (ListOrderedMap QtimePolicy : QTimePolicyList)
		{
			String toFactoryName = CommonUtil.getValue(QtimePolicy, "TOFACTORYNAME");
			String toProcessFlowName = CommonUtil.getValue(QtimePolicy, "TOPROCESSFLOWNAME");
			String toProcessOperationName = CommonUtil.getValue(QtimePolicy, "TOPROCESSOPERATIONNAME");
			String warningLimit = CommonUtil.getValue(QtimePolicy, "WARNINGDURATIONLIMIT");
			String interLockLimit = CommonUtil.getValue(QtimePolicy, "INTERLOCKDURATIONLIMIT");

			try
			{
				// enter into Q time
				moveInQTimeByLot(eventInfo, lotName, factoryName, processFlowName, processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName, warningLimit, interLockLimit,
						returnFlowName);
			}
			catch (Exception ex)
			{
				// Q-time process is optional
				if (logger.isWarnEnabled())
					logger.warn(String.format("Q-time IN process for Lot[%s] to Operation[%s] is failed at Operation[%s]", lotName, toProcessOperationName, processOperationName));
			}
		}
	}

	public void exitQTimeByLot(EventInfo eventInfo, String lotName, String toFactoryName, String toProcessFlowName, String toProcessOperationName) throws CustomException
	{
		List<ProductQueueTime> QTimeDataList;

		try
		{
			QTimeDataList = ExtendedObjectProxy.getProductQTimeService().select(
					"productName in (select productName from product where lotName = ?) AND toFactoryName = ? AND toProcessFlowName = ? AND toProcessOperationName = ? ",
					new Object[] { lotName, toFactoryName, toProcessFlowName, toProcessOperationName });
		}
		catch (Exception ex)
		{
			QTimeDataList = new ArrayList<ProductQueueTime>();
		}

		for (ProductQueueTime QTimeData : QTimeDataList)
		{
			try
			{
				// enter into Q time
				updateQTimeToExit(eventInfo, QTimeData);
			}
			catch (Exception ex)
			{
				// Q-time process is optional
				if (logger.isWarnEnabled())
				{
					logger.warn(String.format("Q-time OUT process for Product[%s] to Operation[%s] is failed at Operation[%s]", QTimeData.getProductName(), QTimeData.getToProcessOperationName(),
							QTimeData.getProcessOperationName()));
				}
			}
		}
	}

	public void exitQTime(EventInfo eventInfo, String productName, String toFactoryName, String toProcessFlowName, String toProcessOperationName) throws CustomException
	{
		List<ProductQueueTime> QTimeDataList;

		try
		{
			QTimeDataList = ExtendedObjectProxy.getProductQTimeService().select("productName = ? AND toFactoryName = ? AND toProcessFlowName = ? AND toProcessOperationName = ?",
					new Object[] { productName, toFactoryName, toProcessFlowName, toProcessOperationName });
		}
		catch (Exception ex)
		{
			QTimeDataList = new ArrayList<ProductQueueTime>();
		}

		for (ProductQueueTime QTimeData : QTimeDataList)
		{
			try
			{
				// enter into Q time
				exitQTime(eventInfo, QTimeData);
			}
			catch (Exception ex)
			{
				// Q-time process is optional
				if (logger.isWarnEnabled())
					logger.warn(String.format("Q-time OUT process for Product[%s] to Operation[%s] is failed at Operation[%s]", QTimeData.getProductName(), QTimeData.getToProcessOperationName(),
							QTimeData.getProcessOperationName()));
			}
		}
	}

	public void validateQTimeByLot(String lotName) throws CustomException
	{
		List<ProductQueueTime> QTimeDataList = findQTimeByLot(lotName);

		for (ProductQueueTime QTimeData : QTimeDataList)
		{
			validateQTime(QTimeData);
		}
	}

	public void validateQTime(String productName) throws CustomException
	{
		List<ProductQueueTime> QTimeDataList = findQTimeByProduct(productName);

		for (ProductQueueTime QTimeData : QTimeDataList)
		{
			validateQTime(QTimeData);
		}
	}

	public void validateQTime(ProductQueueTime QTimeData) throws CustomException
	{
		// compare
		if (QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_OVER))
		{
			// interlock action prior to log-in
			throw new CustomException("LOT-0203", QTimeData.getProductName(), QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName());
		}
		else if (QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_WARN))
		{
			// interlock action prior to log-in
			logger.warn(String.format("Q-Time[%s %s %s] would be expired soon", QTimeData.getProductName(), QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName()));
		}
	}

	public String doActionQTime(ProductQueueTime QTimeData) throws CustomException
	{
		if (QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_OVER))
		{
			// do interlock action
			try
			{
				return "REWORK";
			}
			catch (Exception ex)
			{
				logger.warn(String.format("Q-Time[%s %s %s] interlock action failed", QTimeData.getProductName(), QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName()));
			}
		}
		else if (QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_WARN))
		{
			// do warning action
			logger.warn(String.format("Q-Time[%s %s %s] is warning to interlock", QTimeData.getProductName(), QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName()));

			return "HOLD";
		}

		return "";
	}

	public void resolveQTime(EventInfo eventInfo, String productName, String factoryName, String processFlowName, String processOperationName, String toFactoryName, String toProcessFlowName,
			String toProcessOperationName, String reasonCode,String department) throws CustomException
	{
		try
		{
			eventInfo.setEventName("Resolve");

			ProductQueueTime QtimeData = ExtendedObjectProxy.getProductQTimeService().selectByKey(true,
					new Object[] { productName, factoryName, processFlowName, processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName });
			QtimeData.setResolveTime(eventInfo.getEventTime());
			QtimeData.setResolveUser(eventInfo.getEventUser());
			QtimeData.setQueueTimeState(GenericServiceProxy.getConstantMap().QTIME_STATE_CONFIRM);
			QtimeData.setReasonCode(reasonCode);
			QtimeData.setDepartment(department);

			ExtendedObjectProxy.getProductQTimeService().modify(eventInfo, QtimeData);
		}
		catch (greenFrameDBErrorSignal ne)
		{
			// ignore error to consider as not found exception signal
			throw new CustomException("LOT-0201", productName, processOperationName, toProcessOperationName);
		}
	}

	public void exitQTime(EventInfo eventInfo, ProductQueueTime QTimeData) throws CustomException
	{
		try
		{
			eventInfo.setEventName("Remove");

			QTimeData.setExitTime(eventInfo.getEventTime());
			ExtendedObjectProxy.getProductQTimeService().remove(eventInfo, QTimeData);
		}
		catch (greenFrameDBErrorSignal ne)
		{
			// ignore error to consider as not found exception signal
			throw new CustomException("LOT-0201", QTimeData.getProductName(), QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName());
		}
	}

	public void moveInQTimeByLot(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processOperationName, String toFactoryName, String toProcessFlowName,
			String toProcessOperationName, String warningLimit, String interLockLimit, String returnFlowName) throws CustomException
	{
		List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);

		for (Product productData : productList)
		{
			String productName = productData.getKey().getProductName();

			ProductQueueTime QtimeData = new ProductQueueTime(productName, factoryName, processFlowName, processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName);
			QtimeData.setEnterTime(eventInfo.getEventTime());
			QtimeData.setWarningDurationLimit(warningLimit);
			QtimeData.setInterlockDurationLimit(interLockLimit);
			QtimeData.setQueueTimeState(GenericServiceProxy.getConstantMap().QTIME_STATE_IN);
			QtimeData.setLotName(lotName);

			try
			{
				List<ProductQueueTime> productQTimeDataList = ExtendedObjectProxy
						.getProductQTimeService()
						.select("productName = ? AND factoryName = ? AND processFlowName = ? AND processOperationName = ? AND toFactoryName = ? AND toProcessFlowName = ? AND toProcessOperationName = ? and rowNum = 1 ",
								new Object[] { productName, factoryName, processFlowName, processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName });

				eventInfo.setEventName("Update");
				try
				{
					if (StringUtils.isEmpty(returnFlowName))
					{
						ExtendedObjectProxy.getProductQTimeService().modify(eventInfo, QtimeData);
					}
					else
					{
						if (StringUtils.equals(toProcessFlowName, returnFlowName))
						{
							ExtendedObjectProxy.getProductQTimeService().modify(eventInfo, QtimeData);
						}
						if (toProcessFlowName.equals(processFlowName))
						{
							ExtendedObjectProxy.getProductQTimeService().modify(eventInfo, QtimeData);
						}
					}
				}
				catch (greenFrameDBErrorSignal ne)
				{
					throw new CustomException("LOT-0201", productName, processOperationName, toProcessOperationName);
				}
			}
			catch (greenFrameDBErrorSignal ne)
			{
				eventInfo.setEventName("Enter");

				try
				{
					if (StringUtils.isEmpty(returnFlowName))
					{
						ExtendedObjectProxy.getProductQTimeService().create(eventInfo, QtimeData);
					}
					else
					{
						if (StringUtils.equals(toProcessFlowName, returnFlowName))
						{
							ExtendedObjectProxy.getProductQTimeService().create(eventInfo, QtimeData);
						}
						if (StringUtils.equals(toProcessFlowName, processFlowName))
						{
							ExtendedObjectProxy.getProductQTimeService().create(eventInfo, QtimeData);
						}
					}
				}
				catch (greenFrameDBErrorSignal ne1)
				{
					// ignore error to consider as duplicated exception signal
					throw new CustomException("LOT-0202", productName, processOperationName, toProcessOperationName);
				}
			}
		}
	}

	public void moveInQTime(EventInfo eventInfo, String productName, String factoryName, String processFlowName, String processOperationName, String toFactoryName, String toProcessFlowName,
			String toProcessOperationName, String warningLimit, String interLockLimit, boolean dummyFlag) throws CustomException
	{
		ProductQueueTime QtimeData = new ProductQueueTime(productName, factoryName, processFlowName, processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName);

		QtimeData.setEnterTime(eventInfo.getEventTime());
		QtimeData.setWarningDurationLimit(warningLimit);
		QtimeData.setInterlockDurationLimit(interLockLimit);
		QtimeData.setQueueTimeState(GenericServiceProxy.getConstantMap().QTIME_STATE_IN);
		Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);

		Map<String, String> udfs = productData.getUdfs();

		try
		{
			List<ProductQueueTime> productQTimeDataList = ExtendedObjectProxy.getProductQTimeService().select(
					"productName = ? AND factoryName = ? AND processFlowName = ? AND processOperationName = ? AND toFactoryName = ? AND toProcessFlowName = ? AND toProcessOperationName = ?",
					new Object[] { productName, factoryName, processFlowName, processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName });

			eventInfo.setEventName("Update");
			try
			{

				if (udfs.get("RETURNFLOWNAME").equals(""))
				{
					ExtendedObjectProxy.getProductQTimeService().modify(eventInfo, QtimeData);
				}

				else
				{
					if (toProcessFlowName.equals(udfs.get("RETURNFLOWNAME")))
					{
						ExtendedObjectProxy.getProductQTimeService().modify(eventInfo, QtimeData);
					}
					if (toProcessFlowName.equals(processFlowName))
					{
						ExtendedObjectProxy.getProductQTimeService().modify(eventInfo, QtimeData);
					}
				}

			}
			catch (greenFrameDBErrorSignal ne)
			{
				throw new CustomException("LOT-0201", productName, processOperationName, toProcessOperationName);
			}
		}
		catch (greenFrameDBErrorSignal ne)
		{
			eventInfo.setEventName("Enter");

			try
			{
				if (udfs.get("RETURNFLOWNAME").equals(""))
				{
					ExtendedObjectProxy.getProductQTimeService().create(eventInfo, QtimeData);
				}

				else
				{
					if (toProcessFlowName.equals(udfs.get("RETURNFLOWNAME")))
					{
						ExtendedObjectProxy.getProductQTimeService().create(eventInfo, QtimeData);
					}
					if (toProcessFlowName.equals(processFlowName))
					{
						ExtendedObjectProxy.getProductQTimeService().create(eventInfo, QtimeData);
					}
				}
			}
			catch (greenFrameDBErrorSignal ne1)
			{
				// ignore error to consider as duplicated exception signal
				throw new CustomException("LOT-0202", productName, processOperationName, toProcessOperationName);
			}
		}
	}

	public void warnQTime(EventInfo eventInfo, String productName, String factoryName, String processFlowName, String processOperationName, String toFactoryName, String toProcessFlowName,
			String toProcessOperationName) throws CustomException
	{
		try
		{
			eventInfo.setEventName("Warn");

			ProductQueueTime QtimeData = ExtendedObjectProxy.getProductQTimeService().selectByKey(true,
					new Object[] { productName, factoryName, processFlowName, processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName });
			QtimeData.setWarningTime(eventInfo.getEventTime());
			QtimeData.setQueueTimeState(GenericServiceProxy.getConstantMap().QTIME_STATE_WARN);

			ExtendedObjectProxy.getProductQTimeService().modify(eventInfo, QtimeData);
		}
		catch (greenFrameDBErrorSignal ne)
		{
			// ignore error to consider as not found exception signal
			throw new CustomException("LOT-0201", productName, processOperationName, toProcessOperationName);
		}
	}

	public void lockQTime(EventInfo eventInfo, String productName, String factoryName, String processFlowName, String processOperationName, String toFactoryName, String toProcessFlowName,
			String toProcessOperationName) throws CustomException
	{
		try
		{
			eventInfo.setEventName("Interlock");

			ProductQueueTime QtimeData = ExtendedObjectProxy.getProductQTimeService().selectByKey(true,
					new Object[] { productName, factoryName, processFlowName, processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName });
			QtimeData.setInterlockTime(eventInfo.getEventTime());
			QtimeData.setQueueTimeState(GenericServiceProxy.getConstantMap().QTIME_STATE_OVER);

			ExtendedObjectProxy.getProductQTimeService().modify(eventInfo, QtimeData);
		}
		catch (greenFrameDBErrorSignal ne)
		{
			// ignore error to consider as not found exception signal
			throw new CustomException("LOT-0201", productName, processOperationName, toProcessOperationName);
		}
	}

	public void checkPcellQtime(Product productData) throws CustomException
	{
		List<ListOrderedMap> QTimePolicyList = PolicyUtil.getQTimeSpec(productData.getFactoryName(), productData.getProcessFlowName(), productData.getProcessFlowVersion(),
				productData.getProcessOperationName(), productData.getProcessOperationVersion());

		for (ListOrderedMap QTimePolicy : QTimePolicyList)
		{
			String processOperationName = CommonUtil.getValue(QTimePolicy, "PROCESSOPERATIONNAME");
			String toFactoryName = CommonUtil.getValue(QTimePolicy, "TOFACTORYNAME");
			String toProcessFlowName = CommonUtil.getValue(QTimePolicy, "TOPROCESSFLOWNAME");
			String toProcessOperationName = CommonUtil.getValue(QTimePolicy, "TOPROCESSOPERATIONNAME");
			String interLockLimit = CommonUtil.getValue(QTimePolicy, "INTERLOCKDURATIONLIMIT");

			if (interLockLimit.isEmpty())
				return;

			// TODO: Check toFactory&toProcessFlow
			if (StringUtil.equals(processOperationName, toProcessOperationName))
			{
				Double expired = ConvertUtil.getDiffTime(TimeUtils.toTimeString(productData.getLastProcessingTime(), TimeStampUtil.FORMAT_TIMEKEY),
						TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY));

				// CurrentTime - TrackInTime
				expired = (expired / 60);

				if (Double.compare(expired, Double.parseDouble(interLockLimit)) >= 0)
				{
					throw new CustomException("LOT-0204", productData.getKey().getProductName(), TimeUtils.toTimeString(productData.getLastProcessingTime(), TimeStampUtil.FORMAT_TIMEKEY),
							interLockLimit);
				}
			}
		}
	}

	public void cloneQTime(EventInfo eventInfo, String newLotName)
	{
		try
		{
			List<ProductQueueTime> newQTimeDataList = new ArrayList<ProductQueueTime>();
			List<ProductQueueTime> QTimeDataList = findQTimeByLot(newLotName);

			for (ProductQueueTime QTimeData : QTimeDataList)
			{
				QTimeData.setLotName(newLotName);
				newQTimeDataList.add(QTimeData);
			}

			if (newQTimeDataList.size() > 0)
			{
				modify(eventInfo, newQTimeDataList);
			}
		}
		catch (Exception ex)
		{
			logger.warn("Clone Q-Time Error!");
		}
	}

	public void cloneQTime(EventInfo eventInfo, String sourceLotName, List<String> destLotNameList)
	{
		try
		{
			List<ProductQueueTime> QTimeDataList = findQTimeByLot(sourceLotName);

			for (ProductQueueTime QTimeData : QTimeDataList)
			{
				for (String destLotName : destLotNameList)
				{
					QTimeData.setLotName(destLotName);

					try
					{
						ExtendedObjectProxy.getProductQTimeService().modify(eventInfo, QTimeData);
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

	public void mergeQTime(EventInfo eventInfo, String destProductName, List<String> sourceProductNameList)
	{
		try
		{
			// Get oldest QTime
			StringBuffer sqlBuffer = new StringBuffer();
			sqlBuffer.append(" WITH MINQTIME ");
			sqlBuffer.append("      AS (  SELECT Q.FACTORYNAME, ");
			sqlBuffer.append("                   Q.PROCESSFLOWNAME, ");
			sqlBuffer.append("                   Q.PROCESSOPERATIONNAME, ");
			sqlBuffer.append("                   Q.TOFACTORYNAME, ");
			sqlBuffer.append("                   Q.TOPROCESSFLOWNAME, ");
			sqlBuffer.append("                   Q.TOPROCESSOPERATIONNAME, ");
			sqlBuffer.append("                   MIN (Q.ENTERTIME) AS ENTERTIME ");
			sqlBuffer.append("              FROM CT_ProductQueueTime Q ");
			sqlBuffer.append("              WHERE Q.LOTNAME IN (:LOTNAMELIST) ");
			sqlBuffer.append("          GROUP BY Q.FACTORYNAME, ");
			sqlBuffer.append("                   Q.PROCESSFLOWNAME, ");
			sqlBuffer.append("                   Q.PROCESSOPERATIONNAME, ");
			sqlBuffer.append("                   Q.TOFACTORYNAME, ");
			sqlBuffer.append("                   Q.TOPROCESSFLOWNAME, ");
			sqlBuffer.append("                   Q.TOPROCESSOPERATIONNAME) ");
			sqlBuffer.append(" SELECT Q.LOTNAME, Q.FACTORYNAME, Q.PROCESSFLOWNAME, Q.PROCESSOPERATIONNAME, Q.TOFACTORYNAME, Q.TOPROCESSFLOWNAME, Q.TOPROCESSOPERATIONNAME ");
			sqlBuffer.append("   FROM CT_ProductQueueTime Q, MINQTIME M ");
			sqlBuffer.append("  WHERE     Q.FACTORYNAME = M.FACTORYNAME ");
			sqlBuffer.append("        AND Q.PROCESSFLOWNAME = M.PROCESSFLOWNAME ");
			sqlBuffer.append("        AND Q.PROCESSOPERATIONNAME = M.PROCESSOPERATIONNAME ");
			sqlBuffer.append("        AND Q.TOFACTORYNAME = M.TOFACTORYNAME ");
			sqlBuffer.append("        AND Q.TOPROCESSFLOWNAME = M.TOPROCESSFLOWNAME ");
			sqlBuffer.append("        AND Q.TOPROCESSOPERATIONNAME = M.TOPROCESSOPERATIONNAME ");
			sqlBuffer.append("        AND Q.ENTERTIME = M.ENTERTIME ");

			Object[] bindArray = new Object[0];

			List<ListOrderedMap> result;

			try
			{
				HashMap<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("LOTNAMELIST", sourceProductNameList);
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

				// Get QTime Data
				ProductQueueTime QtimeData = ExtendedObjectProxy.getProductQTimeService().selectByKey(true,
						new Object[] { lotName, factoryName, processFlowName, processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName });

				// Set New ProductName
				QtimeData.setProductName(destProductName);

				// Create
				eventInfo.setEventName("Create");
				try
				{
					ExtendedObjectProxy.getProductQTimeService().create(eventInfo, QtimeData);
				}
				catch (greenFrameDBErrorSignal ne1)
				{
					// ignore error to consider as duplicated exception signal
					throw new CustomException("LOT-0202", lotName, processOperationName, toProcessOperationName);
				}
			}
		}
		catch (Exception ex)
		{
			logger.warn("Merge Q-Time Error!");
		}
	}

	public void moveInQTimeByLotBackUp(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, String returnFlowName) throws CustomException
	{
		List<ListOrderedMap> QTimePolicyList = PolicyUtil.getQTimeSpec(factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion);

		for (ListOrderedMap QtimePolicy : QTimePolicyList)
		{
			String toFactoryName = CommonUtil.getValue(QtimePolicy, "TOFACTORYNAME");
			String toProcessFlowName = CommonUtil.getValue(QtimePolicy, "TOPROCESSFLOWNAME");
			String toProcessOperationName = CommonUtil.getValue(QtimePolicy, "TOPROCESSOPERATIONNAME");
			String warningLimit = CommonUtil.getValue(QtimePolicy, "WARNINGDURATIONLIMIT");
			String interLockLimit = CommonUtil.getValue(QtimePolicy, "INTERLOCKDURATIONLIMIT");

			try
			{
				// enter into Q time
				moveInQTimeByLotBackUp(eventInfo, lotName, factoryName, processFlowName, processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName, warningLimit, interLockLimit,
						returnFlowName);
			}
			catch (Exception ex)
			{
				// Q-time process is optional
				if (logger.isWarnEnabled())
					logger.warn(String.format("Q-time IN process for Lot[%s] to Operation[%s] is failed at Operation[%s]", lotName, toProcessOperationName, processOperationName));
			}
		}
	}

	public void moveInQTimeByLotBackUp(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processOperationName, String toFactoryName, String toProcessFlowName,
			String toProcessOperationName, String warningLimit, String interLockLimit, String returnFlowName) throws CustomException
	{
		List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);

		for (Product productData : productList)
		{
			String productName = productData.getKey().getProductName();

			ProductQueueTime QtimeData = new ProductQueueTime(productName, factoryName, processFlowName, processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName);
			QtimeData.setEnterTime(eventInfo.getEventTime());
			QtimeData.setWarningDurationLimit(warningLimit);
			QtimeData.setInterlockDurationLimit(interLockLimit);
			QtimeData.setQueueTimeState(GenericServiceProxy.getConstantMap().QTIME_STATE_IN);
			QtimeData.setLotName(lotName);

			try
			{
				List<ProductQueueTime> productQTimeDataList = ExtendedObjectProxy
						.getProductQTimeService()
						.select("productName = ? AND factoryName = ? AND processFlowName = ? AND processOperationName = ? AND toFactoryName = ? AND toProcessFlowName = ? AND toProcessOperationName = ? and rowNum = 1 ",
								new Object[] { productName, factoryName, processFlowName, processOperationName, toFactoryName, toProcessFlowName, toProcessOperationName });

				eventInfo.setEventName("Update");
				try
				{
					ExtendedObjectProxy.getProductQTimeService().modify(eventInfo, QtimeData);

				}
				catch (greenFrameDBErrorSignal ne)
				{
					throw new CustomException("LOT-0201", productName, processOperationName, toProcessOperationName);
				}
			}
			catch (greenFrameDBErrorSignal ne)
			{
				eventInfo.setEventName("Enter");

				try
				{
					ExtendedObjectProxy.getProductQTimeService().create(eventInfo, QtimeData);
				}
				catch (greenFrameDBErrorSignal ne1)
				{
					// ignore error to consider as duplicated exception signal
					throw new CustomException("LOT-0202", productName, processOperationName, toProcessOperationName);
				}
			}
		}
	}
	
	public void deleteQTime(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processOperationName) throws CustomException
	{
		try
		{
			eventInfo.setEventName("Delete");
			eventInfo.setEventComment("UnShipLotDelete");
			List<ProductQueueTime> QTimeDataList;

			QTimeDataList = ExtendedObjectProxy.getProductQTimeService().select(
					"productName in (select productName from product where lotName = ?) AND FactoryName = ? AND ProcessFlowName = ? AND ProcessOperationName = ? ",
					new Object[] { lotName, factoryName, processFlowName, processOperationName });
			for (ProductQueueTime QTimeData : QTimeDataList)
			{
				ExtendedObjectProxy.getProductQTimeService().remove(eventInfo, QTimeData);				
			}	
		
		}
		catch (greenFrameDBErrorSignal ne)
		{
			// ignore error to consider as not found exception signal
			//throw new CustomException("LOT-0201", lotName, processOperationName, processOperationName);
		}
	}
	
	public void updateQTimeToExit(EventInfo eventInfo, ProductQueueTime QTimeData) throws CustomException
	{
		try
		{
			eventInfo.setEventName("Exit");

			QTimeData.setExitTime(eventInfo.getEventTime());
			QTimeData.setQueueTimeState(GenericServiceProxy.getConstantMap().QTIME_STATE_OUT);
			ExtendedObjectProxy.getProductQTimeService().modify(eventInfo, QTimeData);
		}
		catch (greenFrameDBErrorSignal ne)
		{
			// ignore error to consider as not found exception signal
			throw new CustomException("LOT-0201", QTimeData.getProductName(), QTimeData.getProcessOperationName(), QTimeData.getToProcessOperationName());
		}
	}
	
	public void exitQTimeByProductElement(EventInfo eventInfo, String lotName, String toFactoryName, String toProcessFlowName, String toProcessOperationName, List<Element> productList) throws CustomException
	{
		List<ProductQueueTime> QtimeList = new ArrayList<ProductQueueTime>();
		
		for(int i = 0 ; i < productList.size() ; i++)
		{
			List<ProductQueueTime> QTimeDataList;
			
			try
			{
				String productName = productList.get(i).getChild("PRODUCTNAME").getText();
				
				QTimeDataList = ExtendedObjectProxy.getProductQTimeService().select(
						"productName = ? AND toFactoryName = ? AND toProcessFlowName = ? AND toProcessOperationName = ? AND queueTimeState = ?  ",
						new Object[] { productName, toFactoryName, toProcessFlowName, toProcessOperationName, GenericServiceProxy.getConstantMap().QTIME_STATE_OUT });
				
				for(ProductQueueTime QTimeData :QTimeDataList )
				{
					try
					{
						// enter into Q time
						exitQTime(eventInfo, QTimeData);
					}
					catch (Exception ex)
					{
						// Q-time process is optional
						if (logger.isWarnEnabled())
							logger.warn(String.format("Q-time OUT process for Product[%s] to Operation[%s] is failed at Operation[%s]", QTimeData.getProductName(), QTimeData.getToProcessOperationName(),
									QTimeData.getProcessOperationName()));
					}
				}
			}
			catch (Exception ex)
			{}
		}		
	}
	
	public void changeQTime(EventInfo eventInfo, String lotName, String toFactoryName, String toProcessFlowName, String toProcessOperationName, List<Element> productList) throws CustomException
	{		
		for(int i = 0 ; i < productList.size() ; i++)
		{
			List<ProductQueueTime> QTimeDataList = null;
			
			try
			{
				String productName = productList.get(i).getChild("PRODUCTNAME").getText();
				
				QTimeDataList = ExtendedObjectProxy.getProductQTimeService().select(
						"productName = ? AND toFactoryName = ? AND toProcessFlowName = ? AND toProcessOperationName = ? AND queueTimeState = ? ",
						new Object[] { productName, toFactoryName, toProcessFlowName, toProcessOperationName ,GenericServiceProxy.getConstantMap().QTIME_STATE_OUT});
				
			}
			catch (Exception ex)
			{
			}
			
			if(QTimeDataList==null)
			{
				continue;
			}
				for (ProductQueueTime QTimeData : QTimeDataList)
				{
					try
					{											
						// in form of millisecond
						Double expired = ConvertUtil.getDiffTime(TimeUtils.toTimeString(QTimeData.getEnterTime(), TimeStampUtil.FORMAT_TIMEKEY), TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY));
						// // limit unit is hour and decimal form
						expired = (expired / 60 / 60);

						if (Double.compare(expired, Double.parseDouble(QTimeData.getInterlockDurationLimit())) >= 0)
						{
							lockQTime(eventInfo, QTimeData.getProductName(), QTimeData.getFactoryName(), QTimeData.getProcessFlowName(), QTimeData.getProcessOperationName(), QTimeData.getToFactoryName(),
									QTimeData.getToProcessFlowName(), QTimeData.getToProcessOperationName());
						}
						else if (Double.compare(expired, Double.parseDouble(QTimeData.getWarningDurationLimit())) >= 0)
						{
							warnQTime(eventInfo, QTimeData.getProductName(), QTimeData.getFactoryName(), QTimeData.getProcessFlowName(), QTimeData.getProcessOperationName(), QTimeData.getToFactoryName(),
									QTimeData.getToProcessFlowName(), QTimeData.getToProcessOperationName());
						}
					}
					catch (Exception ex)
					{}
				}
			
		}

		
		List<ProductQueueTime> newQTimeList = new ArrayList<ProductQueueTime>();

		try
		{
			newQTimeList = ExtendedObjectProxy.getProductQTimeService().select(
					"productName in (select productName from product where lotName = ?) AND toFactoryName = ? AND toProcessFlowName = ? AND toProcessOperationName = ? AND QUEUETIMESTATE = ?",
					new Object[] { lotName, toFactoryName, toProcessFlowName, toProcessOperationName, GenericServiceProxy.getConstantMap().QTIME_STATE_IN});
		}
		catch (Exception ex)
		{}
		for (ProductQueueTime QTimeData : newQTimeList)
		{
			// in form of millisecond
			Double expired = ConvertUtil.getDiffTime(TimeUtils.toTimeString(QTimeData.getEnterTime(), TimeStampUtil.FORMAT_TIMEKEY), TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY));
			// // limit unit is hour and decimal form
			expired = (expired / 60 / 60);

			if (Double.compare(expired, Double.parseDouble(QTimeData.getInterlockDurationLimit())) >= 0
					&& (QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_WARN) || QTimeData.getQueueTimeState().equals(
							GenericServiceProxy.getConstantMap().QTIME_STATE_IN)))
			{
				lockQTime(eventInfo, QTimeData.getProductName(), QTimeData.getFactoryName(), QTimeData.getProcessFlowName(), QTimeData.getProcessOperationName(), QTimeData.getToFactoryName(),
						QTimeData.getToProcessFlowName(), QTimeData.getToProcessOperationName());
			}
			else if (Double.compare(expired, Double.parseDouble(QTimeData.getWarningDurationLimit())) >= 0
					&& QTimeData.getQueueTimeState().equals(GenericServiceProxy.getConstantMap().QTIME_STATE_IN))
			{
				warnQTime(eventInfo, QTimeData.getProductName(), QTimeData.getFactoryName(), QTimeData.getProcessFlowName(), QTimeData.getProcessOperationName(), QTimeData.getToFactoryName(),
						QTimeData.getToProcessFlowName(), QTimeData.getToProcessOperationName());
			}
		}
	}
}
