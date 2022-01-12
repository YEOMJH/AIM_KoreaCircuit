package kr.co.aim.messolution.consumable.event;

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
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
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

public class KitMaterialOnEQPReport extends AsyncHandler {
	private static Log log = LogFactory.getLog(AssignLamiFilmReport.class);

	public void doWorks(Document doc) throws CustomException
	{
		
		String messageName = SMessageUtil.getMessageName(doc);	
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String materialName = SMessageUtil.getBodyItemValue(doc, "MATERIALNAME", true);
		//String materialLocation = SMessageUtil.getBodyItemValue(doc, "MATERIALLOCATION", true);
		String materialPosition = SMessageUtil.getBodyItemValue(doc, "MATERIALPOSITION", false);
		String materialState = SMessageUtil.getBodyItemValue(doc, "MATERIALSTATE", true);
		String materialType = SMessageUtil.getBodyItemValue(doc, "MATERIALTYPE", true);
		
		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(messageName, this.getEventUser(), this.getEventComment(), null, null);
		String timeKey = TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime());

		eventInfo.setEventTimeKey(timeKey);
		
		String materialLocation = "";
		
		if(StringUtil.isNotEmpty(subUnitName))
		{
			materialLocation = subUnitName;
		}
		else if(StringUtil.isNotEmpty(unitName))
		{
			materialLocation = unitName;
		}
		else
		{
			materialLocation = machineName;
		}
		
		if (materialType.equals(constMap.MaterialType_PatternFilm)) 
		{
			Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);
			SetEventInfo setEventInfo = new SetEventInfo();
			
			consumableData.setMaterialLocationName(materialLocation);
			
			setEventInfo.getUdfs().put("MACHINENAME", machineName);
			setEventInfo.getUdfs().put("UNITNAME", unitName);
			
			consumableData.setConsumableState("InUse");
			setEventInfo.getUdfs().put("TRANSPORTSTATE",constMap.MaterialLocation_OnEQP);
			setEventInfo.getUdfs().put("KITQUANTITY", Double.toString(consumableData.getQuantity()));
			setEventInfo.getUdfs().put("KITUSER", eventInfo.getEventUser());
			setEventInfo.getUdfs().put("KITTIME", eventInfo.getEventTime().toString());

