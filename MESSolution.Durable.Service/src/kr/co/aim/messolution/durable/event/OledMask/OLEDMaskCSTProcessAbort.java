package kr.co.aim.messolution.durable.event.OledMask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.CheckOffset;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.MaskMultiHold;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskRecipe;
import kr.co.aim.messolution.extended.object.management.data.SampleMask;
import kr.co.aim.messolution.extended.object.management.impl.ReserveMaskRecipeService;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.NodeKey;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;

public class OLEDMaskCSTProcessAbort extends AsyncHandler 
{
	private static Log log = LogFactory.getLog(OLEDMaskCSTProcessAbort.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		List<MaskLot> dataInfoList = new ArrayList<MaskLot>();
	    String skipFlag = null;
	    
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		List<Element> maskLotList = SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", false);
		
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

		if (maskLotList == null || maskLotList.size() < 0)
		{
			// MASK-0106: The mask information reported by BC is empty!!
			throw new CustomException("MASK-0106");
		}

		if (StringUtil.equals(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), "PL"))
		{
			for (int i = 0; i < maskLotList.size(); i++)
			{
				String maskLotName = maskLotList.get(i).getChildText("MASKNAME");
				String position = maskLotList.get(i).getChildText("POSITION");

				// check maskLot
				MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().getMaskLotData(maskLotName);
				
				// update processingInfo
				maskLotData.setProcessIngInfo(maskLotList.get(i).getChildText("PROCESSINGINFO"));
				ExtendedObjectProxy.getMaskLotService().update(maskLotData);
				
				this.cancelTrackIn(maskLotName, carrierName, position);
				
				EventInfo holdEventInfo = EventInfoUtil.makeEventInfo("HoldMask", this.getEventUser(), this.getEventComment(), "SYSTEM", "CancelTrackInMask");
				ExtendedObjectProxy.getMaskLotService().maskMultiHold(holdEventInfo, maskLotName);
			} 
		}
		else if (StringUtil.equals(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), "PU"))
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOutMask", getEventUser(), getEventComment());

			for (Element maskElement : maskLotList)
			{
				String maskLotName = maskElement.getChildText("MASKNAME");

				MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().getMaskLotData(maskLotName);
				maskLotData = ExtendedObjectProxy.getMaskLotService().TrackOutMaskLot(eventInfo, maskLotData, this.makeMaskElement(doc, maskElement), skipFlag);

				maskLotData = ExtendedObjectProxy.getMaskLotService().executePostAction(eventInfo, maskLotData);
				dataInfoList.add(maskLotData);
			}

			log.info("LoggedOut Mask Lot Size : " + dataInfoList.size());
		}
		else
		{
			// PB Case
			for (Element maskElement : maskLotList)
			{
				String maskLotName = maskElement.getChildText("MASKNAME");
				String processingInfo = maskElement.getChildText("PROCESSINGINFO");
				String position = maskElement.getChildText("POSITION");

				// check maskLot
				MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().getMaskLotData(maskLotName);

				if (StringUtil.equals(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), "PB") && processingInfo.equals("B"))
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
					EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOutMask", getEventUser(), getEventComment());
					maskLotData = ExtendedObjectProxy.getMaskLotService().TrackOutMaskLot(eventInfo, maskLotData, this.makeMaskElement(doc, maskElement), skipFlag);
					
					maskLotData = ExtendedObjectProxy.getMaskLotService().executePostAction(eventInfo, maskLotData);
					dataInfoList.add(maskLotData);
				}
			}
		}

		if (dataInfoList != null && dataInfoList.size() > 0)
		{
			if (durableData.getCapacity() < dataInfoList.size())
			{
				throw new CustomException("MASKINSPECTION-0002", carrierName);
			}

			// change durable info
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOutMask", getEventUser(), getEventComment());
			this.changeCarrierInfo(eventInfo, durableData, dataInfoList, machineName, portName);
		}
		else
		{
			log.info("PB Port not exist trackOut MaskLot");
		}
		
		//check mask flow
		if (this.checkFlowOper(dataInfoList) == false)
		{
			log.info("ProcessFlow or ProcessOperation is not matched in Carrier");
			
			EventInfo holdEventInfo = EventInfoUtil.makeEventInfo("HoldMask", this.getEventUser(), this.getEventComment(), "SYSTEM", "DifferentFlow");
			for (MaskLot dataInfo : dataInfoList)
			{
				ExtendedObjectProxy.getMaskLotService().maskMultiHold(holdEventInfo, dataInfo);
			}
		}
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

	public void changeCarrierInfo(EventInfo eventInfo, Durable durableData, List<MaskLot> maskList, String machineName, String portName)
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

		return ExtendedObjectProxy.getMaskLotService().modify(eventInfo, dataInfo);
	}

	public void cancelTrackIn(String maskLotName, String carrierName, String position) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelTrackInMask", getEventUser(), getEventComment(), "", "");

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

	private void MultiHoldMaskLot(String maskLotName, MaskLot maskLotData, EventInfo eventInfo) throws CustomException
	{
		MaskMultiHold maskMultiHold = new MaskMultiHold();
		maskMultiHold.setMaskLotName(maskLotName);
		maskMultiHold.setFactoryName(maskLotData.getFactoryName());
		maskMultiHold.setMaskProcessOperationName(maskLotData.getMaskProcessOperationName());
		maskMultiHold.setMaskProcessOperationVersion(maskLotData.getMaskProcessOperationVersion());
		maskMultiHold.setReasonCode(eventInfo.getReasonCode());
		maskMultiHold.setReasonCodeType(eventInfo.getReasonCodeType());
		maskMultiHold.setLastEventComment(eventInfo.getEventComment());
		maskMultiHold.setLastEventName(eventInfo.getEventName());
		maskMultiHold.setLastEventTime(eventInfo.getEventTime());
		maskMultiHold.setLastEventUser(eventInfo.getEventUser());

		ExtendedObjectProxy.getMaskMultiHoldService().create(eventInfo, maskMultiHold);
	}
}
