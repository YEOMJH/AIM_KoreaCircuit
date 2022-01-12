package kr.co.aim.messolution.productrequest.event.CNX;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;


import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class UnCompleteWorkOrder extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		String checkFlag = SMessageUtil.getBodyItemValue(doc, "CHECKFLAG", false);
					
		if(!StringUtils.equals(checkFlag, "Y"))
		{
			
			ProductRequest productRequestData = ProductRequestServiceProxy.getProductRequestService().selectByKey(new ProductRequestKey(productRequestName));

			if (!StringUtils.equals(productRequestData.getProductRequestState(), GenericServiceProxy.getConstantMap().Prq_Completed))
					throw new CustomException("PRODUCTREQUEST-0004", productRequestData.getProductRequestState());

			if (productRequestData.getReleasedQuantity() != productRequestData.getFinishedQuantity() + productRequestData.getScrappedQuantity())
					throw new CustomException("PRODUCTREQUEST-0005", productRequestData.getReleasedQuantity(), productRequestData.getFinishedQuantity(), productRequestData.getScrappedQuantity());
			
			SuperProductRequest superProductRequest=new SuperProductRequest();
			if(StringUtils.isNotEmpty(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
			{
				superProductRequest=ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
				if(StringUtils.equals(superProductRequest.getProductRequestState(), "Completed"))
				{
					throw new CustomException("PRODUCTREQUEST-0042");
				}
			}

			checkReleaseLot(productRequestData);
			
			makeUnCompletedInfo(productRequestData);
		}
		else
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnCompleteWorkOrder", getEventUser(), getEventComment(), "", "");
			eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
			SuperProductRequest superProductRequestData = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{ productRequestName });
			if (!StringUtils.equals(superProductRequestData.getProductRequestState(), GenericServiceProxy.getConstantMap().Prq_Completed))
				throw new CustomException("PRODUCTREQUEST-0004", superProductRequestData.getProductRequestState());
			if (superProductRequestData.getReleasedQuantity() != superProductRequestData.getFinishedQuantity() + superProductRequestData.getScrappedQuantity())
				throw new CustomException("PRODUCTREQUEST-0005", superProductRequestData.getReleasedQuantity(), superProductRequestData.getFinishedQuantity(), superProductRequestData.getScrappedQuantity());

			superProductRequestData.setProductRequestState(GenericServiceProxy.getConstantMap().Prq_Released);
			superProductRequestData.setLastEventName(eventInfo.getEventName());
			superProductRequestData.setLastEventComment(eventInfo.getEventComment());
			superProductRequestData.setLastEventFlag("N");
			superProductRequestData.setLastEventTime(eventInfo.getEventTime());
			superProductRequestData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			superProductRequestData.setLastEventUser(eventInfo.getEventUser());
			superProductRequestData.setProductRequestHoldState("N");
			superProductRequestData.setCompleteTime(null);
			superProductRequestData.setCompleteUser("");
			
			ExtendedObjectProxy.getSuperProductRequestService().modify(eventInfo, superProductRequestData);
			
			// Send to SAP
			try
			{
				String sapFlag=CommonUtil.getEnumDefValueStringByEnumName("SAPFLAG");
				if(StringUtil.isNotEmpty(sapFlag)&&StringUtil.equals(sapFlag, "Y"))
				{
					List<Map<String, String>> dataInfoMapList = new ArrayList<Map<String,String>>();
					
					Map<String, String> dataInfoMap = new HashMap<String, String>();
					dataInfoMap.put("PRODUCTREQUESTNAME", superProductRequestData.getProductRequestName());
					
					Calendar cal = Calendar.getInstance();
					int hour = cal.get(Calendar.HOUR_OF_DAY);
					if(hour >= 19)
					{
						cal.set(Calendar.HOUR_OF_DAY, 0);
						cal.set(Calendar.MINUTE, 0);
						cal.set(Calendar.SECOND, 0);
						cal.set(Calendar.MILLISECOND, 0);
						cal.add(Calendar.DAY_OF_MONTH, 1);
						Timestamp receiveTime = new Timestamp(cal.getTime().getTime());
						dataInfoMap.put("EVENTTIME",receiveTime.toString().replace("-","").substring(0,8));
					}
					else
					{
						dataInfoMap.put("EVENTTIME", eventInfo.getEventTimeKey().substring(0,8));
					}
					dataInfoMap.put("ZCANCEL", "X");
					
					dataInfoMapList.add(dataInfoMap);
					
					String resultCode=ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().workOrderClose(eventInfo, dataInfoMapList, 1);
					if(!resultCode.equals("S"))
					{
						//SYSTEM-0015:Send To SAP Failed
						throw new CustomException("SYSTEM-0015");
					}
				}					
			}
			catch(CustomException cs)
			{
				//SYSTEM-0015:Send To SAP Failed
				throw new CustomException("SYSTEM-0015");
			}
			catch(Exception ex)
			{
				//SYSTEM-0015:Send To SAP Failed
				throw new CustomException("SYSTEM-0015");
			}
		}
			
					
		return doc;
	}

	private void checkReleaseLot(ProductRequest productRequestData) throws CustomException
	{
		String condition = " PRODUCTREQUESTNAME = ? AND LOTSTATE = 'Released'";
		Object[] bindSet = new Object[] { productRequestData.getKey().getProductRequestName() };

		List<Lot> lotList = new ArrayList<Lot>();

		try
		{
			lotList = LotServiceProxy.getLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			lotList = null;
		}

		if (lotList != null)
		{
			throw new CustomException("PRODUCTREQUEST-0041");
		}
	}
	
	private void makeUnCompletedInfo(ProductRequest productRequestData) throws CustomException
	{		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnCompleteWorkOrder", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		productRequestData.setProductRequestState(GenericServiceProxy.getConstantMap().Prq_Released);
		productRequestData.setLastEventName(eventInfo.getEventName());
		productRequestData.setLastEventComment(eventInfo.getEventComment());
		productRequestData.setLastEventFlag("N");
		productRequestData.setLastEventTime(eventInfo.getEventTime());
        productRequestData.setLastEventUser(eventInfo.getEventUser());
        productRequestData.setLastEventTimeKey(eventInfo.getEventTimeKey());
        productRequestData.setProductRequestHoldState("N");
        productRequestData.setCompleteTime(null);
        productRequestData.setCompleteUser("");
        
        ProductRequestServiceProxy.getProductRequestService().update(productRequestData);
        MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(productRequestData);
	}
}
