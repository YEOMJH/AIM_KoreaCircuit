package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.consumable.service.ConsumableServiceUtil;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DSPProductRequestPlan;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.MakeLoggedInInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.data.PortSpec;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductC;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class TrackInLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "RECIPENAME", true);
		String tpOffsetFlag = SMessageUtil.getBodyItemValue(doc, "TPOFFSETFLAG", true);
		String changeRecipeFlag = SMessageUtil.getBodyItemValue(doc, "CHANGERECIPEFLAG", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		CommonValidation.checkJobDownFlag(lotData);
		CommonValidation.checkFirstGlassLot(lotData, machineName);
		CommonValidation.checkOriginalProduct(lotData);
		CommonValidation.checkDummyProductReserveOper(lotData);

		// 2021-02-04	dhko	Flow/Oper of NodeStack compare to Flow/Oper of Lot
		CommonValidation.checkNodeStackCompareToOperation(lotData);
		
		List<Product> producList = LotServiceProxy.getLotService().allProducts(lotName);
		// check ProductGrade
		List<String> productNameList = new ArrayList<>();
		for (Product productA : producList)
		{
			String productName = productA.getKey().getProductName();
			productNameList.add(productName);
		}

		CommonValidation.checkProductGradebyString(productNameList);

		MachineSpec machineSpecData = CommonUtil.getMachineSpecByMachineName(machineName);
		Machine machineData = CommonUtil.getMachineInfo(machineName);
		
		// BaseLine check by Runban Rule 
		if ("Y".equals(machineSpecData.getUdfs().get("BASELINEFLAG")))
			CommonValidation.checkBaseLine(lotData, machineData,this.getEventUser());
		
		// Check Runban prevent rule
		this.checkRunbanPreventRule(lotData, machineName);

		if (lotData.getFactoryName().equals("TP"))
		{
			Map<String, String> Udfs = machineSpecData.getUdfs();
			String check = Udfs.get("TPSLOTCHECK");

			if (check.equals("Y"))
			{
				List<Map<String, Object>> sqlResult = checkSlotPosition(lotData);

				if (sqlResult.size() > 0)
					throw new CustomException("LOT-0046");
			}
		}

		CommonValidation.checkLotIssueState(lotData);
		CommonValidation.checkMQCPlanState(lotName, "TrackIn");
		
		// Mantis-0000100
		CommonValidation.checkMQCDummyUsedCount(lotData);

		if (lotData.getFactoryName().equals("OLED"))
			checkProductType(lotData);

		// Validation LotGrade for MainFlow
		MESLotServiceProxy.getLotServiceUtil().validationLotGrade(lotData);

		// Validation for product quantity.
		if (lotData.getProductQuantity() <= 0)
			throw new CustomException("LOT-0149");

		// Validation MachineState
		Machine eqpData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		CommonValidation.checkMachineHold(eqpData);
		// jinlj2020/8/6 offline run card
		/*if (CommonUtil.equalsIn(eqpData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_ArrayPhoto))
			MESLotServiceProxy.getLotServiceUtil().checkPhotoMask(lotData, machineName);*/

		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		PortSpec portSpecData = MESPortServiceProxy.getPortServiceUtil().getPortSpecInfo(machineName, portName);


		String machineRecipeName =  "";
		String productOffset = "";
		
		if(changeRecipeFlag.equals("True")||tpOffsetFlag.equals("True"))
		{
			machineRecipeName = recipeName;
			List<Product> productList = MESProductServiceProxy.getProductServiceUtil().allUnScrappedProductsByLot(lotName);
			productOffset = productList.get(0).getUdfs().get("OFFSET").toString();
		}
		else 
		{
			machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(),
					lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineName, false);
		}
		
		if (MESRecipeServiceProxy.getRecipeServiceUtil().RMSFlagCheck("E", machineName, machineRecipeName, "", lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), productOffset))
			//MESRecipeServiceProxy.getRecipeServiceUtil().checkRecipeOnTrackInTime(machineName, recipeName);
			checkRecipeOnTrackInTime(machineName, recipeName);
		
		if (!recipeName.equals(machineRecipeName))
			throw new CustomException("MACHINE-0103", recipeName, machineName, "");

		if (!StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Unpacker))
		{
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());
			// CST Dirty, Hold
			CommonValidation.CheckDurableHoldState(durableData);
			CommonValidation.CheckDurableCleanState(durableData);
		}

		ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);
		ExtendedObjectProxy.getProductQTimeService().monitorProductQTime(eventInfo, lotName, machineName);
		ExtendedObjectProxy.getProductQTimeService().validateQTimeByLot(lotName);

		if (StringUtil.equals(processFlowData.getProcessFlowType(), "BackUp"))
			MESLotServiceProxy.getLotServiceUtil().isBackUpTrackIn(lotData, eventInfo);
		else
			ExtendedObjectProxy.getProductQTimeService().exitQTimeByLot(eventInfo, lotName, lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessOperationName());

		// Check Rework Count Limit
		CommonValidation.checkReworkLimit(lotData);
		
		if ((processFlowData.getProcessFlowType().equals("Inspection") || processFlowData.getProcessFlowType().equals("Sample")) && !processFlowData.getProcessFlowType().equals("MQC"))
		{
			ExtendedObjectProxy.getSampleProductService().setSampleProductTrackInEvent(eventInfo,lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
					lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());
		}

		// TrackIn
		List<ProductC> productCSequence = MESLotServiceProxy.getLotInfoUtil().setProductCSequence(lotName);

		Map<String, String> lotUdfs = new HashMap<String, String>();
		lotUdfs.put("PORTNAME", portData.getKey().getPortName());
		lotUdfs.put("PORTTYPE", portSpecData.getPortType().toString());
		lotUdfs.put("PORTUSETYPE", portData.getUdfs().get("PORTUSETYPE"));

		MakeLoggedInInfo makeLoggedInInfo = MESLotServiceProxy.getLotInfoUtil().makeLoggedInInfo(machineName, recipeName, productCSequence, lotUdfs);

		eventInfo.setEventName("TrackIn");
		MESLotServiceProxy.getLotServiceImpl().trackInLot(eventInfo, lotData, makeLoggedInInfo);

		//Check ERPBOM 2020-10-15
		ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());
		
		if(StringUtil.isNotEmpty(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
		{
			MESConsumableServiceProxy.getConsumableServiceUtil().compareERPBOM(lotData.getFactoryName(), productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME"), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineName, lotData.getProductSpecName());
		}
		
		// [V3_MES_121_004]DSP Run Control_V1.02
		MESLotServiceProxy.getLotServiceUtil().increaseRunControlUseCount(eventInfo, machineName, lotData.getKey().getLotName(), true, productNameList.size());
		
		try
		{
			MESLotServiceProxy.getLotServiceImpl().deleteCtLotFutureAction(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProcessFlowName(),
					lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), "1");
		}
		catch (CustomException e)
		{
			eventLog.info("Can't delete CtLotFutureAction:Abort Info not exist");
		}

		updateReservedLotState(lotName, machineName, lotData, eventInfo);
		
		if (StringUtil.isNotEmpty(lotData.getProductionType().toString())
				&& (StringUtil.equals(lotData.getProductionType(), "P")
						|| StringUtil.equals(lotData.getProductionType(), "E"))) 
		{
			try 
			{
				MESLotServiceProxy.getLotServiceImpl().sendEmailToMFG(eventInfo, lotData, lotName);
			} 
			catch (Exception e) 
			{
				eventLog.info(" Failed to send mail. ");
			}
		}

		return doc;
	}

	private void checkRunbanPreventRule(Lot lotData,String machineName) throws CustomException
	{
		if (lotData == null)
		{
			eventLog.info("The incoming variable value is null or empty!!");
			return;
		}

		ConstantMap constMap = GenericServiceProxy.getConstantMap();

		List<Product> productDataList = new ArrayList<>();
		ProcessFlow curFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);
		ProcessFlow mainFlowData = MESLotServiceProxy.getLotInfoUtil().getMainProcessFlowData(lotData);

		//MQC Runban Product
		if (StringUtil.in(curFlowData.getProcessFlowType(), "MQC", "MQCRecycle"))
		{
			if (!StringUtil.in(lotData.getProductionType(), constMap.Pord_MQC, constMap.Pord_Dummy))
				return;

			List<Map<String, Object>> mqcProductList = new ArrayList<Map<String, Object>>();

			if (StringUtils.equals(curFlowData.getProcessFlowType(), "MQC"))
			{
				mqcProductList = ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().getMQCProduct(lotData.getKey().getLotName());
			}
			else if (StringUtils.equals(curFlowData.getProcessFlowType(), "MQCRecycle"))
			{
				mqcProductList = ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().getMQCRecycleProduct(lotData.getKey().getLotName());
			}

			if (mqcProductList.size() == 0)
			{
				throw new CustomException("LOT-0301", lotData.getKey().getLotName(), lotData.getProcessOperationName());
			}

			List<String> productNameList = CommonUtil.makeListBySqlResult(mqcProductList, "PRODUCTNAME");
			productDataList = MESProductServiceProxy.getProductServiceUtil().getRunbanProductList(productNameList);
		}
		else
		{
			if (!StringUtil.in(lotData.getProductionType(), constMap.Pord_Production, constMap.Pord_Engineering, constMap.Pord_Test))
				return;

			//Rework Runban Product
			if (curFlowData.getProcessFlowType().equals("Rework"))
			{
				List<Product> tempProductList = new ArrayList<>();
				tempProductList = MESProductServiceProxy.getProductServiceUtil().getRunbanProductList(lotData.getKey().getLotName());
				
				for(Product pData : tempProductList)
				{
					if ("Y".equals(pData.getUdfs().get("REWORKFLAG")))
					{
						productDataList.add(pData);
					}
				}
			}
			else
			{
				//Normal Runban Product
				productDataList = MESProductServiceProxy.getProductServiceUtil().getRunbanProductList(lotData.getKey().getLotName());
			}
		}

		if (productDataList == null || productDataList.size() == 0) return;
		CommonValidation.checkRunbanPreventRule(productDataList, machineName, mainFlowData.getUdfs().get("RUNBANPROCESSFLOWTYPE"));
	}
	
	private void checkProductType(Lot lotData) throws CustomException
	{
		ProcessOperationSpec pos = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());

		if (!pos.getDetailProcessOperationType().equals("CUT"))
		{
			if (!lotData.getProductType().equals("Glass"))
			{
				throw new CustomException("LOT-0038");
			}
		}
	}

	private List<Map<String, Object>> checkSlotPosition(Lot lotData)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT COUNT (P.PRODUCTNAME), P.POSITION ");
		sql.append("  FROM PRODUCT P, DURABLE D ");
		sql.append(" WHERE P.LOTNAME = :LOTNAME ");
		sql.append("   AND P.CARRIERNAME = D.DURABLENAME ");
		sql.append("   AND D.DURABLETYPE = 'TPGlassCST' ");
		sql.append("GROUP BY P.POSITION ");
		sql.append("HAVING COUNT (P.PRODUCTNAME) = 1 ");

		Map<String, String> inquirybindMap = new HashMap<String, String>();

		inquirybindMap.put("LOTNAME", lotData.getKey().getLotName());

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), inquirybindMap);

		return sqlResult;
	}

	private void updateReservedLotState(String lotName, String machineName, Lot lotData, EventInfo eventInfo) throws CustomException
	{
		try
		{
			String condition = "machineName = ? and lotName =? and productSpecName =? and processOperationName =? and productRequestName =? and reserveState = ? ";
			Object bindSet[] = new Object[] { machineName, lotName, lotData.getProductSpecName(), lotData.getProcessOperationName(), lotData.getProductRequestName(), "Reserved" };
			List<ReserveLot> reserveLot = ExtendedObjectProxy.getReserveLotService().select(condition, bindSet);

			reserveLot.get(0).setReserveState(GenericServiceProxy.getConstantMap().RESV_STATE_START);
			reserveLot.get(0).setInputTimeKey(eventInfo.getEventTimeKey());

			ExtendedObjectProxy.getReserveLotService().modify(eventInfo, reserveLot.get(0));

			condition = "productRequestName = ? and productSpecName = ? and processFlowName = ? and processOperationName = ? and  machineName = ? and planDate = ? ";
			bindSet = new Object[] { lotData.getProductRequestName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName,
					reserveLot.get(0).getPlanDate() };
			List<DSPProductRequestPlan> productRequestPlan = ExtendedObjectProxy.getDSPProductRequestPlanService().select(condition, bindSet);

			productRequestPlan.get(0).setPlanState("Processing");
			ExtendedObjectProxy.getDSPProductRequestPlanService().modify(eventInfo, productRequestPlan.get(0));
		}
		catch (Exception e)
		{
			eventLog.info("Fail ReservedLot Updating");
		}
	}
	public void checkRecipeOnTrackInTime(String machineName, String recipeName)
			throws CustomException
 {
		// existence verification in MES
		Recipe recipeData;
		try {
			recipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false,
					new Object[] { machineName, recipeName });
		} catch (FrameworkErrorSignal fe) {
			throw new CustomException("SYS-9999", "RMS", fe.getMessage());
		} catch (greenFrameErrorSignal nfe) {
			throw new CustomException("SYS-9999", "RMS", nfe.getMessage());
		}

		// approval validation
		if (!recipeData.getMFGFlag().equals("Y") || !recipeData.getINTFlag().equals("Y")
				|| !recipeData.getENGFlag().equals("Y") || !recipeData.getActiveState().equals("Active")) 
		{
			// RMS-006 : Recipe[{0}] is not permitted
			throw new CustomException("RMS-006", recipeName);
		}

		// Increase TotalTimeUsed
		String comment = "Increase Recipe check TotalTimeUsed + 1";
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeRecipeInfo", "MES", comment, null, null);
		recipeData.setTotalTimeUsed(recipeData.getTotalTimeUsed() + 1);

		boolean isUpdate = false;
		boolean checkRMSFlag = false;

		// String comment = StringUtil.EMPTY;

		// Check time used
		if ((recipeData.getTimeUsedLimit() == recipeData.getTimeUsed() || recipeData.getTimeUsedLimit() < recipeData.getTimeUsed())
				&& (recipeData.getAutoChangeFlag().equals("ENGINT") || recipeData.getAutoChangeFlag().equals("INTENG"))) {
			throw new CustomException("MACHINE-0022", recipeName);
		} else {

			if (recipeData.getTimeUsedLimit() == recipeData.getTimeUsed() + 1) {
				comment = "Recipe check timeUsed is same with timeusedlimit";
				isUpdate = true;
				checkRMSFlag = true;
				recipeData.setTimeUsed(0);
			} else {
				comment = "Increase Recipe check timeUsed + 1";
				isUpdate = true;
				recipeData.setTimeUsed(recipeData.getTimeUsed() + 1);
			}
		}

		// Check duration used
		if (recipeData.getLastTrackOutTimeKey() == null || recipeData.getLastTrackOutTimeKey().equals("")) {
			String specValue = Double.toString(recipeData.getDurationUsedLimit());
			String interval = Double.toString(ConvertUtil.getDiffTime(
					TimeUtils.toTimeString(recipeData.getLastApporveTime(), TimeStampUtil.FORMAT_TIMEKEY),
					TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY)));

			if (Double.parseDouble(specValue) * 60 * 60 < Double.parseDouble(interval)
					&& (recipeData.getAutoChangeFlag().equals("ENGINT")
							|| recipeData.getAutoChangeFlag().equals("INTENG"))) {
				throw new CustomException("MACHINE-0023", recipeName);
			}
		} else {
			Timestamp value = TimeUtils.getTimestampByTimeKey(recipeData.getLastTrackOutTimeKey());
			String specValue = Double.toString(recipeData.getDurationUsedLimit());
			String interval = Double
					.toString(ConvertUtil.getDiffTime(TimeUtils.toTimeString(value, TimeStampUtil.FORMAT_TIMEKEY),
							TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY)));

			if (Double.parseDouble(specValue) * 60 * 60 < Double.parseDouble(interval)
					&& (recipeData.getAutoChangeFlag().equals("ENGINT")
							|| recipeData.getAutoChangeFlag().equals("INTENG")))// modify DurationUsedLimit by Hours. Double.parseDouble(specValue)*24*60*60 to Double.parseDouble(specValue)*60*60
			{
				throw new CustomException("MACHINE-0023", recipeName);
			}
		}

		// Check Maxduration used
		if (recipeData.getMaxDurationUsedLimit() != null && !recipeData.getMaxDurationUsedLimit().equals(""))
		{
			String value = TimeUtils.toTimeString(recipeData.getMaxDurationUsedLimit(), TimeStampUtil.FORMAT_TIMEKEY);
			String specValue = Double.toString(recipeData.getDurationUsedLimit());
			String interval = Double
					.toString(ConvertUtil.getDiffTime(value, TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY)));

			if (0 < Double.parseDouble(interval) && 
					(recipeData.getAutoChangeFlag().equals("ENGINT") || recipeData.getAutoChangeFlag().equals("INTENG")))
			{
				throw new CustomException("MACHINE-0045", recipeName);
			}
		}

		if (isUpdate) {
			eventInfo = EventInfoUtil.makeEventInfo("ChangeRecipeInfo", "MES", comment, null, null);

			if (checkRMSFlag) {
				if (recipeData.getAutoChangeFlag().equals("ENGINT")
						|| recipeData.getAutoChangeFlag().equals("INTENG")) 
				{
					recipeData.setINTFlag("Y");
					recipeData.setENGFlag("N");
					recipeData.setRMSFlag("N");
				}
			}
			ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipeData);
		}
	}
}
