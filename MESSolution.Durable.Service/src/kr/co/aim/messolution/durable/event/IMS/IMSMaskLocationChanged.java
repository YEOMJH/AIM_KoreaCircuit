package kr.co.aim.messolution.durable.event.IMS;

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

public class IMSMaskLocationChanged extends SyncHandler 
{
	Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		try
		{
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "IMSMaskLocationChangeReply");
			
			String lineName = SMessageUtil.getBodyItemValue(doc, "LINENAME", true);
			String stockerName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String slotId = SMessageUtil.getBodyItemValue(doc, "SLOTID", true);
			String slotStatus = SMessageUtil.getBodyItemValue(doc, "SLOTSTATUS", true);
			String maskId = SMessageUtil.getBodyItemValue(doc, "MASKID", false);
			String userId = SMessageUtil.getBodyItemValue(doc, "USERID", false);

			MESMachineServiceProxy.getMachineInfoUtil().getMachineData(lineName);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("IMSMaskLocationChanged", userId == null ? this.getEventUser():userId, this.getEventComment());
			PhotoMaskStocker dataInfo = ExtendedObjectProxy.getPhotoMaskStockerService().getPhotoMaskStockerData(stockerName, slotId);

			if (StringUtil.in(slotStatus, "IDLE", "DisableIn"))
			{
				dataInfo.setMaskName("");
				dataInfo.setSlotStatus(slotStatus);
				
				ExtendedObjectProxy.getPhotoMaskStockerService().modify(eventInfo, dataInfo);
                
			    StringBuffer sql=new StringBuffer();
				sql.append(" SELECT DURABLENAME FROM DURABLE WHERE DURABLETYPE='PhotoMask' ");
				sql.append(" AND MACHINENAME=:MACHINENAME AND UNITNAME=:UNITNAME ");
				sql.append(" AND RETICLESLOT=:RETICLESLOT ");
				Map<String, String> bindMap = new HashMap<String, String>();
		        bindMap.put("MACHINENAME", lineName);
			    bindMap.put("UNITNAME", stockerName);
				bindMap.put("RETICLESLOT", slotId);
			    List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
					
			    if(result.size()>0)
			    {
			    	String photoMaskID=result.get(0).get("DURABLENAME").toString();
			    	Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(photoMaskID);
					Durable oldData = (Durable) ObjectUtil.copyTo(durableData);
					
					durableData.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_MOVING);
					durableData.getUdfs().put("MACHINENAME", "");
					durableData.getUdfs().put("UNITNAME", "");
					durableData.getUdfs().put("RETICLESLOT", "");
					durableData.setDurableState("UnMounted");
					durableData.setLastEventName(eventInfo.getEventName());
					durableData.setLastEventTimeKey(eventInfo.getEventTimeKey());
					durableData.setLastEventTime(eventInfo.getEventTime());
					durableData.setLastEventUser(eventInfo.getEventUser());
					durableData.setLastEventComment(eventInfo.getEventComment());

					DurableHistory histData = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldData, durableData, new DurableHistory());
					
					DurableServiceProxy.getDurableService().update(durableData);
					DurableServiceProxy.getDurableHistoryService().insert(histData);
			    	
			    }
			    this.setResultItemValue(doc, "OK", "");
			}
			else if (StringUtil.in(slotStatus, "INUSE", "DisableOut"))
			{
				dataInfo.setMaskName(maskId);
				dataInfo.setSlotStatus(slotStatus);

				ExtendedObjectProxy.getPhotoMaskStockerService().modify(eventInfo, dataInfo);
				try
				{
					Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskId);
					Durable oldData = (Durable) ObjectUtil.copyTo(durableData);
					durableData.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_INSTK);
					durableData.getUdfs().put("MACHINENAME", lineName);
					durableData.getUdfs().put("UNITNAME", stockerName);
					durableData.getUdfs().put("RETICLESLOT", slotId);
					durableData.setDurableState("UnMounted");
					durableData.setLastEventName(eventInfo.getEventName());
					durableData.setLastEventTimeKey(eventInfo.getEventTimeKey());
					durableData.setLastEventTime(eventInfo.getEventTime());
					durableData.setLastEventUser(eventInfo.getEventUser());
					durableData.setLastEventComment(eventInfo.getEventComment());
					
					DurableHistory histData = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldData, durableData, new DurableHistory());
					
					DurableServiceProxy.getDurableService().update(durableData);
					DurableServiceProxy.getDurableHistoryService().insert(histData);					
				}
				catch(CustomException e)
				{
					log.info(String.format("Invalid slot [%s] status [%s].", slotId, slotStatus));
					setResultItemValue(doc, "NG", "Not regiest  PhotoMask:["+maskId+"]");
				}
				
			}
			else
			{
				log.info(String.format("Invalid slot [%s] status [%s].", slotId, slotStatus));

				// PHOTOMASK-001 : Invalid slot status.[Stocker = {0},Slot = {1} ,Status = {2}]
				throw new CustomException("PHOTOMASK-001", stockerName, slotId, slotStatus);
			}

			
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
}
