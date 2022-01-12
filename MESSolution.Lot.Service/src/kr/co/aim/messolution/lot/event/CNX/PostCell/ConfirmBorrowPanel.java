package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.BorrowPanel;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;

public class ConfirmBorrowPanel extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String taskID = SMessageUtil.getBodyItemValue(doc, "TASKID", true);
		String taskState = SMessageUtil.getBodyItemValue(doc, "TASKSTATE", true);

		List<Element> panelList = SMessageUtil.getBodySequenceItemList(doc, "PANELLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ConfirmeBorrowPanel", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		for (Element panel : panelList)
		{
			String lotName = SMessageUtil.getChildText(panel, "LOTNAME", true);

			Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);

			CommonValidation.checkLotProcessState(lotData);
			//CommonValidation.checkLotStateScrapped(lotData);
			CommonValidation.checkLotHoldState(lotData);

			ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());

			if (!StringUtils.equals(operationData.getDetailProcessOperationType(), "ScrapPack"))
				throw new CustomException("BORROW-0003", lotData.getKey().getLotName());

			BorrowPanel dataInfo = ExtendedObjectProxy.getBorrowPanelService().getBorrowPanelData(taskID, lotName);

			if (dataInfo == null)
				throw new CustomException("BORROW-0001", taskID, lotName);

			if (!StringUtils.equals(dataInfo.getBorrowState(), taskState))
				throw new CustomException("BORROW-0002", taskState);

			if (StringUtils.equals(taskState, "Confirmed") && !StringUtils.equals(dataInfo.getPanelOutFlag(), "Y"))
				throw new CustomException("BORROW-0005", dataInfo.getTaskId(), dataInfo.getLotName());
			
			String state = "";
			
			if(StringUtils.equals(dataInfo.getBorrowState(), constantMap.Borrow_Created))
				state = constantMap.Borrow_Confirmed;
			else if (StringUtils.equals(dataInfo.getBorrowState(), constantMap.Borrow_Confirmed))
				state = constantMap.Borrow_Borrowed;
			
			ExtendedObjectProxy.getBorrowPanelService().changeState(eventInfo, dataInfo, state);
		}

		return doc;
	}

}
