package kr.co.aim.messolution.transportjob.event;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.CleanInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class CarrierLocationChanged extends AsyncHandler {
	private static Log log = LogFactory.getLog(CarrierLocationChanged.class);
	/**
	 * MessageSpec [MCS -> TEX]
	 * 
	 * <Body>
	 *    <TRANSPORTJOBNAME />
	 *    <CARRIERNAME />
	 *    <CURRENTMACHINENAME />
	 *    <CURRENTPOSITIONTYPE />
	 *    <CURRENTPOSITIONNAME />
	 *    <CURRENTZONENAME />
	 *    <CARRIERSTATE />
	 *    <TRANSFERSTATE />
	 *    <ALTERNATEFLAG />
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeLoc", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", false);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String currentMachineName = SMessageUtil.getBodyItemValue(doc, "CURRENTMACHINENAME", false);
		String currentPositionType = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONTYPE", false);
		String currentPositionName = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONNAME", false);
		String currentZoneName = SMessageUtil.getBodyItemValue(doc, "CURRENTZONENAME", false);
		String transferState = SMessageUtil.getBodyItemValue(doc, "TRANSFERSTATE", false);

		// 1. Check Exist Carrier
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		
		// Change Carrier Location
		durableData = MESTransportServiceProxy.getTransportJobServiceUtil().changeCurrentCarrierLocation(durableData,
				currentMachineName, currentPositionType, currentPositionName, currentZoneName, transferState, "", eventInfo);
		{//Update CST Cleaner Time
			if ((currentMachineName.equals("3ACC01")&&currentPositionName.equals("P02"))||
				 (currentMachineName.equals("3CEW01")&&currentPositionName.equals("P02"))||
				 (currentMachineName.equals("3TCC01")&&currentPositionName.equals("P02")))
			{
		     eventInfo = EventInfoUtil.makeEventInfo("ChangeCSTCleanTime", getEventUser(), getEventComment(), "", "");
			String sLastCleanTime= TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY);
			kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo =new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
			setEventInfo.getUdfs().put("LASTCLEANTIME", sLastCleanTime);
			String sCleanState="Clean";
			CleanInfo cleanInfo = new CleanInfo();
			cleanInfo.setUdfs(durableData.getUdfs());

			Timestamp sLastCleanTimeNew = TimeStampUtil.getCurrentTimestamp();

			StringBuilder updateSql = new StringBuilder();
			updateSql.append("UPDATE DURABLE ");
			updateSql.append("   SET DURABLECLEANSTATE = :DURABLECLEANSTATE, LASTCLEANTIME = :LASTCLEANTIME ");
			updateSql.append(" WHERE DURABLENAME = :DURABLENAME ");

			Map<String, Object> updateMap = new HashMap<String, Object>();
			updateMap.put("DURABLENAME", carrierName);
			updateMap.put("DURABLECLEANSTATE", sCleanState);
			updateMap.put("LASTCLEANTIME", sLastCleanTimeNew);
			GenericServiceProxy.getSqlMesTemplate().update(updateSql.toString(), updateMap);
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
			
		  }
		}
		
		if (StringUtils.isNotEmpty(transportJobName))
		{
			TransportJobCommand sqlRow = new TransportJobCommand();
			try
			{
				sqlRow = ExtendedObjectProxy.getTransportJobCommand().selectByKey(true, new Object[] { transportJobName });
			}
			catch (Exception e)
			{
				eventInfo.setEventComment("Requested");
				TransportJobCommand dataInfo = new TransportJobCommand();
				dataInfo.setTransportJobName(transportJobName);
				dataInfo.setTransportJobType("N/A");
				dataInfo.setCarrierName(carrierName);

				ExtendedObjectProxy.getTransportJobCommand().create(eventInfo, dataInfo);
				eventInfo.setEventComment("ChangeLoc");
			}

			sqlRow = ExtendedObjectProxy.getTransportJobCommand().selectByKey(true, new Object[] { transportJobName });

			List<TransportJobCommand> sqlResult = new ArrayList<TransportJobCommand>();
			sqlResult.add(sqlRow);

			MESTransportServiceProxy.getTransportJobServiceUtil().checkExistTransportJobCommand(sqlResult, transportJobName);
			if(sqlRow.getJobState().equals("Completed"))
			{
				return;
			}
			else
			{
				// Update CT_TRANSPORTJOBCOMMAND
				MESTransportServiceProxy.getTransportJobServiceUtil().updateTransportJobCommand(transportJobName, doc, eventInfo);
			}
		}
		
		//Update film TransportState
		if(StringUtils.equals(currentMachineName, "3FOCV01") && StringUtils.equals(currentPositionType, "PORT") && (StringUtils.equals(currentPositionName, "M02") ||StringUtils.equals(currentPositionName, "M03")))
		{
			if(CommonUtil.equalsIn(durableData.getDurableType(), GenericServiceProxy.getConstantMap().DURABLETYPE_PeelingFilmBox, GenericServiceProxy.getConstantMap().DURABLETYPE_FilmBox) && StringUtils.equals(durableData.getDurableState(), "InUse"))
			{
				try
				{
					List<Consumable> filmList = MESConsumableServiceProxy.getConsumableServiceUtil().getConsumableListByDurable(carrierName);
					if(filmList != null && filmList.size() > 0)
					{
						for (Consumable filmData : filmList)
						{											
							SetEventInfo setEventInfo = new SetEventInfo();
							setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MaterialLocation_InStock);
							ConsumableServiceProxy.getConsumableService().setEvent(filmData.getKey(), eventInfo, setEventInfo);
						}
					}
				}
				catch(Exception e)
				{
					log.info("Update film TransportState error");
				}			
			}
		}
	}
}