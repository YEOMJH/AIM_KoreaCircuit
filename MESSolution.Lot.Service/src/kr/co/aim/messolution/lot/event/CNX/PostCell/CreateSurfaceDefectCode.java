package kr.co.aim.messolution.lot.event.CNX.PostCell;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.RiskLot;
import kr.co.aim.messolution.extended.object.management.data.SurfaceDefectCode;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class CreateSurfaceDefectCode   extends SyncHandler{
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
			String operationName = SMessageUtil.getChildText(codeInfo, "OPERATIONNAME", true);
			String judge = SMessageUtil.getChildText(codeInfo, "JUDGE", true);
			String defectCode = SMessageUtil.getChildText(codeInfo, "DEFECTCODE", true);
			String standard = SMessageUtil.getChildText(codeInfo, "STANDARD", true);
			String description = SMessageUtil.getChildText(codeInfo, "DESCRIPTION", true);
			SurfaceDefectCode newSurfeceDefectCode = new SurfaceDefectCode();
			newSurfeceDefectCode.setProductSpecName(productSpecName);
			newSurfeceDefectCode.setDefectCode(defectCode);
			newSurfeceDefectCode.setOperationName(operationName);
			newSurfeceDefectCode.setJudge(judge);
			newSurfeceDefectCode.setStandard(standard);
			newSurfeceDefectCode.setDescription(description);
			ExtendedObjectProxy.getSurfaceDefectCodeService().create(eventInfo, newSurfeceDefectCode);
		}
		
		return doc;
	}

}
