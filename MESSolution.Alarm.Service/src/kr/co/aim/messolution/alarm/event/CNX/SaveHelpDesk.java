package kr.co.aim.messolution.alarm.event.CNX;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.HelpDeskInfo;
//Add import
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class SaveHelpDesk extends SyncHandler {
	private static Log log = LogFactory.getLog(SaveHelpDesk.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		Element eleBody = SMessageUtil.getBodyElement(doc);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), "", "", "");

		if (eleBody != null)
		{
			log.info("Excute Update Notice :Start! ");
			for (Element eleHelpDesk : SMessageUtil.getBodySequenceItemList(doc, "HELPDESKLIST", false))
			{
				String workDate = SMessageUtil.getChildText(eleHelpDesk, "WORKDATE", true);
				String workUserName = SMessageUtil.getChildText(eleHelpDesk, "WORKUSERNAME", true);
				String workUserId = SMessageUtil.getChildText(eleHelpDesk, "WORKUSERID", true);
				String workType = SMessageUtil.getChildText(eleHelpDesk, "WORKTYPE", true);

				workDate = workDate + " 00:00:00";
				HelpDeskInfo newHelpDeskInfo = new HelpDeskInfo();
				newHelpDeskInfo.setWorkDate(Timestamp.valueOf(workDate));
				newHelpDeskInfo.setWorkUserId(workUserId);
				newHelpDeskInfo.setWorkUserName(workUserName);
				newHelpDeskInfo.setWorkType(workType);

				if (ExtendedObjectProxy.getHelpDeskInfoService().create(eventInfo, newHelpDeskInfo))
					log.info("Excute Insert Notice :Insert! ");
			}

		}

		log.info("Excute Update Notice :Complete! ");

		return doc;
	}
}
