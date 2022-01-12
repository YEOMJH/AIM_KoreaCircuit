package kr.co.aim.messolution.lot.event.CNX;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.util.support.InvokeUtils;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;

public class TrackInOutLot extends AsyncHandler {
	@Override
	public void doWorks(Document doc) throws CustomException {
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String loadPort = SMessageUtil.getBodyItemValue(doc, "LOADPORT", true);
		String unLoadPort = SMessageUtil.getBodyItemValue(doc, "UNLOADPORT", true);
		
		Object result = null;
		
		Lot lotData =  MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		
		try
		{
			Port loader = this.searchLoaderPort(machineName);
			
			String machineRecipeName = MESRecipeServiceProxy.getRecipeServiceUtil().getMachineRecipe(lotData.getFactoryName(),
					lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), 
					lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineName, false);
			
			doc = this.writeTrackInRequest(doc, lotData.getKey().getLotName(), loader.getKey().getMachineName(), loadPort, machineRecipeName);
			
			result = InvokeUtils.invokeMethod(InvokeUtils.newInstance(TrackInLot.class.getName(), null, null), "execute", new Object[] {doc});
			
			Port unloader = this.searchUnloaderPort(loader);
			
			doc = this.writeTrackOutRequest(doc, lotData.getKey().getLotName(), unloader.getKey().getMachineName(), unLoadPort, carrierName,machineRecipeName);
			
			result = InvokeUtils.invokeMethod(InvokeUtils.newInstance(TrackOutLotPU.class.getName(), null, null), "execute", new Object[] {doc});
		}
		catch (NoSuchMethodException e)
		{
			eventLog.error(e);
		}
		catch (IllegalAccessException e)
		{
			eventLog.error(e);
		}
		catch (InvocationTargetException e)
		{
			eventLog.error(e);
		}
		
		eventLog.debug("end");
	}
	
	private Document writeTrackInRequest(Document doc, String lotName, String machineName, String portName, String recipeName)
		throws CustomException
	{
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "TrackInLot");
		//SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", GenericServiceProxy.getESBServive().getSendSubject("CNXsvr"));
		
		//Element eleBody = SMessageUtil.getBodyElement(doc);

		boolean result = doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		
		Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);
		
		Element element1 = new Element("LOTNAME");
		element1.setText(lotName);
		eleBodyTemp.addContent(element1);
		
		Element element2 = new Element("MACHINENAME");
		element2.setText(machineName);
		eleBodyTemp.addContent(element2);
		
		Element element3 = new Element("PORTNAME");
		element3.setText(portName);
		eleBodyTemp.addContent(element3);
		
		Element element4 = new Element("RECIPENAME");
		element4.setText(recipeName);
		eleBodyTemp.addContent(element4);
		
		Element element5 = new Element("TPOFFSETFLAG");
		element5.setText("False");
		eleBodyTemp.addContent(element5);
		
		Element element6 = new Element("CHANGERECIPEFLAG");
		element6.setText("False");
		eleBodyTemp.addContent(element6);
		
		//overwrite
		doc.getRootElement().addContent(eleBodyTemp);
		
		return doc;
	}

	private Document writeTrackOutRequest(Document doc, String lotName, String machineName, String portName, String carrierName,String machineRecipeName)
			throws CustomException
	{
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "TrackOutLot");
		//SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", GenericServiceProxy.getESBServive().getSendSubject("CNXsvr"));
		
		boolean result = doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
		
		Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);
		
		Element element1 = new Element("LOTNAME");
		element1.setText(lotName);
		eleBodyTemp.addContent(element1);
		
		Element element2 = new Element("MACHINENAME");
		element2.setText(machineName);
		eleBodyTemp.addContent(element2);
		
		Element element3 = new Element("PORTNAME");
		element3.setText(portName);
		eleBodyTemp.addContent(element3);
		
		Element element4 = new Element("CARRIERNAME");
		element4.setText(carrierName);
		eleBodyTemp.addContent(element4);
		
		Element element5 = new Element("MACHINERECIPENAME");
		element5.setText(machineRecipeName);
		eleBodyTemp.addContent(element5);
		
		Element elementPL = new Element("PRODUCTLIST");
		try
		{
			List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);
			
			for (Product productData : productList)
			{
				Element elementP = new Element("PRODUCT");
				{
					Element elementS1 = new Element("PRODUCTNAME");
					elementS1.setText(productData.getKey().getProductName());
					elementP.addContent(elementS1);
					
					Element elementS2 = new Element("POSITION");
					elementS2.setText(String.valueOf(productData.getPosition()));
					elementP.addContent(elementS2);
					
					Element elementS3 = new Element("PRODUCTJUDGE");
					elementS3.setText(productData.getProductGrade());
					elementP.addContent(elementS3);
				}
				elementPL.addContent(elementP);
			}
		}
		catch (NotFoundSignal e) 
	    {
	    	throw new CustomException("PRODUCT-9001", "");	
		}
	    catch (FrameworkErrorSignal fe)
	    {
	    	throw new CustomException("PRODUCT-9999", fe.getMessage());	
	    }
		eleBodyTemp.addContent(elementPL);
		
		//overwrite
		doc.getRootElement().addContent(eleBodyTemp);
		
		return doc;
	}
	
	private Port searchLoaderPort(String machineName) throws CustomException
	{
		try
		{
			List<Port> result = PortServiceProxy.getPortService().select("machineName = ? AND portType IN (?, ?, ?)", new Object[] {machineName, "PB", "PL", "PS"});
			
			return result.get(0);
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("PORT-9001", machineName, "");
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("PORT-9999", fe.getMessage());
		}
	}
	
	private Port searchUnloaderPort(Port portData) throws CustomException
	{
		if (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PB"))
		{
			return portData;
		}
		else
		{
			try
			{
				List<Port> result = PortServiceProxy.getPortService().select("machineName = ? AND portType IN (?, ?)", new Object[] {portData.getKey().getMachineName(), "PU", "PS"});
				
				return result.get(0);
			}
			catch (NotFoundSignal ne)
			{
				throw new CustomException("PORT-9001", portData.getKey().getMachineName(), "");
			}
			catch (FrameworkErrorSignal fe)
			{
				throw new CustomException("PORT-9999", fe.getMessage());
			}
		}
	}
	
}
