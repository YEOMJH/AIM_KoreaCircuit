package kr.co.aim.messolution.lot.event.CNX.PostCell;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.FirstOnlineProduct;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;


import kr.co.aim.messolution.extended.object.management.impl.LotQueueTimeService;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;

public class ChangeFirstOnlineFlag  extends SyncHandler{
	
	public static Log logger = LogFactory.getLog(LotQueueTimeService.class);
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		Element eleBody = SMessageUtil.getBodyElement(doc);
		
		if (eleBody != null)
		{
			
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("changeFirstOnlineFlag", getEventUser(), getEventComment(), "", "");

			for (Element eleLot : SMessageUtil.getBodySequenceItemList(doc, "PANELLIST", false))
			{
					
				String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
				String firstOnlineFlag = SMessageUtil.getChildText(eleLot, "FIRSTGLASSFLAG", false);
					
				
				Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
			
				CommonValidation.checkLotProcessStateWait(lotData);
				if (firstOnlineFlag.equalsIgnoreCase("Y")&&(lotData.getUdfs().get("FIRSTGLASSFLAG")).equals("Y"))
				{
					throw new CustomException("LOT-0996", lotData.getKey().getLotName()); 
				}
			
			
				lotData.setLastEventComment(eventInfo.getEventComment());
				lotData.setLastEventName(eventInfo.getEventName());
				lotData.setLastEventTime(TimeStampUtil.getTimestamp(TimeUtils.getCurrentTime(TimeUtils.FORMAT_DEFAULT)));
				lotData.setLastEventUser(eventInfo.getEventUser());
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.getUdfs().put("FIRSTGLASSFLAG", firstOnlineFlag);
				
				LotServiceProxy.getLotService().update(lotData);
				LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
				if(firstOnlineFlag.equals("Y"))
				{
						Lot newLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
						
						String flag ="N";
						FirstOnlineProduct firstOnlineProduct =new FirstOnlineProduct();
						firstOnlineProduct.setLotName(lotName);
						firstOnlineProduct.setProductSpecName(newLotData.getProductSpecName());
						firstOnlineProduct.setMuraFlag(flag);
						firstOnlineProduct.setLineFlag(flag);
						firstOnlineProduct.setSurfaceFlag(flag);
						
						
						firstOnlineProduct.setLastEventComment(eventInfo.getEventComment());
						firstOnlineProduct.setLastEventName(eventInfo.getEventName());
						firstOnlineProduct.setLastEventTime(TimeStampUtil.getTimestamp(TimeUtils.getCurrentTime(TimeUtils.FORMAT_DEFAULT)));
						firstOnlineProduct.setLastEventTimeKey(TimeUtils.getCurrentEventTimeKey());
						firstOnlineProduct.setLastEventUser(eventInfo.getEventUser());
						
						firstOnlineProduct = ExtendedObjectProxy.getFirstOnlineProductService().create(eventInfo, firstOnlineProduct);
						}
				}
				
		}
				
		return doc;
		
	}
}