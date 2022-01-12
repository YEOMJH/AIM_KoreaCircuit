package kr.co.aim.messolution.consumable.event;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.CrucibleLot;
import kr.co.aim.messolution.extended.object.management.data.Organic;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.info.CreateInfo;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class AssignFPCtoPalletJig extends SyncHandler {

	private static Log log = LogFactory.getLog(AssignFPCtoPalletJig.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		
		String palletJigName = SMessageUtil.getBodyItemValue(doc, "PALLETNAME", true);
		List<Element> FPCList = SMessageUtil.getBodySequenceItemList(doc, "FPCLIST", false);

		Durable palletJigData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(palletJigName);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("AssignFPCtoPalletJig", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		if(palletJigData.getCapacity() < palletJigData.getLotQuantity() + FPCList.size())
		{
			throw new CustomException("MATERIAL-9007", palletJigName, palletJigData.getLotQuantity());
		}
		if(palletJigData.getTimeUsed() >= palletJigData.getTimeUsedLimit())
		{
			throw new CustomException("MATERIAL-0032", palletJigName);
		}
		if(palletJigData.getDurableState().equals(constMap.Dur_Scrapped))
		{
			throw new CustomException("MATERIAL-0031", palletJigName);
		}
		
		SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentTime = transFormat.format(new Date());
		Date currentDate = null;
		try {
			currentDate = transFormat.parse(currentTime);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		String palletExpirationDate = "";
		
		if(palletJigData.getUdfs().get("EXPIRATIONDATE") != null)
		{
			palletExpirationDate = palletJigData.getUdfs().get("EXPIRATIONDATE").toString();
			Date expirationTime = null;
			
			try {
				expirationTime = transFormat.parse(palletExpirationDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			int compare = currentDate.compareTo(expirationTime);
			
			if(compare > 0)
			{
				throw new CustomException("MATERIAL-9998", palletJigName, palletExpirationDate);
			}
		}
		
		String firstFPCSpec = "";
		
		if(palletJigData.getLotQuantity() > 0)
		{
			List<Durable> palletFPCList = MESDurableServiceProxy.getDurableServiceUtil().getFPCListByPalletJig(palletJigName);
			
			firstFPCSpec = palletFPCList.get(0).getDurableSpecName();
		}
		else
		{
			Durable firstFPCData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(FPCList.get(0).getChildText("DURABLENAME"));
			firstFPCSpec = firstFPCData.getDurableSpecName();
		}
		
		for(Element eleFPC : FPCList)
		{
			String FPCName = eleFPC.getChildText("DURABLENAME");
			String position = eleFPC.getChildText("POSITION");
			Durable FPCInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(FPCName);//2020/11/28 caixu FPC AssignQutiy 
			
			if(FPCInfo.getTimeUsed() >= FPCInfo.getTimeUsedLimit())
			{
				throw new CustomException("MATERIAL-0032", FPCName);
			}
			
			if(FPCInfo.getDurableState().equals(constMap.Dur_Scrapped))
			{
				throw new CustomException("MATERIAL-0031", FPCName);
			}
			
			if(!FPCInfo.getDurableSpecName().equals(firstFPCSpec))
			{
				throw new CustomException("MATERIAL-0034", FPCName);
			}
			
			String FPCExpirationDate = "";
			
			if(FPCInfo.getUdfs().get("EXPIRATIONDATE") != null)
			{
				FPCExpirationDate = FPCInfo.getUdfs().get("EXPIRATIONDATE").toString();
				Date expirationTimeFPC = null;
				
				try {
					expirationTimeFPC = transFormat.parse(FPCExpirationDate);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				
				int compareFPC = currentDate.compareTo(expirationTimeFPC);
				
				if(compareFPC > 0)
				{
					throw new CustomException("MATERIAL-9998", FPCName, FPCExpirationDate);
				}
			}
			
			kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
			
			setEventInfo.getUdfs().put("PALLETNAME", palletJigName);
			setEventInfo.getUdfs().put("POSITION", position);
			
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(FPCInfo, setEventInfo, eventInfo); 
		}
		
		palletJigData.setLotQuantity(palletJigData.getLotQuantity() + FPCList.size());

		kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();

		// Set durableState, materialLocation
		DurableServiceProxy.getDurableService().update(palletJigData);
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(palletJigData, setEventInfo, eventInfo);

		return doc;
	}
}
