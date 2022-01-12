package kr.co.aim.messolution.machine.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.util.CommonUtil;

import org.apache.commons.lang.StringUtils;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AlarmActionDef;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class MachineStateChanged extends AsyncHandler {

	Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName      = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String RmachineName     = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String machineStateName = SMessageUtil.getBodyItemValue(doc, "MACHINESTATENAME", true);
		String sReasonCodeType  = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", false);
		String sReasonCode      = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);
		
		String startTime = "";
		String endTime   = "";
		String comments  = "";
		
		Machine machineData    = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		String oldMachineState = machineData.getMachineStateName();

		/*if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_EVA))
		{
			throw new CustomException("MACHINE-0034", machineName, oldMachineState);
		}
*/
		if (StringUtil.equals(machineData.getUdfs().get("CHANGESTATELOCKBYOIC"), "Y"))
		{
			throw new CustomException("MACHINE-0036", machineStateName, oldMachineState);
		}

		// if MACHINESTATAE is same, Pass
		if (StringUtil.equals(machineStateName, oldMachineState))
		{
			throw new CustomException("MACHINE-0001", machineName, oldMachineState, machineStateName);
		}

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), sReasonCodeType, sReasonCode);

		Map<String, String> machineUdfs = new HashMap<String, String>();
		
//		if (CommonUtil.equalsIn(StringUtils.upperCase(machineStateName), "MANUAL", "DOWN"))
//			machineUdfs.put("CHANGESTATELOCKBYOIC", "Y");	
		
		if (machineStateName.equalsIgnoreCase("IDLE"))
		{
			machineUdfs.put("MACHINESUBSTATE", "R/D");
		//	sReasonCode = StringUtil.isEmpty(sReasonCode)? "I101-Run Down":sReasonCode;
			if(checkDeleteFLCLoad()&&StringUtils.equals(machineData.getMachineGroupName(), "FLC"))
			{
				
				String deletePostcellLoadInfoSql ="DELETE FROM CT_POSTCELLLOADINFO WHERE MACHINENAME=:MACHINENAME";
	        	List<Object[]> deletePostcellLoadInfo = new ArrayList<Object[]>();
				List<Object> loadInfo = new ArrayList<Object>();
				loadInfo.add(machineName);
				deletePostcellLoadInfo.add(loadInfo.toArray());
				try
				{
				MESLotServiceProxy.getLotServiceUtil().updateBatch(deletePostcellLoadInfoSql, deletePostcellLoadInfo);
				}
				catch (Exception e)
				{
				  log.info("delete Fail");
				}
			}
		}
		
		if (oldMachineState.equalsIgnoreCase("RUN"))
		{
			machineUdfs.put("LASTIDLETIME", TimeStampUtil.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		}
		/*else  //21-0525
		{
			machineUdfs.put("LASTIDLETIME", "");
		}*/

		if (machineStateName.equalsIgnoreCase("RUN"))
		{
			machineUdfs.put("LASTIDLETIME", "");
			machineUdfs.put("MACHINESUBSTATE", "Run");
			//sReasonCode = StringUtil.isEmpty(sReasonCode)? "Run":sReasonCode;
		}
		else if (machineStateName.equalsIgnoreCase("DOWN"))
		{
			machineUdfs.put("MACHINESUBSTATE", "Machine Down");
			//sReasonCode = StringUtil.isEmpty(sReasonCode)?RmachineName:sReasonCode;
			
			//List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getRunningLotListByMachineName(machineName);
			//if(lotList!=null && lotList.size()>0)
			//{
			//	setReserveHold(lotList,machineName);
			//} 
		}
		//Start 20210126 houxk
		else if(machineStateName.equalsIgnoreCase("MANUAL"))
		{
			machineUdfs.put("MACHINESUBSTATE", "");
			//throw new CustomException("MACHINE-0048", machineStateName);
		}
		//End		
		
		sReasonCode = "";
		eventInfo.setReasonCode(sReasonCode);

		MakeMachineStateByStateInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, machineStateName);
		transitionInfo.setUdfs(machineUdfs);
		MESMachineServiceProxy.getMachineServiceImpl().makeMachineStateByState(machineData, transitionInfo, eventInfo);

		
		if (StringUtils.equals(machineStateName, "IDLE") || StringUtils.equals(machineStateName, "DOWN"))
		{
			if (StringUtils.equals(oldMachineState, "MANUAL"))
			{
				// “MANUAL” → “IDLE|DOWN”
				startTime = eventInfo.getEventTimeKey();
				comments = oldMachineState + " → " + machineStateName;
				
				// Make PMS Message
				Document pmsDoc = this.makePMSDoc(doc, machineName, machineStateName, startTime, endTime, comments);
				
				// send to PMS
				GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("PMS"), pmsDoc, "PMSSender");
			}
		}
		try
		
		{
				if (StringUtils.equals(machineStateName, "MANUAL"))
		{
			if (StringUtils.equals(oldMachineState, "IDLE") || StringUtils.equals(oldMachineState, "DOWN"))
			{
				// “IDLE|DOWN” → “MANUAL”
				endTime = eventInfo.getEventTimeKey();
				comments = machineStateName  + " → " + oldMachineState;
				
				// Make PMS Message
				Document pmsDoc = this.makePMSDoc(doc, machineName, machineStateName, startTime, endTime, comments);
				
				// send to PMS
				GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("PMS"), pmsDoc, "PMSSender");
			}
		}
		
		// success then report to FMB
		GenericServiceProxy.getESBServive().sendBySenderToFMB(doc);
				
		}
		
		
		
		
		catch (Exception e) {
			
			
		}
		
		

	}
	
	public void setReserveHold(List<Lot> lotList,String machineName)throws CustomException
	{

		List<AlarmActionDef> actionDataList = null;

		try
		{
			actionDataList = ExtendedObjectProxy.getAlarmActionDefService().select(" WHERE 1=1 AND ALARMTYPE = ? AND MACHINENAME =? AND ACTIONNAME = 'LotHold' AND HOLDFLAG = 'Y'  ORDER BY SEQ ASC ",
					new Object[] { GenericServiceProxy.getConstantMap().AlarmType_MachineDown, machineName });
		}
		catch (greenFrameDBErrorSignal e)
		{
			log.info(String.format("Alarm action data list is empty. search by condition [MachineName = %s, AlarmType = %s, ActionName = %s, HoldFlag <> Y ]",
					GenericServiceProxy.getConstantMap().AlarmType_MachineDown, machineName, "LotHold"));
		}
		catch (Exception e)
		{
			throw new CustomException(e.getCause());
		}

		if (actionDataList == null || actionDataList.size() == 0)
			return;

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("MachineDown", getEventUser(), getEventComment(), actionDataList.get(0).getReasonCodeType(), actionDataList.get(0).getReasonCode());
		for (Lot lotData : lotList)
		{
			MESLotServiceProxy.getLotServiceImpl().insertCtLotFutureMultiHoldActionForAfter(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProcessFlowName(),
					lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), "0", "hold", "System", actionDataList.get(0).getReasonCodeType(),
					actionDataList.get(0).getReasonCode(), "", "", "", "False", "True", "", eventInfo.getEventComment(), "", eventInfo.getEventUser(), "Insert", "", "", "",
					actionDataList.get(0).getReleaseType(), "");
		}
	}
	
	private Document makePMSDoc (Document doc, String machineName, String stauts, String startTime, String endTime, String comments) throws CustomException
	{
		Document pmsDoc = (Document) doc.clone();
		Element bodyElement = SMessageUtil.getBodyElement(pmsDoc);
		
		SMessageUtil.setHeaderItemValue(pmsDoc, "MESSAGENAME", "PMSScheduleReport");
		
		Element newBodyElement = 
				this.generateBodyTemplate(SMessageUtil.getBodyElement(doc), machineName, stauts, startTime, endTime, comments);
		
		bodyElement.detach();
		pmsDoc.getRootElement().addContent(newBodyElement);
		
		return pmsDoc;
	}
	
	private Element generateBodyTemplate(Element bodyElement, String machineName, String stauts, String startTime, String endTime, String comments) throws CustomException
	{
		
		Element newBodyElement = new Element("Body");
		
		XmlUtil.addElement(newBodyElement, "MACHINENAME", machineName);
		XmlUtil.addElement(newBodyElement, "STATUS", stauts);
		XmlUtil.addElement(newBodyElement, "STARTTIME", startTime);
		XmlUtil.addElement(newBodyElement, "ENDTIME", endTime);
		XmlUtil.addElement(newBodyElement, "COMMENTS", comments);

		return newBodyElement;
	}
	private boolean checkDeleteFLCLoad()
	{
		String sql="SELECT*FROM ENUMDEFVALUE WHERE ENUMNAME='FLCDeleteLoadInfo' and ENUMVALUE='FLCDeleteLoadInfo' ";
		Map<String, Object> bindMap = new HashMap<String, Object>();
		List<Map<String,Object>> resultList =null;
		resultList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, bindMap);
		if(resultList!=null&& resultList.size()>0)
		{
			return true;
			
		}
		return false;
	}
}
