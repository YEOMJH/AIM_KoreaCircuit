package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SorterSignReserve;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class SplitSignForSorter extends SyncHandler
{
	public static Log log = LogFactory.getLog(SplitSignForSorter.class);
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String reserveUser = SMessageUtil.getBodyItemValue(doc, "RESERVEUSER", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("SplitSignForSorter", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		List<SorterSignReserve> dataInfoList = new ArrayList<SorterSignReserve>();
		
		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		for (Element product : productList)
		{
			String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", true);
			String position = SMessageUtil.getChildText(product, "POSITION", true);
			String slotPosition = SMessageUtil.getChildText(product, "SLOTPOSITION", false);
			
			List<SorterSignReserve> checkList = ExtendedObjectProxy.getSorterSignReserveService().getDataInfoListByProductName(productName);

			if (checkList != null && checkList.size()>0)
				throw new CustomException("WC-9999","There Is Already Data Created");
			
			SorterSignReserve info = new SorterSignReserve();
			info.setLotName(lotName);
			info.setPosition(position);
			info.setProcessFlowName(processFlowName);
			info.setProcessOperationName(processOperationName);
			info.setProductName(productName);
			info.setReserveUser(reserveUser);
			info.setSlotPosition(slotPosition);
			
			dataInfoList.add(info);
		}
		
		if (dataInfoList!=null && dataInfoList.size() > 0)
			ExtendedObjectProxy.getSorterSignReserveService().create(eventInfo, dataInfoList);

		return doc;
	}
	
}
