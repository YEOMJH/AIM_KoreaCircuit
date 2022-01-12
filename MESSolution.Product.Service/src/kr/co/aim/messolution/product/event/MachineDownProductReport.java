package kr.co.aim.messolution.product.event;

import java.util.List;

import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.TFEDownProduct;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductHistory;

public class MachineDownProductReport extends AsyncHandler 
{
	@Override
	public void doWorks(Document doc) throws CustomException 
	{
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);

		if (subUnitName.isEmpty())
			subUnitName = "-";

		Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

		String trackInTime = "";
		
		if (productData.getLastEventName().equals("TrackIn"))
		{
			trackInTime = ConvertUtil.toString(productData.getLastEventTime());
		}
		else
		{
			try
			{
				// get trackIn History
				List<ProductHistory> productHist = ProductServiceProxy.getProductHistoryService().select(" WHERE  MACHINENAME = ? AND EVENTNAME =? AND PRODUCTNAME =? ORDER BY TIMEKEY DESC ",
						new Object[] { machineName, "TrackIn", productName });

				trackInTime = ConvertUtil.toString(productHist.get(0).getEventTime());

			}
			catch (Exception ex)
			{
				throw new CustomException(ex.getCause());
			}
		}

		String timeKey = TimeUtils.getCurrentEventTimeKey();
		TFEDownProduct dataInfo = new TFEDownProduct(timeKey, productData.getKey().getProductName());
		dataInfo.setLotName(productData.getLotName());
		dataInfo.setFactoryName(productData.getFactoryName());
		dataInfo.setProductRequestName(productData.getProductRequestName());
		dataInfo.setProductSpecName(productData.getProductSpecName());
		dataInfo.setProductSpecVersion(productData.getProductSpecVersion());
		dataInfo.setProcessFlowName(productData.getProcessFlowName());
		dataInfo.setProcessFlowVersion(productData.getProcessFlowVersion());
		dataInfo.setProcessOperationName(productData.getProcessOperationName());
		dataInfo.setProcessOperationVersion(productData.getProcessOperationVersion());
		dataInfo.setProductionType(productData.getProductionType());
		dataInfo.setProductType(productData.getProductType());
		dataInfo.setMachineName(machineName);
		dataInfo.setUnitName(unitName);
		dataInfo.setSubUnitName(subUnitName);
		dataInfo.setMachineRecipeName(productData.getMachineRecipeName());
		dataInfo.setTrackInTime(trackInTime);
		dataInfo.setEventName(this.getClass().getSimpleName());
		dataInfo.setEventUser(this.getEventUser());
		dataInfo.setEventTime(TimeUtils.getCurrentTimestamp());

		ExtendedObjectProxy.getTFEDownProductService().insert(dataInfo);
	}
}

