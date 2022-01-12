package kr.co.aim.messolution.lot.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaterialProduct;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.GradeDefUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.product.service.ProductPP;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.MakeReleasedInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.IncrementReleasedQuantityByInfo;

public class UnpackerProcessEnd extends AsyncHandler {

	private static Log log = LogFactory.getLog(UnpackerProcessEnd.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String workOrder = SMessageUtil.getBodyItemValue(doc, "WORKORDER", true);

		Element virtualProductList = SMessageUtil.getBodySequenceItem(doc, "PRODUCTLIST", true);

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		String planDate = lotData.getUdfs().get("PLANDATE").toString();
		ReserveLot ReserveData = new ReserveLot();

		try
		{
			List<ReserveLot> ReserveLotList = ExtendedObjectProxy.getReserveLotService().select("machineName = ? and lotName = ? and productRequestName = ? and reserveState = ?",
					new Object[] { machineName, lotName, workOrder, "Executing" });

			ReserveData = ReserveLotList.get(0);
			lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(ReserveData.getLotName());
		}
		catch (Exception e)
		{
			log.info("Not found CT_RESERVELOT Data");
		}

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

		// event information
		String timeKey = TimeUtils.getCurrentEventTimeKey();
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Release", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(timeKey);

		List<String> newProductList = new ArrayList<String>();

		if (lotData.getLotState().equals(GenericServiceProxy.getConstantMap().Lot_Created))
		{
			String NewLotName = lotName.substring(0, 9);

			newProductList = this.generateProductName("", NewLotName, Double.parseDouble(lotData.getUdfs().get("PLANPRODUCTQUANTITY")));
		}

		List<ProductPGS> productPGSSequence = this.setProductPGSSequence(lotData, lotData.getFactoryName(), virtualProductList, newProductList, (long) lotData.getSubProductUnitQuantity1(),
				lotData.getProductRequestName(), machineName);

		// Validation
		checkcrate(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), productPGSSequence);

		// 1. Release Lot
		// Set MachineName
		lotData.setMachineName(machineName);
		LotServiceProxy.getLotService().update(lotData);

