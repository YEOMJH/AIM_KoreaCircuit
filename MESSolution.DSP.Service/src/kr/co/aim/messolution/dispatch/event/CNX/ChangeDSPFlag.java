package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;
import kr.co.aim.greentrack.port.management.data.Port;

import org.jdom.Document;
import org.jdom.Element;

public class ChangeDSPFlag extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String changeMode = SMessageUtil.getBodyItemValue(doc, "CHANGEMODE", false);
		String MachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		
		if(changeMode != null && !changeMode.isEmpty() && changeMode.equals("Y"))
		{
			String operationMode = SMessageUtil.getBodyItemValue(doc, "OPERATIONMODE", true);
			Machine machineData    = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(MachineName);
			
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("DspModeChangeReport", getEventUser(), "", "", "");
			eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

			String sDspMode = machineData.getUdfs().get("OPERATIONMODE");

			if (sDspMode.equals(operationMode))
			{
				eventLog.info("dspFlag is same so do not changed");
			}
			else
			{
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.getUdfs().put("OPERATIONMODE", operationMode);
				
				MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setEventInfo, eventInfo);
			}
		}
		else
		{
			String mDSPFlag = SMessageUtil.getBodyItemValue(doc, "DSPFLAG", true);
			List<Element> elePortList = SMessageUtil.getBodySequenceItemList(doc, "PORTLIST", true);
	
			// Change Machine DSPFlag
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(MachineName);
	
			if (!StringUtil.equals(machineData.getUdfs().get("DSPFLAG"), mDSPFlag))
			{
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeDSPFlag", getEventUser(), getEventComment(), null, null);
	
				Map<String, String> udfs = new HashMap<String, String>();
				udfs.put("DSPFLAG", mDSPFlag);
				SetEventInfo setEventInfo = MESMachineServiceProxy.getMachineInfoUtil().setEventInfo(udfs);
				MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setEventInfo, eventInfo);
			}
	
			// Change Port DSPFlag
			for (Element elePort : elePortList)
			{
				String portName = SMessageUtil.getChildText(elePort, "PORTNAME", true);
				String portLoadDSPFlag = SMessageUtil.getChildText(elePort, "LOADDSPFLAG", true);
				String portUnloadDSPFlag = SMessageUtil.getChildText(elePort, "UNLOADDSPFLAG", true);
	
				Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(MachineName, portName);
				
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeDSPFlag", getEventUser(), getEventComment(), null, null);
	
				Map<String, String> udfs = new HashMap<String, String>();
				udfs.put("LOADDSPFLAG", portLoadDSPFlag);
				udfs.put("UNLOADDSPFLAG", portUnloadDSPFlag);
				kr.co.aim.greentrack.port.management.info.SetEventInfo setEventInfo = MESPortServiceProxy.getPortInfoUtil().setEventInfo(udfs);
				MESPortServiceProxy.getPortServiceImpl().setEvent(portData, setEventInfo, eventInfo);
				//Update to FMB DSPflag  -wangchao 2021.04.16
				sendToFMB(portData.getFactoryName(),MachineName,mDSPFlag,portName,portLoadDSPFlag,portUnloadDSPFlag,eventInfo);
			}
		}

		return doc;
	}
	private void sendToFMB(String factoryName,String MachineName,String DSPFlag, String portName, String portLoadDSPFlag,String portUnloadDSPFlag,EventInfo eventInfo) throws CustomException
	{
	
		Element rootElement = new Element(SMessageUtil.Message_Tag);
		
		Element headerElement = new Element(SMessageUtil.Header_Tag);
			
		headerElement.addContent(new Element("MESSAGENAME").setText("ChangeDSPFlag"));
		//headerElement.addContent(new Element("TRANSACTIONID").setText(TimeUtils.getCurrentEventTimeKey()));
		//headerElement.addContent(new Element("TRANSACTIONID").setText(TimeStampUtil.getEventTimeKeyFromTimestamp(tLastEventTime)));
		headerElement.addContent(new Element("ORIGINALSOURCESUBJECTNAME").setText(""));
		headerElement.addContent(new Element("EVENTUSER").setText(eventInfo.getEventUser()));
		headerElement.addContent(new Element("EVENTCOMMENT").setText(eventInfo.getEventComment()));
		headerElement.addContent(new Element("LANGUAGE").setText(""));
		
		rootElement.addContent(headerElement);
		
		Element bodyElement = new Element(SMessageUtil.Body_Tag);
		XmlUtil.addElement(bodyElement, "FACTORYNAME", factoryName);
		XmlUtil.addElement(bodyElement, "MACHINENAME", MachineName);
		XmlUtil.addElement(bodyElement, "DSPFLAG", DSPFlag);
		XmlUtil.addElement(bodyElement, "PORTNAME", portName);
		XmlUtil.addElement(bodyElement, "PORTLOADDSPFLAG", portLoadDSPFlag);
		XmlUtil.addElement(bodyElement, "PORTUNLOADDSPFLAG", portUnloadDSPFlag);
		rootElement.addContent(bodyElement);
		
		//Send to FMB
		GenericServiceProxy.getESBServive().sendBySenderToFMB(new Document(rootElement));
	}
}
