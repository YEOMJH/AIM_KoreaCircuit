package kr.co.aim.messolution.transportjob.event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakeTransferStateInfo;

import org.jdom.Document;
import org.jdom.Element;

public class RTDRequestTransportJobRequest extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException 
	{
		List<Element> dataList = SMessageUtil.getBodySequenceItemList(doc, "DATALIST", false);
		
		for(Element dataE : dataList)
		{
			Document dataDoc = (Document)doc.clone();
			
			Element bodyElement = SMessageUtil.getBodyElement(dataDoc);
			bodyElement.removeChild("DATALIST");
			Element headerElement = null;
			try 
			{
				headerElement = XmlUtil.getNode(dataDoc, new StringBuilder("//").append("Message")
														.append("/").append("Header")
														.toString());
			} catch (Exception e) {
				throw new CustomException("SYS-0001", "Header");
			}
			
			
			String carrierName = dataE.getChildText("CSTNAME");
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
			
			String sourceMachineName = durableData.getUdfs().get("MACHINENAME");
			String sourcePositionType = durableData.getUdfs().get("POSITIONTYPE");
			String sourcePositionName = "";
			if(StringUtil.equals(sourcePositionType, "PORT"))
				sourcePositionName = durableData.getUdfs().get("PORTNAME");
			else
				durableData.getUdfs().get("POSITIONNAME");
			String soruceZoneName = durableData.getUdfs().get("ZONENAME");
			
			String destMachineName = dataE.getChildText("DESTMACHINENAME");
			String destPositionType = "";
			if(StringUtil.isNotEmpty(dataE.getChildText("DESTPORTNAME")))
				destPositionType = "PORT";
			
			String destPositionName = dataE.getChildText("DESTPORTNAME");
			String destZoneName = dataE.getChildText("DESTZONENAME");
			String priority = "50";
			
			String lotName = "";
			String productQuantity = "";
			String carrierState = "";
			
			if(StringUtil.equals(durableData.getDurableState(), "InUse"))
			{
				carrierState = "FULL";
				
				try
				{
					Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotInfoBydurableName(carrierName);
					
					lotName = lotData.getKey().getLotName();
					productQuantity = String.valueOf((int)lotData.getProductQuantity());
				}
				catch(Exception e){}
				
			}
			else
			{
				carrierState = "EMPTY";
			}
			
			//set HEADER Element
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
			
			
			//set BODY Element
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
			
			Element lotNameElement = new Element("LOTNAME");
			lotNameElement.setText(lotName);
			bodyElement.addContent(lotNameElement);
			
			Element productQuantityElement = new Element("PRODUCTQUANTITY");
			productQuantityElement.setText(productQuantity);
			bodyElement.addContent(productQuantityElement);
			
			Element carrierStateElement = new Element("CARRIERSTATE");
			carrierStateElement.setText(carrierState);
			bodyElement.addContent(carrierStateElement);
			
			Element priorityElement = new Element("PRIORITY");
			priorityElement.setText(priority);
			bodyElement.addContent(priorityElement);
			
			// send to TEMsvr
			String replySubject = GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("TEMsvr");
			GenericServiceProxy.getESBServive().sendReplyBySender(replySubject, dataDoc, "TEMSender");
		}
	}
}
