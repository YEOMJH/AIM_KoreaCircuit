package kr.co.aim.messolution.lot.event.Tray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.ObjectUtils.Null;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;

public class TrayGroupProcessCanceled extends AsyncHandler 
{
	private static Log log = LogFactory.getLog(TrayGroupProcessCanceled.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
		String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
		String trayGroupName = SMessageUtil.getBodyItemValue(doc, "TRAYGROUPNAME", true);

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		Durable trayGroupData = MESDurableServiceProxy.getDurableServiceUtil().getDurableDataForUpdate(trayGroupName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrayGroupProcessCanceled", getEventUser(), getEventComment());
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

		SetEventInfo setEventInfo = new SetEventInfo();
		DurableServiceProxy.getDurableService().setEvent(trayGroupData.getKey(), eventInfo, setEventInfo);
		//PFL update PostCellLoadInfo
		if(StringUtils.equals(machineData.getMachineGroupName(), "PFL"))
		{
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT L.LOTNAME FROM LOT L,DURABLE D ");
			sql.append("  WHERE L.CARRIERNAME=D.DURABLENAME ");
			sql.append("  AND D.COVERNAME=:TRAYGROUPNAME ");
			sql.append("  AND L.JOBDOWNFLAG=:MACHINENAME ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("TRAYGROUPNAME", trayGroupName);
			bindMap.put("MACHINENAME", machineName);

			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
			if(sqlResult!=null&&sqlResult.size()>0)
			{
				List<Object[]> updateLotArgList = new ArrayList<Object[]>();
				for(int i=0;i<sqlResult.size();i++)
				{
					List<Object> lotBindList = new ArrayList<Object>();
					lotBindList.add(eventInfo.getEventName());
					lotBindList.add(eventInfo.getEventTimeKey());
					lotBindList.add(eventInfo.getEventTime());
					lotBindList.add(eventInfo.getEventUser());
					lotBindList.add(eventInfo.getEventComment());
					lotBindList.add("");
					lotBindList.add(sqlResult.get(i).get("LOTNAME"));
					updateLotArgList.add(lotBindList.toArray());
				}
				
				StringBuffer sqlForUpdate = new StringBuffer();
				sqlForUpdate.append("UPDATE LOT SET ");
				sqlForUpdate.append("       LASTEVENTNAME = ?, ");
				sqlForUpdate.append("       LASTEVENTTIMEKEY = ?, ");
				sqlForUpdate.append("       LASTEVENTTIME = ?, ");
				sqlForUpdate.append("       LASTEVENTUSER = ?, ");
				sqlForUpdate.append("       LASTEVENTCOMMENT = ?, ");
				sqlForUpdate.append("       JOBDOWNFLAG = ? ");
				sqlForUpdate.append(" WHERE LOTNAME = ? ");

				try 
				{
					MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlForUpdate.toString(), updateLotArgList);
				} catch (Exception ex) 
				{
					log.info("JobDownFlag update file");
				}
			}
		}
	}

}
