package kr.co.aim.messolution.machine.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecHistory;
import kr.co.aim.greentrack.machine.management.data.MachineSpecHistoryKey;
import kr.co.aim.greentrack.machine.management.info.MakeCommunicationStateInfo;
import kr.co.aim.greentrack.machine.management.info.MakeE10StateInfo;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateInfo;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;
import kr.co.aim.greentrack.machine.management.info.UndoInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

@SuppressWarnings({ "unused", "-access" })
public class MachineServiceImpl implements ApplicationContextAware {

	private ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog(MachineServiceImpl.class);

	public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		applicationContext = arg0;
	}

	public void makeMachineState(Machine machineData, MakeMachineStateInfo makeMachineStateInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal,
			DuplicateNameSignal
	{
		if (!StringUtils.equals(machineData.getMachineStateName(), makeMachineStateInfo.getMachineEventName()))
		{
			MachineServiceProxy.getMachineService().makeMachineState(machineData.getKey(), eventInfo, makeMachineStateInfo);
			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		}
		else
		{
			log.info("MachineState is Same. DB : " + machineData.getMachineStateName() + " DATA : " + makeMachineStateInfo.getMachineEventName());
		}
	}

	public void makeMachineStateByState(Machine machineData, MakeMachineStateByStateInfo makeMachineStateByStateInfo, EventInfo eventInfo) throws CustomException
	{
		try
		{
			// Same Value Check
			if (!StringUtils.equals(machineData.getMachineStateName(), makeMachineStateByStateInfo.getMachineStateName()))
			{
				MachineServiceProxy.getMachineService().makeMachineStateByState(machineData.getKey(), eventInfo, makeMachineStateByStateInfo);

				log.info("EventName=" + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
			}
			else
			{
				throw new CustomException("MACHINE-0001", machineData.getKey().getMachineName(), machineData.getMachineStateName(), makeMachineStateByStateInfo.getMachineStateName());
			}

		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("MACHINE-9003", machineData.getKey().getMachineName());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("MACHINE-9999", machineData.getKey().getMachineName());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("MACHINE-9002", machineData.getKey().getMachineName());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("MACHINE-9001", machineData.getKey().getMachineName());
		}
	}

	public void makeCommunicationState(Machine machineData, MakeCommunicationStateInfo makeCommunicationStateInfo, EventInfo eventInfo) throws CustomException
	{
		try
		{
			// Same Value Check
			if (!StringUtils.equals(machineData.getCommunicationState(), makeCommunicationStateInfo.getCommunicationState()))
			{
				log.info("Current CommunicationState=" + machineData.getCommunicationState() + ", Change CommunicationState=" + makeCommunicationStateInfo.getCommunicationState());
				MachineServiceProxy.getMachineService().makeCommunicationState(machineData.getKey(), eventInfo, makeCommunicationStateInfo);

				log.info("EventName=" + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
			}
			else
			{
				throw new CustomException("MACHINE-0001", machineData.getKey().getMachineName(), machineData.getCommunicationState(), makeCommunicationStateInfo.getCommunicationState());
			}
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("MACHINE-9003", machineData.getKey().getMachineName());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("MACHINE-9999", machineData.getKey().getMachineName());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("MACHINE-9002", machineData.getKey().getMachineName());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("MACHINE-9001", machineData.getKey().getMachineName());
		}
	}

	public void makeE10State(Machine machineData, MakeE10StateInfo makeE10StateInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal,
			DuplicateNameSignal
	{
		if (StringUtils.equals(machineData.getE10State(), makeE10StateInfo.getE10State()))
		{
			MachineServiceProxy.getMachineService().makeE10State(machineData.getKey(), eventInfo, makeE10StateInfo);
			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		}
		else
		{
			log.info("E10State is Same. DB : " + machineData.getE10State() + " DATA : " + makeE10StateInfo.getE10State());
		}

	}

	public MachineSpec setEventMachineSpec(EventInfo eventInfo, MachineSpec data, Map<String, String> udfs)
	{
		try
		{
			MachineSpecHistory history = new MachineSpecHistory();
			MachineSpecHistoryKey historyKey = new MachineSpecHistoryKey();

			MachineSpec newData = (MachineSpec) ObjectUtil.copyTo(data);
			newData.setUdfs(udfs);

			/* history = (MachineSpecHistory) */
			ObjectUtil.copyField(data, history);

			historyKey.setMachineName(data.getKey().getMachineName());
			historyKey.setTimeKey(ConvertUtil.getCurrTimeKey());

			history.setKey(historyKey);
			history.setEventName(eventInfo.getEventName());
			history.setEventTime(eventInfo.getEventTime());
			history.setEventUser(eventInfo.getEventUser());

			MachineServiceProxy.getMachineSpecHistoryService().insert(history);
			MachineServiceProxy.getMachineSpecService().update(newData);
			return newData;
		}
		catch (Exception e)
		{
			log.info("Error occurred - setEventForce");
			return data;
		}
	}

	public void setEvent(Machine machineData, SetEventInfo setEventInfo, EventInfo eventInfo) throws CustomException
	{
		try
		{
			MachineServiceProxy.getMachineService().setEvent(machineData.getKey(), eventInfo, setEventInfo);

			log.info("EventName=" + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("MACHINE-9999", machineData.getKey().getMachineName());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("MACHINE-9002", machineData.getKey().getMachineName());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("MACHINE-9001", machineData.getKey().getMachineName());
		}
	}

	public void undo(Machine machineData, UndoInfo undoInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		MachineServiceProxy.getMachineService().undo(machineData.getKey(), eventInfo, undoInfo);

		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void machineAlarmStatChanged(EventInfo eventInfo, String machineName, String unitName, String subUnitName, String alarmCode, String alarmState, String alarmSeverity, String alarmText,
			String productList) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
	{

		StringBuffer sql = new StringBuffer();
		sql.append("INSERT INTO CT_MACHINEALARMLIST ");
		sql.append(" ( MACHINENAME, UNITNAME, SUBUNITNAME,  ");
		sql.append("   ALARMCODE, ALARMSTATE, ALARMSEVERITY, ALARMTEXT, TIMEKEY,  ");
		sql.append("   EVENTNAME, EVENTUSER, EVENTTIME, EVENTCOMMENT, PRODUCTLIST )  ");
		sql.append("VALUES  ");
		sql.append(" ( :machineName, :unitName, :subUnitName,  ");
		sql.append("   :alarmCode, :alarmState, :alarmSeverity, :alarmText, :timeKey,  ");
		sql.append("   :eventName, :eventUser, :eventTime, :eventComment, :productList )  ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("machineName", machineName);
		bindMap.put("unitName", unitName);
		bindMap.put("subUnitName", subUnitName);
		bindMap.put("alarmCode", alarmCode);
		bindMap.put("alarmState", alarmState);
		bindMap.put("alarmSeverity", alarmSeverity);
		bindMap.put("alarmText", alarmText);
		bindMap.put("timeKey", StringUtil.isEmpty(eventInfo.getEventTimeKey()) ? TimeStampUtil.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()) : eventInfo.getEventTimeKey());
		bindMap.put("eventName", eventInfo.getEventName());
		bindMap.put("eventUser", eventInfo.getEventUser());
		bindMap.put("eventTime", eventInfo.getEventTime());
		bindMap.put("eventComment", eventInfo.getEventComment());
		bindMap.put("productList", productList);

		GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);

	}

	public static void machineStateChanged(EventInfo eventInfo, String machineName, String machineState, String fullState) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal,
			DuplicateNameSignal, Exception
	{
		// Get MachineData
		Machine machineData = CommonUtil.getMachineInfo(machineName);

		// Set makeMachineStateInfo
		MakeMachineStateByStateInfo makeMachineStateByStateInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, machineState);

		Map<String, String> machineUdfs = new HashMap<String, String>();

		if (!StringUtils.equals(machineUdfs.get("FULLSTATE"), fullState))
		{
			machineUdfs.put("FULLSTATE", fullState);
		}
		
		makeMachineStateByStateInfo.setUdfs(machineUdfs);

		// Call makeMachineState
		MESMachineServiceProxy.getMachineServiceImpl().makeMachineStateByState(machineData, makeMachineStateByStateInfo, eventInfo);
	}

	public List getEQPAlarm(String machineName, String enableFlag)
	{
		String sql = "SELECT STARTTIME FROM EQPALARM WHERE MACHINENAME = ? AND ENABLE = ?";
		Object[] bindSet = new Object[] { machineName, enableFlag };
		List EQPAlarmList = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindSet);
		return EQPAlarmList;
	}

	public int updateEQPAlarm(Timestamp date, String eventComment, Double overTime, String machineName)
	{
		String sql = "UPDATE EQPALARM SET STARTTIME = ?, EVENTCOMMENT = ? , OVERTIME = ? WHERE MACHINENAME = ?";

		if (eventComment.length() > 50)
		{
			eventComment = eventComment.substring(0, 50);
		}

		Object[] bindSet = new Object[] { date, eventComment, overTime, machineName };
		int result = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().update(sql, bindSet);

		return result;
	}

	public void changeMachineLockFlag(EventInfo eventInfo, Machine machineData, String machineLockFlag)
	{
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("MACHINELOCKFLAG", machineLockFlag);

		MachineServiceProxy.getMachineService().setEvent(machineData.getKey(), eventInfo, setEventInfo);
	}

	public void tcpSend(String machineName)
	{
		int i = 0;
		while (true)
		{
			try
			{
				Format format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss SSSSSS");

				Socket socket = new Socket("10.80.136.71", 4700);

				socket.setSoTimeout(60 * 1000);

				log.info("Connection accept socket:" + socket);

				PrintWriter os = new PrintWriter(socket.getOutputStream());

				BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				os.println(machineName);

				os.flush();

				log.debug(format.format(new java.util.Date()) + " Client:" + machineName);

				Thread.sleep(1000);
				String in = is.readLine();
				log.info(format.format(new java.util.Date()) + " Server:" + in);

				os.close();
				is.close();
				socket.close();

				if (in.isEmpty())
				{
					i++;
				}
				else
				{
					if (in.equals("Roger"))
					{
						break;
					}
					else
					{
						i++;
					}
				}

				if (i == 3)
				{
					break;
				}
			}
			catch (Exception e)
			{
				log.error("Error" + e);
				break;

			}
		}
	}
	public void checkIdleTimeByChamber(Lot lotData, EventInfo eventInfo, String machineName) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append(" WITH IDLE ");
		sql.append("      AS (SELECT M.MACHINENAME, ");
		sql.append("                 MI.CHAMBERNAME, ");
		sql.append("                 M.LASTIDLETIME, ");
		sql.append("                 MS.MAXIDLETIME, ");
		sql.append("                 MS.CHECKIDLETIME, ");
		sql.append("                 ROUND ( (SYSDATE - M.LASTIDLETIME) * 24, 1) ");
		sql.append("                    AS DIFIDLETIME, ");
		sql.append("                 CASE ");
		sql.append("                    WHEN ROUND ( (SYSDATE - M.LASTIDLETIME) * 24, 1) > ");
		sql.append("                            MAXIDLETIME ");
		sql.append("                    THEN ");
		sql.append("                       'Over' ");
		sql.append("                    ELSE ");
		sql.append("                       'More' ");
		sql.append("                 END ");
		sql.append("                    RESULT, ");
		sql.append("                 M.MACHINELOCKFLAG, ");
		sql.append("                 MI.PROCESSOPERATIONNAME, ");
		sql.append("                 MI.PROCESSOPERATIONVERSION ");
		sql.append("            FROM MACHINE M, CT_MACHINEIDLEBYCHAMBER MI, MACHINESPEC MS ");
		sql.append("            WHERE MI.IDLEGROUPNAME = 'ChamberIdle' ");
		sql.append("                 AND M.MACHINENAME = MI.CHAMBERNAME ");
		sql.append("                 AND M.MACHINENAME = MS.MACHINENAME ");
		sql.append("                 AND MI.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("                 AND MI.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		sql.append("                 AND MI.MACHINENAME=:MACHINENAME) ");
		sql.append(" SELECT IDLE.* ");
		sql.append("   FROM IDLE ");
		sql.append("  WHERE LASTIDLETIME = (SELECT MAX (IDLE.LASTIDLETIME) FROM IDLE) ");

		Map<String, String> inquirybindMap = new HashMap<String, String>();
		inquirybindMap.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
		inquirybindMap.put("PROCESSOPERATIONVERSION", lotData.getProcessOperationVersion());
		inquirybindMap.put("MACHINENAME", machineName);

		List<Map<String, Object>> sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), inquirybindMap);

		if (sqlResult.size() > 0 && sqlResult != null && sqlResult.get(0).get("RESULT").equals("Over") && sqlResult.get(0).get("CHECKIDLETIME").equals("Y") && (!lotData.getProductionType().equals("M")&&!lotData.getProductionType().equals("D")))
		{
			//MACHINE-0113: The machine chamber {0} has exceeded IdleTime!
			throw new CustomException("MACHINE-0113",sqlResult.get(0).get("CHAMBERNAME"));
		}
	}
}
