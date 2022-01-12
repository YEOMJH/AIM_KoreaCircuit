package kr.co.aim.messolution.durable.event.OledMask;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskRecipe;
import kr.co.aim.messolution.extended.object.management.impl.ReserveMaskRecipeService;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;

public class OLEDMaskInfoDownLoadRequest extends SyncHandler 
{
	private static Log log = LogFactory.getLog(OLEDMaskInfoDownLoadRequest.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		try
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "OLEDMaskInfoDownLoadSend");

			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
			String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
			String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
			String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
			String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);

			ConstantMap constMap = GenericServiceProxy.getConstantMap();
			
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			CommonValidation.checkMachineHold(machineData);

			MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().getMaskLotData(true, maskName);
			String machineRecipeName = "";
			
			if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_MaskUnpacker))
			{
				// unpacker
				if (!maskLotData.getMaskLotState().equals(GenericServiceProxy.getConstantMap().Lot_Shipped) && !maskLotData.getMaskLotState().equals(GenericServiceProxy.getConstantMap().Lot_Created))
				{
					throw new CustomException("OLEDMASK-0018", maskLotData.getMaskLotName(),maskLotData.getMaskLotState());
				}
				
				if (maskLotData.getMaskLotHoldState().equals(GenericServiceProxy.getConstantMap().MaskLotHoldState_OnHold))
				{
					throw new CustomException("MASK-0013", maskLotData.getMaskLotName());
				}
			}
			else
			{
				commonCheck(maskLotData);

				if (!StringUtil.equals(machineData.getMachineGroupName(), "Tension"))
				{
					if (maskLotData.getCleanState().equals(GenericServiceProxy.getConstantMap().Dur_Dirty))
					{
						throw new CustomException("MASK-0025", maskLotData.getMaskLotName());
					}
				}
				
				// Recipe
			    if (StringUtils.equals(machineData.getMachineGroupName(), constMap.MachineGroup_MaskAOI))
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
				else if (StringUtils.equals(machineData.getMachineGroupName(), constMap.MachineGroup_MaskPPA))
				{
					try
					{
						ReserveMaskRecipeService reserveMaskRecipeService = ExtendedObjectProxy.getReserveMaskRecipeService();
						Object[] keySet = new Object[] { maskLotData.getMaskLotName(), maskLotData.getMaskSpecName(), maskLotData.getMaskProcessFlowName(), maskLotData.getMaskProcessFlowVersion(),
								maskLotData.getMaskProcessOperationName(), maskLotData.getMaskProcessOperationVersion(), machineName };
						ReserveMaskRecipe reserveMaskRecipe = reserveMaskRecipeService.selectByKey(true, keySet);

						machineRecipeName = reserveMaskRecipe.getRecipeName();
					}
					catch (greenFrameDBErrorSignal nfds)
					{
						throw new CustomException("RECIPE-0012", "ReserveMaskRecipe", String.format("Mask =%s ,Spec = %s,Flow = %s Oper = %s", maskLotData.getMaskLotName(),
								maskLotData.getMaskSpecName(), maskLotData.getMaskProcessFlowName(), maskLotData.getMaskProcessOperationName()));
					}
					
					// Operation Validation
					List<Map<String, Object>> trfoPolicyAndMachine = ExtendedObjectProxy.getMaskLotService().getTrfoPolicyAndMachine(maskLotData.getFactoryName(), maskLotData.getMaskSpecName(),
							maskLotData.getMaskProcessFlowName(), maskLotData.getMaskProcessFlowVersion(), maskLotData.getMaskProcessOperationName(), maskLotData.getMaskProcessOperationVersion(),
							machineName);

					if (trfoPolicyAndMachine.size() == 0)
					{
						throw new CustomException("MASK-0061", maskLotData.getMaskLotName());
					}
				}
				else if (StringUtils.equals(machineData.getMachineGroupName(), constMap.MachineGroup_MaskTension))
				{
					machineRecipeName = "";
				}
				else
				{
					machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipeForOLEDMask(maskLotData.getFactoryName(), maskLotData.getMaskSpecName(),
							maskLotData.getMaskProcessFlowName(), maskLotData.getMaskProcessFlowVersion(), maskLotData.getMaskProcessOperationName(),
							maskLotData.getMaskProcessOperationVersion(), machineName);
				}
			}
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("OLEDMaskInfoDownLoadSend", getEventUser(), getEventComment(), null, null);
			MaskLot maskLotInfo = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskName });

			if (maskLotInfo != null)
			{
				maskLotInfo.setJobDownFlag("Y");

				ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotInfo);
			}
			// Set bodyElement
			this.generateBodyTemplate(doc, maskLotData, machineRecipeName,machineData);

			// Set slotMap
			this.setMaskMap(doc);

			return doc;
		}
		catch (CustomException ce)
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "OLEDMaskCancelCommandSend");

			this.generateNGBodyTemplate(doc, SMessageUtil.getBodyElement(doc));

			throw new CustomException("SYS-0010", ce.errorDef.getLoc_errorMessage());
		}
		catch (Exception e)
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "OLEDMaskCancelCommandSend");

			this.generateNGBodyTemplate(doc, SMessageUtil.getBodyElement(doc));

			throw new CustomException(e);
		}
	}
	
	private void commonCheck(MaskLot maskLotData) throws CustomException
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

	private Element generateBodyTemplate(Document doc, MaskLot maskData, String machineRecipeName,Machine machineData) throws CustomException
	{
		Element bodyElement = SMessageUtil.getBodyElement(doc);

		Element eleMaskQuantity = new Element("MASKQUANTITY");
		eleMaskQuantity.setText("1");
		bodyElement.addContent(eleMaskQuantity);

		Element eleSlotMap = new Element("SLOTMAP");
		eleSlotMap.setText(StringUtil.EMPTY);
		bodyElement.addContent(eleSlotMap);

		Element eleInputSlotMap = new Element("INPUTMASKMAP");
		eleInputSlotMap.setText(StringUtil.EMPTY);
		bodyElement.addContent(eleInputSlotMap);

		Element eleMaskSpec = new Element("MASKSPECNAME");
		eleMaskSpec.setText(maskData.getMaskSpecName());
		bodyElement.addContent(eleMaskSpec);

		Element eleProcessFlow = new Element("PROCESSFLOWNAME");
		eleProcessFlow.setText(maskData.getMaskProcessFlowName());
		bodyElement.addContent(eleProcessFlow);

		Element eleProcessFlowVer = new Element("PROCESSFLOWVERSION");
		eleProcessFlowVer.setText(maskData.getMaskProcessFlowVersion());
		bodyElement.addContent(eleProcessFlowVer);

		Element eleProcessOper = new Element("PROCESSOPERATIONNAME");
		eleProcessOper.setText(maskData.getMaskProcessOperationName());
		bodyElement.addContent(eleProcessOper);

		Element eleProcessOperVer = new Element("PROCESSOPERATIONVERSION");
		eleProcessOperVer.setText(maskData.getMaskProcessOperationVersion());
		bodyElement.addContent(eleProcessOperVer);

		Element elePosition = new Element("POSITION");
		elePosition.setText(maskData.getPosition());
		bodyElement.addContent(elePosition);

		Element eleMaskMachineRecipe = new Element("MASKRECIPENAME");
		eleMaskMachineRecipe.setText(machineRecipeName);
		bodyElement.addContent(eleMaskMachineRecipe);

		Element eleMaskType = new Element("MASKTYPE");
		eleMaskType.setText(maskData.getMaskType());
		bodyElement.addContent(eleMaskType);

		Element eleMaskModelNo = new Element("MASKMODELNO");
		eleMaskModelNo.setText(maskData.getMaskModelName());
		bodyElement.addContent(eleMaskModelNo);

		Element eleMaskThk = new Element("MASKTHICKNESS");
		Double deleMaskThk = Double.valueOf(maskData.getMaskThickness()) * 10000;
		eleMaskThk.setText(String.valueOf(deleMaskThk.intValue()));
		bodyElement.addContent(eleMaskThk);

		Element eleMagnet = new Element("MAGNET");
		eleMagnet.setText(String.valueOf(maskData.getMagnet()));
		bodyElement.addContent(eleMagnet);

		Element maskUsedLimit = new Element("MASKUSEDLIMIT");
		maskUsedLimit.setText(new DecimalFormat("#.##").format(maskData.getTimeUsedLimit()));
		//maskUsedLimit.setText(maskData.getTimeUsedLimit().toString());
		bodyElement.addContent(maskUsedLimit);

		Element maskUsedCount = new Element("MASKUSEDCOUNT");
		maskUsedCount.setText(new DecimalFormat("#.##").format(maskData.getTimeUsed()));
		//maskUsedCount.setText(maskData.getTimeUsed().toString());
		bodyElement.addContent(maskUsedCount);

		Element maskJudge = new Element("MASKJUDGE");
		maskJudge.setText(String.valueOf(maskData.getMaskLotJudge()));
		bodyElement.addContent(maskJudge);

		if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_EVA) && StringUtils.equals(maskData.getMaskKind(), "EVA"))
		{
			Element eleMask_OffSet_X = new Element("INSPECTION_OFFSET_X");
			eleMask_OffSet_X.setText(maskData.getInitialOffSetX());
			bodyElement.addContent(eleMask_OffSet_X);

			Element eleMask_OffSet_Y = new Element("INSPECTION_OFFSET_Y");
			eleMask_OffSet_Y.setText(maskData.getInitialOffSetY());
			bodyElement.addContent(eleMask_OffSet_Y);

			Element eleMask_OffSet_T = new Element("INSPECTION_OFFSET_THETA");
			eleMask_OffSet_T.setText(maskData.getInitialOffSetTheta());
			bodyElement.addContent(eleMask_OffSet_T);

		}
		else
		{
			Element eleMask_OffSet_X = new Element("INSPECTION_OFFSET_X");
			eleMask_OffSet_X.setText(null);
			bodyElement.addContent(eleMask_OffSet_X);

			Element eleMask_OffSet_Y = new Element("INSPECTION_OFFSET_Y");
			eleMask_OffSet_Y.setText(null);
			bodyElement.addContent(eleMask_OffSet_Y);

			Element eleMask_OffSet_T = new Element("INSPECTION_OFFSET_THETA");
			eleMask_OffSet_T.setText(null);
			bodyElement.addContent(eleMask_OffSet_T);

		}
		
		//Only Used In EVA
		Element eleChamberName = new Element("CHAMBERNAME");
		eleChamberName.setText("");
		bodyElement.addContent(eleChamberName);

		Element maskFlowState = new Element("MASKFLOWSTATE");
		maskFlowState.setText(maskData.getMaskFlowState());
		bodyElement.addContent(maskFlowState);

		Element collZPosition = new Element("COOLZPOSITION");
		collZPosition.setText(maskData.getCool_Z_Position());
		bodyElement.addContent(collZPosition);

		Element alignRecipe = new Element("ALIGNRECIPE");
		alignRecipe.setText(maskData.getAlignRecipe());
		bodyElement.addContent(alignRecipe);

		Element repairCount = new Element("REPAIRCOUNT");
		repairCount.setText(maskData.getMaskRepairCount() == null ? "0" : new DecimalFormat("#.##").format(maskData.getMaskRepairCount()));
		//repairCount.setText(maskData.getMaskRepairCount() == null ? "0" : maskData.getMaskRepairCount().toString());
		bodyElement.addContent(repairCount);

		// MaintisID : 0000436
		// 用户需求在OLEDMaskInfoDownLoadSend中增加AOICount Item
		Element aoiCount = new Element("AOICOUNT");
		aoiCount.setText(maskData.getAoiCount() == null ? "0" : new DecimalFormat("#.##").format(maskData.getAoiCount()));
		bodyElement.addContent(aoiCount);
		
		Element reworkFlag = new Element("REWORKFLAG");
		reworkFlag.setText("InRework".equals(maskData.getReworkState())?"Y":"N");
		bodyElement.addContent(reworkFlag);

		Element maskCycleCount = new Element("MASKCYCLECOUNT");
		maskCycleCount.setText(maskData.getMaskCycleCount() == null ? "0" : new DecimalFormat("#.##").format(maskData.getMaskCycleCount()));
		//maskCycleCount.setText(maskData.getMaskCycleCount() == null ? "0" : maskData.getMaskCycleCount().toString());
		bodyElement.addContent(maskCycleCount);

		Element frameName = new Element("FRAMENAME");
		frameName.setText(maskData.getFrameName());
		bodyElement.addContent(frameName);
		
		if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_EVA) && StringUtils.equals(maskData.getMaskKind(), "TFE"))
		{
			Element eleOffsetX1 = new Element("TFEOFFSETX_1");
			eleOffsetX1.setText(maskData.getTFEOFFSETX1());
			bodyElement.addContent(eleOffsetX1);
			
			Element eleOffsetY1 = new Element("TFEOFFSETY_1");
			eleOffsetY1.setText(maskData.getTFEOFFSETY1());
			bodyElement.addContent(eleOffsetY1);
			
			Element eleOffsetX2 = new Element("TFEOFFSETX_2");
			eleOffsetX2.setText(maskData.getTFEOFFSETX2());
			bodyElement.addContent(eleOffsetX2);
			
			Element eleOffsetY2 = new Element("TFEOFFSETY_2");
			eleOffsetY2.setText(maskData.getTFEOFFSETY2());
			bodyElement.addContent(eleOffsetY2);
		}
		else
		{
			Element eleOffsetX1 = new Element("TFEOFFSETX_1");
			eleOffsetX1.setText(null);
			bodyElement.addContent(eleOffsetX1);
			
			Element eleOffsetY1 = new Element("TFEOFFSETY_1");
			eleOffsetY1.setText(null);
			bodyElement.addContent(eleOffsetY1);
			
			Element eleOffsetX2 = new Element("TFEOFFSETX_2");
			eleOffsetX2.setText(null);
			bodyElement.addContent(eleOffsetX2);
			
			Element eleOffsetY2 = new Element("TFEOFFSETY_2");
			eleOffsetY2.setText(null);
			bodyElement.addContent(eleOffsetY2);
		}
		
		
		// first removal of existing node would be duplicated
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		// index of Body node is static
		doc.getRootElement().addContent(2, bodyElement);

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

		Element maskNameElement = new Element("MASKNAME");
		maskNameElement.setText(bodyElementOri.getChildText("MASKNAME"));
		bodyElement.addContent(maskNameElement);

		// first removal of existing node would be duplicated
		doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		// index of Body node is static
		doc.getRootElement().addContent(2, bodyElement);
	}

	private void setMaskMap(Document doc) throws CustomException
	{
		eventLog.debug("Completed Mask Selection : " + "O");

		SMessageUtil.setBodyItemValue(doc, "SLOTMAP", "O");
		SMessageUtil.setBodyItemValue(doc, "INPUTMASKMAP", "O");

	}
}