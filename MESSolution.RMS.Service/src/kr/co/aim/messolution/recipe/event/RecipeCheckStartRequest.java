package kr.co.aim.messolution.recipe.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.MaskMQCPlan;
import kr.co.aim.messolution.extended.object.management.data.MaskMQCPlanDetail;
import kr.co.aim.messolution.extended.object.management.data.MaskStick;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.extended.object.management.data.RecipeCheckResult;
import kr.co.aim.messolution.extended.object.management.data.RecipeParameter;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskRecipe;
import kr.co.aim.messolution.extended.object.management.data.TPOffsetAlignInfo;
import kr.co.aim.messolution.extended.object.management.data.WOPatternFilmInfo;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processoperationspec.ProcessOperationSpecServiceProxy;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpecKey;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class RecipeCheckStartRequest extends SyncHandler {

	@Override
	public Document doWorks(Document doc) throws CustomException {
		try 
		{
			this.prepareReply(doc);
			
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
			String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
			//String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", true);
			//String portAcessMode = SMessageUtil.getBodyItemValue(doc, "PORTACCESSMODE", true);
			String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
			String machineRecipeName = "";
			String RMSFlag = "";
			List<Recipe> recipeList = new ArrayList<Recipe>();
			
			ConstantMap constMap = GenericServiceProxy.getConstantMap();
			
			//Remove Last Data
			EventInfo eventInfoDel = EventInfoUtil.makeEventInfo("DeleteRecipeCheckResert", getEventUser(), getEventComment(), null, null);
			List<RecipeCheckResult> delInfoList = new ArrayList<RecipeCheckResult>();
			try
			{
				String condition = " WHERE MACHINENAME = ? AND PORTNAME = ? AND CARRIERNAME = ?";
				
				delInfoList = ExtendedObjectProxy.getRecipeCheckResultService().select(condition, new Object[]{machineName, portName, carrierName});
				
				for(int i = 0 ; i < delInfoList.size() ; i++)
				{
					ExtendedObjectProxy.getRecipeCheckResultService().remove(eventInfoDel, delInfoList.get(i), "MAIN");
				}
			}
			catch (Exception e)
			{
				eventLog.info("Delete Data is null");
			}
			
			// Search data
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName);
			
			//PostCell Sorter
			if(StringUtil.equals(machineData.getMachineGroupName(), constMap.MachineGroup_Sorter) && StringUtil.equals(machineData.getUdfs().get("CLEANMODEFLAG"), "Y"))
			{
				if (portData.getUdfs().get("PORTTYPE").equals("PL") && portData.getUdfs().get("FULLSTATE").equals(constMap.Port_FULL))
					throw new CustomException("SORTER-0002",portData.getUdfs().get("PORTTYPE"));
			}
			
			/*
			try
			{
				List<RecipeCheckedMachine> checkMachineList = ExtendedObjectProxy.getRecipeCheckedMachineService().select("mainMachineName = ? AND portName = ? ", new Object[] {machineName, portName});
				eventInfoDel.setEventName("DeleteCheckedMachineList");
				ExtendedObjectProxy.getRecipeCheckedMachineService().remove(eventInfoDel, checkMachineList);
			}
			catch (Exception e)
			{
				eventLog.info("Delete Data is null");
			}// Remove Last Data End
			*/

			//Get PPID
			Lot lotData = new Lot();
			List<MaskLot> maskLotList = new ArrayList<MaskLot>();
			
			boolean maskFlag = false;
			Durable durInfo = null;
			
			try 
			{
				durInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
			} 
			catch (Exception e) 
			{}
			
			//2020-12-08 For Stick Recipe Check
			if(StringUtil.equals(machineData.getMachineGroupName(), constMap.MachineGroup_MaskInitialInspection))
			{
				MaskStick maskStick = ExtendedObjectProxy.getMaskStickService().selectByKey(false, new Object[]{carrierName});
				
				List<Map<String, Object>> policyInfo = PolicyUtil.getTSMachineInfo(machineData.getFactoryName(), maskStick.getStickSpecName(), machineName);
				RMSFlag = policyInfo.get(0).get("RMSFLAG").toString();
				machineRecipeName = policyInfo.get(0).get("MACHINERECIPENAME").toString();
				
				if(StringUtil.isEmpty(machineRecipeName))
				{
					throw new CustomException("RMS-0005");
				}
			}
			else if(durInfo != null && durInfo.getDurableType().equals("CoverTray"))
			{
				List<Map<String, Object>> panelList =  MESLotServiceProxy.getLotServiceUtil().getLotListByTrayGroup(carrierName);
				if (panelList == null || panelList.size() == 0)
				{
					//TRAY-0029: Panel data by TrayGroup is not exist
					throw new CustomException("TRAY-0029");
				}
				
				String panelName = panelList.get(0).get("LOTNAME").toString();
				lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(panelName);
				//caixu 2021/1/4 Postcell RroductRequest Assign Recipe
				 if(lotData != null)
				{
					String productSpecName =lotData.getProductSpecName();
					String productSpecVersion =lotData.getProductSpecVersion();
					String processFlowName =lotData.getProcessFlowName();
					String processFlowVersion =lotData.getProcessFlowVersion() ;
					String processOperationName =lotData.getProcessOperationName(); 
					String processOperationVersion =lotData.getProcessOperationVersion();
					String productRequestName =lotData.getProductRequestName();
					WOPatternFilmInfo dataInfo = ExtendedObjectProxy.getWOPatternFilmInfoService().getWOPatternFilmInfoData(productSpecName, productSpecVersion,processFlowName,processFlowVersion,processOperationName,processOperationVersion,machineName, productRequestName);
					if(dataInfo!=null)
					{
						machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(),
								lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), 
								lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineName, true);
						machineRecipeName = "";
						machineRecipeName=dataInfo.getRecipeName();
						RMSFlag="N";
						
					}
					
					
				}//}
				
			}
			else if (durInfo != null && CommonUtil.equalsIn(durInfo.getDurableType(), "EVAMaskCST", "TFEMaskCST", "MaskCST") && (portData.getUdfs().get("PORTTYPE").endsWith("PB") || portData.getUdfs().get("PORTTYPE").endsWith("PL")))
			{
				String condition = " WHERE CARRIERNAME = ? ";
				Object[] bindSet = new Object[] { durInfo.getKey().getDurableName() };
				maskLotList = ExtendedObjectProxy.getMaskLotService().select(condition, bindSet);
				
				if(maskLotList == null || maskLotList.size() < 1)
				{
					throw new CustomException("DURABLE-9000", durInfo.getKey().getDurableName());
				}
				else
				{
					MaskLot maskLot = maskLotList.get(0);
					
					/*ReserveMaskRecipe reserveMaskRecipeInfo =  ExtendedObjectProxy.getReserveMaskRecipeService().selectByKey(false, new Object[]{maskLot.getMaskLotName(), maskLot.getMaskSpecName(), maskLot.getMaskProcessFlowName(), maskLot.getMaskProcessFlowVersion(), maskLot.getMaskProcessOperationName(), maskLot.getMaskProcessOperationVersion()});
					
					if(reserveMaskRecipeInfo == null)
					{
						throw new CustomException("DURABLE-0016", carrierName);
					}*/
					
					List<Map<String, Object>> maskRecipeInfo = ExtendedObjectProxy.getMaskLotService().getMaskRecipeV2(maskLot.getFactoryName(), maskLot.getMaskSpecName(), maskLot.getMaskProcessFlowName(), maskLot.getMaskProcessFlowVersion(), maskLot.getMaskProcessOperationName(), maskLot.getMaskProcessOperationVersion(), machineName);
					
					if(maskRecipeInfo.size() < 1)
					{
						throw new CustomException("MACHINE-0102", "");
					}
					
					/*if(!reserveMaskRecipeInfo.getRecipeName().equals(maskRecipeInfo.get(0).get("MACHINERECIPENAME").toString()))
					{
						throw new CustomException("RECIPE-0010", reserveMaskRecipeInfo.getRecipeName(), maskRecipeInfo.get(0).get("MACHINERECIPENAME").toString());
					}*/
					
					machineRecipeName = maskRecipeInfo.get(0).get("MACHINERECIPENAME").toString();
					
					if(maskRecipeInfo.get(0).get("RMSFLAG") != null)
					{
						RMSFlag = maskRecipeInfo.get(0).get("RMSFLAG").toString();
					}
					
					maskFlag = true;
				}
			}
			else if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Unpacker))
			{
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("RecipeCheckStartRequest", getEventUser(), getEventComment(), null, null);
				// Input Scenario Case# Get ReserveLot : order by position. 
				ReserveLot reserveLot = ExtendedObjectProxy.getReserveLotService().getFirstReserveLot(machineName);
				//reserveLot.setReserveState(GenericServiceProxy.getConstantMap().RESV_STATE_START);
				//ExtendedObjectProxy.getReserveLotService().modify(eventInfo, reserveLot);
				
				// Get LotData
				lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(reserveLot.getLotName());
				
				// Get MachineRecipeName (POSMachine)
				machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineName, false);
				
				RMSFlag = PolicyUtil.getPOSMachineRMSFlag(lotData.getFactoryName(),
						lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), 
						lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineName);
			}//Add FLCRecipe
			else if (StringUtils.equals(machineData.getMachineGroupName(),"FLC")&&(durInfo != null && durInfo.getDurableType().equals("FilmCST"))&&getFLCCheckRecipe(machineName,carrierName))
			{
					String lotName = this.getLotInfoBydurableNameForFisrtGlass(carrierName);
					if(StringUtil.isNotEmpty(lotName)){
						lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
						String productSpecName=lotData.getProductSpecName();
						String processFlowName=lotData.getProcessFlowName();
						String processOperationName=lotData.getProcessOperationName();
						String sql = "SELECT DISTINCT FLC.RECEIPENAME FROM CT_MATERIALPRODUCT MA ,PRODUCT P,CONSUMABLE CO,CT_FLCCONSUMABLERECIPE FLC "
								+ "WHERE P.LOTNAME=:LOTNAME AND P.PRODUCTNAME=MA.PRODUCTNAME AND MATERIALTYPE='TopLamination'  AND MA.MATERIALNAME=CO.CONSUMABLENAME "
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
								if(rmsFlag!=null){
									RMSFlag=rmsFlag;
								}
						    }else if(ecRecipeName!=null&&ecRecipeName.equals(consumableRecipeName)){
						    	machineRecipeName=ecRecipeName;
								if(ecRecipeFlag!=null){
									RMSFlag=ecRecipeFlag;
								}
						    }else{
						    	throw new CustomException("MACHINE-0043");

						    }
							
						 }else{
							
							throw new CustomException("RMS-0005");
						}
						
					}
			}
			else if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Photo) && machineData.getFactoryName().equals("TP")) // TP Offset
			{
				String lotName = this.getLotInfoBydurableNameForFisrtGlass(carrierName);

				if (StringUtil.isNotEmpty(lotName))
				{
					lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
				}
				else
				{
					lotData = CommonUtil.getLotInfoBydurableName(carrierName);
				}
				
				ProcessOperationSpec operationSpec = ProcessOperationSpecServiceProxy.getProcessOperationSpecService().selectByKey(new ProcessOperationSpecKey(lotData.getFactoryName(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion()));
			
				String productOffset = "";
				
				List<Product> productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotData.getKey().getLotName());
				
				Product productInfo = productList.get(0);
				
				if(productInfo.getUdfs().get("OFFSET") != null)
				{
					productOffset = productInfo.getUdfs().get("OFFSET").toString();
				}
				
				if((operationSpec.getUdfs().get("LAYERNAME").toString().equals("PEP1") || operationSpec.getUdfs().get("LAYERNAME").toString().equals("PEP0")))
				{
					machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(),
							lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), 
							lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineName, true);
					
					RMSFlag = PolicyUtil.getPOSMachineRMSFlag(lotData.getFactoryName(),
							lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), 
							lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineName);
					
					if(StringUtil.isEmpty(machineRecipeName))
					{
						throw new CustomException("RMS-0005");
					}
				}
				else if(((!operationSpec.getUdfs().get("LAYERNAME").toString().equals("PEP1")) && (!operationSpec.getUdfs().get("LAYERNAME").toString().equals("PEP0"))) && StringUtil.isEmpty(productOffset))
				{
					throw new CustomException("RMS-0005");
				}
				else if(((!operationSpec.getUdfs().get("LAYERNAME").toString().equals("PEP1")) && (!operationSpec.getUdfs().get("LAYERNAME").toString().equals("PEP0"))) && StringUtil.isNotEmpty(productOffset))
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
						TPOffsetAlignInfo offsetInfo = ExtendedObjectProxy.getTPOffsetAlignInfoService().selectByKey(false, new Object[]{productOffset, operationSpec.getKey().getProcessOperationName(), operationSpec.getKey().getProcessOperationVersion(), machineName});
					
						machineRecipeName = offsetInfo.getRecipeName();
						RMSFlag = offsetInfo.getRMSFlag();
					}
					else
					{
						throw new CustomException("RMS-0005");
					}
				}
			}
			else
			{
				MaskLot maskLotInfo = null;
				
				try 
				{
					maskLotInfo = ExtendedObjectProxy.getMaskLotService().getMaskLotData(carrierName);
				} 
				catch (Exception e) 
				{}
				
				if(maskLotInfo != null)
				{
					ProcessFlow processFlowData = ExtendedObjectProxy.getMaskLotService().getProcessFlowData(maskLotInfo);
					if(processFlowData.getProcessFlowType().equals("MQC")&&processFlowData.getUdfs().get("PROCESSFLOWUSEDTYPE").equals("Mask")
							&&(StringUtils.equals(machineData.getMachineGroupName(), constMap.MachineGroup_MaskOrgCleaner)
									||StringUtils.equals(machineData.getMachineGroupName(), constMap.MachineGroup_MaskMetalCleaner)))
					{
						MaskMQCPlanDetail maskMQCPlanDetailData = this.getMaskMQCRecipe(maskLotInfo, processFlowData);
						if(maskMQCPlanDetailData ==null)
						{
							throw new CustomException("MACHINE-0114", "");
						}
						machineRecipeName = maskMQCPlanDetailData.getRecipeName();
						RMSFlag = maskMQCPlanDetailData.getRMSFlag();
						
					}
					else if ((StringUtils.equals(machineData.getMachineGroupName(), constMap.MachineGroup_MaskOrgCleaner)))
					{
						List<Map<String, Object>> maskRecipeInfo = ExtendedObjectProxy.getMaskLotService().getMaskRecipeV2(maskLotInfo.getFactoryName(), maskLotInfo.getMaskSpecName(),
								maskLotInfo.getMaskProcessFlowName(), maskLotInfo.getMaskProcessFlowVersion(), maskLotInfo.getMaskProcessOperationName(), maskLotInfo.getMaskProcessOperationVersion(),
								machineName);

						if (maskRecipeInfo.size() < 1)
							throw new CustomException("MACHINE-0102", "");

						String recipeName = ConvertUtil.getMapValueByName(maskRecipeInfo.get(0), "MACHINERECIPENAME");
						String rmsFlag = ConvertUtil.getMapValueByName(maskRecipeInfo.get(0), "RMSFLAG");
						String ecRecipeFlag = ConvertUtil.getMapValueByName(maskRecipeInfo.get(0), "ECRECIPEFLAG");
						String ecRecipeName = ConvertUtil.getMapValueByName(maskRecipeInfo.get(0), "ECRECIPENAME");
						String maskCycleTarget = ConvertUtil.getMapValueByName(maskRecipeInfo.get(0), "MASKCYCLETARGET");

						if (rmsFlag != null)
							RMSFlag = rmsFlag;

						if (StringUtils.equals(rmsFlag, "Y") && StringUtils.equals(ecRecipeFlag, "Y"))
						{
							if (StringUtils.isEmpty(ecRecipeName))
								throw new CustomException("MACHINE-0043");

							if (StringUtils.isEmpty(maskCycleTarget))
								throw new CustomException("MACHINE-0044");

							int maskCycleCount = maskLotInfo.getMaskCycleCount().intValue();
							int iMaskCycleTarget = Integer.parseInt(maskCycleTarget);

							if (iMaskCycleTarget > 0 && maskCycleCount > 0 && maskCycleCount % iMaskCycleTarget == 0)
								machineRecipeName = ecRecipeName;
							else
								machineRecipeName = recipeName;
						}
						else
						{
							machineRecipeName = recipeName;
						}
					}
					else if (CommonUtil.equalsIn(machineData.getMachineGroupName(), constMap.MachineGroup_MaskAOI, constMap.MachineGroup_MaskMetalCleaner))
					{
						if (StringUtils.isEmpty(maskLotInfo.getMaskSpecName()))
							throw new CustomException("MASKLOT-0005");

						List<Map<String, Object>> maskRecipeInfo = ExtendedObjectProxy.getMaskLotService().getMaskSubSpecRecipe(maskLotInfo.getFactoryName(), maskLotInfo.getMaskSpecName(),
								maskLotInfo.getMaskSubSpecName(), maskLotInfo.getMaskProcessFlowName(), maskLotInfo.getMaskProcessFlowVersion(), maskLotInfo.getMaskProcessOperationName(),
								maskLotInfo.getMaskProcessOperationVersion(), machineName);

						if (maskRecipeInfo.size() < 1)
							throw new CustomException("MACHINE-0102", "");

						machineRecipeName = maskRecipeInfo.get(0).get("MACHINERECIPENAME").toString();

						if (maskRecipeInfo.get(0).get("RMSFLAG") != null)
							RMSFlag = maskRecipeInfo.get(0).get("RMSFLAG").toString();
					}
					else if (StringUtils.equals(machineData.getMachineGroupName(), constMap.MachineGroup_MaskPPA))
					{
						Object[] bindSet = new Object[] { maskLotInfo.getMaskLotName(), maskLotInfo.getMaskSpecName(), maskLotInfo.getMaskProcessFlowName(), maskLotInfo.getMaskProcessFlowVersion(),
								maskLotInfo.getMaskProcessOperationName(), maskLotInfo.getMaskProcessOperationVersion(),machineName };

						ReserveMaskRecipe reserveMaskRecipeInfo = new ReserveMaskRecipe();

						try
						{
							reserveMaskRecipeInfo = ExtendedObjectProxy.getReserveMaskRecipeService().selectByKey(false, bindSet);
						}
						catch (Exception e)
						{
							reserveMaskRecipeInfo = null;
						}

						if (reserveMaskRecipeInfo == null)
							throw new CustomException("DURABLE-0024", carrierName);

						machineRecipeName = reserveMaskRecipeInfo.getRecipeName();

						if (StringUtil.isNotEmpty(reserveMaskRecipeInfo.getRMSFlag()) && reserveMaskRecipeInfo.getRMSFlag().equals("N"))
							RMSFlag = "N";
						else
							RMSFlag = "Y";
					}
					else
					{
						List<Map<String, Object>> maskRecipeInfo = ExtendedObjectProxy.getMaskLotService().getMaskRecipeV2(maskLotInfo.getFactoryName(), maskLotInfo.getMaskSpecName(),
								maskLotInfo.getMaskProcessFlowName(), maskLotInfo.getMaskProcessFlowVersion(), maskLotInfo.getMaskProcessOperationName(), maskLotInfo.getMaskProcessOperationVersion(),
								machineName);

						if (maskRecipeInfo.size() < 1)
							throw new CustomException("MACHINE-0102", "");

						machineRecipeName = maskRecipeInfo.get(0).get("MACHINERECIPENAME").toString();

						if (maskRecipeInfo.get(0).get("RMSFLAG") != null)
						{
							RMSFlag = maskRecipeInfo.get(0).get("RMSFLAG").toString();
						}
					}

					maskFlag = true;
				}
				else
				{
					String lotName = this.getLotInfoBydurableNameForFisrtGlass(carrierName);

					if (StringUtil.isNotEmpty(lotName))
					{
						lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
					}
					else
					{
						lotData = CommonUtil.getLotInfoBydurableName(carrierName);
					}
				}
			}

			if(lotData == null && !maskFlag)
			{
				eventLog.info("Nothing in CST");
				throw new CustomException("CARRIER-9007", carrierName); 
			}
			
			//Check machine hold
			CommonValidation.checkMachineHold(machineData);
			
			//default behavior GetPOSPolicy Recipe
			if(!maskFlag && StringUtil.isEmpty(machineRecipeName))
			{
				machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(),
						lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), 
						lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineName, true);
				
				RMSFlag = PolicyUtil.getPOSMachineRMSFlag(lotData.getFactoryName(),
						lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), 
						lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineName);
			}

			// Cuz BC (OperationMode = SPLIT,MERGE -> RMS SKIP) 
			RMSFlag = checkMachineOperationMode(machineData, RMSFlag);
			
			if(RMSFlag.equals("Y"))
			{
				Recipe mainRecipeInfo;
				
				try //Get PPID
				{ 
					mainRecipeInfo = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] {machineName, machineRecipeName});
				}
				catch (Exception e)
				{
					doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReply(machineRecipeName, "NG", recipeList, doc, machineRecipeName + " : PPID null");
					return doc;
				}
				
				if(!mainRecipeInfo.getActiveState().equals(constMap.Spec_Active)) //Validate PPID ActiveState
				{
					doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReply(machineRecipeName, "NG", recipeList, doc, machineRecipeName + " : NotActive Recipe");
					return doc;
				}
				
				if(!(mainRecipeInfo.getINTFlag().equals("Y") && mainRecipeInfo.getMFGFlag().equals("Y") && mainRecipeInfo.getENGFlag().equals("Y"))) //Validate PPID Flag
				{
					doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReply(machineRecipeName, "NG", recipeList, doc, machineRecipeName + " : Check Flag");
					return doc;
				}
				
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeActiveState", getEventUser(), getEventComment(), null, null);
				
				String autoChangeFlag = mainRecipeInfo.getAutoChangeFlag();
				
				if(autoChangeFlag.equals("INTENG")||autoChangeFlag.equals("ENGINT"))
				{
					SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Date lastTrackOutTime = null;
					String currentTime = transFormat.format(new Date());
					Date currentDate = null;
					boolean firstTrackInFlag = false;
					
					try 
					{
						currentDate = transFormat.parse(currentTime);
						
						if(StringUtil.isNotEmpty(mainRecipeInfo.getLastTrackOutTimeKey()))
						{
							SimpleDateFormat beforeTransFormat = new SimpleDateFormat("yyyyMMddHHmmss");
							Date tempDate = beforeTransFormat.parse(mainRecipeInfo.getLastTrackOutTimeKey());
							//String trackOutTimeKey = mainRecipeInfo.getLastTrackOutTimeKey().substring(0, 14);
							String transDate = transFormat.format(tempDate);
							lastTrackOutTime = transFormat.parse(transDate);	
						}
					} 
					catch (ParseException e) 
					{}
					
					if(lastTrackOutTime != null)
					{
						double gap = (double)(currentDate.getTime() - lastTrackOutTime.getTime()) / (double)(60 * 60 * 1000);
						
						if (gap >= mainRecipeInfo.getDurationUsedLimit())
						{
							doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReply(machineRecipeName, "NG", recipeList, doc, machineRecipeName + " : DurationUsedLimit Over");
							mainRecipeInfo.setActiveState(constMap.Spec_NotActive);
							mainRecipeInfo.setRecipeState(constMap.RECIPESTATE_UNAPPROVED);
						    mainRecipeInfo.setINTFlag("Y");
							mainRecipeInfo.setENGFlag("N");
							mainRecipeInfo.setRMSFlag("N");					
							ExtendedObjectProxy.getRecipeService().modify(eventInfo, mainRecipeInfo);
							return doc;
						}
					}
					else
					{
						firstTrackInFlag = true;
					}
					
					if(firstTrackInFlag)
					{
						Date lastApproveTime = null;
						try 
						{
							lastApproveTime = transFormat.parse(mainRecipeInfo.getLastApporveTime().toString());
						} 
						catch (ParseException e) 
						{
							doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReply(machineRecipeName, "NG", recipeList, doc, "LastApproveTime null");	
							return doc;
						}
						
						double gap = (double)(currentDate.getTime() - lastApproveTime.getTime()) / (double)(60 * 60 * 1000);
						
						if (gap >= mainRecipeInfo.getDurationUsedLimit())
						{
							doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReply(machineRecipeName, "NG", recipeList, doc, machineRecipeName + " : DurationUsedLimit Over");
							mainRecipeInfo.setActiveState(constMap.Spec_NotActive);
							mainRecipeInfo.setRecipeState(constMap.RECIPESTATE_UNAPPROVED);
							mainRecipeInfo.setINTFlag("Y");
							mainRecipeInfo.setENGFlag("N");
							mainRecipeInfo.setRMSFlag("N");

							ExtendedObjectProxy.getRecipeService().modify(eventInfo, mainRecipeInfo);
							return doc;
						}
					}
					
					if(mainRecipeInfo.getTimeUsed() + 1 > mainRecipeInfo.getTimeUsedLimit())
					{
						doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReply(machineRecipeName, "NG", recipeList, doc, machineRecipeName + " : TimeUsedLimit Over");
						mainRecipeInfo.setActiveState(constMap.Spec_NotActive);
						mainRecipeInfo.setINTFlag("Y");
						mainRecipeInfo.setENGFlag("N");
						mainRecipeInfo.setRMSFlag("N");
						
						mainRecipeInfo.setRecipeState(constMap.RECIPESTATE_UNAPPROVED);
						ExtendedObjectProxy.getRecipeService().modify(eventInfo, mainRecipeInfo);
						return doc;
					}
					
					if (eventInfo.getEventTime().compareTo(mainRecipeInfo.getMaxDurationUsedLimit()) == 1)
					{
						doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReply(machineRecipeName, "NG", recipeList, doc, machineRecipeName + " : MaxDurationUsedLimit Over");
						mainRecipeInfo.setActiveState(constMap.Spec_NotActive);
						mainRecipeInfo.setRecipeState(constMap.RECIPESTATE_UNAPPROVED);
						mainRecipeInfo.setINTFlag("Y");
						mainRecipeInfo.setENGFlag("N");
						mainRecipeInfo.setRMSFlag("N");
						
						ExtendedObjectProxy.getRecipeService().modify(eventInfo, mainRecipeInfo);
						return doc;
					}
				}
				
				List<RecipeParameter> paramList;
		        
		        try //Get PPID Param
				{ 
					paramList = ExtendedObjectProxy.getRecipeParamService().select("machineName = ? AND recipeName = ?  ", new Object[] {machineName, machineRecipeName});
				}
				catch (Exception e)
				{
					doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReply(machineRecipeName, "NG", recipeList, doc, machineRecipeName + " : Parameter null");
					return doc;
				}
				
		        for(RecipeParameter paramInfo : paramList)
		        {
		        	if(!paramInfo.getCheckFlag().equals("N"))
		        	{
		        		Recipe unitRecipeInfo;
		        		
		        		try //Get PPID
		    			{ 
		        			unitRecipeInfo = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] {paramInfo.getRecipeParameterName(), paramInfo.getValue()});
		    			}
		        		catch (Exception e)
		    			{
		    				doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReply(machineRecipeName, "NG", recipeList, doc, paramInfo.getRecipeParameterName() + " : Recipe is Null");
		    				return doc;
		    			}
		        		
		        		recipeList.add(unitRecipeInfo);
		        	}
		        }
				
				long unitCount = recipeList.size();
				
				if(unitCount == 0)
				{
					doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReply(machineRecipeName, "SKIP", recipeList, doc, "All of PPID parameter CheckFlag N");
					return doc;
				}
				
				//MainRecipeCheck
				RecipeCheckResult recipeCheckResult = new RecipeCheckResult();
				
				recipeCheckResult.setMachineName(machineName);
				recipeCheckResult.setPortName(portName);
				recipeCheckResult.setCarrierName(carrierName);
				recipeCheckResult.setCheckLevel("");
				recipeCheckResult.setRecipeName(machineRecipeName);
				recipeCheckResult.setOriginalSubjectName("");
				recipeCheckResult.setUnitQty(unitCount);
				recipeCheckResult.setCheckUnitQty(0);
				recipeCheckResult.setSubUnitQty(0);
				recipeCheckResult.setCheckSubUnitQty(0);
				recipeCheckResult.setCreateTimeKey(ConvertUtil.getCurrTimeKey());
				
				EventInfo eventInfoCreate = EventInfoUtil.makeEventInfo("InsertRecipeCheckResult", getEventUser(), getEventComment(), null, null);
				eventInfoCreate.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				
				ExtendedObjectProxy.getRecipeCheckResultService().create(eventInfoCreate, recipeCheckResult, "MAIN");
				
				doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReply(machineRecipeName, "OK", recipeList, doc, "");
			}
			else
			{
				doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckStartReply(machineRecipeName, "SKIP", recipeList, doc, "RMSFlag = N");
			}
			
			return doc;
		} 
		catch (CustomException ce)
		{
			SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
			SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", ce.errorDef.getLoc_errorMessage());
			throw new CustomException("SYS-0010", ce.errorDef.getLoc_errorMessage());
		}
		catch (Exception e)
		{
			SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
			SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", e.toString());
			throw new CustomException(e);
		}
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

	private void prepareReply(Document doc) throws CustomException
	{
		String oldSourceSubjectName = SMessageUtil.getHeaderItemValue(doc, "SOURCESUBJECTNAME", false);
		String oldTargetSubjectName = SMessageUtil.getHeaderItemValue(doc, "TARGETSUBJECTNAME", false);

		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "RecipeCheckStartReply");
		SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", oldTargetSubjectName);
		SMessageUtil.setHeaderItemValue(doc, "SOURCESUBJECTNAME", oldTargetSubjectName);
		SMessageUtil.setHeaderItemValue(doc, "TARGETSUBJECTNAME", oldSourceSubjectName);
		
		try
		{
			Element bodyElement = SMessageUtil.getBodyElement(doc);
			{
				Element attResult = new Element("RESULT");
				attResult.setText("");
				bodyElement.addContent(attResult);
				
				Element attResultDesc = new Element("RESULTDESCRIPTION");
				attResultDesc.setText("");
				bodyElement.addContent(attResultDesc);
			}
		}
		catch (Exception ex)
		{
			//RMS-013:RMSError:Reply message  writing failed
			throw new CustomException("RMS-013");
		}
	}
	
	private String checkMachineOperationMode(Machine machineData, String RMSFlag) throws CustomException
	{
		if(StringUtils.isNotEmpty(machineData.getUdfs().get("OPERATIONMODE")))
		{
			if(StringUtils.equals(machineData.getUdfs().get("OPERATIONMODE"), "SPLIT") || StringUtils.equals(machineData.getUdfs().get("OPERATIONMODE"), "MERGE"))	
			{
				RMSFlag = "N";
			}
		}
		
		return RMSFlag;
	}
	private boolean getFLCCheckRecipe(String machineName,String carrierName) throws CustomException
	{
		String lotName = this.getLotInfoBydurableNameForFisrtGlass(carrierName);
		if(StringUtil.isNotEmpty(lotName)){
		Lot	lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
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
			
		}
		return false;
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
	
	public MaskMQCPlanDetail getMaskMQCRecipe(MaskLot maskLotData,ProcessFlow processFlowData)throws CustomException
	{
		MaskMQCPlanDetail maskMQCPlanDetailData = null;
		List<MaskMQCPlan> maskMQCPlanData = ExtendedObjectProxy.getMaskMQCPlanService().select(" MASKLOTNAME = ? AND MASKPROCESSFLOWNAME = ? "
				+ "AND MQCSTATE = ?", new String[]{maskLotData.getMaskLotName(),processFlowData.getKey().getProcessFlowName(),"Released"});
		if(maskMQCPlanData.size()>0)
		{
			String jobName = maskMQCPlanData.get(0).getJobName();
			Object[] keySet = new Object[] {jobName,processFlowData.getKey().getProcessFlowName(),
					processFlowData.getKey().getProcessFlowVersion(),maskLotData.getMaskProcessOperationName(),maskLotData.getMaskProcessOperationVersion()};
			
			maskMQCPlanDetailData = ExtendedObjectProxy.getMaskMQCPlanDetailService().selectByKey(false, keySet);
		}
		return maskMQCPlanDetailData;
	}
}
