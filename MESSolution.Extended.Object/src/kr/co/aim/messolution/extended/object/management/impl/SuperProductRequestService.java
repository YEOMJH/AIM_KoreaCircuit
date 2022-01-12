package kr.co.aim.messolution.extended.object.management.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.RecipeParameter;
import kr.co.aim.messolution.extended.object.management.data.ShieldLot;
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;

import com.sun.org.apache.xpath.internal.operations.And;

public class SuperProductRequestService extends CTORMService<SuperProductRequest> {
	public static Log logger = LogFactory.getLog(SuperProductRequestService.class);

	private final String historyEntity = "SuperProductRequestHistory";
	
	public List<SuperProductRequest> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<SuperProductRequest> result = super.select(condition, bindSet, SuperProductRequest.class);

		return result;
	}

	public SuperProductRequest selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(SuperProductRequest.class, isLock, keySet);
	}

	public SuperProductRequest create(EventInfo eventInfo, SuperProductRequest dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void create(EventInfo eventInfo, List<SuperProductRequest> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfoList);

		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}

	public void remove(EventInfo eventInfo, SuperProductRequest dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public void remove(EventInfo eventInfo, List<SuperProductRequest> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);

		super.delete(dataInfoList);
	}

	public SuperProductRequest modify(EventInfo eventInfo, SuperProductRequest dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void modify(EventInfo eventInfo, List<SuperProductRequest> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.update(dataInfoList);

		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}
	
	public Map<String, String> getFactoryInfo(String productRequestType, String subProductionType, String riskYN,String lotDetailGrade)
	{
		if(!StringUtils.equals(subProductionType, "P")&&!StringUtils.equals(subProductionType, "ESLC")
				&&!StringUtils.equals(subProductionType, "SLCFG")&&!StringUtils.equals(subProductionType, "LCFG"))
		{
			riskYN="N";
		}
		Map<String, String>  result = new HashMap<String, String>();
		ConstantMap constMap = new ConstantMap();
		
		if(StringUtil.equals(productRequestType, "P"))
		{
			result.put("CODE", constMap.SAPFactoryCode_5001);
			if(StringUtils.contains(lotDetailGrade, "C"))
			{
				result.put("LOCATION", constMap.SAPFactoryPosition_9F93);
			}
			else if(StringUtil.equals(riskYN, "Y"))
			{
				result.put("LOCATION", constMap.SAPFactoryPosition_3F03);
			}
			else
			{
				result.put("LOCATION", constMap.SAPFactoryPosition_3F01);
			}
			
		}
		else if(StringUtil.equals(productRequestType, "E")||StringUtil.equals(productRequestType, "T"))
		{
			if(StringUtil.equals(subProductionType, "ESLC")||StringUtil.equals(subProductionType, "LCFG")
					||StringUtil.equals(subProductionType, "SLCFG")||StringUtil.equals(subProductionType, "SYZLC"))
			{
				result.put("CODE", constMap.SAPFactoryCode_5001);
				if(StringUtils.contains(lotDetailGrade, "C"))
				{
					result.put("LOCATION", constMap.SAPFactoryPosition_9F93);
				}
				else if(StringUtil.equals(riskYN, "Y"))
				{
					result.put("LOCATION", constMap.SAPFactoryPosition_3F03);
				}
				else
				{
					result.put("LOCATION", constMap.SAPFactoryPosition_3F01);
				}
			}
			else 
			{
				result.put("CODE", constMap.SAPFactoryCode_5099);
				result.put("LOCATION", constMap.SAPFactoryPosition_9F91);
			}
			
		}
		
		return result;
	}
}
