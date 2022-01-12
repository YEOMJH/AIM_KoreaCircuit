package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.jdom.Document;

public class CompleteSortFlow extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String returnFlowName = SMessageUtil.getBodyItemValue(doc, "RETURNFLOWNAME", true);
		String returnOperationName = SMessageUtil.getBodyItemValue(doc, "RETURNOPERATION", true);
		String returnOperationVer = SMessageUtil.getBodyItemValue(doc, "RETURNOPERATIONVER", false);
		if (!StringUtil.isNotEmpty(returnOperationVer))
			returnOperationVer = "00001";

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		CommonValidation.checkJobDownFlag(lotData);
		CommonValidation.checkLotProcessStateWait(lotData);
		CommonValidation.checkSortJob(lotData);

		List<ProductU> productUdfs = MESLotServiceProxy.getLotServiceUtil().setProductUSequence(doc);

		ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CompleteSortFlow", getEventUser(), getEventComment(), "", "");

		// delete ReturnInformation
		Lot afterLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());

		processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(afterLotData);

		if (processFlowData.getProcessFlowType().equals("Main"))
			MESLotServiceProxy.getLotServiceImpl().deleteLotReturnInformation(afterLotData);

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

			Map<String, String> lotUdfs = new HashMap<String, String>();

			lotUdfs.put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());
			lotUdfs.put("BEFOREFLOWNAME", lotData.getProcessFlowName());

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

			if (returnNodeResult.length == 0)
			{
				throw new CustomException("Node-0001", lotData.getProductSpecName(), changeSpecInfo.getProcessFlowName(), changeSpecInfo.getProcessOperationName());
			}
			else
			{
				String sToBeNodeStack = (String) returnNodeResult[0][0];
				ProcessFlow returnFlowData = CommonUtil.getProcessFlowData((String) returnNodeResult[0][3], (String) returnNodeResult[0][1], "00001");

				if (StringUtil.equals(returnFlowData.getProcessFlowType(), "Main") || StringUtil.equals(returnFlowData.getProcessFlowType(), "MQCPrepare"))
				{
					lotUdfs.put("RETURNFLOWNAME", "");
					lotUdfs.put("RETURNOPERATIONNAME", "");
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

						StringBuffer sql2 = new StringBuffer();
						sql2.append("SELECT NODEID, PROCESSFLOWNAME, NODEATTRIBUTE1, FACTORYNAME ");
						sql2.append("  FROM NODE ");
						sql2.append(" WHERE NODEID = ? ");
						sql2.append("   AND NODETYPE = 'ProcessOperation' ");

						bind = new Object[] { returnForOriginalNode };

						String[][] orginalNodeResult = null;
						try
						{
							orginalNodeResult = GenericServiceProxy.getSqlMesTemplate().queryForStringArray(sql2.toString(), bind);
						}
						catch (Exception e)
						{
						}

						if (orginalNodeResult.length > 0)
						{
							lotUdfs.put("RETURNFLOWNAME", (String) orginalNodeResult[0][1]);
							lotUdfs.put("RETURNOPERATIONNAME", (String) orginalNodeResult[0][2]);

							sToBeNodeStack = originalNode; // + "." + sToBeNodeStack;
						}
					}
				}

				changeSpecInfo.setNodeStack(sToBeNodeStack);
			}

			changeSpecInfo.setUdfs(lotUdfs);
		}
		else
		{
			throw new CustomException("LOT-9003", lotName + "(LotProcessState:" + lotData.getLotProcessState() + ")");
		}

		afterLotData = MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
		afterLotData = MESLotServiceProxy.getLotServiceUtil().executePostAction(eventInfo, lotData, afterLotData, false);

		return doc;
	}

	int getNodeStackCount(String str, char c)
	{
		int count = 0;
		for (int i = 0; i < str.length(); i++)
		{
			if (str.charAt(i) == c)
				count++;
		}
		return count;
	}
}
