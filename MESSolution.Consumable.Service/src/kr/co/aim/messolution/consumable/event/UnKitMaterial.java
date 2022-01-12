package kr.co.aim.messolution.consumable.event;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
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

public class UnKitMaterial extends SyncHandler {

	private static Log log = LogFactory.getLog(UnKitMaterial.class);
	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		List<Element> eMaterialList = SMessageUtil.getBodySequenceItemList(doc, "MATERIALLIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);

		for (Element materialE : eMaterialList)
		{
			String materialID = materialE.getChildText("MATERIALID");
			String materialType = materialE.getChildText("MATERIALTYPE");
			String materialKind = materialE.getChildText("MATERIALKIND");
			String qauntity = materialE.getChildText("QUANTITY");
			String currentQuantity = materialE.getChildText("CURRENTQUANTITY");
			String unkitTime = materialE.getChildText("UNKITTIME");

			eventInfo.setEventName("UnKit");

			if (materialKind.equals(GenericServiceProxy.getConstantMap().MaterialKind_Consumable))
			{
				Consumable consumable = MESConsumableServiceProxy.getConsumableInfoUtil().getMaterialData(materialID);
				String machineName=consumable.getMaterialLocationName();

				if (!consumable.getUdfs().get("TRANSPORTSTATE").equals(GenericServiceProxy.getConstantMap().MaterialLocation_OnEQP))
					throw new CustomException("MATERIAL-9017", materialID);

				if (consumable.getUdfs().get("CONSUMABLEHOLDSTATE").equals(GenericServiceProxy.getConstantMap().DURABLE_HOLDSTATE_Y))
					throw new CustomException("MATERIAL-9016", materialID);

				consumable.setMaterialLocationName(""); // input subunitname
				consumable.setConsumableState(GenericServiceProxy.getConstantMap().Cons_Available);
				ConsumableServiceProxy.getConsumableService().update(consumable);

				Map<String, String> udfs = new HashMap<String, String>();
				udfs.put("MACHINENAME", ""); // input machinename
				udfs.put("UNITNAME", ""); // input unitname
				//udfs.put("KITQUANTITY", "0");//Modify By yueke
				udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MaterialLocation_OutEQP);
				udfs.put("UNKITTIME", unkitTime);
				//udfs.put("KITUSER", "");

				if (StringUtils.equalsIgnoreCase(materialType, GenericServiceProxy.getConstantMap().MaterialType_Ink)
						|| StringUtils.equalsIgnoreCase(materialType, GenericServiceProxy.getConstantMap().MaterialType_PR))
				{
					udfs.put("LOADFLAG", "");
				}

				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

				BigDecimal beforeQty = BigDecimal.valueOf(Double.parseDouble(qauntity));
				BigDecimal afterQty = BigDecimal.valueOf(Double.parseDouble(currentQuantity));

				BigDecimal resultQty = beforeQty.subtract(afterQty);

				DecrementQuantityInfo transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().decrementQuantityInfo(null, null, null, null, eventInfo.getEventTimeKey(), resultQty.doubleValue(), udfs);
				MESConsumableServiceProxy.getConsumableServiceImpl().decrementQuantity(consumable, transitionInfo, eventInfo);

				if (Double.parseDouble(currentQuantity) == 0)
				{
					eventInfo.setEventName("ChangeState");
					eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

					consumable = MESConsumableServiceProxy.getConsumableInfoUtil().getMaterialData(materialID);

					MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();

					MESConsumableServiceProxy.getConsumableServiceImpl().makeNotAvailable(consumable, makeNotAvailableInfo, eventInfo);
				}
				
				//Reset Kitted Material Seq
				try 
				{
					List<Consumable> consumableDataList = ConsumableServiceProxy.getConsumableService().select(
							" CONSUMABLESTATE = 'InUse' AND TRANSPORTSTATE = 'OnEQP' " + " AND MATERIALLOCATIONNAME =  ? "
									+ "  AND CONSUMABLETYPE=? ORDER BY SEQ ASC",
							new Object[] { machineName,consumable.getConsumableType() });
					int newSeq=1;
					for (Consumable consumableData : consumableDataList) 
					{
						if(StringUtils.isNotEmpty(consumableData.getUdfs().get("SEQ"))&&consumableData.getUdfs().get("SEQ").equals(Integer.toString(newSeq)))
						{
							newSeq++;
							continue; 
						}

						SetEventInfo setEventForSeq = new SetEventInfo();
						eventInfo.setEventName("ChangeSEQ");
						setEventForSeq.getUdfs().put("SEQ", Integer.toString(newSeq));
						ConsumableServiceProxy.getConsumableService().update(consumableData);
						MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(consumableData.getKey().getConsumableName(), setEventForSeq, eventInfo);
						newSeq++;
					}
				} 
				catch (Exception e) 
				{
					eventLog.info("No Kitted Material");
				}
			}
			else
			{
				boolean notAssignFlag = false;
				
				Durable durable = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(materialID);
				kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();

				if (!durable.getUdfs().get("TRANSPORTSTATE").equals(GenericServiceProxy.getConstantMap().Dur_ONEQP))
					throw new CustomException("MATERIAL-9017", materialID);

				if (durable.getUdfs().get("DURABLEHOLDSTATE").equals(GenericServiceProxy.getConstantMap().DURABLE_HOLDSTATE_Y))
					throw new CustomException("MATERIAL-9016", materialID);

				Map<String, String> udfs = new HashMap<String, String>();

				if(StringUtil.equals(durable.getDurableType(), "FilmCST") || StringUtil.equals(durable.getDurableType(), "PeelingCST") || StringUtil.equals(durable.getDurableType(), "FilmBox") || StringUtil.equals(durable.getDurableType(), "PeelingBox") )
				{
					SetEventInfo setEventInfoFilm = new SetEventInfo();
					
					if (!durable.getUdfs().get("TRANSPORTSTATE").equals(GenericServiceProxy.getConstantMap().Dur_ONEQP))
						throw new CustomException("MATERIAL-9017", materialID);

					if (durable.getUdfs().get("DURABLEHOLDSTATE").equals(GenericServiceProxy.getConstantMap().DURABLE_HOLDSTATE_Y))
						throw new CustomException("MATERIAL-9016", materialID);
					
					// Check validation Film
					List<Consumable> filmList = null;
					try
					{
						String condition = " WHERE CARRIERNAME = ?";

						Object[] bindSet = new Object[] { materialID };
						filmList = ConsumableServiceProxy.getConsumableService().select(condition, bindSet);
					}
					catch (NotFoundSignal nfs)
					{
						log.info("Consumed all Film");
					}

					if (filmList != null && filmList.size() > 0)
					{
						for (Consumable consumable : filmList)
						{
						    
							consumable.setMaterialLocationName("");
							consumable.setConsumableState("Available");

							setEventInfoFilm.getUdfs().put("MACHINENAME", "");
							setEventInfoFilm.getUdfs().put("UNITNAME", "");
							setEventInfoFilm.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MaterialLocation_OutEQP);
							setEventInfoFilm.getUdfs().put("UNKITTIME", unkitTime);

							// Set durableState, materialLocation
							ConsumableServiceProxy.getConsumableService().update(consumable);
							MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(consumable.getKey().getConsumableName(), setEventInfoFilm, eventInfo);
						}
					}
					else
					{
						log.info("Consumed all Film");
						//throw new CustomException("MATERIAL-0015");
					}
				}
				else if(StringUtil.equals(durable.getDurableType(), GenericServiceProxy.getConstantMap().MaterialType_FPC))
				{	
					notAssignFlag = true;
					
					String useTime = materialE.getChildText("USETIME");
					String useDuration = materialE.getChildText("USEDURATION");
					
					Durable fpc = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(materialID);
					
					fpc.setDurationUsed(Double.parseDouble(useDuration));
					fpc.setTimeUsed(Double.parseDouble(useTime));
					fpc.setMaterialLocationName("");
					setEventInfo.getUdfs().put("MACHINENAME", "");
					setEventInfo.getUdfs().put("UNITNAME", "");
					
					if(fpc.getTimeUsed() > fpc.getTimeUsedLimit() || fpc.getDurationUsed() > fpc.getDurationUsedLimit())
					{
						fpc.setDurableState(GenericServiceProxy.getConstantMap().Dur_NotAvailable);
					}
					else
					{
						fpc.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
					}
					
					setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_OUTEQP);
					setEventInfo.getUdfs().put("UNKITTIME", unkitTime);
					
					// Set durableState, materialLocation
					DurableServiceProxy.getDurableService().update(fpc);
					MESDurableServiceProxy.getDurableServiceImpl().setEvent(fpc, setEventInfo, eventInfo); 
				}

