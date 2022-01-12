package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.BankQueueTime;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class ChangeBank extends SyncHandler {
	Log log = LogFactory.getLog(ChangeBank.class);
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeBank", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		for (Element lot : lotList)
		{
			String lotName = SMessageUtil.getChildText(lot, "LOTNAME", true);
			
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
						
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("BANKTYPE", "OK");

			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
			
			BankQueueTime bankQueueTime = null;
			try
			{
				bankQueueTime = ExtendedObjectProxy.getBankQueueTimeService().selectByKey(false,
					new Object[] { lotData.getKey().getLotName() });
			}
			catch (greenFrameDBErrorSignal e)
			{
				log.info("bankQueueTime data information is null.");
			}

			if(bankQueueTime != null)
				ExtendedObjectProxy.getBankQueueTimeService().remove(eventInfo, bankQueueTime);
			
			createBankQueueTime(lotData, eventInfo);
		}

		return doc;
	}
	
	private void createBankQueueTime(Lot lotData,EventInfo eventInfo) throws CustomException
	{
		try 
		{
			StringBuffer sql=new StringBuffer();
			sql.append(" SELECT LOTNAME  ");
			sql.append(" FROM CT_BANKQUEUETIME ");
			sql.append(" WHERE LOTNAME=:LOTNAME ");
			
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("LOTNAME", lotData.getKey().getLotName());
			List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
			
			StringBuffer bankPolicy=new StringBuffer();
			bankPolicy.append(" SELECT A.FACTORYNAME,A.PRODUCTSPECNAME,A.PRODUCTSPECVERSION,B.TOFACTORYNAME,B.BANKTYPE,B.WARNINGDURATIONLIMIT,B.INTERLOCKDURATIONLIMIT  ");
			bankPolicy.append(" FROM  TPPOLICY A,POSBANKQUEUETIME B ");
			bankPolicy.append(" WHERE A.CONDITIONID=B.CONDITIONID ");
			bankPolicy.append(" AND A.FACTORYNAME=:FACTORYNAME ");
			bankPolicy.append(" AND A.PRODUCTSPECNAME=:PRODUCTSPECNAME ");
			bankPolicy.append(" AND A.PRODUCTSPECVERSION=:PRODUCTSPECVERSION ");
			bankPolicy.append(" AND B.TOFACTORYNAME=:TOFACTORYNAME ");
			bankPolicy.append(" AND B.BANKTYPE=:BANKTYPE ");
			
			
			Map<String, String> bindMap2 = new HashMap<String, String>();
			bindMap2.put("FACTORYNAME", lotData.getFactoryName());
			bindMap2.put("PRODUCTSPECNAME", lotData.getProductSpecName());
			bindMap2.put("PRODUCTSPECVERSION", lotData.getProductSpecVersion());
			bindMap2.put("TOFACTORYNAME", lotData.getDestinationFactoryName());
			bindMap2.put("BANKTYPE", lotData.getUdfs().get("BANKTYPE"));
			List<Map<String, Object>> bankPolicyResult = GenericServiceProxy.getSqlMesTemplate().queryForList(bankPolicy.toString(), bindMap2);
			
			if(result!=null && result.size()>0)
			
			{
				BankQueueTime bankQueueTime = ExtendedObjectProxy.getBankQueueTimeService().selectByKey(false,
						new Object[] { lotData.getKey().getLotName() });

				ExtendedObjectProxy.getBankQueueTimeService().remove(eventInfo, bankQueueTime);
			}
			if (bankPolicyResult != null && bankPolicyResult.size() > 0) 
			{
				BankQueueTime bankInfo = new BankQueueTime();
				bankInfo.setLotName(lotData.getKey().getLotName());
				bankInfo.setBankType(lotData.getUdfs().get("BANKTYPE"));
				bankInfo.setFactoryName(lotData.getFactoryName());
				bankInfo.setProductSpecName(lotData.getProductSpecName());
				bankInfo.setProductSpecVersion(lotData.getProductSpecVersion());
				bankInfo.setProcessFlowName(lotData.getProcessFlowName());
				bankInfo.setProcessFlowVersion(lotData.getProcessFlowVersion());
				bankInfo.setProcessOperationName(lotData.getProcessOperationName());
				bankInfo.setProcessOperationVersion(lotData.getProcessOperationVersion());
				bankInfo.setToFactoryName(lotData.getDestinationFactoryName());

				bankInfo.setQueueTimeState("Entered");
				bankInfo.setEnterTime(eventInfo.getEventTime());
				bankInfo.setExitTime(null);
				bankInfo.setWarningTime(null);
				bankInfo.setInterlockTime(null);
				bankInfo.setResolveTime(null);
				bankInfo.setResolveUser("");
				bankInfo.setWarningDurationLimit(bankPolicyResult.get(0).get("WARNINGDURATIONLIMIT").toString());
				bankInfo.setInterlockDurationLimit(bankPolicyResult.get(0).get("INTERLOCKDURATIONLIMIT").toString());

				bankInfo.setAlarmType("BankQTimeOver");

				bankInfo.setLastEventName(eventInfo.getEventName());
				bankInfo.setLastEventUser(eventInfo.getEventUser());
				bankInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

				ExtendedObjectProxy.getBankQueueTimeService().create(eventInfo, bankInfo);
			}
		}
		catch (Exception e ) 
		{
			throw new CustomException("BANK-0002");
		}
	}
}
