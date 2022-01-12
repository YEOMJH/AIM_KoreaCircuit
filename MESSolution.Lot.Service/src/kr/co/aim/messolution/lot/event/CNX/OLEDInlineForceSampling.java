package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.InlineSampleLot;
import kr.co.aim.messolution.extended.object.management.data.InlineSampleProduct;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class OLEDInlineForceSampling extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String processOperationVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);

		List<String> actualSamplePositionList = new ArrayList<String>();
		List<InlineSampleProduct> insertInlineSampleProductList = new ArrayList<InlineSampleProduct>();
		List<InlineSampleProduct> updateInlineSampleProductList = new ArrayList<InlineSampleProduct>();
		List<InlineSampleProduct> deleteInlineSampleProductList = new ArrayList<InlineSampleProduct>();

		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		CommonValidation.checkLotState(lotData);
		CommonValidation.checkLotHoldState(lotData);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("OLEDInlineForceSampling", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		for (Element productE : productList)
		{
			String action = "";
			String productName = SMessageUtil.getChildText(productE, "PRODUCTNAME", true);
			String position = SMessageUtil.getChildText(productE, "POSITION", true);
			String inspectionFlag = SMessageUtil.getChildText(productE, "INSPECTIONFLAG", false);
			String sMachineName = SMessageUtil.getChildText(productE, "MACHINENAME", false);

			InlineSampleProduct inlineSampleProduct = new InlineSampleProduct();

			try
			{
				Object[] keySet = new Object[] { productName, lotName, lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), processFlowName, processFlowVersion,
						processOperationName, processOperationVersion, machineName };

				inlineSampleProduct = ExtendedObjectProxy.getInlineSampleProductService().selectByKey(false, keySet);
			}
			catch (Exception e)
			{
				if (StringUtils.isEmpty(sMachineName))
					continue;
				
				inlineSampleProduct.setProductName(productName);
				inlineSampleProduct.setLotName(lotName);
				inlineSampleProduct.setFactoryName(lotData.getFactoryName());
				inlineSampleProduct.setProductSpecName(lotData.getProductSpecName());
				inlineSampleProduct.setProductSpecVersion(lotData.getProductSpecVersion());
				inlineSampleProduct.setProcessFlowName(processFlowName);
				inlineSampleProduct.setProcessFlowVersion(processFlowVersion);
				inlineSampleProduct.setProcessOperationName(processOperationName);
				inlineSampleProduct.setProcessOperationVersion(processOperationVersion);
				inlineSampleProduct.setMachineName(sMachineName);
				action = "INSERT";
			}

			if (StringUtils.isEmpty(sMachineName) || (StringUtils.isEmpty(action) && StringUtils.isEmpty(inspectionFlag)))
				action = "DELETE";
			else if (!StringUtils.equals(action, "INSERT"))
				action = "UPDATE";

			inlineSampleProduct.setActualSamplePosition(position);
			inlineSampleProduct.setInspectionFlag(inspectionFlag);
			inlineSampleProduct.setProductSampleFlag("Y");
			inlineSampleProduct.setEventComment(eventInfo.getEventComment());
			inlineSampleProduct.setEventUser(eventInfo.getEventUser());

			if (StringUtils.equals(action, "INSERT"))
			{
				insertInlineSampleProductList.add(inlineSampleProduct);
				actualSamplePositionList.add(position);
			}
			else if (StringUtils.equals(action, "UPDATE"))
			{
				updateInlineSampleProductList.add(inlineSampleProduct);
				actualSamplePositionList.add(position);
			}
			else if (StringUtils.equals(action, "DELETE"))
			{
				deleteInlineSampleProductList.add(inlineSampleProduct);
			}
		}

		String action = "";
		InlineSampleLot inlineSampleLot = new InlineSampleLot();

		try
		{
			Object[] keySet = new Object[] { lotName, lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), processFlowName, processFlowVersion,
					processOperationName, processOperationVersion, machineName };

			inlineSampleLot = ExtendedObjectProxy.getInlineSampleLotService().selectByKey(false, keySet);
		}
		catch (Exception e)
		{
			inlineSampleLot.setLotName(lotName);
			inlineSampleLot.setFactoryName(lotData.getFactoryName());
			inlineSampleLot.setProductSpecName(lotData.getProductSpecName());
			inlineSampleLot.setProductSpecVersion(lotData.getProductSpecVersion());
			inlineSampleLot.setProcessFlowName(processFlowName);
			inlineSampleLot.setProcessFlowVersion(processFlowVersion);
			inlineSampleLot.setProcessOperationName(processOperationName);
			inlineSampleLot.setProcessOperationVersion(processOperationVersion);
			inlineSampleLot.setMachineName(machineName);
			action = "INSERT";
		}

		if (insertInlineSampleProductList.size() == 0 && updateInlineSampleProductList.size() == 0 && deleteInlineSampleProductList.size() > 0)
			action = "DELETE";
		else if (!StringUtils.equals(action, "INSERT"))
			action = "UPDATE";

		inlineSampleLot.setLotSampleFlag("Y");
		inlineSampleLot.setLotSampleCount("1");
		inlineSampleLot.setCurrentLotCount("1");
		inlineSampleLot.setTotalLotCount("1");
		inlineSampleLot.setProductSampleCount(String.valueOf(actualSamplePositionList.size()));
		inlineSampleLot.setProductSampleposition(CommonUtil.toStringWithoutBrackets(actualSamplePositionList));
		inlineSampleLot.setActualProductCount(String.valueOf(actualSamplePositionList.size()));
		inlineSampleLot.setActualSampleposition(CommonUtil.toStringWithoutBrackets(actualSamplePositionList));
		inlineSampleLot.setManualSampleFlag("Y");
		inlineSampleLot.setEventUser(eventInfo.getEventUser());
		inlineSampleLot.setEventComment(eventInfo.getEventComment());
		inlineSampleLot.setLotGrade(lotData.getLotGrade());
		inlineSampleLot.setPriority(1);

		// InlineSampleLot
		if (StringUtils.equals(action, "INSERT"))
			ExtendedObjectProxy.getInlineSampleLotService().create(eventInfo, inlineSampleLot);
		else if (StringUtils.equals(action, "UPDATE"))
			ExtendedObjectProxy.getInlineSampleLotService().modify(eventInfo, inlineSampleLot);
		else if (StringUtils.equals(action, "DELETE"))
			ExtendedObjectProxy.getInlineSampleLotService().remove(eventInfo, inlineSampleLot);

		// InlineSampleProduct
		if (insertInlineSampleProductList.size() > 0)
			ExtendedObjectProxy.getInlineSampleProductService().create(eventInfo, insertInlineSampleProductList);

		if (updateInlineSampleProductList.size() > 0)
			ExtendedObjectProxy.getInlineSampleProductService().modify(eventInfo, updateInlineSampleProductList);

		if (deleteInlineSampleProductList.size() > 0)
		{
			try
			{
				ExtendedObjectProxy.getInlineSampleProductService().remove(eventInfo, deleteInlineSampleProductList);
			}
			catch (Exception e)
			{
			}
		}

		return doc;
	}

}
