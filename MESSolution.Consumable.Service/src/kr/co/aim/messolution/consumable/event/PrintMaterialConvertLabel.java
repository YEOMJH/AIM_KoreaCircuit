package kr.co.aim.messolution.consumable.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;

import org.jdom.Document;

public class PrintMaterialConvertLabel extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String materialCode = SMessageUtil.getBodyItemValue(doc, "MATERIALCODE", true);
		String materialNumber = SMessageUtil.getBodyItemValue(doc, "MATERIALNUMBER", true);
		String materialDescription = SMessageUtil.getBodyItemValue(doc, "MATERIALDESCRIPTION", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String quantity = SMessageUtil.getBodyItemValue(doc, "QUANTITY", true);
		String productionDate = SMessageUtil.getBodyItemValue(doc, "PRODUCTIONDATE", true);
		String effectiveDate = SMessageUtil.getBodyItemValue(doc, "EFFECTIVEDATE", true);
		String createUser = SMessageUtil.getBodyItemValue(doc, "CREATEUSER", true);

		// Insert PrintMaterialConvertLabel - N
		StringBuffer sql = new StringBuffer();
		sql.append("INSERT ");
		sql.append("  INTO CT_MATERIALLABEL  ");
		sql.append("  (FACTORYNAME, MATERIALCODE, MATERIALNUMBER, MATERIALDESCRIPTION, LOTNAME, ");
		sql.append("   QUANTITY, PRODUCTIONDATE, EFFECTIVEDATE, CREATEUSER, CREATETIME, ");
		sql.append("   LASTPRINTSEQUENCE) ");
		sql.append("VALUES  ");
		sql.append("  (:FACTORYNAME, :MATERIALCODE, :MATERIALNUMBER, :MATERIALDESCRIPTION, :LOTNAME, :QUANTITY, ");
		sql.append("   :PRODUCTIONDATE, :EFFECTIVEDATE, :CREATEUSER, :CREATETIME, ");
		sql.append("   '0') ");
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("MATERIALCODE", materialCode);
		bindMap.put("MATERIALNUMBER", materialNumber);
		bindMap.put("MATERIALDESCRIPTION", materialDescription);
		bindMap.put("LOTNAME", lotName);
		bindMap.put("QUANTITY", quantity);
		bindMap.put("PRODUCTIONDATE", productionDate);
		bindMap.put("EFFECTIVEDATE", effectiveDate);
		bindMap.put("CREATEUSER", createUser);
		bindMap.put("CREATETIME", TimeStampUtil.getCurrentEventTimeKey());

		GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
		
		return doc;
	}

}
