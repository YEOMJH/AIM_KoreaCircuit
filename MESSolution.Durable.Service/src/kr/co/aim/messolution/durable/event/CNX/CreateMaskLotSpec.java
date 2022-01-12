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
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class CreateMaskLotSpec extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String maskType = SMessageUtil.getBodyItemValue(doc, "MASKTYPE", true);
		String productionType = SMessageUtil.getBodyItemValue(doc, "PRODUCTIONTYPE", true);
		String maskProcessFlowName = SMessageUtil.getBodyItemValue(doc, "MASKPROCESSFLOWNAME", true);
		String maskProcessFlowVersion = SMessageUtil.getBodyItemValue(doc, "MASKPROCESSFLOWVERSION", true);
		String vendor = SMessageUtil.getBodyItemValue(doc, "VENDOR", true);
		float timeUsedLimit = Float.parseFloat(SMessageUtil.getBodyItemValue(doc, "TIMEUSEDLIMIT", true));
		String layer = SMessageUtil.getBodyItemValue(doc, "LAYER", true);
		float maskSize = Float.parseFloat(SMessageUtil.getBodyItemValue(doc, "MASKSIZE", true));
		String resolution = SMessageUtil.getBodyItemValue(doc, "RESOLUTION", true);
		String project = SMessageUtil.getBodyItemValue(doc, "PROJECT", true);
		String version = SMessageUtil.getBodyItemValue(doc, "VERSION", true);
		String description = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("MASKTYPE", maskType);
		nameRuleAttrMap.put("LAYER", layer);
		nameRuleAttrMap.put("PRODUCTSIZE", maskSize);
		nameRuleAttrMap.put("RESOLUTION", resolution);
		nameRuleAttrMap.put("PROJECT", project);
		nameRuleAttrMap.put("VERSION", version);
		String newMaskSpecName = "";
		List<String> lstName = CommonUtil.generateNameByNamingRule("OLEDMaskSpecNaming", nameRuleAttrMap, 1);
		newMaskSpecName = lstName.get(0);

		StringBuffer inquirysql = new StringBuffer();
		inquirysql.append("SELECT MASKSPECNAME ");
		inquirysql.append("  FROM CT_MASKSPEC ");
		inquirysql.append(" WHERE MASKSPECNAME = :MASKSPECNAME ");

		Map<String, String> inquirybindMap = new HashMap<String, String>();
		inquirybindMap.put("MASKSPECNAME", newMaskSpecName);
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(inquirysql.toString(), inquirybindMap);

		if (sqlResult.size() > 0)
		{
			StringBuilder maskSpecNames = new StringBuilder();
			for (Map<String, Object> searchedMaskSpec : sqlResult)
			{
				maskSpecNames.append(CommonUtil.getValue(searchedMaskSpec, "MASKSPECNAME"));
				maskSpecNames.append(' ');
			}
			throw new CustomException("MASKSPEC-0001", maskSpecNames);
		}

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), "", "", "");
		eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());
		MaskSpec dataInfo = new MaskSpec();
		dataInfo.setFactoryName(factoryName);
		dataInfo.setMaskSpecName(newMaskSpecName);
		dataInfo.setDescription(description);
		dataInfo.setCreateTime(eventInfo.getEventTime());
		dataInfo.setCreateUser(eventInfo.getEventUser());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setMaskType(maskType);
		dataInfo.setTimeUsedLimit(timeUsedLimit);
		dataInfo.setVendor(vendor);
		dataInfo.setLastEventTimeKey(eventInfo.getLastEventTimekey());
		dataInfo.setMaskProcessFlowName(maskProcessFlowName);
		dataInfo.setMaskProcessFlowVersion(maskProcessFlowVersion);
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setProductionType(productionType);
		//dataInfo.setGeneration(constantMap.Generation);
		dataInfo.setMaskSize(maskSize);
		//dataInfo.setResolution(resolution);
		//dataInfo.setVersion(version);
		//dataInfo.setProject(project);
		//dataInfo.setLayer(layer);
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventName(eventInfo.getEventName());

		ExtendedObjectProxy.getMaskSpecService().create(eventInfo, dataInfo);

		return doc;
	}

}
