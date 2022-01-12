package kr.co.aim.messolution.durable.event.OledMask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.CheckOffset;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.impl.MaskLotService;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;

public class OLEDMaskCSTProcessEnd extends SyncHandler 
{
	private static Log log = LogFactory.getLog(OLEDMaskCSTProcessEnd.class);

	@Override
	public Document doWorks(Document doc) throws CustomException
	{
		try
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "OLEDMaskCSTProcessEndReply");
			
			List<MaskLot> dataInfoList = new ArrayList<MaskLot>();
		    String skipFlag = null;
			
		    String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
			String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
			String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
			String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
			String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
			String maskQuantity = SMessageUtil.getBodyItemValue(doc, "MASKQUANTITY", false);
			String slotMap = SMessageUtil.getBodyItemValue(doc, "SLOTMAP", false);
			String inputMaskMap = SMessageUtil.getBodyItemValue(doc, "INPUTMASKMAP", false);
			List<Element> maskLotList = SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", false);

			Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOutMask", getEventUser(), getEventComment());

			if (StringUtil.equals(portType, "PL"))
			{
				if (maskLotList == null || maskLotList.size() == 0)
				{
					// Mantis : 0000440
					// 所有Mask CST，当TimeUseCount>TimeUseCountLimit时，MaskCST变Dirty
					MESDurableServiceProxy.getDurableServiceImpl().incrementTimeUseCount(durableData, 1, eventInfo);
				}
				else
				{
					// 2021-02-19	dhko	CancelTrackIn
					eventInfo.setEventName("AssignCarrierMask");
					MESDurableServiceProxy.getDurableServiceImpl().durableStateChangeAfterOLEDMaskLotProcess(carrierName, "DEASSIGN", String.valueOf(maskLotList.size()), eventInfo);
					
					for (Element maskLot : maskLotList)
					{
						String maskLotName = maskLot.getChildText("MASKLOTNAME");
						MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });
						
						CommonValidation.checkMaskLotHoldState(maskLotData);
						CommonValidation.checkMaskLotState(maskLotData);
						CommonValidation.checkMaskLotProcessStateNotRun(maskLotData);
						
						// Cancel Track Mask Lot
						// CT_MASKLOT / CT_MASKLOTHISTORY Insert
						// -> CARRIERNAME = Key-in, MASKLOTPROCESSSTATE = WAIT
						maskLotData.setMaskLotProcessState(GenericServiceProxy.getConstantMap().MaskLotProcessState_Wait);
						maskLotData.setMaskLotState(GenericServiceProxy.getConstantMap().Lot_Released);
						maskLotData.setMachineName("");
						maskLotData.setPortName("");
						maskLotData.setPortType("");
						maskLotData.setAreaName("");
						maskLotData.setCarrierName(carrierName);
						maskLotData.setPosition(maskLot.getChildText("POSITION"));
						maskLotData.setLastEventUser(eventInfo.getEventUser());
						maskLotData.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
						maskLotData.setLastEventName(eventInfo.getEventName()); // TODO: Check it is necessary to move to ConstantMap.
						maskLotData.setLastEventComment(eventInfo.getEventComment()); // TODO: Check it is necessary to move to ConstantMap.
						maskLotData.setLastEventTime(TimeStampUtil.getCurrentTimestamp());

						maskLotData = ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotData);
						
