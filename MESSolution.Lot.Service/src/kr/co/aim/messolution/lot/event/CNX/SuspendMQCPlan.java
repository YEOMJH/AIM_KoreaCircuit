package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCPlan;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class SuspendMQCPlan extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		String jobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);

		MQCPlan planData = ExtendedObjectProxy.getMQCPlanService().selectByKey(true, new Object[] { jobName });

		// MQC validation
		if (!planData.getMQCState().equalsIgnoreCase("Released"))
		{
			//MQC-0012:MQC plan should be running
			throw new CustomException("MQC-0012");
		}

		String lotName = planData.getLotName();
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		ProcessFlow sourceProcessFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);
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
		CommonValidation.checkLotReworkState(lotData);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);

		String[] step1 = StringUtils.split(lotData.getNodeStack(), ".");
		String targetNodeStack = step1[0];

		Node targetNode = ProcessFlowServiceProxy.getProcessFlowService().getNode(targetNodeStack);

		String targetFlowName = targetNode.getProcessFlowName();
		String targetFlowVersion = targetNode.getProcessFlowVersion();
		String targetOperationName = targetNode.getNodeAttribute1();
		String targetOperationVersion = targetNode.getNodeAttribute2();
		String preProductSpecName = planData.getPrepareSpecName();
		String preProductSpecVersion = planData.getPrepareSpecVersion();
		String returnFlowName = lotData.getProcessFlowName();
		String returnFlowVersion = lotData.getProcessFlowVersion();
		String returnOperationName = lotData.getProcessOperationName();
		String returnOperationVersion = lotData.getProcessOperationVersion();

		
		// move to MQC main flow except already in there
		if (!sourceProcessFlowData.getProcessFlowType().equals("MQCPrepare")&&(!lotData.getProcessFlowName().equals(targetFlowName) || !lotData.getProcessOperationName().equals(targetOperationName)))
		{
			// suspend MQC plan
			eventInfo.setEventName("SuspendMQC");
			ExtendedObjectProxy.getMQCPlanService().updateMQCPlanReturnInfo(eventInfo, planData, "Suspending", returnFlowName, returnFlowVersion, returnOperationName, returnOperationVersion);

			ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo(lotData.getProductionType(), preProductSpecName, preProductSpecVersion, lotData.getProductSpec2Name(), lotData.getProductSpec2Version(),
					lotData.getProductRequestName(), lotData.getSubProductUnitQuantity1(), lotData.getSubProductUnitQuantity2(), lotData.getDueDate(), lotData.getPriority(), lotData.getFactoryName(),
					lotData.getAreaName(), lotData.getLotState(), lotData.getLotProcessState(), lotData.getLotHoldState(), targetFlowName, targetFlowVersion, targetOperationName,
					targetOperationVersion, targetNodeStack, new ArrayList<ProductU>());

			changeSpecInfo.setUdfs(lotData.getUdfs());
			changeSpecInfo.getUdfs().put("RETURNFLOWNAME", "");
			changeSpecInfo.getUdfs().put("RETURNOPERATIONNAME", "");
			changeSpecInfo.getUdfs().put("RETURNOPERATIONVER", "");
			changeSpecInfo.getUdfs().put("BEFOREFLOWNAME", returnFlowName);
			changeSpecInfo.getUdfs().put("BEFOREOPERATIONNAME", returnOperationName);

			eventInfo.setEventName("SuspendMQC");
			MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
		}
		else
		{
			throw new CustomException("MQC-0001");
		}
		
		return doc;
	}
}
