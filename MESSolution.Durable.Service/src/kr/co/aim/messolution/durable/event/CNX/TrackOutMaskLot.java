package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.durable.event.OledMask.MaskOffsetChangeReport;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.CheckOffset;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.SampleMask;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.NodeKey;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processflow.management.iter.PFIValueSetter;
import kr.co.aim.greentrack.processflow.management.iter.ProcessFlowIterator;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class TrackOutMaskLot extends SyncHandler {
	private static Log logger = LogFactory.getLog(TrackOutMaskLot.class);

	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		String skipFlag = null;
		String maskLotName = SMessageUtil.getBodyItemValue(doc, "MASKLOTNAME", true);
		String maskSpecName = SMessageUtil.getBodyItemValue(doc, "MASKSPECNAME", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "MASKPROCESSFLOWNAME", true);
		String AOICode = SMessageUtil.getBodyItemValue(doc, "AOICODE", false);
		String repairCode = SMessageUtil.getBodyItemValue(doc, "REPAIRCODE", false);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "MASKPROCESSOPERATIONNAME", true);
		String maskLotJudge = SMessageUtil.getBodyItemValue(doc, "MASKLOTJUDGE", false);
		
		String nextProcessFlowName = "";
		String nextProcessFlowVersion = "";
		String nextOperationName = "";
		String nextOperationVersion = "";
		String nextNodeStack = "";
		Boolean countHoldFlag = false;
		Boolean codeHoldFlag = false;
		
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOutMask", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });

		if (!StringUtil.equals(maskLotData.getMaskLotProcessState(), constantMap.MaskLotProcessState_Run))
			throw new CustomException("OLEDMASK-0002", maskLotName);
		
		if(StringUtil.isNotEmpty(maskLotData.getCarrierName()))
			throw new CustomException("MASK-1000");

		ProcessOperationSpec processOperationData = CommonUtil.getProcessOperationSpec(maskLotData.getFactoryName(), maskLotData.getMaskProcessOperationName(),
				maskLotData.getMaskProcessOperationVersion());
		
		maskLotData.setReasonCode("");
		maskLotData.setReasonCodeType("");
		maskLotData.setMaskLotProcessState("WAIT");
			
		maskLotData.setMachineName(machineName);
		maskLotData.setPortName(portName);
		maskLotData.setPortType(portType);
		maskLotData.setLastEventComment(eventInfo.getEventComment());
		maskLotData.setLastEventName(eventInfo.getEventName());
		maskLotData.setLastEventTime(eventInfo.getEventTime());
		maskLotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		maskLotData.setLastEventUser(eventInfo.getEventUser());
		maskLotData.setLastLoggedOutTime(eventInfo.getEventTime());
		maskLotData.setLastLoggedOutUser(eventInfo.getEventUser());
		
		int aoiCount = maskLotData.getAoiCount() == null ? 0 : maskLotData.getAoiCount().intValue();
		int repairCount = maskLotData.getMaskRepairCount() == null ? 0 : maskLotData.getMaskRepairCount().intValue();

		if(StringUtil.equalsIgnoreCase(processOperationData.getDetailProcessOperationType(), "MASKAOI"))
		{
			maskLotData.setAoiCount(aoiCount+1);
			maskLotData.setMaskLotJudge(maskLotJudge);
			maskLotData.setAoiCode(AOICode);
			if(maskLotJudge.equals("N")&&AOICode.equals("1"))
			{
				countHoldFlag = true;
			}
		}
		if(StringUtil.equalsIgnoreCase(processOperationData.getDetailProcessOperationType(), "MASKREPAIR"))
		{
			maskLotData.setMaskRepairCount(repairCount+1);
			maskLotData.setMaskLotJudge(maskLotJudge);
			maskLotData.setRepairCode(repairCode);
			if((maskLotJudge.equals("N")&&(repairCount+1)>=2)||(maskLotJudge.equals("G")&&repairCount>=2)||
					(maskLotJudge.equals("P")&&(repairCount+1)>=2)||(maskLotJudge.equals("N")&&repairCode.equals("1")))
			{
				countHoldFlag = true;
			}
		}
		else if(StringUtil.equalsIgnoreCase(processOperationData.getDetailProcessOperationType(), "MASKPPA"))
		{
			maskLotData.setMaskLotJudge(maskLotJudge);
			if(maskLotJudge.equals("N"))
			{
				codeHoldFlag=true;
			}
		}			
		else if(StringUtil.equalsIgnoreCase(processOperationData.getDetailProcessOperationType(), "MASKMACRO"))
		{
			maskLotData.setMaskLotJudge(maskLotJudge);
			if(maskLotJudge.equals("N"))
			{
				codeHoldFlag=true;
			}
		}
			


		// get next operation
		Node nextNode = ExtendedObjectProxy.getExtendedProcessFlowUtil().getNextNode(maskLotData);
		NodeKey nodekey = nextNode.getKey();
		nextNodeStack = nodekey.getNodeId();
		nextProcessFlowName = nextNode.getProcessFlowName();
		nextProcessFlowVersion = nextNode.getProcessFlowVersion();
		nextOperationName = nextNode.getNodeAttribute1();
		nextOperationVersion = nextNode.getNodeAttribute2();
		

		// If current flow is Sampling Flow
		ProcessFlow processFlowData = ExtendedObjectProxy.getMaskLotService().getProcessFlowData(maskLotData);
		if (StringUtil.equals(processFlowData.getProcessFlowType(), "Sample"))
		{
			SampleMask sampleMask = ExtendedObjectProxy.getSampleMaskService().getSampleMask(true, maskLotName, factoryName, maskSpecName, maskLotData.getMaskProcessFlowName(),
					maskLotData.getMaskProcessFlowVersion(), maskLotData.getMaskProcessOperationName(), maskLotData.getMaskProcessOperationVersion());

			if (sampleMask == null)
				throw new CustomException("MASK-0044", maskLotName, processOperationName);

			// If current operation is last operation of Sampling Flow
			if (StringUtil.isEmpty(nextOperationName) && StringUtil.equals(nextNode.getNodeType(), GenericServiceProxy.getConstantMap().Node_End))
			{
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
				SampleMask nextSampleMask = ExtendedObjectProxy.getSampleMaskService().getSampleMask(true, maskLotName, factoryName, maskSpecName, nextProcessFlowName,
						nextProcessFlowVersion, nextOperationName, nextOperationVersion);

				if (nextSampleMask != null)
					ExtendedObjectProxy.getSampleMaskService().remove(eventInfo, nextSampleMask);

				SampleMask newSampleMask = new SampleMask(maskLotName, factoryName, maskSpecName, nextProcessFlowName, nextProcessFlowVersion, nextOperationName, nextOperationVersion,
						sampleMask.getReturnProcessFlowName(), sampleMask.getReturnProcessFlowVersion(), sampleMask.getReturnOperationName(), sampleMask.getReturnOperationVersion(),
						eventInfo.getEventName(), eventInfo.getLastEventTimekey(), eventInfo.getEventTime(), eventInfo.getEventUser(), eventInfo.getEventComment());

				ExtendedObjectProxy.getSampleMaskService().remove(eventInfo, sampleMask);
				ExtendedObjectProxy.getSampleMaskService().create(eventInfo, newSampleMask);
			}
		}

		// If current operation is 'Tension' or 'EVA' operation
		if (StringUtil.equalsIgnoreCase(processOperationData.getDetailProcessOperationType(), "TENSION")
				|| StringUtil.equalsIgnoreCase(processOperationData.getDetailProcessOperationType(), "EVA"))
		{
			if(StringUtil.equalsIgnoreCase(processOperationData.getDetailProcessOperationType(), "EVA"))
			{
				maskLotData.setMaskCycleCount((maskLotData.getMaskCycleCount()==null?0:maskLotData.getMaskCycleCount().intValue()+1));
			}
			maskLotData.setCleanState("Dirty");
			maskLotData.setCleanFlag("N");
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
			maskLotData.setAoiCount(0);
			maskLotData.setMaskRepairCount(0);
		}
		
		//add by cjl for clear AOICount and RepairCount 20201015
		if(StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_MaskOrgCleaner) || 
				StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_MaskMetalCleaner))
		{
			maskLotData.setAoiCount(0);
			maskLotData.setMaskRepairCount(0);				
		}

		// Skip the mask inspection operation according to sample policy.
		if (skipFlag == null)
			skipFlag = ExtendedObjectProxy.getMaskLotService().getSkipFlagForSampling(maskLotData);

		Map<String, Object> samplePolicy = ExtendedObjectProxy.getMaskLotService().getMaskSamplePolicyByMask(maskLotData);
		if (StringUtil.equals(skipFlag, "Y") && samplePolicy != null)
		{
			eventInfo.setEventComment("Skipped Operation Name: [" + (String) samplePolicy.get("TOPROCESSOPERATIONNAME") + "]");
			String newProcessFlowName = (String) samplePolicy.get("TOPROCESSFLOWNAME");
			String newProcessFlowVersion = (String) samplePolicy.get("TOPROCESSFLOWVERSION");

			if (!StringUtil.equals(maskLotData.getMaskProcessFlowName(), newProcessFlowName) || !StringUtil.equals(maskLotData.getMaskProcessFlowVersion(), newProcessFlowVersion))
				throw new CustomException("SAMPLE-0001");

			String newProcessOperationName = (String) samplePolicy.get("RETURNOPERATIONNAME");
			String newProcessOperationVersion = (String) samplePolicy.get("RETURNOPERATIONVERSION");

			String newNodeStack = CommonUtil.getNodeStack(maskLotData.getFactoryName(), newProcessFlowName, newProcessFlowVersion, newProcessOperationName, newProcessOperationVersion);

			nextNodeStack = newNodeStack;
			nextOperationName = newProcessOperationName;
			nextOperationVersion = newProcessOperationVersion;
		}

		// If this operation is last operation of the Rework Flow
		if (StringUtil.equals(nextNode.getNodeType(), constantMap.Node_End) && StringUtil.equals(maskLotData.getReworkState(), constantMap.Lot_InRework))
		{
			String returnNodeStack = maskLotData.getReturnSequenceId();
			Node returnNodeData = ProcessFlowServiceProxy.getProcessFlowService().getNode(returnNodeStack);

			maskLotData.setNodeStack(returnNodeStack);
			maskLotData.setMaskProcessFlowName(returnNodeData.getProcessFlowName());
			maskLotData.setMaskProcessFlowVersion(returnNodeData.getProcessFlowVersion());
			maskLotData.setMaskProcessOperationName(returnNodeData.getNodeAttribute1());
			maskLotData.setMaskProcessOperationVersion(returnNodeData.getNodeAttribute2());
			maskLotData.setReturnSequenceId("");
			maskLotData.setReworkState(constantMap.Lot_NotInRework);
			
		}
		else
		{
			ProcessOperationSpec nextProcessOperationData=CommonUtil.getProcessOperationSpec(maskLotData.getFactoryName(), nextOperationName,
					nextOperationVersion);
			
			if(nextProcessOperationData.getDetailProcessOperationType().equals("MASKSTOCK"))
			{
				maskLotData.setAoiCode("");
				maskLotData.setRepairCode("");
			}

			// set Next Operation
			maskLotData.setNodeStack(nextNodeStack);
			maskLotData.setMaskProcessFlowName(nextProcessFlowName);
			maskLotData.setMaskProcessFlowVersion(nextProcessFlowVersion);
			maskLotData.setMaskProcessOperationName(nextOperationName);
			maskLotData.setMaskProcessOperationVersion(nextOperationVersion);
		}
		
		ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotData);
		
		if(countHoldFlag == true)
		{
			EventInfo holdEventInfo = EventInfoUtil.makeEventInfo("HoldMask", this.getEventUser(), "AOICount or RepairCount >=2", "SYSTEM", "HOLD");
			maskLotData = ExtendedObjectProxy.getMaskLotService().maskMultiHold(holdEventInfo, maskLotData);
		}
		if(codeHoldFlag == true)
		{
			EventInfo holdEventInfo =EventInfoUtil.makeEventInfo("HoldMask", this.getEventUser(), "AOI/REPAIR CODE='1'", "SYSTEM", "HOLD");
			maskLotData=ExtendedObjectProxy.getMaskLotService().maskMultiHold(holdEventInfo, maskLotData);
		}
		ExtendedObjectProxy.getMaskLotService().executePostAction(eventInfo, maskLotData);

		return doc;
	}

	public static Node getNextOLEDMaskNode(String nodeStack) throws CustomException
	{
		String[] nodeStackArray = StringUtil.split(nodeStack, ".");

		Node nextNode = null;
		boolean isCurrent = true;

		for (int idx = nodeStackArray.length; idx > 0; idx--)
		{
			if (isCurrent)
			{
				try
				{
					// though which is successful, first loop must be descreminated
					isCurrent = false;

					nextNode = PolicyUtil.getNextNode(nodeStackArray[idx - 1]);

					if (StringUtil.isEmpty(nextNode.getNodeAttribute1()) || StringUtil.isEmpty(nextNode.getProcessFlowName()))
						throw new Exception();

					break;
				}
				catch (Exception ex)
				{
					logger.debug("It is last node");
				}
			}
			else
			{
				nextNode = ProcessFlowServiceProxy.getNodeService().getNode(nodeStackArray[idx - 1]);
				break;
			}
		}

		if (nextNode != null)
			return nextNode;
		else
			throw new CustomException("", "");
	}
}
