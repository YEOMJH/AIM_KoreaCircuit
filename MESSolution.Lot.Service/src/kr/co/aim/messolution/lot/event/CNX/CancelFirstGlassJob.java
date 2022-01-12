package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.FirstGlassJob;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.MakeNotOnHoldInfo;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class CancelFirstGlassJob extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelFirstGlassJob", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		eventInfo.setEventComment("CancelFirstGlassJob: " + eventInfo.getEventComment());

		String jobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);
		String jobState = "Canceled";

		// eventInfo.setEventName("Cancel");
		boolean bWork = true;

		FirstGlassJob firstGlassJobData = new FirstGlassJob();
		try
		{
			// 1.check for exist job
			firstGlassJobData = ExtendedObjectProxy.getFirstGlassJobService().selectByKey(false, new Object[] { jobName });
		}
		catch (Exception ex)
		{

		}

		if (firstGlassJobData == null)
		{
			bWork = false;
			throw new CustomException("FIRSTGLASS-0010");
		}

		if (!StringUtils.equals(firstGlassJobData.getJobState(), "Reserved"))
		{
			bWork = false;
			throw new CustomException("FIRSTGLASS-0011");
		}

		if (bWork)
		{
			// 2.FirstGlassJob
			Object bindSet[] = new Object[] { jobName };
			FirstGlassJob dataInfo = ExtendedObjectProxy.getFirstGlassJobService().selectByKey(false, bindSet);
			if (dataInfo != null)
			{
				dataInfo.setJobState(jobState);
				dataInfo.setLastEventTime(eventInfo.getEventTime());
				dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
				dataInfo.setLastEventUser(eventInfo.getEventUser());
				dataInfo.setLastEventComment(eventInfo.getEventComment());

				ExtendedObjectProxy.getFirstGlassJobService().modify(eventInfo, dataInfo);

				Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(dataInfo.getLotName());

				CommonValidation.checkJobDownFlag(lotData);
				Map<String, String> udfs = new HashMap<String, String>();
				udfs.put("JOBNAME", "");
				udfs.put("FIRSTGLASSALL", "");

				// Release Hold Mother Lot
				if (lotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().Lot_OnHold))
				{
					List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
					MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(lotData, productUSequence, udfs);
					LotServiceProxy.getLotService().makeNotOnHold(lotData.getKey(), eventInfo, makeNotOnHoldInfo);

					// delete in LOTMULTIHOLD table
					MESLotServiceProxy.getLotServiceUtil().releaseMultiHold(dataInfo.getLotName(), "FirstGlassHold", dataInfo.getProcessOperationName());

					// delete in PRODUCTMULTIHOLD table
					MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(dataInfo.getLotName(), "FirstGlassHold", dataInfo.getProcessOperationName());

					// setHoldState
					MESLotServiceProxy.getLotServiceUtil().setHoldState(eventInfo, lotData);
				}
				else
				{
					SetEventInfo setEventInfo = new SetEventInfo();
					setEventInfo.setUdfs(udfs);
					LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
					// Delete Future Hold Lot
					eventInfo.setEventName("Delete");
					MESLotServiceProxy.getLotServiceImpl().deleteCtLotFutureActionbyReasonCode(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), dataInfo.getProcessFlowName(),
							dataInfo.getProcessFlowVersion(), dataInfo.getProcessOperationName(), dataInfo.getProcessOperationVersion(), "0", "FirstGlassHold");
				}
			}
		}

		return doc;
	}
}
