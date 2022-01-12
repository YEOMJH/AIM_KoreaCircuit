package kr.co.aim.messolution.machine.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.OrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;

public class UnitStateReport extends AsyncHandler {

	Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");

		List<Element> unitList = SMessageUtil.getSubSequenceItemList(SMessageUtil.getBodyElement(doc), "UNITLIST", true);

		List<Element> eleUnitList = new ArrayList<Element>();

		for (Element unitElement : unitList)
		{
			String machineName = SMessageUtil.getChildText(unitElement, "UNITNAME", true);
			String machineStateName = SMessageUtil.getChildText(unitElement, "UNITSTATENAME", true);
			String softwareVersion = SMessageUtil.getChildText(unitElement, "SOFTWAREVERSION", true);

			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			String oldMachineState = machineData.getMachineStateName();// old

			try
			{
				if (StringUtil.equals(machineStateName, "MANUAL"))
				{
					throw new CustomException("MACHINE-0035", machineStateName);
				}

				if (StringUtil.equals(machineData.getUdfs().get("CHANGESTATELOCKBYOIC"), "Y"))
				{
					throw new CustomException("MACHINE-0036", machineStateName, oldMachineState);
				}

				// if MACHINESTATAE is same, Pass
				if (StringUtil.equals(machineStateName, machineData.getMachineStateName()))
				{
					throw new CustomException("MACHINE-0001", machineName, machineData.getMachineStateName(), machineStateName);
				}

				Map<String, String> machineUdfs = new HashMap<String, String>();
				if (machineStateName.equalsIgnoreCase("IDLE"))
				{
					machineUdfs.put("LASTIDLETIME", TimeStampUtil.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
				}
				else
				{
					machineUdfs.put("LASTIDLETIME", "");
				}
				
				//change machine Software version
				if (machineData.getUdfs().get("SOFTWAREVERSION").equals(softwareVersion))
				{
					log.info("Software version is same so do not changed");
				}
				else
				{
					machineUdfs.put("SOFTWAREVERSION", softwareVersion);
				}
				
				//Start 20210121 houxk
				machineUdfs.put("MACHINESUBSTATE", "");
				//End

				MakeMachineStateByStateInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, machineStateName);
				transitionInfo.setUdfs(machineUdfs);
				MESMachineServiceProxy.getMachineServiceImpl().makeMachineStateByState(machineData, transitionInfo, eventInfo);

				eleUnitList.add(unitElement);
				this.changeFLCVirtualUnitState(eventInfo,machineData.getSuperMachineName(), machineName);
			}
			catch (CustomException ex)
			{
				eventLog.warn(ex.getLocalizedMessage());
			}
		}
		
		Document cloneDoc = new Document();
		cloneDoc = (Document) doc.clone();
		
		//To-do send to AMS:need cim confirm
		
		// send to PMS
		GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("PMS"), cloneDoc, "PMSSender");

		// success then report to FMB
		GenericServiceProxy.getESBServive().sendBySenderToFMB(doc);
		
		// make Unit List Doc that not error
		XmlUtil.setSubChildren(SMessageUtil.getBodyElement(cloneDoc), "UNITLIST", eleUnitList);

		
	}
	private void changeFLCVirtualUnitState(EventInfo eventInfo, String machineName, String unitName) throws CustomException
	{
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		if (!StringUtil.equals("FLC", machineData.getMachineGroupName()))
		{
			eventLog.info("+++ MachineGroupName = " + machineData.getMachineGroupName());
			return ;
		}
		
		Machine unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
		String unitStateName = unitData.getMachineStateName();
		
		String sql = "SELECT M.MACHINENAME, NVL(M.MACHINESTATENAME, '') AS MACHINESTATENAME, VU.VIRTUALUNITNAME "
				   + "FROM MACHINE M, CT_FLCVIRTUALUNIT VU "
				   + "WHERE 1 = 1 "
				   + "  AND VU.UNITNAME = ? "
				   + "  AND VU.MACHINENAME = M.SUPERMACHINENAME "
				   + "  AND VU.PAIRUNITNAME = M.MACHINENAME ";
		
		List<OrderedMap> resultDataList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, new Object[] { unitName });
		if (resultDataList == null || resultDataList.size() == 0)
		{
			eventLog.info("+++ FLCVirtualUnit data is not exist");
			return ;
		}
		
		for (OrderedMap resultData : resultDataList) 
		{
			try
			{
				String pairUnitStateName = resultData.get("MACHINESTATENAME").toString();
				String virtualUnitName = resultData.get("VIRTUALUNITNAME").toString();
				String UnitName=unitName.substring(7);
				String virtualUnitStateName = StringUtil.EMPTY;
				if (CommonUtil.equalsIn("MANUAL", unitStateName, pairUnitStateName))
				{
					virtualUnitStateName = "MANUAL";
				}
				else if (CommonUtil.equalsIn("DOWN", unitStateName, pairUnitStateName))
				{
					virtualUnitStateName = "DOWN";
				}
				else if (UnitName.equals("IND"))
				{
					if (CommonUtil.equalsIn("RUN",pairUnitStateName))
					{
						
						virtualUnitStateName = "RUN";
						
					}
					if (CommonUtil.equalsIn("IDLE",pairUnitStateName))
					{
						
						virtualUnitStateName = "IDLE";
						
					}
					
				}
				else if (!UnitName.equals("IND"))
				{
					if (CommonUtil.equalsIn("RUN",unitStateName))
					{
						
						virtualUnitStateName = "RUN";
						
					}
					if (CommonUtil.equalsIn("IDLE",unitStateName))
					{
						
						virtualUnitStateName = "IDLE";
						
					}
				}
				else
				{
					eventLog.info("+++ UnitStateName = " + unitStateName + ", PairUnitStateName = " + pairUnitStateName);
					continue;
				}
				
				Map<String, String> machineUdfs = new HashMap<String, String>();
				if (virtualUnitStateName.equalsIgnoreCase("IDLE"))
				{
					machineUdfs.put("LASTIDLETIME", TimeStampUtil.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
					machineUdfs.put("MACHINESUBSTATE", "R/D");
				}
				else
				{
					machineUdfs.put("LASTIDLETIME", "");
				}
				
				if (virtualUnitStateName.equalsIgnoreCase("RUN"))
				{
					machineUdfs.put("MACHINESUBSTATE", "Run");
				}
				else if (virtualUnitStateName.equalsIgnoreCase("DOWN"))
				{
					machineUdfs.put("MACHINESUBSTATE", "Machine Down");
				}
				else if(virtualUnitStateName.equalsIgnoreCase("MANUAL"))
				{
					machineUdfs.put("MACHINESUBSTATE", "");
				}
				
				Machine virtualUnitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(virtualUnitName);

				MakeMachineStateByStateInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(virtualUnitData, virtualUnitStateName);
				transitionInfo.setUdfs(machineUdfs);
				MESMachineServiceProxy.getMachineServiceImpl().makeMachineStateByState(virtualUnitData, transitionInfo, eventInfo);
			}
			catch (Exception ex)
			{
				eventLog.error("+++ Virtual UnitState Changed Error : " + ex.getCause());
			}
		}
	}

}
