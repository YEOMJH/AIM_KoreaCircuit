package kr.co.aim.messolution.product.event.CNX;

import java.util.List;

import org.apache.commons.lang.exception.Nestable;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReworkProduct;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;

public class ChangeProductReworkCount extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String reworkCount = SMessageUtil.getBodyItemValue(doc, "REWORKCOUNT", true);
		List<Element> productList=SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);

        for(Element product:productList)
        {
        	String productName=SMessageUtil.getChildText(product, "PRODUCTNAME", true);
        	String processOperationName=SMessageUtil.getChildText(product, "PROCESSOPERATIONNAME", true);
        	
        	try
        	{
        		ReworkProduct reworkProduct=ExtendedObjectProxy.getReworkProductService().selectByKey(false, new Object[]{productName,processOperationName});
        		reworkProduct.setReworkCount(Long.parseLong(reworkCount));
				ExtendedObjectProxy.getReworkProductService().update(reworkProduct);
        	}
        	catch(greenFrameDBErrorSignal n)
        	{
        		throw new CustomException("PRODUCT-0022");
        	}
        }

		return doc;
	}

}

