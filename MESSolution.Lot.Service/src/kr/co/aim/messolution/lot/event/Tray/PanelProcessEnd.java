package kr.co.aim.messolution.lot.event.Tray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaterialProduct;
import kr.co.aim.messolution.extended.object.management.data.ReworkProduct;
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.policy.util.LotPFIValueSetter;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processflow.management.iter.NodeStackUtil;
import kr.co.aim.greentrack.processflow.management.iter.PFIValueSetter;
import kr.co.aim.greentrack.processflow.management.iter.ProcessFlowIterator;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class PanelProcessEnd extends AsyncHandler {
	private static Log log = LogFactory.getLog(PanelProcessEnd.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String panelName = SMessageUtil.getBodyItemValue(doc, "PANELNAME", true);
		String realPanelName = SMessageUtil.getBodyItemValue(doc, "REALPANELNAME", false);
		String trayName = SMessageUtil.getBodyItemValue(doc, "TRAYNAME", false);
		String position = SMessageUtil.getBodyItemValue(doc, "POSITION", false);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
		String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
		String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);
		String lotGrade = SMessageUtil.getBodyItemValue(doc, "PANELJUDGE", true);
		String lotdetailGrade = SMessageUtil.getBodyItemValue(doc, "PANELGRADE", false);
		String firstRunFlag = SMessageUtil.getBodyItemValue(doc, "FIRSTRUNFLAG", false);
		String pickCode = SMessageUtil.getBodyItemValue(doc, "PICKCODE", false);
		String pickResult = SMessageUtil.getBodyItemValue(doc, "PICKRESULT", false);
		List<Element> materialElementList = SMessageUtil.getBodySequenceItemList(doc, "MATERIALLIST", false);

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		Element bodyElement = doc.getRootElement().getChild(SMessageUtil.Body_Tag);

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		
		/*if(!StringUtil.equals(machineData.getMachineGroupName(), constantMap.MachineGroup_Sorter))
		{
			Durable trayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
			CommonValidation.CheckDurableState(trayData);
			CommonValidation.CheckDurableHoldState(trayData);
			
			if (trayData.getDurableType().equals("CoverTray"))
				throw new CustomException("DURABLE-9008", trayName);
		}*/
		
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotDataForUpdate(panelName);

		ProcessOperationSpec processOpSpec = MESLotServiceProxy.getLotServiceUtil().getProcessOperationSpecData(lotData);
		String detailOperationType = processOpSpec.getDetailProcessOperationType();

		CommonValidation.checkMachineHold(machineData);
		if (!lotData.getCarrierName().isEmpty())
		{
			// TRAY-0022: Panel [{0}] already assigned to another tray [{1}].
			throw new CustomException("TRAY-0022", panelName, lotData.getCarrierName());
		}

		// set rework info
		this.setReworkInfo(lotData, detailOperationType, lotGrade);

		// trackout panel
		this.trackOutPanel(lotData, bodyElement,detailOperationType,machineData,firstRunFlag);

		// Scrap panel// Cell Test S not Scrap and LotGrade is N Not Scrap
		List<Map<String, Object>> notScarppedDate = getNotScappedDetailOperationType(detailOperationType);
		if(notScarppedDate.size()==0){
			if (StringUtil.in(lotData.getLotGrade(),"S"))
			{
				EventInfo scrapEventInfo = EventInfoUtil.makeEventInfo("Scrap", getEventUser(), getEventComment());//2020/12/08 ScrapLotModifyScrap
				Lot scrapLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(panelName);
				Lot oldscrapLotData = (Lot) ObjectUtil.copyTo(lotData);

				scrapLotData.setLotState(constantMap.Lot_Scrapped);
				scrapLotData.setLotGrade("S");
				//if(detailOperationType.equals("AGING")&&StringUtil.equals(machineData.getMachineGroupName(), "SCA"))//2021/1/21 caixu AddSCAScrapCode
				if(detailOperationType.equals("AGING")&&(StringUtil.equals(machineData.getMachineGroupName(), "SCA")||StringUtil.equals(machineData.getMachineGroupName(), "Aging")))
				{
				 scrapLotData.setReasonCode("DFE00");
					
				}else if((detailOperationType.equals("Lami")||detailOperationType.equals("Lami2"))&&StringUtil.equals(machineData.getMachineGroupName(), "PFL")){
					
					scrapLotData.setReasonCode("CCS25");//PFL ScrapCode
					
				}
				else
				{
					scrapLotData.setReasonCode("Auto Scrap");	
				}
				if(lotdetailGrade.equals("S1")){
					
					scrapLotData.getUdfs().put("LOTDETAILGRADE", lotdetailGrade);
				}else{
					
					scrapLotData.getUdfs().put("LOTDETAILGRADE", "");
				}
				scrapLotData.setLastEventName(scrapEventInfo.getEventName());
				scrapLotData.setLastEventTimeKey(scrapEventInfo.getEventTimeKey());
				scrapLotData.setLastEventTime(scrapEventInfo.getEventTime());
				scrapLotData.setLastEventUser(scrapEventInfo.getEventUser());
				scrapLotData.setLastEventComment(scrapEventInfo.getEventComment());
				scrapLotData.setLastEventFlag(constantMap.Flag_N);

				LotHistory scrapLotHist = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldscrapLotData, scrapLotData, new LotHistory());

				LotServiceProxy.getLotService().update(scrapLotData);
				LotServiceProxy.getLotHistoryService().insert(scrapLotHist);

				// ChangeWorkOrder Scrap
				String workOrderName = lotData.getProductRequestName();
				ProductRequest newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(scrapEventInfo, workOrderName, 1, 0);

				/*if (newProductRequestData.getPlanQuantity() == newProductRequestData.getFinishedQuantity() + newProductRequestData.getScrappedQuantity())
				{
					scrapEventInfo.setEventName("Complete");
					MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompleted(scrapEventInfo, workOrderName);
				}*/
			}
			
		}
		

		// insert sorter pick&print information by pickCode and pickresult(from SVI (35051, 35052))
		/*
		if (!StringUtil.in(StringUtil.EMPTY, pickResult, pickCode) && StringUtil.in(pickResult, "OK", "YN"))
		{
			SorterPickPrintInfo dataInfo = null;

			try
			{
				dataInfo = ExtendedObjectProxy.getSorterPickPrintInfoService().selectByKey(false, new Object[] { panelName });
			}
			catch (Exception ex)
			{
				if (ex instanceof greenFrameDBErrorSignal && ((greenFrameDBErrorSignal) ex).getErrorCode().equals(ErrorSignal.NotFoundSignal))
					log.info("Not found SorterPickPrintInfo by PanelName =  " + panelName);
				else
					throw new CustomException(ex.getCause());
			}

			EventInfo pickEventInfo = EventInfoUtil.makeEventInfo(this.getMessageName(), getEventUser(), getEventComment());

			if (dataInfo == null)
			{
				SorterPickPrintInfo sorterPickData = new SorterPickPrintInfo();
				sorterPickData.setLotName(panelName);
				sorterPickData.setPickPrintMode("OK".equals(pickResult)?"YN":pickResult);
				sorterPickData.setCode(pickCode);

				ExtendedObjectProxy.getSorterPickPrintInfoService().create(pickEventInfo, sorterPickData);
			}
		}*/

		// record material information
		for (Element materialElement : materialElementList)
		{
			EventInfo materialEventInfo = EventInfoUtil.makeEventInfo("Consume", this.getEventUser(), this.getEventComment());

			String materialType = SMessageUtil.getChildText(materialElement, "MATERIALTYPE", false);
			String materialKind = "";
			String materialLocationName = "";

			if (materialType.equals(constantMap.MaterialType_PatternFilm))
			{
				String materialName = SMessageUtil.getChildText(materialElement, "MATERIALNAME", true);
				String materialQty = SMessageUtil.getChildText(materialElement, "MATERIALQTY", true);

				Consumable consumable = MESConsumableServiceProxy.getConsumableInfoUtil().getMaterialData(materialName);

				materialEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				DecrementQuantityInfo transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().decrementQuantityInfo(null, null, null, null, materialEventInfo.getEventTimeKey(),
						Double.parseDouble(materialQty), new HashMap<String, String>());

				try
				{
					Consumable afterData = MESConsumableServiceProxy.getConsumableServiceImpl().decrementQuantity(consumable, transitionInfo, materialEventInfo);

					if (afterData.getQuantity() == 0)
					{
						materialEventInfo.setEventName("ChangeState");
						materialEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
						MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();

						MESConsumableServiceProxy.getConsumableServiceImpl().makeNotAvailable(afterData, makeNotAvailableInfo, materialEventInfo);
					}
				}
				catch (Exception e)
				{

				}

				materialKind = "Consumable";
				materialLocationName = consumable.getMaterialLocationName();

				// Modify 2020-07-20 Only MaterialType_PatternFilm Set Condition Request by caxiu
				MaterialProduct materialData = new MaterialProduct();
				materialData.setMachineName(machineName);
				materialData.setMaterialLocationName(materialLocationName);
				materialData.setLotName(panelName);
				materialData.setProductName(panelName);
				materialData.setMaterialName(materialElement.getChildText("MATERIALNAME"));
				materialData.setMaterialKind(materialKind);
				materialData.setMaterialType(materialElement.getChildText("MATERIALTYPE"));
				materialData.setQuantity(StringUtil.isNumeric(materialQty) ? Integer.parseInt(materialQty) : 0);
				materialData.setTimeKey(materialEventInfo.getEventTimeKey());
				materialData.setEventName(materialEventInfo.getEventName());
				materialData.setEventTime(materialEventInfo.getEventTime());
				materialData.setFactoryName(lotData.getFactoryName());
				materialData.setProductSpecName(bodyElement.getChildText("PRODUCTSPECNAME"));
				materialData.setProductSpecVersion(bodyElement.getChildText("PRODUCTSPECVERSION"));
				materialData.setProcessFlowName(bodyElement.getChildText("PROCESSFLOWNAME"));
				materialData.setProcessFlowVersion(bodyElement.getChildText("PROCESSFLOWVERSION"));
				materialData.setProcessOperationName(bodyElement.getChildText("PROCESSOPERATIONNAME"));
				materialData.setProcessOperationVersion(bodyElement.getChildText("PROCESSOPERATIONVERSION"));

				ExtendedObjectProxy.getMaterialProductService().create(materialEventInfo, materialData);
			}
		}
		//Add by yueke 20210324
		Lot newLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(panelName);
		if(!StringUtils.equals(newLotData.getUdfs().get("BEFOREOPERATIONNAME"),"3S004" ))
		{
			ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(newLotData.getProductRequestName());
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOut", this.getEventUser(), this.getEventComment());
			String sapFlag=CommonUtil.getEnumDefValueStringByEnumName("SAPFLAG");			
			if(StringUtil.isNotEmpty(sapFlag)&&StringUtil.equals(sapFlag, "Y")&&
					StringUtils.isNotEmpty(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
			{
				List<Map<String, Object>> panelList=new ArrayList<>();
				SuperProductRequest superWO = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
				
				MESConsumableServiceProxy.getConsumableServiceUtil().trackOutERPBOMReportForTrayGroup(eventInfo, newLotData, superWO, machineName, 1, panelList);
			}
		}
	}

	private void setReworkInfo(Lot lotData, String detailOperationType, String lotGrade) throws CustomException
	{
		List<ReworkProduct> createList = new ArrayList<ReworkProduct>();
		List<ReworkProduct> updateList = new ArrayList<ReworkProduct>();

		if ((lotGrade.equals("D") && detailOperationType.equals("CT")))
		{
			String searchDetailOperationType = detailOperationType + "_AGING";
			List<Map<String, Object>> reworkCountData = getReworkCountData(lotData.getKey().getLotName(), searchDetailOperationType);

			if (reworkCountData.size() > 0)
			{
				log.info("CellTest:LotGrade is D More than 1 times");
				ReworkProduct reworkData = setReworkCountData(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
						lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), searchDetailOperationType, Long.parseLong(reworkCountData.get(0).get("REWORKCOUNT").toString()) + 1);
				updateList.add(reworkData);
			}
			else
			{
				ReworkProduct reworkData = setReworkCountData(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
						lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), searchDetailOperationType, 1);
				createList.add(reworkData);
			}
		}

		if ((lotGrade.equals("R") && !detailOperationType.equals("CT")) || detailOperationType.equals("RP"))
		{
			String searchDetailOperationType = detailOperationType;
			List<Map<String, Object>> reworkCountData = getReworkCountData(lotData.getKey().getLotName(), searchDetailOperationType);

			if (reworkCountData.size() > 0)
			{
				log.info("Repair:LotGrade is R or P More than 1 times");
				ReworkProduct reworkData = setReworkCountData(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
						lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), searchDetailOperationType, Long.parseLong(reworkCountData.get(0).get("REWORKCOUNT").toString()) + 1);
				updateList.add(reworkData);
			}
			else
			{
				ReworkProduct reworkData = setReworkCountData(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
						lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), searchDetailOperationType, 1);
				createList.add(reworkData);
			}
		}

		if (createList.size() > 0)
		{
			ExtendedObjectProxy.getReworkProductService().insert(createList);
		}
		if (updateList.size() > 0)
		{
			ExtendedObjectProxy.getReworkProductService().update(updateList);
		}
	}

	private void trackOutPanel(Lot lotData, Element bodyElement,String detailOperationType,Machine machineData,String firstRunFlag) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOut", this.getEventUser(), this.getEventComment());

		CommonValidation.checkLotProcessStateRun(lotData);
		CommonValidation.checkLotState(lotData);
		CommonValidation.checkLotHoldState(lotData);

		Lot oldLot = (Lot) ObjectUtil.copyTo(lotData);
        if(!firstRunFlag.isEmpty())
        {
        	 lotData.getUdfs().put("FIRSTGLASSFLAG", firstRunFlag);
        }
        else
        {
        	lotData.getUdfs().put("FIRSTGLASSFLAG", "");
        }
        
		lotData.setMachineName(bodyElement.getChildText("MACHINENAME"));
		lotData.setMachineRecipeName(bodyElement.getChildText("MACHINERECIPENAME"));
		lotData.setLotState(GenericServiceProxy.getConstantMap().Lot_Released);
		lotData.setLotProcessState(GenericServiceProxy.getConstantMap().Lot_Wait);
		// 2020-09-27 PRODUCTJUDGE -> PANELJUDGE messageSpec not exist 'productJudge' -dkh
