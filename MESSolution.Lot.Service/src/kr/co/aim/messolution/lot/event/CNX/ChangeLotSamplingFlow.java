package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SampleLot;
import kr.co.aim.messolution.extended.object.management.data.SampleProduct;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ChangeLotSamplingFlow extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		// for common
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeFlow", getEventUser(), getEventComment(), null, null);

		if (!StringUtils.equals(eventInfo.getEventComment(), "ChangeLotSamplingFlow"))
			eventInfo.setEventComment("ChangeLotSamplingFlow: " + eventInfo.getEventComment());

		boolean isChanged = false;
		int iPriority = 1;
		String sPriority = "";
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String fromSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String fromFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String fromOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String changeFlowName = SMessageUtil.getBodyItemValue(doc, "TOPROCESSFLOWNAME", true);
		String changeFlowVersion = SMessageUtil.getBodyItemValue(doc, "TOPROCESSFLOWVERSION", true);

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		Lot newLotData = (Lot) ObjectUtil.copyTo(lotData);

		ExtendedObjectProxy.getSampleLotService().checkLotForceSampling(newLotData);

		List<Product> producList = LotServiceProxy.getLotService().allProducts(lotName);

		// check ProcessInfo
		List<String> productNameList = new ArrayList<>();
		for (Product productA : producList)
		{
			String productName = productA.getKey().getProductName();
			productNameList.add(productName);
		}

		CommonValidation.checkProductProcessInfobyString(productNameList);
		CommonValidation.checkJobDownFlag(lotData);
		CommonValidation.checkLotState(lotData);
		CommonValidation.checkLotProcessState(lotData);
		CommonValidation.checkLotHoldState(lotData);
		CommonValidation.checkLotIssueState(lotData);

		// Cannot Change to Same Flow
		if (StringUtils.equals(changeFlowName, lotData.getProcessFlowName()) && StringUtils.equals(changeFlowVersion, lotData.getProcessFlowVersion()))
			throw new CustomException("LOT-9023", changeFlowName, changeFlowVersion);

		// 1. set Sampling Lot List by Lot
		List<SampleLot> sampleLotList = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListByInfo(lotName, factoryName, fromSpecName, "00001", fromFlowName, "00001", fromOperationName,
				"00001");

		for (SampleLot sampleLot : sampleLotList)
		{
			String toFlowName = sampleLot.getToProcessFlowName();
			String toFlowVersion = sampleLot.getToProcessFlowVersion();

			if (StringUtils.equals(changeFlowName, toFlowName) && StringUtils.equals(changeFlowVersion, toFlowVersion))
			{
				isChanged = true;
				sPriority = String.valueOf(iPriority);

				List<Map<String, Object>> operationList = MESLotServiceProxy.getLotServiceUtil().operationList(factoryName, changeFlowName, changeFlowVersion);

				if (operationList.size() > 0)
				{
					String changeOperationName = ConvertUtil.getMapValueByName(operationList.get(0), "PROCESSOPERATIONNAME");
					String changeOperationVer = ConvertUtil.getMapValueByName(operationList.get(0), "PROCESSOPERATIONVERSION");

					// ChangeSpec
					List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUSequence(lotName);
					lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

					ChangeSpecInfo changeSpecInfo = changeSpecInfo(lotData, lotData.getAreaName(), lotData.getDueDate(), factoryName, lotData.getLotHoldState(), lotData.getLotProcessState(),
							lotData.getLotState(), "", lotData.getPriority(), changeFlowName, changeFlowVersion, changeOperationName, changeOperationVer, lotData.getProcessOperationName(),
							lotData.getProductionType(), lotData.getProductRequestName(), lotData.getProductSpec2Name(), lotData.getProductSpec2Version(), lotData.getProductSpecName(),
							lotData.getProductSpecVersion(), productUdfs, lotData.getSubProductUnitQuantity1(), lotData.getSubProductUnitQuantity2());

					newLotData = MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);

					// SetPriority as 1
					ExtendedObjectProxy.getSampleLotService().updateSampleLotWithoutOperation(eventInfo, lotName, factoryName, fromSpecName, "00001", fromFlowName, "00001", toFlowName, toFlowVersion,
							"", "", "", "", "", "", "", "", "", "", sPriority, "", "", "", "", "");
				}
				else
				{
					throw new CustomException("LOT-0140", changeFlowName);
				}
			}
			else
			{
				if (isChanged)
				{
					iPriority = iPriority + 1;
					sPriority = String.valueOf(iPriority);

					ExtendedObjectProxy.getSampleLotService().updateSampleLotWithoutOperation(eventInfo, lotName, factoryName, fromSpecName, "00001", fromFlowName, "00001", toFlowName, toFlowVersion,
							"", "", "", "", "", "", "", "", "", "", sPriority, "", "", "", "", "");
				}
				else
				{
					// delete
					deleteSampleProduct(eventInfo, sampleLot);

					ExtendedObjectProxy.getSampleLotService().deleteSampleLotWithOutOperationName(eventInfo, lotName, factoryName, fromSpecName, "00001", fromFlowName, "00001", toFlowName,
							toFlowVersion);
				}
			}
		}

		newLotData = MESLotServiceProxy.getLotServiceUtil().executeReserveAction(eventInfo, lotData, newLotData);

		return doc;
	}

	private void deleteSampleProduct(EventInfo eventInfo, SampleLot sampleLot) throws CustomException
	{
		String lotName = sampleLot.getLotName();
		String factoryName = sampleLot.getFactoryName();
		String productSpecName = sampleLot.getProductSpecName();
		String productSpecVersion = sampleLot.getProductSpecVersion();
		String processFlowName = sampleLot.getProcessFlowName();
		String processFlowVersion = sampleLot.getProcessFlowVersion();
		String processOperationName = sampleLot.getProcessOperationName();
		String processOperationVersion = sampleLot.getProcessOperationVersion();
		String toProcessFlowName = sampleLot.getToProcessFlowName();
		String toProcessFlowVersion = sampleLot.getToProcessFlowVersion();
		String toProcessOperationName = sampleLot.getToProcessOperationName();
		String toProcessOperationVersion = sampleLot.getToProcessOperationVersion();

		// Get deleteSampleProductList
		List<SampleProduct> deleteSampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListByLotName(lotName, factoryName, productSpecName, productSpecVersion,
				processFlowName, processFlowVersion, processOperationName, processOperationVersion, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);

		if (deleteSampleProductList.size() > 0)
		{
			for (SampleProduct deleteSampleProduct : deleteSampleProductList)
			{
				String productName = deleteSampleProduct.getProductName();

				ExtendedObjectProxy.getSampleProductService().deleteSampleProductWithoutMachineName(eventInfo, productName, lotName, factoryName, productSpecName, productSpecVersion, processFlowName,
						processFlowVersion, processOperationName, processOperationVersion, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);
			}
		}
	}

	private ChangeSpecInfo changeSpecInfo(Lot lotData, String areaName, Timestamp dueDate, String factoryName, String lotHoldState, String lotProcessState, String lotState, String nodeStack,
			long priority, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String beforeOperationName, String productionType,
			String productRequestName, String productSpec2Name, String productSpec2Version, String productSpecName, String productSpecVersion, List<ProductU> productUdfs,
			double subProductUnitQuantity1, double subProductUnitQuantity2) throws CustomException
	{
		if (StringUtil.equals(lotData.getLotProcessState(), "WAIT"))
		{
			// 1. Validation
			ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();

			changeSpecInfo.setAreaName(areaName);
			changeSpecInfo.setDueDate(dueDate);
			changeSpecInfo.setFactoryName(factoryName);
			changeSpecInfo.setLotHoldState(lotHoldState);
			changeSpecInfo.setLotProcessState(lotProcessState);
			changeSpecInfo.setLotState(lotState);
			changeSpecInfo.setNodeStack(nodeStack);
			changeSpecInfo.setPriority(priority);
			changeSpecInfo.setProcessFlowName(processFlowName);
			changeSpecInfo.setProcessFlowVersion(processFlowVersion);
			changeSpecInfo.setProcessOperationName(processOperationName);
			changeSpecInfo.setProcessOperationVersion(processOperationVersion);
			changeSpecInfo.setProductionType(productionType);
			changeSpecInfo.setProductRequestName(productRequestName);
			changeSpecInfo.setProductSpec2Name(productSpec2Name);
			changeSpecInfo.setProductSpec2Version(productSpec2Version);
			changeSpecInfo.setProductSpecName(productSpecName);
			changeSpecInfo.setProductSpecVersion(productSpecVersion);
			changeSpecInfo.setProductUSequence(productUdfs);
			changeSpecInfo.setSubProductUnitQuantity1(subProductUnitQuantity1);
			changeSpecInfo.setSubProductUnitQuantity2(subProductUnitQuantity2);

			Map<String, String> lotUdfs = new HashMap<String, String>();

			lotUdfs.put("BEFOREOPERATIONNAME", beforeOperationName);
			lotUdfs.put("BEFOREFLOWNAME", processFlowName);

			StringBuffer sql = new StringBuffer();
			sql.append("SELECT NODEID ");
			sql.append("  FROM NODE ");
			sql.append(" WHERE FACTORYNAME = ? ");
			sql.append("   AND PROCESSFLOWNAME = ? ");
			sql.append("   AND PROCESSFLOWVERSION = ? ");
			sql.append("   AND NODEATTRIBUTE1 = ? ");
			sql.append("   AND NODEATTRIBUTE2 = ? ");
			sql.append("   AND NODETYPE = 'ProcessOperation' ");

			Object[] bind = new Object[] { factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion };

			String[][] result = GenericServiceProxy.getSqlMesTemplate().queryForStringArray(sql.toString(), bind);

			if (result.length == 0)
			{
				eventLog.info("Not Found NodeID");
				throw new CustomException("Node-0001", lotData.getProductSpecName(), lotData.getProcessFlowName(), processOperationName);
			}
			else
			{
				String sToBeNodeStack = lotData.getNodeStack();
				String sNodeStack = lotData.getNodeStack();

				if (sNodeStack.contains("."))
				{
					String sCurNode = StringUtil.getLastValue(sNodeStack, ".");
					sNodeStack = sNodeStack.substring(0, sNodeStack.length() - sCurNode.length() - 1);

					sToBeNodeStack = sNodeStack + "." + (String) result[0][0];
				}
				else
				{
					sToBeNodeStack = (String) result[0][0];
				}
				
				changeSpecInfo.setNodeStack(sToBeNodeStack);
			}

			changeSpecInfo.setUdfs(lotUdfs);
			
			return changeSpecInfo;
		}
		else
		{
			throw new CustomException("LOT-0031", lotData.getKey().getLotName());
		}
	}
}
