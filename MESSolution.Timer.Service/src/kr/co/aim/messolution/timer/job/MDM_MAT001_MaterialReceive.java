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

import kr.co.aim.messolution.consumable.service.ConsumableServiceImpl;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DefectCode;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpecHistory;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpecHistoryKey;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpecKey;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.durable.management.data.DurableSpecHistory;
import kr.co.aim.greentrack.durable.management.data.DurableSpecHistoryKey;
import kr.co.aim.greentrack.durable.management.data.DurableSpecKey;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.orm.OrmMesEngine;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.ProductSpecService;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.data.ProductSpecHistory;
import kr.co.aim.greentrack.product.management.data.ProductSpecHistoryKey;
import kr.co.aim.greentrack.product.management.data.ProductSpecKey;
import kr.co.aim.greentrack.productspec.management.info.CreateInfo;

public class MDM_MAT001_MaterialReceive implements Job, InitializingBean {
	private static Log log = LogFactory.getLog(MDM_MAT001_MaterialReceive.class);

	@Override
	public void afterPropertiesSet() throws Exception
	{
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}

	public void execute(JobExecutionContext arg0) throws JobExecutionException
	{
		try
		{
			monitorMDM();
		}
		catch (CustomException e)
		{
			if (log.isDebugEnabled())
				log.error(e.errorDef.getLoc_errorMessage());
		}
	}

	public void monitorMDM() throws CustomException
	{
		Object[] bindArray = new Object[0];
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", "MDM", "MDM_MAT001", "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

		List<Object[]> resultArgList = new ArrayList<Object[]>();
		List<Object> resultBindList = new ArrayList<Object>();
		
		StringBuffer sqlResult = new StringBuffer();
		sqlResult.append("UPDATE MES_MDMIF_MAT001@OADBLINK  SET DATA_STATUS = ?, RESULTMESSAGE = ? WHERE MD_CODE = ? ");
		
		//ProductSpec
		StringBuffer sqlProductSpec = new StringBuffer();
		sqlProductSpec.append("SELECT MD_ID, MD_CODE, MD_CATE_CODE, MD_CATE_NAME, MD_DESCRIPTION, MATERIALGROUP, MATERIALGROUP_DESC, UNIT, UNIT_DESC,  ");
		sqlProductSpec.append("    WERKSID, VENDOR, ORG_CODE, MRPCONTROL, ROUNDVALUE ");
		sqlProductSpec.append("FROM MES_MDMIF_MAT001@OADBLINK   ");
		sqlProductSpec.append("WHERE MD_CATE_CODE IN ('HALB', 'FERT') ");
		sqlProductSpec.append("    AND DATA_STATUS = 'N' ");
		sqlProductSpec.append("    AND (WERKSID  IS NULL OR WERKSID <>'MOD') ");

		List<ListOrderedMap> resultProductSpec;

		String result = "";
		String resultMessage = "";
		String serviceName = "MES_MDMIF_MAT001";
		
		try
		{
			resultProductSpec = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlProductSpec.toString(), bindArray);
		}
		catch (FrameworkErrorSignal fe)
		{
			resultProductSpec = null;
		}
		
		if (resultProductSpec != null && resultProductSpec.size() > 0)
		{
			for (ListOrderedMap resultRowProductSpec : resultProductSpec)
			{
				String MD_ID = CommonUtil.getValue(resultRowProductSpec, "MD_ID");
				String factoryName = CommonUtil.getValue(resultRowProductSpec, "WERKSID");
				String productSpecName = CommonUtil.getValue(resultRowProductSpec, "MD_CODE");
				String mesproductSpecName = CommonUtil.getValue(resultRowProductSpec, "MD_CODE")+"E";
				String description = CommonUtil.getValue(resultRowProductSpec, "MD_DESCRIPTION");
				
				ProductSpec productSpecData = null;
				resultBindList.clear();
				
				if(StringUtils.equals(factoryName, "OLTPL"))
				{
					factoryName="OLED";
				}
				
				if(factoryName==null||StringUtils.isEmpty(factoryName))
				{
					resultBindList.add("E");
					resultBindList.add(eventInfo.getEventTimeKey()+" FactoryName is empty");
					resultBindList.add(productSpecName);
					resultArgList.add(resultBindList.toArray());
					
					GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
					GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResult.toString(), resultArgList);
					GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					
					result = "E";
					resultMessage = "FactoryName is empty";
					//CommonUtil.resultSendToMDM(MD_ID, MD_Code, MD_Desc, "MAT001", eventInfo, serviceName, resultMessage, result);
					continue;
				}
				
				if(!StringUtils.startsWith(productSpecName, "G3"))
				{
					resultBindList.add("E");
					resultBindList.add(eventInfo.getEventTimeKey()+" mismatch namingRule");
					resultBindList.add(productSpecName);
					resultArgList.add(resultBindList.toArray());
					
					GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
					GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResult.toString(), resultArgList);
					GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					
					result = "E";
					resultMessage = "FactoryName is empty";
					//CommonUtil.resultSendToMDM(MD_ID, MD_Code, MD_Desc, "MAT001", eventInfo, serviceName, resultMessage, result);
					continue;
				}
				
