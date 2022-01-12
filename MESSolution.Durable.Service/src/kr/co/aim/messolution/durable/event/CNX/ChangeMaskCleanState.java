package kr.co.aim.messolution.durable.event.CNX;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.CleanInfo;
import kr.co.aim.greentrack.durable.management.info.DirtyInfo;
import kr.co.aim.greentrack.durable.management.info.RepairInfo;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class ChangeMaskCleanState extends SyncHandler {
	private static Log log = LogFactory.getLog(ChangeMaskCleanState.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String messageName = SMessageUtil.getMessageName(doc);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo(messageName, this.getEventUser(), this.getEventComment(), "", "");

		String sDurableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String sDurableCleanState = SMessageUtil.getBodyItemValue(doc, "DURABLECLEANSTATE", true);
		String origialSourceSubjectName=SMessageUtil.getHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", true);

		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		
		// getDurableData
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);

		if(!(durableData.getDurableState().equals(constMap.Dur_Available) || durableData.getDurableState().equals(constMap.Dur_UnMounted)))
		{
			throw new CustomException("MASK-0065", durableData.getDurableState(), sDurableName);
		}
		
		if (StringUtils.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_Scrapped))
			throw new CustomException("MASK-0012", messageName, durableData.getDurableState());
		
		durableData.setDurableCleanState(sDurableCleanState);
		
		if (StringUtils.equals(sDurableCleanState, GenericServiceProxy.getConstantMap().Dur_Dirty))
		{
			SetEventInfo setEventInfo = new SetEventInfo();
			DurableServiceProxy.getDurableService().update(durableData);
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);

			log.info("Excute  ChangeMaskCleanState : " + sDurableName + sDurableCleanState);
		}
		else if (StringUtils.equals(sDurableCleanState, GenericServiceProxy.getConstantMap().Dur_Clean))
		{
			Map<String, String > durUdfs = durableData.getUdfs();
			
			String cleanUsed = durUdfs.get("CLEANUSED").toString();
			int cleanCount = Integer.parseInt(cleanUsed) + 1;
			/*
			String cleanUsedLimit = durUdfs.get("CLEANUSEDLIMIT").toString();
			int cleanLimit = Integer.parseInt(cleanUsedLimit);
			
			if(cleanCount > cleanLimit)
			{
				throw new CustomException("MASK-0070", sDurableName);
			}
			*/
			cleanUsed = String.valueOf(cleanCount);
			
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("CLEANUSED", cleanUsed);
			setEventInfo.getUdfs().put("LASTCLEANTIME", eventInfo.getEventTime().toString());
			
			DurableServiceProxy.getDurableService().update(durableData);
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
			
			log.info("Excute  ChangeMaskCleanState : " + sDurableName + sDurableCleanState);
		}
		else 
		{
			log.info(String.format("Invalid Mask clean state [%s].",sDurableCleanState));
			
			//MASK-0105: Invalid Mask[{0}] clean state [{1}].
			throw new CustomException("MASK-0105",sDurableName,sDurableCleanState);
		}

		String lineName = DurableServiceProxy.getDurableService().selectByKey(durableData.getKey()).getUdfs().get("MASKSTOCKERNAME");

		if (lineName == null || lineName.isEmpty())
		{
			log.info(String.format("Mask [%s] Stocker LineName is empty.", sDurableName));
		}
		else
		{
			// Get Stocker machine info(Durable.MASKSTOCKERNAME = PhotoMaskStocker.LineName and is Main machine)
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(lineName);

			if (machineData.getCommunicationState().equals(GenericServiceProxy.getConstantMap().Mac_OnLineRemote))
			{
				String targetSubjectName = machineData.getUdfs().get("MCSUBJECTNAME");

				Durable maskData = DurableServiceProxy.getDurableService().selectByKey(durableData.getKey());
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
