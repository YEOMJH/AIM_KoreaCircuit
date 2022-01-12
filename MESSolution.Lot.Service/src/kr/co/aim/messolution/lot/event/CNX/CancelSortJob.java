package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.OriginalProductInfo;
import kr.co.aim.messolution.extended.object.management.data.SortJob;
import kr.co.aim.messolution.extended.object.management.data.SortJobCarrier;
import kr.co.aim.messolution.extended.object.management.data.SortJobProduct;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class CancelSortJob extends SyncHandler {
	Log log = LogFactory.getLog(CancelSortJob.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String jobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelSortJob", this.getEventUser(), this.getEventComment(), "", "");
		SortJob sortJobData = ExtendedObjectProxy.getSortJobService().getSortJobData(jobName);

		String jobType = sortJobData.getJobType();
		
		if (!StringUtils.equals(sortJobData.getJobState(), GenericServiceProxy.getConstantMap().SORT_JOBSTATE_RESERVED))
		{
			throw new CustomException("SORT-0005", sortJobData.getJobName(), GenericServiceProxy.getConstantMap().SORT_JOBSTATE_RESERVED);
		}

		MESLotServiceProxy.getLotServiceUtil().deleteSortJob(eventInfo, jobName);

		Element body = SMessageUtil.getBodyElement(doc);
		List<String> carrierNameList = CommonUtil.makeListExceptNull(body, "SORTJOBLIST", "CARRIERNAME");
		List<String> productNameList = CommonUtil.makeListExceptNull(body, "SORTJOBLIST", "PRODUCTNAME");

		for (String carrierName : carrierNameList)
		{
			if (StringUtils.isNotEmpty(carrierName))
			{
				// Delete SortJobCarrier
				SortJobCarrier jobCarrierData = ExtendedObjectProxy.getSortJobCarrierService().selectByKey(false, new Object[] { jobName, carrierName });
				ExtendedObjectProxy.getSortJobCarrierService().remove(eventInfo, jobCarrierData);

				Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

				// update Carrier TransportLockFlag
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.getUdfs().put("TRANSPORTLOCKFLAG", "");

				DurableServiceProxy.getDurableService().setEvent(carrierData.getKey(), eventInfo, setEventInfo);
			}
		}

		if (productNameList.size() > 0)
		{
			for (String product : productNameList)
			{
				// Delete SrotJobProduct
				SortJobProduct jobProductData = ExtendedObjectProxy.getSortJobProductService().selectByKey(false, new Object[] { jobName, product });
				ExtendedObjectProxy.getSortJobProductService().remove(eventInfo, jobProductData);

				Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(product);

				// Delete OriginalProductInfo Data by CreateSortJobForIFI
				if (StringUtils.equals(jobType, "Split(TP to IFI)") && !StringUtils.isEmpty(productData.getUdfs().get("CHANGESHOPLOTNAME")))
				{
					kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
					Map<String, String> udfs = setEventInfo.getUdfs();
					udfs.put("CHANGESHOPLOTNAME", "");

					MESProductServiceProxy.getProductServiceImpl().setEvent(productData, setEventInfo, eventInfo);

					OriginalProductInfo oriData = new OriginalProductInfo();

					try
					{
						oriData = ExtendedObjectProxy.getOriginalProductInfoService().selectByKey(false, new Object[] { product });
						ExtendedObjectProxy.getOriginalProductInfoService().remove(eventInfo, oriData);
					}
					catch (Exception e)
					{
						oriData = null;
					}
				}
			}
		}

		return doc;
	}
}
