package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.STKConfig;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class ChangeStockerConfig extends SyncHandler {
	private static Log log = LogFactory.getLog(ChangeStockerConfig.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> stkList = SMessageUtil.getBodySequenceItemList(doc, "STKLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeStockerConfig", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		for (Element rule : stkList) 
		{
			String factoryName = SMessageUtil.getChildText(rule, "FACTORYNAME", true);
			String stkName = SMessageUtil.getChildText(rule, "STKNAME", true);
			String emptyLowerLimit = SMessageUtil.getChildText(rule, "EMPTYLOWERLIMIT", true);
			String emptyUpperLimit = SMessageUtil.getChildText(rule, "EMPTYUPPERLIMIT", true);
			
			ArrayList<Long> list = new ArrayList<Long>();

			String[] slist = new String[] { emptyLowerLimit, emptyUpperLimit };

			try 
			{
				list = ExtendedObjectProxy.getSTKConfigService().StringToLong(slist);
			} 
			catch (NumberFormatException e) 
			{
				log.info("ParseError");
				throw new CustomException("ParseError-001");
			}

			try 
			{
				STKConfig STKConfigInfo = ExtendedObjectProxy.getSTKConfigService().selectByKey(false, stkName);
				log.info("Start Modify");
				eventInfo.setEventName("Modify");
				
				STKConfigInfo.setSTKNAME(stkName);
				STKConfigInfo.setEMPTYLOWERLIMIT(list.get(0));
				STKConfigInfo.setEMPTYUPPERLIMIT(list.get(1));
				STKConfigInfo.setFACTORYNAME(factoryName);

				ExtendedObjectProxy.getSTKConfigService().modify(eventInfo, STKConfigInfo);
			} 
			catch (greenFrameDBErrorSignal e) 
			{
				if (StringUtil.equals(e.getErrorCode().toString(), ErrorSignal.NotFoundSignal)) 
				{
					log.info("Start Create");
					eventInfo.setEventName("Create");
					STKConfig newSTKConfig = new STKConfig();
					
					newSTKConfig.setSTKNAME(stkName);
					newSTKConfig.setFACTORYNAME(factoryName);
					newSTKConfig.setEMPTYLOWERLIMIT(list.get(0));
					newSTKConfig.setEMPTYUPPERLIMIT(list.get(1));

					ExtendedObjectProxy.getSTKConfigService().create(eventInfo, newSTKConfig);
				} 
				else 
				{
					log.info("DB Error");
					throw new CustomException("DBError-001");
				}
			}
		}
		return doc;
	}

}
