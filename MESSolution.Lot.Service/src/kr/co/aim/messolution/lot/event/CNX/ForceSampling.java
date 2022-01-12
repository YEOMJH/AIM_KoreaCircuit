package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SampleLot;
import kr.co.aim.messolution.extended.object.management.data.SampleLotCount;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ForceSampling extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String machineName = "NA";
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String currentProductSpecName = SMessageUtil.getBodyItemValue(doc, "CURRENTPRODUCTSPECNAME", true);
		String currentFlowName = SMessageUtil.getBodyItemValue(doc, "CURRENTFLOWNAME", true);
		String currentOperationName = SMessageUtil.getBodyItemValue(doc, "CURRENTOPERATIONNAME", true);
		String department = SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", false);
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);
		
		List<Element> tabList = SMessageUtil.getBodySequenceItemList(doc, "FORCESAMPLELIST", true);

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		Lot oldLotData = (Lot) ObjectUtil.copyTo(lotData);

		CommonValidation.checkLotState(lotData);
		CommonValidation.checkLotProcessState(lotData);
		CommonValidation.checkJobDownFlag(lotData);
		CommonValidation.checkDummyProductReserve(lotData);

		// 2020-11-14 dhko Add Validation
		CommonValidation.checkProcessInfobyString(lotName);

		if (!StringUtils.equals(currentProductSpecName, lotData.getProductSpecName()))
			throw new CustomException("LOT-0137", lotData.getProductSpecName());

		if (!StringUtils.equals(currentFlowName, lotData.getProcessFlowName()))
			throw new CustomException("LOT-0138", lotData.getProcessFlowName());

		if (!StringUtils.equals(currentOperationName, lotData.getProcessOperationName()))
			throw new CustomException("LOT-0139", lotData.getProcessOperationName());

		// 2020-12-07 ghhan Add ForceSamplingCheck
		checkForceSampling(lotName, lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(),
				lotData.getProcessOperationVersion(), lotData.getUdfs().get("RETURNFLOWNAME").toString(), "00001", lotData.getUdfs().get("RETURNOPERATIONNAME").toString(), "00001");
		
		List<Product> producList = LotServiceProxy.getLotService().allProducts(lotName);

		// check ProcessInfo
		List<String> productNameList = new ArrayList<>();
		for (Product productA : producList)
		{
			String productName = productA.getKey().getProductName();
			productNameList.add(productName);
		}

		CommonValidation.checkProductProcessInfobyString(productNameList);

		ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);

		if (processFlowData.getProcessFlowType().equals("Sort"))
			throw new CustomException("LOT-0112");

		if (StringUtils.isNotEmpty(lotData.getMachineName()))
			machineName = lotData.getMachineName();

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		// 2019.12.23 Reqeust by V3.
		lotData = MESLotServiceProxy.getLotServiceUtil().ReleaseHoldLot(eventInfo, lotData);
		eventInfo.setEventName("ForceSample");

		// For Rework
		Map<String, String> changeInfo = null;
		if (StringUtils.equals(processFlowData.getProcessFlowType(), "Rework"))
		{
			changeInfo = getChangeInfoForRework(tabList, lotData);
			eventInfo.setEventName("ForceSampingForRework");
		}
		else
			changeInfo = getChangeInfo(tabList, lotData);

		String returnFlowName = "";
		String returnFlowVersion = "";
		String returnOperationName = "";
		String returnOperationVersion = "";

		String nextReturnFlowName = "";
		String nextReturnFlowVersion = "";
		String nextReturnOperationName = "";
		String nextReturnOperationVersion = "";

		boolean futureFlag = true;

		for (int i = tabList.size() - 1; i > -1; i--)
		{
			List<Element> samplingOperList = SMessageUtil.getSubSequenceItemList(tabList.get(i), "SAMPLINGOPERLIST", true);

			String sampleFlowName = SMessageUtil.getChildText(tabList.get(i), "SAMPLEFLOWNAME", true);
			String sampleFlowVersion = SMessageUtil.getChildText(tabList.get(i), "SAMPLEFLOWVERSION", true);
			String priority = SMessageUtil.getChildText(tabList.get(i), "PRIORITY", true);

			if (StringUtils.isEmpty(returnFlowVersion))
			{
				returnFlowName = lotData.getProcessFlowName();
				returnFlowVersion = lotData.getProcessFlowVersion();
			}
			else
			{
				returnFlowName = nextReturnFlowName;
				returnFlowVersion = nextReturnFlowVersion;
			}

			nextReturnFlowName = sampleFlowName;
			nextReturnFlowVersion = sampleFlowVersion;

			if (!StringUtils.equals(processFlowData.getProcessFlowType(), "Rework"))
			{
				if (StringUtils.equals(sampleFlowName, lotData.getProcessFlowName()))
					throw new CustomException("LOT-0209");
			}

			for (int k = samplingOperList.size() - 1; k > -1; k--)
			{
				String sampleOperationName = samplingOperList.get(k).getChildText("SAMPLINGOPERNAME");
				String sampleOperationVersion = samplingOperList.get(k).getChildText("SAMPLINGOPERVERSION");

				List<Element> productList = SMessageUtil.getSubSequenceItemList(samplingOperList.get(k), "PRODUCTLIST", true);

				List<SampleLot> sampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataList(lotName, lotData.getFactoryName(), lotData.getProductSpecName(),
						lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
						machineName, sampleFlowName, sampleFlowVersion, sampleOperationName, sampleOperationVersion);

				if (sampleLot == null)
				{
					if (StringUtils.isEmpty(returnOperationName))
					{
						returnOperationName = lotData.getProcessOperationName();
						returnOperationVersion = lotData.getProcessOperationVersion();
					}
					else
					{
						returnOperationName = nextReturnOperationName;
						returnOperationVersion = nextReturnOperationVersion;
					}

					nextReturnOperationName = sampleOperationName;
					nextReturnOperationVersion = sampleOperationVersion;

					List<SampleLotCount> sampleLotCount = ExtendedObjectProxy.getSampleLotCountService().getSampleLotCountWithoutToFlow(lotData.getFactoryName(), lotData.getProductSpecName(),
							lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName, sampleOperationName);

					String lotSampleCount = "";
					String currentLotCount = "";
					String totalLotCount = "";
					String productSamplingCount = "";
					String productSamplingPosition = "";

					// Synchronize LotSampleCount with SamplePolicy
					if (sampleLotCount != null)
					{
						lotSampleCount = sampleLotCount.get(0).getLotSampleCount();
						currentLotCount = sampleLotCount.get(0).getCurrentLotCount();
						totalLotCount = sampleLotCount.get(0).getTotalLotCount();
					}

					List<Product> allUnScrappedProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);
					List<String> allUnScrappedProductSList = new ArrayList<String>();

					for (Product unScrappedProductE : allUnScrappedProductList)
					{
						allUnScrappedProductSList.add(unScrappedProductE.getKey().getProductName());
					}

					List<String> actualSamplePositionList = new ArrayList<String>();
					List<Map<String, String>> actualSampleProductMapList = new ArrayList<Map<String, String>>();

					// insert SampleProduct & make actualSamplePositionList
					for (Element productE : productList)
					{
						String productName = productE.getChildText("PRODUCTNAME");
						Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);

						// Validation
						if (!productData.getLotName().equals(lotName))
							throw new CustomException("LOT-0014", lotName, productData.getLotName(), productData.getKey().getProductName());

						if (!allUnScrappedProductSList.contains(productName))
							throw new CustomException("PRODUCT-0018", productName);

						actualSamplePositionList.add(String.valueOf(productData.getPosition()));
						Map<String, String> positionMap=new HashMap<String, String>();
						positionMap.put("PRODUCTNAME", productName);
						positionMap.put("POSITION", String.valueOf(productData.getPosition()));
						actualSampleProductMapList.add(positionMap);
					}
					
					for(Map<String, String> positionInfo:actualSampleProductMapList)
					{
						String productName=positionInfo.get("PRODUCTNAME");
						String positionName=positionInfo.get("POSITION");
						
						ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, productName, lotName, lotData.getFactoryName(), lotData.getProductSpecName(),
								lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),
								machineName, sampleFlowName, sampleFlowVersion, sampleOperationName, sampleOperationVersion, "Y", String.valueOf(actualSamplePositionList.size()), CommonUtil.toStringWithoutBrackets(actualSamplePositionList),
								positionName, "", "Y", "Y", department,reasonCode);
					}

					ExtendedObjectProxy.getSampleLotService().insertForceSampleLot(eventInfo, lotName, lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(),
							lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineName, sampleFlowName,
							sampleFlowVersion, sampleOperationName, sampleOperationVersion, "Y", lotSampleCount, currentLotCount, totalLotCount, productSamplingCount, productSamplingPosition,
							String.valueOf(actualSamplePositionList.size()), CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "Y", "", Integer.parseInt(priority), returnFlowName,
							returnFlowVersion, returnOperationName, returnOperationVersion, "Y", currentFlowName, "00001", currentOperationName, "00001");

					if ((StringUtils.equals(lotData.getFactoryName(), "ARRAY") || StringUtils.equals(lotData.getFactoryName(), "TP")) && futureFlag)
					{
						EventInfo eventInfoNew = (EventInfo) ObjectUtil.copyTo(eventInfo);
						eventInfoNew.setEventComment("CompleteForceSampling+" + this.getEventComment());
						MESLotServiceProxy.getLotServiceImpl().insertCtLotFutureMultiHoldActionForAfter(eventInfoNew, lotName, lotData.getFactoryName(), lotData.getProcessFlowName(),
								lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), "0", "hold", "System", "ReserveHoldLot",
								"CheckForceSamplingDate", "", "", "", "True", "False", eventInfoNew.getEventComment(), "", getEventUser(), "", "Insert", "", "");

						futureFlag = false;
					}
				}
				else
				{
					throw new CustomException("LOT-0018", sampleLot.get(0).getLotName(), sampleLot.get(0).getToProcessOperationName());
				}
			}
		}

		List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUSequence(lotName);

		lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		lotData.setNodeStack(changeInfo.get("CHANGENODESTACK"));

		ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeForceSpecInfo(lotData, lotData.getAreaName(), lotData.getDueDate(), lotData.getFactoryName(),
				lotData.getLotHoldState(), lotData.getLotProcessState(), lotData.getLotState(), lotData.getNodeStack(), lotData.getPriority(), changeInfo.get("CHANGEFLOWNAME"),
				changeInfo.get("CHANGEFLOWVERSION"), changeInfo.get("CHANGEOPERATIONNAME"), changeInfo.get("CHANGEOPERATIONVERSION"), lotData.getProductionType(), lotData.getProductRequestName(), "",
				"", lotData.getProductSpecName(), lotData.getProductSpecVersion(), productUdfs, lotData.getSubProductUnitQuantity1(), lotData.getSubProductUnitQuantity2(),
				changeInfo.get("RETURNFLOWNAME"), changeInfo.get("RETURNOPERATIONNAME"));

		changeSpecInfo.getUdfs().put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());
		changeSpecInfo.getUdfs().put("BEFOREFLOWNAME", lotData.getProcessFlowName());

		Lot aLot = MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);

		MESLotServiceProxy.getLotServiceImpl().deleteCtLotFutureActionByFlow(eventInfo, aLot.getKey().getLotName(), aLot.getFactoryName(), aLot.getProcessFlowName(), aLot.getProcessFlowVersion(),
				"1");

		// Set NextOper ReworkFlag.
		if (StringUtils.equals(processFlowData.getProcessFlowType(), "Rework"))
			MESLotServiceProxy.getLotServiceUtil().setNextOperReworkFlag(eventInfo, aLot);

		// Hold Lot
		makeOnHoldForForceSampling(eventInfo, aLot, oldLotData);

		return doc;
	}

	private Map<String, String> getChangeInfo(List<Element> tabList, Lot lotData) throws CustomException
	{
		String changeFlowName = SMessageUtil.getChildText(tabList.get(0), "SAMPLEFLOWNAME", true);
		String changeFlowVersion = SMessageUtil.getChildText(tabList.get(0), "SAMPLEFLOWVERSION", true);

		List<Element> samplingOperList = SMessageUtil.getSubSequenceItemList(tabList.get(0), "SAMPLINGOPERLIST", true);

		String changeOperationName = SMessageUtil.getChildText(samplingOperList.get(0), "SAMPLINGOPERNAME", true);
		String changeOperationVersion = SMessageUtil.getChildText(samplingOperList.get(0), "SAMPLINGOPERVERSION", true);

		String changeNodeStack = lotData.getNodeStack();

		String returnFlowName = "";
		String returnFlowVersion = "";
		String returnOperationName = "";
		String returnOperationVersion = "";

		Map<String, String> changeInfo = new HashMap<String, String>();

		if (tabList.size() > 1)
		{
			int tabCount = 0;

			for (int i = tabList.size() - 1; i > -1; i--)
			{
				int operCount = 0;

				tabCount += 1;

				String processFlowName = SMessageUtil.getChildText(tabList.get(i), "SAMPLEFLOWNAME", true);
				String processFlowVersion = SMessageUtil.getChildText(tabList.get(i), "SAMPLEFLOWVERSION", true);

				List<Element> operList = SMessageUtil.getSubSequenceItemList(tabList.get(i), "SAMPLINGOPERLIST", true);

				for (int k = operList.size() - 1; k > -1; k--)
				{
					operCount += 1;

					if (tabCount != tabList.size() || operCount != operList.size())
					{
						String processOperationName = SMessageUtil.getChildText(operList.get(k), "SAMPLINGOPERNAME", true);
						String processOperationVersion = SMessageUtil.getChildText(operList.get(k), "SAMPLINGOPERVERSION", true);

						String nodeStack = CommonUtil.getNodeStack(lotData.getFactoryName(), processFlowName, processFlowVersion, processOperationName, processOperationVersion);

						returnFlowName = processFlowName;
						returnFlowVersion = processFlowVersion;
						returnOperationName = processOperationName;
						returnOperationVersion = processOperationVersion;

						changeNodeStack = changeNodeStack + "." + nodeStack;
					}
				}
			}
		}
		else
		{
			if (samplingOperList.size() > 1)
			{
				returnFlowName = SMessageUtil.getChildText(tabList.get(0), "SAMPLEFLOWNAME", true);
				returnFlowVersion = SMessageUtil.getChildText(tabList.get(0), "SAMPLEFLOWVERSION", true);
				returnOperationName = SMessageUtil.getChildText(samplingOperList.get(1), "SAMPLINGOPERNAME", true);
				returnOperationVersion = SMessageUtil.getChildText(samplingOperList.get(1), "SAMPLINGOPERVERSION", true);

				for (int i = samplingOperList.size() - 1; i > 0; i--)
				{
					String processOperationName = SMessageUtil.getChildText(samplingOperList.get(i), "SAMPLINGOPERNAME", true);
					String processOperationVersion = SMessageUtil.getChildText(samplingOperList.get(i), "SAMPLINGOPERVERSION", true);

					String nodeStack = CommonUtil.getNodeStack(lotData.getFactoryName(), returnFlowName, returnFlowVersion, processOperationName, processOperationVersion);

					changeNodeStack = changeNodeStack + "." + nodeStack;
				}
			}
			else
			{
				returnFlowName = lotData.getProcessFlowName();
				returnFlowVersion = lotData.getProcessFlowVersion();
				returnOperationName = lotData.getProcessOperationName();
				returnOperationVersion = lotData.getProcessOperationVersion();
			}
		}

		changeInfo.put("CHANGEFLOWNAME", changeFlowName);
		changeInfo.put("CHANGEFLOWVERSION", changeFlowVersion);
		changeInfo.put("CHANGEOPERATIONNAME", changeOperationName);
		changeInfo.put("CHANGEOPERATIONVERSION", changeOperationVersion);
		changeInfo.put("CHANGENODESTACK", changeNodeStack);

		changeInfo.put("RETURNFLOWNAME", returnFlowName);
		changeInfo.put("RETURNFLOWVERSION", returnFlowVersion);
		changeInfo.put("RETURNOPERATIONNAME", returnOperationName);
		changeInfo.put("RETURNOPERATIONVERSION", returnOperationVersion);

		return changeInfo;
	}

	private Map<String, String> getChangeInfoForRework(List<Element> tabList, Lot lotData) throws CustomException
	{
		String changeFlowName = SMessageUtil.getChildText(tabList.get(0), "SAMPLEFLOWNAME", true);
		String changeFlowVersion = SMessageUtil.getChildText(tabList.get(0), "SAMPLEFLOWVERSION", true);
		String priority = SMessageUtil.getChildText(tabList.get(0), "PRIORITY", true);
		
		List<Element> samplingOperList = SMessageUtil.getSubSequenceItemList(tabList.get(0), "SAMPLINGOPERLIST", true);
		String changeOperationName = SMessageUtil.getChildText(samplingOperList.get(0), "SAMPLINGOPERNAME", true);
		String changeOperationVersion = SMessageUtil.getChildText(samplingOperList.get(0), "SAMPLINGOPERVERSION", true);

		String changeNodeStack = lotData.getNodeStack();
		
		for (int i = tabList.size() - 1; i > 0; i--)
		{
			List<Element> samplingOper = SMessageUtil.getSubSequenceItemList(tabList.get(i), "SAMPLINGOPERLIST", true);
			
			String processOperationName = SMessageUtil.getChildText(samplingOper.get(0), "SAMPLINGOPERNAME", true);
			String processOperationVersion = SMessageUtil.getChildText(samplingOper.get(0), "SAMPLINGOPERVERSION", true);

			String nodeStack = CommonUtil.getNodeStack(lotData.getFactoryName(), changeFlowName, changeFlowVersion, processOperationName, processOperationVersion);
			changeNodeStack = changeNodeStack + "." + nodeStack;
		}

		String returnFlowName = lotData.getProcessFlowName();
		String returnFlowVersion = lotData.getProcessFlowVersion();
		String returnOperationName = lotData.getProcessOperationName();
		String returnOperationVersion = lotData.getProcessOperationVersion();

		Map<String, String> changeInfo = new HashMap<String, String>();

		changeInfo.put("CHANGEFLOWNAME", changeFlowName);
		changeInfo.put("CHANGEFLOWVERSION", changeFlowVersion);
		changeInfo.put("CHANGEOPERATIONNAME", changeOperationName);
		changeInfo.put("CHANGEOPERATIONVERSION", changeOperationVersion);
		changeInfo.put("CHANGENODESTACK", changeNodeStack);

		changeInfo.put("RETURNFLOWNAME", returnFlowName);
		changeInfo.put("RETURNFLOWVERSION", returnFlowVersion);
		changeInfo.put("RETURNOPERATIONNAME", returnOperationName);
		changeInfo.put("RETURNOPERATIONVERSION", returnOperationVersion);

		return changeInfo;
	}

	private void makeOnHoldForForceSampling(EventInfo eventInfo, Lot lotData, Lot oldLotData) throws CustomException
	{

		if (StringUtil.equals(oldLotData.getLotHoldState(), GenericServiceProxy.getConstantMap().Lot_OnHold))
		{
			eventInfo.setEventName("Hold");
			eventInfo.setReasonCode("SYSTEM");
			eventInfo.setEventComment("Start ForceSampling [" + lotData.getKey().getLotName() + "], " + eventInfo.getEventComment());

			Map<String, String> udfs = new HashMap<String, String>();

			if (StringUtil.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Released) && StringUtil.equals(lotData.getLotProcessState(), "WAIT"))
			{
				MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, lotData, udfs);
			}
			else
			{
				throw new CustomException("LOT-0113", lotData.getLotState(), lotData.getLotProcessState());
			}
		}
	}
	
	public void checkForceSampling(String lotName, String factoryName, String productSpecName, String productSpecVersion, String toProcessFlowName, String toProcessFlowVersion, String toProcessOperationName,
			String toProcessOperationVersion, String returnProcessFlowName, String returnProcessFlowVersion, String returnProcessOperationName, String returnProcessOperationVersion) throws greenFrameDBErrorSignal, CustomException
	{
		String condition = "LOTNAME = ? AND FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PRODUCTSPECVERSION = ? AND TOPROCESSFLOWNAME = ? AND TOPROCESSFLOWVERSION = ? AND TOPROCESSOPERATIONNAME = ? AND TOPROCESSOPERATIONVERSION = ? AND RETURNPROCESSFLOWNAME = ? AND RETURNPROCESSFLOWVERSION = ? AND RETURNOPERATIONNAME = ? AND RETURNOPERATIONVERSION = ? AND FORCESAMPLINGFLAG = 'Y'";
		Object[] bindSet = new Object[] { lotName, factoryName, productSpecName, productSpecVersion, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion, returnProcessFlowName,
				returnProcessFlowVersion, returnProcessOperationName, returnProcessOperationVersion };

		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();

		try
		{
			sampleLotList = ExtendedObjectProxy.getSampleLotService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			sampleLotList = null;
		}

		if(sampleLotList != null && sampleLotList.size() > 0)
		{
			throw new CustomException("LOT-3008");
		}
	}
}
