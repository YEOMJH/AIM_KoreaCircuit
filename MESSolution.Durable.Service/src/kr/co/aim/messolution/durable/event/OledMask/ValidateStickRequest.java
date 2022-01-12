package kr.co.aim.messolution.durable.event.OledMask;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskStick;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

public class ValidateStickRequest extends SyncHandler {
	
	private static Log log = LogFactory.getLog(ValidateStickRequest.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		try
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "ValidateStickReply");

			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
			String stickName = SMessageUtil.getBodyItemValue(doc, "STICKNAME", true);

			// common check
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			CommonValidation.checkMachineHold(machineData);

			MaskStick maskStickData = ExtendedObjectProxy.getMaskStickService().getMaskStickData(stickName);
			//Allow Available F Type Stick do MII
//			ExtendedObjectProxy.getMaskStickService().stickCommonCheck(maskStickData, true);

			//Add TSPolicy Check 2020-12-09
			List<Map<String, Object>> policyInfo = PolicyUtil.getTSMachineInfo(machineData.getFactoryName(), maskStickData.getStickSpecName(), machineName);
			String machineRecipeName = policyInfo.get(0).get("MACHINERECIPENAME").toString();
			
			if(StringUtil.isEmpty(machineRecipeName))
			{
				throw new CustomException("RMS-0005");
			}
			
			setResultItemValue(doc, machineRecipeName, "OK", "");
		}
		catch (CustomException ce)
		{
			setResultItemValue(doc, "", "NG", ce.errorDef.getLoc_errorMessage());
			throw ce;
		}
		catch (Exception e)
		{
			setResultItemValue(doc, "", "NG", e.getMessage());
			throw new CustomException(e.getCause());
		}

		return doc;
	}

	private Document setResultItemValue(Document doc, String recipeId, String result, String resultDescription) throws CustomException
	{
		Element bodyElement = doc.getRootElement().getChild(SMessageUtil.Body_Tag);
		bodyElement.addContent(new Element("RECIPEID").setText(recipeId));
		bodyElement.addContent(new Element("RESULT").setText(result));
		bodyElement.addContent(new Element("RESULTDESCRIPTION").setText(resultDescription));

		return doc;
	}
}