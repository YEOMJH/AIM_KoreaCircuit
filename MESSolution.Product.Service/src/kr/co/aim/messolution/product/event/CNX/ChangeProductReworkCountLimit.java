package kr.co.aim.messolution.product.event.CNX;

import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReworkProduct;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

public class ChangeProductReworkCountLimit extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String reworkType = SMessageUtil.getBodyItemValue(doc, "REWORKTYPE", true);
		String reworkCountLimit = SMessageUtil.getBodyItemValue(doc, "REWORKCOUNTLIMIT", true);

		ReworkProduct reworkProductData = new ReworkProduct();

		try
		{
			reworkProductData = ExtendedObjectProxy.getReworkProductService().selectByKey(false, new Object[] { productName, reworkType });
		}
		catch (Exception e)
		{
			throw new CustomException("PRODUCT-0021", productName, reworkType);
		}

		if (reworkProductData != null)
		{
			reworkProductData.setReworkCountLimit(reworkCountLimit);
			ExtendedObjectProxy.getReworkProductService().update(reworkProductData);
		}

		return doc;
	}

}
