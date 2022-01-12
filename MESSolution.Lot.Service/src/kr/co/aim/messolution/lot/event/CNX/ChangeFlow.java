package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.jdom.Document;

public class ChangeFlow extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String newProcOperName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String newProcOperVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", true);
		String newFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String newFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		CommonValidation.checkJobDownFlag(lotData);
		CommonValidation.checkDummyProductReserve(lotData);

		Node changeNode = ProcessFlowServiceProxy.getNodeService().getNode(lotData.getFactoryName(), newFlowName, newFlowVersion, constantMap.Node_ProcessOperation, newProcOperName, newProcOperVersion);

		List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUSequence(lotName);

		String areaName = lotData.getAreaName();
		String factoryName = lotData.getFactoryName();
		String lotHoldState = lotData.getLotHoldState();
		String lotProcessState = lotData.getLotProcessState();
		String lotState = lotData.getLotState();
		String processFlowName = lotData.getProcessFlowName();
		String processFlowVersion = lotData.getProcessFlowVersion();
		String productionType = lotData.getProductionType();
		String productRequestName = lotData.getProductRequestName();
		String productSpec2Name = lotData.getProductSpec2Name();
		String productSpec2Version = lotData.getProductSpec2Version();
		String productSpecName = lotData.getProductSpecName();
		String productSpecVersion = lotData.getProductSpecVersion();
		long priority = lotData.getPriority();
		Timestamp dueDate = lotData.getDueDate();
		double subProductUnitQuantity1 = lotData.getSubProductUnitQuantity1();
		double subProductUnitQuantity2 = lotData.getSubProductUnitQuantity2();
		List<Product> producList = LotServiceProxy.getLotService().allProducts(lotName);

		List<String> productNameList = new ArrayList<>();
		for (Product productA : producList)
		{
			String productName = productA.getKey().getProductName();
			productNameList.add(productName);
		}
		
		CommonValidation.checkProductProcessInfobyString(productNameList);

		lotData.setProcessFlowName(newFlowName);
		lotData.setProcessFlowVersion(newFlowVersion);

		if (StringUtil.equals(lotHoldState, "Y"))
			throw new CustomException("LOT-0033", lotName);

		ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeFlow(lotData, areaName, dueDate, factoryName, lotHoldState, lotProcessState, lotState,
				changeNode.getKey().getNodeId(), priority, processFlowName, processFlowVersion, newProcOperName, newProcOperVersion, lotData.getProcessOperationName(), productionType,
				productRequestName, productSpec2Name, productSpec2Version, productSpecName, productSpecVersion, productUdfs, subProductUnitQuantity1, subProductUnitQuantity2);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeFlow", getEventUser(), getEventComment(), "", "");

		lotData = MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);

		return doc;
	}
}
