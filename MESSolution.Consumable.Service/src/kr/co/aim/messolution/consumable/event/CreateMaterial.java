package kr.co.aim.messolution.consumable.event;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.info.CreateInfo;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class CreateMaterial extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> eMaterialList = SMessageUtil.getBodySequenceItemList(doc, "MATERIALLIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), null, null);

		String firstBoxId = StringUtil.EMPTY;
		
		// TransportState isn't set. set the state in StockIn Screen.
		for (Element materialE : eMaterialList)
		{

			String factoryName = materialE.getChildText("FACTORYNAME");
			String materialID = materialE.getChildText("MATERIALID");
			String materialType = materialE.getChildText("MATERIALTYPE");
			String materialKind = materialE.getChildText("MATERIALKIND");
			String materialSpecName = materialE.getChildText("MATERIALSPECNAME");
			String materialSpecVersion = materialE.getChildText("MATERIALSPECVERSION");
			String quantity = materialE.getChildText("QTANTITY");
			String machineName = materialE.getChildText("MACHINENAME");
			String unitName = materialE.getChildText("UNITNAME");
			String subUnitName = materialE.getChildText("SUBUNITNAME");
			String batchNo = materialE.getChildText("BATCHNO");
			String boxid = materialE.getChildText("BOXID");
			String wmsFactoryName = materialE.getChildText("WMSFACTORYNAME");
			String wmsFactoryPosition = materialE.getChildText("WMSFACTORYPOSITION");

			String productDate = materialE.getChildText("PRODUCTDATE");
			String expirationDate = materialE.getChildText("EXPIRATIONDATE");
			String location = materialE.getChildText("WMSFACTORYNAME");
			String DepartExpenseFlag = materialE.getChildText("DEPARTEXPENSEFLAG");
			
			java.sql.Timestamp expirationDatetimeStamp = null;

			if(!expirationDate.isEmpty())
			{
				expirationDatetimeStamp = TimeUtils.getTimestamp(expirationDate);
				expirationDate = expirationDatetimeStamp.toString();
			}
			
			if (StringUtil.isEmpty(firstBoxId))
			{
				firstBoxId = boxid;
			}
			
			if (materialKind.equals(GenericServiceProxy.getConstantMap().MaterialKind_Consumable))
			{
				ConsumableSpec consumableSpec = GenericServiceProxy.getSpecUtil().getMaterialSpec(factoryName, materialSpecName, materialSpecVersion);

				Map<String, String> udfs = new HashMap<String, String>();
				udfs.put("PRODUCTDATE", productDate);
				udfs.put("EXPIRATIONDATE", expirationDate);
				udfs.put("BATCHNO", batchNo);
				udfs.put("TIMEUSEDLIMIT", consumableSpec.getUdfs().get("TIMEUSEDLIMIT"));
				udfs.put("DURATIONUSEDLIMIT", consumableSpec.getUdfs().get("DURATIONUSEDLIMIT"));
				udfs.put("DEPARTEXPENSEFLAG", DepartExpenseFlag);
				udfs.put("CONSUMABLEHOLDSTATE", "N");
				udfs.put("WMSFACTORYNAME", wmsFactoryName);
				udfs.put("WMSFACTORYPOSITION", wmsFactoryPosition);
				if(materialType.endsWith("Lamination"))
				{
					udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MaterialLocation_Bank);
				}
				else
				{
					udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MaterialLocation_OutEQP);
				}
				
				udfs.put("WMSFACTORYNAME", location);
				udfs.put("BOXID", boxid);
				//JYJ
                if(materialType.equals("PR")||materialType.equals("Adhesive Agent")||materialType.equals("Organic adhesive"))
                {
                	udfs.put("DETAILCONSUMABLESTATE","Freeze");
                	udfs.put("FREEZETIME", eventInfo.getEventTime().toString());
                }
				if (materialType.equals("PR"))
				{
					if (StringUtil.isEmpty(consumableSpec.getUdfs().get("LIFETIMESTORE")))
					{
						//MATERIAL-0040:{0} Value is null. Please Check ConsumableSpec.
						throw new CustomException("MATERIAL-0040", "LIFETIMESTORE");
					}

					if (StringUtil.isEmpty(consumableSpec.getUdfs().get("LIFETIMEOPEN")))
						throw new CustomException("MATERIAL-0040", "LIFETIMEOPEN");

					if (StringUtil.isEmpty(consumableSpec.getUdfs().get("DETAILCONSUMABLETYPE")))
						throw new CustomException("MATERIAL-0040", "DETAILCONSUMABLETYPE");

					udfs.put("LIFETIMESTORE", consumableSpec.getUdfs().get("LIFETIMESTORE"));
					udfs.put("LIFETIMEOPEN", consumableSpec.getUdfs().get("LIFETIMEOPEN"));
					udfs.put("DETAILCONSUMABLETYPE", consumableSpec.getUdfs().get("DETAILCONSUMABLETYPE"));

					if (consumableSpec.getUdfs().get("DETAILCONSUMABLETYPE").equals("OrganicGlue"))
					{
						udfs.put("FREEZETIME", TimeStampUtil.toTimeString(eventInfo.getEventTime()));
						udfs.put("DETAILCONSUMABLESTATE", GenericServiceProxy.getConstantMap().DetailMaterialState_Freeze);

					}
					else if (consumableSpec.getUdfs().get("DETAILCONSUMABLETYPE").equals("PhotoGlue"))
					{
						udfs.put("MATERIALSTOCKTIME", TimeStampUtil.toTimeString(eventInfo.getEventTime()));
					}

				}
				//houxk add
				if (materialType.equals("Organic")||materialType.equals("InOrganic"))
				{
//					if (StringUtil.isEmpty(consumableSpec.getUdfs().get("LIFETIMEOPEN")))
//						throw new CustomException("MATERIAL-0040", "LIFETIMEOPEN");
//					if (StringUtil.isEmpty(consumableSpec.getUdfs().get("LIFETIMESTORE")))
//						throw new CustomException("MATERIAL-0040", "LIFETIMESTORE");	

					udfs.put("LIFETIMEOPEN", "1440");
					//udfs.put("LIFETIMESTORE", consumableSpec.getUdfs().get("LIFETIMESTORE"));
				}
				
				CreateInfo createInfo = MESConsumableServiceProxy.getConsumableInfoUtil().createInfo(factoryName, "", materialID, materialSpecName, materialSpecVersion,
						consumableSpec.getConsumableType(), Double.valueOf(quantity).doubleValue(), udfs);

				MESConsumableServiceProxy.getConsumableServiceImpl().createMaterial(eventInfo, materialID, createInfo);

			}
			else
			{
				try
				{
					kr.co.aim.greentrack.durable.management.info.CreateInfo createInfo = MESDurableServiceProxy.getDurableInfoUtil().createInfo(materialID, materialSpecName, materialSpecVersion,
							quantity, factoryName);

					Map<String, String> udfs = createInfo.getUdfs();
					udfs.put("MACHINENAME", machineName);
					udfs.put("UNITNAME", unitName);
					udfs.put("CHAMBERNAME", subUnitName);
					udfs.put("EXPIRATIONDATE", expirationDate);
					udfs.put("BOXID", boxid);

					createInfo.setUdfs(udfs);

					MESDurableServiceProxy.getDurableServiceImpl().create(materialID, createInfo, eventInfo);
				}
				catch (FrameworkErrorSignal fe)
				{
					throw new CustomException("MATERIAL-9999", fe.getMessage());
				}
				catch (DuplicateNameSignal de)
				{
					throw new CustomException("MATERIAL-9003", materialID);
				}

			}

		}
        try{
        	this.updateInterfaceTable(firstBoxId);
        }
		catch(Exception e)
        {
			eventLog.info("DBLink Update filed");
        }
		
		return doc;
	}
	
	private void updateInterfaceTable(String boxId)
	{
		String sql = "UPDATE MES_WMSIF_RECEIVE@V3MESWMSLINK "
				   + "SET INTERFACEFLAG = 'Y' "
				   + "WHERE BOXID = ? ";
		
		greenFrameServiceProxy.getSqlTemplate().update(sql, new Object[] { boxId });
	}
}
