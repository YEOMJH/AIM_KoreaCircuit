package kr.co.aim.messolution.machine.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;

import org.jdom.Document;

public class SubUnitStateChanged extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", true);
		String RmachineName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", true);
		String machineStateName = SMessageUtil.getBodyItemValue(doc, "SUBUNITSTATENAME", false);
		String reasonCode =  SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);;

		// check exist Machine
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		String oldMachineState = machineData.getMachineStateName();// old

		if (StringUtil.equals(machineData.getUdfs().get("CHANGESTATELOCKBYOIC"), "Y"))
		{
			throw new CustomException("MACHINE-0036", machineStateName, oldMachineState);
		}

		// if MACHINESTATAE is same, Pass
		if (StringUtil.equals(machineStateName, machineData.getMachineStateName()))
		{
			throw new CustomException("MACHINE-0001", machineName, machineData.getMachineStateName(), machineStateName);
		}

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");

		Map<String, String> machineUdfs = new HashMap<String, String>();
		
//		if (CommonUtil.equalsIn(StringUtils.upperCase(machineStateName), "MANUAL", "DOWN"))
//			machineUdfs.put("CHANGESTATELOCKBYOIC", "Y");
		
		if (machineStateName.equalsIgnoreCase("IDLE"))
		{
			machineUdfs.put("LASTIDLETIME", TimeStampUtil.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
			machineUdfs.put("MACHINESUBSTATE", "R/D");
			//reasonCode = StringUtil.isEmpty(reasonCode)?"I101-Run Down":reasonCode;
		}
		else
		{
			machineUdfs.put("LASTIDLETIME", "");
		}

		if (machineStateName.equalsIgnoreCase("RUN"))
		{
			machineUdfs.put("MACHINESUBSTATE", "Run");
			//reasonCode = StringUtil.isEmpty(reasonCode)?"RuN":reasonCode;

			if (!CommonUtil.getEnumDefValueStringByEnumNameAndEnumValue("IDLETimeByChamberRunRelease",machineName.substring(0, 6)).isEmpty()) 
			{
				eventLog.info("Start Release MachineIdle CT_MACHINEIDLEBYCHAMBER By Machine Report State �쁒UN��");

				String sql = " SELECT * FROM CT_MACHINEIDLEBYCHAMBER CH WHERE CH.CHAMBERNAME = :MACHINENAME AND CH.CONTROLSWITCH='Y' AND CH.IDLEGROUPNAME = 'ChamberIdle' ";

				Map<String, Object> argsIdle = new HashMap<String, Object>();
				argsIdle.put("MACHINENAME", machineName);
				List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, argsIdle);

				if (result != null && !result.isEmpty()) 
				{
					List<Object[]> updateIdleBChamberList = new ArrayList<Object[]>();

					String queryStringUpadte = " UPDATE CT_MACHINEIDLEBYCHAMBER A SET A.CONTROLSWITCH = 'N',A.LASTEVENTTIME =?,A.LASTEVENTTIMEKEY =?, "
							+ " A.LASTEVENTNAME = 'SubUnitStateChanged',A.LASTEVENTUSER = 'MES'  WHERE A.CHAMBERNAME=? ";

					for (Map<String, Object> row : result) 
					{
						List<Object> IdleBChamberList = new ArrayList<Object>();
						String CurrentTime = TimeStampUtil.getCurrentTime(TimeStampUtil.FORMAT_DEFAULT);
						String timeKey = TimeStampUtil.getCurrentEventTimeKey();
						IdleBChamberList.add(CurrentTime);
						IdleBChamberList.add(timeKey);
						IdleBChamberList.add(machineName);
						updateIdleBChamberList.add(IdleBChamberList.toArray());
					}

					try 
					{
						MESLotServiceProxy.getLotServiceUtil().updateBatch(queryStringUpadte, updateIdleBChamberList);
						eventLog.info("UPDATE CT_MACHINEIDLEBYCHAMBER success ");
					} 
					catch (Exception e) 
					{
						eventLog.info("UPDATE CT_MACHINEIDLEBYCHAMBER Fail ");
					}
				} 
				else 
				{
					eventLog.info("ChamberIdleTime not over yet");
				}
			}
		}
		else if (machineStateName.equalsIgnoreCase("DOWN"))
		{
			machineUdfs.put("MACHINESUBSTATE", "Machine Down");
			//reasonCode = StringUtil.isEmpty(reasonCode)?RmachineName:reasonCode;
		}
		//Start 20210126 houxk
		else if(machineStateName.equalsIgnoreCase("MANUAL"))
		{
			machineUdfs.put("MACHINESUBSTATE", "");
			//throw new CustomException("MACHINE-0048", machineStateName);
		}
		//End

		reasonCode = "";
		eventInfo.setReasonCode(reasonCode);

		MakeMachineStateByStateInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, machineStateName);
		transitionInfo.setUdfs(machineUdfs);
		MESMachineServiceProxy.getMachineServiceImpl().makeMachineStateByState(machineData, transitionInfo, eventInfo);

		// success then report to FMB
		GenericServiceProxy.getESBServive().sendBySenderToFMB(doc);

		// send to PMS
		GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("PMS"), doc, "PMSSender");

	}
}
