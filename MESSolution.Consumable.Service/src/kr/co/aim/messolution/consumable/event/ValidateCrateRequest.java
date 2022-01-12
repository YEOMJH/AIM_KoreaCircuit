package kr.co.aim.messolution.consumable.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;

public class ValidateCrateRequest extends SyncHandler {

	@SuppressWarnings("unchecked")
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String crateName = SMessageUtil.getBodyItemValue(doc, "CRATENAME", true);

		// Set MessageName
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "ValidateCrateReply");
		this.generateBodyTemplate(SMessageUtil.getBodyElement(doc), machineName, portName, crateName);

		// Consumable Data
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

		ConsumableSpec crateSpecData = GenericServiceProxy.getSpecUtil().getConsumableSpec(crateData.getFactoryName(), crateData.getConsumableSpecName(), "");
		
		// Validation - 1. Consumable State = Available
		if (!StringUtils.equals(crateData.getConsumableState(), GenericServiceProxy.getConstantMap().Cons_Available))
		{
			SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
			throw new CustomException("CRATE-0005", crateData.getKey().getConsumableName());
		}

		// Validation - 2. Consumable Quantity > 0
		if (crateData.getQuantity() <= 0)
		{
			SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
			throw new CustomException("MATERIAL-0020", crateData.getKey().getConsumableName());
		}

		
		// Validation batchNo first in first out
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT MIN(BATCHNO) AS MAXBATCHNO  FROM CONSUMABLE   ");
		sql.append("  WHERE CONSUMABLETYPE='Crate'  ");
		sql.append("  AND CONSUMABLESTATE='Available' ");
		sql.append("  AND CONSUMABLESPECNAME=:CONSUMABLESPECNAME ");
		sql.append("  AND QUANTITY>0 ");
		sql.append("  AND (LOADFLAG IS NULL OR LOADFLAG='N' OR CONSUMABLENAME=:CONSUMABLENAME) ");

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("CONSUMABLESPECNAME", crateData.getConsumableSpecName());
		bindMap.put("CONSUMABLENAME", crateName);
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		if (sqlResult!=null&&sqlResult.size() > 0)
		{
			if(!StringUtils.equals(sqlResult.get(0).get("MAXBATCHNO").toString(),crateData.getUdfs().get("BATCHNO") ))
			{
				SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
				throw new CustomException("MATERIAL-0043", sqlResult.get(0).get("MAXBATCHNO").toString());
			}
		}
				
		//Validate ExpirationDate
		if(crateData.getUdfs().get("EXPIRATIONDATE") != null)
		{
			String expirationDateString = crateData.getUdfs().get("EXPIRATIONDATE").toString();
			
			Date expirationDate = null;
			Date currentDate = null;
			SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String currentTime = transFormat.format(new Date());
			
			try {
				currentDate = transFormat.parse(currentTime);
				expirationDate = transFormat.parse(expirationDateString);
			} catch (ParseException e) {
				SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
				throw new CustomException("MATERIAL-0028", crateData.getKey().getConsumableName(), "Crate");
			}
			
			if(currentDate.compareTo(expirationDate) > 0)
			{
				SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
				throw new CustomException("MATERIAL-9998", crateData.getKey().getConsumableName(), expirationDateString);
			}
		}
		
		SMessageUtil.setBodyItemValue(doc, "CRATEQUANTITY", String.valueOf((int) crateData.getQuantity()));
		SMessageUtil.setBodyItemValue(doc, "CRATESPECNAME",  crateSpecData.getKey().getConsumableSpecName());
		SMessageUtil.setBodyItemValue(doc, "THICKNESS",  crateSpecData.getUdfs().get("GLASSTHICKNESS"));
		SMessageUtil.setBodyItemValue(doc, "VENDOR","1");
		//SMessageUtil.setBodyItemValue(doc, "VENDOR", ConvertUtil.getMapValueByName(sqlResult.get(0), "VENDOR"));
		SMessageUtil.setBodyItemValue(doc, "GLASSSIZE", crateSpecData.getUdfs().get("GLASSSIZE"));
		SMessageUtil.setBodyItemValue(doc, "RESULT", "OK");

		return doc;
	}

	private Element generateBodyTemplate(Element bodyElement, String machineName, String portName, String crateName) throws CustomException
	{
		JdomUtils.addElement(bodyElement, "CRATEQUANTITY", "");
		JdomUtils.addElement(bodyElement, "CRATESPECNAME", "");
		JdomUtils.addElement(bodyElement, "THICKNESS", "");
		JdomUtils.addElement(bodyElement, "VENDOR", "");
		JdomUtils.addElement(bodyElement, "GLASSSIZE", "");
		JdomUtils.addElement(bodyElement, "RESULT", "");
		JdomUtils.addElement(bodyElement, "RESULTDESCRIPTION", "");

		return bodyElement;
	}
}
