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

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class ChangeMaskDirty extends SyncHandler {
	private static Log log = LogFactory.getLog(ChangeMaskDirty.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String messageName = SMessageUtil.getMessageName(doc);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo(messageName, this.getEventUser(), this.getEventComment(), "", "");

		List<Element> maskList = SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", true);
		String origialSourceSubjectName=SMessageUtil.getHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", true);
		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		
		for(Element maskName : maskList)
		{
			String sDurableName = SMessageUtil.getChildText(maskName, "DURABLENAME", true);
			
			// getDurableData
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);
			
			if(!(durableData.getDurableState().equals(constMap.Dur_Available) || durableData.getDurableState().equals(constMap.Dur_UnMounted)))
			{
				throw new CustomException("MASK-0065", durableData.getDurableState(), sDurableName);
			}
			
			if (StringUtils.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_Scrapped))
				throw new CustomException("MASK-0012", messageName, durableData.getDurableState());
			
			durableData.setDurableCleanState(GenericServiceProxy.getConstantMap().Dur_Dirty);
			
			SetEventInfo setEventInfo = new SetEventInfo();
			DurableServiceProxy.getDurableService().update(durableData);
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
			
			this.sendMessageToStocker(durableData,eventInfo,origialSourceSubjectName);
		}
		
		return doc;
	}
	
	private void sendMessageToStocker(Durable durableData, EventInfo eventInfo,String origialSourceSubjectName) throws CustomException
	{
		String lineName = DurableServiceProxy.getDurableService().selectByKey(durableData.getKey()).getUdfs().get("MASKSTOCKERNAME");

		if (lineName == null || lineName.isEmpty())
		{
			log.info(String.format("Mask [%s] Stocker LineName is empty.", durableData.getKey().getDurableName()));
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
	}
}
