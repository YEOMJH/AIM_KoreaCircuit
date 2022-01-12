package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SampleLot;
import kr.co.aim.messolution.extended.object.management.data.SampleLotCount;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;

public class ReserveBatchSampling extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String department = SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", false);
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);
		
		List<Element> reserveSampleList = SMessageUtil.getBodySequenceItemList(doc, "RESERVESAMPLELIST", true);

		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
		CommonValidation.checkDummyProductReserve(lotData);
		
		for (Element reserveSample : reserveSampleList)
		{
			String fromProcessFlowName = SMessageUtil.getChildText(reserveSample, "FROMPROCESSFLOWNAME", true);
			String fromProcessFlowVersion = SMessageUtil.getChildText(reserveSample, "FROMPROCESSFLOWVERSION", true);
			String fromOperationName = SMessageUtil.getChildText(reserveSample, "FROMOPERATIONNAME", true);
			String fromOperationVersion = SMessageUtil.getChildText(reserveSample, "FROMOPERATIONVERSION", true);
			String toFlowName = SMessageUtil.getChildText(reserveSample, "SAMPLINGFLOWNAME", true);
			String toFlowVersion = SMessageUtil.getChildText(reserveSample, "SAMPLINGFLOWVERSION", true);
			String returnProcessFlowName = SMessageUtil.getChildText(reserveSample, "RETURNPROCESSFLOWNAME", true);
			String returnProcessFlowVersion = SMessageUtil.getChildText(reserveSample, "RETURNPROCESSFLOWVERSION", true);
			String returnOperationName = SMessageUtil.getChildText(reserveSample, "RETURNOPERATIONNAME", true);
			String returnOperationVersion = SMessageUtil.getChildText(reserveSample, "RETURNOPERATIONVERSION", true);

			List<Element> productList = SMessageUtil.getSubSequenceItemList(reserveSample, "PRODUCTLIST", true);

			String machineName = "NA";

			// AR-AMF-0030-01
			// Check the existence of MainReserveSkip data
			CommonValidation.checkMainReserveSkipData(lotData, fromOperationName, fromOperationVersion);

			// Set SampleLot Info by SamplingFlowList
			int samplePriority = MESLotServiceProxy.getLotServiceUtil().getSamplePriority(lotData.getFactoryName(), fromProcessFlowName, fromProcessFlowVersion, fromOperationName,
					fromOperationVersion, toFlowName, toFlowVersion);

			boolean isSampleFlow = MESLotServiceProxy.getLotServiceUtil().isSampleFlow(lotData.getFactoryName(), toFlowName, toFlowVersion);

			if (!isSampleFlow)
				throw new CustomException("LOT-0208");

			List<Map<String, Object>> operationList = MESLotServiceProxy.getLotServiceUtil().operationList(lotData.getFactoryName(), toFlowName, toFlowVersion);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReserveSample", getEventUser(), getReserveSampleEventComment(fromOperationName, operationList), null, null);
			eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

			for (Map<String, Object> map : operationList)
			{
				String toProcessOperationName = ConvertUtil.getMapValueByName(map, "PROCESSOPERATIONNAME");
				String toProcessOperationVersion = ConvertUtil.getMapValueByName(map, "PROCESSOPERATIONVERSION");

				// 1. set Sampling Lot Data
				// get SampleLotData(CT_SAMPLELOT)
				List<SampleLot> sampleLotList = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListWithOutMachineName(lotData.getKey().getLotName(), lotData.getFactoryName(),
						lotData.getProductSpecName(), lotData.getProductSpecVersion(), fromProcessFlowName, fromProcessFlowVersion, fromOperationName, fromOperationVersion, toFlowName, toFlowVersion,
						toProcessOperationName, toProcessOperationVersion);

				if (sampleLotList == null)
				{
					// get SamplingLotCount(CT_SAMPLELOTCOUNT)
					List<SampleLotCount> sampleLotCount = ExtendedObjectProxy.getSampleLotCountService().getSampleLotCountWithoutToFlow(lotData.getFactoryName(), lotData.getProductSpecName(),
							fromProcessFlowName, fromOperationName, lotData.getMachineName(), toProcessOperationName);

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
					List<String> SamplePositionListWithOutDuplicated = new ArrayList<String>();
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

						boolean isFound = false;
						for(int i = 0 ; i < actualSamplePositionList.size() ; i++)
						{
							if(StringUtil.equals(actualSamplePositionList.get(i), String.valueOf(productData.getPosition())))
							{
								isFound = true;
								break;
							}
						}
						if(!isFound)
							actualSamplePositionList.add(String.valueOf(productData.getPosition()));
						
						Map<String, String> positionMap=new HashMap<String, String>();
						positionMap.put("PRODUCTNAME", productName);
						positionMap.put("POSITION", String.valueOf(productData.getPosition()));
						actualSampleProductMapList.add(positionMap);
						SamplePositionListWithOutDuplicated.add(String.valueOf(productData.getPosition()));
					}
					
					for(Map<String, String> positionInfo:actualSampleProductMapList)
					{
						String productName=positionInfo.get("PRODUCTNAME");
						String positionName=positionInfo.get("POSITION");
						
						ExtendedObjectProxy.getSampleProductService().insertReserveSampleProduct(eventInfo, productName, lotName, lotData.getFactoryName(), lotData.getProductSpecName(),
								lotData.getProductSpecVersion(), fromProcessFlowName, fromProcessFlowVersion, fromOperationName, fromOperationVersion, machineName, toFlowName, toFlowVersion,
								toProcessOperationName, toProcessOperationVersion, "Y", String.valueOf(SamplePositionListWithOutDuplicated.size()),
								CommonUtil.toStringWithoutBrackets(SamplePositionListWithOutDuplicated), positionName, "", "Y", "", department,reasonCode);
					}

					// set SampleLotData(CT_SAMPLELOT)
					ExtendedObjectProxy.getSampleLotService().insertReserveSampleLot(eventInfo, lotName, lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(),
							fromProcessFlowName, fromProcessFlowVersion, fromOperationName, fromOperationVersion, machineName, toFlowName, toFlowVersion, toProcessOperationName,
							toProcessOperationVersion, "Y", lotSampleCount, currentLotCount, totalLotCount, productSamplingCount, productSamplingPosition,
							String.valueOf(actualSamplePositionList.size()), CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "Y", "", samplePriority, returnProcessFlowName,
							returnProcessFlowVersion, returnOperationName, returnOperationVersion, "");
				}
				else
				{
					for (SampleLot sampleLot : sampleLotList)
					{
						// Validation Force or Manual SampleData
						if (!StringUtils.equals(sampleLot.getForceSamplingFlag(), "Y") && StringUtils.equals(sampleLot.getManualSampleFlag(), "Y"))
						{
							String actualSamplePosition = sampleLot.getActualSamplePosition();

							String[] actualPositionArray = actualSamplePosition.split(",");
							List<String> actualSamplePositionList = new ArrayList<String>();
							List<String> SamplePositionListWithOutDuplicated = new ArrayList<String>();
							List<Map<String, String>> actualSampleProductMapList = new ArrayList<Map<String, String>>();

							for (int i = 0; i < actualPositionArray.length; i++)
							{
								actualSamplePositionList.add(actualPositionArray[i].trim());
							}

							List<Product> allUnScrappedProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);
							List<String> allUnScrappedProductSList = new ArrayList<String>();

							for (Product unScrappedProductE : allUnScrappedProductList)
							{
								allUnScrappedProductSList.add(unScrappedProductE.getKey().getProductName());
							}

							for (Element productE : productList)
							{
								String productName = productE.getChildText("PRODUCTNAME");
								Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);

								// Validation
								if (!productData.getLotName().equals(lotName))
									throw new CustomException("LOT-0014", lotName, productData.getLotName(), productData.getKey().getProductName());

								if (!allUnScrappedProductSList.contains(productName))
									throw new CustomException("PRODUCT-0018", productName);

								boolean isFound = false;
								for(int i = 0 ; i < actualSamplePositionList.size() ; i++)
								{
									if(StringUtil.equals(actualSamplePositionList.get(i), String.valueOf(productData.getPosition())))
									{
										isFound = true;
										break;
									}
								}
								if(!isFound)
									actualSamplePositionList.add(String.valueOf(productData.getPosition()));
								
								Map<String, String> positionMap=new HashMap<String, String>();
								positionMap.put("PRODUCTNAME", productName);
								positionMap.put("POSITION", String.valueOf(productData.getPosition()));
								actualSampleProductMapList.add(positionMap);
								SamplePositionListWithOutDuplicated.add(String.valueOf(productData.getPosition()));
							}
							
							for(Map<String, String> positionInfo:actualSampleProductMapList)
							{
								String productName=positionInfo.get("PRODUCTNAME");
								String positionName=positionInfo.get("POSITION");
								
								ExtendedObjectProxy.getSampleProductService().insertReserveSampleProduct(eventInfo, productName, lotName, lotData.getFactoryName(), lotData.getProductSpecName(),
										lotData.getProductSpecVersion(), fromProcessFlowName, fromProcessFlowVersion, fromOperationName, fromOperationVersion, machineName, toFlowName, toFlowVersion,
										toProcessOperationName, toProcessOperationVersion, "Y", String.valueOf(SamplePositionListWithOutDuplicated.size()),
										CommonUtil.toStringWithoutBrackets(SamplePositionListWithOutDuplicated), positionName, "", "Y", "", department,reasonCode);
							}

							actualSamplePositionList = CommonUtil.sort(actualSamplePositionList);

							// set SampleLotData(CT_SAMPLELOT)
							ExtendedObjectProxy.getSampleLotService().updateSampleLot(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
									lotData.getProductSpecVersion(), fromProcessFlowName, fromProcessFlowVersion, fromOperationName, fromOperationVersion, machineName, toFlowName, toFlowVersion,
									toProcessOperationName, toProcessOperationVersion, "", "", "", "", "", "", String.valueOf(actualSamplePositionList.size()),
									CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "", "", "", "", "", "", "", "");
						}
					}

					// throw new CustomException("LOT-0018", sampleLot.get(0).getLotName(), sampleLot.get(0).getToProcessOperationName());
				}
			}
		}

		return doc;
	}
}
