package kr.co.aim.messolution.transportjob.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.management.data.Port;

import org.apache.commons.collections.OrderedMap;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class GetDestinationRequest extends SyncHandler {

	/**
	 * MessageSpec [MCS -> TEX]
	 * 
	 * <Body>
	 *    <CARRIERNAME />
	 *    <CURRENTMACHINENAME />
	 *    <CURRENTPOSITIONTYPE />
	 *    <CURRENTPOSITIONNAME />
	 *    <CURRENTZONENAME />
	 * </Body>
	 */
	
	/**
	 * MessageSpec [TEX -> MCS]
	 * 
	 * <Body>
	 *    <TRANSPORTJOBNAME />
	 *    <CARRIERNAME />
	 *    <CARRIERTYPE />
	 *    <DESTINATIONMACHINENAME />
	 *    <DESTINATIONPOSITIONTYPE />
	 *    <DESTINATIONPOSITIONNAME />
	 *    <DESTINATIONZONENAME />
	 *    <MATERIALLIST>
	 *       <MATERIAL>
	 *         <MATERIALNAME />
	 *         <QUANTITY />
	 *       </MATERIAL>
	 *    </MATERIALLIST>
	 *    <PRIORITY />
	 *    <CARRIERSTATE />
	 *    <CLEANSTATE />
	 *    <LOTNAME />
	 *    <PRODUCTQUANTITY />
	 * </Body>
	 */
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		int productQty = 0;
		String carrierName = StringUtils.EMPTY;
		String materialName = StringUtils.EMPTY;
		String transportJobName = StringUtils.EMPTY;
		String carrierType = StringUtils.EMPTY;
		String cleanState = StringUtils.EMPTY;
		String priority = StringUtils.EMPTY;
		String lotName = StringUtils.EMPTY;
		String carrierState = StringUtils.EMPTY;
		String destinationMachineName = StringUtils.EMPTY;
		String destinationPositionType = StringUtils.EMPTY;
		String destinationPositionName = StringUtils.EMPTY;
		String destinationZoneName = StringUtils.EMPTY;
		String transportJobType = GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_RTD;
		String jobState = GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Requested;
		String transferState = GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_RESERVED;
		Element bodyElement = SMessageUtil.getBodyElement(doc);
		Element materialListElement = new Element("MATERIALLIST");
		Element returnElement = doc.getRootElement().getChild("Return");
		Map<String, Object> destinationResult = null;

		try
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "GetDestinationReply");

			carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
			String currentMachineName = SMessageUtil.getBodyItemValue(doc, "CURRENTMACHINENAME", false);
			String currentPositionType = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONTYPE", false);
			String currentPositionName = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONNAME", false);
			String currentZoneName = SMessageUtil.getBodyItemValue(doc, "CURRENTZONENAME", false);
			
			MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(currentMachineName);
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
			String durableType = durableData.getDurableType();
			
			String sqlForSwitch = "SELECT DESCRIPTION FROM ENUMDEFVALUE "
					+ "WHERE ENUMNAME = :ENUMNAME AND ENUMVALUE = :ENUMVALUE";
		
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("ENUMNAME", "GetDestinationRequestSwitch");
			bindMap.put("ENUMVALUE", "Y");
			
			List<Map<String, Object>> sqlResult = 
					GenericServiceProxy.getSqlMesTemplate().queryForList(sqlForSwitch, bindMap);
			
			List<Durable> trayList = MESDurableServiceProxy.getDurableServiceUtil().getSubTrayListByCoverTray(carrierName,false);
			List<String> toMachineList = new ArrayList<String>();
			if (sqlResult != null && sqlResult.size()>0 && trayList!=null && trayList.size()>0)
			{
				List<Lot> lotDataList = MESLotServiceProxy.getLotServiceUtil().getLotListByTrayList(trayList, false);
				
				if (lotDataList !=null && lotDataList.size()>0)
				{
					Lot lotData = lotDataList.get(0);

					String sql = "SELECT DISTINCT PM.MACHINENAME FROM TPFOPOLICY TPFO, POSMACHINE PM "
							+ "WHERE TPFO.CONDITIONID = PM.CONDITIONID AND TPFO.FACTORYNAME = 'POSTCELL' "
							+ "AND TPFO.PRODUCTSPECNAME = :PRODUCTSPECNAME "
							+ "AND TPFO.PROCESSFLOWNAME = :PROCESSFLOWNAME "
							+ "AND TPFO.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME";
					
					Map<String, String> argsIdle = new HashMap<String, String>();
					argsIdle.put("PRODUCTSPECNAME", lotData.getProductSpecName());
					argsIdle.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
					argsIdle.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
					List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, argsIdle);
					if (result != null && result.size() > 0)
					{
						for (Map<String, Object> map : result)
						{
							toMachineList.add(map.get("MACHINENAME").toString());
						}
					}
				}
			}

			if(toMachineList.size() == 0)
				toMachineList.add(currentMachineName);
			
			if (StringUtils.equals(GenericServiceProxy.getConstantMap().Mac_StorageMachine, machineSpecData.getMachineType()))
			{
				if (StringUtils.equals(GenericServiceProxy.getConstantMap().DURABLETYPE_FilmBox, durableType))
				{
					if (StringUtils.contains(currentPositionName, "P01") || StringUtils.contains(currentPositionName, "P22"))
					{
						// Report Buffer Port : Stocker MGV Port to Stocker Shelf
						if (!StringUtils.equals(durableData.getDurableCleanState(), GenericServiceProxy.getConstantMap().Dur_Clean))
						{
							// Cassette[{0}] State is Dirty
							throw new CustomException("CST-0008", carrierName);
						}
						
						List<Consumable> filmDataList = MESConsumableServiceProxy.getConsumableInfoUtil().getFilmListByCarrierName(carrierName);
						
						if (filmDataList == null || filmDataList.size() == 0)
						{
							// This CST [{0}] is empty
							throw new CustomException("CARRIER-9007", carrierName);
						}
						
						boolean isZero = true;
						String emptyFilms = StringUtils.EMPTY;
						List<String> consumableSpecNameList = new ArrayList<String>(); 
						for (Consumable filmData : filmDataList) 
						{
							if (filmData.getQuantity() > 0)
							{
								isZero = false;
							}
							else
							{
								emptyFilms += filmData.getKey().getConsumableName() + ",";
							}
							
							if (!CommonUtil.equalsIn(filmData.getConsumableType(), "TopLamination", "BottomLamination"))
							{
								// Invalid MaterialType: {0}, {1}
								throw new CustomException("MATERIAL-0026", filmData.getConsumableType(), materialName);
							}
							
							if (consumableSpecNameList.indexOf(filmData.getConsumableSpecName()) < 0)
							{
								consumableSpecNameList.add(filmData.getConsumableSpecName());
							}
							
							Element materialElement = new Element("MATERIAL");
							materialElement.addContent(new Element("MATERIALNAME").setText(filmData.getKey().getConsumableName()));
							materialElement.addContent(new Element("QUANTITY").setText(String.valueOf(filmData.getQuantity())));
							
							materialListElement.addContent(materialElement);
						}
						
						if (isZero)
						{
							// Lami[{0}] QTY is 0
							throw new CustomException("MATERIAL-0017", emptyFilms.substring(0, emptyFilms.length() - 1));
						}
						
						if (consumableSpecNameList != null && consumableSpecNameList.size() > 1)
						{
							// There are several ConsumableSpecs in FilmBox
							throw new CustomException("FILM-0003");
						}
						
						this.checkMaterialBOM(consumableSpecNameList.get(0));

						productQty = filmDataList.size();
						
						// Stocker Load Port to Stocker Shelf
						destinationResult = getDestinationStockerShelf();
					}
					else 
					{
						// Report Stocker Port : Stocker Load Port to Stocker MGV Port
						List<Consumable> filmDataList = MESConsumableServiceProxy.getConsumableInfoUtil().getFilmListByCarrierName(carrierName);
						
						boolean isZero = true;
						if (filmDataList != null && filmDataList.size() > 0)
						{
							for (Consumable filmData : filmDataList) 
							{
								if (filmData.getQuantity() > 0)
								{
									isZero = false;
									break;
								}
							}
						}
											
						if (isZero)
						{
							// Empty : Move to Stocker Unload Port
							destinationResult = getDestinationStockerPort();
						}
						else
						{
							// Partial : Move to Stocker Shelf
							destinationResult = getDestinationStockerShelf();	
						}
					}
				}
				else if (StringUtils.equals(GenericServiceProxy.getConstantMap().DURABLETYPE_PeelingFilmBox, durableType))
				{
					if (StringUtils.contains(currentPositionName, "P01") || StringUtils.contains(currentPositionName, "P22"))
					{
						// Move to Stocker Shelf
						destinationResult = getDestinationStockerShelf();
					}
					else
					{
						// Move to Garbage Disposal
						destinationResult = getDestinationStockerDumpPort();						
					}
				}
				else if (StringUtils.equals("CoverTray", durableType))
				{
					// Stocker to EQP
					destinationResult = getDestinationStockerShelf_CoverTray(currentMachineName,toMachineList);
					
				}
				else if (CommonUtil.equalsIn(durableType, "MaskCST", "EVAMaskCST", "TFEMaskCST"))
				{
					// Stocker to EQP
					destinationResult = getDestinationMaskCST(durableData, currentMachineName, currentPositionName);
				}
				else
				{
					throw new CustomException("DURABLE-0014", durableType);
				}
			}
			else
			{
				if (StringUtils.equals("CoverTray", durableType))
				{
					// EQP to Stocker
					destinationResult = getDestinationStockerShelf_CoverTray(currentMachineName,toMachineList);
				}
				else
				{
					throw new CustomException("DURABLE-0014", durableType);
				}
			}
			
			if (destinationResult == null)
			{
				throw new CustomException("DSP-0003");
			}

			destinationMachineName = ConvertUtil.getMapValueByName(destinationResult, "DESTINATIONMACHINENAME");
			destinationPositionType = ConvertUtil.getMapValueByName(destinationResult, "DESTINATIONPOSITIONTYPE");
			destinationPositionName = ConvertUtil.getMapValueByName(destinationResult, "DESTINATIONPOSITIONNAME");
			destinationZoneName = ConvertUtil.getMapValueByName(destinationResult, "DESTINATIONZONENAME");
			//PostCell is null
			carrierType = MESTransportServiceProxy.getTransportJobServiceUtil().getCarrierType(durableData);
			carrierState = MESTransportServiceProxy.getTransportJobServiceUtil().getCarrierState(durableData);
			cleanState = MESTransportServiceProxy.getTransportJobServiceUtil().getCleanState(durableData);
			
			transportJobName = MESTransportServiceProxy.getTransportJobServiceUtil().generateTransportJobIdBySender(carrierName, GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_RTD);

			Element newBodyElement = generateBodyTemplate(transportJobName, carrierName, carrierType, 
														  destinationMachineName, destinationPositionType, destinationPositionName, destinationZoneName, 
														  "50", carrierState, cleanState, lotName, String.valueOf(productQty), materialListElement);
			
			bodyElement.detach();

			doc.getRootElement().addContent(newBodyElement);

			if (returnElement != null)
			{
				Element newReturnElement = (Element) returnElement.clone();
				returnElement.detach();

				doc.getRootElement().addContent(newReturnElement);
			}

			try
			{
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("TransportRequest", getEventUser(), getEventComment(), "", "");
				eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

				TransportJobCommand transportJobCommandInfo = new TransportJobCommand();
				transportJobCommandInfo.setTransportJobName(transportJobName);
				transportJobCommandInfo.setCarrierName(carrierName);
				transportJobCommandInfo.setTransportJobType(transportJobType);
				transportJobCommandInfo.setJobState(jobState);
				transportJobCommandInfo.setCancelState("");
				transportJobCommandInfo.setChangeState("");
				transportJobCommandInfo.setAlternateFlag("");
				transportJobCommandInfo.setTransferState(transferState);
				transportJobCommandInfo.setPriority(priority);
				transportJobCommandInfo.setSourceMachineName(currentMachineName);
				transportJobCommandInfo.setSourcePositionType(currentPositionType);
				transportJobCommandInfo.setSourcePositionName(currentPositionName);
				transportJobCommandInfo.setSourceZoneName(currentZoneName);
				transportJobCommandInfo.setDestinationMachineName(destinationMachineName);
				transportJobCommandInfo.setDestinationPositionType(destinationPositionType);
				transportJobCommandInfo.setDestinationPositionName(destinationPositionName);
				transportJobCommandInfo.setDestinationZoneName(destinationZoneName);
				transportJobCommandInfo.setCurrentMachineName(currentMachineName);
				transportJobCommandInfo.setCurrentPositionType(currentPositionType);
				transportJobCommandInfo.setCurrentPositionName(currentPositionName);
				transportJobCommandInfo.setCurrentZoneName(currentZoneName);
				transportJobCommandInfo.setCarrierState(carrierState);
				transportJobCommandInfo.setLotName(lotName);
				transportJobCommandInfo.setProductQuantity(productQty);
				transportJobCommandInfo = MESTransportServiceProxy.getTransportJobServiceUtil().setTransportJobCommandEventInfo(transportJobCommandInfo, eventInfo);
				transportJobCommandInfo.setLastEventResultCode(SMessageUtil.getReturnItemValue(doc, "RETURNCODE", false));
				transportJobCommandInfo.setLastEventResultText(SMessageUtil.getReturnItemValue(doc, "RETURNMESSAGE", false));

				ExtendedObjectProxy.getTransportJobCommand().create(eventInfo, transportJobCommandInfo);
			}
			catch (Exception e)
			{
				eventLog.info("Error Occured for Create TransportJob");
			}

			String replySubject = GenericServiceProxy.getESBServive().getSendSubject("MCS");
			GenericServiceProxy.getESBServive().sendBySender(replySubject, doc, "HIFSender");
		}
		catch (Exception e)
		{
			Element newBodyElement = generateBodyTemplate(transportJobName, carrierName, carrierType, 
					  									  destinationMachineName, destinationPositionType, destinationPositionName, destinationZoneName, 
					  									priority, carrierState, cleanState, lotName, "0", materialListElement);
			
			bodyElement.detach();

			doc.getRootElement().addContent(newBodyElement);

			if (returnElement != null)
			{
				Element newReturnElement = (Element) returnElement.clone();
				returnElement.detach();

				doc.getRootElement().addContent(newReturnElement);
			}

			String replySubject = GenericServiceProxy.getESBServive().getSendSubject("MCS");
			GenericServiceProxy.getESBServive().sendErrorBySender(replySubject, doc, "", e, "HIFSender");
		}

		return doc;
	}

	private Element generateBodyTemplate(String transportJobName, String carrierName, String carrierType, 
										 String destinationMachineName, String destinationPositionType, String destinationPositionName, String destinationZoneName,
										 String priority, String carrierState, String cleanState, String lotName, String productQuantity, Element materialListElement)
	{
		Element newBodyElement = new Element("Body");

		JdomUtils.addElement(newBodyElement, "TRANSPORTJOBNAME", transportJobName);
		JdomUtils.addElement(newBodyElement, "CARRIERNAME", carrierName);
		JdomUtils.addElement(newBodyElement, "CARRIERTYPE", carrierType);
		JdomUtils.addElement(newBodyElement, "DESTINATIONMACHINENAME", destinationMachineName);
		JdomUtils.addElement(newBodyElement, "DESTINATIONPOSITIONTYPE", destinationPositionType);
		JdomUtils.addElement(newBodyElement, "DESTINATIONPOSITIONNAME", destinationPositionName);
		JdomUtils.addElement(newBodyElement, "DESTINATIONZONENAME", destinationZoneName);		
		JdomUtils.addElement(newBodyElement, "PRIORITY", priority);
		JdomUtils.addElement(newBodyElement, "CARRIERSTATE", carrierState);
		JdomUtils.addElement(newBodyElement, "CLEANSTATE", cleanState);
		JdomUtils.addElement(newBodyElement, "LOTNAME", lotName);
		JdomUtils.addElement(newBodyElement, "PRODUCTQUANTITY", productQuantity);

		newBodyElement.addContent(materialListElement);
	
		return newBodyElement;
	}
	
	@SuppressWarnings("unchecked")
	private void checkMaterialBOM(String consumableSpecName) throws CustomException
	{
		String sql = "SELECT TP.FACTORYNAME, TP.PRODUCTSPECNAME, TP.PRODUCTSPECVERSION "
				   + "  FROM TPPOLICY TP, POSBOM PB "
				   + " WHERE 1 = 1 "
				   + "   AND PB.MATERIALSPECNAME = :MATERIALSPECNAME "
				   + "   AND PB.CONDITIONID = TP.CONDITIONID ";
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("MATERIALSPECNAME", consumableSpecName);
		
		List<Map<String, Object>> destinationResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		if (destinationResult == null || destinationResult.size() == 0)
		{
			// Check the BomData of MaterialSpec[0]
			throw new CustomException("FILM-0004", consumableSpecName);	
		}
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> getDestinationEQP(String durableType)
	{
		String sql = "SELECT P.MACHINENAME AS DESTINATIONMACHINENAME, "
				   + "    'PORT' AS DESTINATIONPOSITIONTYPE, "
				   + "    P.PORTNAME AS DESTINATIONPOSITIONNAME, "
				   + "    NULL AS DESTINATIONZONENAME "
				   + "FROM PORT P, PORTSPEC PS "
				   + "WHERE 1 = 1 "
				   + "  AND P.PORTTYPE = 'PL' "
				   + "  AND P.TRANSFERSTATE = 'ReadyToLoad' "
				   + "  AND P.ACCESSMODE = 'Auto' "
				   + "  AND P.PORTSTATENAME = 'UP' "
				   + "  AND PS.USEDURABLETYPE = :DURABLETYPE "
				   + "  AND PS.MACHINENAME = P.MACHINENAME "
				   + "  AND PS.PORTNAME = P.PORTNAME "
				   + "ORDER BY P.LASTEVENTTIME ";
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("DURABLETYPE", "CoverTray".equals(durableType) ? "Tray" : durableType);

		List<Map<String, Object>> destinationEQP = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		if (destinationEQP.size() > 0)
		{
			eventLog.info("Transfer to " + durableType + " EQP");
			return destinationEQP.get(0);
		}
		else
		{
			eventLog.info("Not exist available " + durableType + " EQP Port");
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getDestinationStockerShelf_CoverTray(String machineName, List<String> toMachineList)
	{
		//  NOT To Z03
		String sql = "SELECT SZ.MACHINENAME, "
				   + "       SZ.ZONENAME, "
				   + "       NVL(SZ.TOTALCAPACITY, 0) AS TOTALCAPACITY, "
				   + "       NVL(SZ.PROHIBITEDSHELFCOUNT, 0) AS PROHIBITEDSHELFCOUNT, "
				   + "       COUNT(*) AS COUNT, "
				   + "       COUNT(*) / (NVL(SZ.TOTALCAPACITY, 0) - NVL(SZ.PROHIBITEDSHELFCOUNT, 0)) AS RATIO, "
				   + "       CASE WHEN COUNT(*) / (NVL(SZ.TOTALCAPACITY, 0) - NVL(SZ.PROHIBITEDSHELFCOUNT, 0)) >= 1 THEN 'FULL' "
				   + "            ELSE 'NOTFULL' "
				   + "       END AS FULLSTATE "
				   + "FROM CT_STOCKERZONEINFO SZ, TMPOLICY TM,POSCONNECTEDSTOCKER PO,  "
				   + "( "
				   + "    SELECT MACHINENAME, ZONENAME "
				   + "    FROM DURABLE "
				   + "    WHERE TRANSPORTSTATE = 'INSTK' "
				   + ") D "
				   + "WHERE 1 = 1 "
				   + "  AND SZ.MACHINENAME IN(SELECT MACHINENAME FROM MACHINE WHERE FACTORYNAME = 'POSTCELL' AND MACHINEGROUPNAME = 'STK') "
				   + "  AND SZ.MACHINENAME NOT IN ('3PSTK01') "
				   + "  AND SZ.ZONENAME NOT LIKE '%Z03%' "
				   + "  AND D.MACHINENAME(+) = SZ.MACHINENAME  "
				   + "  AND SZ.MACHINENAME=PO.STOCKERNAME  "
				   + "  AND TM.CONDITIONID =PO.CONDITIONID  "
				   + "  AND TM.MACHINENAME IN (:MACHINELIST)  "
				   + "  AND D.ZONENAME(+) = SZ.ZONENAME "
				   + "GROUP BY SZ.MACHINENAME, SZ.ZONENAME, SZ.TOTALCAPACITY, SZ.PROHIBITEDSHELFCOUNT "
				   + "ORDER BY RATIO ";
		Map<String, Object> argsIdle = new HashMap<String, Object>();
		
		argsIdle.put("MACHINELIST", toMachineList);
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, argsIdle);
		if (result != null && result.size() > 0)
		{
			Map<String, Object> destinationResult = new HashMap<String, Object>();
			destinationResult.put("DESTINATIONMACHINENAME", result.get(0).get("MACHINENAME"));
			destinationResult.put("DESTINATIONPOSITIONTYPE", "SHELF");
			destinationResult.put("DESTINATIONPOSITIONNAME", "");
			destinationResult.put("DESTINATIONZONENAME", result.get(0).get("ZONENAME"));

			eventLog.info("MaskCST Stocker Zone");
			return destinationResult;
		}
		
		return null;
	}
	
	private Map<String, Object> getDestinationStockerShelf()
	{
		String stockerName = GenericServiceProxy.getConstantMap().Stocker_FilmBox;
		
		Map<String, Object> destinationResult = new HashMap<String, Object>();
		destinationResult.put("DESTINATIONMACHINENAME", stockerName);
		destinationResult.put("DESTINATIONPOSITIONTYPE", "SHELF");
		destinationResult.put("DESTINATIONPOSITIONNAME", "");
		destinationResult.put("DESTINATIONZONENAME", stockerName + "_Z01");

		eventLog.info("Transfer to " + stockerName + " Shelf");
		return destinationResult;
	}
	
	private Map<String, Object> getDestinationStockerPort() throws CustomException
	{
		// Check Port Down
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData("3FOCV01", "M01");
		if ("DOWN".equals(portData.getPortStateName()))
		{
			// Port[{0}/{1}] is DOWN
			throw new CustomException("PORT-3006", "3FOCV01", "M01");	
		}
		
		Map<String, Object> destinationResult = new HashMap<String, Object>();
		destinationResult.put("DESTINATIONMACHINENAME", "3FOCV01");
		destinationResult.put("DESTINATIONPOSITIONTYPE", "PORT");
		destinationResult.put("DESTINATIONPOSITIONNAME", "M01");
		destinationResult.put("DESTINATIONZONENAME", "");

		eventLog.info("Transfer to 3FOCV01 Port");
		return destinationResult;
	}
	
	private Map<String, Object> getDestinationStockerDumpPort() throws CustomException
	{
		// Check Port Down
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData("3FOCV01", "P01");
		if ("DOWN".equals(portData.getPortStateName()))
		{
			// Port[{0}/{1}] is DOWN
			throw new CustomException("PORT-3006", "3FOCV01", "P01");	
		}
		
		Map<String, Object> destinationResult = new HashMap<String, Object>();
		destinationResult.put("DESTINATIONMACHINENAME", "3FOCV01");
		destinationResult.put("DESTINATIONPOSITIONTYPE", "PORT");
		destinationResult.put("DESTINATIONPOSITIONNAME", "P01");
		destinationResult.put("DESTINATIONZONENAME", "");

		eventLog.info("Transfer to 3FOCV01 Dump Port");
		return destinationResult;
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, Object> getDestinationMaskCST(Durable durableData, String currentMachineName, String currentPositionName)
	{
		// If the Empty Mask CST is unloaded on port P01/P02 of the 3MCTK02
		if (StringUtils.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_Available) &&
			StringUtils.equals(currentMachineName, "3MCTK02") &&
			!StringUtils.equals(currentPositionName, "P03"))
		{
			Map<String, Object> destinationResult = new HashMap<String, Object>();
			destinationResult.put("DESTINATIONMACHINENAME", "3MCTK02");
			destinationResult.put("DESTINATIONPOSITIONTYPE", "PORT");
			destinationResult.put("DESTINATIONPOSITIONNAME", "P03");
			destinationResult.put("DESTINATIONZONENAME", "");
			
			eventLog.info("MaskCST Stocker P03");
			return destinationResult;			
		}
		
		// Check MaskTransfer
		String sql = "SELECT MACHINENAME, PORTNAME "
				   + "FROM "
				   + "( "
				   + "    SELECT DISTINCT RM.CARRIERNAME, RM.LASTEVENTTIME, J.TRANSPORTJOBNAME, "
				   + "           CASE WHEN P.TRANSFERSTATE = 'ReadyToLoad' THEN P.MACHINENAME ELSE BP.BUFFERMACHINENAME END AS MACHINENAME, "
				   + "           CASE WHEN P.TRANSFERSTATE = 'ReadyToLoad' THEN P.PORTNAME ELSE BP.BUFFERPORTNAME END AS PORTNAME, "
				   + "           CASE WHEN P.TRANSFERSTATE = 'ReadyToLoad' THEN 1 ELSE 2 END AS PRIORITY "
				   + "    FROM CT_RESERVEMASKTRANSFER RM, DURABLE D, PORT P, "
				   + "    ( "
				   + "        SELECT BP.MACHINENAME, BP.PORTNAME, BP.UNITNAME, "
				   + "               BP.BUFFERMACHINENAME, BP.BUFFERPORTNAME, P.PORTTYPE AS BUFFERPORTTYPE "
				   + "        FROM CT_CONNECTEDBUFFERPORT BP, PORT P "
				   + "        WHERE 1 = 1 "
				   + "          AND P.PORTTYPE IN('PL','PB') "
				   + "          AND P.RESOURCESTATE = 'InService' "
				   + "          AND P.ACCESSMODE = 'AUTO' "
				   + "          AND P.TRANSFERSTATE = 'ReadyToLoad' "
				   + "          AND P.MACHINENAME = BP.BUFFERMACHINENAME "
				   + "          AND P.PORTNAME = BP.BUFFERPORTNAME "
				   + "    ) BP, "
				   + "    ( "
				   + "        SELECT TRANSPORTJOBNAME, CARRIERNAME, DESTINATIONMACHINENAME, DESTINATIONPOSITIONNAME "
				   + "        FROM CT_TRANSPORTJOBCOMMAND "
				   + "        WHERE JOBSTATE IN('Requested','Accepted','Started') "
				   + "    ) J "
				   + "    WHERE 1 = 1 "
				   + "      AND P.PORTTYPE IN('PL','PB') "
				   + "      AND P.RESOURCESTATE = 'InService' "
				   + "      AND P.ACCESSMODE = 'AUTO' "
				   + "      AND P.MACHINENAME = RM.MACHINENAME "
				   + "      AND D.DURABLENAME = ? "
				   + "      AND D.DURABLESTATE = 'InUse' "
				   + "      AND D.DURABLETYPE IN('MaskCST','EVAMaskCST','TFEMaskCST') "
				   + "      AND NVL(D.DURABLEHOLDSTATE, 'N') != 'Y' "
				   + "      AND NVL(D.TRANSPORTLOCKFLAG, 'N') != 'Y' "
				   + "      AND NVL(D.CANCELINFOFLAG, 'N') != 'Y' "
				   + "      AND D.DURABLENAME = RM.CARRIERNAME "
				   + "      AND BP.MACHINENAME(+) = RM.MACHINENAME "
				   + "      AND J.DESTINATIONMACHINENAME(+) = RM.MACHINENAME "
				   + "      AND J.DESTINATIONPOSITIONNAME(+) = P.PORTNAME "
				   + "      AND J.CARRIERNAME(+) = D.DURABLENAME "
				   + ") "
				   + "WHERE 1 = 1 "
				   + "  AND MACHINENAME IS NOT NULL "
				   + "  AND PORTNAME IS NOT NULL "
				   + "  AND TRANSPORTJOBNAME IS NULL "
				   + "ORDER BY LASTEVENTTIME, PRIORITY, MACHINENAME, PORTNAME ";
		
		List<OrderedMap> resultDataList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, new Object[] { durableData.getKey().getDurableName() });
		if (resultDataList != null && resultDataList.size() > 0)
		{
			Map<String, Object> destinationResult = new HashMap<String, Object>();
			destinationResult.put("DESTINATIONMACHINENAME", resultDataList.get(0).get("MACHINENAME"));
			destinationResult.put("DESTINATIONPOSITIONTYPE", "PORT");
			destinationResult.put("DESTINATIONPOSITIONNAME", resultDataList.get(0).get("PORTNAME"));
			destinationResult.put("DESTINATIONZONENAME", "");

			eventLog.info("ReserveMaskTransfer");
			return destinationResult;
		}
		
		// MaskCST Stocker Shelf
		sql = "SELECT SZ.MACHINENAME, "
			+ "       SZ.ZONENAME, "
			+ "       NVL(SZ.TOTALCAPACITY, 0) AS TOTALCAPACITY, "
			+ "       NVL(SZ.PROHIBITEDSHELFCOUNT, 0) AS PROHIBITEDSHELFCOUNT, "
			+ "       COUNT(*) AS COUNT, "
			+ "       COUNT(*) / (NVL(SZ.TOTALCAPACITY, 0) - NVL(SZ.PROHIBITEDSHELFCOUNT, 0)) AS RATIO, "
			+ "       CASE WHEN COUNT(*) / (NVL(SZ.TOTALCAPACITY, 0) - NVL(SZ.PROHIBITEDSHELFCOUNT, 0)) = 1 THEN 'FULL' "
			+ "            ELSE 'NOTFULL' "
			+ "       END AS FULLSTATE "
			+ "FROM CT_STOCKERZONEINFO SZ, "
			+ "( "
			+ "    SELECT MACHINENAME, ZONENAME "
			+ "    FROM DURABLE "
			+ "    WHERE TRANSPORTSTATE = 'INSTK' "
			+ ") D "
			+ "WHERE 1 = 1 "
			+ "  AND SZ.MACHINENAME IN('3MCTK01','3MCTK02') "
			+ "  AND D.MACHINENAME(+) = SZ.MACHINENAME "
			+ "  AND D.ZONENAME(+) = SZ.ZONENAME "
			+ "GROUP BY SZ.MACHINENAME, SZ.ZONENAME, SZ.TOTALCAPACITY, SZ.PROHIBITEDSHELFCOUNT "
			+ "ORDER BY RATIO ";
		
		resultDataList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, new Object[] {});
		if (resultDataList != null && resultDataList.size() > 0)
		{
			Map<String, Object> destinationResult = new HashMap<String, Object>();
			destinationResult.put("DESTINATIONMACHINENAME", resultDataList.get(0).get("MACHINENAME"));
			destinationResult.put("DESTINATIONPOSITIONTYPE", "SHELF");
			destinationResult.put("DESTINATIONPOSITIONNAME", "");
			destinationResult.put("DESTINATIONZONENAME", resultDataList.get(0).get("ZONENAME"));

			eventLog.info("MaskCST Stocker Zone");
			return destinationResult;
		}

		return null;		
	}
}
