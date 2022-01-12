package kr.co.aim.messolution.durable.event.CNX;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.EvaLineSchedule;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.MaterialProduct;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

public class WOSchedulingToEVA extends SyncHandler
{
	private static Log log = LogFactory.getLog(WOSchedulingToEVA.class);
	@Override
	public Object doWorks(Document doc) throws CustomException	
	{	
		
			String action = SMessageUtil.getBodyItemValue(doc, "ACTION", true);			
			List<Element> workOrderList = SMessageUtil.getBodySequenceItemList(doc, "WORKORDERLIST", true);

			for (Element workOrder : workOrderList)	 
	        {
				String machineName = SMessageUtil.getChildText(workOrder, "MACHINENAME", true);
				String lineType = SMessageUtil.getChildText(workOrder, "LINETYPE", true);
				String productSpecName =SMessageUtil.getChildText(workOrder, "PRODUCTSPECNAME", true);
				String seq =SMessageUtil.getChildText(workOrder, "SEQ", true);
				String productRequestName =SMessageUtil.getChildText(workOrder, "PRODUCTREQUESTNAME", true);

				String state =SMessageUtil.getChildText(workOrder, "STATE", true);
				String planQuantity =SMessageUtil.getChildText(workOrder, "PLANQUANTITY", true);
				String planStartDate =SMessageUtil.getChildText(workOrder, "PLANSTARTDATE", true);
				String planEndDate =SMessageUtil.getChildText(workOrder, "PLANENDDATE", true);
				String useFlag =SMessageUtil.getChildText(workOrder, "USEFLAG", true);
				Date date = new Date();
				DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:00:00.0");
				try {
					date = sdf.parse(planStartDate);
				} catch (Exception e) {
					e.printStackTrace();
				}
				Timestamp tplanStartDate = new Timestamp(date.getTime());
				ProductRequest productRequest = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);
				
				if(!productRequest.getProductRequestState().equals("Released"))
				{
					log.info("Plan"+productRequestName+" State is Released !!");
					throw new CustomException("WOSchedulingToEVA-001", productRequestName);
				}
				
				if(productRequest.getPlanQuantity()<Float.parseFloat(planQuantity)){
					log.info("The number of Plans exceeds workOrder"+productRequestName+"!!");
					throw new CustomException("WOSchedulingToEVA-003", productRequestName);
				}
				
				boolean isExist =  ExtendedObjectProxy.getEvaLineScheduleService().selectByKeyReturnTrueOrFalse(machineName,lineType,productRequestName,planStartDate);
		        if(action.equals("OK"))
			    {	
					if( !isExist ){
						EventInfo eventInfo = EventInfoUtil.makeEventInfo("WOSchedulingToEVACreate", this.getEventUser(), this.getEventComment(), "", "", "Y");			
						eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
						
						EvaLineSchedule evaLineScheduleInfo = new EvaLineSchedule();
						evaLineScheduleInfo.setMachineName(machineName);
						evaLineScheduleInfo.setLineType(lineType);
						evaLineScheduleInfo.setProductSpecName(productSpecName);
						evaLineScheduleInfo.setSeq(Long.parseLong(seq));
						evaLineScheduleInfo.setProductRequestName(productRequestName);
						evaLineScheduleInfo.setState(state);
						evaLineScheduleInfo.setPlanQuantity(Long.parseLong(planQuantity));
						evaLineScheduleInfo.setPlanStartDate(TimeUtils.getTimestamp(planStartDate));
						evaLineScheduleInfo.setPlanEndDate(TimeUtils.getTimestamp(planEndDate));
						evaLineScheduleInfo.setUseFlag(useFlag);
						evaLineScheduleInfo.setLastEventComment(eventInfo.getEventComment());
						evaLineScheduleInfo.setLastEventName(eventInfo.getEventName());
						evaLineScheduleInfo.setLastEventTime(eventInfo.getEventTime());
						evaLineScheduleInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
						evaLineScheduleInfo.setLastEventUser(eventInfo.getEventUser());
						evaLineScheduleInfo.setScheduleTimeKey(eventInfo.getEventTimeKey());
						ExtendedObjectProxy.getEvaLineScheduleService().create(eventInfo, evaLineScheduleInfo);
					}
							
					if(isExist)
					{
						
						EventInfo eventInfo = EventInfoUtil.makeEventInfo("WOSchedulingToEVAModify", this.getEventUser(), this.getEventComment(), "", "", "Y");
					eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());
					
					EvaLineSchedule evaLineScheduleInfo =  ExtendedObjectProxy.getEvaLineScheduleService().selectByKey(machineName,lineType,planStartDate,productRequestName);
					if(!evaLineScheduleInfo.getState().equals("Ready")){
					
					log.info("Plan"+productRequestName+" State is Released !!");
					throw new CustomException("WOSchedulingToEVA-004");
						}
						evaLineScheduleInfo.setState(state);
						evaLineScheduleInfo.setPlanQuantity(Long.parseLong(planQuantity));
						evaLineScheduleInfo.setSeq(Long.parseLong(seq));
						evaLineScheduleInfo.setPlanStartDate(TimeUtils.getTimestamp(planStartDate));
						evaLineScheduleInfo.setPlanEndDate(TimeUtils.getTimestamp(planEndDate));
						evaLineScheduleInfo.setUseFlag(useFlag);
						evaLineScheduleInfo.setLastEventComment(eventInfo.getEventComment());
						evaLineScheduleInfo.setLastEventName(eventInfo.getEventName());
						evaLineScheduleInfo.setLastEventTime(eventInfo.getEventTime());
						evaLineScheduleInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
						evaLineScheduleInfo.setLastEventUser(eventInfo.getEventUser());
						ExtendedObjectProxy.getEvaLineScheduleService().modify(eventInfo, evaLineScheduleInfo);	
					}
		        }
				if(action.equals("Delete"))
				{
					if(isExist)
					{
						EventInfo eventInfo = EventInfoUtil.makeEventInfo("WOSchedulingToEVADelete", this.getEventUser(), this.getEventComment(), "", "", "Y");
						eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());
						
						EvaLineSchedule evaLineScheduleInfo =  ExtendedObjectProxy.getEvaLineScheduleService().selectByKey(machineName,lineType,planStartDate,productRequestName);
						
						evaLineScheduleInfo.setLastEventComment(eventInfo.getEventComment());
						evaLineScheduleInfo.setLastEventName(eventInfo.getEventName());
						evaLineScheduleInfo.setLastEventTime(eventInfo.getEventTime());
						evaLineScheduleInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
						evaLineScheduleInfo.setLastEventUser(eventInfo.getEventUser());
						
						ExtendedObjectProxy.getEvaLineScheduleService().remove(eventInfo, evaLineScheduleInfo);						
					}
					else
					{
						log.info("Binding relationship already exists !!");
						throw new CustomException("WOSchedulingToEVA-001");
					}
			    }	
	        
	     }

		return doc;
		
	}

	
	
	
	
	
}
