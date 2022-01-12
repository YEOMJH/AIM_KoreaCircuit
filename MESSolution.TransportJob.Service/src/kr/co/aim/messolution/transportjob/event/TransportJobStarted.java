package kr.co.aim.messolution.transportjob.event;

import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class TransportJobStarted extends AsyncHandler {

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
	public void doWorks(Document doc) throws CustomException {
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TransportStart", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		//Validation : Exist Carrier
		String transportJobName = SMessageUtil.getBodyItemValue(doc, "TRANSPORTJOBNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String currentMachineName = SMessageUtil.getBodyItemValue(doc, "CURRENTMACHINENAME", false);
		String currentPositionType = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONTYPE", false);
		String currentPositionName = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONNAME", false);
		String currentZoneName = SMessageUtil.getBodyItemValue(doc, "CURRENTZONENAME", false);
		String transferState = SMessageUtil.getBodyItemValue(doc, "TRANSFERSTATE", false);
		
		TransportJobCommand transportJobCommandInfo = 
			MESTransportServiceProxy.getTransportJobServiceUtil().getTransportJobInfo(transportJobName);
		
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		//update Current Carrier Location
		durableData = MESTransportServiceProxy.getTransportJobServiceUtil().changeCurrentCarrierLocation(
				durableData, 
				currentMachineName, currentPositionType, currentPositionName, currentZoneName, 
				transferState, "", eventInfo);

		//Update CT_TRANSPORTJOBCOMMAND
		MESTransportServiceProxy.getTransportJobServiceUtil().updateTransportJobCommand(transportJobName, doc, eventInfo);
		
		Lot lotData = new Lot();
		try
		{
			lotData = MESLotServiceProxy.getLotInfoUtil().getLotInfoBydurableName(carrierName);
		}catch(Exception e){}
		
		if(lotData != null)
		{
			List<Map<String, Object>> reserveProductSpecList = 
				MESTransportServiceProxy.getTransportJobServiceUtil().getReserveProductSpecList(
					lotData.getProductSpecName(), lotData.getProcessOperationName(), 
					transportJobCommandInfo.getDestinationMachineName());
			
			if(reserveProductSpecList.size() > 0)
			{
				if(StringUtils.equals((String)reserveProductSpecList.get(0).get("RESERVESTATE"), "Executing"))
				{
					try
					{
						MESTransportServiceProxy.getTransportJobServiceImpl().updateReserveProductSpec(
								eventInfo, (String)reserveProductSpecList.get(0).get("MACHINENAME"), 
								(String)reserveProductSpecList.get(0).get("PROCESSOPERATIONGROUPNAME"), 
								(String)reserveProductSpecList.get(0).get("PROCESSOPERATIONNAME"), 
								(String)reserveProductSpecList.get(0).get("PRODUCTSPECNAME"), 
								"SAME", "SAME", "SAME", 
								String.valueOf(Integer.valueOf((String)reserveProductSpecList.get(0).get("COMPLETEQUANTITY"))+1));
					}
					catch(Exception e){}
				}
			}
		}
	}
}
