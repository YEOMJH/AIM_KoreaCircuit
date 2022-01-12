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
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.NodeKey;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;

public class OLEDMaskCSTProcessAborted extends AsyncHandler {

	private static Log log = LogFactory.getLog(OLEDMaskCSTProcessAborted.class);
	List<MaskLot> dataInfoList = new ArrayList<MaskLot>();
	private String skipFlag;

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		skipFlag = null;
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		List<Element> maskLotList = SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", false);

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

		dataInfoList.clear();

		if (maskLotList == null || maskLotList.size() < 0)
			throw new CustomException("CARRIER-9003", carrierName);

		if (StringUtil.equals(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), "PL"))
		{
			for (int i = 0; i < maskLotList.size(); i++)
			{
				String maskLotName = maskLotList.get(i).getChildText("MASKNAME");
				// check maskLot
				MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });

				String position = maskLotList.get(i).getChildText("POSITION");
				this.cancelTrackIn(maskLotName, carrierName, position);
			}
		}
		else if (StringUtil.equals(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), "PU"))
		{

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOutMask", getEventUser(), getEventComment(), "", "");
			String processingInfo = "";

			for (int i = 0; i < maskLotList.size(); i++)
			{
				String maskLotName = maskLotList.get(i).getChildText("MASKNAME");
				String position = maskLotList.get(i).getChildText("POSITION");

				this.setMaskDataList(doc, eventInfo, maskLotName, carrierName, machineName, portName, CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), maskLotList, i);
			}
			ExtendedObjectProxy.getMaskLotService().modify(eventInfo, dataInfoList);

			int lotQty = ExtendedObjectProxy.getMaskLotService().assignedMaskLotQtyByCarrier(carrierName);

			// Assign Carrier
			if (durableData.getCapacity() < lotQty)
			{
				// error
				throw new CustomException("MASKINSPECTION-0002", carrierName);
			}
			durableData.setLotQuantity(lotQty);
			durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);

			DurableServiceProxy.getDurableService().update(durableData);

			SetEventInfo setEventInfo1 = new SetEventInfo();
			setEventInfo1.getUdfs().put("MACHINENAME", machineName);
			setEventInfo1.getUdfs().put("PORTNAME", portName);
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo1, eventInfo);

			for (int i = 0; i < maskLotList.size(); i++)
			{
				String maskLotName = maskLotList.get(i).getChildText("MASKNAME");
				processingInfo = maskLotList.get(i).getChildText("PROCESSINGINFO");

				if (processingInfo.equals("L") || processingInfo.equals("F"))
				{
					this.holdMask(maskLotName);
				}
			}

		}
		else
		// PB Case
		{
			for (int i = 0; i < maskLotList.size(); i++)
			{
				String maskLotName = maskLotList.get(i).getChildText("MASKNAME");
				String processingInfo = maskLotList.get(i).getChildText("PROCESSINGINFO");
				String position = maskLotList.get(i).getChildText("POSITION");
				// check maskLot
				MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });

				if (StringUtil.equals(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), "PB") && (processingInfo.equals("B") || processingInfo.equals("F")))
				{
					this.cancelTrackIn(maskLotName, carrierName, position);
				}
				else
				{
					EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOutMask", getEventUser(), getEventComment(), "", "");

					this.setMaskDataList(doc, eventInfo, maskLotName, carrierName, machineName, portName, CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), maskLotList, i);

					ExtendedObjectProxy.getMaskLotService().modify(eventInfo, dataInfoList);

					if (dataInfoList != null && dataInfoList.size() > 0)
					{
						int lotQty = ExtendedObjectProxy.getMaskLotService().assignedMaskLotQtyByCarrier(carrierName);

						// Assign Carrier
						if (durableData.getCapacity() < lotQty)
						{
							// error
							throw new CustomException("MASKINSPECTION-0002", carrierName);
						}
						durableData.setLotQuantity(lotQty);
						durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);

						DurableServiceProxy.getDurableService().update(durableData);

						SetEventInfo setEventInfo1 = new SetEventInfo();
						setEventInfo1.getUdfs().put("MACHINENAME", machineName);
						setEventInfo1.getUdfs().put("PORTNAME", portName);
						MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo1, eventInfo);
					}
					else
						log.info("PB Port not exist trackOut MaskLot");
				}
			}
		}

		if (this.checkFlowOper() == false)
		{
			this.holdMask();
		}

	}

	public void cancelTrackIn(String maskLotName, String carrierName, String position) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelTrackInMask", getEventUser(), getEventComment(), "", "");

		// Cancel Track In Mask Lot
		ExtendedObjectProxy.getMaskLotService().maskCancelTrackIn(eventInfo, maskLotName, carrierName, position);
	}

	public void setMaskDataList(Document doc, EventInfo eventInfo, String maskLotName, String carrierName, String machineName, String portName, String portType, List<Element> maskLotList, int i)
			throws CustomException
	{
		Float magnet = 0.0f;
		MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });

		ProcessOperationSpec processOperationData = CommonUtil.getProcessOperationSpec(maskLotData.getFactoryName(), maskLotData.getMaskProcessOperationName(),
				maskLotData.getMaskProcessOperationVersion());
		ProcessFlow processFlowData = ExtendedObjectProxy.getMaskLotService().getProcessFlowData(maskLotData);

		if (!maskLotData.getMaskLotProcessState().equals("RUN"))
		{
			throw new CustomException("MASK-0023", maskLotName);
		}

		maskLotData.setCarrierName(carrierName);
		maskLotData.setPosition(maskLotList.get(i).getChildText("POSITION"));
		maskLotData.setReasonCode("");
		maskLotData.setReasonCodeType("");
		maskLotData.setMaskLotProcessState("WAIT");
		maskLotData.setMachineName(machineName);
		maskLotData.setPortName(portName);
		maskLotData.setPortType(portType);
		maskLotData.setJobDownFlag("");
		maskLotData.setMaskLotJudge(maskLotList.get(i).getChildText("MASKJUDGE"));
		if (processFlowData.getUdfs().get("DETAILPROCESSFLOWTYPE").equals("Eva") && processOperationData.getProcessOperationType().equals(GenericServiceProxy.getConstantMap().Mac_InspectUnit))
		{
			maskLotData.setEvaOffSetTheta(maskLotList.get(i).getChildText("INSPECTION_OFFSET_THETA"));
			maskLotData.setEvaOffSetX(maskLotList.get(i).getChildText("INSPECTION_OFFSET_X"));
			maskLotData.setEvaOffSetY(maskLotList.get(i).getChildText("INSPECTION_OFFSET_Y"));
		}
		if (processFlowData.getUdfs().get("DETAILPROCESSFLOWTYPE").equals("Creation") && processOperationData.getProcessOperationType().equals(GenericServiceProxy.getConstantMap().Mac_InspectUnit))
		{
			maskLotData.setInitialOffSetTheta(maskLotList.get(i).getChildText("INSPECTION_OFFSET_THETA"));
			maskLotData.setInitialOffSetX(maskLotList.get(i).getChildText("INSPECTION_OFFSET_X"));
			maskLotData.setInitialOffSetY(maskLotList.get(i).getChildText("INSPECTION_OFFSET_Y"));
		}
		if (processFlowData.getUdfs().get("DETAILPROCESSFLOWTYPE").equals("Tension") && processOperationData.getProcessOperationType().equals(GenericServiceProxy.getConstantMap().Mac_InspectUnit))
		{
			maskLotData.setTensionJudge(maskLotList.get(i).getChildText("MASKJUDGE"));
		}

		if (StringUtils.isEmpty(maskLotList.get(i).getChildText("MAGNET")))
		{
			magnet = maskLotData.getMagnet();
		}
		else
		{
			try
			{
				magnet = Float.valueOf(maskLotList.get(i).getChildText("MAGNET"));
			}
			catch (Exception e)
			{
				magnet = maskLotData.getMagnet();
			}
		}

		maskLotData.setMagnet(magnet);
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		if (maskLotList.get(i).getChildText("MASKTHICKNESS").isEmpty())
		{
			maskLotData.setMaskThickness(maskLotData.getMaskThickness());
		}
		else
		{
			if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_MaskPPA) || StringUtils.equals(machineData.getMachineGroupName(), "TENSION")
					|| (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_EVA) && StringUtils.equals(maskLotData.getMaskKind(), "EVA")))
			{
				maskLotData.setMaskThickness(String.valueOf(Double.valueOf(maskLotList.get(i).getChildText("MASKTHICKNESS")) / 10000));
			}
			else if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_EVA) && StringUtils.equals(maskLotData.getMaskKind(), "TFE"))
			{
				maskLotData.setMaskThickness(maskLotList.get(i).getChildText("MASKTHICKNESS"));
			}
			else
			{
				maskLotData.setMaskThickness(maskLotList.get(i).getChildText("MASKTHICKNESS"));
			}
		}

		maskLotData.setLastEventComment(eventInfo.getEventComment());
		maskLotData.setLastEventName(eventInfo.getEventName());
		maskLotData.setLastEventTime(eventInfo.getEventTime());
		maskLotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		maskLotData.setLastEventUser(eventInfo.getEventUser());
		maskLotData.setLastLoggedOutTime(eventInfo.getEventTime());
		maskLotData.setLastLoggedOutUser(eventInfo.getEventUser());

		if (StringUtil.equals(maskLotData.getMaskType(), "FMM") && processOperationData.getProcessOperationType().equals("Production"))
		{
			maskLotData.setCleanState("Dirty");
			maskLotData.setCleanFlag("N");
		}

		// If the mask recipe was reserved, add history mask recipe (in CT_ReserveMaskRecipeHist).
		ReserveMaskRecipe reservedRecipe = null;
		try
		{
			ReserveMaskRecipeService reserveMaskRecipeService = ExtendedObjectProxy.getReserveMaskRecipeService();
			Object[] keySet = new Object[] { maskLotName, maskLotData.getMaskSpecName(), maskLotData.getMaskProcessFlowName(), maskLotData.getMaskProcessFlowVersion(),
					maskLotData.getMaskProcessOperationName(), maskLotData.getMaskProcessOperationVersion() ,machineName};
			reservedRecipe = reserveMaskRecipeService.selectByKey(true, keySet);

			if (reservedRecipe != null)
			{
				eventInfo.setEventName("UseReserveMaskRecipe");
				reserveMaskRecipeService.addHistory(eventInfo, reserveMaskRecipeService.getHistoryEntity(), reservedRecipe, ReserveMaskRecipeService.getLogger());
			}
		}
		catch (greenFrameDBErrorSignal nfds)
		{
		}

		if (StringUtil.equalsIgnoreCase(processOperationData.getDetailProcessOperationType(), "CLN"))
		{
			maskLotData.setCleanState("Clean");
			maskLotData.setCleanFlag("Y");
			maskLotData.setCleanStartTimekey(maskLotData.getCleanStartTimekey());
			maskLotData.setCleanEndTimekey(eventInfo.getEventTimeKey());
			maskLotData.setMaskCleanCount(maskLotData.getMaskCleanCount().intValue() + 1);
			maskLotData.setCleanTime(eventInfo.getEventTime());
			maskLotData.setLastCleanTimekey(eventInfo.getLastEventTimekey());
			maskLotData.setTimeUsed(0f);
		}

		// get next operation
		log.info("Get NextNode");
		Node nextNode = PolicyUtil.getNextOLEDMaskOperation(maskLotData);

		NodeKey nodekey = nextNode.getKey();
		String nextNodeStack = nodekey.getNodeId();
		String nextProcessFlowName = nextNode.getProcessFlowName();
		String nextProcessFlowVersion = nextNode.getProcessFlowVersion();
		String nextOperationName = nextNode.getNodeAttribute1();
		String nextOperationVersion = nextNode.getNodeAttribute2();

		if (StringUtil.equalsIgnoreCase(processOperationData.getDetailProcessOperationType(), "TENSION") || StringUtil.equalsIgnoreCase(processOperationData.getDetailProcessOperationType(), "EVA"))
		{
			maskLotData.setCleanState("Dirty");
			maskLotData.setCleanFlag("N");

		}

		// If current flow is Sampling Flow
		log.info("ProcessFlowType : " + processFlowData.getProcessFlowType());
		if (StringUtil.equals(processFlowData.getProcessFlowType(), "Sample"))
		{
			SampleMask sampleMask = ExtendedObjectProxy.getSampleMaskService().getSampleMask(true, maskLotName, maskLotData.getFactoryName(), maskLotData.getMaskSpecName(),
					maskLotData.getMaskProcessFlowName(), maskLotData.getMaskProcessFlowVersion(), maskLotData.getMaskProcessOperationName(), maskLotData.getMaskProcessOperationVersion());

			if (sampleMask == null)
			{
				throw new CustomException("OLEDMASK-0011", maskLotName, maskLotData.getMaskProcessOperationName());
			}

			// If current operation is last operation of Sampling Flow
			if (StringUtil.isEmpty(nextOperationName) && StringUtil.equals(nextNode.getNodeType(), GenericServiceProxy.getConstantMap().Node_End))
			{
				log.info("If current operation is last operation of Sampling Flow");
				String newNodeStack = CommonUtil.getNodeStack(maskLotData.getFactoryName(), sampleMask.getReturnProcessFlowName(), sampleMask.getReturnProcessFlowVersion(),
						sampleMask.getReturnOperationName(), sampleMask.getReturnOperationVersion());

				nextNodeStack = newNodeStack;
				nextProcessFlowName = sampleMask.getReturnProcessFlowName();
				nextProcessFlowVersion = sampleMask.getReturnProcessFlowVersion();
				nextOperationName = sampleMask.getReturnOperationName();
				nextOperationVersion = sampleMask.getReturnOperationVersion();

				ExtendedObjectProxy.getSampleMaskService().remove(eventInfo, sampleMask);
			}
			// If current operation is not last operation of Sampling Flow
			else
			{
				log.info("If current operation is not last operation of Sampling Flow");
				SampleMask nextSampleMask = ExtendedObjectProxy.getSampleMaskService().getSampleMask(true, maskLotName, maskLotData.getFactoryName(), maskLotData.getMaskSpecName(),
						nextProcessFlowName, nextProcessFlowVersion, nextOperationName, nextOperationVersion);

				if (nextSampleMask != null)
				{
					ExtendedObjectProxy.getSampleMaskService().remove(eventInfo, nextSampleMask);
				}

				SampleMask newSampleMask = new SampleMask(maskLotName, maskLotData.getFactoryName(), maskLotData.getMaskSpecName(), nextProcessFlowName, nextProcessFlowVersion, nextOperationName,
						nextOperationVersion, sampleMask.getReturnProcessFlowName(), sampleMask.getReturnProcessFlowVersion(), sampleMask.getReturnOperationName(),
						sampleMask.getReturnOperationVersion(), eventInfo.getEventName(), eventInfo.getLastEventTimekey(), eventInfo.getEventTime(), eventInfo.getEventUser(),
						eventInfo.getEventComment());

				ExtendedObjectProxy.getSampleMaskService().remove(eventInfo, sampleMask);
				ExtendedObjectProxy.getSampleMaskService().create(eventInfo, newSampleMask);
			}

			maskLotData.setNodeStack(nextNodeStack);
			maskLotData.setMaskProcessFlowName(nextProcessFlowName);
			maskLotData.setMaskProcessFlowVersion(nextProcessFlowVersion);
			maskLotData.setMaskProcessOperationName(nextOperationName); // MASKPROCESSOPERATIONNAME move to next operation
			maskLotData.setMaskProcessOperationVersion(nextOperationVersion);
		}
		// If this operation is last operation of the Rework Flow
		else if (StringUtil.equals(nextNode.getNodeType(), GenericServiceProxy.getConstantMap().Node_End)
				&& StringUtil.equals(maskLotData.getReworkState(), GenericServiceProxy.getConstantMap().Lot_InRework))
		{
			log.info("If this operation is last operation of the Rework Flow");
			String returnNodeStack = maskLotData.getReturnSequenceId();
			Node returnNodeData = ProcessFlowServiceProxy.getProcessFlowService().getNode(returnNodeStack);

			maskLotData.setNodeStack(returnNodeStack);
			maskLotData.setMaskProcessFlowName(returnNodeData.getProcessFlowName());
			maskLotData.setMaskProcessFlowVersion(returnNodeData.getProcessFlowVersion());
			maskLotData.setMaskProcessOperationName(returnNodeData.getNodeAttribute1());
			maskLotData.setMaskProcessOperationVersion(returnNodeData.getNodeAttribute2());
			maskLotData.setReturnSequenceId("");
			maskLotData.setReworkState(GenericServiceProxy.getConstantMap().Lot_NotInRework);
		}
		else
		{
			// Skip the mask inspection operation according to sample policy.
			log.info("Skip the mask inspection operation according to sample policy");
			log.info("Check SkipFlag");
			if (skipFlag == null)
			{
				skipFlag = ExtendedObjectProxy.getMaskLotService().getSkipFlagForSampling(maskLotData);
				log.info("SkipFlag : " + skipFlag);
			}

			Map<String, Object> samplePolicy = ExtendedObjectProxy.getMaskLotService().getMaskSamplePolicyByMask(maskLotData);
			if (StringUtil.equals(skipFlag, "Y") && samplePolicy != null)
			{
				log.info("Skip Operation");
				eventInfo.setEventComment("Skipped Operation Name: [" + (String) samplePolicy.get("TOPROCESSOPERATIONNAME") + "]");
				String newProcessFlowName = (String) samplePolicy.get("TOPROCESSFLOWNAME");
				String newProcessFlowVersion = (String) samplePolicy.get("TOPROCESSFLOWVERSION");
				if (!StringUtil.equals(maskLotData.getMaskProcessFlowName(), newProcessFlowName) || !StringUtil.equals(maskLotData.getMaskProcessFlowVersion(), newProcessFlowVersion))
				{
					throw new CustomException("SAMPLE-0001");
				}

				String newProcessOperationName = (String) samplePolicy.get("RETURNOPERATIONNAME");
				String newProcessOperationVersion = (String) samplePolicy.get("RETURNOPERATIONVERSION");

				String newNodeStack = CommonUtil.getNodeStack(maskLotData.getFactoryName(), newProcessFlowName, newProcessFlowVersion, newProcessOperationName, newProcessOperationVersion);

				nextNodeStack = newNodeStack;
				// nextProcessFlowName = newProcessFlowName;
				// nextProcessFlowVersion = newProcessFlowVersion;
				nextOperationName = newProcessOperationName;
				nextOperationVersion = newProcessOperationVersion;
			}

			maskLotData.setNodeStack(nextNodeStack);
			maskLotData.setMaskProcessFlowName(nextProcessFlowName);
			maskLotData.setMaskProcessFlowVersion(nextProcessFlowVersion);
			maskLotData.setMaskProcessOperationName(nextOperationName); // MASKPROCESSOPERATIONNAME move to next operation
			maskLotData.setMaskProcessOperationVersion(nextOperationVersion);
		}

		dataInfoList.add(maskLotData);
	}

	private boolean checkFlowOper() throws CustomException
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

	private void holdMask() throws CustomException
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

	private void holdMask(String maskLotName) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("HoldMask", this.getEventUser(), this.getEventComment(), "", "");

		eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());

		MaskLot dataInfo = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskLotName });
		dataInfo.setMaskLotName(maskLotName);
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
