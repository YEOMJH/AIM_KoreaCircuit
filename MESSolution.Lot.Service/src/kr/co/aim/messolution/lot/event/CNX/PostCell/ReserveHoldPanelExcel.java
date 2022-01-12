package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

import org.jdom.Document;
import org.jdom.Element;

public class ReserveHoldPanelExcel extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> panelList = SMessageUtil.getBodySequenceItemList(doc, "PANELLIST", true);
		List<Element> ReasonCodeList = SMessageUtil.getBodySequenceItemList(doc, "REASONCODELIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("FutureHold", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		List<Object[]> updateFutureActionArgList = new ArrayList<Object[]>();

		List<Lot> lotDataList = getLotDataList(panelList);
		
		for(Lot lot : lotDataList)
		{
			for(int i = 0 ; i < panelList.size() ; i++)
			{
				if(StringUtil.equals(panelList.get(i).getChildText("LOTNAME"), lot.getKey().getLotName()))
				{
					CommonValidation.checkLotState(lot);
					
					String processFlowName = panelList.get(i).getChildText("PROCESSFLOWNAME");
					String processOperationName = panelList.get(i).getChildText("PROCESSOPERATIONNAME");
			
					for (Element code : ReasonCodeList)
					{
						String reasonCodeType = code.getChildText("REASONCODETYPE");
						String reasonCode = code.getChildText("REASONCODE");
						
						List<Object> faBindList = new ArrayList<Object>();

						faBindList.add(lot.getKey().getLotName());
						faBindList.add(lot.getFactoryName());
						faBindList.add(processFlowName);
						faBindList.add("00001");
						faBindList.add(processOperationName);
						faBindList.add("00001");
						faBindList.add("0");
						faBindList.add("hold");
						faBindList.add("System");
						faBindList.add(eventInfo.getEventName());
						faBindList.add(eventInfo.getEventUser());
						faBindList.add(eventInfo.getEventComment());
						faBindList.add(reasonCodeType);
						faBindList.add(reasonCode);
						faBindList.add("");
						faBindList.add("");
						faBindList.add("");
						faBindList.add(eventInfo.getEventTimeKey());
						faBindList.add(eventInfo.getEventTime());

						updateFutureActionArgList.add(faBindList.toArray());
					}
					
					panelList.remove(i);
					break;
				}
			}
		}
		
		insertLotFutureActionData(updateFutureActionArgList);

		return doc;
	}

	private void insertLotFutureActionData(List<Object[]> updateFutureActionArgList) throws CustomException
	{
		StringBuffer sqlFA = new StringBuffer();
		sqlFA.append("INSERT INTO CT_LOTFUTUREACTION  ");
		sqlFA.append("(LOTNAME, FACTORYNAME, PROCESSFLOWNAME, PROCESSFLOWVERSION, PROCESSOPERATIONNAME, ");
		sqlFA.append(" PROCESSOPERATIONVERSION, POSITION, ACTIONNAME, ACTIONTYPE, LASTEVENTNAME, ");
		sqlFA.append(" LASTEVENTUSER, LASTEVENTCOMMENT, REASONCODETYPE, REASONCODE, ATTRIBUTE1, ");
		sqlFA.append(" ATTRIBUTE2, ATTRIBUTE3, LASTEVENTTIMEKEY, LASTEVENTTIME) ");
		sqlFA.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");

		StringBuffer sqlFAHistory = new StringBuffer();
		sqlFAHistory.append("INSERT INTO CT_LOTFUTUREACTIONHIST  ");
		sqlFAHistory.append("(LOTNAME, FACTORYNAME, PROCESSFLOWNAME, PROCESSFLOWVERSION, PROCESSOPERATIONNAME, ");
		sqlFAHistory.append(" PROCESSOPERATIONVERSION, POSITION, ACTIONNAME, ACTIONTYPE, EVENTNAME, ");
		sqlFAHistory.append(" EVENTUSER, EVENTCOMMENT, REASONCODETYPE, REASONCODE, ATTRIBUTE1, ");
		sqlFAHistory.append(" ATTRIBUTE2, ATTRIBUTE3, TIMEKEY, EVENTTIME) ");
		sqlFAHistory.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");

		MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlFA.toString(), updateFutureActionArgList);
		MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlFAHistory.toString(), updateFutureActionArgList);

	}
	
	private List<Lot> getLotDataList(List<Element> lotList) throws CustomException
	{
		String condition = "WHERE LOTNAME IN(";
		for (Element lotE : lotList) 
		{
			String lotName = lotE.getChildText("LOTNAME");
			
			condition += "'" + lotName + "',";
		}
		condition = condition.substring(0, condition.length() - 1) + ")";
		
		List<Lot> lotDataList = LotServiceProxy.getLotService().select(condition, new Object[] { });
		
		if(lotDataList.size() != lotList.size())
		{
			throw new CustomException("LOT-0311", lotList.size());
		}
		
		return lotDataList;
	}
}
