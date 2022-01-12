package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SampleLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.NodeStack;
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

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ChangeLotSamplingPriority extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		// for common
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeSamplePriority", getEventUser(), getEventComment(), null, null);

		boolean isInSampleFlow = false;
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);

		List<Element> flowList = SMessageUtil.getBodySequenceItemList(doc, "SAMPLINGFLOWLIST", true);
		List<String> positionList = CommonUtil.makeList(SMessageUtil.getBodyElement(doc), "SAMPLINGFLOWLIST", "POSITION");
		String minPriority = minPriority(positionList);
		String changeFlowName = "";
		String changeFlowVersion = "";
		String changeOperName = "";
		String changeOperVersion = "";

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		CommonValidation.checkLotState(lotData);
		CommonValidation.checkLotProcessState(lotData);
		CommonValidation.checkLotHoldState(lotData);
		CommonValidation.checkJobDownFlag(lotData);

		ExtendedObjectProxy.getSampleLotService().checkLotForceSampling(lotData);

		List<Product> producList = LotServiceProxy.getLotService().allProducts(lotName);

		// check ProcessInfo
		List<String> productNameList = new ArrayList<>();
		for (Product productA : producList)
		{
			String productName = productA.getKey().getProductName();
			productNameList.add(productName);
		}

		CommonValidation.checkProductProcessInfobyString(productNameList);

		// 1. set Sampling Lot List by Lot
		List<SampleLot> sampleLotList = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListBySpec(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
				lotData.getProductSpecVersion());

		for (SampleLot sampleLot : sampleLotList)
		{
			String toFlowName = sampleLot.getToProcessFlowName();
			String toFlowVersion = sampleLot.getToProcessFlowVersion();
			String toOperName = sampleLot.getToProcessOperationName();
			String toOperVersion = sampleLot.getToProcessOperationVersion();

			for (Element flowE : flowList)
			{
				String samplingFlowName = flowE.getChildText("SAMPLINGFLOWNAME");

				if (StringUtils.equals(toFlowName, samplingFlowName))
				{
					String priority = flowE.getChildText("POSITION");

					if (StringUtils.equals(lotData.getProcessFlowName(), toFlowName) && StringUtils.equals(lotData.getProcessOperationName(), toOperName))
						isInSampleFlow = true;

					if (StringUtils.equals(priority, minPriority) && !StringUtils.equals(toFlowName, lotData.getProcessFlowName()))
					{
						changeFlowName = toFlowName;
						changeFlowVersion = toFlowVersion;
						changeOperName = toOperName;
						changeOperVersion = toOperVersion;
					}

					ExtendedObjectProxy.getSampleLotService().updatePriority(eventInfo, lotName, samplingFlowName, toFlowVersion, priority);

					break;
				}
			}
		}

		if (isInSampleFlow && StringUtils.isNotEmpty(changeFlowName) && StringUtils.isNotEmpty(changeOperName))
			changeFlow(eventInfo, lotData, changeFlowName, changeFlowVersion, changeOperName, changeOperVersion);

		return doc;
	}

	private String minPriority(List<String> positionList)
	{
		int iMinPriority = 99;
		String minPriority = "";

		for (String position : positionList)
		{
			int iPosition = Integer.parseInt(position);

			if (iPosition < iMinPriority)
				iMinPriority = iPosition;
		}

		minPriority = String.valueOf(iMinPriority);

		return minPriority;
	}

	private void changeFlow(EventInfo eventInfo, Lot lotData, String toFlowName, String toFlowVersion, String toOperName, String toOperVersion) throws CustomException
	{
		// Get Node Info
		String toNodeId = NodeStack.getNodeID(lotData.getFactoryName(), toFlowName, toOperName, toOperVersion);
		Node nextNode = ProcessFlowServiceProxy.getProcessFlowService().getNode(toNodeId);

		String[] nodeStackArray = StringUtil.split(lotData.getNodeStack(), ".");
		int idx = nodeStackArray.length;

		// Set Node Stack
		nodeStackArray[idx - 1] = nextNode.getKey().getNodeId();
		String nextNodeId = StringUtils.join(nodeStackArray, ".");

		// Change Flow
		// Mantis 0000024
		// eventInfo.setEventName("ChangeFlow");
		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo(lotData.getProductionType(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProductSpec2Name(),
				lotData.getProductSpec2Version(), lotData.getProductRequestName(), lotData.getSubProductUnitQuantity1(), lotData.getSubProductUnitQuantity2(), lotData.getDueDate(),
				lotData.getPriority(), lotData.getFactoryName(), lotData.getAreaName(), lotData.getLotState(), lotData.getLotProcessState(), lotData.getLotHoldState(), toFlowName, toFlowVersion,
				toOperName, toOperVersion, nextNodeId, new ArrayList<ProductU>());

		MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
	}
}