			// Set ConsumableState, materialLocation
			ConsumableServiceProxy.getConsumableService().update(consumableData);
			MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(materialName, setEventInfo, eventInfo);
		}
		else
		{
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(materialName);
			kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
			
			boolean assignFlag = false;
			
			if (materialType.equals(constMap.MaterialType_PalletJig))
			{

				double timeUsedLimit = durableData.getTimeUsedLimit();
				double timeUsed = durableData.getTimeUsed();
				double durationUsedLimit = durableData.getDurationUsedLimit();
				double durationUsed = durableData.getDurationUsed();
				
				SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String currentTime = transFormat.format(new Date());
				Date currentDate = null;
				
				try {
					currentDate = transFormat.parse(currentTime);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				if(timeUsed >= timeUsedLimit)
				{
					throw new CustomException("MATERIAL-0032", durableData.getKey().getDurableName());
				}
				if(durationUsed >= durationUsedLimit)
				{
					throw new CustomException("MATERIAL-0033", durableData.getKey().getDurableName());
				}
				if(durableData.getDurableState().equals(constMap.Dur_Scrapped))
				{
					throw new CustomException("MATERIAL-0031", durableData.getKey().getDurableName());
				}

				if (durableData.getUdfs().get("DURABLEHOLDSTATE").equals(GenericServiceProxy.getConstantMap().DURABLE_HOLDSTATE_Y))
					throw new CustomException("MATERIAL-9016", durableData.getKey().getDurableName());

				if (!StringUtils.isEmpty(durableData.getUdfs().get("EXPIRATIONDATE")))
				{
					String palletExpirationDate = durableData.getUdfs().get("EXPIRATIONDATE");

					Date palletExpirationTime = null;

					try {
						palletExpirationTime = transFormat.parse(palletExpirationDate);
					} catch (ParseException e) {
						e.printStackTrace();
					}

					int compare = currentDate.compareTo(palletExpirationTime);

					if (compare > 0)
					{
						throw new CustomException("MATERIAL-9998", durableData.getKey().getDurableName(), palletExpirationDate);
					}
				}

				// Check Policy
				StringBuffer sql = new StringBuffer();
				sql.append("SELECT P.UNITNAME, P.DURABLESPECNAME, P.DURABLESPECVERSION, P.SUBUNITNAME ");
				sql.append("  FROM TMPOLICY T, POSDURABLE P, DURABLE D, MACHINE M ");
				sql.append(" WHERE T.CONDITIONID = P.CONDITIONID ");
				sql.append("   AND D.DURABLENAME = :DURABLENAME ");
				sql.append("   AND P.DURABLESPECNAME = D.DURABLESPECNAME ");
				sql.append("   AND P.DURABLESPECVERSION = D.DURABLESPECVERSION ");
				sql.append("   AND T.FACTORYNAME = D.FACTORYNAME ");
				sql.append("   AND M.MACHINENAME = :MACHINENAME ");
				sql.append("   AND T.MACHINENAME = M.MACHINENAME ");

				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("DURABLENAME", durableData.getKey().getDurableName());
				bindMap.put("MACHINENAME", machineName);

				List<Map<String, Object>> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), bindMap);

				if (sqlResult.size() < 1)
					throw new CustomException("MATERIAL-0013");
				
				MachineSpec machineSpec = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
				
				if (machineSpec.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().DetailMachineType_Unit))
				{
					if(!sqlResult.get(0).get("UNITNAME").toString().equals(machineName))
					{
						throw new CustomException("MATERIAL-0013");
					}
				}
				else if (machineSpec.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().DetailMachineType_SubUnit))
				{
					if(!sqlResult.get(0).get("SUBUNITNAME").toString().equals(machineName))
					{
						throw new CustomException("MATERIAL-0013");
					}
				}

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

				if (fpcList != null && fpcList.size() > 0)
				{
					for (Durable fpc : fpcList)
					{
						kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfoFPC = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
						timeUsedLimit = fpc.getTimeUsedLimit();
						timeUsed = fpc.getTimeUsed();
						durationUsedLimit = fpc.getDurationUsedLimit();
						durationUsed = fpc.getDurationUsed();

						if (timeUsedLimit <= timeUsed)
							throw new CustomException("MATERIAL-9015", fpc.getKey().getDurableName());
						if (durationUsedLimit <= durationUsed)
							throw new CustomException("MATERIAL-9014", fpc.getKey().getDurableName());
						
						if(fpc.getUdfs().get("EXPIRATIONDATE") != null)
						{
							String FPCExpirationDate = fpc.getUdfs().get("EXPIRATIONDATE").toString();
							
							Date expirationTimeFPC = null;

							try {
								expirationTimeFPC = transFormat.parse(FPCExpirationDate);
							} catch (ParseException e) {
								e.printStackTrace();
							}
							
							int compareFPC = currentDate.compareTo(expirationTimeFPC);
							
							if(compareFPC > 0)
							{
								throw new CustomException("MATERIAL-9998", fpc.getKey().getDurableName(), FPCExpirationDate);
							}
						}
						
						MachineSpec locationSpec = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(materialLocation);
						
						// materialLocation column = current machineName / machineName column = machineName / UnitName column = unitName / subUnitName column = subUnitName
						if (locationSpec.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().DetailMachineType_Main))
						{
							fpc.setMaterialLocationName(materialLocation); // input machinename
							setEventInfoFPC.getUdfs().put("MACHINENAME", machineName);

						}
						else if (locationSpec.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().DetailMachineType_Unit))
						{
							fpc.setMaterialLocationName(materialLocation); // input unitname
							setEventInfoFPC.getUdfs().put("MACHINENAME", machineName);
							setEventInfoFPC.getUdfs().put("UNITNAME", unitName);

						}
						//2020 09 17 - dkh
