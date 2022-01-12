package kr.co.aim.messolution.durable.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
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
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.durable.management.data.DurableSpecKey;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupKey;
import kr.co.aim.greentrack.processgroup.management.info.CreateInfo;

public class CreateMaskGroup extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		// String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String NewMaskGroupName = SMessageUtil.getBodyItemValue(doc, "MASKGROUPNAME", true);
		String MaskCapacity = SMessageUtil.getBodyItemValue(doc, "MASKCAPACITY", true);
		String MaskSpecName = SMessageUtil.getBodyItemValue(doc, "MASKSPECNAME", true);

		StringBuffer inquirysql = new StringBuffer();
		inquirysql.append("SELECT * ");
		inquirysql.append("  FROM PROCESSGROUP ");
		inquirysql.append(" WHERE PROCESSGROUPNAME = :PROCESSGROUPNAME ");

		Map<String, String> inquirybindMap = new HashMap<String, String>();
		inquirybindMap.put("PROCESSGROUPNAME", NewMaskGroupName);
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(inquirysql.toString(), inquirybindMap);

		if (sqlResult.size() > 0)
		{
			StringBuilder maskGroupNames = new StringBuilder();
			for (Map<String, Object> searchedGroupName : sqlResult)
			{
				maskGroupNames.append(CommonUtil.getValue(searchedGroupName, "PROCESSGROUPNAME"));
				maskGroupNames.append(' ');
			}
			throw new CustomException("MASK-0019", maskGroupNames);
		}

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateMaskGroup", getEventUser(), getEventComment(), "", "", "");
		// eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());

		ProcessGroupKey processGroupKey = new ProcessGroupKey();
		processGroupKey.setProcessGroupName(NewMaskGroupName);

		ProcessGroup processGroupData = new ProcessGroup();
		processGroupData.setKey(processGroupKey);

		// Put data UDF
		Map<String, String> udfs = new HashMap<String, String>();

		udfs.put("MASKSPEC", MaskSpecName);
		udfs.put("MASKCAPACITY", MaskCapacity);

		processGroupData.setUdfs(udfs);

		CreateInfo createInfo = new CreateInfo();
		createInfo.setProcessGroupType(constantMap.MaterialGroup_Mask);
		createInfo.setMaterialType(constantMap.MaterialType_Mask);
		createInfo.setMaterialQuantity(0);
		createInfo.setProcessGroupName(NewMaskGroupName);

		Map<String, String> processGroupUdfs = processGroupData.getUdfs();
		createInfo.setUdfs(processGroupUdfs);

		MESProcessGroupServiceProxy.getProcessGroupServiceImpl().create(processGroupData, createInfo, eventInfo);

		return doc;
	}

}
