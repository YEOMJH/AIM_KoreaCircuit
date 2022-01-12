package kr.co.aim.messolution.durable.event;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.event.LotProcessEnd;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class MaterialStateChanged extends AsyncHandler {

	private static Log log = LogFactory.getLog(MaterialStateChanged.class);
	
	public void doWorks(Document doc) throws CustomException
	{
		String smachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String sUnitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String sSubUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String materialName = SMessageUtil.getBodyItemValue(doc, "MATERIALNAME", true);
		String materialPosition = SMessageUtil.getBodyItemValue(doc, "MATERIALPOSITION", false);
		String materialState = SMessageUtil.getBodyItemValue(doc, "MATERIALSTATE", true);
		String materialType = SMessageUtil.getBodyItemValue(doc, "MATERIALTYPE", false);
		String materialUsedCount = SMessageUtil.getBodyItemValue(doc, "MATERIALUSEDCOUNT", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("StateChanged", this.getEventUser(), this.getEventComment(), null, null);

		Consumable consumableData = null;
		Durable durableData = null;

		try
		{
			consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);
		}
		catch (Exception e)
		{
		}

		try
		{
			durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(materialName);
		}
		catch (Exception e)
		{
		}

		if (consumableData == null && durableData == null)
		{
			throw new CustomException("MATERIAL-0001", materialName);
		}

		// MaterialType is Consumable
		if (consumableData != null)
		{
			materialType = consumableData.getConsumableType();

			if (StringUtils.equalsIgnoreCase(materialType, GenericServiceProxy.getConstantMap().MaterialType_Ink)
					|| StringUtils.equalsIgnoreCase(materialType, GenericServiceProxy.getConstantMap().MaterialType_PR))
			{
				if (StringUtils.equalsIgnoreCase(materialState, GenericServiceProxy.getConstantMap().Dur_InUse))
				{
					kr.co.aim.greentrack.consumable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.consumable.management.info.SetEventInfo();
					
					if (StringUtils.equalsIgnoreCase(materialType, "INK"))
					{
						setEventInfo.getUdfs().put("LOADFLAG", "Y");
					}

					String materialLocation = "";
					if (!StringUtils.isEmpty(sSubUnitName))
					{
						materialLocation = sSubUnitName;
					}
					else if (!StringUtils.isEmpty(sUnitName))
					{
						materialLocation = sUnitName;
					}
					else if (!StringUtils.isEmpty(smachineName))
					{
						materialLocation = smachineName;
					}

					consumableData.setConsumableState(GenericServiceProxy.getConstantMap().Cons_InUse);
					consumableData.setMaterialLocationName(materialLocation);
					MESConsumableServiceProxy.getConsumableServiceUtil().update(consumableData);

					setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MaterialLocation_OnEQP);
					setEventInfo.getUdfs().put("MACHINENAME", smachineName);
					setEventInfo.getUdfs().put("UNITNAME", sUnitName);
					setEventInfo.getUdfs().put("KITTIME", TimeStampUtil.toTimeString(eventInfo.getEventTime()));
					setEventInfo.getUdfs().put("KITUSER", eventInfo.getEventUser());
					
					MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(materialName, setEventInfo, eventInfo);
				}
				else
				{
					if (consumableData.getQuantity() == 0)
						consumableData.setConsumableState(GenericServiceProxy.getConstantMap().Cons_NotAvailable);
					else
						consumableData.setConsumableState(GenericServiceProxy.getConstantMap().Cons_Available);

					consumableData.setMaterialLocationName("");
					MESConsumableServiceProxy.getConsumableServiceUtil().update(consumableData);

					kr.co.aim.greentrack.consumable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.consumable.management.info.SetEventInfo();

					setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MaterialLocation_OutStock);
					setEventInfo.getUdfs().put("MACHINENAME", "");
					setEventInfo.getUdfs().put("UNITNAME", "");
					setEventInfo.getUdfs().put("UNKITTIME", TimeStampUtil.toTimeString(eventInfo.getEventTime()));
					setEventInfo.getUdfs().put("LOADFLAG", "");

					MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(materialName, setEventInfo, eventInfo);
				}
			}
		}

		// MaterialType is Durable
		if (durableData != null)
		{
			materialType = durableData.getDurableType();

			if (StringUtils.equalsIgnoreCase(materialType, GenericServiceProxy.getConstantMap().MaterialType_PalletJig)
					|| StringUtils.equalsIgnoreCase(materialType, GenericServiceProxy.getConstantMap().MaterialType_FPC))
			{
				if (StringUtils.equalsIgnoreCase(materialState, GenericServiceProxy.getConstantMap().Dur_InUse))
				{
					String materialLocation = "";
					if (!StringUtils.isEmpty(sSubUnitName))
					{
						materialLocation = sSubUnitName;
					}
					else if (!StringUtils.isEmpty(sUnitName))
					{
						materialLocation = sUnitName;
					}
					else if (!StringUtils.isEmpty(smachineName))
					{
						materialLocation = smachineName;
					}

					durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
					durableData.setMaterialLocationName(materialLocation);
					DurableServiceProxy.getDurableService().update(durableData);

					SetEventInfo setEventInfo = new SetEventInfo();
					setEventInfo.getUdfs().put("MACHINENAME", smachineName);
					setEventInfo.getUdfs().put("UNITNAME", sUnitName);
					setEventInfo.getUdfs().put("KITTIME", TimeStampUtil.toTimeString(eventInfo.getEventTime()));
					MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
				}
				else
				{
					durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
					durableData.setMaterialLocationName("");
					DurableServiceProxy.getDurableService().update(durableData);

					SetEventInfo setEventInfo = new SetEventInfo();
					setEventInfo.getUdfs().put("MACHINENAME", "");
					setEventInfo.getUdfs().put("UNITNAME", sUnitName);
					MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
				}
			}
			else if (StringUtils.equalsIgnoreCase(materialType, GenericServiceProxy.getConstantMap().MaterialType_PhotoMask))
			{
				if(durableData.getDurableState().equals(GenericServiceProxy.getConstantMap().Dur_Scrapped))
				{
					log.info("Scrapped Mask");
					return;
				}
				
				if (StringUtils.isNotEmpty(materialPosition))
				{
					int reticleSlot = 0;
					try
					{
						reticleSlot = Integer.parseInt(materialPosition);
					}
					catch (Exception e)
					{
					}

					if (reticleSlot > 0)
					{
						materialPosition = String.valueOf(reticleSlot);
					}
				}

				if (StringUtils.equals(materialState, GenericServiceProxy.getConstantMap().Dur_InUse))
				{
					durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
					
					/*
					List<Durable> kittedMasks = null;

					String condition = " MACHINENAME = ? AND UNITNAME = ? AND DURABLETYPE = ? AND DURABLESTATE = ? AND TRANSPORTSTATE = ? AND RETICLESLOT IS NULL"; //
					Object[] bindSet = new Object[] { smachineName, sUnitName, "PhotoMask", "InUse", "OutStock" };
					try
					{
						kittedMasks = DurableServiceProxy.getDurableService().select(condition, bindSet);
					}
					catch (Exception de)
					{
					}

					if (kittedMasks != null)
					{
						for (Durable mask : kittedMasks)
						{
							if (!StringUtil.equals(mask.getKey().getDurableName(), durableData.getKey().getDurableName()))
								throw new CustomException("MASK-0031");
						}
					}
					*/
					
					SetEventInfo setEventInfo = new SetEventInfo();
					
					setEventInfo.getUdfs().put("MACHINENAME", smachineName);
					setEventInfo.getUdfs().put("UNITNAME", sUnitName);
					setEventInfo.getUdfs().put("RETICLESLOT", materialPosition);
					setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_ONEQP);
					
					if(durableData.getDurableCleanState().equals(GenericServiceProxy.getConstantMap().Dur_Dirty))
					{
						durableData.setDurableCleanState(GenericServiceProxy.getConstantMap().Dur_Clean);
						
						Map<String, String > durUdfs = durableData.getUdfs();
						
						String cleanUsed = durUdfs.get("CLEANUSED").toString();
						int cleanCount = Integer.parseInt(cleanUsed) + 1;
						
						setEventInfo.getUdfs().put("LASTCLEANTIME", eventInfo.getEventTime().toString());
						setEventInfo.getUdfs().put("CLEANUSED", String.valueOf(cleanCount));
					}
					DurableServiceProxy.getDurableService().update(durableData);

					// SetEvent Info
					MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
				}
				else if (StringUtils.equals(materialState, GenericServiceProxy.getConstantMap().Dur_Mounted))
				{
					if(cleanLimitCheck(durableData))
					{
						durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Mounted);
						DurableServiceProxy.getDurableService().update(durableData);

						SetEventInfo setEventInfo = new SetEventInfo();
						
						setEventInfo.getUdfs().put("MACHINENAME", smachineName);
						setEventInfo.getUdfs().put("UNITNAME", sUnitName);
						setEventInfo.getUdfs().put("RETICLESLOT", materialPosition);
						setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_ONEQP);

						// SetEvent Info
						MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
					}
					else
					{
						durableData.setDurableCleanState(GenericServiceProxy.getConstantMap().Dur_Dirty);
						DurableServiceProxy.getDurableService().update(durableData);

						SetEventInfo setEventInfo = new SetEventInfo();
						
						setEventInfo.getUdfs().put("MACHINENAME", smachineName);
						setEventInfo.getUdfs().put("UNITNAME", sUnitName);
						setEventInfo.getUdfs().put("RETICLESLOT", materialPosition);
						setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_ONEQP);

						// SetEvent Info
						MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
					}
				}
				else if (StringUtils.equals(materialState, GenericServiceProxy.getConstantMap().Dur_UnMounted))
				{
					durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_UnMounted);
					DurableServiceProxy.getDurableService().update(durableData);

					SetEventInfo setEventInfo = new SetEventInfo();
					
					setEventInfo.getUdfs().put("MACHINENAME", "");
					setEventInfo.getUdfs().put("UNITNAME", "");
					setEventInfo.getUdfs().put("RETICLESLOT", "");
					setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_MOVING);
					// SetEvent Info
					MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
				}
				else if (StringUtils.equals(materialState, GenericServiceProxy.getConstantMap().Dur_Prepare))
				{
					durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Prepare);
					DurableServiceProxy.getDurableService().update(durableData);

					SetEventInfo setEventInfo = new SetEventInfo();
					
					setEventInfo.getUdfs().put("MACHINENAME", smachineName);
					setEventInfo.getUdfs().put("UNITNAME", sUnitName);
					setEventInfo.getUdfs().put("RETICLESLOT", "");
					// SetEvent Info
					MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
				}
			}
		}

	}
	
	boolean cleanLimitCheck(Durable durInfo)
	{
		String cleanUsedLimit = durInfo.getUdfs().get("CLEANUSEDLIMIT").toString();
		String lastCleanTime = durInfo.getUdfs().get("LASTCLEANTIME").toString();
		SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentTime = transFormat.format(new Date());
		Date lastCleanTimeDate = null;
		Date currentDate = null;
		try {
			lastCleanTimeDate = transFormat.parse(lastCleanTime);
			currentDate = transFormat.parse(currentTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		//long gapTest = currentDate.getTime() - lastCleanTimeDate.getTime();
		double gap = (double)(currentDate.getTime() - lastCleanTimeDate.getTime()) / (double)(60 * 60 * 1000);
		
		if (gap >= Double.parseDouble(cleanUsedLimit)) 
		{
			return false;
		}
		else
		{
			return true;
		}
	}
}
