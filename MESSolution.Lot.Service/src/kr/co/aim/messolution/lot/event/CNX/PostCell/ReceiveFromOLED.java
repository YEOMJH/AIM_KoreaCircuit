package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.MakeReceivedInfo;
import kr.co.aim.greentrack.lot.management.policy.util.LotPFIValueSetter;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processflow.management.iter.NodeStack;
import kr.co.aim.greentrack.processflow.management.iter.NodeStackUtil;
import kr.co.aim.greentrack.processflow.management.iter.PFIValueSetter;
import kr.co.aim.greentrack.processflow.management.iter.ProcessFlowIterator;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductHistory;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.IncrementReleasedQuantityByInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ReceiveFromOLED extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String workOrderName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUEST", true);
		String autoShipFlag = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", false);

		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		
		List<Lot> lotDataList = new ArrayList<Lot>();
		ConstantMap constMap = new ConstantMap();
		
		// 1. Get ProductSpec Data
		ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(factoryName, productSpecName, productSpecVersion);
		// Panel x, y
		int xProductCount = Integer.parseInt(productSpecData.getUdfs().get("PRODUCTCOUNTTOXAXIS"));
		int yProductCount = Integer.parseInt(productSpecData.getUdfs().get("PRODUCTCOUNTTOYAXIS"))/2;

		// set for count
		int xCnt, yCnt;

		if (xProductCount > 14)
			xCnt = 65 + xProductCount + 2;
		else if (xProductCount > 8)
			xCnt = 65 + xProductCount + 1;
		else
			xCnt = 65 + xProductCount;

		if (yProductCount > 14)
			yCnt = 65 + yProductCount + 2;
		else if (yProductCount > 8)
			yCnt = 65 + yProductCount + 1;
		else
			yCnt = 65 + yProductCount;

		for (Element lot : lotList)
		{
			eventInfo.setEventName("Receive");
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lot.getChildText("LOTNAME"));
			Lot oldLotData = (Lot) ObjectUtil.copyTo(lotData);

			if (!lotData.getLotState().equals(GenericServiceProxy.getConstantMap().Lot_Shipped))
				throw new CustomException("LOT-0303", lotData.getKey().getLotName());

			String targetAreaName = GenericServiceProxy.getSpecUtil().getDefaultArea(factoryName);

			// 2. Get Operation, Node
			ProcessFlowKey pfKey = new ProcessFlowKey(factoryName, processFlowName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);

			String startNodeStack = ProcessFlowServiceProxy.getProcessFlowService().getStartNode(pfKey).getKey().getNodeId();

			ProcessFlow processFlow = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(pfKey);

			NodeStack nodeStack = NodeStackUtil.stringToNodeStack(startNodeStack);
			ProcessFlowIterator pfi = new ProcessFlowIterator(processFlow, nodeStack, "");

			lotData.setNodeStack(startNodeStack);
			
			@SuppressWarnings("rawtypes")
			PFIValueSetter valueSetter = new LotPFIValueSetter(pfi, oldLotData, lotData );
			pfi.moveNext("N", valueSetter);

			// 1.3. Set ProcessFlow Iterator Related Data
			Node nextNode = pfi.getCurrentNodeData();

			String targetOperationName = nextNode.getNodeAttribute1();
			String targerOperationVer = nextNode.getNodeAttribute2();
			String targetNodeId = nextNode.getKey().getNodeId();

			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());
			udfs.put("LASTFACTORYNAME", lotData.getFactoryName());
			udfs.put("RECEIVELOTNAME", lotData.getKey().getLotName());
			udfs.put("BANKTYPE", lotData.getUdfs().get("BANKTYPE"));
			udfs.put("OLDPRODUCTREQUESTNAME", lotData.getProductRequestName());

			List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUSequence(lotData.getKey().getLotName());
			MakeReceivedInfo makeReceivedInfo = MESLotServiceProxy.getLotInfoUtil().makeReceivedInfo(lotData, targetAreaName, targetNodeId, processFlowName, processFlowVersion, targetOperationName,
					targerOperationVer, lotData.getProductionType(), workOrderName, "", "", productSpecName, productSpecVersion, lotData.getProductType(), productUdfs, lotData.getSubProductType(),
					autoShipFlag, udfs);

			lotData = MESLotServiceProxy.getLotServiceImpl().receiveLot(eventInfo, lotData, makeReceivedInfo);

			List<Product> productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotData.getKey().getLotName());

			EventInfo changeEventInfo = EventInfoUtil.makeEventInfo("ChangeSubProductGrade", getEventUser(), getEventComment(), null, null);
			changeEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			changeEventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());

			for (Product productInfo : productList)
			{
				Product oldProductInfo = (Product) ObjectUtil.copyTo(productInfo);
				String subProductGrade = productInfo.getSubProductGrades1();
				String subProductGradeFixed = "";

				List<Map<String, Object>> result = getMVIJudgeInfo(productInfo);

				if (!(result.size() > 0))
				{
					for (int i = 0; i < subProductGrade.length(); i++)
					{
						subProductGradeFixed += "G";
					}
				}
				else
				{
					for (int i = 65; i < yCnt; i++)
					{
						if (i == 73 || i == 79)
							continue;

						for (int j = 65; j < xCnt; j++)
						{
							if (j == 73 || j == 79)
								continue;

							String newPanelName = productInfo.getKey().getProductName() + (char) i + (char) j + "0";
							Map<String, Object> arga = new HashMap<String, Object>();
							arga.put("PANELNAME", newPanelName);

							if (result.contains(arga))// new HashMap<String, Object>().put("PANELNAME", (newPanelName))
								subProductGradeFixed += "S";
							else
								subProductGradeFixed += "G";
						}
					}
				}

				String newSubProductGrades1 = productInfo.getSubProductGrades1();
				newSubProductGrades1 = newSubProductGrades1.replace("N", "G");
				//newSubProductGrades1 = newSubProductGrades1.replace("S", "N");

				String finalSubProductGrades = "";

				for (int m = 0; m < subProductGrade.length(); m++)
				{
					if (newSubProductGrades1.substring(m, m + 1).equalsIgnoreCase("S") || subProductGradeFixed.substring(m, m + 1).equalsIgnoreCase("S"))
						finalSubProductGrades += "S";
					else
						finalSubProductGrades += "G";
				}

				if (subProductGrade.length() == finalSubProductGrades.length())
				{
					productInfo.setSubProductGrades1(finalSubProductGrades);
					productInfo.setLastEventComment(changeEventInfo.getEventComment());
					productInfo.setLastEventName(changeEventInfo.getEventName());
					productInfo.setLastEventTime(changeEventInfo.getEventTime());
					productInfo.setLastEventTimeKey(changeEventInfo.getEventTimeKey());
					productInfo.setLastEventUser(changeEventInfo.getEventUser());

					ProductServiceProxy.getProductService().update(productInfo);
					ProductHistory productHistory = new ProductHistory();
					productHistory = ProductServiceProxy.getProductHistoryDataAdaptor().setHV(oldProductInfo, productInfo, productHistory);
					ProductServiceProxy.getProductHistoryService().insert(productHistory);
				}
			}

			int incrementQty = (int) (lotData.getProductQuantity() * xProductCount * yProductCount);
			this.incrementProductRequest(eventInfo, oldLotData, incrementQty, workOrderName);

			if (StringUtils.isNotEmpty(productSpecData.getProductionType()) && !StringUtils.equals(lotData.getProductionType(), productSpecData.getProductionType()))
			{
				lotData.getUdfs().put("CHANGEPRODUCTIONTYPE", productSpecData.getProductionType());
				lotDataList.add(lotData);
			}
			
			//SAP Send
			//Add SAP Switch
			ProductRequest workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());
			ProductRequest oldWorkOrderData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(oldLotData.getProductRequestName());
			SuperProductRequest superWO = new SuperProductRequest();
			
			String sapFlag=CommonUtil.getEnumDefValueStringByEnumName("SAPFLAG");
			if(StringUtil.isNotEmpty(sapFlag)&&StringUtil.equals(sapFlag, "Y")&&
					StringUtils.isNotEmpty(workOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
			{
				superWO = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{workOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
				Lot newLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
				String SAPlotGrade = oldLotData.getLotGrade();
				if(oldLotData.getLotGrade().length() == 1)
					SAPlotGrade += "0";
					
				String factoryPositionCode = "";
				String batchNoForEmptySuperWO="";
				String sql = " SELECT DESCRIPTION FROM ENUMDEFVALUE WHERE ENUMNAME=:ENUMNAME AND ENUMVALUE=:ENUMVALUE ";
					
				Map<String, String> bindMap = new HashMap<String, String>();
				bindMap.put("ENUMNAME", "FactoryPosition");
				bindMap.put("ENUMVALUE", superWO.getFactoryName());
					
				List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
				
				Map<String, String> bindMap2 = new HashMap<String, String>();
				bindMap2.put("ENUMNAME", "BatchNoForEmptySuperWO");
				bindMap2.put("ENUMVALUE", oldWorkOrderData.getFactoryName());
				
				List<Map<String, Object>> sqlResult2 = 
						GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap2);
					
				if(sqlResult.size() > 0){
					factoryPositionCode = sqlResult.get(0).get("DESCRIPTION").toString();
				}
				else
				{
					factoryPositionCode="";
				}
				if(sqlResult2.size()>0) batchNoForEmptySuperWO=sqlResult2.get(0).get("DESCRIPTION").toString();
				
				List<Map<String, String>> ERPReportList = new ArrayList<Map<String, String>>();
				Map<String, String> ERPInfo = new HashMap<>();
				
				ERPInfo.put("SEQ", TimeUtils.getCurrentEventTimeKey());
				ERPInfo.put("PRODUCTREQUESTNAME", superWO.getProductRequestName());
				ERPInfo.put("MATERIALSPECNAME", oldLotData.getProductSpecName().substring(0,  oldLotData.getProductSpecName().length() - 1));
				ERPInfo.put("PROCESSOPERATIONNAME", newLotData.getProcessOperationName());
				ERPInfo.put("QUANTITY", String.valueOf(newLotData.getProductQuantity()));
				if((superWO.getProductRequestType().equals("E")&&!superWO.getSubProductionType().equals("ESLC")))
				{
					ERPInfo.put("FACTORYCODE","5099");
					ERPInfo.put("FACTORYPOSITION", "9F99");
				}
				else
				{
					ERPInfo.put("FACTORYCODE", "5001");
					ERPInfo.put("FACTORYPOSITION",factoryPositionCode);
				}
				ERPInfo.put("CONSUMEUNIT", superWO.getProductType());
				if(!StringUtil.isEmpty( oldWorkOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME") ))
				{
					ERPInfo.put("BATCHNO", oldWorkOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME") + SAPlotGrade);
				}
				else {
					ERPInfo.put("BATCHNO", batchNoForEmptySuperWO);
				}				
				ERPInfo.put("PRODUCTQUANTITY", String.valueOf(newLotData.getProductQuantity()));
				
				Calendar cal = Calendar.getInstance();
				int hour = cal.get(Calendar.HOUR_OF_DAY);
				if(hour >= 19)
				{
					cal.set(Calendar.HOUR_OF_DAY, 0);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MILLISECOND, 0);
					cal.add(Calendar.DAY_OF_MONTH, 1);
					Timestamp receiveTime = new Timestamp(cal.getTime().getTime());
					ERPInfo.put("EVENTTIME", receiveTime.toString().replace("-","").substring(0,8));
				}
				else
				{
					ERPInfo.put("EVENTTIME", eventInfo.getEventTimeKey().substring(0,8));
				}
				ERPInfo.put("CANCELFLAG", "");
				ERPInfo.put("WSFLAG", "X");
				
				ERPReportList.add(ERPInfo);
				
				eventInfo.setEventName("Receive");
				
				//Send
				try
				{
					ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().sapVXG372Send(eventInfo, ERPReportList, 1, superWO.getProductType());
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		if (lotDataList.size() > 0)
		{
			eventInfo.setEventName("ChangeProductionType");
			eventInfo.setEventTimeKey(ConvertUtil.getCurrTimeKey());

			for (Lot lotData : lotDataList)
			{
				ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotData, lotData.getAreaName(), lotData.getDueDate(), lotData.getFactoryName(),
						lotData.getLotHoldState(), lotData.getLotProcessState(), lotData.getLotState(), lotData.getNodeStack(), lotData.getPriority(), lotData.getProcessFlowName(),
						lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), lotData.getUdfs().get("CHANGEPRODUCTIONTYPE"),
						lotData.getProductRequestName(), lotData.getProductSpec2Name(), lotData.getProductSpec2Version(), lotData.getProductSpecName(), lotData.getProductSpecVersion(),
						new ArrayList<ProductU>(), lotData.getSubProductUnitQuantity1(), lotData.getSubProductUnitQuantity2());

				MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
			}
		}

		return doc;
	}

	private void incrementProductRequest(EventInfo eventInfo, Lot lotData, int incrementQty, String productRequestName) throws CustomException
	{
		ProductRequest workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);

		IncrementReleasedQuantityByInfo incrementReleasedQuantityByInfo = new IncrementReleasedQuantityByInfo();
		incrementReleasedQuantityByInfo.setQuantity(incrementQty);

		int createdQty = Integer.parseInt(workOrderData.getUdfs().get("CREATEDQUANTITY")) + incrementQty;

		Map<String, String> productRequestUdfs = workOrderData.getUdfs();
		productRequestUdfs.put("RETURNPRODUCTREQUESTNAME", lotData.getProductRequestName());
		productRequestUdfs.put("CREATEDQUANTITY", Integer.toString(createdQty));
		incrementReleasedQuantityByInfo.setUdfs(productRequestUdfs);

		// Increment Release Qty
		eventInfo.setEventName("IncreamentQuantity");
		workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementReleasedQuantityBy(workOrderData, incrementReleasedQuantityByInfo, eventInfo);

		if (workOrderData.getPlanQuantity() < workOrderData.getReleasedQuantity())
		{
			throw new CustomException("PRODUCTREQUEST-0026", String.valueOf(workOrderData.getPlanQuantity()), String.valueOf(workOrderData.getReleasedQuantity()));
		}
	}
	

	private List<Map<String, Object>> getMVIJudgeInfo(Product productInfo)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT PANELNAME ");
		sql.append("  FROM CT_REVIEWPANELJUDGE ");
		sql.append(" WHERE PROCESSOPERATIONNAME = '21220' ");
		sql.append("   AND PRODUCTNAME = :PRODUCTNAME ");
		sql.append("   AND PANELJUDGE IN ('S') ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("PRODUCTNAME", productInfo.getKey().getProductName());

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		return result;
	}
}
