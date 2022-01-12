package kr.co.aim.messolution.product.event;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LOIRecipe;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.product.management.data.Product;

public class LOIRecipeRequest extends SyncHandler {

	private static Log log = LogFactory.getLog(LOIRecipeRequest.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		//String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		//String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		Element body = SMessageUtil.getBodyElement(doc);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("LOIRecipeRequest", this.getEventUser(), this.getEventComment(), null, null);
		
		// Set MessageName
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "LOIRecipeReply");

		Machine unitData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
		Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);
		LOIRecipe LOIRecipeDate = ExtendedObjectProxy.getLOIRecipeService().selectByKey(true, 
				new Object[] {productData.getFactoryName(),productData.getProductSpecName(),productData.getProductSpecVersion(),
						productData.getProcessFlowName(),productData.getProcessFlowVersion(),productData.getProcessOperationName(),
						productData.getProcessOperationVersion(),unitData.getKey().getMachineName()});

		if(StringUtil.isEmpty(LOIRecipeDate.getRecipeList()))
		{
			throw new CustomException("SYS-0010", "LOIRecipe is null");
		}
		List<String> recipeList = Arrays.asList(LOIRecipeDate.getRecipeList().split(","));
		
		long recipeIndex = (recipeList.size() + LOIRecipeDate.getCurrentProductCount())%recipeList.size();
		
		//add bodyElement PRODUCTRECIPE
		setBodyItem(body, "PRODUCTRECIPE", recipeList.get((int)recipeIndex).toString());
		
		//increase currentProductCount
		LOIRecipeDate.setCurrentProductCount(LOIRecipeDate.getCurrentProductCount() + 1);
		
		ExtendedObjectProxy.getLOIRecipeService().modify(eventInfo, LOIRecipeDate);
		
		return doc;
	}
	
	private void setBodyItem(Element body, String itemName, String itemValue)
	{
		if (body.getChild(itemName) == null)
			JdomUtils.addElement(body, itemName, itemValue);
		else
			body.getChild(itemName).setText(itemValue);
	}

}
