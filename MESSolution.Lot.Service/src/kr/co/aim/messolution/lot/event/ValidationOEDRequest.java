package kr.co.aim.messolution.lot.event;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;

public class ValidationOEDRequest extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName    = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String portName    = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String lotName   = MESLotServiceProxy.getLotInfoUtil().getLotNameByCarrierName(carrierName);
		
		// Get LotData 
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		
		// POSMachine Recipe
		String machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(),
				lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineName, false);
		
		// create reply message 
		Document replyDoc = this.generateReplyMessage(doc, lotData, machineRecipeName);
		
		// send message to R2R
		String targetSubject = GenericServiceProxy.getESBServive().getSendSubject("R2R");
		GenericServiceProxy.getESBServive().sendBySender(targetSubject, replyDoc, "R2RSender");
	}
	
	private Document generateReplyMessage(Document doc,Lot lotData,String machineRecipeName) throws CustomException
	{
		Element originalBodyElement = doc.getRootElement().getChild(SMessageUtil.Body_Tag);
		
		Element messageElement = new Element(SMessageUtil.Message_Tag);
		Element HeaderElement = (Element) doc.getRootElement().getChild(SMessageUtil.Header_Tag).clone();

		HeaderElement.getChild("MESSAGENAME").setText("OEDParameterRequest");
		HeaderElement.addContent(new Element("MACHINENAME").setText(originalBodyElement.getChildText("MACHINENAME")));
		messageElement.addContent(HeaderElement);

		Element bodyElement = new Element(SMessageUtil.Body_Tag);
		
		bodyElement.addContent(new Element("MACHINENAME").setText(originalBodyElement.getChildText("MACHINENAME")));
		bodyElement.addContent(new Element("CARRIERNAME").setText(originalBodyElement.getChildText("CARRIERNAME")));
		bodyElement.addContent(new Element("LOTNAME").setText(lotData.getKey().getLotName()));
		bodyElement.addContent(new Element("PROCESSOPERATIONNAME").setText(lotData.getProcessOperationName()));
		bodyElement.addContent(new Element("MACHINERECIPENAME").setText(machineRecipeName));
		bodyElement.addContent(new Element("PRODUCTSPECNAME").setText(lotData.getProductSpecName()));

		Element productList = new Element("PRODUCTLIST");

		for (Element productElement : this.generateProductListElement(lotData))
		{
			productList.addContent(productElement);
		}

		bodyElement.addContent(productList);
		messageElement.addContent(bodyElement);

		return new Document(messageElement);
	}
	
	private List<Element> generateProductListElement(Lot lotData) throws CustomException
	{

		List<Product> productList = new ArrayList<Product>();

		productList = ProductServiceProxy.getProductService().select(" WHERE lotName = ? AND productState != ? AND productState != ? ORDER BY position, slotPosition ",
					new Object[] { lotData.getKey().getLotName(), GenericServiceProxy.getConstantMap().Prod_Scrapped, GenericServiceProxy.getConstantMap().Prod_Consumed });

		List<Element> productListElement = new ArrayList<Element>();
		for (Product productData : productList)
		{
			Element productElement = generateProductElement(productData);
			productListElement.add(productElement);
		}

		return productListElement;
	}
	
	private Element generateProductElement(Product productData) throws CustomException
	{
		Element productElement = new Element("PRODUCT");

		productElement.addContent(new Element("PRODUCTNAME").setText(productData.getKey().getProductName()));
		productElement.addContent(new Element("POSITION").setText(String.valueOf(productData.getPosition())));
		productElement.addContent(new Element("PROCESSOPERATIONNAME").setText(productData.getProcessOperationName()));
		productElement.addContent(new Element("PRODUCTSPECNAME").setText(productData.getProductSpecName()));
		productElement.addContent(new Element("PRODUCTIONTYPE").setText(productData.getProductionType()));

		return productElement;
	}
}