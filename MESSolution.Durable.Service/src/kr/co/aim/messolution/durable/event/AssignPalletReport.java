package kr.co.aim.messolution.durable.event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.product.management.data.Product;

public class AssignPalletReport extends AsyncHandler {

	private static Log log = LogFactory.getLog(AssignPalletReport.class);
	private List<Object[]> updateArgList = new ArrayList<Object[]>();

	public void doWorks(Document doc) throws CustomException
	{
		// MACHINENAME
		// UNITNAME
		// LOTNAME
		// PALLETNAME
		// PRODUCTLIST
		// PRODUCT
		// PRODUCTNAME
		// FPCNAME

		String messageName = SMessageUtil.getMessageName(doc);

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String palletName = SMessageUtil.getBodyItemValue(doc, "PALLETNAME", false);

		Element bodyElement = SMessageUtil.getBodyElement(doc);
		Element productListElement = bodyElement.getChild("PRODUCTLIST");

		EventInfo eventInfo = EventInfoUtil.makeEventInfo(messageName, this.getEventUser(), this.getEventComment(), null, null);
		Durable palletData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(palletName);

		if (productListElement != null)
		{
			String timeKey = TimeUtils.getCurrentEventTimeKey();

			for (Iterator<?> iterator = productListElement.getChildren().iterator(); iterator.hasNext();)
			{
				Element product = (Element) iterator.next();
				String productName = product.getChildText("PRODUCTNAME");
				String fpcName = product.getChildText("FPCNAME");

				Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
				Durable fpcData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(fpcName);

				setArgs(timeKey, palletData, productData, eventInfo, machineName, unitName);
				setArgs(timeKey, fpcData, productData, eventInfo, machineName, unitName);
			}

			if (updateArgList.size() > 0)
				MESConsumableServiceProxy.getConsumableServiceUtil().InsertCT_MaterialProduct(updateArgList);
			else
				log.error("Cannot insert into CT_MATERIALPRODUCT because insert item[Pallet or FPC] does not exist");
		}
	}

	private void setArgs(String timeKey, Durable durableData, Product productData, EventInfo eventInfo, String machineName, String materialLocationName)
	{
		List<Object> bindList = new ArrayList<Object>();

		bindList.add(timeKey);
		bindList.add(productData.getKey().getProductName());
		bindList.add(productData.getLotName());
		bindList.add(GenericServiceProxy.getConstantMap().MaterialKind_Durable);
		bindList.add(durableData.getDurableType());
		bindList.add(durableData.getKey().getDurableName());
		bindList.add("1");
		bindList.add(eventInfo.getEventName());
		bindList.add("TO_DATE(SUBSTR('" + timeKey + "',0,14),'YYYYMMDDHH24MISS')");
		bindList.add(productData.getFactoryName());
		bindList.add(productData.getProductSpecName());
		bindList.add(productData.getProductSpecVersion());
		bindList.add(productData.getProcessFlowName());
		bindList.add(productData.getProcessFlowVersion());
		bindList.add(productData.getProcessOperationName());
		bindList.add(productData.getProcessOperationVersion());
		bindList.add(machineName);
		bindList.add(materialLocationName);

		updateArgList.add(bindList.toArray());
	}

}
