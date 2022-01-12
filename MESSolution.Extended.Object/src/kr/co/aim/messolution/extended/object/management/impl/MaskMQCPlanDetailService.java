package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MQCPlan;
import kr.co.aim.messolution.extended.object.management.data.MQCPlanDetail;
import kr.co.aim.messolution.extended.object.management.data.MaskMQCPlanDetail;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.product.management.data.Product;

public class MaskMQCPlanDetailService extends CTORMService<MaskMQCPlanDetail> {
	
	public static Log logger = LogFactory.getLog(MaskMQCPlanDetailService.class);
	
	private final String historyEntity = "MASKMQCPLANDETAILHISTORY";
	
	public List<MaskMQCPlanDetail> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<MaskMQCPlanDetail> result = super.select(condition, bindSet, MaskMQCPlanDetail.class);
		
		return result;
	}
	
	public MaskMQCPlanDetail selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(MaskMQCPlanDetail.class, isLock, keySet);
	}
	
	public MaskMQCPlanDetail create(EventInfo eventInfo, MaskMQCPlanDetail dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void create(EventInfo eventInfo, List<MaskMQCPlanDetail> dataInfoList) throws CustomException
	{
		super.insert(dataInfoList);

		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}

	public void remove(EventInfo eventInfo, MaskMQCPlanDetail dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public MaskMQCPlanDetail modify(EventInfo eventInfo, MaskMQCPlanDetail dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public MaskMQCPlanDetail insertMaskMQCPlanDetail(EventInfo eventInfo, String jobName, String maskProcessFlowName, String maskProcessFlowVersion, String maskProcessOperationName, String maskProcessOperationVersion,
			String maskLotName, String recipeName, String machineName,String RMSFlag) throws CustomException
	{
		MaskMQCPlanDetail dataInfo = new MaskMQCPlanDetail();
		dataInfo.setJobName(jobName);
		dataInfo.setMaskProcessFlowName(maskProcessFlowName);
		dataInfo.setMaskProcessFlowVersion(maskProcessFlowVersion);
		dataInfo.setMaskProcessOperationName(maskProcessOperationName);
		dataInfo.setMaskProcessOperationVersion(maskProcessOperationVersion);
		dataInfo.setMaskLotName(maskLotName);
		dataInfo.setMachineName(machineName);
		dataInfo.setRecipeName(recipeName);
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventUser(eventInfo.getEventUser());

		MaskMQCPlanDetail planDetailData = ExtendedObjectProxy.getMaskMQCPlanDetailService().create(eventInfo, dataInfo);

		return planDetailData;
	}
	
}
