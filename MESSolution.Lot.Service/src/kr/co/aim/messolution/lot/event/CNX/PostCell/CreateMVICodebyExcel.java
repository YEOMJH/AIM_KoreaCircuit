package kr.co.aim.messolution.lot.event.CNX.PostCell;
import org.jdom.Document;
import org.jdom.Element;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MVIDefectCode;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import java.util.List;
public class CreateMVICodebyExcel extends SyncHandler{
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> codeList = SMessageUtil.getBodySequenceItemList(doc, "CODELIST", false);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateSurfaceDefectCode", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		for (Element codeInfo  : codeList)
		{
			String productSpecName = SMessageUtil.getChildText(codeInfo, "PRODUCTSPECNAME", true);
			String defectCode = SMessageUtil.getChildText(codeInfo, "DEFECTCODE", true);
			String description = SMessageUtil.getChildText(codeInfo, "DESCRIPTION", true);
			String superDefectCode = SMessageUtil.getChildText(codeInfo, "SUPERDEFECTCODE", true);
			String levelNo = SMessageUtil.getChildText(codeInfo, "LEVELNO", true);
			
			MVIDefectCode dataInfo = new MVIDefectCode();
			dataInfo.setProductSpecName(productSpecName);
			dataInfo.setDefectCode(defectCode);
			dataInfo.setDescription(description);
			dataInfo.setSuperDefectCode(superDefectCode);
			dataInfo.setLevelNo(Long.parseLong(levelNo));
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			ExtendedObjectProxy.getMVIDefectCodeService().create(eventInfo, dataInfo);
			
		}
		return doc;
	}
}
