package kr.co.aim.messolution.lot.event.Tray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SVIPanelJudge;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;

public class SVIPanelJudgeReport  extends AsyncHandler{
	private static Log log = LogFactory.getLog(SVIPanelJudge.class);
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String panelName = SMessageUtil.getBodyItemValue(doc, "PANELNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String panelJudge = SMessageUtil.getBodyItemValue(doc, "PANELJUDGE", true);
		String panelGrade = SMessageUtil.getBodyItemValue(doc, "PANELGRADE", true);//S1/A/C0-C5/S
		String judgeUser = SMessageUtil.getBodyItemValue(doc, "EVENTUSER", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("SVIPanelJudge", judgeUser, getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());		
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(panelName);
		Lot oldLot = (Lot) ObjectUtil.copyTo(lotData);
		//*********************************CheckLotInfo*************************************************
		//Run &Operation &Hold &Released 
		CommonValidation.checkLotProcessStateRun(lotData);
		CommonValidation.checkLotHoldState(lotData);
		CommonValidation.checkLotState(lotData);
		ProcessOperationSpec processOpSpec = MESLotServiceProxy.getLotServiceUtil().getProcessOperationSpecData(lotData);
		String detailOperationType = processOpSpec.getDetailProcessOperationType();
		if(!detailOperationType.equals("SVI/MVI"))
		{
			throw new CustomException("SVI-0001", lotData.getKey().getLotName()); 
		}
		//**************************************SVIPanelJudge*******************************************
		String beforeJudge = lotData.getLotGrade();
		String beforeGrade = "";

		if (beforeJudge.equals("S"))
			beforeGrade = "S";
		else
			beforeGrade = lotData.getUdfs().get("LOTDETAILGRADE");
		
		String seq = ExtendedObjectProxy.getSVIPanelJudgeService().getSVILotSeq(panelName);	
		SVIPanelJudge sviPanelJudge = new SVIPanelJudge();
		sviPanelJudge.setPanelName(panelName);
		sviPanelJudge.setSeq(Long.parseLong(seq));
		sviPanelJudge.setBeforeGrade(beforeGrade);
		sviPanelJudge.setBeforeJudge(beforeJudge);//lotgrade
		sviPanelJudge.setSVIPanelGrade(panelGrade);
		sviPanelJudge.setSVIPanelJudge(panelJudge);
		sviPanelJudge.setEventUser(judgeUser);
		sviPanelJudge.setEventTime(eventInfo.getEventTime());
		sviPanelJudge.setEventName(eventInfo.getEventName());
		sviPanelJudge.setLastEventTimeKey(eventInfo.getEventTimeKey());
		sviPanelJudge.setMachineName(machineName);
		ExtendedObjectProxy.getSVIPanelJudgeService().create(eventInfo, sviPanelJudge);
		String lotGrade = ""; ;
		String lotDetailGrade = "";

		//**************************************LOT&LOTHISTORY*******************************************
		if (panelGrade.equals("S") || panelGrade.equals("N"))
		{
			lotGrade = "S";
			lotDetailGrade = "";
		}
		else if(panelGrade.equals("S1"))//caixu 2020/12/10 ADD S1
		{
			lotGrade = "S";
			lotDetailGrade =panelGrade;
		}
		else
		{
			lotGrade = "G";
			lotDetailGrade = panelGrade;
		}
		lotData.setLotGrade(lotGrade);
		lotData.setLastEventName(eventInfo.getEventName());
		lotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		lotData.setLastEventTime(eventInfo.getEventTime());
		lotData.setLastEventUser(eventInfo.getEventUser());
		lotData.setLastEventComment(eventInfo.getEventComment());
		Map<String, String> lotUdf = new HashMap<>();
		lotUdf = lotData.getUdfs();
		lotUdf.put("LOTDETAILGRADE", lotDetailGrade);
		LotHistory lotHist = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lotData, new LotHistory());
		LotServiceProxy.getLotService().update(lotData);
		LotServiceProxy.getLotHistoryService().insert(lotHist);
			
	}
}
