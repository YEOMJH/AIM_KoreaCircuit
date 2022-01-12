package kr.co.aim.messolution.lot.event.CNX;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ProductQueueTime;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.NodeStack;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.IncrementReleasedQuantityByInfo;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class ChangeProductSpec extends SyncHandler {

	private static Log log = LogFactory.getLog(ChangeProductSpec.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String productionType = SMessageUtil.getBodyItemValue(doc, "PRODUCTIONTYPE", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productSpecVer = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String processOperationVer = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", true);
		String sfactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		String oldProductRequestName = SMessageUtil.getBodyItemValue(doc, "OLDPRODUCTREQUESTNAME", true);

		// PC-Tech-0008-01
		// Add the function to change the Priority of Lot.
		String priority = SMessageUtil.getBodyItemValue(doc, "PRIORITY", false);

		List<Product> productDataList = LotServiceProxy.getLotService().allProducts(lotName);

		// check ProcessInfo
		List<String> productNameList = new ArrayList<>();
		for (Product productA : productDataList)
		{
			String productName = productA.getKey().getProductName();
			productNameList.add(productName);
		}
		CommonValidation.checkProductProcessInfobyString(productNameList);

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		Lot oldLotData = (Lot) ObjectUtil.copyTo(lotData);
		
		CommonValidation.checkJobDownFlag(lotData);
		CommonValidation.checkDummyProductReserve(lotData);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeProductSpec", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		// Mantis - 0000042
		List<ProductQueueTime> qTimeDataList = ExtendedObjectProxy.getProductQTimeService().findProductQTimeByLot(lotData.getKey().getLotName());
		lotData = ExtendedObjectProxy.getProductQTimeService().monitorProductQTimeByChangeProductSpec(eventInfo, qTimeDataList, lotData);

//		CommonValidation.checkProductQueueTime(lotData);
		CommonValidation.checkLotHoldState(lotData);

		// if flag is true,update subproductqty
		ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(sfactoryName, productSpecName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
		long s1 = Integer.parseInt(productSpecData.getUdfs().get("PRODUCTCOUNTTOXAXIS")) * Integer.parseInt(productSpecData.getUdfs().get("PRODUCTCOUNTTOYAXIS"));

		lotData = updateSubProductQuantity(productSpecData, lotData, s1);

		int productQuantity = (int) lotData.getProductQuantity();

		ProductRequest ReqproductRequest = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);
		if (ReqproductRequest.getReleasedQuantity() < (ReqproductRequest.getFinishedQuantity() + ReqproductRequest.getScrappedQuantity()))
		{
			throw new CustomException("PRODUCTREQUEST-0028", String.valueOf(ReqproductRequest.getReleasedQuantity()), String.valueOf(ReqproductRequest.getFinishedQuantity()
					+ ReqproductRequest.getScrappedQuantity()));
		}

		if (!StringUtils.equals(lotData.getFactoryName(), "POSTCELL"))
		{
			if (StringUtils.equals(lotData.getFactoryName(), "OLED"))
			{
				ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());

				if (StringUtils.equals(operationData.getDetailProcessOperationType(), "CUT"))
					productQuantity *= 2;
			}

			changeReleasedQuantity(eventInfo, lotData, productRequestName, oldProductRequestName, productQuantity);
		}
		else
		{
			if (lotData.getProductType().equals("Glass"))
			{
				// new project add release qty
				ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);

				IncrementReleasedQuantityByInfo incrementReleasedQuantityByInfo = new IncrementReleasedQuantityByInfo();
				incrementReleasedQuantityByInfo.setQuantity((int) (lotData.getProductQuantity() * s1 / 2));

				eventInfo.setEventName("IncreamentQuantity");
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementReleasedQuantityBy(productRequestData, incrementReleasedQuantityByInfo, eventInfo);
				productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);

				if (productRequestData.getPlanQuantity() < productRequestData.getReleasedQuantity())
					throw new CustomException("PRODUCTREQUEST-0029", String.valueOf(productRequestData.getPlanQuantity() - productRequestData.getReleasedQuantity()),
							String.valueOf(incrementReleasedQuantityByInfo.getQuantity()));

				productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());

				// old project substr release qty
				eventInfo.setEventName("DecreamentQuantity");

				IncrementReleasedQuantityByInfo DecrementReleasedQuantityByInfo = new IncrementReleasedQuantityByInfo();
				DecrementReleasedQuantityByInfo.setQuantity(-(int) (lotData.getProductQuantity() * s1 / 2));

				MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementReleasedQuantityBy(productRequestData, DecrementReleasedQuantityByInfo, eventInfo);
				productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());

				if (productRequestData.getReleasedQuantity() < 0)
					throw new CustomException("PRODUCTREQUEST-0026", String.valueOf(productRequestData.getPlanQuantity()), String.valueOf(productRequestData.getReleasedQuantity()));
			}
			else
			{
				// new project add release qty
				ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);

				IncrementReleasedQuantityByInfo incrementReleasedQuantityByInfo = new IncrementReleasedQuantityByInfo();
				incrementReleasedQuantityByInfo.setQuantity((int) lotData.getProductQuantity());

				eventInfo.setEventName("IncreamentQuantity");
				productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementReleasedQuantityBy(productRequestData, incrementReleasedQuantityByInfo, eventInfo);

				if (productRequestData.getPlanQuantity() < productRequestData.getReleasedQuantity())
					throw new CustomException("PRODUCTREQUEST-0029", String.valueOf(productRequestData.getPlanQuantity() - productRequestData.getReleasedQuantity()),
							String.valueOf(incrementReleasedQuantityByInfo.getQuantity()));

				productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());

				// old project substr release qty
				eventInfo.setEventName("DecreamentQuantity");

				IncrementReleasedQuantityByInfo DecrementReleasedQuantityByInfo = new IncrementReleasedQuantityByInfo();
				DecrementReleasedQuantityByInfo.setQuantity(-(int) lotData.getProductQuantity());

				productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementReleasedQuantityBy(productRequestData, DecrementReleasedQuantityByInfo, eventInfo);

				if (productRequestData.getReleasedQuantity() < 0)
					throw new CustomException("PRODUCTREQUEST-0029", String.valueOf(productRequestData.getPlanQuantity()), String.valueOf(productRequestData.getReleasedQuantity()));
			}
		}

		String NodeId = NodeStack.getNodeID(lotData.getFactoryName(), processFlowName, processOperationName, processOperationVer);

		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();
		changeSpecInfo.setAreaName(lotData.getAreaName());
		changeSpecInfo.setDueDate(lotData.getDueDate());
		changeSpecInfo.setFactoryName(lotData.getFactoryName());
		changeSpecInfo.setLotHoldState(lotData.getLotHoldState());
		changeSpecInfo.setLotProcessState(lotData.getLotProcessState());
		changeSpecInfo.setLotState(lotData.getLotState());
		changeSpecInfo.setNodeStack(NodeId);

		// PC-Tech-0008-01
		// Add the function to change the Priority of Lot.
		changeSpecInfo.setPriority(StringUtil.isEmpty(priority) ? lotData.getPriority() : Long.valueOf(priority));

		changeSpecInfo.setProcessFlowName(processFlowName);
		changeSpecInfo.setProcessFlowVersion(processFlowVersion);
		changeSpecInfo.setProcessOperationName(processOperationName);
		changeSpecInfo.setProcessOperationVersion(processOperationVer);
		changeSpecInfo.setProductionType(productionType);
		changeSpecInfo.setProductRequestName(productRequestName);
		changeSpecInfo.setProductSpec2Name(lotData.getProductSpec2Name());
		changeSpecInfo.setProductSpec2Version(lotData.getProductSpec2Version());
		changeSpecInfo.setProductSpecName(productSpecName);
		changeSpecInfo.setProductSpecVersion(productSpecVer);
		changeSpecInfo.setSubProductUnitQuantity1(lotData.getSubProductUnitQuantity1());
		changeSpecInfo.setSubProductUnitQuantity2(lotData.getSubProductUnitQuantity2());

		// Mantis - 0000042
		Map<String, String> udfs = new HashMap<String, String>();
		udfs.put("OLDPRODUCTREQUESTNAME", oldProductRequestName);

		changeSpecInfo.setUdfs(udfs);

		eventInfo.setEventName("ChangeProductSpec");
		lotData = LotServiceProxy.getLotService().changeSpec(lotData.getKey(), eventInfo, changeSpecInfo);

		if(StringUtil.isNotEmpty(lotData.getCarrierName()))
		{
			Durable carrierInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());
			SetEventInfo setEventInfo = new SetEventInfo();
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(carrierInfo, setEventInfo, eventInfo);
		}
		
		// Mantis - 0000042
		ExtendedObjectProxy.getProductQTimeService().updateQTimeDataByChangeProductSpec(eventInfo, qTimeDataList, lotData, oldLotData);
		
		String sapFlag=CommonUtil.getEnumDefValueStringByEnumName("SAPFLAG");
		if(StringUtil.isNotEmpty(sapFlag)&&StringUtil.equals(sapFlag, "Y")&& StringUtil.isNotEmpty(ReqproductRequest.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
		{
			ProductRequest oldProductRequest = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(oldProductRequestName);
			
			if(!StringUtil.equals(ReqproductRequest.getUdfs().get("SUPERPRODUCTREQUESTNAME"), oldProductRequest.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
			{
				try
				{
					insertIntoSAPDB(ReqproductRequest, oldProductRequest, lotData, eventInfo);
				}
				catch (Exception e)
				{
					eventLog.info("SAP Report Error");
				}
			}
		}
		
		
//		List<ListOrderedMap> QTimePolicyList = PolicyUtil.getQTimeSpec(lotData.getFactoryName(), processFlowName, processFlowVersion, processOperationName, processOperationVer);
//		List<ProductQueueTime> QTimeDataList = ExtendedObjectProxy.getProductQTimeService().findQTimeByLot(lotData.getKey().getLotName());

//		for (ListOrderedMap QtimePolicy : QTimePolicyList)
//		{
//			String toProcessFlowName = CommonUtil.getValue(QtimePolicy, "TOPROCESSFLOWNAME");
//			String toProcessOperationName = CommonUtil.getValue(QtimePolicy, "TOPROCESSOPERATIONNAME");
//
//			for (ProductQueueTime oldQTimeData : QTimeDataList)
//			{
//				log.info("Start To Change ProductQtime's FlowName");
//				ProductQueueTime newQTimeData = (ProductQueueTime) ObjectUtil.copyTo(oldQTimeData);
//				newQTimeData.setProcessFlowName(lotData.getProcessFlowName());
//				newQTimeData.setProcessOperationName(processOperationName);
//				newQTimeData.setToProcessFlowName(toProcessFlowName);
//				newQTimeData.setToProcessOperationName(toProcessOperationName);
//				ExtendedObjectProxy.getProductQTimeService().modifyToNew(eventInfo, oldQTimeData, newQTimeData);
//				log.info("End To Change ProductQtime's FlowName");
//			}
//		}

		return doc;
	}

	public double[] calculateLotAllQuantity(String lotName) throws FrameworkErrorSignal, NotFoundSignal
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT COUNT (*), SUM (SUBPRODUCTQUANTITY), SUM (SUBPRODUCTQUANTITY1), SUM (SUBPRODUCTQUANTITY2) ");
		sql.append("  FROM PRODUCT ");
		sql.append(" WHERE LOTNAME = :LOTNAME ");
		sql.append("   AND PRODUCTSTATE NOT IN ('Scrapped', 'Shipped', 'Consumed') ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("LOTNAME", lotName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() == 0)
		{
			String keyString = "lotName:" + lotName + ",Scrapped:" + GenericServiceProxy.getConstantMap().Prod_Scrapped + ",Shipped:" + GenericServiceProxy.getConstantMap().Prod_Shipped
					+ ",Consumed:" + GenericServiceProxy.getConstantMap().Prod_Consumed;

			throw new NotFoundSignal(keyString, sql.toString());
		}

		ListOrderedMap orderMap = (ListOrderedMap) result.get(0);

		double[] quantityResult = new double[4];
		for (int i = 0; i < 4; i++)
		{
			quantityResult[i] = orderMap.getValue(i) == null ? 0 : ((BigDecimal) orderMap.getValue(i)).doubleValue();
		}

		return quantityResult;
	}

	private void changeReleasedQuantity(EventInfo eventInfo, Lot lotData, String productRequestName, String oldProductRequestName, int productQuantity) throws greenFrameDBErrorSignal, CustomException
	{
		// New Info
		ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);

		IncrementReleasedQuantityByInfo incrementReleasedQuantityByInfo = new IncrementReleasedQuantityByInfo();
		incrementReleasedQuantityByInfo.setQuantity(productQuantity);

		Map<String, String> udfs = new HashMap<String, String>();

		int iCreateQty = Integer.parseInt(productRequestData.getUdfs().get("CREATEDQUANTITY"));
		udfs.put("CREATEDQUANTITY", Integer.toString(iCreateQty + productQuantity));

		incrementReleasedQuantityByInfo.setUdfs(udfs);

		eventInfo.setEventName("IncreamentQuantity");
		productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementReleasedQuantityBy(productRequestData, incrementReleasedQuantityByInfo, eventInfo);

		if (productRequestData.getPlanQuantity() < Long.parseLong(productRequestData.getUdfs().get("CREATEDQUANTITY")))
		{
			throw new CustomException("PRODUCTREQUEST-0030");
		}

		if (Integer.parseInt(productRequestData.getUdfs().get("CREATEDQUANTITY")) < productRequestData.getReleasedQuantity())
		{
			throw new CustomException("PRODUCTREQUEST-0031");
		}

		// Mantis - 0000042
		// if (productRequestData.getPlanQuantity() == productRequestData.getReleasedQuantity() + productRequestData.getScrappedQuantity() && !existCreateStartedLot(productRequestData))
		// {
		// eventInfo.setEventName("Completed");
		// MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompleted(eventInfo, lotData.getProductRequestName(), productRequestData.getUdfs());
		// }

		// Old Info
		ProductRequest oldProductRequest = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(oldProductRequestName);

		IncrementReleasedQuantityByInfo oldIncrementReleasedQuantityByInfo = new IncrementReleasedQuantityByInfo();
		oldIncrementReleasedQuantityByInfo.setQuantity(-productQuantity);

		Map<String, String> oldUdfs = new HashMap<String, String>();

		int ioldCreateQty = Integer.parseInt(oldProductRequest.getUdfs().get("CREATEDQUANTITY"));
		oldUdfs.put("CREATEDQUANTITY", Integer.toString(ioldCreateQty - productQuantity));

		oldIncrementReleasedQuantityByInfo.setUdfs(oldUdfs);

		eventInfo.setEventName("DecreamentQuantity");
		oldProductRequest = MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementReleasedQuantityBy(oldProductRequest, oldIncrementReleasedQuantityByInfo, eventInfo);

		if (oldProductRequest.getReleasedQuantity() < 0)
			throw new CustomException("PRODUCTREQUEST-0026", String.valueOf(oldProductRequest.getPlanQuantity()), String.valueOf(oldProductRequest.getReleasedQuantity()));

		// Mantis - 0000042
		// if (oldProductRequest.getPlanQuantity() > oldProductRequest.getFinishedQuantity() + oldProductRequest.getScrappedQuantity()
		// && StringUtils.equals(oldProductRequest.getProductRequestState(), GenericServiceProxy.getConstantMap().Prq_Completed))
		// {
		// if (StringUtils.equals(oldProductRequest.getFactoryName(), "ARRAY"))
		// {
		// StringBuffer sql = new StringBuffer();
		// sql.append("SELECT MAX (PLANSEQUENCE) AS MAXSEQ ");
		// sql.append("  FROM PRODUCTREQUEST ");
		// sql.append(" WHERE PRODUCTREQUESTSTATE IN ('Released', 'Started') ");
		// sql.append("   AND MACHINENAME = :MACHINENAME ");
		//
		// Map<String, Object> args = new HashMap<String, Object>();
		// args.put("MACHINENAME", "2AUU01");
		//
		// @SuppressWarnings("unchecked")
		// List<Map<String, Object>> maxSeq = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		//
		// if (maxSeq.size() > 0)
		// {
		// String planSequence = Integer.toString(Integer.parseInt(ConvertUtil.getMapValueByName(maxSeq.get(0), "MAXSEQ")) + 1);
		// oldUdfs.put("PLANSEQUENCE", planSequence);
		// }
		// }
		//
		// eventInfo.setEventName("ChangeProductSpec");
		// MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeReleased(eventInfo, oldProductRequestName, oldUdfs);
		// }
	}

	private boolean existCreateStartedLot(ProductRequest productRequestData) throws CustomException
	{
		boolean checkCreateLot = false;

		// Check Lot of lotState(Created)

		String condition = "productRequestName =? and reserveState = ?";
		Object bindSet[] = new Object[] { productRequestData.getKey().getProductRequestName(), "Reserved" };
		try
		{
			List<ReserveLot> pLotList = ExtendedObjectProxy.getReserveLotService().select(condition, bindSet);

			if (pLotList.size() > 0)
				checkCreateLot = true;
		}
		catch (Exception e)
		{

		}

		return checkCreateLot;
	}

	private Lot updateSubProductQuantity(ProductSpec productSpecData, Lot lotData, long s1) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		List<Product> sequence = LotServiceProxy.getLotService().allUnScrappedProducts(lotData.getKey().getLotName());

		for (Product product : sequence)
		{
			product.setSubProductQuantity(s1);
			product.setSubProductQuantity1(s1);
			ProductServiceProxy.getProductService().update(product);
		}

		// calulate lot
		double[] result = this.calculateLotAllQuantity(lotData.getKey().getLotName());
		lotData.setSubProductQuantity(result[1]);
		lotData.setSubProductQuantity1(result[2]);
		lotData.setSubProductQuantity2(result[3]);
		LotServiceProxy.getLotService().update(lotData);

		lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getKey().getLotName());

		return lotData;
	}
	
	private void insertIntoSAPDB(ProductRequest newWO, ProductRequest oldWO, Lot lotData, EventInfo eventInfo) throws CustomException
	{
		List<Object[]> insertSAPList = new ArrayList<Object[]>();
		List<Object> insertSAPData = new ArrayList<Object>();
		
		SuperProductRequest newSuperWO = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{newWO.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
		String factoryPositionCode = "";
		String sql2 = " SELECT DESCRIPTION FROM ENUMDEFVALUE WHERE ENUMNAME=:ENUMNAME AND ENUMVALUE=:ENUMVALUE ";
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("ENUMNAME", "FactoryPosition");
		bindMap.put("ENUMVALUE", newSuperWO.getFactoryName());
		
		List<Map<String, Object>> sqlResult = 
				GenericServiceProxy.getSqlMesTemplate().queryForList(sql2, bindMap);
		
		if(sqlResult.size() > 0){
			factoryPositionCode = sqlResult.get(0).get("DESCRIPTION").toString();
		}
		else
		{
			factoryPositionCode="";
		}
		insertSAPData.add(TimeUtils.getCurrentEventTimeKey());
		insertSAPData.add(oldWO.getUdfs().get("SUPERPRODUCTREQUESTNAME"));
		insertSAPData.add(lotData.getProcessOperationName());
		insertSAPData.add(lotData.getProductQuantity());
		insertSAPData.add(newSuperWO.getProductType());
		insertSAPData.add(newSuperWO.getProductRequestName());
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
			insertSAPData.add(receiveTime.toString().replace("-","").substring(0,8));
		}
		else
		{
			insertSAPData.add(eventInfo.getEventTimeKey().substring(0,8));
		}
		insertSAPData.add("N");
		insertSAPData.add("");
		insertSAPData.add("");
		insertSAPData.add(lotData.getKey().getLotName());
		insertSAPData.add(eventInfo.getEventUser());
		insertSAPData.add(eventInfo.getEventComment());
		insertSAPData.add(factoryPositionCode);
		insertSAPList.add(insertSAPData.toArray());
		
		
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO MES_SAPIF_PP006@OADBLINK.V3FAB.COM (SEQ, OLDPRODUCTREQUESTNAME, PROCESSOPERATIONNAME, PRODUCTQUANTIRY, PRODUCTTYPE,  ");
		sql.append("    PRODUCTREQUESTNAME, EVENTTIME, ESBFLAG, RESULT, RESULTMESSAGE,  ");
		sql.append("    LOTNAME, EVENTUSER, EVENTCOMMENT,FACTORYPOSITION) VALUES ");
		sql.append("    (?, ?, ?, ?, ?, ");
		sql.append("    ?, ?, ?, ?, ?, ");
		sql.append("    ?, ?, ?, ? ) ");

		MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), insertSAPList);
	}
}
