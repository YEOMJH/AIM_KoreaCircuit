package kr.co.aim.messolution.consumable.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.WOPatternFilmInfo;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;


public class MaterialInfoRequest extends SyncHandler {
	private static Log log = LogFactory.getLog(MaterialInfoRequest.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		// Set MessageName
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "MaterialInfoReply");
		this.generateBodyTemplate(SMessageUtil.getBodyElement(doc));
		
		// MATERIALTYPE String [ INK | PR | PALLET | WORKTABLE | FPC | FILM | PHOTOMASK ]

		// MaterialInfoRequest EAS,EAP->MES REQ MACHINENAME
		// MaterialInfoRequest EAS,EAP->MES REQ UNITNAME
		// MaterialInfoRequest EAS,EAP->MES REQ MATERIALNAME
		// MaterialInfoRequest EAS,EAP->MES REQ MATERIALTYPE
		// MaterialInfoReply MES->EAS,EAP REP MACHINENAME
		// MaterialInfoReply MES->EAS,EAP REP UNITNAME
		// MaterialInfoReply MES->EAS,EAP REP MATERIALNAME
		// MaterialInfoReply MES->EAS,EAP REP MATERIALKIND
		// MaterialInfoReply MES->EAS,EAP REP MATERIALTYPE
		// MaterialInfoReply MES->EAS,EAP REP MATERIALPOSITION
		// MaterialInfoReply MES->EAS,EAP REP QUANTITY
		// MaterialInfoReply MES->EAS,EAP REP TIMEUSEDLIMIT
		// MaterialInfoReply MES->EAS,EAP REP TIMEUSED
		// MaterialInfoReply MES->EAS,EAP REP DURATIONUSEDLIMIT
		// MaterialInfoReply MES->EAS,EAP REP DURATIONUSED
		// MaterialInfoReply MES->EAS,EAP REP RESULT
		// MaterialInfoReply MES->EAS,EAP REP RESULTDESCRIPTION

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String materialName = SMessageUtil.getBodyItemValue(doc, "MATERIALNAME", true);
		String materialType = SMessageUtil.getBodyItemValue(doc, "MATERIALTYPE", false);

		boolean isOK = false;
		Consumable consumableData = null;
		Durable durableData = null;

		// Get MaterialType
		try
		{
			durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(materialName);
			materialType = durableData.getDurableType();
		}
		catch (Exception e)
		{
			try
			{
				consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);
				materialType = consumableData.getConsumableType();
			}
			catch (Exception u)
			{
				SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
				SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "Material is not Exist" + "[" + materialName + "]" + " Check Material Info");