//		lotData.setLotGrade(bodyElement.getChildText("PRODUCTJUDGE"));
		lotData.setLotGrade(bodyElement.getChildText("PANELJUDGE"));
		lotData.setLastLoggedOutTime(eventInfo.getEventTime());
		lotData.setLastLoggedOutUser(eventInfo.getEventUser());
		lotData.setLastEventName(eventInfo.getEventName());
		lotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		lotData.setLastEventTime(eventInfo.getEventTime());
		lotData.setLastEventUser(eventInfo.getEventUser());
		lotData.setLastEventComment(eventInfo.getEventComment());
		lotData.setLastEventFlag(GenericServiceProxy.getConstantMap().Flag_N);
		if(detailOperationType.equals("AGING")&&StringUtil.equals(machineData.getMachineGroupName(), "SCA")&&StringUtil.in(lotData.getLotGrade(),"S"))//2021/1/21 caixu AddSCAScrapCode
		{
			lotData.setReasonCode("DFE00");;
			
		}//ADD PFL Flag
		else if(detailOperationType.equals("AGING")&&StringUtil.equals(machineData.getMachineGroupName(), "Aging")&&StringUtil.in(lotData.getLotGrade(),"S"))//2021/1/21 caixu AddSCAScrapCode
		{
			lotData.setReasonCode("DFE00");;
			
		}//ADD PFL Flag
		if((detailOperationType.equals("Lami")||detailOperationType.equals("Lami2"))&&StringUtil.equals(machineData.getMachineGroupName(), "PFL")){
			
			lotData.getUdfs().put("JOBDOWNFLAG", "PFL");
		}
        if(detailOperationType.equals("SVI/MVI")&&StringUtil.equals(machineData.getMachineGroupName(), "SVI")){
			
    	   lotData.getUdfs().put("LOTDETAILGRADE", bodyElement.getChildText("PANELGRADE"));
		}

        if(detailOperationType.equals("CT") && StringUtil.equals(machineData.getMachineGroupName(), "AVI") && bodyElement.getChildText("PANELJUDGE").equals("G"))
        {
        	String sql = "SELECT SEQ FROM ENUMDEFVALUE "
					+ "WHERE ENUMNAME = :ENUMNAME AND DESCRIPTION = :PRODUCTSPECNAME AND DEFAULTFLAG = 'Y' "
					+ "AND DISPLAYCOLOR = :PROCESSFLOWNAME AND SEQ = :PROCESSOPERATIONNAME";
		
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("ENUMNAME", "AVISetLotDetailGrade");
			bindMap.put("PRODUCTSPECNAME", oldLot.getProductSpecName());
			bindMap.put("PROCESSFLOWNAME", oldLot.getProcessFlowName());
			bindMap.put("PROCESSOPERATIONNAME", oldLot.getProcessOperationName());
			
			List<Map<String, Object>> sqlResult = 
					GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			
			if(sqlResult!=null && sqlResult.size()>0)
			{
				lotData.getUdfs().put("LOTDETAILGRADE", "A");
			}
        }
        
        //临时需求,AVI P品卡控 MVI不能高于A1等级,所有规格产品
        if(StringUtil.equals(machineData.getMachineGroupName(), "AVI") && bodyElement.getChildText("PANELJUDGE").equals("P")
        		&& !detailOperationType.equals("CT"))
        {
        	lotData.getUdfs().put("ELANFC", "Y");
        }
        
		lotData.getUdfs().put("PORTNAME", bodyElement.getChildText("PORTNAME"));
		lotData.getUdfs().put("PORTTYPE", bodyElement.getChildText("PORTTYPE"));
		lotData.getUdfs().put("PORTUSETYPE", bodyElement.getChildText("PORTUSETYPE"));

		lotData.getUdfs().put("BEFOREOPERATIONNAME", oldLot.getProcessOperationName());
		lotData.getUdfs().put("BEFOREOPERATIONVER", oldLot.getProcessOperationVersion());
		lotData.getUdfs().put("BEFOREFLOWNAME", oldLot.getProcessFlowName());
		//lotData.getUdfs().put("LOTDETAILGRADE", bodyElement.getChildText("PRODUCTGRADE"));

		ProcessFlowKey processFlowKey = new ProcessFlowKey();
		processFlowKey.setFactoryName(lotData.getFactoryName());
		processFlowKey.setProcessFlowName(lotData.getProcessFlowName());
		processFlowKey.setProcessFlowVersion(lotData.getProcessFlowVersion());

		ProcessFlow processFlow = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);

		kr.co.aim.greentrack.processflow.management.iter.NodeStack nodeStack = NodeStackUtil.stringToNodeStack(lotData.getNodeStack());
		ProcessFlowIterator pfi = new ProcessFlowIterator(processFlow, nodeStack, "");
		
		PFIValueSetter valueSetter = new LotPFIValueSetter(pfi, oldLot, lotData );
		pfi.moveNext("N", valueSetter);

		Node nextNode = pfi.getCurrentNodeData();

		lotData.setNodeStack(nextNode.getKey().getNodeId());
		lotData.setProcessFlowName(nextNode.getProcessFlowName());
		lotData.setProcessFlowVersion(nextNode.getProcessFlowVersion());
		lotData.setProcessOperationName(nextNode.getNodeAttribute1());
		lotData.setProcessOperationVersion(nextNode.getNodeAttribute2());

		LotHistory lotHist = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lotData, new LotHistory());

		// record panel process count (DetailProcessType in 'ACA, SCA, CRP')
		String processOperType = MESLotServiceProxy.getLotServiceUtil().getProcessOperationSpecData(oldLot).getDetailProcessOperationType();
		Map<String, String> processLimitEnum = ExtendedObjectProxy.getPanelProcessCountService().getProcessLimitConfiguration("ProcessLimit");

		if (processLimitEnum != null && StringUtil.in(processOperType, processLimitEnum.keySet().toArray(new String[] {})))
			ExtendedObjectProxy.getPanelProcessCountService().setPanelProcessCount(eventInfo, oldLot, processOperType, processLimitEnum.get(processOperType));

		LotServiceProxy.getLotService().update(lotData);
		LotServiceProxy.getLotHistoryService().insert(lotHist);
	}

	private List<Map<String, Object>> getReworkCountData(String lotName, String detailOperType) throws CustomException
	{
		String sql = "SELECT * FROM CT_REWORKPRODUCT WHERE PRODUCTNAME = :PRODUCTNAME AND REWORKTYPE = :REWORKTYPE ";

		Map<String, String> args = new HashMap<String, String>();
		args.put("PRODUCTNAME", lotName);
		args.put("REWORKTYPE", detailOperType);

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

		return result;
	}
	
	private List<Map<String, Object>> getNotScappedDetailOperationType(String detailOperType) throws CustomException
	{
		String sql = "SELECT * FROM ENUMDEFVALUE WHERE  ENUMNAME = 'NotScrappDetailOperationType' AND ENUMVALUE=:ENUMVALUE ";

		Map<String, String> args = new HashMap<String, String>();
		args.put("ENUMVALUE", detailOperType);

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

		return result;
	}

	private ReworkProduct setReworkCountData(String productName, String factoryName, String processFlowName, String processFlowVer, String processOperName, String processOperVer, String reworkType,
			long reworkCount) throws CustomException
	{
		ReworkProduct reworkData = new ReworkProduct();

		reworkData.setProductName(productName);
		reworkData.setFactoryName(factoryName);
		reworkData.setProcessFlowName(processFlowName);
		reworkData.setProcessFlowVersion(processFlowVer);
		reworkData.setProcessOperationName(processOperName);
		reworkData.setProcessOperationVersion(processOperVer);
		reworkData.setReworkType(reworkType);
		reworkData.setReworkCount(reworkCount);

		return reworkData;
	}
}
