package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.POSQueueTime;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import com.sun.org.apache.bcel.internal.generic.NEW;

public class ImportQTimeManagementDesc extends SyncHandler{
	
	private static Log log = LogFactory.getLog(ImportQTimeManagementDesc.class);
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> operationList = SMessageUtil.getBodySequenceItemList(doc, "PROCESSOPERATIONNAMELIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ImportQTimeManagementDesc", getEventUser(), getEventComment(), "", "");
		
		for(Element operationInfo:operationList)
		{
			eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
			String processOperationName=SMessageUtil.getChildText(operationInfo, "PROCESSOPERATIONNAME", true);				
			String toProcessOperationName=SMessageUtil.getChildText(operationInfo, "TOPROCESSOPERATIONNAME", true);
			String managementDesc=SMessageUtil.getChildText(operationInfo, "MANAGEMENTDESC", true);
			
			try 
			{
				List<POSQueueTime> qTimeDataList=ExtendedObjectProxy.getPOSQueueTimeService().select(" CONDITIONID LIKE ? AND TOPROCESSOPERATIONNAME=? ", 
						   new Object[]{"%"+processOperationName+"%",toProcessOperationName});
				for(POSQueueTime qTimeData:qTimeDataList)
				{
					qTimeData.setMANAGEMENTDESC(managementDesc);

					ExtendedObjectProxy.getPOSQueueTimeService().modify(eventInfo, qTimeData);
					
				}				
			} 
			catch (greenFrameDBErrorSignal e) 
			{
				log.error("not exist QTimeInfo,From:" +processOperationName+" To:"+toProcessOperationName);
			}
		}
		
		
		
		return doc;
	}

}
