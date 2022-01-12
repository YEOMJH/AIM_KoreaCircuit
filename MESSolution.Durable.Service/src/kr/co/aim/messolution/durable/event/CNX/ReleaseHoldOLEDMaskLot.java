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
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;


import org.jdom.Document;
import org.jdom.Element;

public class ReleaseHoldOLEDMaskLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		List<Element> MASKLIST = SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", true);
		String transportLockFlag = SMessageUtil.getBodyItemValue(doc, "TRANSPORTLOCKFLAG", false);
		
		for (Element mask : MASKLIST)
		{
			String maskLotName = SMessageUtil.getChildText(mask, "MASKLOTNAME", true);
			String reasonCode = SMessageUtil.getChildText(mask, "REASONCODE", true);
			
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReleaseHoldMask", this.getEventUser(), this.getEventComment(), "", "");
			eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());
			
			checkMaskHoldReasonCode(maskLotName, reasonCode);
			
			MaskLot dataInfo = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskLotName });
			try
			{
			MaskMultiHold maskMultiHold =ExtendedObjectProxy.getMaskMultiHoldService().selectByKey(false, new Object[] { maskLotName,dataInfo.getFactoryName(),reasonCode });
			ExtendedObjectProxy.getMaskMultiHoldService().remove(eventInfo, maskMultiHold);
			}
			catch(greenFrameDBErrorSignal x)
			{
				throw new CustomException("MASK-0100",maskLotName,reasonCode);//houxk 20200416
			}
					
			if (dataInfo.getMaskLotHoldState().equals("Y"))
			{
				if(checkMaskMultiHold(maskLotName))
				{		
					dataInfo.setLastEventComment(eventInfo.getEventComment());
					dataInfo.setLastEventName(eventInfo.getEventName());
					dataInfo.setLastEventTime(eventInfo.getEventTime());
					dataInfo.setLastEventTimeKey(eventInfo.getLastEventTimekey());
					dataInfo.setLastEventUser(eventInfo.getEventUser());
					dataInfo.setTransportLockFlag(transportLockFlag);
					ExtendedObjectProxy.getMaskLotService().modify(eventInfo, dataInfo);
					continue;
				}
				else
				{				
				    dataInfo.setMaskLotName(maskLotName);
				    dataInfo.setMaskLotHoldState(constantMap.MaskLotHoldState_NotOnHold);
				    dataInfo.setReasonCode("");
				    dataInfo.setReasonCodeType("");
				    dataInfo.setLastEventComment(eventInfo.getEventComment());
				    dataInfo.setLastEventName(eventInfo.getEventName());
				    dataInfo.setLastEventTime(eventInfo.getEventTime());
				    dataInfo.setLastEventTimeKey(eventInfo.getLastEventTimekey());
				    dataInfo.setLastEventUser(eventInfo.getEventUser());
				    dataInfo.setTransportLockFlag(transportLockFlag);
				    ExtendedObjectProxy.getMaskLotService().modify(eventInfo, dataInfo);
				}
			}
			else
			{
				throw new CustomException("MASKLOTRH-0001", maskLotName);
			}
		}

		return doc;
	}
	
	private void checkMaskHoldReasonCode(String maskLotName,String reasonCode) throws CustomException
	{
		StringBuffer inquirysql = new StringBuffer();
		inquirysql.append(" SELECT MASKLOTNAME ");
		inquirysql.append(" FROM CT_MASKMULTIHOLD ");
		inquirysql.append(" WHERE MASKLOTNAME=:MASKLOTNAME ");
		inquirysql.append(" AND REASONCODE=:REASONCODE ");
		
		Map<String, String> inquirybindMap = new HashMap<String, String>();
		inquirybindMap.put("MASKLOTNAME", maskLotName);
		inquirybindMap.put("REASONCODE", reasonCode);
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(inquirysql.toString(), inquirybindMap);
		if(sqlResult==null&& sqlResult.size()<1)
		{
			throw new CustomException("MASK-0083",maskLotName,reasonCode);
	    }
	}
	
	private boolean checkMaskMultiHold (String maskLotName) throws CustomException
	{
		StringBuffer inquirysql = new StringBuffer();
		inquirysql.append(" SELECT MASKLOTNAME ");
		inquirysql.append(" FROM CT_MASKMULTIHOLD ");
		inquirysql.append(" WHERE MASKLOTNAME=:MASKLOTNAME ");
		
		Map<String, String> inquirybindMap = new HashMap<String, String>();
		inquirybindMap.put("MASKLOTNAME", maskLotName);
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(inquirysql.toString(), inquirybindMap);
		if(sqlResult!=null&& sqlResult.size()>0)
		{
			return true;
	    }
		else
		{
			return false;
		}
	}
	
}
