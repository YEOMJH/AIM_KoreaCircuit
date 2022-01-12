package kr.co.aim.messolution.consumable.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableHistory;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;
import org.jdom.Element;

public class ChangeOrganicPRState extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		List<Element> eMaterialList = SMessageUtil.getBodySequenceItemList(doc, "MATERIALLIST", true);
		String detailState = SMessageUtil.getBodyItemValue(doc, "DETAILSTATE", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String freezeTimeLimit = SMessageUtil.getBodyItemValue(doc, "FREEZETIMELIMIT", true);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(detailState, getEventUser(), getEventComment(), null, null);

		for (Element materialE : eMaterialList)
		{
			String materialID = materialE.getChildText("MATERIALNAME");
			
			Consumable consumable = MESConsumableServiceProxy.getConsumableInfoUtil().getMaterialData(materialID);
			
			if(consumable.getUdfs().get("DETAILCONSUMABLESTATE").equals(detailState))
			{
				throw new CustomException("MATERIAL-0029");
			}
			
			if(detailState.equals("Thaw") && consumable.getUdfs().get("FREEZETIME")!=null)
			{
				String freezeTimeString = consumable.getUdfs().get("FREEZETIME").toString();
				SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String currentTime = transFormat.format(new Date());
				Date freezeTime = null;
				Date currentDate = null;
				try {
					freezeTime = transFormat.parse(freezeTimeString);
					currentDate = transFormat.parse(currentTime);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				double gap = (double)(currentDate.getTime() - freezeTime.getTime()) / (double)(60 * 60 * 1000);
				
				if (gap >= Double.parseDouble(freezeTimeLimit)) 
				{
					throw new CustomException("MATERIAL-0030", materialID);
				}
			}
			
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("DETAILCONSUMABLESTATE", detailState);
			
			if(detailState.equals("Freeze"))
			{
				setEventInfo.getUdfs().put("FREEZETIME", eventInfo.getEventTime().toString());
			}
			else
			{
				setEventInfo.getUdfs().put("THAWTIME", eventInfo.getEventTime().toString());
			}
			MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(materialID, setEventInfo, eventInfo);
		}

		return doc;
	}
}
