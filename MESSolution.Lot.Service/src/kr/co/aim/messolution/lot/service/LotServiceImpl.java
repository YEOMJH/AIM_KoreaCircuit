package kr.co.aim.messolution.lot.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LotFutureAction;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EMailInterface;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.NodeStack;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.data.LotMultiHold;
import kr.co.aim.greentrack.lot.management.data.LotMultiHoldKey;
import kr.co.aim.greentrack.lot.management.info.AssignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.ChangeGradeInfo;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.CreateAndAssignAllProductsInfo;
import kr.co.aim.greentrack.lot.management.info.CreateInfo;
import kr.co.aim.greentrack.lot.management.info.CreateWithParentLotInfo;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.DeassignProductsInfo;
import kr.co.aim.greentrack.lot.management.info.MakeEmptiedInfo;
import kr.co.aim.greentrack.lot.management.info.MakeInReworkInfo;
import kr.co.aim.greentrack.lot.management.info.MakeLoggedInInfo;
import kr.co.aim.greentrack.lot.management.info.MakeLoggedOutInfo;
import kr.co.aim.greentrack.lot.management.info.MakeNotInReworkInfo;
import kr.co.aim.greentrack.lot.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.lot.management.info.MakeReceivedInfo;
import kr.co.aim.greentrack.lot.management.info.MakeReleasedInfo;
import kr.co.aim.greentrack.lot.management.info.MakeShippedInfo;
import kr.co.aim.greentrack.lot.management.info.MakeUnScrappedInfo;
import kr.co.aim.greentrack.lot.management.info.MakeUnShippedInfo;
import kr.co.aim.greentrack.lot.management.info.RelocateProductsInfo;
import kr.co.aim.greentrack.lot.management.info.SeparateInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.lot.management.info.SplitInfo;
import kr.co.aim.greentrack.lot.management.info.TransferProductsToLotInfo;
import kr.co.aim.greentrack.lot.management.info.ext.ConsumedMaterial;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class LotServiceImpl implements ApplicationContextAware {
	
	private ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog(LotServiceImpl.class);

	@Override
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		applicationContext = arg0;
	}
	
	/*
	 * Mantis : 0000355
	 * If Factory information and LotName information are different, change LotName.
	 * OLED <-> TP
	 */
	public void changeLotName(EventInfo eventInfo,String fromType, List<String> lotNameList) throws CustomException
	{
		String[] oledShopCode = { "B", "C", "F" };
		String[] tpShopCode = { "L", "T" };
		
		for (String lotName : lotNameList) 
		{
			Lot oldLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			
			if (!CommonUtil.equalsIn(oldLotData.getFactoryName(), "OLED", "TP"))
			{
				continue;
			}
			if (!StringUtil.equals(oldLotData.getProductType(), "Glass"))
			{
				continue;
			}
			if ("OLED".equals(oldLotData.getFactoryName()) &&
				Arrays.asList(oledShopCode).contains(oldLotData.getKey().getLotName().substring(0, 1)))
			{
				continue;
			}
			else if ("TP".equals(oldLotData.getFactoryName()) &&
					 Arrays.asList(tpShopCode).contains(oldLotData.getKey().getLotName().substring(0, 1)))
			{
				continue;
			}

			List<ProductP> productPSequence = MESLotServiceProxy.getLotInfoUtil().setProductPSequence(oldLotData.getKey().getLotName());
			
			String targetAreaName = GenericServiceProxy.getSpecUtil().getDefaultArea(oldLotData.getFactoryName());
			              String targetOperationName = StringUtil.EMPTY;
			              String targerOperationVer = StringUtil.EMPTY;
			
			              
			//fromType : ReceiveLot or UnShipLot              
			if ("ReceiveLot".equals(fromType))
			{
				// OLED -> TP or TP -> OELD ReceiveLot
				targetOperationName = CommonUtil.getFirstOperation(oldLotData.getFactoryName(), oldLotData.getProcessFlowName()).getKey().getProcessOperationName();
				targerOperationVer = CommonUtil.getFirstOperation(oldLotData.getFactoryName(), oldLotData.getProcessFlowName()).getKey().getProcessOperationVersion();
			}
			else
			{
				// TP -> OLED UnShipLot
				targetOperationName = CommonUtil.getLastOperation(oldLotData.getFactoryName(), oldLotData.getProcessFlowName()).getKey().getProcessOperationName();
				targerOperationVer = CommonUtil.getLastOperation(oldLotData.getFactoryName(), oldLotData.getProcessFlowName()).getKey().getProcessOperationVersion();
			}
		
			String targetNodeId  = NodeStack.getNodeID(oldLotData.getFactoryName(), oldLotData.getProcessFlowName(), targetOperationName, targerOperationVer);
		
			// 1. Create New LotName
			ProductSpec baseData = GenericServiceProxy.getSpecUtil().getProductSpec(oldLotData.getFactoryName(), oldLotData.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);

			String namingRule = "";

			if (StringUtil.equals(baseData.getProductionType(), "P"))
				namingRule = "ProductionLotNaming";
			else
				namingRule = "LotNaming";

			if (StringUtil.isEmpty(baseData.getUdfs().get("PRODUCTCODE")))
				throw new CustomException("NAMING-0002", baseData.getKey().getProductSpecName());
			
			Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
			nameRuleAttrMap.put("PRODUCTCODE", baseData.getUdfs().get("PRODUCTCODE"));
			nameRuleAttrMap.put("PRODUCTIONTYPE", baseData.getProductionType());
			nameRuleAttrMap.put("PRODUCTSPECTYPE", baseData.getUdfs().get("PRODUCTSPECTYPE"));

			String newLotName = CommonUtil.generateNameByNamingRule(namingRule, nameRuleAttrMap, 1).get(0);
			
			// 2. Deassign Carrier
			List<ProductU> productUSequence = MESLotServiceProxy.getLotServiceUtil().setProductUSequence(oldLotData.getKey().getLotName());
			DeassignCarrierInfo deassignCarrierInfo = new DeassignCarrierInfo(productUSequence, new HashMap<String, String>());
			
			eventInfo.setEventName("DeassignCarrier");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			LotServiceProxy.getLotService().deassignCarrier(oldLotData.getKey(), eventInfo, deassignCarrierInfo);
			
			// 3. Deassign Products
			DeassignProductsInfo deassignProductsInfo = new DeassignProductsInfo();
			deassignProductsInfo.setConsumerLotName(newLotName);
			deassignProductsInfo.setEmptyFlag("Y");
			deassignProductsInfo.setProductPSequence(productPSequence);
			deassignProductsInfo.setProductQuantity(productPSequence.size());
			deassignProductsInfo.setUdfs(new HashMap<String, String>());
			
			eventInfo.setBehaviorName("LPC");
			eventInfo.setEventName("DeassignProducts");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			LotServiceProxy.getLotService().deassignProducts(oldLotData.getKey(), eventInfo, deassignProductsInfo);
			
			// 4. Create New Lot
			CreateAndAssignAllProductsInfo createAndAssignAllProductsInfo = new CreateAndAssignAllProductsInfo();
			createAndAssignAllProductsInfo.setAreaName(targetAreaName);
			createAndAssignAllProductsInfo.setAssignCarrierFlag("Y");
			createAndAssignAllProductsInfo.setAssignCarrierUdfs(new HashMap<String, String>());
			createAndAssignAllProductsInfo.setCarrierName(oldLotData.getCarrierName());
			createAndAssignAllProductsInfo.setDueDate(oldLotData.getDueDate());
			createAndAssignAllProductsInfo.setFactoryName(oldLotData.getFactoryName());
			createAndAssignAllProductsInfo.setLastLoggedInTime(oldLotData.getLastLoggedInTime());
			createAndAssignAllProductsInfo.setLastLoggedInUser(oldLotData.getLastLoggedInUser());
			createAndAssignAllProductsInfo.setLastLoggedOutTime(oldLotData.getLastLoggedOutTime());
			createAndAssignAllProductsInfo.setLastLoggedOutUser(oldLotData.getLastLoggedOutUser());
			createAndAssignAllProductsInfo.setLotGrade(oldLotData.getLotGrade());
			createAndAssignAllProductsInfo.setLotName(newLotName);
			createAndAssignAllProductsInfo.setMachineName(oldLotData.getMachineName());
			createAndAssignAllProductsInfo.setMachineRecipeName(oldLotData.getMachineRecipeName());
			createAndAssignAllProductsInfo.setNodeStack(targetNodeId);
			createAndAssignAllProductsInfo.setOriginalLotName(newLotName);
			createAndAssignAllProductsInfo.setPriority(oldLotData.getPriority());
			createAndAssignAllProductsInfo.setProcessFlowName(oldLotData.getProcessFlowName());
			createAndAssignAllProductsInfo.setProcessFlowVersion(GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
			createAndAssignAllProductsInfo.setProcessGroupName(oldLotData.getProcessGroupName());
			createAndAssignAllProductsInfo.setProcessOperationName(targetOperationName);
			createAndAssignAllProductsInfo.setProcessOperationVersion(targerOperationVer);
			createAndAssignAllProductsInfo.setProductionType(oldLotData.getProductionType());
			createAndAssignAllProductsInfo.setProductPSequence(productPSequence);
			createAndAssignAllProductsInfo.setProductQuantity(productPSequence.size());
			createAndAssignAllProductsInfo.setProductRequestName(oldLotData.getProductRequestName());
			createAndAssignAllProductsInfo.setProductSpec2Name(oldLotData.getProductSpec2Name());
			createAndAssignAllProductsInfo.setProductSpec2Version(oldLotData.getProductSpec2Version());
			createAndAssignAllProductsInfo.setProductSpecName(oldLotData.getProductSpecName());
			createAndAssignAllProductsInfo.setProductSpecVersion(GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
			createAndAssignAllProductsInfo.setProductType(oldLotData.getProductType());
			createAndAssignAllProductsInfo.setReworkCount(oldLotData.getReworkCount());
			createAndAssignAllProductsInfo.setReworkFlag("N");
			createAndAssignAllProductsInfo.setReworkNodeId(oldLotData.getReworkNodeId());
			createAndAssignAllProductsInfo.setSourceLotName("");
			createAndAssignAllProductsInfo.setSubProductType(oldLotData.getSubProductType());
			createAndAssignAllProductsInfo.setSubProductUnitQuantity1(oldLotData.getSubProductUnitQuantity1());
			createAndAssignAllProductsInfo.setSubProductUnitQuantity2(oldLotData.getSubProductUnitQuantity2());
			createAndAssignAllProductsInfo.setUdfs(oldLotData.getUdfs());
			
			eventInfo.setBehaviorName("");
			eventInfo.setEventName("ChangeLot");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			Lot newLotData = LotServiceProxy.getLotService().createAndAssignAllProducts(eventInfo, createAndAssignAllProductsInfo);
			
			// 5. Update SourceLotName
			String sql = "UPDATE LOT SET SOURCELOTNAME = ?, DESTINATIONFACTORYNAME = ? WHERE LOTNAME = ? ";
			greenFrameServiceProxy.getSqlTemplate().update(sql, new Object[] { oldLotData.getKey().getLotName(), oldLotData.getDestinationFactoryName(), newLotData.getKey().getLotName() });
			
			sql = "UPDATE LOTHISTORY SET SOURCELOTNAME = ?, DESTINATIONFACTORYNAME = ? WHERE LOTNAME = ? AND TIMEKEY = ? ";
			greenFrameServiceProxy.getSqlTemplate().update(sql, new Object[] { oldLotData.getKey().getLotName(), oldLotData.getDestinationFactoryName(), newLotData.getKey().getLotName(), newLotData.getLastEventTimeKey() });
		}
	}
	
	/*
	 * 2020-11-17	dhko	0000406
	 * CreateSortJob时，如果JobType是Split，需要在LotProcessEnd/LotProcessAbort之后Hold SourceLot和TargetLot
	 */
	public void makeReserveHoldByAbortProductList(EventInfo eventInfo, List<String> lotNameList) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		eventInfo.setEventName("SetFutureActionData");

		for (String lotName : lotNameList) 
		{
			Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
			
			String factoryName = lotData.getFactoryName();
			String processFlowName = lotData.getUdfs().get("RETURNFLOWNAME");
			String processFlowVersion = lotData.getProcessFlowVersion();
			String processOperationName = lotData.getUdfs().get("RETURNOPERATIONNAME");
			String processOperationVersion = lotData.getProcessOperationVersion();
			String actionName = "hold";
			String actionType = "System";
			String reasonCodeType = "ReserveHoldLot";
			String reasonCode = "ABORT-SORT";
			String attribute1 = "";
			String attribute2 = "";
			String attribute3 = "";
			String beforeAction = "True";
			String afterAction = "False";
			String beforeActionComment = "CreateSortJob";
			String afterActionComment = "";
			String beforeActionUser = "";
			String afterActionUser = "";
			
			ExtendedObjectProxy.getLotFutureActionService().insertLotFutureAction(eventInfo, lotData.getKey().getLotName(), factoryName, processFlowName, processFlowVersion, processOperationName,
					processOperationVersion, 0, reasonCode, reasonCodeType, actionName, actionType, attribute1, attribute2, attribute3, beforeAction, afterAction, beforeActionComment,
					afterActionComment, beforeActionUser, afterActionUser);
		}
	}
	
	/*
	 * 2020-11-15	dhko	LotHold if ProcessingInfo is 'B' in Product
	 */
	public void makeOnHoldByAbortProductList(EventInfo eventInfo, List<String> lotNameList) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		if (lotNameList == null || lotNameList.size() == 0)
		{
			return ;
		}
		
		eventInfo.setEventName("Hold");
		eventInfo.setReasonCode("SYSTEM-ABORT");
		eventInfo.setEventComment("LotHold if ProcessingInfo is 'B' in Product");
		
		for (String lotName : lotNameList) 
		{
			Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
			
			if (!GenericServiceProxy.getConstantMap().Lot_Released.equals(lotData.getLotState()))
			{
				continue;
			}
			
			boolean isExist = MESLotServiceProxy.getLotServiceUtil().isAbortProductList(lotName);
			if (isExist)
			{
				MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, lotData, new HashMap<String, String>());
			}
		}
	}
	
	public void insertCtLotFutureMultiHoldActionForAfter(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, String position, String actionName, String actionType, String reasonCodeType, String reasonCode, String attribute1, String attribute2, String attribute3,
			String beforeAction, String afterAction, String beforeActionComment, String afterActionComment, String beforeActionUser, String afterActionUser, String ActionKind, String beforeMailFlag,
			String afterMailFlag, String requestDepartment, String releaseType , String alarmCode) throws CustomException
	{
		eventInfo.setEventTimeKey(StringUtil.isEmpty(eventInfo.getEventTimeKey()) ? TimeStampUtil.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()) : eventInfo.getEventTimeKey());
		
		if(ActionKind.equals("Insert"))
		{
			ExtendedObjectProxy.getLotFutureActionService().insertLotFutureAction(eventInfo, lotName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion,
					Integer.parseInt(position), reasonCode, reasonCodeType, actionName, actionType, attribute1, attribute2, attribute3, beforeAction, afterAction, beforeActionComment, afterActionComment,
					beforeActionUser, afterActionUser, beforeMailFlag, afterMailFlag, requestDepartment, releaseType,alarmCode);
		}
		else if(ActionKind.equals("Update"))
		{
			ExtendedObjectProxy.getLotFutureActionService().updateLotFutureActionWithReasonCodeType(eventInfo, lotName, factoryName, processFlowName, processFlowVersion, processOperationName,
					processOperationVersion, Integer.parseInt(position), reasonCode, reasonCodeType, actionName, actionType, attribute1, attribute2, attribute3, beforeAction, afterAction,
					beforeActionComment, afterActionComment, beforeActionUser, afterActionUser, beforeMailFlag, afterMailFlag, requestDepartment, releaseType,alarmCode);
		}
		else if(ActionKind.equals("Delete"))
		{
			ExtendedObjectProxy.getLotFutureActionService().deleteLotFutureActionWithReasonCodeType(eventInfo, lotName, factoryName, processFlowName, processFlowVersion, processOperationName,
					processOperationVersion, Integer.parseInt(position), reasonCode, reasonCodeType);
		}
	}

	
	
	public void updateLotData(String fieldName, String fieldValue, String lotName) throws CustomException
	{
		String sql = "UPDATE LOT SET " 
					 + fieldName + " = :fieldValue WHERE LOTNAME = :lotName ";
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("fieldValue"	  , fieldValue);
		bindMap.put("lotName"	 	  , lotName);
		
		try
		{
			GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		}
		catch(Exception e)
		{
			throw new CustomException("SYS-8001", e.getMessage());
		}
		
	}
	
	public Lot createLot(EventInfo eventInfo, CreateInfo createInfo) throws CustomException
	{
		try
		{
			Lot lot = LotServiceProxy.getLotService().create(eventInfo, createInfo);

			return lot;
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("LOT-9003", createInfo.getLotName());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("LOT-9999", fe.getMessage());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("LOT-9002", createInfo.getLotName());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("LOT-9001", createInfo.getLotName());
		}
	}
	
	
	public Lot createWithParentLot(EventInfo eventInfo, String newLotName, CreateWithParentLotInfo createWithParentLotInfo) throws CustomException
	{
		try
		{
			LotKey newLotKey = new LotKey();
			newLotKey.setLotName(newLotName);

			Lot newLotData = LotServiceProxy.getLotService().createWithParentLot(eventInfo, createWithParentLotInfo);
			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey = " + eventInfo.getEventTimeKey());

			return newLotData;
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("LOT-9003", newLotName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("LOT-9999", fe.getMessage());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("LOT-9002", newLotName);
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("LOT-9001", newLotName);
		}
	}

	public void transferProductsToLot(EventInfo eventInfo, Lot sourceLotData, String destinationLotName, double productQuantity, List<ProductP> productPSequence, String emptyFlag,
			Map<String, String> destinationLotUdfs, Map<String, String> deassignCarrierUdfs, Map<String, String> sourceLotUdfs) throws CustomException
	{
		try
		{
			TransferProductsToLotInfo transferProductsToLotInfo = new TransferProductsToLotInfo();

			transferProductsToLotInfo.setDestinationLotName(destinationLotName);
			transferProductsToLotInfo.setProductQuantity(productQuantity);
			transferProductsToLotInfo.setProductPSequence(productPSequence);
			transferProductsToLotInfo.setEmptyFlag(emptyFlag);
			transferProductsToLotInfo.setDestinationLotUdfs(destinationLotUdfs);
			transferProductsToLotInfo.setDeassignCarrierUdfs(deassignCarrierUdfs);
			transferProductsToLotInfo.setValidationFlag("N");
			transferProductsToLotInfo.setUdfs(sourceLotUdfs);

			LotServiceProxy.getLotService().transferProductsToLot(sourceLotData.getKey(), eventInfo, transferProductsToLotInfo);
			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey = " + eventInfo.getEventTimeKey());
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("LOT-9003", sourceLotData.getKey().getLotName());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("LOT-9999", fe.getMessage());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("LOT-9002", sourceLotData.getKey().getLotName());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("LOT-9001", sourceLotData.getKey().getLotName());
		}
	}

	public Lot transferProductsToLot(EventInfo eventInfo, Lot lotData, TransferProductsToLotInfo transitionInfo) throws CustomException
	{
		try
		{
			lotData = LotServiceProxy.getLotService().transferProductsToLot(lotData.getKey(), eventInfo, transitionInfo);

			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey = " + eventInfo.getEventTimeKey());

			return lotData;
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("LOT-9003", lotData.getKey().getLotName());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("LOT-9999", fe.getMessage());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("LOT-9002", lotData.getKey().getLotName());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("LOT-9001", lotData.getKey().getLotName());
		}
	}
	
	public Lot trackInLot(EventInfo eventInfo, Lot lotData, MakeLoggedInInfo makeLoggedInInfo) throws CustomException
	{
		try
		{
			Lot trackInLotData = LotServiceProxy.getLotService().makeLoggedIn(lotData.getKey(), eventInfo, makeLoggedInInfo);
			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
			
			return trackInLotData;
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("LOT-9003", lotData.getKey().getLotName());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("LOT-9999", fe.getMessage());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("LOT-9002", lotData.getKey().getLotName());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("LOT-9001", lotData.getKey().getLotName());
		}
	}
	
	public Lot trackOutLot(EventInfo eventInfo, Lot lotData, MakeLoggedOutInfo makeLoggedOutInfo) throws CustomException
	{
		Lot trackOutLotData = new Lot();
		try
		{
			trackOutLotData = LotServiceProxy.getLotService().makeLoggedOut(lotData.getKey(), eventInfo, makeLoggedOutInfo);
			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("LOT-9003", lotData.getKey().getLotName());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("LOT-9999", fe.getMessage());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("LOT-9002", lotData.getKey().getLotName());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("LOT-9001", lotData.getKey().getLotName());
		}
		return trackOutLotData;
	}
	
	public Lot cancelTrackIn(EventInfo eventInfo, Lot lotData, List<ProductPGSRC> productPGSRCSequence, Map<String, String> assignCarrierUdfs, Map<String, String> deassignCarrierUdfs,
			List<ConsumedMaterial> consumedMaterial, String carrierName) throws CustomException
	{
		eventInfo.setEventName("CancelTrackIn");
		String areaName = lotData.getAreaName();

		String machineName = "";
		String machineRecipeName = "";
		String lotGrade = lotData.getLotGrade();
		String processFlowName = lotData.getProcessFlowName();
		String processFlowVersion = lotData.getProcessFlowVersion();
		String processOperationName = lotData.getProcessOperationName();
		String processOperationVersion = lotData.getProcessOperationVersion();
		
		String reworkFlag = "";
		String completeFlag = "";

		MakeLoggedOutInfo makeLoggedOutInfo = new MakeLoggedOutInfo();

		makeLoggedOutInfo.setAreaName(areaName);
		if (StringUtil.isEmpty(lotData.getCarrierName()) && StringUtil.isNotEmpty(carrierName))
			makeLoggedOutInfo.setCarrierName(carrierName);
		else
			makeLoggedOutInfo.setCarrierName(lotData.getCarrierName());

		makeLoggedOutInfo.setLotGrade(lotGrade);
		makeLoggedOutInfo.setMachineName(machineName);
		makeLoggedOutInfo.setMachineRecipeName(machineRecipeName);
		makeLoggedOutInfo.setProcessFlowName(processFlowName);
		makeLoggedOutInfo.setProcessFlowVersion(processFlowVersion);
		makeLoggedOutInfo.setProcessOperationName(processOperationName);
		makeLoggedOutInfo.setProcessOperationVersion(processOperationVersion);
		makeLoggedOutInfo.setNodeStack(lotData.getNodeStack());
		makeLoggedOutInfo.setReworkFlag(reworkFlag);
		makeLoggedOutInfo.setReworkNodeId(lotData.getReworkNodeId());
		makeLoggedOutInfo.setCompleteFlag(completeFlag);
		makeLoggedOutInfo.setProductPGSRCSequence(productPGSRCSequence);
		makeLoggedOutInfo.setAssignCarrierUdfs(assignCarrierUdfs);
		makeLoggedOutInfo.setDeassignCarrierUdfs(deassignCarrierUdfs);
		makeLoggedOutInfo.setConsumedMaterialSequence(consumedMaterial);

		LotServiceProxy.getLotService().makeLoggedOut(lotData.getKey(), eventInfo, makeLoggedOutInfo);

		return lotData;
	}
	
	public Lot receiveLot(EventInfo eventInfo, Lot lotData, MakeReceivedInfo makeReceivedInfo)
	{
		Lot receiveLotData = LotServiceProxy.getLotService().makeReceived(lotData.getKey(), eventInfo, makeReceivedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());

		return receiveLotData;
	}

	public Lot shipLot(EventInfo eventInfo, Lot lotData, MakeShippedInfo makeShippedInfo)
	{
		Lot shipLotData = LotServiceProxy.getLotService().makeShipped(lotData.getKey(), eventInfo, makeShippedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());

		return shipLotData;
	}

	public Lot unShipLot(EventInfo eventInfo, Lot lotData, MakeUnShippedInfo makeUnShippedInfo)
	{
		Lot unShipLotData = LotServiceProxy.getLotService().makeUnShipped(lotData.getKey(), eventInfo, makeUnShippedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());

		return unShipLotData;
	}

	
	public Lot changeProcessOperation(EventInfo eventInfo, Lot lotData, ChangeSpecInfo changeSpecInfo) throws CustomException
	{
		try
		{
			Lot changeOperLotData = LotServiceProxy.getLotService().changeSpec(lotData.getKey(), eventInfo, changeSpecInfo);
			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());

			return changeOperLotData;
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("LOT-9003", lotData.getKey().getLotName());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("LOT-9999", fe.getMessage());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("LOT-9002", lotData.getKey().getLotName());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("LOT-9001", lotData.getKey().getLotName());
		}
	}
	
	public Lot startRework(EventInfo eventInfo, Lot lotData, MakeInReworkInfo makeInReworkInfo)
	{
		Lot preLotData = (Lot) ObjectUtil.copyTo(lotData);
		Lot startReworkData = LotServiceProxy.getLotService().makeInRework(lotData.getKey(), eventInfo, makeInReworkInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());

		try
		{
			startReworkData = MESLotServiceProxy.getLotServiceUtil().executeReserveAction(eventInfo, preLotData, startReworkData);
		}
		catch (Exception err)
		{
		}
		return startReworkData;
	}
	
	public Lot completeRework(EventInfo eventInfo, Lot lotData, MakeNotInReworkInfo makeNotInReworkInfo)
	{
		Lot completeReworkData = LotServiceProxy.getLotService().makeNotInRework(lotData.getKey(), eventInfo, makeNotInReworkInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());

		return completeReworkData;
	}

	public Lot CutLot(EventInfo eventInfo, Lot lotData, SeparateInfo separateInfo)
	{
		Lot CutLotData = LotServiceProxy.getLotService().separate(lotData.getKey(), eventInfo, separateInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());

		return CutLotData;
	}

	public Lot ChangeGrade(EventInfo eventInfo, Lot lotData, ChangeGradeInfo changeGradeInfo)
	{
		Lot changeGradeData = LotServiceProxy.getLotService().changeGrade(lotData.getKey(), eventInfo, changeGradeInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());

		return changeGradeData;
	}

	public Lot makeUnScrapped(EventInfo eventInfo, Lot lotData, MakeUnScrappedInfo makeUnScrappedInfo) throws CustomException
	{
		try
		{
			Lot result = LotServiceProxy.getLotService().makeUnScrapped(lotData.getKey(), eventInfo, makeUnScrappedInfo);
			log.info(String.format("EventName[%s] EventTimeKey[%s]", eventInfo.getEventName(), eventInfo.getEventTimeKey()));

			return result;
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("LOT-9003", lotData.getKey().getLotName());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("LOT-9999", fe.getMessage());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("LOT-9002", lotData.getKey().getLotName());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("LOT-9001", lotData.getKey().getLotName());
		}
	}

	public Lot splitLot(EventInfo eventInfo, Lot lotData, SplitInfo splitInfo)
	{
		Lot splitLot = LotServiceProxy.getLotService().split(lotData.getKey(), eventInfo, splitInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());

		return splitLot;
	}

	public Lot releaseLot(EventInfo eventInfo, Lot lotData, MakeReleasedInfo makeReleasedInfo, List<ProductPGS> productPGSSequence) throws CustomException
	{
		try
		{
			// by actual Product amount
			if (productPGSSequence != null && productPGSSequence.size() > 0)
			{
				makeReleasedInfo.setProductPGSSequence(productPGSSequence);
				makeReleasedInfo.setProductQuantity(productPGSSequence.size());
			}
			else
			{
				makeReleasedInfo.setProductPGSSequence(new ArrayList<ProductPGS>());
				makeReleasedInfo.setProductQuantity(0);
			}

			Lot result = LotServiceProxy.getLotService().makeReleased(lotData.getKey(), eventInfo, makeReleasedInfo);

			return result;
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("LOT-9003", lotData.getKey().getLotName());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("LOT-9999", fe.getMessage());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("LOT-9002", lotData.getKey().getLotName());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("LOT-9001", lotData.getKey().getLotName());
		}
	}
	
	public void assignCarrier(Lot lotData, AssignCarrierInfo assignCarrierInfo, EventInfo eventInfo) throws CustomException
	{
		try
		{
			LotServiceProxy.getLotService().assignCarrier(lotData.getKey(), eventInfo, assignCarrierInfo);
			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("LOT-9003", lotData.getKey().getLotName());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("LOT-9999", fe.getMessage()); 
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("LOT-9002", lotData.getKey().getLotName());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("LOT-9001", lotData.getKey().getLotName());
		}
	}
	
	
	public Lot deassignCarrier(Lot lotData, DeassignCarrierInfo deassignCarrierInfo, EventInfo eventInfo) throws CustomException
	{
		try
		{
			if (!lotData.getCarrierName().isEmpty())
			{
				Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());

				if (!eventInfo.getEventTimeKey().isEmpty())
				{
					if (durableData.getLastEventTimeKey().compareTo(eventInfo.getEventTimeKey()) >= 0)
						eventInfo.setEventTimeKey(durableData.getLastEventTimeKey());
				}
			}

			lotData = LotServiceProxy.getLotService().deassignCarrier(lotData.getKey(), eventInfo, deassignCarrierInfo);

			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("LOT-9003", lotData.getKey().getLotName());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("LOT-9999", fe.getMessage());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("LOT-9002", lotData.getKey().getLotName());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("LOT-9001", lotData.getKey().getLotName());
		}

		return lotData;
	}
	
	public void relocateProducts(Lot lotData, RelocateProductsInfo relocateProductsInfo, EventInfo eventInfo) throws CustomException
	{
		try
		{
			LotServiceProxy.getLotService().relocateProducts(lotData.getKey(), eventInfo, relocateProductsInfo);
			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		}
		catch (FrameworkErrorSignal de)
		{
			throw new CustomException("LOT-9999", de.getMessage());
		}
		catch (Exception e)
		{
			throw new CustomException("LOT-0001", lotData.getKey().getLotName());
		}

	}
	
	public void deleteLotReturnInformation(Lot lotData) throws CustomException
	{
		try
		{
			String sql = " UPDATE LOT " +
						 "   SET RETURNFLOWNAME = '' , " +
						 "       RETURNOPERATIONNAME = '' " +
						 " WHERE LOTNAME = :lotName ";
			 
			Map<String,Object> bindMap = new HashMap<String,Object>();
			bindMap.put("lotName" , lotData.getKey().getLotName());

			GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		}
		catch(Exception e)
		{	
		}
	}

	public void lotMultiHold(EventInfo eventInfo, Lot lotData, Map<String, String> udfs) throws CustomException
	{
		// Set EventInfo
		eventInfo = EventInfoUtil.makeEventInfo("Hold", eventInfo.getEventUser(), eventInfo.getEventComment(), eventInfo.getReasonCodeType(), eventInfo.getReasonCode());

		// Update HoldState for execution MultiHold
		if (StringUtil.equals(lotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_OnHold))
		{
			// Update LotHoldState - N
			String sql = "UPDATE LOT SET LOTHOLDSTATE = :LOTHOLDSTATE WHERE LOTNAME = :LOTNAME ";
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LOTHOLDSTATE", GenericServiceProxy.getConstantMap().Lot_NotOnHold);
			bindMap.put("LOTNAME", lotData.getKey().getLotName());

			GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);

			// Update ProductHoldState - N
			sql = "UPDATE PRODUCT SET PRODUCTHOLDSTATE = :PRODUCTHOLDSTATE WHERE LOTNAME = :LOTNAME ";
			bindMap.clear();
			bindMap.put("PRODUCTHOLDSTATE", GenericServiceProxy.getConstantMap().Prod_NotOnHold);
			bindMap.put("LOTNAME", lotData.getKey().getLotName());

			GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		}

		// Get ProductUSequence
		List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);

		// Set MakeOnHoldInfo
		MakeOnHoldInfo makeOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeOnHoldInfo(productUSequence, udfs);
		LotServiceProxy.getLotService().makeOnHold(lotData.getKey(), eventInfo, makeOnHoldInfo);

		// Set Udfs - PROCESSOPERATIONNAME to Insert OperationName MultiHold
		udfs.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());

		// Insert into LOTMULTIHOLD table
		LotMultiHoldKey holdKey = new LotMultiHoldKey();
		holdKey.setLotName(lotData.getKey().getLotName());
		holdKey.setReasonCode(eventInfo.getReasonCode());

		LotMultiHold holdData = new LotMultiHold();
		holdData.setKey(holdKey);
		holdData.setEventName(eventInfo.getEventName());
		holdData.setEventTime(eventInfo.getEventTime());
		holdData.setEventComment(eventInfo.getEventComment());
		holdData.setEventUser(eventInfo.getEventUser());
		holdData.setUdfs(udfs);

		try
		{
			LotServiceProxy.getLotMultiHoldService().insert(holdData);
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("LOT-0002", holdKey.getLotName(), holdKey.getReasonCode());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("LOT-9999", fe.getMessage());
		}

		// insert in PRODUCTMULTIHOLD table
		MESProductServiceProxy.getProductServiceImpl().setProductMultiHold(eventInfo, lotData.getKey().getLotName(), udfs);
	}
	
	public Lot lotMultiHoldR(EventInfo eventInfo, Lot lotData, Map<String, String> udfs) throws CustomException
	{
		// Set EventInfo
		eventInfo = EventInfoUtil.makeEventInfo("Hold", eventInfo.getEventUser(), eventInfo.getEventComment(), eventInfo.getReasonCodeType(), eventInfo.getReasonCode());

		// Update HoldState for execution MultiHold
		if (StringUtil.equals(lotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_OnHold))
		{
			// Update LotHoldState - N
			String sql = "UPDATE LOT SET LOTHOLDSTATE = :LOTHOLDSTATE WHERE LOTNAME = :LOTNAME ";
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LOTHOLDSTATE", GenericServiceProxy.getConstantMap().Lot_NotOnHold);
			bindMap.put("LOTNAME", lotData.getKey().getLotName());

			GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);

			// Update ProductHoldState - N
			sql = "UPDATE PRODUCT SET PRODUCTHOLDSTATE = :PRODUCTHOLDSTATE WHERE LOTNAME = :LOTNAME ";
			bindMap.clear();
			bindMap.put("PRODUCTHOLDSTATE", GenericServiceProxy.getConstantMap().Prod_NotOnHold);
			bindMap.put("LOTNAME", lotData.getKey().getLotName());

			GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		}

		// Get ProductUSequence
		List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);

		// Set MakeOnHoldInfo
		MakeOnHoldInfo makeOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeOnHoldInfo(productUSequence, udfs);
		lotData = LotServiceProxy.getLotService().makeOnHold(lotData.getKey(), eventInfo, makeOnHoldInfo);

		// Set Udfs - PROCESSOPERATIONNAME to Insert OperationName MultiHold
		udfs.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());

		// Insert into LOTMULTIHOLD table
		LotMultiHoldKey holdKey = new LotMultiHoldKey();
		holdKey.setLotName(lotData.getKey().getLotName());
		holdKey.setReasonCode(eventInfo.getReasonCode());

		LotMultiHold holdData = new LotMultiHold();
		holdData.setKey(holdKey);
		holdData.setEventName(eventInfo.getEventName());
		holdData.setEventTime(eventInfo.getEventTime());
		holdData.setEventComment(eventInfo.getEventComment());
		holdData.setEventUser(eventInfo.getEventUser());
		holdData.setUdfs(udfs);

		try
		{
			LotServiceProxy.getLotMultiHoldService().insert(holdData);
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("LOT-0002", holdKey.getLotName(), holdKey.getReasonCode());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("LOT-9999", fe.getMessage());
		}

		// insert in PRODUCTMULTIHOLD table
		MESProductServiceProxy.getProductServiceImpl().setProductMultiHold(eventInfo, lotData.getKey().getLotName(), udfs);
		
		return lotData;
	}

	public void MakeEmptied(EventInfo eventInfo, Lot lotData, List<ProductU> productUSequence, Map<String, String> deassignCarrierUdfs) throws CustomException
	{
		MakeEmptiedInfo makeEmptiedInfo = new MakeEmptiedInfo();
		makeEmptiedInfo.setDeassignCarrierUdfs(deassignCarrierUdfs);
		makeEmptiedInfo.setProductUSequence(productUSequence);
		makeEmptiedInfo.setUdfs(lotData.getUdfs());

		LotServiceProxy.getLotService().makeEmptied(lotData.getKey(), eventInfo, makeEmptiedInfo);
	}

	public void insertCtReviewProductJudge(EventInfo eventInfo, String ProductName, String ProcessOperationName, String MachineName, String ProductJudge, String ActionKind,boolean dmFlag) throws CustomException
	{
		String sql = "";

		Map<String, Object> bindMap = new HashMap<String, Object>();

		if(ActionKind.equals("Insert"))
		{
			if(!dmFlag)
			{
				sql = " INSERT INTO CT_REVIEWPRODUCTJUDGE "
						+ " ( PRODUCTNAME, PROCESSOPERATIONNAME, MACHINENAME, ASSIGNUSER, ASSIGNTIME, PRODUCTJUDGE, CREATETIME, CREATEUSER, LASTEVENTTIME, LASTEVENTUSER) " 
						+ " VALUES "
						+ " ( :ProductName, :ProcessOperationName, :MachineName, :AssignUser, :AssignTime, :ProductJudge, :CreateTime, :CreateUser, :LastEventTime, :LastEventUser ) ";
				bindMap.put("ProductName", ProductName);
				bindMap.put("ProcessOperationName", ProcessOperationName);
				bindMap.put("MachineName", MachineName);
				bindMap.put("AssignUser", eventInfo.getEventUser());
				bindMap.put("AssignTime", eventInfo.getEventTime());
				bindMap.put("ProductJudge", ProductJudge);
				bindMap.put("CreateTime", eventInfo.getEventTime());
				bindMap.put("CreateUser", eventInfo.getEventUser());
				bindMap.put("LastEventTime", eventInfo.getEventTime());
				bindMap.put("LastEventUser", eventInfo.getEventUser());
			}
			else
			{
				sql = " INSERT INTO CT_REVIEWPRODUCTJUDGE "
						+ " ( PRODUCTNAME, PROCESSOPERATIONNAME, MACHINENAME,  DMASSIGNUSER, ASSIGNTIME, DMPRODUCTJUDGE, DMCREATETIME, DMCREATEUSER, DMLASTEVENTTIME, DMLASTEVENTUSER) " 
						+ " VALUES "
						+ " ( :ProductName, :ProcessOperationName, :MachineName, :DMAssignUser, :DMAssignTime, :DMProductJudge, :DMCreateTime, :DMCreateUser, :DMLastEventTime, :DMLastEventUser ) ";
				bindMap.put("ProductName", ProductName);
				bindMap.put("ProcessOperationName", ProcessOperationName);
				bindMap.put("MachineName", MachineName);
				bindMap.put("DMAssignUser", eventInfo.getEventUser());
				bindMap.put("DMAssignTime", eventInfo.getEventTime());
				bindMap.put("DMProductJudge", ProductJudge);
				bindMap.put("DMCreateTime", eventInfo.getEventTime());
				bindMap.put("DMCreateUser", eventInfo.getEventUser());
				bindMap.put("DMLastEventTime", eventInfo.getEventTime());
				bindMap.put("DMLastEventUser", eventInfo.getEventUser());			
			}

		}
		else if(ActionKind.equals("Update"))
		{
			if(eventInfo.getEventName().equals("AssignUser"))
			{
				sql = " UPDATE CT_REVIEWPRODUCTJUDGE "
						+ " Set "
						+ " ASSIGNUSER = :AssignUser, "
						+ " ASSIGNTIME = :AssignTime, "
						+ " PRODUCTJUDGE = '', "
						+ " LASTEVENTTIME = :LastEventTime, "
						+ " LASTEVENTUSER = :LastEventUser  "
						+ " where 1 = 1"
						+ " and PRODUCTNAME = :ProductName "
						+ " and PROCESSOPERATIONNAME = :ProcessOperationName"
						+ " and MACHINENAME = :MachineName";
				bindMap.put("ProductName", ProductName);
				bindMap.put("ProcessOperationName", ProcessOperationName);
				bindMap.put("MachineName", MachineName);
				bindMap.put("AssignUser", eventInfo.getEventUser());
				bindMap.put("AssignTime", eventInfo.getEventTime());
				bindMap.put("ProductJudge", ProductJudge);
				bindMap.put("LastEventTime", eventInfo.getEventTime());
				bindMap.put("LastEventUser", eventInfo.getEventUser());
			}
			else if (eventInfo.getEventName().equals("AssignDMUser"))
			{
				sql = " UPDATE CT_REVIEWPRODUCTJUDGE "
						+ " Set "
						+ " DMASSIGNUSER = :AssignUser, "
						+ " DMASSIGNTIME = :AssignTime, "
						+ " DMPRODUCTJUDGE = '', "
						+ " DMLASTEVENTTIME = :LastEventTime, "
						+ " DMLASTEVENTUSER = :LastEventUser  "
						+ " where 1 = 1"
						+ " and PRODUCTNAME = :ProductName "
						+ " and PROCESSOPERATIONNAME = :ProcessOperationName"
						+ " and MACHINENAME = :MachineName";
				bindMap.put("ProductName", ProductName);
				bindMap.put("ProcessOperationName", ProcessOperationName);
				bindMap.put("MachineName", MachineName);
				bindMap.put("AssignUser", eventInfo.getEventUser());
				bindMap.put("AssignTime", eventInfo.getEventTime());
				bindMap.put("ProductJudge", ProductJudge);
				bindMap.put("LastEventTime", eventInfo.getEventTime());
				bindMap.put("LastEventUser", eventInfo.getEventUser());
			}
			else if(eventInfo.getEventName().equals("ClearDMAssignUser"))
			{
				sql = " UPDATE CT_REVIEWPRODUCTJUDGE "
						+ " Set "
						+ " DMASSIGNUSER = '', "
						+ " DMASSIGNTIME = :AssignTime, "
						+ " DMPRODUCTJUDGE = 'G', "
						+ " DMLASTEVENTTIME = :LastEventTime, "
						+ " DMLASTEVENTUSER = :LastEventUser  "
						+ " where 1 = 1"
						+ " and PRODUCTNAME = :ProductName "
						+ " and PROCESSOPERATIONNAME = :ProcessOperationName"
						+ " and MACHINENAME = :MachineName";
				bindMap.put("ProductName", ProductName);
				bindMap.put("ProcessOperationName", ProcessOperationName);
				bindMap.put("MachineName", MachineName);
				bindMap.put("AssignTime", eventInfo.getEventTime());
				bindMap.put("LastEventTime", eventInfo.getEventTime());
				bindMap.put("LastEventUser", eventInfo.getEventUser());
			}
			else 
			{
				sql = " UPDATE CT_REVIEWPRODUCTJUDGE "
						+ " Set "
						+ " ASSIGNUSER = '', "
						+ " ASSIGNTIME = :AssignTime, "
						+ " PRODUCTJUDGE = :ProductJudge, "
						+ " LASTEVENTTIME = :LastEventTime, "
						+ " LASTEVENTUSER = :LastEventUser  "
						+ " where 1 = 1"
						+ " and PRODUCTNAME = :ProductName "
						+ " and PROCESSOPERATIONNAME = :ProcessOperationName"
						+ " and MACHINENAME = :MachineName";
				bindMap.put("ProductName", ProductName);
				bindMap.put("ProcessOperationName", ProcessOperationName);
				bindMap.put("MachineName", MachineName);
				bindMap.put("AssignTime", eventInfo.getEventTime());
				bindMap.put("ProductJudge", ProductJudge);
				bindMap.put("LastEventTime", eventInfo.getEventTime());
				bindMap.put("LastEventUser", eventInfo.getEventUser());
			}
		}

		GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
	}

	public void insertCtReviewProductImageJudge(EventInfo eventInfo, String ProductName, String ProcessOperationName, String MachineName, String ImageName, String ScreenJudge, String DefectCode,
			String DefectJudge, String PanelName, String ActionKind, String detaX, String detaY, String detaX2, String detaY2) throws CustomException
	{
		String sql = "";

		Map<String, Object> bindMap = new HashMap<String, Object>();

		if(ActionKind.equals("Insert"))
		{
			sql = " INSERT INTO CT_REVIEWPRODUCTIMAGEJUDGE "
					+ " ( PRODUCTNAME, PROCESSOPERATIONNAME, MACHINENAME, IMAGESEQ, SCREENJUDGE, DEFECTJUDGE, DEFECTCODE, CREATETIME, CREATEUSER, LASTEVENTTIME, LASTEVENTUSER, PANELNAME, DETAX, DETAY, DETAX2, DETAY2, LASTEVENTTIMEKEY) " 
					+ " VALUES "
					+ " ( :ProductName, :ProcessOperationName, :MachineName, :ImageSeq, :ScreenJudge, :DefectJudge, :DefectCode, :CreateTime, :CreateUser, :LastEventTime, :LastEventUser, :PanelName, :DETAX, :DETAY, :DETAX2, :DETAY2, :LASTEVENTTIMEKEY ) ";

			bindMap.put("ProductName", ProductName);
			bindMap.put("ProcessOperationName", ProcessOperationName);
			bindMap.put("MachineName", MachineName);
			bindMap.put("ImageSeq", ImageName);
			bindMap.put("ScreenJudge", ScreenJudge);
			bindMap.put("DefectJudge", DefectJudge);
			bindMap.put("DefectCode", DefectCode);
			bindMap.put("CreateTime", eventInfo.getEventTime());
			bindMap.put("CreateUser", eventInfo.getEventUser());
			bindMap.put("LastEventTime", eventInfo.getEventTime());
			bindMap.put("LastEventUser", eventInfo.getEventUser());
			bindMap.put("PanelName", PanelName);
			bindMap.put("DETAX", detaX);
			bindMap.put("DETAY", detaY);
			bindMap.put("DETAX2", detaX2);
			bindMap.put("DETAY2", detaY2);
			bindMap.put("LASTEVENTTIMEKEY", eventInfo.getEventTimeKey());
		}
		else if(ActionKind.equals("Update"))
		{
			sql = " UPDATE CT_REVIEWPRODUCTIMAGEJUDGE "
					+ " Set "
					+ " SCREENJUDGE = :ScreenJudge, "
					+ " DEFECTJUDGE = :DefectJudge, "
					+ " DEFECTCODE = :DefectCode, "
					+ " LASTEVENTTIME = :LastEventTime, "
					+ " LASTEVENTUSER = :LastEventUser,  "
					+ " PANELNAME = :PanelName,  "
					+ " LASTEVENTTIMEKEY = :LASTEVENTTIMEKEY  "
					+ " where 1 = 1"
					+ " and PRODUCTNAME = :ProductName "
					+ " and PROCESSOPERATIONNAME = :ProcessOperationName"
					+ " and MACHINENAME = :MachineName"
					+ " and IMAGESEQ = :ImageSeq";
			bindMap.put("ProductName", ProductName);
			bindMap.put("ProcessOperationName", ProcessOperationName);
			bindMap.put("MachineName", MachineName);
			bindMap.put("ImageSeq", ImageName);
			bindMap.put("ScreenJudge", ScreenJudge);
			bindMap.put("DefectJudge", DefectJudge);
			bindMap.put("DefectCode", DefectCode);
			bindMap.put("LastEventTime", eventInfo.getEventTime());
			bindMap.put("LastEventUser", eventInfo.getEventUser());
			bindMap.put("PanelName", PanelName);
			bindMap.put("LASTEVENTTIMEKEY", eventInfo.getEventTimeKey());
		}

		GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
	}


	public void insertCtReviewProductImageJudgeSeq(EventInfo eventInfo, String ProductName, String ProcessOperationName, String MachineName, String ImageName, String ScreenJudge, String DefectCode,
			String DefectJudge, String PanelName, String Seq, String PanelGrade, String ActionKind) throws CustomException
	{
		String sql = "";

		Map<String, Object> bindMap = new HashMap<String, Object>();

		if(ActionKind.equals("Insert"))
		{
			sql = " INSERT INTO CT_REVIEWPRODUCTIMAGEJUDGE "
					+ " ( PRODUCTNAME, PROCESSOPERATIONNAME, MACHINENAME, IMAGESEQ, SCREENJUDGE, DEFECTJUDGE, DEFECTCODE, CREATETIME, CREATEUSER, LASTEVENTTIME, LASTEVENTUSER, PANELNAME, PANELGRADE, SEQ, LASTEVENTTIMEKEY) " 
					+ " VALUES "
					+ " ( :ProductName, :ProcessOperationName, :MachineName, :ImageSeq, :ScreenJudge, :DefectJudge, :DefectCode, :CreateTime, :CreateUser, :LastEventTime, :LastEventUser, :PanelName, :PanelGrade, :Seq, :LASTEVENTTIMEKEY ) ";
			bindMap.put("ProductName", ProductName);
			bindMap.put("ProcessOperationName", ProcessOperationName);
			bindMap.put("MachineName", MachineName);
			bindMap.put("ImageSeq", ImageName);
			bindMap.put("ScreenJudge", ScreenJudge);
			bindMap.put("DefectJudge", DefectJudge);
			bindMap.put("DefectCode", DefectCode);
			bindMap.put("CreateTime", eventInfo.getEventTime());
			bindMap.put("CreateUser", eventInfo.getEventUser());
			bindMap.put("LastEventTime", eventInfo.getEventTime());
			bindMap.put("LastEventUser", eventInfo.getEventUser());
			bindMap.put("PanelName", PanelName);
			bindMap.put("PanelGrade", PanelGrade);
			bindMap.put("Seq", Seq);
			bindMap.put("LASTEVENTTIMEKEY", eventInfo.getEventTimeKey());
		}
		else if(ActionKind.equals("Update"))
		{
			sql = " UPDATE CT_REVIEWPRODUCTIMAGEJUDGE "
					+ " Set "
					+ " SCREENJUDGE = :ScreenJudge, "
					+ " DEFECTJUDGE = :DefectJudge, "
					+ " DEFECTCODE = :DefectCode, "
					+ " LASTEVENTTIME = :LastEventTime, "
					+ " LASTEVENTUSER = :LastEventUser,  "
					+ " PANELNAME = :PanelName,  "
					+ " PANELGRADE = :PanelGrade,  "
					+ " LASTEVENTTIMEKEY = :LASTEVENTTIMEKEY  "
					+ " where 1 = 1"
					+ " and PRODUCTNAME = :ProductName "
					+ " and PROCESSOPERATIONNAME = :ProcessOperationName"
					+ " and MACHINENAME = :MachineName"
					+ " and IMAGESEQ = :ImageSeq"
					+ " and SEQ = :Seq";
			bindMap.put("ProductName", ProductName);
			bindMap.put("ProcessOperationName", ProcessOperationName);
			bindMap.put("MachineName", MachineName);
			bindMap.put("ImageSeq", ImageName);
			bindMap.put("ScreenJudge", ScreenJudge);
			bindMap.put("DefectJudge", DefectJudge);
			bindMap.put("DefectCode", DefectCode);
			bindMap.put("LastEventTime", eventInfo.getEventTime());
			bindMap.put("LastEventUser", eventInfo.getEventUser());
			bindMap.put("PanelName", PanelName);
			bindMap.put("PanelGrade", PanelGrade);
			bindMap.put("Seq", Seq);
			bindMap.put("LASTEVENTTIMEKEY", eventInfo.getEventTimeKey());
		}

		GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
	}

	public void DeleteCtReviewProductImageJudge(EventInfo eventInfo, String ProductName, String ProcessOperationName, String MachineName, String ImageName) throws CustomException
	{
		String sql = "";

		Map<String, Object> bindMap = new HashMap<String, Object>();

		sql = " Delete from CT_REVIEWPRODUCTIMAGEJUDGE "
				+ " where 1 = 1"
				+ " and PRODUCTNAME = :ProductName "
				+ " and PROCESSOPERATIONNAME = :ProcessOperationName"
				+ " and MACHINENAME = :MachineName"
				+ " and IMAGESEQ = :ImageSeq";
		bindMap.put("ProductName", ProductName);
		bindMap.put("ProcessOperationName", ProcessOperationName);
		bindMap.put("MachineName", MachineName);
		bindMap.put("ImageSeq", ImageName);

		GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
	}

	public void deleteCtLotFutureAction(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, String position) throws CustomException
	{
		log.info("Delete LotFutureAction Start.");
		List<LotFutureAction> lotFutureActionList = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataListWithoutReasonCode(lotName, factoryName, processFlowName,
				processFlowVersion, processOperationName, processOperationVersion, Integer.parseInt(position));

		if (lotFutureActionList == null)
			throw new CustomException("JOB-9001", lotName);

		ExtendedObjectProxy.getLotFutureActionService().deleteLotFutureActionWithoutReaconCode(eventInfo, lotName, factoryName, processFlowName, processFlowVersion, processOperationName,
				processOperationVersion, Integer.parseInt(position));
		
		log.info("Delete LotFutureAction End.");
	}
	
	public void deleteCtLotFutureActionByFlow(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processFlowVersion, String position) throws CustomException
	{
		log.info("Delete LotFutureAction Start.");
		
		List<LotFutureAction> lotFutureActionList = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataListByFlow(lotName, factoryName, processFlowName, processFlowVersion,
				Integer.parseInt(position));
		
		if (lotFutureActionList == null)
		{
			log.info("LotFutureAction Data not exist.");
		}
		else 
		{
			ExtendedObjectProxy.getLotFutureActionService().deleteLotFutureActionByFlow(eventInfo, lotName, factoryName, processFlowName, processFlowVersion, Integer.parseInt(position));

			log.info("Delete LotFutureAction End.");
		}
	}

	public void deleteCtLotFutureActionbyReasonCode(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, String position, String reasonCode) throws CustomException
	{
		log.info("Delete LotFutureAction Start.");
		List<LotFutureAction> lotFutureActionList = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataList(lotName, factoryName, processFlowName, processFlowVersion,
				processOperationName, processOperationVersion, Integer.parseInt(position), reasonCode);
		
		if (lotFutureActionList == null)
			throw new CustomException("JOB-9001", lotName);

		ExtendedObjectProxy.getLotFutureActionService().deleteLotFutureActionData(eventInfo, lotName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion,
				Integer.parseInt(position), reasonCode);
		
		log.info("Delete LotFutureAction End.");
	}

	public Lot setEventForce(EventInfo eventInfo, Lot lotData)
	{
		try
		{
			Lot newLotData = (Lot) ObjectUtil.copyTo(lotData);
			newLotData.setLastEventComment(eventInfo.getEventComment());
			newLotData.setLastEventName(eventInfo.getEventName());
			newLotData.setLastEventTime(eventInfo.getEventTime());
			newLotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			newLotData.setLastEventUser(eventInfo.getEventUser());
			newLotData.setReasonCode(eventInfo.getReasonCode());
			newLotData.setReasonCodeType(eventInfo.getReasonCodeType());

			LotServiceProxy.getLotService().update(newLotData);

			LotHistory HistoryData = new LotHistory();
			HistoryData = LotServiceProxy.getLotHistoryDataAdaptor().setHV(lotData, newLotData, HistoryData);

			LotServiceProxy.getLotHistoryService().insert(HistoryData);

			return newLotData;
		}
		catch (Exception e)
		{
			log.info("Error occurred - setEventForce");
			return lotData;
		}
	}

	public Lot setEventForce(EventInfo eventInfo, Lot oldLotData, Lot newLotData) throws CustomException
	{
		try
		{
			newLotData.setLastEventComment(eventInfo.getEventComment());
			newLotData.setLastEventName(eventInfo.getEventName());
			newLotData.setLastEventTime(eventInfo.getEventTime());
			newLotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			newLotData.setLastEventUser(eventInfo.getEventUser());
			newLotData.setReasonCode(eventInfo.getReasonCode());
			newLotData.setReasonCodeType(eventInfo.getReasonCodeType());

			LotServiceProxy.getLotService().update(newLotData);

			LotHistory HistoryData = new LotHistory();
			HistoryData = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLotData, newLotData, HistoryData);

			LotServiceProxy.getLotHistoryService().insert(HistoryData);

			return newLotData;
		}
		catch (Exception e)
		{
			log.info("Error occurred - setEventForce");
			throw new CustomException("LOT-9999", "Update Fail");
		}
	}

	public void RSchangeAOILotJudge(String lotName) throws CustomException
	{
		String CurrentTime = TimeStampUtil.getCurrentTime(TimeStampUtil.FORMAT_DEFAULT);
		String timeKey = TimeStampUtil.getCurrentEventTimeKey();
		
		String sql = " UPDATE CT_AOILOT SET LOTJUDGE = :AOILOTJUDGE,EVENTUSER = :EVENTUSER,EVENTNAME = :EVENTNAME,EVENTTIME = :EVENTTIME,TIMEKEY = :TIMEKEY WHERE TIMEKEY = (SELECT MAX(L.TIMEKEY) FROM CT_AOILOT L WHERE L.LOTNAME = :LOTNAME) ";
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("LOTNAME", lotName);
		bindMap.put("AOILOTJUDGE", "Y");
		bindMap.put("EVENTUSER", "MES");
		bindMap.put("EVENTNAME", "ChangeAOILotJudge");
		bindMap.put("EVENTTIME", CurrentTime);
		bindMap.put("TIMEKEY", timeKey);

		GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		
		Lot LotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		SetEventInfo setEventInfo = new SetEventInfo();
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeAOILotJudge-Y", "MES", "Auto change AOILotJudge", "", "");
		LotServiceProxy.getLotService().setEvent(LotData.getKey(), eventInfo, setEventInfo);
	}

	public void insertCtLotFutureMultiHoldActionForAfter(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, String position, String actionName, String actionType, String reasonCodeType, String reasonCode, String attribute1, String attribute2, String attribute3,
			String beforeAction, String afterAction, String beforeActionComment, String afterActionComment, String beforeActionUser, String afterActionUser, String ActionKind, String beforeMailFlag,
			String afterMailFlag) throws CustomException
	{
		eventInfo.setEventTimeKey(StringUtil.isEmpty(eventInfo.getEventTimeKey()) ? TimeStampUtil.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()) : eventInfo.getEventTimeKey());
		
		if(ActionKind.equals("Insert"))
		{
			ExtendedObjectProxy.getLotFutureActionService().insertLotFutureAction(eventInfo, lotName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion,
					Integer.parseInt(position), reasonCode, reasonCodeType, actionName, actionType, attribute1, attribute2, attribute3, beforeAction, afterAction, beforeActionComment, afterActionComment,
					beforeActionUser, afterActionUser, beforeMailFlag, afterMailFlag);
		}
		else if(ActionKind.equals("Update"))
		{
			ExtendedObjectProxy.getLotFutureActionService().updateLotFutureActionWithReasonCodeType(eventInfo, lotName, factoryName, processFlowName, processFlowVersion, processOperationName,
					processOperationVersion, Integer.parseInt(position), reasonCode, reasonCodeType, actionName, actionType, attribute1, attribute2, attribute3, beforeAction, afterAction,
					beforeActionComment, afterActionComment, beforeActionUser, afterActionUser, beforeMailFlag, afterMailFlag);
		}
		else if(ActionKind.equals("Delete"))
		{
			ExtendedObjectProxy.getLotFutureActionService().deleteLotFutureActionWithReasonCodeType(eventInfo, lotName, factoryName, processFlowName, processFlowVersion, processOperationName,
					processOperationVersion, Integer.parseInt(position), reasonCode, reasonCodeType);
		}
	}
	
	public void insertCtLotFutureMultiHoldActionForAfter(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, String position, String actionName, String actionType, String reasonCodeType, String reasonCode, String attribute1, String attribute2, String attribute3,
			String beforeAction, String afterAction, String beforeActionComment, String afterActionComment, String beforeActionUser, String afterActionUser, String ActionKind, String beforeMailFlag,
			String afterMailFlag, String requestDepartment, String releaseType) throws CustomException
	{
		eventInfo.setEventTimeKey(StringUtil.isEmpty(eventInfo.getEventTimeKey()) ? TimeStampUtil.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()) : eventInfo.getEventTimeKey());
		
		if(ActionKind.equals("Insert"))
		{
			ExtendedObjectProxy.getLotFutureActionService().insertLotFutureAction(eventInfo, lotName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion,
					Integer.parseInt(position), reasonCode, reasonCodeType, actionName, actionType, attribute1, attribute2, attribute3, beforeAction, afterAction, beforeActionComment, afterActionComment,
					beforeActionUser, afterActionUser, beforeMailFlag, afterMailFlag, requestDepartment, releaseType);
		}
		else if(ActionKind.equals("Update"))
		{
			ExtendedObjectProxy.getLotFutureActionService().updateLotFutureActionWithReasonCodeType(eventInfo, lotName, factoryName, processFlowName, processFlowVersion, processOperationName,
					processOperationVersion, Integer.parseInt(position), reasonCode, reasonCodeType, actionName, actionType, attribute1, attribute2, attribute3, beforeAction, afterAction,
					beforeActionComment, afterActionComment, beforeActionUser, afterActionUser, beforeMailFlag, afterMailFlag, requestDepartment, releaseType);
		}
		else if(ActionKind.equals("Delete"))
		{
			ExtendedObjectProxy.getLotFutureActionService().deleteLotFutureActionWithReasonCodeType(eventInfo, lotName, factoryName, processFlowName, processFlowVersion, processOperationName,
					processOperationVersion, Integer.parseInt(position), reasonCode, reasonCodeType);
		}
	}
	
	public void deleteCtLotFutureActionForAfter(EventInfo eventInfo, String lotName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, String position) throws CustomException
	{
		log.info("Delete LotFutureAction Start.");
		List<LotFutureAction> lotFutureActionList = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataListWithoutReasonCode(lotName, factoryName, processFlowName,
				processFlowVersion, processOperationName, processOperationVersion, Integer.parseInt(position));
		
		if (lotFutureActionList == null)
			throw new CustomException("JOB-9001", lotName);

		ExtendedObjectProxy.getLotFutureActionService().deleteLotFutureActionWithoutReaconCode(eventInfo, lotName, factoryName, processFlowName, processFlowVersion, processOperationName,
				processOperationVersion, Integer.parseInt(position));

		log.info("Delete LotFutureAction End.");
	}
	public void insertCtOfflineReviewProductImageJudge(EventInfo eventInfo, String productName, String processOperationName, String machineName, String imageName, String defectCode, String panelName,
			String actionKind, String defectX, String defectY)
	{
		String sql = "";

		Map<String, Object> bindMap = new HashMap<String, Object>();

		if(actionKind.equals("Insert"))
		{
			sql = " INSERT INTO CT_OFFLINEREVIEWIMAGECODE "
					+ " ( PRODUCTNAME, PROCESSOPERATIONNAME, MACHINENAME, IMAGESEQ, DEFECTCODE, CREATETIME, CREATEUSER, LASTEVENTTIME, LASTEVENTUSER, PANELNAME, DETAX, DETAY) " + " VALUES "
					+ " ( :ProductName, :ProcessOperationName, :MachineName, :ImageSeq, :DefectCode, :CreateTime, :CreateUser, :LastEventTime, :LastEventUser, :PanelName, :DETAX, :DETAY ) ";
			bindMap.put("ProductName", productName);
			bindMap.put("ProcessOperationName", processOperationName);
			bindMap.put("MachineName", machineName);
			bindMap.put("ImageSeq", imageName);
			bindMap.put("DefectCode", defectCode);
			bindMap.put("DETAX", defectX);
			bindMap.put("DETAY", defectY);
			bindMap.put("CreateTime", eventInfo.getEventTime());
			bindMap.put("CreateUser", eventInfo.getEventUser());
			bindMap.put("LastEventTime", eventInfo.getEventTime());
			bindMap.put("LastEventUser", eventInfo.getEventUser());
			bindMap.put("PanelName", panelName);
		}
		else if(actionKind.equals("Update"))
		{
			sql = " UPDATE CT_OFFLINEREVIEWIMAGECODE "
					+ " Set "
					+ " DEFECTCODE = :DefectCode, "
					+ " LASTEVENTTIME = :LastEventTime, "
					+ " LASTEVENTUSER = :LastEventUser,  "
					+ " PANELNAME = :PanelName  "
					+ " where 1 = 1"
					+ " and PRODUCTNAME = :ProductName "
					+ " and PROCESSOPERATIONNAME = :ProcessOperationName"
					+ " and MACHINENAME = :MachineName"
					+ " and IMAGESEQ = :ImageSeq";
			bindMap.put("ProductName", productName);
			bindMap.put("ProcessOperationName", processOperationName);
			bindMap.put("MachineName", machineName);
			bindMap.put("ImageSeq", imageName);
			bindMap.put("DefectCode", defectCode);
			bindMap.put("LastEventTime", eventInfo.getEventTime());
			bindMap.put("LastEventUser", eventInfo.getEventUser());
			bindMap.put("PanelName", panelName);
		}

		GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
	
	}

	public void sendEmailToMFG(EventInfo eventInfo, Lot lotData, String lotName) 
	{
		//Get EmailList
		List<String> emailList = new ArrayList<String>();
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT B.* FROM CT_ALARMUSERGROUP A, CT_ALARMUSER B WHERE A.USERID = B.USERID  AND A.ALARMGROUPNAME = 'MFGAbnormalOper' AND B.DEPARTMENT=:DEPARTMENT");
		
		Map<String, Object> args = new HashMap<String, Object>();
		
		args.put("DEPARTMENT", lotData.getFactoryName());
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate()
				.queryForList(sql.toString(), args);
		
		if (sqlResult.size() > 0) 
		{
			for (Map<String, Object> user : sqlResult)
			{
				String eMail = ConvertUtil.getMapValueByName(user, "EMAIL");
				emailList.add(eMail);
			}
		}
		
		//Execute Message
		StringBuffer messageBF = new StringBuffer();
		messageBF.append("<pre> 事件操作者: " + eventInfo.getEventUser() + "</pre>"
				+ "<pre> 事件名称: " + eventInfo.getEventName() + "</pre>"
				+ "<pre> 事件时间: " + eventInfo.getEventTime() + "</pre>"
				+ "<pre> 批次名: " + lotName + "</pre>"
				+ "<pre> 产品类型: " + lotData.getProductionType() + "</pre>"
				+ "<pre> 事件注解: " + eventInfo.getEventComment() + "</pre>");

		this.sendEmail(emailList, messageBF.toString(), "Abnormal Operation BY OP");
	}
	
	public void sendEmail(List<String> emailList, String message, String title)
	{
		if(emailList !=null && emailList.size()>0)
		{
			StringBuffer messageBF = new StringBuffer();
			messageBF.append("<pre> Dear All： </pre>");
			messageBF.append("<pre>===============" + title + "===============</pre>");
			messageBF.append(message);
		 
			try
			{
				EMailInterface ei = new EMailInterface("mail.visionox.com", "25", "1000", "1000");
				ei.postMail(emailList, title, messageBF.toString(), "V3MES", "V3MES@visionox.com", "V3MES", "vis@2019");
			}
			catch (Exception e)
			{
				log.info(" Failed to send mail.");
			}
		}
	}

	public List<String> getEmailList(String machineName, String alarmGroupName) 
	{
		List<Map<String,Object>> resultList = null;
		List<String> emailList = new ArrayList<String>();
		
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
		    log.info(ex.getCause());
		}
		
		try 
		{
			if (resultList.size() > 0) 
			{
				String departmentAll = resultList.get(0).get("RMSDEPARTMENT").toString();

				List<String> department =  CommonUtil.splitStringDistinct(",",departmentAll);

				StringBuffer sql1 = new StringBuffer();
				sql1.append(
						"SELECT B.* FROM CT_ALARMUSERGROUP A, CT_ALARMUSER B, USERPROFILE C WHERE A.USERID = B.USERID AND B.USERID = C.USERID  AND A.ALARMGROUPNAME = :ALARMGROUPNAME AND C.DEPARTMENT=:DEPARTMENT");

				for (String department1 : department) 
				{
					Map<String, Object> args1 = new HashMap<String, Object>();
					args1.put("ALARMGROUPNAME", alarmGroupName);
					args1.put("DEPARTMENT", department1);
					List<Map<String, Object>> sqlResult1 = GenericServiceProxy.getSqlMesTemplate()
							.queryForList(sql1.toString(), args1);
					
					if (sqlResult1.size() > 0) 
					{
						for (Map<String, Object> user : sqlResult1)
						{
							String eMail = ConvertUtil.getMapValueByName(user, "EMAIL");
							emailList.add(eMail);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			log.info("Not Found the Department of "+ machineName);
			log.info(" Failed to send mail. MachineName: " + machineName);
		}
		
		return emailList;
	}
}


