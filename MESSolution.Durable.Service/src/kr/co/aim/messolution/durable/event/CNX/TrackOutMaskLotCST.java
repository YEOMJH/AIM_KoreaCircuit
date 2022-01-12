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

public class TrackOutMaskLotCST extends SyncHandler {
	private static Log logger = LogFactory.getLog(TrackOutMaskLotCST.class);

	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		String skipFlag = null;
		List<Element> maskList = SMessageUtil.getBodySequenceItemList(doc, "MASKLOTLIST", false);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "MASKPROCESSOPERATIONNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
		String durableName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String nextProcessFlowName = "";
		String nextProcessFlowVersion = "";
		String nextOperationName = "";
		String nextOperationVersion = "";
		String nextNodeStack = "";
		Boolean countHoldFlag=false;
		Boolean codeHoldFlag=false;

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOutMask", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);

		List<MaskLot> dataInfoList = new ArrayList<MaskLot>();

		List<String> maskLotNameList = CommonUtil.makeList(SMessageUtil.getBodyElement(doc), "MASKLOTLIST", "MASKLOTNAME");

		List<Map<String, Object>> result = getMaskLotList(maskLotNameList);

		if (result.size() > 0)
		{
			if (result.size() == 1)
			{
				// OK
				for (Element mask : maskList)
				{
					String maskLotName = SMessageUtil.getChildText(mask, "MASKLOTNAME", true);
					String position = SMessageUtil.getChildText(mask, "POSITION", true);
					String maskSpecName = SMessageUtil.getChildText(mask, "MASKSPECNAME", true);
					String processFlowName = SMessageUtil.getChildText(mask, "MASKPROCESSFLOWNAME", true);
					String aoiCode = null;
					String repairCode = null;	

					MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });

					if (!StringUtil.equals(maskLotData.getMaskLotProcessState(), constantMap.MaskLotProcessState_Run))
						throw new CustomException("OLEDMASK-0002", maskLotName);

					if(!StringUtil.equals(maskLotData.getCarrierName(), durableName))
					{
						if(StringUtil.isNotEmpty(maskLotData.getCarrierName()))
							throw new CustomException("MASK-1000");
					}
					
					ProcessOperationSpec processOperationData = CommonUtil.getProcessOperationSpec(maskLotData.getFactoryName(), maskLotData.getMaskProcessOperationName(),
							maskLotData.getMaskProcessOperationVersion());

					maskLotData.setCarrierName(durableName);

					ProcessFlowKey processFlowKey = new ProcessFlowKey();
					processFlowKey.setFactoryName(maskLotData.getFactoryName());
					processFlowKey.setProcessFlowName(processFlowName);
					processFlowKey.setProcessFlowVersion("00001");
					ProcessFlow processFlow = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
					
					maskLotData.setPosition(position);

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

						durableData.setDurableCleanState(GenericServiceProxy.getConstantMap().Dur_Dirty);
						DurableServiceProxy.getDurableService().update(durableData);

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
					int aoiCount=maskLotData.getAoiCount()==null?0:maskLotData.getAoiCount().intValue();
					int repairCount=maskLotData.getAoiCount()==null?0:maskLotData.getAoiCount().intValue();
					if(StringUtil.equalsIgnoreCase(processOperationData.getDetailProcessOperationType(), "MASKAOI"))
					{
						maskLotData.setAoiCount(aoiCount+1);
						if((aoiCount+1)>=2)
						{
							countHoldFlag=true;
							aoiCode = SMessageUtil.getChildText(mask, "AOICODE", false);
							if(aoiCode.equals("1")||aoiCode.equals("0"))
							{
								maskLotData.setMaskLotJudge("N");
							}
							else 
							{
								maskLotData.setMaskLotJudge("G");
							}
						}
						else
						{
					    	aoiCode = SMessageUtil.getChildText(mask, "AOICODE", false);
					    	
							if(aoiCode.equals("1"))
							{
								codeHoldFlag=true;
								maskLotData.setMaskLotJudge("N");
							}
							else if(aoiCode.equals("0"))
							{
								maskLotData.setMaskLotJudge("N");
							}
							else 
							{
								maskLotData.setMaskLotJudge("G");
							}

						}
					}
					if(StringUtil.equalsIgnoreCase(processOperationData.getDetailProcessOperationType(), "MASKREPAIR"))
					{
						maskLotData.setMaskRepairCount(repairCount+1);
						if((repairCount+1)>=2)
						{
							countHoldFlag=true;
							repairCode = SMessageUtil.getChildText(mask, "REPAIRCODE", false);
							if(repairCode.equals("1")||repairCode.equals("0"))
							{
								maskLotData.setMaskLotJudge("N");
							}
							else 
							{
								maskLotData.setMaskLotJudge("G");
							}
						}
						else
						{
							repairCode = SMessageUtil.getChildText(mask, "REPAIRCODE", false);
					    	
							if(repairCode.equals("1"))
							{
								codeHoldFlag=true;
								maskLotData.setMaskLotJudge("N");
							}
							else if(repairCode.equals("0"))
							{
								maskLotData.setMaskLotJudge("N");
							}
							else 
							{
								maskLotData.setMaskLotJudge("G");
							}
						}
	
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

						dataInfoList.add(maskLotData);

						continue;
					}
					
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

					dataInfoList.add(maskLotData);
				}
				ExtendedObjectProxy.getMaskLotService().modify(eventInfo, dataInfoList);