				try 
				{
					productSpecData = ProductServiceProxy.getProductSpecService().selectByKey(new ProductSpecKey(factoryName, mesproductSpecName, "00001"));
				} 
				catch (Exception e) 
				{
					productSpecData = null;
				}
				
				if(productSpecData != null)
				{
					/*
					eventInfo.setEventName("Modify");
					productSpecData.setDescription(description);
					
					ProductSpecHistory historyValue = new ProductSpecHistory();
					
					ProductSpecHistoryKey historyKey = new ProductSpecHistoryKey();
					historyKey.setFactoryName(productSpecData.getKey().getFactoryName());
					historyKey.setProductSpecName(productSpecData.getKey().getProductSpecName());
					historyKey.setProductSpecVersion(productSpecData.getKey().getProductSpecVersion());
					historyKey.setTimeKey(eventInfo.getEventTimeKey());

					historyValue.setKey(historyKey);

					historyValue.setEventTime(eventInfo.getEventTime());
					historyValue.setEventName(eventInfo.getEventName());
					historyValue.setProductionType(productSpecData.getProductionType());
					historyValue.setProductType(productSpecData.getProductType());
					historyValue.setProductQuantity(productSpecData.getProductQuantity());
					historyValue.setSubProductType(productSpecData.getSubProductType());
					historyValue.setSubProductUnitQuantity1(productSpecData.getSubProductUnitQuantity1());
					historyValue.setSubProductUnitQuantity2(productSpecData.getSubProductUnitQuantity2());
					historyValue.setProcessFlowName(productSpecData.getProcessFlowName());
					historyValue.setProcessFlowVersion(productSpecData.getProcessFlowVersion());
					historyValue.setEstimatedCycleTime(productSpecData.getEstimatedCycleTime());
					historyValue.setMultiProductSpecType(productSpecData.getMultiProductSpecType());
					historyValue.setProductSpec2Name(productSpecData.getProductSpec2Name());
					historyValue.setProductSpec2Version(productSpecData.getProductSpec2Version());
					historyValue.setEventUser(eventInfo.getEventUser());
					historyValue.setEventComment(eventInfo.getEventComment());
					historyValue.setUdfs(productSpecData.getUdfs());
                    */
					try 
					{
						/*
						GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
						ProductServiceProxy.getProductSpecService().update(productSpecData);
						ProductServiceProxy.getProductSpecHistoryService().insert(historyValue);
						GenericServiceProxy.getTxDataSourceManager().commitTransaction();
						*/
						resultBindList.add("S");
						resultBindList.add(eventInfo.getEventTimeKey()+" materialSpecName has been Created");
						resultBindList.add(productSpecName);
						resultArgList.add(resultBindList.toArray());
						
						GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
						GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResult.toString(), resultArgList);
						GenericServiceProxy.getTxDataSourceManager().commitTransaction();
						
						result = "S";
						resultMessage = "OK";
					} 
					catch (Exception e) 
					{
						GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
						
						resultBindList.add("E");
						resultBindList.add(eventInfo.getEventTimeKey()+e.getMessage());
						resultBindList.add(productSpecName);
						resultArgList.add(resultBindList.toArray());
						
						GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
						GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResult.toString(), resultArgList);
						GenericServiceProxy.getTxDataSourceManager().commitTransaction();
						
