package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.print.DocFlavor.STRING;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.BankQueueTime;
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductHistory;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.IncrementReleasedQuantityByInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

import com.sun.java.accessibility.util.EventQueueMonitor;

public class CancelReceiveLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
		String reasonCodeName = SMessageUtil.getBodyItemValue(doc, "REASONCODE", true);
		String reasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelReceive", getEventUser(), getEventComment(), reasonCodeType, reasonCodeName);

		ConstantMap constMap = new ConstantMap();
		
		for (Element lot : lotList)
		{
			String lotName = SMessageUtil.getChildText(lot, "LOTNAME", true);
			String nowProductRequestName = SMessageUtil.getChildText(lot, "PRODUCTREQUESTNAME", true);
			String sourceFactoryName = SMessageUtil.getChildText(lot, "FACTORYNAME", true);
			String bankType = SMessageUtil.getChildText(lot, "BANKTYPE", true);
			
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			Lot baseLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			String receiveLotName = lotData.getUdfs().get("RECEIVELOTNAME");

			if (StringUtils.isEmpty(receiveLotName))
				receiveLotName = lotName;

			// Validation Lot
			CommonValidation.checkLotState(lotData);
			CommonValidation.checkLotProcessState(lotData);
			CommonValidation.checkLotHoldState(lotData);
			
			// Get Data
			Map<String, String> histUdfs = this.findLotHist(receiveLotName, lotData.getFactoryName());

			String oldProcessFlowName = lotData.getProcessFlowName();
			String oldProcessOperationName = lotData.getProcessOperationName();
			String oldAreaName = lotData.getAreaName();
			String oldDestFactoryName = lotData.getDestinationFactoryName();
			String oldProductSpecName = lotData.getProductSpecName();
			String oleProductRequestName = lotData.getProductRequestName();
			String oldProductionType = lotData.getProductionType();

			String factoryName = histUdfs.get("LASTFACTORYNAME");
			String areaName = histUdfs.get("AREANAME");
			String productSpecName = histUdfs.get("PRODUCTSPECNAME");
			String productRequestName = histUdfs.get("OLDPRODUCTREQUESTNAME");
			String processFlowName = histUdfs.get("PROCESSFLOWNAME");
			String processFlowVersion = histUdfs.get("PROCESSFLOWVERSION");
			String processOperationName = histUdfs.get("PROCESSOPERATIONNAME");
			String processOperationVer = histUdfs.get("PROCESSOPERATIONVERSION");
			String destinationFactoryName = histUdfs.get("DESTINATIONFACTORYNAME");
			String oldFactoryName = lotData.getFactoryName();
			String nodeStack = CommonUtil.getNodeStack(factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVer);
			ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(factoryName, productSpecName, "00001");
			
			lotData.setFactoryName(factoryName);
			lotData.setAreaName(areaName);
			lotData.setProductSpecName(productSpecName);
			lotData.setProductionType(productSpecData.getProductionType());
			lotData.setProductRequestName(productRequestName);
			lotData.setProcessFlowName(processFlowName);
			lotData.setProcessOperationName(processOperationName);
			lotData.setDestinationFactoryName(destinationFactoryName);
			lotData.setNodeStack(nodeStack);
			lotData.setLotState("Shipped");
			lotData.setLotProcessState("");
			lotData.setLotHoldState("");
		

			LotServiceProxy.getLotService().update(lotData);

			// Set Event
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("BEFOREOPERATIONNAME", baseLotData.getProcessOperationName());
			setEventInfo.getUdfs().put("BEFOREFLOWNAME", baseLotData.getProcessFlowName());
			setEventInfo.getUdfs().put("LASTFACTORYNAME", baseLotData.getFactoryName());
			setEventInfo.getUdfs().put("RECEIVELOTNAME", "");
			setEventInfo.getUdfs().put("BANKTYPE", bankType);
			setEventInfo.getUdfs().put("OLDPRODUCTREQUESTNAME", baseLotData.getProductRequestName());

			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);

			String condition = "where lotname=? and timekey=(select max(timekey) from lothistory where lotname=? and eventname = ? ) ";
			Object[] bindSet = new Object[] { lotName, lotName, "CancelReceive" };
			List<LotHistory> arrayList = LotServiceProxy.getLotHistoryService().select(condition, bindSet);
			LotHistory lotHistory = arrayList.get(0);
			lotHistory.setOldFactoryName(oldFactoryName);
			lotHistory.setOldProcessFlowName(oldProcessFlowName);
			lotHistory.setOldProcessOperationName(oldProcessOperationName);
			lotHistory.setOldAreaName(oldAreaName);
			lotHistory.setOldDestinationFactoryName(oldDestFactoryName);
			lotHistory.setOldProductSpecName(oldProductSpecName);
			lotHistory.setOldProductionType(oldProductionType);				

			Map<String, String> lotHistUdfs = lotHistory.getUdfs();
			lotHistUdfs.put("OLDPRODUCTREQUESTNAME", oleProductRequestName);

			LotServiceProxy.getLotHistoryService().update(lotHistory);

			// Set
			List<Product> prdList = MESProductServiceProxy.getProductServiceUtil().allUnScrappedProductsByLot(lotName);

			// Check TP cancel received Lot is only one WO.
			if (sourceFactoryName.equals("TP") && checkDiffWO(prdList))
				throw new CustomException("PRODUCTREQUEST-1000");

			for (Product prdData : prdList)
			{
				// Cancel Receive for only InProduction Product
				if (StringUtils.equals(prdData.getProductState(), GenericServiceProxy.getConstantMap().Prod_InProduction))
				{
					prdData.setProductSpecName(productSpecName);
					prdData.setProductRequestName(productRequestName);
					prdData.setProcessFlowName(processFlowName);
					prdData.setProcessOperationName(processOperationName);
					prdData.setNodeStack(nodeStack);
					prdData.setDestinationFactoryName(destinationFactoryName);
					prdData.setProductState("Shipped");
					prdData.setProductProcessState("");
					prdData.setProductHoldState("");
					prdData.setFactoryName(factoryName);
					prdData.setAreaName(areaName);

					ProductServiceProxy.getProductService().update(prdData);

					kr.co.aim.greentrack.product.management.info.SetEventInfo prdSetEvInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
					ProductServiceProxy.getProductService().setEvent(prdData.getKey(), eventInfo, prdSetEvInfo);

					String conditions = "where productname=? and timekey=(select max(timekey) from productHistory where productname=? and eventname = ? ) ";
					Object[] bindSets = new Object[] { prdData.getKey().getProductName(), prdData.getKey().getProductName(), "CancelReceive" };
					List<ProductHistory> arrayLists = ProductServiceProxy.getProductHistoryService().select(conditions, bindSets);
					ProductHistory productHistory = arrayLists.get(0);
					productHistory.setOldFactoryName(oldFactoryName);
					productHistory.setOldProcessFlowName(oldProcessFlowName);
					productHistory.setOldProcessOperationName(oldProcessOperationName);
					productHistory.setOldAreaName(oldAreaName);
					productHistory.setOldDestinationFactoryName(oldDestFactoryName);

					ProductServiceProxy.getProductHistoryService().update(productHistory);
				}
			}

			int decrementQty = -(int) lotData.getProductQuantity();

			if (lotData.getFactoryName().equals("ARRAY") && lotData.getDestinationFactoryName().equals("OLED"))
			{
				decrementQty *= 2;
			}
			else if (lotData.getFactoryName().equals("OLED") && lotData.getDestinationFactoryName().equals("POSTCELL"))
			{
				ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(nowProductRequestName);
				productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(productRequestData.getFactoryName(), productRequestData.getProductSpecName(), productRequestData.getProductSpecVersion());
				
				// Panel x, y
				int xProductCount = Integer.parseInt(productSpecData.getUdfs().get("PRODUCTCOUNTTOXAXIS"));
				int yProductCount = Integer.parseInt(productSpecData.getUdfs().get("PRODUCTCOUNTTOYAXIS"));

				decrementQty = -(int) (lotData.getProductQuantity() * xProductCount * yProductCount/2);
			}

			ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(nowProductRequestName);
			
			if((productRequestData.getReleasedQuantity()<(-decrementQty))
					||(Integer.valueOf(productRequestData.getUdfs().get("CREATEDQUANTITY"))<(-decrementQty)))
			{
				throw new CustomException("WO-00003");
			}
			
			IncrementReleasedQuantityByInfo incrementReleasedQuantityByInfo = new IncrementReleasedQuantityByInfo();
			incrementReleasedQuantityByInfo.setQuantity(decrementQty);
			
			int createdQty = Integer.parseInt(productRequestData.getUdfs().get("CREATEDQUANTITY")) + decrementQty;
			incrementReleasedQuantityByInfo.getUdfs().put("CREATEDQUANTITY", Integer.toString(createdQty));

			productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementReleasedQuantityBy(productRequestData, incrementReleasedQuantityByInfo, eventInfo);

			Lot cancelLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

			if (sourceFactoryName.equals("TP"))
			{
				long oldPriority = Long.valueOf(getSourceLotPriority(receiveLotName).get(0).get("PRIORITY").toString());
				
				if (!String.valueOf(cancelLotData.getPriority()).equals(String.valueOf(oldPriority)))
				{
					EventInfo changePriorityEventInfo = EventInfoUtil.makeEventInfo("ChangeLotPriority", this.getEventUser(), this.getEventComment(), "", "");
					cancelLotData.setPriority(Long.valueOf(oldPriority));
					changePriorityEventInfo.setEventTimeKey(ConvertUtil.getCurrTimeKey());
					changePriorityEventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
					LotServiceProxy.getLotService().update(cancelLotData);
					SetEventInfo setchangePriorityEventInfo = new SetEventInfo();
					LotServiceProxy.getLotService().setEvent(cancelLotData.getKey(), changePriorityEventInfo, setchangePriorityEventInfo);
					kr.co.aim.greentrack.product.management.info.SetEventInfo setProductEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
					List<Product> productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotName);

					for (Product product : productList)
					{
						product.setPriority(Long.valueOf(oldPriority));
						ProductServiceProxy.getProductService().update(product);
						ProductServiceProxy.getProductService().setEvent(product.getKey(), changePriorityEventInfo, setProductEventInfo);
					}
				}
			}
			
			//Modify ct_bankQueueTime ExitTime=""
			modifyBankQueueTime(lotData,eventInfo);
			
			//SAP Send
			ProductRequest oldWorkOrderData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(cancelLotData.getProductRequestName());
			ProductRequest workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(baseLotData.getProductRequestName());
			if(StringUtil.in(oldWorkOrderData.getProductRequestType(), "P","E","T")
					&&StringUtil.in(workOrderData.getProductRequestType(), "M","D"))
			{
				throw new CustomException("WO-00004");
			}
			
			SuperProductRequest superWO = new SuperProductRequest();
			
			String sapFlag=CommonUtil.getEnumDefValueStringByEnumName("SAPFLAG");
			if(StringUtil.isNotEmpty(sapFlag)&&StringUtil.equals(sapFlag, "Y")&&
					StringUtils.isNotEmpty(workOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
			{
				superWO = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{workOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
				
				String SAPlotGrade = baseLotData.getLotGrade();
				if(baseLotData.getLotGrade().length() == 1)
					SAPlotGrade += "0";
				 
				String factoryPositionCode = "";
				String batchNoForEmptySuperWO="";
				String sql = " SELECT DESCRIPTION FROM ENUMDEFVALUE WHERE ENUMNAME=:ENUMNAME AND ENUMVALUE=:ENUMVALUE ";
				
				Map<String, String> bindMap = new HashMap<String, String>();
				bindMap.put("ENUMNAME", "FactoryPosition");
				bindMap.put("ENUMVALUE", superWO.getFactoryName());
				
				List<Map<String, Object>> sqlResult = 
						GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
				
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
				
				List<Map<String, String>> ERPReportList = new ArrayList<Map<String, String>>();
				Map<String, String> ERPInfo = new HashMap<>();
				
				ERPInfo.put("SEQ", TimeUtils.getCurrentEventTimeKey());
				ERPInfo.put("PRODUCTREQUESTNAME", workOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME"));
				ERPInfo.put("MATERIALSPECNAME", productSpecName.substring(0,productSpecName.length() - 1));
				ERPInfo.put("PROCESSOPERATIONNAME", baseLotData.getProcessOperationName());
				ERPInfo.put("QUANTITY", "-"+String.valueOf(baseLotData.getProductQuantity()));
				ERPInfo.put("CONSUMEUNIT", superWO.getProductType());
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
				if(!StringUtil.isEmpty( oldWorkOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME") ))
				{
					ERPInfo.put("BATCHNO", oldWorkOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME") + SAPlotGrade);
				}
				else {
					ERPInfo.put("BATCHNO", batchNoForEmptySuperWO);
				}				
				ERPInfo.put("PRODUCTQUANTITY", String.valueOf(baseLotData.getProductQuantity()));
				
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
					ERPInfo.put("EVENTTIME",receiveTime.toString().replace("-","").substring(0,8));
				}
				else
				{
					ERPInfo.put("EVENTTIME", eventInfo.getEventTimeKey().substring(0,8));
				}
				ERPInfo.put("CANCELFLAG", "X");
				ERPInfo.put("WSFLAG", "X");
				
				ERPReportList.add(ERPInfo);
				
				eventInfo.setEventName("CancelReceive");
				
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

		return doc;
	}

	public boolean checkDiffWOLOT(List<String> lotList)
	{
		boolean diffWO = false;

		if (lotList == null || lotList.size() == 0)
		{
			eventLog.info("checkDiffWO: Lot list is null or Empty!! ");
			return diffWO;
		}

		String sql = " SELECT DISTINCT PRODUCTREQUESTNAME  FROM PRODUCT  WHERE 1=1  " + CommonUtil.makeInString("LOTNAME", lotList.toArray(new String[lotList.size()]), false);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new Object[] {});

		if (resultList == null || resultList.size() == 0)
		{
			eventLog.info(String.format("checkDiffWO: ProductRquestName is Empty !! [SQL:%s]", sql.toString()));
			return diffWO;
		}

		if (resultList.size() > 1)
		{
			diffWO = true;
			eventLog.info("checkDiffWO: ProductRquestName quantity is greater than one.");
		}

		return diffWO;
	}

	@SuppressWarnings("unchecked")
	public boolean checkDiffWO(List<Product> productList)
	{
		boolean diffWO = false;

		if (productList == null || productList.size() == 0)
		{
			eventLog.info("checkDiffWO: Product list is null or Empty!! ");
			return diffWO;
		}

		List<String> productNameList = new ArrayList<String>();

		for (Product product : productList)
		{
			productNameList.add(product.getKey().getProductName());
		}

		List<Map<String, Object>> resultList = null;

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT DISTINCT PRODUCTREQUESTNAME ");
		sql.append("  FROM PRODUCT ");
		sql.append(" WHERE PRODUCTNAME IN (:PRODUCTLIST) ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("PRODUCTLIST", productNameList);

		resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		if (resultList == null || resultList.size() == 0)
		{
			eventLog.info(String.format("checkDiffWO: ProductRquestName is Empty !! [SQL:%s]", sql.toString()));
			return diffWO;
		}

		if (resultList.size() > 1)
		{
			diffWO = true;
			eventLog.info("checkDiffWO: ProductRquestName quantity is greater than one.");
		}

		return diffWO;
	}

	private Map<String, String> findLotHist(String lotName, String factoryName) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT OLDFACTORYNAME, ");
		sql.append("       OLDPRODUCTSPECNAME, ");
		sql.append("       OLDPRODUCTREQUESTNAME, ");
		sql.append("       OLDPROCESSFLOWNAME, ");
		sql.append("       OLDPROCESSFLOWVERSION, ");
		sql.append("       OLDPROCESSOPERATIONNAME, ");
		sql.append("       OLDPROCESSOPERATIONVERSION, ");
		sql.append("       OLDAREANAME, ");
		sql.append("       NODESTACK, ");
		sql.append("       OLDDESTINATIONFACTORYNAME, ");
		sql.append("       OLDPRODUCTREQUESTNAME, ");
		sql.append("       LASTFACTORYNAME, ");
		sql.append("       BEFOREOPERATIONNAME, ");
		sql.append("       LASTFACTORYNAME, ");
		sql.append("       PRIORITY ");
		sql.append("  FROM LOTHISTORY ");
		sql.append(" WHERE LOTNAME = :LOTNAME ");
		sql.append("   AND TIMEKEY = (SELECT MAX (TIMEKEY) ");
		sql.append("                    FROM LOTHISTORY ");
		sql.append("                   WHERE LOTNAME = :LOTNAME ");
		sql.append("                     AND EVENTNAME = :EVENTNAME ");
		sql.append("                     AND FACTORYNAME = :FACTORYNAME) ");

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("LOTNAME", lotName);
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("EVENTNAME", "Receive");

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		Map<String, String> udfs = new HashMap<String, String>();

		try
		{
			if (sqlResult.size() > 0)
			{
				udfs.put("PRODUCTSPECNAME", sqlResult.get(0).get("OLDPRODUCTSPECNAME").toString());
				udfs.put("OLDPRODUCTREQUESTNAME", sqlResult.get(0).get("OLDPRODUCTREQUESTNAME").toString());
				udfs.put("PROCESSFLOWNAME", sqlResult.get(0).get("OLDPROCESSFLOWNAME").toString());
				udfs.put("PROCESSFLOWVERSION", sqlResult.get(0).get("OLDPROCESSFLOWVERSION").toString());
				udfs.put("PROCESSOPERATIONNAME", sqlResult.get(0).get("OLDPROCESSOPERATIONNAME").toString());
				udfs.put("PROCESSOPERATIONVERSION", sqlResult.get(0).get("OLDPROCESSOPERATIONVERSION").toString());
				udfs.put("AREANAME", sqlResult.get(0).get("OLDAREANAME").toString());
				udfs.put("DESTINATIONFACTORYNAME", sqlResult.get(0).get("OLDDESTINATIONFACTORYNAME").toString());
				udfs.put("LASTFACTORYNAME", sqlResult.get(0).get("LASTFACTORYNAME").toString());
				udfs.put("BEFOREOPERATIONNAME", sqlResult.get(0).get("BEFOREOPERATIONNAME").toString());
				udfs.put("OLDFACTORYNAME", sqlResult.get(0).get("OLDFACTORYNAME").toString());
				udfs.put("OLDPRODUCTREQUESTNAME", sqlResult.get(0).get("OLDPRODUCTREQUESTNAME").toString());
			}
			else
			{
				throw new CustomException();
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			throw new CustomException("LOT-0115", lotName);
		}

		return udfs;
	}

	private List<Map<String, Object>> getSourceLotPriority(String lotName) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT PRIORITY ");
		sql.append("  FROM LOTHISTORY ");
		sql.append(" WHERE LOTNAME = :LOTNAME ");
		sql.append("   AND EVENTNAME = 'Receive' ");
		sql.append("   AND FACTORYNAME = 'TP' ");
		sql.append("   AND ROWNUM = '1' ");
		sql.append("ORDER BY TIMEKEY DESC ");
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("LOTNAME", lotName);
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sourcePriority = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
		
		if (sourcePriority.size() > 0)
			return sourcePriority;
		else
			throw new CustomException();
	}
	
	private void modifyBankQueueTime(Lot lotData,EventInfo eventInfo) throws CustomException
	{
		try 
		{
			StringBuffer sql=new StringBuffer();
			sql.append(" SELECT LOTNAME  ");
			sql.append(" FROM CT_BANKQUEUETIME ");
			sql.append(" WHERE LOTNAME=:LOTNAME ");
			
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("LOTNAME", lotData.getKey().getLotName());
			List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
			
			StringBuffer bankPolicy=new StringBuffer();
			bankPolicy.append(" SELECT A.FACTORYNAME,A.PRODUCTSPECNAME,A.PRODUCTSPECVERSION,B.TOFACTORYNAME,B.BANKTYPE,B.WARNINGDURATIONLIMIT,B.INTERLOCKDURATIONLIMIT  ");
			bankPolicy.append(" FROM  TPPOLICY A,POSBANKQUEUETIME B ");
			bankPolicy.append(" WHERE A.CONDITIONID=B.CONDITIONID ");
			bankPolicy.append(" AND A.FACTORYNAME=:FACTORYNAME ");
			bankPolicy.append(" AND A.PRODUCTSPECNAME=:PRODUCTSPECNAME ");
			bankPolicy.append(" AND A.PRODUCTSPECVERSION=:PRODUCTSPECVERSION ");
			bankPolicy.append(" AND B.TOFACTORYNAME=:TOFACTORYNAME ");
			bankPolicy.append(" AND B.BANKTYPE=:BANKTYPE ");
			
			
			Map<String, String> bindMap2 = new HashMap<String, String>();
			bindMap2.put("FACTORYNAME", lotData.getFactoryName());
			bindMap2.put("PRODUCTSPECNAME", lotData.getProductSpecName());
			bindMap2.put("PRODUCTSPECVERSION", lotData.getProductSpecVersion());
			bindMap2.put("TOFACTORYNAME", lotData.getDestinationFactoryName());
			bindMap2.put("BANKTYPE", lotData.getUdfs().get("BANKTYPE"));
			List<Map<String, Object>> bankPolicyResult = GenericServiceProxy.getSqlMesTemplate().queryForList(bankPolicy.toString(), bindMap2);
			
			if(result!=null && result.size()>0)
			
			{
				BankQueueTime bankQueueTime = ExtendedObjectProxy.getBankQueueTimeService().selectByKey(false,
						new Object[] { lotData.getKey().getLotName() });

				bankQueueTime.setExitTime(null);
				bankQueueTime.setBankType(lotData.getUdfs().get("BANKTYPE"));
				bankQueueTime.setEnterTime(eventInfo.getEventTime());
				bankQueueTime.setQueueTimeState("Entered");
				bankQueueTime.setLastEventName(eventInfo.getEventName());
				bankQueueTime.setLastEventUser(eventInfo.getEventUser());
				bankQueueTime.setLastEventTimekey(eventInfo.getEventTimeKey());
				
				if(bankPolicyResult!=null && bankPolicyResult.size()>0)
				{
					bankQueueTime.setWarningDurationLimit(bankPolicyResult.get(0).get("WARNINGDURATIONLIMIT").toString());
					bankQueueTime.setInterlockDurationLimit(bankPolicyResult.get(0).get("INTERLOCKDURATIONLIMIT").toString());
				}
				ExtendedObjectProxy.getBankQueueTimeService().modify(eventInfo, bankQueueTime);
			}
			else
			{
				if(bankPolicyResult!=null && bankPolicyResult.size()>0)
                {
					BankQueueTime bankInfo = new BankQueueTime();
					bankInfo.setLotName(lotData.getKey().getLotName());
					bankInfo.setBankType(lotData.getUdfs().get("BANKTYPE"));
					bankInfo.setFactoryName(lotData.getFactoryName());
					bankInfo.setProductSpecName(lotData.getProductSpecName());
					bankInfo.setProductSpecVersion(lotData.getProductSpecVersion());
					bankInfo.setProcessFlowName(lotData.getProcessFlowName());
					bankInfo.setProcessFlowVersion(lotData.getProcessFlowVersion());
					bankInfo.setProcessOperationName(lotData.getProcessOperationName());
					bankInfo.setProcessOperationVersion(lotData.getProcessOperationVersion());
					bankInfo.setToFactoryName(lotData.getDestinationFactoryName());
					
					bankInfo.setQueueTimeState("Entered");
					bankInfo.setEnterTime(eventInfo.getEventTime());
					bankInfo.setExitTime(null);
					bankInfo.setWarningTime(null);
					bankInfo.setInterlockTime(null);
					bankInfo.setResolveTime(null);
					bankInfo.setResolveUser("");
					bankInfo.setWarningDurationLimit(bankPolicyResult.get(0).get("WARNINGDURATIONLIMIT").toString());
					bankInfo.setInterlockDurationLimit(bankPolicyResult.get(0).get("INTERLOCKDURATIONLIMIT").toString());
					
					bankInfo.setAlarmType("BankQTimeOver");
					
					bankInfo.setLastEventName(eventInfo.getEventName());
					bankInfo.setLastEventUser(eventInfo.getEventUser());
					bankInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
					
					ExtendedObjectProxy.getBankQueueTimeService().create(eventInfo, bankInfo);
				}
			}
		}
		catch (Exception e ) 
		{
			throw new CustomException("BANK-0002");
		}
	}
}