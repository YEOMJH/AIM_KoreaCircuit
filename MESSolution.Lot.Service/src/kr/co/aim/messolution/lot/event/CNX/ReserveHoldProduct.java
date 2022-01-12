package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LotFutureAction;
import kr.co.aim.messolution.extended.object.management.data.ProductFutureAction;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ReserveHoldProduct extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String requestDepartment = SMessageUtil.getBodyItemValue(doc, "REQUESTDEPARTMENT", false);
		String owner = SMessageUtil.getBodyItemValue(doc, "OWNER", false);

		List<Element> operationList = SMessageUtil.getBodySequenceItemList(doc, "OPERATIONLIST", true);
		List<Element> ReasonCodeList = SMessageUtil.getBodySequenceItemList(doc, "REASONCODELIST", true);

		String currentProductSpec = SMessageUtil.getBodyItemValue(doc, "CURRENTPRODUCTSPEC", true);
		String currentProcessFlow = SMessageUtil.getBodyItemValue(doc, "CURRENTPROCESSFLOW", true);
		String currentProcessOperation = SMessageUtil.getBodyItemValue(doc, "CURRENTPROCESSOPERATION", true);

		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		
		EventInfo eventInfo = new EventInfo();
		boolean actionFlag = false;

		// Validation
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		CommonValidation.checkDummyProductReserve(lotData);
		
		if (!StringUtils.equals(currentProductSpec, lotData.getProductSpecName()))
			throw new CustomException("LOT-0137", lotData.getProductSpecName());

		if (!StringUtils.equals(currentProcessFlow, lotData.getProcessFlowName()))
			throw new CustomException("LOT-0138", lotData.getProcessFlowName());

		if (!StringUtils.equals(currentProcessOperation, lotData.getProcessOperationName()))
			throw new CustomException("LOT-0139", lotData.getProcessOperationName());

		// Check ProcessFlow
		if (!checkProcessFlow(lotData, processFlowName, processFlowVersion))
			throw new CustomException("LOT-0081");

		for (Element productE : productList)
		{
			String productName = productE.getChildText("PRODUCTNAME");
			//String position = productE.getChildText("POSITION");
			Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);
			
			for (Element operation : operationList)
			{
				String processOperationName = operation.getChildText("PROCESSOPERATIONNAME");
				String processOperationVersion = operation.getChildText("PROCESSOPERATIONVERSION");

				for (Element code : ReasonCodeList)
				{
					String reasonCode = code.getChildText("REASONCODE");
					String reasonCodeType = code.getChildText("REASONCODETYPE")+ "[" + requestDepartment + "," + owner + "]";
					String unitName = "-";
					
					// Check ReserveHoldList
					if (!checkReserveHoldProductList(productName, factoryName, processFlowName, processFlowVersion,
							processOperationName, processOperationVersion, reasonCode, unitName)) 
					{
						eventInfo = EventInfoUtil.makeEventInfo("ReserveHoldProduct", getEventUser(),
								getFutureActionEventComment(processOperationName), null, null);
						eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
						
						List<LotFutureAction> reserveLotData = ExtendedObjectProxy.getLotFutureActionService()
								.getLotFutureActionDataList(lotName, factoryName, processFlowName, processFlowVersion,
										processOperationName, processOperationVersion, 0, reasonCode);

						if (reserveLotData != null) 
						{
							throw new CustomException("LOT-9069");
						} 
						else 
						{
							ExtendedObjectProxy.getProductFutureActionService().insertProductFutureAction(eventInfo, productName, productData.getFactoryName(),
									processFlowName, processFlowVersion, processOperationName,
									processOperationVersion, unitName, reasonCode, reasonCodeType, "hold", "System", productData.getLotName());
						}

						actionFlag = true;
					}
				}
			}
		}
		
		

		if (!actionFlag)
			throw new CustomException("LOT-0083");

		return doc;
	}

	private boolean checkProcessFlow(Lot lotData, String processFlowName, String processFlowVersion)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT DISTINCT PF.PROCESSFLOWNAME, PF.DESCRIPTION, PF.PROCESSFLOWVERSION, PF.PROCESSFLOWTYPE ");
		sql.append("  FROM TPFOPOLICY TPFO, PROCESSFLOW PF ");
		sql.append(" WHERE TPFO.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND TPFO.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("   AND TPFO.PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");
		sql.append("   AND PF.FACTORYNAME = TPFO.FACTORYNAME ");
		sql.append("   AND PF.PROCESSFLOWNAME = TPFO.PROCESSFLOWNAME ");
		sql.append("   AND PF.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION ");
		sql.append("   AND PF.PROCESSFLOWTYPE IN ('Inspection', 'Sample', 'Main', 'MQC', 'MQCPrepare','MQCRecycle','Rework') ");
		sql.append("   AND PF.ACTIVESTATE = 'Active' ");
		sql.append("ORDER BY NVL (LENGTH (TRIM (PF.PROCESSFLOWNAME)), 0) DESC, PF.PROCESSFLOWNAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("FACTORYNAME", lotData.getFactoryName());
		args.put("PRODUCTSPECNAME", lotData.getProductSpecName());
		args.put("PRODUCTSPECVERSION", lotData.getProductSpecVersion());

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		boolean checkFlag = false;

		if (result.size() > 0)
		{
			for (Map<String, Object> flowList : result)
			{
				String queryFlowName = ConvertUtil.getMapValueByName(flowList, "PROCESSFLOWNAME");
				String queryFlowVersion = ConvertUtil.getMapValueByName(flowList, "PROCESSFLOWVERSION");

				if (StringUtils.equals(processFlowName, queryFlowName) && StringUtils.equals(processFlowVersion, queryFlowVersion))
				{
					checkFlag = true;
					break;
				}
			}
		}

		return checkFlag;
	}

	private boolean checkReserveHoldProductList(String productName, String factoryName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion,
			String reasonCode, String unitName) throws CustomException
	{
		boolean checkFlag = true;
		ProductFutureAction productFutureAction = null;

		try
		{
			productFutureAction = ExtendedObjectProxy.getProductFutureActionService().selectByKey(false, new Object []{productName,factoryName,processFlowName,processFlowVersion, processOperationName,processOperationVersion,unitName,reasonCode});
		}
		catch(greenFrameDBErrorSignal e)
		{
			eventLog.info(e.getCause());
			productFutureAction = null;
		}
		
		if (productFutureAction != null)
			checkFlag = true;
		else
			checkFlag = false;

		return checkFlag;
	}
}
