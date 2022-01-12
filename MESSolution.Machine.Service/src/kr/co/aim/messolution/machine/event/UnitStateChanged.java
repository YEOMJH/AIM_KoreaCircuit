package kr.co.aim.messolution.machine.event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;

import org.apache.commons.collections.OrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class UnitStateChanged extends AsyncHandler {
	
	private static Log log = LogFactory.getLog(UnitStateChanged.class);
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String superMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String machineStateName = SMessageUtil.getBodyItemValue(doc, "UNITSTATENAME", true);
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);
		
		// Check exist machine
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
			//reasonCode =  StringUtil.isEmpty(reasonCode)?"I101-Run Down":reasonCode;
		}
		else
		{
			machineUdfs.put("LASTIDLETIME", "");
		}

		if (machineStateName.equalsIgnoreCase("RUN"))
		{
			machineUdfs.put("MACHINESUBSTATE", "Run");
			//reasonCode =  StringUtil.isEmpty(reasonCode)?"RuN":reasonCode;
		}
		
		
		else if (machineStateName.equalsIgnoreCase("DOWN"))
		{
			machineUdfs.put("MACHINESUBSTATE", "Machine Down");
			//reasonCode = StringUtil.isEmpty(reasonCode)?RmachineName:reasonCode;
			/*jinlj 2020/11/3 send short message #start
			String factoryName = machineData.getFactoryName();
			String areName = machineData.getAreaName();
			CheckEQPDepart(machineName,reasonCode,factoryName,areName);
			#end*/
			
			//send message to em & wechat
			String[] userGroup = MESMachineServiceProxy.getMachineInfoUtil().getAlarmUserIdByUnitName(machineName);
			if(userGroup != null)
			{
				sendMessageToEM(eventInfo,userGroup,"",superMachineName,machineName,machineStateName,reasonCode,oldMachineState);
			}else
			{
				log.info("when unitstate is down,there is no user need to send emoblie&wechat");
			}
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

		// 2021-02-24	dhko	FLC Virtual UnitState
		this.changeFLCVirtualUnitState(eventInfo, superMachineName, machineName);
		
		// success then report to FMB
		GenericServiceProxy.getESBServive().sendBySenderToFMB(doc);

		// send to PMS
		GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("PMS"), doc, "PMSSender");
	}
	
	/**
	 * 2021-02-24	dhko	FLC Virtual UnitState
	 */
	@SuppressWarnings("unchecked")
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
	
		//2020/11/04 jinlj #start
	private void CheckEQPDepart(String machineName,String reasonCode,String factoryName,String areName) throws CustomException{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT DESCRIPTION,DEFAULTFLAG FROM ENUMDEFVALUE "
				+ "WHERE ENUMNAME = 'EQPDepartment' AND ENUMVALUE =:MACHINENAME ");
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("MACHINENAME", machineName);
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(),args);
		if (sqlResult.size() > 0)
		{
			String alarmGroupName = sqlResult.get(0).get("DESCRIPTION").toString();
			SendFAEmailAndMessage(machineName,reasonCode,factoryName,areName,alarmGroupName);
		}			
	}
	private void SendFAEmailAndMessage(String machineName,String reasonCode,String factoryName,String areName,String alarmGroupName)throws CustomException {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String CurrentTime = df.format(new Date());
		String smsMessage =  alarmSmsText(factoryName,machineName,areName,reasonCode,CurrentTime);
		GenericServiceProxy.getSMSInterface().indexSmsSend(alarmGroupName,  smsMessage);
	}
		// #end
	private String alarmSmsText(String factoryName, String machineName, String areName, String reasonCode,String CurrentTime) {
		StringBuilder smsMessage = new StringBuilder();
		smsMessage.append("\n");
		smsMessage.append("\n");
		smsMessage.append("IndexDown");
		smsMessage.append("\n");
		smsMessage.append("\n");
		smsMessage.append("Factory: ");
		smsMessage.append(factoryName);
		smsMessage.append("\n");
		smsMessage.append("CurrentMachineName: ");
		smsMessage.append(machineName);
		smsMessage.append("\n");
		smsMessage.append("CurrentAreName: ");
		smsMessage.append(areName);
		smsMessage.append("\n");
		smsMessage.append("CurrentTime: ");
		smsMessage.append(CurrentTime);
		smsMessage.append("\n");
		smsMessage.append("ReasonCode: ");
		smsMessage.append(reasonCode);
		return smsMessage.toString();
	}
	
	private void sendMessageToEM(EventInfo eventInfo, String[] userGroup, String title, String machineName, String unitName, 
			String machineStateName, String reasonCode, String oldMachineState) {
		try
		{																
			//String[] userGroup = userList.split(";");
			String detailtitle = "${}CIM楹사퍨易덃겘�싩윥";

			StringBuffer info = new StringBuffer();
			info.append("<pre>=======================MachineInformation=======================</pre>");
			info.append("<pre>	MachineName竊�"+machineName+"</pre>");
			info.append("<pre>	UnitName竊�"+unitName+"</pre>");
			info.append("<pre>	MachineStateName竊�"+machineStateName+"</pre>");
			info.append("<pre>	ReasonCode竊�"+reasonCode+"</pre>");
			info.append("<pre>	OldMachineState竊�"+oldMachineState+"</pre>");
			info.append("<pre>===============================End==============================</pre>");			
			
			String message = info.toString();
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userGroup, title, detailtitle, message, "");
			
			StringBuffer weChatInfo = new StringBuffer();
			weChatInfo.append("<pre>====MachineInformation=====</pre>");
			weChatInfo.append("<pre>MachineName竊�"+machineName+"</pre>");
			weChatInfo.append("<pre>    UnitName竊�"+unitName+"</pre>");
			weChatInfo.append("<pre>	MachineStateName竊�"+machineStateName+"</pre>");
			weChatInfo.append("<pre>	ReasonCode竊�"+reasonCode+"</pre>");
			weChatInfo.append("<pre>	OldMachineState竊�"+oldMachineState+"</pre>");
			weChatInfo.append("<pre>	======MachineInfoEnd======</pre>");
			
			String weChatMessage = weChatInfo.toString();
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userGroup, title, detailtitle, weChatMessage, "");	
		}
		catch (Exception e)
		{
			log.info("eMobile or WeChat Send Error : " + e.getCause());	
		}
	}
}
