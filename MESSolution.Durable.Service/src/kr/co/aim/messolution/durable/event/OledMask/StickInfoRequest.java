package kr.co.aim.messolution.durable.event.OledMask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.MaskStick;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

public class StickInfoRequest extends SyncHandler {
	
	private static Log log = LogFactory.getLog(StickInfoRequest.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		try
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "StickInfoReply");

			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
			String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
			String stickName = SMessageUtil.getBodyItemValue(doc, "STICKNAME", true);

			//common check
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			CommonValidation.checkMachineHold(machineData);

			//check stick state , judge , spec
			MaskStick maskStickData = ExtendedObjectProxy.getMaskStickService().getMaskStickData(stickName);
			ExtendedObjectProxy.getMaskStickService().stickCommonCheck(maskStickData, false);
			ExtendedObjectProxy.getMaskStickService().checkSpecRule(maskName, stickName);
			
			MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().getMaskLotData(maskName);

			setReplyItemValue(doc,maskLotData,maskStickData);
		}
		catch (CustomException ce)
		{
			setResultItemValue(doc, "NG", ce.errorDef.getLoc_errorMessage());
			throw ce;
		}
		catch (Exception e)
		{
			setResultItemValue(doc, "NG", e.getMessage());
			throw new CustomException(e.getCause());
		}

		return doc;
	}
	
	private Document setReplyItemValue(Document doc,MaskLot maskLot,MaskStick maskStick) throws CustomException
	{
		Element bodyElement = doc.getRootElement().getChild(SMessageUtil.Body_Tag);
		bodyElement.addContent(new Element("DETACHSTICKTYPE").setText(maskLot.getDetachStickType()));
		bodyElement.addContent(new Element("DETACHPOSITION").setText(maskLot.getDetachPosition()));
		bodyElement.addContent(new Element("STICKJUDGE").setText(maskStick.getStickJudge()));
		bodyElement.addContent(new Element("STICKGRADE").setText(maskStick.getStickGrade()));
		bodyElement.addContent(new Element("TYPE").setText(maskStick.getStickType()));
		bodyElement.addContent(new Element("TP_X").setText(maskStick.getTP_X()));
		bodyElement.addContent(new Element("TP_X_JUDGE").setText(maskStick.getTP_X_JUDGE()));
		bodyElement.addContent(new Element("TP_Y").setText(maskStick.getTP_Y()));
		bodyElement.addContent(new Element("TP_Y_JUDGE").setText(maskStick.getTP_Y_JUDGE()));
		bodyElement.addContent(new Element("STRIGHTNESS").setText(maskStick.getStrightness()));
		bodyElement.addContent(new Element("SHARP").setText(maskStick.getSharp()));
		bodyElement.addContent(new Element("DEFECT_NO").setText(maskStick.getDefect_No()));
		bodyElement.addContent(new Element("CD_X_MAX").setText(maskStick.getCD_X_MAX()));
		bodyElement.addContent(new Element("CD_X_MIN").setText(maskStick.getCD_X_MIN()));
		bodyElement.addContent(new Element("CD_X_AVE").setText(maskStick.getCD_X_AVE()));
		bodyElement.addContent(new Element("CD_X_CPK").setText(maskStick.getCD_X_CPK()));
		bodyElement.addContent(new Element("CD_Y_MAX").setText(maskStick.getCD_Y_MAX()));
		bodyElement.addContent(new Element("CD_Y_MIN").setText(maskStick.getCD_Y_MIN()));
		bodyElement.addContent(new Element("CD_Y_AVE").setText(maskStick.getCD_Y_AVE()));
		bodyElement.addContent(new Element("CD_Y_CPK").setText(maskStick.getCD_Y_CPK()));
		
		return setResultItemValue(doc,"OK","");
	}
	
	private Document setResultItemValue(Document doc, String result, String resultDescription) throws CustomException
	{
		Element bodyElement = doc.getRootElement().getChild(SMessageUtil.Body_Tag);
		bodyElement.addContent(new Element("RESULT").setText(result));
		bodyElement.addContent(new Element("RESULTDESCRIPTION").setText(resultDescription));

		return doc;
	}
}