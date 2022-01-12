package kr.co.aim.messolution.durable.event.OledMask;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

public class OLEDMaskCreateInfoDownloadRequest extends SyncHandler {
	private static Log log = LogFactory.getLog(OLEDMaskCreateInfoDownloadRequest.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		try
		{

			Element bodyElement = SMessageUtil.getBodyElement(doc);

			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "OLEDMaskCreateInfoDownloadSend");

			ConstantMap constantMap = GenericServiceProxy.getConstantMap();
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
			String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
			String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", true);
			String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);

			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName);

			CommonValidation.checkMachineHold(machineData);

			if (!CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PL"))
			{
				eventLog.info("PU Loader Port job download Request");
				throw new CustomException("DURABLE-9003", maskName);
			}

			MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskName });

			// Common validation
			ExtendedObjectProxy.getMaskLotService().CheckCommonTrackIn(maskLotData);

			// Recipe
			String machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil()
					.getMachineRecipeForOLEDMask(maskLotData.getFactoryName(), maskLotData.getMaskSpecName(), maskLotData.getMaskProcessFlowName(), maskLotData.getMaskProcessFlowVersion(),
							maskLotData.getMaskProcessOperationName(), maskLotData.getMaskProcessOperationVersion(), machineName);

			// Set bodyElement
			this.generateBodyTemplate(doc, maskLotData, machineRecipeName);

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

	private Element generateBodyTemplate(Document doc, MaskLot maskData, String machineRecipeName) throws CustomException
	{
		Element bodyElement = null;
		bodyElement = new Element("Body");

		Element machineNameElement = new Element("MACHINENAME");
		machineNameElement.setText(bodyElement.getChildText("MACHINENAME"));
		bodyElement.addContent(machineNameElement);

		Element unitNameElement = new Element("UNITNAME");
		unitNameElement.setText(bodyElement.getChildText("UNITNAME"));
		bodyElement.addContent(unitNameElement);

		Element portNameElement = new Element("PORTNAME");
		portNameElement.setText(bodyElement.getChildText("PORTNAME"));
		bodyElement.addContent(portNameElement);

		Element portTypeElement = new Element("PORTTYPE");
		portTypeElement.setText(bodyElement.getChildText("PORTTYPE"));
		bodyElement.addContent(portTypeElement);

		Element portUseTypeElement = new Element("PORTUSETYPE");
		portUseTypeElement.setText(bodyElement.getChildText("PORTUSETYPE"));
		bodyElement.addContent(portUseTypeElement);

		Element eleMaskQuantity = new Element("MASKQUANTITY");
		eleMaskQuantity.setText("1");
		bodyElement.addContent(eleMaskQuantity);

		Element eleSlotMap = new Element("SLOTMAP");
		eleSlotMap.setText(StringUtil.EMPTY);
		bodyElement.addContent(eleSlotMap);

		Element eleInputSlotMap = new Element("INPUTSLOTMAP");
		eleInputSlotMap.setText(StringUtil.EMPTY);
		bodyElement.addContent(eleInputSlotMap);

		Element eleMaskName = new Element("MASKNAME");
		eleMaskName.setText(maskData.getMaskLotName());
		bodyElement.addContent(eleMaskName);

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

		Element eleMaskUsedLimit = new Element("MASKUSEDLIMIT");
		eleMaskUsedLimit.setText(String.valueOf(maskData.getDurationUsedLimit()));
		bodyElement.addContent(eleMaskUsedLimit);

		Element eleMaskUsedCount = new Element("MASKUSEDCOUNT");
		eleMaskUsedCount.setText(String.valueOf(maskData.getTimeUsed()));
		bodyElement.addContent(eleMaskUsedCount);

		Element eleMaskThk = new Element("MASKTHICKNESS");
		eleMaskThk.setText(maskData.getMaskThickness());
		bodyElement.addContent(eleMaskThk);

		Element eleMagnet = new Element("MAGNET");
		eleMagnet.setText(String.valueOf(maskData.getMagnet()));
		bodyElement.addContent(eleMagnet);

		Element eleMaskJudge = new Element("MASKJUDGE");
		eleMaskJudge.setText(maskData.getMaskLotJudge());
		bodyElement.addContent(eleMaskJudge);

		Element eleMask_OffSet_X = new Element("INSPECTION_OFFSET_X");
		eleMask_OffSet_X.setText(maskData.getInitialOffSetX());
		bodyElement.addContent(eleMask_OffSet_X);

		Element eleMask_OffSet_Y = new Element("INSPECTION_OFFSET_Y");
		eleMask_OffSet_Y.setText(maskData.getInitialOffSetY());
		bodyElement.addContent(eleMask_OffSet_Y);

		Element eleMask_OffSet_T = new Element("INSPECTION_OFFSET_THETA");
		eleMask_OffSet_T.setText(maskData.getInitialOffSetTheta());
		bodyElement.addContent(eleMask_OffSet_T);

		Element eleChamberName = new Element("CHAMBERNAME");
		eleChamberName.setText(maskData.getChamberName());
		bodyElement.addContent(eleChamberName);

		Element eleStageName = new Element("STAGENAME");
		eleStageName.setText(maskData.getStageName());
		bodyElement.addContent(eleStageName);

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