		// Set MakeReleasedInfo
		MakeReleasedInfo releaseInfo = MESLotServiceProxy.getLotInfoUtil().makeReleasedInfo(lotData, machineData.getAreaName(), lotData.getNodeStack(), lotData.getProcessFlowName(),
				lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), lotData.getProductionType(), lotData.getUdfs(), "", lotData.getDueDate(),
				lotData.getPriority());

		releaseInfo.getUdfs().put("RELEASEQUANTITY", Integer.toString(productPGSSequence.size()));
		
		// Release Lot
		lotData = MESLotServiceProxy.getLotServiceImpl().releaseLot(eventInfo, lotData, releaseInfo, productPGSSequence);

		// 2. increment Product Request
		this.incrementProductRequest(eventInfo, productPGSSequence, lotData.getProductRequestName(),lotData);

		// 3. refresh CT_ReserveLotList
		updateInputPlan(eventInfo, ReserveData);

		// 4. consume DP box 
		decreaseCrateQuantity(eventInfo, lotData, productPGSSequence, machineName);

		// 5. Auto Track In / Out Unpacker
		String crateName = this.getCrateName(virtualProductList);
		String loadPort = this.getLoadPortByConsumable(crateName);

		Document trackInOutDoc = writeTrackInOutRequest(doc, lotName, machineName, carrierName, loadPort, portName);
		String replySubject = GenericServiceProxy.getESBServive().getSendSubject("PEXsvr");
		GenericServiceProxy.getESBServive().sendBySender(replySubject, trackInOutDoc, "LocalSender");
	}

	private List<ProductPGS> setProductPGSSequence(Lot lotData, String factoryName, Element productList, List<String> newProductList, long createSubProductQuantity, String projectName,
			String machineName) throws CustomException
	{
		List<ProductPGS> productPGSSequence = new ArrayList<ProductPGS>();

		int idx = 0;
		int pos = 0;
		for (Iterator itProduct = productList.getChildren().iterator(); itProduct.hasNext();)
		{
			idx++;

			Element eProduct = (Element) itProduct.next();

			try
			{
				String sProductName = SMessageUtil.getChildText(eProduct, "VCRPRODUCTNAME", true);
				String sPosition = SMessageUtil.getChildText(eProduct, "POSITION", true);
				String sCrateName = SMessageUtil.getChildText(eProduct, "CRATENAME", false);

				ProductPP productInfo = new ProductPP();
				productInfo.setPosition(Long.parseLong(sPosition));
				productInfo.setProductGrade(GradeDefUtil.getGrade(GenericServiceProxy.getConstantMap().DEFAULT_FACTORY, GenericServiceProxy.getConstantMap().GradeType_Product, true).getGrade());
				productInfo.setProductName(newProductList.get(pos));
				productInfo.setProductRequestName(lotData.getProductRequestName());
				productInfo.setSubProductGrades1("");
				productInfo.setSubProductQuantity1(createSubProductQuantity);

				productInfo.getUdfs().put("CRATENAME", sCrateName);
				productInfo.getUdfs().put("OLDPRODUCTREQUESTNAME", lotData.getProductRequestName());
				productInfo.getUdfs().put("VIRTUALGLASSID", sProductName);
				productInfo.getUdfs().put("MAINMACHINENAME", machineName);

				productPGSSequence.add(productInfo);
			}
			catch (Exception ex)
			{
				eventLog.info(String.format("[%d]th Product not set", idx));
				// go to next
			}

			pos++;

		}

		return productPGSSequence;
	}

	private void updateInputPlan(EventInfo eventInfo, ReserveLot ReserveData) throws CustomException
	{
		ReserveData.setReserveState(GenericServiceProxy.getConstantMap().RESV_STATE_END);
		ReserveData.setInputTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		ReserveData.setCompleteTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		try
		{
			ExtendedObjectProxy.getReserveLotService().modify(eventInfo, ReserveData);
		}
		catch (greenFrameDBErrorSignal ne)
		{
			throw new CustomException("CRATE-8001", ne.getMessage());
		}
	}

	private void decreaseCrateQuantity(EventInfo eventInfo, Lot lotData, List<ProductPGS> productPGSSequence, String machineName) throws CustomException
	{
		Map<String, Object> crateMap = new HashMap<String, Object>();

		String oldCrateName = "";

		int count = 0;

		for (ProductPGS productPGS : productPGSSequence)
		{
			if (!oldCrateName.equals(CommonUtil.getValue(productPGS.getUdfs(), "CRATENAME")))
			{
				// initialize
				count = 0;
				oldCrateName = CommonUtil.getValue(productPGS.getUdfs(), "CRATENAME");
			}

			count++;
			crateMap.put(oldCrateName, count);

			eventInfo.setEventName("Release");
			insertMaterialProduct(eventInfo, productPGS, CommonUtil.getValue(productPGS.getUdfs(), "CRATENAME"), machineName);
		}

		for (String crateName : crateMap.keySet())
		{
			if (crateMap.get(crateName) != null)
			{
				Consumable crateDataBefore = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(crateName);
				int quantity = Integer.parseInt(crateMap.get(crateName).toString());
				int nowQty = (int) crateDataBefore.getQuantity();
				int actQty = nowQty - quantity;
				boolean flag = false;

				if (quantity > nowQty)
				{
					quantity = nowQty;
					flag = true;
				}

				eventInfo = EventInfoUtil.makeEventInfo("AdjustQty", getEventUser(), getEventComment(), null, null);
				decreaseCrateQuantity(eventInfo, lotData, crateName, quantity);

				// makeNotAvailable
				Consumable crateDataAfter = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(crateName);
				if (crateDataAfter.getQuantity() == 0 && StringUtil.equals(crateDataAfter.getConsumableState(), "Available"))
				{
					eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), null, null);

					MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();
					makeNotAvailableInfo.setUdfs(crateDataAfter.getUdfs());
					MESConsumableServiceProxy.getConsumableServiceImpl().makeNotAvailable(crateDataAfter, makeNotAvailableInfo, eventInfo);
				}

				if (flag)
				{
					try
					{
						String sql = "UPDATE CONSUMABLEHISTORY SET QUANTITY = :QUANTITY WHERE CONSUMABLENAME = :CONSUMABLENAME AND TIMEKEY = :TIMEKEY ";

						Map<String, String> args = new HashMap<String, String>();
						args.put("QUANTITY", String.valueOf(actQty));
						args.put("CONSUMABLENAME", crateName);
						args.put("TIMEKEY", eventInfo.getEventTimeKey());

						GenericServiceProxy.getSqlMesTemplate().update(sql, args);

						sql = "UPDATE CONSUMABLE SET QUANTITY = :QUANTITY WHERE CONSUMABLENAME = :CONSUMABLENAME ";
						GenericServiceProxy.getSqlMesTemplate().update(sql, args);
					}
					catch (Exception e)
					{
					}
				}
			}
		}
	}

	private void decreaseCrateQuantity(EventInfo eventInfo, Lot lotData, String consumableName, double quantity) throws CustomException
	{
		eventInfo.setEventName("Consume");

		Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(consumableName);

		DecrementQuantityInfo transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().decrementQuantityInfo(lotData.getKey().getLotName(), lotData.getProcessOperationName(),
				lotData.getProcessOperationVersion(), "", TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()), quantity, lotData.getUdfs());

		MESConsumableServiceProxy.getConsumableServiceImpl().decrementQuantity(consumableData, transitionInfo, eventInfo);
	}

	private void incrementProductRequest(EventInfo eventInfo, List<ProductPGS> productPGSSequence, String productRequestName,Lot lotData) throws CustomException
	{
		ProductRequest workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);

		int incrementQty = productPGSSequence.size();
	    int cratedecrementQty=Integer.parseInt(lotData.getUdfs().get("PLANPRODUCTQUANTITY"))-productPGSSequence.size();

		IncrementReleasedQuantityByInfo incrementReleasedQuantityByInfo = new IncrementReleasedQuantityByInfo();
		incrementReleasedQuantityByInfo.setQuantity(incrementQty);

		eventInfo.setEventName("IncreamentQuantity");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		ProductRequest newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementReleasedQuantityBy(workOrderData, incrementReleasedQuantityByInfo, eventInfo);

		if (newProductRequestData.getPlanQuantity() < newProductRequestData.getReleasedQuantity())
		{
			throw new CustomException("PRODUCTREQUEST-0026", String.valueOf(newProductRequestData.getPlanQuantity()), String.valueOf(newProductRequestData.getReleasedQuantity()));
		}
		
		MESWorkOrderServiceProxy.getProductRequestServiceImpl().DecrementCreatedQuantityBy(newProductRequestData, cratedecrementQty, eventInfo);
	}

	private String getCrateName(Element productList) throws CustomException
	{
		String crateName = "";

		for (Iterator itProduct = productList.getChildren().iterator(); itProduct.hasNext();)
		{
			Element eProduct = (Element) itProduct.next();

			try
			{
				crateName = SMessageUtil.getChildText(eProduct, "CRATENAME", true);
			}
			catch (Exception ex)
			{
				crateName = "";
			}
		}

		return crateName;
	}

	private String getLoadPortByConsumable(String crateName) throws CustomException
	{
		String loadPort = "";
		try
		{
			Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(crateName);
			loadPort = consumableData.getUdfs().get("PORTNAME");

			if (consumableData.getMaterialLocationName().isEmpty())
			{
				loadPort = "P01";
			}
		}
		catch (Exception ex)
		{
			loadPort = "P01";
		}

		return loadPort;
	}

	private Document writeTrackInOutRequest(Document doc, String lotName, String machineName, String carrierName, String loadPort, String unLoadPort) throws CustomException
	{
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "TrackInOutLot");

		boolean result = doc.getRootElement().removeChild(SMessageUtil.Body_Tag);

		Element eleBodyTemp = new Element(SMessageUtil.Body_Tag);

		Element element1 = new Element("LOTNAME");
		element1.setText(lotName);
		eleBodyTemp.addContent(element1);

		Element element2 = new Element("MACHINENAME");
		element2.setText(machineName);
		eleBodyTemp.addContent(element2);

		Element element3 = new Element("CARRIERNAME");
		element3.setText(carrierName);
		eleBodyTemp.addContent(element3);

		Element element4 = new Element("LOADPORT");
		element4.setText(loadPort);
		eleBodyTemp.addContent(element4);

		Element element5 = new Element("UNLOADPORT");
		element5.setText(unLoadPort);
		eleBodyTemp.addContent(element5);

		// overwrite
		doc.getRootElement().addContent(eleBodyTemp);

		return doc;
	}

	private List<String> generateProductName(String ruleName, String prefix, double quantity) throws CustomException
	{
		List<String> argSeq = new ArrayList<String>();
		argSeq.add(prefix);

		List<String> names = new ArrayList<String>();

		for (int i = 0; i < quantity; i++)
		{
			names.add(String.format("%s%02d", prefix, 31-(int)quantity+i));
		}

		return names;
	}

	private void checkcrate(String factoryName, String ProductSpecName, String productSpecVersion, List<ProductPGS> productPGSSequence) throws CustomException
	{
		for (ProductPGS productPGS : productPGSSequence)
		{
			String sCrateName = CommonUtil.getValue(productPGS.getUdfs(), "CRATENAME");
			Consumable crateData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(sCrateName);

			StringBuilder sql = new StringBuilder();
			sql.append("SELECT PC.MATERIALSPECNAME ");
			sql.append("  FROM TPPOLICY T, POSBOM PC ");
			sql.append(" WHERE PC.CONDITIONID = T.CONDITIONID ");
			sql.append("   AND T.FACTORYNAME = :FACTORYNAME ");
			sql.append("   AND T.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
			sql.append("   AND T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");

			Map<String, String> args = new HashMap<String, String>();
			args.put("FACTORYNAME", factoryName);
			args.put("PRODUCTSPECNAME", ProductSpecName);
			args.put("PRODUCTSPECVERSION", productSpecVersion);

			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (sqlResult.size() == 0)
			{
				throw new CustomException("CRATE-0007");
			}
			else
			{
				boolean flag = true;

				for (Map<String, Object> row : sqlResult)
				{
					if (StringUtil.equals(ConvertUtil.getMapValueByName(row, "MATERIALSPECNAME"), crateData.getConsumableSpecName()))
					{
						flag = false;
						break;
					}
				}
				if (flag)
				{
					throw new CustomException("CRATE-0008");
				}
			}
		}
	}

	private void insertMaterialProduct(EventInfo eventInfo, ProductPGS productPGS, String consumableName, String machineName) throws CustomException
	{
		Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productPGS.getProductName());
		Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(consumableName);

		MaterialProduct dataInfo = new MaterialProduct();
		dataInfo.setTimeKey(eventInfo.getEventTimeKey());
		dataInfo.setProductName(productData.getKey().getProductName());
		dataInfo.setLotName(productData.getLotName());
		dataInfo.setMaterialKind(GenericServiceProxy.getConstantMap().MaterialKind_Consumable);
		dataInfo.setMaterialType(consumableData.getConsumableType());
		dataInfo.setMaterialName(consumableData.getKey().getConsumableName());
		dataInfo.setQuantity(1);
		dataInfo.setEventName(eventInfo.getEventName());
		dataInfo.setEventTime(eventInfo.getEventTime());
		dataInfo.setFactoryName(productData.getFactoryName());
		dataInfo.setProductSpecName(productData.getProductSpecName());
		dataInfo.setProductSpecVersion(productData.getProductSpecVersion());
		dataInfo.setProcessFlowName(productData.getProcessFlowName());
		dataInfo.setProcessFlowVersion(productData.getProcessFlowVersion());
		dataInfo.setProcessOperationName(productData.getProcessOperationName());

		dataInfo.setProcessOperationVersion(productData.getProcessOperationVersion());
		dataInfo.setMachineName(machineName);
		dataInfo.setMaterialLocationName("");

		ExtendedObjectProxy.getMaterialProductService().create(eventInfo, dataInfo);
	}
}
