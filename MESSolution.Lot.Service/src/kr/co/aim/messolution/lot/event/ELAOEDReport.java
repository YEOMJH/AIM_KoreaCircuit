package kr.co.aim.messolution.lot.event;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.product.management.data.Product;

import org.jdom.Document;
import org.jdom.Element;

public class ELAOEDReport extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		// Set MessageName
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "ELAOEDReply");
		Element bodyElement = SMessageUtil.getBodyElement(doc);
		
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName    = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String lotName     = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String oedValue    = SMessageUtil.getBodyItemValue(doc, "OEDVALUE", true);
		
		Product productData     = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
		String mainMachineName  = productData.getUdfs().get("MAINMACHINENAME");
		Machine mainMachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(mainMachineName);

		Element newBodyElement = this.generateBodyTemplate(SMessageUtil.getBodyElement(doc), mainMachineName,
				machineName, unitName, lotName, carrierName, productName, oedValue);
		
		bodyElement.detach();
		doc.getRootElement().addContent(newBodyElement);

		String targetSubjectName = MESRecipeServiceProxy.getRecipeServiceUtil().getEASSubjectName(mainMachineName);
		setHeaderItemValues(doc, mainMachineData.getFactoryName(), machineName, targetSubjectName);
		GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, doc, "EISSender");
	}
	
	private void setHeaderItemValues(Document doc, String factoryName, String machineName, String targetSubjectName)
	{
		SMessageUtil.setItemValue(doc, "Header", "SHOPNAME", factoryName);
		SMessageUtil.setItemValue(doc, "Header", "MACHINENAME", machineName);
		SMessageUtil.setItemValue(doc, "Header", "SOURCESUBJECTNAME", GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("RMSsvr"));
		SMessageUtil.setItemValue(doc, "Header", "TARGETSUBJECTNAME", targetSubjectName);
	}
	
	private Element generateBodyTemplate(Element bodyElement, String mainMachineName, String machineName, String unitName,
			String lotName, String carrierName, String productName, String oedValue) throws CustomException
	{
		
		Element newBodyElement = new Element("Body");
		
		XmlUtil.addElement(newBodyElement, "MACHINENAME", mainMachineName);
		XmlUtil.addElement(newBodyElement, "FROMMACHINENAME", machineName);
		XmlUtil.addElement(newBodyElement, "FROMUNITNAME", unitName);
		XmlUtil.addElement(newBodyElement, "LOTNAME", lotName);
		XmlUtil.addElement(newBodyElement, "CARRIERNAME", carrierName);
		XmlUtil.addElement(newBodyElement, "PRODUCTNAME", productName);
		XmlUtil.addElement(newBodyElement, "OEDVALUE", oedValue);

		return newBodyElement;
	}
}
