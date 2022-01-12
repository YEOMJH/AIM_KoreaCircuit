package kr.co.aim.messolution.lot.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class SorterJobCancelCommandReply extends AsyncHandler {

	private Log log = LogFactory.getLog(SorterJobCancelCommandReply.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String jobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);
		String Result = SMessageUtil.getBodyItemValue(doc, "RESULT", true);
		String ResultDescription = SMessageUtil.getBodyItemValue(doc, "RESULTDESCRIPTION", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Cancel", getEventUser(), ResultDescription, null, null);

		if (StringUtil.equals(Result, "NG"))
		{
			log.info("Sorter Job cancel command  reply NG .[ " + ResultDescription + " ]");
		}
		else
		{
			try
			{
				MESLotServiceProxy.getLotServiceUtil().changeSortJobState(eventInfo, jobName, GenericServiceProxy.getConstantMap().SORT_JOBSTATE_CANCELED);

				log.info("Sorter Job cancel command Reply OK !!");
			}
			catch (CustomException customEx)
			{
				interruptDowork(customEx, doc);
			}
			catch (Exception ex)
			{
				interruptDowork(new CustomException("SYS-0010", ex.getMessage()), doc);
			}
		}

		// send to OIC
		if (doc.getRootElement().getChild(SMessageUtil.Return_Tag) == null)
			GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");
		else
			GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), JdomUtils.toString(doc), "OICSender");

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
