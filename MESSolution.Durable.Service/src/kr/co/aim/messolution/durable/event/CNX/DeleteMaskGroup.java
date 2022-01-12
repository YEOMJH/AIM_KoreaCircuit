package kr.co.aim.messolution.durable.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskSpec;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.processgroup.management.info.UndoInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupKey;
import kr.co.aim.greentrack.processgroup.management.info.CreateInfo;
import kr.co.aim.greentrack.processgroup.management.info.SetEventInfo;

public class DeleteMaskGroup extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		String maskSpec = SMessageUtil.getBodyItemValue(doc, "MASKSPEC", true);
		String processGroupName = SMessageUtil.getBodyItemValue(doc, "PROCESSGROUPNAME", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteMaskGroup", getEventUser(), getEventComment(), "", "", "");
		Map<String, Object> bindMap = new HashMap<String, Object>();

		ProcessGroup processGroupData = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getProcessGroupData(processGroupName);
		processGroupData.setMaterialQuantity(0);

		SetEventInfo setEventInfo = new SetEventInfo();

		StringBuffer sql = new StringBuffer();
		sql.append("DELETE PROCESSGROUP ");
		sql.append(" WHERE PROCESSGROUPNAME = :PROCESSGROUPNAME ");
		sql.append("   AND MASKSPEC = :MASKSPEC ");

		bindMap.put("PROCESSGROUPNAME", processGroupName);
		bindMap.put("MASKSPEC", maskSpec);

		MESProcessGroupServiceProxy.getProcessGroupServiceImpl().setEvent(processGroupData, setEventInfo, eventInfo);

		kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);

		return doc;
	}

}
