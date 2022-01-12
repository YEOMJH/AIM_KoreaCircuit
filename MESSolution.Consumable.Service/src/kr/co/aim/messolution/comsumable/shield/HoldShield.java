package kr.co.aim.messolution.comsumable.shield;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ShieldLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class HoldShield extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String reasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", true);
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", true);

		List<Element> shieldList = SMessageUtil.getBodySequenceItemList(doc, "SHIELDLIST", true);

		List<Object[]> updateArgsList = new ArrayList<Object[]>();
		List<Object[]> insertHistArgsList = new ArrayList<Object[]>();

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("HoldShield", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());

		for (Element shield : shieldList)
		{
			String shieldLotName = SMessageUtil.getChildText(shield, "SHIELDLOTNAME", true);
			ShieldLot shieldLotData = ExtendedObjectProxy.getShieldLotService().selectByKey(false, new Object[] { shieldLotName });

			CommonValidation.CheckShieldHoldState(shieldLotData);

			List<Object> updateBindList = new ArrayList<Object>();
			updateBindList.add("Y");
			updateBindList.add(reasonCodeType);
			updateBindList.add(reasonCode);
			updateBindList.add(eventInfo.getEventComment());
			updateBindList.add(eventInfo.getEventUser());
			updateBindList.add(eventInfo.getEventName());
			updateBindList.add(eventInfo.getEventTime());
			updateBindList.add(eventInfo.getEventTimeKey());
			updateBindList.add(shieldLotName);

			updateArgsList.add(updateBindList.toArray());

			List<Object> histBindList = new ArrayList<Object>();
			histBindList.add(shieldLotData.getShieldLotName());
			histBindList.add(eventInfo.getEventTimeKey());
			histBindList.add(shieldLotData.getLine());
			histBindList.add(shieldLotData.getChamberType());
			histBindList.add(shieldLotData.getChamberNo());
			histBindList.add(shieldLotData.getSetValue());
			histBindList.add(shieldLotData.getJudge());
			histBindList.add(shieldLotData.getFactoryName());
			histBindList.add(shieldLotData.getLotState());
			histBindList.add(shieldLotData.getLotProcessState());
			histBindList.add("Y");
			histBindList.add(shieldLotData.getNodeStack());
			histBindList.add(shieldLotData.getCleanState());
			histBindList.add(shieldLotData.getCarrierName());
			histBindList.add(shieldLotData.getMachineName());
			histBindList.add(shieldLotData.getChamberName());
			histBindList.add(shieldLotData.getShieldSpecName());
			histBindList.add(shieldLotData.getProcessFlowName());
			histBindList.add(shieldLotData.getProcessFlowVersion());
			histBindList.add(shieldLotData.getProcessOperationName());
			histBindList.add(shieldLotData.getProcessOperationVersion());
			histBindList.add(reasonCodeType);
			histBindList.add(reasonCode);
			histBindList.add(shieldLotData.getReworkState());
			histBindList.add(shieldLotData.getReworkCount());
			histBindList.add(shieldLotData.getLastLoggedInTime());
			histBindList.add(shieldLotData.getLastLoggedInUser());
			histBindList.add(shieldLotData.getLastLoggedOutTime());
			histBindList.add(shieldLotData.getLastLoggedOutUser());
			histBindList.add(shieldLotData.getSampleFlag());
			histBindList.add(shieldLotData.getCreateTime());
			histBindList.add(eventInfo.getEventComment());
			histBindList.add(eventInfo.getEventName());
			histBindList.add(eventInfo.getEventUser());
			histBindList.add(eventInfo.getEventTime());
			histBindList.add(shieldLotData.getCarGroupName());
			histBindList.add(shieldLotData.getBasketGroupName());

			insertHistArgsList.add(histBindList.toArray());
		}

		updateShieldLot(updateArgsList);
		ExtendedObjectProxy.getShieldLotService().insertHistory(insertHistArgsList);
		
		return doc;
	}
	
	private void updateShieldLot(List<Object[]> updateArgsList) throws CustomException
	{		
		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE CT_SHIELDLOT  ");
		sql.append(" SET LOTHOLDSTATE = ? , REASONCODETYPE = ? , REASONCODE = ? , LASTEVENTCOMMENT = ? , LASTEVENTNAME = ? , LASTEVENTUSER = ? , LASTEVENTTIME = ? , LASTEVENTTIMEKEY = ? ");
		sql.append(" WHERE SHIELDLOTNAME = ? ");

		MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), updateArgsList);
	}

}
