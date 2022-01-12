package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.VcrAbnormalPanel;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class ClosedAbnormalPanelTask extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		List<Element> abnormalPanelList = SMessageUtil.getBodySequenceItemList(doc, "VCRABNORMALPANELLIST", true);

		for (Element abnormalPane : abnormalPanelList)
		{
			String taskID = SMessageUtil.getChildText(abnormalPane, "TASKID", true);
			String panelID = SMessageUtil.getChildText(abnormalPane, "LOTNAME", true);
			String vcrPanelID = SMessageUtil.getChildText(abnormalPane, "VCRLOTNAME", true);
		
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ClosedAbnormalPanelTask", this.getEventUser(), this.getEventComment(), "", "");
			eventInfo.setLastEventTimekey(TimeUtils.getCurrentEventTimeKey());

			VcrAbnormalPanel abnormalPanelInfo= ExtendedObjectProxy.getVcrAbnormalPanelDataService().getVcrAbnormalPanelData(taskID, panelID, vcrPanelID);

			ExtendedObjectProxy.getVcrAbnormalPanelDataService().remove(eventInfo, abnormalPanelInfo);
		}
		
		
		
		return doc;
	}

}