				for (MaskLot mask : dataInfoList)
				{
					
					if(countHoldFlag==true)
					{
						EventInfo holdEventInfo =EventInfoUtil.makeEventInfo("HoldMask", this.getEventUser(), "AOICount or RepairCount >=2", "SYSTEM", "HOLD");
						mask=ExtendedObjectProxy.getMaskLotService().maskMultiHold(holdEventInfo, mask);
					}
					if(codeHoldFlag==true)
					{
						EventInfo holdEventInfo =EventInfoUtil.makeEventInfo("HoldMask", this.getEventUser(), "AOI/REPAIR CODE='1'", "SYSTEM", "HOLD");
						mask=ExtendedObjectProxy.getMaskLotService().maskMultiHold(holdEventInfo, mask);
					}
					ExtendedObjectProxy.getMaskLotService().executePostAction(eventInfo, mask);

					// If next node is different with each node of masks in the carrier, not to be assigned.
					checkOperation(mask, durableName);					
				}

				int lotQty = ExtendedObjectProxy.getMaskLotService().assignedMaskLotQtyByCarrier(durableName);

				Machine machineData =  MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
				// Assign Carrier
				if (durableData.getCapacity() < lotQty)
					throw new CustomException("MASKINSPECTION-0002", durableName);
				
				durableData.setLotQuantity(lotQty);
				durableData.setDurableState(constantMap.Dur_InUse);
				for (MaskLot maskLotData : dataInfoList)
				{
					if (StringUtil.equals(maskLotData.getCleanState(), constantMap.Dur_Dirty)
							&& !StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_MaskUnpacker))
					{
						durableData.setDurableCleanState(constantMap.Dur_Dirty);
						break;
					}
				}

				DurableServiceProxy.getDurableService().update(durableData);

				SetEventInfo setEventInfo1 = new SetEventInfo();
				setEventInfo1.getUdfs().put("MACHINENAME", machineName);
				setEventInfo1.getUdfs().put("PORTNAME", portName);
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo1, eventInfo);
			}
		}
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

	private List<Map<String, Object>> getMaskLotList(List<String> maskLotNameList)
	{
		// Check SpecName, OperationName, ProcessFlowName
		String sql = "SELECT DISTINCT MASKSPECNAME, MASKPROCESSOPERATIONNAME, MASKPROCESSFLOWNAME FROM CT_MASKLOT WHERE MASKLOTNAME IN (:MASKLOTLIST)";

		Map<String, Object> inquirybindMap = new HashMap<String, Object>();
		inquirybindMap.put("MASKLOTLIST", maskLotNameList);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, inquirybindMap);

		return result;
	}

	private void checkOperation(MaskLot mask, String durableName) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT MASKLOTNAME, NODESTACK ");
		sql.append("  FROM CT_MASKLOT ");
		sql.append(" WHERE CARRIERNAME = :CARRIERNAME ");
		
		Map<String, String> args = new HashMap<String, String>();
		args.put("CARRIERNAME", durableName);
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> maskLotListInCarrier = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		
		for (Map<String, Object> maskLotMap : maskLotListInCarrier)
		{
			String nodeStack = ConvertUtil.getMapValueByName(maskLotMap, "NODESTACK");
			if (!StringUtil.equals(mask.getNodeStack(), nodeStack))
				throw new CustomException("MASK-0062");
		}
	}
	
}
