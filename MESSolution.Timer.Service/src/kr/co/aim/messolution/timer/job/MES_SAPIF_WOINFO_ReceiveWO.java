package kr.co.aim.messolution.timer.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DefectCode;
import kr.co.aim.messolution.extended.object.management.data.MaskSpec;
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.CreateInfo;

public class MES_SAPIF_WOINFO_ReceiveWO implements Job, InitializingBean {
	private static Log log = LogFactory.getLog(MES_SAPIF_WOINFO_ReceiveWO.class);

	@Override
	public void afterPropertiesSet() throws Exception
	{
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}

	public void execute(JobExecutionContext arg0) throws JobExecutionException
	{
		try
		{
			monitorSAP();
		}
		catch (CustomException e)
		{
			if (log.isDebugEnabled())
				log.error(e.errorDef.getLoc_errorMessage());
		}
	}

	public void monitorSAP() throws CustomException
	{
		Object[] bindArray = new Object[0];
		
		StringBuffer sqlSelect = new StringBuffer();
		sqlSelect.append("SELECT PRODUCTIONFACTORYCODE, RECEIVEPRODUCTIONCODE, PRODUCTSPECNAME, PRODUCTSPECDESC, PRODUCTREQUESTNAME, ");
		sqlSelect.append("    SUBPRODUCTIONTYPECODE, PRODUCTREQUESTDESC, PRODUCTREQUESTSTATE, PLANQUANTITY, PRODUCTTYPE, PRODUCTREQUESTPRIORITY, ");
		sqlSelect.append("    PLANRELEASEDTIME, PLANFINISHEDTIME, FACTORYNAME, PRODUCTIONTYPE, PROJECTPRODUCTREQUESTNAME, COSTDEPARTMENT, ESBFLAG, RESULTMESSAGE   ");
		sqlSelect.append("FROM MES_SAPIF_WOINFO@OADBLINK.V3FAB.COM ");
		sqlSelect.append("WHERE ESBFLAG = 'N' "); 
		sqlSelect.append("AND FACTORYNAME <> 'MODULE' "); 


		List<ListOrderedMap> selResult;

		try
		{
			GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
			selResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlSelect.toString(), bindArray);
			GenericServiceProxy.getTxDataSourceManager().commitTransaction();
		}
		catch (FrameworkErrorSignal fe)
		{
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			selResult = null;
		}
		
