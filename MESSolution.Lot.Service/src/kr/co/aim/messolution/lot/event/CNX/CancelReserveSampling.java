package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SampleLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.lot.service.LotServiceImpl;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class CancelReserveSampling extends SyncHandler {
	private static Log log = LogFactory.getLog(LotServiceImpl.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		// for common
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelReserveSample", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", false);
		String processOperationVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", false);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String toProcessFlowName = SMessageUtil.getBodyItemValue(doc, "TOPROCESSFLOWNAME", true);
		String toProcessFlowVersion = SMessageUtil.getBodyItemValue(doc, "TOPROCESSFLOWVERSION", true);

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		if (StringUtils.equals(lotData.getProcessFlowName(), toProcessFlowName) && StringUtils.equals(lotData.getProcessFlowVersion(), toProcessFlowVersion))
			throw new CustomException("LOT-9022", toProcessFlowName, toProcessFlowVersion);

		// get SampleLotData(CT_SAMPLELOT)
		List<SampleLot> sampleLotList = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListByToFlow(lotName, factoryName, productSpecName, productSpecVersion, processFlowName,
				processFlowVersion, processOperationName, processOperationVersion, toProcessFlowName, toProcessFlowVersion);

		// MantisID : 0000022
		// 在前台展示的预约信息，已经被其他人取消掉后。点击OK时，后台没有与DB比较，该预约信息是否存在。仍然操作成功。或者前台增加并发的卡关
		if (sampleLotList == null || sampleLotList.size() == 0)
		{
			// ReserveSampling information already deleted.
			throw new CustomException("SAMPLE-0002", toProcessFlowName, toProcessFlowVersion);
		}

		for (SampleLot sampleLot : sampleLotList)
		{
			String toProcessOperationName = sampleLot.getToProcessOperationName();
			String toProcessOperationVersion = sampleLot.getToProcessOperationVersion();

			// Delete SampleLotData
			ExtendedObjectProxy.getSampleLotService().deleteReserveSampleLotDataByToInfo(eventInfo, lotName, factoryName, productSpecName, productSpecVersion, toProcessFlowName, toProcessFlowVersion,
					toProcessOperationName, toProcessOperationVersion);

			ExtendedObjectProxy.getSampleProductService().deleteReserveSampleProductByLotName(eventInfo, lotName, factoryName, productSpecName, productSpecVersion, processFlowName,
					processFlowVersion, processOperationName, processOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);

			log.info("Success CancelReserveSampling ! LotName = (" + lotName + "), ToProcessOperationName = (" + toProcessOperationName + ") .");
		}

		return doc;
	}
}
