package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.text.SimpleDateFormat;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class RenewBorrowPanel extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String taskID = SMessageUtil.getBodyItemValue(doc, "TASKID", true);

		List<Element> panelList = SMessageUtil.getBodySequenceItemList(doc, "PANELLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("RenewBorrowPanel", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		for (Element panel : panelList)
		{
			String lotName = SMessageUtil.getChildText(panel, "LOTNAME", true);

			Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);

			CommonValidation.checkLotProcessState(lotData);
			CommonValidation.checkLotStateScrapped(lotData);
			CommonValidation.checkLotHoldState(lotData);

			ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());

			String sCurrentTime = TimeStampUtil.getCurrentTime(TimeStampUtil.FORMAT_DEFAULT);
			String sPlanFinishiedTime = productRequestData.getPlanFinishedTime().toString();

			SimpleDateFormat sf = new SimpleDateFormat(TimeStampUtil.FORMAT_DEFAULT);

			long lCurrentTime = 0;
			long lFinishedTime = 0;

			try
			{
				lCurrentTime = sf.parse(sCurrentTime).getTime();
				lFinishedTime = sf.parse(sPlanFinishiedTime).getTime();
			}
			catch (Exception e)
			{

			}

			// Hour unit
			if ((lFinishedTime - lCurrentTime) / 3600000 < 48)
			{
				throw new CustomException("BORROW-0009");
			}

			ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());

			if (!StringUtils.equals(operationData.getDetailProcessOperationType(), "ScrapPack"))
				throw new CustomException("BORROW-0003", lotData.getKey().getLotName());

			ExtendedObjectProxy.getBorrowPanelService().increaseRenuwCount(eventInfo, taskID, lotName);
		}

		return doc;
	}

}