		if (selResult != null && selResult.size() > 0)
		{
			StringBuffer sqlUpdateResult = new StringBuffer();
			sqlUpdateResult.append("UPDATE MES_SAPIF_WOINFO@OADBLINK.V3FAB.COM SET ESBFLAG = ?, RESULT = ?, RESULTMESSAGE = ? WHERE PRODUCTREQUESTNAME = ? ");

			for (ListOrderedMap resultRow : selResult)
			{
				String productRequestName = CommonUtil.getValue(resultRow, "PRODUCTREQUESTNAME");
				String subProductionTypeCode = CommonUtil.getValue(resultRow, "SUBPRODUCTIONTYPECODE");
				String factoryName = CommonUtil.getValue(resultRow, "FACTORYNAME");
				if(factoryName.contains("OLED"))
				{
					factoryName="OLED";
				}
				String productSpecName = CommonUtil.getValue(resultRow, "PRODUCTSPECNAME");
				String projectProductRequestName = CommonUtil.getValue(resultRow, "PROJECTPRODUCTREQUESTNAME");
				String productionType = CommonUtil.getValue(resultRow, "PRODUCTIONTYPE");
				String productRequestDesc = CommonUtil.getValue(resultRow, "PRODUCTREQUESTDESC");
				String planQuantity = CommonUtil.getValue(resultRow, "PLANQUANTITY");
				String productType = CommonUtil.getValue(resultRow, "PRODUCTTYPE");
				String planReleasedTime = CommonUtil.getValue(resultRow, "PLANRELEASEDTIME");
				String planFinishedTime = CommonUtil.getValue(resultRow, "PLANFINISHEDTIME");
				String riskFlag = CommonUtil.getValue(resultRow, "PRODUCTREQUESTPRIORITY");
				String costDepartment = CommonUtil.getValue(resultRow, "COSTDEPARTMENT");
				String productRequestState = CommonUtil.getValue(resultRow, "PRODUCTREQUESTSTATE");
				
				if(StringUtil.equals(subProductionTypeCode, "ZP12")
						||StringUtil.equals(subProductionTypeCode, "ZP11")
						||StringUtil.equals(subProductionTypeCode, "ZP10"))
				{
					productionType="E";
				}
				else if (StringUtil.equals(subProductionTypeCode, "ZP16"))
				{
					productionType="P";
				}
				
				List<Object[]> resultArgList = new ArrayList<Object[]>();
				List<Object> resultBindList = new ArrayList<Object>();
				
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("", "ERP", "", null, null);
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
				
				if(subProductionTypeCode.equals("Z003"))
				{
					/*
					eventInfo.setEventName("UpdateMaskSpec");
					eventInfo.setEventComment("Update MaskSpec By ERP");
					
					String maskSpecName = productSpecName;
					
					MaskSpec maskSpec = null;
					
					try
					{
						maskSpec = ExtendedObjectProxy.getMaskSpecService().selectByKey(false, new Object[]{factoryName, maskSpecName});
						
						maskSpec.setProjectProductRequestName(projectProductRequestName);
						
						ExtendedObjectProxy.getMaskSpecService().modify(eventInfo, maskSpec);
					}
					catch (Exception e)
					{
						resultBindList.add("Y");
						resultBindList.add("E");
						resultBindList.add(e.getMessage());
						resultBindList.add(productRequestName);
						resultArgList.add(resultBindList.toArray());
						
						GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
						GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlUpdateResult.toString(), resultArgList);
						GenericServiceProxy.getTxDataSourceManager().commitTransaction();
						
						continue;
					}
					
					resultBindList.add("Y");
					resultBindList.add("S");
					resultBindList.add("");
					resultBindList.add(productRequestName);
					resultArgList.add(resultBindList.toArray());
					
					GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
					GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlUpdateResult.toString(), resultArgList);
					GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					*/
				}
				else
				{
					eventInfo.setEventName("CreateWorkOrder");
					eventInfo.setEventComment("Create WO By ERP");
					
					productSpecName = productSpecName + productionType;
					String subProductionType = "";
					
					StringBuilder sql = new StringBuilder();
					sql.append("SELECT EDV2.DESCRIPTION ");
					sql.append("  FROM ENUMDEFVALUE EDV1,ENUMDEFVALUE EDV2 ");
					sql.append(" WHERE EDV1.ENUMNAME=:ENUMNAME1 ");
					sql.append(" AND EDV2.ENUMNAME=:ENUMNAME2 ");
					sql.append(" AND EDV1.ENUMVALUE=:ENUMVALUE ");
					sql.append(" AND EDV1.ENUMVALUE = EDV2.ENUMVALUE ");

					Map<String, Object> args = new HashMap<String, Object>();
					args.put("ENUMNAME1",productionType+"SapSubProductionType" );
					args.put("ENUMNAME2",productionType+"MESSubProductionType" );
					args.put("ENUMVALUE",subProductionTypeCode );
 
					boolean insertFlag=true;
					try {
						
						SuperProductRequest superProductRequest=ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new String[]{productRequestName});
						insertFlag=false;
						if(superProductRequest.getProductRequestState().equals("Completed"))
						{
							//PRODUCTREQUEST-0047:ProductRequest is Completed
							throw new CustomException("PRODUCTREQUEST-0047");
						} 
						else if (superProductRequest.getProductRequestState().equals("Released"))
						{
							if(Long.parseLong(planQuantity)<superProductRequest.getPlanQuantity())
							{
								//PRODUCTREQUEST-0048: ProductRequest is Released and newPlanQty<OldPlanQty
								throw new CustomException("PRODUCTREQUEST-0048");
							}
						}
					} catch (greenFrameDBErrorSignal n ) 
					{
						log.info("Insert SuperProductRequest");						
					}
					catch (CustomException ce)
					{
						resultBindList.add("Y");
						resultBindList.add("E");
						resultBindList.add(eventInfo.getEventTimeKey()+" "+ce.errorDef.getLoc_errorMessage());
						resultBindList.add(productRequestName);
						resultArgList.add(resultBindList.toArray());
							
						GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
						GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlUpdateResult.toString(), resultArgList);
						GenericServiceProxy.getTxDataSourceManager().commitTransaction();
							
						continue;
					}
					try
					{
						@SuppressWarnings("unchecked")
						List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
						if(result.size()<1)
						{
							//PRODUCTREQUEST-0049: MESSubProductionType not found
							throw new CustomException("PRODUCTREQUEST-0049");
						}
						subProductionType=result.get(0).get("DESCRIPTION").toString();
						ProductSpec productSpec = GenericServiceProxy.getSpecUtil().getProductSpec(factoryName, productSpecName, "00001");
						
						if(insertFlag)
						{
							if(!StringUtils.contains(productRequestState, "DLFL"))
							{
							    SuperProductRequest superProductRequest = new SuperProductRequest();
								superProductRequest.setProductRequestName(productRequestName);
								superProductRequest.setFactoryName(factoryName);
								superProductRequest.setProductRequestType(productionType);
								superProductRequest.setProductSpecName(productSpecName);
								superProductRequest.setProductSpecVersion("00001");
								if(factoryName.equals("POSTCELL"))
								{
								 superProductRequest.setProcessFlowName("");
								 superProductRequest.setProcessFlowVersion("");
								}else
								{
								 superProductRequest.setProcessFlowName(productSpec.getProcessFlowName());
								 superProductRequest.setProcessFlowVersion(productSpec.getProcessFlowVersion());
								}
								superProductRequest.setCreatedQuantity(0);
								superProductRequest.setPlanSequence("NA");
								superProductRequest.setSubProductionType(subProductionType);
								superProductRequest.setDescription(productRequestDesc);
								superProductRequest.setLastEventComment(eventInfo.getEventComment());
								superProductRequest.setLastEventTime(eventInfo.getEventTime());
								superProductRequest.setLastEventFlag("N");
								superProductRequest.setLastEventName(eventInfo.getEventName());
								superProductRequest.setLastEventTimeKey(eventInfo.getEventTimeKey());
								superProductRequest.setLastEventUser(eventInfo.getEventUser());
								superProductRequest.setCreateTime(eventInfo.getEventTime());
								superProductRequest.setCreateUser(eventInfo.getEventUser());
								superProductRequest.setProductRequestState(GenericServiceProxy.getConstantMap().Prq_Created);
								superProductRequest.setProductRequestHoldState("N");
								superProductRequest.setPlanQuantity(Long.parseLong(planQuantity));
								superProductRequest.setPlanReleasedTime(TimeUtils.getTimestamp(planReleasedTime));
								superProductRequest.setPlanFinishedTime(TimeUtils.getTimestamp(planFinishedTime));
								superProductRequest.setProductType(productType);
								if(StringUtil.equals(riskFlag, "X"))
								{
									superProductRequest.setRiskFlag("Y");
								}
								superProductRequest.setProjectProductRequestName(projectProductRequestName);
								superProductRequest.setCostDepartment(costDepartment);
									
								
								GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
								ExtendedObjectProxy.getSuperProductRequestService().create(eventInfo, superProductRequest);
								GenericServiceProxy.getTxDataSourceManager().commitTransaction();
							}
						}
						else
						{
							try
							{
								List<ProductRequest> productRequestList = ProductRequestServiceProxy.getProductRequestService().select(" superProductRequestName = ? and productRequestState=? ", new Object[] { productRequestName,"Released"});	
								for(ProductRequest productRequestData : productRequestList)
								{
									if(!StringUtils.equals(productRequestData.getPlanFinishedTime().toString().substring(0,10), TimeUtils.getTimestamp(planFinishedTime).toString().substring(0,10))||
											!StringUtils.equals(productRequestData.getPlanReleasedTime().toString().substring(0,10), TimeUtils.getTimestamp(planReleasedTime).toString().substring(0,10))	)
									{
										productRequestData.setPlanReleasedTime(TimeUtils.getTimestamp(planReleasedTime));
										productRequestData.setPlanFinishedTime(TimeUtils.getTimestamp(planFinishedTime));
										productRequestData.setLastEventComment("SAP Update WorkOrder Info");
										productRequestData.setLastEventTime(eventInfo.getEventTime());
										productRequestData.setLastEventName("UpdateWO");
										productRequestData.setLastEventTimeKey(eventInfo.getEventTimeKey());
										productRequestData.setLastEventUser(eventInfo.getEventUser());
										GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
										ProductRequestServiceProxy.getProductRequestService().update(productRequestData);
										GenericServiceProxy.getTxDataSourceManager().commitTransaction();
									}
								}
							}
							catch(NotFoundSignal n)
							{
								log.info("Not found Released ProductRequestList");
							}
							SuperProductRequest superProductRequest=ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new String[]{productRequestName});
							if(StringUtil.equals(superProductRequest.getProductRequestState(), "Created"))
							{								
								superProductRequest.setFactoryName(factoryName);
								superProductRequest.setProductRequestType(productionType);
								superProductRequest.setProductSpecName(productSpecName);
								superProductRequest.setProductSpecVersion("00001");
								superProductRequest.setSubProductionType(subProductionType);
								superProductRequest.setDescription(productRequestDesc);
								superProductRequest.setLastEventComment("SAP Update WorkOrder Info");
								superProductRequest.setLastEventTime(eventInfo.getEventTime());
								superProductRequest.setLastEventFlag("N");
								superProductRequest.setLastEventName("UpdateWO");
								superProductRequest.setLastEventTimeKey(eventInfo.getEventTimeKey());
								superProductRequest.setLastEventUser(eventInfo.getEventUser());
								superProductRequest.setPlanQuantity(Long.parseLong(planQuantity));
								superProductRequest.setPlanReleasedTime(TimeUtils.getTimestamp(planReleasedTime));
								superProductRequest.setPlanFinishedTime(TimeUtils.getTimestamp(planFinishedTime));
								superProductRequest.setProductType(productType);
								if(StringUtil.equals(riskFlag, "X"))
								{
									superProductRequest.setRiskFlag("Y");
								}
								superProductRequest.setProjectProductRequestName(projectProductRequestName);
								superProductRequest.setCostDepartment(costDepartment);
							}
							else
							{
								superProductRequest.setLastEventComment("SAP Update WorkOrder Info");
								superProductRequest.setLastEventTime(eventInfo.getEventTime());
								superProductRequest.setLastEventFlag("N");
								superProductRequest.setLastEventName(eventInfo.getEventName());
								superProductRequest.setLastEventTimeKey(eventInfo.getEventTimeKey());
								superProductRequest.setLastEventUser(eventInfo.getEventUser());
								superProductRequest.setPlanQuantity(Long.parseLong(planQuantity));
								superProductRequest.setPlanReleasedTime(TimeUtils.getTimestamp(planReleasedTime));
								superProductRequest.setPlanFinishedTime(TimeUtils.getTimestamp(planFinishedTime));
							}
							if(StringUtil.equals(superProductRequest.getProductRequestState(), "Created")
									&&StringUtils.contains(productRequestState, "DLFL"))
							{
								eventInfo.setEventName("Delete");
								eventInfo.setEventComment("Delete WO By ERP");
								GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
								ExtendedObjectProxy.getSuperProductRequestService().remove(eventInfo, superProductRequest);
								GenericServiceProxy.getTxDataSourceManager().commitTransaction();
							}
							else
							{
								GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
								ExtendedObjectProxy.getSuperProductRequestService().modify(eventInfo, superProductRequest);
								GenericServiceProxy.getTxDataSourceManager().commitTransaction();
							}
						}

								
						resultBindList.add("Y");
						resultBindList.add("S");
						resultBindList.add(eventInfo.getEventTimeKey()+" SUCCESS");
						resultBindList.add(productRequestName);
						resultArgList.add(resultBindList.toArray());
							
						GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
						GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlUpdateResult.toString(), resultArgList);
						GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					}
					catch (CustomException ce)
					{
						resultBindList.add("Y");
						resultBindList.add("E");
						resultBindList.add(eventInfo.getEventTimeKey()+" "+ce.errorDef.getLoc_errorMessage());
						resultBindList.add(productRequestName);
						resultArgList.add(resultBindList.toArray());
							
						GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
						GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlUpdateResult.toString(), resultArgList);
						GenericServiceProxy.getTxDataSourceManager().commitTransaction();
							
						continue;
					}
					catch (Exception e)
					{
						resultBindList.add("Y");
						resultBindList.add("E");
						resultBindList.add(eventInfo.getEventTimeKey()+" "+e.getMessage());
						resultBindList.add(productRequestName);
						resultArgList.add(resultBindList.toArray());
							
						GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
						GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlUpdateResult.toString(), resultArgList);
						GenericServiceProxy.getTxDataSourceManager().commitTransaction();
							
						continue;
					}
				}
					
			}
			
		}
	}
}
