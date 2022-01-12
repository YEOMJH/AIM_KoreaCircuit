package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.jdom.Document;

public class BackUpEQP extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String BackUpFlowName = SMessageUtil.getBodyItemValue(doc, "BackUpFLOWNAME", true);
		String BackUpOperationName = SMessageUtil.getBodyItemValue(doc, "BackUpOPERATIONNAME", true);
		String BackUpProcessOperationVer = SMessageUtil.getBodyItemValue(doc, "BackUpOPERATIONVER", false);
		
		if (!StringUtil.isNotEmpty(BackUpProcessOperationVer))
			BackUpProcessOperationVer = "00001";
		
		String returnFlowName = SMessageUtil.getBodyItemValue(doc, "RETURNFLOWNAME", true);
		String returnOperationName = SMessageUtil.getBodyItemValue(doc, "RETURNOPERATIONNAME", true);

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		CommonValidation.checkJobDownFlag(lotData);

		List<ProductU> productU = setProductUSequence(lotName);

		ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotData, lotData.getAreaName(), lotData.getDueDate(), lotData.getFactoryName(), lotData.getLotHoldState(),
				lotData.getLotProcessState(), lotData.getLotState(), ""/* nodeStack */, lotData.getPriority(), BackUpFlowName, "00001", BackUpOperationName, BackUpProcessOperationVer,
				lotData.getProductionType(), lotData.getProductRequestName(), "", "", lotData.getProductSpecName(), lotData.getProductSpecVersion(), productU, lotData.getSubProductUnitQuantity1(),
				lotData.getSubProductUnitQuantity2(), returnFlowName, returnOperationName);
		
		// for Sampling.
		changeSpecInfo.getUdfs().put("BACKUPMAINOPERNAME", lotData.getProcessOperationName());
		changeSpecInfo.getUdfs().put("BACKUPMAINFLOWNAME", lotData.getProcessFlowName()); 

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("BackupEQP", getEventUser(), getEventComment(), "", "");
		
		MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);

		// 2020-11-15	dhko	LotHold if ProcessingInfo is 'B' in Product
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		List<String> lotNameList = new ArrayList<String>();
		lotNameList.add(lotName);
		
		MESLotServiceProxy.getLotServiceImpl().makeOnHoldByAbortProductList(eventInfo, lotNameList);
		
		return doc;
	}
	
	private List<ProductU> setProductUSequence(String lotName)
	{
		List<ProductU> productUList = new ArrayList<ProductU>();

		try
		{
			List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);

			for (Product product : productList)
			{
				ProductU productU = new ProductU();
				productU.setProductName(product.getKey().getProductName());
				productU.getUdfs().put("REWORKFLAG", "");
				productUList.add(productU);
			}
		}
		catch (NotFoundSignal ne)
		{
			//
		}
		return productUList;
	}

}
