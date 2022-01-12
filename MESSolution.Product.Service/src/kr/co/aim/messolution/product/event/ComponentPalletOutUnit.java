package kr.co.aim.messolution.product.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ComponentHistory;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.product.management.data.Product;

public class ComponentPalletOutUnit extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException {
		
		/*
		 	MACHINENAME
			PALLETNAME
			UNITNAME
			PANELLIST
			  PANEL
			    PALENNAME
		 */
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String palletName = SMessageUtil.getBodyItemValue(doc, "PALLETNAME", true);
		
		
		//Check Machine
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);

		//Insert to Component History
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ComponentPalletOutUnit", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
						
				
		List<Element> ePanelList = SMessageUtil.getBodySequenceItemList(doc, "PANELLIST", false);
		List<ComponentHistory> panelList = new ArrayList<ComponentHistory>();
		
				
		for(Element panelE : ePanelList)
		{
			String panelName = panelE.getChildText("PANELNAME");
			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(panelName);
			
			ComponentHistory dataInfo = new ComponentHistory();
			dataInfo.setTimeKey(eventInfo.getEventTimeKey());
			dataInfo.setProductName(panelName);
			dataInfo.setLotName(productData.getLotName());
			dataInfo.setEventName(eventInfo.getEventName());
			
			dataInfo.setEventTime(eventInfo.getEventTime());
			dataInfo.setEventUser(eventInfo.getEventUser());
			dataInfo.setFactoryName(productData.getFactoryName());
			dataInfo.setProductSpecName(productData.getProductSpecName());
			dataInfo.setProductSpecVersion(productData.getProductSpecVersion());
			dataInfo.setProcessFlowName(productData.getProcessFlowName());
			dataInfo.setProcessFlowVersion(productData.getProcessFlowVersion());
			dataInfo.setProcessOperationName(productData.getProcessOperationName());
			dataInfo.setProcessOperationVersion(productData.getProcessOperationVersion());
			dataInfo.setProductionType(productData.getProductionType());
			dataInfo.setProductType(productData.getProductType());
			dataInfo.setMachineName(machineName);
			dataInfo.setMaterialLocationName(unitName);
			dataInfo.setPalletName(palletName);
			dataInfo.setProductRequestName(productData.getProductRequestName());
			
			panelList.add(dataInfo);
		}
		
		
		ExtendedObjectProxy.getComponentHistoryService().create(eventInfo, panelList);
		
	}
}
