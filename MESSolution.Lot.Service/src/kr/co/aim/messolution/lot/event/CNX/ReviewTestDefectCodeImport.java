package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class ReviewTestDefectCodeImport extends SyncHandler{
	
	private static Log log = LogFactory.getLog(ReviewTestDefectCodeImport.class);
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> defectCodeList = SMessageUtil.getBodySequenceItemList(doc, "DEFECTCOEDLIST", true);
		List<Element> reviewTestNameList = SMessageUtil.getBodySequenceItemList(doc, "REVIEWTESTLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Import", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		StringBuffer deleteSQL  = new StringBuffer();
		deleteSQL.append(" DELETE CT_REVIEWTESTDEFECTCODE WHERE FACTORYNAME=:FACTORYNAME ");
		
		StringBuffer insetSQL  = new StringBuffer();
		insetSQL.append("  INSERT INTO CT_REVIEWTESTDEFECTCODE VALUES ");
		insetSQL.append(" (:FACTORYNAME,:REVIEWDEFECTCODE,:DESCRIPTION,:LASTEVENTTIMEKEY,:LASTEVENTUSER,:LASTEVENTCOMMENT,:LASTEVENTNAME,:MACHINETYPE) ");
		
		for (Element reviewTest : reviewTestNameList)
		{
			String reviewTestName = reviewTest.getChild("FACTORYNAME").getText();
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("FACTORYNAME", reviewTestName);
			try
			{
			    GenericServiceProxy.getSqlMesTemplate().update(deleteSQL.toString(), args);
			    log.info(reviewTestName +" info Deleted");
			}
			catch (Exception e)
			{
				throw new CustomException(e.getCause());
			}
			
		}
		
		for (Element defectCode : defectCodeList)
		{
			String factoryName = defectCode.getChild("FACTORYNAME").getText();
			String machineType = defectCode.getChild("MACHINETYPE").getText();
			String reviewDefectCode = defectCode.getChild("REVIEWDEFECTCODE").getText();
			String description = defectCode.getChild("DESCRIPTION").getText();
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("FACTORYNAME", factoryName);
			args.put("MACHINETYPE", machineType);
			args.put("REVIEWDEFECTCODE", reviewDefectCode);
			args.put("DESCRIPTION", description);
			args.put("LASTEVENTTIMEKEY", eventInfo.getEventTimeKey());
			args.put("LASTEVENTUSER", eventInfo.getEventUser());
			args.put("LASTEVENTCOMMENT", eventInfo.getEventComment());
			args.put("LASTEVENTNAME", "Import");

			try
			{
			    GenericServiceProxy.getSqlMesTemplate().update(insetSQL.toString(), args);
			    log.info(factoryName +" info Insert");
			}
			catch (Exception e)
			{
				throw new CustomException(e.getCause());
			}			
		}
				
		
		
		return doc;
	}

}
