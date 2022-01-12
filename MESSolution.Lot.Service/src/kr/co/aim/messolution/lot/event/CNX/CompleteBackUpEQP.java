package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.Iterator;
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
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Arc;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.jdom.Document;
import org.jdom.Element;

public class CompleteBackUpEQP extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName 			   = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String returnFlowName      = SMessageUtil.getBodyItemValue(doc, "RETURNFLOWNAME", true);
		String returnOperationName = SMessageUtil.getBodyItemValue(doc, "RETURNOPERATION", true);
		String returnOperationVer  = SMessageUtil.getBodyItemValue(doc, "RETURNOPERATIONVER", false);
		String beforeOperationName = "";

		// 2020-11-14	dhko	Add Validation
		CommonValidation.checkProcessInfobyString(lotName);
		
		if (!StringUtil.isNotEmpty(returnOperationVer))
			returnOperationVer = "00001";

		Element element = doc.getDocument().getRootElement();

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		CommonValidation.checkLotProcessStateWait(lotData);

		Map<String, String> udfs = new HashMap<String, String>();

		List<ProductU> productUdfs = MESLotServiceProxy.getLotServiceUtil().setProductUSequence(doc);

		beforeOperationName = MESLotServiceProxy.getLotServiceUtil().getBeforeOperationName(returnFlowName, returnOperationName);
		udfs.put("BEFOREOPERATIONNAME", beforeOperationName);

		ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CompleteBackUpEQP", getEventUser(), getEventComment(), "", "");

		// delete ReturnInformation
		Lot afterLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());

		processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(afterLotData);

		try
		{
			this.recoverProcessingInfo(eventInfo, afterLotData);
		}
		catch (Exception ex)
		{
			eventLog.warn("ProcessingFlag recovery is failed");
		}

		if (processFlowData.getProcessFlowType().equals("Main"))
		{
			MESLotServiceProxy.getLotServiceImpl().deleteLotReturnInformation(afterLotData);
		}

		//Mantis - 0000060
		Node nextNodeData = ProcessFlowServiceProxy.getNodeService().getNode(lotData.getNodeStack().substring(0, lotData.getNodeStack().indexOf(".")));

		List<Arc> arcData = ProcessFlowServiceProxy.getArcService().select(" PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND TONODEID = ? ",
				new Object[] { nextNodeData.getProcessFlowName(), nextNodeData.getProcessFlowVersion(), nextNodeData.getKey().getNodeId() });
		
		Node currentNodeData = ProcessFlowServiceProxy.getNodeService().getNode(arcData.get(0).getKey().getFromNodeId());

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
			changeSpecInfo.setProcessFlowName(currentNodeData.getProcessFlowName());
			changeSpecInfo.setProcessFlowVersion(currentNodeData.getProcessFlowVersion());
			changeSpecInfo.setProcessOperationName(currentNodeData.getNodeAttribute1());
			changeSpecInfo.setProcessOperationVersion(currentNodeData.getNodeAttribute2());
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
			lotUdfs.put("BACKUPMAINOPERNAME", "");
			lotUdfs.put("BACKUPMAINFLOWNAME", ""); 

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

				if ((StringUtil.equals(returnFlowData.getProcessFlowType(), "Main") || StringUtil.equals(returnFlowData.getProcessFlowType(), "MQC")))
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

						String[][] orginalNodeResult = getOriginalNode(returnForOriginalNode);

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

		MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);

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

	private void recoverProcessingInfo(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		List<Product> productList = ProductServiceProxy.getProductService().select("lotName = ? AND productState = ? AND processingInfo = ?",
				new Object[] { lotData.getKey().getLotName(), GenericServiceProxy.getConstantMap().Prod_InProduction, "B" });

		for (Iterator<Product> iteratorProduct = productList.iterator(); iteratorProduct.hasNext();)
		{
			Product product = iteratorProduct.next();

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("PROCESSINGINFO", "S"); // StringUtil.EMPTY);
			bindMap.put("PRODUCTNAME", product.getKey().getProductName());

			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE PRODUCT ");
			sql.append("   SET PROCESSINGINFO = :PROCESSINGINFO ");
			sql.append(" WHERE PRODUCTNAME = :PRODUCTNAME ");

			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);

			StringBuffer updateSql = new StringBuffer();
			updateSql.append("UPDATE PRODUCTHISTORY ");
			updateSql.append("   SET PROCESSINGINFO = :PROCESSINGINFO ");
			updateSql.append(" WHERE PRODUCTNAME = :PRODUCTNAME ");
			updateSql.append("   AND TIMEKEY = (SELECT LASTEVENTTIMEKEY ");
			updateSql.append("                    FROM PRODUCT ");
			updateSql.append("                   WHERE PRODUCTNAME = :PRODUCTNAME) ");

			GenericServiceProxy.getSqlMesTemplate().update(updateSql.toString(), bindMap);

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

		Object[] bind = new Object[] { returnForOriginalNode };

		String[][] orginalNodeResult = null;
		try
		{
			orginalNodeResult = GenericServiceProxy.getSqlMesTemplate().queryForStringArray(sql.toString(), bind);
		}
		catch (Exception e)
		{
		}

		return orginalNodeResult;
	}
}
