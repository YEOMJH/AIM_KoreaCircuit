package kr.co.aim.messolution.lot.event;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.orm.info.DataInfo;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineKey;

public class SorterJobListRequest extends AsyncHandler {

	Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		ConstantMap constMap = GenericServiceProxy.getConstantMap();

		MachineKey keyInfo = new MachineKey();
		keyInfo.setMachineName(machineName);

		Machine dataInfo = MachineServiceProxy.getMachineService().selectByKey(keyInfo);

		if (dataInfo.getCommunicationState().equals(constMap.Mac_OffLine))
		{
			this.interruptDowork(new CustomException("MACHINE-0003", dataInfo.getKey().getMachineName()), doc);
		}

		if ((dataInfo.getMachineGroupName().equals(constMap.MachineGroup_Sorter)&&!dataInfo.getUdfs().get("OPERATIONMODE").equals(constMap.SORT_OPERATIONMODE_NORMAL)
				||(!dataInfo.getMachineGroupName().equals(constMap.MachineGroup_Sorter)&&!dataInfo.getUdfs().get("OPERATIONMODE").equals(constMap.SORT_OPERATIONMODE))))
		{
			this.interruptDowork(new CustomException("MACHINE-0100", dataInfo.getKey().getMachineName(), dataInfo.getUdfs().get("OPERATIONMODE")), doc);
		}

		// send to EAP
		GenericServiceProxy.getESBServive().sendBySender(dataInfo.getUdfs().get("MCSUBJECTNAME"), doc, "EISSender");
	}

	private void interruptDowork(CustomException customEx, Document doc) throws CustomException
	{
		String language = "English";

		try
		{
			language = JdomUtils.getNodeText(doc, "//" + SMessageUtil.Message_Tag + "/" + SMessageUtil.Header_Tag + "/" + "LANGUAGE");
		}
		catch (Exception ex)
		{
		}

		String errorCode = customEx.errorDef.getErrorCode();
		String errorMessage = customEx.errorDef.getEng_errorMessage();

		if ("Chinese".equals(language))
		{
			errorMessage = customEx.errorDef.getCha_errorMessage();
		}
		else if ("Korean".equals(language))
		{
			errorMessage = customEx.errorDef.getKor_errorMessage();
		}

		Element returnElement = doc.getRootElement().getChild(SMessageUtil.Return_Tag);

		if (returnElement == null)
		{
			returnElement = new Element(SMessageUtil.Return_Tag);
			returnElement.addContent(new Element(SMessageUtil.Result_ReturnCode));
			returnElement.addContent(new Element(SMessageUtil.Result_ErrorMessage));
			doc.getRootElement().addContent(returnElement);
		}

		returnElement.getChild(SMessageUtil.Result_ReturnCode).setText(errorCode);
		returnElement.getChild(SMessageUtil.Result_ErrorMessage).setText(errorMessage);

		// send to OIC
		GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), JdomUtils.toString(doc), "OICSender");
		throw customEx;
	}
}
