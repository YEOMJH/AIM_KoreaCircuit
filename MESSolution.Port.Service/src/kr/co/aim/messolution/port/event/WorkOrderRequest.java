package kr.co.aim.messolution.port.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.XmlUtil;

public class WorkOrderRequest extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String crateName = SMessageUtil.getBodyItemValue(doc, "CRATENAME", true);

		// Set MessageName
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "WorkOrderReply");
		this.generateBodyTemplate(SMessageUtil.getBodyElement(doc), machineName, portName, crateName);

		// 1. Check crate
		Consumable crateData = new Consumable();
		try
		{
			crateData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(crateName);
		}
		catch (CustomException e)
		{
			SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
			throw new CustomException("CRATE-9001", crateName);
		}

		// 2. Check Consumable State
		if (!StringUtil.equals(crateData.getConsumableState(), GenericServiceProxy.getConstantMap().Cons_Available))
		{
			SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
			throw new CustomException("CRATE-0005", crateData.getKey().getConsumableName());
		}

		// 3. Check Consumable Quantity > 0
		if (crateData.getQuantity() <= 0)
		{
			SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
			throw new CustomException("PRODUCTREQUEST-0001", crateData.getKey().getConsumableName());
		}

		// Check TPPolicy + POSBOM by Crate.
		boolean checkFlag = CommonValidation.checkProductSpecByComsumable(crateData);

		if (checkFlag)
		{
			SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
			SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "ProductSpec and CrateSpec is no mapping relationship.");
			throw new CustomException("CRATE-0006");
		}

		// 4. Search WorkOrder info by plan date
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT PR.FACTORYNAME, ");
		sql.append("       PR.PRODUCTREQUESTNAME, ");
		sql.append("       PR.PRODUCTSPECNAME, ");
		sql.append("       PR.PRODUCTSPECVERSION, ");
		sql.append("       PR.PROCESSFLOWNAME, ");
		sql.append("       PR.PROCESSFLOWVERSION, ");
		sql.append("       PRP.PLANQUANTITY, ");
		sql.append("       PRP.PLANDATE, ");
		sql.append("       PRP.POSITION, ");
		sql.append("       PRP.MACHINENAME ");
		sql.append("  FROM CT_PRODUCTREQUESTPLAN PRP, PRODUCTREQUEST PR ");
		sql.append(" WHERE PRP.PRODUCTREQUESTNAME = PR.PRODUCTREQUESTNAME ");
		sql.append("   AND (PRP.PRODUCTSPECNAME, PRP.PRODUCTSPECVERSION, PRP.FACTORYNAME) IN ");
		sql.append("          (SELECT TP.PRODUCTSPECNAME, TP.PRODUCTSPECVERSION, TP.FACTORYNAME ");
		sql.append("             FROM TPPOLICY TP, POSBOM PB ");
		sql.append("            WHERE TP.CONDITIONID = PB.CONDITIONID ");
		sql.append("              AND PB.MATERIALSPECNAME = :CONSUMABLESPECNAME ");
		sql.append("              AND PB.MATERIALSPECVERSION = :CONSUMABLESPECVERSION) ");
		sql.append("   AND PRP.PLANSTATE IN ('Released', 'Started') ");
		sql.append("   AND PRP.MACHINENAME = :MACHINENAME ");
		sql.append("   AND PRP.PRODUCTREQUESTHOLDSTATE = 'N' ");
		sql.append("   AND PR.PRODUCTREQUESTSTATE = 'Released' ");
		sql.append("ORDER BY PRP.POSITION ");

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("CONSUMABLESPECNAME", crateData.getConsumableSpecName());
		bindMap.put("CONSUMABLESPECVERSION", crateData.getConsumableSpecVersion());
		bindMap.put("MACHINENAME", machineName);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		if (sqlResult == null || sqlResult.size() < 1)
			throw new CustomException("PLAN-0001", crateName);
		else
		{
			eventLog.info("Check Plan's ReserveLot");
			try
			{
				String condition = "machineName = ? and productRequestName =? and planDate =? and reserveState in (? ,?) order by position, reserveTimekey";
				Object bindSet[] = new Object[] { machineName, sqlResult.get(0).get("PRODUCTREQUESTNAME").toString(), sqlResult.get(0).get("PLANDATE").toString(), "Reserved", "Executing" };
				List<ReserveLot> pLotList = ExtendedObjectProxy.getReserveLotService().select(condition, bindSet);
			}
			catch (greenFrameDBErrorSignal ne)
			{
				throw new CustomException("LOT-0207", machineName, sqlResult.get(0).get("PRODUCTREQUESTNAME").toString(), sqlResult.get(0).get("PLANDATE").toString());
			}

			SMessageUtil.setBodyItemValue(doc, "WORKORDER", sqlResult.get(0).get("PRODUCTREQUESTNAME").toString());
			SMessageUtil.setBodyItemValue(doc, "QUANTITY", sqlResult.get(0).get("PLANQUANTITY").toString());
			SMessageUtil.setBodyItemValue(doc, "MAKER", crateData.getUdfs().get("VENDOR"));

			// 5. find PP id
			StringBuffer ppSql = new StringBuffer();
			ppSql.append("SELECT PM.MACHINERECIPENAME ");
			ppSql.append("  FROM TPFOPOLICY TPFO, POSMACHINE PM ");
			ppSql.append(" WHERE 1 = 1 ");
			ppSql.append("   AND TPFO.CONDITIONID = PM.CONDITIONID ");
			ppSql.append("   AND TPFO.FACTORYNAME = :FACTORYNAME ");
			ppSql.append("   AND TPFO.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
			ppSql.append("   AND TPFO.PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");
			ppSql.append("   AND TPFO.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
			ppSql.append("   AND TPFO.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
			ppSql.append("   AND PM.MACHINENAME = :MACHINENAME ");

			bindMap.clear();
			bindMap.put("FACTORYNAME", sqlResult.get(0).get("FACTORYNAME").toString());
			bindMap.put("PRODUCTSPECNAME", sqlResult.get(0).get("PRODUCTSPECNAME").toString());
			bindMap.put("PRODUCTSPECVERSION", sqlResult.get(0).get("PRODUCTSPECVERSION").toString());
			bindMap.put("PROCESSFLOWNAME", sqlResult.get(0).get("PROCESSFLOWNAME").toString());
			bindMap.put("PROCESSFLOWVERSION", sqlResult.get(0).get("PROCESSFLOWVERSION").toString());
			bindMap.put("MACHINENAME", sqlResult.get(0).get("MACHINENAME").toString());

			List<Map<String, Object>> sqlPPResult = GenericServiceProxy.getSqlMesTemplate().queryForList(ppSql.toString(), bindMap);

			SMessageUtil.setBodyItemValue(doc, "MACHINERECIPENAME", sqlPPResult.get(0).get("MACHINERECIPENAME").toString());

			// 5. Search quantity of released product
			StringBuffer qtySql = new StringBuffer();
			qtySql.append("SELECT NVL (SUM (L.PLANPRODUCTQUANTITY), 0) AS PROCESSEDQUANTITY ");
			qtySql.append("  FROM CT_RESERVELOT RL, LOT L ");
			qtySql.append(" WHERE RL.LOTNAME = L.LOTNAME ");
			qtySql.append("   AND RL.PRODUCTREQUESTNAME = :PRODUCTREQUESTNAME ");
			qtySql.append("   AND RL.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
			qtySql.append("   AND RL.PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");
			qtySql.append("   AND RL.PLANDATE = :PLANDATE ");
			qtySql.append("   AND RL.RESERVESTATE = 'Completed' ");
			qtySql.append("   AND L.LOTSTATE = 'Released' ");
			qtySql.append("ORDER BY RL.POSITION ");

			bindMap.clear();
			bindMap.put("PRODUCTREQUESTNAME", sqlResult.get(0).get("PRODUCTREQUESTNAME").toString());
			bindMap.put("PRODUCTSPECNAME", sqlResult.get(0).get("PRODUCTSPECNAME").toString());
			bindMap.put("PRODUCTSPECVERSION", sqlResult.get(0).get("PRODUCTSPECVERSION").toString());
			bindMap.put("PLANDATE", sqlResult.get(0).get("PLANDATE").toString());

			List<Map<String, Object>> sqlQtyesult = GenericServiceProxy.getSqlMesTemplate().queryForList(qtySql.toString(), bindMap);

			if (sqlQtyesult != null && sqlQtyesult.size() == 1)
				SMessageUtil.setBodyItemValue(doc, "PROCESSEDQUANTITY", sqlQtyesult.get(0).get("PROCESSEDQUANTITY").toString());
			else
			{
				SMessageUtil.setBodyItemValue(doc, "PROCESSEDQUANTITY", StringUtil.EMPTY);
			}

			SMessageUtil.setBodyItemValue(doc, "RESULT", "OK");
		}

		return doc;

	}

	private Element generateBodyTemplate(Element bodyElement, String machineName, String portName, String crateName) throws CustomException
	{
		XmlUtil.addElement(bodyElement, "WORKORDER", "");
		XmlUtil.addElement(bodyElement, "QUANTITY", "");
		XmlUtil.addElement(bodyElement, "MAKER", "");
		XmlUtil.addElement(bodyElement, "MACHINERECIPENAME", "");
		XmlUtil.addElement(bodyElement, "PROCESSEDQUANTITY", "");
		XmlUtil.addElement(bodyElement, "RESULT", "");
		XmlUtil.addElement(bodyElement, "RESULTDESCRIPTION", "");

		return bodyElement;
	}
}
