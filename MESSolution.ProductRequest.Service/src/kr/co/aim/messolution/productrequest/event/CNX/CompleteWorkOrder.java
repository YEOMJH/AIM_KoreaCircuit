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
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
import kr.co.aim.greentrack.productrequest.management.info.MakeCompletedInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class CompleteWorkOrder extends SyncHandler {
	
	private static Log log = LogFactory.getLog(CompleteWorkOrder.class);

	public Object doWorks(Document doc) throws CustomException
	{
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		String productRequestType = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTTYPE", true);
		String checkFlag = SMessageUtil.getBodyItemValue(doc, "CHECKFLAG", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CompleteWorkOrder", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		if(StringUtils.equals(productRequestType, "M") || StringUtils.equals(productRequestType, "D") )
		{				
			ProductRequest productRequestData = ProductRequestServiceProxy.getProductRequestService().selectByKey(new ProductRequestKey(productRequestName));

			if (!StringUtils.equals(productRequestData.getProductRequestState(), GenericServiceProxy.getConstantMap().Prq_Released)
					&& !StringUtils.equals(productRequestData.getProductRequestState(), GenericServiceProxy.getConstantMap().Prq_Created))
				throw new CustomException("PRODUCTREQUEST-0004", productRequestData.getProductRequestState());

			if (productRequestData.getReleasedQuantity() != productRequestData.getFinishedQuantity() + productRequestData.getScrappedQuantity())
				throw new CustomException("PRODUCTREQUEST-0005", productRequestData.getReleasedQuantity(), productRequestData.getFinishedQuantity(), productRequestData.getScrappedQuantity());

			if (StringUtils.equals(productRequestData.getProductRequestHoldState(), GenericServiceProxy.getConstantMap().Prq_OnHold))
			{
				throw new CustomException("PRODUCTREQUEST-0024", productRequestData.getKey().getProductRequestName(), productRequestData.getProductRequestHoldState());
			}

			checkCreateLot(productRequestData);

			MakeCompletedInfo makeCompletedInfo = MESWorkOrderServiceProxy.getProductRequestInfoUtil().makeCompletedInfo(productRequestData);

			MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompleted(productRequestData, makeCompletedInfo, eventInfo);
		}
		else
		{			
			if(StringUtils.equals(checkFlag, "Y")){

				SuperProductRequest superProductRequestData = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{ productRequestName });
				
				try
				{
					List<ProductRequest> productRequestList = ProductRequestServiceProxy.getProductRequestService().select(" superProductRequestName = ? and productRequestState = ? ", new Object[] { productRequestName, GenericServiceProxy.getConstantMap().Prq_Released});	
					for(ProductRequest productRequestData : productRequestList)
					{
						if (productRequestData.getReleasedQuantity() != productRequestData.getFinishedQuantity() + productRequestData.getScrappedQuantity())
							throw new CustomException("PRODUCTREQUEST-0005", productRequestData.getReleasedQuantity(), productRequestData.getFinishedQuantity(), productRequestData.getScrappedQuantity());

						if (StringUtils.equals(productRequestData.getProductRequestHoldState(), GenericServiceProxy.getConstantMap().Prq_OnHold))
						{
							throw new CustomException("PRODUCTREQUEST-0024", productRequestData.getKey().getProductRequestName(), productRequestData.getProductRequestHoldState());
						}

						checkCreateLot(productRequestData);

						MakeCompletedInfo makeCompletedInfo = MESWorkOrderServiceProxy.getProductRequestInfoUtil().makeCompletedInfo(productRequestData);

						MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompleted(productRequestData, makeCompletedInfo, eventInfo);
					}
					
				}
				catch(NotFoundSignal n)
				{
					log.info("Not found Released ProductRequestList");
				}
					
				List<ProductRequest> notCompleteProductRequestList = null;
				
				try 
				{
					notCompleteProductRequestList = ProductRequestServiceProxy.getProductRequestService().select(" superProductRequestName = ? and productRequestState != ? ", new Object[] { productRequestName, GenericServiceProxy.getConstantMap().Prq_Completed});
//					notCompleteProductRequestList = ProductRequestServiceProxy.getProductRequestService().select(" superProductRequestName = ?  ", new Object[] { productRequestName});
				} 
				catch (Exception e) 
				{}
						
				
				if(notCompleteProductRequestList != null && notCompleteProductRequestList.size() > 0)
				{
					throw new CustomException("PRODUCTREQUEST-0004", notCompleteProductRequestList.get(0).getProductRequestState());
				}
				
				if (StringUtils.equals(superProductRequestData.getProductRequestHoldState(), "Y"))
					throw new CustomException("WORKORDER-0004", superProductRequestData.getProductRequestName(), superProductRequestData.getProductRequestHoldState());
				
				superProductRequestData.setProductRequestState(GenericServiceProxy.getConstantMap().Prq_Completed);
				superProductRequestData.setLastEventName(eventInfo.getEventName());
				superProductRequestData.setLastEventComment(eventInfo.getEventComment());
				superProductRequestData.setLastEventFlag("N");
				superProductRequestData.setLastEventTime(eventInfo.getEventTime());
				superProductRequestData.setLastEventTimeKey(eventInfo.getEventTimeKey());
				superProductRequestData.setLastEventUser(eventInfo.getEventUser());
				superProductRequestData.setCompleteTime(eventInfo.getEventTime());
				superProductRequestData.setCompleteUser(eventInfo.getEventUser());
				
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
						dataInfoMap.put("ZCANCEL", "");
						
						dataInfoMapList.add(dataInfoMap);
						
						String resultCode=ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().workOrderClose(eventInfo, dataInfoMapList, 1);
						if(!resultCode.equals("S"))
						{
							//SYSTEM-0014:Close SAPWO Failed
							throw new CustomException("SYSTEM-0014");
						}
					}					
				}
				catch(CustomException cs)
				{
					//SYSTEM-0014:Close SAPWO Failed
					throw new CustomException("SYSTEM-0014");
				}
				catch(Exception ex)
				{
					//SYSTEM-0015:Send To SAP Failed
					throw new CustomException("SYSTEM-0015");
				}
			
			}
			else
			{
				
				ProductRequest productRequestData = ProductRequestServiceProxy.getProductRequestService().selectByKey(new ProductRequestKey(productRequestName));

				if (!StringUtils.equals(productRequestData.getProductRequestState(), GenericServiceProxy.getConstantMap().Prq_Released)
						&& !StringUtils.equals(productRequestData.getProductRequestState(), GenericServiceProxy.getConstantMap().Prq_Created))
					throw new CustomException("PRODUCTREQUEST-0004", productRequestData.getProductRequestState());

				if (productRequestData.getReleasedQuantity() != productRequestData.getFinishedQuantity() + productRequestData.getScrappedQuantity())
					throw new CustomException("PRODUCTREQUEST-0005", productRequestData.getReleasedQuantity(), productRequestData.getFinishedQuantity(), productRequestData.getScrappedQuantity());

				if (StringUtils.equals(productRequestData.getProductRequestHoldState(), GenericServiceProxy.getConstantMap().Prq_OnHold))
				{
					throw new CustomException("PRODUCTREQUEST-0024", productRequestData.getKey().getProductRequestName(), productRequestData.getProductRequestHoldState());
				}

				checkCreateLot(productRequestData);

				MakeCompletedInfo makeCompletedInfo = MESWorkOrderServiceProxy.getProductRequestInfoUtil().makeCompletedInfo(productRequestData);

				MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompleted(productRequestData, makeCompletedInfo, eventInfo);
			}
			
		}
		
		return doc;
	}

	private void checkCreateLot(ProductRequest productRequestData) throws CustomException
	{
		String condition = " PRODUCTREQUESTNAME = ? AND LOTSTATE = 'Created'";
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
			throw new CustomException("PRODUCTREQUEST-0034");
		}
	}

}
