package kr.co.aim.messolution.durable.event;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class MaterialStateReport extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String smachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeStateReport", getEventUser(), getEventComment(), "", "");

		Element eleBody = SMessageUtil.getBodyElement(doc);

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		if (eleBody != null)
		{
			for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "MATERIALLIST", false))
			{
				String unitName = SMessageUtil.getChildText(eledur, "UNITNAME", false);
				String subUnitName = SMessageUtil.getChildText(eledur, "SUBUNITNAME", false);
				String portName = SMessageUtil.getChildText(eledur, "PORTNAME", false);
				String materialName = SMessageUtil.getChildText(eledur, "MATERIALNAME", true);
				String materialPosition = SMessageUtil.getChildText(eledur, "MATERIALPOSITION", false);
				String materialState = SMessageUtil.getChildText(eledur, "MATERIALSTATE", true);
				String materialType = SMessageUtil.getChildText(eledur, "MATERIALTYPE", false);
				String materialUsedCount = SMessageUtil.getChildText(eledur, "MATERIALUSEDCOUNT", false);

				// getMachineData
				CommonUtil.getMachineInfo(smachineName).toString();

				Consumable consumableData = null;
				Durable durableData = null;

				if (StringUtils.isEmpty(materialName))
				{
					eventLog.info("MaterialName is null on Position: " + materialPosition);
					continue;
				}

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

				// MaterialType is Consumable
				if (consumableData != null)
				{
					if (materialType.equals("LamiFilm"))
					{
						if (StringUtils.equalsIgnoreCase(materialState, "InUse"))
						{
							kr.co.aim.greentrack.consumable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.consumable.management.info.SetEventInfo();
							
							if (StringUtils.equalsIgnoreCase(consumableData.getConsumableType(), GenericServiceProxy.getConstantMap().MaterialType_Ink))
							{
								setEventInfo.getUdfs().put("LOADFLAG", "Y");
							}

							String materialLocation = "";
							if (!StringUtils.isEmpty(subUnitName))
							{
								materialLocation = subUnitName;
							}
							else if (!StringUtils.isEmpty(unitName))
							{
								materialLocation = unitName;
							}
							else if (!StringUtils.isEmpty(smachineName))
							{
								materialLocation = smachineName;
							}

							consumableData.setConsumableState(GenericServiceProxy.getConstantMap().Cons_NotAvailable);
							consumableData.setMaterialLocationName(materialLocation);
							MESConsumableServiceProxy.getConsumableServiceUtil().update(consumableData);

							
							setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MaterialLocation_OnEQP);
							setEventInfo.getUdfs().put("PORTNAME", portName);
							setEventInfo.getUdfs().put("MACHINENAME", smachineName);
							setEventInfo.getUdfs().put("UNITNAME", unitName);
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
							setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MaterialLocation_OutEQP);
							setEventInfo.getUdfs().put("MACHINENAME", "");
							setEventInfo.getUdfs().put("PORTNAME", "");
							setEventInfo.getUdfs().put("UNITNAME", "");
							setEventInfo.getUdfs().put("UNKITTIME", TimeStampUtil.toTimeString(eventInfo.getEventTime()));

							MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(materialName, setEventInfo, eventInfo);
						}
					}
				}

				// MaterialType is Durable
				if (durableData != null)
				{
					if (StringUtils.equals(durableData.getDurableType(), GenericServiceProxy.getConstantMap().MaterialType_PhotoMask))
					{
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

						if (StringUtils.equals(materialState, "InUse"))
						{
							durableData.setDurableState(materialState);
							DurableServiceProxy.getDurableService().update(durableData);

							SetEventInfo setEventInfo = new SetEventInfo();
							setEventInfo.getUdfs().put("MACHINENAME", smachineName);
							setEventInfo.getUdfs().put("PORTNAME", portName);
							setEventInfo.getUdfs().put("UNITNAME", unitName);
							setEventInfo.getUdfs().put("RETICLESLOT", materialPosition);
							setEventInfo.getUdfs().put("TRANSPORTSTATE", constantMap.Dur_ONEQP);

							// SetEvent Info
							MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
						}
						else if (StringUtils.equals(materialState, "Mounted"))
						{
							durableData.setDurableState(materialState);
							DurableServiceProxy.getDurableService().update(durableData);

							SetEventInfo setEventInfo = new SetEventInfo();
							
							setEventInfo.getUdfs().put("MACHINENAME", smachineName);
							setEventInfo.getUdfs().put("UNITNAME", unitName);
							setEventInfo.getUdfs().put("RETICLESLOT", materialPosition);
							setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_MOVING);

							// SetEvent Info
							MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
						}
						else if (StringUtils.equals(materialState, "UnMounted"))
						{
							durableData.setDurableState(materialState);
							DurableServiceProxy.getDurableService().update(durableData);

							SetEventInfo setEventInfo = new SetEventInfo();
							setEventInfo.getUdfs().put("MACHINENAME", smachineName);
							setEventInfo.getUdfs().put("UNITNAME", unitName);
							setEventInfo.getUdfs().put("RETICLESLOT", "");
							setEventInfo.getUdfs().put("TRANSPORTSTATE", constantMap.Dur_INSTK);

							// SetEvent Info
							MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
						}
						else
						{
							durableData.setDurableState(materialState);
							DurableServiceProxy.getDurableService().update(durableData);

							SetEventInfo setEventInfo = new SetEventInfo();
							setEventInfo.getUdfs().put("MACHINENAME", smachineName);
							setEventInfo.getUdfs().put("PORTNAME", portName);
							setEventInfo.getUdfs().put("UNITNAME", unitName);
							setEventInfo.getUdfs().put("RETICLESLOT", materialPosition);
							setEventInfo.getUdfs().put("TRANSPORTSTATE", constantMap.Dur_ONEQP);
						}
					}
				}
			}
		}
	}
}
