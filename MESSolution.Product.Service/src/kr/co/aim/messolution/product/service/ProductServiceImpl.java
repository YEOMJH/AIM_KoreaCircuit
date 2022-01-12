package kr.co.aim.messolution.product.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.SetMaterialLocationInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.name.NameServiceProxy;
import kr.co.aim.greentrack.name.management.data.NameGeneratorRuleAttrDef;
import kr.co.aim.greentrack.name.management.data.NameGeneratorRuleAttrDefKey;
import kr.co.aim.greentrack.name.management.data.NameGeneratorSerialKey;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductMultiHold;
import kr.co.aim.greentrack.product.management.data.ProductMultiHoldKey;
import kr.co.aim.greentrack.product.management.info.AssignCarrierInfo;
import kr.co.aim.greentrack.product.management.info.AssignLotAndCarrierInfo;
import kr.co.aim.greentrack.product.management.info.AssignLotInfo;
import kr.co.aim.greentrack.product.management.info.AssignProcessGroupInfo;
import kr.co.aim.greentrack.product.management.info.AssignTransportGroupInfo;
import kr.co.aim.greentrack.product.management.info.ChangeGradeInfo;
import kr.co.aim.greentrack.product.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.product.management.info.ConsumeMaterialsInfo;
import kr.co.aim.greentrack.product.management.info.CreateInfo;
import kr.co.aim.greentrack.product.management.info.CreateRawInfo;
import kr.co.aim.greentrack.product.management.info.CreateWithLotInfo;
import kr.co.aim.greentrack.product.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.product.management.info.DeassignLotAndCarrierInfo;
import kr.co.aim.greentrack.product.management.info.DeassignLotInfo;
import kr.co.aim.greentrack.product.management.info.DeassignProcessGroupInfo;
import kr.co.aim.greentrack.product.management.info.DeassignTransportGroupInfo;
import kr.co.aim.greentrack.product.management.info.MakeAllocatedInfo;
import kr.co.aim.greentrack.product.management.info.MakeCompletedInfo;
import kr.co.aim.greentrack.product.management.info.MakeConsumedInfo;
import kr.co.aim.greentrack.product.management.info.MakeIdleInfo;
import kr.co.aim.greentrack.product.management.info.MakeInProductionInfo;
import kr.co.aim.greentrack.product.management.info.MakeInReworkInfo;
import kr.co.aim.greentrack.product.management.info.MakeNotOnHoldInfo;
import kr.co.aim.greentrack.product.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.product.management.info.MakeProcessingInfo;
import kr.co.aim.greentrack.product.management.info.MakeReceivedInfo;
import kr.co.aim.greentrack.product.management.info.MakeScrappedInfo;
import kr.co.aim.greentrack.product.management.info.MakeShippedInfo;
import kr.co.aim.greentrack.product.management.info.MakeTravelingInfo;
import kr.co.aim.greentrack.product.management.info.MakeUnScrappedInfo;
import kr.co.aim.greentrack.product.management.info.MakeUnShippedInfo;
import kr.co.aim.greentrack.product.management.info.RecreateInfo;
import kr.co.aim.greentrack.product.management.info.SeparateInfo;
import kr.co.aim.greentrack.product.management.info.SetAreaInfo;
import kr.co.aim.greentrack.product.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.management.info.UndoInfo;

