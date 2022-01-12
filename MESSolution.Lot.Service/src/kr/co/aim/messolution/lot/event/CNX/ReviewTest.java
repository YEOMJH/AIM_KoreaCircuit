package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.util.List;

import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReviewTestImageJudge;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class ReviewTest extends AsyncHandler{
	
	private static Log log = LogFactory.getLog(ReviewTest.class);
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		
 		String reviewTestName=SMessageUtil.getBodyItemValue(doc, "REVIEWTESTNAME", true);
		String factoryName=SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String machineType=SMessageUtil.getBodyItemValue(doc, "MACHINETYPE", true);
		String action=SMessageUtil.getBodyItemValue(doc, "ACITION", true);
		
		String currect="N";
		Timestamp startTime =null;
		Timestamp endTime=null;
		Boolean firstFlag=false;
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReviewTestImageJudge", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setLastEventTimekey(TimeUtils.getCurrentEventTimeKey());
		
		
		try
		{
			//Get startTime
			log.info("get start time");
			String condition=" REVIEWTESTNAME=? AND FACTORYNAME=? AND MACHINETYPE=? AND JUDGEUSER=? ";
			List<ReviewTestImageJudge> judgeList=
					ExtendedObjectProxy.getReviewStationImageJudgeService().select(condition,
							new Object[]{reviewTestName,factoryName,machineType,eventInfo.getEventUser()});
			firstFlag=false;
		}
		catch(greenFrameDBErrorSignal e)
		{
			startTime=TimeUtils.getCurrentTimestamp();
			firstFlag=true;
		}
        ReviewTestImageJudge dataInfo=new ReviewTestImageJudge();
		if(StringUtil.equals(action, "Judge"))
		{
			for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "IMAGELIST", true))
			{
				String reviewDefectCode=SMessageUtil.getChildText(eledur, "DEFECTCODE", false);
				String seq=SMessageUtil.getChildText(eledur, "SEQ", true);
				String imageName=SMessageUtil.getChildText(eledur, "IMAGENAME", false);
				String judgeCode=SMessageUtil.getChildText(eledur, "JUDGECODE", false);
				String defectJudge=SMessageUtil.getChildText(eledur, "DEFECTGRADE", false);
				String reviewDefectGrade="";

                if(firstFlag)
                {
    				if((StringUtils.equals(factoryName, "ARRAY")||StringUtils.equals(factoryName, "TP"))
    						&&StringUtils.isNotEmpty(imageName)&&imageName.length()>=13)
    				{
    					reviewDefectGrade=imageName.substring(12,13);
    				}
                	dataInfo.setSeq(seq);
                	dataInfo.setReviewTestName(reviewTestName);
    				dataInfo.setFactoryName(factoryName);;
    				dataInfo.setMachineType(machineType);
    				dataInfo.setReviewDefectCode(reviewDefectCode);
    				dataInfo.setImageName(imageName);
    				if(StringUtil.isNotEmpty(judgeCode))
    				{
    					if(StringUtil.equals(reviewDefectCode, judgeCode))
    					{
    						currect="Y";
    					}
    					else
    					{
    						currect="N";
    					}
    					if(StringUtil.isNotEmpty(reviewDefectGrade)&&StringUtils.equals(currect, "Y")&&StringUtils.equals(machineType, "AOI"))
    					{
    						if(!StringUtils.equals(reviewDefectGrade, defectJudge))
    						{
    							currect="N";
    						}
    					}
    					dataInfo.setJudgeCode(judgeCode);
    					dataInfo.setStartTime(startTime);
    					dataInfo.setCurrect(currect);
    					dataInfo.setLastEventName("ReviewTestImageJudge");
    				}
    				else
    				{
    					dataInfo.setJudgeCode(judgeCode);
    					dataInfo.setStartTime(startTime);
    					dataInfo.setCurrect("");
    					dataInfo.setLastEventName("InsertReviewTestImage");
    				}  	
    				dataInfo.setDefectGrade(defectJudge);;
    				dataInfo.setJudgeUser(eventInfo.getEventUser());
    				dataInfo.setLastEventComment(eventInfo.getEventComment());
    				dataInfo.setLastEventTimeKey(eventInfo.getLastEventTimekey());
    				ExtendedObjectProxy.getReviewStationImageJudgeService().create(eventInfo, dataInfo);
                }
                else
                {
                	String condition2=" REVIEWTESTNAME=? AND FACTORYNAME=? AND MACHINETYPE=? AND JUDGEUSER=? AND SEQ=?  ";
                	List<ReviewTestImageJudge> updateImageJudge=
    						ExtendedObjectProxy.getReviewStationImageJudgeService().select(condition2,
    								new Object[]{reviewTestName,factoryName,machineType,eventInfo.getEventUser(),seq});
                	dataInfo=updateImageJudge.get(0);
                	imageName=dataInfo.getImageName();
    				if((StringUtils.equals(factoryName, "ARRAY")||StringUtils.equals(factoryName, "TP"))
    						&&StringUtils.isNotEmpty(imageName)&&imageName.length()>=13)
    				{
    					reviewDefectGrade=imageName.substring(12,13);
    				}
                	
    				dataInfo.setJudgeCode(judgeCode);
    				if(StringUtil.equals(dataInfo.getReviewDefectCode(), judgeCode))
					{
						currect="Y";
					}
					else
					{
						currect="N";
					}
    				
    				if(StringUtil.isNotEmpty(reviewDefectGrade)&&StringUtils.equals(currect, "Y")&&StringUtils.equals(machineType, "AOI"))
					{
						if(!StringUtils.equals(reviewDefectGrade, defectJudge))
						{
							currect="N";
						}
					}
    				
    				dataInfo.setCurrect(currect);
    				dataInfo.setDefectGrade(defectJudge);
    				dataInfo.setLastEventTimeKey(eventInfo.getLastEventTimekey());
    				dataInfo.setLastEventName("ReviewTestImageJudge");
    				dataInfo.setLastEventComment(eventInfo.getEventComment());
    				ExtendedObjectProxy.getReviewStationImageJudgeService().modify(eventInfo, dataInfo);
                }
			}
		}
		else if(StringUtil.equals(action, "Complete"))
		{
			endTime=TimeUtils.getCurrentTimestamp();
			if(firstFlag)
			{
				dataInfo.setReviewTestName(reviewTestName);
				dataInfo.setFactoryName(factoryName);
				dataInfo.setMachineType(machineType);
				dataInfo.setReviewDefectCode("-");
				dataInfo.setImageName("-");
				dataInfo.setJudgeCode("-");
				dataInfo.setJudgeUser(eventInfo.getEventUser());
				dataInfo.setCurrect("-");
				dataInfo.setStartTime(endTime);
				dataInfo.setEndTime(endTime);
				dataInfo.setLastEventComment(eventInfo.getEventComment());
				dataInfo.setLastEventName("CompleteReviewTestJudge");
				dataInfo.setLastEventTimeKey(eventInfo.getLastEventTimekey());
				ExtendedObjectProxy.getReviewStationImageJudgeService().create(eventInfo, dataInfo);
			}
			else
			{
				String condition2=" REVIEWTESTNAME=? AND FACTORYNAME=? AND MACHINETYPE=? AND JUDGEUSER=? ";
				List<ReviewTestImageJudge> imageJudgeList=
						ExtendedObjectProxy.getReviewStationImageJudgeService().select(condition2,
								new Object[]{reviewTestName,factoryName,machineType,eventInfo.getEventUser()});
				for (ReviewTestImageJudge reviewTestImageJudge : imageJudgeList) 
				{
					if(reviewTestImageJudge.getStartTime()==null || StringUtil.isEmpty(reviewTestImageJudge.getStartTime().toString()))
					continue;
					dataInfo=(ReviewTestImageJudge) ObjectUtil.copyTo(reviewTestImageJudge);
					dataInfo.setEndTime(endTime);
					dataInfo.setLastEventComment(eventInfo.getEventComment());
					dataInfo.setLastEventName("CompleteReviewTestJudge");
					dataInfo.setLastEventTimeKey(eventInfo.getLastEventTimekey());
					ExtendedObjectProxy.getReviewStationImageJudgeService().modify(eventInfo, dataInfo);
					break;
				}
			}
		}
		
	}

}
