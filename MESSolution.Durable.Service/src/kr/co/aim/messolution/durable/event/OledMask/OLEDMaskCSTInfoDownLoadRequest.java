package kr.co.aim.messolution.durable.event.OledMask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskGroup;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskRecipe;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskToEQP;
import kr.co.aim.messolution.extended.object.management.impl.ReserveMaskRecipeService;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;

public class OLEDMaskCSTInfoDownLoadRequest extends SyncHandler 
{
	private static Log log = LogFactory.getLog(OLEDMaskCSTInfoDownLoadRequest.class);
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		try
		{
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "OLEDMaskCSTInfoDownLoadSend");
			
			boolean isCSTCleaner = false;
			boolean checkMaskCleanState = false;

			// Set MessageName
			Element bodyElement = SMessageUtil.getBodyElement(doc);
			bodyElement.removeChild("SUBUNITNAME");
			
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
			String slotMap = SMessageUtil.getBodyItemValue(doc, "SLOTMAP", false);
			String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);

			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
			Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName);
			
			ConstantMap constMap = GenericServiceProxy.getConstantMap();

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("OLEDMaskCSTInfoDownLoadSend", getEventUser(), getEventComment(), null, null);

			if (CommonUtil.equalsIn(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_MaskCSTCleaner, GenericServiceProxy.getConstantMap().MachineGroup_CSTCleaner))
			{
				isCSTCleaner = true;
				log.info("CST Cleaner");
			}
			else if (CommonUtil.equalsIn(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_MaskOrgCleaner,
					GenericServiceProxy.getConstantMap().MachineGroup_MaskMetalCleaner,GenericServiceProxy.getConstantMap().MachineGroup_MaskTension))
			{
				checkMaskCleanState = true;
				log.info("Mask Cleaner");
			}

			//common check
			CommonValidation.checkMachineHold(machineData);
			CommonValidation.CheckDurableState(durableData);
			CommonValidation.CheckDurableHoldState(durableData);

			if (!isCSTCleaner && !checkMaskCleanState)
			{
				CommonValidation.CheckDurableCleanState(durableData);
			}
			
			if (isCSTCleaner && slotMap.contains("O"))
			{
				throw new CustomException("MASKCST-0002", slotMap);
			}

			String condition = " WHERE CARRIERNAME = ?  ORDER BY POSITION ";
			Object[] bindSet = new Object[] { carrierName };
			List<MaskLot> maskLotList = null;

			try
			{
				maskLotList = ExtendedObjectProxy.getMaskLotService().select(condition, bindSet);
			}
			catch (Exception e)
			{
				// By pass sequence
			}

			if (isCSTCleaner && maskLotList != null)
			{
				throw new CustomException("MASKCST-0001", carrierName);
			}
			
			//validation MaskLot Position 
			if(maskLotList != null)
			{
				CheckMaskPosition(maskLotList);
			}
			
			
			// slot map validation
			String logicalSlotMap = getSlotMapInfo(durableData, maskLotList);
			if (!slotMap.equals(logicalSlotMap))
			{
				throw new CustomException("PRODUCT-0020", slotMap, logicalSlotMap);
			}

			if (StringUtils.equals(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), "PU"))
			{
				eventLog.info("Unloader Port job download");

				// Mask CST Cleaner
				if (isCSTCleaner)
				{
					// Cannot Load CST on CST Cleaner Unloader Port
					throw new CustomException("CARRIER-9005");
				}

				// MaskLot is not empty
				if (maskLotList != null)
				{
					throw new CustomException("CARRIER-9003", carrierName);
				}
				else
				{
					this.generateBodyTemplate(bodyElement, durableData);

					return doc;
				}
			}

			// Set bodyElement
			this.generateBodyTemplate(bodyElement, maskLotList, durableData);

			if (!isCSTCleaner && maskLotList != null && maskLotList.size() > 0)
			{
				List<MaskLot> tempMaskLotList = new ArrayList<>();

				for (int i = 0; i < maskLotList.size(); i++)
				{
					String maskLotName = maskLotList.get(i).getMaskLotName();
					MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().getMaskLotData(maskLotName);
					tempMaskLotList.add(maskLotData);

					// Common validation
					CheckCommonTrackIn(maskLotData, checkMaskCleanState);

					// Recipe
					boolean checkOper = true;
					String machineRecipeName = null;
					ProcessFlow flowData = getProcessFlowData(maskLotData);

					if (!StringUtil.equalsIgnoreCase(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_EVA))
					{
						if (StringUtils.equals(flowData.getProcessFlowType(), "MQC")&&(StringUtils.equals(machineData.getMachineGroupName(), constMap.MachineGroup_MaskOrgCleaner)
								||StringUtils.equals(machineData.getMachineGroupName(), constMap.MachineGroup_MaskMetalCleaner)))
						{
							HashMap<String,String> MQCList = getMQCMachineRecipeName(maskLotName);
							if (MQCList.size()>0)
							{
								machineRecipeName = MQCList.get("RECIPENAME").toString();
								if(!MQCList.get("MACHINENAME").equals(machineName))
								{
									throw new CustomException("MACHINE-0115", machineName,MQCList.get("MACHINENAME").toString());
								}
							}
							else
							{
								throw new CustomException("MACHINE-0114", "");
							}
						}
						else
						{
							if (StringUtils.equals(machineData.getMachineGroupName(), constMap.MachineGroup_MaskOrgCleaner))
							{
								List<Map<String, Object>> maskRecipeInfo = ExtendedObjectProxy.getMaskLotService().getMaskRecipeV2(maskLotData.getFactoryName(), maskLotData.getMaskSpecName(),
										maskLotData.getMaskProcessFlowName(), maskLotData.getMaskProcessFlowVersion(), maskLotData.getMaskProcessOperationName(),
										maskLotData.getMaskProcessOperationVersion(), machineName);

								if (maskRecipeInfo.size() < 1)
								{
									throw new CustomException("RECIPE-0012", "[Policy = TRFOPOLICY] ", String.format("Factory =%s ,Spec = %s,Flow = %s Oper = %s,Machine = %s",
											maskLotData.getFactoryName(), maskLotData.getMaskSpecName(), maskLotData.getMaskProcessFlowName(), maskLotData.getMaskProcessOperationName(), machineName));
								}
								
								String recipeName = ConvertUtil.getMapValueByName(maskRecipeInfo.get(0), "MACHINERECIPENAME");
								String rmsFlag = ConvertUtil.getMapValueByName(maskRecipeInfo.get(0), "RMSFLAG");
								String ecRecipeFlag = ConvertUtil.getMapValueByName(maskRecipeInfo.get(0), "ECRECIPEFLAG");
								String ecRecipeName = ConvertUtil.getMapValueByName(maskRecipeInfo.get(0), "ECRECIPENAME");
								String maskCycleTarget = ConvertUtil.getMapValueByName(maskRecipeInfo.get(0), "MASKCYCLETARGET");
								
								if ("Y".equals(rmsFlag)&& ecRecipeFlag.equals("Y"))
								{
									if (StringUtils.isEmpty(ecRecipeName))
										throw new CustomException("MACHINE-0043");

									if (StringUtils.isEmpty(maskCycleTarget))
										throw new CustomException("MACHINE-0044");

									int maskCycleCount = maskLotData.getMaskCycleCount().intValue();
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
							else if (StringUtils.equals(machineData.getMachineGroupName(), constMap.MachineGroup_MaskMetalCleaner))
							{
								List<Map<String, Object>> maskRecipeInfo = ExtendedObjectProxy.getMaskLotService().getMaskSubSpecRecipe(maskLotData.getFactoryName(), maskLotData.getMaskSpecName(),
										maskLotData.getMaskSubSpecName(), maskLotData.getMaskProcessFlowName(), maskLotData.getMaskProcessFlowVersion(), maskLotData.getMaskProcessOperationName(),
										maskLotData.getMaskProcessOperationVersion(), machineName);

								if (maskRecipeInfo.size() < 1)
								{
									throw new CustomException("RECIPE-0012", "[Policy = TRSFOPOLICY] ", String.format("Factory =%s ,Spec = %s,Flow = %s Oper = %s,Machine = %s",
											maskLotData.getFactoryName(), maskLotData.getMaskSpecName(), maskLotData.getMaskProcessFlowName(), maskLotData.getMaskProcessOperationName(), machineName));
								}
								
								machineRecipeName = maskRecipeInfo.get(0).get("MACHINERECIPENAME").toString();
							}							
							else
							{
								machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipeForOLEDMask(maskLotData.getFactoryName(), maskLotData.getMaskSpecName(),
										maskLotData.getMaskProcessFlowName(), maskLotData.getMaskProcessFlowVersion(), maskLotData.getMaskProcessOperationName(),
										maskLotData.getMaskProcessOperationVersion(), machineName);
							}
							
							checkOper = false;
						}
						//Mask Tension Recipe Download BY TRFO Policy
//						if (StringUtils.equals(machineData.getMachineGroupName(), constMap.MachineGroup_MaskTension))
//						{
//							machineRecipeName = "";
//						}
					}

					if (checkOper)
					{
						// Operation Validation
						List<Map<String, Object>> trfoPolicyAndMachine = ExtendedObjectProxy.getMaskLotService().getTrfoPolicyAndMachine(maskLotData.getFactoryName(), maskLotData.getMaskSpecName(),
								maskLotData.getMaskProcessFlowName(), maskLotData.getMaskProcessFlowVersion(), maskLotData.getMaskProcessOperationName(), maskLotData.getMaskProcessOperationVersion(),
								machineName);

						if (trfoPolicyAndMachine.size() == 0)
						{
							throw new CustomException("MASK-0061", maskLotName);
						}
					}
					this.createMaskElement(doc, maskLotData, machineData, durableData, machineRecipeName);

					MaskLot maskLotInfo = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });

					if (maskLotInfo != null)
					{
						eventInfo.setEventName("OLEDMaskCSTInfoDownLoadSend");
						maskLotInfo.setJobDownFlag("Y");

						ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotInfo);
					}
				}

				if (this.checkFlowOper(tempMaskLotList) == false)
				{
					log.info("ProcessFlow or ProcessOperation is not matched in Carrier");
					throw new CustomException("CARRIER-9006", carrierName);
				}
			}
			else if (isCSTCleaner)
			{
				CommonValidation.checkEmptyCst(carrierName);
				checkAssignedMaskLotByCst(carrierName);
			}
			else
			{
				throw new CustomException("CARRIER-9002", carrierName);
			}
            
			// Set slotMap
			this.setMaskMap(doc, maskLotList, durableData, slotMap);

			// Update CANCELINFOFLAG for DSP
			if (durableData.getUdfs().get("CANCELINFOFLAG").equals("Y"))
			{
				kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfoDur = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
				setEventInfoDur.getUdfs().put("CANCELINFOFLAG", "");

				DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfoDur);
			}

			return doc;
		}
		catch (CustomException ce)
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "OLEDMaskCSTCancelCommandSend");

			this.generateNGBodyTemplate(doc, SMessageUtil.getBodyElement(doc));

			throw new CustomException("SYS-0010", ce.errorDef.getLoc_errorMessage());
		}
		catch (Exception e)
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "OLEDMaskCSTCancelCommandSend");

			this.generateNGBodyTemplate(doc, SMessageUtil.getBodyElement(doc));

			throw new CustomException(e);
		}
	}

	private void checkAssignedMaskLotByCst(String carrierName) throws CustomException
	{
		String sql = "SELECT MASKLOTNAME FROM CT_MASKLOT WHERE CARRIERNAME = :CARRIERNAME ";
		Map<String, String> args = new HashMap<String, String>();
		args.put("CARRIERNAME", carrierName);

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

		if (result.size() > 0)
		{
			String lotName = ConvertUtil.getMapValueByName(result.get(0), "MASKLOTNAME");

			// Lot[{0}] is assigned to CST[{1}]
			throw new CustomException("CST-0002", lotName, carrierName);
		}
	}

	private Element generateBodyTemplate(Element bodyElement, List<MaskLot> maskLotList, Durable durableData) throws CustomException
	{
		if (maskLotList != null)
		{
			XmlUtil.addElement(bodyElement, "CARRIERSTATE", durableData.getDurableState());
			XmlUtil.addElement(bodyElement, "MASKQUANTITY", Integer.toString(maskLotList.size()));
			XmlUtil.addElement(bodyElement, "INPUTMASKMAP", StringUtil.EMPTY);
			XmlUtil.addElement(bodyElement, "MASKLIST", StringUtil.EMPTY);
		}
		else
		{
			XmlUtil.addElement(bodyElement, "CARRIERSTATE", durableData.getDurableState());
			XmlUtil.addElement(bodyElement, "MASKQUANTITY", StringUtil.EMPTY);
			XmlUtil.addElement(bodyElement, "INPUTMASKMAP", StringUtil.EMPTY);
			XmlUtil.addElement(bodyElement, "MASKLIST", StringUtil.EMPTY);
		}
		return bodyElement;
	}

	private Element generateBodyTemplate(Element bodyElement, Durable durableData) throws CustomException
	{
		String slotMap = StringUtils.repeat(GenericServiceProxy.getConstantMap().PRODUCT_NOT_IN_SLOT, (int) durableData.getCapacity());

		XmlUtil.addElement(bodyElement, "CARRIERSTATE", durableData.getDurableState());
		XmlUtil.addElement(bodyElement, "MASKQUANTITY", String.valueOf(0));
		XmlUtil.addElement(bodyElement, "INPUTMASKMAP", slotMap);
		XmlUtil.addElement(bodyElement, "MASKLIST", StringUtil.EMPTY);

		return bodyElement;
	}

	private void generateNGBodyTemplate(Document doc, Element bodyElementOri) throws CustomException
	{
		Element bodyElement = null;
		bodyElement = new Element("Body");

		Element machineNameElement = new Element("MACHINENAME");
		machineNameElement.setText(bodyElementOri.getChildText("MACHINENAME"));
		bodyElement.addContent(machineNameElement);

		Element unitNameElement = new Element("UNITNAME");
		unitNameElement.setText(bodyElementOri.getChildText("UNITNAME"));
		bodyElement.addContent(unitNameElement);

		Element portNameElement = new Element("PORTNAME");
		portNameElement.setText(bodyElementOri.getChildText("PORTNAME"));
		bodyElement.addContent(portNameElement);

		Element portTypeElement = new Element("PORTTYPE");
		portTypeElement.setText(bodyElementOri.getChildText("PORTTYPE"));
		bodyElement.addContent(portTypeElement);

		Element portUseTypeElement = new Element("PORTUSETYPE");
		portUseTypeElement.setText(bodyElementOri.getChildText("PORTUSETYPE"));
		bodyElement.addContent(portUseTypeElement);

		Element carrierNameElement = new Element("CARRIERNAME");
		carrierNameElement.setText(bodyElementOri.getChildText("CARRIERNAME"));
		bodyElement.addContent(carrierNameElement);

		// first removal of existing node would be duplicated
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		// index of Body node is static
		doc.getRootElement().addContent(2, bodyElement);
	}

	private void createMaskElement(Document doc, MaskLot maskData, Machine machineData, Durable durableData, String machineRecipeName) throws CustomException
	{
	    doc.getRootElement().getChild(SMessageUtil.Body_Tag).removeChild("PRODUCTIONTYPE");
	   
		Element maskListElement = XmlUtil.getChild(SMessageUtil.getBodyElement(doc), "MASKLIST", true);

		Element maskElement = new Element("MASK");

		Element eleMaskName = new Element("MASKNAME");
		eleMaskName.setText(maskData.getMaskLotName());
		maskElement.addContent(eleMaskName);

		Element eleMaskSpec = new Element("MASKSPECNAME");
		eleMaskSpec.setText(maskData.getMaskSpecName());
		maskElement.addContent(eleMaskSpec);

		Element eleProcessFlow = new Element("PROCESSFLOWNAME");
		eleProcessFlow.setText(maskData.getMaskProcessFlowName());
		maskElement.addContent(eleProcessFlow);

		Element eleProcessFlowVer = new Element("PROCESSFLOWVERSION");
		eleProcessFlowVer.setText(maskData.getMaskProcessFlowVersion());
		maskElement.addContent(eleProcessFlowVer);

		Element eleProcessOper = new Element("PROCESSOPERATIONNAME");
		eleProcessOper.setText(maskData.getMaskProcessOperationName());
		maskElement.addContent(eleProcessOper);

		Element eleProcessOperVer = new Element("PROCESSOPERATIONVERSION");
		eleProcessOperVer.setText(maskData.getMaskProcessOperationVersion());
		maskElement.addContent(eleProcessOperVer);

		Element elePosition = new Element("POSITION");
		elePosition.setText(maskData.getPosition());
		maskElement.addContent(elePosition);

		Element eleMaskMachineRecipe = new Element("MASKRECIPENAME");
		eleMaskMachineRecipe.setText(machineRecipeName);
		maskElement.addContent(eleMaskMachineRecipe);

		Element eleMaskType = new Element("MASKTYPE");
		eleMaskType.setText(maskData.getMaskType());
		maskElement.addContent(eleMaskType);

		Element eleMaskModelNo = new Element("MASKMODELNO");
		eleMaskModelNo.setText(maskData.getMaskModelName());
		maskElement.addContent(eleMaskModelNo);

		Element eleMaskMagnet = new Element("MAGNET");
		eleMaskMagnet.setText(String.valueOf(maskData.getMagnet()));
		maskElement.addContent(eleMaskMagnet);

		Element eleMaskUsedLimit = new Element("MASKUSEDLIMIT");
		eleMaskUsedLimit.setText(String.valueOf(maskData.getTimeUsedLimit().intValue()));
		maskElement.addContent(eleMaskUsedLimit);

		Element eleMaskUsedCount = new Element("MASKUSEDCOUNT");
		eleMaskUsedCount.setText(String.valueOf(maskData.getTimeUsed().intValue()));
		maskElement.addContent(eleMaskUsedCount);

		Element eleMaskJudge = new Element("MASKJUDGE");
		eleMaskJudge.setText(maskData.getMaskLotJudge());
		maskElement.addContent(eleMaskJudge);

		if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_EVA) && StringUtils.equals(maskData.getMaskKind(), "EVA"))
		{
			Element eleMask_OffSet_X = new Element("INSPECTION_OFFSET_X");
			//Double dInitialOffSetX = StringUtil.isNotEmpty(maskData.getInitialOffSetX()) ? Double.valueOf(maskData.getInitialOffSetX()) * 10 : 0;
			//eleMask_OffSet_X.setText(String.valueOf(dInitialOffSetX.intValue()));
			eleMask_OffSet_X.setText(maskData.getInitialOffSetX());
			maskElement.addContent(eleMask_OffSet_X);

			Element eleMask_OffSet_Y = new Element("INSPECTION_OFFSET_Y");
			//Double dInitialOffSetY = StringUtil.isNotEmpty(maskData.getInitialOffSetY()) ? Double.valueOf(maskData.getInitialOffSetY()) * 10 : 0;
			//eleMask_OffSet_Y.setText(String.valueOf(dInitialOffSetY.intValue()));
			eleMask_OffSet_Y.setText(maskData.getInitialOffSetY());
			maskElement.addContent(eleMask_OffSet_Y);

			Element eleMask_OffSet_T = new Element("INSPECTION_OFFSET_THETA");
			eleMask_OffSet_T.setText(maskData.getInitialOffSetTheta());
			maskElement.addContent(eleMask_OffSet_T);

		}
		else
		{
			Element eleMask_OffSet_X = new Element("INSPECTION_OFFSET_X");
			eleMask_OffSet_X.setText(null);
			maskElement.addContent(eleMask_OffSet_X);

			Element eleMask_OffSet_Y = new Element("INSPECTION_OFFSET_Y");
			eleMask_OffSet_Y.setText(null);
			maskElement.addContent(eleMask_OffSet_Y);

			Element eleMask_OffSet_T = new Element("INSPECTION_OFFSET_THETA");
			eleMask_OffSet_T.setText(null);
			maskElement.addContent(eleMask_OffSet_T);

		}		
		if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_EVA) && StringUtils.equals(maskData.getMaskKind(), "EVA"))
		{
			Element eleMaskThk = new Element("MASKTHICKNESS");
			Double deleMaskThk = Double.valueOf(maskData.getMaskThickness()) * 10000;
			eleMaskThk.setText(String.valueOf(deleMaskThk.intValue()));
			maskElement.addContent(eleMaskThk);
		}
		else
		{
			Element eleMaskThk = new Element("MASKTHICKNESS");
			Double deleMaskThk = Double.valueOf(maskData.getMaskThickness()) * 10000;
			eleMaskThk.setText(String.valueOf(deleMaskThk.intValue()));
			maskElement.addContent(eleMaskThk);
		}

		MaskGroup maskGroupData = null;
		try
		{
			maskGroupData = ExtendedObjectProxy.getMaskGroupService().selectByKey(false,new Object[]{maskData.getMaskLotName()});
		}
		catch (greenFrameDBErrorSignal nfdes)
		{
			log.info("The MaskLot: "+maskData.getMaskLotName()+" has no data in CT_MaskGroup!");
		}
		
		if(maskGroupData!=null&&maskGroupData.getMachineName().equals(machineData.getKey().getMachineName()))
		{
			Element eleChamberName = new Element("CHAMBERNAME");
			eleChamberName.setText(maskGroupData.getSubUnitName());
			maskElement.addContent(eleChamberName);
		}
		else
		{
			Element eleChamberName = new Element("CHAMBERNAME");
			eleChamberName.setText("");
			maskElement.addContent(eleChamberName);
		}
		

		Element elelFlowState = new Element("MASKFLOWSTATE");
		elelFlowState.setText(maskData.getMaskFlowState());
		maskElement.addContent(elelFlowState);
		
		Element eleCollPosi = new Element("COOLZPOSITION");
		eleCollPosi.setText(maskData.getCool_Z_Position());
		maskElement.addContent(eleCollPosi);
		
		Element eleAlignRecipe = new Element("ALIGNRECIPE");
		eleAlignRecipe.setText(maskData.getAlignRecipe());
		maskElement.addContent(eleAlignRecipe);

		Element eleRepairCount = new Element("REPAIRCOUNT");
		eleRepairCount.setText(maskData.getMaskRepairCount() == null ? "0":maskData.getMaskRepairCount().toString());
		maskElement.addContent(eleRepairCount);
		
		Element eleReworkFlag = new Element("REWORKFLAG");
		eleReworkFlag.setText("InRework".equals(maskData.getReworkState())?"Y":"N");
		maskElement.addContent(eleReworkFlag);
		
		Element eleMaskCycleCount = new Element("MASKCYCLECOUNT");
		eleMaskCycleCount.setText(maskData.getMaskCycleCount() == null ? "0":maskData.getMaskCycleCount().toString());
		maskElement.addContent(eleMaskCycleCount);

		Element eleFrameName = new Element("FRAMENAME");
		eleFrameName.setText(maskData.getFrameName());
		maskElement.addContent(eleFrameName);
		
		if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_EVA) && StringUtils.equals(maskData.getMaskKind(), "TFE"))
		{
			Element eleOffsetX1 = new Element("TFEOFFSETX_1");
			eleOffsetX1.setText(maskData.getTFEOFFSETX1());
			maskElement.addContent(eleOffsetX1);
			
			Element eleOffsetY1 = new Element("TFEOFFSETY_1");
			eleOffsetY1.setText(maskData.getTFEOFFSETY1());
			maskElement.addContent(eleOffsetY1);
			
			Element eleOffsetX2 = new Element("TFEOFFSETX_2");
			eleOffsetX2.setText(maskData.getTFEOFFSETX2());
			maskElement.addContent(eleOffsetX2);
			
			Element eleOffsetY2 = new Element("TFEOFFSETY_2");
			eleOffsetY2.setText(maskData.getTFEOFFSETY2());
			maskElement.addContent(eleOffsetY2);
		}
		else
		{
			Element eleOffsetX1 = new Element("TFEOFFSETX_1");
			eleOffsetX1.setText(null);
			maskElement.addContent(eleOffsetX1);
			
			Element eleOffsetY1 = new Element("TFEOFFSETY_1");
			eleOffsetY1.setText(null);
			maskElement.addContent(eleOffsetY1);
			
			Element eleOffsetX2 = new Element("TFEOFFSETX_2");
			eleOffsetX2.setText(null);
			maskElement.addContent(eleOffsetX2);
			
			Element eleOffsetY2 = new Element("TFEOFFSETY_2");
			eleOffsetY2.setText(null);
			maskElement.addContent(eleOffsetY2);
		}
		
		maskListElement.addContent(maskElement);
	}

	private void setMaskMap(Document doc, List<MaskLot> maskLotList, Durable durableData, String eqSlotMap) throws CustomException
	{
		StringBuffer slotMapTemp = new StringBuffer();

		for (long i = 0; i < durableData.getCapacity(); i++)
		{
			slotMapTemp.append(GenericServiceProxy.getConstantMap().PRODUCT_NOT_IN_SLOT);
		}

		if (maskLotList != null)
		{
			for (MaskLot maskLot : maskLotList)
			{
				// Validataion Position null check
				if (StringUtils.isEmpty(maskLot.getPosition()))
				{
					// Mask[{0}] Position Is Null
					throw new CustomException("MASK-0210", maskLot.getMaskLotName());
				}
				
				int position = Integer.parseInt(maskLot.getPosition());
				slotMapTemp.replace(position - 1, position, GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT);
			}
		}

		eventLog.debug("Completed Mask Selection : " + slotMapTemp.toString());

		if (!eqSlotMap.equals(slotMapTemp.toString()))
		{
			throw new CustomException("PRODUCT-0020", eqSlotMap, slotMapTemp);
		}

		SMessageUtil.setBodyItemValue(doc, "SLOTMAP", slotMapTemp.toString());
		SMessageUtil.setBodyItemValue(doc, "INPUTMASKMAP", slotMapTemp.toString());

	}

	private boolean checkFlowOper(List<MaskLot> dataInfoList) throws CustomException
	{
		if (dataInfoList != null && dataInfoList.size() > 0)
		{
			String processFlow = StringUtil.EMPTY;
			String processFlowVer = StringUtil.EMPTY;
			String processOper = StringUtil.EMPTY;
			String processOperVer = StringUtil.EMPTY;

			for (MaskLot maskData : dataInfoList)
			{
				if (StringUtil.isEmpty(processFlow))
				{
					processFlow = maskData.getMaskProcessFlowName();
					processFlowVer = maskData.getMaskProcessFlowVersion();
					processOper = maskData.getMaskProcessOperationName();
					processOperVer = maskData.getMaskProcessOperationVersion();
				}
				else
				{
					if (!processFlow.equals(maskData.getMaskProcessFlowName()) || !processFlowVer.equals(maskData.getMaskProcessFlowVersion())
							|| !processOper.equals(maskData.getMaskProcessOperationName()) || !processOperVer.equals(maskData.getMaskProcessOperationVersion()))
					{
						return false;
					}
				}
			}
		}

		return true;
	}

	private void CheckCommonTrackIn(MaskLot maskLotData, boolean checkMaskCleanState) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		if (!maskLotData.getMaskLotState().equals(constantMap.MaskLotState_Released))
		{
			throw new CustomException("MASK-0026", maskLotData.getMaskLotName(), maskLotData.getMaskLotState());
		}

		if (!maskLotData.getMaskLotProcessState().equals(constantMap.MaskLotProcessState_Wait))
		{
			throw new CustomException("MASK-0026", maskLotData.getMaskLotName(), maskLotData.getMaskLotProcessState());
		}

		if (!checkMaskCleanState && StringUtils.equals(maskLotData.getCleanState(), constantMap.Dur_Dirty))
		{
			throw new CustomException("MASK-0025", maskLotData.getMaskLotName());
		}

		if (maskLotData.getMaskLotHoldState().equals(constantMap.MaskLotHoldState_OnHold))
		{
			throw new CustomException("MASK-0013", maskLotData.getMaskLotName());
		}

		if (maskLotData.getTimeUsedLimit() != null && maskLotData.getTimeUsed() != null)
		{
			if (maskLotData.getTimeUsedLimit() <= maskLotData.getTimeUsed())
			{
				throw new CustomException("MASK-0030", maskLotData.getMaskLotName());
			}
		}
	}

	private ProcessFlow getProcessFlowData(MaskLot maskLotData) throws CustomException
	{
		ProcessFlowKey processFlowKey = new ProcessFlowKey();
		processFlowKey.setFactoryName(maskLotData.getFactoryName());
		processFlowKey.setProcessFlowName(maskLotData.getMaskProcessFlowName());
		processFlowKey.setProcessFlowVersion(maskLotData.getMaskProcessFlowVersion());
		ProcessFlow processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);

		return processFlowData;
	}

	private HashMap<String,String> getMQCMachineRecipeName(String maskLotName) throws CustomException
	{
		String machineRecipeName = "";
		String machineName = "";
		HashMap<String,String> MQCList = new HashMap<String,String>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT L.MASKLOTNAME, L.POSITION, D.RECIPENAME, D.MACHINENAME  ");
		sql.append("  FROM CT_MASKMQCPLAN M,  ");
		sql.append("       CT_MASKMQCPLANDETAIL D,  ");
		sql.append("       CT_MASKLOT L ");
		sql.append(" WHERE M.JOBNAME = D.JOBNAME  ");
		sql.append("   AND M.MQCSTATE = 'Released'  ");
		sql.append("   AND M.MASKLOTNAME = :MASKLOTNAME  ");
		sql.append("   AND M.MASKLOTNAME = D.MASKLOTNAME ");
		sql.append("   AND L.MASKLOTNAME = D.MASKLOTNAME ");
		sql.append("   AND M.MASKPROCESSFLOWNAME = L.MASKPROCESSFLOWNAME ");
		sql.append("   AND M.MASKPROCESSFLOWVERSION = L.MASKPROCESSFLOWVERSION ");
		sql.append("   AND D.MASKPROCESSFLOWNAME = L.MASKPROCESSFLOWNAME ");
		sql.append("   AND D.MASKPROCESSFLOWVERSION = L.MASKPROCESSFLOWVERSION ");
		sql.append("   AND D.MASKPROCESSOPERATIONNAME = L.MASKPROCESSOPERATIONNAME ");
		sql.append("   AND D.MASKPROCESSOPERATIONVERSION = L.MASKPROCESSOPERATIONVERSION ");
		sql.append(" ORDER BY L.POSITION ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("MASKLOTNAME", maskLotName);

		try
		{
			List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (result.size() > 0)
			{
				machineRecipeName = ConvertUtil.getMapValueByName(result.get(0), "RECIPENAME");
				machineName = ConvertUtil.getMapValueByName(result.get(0), "MACHINENAME");
				MQCList.put("RECIPENAME", machineRecipeName);
				MQCList.put("MACHINENAME",machineName);
			}
		}
		catch (Exception e)
		{

		}

		return MQCList;
	}

	public static String getSlotMapInfo(Durable durableData, List<MaskLot> maskLotList)
	{
		if (maskLotList == null)
		{
			maskLotList = new ArrayList<MaskLot>();
		}

		String normalSlotInfo = "";
		StringBuffer normalSlotInfoBuffer = new StringBuffer();

		// Get Durable's Capacity
		long iCapacity = durableData.getCapacity();

		// Make Durable Normal SlotMapInfo
		for (int i = 0; i < iCapacity; i++)
		{
			normalSlotInfoBuffer.append(GenericServiceProxy.getConstantMap().PRODUCT_NOT_IN_SLOT);
		}
		log.debug("Normal Slot Map : " + normalSlotInfoBuffer);

		for (int i = 0; i < maskLotList.size(); i++)
		{
			int index = (int) Integer.parseInt(maskLotList.get(i).getPosition()) - 1;

			normalSlotInfoBuffer.replace(index, index + 1, GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT);
		}
		log.debug("Completed Slot Map : " + normalSlotInfoBuffer);

		normalSlotInfo = normalSlotInfoBuffer.toString();

		return normalSlotInfo;
	}
	private void CheckMaskPosition( List<MaskLot> maskLotList) throws CustomException
	{
		
		for (int i = 0; i < maskLotList.size(); i++){
			
			if (maskLotList.get(i).getPosition().toString()==null || maskLotList.get(i).getPosition().toString().equals(""))
			{
				throw new CustomException("MASK-0210" , maskLotList.get(i).getMaskLotName());
			}
		}
	
	}
}