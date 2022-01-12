package kr.co.aim.messolution.transportjob.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.data.PortKey;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ChangeDestinationRequest extends SyncHandler {

	/**
	 * MessageSpec [OIC -> TEX -> MCS]
	 * 
	 * <Body>
	 *    <TRANSPORTJOBNAME />
	 *    <CARRIERNAME />
	 *    <OLDDESTINATIONMACHINENAME />
	 *    <OLDDESTINATIONPOSITIONTYPE />
	 *    <OLDDESTINATIONPOSITIONNAME />
	 *    <OLDDESTINATIONZONENAME />
	 *    <NEWDESTINATIONMACHINENAME />
	 *    <NEWDESTINATIONPOSITIONTYPE />
	 *    <NEWDESTINATIONPOSITIONNAME />
	 *    <NEWDESTINATIONZONENAME />
	 * </Body>
	 */
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeRequest", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		try
		{
			String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);

			// Check Exist Carrier
			String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
			Durable durData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

			// Check Transport Job Completed
			TransportJobCommand objJobData = ExtendedObjectProxy.getTransportJobCommand().selectByKey(false, new Object[] { transportJobName });
			if (objJobData.getJobState().equals(GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Completed))
				throw new CustomException("TRANSPORT-0003", transportJobName, objJobData.getJobState());
			if (objJobData.getJobState().equals(GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Started))//check Started 2020/4/15
				throw new CustomException("TRANSPORT-0003", transportJobName, objJobData.getJobState());

			// Check FullState of Port
			String newDestinationMachineName = SMessageUtil.getBodyItemValue(doc, "NEWDESTINATIONMACHINENAME", true);
			String newDestinationPositionType = SMessageUtil.getBodyItemValue(doc, "NEWDESTINATIONPOSITIONTYPE", true);
			String newDestinationZoneName = SMessageUtil.getBodyItemValue(doc, "NEWDESTINATIONZONENAME", false);
			String newDestinationPortName = SMessageUtil.getBodyItemValue(doc, "NEWDESTINATIONPOSITIONNAME", false);
			if (StringUtils.equals("PORT", newDestinationPositionType))
			{
				PortKey portKey = new PortKey();
				portKey.setMachineName(newDestinationMachineName);
				portKey.setPortName(newDestinationPortName);
				
				Port portData = PortServiceProxy.getPortService().selectByKey(portKey);
				if (StringUtils.equals(GenericServiceProxy.getConstantMap().Port_FULL, portData.getUdfs().get("FULLSTATE")))
				{
					// FullState of port is [{0}]. MachineName={1}, PortName={2}
					throw new CustomException("PORT-3002", GenericServiceProxy.getConstantMap().Port_FULL, newDestinationMachineName, newDestinationPortName);
				}
				if(!portData.getTransferState().equals(GenericServiceProxy.getConstantMap().Port_ReadyToLoad))
				{
					throw new CustomException("PORT-3005",newDestinationMachineName, newDestinationPortName);
				}
			}
			
			// Check NewDestination TransportJob
			checkExistNewDestinationTransportJob(newDestinationMachineName, newDestinationPositionType, newDestinationZoneName, newDestinationPortName);
			
			// Update CT_TRANSPORTJOBCOMMAND
			MESTransportServiceProxy.getTransportJobServiceUtil().updateTransportJobCommand(transportJobName, doc, eventInfo);

			String messageName = SMessageUtil.getHeaderItemValue(doc, "MESSAGENAME", true);
			doc.getRootElement().getChild("Header").getChild("EVENTCOMMENT").setText(messageName);

			// Send Message to MCS
			String replySubject = GenericServiceProxy.getESBServive().getSendSubject("MCS");
			GenericServiceProxy.getESBServive().sendReplyBySender(replySubject, doc, "HIFSender");

			insertDurableHistory(eventInfo, durData);
		}
		catch (Exception e)
		{
			String originalSourceSubjectName = getOriginalSourceSubjectName();
			GenericServiceProxy.getESBServive().sendErrorBySender(originalSourceSubjectName, doc, getLanguage(), e, "OICSender");

			throw new CustomException(e);
		}
		return null;
	}

	private void checkExistNewDestinationTransportJob(String newDestinationMachineName, String newDestinationPositionType, String newDestinationZoneName, String newDestinationPositionName) throws greenFrameDBErrorSignal, CustomException
	{
		String condition = "WHERE 1 = 1 "
						 + "  AND JOBSTATE = 'Started' "
				   		 + "  AND DESTINATIONMACHINENAME = ? "
				   		 + "  AND DESTINATIONPOSITIONTYPE = ? ";
		
		if (StringUtils.equals("PORT", newDestinationPositionType))
		{
			condition += "  AND DESTINATIONPOSITIONNAME = ? ";
		}
		else if (StringUtils.equals("SHELF", newDestinationPositionType))
		{
			condition += "  AND DESTINATIONZONENAME = ? ";
		}

		Object[] bindSet = null;
		if (StringUtils.equals("PORT", newDestinationPositionType))
		{
			bindSet = new Object[] { newDestinationMachineName, newDestinationPositionType, newDestinationPositionName };
		}
		else if (StringUtils.equals("SHELF", newDestinationPositionType))
		{
			bindSet = new Object[] { newDestinationMachineName, newDestinationPositionType, newDestinationZoneName };
		}
		
		List<TransportJobCommand> jobDataList = ExtendedObjectProxy.getTransportJobCommand().select(condition, bindSet);
		if (jobDataList != null && jobDataList.size() > 0)
		{
			// Destination[{0}, {1}] is reserved,TRANSPORTJOBNAME is [{2}]
			throw new CustomException("MACHINE-0032", newDestinationMachineName, StringUtils.equals("PORT", newDestinationPositionType) ? newDestinationPositionName : newDestinationZoneName, jobDataList.get(0).getTransportJobName());
		}
	}
	
	private void insertDurableHistory(EventInfo eventInfo, Durable durData) throws CustomException, ParseException
	{
		// History insert Query & insertArgList
		StringBuffer queryStringHistory = new StringBuffer();
		queryStringHistory.append("INSERT INTO DURABLEHISTORY ");
		queryStringHistory.append("   (DURABLENAME, TIMEKEY, EVENTTIME, EVENTNAME, OLDDURABLESPECNAME, ");
		queryStringHistory.append("    DURABLESPECNAME, OLDDURABLESPECVERSION, DURABLESPECVERSION, MATERIALLOCATIONNAME, TRANSPORTGROUPNAME, ");
		queryStringHistory.append("    TIMEUSEDLIMIT, TIMEUSED, DURATIONUSEDLIMIT, DURATIONUSED, CAPACITY, ");
		queryStringHistory.append("    LOTQUANTITY, OLDFACTORYNAME, FACTORYNAME, OLDAREANAME, AREANAME, ");
		queryStringHistory.append("    DURABLESTATE, DURABLECLEANSTATE, EVENTUSER, EVENTCOMMENT, EVENTFLAG, ");
		queryStringHistory.append("    REASONCODETYPE, REASONCODE, TOTALUSEDCOUNT, LASTCLEANTIME, DURABLEHOLDSTATE, ");
		queryStringHistory.append("    TRANSPORTLOCKFLAG, TRANSPORTSTATE, POSITIONTYPE, MACHINENAME, UNITNAME, ");
		queryStringHistory.append("    PORTNAME, POSITIONNAME, ZONENAME, CLEANUSED, MACHINERECIPENAME, ");
		queryStringHistory.append("    CLEANUSEDLIMIT, TRANSPORTTYPE, DEPARTMENT, RETICLESLOT, CHAMBERUSETYPE, ");
		queryStringHistory.append("    KITTIME, UNKITTIME, ACCUMULATEUSEDTIME, CANCELTIMEKEY, CANCELFLAG, ");
		queryStringHistory.append("    SYSTEMTIME, CONSUMERLOTNAME, CONSUMERPRODUCTNAME, CONSUMERTIMEKEY, CONSUMERPONAME, ");
		queryStringHistory.append("    CONSUMERPOVERSION, PPBOXGRADE, PPBOXSTATE, REPAIRCOUNT, REPAIRTIME, ");
		queryStringHistory.append("    VENDORNUMBER, COVERNAME, POSITION, RESERVEMASKSTOCKER) ");
		queryStringHistory.append(" VALUES   ");
		queryStringHistory.append("   (?, ?, ?, ?, ?, ");
		queryStringHistory.append("    ?, ?, ?, ?, ?, ");
		queryStringHistory.append("    ?, ?, ?, ?, ?, ");
		queryStringHistory.append("    ?, ?, ?, ?, ?, ");
		queryStringHistory.append("    ?, ?, ?, ?, ?, ");
		queryStringHistory.append("    ?, ?, ?, ?, ?, ");
		queryStringHistory.append("    ?, ?, ?, ?, ?, ");
		queryStringHistory.append("    ?, ?, ?, ?, ?, ");
		queryStringHistory.append("    ?, ?, ?, ?, ?, ");
		queryStringHistory.append("    ?, ?, ?, ?, ?, ");
		queryStringHistory.append("    ?, ?, ?, ?, ?, ");
		queryStringHistory.append("    ?, ?, ?, ?, ?, ");
		queryStringHistory.append("    ?, ?, ?, ?) ");

		List<Object[]> insertArgListHistory = new ArrayList<Object[]>();

		EventInfo newEventInfo = EventInfoUtil.makeEventInfo(eventInfo.getEventName(), eventInfo.getEventUser(), eventInfo.getEventComment(), eventInfo.getReasonCodeType(), eventInfo.getReasonCode());
		newEventInfo.setEventTimeKey(eventInfo.getEventTimeKey());
		newEventInfo.setEventTime(eventInfo.getEventTime());

		// insert History
		List<Object> bindList = new ArrayList<Object>();
		bindList.add(durData.getKey().getDurableName());
		bindList.add(newEventInfo.getEventTimeKey());
		bindList.add(newEventInfo.getEventTime());
		bindList.add(newEventInfo.getEventName());
		bindList.add(durData.getDurableSpecName());
		bindList.add(durData.getDurableSpecName());
		bindList.add(durData.getDurableSpecVersion());
		bindList.add(durData.getDurableSpecVersion());
		bindList.add(durData.getMaterialLocationName());
		bindList.add(durData.getTransportGroupName());
		bindList.add(durData.getTimeUsedLimit());
		bindList.add(durData.getTimeUsed());
		bindList.add(durData.getDurationUsedLimit());
		bindList.add(durData.getDurationUsed());
		bindList.add(durData.getCapacity());
		bindList.add(durData.getLotQuantity());
		bindList.add(durData.getFactoryName());
		bindList.add(durData.getFactoryName());
		bindList.add(durData.getAreaName());
		bindList.add(durData.getAreaName());
		bindList.add(durData.getDurableState());
		bindList.add(durData.getDurableCleanState());
		bindList.add(eventInfo.getEventUser());
		bindList.add(eventInfo.getEventComment());
		bindList.add("N"); // eventflag
		bindList.add(durData.getReasonCodeType());
		bindList.add(durData.getReasonCode());
		bindList.add(durData.getUdfs().get("TOTALUSEDCOUNT"));

		String lastCleantime = durData.getUdfs().get("LASTCLEANTIME");
		Date slastCleantime = new Date();
		if (StringUtils.isEmpty(lastCleantime))
		{
			bindList.add("");
		}
		else if (lastCleantime.contains("-"))
		{
			if (lastCleantime.contains(":"))
			{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				slastCleantime = sdf.parse(lastCleantime);
			}
			else
			{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				slastCleantime = sdf.parse(lastCleantime);
			}
			
			bindList.add(slastCleantime);
		}

		bindList.add(durData.getUdfs().get("DURABLEHOLDSTATE"));
		bindList.add(durData.getUdfs().get("TRANSPORTLOCKFLAG"));
		bindList.add(durData.getUdfs().get("TRANSPORTSTATE"));
		bindList.add(durData.getUdfs().get("POSITIONTYPE"));
		bindList.add(durData.getUdfs().get("MACHINENAME"));
		bindList.add(durData.getUdfs().get("UNITNAME"));
		bindList.add(durData.getUdfs().get("PORTNAME"));
		bindList.add(durData.getUdfs().get("POSITIONNAME"));
		bindList.add(durData.getUdfs().get("ZONENAME"));
		bindList.add(durData.getUdfs().get("CLEANUSED"));
		bindList.add(durData.getUdfs().get("MACHINERECIPENAME"));
		bindList.add(durData.getUdfs().get("CLEANUSEDLIMIT"));
		bindList.add(durData.getUdfs().get("TRANSPORTTYPE"));
		bindList.add(durData.getUdfs().get("DEPARTMENT"));
		bindList.add(durData.getUdfs().get("RETICLESLOT"));
		bindList.add(durData.getUdfs().get("CHAMBERUSETYPE"));

		bindList.add(durData.getUdfs().get("KITTIME"));
		bindList.add(durData.getUdfs().get("UNKITTIME"));
		bindList.add(durData.getUdfs().get("ACCUMULATEUSEDTIME"));
		bindList.add(durData.getUdfs().get("CANCELTIMEKEY"));
		bindList.add(durData.getUdfs().get("CANCELFLAG"));

		bindList.add(durData.getUdfs().get("SYSTEMTIME"));
		bindList.add(durData.getUdfs().get("CONSUMERLOTNAME"));
		bindList.add(durData.getUdfs().get("CONSUMERPRODUCTNAME"));
		bindList.add(durData.getUdfs().get("CONSUMERTIMEKEY"));
		bindList.add(durData.getUdfs().get("CONSUMERPONAME"));

		bindList.add(durData.getUdfs().get("CONSUMERPOVERSION"));
		bindList.add(durData.getUdfs().get("PPBOXGRADE"));
		bindList.add(durData.getUdfs().get("PPBOXSTATE"));
		bindList.add(durData.getUdfs().get("REPAIRCOUNT"));
		bindList.add(durData.getUdfs().get("REPAIRTIME"));

		bindList.add(durData.getUdfs().get("VENDORNUMBER"));
		bindList.add(durData.getUdfs().get("COVERNAME"));
		bindList.add(durData.getUdfs().get("POSITION"));
		bindList.add(durData.getUdfs().get("RESERVEMASKSTOCKER"));

		insertArgListHistory.add(bindList.toArray());

		if (insertArgListHistory.size() > 0)
		{
			try
			{
				if (insertArgListHistory.size() == 1)
				{
					GenericServiceProxy.getSqlMesTemplate().update(queryStringHistory.toString(), insertArgListHistory.get(0));
				}
				else
				{
					GenericServiceProxy.getSqlMesTemplate().updateBatch(queryStringHistory.toString(), insertArgListHistory);
				}
			}
			catch (Exception e)
			{
				throw new CustomException();
			}
		}
	}

}