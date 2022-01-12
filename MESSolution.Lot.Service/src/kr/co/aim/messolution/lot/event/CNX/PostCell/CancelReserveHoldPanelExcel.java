package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LotFutureAction;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class CancelReserveHoldPanelExcel extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> panelList = SMessageUtil.getBodySequenceItemList(doc, "PANELLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelReserveHold", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		List<LotFutureAction> deleteDataList = new ArrayList<LotFutureAction>();
		
		for(int i = 0 ; i < panelList.size() ; i++)
		{
			String lotName = panelList.get(i).getChildText("LOTNAME");
			String processFlowName = panelList.get(i).getChildText("PROCESSFLOWNAME");
			String processOperationName = panelList.get(i).getChildText("PROCESSOPERATIONNAME");
	
			String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? ";
			Object[] bindSet = new Object[] { lotName, "POSTCELL", processFlowName, "00001", processOperationName, "00001" };
			
			List<LotFutureAction> futureActionList = new ArrayList<>();
			
			try
			{
				futureActionList = ExtendedObjectProxy.getLotFutureActionService().select(condition, bindSet);
			}
			catch (Exception e)
			{}
			
			for(LotFutureAction dataInfo : futureActionList)
			{
				deleteDataList.add(dataInfo);
			}
		}
		
		if(deleteDataList.size() == 0)
		{
			throw new CustomException("CRUCIBLE-0016"); 
		}
		
		ExtendedObjectProxy.getLotFutureActionService().remove(eventInfo, deleteDataList);

		return doc;
	}
}
