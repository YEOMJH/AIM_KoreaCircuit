package kr.co.aim.messolution.lot.event;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ChangeRecipe;
import kr.co.aim.messolution.extended.object.management.data.DummyProductReserve;
import kr.co.aim.messolution.extended.object.management.data.InlineSampleProduct;
import kr.co.aim.messolution.extended.object.management.data.OffsetAlignInfo;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.extended.object.management.data.SampleLot;
import kr.co.aim.messolution.extended.object.management.data.TPOffsetAlignInfo;
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
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class LotInfoDownloadRequest extends SyncHandler {
	private static Log log = LogFactory.getLog(LotInfoDownloadRequest.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		try
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "LotInfoDownLoadSend");
			
			// EventInfo
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
			
			
			Lot lotData = null;
			boolean isCSTCleaner = false;
			boolean isSorterCST  = false;
			boolean upkFlag = false;
			ArrayList<String> photoMaskList = new ArrayList<String>();
			List<Map<String, Object>> consumableSpecList = null;
			
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String portName    = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
			String slotMap     = SMessageUtil.getBodyItemValue(doc, "SLOTMAP", false);
			String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
			
			String maskSpecName      = "";
			String machineRecipeName = "";
			String reworkType        = "Rework";
			String productOffset = "";
			
			// Get LotName
			String lotName = this.getLotInfoBydurableNameForFisrtGlass(carrierName);
			
			// Get LotData
			if (StringUtils.isNotEmpty(lotName))
				lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

			// Case# LotData is null. 
			if (lotData == null)
			{
				log.info("No lot info exist by CST: " + carrierName);
				if (slotMap.contains("O"))
					throw new CustomException("CARRIER-9002", carrierName);
			}
			else
			{
				CommonValidation.checkFirstGlassLot(lotData, machineName);
			    CommonValidation.checkJobDownFlag(lotData);
			    CommonValidation.checkSorterSignReserve(lotData);
			}


			// Set Item (LOTNAME) 
			setLotNameToBody(SMessageUtil.getBodyElement(doc), lotData);
			
			
			
			
			// Existence Validation & Get Data
			Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName);
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
			MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
			
			
			
			// CST Cleaner - MachineGroupName like 'CST Cleaner'
			if (StringUtils.indexOf(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_CSTCleaner) >= 0
					|| StringUtils.indexOf(StringUtils.trim(machineData.getMachineGroupName()), GenericServiceProxy.getConstantMap().MachineGroup_CSTCleaner) >= 0)
			{
				isCSTCleaner = true;
			}

			// Sorter - MachineGroupName like 'SORTER'
			if (StringUtils.indexOf(StringUtils.upperCase(machineData.getMachineGroupName()), GenericServiceProxy.getConstantMap().MachineGroup_Sorter) >= 0)
				isSorterCST = true;

			if (machineData.getUdfs().get("OPERATIONMODE").equals(GenericServiceProxy.getConstantMap().SORT_OPERATIONMODE))
				isSorterCST = true;

			
			// Validation
			CommonValidation.CheckDurableState(durableData);
			CommonValidation.CheckDurableHoldState(durableData);
			CommonValidation.checkMachineHold(machineData);
			CommonValidation.checkMachineLockFlag(machineData, lotData);
			CommonValidation.CheckMachineStateExceptedEVALine(machineData, lotData);
			CommonValidation.check2ndLamination(portData, machineData, durableData);
			CommonValidation.checkLaminationSpec(lotData, portData, machineData);
			CommonValidation.checkOvenSortMode(machineData, portData, lotData);	
			CommonValidation.check2ndLaminationVendor(lotData,machineData);
			
			// 2021-02-04	dhko	Flow/Oper of NodeStack compare to Flow/Oper of Lot
			CommonValidation.checkNodeStackCompareToOperation(lotData);
			
			// 2020-11-14	dhko	Add Validation
			CommonValidation.checkProcessInfobyString(lotName, machineName,portName);
			
			//2021-05-12 add by yueke
			CommonValidation.checkRSMeterLot(lotName, machineData);			
			
			// Mantis-0000100
			CommonValidation.checkMQCDummyUsedCount(lotData);

			// [V3_MES_121_037]TP NG,Dummy Supplement Scenario_V1.02
			CommonValidation.checkDummyProductReserveOper(lotData);
			
			// Mantis-0000389
			CommonValidation.checkOLEDSortCarrier(machineData, durableData, lotData);
			
			//Get ConsumableSpecList (Only 5)
			//List<Map<String, Object>> consumableSpecList = this.getConsumableSpecList(lotData);
			// checkProductionTypebyPort xuch.
			CommonValidation.checkProductionTypebyPort(lotData,machineData,portData);
		
			if (lotData != null)
			{
				// checkMachineIdleTime hankun-0524.
				if(!StringUtils.equals(lotData.getProductionType(), "M")&&!StringUtils.equals(lotData.getProductionType(), "D"))
					CommonValidation.checkMachineIdleTime(machineData);
				// Validation for product quantity.
				if (lotData.getProductQuantity() <= 0)
					throw new CustomException("LOT-0149");
			}


			// Check Durable Clean State.
			if (!isCSTCleaner)
			{
				if (isSorterCST)
				{
					if (StringUtils.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_Available) || (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PS") && lotData == null))
							CommonValidation.CheckDurableCleanState(durableData);
				}
				else
					CommonValidation.CheckDurableCleanState(durableData);
			}
			

			if (lotData != null)
			{
				// BaseLine check by Runban Rule 
				if ("Y".equals(machineSpecData.getUdfs().get("BASELINEFLAG")))
					CommonValidation.checkBaseLine(lotData, machineData,"");

				// Check PhotoMask
				if (CommonUtil.equalsIn(machineSpecData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Photo, "TPPhoto"))
					photoMaskList = MESLotServiceProxy.getLotServiceUtil().checkPhotoMask(lotData, machineName);

				// Check Photo material QueueTime
				if(GenericServiceProxy.getConstantMap().Mac_ProductionMachine.equals(machineSpecData.getMachineType()) && GenericServiceProxy.getConstantMap().DetailMachineType_Main.equals(machineSpecData.getDetailMachineType()))
					MESLotServiceProxy.getLotServiceUtil().checkMaterialQTime(lotData,machineName);

				// Check Rework Count Limit
				CommonValidation.checkReworkLimit(lotData);
				
				// AR-Photo-0018-01 : Check Idle Time Limit
				calculateIdleTime(machineData, eventInfo);
				
				// AR-Photo-0033-01 : Check Expire Date
				calculateExpireDate(eventInfo, machineData);
			}
			
			//Mantis 0000431
			if(isSorterCST && lotData != null)
			{
				checkSorterJob(lotData, durableData, portData, machineData);
			}
			
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////Loader Port job download  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			if (!isCSTCleaner && (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PB") || CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PL")|| CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("BL") || CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("BB")))
			{
				// Lot Validation
				if (lotData == null)
					throw new CustomException("CARRIER-9002", carrierName);

				// Validation
				CommonValidation.checkLotState(lotData);
				CommonValidation.checkLotProcessState(lotData);
				CommonValidation.checkLotHoldState(lotData);
				CommonValidation.checkLotGrade(lotData);
				CommonValidation.checkLotIssueState(lotData);
				//Common  Check AllChamber
				List<Map<String, Object>> idleTimeByChamberData=getIDLETimeByChamber(machineName);
				if(idleTimeByChamberData!=null&&idleTimeByChamberData.size()>0){
					if(StringUtil.isNotEmpty(lotName)&&!(lotData.getProductionType().equals("M")||lotData.getProductionType().equals("D"))){
						CommonValidation.checkMachineIdleTime(machineData);
						String processOperationName=lotData.getProcessOperationName();
						String sql = " SELECT*FROM CT_MACHINEIDLEBYCHAMBER CH ,MACHINE MA ,MACHINESPEC MS  "
								+ "WHERE  MA.MACHINENAME=MS.MACHINENAME  "
								+ "AND  MA.MACHINENAME=CH.CHAMBERNAME "
								+ "AND CH.MACHINENAME =:MACHINENAME "
								+ "AND (CH.PROCESSOPERATIONNAME=:PROCESSOPERATIONNAME OR CH.PROCESSOPERATIONNAME = 'ALL') "
								+ "AND MS.CHECKIDLETIME='Y' AND CH.CONTROLSWITCH='Y' AND CH.IDLEGROUPNAME = 'ChamberIdle'  " ;
						Map<String, String> argsIdle = new HashMap<String, String>();
						argsIdle.put("MACHINENAME", machineName);
						argsIdle.put("PROCESSOPERATIONNAME", processOperationName);
						List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, argsIdle);
						if(result!=null&&result.size()>0){
							throw new CustomException("MACHINE-0038");
						}else{
							MESMachineServiceProxy.getMachineServiceImpl().checkIdleTimeByChamber(lotData, eventInfo, machineName);//check checkIdleTimeByChamber
						}
					}
					
				}
				//Array PVD/CVD not Check Down Chamber
				List<Map<String, Object>> idleTimeByNormalChamberData=getIDLETimeByNormalChamber(machineName);
				if(idleTimeByNormalChamberData!=null&&idleTimeByNormalChamberData.size()>0){
					if(StringUtil.isNotEmpty(lotName)&&!(lotData.getProductionType().equals("M")||lotData.getProductionType().equals("D"))){
						CommonValidation.checkMachineIdleTime(machineData);
						String processOperationName=lotData.getProcessOperationName();
						String sql = " SELECT*FROM CT_MACHINEIDLEBYCHAMBER CH ,MACHINE MA ,MACHINESPEC MS  "
								+ "WHERE  MA.MACHINENAME=MS.MACHINENAME  "
								+ "AND  MA.MACHINENAME=CH.CHAMBERNAME "
								+ "AND CH.MACHINENAME =:MACHINENAME "
								+ "AND (CH.PROCESSOPERATIONNAME=:PROCESSOPERATIONNAME OR CH.PROCESSOPERATIONNAME = 'ALL') "
								+ "AND MA.MACHINESTATENAME IN('RUN','IDLE')"
								+ "AND MS.CHECKIDLETIME='Y' AND CH.CONTROLSWITCH='Y' AND CH.IDLEGROUPNAME = 'ChamberIdle'  " ;
						Map<String, String> argsIdle = new HashMap<String, String>();
						argsIdle.put("MACHINENAME", machineName);
						argsIdle.put("PROCESSOPERATIONNAME", processOperationName);
						List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, argsIdle);
						if(result!=null&&result.size()>0){
							throw new CustomException("MACHINE-0038");
						}else{
							MESMachineServiceProxy.getMachineServiceImpl().checkIdleTimeByChamber(lotData, eventInfo, machineName);//check checkIdleTimeByChamber
							//MESMachineServiceProxy.getMachineServiceImpl().checkIdleTimeByNormalChamber(lotData, eventInfo, machineName);//check checkIdleTimeByChamber
						}
					}
					
				}

				// SlotMap Validation
				String logicalSlotMap = CommonUtil.getSlotMapInfo(durableData, lotData);
				if (!slotMap.equals(logicalSlotMap))
					throw new CustomException("PRODUCT-0020", slotMap, logicalSlotMap);

				// Check TPS Lot (for TP) 
				checkSlotMapInfoByEQP(machineName, lotData, durableData, logicalSlotMap);

				if(!isSorterCST)
				{
					// Get PPID to CT_ChangeRecipe
					machineRecipeName = 
						findPairingRecipe(lotData, lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getUdfs().get("BEFOREOPERATIONNAME"), lotData.getMachineName(), lotData.getProcessOperationName());
					//Add FLCRecipe
					if (StringUtils.equals(machineData.getMachineGroupName(),"FLC")&&(durableData != null && durableData.getDurableType().equals("FilmCST")))
					{
							if(StringUtil.isNotEmpty(lotName)){
								String productSpecName=lotData.getProductSpecName();
								String processFlowName=lotData.getProcessFlowName();
								String processOperationName=lotData.getProcessOperationName();
								String productRequestName=lotData.getProductRequestName();
								if(getFLCCheckRecipe(machineName,lotData)){
									String sql = "SELECT DISTINCT FLC.RECEIPENAME FROM CT_MATERIALPRODUCT MA ,PRODUCT P,CONSUMABLE CO,CT_FLCCONSUMABLERECIPE FLC "
											+ "WHERE P.LOTNAME=:LOTNAME AND P.PRODUCTNAME=MA.PRODUCTNAME AND MATERIALTYPE='TopLamination'  AND MA.MATERIALNAME=CO.CONSUMABLENAME"
											+ " AND FLC.PROCESSFLOWNAME=:PROCESSFLOWNAME AND FLC.PROCESSOPERATIONNAME=:PROCESSOPERATIONNAME and FLC.PRODUCTSPECNAME=:PRODUCTSPECNAME "
											+"AND FLC.CONSUMABLENAME=CO.CONSUMABLESPECNAME";
									Map<String, String> args = new HashMap<String, String>();
									args.put("LOTNAME", lotName);
									args.put("PRODUCTSPECNAME", productSpecName);
									args.put("PROCESSFLOWNAME", processFlowName);
									args.put("PROCESSOPERATIONNAME", processOperationName);
									List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);
									if(result!=null&&result.size()==1){
										String consumableRecipeName=result.get(0).get("RECEIPENAME").toString();
										List<Map<String, Object>> flcRecipeInfo = getFLCRecipe(lotData.getFactoryName(),lotData.getProductSpecName() ,lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
												lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),machineName);
										
										if (flcRecipeInfo.size() < 1)
											throw new CustomException("MACHINE-0102", "");
										String recipeName = ConvertUtil.getMapValueByName(flcRecipeInfo.get(0), "MACHINERECIPENAME");
										String rmsFlag = ConvertUtil.getMapValueByName(flcRecipeInfo.get(0), "RMSFLAG");
										String ecRecipeFlag = ConvertUtil.getMapValueByName(flcRecipeInfo.get(0), "ECRECIPEFLAG");
										String ecRecipeName = ConvertUtil.getMapValueByName(flcRecipeInfo.get(0), "ECRECIPENAME");
										if(recipeName!=null&&recipeName.equals(consumableRecipeName)){
											machineRecipeName=recipeName;
									    }else if(ecRecipeName!=null&&ecRecipeName.equals(consumableRecipeName)){
									    	machineRecipeName=ecRecipeName;
									    }else{
									    	throw new CustomException("MACHINE-0043");
									    }
										
									 }else{
										
										throw new CustomException("RMS-0005");
									}
									
								}
								//202/5/23 Add updatePostCellLoadInfo caixu
								if(checkPostCellLoad()){
									
									updatePostCellLoadInfo(machineName,portName,productSpecName,processOperationName,productRequestName);
								}
						   }
					}
						
					// Get PPID to POSMachine
					if(StringUtils.isEmpty(machineRecipeName))
						machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineName, false);
					
				}

				// for RMS
				//CommonValidation.checkRecipeV3(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
				//		lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineName, machineRecipeName, false);

				// Check WIP Balance 临时屏蔽 caixu 2021/6/3
				//checkWIPBalance(lotData, machineName);

				// Q-time
				ExtendedObjectProxy.getProductQTimeService().monitorProductQTime(eventInfo, lotData.getKey().getLotName(), machineName);
				ExtendedObjectProxy.getProductQTimeService().validateQTimeByLot(lotData.getKey().getLotName());

				// OLED MaskSpec Validation
				if (StringUtils.equals(lotData.getFactoryName(), "OLED") && StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_EVA))
					maskSpecName = checkMaskSpecName(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion());

				//  DummyReserveCheck
				if (StringUtil.equals(lotData.getProductionType(), GenericServiceProxy.getConstantMap().Pord_Dummy))
				{
					List<DummyProductReserve> dummyOperation = ExtendedObjectProxy.getDummyProductReserveService().getDummyProductReserveData(lotData);
					
					if(dummyOperation == null || dummyOperation.size() == 0 || StringUtil.equals(dummyOperation.get(0).getProcessingFlag(), "Y"))
					{
						throw new CustomException("DUMMYPRODUCT-0003", lotData.getKey().getLotName());
					}
				}
				
				
				

				//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				// Grnerate Body Template
				generateBodyTemplate(SMessageUtil.getBodyElement(doc), lotData,machineSpecData, durableData.getDurableState(), durableData.getDurableType(), machineRecipeName);
				
				
				//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				// Make ProductList Element
				for (Element productElement : this.generateProductListElement(
						eventInfo, lotData, machineData, portData, durableData, machineRecipeName, lotData.getProductRequestName(), reworkType, maskSpecName))
				{
					Element productListElement = XmlUtil.getChild(SMessageUtil.getBodyElement(doc), "PRODUCTLIST", true);
					productListElement.addContent(productElement);
				}
				//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				
				//Set Machine Recipe > PhotoRecipe 2020-08-25 ghhan
				
				if (CommonUtil.equalsIn(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Photo))
				{
					if(lotData.getFactoryName().equals("TP"))
					{
						List<Element> productListElement = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false);
						machineRecipeName = productListElement.get(0).getChildText("PRODUCTRECIPE");
						SMessageUtil.setBodyItemValue(doc, "MACHINERECIPENAME", machineRecipeName);
						Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productListElement.get(0).getChildText("PRODUCTNAME"));
						productOffset = productData.getUdfs().get("OFFSET").toString();
					}
				}
			}
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////PortType is 'PS' job download  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PS"))
			{
				eventLog.info("Sorter Port job download");

				// Lot validation
				if (lotData != null)
				{
					CommonValidation.checkLotState(lotData);
					CommonValidation.checkLotProcessState(lotData);
					CommonValidation.checkLotHoldState(lotData);
					CommonValidation.checkLotIssueState(lotData);
					
					//Auto Sort Mode ProductSpec、ProcessFlow、ProcessOperation、ProductRequest
					CommonValidation.checkAutoSorterSpec_Flow_Operation_ProductRequest(machineData, durableData, lotData);

					// Slot map validation
					String logicalSlotMap = CommonUtil.getSlotMapInfo(durableData, lotData);
					if (!slotMap.equals(logicalSlotMap))
						throw new CustomException("PRODUCT-0020", slotMap, logicalSlotMap);

					/*
					// Recipe
					try{
						machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineName, false);
					}catch(Exception ex)
					{
						eventLog.info("Sorter Recipe is not Registered.");
					}
					*/
					
					//SMessageUtil.setBodyItemValue(doc, "MACHINERECIPENAME", machineRecipeName);
					
					// Q-time 
					//ExtendedObjectProxy.getProductQTimeService().monitorProductQTime(eventInfo, lotData.getKey().getLotName(), machineName);
					//ExtendedObjectProxy.getProductQTimeService().validateQTimeByLot(lotData.getKey().getLotName());

					
					
					
					
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					// Grnerate Body Template
					this.generateBodyTemplate(SMessageUtil.getBodyElement(doc), lotData,machineSpecData, durableData.getDurableState(), durableData.getDurableType(), machineRecipeName);

					
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					// Make ProductList Element
					for (Element productElement : this.generateProductListElement(
							eventInfo, lotData, machineData, portData, durableData, machineRecipeName, lotData.getProductRequestName(), reworkType, maskSpecName))
					{
						Element productListElement = XmlUtil.getChild(SMessageUtil.getBodyElement(doc), "PRODUCTLIST", true);
						productListElement.addContent(productElement);
					}
					//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					
					
				}
				else
				{
					this.generateBodyTemplate(SMessageUtil.getBodyElement(doc), lotData,machineSpecData, durableData.getDurableState(), durableData.getDurableType(), machineRecipeName);
					//SMessageUtil.setBodyItemValue(doc, "MACHINERECIPENAME", "");
				}
			}
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////UnLoader Port job download  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PU")
					&& StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Unpacker))
			{
				upkFlag = true;
				eventLog.info("Unpacker unloader Port job download");

				// Validation
				CommonValidation.checkEmptyCst(carrierName);

				// SlotMap Validation
				String logicalSlotMap = MESProductServiceProxy.getProductServiceUtil().getSlotMap(durableData);
				if (StringUtils.isNotEmpty(slotMap) && !slotMap.equals(logicalSlotMap))
					throw new CustomException("PRODUCT-0020", slotMap, logicalSlotMap);

				// Input Scenario Case# Get ReserveLot : order by position. 
				ReserveLot reserveLot = ExtendedObjectProxy.getReserveLotService().getFirstReserveLot(machineName);
				reserveLot.setReserveState(GenericServiceProxy.getConstantMap().RESV_STATE_START);
				ExtendedObjectProxy.getReserveLotService().modify(eventInfo, reserveLot);

				// Get LotData
				lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(reserveLot.getLotName());

				// Get MachineRecipeName (POSMachine)
				machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineName, false);

				//Get ConsumableSpecList (Only 5)
				consumableSpecList = this.getConsumableSpecList(lotData);

				//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				// Grnerate Body Template
				this.generateBodyTemplate(SMessageUtil.getBodyElement(doc), lotData,machineSpecData, durableData.getDurableState(), durableData.getDurableType(), machineRecipeName);

				SMessageUtil.setBodyItemValue(doc, "WORKORDER", lotData.getProductRequestName());
				SMessageUtil.setBodyItemValue(doc, "LOTNAME", lotData.getKey().getLotName());
				SMessageUtil.setBodyItemValue(doc, "MACHINERECIPENAME", machineRecipeName);
				SMessageUtil.setBodyItemValue(doc, "PRODUCTQUANTITY", lotData.getUdfs().get("PLANPRODUCTQUANTITY").toString());
				
				//CrateSpecList
				this.setCrateSpecList(SMessageUtil.getBodyElement(doc), consumableSpecList, upkFlag);
				//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				
				return doc;
			}
			else if (isCSTCleaner)
			{
				CommonValidation.checkEmptyCst(carrierName);
				checkAssignedLotByCst(carrierName);

				this.generateBodyTemplate(SMessageUtil.getBodyElement(doc), lotData,machineSpecData, durableData.getDurableState(), durableData.getDurableType(), machineRecipeName);

				slotMap = StringUtils.repeat(GenericServiceProxy.getConstantMap().PRODUCT_NOT_IN_SLOT, (int) durableData.getCapacity());
				SMessageUtil.setBodyItemValue(doc, "SLOTMAP", slotMap);
				SMessageUtil.setBodyItemValue(doc, "MACHINERECIPENAME", "");
			}
			else
			{
				eventLog.info("Unloader Port job download");

				CommonValidation.checkEmptyCst(carrierName);
				this.generateBodyTemplate(SMessageUtil.getBodyElement(doc), lotData,machineSpecData, durableData.getDurableState(), durableData.getDurableType(), machineRecipeName);
				SMessageUtil.setBodyItemValue(doc, "MACHINERECIPENAME", "");
			}
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////End  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
			
			
			
			
			// Check Runban prevent rule
			if (lotData != null && !isCSTCleaner)
			{
				List<String> selProductNameList = new ArrayList<>();
				List<Element> productElementList = SMessageUtil.getSubSequenceItemList(SMessageUtil.getBodyElement(doc), "PRODUCTLIST", false);

				for (Element productElement : productElementList)
				{
					if ("Y".equals(productElement.getChildText("SAMPLINGFLAG")))
						selProductNameList.add(productElement.getChildText("PRODUCTNAME"));
				}

				if (selProductNameList != null && selProductNameList.size() > 0)
					this.checkRunbanPreventRule(lotData, machineName, selProductNameList);
			}
			
			
			// Generate selection map (Make SlotSel) 
			String slotSel = this.doGlassSelection(doc, lotData, durableData, slotMap);

			
			
			// Set work order
			if (lotData != null)
			{
				SMessageUtil.setBodyItemValue(doc, "WORKORDER", lotData.getProductRequestName());
			//ERP BOM注释修复 2021/1/12 add by xiaoxh	
//				//Check ERPBOM 2020-10-15
//				ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());
//				
//				if(StringUtil.isNotEmpty(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
//				{
//					MESConsumableServiceProxy.getConsumableServiceUtil().compareERPBOM(lotData.getFactoryName(), productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME"), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineName, lotData.getProductSpecName());
//				}
			}
		
			// Update CANCELINFOFLAG for DSP
			if (durableData.getUdfs().get("CANCELINFOFLAG").equals("Y"))
			{
				kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfoDur = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
				Map<String, String> durableUdfs = new HashMap<>();
				durableUdfs.put("CANCELINFOFLAG", "");
				setEventInfoDur.setUdfs(durableUdfs);
				DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfoDur);
			}

			// Set JobDownFlag and record to LotHistory
			if (lotData != null)
			{
				eventInfo.setEventName("LotInfoDownLoad");
				lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getKey().getLotName());

				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.getUdfs().put("JOBDOWNFLAG",  "Y");

				LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
			}

			
			
			
			// Check ProductGrade (W) 
			if (lotData != null)
			{
				List<String> productNameList = new ArrayList<>();
				List<Product> producList = LotServiceProxy.getLotService().allProducts(lotName);
				
				// check ProductGrade
				for (Product productA : producList)
				{
					String productName = productA.getKey().getProductName();
					productNameList.add(productName);
				}
				CommonValidation.checkProductGradebyString(productNameList);// 20190228 Add
			}

			
			// Check ProductGrade (P) 
			if (lotData != null)
				if (lotData.getProductionType().equals("P") || lotData.getProductionType().equals("E"))
					CommonValidation.checkProductGradeP(lotData);

			// [V3_MES_121_004]DSP Run Control_V1.02
			int actualProductQty = 0;
			for (int i = 0 ; i < slotSel.length() ; i++)
			{
				if (slotSel.substring(i, i+1).equals(GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT))
					actualProductQty++;
			}
			if (lotData != null)
				MESLotServiceProxy.getLotServiceUtil().increaseRunControlUseCount(eventInfo, machineName, lotData.getKey().getLotName(), true, actualProductQty);

			//PhotoMaskList
			this.setPhotoMaskList(SMessageUtil.getBodyElement(doc), photoMaskList);

			//CrateSpecList
			this.setCrateSpecList(SMessageUtil.getBodyElement(doc), consumableSpecList, upkFlag);
			
			//Set SlotSel to Lot Table (AR-AMF-0029-01)
			this.setSlotSelData(eventInfo, lotData, slotSel);

			//Increase Recipe TimeUsed 
			//2020-07-21 RMSFlag = N Pass ghhan
			
			if(lotData != null&&(!isSorterCST && !isCSTCleaner))
			{
				if (MESRecipeServiceProxy.getRecipeServiceUtil().RMSFlagCheck("E", machineName, machineRecipeName, "", lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), productOffset))
					checkRecipeOnTrackInTime(machineName, machineRecipeName);
					//MESRecipeServiceProxy.getRecipeServiceUtil().checkRecipeOnTrackInTime(machineName, machineRecipeName);
			}		
			
			return doc;
		}
		catch (CustomException ce)
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "LotCancelCommandSend");

			this.generateNGBodyTemplate(doc, SMessageUtil.getBodyElement(doc));

			throw new CustomException("SYS-0010", ce.errorDef.getLoc_errorMessage());
		}
		catch (Exception e)
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "LotCancelCommandSend");

			this.generateNGBodyTemplate(doc, SMessageUtil.getBodyElement(doc));

			throw new CustomException(e);
		}
	}
	
	private void checkRunbanPreventRule(Lot lotData,String machineName,List<String> productNameList) throws CustomException
	{
		if (lotData == null || productNameList.size() == 0)
		{
			log.info("checkRunbanPreventRule:The incoming variable value is null or empty!!");
			return;
		}

		ConstantMap constMap = GenericServiceProxy.getConstantMap();

		ProcessFlow curFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);
		ProcessFlow mainFlowData = MESLotServiceProxy.getLotInfoUtil().getMainProcessFlowData(lotData);

		if (StringUtil.in(curFlowData.getProcessFlowType(), "MQC", "MQCRecycle"))
		{
			if (!StringUtil.in(lotData.getProductionType(), constMap.Pord_MQC, constMap.Pord_Dummy)) return;
		}
		else
		{
			if (!StringUtil.in(lotData.getProductionType(), constMap.Pord_Production, constMap.Pord_Engineering, constMap.Pord_Test)) return;
		}

		List<Product> productDataList = MESProductServiceProxy.getProductServiceUtil().getProductDataListByNameList(productNameList);

		if (productDataList == null || productDataList.size() == 0) return;
		
		CommonValidation.checkRunbanPreventRule(productDataList, machineName, mainFlowData.getUdfs().get("RUNBANPROCESSFLOWTYPE"));
	}
	
	
	private void setLotNameToBody(Element bodyElement, Lot lotData) throws CustomException
	{
		JdomUtils.addElement(bodyElement, "LOTNAME", (lotData == null) ? "" : lotData.getKey().getLotName());
	}
	
	private void setPhotoMaskList(Element bodyElement, ArrayList<String> photoMaskList) throws CustomException
	{
		Element photoList = new Element("PHOTOMASKLIST");
		
		// 2021-01-12	dhko	Add check Null
		if (photoMaskList != null && photoMaskList.size() > 0)
		{
			for(String maskName : photoMaskList)
			{
				Element attMask = new Element("PHOTOMASK");
				
				Element attMaskID = new Element("MASKNAME");
				attMaskID.setText(maskName);
				attMask.addContent(attMaskID);
				
				photoList.addContent(attMask);
			}
		}
		
		bodyElement.addContent(photoList);
	}
	
	private void setCrateSpecList(Element bodyElement, List<Map<String, Object>> consumableSpecList, boolean upkFlag) throws CustomException
	{
		Element crateSpecList = new Element("CRATESPECLIST");
	
		if(consumableSpecList != null)
		{
			for(Map<String, Object> consumalbeSpecName : consumableSpecList)
			{
				Element crateSpec = new Element("CRATESPEC");
				
				Element crateSpecName = new Element("CRATESPECNAME");
				
				if(upkFlag)
					crateSpecName.setText(consumalbeSpecName.get("MATERIALSPECNAME").toString());
				else
					crateSpecName.setText("");
				
				crateSpec.addContent(crateSpecName);
				crateSpecList.addContent(crateSpec);
			}
		}
		
		bodyElement.addContent(crateSpecList);
	}

	private Element generateBodyTemplate(Element bodyElement, Lot lotData,MachineSpec machineSpecData, String durableState, String durableType, String machineRecipeName) throws CustomException
	{

		Element removeElement = bodyElement.getChild("PORTACCESSMODE");

		if (removeElement != null)removeElement.detach();
		 
		if (lotData != null)
		{
			JdomUtils.addElement(bodyElement, "PROCESSOPERATIONNAME", lotData.getProcessOperationName());
			JdomUtils.addElement(bodyElement, "PROCESSOPERATIONVERSION", lotData.getProcessOperationVersion());
			JdomUtils.addElement(bodyElement, "CARRIERSTATE", durableState);
			JdomUtils.addElement(bodyElement, "CARRIERTYPE", durableType);
			JdomUtils.addElement(bodyElement, "PRODUCTSPECNAME", lotData.getProductSpecName());
			JdomUtils.addElement(bodyElement, "PRODUCTSPECVERSION", lotData.getProductSpecVersion());
			JdomUtils.addElement(bodyElement, "PROCESSFLOWNAME", lotData.getProcessFlowName());
			JdomUtils.addElement(bodyElement, "PROCESSFLOWVERSION", lotData.getProcessFlowVersion());
			JdomUtils.addElement(bodyElement, "PRODUCTIONTYPE", lotData.getProductionType());
			JdomUtils.addElement(bodyElement, "PRODUCTQUANTITY", String.valueOf((int) lotData.getProductQuantity()));
			JdomUtils.addElement(bodyElement, "SLOTSEL", "");
			JdomUtils.addElement(bodyElement, "MACHINERECIPENAME", machineRecipeName);
			JdomUtils.addElement(bodyElement, "LOTJUDGE", lotData.getLotGrade());
			JdomUtils.addElement(bodyElement, "WORKORDER", "");
			
			if (StringUtils.isEmpty(machineSpecData.getUdfs().get("QTIMEFLAG")))
				JdomUtils.addElement(bodyElement, "QTIMEFLAG", GenericServiceProxy.getConstantMap().FLAG_Y);
			else
				JdomUtils.addElement(bodyElement, "QTIMEFLAG", machineSpecData.getUdfs().get("QTIMEFLAG"));
			//get lot baselineflag
			String baseLineFlag = CheckBaseLineFlag(lotData,bodyElement,machineSpecData);
			//JdomUtils.addElement(bodyElement, "BASELINEFLAG", machineSpecData.getUdfs().get("BASELINEFLAG"));
			JdomUtils.addElement(bodyElement, "BASELINEFLAG", baseLineFlag);
			JdomUtils.addElement(bodyElement, "PRODUCTLIST", "");
		}
		else
		{
			JdomUtils.addElement(bodyElement, "PROCESSOPERATIONNAME", StringUtils.EMPTY);
			JdomUtils.addElement(bodyElement, "PROCESSOPERATIONVERSION", StringUtils.EMPTY);
			JdomUtils.addElement(bodyElement, "CARRIERSTATE", durableState);
			JdomUtils.addElement(bodyElement, "CARRIERTYPE", durableType);
			JdomUtils.addElement(bodyElement, "PRODUCTSPECNAME", StringUtils.EMPTY);
			JdomUtils.addElement(bodyElement, "PRODUCTSPECVERSION", StringUtils.EMPTY);
			JdomUtils.addElement(bodyElement, "PROCESSFLOWNAME", StringUtils.EMPTY);
			JdomUtils.addElement(bodyElement, "PROCESSFLOWVERSION", StringUtils.EMPTY);
			JdomUtils.addElement(bodyElement, "PRODUCTIONTYPE", StringUtils.EMPTY);
			JdomUtils.addElement(bodyElement, "PRODUCTQUANTITY", "");
			JdomUtils.addElement(bodyElement, "SLOTSEL", "");
			JdomUtils.addElement(bodyElement, "MACHINERECIPENAME", "");
			JdomUtils.addElement(bodyElement, "LOTJUDGE", StringUtils.EMPTY);
			JdomUtils.addElement(bodyElement, "WORKORDER", "");
			JdomUtils.addElement(bodyElement, "QTIMEFLAG", "");
			JdomUtils.addElement(bodyElement, "BASELINEFLAG", "");
			JdomUtils.addElement(bodyElement, "PRODUCTLIST", "");
		}
		return bodyElement;
	}

	private String CheckBaseLineFlag(Lot lotData ,Element bodyElement,MachineSpec machineSpecData) throws CustomException
	{
		String baseLineFlag = "Y";
		String lotNmae = lotData.getKey().getLotName();
		String toFlowName = lotData.getProcessFlowName();
		String sampleOperationName = lotData.getProcessOperationName();
		String machineName = bodyElement.getChildText("MACHINENAME");
		List<Product> productList = new ArrayList<Product>();
		
		if(StringUtils.equals(machineSpecData.getMachineGroupName(), "Unpacker"))
		{
			boolean isExist = ExtendedObjectProxy.getEnumDefValueService().isExistEnumNameInfo("UPKBaseLineFlag", lotData.getProductSpecName());
			if(isExist)
			{
				baseLineFlag="Y";
			}
			else
			{
				baseLineFlag="N";
			}
			return baseLineFlag;
		}

		ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);
		
		String sql= "SELECT P.LOTNAME,V.BASELINEFLAG FROM "
				+ "PRODUCT P , CT_VALICATIONPRODUCT V "
				+ "WHERE P.LOTNAME = :LOTNAME "
				+ "AND  P.PRODUCTNAME = V.PRODUCTNAME  "
				+ "AND P.PRODUCTSPECNAME = V.PRODUCTSPECNAME"
				+ " AND P.PROCESSFLOWNAME = V.TOFLOWNAME "
				+ "AND V.TOFLOWNAME= :TOFLOWNAME "
				+ "AND P.PROCESSOPERATIONNAME = V.SAMPLEOPERATIONNAME "
				+ "AND V.SAMPLEOPERATIONNAME = :SAMPLEOPERATIONNAME "
				+ "AND V.BASELINEFLAG = 'Y'";
		
		String sql1 = "SELECT P.LOTNAME,V.BASELINEFLAG FROM "
				+ "PRODUCT P , CT_VALICATIONPRODUCT V "
				+ "WHERE 1=1 AND P.LOTNAME = :LOTNAME "
				+ "AND  P.PRODUCTNAME = V.PRODUCTNAME  "
				+ "AND V.ALLFLAG= 'Y' AND V.BASELINEFLAG = 'Y'";
		
		String sql2 = "SELECT P.LOTNAME,V.BASELINEFLAG "
				+ "FROM PRODUCT P , CT_VALICATIONPRODUCT V  "
				+ "WHERE 1=1 AND P.LOTNAME = :LOTNAME "
				+ "AND  P.PRODUCTNAME = V.PRODUCTNAME  "
				+ "AND V.ENGOPERATIONNAME=:OPERATIONNAME "
				//+ "AND V.ENGMACHINENAME = :MACHINENAME"
				+ "AND V.BASELINEFLAG = 'Y'";
		
		if(!CommonUtil.equalsIn(flowData.getProcessFlowType(), "Inspection", "Sample"))
		{
			List<Map<String, Object>> result1;
			Map<String, Object> bindMap1 = new HashMap<String, Object>();
			bindMap1.put("LOTNAME",lotNmae);
			try
			{
				result1 = GenericServiceProxy.getSqlMesTemplate().queryForList(sql1, bindMap1);
			}
			catch (FrameworkErrorSignal fe)
			{
				result1 = null;
			}
			if(result1 != null && result1.size() > 0)
			{
				//baseLineFlag = "Y";
				baseLineFlag = "N";
				return baseLineFlag;
			}
			else
			{
				List<Map<String, Object>> result2;
				Map<String, Object> bindMap2 = new HashMap<String, Object>();
				bindMap2.put("LOTNAME",lotNmae);
				bindMap2.put("OPERATIONNAME",sampleOperationName);
				//bindMap2.put("MACHINENAME",machineName);
				try
				{
					result2 = GenericServiceProxy.getSqlMesTemplate().queryForList(sql2, bindMap2);
				}
				catch (FrameworkErrorSignal fe)
				{
					result2 = null;
				}
				if(result2 != null && result2.size() > 0)
				{
					//baseLineFlag = "Y";
					baseLineFlag = "N";
					return baseLineFlag;
				}
				else
				{
					return baseLineFlag;
				}
			}
		}
		else
		{
			List<Map<String, Object>> result1;
			Map<String, Object> bindMap1 = new HashMap<String, Object>();
			bindMap1.put("LOTNAME",lotNmae);
			try
			{
				result1 = GenericServiceProxy.getSqlMesTemplate().queryForList(sql1, bindMap1);
			}
			catch (FrameworkErrorSignal fe)
			{
				result1 = null;
			}
			if(result1 != null && result1.size() > 0)
			{
				//baseLineFlag = "Y";
				baseLineFlag = "N";
				return baseLineFlag;
			}
			else
			{
				List<Map<String, Object>> result;
				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("LOTNAME",lotNmae);
				bindMap.put("TOFLOWNAME",toFlowName);
				bindMap.put("SAMPLEOPERATIONNAME",sampleOperationName);
				try
				{
					result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
				}
				catch (FrameworkErrorSignal fe)
				{
					result = null;
				}
				if(result != null && result.size() > 0)
				{
					//baseLineFlag = "Y";
					baseLineFlag = "N";
					return baseLineFlag;
				}
				else
				{
					return baseLineFlag;
				}
			}
		}
		// TODO Auto-generated method stub
		
	}

	private void generateNGBodyTemplate(Document doc, Element bodyElementOri) throws CustomException
	{
		Element bodyElement = null;
		bodyElement = new Element("Body");

		Element machineNameElement = new Element("MACHINENAME");
		machineNameElement.setText(bodyElementOri.getChildText("MACHINENAME"));
		bodyElement.addContent(machineNameElement);

		Element portNameElement = new Element("PORTNAME");
		portNameElement.setText(bodyElementOri.getChildText("PORTNAME"));
		bodyElement.addContent(portNameElement);

		Element portTypeElement = new Element("PORTTYPE");
		portTypeElement.setText(bodyElementOri.getChildText("PORTTYPE"));
		bodyElement.addContent(portTypeElement);

		Element portUseTypeElement = new Element("PORTUSETYPE");
		portUseTypeElement.setText(bodyElementOri.getChildText("PORTUSETYPE"));
		bodyElement.addContent(portUseTypeElement);

		Element lotNameElement = new Element("LOTNAME");
		lotNameElement.setText(bodyElementOri.getChildText("LOTNAME"));
		bodyElement.addContent(lotNameElement);

		Element carrierNameElement = new Element("CARRIERNAME");
		carrierNameElement.setText(bodyElementOri.getChildText("CARRIERNAME"));
		bodyElement.addContent(carrierNameElement);

		// first removal of existing node would be duplicated
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		// index of Body node is static
		doc.getRootElement().addContent(2, bodyElement);
	}

	private List<Element> generateProductListElement(EventInfo eventInfo, Lot lotData, Machine machineData, Port portData, Durable durableData, String machineRecipeName, String workOrderName,
			String reworkType, String maskSpecName) throws CustomException
	{
		boolean isFirstGlass = false;
		boolean isLightingInspection = false;
		boolean isPhoto = false;
		
		MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineData.getKey().getMachineName());

		if (CommonUtil.equalsIn(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Photo))
			isPhoto = true;

		List<Product> productList = new ArrayList<Product>();

		if (StringUtils.equals(durableData.getDurableType(), GenericServiceProxy.getConstantMap().CST_TYPE_BUFFER))
		{
			productList = ProductServiceProxy.getProductService().select(" WHERE carrierName = ? AND productState != ? AND productState != ? ORDER BY position, slotPosition ",
					new Object[] { durableData.getKey().getDurableName(), GenericServiceProxy.getConstantMap().Prod_Scrapped, GenericServiceProxy.getConstantMap().Prod_Consumed });
		}
		else if (StringUtils.equals(lotData.getUdfs().get("FIRSTGLASSFLAG"), "N") && StringUtils.isNotEmpty(lotData.getUdfs().get("JOBNAME"))) // for firstGlass cLot.
		{	// for firstGlass cLot.
			// select productList by cstName
			productList = ProductServiceProxy.getProductService().select(" WHERE carrierName = ? AND productState != ? AND productState != ? ORDER BY position ",
					new Object[] { durableData.getKey().getDurableName(), GenericServiceProxy.getConstantMap().Prod_Scrapped, GenericServiceProxy.getConstantMap().Prod_Consumed });

			isFirstGlass = true;
		}
		else if (StringUtils.isNotEmpty(lotData.getUdfs().get("JOBNAME")) && StringUtils.equals(lotData.getUdfs().get("FIRSTGLASSALL"), "Y"))
		{	// for firstGlass ALL.
			// select productList by cstName
			productList = ProductServiceProxy.getProductService().select(" WHERE carrierName = ? AND productState != ? AND productState != ? ORDER BY position ",
					new Object[] { durableData.getKey().getDurableName(), GenericServiceProxy.getConstantMap().Prod_Scrapped, GenericServiceProxy.getConstantMap().Prod_Consumed });

			isFirstGlass = true;
		}
		else if (StringUtils.equals(lotData.getUdfs().get("FIRSTGLASSFLAG"), StringUtils.EMPTY) && StringUtils.isNotEmpty(lotData.getUdfs().get("JOBNAME"))
				&& StringUtils.isEmpty(lotData.getUdfs().get("FIRSTGLASSALL")))
		{	// for firstGlass mLot.
			// select productList by cstName
			productList = ProductServiceProxy.getProductService().select(" WHERE carrierName = ? AND productState != ? AND productState != ? ORDER BY position ",
					new Object[] { durableData.getKey().getDurableName(), GenericServiceProxy.getConstantMap().Prod_Scrapped, GenericServiceProxy.getConstantMap().Prod_Consumed });
		}
		else
		{
			productList = ProductServiceProxy.getProductService().select(" WHERE lotName = ? AND productState != ? AND productState != ? ORDER BY position, slotPosition ",
					new Object[] { lotData.getKey().getLotName(), GenericServiceProxy.getConstantMap().Prod_Scrapped, GenericServiceProxy.getConstantMap().Prod_Consumed });
		}
		
		
		//Validation product (productGrade is S 鈫?LotCancelCommadSend)
		if(!StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Sorter))
			CommonValidation.checkProductGrade(lotData, productList);
		

		ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion());
		ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);
		ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());

		if (StringUtils.equals(lotData.getFactoryName(), "OLED") && StringUtils.equals(operationData.getDetailProcessOperationType(), "ILO")
				&& CommonUtil.equalsIn(flowData.getProcessFlowType(), "Inspection", "Sample"))
		{
			// OLED Lighting Inspection - Sampling
			isLightingInspection = true;
		}

		// make sure TK cancel
		boolean abortFlag = false;
		try
		{
			if (StringUtils.equals(durableData.getDurableType(), GenericServiceProxy.getConstantMap().CST_TYPE_BUFFER))
			{
				ProductServiceProxy.getProductService().select(
						"carrierName = ? AND processingInfo = ? AND productState = ?", new Object[] { durableData.getKey().getDurableName(), "B", GenericServiceProxy.getConstantMap().Prod_InProduction });
			}
			else
			{
				ProductServiceProxy.getProductService().select(
						"lotName = ? AND processingInfo = ? AND productState = ?", new Object[] { lotData.getKey().getLotName(), "B", GenericServiceProxy.getConstantMap().Prod_InProduction });
			}

			abortFlag = true;
		}
		catch (NotFoundSignal ne)
		{
			eventLog.info("retry after TK In Canceled");

			abortFlag = false;
		}

		List<ListOrderedMap> sortJobList;
		try
		{
			// Sorter job reserved
			StringBuffer sqlBuffer = new StringBuffer();
			sqlBuffer.append("SELECT J.JOBNAME, ");
			sqlBuffer.append("       J.JOBSTATE, ");
			sqlBuffer.append("       C.MACHINENAME, ");
			sqlBuffer.append("       C.PORTNAME, ");
			sqlBuffer.append("       C.CARRIERNAME, ");
			sqlBuffer.append("       P.FROMLOTNAME, ");
			sqlBuffer.append("       P.PRODUCTNAME, ");
			sqlBuffer.append("       P.FROMPOSITION, ");
			sqlBuffer.append("       P.FROMSLOTPOSITION, ");
			sqlBuffer.append("       P.TURNDEGREE ");
			sqlBuffer.append("  FROM CT_SORTJOB J, CT_SORTJOBCARRIER C, CT_SORTJOBPRODUCT P ");
			sqlBuffer.append(" WHERE J.JOBNAME = C.JOBNAME ");
			sqlBuffer.append("   AND C.JOBNAME = P.JOBNAME ");
			sqlBuffer.append("   AND C.MACHINENAME = P.MACHINENAME ");
			sqlBuffer.append("   AND C.CARRIERNAME = P.FROMCARRIERNAME ");
			sqlBuffer.append("   AND C.CARRIERNAME = ? ");
			sqlBuffer.append("   AND C.MACHINENAME = ? ");
			sqlBuffer.append("   AND C.PORTNAME = ? ");
			sqlBuffer.append("   AND J.JOBSTATE = ? ");

			Object[] bindList = new Object[] { durableData.getKey().getDurableName(), machineData.getKey().getMachineName(), portData.getKey().getPortName(),
					GenericServiceProxy.getConstantMap().SORT_JOBSTATE_CONFIRMED };
			sortJobList = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bindList);
		}
		catch (Exception ex)
		{
			sortJobList = new ArrayList<ListOrderedMap>();
		}

		List<SampleLot> sampleLot = new ArrayList<SampleLot>();
		List<Map<String, Object>> mqcLot = new ArrayList<Map<String, Object>>();

		if (StringUtils.equals(flowData.getProcessFlowType(), "MQC"))
		{
			mqcLot = getMQCProduct(lotData.getKey().getLotName());

			if (mqcLot.size() == 0)
			{
				throw new CustomException("LOT-0301", lotData.getKey().getLotName(), lotData.getProcessOperationName());
			}
		
		}
		else if (StringUtils.equals(flowData.getProcessFlowType(), "MQCRecycle"))
		{
			mqcLot = getMQCRecycleProduct(lotData.getKey().getLotName());

			if (mqcLot.size() == 0)
			{
				throw new CustomException("LOT-0301", lotData.getKey().getLotName(), lotData.getProcessOperationName());
			}
		}
		else
		{
			if (isLightingInspection)
			{
				// Reserved forceSampling.
				log.info("Check Force Samping Data");
				sampleLot = ExtendedObjectProxy.getSampleLotService().getForceSampleLotDataListByToInfo(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
						lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), "Y");

				if (sampleLot==null||sampleLot.size() == 0)
				{
					// LightingSample
					log.info("Check OLED Lighting Inspection Glass");
					sampleLot = getLightingSampleData(lotData, machineData.getKey().getMachineName());
				}
			}
			// Reserved Sample
			if (sampleLot.size() == 0)
			{
				sampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListByToInfo(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
						lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());
			}
		}

		// MQC reserved
		List<Object> MQCSampleSlotList = new ArrayList<Object>();
		if (StringUtils.equals(flowData.getProcessFlowType(), "MQC") || StringUtils.equals(flowData.getProcessFlowType(), "MQCRecycle"))
		{
			MQCSampleSlotList = getSelectedSlotbySampleLotForMQC(mqcLot);
		}

		if (sampleLot != null && isLightingInspection == false && isFirstGlass == false && mqcLot.size() < 1)
		{
			if ((flowData.getProcessFlowType().equals("Inspection") || flowData.getProcessFlowType().equals("Sample")) && !flowData.getProcessFlowType().equals("MQC"))
			{
				// Sync Samping Data
				List<Product> syncProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());
				syncProductList = CommonUtil.makeProdListForDummy(syncProductList);
				
				sampleLot = MESLotServiceProxy.getLotServiceUtil().syncSamplingData(eventInfo, sampleLot, lotData, syncProductList);
			}
		}
		
		//getDummyUseCountLimit yueke 20200831
		String dummyUseCountLimit=CommonUtil.getEnumDefValueStringByEnumName("DummyUseCountLimit");
		
		//Batch query of ConsumableSpec data through crateNameList information  
		Set crateNameSet = new TreeSet<>();
		for(Product productData : productList )
		{
			crateNameSet.add(CommonUtil.getValue(productData.getUdfs(), "CRATENAME"));
		}
		
		Map<String,ConsumableSpec> crateSpecDataMap = new HashMap<>();
		if(crateNameSet.size()>0)
		{
			crateSpecDataMap = CommonUtil.generateConsumableSpecDataMap(new ArrayList(crateNameSet));
		}

		List<Element> productListElement = new ArrayList<Element>();
		
		//2021-05-14 ghhan Update Make ChamberList
		List<Map<String, Object>> getChamberProductDataList = this.getChamberProduct(machineData, lotData, productList);
		
		for (Product productData : productList)
		{
			String oriMachineRecipeName = machineRecipeName;
			String crateName = CommonUtil.getValue(productData.getUdfs(), "CRATENAME");
			
			String productVendor = "";
			String productThickness = "";
			String productSize = "";
			
			// DP box data
			if (crateSpecDataMap.keySet().contains(crateName))
			{
				productVendor = CommonUtil.getValue(crateSpecDataMap.get(crateName).getUdfs(), "GLASSVENDOR");
				productThickness = CommonUtil.getValue(crateSpecDataMap.get(crateName).getUdfs(), "GLASSTHICKNESS");
				productSize = CommonUtil.getValue(crateSpecDataMap.get(crateName).getUdfs(), "GLASSSIZE");
			}
			else
			{
				// Check whether the Consumable data or ConsumableSpec data are registered
				ConsumableSpec consumableSpecData = CommonUtil.getConsumableSpec(crateName);
				productVendor = CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSVENDOR");
				productThickness = CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSTHICKNESS");
				productSize = CommonUtil.getValue(consumableSpecData.getUdfs(), "GLASSSIZE");
			}

			String samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
			String turnDegree = "";
			String flowModeValue = "";

			// Sampling
			if (isFirstGlass)
			{
				if (sortJobList.size() > 0 && operationData.getDetailProcessOperationType().indexOf("SORT") > -1)
				{
					samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSortJobFlag(productData, sortJobList);
					turnDegree = MESLotServiceProxy.getLotServiceUtil().getTurnDegree(productData, sortJobList);
				}
				else if (CommonUtil.equalsIn(operationData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().SORT_OLEDtoTP, GenericServiceProxy.getConstantMap().SORT_TPtoOLED)) // Mantis-0000389
				{
					if (CommonUtil.equalsIn(machineData.getUdfs().get("OPERATIONMODE"), GenericServiceProxy.getConstantMap().SORT_OPERATIONMODE_MERGE,
							GenericServiceProxy.getConstantMap().SORT_OPERATIONMODE_SPLIT))
					{
						// Normal
						samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSelectionFlag(abortFlag, productData);

						if (CommonUtil.equalsIn(productData.getProductGrade(), GenericServiceProxy.getConstantMap().ProductGrade_S, GenericServiceProxy.getConstantMap().ProductGrade_R))
							samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
					}
					else if (sortJobList.size() > 0)
					{
						// SortJob
						samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSortJobFlag(productData, sortJobList);
						turnDegree = MESLotServiceProxy.getLotServiceUtil().getTurnDegree(productData, sortJobList);
					}
					else
					{
						samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
					}
				}
				else if (StringUtils.equals(productData.getLotName(), lotData.getKey().getLotName()))
				{
					if (StringUtils.equals(lotData.getUdfs().get("FIRSTGLASSALL"), "Y"))
					{
						if ((flowData.getProcessFlowType().equals("Inspection") || flowData.getProcessFlowType().equals("Sample")))
						{
							samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSamplingFlag(productData, sampleLot,abortFlag);
						}
						else if (StringUtils.equals(flowData.getProcessFlowType(), "Rework") || StringUtils.equals(flowData.getProcessFlowType(), "Strip"))
						{
							if (StringUtils.equals(productData.getUdfs().get("REWORKFLAG"), "Y")) 
							{
								if (sampleLot == null || sampleLot.size() == 0)
								{
									samplingFlag = "N";

								}
								else
								{
									samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSelectionFlag(abortFlag, productData);

									if ("Y".equals(samplingFlag))
									{
										log.info(String.format("Product [%s] Sampling Flag is Y in ReworkFlow.Next check sampling information is registered.", productData.getKey().getProductName()));
										samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSamplingFlag(productData, sampleLot);
										log.info(String.format("Product [%s] final Sampling Flag is Y in ReworkFlow.", productData.getKey().getProductName()));
									}
								}
							}
							else
							{
								samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
							}
						}
						else
						{
							samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSelectionFlag(abortFlag, productData);
						}
					}
					else
					{
						if (StringUtils.equals(flowData.getProcessFlowType(), "Rework") || StringUtils.equals(flowData.getProcessFlowType(), "Strip"))
						{
							/*if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_R)
									|| productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_N))
							{*/
							if (StringUtils.equals(productData.getUdfs().get("REWORKFLAG"), "Y")) 
							{
								if (sampleLot == null || sampleLot.size() == 0)
								{
									samplingFlag = "N";

								}
								else
								{
									samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSelectionFlag(abortFlag, productData);

									if ("Y".equals(samplingFlag))
									{
										log.info(String.format("Product [%s] Sampling Flag is Y in ReworkFlow.Next check sampling information is registered.", productData.getKey().getProductName()));
										samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSamplingFlag(productData, sampleLot);
										log.info(String.format("Product [%s] final Sampling Flag is Y in ReworkFlow.", productData.getKey().getProductName()));
									}
								}
							}
							else
							{
								samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
							}
						}
						else
						{
							samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSelectionFlag(abortFlag, productData);
						}
					}
				}
				else
				{
					samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
				}
			}
			else if ((flowData.getProcessFlowType().equals("Inspection") || flowData.getProcessFlowType().equals("Sample")) && !flowData.getProcessFlowType().equals("MQC"))
			{
				if (StringUtils.equals(productData.getLotName(), lotData.getKey().getLotName()))
				{
					if (operationData.getDetailProcessOperationType().equals("RP"))
					{
						boolean skipFlag = false;
						List<Map<String, Object>> allRepairFlows = CommonUtil.getEnumDefValueByEnumName("ArrayReviewStationSampleSkipFlow");
						if (allRepairFlows.size() > 0)
						{
							for (Map<String, Object> allRepairFlow : allRepairFlows)
							{
								if (ConvertUtil.getMapValueByName(allRepairFlow, "ENUMVALUE").equalsIgnoreCase(lotData.getProcessFlowName()))
								{
									skipFlag = true;
								}
							}
						}
						if (skipFlag)
						{
							samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
						}
						else
						{
							if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_P))
							{
								samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
							}
							else
							{
								samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
							}
						}
					}
					else
					{
						if (!flowData.getProcessFlowType().equals("MQC"))
						{
							samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSamplingFlag(productData, sampleLot,abortFlag);
						}
						
						if (StringUtils.equals(productData.getUdfs().get("DUMMYGLASSFLAG"), "Y"))
							samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
					}
				}
			}
			else if (StringUtils.equals(lotData.getFactoryName(), "OLED") && StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_EVA))
			{
				if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_N))
				{
					throw new CustomException("LOT-9067", productData.getKey().getProductName());
				}

				if (StringUtils.equals(productData.getLotName(), lotData.getKey().getLotName()))
				{
					// Inline Case
					InlineSampleProduct inlineSampleProduct = null;

					try
					{
						inlineSampleProduct = ExtendedObjectProxy.getInlineSampleProductService().selectByKey(false,
								new Object[] { productData.getKey().getProductName(), productData.getLotName(), productData.getFactoryName(), productData.getProductSpecName(),
										productData.getProductSpecVersion(), productData.getProcessFlowName(), productData.getProcessFlowVersion(), productData.getProcessOperationName(),
										productData.getProcessOperationVersion(), machineData.getKey().getMachineName() });
					}
					catch (Exception e)
					{
						log.info("No InlineSampleProduct Data");
					}

					// Check InlineSampleProduct to set InspectionFlag by POSInlineSample
					if (inlineSampleProduct == null || StringUtils.isEmpty(inlineSampleProduct.getInspectionFlag()))
					{
						inlineSampleProduct = setInlineSampleProduct(inlineSampleProduct, productData, lotData, machineData.getKey().getMachineName());
					}

					if (StringUtils.equals(flowData.getProcessFlowType(), "MQC") || StringUtils.equals(flowData.getProcessFlowType(), "MQCRecycle"))
					{
						// get sample lot position
						if (MQCSampleSlotList.size() > 0)
						{
							for (Object position : MQCSampleSlotList)
							{
								if (productData.getPosition() == Long.parseLong(position.toString()))
								{
									if (inlineSampleProduct != null)
									{
										flowModeValue = inlineSampleProduct.getInspectionFlag();
									}

									samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSelectionFlag(abortFlag, productData);
									machineRecipeName = getMQCMachineRecipeName(mqcLot, machineData.getKey().getMachineName(), productData.getKey().getProductName(), oriMachineRecipeName);
									break;
								}
								samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
							}
						}
						else
						{
							samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
						}
					}
					// End
					// normal Rework
					else
					{
						if (inlineSampleProduct != null)
						{
							flowModeValue = inlineSampleProduct.getInspectionFlag();
							samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
						}
						else
						{
							samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
						}

						samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSelectionFlag(abortFlag, productData);
					}
				}
			}
			else
			{
				if (StringUtils.equals(productData.getLotName(), lotData.getKey().getLotName()))
				{
					// make SampleFlag
					if (operationData.getDetailProcessOperationType().indexOf("SORT") > -1) // Sort is always depended on SortJob.
					{
						samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSortJobFlag(productData, sortJobList);
						turnDegree = MESLotServiceProxy.getLotServiceUtil().getTurnDegree(productData, sortJobList);
					}
					else if (CommonUtil.equalsIn(operationData.getDetailProcessOperationType(), GenericServiceProxy.getConstantMap().SORT_OLEDtoTP, GenericServiceProxy.getConstantMap().SORT_TPtoOLED)) // Mantis-0000389
					{
						if (CommonUtil.equalsIn(machineData.getUdfs().get("OPERATIONMODE"), GenericServiceProxy.getConstantMap().SORT_OPERATIONMODE_MERGE,
								GenericServiceProxy.getConstantMap().SORT_OPERATIONMODE_SPLIT))
						{
							// Normal
							samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSelectionFlag(abortFlag, productData);

							if (CommonUtil.equalsIn(productData.getProductGrade(), GenericServiceProxy.getConstantMap().ProductGrade_S, GenericServiceProxy.getConstantMap().ProductGrade_R))
								samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
						}
						else
						{
							// SortJob
							samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSortJobFlag(productData, sortJobList);
							turnDegree = MESLotServiceProxy.getLotServiceUtil().getTurnDegree(productData, sortJobList);
						}
					}
					else
					// Normal Case
					{
						// Glass selection
						if (operationData.getDetailProcessOperationType().equals("RP"))
						{
							// Repair
							boolean skipFlag = false;
							List<Map<String, Object>> allRepairFlows = CommonUtil.getEnumDefValueByEnumName("ArrayReviewStationSampleSkipFlow");
							if (allRepairFlows.size() > 0)
							{
								for (Map<String, Object> allRepairFlow : allRepairFlows)
								{
									if (ConvertUtil.getMapValueByName(allRepairFlow, "ENUMVALUE").equalsIgnoreCase(lotData.getProcessFlowName()))
									{
										skipFlag = true;
									}
								}
							}
							if (skipFlag)
							{
								samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
							}
							else
							{
								if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_P))
								{
									samplingFlag = GenericServiceProxy.getConstantMap().Flag_Y;
								}
								else
								{
									samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
								}
							}
						}
						else if (flowData.getProcessFlowType().equals("Rework") || StringUtils.equals(flowData.getProcessFlowType(), "Strip")
									|| StringUtils.equals(flowData.getProcessFlowType(), "MQC") || StringUtils.equals(flowData.getProcessFlowType(), "MQCRecycle"))
						{
							// MQC Rework
							// Start
							if (StringUtils.equals(flowData.getProcessFlowType(), "MQC") || StringUtils.equals(flowData.getProcessFlowType(), "MQCRecycle"))
							{
								// get sample lot position
								if (MQCSampleSlotList.size() > 0)
								{
									for (Object position : MQCSampleSlotList)
									{
										if (productData.getPosition() == Long.parseLong(position.toString()))
										{
											samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSelectionFlag(abortFlag, productData);
											machineRecipeName = getMQCMachineRecipeName(mqcLot, machineData.getKey().getMachineName(), productData.getKey().getProductName(), oriMachineRecipeName);
											break;
										}
										samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
									}
								}
								else
								{
									samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
								}
							}
							// End
							// normal Rework
							else
							{
								if (sampleLot!=null&&sampleLot.size() > 0)
								{
									if (StringUtils.equals(operationData.getProcessOperationType(), "Inspection"))
									{
										samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSamplingFlag(productData, sampleLot,abortFlag);
									}
									else
									{
										if (StringUtils.equals(productData.getUdfs().get("REWORKFLAG"), "Y")) //if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_R))
										{
											samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSelectionFlag(abortFlag, productData);

											if ("Y".equals(samplingFlag))
											{
												log.info(String.format("Product [%s] Sampling Flag is Y in ReworkFlow.Next check sampling information is registered.", productData.getKey().getProductName()));
												samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSamplingFlag(productData, sampleLot);
												log.info(String.format("Product [%s] final Sampling Flag is Y in ReworkFlow.", productData.getKey().getProductName()));
											}

										}
										else if ((StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_HalfCut) && productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_N)))
										{
											samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSelectionFlag(abortFlag, productData);
										}
										else
										{
											samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
										}
									}
								}
								else
								{

									if ((StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_HalfCut) && productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_N)))
									{
										samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSelectionFlag(abortFlag, productData);
									}
									else
									{
										samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
									}
								}
							}
						}
						else
						// Normal Case
						{
							samplingFlag = MESLotServiceProxy.getLotServiceUtil().getSelectionFlag(abortFlag, productData);

							if (productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_S)
									|| productData.getProductGrade().equals(GenericServiceProxy.getConstantMap().ProductGrade_R))
								samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
						}
					}
				}
			}

			//PhotoOffset
			String offsetResult = "";
			
			if(isPhoto)
			{
				if(machineData.getFactoryName().equals("TP"))
				{
					String productOffset = "";
					
					if(productData.getUdfs().get("OFFSET") != null)
					{
						productOffset = productData.getUdfs().get("OFFSET").toString();
					}
					
					if((operationData.getUdfs().get("LAYERNAME").toString().equals("PEP1") || operationData.getUdfs().get("LAYERNAME").toString().equals("PEP0")) && StringUtil.isEmpty(productOffset))
					{
						machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(),
								lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), 
								lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineData.getKey().getMachineName(), true);
						
						if(StringUtil.isEmpty(machineRecipeName))
						{
							throw new CustomException("RMS-0005");
						}
					}
					else if(((!operationData.getUdfs().get("LAYERNAME").toString().equals("PEP1")) && (!operationData.getUdfs().get("LAYERNAME").toString().equals("PEP0"))) && StringUtil.isEmpty(productOffset))
					{
						throw new CustomException("RMS-0005");
					}
					else if(((!operationData.getUdfs().get("LAYERNAME").toString().equals("PEP1")) && (!operationData.getUdfs().get("LAYERNAME").toString().equals("PEP0"))) && StringUtil.isNotEmpty(productOffset))
					{
						boolean isSame = true;
						
						for(int i = 1 ; i < productList.size() ; i++)
						{
							if(!productList.get(i).getUdfs().get("OFFSET").equals(productOffset))
							{
								isSame = false;
								break;
							}
						}
						
						if(isSame)
						{
							TPOffsetAlignInfo offsetInfo = ExtendedObjectProxy.getTPOffsetAlignInfoService().selectByKey(false, new Object[]{productOffset, operationData.getKey().getProcessOperationName(), operationData.getKey().getProcessOperationVersion(), machineData.getKey().getMachineName()});
						
							machineRecipeName = offsetInfo.getRecipeName();
						}
						else
						{
							throw new CustomException("RMS-0005");
						}
					}
					offsetResult = machineSpecData.getUdfs().get("OFFSETID").toString();
				}
				else
				{
					offsetResult = getPhotoOffset(operationData, productData, machineData, machineRecipeName);
					CommonValidation.checkFirstGlassLotOffset(lotData, machineData, offsetResult);
				}
			}
				

			// DoubleRun Operation Check
			//shield by yueke 20201111
			/*
			if (samplingFlag.equals("Y"))
			{
				log.info("Main Operation Check");
				ProcessOperationSpecKey specKey = new ProcessOperationSpecKey(lotData.getFactoryName(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());
				ProcessOperationSpec afterOperSpec = ProcessOperationSpecServiceProxy.getProcessOperationSpecService().selectByKey(specKey);

				log.info("ProcessOperationSpecData");
				log.info("ProcessOperation - " + afterOperSpec.getKey().getProcessOperationName());
				eventLog.info("ProcessOperationType - " + afterOperSpec.getProcessOperationType());
				eventLog.info("IsMainOperation - " + afterOperSpec.getUdfs().get("ISMAINOPERATION").toString());
				eventLog.info("ProcessOperationGroup - " + afterOperSpec.getProcessOperationGroup());
				if (afterOperSpec.getProcessOperationType().equals("Production") && afterOperSpec.getUdfs().get("ISMAINOPERATION").toString().equals("Y")
						&& afterOperSpec.getProcessOperationGroup().equals("Normal"))
				{
					samplingFlag = MESLotServiceProxy.getLotServiceUtil().doubleRunOperationCheck(productData, lotData, samplingFlag);
				}
			}*/

			// If Lot's ProductionType is "D", download the DummyType of ProductSpec.
			String dummyType = StringUtils.EMPTY;
			if("D".equals( productSpecData.getProductionType()))
			{
				dummyType = productSpecData.getUdfs().get("CATEGORYTYPE");
				
				//DummyProduct SlotSel Change
				if(StringUtil.equals(productData.getUdfs().get("DUMMYGLASSFLAG").toString(), GenericServiceProxy.getConstantMap().Flag_Y))
				{
					samplingFlag = GenericServiceProxy.getConstantMap().Flag_N;
				}
			}
	      
			//2021-05-14 ghhan Update Make ChamberList 
			List<Map<String, Object>> chamberHistListByProduct = new ArrayList<Map<String, Object>>();
			
			if(StringUtil.equals(machineData.getMachineGroupName(), "IFI"))
			{
				chamberHistListByProduct = getChamberProductDataList;
			}
			else
			{
				for(Map<String, Object> chamberProductData : getChamberProductDataList)
				{
					if(StringUtil.equals(chamberProductData.get("PRODUCTNAME").toString(), productData.getKey().getProductName()))
					{
						chamberHistListByProduct.add(chamberProductData);
					}
				}
			}
			
			/*if(!StringUtil.in( String.valueOf(productData.getPosition() ) , "2","3","4") )
				samplingFlag ="N";	
			else 
				samplingFlag ="Y";*/  //TEST BY CJL
			
			////2021-05-14 ghhan Update Make ChamberList 
			Element productElement = this.generateProductElement(machineData,lotData, productData, productSpecData.getProductionType(), productThickness, productSize, productVendor,
					machineRecipeName, productData.getUdfs().get("EXPOSURERECIPENAME").toString(), samplingFlag, workOrderName, turnDegree, flowModeValue, productSpecData.getUdfs().get("GLASSTYPE"),
					reworkType, maskSpecName, dummyType, productData.getUdfs().get("LASTPHOTOMACHINENAME").toString(), offsetResult,dummyUseCountLimit, chamberHistListByProduct);

			productListElement.add(productElement);
						
			if (StringUtils.equals(flowData.getProcessFlowType(), "MQC") || StringUtils.equals(flowData.getProcessFlowType(), "MQCRecycle"))
			{
				machineRecipeName = oriMachineRecipeName;
			}
		}

		return productListElement;
	}
	
	//2021-05-14 ghhan Update Make ChamberList 
	private Element generateProductElement(Machine machineData,Lot lotData, Product productData, String productionType, String productThickness, String productSize, String productVendor, String machineRecipeName,
			String exposureRecipe, String samplingFlag, String workOrderName, String turnDegree, String flowModeValue, String glassType, String reworkType, String maskSpecName, String dummyType, String photoMachineName, String offset,String dummyUseCountLimit, List<Map<String, Object>> getChamberProductData) throws CustomException
	{
		
		Element productElement = new Element("PRODUCT");

		JdomUtils.addElement(productElement, "LOTNAME", productData.getLotName());
		JdomUtils.addElement(productElement, "PRODUCTNAME", productData.getKey().getProductName());
		JdomUtils.addElement(productElement, "PRODUCTTYPE", productData.getProductType());
		JdomUtils.addElement(productElement, "PROCESSFLOWNAME", productData.getProcessFlowName());
		JdomUtils.addElement(productElement, "PROCESSFLOWVERSION", productData.getProcessFlowVersion());
		JdomUtils.addElement(productElement, "PROCESSOPERATIONNAME", productData.getProcessOperationName());
		JdomUtils.addElement(productElement, "PROCESSOPERATIONVERSION", productData.getProcessOperationVersion());
		JdomUtils.addElement(productElement, "POSITION", String.valueOf(productData.getPosition()));
		JdomUtils.addElement(productElement, "SLOTPOSITION", productData.getUdfs().get("SLOTPOSITION"));
		JdomUtils.addElement(productElement, "PRODUCTSPECNAME", productData.getProductSpecName());
		JdomUtils.addElement(productElement, "PRODUCTSPECVERSION", productData.getProductSpecVersion());
		
		if(productData.getUdfs().get("DUMMYGLASSFLAG") != null && productData.getUdfs().get("DUMMYGLASSFLAG").equals("Y"))
			JdomUtils.addElement(productElement, "PRODUCTIONTYPE", "D");
		else 
			JdomUtils.addElement(productElement, "PRODUCTIONTYPE", productionType);
		
		JdomUtils.addElement(productElement, "PRODUCTJUDGE", productData.getProductGrade());
		JdomUtils.addElement(productElement, "PRODUCTGRADE", getProductDetailGrade(machineData, productData));
		JdomUtils.addElement(productElement, "HALFPRODUCTJUDGE", getHalfProductJudge( machineData, productData));
		JdomUtils.addElement(productElement, "SUBPRODUCTJUDGES", getSubProductJudges(machineData, productData));
		JdomUtils.addElement(productElement, "SUBPRODUCTGRADES", productData.getSubProductGrades1());
		JdomUtils.addElement(productElement, "PRODUCTTHICKNESS", productThickness);
		JdomUtils.addElement(productElement, "CRATENAME", CommonUtil.getValue(productData.getUdfs(), "CRATENAME"));
		JdomUtils.addElement(productElement, "PRODUCTSIZE", productSize);
		JdomUtils.addElement(productElement, "GLASSMAKER", productVendor);
		JdomUtils.addElement(productElement, "PRODUCTRECIPE", machineRecipeName);
		JdomUtils.addElement(productElement, "REWORKTYPE", reworkType);
		JdomUtils.addElement(productElement, "REWORKCOUNT", String.valueOf(productData.getReworkCount()));
		JdomUtils.addElement(productElement, "PROCESSINGFLAG", ""/* processingFlag */);
		JdomUtils.addElement(productElement, "SAMPLINGFLAG", samplingFlag);
		JdomUtils.addElement(productElement, "EXPOSURERECIPENAME", exposureRecipe);
		JdomUtils.addElement(productElement, "PHOTOMACHINENAME", photoMachineName);
		JdomUtils.addElement(productElement, "TURNDEGREE", turnDegree);
		JdomUtils.addElement(productElement, "FLOWMODEVALUE", flowModeValue);
		JdomUtils.addElement(productElement, "BEFOREPROCESSMACHINE", productData.getUdfs().get("MAINMACHINENAME"));
		JdomUtils.addElement(productElement, "GLASSTYPE", glassType);
		JdomUtils.addElement(productElement, "MASKSPECNAME", maskSpecName);
		JdomUtils.addElement(productElement, "DUMMYTYPE", dummyType);
		
		try
		{
			if(productionType.equals("D")&&machineData.getMachineGroupName().equals(GenericServiceProxy.getConstantMap().MachineGroup_Oven))
			{
				String dummyUsedCount=productData.getUdfs().get("OVENDUMMYCOUNT").toString();
				if(StringUtil.isEmpty(dummyUsedCount))
				{
					JdomUtils.addElement(productElement, "DUMMYUSECOUNT","0");
					dummyUsedCount="0";
				}
				else 
				{
					JdomUtils.addElement(productElement, "DUMMYUSECOUNT",dummyUsedCount);
				}
	            if(StringUtil.isEmpty(dummyUseCountLimit)||Integer.parseInt(dummyUsedCount)>=Integer.parseInt(dummyUseCountLimit))
	            {
	            	throw new CustomException("LOT-1015", productData.getKey().getProductName()); 
	            }
	            else
	            {
					JdomUtils.addElement(productElement, "DUMMYUSELIMIT", dummyUseCountLimit);
	            }
			}
			else
			{
				JdomUtils.addElement(productElement, "DUMMYUSELIMIT", "");
				JdomUtils.addElement(productElement, "DUMMYUSECOUNT", "");
			}
		}
		catch(CustomException e)
		{
			e.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		JdomUtils.addElement(productElement, "OFFSET", offset);
		
		Element mainChamberListE = new Element("MAINCHAMBERLIST");
		
		if(getChamberProductData.size() > 0)
		{
			JdomUtils.addElement(productElement, "PRE_STEP_ID", getChamberProductData.get(0).get("PROCESSOPERATIONNAME").toString());
			JdomUtils.addElement(productElement, "PRE_EQUIP_ID", getChamberProductData.get(0).get("MACHINENAME").toString());
			if(getChamberProductData.get(0).get("MACHINERECIPENAME")!=null&&StringUtils.isNotEmpty(getChamberProductData.get(0).get("MACHINERECIPENAME").toString()))
			{
				JdomUtils.addElement(productElement, "PRE_RECIPE_ID", getChamberProductData.get(0).get("MACHINERECIPENAME").toString());
			}
			else
			{
				JdomUtils.addElement(productElement, "PRE_RECIPE_ID","");
			}
			
			for(Map<String, Object> chamberP : getChamberProductData)
			{
				Element mainChamberE = new Element("MAINCHAMBER");
				JdomUtils.addElement(mainChamberE, "PRE_SUB_UNIT_ID", ConvertUtil.getMapValueByName(chamberP,"MATERIALLOCATIONNAME"));
				mainChamberListE.addContent(mainChamberE);
			}
		}
		else
		{
			JdomUtils.addElement(productElement, "PRE_STEP_ID", "");
			JdomUtils.addElement(productElement, "PRE_EQUIP_ID", "");
			JdomUtils.addElement(productElement, "PRE_RECIPE_ID", "");
			

			Element mainChamberE = new Element("MAINCHAMBER");
			JdomUtils.addElement(mainChamberE, "PRE_SUB_UNIT_ID", "");
			mainChamberListE.addContent(mainChamberE);
		}
		
		productElement.addContent(mainChamberListE);

		return productElement;
	}


	/**
	 * 
	 * Create ProductElement List.
	 * 
	 * @see 2020.01.08 aim_dhko Add Argument/Element DummyType.
	 * 
	 * @author aim_dhko
	 * @return 
	 */
	/*
	private Element generateProductElement(String machinename, Product productData, String productionType, String productThickness, String productSize, String productVendor, String machineRecipeName,
			String exposureRecipe, String samplingFlag, String workOrderName, String turnDegree, String flowModeValue, String glassType, String reworkType, String maskSpecName, String dummyType, String photoMachineName, String offset,String dummyUseCountLimit) throws CustomException
	{
		
		Machine machineData = new Machine();
		
		try
		{
			machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machinename);
		}
		catch (CustomException e)
		{
			e.printStackTrace();
		}
		
		Element productElement = new Element("PRODUCT");

		JdomUtils.addElement(productElement, "LOTNAME", productData.getLotName());
		JdomUtils.addElement(productElement, "PRODUCTNAME", productData.getKey().getProductName());
		JdomUtils.addElement(productElement, "PRODUCTTYPE", productData.getProductType());
		JdomUtils.addElement(productElement, "PROCESSFLOWNAME", productData.getProcessFlowName());
		JdomUtils.addElement(productElement, "PROCESSFLOWVERSION", productData.getProcessFlowVersion());
		JdomUtils.addElement(productElement, "PROCESSOPERATIONNAME", productData.getProcessOperationName());
		JdomUtils.addElement(productElement, "PROCESSOPERATIONVERSION", productData.getProcessOperationVersion());
		JdomUtils.addElement(productElement, "POSITION", String.valueOf(productData.getPosition()));
		JdomUtils.addElement(productElement, "SLOTPOSITION", productData.getUdfs().get("SLOTPOSITION"));
		JdomUtils.addElement(productElement, "PRODUCTSPECNAME", productData.getProductSpecName());
		JdomUtils.addElement(productElement, "PRODUCTSPECVERSION", productData.getProductSpecVersion());
		JdomUtils.addElement(productElement, "PRODUCTIONTYPE", productionType);
		JdomUtils.addElement(productElement, "PRODUCTJUDGE", productData.getProductGrade());
		JdomUtils.addElement(productElement, "PRODUCTGRADE", getProductDetailGrade(machinename, productData));
		JdomUtils.addElement(productElement, "HALFPRODUCTJUDGE", getHalfProductJudge( machineData, productData));
		JdomUtils.addElement(productElement, "SUBPRODUCTJUDGES", getSubProductJudges(machinename, productData));
		JdomUtils.addElement(productElement, "SUBPRODUCTGRADES", productData.getSubProductGrades1());
		JdomUtils.addElement(productElement, "PRODUCTTHICKNESS", productThickness);
		JdomUtils.addElement(productElement, "CRATENAME", CommonUtil.getValue(productData.getUdfs(), "CRATENAME"));
		JdomUtils.addElement(productElement, "PRODUCTSIZE", productSize);
		JdomUtils.addElement(productElement, "GLASSMAKER", productVendor);
		JdomUtils.addElement(productElement, "PRODUCTRECIPE", machineRecipeName);
		JdomUtils.addElement(productElement, "REWORKTYPE", reworkType);
		JdomUtils.addElement(productElement, "REWORKCOUNT", String.valueOf(productData.getReworkCount()));
		JdomUtils.addElement(productElement, "PROCESSINGFLAG", "");
		JdomUtils.addElement(productElement, "SAMPLINGFLAG", samplingFlag);
		JdomUtils.addElement(productElement, "EXPOSURERECIPENAME", exposureRecipe);
		JdomUtils.addElement(productElement, "PHOTOMACHINENAME", photoMachineName);
		JdomUtils.addElement(productElement, "TURNDEGREE", turnDegree);
		JdomUtils.addElement(productElement, "FLOWMODEVALUE", flowModeValue);
		JdomUtils.addElement(productElement, "BEFOREPROCESSMACHINE", productData.getUdfs().get("MAINMACHINENAME"));
		JdomUtils.addElement(productElement, "GLASSTYPE", glassType);
		JdomUtils.addElement(productElement, "MASKSPECNAME", maskSpecName);
		JdomUtils.addElement(productElement, "DUMMYTYPE", dummyType);
		
		try
		{
			if(productionType.equals("D")&&machineData.getMachineGroupName().equals(GenericServiceProxy.getConstantMap().MachineGroup_Oven))
			{
				String dummyUsedCount=productData.getUdfs().get("DUMMYUSEDCOUNT").toString();
				if(StringUtil.isEmpty(dummyUsedCount))
				{
					JdomUtils.addElement(productElement, "DUMMYUSEDCOUNT","0");
					dummyUsedCount="0";
				}
				else 
				{
					JdomUtils.addElement(productElement, "DUMMYUSEDCOUNT",dummyUsedCount);
				}
	            if(StringUtil.isEmpty(dummyUseCountLimit)||Integer.parseInt(dummyUsedCount)>=Integer.parseInt(dummyUseCountLimit))
	            {
	            	throw new CustomException("LOT-1015", productData.getKey().getProductName()); 
	            }
	            else
	            {
					JdomUtils.addElement(productElement, "DUMMYUSELIMIT", dummyUseCountLimit);
	            }
			}
			else
			{
				JdomUtils.addElement(productElement, "DUMMYUSELIMIT", "");
				JdomUtils.addElement(productElement, "DUMMYUSECOUNT", "");
			}
		}
		catch(CustomException e)
		{
			e.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		JdomUtils.addElement(productElement, "OFFSET", offset);
		
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(productData.getLotName());
		
		List<Map<String, Object>> getChamberProductData = this.getChamberProduct(machineData,lotData, productData.getKey().getProductName());
		
		Element mainChamberListE = new Element("MAINCHAMBERLIST");
		
		if(getChamberProductData.size() > 0)
		{
			JdomUtils.addElement(productElement, "PRE_STEP_ID", getChamberProductData.get(0).get("PROCESSOPERATIONNAME").toString());
			JdomUtils.addElement(productElement, "PRE_EQUIP_ID", getChamberProductData.get(0).get("MACHINENAME").toString());
			if(getChamberProductData.get(0).get("MACHINERECIPENAME")!=null&&StringUtils.isNotEmpty(getChamberProductData.get(0).get("MACHINERECIPENAME").toString()))
			{
				JdomUtils.addElement(productElement, "PRE_RECIPE_ID", getChamberProductData.get(0).get("MACHINERECIPENAME").toString());
			}
			else
			{
				JdomUtils.addElement(productElement, "PRE_RECIPE_ID","");
			}
			
			for(Map<String, Object> chamberP : getChamberProductData)
			{
				Element mainChamberE = new Element("MAINCHAMBER");
				JdomUtils.addElement(mainChamberE, "PRE_SUB_UNIT_ID", ConvertUtil.getMapValueByName(chamberP,"MATERIALLOCATIONNAME"));
				mainChamberListE.addContent(mainChamberE);
			}
		}
		else
		{
			JdomUtils.addElement(productElement, "PRE_STEP_ID", "");
			JdomUtils.addElement(productElement, "PRE_EQUIP_ID", "");
			JdomUtils.addElement(productElement, "PRE_RECIPE_ID", "");
			

			Element mainChamberE = new Element("MAINCHAMBER");
			JdomUtils.addElement(mainChamberE, "PRE_SUB_UNIT_ID", "");
			mainChamberListE.addContent(mainChamberE);
		}
		
		productElement.addContent(mainChamberListE);

		return productElement;
	}
	*/
	
	private String getProductDetailGrade(Machine machineData, Product productData)
	{
		String getProductDetailGrade = "A";

		if (StringUtils.indexOf(StringUtils.upperCase(machineData.getMachineGroupName()), GenericServiceProxy.getConstantMap().MachineGroup_Sorter) >= 0)
		{
			getProductDetailGrade = productData.getUdfs().get("DETAILGRADE").isEmpty() ? "A" : productData.getUdfs().get("DETAILGRADE");
		}

		return getProductDetailGrade;
	}

	private String getProductDetailGrade(String machineName, Product productData)
	{
		String getProductDetailGrade = "A";
		Machine machineData;
		try
		{
			machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

			if (StringUtils.indexOf(StringUtils.upperCase(machineData.getMachineGroupName()), GenericServiceProxy.getConstantMap().MachineGroup_Sorter) >= 0)
			{
				getProductDetailGrade = productData.getUdfs().get("DETAILGRADE").isEmpty() ? "A" : productData.getUdfs().get("DETAILGRADE");
			}
		}
		catch (CustomException e)
		{
			e.printStackTrace();
		}
		return getProductDetailGrade;
	}
	
	private String getSubProductJudges(Machine machineData, Product productData)
	{
		String getSubProductJudges = "";

		if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_HalfCut)
				&& StringUtils.equals(productData.getProductType(), GenericServiceProxy.getConstantMap().ProductType_Sheet)
				&& StringUtils.equals(CommonUtil.getEnumDefValueStringByEnumName("FLAG_HALFCUT_SUBPRODUCTJUDGE"), "Y"))
		{
			getSubProductJudges = getHalfProductJudge(machineData, productData);
		}
		else
		{
			getSubProductJudges = productData.getSubProductGrades1();
			if (getSubProductJudges.isEmpty())
			{
				getSubProductJudges = StringUtils.repeat("G", (int) productData.getSubProductQuantity1());
			}
		}

		return getSubProductJudges;
	}

	private String getSubProductJudges(String machineName, Product productData)
	{
		String getSubProductJudges = "";

		Machine machineData = new Machine();
		
		try
		{
			machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		}
		catch (CustomException e)
		{
			e.printStackTrace();
		}

		if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_HalfCut)
				&& StringUtils.equals(productData.getProductType(), GenericServiceProxy.getConstantMap().ProductType_Sheet)
				&& StringUtils.equals(CommonUtil.getEnumDefValueStringByEnumName("FLAG_HALFCUT_SUBPRODUCTJUDGE"), "Y"))
		{
			getSubProductJudges = getHalfProductJudge(machineData, productData);
		}
		else
		{
			getSubProductJudges = productData.getSubProductGrades1();
			if (getSubProductJudges.isEmpty())
			{
				getSubProductJudges = StringUtils.repeat("G", (int) productData.getSubProductQuantity1());
			}
		}

		return getSubProductJudges;
	}

	private String getHalfProductJudge(Machine machineData, Product productData)
	{
		StringBuilder halfProductJudge = new StringBuilder();

		if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_HalfCut)
				&& StringUtils.equals(productData.getProductType(), GenericServiceProxy.getConstantMap().ProductType_Sheet))
		{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT GLASSNAME, GLASSJUDGE,NGFLAG ");
			sql.append("  FROM CT_GLASSJUDGE ");
			sql.append(" WHERE SHEETNAME = :SHEETNAME ");
			sql.append("ORDER BY GLASSNAME ");

			Map<String, String> args = new HashMap<String, String>();
			args.put("SHEETNAME", productData.getKey().getProductName());

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (result.size() > 0)
			{
				for (int i = 0; result.size() > i; i++)
				{
					String glassJudge = ConvertUtil.getMapValueByName(result.get(i), "GLASSJUDGE");
					if(result.get(i).get("NGFLAG")!=null &&StringUtils.equals(result.get(i).get("NGFLAG").toString(), "Y"))
					{
						glassJudge="N";
					}
					halfProductJudge.append(glassJudge);
				}
			}
			else if(productData.getProductionType().equals("M")||productData.getProductionType().equals("D"))
			{
				halfProductJudge.append("GG");
			}

			if (StringUtils.isEmpty(halfProductJudge.toString()) || StringUtils.length(halfProductJudge.toString()) != 2)
			{
				log.info("Not exist Data on CT_GLASSJUDGE");
			}
		}
		else if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_HalfCut)
				&& StringUtils.equals(productData.getProductType(), GenericServiceProxy.getConstantMap().ProductType_Glass))
		{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT GLASSNAME, GLASSJUDGE ");
			sql.append("  FROM CT_GLASSJUDGE ");
			sql.append(" WHERE GLASSNAME = :GLASSNAME ");
			sql.append("ORDER BY GLASSNAME ");

			Map<String, String> args = new HashMap<String, String>();
			args.put("GLASSNAME", productData.getKey().getProductName());

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (result.size() > 0)
			{
				String glassJudge = ConvertUtil.getMapValueByName(result.get(0), "GLASSJUDGE").toString();
				halfProductJudge.append(glassJudge);
			}

			if (StringUtils.isEmpty(halfProductJudge.toString()))
			{
				log.info("Not exist Data on CT_GLASSJUDGE");
			}
		}

		return halfProductJudge.toString();
	}

	private String doGlassSelection(Document doc, Lot lotData, Durable durableData, String slotMap) throws CustomException
	{
		StringBuffer slotMapTemp = new StringBuffer();

		for (long i = 0; i < durableData.getCapacity(); i++)
		{
			slotMapTemp.append(GenericServiceProxy.getConstantMap().PRODUCT_NOT_IN_SLOT);
		}

		List<Element> lstDownloadProduct = SMessageUtil.getSubSequenceItemList(SMessageUtil.getBodyElement(doc), "PRODUCTLIST", false);

		for (Element eleProduct : lstDownloadProduct)
		{
			String sSelection = SMessageUtil.getChildText(eleProduct, "SAMPLINGFLAG", true);
			String sPosition = SMessageUtil.getChildText(eleProduct, "POSITION", true);
			String sSlotPosition = SMessageUtil.getChildText(eleProduct, "SLOTPOSITION", false);

			int position;

			try
			{
				position = Integer.parseInt(sPosition);
			}
			catch (Exception ex)
			{
				position = 0;
			}

			if (durableData.getCapacity() > 30 && StringUtils.isNotEmpty(sSlotPosition))
			{
				if (StringUtils.equals(sSlotPosition, "A"))
				{
					position = (Integer.parseInt(sPosition) * 2) - 1;
				}
				else if (StringUtils.equals(sSlotPosition, "B"))
				{
					position = (Integer.parseInt(sPosition) * 2);
				}
			}

			if (sSelection.equalsIgnoreCase(GenericServiceProxy.getConstantMap().Flag_Y))
			{
				slotMapTemp.replace(position - 1, position, GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT);
			}
			else
			{
				slotMapTemp.replace(position - 1, position, GenericServiceProxy.getConstantMap().PRODUCT_NOT_IN_SLOT);
			}
		}

		eventLog.debug("Completed Slot Selection : " + slotMapTemp.toString());

		// Glass selection validation
		CommonValidation.checkSlotSel(slotMapTemp.toString(), slotMap);

		// Glass selection decision
		SMessageUtil.setBodyItemValue(doc, "SLOTSEL", slotMapTemp.toString());

		SMessageUtil.setBodyItemValue(doc, "PRODUCTQUANTITY", String.valueOf(lstDownloadProduct.size()));
		
		return slotMapTemp.toString();
	}

	private void checkAssignedLotByCst(String carrierName) throws CustomException
	{
		String sql = "SELECT LOTNAME, LOTSTATE FROM LOT WHERE CARRIERNAME = :CARRIERNAME ";
		Map<String, String> args = new HashMap<String, String>();
		args.put("CARRIERNAME", carrierName);

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

		if (result.size() > 0)
		{
			String lotName = ConvertUtil.getMapValueByName(result.get(0), "LOTNAME");

			// Lot[{0}] is assigned to CST[{1}]
			throw new CustomException("CST-0002", lotName, carrierName);
		}
	}

	private String checkMaskSpecName(String factoryName, String productSpecName, String productSpecVersion) throws CustomException
	{
		String maskSpecName = "";
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT P.MASKSPECNAME AS MASKSPECNAME ");
		sql.append("  FROM TPPOLICY T, POSMASKSPEC P ");
		sql.append(" WHERE T.CONDITIONID = P.CONDITIONID ");
		sql.append("   AND T.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND T.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("   AND T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", factoryName);
		args.put("PRODUCTSPECNAME", productSpecName);
		args.put("PRODUCTSPECVERSION", productSpecVersion);

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() == 1)
		{
			maskSpecName = ConvertUtil.getMapValueByName(result.get(0), "MASKSPECNAME");
		}
		else if (result.size() == 0)
		{
			// No MaskSpec assigned for ProductSpec[{0}]
			throw new CustomException("OLEDMASK-0009", productSpecName);
		}
		else if (result.size() > 1)
		{
			List<String> maskSpecList = CommonUtil.makeListBySqlResult(result, "MASKSPECNAME");
			// Several MaskSpec{0} assigned for ProductSpec[{1}]. Only 1 MaskSpec can be assigned.
			throw new CustomException("OLEDMASK-0010", maskSpecList, productSpecName);
		}

		//EVA MaskSpec Download the first two digits
		if(StringUtils.isNotEmpty(maskSpecName))
		{
			maskSpecName = maskSpecName.substring(0,2);
		}
		return maskSpecName;
	}

	private void checkSlotMapInfoByEQP(String machineName, Lot lotData, Durable durableData, String slotMap) throws CustomException
	{
		if (CommonUtil.equalsIn(lotData.getFactoryName(), "TSP", "TP"))
		{
			String sql = "SELECT MACHINENAME FROM MACHINESPEC WHERE MACHINENAME = :MACHINENAME AND TPSLOTCHECK = :TPSLOTCHECK ";

			Map<String, String> args = new HashMap<String, String>();
			args.put("MACHINENAME", machineName);
			args.put("TPSLOTCHECK", "Y");

			List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

			if (result.size() > 0)
			{
				if (durableData.getCapacity() == StringUtils.length(slotMap))
				{
					int startPos = 0;
					int endPos = 2;

					for (int i = 0; i < 30; i++)
					{
						String slotPos = StringUtils.substring(slotMap, startPos, endPos);

						if (slotPos.equals("OX") || slotPos.equals("XO"))
						{
							throw new CustomException("CARRIER-9001", endPos / 2, slotPos, durableData.getKey().getDurableName(), machineName);
						}
						else
						{
							startPos = startPos + 2;
							endPos = endPos + 2;
						}
					}
				}
				else
				{
					throw new CustomException("CARRIER-9004", durableData.getKey().getDurableName());
				}
			}
			else
			{
				log.info("Skip check slot position info : factory [ " + lotData.getFactoryName() + " ], EQP [ " + machineName + " ] ");
			}
		}
		else
		{
			log.info("Skip check slot position info : factory [ " + lotData.getFactoryName() + " ], EQP [ " + machineName + " ] ");
		}
	}

	private List<Map<String, Object>> getMQCProduct(String lotName)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT P.PRODUCTNAME, P.POSITION, E.RECIPENAME, E.MACHINENAME ");
		sql.append("  FROM CT_MQCPLAN M, ");
		sql.append("       CT_MQCPLANDETAIL D, ");
		sql.append("       CT_MQCPLANDETAIL_EXTENDED E, ");
		sql.append("       PRODUCT P ");
		sql.append(" WHERE M.JOBNAME = D.JOBNAME ");
		sql.append("   AND M.JOBNAME = E.JOBNAME ");
		sql.append("   AND M.MQCSTATE = 'Released' ");
		sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME ");
		sql.append("   AND P.PRODUCTSTATE = 'InProduction' ");
		sql.append("   AND M.LOTNAME = :LOTNAME ");
		sql.append("   AND M.LOTNAME = P.LOTNAME ");
		sql.append("   AND M.PROCESSFLOWNAME = P.PROCESSFLOWNAME ");
		sql.append("   AND M.PROCESSFLOWVERSION = P.PROCESSFLOWVERSION ");
		sql.append("   AND D.PROCESSFLOWNAME = E.PROCESSFLOWNAME ");
		sql.append("   AND D.PROCESSFLOWVERSION = E.PROCESSFLOWVERSION ");
		sql.append("   AND D.PROCESSOPERATIONNAME = E.PROCESSOPERATIONNAME ");
		sql.append("   AND D.PROCESSOPERATIONVERSION = E.PROCESSOPERATIONVERSION ");
		sql.append("   AND D.PROCESSFLOWNAME = P.PROCESSFLOWNAME ");
		sql.append("   AND D.PROCESSFLOWVERSION = P.PROCESSFLOWVERSION ");
		sql.append("   AND D.PROCESSOPERATIONNAME = P.PROCESSOPERATIONNAME ");
		sql.append("   AND D.PROCESSOPERATIONVERSION = P.PROCESSOPERATIONVERSION ");
		sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME ");
		sql.append("ORDER BY P.POSITION ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("LOTNAME", lotName);

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		return result;
	}

	private List<Map<String, Object>> getMQCRecycleProduct(String lotName)
	{
		List<Map<String, Object>> result = null;

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT P.PRODUCTNAME, P.POSITION, E.RECIPENAME, E.MACHINENAME  ");
		sql.append("  FROM CT_MQCPLAN M,  ");
		sql.append("       CT_MQCPLANDETAIL D,  ");
		sql.append("       CT_MQCPLANDETAIL_EXTENDED E,  ");
		sql.append("       PRODUCT P  ");
		sql.append(" WHERE M.JOBNAME = D.JOBNAME  ");
		sql.append("   AND M.JOBNAME = E.JOBNAME  ");
		sql.append("   AND M.MQCSTATE = 'Recycling'  ");
		sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME  ");
		sql.append("   AND P.PRODUCTSTATE = 'InProduction'  ");
		sql.append("   AND M.LOTNAME = :LOTNAME  ");
		sql.append("   AND M.LOTNAME = P.LOTNAME  ");
		sql.append("   AND D.PROCESSFLOWNAME = E.PROCESSFLOWNAME  ");
		sql.append("   AND D.PROCESSFLOWVERSION = E.PROCESSFLOWVERSION  ");
		sql.append("   AND D.PROCESSOPERATIONNAME = E.PROCESSOPERATIONNAME  ");
		sql.append("   AND D.PROCESSOPERATIONVERSION = E.PROCESSOPERATIONVERSION  ");
		sql.append("   AND D.PROCESSFLOWNAME = P.PROCESSFLOWNAME  ");
		sql.append("   AND D.PROCESSFLOWVERSION = P.PROCESSFLOWVERSION  ");
		sql.append("   AND D.PROCESSOPERATIONNAME = P.PROCESSOPERATIONNAME  ");
		sql.append("   AND D.PROCESSOPERATIONVERSION = P.PROCESSOPERATIONVERSION  ");
		sql.append("   AND E.PRODUCTNAME = P.PRODUCTNAME  ");
		sql.append("ORDER BY P.POSITION  ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("LOTNAME", lotName);

		try
		{
			result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (result.size() > 0)
			{
				// Product Position information based on Operation registered in the MQCPlanDetail_EXTENDED Table
			}
			else
			{// Product Position Information Based on Existing MQCMainFlow
				StringBuilder sql2 = new StringBuilder();

				sql2.append("SELECT DISTINCT P.PRODUCTNAME, P.POSITION, E.RECIPENAME, E.MACHINENAME  ");
				sql2.append("  FROM CT_MQCPLAN M,  ");
				sql2.append("       CT_MQCPLANDETAIL D,  ");
				sql2.append("       CT_MQCPLANDETAIL_EXTENDED E, ");
				sql2.append("       PROCESSFLOW F, ");
				sql2.append("       PRODUCT P  ");
				sql2.append(" WHERE M.JOBNAME = D.JOBNAME  ");
				sql2.append("   AND M.JOBNAME = E.JOBNAME  ");
				sql2.append("   AND M.MQCSTATE = 'Recycling'  ");
				sql2.append("   AND E.PRODUCTNAME = P.PRODUCTNAME  ");
				sql2.append("   AND P.PRODUCTSTATE = 'InProduction' ");
				sql2.append("   AND D.PROCESSFLOWNAME = E.PROCESSFLOWNAME ");
				sql2.append("   AND D.PROCESSFLOWVERSION = E.PROCESSFLOWVERSION ");
				sql2.append("   AND E.PROCESSFLOWNAME = F.PROCESSFLOWNAME ");
				sql2.append("   AND E.PROCESSFLOWVERSION = F.PROCESSFLOWVERSION ");
				sql2.append("   AND F.PROCESSFLOWTYPE != 'MQCRecycle' ");
				sql2.append("   AND M.LOTNAME = :LOTNAME  ");
				sql2.append("   AND M.LOTNAME = P.LOTNAME  ");
				sql2.append("   AND E.PRODUCTNAME = P.PRODUCTNAME  ");
				sql2.append("ORDER BY P.POSITION  ");

				Map<String, String> args2 = new HashMap<String, String>();
				args2.put("LOTNAME", lotName);

				result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql2.toString(), args2);

				if (result.size() > 0)
				{
					return result;
				}
			}
		}
		catch (Exception ex)
		{
			log.info("getMQCRecycleProduct error ");
		}

		return result;
	}

	private List<Object> getSelectedSlotbySampleLotForMQC(List<Map<String, Object>> sampleLot)
	{
		List<Object> slotList = new ArrayList<Object>();

		for (Map<String, Object> result : sampleLot)
		{
			try
			{
				String sPosition = ConvertUtil.getMapValueByName(result, "POSITION");
				long position = Long.parseLong(sPosition);

				slotList.add(position);
			}
			catch (Exception ex)
			{
				continue;
			}
		}
		return slotList;
	}

	private String getMQCMachineRecipeName(List<Map<String, Object>> sampleLot, String machineName, String productName, String machineRecipeName)
	{
		for (Map<String, Object> result : sampleLot)
		{
			try
			{
				String sProductName = ConvertUtil.getMapValueByName(result, "PRODUCTNAME");

				if (StringUtils.equals(sProductName, productName))
				{
					String MQCMachineName = ConvertUtil.getMapValueByName(result, "MACHINENAME");

					if (StringUtils.equals(MQCMachineName, machineName)
							||(StringUtils.isEmpty(MQCMachineName)&&StringUtils.isNotEmpty(ConvertUtil.getMapValueByName(result, "RECIPENAME"))))
					{
						machineRecipeName = ConvertUtil.getMapValueByName(result, "RECIPENAME");
					}
					break;
				}
			}
			catch (Exception ex)
			{
				continue;
			}
		}
		return machineRecipeName;
	}

	private String getLotInfoBydurableNameForFisrtGlass(String carrierName) throws CustomException
	{
		List<Map<String, Object>> lotList;
		String lotName = "";

		String sql = "SELECT LOTNAME FROM LOT WHERE CARRIERNAME = :CARRIERNAME AND LOTSTATE = :LOTSTATE AND FIRSTGLASSFLAG = 'N' AND JOBNAME IS NOT NULL";

		Map<String, String> args = new HashMap<String, String>();
		args.put("CARRIERNAME", carrierName);
		args.put("LOTSTATE", GenericServiceProxy.getConstantMap().Lot_Released);

		lotList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

		if (lotList.size() > 0 && lotList != null)
			lotName = lotList.get(0).get("LOTNAME").toString();
		else
		{
			sql = "SELECT LOTNAME FROM LOT WHERE CARRIERNAME = :CARRIERNAME AND LOTSTATE = :LOTSTATE AND FIRSTGLASSFLAG IS NULL AND JOBNAME IS NOT NULL";

			lotList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

			if (lotList.size() > 0 && lotList != null)
				lotName = lotList.get(0).get("LOTNAME").toString();
			else
			{
				sql = "SELECT LOTNAME FROM LOT WHERE CARRIERNAME = :CARRIERNAME AND LOTSTATE = :LOTSTATE ";
				lotList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

				if (lotList.size() > 0 && lotList != null)
					lotName = lotList.get(0).get("LOTNAME").toString();
				else
					lotName = "";
			}
		}

		return lotName;
	}

	private void checkWIPBalance(Lot lotData, String machineName) throws CustomException
	{
		StringBuilder dataSql = new StringBuilder();
		dataSql.append(" SELECT W.FACTORYNAME, ");
		dataSql.append("        W.TARGETOPERATIONNAME, ");
		dataSql.append("        W.REASONOPERATIONNAME, ");
		dataSql.append("        W.LIMIT, ");
		dataSql.append("        W.DISPATCHSTATE ");
		dataSql.append("   FROM CT_WIPBALANCE W ");
		dataSql.append("  WHERE     W.TARGETOPERATIONNAME = :TARGETOPERATIONNAME ");
		dataSql.append("        AND W.TARGETOPERATIONVERSION = :TARGETOPERATIONVERSION ");

		Map<String, String> databindMap = new HashMap<String, String>();
		databindMap.put("TARGETOPERATIONNAME", lotData.getProcessOperationName());
		databindMap.put("TARGETOPERATIONVERSION", lotData.getProcessOperationVersion());

		List<Map<String, Object>> dataSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(dataSql.toString(), databindMap);

		if (dataSqlResult.size() > 0 && dataSqlResult != null)
		{
			StringBuilder sql = new StringBuilder();
			sql.append("WITH BALANCE ");
			sql.append("     AS (SELECT RESULT.FACTORYNAME, ");
			sql.append("                RESULT.TARGETOPERATIONNAME, ");
			sql.append("                RESULT.TARGETOPERATIONVERSION, ");
			sql.append("                LISTAGG (RESULT.REASONOPERATIONNAME, ', ') WITHIN GROUP (ORDER BY RESULT.REASONOPERATIONNAME) ");
			sql.append("                   REASONOPERATIONNAME, ");
			sql.append("                LISTAGG (RESULT.REASONOPERATIONVERSION, ', ') ");
			sql.append("                   WITHIN GROUP (ORDER BY RESULT.REASONOPERATIONVERSION) ");
			sql.append("                   REASONOPERATIONVERSION, ");
			sql.append("                LISTAGG (RESULT.LIMIT, ', ') WITHIN GROUP (ORDER BY RESULT.LIMIT) LIMIT, ");
			sql.append("                LISTAGG (RESULT.DISPATCHSTATE, ', ') WITHIN GROUP (ORDER BY RESULT.DISPATCHSTATE) ");
			sql.append("                   DISPATCHSTATE, ");
			sql.append("                LISTAGG (RESULT.REASONWIP, ', ') WITHIN GROUP (ORDER BY RESULT.REASONWIP) REASONWIP, ");
			sql.append("                SUM (RESULT.RESULT) RESULT ");
			sql.append("           FROM (SELECT WIPINFO.FACTORYNAME, ");
			sql.append("                        WIPINFO.TARGETOPERATIONNAME, ");
			sql.append("                        WIPINFO.TARGETOPERATIONVERSION, ");
			sql.append("                        WIPINFO.REASONOPERATIONNAME, ");
			sql.append("                        WIPINFO.REASONOPERATIONNAME || '_' || WIPINFO.REASONOPERATIONVERSION ");
			sql.append("                           REASONOPERATIONVERSION, ");
			sql.append("                        WIPINFO.REASONOPERATIONNAME || '_' || WIPINFO.LIMIT LIMIT, ");
			sql.append("                        WIPINFO.REASONOPERATIONNAME || '_' || WIPINFO.DISPATCHSTATE DISPATCHSTATE, ");
			sql.append("                        WIPINFO.REASONOPERATIONNAME || '_' || WIPINFO.REASONWIP REASONWIP, ");
			sql.append("                        CASE ");
			sql.append("                           WHEN WIPINFO.REASONWIP > WIPINFO.LIMIT ");
			sql.append("                            AND WIPINFO.DISPATCHSTATE = 'Y' ");
			sql.append("                           THEN ");
			sql.append("                              1 ");
			sql.append("                           ELSE ");
			sql.append("                              0 ");
			sql.append("                        END ");
			sql.append("                           RESULT ");
			sql.append("                   FROM (SELECT DISTINCT ");
			sql.append("                                WIP.FACTORYNAME, ");
			sql.append("                                WIP.TARGETOPERATIONNAME, ");
			sql.append("                                WIP.TARGETOPERATIONVERSION, ");
			sql.append("                                WIP.REASONOPERATIONNAME, ");
			sql.append("                                WIP.REASONOPERATIONVERSION, ");
			sql.append("                                WIP.LIMIT, ");
			sql.append("                                NVL (WIP.DISPATCHSTATE, 'N') DISPATCHSTATE, ");
			sql.append("                                (SELECT NVL (SUM (L.PRODUCTQUANTITY), 0) ");
			sql.append("                                   FROM LOT L ");
			sql.append("                                  WHERE L.FACTORYNAME = 'ARRAY' ");
			sql.append("                                    AND L.LOTSTATE = 'Released' ");
			sql.append("                                    AND L.PRODUCTIONTYPE IN ('P', 'E', 'T') ");
			sql.append("                                    AND ( ( (L.PROCESSOPERATIONNAME, L.PROCESSOPERATIONVERSION) IN ");
			sql.append("                                              (SELECT DISTINCT N.NODEATTRIBUTE1, N.NODEATTRIBUTE2 ");
			sql.append("                                                 FROM NODE N, ARC A ");
			sql.append("                                                WHERE N.NODEID = A.FROMNODEID ");
			sql.append("                                                  AND N.NODETYPE = 'ProcessOperation' ");
			sql.append("                                                  AND N.FACTORYNAME = 'ARRAY' ");
			sql.append("                                               START WITH NODEATTRIBUTE1 = WIP.REASONOPERATIONNAME ");
			sql.append("                                               CONNECT BY NOCYCLE A.TONODEID = PRIOR A.FROMNODEID ");
			sql.append("                                                              AND NODEATTRIBUTE1 != WIP.TARGETOPERATIONNAME)) ");
			sql.append("                                      OR ( (L.RETURNOPERATIONNAME, L.RETURNOPERATIONVER) IN ");
			sql.append("                                             (SELECT DISTINCT N.NODEATTRIBUTE1, N.NODEATTRIBUTE2 ");
			sql.append("                                                FROM NODE N, ARC A ");
			sql.append("                                               WHERE N.NODEID = A.FROMNODEID ");
			sql.append("                                                 AND N.NODETYPE = 'ProcessOperation' ");
			sql.append("                                                 AND N.FACTORYNAME = 'ARRAY' ");
			sql.append("                                              START WITH NODEATTRIBUTE1 = WIP.REASONOPERATIONNAME ");
			sql.append("                                              CONNECT BY NOCYCLE A.TONODEID = PRIOR A.FROMNODEID ");
			sql.append("                                                             AND NODEATTRIBUTE1 != WIP.TARGETOPERATIONNAME) ");
			sql.append("                                      AND INSTR (NODESTACK, '.') > 0))) ");
			sql.append("                                   REASONWIP ");
			sql.append("                           FROM TPFOPOLICY TPFO, POSMACHINE POS, CT_WIPBALANCE WIP ");
			sql.append("                          WHERE TPFO.CONDITIONID = POS.CONDITIONID ");
			sql.append("                            AND POS.MACHINENAME = :MACHINENAME ");
			sql.append("                            AND TPFO.PROCESSOPERATIONNAME = WIP.TARGETOPERATIONNAME ");
			sql.append("                            AND TPFO.PROCESSOPERATIONVERSION = WIP.TARGETOPERATIONVERSION) WIPINFO ");
			sql.append("                 ORDER BY TARGETOPERATIONNAME) RESULT ");
			sql.append("         GROUP BY RESULT.FACTORYNAME, RESULT.TARGETOPERATIONNAME, RESULT.TARGETOPERATIONVERSION) ");
			sql.append("SELECT A.* ");
			sql.append("  FROM BALANCE A ");
			sql.append(" WHERE A.TARGETOPERATIONNAME = :TARGETOPERATIONNAME ");
			sql.append("   AND A.TARGETOPERATIONVERSION = :TARGETOPERATIONVERSION ");

			Map<String, String> inquirybindMap = new HashMap<String, String>();
			inquirybindMap.put("MACHINENAME", machineName);
			inquirybindMap.put("TARGETOPERATIONNAME", lotData.getProcessOperationName());
			inquirybindMap.put("TARGETOPERATIONVERSION", lotData.getProcessOperationVersion());

			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), inquirybindMap);

			if (sqlResult.size() > 0)
			{
				log.info("Limit : " + sqlResult.get(0).get("LIMIT") + " , DispatchState : " + sqlResult.get(0).get("DISPATCHSTATE") + " , ReasonWIP : " + sqlResult.get(0).get("REASONWIP")
						+ " , Result : " + sqlResult.get(0).get("RESULT"));

				if (!StringUtils.equals(ConvertUtil.getMapValueByName(sqlResult.get(0), "RESULT"), "0"))
				{
					throw new CustomException("LOT-0136");
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private List<SampleLot> getLightingSampleData(Lot lotData, String machineName)
	{
		// Get SamplingRule
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT P.PRODUCTSAMPLINGCOUNT, P.PRODUCTSAMPLINGPOSITION ");
		sql.append("  FROM TFOMPOLICY T, POSSAMPLE P ");
		sql.append(" WHERE T.CONDITIONID = P.CONDITIONID ");
		sql.append("   AND T.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND T.PROCESSFLOWVERSION =:PROCESSFLOWVERSION ");
		sql.append("   AND T.PROCESSOPERATIONNAME =:PROCESSOPERATIONNAME   ");
		sql.append("   AND T.PROCESSOPERATIONVERSION =:PROCESSOPERATIONVERSION   ");
		sql.append("   AND P.TOPROCESSFLOWNAME = :TOPROCESSFLOWNAME ");
		sql.append("   AND P.TOPROCESSFLOWVERSION = :TOPROCESSFLOWVERSION ");
		sql.append("   AND P.TOPROCESSOPERATIONNAME = :TOPROCESSOPERATIONNAME ");
		sql.append("   AND P.TOPROCESSOPERATIONVERSION = :TOPROCESSOPERATIONVERSION ");
		
		String[] nodeStackArray = StringUtil.split(lotData.getNodeStack(), ".");
		if(nodeStackArray.length<2)
		{
			return null;
		}
		Node MainFlowNode = ProcessFlowServiceProxy.getNodeService().getNode(nodeStackArray[nodeStackArray.length - 2]);
		
		List<Map<String, Object>>nodeData = lastNode(MainFlowNode);

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("FACTORYNAME", lotData.getFactoryName());
		args.put("PROCESSFLOWNAME", MainFlowNode.getProcessFlowName());
		args.put("PROCESSFLOWVERSION", MainFlowNode.getProcessFlowVersion());
		args.put("PROCESSOPERATIONNAME", nodeData.get(0).get("NODEATTRIBUTE1"));
		args.put("PROCESSOPERATIONVERSION",  nodeData.get(0).get("NODEATTRIBUTE2"));
		args.put("TOPROCESSFLOWNAME", lotData.getProcessFlowName());
		args.put("TOPROCESSFLOWVERSION", lotData.getProcessFlowVersion());
		args.put("TOPROCESSOPERATIONNAME", lotData.getProcessOperationName());
		args.put("TOPROCESSOPERATIONVERSION", lotData.getProcessOperationVersion());

		List<Map<String, Object>> productResult = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();

		if (result.size() > 0)
		{
			String productSamplingCount = ConvertUtil.getMapValueByName(result.get(0), "PRODUCTSAMPLINGCOUNT");
			String productSamplingPosition = ConvertUtil.getMapValueByName(result.get(0), "PRODUCTSAMPLINGPOSITION");

			log.info(String.format("Get SamplingRule. ProductSamplingCount[%s] ProductSamplingPosition[%s]", productSamplingCount, productSamplingPosition));

			if (StringUtils.isEmpty(productSamplingCount) || StringUtils.isEmpty(productSamplingPosition))
			{
				result.clear();
				return null;
			}

			int iProductSamplingCountByRule = Integer.parseInt(productSamplingCount);
			String[] productSamplingPositionListByRule = StringUtils.split(productSamplingPosition, ",");
			List<String> positionList = new ArrayList<String>();
			List<String> positionListByRule = new ArrayList<String>();
			for (int idx = 0; productSamplingPositionListByRule.length > idx; idx++)
			{
				positionListByRule.add(productSamplingPositionListByRule[idx]);
			}

			// Get Glass inspected on EVA
			sql.setLength(0);
			sql.append("SELECT DISTINCT P.PRODUCTNAME, P.POSITION ");
			sql.append("  FROM CT_COMPONENTINSPECTHISTORY C, ENUMDEFVALUE E, PRODUCT P ");
			sql.append(" WHERE 1 = 1 ");
			sql.append("   AND C.PRODUCTNAME = P.PRODUCTNAME ");
			sql.append("   AND C.MATERIALLOCATIONNAME = E.ENUMVALUE ");
			sql.append("   AND P.LOTNAME = :LOTNAME ");
			sql.append("   AND P.PRODUCTSTATE = 'InProduction' ");
			sql.append("   AND E.ENUMNAME = :ENUMNAME ");
			sql.append("   AND C.INSPECTIONFLAG = 'O' ");
			sql.append("ORDER BY P.POSITION ");

			args.put("LOTNAME", lotData.getKey().getLotName());
			args.put("ENUMNAME", "OLEDLightingUnit");

			result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
			log.info("Inspected OLED Lighting Inspection Glass Count: " + result.size());

			if (result.size() > 0)
			{
				positionList = CommonUtil.makeListBySqlResult(result, "POSITION");
				log.info("Inspected OLED Lighting Inspection Glass Position " + positionList);
			}

			// Check Count
			if (iProductSamplingCountByRule > result.size())
			{
				sql.setLength(0);
				sql.append("SELECT P.POSITION ");
				sql.append("      ,CASE WHEN P.POSITION IN ( :POSLITIONLIST) THEN 'Y' ELSE 'N' END AS RULEPOSITON ");
				sql.append("      ,P.PRODUCTNAME ");
				sql.append("  FROM PRODUCT P ");
				sql.append(" WHERE P.LOTNAME = :LOTNAME ");
				sql.append("ORDER BY RULEPOSITON DESC, P.POSITION ");

				args.put("POSLITIONLIST", positionListByRule);
				productResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

				if (productResult.size() > 0)
				{
					for (int j = 0; productResult.size() > j; j++)
					{
						String position = ConvertUtil.getMapValueByName(productResult.get(j), "POSITION");

						if (!positionList.contains(position))
						{
							positionList.add(position);

							if (iProductSamplingCountByRule == positionList.size())
							{
								break;
							}
						}
					}
				}
			}
			else if (iProductSamplingCountByRule < result.size())
			{
				positionList = positionList.subList(0, iProductSamplingCountByRule);
			}

			String actualPosition = StringUtils.join(positionList, ",");
			log.info("LightingSample ActualPosition: " + actualPosition);

			SampleLot sampleLot = new SampleLot();
			sampleLot.setLotName(lotData.getKey().getLotName());
			sampleLot.setFactoryName(lotData.getFactoryName());
			sampleLot.setProductSpecName(lotData.getProductSpecName());
			sampleLot.setProductSpecVersion(lotData.getProductSpecVersion());
			sampleLot.setProcessFlowName(lotData.getProcessFlowName());
			sampleLot.setProcessFlowVersion(lotData.getProcessFlowVersion());
			sampleLot.setProcessOperationName(lotData.getProcessOperationName());
			sampleLot.setProcessOperationVersion(lotData.getProcessOperationVersion());
			sampleLot.setMachineName(machineName);
			sampleLot.setLotSampleFlag("Y");
			sampleLot.setLotSampleCount("1");
			sampleLot.setCurrentLotCount("1");
			sampleLot.setTotalLotCount("1");
			sampleLot.setProductSampleCount(Integer.toString(result.size()));
			sampleLot.setProductSamplePosition(actualPosition);
			sampleLot.setActualProductCount(Integer.toString(result.size()));
			sampleLot.setActualSamplePosition(actualPosition);

			sampleLotList.add(sampleLot);

			result.clear();
		}
		else
		{
			log.info("No SamplingRule Data.");
		}

		return sampleLotList;
	}

	private InlineSampleProduct setInlineSampleProduct(InlineSampleProduct oriInlineSampleProduct, Product productData, Lot lotData, String machineName)
	{
		InlineSampleProduct inlineSampleProduct = new InlineSampleProduct();
		List<InlineSampleProduct> inlineSampleProductList = new ArrayList<InlineSampleProduct>();

		try
		{
			String inspectionFlag = MESLotServiceProxy.getLotServiceUtil().getInspectionFlagForInlineSample(lotData, machineName, productData.getKey().getProductName());
			log.info("Slot: " + productData.getPosition() + ", Product: " + productData.getKey().getProductName() + ", InspectionFlag: " + inspectionFlag);

			if (StringUtils.isEmpty(inspectionFlag) && (oriInlineSampleProduct == null || StringUtils.isEmpty(oriInlineSampleProduct.getInspectionFlag())))
			{
				return null;
			}

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("InlineSampling", getEventUser(), getEventComment(), null, null);
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			if (oriInlineSampleProduct == null)
			{
				// Create
				inlineSampleProduct.setProductName(productData.getKey().getProductName());
				inlineSampleProduct.setLotName(productData.getLotName());
				inlineSampleProduct.setFactoryName(lotData.getFactoryName());
				inlineSampleProduct.setProductSpecName(lotData.getProductSpecName());
				inlineSampleProduct.setProductSpecVersion(lotData.getProductSpecVersion());
				inlineSampleProduct.setProcessFlowName(lotData.getProcessFlowName());
				inlineSampleProduct.setProcessFlowVersion(lotData.getProcessFlowVersion());
				inlineSampleProduct.setProcessOperationName(lotData.getProcessOperationName());
				inlineSampleProduct.setProcessOperationVersion(lotData.getProcessOperationVersion());
				inlineSampleProduct.setMachineName(machineName);
				inlineSampleProduct.setActualSamplePosition(String.valueOf(productData.getPosition()));
				inlineSampleProduct.setInspectionFlag(inspectionFlag);
				inlineSampleProduct.setProductSampleFlag("Y");
				inlineSampleProduct.setEventComment(eventInfo.getEventComment());
				inlineSampleProduct.setEventUser(eventInfo.getEventUser());

				inlineSampleProductList.add(inlineSampleProduct);

				ExtendedObjectProxy.getInlineSampleProductService().create(eventInfo, inlineSampleProductList);
				log.info("Success to set new InlineSampleProduct Data");

				inlineSampleProduct = ExtendedObjectProxy.getInlineSampleProductService().selectByKey(
						false,
						new Object[] { inlineSampleProduct.getProductName(), inlineSampleProduct.getLotName(), inlineSampleProduct.getFactoryName(), inlineSampleProduct.getProductSpecName(),
								inlineSampleProduct.getProductSpecVersion(), inlineSampleProduct.getProcessFlowName(), inlineSampleProduct.getProcessFlowVersion(),
								inlineSampleProduct.getProcessOperationName(), inlineSampleProduct.getProcessOperationVersion(), inlineSampleProduct.getMachineName() });
			}
			else
			{
				log.info("Slot: " + productData.getPosition() + ", Product: " + productData.getKey().getProductName() + ", ChangedInspectionFlag: " + inspectionFlag);

				oriInlineSampleProduct.setInspectionFlag(inspectionFlag);

				inlineSampleProductList.add(oriInlineSampleProduct);
				ExtendedObjectProxy.getInlineSampleProductService().modify(eventInfo, inlineSampleProductList);
				log.info("Success to change InspectionFlag InlineSampleProduct Data");

				inlineSampleProduct = ExtendedObjectProxy.getInlineSampleProductService().selectByKey(
						false,
						new Object[] { oriInlineSampleProduct.getProductName(), oriInlineSampleProduct.getLotName(), oriInlineSampleProduct.getFactoryName(),
								oriInlineSampleProduct.getProductSpecName(), oriInlineSampleProduct.getProductSpecVersion(), oriInlineSampleProduct.getProcessFlowName(),
								oriInlineSampleProduct.getProcessFlowVersion(), oriInlineSampleProduct.getProcessOperationName(), oriInlineSampleProduct.getProcessOperationVersion(),
								oriInlineSampleProduct.getMachineName() });
			}
		}
		catch (Exception e)
		{
			log.info("Fail to set new InlineSampleProduct Data");
			return null;
		}
 
		return inlineSampleProduct;
	}
	
	private List<Map<String, Object>> getChamberProduct (Machine machineData,Lot lotData, List<Product> productList) throws CustomException
	{
		List<Map<String, Object>> chamberProList;

        if(StringUtil.equals(machineData.getMachineGroupName(), "IFI"))
        {
        	String sql = " WITH MAXTIMEKEY AS "+
        			" (SELECT MAX (A.TIMEKEY)     TIMEKEY "+
        			" FROM (SELECT LH.TIMEKEY,LH.PROCESSOPERATIONNAME,LH.MACHINENAME,LH.MACHINERECIPENAME "+
        			" FROM LOTHISTORY LH, PROCESSOPERATIONSPEC POS "+
        			" WHERE     LH.PROCESSOPERATIONNAME =POS.PROCESSOPERATIONNAME "+
        			" AND POS.DETAILPROCESSOPERATIONTYPE = 'AOI' "+
        			" AND LOTNAME = :LOTNAME "+
        			" AND EVENTNAME = 'TrackIn') A)  "+
        			" SELECT L.PROCESSOPERATIONNAME, L.MACHINENAME, L.MACHINERECIPENAME "+
        			" FROM LOTHISTORY L, MAXTIMEKEY "+
        			" WHERE L.TIMEKEY = MAXTIMEKEY.TIMEKEY";
        	Map<String, String> args = new HashMap<String, String>();
    		args.put("LOTNAME", lotData.getKey().getLotName());

    		chamberProList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);
        }
        else
        {
        	List<String> productNameList = new ArrayList<String>();
        	
        	for(Product productData : productList)
        	{
        		productNameList.add(productData.getKey().getProductName());
        	}
        	
        	String sql = "SELECT DISTINCT A.PRODUCTNAME,A.PROCESSFLOWNAME,A.PROCESSOPERATIONNAME,A.MATERIALLOCATIONNAME,A.MACHINENAME,A.MACHINERECIPENAME FROM CT_COMPONENTINCHAMBERHIST A , "+
        			" (SELECT MAX(TRACKINTIME) MAXTRACKINTIME, PRODUCTNAME FROM CT_COMPONENTINCHAMBERHIST WHERE PRODUCTNAME IN (:PRODUCTNAME) GROUP BY PRODUCTNAME) B "+
        					" WHERE A.TRACKINTIME=B.MAXTRACKINTIME AND A.PRODUCTNAME = B.PRODUCTNAME ORDER BY MATERIALLOCATIONNAME DESC ";

        	Map<String, Object> args = new HashMap<String, Object>();
        	args.put("PRODUCTNAME", productNameList);

           chamberProList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);
        }

		return chamberProList;
	}
	private void checkTPPhotoOffset(ProcessOperationSpec operationData, Product productData, Machine machineData)
	{
		
	}
	
	private String getPhotoOffset(ProcessOperationSpec operationData, Product productData, Machine machineData, String machineRecipeName) throws CustomException
	{
		if(StringUtils.equals(productData.getProductionType(), "M")||StringUtils.equals(productData.getProductionType(), "D"))
		{
			return "0000";
		}
		String layerName = operationData.getUdfs().get("LAYERNAME").toString();
		String offset = "0000";
		String bindOffset = "";
		String offsetResult = "";
		
		if(productData.getUdfs().get("OFFSET") != null && StringUtils.isNotEmpty(productData.getUdfs().get("OFFSET").toString()))
		{
			offset = productData.getUdfs().get("OFFSET").toString();
		}
		OffsetAlignInfo offsetAlignInfo=new OffsetAlignInfo();
		try
		{
			offsetAlignInfo  = ExtendedObjectProxy.getOffsetAlignInfoService().selectByKey(false, new Object[]{productData.getFactoryName(), productData.getProductSpecName(), productData.getProductSpecVersion(), layerName});
		}
		catch (Exception ex)
		{
			if(StringUtils.equals(productData.getProductionType(), "E")||StringUtils.equals(productData.getProductionType(), "P")
					||StringUtils.equals(productData.getProductionType(), "T"))
			{
				throw new CustomException("OFFSET-0001");
			}
		}
			
		String alignInfoOffset = offsetAlignInfo.getAlignLayer1() + offsetAlignInfo.getAlignLayer2() + offsetAlignInfo.getAlignLayer3()
		                         + offsetAlignInfo.getAlignLayer4();
		
		for(int i = 0 ; i < alignInfoOffset.length() ; i++)
		{
			if(alignInfoOffset.charAt(i) == 'X')
			{
				offsetResult += "0";
			}
			else if(offset.length() <= i)
			{
				offsetResult += "0";
			}
			else
			{
				offsetResult += offset.charAt(i);
			}
		}
		
		bindOffset = machineRecipeName + "_" + offsetResult;
		
		ExtendedObjectProxy.getPhotoOffsetResultService().photoOffsetLimitCheck(machineData.getKey().getMachineName(), bindOffset); 
		return offsetResult;
	}
	
	private List<Map<String, Object>> getConsumableSpecList (Lot lotData) throws CustomException
	{
		List<Map<String, Object>> consumableSpecList = null;
		
		if (lotData != null)
		{
			if(StringUtils.equals(lotData.getProductionType(), "P")
					||StringUtils.equals(lotData.getProductionType(), "E")||StringUtils.equals(lotData.getProductionType(), "T"))
			{
				String sql = "select PB.MATERIALSPECNAME, TP.PRODUCTSPECNAME from POSBOM PB, TPPOLICY TP " +
						 "where PB.CONDITIONID = TP.CONDITIONID and PB.MATERIALTYPE = 'Crate' and TP.PRODUCTSPECNAME = :PRODUCTSPECNAME AND PB.USEFLAG = 'Y'";
	
			    Map<String, String> args = new HashMap<String, String>();
			    args.put("PRODUCTSPECNAME", lotData.getProductSpecName());
	
			    consumableSpecList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);
			    
			    if(consumableSpecList==null||consumableSpecList.size()==0)
			    {
			    	throw new CustomException("CRATE-0010");
			    }
	
			    if(consumableSpecList.size() > 5)
				   throw new CustomException("CRATE-0009", lotData.getProductSpecName(), String.valueOf(consumableSpecList.size()) );
			}
			else
			{
				String sql = "SELECT P.CRATESPECNAME AS MATERIALSPECNAME FROM PRODUCTREQUEST P,LOT L WHERE 1=1 " +
						 " AND L.LOTNAME=:LOTNAME AND L.PRODUCTREQUESTNAME = P.PRODUCTREQUESTNAME AND P.CRATESPECNAME IS NOT NULL ";
	
			    Map<String, String> args = new HashMap<String, String>();
			    args.put("LOTNAME", lotData.getKey().getLotName());
	
			    consumableSpecList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);
			    
			    if(consumableSpecList==null||consumableSpecList.size()==0)
			    {
			    	throw new CustomException("CRATE-0010");
			    }
			}

		}
		
		return consumableSpecList;
	}
	
	private void calculateIdleTime (Machine machineData, EventInfo eventInfo) throws CustomException
	{
		MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineData.getKey().getMachineName());
		
		String sLastIdleTime  = machineData.getUdfs().get("LASTIDLETIME");
		String sIdleTimeLimit = machineSpecData.getUdfs().get("IDLETIMELIMIT");

		if(!StringUtils.isEmpty(sIdleTimeLimit))
		{
			Timestamp currentTime  = eventInfo.getEventTime();
			Timestamp lastIdleTime = TimeStampUtil.getTimestamp(sLastIdleTime);
			
			long idleTimeLimit = Long.parseLong(sIdleTimeLimit);
			long calculateIdleTime = (currentTime.getTime() - lastIdleTime.getTime()) / (1000 * 60 * 60);
	
			if(calculateIdleTime > idleTimeLimit)
				throw new CustomException("MACHINE-0108", machineData.getKey().getMachineName(), String.valueOf(calculateIdleTime), String.valueOf(idleTimeLimit));
		
		}
	}
	
	private void calculateExpireDate (EventInfo eventInfo, Machine machineData) throws CustomException
	{
		String sql = "SELECT C.CONSUMABLENAME, C.CONSUMABLETYPE, C.EXPIRATIONDATE, C.THAWTIME, C.THAWTIMEUSEDLIMIT " +
					 "FROM CONSUMABLE C, CONSUMABLESPEC CS " +
					 "WHERE C.MACHINENAME = :MACHINENAME " +
					 "AND C.CONSUMABLESPECNAME = CS.CONSUMABLESPECNAME AND C.CONSUMABLETYPE = CS.CONSUMABLETYPE " +
					 "AND C.CONSUMABLESTATE = 'InUse' " +
					 "AND C.TRANSPORTSTATE = 'OnEQP' " +
					 "AND C.EXPIRATIONDATE is not null ";

		Map<String, String> args = new HashMap<String, String>();
		args.put("MACHINENAME", machineData.getKey().getMachineName());

		List<ListOrderedMap> consumableList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);
		SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		for (ListOrderedMap consumableMap : consumableList)
		{
			String consumableName = CommonUtil.getValue(consumableMap, "CONSUMABLENAME");
			String consumableType = CommonUtil.getValue(consumableMap, "CONSUMABLETYPE");
			
			
			Date currentTime    = eventInfo.getEventTime();
			Date expirationDate = (Date)CommonUtil.getDateValue(consumableMap, "EXPIRATIONDATE");

			int compare = currentTime.compareTo(expirationDate);

			if (compare == 1)
				throw new CustomException("MATERIAL-0028", consumableName, consumableType);
			
//Start 20210202 houxk			
//			if (consumableType.equals(constMap.MaterialType_Organicadhesive) || consumableType.equals(constMap.MaterialType_AdhesiveAgent) || consumableType.equals(constMap.MaterialType_PR))
//			{
//				String thawTimeString = CommonUtil.getValue(consumableMap, "THAWTIME");
//				String thawTimeUsedLimit = CommonUtil.getValue(consumableMap, "THAWTIMEUSEDLIMIT");
//				
//				Date thawTime = null;
//				Date currentDate = null;
//				String currentTimeString = transFormat.format(new Date());
//				
//				try {
//					thawTime = transFormat.parse(thawTimeString);
//					currentDate = transFormat.parse(currentTimeString);
//				} catch (ParseException e) {
//					e.printStackTrace();
//				}
//				
//				double gap = (double)(currentDate.getTime() - thawTime.getTime()) / (double)(60 * 60 * 1000);
//				
//				if (gap >= Double.parseDouble(thawTimeUsedLimit)) 
//				{
//					throw new CustomException("MATERIAL-0030", consumableName);
//				}
//			}
//End			
		}
	}
	
	//AR-AMF-0029-01
	private void setSlotSelData (EventInfo eventInfo, Lot lotData, String slotSel) throws CustomException
	{
		try
		{
			// Update Product ReworkFlag
			String sql = "UPDATE LOT SET SLOTSEL = :SLOTSEL WHERE LOTNAME = :LOTNAME";
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("SLOTSEL", slotSel);
			bindMap.put("LOTNAME", lotData.getKey().getLotName());
	
			GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		}
		catch (Exception e)
		{
			
		}
	}
	
	private String findPairingRecipe (Lot lotData, String factoryName, String productSpecName, String processFlowName, 
			String processOperationName, String machineName, String afterProcessOperName ) throws CustomException
	{
		ChangeRecipe changeRecipe = null;
		String machineRecipeName = "";
		
		try
		{
			changeRecipe = ExtendedObjectProxy.getChangeRecipeService().
					selectByKey(false, new Object[]{factoryName, productSpecName, processFlowName, processOperationName, machineName, afterProcessOperName});
		}
		catch (Exception ex)
		{
			log.info("ChangeRecipe has no data..");
		}
		if(changeRecipe != null)
		{
			Date chengedTime = changeRecipe.getChangedTime();
			Date lastLoggedInTime = lotData.getLastLoggedInTime();
		
			if(chengedTime.getTime() > lastLoggedInTime.getTime())
				machineRecipeName = changeRecipe.getAfterRecipeName();
		}
		else
			log.info("Find recipe from POSMachine..");
		
		return machineRecipeName;
		
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
				|| !recipeData.getENGFlag().equals("Y") || !recipeData.getActiveState().equals("Active")) {

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
	
	public void checkSorterJob(Lot lotData, Durable durableData, Port portData, Machine machineData) throws CustomException
	{
		ProcessFlowKey processFlowKey = new ProcessFlowKey();

		processFlowKey.setFactoryName(lotData.getFactoryName());
		processFlowKey.setProcessFlowName(lotData.getProcessFlowName());
		processFlowKey.setProcessFlowVersion(lotData.getProcessFlowVersion());

		ProcessFlow processFlow = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
		
		ProcessOperationSpec operationSpecData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());
		String DetailOperationType = operationSpecData.getDetailProcessOperationType().toString();
		
		if(StringUtil.equals(processFlow.getProcessFlowType(), "Sort"))
		{
			List<ListOrderedMap> sortJobList;
			try
			{
				// Sorter job reserved
				StringBuffer sqlBuffer = new StringBuffer();
				sqlBuffer.append("SELECT J.JOBNAME, ");
				sqlBuffer.append("       J.JOBSTATE, ");
				sqlBuffer.append("       C.MACHINENAME, ");
				sqlBuffer.append("       C.PORTNAME, ");
				sqlBuffer.append("       C.CARRIERNAME, ");
				sqlBuffer.append("       P.FROMLOTNAME, ");
				sqlBuffer.append("       P.PRODUCTNAME, ");
				sqlBuffer.append("       P.FROMPOSITION, ");
				sqlBuffer.append("       P.FROMSLOTPOSITION, ");
				sqlBuffer.append("       P.TURNDEGREE ");
				sqlBuffer.append("  FROM CT_SORTJOB J, CT_SORTJOBCARRIER C, CT_SORTJOBPRODUCT P ");
				sqlBuffer.append(" WHERE J.JOBNAME = C.JOBNAME ");
				sqlBuffer.append("   AND C.JOBNAME = P.JOBNAME ");
				sqlBuffer.append("   AND C.MACHINENAME = P.MACHINENAME ");
				sqlBuffer.append("   AND (C.CARRIERNAME = P.FROMCARRIERNAME OR C.CARRIERNAME = P.TOCARRIERNAME )");
				sqlBuffer.append("   AND C.CARRIERNAME = ? ");
				sqlBuffer.append("   AND C.MACHINENAME = ? ");
				sqlBuffer.append("   AND C.PORTNAME = ? ");
				sqlBuffer.append("   AND J.JOBSTATE = ? ");

				Object[] bindList = new Object[] { durableData.getKey().getDurableName(), machineData.getKey().getMachineName(), portData.getKey().getPortName(),
						GenericServiceProxy.getConstantMap().SORT_JOBSTATE_CONFIRMED };
				
				sortJobList = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bindList);
				
				if(sortJobList.size() == 0)
				{
					throw new CustomException("LOT-0326");//
				}
			}
			catch (Exception ex)
			{
				throw new CustomException("LOT-0327");//
			}
			
			
		}
	}
	
	private List<Map<String, Object>> lastNode(Node nodeStack)
	{
		// Get SamplingRule
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT A.NODEID,A.NODETYPE,A.NODEATTRIBUTE1,A.NODEATTRIBUTE2,A.XCOORDINATE,A.YCOORDINATE,A.FACTORYNAME,A.PROCESSFLOWNAME,A.PROCESSFLOWVERSION ");
		sql.append("  FROM NODE A, ARC B WHERE 1=1 ");
		sql.append(" AND A.NODEID = B.FROMNODEID ");
		sql.append("   AND B.TONODEID = :NODEID ");
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("NODEID", nodeStack.getKey().getNodeId());		
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		return result;
	}
	private boolean getFLCCheckRecipe(String machineName,Lot lotData) throws CustomException
	{
		String sqlEQP = "SELECT * FROM ENUMDEFVALUE WHERE  ENUMNAME = 'PFTCheckReceipeEQP' AND ENUMVALUE=:ENUMVALUE";
		String sqlSpec = "SELECT * FROM ENUMDEFVALUE WHERE  ENUMNAME = 'PFTCheckReceipeSpec' AND ENUMVALUE=:ENUMVALUE";

		Map<String, String> argsEqp = new HashMap<String, String>();
		argsEqp.put("ENUMVALUE", machineName);
		Map<String, String> argsSpec = new HashMap<String, String>();
		argsSpec.put("ENUMVALUE", lotData.getProductSpecName());

		List<Map<String, Object>> resultEQP = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlEQP, argsEqp);
		List<Map<String, Object>> resultSpec = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlSpec, argsSpec);
		if((resultEQP!=null&&resultEQP.size()>0)&&(resultSpec!=null&&resultSpec.size()>0)){
			return true;
		}
		return false;
	}
	private List<Map<String, Object>> getIDLETimeByChamber(String machineName) throws CustomException
    {
		String sql = "SELECT * FROM ENUMDEFVALUE WHERE  ENUMNAME = 'IDLETimeByChamber' AND ENUMVALUE=:ENUMVALUE AND DEFAULTFLAG='N'";

        Map<String, String> args = new HashMap<String, String>();
        args.put("ENUMVALUE", machineName);
        List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

        return result;
    }
	private List<Map<String, Object>> getIDLETimeByNormalChamber(String machineName) throws CustomException
    {
		String sql = "SELECT * FROM ENUMDEFVALUE WHERE  ENUMNAME = 'IDLETimeByChamber' AND ENUMVALUE=:ENUMVALUE AND DEFAULTFLAG='Y'";

        Map<String, String> args = new HashMap<String, String>();
        args.put("ENUMVALUE", machineName);
        List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

        return result;
    }
    public List<Map<String, Object>> getFLCRecipe(String factoryName, String productSpecName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String machineName)
    {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT PM.MACHINERECIPENAME, PM.RMSFLAG, PM.ECRECIPEFLAG, PM.ECRECIPENAME");
        sql.append("  FROM TPFOPOLICY TR, POSMACHINE PM ");
        sql.append(" WHERE TR.CONDITIONID = PM.CONDITIONID ");
        sql.append("   AND TR.FACTORYNAME = :FACTORYNAME ");
        sql.append("   AND TR.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
        sql.append("   AND TR.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
        sql.append("   AND TR.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
        sql.append("   AND TR.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
        sql.append("   AND TR.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
        sql.append("   AND PM.MACHINENAME = :MACHINENAME ");

        Map<String, String> args = new HashMap<String, String>();
        args.put("FACTORYNAME", factoryName);
        args.put("PRODUCTSPECNAME", productSpecName);
        args.put("PROCESSFLOWNAME", processFlowName);
        args.put("PROCESSFLOWVERSION", processFlowVersion);
        args.put("PROCESSOPERATIONNAME", processOperationName);
        args.put("PROCESSOPERATIONVERSION", processOperationVersion);
        args.put("MACHINENAME", machineName);

        List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

        return result;
    }
    private boolean checkPostCellLoad()
	{
		String sql="SELECT*FROM ENUMDEFVALUE WHERE ENUMNAME='PostCellLoadInfo' and ENUMVALUE='PostCellLoadInfo' ";
		Map<String, Object> bindMap = new HashMap<String, Object>();
		List<Map<String,Object>> resultList =null;
		resultList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, bindMap);
		if(resultList!=null&& resultList.size()>0)
		{
			return true;
			
		}
		return false;
	}
    private void updatePostCellLoadInfo(String machineName,String portName,String productSpec,String processOperation,String productRequest) throws CustomException
	{
		String sql="SELECT PRODUCTSPEC,PROCESSOPERATION, PRODUCTREQUEST FROM CT_POSTCELLLOADINFO WHERE  MACHINENAME=:MACHINENAME AND PORTNAME=:PORTNAME ";
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("MACHINENAME", machineName);
		bindMap.put("PORTNAME", portName);
		List<Map<String,Object>> resultList =null;
		try
		{
		   resultList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, bindMap);
		}
		catch (Exception ex)
		{
		  log.info("updatePostCellLoadInfo File");
		}
		if(resultList!=null&& resultList.size()>0)
		{
			String loadProductSpecName=resultList.get(0).get("PRODUCTSPEC").toString();
			String loadProcessOperationName=resultList.get(0).get("PROCESSOPERATION").toString();
			String loadProductRequestName=resultList.get(0).get("PRODUCTREQUEST").toString();
			if(!loadProductSpecName.equals(productSpec)||!loadProcessOperationName.equals(processOperation)||!loadProductRequestName.equals(productRequest)){
				
				StringBuffer updateSql = new StringBuffer();
				updateSql.append("UPDATE CT_POSTCELLLOADINFO SET PRODUCTSPEC=?, ");
				updateSql.append(" PROCESSOPERATION=?, PRODUCTREQUEST=?  ");
				updateSql.append(" WHERE  MACHINENAME=? AND PORTNAME=?  ");
				List<Object[]> updatePostcellLoadInfo = new ArrayList<Object[]>();
				List<Object> loadInfo = new ArrayList<Object>();
				loadInfo.add(productSpec);
				loadInfo.add(processOperation);
				loadInfo.add(productRequest);
				loadInfo.add(machineName);
				loadInfo.add(portName);
				updatePostcellLoadInfo.add(loadInfo.toArray());
				try
				{
					MESLotServiceProxy.getLotServiceUtil().updateBatch(updateSql.toString(), updatePostcellLoadInfo);
				}
				catch (Exception ex)
				{
				  log.info("updatePostCellLoadInfo File");
				}
				
			}
			
		}else{
			StringBuffer insertSql = new StringBuffer();
			insertSql.append("INSERT INTO CT_POSTCELLLOADINFO  ");
			insertSql.append("(PRODUCTSPEC,PROCESSOPERATION,PRODUCTREQUEST,MACHINENAME,PORTNAME) ");
			insertSql.append(" VALUES  ");
			insertSql.append("(?,?,?,?,?) ");
			List<Object[]> insertPostcellLoadInfo = new ArrayList<Object[]>();
			List<Object> insertloadInfo = new ArrayList<Object>();
			insertloadInfo.add(productSpec);
			insertloadInfo.add(processOperation);
			insertloadInfo.add(productRequest);
			insertloadInfo.add(machineName);
			insertloadInfo.add(portName);
			insertPostcellLoadInfo.add(insertloadInfo.toArray());
			try
			{
				MESLotServiceProxy.getLotServiceUtil().updateBatch(insertSql.toString(), insertPostcellLoadInfo);
			}
			catch (Exception ex)
			{
			  log.info("insertPostCellLoadInfo File");
			}
		}
	}
}