				return doc;
			}
		}

		if (materialType.equals(GenericServiceProxy.getConstantMap().MaterialType_PatternFilm))
		{
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT DISTINCT S.SUPERPRODUCTREQUESTNAME FROM LOT L,PROCESSOPERATIONSPEC P,PRODUCTREQUEST S ");
			sql.append("  WHERE L.PROCESSOPERATIONNAME = P.PROCESSOPERATIONNAME ");
			sql.append("  AND L.PRODUCTREQUESTNAME=S.PRODUCTREQUESTNAME ");
			sql.append("  AND P.DETAILPROCESSOPERATIONTYPE='Lami' ");
			sql.append("  AND L.FACTORYNAME='POSTCELL' ");
			sql.append("  AND L.JOBDOWNFLAG=:MACHINENAME ");
			sql.append("  AND L.LOTPROCESSSTATE='WAIT' ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("MACHINENAME", machineName);

			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

			if (sqlResult!=null&&sqlResult.size()>0)
			{
				for(int i=0;i<sqlResult.size();i++)
				{					
					if(!checkConsumableMaterial(sqlResult.get(0).get("SUPERPRODUCTREQUESTNAME").toString(), consumableData.getConsumableSpecName()))
					{
						SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
						SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "Not matched MasterData. ProductRequestName: " + "[" + sqlResult.get(0).get("SUPERPRODUCTREQUESTNAME").toString() + "]" + ", MaterialSpec:[" 
						+ consumableData.getConsumableSpecName() + "]"+ " ,Check Material Info");

						return doc;
					}
				}
			}	 

			if (!StringUtil.equals(consumableData.getConsumableState(), GenericServiceProxy.getConstantMap().Cons_Available))
			{
				SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
				SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "Material" + "[" + consumableData.getKey().getConsumableName() + "]" + "State[" + consumableData.getConsumableState() + "]"
						+ " Check Material Info");

				return doc;
			}

			if (consumableData.getQuantity() <= 0)
			{
				SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
				SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "Material" + "[" + consumableData.getKey().getConsumableName() + "]" + " Quantity is Zero");

				return doc;
			}

			if (consumableData.getUdfs().get("EXPIRATIONDATE") != null)
			{
				String expirationDateString = consumableData.getUdfs().get("EXPIRATIONDATE").toString();

				Date expirationDate = null;
				Date currentDate = null;
				SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String currentTime = transFormat.format(new Date());

				try
				{
					currentDate = transFormat.parse(currentTime);
					expirationDate = transFormat.parse(expirationDateString);
				}
				catch (ParseException e)
				{

					SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
					SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "Material" + "[" + consumableData.getKey().getConsumableName() + "]" + " Check ExpirationDate");

					return doc;
				}

				if (currentDate.compareTo(expirationDate) > 0)
				{
					SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
					SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "Material" + "[" + consumableData.getKey().getConsumableName() + "]" + " Is Over DueDate");

					return doc;
				}
			}

			/*List<WOPatternFilmInfo> woPatternFilmInfoData = ExtendedObjectProxy.getWOPatternFilmInfoService().getWOPatternFilmInfoData(consumableData.getConsumableSpecName(),
					consumableData.getConsumableSpecVersion());

			if (woPatternFilmInfoData == null)
			{
				SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
				SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "MaterialSpec[PatternFilm, " + consumableData.getConsumableSpecName() + ", " + consumableData.getConsumableSpecVersion()
						+ "] is not Assigned to WorkOrder");

				return doc;
			}*/

			// 4. Set Data
			SMessageUtil.setBodyItemValue(doc, "MATERIALPOSITION", "");
			SMessageUtil.setBodyItemValue(doc, "QUANTITY", String.valueOf((int) consumableData.getQuantity()));
			SMessageUtil.setBodyItemValue(doc, "TIMEUSEDLIMIT", consumableData.getUdfs().get("TIMEUSEDLIMIT"));
			SMessageUtil.setBodyItemValue(doc, "TIMEUSED", consumableData.getUdfs().get("TIMEUSED"));
			SMessageUtil.setBodyItemValue(doc, "DURATIONUSEDLIMIT", consumableData.getUdfs().get("DURATIONUSEDLIMIT"));
			SMessageUtil.setBodyItemValue(doc, "DURATIONUSED", consumableData.getUdfs().get("DURATIONUSED"));
			SMessageUtil.setBodyItemValue(doc, "MATERIALTYPE", consumableData.getConsumableType());

			isOK = true;
		}
		else if (materialType.equals(GenericServiceProxy.getConstantMap().MaterialType_PalletJig) || materialType.equals(GenericServiceProxy.getConstantMap().MaterialType_FPC))
		{
			if (!checkData(doc, durableData))
				return doc;

			List<Durable> fpcList = new ArrayList<Durable>();

			if (StringUtil.equals(materialType, GenericServiceProxy.getConstantMap().MaterialType_PalletJig))
			{
				try
				{
					fpcList = DurableServiceProxy.getDurableService().select(" PALLETNAME = ? ORDER BY POSITION", new Object[] { materialName });
				}
				catch (Exception e)
				{
					SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
					SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "PalletJig[" + materialName + "] is empty");

					return doc;
				}

				for (Durable fpcData : fpcList)
				{
					if (!checkData(doc, fpcData))
						return doc;
				}
			}

			if (StringUtil.equals(materialType, GenericServiceProxy.getConstantMap().MaterialType_FPC) && !StringUtils.isEmpty(durableData.getUdfs().get("PALLETNAME")))
			{
				SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
				SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "FPC[" + materialName + "] is assigned Pallet[" + durableData.getUdfs().get("PALLETNAME") + "]");

				return doc;
			}

			SMessageUtil.setBodyItemValue(doc, "MATERIALPOSITION", durableData.getUdfs().get("POSITION"));
			SMessageUtil.setBodyItemValue(doc, "MATERIALTYPE", durableData.getDurableType());
			SMessageUtil.setBodyItemValue(doc, "QUANTITY", String.valueOf((int) durableData.getLotQuantity()));
			SMessageUtil.setBodyItemValue(doc, "TIMEUSEDLIMIT", String.valueOf((int) durableData.getTimeUsedLimit()));
			SMessageUtil.setBodyItemValue(doc, "TIMEUSED", String.valueOf((int) durableData.getTimeUsed()));
			SMessageUtil.setBodyItemValue(doc, "DURATIONUSEDLIMIT", String.valueOf((int) durableData.getDurationUsedLimit()));
			SMessageUtil.setBodyItemValue(doc, "DURATIONUSED", String.valueOf((int) durableData.getDurationUsed()));

			if (StringUtil.equals(materialType, GenericServiceProxy.getConstantMap().MaterialType_PalletJig))
			{
				List<Element> subMaterialList = new ArrayList<Element>();

				for (Durable fpcData : fpcList)
				{
					Element productElement = generateMaterialElement(fpcData);
					subMaterialList.add(productElement);
				}

				Element subMaterialElement = XmlUtil.getChild(SMessageUtil.getBodyElement(doc), "SUBMATERIALLIST", true);
				subMaterialElement.addContent(subMaterialList);

				isOK = true;
			}
			else if (materialType.equals(GenericServiceProxy.getConstantMap().MaterialType_FPC))
			{
				isOK = true;
			}
		}
		else
		{
			SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
			SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "Material[" + materialName + "] Current MaterialType is[" + materialType + "]" + " Check Material Info");

			return doc;
		}

		if (isOK == true)
		{
			SMessageUtil.setBodyItemValue(doc, "RESULT", "OK");
			SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "");
		}
		else
		{
			SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
			SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "");
		}

		return doc;
	}

	private Element generateBodyTemplate(Element bodyElement) throws CustomException
	{

		XmlUtil.addElement(bodyElement, "MATERIALPOSITION", StringUtil.EMPTY);
		XmlUtil.addElement(bodyElement, "QUANTITY", StringUtil.EMPTY);
		XmlUtil.addElement(bodyElement, "TIMEUSEDLIMIT", "0");
		XmlUtil.addElement(bodyElement, "TIMEUSED", "0");
		XmlUtil.addElement(bodyElement, "DURATIONUSEDLIMIT", "0");
		XmlUtil.addElement(bodyElement, "DURATIONUSED", "0");
		XmlUtil.addElement(bodyElement, "SUBMATERIALLIST", StringUtil.EMPTY);
		XmlUtil.addElement(bodyElement, "RESULT", StringUtil.EMPTY);
		XmlUtil.addElement(bodyElement, "RESULTDESCRIPTION", StringUtil.EMPTY);

		return bodyElement;
	}

	private Element generateMaterialElement(Durable durableData)
	{
		Element materialElement = new Element("SUBMATERIAL");

		XmlUtil.addElement(materialElement, "MATERIALNAME", durableData.getKey().getDurableName());
		XmlUtil.addElement(materialElement, "MATERIALTYPE", durableData.getDurableType());
		XmlUtil.addElement(materialElement, "MATERIALPOSITION", durableData.getUdfs().get("POSITION"));
		XmlUtil.addElement(materialElement, "QUANTITY", "1");
		XmlUtil.addElement(materialElement, "TIMEUSEDLIMIT", Integer.toString((int) durableData.getTimeUsedLimit()));
		XmlUtil.addElement(materialElement, "TIMEUSED", Integer.toString((int) durableData.getTimeUsed()));
		XmlUtil.addElement(materialElement, "DURATIONUSEDLIMIT", Integer.toString((int) durableData.getDurationUsedLimit()));
		XmlUtil.addElement(materialElement, "DURATIONUSED", Integer.toString((int) durableData.getDurationUsed()));

		return materialElement;
	}

	@SuppressWarnings("unchecked")
	private boolean checkConsumableMaterial(String produtRequestName, String materialSpecName) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT DISTINCT 1 FROM CT_ERPBOM A ");
		sql.append("  WHERE A.PRODUCTREQUESTNAME=:PRODUCTREQUESTNAME ");
		sql.append(" AND A.MATERIALSPECNAME=:MATERIALSPECNAME ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("PRODUCTREQUESTNAME", produtRequestName);
		bindMap.put("MATERIALSPECNAME", materialSpecName);

		List<Map<String, Object>> sqlResult = null;

		try
		{
			sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-0010", ex.getCause());
		}

		if (sqlResult == null || sqlResult.size() < 1)
		{
			return false;
		}
		return true;
	}

	private boolean checkData(Document doc, Durable durableData) throws CustomException
	{
		if (StringUtil.equals(durableData.getDurableState(), GenericServiceProxy.getConstantMap().Dur_NotAvailable))
		{
			SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
			SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "Material" + "[" + durableData.getKey().getDurableName() + "]" + "State[" + durableData.getDurableState() + "]"
					+ " Check Material Info");

			return false;
		}

		String expirationDateString = durableData.getUdfs().get("EXPIRATIONDATE") == null ? "" : durableData.getUdfs().get("EXPIRATIONDATE").toString();

		Date expirationDate = null;
		Date currentDate = null;
		SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String currentTime = transFormat.format(new Date());

		try
		{
			currentDate = transFormat.parse(currentTime);
			if (!expirationDateString.isEmpty() && expirationDateString != null)
			{
				expirationDate = transFormat.parse(expirationDateString);
			}
		}
		catch (ParseException e)
		{
			SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
			SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "Checking ExpirationDate " + "[" + durableData.getKey().getDurableName() + "]");

			return false;
		}

		if (expirationDate != null)
		{
			if (currentDate.compareTo(expirationDate) > 0)
			{
				SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
				SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "CurrentDate is Over then ExpirationDate");

				return false;
			}
		}

		double timeUsedLimit = durableData.getTimeUsedLimit();
		double timeUsed = durableData.getTimeUsed();
		double durationUsedLimit = durableData.getDurationUsedLimit();
		double durationUsed = durableData.getDurationUsed();

		if (timeUsed >= timeUsedLimit)
		{
			SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
			SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "TimeUsedLimit Over " + "[" + durableData.getKey().getDurableName() + "]");

			return false;
		}

		if (durationUsed >= durationUsedLimit)
		{
			SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
			SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "DurationLimit Over " + "[" + durableData.getKey().getDurableName() + "]");

			return false;
		}

		return true;
	}
}
