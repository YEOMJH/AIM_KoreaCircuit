package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.BorrowPanel;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class CancelBorrowPanel extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String taskID = SMessageUtil.getBodyItemValue(doc, "TASKID", true);
		String taskFlag = SMessageUtil.getBodyItemValue(doc, "TASKFLAG", true);
		List<Element> panelList = SMessageUtil.getBodySequenceItemList(doc, "PANELLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelBorrowPanel", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		if(taskFlag.equals("Y"))
		{

			StringBuilder dataSql = new StringBuilder();
			dataSql.append(" SELECT LOTNAME,BORROWSTATE FROM CT_BORROWPANEL WHERE TASKID = :TASKID ");
			Map<String, String> databindMap = new HashMap<String, String>();
			databindMap.put("TASKID", taskID);
			List<Map<String, Object>> dataSqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(dataSql.toString(), databindMap);
			if(dataSqlResult.size()>0)
			{
				for (int j = 0; dataSqlResult.size() > j; j++)
				{
					String lotName = ConvertUtil.getMapValueByName(dataSqlResult.get(j), "LOTNAME");
					String borrowPanel = ConvertUtil.getMapValueByName(dataSqlResult.get(j), "BORROWSTATE");
					if(StringUtils.equals(borrowPanel, constantMap.Borrow_Created))
					{
						
						ExtendedObjectProxy.getBorrowPanelService().deleteBorrowPanel(eventInfo, taskID, lotName, constantMap.Borrow_Created);
						
					}
					if(StringUtils.equals(borrowPanel, constantMap.Borrow_Confirmed))
					{
						
						ExtendedObjectProxy.getBorrowPanelService().deleteBorrowPanel(eventInfo, taskID, lotName, constantMap.Borrow_Confirmed);
					}
					
				}
			}	
		}
		else
		{
			for (Element panel : panelList)
			{
				String lotName = SMessageUtil.getChildText(panel, "LOTNAME", true);
				
				Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
	
				//CommonValidation.checkLotProcessState(lotData);
				//CommonValidation.checkLotStateScrapped(lotData);
				//CommonValidation.checkLotHoldState(lotData);
	
				ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());
	
				BorrowPanel dataInfo = ExtendedObjectProxy.getBorrowPanelService().getBorrowPanelData(taskID, lotName);
				if(StringUtils.equals(dataInfo.getBorrowState(), constantMap.Borrow_Created))
				{
					
					ExtendedObjectProxy.getBorrowPanelService().deleteBorrowPanel(eventInfo, taskID, lotName, constantMap.Borrow_Created);
					
				}
				if(StringUtils.equals(dataInfo.getBorrowState(), constantMap.Borrow_Confirmed))
				{
					
					ExtendedObjectProxy.getBorrowPanelService().deleteBorrowPanel(eventInfo, taskID, lotName, constantMap.Borrow_Confirmed);
				}
	
				
			}
		}
		return doc;
	}

}
