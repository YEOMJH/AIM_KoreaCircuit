package kr.co.aim.messolution.durable.event.CNX;

import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class UnscrapMask extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> durableList = SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", false);

		String messageName = SMessageUtil.getMessageName(doc);
		String origialSourceSubjectName=SMessageUtil.getHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", true);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(messageName, this.getEventUser(), this.getEventComment(), "", "");

		for (Element eledur : durableList)
		{
			String sDurableName = SMessageUtil.getChildText(eledur, "DURABLENAME", true);
			String sDurableType = SMessageUtil.getChildText(eledur, "DURABLETYPE", true);

			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);

			if (!StringUtils.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_Scrapped))
			{
				throw new CustomException("MASK-0058", durableData.getKey().getDurableName());
			}
			
			if (StringUtil.equals(sDurableType, "PhotoMask"))
			{
				MESDurableServiceProxy.getDurableServiceImpl().makePhotoMaskUnScrap(durableData, eventInfo);
				durableData = DurableServiceProxy.getDurableService().selectByKey(durableData.getKey());
				
				this.sendMessageToStocker(durableData, eventInfo,origialSourceSubjectName);
			}
			else
			{
				MESDurableServiceProxy.getDurableServiceImpl().makeUnScrap(durableData, eventInfo);
			}
		}

		return doc;
	}

	private void sendMessageToStocker(Durable durableData,EventInfo eventInfo,String origialSourceSubjectName) throws CustomException
	{
		String lineName = DurableServiceProxy.getDurableService().selectByKey(durableData.getKey()).getUdfs().get("MASKSTOCKERNAME");

		if (lineName == null || lineName.isEmpty())
		{
			LogFactory.getLog(this.getClass()).info(String.format("Mask [%s] Stocker LineName is empty.", durableData.getKey().getDurableName()));
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