						ExtendedObjectProxy.getMaskLotService().maskMultiHold(eventInfo, maskLotData);
					}
				}
			}   
			else if (StringUtil.equals(portType, "PU"))
			{
				if (maskLotList != null && maskLotList.size() > 0)
				{
					for (Element maskElement : maskLotList)
					{
						String maskLotName = maskElement.getChildText("MASKNAME");

						MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().getMaskLotData(maskLotName);
						maskLotData = ExtendedObjectProxy.getMaskLotService().TrackOutMaskLot(eventInfo, maskLotData, this.makeMaskElement(doc, maskElement), skipFlag);

						// execute future action and auto flow change
					    maskLotData = ExtendedObjectProxy.getMaskLotService().executePostAction(eventInfo, maskLotData);
						dataInfoList.add(maskLotData);
					}
					
					log.info("LoggedOut Mask Lot Size : " + dataInfoList.size());

					if (durableData.getCapacity() < dataInfoList.size())
					{
						throw new CustomException("MASKINSPECTION-0002", carrierName);
					}
					
					// change durable info
					this.changeCarrierInfo(eventInfo, durableData, dataInfoList, machineName, portName);
				}	
				//2021-03-03 CVD Online test Empty CST End 'NG', Remove this validation to xiaoxh
//				else
//				{
//					throw new CustomException("CARRIER-9003", carrierName);
//				}

			}
			else if (StringUtil.equals(portType, "PB"))
			{
				for (Element maskElement : maskLotList)
				{
					String maskLotName = maskElement.getChildText("MASKNAME");
					String processingInfo = maskElement.getChildText("PROCESSINGINFO");
					String position = maskElement.getChildText("POSITION");

					// check maskLot
					MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().getMaskLotData(maskLotName);

					if (processingInfo.equals("B") )  
					{
						// update processingInfo
						maskLotData.setProcessIngInfo(processingInfo);
						ExtendedObjectProxy.getMaskLotService().update(maskLotData);
						
						this.cancelTrackIn(maskLotName, carrierName, position);
						
						// hold mask lot
						EventInfo holdEventInfo = EventInfoUtil.makeEventInfo("HoldMask", this.getEventUser(), this.getEventComment(), "SYSTEM", "CancelTrackInMask");
						MaskLot HoldMaskLot = ExtendedObjectProxy.getMaskLotService().maskMultiHold(holdEventInfo, maskLotName);

						dataInfoList.add(HoldMaskLot);
					}
					else   
					{
						maskLotData = ExtendedObjectProxy.getMaskLotService().TrackOutMaskLot(eventInfo, maskLotData, this.makeMaskElement(doc, maskElement), skipFlag);

						// execute future action and auto flow change
						maskLotData = ExtendedObjectProxy.getMaskLotService().executePostAction(eventInfo, maskLotData);
						dataInfoList.add(maskLotData);
					}
				}

				if (dataInfoList != null && dataInfoList.size() > 0)
				{
					if (durableData.getCapacity() < dataInfoList.size())
					{
						throw new CustomException("MASKINSPECTION-0002", carrierName);
					}

					// change durable info
					this.changeCarrierInfo(eventInfo, durableData, dataInfoList, machineName, portName);
				}
				else
				{
					log.info("PB Port not exist trackOut MaskLot");
				}
			}

			//check mask flow
			if (this.checkFlowOper(dataInfoList) == false)
			{
				log.info("ProcessFlow or ProcessOperation is not matched in Carrier");
				
				EventInfo holdEventInfo = EventInfoUtil.makeEventInfo("HoldMask", eventInfo.getEventUser(), this.getEventComment(), "SYSTEM", "DifferentFlow");
				for (MaskLot dataInfo : dataInfoList)
				{
					ExtendedObjectProxy.getMaskLotService().maskMultiHold(holdEventInfo, dataInfo);
				}
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
		
		returnBodyElement.addContent(new Element("RESULT").setText(result));
		returnBodyElement.addContent(new Element("RESULTDESCRIPTION").setText(resultDescription));
		
		doc.getRootElement().getChild(SMessageUtil.Body_Tag).detach();
		doc.getRootElement().addContent(2, returnBodyElement);

		return doc;
	}

    public Element makeMaskElement(Document doc, Element maskElement)
    {
        Element bodyElement = doc.getRootElement().getChild(SMessageUtil.Body_Tag);
        
        maskElement.addContent(new Element("MACHINENAME").setText(bodyElement.getChildText("MACHINENAME")));
        maskElement.addContent(new Element("UNITNAME").setText(bodyElement.getChildText("UNITNAME")));
        maskElement.addContent(new Element("CARRIERNAME").setText(bodyElement.getChildText("CARRIERNAME")));
        maskElement.addContent(new Element("PORTTYPE").setText(bodyElement.getChildText("PORTTYPE")));
        maskElement.addContent(new Element("PORTUSETYPE").setText(bodyElement.getChildText("PORTUSETYPE")));
       
        return maskElement;
    }
    
    public void changeCarrierInfo(EventInfo eventInfo ,Durable durableData,List<MaskLot> maskList,String machineName,String portName)
    {
		for (MaskLot maskLotData : maskList)
		{
			if (StringUtil.equals(maskLotData.getCleanState(), GenericServiceProxy.getConstantMap().Dur_Dirty))
			{
				durableData.setDurableCleanState(GenericServiceProxy.getConstantMap().Dur_Dirty);
				break;
			}
		}

		durableData.setLotQuantity(maskList.size());
		durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
		DurableServiceProxy.getDurableService().update(durableData);
		
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("MACHINENAME", machineName);
		setEventInfo.getUdfs().put("PORTNAME", portName);
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
		log.info("SetEvent DurableData");
    }
    
	public void cancelTrackIn(String maskLotName, String carrierName, String position) throws CustomException
	{
		log.info("Cancel TrackIn");
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelTrackInMask", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());

		// Cancel Track In Mask Lot
		ExtendedObjectProxy.getMaskLotService().maskCancelTrackIn(eventInfo, maskLotName, carrierName, position);
	}

	private boolean checkFlowOper(List<MaskLot> dataInfoList) throws CustomException
	{
		if (dataInfoList != null && dataInfoList.size() > 0)
		{
			String processFlow = StringUtil.EMPTY;
			String processFlowVer = StringUtil.EMPTY;
			String processOper = StringUtil.EMPTY;
			String processOperVer = StringUtil.EMPTY;
			for (MaskLot maskData : dataInfoList)
			{
				if (StringUtil.isEmpty(processFlow))
				{
					processFlow = maskData.getMaskProcessFlowName();
					processFlowVer = maskData.getMaskProcessFlowVersion();
					processOper = maskData.getMaskProcessOperationName();
					processOperVer = maskData.getMaskProcessOperationVersion();
				}
				else
				{
					if (!processFlow.equals(maskData.getMaskProcessFlowName()) || !processFlowVer.equals(maskData.getMaskProcessFlowVersion())
							|| !processOper.equals(maskData.getMaskProcessOperationName()) || !processOperVer.equals(maskData.getMaskProcessOperationVersion()))
					{
						return false;
					}
				}
			}
		}
		return true;
	}

	private void holdMask(List<MaskLot> dataInfoList) throws CustomException
	{
		if (dataInfoList != null && dataInfoList.size() > 0)
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("HoldMask", this.getEventUser(), this.getEventComment(), "", "");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());
			eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());

			for (MaskLot maskData : dataInfoList)
			{
				MaskLot dataInfo = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskData.getMaskLotName() });
				dataInfo.setMaskLotName(maskData.getMaskLotName());
				dataInfo.setMaskLotHoldState(GenericServiceProxy.getConstantMap().MaskLotHoldState_OnHold);
				dataInfo.setReasonCode("HOLD");
				dataInfo.setReasonCodeType("SYSTEM");
				dataInfo.setLastEventComment(eventInfo.getEventComment());
				dataInfo.setLastEventName(eventInfo.getEventName());
				dataInfo.setLastEventTime(eventInfo.getEventTime());
				dataInfo.setLastEventTimeKey(eventInfo.getLastEventTimekey());
				dataInfo.setLastEventUser(eventInfo.getEventUser());

				ExtendedObjectProxy.getMaskLotService().modify(eventInfo, dataInfo);
			}
		}
	}

	private MaskLot holdMask(MaskLot dataInfo) throws CustomException
	{
		log.info("HoldAction");
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("HoldMask", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setLastEventTimekey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());

		dataInfo.setMaskLotName(dataInfo.getMaskLotName());
		dataInfo.setMaskLotHoldState(GenericServiceProxy.getConstantMap().MaskLotHoldState_OnHold);
		dataInfo.setReasonCode("HOLD");
		dataInfo.setReasonCodeType("SYSTEM");
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventTimeKey(eventInfo.getLastEventTimekey());
		dataInfo.setLastEventUser(eventInfo.getEventUser());

		return  ExtendedObjectProxy.getMaskLotService().modify(eventInfo, dataInfo);
	}
}
