package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.ext.ConsumedMaterial;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class CancelTrackInLotCellCut extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		
		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		List<Element> noCancelProductList = SMessageUtil.getBodySequenceItemList(doc, "NOCANCELPRODUCTLIST", false);
		
		String newLotName = "";

		// for common
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);

		Map<String, String> assignCarrierUdfs = new HashMap<String, String>();
		Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();

		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);

		MachineSpec machineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);

		String portType = portData.getUdfs().get("PORTTYPE");
		String machineDesc = machineSpecData.getMachineGroupName();

		// Check validation for carrierName(portType = PL)
		if (StringUtil.equals(portData.getUdfs().get("PORTTYPE"), "PL") && !StringUtils.isEmpty(carrierName))
		{
			CommonValidation.checkAvailableCST(carrierName, portType, machineDesc);
		}

		Lot cancelTrackInLot = new Lot();

		// get LotList by ProductList
		List<Map<String, Object>> lotListByProductList = MESLotServiceProxy.getLotServiceUtil().getLotListByProductList(productList);

		// nothing to track out case
		if (lotListByProductList.size() < 1)
			new CustomException("LOT-9001", "PRODUCTLIST : " + CommonUtil.makeListForQuery(productList, "PRODUCTNAME"));

		// first Lot of LotList is Base Lot : much productQty Lot of LotList
		Lot baseLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotListByProductList.get(0).get("LOTNAME").toString());

		ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(baseLotData.getFactoryName(), baseLotData.getProductSpecName(), baseLotData.getProductSpecVersion());

		double subProductUnitQuantity1 = baseLotData.getSubProductUnitQuantity1();

		if (noCancelProductList.size() == 0)
		{// after update on subProductUnitQuantity then keep original
			cancelTrackInLot = MESLotServiceProxy.getLotInfoUtil().getLotData(baseLotData.getKey().getLotName());
		}
		else
		{// create and merge
			Lot newLotData = MESLotServiceProxy.getLotServiceUtil().createNewLotWithSplit(eventInfo, baseLotData, productSpecData, subProductUnitQuantity1, "", assignCarrierUdfs);
			newLotName = newLotData.getKey().getLotName();

			List<Map<String, Object>> lotListByNoCancelProductList = MESLotServiceProxy.getLotServiceUtil().getLotListByProductList(productList);

			// nothing to track out case
			if (lotListByNoCancelProductList.size() < 1)
				new CustomException("LOT-9001", "PRODUCTLIST : " + CommonUtil.makeListForQuery(productList, "PRODUCTNAME"));

			for (Map<String, Object> lotM : lotListByNoCancelProductList)
			{
				String sLotName = CommonUtil.getValue(lotM, "LOTNAME");
				String sProductQuantity = CommonUtil.getValue(lotM, "PRODUCTQTY");

				MESLotServiceProxy.getLotServiceUtil().transferProductsToLot(eventInfo, newLotData, portData, sLotName, sProductQuantity, deassignCarrierUdfs, productList);
			}

			cancelTrackInLot = MESLotServiceProxy.getLotInfoUtil().getLotData(newLotName);

			Lot remainLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

			deassignCarrier(eventInfo, remainLotData);
		}

		List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequenceForManualCancel(SMessageUtil.getBodyElement(doc));

		deassignCarrierUdfs.clear();
		assignCarrierUdfs.clear();
		List<ConsumedMaterial> lotConsumedMaterail = new ArrayList<ConsumedMaterial>();

		eventInfo = EventInfoUtil.makeEventInfo("CancelTrackIn", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		cancelTrackInLot = MESLotServiceProxy.getLotServiceImpl().cancelTrackIn(eventInfo, cancelTrackInLot, productPGSRCSequence, assignCarrierUdfs, deassignCarrierUdfs, lotConsumedMaterail,
				carrierName);

		if (!StringUtil.equals(cancelTrackInLot.getLotHoldState(), "Y"))
		{
			// Set ReasonCode
			eventInfo.setReasonCodeType("HOLD");
			eventInfo.setReasonCode("HD100");

			// LotMultiHold
			MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, cancelTrackInLot, new HashMap<String, String>());
		}

		String returnLotname = cancelTrackInLot.getKey().getLotName();

		Document rtnDoc = new Document();
		rtnDoc = (Document) doc.clone();
		rtnDoc = SMessageUtil.addItemToBody(rtnDoc, "CANCELLOTNAME", returnLotname);

		return rtnDoc;
	}

	private void deassignCarrier(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());

		if (StringUtils.equals(carrierData.getDurableState(), "InUse"))
		{
			eventInfo.setEventName("Deassign");

			List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);

			DeassignCarrierInfo deassignCarrierInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, carrierData, productUSequence);

			LotServiceProxy.getLotService().deassignCarrier(lotData.getKey(), eventInfo, deassignCarrierInfo);
		}
	}

}
