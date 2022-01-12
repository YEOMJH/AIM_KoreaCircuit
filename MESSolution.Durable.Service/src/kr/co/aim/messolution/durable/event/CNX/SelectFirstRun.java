package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.durable.service.DurableServiceUtil;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;

public class SelectFirstRun extends SyncHandler {

	private static Log log = LogFactory.getLog(SelectFirstRun.class);

	public Object doWorks(Document doc) throws CustomException
	{

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("SelectFirstRun", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String firstRunQty = SMessageUtil.getBodyItemValue(doc, "FIRSTRUNQTY", false);
		String sDurableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);

		log.debug("machineName : " + machineName);
		log.debug("firstRunQty : " + firstRunQty);
		log.debug("sDurableName : " + sDurableName);

		// Check exist Durable
		Durable durable = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);

		if (!StringUtil.equals(durable.getDurableState(), GenericServiceProxy.getConstantMap().Dur_InUse))
			throw new CustomException("MASK-0041");

		CommonValidation.CheckDurableHoldState(durable);
		CommonValidation.CheckDurableCleanState(durable);

		List<Element> elPANELLIST = SMessageUtil.getBodySequenceItemList(doc, "PANELLIST", true);

		List<Object[]> updateLotArgList = new ArrayList<Object[]>();

		for (Element elPANEL : elPANELLIST)
		{
			String trayGroupName = SMessageUtil.getChildText(elPANEL, "TRAYGROUPNAME", true);

			String lotName = SMessageUtil.getChildText(elPANEL, "LOTNAME", true);
			String productSpecName = SMessageUtil.getChildText(elPANEL, "PRODUCTSPECNAME", true);
			String productSpecVersion = SMessageUtil.getChildText(elPANEL, "PRODUCTSPECVERSION", true);
			String processFlowName = SMessageUtil.getChildText(elPANEL, "PROCESSFLOWNAME", true);
			String processFlowVersion = SMessageUtil.getChildText(elPANEL, "PROCESSFLOWVERSION", true);
			String processOperationName = SMessageUtil.getChildText(elPANEL, "PROCESSOPERATIONNAME", true);
			String processOperationVersion = SMessageUtil.getChildText(elPANEL, "PROCESSOPERATIONVERSION", true);

			log.debug("trayGroupName 			: " + trayGroupName);
			log.debug("lotName 					: " + lotName);
			log.debug("productSpecName 			: " + productSpecName);
			log.debug("productSpecVersion 		: " + productSpecVersion);
			log.debug("processFlowName 			: " + processFlowName);
			log.debug("processFlowVersion 		: " + processFlowVersion);
			log.debug("processOperationName  	: " + processOperationName);
			log.debug("processOperationVersion 	: " + processOperationVersion);

			// Check exist Lot
			Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);

			CommonValidation.checkLotHoldState(lotData);
			CommonValidation.checkLotProcessState(lotData);

			// save data
			List<Object> lotBindList = new ArrayList<Object>();

			lotBindList.add(lotData.getKey().getLotName());
			lotBindList.add(eventInfo.getEventTimeKey());
			lotBindList.add(lotData.getFactoryName());
			lotBindList.add(lotData.getProductSpecName());
			lotBindList.add(lotData.getProductSpecVersion());
			lotBindList.add(lotData.getProcessFlowName());
			lotBindList.add(lotData.getProcessFlowVersion());
			lotBindList.add(lotData.getProcessOperationName());
			lotBindList.add(lotData.getProcessOperationVersion());
			lotBindList.add(machineName);
			lotBindList.add(trayGroupName);

			updateLotArgList.add(lotBindList.toArray());

		}

		StringBuffer strQuery = new StringBuffer();
		strQuery.append("Insert into CT_FIRSTRUN  ");
		strQuery.append("(LOTNAME, TIMEKEY, FACTORYNAME, PRODUCTSPECNAME, PRODUCTSPECVERSION,  ");
		strQuery.append(" PROCESSFLOWNAME, PROCESSFLOWVERSION,PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION, MACHINENAME,  ");
		strQuery.append(" TRAYGROUPNAME )  ");
		strQuery.append("Values  ");
		strQuery.append("( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)  ");

		// Insert in CT_FIRSTRUN
		MESLotServiceProxy.getLotServiceUtil().updateBatch(strQuery.toString(), updateLotArgList);

		return doc;
	}

}
