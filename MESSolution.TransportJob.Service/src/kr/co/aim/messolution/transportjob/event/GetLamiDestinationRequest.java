package kr.co.aim.messolution.transportjob.event;

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
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.MakeNotInUseInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class GetLamiDestinationRequest extends SyncHandler {

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
	 *         <MATERIALNAMEv
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
		String carrierName = "";
		String materialName = "";
		String transportJobName = "";
		String carrierType = "";
		String priority = "";
		String lotName = "";
		String carrierState = "";
		String destinationMachineName = "";
		String destinationPositionType = "";
		String destinationPositionName = "";
		String destinationZoneName = "";
		String transportJobType = GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_RTD;
		String jobState = GenericServiceProxy.getConstantMap().MCS_JOBSTATE_Requested;
		String transferState = GenericServiceProxy.getConstantMap().MCS_TRANSFERSTATE_RESERVED;
		Element bodyElement = SMessageUtil.getBodyElement(doc);
		Element returnElement = doc.getRootElement().getChild("Return");
		Map<String, Object> destinationResult = null;

		try
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "GetLamiDestinationReply");

			carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
			materialName = SMessageUtil.getBodyItemValue(doc, "MATERIALNAME", false);
			String currentMachineName = SMessageUtil.getBodyItemValue(doc, "CURRENTMACHINENAME", false);
			String currentPositionType = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONTYPE", false);
			String currentPositionName = SMessageUtil.getBodyItemValue(doc, "CURRENTPOSITIONNAME", false);
			String currentZoneName = SMessageUtil.getBodyItemValue(doc, "CURRENTZONENAME", false);

			MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(currentMachineName);
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
			String durableType = durableData.getDurableType();

			if (StringUtils.equals(machineSpecData.getMachineType(), GenericServiceProxy.getConstantMap().Mac_StorageMachine))
			{
				// CST Manual Port Input (3FRGV01_M01)
				// 1. Transfer to 3FSTK01 shelf
				// 2. Transfer to 3CTL0X_PXX (Lami EQP Port)
				if (StringUtils.equals(durableType, GenericServiceProxy.getConstantMap().DURABLETYPE_FilmBox))
				{
					if (StringUtils.isEmpty(materialName))
					{
						// 1. Transfer to 2FSTK01 shelf
						destinationResult = getDestinationStockerResult();
					}
					else
					{
						// Validation - Assign Info FilmBox and Film Material
						Consumable film = MESConsumableServiceProxy.getConsumableInfoUtil().getMaterialData(materialName);
						
						if (StringUtils.isEmpty(film.getUdfs().get("CARRIERNAME")))
						{
							EventInfo eventInfo = EventInfoUtil.makeEventInfo("AssignMaterialToCST", getEventUser(), getEventComment(), null, null);
							
							// Film Update
							SetEventInfo setEventInfoConsu = new SetEventInfo();
							setEventInfoConsu.getUdfs().put("CARRIERNAME", carrierName);
							MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(materialName, setEventInfoConsu, eventInfo);
							
							// FilmCST Update
							kr.co.aim.greentrack.durable.management.info.MakeInUseInfo makeInUseInfo = new kr.co.aim.greentrack.durable.management.info.MakeInUseInfo();
							MESDurableServiceProxy.getDurableServiceImpl().makeInUse(durableData, makeInUseInfo, eventInfo);
						}
						
						film = MESConsumableServiceProxy.getConsumableInfoUtil().getMaterialData(materialName);

						if (!StringUtils.equals(film.getUdfs().get("CARRIERNAME"), carrierName))
						{
							throw new CustomException("MATERIAL-0025", carrierName, materialName);
						}

						if (!CommonUtil.equalsIn(film.getConsumableType(), "TopLamination", "BottomLamination"))
						{
							throw new CustomException("MATERIAL-0026", film.getConsumableType(), materialName);
						}

						try
						{
							productQty = (int) film.getQuantity();
							lotName = materialName;
						}
						catch (Exception e)
						{
						}
					}
				}
				else if (StringUtils.equals(durableType, GenericServiceProxy.getConstantMap().DURABLETYPE_PeelingFilmBox))
				{
					if (StringUtils.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_InUse) && StringUtils.isNotEmpty(materialName))
					{
						// 1. Transfer to 3CTL0X_PXX (Lami EQP - PeelingFilmBox Port)
						destinationResult = getDestinationEQPResult(durableType);
					}

					if (destinationResult == null)
					{
						// 2. Transfer to 3FSTK01 shelf
						destinationResult = getDestinationStockerResult();
					}
				}
				else
				{
					throw new CustomException("DURABLE-0014", durableType);
				}
			}
			else
			{
				// 3CTL0X -> STK
				if (StringUtils.equals(durableType, GenericServiceProxy.getConstantMap().DURABLETYPE_FilmBox))
				{
					destinationResult = getDestinationStockerResult();
				}
				else if (StringUtils.equals(durableType, GenericServiceProxy.getConstantMap().DURABLETYPE_PeelingFilmBox))
				{
					if (StringUtils.equals(currentMachineName, "3CTD1"))
					{
						// Change DurableState to InUse
						EventInfo eventInfo = EventInfoUtil.makeEventInfo("MakeNotInUse", getEventUser(), getEventComment(), null, null);
						MakeNotInUseInfo makeNotInUseInfo = new MakeNotInUseInfo();
						MESDurableServiceProxy.getDurableServiceImpl().makeNotInUse(durableData, makeNotInUseInfo, eventInfo);
						
						// 1. Transfer to 3FSTK01 shelf
						destinationResult = getDestinationStockerResult();
					}
					else if (StringUtils.isEmpty(materialName))
					{
						// 1. Transfer to 3CDP01_P01
						destinationResult = getDestinationEQPResult_GarbagePeelingFilm();
					}

					if (destinationResult == null)
					{
						// 2. Transfer to 2FSTK01 shelf
						destinationResult = getDestinationStockerResult();
					}
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

			carrierType = MESTransportServiceProxy.getTransportJobServiceUtil().getCarrierType(durableData);

			// Set CarrierState
			if (StringUtils.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_InUse))
			{
				carrierState = GenericServiceProxy.getConstantMap().MCS_CARRIERSTATE_FULL;
			}
			else
			{
				carrierState = GenericServiceProxy.getConstantMap().MCS_CARRIERSTATE_EMPTY;
			}

			transportJobName = MESTransportServiceProxy.getTransportJobServiceUtil().generateTransportJobIdBySender(carrierName, GenericServiceProxy.getConstantMap().MCS_TRANSPORTJOBTYPE_RTD);

			Element newBodyElement = generateBodyTemplate(transportJobName, carrierName, materialName, carrierType, destinationMachineName,
					destinationPositionType, destinationPositionName, destinationZoneName, "50", lotName, carrierState, String.valueOf(productQty));

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
			Element newBodyElement = generateBodyTemplate(transportJobName, carrierName, materialName, carrierType, destinationMachineName,
					destinationPositionType, destinationPositionName, destinationZoneName, priority, lotName, carrierState, "0");

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

	@SuppressWarnings("unchecked")
	private Map<String, Object> getDestinationEQPResult(String durableType)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT P.MACHINENAME AS DESTINATIONMACHINENAME, ");
		sql.append("       'PORT' AS DESTINATIONPOSITIONTYPE, ");
		sql.append("       P.PORTNAME AS DESTINATIONPOSITIONNAME, ");
		sql.append("       NULL AS DESTINATIONZONENAME ");
		sql.append("  FROM PORT P, PORTSPEC PS ");
		sql.append(" WHERE 1 = 1 ");
		sql.append("   AND P.MACHINENAME = PS.MACHINENAME ");
		sql.append("   AND P.PORTNAME = PS.PORTNAME ");
		sql.append("   AND P.MACHINENAME IN ('3CTL01', '3CTL02', '3CTL03') ");
		sql.append("   AND PS.USEDURABLETYPE = :DURABLETYPE ");
		sql.append("   AND PS.PORTTYPE = 'PL' ");
		sql.append("   AND P.ACCESSMODE = 'Auto' ");
		sql.append("   AND P.TRANSFERSTATE = 'ReadyToLoad' ");
		sql.append("   AND P.PORTSTATENAME = 'UP' ");
		sql.append("ORDER BY P.LASTEVENTTIME ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("DURABLETYPE", durableType);

		List<Map<String, Object>> destinationResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (destinationResult.size() > 0)
		{
			eventLog.info("Transfer to Lamination EQP");
			return destinationResult.get(0);
		}
		else
		{
			eventLog.info("Not exist available Lami EQP Port");
			return null;
		}
	}

	private Map<String, Object> getDestinationStockerResult()
	{
		Map<String, Object> destinationResult = new HashMap<String, Object>();
		destinationResult.put("DESTINATIONMACHINENAME", "3FSTK01");
		destinationResult.put("DESTINATIONPOSITIONTYPE", "SHELF");
		destinationResult.put("DESTINATIONPOSITIONNAME", "");
		destinationResult.put("DESTINATIONZONENAME", "");

		eventLog.info("Transfer to 3FSTK01");
		return destinationResult;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> getDestinationEQPResult_GarbagePeelingFilm()
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT P.MACHINENAME AS DESTINATIONMACHINENAME, ");
		sql.append("       'PORT' AS DESTINATIONPOSITIONTYPE, ");
		sql.append("       P.PORTNAME AS DESTINATIONPOSITIONNAME, ");
		sql.append("       NULL AS DESTINATIONZONENAME, ");
		sql.append("       P.PORTSTATENAME, ");
		sql.append("       P.TRANSFERSTATE ");
		sql.append("  FROM PORT P, PORTSPEC PS ");
		sql.append(" WHERE 1 = 1 ");
		sql.append("   AND P.MACHINENAME = PS.MACHINENAME ");
		sql.append("   AND P.PORTNAME = PS.PORTNAME ");
		sql.append("   AND P.MACHINENAME = '3CTD01' ");
		sql.append("   AND PS.PORTTYPE = 'PL' ");

		Map<String, String> args = new HashMap<String, String>();

		List<Map<String, Object>> destinationResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (destinationResult.size() > 0)
		{
			String portStateName = ConvertUtil.getMapValueByName(destinationResult.get(0), "PORTSTATENAME");
			String transferState = ConvertUtil.getMapValueByName(destinationResult.get(0), "TRANSFERSTATE");

			if (!StringUtils.equals(portStateName, GenericServiceProxy.getConstantMap().Port_UP) || !StringUtils.equals(transferState, GenericServiceProxy.getConstantMap().Port_ReadyToLoad))
			{
				return null;
			}
			else
			{
				return destinationResult.get(0);
			}
		}
		else
		{
			return null;
		}
	}

	private Element generateBodyTemplate(String transportJobName, String carrierName,
			String materialName, String carrierType, String destinationMachineName, String destinationPositionType,
			String destinationPositionName, String destinationZoneName, String priority, String lotName,
			String carrierState, String productQuantity)
	{
		Element newBodyElement = new Element("Body");

		JdomUtils.addElement(newBodyElement, "TRANSPORTJOBNAME", transportJobName);
		JdomUtils.addElement(newBodyElement, "CARRIERNAME", carrierName);
		JdomUtils.addElement(newBodyElement, "MATERIALNAME", materialName);
		JdomUtils.addElement(newBodyElement, "CARRIERTYPE", carrierType);
		JdomUtils.addElement(newBodyElement, "DESTINATIONMACHINENAME", destinationMachineName);
		JdomUtils.addElement(newBodyElement, "DESTINATIONPOSITIONTYPE", destinationPositionType);
		JdomUtils.addElement(newBodyElement, "DESTINATIONPOSITIONNAME", destinationPositionName);
		JdomUtils.addElement(newBodyElement, "DESTINATIONZONENAME", destinationZoneName);
		JdomUtils.addElement(newBodyElement, "PRIORITY", priority);
		JdomUtils.addElement(newBodyElement, "LOTNAME", lotName);
		JdomUtils.addElement(newBodyElement, "CARRIERSTATE", carrierState);
		JdomUtils.addElement(newBodyElement, "PRODUCTQUANTITY", productQuantity);

		return newBodyElement;
	}
}
