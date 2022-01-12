package kr.co.aim.messolution.durable.event.IMS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.PhotoMaskStocker;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;

public class IMSMaskRoomStatusReport extends SyncHandler {

	Log log = LogFactory.getLog(this.getClass());
	
	// SLOTSTATUS [ IDLE | INUSE | DisableIn | DisableOut ] 
	// IDLE : Mask out from Slot , Stocker report mask ID , Change Mask transfer state to Moving
	// INUSE : Mask stock in Slot , Stocker report mask ID  , Change Mask transfer state to InStock
	// DisableIn: Block the mask from stock in the Stocker, Stocker slot is empty , Do not change mask transfer state
	// DisableOut: Block the mask from leaving the Stocker , Stocker report mask ID , Do not change mask transfer state
	
	@Override
	public Document doWorks(Document doc) throws CustomException
	{
		try
		{
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "IMSMaskRoomStatusReportReply");

			String lineName = SMessageUtil.getBodyItemValue(doc, "LINENAME", true);
			List<Element> stockerElementList = SMessageUtil.getBodySequenceItemList(doc, "STOCKERLIST", true);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("IMSMaskRoomStatusReport", this.getEventUser(), this.getEventComment());

			MESMachineServiceProxy.getMachineInfoUtil().getMachineData(lineName);
			List<PhotoMaskStocker> maskStockerDataList = ExtendedObjectProxy.getPhotoMaskStockerService().getDataInfoByLineName(lineName);

			// stockerSlotDataMap: Map<MaskStockerName,Map<SlotId,DataInfo>>
			Map<String, Map<String, PhotoMaskStocker>> stockerSlotDataMap = this.makeStockerSlotDataMap(maskStockerDataList);

			Map<Durable, DurableHistory> maskDataHistMap = new HashMap<>();
			List<PhotoMaskStocker> maskSlotDataList = new ArrayList<>();

			for (Element stockElement : stockerElementList)
			{
				String stockerName = stockElement.getChildText("MACHINENAME");
				List<Element> slotElementList = SMessageUtil.getSubSequenceItemList(stockElement, "SLOTLIST", true);

				for (Element slotElement : slotElementList)
				{
					String slotId = slotElement.getChildText("SLOTID");
					String slotStatus = slotElement.getChildText("SLOTSTATUS");
					String maskId = slotElement.getChildText("MASKID");

					eventInfo.setEventUser(slotElement.getChildText("USERID") != null ? slotElement.getChildText("USERID") : this.getEventUser());

					if (StringUtil.in(slotStatus, "IDLE", "DisableIn"))
					{
						PhotoMaskStocker dataInfo = this.getDataInfoFromStockerSlotDataMap(stockerName, slotId, stockerSlotDataMap);
						maskSlotDataList.add(this.makeSlotToEmpty(dataInfo, eventInfo, slotElement));

						if ("IDLE".equals(slotStatus) && maskId != null && !maskId.isEmpty())
						{
							Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskId);
							
							if(!dataInfo.getLineName().equals(durableData.getUdfs().get("MASKSTOCKERNAME")))continue;
							
							Durable oldData = (Durable) ObjectUtil.copyTo(durableData);
							durableData = this.makeMaskTransferToMoving(durableData, eventInfo);

							DurableHistory histData = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldData, durableData, new DurableHistory());
							maskDataHistMap.put(durableData, histData);
						}
						else
						{
							log.info(String.format("Pass Mask: StockerName [%s] , SlotId [%s] ,SlotState [%s] , MaskId [%s]", stockerName, slotId, slotStatus, maskId == null ? "" : maskId));
						}
					}
					else if (StringUtil.in(slotStatus, "INUSE", "DisableOut"))
					{
						PhotoMaskStocker dataInfo = this.getDataInfoFromStockerSlotDataMap(stockerName, slotId, stockerSlotDataMap);
						maskSlotDataList.add(this.makeSlotToUse(dataInfo, eventInfo, slotElement));
						
						Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskId);
						Durable oldData = (Durable) ObjectUtil.copyTo(durableData);
						durableData = this.makeMaskTransferToInStock(durableData, lineName, stockerName, eventInfo);
						
						DurableHistory histData = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldData, durableData, new DurableHistory());
						maskDataHistMap.put(durableData, histData);
					}
					else
					{
						log.info(String.format("Invalid slot [%s] status [%s].", slotId, slotStatus));

						// PHOTOMASK-001 : Invalid slot status.[Stocker = {0},Slot = {1} ,Status = {2}]
						throw new CustomException("PHOTOMASK-001", stockerName, slotId, slotStatus);
					}
				}
			}

			if (maskSlotDataList.size() > 0)
				ExtendedObjectProxy.getPhotoMaskStockerService().modify(maskSlotDataList);

			if (maskDataHistMap.size() > 0)
			{
				try
				{
					CommonUtil.executeBatch("update", new ArrayList<Durable>(maskDataHistMap.keySet()), true);
					CommonUtil.executeBatch("insert", new ArrayList<DurableHistory>(maskDataHistMap.values()), true);

					log.info(String.format("▶Successfully update %s pieces of Masks.", maskDataHistMap.size()));
				}
				catch (Exception e)
				{
					log.error(e.getMessage());
					throw new CustomException(e.getCause());
				}
			}
			
			this.setResultItemValue(doc,"OK","");
		}
		catch (CustomException ce)
		{
			setResultItemValue(doc, "NG", ce.errorDef.getLoc_errorMessage());
			throw ce;
		}
		catch (Exception ex)
		{
			setResultItemValue(doc, "NG", ex.getMessage());
			throw new CustomException(ex.getCause());
		}

		return doc;
	}
	
	private Document setResultItemValue(Document doc,String result, String resultDescription) throws CustomException
	{
		Element bodyElement = doc.getRootElement().getChild(SMessageUtil.Body_Tag);
		bodyElement.addContent(new Element("RESULT").setText(result));
		bodyElement.addContent(new Element("RESULTDESCRIPTION").setText(resultDescription));

		return doc;
	}
	
	private Map<String,Map<String,PhotoMaskStocker>> makeStockerSlotDataMap(List<PhotoMaskStocker> maskStockerDataList)
	{
	
		Map<String, Map<String, PhotoMaskStocker>> stockerSlotDataMap = new HashMap<>();

		for (PhotoMaskStocker maskstockerData : maskStockerDataList)
		{
			if (stockerSlotDataMap.containsKey(maskstockerData.getMaskStockerName()))
			{
				stockerSlotDataMap.get(maskstockerData.getMaskStockerName()).put(maskstockerData.getSlotId(), maskstockerData);
			}
			else
			{
				Map<String, PhotoMaskStocker> slotDataMap = new HashMap<>();
				slotDataMap.put(maskstockerData.getSlotId(), maskstockerData);

				stockerSlotDataMap.put(maskstockerData.getMaskStockerName(), slotDataMap);
			}
		}

		return stockerSlotDataMap;
	}
	
	private PhotoMaskStocker getDataInfoFromStockerSlotDataMap(String stockerName,String slotId,Map<String,Map<String,PhotoMaskStocker>> stockerSlotDataMap) throws CustomException
	{
		if (log.isInfoEnabled())
			log.info(String.format("getDataInfoFromStockerSlotDataMap : Input StockerName [%s] , SlotId [%s].", stockerName, slotId));

		PhotoMaskStocker dataInfo = null;
		Map<String, PhotoMaskStocker> slotDataMap = stockerSlotDataMap.get(stockerName);

		if (slotDataMap == null)
		{
			log.info("Fail Case 1: Stockername not found.");
			throw new CustomException("COMM-0010", "PhotoMaskStocker", String.format("StockerName = %s , SlotId = %s", stockerName, slotId));
		}
		else
		{
			dataInfo = slotDataMap.get(slotId);

			if (dataInfo == null)
			{
				log.info("Fail Case 2: SlotId not found.");
				throw new CustomException("COMM-0010", "PhotoMaskStocker", String.format("StockerName = %s , SlotId = %s", stockerName, slotId));
			}
			else
			{
				return dataInfo;
			}
		}
	}
	
	private PhotoMaskStocker makeSlotToEmpty(PhotoMaskStocker dataInfo,EventInfo eventInfo ,Element slotElement)
	{
		dataInfo.setMaskName("");
		dataInfo.setSlotStatus(slotElement.getChildText("SLOTSTATUS"));
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		
		return dataInfo;
	}
	
	private PhotoMaskStocker makeSlotToUse(PhotoMaskStocker dataInfo,EventInfo eventInfo ,Element slotElement)
	{
		dataInfo.setMaskName(slotElement.getChildText("MASKID"));
		dataInfo.setSlotStatus(slotElement.getChildText("SLOTSTATUS"));
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		
		return dataInfo;
	}
	
	private Durable makeMaskTransferToMoving(Durable durableData,EventInfo eventInfo)
	{
		durableData.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_MOVING);
		durableData.getUdfs().put("MACHINENAME", "");
		durableData.getUdfs().put("UNITNAME", "");
		durableData.setLastEventName(eventInfo.getEventName());
		durableData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		durableData.setLastEventTime(eventInfo.getEventTime());
		durableData.setLastEventUser(eventInfo.getEventUser());
		durableData.setLastEventComment(eventInfo.getEventComment());

		return durableData;
	}
	
	private Durable makeMaskTransferToInStock(Durable durableData,String lineName, String maskStockerName,EventInfo eventInfo)
	{
		durableData.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_INSTK);
		durableData.getUdfs().put("MACHINENAME", lineName);
		durableData.getUdfs().put("UNITNAME", maskStockerName);
		durableData.setLastEventName(eventInfo.getEventName());
		durableData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		durableData.setLastEventTime(eventInfo.getEventTime());
		durableData.setLastEventUser(eventInfo.getEventUser());
		durableData.setLastEventComment(eventInfo.getEventComment());

		return durableData;
	}
}
