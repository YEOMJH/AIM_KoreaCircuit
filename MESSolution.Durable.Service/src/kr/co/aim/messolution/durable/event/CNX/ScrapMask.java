package kr.co.aim.messolution.durable.event.CNX;

import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.MakeScrappedInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class ScrapMask extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> durableList = SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", false);

		String messageName = SMessageUtil.getMessageName(doc);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(messageName, getEventUser(), getEventComment(), "", "");
		String origialSourceSubjectName=SMessageUtil.getHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", true);

		for (Element eledur : durableList)
		{
			String sDurableName = SMessageUtil.getChildText(eledur, "DURABLENAME", true);
			String reasonCode = SMessageUtil.getChildText(eledur, "REASONCODE", true);
			String reasonCodeType = SMessageUtil.getChildText(eledur, "REASONCODETYPE", true);

			// getDurableData
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);

			if (StringUtils.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_Mounted))
				throw new CustomException("MASK-0058", durableData.getDurableState());
			else if (StringUtils.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_InUse))
				throw new CustomException("MASK-0026", sDurableName, durableData.getDurableState());
			else if (StringUtils.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_Scrapped))
				throw new CustomException("MASK-0012", messageName, durableData.getDurableState());
			
			durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
			DurableServiceProxy.getDurableService().update(durableData);

			eventInfo.setReasonCode(reasonCode);
			eventInfo.setReasonCodeType(reasonCodeType);

			MakeScrappedInfo makeScrappedInfo = MESDurableServiceProxy.getDurableInfoUtil().makeScrappedInfo(durableData);
			//makeScrappedInfo.getUdfs().put("TRANSPORTSTATE", "OutStock");
			
			// Execution
			MESDurableServiceProxy.getDurableServiceImpl().makeScrapped(durableData, makeScrappedInfo, eventInfo);
			
			String lineName = DurableServiceProxy.getDurableService().selectByKey(durableData.getKey()).getUdfs().get("MASKSTOCKERNAME");
			
			if (lineName == null || lineName.isEmpty())
			{
	            LogFactory.getLog(this.getClass()).info(String.format("Mask [%s] Stocker LineName is empty.",sDurableName));
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

		return doc;
	}

}
