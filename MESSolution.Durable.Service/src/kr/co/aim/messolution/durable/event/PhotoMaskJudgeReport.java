package kr.co.aim.messolution.durable.event;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.DirtyInfo;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class PhotoMaskJudgeReport extends AsyncHandler {
	private static Log log = LogFactory.getLog(PhotoMaskJudgeReport.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String sUnitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String sSubUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String sDurableName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String judge = SMessageUtil.getBodyItemValue(doc, "JUDGE", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("PhotoMaskJudgeReport", getEventUser(), getEventComment(), "", judge);
		
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);
		if(judge.equals("D"))
		{
			durableData.setDurableCleanState(GenericServiceProxy.getConstantMap().Dur_Dirty);
		}
		DurableServiceProxy.getDurableService().update(durableData);

		SetEventInfo setEventInfo = new SetEventInfo();
		
		setEventInfo.getUdfs().put("MACHINENAME", sMachineName);
		setEventInfo.getUdfs().put("UNITNAME", sUnitName);

		// SetEvent Info
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
		
	}
}
