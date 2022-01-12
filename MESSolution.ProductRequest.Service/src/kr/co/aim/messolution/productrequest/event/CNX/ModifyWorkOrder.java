package kr.co.aim.messolution.productrequest.event.CNX;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.ChangeSpecInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

import com.sun.jndi.url.iiopname.iiopnameURLContextFactory;

public class ModifyWorkOrder extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", false);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", false);
		String planQuantity = SMessageUtil.getBodyItemValue(doc, "PLANQUANTITY", true);
		String planFinishedTime = SMessageUtil.getBodyItemValue(doc, "PLANFINISHEDTIME", true);
		String planReleasedTime = SMessageUtil.getBodyItemValue(doc, "PLANRELEASEDTIME", true);
		String autoShippingFlag = SMessageUtil.getBodyItemValue(doc, "AUTOSHIPPINGFLAG", true);
		String description = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", true);
		String productRequestType = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTTYPE", true);
		String riskFlag = SMessageUtil.getBodyItemValue(doc, "RISKFLAG", true);
		String prOwner = SMessageUtil.getBodyItemValue(doc, "PROWNER", false);
		String crateSpecName = SMessageUtil.getBodyItemValue(doc, "CRATESPECNAME", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyWorkOrder", getEventUser(), getEventComment(), "", "");
		
		ProductRequest productRequest = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);
		
		
		if(StringUtils.equals(productRequestType, "M") || StringUtils.equals(productRequestType, "D"))
		{
			checkData(productRequest, productSpecName, processFlowName, planQuantity, planFinishedTime, planReleasedTime, autoShippingFlag,description,riskFlag);
		}
		else
		{
			checkDataWithSuperProductRequest(productRequest, planQuantity, planFinishedTime, autoShippingFlag,description,riskFlag,prOwner);
		}
			
		ChangeSpecInfo changeSpecInfo = MESWorkOrderServiceProxy.getProductRequestInfoUtil().changeSpecInfo(factoryName, productRequest.getProductRequestType(), productSpecName, productSpecVersion,
				TimeUtils.getTimestamp(planFinishedTime), TimeUtils.getTimestamp(planReleasedTime), Long.parseLong(planQuantity), productRequest.getReleasedQuantity(),
				productRequest.getFinishedQuantity(), productRequest.getScrappedQuantity(), productRequest.getProductRequestState(), productRequest.getProductRequestHoldState(), processFlowName,
				processFlowVersion, autoShippingFlag, productRequest.getUdfs().get("PLANSEQUENCE"), productRequest.getUdfs().get("CREATEDQUANTITY"), description);
				
		if(StringUtils.equals(productRequestType, "M") || StringUtils.equals(productRequestType, "D"))
		{
			Map<String, String> productRequestUdfs=changeSpecInfo.getUdfs();
			productRequestUdfs.put("CRATESPECNAME", crateSpecName);
			changeSpecInfo.setUdfs(productRequestUdfs);
		}

		MESWorkOrderServiceProxy.getProductRequestServiceImpl().changeSpec(productRequest, changeSpecInfo, eventInfo);
		
		if(!riskFlag.equals(productRequest.getUdfs().get("RISKFLAG")) || !prOwner.equals(productRequest.getUdfs().get("PROWNER")))
		{
			String sql = " UPDATE PRODUCTREQUEST "
					+ " SET RISKFLAG = '"+riskFlag+ "', PROWNER = '" + prOwner
					+ "' WHERE 1 = 1 "					
					+ "  AND PRODUCTREQUESTNAME = :PRODUCTREQUESTNAME "
					+ "  AND FACTORYNAME = :FACTORYNAME ";

				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("PRODUCTREQUESTNAME", productRequestName);
				bindMap.put("FACTORYNAME", factoryName);
				
				greenFrameServiceProxy.getSqlTemplate().update(sql, bindMap);
		}//add risk && prOwner
		
		
					
		return doc;
	}

	private void checkData(ProductRequest productRequest, String productSpecName, String processFlowName, String sPlanQuantity, String sPlanFinishedTime, String sPlanReleasedTime,
			String autoShippingFlag, String description,String riskFlag) throws CustomException
	{
		String oldProductSpecName = productRequest.getProductSpecName();
		Map<String, String> udfs = productRequest.getUdfs();
		String oldProcessFlowName = udfs.get("PROCESSFLOWNAME");
		Long oldplanQuantity = productRequest.getPlanQuantity();
		Timestamp oldPlanFinishedTime = productRequest.getPlanFinishedTime();
		Timestamp oldPlanReleasedTime = productRequest.getPlanReleasedTime();
		String oldAutoShippingFlag = productRequest.getUdfs().get("AUTOSHIPPINGFLAG");

		long planQuantity = Long.parseLong(sPlanQuantity);
		Timestamp planFinishedTime = TimeUtils.getTimestamp(sPlanFinishedTime);
		Timestamp planReleasedTime = TimeUtils.getTimestamp(sPlanReleasedTime);

		if (oldProductSpecName.equals(productSpecName) && oldProcessFlowName.equals(processFlowName) && oldplanQuantity == planQuantity && oldPlanFinishedTime==planFinishedTime
				&& oldPlanReleasedTime==planReleasedTime && oldAutoShippingFlag.equals(autoShippingFlag)&&productRequest.getUdfs().get("DESCRIPTION").equals(description)&&productRequest.getUdfs().get("RISKFLAG").equals(riskFlag))
			throw new CustomException("WORKORDER-0001");
	}
	
	private void checkDataWithSuperProductRequest(ProductRequest productRequest, String sPlanQuantity, String sPlanFinishedTime, String autoShippingFlag, String description,String riskFlag, String prOwner) throws CustomException
	{
		Long oldplanQuantity = productRequest.getPlanQuantity();
		Timestamp oldPlanFinishedTime = productRequest.getPlanFinishedTime();
		String oldAutoShippingFlag = productRequest.getUdfs().get("AUTOSHIPPINGFLAG");
		String oldRiskFlag = productRequest.getUdfs().get("RISKFLAG");
		String oldPrOwner = productRequest.getUdfs().get("PROWNER");
		
		long planQuantity = Long.parseLong(sPlanQuantity);
		Timestamp planFinishedTime = TimeUtils.getTimestamp(sPlanFinishedTime.substring(0, 8));
		
		List<ProductRequest> productRequestList = null;
		
		try 
		{
			productRequestList = ProductRequestServiceProxy.getProductRequestService().select(" superProductRequestName = ? and productRequestName != ? ", new Object[] { productRequest.getUdfs().get("SUPERPRODUCTREQUESTNAME").toString(), productRequest.getKey().getProductRequestName()});
		} 
		catch (Exception e) 
		{}
		
		SuperProductRequest superProductRequest = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{ productRequest.getUdfs().get("SUPERPRODUCTREQUESTNAME").toString()});
		
		long sumPlanQuantity = planQuantity;
		
		if(productRequestList != null)
		{
			for(ProductRequest productRequestInfo : productRequestList)
			{
				sumPlanQuantity += productRequestInfo.getPlanQuantity();
			}
		}

		if(superProductRequest.getPlanFinishedTime().before(planFinishedTime))
			throw new CustomException("WORKORDER-0006", superProductRequest.getPlanFinishedTime(), planFinishedTime);
		
		if(superProductRequest.getPlanQuantity() < sumPlanQuantity)
			throw new CustomException("WORKORDER-0005", superProductRequest.getPlanQuantity(), sumPlanQuantity);

		if (oldplanQuantity == planQuantity && 
				oldPlanFinishedTime.equals(planFinishedTime) && 
				oldAutoShippingFlag.equals(autoShippingFlag)&&
				productRequest.getUdfs().get("DESCRIPTION").equals(description)&&oldRiskFlag.equals(riskFlag)&&oldPrOwner.equals(prOwner))
			throw new CustomException("WORKORDER-0001");
	}
	
	private boolean checkSubType( String productRequestName) throws CustomException
	{
		
		List<Map<String, Object>> checkDataResult = new ArrayList<Map<String, Object>>();	
		Map<String, Object> checkDatabindMap = new HashMap<String, Object>();
		StringBuilder checkDataSql = new StringBuilder();
   
		checkDataSql.append("SELECT SUBPRODUCTIONTYPE ");
		checkDataSql.append("  FROM PRODUCTREQUEST ");
		checkDataSql.append(" WHERE PRODUCTREQUESTNAME = :PRODUCTREQUESTNAME ");

	       
		checkDatabindMap = new HashMap<String, Object>();	
		
		checkDatabindMap.put("PRODUCTREQUESTNAME", productRequestName );	
	
		checkDataResult = GenericServiceProxy.getSqlMesTemplate().queryForList(checkDataSql.toString(), checkDatabindMap);
		
		if(checkDataResult.isEmpty() || checkDataResult.size()<0){
			
			List<Map<String, Object>> checkSuperDataResult = new ArrayList<Map<String, Object>>();	
			Map<String, Object> checkSuperDatabindMap = new HashMap<String, Object>();
			StringBuilder checkSuperDataSql = new StringBuilder();
	   
			checkSuperDataSql.append("SELECT SUBPRODUCTIONTYPE ");
			checkSuperDataSql.append("  FROM CT_SUPERPRODUCTREQUEST ");
			checkSuperDataSql.append(" WHERE PRODUCTREQUESTNAME = :PRODUCTREQUESTNAME ");

		       
			checkSuperDatabindMap = new HashMap<String, Object>();	
			
			checkSuperDatabindMap.put("PRODUCTREQUESTNAME", productRequestName );	
		
			checkSuperDataResult = GenericServiceProxy.getSqlMesTemplate().queryForList(checkSuperDataSql.toString(), checkSuperDatabindMap);
			
			String resultSuperData =checkSuperDataResult.get(0).get("SUBPRODUCTIONTYPE").toString();
			if(resultSuperData.equals("ESCL")){
				return true;
			}else{
				
				return false;
			}
			
		}else{
			return false;
		}
	}
	
}
