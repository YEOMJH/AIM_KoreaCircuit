package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.List;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.EnumDefValue;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class CRPRepair extends SyncHandler {

	private static Log log = LogFactory.getLog(CRPRepair.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String pointName = SMessageUtil.getBodyItemValue(doc, "POINTNAME", true);
		String pointX = SMessageUtil.getBodyItemValue(doc, "POINTX", true);
		String pointY = SMessageUtil.getBodyItemValue(doc, "POINTY", true);
		String insName = SMessageUtil.getBodyItemValue(doc, "INSNAME", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CRPRepair", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(ConvertUtil.getCurrTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		List<EnumDefValue> descendedCodeList = ExtendedObjectProxy.getEnumDefValueService().select("ENUMNAME = ? AND DESCRIPTION = ? AND DISPLAYCOLOR = ? AND SEQ = ?", new Object[]{"CRPRepair",lotName,pointX,pointY});
		
		if (descendedCodeList!=null && descendedCodeList.size()>0)
		{
			throw new CustomException("SYS-0010", "Can't Insert Same PointXY");
			//throw new CustomException("MVI-0003");
			//throw new CustomException("Can't Insert Same PointXY");
		}
		else 
		{
			EnumDefValue descendedCode = new EnumDefValue();
			
			descendedCode.setEnumName("CRPRepair");
			descendedCode.setEnumValue(TimeStampUtil.getCurrentEventTimeKey());
			descendedCode.setDescription(lotName);
			descendedCode.setDefaultFlag(pointName + " " + insName);
			descendedCode.setDisplayColor(pointX);
			descendedCode.setSeq(pointY);
			
			ExtendedObjectProxy.getEnumDefValueService().create("CRPRepair", TimeStampUtil.getCurrentEventTimeKey(), lotName, pointName + " " + insName, pointX, pointY, eventInfo);
		}
		return doc;
	}	
}