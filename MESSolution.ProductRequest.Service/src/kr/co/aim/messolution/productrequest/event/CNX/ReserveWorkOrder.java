package kr.co.aim.messolution.productrequest.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.productrequest.management.info.MakeReleasedInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ReserveWorkOrder extends SyncHandler {

	public Object doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);

		List<Element> productRequestList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTREQUESTLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		for (Element productRequest : productRequestList)
		{
			String productRequestName = SMessageUtil.getChildText(productRequest, "PRODUCTREQUESTNAME", true);
			String planSequence = SMessageUtil.getChildText(productRequest, "PLANSEQUENCE", true);

			ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);

			if (StringUtils.equals(productRequestData.getProductRequestState(), GenericServiceProxy.getConstantMap().Prq_Completed))
			{
				throw new CustomException("PRODUCTREQUEST-0032", productRequestData.getKey().getProductRequestName());
			}

			if (StringUtils.equals(productRequestData.getProductRequestState(), GenericServiceProxy.getConstantMap().Prq_Created))
			{
				eventInfo.setEventName("ReserveWorkOrder");
				MakeReleasedInfo makeReleasedInfo = MESWorkOrderServiceProxy.getProductRequestInfoUtil().makeReleasedInfo(productRequestData);

				Map<String, String> udfs = new HashMap<String, String>();
				udfs.put("PLANSEQUENCE", planSequence);
				udfs.put("MACHINENAME", machineName);

				makeReleasedInfo.setUdfs(udfs);

				MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeReleased(productRequestData, makeReleasedInfo, eventInfo);
			}
			else
			{
				if (!StringUtils.equals(productRequestData.getUdfs().get("PLANSEQUENCE"), planSequence))
				{
					eventInfo.setEventName("ChangePosition");
					ChangeSpecInfo changeSpecInfo = MESWorkOrderServiceProxy.getProductRequestInfoUtil().changeSpecInfo(productRequestData.getFactoryName(),
							productRequestData.getProductRequestType(), productRequestData.getProductSpecName(), productRequestData.getProductSpecVersion(), productRequestData.getPlanFinishedTime(),
							productRequestData.getPlanReleasedTime(), productRequestData.getPlanQuantity(), productRequestData.getReleasedQuantity(), productRequestData.getFinishedQuantity(),
							productRequestData.getScrappedQuantity(), productRequestData.getProductRequestState(), productRequestData.getProductRequestHoldState(),
							productRequestData.getUdfs().get("PROCESSFLOWNAME"), productRequestData.getUdfs().get("PROCESSFLOWVERSION"), productRequestData.getUdfs().get("AUTOSHIPPINGFLAG"),
							planSequence, productRequestData.getUdfs().get("CREATEDQUANTITY"), productRequestData.getUdfs().get("DESCRIPTION"));

					MESWorkOrderServiceProxy.getProductRequestServiceImpl().changeSpec(productRequestData, changeSpecInfo, eventInfo);
				}
			}
		}

		return doc;
	}
}