				if(!notAssignFlag)
				{
					Timestamp unKitTime = Timestamp.valueOf(unkitTime);
					Timestamp kitTime = TimeStampUtil.getTimestamp(durable.getUdfs().get("KITTIME"));

					double intervalDay = (unKitTime.getTime() - kitTime.getTime()) / (1000 * 60 * 60);

					durable.setDurationUsed(CommonUtil.doubleAdd(durable.getDurationUsed(), intervalDay));
					durable.setTimeUsed(CommonUtil.doubleAdd(durable.getTimeUsed(), 1.0));
					durable.setMaterialLocationName("");
					
					setEventInfo.getUdfs().put("MACHINENAME", "");
					setEventInfo.getUdfs().put("UNITNAME", "");

					if(durable.getTimeUsed() > durable.getTimeUsedLimit() || durable.getDurationUsed() > durable.getDurationUsedLimit())
					{
						durable.setDurableState(GenericServiceProxy.getConstantMap().Dur_NotAvailable);
					}
					else
					{
						if(!(StringUtil.equals(durable.getDurableType(), "FilmCST") || StringUtil.equals(durable.getDurableType(), "PeelingCST") || StringUtil.equals(durable.getDurableType(), "FilmBox") || StringUtil.equals(durable.getDurableType(), "PeelingBox")))
						{
							durable.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
						}
					}
					
					setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_OUTEQP);
					setEventInfo.getUdfs().put("UNKITTIME", unkitTime);
					
					if(StringUtil.equals(durable.getDurableType(), "FilmCST") || StringUtil.equals(durable.getDurableType(), "PeelingCST") || StringUtil.equals(durable.getDurableType(), "FilmBox") || StringUtil.equals(durable.getDurableType(), "PeelingBox"))
					{
						setEventInfo.getUdfs().put("PORTNAME", "");
					}

					// Set durableState, materialLocation
					DurableServiceProxy.getDurableService().update(durable);
					MESDurableServiceProxy.getDurableServiceImpl().setEvent(durable, setEventInfo, eventInfo);
				}
			}
		}

		return doc;
	}
}
