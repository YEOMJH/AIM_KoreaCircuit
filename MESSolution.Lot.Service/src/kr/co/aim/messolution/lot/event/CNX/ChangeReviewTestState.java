package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReviewStationTest;
import kr.co.aim.messolution.extended.object.management.impl.ReviewStationTestService;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.event.LotProcessEnd;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.springframework.aop.ThrowsAdvice;

import com.sun.xml.internal.ws.resources.UtilMessages;

public class ChangeReviewTestState extends SyncHandler{

	private static Log log = LogFactory.getLog(LotProcessEnd.class);
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeReviewTestState", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

	    for ( Element  defectCode : SMessageUtil.getBodySequenceItemList(doc, "DEFECTCODELIST", true)) 
		{
		    String reviewtestName=SMessageUtil.getChildText(defectCode, "REVIEWTESTNAME", true);
			String factoryName=SMessageUtil.getChildText(defectCode, "FACTORYNAME", true);
			String machineType=SMessageUtil.getChildText(defectCode, "MACHINETYPE", true);
				
			List<ReviewStationTest> reviewTestData= ExtendedObjectProxy.getReviewStationTestService().select(" REVIEWTESTNAME=? AND FACTORYNAME=? AND MACHINETYPE=? ", 
					new Object[]{reviewtestName,factoryName,machineType});
			if(reviewTestData.size()<1)
			{
				throw new CustomException("REVIEWTEST-0002",reviewtestName );
			}
			else if(reviewTestData.get(0).getReviewTestState().equals("Completed"))
			{
				throw new CustomException("REVIEWTEST-0003",reviewtestName );
			}
				
			for(ReviewStationTest reviewTestInfo:reviewTestData)
			{
				reviewTestInfo.setReviewTestState("Completed");
				reviewTestInfo.setLastEventComment(eventInfo.getEventComment());
				reviewTestInfo.setLastEventName(eventInfo.getEventName());
				reviewTestInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
				reviewTestInfo.setLastEventUser(eventInfo.getEventUser());
				ExtendedObjectProxy.getReviewStationTestService().modify(eventInfo, reviewTestInfo);
			}
		}

		return doc;
		
	}
}
