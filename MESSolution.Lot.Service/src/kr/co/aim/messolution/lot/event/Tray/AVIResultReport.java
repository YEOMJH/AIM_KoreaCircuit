package kr.co.aim.messolution.lot.event.Tray;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AVIPanelJudge;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;

public class AVIResultReport extends AsyncHandler {

	private static Log log = LogFactory.getLog(AVIResultReport.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String panelName = SMessageUtil.getBodyItemValue(doc, "PANELNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String panelJudge = SMessageUtil.getBodyItemValue(doc, "PANELJUDGE", false);
		String panelGrade = SMessageUtil.getBodyItemValue(doc, "PANELGRADE", false);
		String gamJudge = SMessageUtil.getBodyItemValue(doc, "GAMJUDGE", false);
		String aoiJudge = SMessageUtil.getBodyItemValue(doc, "AOIJUDGE", false);
		String murJudge = SMessageUtil.getBodyItemValue(doc, "MURJUDGE", false);
		String tpJudge = SMessageUtil.getBodyItemValue(doc, "TPJUDGE", false);
		String manJudge = SMessageUtil.getBodyItemValue(doc, "MANJUDGE", false);
		String appJudge = SMessageUtil.getBodyItemValue(doc, "APPJUDGE", false);
		String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);

		// EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("AVIResultReport", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(panelName);

		if (!ExtendedObjectProxy.getAVIPanelJudgeService().isExist(panelName))
		{
			// Insert into CT_AVIPANELJUDGE
			AVIPanelJudge aviPanelJudge = new AVIPanelJudge();

			aviPanelJudge.setPanelName(panelName);
			aviPanelJudge.setProductSpecName(lotData.getProductSpecName());
			aviPanelJudge.setProcessFlowName(lotData.getProcessFlowName());
			aviPanelJudge.setProcessOperationName(lotData.getProcessOperationName());
			aviPanelJudge.setMachineName(machineName);
			aviPanelJudge.setUnitName(unitName);
			aviPanelJudge.setMachineRecipeName(machineRecipeName);
			aviPanelJudge.setPanelJudge(panelJudge);
			aviPanelJudge.setPanelGrade(panelGrade);
			aviPanelJudge.setGamJudge(gamJudge);
			aviPanelJudge.setAoiJudge(aoiJudge);
			aviPanelJudge.setMurJudge(murJudge);
			aviPanelJudge.setTpJudge(tpJudge);
			aviPanelJudge.setManJudge(manJudge);
			aviPanelJudge.setAppJudge(appJudge);
			aviPanelJudge.setLastEventName(eventInfo.getEventName());
			aviPanelJudge.setLastEventUser(eventInfo.getEventUser());
			aviPanelJudge.setLastEventTime(eventInfo.getEventTime());
			aviPanelJudge.setLastEventComment(eventInfo.getEventComment());
			aviPanelJudge.setLastEventTimekey(eventInfo.getEventTimeKey());
			
			ExtendedObjectProxy.getAVIPanelJudgeService().create(eventInfo, aviPanelJudge);
		}
		else
		{
			AVIPanelJudge aviPanelJudge = ExtendedObjectProxy.getAVIPanelJudgeService().selectByKey(false, new Object[] { panelName });

			aviPanelJudge.setProductSpecName(lotData.getProductSpecName());
			aviPanelJudge.setProcessFlowName(lotData.getProcessFlowName());
			aviPanelJudge.setProcessOperationName(lotData.getProcessOperationName());
			aviPanelJudge.setMachineName(machineName);
			aviPanelJudge.setUnitName(unitName);
			aviPanelJudge.setMachineRecipeName(machineRecipeName);
			aviPanelJudge.setPanelJudge(panelJudge);
			aviPanelJudge.setPanelGrade(panelGrade);
			aviPanelJudge.setGamJudge(gamJudge);
			aviPanelJudge.setAoiJudge(aoiJudge);
			aviPanelJudge.setMurJudge(murJudge);
			aviPanelJudge.setTpJudge(tpJudge);
			aviPanelJudge.setManJudge(manJudge);
			aviPanelJudge.setAppJudge(appJudge);
			aviPanelJudge.setLastEventName(eventInfo.getEventName());
			aviPanelJudge.setLastEventUser(eventInfo.getEventUser());
			aviPanelJudge.setLastEventTime(eventInfo.getEventTime());
			aviPanelJudge.setLastEventComment(eventInfo.getEventComment());
			aviPanelJudge.setLastEventTimekey(eventInfo.getEventTimeKey());

			ExtendedObjectProxy.getAVIPanelJudgeService().modify(eventInfo, aviPanelJudge);
		}
	}
}
