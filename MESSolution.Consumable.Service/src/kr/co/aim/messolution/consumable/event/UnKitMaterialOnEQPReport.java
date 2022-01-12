package kr.co.aim.messolution.consumable.event;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class UnKitMaterialOnEQPReport extends AsyncHandler {
	private static Log log = LogFactory.getLog(AssignLamiFilmReport.class);

	public void doWorks(Document doc) throws CustomException
	{
		
		String messageName = SMessageUtil.getMessageName(doc);	
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String materialName = SMessageUtil.getBodyItemValue(doc, "MATERIALNAME", true);
		String materialPosition = SMessageUtil.getBodyItemValue(doc, "MATERIALPOSITION", false);
		String materialState = SMessageUtil.getBodyItemValue(doc, "MATERIALSTATE", true);
		String materialType = SMessageUtil.getBodyItemValue(doc, "MATERIALTYPE", true);
		String quantity = SMessageUtil.getBodyItemValue(doc, "QUANTITY", false);
		String timeUsed = SMessageUtil.getBodyItemValue(doc, "TIMEUSED", false);
		String durationUsed = SMessageUtil.getBodyItemValue(doc, "DURATIONUSED", false);
		List<Element> subMaterialList = SMessageUtil.getBodySequenceItemList(doc, "SUBMATERIALLIST", false);
		
		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(messageName, this.getEventUser(), this.getEventComment(), null, null);
		String timeKey = TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime());

		eventInfo.setEventTimeKey(timeKey);
		
		if (materialType.equals(constMap.MaterialType_PatternFilm)) 
		{
			Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);
			
			consumableData.setMaterialLocationName("");
			consumableData.setConsumableState(materialState);
			ConsumableServiceProxy.getConsumableService().update(consumableData);

			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("MACHINENAME", "");
			udfs.put("UNITNAME", "");
			udfs.put("SUBUNITNAME", "");
			udfs.put("KITQUANTITY", "0");
			udfs.put("KITUSER", "");
			udfs.put("UNKITTIME", eventInfo.getEventTime().toString());
			udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MaterialLocation_OutEQP);

			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			// Request by caixu (PatternFilm is not Decreament quantity)
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.setUdfs(udfs);
			
			MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(materialName, setEventInfo , eventInfo);
		}
		else
		{
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(materialName);
			kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
			
			if (materialType.equals(constMap.MaterialType_PalletJig))
			{
				//Timestamp unKitTime = Timestamp.valueOf(eventInfo.getEventTime().toString());
				//Timestamp kitTime = TimeStampUtil.getTimestamp(durableData.getUdfs().get("KITTIME"));

				//long intervalDay = (unKitTime.getTime() - kitTime.getTime()) / (1000 * 60 * 60);

				durableData.setDurationUsed(durableData.getDurationUsed() + Double.parseDouble(durationUsed));
				durableData.setTimeUsed(durableData.getTimeUsed() + Double.parseDouble(timeUsed));
				

				if(durableData.getTimeUsed() > durableData.getTimeUsedLimit() || durableData.getDurationUsed() > durableData.getDurationUsedLimit())
				{
					durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_NotAvailable);
				}
				else
				{
					durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
				}
				
				durableData.setMaterialLocationName("");
				setEventInfo.getUdfs().put("MACHINENAME", "");
				setEventInfo.getUdfs().put("UNITNAME", "");
				setEventInfo.getUdfs().put("SUBUNITNAME", "");
				setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_OUTEQP);
				durableData.setDurableState(materialState);
				setEventInfo.getUdfs().put("UNKITTIME", eventInfo.getEventTime().toString());

				// Set ConsumableState, materialLocation
				DurableServiceProxy.getDurableService().update(durableData);
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
				
				/*
				// Check validation FPC in the pallet
				List<Durable> fpcList = null;
				try
				{
					fpcList = MESDurableServiceProxy.getDurableServiceUtil().getFPCListByPalletJig(durableData.getKey().getDurableName());
				}
				catch (NotFoundSignal nfs)
				{
					throw new CustomException("MATERIAL-0035");
				}
				*/
				for(Element subMaterial : subMaterialList)
				{
					String subMaterialName = subMaterial.getChildText("MATERIALNAME");
					String subMaterialPosition = subMaterial.getChildText("MATERIALPOSITION");
					String subMaterialState = subMaterial.getChildText("MATERIALSTATE");
					String subMaterialType = subMaterial.getChildText("MATERIALTYPE");
					String subQuantity = subMaterial.getChildText("QUANTITY");
					String subTimeUsed = subMaterial.getChildText("TIMEUSED");
					String subDurationUsed = subMaterial.getChildText("DURATIONUSED");
					
					Durable fpc = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(subMaterialName);
				
					kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfoFPC = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
					//Timestamp unKitTimeFPC = Timestamp.valueOf(eventInfo.getEventTime().toString());
					//Timestamp kitTimeFPC = TimeStampUtil.getTimestamp(fpc.getUdfs().get("KITTIME"));

					//long intervalDayFPC = (unKitTimeFPC.getTime() - kitTimeFPC.getTime()) / (1000 * 60 * 60);

					fpc.setDurationUsed(fpc.getDurationUsed() + Double.parseDouble(subDurationUsed));
					fpc.setTimeUsed(fpc.getTimeUsed() + Double.parseDouble(subTimeUsed));
					fpc.setMaterialLocationName("");

					if(fpc.getTimeUsed() > fpc.getTimeUsedLimit() || fpc.getDurationUsed() > fpc.getDurationUsedLimit())
					{
						fpc.setDurableState(GenericServiceProxy.getConstantMap().Dur_NotAvailable);
					}
					else
					{
						fpc.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
					}
					
					setEventInfoFPC.getUdfs().put("MACHINENAME", "");
					setEventInfoFPC.getUdfs().put("UNITNAME", "");
					setEventInfoFPC.getUdfs().put("SUBUNITNAME", "");

					// Set durableState, materialLocation
					DurableServiceProxy.getDurableService().update(fpc);
					MESDurableServiceProxy.getDurableServiceImpl().setEvent(fpc, setEventInfoFPC, eventInfo);
				}
			}
			else if (materialType.equals(constMap.MaterialType_FPC))
			{
				//Timestamp unKitTime = Timestamp.valueOf(eventInfo.getEventTime().toString());
				//Timestamp kitTime = TimeStampUtil.getTimestamp(durableData.getUdfs().get("KITTIME"));

				//long intervalDay = (unKitTime.getTime() - kitTime.getTime()) / (1000 * 60 * 60);

				durableData.setDurationUsed(durableData.getDurationUsed() + Double.parseDouble(durationUsed));
				durableData.setTimeUsed(durableData.getTimeUsed() + Double.parseDouble(timeUsed));

				if(durableData.getTimeUsed() > durableData.getTimeUsedLimit() || durableData.getDurationUsed() > durableData.getDurationUsedLimit())
				{
					durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_NotAvailable);
				}
				else
				{
					durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
				}
				
				durableData.setMaterialLocationName("");
				setEventInfo.getUdfs().put("MACHINENAME", "");
				setEventInfo.getUdfs().put("UNITNAME", "");
				setEventInfo.getUdfs().put("SUBUNITNAME", "");
				setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_OUTEQP);
				durableData.setDurableState(materialState);
				setEventInfo.getUdfs().put("UNKITTIME", eventInfo.getEventTime().toString());

				// Set ConsumableState, materialLocation
				DurableServiceProxy.getDurableService().update(durableData);
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
			}
		}
	}
}