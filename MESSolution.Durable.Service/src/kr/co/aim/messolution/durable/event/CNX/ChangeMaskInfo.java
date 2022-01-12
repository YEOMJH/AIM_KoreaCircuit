package kr.co.aim.messolution.durable.event.CNX;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineKey;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;

import java.util.ArrayList;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class ChangeMaskInfo extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String sMaskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String sMaskPosition = SMessageUtil.getBodyItemValue(doc, "MASKPOSITION", false);
		String sPosition = SMessageUtil.getBodyItemValue(doc, "POSITION", false);
		String sTimeUsed = SMessageUtil.getBodyItemValue(doc, "TIMEUSED", false);
		String sCleanUsed = SMessageUtil.getBodyItemValue(doc, "CLEANUSED", false);
		String sMaskProcessRecipe = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);
		String sMaskReworkRecipe = SMessageUtil.getBodyItemValue(doc, "MASKNGRECIPENAME", false);
		String sTransportState = SMessageUtil.getBodyItemValue(doc, "TRANSPORTSTATE", false);
		String sTimeUsedLimit = SMessageUtil.getBodyItemValue(doc, "TIMEUSEDLIMIT", false);
		String sCleanUsedLimit = SMessageUtil.getBodyItemValue(doc, "CLEANUSEDLIMIT", false);
		String sMaskHoldState = SMessageUtil.getBodyItemValue(doc, "DURABLEHOLDSTATE", false);
		String sDurableState = SMessageUtil.getBodyItemValue(doc, "DURABLESTATE", false);
		String sReasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);
		String sDurationUsedLimit = SMessageUtil.getBodyItemValue(doc, "DURATIONUSEDLIMIT", false);
		String maskStockerName = SMessageUtil.getBodyItemValue(doc, "MASKSTOCKERNAME", false);
		String origialSourceSubjectName=SMessageUtil.getHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Change", this.getEventUser(), this.getEventComment(), "", "");

		// getDurableData
		Durable durMaskData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sMaskName);

		if (StringUtils.equals(durMaskData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_Mounted))
			throw new CustomException("MASK-0058", GenericServiceProxy.getConstantMap().Dur_Mounted);
		else if (StringUtils.equals(durMaskData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_InUse))
			throw new CustomException("MASK-0026", sMaskName, durMaskData.getDurableState());
		else if (StringUtils.equals(durMaskData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_Scrapped))
			throw new CustomException("MASK-0012", sMaskName, durMaskData.getDurableState());

		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("MASKPOSITION", sMaskPosition);
		setEventInfo.getUdfs().put("CLEANUSED", sCleanUsed);
		setEventInfo.getUdfs().put("POSITION", sPosition);
		setEventInfo.getUdfs().put("MACHINERECIPENAME", sMaskProcessRecipe);
		setEventInfo.getUdfs().put("MASKNGRECIPENAME", sMaskReworkRecipe);
		setEventInfo.getUdfs().put("TRANSPORTSTATE", sTransportState);
		setEventInfo.getUdfs().put("CLEANUSEDLIMIT", sCleanUsedLimit);
		setEventInfo.getUdfs().put("DURABLEHOLDSTATE", sMaskHoldState);
		setEventInfo.getUdfs().put("MASKSTOCKERNAME", maskStockerName);

		if (StringUtil.equals("Y", sMaskHoldState))
			eventInfo.setReasonCodeType("HOLD");
		eventInfo.setReasonCode(sReasonCode);
		
		if(StringUtil.isNotEmpty(sTimeUsed))
		{
			durMaskData.setTimeUsed(Double.parseDouble(sTimeUsed));
		}
		if(StringUtil.isNotEmpty(sTimeUsedLimit))
		{
			durMaskData.setTimeUsedLimit(Double.parseDouble(sTimeUsedLimit));
		}
		if(StringUtil.isNotEmpty(sDurationUsedLimit))
		{
			durMaskData.setDurationUsedLimit(Double.parseDouble(sDurationUsedLimit));
		}
		durMaskData.setDurableState(sDurableState);
		durMaskData.setReasonCode(sReasonCode);
		

		DurableServiceProxy.getDurableService().update(durMaskData);
		
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durMaskData, setEventInfo, eventInfo);
		
		String lineName = DurableServiceProxy.getDurableService().selectByKey(durMaskData.getKey()).getUdfs().get("MASKSTOCKERNAME");
		
		if (lineName == null || lineName.isEmpty())
		{
            LogFactory.getLog(this.getClass()).info(String.format("Mask [%s] Stocker LineName is empty.",sMaskName));
		}
		else
		{
			// Get Stocker machine info(Durable.MASKSTOCKERNAME = PhotoMaskStocker.LineName and is Main machine)
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(lineName);

			if (machineData.getCommunicationState().equals(GenericServiceProxy.getConstantMap().Mac_OnLineRemote))
			{
				String targetSubjectName = machineData.getUdfs().get("MCSUBJECTNAME");

				Durable maskData = DurableServiceProxy.getDurableService().selectByKey(durMaskData.getKey());
				Document messageDoc = MESDurableServiceProxy.getDurableServiceUtil().generateIMSMaskChangeInfo(origialSourceSubjectName,machineData, maskData, eventInfo);

				try
				{
					GenericServiceProxy.getESBServive().sendBySenderWithoutChangeReturnElement(targetSubjectName, messageDoc, "EISSender");
					GenericServiceProxy.getMessageTraceService().recordMessageLog(messageDoc, GenericServiceProxy.getConstantMap().INSERT_LOG_TYPE_SEND);
				}
				catch (Exception e)
				{
					//SYSTEM-0001: IMSMessage send fail
					throw new CustomException("SYSTEM-0001");
				}
			}
		}
		
		return doc;
	}
}
