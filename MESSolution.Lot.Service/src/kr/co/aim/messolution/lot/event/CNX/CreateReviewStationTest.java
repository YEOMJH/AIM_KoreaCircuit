package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReviewStationTest;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.event.LotProcessEnd;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class CreateReviewStationTest extends SyncHandler{

	private static Log log = LogFactory.getLog(CreateReviewStationTest.class);
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		
		String reviewStationTestID=SMessageUtil.getBodyItemValue(doc, "REVIEWTESTNAME", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateReviewStationTest", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		try{
			//Validation ReviewStationTestID exist
			log.info("Validation ReviewStationTestID exist Start");
			String condition="REVIEWTESTNAME=?";
			List<ReviewStationTest> reviewStationTestData=ExtendedObjectProxy.getReviewStationTestService().select(condition, new Object[]{reviewStationTestID});
			if(reviewStationTestData.size()>0)
			{
				throw new CustomException("REVIEWTEST-0001",reviewStationTestID );
			}
		}
		catch(greenFrameDBErrorSignal e){
			log.info("Validation ReviewStationTestID exist Completed");
		}
		
		
		try{
			for ( Element  defectCode : SMessageUtil.getBodySequenceItemList(doc, "DEFECTCODELIST", true)) 
			{
				String factoryName=SMessageUtil.getChildText(defectCode, "FACTORYNAME", true);
				String machineType=SMessageUtil.getChildText(defectCode, "MACHINETYPE", true);
				String reviewDefectCode=SMessageUtil.getChildText(defectCode, "REVIEWDEFECTCODE", true);
				int imageCount=Integer.parseInt(SMessageUtil.getChildText(defectCode, "IMAGECOUNT", true));
				int testImageQty=Integer.parseInt(SMessageUtil.getChildText(defectCode, "TESTIMAGEQTY", true));
				
				ReviewStationTest reviewTestData=new ReviewStationTest();
				reviewTestData.setReviewTestName(reviewStationTestID);
				reviewTestData.setFactoryName(factoryName);
				reviewTestData.setMachineType(machineType);
				reviewTestData.setReviewDefectCode(reviewDefectCode);
				reviewTestData.setImageCount(imageCount);
				reviewTestData.setTestImageQty(testImageQty);
				reviewTestData.setReviewTestState("Released");
				reviewTestData.setCreateUser(eventInfo.getEventUser());
				reviewTestData.setCreateTime(eventInfo.getEventTime());
				reviewTestData.setLastEventTimeKey(eventInfo.getEventTimeKey());
				reviewTestData.setLastEventUser(eventInfo.getEventUser());
				reviewTestData.setLastEventName(eventInfo.getEventName());
				reviewTestData.setLastEventComment(eventInfo.getEventComment());
				ExtendedObjectProxy.getReviewStationTestService().create(eventInfo, reviewTestData);
			}
		}
		catch(Exception e)
		{
			
		}
		

		return doc;
		
	}
}
