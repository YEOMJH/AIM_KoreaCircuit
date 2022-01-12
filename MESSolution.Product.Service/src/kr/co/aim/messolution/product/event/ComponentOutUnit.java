package kr.co.aim.messolution.product.event;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import com.sun.org.apache.bcel.internal.generic.NEW;

import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ComponentHistory;
import kr.co.aim.messolution.extended.object.management.data.ComponentInspectHistory;
import kr.co.aim.messolution.extended.object.management.data.ComponentMonitor;
import kr.co.aim.messolution.extended.object.management.data.EQPProcessTimeConf;
import kr.co.aim.messolution.extended.object.management.data.MaterialProduct;
import kr.co.aim.messolution.extended.object.management.data.OffsetAlignInfo;
import kr.co.aim.messolution.extended.object.management.data.PhotoOffsetResult;
import kr.co.aim.messolution.extended.object.management.data.ReviewComponentHistory;
import kr.co.aim.messolution.extended.object.management.impl.ReviewComponentHistoryService;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.extended.object.management.data.RunBanRule;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processoperationspec.ProcessOperationSpecServiceProxy;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpecKey;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.generic.util.XmlUtil;

public class ComponentOutUnit extends AsyncHandler {

	private static Log log = LogFactory.getLog(ComponentOutUnit.class);
	@SuppressWarnings("unchecked")
	@Override
	public void doWorks(Document doc) throws CustomException
	{

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String productJudge = SMessageUtil.getBodyItemValue(doc, "PRODUCTJUDGE", false);
		String productGrade = SMessageUtil.getBodyItemValue(doc, "PRODUCTGRADE", false);
		String fromSlotId = SMessageUtil.getBodyItemValue(doc, "FROMSLOTID", false);
		String toSlotId = SMessageUtil.getBodyItemValue(doc, "TOSLOTID", false);
		String fromSlotPosition = SMessageUtil.getBodyItemValue(doc, "FROMSLOTPOSITION", false);
		String toSlotPosition = SMessageUtil.getBodyItemValue(doc, "TOSLOTPOSITION", false);
		String productType = SMessageUtil.getBodyItemValue(doc, "PRODUCTTYPE", false);
		String photoMaskName = SMessageUtil.getBodyItemValue(doc, "PHOTOMASKNAME", false);
		String ppid = SMessageUtil.getBodyItemValue(doc, "PPID", false);

		Product productData = null;
		ProductSpec productSpecData=null;
		Lot lotData = null;
		String factoryName = "";
		String productSpecName = "";
		String productSpecVersion = "";
		String processFlowName = "";
		String processFlowVersion = "";
		String processOperationName = "";
		String processOperationVersion = "";
		String productionType = "";
		String productRequestName = "";
		
		// 2. Check Machine
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
		MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(unitName);
		
		// 1. Select product or lot data
		if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Unpacker))
		{
			factoryName = "ARRAY";
			productType = "Sheet";
		}
		else if (productType.equals(GenericServiceProxy.getConstantMap().ProductType_Panel))
		{
			lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
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
			lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
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
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			productData = MESProductServiceProxy.getProductServiceUtil().updateFlagStack(eventInfo,productData, machineName, unitName,"-",mainFlowData.getUdfs().get("RUNBANPROCESSFLOWTYPE"));
		}
		
		// Check PhotoMask
		
		if ((StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Photo)
				||StringUtils.equals(machineData.getMachineGroupName(), "TPPhoto"))
				&& unitName.endsWith("EXP")
				&&StringUtils.isNotEmpty(photoMaskName)&&CommonUtil.equalsIn(lotData.getProductionType(),"E","P","T"))
		{
			lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
			Durable usedMaskInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(photoMaskName);
			List<ListOrderedMap> policyMaskList = PolicyUtil.getPhotoMaskPolicy(lotData, machineName);
			List<ListOrderedMap> assignedMaskToMachine = MESDurableServiceProxy.getDurableServiceUtil().getAssignedPhotoMaskList(machineName);
			
			usedMaskInfo.setDurationUsed(usedMaskInfo.getDurationUsed() + 1);
			
			Map<String, String> udfs = new HashMap<String, String>();

			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.setUdfs(udfs);

			DurableServiceProxy.getDurableService().update(usedMaskInfo);
			
			EventInfo eventInfoMask = EventInfoUtil.makeEventInfo("ComponentOutUnit", getEventUser(), getEventComment(), null, null);
			eventInfoMask.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			//Update Count
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(usedMaskInfo, setEventInfo, eventInfoMask);
			
			int inUseMaskCount = 0;
			
			boolean policyCheck = false;
			for (ListOrderedMap policyMask : policyMaskList)
			{
				String policyMaskName = CommonUtil.getValue(policyMask, "MASKNAME");
				
				if(usedMaskInfo.getKey().getDurableName().equals(policyMaskName))
				{
					policyCheck = true;
				}
			}
			
			for (ListOrderedMap assignedMask : assignedMaskToMachine)
			{
				String assignedMaskName = CommonUtil.getValue(assignedMask, "DURABLENAME");
				
				Durable assignedMaskInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(assignedMaskName);
				
				if(assignedMaskInfo.getDurableState().equals(GenericServiceProxy.getConstantMap().Dur_InUse))
				{
					inUseMaskCount ++;
				}
			}
			
			// Create MaterialProduct
			MaterialProduct dataInfo = new MaterialProduct();
			dataInfo.setTimeKey(eventInfoMask.getEventTimeKey());
			dataInfo.setProductName(productName);
			dataInfo.setLotName(productData.getLotName());
			dataInfo.setMaterialKind(GenericServiceProxy.getConstantMap().MaterialKind_Durable);
			dataInfo.setMaterialType(usedMaskInfo.getDurableType());
			dataInfo.setMaterialName(photoMaskName);
			dataInfo.setQuantity(1);
			dataInfo.setEventName(eventInfoMask.getEventName());
			dataInfo.setEventTime(eventInfoMask.getEventTime());
			dataInfo.setFactoryName(productData.getFactoryName());
			dataInfo.setProductSpecName(productData.getProductSpecName());
			dataInfo.setProductSpecVersion(productData.getProductSpecVersion());
			dataInfo.setProcessFlowName(productData.getProcessFlowName());
			dataInfo.setProcessFlowVersion(productData.getProcessFlowVersion());
			dataInfo.setProcessOperationName(productData.getProcessOperationName());			
			dataInfo.setProcessOperationVersion(productData.getProcessOperationVersion());
			dataInfo.setMachineName(machineName);
			dataInfo.setMaterialLocationName(unitName);
			
			ExtendedObjectProxy.getMaterialProductService().create(eventInfoMask, dataInfo);
			
			/*
			if(!policyCheck)
			{				
				if(!checkReserveHoldList(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), 
						lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), "PhotoMask", "False", "True"))
				{
					EventInfo eventInfo = EventInfoUtil.makeEventInfo("Insert" + "FutureHold", getEventUser(), "[ReserveOper : " + lotData.getProcessOperationName() + "]", null, null);
					eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

					String afterActionUser = getEventUser();
					String afterMailFlag = "";
					String afterActionComment = "";
					ExtendedObjectProxy.getLotFutureActionService().insertLotFutureAction(eventInfo, lotName, factoryName, processFlowName, processFlowVersion, processOperationName,
					processOperationVersion, 0, "PhotoMask", "PhotoMask", "hold", "System", "", "", "", "False", "True", "", afterActionComment,
					"", afterActionUser, "", afterMailFlag);
				}
			}*/
		}
		
		
		// Photo Offset
		if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Photo) && unitName.endsWith("EXP"))
		{
			PhotoOffsetResult photoOffsetResult = null;
			
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("UpdateOffset", getEventUser(), getEventComment(), null, null);
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			
			try
			{
				photoOffsetResult = ExtendedObjectProxy.getPhotoOffsetResultService().selectByKey(false, new Object[]{machineName, ppid});
			} catch (Exception e) {log.info("Not Exist photoOffsetResult");}
			
			if(photoOffsetResult == null)
			{
				photoOffsetResult = new PhotoOffsetResult();
				photoOffsetResult.setMachineName(machineName);
				photoOffsetResult.setOffset(ppid);
				photoOffsetResult.setLastUseTime(eventInfo.getEventTime());
				ExtendedObjectProxy.getPhotoOffsetResultService().insert(photoOffsetResult);
			}
			else
			{
				photoOffsetResult.setLastUseTime(eventInfo.getEventTime());
				ExtendedObjectProxy.getPhotoOffsetResultService().update(photoOffsetResult);
			}
			
			ProcessOperationSpec operationSpec = ProcessOperationSpecServiceProxy.getProcessOperationSpecService().selectByKey(new ProcessOperationSpecKey(factoryName, processOperationName, processOperationVersion));
			OffsetAlignInfo offsetAlignInfo=new OffsetAlignInfo();
			try
			{
				offsetAlignInfo = ExtendedObjectProxy.getOffsetAlignInfoService().selectByKey(false, new Object[]{productData.getFactoryName(), productData.getProductSpecName(), productData.getProductSpecVersion(), operationSpec.getUdfs().get("LAYERNAME").toString()});				
			}
			catch (Exception ex)
			{
				log.info("offsetAlignInfo not found");
			}
			 
			if(offsetAlignInfo!=null&&StringUtils.isNotEmpty(offsetAlignInfo.getMainLayerStep()))
			{
				String offset = productData.getUdfs().get("OFFSET").toString();
				MachineSpec machineSpec = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
				String machineOffset = machineSpec.getUdfs().get("OFFSETID").toString();
				int currentStepOffset=Integer.parseInt(offsetAlignInfo.getMainLayerStep());
				if(StringUtils.isNotEmpty(offset)&&offset.length()>=currentStepOffset)
				{
					String tempOffset="";
					for(int i=0;i<offset.length();i++)
					{
						if(i!=(currentStepOffset-1))
							tempOffset+=offset.charAt(i);
						else if(currentStepOffset!=1)
						{
							tempOffset+=machineOffset;
						}
						else tempOffset=machineOffset;
					}
					offset=tempOffset;
				}
				else if((StringUtils.isNotEmpty(offset)&&offset.length()==currentStepOffset-1)
						||(StringUtils.isEmpty(offset)&&currentStepOffset==1))
				{
					offset+=machineOffset;
				}
				else if((StringUtils.isNotEmpty(offset)&&offset.length()< currentStepOffset)
						||(StringUtils.isEmpty(offset)&&currentStepOffset!=1))
				{
					for(int i=0;i<currentStepOffset;i++)
					{
						offset+="0";
						if(offset.length()==currentStepOffset-1)
						{
							offset+=machineOffset;
							break;
						}
					}
				}
				
				Map<String, String> udfs = productData.getUdfs();
				udfs.put("OFFSET", offset);

				kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
				setEventInfo.getUdfs().put("OFFSET", offset);
				
				MESProductServiceProxy.getProductServiceImpl().setEvent(productData, setEventInfo, eventInfo);
			}
		}

		// 3. Insert to Component History
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ComponentOutUnit", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		ComponentHistory dataInfo = new ComponentHistory();
		dataInfo.setTimeKey(eventInfo.getEventTimeKey());
		dataInfo.setProductName(productName);
		dataInfo.setLotName(lotName);
		dataInfo.setEventName(eventInfo.getEventName());
		dataInfo.setToSlotId(StringUtils.trimToEmpty(toSlotId) == "" ? 0 : Integer.valueOf(toSlotId));
		dataInfo.setFromSlotId(StringUtils.trimToEmpty(fromSlotId) == "" ? 0 : Integer.valueOf(fromSlotId));
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
		dataInfo.setMaterialLocationName(unitName);
		dataInfo.setProductGrade(productGrade);
		dataInfo.setProductJudge(productJudge);
		dataInfo.setProductRequestName(productRequestName);

		ExtendedObjectProxy.getComponentHistoryService().create(eventInfo, dataInfo);
		
		//4. Clear ComponentInUnit IN Component Monitor
		try
		{
			List<ComponentMonitor> dataMonitorRemove = ExtendedObjectProxy.getComponentMonitorService().select("PRODUCTNAME = ? AND EVENTNAME = 'ComponentInUnit'", new Object[] { productName });
			if (dataMonitorRemove.size()>0)
			{
				CheckComponentMonitor(machineData, lotData, unitName, productData, productName, dataMonitorRemove, machineName);
			
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
		
		// Exposure Unit
		if (CommonUtil.equalsIn(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Photo) && unitName.indexOf("EXP") != -1)
		{
			String condition = " MACHINENAME = ? AND UNITNAME = ? AND DURABLESTATE = ? ";
			Object[] bindSet = new Object[] { machineName, unitName, "InUse" };
			List<Durable> durableList = null;
			try
			{
				durableList = DurableServiceProxy.getDurableService().select(condition, bindSet);
			}
			catch (NotFoundSignal nfs)
			{
			}

			if (durableList != null && durableList.size() > 0)
			{
				for (Durable durableData : durableList)
				{
					// Create MaterialProduct
					MaterialProduct dataInfoMaterialProduct = new MaterialProduct();
					dataInfoMaterialProduct.setTimeKey(eventInfo.getEventTimeKey());
					dataInfoMaterialProduct.setProductName(productName);
					dataInfoMaterialProduct.setLotName(productData.getLotName());
					dataInfoMaterialProduct.setMaterialKind(GenericServiceProxy.getConstantMap().MaterialKind_Durable);
					dataInfoMaterialProduct.setMaterialType(durableData.getDurableType());
					dataInfoMaterialProduct.setMaterialName(durableData.getKey().getDurableName());
					dataInfoMaterialProduct.setQuantity(1);
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
					dataInfoMaterialProduct.setMaterialLocationName(unitName);

					ExtendedObjectProxy.getMaterialProductService().create(eventInfo, dataInfoMaterialProduct);
				}
			}
		}
		else
		{
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT MATERIALID, MATERIALTYPE, MATERIALKIND, QUANTITY ");
			sql.append("  FROM (SELECT CONSUMABLENAME AS MATERIALID, ");
			sql.append("               CONSUMABLETYPE AS MATERIALTYPE, ");
			sql.append("               'Consumable' AS MATERIALKIND, ");
			sql.append("               QUANTITY, ");
			sql.append("               KITTIME ");
			sql.append("          FROM CONSUMABLE ");
			sql.append("         WHERE 1 = 1 ");
			sql.append("           AND CONSUMABLESTATE = 'InUse' ");
			sql.append("           AND TRANSPORTSTATE = 'OnEQP' ");
			sql.append("           AND MACHINENAME = :MACHINENAME ");
			sql.append("           AND MATERIALLOCATIONNAME = :MATERIALLOCATIONNAME ");
			sql.append("           AND (SEQ IS NULL OR SEQ=1) ");
			sql.append("        UNION ALL ");
			sql.append("        SELECT DURABLENAME AS MATERIALID, ");
			sql.append("               DURABLETYPE AS MATERIALTYPE, ");
			sql.append("               'Durable' AS MATERIALKIND, ");
			sql.append("               1 AS QUANTITY, ");
			sql.append("               KITTIME ");
			sql.append("          FROM DURABLE ");
			sql.append("         WHERE 1 = 1 ");
			sql.append("           AND DURABLESTATE = 'InUse' ");
			sql.append("           AND UPPER(TRANSPORTSTATE) = 'ONEQP' ");
			sql.append("           AND MACHINENAME = :MACHINENAME ");
			sql.append("           AND MATERIALLOCATIONNAME = :MATERIALLOCATIONNAME) B ");
			sql.append(" WHERE 1 = 1 ");
			sql.append("ORDER BY KITTIME ASC ");

			List<Map<String, Object>> resultList = null;
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("MACHINENAME", machineName);
			bindMap.put("MATERIALLOCATIONNAME", unitName);

			try
			{
				resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

			}
			catch (Exception ex)
			{
				eventLog.info(String.format("Data Information is Empty!! [SQL= %s] [MachineName = %s , MaterialLocationName=%s]", sql, machineName, unitName));
			}

			if (resultList != null && resultList.size() > 0)
			{
				for (Map<String, Object> resultInfo : resultList)
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
							List<ListOrderedMap> erpBom = MESConsumableServiceProxy.getConsumableServiceUtil().getERPBOMMaterialSpec(factoryName, productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME").toString(), processOperationName, processOperationVersion, unitName, consumableData.getConsumableSpecName());
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
					dataInfoMaterialProduct.setMaterialLocationName(unitName);

					ExtendedObjectProxy.getMaterialProductService().create(eventInfo, dataInfoMaterialProduct);
				}
			}
		}

		// Added for 3CEE01 Review Station
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT UNITNAME, SEQ ");
		sql.append("  FROM POSINLINEMACHINE ");
		sql.append(" WHERE UNITNAME = :UNITNAME ");
		sql.append("   AND ROWNUM = 1 ");
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("UNITNAME", unitName);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		if (sqlResult.size() > 0)
		{
			ComponentInspectHistory inspectDataInfo = new ComponentInspectHistory();
			inspectDataInfo.setTimeKey(eventInfo.getEventTimeKey());
			inspectDataInfo.setProductName(productName);
			inspectDataInfo.setLotName(lotName);
			inspectDataInfo.setEventName(eventInfo.getEventName());
			inspectDataInfo.setToSlotId(StringUtils.trimToEmpty(toSlotId) == "" ? 0 : Integer.valueOf(toSlotId));
			inspectDataInfo.setFromSlotId(StringUtils.trimToEmpty(fromSlotId) == "" ? 0 : Integer.valueOf(fromSlotId));
			inspectDataInfo.setToSlotPosition(toSlotPosition);
			inspectDataInfo.setFromSlotPosition(fromSlotPosition);
			inspectDataInfo.setEventTime(eventInfo.getEventTime());
			inspectDataInfo.setEventUser(eventInfo.getEventUser());
			inspectDataInfo.setFactoryName(factoryName);
			inspectDataInfo.setProductSpecName(productSpecName);
			inspectDataInfo.setProductSpecVersion(productSpecVersion);
			inspectDataInfo.setProcessFlowName(processFlowName);
			inspectDataInfo.setProcessFlowVersion(processFlowVersion);
			inspectDataInfo.setProcessOperationName(processOperationName);
			inspectDataInfo.setProcessOperationVersion(processOperationVersion);
			inspectDataInfo.setProductionType(productionType);
			inspectDataInfo.setProductType(productType);
			inspectDataInfo.setMachineName(machineName);
			inspectDataInfo.setMaterialLocationName(unitName);
			inspectDataInfo.setProductGrade(productGrade);
			inspectDataInfo.setProductRequestName(productRequestName);
			inspectDataInfo.setInspectionFlag(getInspectionFlagForEVA(productName, lotName, factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion,
					processOperationName, processOperationVersion, machineName, ConvertUtil.getMapValueByName(sqlResult.get(0), "SEQ")));

			ExtendedObjectProxy.getComponentInspectHistoryService().create(eventInfo, inspectDataInfo);
		}
		// For ReviewStation
		if(StringUtils.equals(machineSpecData.getMachineGroupName(),"AOI")||StringUtils.equals(machineSpecData.getMachineGroupName(),"LOI")
				||StringUtils.equals(machineSpecData.getMachineGroupName(),"ArrayTest")
				||CommonUtil.equalsIn(unitName,"3CEE01-EAI","3CEE01-MAI","3CEE01-LOI","3CEE02-EAI","3CEE02-MAI","3CEE02-LOI"))
		{
			String sqlForRSIn= " SELECT P2.PROCESSOPERATIONNAME FROM NODE N,PROCESSOPERATIONSPEC P1,PROCESSOPERATIONSPEC P2 "
					+ " WHERE 1=1 "
					+ " AND P1.DETAILPROCESSOPERATIONTYPE IN ('AOI','AMM','ILO')  "
					+ " AND P2.DETAILPROCESSOPERATIONTYPE='VIEW'  "
					+ " AND N.NODEATTRIBUTE1=P2.PROCESSOPERATIONNAME  "
					+ " AND N.PROCESSFLOWNAME=:PROCESSFLOWNAME  "
					+ " AND P1.PROCESSOPERATIONNAME=:PROCESSOPERATIONNAME ";
				
			Map<String, Object> bindForRSIn = new HashMap<String, Object>();
			bindForRSIn.put("PROCESSFLOWNAME",processFlowName);
			bindForRSIn.put("PROCESSOPERATIONNAME",processOperationName);
			
			List<Map<String, Object>> rsInData = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlForRSIn, bindForRSIn);

			if((rsInData!=null&&rsInData.size()==1)||StringUtils.equals(processOperationName, "21200")||StringUtils.equals(machineSpecData.getMachineGroupName(),"ArrayTest"))
			{
				List<ReviewComponentHistory> reviewComponentHisList=new ArrayList<ReviewComponentHistory>();
				try 
				{
					 reviewComponentHisList=ExtendedObjectProxy.getReviewComponentHistoryService().select(
							" PRODUCTNAME=? AND PROCESSOPERATIONNAME=? AND MATERIALLOCATIONNAME=? ", new Object[]{productName,processOperationName,unitName  });
				} 
				catch (greenFrameDBErrorSignal n) 
				{

				}
				
				if(reviewComponentHisList!=null &&reviewComponentHisList.size()>0)
				{
					for(ReviewComponentHistory reviewComponentHisInfo:reviewComponentHisList)
					{
						reviewComponentHisInfo.setLastEventTime(eventInfo.getEventTime());
						reviewComponentHisInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
						reviewComponentHisInfo.setLastEventUser(eventInfo.getEventUser());
					}
					ExtendedObjectProxy.getReviewComponentHistoryService().update(reviewComponentHisList);
				}
				else 
				{
					ReviewComponentHistory reviewComponentHis=new ReviewComponentHistory();
					reviewComponentHis.setProductName(productName);
					reviewComponentHis.setProductSpecName(productSpecName);
					reviewComponentHis.setProcessFlowName(processFlowName);
					reviewComponentHis.setProcessOperationName(processOperationName);
					reviewComponentHis.setMachineName(machineName);
					reviewComponentHis.setMaterialLocationName(unitName);
					reviewComponentHis.setLotName(lotName);
					reviewComponentHis.setFactoryName(factoryName);
					if(StringUtils.equals(processOperationName, "21200")||StringUtils.equals(machineSpecData.getMachineGroupName(),"ArrayTest"))
					{
						reviewComponentHis.setRsProcessOperationName("");
					}
					else
					{
						reviewComponentHis.setRsProcessOperationName(rsInData.get(0).get("PROCESSOPERATIONNAME").toString());
					}

					reviewComponentHis.setLastEventTime(eventInfo.getEventTime());
					reviewComponentHis.setLastEventTimeKey(eventInfo.getEventTimeKey());
					reviewComponentHis.setLastEventUser(eventInfo.getEventUser());
					ExtendedObjectProxy.getReviewComponentHistoryService().create(eventInfo, reviewComponentHis);
					
				}
			}
		}

		
	}
	
	private void CheckComponentMonitor(Machine machineData, Lot lotData, String unitName, Product productData,
			String productName, List<ComponentMonitor> dataMonitorRemove, String machineName) 
	{
		// Check Machine Component Monitor Flag
		String condition = " FACTORYNAME = ? AND MACHINENAME = ? AND MONITORTYPE = 'ComponentMonitorLower' AND SUBMACHINENAME = ? AND (PRODUCTSPECNAME = 'ALL' OR PRODUCTSPECNAME = ?) AND PROCESSOPERATIONNAME = ? ";
		
		List<EQPProcessTimeConf> sqlResult = new ArrayList<EQPProcessTimeConf>();
		try
		{
			sqlResult = ExtendedObjectProxy.getEQPProcessTimeConfService().select(condition, new Object[]{machineData.getFactoryName(), machineName, unitName, lotData.getProductSpecName(), lotData.getProcessOperationName()});
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
							+ "  UnitName: " + unitName
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

	private void sendToFMB(String factoryName, String machineName, EventInfo eventInfo, StringBuffer messageInfo) 
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

	private String getInspectionFlagForEVA(String productName, String lotName, String factoryName, String productSpecName, String productSpecVersion, String processFlowName,
			String processFlowVersion, String processOperationName, String processOperationVersion, String machineName, String seq)
	{
		String inspectionFlag = "X";
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT SUBSTR (INSPECTIONFLAG, :SEQ, 1) AS INSPECTIONFLAG ");
		sql.append("  FROM CT_INLINESAMPLEPRODUCT ");
		sql.append(" WHERE PRODUCTNAME = :PRODUCTNAME ");
		sql.append("   AND FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("   AND PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");
		sql.append("   AND PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		sql.append("   AND MACHINENAME = :MACHINENAME ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("SEQ", seq);
		args.put("PRODUCTNAME", productName);
		args.put("FACTORYNAME", factoryName);
		args.put("PRODUCTSPECNAME", productSpecName);
		args.put("PRODUCTSPECVERSION", productSpecVersion);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);
		args.put("PROCESSOPERATIONNAME", processOperationName);
		args.put("PROCESSOPERATIONVERSION", processOperationVersion);
		args.put("MACHINENAME", machineName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			inspectionFlag = ConvertUtil.getMapValueByName(result.get(0), "INSPECTIONFLAG");
		}

		return inspectionFlag;
	}
	
	private boolean checkReserveHoldList(String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion,
			String reasonCode, String beforeAction, String afterAction) throws CustomException
	{
		boolean checkFlag = true;

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT LOTNAME, REASONCODE, BEFOREACTION, AFTERACTION ");
		sql.append("  FROM CT_LOTFUTUREACTION ");
		sql.append(" WHERE LOTNAME = :LOTNAME ");
		sql.append("   AND FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		sql.append("   AND POSITION = '0' ");
		sql.append("   AND REASONCODE = :REASONCODE ");
		sql.append("   AND BEFOREACTION = :BEFOREACTION ");
		sql.append("UNION ");
		sql.append("SELECT LOTNAME, REASONCODE, BEFOREACTION, AFTERACTION ");
		sql.append("  FROM CT_LOTFUTUREACTION ");
		sql.append(" WHERE LOTNAME = :LOTNAME ");
		sql.append("   AND FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		sql.append("   AND POSITION = '0' ");
		sql.append("   AND REASONCODE = :REASONCODE ");
		sql.append("   AND AFTERACTION = :AFTERACTION ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("LOTNAME", lotName);
		args.put("FACTORYNAME", factoryName);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);
		args.put("PROCESSOPERATIONNAME", processOperationName);
		args.put("PROCESSOPERATIONVERSION", processOperationVersion);
		args.put("REASONCODE", reasonCode);
		args.put("BEFOREACTION", beforeAction);
		args.put("AFTERACTION", afterAction);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (sqlResult.size() > 0)
			checkFlag = true;
		else
			checkFlag = false;

		return checkFlag;
	}
}
