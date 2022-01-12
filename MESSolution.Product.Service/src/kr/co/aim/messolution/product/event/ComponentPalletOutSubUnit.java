package kr.co.aim.messolution.product.event;

import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ComponentPalletHistory;
import kr.co.aim.messolution.extended.object.management.data.MaterialProduct;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.jdom.Document;
import org.jdom.Element;

public class ComponentPalletOutSubUnit extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", true);
		String palletName = SMessageUtil.getBodyItemValue(doc, "PALLETNAME", true);
		List<Element> panelList = SMessageUtil.getBodySequenceItemList(doc, "PANELLIST", false);

		// Insert to Component History
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ComponentPalletOutSubUnit", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		ComponentPalletHistory dataInfo = new ComponentPalletHistory();
		dataInfo.setTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		dataInfo.setPalletName(palletName);
		dataInfo.setEventName(eventInfo.getEventName());
		dataInfo.setEventTime(eventInfo.getEventTime());
		dataInfo.setEventUser(eventInfo.getEventUser());
		dataInfo.setMachineName(machineName);
		dataInfo.setUnitName(unitName);
		dataInfo.setSubUnitName(subUnitName);

		ExtendedObjectProxy.getComponentPalletHistoryService().create(eventInfo, dataInfo);

		Durable palletData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(palletName);

		for (Element panel : panelList)
		{
			String panelName = SMessageUtil.getChildText(panel, "PANELNAME", true);
			Lot panelData = MESLotServiceProxy.getLotServiceUtil().getLotData(panelName);

			MaterialProduct materialData = new MaterialProduct();
			materialData.setMachineName(machineName);
			materialData.setMaterialLocationName(subUnitName);
			materialData.setLotName("");
			materialData.setProductName(panelName);
			materialData.setMaterialName(palletData.getKey().getDurableName());
			materialData.setMaterialKind("Durable");
			materialData.setMaterialType(palletData.getDurableType());
			materialData.setQuantity(1);
			materialData.setTimeKey(eventInfo.getEventTimeKey());
			materialData.setEventName(eventInfo.getEventName());
			materialData.setEventTime(eventInfo.getEventTime());
			materialData.setFactoryName(panelData.getFactoryName());
			materialData.setProductSpecName(panelData.getProductSpecName());
			materialData.setProductSpecVersion(panelData.getProductSpecVersion());
			materialData.setProcessFlowName(panelData.getProcessFlowName());
			materialData.setProcessFlowVersion(panelData.getProcessFlowVersion());
			materialData.setProcessOperationName(panelData.getProcessOperationName());
			materialData.setProcessOperationVersion(panelData.getProcessOperationVersion());

			ExtendedObjectProxy.getMaterialProductService().create(eventInfo, materialData);
		}
	}
}
