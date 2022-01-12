package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductHistory;
import kr.co.aim.greentrack.lot.management.data.Lot;

public class PostCellChangeSubProductGrade extends SyncHandler{
	
	private static Log log = LogFactory.getLog(PostCellChangeSubProductGrade.class);
	
	@SuppressWarnings("unchecked")
	@Override
	public Object doWorks(Document doc) throws CustomException {
		

		//String FactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String subProductGrade = SMessageUtil.getBodyItemValue(doc, "SUBPRODUCTGRADE", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeSubProductGrade", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		List<Product> productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotName);
		
		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
		
		CommonValidation.checkLotHoldState(lotData);
		CommonValidation.checkLotProcessState(lotData);
		
		List<Product> updateProductList = new ArrayList<Product>();
		List<ProductHistory> updateProductHistory = new ArrayList<ProductHistory>();
		
		for (Product product : productList)
		{
			Product oldProduct = (Product) ObjectUtil.copyTo(product);
			
			product.setSubProductGrades1(subProductGrade);
			product.setLastEventName(eventInfo.getEventName());
			product.setLastEventTime(eventInfo.getEventTime());
			product.setLastEventTimeKey(eventInfo.getEventTimeKey());
			product.setLastEventComment(eventInfo.getEventComment());
			product.setLastEventUser(eventInfo.getEventUser());
			
			ProductHistory productHistory = new ProductHistory();
			productHistory = ProductServiceProxy.getProductHistoryDataAdaptor().setHV(oldProduct, product, productHistory);
					
			updateProductList.add(product);
			updateProductHistory.add(productHistory);
		}
		
		if(updateProductList.size() > 0)
		{
			log.debug("Update Product");
			try
			{
				CommonUtil.executeBatch("update", updateProductList);
				CommonUtil.executeBatch("insert", updateProductHistory);
			}
			catch (Exception e)
			{
				log.error(e.getMessage());
				throw new CustomException(e.getCause());
			}
		}
		
		return doc;
	}
}