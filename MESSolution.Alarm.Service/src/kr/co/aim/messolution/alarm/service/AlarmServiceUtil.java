package kr.co.aim.messolution.alarm.service;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AlarmActionDef;
import kr.co.aim.messolution.extended.object.management.data.LotFutureAction;
import kr.co.aim.messolution.extended.object.management.data.MaskFutureAction;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.MaskMultiHold;
import kr.co.aim.messolution.extended.object.management.data.ProductFutureAction;
import kr.co.aim.messolution.extended.object.management.data.SampleMask;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.orm.ObjectAttributeDef;
import kr.co.aim.greentrack.alarm.AlarmServiceProxy;
import kr.co.aim.greentrack.alarm.management.data.AlarmDefinition;
import kr.co.aim.greentrack.alarm.management.data.AlarmDefinitionKey;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.lot.management.policy.util.LotHistoryDataAdaptor;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;
import kr.co.aim.greentrack.name.NameServiceProxy;
import kr.co.aim.greentrack.name.management.data.NameGeneratorRuleDefKey;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.NodeKey;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class AlarmServiceUtil implements ApplicationContextAware {
	private ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog("AlarmServiceUtil");


	public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		applicationContext = arg0;
	}
	
	public void lotHoldAction(Object dataInfo , AlarmActionDef actionData, Machine machineData, EventInfo eventInfo, List<Element> productList) throws CustomException
	{
		List<String> list = new ArrayList<String>();
		eventInfo.setEventComment("AlarmHold");
		
		String alarmCode =  this.getAlarmTableFieldValue(dataInfo, "AlarmCode");
		String alarmType =  this.getAlarmTableFieldValue(dataInfo, "AlarmType");
		String alarmIndex = this.getAlarmTableFieldValue(dataInfo, "AlarmIndex");
		String releaseType = actionData.getReleaseType();
		
		for (Element productE : productList)
		{
			String productName = productE.getChildText("PRODUCTNAME");
			String sql1 = "SELECT PRODUCTNAME ,LOTNAME FROM PRODUCT WHERE PRODUCTNAME = :PRODUCTNAME AND PRODUCTSTATE = 'InProduction'";
			Map<String, Object> bindMap1 = new HashMap<String, Object>();
			bindMap1.put("PRODUCTNAME", productName);
			List<Map<String, Object>> Result1 = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql1, bindMap1);
			if (Result1.size() > 0 && Result1 != null)
			{
				String lotName = Result1.get(0).get("LOTNAME").toString();
				list.add(lotName);
			}
		}
		if (list.size() > 0)
		{
			List newList = new ArrayList(new TreeSet(list));
			for (int i = 0; i < newList.size(); i++)
			{
				String lotName = newList.get(i).toString();
				String sql2 = "SELECT LOTNAME,LOTPROCESSSTATE,JOBDOWNFLAG FROM LOT WHERE LOTNAME = :LOTNAME AND LOTSTATE = 'Released'";
				Map<String, Object> bindMap2 = new HashMap<String, Object>();
				bindMap2.put("LOTNAME", lotName);
				List<Map<String, Object>> Result2 = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql2, bindMap2);
				if (Result2.size() > 0)
				{
					String lotProcessState = Result2.get(0).get("LOTPROCESSSTATE").toString();
					String jobDownFlag = "";
					if(Result2.get(0).get("JOBDOWNFLAG")!=null)
					{
						jobDownFlag=Result2.get(0).get("JOBDOWNFLAG").toString();
					}	
					if (StringUtil.equals(lotProcessState, "WAIT") && !jobDownFlag.equals("Y"))
					{
						Lot postLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
						String sql3 = "SELECT LOTNAME,REASONCODE FROM LOTMULTIHOLD WHERE LOTNAME = :LOTNAME ";
						List<Map<String, Object>> listResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql3, bindMap2);
						if (listResult.size() <= 0)
						{
							postLotData.getUdfs().put("ALARMINDEX", alarmIndex);
							postLotData.getUdfs().put("ACTIONTYPE", alarmType);
							postLotData.getUdfs().put("RELEASETYPE",releaseType);
							
							// LotMultiHold
							MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, postLotData, postLotData.getUdfs());
						}
					}
					else
					{
						try
						{
							Lot postLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
							if (alarmType.equals("FDC"))
							{
								MESLotServiceProxy.getLotServiceImpl().insertCtLotFutureMultiHoldActionForAfter(eventInfo, lotName, postLotData.getFactoryName(), postLotData.getProcessFlowName(),
										postLotData.getProcessFlowVersion(), postLotData.getProcessOperationName(), postLotData.getProcessOperationVersion(), "0", "hold", "System", "HOLD", "FDC", "",
										"", "", "False", "True", "", eventInfo.getEventComment(), "", eventInfo.getEventUser(), "Insert", "", "", "", releaseType, alarmIndex);
							}
							if (alarmType.equals("SPC"))
							{
								MESLotServiceProxy.getLotServiceImpl().insertCtLotFutureMultiHoldActionForAfter(eventInfo, lotName, postLotData.getFactoryName(), postLotData.getProcessFlowName(),
										postLotData.getProcessFlowVersion(), postLotData.getProcessOperationName(), postLotData.getProcessOperationVersion(), "0", "hold", "System", "HOLD", "SPC", "",
										"", "", "False", "True", "", eventInfo.getEventComment(), "", eventInfo.getEventUser(), "Insert", "", "", "", releaseType, alarmIndex);
							}
							if (!alarmType.equals("SPC") && !alarmType.equals("FDC"))
							{
								MESLotServiceProxy.getLotServiceImpl().insertCtLotFutureMultiHoldActionForAfter(eventInfo, lotName, postLotData.getFactoryName(), postLotData.getProcessFlowName(),
										postLotData.getProcessFlowVersion(), postLotData.getProcessOperationName(), postLotData.getProcessOperationVersion(), "0", "hold", "System", "HOLD", "Alarm",
										"", "", "", "False", "True", "", eventInfo.getEventComment(), "", eventInfo.getEventUser(), "Insert", "", "", "", releaseType, alarmIndex);

							}
						}
						catch (greenFrameDBErrorSignal e)
						{
							log.info(lotName + " insert to lot_fucture action fail ! ");
						}
					}
				}
			}
		}
	}
	
	public String getAlarmTableFieldValue(Object obj,String fieldName)
	{
		if(obj == null) 
		{
			log.info(String.format("[%s]Function: Input argument is null. ",Thread.currentThread().getStackTrace()[1].getMethodName()));
			return "";
		}
		
		Field[] fields = obj.getClass().getDeclaredFields();
		for(Field field : fields)
		{
			if(field.getName().toUpperCase().equals(fieldName.toUpperCase()))
			{
				field.setAccessible(true);
				try {
					return field.get(obj).toString();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		log.info(String.format("Do not found object field. [Object = %s , Field = %s] ",obj.getClass().getSimpleName(),fieldName));
		return "";
	}
	
	public void updateNotice(String title, String content, String factoryName, String nlsType) throws CustomException
	{
		String sql = "UPDATE CT_NLSDATA SET english= :title, chinese= :content " + "WHERE nlsname = :factoryName and nlstype= :nlsType ";
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("title", title);
		bindMap.put("content", content);
		bindMap.put("factoryName", factoryName);
		bindMap.put("nlsType", nlsType);
		
		try
		{
			GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", e.getMessage());
		}
		
	}

	public Map<String, String> setNamedValueSequence(Element element) throws FrameworkErrorSignal, NotFoundSignal
	{
		Map<String, String> namedValuemap = new HashMap<String, String>();

		List<ObjectAttributeDef> objectAttributeDefs = greenFrameServiceProxy.getObjectAttributeMap().getAttributeNames("Alarm", "ExtendedC");

		log.info("UDF SIZE=" + objectAttributeDefs.size());

		if (objectAttributeDefs != null)
		{
			for (int i = 0; i < objectAttributeDefs.size(); i++)
			{
				String name = "";
				String value = "";

				if (element != null)
				{
					for (int j = 0; j < element.getContentSize(); j++)
					{
						if (element.getChildText(objectAttributeDefs.get(i).getAttributeName()) != null)
						{
							name = objectAttributeDefs.get(i).getAttributeName();
							value = element.getChildText(objectAttributeDefs.get(i).getAttributeName());

							log.info("AttributName : " + i + " " + objectAttributeDefs.get(i).getAttributeName());
							log.info("ElementText : " + i + " " + element.getChildText(objectAttributeDefs.get(i).getAttributeName()));

							break;
						}
						else
						{
							name = objectAttributeDefs.get(i).getAttributeName();
						}
					}
				}
				else
				{

				}

				if (name.equals("") != true)
					namedValuemap.put(name, value);
			}
		}

		log.info("UDF SIZE=" + namedValuemap.size());
		return namedValuemap;
	}

	public List<String> generateAlarmId(String alarmDefName, String quantity)
	{
		if (log.isInfoEnabled())
		{
			log.info("alarmDefName = " + alarmDefName);
			log.info("quantity = " + quantity);
		}

		log.info("Generate alarm Name");

		ArrayList<String> list = new ArrayList<String>();

		int count = Integer.parseInt(quantity);

		NameGeneratorRuleDefKey nameGeneratorRuleDefKey = new NameGeneratorRuleDefKey();
		nameGeneratorRuleDefKey.setRuleName("ALARMID");

		try
		{
			NameServiceProxy.getNameGeneratorRuleDefService().selectByKey(nameGeneratorRuleDefKey);
		}
		catch (FrameworkErrorSignal e)
		{
			e.printStackTrace();
		}
		catch (NotFoundSignal e)
		{
			e.printStackTrace();
		}

		List<String> argSeq = new ArrayList<String>();
		argSeq.add(alarmDefName);

		List<String> alarmIdList = null;

		try
		{
			alarmIdList = NameServiceProxy.getNameGeneratorRuleDefService().generateName("ALARMID", argSeq, count);
		}
		catch (FrameworkErrorSignal e)
		{
			log.error(e);
		}
		catch (NotFoundSignal e)
		{
		}

		log.info("AlarmIDList Length : " + alarmIdList.size());

		for (int i = 0; i < count; i++)
		{
			list.add(alarmIdList.get(i));
			log.info(list.get(i));
		}
		return list;
	}

	public AlarmDefinition getAlarmDefinitionData(String alarmId)
	{
		if (log.isInfoEnabled())
		{
			log.info("alarmId = " + alarmId);
		}

		AlarmDefinitionKey key = new AlarmDefinitionKey();
		key.setAlarmId(alarmId);
		AlarmDefinition alarmDefinition = new AlarmDefinition();
		try
		{
			alarmDefinition = AlarmServiceProxy.getAlarmDefinitionService().selectByKey(key);
		}
		catch (Exception e)
		{

		}

		return alarmDefinition;
	}

	public void lotHoldAction(String machineName, EventInfo eventInfo, List<Element> productList, String alarmType,String productComment,String unitName,String item) throws CustomException
	{
		List<String> list = new ArrayList<String>();
		List<String> productNameList=new ArrayList<String>();
		

		for (Element productE : productList)
		{
			String productName = productE.getChildText("PRODUCTNAME");
			Boolean rsMeterFlag=false;
			if(productNameList.contains(productName))
			{
				continue;
			}
			productNameList.add(productName);
			Product productData=new Product();
			Lot LotData=new Lot();
			try
			{
				productData = ProductServiceProxy.getProductService().selectByKey(new ProductKey(productName));
			    LotData =  LotServiceProxy.getLotService().selectByKey(new LotKey(productData.getLotName()));
				ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(LotData.getFactoryName(), LotData.getProcessOperationName(), LotData.getProcessOperationVersion());
				if(StringUtils.equals(operationData.getDetailProcessOperationType(), "RS"))
				{
					rsMeterFlag=true;
				}
			}
			catch(NotFoundSignal n)
			{
				log.error("Not Found Product: "+productName);
				continue;
			}
			catch (FrameworkErrorSignal e) 
			{
				log.error("Not Found Product: "+productName);
				continue;
			}

			try
			{
				if(LotData.getLotState().equals("Released")&&productData.getProductState().equals("InProduction"))
				{
					if(StringUtils.equals(LotData.getLotProcessState(), "WAIT")&&!StringUtils.equals(LotData.getUdfs().get("JOBDOWNFLAG"), "Y"))
					{
						if(!list.contains(productData.getLotName()))
						{
							list.add(productData.getLotName());
						}
						rsMeterFlag=false;
					}
					else
					{
						eventInfo.setEventComment(productComment);
						ProductFutureAction actionData 
						= ExtendedObjectProxy.getProductFutureActionService().selectByKey(false, 
									new Object[] {productName,productData.getFactoryName(),productData.getProcessFlowName(),productData.getProcessFlowVersion(),
											productData.getProcessOperationName(),productData.getProcessOperationVersion(),unitName,item});
						ExtendedObjectProxy.getProductFutureActionService().updateProductFutureAction(eventInfo, productName, productData.getFactoryName(), productData.getProcessFlowName(),
								productData.getProcessFlowVersion(), productData.getProcessOperationName(), productData.getProcessOperationVersion(), 
								unitName, item, "HOLD", "hold", "System", productData.getLotName());
					}
				}
				else
				{
					log.info("LotState is not Released or productState is not InProduction");
				}
			}
			catch (greenFrameDBErrorSignal no) 
			{
				log.info("Not Found productFutureAction: "+productName);
				eventInfo.setEventComment(productComment);
				ExtendedObjectProxy.getProductFutureActionService().insertProductFutureAction(eventInfo, productName, productData.getFactoryName(),
						productData.getProcessFlowName(), productData.getProcessFlowVersion(), productData.getProcessOperationName(),
						productData.getProcessOperationVersion(), unitName, item, "HOLD", "hold", "System", productData.getLotName());
			}
			if(rsMeterFlag)
			{
				ProcessFlow mainFlowData = MESLotServiceProxy.getLotInfoUtil().getMainProcessFlowData(LotData);
				String sqlForDEOper= "SELECT N.NODEATTRIBUTE1,N.PROCESSFLOWNAME FROM ENUMDEFVALUE EN,NODE N "
						+ " WHERE EN.ENUMVALUE=:ENUMVALUE "
						+ " AND EN.DESCRIPTION=N.NODEATTRIBUTE1  "
						+ " AND N.PROCESSFLOWNAME=:PROCESSFLOWNAME "
				        + " AND EN.ENUMNAME='SPCRSOperMappling' ";
					
				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("ENUMVALUE",LotData.getProcessOperationName());
				bindMap.put("PROCESSFLOWNAME",mainFlowData.getKey().getProcessFlowName());
				
				List<Map<String, Object>> DEOperList = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlForDEOper, bindMap);
				if(DEOperList!=null&&DEOperList.size()>0)
				{
					try
					{
						eventInfo.setEventComment(productComment);
						ProductFutureAction actionData 
						= ExtendedObjectProxy.getProductFutureActionService().selectByKey(false, 
									new Object[] {productName,productData.getFactoryName(),mainFlowData.getKey().getProcessFlowName(),"00001",
											DEOperList.get(0).get("NODEATTRIBUTE1").toString(),"00001",unitName,item});
						ExtendedObjectProxy.getProductFutureActionService().updateProductFutureAction(eventInfo, productName, productData.getFactoryName(), mainFlowData.getKey().getProcessFlowName(),
								"00001", DEOperList.get(0).get("NODEATTRIBUTE1").toString(), "00001", 
								unitName, item, "RS-DE", "hold", "System", productData.getLotName());
					}
					catch(greenFrameDBErrorSignal no)
					{
						log.info("Not Found productFutureAction: "+productName);
						eventInfo.setEventComment(productComment);
						ExtendedObjectProxy.getProductFutureActionService().insertProductFutureAction(eventInfo, productName, productData.getFactoryName(),
								mainFlowData.getKey().getProcessFlowName(), "00001", DEOperList.get(0).get("NODEATTRIBUTE1").toString(),
								"00001", unitName, item, "RS-DE", "hold", "System", productData.getLotName());
					}
				}				
			}

		}
		if (list.size() > 0)
		{
			List newList = new ArrayList(new TreeSet(list));
			for (int i = 0; i < newList.size(); i++)
			{
				String lotName = newList.get(i).toString();
				Lot postLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
				String sql3 = "SELECT LOTNAME,REASONCODE FROM LOTMULTIHOLD WHERE LOTNAME = :LOTNAME AND REASONCODE=:REASONCODE";
				Map<String, Object> bindMap2 = new HashMap<String, Object>();
				eventInfo.setEventComment("AlarmHold");
				if (alarmType.equals("FDC"))
				{
					eventInfo.setReasonCode("FDCAlarm");
				}
				if (alarmType.equals("SPC"))
				{
					eventInfo.setReasonCode("SPCAlarm");
				}
				if (!alarmType.equals("SPC") && !alarmType.equals("FDC"))
				{
					eventInfo.setReasonCode("AlarmHold");
				}
				eventInfo.setReasonCodeType("HOLD");
				
				bindMap2.put("LOTNAME", lotName);
				bindMap2.put("REASONCODE", eventInfo.getReasonCode());
				List<Map<String, Object>> listResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql3, bindMap2);

				// LotMultiHold
				if(listResult!=null&&listResult.size()<=0)
				{
					eventInfo.setEventComment("SPCHOLD: " +productComment);
					MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, postLotData, postLotData.getUdfs());	
				}				
			}
		}
	}
	
	//houxk 20210621
	public void maskLotHoldAction(String machineName, EventInfo eventInfo, List<Element> productList, String alarmType,String productComment,String unitName,String item) throws CustomException
	{
		List<String> maskNameList=new ArrayList<String>();
		List<String> list = new ArrayList<String>();
		
		for (Element productE : productList)
		{
			String maskLotName = productE.getChildText("PRODUCTNAME");
			//Boolean rsMeterFlag=false;
			if(maskNameList.contains(maskLotName))
			{
				continue;
			}
			maskNameList.add(maskLotName);
			
			MaskLot maskLotData = new MaskLot();
			
			try
			{
				maskLotData = ExtendedObjectProxy.getMaskLotService().getMaskLotData(maskLotName);
				ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(maskLotData.getFactoryName(), maskLotData.getMaskProcessOperationName(), maskLotData.getMaskProcessOperationVersion());
				if(StringUtils.equals(operationData.getDetailProcessOperationType(), "RS"))
				{
					//rsMeterFlag=true;
				}
			}
			catch(NotFoundSignal n)
			{
				log.error("Not Found MaskLot: "+maskLotName);
				continue;
			}
			catch (FrameworkErrorSignal e) 
			{
				log.error("Not Found MaskLot: "+maskLotName);
				continue;
			}									

			if(maskLotData.getMaskLotState().equals("Released"))
			{
				if(StringUtils.equals(maskLotData.getMaskLotProcessState(), "WAIT")&&!StringUtils.equals(maskLotData.getJobDownFlag(), "Y"))
				{
					if(!list.contains(maskLotData.getMaskLotName()))
					{
						list.add(maskLotData.getMaskLotName());
					}
					//rsMeterFlag=false;
				}
				else
				{
					try
					{
						log.info("Update MaskLot SPCReserveHoldFlag");
						
						eventInfo.setEventComment("SPCReserveHold: "+productComment);
						
						maskLotData.setSpcReserveHoldFlag(item);
						maskLotData.setLastEventComment(eventInfo.getEventComment());
						maskLotData.setLastEventName(eventInfo.getEventName());
						maskLotData.setLastEventTime(eventInfo.getEventTime());
						maskLotData.setLastEventTimeKey(eventInfo.getLastEventTimekey());
						maskLotData.setLastEventUser(eventInfo.getEventUser());
						ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotData);	
					}
					catch(Exception e)
					{
						log.info("MaskLot Update Error");
					}								
				}
			}
			else
			{
				log.info("MaskLotState is not Released");
			}				
		}
		
		if (list.size() > 0)
		{
			List newList = new ArrayList(new TreeSet(list));
			for (int i = 0; i < newList.size(); i++)
			{
				String maskLotName = newList.get(i).toString();
				MaskLot postLotData = ExtendedObjectProxy.getMaskLotService().getMaskLotData(maskLotName);
				String sql3 = "SELECT MASKLOTNAME,REASONCODE FROM CT_MASKMULTIHOLD WHERE MASKLOTNAME = :MASKLOTNAME AND REASONCODE=:REASONCODE";
				Map<String, Object> bindMap2 = new HashMap<String, Object>();
				//eventInfo.setEventComment("AlarmHold");
				if (alarmType.equals("FDC"))
				{
					eventInfo.setReasonCode("FDCAlarm");
				}
				if (alarmType.equals("SPC"))
				{
					eventInfo.setReasonCode("SPCSpecOut");
				}
				if (!alarmType.equals("SPC") && !alarmType.equals("FDC"))
				{
					eventInfo.setReasonCode("AlarmHold");
				}
				
				eventInfo.setReasonCodeType("HOLD");
				
				bindMap2.put("MASKLOTNAME", maskLotName);
				bindMap2.put("REASONCODE", eventInfo.getReasonCode());
				List<Map<String, Object>> listResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql3, bindMap2);

				// MaskLotMultiHold
				if(listResult!=null&&listResult.size()<=0)
				{
					eventInfo.setEventComment("SPCHOLD: " +productComment);
					
					// Set EventInfo
					eventInfo = EventInfoUtil.makeEventInfo("Hold", eventInfo.getEventUser(), eventInfo.getEventComment(), eventInfo.getReasonCodeType(), eventInfo.getReasonCode());

					try
					{
						// Update HoldState for execution MultiHold
						if (StringUtil.equals(postLotData.getMaskLotHoldState(), "Y"))
						{
							// Update LotHoldState - N
							String sql = "UPDATE CT_MASKLOT SET MASKLOTHOLDSTATE = :MASKLOTHOLDSTATE WHERE MASKLOTNAME = :MASKLOTNAME ";
							Map<String, Object> bindMap = new HashMap<String, Object>();
							bindMap.put("MASKLOTHOLDSTATE", "N");
							bindMap.put("MASKLOTNAME", maskLotName);

							GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);					
						}					

						// Set MakeOnHoldInfo
						MaskLot dataInfo = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskLotName });
						dataInfo.setMaskLotName(maskLotName);
						dataInfo.setMaskLotHoldState("Y");
						dataInfo.setReasonCode(eventInfo.getReasonCode());
						dataInfo.setReasonCodeType(eventInfo.getReasonCodeType());
						dataInfo.setLastEventComment(eventInfo.getEventComment());
						dataInfo.setLastEventName(eventInfo.getEventName());
						dataInfo.setLastEventTime(eventInfo.getEventTime());
						dataInfo.setLastEventTimeKey(eventInfo.getLastEventTimekey());
						dataInfo.setLastEventUser(eventInfo.getEventUser());

						ExtendedObjectProxy.getMaskLotService().modify(eventInfo, dataInfo);
					}
					catch(Exception e)
					{
						log.info("MaskLot Update Error");
					}
					
					// Insert into LOTMULTIHOLD table
					MaskMultiHold holdData = new MaskMultiHold();
					holdData.setMaskLotName(maskLotName);
					holdData.setFactoryName(postLotData.getFactoryName());
					holdData.setMaskProcessOperationName(postLotData.getMaskProcessOperationName());
					holdData.setMaskProcessOperationVersion(postLotData.getMaskProcessOperationVersion());
					holdData.setReasonCode(eventInfo.getReasonCode());
					holdData.setReasonCodeType(eventInfo.getReasonCodeType());
					holdData.setLastEventName(eventInfo.getEventName());
					holdData.setLastEventTime(eventInfo.getEventTime());
					holdData.setLastEventComment(eventInfo.getEventComment());
					holdData.setLastEventUser(eventInfo.getEventUser());

					try
					{
						ExtendedObjectProxy.getMaskMultiHoldService().create(eventInfo, holdData);
					}
					catch (DuplicateNameSignal de)
					{
						throw new CustomException("LOT-0002", maskLotName, eventInfo.getReasonCode());
					}
					catch (FrameworkErrorSignal fe)
					{
						throw new CustomException("LOT-9999", fe.getMessage());
					}
				}				
			}
		}
	}
	
	public void lotHoldActionSPC(String machineName, EventInfo eventInfo, List<Element> productList, String alarmType, String alarmIndex) throws CustomException
	{
		List<String> list = new ArrayList<String>();

		for (Element productE : productList)
		{
			String productName = productE.getChildText("PRODUCTNAME");
			String sql1 = "SELECT PRODUCTNAME ,LOTNAME FROM PRODUCT WHERE PRODUCTNAME = :PRODUCTNAME AND PRODUCTSTATE = 'InProduction'";
			Map<String, Object> bindMap1 = new HashMap<String, Object>();
			bindMap1.put("PRODUCTNAME", productName);
			List<Map<String, Object>> Result1 = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql1, bindMap1);
			if (Result1.size() > 0 && Result1 != null)
			{
				String lotName = Result1.get(0).get("LOTNAME").toString();
				list.add(lotName);
			}
		}
		if (list.size() > 0)
		{
			List newList = new ArrayList(new TreeSet(list));
			for (int i = 0; i < newList.size(); i++)
			{
				String lotName = newList.get(i).toString();
				String sql2 = "SELECT LOTNAME,LOTPROCESSSTATE,JOBDOWNFLAG FROM LOT WHERE LOTNAME = :LOTNAME AND LOTSTATE = 'Released'";
				Map<String, Object> bindMap2 = new HashMap<String, Object>();
				bindMap2.put("LOTNAME", lotName);
				List<Map<String, Object>> Result2 = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql2, bindMap2);
				if (Result2.size() > 0)
				{
					String lotProcessState = Result2.get(0).get("LOTPROCESSSTATE").toString();
					String jobDownFlag = "";
					if(Result2.get(0).get("JOBDOWNFLAG")!=null)
					{
						jobDownFlag=Result2.get(0).get("JOBDOWNFLAG").toString();
					}					
					if (StringUtil.equals(lotProcessState, "WAIT") && !jobDownFlag.equals("Y"))
					{
						Lot postLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
						String sql3 = "SELECT LOTNAME,REASONCODE FROM LOTMULTIHOLD WHERE LOTNAME = :LOTNAME ";
						List<Map<String, Object>> listResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql3, bindMap2);
						if (listResult.size() <= 0)
						{
							Map<String, String> udfs = new HashMap<String, String>();

							eventInfo.setEventComment("AlarmHold");
							if (alarmType.equals("FDC"))
							{
								eventInfo.setReasonCode("FDCAlarm");
							}
							if (alarmType.equals("SPC"))
							{
								eventInfo.setReasonCode("SPCAlarm");
								udfs.put("ACTIONTYPE", "SPC");
								udfs.put("ALARMCODE", alarmIndex);
							}
							if (!alarmType.equals("SPC") && !alarmType.equals("FDC"))
							{
								eventInfo.setReasonCode("AlarmHold");
							}
							eventInfo.setReasonCodeType("HOLD");

							// LotMultiHold
							MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, postLotData, udfs);
						}
					}
					else
					{
						try
						{
							Lot postLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

							if (alarmType.equals("FDC"))
							{
								eventInfo.setEventComment("FDCHOLD");
								MESLotServiceProxy.getLotServiceImpl().insertCtLotFutureMultiHoldActionForAfter(eventInfo, lotName, postLotData.getFactoryName(), postLotData.getProcessFlowName(),
										postLotData.getProcessFlowVersion(), postLotData.getProcessOperationName(), postLotData.getProcessOperationVersion(), "0", "hold", "System", "HOLD", "FDC", "",
										"", "", "False", "True", "", eventInfo.getEventComment(), "", eventInfo.getEventUser(), "Insert", "", "");
							}
							if (alarmType.equals("SPC"))
							{
								try
								{
									LotFutureAction actionData 
										= ExtendedObjectProxy.getLotFutureActionService().selectByKey(false, 
													new Object[] { lotName, postLotData.getFactoryName(), postLotData.getProcessFlowName(),postLotData.getProcessFlowVersion(), 
																   postLotData.getProcessOperationName(), postLotData.getProcessOperationVersion(), 0, "SPC" });
									
									// Mantis : 0000459
									// AfterEventComment记录时，先Check AfterEventComment中是否包含当前SPC Alarm ProductName，如果已经包含，不做CT_LOTFUTUREACTION表的更新
									String afterActionComment = actionData.getAfterActionComment();
									
									String productNameList = StringUtil.EMPTY;
									for (Element productElement : productList) 
									{
										String productName = productElement.getChildText("PRODUCTNAME");
										if (!afterActionComment.contains(productName))
										{
											productNameList += productName + ",";
										}
									}
									
									if (!StringUtil.isEmpty(productNameList))
									{
										afterActionComment += productNameList + ",";
										
										eventInfo.setEventComment("SPCHOLD");
										actionData.setAfterActionComment(afterActionComment);
										ExtendedObjectProxy.getLotFutureActionService().modify(eventInfo, actionData);										
									}
								}
								catch (Exception ex)
								{
									log.info("AlarmServiceUtil.lotHoldActionSPC : " + ex.getCause());
									
									String productNameList = StringUtil.EMPTY;
									for (Element productElement : productList) 
									{
										productNameList += productElement.getChildText("PRODUCTNAME") + ",";
									}
									
									eventInfo.setEventComment("SPCHOLD : " + productNameList);
									ExtendedObjectProxy.getLotFutureActionService().insertLotFutureAction(eventInfo, lotName, postLotData.getFactoryName(), postLotData.getProcessFlowName(),
											postLotData.getProcessFlowVersion(), postLotData.getProcessOperationName(), postLotData.getProcessOperationVersion(), 0, "SPC", "HOLD", "hold", "System", "",
											"", "", "False", "True", "", eventInfo.getEventComment(), "", eventInfo.getEventUser(), "", "", alarmIndex);
								}								

//								MESLotServiceProxy.getLotServiceImpl().insertCtLotFutureMultiHoldActionForAfter(eventInfo, lotName, postLotData.getFactoryName(), postLotData.getProcessFlowName(),
//										postLotData.getProcessFlowVersion(), postLotData.getProcessOperationName(), postLotData.getProcessOperationVersion(), "0", "hold", "System", "HOLD", "SPC", "",
//										"", "", "False", "True", "", eventInfo.getEventComment(), "", eventInfo.getEventUser(), "Insert", "", "");
							}
							if (!alarmType.equals("SPC") && !alarmType.equals("FDC"))
							{
								eventInfo.setEventComment("AlARMHOLD");
								MESLotServiceProxy.getLotServiceImpl().insertCtLotFutureMultiHoldActionForAfter(eventInfo, lotName, postLotData.getFactoryName(), postLotData.getProcessFlowName(),
										postLotData.getProcessFlowVersion(), postLotData.getProcessOperationName(), postLotData.getProcessOperationVersion(), "0", "hold", "System", "HOLD", "Alarm",
										"", "", "", "False", "True", "", eventInfo.getEventComment(), "", eventInfo.getEventUser(), "Insert", "", "");

							}
						}
						catch (greenFrameDBErrorSignal e)
						{
							log.info(lotName + " insert to lot_fucture action fail ! ");
						}
					}
				}
			}
		}
	}

	public void lotAlarmHoldAction(String machineName, String alarmCode, String alarmText, EventInfo eventInfo, List<Element> productList) throws CustomException
	{
		List<String> list = new ArrayList<String>();

		for (Element productE : productList)
		{
			String productName = productE.getChildText("PRODUCTNAME");
			String sql1 = "SELECT PRODUCTNAME ,LOTNAME FROM PRODUCT WHERE PRODUCTNAME = :PRODUCTNAME AND PRODUCTSTATE = 'InProduction'";
			Map<String, Object> bindMap1 = new HashMap<String, Object>();
			bindMap1.put("PRODUCTNAME", productName);
			List<Map<String, Object>> Result1 = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql1, bindMap1);
			if (Result1.size() > 0 && Result1 != null)
			{
				String lotName = Result1.get(0).get("LOTNAME").toString();
				list.add(lotName);
			}
		}
		if (list.size() > 0)
		{
			List newList = new ArrayList(new TreeSet(list));
			for (int i = 0; i < newList.size(); i++)
			{
				String lotName = newList.get(i).toString();
				String sql2 = "SELECT LOTNAME,LOTPROCESSSTATE FROM LOT WHERE LOTNAME = :LOTNAME AND LOTSTATE = 'Released'";
				Map<String, Object> bindMap2 = new HashMap<String, Object>();
				bindMap2.put("LOTNAME", lotName);
				List<Map<String, Object>> Result2 = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql2, bindMap2);
				if (Result2.size() > 0)
				{

					String lotProcessState = Result2.get(0).get("LOTPROCESSSTATE").toString();
					if (StringUtil.equals(lotProcessState, "RUN"))
					{
						try
						{

							Lot postLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

							eventInfo.setEventComment("AlarmCode:" + alarmCode + " Text:" + alarmText);

							MESLotServiceProxy.getLotServiceImpl().insertCtLotFutureMultiHoldActionForAfter(eventInfo, lotName, postLotData.getFactoryName(), postLotData.getProcessFlowName(),
									postLotData.getProcessFlowVersion(), postLotData.getProcessOperationName(), postLotData.getProcessOperationVersion(), "0", "hold", "System", "HOLD", "Alarm", "",
									"", "", "False", "True", "", eventInfo.getEventComment(), "", eventInfo.getEventUser(), "Insert", "", "");

						}

						catch (greenFrameDBErrorSignal e)
						{
							log.info(lotName + " insert to lot_fucture action fail ! ");

						}
					}
					else
					{
						return;
					}
				}
			}
		}
	}

	public void eqpHoldAction(Machine machineData, EventInfo eventInfo) throws CustomException
	{
		if (StringUtil.equals(machineData.getUdfs().get("MACHINEHOLDSTATE"), "N") || StringUtil.equals(machineData.getUdfs().get("MACHINEHOLDSTATE"), ""))
		{
			try
			{
				SetEventInfo setEventInfo = new SetEventInfo();

				setEventInfo.getUdfs().put("MACHINEHOLDSTATE", "Y");
				MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setEventInfo, eventInfo);

			}
			catch (Exception e)
			{
				log.error(machineData.getKey().getMachineName() + "Hold failed");
			}
		}
		else
		{
			log.info(machineData.getKey().getMachineName() + " has been hold  state ! ");
		}
	}
	
	/**
	 * 
	 * AR-Photo-0027-01,AR-Photo-0032-01
	 * ReserveHold sends an e-mail when it arrives at the process.
	 * 
	 * @author aim_dhko
	 * @return 
	 */
	public void sendMailForReserveHold(Lot lotData)
	{
		try
		{
			List<LotFutureAction> reserveHoldDataList = 
					ExtendedObjectProxy.getLotFutureActionService()
						.getReserveHoldDataList(lotData.getKey().getLotName(), lotData.getFactoryName(),
								lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
								lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), 0);

			if(reserveHoldDataList == null || reserveHoldDataList.size() == 0)
			{
				log.info("sendMailForReserveHold : ReserveHold data is not exists.");
				return ;
			}
			
			StringBuilder mailMessage = new StringBuilder();
			mailMessage.append("<pre>=======ReserveHold=======</pre>");
			mailMessage.append("<pre>- LotName : ").append(lotData.getKey().getLotName()).append("</pre>");
			mailMessage.append("<pre>- FactoryName : ").append(lotData.getFactoryName()).append("</pre>");
			mailMessage.append("<pre>- ProcessFlowName : ").append(lotData.getProcessFlowName()).append("</pre>");
			mailMessage.append("<pre>- ProcessOperationName : ").append(lotData.getProcessOperationName()).append("</pre>");
			mailMessage.append("<pre>=======================================================</pre>");
			mailMessage.append("<pre>=======================================================</pre>");
			
			StringBuilder beforeMailMessage = new StringBuilder();
			StringBuilder afterMailMessage = new StringBuilder();
			
			for (LotFutureAction reserveHoldData : reserveHoldDataList) 
			{
				String beforeAction = reserveHoldData.getBeforeAction();
				String beforeMailFlag = reserveHoldData.getBeforeMailFlag();
				String afterAction = reserveHoldData.getAfterAction();
				String afterMailFlag = reserveHoldData.getAfterMailFlag();
				
				log.info("sendMailForReserveHold : BeforeAction = " + beforeAction + ", BeforeMailFlag = " + beforeMailFlag + ", AfterAction = " + afterAction + ", AfterMailFlag = " + afterMailFlag);
				
				if("True".equals(beforeAction) && "Y".equals(beforeMailFlag))
				{
					if(beforeMailMessage != null && beforeMailMessage.length() > 0)
					{
						beforeMailMessage.append("<pre>-------------------------------------------------------</pre>");
					}
					beforeMailMessage.append("<pre>- ReasonCode : ").append(reserveHoldData.getReasonCode()).append("</pre>");
					beforeMailMessage.append("<pre>- Comment : ").append(reserveHoldData.getBeforeActionComment()).append("</pre>");	
				}
				
				if("True".equals(afterAction) && "Y".equals(afterMailFlag))
				{
					if(afterMailMessage != null && afterMailMessage.length() > 0)
					{
						afterMailMessage.append("<pre>-------------------------------------------------------</pre>");
					}
					afterMailMessage.append("<pre>- ReasonCode : ").append(reserveHoldData.getReasonCode()).append("</pre>");
					afterMailMessage.append("<pre>- Comment : ").append(reserveHoldData.getAfterActionComment()).append("</pre>");
				}
			}
			
			mailMessage.append("<pre></pre>");
			if(beforeMailMessage != null && beforeMailMessage.length() > 0)
			{
				mailMessage.append("<pre>=======BeforeAction=======</pre>");
				mailMessage.append("<pre>=======================================================</pre>");
				mailMessage.append(beforeMailMessage);
				mailMessage.append("<pre>=======================================================</pre>");
			}
			if(afterMailMessage != null && afterMailMessage.length() > 0)
			{
				mailMessage.append("<pre>=======AfterAction=======</pre>");
				mailMessage.append("<pre>=======================================================</pre>");
				mailMessage.append(afterMailMessage);
				mailMessage.append("<pre>=======================================================</pre>");
			}

			CommonUtil.sendEmail("FutureAction", "ReserveHold", mailMessage.toString());
		}
		catch(Exception ex)
		{
			log.info("sendMailForReserveHold : " +  ex.getCause());
		}
	}
}
