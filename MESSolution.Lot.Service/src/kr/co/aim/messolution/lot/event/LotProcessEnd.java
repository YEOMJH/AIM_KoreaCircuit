package kr.co.aim.messolution.lot.event;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AbnormalUnitInfo;
import kr.co.aim.messolution.extended.object.management.data.LotHoldAction;
import kr.co.aim.messolution.extended.object.management.data.MQCRunControl;
import kr.co.aim.messolution.extended.object.management.data.ProductQueueTime;
import kr.co.aim.messolution.extended.object.management.data.ReviewOperationInfo;
import kr.co.aim.messolution.extended.object.management.data.SampleLot;
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.event.EventInfoExtended;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.MakeLoggedOutInfo;
import kr.co.aim.greentrack.lot.management.info.ext.ConsumedMaterial;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processoperationspec.ProcessOperationSpecServiceProxy;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpecKey;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductHistory;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class LotProcessEnd extends SyncHandler {

	private static Log log = LogFactory.getLog(LotProcessEnd.class);

	@Override
	public Document doWorks(Document doc) throws CustomException
	{
		try
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "LotProcessEndReply");

			boolean isSorter       = false;
			boolean isFirstGlass   = false;
			boolean isBufferCST    = false;
			boolean isInReworkFlow = false;
			boolean isBufferCSTSortJob = false;
			
			Element bodyElement      = SMessageUtil.getBodyElement(doc);
			String oldMqcSpecName    = "";
			String machineName       = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String carrierName       = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
			String portName          = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
			String slotSel 			 = SMessageUtil.getBodyItemValue(doc, "SLOTSEL", false);
			String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);
			String oldLotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);

			String newLotName = "";
			String reworkFlag = "";
			String lotJudge   = "";
			String productOffset = "";
			
			List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false);
			List<String> productNameList = CommonUtil.makeList(bodyElement, "PRODUCTLIST", "PRODUCTNAME");
			List<String> processedProductList=getProcessedProductList(productList);
			
			List<Map<String, Object>> lotListByProductList = null;
			List<String> srcLotList  = new ArrayList<String>();
			List<String> destLotList = new ArrayList<String>();
			
			greenFrameServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);

			Lot oldLotData = null;
			
			if(StringUtil.isNotEmpty(oldLotName))
			{
				oldLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(oldLotName);
			}
			
			// for common
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
			Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
			
			// Check isInReworkFlow (False or True)
			if (productNameList.size() > 0)
				isInReworkFlow = MESLotServiceProxy.getLotServiceUtil().isInReworkFlow(productNameList);

			
			// Get LotData
			Lot lotDataByCarrier = new Lot();
			String lotNameByCarrier = MESLotServiceProxy.getLotServiceUtil().getLotInfoBydurableNameForFisrtGlass(carrierName);
			try{lotDataByCarrier = MESLotServiceProxy.getLotServiceUtil().getLotData(lotNameByCarrier);}catch (Exception e){}

			
			// Check FisrtGlass (False or True)
			isFirstGlass = MESLotServiceProxy.getLotServiceUtil().judgeFirstGlassLot(lotDataByCarrier, isFirstGlass);
			

			// Check isBufferCST
			if (StringUtils.equals(durableData.getDurableType(), GenericServiceProxy.getConstantMap().CST_TYPE_BUFFER))
				isBufferCST = true;

			
			// Sorter EQP - MachineGroupName : SORTER
			if (StringUtils.equalsIgnoreCase(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Sorter))
			{
				isSorter = true;

				if (StringUtils.equals(machineData.getFactoryName(), "ARRAY") || StringUtils.equals(machineData.getFactoryName(), "TP"))
				{
					isBufferCSTSortJob = MESLotServiceProxy.getLotServiceUtil().isBufferCSTSortJob(machineName, portName, carrierName);

					if (isBufferCSTSortJob)
						lotNameByCarrier = MESLotServiceProxy.getLotServiceUtil().getLotNameForSorter(lotDataByCarrier, durableData, lotNameByCarrier, machineName, portName);
				}
			}

			if(machineData.getUdfs().get("OPERATIONMODE").equals(GenericServiceProxy.getConstantMap().SORT_OPERATIONMODE))
				isSorter = true;
			
			// job end in unpacker scenario
			if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Unpacker))
			{
				this.UnpackerProcessEnd(doc);
				return doc;
			}

			// Validation - Check duplicate reported glass ID and position
			CommonValidation.checkDuplicatedProductNameByProductList(productNameList);
			CommonValidation.checkDuplicatePosition(productList);
			
			// Mantis - 0000391
			if (!isSorter)
				CommonValidation.checkDifferentSpec(productList);

			// lot on PB Port then throw error
			if (StringUtils.equals(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), "PB") && !isFirstGlass)
			{
				if (lotDataByCarrier.getProductQuantity() != productNameList.size())
					throw new CustomException("LOT-0071", productNameList.size(), lotDataByCarrier.getProductQuantity(), "PB");
			}

			
			if(!StringUtils.equals(machineSpecData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Sorter))
			{
				// Check Validation (if LotProcessState is WAIT then error)
				if (!isBufferCSTSortJob && !isBufferCST && !isFirstGlass)
				{
					// Lot Hold after LotProcessState is Wait #0000360
					lotDataByCarrier = checkLotProcessStateWait( eventInfo, lotDataByCarrier);
					
					if (StringUtils.isNotEmpty(lotNameByCarrier))
						MESLotServiceProxy.getLotServiceUtil().checkLotValidation(lotDataByCarrier); //Check lotState, lotHoldState, LotProcessState
	
					if (productNameList.size() > 0)
						MESLotServiceProxy.getLotServiceUtil().checkLotStatus(productNameList); //Check lotState, lotHoldState, LotProcessState
				}
				else if (isFirstGlass && productNameList.size() > 0 && StringUtils.isNotEmpty(lotNameByCarrier))
				{
					MESLotServiceProxy.getLotServiceUtil().checkLotValidation(lotDataByCarrier); //Check lotState, lotHoldState, LotProcessState
				}
			}
			
			
			// Check Available EQP
			if (!isBufferCSTSortJob && StringUtils.isNotEmpty(lotNameByCarrier) && !isSorter)
				MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotDataByCarrier.getFactoryName(), lotDataByCarrier.getProductSpecName(),lotDataByCarrier.getProductSpecVersion(), lotDataByCarrier.getProcessFlowName(), lotDataByCarrier.getProcessFlowVersion(), lotDataByCarrier.getProcessOperationName(),lotDataByCarrier.getProcessOperationVersion(), machineName, false);		

			
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// * Check Abort for PB Port
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			if (this.checkAbort(productList) && CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PB"))
			{
				ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(oldLotData.getProductRequestName());
				List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();
				ProcessOperationSpec operationSpecData = 
						CommonUtil.getProcessOperationSpec(lotDataByCarrier.getFactoryName(), lotDataByCarrier.getProcessOperationName(),lotDataByCarrier.getProcessOperationVersion());

				eventInfo = EventInfoUtil.makeEventInfo("CancelTrackIn", getEventUser(), getEventComment(), "", "");
				eventInfo.setEventComment(eventInfo.getEventComment() + " :Exist 'B' as ProcessingInfo");

				// Set productPGSRCSequence
				if (isFirstGlass)
					productPGSRCSequence = MESLotServiceProxy.getLotServiceUtil().setProductPGSRCSequenceForFirstGlass(bodyElement, lotDataByCarrier.getKey().getLotName(), isInReworkFlow, eventInfo);
				else if (operationSpecData.getDetailProcessOperationType().equals("AT"))
					productPGSRCSequence = MESLotServiceProxy.getLotServiceUtil().setProductPGSRCSequenceForArrayTest(bodyElement, isInReworkFlow, eventInfo);
				else
					productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(SMessageUtil.getBodyElement(doc), isInReworkFlow, eventInfo);

				//Return QTime Count
				ExtendedObjectProxy.getProductQTimeService().changeQTime(eventInfo, lotDataByCarrier.getKey().getLotName(), lotDataByCarrier.getFactoryName(), lotDataByCarrier.getProcessFlowName(), lotDataByCarrier.getProcessOperationName(), productList);
				//Cancel TrackIn. 
				lotDataByCarrier = MESLotServiceProxy.getLotServiceImpl().cancelTrackIn(eventInfo, lotDataByCarrier, productPGSRCSequence, new HashMap<String, String>(),new HashMap<String, String>(), new ArrayList<ConsumedMaterial>(), carrierName);

				// Mantis : 0000474
				// 2021-04-19	dhko	Update CT_MQCRUNCONTROL for DSP
				ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(oldLotData.getFactoryName(), oldLotData.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
				
				EventInfoExtended mqcRunControlEventInfo = new EventInfoExtended(eventInfo);
				mqcRunControlEventInfo.setEventName("TrackOut");
				mqcRunControlEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				
				int actualProductQty = 0;
				for (ProductPGSRC productPGSRC : productPGSRCSequence)
				{
					Map<String, String> productUdfs = productPGSRC.getUdfs();
					if (!StringUtils.equals(productUdfs.get("PROCESSINGINFO"), "B") && !StringUtils.equals(productUdfs.get("PROCESSINGINFO"), "S"))
					{
						actualProductQty ++;
					}
				}
				
				String machineLockFlag = StringUtil.EMPTY;
				if (CommonUtil.equalsIn(oldLotData.getProductionType(), "P", "E") && actualProductQty > 0)
				{
					// Update ACTUALPRODUCTQTY
					mqcRunControlEventInfo.setEventComment("Update ActualProductQty. " + eventInfo.getEventComment());
					MQCRunControl dataInfo = ExtendedObjectProxy.getMQCRunControlService().updateActualProductQty(mqcRunControlEventInfo, machineName, oldLotData.getProcessOperationName(), oldLotData.getProcessOperationVersion(), actualProductQty);
					
					if (dataInfo != null)
					{
						String condition = " WHERE MACHINENAME = ? AND MQCPROCESSFLOWNAME = ? AND MQCPROCESSFLOWVERSION = ? ";
						List<MQCRunControl> checkActualProduct = null;
						
						try
						{
							checkActualProduct = ExtendedObjectProxy.getMQCRunControlService().select(condition, new Object[] { machineName, dataInfo.getMqcProcessFlowName(), dataInfo.getMqcProcessFlowVersion() });
						}
						catch (Exception e)
						{
							checkActualProduct =null;
						}
						
						if (checkActualProduct.size()>0)
						{
							double ratio = 0;
							for (MQCRunControl checkActualProductRow : checkActualProduct)
							{
								int maxProductQtyByChamber = (int) checkActualProductRow.getMaxProductQtyByChamber().intValue();
								int chamberQty = (int) checkActualProductRow.getChamberQty().intValue();
								int actualProductRow = (int) checkActualProductRow.getActualProductQty().intValue();
								ratio = ratio + new BigDecimal((float)actualProductRow / (maxProductQtyByChamber * chamberQty)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
							}
							if (ratio >= 1)
							{
								machineLockFlag = "Y";
							}
						}
					}
				}
				else if (CommonUtil.equalsIn(productSpecData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare") && actualProductQty > 0)
				{
					// Update MQCPROCESSQTY
					mqcRunControlEventInfo.setEventComment("Update MQCProcessQty. " + eventInfo.getEventComment());
					
					List<MQCRunControl> runControlDataList = ExtendedObjectProxy.getMQCRunControlService()
							.updateMQCProcessQty(mqcRunControlEventInfo, machineName, oldLotData.getProcessFlowName(),
									oldLotData.getProcessFlowVersion(), actualProductQty);
					
					if (runControlDataList != null && runControlDataList.size() > 0) 
					{
						int actualMQCQty = 0;
						double needMQCQty = 0;
						
						for (MQCRunControl runControlData : runControlDataList) 
						{
							actualMQCQty = (int) runControlData.getMqcProcessQty().intValue();
							int maxMQCQtyByChamber = (int) runControlData.getMaxMQCQtyByChamber().intValue();
							int chamberQty = (int) runControlData.getChamberQty().intValue();
							int maxProductQtyByChamber = (int) runControlData.getMaxProductQtyByChamber().intValue();
							int actualProductRow = (int) runControlData.getActualProductQty().intValue();
							double ratio = new BigDecimal((float)actualProductRow / (maxProductQtyByChamber * chamberQty)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
							
							needMQCQty = needMQCQty + ratio * maxMQCQtyByChamber * chamberQty;
						}
						if (actualMQCQty > needMQCQty)
						{
							mqcRunControlEventInfo.setEventComment(
									"Initial ActualProductQty/MQCProcessQty. " + eventInfo.getEventComment());
							
							ExtendedObjectProxy.getMQCRunControlService().initialQtyByFlow(mqcRunControlEventInfo,
									machineName, oldLotData.getProcessFlowName(), oldLotData.getProcessFlowVersion());
							
							machineLockFlag = "N";
						}
					}
				}
				
				if (!StringUtil.isEmpty(machineLockFlag))
				{
					mqcRunControlEventInfo.setEventComment("ActualProductQty/MQCProcessQty Over. " + eventInfo.getEventComment());
					MESMachineServiceProxy.getMachineServiceImpl().changeMachineLockFlag(mqcRunControlEventInfo, machineData, machineLockFlag);
				}
				
				//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

				if (!StringUtils.equals(lotDataByCarrier.getLotHoldState(), "Y") || MESLotServiceProxy.getLotServiceUtil().isExistAbortedAlarmFailProduct(productList))
				{
					// Set ReasonCode
					eventInfo.setReasonCodeType("HOLD");
					eventInfo.setReasonCode("SYSTEM");

					// LotMultiHold
					MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, lotDataByCarrier, new HashMap<String, String>());
				}

				// Update Reserve Lot Info (State) 
				MESLotServiceProxy.getLotServiceUtil().updateReservedLotStateByCancelTrackIn(lotDataByCarrier.getKey().getLotName(), machineName, lotDataByCarrier, eventInfo);

				// Update reworkCount
				MESLotServiceProxy.getLotServiceUtil().setProductReworkCount(oldLotData, processedProductList, operationSpecData);
				
				// Make new elementBody for RetuenMsg(BC)
				Element newBody = generateReturnBodyTemplate(SMessageUtil.getBodyElement(doc), lotDataByCarrier);
				doc.getRootElement().getChild(SMessageUtil.Body_Tag).detach();
				doc.getRootElement().addContent(2,newBody);
				
				//SAP
				try
				{
					String sapFlag=CommonUtil.getEnumDefValueStringByEnumName("SAPFLAG");
					if(StringUtil.isNotEmpty(sapFlag)&&StringUtil.equals(sapFlag, "Y")&&
							StringUtils.isNotEmpty(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
					{
						SuperProductRequest superWO = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
						
						MESConsumableServiceProxy.getConsumableServiceUtil().trackOutERPBOMReportForOnline(eventInfo, oldLotData, superWO, machineName, productPGSRCSequence);
					}
				}
				catch(Exception x)
				{
					eventLog.info("SAP Report Error");
				}

				
				return doc;
			}
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


			
			
			

			
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 0. ProductCount of CST is to be 0 (Empty CST is out)
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			if (productList.size() == 0)
			{
				if (lotDataByCarrier.getProductQuantity() > 0)
				{
					List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotDataByCarrier);
					DeassignCarrierInfo createInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotDataByCarrier, durableData, productUSequence);

					eventInfo = EventInfoUtil.makeEventInfo("DeassignCarrier", getEventUser(), getEventComment(), "", "");
					MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotDataByCarrier, createInfo, eventInfo);
				}
                 
				Element newBody = generateReturnBodyTemplate(SMessageUtil.getBodyElement(doc));
				newBody.getChild("RESULT").setText("OK");
				newBody.getChild("RESULTDESCRIPTION").setText("");

				doc.getRootElement().getChild(SMessageUtil.Body_Tag).detach();
				doc.getRootElement().addContent(2, newBody);
				
				return doc;
			}
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


			
			
			

			
			Lot trackOutLot = new Lot();
			Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();
			Map<String, String> assignCarrierUdfs = new HashMap<String, String>();
			
			// MQC DummyUsedCountIncrease. 
			// AR-AMF-0022-01 -> ComponentInIndexer
			//if (StringUtils.isNotEmpty(lotNameByCarrier))
			//	MESLotServiceProxy.getLotServiceUtil().dummyUsedCountIncrease(lotDataByCarrier, eventInfo);
			
			// Get LotList by ProductList
			lotListByProductList = MESLotServiceProxy.getLotServiceUtil().getLotListByProductList(productList, productNameList, portData, isBufferCST, isBufferCSTSortJob, isFirstGlass, machineName, portName, carrierName);

			// Get not reportedProductList that included in CSTLot, not included in ProductList
			List<Element> noReportProductList = new ArrayList<Element>();
			if (!isFirstGlass)
				noReportProductList = MESLotServiceProxy.getLotServiceUtil().getNoReportedProductListInLot(lotNameByCarrier, productList);

			// Nothing to track out case
			if (lotListByProductList.size() < 1)
				throw new CustomException("LOT-9001", "PRODUCTLIST : " + CommonUtil.makeListForQuery(productList, "PRODUCTNAME"));

			// First Lot of LotList is Base Lot : much productQty Lot of LotList
			Lot baseLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotListByProductList.get(0).get("LOTNAME").toString());
			ProductSpec BaseProductSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(baseLotData.getFactoryName(), baseLotData.getProductSpecName(), baseLotData.getProductSpecVersion());
			ProcessOperationSpec operationSpecData = CommonUtil.getProcessOperationSpec(baseLotData.getFactoryName(), baseLotData.getProcessOperationName(), baseLotData.getProcessOperationVersion());

			List<Element> productElementList = SMessageUtil.getSubSequenceItemList(bodyElement, "PRODUCTLIST", true);
			List<String> productRecipeList = new ArrayList<String>();
			
			for (Element productElement : productElementList)
			{
				String productRecipe = SMessageUtil.getChildText(productElement, "PRODUCTRECIPE", false);
				productRecipeList.add(productRecipe);
			}
			
			//Get ProductData for TP Photo Offset ghhan
			if(machineData.getFactoryName().equals("TP") && CommonUtil.equalsIn(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Photo))
			{
				List<ProductHistory> productHistoryList = ProductServiceProxy.getProductHistoryService().select(" productName = ? order by timekey desc ",
						new Object[] { productNameList.get(0)});
				
				for(ProductHistory productHistoryData : productHistoryList)
				{
					if(productHistoryData.getEventName().equals("TrackIn"))
					{
						productOffset = productHistoryData.getUdfs().get("OFFSET").toString();
						break;
					}
				}
			}
			
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 1. ProductList go into EmptyCarrier
			if (lotDataByCarrier.getProductQuantity() == 0)
			{
				// productList.size() == baseLotData.getProductQuantity()
				if (lotListByProductList.size() == 1 && (productList.size() == baseLotData.getProductQuantity()) && !operationSpecData.getDetailProcessOperationType().equals("CUT"))
				{
					List<Map<String, Object>> sortJobData = MESLotServiceProxy.getLotServiceUtil().getSortJobDatabyLotName(baseLotData.getKey().getLotName());
					
					// Sorter Case
					if (isSorter && !(BaseProductSpecData.getUdfs().get("PRODUCTSPECTYPE").equalsIgnoreCase(baseLotData.getKey().getLotName().substring(0, 1))) && sortJobData.size() > 0)
					{
						Lot newLotData = MESLotServiceProxy.getLotServiceUtil().createNewLotforOLEDtoTPShopChange(eventInfo, baseLotData, carrierName, assignCarrierUdfs, lotListByProductList.size(),productList);
						newLotName = newLotData.getKey().getLotName();

						for (Map<String, Object> lotM : lotListByProductList)
						{
							String sLotName = CommonUtil.getValue(lotM, "LOTNAME");
							String sProductQuantity = CommonUtil.getValue(lotM, "PRODUCTQTY");

							srcLotList.add(sLotName);
							
							// TransferProductsToLot
							MESLotServiceProxy.getLotServiceUtil().transferProductsToLot(eventInfo, newLotData, portData, sLotName, sProductQuantity, deassignCarrierUdfs, productList);

							// Create MQC
							ProductSpec baseData = GenericServiceProxy.getSpecUtil().getProductSpec(baseLotData.getFactoryName(), baseLotData.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);

							if (CommonUtil.equalsIn(baseData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare"))
							{
								eventLog.info("ProductSpecGroup: " + baseData.getUdfs().get("PRODUCTSPECGROUP"));

								List<String> sProductList = MESLotServiceProxy.getLotServiceUtil().getProductListByElementList(productList);
								MESLotServiceProxy.getLotServiceUtil().createMQC(baseLotData, newLotData, sProductList, carrierName, eventInfo);
							}
						}

						// Initializing TrackOutLot.
						trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(newLotName);
						destLotList.add(newLotName);
					}
					else
					{
						// Not create newLot
						// API works automatically (NewCST Assaign)
						// Initializing TrackOutLot.
						trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(baseLotData.getKey().getLotName());

						// Update ActualSamplePosition
						MESLotServiceProxy.getLotServiceUtil().syncSamplePosition(eventInfo, baseLotData, productList);
						ProductSpec baseSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(baseLotData.getFactoryName(), baseLotData.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);

						//MQC
						if (CommonUtil.equalsIn(baseSpecData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare"))
						{
							String jobName = MESLotServiceProxy.getLotServiceUtil().getMQCJobName(baseLotData);

							if (!jobName.isEmpty())
							{
								// Update Carriername if CarrierName is null in MQC Job [CT_MQCPLANDETAIL table]
								ExtendedObjectProxy.getMQCPlanDetailService().UpdateCarrierChangedforMQCJob(eventInfo, baseLotData, jobName, carrierName);

								// Update position in MQC Job [CT_MQCPLANDETAIL , CT_MQCPLANDETAIL_EXTENDED]
								MESLotServiceProxy.getLotServiceUtil().UpdatePositionforMQCJob(eventInfo, baseLotData, jobName, productList);
							}
						}
					}
				}
				else
				{
					// Create New Lot.
					Lot newLotData = MESLotServiceProxy.getLotServiceUtil().createNewLot(eventInfo, baseLotData, carrierName, assignCarrierUdfs, lotListByProductList.size(), productList);
					newLotName = newLotData.getKey().getLotName();

					for (Map<String, Object> lotM : lotListByProductList)
					{
						String sLotName = CommonUtil.getValue(lotM, "LOTNAME");
						String sProductQuantity = CommonUtil.getValue(lotM, "PRODUCTQTY");

						srcLotList.add(sLotName);
						Lot sLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(sLotName);
						
						// TransferProductsToLot
						MESLotServiceProxy.getLotServiceUtil().transferProductsToLot(eventInfo, newLotData, portData, sLotName, sProductQuantity, deassignCarrierUdfs, productList);

						// Create MQC
						ProductSpec baseData = GenericServiceProxy.getSpecUtil().getProductSpec(baseLotData.getFactoryName(), baseLotData.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);

						if (CommonUtil.equalsIn(baseData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare"))
						{
							eventLog.info("ProductSpecGroup: " + baseData.getUdfs().get("PRODUCTSPECGROUP"));

							boolean ExistMQCJobDestLot = false;

							ExistMQCJobDestLot = MESLotServiceProxy.getLotServiceUtil().ExistMQCJobLot(newLotData);

							List<String> sProductList = MESLotServiceProxy.getLotServiceUtil().getProductListByElementList(productList);

							if (!ExistMQCJobDestLot)
								MESLotServiceProxy.getLotServiceUtil().createMQC(sLotData, newLotData, sProductList, carrierName, eventInfo);
							else
							{
								List<ProductP> destProductPSequence = MESLotServiceProxy.getLotInfoUtil().setProductPSequence(productList, newLotData.getKey().getLotName());
								List<String> dProductList = MESLotServiceProxy.getLotServiceUtil().convertProductList(destProductPSequence);

								MESLotServiceProxy.getLotServiceUtil().inheritSourceLotMQCJob(sLotData, newLotData, sProductList, dProductList, carrierName, eventInfo);
							}
						}
					}

					// Initializing TrackOutLot.
					trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(newLotName);

					// Cut Case
					if (operationSpecData.getDetailProcessOperationType().equals("CUT"))
					{
						Lot emptiedLot = MESLotServiceProxy.getLotServiceUtil().getLotData(SMessageUtil.getBodyItemValue(doc, "LOTNAME", true));
						
						// DeassignCarrier to EmptiedLot
						if (!StringUtils.equals(emptiedLot.getLotState(), "Released") || StringUtils.equals(emptiedLot.getLotState(), "Emptied"))
						{
							if (StringUtils.isNotEmpty(emptiedLot.getCarrierName()))
							{
								eventInfo.setEventName("DeassignCarrier");

								Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(emptiedLot.getCarrierName());

								List<ProductU> productUSequence = new ArrayList<ProductU>();

								DeassignCarrierInfo deassignCarrierInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(emptiedLot, carrierData, productUSequence);

								LotServiceProxy.getLotService().deassignCarrier(emptiedLot.getKey(), eventInfo, deassignCarrierInfo);
							}
						}
					}

					destLotList.add(newLotName);
				}
			}
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 2. FirstGlass Lot.  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (isFirstGlass)
			{
				List<Product> productDataList = MESLotServiceProxy.getLotServiceUtil().getProductListByCST(carrierName);

				if (productDataList.size() == productList.size())
				{
					// Initializing TrackOutLot.
					trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(lotNameByCarrier); 
				}
			}
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 3. ProductCount of Carrier is not Changed(Keep own ProductList) //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (lotDataByCarrier.getProductQuantity() == productList.size())
			{
				if (lotListByProductList.size() == 1)
				{
					// Initializing TrackOutLot.
					trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(lotNameByCarrier);
				}
				
				ProductSpec baseSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(baseLotData.getFactoryName(), baseLotData.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
				
				//MQC
				if (CommonUtil.equalsIn(baseSpecData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare"))
				{
					String jobName = MESLotServiceProxy.getLotServiceUtil().getMQCJobName(baseLotData);

					if (!jobName.isEmpty())
					{
						// Update CarrierName when CarrierName is changed in CT_MQCPLANDETAIL table
						ExtendedObjectProxy.getMQCPlanDetailService().UpdateCarrierChangedforMQCJob(eventInfo, baseLotData, jobName, carrierName);
					}
				}				
			}
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 4. ProductCount of Carrier is decreased. ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (lotDataByCarrier.getProductQuantity() > productList.size())
			{				
				if (StringUtils.equals(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), "PL") || MESLotServiceProxy.getLotServiceUtil().isExistAbortedProduct(productList))
				{
					Lot newLotData = MESLotServiceProxy.getLotServiceUtil().createNewLot(eventInfo, lotDataByCarrier, "", assignCarrierUdfs, 1, productList);
					newLotName = newLotData.getKey().getLotName();

					List<ProductP> productPSequence = MESLotServiceProxy.getLotInfoUtil().setProductPSequence(productList, lotNameByCarrier);

					srcLotList.add(lotDataByCarrier.getKey().getLotName());
					destLotList.add(newLotName);

					eventInfo = EventInfoUtil.makeEventInfo("TransferProduct", getEventUser(), getEventComment(), null, null);

					// TransferProductsToLot (Split)
					MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotDataByCarrier, newLotData.getKey().getLotName(), productList.size(), productPSequence, "Y", newLotData.getUdfs(), deassignCarrierUdfs, lotDataByCarrier.getUdfs());

					// Create MQC
					ProductSpec baseData = GenericServiceProxy.getSpecUtil().getProductSpec(baseLotData.getFactoryName(), baseLotData.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);

					if (CommonUtil.equalsIn(baseData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare"))
					{
						eventLog.info("ProductSpecGroup: " + baseData.getUdfs().get("PRODUCTSPECGROUP"));

						List<String> sProductList = MESLotServiceProxy.getLotServiceUtil().getProductListByElementList(productList);
						MESLotServiceProxy.getLotServiceUtil().createMQC(baseLotData, newLotData, sProductList, carrierName, eventInfo);
					}

					// Initializing TrackOutLot.
					trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(newLotData.getKey().getLotName());
					
					// Deassign
					Durable durCarrier = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
					
					if (StringUtils.equals(durCarrier.getDurableState(), "InUse"))
					{
						eventInfo = EventInfoUtil.makeEventInfo("Deassign", this.getEventUser(), this.getEventComment(), "", "");
						List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotDataByCarrier);

						DeassignCarrierInfo deassignCarrierInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotDataByCarrier, durCarrier, productUSequence);
						
						// Excute Deassign
						MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotDataByCarrier, deassignCarrierInfo, eventInfo);
					}
					
					// sync TPTJ product 
					if (StringUtil.in(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), "PL", "PS"))
						MESLotServiceProxy.getLotServiceUtil().syncTPTJData(eventInfo, srcLotList, destLotList);
				}
				else
				{
					Lot newLotData = MESLotServiceProxy.getLotServiceUtil().createNewLot(eventInfo, lotDataByCarrier, "", assignCarrierUdfs, 1, noReportProductList);
					newLotName = newLotData.getKey().getLotName();

					List<ProductP> productPSequence = MESLotServiceProxy.getLotInfoUtil().setProductPSequence(noReportProductList, lotNameByCarrier);

					srcLotList.add(lotDataByCarrier.getKey().getLotName());
					destLotList.add(newLotName);

					eventInfo = EventInfoUtil.makeEventInfo("TransferProduct", getEventUser(), getEventComment(), null, null);

					// TransferProductsToLot (Split)
					MESLotServiceProxy.getLotServiceImpl().transferProductsToLot(eventInfo, lotDataByCarrier, newLotData.getKey().getLotName(), noReportProductList.size(), productPSequence, "Y", newLotData.getUdfs(), deassignCarrierUdfs, lotDataByCarrier.getUdfs());

					// Create MQC
					ProductSpec baseData = GenericServiceProxy.getSpecUtil().getProductSpec(baseLotData.getFactoryName(), baseLotData.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);

					if (CommonUtil.equalsIn(baseData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare"))
					{
						eventLog.info("ProductSpecGroup: " + baseData.getUdfs().get("PRODUCTSPECGROUP"));

						List<String> sProductList = MESLotServiceProxy.getLotServiceUtil().getProductListByElementList(noReportProductList);
						MESLotServiceProxy.getLotServiceUtil().createMQC(baseLotData, newLotData, sProductList, "", eventInfo);
					}

					// Initializing TrackOutLot.
					trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(lotNameByCarrier);
				}
			}
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// 5. ProductCount of Carrier is increased. ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			else if (lotDataByCarrier.getProductQuantity() < productList.size())
			{
				destLotList.add(lotDataByCarrier.getKey().getLotName());
				
				for (Map<String, Object> lotM : lotListByProductList)
				{
					String sLotName = CommonUtil.getValue(lotM, "LOTNAME");

					if (!StringUtils.equals(lotNameByCarrier, sLotName))
					{
						srcLotList.add(sLotName);

						String sProductQuantity = CommonUtil.getValue(lotM, "PRODUCTQTY");

						List<ProductP> srcProductPSequence = MESLotServiceProxy.getLotInfoUtil().setProductPSequence(productList, sLotName);
						List<String> sProductList = MESLotServiceProxy.getLotServiceUtil().convertProductList(srcProductPSequence);

						List<ProductP> destProductPSequence = MESLotServiceProxy.getLotInfoUtil().setProductPSequence(productList, lotNameByCarrier);
						List<String> dProductList = MESLotServiceProxy.getLotServiceUtil().convertProductList(destProductPSequence);

						Lot sourceLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(sLotName);
						
						// Deassign source lot carrier。(When sorter operation PU cassette first reports LotProcessEnd) 
						if(StringUtils.equals(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), "PS") && sourceLotData.getProductQuantity() == sProductList.size() && !StringUtils.equals(sourceLotData.getUdfs().get("MERGEABLEFLAG"), "Y"))
						{
							if(StringUtil.isNotEmpty(sourceLotData.getCarrierName()))
							{
								Durable durCarrier = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sourceLotData.getCarrierName());
								
								if (StringUtils.equals(durCarrier.getDurableState(), "InUse"))
								{
									eventInfo = EventInfoUtil.makeEventInfo("Deassign", this.getEventUser(), this.getEventComment());
									List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(sourceLotData);

									DeassignCarrierInfo deassignCarrierInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(sourceLotData, durCarrier, productUSequence);
									
									// Excute Deassign
									MESLotServiceProxy.getLotServiceImpl().deassignCarrier(sourceLotData, deassignCarrierInfo, eventInfo);
								
								}
							}
						}
						
						if (StringUtils.equals(sourceLotData.getFactoryName(), "TP") && StringUtils.equals(sourceLotData.getUdfs().get("MERGEABLEFLAG"), "Y"))
						{
							eventLog.info("Dummy to TPGlassCST Case");
							// Dummy -> Normal
							MESLotServiceProxy.getLotServiceUtil().transferProductForDummy(eventInfo, sourceLotData, lotDataByCarrier, productList, deassignCarrierUdfs, portData, sProductQuantity);
						}
						else
						{
							//2021-04-07 ghhan Mantis 0000471
							if(isBufferCST && isSorter)
							{
								MESLotServiceProxy.getLotServiceUtil().transferProductsToLotForBuffer(eventInfo, lotDataByCarrier, portData, sLotName, sProductQuantity, deassignCarrierUdfs, productList);
							}
							else
							{
								// TransferProductsToLot (Merge)
								MESLotServiceProxy.getLotServiceUtil().transferProductsToLot(eventInfo, lotDataByCarrier, portData, sLotName, sProductQuantity, deassignCarrierUdfs, productList);
							}
						}
						
						ProductSpec baseData = GenericServiceProxy.getSpecUtil().getProductSpec(lotDataByCarrier.getFactoryName(), lotDataByCarrier.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
						
						// MQC
						if (CommonUtil.equalsIn(baseData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare"))
						{
							Lot srcLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(sLotName);
							eventLog.info("ProductSpecGroup: " + baseData.getUdfs().get("PRODUCTSPECGROUP"));

							boolean ExistMQCJobSrcLot = false;
							boolean ExistMQCJobDestLot = false;

							ExistMQCJobSrcLot = MESLotServiceProxy.getLotServiceUtil().ExistMQCJobLot(srcLotData);
							ExistMQCJobDestLot = MESLotServiceProxy.getLotServiceUtil().ExistMQCJobLot(lotDataByCarrier);

							// Check Exist Job (SrcLot & DestLot)
							if (ExistMQCJobSrcLot && ExistMQCJobDestLot)
							{
								eventLog.info("exist MQC Job Info by ScrLot, DestLot");
								// inherit MQCJob (SrcLot -> DestLot)
								MESLotServiceProxy.getLotServiceUtil().inheritSourceLotMQCJob(srcLotData, lotDataByCarrier, sProductList, dProductList, carrierName, eventInfo);
							}
							else if (ExistMQCJobSrcLot && !ExistMQCJobDestLot)
							{
								// Create MQC
								MESLotServiceProxy.getLotServiceUtil().createMQC(srcLotData, lotDataByCarrier, sProductList, carrierName, eventInfo);
							}
						}
					}
				}

				// Initializing TrackOutLot.
				trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(lotNameByCarrier);
			}
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			
			
			//Sorter Source CST ProductList>0 then Cancel Track In error
			try
			{
				if (StringUtils.equals(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), "PS"))
				{
					//Get sourceCSTSortJobData
					List<Map<String, Object>> sourceCSTSortJobData = MESLotServiceProxy.getLotServiceUtil().getOLEDTPSortJobData(baseLotData.getKey().getLotName(),carrierName);
					if(oldLotData!=null)
					{
						// Get ProcessFlowData
						ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(oldLotData);
						if(processFlowData.getProcessFlowType().equals("Main")&&sourceCSTSortJobData.size()>0)
						{
							if(StringUtils.equals(machineSpecData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Sorter) && StringUtils.equals(machineData.getUdfs().get("OPERATIONMODE"), "NORMAL"))
							{
								if(StringUtils.equals(durableData.getDurableType(), "OLEDGlassCST") || StringUtils.equals(durableData.getDurableType(), "TPGlassCST"))
								{							
									List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(trackOutLot.getKey().getLotName());
									
									eventInfo = EventInfoUtil.makeEventInfo("CancelTrackIn", getEventUser(), getEventComment(), "", "");
									eventInfo.setEventComment(eventInfo.getEventComment() + " :Sorter Source CST ProductList>0");

									//Return QTime Count
									ExtendedObjectProxy.getProductQTimeService().changeQTime(eventInfo, trackOutLot.getKey().getLotName(), trackOutLot.getFactoryName(), trackOutLot.getProcessFlowName(), trackOutLot.getProcessOperationName(), productList);
									// Cancel Track In
									trackOutLot = MESLotServiceProxy.getLotServiceImpl().cancelTrackIn(eventInfo, trackOutLot, productPGSRCSequence, assignCarrierUdfs, deassignCarrierUdfs, new ArrayList<ConsumedMaterial>(), carrierName);

									if (!StringUtils.equals(trackOutLot.getLotHoldState(), "Y") || MESLotServiceProxy.getLotServiceUtil().isExistAbortedAlarmFailProduct(productList))
									{							
										// Hold
										eventInfo = EventInfoUtil.makeEventInfo("Hold", getEventUser(), eventInfo.getEventComment(), "HOLD", "SYSTEM");
										MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, trackOutLot, new HashMap<String, String>());
										//}
									}
									
									// Make new elementBody for RetuenMsg(BC)
									Element newBody = generateReturnBodyTemplate(SMessageUtil.getBodyElement(doc), lotDataByCarrier);
									doc.getRootElement().getChild(SMessageUtil.Body_Tag).detach();
									doc.getRootElement().addContent(2,newBody);
									
									return doc;
								}
								
							}
						}
					}								
				}
			}
			catch(Exception ex)
			{
				log.info("Sorter Source CST ProductList>0 then Cancel Track In error");
			}
			
			
			// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// * Port Type is PL
			if (StringUtils.equals(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), "PL") || MESLotServiceProxy.getLotServiceUtil().isExistAbortedProduct(productList))
			{
				boolean CSTDeassignCheckFlag = true;
				
				List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(SMessageUtil.getBodyElement(doc), isInReworkFlow, eventInfo);

				// All sheet Lot in cutting
				if (operationSpecData.getDetailProcessOperationType().equals("CUT"))
				{
					boolean isAllProductSheet = true;
					CSTDeassignCheckFlag = false;
					
					List<Product> allUnScrappedProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(trackOutLot.getKey().getLotName());

					for (Product productData : allUnScrappedProductList)
					{
						if (!StringUtils.equals(productData.getProductType(), "Sheet"))
							isAllProductSheet = false;
					}

					if (isAllProductSheet)
					{
						String condition = "UPDATE LOT SET PRODUCTTYPE = 'Sheet' WHERE lotName = ?";
						Object[] bindSet = new Object[] { trackOutLot.getKey().getLotName() };
						kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().update(condition, bindSet);
					}
				}

				

				if (StringUtils.equals(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), "PL")) // PL
					eventInfo.setEventComment(eventInfo.getEventComment() + " :PortType is 'PL'");
				else if (MESLotServiceProxy.getLotServiceUtil().isExistAbortedProduct(productList)) // Abort
					eventInfo.setEventComment(eventInfo.getEventComment() + " :Exist 'B' as ProcessingInfo");

				//Return QTime Count
				ExtendedObjectProxy.getProductQTimeService().changeQTime(eventInfo, trackOutLot.getKey().getLotName(), trackOutLot.getFactoryName(), trackOutLot.getProcessFlowName(), trackOutLot.getProcessOperationName(), productList);
				// Cancel Track In
				eventInfo = EventInfoUtil.makeEventInfo("CancelTrackIn", getEventUser(), getEventComment(), "", "");
				trackOutLot = MESLotServiceProxy.getLotServiceImpl().cancelTrackIn(eventInfo, trackOutLot, productPGSRCSequence, assignCarrierUdfs, deassignCarrierUdfs, new ArrayList<ConsumedMaterial>(), carrierName);

				// Source Lot Deassign Check.
				Lot sourceLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotDataByCarrier.getKey().getLotName());

				if(CSTDeassignCheckFlag)
				{
					if (StringUtils.isNotEmpty(sourceLotData.getCarrierName()))
					{
						// Deassign
						Durable durCarrier = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
	
						if (!StringUtils.equals(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), "PS"))
						{
							if (StringUtils.equals(durCarrier.getDurableState(), "InUse"))
							{
								eventInfo = EventInfoUtil.makeEventInfo("DeassignCarrier", this.getEventUser(), this.getEventComment(), "", "");
								List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(sourceLotData);
	
								DeassignCarrierInfo deassignCarrierInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(sourceLotData, durCarrier, productUSequence);
	
								// Excute Deassign.
								MESLotServiceProxy.getLotServiceImpl().deassignCarrier(sourceLotData, deassignCarrierInfo, eventInfo);
							}
						}
					}
				}
				if (!StringUtils.equals(trackOutLot.getLotHoldState(), "Y") || MESLotServiceProxy.getLotServiceUtil().isExistAbortedAlarmFailProduct(productList))
				{
					/*
					if (StringUtils.equals(trackOutLot.getReworkState(), "InRework"))
					{
						// Complete Rework
						if (MESLotServiceProxy.getLotServiceUtil().checkCompleteReworkFlag(productList))
						{
							eventInfo = EventInfoUtil.makeEventInfo_completeRework("CompleteRework", getEventUser(), getEventComment(), "", "");
							trackOutLot = MESLotServiceProxy.getLotServiceUtil().completeRework(eventInfo, lotDataByCarrier, productList);
						}

						// Hold
						eventInfo = EventInfoUtil.makeEventInfo("Hold", getEventUser(), eventInfo.getEventComment(), "HOLD", "SYSTEM");
						MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, trackOutLot, new HashMap<String, String>());
					}
					else
					{*/
					// Hold
					eventInfo = EventInfoUtil.makeEventInfo("Hold", getEventUser(), eventInfo.getEventComment(), "HOLD", "SYSTEM");
					MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, trackOutLot, new HashMap<String, String>());
					//}
				}

				// Update Reserve Lot Info (State)
				MESLotServiceProxy.getLotServiceUtil().updateReservedLotStateByCancelTrackIn(trackOutLot.getKey().getLotName(), machineName, trackOutLot, eventInfo);

				// Make new elementBody for RetuenMsg(BC)
				Element newBody = generateReturnBodyTemplate(SMessageUtil.getBodyElement(doc), lotDataByCarrier);
				doc.getRootElement().getChild(SMessageUtil.Body_Tag).detach();
				doc.getRootElement().addContent(2,newBody);
				
				return doc;
			}
			// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			// //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("LASTAPPROVEDTIME", "");
			
			if (StringUtils.equals(trackOutLot.getFactoryName(), "TP"))
			{
				log.info("TP Glass1");
				String dummyGlassQty = MESLotServiceProxy.getLotServiceUtil().getDummyGlassQty(trackOutLot);
				if (!dummyGlassQty.isEmpty())
					udfs.put("DUMMYGLASSQTY", dummyGlassQty);   //0311 add by hk ***TP DummyGlassQty***
				log.info("TP Glass2");
			}
			
			if (!isBufferCST)
			{
				// for SPC
				if (operationSpecData.getUdfs().get("ISMAINOPERATION") != null && operationSpecData.getUdfs().get("ISMAINOPERATION").toString().equals("Y")) 
					MESLotServiceProxy.getLotServiceUtil().setProcessingMainMachine(trackOutLot.getKey().getLotName());

				// refined Lot logged in
				Lot beforeTrackOutLot = (Lot) ObjectUtil.copyTo(trackOutLot);
				List<ProductPGSRC> productPGSRCSequence = new ArrayList<ProductPGSRC>();

				// Set ProductPGSRCSequence
				if (isFirstGlass)
					productPGSRCSequence = MESLotServiceProxy.getLotServiceUtil().setProductPGSRCSequenceForFirstGlass(bodyElement, trackOutLot.getKey().getLotName(), isInReworkFlow, eventInfo);
				else if (operationSpecData.getDetailProcessOperationType().equals("AT"))
					productPGSRCSequence = MESLotServiceProxy.getLotServiceUtil().setProductPGSRCSequenceForArrayTest(bodyElement, isInReworkFlow, eventInfo);
				else if (operationSpecData.getDetailProcessOperationType().equals("RP"))
					productPGSRCSequence = MESLotServiceProxy.getLotServiceUtil().setProductPGSRCSequenceForRepair(bodyElement, isInReworkFlow, eventInfo);
				else
					productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(bodyElement, isInReworkFlow, eventInfo);

				// Extraction lotJudge
				lotJudge = MESLotServiceProxy.getLotServiceUtil().judgeLotGradeByProductList(productPGSRCSequence);

				// Get ProcessFlowData
				ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(trackOutLot);

				// BackUp EQP
				if (StringUtils.equals(processFlowData.getProcessFlowType(), "BackUp"))
				{
					// Q-time By BackUp
					MESLotServiceProxy.getLotServiceUtil().isBackUpTrackOut(trackOutLot, eventInfo);
					
					// BackUp Sampling
					MESLotServiceProxy.getLotServiceUtil().setSamplingListDataForBackUpEQP(eventInfo, trackOutLot, machineData, productList);
				}
				else
				{
					ExtendedObjectProxy.getProductQTimeService().moveInQTimeByLot(eventInfo, trackOutLot.getKey().getLotName(), trackOutLot.getFactoryName(), trackOutLot.getProcessFlowName(), trackOutLot.getProcessFlowVersion(), trackOutLot.getProcessOperationName(), trackOutLot.getProcessOperationVersion(), trackOutLot.getUdfs().get("RETURNFLOWNAME"));
				}
				// Q-time
				ExtendedObjectProxy.getProductQTimeService().monitorProductQTime(eventInfo, trackOutLot.getKey().getLotName(), machineName);
				reworkFlag = ExtendedObjectProxy.getProductQTimeService().doProductQTimeAction(doc, trackOutLot.getKey().getLotName()).equalsIgnoreCase("rework") ? "Y" : reworkFlag;
				ExtendedObjectProxy.getProductQTimeService().exitQTimeByProductElement(eventInfo, trackOutLot.getKey().getLotName(), trackOutLot.getFactoryName(), trackOutLot.getProcessFlowName(), trackOutLot.getProcessOperationName(), productList);
				
				//Q-time重新计时
				if (trackOutLot.getFactoryName().equalsIgnoreCase("ARRAY") || trackOutLot.getFactoryName().equalsIgnoreCase("TP"))
				{
					if (processFlowData.getProcessFlowType().equals("Rework") && operationSpecData.getProcessOperationType().equals("Production"))
					{
						int productionCount = 0;
						productionCount = this.getReworkProductionOperCount(trackOutLot);
						
						List<Map<String, Object>> beforeMainOper = this.getBeforeMainOperationNode(trackOutLot);

						if (beforeMainOper != null && beforeMainOper.size() > 0)
						{
							List<ProductQueueTime> QTimeDataList = ExtendedObjectProxy.getProductQTimeService().findProductQTimeByFromOper(trackOutLot.getKey().getLotName(),
									beforeMainOper.get(0).get("PROCESSFLOWNAME").toString(), beforeMainOper.get(0).get("NODEATTRIBUTE1").toString());
							
							EventInfo eventInfoForReworkQtime =(EventInfo) ObjectUtil.copyTo(eventInfo);
							eventInfoForReworkQtime.setEventName("LotProcessEndReEnterQtimeForRework");
							eventInfoForReworkQtime.setEventComment("Rework Production's Operation ReEnter Q-time");
							
							try 
							{
								this.modifyQtimeEnterTime(eventInfoForReworkQtime,productionCount,QTimeDataList,operationSpecData);
							} 
							catch (Exception e) 
							{
								log.info("modify Q-time EnterTime Exception,skip Modify");
							}
						}
					}
				}
				
				// Auto-Rewok Case#2 : NG
				reworkFlag = StringUtils.equals(trackOutLot.getReworkState(), "NotInRework") && lotJudge.equalsIgnoreCase("R") ? "Y" : reworkFlag;

				
				
				List<SampleLot> sampleLotList = new ArrayList<SampleLot>();

				// FirstGlass
				if (!StringUtils.equals(processFlowData.getProcessFlowType(), "MQC"))
				{
					// if lot is FirstGlass child Lot, NG
					if (!StringUtils.isEmpty(trackOutLot.getUdfs().get("FIRSTGLASSFLAG")) && !StringUtils.isEmpty(trackOutLot.getUdfs().get("JOBNAME")))
					{
						if (trackOutLot.getUdfs().get("FIRSTGLASSFLAG").equals("N") && lotJudge.equals("N"))
						{
							// Delete Sample Lot and Product (FirstGlass)
							MESLotServiceProxy.getLotServiceUtil().deleteSampleFirstGlass(eventInfo, trackOutLot.getUdfs().get("JOBNAME"), trackOutLot);
						}
					}

					if(srcLotList.size()>0)
					{
						sampleLotList = MESLotServiceProxy.getLotServiceUtil().deleteSrcSamplingDataReturn(eventInfo, srcLotList, productList, false);
					}
					else
					{
						sampleLotList = MESLotServiceProxy.getLotServiceUtil().deleteSamplingDataReturn(eventInfo, trackOutLot, productList, true);
					}
				}
				else //MQC
				{
					// Increase MQC UsedCount
					
					// AR-AMF-0022-01 -> ComponentInIndexer
					// MESLotServiceProxy.getLotServiceUtil().increaseMQCUsedCount(productNameList, baseLotData.getProcessFlowName(), baseLotData.getProcessFlowVersion(), baseLotData.getProcessOperationName(), baseLotData.getProcessOperationVersion());
					oldMqcSpecName = baseLotData.getProductSpecName();
				}

				
				
				// Check complete TPTJ Product Data
				MESLotServiceProxy.getLotServiceUtil().deleteTPTJProductData(eventInfo, trackOutLot); // TPTJCase

				// Set Data(MainReserveSkip) New - Lot AR-AMF-0030-01
				ExtendedObjectProxy.getMainReserveSkipService().syncMainReserveSkip(eventInfo, srcLotList, destLotList);
				
				// Initializing forceSampling Check
				SampleLot forceCheckData = new SampleLot();
				boolean forceFlag = false;
				boolean dummyProuctReserve = ExtendedObjectProxy.getDummyProductReserveService().checkDummyProductReserveData(trackOutLot.getKey().getLotName());
				boolean allDummyGlass = MESLotServiceProxy.getLotServiceUtil().allDummyGlass(trackOutLot);
				
				if (sampleLotList != null && sampleLotList.size() > 0)
					forceCheckData = sampleLotList.get(0);

				
				
				// if the Lot is FirstGlassLot, Pass
				if (StringUtils.isEmpty(trackOutLot.getUdfs().get("FIRSTGLASSFLAG")) )
				{
					if (StringUtils.isEmpty(trackOutLot.getUdfs().get("FIRSTGLASSALL")))
					{
						if (forceCheckData != null && StringUtils.isNotEmpty(forceCheckData.getForceSamplingFlag()))
						{
							forceFlag = true; // Set forceSample Flag
						}
						else if (!MESLotServiceProxy.getLotServiceUtil().checkMainReserveSkip(eventInfo, trackOutLot) // AR-AMF-0030-01
								&& !dummyProuctReserve && !allDummyGlass) // [V3_MES_121_037]TP NG,Dummy Supplement Scenario_V1.02
						{
							if (!StringUtils.equals(processFlowData.getProcessFlowType(), "BackUp"))
							{
								///////////////////////////////////////////////////////////////////////////////////////////////////////////////
								// Set Sampling Data //////////////////////////////////////////////////////////////////////////////////////////
								MESLotServiceProxy.getLotServiceUtil().setSamplingListData(eventInfo, trackOutLot, machineData, productList);
								///////////////////////////////////////////////////////////////////////////////////////////////////////////////
								///////////////////////////////////////////////////////////////////////////////////////////////////////////////
							}
						}
					}
				}

				// InlineSampling - Delete InlineSampling Data
				MESLotServiceProxy.getLotServiceUtil().deleteInlineSamplingData(eventInfo, trackOutLot, productList, machineName, true);

				// TFE CVD Chamber Sampling & TFE Down Sampling
				if ("EVA".equals(machineData.getMachineGroupName()))
				{
					// Get TFE Down Sampling Rule (joined TFOMPOLICY & POSMACHINEDOWNSAMPLE)
					List<Map<String, Object>> machineDownSamplePolicyList = MESLotServiceProxy.getLotServiceUtil().getMachineDownSamplePolicyList(trackOutLot.getFactoryName(),
							trackOutLot.getProcessFlowName(), trackOutLot.getProcessFlowVersion(), trackOutLot.getProcessOperationName(), trackOutLot.getProcessOperationVersion(), 
							machineName, "", "", "", "");
					
					if (machineDownSamplePolicyList != null && machineDownSamplePolicyList.size() > 0)
					{
						EventInfoExtended machineDownSampleEventInfo = new EventInfoExtended(eventInfo);
						machineDownSampleEventInfo.setEventComment("[MachineDown] System MachineDownSampling Normal. " + eventInfo.getEventComment());
						machineDownSampleEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
						
						MESLotServiceProxy.getLotServiceUtil().setMachineDownSamplingData(machineDownSampleEventInfo, machineData, trackOutLot, machineDownSamplePolicyList.get(0));
					}
					
					// Get TFE CVD Chamber Sampling Rule (joined TFOMPOLICY & POSCHAMBERSAMPLE)
					List<Map<String, Object>> chamberSamplePolicyList = MESLotServiceProxy.getLotServiceUtil().getChamberSamplePolicyList(trackOutLot.getFactoryName(), trackOutLot.getProcessFlowName(),
							trackOutLot.getProcessFlowVersion(), trackOutLot.getProcessOperationName(), trackOutLot.getProcessOperationVersion(), trackOutLot.getMachineName(), "", "", "", "");

					if (chamberSamplePolicyList != null && chamberSamplePolicyList.size() > 0)
					{
						EventInfoExtended chamberSampleEventInfo = new EventInfoExtended(eventInfo);
						chamberSampleEventInfo.setEventComment("[Chamber] System ChamberSampling Normal. " + eventInfo.getEventComment());
						chamberSampleEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
						
						for (Map<String, Object> chamberSamplePolicy : chamberSamplePolicyList)
						{
							if ("Y".equals(chamberSamplePolicy.get("TFESAMPLEFLAG")))
							{
								MESLotServiceProxy.getLotServiceUtil().setTFECVDChamberSamplingData(chamberSampleEventInfo, machineData, trackOutLot, chamberSamplePolicy);
							}
						}				
					}
				}
				
				/*
				// for DSP ////////////////////////////////////////////////////////////////////////////////
				try
				{
					if (StringUtils.equals(CommonUtil.getEnumDefValueStringByEnumName("FLAG_DSP_SetFlagStack"), "Y") && StringUtils.equals(baseLotData.getFactoryName(), "ARRAY"))
						SetFlagStack(productPGSRCSequence, baseLotData);
				}
				catch (Exception e)
				{
					eventLog.info("Error Occurred - SetFlagStack");
				}

				try
				{
					if (StringUtils.equals(CommonUtil.getEnumDefValueStringByEnumName("Switch_DSP_AOILotJudge"), "Y") && StringUtils.equals(baseLotData.getFactoryName(), "ARRAY"))
						AOILotJudge(baseLotData, lotNameByCarrier);
				}
				catch (Exception e)
				{
					eventLog.info("Error Occurred - AOILotJudge");
				}

				try
				{
					if (StringUtil.equals(baseLotData.getProductionType(), GenericServiceProxy.getConstantMap().Pord_MQC) || StringUtil.equals(baseLotData.getProductionType(), GenericServiceProxy.getConstantMap().Pord_Dummy))
					{
						String RunControlClearMachine = CommonUtil.getEnumDefValueStringByEnumNameAndEnumValue("DSP_RunControlClearMachine", machineName);
						
						if (StringUtil.isNotEmpty(RunControlClearMachine))
							MESMachineServiceProxy.getMachineServiceImpl().DryEtchByChamber(baseLotData, machineName, machineRecipeName);
					}
				}
				catch (Exception e)
				{
					eventLog.info("Error Occurred - DryEtchRecordLot");
				}*/

				// Mantis : 0000474
				// 2021-04-19	dhko	Update CT_MQCRUNCONTROL for DSP
				ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(oldLotData.getFactoryName(), oldLotData.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
				
				EventInfoExtended mqcRunControlEventInfo = new EventInfoExtended(eventInfo);
				mqcRunControlEventInfo.setEventName("TrackOut");
				mqcRunControlEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				
				int actualProductQty = 0;
				for (ProductPGSRC productPGSRC : productPGSRCSequence)
				{
					Map<String, String> productUdfs = productPGSRC.getUdfs();
					if (!StringUtils.equals(productUdfs.get("PROCESSINGINFO"), "B") && !StringUtils.equals(productUdfs.get("PROCESSINGINFO"), "S"))
					{
						actualProductQty ++;
					}
				}
				
				String machineLockFlag = StringUtil.EMPTY;
				if (CommonUtil.equalsIn(oldLotData.getProductionType(), "P", "E") && actualProductQty > 0)
				{
					// Update ACTUALPRODUCTQTY
					mqcRunControlEventInfo.setEventComment("Update ActualProductQty. " + eventInfo.getEventComment());
					MQCRunControl dataInfo = ExtendedObjectProxy.getMQCRunControlService().updateActualProductQty(mqcRunControlEventInfo, machineName, oldLotData.getProcessOperationName(), oldLotData.getProcessOperationVersion(), actualProductQty);
					
					if (dataInfo != null)
					{
						String condition = " WHERE MACHINENAME = ? AND MQCPROCESSFLOWNAME = ? AND MQCPROCESSFLOWVERSION = ? ";
						List<MQCRunControl> checkActualProduct = null;
						
						try
						{
							checkActualProduct = ExtendedObjectProxy.getMQCRunControlService().select(condition, new Object[] { machineName, dataInfo.getMqcProcessFlowName(), dataInfo.getMqcProcessFlowVersion() });
						}
						catch (Exception e)
						{
							checkActualProduct =null;
						}
						
						if (checkActualProduct.size()>0)
						{
							double ratio = 0;
							for (MQCRunControl checkActualProductRow : checkActualProduct)
							{
								int maxProductQtyByChamber = (int) checkActualProductRow.getMaxProductQtyByChamber().intValue();
								int chamberQty = (int) checkActualProductRow.getChamberQty().intValue();
								int actualProductRow = (int) checkActualProductRow.getActualProductQty().intValue();
								ratio = ratio + new BigDecimal((float)actualProductRow / (maxProductQtyByChamber * chamberQty)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
							}
							if (ratio >= 1)
							{
								machineLockFlag = "Y";
							}
						}
					}
				}
				else if (CommonUtil.equalsIn(productSpecData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare") && actualProductQty > 0)
				{
					// Update MQCPROCESSQTY
					mqcRunControlEventInfo.setEventComment("Update MQCProcessQty. " + eventInfo.getEventComment());
					
					List<MQCRunControl> runControlDataList = ExtendedObjectProxy.getMQCRunControlService()
							.updateMQCProcessQty(mqcRunControlEventInfo, machineName, oldLotData.getProcessFlowName(),
									oldLotData.getProcessFlowVersion(), actualProductQty);
					
					if (runControlDataList != null && runControlDataList.size() > 0) 
					{
						int actualMQCQty = 0;
						double needMQCQty = 0;
						
						for (MQCRunControl runControlData : runControlDataList) 
						{
							actualMQCQty = (int) runControlData.getMqcProcessQty().intValue();
							int maxMQCQtyByChamber = (int) runControlData.getMaxMQCQtyByChamber().intValue();
							int chamberQty = (int) runControlData.getChamberQty().intValue();
							int maxProductQtyByChamber = (int) runControlData.getMaxProductQtyByChamber().intValue();
							int actualProductRow = (int) runControlData.getActualProductQty().intValue();
							double ratio = new BigDecimal((float)actualProductRow / (maxProductQtyByChamber * chamberQty)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
							
							needMQCQty = needMQCQty + ratio * maxMQCQtyByChamber * chamberQty;
						}
						if (actualMQCQty > needMQCQty)
						{
							mqcRunControlEventInfo.setEventComment(
									"Initial ActualProductQty/MQCProcessQty. " + eventInfo.getEventComment());
							
							ExtendedObjectProxy.getMQCRunControlService().initialQtyByFlow(mqcRunControlEventInfo,
									machineName, oldLotData.getProcessFlowName(), oldLotData.getProcessFlowVersion());
							
							machineLockFlag = "N";
						}
					}
				}
				
				if (!StringUtil.isEmpty(machineLockFlag))
				{
					mqcRunControlEventInfo.setEventComment("ActualProductQty/MQCProcessQty Over. " + eventInfo.getEventComment());
					MESMachineServiceProxy.getMachineServiceImpl().changeMachineLockFlag(mqcRunControlEventInfo, machineData, machineLockFlag);
				}
				
				
				////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				// Track OUT
				Lot afterTrackOutLot;
				
				// AR-AMF-0029-01
				String processingInfo = this.makeProcessingInfo(doc, durableData);
				eventInfo.setEventComment("LotProcessEnd ActualRunSlot : " + processingInfo);
				
				// AOI Repair Skip - If the Lot judge is P then goes to repair operation else skip.
				if ((StringUtils.equals(trackOutLot.getFactoryName(), "ARRAY") ||StringUtils.equals(trackOutLot.getFactoryName(), "TP"))
						&& StringUtils.equals(operationSpecData.getDetailProcessOperationType(), "RP"))
					trackOutLot = getNextOperationForRepairSkip(eventInfo, trackOutLot, lotJudge, true, forceFlag,isFirstGlass);
				
				if (forceFlag)
				{
					// Force Sampling TrackOut
					afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().trackOutLotForceSampling(eventInfo, trackOutLot, portData, carrierName, lotJudge, machineName, "", productPGSRCSequence, assignCarrierUdfs, deassignCarrierUdfs, udfs);
				}
				else 
				{
					// Normal TrackOut
					afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().trackOutLotWithSampling(eventInfo, trackOutLot, portData, carrierName, lotJudge, machineName, "", productPGSRCSequence, assignCarrierUdfs, deassignCarrierUdfs, udfs);
				}
				
				// Update Reserve Lot Info (State)
				MESLotServiceProxy.getLotServiceUtil().updateReservedLotStateByTrackOut(trackOutLot.getKey().getLotName(), machineName, trackOutLot, eventInfo);

				////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

				
				
				
				
				
				
				ProcessOperationSpecKey specKey = new ProcessOperationSpecKey(afterTrackOutLot.getFactoryName(), afterTrackOutLot.getProcessOperationName(), afterTrackOutLot.getProcessOperationVersion());
				ProcessOperationSpec afterOperSpec = ProcessOperationSpecServiceProxy.getProcessOperationSpecService().selectByKey(specKey);
				ProcessFlow currentFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(afterTrackOutLot);
				
				// Delete before operation sampling/repair data all
				if (afterOperSpec.getProcessOperationType().equals("Production") && 
						afterOperSpec.getUdfs().get("ISMAINOPERATION").toString().equals("Y") && afterOperSpec.getProcessOperationGroup().equals("Normal"))
				{
					if(srcLotList.size()>0)
					{
						MESLotServiceProxy.getLotServiceUtil().deleteSrcLotBeforeOperationSamplingData(eventInfo, afterTrackOutLot, srcLotList);
					}
					else
					{
						MESLotServiceProxy.getLotServiceUtil().deleteBeforeOperationSamplingData(eventInfo, afterTrackOutLot);
					}
				}
					
				// Update reworkCount
				MESLotServiceProxy.getLotServiceUtil().setProductReworkCount(oldLotData, processedProductList, operationSpecData);

				// Set NextOper ReworkFlag. 
				if( StringUtils.equals(currentFlowData.getProcessFlowType(), "Rework"))
				{
					MESLotServiceProxy.getLotServiceUtil().setNextOperReworkFlag(eventInfo, afterTrackOutLot);
				}
				
				// Set Data(Sample, FutureAction) Transfer Product
				MESLotServiceProxy.getLotServiceUtil().transferProductSyncData(eventInfo, srcLotList, destLotList);

				// InlineSampling - Set InlineSampling Data
				MESLotServiceProxy.getLotServiceUtil().setInlineSamplingListData(eventInfo, afterTrackOutLot);

				// Complete Rework
				afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().completeRework(eventInfo, beforeTrackOutLot, afterTrackOutLot, productList);

				// FirstGlass
				afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().excuteFirstGlass(eventInfo, beforeTrackOutLot, afterTrackOutLot, reworkFlag);
				
				// Hold (Product Future Hold)
				 MESLotServiceProxy.getLotServiceUtil().executePostActionByPrdocutFutureHold(eventInfo, beforeTrackOutLot,afterTrackOutLot, productNameList);

				// Hold & Skip
				afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().executePostAction(eventInfo, beforeTrackOutLot, afterTrackOutLot, forceFlag);
				
				/*//Offline Review Skip 2020-07-17 request by yueke
				if(afterOperSpec.getDetailProcessOperationType().equals("VIEW"))
					afterTrackOutLot = OfflineRSSkip(afterTrackOutLot);
				*/
				
				// ReviewStation Skip by Lot count for ( Array and notInRework Flow and not forceSampling )
				if (afterTrackOutLot.getFactoryName().equalsIgnoreCase("ARRAY") && afterTrackOutLot.getReworkState().equalsIgnoreCase("NotInRework") && !forceFlag)
					afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().exceutePostShipOfReviewStation(eventInfo, beforeTrackOutLot, afterTrackOutLot);

				// After Skip, Re-check FirstGlass to merge
				if (StringUtils.equals(afterTrackOutLot.getLastEventName(), "Skip") && StringUtils.equals(afterTrackOutLot.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_NotOnHold))
					afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().excuteFirstGlass(eventInfo, beforeTrackOutLot, afterTrackOutLot, reworkFlag);

				// After LotProcessEnd, CheckProcessGlass
				afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().checkProcessGlass(eventInfo, doc, trackOutLot, afterTrackOutLot, durableData, slotSel, machineName, machineRecipeName, machineData, operationSpecData, oldMqcSpecName, forceFlag);

				// When ProductJudge is S → Lot Hold.
				afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().checkProductJudgeAndHold(eventInfo, productList, afterTrackOutLot);
				
				// Check productRecipe #Mantis 0000360
			     afterTrackOutLot = checkProductRecipe(eventInfo, afterTrackOutLot, productRecipeList, machineRecipeName);
			
				// Hold Lot about Exposure
				if (CommonUtil.equalsIn(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Photo,"TPPhoto"))
					afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().validateAndHoldLotAboutExposure(eventInfo, beforeTrackOutLot, afterTrackOutLot);

				// Insert PhotoRecipe, PhotoMachine Info
				if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Photo))
					insertPhotoData(eventInfo, machineName, machineRecipeName, productElementList);
				
				//Update Recipe LastTrackOutTimekey Modify 2020-07-24 ghhan
				if(!isSorter)
				{
					if (MESRecipeServiceProxy.getRecipeServiceUtil().RMSFlagCheck("E", machineName, machineRecipeName, "", lotDataByCarrier.getProductSpecName(), lotDataByCarrier.getProcessFlowName(), lotDataByCarrier.getProcessOperationName(), productOffset))
					{
						MESRecipeServiceProxy.getRecipeServiceUtil().updateTrackOutTime(machineName, machineRecipeName, eventInfo);
					}
				}

				if (allDummyGlass)
				{
					if (StringUtils.equals(trackOutLot.getFactoryName(), "TP"))
					{
						boolean assignDummyCheck = ExtendedObjectProxy.getDummyProductAssignService().checkDummyProductAssign(productList);
						
						if (assignDummyCheck)
						{
							// Recovery DummyProductAssign ReturnInfo
							log.info("Recovery DummyGlass Spec");
							
							Lot oriLotData = (Lot)ObjectUtil.copyTo(trackOutLot);
							trackOutLot = MESLotServiceProxy.getLotServiceUtil().RecoverySpec(eventInfo, productList, trackOutLot);

							MESWorkOrderServiceProxy.getProductRequestServiceImpl().decrementQuantity(eventInfo, oriLotData, productList.size());
							MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementQuantity(eventInfo, trackOutLot, productList.size());
						}
					}
				}

				if(dummyProuctReserve)
					ExtendedObjectProxy.getDummyProductReserveService().checkProcessingFlag(eventInfo, beforeTrackOutLot, afterTrackOutLot);
				
				// AR-Photo-0005-01 : Verify that you have performed on a modelled unit
				afterTrackOutLot = validateAbnormalUnitInfo(eventInfo, machineName, afterTrackOutLot, productElementList);
				
				// AR-Photo-0028-01 
				afterTrackOutLot = validateLotHoldAction(eventInfo, beforeTrackOutLot, afterTrackOutLot);
				
				// CO_INT-0032-01 : ArrayTestYieldTarget > sum(NGPANELQTY) / sum(MEASUREPANELQTY) * 100
				if (StringUtils.equals(machineData.getMachineGroupName(), "ArrayTest"))
					afterTrackOutLot = checkYield(eventInfo, productElementList, afterTrackOutLot);
				
				//Clear ELANFC
				if(StringUtils.equals(operationSpecData.getDetailProcessOperationType(), "ELA"))
				{
					afterTrackOutLot=MESLotServiceProxy.getLotServiceUtil().clearELANFC(afterTrackOutLot);
				}

				// Auto Shipping Action
				MESLotServiceProxy.getLotServiceUtil().autoShipLotAction(eventInfo, afterTrackOutLot);
				
				// TP Offset
				if (machineData.getFactoryName().equals("TP") && StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Photo)
						&& operationSpecData.getUdfs().get("LAYERNAME").toString().equals("PEP1"))
				{
					eventInfo.setEventName("UpdateOffset");
					eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
					String machineOffset = machineSpecData.getUdfs().get("OFFSETID").toString();
					for(Element productInfo : productList)
					{
						String productName = productInfo.getChildText("PRODUCTNAME");
						Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
						
						kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
						setEventInfo.getUdfs().put("OFFSET", machineOffset);
						
						MESProductServiceProxy.getProductServiceImpl().setEvent(productData, setEventInfo, eventInfo);
					}
				}
				
				// Delete futureAction 2020-11-16
				MESLotServiceProxy.getLotServiceUtil().deleteFutureActionData(eventInfo, sampleLotList, beforeTrackOutLot, afterTrackOutLot);
				
				// Delete productFutureAction 
				MESLotServiceProxy.getLotServiceUtil().deleteProductFutureActionData(eventInfo, sampleLotList, beforeTrackOutLot, afterTrackOutLot);
				
				// Delete OriginalProductInfo data after SortFlow
				MESLotServiceProxy.getLotServiceUtil().deleteOriginalProductInfo(eventInfo, beforeTrackOutLot, afterTrackOutLot, productNameList);

				// [V3_MES_121_004]DSP Run Control_V1.02
				MESLotServiceProxy.getLotServiceUtil().runControlResetCountAndSendMail(eventInfo, machineName, beforeTrackOutLot, machineRecipeName, productRecipeList);
				
				//BaseLineFlag = 'Y'
				CheckProductBaseLineFlag(productList,machineName,beforeTrackOutLot);
				
				//Start IdleTime超时后RUN指定Recipe后重置CONTROLSWITCH（CT_MACHINEIDLEBYCHAMBER）  Modify by _hk
				List<Map<String, Object>> idleTimeByChamberData=getIDLETimeByChamber(machineName);
				if(idleTimeByChamberData!=null&&idleTimeByChamberData.size()>0)
				{
					idleTimeByCharmberEtch(machineName,beforeTrackOutLot,productRecipeList);
				}
				List<Map<String, Object>> idleTimeByTPCVD=getIDLETimeByTPCVD(machineName);
				if(idleTimeByTPCVD!=null&&idleTimeByTPCVD.size()>0)
				{
					idleTimeByCharmberTPCVD(machineName, beforeTrackOutLot,productRecipeList);
				}
				//End IdleTime超时
			
				log.info("Track Out Sucesse ! (Lot = '" + afterTrackOutLot.getKey().getLotName() + "') ***************************");

				// Make new elementBody for RetuenMsg(BC)
				Element newBody = generateReturnBodyTemplate(SMessageUtil.getBodyElement(doc), afterTrackOutLot);
				doc.getRootElement().getChild(SMessageUtil.Body_Tag).detach();
				doc.getRootElement().addContent(2,newBody);

		
				// Lot without MQCJob is sent to MQC Bank and Delete MQC Job
				ProductSpec baseData = GenericServiceProxy.getSpecUtil().getProductSpec(afterTrackOutLot.getFactoryName(), afterTrackOutLot.getProductSpecName(), GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
				if (CommonUtil.equalsIn(baseData.getUdfs().get("PRODUCTSPECGROUP"), "MQC", "MQCPrepare"))
				{
					for (Map<String, Object> lotM : lotListByProductList)
					{
						String sLotName = CommonUtil.getValue(lotM, "LOTNAME");

						if (!StringUtils.equals(afterTrackOutLot.getKey().getLotName(), sLotName))
						{
							Lot srcLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(sLotName);
							MESLotServiceProxy.getLotServiceUtil().SentToMQCBankWithoutMQCproduct(eventInfo, srcLotData);
						}
					}
					MESLotServiceProxy.getLotServiceUtil().SentToMQCBankWithoutMQCproduct(eventInfo, afterTrackOutLot);
				}
				ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(beforeTrackOutLot.getProductRequestName());
				//SAP
				try
				{
					String sapFlag=CommonUtil.getEnumDefValueStringByEnumName("SAPFLAG");
					if(StringUtil.isNotEmpty(sapFlag)&&StringUtil.equals(sapFlag, "Y")&&
							StringUtils.isNotEmpty(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
					{
						SuperProductRequest superWO = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
						
						MESConsumableServiceProxy.getConsumableServiceUtil().trackOutERPBOMReportForOnline(eventInfo, oldLotData, superWO, machineName, productPGSRCSequence);
					}
				}
				catch(Exception x)
				{
					eventLog.info("SAP Report Error");
				}

								
			}
			else
			{
				// TrackOut to Current Operation for BufferCST
				List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(machineData, trackOutLot.getKey().getLotName(), productList);

				MakeLoggedOutInfo makeLoggedOutInfo = MESLotServiceProxy.getLotInfoUtil().makeLoggedOutInfo(trackOutLot, trackOutLot.getAreaName(), assignCarrierUdfs, carrierName, "",
						deassignCarrierUdfs, StringUtils.isEmpty(lotJudge) ? trackOutLot.getLotGrade() : lotJudge, machineName, "", // machineRecipe
						trackOutLot.getNodeStack(), trackOutLot.getProcessFlowName(), trackOutLot.getProcessFlowVersion(), trackOutLot.getProcessOperationName(),
						trackOutLot.getProcessOperationVersion(), productPGSRCSequence, "N", "", udfs);

				eventInfo.setEventName("TrackOut");
				
				// Track Out Lot
				Lot afterTrackOutLot = MESLotServiceProxy.getLotServiceImpl().trackOutLot(eventInfo, trackOutLot, makeLoggedOutInfo);
				
				Element newBody = generateReturnBodyTemplate(SMessageUtil.getBodyElement(doc), afterTrackOutLot);
				doc.getRootElement().getChild(SMessageUtil.Body_Tag).detach();
				doc.getRootElement().addContent(2,newBody);

			}
		}
		catch (CustomException ce)
		{
			Element newBody = generateReturnBodyTemplate(SMessageUtil.getBodyElement(doc));
			newBody.getChild("RESULT").setText(ce.errorDef.getErrorCode());
			newBody.getChild("RESULTDESCRIPTION").setText(ce.errorDef.getLoc_errorMessage());

			doc.getRootElement().getChild(SMessageUtil.Body_Tag).detach();
			doc.getRootElement().addContent(2, newBody);
			
			greenFrameServiceProxy.getTxDataSourceManager().rollbackAllTransactions();
		}
		catch (Exception e)
		{
			Element newBody = generateReturnBodyTemplate(SMessageUtil.getBodyElement(doc));
			newBody.getChild("RESULT").setText("UndefinedCode");
			newBody.getChild("RESULTDESCRIPTION").setText(e.getMessage());

			doc.getRootElement().getChild(SMessageUtil.Body_Tag).detach();
			doc.getRootElement().addContent(2, newBody);
			
			greenFrameServiceProxy.getTxDataSourceManager().rollbackAllTransactions();
		}

		return doc;
	}


	
	
	private void CheckProductBaseLineFlag(List<Element> productList, String machineName, Lot beforeTrackOutLot)throws CustomException
	{
		//Ct_ValicationProduct
		log.info("BaseLineFlag start by ProductList");
		String ProductSpec = beforeTrackOutLot.getProductSpecName();
		String currentFlowName = beforeTrackOutLot.getProcessFlowName();
		String currentOperationName = beforeTrackOutLot.getProcessOperationName();
		String currentMachineName = machineName;
		for(Element productInfo : productList)
		{
			String productName = productInfo.getChildText("PRODUCTNAME");
			String sql= "SELECT PRODUCTNAME,ENGOPERATIONNAME,ENGMACHINENAME,TOFLOWNAME,SAMPLEOPERATIONNAME "
					+ "FROM CT_VALICATIONPRODUCT WHERE 1=1 "
					+ "AND  PRODUCTNAME = :PRODUCTNAME  "
					+ "AND PRODUCTSPECNAME = :PRODUCTSPECNAME "
					+ "AND PROCESSFLOWNAME = :PROCESSFLOWNAME "
					+ "AND ENGOPERATIONNAME = :ENGOPERATIONNAME "
					+ "AND ENGMACHINENAME= :ENGMACHINENAME";
		
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("PRODUCTNAME",productName);
			bindMap.put("PRODUCTSPECNAME",ProductSpec);
			bindMap.put("PROCESSFLOWNAME",currentFlowName);
			bindMap.put("ENGOPERATIONNAME",currentOperationName);
			bindMap.put("ENGMACHINENAME",currentMachineName);
			
			List<Map<String, Object>> result;
			try
			{
				result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			}
			catch (FrameworkErrorSignal fe)
			{
				result = null;
				throw new CustomException("SYS-9999", fe.getMessage());
			}
			if(result != null && result.size() > 0)
			{
				String sql2 = "UPDATE CT_VALICATIONPRODUCT set BASELINEFLAG = 'Y' "
						+ "WHERE PRODUCTNAME = :PRODUCTNAME  "
						+ "AND PRODUCTSPECNAME = :PRODUCTSPECNAME "
						+ "AND PROCESSFLOWNAME = :PROCESSFLOWNAME "
						+ "AND ENGOPERATIONNAME = :ENGOPERATIONNAME "
						+ "AND ENGMACHINENAME= :ENGMACHINENAME";
				GenericServiceProxy.getSqlMesTemplate().update(sql2, bindMap);
			}	
		}
	}



	private Element generateReturnBodyTemplate(Element bodyElement) throws CustomException
	{
		Element body = new Element(SMessageUtil.Body_Tag);

		JdomUtils.addElement(body, "MACHINENAME", bodyElement.getChildText("MACHINENAME"));
		JdomUtils.addElement(body, "LOTNAME", bodyElement.getChildText("LOTNAME"));
		JdomUtils.addElement(body, "CARRIERNAME", bodyElement.getChildText("CARRIERNAME"));
		JdomUtils.addElement(body, "PORTNAME", bodyElement.getChildText("PORTNAME"));
		JdomUtils.addElement(body, "PORTTYPE", bodyElement.getChildText("PORTTYPE"));
		JdomUtils.addElement(body, "PORTUSETYPE", bodyElement.getChildText("PORTUSETYPE"));
		JdomUtils.addElement(body, "RESULT", "");
		JdomUtils.addElement(body, "RESULTDESCRIPTION", "");

		return body;
	}

	private Element generateReturnBodyTemplate(Element bodyElement, Lot lotData) throws CustomException
	{
		Element body = new Element(SMessageUtil.Body_Tag);

		JdomUtils.addElement(body, "MACHINENAME", bodyElement.getChildText("MACHINENAME"));
		JdomUtils.addElement(body, "LOTNAME", lotData.getKey().getLotName());
		JdomUtils.addElement(body, "CARRIERNAME", bodyElement.getChildText("CARRIERNAME"));
		JdomUtils.addElement(body, "PORTNAME", bodyElement.getChildText("PORTNAME"));
		JdomUtils.addElement(body, "PORTTYPE", bodyElement.getChildText("PORTTYPE"));
		JdomUtils.addElement(body, "PORTUSETYPE", bodyElement.getChildText("PORTUSETYPE"));
		JdomUtils.addElement(body, "RESULT", "OK");
		JdomUtils.addElement(body, "RESULTDESCRIPTION", "");

		return body;
	}

	/*
	public void updateMachineUdfs(String machineForChange, Lot afterTrackOutLot, Lot baseLotData, EventInfo eventInfo) throws CustomException
	{
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineForChange);
		EventInfo setLastProduction = EventInfoUtil.makeEventInfo("TrackOut", this.getEventUser(), this.getEventComment(), null, null);

		Map<String, String> machineUdfs = machineData.getUdfs();
		machineUdfs.put("LASTPROCESSENDTIME", TimeStampUtil.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		SetEventInfo setEventInfo = MESMachineServiceProxy.getMachineInfoUtil().setEventInfo(machineData.getUdfs());
		MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setEventInfo, setLastProduction);
	}*/

	private void UnpackerProcessEnd(Document doc)
	{
		Document copyDoc = (Document) doc.clone();

		SMessageUtil.setHeaderItemValue(copyDoc, "MESSAGENAME", "UnpackerProcessEnd");

		try
		{
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("PEXsvr"), copyDoc, "LocalSender");
		}
		catch (Exception ex)
		{
			eventLog.warn("PEX Report Failed!");
		}
	}
	
	/*
	private void AOILotJudge(Lot baseLotData, String lotName)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT M.MACHINEGROUPNAME FROM MACHINESPEC M     WHERE 1=1     AND M.FACTORYNAME = :FACTORYNAME ");
		sql.append("     AND M.DETAILMACHINETYPE = 'MAIN'     AND M.MACHINENAME = :MACHINENAME     ORDER BY M.MACHINENAME ASC ");

		Map<String, Object> args = new HashMap<>();

		args.put("FACTORYNAME", baseLotData.getFactoryName());
		args.put("MACHINENAME", baseLotData.getMachineName());

		List result = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), args);

		if (result.get(0).toString().equals("{MACHINEGROUPNAME=AutoMAC}") || result.get(0).toString().equals("{MACHINEGROUPNAME=ParticleCounter}"))
		{
			StringBuilder insql = new StringBuilder();
			insql.append("INSERT INTO CT_AOILOT (LOTNAME,FACTORYNAME,LOTJUDGE,EVENTUSER,EVENTTIME,TIMEKEY,MACHINENAME) ");
			insql.append("      VALUES ( :LOTNAME,:FACTORYNAME,'N',:EVENTUSER,:EVENTTIME,:TIMEKEY,:MACHINENAME) ");

			Map<String, Object> arg = new HashMap<String, Object>();

			String CurrentTime = TimeStampUtil.getCurrentTime(TimeStampUtil.FORMAT_DEFAULT);
			String timeKey = TimeStampUtil.getCurrentEventTimeKey();
			arg.put("LOTNAME", lotName);
			arg.put("FACTORYNAME", baseLotData.getFactoryName());
			arg.put("MACHINENAME", baseLotData.getMachineName());
			arg.put("EVENTUSER", baseLotData.getLastEventUser());
			arg.put("EVENTTIME", CurrentTime);
			arg.put("TIMEKEY", timeKey);

			GenericServiceProxy.getSqlMesTemplate().update(insql.toString(), arg);

			kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("AutoLotJudge-N", getEventUser(), getEventComment(), "", "");
			LotServiceProxy.getLotService().setEvent(baseLotData.getKey(), eventInfo, setEventInfo);
		}
	}*/

	private boolean checkAbort(List<Element> productElementList) throws CustomException
	{
		boolean result = false;

		boolean abnormalFlag = false;
		List<String> targetLotList = new ArrayList<String>();

		// detect abnormal
		for (Element productElement : productElementList)
		{
			String flag = SMessageUtil.getChildText(productElement, "PROCESSINGINFO", false);

			if (StringUtils.equals(flag, "B"))
			{
				abnormalFlag = true;
			}
		}

		if (abnormalFlag)
		{
			for (Element productElement : productElementList)
			{
				String productName = SMessageUtil.getChildText(productElement, "PRODUCTNAME", true);
				String processResult = SMessageUtil.getChildText(productElement, "PROCESSINGINFO", false);

				if (processResult.equals("N") || processResult.equals("F"))
				{
					// processed flag
					Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

					if (!targetLotList.contains(productData.getLotName()))
					{
						// only gather active Lot
						targetLotList.add(productData.getLotName());
					}
				}

				result = true;
			}
		}
	
		return result;
	}
	
	/**
	 * 
	 * AR-Photo-0005-01
	 * Verify that you have performed on a modelled unit
	 * 
	 * @author aim_dhko
	 * @return 
	 */
	private Lot validateAbnormalUnitInfo(EventInfo eventInfo, String machineName, Lot afterTrackOutLot, List<Element> productElementList)
	{
		try
		{
			String condition = "WHERE MACHINENAME = ? ";
			List<AbnormalUnitInfo> abnormalUnitInfoList = ExtendedObjectProxy.getAbnormalUnitInfoService().select(condition, new Object[] { machineName });
			
			Map<String, String> actionMap = new HashMap<String, String>();
			
			for (Element productElement : productElementList)
			{
				String processingFlag = productElement.getChildText("PROCESSINGFLAG");
				if(StringUtils.isEmpty(processingFlag))
				{
					StringBuilder mailMessage = new StringBuilder();
					mailMessage.append("<pre>=======Verify that you have performed on a modelled unit=======</pre>");
					mailMessage.append("<pre>=======================================================</pre>");
					mailMessage.append("<pre>- EQP ID : ").append(machineName).append("</pre>");
					mailMessage.append("<pre>- ProductName : ").append(productElement.getChildText("PRODUCTNAME")).append("</pre>");
					mailMessage.append("<pre>- Cause : <PROCESSINGINFOFLAG> is empty.</pre>");
					mailMessage.append("<pre>=======================================================</pre>");
					
					actionMap.put("HOLDCAUSE", MessageFormat.format("<PROCESSINGINFOFLAG> is empty. ProductName={0}", productElement.getChildText("PRODUCTNAME")));
					actionMap.put("EMAILMESSAGE", mailMessage.toString());
					
					break;
				}
				
				char[] processingFlags = processingFlag.toCharArray();
				
				for (AbnormalUnitInfo abnormalUnitInfo : abnormalUnitInfoList) 
				{
					int position = abnormalUnitInfo.getPosition();
					if(position > processingFlags.length)
					{
						StringBuilder mailMessage = new StringBuilder();
						mailMessage.append("<pre>=======Verify that you have performed on a modelled unit=======</pre>");
						mailMessage.append("<pre>=======================================================</pre>");
						mailMessage.append("<pre>- EQP ID : ").append(machineName).append("</pre>");
						mailMessage.append("<pre>- UnitName : ").append(abnormalUnitInfo.getUnitName()).append("</pre>");
						mailMessage.append("<pre>- Position : ").append(position).append("</pre>");
						mailMessage.append("<pre>- ProductName : ").append(productElement.getChildText("PRODUCTNAME")).append("</pre>");
						mailMessage.append("<pre>- <PROCESSINGINFOFLAG> : ").append(processingFlag).append("</pre>");
						mailMessage.append("<pre>- Cause : Size of <PROCESSINGINFOFLAG> smaller then position.</pre>");
						mailMessage.append("<pre>=======================================================</pre>");
						
						actionMap.put("HOLDCAUSE", MessageFormat.format("Size of <PROCESSINGINFOFLAG> smaller then position. ProductName={0} Position={1} <PROCESSINGINFOFLAG>={2}", productElement.getChildText("PRODUCTNAME"), position, processingFlag));
						actionMap.put("EMAILMESSAGE", mailMessage.toString());
						
						break;
					}
					
					if(!"O".equals(String.valueOf(processingFlags[position])))
					{
						StringBuilder mailMessage = new StringBuilder();
						mailMessage.append("<pre>=======Verify that you have performed on a modelled unit=======</pre>");
						mailMessage.append("<pre>=======================================================</pre>");
						mailMessage.append("<pre>- EQP ID : ").append(machineName).append("</pre>");
						mailMessage.append("<pre>- UnitName : ").append(abnormalUnitInfo.getUnitName()).append("</pre>");
						mailMessage.append("<pre>- Position : ").append(position).append("</pre>");
						mailMessage.append("<pre>- ProductName : ").append(productElement.getChildText("PRODUCTNAME")).append("</pre>");
						mailMessage.append("<pre>- <PROCESSINGINFOFLAG> : ").append(processingFlag).append("</pre>");
						mailMessage.append("<pre>- Cause : No work carried out on the modelled unit.</pre>");
						mailMessage.append("<pre>=======================================================</pre>");
						
						actionMap.put("HOLDCAUSE", MessageFormat.format("No work carried out on the modelled unit. ProductName={0} MachineName={1} UnitName={2} Position={3} <PROCESSINGINFOFLAG>={4}", productElement.getChildText("PRODUCTNAME"), machineName, abnormalUnitInfo.getUnitName(), position, processingFlag));
						actionMap.put("EMAILMESSAGE", mailMessage.toString());
						
						break;
					}
				}
				
				if(actionMap != null && actionMap.size() > 0)
				{
					break;
				}
			}
			
			afterTrackOutLot = lotMultiHoldAboutLotProcessEnd(eventInfo, afterTrackOutLot, "HOLD", "AbnormalUnitInfo", actionMap.get("HOLDCAUSE"));
			
			CommonUtil.sendAlarmEmail("LotProcessEnd", "AbnormalUnitInfo", actionMap.get("EMAILMESSAGE"));
			
			return afterTrackOutLot;
		}
		catch (Exception ex)
		{
			return afterTrackOutLot;
		}
	}
	
	/**
	 * 
	 * AR-Photo-0028-01
	 * Lot hold before and after next operation work
	 * 
	 * @author aim_dhko
	 * @return 
	 */
	private Lot validateLotHoldAction(EventInfo eventInfo, Lot beforeTrackOutLot, Lot afterTrackOutLot)
	{
		try
		{
			// Check BeforeHold of LotHoldAction [afterTrackOutLot]
			
			// Get ProcessFlowType
			ProcessFlowKey afterFlowKey = new ProcessFlowKey();
			afterFlowKey.setFactoryName(afterTrackOutLot.getFactoryName());
			afterFlowKey.setProcessFlowName(afterTrackOutLot.getProcessFlowName());
			afterFlowKey.setProcessFlowVersion(afterTrackOutLot.getProcessFlowVersion());
			ProcessFlow afterFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(afterFlowKey);
			
			// Get DetailProcessOperationType
			ProcessOperationSpecKey afterOperKey = new ProcessOperationSpecKey();
			afterOperKey.setFactoryName(afterTrackOutLot.getFactoryName());
			afterOperKey.setProcessOperationName(afterTrackOutLot.getProcessOperationName());
			afterOperKey.setProcessOperationVersion(afterTrackOutLot.getProcessOperationVersion());
			ProcessOperationSpec afterOperData = ProcessOperationSpecServiceProxy.getProcessOperationSpecService().selectByKey(afterOperKey);
			
			LotHoldAction afterHoldAction = ExtendedObjectProxy.getLotHoldActionService().selectByKey(false, new Object[] { 
												afterTrackOutLot.getFactoryName(), afterFlowData.getProcessFlowType(), afterOperData.getDetailProcessOperationType() });
			
			if("Y".equals(afterHoldAction.getBeforeHold()))
			{
				String holdCause = "BeforeHold of LotHoldAction is 'Y'. "
								 + " LotName=" + afterTrackOutLot.getKey().getLotName()
								 + " ProcessFlowName=" + afterTrackOutLot.getProcessFlowName()
								 + " ProcessOperationName=" + afterTrackOutLot.getProcessOperationName()
								 + " FactoryName=" + afterTrackOutLot.getFactoryName()
								 + " ProcessFlowType=" + afterFlowData.getProcessFlowType()
								 + " DetailProcessOperationType=" + afterOperData.getDetailProcessOperationType();
				
				afterTrackOutLot = lotMultiHoldAboutLotProcessEnd(eventInfo, afterTrackOutLot, "HOLD", "BeforeLotHoldAction", holdCause);
			}
		}
		catch (Exception ex)
		{
			log.info("validateLotHoldAction : LotHoldAction data is not exist. [BeforeHold]");
		}
		
		try
		{
			// Check AfterHold of LotHoldAction [BeforeTrackOutLot]
			
			// Get ProcessFlowType
			ProcessFlowKey beforeFlowKey = new ProcessFlowKey();
			beforeFlowKey.setFactoryName(beforeTrackOutLot.getFactoryName());
			beforeFlowKey.setProcessFlowName(beforeTrackOutLot.getProcessFlowName());
			beforeFlowKey.setProcessFlowVersion(beforeTrackOutLot.getProcessFlowVersion());
			ProcessFlow beforeFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(beforeFlowKey);
			
			// Get DetailProcessOperationType
			ProcessOperationSpecKey beforeOperKey = new ProcessOperationSpecKey();
			beforeOperKey.setFactoryName(beforeTrackOutLot.getFactoryName());
			beforeOperKey.setProcessOperationName(beforeTrackOutLot.getProcessOperationName());
			beforeOperKey.setProcessOperationVersion(beforeTrackOutLot.getProcessOperationVersion());
			ProcessOperationSpec beforeOperData = ProcessOperationSpecServiceProxy.getProcessOperationSpecService().selectByKey(beforeOperKey);
			
			LotHoldAction beforeHoldAction = ExtendedObjectProxy.getLotHoldActionService().selectByKey(false, new Object[] { 
												beforeTrackOutLot.getFactoryName(), beforeFlowData.getProcessFlowType(), beforeOperData.getDetailProcessOperationType() });
			
			if("Y".equals(beforeHoldAction.getAfterHold()))
			{
				String holdCause = "AfterHold of LotHoldAction is 'Y'. "
								 + " LotName=" + beforeTrackOutLot.getKey().getLotName()
								 + " ProcessFlowName=" + beforeTrackOutLot.getProcessFlowName()
								 + " ProcessOperationName=" + beforeTrackOutLot.getProcessOperationName()
								 + " FactoryName=" + beforeTrackOutLot.getFactoryName()
								 + " ProcessFlowType=" + beforeFlowData.getProcessFlowType()
								 + " DetailProcessOperationType=" + beforeOperData.getDetailProcessOperationType();
				
				afterTrackOutLot = lotMultiHoldAboutLotProcessEnd(eventInfo, afterTrackOutLot, "HOLD", "AfterLotHoldAction", holdCause);
			}
		}
		catch (Exception ex)
		{
			log.info("validateLotHoldAction : LotHoldAction data is not exist. [AfterHold]");
		}
		
		return afterTrackOutLot;
	}
	
	private Lot lotMultiHoldAboutLotProcessEnd(EventInfo eventInfo, Lot lotData, String reasonCodeType, String reasonCode, String holdCause) throws CustomException
	{
		// Save original reason info, comment
		String oriReasonCodeType = eventInfo.getReasonCodeType();
		String oriReasonCode = eventInfo.getReasonCode();
		String oriEventComment = eventInfo.getEventComment();

		// Set reason, comment
		eventInfo.setReasonCodeType(reasonCodeType);
		eventInfo.setReasonCode(reasonCode);
		eventInfo.setEventComment(holdCause);

		// LotMultiHold
		MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, lotData, new HashMap<String, String>());
		lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getKey().getLotName());

		eventInfo.setReasonCodeType(oriReasonCodeType);
		eventInfo.setReasonCode(oriReasonCode);
		eventInfo.setEventComment(oriEventComment);

		return lotData;
	}
	
	// Insert PhotoMachineName, ExposureReipceName
	private void insertPhotoData(EventInfo eventInfo, String machineName, String recipeName,
			List<Element> productElementList) 
	{
		try 
		{
			String exposureRecipeName = StringUtils.substring(recipeName, 13, 17);
			
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfo.setEventName("InsertPhotoMachineInfo");

			for (Element productElement : productElementList) {
				String productName = productElement.getChildText("PRODUCTNAME");

				Product productData = ProductServiceProxy.getProductService().selectByKey(new ProductKey(productName));

				kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
				setEventInfo.getUdfs().put("LASTPHOTOMACHINENAME", machineName);
				setEventInfo.getUdfs().put("EXPOSURERECIPENAME", exposureRecipeName);

				MESProductServiceProxy.getProductServiceImpl().setEvent(productData, setEventInfo, eventInfo);
			}
		} catch (Exception ex) 
		{

		}
	}
	
	//AR-AMF-0029-01
	private String makeProcessingInfo(Document doc, Durable durableData) throws CustomException
	{
		StringBuffer processingInfoMapTemp = new StringBuffer();

		for (long i = 0; i < durableData.getCapacity(); i++)
			processingInfoMapTemp.append(GenericServiceProxy.getConstantMap().PRODUCT_NOT_IN_SLOT);

		List<Element> lstDownloadProduct = SMessageUtil.getSubSequenceItemList(SMessageUtil.getBodyElement(doc), "PRODUCTLIST", false);

		for (Element eleProduct : lstDownloadProduct)
		{
			String sPosition = SMessageUtil.getChildText(eleProduct, "POSITION", true);
			String sSlotPosition = SMessageUtil.getChildText(eleProduct, "SLOTPOSITION", false);
			String sProcessingInfo = SMessageUtil.getChildText(eleProduct, "PROCESSINGINFO", false);

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
					position = (Integer.parseInt(sPosition) * 2) - 1;
				else if (StringUtils.equals(sSlotPosition, "B"))
					position = (Integer.parseInt(sPosition) * 2);
			}
			processingInfoMapTemp.replace(position - 1, position, sProcessingInfo);
		}
		
		return processingInfoMapTemp.toString();
	}
	
	//CO_INT-0032-01 
	private Lot checkYield (EventInfo eventInfo, List<Element> productElementList, Lot lotData) throws CustomException
	{
		boolean holdFlag = false;
		
		long sumMeasurePanelQty = 0; 
		long sumNgPanelQty = 0; 
		long arrayTestYieldTarget = 0;
		
		for (Element productElement : productElementList) 
		{
			if(StringUtils.isNotEmpty(productElement.getChildText("MEASUREPANELQTY")) && StringUtils.isNotEmpty(productElement.getChildText("NGPANELQTY")))
			{
				long measurePanelQty = Long.parseLong(productElement.getChildText("MEASUREPANELQTY"));
				long ngPanelQty = Long.parseLong(productElement.getChildText("NGPANELQTY")); 
				
				sumMeasurePanelQty = sumMeasurePanelQty + measurePanelQty ;
				sumNgPanelQty = sumNgPanelQty + ngPanelQty;
			}
		}

		List<Map<String, Object>> arrayTestYieldTargetList = CommonUtil.getEnumDefValueByEnumName("ArrayTestYieldTarget");
		if (arrayTestYieldTargetList.size() > 0)
			for (Map<String, Object> arrayTestYieldTargetValue : arrayTestYieldTargetList)
				arrayTestYieldTarget = Long.parseLong(ConvertUtil.getMapValueByName(arrayTestYieldTargetValue, "ENUMVALUE"));

		if (sumNgPanelQty != 0 && sumMeasurePanelQty != 0)
		{
			long calculateValue = (sumNgPanelQty / sumMeasurePanelQty) * 100;

			if (arrayTestYieldTarget < calculateValue)
				holdFlag = true;

			if (holdFlag)
			{
				eventInfo.setEventName("Hold");
				eventInfo.setReasonCode("SYSTEM");
				eventInfo.setEventComment("CheckYield [ ArrayTestYieldTarget=" + String.valueOf(arrayTestYieldTarget) + ", sumNgPanelQty=" + String.valueOf(sumNgPanelQty) + ", sumMeasurePanelQty="
						+ String.valueOf(sumMeasurePanelQty) + "], " + eventInfo.getEventComment());

				Map<String, String> udfs = new HashMap<String, String>();

				if (StringUtil.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Released) && StringUtil.equals(lotData.getLotProcessState(), "WAIT"))
				{
					MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, lotData, udfs);
				}
				else
				{
					throw new CustomException("LOT-0113", lotData.getLotState(), lotData.getLotProcessState());
				}
			}
		}
		
		Lot refreshLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
		
		return refreshLotData;
	}
	
	private Lot checkLotProcessStateWait (EventInfo eventInfo, Lot lotData) throws CustomException
	{
		if ( StringUtils.equals(lotData.getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_Wait))
		{
			eventInfo.setEventName("Hold");
			eventInfo.setReasonCode("SYSTEM");
			eventInfo.setEventComment("LotProcessState is WAIT !" + eventInfo.getEventComment());

			Map<String, String> udfs = new HashMap<String, String>();

			if (StringUtil.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Released) && StringUtil.equals(lotData.getLotProcessState(), "WAIT"))
				lotData = MESLotServiceProxy.getLotServiceImpl().lotMultiHoldR(eventInfo, lotData, udfs);
			else
				throw new CustomException("LOT-0113", lotData.getLotState(), lotData.getLotProcessState());
		}
		
		return lotData;
	}
	
	private Lot OfflineRSSkip(Lot preLotData) throws CustomException
	{
		ReviewOperationInfo rsInfo = new ReviewOperationInfo();
		Lot postLotData = (Lot) ObjectUtil.copyTo(preLotData);
		
		try 
		{
			rsInfo = ExtendedObjectProxy.getReviewOperationInfoService().selectByKey(false, new Object[] {preLotData.getProcessOperationName() , preLotData.getProcessOperationVersion()});
		} 
		catch (Exception e) 
		{
			return preLotData;
		}
		
		if(!rsInfo.getOfflineFlag().equals("Y"))
		{
			return preLotData;
		}
		else
		{
			if (StringUtils.equals(preLotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_NotOnHold))
			{
				boolean mainFlow = false;
				while(!mainFlow)
				{
					EventInfo eventInfo = EventInfoUtil.makeEventInfo("RSOfflineSkip", this.getEventUser(), this.getEventComment(), "", "");
					Map<String, String> udfs = new HashMap<String, String>();
					ChangeSpecInfo skipInfo = MESLotServiceProxy.getLotInfoUtil().skipInfo(eventInfo, postLotData, udfs, new ArrayList<ProductU>());
					postLotData = MESLotServiceProxy.getLotServiceUtil().skip(eventInfo, postLotData, skipInfo, false);
					
					ProcessFlow flowSpec = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(new ProcessFlowKey(postLotData.getFactoryName(), postLotData.getProcessFlowName(), postLotData.getProcessFlowVersion()));
					
					if(flowSpec.getProcessFlowType().equals("Main"))
					{
						mainFlow = true;
					}
				}
			}
			else
			{
				return preLotData;
			}
		}
		
		return postLotData;
	}
	
	private Lot checkProductRecipe (EventInfo eventInfo, Lot lotData, List<String> productRecipeList, String machineRecipeName) throws CustomException
	{
		if((!StringUtil.equals(lotData.getProductionType(), "E"))&&(!StringUtil.equals(lotData.getProductionType(), "P"))&&(!StringUtil.equals(lotData.getProductionType(), "T")))
		{
			return lotData;
		}
		boolean holdFlag = false; 
		String eqpRecipeName = "";
		
		//if(lotData.getReasonCode().equals("SYSTEM"))
			//return lotData;
		
		for (String productRecipeName : productRecipeList)
		{
			eqpRecipeName = productRecipeName;
			if(!StringUtils.equals(machineRecipeName, productRecipeName))
			{
				holdFlag =  true;
				break;
			}
		}
		
		if(holdFlag)
		{
			eventInfo.setEventName("Hold");
			eventInfo.setReasonCode("HL-PPID");
			eventInfo.setEventComment("Different Recipe! MESRecipeName(" + machineRecipeName + "), EQPRecipeName(" + eqpRecipeName + ")" +  eventInfo.getEventComment());

			Map<String, String> udfs = new HashMap<String, String>();
			
			if (StringUtil.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Released) && StringUtil.equals(lotData.getLotProcessState(), "WAIT"))
				lotData = MESLotServiceProxy.getLotServiceImpl().lotMultiHoldR(eventInfo, lotData, udfs);
			else
				throw new CustomException("LOT-0113", lotData.getLotState(), lotData.getLotProcessState());
		}

		return lotData;
	}
	
	//Mantis 0000394
	//2021-05-20 ForceSampling RepairSkip 
	private Lot getNextOperationForRepairSkip(EventInfo eventInfo, Lot trackOutLot, String lotJudge, boolean firstCheck, boolean forceFlag, boolean isFirstGlass) throws CustomException
	{
		ProcessFlow currentFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(trackOutLot);

		if (CommonUtil.equalsIn(currentFlowData.getProcessFlowType(), "Inspection", "Sample"))
		{
			if (!StringUtils.equals(lotJudge, GenericServiceProxy.getConstantMap().LotGrade_P) || !firstCheck || isFirstGlass)
			{
				Map<String, String> trackOutLotUdfs = trackOutLot.getUdfs();

				Node nextNode = new Node();
				
				if(forceFlag)
				{
					String[] nodeStackArray = StringUtil.split(trackOutLot.getNodeStack(), ".");
					nextNode = ProcessFlowServiceProxy.getNodeService().getNode(nodeStackArray[nodeStackArray.length - 2]);
				}
				else
				{
					nextNode = PolicyUtil.getNextOperation(trackOutLot);
				}
				
				ProcessOperationSpec nextOperationData = CommonUtil.getProcessOperationSpec(nextNode.getFactoryName(), nextNode.getNodeAttribute1(), nextNode.getNodeAttribute2());

				if (StringUtils.equals(nextOperationData.getDetailProcessOperationType(), "RP"))
				{
					// Next Operation
					ProcessFlowKey processFlowKey1 = new ProcessFlowKey();
					processFlowKey1.setFactoryName(nextNode.getFactoryName());
					processFlowKey1.setProcessFlowName(nextNode.getProcessFlowName());
					processFlowKey1.setProcessFlowVersion(nextNode.getProcessFlowVersion());

					ProcessFlow nextNodeFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey1);

					if (StringUtils.equals(nextNodeFlowData.getProcessFlowType(), "Main") && nextNode.getProcessFlowName().equals(CommonUtil.getValue(trackOutLotUdfs, "RETURNFLOWNAME"))
							&& nextNode.getNodeAttribute1().equals(CommonUtil.getValue(trackOutLotUdfs, "RETURNOPERATIONNAME")))
					{
						trackOutLotUdfs.put("RETURNFLOWNAME", "");
						trackOutLotUdfs.put("RETURNOPERATIONNAME", "");
					}
					else
					{
						String nodeStack = "";
						String currentNode = trackOutLot.getNodeStack();
						boolean nextForce = false;

						if(forceFlag)
						{
							String currentFlowName = trackOutLot.getProcessFlowName();
							String currentFlowVersion = trackOutLot.getProcessFlowVersion();
							String currentOperationName = trackOutLot.getProcessOperationName();
							String currentOperationVersion = trackOutLot.getProcessOperationVersion();
							
							nodeStack = currentNode.substring(0, currentNode.lastIndexOf("."));
							
							trackOutLot.setNodeStack(nodeStack);
							trackOutLot.setProcessFlowName(nextNode.getProcessFlowName());
							trackOutLot.setProcessFlowVersion(nextNode.getProcessFlowVersion());
							trackOutLot.setProcessOperationName(nextNode.getNodeAttribute1());
							trackOutLot.setProcessOperationVersion(nextNode.getNodeAttribute2());
							
							eventLog.info("Next force check");
							List<SampleLot> sampleLotList = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListByToInfo(trackOutLot.getKey().getLotName(), trackOutLot.getFactoryName(),
									trackOutLot.getProductSpecName(), trackOutLot.getProductSpecVersion(), trackOutLot.getProcessFlowName(), trackOutLot.getProcessFlowVersion(),
									trackOutLot.getProcessOperationName(), trackOutLot.getProcessOperationVersion());
							
							if(sampleLotList != null)
							{
								if(StringUtils.isNotEmpty(sampleLotList.get(0).getForceSamplingFlag()))
								{
									nextForce = true;
								}
							}
							eventInfo.setEventName("SkipRepairOperation");
							MESLotServiceProxy.getLotServiceUtil().deleteSamplingDataReturn(eventInfo, trackOutLot, false);
							
							trackOutLot.setProcessFlowName(currentFlowName);
							trackOutLot.setProcessFlowVersion(currentFlowVersion);
							trackOutLot.setProcessOperationName(currentOperationName);
							trackOutLot.setProcessOperationVersion(currentOperationVersion);
							
							if(nextForce)
							{
								Map<String, String> setUdfs = trackOutLot.getUdfs();
								setUdfs.put("RETURNFLOWNAME", sampleLotList.get(0).getReturnProcessFlowName());
								setUdfs.put("RETURNOPERATIONNAME", sampleLotList.get(0).getReturnOperationName());
								setUdfs.put("RETURNOPERATIONVER", sampleLotList.get(0).getReturnOperationVersion());
							}

							LotServiceProxy.getLotService().update(trackOutLot);
							
							eventLog.info("Skip Repair Operation after ReviewStation because the LotJudge is " + lotJudge);

							trackOutLot = getNextOperationForRepairSkip(eventInfo, trackOutLot, lotJudge, false, nextForce, isFirstGlass);
						}
						else
						{
							if (StringUtils.contains(currentNode, "."))
							{
								String currentFlowName = trackOutLot.getProcessFlowName();
								String currentFlowVersion = trackOutLot.getProcessFlowVersion();
								String currentOperationName = trackOutLot.getProcessOperationName();
								String currentOperationVersion = trackOutLot.getProcessOperationVersion();

								String originalNode = currentNode.substring(0, currentNode.lastIndexOf("."));
								nodeStack = originalNode + "." + nextNode.getKey().getNodeId();

								trackOutLot.setNodeStack(nodeStack);
								trackOutLot.setProcessFlowName(nextNode.getProcessFlowName());
								trackOutLot.setProcessFlowVersion(nextNode.getProcessFlowVersion());
								trackOutLot.setProcessOperationName(nextNode.getNodeAttribute1());
								trackOutLot.setProcessOperationVersion(nextNode.getNodeAttribute2());

								MESLotServiceProxy.getLotServiceUtil().deleteSamplingDataReturn(eventInfo, trackOutLot, false);

								trackOutLot.setProcessFlowName(currentFlowName);
								trackOutLot.setProcessFlowVersion(currentFlowVersion);
								trackOutLot.setProcessOperationName(currentOperationName);
								trackOutLot.setProcessOperationVersion(currentOperationVersion);

								LotServiceProxy.getLotService().update(trackOutLot);
								eventLog.info("Skip Repair Operation after ReviewStation because the LotJudge is " + lotJudge);
								trackOutLot = getNextOperationForRepairSkip(eventInfo, trackOutLot, lotJudge, false, nextForce, isFirstGlass);
							}
						}
					}
				}
			}
		}

		return trackOutLot;
	}
	private List<Map<String, Object>> getIDLETimeByChamber(String machineName) throws CustomException
    {
		String sql = "SELECT * FROM ENUMDEFVALUE WHERE  ENUMNAME = 'IDLETimeByChamber' AND ENUMVALUE=:ENUMVALUE";

        Map<String, String> args = new HashMap<String, String>();
        args.put("ENUMVALUE", machineName);
        List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

        return result;
    }
	private List<Map<String, Object>> getIDLETimeByTPCVD(String machineName) throws CustomException
    {
		String sql = "SELECT * FROM ENUMDEFVALUE WHERE  ENUMNAME = 'IDLETimeByTPCVD' AND ENUMVALUE=:ENUMVALUE";

        Map<String, String> args = new HashMap<String, String>();
        args.put("ENUMVALUE", machineName);
        List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

        return result;
    }
	private static void idleTimeByCharmberEtch(String machineName, Lot beforeTrackOutLot,List<String> productRecipeList) throws CustomException
	{
		eventLog.info("Start UPDATE CT_MACHINEIDLEBYCHAMBER");
		ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(beforeTrackOutLot);
		
		if (StringUtils.contains(flowData.getProcessFlowType(), "MQC"))
		{
			if(productRecipeList != null && !productRecipeList.isEmpty())
			{
				String sql = " SELECT DISTINCT CHAMBERNAME,PROCESSOPERATIONNAME,RECIPENAME FROM CT_MACHINEIDLEBYCHAMBER CH ,MACHINE MA ,MACHINESPEC MS  "
						+ "WHERE  MA.MACHINENAME=MS.MACHINENAME  "
						+ "AND  MA.MACHINENAME=CH.CHAMBERNAME "
						+ "AND CH.MACHINENAME =:MACHINENAME "
						+ "AND CH.RECIPENAME IN (:RECIPENAME)  "
						+ "AND CH.CONTROLSWITCH='Y' AND CH.IDLEGROUPNAME = 'ChamberIdle'  " ;
				
				Map<String, Object> argsIdle = new HashMap<String, Object>();
				argsIdle.put("MACHINENAME", machineName);
				argsIdle.put("RECIPENAME", productRecipeList);
				
				List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, argsIdle);
				
				if(result!=null && !result.isEmpty())
				{
					List<Object[]> updateIdleBChamberList = new ArrayList<Object[]>();
					
					String queryStringUpadte = "UPDATE CT_MACHINEIDLEBYCHAMBER A  SET A.CONTROLSWITCH = 'N',A.LASTEVENTTIME =?,A.LASTEVENTTIMEKEY =?, "
							+ "A.LASTEVENTNAME = 'LotProcessEnd',A.LASTEVENTUSER = 'MES'  WHERE A.MACHINENAME=? AND  A.CHAMBERNAME=? AND  A.PROCESSOPERATIONNAME=? AND A.RECIPENAME=? ";
					
					for (Map<String, Object> row : result)
					{
					  List<Object> IdleBChamberList = new ArrayList<Object>();
					  String CurrentTime = TimeStampUtil.getCurrentTime(TimeStampUtil.FORMAT_DEFAULT);
					  String timeKey = TimeStampUtil.getCurrentEventTimeKey();
					  IdleBChamberList.add(CurrentTime);
					  IdleBChamberList.add(timeKey);
					  IdleBChamberList.add(machineName);
					  IdleBChamberList.add(ConvertUtil.getMapValueByName(row, "CHAMBERNAME"));
					  IdleBChamberList.add(ConvertUtil.getMapValueByName(row, "PROCESSOPERATIONNAME"));
					  IdleBChamberList.add(ConvertUtil.getMapValueByName(row, "RECIPENAME"));
					  updateIdleBChamberList.add(IdleBChamberList.toArray());
					}
								
					try
					{
						MESLotServiceProxy.getLotServiceUtil().updateBatch(queryStringUpadte, updateIdleBChamberList);
					}
					catch (Exception e)
					{
						 eventLog.info("UPDATE CT_MACHINEIDLEBYCHAMBER Fail ");
					}
				}
				else 
				{
					eventLog.info("End UPDATE CT_MACHINEIDLEBYCHAMBER By no correct ProductRecipe");
				}
			}
		}
	}
	private static void idleTimeByCharmberTPCVD(String machineName, Lot beforeTrackOutLot,List<String> productRecipeList) throws CustomException
	{
		

		   eventLog.info("Start UPDATE TPCVD CT_MACHINEIDLEBYCHAMBER");
		   ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(beforeTrackOutLot);
		
		if(StringUtils.contains(flowData.getProcessFlowType(), "MQC"))
		   {
			if(productRecipeList != null && !productRecipeList.isEmpty())
			{
				String sql = " SELECT DISTINCT CHAMBERNAME,PROCESSOPERATIONNAME FROM CT_MACHINEIDLEBYCHAMBER CH ,MACHINE MA ,MACHINESPEC MS  "
						+ "WHERE  MA.MACHINENAME=MS.MACHINENAME  "
						+ "AND  MA.MACHINENAME=CH.CHAMBERNAME "
						+ "AND CH.MACHINENAME =:MACHINENAME "
						+ "AND CH.CONTROLSWITCH='Y' AND CH.IDLEGROUPNAME = 'ChamberIdle'  " ;
				
				Map<String, Object> argsIdle = new HashMap<String, Object>();
				argsIdle.put("MACHINENAME", machineName);
				List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, argsIdle);
				
				if(result!=null && !result.isEmpty())
				{
					List<Object[]> updateIdleBChamberList = new ArrayList<Object[]>();
					
					String queryStringUpadte = "UPDATE CT_MACHINEIDLEBYCHAMBER A  SET A.CONTROLSWITCH = 'N',A.LASTEVENTTIME =?,A.LASTEVENTTIMEKEY =?, "
							+ "A.LASTEVENTNAME = 'LotProcessEnd',A.LASTEVENTUSER = 'MES'  WHERE A.MACHINENAME=? AND  A.CHAMBERNAME=? AND  A.PROCESSOPERATIONNAME=?";
					
					for (Map<String, Object> row : result)
					{
					  List<Object> IdleBChamberList = new ArrayList<Object>();
					  String CurrentTime = TimeStampUtil.getCurrentTime(TimeStampUtil.FORMAT_DEFAULT);
					  String timeKey = TimeStampUtil.getCurrentEventTimeKey();
					  IdleBChamberList.add(CurrentTime);
					  IdleBChamberList.add(timeKey);
					  IdleBChamberList.add(machineName);
					  IdleBChamberList.add(ConvertUtil.getMapValueByName(row, "CHAMBERNAME"));
					  IdleBChamberList.add(ConvertUtil.getMapValueByName(row, "PROCESSOPERATIONNAME"));
					  updateIdleBChamberList.add(IdleBChamberList.toArray());
					}
								
					try
					{
						MESLotServiceProxy.getLotServiceUtil().updateBatch(queryStringUpadte, updateIdleBChamberList);
					}
					catch (Exception e)
					{
						 eventLog.info("UPDATE CT_MACHINEIDLEBYCHAMBER Fail ");
					}
				}
				else 
				{
					eventLog.info("End UPDATE CT_MACHINEIDLEBYCHAMBER By no correct ProductRecipe");
				}
			}
		 }
	 }
	
	private void modifyQtimeEnterTime(EventInfo eventInfoForReworkQtime, int productionCount, List<ProductQueueTime> qTimeDataList, ProcessOperationSpec operationSpec) throws CustomException 
	{
		if (productionCount == 1)
		{
			for (ProductQueueTime productQueueTime : qTimeDataList) 
			{
				productQueueTime.setEnterTime(eventInfoForReworkQtime.getEventTime());
				productQueueTime.setWarningTime(null);
				productQueueTime.setQueueTimeState(GenericServiceProxy.getConstantMap().QTIME_STATE_IN);

				ExtendedObjectProxy.getProductQTimeService().modify(eventInfoForReworkQtime, productQueueTime);
			}
		}
		else if (productionCount == 2)
		{
			if (operationSpec.getDetailProcessOperationType().equals("PH") || operationSpec.getDetailProcessOperationType().equals("STRIP"))
			{
				for (ProductQueueTime productQueueTime : qTimeDataList) 
				{
					productQueueTime.setEnterTime(eventInfoForReworkQtime.getEventTime());
					productQueueTime.setWarningTime(null);
					productQueueTime.setQueueTimeState(GenericServiceProxy.getConstantMap().QTIME_STATE_IN);

					ExtendedObjectProxy.getProductQTimeService().modify(eventInfoForReworkQtime, productQueueTime);
				}
			}
			else
			{
				log.info("Production ReworkOperCount Is 2,But Opera Is Not PH Or STRIP,So Not ReCount Q-Time");
			}
		}
		else
		{
			log.info("Production ReworkOperCount Is Not 1 Or 2,So Not ReCount Q-Time");
		}
		
	}

	private List<Map<String, Object>> getBeforeMainOperationNode(Lot trackOutLot) 
	{
		List<Map<String, Object>> beforeMainOper = null;
				
		String currentNode  = trackOutLot.getNodeStack(); //Get current NodeID
		String originalNode = currentNode.substring(0, currentNode.indexOf(".")); //Get original NodeID
		Node aNode = ProcessFlowServiceProxy.getNodeService().getNode(originalNode);
		
		String sqlForNode = "SELECT C.* FROM  NODE C,(SELECT A.FROMNODEID FROM  ARC A WHERE "
				+ "A.PROCESSFLOWNAME = :PROCESSFLOWNAME AND A.PROCESSFLOWVERSION = :PROCESSFLOWVERSION "
				+ "AND A.TONODEID = :TONODEID) B WHERE C.NODEID = B.FROMNODEID";
		
		Map<String, Object> bindMapForNode = new HashMap<String, Object>();
		bindMapForNode.put("PROCESSFLOWNAME", aNode.getProcessFlowName());
		bindMapForNode.put("PROCESSFLOWVERSION", aNode.getProcessFlowVersion());	
		bindMapForNode.put("TONODEID", aNode.getKey().getNodeId());
		
		
		try 
		{
			beforeMainOper = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlForNode, bindMapForNode);
		} 
		catch (Exception e) 
		{
			log.info("Can't Find beforeMainOper,So Not ReCount Q-Time");
			return null;
		}
		
		return beforeMainOper;
	}
	
	private int getReworkProductionOperCount(Lot trackOutLot) 
	{
		int productionCount = 0;
		String sql = "SELECT B.* FROM  NODE A ,PROCESSOPERATIONSPEC B WHERE A.FACTORYNAME = :FACTORYNAME AND A.PROCESSFLOWNAME = :PROCESSFLOWNAME "
				+ "AND A.NODETYPE = 'ProcessOperation' AND A.NODEATTRIBUTE1 = B.PROCESSOPERATIONNAME AND B.PROCESSOPERATIONTYPE = 'Production'";
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", trackOutLot.getFactoryName());
		bindMap.put("PROCESSFLOWNAME", trackOutLot.getProcessFlowName());			
		try 
		{
			List<Map<String, Object>> reworkOper = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			productionCount = reworkOper.size();
		} 
		catch (Exception e) 
		{
			log.info("Can't Find reworkOper,So Not ReCount Q-Time");
			return 0;
		}
		return productionCount;
	}
	
	private List<String> getProcessedProductList(List<Element> productList) throws CustomException 
	{
		List<String> processedProductList=new ArrayList<String>();
		for(Element product:productList)
		{
			String flag = SMessageUtil.getChildText(product, "PROCESSINGINFO", false);
			String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", false);

			if (CommonUtil.equalsIn(flag, "N","F","L"))
			{
				processedProductList.add(productName);
			}
		}
		
		return processedProductList;
	}

	
}