						result = "E";
						resultMessage = "DB Error:Update Failed";
					}
				}
				else
				{
					eventInfo.setEventName("Create");
					
					productSpecData = new ProductSpec();
					ProductSpecKey specKey = new ProductSpecKey(factoryName, mesproductSpecName, "00001");
					productSpecData.setKey(specKey);
					productSpecData.setCheckState(GenericServiceProxy.getConstantMap().Spec_CheckedIn);
					productSpecData.setActiveState(GenericServiceProxy.getConstantMap().Spec_NotActive);
					productSpecData.setCreateTime(eventInfo.getEventTime());
					productSpecData.setCreateUser(eventInfo.getEventUser());
					productSpecData.setProductionType("E");
					productSpecData.setDescription(description);
					if(StringUtils.equals(factoryName, "ARRAY"))
					{
						productSpecData.setProductType("Sheet");
					}
					else if(StringUtils.equals(factoryName, "OLED")||StringUtils.equals(factoryName, "TP"))
					{
						productSpecData.setProductType("Glass");
					}
					else if(StringUtils.equals(factoryName, "POSTCELL"))
					{
						productSpecData.setProductType("Panel");
					}
					
					ProductSpecHistory historyValue = new ProductSpecHistory();
					
					ProductSpecHistoryKey historyKey = new ProductSpecHistoryKey();
					historyKey.setFactoryName(productSpecData.getKey().getFactoryName());
					historyKey.setProductSpecName(productSpecData.getKey().getProductSpecName());
					historyKey.setProductSpecVersion(productSpecData.getKey().getProductSpecVersion());
					historyKey.setTimeKey(eventInfo.getEventTimeKey());

					historyValue.setKey(historyKey);
                    
					historyValue.setProductionType(productSpecData.getProductionType());
					historyValue.setProductType(productSpecData.getProductType());
					historyValue.setEventTime(eventInfo.getEventTime());
					historyValue.setEventName(eventInfo.getEventName());
					historyValue.setEventUser(eventInfo.getEventUser());
					historyValue.setEventComment(eventInfo.getEventComment());
					historyValue.setUdfs(productSpecData.getUdfs());

					try 
					{
						GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
						ProductServiceProxy.getProductSpecService().insert(productSpecData);
						ProductServiceProxy.getProductSpecHistoryService().insert(historyValue);
						GenericServiceProxy.getTxDataSourceManager().commitTransaction();
						
						resultBindList.add("S");
						resultBindList.add(eventInfo.getEventTimeKey()+" SUCCESS");
						resultBindList.add(productSpecName);
						resultArgList.add(resultBindList.toArray());
						
						GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
						GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResult.toString(), resultArgList);
						GenericServiceProxy.getTxDataSourceManager().commitTransaction();
						
						result = "S";
						resultMessage = "OK";
					} 
					catch (Exception e) 
					{
						GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
						
						resultBindList.add("E");
						resultBindList.add(eventInfo.getEventTimeKey()+e.getMessage());
						resultBindList.add(productSpecName);
						resultArgList.add(resultBindList.toArray());
						
						GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
						GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResult.toString(), resultArgList);
						GenericServiceProxy.getTxDataSourceManager().commitTransaction();
						
						result = "E";
						resultMessage = "DB Error:Update Failed";
					}
				}
				
				//CommonUtil.resultSendToMDM(MD_ID, MD_Code, MD_Desc, "MAT001", eventInfo, serviceName, resultMessage, result);
			}
		}
		
		//DurableSpec, ConsumableSpec
		StringBuffer sqlMaterialSpec = new StringBuffer();
		sqlMaterialSpec.append("SELECT MD_ID, MD_CODE, MD_CATE_CODE, MD_CATE_NAME, MD_DESCRIPTION, MATERIALGROUP, MATERIALGROUP_DESC, UNIT, UNIT_DESC,  ");
		sqlMaterialSpec.append("    WERKSID, VENDOR, ORG_CODE, MRPCONTROL, ROUNDVALUE ");
		sqlMaterialSpec.append("FROM MES_MDMIF_MAT001@OADBLINK   ");
		sqlMaterialSpec.append("WHERE MD_CATE_CODE NOT IN ('HALB', 'FERT') ");
		sqlMaterialSpec.append("    AND DATA_STATUS = 'N' ");
		sqlMaterialSpec.append("    AND (WERKSID  IS NULL OR WERKSID <>'MOD') ");


		List<ListOrderedMap> resultMaterialSpec;

		try
		{
			resultMaterialSpec = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlMaterialSpec.toString(), bindArray);
		}
		catch (FrameworkErrorSignal fe)
		{
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			resultMaterialSpec = null;
		}
		
		String[] factoryList={"ARRAY","OLED","TP","POSTCELL"};
		if (resultMaterialSpec != null && resultMaterialSpec.size() > 0)
		{
			StringBuffer sqlMaterialInfo = new StringBuffer();
			
			sqlMaterialInfo.append("SELECT MATERIALKIND, MATERIALTYPE, USEFLAG, DESCRIPTION, DEPARTMENT, GROUPNAME  ");
			sqlMaterialInfo.append("FROM CT_MATERIALINFO ");
			sqlMaterialInfo.append("WHERE SAPMATERIALTYPE = ? AND FACTORYNAME= ? ");
			
			for (ListOrderedMap resultRowMaterialSpec : resultMaterialSpec)
			{
				boolean findFlag=false;
				for(int i=0;i<factoryList.length;i++)
				{					
					String MD_ID = CommonUtil.getValue(resultRowMaterialSpec, "MD_ID");
					String materialSpecName = CommonUtil.getValue(resultRowMaterialSpec, "MD_CODE");
					String sapMaterialType = CommonUtil.getValue(resultRowMaterialSpec, "MATERIALGROUP");
					String factoryName = factoryList[i];
					String vendor = CommonUtil.getValue(resultRowMaterialSpec, "VENDOR");
					String MD_Desc = CommonUtil.getValue(resultRowMaterialSpec, "MD_DESCRIPTION");
					
					String[] materialBind = new String[]{ sapMaterialType,factoryName };
					List<ListOrderedMap> resultMaterialInfo;
					resultBindList.clear();
					
					try
					{
						resultMaterialInfo = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlMaterialInfo.toString(), materialBind);
					}
					catch (FrameworkErrorSignal fe)
					{
						resultBindList.add("E");
						resultBindList.add(eventInfo.getEventTimeKey()+" MaterialGroup Not Exists,Skip By MES"); 
						resultBindList.add(materialSpecName);
						resultArgList.add(resultBindList.toArray());
						
						GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
						GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResult.toString(), resultArgList);
						GenericServiceProxy.getTxDataSourceManager().commitTransaction();
						
						result = "S";
						resultMessage = "MaterialGroup Not Exists,Skip By MES";
						//CommonUtil.resultSendToMDM(MD_ID, MD_Code, MD_Desc, "MAT001", eventInfo, serviceName, resultMessage, result);
						continue;
					}
					if((resultMaterialInfo==null || resultMaterialInfo.size()<1) &&!findFlag&&i==3)
					{
						resultBindList.add("E");
						resultBindList.add(eventInfo.getEventTimeKey()+" MaterialGroup Not Exists,Skip By MES"); 
						resultBindList.add(materialSpecName);
						resultArgList.add(resultBindList.toArray());
						
						GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
						GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResult.toString(), resultArgList);
						GenericServiceProxy.getTxDataSourceManager().commitTransaction();
						
						result = "S";
						resultMessage = "MaterialGroup Not Exists,Skip By MES";
						//CommonUtil.resultSendToMDM(MD_ID, MD_Code, MD_Desc, "MAT001", eventInfo, serviceName, resultMessage, result);
						continue;
					}
					
					if((resultMaterialInfo==null || resultMaterialInfo.size()<1))
					{
						continue;
					}
					
					findFlag=true;
					String materialKind = CommonUtil.getValue(resultMaterialInfo.get(0), "MATERIALKIND");
					String materialType = CommonUtil.getValue(resultMaterialInfo.get(0), "MATERIALTYPE");
					
					if(StringUtils.equals(sapMaterialType, "1000130")||StringUtils.equals(sapMaterialType, "1000221"))
					{
						materialType="UNKNOWN";
					}
					
					if(StringUtil.equals(materialKind, "Consumable"))
					{
						String consumeUnit = CommonUtil.getValue(resultRowMaterialSpec, "UNIT");
						String minQtyUnit = CommonUtil.getValue(resultMaterialInfo.get(0), "ROUNDVALUE");
						
						ConsumableSpec consumableSpec = null;
						
						try 
						{
							consumableSpec =GenericServiceProxy.getSpecUtil().getMaterialSpec(factoryName, materialSpecName, "00001");
						} 
						catch (Exception e) 
						{
							consumableSpec = null;
						}
						
						if(consumableSpec != null)
						{
							/*
							eventInfo.setEventName("Modify");
							consumableSpec.setDescription(MD_Desc);
							consumableSpec.setConsumeUnit(consumeUnit);
							
							Map<String, String> udfs = consumableSpec.getUdfs();
							//udfs.put("MINQTYUNIT", minQtyUnit);
							udfs.put("VENDOR", vendor);
							consumableSpec.setUdfs(udfs);
							
							ConsumableSpecHistory historyValue = new ConsumableSpecHistory();
							
							ConsumableSpecHistoryKey historyKey = new ConsumableSpecHistoryKey();
							historyKey.setFactoryName(consumableSpec.getKey().getFactoryName());
							historyKey.setConsumableSpecName(consumableSpec.getKey().getConsumableSpecName());
							historyKey.setConsumableSpecVersion(consumableSpec.getKey().getConsumableSpecVersion());
							historyKey.setTimeKey(eventInfo.getEventTimeKey());

							historyValue.setKey(historyKey);

							historyValue.setEventTime(eventInfo.getEventTime());
							historyValue.setEventName(eventInfo.getEventName());
							historyValue.setConsumableType(consumableSpec.getConsumableType());
							historyValue.setConsumeUnit(consumableSpec.getConsumeUnit());
							historyValue.setEventUser(eventInfo.getEventUser());
							historyValue.setEventComment(eventInfo.getEventComment());
							historyValue.setUdfs(udfs);
                            */
							try 
							{
								/*
								GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
								ConsumableServiceProxy.getConsumableSpecService().update(consumableSpec);
								ConsumableServiceProxy.getConsumableSpecHistoryService().insert(historyValue);
								GenericServiceProxy.getTxDataSourceManager().commitTransaction();
								*/
								resultBindList.add("S");
								resultBindList.add(eventInfo.getEventTimeKey()+" materialSpecName has been created");
								resultBindList.add(materialSpecName);
								resultArgList.add(resultBindList.toArray());
								
								GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
								GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResult.toString(), resultArgList);
								GenericServiceProxy.getTxDataSourceManager().commitTransaction();
								
								result = "S";
								resultMessage = "OK";
							} 
							catch (Exception e) 
							{
								GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
								
								resultBindList.add("E");
								resultBindList.add(eventInfo.getEventTimeKey()+e.getMessage());
								resultBindList.add(materialSpecName);
								resultArgList.add(resultBindList.toArray());
								
								GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
								GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResult.toString(), resultArgList);
								GenericServiceProxy.getTxDataSourceManager().commitTransaction();
								
								result = "E";
								resultMessage ="DB Error:Update Failed";
							}
						}
						else
						{

							eventInfo.setEventName("Create");
							
							consumableSpec = new ConsumableSpec();
							ConsumableSpecKey specKey = new ConsumableSpecKey();
							specKey.setConsumableSpecName(materialSpecName);
							specKey.setConsumableSpecVersion("00001");
							specKey.setFactoryName(factoryName);
							
							consumableSpec.setKey(specKey);
							consumableSpec.setDescription(MD_Desc);
							consumableSpec.setCheckState(GenericServiceProxy.getConstantMap().Spec_CheckedIn);
							consumableSpec.setActiveState(GenericServiceProxy.getConstantMap().Spec_NotActive);
							consumableSpec.setCreateTime(eventInfo.getEventTime());
							consumableSpec.setCreateUser(eventInfo.getEventUser());
							consumableSpec.setConsumableType(materialType);
							consumableSpec.setConsumeUnit(consumeUnit);

							Map<String, String> udfs = new HashMap<String, String>();
							//udfs.put("MINQTYUNIT", minQtyUnit);
							udfs.put("VENDOR", vendor);
							consumableSpec.setUdfs(udfs);
							
							ConsumableSpecHistory historyValue = new ConsumableSpecHistory();
							
							ConsumableSpecHistoryKey historyKey = new ConsumableSpecHistoryKey();
							historyKey.setFactoryName(consumableSpec.getKey().getFactoryName());
							historyKey.setConsumableSpecName(consumableSpec.getKey().getConsumableSpecName());
							historyKey.setConsumableSpecVersion(consumableSpec.getKey().getConsumableSpecVersion());
							historyKey.setTimeKey(eventInfo.getEventTimeKey());

							historyValue.setKey(historyKey);
							historyValue.setConsumableType(consumableSpec.getConsumableType());
							historyValue.setConsumeUnit(consumableSpec.getConsumeUnit());					
							historyValue.setEventTime(eventInfo.getEventTime());
							historyValue.setEventName(eventInfo.getEventName());
							historyValue.setEventUser(eventInfo.getEventUser());
							historyValue.setEventComment(eventInfo.getEventComment());
							historyValue.setUdfs(consumableSpec.getUdfs());

							try 
							{
								GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
								ConsumableServiceProxy.getConsumableSpecService().insert(consumableSpec);
								ConsumableServiceProxy.getConsumableSpecHistoryService().insert(historyValue);
								GenericServiceProxy.getTxDataSourceManager().commitTransaction();
								
								resultBindList.add("S");
								resultBindList.add(eventInfo.getEventTimeKey()+" SUCCESS");
								resultBindList.add(materialSpecName);
								resultArgList.add(resultBindList.toArray());
								
								GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
								GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResult.toString(), resultArgList);
								GenericServiceProxy.getTxDataSourceManager().commitTransaction();
								
								result = "S";
								resultMessage = "OK";
							} 
							catch (Exception e) 
							{
								GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
								
								resultBindList.add("E");
								resultBindList.add(eventInfo.getEventTimeKey()+e.getMessage());
								resultBindList.add(materialSpecName);
								resultArgList.add(resultBindList.toArray());
								
								GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
								GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResult.toString(), resultArgList);
								GenericServiceProxy.getTxDataSourceManager().commitTransaction();
								
								result = "E";
								resultMessage = "DB Error:Create Failed";
							}
						}				
					}
					else //Durable
					{
						DurableSpecKey durableSpecKey = new DurableSpecKey();
						durableSpecKey.setFactoryName(factoryName);
						durableSpecKey.setDurableSpecName(materialSpecName);
						durableSpecKey.setDurableSpecVersion("00001");
						
						DurableSpec durableSpec = null;
						
						try 
						{
							durableSpec = DurableServiceProxy.getDurableSpecService().selectByKey(durableSpecKey);
						} 
						catch (Exception e) 
						{
							durableSpec = null;
						}
						
						if(durableSpec != null)
						{
							eventInfo.setEventName("Modify");
							durableSpec.setDescription(MD_Desc);
							
							Map<String, String> udfs = durableSpec.getUdfs();
							udfs.put("VENDOR", vendor);
							durableSpec.setUdfs(udfs);
							
							DurableSpecHistory historyValue = new DurableSpecHistory();
							
							DurableSpecHistoryKey historyKey = new DurableSpecHistoryKey();
							historyKey.setFactoryName(durableSpec.getKey().getFactoryName());
							historyKey.setDurableSpecName(durableSpec.getKey().getDurableSpecName());
							historyKey.setDurableSpecVersion(durableSpec.getKey().getDurableSpecVersion());
							historyKey.setTimeKey(eventInfo.getEventTimeKey());

							historyValue.setKey(historyKey);
							historyValue.setDefaultCapacity(durableSpec.getDefaultCapacity());
							historyValue.setDurableType(materialType);
							historyValue.setDurationUsedLimit(durableSpec.getDurationUsedLimit());
							historyValue.setTimeUsedLimit(durableSpec.getTimeUsedLimit());
							historyValue.setEventTime(eventInfo.getEventTime());
							historyValue.setEventName(eventInfo.getEventName());
							historyValue.setEventUser(eventInfo.getEventUser());
							historyValue.setEventComment(eventInfo.getEventComment());
							historyValue.setUdfs(udfs);

							try 
							{
								GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
								DurableServiceProxy.getDurableSpecService().update(durableSpec);
								DurableServiceProxy.getDurableSpecHistoryService().insert(historyValue);
								GenericServiceProxy.getTxDataSourceManager().commitTransaction();
								
								resultBindList.add("S");
								resultBindList.add(eventInfo.getEventTimeKey()+"SUCCESS");
								resultBindList.add(materialSpecName);
								resultArgList.add(resultBindList.toArray());
								
								GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
								GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResult.toString(), resultArgList);
								GenericServiceProxy.getTxDataSourceManager().commitTransaction();
								
								result = "S";
								resultMessage = "OK";
							} 
							catch (Exception e) 
							{
								GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
								
								resultBindList.add("E");
								resultBindList.add(eventInfo.getEventTimeKey()+e.getMessage());
								resultBindList.add(materialSpecName);
								resultArgList.add(resultBindList.toArray());
								
								GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
								GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResult.toString(), resultArgList);
								GenericServiceProxy.getTxDataSourceManager().commitTransaction();
								
								result = "E";
								resultMessage = "DB Error:Update Failed";
							}
						}
						else
						{

							eventInfo.setEventName("Create");
							
							durableSpec = new DurableSpec();
							DurableSpecKey specKey = new DurableSpecKey();
							specKey.setDurableSpecName(materialSpecName);
							specKey.setDurableSpecVersion("00001");
							specKey.setFactoryName(factoryName);
							
							durableSpec.setKey(specKey);
							durableSpec.setDescription(MD_Desc);
							durableSpec.setCheckState(GenericServiceProxy.getConstantMap().Spec_CheckedIn);
							durableSpec.setActiveState(GenericServiceProxy.getConstantMap().Spec_NotActive);
							durableSpec.setCreateTime(eventInfo.getEventTime());
							durableSpec.setCreateUser(eventInfo.getEventUser());
							durableSpec.setDurableType(materialType);

							Map<String, String> udfs = new HashMap<String, String>();
							udfs.put("VENDOR", vendor);
							durableSpec.setUdfs(udfs);
							
							DurableSpecHistory historyValue = new DurableSpecHistory();
							
							DurableSpecHistoryKey historyKey = new DurableSpecHistoryKey();
							historyKey.setFactoryName(durableSpec.getKey().getFactoryName());
							historyKey.setDurableSpecName(durableSpec.getKey().getDurableSpecName());
							historyKey.setDurableSpecVersion(durableSpec.getKey().getDurableSpecVersion());
							historyKey.setTimeKey(eventInfo.getEventTimeKey());

							historyValue.setKey(historyKey);
							historyValue.setDurableType(durableSpec.getDurableType());
							historyValue.setEventTime(eventInfo.getEventTime());
							historyValue.setEventName(eventInfo.getEventName());
							historyValue.setEventUser(eventInfo.getEventUser());
							historyValue.setEventComment(eventInfo.getEventComment());
							historyValue.setUdfs(durableSpec.getUdfs());

							try 
							{
								GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
								DurableServiceProxy.getDurableSpecService().insert(durableSpec);
								DurableServiceProxy.getDurableSpecHistoryService().insert(historyValue);
								GenericServiceProxy.getTxDataSourceManager().commitTransaction();
								
								resultBindList.add("S");
								resultBindList.add(eventInfo.getEventTimeKey()+"SUCCESS");
								resultBindList.add(materialSpecName);
								resultArgList.add(resultBindList.toArray());
								
								GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
								GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResult.toString(), resultArgList);
								GenericServiceProxy.getTxDataSourceManager().commitTransaction();
								
								result = "S";
								resultMessage = "OK";
							} 
							catch (Exception e) 
							{
								GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
								
								resultBindList.add("E");
								resultBindList.add(eventInfo.getEventTimeKey()+e.getMessage());
								resultBindList.add(materialSpecName);
								resultArgList.add(resultBindList.toArray());
								
								GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
								GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResult.toString(), resultArgList);
								GenericServiceProxy.getTxDataSourceManager().commitTransaction();
								
								result = "E";
								resultMessage = "DB Error:Update Failed";
							}
						}				
					}
					
					//CommonUtil.resultSendToMDM(MD_ID, MD_Code, MD_Desc, "MAT001", eventInfo, serviceName, resultMessage, result);
				}
				
			}
		}
	}
}