//						else if (machineSpec.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().DetailMachineType_SubUnit))
//						{
//							fpc.setMaterialLocationName(materialLocation); // input subunitname
//							setEventInfoFPC.getUdfs().put("MACHINENAME", machineName);
//							setEventInfoFPC.getUdfs().put("UNITNAME", unitName);
//						}
						else if (locationSpec.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().DetailMachineType_SubUnit))
						{
							fpc.setMaterialLocationName(materialLocation); // input subunitname
							setEventInfoFPC.getUdfs().put("MACHINENAME", machineName);
							setEventInfoFPC.getUdfs().put("UNITNAME", unitName);
						}
						else
						{ // subsubunit error
							throw new CustomException("MATERIAL-0014", machineName);
						}

						fpc.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);

						setEventInfoFPC.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_ONEQP);
						setEventInfoFPC.getUdfs().put("KITTIME", eventInfo.getEventTime().toString());

						// Set durableState, materialLocation
						DurableServiceProxy.getDurableService().update(fpc);
						MESDurableServiceProxy.getDurableServiceImpl().setEvent(fpc, setEventInfoFPC, eventInfo);
					}
				}
				else
				{
					throw new CustomException("MATERIAL-0015");
				}
			}
			else if(materialType.equals(constMap.MaterialType_FPC))
			{
				assignFlag = true;
				
				kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfoFPC = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
				double timeUsedLimit = durableData.getTimeUsedLimit();
				double timeUsed = durableData.getTimeUsed();
				double durationUsedLimit = durableData.getDurationUsedLimit();
				double durationUsed = durableData.getDurationUsed();

				if (timeUsedLimit <= timeUsed)
					throw new CustomException("MATERIAL-9015", durableData.getKey().getDurableName());
				if (durationUsedLimit <= durationUsed)
					throw new CustomException("MATERIAL-9014", durableData.getKey().getDurableName());
				
				SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				
				String currentTime = transFormat.format(new Date());
				Date currentDate = null;
				try {
					currentDate = transFormat.parse(currentTime);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				if(durableData.getUdfs().get("EXPIRATIONDATE") != null)
				{
					String FPCExpirationDate = durableData.getUdfs().get("EXPIRATIONDATE").toString();
					
					Date expirationTimeFPC = null;
					
					try {
						expirationTimeFPC = transFormat.parse(FPCExpirationDate);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					
					int compareFPC = currentDate.compareTo(expirationTimeFPC);
					
					if(compareFPC > 0)
					{
						throw new CustomException("MATERIAL-9998", durableData.getKey().getDurableName(), FPCExpirationDate);
					}
				}
				
				MachineSpec locationSpec = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(materialLocation);
				
				// materialLocation column = current machineName / machineName column = machineName / UnitName column = unitName / subUnitName column = subUnitName
				if (locationSpec.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().DetailMachineType_Main))
				{
					durableData.setMaterialLocationName(materialLocation); // input machinename
					setEventInfoFPC.getUdfs().put("MACHINENAME", machineName);

				}
				else if (locationSpec.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().DetailMachineType_Unit))
				{
					durableData.setMaterialLocationName(materialLocation); // input unitname
					setEventInfoFPC.getUdfs().put("MACHINENAME", machineName);
					setEventInfoFPC.getUdfs().put("UNITNAME", unitName);

				}
				else if (locationSpec.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().DetailMachineType_SubUnit))
				{
					durableData.setMaterialLocationName(materialLocation); // input subunitname
					setEventInfoFPC.getUdfs().put("MACHINENAME", machineName);
					setEventInfoFPC.getUdfs().put("UNITNAME", unitName);
				}
				else
				{ // subsubunit error

					throw new CustomException("MATERIAL-0014", machineName);
				}

				durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);

				setEventInfoFPC.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_ONEQP);
				setEventInfoFPC.getUdfs().put("KITTIME", eventInfo.getEventTime().toString());

				// Set durableState, materialLocation
				DurableServiceProxy.getDurableService().update(durableData);
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfoFPC, eventInfo);
			}
			
			if(!assignFlag)
			{
				durableData.setMaterialLocationName(materialLocation);
				setEventInfo.getUdfs().put("MACHINENAME", machineName);
				setEventInfo.getUdfs().put("UNITNAME", unitName);
				
				durableData.setDurableState(materialState);
				setEventInfo.getUdfs().put("TRANSPORTSTATE",constMap.Dur_ONEQP);
				setEventInfo.getUdfs().put("KITUSER", eventInfo.getEventUser());
				setEventInfo.getUdfs().put("KITTIME", eventInfo.getEventTime().toString());

				// Set ConsumableState, materialLocation
				DurableServiceProxy.getDurableService().update(durableData);
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
			}
		}
	}
}