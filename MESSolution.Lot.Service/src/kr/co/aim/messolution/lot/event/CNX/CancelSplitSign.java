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

public class CancelSplitSign extends SyncHandler
{
	public static Log log = LogFactory.getLog(CancelSplitSign.class);
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelSplitSign", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		List<SorterSignReserve> dataInfoList = new ArrayList<SorterSignReserve>();
		
		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		for (Element product : productList)
		{
			String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", true);
			String position = SMessageUtil.getChildText(product, "POSITION", true);
			String slotPosition = SMessageUtil.getChildText(product, "SLOTPOSITION", false);
			String processOperationName = SMessageUtil.getChildText(product, "PROCESSOPERATIONNAME", false);
			
			List<SorterSignReserve> checkList = ExtendedObjectProxy.getSorterSignReserveService().getDataInfoListByProductNameAndOper(productName, processOperationName);

			if (checkList == null || checkList.size() < 1)
				throw new CustomException("WC-9999","There Is No Data To Delete!");
			if (checkList.size() > 1)
				throw new CustomException("WC-9999","There Is More Then One Data To Delete!");
			
			ExtendedObjectProxy.getSorterSignReserveService().remove(eventInfo,checkList.get(0));
		}
		
		return doc;
	}
	
}
