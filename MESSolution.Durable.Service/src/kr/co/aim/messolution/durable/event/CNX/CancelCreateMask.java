package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class CancelCreateMask extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> durableList = SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", false);
		String origialSourceSubjectName=SMessageUtil.getHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelCreate", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		Map<String,List<String>> lineMaskListMap = new HashMap<>();
		Map<String,Machine> lineDataMap = new HashMap<>();
		
		for (Element eledur : durableList)
		{
			String sDurableName = SMessageUtil.getChildText(eledur, "DURABLENAME", true);
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);

			String lineName = DurableServiceProxy.getDurableService().selectByKey(durableData.getKey()).getUdfs().get("MASKSTOCKERNAME");

			if (lineName == null || lineName.isEmpty())
			{
				LogFactory.getLog(this.getClass()).info(String.format("Mask [%s] Stocker LineName is empty.", durableData.getKey().getDurableName()));
			}
			else
			{
				Machine machineData = null;
				
				if (lineDataMap.keySet().contains(lineName))
					machineData = lineDataMap.get(lineName);
				else
					machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(lineName);

				if (machineData.getCommunicationState().equals(GenericServiceProxy.getConstantMap().Mac_OnLineRemote))
				{
					if (lineDataMap.keySet().contains(lineName))
					{
						lineMaskListMap.get(lineName).add(sDurableName);
					}
					else
					{
						List<String> maskList = new ArrayList<>();
						maskList.add(sDurableName);

						lineDataMap.put(lineName, machineData);
						lineMaskListMap.put(lineName, maskList);
					}
				}
			}
			// CancelCreate
			MESDurableServiceProxy.getDurableServiceImpl().cancelCreateDurable(durableData, eventInfo);
		}
		
		if (lineMaskListMap.size() > 0)
		{
			for (String lineName : lineMaskListMap.keySet())
			{
				Document messageDoc = this.createSendMessage(lineMaskListMap.get(lineName), lineDataMap.get(lineName), eventInfo,origialSourceSubjectName);

				try
				{
					GenericServiceProxy.getESBServive().sendBySenderWithoutChangeReturnElement(lineDataMap.get(lineName).getUdfs().get("MCSUBJECTNAME"), messageDoc, "EISSender");
					GenericServiceProxy.getMessageTraceService().recordMessageLog(messageDoc, GenericServiceProxy.getConstantMap().INSERT_LOG_TYPE_SEND);
				}
				catch (Exception e)
				{
					//SYSTEM-0001: IMSMessage send fail
					throw new CustomException("SYSTEM-0001");
				}
			}
		}

		return doc;
	}
	
	private Document createSendMessage (List<String>  maskList,Machine lineData, EventInfo eventInfo,String origialSourceSubjectName)
	{
		Element rootElement = new Element(SMessageUtil.Message_Tag);

		Element headerElement = new Element(SMessageUtil.Header_Tag);
		{
			headerElement.addContent(new Element(SMessageUtil.MessageName_Tag).setText("IMSCancelCreateMaskSend"));
			headerElement.addContent(new Element("SHOPNAME").setText(lineData.getFactoryName()));
			headerElement.addContent(new Element("MACHINENAME").setText(lineData.getKey().getMachineName()));
			headerElement.addContent(new Element("TRANSACTIONID").setText(TimeUtils.getCurrentEventTimeKey()));
			headerElement.addContent(new Element("ORIGINALSOURCESUBJECTNAME").setText(origialSourceSubjectName));
			headerElement.addContent(new Element("SOURCESUBJECTNAME").setText(GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("CNXsvr")));
			headerElement.addContent(new Element("TARGETSUBJECTNAME").setText(lineData.getUdfs().get("MCSUBJECTNAME")));
			headerElement.addContent(new Element("EVENTUSER").setText(eventInfo.getEventUser()));
			headerElement.addContent(new Element("EVENTCOMMENT").setText(eventInfo.getEventComment()));
			headerElement.addContent(new Element("LANGUAGE").setText("ENG"));

			rootElement.addContent(headerElement);
		}

		Element bodyElement = new Element(SMessageUtil.Body_Tag);
		{
			bodyElement.addContent(new Element("LINENAME").setText(lineData.getKey().getMachineName()));

			Element maskListElement = new Element("MASKLIST");

			for (String maskId : maskList)
			{
				Element maskElement = new Element("MASK");
				maskElement.addContent(new Element("MASKID").setText(maskId));

				maskListElement.addContent(maskElement);
			}

			bodyElement.addContent(maskListElement);
			rootElement.addContent(bodyElement);
		}

		return new Document(rootElement);
	}

}
