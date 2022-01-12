package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.jdom.Document;
import org.jdom.Element;

public class ReplyToUnloadRequest extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{

		Document dataDoc = (Document) doc.clone();

		Element bodyElement = SMessageUtil.getBodyElement(dataDoc);
		
		Element headerElement = null;
		try
		{
			headerElement = XmlUtil.getNode(dataDoc, new StringBuilder("//").append("Message").append("/").append("Header").toString());
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-0001", "Header");
		}

		String carrierName = bodyElement.getChildText("CSTNAME");
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

		String sourceMachineName = durableData.getUdfs().get("MACHINENAME");
		String sourcePositionType = durableData.getUdfs().get("POSITIONTYPE");
		String sourcePositionName = "";
		if (StringUtil.equals(sourcePositionType, "PORT"))
		{
			sourcePositionName = durableData.getUdfs().get("PORTNAME");
		}
		else
		{
			sourcePositionName = durableData.getUdfs().get("POSITIONNAME");
		}
		String soruceZoneName = durableData.getUdfs().get("ZONENAME");

		String destMachineName = bodyElement.getChildText("DESTMACHINENAME");
		String destPositionType = "";
		if (StringUtil.isNotEmpty(bodyElement.getChildText("DESTPORTNAME")))
		{
			destPositionType = "PORT";
		}
		else
		{
			destPositionType = "SHELF";
		}

		String destPositionName = bodyElement.getChildText("DESTPORTNAME");
		String destZoneName = bodyElement.getChildText("DESTZONENAME");
		
		// CO-FA-0014-01
		// DSP搬送优先级调高至51
		String priority = "51";
		if (StringUtil.isNotEmpty(bodyElement.getChildText("PRIORITY")))
		{
			priority = bodyElement.getChildText("PRIORITY");
		}

		String lotName = "";
		String productQuantity = "";
		String carrierState = "";

		if (StringUtil.equals(durableData.getDurableState(), "InUse"))
		{
			carrierState = "FULL";

			try
			{
				Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotInfoBydurableName(carrierName);

				lotName = lotData.getKey().getLotName();
				productQuantity = String.valueOf((int) lotData.getProductQuantity());
			}
			catch (Exception e)
			{
			}
		}
		else
		{
			carrierState = "EMPTY";
		}

		// set HEADER Element
		SMessageUtil.setItemValue(dataDoc, "Header", "MESSAGENAME", "RequestTransportJobRequest");

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

		XmlUtil.addElement(bodyElement, "TRANSPORTJOBNAME", "");

		Element carrierNameElement = new Element("CARRIERNAME");
		carrierNameElement.setText(carrierName);
		bodyElement.addContent(carrierNameElement);

		Element sourceMachineNameElement = new Element("SOURCEMACHINENAME");
		sourceMachineNameElement.setText(sourceMachineName);
		bodyElement.addContent(sourceMachineNameElement);

		Element sourcePositionTypeElement = new Element("SOURCEPOSITIONTYPE");
		sourcePositionTypeElement.setText(sourcePositionType);
		bodyElement.addContent(sourcePositionTypeElement);

		Element sourcePositionNameElement = new Element("SOURCEPOSITIONNAME");
		sourcePositionNameElement.setText(sourcePositionName);
		bodyElement.addContent(sourcePositionNameElement);

		Element soruceZoneNameElement = new Element("SOURCEZONENAME");
		soruceZoneNameElement.setText(soruceZoneName);
		bodyElement.addContent(soruceZoneNameElement);

		Element destMachineNameElement = new Element("DESTINATIONMACHINENAME");
		destMachineNameElement.setText(destMachineName);
		bodyElement.addContent(destMachineNameElement);

		Element destPositionTypeElement = new Element("DESTINATIONPOSITIONTYPE");
		destPositionTypeElement.setText(destPositionType);
		bodyElement.addContent(destPositionTypeElement);

		Element destPositionNameElement = new Element("DESTINATIONPOSITIONNAME");
		destPositionNameElement.setText(destPositionName);
		bodyElement.addContent(destPositionNameElement);

		Element destZoneNameElement = new Element("DESTINATIONZONENAME");
		destZoneNameElement.setText(destZoneName);
		bodyElement.addContent(destZoneNameElement);

		Element priorityElement = new Element("PRIORITY");
		priorityElement.setText(priority);
		bodyElement.addContent(priorityElement);

		Element carrierStateElement = new Element("CARRIERSTATE");
		carrierStateElement.setText(carrierState);
		bodyElement.addContent(carrierStateElement);

		Element carrierTypeElement = new Element("CARRIERTYPE");
		carrierTypeElement.setText(MESTransportServiceProxy.getTransportJobServiceUtil().getCarrierType(durableData));
		bodyElement.addContent(carrierTypeElement);

		Element cleanStateElement = new Element("CLEANSTATE");
		cleanStateElement.setText(MESTransportServiceProxy.getTransportJobServiceUtil().getCleanState(durableData));
		bodyElement.addContent(cleanStateElement);

		Element lotNameElement = new Element("LOTNAME");
		lotNameElement.setText(lotName);
		bodyElement.addContent(lotNameElement);

		Element productQuantityElement = new Element("PRODUCTQUANTITY");
		productQuantityElement.setText(productQuantity);
		bodyElement.addContent(productQuantityElement);

		// send to TEMsvr
		String replySubject = GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("TEMsvr");
		GenericServiceProxy.getESBServive().sendReplyBySender(replySubject, dataDoc, "TEMSender");
	
	}
}
