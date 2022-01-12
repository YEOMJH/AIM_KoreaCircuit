package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.BankQueueTime;
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.NodeStack;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.lot.event.LotProcessEnd;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.MakeReceivedInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductHistory;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.IncrementReleasedQuantityByInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class ReceiveLot extends SyncHandler {

	private static Log log = LogFactory.getLog(LotProcessEnd.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String lotGrade = SMessageUtil.getBodyItemValue(doc, "LOTGRADE", false);
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		List<Lot> lotDataList = new ArrayList<Lot>();

		for (Element lot : lotList)
		{
			String lotName = SMessageUtil.getChildText(lot, "LOTNAME", true);
			String productSpecName = SMessageUtil.getChildText(lot, "PRODUCTSPECNAME", true);
			String productSpecVer = SMessageUtil.getChildText(lot, "PRODUCTSPECVERSION", true);
			String processFlowName = SMessageUtil.getChildText(lot, "PROCESSFLOWNAME", true);
			String processFlowVer = SMessageUtil.getChildText(lot, "PROCESSFLOWVERSION", true);
			String workOrderName = SMessageUtil.getChildText(lot, "PRODUCTREQUESTNAME", true);
			String priority = SMessageUtil.getChildText(lot, "PRIORITY", true);
			String autoShipFlag = SMessageUtil.getChildText(lot, "AUTOSHIPPINGFLAG", false);
			String prOwner = SMessageUtil.getChildText(lot, "PROWNER", false);
			String experimenter = SMessageUtil.getChildText(lot, "EXPERIMENTER", false);

			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			Lot oldLotData = (Lot) ObjectUtil.copyTo(lotData);
			
			if(prOwner != null)
			{
				String updatePROwner = " UPDATE PRODUCTREQUEST "
						+ " SET PROWNER = '" + prOwner
						+ "' WHERE 1 = 1 "					
						+ "  AND PRODUCTREQUESTNAME = :PRODUCTREQUESTNAME "
						+ "  AND FACTORYNAME = :FACTORYNAME ";
				
				Map<String, Object> bdMap = new HashMap<String, Object>();
				bdMap.put("PRODUCTREQUESTNAME", workOrderName);
				bdMap.put("FACTORYNAME", factoryName);
				
				greenFrameServiceProxy.getSqlTemplate().update(updatePROwner, bdMap);
			}
			
			String oldProductSpecName=oldLotData.getProductSpecName();
			ProductRequest oldProductRequestData=MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());
			ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(factoryName, productSpecName, productSpecVer);

			// validation
			// must be shipped
			if (!lotData.getLotState().equals(GenericServiceProxy.getConstantMap().Lot_Shipped))
				throw new CustomException("LOT-0303", lotData.getKey().getLotName());
			
			if(!lotGrade.equals("NG")){
				if (lotData.getUdfs().get("BANKTYPE").equals("NG"))
					throw new CustomException("LOT-0304");
			}

			// mandatory info
			String targetAreaName = GenericServiceProxy.getSpecUtil().getDefaultArea(factoryName);

			// Get Operation, Node
			String targetOperationName = CommonUtil.getFirstOperation(factoryName, processFlowName).getKey().getProcessOperationName();
			String targerOperationVer = CommonUtil.getFirstOperation(factoryName, processFlowName).getKey().getProcessOperationVersion();
			String targetNodeId = NodeStack.getNodeID(factoryName, processFlowName, targetOperationName, targerOperationVer);

			Map<String, String> lotUdfs = new HashMap<String, String>();
			lotUdfs.put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());
			lotUdfs.put("LASTFACTORYNAME", lotData.getFactoryName());
			lotUdfs.put("RECEIVELOTNAME", lotData.getKey().getLotName());
			lotUdfs.put("BANKTYPE", lotData.getUdfs().get("BANKTYPE"));
			lotUdfs.put("OLDPRODUCTREQUESTNAME", lotData.getProductRequestName());
			lotUdfs.put("EXPERIMENTER", experimenter);

			List<ProductU> productUdfs = setProductUSequenceClearDetailGrade(lotName);

			if (factoryName.equals("TP") && (!priority.equals(String.valueOf(lotData.getPriority()))))
				lotData.setPriority(Long.parseLong(priority));
			
			LotServiceProxy.getLotService().update(lotData);
			
			MakeReceivedInfo makeReceivedInfo = MESLotServiceProxy.getLotInfoUtil().makeReceivedInfo(lotData, targetAreaName, targetNodeId, processFlowName, processFlowVer, targetOperationName,
					targerOperationVer, lotData.getProductionType(), workOrderName, "", "", productSpecName, productSpecVer, lotData.getProductType(), productUdfs, lotData.getSubProductType(), autoShipFlag, lotUdfs);

			int incrementQty = (int) lotData.getProductQuantity();

			if (oldLotData.getFactoryName().equals("ARRAY") && oldLotData.getDestinationFactoryName().equals("OLED"))
				incrementQty *= 2;

			eventInfo.setEventName("Receive");
			lotData = MESLotServiceProxy.getLotServiceImpl().receiveLot(eventInfo, lotData, makeReceivedInfo);

			// Mantis : 0000454
			// 2021-02-03	dhko	FactoryName、OLDFactoryName、DestinationFactoryName字段修复
			modifyDestinationFactoryName(lotData, factoryName);
			
			// initialize "SubProductGrades2" and "DetailGrade" when receive by next factory.
			List<Product> productList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotData.getKey().getLotName());

			EventInfo changeEventInfo = EventInfoUtil.makeEventInfo("ChangeSubProductGrades2", getEventUser(), getEventComment(), null, null);
			changeEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			changeEventInfo.setLastEventTimekey(TimeUtils.getCurrentEventTimeKey());

			for (Product productInfo : productList)
			{
				Product oldProductInfo = (Product) ObjectUtil.copyTo(productInfo);
				productInfo.setSubProductGrades2("");

				productInfo.setLastEventComment(changeEventInfo.getEventComment());
				productInfo.setLastEventName(changeEventInfo.getEventName());
				productInfo.setLastEventTime(changeEventInfo.getEventTime());
				productInfo.setLastEventTimeKey(changeEventInfo.getEventTimeKey());
				productInfo.setLastEventUser(changeEventInfo.getEventUser());
				
				Map<String, String> udfs = new HashMap<String, String>();
				udfs.put("TURNCOUNT", "0");
				productInfo.setUdfs(udfs);

				ProductServiceProxy.getProductService().update(productInfo);
				ProductHistory productHistory = new ProductHistory();
				productHistory = ProductServiceProxy.getProductHistoryDataAdaptor().setHV(oldProductInfo, productInfo, productHistory);
				ProductServiceProxy.getProductHistoryService().insert(productHistory);
			}

			this.incrementProductRequest(eventInfo, oldLotData, incrementQty, workOrderName);

			if (StringUtils.isNotEmpty(productSpecData.getProductionType()) && !StringUtils.equals(lotData.getProductionType(), productSpecData.getProductionType()))
			{
				lotData.getUdfs().put("CHANGEPRODUCTIONTYPE", productSpecData.getProductionType());
				lotDataList.add(lotData);
			}
			
			//Modify CT_BANKQUEUETIME
			modifyBankQueueTime(lotData,eventInfo);
			
			//TP FirstOperation(Virtual) Skip
			if(StringUtils.equals(factoryName, "TP"))
			{
				Lot postLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
				
				MachineSpec machineSpecData = this.findVirtualMachine(postLotData);
				Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineSpecData.getKey().getMachineName());
				
				//Setting element.
				List<Element> productListElement = this.makeElementProdList(productList);
				
				//Set Sampling Data ( Main is Virtual )
				this.setSamplingListData(eventInfo, postLotData, machineData, productListElement); // Set Sampling Data
				
				//Skip operation (Virtual = First TP operation) 
				if(StringUtils.equals(machineSpecData.getMachineType(), "VirtualMachine"))
				{
					Map<String, String> udfs = new HashMap<String, String>();
					EventInfo eventInfo_ChangeSpec = EventInfoUtil.makeEventInfo_TrackInOutWithoutHistory("", getEventUser(), getEventComment(), null, null);
					
					ChangeSpecInfo skipInfo = skipInfo(eventInfo_ChangeSpec, postLotData, udfs, new ArrayList<ProductU>());	
					lotData = changeSpec(eventInfo_ChangeSpec, postLotData, skipInfo);
				}
			}

			//SAP Send
			//Add SAP Switch
			ProductRequest workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());
			if(StringUtil.in(oldProductRequestData.getProductRequestType(), "P","E","T")
					&&StringUtil.in(workOrderData.getProductRequestType(), "M","D"))
			{
				throw new CustomException("WO-00006");
			}
			SuperProductRequest superWO = new SuperProductRequest();
			
			String sapFlag=CommonUtil.getEnumDefValueStringByEnumName("SAPFLAG");
			if(StringUtils.isNotEmpty(sapFlag)&&StringUtils.equals(sapFlag, "Y")&&
					StringUtils.isNotEmpty(workOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
			{
				superWO = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{workOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
				
				Lot newLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());
				String SAPlotGrade = oldLotData.getLotGrade();
				if(newLotData.getLotGrade().length() == 1)
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
				bindMap2.put("ENUMVALUE", oldProductRequestData.getFactoryName());
				
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
				ERPInfo.put("MATERIALSPECNAME", oldProductSpecName.substring(0, oldProductSpecName.length() - 1));
				ERPInfo.put("PROCESSOPERATIONNAME", newLotData.getProcessOperationName());
				ERPInfo.put("QUANTITY", String.valueOf(newLotData.getProductQuantity()));
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
				//ERPInfo.put("FACTORYPOSITION", factoryPositionCode);
				if(!StringUtils.isEmpty( oldProductRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME") ))
				{
					ERPInfo.put("BATCHNO", oldProductRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME") + SAPlotGrade);
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

				eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			}
		}
		
		// Mantis : 0000355
		// If Factory information and LotName information are different, change LotName.
		// OLED <-> TP
		List<String> lotNameList = new ArrayList<String>();
		
		for (Element lot : lotList)
		{
			String lotName = SMessageUtil.getChildText(lot, "LOTNAME", true);
			lotNameList.add(lotName);
		}
		
		MESLotServiceProxy.getLotServiceImpl().changeLotName(eventInfo, "ReceiveLot", lotNameList);
		
		return doc;

	}

	/**
	 * Mantis : 0000454
	 * 
	 * 2021-02-03	dhko	FactoryName、OLDFactoryName、DestinationFactoryName字段修复
	 */
	private void modifyDestinationFactoryName(Lot lotData, String destinationFactoryName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		// Modify Lot
		String sql = "UPDATE LOT SET DESTINATIONFACTORYNAME = ? WHERE LOTNAME = ? ";
		greenFrameServiceProxy.getSqlTemplate().update(sql, new Object[] { destinationFactoryName, lotData.getKey().getLotName() });
		
		// Modify LotHistory
		sql = "UPDATE LOTHISTORY SET DESTINATIONFACTORYNAME = ? WHERE TIMEKEY = ? AND LOTNAME = ? ";
		greenFrameServiceProxy.getSqlTemplate().update(sql, new Object[] { destinationFactoryName, lotData.getLastEventTimeKey(), lotData.getKey().getLotName() });
		
		List<Product> productDataList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotData.getKey().getLotName());
		for (Product product : productDataList) 
		{
			// Product
			sql = "UPDATE PRODUCT SET DESTINATIONFACTORYNAME = ? WHERE PRODUCTNAME = ? ";
			greenFrameServiceProxy.getSqlTemplate().update(sql, new Object[] { destinationFactoryName, product.getKey().getProductName() });
			
			// ProductHistory
			sql = "UPDATE PRODUCTHISTORY SET DESTINATIONFACTORYNAME = ? WHERE TIMEKEY = ? AND PRODUCTNAME = ? ";
			greenFrameServiceProxy.getSqlTemplate().update(sql, new Object[] { destinationFactoryName, product.getLastEventTimeKey(), product.getKey().getProductName() });
		}
	}
	
	private List<ProductU> setProductUSequenceClearDetailGrade(String lotName)
	{
		List<ProductU> productUList = new ArrayList<ProductU>();

		try
		{
			List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotName);

			for (Product product : productList)
			{
				Map<String, String> productsUdfs = new HashMap<String, String>();
				productsUdfs = product.getUdfs();
				productsUdfs.put("DETAILGRADE", "");
				productsUdfs.put("SUBPRODUCTGRADES2", "");
				productsUdfs.put("BEFOREPROCESSOPERATION", product.getProcessOperationName());
				
				ProductU productU = new ProductU();
				productU.setProductName(product.getKey().getProductName());
				productU.setUdfs(productsUdfs);
				productUList.add(productU);
			}
		}
		catch (NotFoundSignal ne)
		{

		}
		return productUList;
	}

	private void incrementProductRequest(EventInfo eventInfo, Lot lotData, int incrementQty, String productRequestName) throws CustomException
	{
		ProductRequest workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);

		IncrementReleasedQuantityByInfo incrementReleasedQuantityByInfo = new IncrementReleasedQuantityByInfo();
		incrementReleasedQuantityByInfo.setQuantity(incrementQty);

		int createdQty = Integer.parseInt(workOrderData.getUdfs().get("CREATEDQUANTITY")) + incrementQty;

		Map<String, String> productRequestUdfs = workOrderData.getUdfs();
		//Not Exist in ProductRequestTable
		//productRequestUdfs.put("RETURNPRODUCTREQUESTNAME", lotData.getProductRequestName());
		productRequestUdfs.put("CREATEDQUANTITY", Integer.toString(createdQty));
		incrementReleasedQuantityByInfo.setUdfs(productRequestUdfs);

		// Increment Release Qty
		eventInfo.setEventName("IncreamentQuantity");
		workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementReleasedQuantityBy(workOrderData, incrementReleasedQuantityByInfo, eventInfo);

		if (workOrderData.getPlanQuantity() < workOrderData.getReleasedQuantity())
			throw new CustomException("PRODUCTREQUEST-0026", String.valueOf(workOrderData.getPlanQuantity()), String.valueOf(workOrderData.getReleasedQuantity()));
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
			
			if(result!=null && result.size()>0)
			
			{
				BankQueueTime bankQueueTime = ExtendedObjectProxy.getBankQueueTimeService().selectByKey(false,
						new Object[] { lotData.getKey().getLotName() });

				bankQueueTime.setExitTime(eventInfo.getEventTime());
				bankQueueTime.setQueueTimeState("Exited");
				
				bankQueueTime.setLastEventName("Receive");
				bankQueueTime.setLastEventUser(eventInfo.getEventUser());
				bankQueueTime.setLastEventTimekey(eventInfo.getEventTimeKey());
				ExtendedObjectProxy.getBankQueueTimeService().modify(eventInfo, bankQueueTime);
			}
		}
		catch (Exception e ) 
		{
			throw new CustomException("BANK-0002");
		}
	}
	
	private MachineSpec findVirtualMachine(Lot lotData) throws CustomException
	{
		String virtualMachineName = "";
		MachineSpec machineSpecData = null;
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT POSM.MACHINENAME FROM ");
		sql.append("  TPFOPOLICY TPFO, POSMACHINE POSM ");
		sql.append(" WHERE 1=1 ");
		sql.append("   AND TPFO.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND TPFO.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("   AND TPFO.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND TPFO.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND TPFO.CONDITIONID = POSM.CONDITIONID ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("FACTORYNAME", lotData.getFactoryName());
		args.put("PRODUCTSPECNAME", lotData.getProductSpecName());
		args.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
		args.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			for (Map<String, Object> machineList : result)
			{
				virtualMachineName = ConvertUtil.getMapValueByName(machineList, "MACHINENAME");

				if (StringUtils.isNotEmpty(virtualMachineName))
				{
					machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(virtualMachineName);
				}
			}
		}

		return machineSpecData;
	}
	
	private List<Element> makeElementProdList(List<Product> productList) throws CustomException
	{

		List<Element> productListElement = new ArrayList<Element>();
		
		for (Product productData : productList)
		{
			Element productElement = new Element("PRODUCT");


			Element productNameElement = new Element("PRODUCTNAME");
			productNameElement.setText(productData.getKey().getProductName());
			productElement.addContent(productNameElement);

			Element positionElement = new Element("POSITION");
			positionElement.setText(String.valueOf(productData.getPosition()));
			productElement.addContent(positionElement);

			productListElement.add(productElement);
		}
		
		return productListElement;
	}
	
	private void setSamplingListData(EventInfo eventInfo, Lot lotData, Machine machineData, List<Element> productListElement) throws CustomException
	{
		eventInfo = EventInfoUtil.makeEventInfo("Sampling", eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		// 0. get all sampling Flow List (joined TFOMPOLICY & POSSAMPLE)
		List<Map<String, Object>> samplePolicyList = MESLotServiceProxy.getLotServiceUtil().getSamplePolicyList(lotData.getFactoryName(), lotData.getProcessFlowName(),
				lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), machineData.getKey().getMachineName(), "", "", "", "");

		for (Map<String, Object> samplePolicyM : samplePolicyList)
		{
			List<Element> newProductListElement = CommonUtil.makeElementProdListForDummy(productListElement);
			MESLotServiceProxy.getLotServiceUtil().setSamplingData(eventInfo, lotData, samplePolicyM, newProductListElement);

		}
	}
	
	private ChangeSpecInfo skipInfo(EventInfo eventInfo, Lot lotData, Map<String, String> udfs, List<ProductU> productUdfs) throws CustomException
	{
		String nodeStack = "";
		ChangeSpecInfo skipInfo = new ChangeSpecInfo();

		skipInfo.setProductionType(lotData.getProductionType());
		skipInfo.setProductSpecName(lotData.getProductSpecName());
		skipInfo.setProductSpecVersion(lotData.getProductSpecVersion());
		skipInfo.setProductSpec2Name(lotData.getProductSpec2Name());
		skipInfo.setProductSpec2Version(lotData.getProductSpec2Version());

		skipInfo.setProductRequestName(lotData.getProductRequestName());

		skipInfo.setSubProductUnitQuantity1(lotData.getSubProductUnitQuantity1());
		skipInfo.setSubProductUnitQuantity2(lotData.getSubProductUnitQuantity2());

		skipInfo.setDueDate(lotData.getDueDate());
		skipInfo.setPriority(lotData.getPriority());

		skipInfo.setFactoryName(lotData.getFactoryName());
		skipInfo.setAreaName(lotData.getAreaName());

		Node sampleNode = null;
		// NEXT OPERATION INFO
		// to next operation
		{
		    sampleNode = MESLotServiceProxy.getLotServiceUtil().getReserveSampleLotData(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(),
							lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), lotData.getProcessOperationName(), lotData.getUdfs().get("RETURNOPERATIONNAME"));

		    if (sampleNode != null)
			{
				// Move to Sampling Flow
				NodeStack lotNodeStack = MESLotServiceProxy.getLotServiceUtil().getLotNodeStackForProcessEnd(sampleNode, lotData);
				
				// Set NextInfo
				nodeStack = NodeStack.nodeStackToString(lotNodeStack); //Set SampleNodeID for makeLoggedOut (Ex. NodeID.NodeID)
				
				skipInfo.setProcessFlowName(sampleNode.getProcessFlowName());
				skipInfo.setProcessFlowVersion(sampleNode.getProcessFlowVersion());
				skipInfo.setProcessOperationName(sampleNode.getNodeAttribute1());
				skipInfo.setProcessOperationVersion(sampleNode.getNodeAttribute2());
				
				// Set ReturnInfo
				udfs.put("RETURNFLOWNAME", sampleNode.getUdfs().get("RETURNPROCESSFLOWNAME").toString());
				udfs.put("RETURNOPERATIONNAME", sampleNode.getUdfs().get("RETURNOPERATIONNAME"));
				udfs.put("RETURNOPERATIONVER", sampleNode.getUdfs().get("RETURNOPERATIONVERSION"));
					
			}
		    else
		    {
		    	Node nextNode = PolicyUtil.getNextOperation(lotData);
		    	nodeStack = nextNode.getKey().getNodeId();
		    	
				skipInfo.setProcessFlowName(nextNode.getProcessFlowName());
				skipInfo.setProcessFlowVersion(nextNode.getProcessFlowVersion());
				skipInfo.setProcessOperationName(nextNode.getNodeAttribute1());
				skipInfo.setProcessOperationVersion(nextNode.getNodeAttribute2());
				
		    	udfs.put("RETURNFLOWNAME", "");
		    	udfs.put("RETURNOPERATIONNAME", "");
		    	udfs.put("RETURNOPERATIONVER", "");
		    }
		}
		
		skipInfo.setNodeStack(nodeStack);

		// trace setting
		udfs.put("BEFOREFLOWNAME", lotData.getProcessFlowName());
		udfs.put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());
		udfs.put("BEFOREOPERATIONVER", lotData.getProcessOperationVersion());

		skipInfo.setUdfs(udfs);
		skipInfo.setProductUSequence(productUdfs);

		if (sampleNode != null)
		{
			//Delete Sampling
			//deleteSamplingDataReturn(eventInfo, lotData, sampleNode, false);
		}
		return skipInfo;
	}
	
	
	public Lot changeSpec (EventInfo eventInfo, Lot lotData, ChangeSpecInfo changeSpecInfo) throws CustomException
	{
		try
		{
			Lot changeOperLotData = LotServiceProxy.getLotService().changeSpec(lotData.getKey(), eventInfo, changeSpecInfo);
			log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());

			return changeOperLotData;
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("LOT-9003", lotData.getKey().getLotName());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("LOT-9999", fe.getMessage());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("LOT-9002", lotData.getKey().getLotName());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("LOT-9001", lotData.getKey().getLotName());
		}
	}
	
}
