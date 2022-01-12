package kr.co.aim.messolution.consumable.event;

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
import kr.co.aim.greenframe.exception.greenFrameErrorSignal;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.info.CreateInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class CreateCrate extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		List<Element> eleCrateList = SMessageUtil.getBodySequenceItemList(doc, "CRATELIST", true);

		for (Element eleCrate : eleCrateList)
		{
			String sFactoryName = SMessageUtil.getChildText(eleCrate, "FACTORYNAME", true);
			String sConsumableName = SMessageUtil.getChildText(eleCrate, "CONSUMABLENAME", true);
			String sConsumableSpecName = SMessageUtil.getChildText(eleCrate, "CONSUMABLESPECNAME", true);
			String sGlassDefaultQuantity = SMessageUtil.getChildText(eleCrate, "CREATEQUANTITY", true);
			String sBatchNo = SMessageUtil.getChildText(eleCrate, "BATCHNO", true);
			String expireDate = SMessageUtil.getChildText(eleCrate, "EXPIREDATE", false);
			String boxID = SMessageUtil.getChildText(eleCrate, "BOXID", false);

			ConsumableSpec crateSpec = GenericServiceProxy.getSpecUtil().getConsumableSpec(sFactoryName, sConsumableSpecName, "");
			String crateName = sConsumableName;
			
			java.sql.Timestamp expirationDatetimeStamp = null;

			if(!expireDate.isEmpty())
			{
				expirationDatetimeStamp = TimeUtils.getTimestamp(expireDate);
				expireDate = expirationDatetimeStamp.toString();
			}
			
			Map<String, String> udfs = CommonUtil.setNamedValueSequence(eleCrate, Consumable.class.getSimpleName());
			udfs.put("BATCHNO", sBatchNo);
			udfs.put("EXPIRATIONDATE", expireDate);
			udfs.put("BOXID", boxID);

			CreateInfo transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().createInfo(sFactoryName, "", crateName, crateSpec.getKey().getConsumableSpecName(),
					crateSpec.getKey().getConsumableSpecVersion(), crateSpec.getConsumableType(), Long.parseLong(sGlassDefaultQuantity), udfs);

			MESConsumableServiceProxy.getConsumableServiceImpl().createCrate(eventInfo, crateName, transitionInfo);
			
			try{
				if(StringUtil.isNotEmpty(boxID))
				{
					StringBuffer sql = new StringBuffer();
					sql.append(" SELECT BOXID  ");
					sql.append("  FROM MES_WMSIF_RECEIVE@OADBLINK.V3FAB.COM ");
					sql.append(" WHERE BOXID = :BOXID ");
					sql.append(" AND ( INTERFACEFLAG<>'S' OR INTERFACEFLAG IS NULL ) ");
					
					Map<String, String> args = new HashMap<String, String>();
					args.put("BOXID", boxID);
					
					@SuppressWarnings("unchecked")
					List<Map<String, Object>> boxList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
					if(boxList!=null && boxList.size()>0)
					{
						String updateIf= " UPDATE  MES_WMSIF_RECEIVE@OADBLINK.V3FAB.COM SET INTERFACEFLAG='S' WHERE BOXID = :BOXID ";
	                    Map<String, String> bindMap=new HashMap<String, String>();
	                    bindMap.put("BOXID", boxID);
						GenericServiceProxy.getSqlMesTemplate().update(updateIf, bindMap);
					}
				}
			}
			catch(greenFrameErrorSignal ex){
				throw new CustomException("Crate-001", boxID);
			}

		}

		return doc;
	}

}
