package kr.co.aim.messolution.lot.event.CNX;

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
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class CompleteRuleSample extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String returnFlowName = SMessageUtil.getBodyItemValue(doc, "RETURNFLOWNAME", true);
		String returnOperationName = SMessageUtil.getBodyItemValue(doc, "RETURNOPERATION", true);
		String returnOperationVer = SMessageUtil.getBodyItemValue(doc, "RETURNOPERATIONVER", false);
		if (!StringUtil.isNotEmpty(returnOperationVer))
			returnOperationVer = "00001";
		String beforeOperationName = "";

		Element element = doc.getDocument().getRootElement();

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		CommonValidation.checkLotState(lotData);
		CommonValidation.checkLotHoldState(lotData);
		CommonValidation.checkLotProcessStateWait(lotData);
		CommonValidation.checkJobDownFlag(lotData);

		List<Product> producList = LotServiceProxy.getLotService().allProducts(lotName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CompleteRuleSample", getEventUser(), getEventComment(), "", "");

		// check ProcessInfo
		List<String> productNameList = new ArrayList<>();
		for (Product productA : producList)
		{
			String productName = productA.getKey().getProductName();
			productNameList.add(productName);
		}

		CommonValidation.checkProductProcessInfobyString(productNameList);

		deleteSamplingForCompleteRuleSampling(eventInfo, lotData, new ArrayList<Element>(), true);

		Map<String, String> udfs = MESLotServiceProxy.getLotServiceUtil().setNamedValueSequence(lotName, element);

		beforeOperationName = MESLotServiceProxy.getLotServiceUtil().getBeforeOperationName(returnFlowName, returnOperationName);
		udfs.put("BEFOREOPERATIONNAME", beforeOperationName);

		List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUSequence(lotName);

		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();
		if (StringUtil.equals(lotData.getLotProcessState(), "WAIT"))
		{
			changeSpecInfo.setAreaName(lotData.getAreaName());
			changeSpecInfo.setDueDate(lotData.getDueDate());
			changeSpecInfo.setFactoryName(lotData.getFactoryName());
			changeSpecInfo.setLotHoldState(lotData.getLotHoldState());
			changeSpecInfo.setLotProcessState(lotData.getLotProcessState());
			changeSpecInfo.setLotState(lotData.getLotState());
			changeSpecInfo.setNodeStack("");
			changeSpecInfo.setPriority(lotData.getPriority());
			changeSpecInfo.setProcessFlowName(returnFlowName);
			changeSpecInfo.setProcessFlowVersion("00001");
			changeSpecInfo.setProcessOperationName(returnOperationName);
			changeSpecInfo.setProcessOperationVersion(returnOperationVer);
			changeSpecInfo.setProductionType(lotData.getProductionType());
			changeSpecInfo.setProductRequestName(lotData.getProductRequestName());
			changeSpecInfo.setProductSpec2Name(lotData.getProductSpec2Name());
			changeSpecInfo.setProductSpec2Version(lotData.getProductSpec2Version());
			changeSpecInfo.setProductSpecName(lotData.getProductSpecName());
			changeSpecInfo.setProductSpecVersion(lotData.getProductSpecVersion());
			changeSpecInfo.setProductUSequence(productUdfs);
			changeSpecInfo.setSubProductUnitQuantity1(lotData.getSubProductUnitQuantity1());
			changeSpecInfo.setSubProductUnitQuantity2(lotData.getSubProductUnitQuantity2());
			
			changeSpecInfo.getUdfs().put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());
			changeSpecInfo.getUdfs().put("BEFOREFLOWNAME", lotData.getProcessFlowName());
			changeSpecInfo.getUdfs().put("BACKUPMAINOPERNAME", "");
			changeSpecInfo.getUdfs().put("BACKUPMAINFLOWNAME", ""); 
			
			String[][] returnNodeResult = getReturnNode(changeSpecInfo);

			if (returnNodeResult.length == 0)
			{
				throw new CustomException("Node-0001", lotData.getProductSpecName(), changeSpecInfo.getProcessFlowName(), changeSpecInfo.getProcessOperationName());
			}
			else
			{
				String sToBeNodeStack = (String) returnNodeResult[0][0];

				ProcessFlowKey processFlowKey = new ProcessFlowKey();
				processFlowKey.setFactoryName((String) returnNodeResult[0][3]);
				processFlowKey.setProcessFlowName((String) returnNodeResult[0][1]);
				processFlowKey.setProcessFlowVersion("00001");
				ProcessFlow returnFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);

				// After doing Rework -> RuleSampling
				if ((StringUtil.equals(returnFlowData.getProcessFlowType(), "Main") || StringUtil.equals(returnFlowData.getProcessFlowType(), "MQC")))
				{
					changeSpecInfo.getUdfs().put("RETURNFLOWNAME", "");
					changeSpecInfo.getUdfs().put("RETURNOPERATIONNAME", "");
				}
				else
				{
					String currentNode = lotData.getNodeStack();
					if (StringUtil.isNotEmpty(currentNode) && currentNode.indexOf(".") > 0)
					{
						int NodeStackCount = getNodeStackCount(currentNode, '.');
						String originalNode = currentNode.substring(0, currentNode.lastIndexOf("."));

						String returnForOriginalNode = originalNode;

						if (NodeStackCount >= 3)
						{
							if (returnForOriginalNode.lastIndexOf(".") > -1)
								returnForOriginalNode = returnForOriginalNode.substring(0, returnForOriginalNode.lastIndexOf("."));

							returnForOriginalNode = returnForOriginalNode.substring(returnForOriginalNode.lastIndexOf(".") + 1, returnForOriginalNode.length());
						}
						else
						{
							if (returnForOriginalNode.lastIndexOf(".") > -1)
								returnForOriginalNode = returnForOriginalNode.substring(0, returnForOriginalNode.lastIndexOf("."));
						}

						String[][] originalNodeResult = getOriginalNode(returnForOriginalNode);

						if (originalNodeResult.length > 0)
						{
							changeSpecInfo.getUdfs().put("RETURNFLOWNAME", (String) originalNodeResult[0][1]);
							changeSpecInfo.getUdfs().put("RETURNOPERATIONNAME", (String) originalNodeResult[0][2]);

							sToBeNodeStack = originalNode;
						}
					}
				}

				changeSpecInfo.setNodeStack(sToBeNodeStack);
			}
		}
		else
		{
			throw new CustomException("LOT-9003", lotName + "(LotProcessState:" + lotData.getLotProcessState() + ")");
		}

		Lot afterLotData = MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);

		afterLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());

		// FirstGlass
		afterLotData = MESLotServiceProxy.getLotServiceUtil().excuteFirstGlass(eventInfo, lotData, afterLotData, "N");

		// Skip
		afterLotData = MESLotServiceProxy.getLotServiceUtil().executePostAction(eventInfo, lotData, afterLotData, false);

		
		// If firstGlass Lot is not process hold. 2021-03-31 #0000464
		if ( StringUtils.isEmpty(afterLotData.getUdfs().get("JOBNAME")))
		{
			if (StringUtils.isEmpty(afterLotData.getUdfs().get("FIRSTGLASSALL")))
			{
				// Hold
				makeOnHoldForCompleteRuleSample(eventInfo, afterLotData);
			}
		}
		
		return doc;
	}

	private int getNodeStackCount(String str, char c)
	{
		int count = 0;
		for (int i = 0; i < str.length(); i++)
		{
			if (str.charAt(i) == c)
				count++;
		}
		return count;
	}

	private void deleteSamplingForCompleteRuleSampling(EventInfo eventInfo, Lot lotData, List<Element> productList, boolean isManual) throws CustomException
	{
		// Get Current Sample Data
		List<SampleLot> sampleLotList = ExtendedObjectProxy.getSampleLotService().getForceSampleLotDataListbyToProcessFlow(lotData.getKey().getLotName(), lotData.getFactoryName(),
				lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion());

		if (sampleLotList != null)
		{
			// Get Sample Data from Before Operation
			String beforeFlow = sampleLotList.get(0).getProcessFlowName();
			String beforeFlowVersion = sampleLotList.get(0).getProcessFlowVersion();
			String beforeOper = sampleLotList.get(0).getProcessOperationName();
			String beforeOperVersion = sampleLotList.get(0).getProcessOperationVersion();

			sampleLotList = ExtendedObjectProxy.getSampleLotService().getForceSampleLotDataList(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
					lotData.getProductSpecVersion(), beforeFlow, beforeFlowVersion, beforeOper, beforeOperVersion);

			for (SampleLot sampleLotM : sampleLotList)
			{
				List<SampleProduct> sampleProductList = new ArrayList<SampleProduct>();

				if (isManual)
				{
					ExtendedObjectProxy.getSampleProductService().deleteSampleProductByLotNameAndToInfo(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(),
							lotData.getProductSpecName(), lotData.getProductSpecVersion(), sampleLotM.getToProcessFlowName(), sampleLotM.getToProcessFlowVersion(),
							sampleLotM.getToProcessOperationName(), sampleLotM.getToProcessOperationVersion());
				}
				else
				{
					sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListByLotName(lotData.getKey().getLotName(), lotData.getFactoryName(),
							lotData.getProductSpecName(), lotData.getProductSpecVersion(), sampleLotM.getProcessFlowName(), sampleLotM.getProcessFlowVersion(), sampleLotM.getProcessOperationName(),
							sampleLotM.getProcessOperationVersion(), sampleLotM.getMachineName(), sampleLotM.getToProcessFlowName(), sampleLotM.getToProcessFlowVersion(),
							sampleLotM.getToProcessOperationName(), sampleLotM.getToProcessOperationVersion());

					for (SampleProduct sampleProductM : sampleProductList)
					{
						for (Element productE : productList)
						{
							try
							{
								if (StringUtils.equals(productE.getChildText("PRODUCTNAME"), sampleProductM.getProductName()))
								{
									ExtendedObjectProxy.getSampleProductService().deleteSampleProductByToInfo(eventInfo, productE.getChildText("PRODUCTNAME"), lotData.getKey().getLotName(),
											lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), sampleLotM.getToProcessFlowName(),
											sampleLotM.getToProcessFlowVersion(), sampleLotM.getToProcessOperationName(), sampleLotM.getToProcessOperationVersion());
								}
							}
							catch (Exception e)
							{
							}
						}
					}
				}

				sampleProductList = ExtendedObjectProxy.getSampleProductService().getSampleProductDataListByLotName(lotData.getKey().getLotName(), lotData.getFactoryName(),
						lotData.getProductSpecName(), lotData.getProductSpecVersion(), sampleLotM.getProcessFlowName(), sampleLotM.getProcessFlowVersion(), sampleLotM.getProcessOperationName(),
						sampleLotM.getProcessOperationVersion(), sampleLotM.getMachineName(), sampleLotM.getToProcessFlowName(), sampleLotM.getToProcessFlowVersion(),
						sampleLotM.getToProcessOperationName(), sampleLotM.getToProcessOperationVersion());

				if (sampleProductList == null)
				{
					ExtendedObjectProxy.getSampleLotService().deleteSampleLotDataByToInfo(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
							lotData.getProductSpecVersion(), sampleLotM.getToProcessFlowName(), sampleLotM.getToProcessFlowVersion(), sampleLotM.getToProcessOperationName(),
							sampleLotM.getToProcessOperationVersion());
				}
			}
		}
	}

	private String[][] getReturnNode(ChangeSpecInfo changeSpecInfo)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT NODEID, PROCESSFLOWNAME, NODEATTRIBUTE1, FACTORYNAME ");
		sql.append("  FROM NODE ");
		sql.append(" WHERE FACTORYNAME = ? ");
		sql.append("   AND PROCESSFLOWNAME = ? ");
		sql.append("   AND PROCESSFLOWVERSION = ? ");
		sql.append("   AND NODEATTRIBUTE1 = ? ");
		sql.append("   AND NODEATTRIBUTE2 = ? ");
		sql.append("   AND NODETYPE = 'ProcessOperation' ");

		Object[] bind = new Object[] { changeSpecInfo.getFactoryName(), changeSpecInfo.getProcessFlowName(), changeSpecInfo.getProcessFlowVersion(), changeSpecInfo.getProcessOperationName(),
				changeSpecInfo.getProcessOperationVersion() };

		String[][] returnNodeResult = null;
		try
		{
			returnNodeResult = GenericServiceProxy.getSqlMesTemplate().queryForStringArray(sql.toString(), bind);
		}
		catch (Exception e)
		{
		}

		return returnNodeResult;
	}

	public String[][] getOriginalNode(String returnForOriginalNode)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT NODEID, PROCESSFLOWNAME, NODEATTRIBUTE1, FACTORYNAME ");
		sql.append("  FROM NODE ");
		sql.append(" WHERE NODEID = ? ");
		sql.append("   AND NODETYPE = 'ProcessOperation' ");

		Object[] bindSet = new Object[] { returnForOriginalNode };

		String[][] originalNodeResult = null;
		try
		{
			originalNodeResult = GenericServiceProxy.getSqlMesTemplate().queryForStringArray(sql.toString(), bindSet);
		}
		catch (Exception e)
		{
		}

		return originalNodeResult;
	}
	
	private void makeOnHoldForCompleteRuleSample (EventInfo eventInfo, Lot lotData) throws CustomException
	{
		eventInfo.setEventName("Hold");
		eventInfo.setReasonCode("SYSTEM");
		eventInfo.setEventComment("Complete Rule Sample [" + lotData.getKey().getLotName() + "], " + eventInfo.getEventComment());
		
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
