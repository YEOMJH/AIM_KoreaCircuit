package kr.co.aim.messolution.extended.object.management.impl;

import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.ProductRequestPlan;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class ProductRequestPlanService extends CTORMService<ProductRequestPlan> {

	private static Log log = LogFactory.getLog(ProductRequestPlanService.class);
	public static Log logger = LogFactory.getLog(ProductRequestPlanService.class);

	private final String historyEntity = "ProductRequestPlanHist";

	public List<ProductRequestPlan> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<ProductRequestPlan> result = super.select(condition, bindSet, ProductRequestPlan.class);

		return result;
	}

	public ProductRequestPlan selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ProductRequestPlan.class, isLock, keySet);
	}

	public ProductRequestPlan create(EventInfo eventInfo, ProductRequestPlan dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void create(EventInfo eventInfo, List<ProductRequestPlan> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfoList);

		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}

	public void remove(EventInfo eventInfo, ProductRequestPlan dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public ProductRequestPlan modify(EventInfo eventInfo, ProductRequestPlan dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void modify(EventInfo eventInfo, List<ProductRequestPlan> dataInfoList)
	{
		super.update(dataInfoList);

		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}

	public ProductRequestPlan getProductRequestData(String productRequestName, Timestamp planDate, String machineName)
	{
		Object[] keySet = new Object[] { productRequestName, planDate, machineName };

		ProductRequestPlan dataInfo = new ProductRequestPlan();
		try
		{
			dataInfo = ExtendedObjectProxy.getProductRequestPlanService().selectByKey(false, keySet);
		}
		catch (Exception e)
		{
			dataInfo = null;
		}

		return dataInfo;
	}

	public void createProductRequestPlan(EventInfo eventInfo, String productRequestName, Timestamp planDate, String machineName, String productSpecName, String productSpecVersion,
			String planQuantity, String factoryName, String position) throws greenFrameDBErrorSignal, CustomException
	{
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		ProductRequestPlan productRequestPlan = new ProductRequestPlan();

		productRequestPlan.setProductRequestName(productRequestName);
		productRequestPlan.setPlanDate(planDate);
		productRequestPlan.setMachineName(machineName);
		productRequestPlan.setProductSpecName(productSpecName);
		productRequestPlan.setProductSpecVersion(productSpecVersion);
		productRequestPlan.setPlanQuantity(Integer.parseInt(planQuantity));
		productRequestPlan.setCreateQuantity(0);
		productRequestPlan.setFactoryName(factoryName);
		productRequestPlan.setPlanState(GenericServiceProxy.getConstantMap().PRQPLAN_STATE_CREATED);
		productRequestPlan.setPosition(Integer.parseInt(position));
		productRequestPlan.setCreateUser(eventInfo.getEventUser());
		productRequestPlan.setCreateTime(eventInfo.getEventTime());
		productRequestPlan.setLastEventName(eventInfo.getEventName());
		productRequestPlan.setLastEventUser(eventInfo.getEventUser());
		productRequestPlan.setLastEventTime(eventInfo.getEventTime());
		productRequestPlan.setLastEventTimeKey(eventInfo.getEventTimeKey());
		productRequestPlan.setProductRequestHoldState(GenericServiceProxy.getConstantMap().PRQPLAN_HOLDSTATE_ONHOLD);

		ExtendedObjectProxy.getProductRequestPlanService().create(eventInfo, productRequestPlan);
	}

	public void deleteProductRequestPlan(EventInfo eventInfo, String productRequestName, Timestamp planDate, String machineName) throws CustomException
	{
		ProductRequestPlan dataInfo = ExtendedObjectProxy.getProductRequestPlanService().getProductRequestData(productRequestName, planDate, machineName);

		if (dataInfo != null)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			ExtendedObjectProxy.getProductRequestPlanService().remove(eventInfo, dataInfo);
		}
		else
		{
			log.info("ProductReqeustPlan not exist");
		}
	}

	public void makeCreatedProductRequestPlan(EventInfo eventInfo, String productRequestName, Timestamp planDate, String machineName) throws greenFrameDBErrorSignal, CustomException
	{
		ProductRequestPlan productRequestPlan = ExtendedObjectProxy.getProductRequestPlanService().selectByKey(false, new Object[] { productRequestName, planDate, machineName });

		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		productRequestPlan.setPlanState(GenericServiceProxy.getConstantMap().PRQPLAN_STATE_CREATED);
		productRequestPlan.setLastEventName(eventInfo.getEventName());
		productRequestPlan.setLastEventUser(eventInfo.getEventUser());
		productRequestPlan.setLastEventTime(eventInfo.getEventTime());
		productRequestPlan.setLastEventTimeKey(eventInfo.getEventTimeKey());
		productRequestPlan.setProductRequestHoldState(GenericServiceProxy.getConstantMap().PRQPLAN_HOLDSTATE_ONHOLD);

		ExtendedObjectProxy.getProductRequestPlanService().modify(eventInfo, productRequestPlan);
	}

	public void makeReleasedProductRequestPlan(EventInfo eventInfo, String productRequestName, Timestamp planDate, String machineName) throws greenFrameDBErrorSignal, CustomException
	{
		ProductRequestPlan productRequestPlan = ExtendedObjectProxy.getProductRequestPlanService().selectByKey(false, new Object[] { productRequestName, planDate, machineName });

		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		productRequestPlan.setPlanState(GenericServiceProxy.getConstantMap().PRQPLAN_STATE_RELEASED);
		productRequestPlan.setLastEventName(eventInfo.getEventName());
		productRequestPlan.setLastEventUser(eventInfo.getEventUser());
		productRequestPlan.setLastEventTime(eventInfo.getEventTime());
		productRequestPlan.setLastEventTimeKey(eventInfo.getEventTimeKey());
		productRequestPlan.setProductRequestHoldState(GenericServiceProxy.getConstantMap().PRQPLAN_HOLDSTATE_NOTONHOLD);

		ExtendedObjectProxy.getProductRequestPlanService().modify(eventInfo, productRequestPlan);
	}

	public void makeReleasedProductRequestPlan(EventInfo eventInfo, ProductRequestPlan productRequestPlan) throws greenFrameDBErrorSignal, CustomException
	{
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		productRequestPlan.setPlanState(GenericServiceProxy.getConstantMap().PRQPLAN_STATE_RELEASED);
		productRequestPlan.setLastEventName(eventInfo.getEventName());
		productRequestPlan.setLastEventUser(eventInfo.getEventUser());
		productRequestPlan.setLastEventTime(eventInfo.getEventTime());
		productRequestPlan.setLastEventTimeKey(eventInfo.getEventTimeKey());
		productRequestPlan.setProductRequestHoldState(GenericServiceProxy.getConstantMap().PRQPLAN_HOLDSTATE_NOTONHOLD);

		ExtendedObjectProxy.getProductRequestPlanService().modify(eventInfo, productRequestPlan);
	}

	public void makeReleasedProductRequestPlanWithPosition(EventInfo eventInfo, ProductRequestPlan productRequestPlan, long position) throws greenFrameDBErrorSignal, CustomException
	{
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		productRequestPlan.setPlanState(GenericServiceProxy.getConstantMap().PRQPLAN_STATE_RELEASED);
		productRequestPlan.setPosition(position);
		productRequestPlan.setLastEventName(eventInfo.getEventName());
		productRequestPlan.setLastEventUser(eventInfo.getEventUser());
		productRequestPlan.setLastEventTime(eventInfo.getEventTime());
		productRequestPlan.setLastEventTimeKey(eventInfo.getEventTimeKey());
		productRequestPlan.setProductRequestHoldState(GenericServiceProxy.getConstantMap().PRQPLAN_HOLDSTATE_NOTONHOLD);

		ExtendedObjectProxy.getProductRequestPlanService().modify(eventInfo, productRequestPlan);
	}

	public void makeCompletedProductRequestPlan(EventInfo eventInfo, String productRequestName, Timestamp planDate, String machineName) throws greenFrameDBErrorSignal, CustomException
	{
		ProductRequestPlan productRequestPlan = ExtendedObjectProxy.getProductRequestPlanService().selectByKey(false, new Object[] { productRequestName, planDate, machineName });

		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		productRequestPlan.setPlanState(GenericServiceProxy.getConstantMap().PRQPLAN_STATE_COMPLETED);
		productRequestPlan.setLastEventName(eventInfo.getEventName());
		productRequestPlan.setLastEventUser(eventInfo.getEventUser());
		productRequestPlan.setLastEventTime(eventInfo.getEventTime());
		productRequestPlan.setLastEventTimeKey(eventInfo.getLastEventTimekey());

		ExtendedObjectProxy.getProductRequestPlanService().modify(eventInfo, productRequestPlan);
	}

	public void makeCompletedProductRequestPlan(EventInfo eventInfo, ProductRequestPlan productRequstPlanData) throws greenFrameDBErrorSignal, CustomException
	{
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		productRequstPlanData.setPlanState(GenericServiceProxy.getConstantMap().PRQPLAN_STATE_STARTED);
		productRequstPlanData.setLastEventName(eventInfo.getEventName());
		productRequstPlanData.setLastEventUser(eventInfo.getEventUser());
		productRequstPlanData.setLastEventTime(eventInfo.getEventTime());
		productRequstPlanData.setLastEventTimeKey(eventInfo.getLastEventTimekey());

		ExtendedObjectProxy.getProductRequestPlanService().modify(eventInfo, productRequstPlanData);
	}

	public void makeStartedProductRequestPlan(EventInfo eventInfo, String productRequestName, Timestamp planDate, String machineName) throws greenFrameDBErrorSignal, CustomException
	{
		ProductRequestPlan productRequestPlan = ExtendedObjectProxy.getProductRequestPlanService().selectByKey(false, new Object[] { productRequestName, planDate, machineName });

		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		productRequestPlan.setPlanState(GenericServiceProxy.getConstantMap().PRQPLAN_STATE_STARTED);
		productRequestPlan.setLastEventName(eventInfo.getEventName());
		productRequestPlan.setLastEventUser(eventInfo.getEventUser());
		productRequestPlan.setLastEventTime(eventInfo.getEventTime());
		productRequestPlan.setLastEventTimeKey(eventInfo.getLastEventTimekey());

		ExtendedObjectProxy.getProductRequestPlanService().modify(eventInfo, productRequestPlan);
	}

	public void makeStartedProductRequestPlan(EventInfo eventInfo, ProductRequestPlan productRequestPlan) throws greenFrameDBErrorSignal, CustomException
	{
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		productRequestPlan.setPlanState(GenericServiceProxy.getConstantMap().PRQPLAN_STATE_STARTED);
		productRequestPlan.setLastEventName(eventInfo.getEventName());
		productRequestPlan.setLastEventUser(eventInfo.getEventUser());
		productRequestPlan.setLastEventTime(eventInfo.getEventTime());
		productRequestPlan.setLastEventTimeKey(eventInfo.getLastEventTimekey());

		ExtendedObjectProxy.getProductRequestPlanService().modify(eventInfo, productRequestPlan);
	}

	public void makeHoldProductRequestPlan(EventInfo eventInfo, String productRequestName, Timestamp planDate, String machineName) throws greenFrameDBErrorSignal, CustomException
	{
		ProductRequestPlan productRequestPlanData = ExtendedObjectProxy.getProductRequestPlanService().selectByKey(false, new Object[] { productRequestName, planDate, machineName });

		if (!StringUtils.equals(productRequestPlanData.getPlanState(), GenericServiceProxy.getConstantMap().PRQPLAN_STATE_RELEASED))
		{
			throw new CustomException("PRODUCTREQUESTPLAN-0001", productRequestPlanData.getPlanState());
		}

		if (StringUtils.equals(productRequestPlanData.getProductRequestHoldState(), GenericServiceProxy.getConstantMap().PRQPLAN_HOLDSTATE_ONHOLD))
		{
			throw new CustomException("PRODUCTREQUESTPLAN-0002", productRequestPlanData.getProductRequestHoldState());
		}

		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		productRequestPlanData.setLastEventName(eventInfo.getEventName());
		productRequestPlanData.setLastEventUser(eventInfo.getEventUser());
		productRequestPlanData.setLastEventTime(eventInfo.getEventTime());
		productRequestPlanData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		productRequestPlanData.setProductRequestHoldState(GenericServiceProxy.getConstantMap().PRQPLAN_HOLDSTATE_ONHOLD);

		ExtendedObjectProxy.getProductRequestPlanService().modify(eventInfo, productRequestPlanData);
	}

	public void makeReleaseHoldProductRequestPlan(EventInfo eventInfo, String productRequestName, Timestamp planDate, String machineName) throws greenFrameDBErrorSignal, CustomException
	{
		ProductRequestPlan productRequestPlanData = ExtendedObjectProxy.getProductRequestPlanService().selectByKey(false, new Object[] { productRequestName, planDate, machineName });

		if (!StringUtils.equals(productRequestPlanData.getPlanState(), GenericServiceProxy.getConstantMap().PRQPLAN_STATE_RELEASED))
		{
			throw new CustomException("PRODUCTREQUESTPLAN-0001", productRequestPlanData.getPlanState());
		}

		if (StringUtils.equals(productRequestPlanData.getProductRequestHoldState(), GenericServiceProxy.getConstantMap().PRQPLAN_HOLDSTATE_NOTONHOLD))
		{
			throw new CustomException("PRODUCTREQUESTPLAN-0002", productRequestPlanData.getProductRequestHoldState());
		}

		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		productRequestPlanData.setLastEventName(eventInfo.getEventName());
		productRequestPlanData.setLastEventUser(eventInfo.getEventUser());
		productRequestPlanData.setLastEventTime(eventInfo.getEventTime());
		productRequestPlanData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		productRequestPlanData.setProductRequestHoldState(GenericServiceProxy.getConstantMap().PRQPLAN_HOLDSTATE_NOTONHOLD);

		ExtendedObjectProxy.getProductRequestPlanService().modify(eventInfo, productRequestPlanData);
	}

	public ProductRequestPlan changePosition(EventInfo eventInfo, ProductRequestPlan productReqeustPlanData, String position) throws CustomException
	{
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		productReqeustPlanData.setPosition(Integer.parseInt(position));
		ProductRequestPlan planData = ExtendedObjectProxy.getProductRequestPlanService().modify(eventInfo, productReqeustPlanData);

		return planData;
	}

	public void modifyCreateQuantity(EventInfo eventInfo, ProductRequestPlan productRequestPlanData, long createQty) throws CustomException
	{
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		productRequestPlanData.setCreateQuantity(createQty);
		productRequestPlanData.setLastEventName(eventInfo.getEventName());
		productRequestPlanData.setLastEventTime(eventInfo.getEventTime());
		productRequestPlanData.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		productRequestPlanData.setLastEventUser(eventInfo.getEventUser());

		ExtendedObjectProxy.getProductRequestPlanService().modify(eventInfo, productRequestPlanData);
	}
}
