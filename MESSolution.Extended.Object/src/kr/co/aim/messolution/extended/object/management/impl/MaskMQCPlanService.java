package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MQCPlan;
import kr.co.aim.messolution.extended.object.management.data.MaskMQCPlan;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.generic.util.SpecUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.orm.SQLLogUtil;
import kr.co.aim.greentrack.generic.GenericServiceProxy;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.product.management.data.ProductSpec;

public class MaskMQCPlanService extends CTORMService<MaskMQCPlan> {
	
	public static Log logger = LogFactory.getLog(MaskMQCPlanService.class);
	
	private final String historyEntity = "MASKMQCPLANHISTORY";
	
	public List<MaskMQCPlan> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<MaskMQCPlan> result = super.select(condition, bindSet, MaskMQCPlan.class);
		
		return result;
	}
	
	public MaskMQCPlan selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(MaskMQCPlan.class, isLock, keySet);
	}
	
	public MaskMQCPlan create(EventInfo eventInfo, MaskMQCPlan dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, MaskMQCPlan dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public MaskMQCPlan modify(EventInfo eventInfo, MaskMQCPlan dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public MaskMQCPlan insertMaskMQCPlan(EventInfo eventInfo, String jobName, String factoryName, String maskSpecName, String maskProcessFlowName,
			String maskProcessFlowVersion, String maskLotName,String returnProcessFlowName,String returnProcessFlowVersion,String returnProcessOperationName,String returnProcessOperationVersion) throws CustomException
	{
		MaskMQCPlan planData = new MaskMQCPlan(jobName);
		planData.setJobName(jobName);
		planData.setFactoryName(factoryName);
		planData.setMaskSpecName(maskSpecName);
		planData.setMaskProcessFlowName(maskProcessFlowName);
		planData.setMaskProcessFlowVersion(maskProcessFlowVersion);
		planData.setMaskLotName(maskLotName);
		planData.setMQCState("Created");
		planData.setLastEventComment(eventInfo.getEventComment());
		planData.setLastEventName(eventInfo.getEventName());
		planData.setLastEventTime(eventInfo.getEventTime());
		planData.setLastEventUser(eventInfo.getEventUser());
		planData.setCreateUser(eventInfo.getEventUser());
		planData = ExtendedObjectProxy.getMaskMQCPlanService().create(eventInfo, planData);

		return planData;
	}
}
