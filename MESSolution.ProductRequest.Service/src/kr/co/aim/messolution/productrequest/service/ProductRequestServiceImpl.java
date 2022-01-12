package kr.co.aim.messolution.productrequest.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ProductRequestHistory;
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.productrequest.ProductRequestServiceProxy;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequestKey;
import kr.co.aim.greentrack.productrequest.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.productrequest.management.info.CreateInfo;
import kr.co.aim.greentrack.productrequest.management.info.DecrementScrappedQuantityByInfo;
import kr.co.aim.greentrack.productrequest.management.info.IncrementFinishedQuantityByInfo;
import kr.co.aim.greentrack.productrequest.management.info.IncrementReleasedQuantityByInfo;
import kr.co.aim.greentrack.productrequest.management.info.IncrementScrappedQuantityByInfo;
import kr.co.aim.greentrack.productrequest.management.info.MakeCompletedInfo;
import kr.co.aim.greentrack.productrequest.management.info.MakeNotOnHoldInfo;
import kr.co.aim.greentrack.productrequest.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.productrequest.management.info.MakeReleasedInfo;

public class ProductRequestServiceImpl implements ApplicationContextAware {

	private ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog("ProductRequestServiceImpl");

	public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		applicationContext = arg0;
	}

	public ProductRequest create(EventInfo eventInfo, CreateInfo createInfo, String productRequestName) throws CustomException
	{

		log.info("Execute ProductRequest create.");

		if (log.isInfoEnabled())
		{
			log.info("productRequestName = " + productRequestName);

		}
		ProductRequestKey productRequestKey = new ProductRequestKey();
		productRequestKey.setProductRequestName(productRequestName);

		ProductRequest productRequest = null;
		try
		{
			// productRequest = ProductRequestServiceProxy.getProductRequestService().create(productRequestKey, eventInfo, createInfo);
			productRequest = ProductRequestServiceProxy.getProductRequestService().create(eventInfo, createInfo);

			MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(productRequest);

			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());

		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("PRODUCTREQUEST-9003", productRequestName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("PRODUCTREQUEST-9999", fe.getMessage());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("PRODUCTREQUEST-9002", productRequestName);
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("PRODUCTREQUEST-9001", productRequestName);
		}

		return productRequest;

	}

	public void makeReleased(EventInfo eventInfo, String productRequestName) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal,
			CustomException
	{
		log.info("Execute makeReleased");

		if (log.isInfoEnabled())
		{
			log.info("productRequestName = " + productRequestName);
		}

		ProductRequestKey productRequestKey = new ProductRequestKey();
		productRequestKey.setProductRequestName(productRequestName);

		// MakeReleasedInfo
		MakeReleasedInfo makeReleasedInfo = new MakeReleasedInfo();

		ProductRequest productRequestData = ProductRequestServiceProxy.getProductRequestService().makeReleased(productRequestKey, eventInfo, makeReleasedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());

		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(productRequestData);
	}

	public ProductRequest makeCompleted(EventInfo eventInfo, String productRequestName) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal,
			DuplicateNameSignal, CustomException
	{
		log.info("Execute makeCompleted");

		if (log.isInfoEnabled())
		{
			log.info("productRequestName = " + productRequestName);
		}

		ProductRequestKey productRequestKey = new ProductRequestKey();
		productRequestKey.setProductRequestName(productRequestName);

		MakeCompletedInfo makeCompletedInfo = new MakeCompletedInfo();
		ProductRequest productRequestData = ProductRequestServiceProxy.getProductRequestService().makeCompleted(productRequestKey, eventInfo, makeCompletedInfo);
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(productRequestData);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());

		return productRequestData;
	}

	public ProductRequest changeSpec(ProductRequest productRequestData, ChangeSpecInfo changeSpecInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal,
			DuplicateNameSignal, CustomException
	{

		log.info("Execute changeSpec");

		if (log.isInfoEnabled())
		{
			log.info("productRequestName = " + productRequestData.getKey().getProductRequestName());
		}

		ProductRequestKey productRequestKey = new ProductRequestKey();
		productRequestKey.setProductRequestName(productRequestData.getKey().getProductRequestName());

		ProductRequest requestData = ProductRequestServiceProxy.getProductRequestService().changeSpec(productRequestKey, eventInfo, changeSpecInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());

		// Add History
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(requestData);

		return requestData;
	}

	public ProductRequest decrementScrappedQuantityBy(ProductRequest productRequestData, DecrementScrappedQuantityByInfo decrementScrappedQuantityByInfo, EventInfo eventInfo)
			throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
	{
		productRequestData = ProductRequestServiceProxy.getProductRequestService().decrementScrappedQuantityBy(productRequestData.getKey(), eventInfo, decrementScrappedQuantityByInfo);

		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(productRequestData);

		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());

		if(StringUtil.isNotEmpty(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
		{
			SuperProductRequest superProductRequest = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
			superProductRequest.setScrappedQuantity(superProductRequest.getScrappedQuantity() - decrementScrappedQuantityByInfo.getQuantity());
			superProductRequest.setLastEventName(eventInfo.getEventName());
			superProductRequest.setLastEventComment(eventInfo.getEventComment());
			superProductRequest.setLastEventTime(eventInfo.getEventTime());
			superProductRequest.setLastEventTimeKey(eventInfo.getEventTimeKey());
			superProductRequest.setLastEventUser(eventInfo.getEventUser());
			
			ExtendedObjectProxy.getSuperProductRequestService().modify(eventInfo, superProductRequest);
		}
		
		return productRequestData;
	}

	public ProductRequest incrementFinishedQuantityBy(ProductRequest productRequestData, IncrementFinishedQuantityByInfo incrementFinishedQuantityByInfo, EventInfo eventInfo)
			throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
	{
		productRequestData = ProductRequestServiceProxy.getProductRequestService().incrementFinishedQuantityBy(productRequestData.getKey(), eventInfo, incrementFinishedQuantityByInfo);

		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(productRequestData);

		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());

		if(StringUtil.isNotEmpty(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
		{
			SuperProductRequest superProductRequest = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
			superProductRequest.setFinishedQuantity(superProductRequest.getFinishedQuantity() + incrementFinishedQuantityByInfo.getQuantity());
			superProductRequest.setLastEventName(eventInfo.getEventName());
			superProductRequest.setLastEventComment(eventInfo.getEventComment());
			superProductRequest.setLastEventTime(eventInfo.getEventTime());
			superProductRequest.setLastEventTimeKey(eventInfo.getEventTimeKey());
			superProductRequest.setLastEventUser(eventInfo.getEventUser());
			
			ExtendedObjectProxy.getSuperProductRequestService().modify(eventInfo, superProductRequest);
		}
		
		return productRequestData;
	}
	
	public ProductRequest incrementFinishedQuantityBy_Lock(ProductRequest productRequestData, IncrementFinishedQuantityByInfo incrementFinishedQuantityByInfo, EventInfo eventInfo)
			throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
	{
		productRequestData = ProductRequestServiceProxy.getProductRequestService().incrementFinishedQuantityBy(productRequestData.getKey(), eventInfo, incrementFinishedQuantityByInfo);

		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(productRequestData);

		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());

		if(StringUtil.isNotEmpty(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
		{
			SuperProductRequest superProductRequest = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(true, new Object[]{productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
			superProductRequest.setFinishedQuantity(superProductRequest.getFinishedQuantity() + incrementFinishedQuantityByInfo.getQuantity());
			superProductRequest.setLastEventName(eventInfo.getEventName());
			superProductRequest.setLastEventComment(eventInfo.getEventComment());
			superProductRequest.setLastEventTime(eventInfo.getEventTime());
			superProductRequest.setLastEventTimeKey(eventInfo.getEventTimeKey());
			superProductRequest.setLastEventUser(eventInfo.getEventUser());
			
			ExtendedObjectProxy.getSuperProductRequestService().modify(eventInfo, superProductRequest);
		}
		
		return productRequestData;
	}
	
	public ProductRequest incrementReleasedQuantityBy(ProductRequest productRequestData, IncrementReleasedQuantityByInfo incrementReleasedQuantityByInfo, EventInfo eventInfo)
			throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
	{
		productRequestData = ProductRequestServiceProxy.getProductRequestService().incrementReleasedQuantityBy(productRequestData.getKey(), eventInfo, incrementReleasedQuantityByInfo);

		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(productRequestData);

		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());

		if(StringUtil.isNotEmpty(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
		{
			SuperProductRequest superProductRequest = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
			superProductRequest.setReleasedQuantity(superProductRequest.getReleasedQuantity() + incrementReleasedQuantityByInfo.getQuantity());
			superProductRequest.setLastEventName(eventInfo.getEventName());
			superProductRequest.setLastEventComment(eventInfo.getEventComment());
			superProductRequest.setLastEventTime(eventInfo.getEventTime());
			superProductRequest.setLastEventTimeKey(eventInfo.getEventTimeKey());
			superProductRequest.setLastEventUser(eventInfo.getEventUser());
			
			ExtendedObjectProxy.getSuperProductRequestService().modify(eventInfo, superProductRequest);
		}
		
		return productRequestData;
	}
	
	public ProductRequest incrementReleasedQuantityBy_Lock(ProductRequest productRequestData, IncrementReleasedQuantityByInfo incrementReleasedQuantityByInfo, EventInfo eventInfo)
			throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
	{
		productRequestData = ProductRequestServiceProxy.getProductRequestService().incrementReleasedQuantityBy(productRequestData.getKey(), eventInfo, incrementReleasedQuantityByInfo);

		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(productRequestData);

		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());

		if(StringUtil.isNotEmpty(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
		{
			SuperProductRequest superProductRequest = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(true, new Object[]{productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
			superProductRequest.setReleasedQuantity(superProductRequest.getReleasedQuantity() + incrementReleasedQuantityByInfo.getQuantity());
			superProductRequest.setLastEventName(eventInfo.getEventName());
			superProductRequest.setLastEventComment(eventInfo.getEventComment());
			superProductRequest.setLastEventTime(eventInfo.getEventTime());
			superProductRequest.setLastEventTimeKey(eventInfo.getEventTimeKey());
			superProductRequest.setLastEventUser(eventInfo.getEventUser());
			
			ExtendedObjectProxy.getSuperProductRequestService().modify(eventInfo, superProductRequest);
		}
		
		return productRequestData;
	}
	
	public ProductRequest incrementScrappedQuantityBy(ProductRequest productRequestData, IncrementScrappedQuantityByInfo incrementScrappedQuantityByInfo, EventInfo eventInfo)
			throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
	{
		productRequestData = ProductRequestServiceProxy.getProductRequestService().incrementScrappedQuantityBy(productRequestData.getKey(), eventInfo, incrementScrappedQuantityByInfo);

		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(productRequestData);

		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());

		if(StringUtil.isNotEmpty(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
		{
			SuperProductRequest superProductRequest = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
			superProductRequest.setScrappedQuantity(superProductRequest.getScrappedQuantity() + incrementScrappedQuantityByInfo.getQuantity());
			superProductRequest.setLastEventName(eventInfo.getEventName());
			superProductRequest.setLastEventComment(eventInfo.getEventComment());
			superProductRequest.setLastEventTime(eventInfo.getEventTime());
			superProductRequest.setLastEventTimeKey(eventInfo.getEventTimeKey());
			superProductRequest.setLastEventUser(eventInfo.getEventUser());
			
			ExtendedObjectProxy.getSuperProductRequestService().modify(eventInfo, superProductRequest);
		}
		
		return productRequestData;
	}

	public ProductRequest incrementCreatedQuantityBy(ProductRequest productRequestData, int increamentQuantity, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal,
			NotFoundSignal, DuplicateNameSignal, CustomException
	{
		int createQuantity = Integer.parseInt(productRequestData.getUdfs().get("CREATEDQUANTITY"));

		ChangeSpecInfo changeSpecInfo = MESWorkOrderServiceProxy.getProductRequestInfoUtil().changeSpecInfo(productRequestData.getFactoryName(), productRequestData.getProductRequestType(),
				productRequestData.getProductSpecName(), productRequestData.getProductSpecVersion(), productRequestData.getPlanFinishedTime(), productRequestData.getPlanReleasedTime(),
				productRequestData.getPlanQuantity(), productRequestData.getReleasedQuantity(), productRequestData.getFinishedQuantity(), productRequestData.getScrappedQuantity(),
				productRequestData.getProductRequestState(), productRequestData.getProductRequestHoldState(), productRequestData.getUdfs().get("PROCESSFLOWNAME"),
				productRequestData.getUdfs().get("PROCESSFLOWVERSION"), productRequestData.getUdfs().get("AUTOSHIPPINGFLAG"), productRequestData.getUdfs().get("PLANSEQUENCE"),
				Integer.toString(createQuantity + increamentQuantity), productRequestData.getUdfs().get("DESCRIPTION"));

		productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().changeSpec(productRequestData, changeSpecInfo, eventInfo);

		if (productRequestData.getPlanQuantity() < Integer.parseInt(productRequestData.getUdfs().get("CREATEDQUANTITY")))
		{
			throw new CustomException("PRODUCTREQUEST-0030");
		}

		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());

		if(StringUtil.isNotEmpty(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
		{
			SuperProductRequest superProductRequest = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
			superProductRequest.setCreatedQuantity(superProductRequest.getCreatedQuantity() + increamentQuantity);
			superProductRequest.setLastEventName(eventInfo.getEventName());
			superProductRequest.setLastEventComment(eventInfo.getEventComment());
			superProductRequest.setLastEventTime(eventInfo.getEventTime());
			superProductRequest.setLastEventTimeKey(eventInfo.getEventTimeKey());
			superProductRequest.setLastEventUser(eventInfo.getEventUser());
			
			ExtendedObjectProxy.getSuperProductRequestService().modify(eventInfo, superProductRequest);
		}
		
		return productRequestData;
	}

	public ProductRequest DecrementCreatedQuantityBy(ProductRequest productRequestData, int DecreamentQuantity, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal,
			NotFoundSignal, DuplicateNameSignal, CustomException
	{
		int createQuantity = Integer.parseInt(productRequestData.getUdfs().get("CREATEDQUANTITY"));

		ChangeSpecInfo changeSpecInfo = MESWorkOrderServiceProxy.getProductRequestInfoUtil().changeSpecInfo(productRequestData.getFactoryName(), productRequestData.getProductRequestType(),
				productRequestData.getProductSpecName(), productRequestData.getProductSpecVersion(), productRequestData.getPlanFinishedTime(), productRequestData.getPlanReleasedTime(),
				productRequestData.getPlanQuantity(), productRequestData.getReleasedQuantity(), productRequestData.getFinishedQuantity(), productRequestData.getScrappedQuantity(),
				productRequestData.getProductRequestState(), productRequestData.getProductRequestHoldState(), productRequestData.getUdfs().get("PROCESSFLOWNAME"),
				productRequestData.getUdfs().get("PROCESSFLOWVERSION"), productRequestData.getUdfs().get("AUTOSHIPPINGFLAG"), productRequestData.getUdfs().get("PLANSEQUENCE"),
				Integer.toString(createQuantity - DecreamentQuantity), productRequestData.getUdfs().get("DESCRIPTION"));

		productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().changeSpec(productRequestData, changeSpecInfo, eventInfo);

		if(StringUtil.isNotEmpty(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
		{
			SuperProductRequest superProductRequest = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
			superProductRequest.setCreatedQuantity(superProductRequest.getCreatedQuantity() - DecreamentQuantity);
			superProductRequest.setLastEventName(eventInfo.getEventName());
			superProductRequest.setLastEventComment(eventInfo.getEventComment());
			superProductRequest.setLastEventTime(eventInfo.getEventTime());
			superProductRequest.setLastEventTimeKey(eventInfo.getEventTimeKey());
			superProductRequest.setLastEventUser(eventInfo.getEventUser());
			
			ExtendedObjectProxy.getSuperProductRequestService().modify(eventInfo, superProductRequest);
		}
		
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());

		return productRequestData;
	}

	public ProductRequest makeCompleted(ProductRequest productRequestData, MakeCompletedInfo makeCompletedInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal,
			NotFoundSignal, DuplicateNameSignal, CustomException
	{
		ProductRequest productRequest = ProductRequestServiceProxy.getProductRequestService().makeCompleted(productRequestData.getKey(), eventInfo, makeCompletedInfo);
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(productRequest);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());

		return productRequest;
	}

	public void makeNotOnHold(ProductRequest productRequestData, MakeNotOnHoldInfo makeNotOnHoldInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal,
			DuplicateNameSignal, CustomException
	{
		ProductRequest productRequest = ProductRequestServiceProxy.getProductRequestService().makeNotOnHold(productRequestData.getKey(), eventInfo, makeNotOnHoldInfo);
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(productRequest);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void makeOnHold(ProductRequest productRequestData, MakeOnHoldInfo makeOnHoldInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal,
			DuplicateNameSignal, CustomException
	{
		ProductRequest productRequest = ProductRequestServiceProxy.getProductRequestService().makeOnHold(productRequestData.getKey(), eventInfo, makeOnHoldInfo);
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(productRequest);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public ProductRequest makeReleased(ProductRequest productRequestData, MakeReleasedInfo makeReleasedInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal,
			NotFoundSignal, DuplicateNameSignal, CustomException
	{
		ProductRequest productRequest = ProductRequestServiceProxy.getProductRequestService().makeReleased(productRequestData.getKey(), eventInfo, makeReleasedInfo);

		// Add History
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(productRequest);

		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());

		return productRequest;
	}

	public void remove(EventInfo eventInfo, ProductRequest productRequestData) throws CustomException
	{
		// Add ProductRequest History
		productRequestData.setLastEventComment(eventInfo.getEventComment());
		productRequestData.setLastEventName(eventInfo.getEventName());
		productRequestData.setLastEventTime(eventInfo.getEventTime());
		productRequestData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		productRequestData.setLastEventUser(eventInfo.getEventUser());
		MESWorkOrderServiceProxy.getProductRequestServiceUtil().addHistory(productRequestData);

		// ProductRequest Delete
		ProductRequestServiceProxy.getProductRequestService().delete(productRequestData.getKey());
	}

	public void addHistory(ProductRequestHistory historyData) throws CustomException
	{
		ExtendedObjectProxy.getProductRequestHistoryService().insert(historyData);
	}

	public void addHistory(List<ProductRequestHistory> historyDataList) throws CustomException
	{
		ExtendedObjectProxy.getProductRequestHistoryService().insert(historyDataList);
	}

	public ProductRequest ChangeScrapQty(EventInfo eventInfo, String workOrderName, long incrementQty, long decrementQty) throws CustomException
	{
		ProductRequest workOrderData = ProductRequestServiceProxy.getProductRequestService().selectByKey(new ProductRequestKey(workOrderName));

		ProductRequest newProductRequestData = new ProductRequest();

		if (incrementQty == 0 && decrementQty != 0)
		{
			long newScrapQty = workOrderData.getScrappedQuantity() - decrementQty;
			if (newScrapQty < 0)
			{
				throw new CustomException("PRODUCTREQUEST-0025", workOrderName);
			}

			// Decrease Scrapped Quantity
			DecrementScrappedQuantityByInfo decrementScrappedQuantityByInfo = MESWorkOrderServiceProxy.getProductRequestInfoUtil().decrementScrappedQuantityByInfo(workOrderData, decrementQty);
			newProductRequestData = decrementScrappedQuantityBy(workOrderData, decrementScrappedQuantityByInfo, eventInfo);
		}
		if (decrementQty == 0 && incrementQty != 0)
		{
			// Increase Scrapped Quantity
			IncrementScrappedQuantityByInfo incrementScrappedQuantityByInfo = MESWorkOrderServiceProxy.getProductRequestInfoUtil().incrementScrappedQuantityByInfo(workOrderData, incrementQty);
			newProductRequestData = incrementScrappedQuantityBy(workOrderData, incrementScrappedQuantityByInfo, eventInfo);
		}
		if (decrementQty == 0 && incrementQty == 0)
		{
			throw new CustomException("PRODUCTREQUEST-0025", workOrderName);
		}

		return newProductRequestData;
	}
	
	public void incrementQuantity(EventInfo eventInfo, Lot lotData, int incrementQty) throws CustomException
	{
		ProductRequest workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());

		IncrementReleasedQuantityByInfo incrementReleasedQuantityByInfo = new IncrementReleasedQuantityByInfo();
		incrementReleasedQuantityByInfo.setQuantity(incrementQty);

		int createdQty = Integer.parseInt(workOrderData.getUdfs().get("CREATEDQUANTITY")) + incrementQty;

		incrementReleasedQuantityByInfo.getUdfs().put("CREATEDQUANTITY", Integer.toString(createdQty));

		// Increment Release Qty
		eventInfo.setEventName("IncreamentQuantity");
		workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementReleasedQuantityBy(workOrderData, incrementReleasedQuantityByInfo, eventInfo);

		if (workOrderData.getPlanQuantity() < workOrderData.getReleasedQuantity())
			throw new CustomException("PRODUCTREQUEST-0026", String.valueOf(workOrderData.getPlanQuantity()), String.valueOf(workOrderData.getReleasedQuantity()));
	}

	public void decrementQuantity(EventInfo eventInfo, Lot lotData, int incrementQty) throws CustomException
	{
		ProductRequest workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());

		IncrementReleasedQuantityByInfo incrementReleasedQuantityByInfo = new IncrementReleasedQuantityByInfo();
		incrementReleasedQuantityByInfo.setQuantity(-incrementQty);

		int createdQty = Integer.parseInt(workOrderData.getUdfs().get("CREATEDQUANTITY")) - incrementQty;

		incrementReleasedQuantityByInfo.getUdfs().put("CREATEDQUANTITY", Integer.toString(createdQty));

		// Increment Release Qty
		eventInfo.setEventName("DecreamentQuantity");
		workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementReleasedQuantityBy(workOrderData, incrementReleasedQuantityByInfo, eventInfo);
	}
	
}