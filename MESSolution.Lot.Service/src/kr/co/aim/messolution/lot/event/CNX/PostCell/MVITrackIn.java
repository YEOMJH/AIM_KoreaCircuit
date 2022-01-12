package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MVIPanelJudge;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.master.EnumDef;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class MVITrackIn extends SyncHandler {

	private static Log log = LogFactory.getLog(MVITrackIn.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String panelName = SMessageUtil.getBodyItemValue(doc, "PANELNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", false);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", false);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackIn", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		Lot lot = MESLotServiceProxy.getLotInfoUtil().getLotData(panelName);
		Lot oldLot = (Lot) ObjectUtil.copyTo(lot);
		
		//Check ERPBOM	
		ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lot.getProductRequestName());
		
		if(StringUtil.isNotEmpty(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
		{
			MESConsumableServiceProxy.getConsumableServiceUtil().compareERPBOM(lot.getFactoryName(), productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME"), processOperationName, "00001", machineName, productSpecName);
		}

		//Validation
		CommonValidation.checkLotProcessState(lot);
		CommonValidation.checkLotHoldState(lot);
		CommonValidation.checkLotState(lot);
		CommonValidation.checkLotCarriernameNull(lot);
		if(!StringUtils.equals(processOperationName, lot.getProcessOperationName()))
		{
			throw new CustomException("LOT-1000"); 
		}
		
		String detailOperationType = "SVI/MVI";
		
		Map<String,String> processLimitEnum = ExtendedObjectProxy.getPanelProcessCountService().getProcessLimitConfiguration("ProcessLimit");
		
		if (processLimitEnum != null && StringUtil.in(detailOperationType, processLimitEnum.keySet().toArray(new String[]{})));
		{
			ExtendedObjectProxy.getPanelProcessCountService().checkPanelProcessCount(panelName, detailOperationType);
		}

		// Set Event
		lot.setLotState(GenericServiceProxy.getConstantMap().Lot_Released);
		lot.setLotProcessState(GenericServiceProxy.getConstantMap().Lot_LoggedIn);
		lot.setLastLoggedInTime(eventInfo.getEventTime());
		lot.setLastLoggedInUser(eventInfo.getEventUser());
		lot.setProcessOperationName(lot.getProcessOperationName());
		lot.setProcessOperationVersion(lot.getProcessOperationVersion());
		lot.setMachineName(machineName);

		SetEventInfo setEventInfo = new SetEventInfo();

		LotServiceProxy.getLotService().update(lot);
		LotServiceProxy.getLotService().setEvent(lot.getKey(), eventInfo, setEventInfo);

		
		//Insert MVIData
		List<MVIPanelJudge> mviPanelList = new ArrayList<>();
		
		if(detailOperationType.equals("SVI/MVI") || detailOperationType.equals("MCT"))
		{
			MVIPanelJudge mviData = new MVIPanelJudge();
			
			String seq = ExtendedObjectProxy.getMVIPanelJudgeService().getMVIPanelSeqV2(oldLot.getKey().getLotName());
			
			mviData.setPanelName(oldLot.getKey().getLotName());
			mviData.setSeq(Long.parseLong(seq));
			if(oldLot.getLotGrade().equals("G"))
			{
				mviData.setBeforeGrade(oldLot.getUdfs().get("LOTDETAILGRADE").toString());
			}
			else
			{
				mviData.setBeforeGrade(oldLot.getLotGrade());
			}
			mviData.setEventUser(eventInfo.getEventUser());
			mviData.setEventTime(eventInfo.getEventTime());
			mviData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			mviData.setLastLoggedInTime(eventInfo.getEventTime());
			mviData.setLastLoggedOutTime(null);
			
			mviPanelList.add(mviData);
		}
		
		if(mviPanelList.size() > 0)
		{
			ExtendedObjectProxy.getMVIPanelJudgeService().create(eventInfo, mviPanelList);
		}

		return doc;
	}
}
