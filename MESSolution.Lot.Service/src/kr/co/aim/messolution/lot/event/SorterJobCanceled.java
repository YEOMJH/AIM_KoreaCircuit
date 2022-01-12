package kr.co.aim.messolution.lot.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.OriginalProductInfo;
import kr.co.aim.messolution.extended.object.management.data.SortJobCarrier;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class SorterJobCanceled extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("SorterJobCanceled", getEventUser(), getEventComment(), null, null);

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String jobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);

		MESLotServiceProxy.getLotServiceUtil().changeSortJobState(eventInfo, jobName, GenericServiceProxy.getConstantMap().SORT_JOBSTATE_CANCELED);

		List<SortJobCarrier> sortJobCarrierList = ExtendedObjectProxy.getSortJobCarrierService().getSortJobCarrierListLotNameNotNull(jobName);

		EventInfo eventInfoL = EventInfoUtil.makeEventInfo_IgnoreHold("SorterJobCanceled", getEventUser(), getEventComment(), null, null);
		MESLotServiceProxy.getLotServiceUtil().completeSortFlow(eventInfoL, sortJobCarrierList);

		List<Map<String, Object>> ifiList = getIFIProductList(jobName);
		
		for (Map<String, Object> ifi : ifiList)
		{
			String productName = ConvertUtil.getMapValueByName(ifi, "PRODUCTNAME");
			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

			// Delete OriginalProductInfo Data by CreateSortJobForIFI
			if (!StringUtils.isEmpty(productData.getUdfs().get("CHANGESHOPLOTNAME")))
			{
				kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
				Map<String, String> udfs = setEventInfo.getUdfs();
				udfs.put("CHANGESHOPLOTNAME", "");

				MESProductServiceProxy.getProductServiceImpl().setEvent(productData, setEventInfo, eventInfo);

				OriginalProductInfo oriData = new OriginalProductInfo();

				try
				{
					oriData = ExtendedObjectProxy.getOriginalProductInfoService().selectByKey(false, new Object[] { productName });
					ExtendedObjectProxy.getOriginalProductInfoService().remove(eventInfo, oriData);
				}
				catch (Exception e)
				{
					oriData = null;
				}
			}
		}
	}

	private List<Map<String, Object>> getIFIProductList(String jobName)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT P.PRODUCTNAME, P.CHANGESHOPLOTNAME ");
		sql.append("  FROM CT_SORTJOBPRODUCT SP, PRODUCT P ");
		sql.append(" WHERE SP.PRODUCTNAME = P.PRODUCTNAME ");
		sql.append("   AND P.CHANGESHOPLOTNAME IS NOT NULL ");
		sql.append("   AND SP.JOBNAME = :JOBNAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("JOBNAME", jobName);
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		
		return sqlResult;
	}

}
