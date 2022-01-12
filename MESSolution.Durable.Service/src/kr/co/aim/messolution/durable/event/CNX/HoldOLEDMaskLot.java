package kr.co.aim.messolution.durable.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.MaskMultiHold;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class HoldOLEDMaskLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", true);
		String reasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", true);
		List<Element> MASKLIST = SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", true);

		for (Element mask : MASKLIST)
		{
			String maskLotName = SMessageUtil.getChildText(mask, "MASKLOTNAME", true);
			
			checkMaskMultiHold(maskLotName,reasonCode);

			List<Map<String, Object>> sqlResult = getMaskLot(maskLotName);

			if (sqlResult.size() > 0)
			{
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("HoldMask", this.getEventUser(), this.getEventComment(), "", "");
				eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());
				
				MaskLot dataInfo = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskLotName });
				if(dataInfo.getJobDownFlag().equals("Y"))
				{
					throw new CustomException("MASK-0078",maskLotName);
				}
				dataInfo.setMaskLotName(maskLotName);
				dataInfo.setMaskLotHoldState(constantMap.MaskLotHoldState_OnHold);
				dataInfo.setReasonCode(reasonCode);
				dataInfo.setReasonCodeType(reasonCodeType);
				dataInfo.setLastEventComment(eventInfo.getEventComment());
				dataInfo.setLastEventName(eventInfo.getEventName());
				dataInfo.setLastEventTime(eventInfo.getEventTime());
				dataInfo.setLastEventTimeKey(eventInfo.getLastEventTimekey());
				dataInfo.setLastEventUser(eventInfo.getEventUser());

				ExtendedObjectProxy.getMaskLotService().modify(eventInfo, dataInfo);
				
				MaskMultiHold maskMultiHold =new MaskMultiHold();
				maskMultiHold.setMaskLotName(maskLotName);
				maskMultiHold.setFactoryName(dataInfo.getFactoryName());
				maskMultiHold.setMaskProcessOperationName(dataInfo.getMaskProcessOperationName());
				maskMultiHold.setMaskProcessOperationVersion(dataInfo.getMaskProcessOperationVersion());
				maskMultiHold.setReasonCode(reasonCode);
				maskMultiHold.setReasonCodeType(reasonCodeType);
				maskMultiHold.setLastEventComment(eventInfo.getEventComment());
				maskMultiHold.setLastEventName(eventInfo.getEventName());
				maskMultiHold.setLastEventTime(eventInfo.getEventTime());
				maskMultiHold.setLastEventUser(eventInfo.getEventUser());
				
				ExtendedObjectProxy.getMaskMultiHoldService().create(eventInfo, maskMultiHold);
			}
			else
			{
				throw new CustomException("MASK-0077",maskLotName);
			}
		}

		return doc;
	}

	private List<Map<String, Object>> getMaskLot(String maskLotName)
	{
		StringBuffer inquirysql = new StringBuffer();
		inquirysql.append("SELECT MASKLOTHOLDSTATE ");
		inquirysql.append("  FROM CT_MASKLOT ");
		inquirysql.append("  WHERE MASKLOTPROCESSSTATE = 'WAIT' ");
		inquirysql.append("  AND MASKLOTSTATE = 'Released' ");
		inquirysql.append("  AND MASKLOTNAME = :MASKLOTNAME ");

		Map<String, String> inquirybindMap = new HashMap<String, String>();
		inquirybindMap.put("MASKLOTNAME", maskLotName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(inquirysql.toString(), inquirybindMap);

		return sqlResult;
	}
	
	private void checkMaskMultiHold(String masklotname,String reasonCode) throws CustomException
	{
		StringBuffer inquirysql = new StringBuffer();
		inquirysql.append(" SELECT MASKLOTNAME ");
		inquirysql.append(" FROM CT_MASKMULTIHOLD ");
		inquirysql.append(" WHERE MASKLOTNAME=:MASKLOTNAME ");
		inquirysql.append(" AND REASONCODE=:REASONCODE ");
		
		Map<String, String> inquirybindMap = new HashMap<String, String>();
		inquirybindMap.put("MASKLOTNAME", masklotname);
		inquirybindMap.put("REASONCODE", reasonCode);
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(inquirysql.toString(), inquirybindMap);
		if(sqlResult!=null&& sqlResult.size()>0)
		{
			throw new CustomException("MASK-0082",masklotname,reasonCode);
		}
	}

}
