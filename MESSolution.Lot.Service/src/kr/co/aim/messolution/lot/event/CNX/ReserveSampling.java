package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

import org.jdom.Document;
import org.jdom.Element;

public class ReserveSampling extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String fromProcessFlowName = SMessageUtil.getBodyItemValue(doc, "FROMPROCESSFLOWNAME", true);
		String fromProcessFlowVersion = SMessageUtil.getBodyItemValue(doc, "FROMPROCESSFLOWVERSION", true);
		String fromOperationName = SMessageUtil.getBodyItemValue(doc, "FROMOPERATIONNAME", true);
		String fromOperationVersion = SMessageUtil.getBodyItemValue(doc, "FROMOPERATIONVERSION", true);
		String toFlowName = SMessageUtil.getBodyItemValue(doc, "SAMPLINGFLOWNAME", true);
		String toFlowVersion = SMessageUtil.getBodyItemValue(doc, "SAMPLINGFLOWVERSION", true);
		String returnProcessFlowName = SMessageUtil.getBodyItemValue(doc, "RETURNPROCESSFLOWNAME", true);
		String returnProcessFlowVersion = SMessageUtil.getBodyItemValue(doc, "RETURNPROCESSFLOWVERSION", true);
		String returnOperationName = SMessageUtil.getBodyItemValue(doc, "RETURNOPERATIONNAME", true);
		String returnOperationVersion = SMessageUtil.getBodyItemValue(doc, "RETURNOPERATIONVERSION", true);

		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		CommonValidation.checkDummyProductReserve(lotData);
		
		// AR-AMF-0030-01
		// Check the existence of MainReserveSkip data
		CommonValidation.checkMainReserveSkipData(lotData, fromOperationName, fromOperationVersion);
		CommonValidation.checkDummyProductReserve(lotData);

		// Set SampleLot Info by SamplingFlowList
		int samplePriority = MESLotServiceProxy.getLotServiceUtil().getSamplePriority(lotData.getFactoryName(), fromProcessFlowName, fromProcessFlowVersion, fromOperationName, fromOperationVersion,
				toFlowName, toFlowVersion);

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
			List<SampleLot> sampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListWithOutMachineName(lotData.getKey().getLotName(), lotData.getFactoryName(),
					lotData.getProductSpecName(), lotData.getProductSpecVersion(), fromProcessFlowName, fromProcessFlowVersion, fromOperationName, fromOperationVersion, toFlowName, toFlowVersion,
					toProcessOperationName, toProcessOperationVersion);

			if (sampleLot == null)
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

					// set SamplingProduct Data(CT_SAMPLEPRODUCT)
					ExtendedObjectProxy.getSampleProductService().insertReserveSampleProduct(eventInfo, productName, lotName, lotData.getFactoryName(), lotData.getProductSpecName(),
							lotData.getProductSpecVersion(), fromProcessFlowName, fromProcessFlowVersion, fromOperationName, fromOperationVersion, machineName, toFlowName, toFlowVersion,
							toProcessOperationName, toProcessOperationVersion, "Y", productSamplingCount, productSamplingPosition, String.valueOf(productData.getPosition()), "", "Y");

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
				}

				// set SampleLotData(CT_SAMPLELOT)
				ExtendedObjectProxy.getSampleLotService().insertReserveSampleLot(eventInfo, lotName, lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(),
						fromProcessFlowName, fromProcessFlowVersion, fromOperationName, fromOperationVersion, machineName, toFlowName, toFlowVersion, toProcessOperationName,
						toProcessOperationVersion, "Y", lotSampleCount, currentLotCount, totalLotCount, productSamplingCount, productSamplingPosition, String.valueOf(actualSamplePositionList.size()),
						CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "Y", "", samplePriority, returnProcessFlowName, returnProcessFlowVersion, returnOperationName,
						returnOperationVersion, "");
			}
			else
			{
				throw new CustomException("LOT-0018", sampleLot.get(0).getLotName(), sampleLot.get(0).getToProcessOperationName());
			}
		}

		return doc;
	}
}
