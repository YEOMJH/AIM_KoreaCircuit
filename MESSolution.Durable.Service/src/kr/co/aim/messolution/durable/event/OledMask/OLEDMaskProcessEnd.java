package kr.co.aim.messolution.durable.event.OledMask;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.management.data.Port;

public class OLEDMaskProcessEnd extends SyncHandler {

	private static Log log = LogFactory.getLog(OLEDMaskProcessEnd.class);

	@Override
	public Document doWorks(Document doc) throws CustomException
	{
		try
		{
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "OLEDMaskProcessEndReply");
			 
			Element bodyElement = doc.getRootElement().getChild(SMessageUtil.Body_Tag);

			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
			String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
			String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
			String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
			String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
			String maskLotName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
			String maskRecipeName = SMessageUtil.getBodyItemValue(doc, "MASKSPECNAME", false);
			String processingInfo = SMessageUtil.getBodyItemValue(doc, "PROCESSINGINFO", false);
			String position = SMessageUtil.getBodyItemValue(doc, "POSITION", false);
			
			ConstantMap constMap = GenericServiceProxy.getConstantMap();

			MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().getMaskLotData(true, maskLotName);
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
			
			//******************************2021/09/08 由于设备/BC消息上报错误，清洗机不对OLEDMaskProcessEnd进行处理**********************************//
			if(StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_MaskOrgCleaner) || 
					StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_MaskMetalCleaner))
			{
				log.info("Error message:Machine is:"+machineName+",but message is OLEDMaskProcessEnd" );
				return setResultItemValue(doc, "OK", "");
			}

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOutMask", this.getEventUser(), this.getEventComment());

			if ((StringUtil.equals(portType, "PU") || (StringUtil.equals(portType, "PB")) && !processingInfo.equals("B")))
			{
				if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_MaskUnpacker) && maskLotData.getMaskLotState().equals(constMap.Lot_Created)
						&& !maskLotData.getMaskLotJudge().equals("G"))
				{
                    //packing case end 
					maskLotData.setMaskLotJudge(bodyElement.getChildText("MASKJUDGE"));
				    ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotData);
				}
				else
				{
					if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_MaskUnpacker))
					{
						// make Release mask lot
						maskLotData = ExtendedObjectProxy.getMaskLotService().makeReleasedAndLoggedIn(eventInfo, maskLotData, machineName, maskRecipeName, carrierName);
					}

					maskLotData = ExtendedObjectProxy.getMaskLotService().TrackOutMaskLot(eventInfo, maskLotData, bodyElement, "N");
					
					// execute future action and auto flow change
				    maskLotData = ExtendedObjectProxy.getMaskLotService().executePostAction(eventInfo, maskLotData);
				}
			}
			else
			{
				// PL PORT or PB Abort Case
				eventInfo.setEventName("CancelTrackInMask");
				
				// update processingInfo
				maskLotData.setProcessIngInfo(processingInfo);
				ExtendedObjectProxy.getMaskLotService().update(maskLotData);
				
				if (!CommonValidation.isNullOrEmpty(carrierName) && !carrierName.equals(maskLotName))
				{
					ExtendedObjectProxy.getMaskLotService().maskCancelTrackIn(eventInfo, maskLotName, carrierName, position);
				}
				else
				{
					ExtendedObjectProxy.getMaskLotService().maskCancelTrackInWithOutCarrier(eventInfo, maskLotName,position);
				}
				
				// hold mask lot
				EventInfo holdEventInfo = EventInfoUtil.makeEventInfo("HoldMask", this.getEventUser(), this.getEventComment(), "SYSTEM", "CancelTrackInMask");
			    ExtendedObjectProxy.getMaskLotService().maskMultiHold(holdEventInfo, maskLotName);
			}

			return setResultItemValue(doc, "OK", "");
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
	}
	
	private Document setResultItemValue(Document doc,String result, String resultDescription) throws CustomException
	{
		Element bodyElement = doc.getRootElement().getChild(SMessageUtil.Body_Tag);
	
		Element returnBodyElement = new Element(SMessageUtil.Body_Tag);
		returnBodyElement.addContent(new Element("MACHINENAME").setText(bodyElement.getChildText("MACHINENAME")));
		returnBodyElement.addContent(new Element("UNITNAME").setText(bodyElement.getChildText("UNITNAME")));
		returnBodyElement.addContent(new Element("CARRIERNAME").setText(bodyElement.getChildText("CARRIERNAME")));
		returnBodyElement.addContent(new Element("PORTNAME").setText(bodyElement.getChildText("PORTNAME")));
		returnBodyElement.addContent(new Element("PORTTYPE").setText(bodyElement.getChildText("PORTTYPE")));
		returnBodyElement.addContent(new Element("PORTUSETYPE").setText(bodyElement.getChildText("PORTUSETYPE")));
		returnBodyElement.addContent(new Element("MASKNAME").setText(bodyElement.getChildText("MASKNAME")));
		
		returnBodyElement.addContent(new Element("RESULT").setText(result));
		returnBodyElement.addContent(new Element("RESULTDESCRIPTION").setText(resultDescription));
		
		doc.getRootElement().getChild(SMessageUtil.Body_Tag).detach();
		//doc.getRootElement().addContent(returnBodyElement);
		doc.getRootElement().addContent(2,returnBodyElement); //Modify by cjl 20201013

		return doc;
	}
}
