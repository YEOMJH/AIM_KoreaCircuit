package kr.co.aim.messolution.alarm.event.CNX;

import kr.co.aim.messolution.alarm.MESAlarmServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class NoticeUpdate extends SyncHandler {
	private static Log log = LogFactory.getLog(NoticeUpdate.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String title = SMessageUtil.getBodyItemValue(doc, "TITLE", true);
		String content = SMessageUtil.getBodyItemValue(doc, "CONTENT", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String nlsType = SMessageUtil.getBodyItemValue(doc, "NLSTYPE", true);

		log.info("Excute Update Notice :Start! ");
		MESAlarmServiceProxy.getAlarmServiceUtil().updateNotice(title, content, factoryName, nlsType);
		log.info("Excute Update Notice :Complete! ");

		return doc;
	}
}
