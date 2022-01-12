package kr.co.aim.messolution.lot.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SortJob;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.generic.util.StringUtil;

public class SorterJobListReply extends AsyncHandler {

	Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		List<Element> jobListElement = null;
		
		try
		{
			jobListElement = SMessageUtil.getBodySequenceItemList(doc, "JOBLIST", true);
		}
		catch (CustomException ex)
		{
			setErrorInfoToReturn(ex,doc);
		}
		
		String invalidJob = "";
		
		for(Element jobE : jobListElement)
		{
			SortJob dataInfo = null;
			try
			{
				dataInfo = ExtendedObjectProxy.getSortJobService().selectByKey(false, new Object[] { jobE.getChildText("JOBNAME") });
			}
			catch (Exception ex)
			{
				log.info(ex.getMessage());
			}

			if (dataInfo == null)
			{
				invalidJob += jobE.getChildText("JOBNAME") + ",";
				continue;
			}
			
			jobE.addContent(new Element("MACHINENAME").setText(machineName));
			jobE.addContent(new Element("JOBTYPE").setText(dataInfo.getJobType()));
			jobE.addContent(new Element("CREATETIME").setText(TimeStampUtil.toTimeString(dataInfo.getCreateTime())));
			jobE.addContent(new Element("CREATEUSER").setText(dataInfo.getCreateUser()));
			jobE.addContent(new Element("PRIORITY").setText(String.valueOf(dataInfo.getPriority())));
		}
		
		if (StringUtil.isNotEmpty(invalidJob))
		{
			String errorMsg = String.format("The job information registered by MES does not match the job information reported by BC.[Invalid Job Info:%s]", invalidJob);
			setErrorInfoToReturn(new CustomException("SYS-0010", StringUtil.removeEnd(errorMsg,",")), doc);
		}
		
		// send to OIC
		if (doc.getRootElement().getChild(SMessageUtil.Return_Tag) == null)
			GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), doc, "OICSender");
		else
			GenericServiceProxy.getESBServive().sendBySender(getOriginalSourceSubjectName(), JdomUtils.toString(doc), "OICSender");
	}
	
	private void setErrorInfoToReturn(CustomException customEx, Document doc) throws CustomException
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
	}
}