public class ProductServiceImpl implements ApplicationContextAware {
	
	
	private ApplicationContext     	applicationContext;
	private static Log log = LogFactory.getLog(ProductServiceImpl.class);

	
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {

	
		applicationContext = arg0;
	}
	 
	
	private List<String> generateProduct(String ruleName, String quantity, String lotName) throws FrameworkErrorSignal, NotFoundSignal
	{		
		 List<String> argSeq = new ArrayList<String>();
		 argSeq.add(lotName);
		 
		 List<String> names = null;
		 names = NameServiceProxy.getNameGeneratorRuleDefService().generateName(ruleName, argSeq, Long.valueOf(quantity));
		 
		 NameGeneratorRuleAttrDefKey keyInfo = new NameGeneratorRuleAttrDefKey();
		 keyInfo.setRuleName(ruleName);
		 keyInfo.setPosition(0); // Argument
		 NameGeneratorRuleAttrDef attrDef = NameServiceProxy.getNameGeneratorRuleAttrDefService().selectByKey(keyInfo);
		 int length = Long.valueOf(attrDef.getSectionLength()).intValue();
		  
		 NameGeneratorSerialKey key = new NameGeneratorSerialKey();
		 key.setRuleName(ruleName);
		 key.setPrefix(lotName.substring(0, length));
				 
		 NameServiceProxy.getNameGeneratorSerialService().delete(key);
		 
		 return names;		 
	}

	
	public void assignCarrier( Product productData, 
			AssignCarrierInfo assignCarrierInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().assignCarrier(productData.getKey(), eventInfo, assignCarrierInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void assignLot( Product productData, 
			AssignLotInfo assignLotInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().assignLot(productData.getKey(), eventInfo, assignLotInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void assignLotAndCarrier( Product productData, 
			AssignLotAndCarrierInfo assignLotAndCarrierInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().assignLotAndCarrier(productData.getKey(), eventInfo, assignLotAndCarrierInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void assignProcessGroup( Product productData, 
			AssignProcessGroupInfo assignProcessGroupInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().assignProcessGroup(productData.getKey(), eventInfo, assignProcessGroupInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void assignTransportGroup( Product productData, 
			AssignTransportGroupInfo assignTransportGroupInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().assignTransportGroup(productData.getKey(), eventInfo, assignTransportGroupInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public Product changeGrade(Product productData, ChangeGradeInfo changeGradeInfo, EventInfo eventInfo)
		throws CustomException
	{
		try
		{
			productData = ProductServiceProxy.getProductService().changeGrade(productData.getKey(), eventInfo, changeGradeInfo);
			log.info("EventName=" + eventInfo.getEventName() + " EventTimeKey=" + eventInfo.getEventTimeKey());
		}
		catch (NotFoundSignal e) 
	    {
	    	throw new CustomException("PRODUCT-9001", productData.getKey().getProductName());	
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("PRODUCT-9002", productData.getKey().getProductName());	
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("PRODUCT-9003", productData.getKey().getProductName());	
		}
	    catch (FrameworkErrorSignal fe)
	    {
	    	throw new CustomException("PRODUCT-9999", fe.getMessage());	
	    }
	    
	    return productData;
	}

	
	public void changeSpec(EventInfo eventInfo, Product productData, ChangeSpecInfo changeSpecInfo)
		throws CustomException
	{
		try
		{
			ProductServiceProxy.getProductService().changeSpec(productData.getKey(), eventInfo, changeSpecInfo);
			
			log.info(String.format("EventName[%s] EventTimeKey[%s]", eventInfo.getEventName(), eventInfo.getEventTimeKey()));
		}
		catch (NotFoundSignal e) 
	    {
	    	throw new CustomException("PRODUCT-9001", productData.getKey().getProductName());	
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("PRODUCT-9002", productData.getKey().getProductName());	
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("PRODUCT-9003", productData.getKey().getProductName());	
		}
	    catch (FrameworkErrorSignal fe)
	    {
	    	throw new CustomException("PRODUCT-9999", fe.getMessage());	
	    }
	}

	
	public void create( Product productData, 
			CreateInfo createInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().create(productData.getKey(), eventInfo, createInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void createRaw( Product productData, 
			CreateRawInfo createRawInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().createRaw(productData.getKey(), eventInfo, createRawInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void createWithLot( Product productData, 
			CreateWithLotInfo createWithLotInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().createWithLot(productData.getKey(), eventInfo, createWithLotInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void consumeMaterials( Product productData, 
			ConsumeMaterialsInfo consumeMaterialsInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().consumeMaterials(productData.getKey(), eventInfo, consumeMaterialsInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void deassignCarrier( Product productData, 
			DeassignCarrierInfo deassignCarrierInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().deassignCarrier(productData.getKey(), eventInfo, deassignCarrierInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void deassignLot( Product productData, 
			DeassignLotInfo deassignLotInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().deassignLot(productData.getKey(), eventInfo, deassignLotInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public Product deassignLotAndCarrier( Product productData, 
			DeassignLotAndCarrierInfo deassignLotAndCarrierInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		productData = ProductServiceProxy.getProductService().deassignLotAndCarrier(productData.getKey(), eventInfo, deassignLotAndCarrierInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		
		return productData;
	}

	
	public void deassignProcessGroup( Product productData, 
			DeassignProcessGroupInfo deassignProcessGroupInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().deassignProcessGroup(productData.getKey(), eventInfo, deassignProcessGroupInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void deassignTransportGroup( Product productData, 
			DeassignTransportGroupInfo deassignTransportGroupInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().deassignTransportGroup(productData.getKey(), eventInfo, deassignTransportGroupInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void makeAllocated( Product productData, 
			MakeAllocatedInfo makeAllocatedInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeAllocated(productData.getKey(), eventInfo, makeAllocatedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void makeCompleted( Product productData, 
			MakeCompletedInfo makeCompletedInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeCompleted(productData.getKey(), eventInfo, makeCompletedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void makeConsumed( Product productData, 
			MakeConsumedInfo makeConsumedInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeConsumed(productData.getKey(), eventInfo, makeConsumedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void makeIdle( Product productData, 
			MakeIdleInfo makeIdleInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeIdle(productData.getKey(), eventInfo, makeIdleInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void makeInProduction( Product productData, 
			MakeInProductionInfo makeInProductionInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeInProduction(productData.getKey(), eventInfo, makeInProductionInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void makeInRework( Product productData, 
			MakeInReworkInfo makeInReworkInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeInRework(productData.getKey(), eventInfo, makeInReworkInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void makeNotOnHold( Product productData, 
			MakeNotOnHoldInfo makeNotOnHoldInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeNotOnHold(productData.getKey(), eventInfo, makeNotOnHoldInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void makeOnHold( Product productData, 
			MakeOnHoldInfo makeOnHoldInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeOnHold(productData.getKey(), eventInfo, makeOnHoldInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void makeProcessing( Product productData, 
			MakeProcessingInfo makeProcessingInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeProcessing(productData.getKey(), eventInfo, makeProcessingInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void makeReceived( Product productData, 
			MakeReceivedInfo makeReceivedInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeReceived(productData.getKey(), eventInfo, makeReceivedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void makeScrapped( Product productData, 
			MakeScrappedInfo makeScrappedInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeScrapped(productData.getKey(), eventInfo, makeScrappedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void makeShipped( Product productData, 
			MakeShippedInfo makeShippedInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeShipped(productData.getKey(), eventInfo, makeShippedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void makeTraveling( Product productData, 
			MakeTravelingInfo makeTravelingInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeTraveling(productData.getKey(), eventInfo, makeTravelingInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void makeUnScrapped( Product productData, 
			MakeUnScrappedInfo makeUnScrappedInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeUnScrapped(productData.getKey(), eventInfo, makeUnScrappedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void makeUnShipped( Product productData, 
			MakeUnShippedInfo makeUnShippedInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().makeUnShipped(productData.getKey(), eventInfo, makeUnShippedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void recreate( Product productData, 
			RecreateInfo recreateInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().recreate(productData.getKey(), eventInfo, recreateInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void separate( Product productData, 
			SeparateInfo separateInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		
		ProductServiceProxy.getProductService().separate(productData.getKey(), eventInfo, separateInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void setArea( Product productData, 
			SetAreaInfo setAreaInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().setArea(productData.getKey(), eventInfo, setAreaInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void setEvent( Product productData, 
			SetEventInfo setEventInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().setEvent(productData.getKey(), eventInfo, setEventInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	
	public void setMaterialLocation(EventInfo eventInfo, Product productData, SetMaterialLocationInfo setMaterialLocationInfo)
		throws CustomException
	{
		try
		{
			ProductServiceProxy.getProductService().setMaterialLocation(productData.getKey(), eventInfo, setMaterialLocationInfo);
			
			log.info(String.format("EventName[%s] EventTimeKey[%s]", eventInfo.getEventName(), eventInfo.getEventTimeKey()));
		}
		catch (NotFoundSignal e) 
	    {
	    	throw new CustomException("PRODUCT-9001", productData.getKey().getProductName());	
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("PRODUCT-9002", productData.getKey().getProductName());	
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("PRODUCT-9003", productData.getKey().getProductName());	
		}
	    catch (FrameworkErrorSignal fe)
	    {
	    	throw new CustomException("PRODUCT-9999", fe.getMessage());	
	    }
	}

	
	public void undo( Product productData, 
			UndoInfo undoInfo,
			EventInfo eventInfo	)
	throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{
		ProductServiceProxy.getProductService().undo(productData.getKey(), eventInfo, undoInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public Product CancelReceiveProduct(EventInfo eventInfo, Product productData) throws CustomException
	{
		ProductServiceProxy.getProductService().update(productData);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
		return productData;
	}

	public void setProductMultiHold(EventInfo eventInfo, String lotName, Map<String, String> udfs) throws CustomException
	{
		List<Product> sequence = null;
		try
		{
			sequence = LotServiceProxy.getLotService().allUnScrappedProducts(lotName);
		}
		catch (NotFoundSignal notFoundSignal)
		{
		}

		if (sequence != null)
		{
			for (Product product : sequence)
			{
				ProductMultiHoldKey holdKey = new ProductMultiHoldKey();
				holdKey.setProductName(product.getKey().getProductName());
				holdKey.setReasonCode(eventInfo.getReasonCode());

				ProductMultiHold holdData = new ProductMultiHold();
				holdData.setKey(holdKey);
				holdData.setEventName(eventInfo.getEventName());
				holdData.setEventTime(eventInfo.getEventTime());
				holdData.setEventComment(eventInfo.getEventComment());
				holdData.setEventUser(eventInfo.getEventUser());
				holdData.setUdfs(udfs);

				try
				{
					ProductServiceProxy.getProductMultiHoldService().insert(holdData);
				}
				catch (DuplicateNameSignal de)
				{
					//PRODUCT-0003: Product[{0}] already has same hold reason[{1}]
					throw new CustomException("PRODUCT-0003", holdKey.getProductName() ,holdKey.getReasonCode() );
				}
				catch (FrameworkErrorSignal fe)
				{
					throw new CustomException("PRODUCT-9999", fe.getMessage());
				}
			}
		}
	}

	public void releaseProductMultiHold(String lotName, String reasonCode, String processOperationName) throws CustomException
	{
		List<Product> sequence = null;
		try
		{
			sequence = LotServiceProxy.getLotService().allUnScrappedProducts(lotName);
		}
		catch (NotFoundSignal notFoundSignal)
		{
		}

		if (sequence != null)
		{
			for (Product product : sequence)
			{
				try
				{
					ProductServiceProxy.getProductMultiHoldService().delete(" productname = ? and reasoncode = ? and processoperationname = ?",
							new Object[] { product.getKey().getProductName(), reasonCode, processOperationName });
				}
				catch (NotFoundSignal ne)
				{
					new CustomException("PRODUCT-9999", product.getKey().getProductName());
				}
				catch (DuplicateNameSignal de)
				{
					//PRODUCT-0003: Product[{0}] already has same hold reason[{1}]
					throw new CustomException("PRODUCT-0003", product.getKey().getProductName() ,reasonCode );
				}
				catch (FrameworkErrorSignal fe)
				{
					new CustomException("PRODUCT-9999", fe.getMessage());
				}
			}
		}
	}

}
