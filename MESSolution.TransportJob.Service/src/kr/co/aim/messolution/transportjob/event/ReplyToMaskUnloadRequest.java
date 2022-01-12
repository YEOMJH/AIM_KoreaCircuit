package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ReplyToMaskUnloadRequest extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		Document dataDoc = (Document) doc.clone();

		Element bodyElement = SMessageUtil.getBodyElement(dataDoc);
		
		Element headerElement = null;
		try
		{
			headerElement = JdomUtils.getNode(dataDoc, new StringBuilder("//").append("Message").append("/").append("Header").toString());
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-0001", "Header");
		}

		String maskName = bodyElement.getChildText("CSTNAME");
		MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().getMaskLotData(maskName);
		
		String destMachineName = bodyElement.getChildText("DESTMACHINENAME");
		String destPositionName = bodyElement.getChildText("DESTPORTNAME");
		String destZoneName = bodyElement.getChildText("DESTZONENAME");
		
		// set HEADER Element
		SMessageUtil.setItemValue(dataDoc, "Header", "MESSAGENAME", "RequestMaskTransportJobRequest");

		Element originalSourceSubjectElement = new Element("ORIGINALSOURCESUBJECTNAME");
		originalSourceSubjectElement.setText(SMessageUtil.getHeaderItemValue(dataDoc, "REPLYSUBJECTNAME", false));
		headerElement.addContent(originalSourceSubjectElement);

		Element eventUserElement = new Element("EVENTUSER");
		eventUserElement.setText(SMessageUtil.getHeaderItemValue(dataDoc, "REQUESTOR", true));
		headerElement.addContent(eventUserElement);

		Element eventCommentElement = new Element("EVENTCOMMENT");
		eventCommentElement.setText("");
		headerElement.addContent(eventCommentElement);

		// set BODY Element
		bodyElement.removeContent();

		JdomUtils.addElement(bodyElement, "TRANSPORTJOBNAME", "");
		JdomUtils.addElement(bodyElement, "MASKNAME", maskName);
		JdomUtils.addElement(bodyElement, "SOURCEMACHINENAME", maskLotData.getMachineName());
		
		if (StringUtils.isEmpty(maskLotData.getPortName()))
		{
			JdomUtils.addElement(bodyElement, "SOURCEPOSITIONTYPE", "SHELF");
			JdomUtils.addElement(bodyElement, "SOURCEPOSITIONNAME", "");
			JdomUtils.addElement(bodyElement, "SOURCEZONENAME", maskLotData.getZoneName());
		}
		else
		{
			JdomUtils.addElement(bodyElement, "SOURCEPOSITIONTYPE", "PORT");
			JdomUtils.addElement(bodyElement, "SOURCEPOSITIONNAME", maskLotData.getPortName());
			JdomUtils.addElement(bodyElement, "SOURCEZONENAME", "");
		}
		
		JdomUtils.addElement(bodyElement, "SOURCECARRIERNAME", "");
		JdomUtils.addElement(bodyElement, "SOURCECARRIERSLOTNO", "");
		JdomUtils.addElement(bodyElement, "DESTINATIONMACHINENAME", destMachineName);
		
		if (StringUtils.isEmpty(destPositionName))
		{
			JdomUtils.addElement(bodyElement, "DESTINATIONPOSITIONTYPE", "SHELF");
			JdomUtils.addElement(bodyElement, "DESTINATIONPOSITIONNAME", "");
			JdomUtils.addElement(bodyElement, "DESTINATIONZONENAME", destZoneName);
		}
		else
		{
			JdomUtils.addElement(bodyElement, "DESTINATIONPOSITIONTYPE", "PORT");
			JdomUtils.addElement(bodyElement, "DESTINATIONPOSITIONNAME", destPositionName);
			JdomUtils.addElement(bodyElement, "DESTINATIONZONENAME", "");	
		}
		
		JdomUtils.addElement(bodyElement, "DESTINATIONCARRIERNAME", "");
		JdomUtils.addElement(bodyElement, "DESTINATIONCARRIERSLOTNO", "");
		JdomUtils.addElement(bodyElement, "PRIORITY", "51");
		JdomUtils.addElement(bodyElement, "MASKTYPE", maskLotData.getMaskType());
		
		// send to TEMsvr
		String replySubject = GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("TEMsvr");
		GenericServiceProxy.getESBServive().sendReplyBySender(replySubject, dataDoc, "TEMSender");
	}
}
