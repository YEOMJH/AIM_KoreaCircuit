package kr.co.aim.messolution.product.event;

import java.util.ArrayList;
import java.util.List;




import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ComponentHistory;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.MakeScrappedInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

public class ComponentPanelListScrap extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", true);
		
		List<Element> LotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ScrapLot", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		for (Element Lot : LotList)
		{
			String lotName = SMessageUtil.getChildText(Lot, "LOTNAME", true);
			String productName = SMessageUtil.getChildText(Lot, "PRODUCTNAME", true);
			String vcrProductName = SMessageUtil.getChildText(Lot, "VCRPRODUCTNAME", false);
			String productGrade = SMessageUtil.getChildText(Lot, "PRODUCTGRADE", false);
			String productJudge = SMessageUtil.getChildText(Lot, "PRODUCTJUDGE", false);
			String productType = SMessageUtil.getChildText(Lot, "PRODUCTTYPE", false);
			String fromSlotID = SMessageUtil.getChildText(Lot, "FROMSLOTID", false);
			String toSlotID = SMessageUtil.getChildText(Lot, "TOSLOTID", false);
			String scrapCode = SMessageUtil.getChildText(Lot, "SCRAPCODE", false);
			
			Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
			String factoryName = lotData.getFactoryName();
			String productSpecName = lotData.getProductSpecName();
			String productSpecVersion = lotData.getProductSpecVersion();
			String processFlowName = lotData.getProcessFlowName();
			String processFlowVersion = lotData.getProcessFlowVersion();
			String processOperationName = lotData.getProcessOperationName();
			String processOperationVersion = lotData.getProcessOperationVersion();
			String productionType = lotData.getProductionType();
			String productRequestName = lotData.getProductRequestName();
			
			// Insert to Component History

			ComponentHistory dataInfo = new ComponentHistory();
			dataInfo.setTimeKey(eventInfo.getEventTimeKey());
			dataInfo.setProductName(productName);
			dataInfo.setLotName(lotName);
			dataInfo.setEventName(eventInfo.getEventName());
			dataInfo.setEventTime(eventInfo.getEventTime());
			dataInfo.setEventUser(eventInfo.getEventUser());
			dataInfo.setFactoryName(factoryName);
			dataInfo.setProductSpecName(productSpecName);
			dataInfo.setProductSpecVersion(productSpecVersion);
			dataInfo.setProcessFlowName(processFlowName);
			dataInfo.setProcessFlowVersion(processFlowVersion);
			dataInfo.setProcessOperationName(processOperationName);
			dataInfo.setProcessOperationVersion(processOperationVersion);
			dataInfo.setProductionType(productionType);
			dataInfo.setProductType(productType);
			dataInfo.setMachineName(machineName);
			dataInfo.setMaterialLocationName(subUnitName);
			dataInfo.setProductRequestName(productRequestName);
			dataInfo.setReasonCode(scrapCode);

			ComponentHistory componentHistoryData = ExtendedObjectProxy.getComponentHistoryService().create(eventInfo, dataInfo);

			// Lot Scrap
			eventInfo = EventInfoUtil.makeEventInfo("ScrapLot", this.getEventUser(), this.getEventComment(), "", "");
			lotData.setLotGrade("S");

			this.makeScrapped(eventInfo, lotData);

			// change scrap qty
			MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfo, lotData.getProductRequestName(), 1, 0);
			
			ProductRequest newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());

			if (newProductRequestData.getPlanQuantity() == newProductRequestData.getFinishedQuantity() + newProductRequestData.getScrappedQuantity()) 
			{
				EventInfo newEventInfo = eventInfo;
				newEventInfo.setEventName("Completed");
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompleted(newEventInfo, lotData.getProductRequestName());
			}
			
		}
	}
	
	private void makeScrapped(EventInfo eventInfo, Lot lotData) throws CustomException
	{	
		SetEventInfo lotEventInfo = new SetEventInfo();
		
		lotData.setLotState("Scrapped");
		lotData.setProductQuantity(0);
		lotEventInfo.setUdfs(lotData.getUdfs());
		
		LotServiceProxy.getLotService().update(lotData);
		LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, lotEventInfo);
		
	}
}
