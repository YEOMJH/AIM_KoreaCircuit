package kr.co.aim.messolution.machine.event;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineHistory;
import kr.co.aim.greentrack.machine.management.data.MachineHistoryKey;
import kr.co.aim.greentrack.machine.management.data.MachineKey;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;
import kr.co.aim.greentrack.generic.util.XmlUtil;

import org.apache.commons.collections.OrderedMap;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;


public class ChangeMachineState extends SyncHandler {

	@Override
	public Document doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String machineStateName = SMessageUtil.getBodyItemValue(doc, "MACHINESTATENAME", true);
		String machineSubState = SMessageUtil.getBodyItemValue(doc, "MACHINESUBSTATE", false);
		String sReasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", false);
		String sReasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);
		String lastEventTime = SMessageUtil.getBodyItemValue(doc, "LASTEVENTTIME", false);
		
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		String oldMachineState = machineData.getMachineStateName();// old
		String oldMachineSubState = machineData.getUdfs().get("MACHINESUBSTATE");// old
		
		String startTime = "";
		String endTime   = "";
		String comments  = "";
		
		SimpleDateFormat transFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Timestamp tLastEventTime = Timestamp.valueOf(lastEventTime);
		
//		if (StringUtils.equals(machineSubState, oldMachineSubState))
//			throw new CustomException("MACHINE-0033");

		Map<String, String> machineUdfs = new HashMap<String, String>();
		machineUdfs.put("MACHINESUBSTATE", machineSubState);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), sReasonCodeType, sReasonCode);

		if (machineStateName.equalsIgnoreCase("IDLE"))
			machineUdfs.put("LASTIDLETIME", TimeStampUtil.getEventTimeKeyFromTimestamp(tLastEventTime));
			//machineUdfs.put("LASTIDLETIME", TimeStampUtil.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		else
			machineUdfs.put("LASTIDLETIME", "");

		if (CommonUtil.equalsIn(StringUtils.upperCase(machineStateName), "MANUAL", "DOWN"))
			machineUdfs.put("CHANGESTATELOCKBYOIC", "Y");
		else
			machineUdfs.put("CHANGESTATELOCKBYOIC", "");

		MakeMachineStateByStateInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, machineStateName);
		transitionInfo.setUdfs(machineUdfs);
		
		eventInfo.setEventTime(tLastEventTime);
		eventInfo.setEventTimeKey(TimeStampUtil.getEventTimeKeyFromTimestamp(tLastEventTime));	

		this.makeMachineStateByState(machineData, transitionInfo, eventInfo, tLastEventTime);
		
		machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		
		MachineHistoryKey historyKey = new MachineHistoryKey();
		historyKey.setMachineName(machineData.getKey().getMachineName());
		MachineHistory lastHis = MachineServiceProxy.getMachineHistoryService().selectLastOne(historyKey);
		this.makeCTMachineHistory(lastHis, machineData, eventInfo);

		//应制造需求继承DOWN State
	    changeAllSubMachineStateChangeDown(machineData.getFactoryName(), machineName, machineStateName, oldMachineState, machineSubState, eventInfo, tLastEventTime);
				
		changeAllSubMachineStateChange(machineData.getFactoryName(), machineName, machineStateName, oldMachineState, machineSubState, eventInfo, tLastEventTime);				
		
		// send to R2R
		if ("CVD".equals(machineData.getMachineGroupName()) &&
			"MANUAL".equals(machineStateName) &&
			"PM".equals(machineSubState))
		{
			MachineSpec machineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(machineName));
			this.sendToR2R(doc, machineSpecData, machineStateName, machineSubState,tLastEventTime);
		}
		
		if (!StringUtil.equals(machineSubState, "PM"))
		{
			if (StringUtils.equals(oldMachineSubState, "PM"))
			{
				// “PM” → Others
				//endTime =transFormat.format(eventInfo.getEventTime());
				endTime =transFormat.format(tLastEventTime);
				comments = oldMachineSubState + " To " + machineSubState;
				MachineSpec machineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(machineName));
				this.sendToPMS(doc, machineSpecData, machineStateName, startTime, endTime, comments,tLastEventTime);
			}
		}
		
		if (StringUtil.equals(machineSubState, "PM"))
		{
			if (!StringUtils.equals(oldMachineSubState, "PM"))
			{
				// Others->"PM"
				//startTime = transFormat.format(eventInfo.getEventTime());
				startTime = transFormat.format(tLastEventTime);
				comments = oldMachineSubState  + " To " + machineSubState;
				
				MachineSpec machineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(machineName));
				this.sendToPMS(doc, machineSpecData, machineStateName, startTime, endTime, comments,tLastEventTime);
				
			}
		}
		
		// success then report to FMB
		GenericServiceProxy.getESBServive().sendBySenderToFMB(doc);

		// send to PMS
		//GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("PMS"), doc, "PMSSender");
		MachineSpec machineSpec = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
		if(StringUtils.equals(machineSpec.getDetailMachineType(), "UNIT"))
		{
			this.changeFLCVirtualUnitState(eventInfo, machineData.getSuperMachineName(), machineName);
		}
		return doc;
	}
	
	private void makeCTMachineHistory(MachineHistory lastHis, Machine machineData, EventInfo eventInfo) throws greenFrameDBErrorSignal, CustomException 
	{
		kr.co.aim.messolution.extended.object.management.data.MachineHistory MachineStateData = new kr.co.aim.messolution.extended.object.management.data.MachineHistory();

		MachineStateData.setMachineStateName(lastHis.getMachineStateName());
		MachineStateData.setTimeKey(lastHis.getKey().getTimeKey());
		MachineStateData.setEventComment(lastHis.getEventComment());
		MachineStateData.setEventUser(lastHis.getEventUser());
		MachineStateData.setReasonCode(lastHis.getReasonCode());
		MachineStateData.setReasonCodeType(lastHis.getReasonCodeType());
		MachineStateData.setE10State(lastHis.getE10State());
		MachineStateData.setMACHINESUBSTATE(machineData.getUdfs().get("MACHINESUBSTATE"));
		//MachineStateData.setRADIO(Radio);
		MachineStateData.setSYSTEMTIME(TimeStampUtil.getCurrentTimestamp());
		MachineStateData.setOLDE10STATE(lastHis.getOldE10State());
		MachineStateData.setRESOURCESTATE(lastHis.getResourceState());
		MachineStateData.setOLDRESOURCESTATE(lastHis.getOldResourceState());
		MachineStateData.setMachineName(lastHis.getKey().getMachineName());
		MachineStateData.setOldMachineStateName(lastHis.getOldMachineStateName());
		MachineStateData.setMachineEventName(machineData.getMachineEventName());
		MachineStateData.setCommunicationState(lastHis.getCommunicationState());
		MachineStateData.setEventName(lastHis.getEventName());
	    MachineStateData.setCancelFlag("A");
	    MachineStateData.setEventFlag(lastHis.getEventFlag());
	    if(lastHis.getUdfs().get("LASTIDLETIME") != null && !lastHis.getUdfs().get("LASTIDLETIME").isEmpty())
	    {
	    	MachineStateData.setLastIdleTime(Timestamp.valueOf(lastHis.getUdfs().get("LASTIDLETIME")));
	    }		    
	    MachineStateData.setProcessCount(lastHis.getProcessCount());
	    MachineStateData.setMCSUBJECTNAME(lastHis.getUdfs().get("MCSUBJECTNAME"));
	    MachineStateData.setEventTime(lastHis.getEventTime());
	    
		ExtendedObjectProxy.getMachineHistoryService().create(eventInfo, MachineStateData);
		
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
					machineUdfs.put("CHANGESTATELOCKBYOIC", "");
				}
				else
				{
					machineUdfs.put("LASTIDLETIME", "");
				}
				
				if (virtualUnitStateName.equalsIgnoreCase("RUN"))
				{
					machineUdfs.put("MACHINESUBSTATE", "Run");
					machineUdfs.put("CHANGESTATELOCKBYOIC", "");
				}
				else if (virtualUnitStateName.equalsIgnoreCase("DOWN"))
				{
					machineUdfs.put("MACHINESUBSTATE", "Machine Down");
					machineUdfs.put("CHANGESTATELOCKBYOIC", "Y");
				}
				else if(virtualUnitStateName.equalsIgnoreCase("MANUAL"))
				{
					machineUdfs.put("MACHINESUBSTATE", "");
					machineUdfs.put("CHANGESTATELOCKBYOIC", "Y");
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

	private void sendToR2R(Document doc, MachineSpec machineSpecData, String machineStateName, String machineSubStateName, Timestamp tLastEventTime) throws CustomException
	{
		Element originalHeaderElement = doc.getRootElement().getChild(SMessageUtil.Header_Tag);
		
		String eventUser = SMessageUtil.getHeaderItemValue(doc, "EVENTUSER", false);
		String eventComment = SMessageUtil.getHeaderItemValue(doc, "EVENTCOMMENT", false);
		
		Element rootElement = new Element(SMessageUtil.Message_Tag);
		
		Element headerElement = new Element(SMessageUtil.Header_Tag);
		
		String machineName = "";
		String unitName = "";
		String unitStateName = "";
		String subUnitName = "";
		String subUnitStateName = "";
		
		if ("MAIN".equals(machineSpecData.getDetailMachineType()))
		{
			machineName = machineSpecData.getKey().getMachineName();
			
			headerElement.addContent(new Element("MESSAGENAME").setText("MachineChangeState"));
		}
		else if ("UNIT".equals(machineSpecData.getDetailMachineType()))
		{
			machineName = machineSpecData.getSuperMachineName();
			unitName = machineSpecData.getKey().getMachineName();
			
			headerElement.addContent(new Element("MESSAGENAME").setText("UnitChangeState"));
		}
		else if ("SUBUNIT".equals(machineSpecData.getDetailMachineType()))
		{
			MachineKey keyInfo = new MachineKey();
			keyInfo.setMachineName(machineSpecData.getSuperMachineName());
			
			Machine unitData = MachineServiceProxy.getMachineService().selectByKey(keyInfo);
			
			machineName = unitData.getSuperMachineName();
			unitName = machineSpecData.getSuperMachineName();
			unitStateName = unitData.getMachineStateName();
			subUnitName = machineSpecData.getKey().getMachineName();
			
			headerElement.addContent(new Element("MESSAGENAME").setText("SubUnitChangeState"));
		}
		else
		{
			eventLog.info("+++ DetailMachineType = " + machineSpecData.getDetailMachineType());
			return;
		}
		
		headerElement.addContent(new Element("SHOPNAME").setText(machineSpecData.getFactoryName()));
		headerElement.addContent(new Element("MACHINENAME").setText(machineName));
		//headerElement.addContent(new Element("TRANSACTIONID").setText(TimeUtils.getCurrentEventTimeKey()));
		headerElement.addContent(new Element("TRANSACTIONID").setText(TimeStampUtil.getEventTimeKeyFromTimestamp(tLastEventTime)));
		headerElement.addContent(new Element("ORIGINALSOURCESUBJECTNAME").setText(originalHeaderElement.getChildText("ORIGINALSOURCESUBJECTNAME")));
		headerElement.addContent(new Element("SOURCESUBJECTNAME").setText(GenericServiceProxy.getESBServive().getSendSubject("CNXsvr")));
		headerElement.addContent(new Element("TARGETSUBJECTNAME").setText(GenericServiceProxy.getESBServive().getSendSubject("R2R")));
		headerElement.addContent(new Element("EVENTUSER").setText(eventUser));
		headerElement.addContent(new Element("EVENTCOMMENT").setText(eventComment));
		
		rootElement.addContent(headerElement);
		
		Element bodyElement = new Element(SMessageUtil.Body_Tag);
		
		if ("MAIN".equals(machineSpecData.getDetailMachineType()))
		{
			bodyElement.addContent(new Element("MACHINENAME").setText(machineName));
			bodyElement.addContent(new Element("MACHINESTATENAME").setText(machineStateName));
			bodyElement.addContent(new Element("SUBMACHINESTATENAME").setText(machineSubStateName));
		}
		else if ("UNIT".equals(machineSpecData.getDetailMachineType()))
		{
			bodyElement.addContent(new Element("MACHINENAME").setText(machineName));
			bodyElement.addContent(new Element("UNITNAME").setText(unitName));
			bodyElement.addContent(new Element("UNITSTATENAME").setText(machineStateName));
			bodyElement.addContent(new Element("SUBMACHINESTATENAME").setText(machineSubStateName));
		}
		else if ("SUBUNIT".equals(machineSpecData.getDetailMachineType()))
		{
			bodyElement.addContent(new Element("MACHINENAME").setText(machineName));
			bodyElement.addContent(new Element("UNITNAME").setText(unitName));
			bodyElement.addContent(new Element("UNITSTATENAME").setText(unitStateName));
			bodyElement.addContent(new Element("SUBUNITNAME").setText(subUnitName));
			bodyElement.addContent(new Element("SUBUNITSTATENAME").setText(machineStateName));
			bodyElement.addContent(new Element("SUBMACHINESTATENAME").setText(machineSubStateName));
		}
		
		bodyElement.addContent(new Element("REASONCODETYPE").setText(SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", false)));
		bodyElement.addContent(new Element("REASONCODE").setText(SMessageUtil.getBodyItemValue(doc, "REASONCODE", false)));
		bodyElement.addContent(new Element("EVENTUSER").setText(eventUser));
		bodyElement.addContent(new Element("EVENTCOMMENT").setText(eventComment));
		bodyElement.addContent(new Element("TIMESTAMP").setText(TimeStampUtil.getCurrentTime()));
		
		rootElement.addContent(bodyElement);
		
		GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("R2R"), new Document(rootElement), "R2RSender");
	}
	
	private void sendToPMS(Document doc, MachineSpec machineSpecData, String stauts, String startTime, String endTime, String comments, Timestamp tLastEventTime) throws CustomException
	{
		Element originalHeaderElement = doc.getRootElement().getChild(SMessageUtil.Header_Tag);
		
		String eventUser = SMessageUtil.getHeaderItemValue(doc, "EVENTUSER", false);
		String eventComment = SMessageUtil.getHeaderItemValue(doc, "EVENTCOMMENT", false);
		
		Element rootElement = new Element(SMessageUtil.Message_Tag);
		
		Element headerElement = new Element(SMessageUtil.Header_Tag);
		
		String machineName = machineSpecData.getKey().getMachineName();
		
		headerElement.addContent(new Element("MESSAGENAME").setText("PMSScheduleReport"));
		headerElement.addContent(new Element("SHOPNAME").setText(machineSpecData.getFactoryName()));
		headerElement.addContent(new Element("MACHINENAME").setText(machineName));
		//headerElement.addContent(new Element("TRANSACTIONID").setText(TimeUtils.getCurrentEventTimeKey()));
		headerElement.addContent(new Element("TRANSACTIONID").setText(TimeStampUtil.getEventTimeKeyFromTimestamp(tLastEventTime)));
		headerElement.addContent(new Element("ORIGINALSOURCESUBJECTNAME").setText(originalHeaderElement.getChildText("ORIGINALSOURCESUBJECTNAME")));
		headerElement.addContent(new Element("SOURCESUBJECTNAME").setText(GenericServiceProxy.getESBServive().getSendSubject("CNXsvr")));
		headerElement.addContent(new Element("TARGETSUBJECTNAME").setText(GenericServiceProxy.getESBServive().getSendSubject("R2R")));
		headerElement.addContent(new Element("EVENTUSER").setText(eventUser));
		headerElement.addContent(new Element("EVENTCOMMENT").setText(eventComment));
		
		rootElement.addContent(headerElement);
		
		Element bodyElement = new Element(SMessageUtil.Body_Tag);
		XmlUtil.addElement(bodyElement, "MACHINENAME", machineName);
		XmlUtil.addElement(bodyElement, "STATUS", stauts);
		XmlUtil.addElement(bodyElement, "STARTTIME", startTime);
		XmlUtil.addElement(bodyElement, "ENDTIME", endTime);
		XmlUtil.addElement(bodyElement, "COMMENTS", comments);

		
		
		rootElement.addContent(bodyElement);
		
		GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("PMS"), new Document(rootElement), "PMSSender");
	}
	
	private void sendToFMB(String factoryName, String machineName, String machineStateName,EventInfo eventInfo, Timestamp tLastEventTime) throws CustomException
	{
	
		Element rootElement = new Element(SMessageUtil.Message_Tag);
		
		Element headerElement = new Element(SMessageUtil.Header_Tag);
			
		headerElement.addContent(new Element("MESSAGENAME").setText("ChangeMachineState"));
		//headerElement.addContent(new Element("TRANSACTIONID").setText(TimeUtils.getCurrentEventTimeKey()));
		headerElement.addContent(new Element("TRANSACTIONID").setText(TimeStampUtil.getEventTimeKeyFromTimestamp(tLastEventTime)));
		headerElement.addContent(new Element("ORIGINALSOURCESUBJECTNAME").setText(""));
		headerElement.addContent(new Element("EVENTUSER").setText(eventInfo.getEventUser()));
		headerElement.addContent(new Element("EVENTCOMMENT").setText(eventInfo.getEventComment()));
		headerElement.addContent(new Element("LANGUAGE").setText(""));
		
		rootElement.addContent(headerElement);
		
		Element bodyElement = new Element(SMessageUtil.Body_Tag);
		XmlUtil.addElement(bodyElement, "FACTORYNAME", factoryName);
		XmlUtil.addElement(bodyElement, "MACHINENAME", machineName);
		XmlUtil.addElement(bodyElement, "MACHINESTATENAME", machineStateName);
		XmlUtil.addElement(bodyElement, "MACHINESUBSTATE", "");
		XmlUtil.addElement(bodyElement, "REASONCODE", "");
		XmlUtil.addElement(bodyElement, "REASONCODETYPE", "");

		rootElement.addContent(bodyElement);
		
		//Send to FMB
		GenericServiceProxy.getESBServive().sendBySenderToFMB(new Document(rootElement));
	}
	
	private void changeAllSubMachineStateChange(String factoryName, String machineName, String machineStateName, String oldMachineStateName, String machineSubState, EventInfo eventInfo, Timestamp tLastEventTime)
			throws CustomException
	{
		if ((CommonUtil.equalsIn(machineStateName, "MANUAL") || CommonUtil.equalsIn(oldMachineStateName, "MANUAL")) && !CommonUtil.equalsIn(machineStateName, "DOWN") && !CommonUtil.equalsIn(oldMachineStateName, "DOWN"))
		{
			MachineSpec machineSpec = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);

			if ((StringUtils.equals(machineSpec.getDetailMachineType(), "MAIN") || StringUtils.equals(machineSpec.getDetailMachineType(), "UNIT"))
					&& !StringUtils.equals(machineSpec.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_EVA))
			{
				eventLog.info("changeAllSubMachineStateChange-Start");

				List<Machine> machineList = new ArrayList<Machine>();
				List<MachineHistory> machineHistoryList = new ArrayList<MachineHistory>();

				// Get SubMachine List
				List<Machine> machineDataList = null;

				try
				{
					machineDataList = MachineServiceProxy.getMachineService().select(
							" MACHINENAME LIKE :MACHINENAME || '%' AND MACHINENAME != :MACHINENAME /*AND MACHINESTATENAME != :MACHINESTATENAME*/ ORDER BY MACHINENAME ",
							new Object[] { machineName, machineName /* , machineStateName */});
				}
				catch (Exception e)
				{
					return;
				}

				for (Machine machineData : machineDataList)
				{
					String subMachineName = machineData.getKey().getMachineName();
					Machine oldSubMachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(subMachineName);
					try
					{
						Map<String, String> machineUdfs = machineData.getUdfs();
						machineUdfs.put("MACHINESUBSTATE", machineSubState);

						if (StringUtils.equalsIgnoreCase(machineStateName, "IDLE"))
							//machineUdfs.put("LASTIDLETIME", TimeStampUtil.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
							//machineUdfs.put("LASTIDLETIME", TimeStampUtil.getEventTimeKeyFromTimestamp(tLastEventTime));
							machineUdfs.put("LASTIDLETIME", tLastEventTime.toString());
						else
							machineUdfs.put("LASTIDLETIME", "");

						if (CommonUtil.equalsIn(StringUtils.upperCase(machineStateName), "MANUAL", "DOWN"))
							machineUdfs.put("CHANGESTATELOCKBYOIC", "Y");
						else
							machineUdfs.put("CHANGESTATELOCKBYOIC", "");

						Machine newMachineData = (Machine) ObjectUtil.copyTo(machineData);

						newMachineData.setMachineStateName(machineStateName);
						newMachineData.setLastEventComment(eventInfo.getEventComment());
						newMachineData.setLastEventFlag("N");
						newMachineData.setLastEventName(eventInfo.getEventName());
						newMachineData.setLastEventTime(tLastEventTime);
						newMachineData.setLastEventTimeKey(TimeStampUtil.getEventTimeKeyFromTimestamp(tLastEventTime));
						newMachineData.setLastEventUser(eventInfo.getEventUser());
						newMachineData.setReasonCode(eventInfo.getReasonCode());
						newMachineData.setReasonCodeType(eventInfo.getReasonCodeType());
						newMachineData.setMachineEventName(oldSubMachineData.getMachineStateName()+"-"+machineStateName);
						newMachineData.setUdfs(machineUdfs);

						MachineHistory HistoryData = new MachineHistory();
						MachineServiceProxy.getMachineHistoryDataAdaptor().setHV(machineData, newMachineData, HistoryData);
						HistoryData.setCancelFlag("A");

						machineList.add(newMachineData);
						machineHistoryList.add(HistoryData);
						
						makeCTMachineHistory(HistoryData, newMachineData, eventInfo);
						
						//Update to FMB Unit& SubUnit state  -jzli 2020.12.23
						sendToFMB(newMachineData.getFactoryName(),subMachineName,machineStateName,eventInfo,tLastEventTime);				
					}
					catch (Exception e)
					{
						eventLog.info("Failed to Change SubMachine State to " + machineStateName + ". EQP: " + subMachineName);
					}
				}

				// Execute Batch
				if (machineList.size() > 0)
				{
					try
					{
						CommonUtil.executeBatch("update", machineList);
						CommonUtil.executeBatch("insert", machineHistoryList);
						eventLog.info(machineList.size() + " SubMachine State updated");
					}
					catch (Exception e)
					{
						e.printStackTrace();
						throw new CustomException(e.getCause());
					}
				}
				eventLog.info("changeAllSubMachineStateChange-End");
			}
		}
	}

	private void makeMachineStateByState(Machine machineData, MakeMachineStateByStateInfo makeMachineStateByStateInfo, EventInfo eventInfo, Timestamp tLastEventTime) throws CustomException
	{
		try
		{
			MachineServiceProxy.getMachineService().makeMachineStateByState(machineData.getKey(), eventInfo, makeMachineStateByStateInfo);			
			//eventLog.info("EventName=" + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
			eventLog.info("EventName=" + eventInfo.getEventName() + " EventTimeKey=" + TimeStampUtil.getEventTimeKeyFromTimestamp(tLastEventTime));
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
	
	private void changeAllSubMachineStateChangeDown(String factoryName, String machineName, String machineStateName, String oldMachineStateName, String machineSubState, EventInfo eventInfo, Timestamp tLastEventTime)
			throws CustomException
	{
		if (CommonUtil.equalsIn(machineStateName, "DOWN") || CommonUtil.equalsIn(oldMachineStateName, "DOWN"))
		{
			MachineSpec machineSpec = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);

			if ((StringUtils.equals(machineSpec.getDetailMachineType(), "MAIN") || StringUtils.equals(machineSpec.getDetailMachineType(), "UNIT"))
					&& !StringUtils.equals(machineSpec.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_EVA))
			{
				eventLog.info("changeAllSubMachineStateChangeDown-Start");

				List<Machine> machineList = new ArrayList<Machine>();
				List<MachineHistory> machineHistoryList = new ArrayList<MachineHistory>();

				// Get SubMachine List
				List<Machine> machineDataList = null;

				try
				{
					machineDataList = MachineServiceProxy.getMachineService().select(
							" MACHINENAME LIKE :MACHINENAME || '%' AND MACHINENAME != :MACHINENAME AND MACHINENAME NOT LIKE '%DKC%' ORDER BY MACHINENAME ",
							new Object[] { machineName, machineName /* , machineStateName */});
				}
				catch (Exception e)
				{
					return;
				}

				for (Machine machineData : machineDataList)
				{
					String subMachineName = machineData.getKey().getMachineName();
					Machine oldSubMachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(subMachineName);
					try
					{
						Map<String, String> machineUdfs = machineData.getUdfs();
						machineUdfs.put("MACHINESUBSTATE", machineSubState);

						if (StringUtils.equalsIgnoreCase(machineStateName, "IDLE"))
							//machineUdfs.put("LASTIDLETIME", TimeStampUtil.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
							//machineUdfs.put("LASTIDLETIME", TimeStampUtil.getEventTimeKeyFromTimestamp(tLastEventTime));
							machineUdfs.put("LASTIDLETIME", tLastEventTime.toString());
						else
							machineUdfs.put("LASTIDLETIME", "");

						if (CommonUtil.equalsIn(StringUtils.upperCase(machineStateName), "MANUAL", "DOWN"))
							machineUdfs.put("CHANGESTATELOCKBYOIC", "Y");
						else
							machineUdfs.put("CHANGESTATELOCKBYOIC", "");

						Machine newMachineData = (Machine) ObjectUtil.copyTo(machineData);

						newMachineData.setMachineStateName(machineStateName);
						newMachineData.setLastEventComment(eventInfo.getEventComment());
						newMachineData.setLastEventFlag("N");
						newMachineData.setLastEventName(eventInfo.getEventName());
						newMachineData.setLastEventTime(tLastEventTime);
						newMachineData.setLastEventTimeKey(TimeStampUtil.getEventTimeKeyFromTimestamp(tLastEventTime));
						newMachineData.setLastEventUser(eventInfo.getEventUser());
						newMachineData.setReasonCode(eventInfo.getReasonCode());
						newMachineData.setReasonCodeType(eventInfo.getReasonCodeType());
						newMachineData.setMachineEventName(oldSubMachineData.getMachineStateName()+"-"+machineStateName);
						newMachineData.setUdfs(machineUdfs);

						MachineHistory HistoryData = new MachineHistory();
						MachineServiceProxy.getMachineHistoryDataAdaptor().setHV(machineData, newMachineData, HistoryData);
						HistoryData.setCancelFlag("A");

						machineList.add(newMachineData);
						machineHistoryList.add(HistoryData);
						
						makeCTMachineHistory(HistoryData, newMachineData, eventInfo);
						
						//Update to FMB Unit& SubUnit state  -jzli 2020.12.23
						sendToFMB(newMachineData.getFactoryName(),subMachineName,machineStateName,eventInfo,tLastEventTime);
					}
					catch (Exception e)
					{
						eventLog.info("Failed to Change SubMachine State to " + machineStateName + ". EQP: " + subMachineName);
					}
				}

				// Execute Batch
				if (machineList.size() > 0)
				{
					try
					{
						CommonUtil.executeBatch("update", machineList);
						CommonUtil.executeBatch("insert", machineHistoryList);
						eventLog.info(machineList.size() + " SubMachine State updated");
					}
					catch (Exception e)
					{
						e.printStackTrace();
						throw new CustomException(e.getCause());
					}
				}
				eventLog.info("changeAllSubMachineStateChangeDown-End");
			}
		}
	}

}
