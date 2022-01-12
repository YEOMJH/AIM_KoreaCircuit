package kr.co.aim.messolution.consumable.event;

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
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class KitMaterial extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String materialID = SMessageUtil.getBodyItemValue(doc, "MATERIALID", true);
		String materialKind = SMessageUtil.getBodyItemValue(doc, "MATERIALKIND", true);
		String kitTime = SMessageUtil.getBodyItemValue(doc, "KITTIME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);
		String seq = SMessageUtil.getBodyItemValue(doc, "SEQ", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Kit", getEventUser(), getEventComment(), null, null);

		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		
		SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentTime = transFormat.format(new Date());
		Date currentDate = null;
		
		try {
			currentDate = transFormat.parse(currentTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		if (materialKind.equals(GenericServiceProxy.getConstantMap().MaterialKind_Consumable))
		{
			SetEventInfo setEventInfo = new SetEventInfo();
			
			Consumable consumable = MESConsumableServiceProxy.getConsumableInfoUtil().getMaterialData(materialID);
			
			String expirationDateString = "";
			Date expirationDate = null;
			
			if (!consumable.getConsumableType().equals(constMap.MaterialType_PI) && !consumable.getConsumableType().equals(constMap.MaterialType_Target))
			{
				if(consumable.getUdfs().get("EXPIRATIONDATE") != null)
				{
					expirationDateString = consumable.getUdfs().get("EXPIRATIONDATE").toString();
					
					try {
						expirationDate = transFormat.parse(expirationDateString);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					
					if(currentDate.compareTo(expirationDate) > 0)
					{
						throw new CustomException("MATERIAL-9998", materialID, expirationDateString);
					}
				}
			}

			//long timeUsedLimit = Long.parseLong(StringUtil.isNotEmpty(consumable.getUdfs().get("TIMEUSEDLIMIT")) ? consumable.getUdfs().get("TIMEUSEDLIMIT") : "0");
			//long timeUsed = Long.parseLong(StringUtil.isNotEmpty(consumable.getUdfs().get("TIMEUSED")) ? consumable.getUdfs().get("TIMEUSED") : "0");
			//long durationUsedLimit = Long.parseLong(StringUtil.isNotEmpty(consumable.getUdfs().get("DURATIONUSEDLIMIT")) ? consumable.getUdfs().get("DURATIONUSEDLIMIT") : "0");

			if (consumable.getUdfs().get("CONSUMABLEHOLDSTATE").equals(GenericServiceProxy.getConstantMap().DURABLE_HOLDSTATE_Y))
			{
				throw new CustomException("MATERIAL-9016", materialID);
			}
			
			if (!consumable.getConsumableState().equals(constMap.Dur_Available))
			{
				throw new CustomException("MATERIAL-0003", materialID, consumable.getConsumableState() );
			}
			
			if (consumable.getQuantity() <= 0)
			{
				throw new CustomException("MATERIAL-0018");
			}
			
			//Modify By yueke 20210216 --Required by PH WangYan
			/*
			if (consumable.getConsumableType().equals(constMap.MaterialType_Organicadhesive))
			{
				String thawTimeString = consumable.getUdfs().get("THAWTIME").toString();
				List<Map<String, Object>> thawTimeUsedLimitListMap = CommonUtil.getEnumDefValueByEnumName("ThawTimeUsedLimit");;
				String thawTimeUsedLimit = thawTimeUsedLimitListMap.get(0).get("ENUMVALUE").toString();
				Date thawTime = null;

				try {
					thawTime = transFormat.parse(thawTimeString);
					currentDate = transFormat.parse(currentTime);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				double gap = (double)(currentDate.getTime() - thawTime.getTime()) / (double)(60 * 60 * 1000);
				
				if (gap < Double.parseDouble(thawTimeUsedLimit)) 
				{
					throw new CustomException("MATERIAL-0030", materialID);
				}

				setEventInfo.getUdfs().put("LOADFLAG", "Y");
			}
			else
			{
				setEventInfo.getUdfs().put("LOADFLAG", "Y");
			}*/
			setEventInfo.getUdfs().put("LOADFLAG", "Y");
			// set machine info
			MachineSpec machineSpec = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);

			if (machineSpec.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().DetailMachineType_Main))
			{
				consumable.setMaterialLocationName(machineName); // input machinename
				setEventInfo.getUdfs().put("MACHINENAME", machineName);
			}
			else if (machineSpec.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().DetailMachineType_Unit))
			{
				consumable.setMaterialLocationName(machineName); // input unitname
				setEventInfo.getUdfs().put("MACHINENAME", machineSpec.getSuperMachineName());
				setEventInfo.getUdfs().put("UNITNAME", machineName);

			}
			else if (machineSpec.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().DetailMachineType_SubUnit))
			{
				MachineSpec unitMachineSpec = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineSpec.getSuperMachineName());

				consumable.setMaterialLocationName(machineName); // input subunitname
				setEventInfo.getUdfs().put("MACHINENAME", unitMachineSpec.getSuperMachineName());
				setEventInfo.getUdfs().put("UNITNAME", machineSpec.getSuperMachineName());
			}

			consumable.setConsumableState(GenericServiceProxy.getConstantMap().Cons_InUse);
			setEventInfo.getUdfs().put("KITTIME", kitTime);
			setEventInfo.getUdfs().put("UNKITTIME", "");
			setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MaterialLocation_OnEQP);
			setEventInfo.getUdfs().put("KITQUANTITY", Double.toString(consumable.getQuantity()));
			setEventInfo.getUdfs().put("KITUSER", eventInfo.getEventUser());
			setEventInfo.getUdfs().put("SEQ", seq);

			// Set ConsumableState, materialLocation
			ConsumableServiceProxy.getConsumableService().update(consumable);

			MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(materialID, setEventInfo, eventInfo);
			
			//Reset Kitted Material Seq
			try 
			{
				List<Consumable> consumableDataList = ConsumableServiceProxy.getConsumableService().select(
						" CONSUMABLESTATE = 'InUse' AND TRANSPORTSTATE = 'OnEQP' " + " AND MATERIALLOCATIONNAME =  ? "
								+ " AND CONSUMABLENAME <> ? AND CONSUMABLETYPE=? ORDER BY SEQ ASC",
						new Object[] { machineName,materialID,consumable.getConsumableType() });
				int newSeq=1;
				for (Consumable consumableData : consumableDataList) 
				{
					if(StringUtils.isNotEmpty(consumableData.getUdfs().get("SEQ"))&&consumableData.getUdfs().get("SEQ").equals(Integer.toString(newSeq)))
					{
						newSeq++;
						continue; 
					}

					eventInfo.setEventName("ChangeSEQ");
					SetEventInfo setEventForSeq = new SetEventInfo();
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
			kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
			
			Durable durable = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(materialID);
			MachineSpec machineSpec = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
			
			boolean notAssignFlag = false;
			
			if (StringUtil.equals(durable.getDurableType(), GenericServiceProxy.getConstantMap().MaterialType_PalletJig))
			{
				double timeUsedLimit = durable.getTimeUsedLimit();
				double timeUsed = durable.getTimeUsed();
				double durationUsedLimit = durable.getDurationUsedLimit();
				double durationUsed = durable.getDurationUsed();
				
				if(timeUsed >= timeUsedLimit)
				{
					throw new CustomException("MATERIAL-0032", durable.getKey().getDurableName());
				}
				if(durationUsed >= durationUsedLimit)
				{
					throw new CustomException("MATERIAL-0033", durable.getKey().getDurableName());
				}
				if(durable.getDurableState().equals(constMap.Dur_Scrapped))
				{
					throw new CustomException("MATERIAL-0031", durable.getKey().getDurableName());
				}

				if (durable.getUdfs().get("DURABLEHOLDSTATE").equals(GenericServiceProxy.getConstantMap().DURABLE_HOLDSTATE_Y))
					throw new CustomException("MATERIAL-9016", materialID);
				
				String palletExpirationDate = "";
				Date palletExpirationTime = null;
				
				if(durable.getUdfs().get("EXPIRATIONDATE") != null)
				{
					palletExpirationDate = durable.getUdfs().get("EXPIRATIONDATE").toString();
					
					try {
						palletExpirationTime = transFormat.parse(palletExpirationDate);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					
					int compare = currentDate.compareTo(palletExpirationTime);
					
					if(compare > 0)
					{
						throw new CustomException("MATERIAL-9998", durable.getKey().getDurableName(), palletExpirationDate);
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
				bindMap.put("DURABLENAME", materialID);
				bindMap.put("MACHINENAME", machineName.substring(0, 6));

				List<Map<String, Object>> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), bindMap);

				if (sqlResult.size() < 1)
					throw new CustomException("MATERIAL-0013");
				
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
					fpcList = MESDurableServiceProxy.getDurableServiceUtil().getFPCListByPalletJig(materialID);
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
							throw new CustomException("MATERIAL-9015", materialID);
						if (durationUsedLimit <= durationUsed)
							throw new CustomException("MATERIAL-9014", materialID);
						
						Date expirationTimeFPC = null;
					    String FPCExpirationDate = "";
					    
					    if(fpc.getUdfs().get("EXPIRATIONDATE") != null)
					    {
					    	FPCExpirationDate = fpc.getUdfs().get("EXPIRATIONDATE").toString();
					    	
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

						// materialLocation column = current machineName / machineName column = machineName / UnitName column = unitName / subUnitName column = subUnitName
						if (machineSpec.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().DetailMachineType_Main))
						{
							fpc.setMaterialLocationName(machineName); // input machinename
							setEventInfoFPC.getUdfs().put("MACHINENAME", machineName);

						}
						else if (machineSpec.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().DetailMachineType_Unit))
						{
							fpc.setMaterialLocationName(machineName); // input unitname
							setEventInfoFPC.getUdfs().put("MACHINENAME", machineSpec.getSuperMachineName());
							setEventInfoFPC.getUdfs().put("UNITNAME", machineName);

						}
						else if (machineSpec.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().DetailMachineType_SubUnit))
						{
							MachineSpec unitMachineSpec = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineSpec.getSuperMachineName());

							fpc.setMaterialLocationName(machineName); // input subunitname
							setEventInfoFPC.getUdfs().put("MACHINENAME", unitMachineSpec.getSuperMachineName());
							setEventInfoFPC.getUdfs().put("UNITNAME", machineSpec.getSuperMachineName());
						}
						else
						{ // subsubunit error

							throw new CustomException("MATERIAL-0014", machineName);
						}

						fpc.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);

						setEventInfoFPC.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_ONEQP);
						setEventInfoFPC.getUdfs().put("KITTIME", kitTime);

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
			else if(StringUtil.equals(durable.getDurableType(), "FilmCST") || StringUtil.equals(durable.getDurableType(), "PeelingCST") || StringUtil.equals(durable.getDurableType(), "FilmBox") || StringUtil.equals(durable.getDurableType(), "PeelingBox") )
			{
				SetEventInfo setEventInfoFilm = new SetEventInfo();
				double timeUsedLimit = durable.getTimeUsedLimit();
				double timeUsed = durable.getTimeUsed();
				double durationUsedLimit = durable.getDurationUsedLimit();
				double durationUsed = durable.getDurationUsed();
				
				if(timeUsed >= timeUsedLimit)
				{
					throw new CustomException("MATERIAL-0032", durable.getKey().getDurableName());
				}
				if(durationUsed >= durationUsedLimit)
				{
					throw new CustomException("MATERIAL-0033", durable.getKey().getDurableName());
				}
				if(durable.getDurableState().equals(constMap.Dur_Scrapped))
				{
					throw new CustomException("MATERIAL-0031", durable.getKey().getDurableName());
				}

				if (durable.getUdfs().get("DURABLEHOLDSTATE").equals(GenericServiceProxy.getConstantMap().DURABLE_HOLDSTATE_Y))
					throw new CustomException("MATERIAL-9016", materialID);
				
				// Check validation film in the Box
				List<Consumable> filmList = null;
				try
				{
					String condition = " WHERE CARRIERNAME = ?";

					Object[] bindSet = new Object[] { materialID };
					filmList = ConsumableServiceProxy.getConsumableService().select(condition, bindSet);
				}
				catch (NotFoundSignal nfs)
				{
					throw new CustomException("MATERIAL-0035");
				}

				if (filmList != null && filmList.size() > 0)
				{
					for (Consumable consumable : filmList)
					{
						String consumableExpirationDate = "";
						Date expirationTimeConsumable = null;
						
						if(consumable.getUdfs().get("EXPIRATIONDATE") != null)
						{
							consumableExpirationDate = consumable.getUdfs().get("EXPIRATIONDATE").toString();
							
							try {
								expirationTimeConsumable = transFormat.parse(consumableExpirationDate);
							} catch (ParseException e) {
								e.printStackTrace();
							}
							
							int compareConsumable = currentDate.compareTo(expirationTimeConsumable);
							
							if(compareConsumable > 0)
							{
								throw new CustomException("MATERIAL-9998", consumable.getKey().getConsumableName(), consumableExpirationDate);
							}
						}			
						
						// materialLocation column = current machineName / machineName column = machineName / UnitName column = unitName / subUnitName column = subUnitName
						if (machineSpec.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().DetailMachineType_Main))
						{
							consumable.setMaterialLocationName(machineName); // input machinename
							setEventInfoFilm.getUdfs().put("MACHINENAME", machineName);

						}

						consumable.setConsumableState(GenericServiceProxy.getConstantMap().Dur_InUse);

						setEventInfoFilm.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MaterialLocation_OnEQP);
						setEventInfoFilm.getUdfs().put("KITTIME", kitTime);

						// Set durableState, materialLocation
						ConsumableServiceProxy.getConsumableService().update(consumable);
						MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(consumable.getKey().getConsumableName(), setEventInfoFilm, eventInfo);
					}
				}
				else
				{
					throw new CustomException("MATERIAL-0015");
				}
			
			}
			else if(StringUtil.equals(durable.getDurableType(), GenericServiceProxy.getConstantMap().MaterialType_FPC))
			{	
				notAssignFlag = true;
				Durable fpc = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(materialID);
				
				kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfoFPC = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
				double timeUsedLimit = fpc.getTimeUsedLimit();
				double timeUsed = fpc.getTimeUsed();
				double durationUsedLimit = fpc.getDurationUsedLimit();
				double durationUsed = fpc.getDurationUsed();

				if (timeUsedLimit <= timeUsed)
					throw new CustomException("MATERIAL-9015", materialID);
				if (durationUsedLimit <= durationUsed)
					throw new CustomException("MATERIAL-9014", materialID);
				
				String FPCExpirationDate = "";
				Date expirationTimeFPC = null;
				
				if(fpc.getUdfs().get("EXPIRATIONDATE") != null)
				{
					FPCExpirationDate = fpc.getUdfs().get("EXPIRATIONDATE").toString();
				
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
				
				// materialLocation column = current machineName / machineName column = machineName / UnitName column = unitName / subUnitName column = subUnitName
				if (machineSpec.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().DetailMachineType_Main))
				{
					fpc.setMaterialLocationName(machineName); // input machinename
					setEventInfoFPC.getUdfs().put("MACHINENAME", machineName);

				}
				else if (machineSpec.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().DetailMachineType_Unit))
				{
					fpc.setMaterialLocationName(machineName); // input unitname
					setEventInfoFPC.getUdfs().put("MACHINENAME", machineSpec.getSuperMachineName());
					setEventInfoFPC.getUdfs().put("UNITNAME", machineName);

				}
				else if (machineSpec.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().DetailMachineType_SubUnit))
				{
					MachineSpec unitMachineSpec = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineSpec.getSuperMachineName());

					fpc.setMaterialLocationName(machineName); // input subunitname
					setEventInfoFPC.getUdfs().put("MACHINENAME", unitMachineSpec.getSuperMachineName());
					setEventInfoFPC.getUdfs().put("UNITNAME", machineSpec.getSuperMachineName());
				}
				else
				{ // subsubunit error

					throw new CustomException("MATERIAL-0014", machineName);
				}

				fpc.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);

				setEventInfoFPC.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_ONEQP);
				setEventInfoFPC.getUdfs().put("KITTIME", kitTime);

				// Set durableState, materialLocation
				DurableServiceProxy.getDurableService().update(fpc);
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(fpc, setEventInfoFPC, eventInfo);
			}
			
			if(!notAssignFlag)
			{
				if (machineSpec.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().DetailMachineType_Main))
				{
					durable.setMaterialLocationName(machineName); // input machinename
					setEventInfo.getUdfs().put("MACHINENAME", machineName);

				}
				else if (machineSpec.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().DetailMachineType_Unit))
				{
					durable.setMaterialLocationName(machineName); // input unitname
					setEventInfo.getUdfs().put("MACHINENAME", machineSpec.getSuperMachineName());
					setEventInfo.getUdfs().put("UNITNAME", machineName);

				}
				else if (machineSpec.getDetailMachineType().equals(GenericServiceProxy.getConstantMap().DetailMachineType_SubUnit))
				{
					MachineSpec unitMachineSpec = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineSpec.getSuperMachineName());

					durable.setMaterialLocationName(machineName); // input subunitname
					setEventInfo.getUdfs().put("MACHINENAME", unitMachineSpec.getSuperMachineName());
					setEventInfo.getUdfs().put("UNITNAME", machineSpec.getSuperMachineName());
				}
				else
				{ // subsubunit error
					throw new CustomException("MATERIAL-0014", machineName);
				}

				if(StringUtil.equals(durable.getDurableType(), "FilmCST") || StringUtil.equals(durable.getDurableType(), "PeelingCST") || StringUtil.equals(durable.getDurableType(), "FilmBox") || StringUtil.equals(durable.getDurableType(), "PeelingBox"))
				{
					setEventInfo.getUdfs().put("PORTNAME", portName);
				}
				
				durable.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);

				setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_ONEQP);
				setEventInfo.getUdfs().put("KITTIME", kitTime);

				// Set durableState, materialLocation
				DurableServiceProxy.getDurableService().update(durable);
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(durable, setEventInfo, eventInfo);
			}
		}
		return doc;
	}
}
