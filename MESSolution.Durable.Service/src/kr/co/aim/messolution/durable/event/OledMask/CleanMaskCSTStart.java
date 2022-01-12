package kr.co.aim.messolution.durable.event.OledMask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;

public class CleanMaskCSTStart extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String sCarrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String sPortName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sCarrierName);

		CommonValidation.checkEmptyCst(sCarrierName);
		checkAssignedMaskLotByCst(sCarrierName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackIn", getEventUser(), getEventComment(), "", "");

		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("MACHINENAME", sMachineName);
		setEventInfo.getUdfs().put("PORTNAME", sPortName);

		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
	}

	private void checkAssignedMaskLotByCst(String carrierName) throws CustomException
	{
		String sql = "SELECT MASKLOTNAME FROM CT_MASKLOT WHERE CARRIERNAME = :CARRIERNAME ";
		Map<String, String> args = new HashMap<String, String>();
		args.put("CARRIERNAME", carrierName);

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

		if (result.size() > 0)
		{
			String lotName = ConvertUtil.getMapValueByName(result.get(0), "MASKLOTNAME");

			// Lot[{0}] is assigned to CST[{1}]
			throw new CustomException("CST-0002", lotName, carrierName);
		}
	}
}
