package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.BorrowPanel;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class CreateBorrowPanel extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		List<Element> BORROWPANELLIST = SMessageUtil.getBodySequenceItemList(doc, "CREATEBORROWPANELLIST", true);
		
		String taskIdTimeKey=TimeUtils.getCurrentEventTimeKey().substring(0,14);
		
		for(Element PANEL:BORROWPANELLIST){
			
			String lotName = SMessageUtil.getChildText(PANEL, "LOTNAME", true);
			String productSpecName = SMessageUtil.getChildText(PANEL, "PRODUCTSPECNAME", true);
			String productSpecVersion = SMessageUtil.getChildText(PANEL, "PRODUCTSPECVERSION",true);
			String productRequestName = SMessageUtil.getChildText(PANEL, "PRODUCTREQUESTNAME", false);
			String processFlowName = SMessageUtil.getChildText(PANEL, "PROCESSFLOWNAME", false);
			String processFlowVersion = SMessageUtil.getChildText(PANEL, "PROCESSFLOWVERSION", false);
			String borrowUserName = SMessageUtil.getChildText(PANEL, "BORROWUSERNAME", true);
			String borrowCentrality = SMessageUtil.getChildText(PANEL, "BORROWCENTRALITY", true);
			String borrowDepartment = SMessageUtil.getChildText(PANEL, "BORROWDEPARTMENT", true);
			String phone = SMessageUtil.getChildText(PANEL, "PHONE", false);
			String email = SMessageUtil.getChildText(PANEL, "EMAIL", false);

			
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateBorrowPanel", getEventUser(), getEventComment(), "", "");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			
			//BorrowPanel
			BorrowPanel dataInfo = new BorrowPanel();
			
			dataInfo.setTaskId(borrowDepartment+taskIdTimeKey);
			dataInfo.setBorrowState("Created");
			dataInfo.setLotName(lotName);
			dataInfo.setProductRequestName(productRequestName);
			dataInfo.setProductSpecName(productSpecName);
			dataInfo.setProductSpecVersion(productSpecVersion);
			dataInfo.setProcessFlowName(processFlowName);
			dataInfo.setProcessFlowVersion(processFlowVersion);
			dataInfo.setBorrowUserName(borrowUserName);
			dataInfo.setBorrowCentrality(borrowCentrality);
			dataInfo.setBorrowDepartment(borrowDepartment);
			dataInfo.setRenewCount(0);
			if(StringUtil.isNotEmpty(phone))dataInfo.setPhone(phone);		
			dataInfo.setEmail(email);
			
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
			
			if (!checkExist(lotName))
			{
				throw new CustomException("BORROW-0008", lotName);
			}
			
			
			ExtendedObjectProxy.getBorrowPanelService().create(eventInfo, dataInfo);
	}
		return doc;
	}
	
	private boolean checkExist(String lotName) throws greenFrameDBErrorSignal, CustomException
	{
		try
		{
			ExtendedObjectProxy.getBorrowPanelService().select(" LOTNAME=: LOTNAME", new Object[] {lotName});
			return false;
		}
		catch (Exception e)
		{
			return true;
		}
	}
}
