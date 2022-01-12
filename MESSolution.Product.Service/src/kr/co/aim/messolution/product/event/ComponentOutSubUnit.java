package kr.co.aim.messolution.product.event;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ComponentHistory;
import kr.co.aim.messolution.extended.object.management.data.ComponentInChamberHist;
import kr.co.aim.messolution.extended.object.management.data.ComponentMonitor;
import kr.co.aim.messolution.extended.object.management.data.EQPProcessTimeConf;
import kr.co.aim.messolution.extended.object.management.data.LotFutureAction;
import kr.co.aim.messolution.extended.object.management.data.MaterialProduct;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ComponentOutSubUnit extends AsyncHandler {

	@SuppressWarnings("unchecked")
	@Override
	public void doWorks(Document doc) throws CustomException {
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String productJudge = SMessageUtil.getBodyItemValue(doc, "PRODUCTJUDGE", false);
		String productGrade = SMessageUtil.getBodyItemValue(doc, "PRODUCTGRADE", false);
		String fromSlotId = SMessageUtil.getBodyItemValue(doc, "FROMSLOTID", false);
		String toSlotId = SMessageUtil.getBodyItemValue(doc, "TOSLOTID", false);
		String fromSlotPosition = SMessageUtil.getBodyItemValue(doc, "FROMSLOTPOSITION", false);
		String toSlotPosition = SMessageUtil.getBodyItemValue(doc, "TOSLOTPOSITION", false);
		String productType = SMessageUtil.getBodyItemValue(doc, "PRODUCTTYPE", false);
		String chamberUseCount = SMessageUtil.getBodyItemValue(doc, "CHAMBERUSECOUNT", false);

		Product productData = null;
		ProductSpec productSpecData=null;
		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
		String factoryName = "";
		String productSpecName = "";
		String productSpecVersion = "";
		String processFlowName = "";
		String processFlowVersion = "";
		String processOperationName = "";
		String processOperationVersion = "";
		String productionType = "";
		String productRequestName = "";
		
		
		// 1. Check Machine
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(subUnitName);

		// 2. Select product or lot data
		if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Unpacker))
		{
			factoryName = "ARRAY";
			productType = "Sheet";
		}
		else if(productType.equals(GenericServiceProxy.getConstantMap().ProductType_Panel))
		{
			factoryName = lotData.getFactoryName();
			productSpecName = lotData.getProductSpecName();
			productSpecVersion = lotData.getProductSpecVersion();
			processFlowName = lotData.getProcessFlowName();
			processFlowVersion = lotData.getProcessFlowVersion();
			processOperationName = lotData.getProcessOperationName();
			processOperationVersion = lotData.getProcessOperationVersion();
			productionType = lotData.getProductionType();
			productRequestName = lotData.getProductRequestName();
		}
		else
		{
			productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
			factoryName = productData.getFactoryName();
			productSpecName = productData.getProductSpecName();
			productSpecVersion = productData.getProductSpecVersion();
			processFlowName = productData.getProcessFlowName();
			processFlowVersion = productData.getProcessFlowVersion();
			processOperationName = productData.getProcessOperationName();
			processOperationVersion = productData.getProcessOperationVersion();
			productionType = productData.getProductionType();
			productRequestName = productData.getProductRequestName();
			productType = productData.getProductType();
			
			productSpecData=GenericServiceProxy.getSpecUtil().getProductSpec(factoryName, productSpecName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
			// update product FlagStack
			ProcessFlow mainFlowData = MESLotServiceProxy.getLotInfoUtil().getMainProcessFlowData(lotData);
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("UpdateFlagStack", this.getEventUser(), this.getEventComment());
			productData = MESProductServiceProxy.getProductServiceUtil().updateFlagStack(eventInfo,productData, machineName, unitName,subUnitName,mainFlowData.getUdfs().get("RUNBANPROCESSFLOWTYPE"));
		}
	
		// 3. Insert to Component History
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ComponentOutSubUnit", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		ComponentHistory dataInfo = new ComponentHistory();
		dataInfo.setTimeKey(eventInfo.getEventTimeKey());
		dataInfo.setProductName(productName);
		dataInfo.setLotName(lotName);
		dataInfo.setEventName(eventInfo.getEventName());
		dataInfo.setToSlotId(StringUtils.trimToEmpty(toSlotId) == "" ? 0 : Integer.valueOf(toSlotId));
		dataInfo.setFromSlotId(StringUtils.trimToEmpty(fromSlotId)== "" ? 0 : Integer.valueOf(fromSlotId));
		dataInfo.setToSlotPosition(toSlotPosition);
		dataInfo.setFromSlotPosition(fromSlotPosition);
		dataInfo.setEventTime(eventInfo.getEventTime());
		dataInfo.setEventUser(eventInfo.getEventUser());
		dataInfo.setFactoryName(factoryName);
		dataInfo.setProductSpecName(productSpecName);
		dataInfo.setProductSpecVersion(productSpecVersion);
		dataInfo.setProcessFlowName(processFlowName);
		dataInfo.setProcessFlowVersion(processFlowVersion);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(processOperationVersion);
		dataInfo.setProductionType(productionType);
		dataInfo.setProductType(productType);
		dataInfo.setMachineName(machineName);
		dataInfo.setMaterialLocationName(subUnitName);
		dataInfo.setProductGrade(productGrade);
		dataInfo.setProductJudge(productJudge);
		dataInfo.setProductRequestName(productRequestName);
		dataInfo.setChamberUseCount(StringUtil.isEmpty(chamberUseCount) ? null : Integer.parseInt(chamberUseCount));

		ExtendedObjectProxy.getComponentHistoryService().create(eventInfo, dataInfo);
		
		//4. Clear ComponentInSubUnit IN Component Monitor
		try
		{
			List<ComponentMonitor> dataMonitorRemove = ExtendedObjectProxy.getComponentMonitorService().select("PRODUCTNAME = ? AND EVENTNAME = 'ComponentInSubUnit'", new Object[] { productName });
			if (dataMonitorRemove.size()>0)
			{
				CheckComponentMonitor(machineData, lotData, subUnitName, productData, productName, dataMonitorRemove, machineName);
			
				for (ComponentMonitor dataMonitorRemoves : dataMonitorRemove)
				{
					eventLog.info(" Remove Product: " + productName + " IN CT_ComponentMonitor ");
					ExtendedObjectProxy.getComponentMonitorService().remove(eventInfo, dataMonitorRemoves);
				}
			}
		}
		catch(Exception ex)
		{
			eventLog.info(ex.getCause());
		}
		
		//5. SAP
		  //Start get ChamberName 20210219 houxk
		String chamberName = "";
		Machine EQPData    = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		if(EQPData.getMachineGroupName().equals("EVA"))
		{
			String sql2 =
					" SELECT SUPERREASONCODE FROM REASONCODE\n" +
							"  WHERE REASONCODETYPE = 'ChamberMapping' \n" + 
							"               AND SIGN = 'SUBUNIT'\n" + 
							"               AND LEVELNO = '3'\n" + 
							"  AND REASONCODE = :REASONCODE ";
			List<Map<String,Object>> resultList2 = null;
			Map<String,String> bindMap2 = new HashMap<String,String>();
			bindMap2.put("REASONCODE", subUnitName);
			
			try
			{
				resultList2 = GenericServiceProxy.getSqlMesTemplate().queryForList(sql2, bindMap2);
				
			}catch(Exception ex)
			{
				eventLog.info(String.format("Data Information is Empty!!"));
			}

			if (resultList2 != null && resultList2.size() > 0)
			{
				Map<String,Object> resultInfo2 = resultList2.get(0);
				chamberName = ConvertUtil.getMapValueByName(resultInfo2, "SUPERREASONCODE");
			}
		}
		  //End
		
		String sql =
				"SELECT MATERIALID, MATERIALTYPE, MATERIALKIND, QUANTITY\n" +
						"  FROM (SELECT CONSUMABLENAME AS MATERIALID,\n" + 
						"               CONSUMABLETYPE AS MATERIALTYPE,\n" + 
						"               'Consumable' as MATERIALKIND,\n" + 
						"               QUANTITY,\n" + 
						"               KITTIME\n" + 
						"          FROM CONSUMABLE\n" + 
						"         WHERE 1 = 1\n" + 
						"           AND CONSUMABLESTATE = 'InUse'\n" + 
						"           AND TRANSPORTSTATE = 'OnEQP'\n" + 
						"           AND MACHINENAME = :MACHINENAME\n" + 
						"           AND ( MATERIALLOCATIONNAME = :MATERIALLOCATIONNAME OR MATERIALLOCATIONNAME = :MATERIALLOCATIONNAME2)\n" + 
						"           AND (SEQ IS NULL OR SEQ=1)\n" + 
						"        UNION ALL\n" + 
						"        SELECT DURABLENAME AS MATERIALID,\n" + 
						"               DURABLETYPE AS MATERIALTYPE,\n" + 
						"               'Durable' as MATERIALKIND,\n" + 
						"               1 AS QUANTITY,\n" + 
						"               KITTIME\n" + 
						"          FROM DURABLE\n" + 
						"         WHERE 1 = 1\n" + 
						"           AND DURABLESTATE = 'InUse'\n" + 
						"           AND UPPER(TRANSPORTSTATE) = 'ONEQP'\n" + 
						"           AND MACHINENAME = :MACHINENAME\n" + 
						"           AND ( MATERIALLOCATIONNAME = :MATERIALLOCATIONNAME OR MATERIALLOCATIONNAME = :MATERIALLOCATIONNAME2)) B\n" + 
						" WHERE 1 = 1 ORDER BY KITTIME ASC ";

		List<Map<String,Object>> resultList = null;
		Map<String,String> bindMap = new HashMap<String,String>();
		bindMap.put("MACHINENAME", machineName);
		bindMap.put("MATERIALLOCATIONNAME", subUnitName);
		bindMap.put("MATERIALLOCATIONNAME2", chamberName);						
		
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			
		}catch(Exception ex)
		{
			eventLog.info(String.format("Data Information is Empty!! [SQL= %s] [MachineName = %s , MaterialLocationName=%s]",sql,machineName,unitName));
		}

		if (resultList != null && resultList.size() > 0)
		{
			for (Map<String,Object> resultInfo : resultList)
			{
	
				MaterialProduct dataInfoMaterialProduct = new MaterialProduct();
				
				//For SAP Quantity//////////////////////////////////////////////////////////////////////////////////////////////////
				double quantity = 1;
				
				if(StringUtils.equals(ConvertUtil.getMapValueByName(resultInfo, "MATERIALKIND"), GenericServiceProxy.getConstantMap().MaterialKind_Consumable))
				{
					ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);
					
					if(StringUtils.isNotEmpty(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
					{
						Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(ConvertUtil.getMapValueByName(resultInfo, "MATERIALID"));
						/*
						List<ListOrderedMap> erpBom = MESConsumableServiceProxy.getConsumableServiceUtil().getERPBOMMaterialSpec(factoryName, productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME").toString(), processOperationName, processOperationVersion, subUnitName, consumableData.getConsumableSpecName());
						String sQuantity = CommonUtil.getValue(erpBom.get(0), "QUANTITY");
						quantity = Double.parseDouble(sQuantity);*/
					}
				}
				dataInfoMaterialProduct.setQuantity(1);
				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				
				dataInfoMaterialProduct.setTimeKey(eventInfo.getEventTimeKey());
				dataInfoMaterialProduct.setProductName(productName);
				dataInfoMaterialProduct.setLotName(productData.getLotName());
				dataInfoMaterialProduct.setMaterialKind(ConvertUtil.getMapValueByName(resultInfo, "MATERIALKIND"));
				dataInfoMaterialProduct.setMaterialType(ConvertUtil.getMapValueByName(resultInfo, "MATERIALTYPE"));
				dataInfoMaterialProduct.setMaterialName(ConvertUtil.getMapValueByName(resultInfo, "MATERIALID"));
				dataInfoMaterialProduct.setEventName(eventInfo.getEventName());
				dataInfoMaterialProduct.setEventTime(eventInfo.getEventTime());
				dataInfoMaterialProduct.setFactoryName(productData.getFactoryName());
				dataInfoMaterialProduct.setProductSpecName(productData.getProductSpecName());
				dataInfoMaterialProduct.setProductSpecVersion(productData.getProductSpecVersion());
				dataInfoMaterialProduct.setProcessFlowName(productData.getProcessFlowName());
				dataInfoMaterialProduct.setProcessFlowVersion(productData.getProcessFlowVersion());
				dataInfoMaterialProduct.setProcessOperationName(productData.getProcessOperationName());
				dataInfoMaterialProduct.setProcessOperationVersion(productData.getProcessOperationVersion());
				dataInfoMaterialProduct.setMachineName(machineName);
				dataInfoMaterialProduct.setMaterialLocationName(subUnitName);

				ExtendedObjectProxy.getMaterialProductService().create(eventInfo, dataInfoMaterialProduct);
			}
		}
		
		// for ChamberSampling
		MachineSpec machineSpecData = GenericServiceProxy.getSpecUtil().getMachineSpec(subUnitName);

		if (StringUtils.equals(machineSpecData.getUdfs().get("CHAMBERYN"), "Y"))
		{
			ComponentInChamberHist inChamberDataInfo = new ComponentInChamberHist();
			inChamberDataInfo.setTimeKey(eventInfo.getEventTimeKey());
			inChamberDataInfo.setProductName(productName);
			inChamberDataInfo.setLotName(lotName);
			inChamberDataInfo.setEventName(eventInfo.getEventName());
			inChamberDataInfo.setToSlotId(StringUtils.trimToEmpty(toSlotId) == "" ? 0 : Integer.valueOf(toSlotId));
			inChamberDataInfo.setFromSlotId(StringUtils.trimToEmpty(fromSlotId) == "" ? 0 : Integer.valueOf(fromSlotId));
			inChamberDataInfo.setToSlotPosition(toSlotPosition);
			inChamberDataInfo.setFromSlotPosition(fromSlotPosition);
			inChamberDataInfo.setEventTime(eventInfo.getEventTime());
			inChamberDataInfo.setEventUser(eventInfo.getEventUser());
			inChamberDataInfo.setFactoryName(factoryName);
			inChamberDataInfo.setProductSpecName(productSpecName);
			inChamberDataInfo.setProductSpecVersion(productSpecVersion);
			inChamberDataInfo.setProcessFlowName(processFlowName);
			inChamberDataInfo.setProcessFlowVersion(processFlowVersion);
			inChamberDataInfo.setProcessOperationName(processOperationName);
			inChamberDataInfo.setProcessOperationVersion(processOperationVersion);
			inChamberDataInfo.setProductionType(productionType);
			inChamberDataInfo.setProductType(productType);
			inChamberDataInfo.setMachineName(machineName);
			inChamberDataInfo.setMaterialLocationName(subUnitName);
			inChamberDataInfo.setProductGrade(productGrade);
			inChamberDataInfo.setProductJudge(productJudge);
			inChamberDataInfo.setProductRequestName(productRequestName);
			inChamberDataInfo.setChamberYN(machineSpecData.getUdfs().get("CHAMBERYN"));
			inChamberDataInfo.setMachineRecipeName(lotData.getMachineRecipeName());
			inChamberDataInfo.setTrackInTime(TimeStampUtil.getEventTimeKeyFromTimestamp(lotData.getLastLoggedInTime()));

			ExtendedObjectProxy.getComponentInChamberHistService().create(eventInfo, inChamberDataInfo);
		}
		
		//Photo check lastProcessedTime,if currentTime-lastProcessedTime,reserve hold Lot   --by PH guoShuangWang 20211014
		if(productData!=null)
		{
			subUnitIdleCheck(Long.toString(productData.getPosition()), lotName,  processOperationName, machineData, subUnitName, lotData, eventInfo);
		}

	}

	private void CheckComponentMonitor(Machine machineData, Lot lotData, String subUnitName, Product productData, String productName, List<ComponentMonitor> dataMonitorRemove, String machineName) 
	{
		// Check Machine Component Monitor Flag
		String condition = " FACTORYNAME = ? AND MACHINENAME = ? AND MONITORTYPE = 'ComponentMonitorLower' AND SUBMACHINENAME = ? AND (PRODUCTSPECNAME = 'ALL' OR PRODUCTSPECNAME = ?) AND PROCESSOPERATIONNAME = ? ";
		
		List<EQPProcessTimeConf> sqlResult = new ArrayList<EQPProcessTimeConf>();
		try
		{
			sqlResult = ExtendedObjectProxy.getEQPProcessTimeConfService().select(condition, new Object[]{machineData.getFactoryName(), machineName, subUnitName, lotData.getProductSpecName(), lotData.getProcessOperationName()});
			if(sqlResult.size() > 0 && !sqlResult.isEmpty())
			{
				String ruleTime = sqlResult.get(0).getRuleTime();
				
				String eventTime = TimeUtils.toTimeString(dataMonitorRemove.get(0).getEventTime(), TimeStampUtil.FORMAT_TIMEKEY);
				String interval = Double
						.toString(ConvertUtil.getDiffTime(eventTime, TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY)));
				
				if (Double.parseDouble(ruleTime) * 60 > Double.parseDouble(interval))
				{
					// execute message
					eventLog.info(" 制程时间不足 ");
					StringBuffer messageInfo = new StringBuffer();
					double processingTime = new BigDecimal(Double.parseDouble(interval) / 60).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
					messageInfo.append("<pre> productName: " + productName
							+ "  SubUnitName: " + subUnitName
							+ "  ProcessingTime: " + processingTime + " min "
							+ "  RuleTime(Limit): " + ruleTime + " min " + "</pre>");
				
					// find EmailList
					List<String> emailList = null;
					
					try 
					{
						emailList = MESLotServiceProxy.getLotServiceImpl().getEmailList(machineName,"ComponentMonitorLower");
						
						// sendEmail
						MESLotServiceProxy.getLotServiceImpl().sendEmail(emailList, messageInfo.toString(), "Process time not enough!");
					} 
					catch (Exception e) 
					{
						eventLog.info(" Failed to send mail. ");
					}
				
					//get UserList
					String userList = getUserList(machineName);	
				
					//SendToEM
					sendToEM(userList,machineName,messageInfo);
				
					//SendToFMB
					EventInfo eventInfo = EventInfoUtil.makeEventInfo("ComponentMonitorTimer", "MES", "", "", "");		
					
					try 
					{
						MachineSpec machineSpec = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
					
						sendToFMB(machineSpec.getFactoryName(), machineSpec.getKey().getMachineName(), eventInfo, messageInfo);
					} 
					catch (CustomException e) 
					{
						eventLog.info("Failed to sendToFMB");
						return;
					}
				}
			}
		}
		catch (Exception e)
		{
			eventLog.info(e.getCause());
		}
	}
	
	//get UserList
	private String getUserList(String machineName)
	{
		List<Map<String,Object>> resultList = null;
		String userList = new String();
		StringBuilder sb = new StringBuilder();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT MS.RMSDEPARTMENT FROM MACHINESPEC MS WHERE MS.MACHINENAME = :MACHINENAME");
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("MACHINENAME", machineName);
	
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		}
		catch(Exception ex)
		{
			eventLog.info(ex.getCause());
		}
		
		try 
		{
			if (resultList.size() > 0) 
			{
				String departmentAll = resultList.get(0).get("RMSDEPARTMENT").toString();

				List<String> department =  CommonUtil.splitStringDistinct(",",departmentAll);

				StringBuffer sql1 = new StringBuffer();
				sql1.append(
						"SELECT B.* FROM CT_ALARMUSERGROUP A, CT_ALARMUSER B, USERPROFILE C WHERE     A.USERID = B.USERID AND B.USERID = C.USERID AND A.ALARMGROUPNAME = 'ComponentMonitorLower' AND C.DEPARTMENT = :DEPARTMENT");
				Map<String, Object> args1 = new HashMap<String, Object>();

				//for (String department1 : department) 
				for(int j = 0; j < department.size(); j++)
				{
					args1.put("DEPARTMENT", department.get(j));
					List<Map<String, Object>> sqlResult1 = GenericServiceProxy.getSqlMesTemplate()
							.queryForList(sql1.toString(), args1);
					
					if (sqlResult1.size() > 0) 
					{
						if(j < department.size() - 1)
						{
							for (int i = 0; i < sqlResult1.size(); i++) 
							{  
								String user = ConvertUtil.getMapValueByName(sqlResult1.get(i), "USERID");
								sb.append(user + ",");  
				             } 
						}
						else
						{
							for (int i = 0; i < sqlResult1.size(); i++) 
							{  
								String user = ConvertUtil.getMapValueByName(sqlResult1.get(i), "USERID");
				                 if (i < sqlResult1.size() - 1) {  
				                     sb.append(user + ",");  
				                 } else {  
				                     sb.append(user);  
				                 }  
				             } 
						}
					}
				}
				userList = sb.toString();
			}
		}
		catch (Exception e)
		{
			eventLog.info("Not Found the Department of "+ machineName);
			eventLog.info(" Failed to send to EMobile, MachineName: " + machineName);
		}
		return userList;
	}
	
	//sendToEM & WeChat
	private void sendToEM(String userList, String machineName, StringBuffer messageInfo) 
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ComponentMonitorTimer", "MES", "", "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		try
		{																
			String[] userGroup = userList.split(",");				
			String title = "工艺超时报警";
			String detailtitle = "${}CIM系统消息通知";
			String url = "";
			
			StringBuffer info = new StringBuffer();
			info.append("<pre>=======================AlarmInformation=======================</pre>");
			info.append("<pre> MachineName : " + machineName + "</pre>");
			info.append(messageInfo);
			info.append("<pre>=============================End=============================</pre>");			
			
			String message = info.toString();
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userGroup, title, detailtitle, message, url);
			//log.info("eMobile Send Success!");	
			
			StringBuffer weChatInfo = new StringBuffer();
			weChatInfo.append("<pre>====AlarmInformation====</pre>");
			weChatInfo.append("<pre> MachineName : " + machineName + "</pre>");
			weChatInfo.append(messageInfo);
			weChatInfo.append("<pre>====AlarmInfoEnd====</pre>");
			
			String weChatMessage = weChatInfo.toString();
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userGroup, title, detailtitle, weChatMessage, "");
			//log.info("WeChat Send Success!");	
		}
		catch (Exception e)
		{
			eventLog.info("eMobile or WeChat Send Error : " + e.getCause());	
		}
	}
	
	//sendToFMB
	private void sendToFMB(String factoryName, String machineName, EventInfo eventInfo, StringBuffer messageInfo) throws CustomException
	{
	
		Element rootElement = new Element(SMessageUtil.Message_Tag);
		
		Element headerElement = new Element(SMessageUtil.Header_Tag);
			
		headerElement.addContent(new Element("MESSAGENAME").setText("ProductInSubUnitOverTime"));
		headerElement.addContent(new Element("TRANSACTIONID").setText(TimeUtils.getCurrentEventTimeKey()));		
		headerElement.addContent(new Element("ORIGINALSOURCESUBJECTNAME").setText(""));
		headerElement.addContent(new Element("EVENTUSER").setText(eventInfo.getEventUser()));
		headerElement.addContent(new Element("EVENTCOMMENT").setText(eventInfo.getEventComment()));
		headerElement.addContent(new Element("LANGUAGE").setText(""));
		
		rootElement.addContent(headerElement);
		
		Element bodyElement = new Element(SMessageUtil.Body_Tag);
		XmlUtil.addElement(bodyElement, "FACTORYNAME", factoryName);
		XmlUtil.addElement(bodyElement, "MACHINENAME", machineName);
		XmlUtil.addElement(bodyElement, "ALARMINFORMATION", messageInfo.toString());

		rootElement.addContent(bodyElement);
		
		//Send to FMB
		GenericServiceProxy.getESBServive().sendBySenderToFMB(new Document(rootElement));
	}
	//End
	
	private void subUnitIdleCheck(String position,String lotName, String processOperationName,Machine subUnitData,String subUnitName,Lot lotData,EventInfo eventInfo) throws CustomException
	{
		boolean photoSubUnitCheckFlag = ExtendedObjectProxy.getEnumDefValueService().isExistEnumNameInfo("SubUnitIdleCheck", subUnitName);
		if(photoSubUnitCheckFlag)
		{
			try
			{
				List<LotFutureAction> lotFutureList=ExtendedObjectProxy.getLotFutureActionService().select("LOTNAME=? AND PROCESSOPERATIONNAME=? AND ACTIONNAME=? AND REASONCODE=?  ",
						new Object[]{lotName,processOperationName,"hold","RH-PHIdle"} );
				if(lotFutureList!=null&&lotFutureList.size()>0)
				{
					LotFutureAction lotFutureInfo=lotFutureList.get(0);
					String comment=lotFutureInfo.getAfterActionComment();
					String newComment="";
					if(StringUtils.contains(comment, subUnitName))
					{
						//update comment
						String[] commonList=comment.split("],");
						for(int i=0;i<commonList.length;i++)
						{
							if(StringUtils.contains(commonList[i], subUnitName))
							{
								commonList[i]+="、"+position;
								break;
							}
						}

						for(int i=0;i<commonList.length;i++)
						{
							newComment+=commonList[i]+"],";
						}
					}
					else
					{
						newComment=comment+"["+subUnitName+": "+position+"],";
					}

					lotFutureInfo.setAfterActionComment(newComment);
					ExtendedObjectProxy.getLotFutureActionService().modify(eventInfo, lotFutureInfo);
				}				
			}
			catch(greenFrameDBErrorSignal n)
			{
				eventLog.info("not found subUnitIdleCheck reserve hold List");
				
				String lastProcessedTime=subUnitData.getUdfs().get("LASTPROCESSENDTIME");
				String sCurrentTime = TimeStampUtil.getCurrentTime(TimeStampUtil.FORMAT_DEFAULT);
				
				SimpleDateFormat sf = new SimpleDateFormat(TimeStampUtil.FORMAT_DEFAULT);
				long lCurrentTime = 0;
				long lLastProcessedTime = 0;
				if(!StringUtils.isEmpty(lastProcessedTime))
				{
					// Idle Limit(day)
					String idleLimit=CommonUtil.getEnumDefValueStringByEnumName("SubUnitIdleLimit");
					double maxIdleTime = 0;
					//compare
					try
					{
						lCurrentTime = sf.parse(sCurrentTime).getTime();
						lLastProcessedTime = sf.parse(lastProcessedTime).getTime();
					}
					catch (Exception e)
					{
						eventLog.info("DateFormat change error");
					}
					
					try
					{
						maxIdleTime=Double.parseDouble(idleLimit);
					}
					catch(Exception e)
					{
						maxIdleTime=72;
					}
					
					if (lCurrentTime!=0&&lLastProcessedTime!=0&&((lCurrentTime - lLastProcessedTime) > maxIdleTime * 3600000))
					{
						//insert reserve hold
						String afterActionUser = getEventUser();
						String afterActionComment ="SubUnit Over MaxIdleLimit "+"["+subUnitName+": "+position+"],";
						ExtendedObjectProxy.getLotFutureActionService().insertLotFutureAction(eventInfo, lotName, lotData.getFactoryName(), lotData.getProcessFlowName(), 
								lotData.getProcessFlowVersion(), processOperationName,lotData.getProcessOperationVersion(), 0, "RH-PHIdle", "ReserveHoldLot", "hold", "System",
								"", "", "", "False", "True", "", afterActionComment,"", afterActionUser, "", "");
					}
				}	
				
				//update lastProcessEndTime										
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.getUdfs().put("LASTPROCESSENDTIME",  TimeStampUtil.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));					
				MESMachineServiceProxy.getMachineServiceImpl().setEvent(subUnitData, setEventInfo, eventInfo);
			}
		}


	}
}
