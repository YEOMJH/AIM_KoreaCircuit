package kr.co.aim.messolution.consumable.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.SetMaterialLocationInfo;

import org.jdom.Document;
import org.jdom.Element;

public class InkReserveEQP extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "MATERIALLOCATIONNAME", true);

		List<Element> consumerbleList = SMessageUtil.getBodySequenceItemList(doc, "CONSUMABLELIST", false);

		Element bodyElement = SMessageUtil.getBodyElement(doc);
		List<String> inkNameList = CommonUtil.makeList(bodyElement, "CONSUMABLELIST", "CONSUMABLENAME");

		if (inkNameList.size() == 0)
		{
			inkNameList.add("");
		}

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("InkReserveEQP", getEventUser(), getEventComment(), null, null);

		List<String> removeList = removedInkList(machineName, unitName, subUnitName, factoryName, inkNameList);

		if (removeList.size() > 0)
		{
			for (String removedInkName : removeList)
			{
				Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(removedInkName);

				SetMaterialLocationInfo setMaterialLocationInfo = new SetMaterialLocationInfo();
				setMaterialLocationInfo.setMaterialLocationName("");
				setMaterialLocationInfo.getUdfs().put("MACHINENAME", "");
				setMaterialLocationInfo.getUdfs().put("UNITNAME", "");
				setMaterialLocationInfo.getUdfs().put("LOADFLAG", "");
				
				MESConsumableServiceProxy.getConsumableServiceImpl().setMaterialLocation(consumableData, setMaterialLocationInfo , eventInfo);
			}
		}

		for (Element consumableE : consumerbleList)
		{
			String consumableName = consumableE.getChild("CONSUMABLENAME").getText();

			Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(consumableName);

			SetMaterialLocationInfo setMaterialLocationInfo = new SetMaterialLocationInfo();
			setMaterialLocationInfo.setMaterialLocationName(subUnitName);
			setMaterialLocationInfo.getUdfs().put("MACHINENAME", machineName);
			setMaterialLocationInfo.getUdfs().put("UNITNAME", unitName);
			setMaterialLocationInfo.getUdfs().put("LOADFLAG", "R");
			
			MESConsumableServiceProxy.getConsumableServiceImpl().setMaterialLocation(consumableData, setMaterialLocationInfo , eventInfo);
		}

		return doc;
	}

	public List<String> removedInkList(String machineName, String unitName, String subUnitName, String factoryName, List<String> inkList)
	{
		List<String> removedList = new ArrayList<String>();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT C.CONSUMABLENAME ");
		sql.append("  FROM CONSUMABLE C, CONSUMABLESPEC CS ");
		sql.append(" WHERE C.CONSUMABLESPECNAME = CS.CONSUMABLESPECNAME ");
		sql.append("   AND C.CONSUMABLESPECVERSION = CS.CONSUMABLESPECVERSION ");
		sql.append("   AND C.FACTORYNAME = CS.FACTORYNAME ");
		sql.append("   AND C.MACHINENAME = :MACHINENAME ");
		sql.append("   AND C.UNITNAME = :UNITNAME ");
		sql.append("   AND C.MATERIALLOCATIONNAME = :SUBUNITNAME ");
		sql.append("   AND C.LOADFLAG = 'R' ");
		sql.append("   AND C.CONSUMABLETYPE = 'Ink' ");
		sql.append("   AND C.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND C.CONSUMABLENAME NOT IN ( :CONSUMABLENAME) ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("MACHINENAME", machineName);
		args.put("UNITNAME", unitName);
		args.put("SUBUNITNAME", subUnitName);
		args.put("FACTORYNAME", factoryName);
		args.put("CONSUMABLENAME", inkList);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			removedList = CommonUtil.makeListBySqlResult(result, "CONSUMABLENAME");
		}

		return removedList;
	}

}
