package kr.co.aim.messolution.consumable.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpecKey;
import kr.co.aim.greentrack.consumable.management.info.SetAreaInfo;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;
import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaterialProduct;
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ConsumableServiceUtil implements ApplicationContextAware {
	private ApplicationContext applicationContext;
	private Log log = LogFactory.getLog(ConsumableServiceUtil.class);

	public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		applicationContext = arg0;
	}

	public void changeConsumableLocation(EventInfo eventInfo, String consumableName, String areaName, String machineName, String portName) throws CustomException
	{
		Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(consumableName);

		// Crate set udfsData
		Map<String, String> udfs = new HashMap<String, String>();
		udfs.put("MACHINENAME", machineName);
		udfs.put("PORTNAME", portName);
		if (StringUtil.equals(eventInfo.getEventName(), "Unload"))
		{
			udfs.put("LOADFLAG", "N");
		}

		SetAreaInfo setAreaInfo = new SetAreaInfo();
		setAreaInfo.setAreaName(areaName);
		setAreaInfo.setUdfs(udfs);

		MESConsumableServiceProxy.getConsumableServiceImpl().setArea(consumableData, setAreaInfo, eventInfo);
	}
	
	public void changeConsumableLoadFlag(EventInfo eventInfo, String consumableName, String machineName, String portName,String loadFlag) throws CustomException
	{
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("MACHINENAME", machineName);
		setEventInfo.getUdfs().put("PORTNAME", portName);
		setEventInfo.getUdfs().put("LOADFLAG", loadFlag);
		MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(consumableName, setEventInfo, eventInfo);
	}

	public boolean isExistConsumable(String consumableName)
	{
		boolean isExist = false;

		String sql = "SELECT CONSUMABLENAME FROM CONSUMABLE WHERE CONSUMABLENAME = :CONSUMABLENAME ";
		Map<String, String> args = new HashMap<String, String>();
		args.put("CONSUMABLENAME", consumableName);

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

		if (result.size() > 0)
		{
			isExist = true;
		}

		return isExist;
	}

	public void update(Consumable consumableData) throws CustomException
	{
		ConsumableServiceProxy.getConsumableService().update(consumableData);
	}

	public List<String> removedOrganicList(String crucibleLotName, List<String> organicList)
	{
		List<String> removedList = new ArrayList<String>();
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT CONSUMABLENAME ");
		sql.append("  FROM CONSUMABLE ");
		sql.append(" WHERE CONSUMABLETYPE = 'Organic' ");
		sql.append("   AND CRUCIBLELOTNAME = :CRUCIBLELOTNAME ");
		sql.append("MINUS ");
		sql.append("SELECT CONSUMABLENAME ");
		sql.append("  FROM CONSUMABLE ");
		sql.append(" WHERE CONSUMABLENAME IN ( :CONSUMABLENAME) ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("CRUCIBLELOTNAME", crucibleLotName);
		args.put("CONSUMABLENAME", organicList);

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			removedList = CommonUtil.makeListBySqlResult(result, "CONSUMABLENAME");
		}

		return removedList;
	}

	public boolean checkBomTP(Lot lotData, Consumable consumableData)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT TP.FACTORYNAME, TP.PRODUCTSPECNAME, TP.PRODUCTSPECVERSION ");
		sql.append("  FROM TPPOLICY TP, POSBOM PS ");
		sql.append(" WHERE TP.CONDITIONID = PS.CONDITIONID ");
		sql.append("   AND TP.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND PS.MATERIALSPECNAME = :MATERIALSPECNAME ");
		sql.append("   AND PS.MATERIALSPECVERSION = :MATERIALSPECVERSION ");

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("FACTORYNAME", consumableData.getFactoryName());
		bindMap.put("MATERIALSPECNAME", consumableData.getConsumableSpecName());
		bindMap.put("MATERIALSPECVERSION", consumableData.getConsumableSpecVersion());

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		if (sqlResult == null || sqlResult.size() < 1)
		{
			return false;
		}

		if (sqlResult.size() > 0)
		{
			for (Map<String, Object> TPInfo : sqlResult)
			{
				if (!lotData.getFactoryName().equals(TPInfo.get("FACTORYNAME").toString()) || !lotData.getProductSpecName().equals(TPInfo.get("PRODUCTSPECNAME").toString())
						|| !lotData.getProductSpecVersion().equals(TPInfo.get("PRODUCTSPECVERSION").toString()))
				{
					return false;
				}
			}
		}

		return true;
	}

	public boolean checkDurationLimit(Consumable consumableData, String item)
	{
		ConsumableSpec consumableSpec = null;
		try
		{
			consumableSpec = GenericServiceProxy.getSpecUtil().getMaterialSpec(consumableData.getFactoryName(), consumableData.getConsumableSpecName(), consumableData.getConsumableSpecVersion());
		}
		catch (CustomException e)
		{
			return false;
		}
		Object value = consumableData.getUdfs().get(item);
		Object specValue = consumableSpec.getUdfs().get("DURATIONUSEDLIMIT");
		if (value instanceof Timestamp)
		{
			double interval = (double) TimeUtils.getCurrentTimestamp().compareTo((Timestamp) value);
			if (specValue instanceof Double)
			{
				double limit = (double) specValue;
				if (interval > limit)
					return false;
			}
			else
				return false;
		}
		else
			return false;

		return true;
	}

	public void InsertCT_MaterialProduct(List<Object[]> updateArgList)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("INSERT INTO CT_MATERIALPRODUCT  ");
		sql.append("(TIMEKEY,PRODUCTNAME,LOTNAME,MATERIALKIND,MATERIALTYPE, ");
		sql.append(" MATERIALNAME,QUANTITY,EVENTNAME,EVENTTIME,FACTORYNAME, ");
		sql.append(" PRODUCTSPECNAME,PRODUCTSPECVERSION,PROCESSFLOWNAME,PROCESSFLOWVERSION,PROCESSOPERATIONNAME, ");
		sql.append(" PROCESSOPERATIONVERSION,MACHINENAME,MATERIALLOCATIONNAME)  ");
		sql.append("VALUES ");
		sql.append(" ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) ");

		try
		{
			MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), updateArgList);
		}
		catch (CustomException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<Consumable> getConsumableListByDurable(String durableName) throws CustomException
	{
		List<Consumable> consumableList = null;
		try
		{
			String condition = " WHERE CARRIERNAME = ? ";
			consumableList = ConsumableServiceProxy.getConsumableService().select(condition, new Object[] { durableName });
		}
		catch (NotFoundSignal notFoundEx)
		{
			log.info(String.format("No film exists in FilmBox.[BoxID = %s] ", durableName));
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-0010", ex.getCause());
		}

		return consumableList;
	}
	
	public List<Consumable> getOnEQFilmList(Machine machineData, String portName) throws CustomException
	{
		List<Consumable> consumableDataList = null;
		try
		{
			String condition = " WHERE CARRIERNAME IN (SELECT DURABLENAME FROM DURABLE WHERE 1=1 AND MACHINENAME = ? AND DURABLETYPE = 'FilmBox' AND PORTNAME <> ? AND UPPER(TRANSPORTSTATE) = 'ONEQP')";
			consumableDataList = ConsumableServiceProxy.getConsumableService().select(condition, new Object[] { machineData.getKey().getMachineName(), portName });
		}
		catch (NotFoundSignal notFoundEx)
		{
			log.info(String.format("No film loaded on macine.[MachineName = %s] ", machineData.getKey().getMachineName()));
			//throw new CustomException("FILM-0006",  machineData.getKey().getMachineName());
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-0010", ex.getCause());
		}

		return consumableDataList;
	}
	
	public List<ListOrderedMap> getERPBOMMaterialSpec(String factoryName, String productRequestName, String processOperationName, String processOperationVer, String productSpecName)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT * ");
		sql.append("  FROM CT_ERPBOM ");
		sql.append(" WHERE FACTORYNAME = ? ");
		sql.append("   AND PRODUCTREQUESTNAME = ? ");
		sql.append("   AND PROCESSOPERATIONNAME = ? ");
		sql.append("   AND PROCESSOPERATIONVERSION = ? ");
		sql.append("   AND LENGTH(MATERIALSPECNAME )<16 ");
		sql.append("    AND MATERIALSPECNAME NOT IN (SELECT ENUMVALUE FROM ENUMDEFVALUE WHERE ENUMNAME='GasAutoConsumeMaterial') ");

		Object[] bindArray = new Object[]{factoryName, productRequestName, processOperationName, processOperationVer};
		
		List<ListOrderedMap> result = null;

		try
		{
			result = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindArray);
		}
		catch (FrameworkErrorSignal fe)
		{
			result = null;
		}
		return result;
	}
	
	public boolean getERPSkipMaterialList(String materialSpecName,String batchNo)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT DESCRIPTION,DEFAULTFLAG  ");
		sql.append("  FROM ENUMDEFVALUE  ");
		sql.append(" WHERE ENUMNAME ='ERPSkipMaterial' ");
		sql.append(" AND DESCRIPTION=? ");
		sql.append(" AND (DEFAULTFLAG =? OR ? IS NULL) ");

		Object[] bindArray = new Object[]{materialSpecName,batchNo,batchNo};
		
		List<ListOrderedMap> result = null;

		try
		{
			result = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindArray);
		}
		catch (FrameworkErrorSignal fe)
		{
			result = null;
		}
		if(result!=null&&result.size()>0) return true;
		else return false;
	}
	
	public List<String> getERPBackUpGroup(String factoryName, String productRequestName, String processOperationName, String processOperationVer, String productSpecName)
	{
		List<String> backUpGroup=new ArrayList<String>();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT DISTINCT SUBSTITUTEGROUP  ");
		sql.append("  FROM CT_ERPBOM ");
		sql.append(" WHERE FACTORYNAME = ? ");
		sql.append("   AND PRODUCTREQUESTNAME = ? ");
		sql.append("   AND PROCESSOPERATIONNAME = ? ");
		sql.append("   AND PROCESSOPERATIONVERSION = ? ");
		sql.append("   AND LENGTH(MATERIALSPECNAME )<16 ");
		sql.append("   AND KITFLAG='Y' ");

		Object[] bindArray = new Object[]{factoryName, productRequestName, processOperationName, processOperationVer};
		
		List<Map<String, Object>> result = null;

		try
		{
			result = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindArray);
			for(int i=0;i<result.size();i++)
			{
				backUpGroup.add(result.get(i).get("SUBSTITUTEGROUP").toString());
			}
		}
		catch (FrameworkErrorSignal fe)
		{
			result = null;
		}
		return backUpGroup;
	}
	
	public List<ListOrderedMap> getERPBOMMaterialSpec(String factoryName, String productRequestName, String processOperationName, String processOperationVer, String consumeUnit, String materialSpecName)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT * FROM CT_ERPBOM WHERE FACTORYNAME = ? AND PRODUCTREQUESTNAME = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND CONSUMEUNIT = ? AND MATERIALSPECNAME = ? ");

		Object[] bindArray = new Object[]{factoryName, productRequestName, processOperationName, processOperationVer, consumeUnit, materialSpecName};
		
		List<ListOrderedMap> result = null;

		try
		{
			result = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindArray);
		}
		catch (FrameworkErrorSignal fe)
		{
			result = null;
		}
		return result;
	}
	
	public void compareERPBOM(String factoryName, String productRequestName, String processOperationName, String processOperationVer, String machineName, String productSpecName) throws CustomException
	{
		List<Consumable> kiettedConsumableDataList = null;
		try
		{ 
			String condition = " WHERE MATERIALLOCATIONNAME LIKE ? AND CONSUMABLESTATE = 'InUse'";
			kiettedConsumableDataList = ConsumableServiceProxy.getConsumableService().select(condition, new Object[] { machineName + "%" });
		}
		catch (Exception ex)
		{}
		
		if(kiettedConsumableDataList != null && kiettedConsumableDataList.size() > 0)
		{
			List<ListOrderedMap> ERPBOMList =  getERPBOMMaterialSpec(factoryName, productRequestName, processOperationName, processOperationVer, productSpecName);
			
			if(ERPBOMList != null && ERPBOMList.size() > 0)
			{
				//productSpecName = productSpecName.substring(0, productSpecName.length() - 1);
				
				for(Consumable consumableData : kiettedConsumableDataList)
				{
					boolean isFound = false;
					
					for(ListOrderedMap ERPBOM : ERPBOMList)
					{
						String materialSpecName = CommonUtil.getValue(ERPBOM, "MATERIALSPECNAME");
						
						if(StringUtil.equals(consumableData.getConsumableSpecName(), materialSpecName))
						{
							isFound = true;
							break;
						}
					}
					
					if(!isFound)
					{
						throw new CustomException("MATERIAL-0013");
					}
				}
			}
			else
			{
				throw new CustomException("MATERIAL-0039");
			}
		}
		
		/*
		List<ListOrderedMap> ERPBOMList =  getERPBOMMaterialSpec(factoryName, productRequestName, processOperationName, processOperationVer, productSpecName);
		
		if(ERPBOMList != null && ERPBOMList.size() > 0)
		{
			productSpecName = productSpecName.substring(0, productSpecName.length() - 1);
			
			List<Consumable> kiettedConsumableDataList = null;
			try
			{
				String condition = " WHERE MATERIALLOCATIONNAME LIKE ? AND CONSUMABLESTATE = 'InUse'";
				kiettedConsumableDataList = ConsumableServiceProxy.getConsumableService().select(condition, new Object[] { machineName + "%" });
			}
			catch (Exception ex)
			{}
			
			for(ListOrderedMap ERPBOM : ERPBOMList)
			{
				String materialSpecName = CommonUtil.getValue(ERPBOM, "MATERIALSPECNAME");
				//String materialSpecVersion = CommonUtil.getValue(ERPBOM, "MATERIALSPECVERSION");
				//int quantity = Integer.parseInt(CommonUtil.getValue(ERPBOM, "QUANTITY"));
				//String consumeUnit = CommonUtil.getValue(ERPBOM, "CONSUMEUNIT");
				//String factoryCode = CommonUtil.getValue(ERPBOM, "FACTORYCODE");
				//String factoryPosition = CommonUtil.getValue(ERPBOM, "FACTORYPOSITION");
				
				boolean isFound = false;
				
				if(kiettedConsumableDataList == null)
				{
					throw new CustomException("MATERIAL-0004");
				}
				else
				{
					for(Consumable consumableData : kiettedConsumableDataList)
					{
						if(StringUtil.equals(consumableData.getConsumableSpecName(), materialSpecName))
						{
							isFound = true;
							break;
						}
					}
				}
				
				if(!isFound)
				{
					throw new CustomException("MATERIAL-0013");
				}
			}
		}
		*/
	}
	
	public void compareERPBOMForOnline(String factoryName, String productRequestName, String processOperationName, String processOperationVer, String machineName, String productSpecName) throws CustomException
	{
		List<ListOrderedMap> ERPBOMList =  getERPBOMMaterialSpec(factoryName, productRequestName, processOperationName, processOperationVer, productSpecName);
		
		if(ERPBOMList != null && ERPBOMList.size() > 0)
		{
			productSpecName = productSpecName.substring(0, productSpecName.length() - 1);
			
			List<Consumable> kiettedConsumableDataList = null;
			try
			{
				String condition = " WHERE MATERIALLOCATIONNAME LIKE ? AND CONSUMABLESTATE = 'InUse'";
				kiettedConsumableDataList = ConsumableServiceProxy.getConsumableService().select(condition, new Object[] { machineName + "%" });
			}
			catch (Exception ex)
			{}
			
			for(ListOrderedMap ERPBOM : ERPBOMList)
			{
				String materialSpecName = CommonUtil.getValue(ERPBOM, "MATERIALSPECNAME");
				//String materialSpecVersion = CommonUtil.getValue(ERPBOM, "MATERIALSPECVERSION");
				//int quantity = Integer.parseInt(CommonUtil.getValue(ERPBOM, "QUANTITY"));
				//String consumeUnit = CommonUtil.getValue(ERPBOM, "CONSUMEUNIT");
				//String factoryCode = CommonUtil.getValue(ERPBOM, "FACTORYCODE");
				//String factoryPosition = CommonUtil.getValue(ERPBOM, "FACTORYPOSITION");
				
				boolean isFound = false;
				
				if(kiettedConsumableDataList == null)
				{
					throw new CustomException("MATERIAL-0004");
				}
				else
				{
					for(Consumable consumableData : kiettedConsumableDataList)
					{
						if(StringUtil.equals(consumableData.getConsumableSpecName(), materialSpecName))
						{
							isFound = true;
							break;
						}
					}
				}
				
				if(!isFound)
				{
					throw new CustomException("MATERIAL-0013");
				}
			}
		}
	}
	
	public void trackOutERPBOMReport(EventInfo eventInfo, Lot lotData, SuperProductRequest productRequestInfo, String machineName, int trackOutQuantity) throws CustomException
	{
		List<ListOrderedMap> ERPBOMList = getERPBOMMaterialSpec(lotData.getFactoryName(), productRequestInfo.getProductRequestName(), lotData.getProcessOperationName(),
				lotData.getProcessOperationVersion(), lotData.getProductSpecName());

		if(ERPBOMList != null && ERPBOMList.size() > 0)
		{
			for(ListOrderedMap ERPBOM : ERPBOMList)
			{
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				
				String materialSpecName = CommonUtil.getValue(ERPBOM, "MATERIALSPECNAME");
				String processOperationName = CommonUtil.getValue(ERPBOM, "PROCESSOPERATIONNAME");
				double quantity = Double.parseDouble(CommonUtil.getValue(ERPBOM, "QUANTITY"));
				//String consumeUnit = CommonUtil.getValue(ERPBOM, "CONSUMEUNIT");
				//String factoryCode = CommonUtil.getValue(ERPBOM, "FACTORYCODE");
				//String factoryPosition = CommonUtil.getValue(ERPBOM, "FACTORYPOSITION");
				
				ConsumableSpecKey specKey = new ConsumableSpecKey();
				specKey.setFactoryName(lotData.getFactoryName());
				specKey.setConsumableSpecName(materialSpecName);
				specKey.setConsumableSpecVersion("00001");
				
				ConsumableSpec consumableSpecData = ConsumableServiceProxy.getConsumableSpecService().selectByKey(specKey);
				
				List<Consumable> kiettedConsumableDataList = null;
				try
				{
					String condition = " WHERE MATERIALLOCATIONNAME LIKE ? AND CONSUMABLESTATE = 'InUse' AND CONSUMABLESPECNAME = ?";
					kiettedConsumableDataList = ConsumableServiceProxy.getConsumableService().select(condition, new Object[] { machineName + "%", materialSpecName });
				}
				catch (Exception ex)
				{
					throw new CustomException("MATERIAL-0004");
				}
				
				Consumable useConsumable = kiettedConsumableDataList.get(0);
				
				List<Map<String, String>> ERPReportList = new ArrayList<Map<String, String>>();
				Map<String, String> ERPInfo = new HashMap<>();
				
				ERPInfo.put("ZID", eventInfo.getEventTimeKey());
				ERPInfo.put("AUFNR", productRequestInfo.getProductRequestName());
				ERPInfo.put("MATNR", materialSpecName);
				ERPInfo.put("SORTF", processOperationName);
				ERPInfo.put("ENMNG", Double.toString(trackOutQuantity * quantity));
				ERPInfo.put("MEIN", consumableSpecData.getConsumeUnit());
				ERPInfo.put("WERKS", useConsumable.getUdfs().get("WMSFACTORYNAME"));
				ERPInfo.put("LGORT", useConsumable.getUdfs().get("WMSFACTORYPOSITION"));
				ERPInfo.put("CHARG", useConsumable.getUdfs().get("BATCHNO"));
				ERPInfo.put("ZMOVE", Integer.toString(trackOutQuantity));
				
				Calendar cal = Calendar.getInstance();
				int hour = cal.get(Calendar.HOUR_OF_DAY);
				if(hour >= 20)
				{
					cal.set(Calendar.HOUR_OF_DAY, 0);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MILLISECOND, 0);
					cal.add(Calendar.DAY_OF_MONTH, 1);
					Timestamp receiveTime = new Timestamp(cal.getTime().getTime());
					ERPInfo.put("ZDTIME", receiveTime.toString());
				}
				else
				{
					ERPInfo.put("ZDTIME", eventInfo.getEventTime().toString());
				}
				
				ERPInfo.put("ZUNRECEIVE", "");
				ERPInfo.put("ZIMMED", "");
				ERPReportList.add(ERPInfo);
				
				eventInfo.setEventName("TrackOut");
				
				//Send
				try
				{
					ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().sapVXG372Send(eventInfo, ERPReportList, 1, consumableSpecData.getConsumeUnit());
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			
		}
	}
	
	public void trackOutERPBOMReportForOnline(EventInfo eventInfo, Lot lotData, SuperProductRequest productRequestInfo, String machineName, int trackOutQuantity) throws CustomException
	{
		if(lotData != null)
		{
			List<ListOrderedMap> ERPBOMList = getERPBOMMaterialSpec(lotData.getFactoryName(), productRequestInfo.getProductRequestName(), lotData.getProcessOperationName(),
					lotData.getProcessOperationVersion(), lotData.getProductSpecName());

			if(ERPBOMList != null && ERPBOMList.size() > 0)
			{
				for(ListOrderedMap ERPBOM : ERPBOMList)
				{
					eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
					
					String materialSpecName = CommonUtil.getValue(ERPBOM, "MATERIALSPECNAME");
					String processOperationName = CommonUtil.getValue(ERPBOM, "PROCESSOPERATIONNAME");
					int quantity = Integer.parseInt(CommonUtil.getValue(ERPBOM, "QUANTITY"));
					//String consumeUnit = CommonUtil.getValue(ERPBOM, "CONSUMEUNIT");
					//String factoryCode = CommonUtil.getValue(ERPBOM, "FACTORYCODE");
					//String factoryPosition = CommonUtil.getValue(ERPBOM, "FACTORYPOSITION");
					
					ConsumableSpecKey specKey = new ConsumableSpecKey();
					specKey.setFactoryName(lotData.getFactoryName());
					specKey.setConsumableSpecName(materialSpecName);
					specKey.setConsumableSpecVersion("00001");
					
					ConsumableSpec consumableSpecData = ConsumableServiceProxy.getConsumableSpecService().selectByKey(specKey);
					
					List<MaterialProduct> consumedMaterialList = null;
					try
					{
						Timestamp trackInTime = lotData.getLastLoggedInTime();
						String trackInTimeKey = TimeStampUtil.getEventTimeKeyFromTimestamp(trackInTime);
						
						String condition = " WHERE TIMEKEY BETWEEN ? AND ? AND LOTNAME = ? AND PROCESSOPERATIONNAME = ? AND MATERIALTYPE = ? ";
						consumedMaterialList = ExtendedObjectProxy.getMaterialProductService().select(condition, new Object[] { trackInTimeKey, eventInfo.getEventTimeKey(), lotData.getKey().getLotName(), processOperationName, consumableSpecData.getConsumableType() });
					}
					catch (Exception ex)
					{
						throw new CustomException("MATERIAL-0004");
					}
					
					List<String> consumableNameList = new ArrayList<String>();
					
					for(MaterialProduct materialProductInfo : consumedMaterialList)
					{
						if(consumableNameList == null || consumableNameList.size() == 0)
						{
							consumableNameList.add(materialProductInfo.getMaterialName());
						}
						else
						{
							boolean isFound = false;
							for(String consumableName : consumableNameList)
							{
								if(StringUtil.equals(consumableName, materialProductInfo.getMaterialName()))
								{
									isFound = true;
									break;
								}
							}
							
							if(!isFound)
							{
								consumableNameList.add(materialProductInfo.getMaterialName());
							}
						}
					}
					
					for(String consumableName : consumableNameList)
					{
						Consumable useConsumable = ConsumableServiceProxy.getConsumableService().selectByKey(new ConsumableKey(consumableName));
						
						int count = 0;
						
						for(MaterialProduct materialProductInfo : consumedMaterialList)
						{
							if(StringUtil.equals(materialProductInfo.getMaterialName(), consumableName))
							{
								count++;
							}
						}
						
						List<Map<String, String>> ERPReportList = new ArrayList<Map<String, String>>();
						Map<String, String> ERPInfo = new HashMap<>();
						
						ERPInfo.put("ZID", eventInfo.getEventTimeKey());
						ERPInfo.put("AUFNR", productRequestInfo.getProductRequestName());
						ERPInfo.put("MATNR", materialSpecName);
						ERPInfo.put("SORTF", processOperationName);
						ERPInfo.put("ENMNG", Integer.toString(count * quantity));
						ERPInfo.put("MEIN", consumableSpecData.getConsumeUnit());
						ERPInfo.put("WERKS", useConsumable.getUdfs().get("WMSFACTORYNAME"));
						ERPInfo.put("LGORT", useConsumable.getUdfs().get("WMSFACTORYPOSITION"));
						ERPInfo.put("CHARG", useConsumable.getUdfs().get("BATCHNO"));
						ERPInfo.put("ZMOVE", Integer.toString(count));
						
						Calendar cal = Calendar.getInstance();
						int hour = cal.get(Calendar.HOUR_OF_DAY);
						if(hour >= 20)
						{
							cal.set(Calendar.HOUR_OF_DAY, 0);
							cal.set(Calendar.MINUTE, 0);
							cal.set(Calendar.SECOND, 0);
							cal.set(Calendar.MILLISECOND, 0);
							cal.add(Calendar.DAY_OF_MONTH, 1);
							Timestamp receiveTime = new Timestamp(cal.getTime().getTime());
							ERPInfo.put("ZDTIME", receiveTime.toString());
						}
						else
						{
							ERPInfo.put("ZDTIME", eventInfo.getEventTime().toString());
						}
						
						ERPInfo.put("ZUNRECEIVE", "");
						ERPInfo.put("ZIMMED", "");
						ERPReportList.add(ERPInfo);
						
						eventInfo.setEventName("TrackOut");
						
						//Send
						try
						{
							ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().sapVXG372Send(eventInfo, ERPReportList, 1, consumableSpecData.getConsumeUnit());
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
				
			}
		}
	}
	
	public void trackOutERPBOMReportForSort(EventInfo eventInfo, Lot lotData, SuperProductRequest productRequestInfo, String machineName, int trackOutQuantity, String Oper) throws CustomException
	{
		if(productRequestInfo.getSubProductionType().equals("SYZLC"))
		{
			return;
		}
		List<ListOrderedMap> ERPBOMList = getERPBOMMaterialSpec(lotData.getFactoryName(), productRequestInfo.getProductRequestName(),Oper, "00001",
				productRequestInfo.getProductSpecName());

		if(ERPBOMList != null && ERPBOMList.size() > 0)
		{
			List<String> backUpGroup = getERPBackUpGroup(lotData.getFactoryName(), productRequestInfo.getProductRequestName(),Oper, "00001",
					productRequestInfo.getProductSpecName());
			String factoryCode="5001";
			String factoryPosition="";
			String getFactoryPosition = " SELECT DESCRIPTION FROM ENUMDEFVALUE WHERE ENUMNAME=:ENUMNAME AND ENUMVALUE=:ENUMVALUE ";
			
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("ENUMNAME", "FactoryPosition");
			bindMap.put("ENUMVALUE", productRequestInfo.getFactoryName());
			
			List<Map<String, Object>> factoryPositionList = GenericServiceProxy.getSqlMesTemplate().queryForList(getFactoryPosition, bindMap);			
			if(factoryPositionList.size() > 0){
				factoryPosition = factoryPositionList.get(0).get("DESCRIPTION").toString();
			}
			else
			{
				factoryPosition="";
			}					
			for(ListOrderedMap ERPBOM : ERPBOMList)
			{
				ConsumableSpec consumableSpec = null;
				String consumableType="";
				boolean gasFlag=false;
				try
				{
					consumableSpec = GenericServiceProxy.getSpecUtil().getMaterialSpec(CommonUtil.getValue(ERPBOM, "FACTORYNAME"), CommonUtil.getValue(ERPBOM, "MATERIALSPECNAME"), "00001");
					consumableType=consumableSpec.getConsumableType();
					if(StringUtils.equals(consumableType, "Crate")||StringUtils.equals(consumableType, "TopLamination")
							||StringUtils.equals(consumableType, "BottomLamination")||StringUtils.equals(consumableType, "PatternFilm")
							||StringUtils.equals(consumableType, "Organic")||StringUtils.equals(consumableType, "InOrganic") )
					{
						continue;
					}
					if(StringUtils.equals(consumableType, "CH3COOH")||StringUtils.equals(consumableType, "NMP")
							||StringUtils.equals(consumableType, "Other")||StringUtils.equals(consumableType, "HF")
							||StringUtils.equals(consumableType, "Diluent")||StringUtils.equals(consumableType, "Detergent")
							||StringUtils.equals(consumableType, "Developing Liquid")||StringUtils.equals(consumableType, "HN03")
							||StringUtils.equals(consumableType, "EtchingLiquid")||StringUtils.equals(consumableType, "Liquid-Cleaner")
							||StringUtils.equals(consumableType, "Gas"))
					{
						gasFlag=true;
					}
				}
				catch(CustomException c)
				{
					log.info("Not Fount ConsumeSpec:"+ CommonUtil.getValue(ERPBOM, "MATERIALSPECNAME"));
				}


				if(StringUtils.isNotEmpty(CommonUtil.getValue(ERPBOM, "SUBSTITUTEGROUP")))
				{
					if(backUpGroup.contains(CommonUtil.getValue(ERPBOM, "SUBSTITUTEGROUP"))
							&&(!StringUtils.equals(CommonUtil.getValue(ERPBOM, "KITFLAG"), "Y")))
					{							
						log.info("Consumable have KITFLAG ,but KITFLAG is not Y");
						continue;
					}
					else if (!backUpGroup.contains(CommonUtil.getValue(ERPBOM, "SUBSTITUTEGROUP"))
							&&(!StringUtils.equals(CommonUtil.getValue(ERPBOM, "VIRTUALFLAG"), "01")))
					{
						log.info("Consumable not have KITFLAG,VIRTUALFLAG is not 1");
						continue;
					}
				}
				else
				{
					log.info("Consumable not have backUpMaterial");
				}
            	if(getERPSkipMaterialList(CommonUtil.getValue(ERPBOM, "MATERIALSPECNAME"), ""))
            	{
            		continue;
            	}
				List<Map<String, String>> ERPReportList = new ArrayList<Map<String, String>>();
    			Map<String, String> ERPInfo = new HashMap<>();
				ERPInfo.put("SEQ",TimeStampUtil.getCurrentEventTimeKey());
    			ERPInfo.put("PRODUCTREQUESTNAME", productRequestInfo.getProductRequestName());
    			ERPInfo.put("MATERIALSPECNAME",CommonUtil.getValue(ERPBOM, "MATERIALSPECNAME"));
    			ERPInfo.put("PROCESSOPERATIONNAME", Oper);
	    		ERPInfo.put("QUANTITY",String.format("%.9f", trackOutQuantity* Double.parseDouble(CommonUtil.getValue(ERPBOM, "QUANTITY"))));
    			ERPInfo.put("CONSUMEUNIT", CommonUtil.getValue(ERPBOM, "CONSUMEUNIT"));
    			ERPInfo.put("FACTORYCODE",factoryCode );
    			if(gasFlag)
    			{
	    			ERPInfo.put("FACTORYPOSITION", "5F02");
    			}
    			else
    			{
	    			ERPInfo.put("FACTORYPOSITION", factoryPosition);
    			}
    			ERPInfo.put("BATCHNO", "");
    			ERPInfo.put("PRODUCTQUANTITY", String.valueOf(trackOutQuantity));
    			
    			Calendar cal = Calendar.getInstance();
    			int hour = cal.get(Calendar.HOUR_OF_DAY);
    			if(hour >= 19)
    			{
    				cal.set(Calendar.HOUR_OF_DAY, 0);
    				cal.set(Calendar.MINUTE, 0);
    				cal.set(Calendar.SECOND, 0);
    				cal.set(Calendar.MILLISECOND, 0);
    				cal.add(Calendar.DAY_OF_MONTH, 1);
    				Timestamp receiveTime = new Timestamp(cal.getTime().getTime());
    				ERPInfo.put("EVENTTIME", receiveTime.toString().replace("-","").substring(0,8));
    			}
    			else
    			{
    				ERPInfo.put("EVENTTIME", eventInfo.getEventTimeKey().substring(0,8));
    			}
    			
    			ERPInfo.put("CANCELFLAG", "");
    			ERPInfo.put("WSFLAG", "");
    			ERPReportList.add(ERPInfo);
    			
    			eventInfo.setEventName("TrackOut");
    			
    			//Send
    			try
    			{
    				ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().sapVXG372Send(eventInfo, ERPReportList, 1, CommonUtil.getValue(ERPBOM, "CONSUMEUNIT"));
    			}
    			catch (Exception e)
    			{
    				e.printStackTrace();
    			}

				
			}
			
		}
		else
		{
			return;
		}
	}
	
	public void trackOutERPBOMReportForTrayGroup(EventInfo eventInfo, Lot lotData, SuperProductRequest productRequestInfo, String machineName, int trackOutQuantity, List<Map<String, Object>> panelList) throws CustomException
	{
		if(productRequestInfo.getSubProductionType().equals("SYZLC"))
		{
			return;
		}
		String Oper="";
		if(StringUtils.equals(lotData.getProcessOperationName(), "36010")||StringUtils.equals(lotData.getProcessOperationName(), "36030"))
			Oper=lotData.getProcessOperationName();
		else if(StringUtils.equals(lotData.getUdfs().get("BEFOREOPERATIONNAME"), "32001"))
		{
			Oper="32000";
		}
		else Oper=lotData.getUdfs().get("BEFOREOPERATIONNAME");
		List<ListOrderedMap> ERPBOMList = getERPBOMMaterialSpec(lotData.getFactoryName(), productRequestInfo.getProductRequestName(),Oper, "00001",
				productRequestInfo.getProductSpecName());

		if(ERPBOMList != null && ERPBOMList.size() > 0)
		{
			List<String> backUpGroup = getERPBackUpGroup(lotData.getFactoryName(), productRequestInfo.getProductRequestName(),Oper, "00001",
					productRequestInfo.getProductSpecName());
			String factoryCode="5001";
			String factoryPosition="";
			String getFactoryPosition = " SELECT DESCRIPTION FROM ENUMDEFVALUE WHERE ENUMNAME=:ENUMNAME AND ENUMVALUE=:ENUMVALUE ";
			
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("ENUMNAME", "FactoryPosition");
			bindMap.put("ENUMVALUE", productRequestInfo.getFactoryName());
			
			List<Map<String, Object>> factoryPositionList = GenericServiceProxy.getSqlMesTemplate().queryForList(getFactoryPosition, bindMap);			
			if(factoryPositionList.size() > 0){
				factoryPosition = factoryPositionList.get(0).get("DESCRIPTION").toString();
			}
			else
			{
				factoryPosition="";
			}					
			for(ListOrderedMap ERPBOM : ERPBOMList)
			{
				StringBuilder sql = new StringBuilder();
				sql.append(" SELECT COUNT(A.PRODUCTNAME) AS QTY,C.CONSUMABLENAME,C.CONSUMABLESPECNAME,C.BATCHNO,D.CONSUMABLETYPE ");
				sql.append("  FROM CT_MATERIALPRODUCT  A,LOT B,CONSUMABLE C,CONSUMABLESPEC D ");
				sql.append("  WHERE     A.LOTNAME = B.LOTNAME ");
				sql.append("        AND A.MATERIALNAME = C.CONSUMABLENAME ");
				sql.append("        AND D.CONSUMABLESPECNAME = C.CONSUMABLESPECNAME ");
				sql.append("        AND C.FACTORYNAME = D.FACTORYNAME ");
				sql.append("        AND A.PRODUCTNAME =:LOTNAME ");
				sql.append("        AND A.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
			    //sql.append("        AND A.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
				sql.append("        AND A.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
				sql.append("        AND A.MACHINENAME =:MACHINENAME ");
				sql.append("        AND D.CONSUMABLESPECNAME =:CONSUMABLESPECNAME ");
				sql.append("        AND D.CONSUMABLETYPE IN ('InOrganic','Organic','TopLamination','BottomLamination','Crate','PatternFilm') ");
				sql.append("        AND A.EVENTTIME> (CASE WHEN A.MATERIALTYPE='Crate' THEN B.CREATETIME ELSE B.LASTLOGGEDINTIME END) ");
				sql.append("        GROUP BY C.CONSUMABLESPECNAME,C.CONSUMABLENAME,C.BATCHNO,D.CONSUMABLETYPE ");

				Map<String, Object> args = new HashMap<String, Object>();
				args.put("LOTNAME", lotData.getKey().getLotName());
				args.put("PRODUCTSPECNAME", lotData.getProductSpecName());
				//args.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
				args.put("PROCESSOPERATIONNAME", lotData.getUdfs().get("BEFOREOPERATIONNAME"));
				args.put("MACHINENAME", machineName);
				args.put("CONSUMABLESPECNAME", CommonUtil.getValue(ERPBOM, "MATERIALSPECNAME"));
				List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
				if(sqlResult.size()>0)
				{
		            for(int i=0;i<sqlResult.size();i++)
		            {
		            	if(getERPSkipMaterialList(sqlResult.get(i).get("CONSUMABLESPECNAME").toString(), sqlResult.get(i).get("BATCHNO").toString()))
		            	{
		            		continue;
		            	}
		    			List<Map<String, String>> ERPReportList = new ArrayList<Map<String, String>>();
		    			Map<String, String> ERPInfo = new HashMap<>();
		    			
		    			ERPInfo.put("SEQ",TimeUtils.getCurrentEventTimeKey());
		    			ERPInfo.put("PRODUCTREQUESTNAME", productRequestInfo.getProductRequestName());
		    			ERPInfo.put("MATERIALSPECNAME", sqlResult.get(i).get("CONSUMABLESPECNAME").toString());
		    			ERPInfo.put("PROCESSOPERATIONNAME",Oper);
		    			if((!StringUtil.equals(sqlResult.get(i).get("CONSUMABLETYPE").toString(), "InOrganic"))
		    					&&(!StringUtil.equals(sqlResult.get(i).get("CONSUMABLETYPE").toString(), "Organic")))
		    			{
		    				ERPInfo.put("QUANTITY", String.valueOf(trackOutQuantity));
		    			}
		    			else
		    			{
			    			ERPInfo.put("QUANTITY",String.format("%.9f", (Double.parseDouble(sqlResult.get(i).get("QTY").toString())* Double.parseDouble(CommonUtil.getValue(ERPBOM, "QUANTITY")))));
		    			}
		    			ERPInfo.put("CONSUMEUNIT", CommonUtil.getValue(ERPBOM, "CONSUMEUNIT"));
		    			ERPInfo.put("FACTORYCODE",factoryCode );
		    			ERPInfo.put("FACTORYPOSITION", factoryPosition);
		    			ERPInfo.put("BATCHNO", "");
		    			ERPInfo.put("PRODUCTQUANTITY", Integer.toString(trackOutQuantity));
		    			
		    			Calendar cal = Calendar.getInstance();
		    			int hour = cal.get(Calendar.HOUR_OF_DAY);
		    			if(hour >= 19)
		    			{
		    				cal.set(Calendar.HOUR_OF_DAY, 0);
		    				cal.set(Calendar.MINUTE, 0);
		    				cal.set(Calendar.SECOND, 0);
		    				cal.set(Calendar.MILLISECOND, 0);
		    				cal.add(Calendar.DAY_OF_MONTH, 1);
		    				Timestamp receiveTime = new Timestamp(cal.getTime().getTime());
		    				ERPInfo.put("EVENTTIME", receiveTime.toString().replace("-","").substring(0,8));
		    			}
		    			else
		    			{
		    				ERPInfo.put("EVENTTIME", eventInfo.getEventTimeKey().substring(0,8));
		    			}
		    			
		    			ERPInfo.put("CANCELFLAG", "");
		    			ERPInfo.put("WSFLAG", "");
		    			ERPReportList.add(ERPInfo);
		    			
		    			eventInfo.setEventName("TrackOut");
		    			
		    			//Send
		    			try
		    			{
		    				ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().sapVXG372Send(eventInfo, ERPReportList, 1, CommonUtil.getValue(ERPBOM, "CONSUMEUNIT"));
		    			}
		    			catch (Exception e)
		    			{
		    				e.printStackTrace();
		    			}
		            }
				}
				else 
				{
					ConsumableSpec consumableSpec = null;
					String consumableType="";
					boolean gasFlag=false;
					try
					{
						consumableSpec = GenericServiceProxy.getSpecUtil().getMaterialSpec(CommonUtil.getValue(ERPBOM, "FACTORYNAME"), CommonUtil.getValue(ERPBOM, "MATERIALSPECNAME"), "00001");
						consumableType=consumableSpec.getConsumableType();
						if(StringUtils.equals(consumableType, "Crate")||StringUtils.equals(consumableType, "TopLamination")
								||StringUtils.equals(consumableType, "BottomLamination")||StringUtils.equals(consumableType, "PatternFilm")
								||StringUtils.equals(consumableType, "Organic")||StringUtils.equals(consumableType, "InOrganic") )
						{
							continue;
						}
						if(StringUtils.equals(consumableType, "CH3COOH")||StringUtils.equals(consumableType, "NMP")
								||StringUtils.equals(consumableType, "Other")||StringUtils.equals(consumableType, "HF")
								||StringUtils.equals(consumableType, "Diluent")||StringUtils.equals(consumableType, "Detergent")
								||StringUtils.equals(consumableType, "Developing Liquid")||StringUtils.equals(consumableType, "HN03")
								||StringUtils.equals(consumableType, "EtchingLiquid")||StringUtils.equals(consumableType, "Liquid-Cleaner")
								||StringUtils.equals(consumableType, "Gas"))
						{
							gasFlag=true;
						}
						if(StringUtils.equals(Oper, "32000"))
						{
							continue;
						}
					}
					catch(CustomException c)
					{
						log.info("Not Fount ConsumeSpec:"+ CommonUtil.getValue(ERPBOM, "MATERIALSPECNAME"));
					}


					if(StringUtils.isNotEmpty(CommonUtil.getValue(ERPBOM, "SUBSTITUTEGROUP")))
					{
						if(backUpGroup.contains(CommonUtil.getValue(ERPBOM, "SUBSTITUTEGROUP"))
								&&(!StringUtils.equals(CommonUtil.getValue(ERPBOM, "KITFLAG"), "Y")))
						{							
							log.info("Consumable have KITFLAG ,but KITFLAG is not Y");
							continue;
						}
						else if (!backUpGroup.contains(CommonUtil.getValue(ERPBOM, "SUBSTITUTEGROUP"))
								&&(!StringUtils.equals(CommonUtil.getValue(ERPBOM, "VIRTUALFLAG"), "01")))
						{
							log.info("Consumable not have KITFLAG,VIRTUALFLAG is not 1");
							continue;
						}
					}
					else
					{
						log.info("Consumable not have backUpMaterial");
					}
	            	if(getERPSkipMaterialList(CommonUtil.getValue(ERPBOM, "MATERIALSPECNAME"), ""))
	            	{
	            		continue;
	            	}
					List<Map<String, String>> ERPReportList = new ArrayList<Map<String, String>>();
	    			Map<String, String> ERPInfo = new HashMap<>();
					ERPInfo.put("SEQ",TimeStampUtil.getCurrentEventTimeKey());
	    			ERPInfo.put("PRODUCTREQUESTNAME", productRequestInfo.getProductRequestName());
	    			ERPInfo.put("MATERIALSPECNAME",CommonUtil.getValue(ERPBOM, "MATERIALSPECNAME"));
	    			ERPInfo.put("PROCESSOPERATIONNAME", Oper);
		    		ERPInfo.put("QUANTITY",String.format("%.9f", trackOutQuantity* Double.parseDouble(CommonUtil.getValue(ERPBOM, "QUANTITY"))));
	    			ERPInfo.put("CONSUMEUNIT", CommonUtil.getValue(ERPBOM, "CONSUMEUNIT"));
	    			ERPInfo.put("FACTORYCODE",factoryCode );
	    			if(gasFlag)
	    			{
		    			ERPInfo.put("FACTORYPOSITION", "5F02");
	    			}
	    			else
	    			{
		    			ERPInfo.put("FACTORYPOSITION", factoryPosition);
	    			}
	    			ERPInfo.put("BATCHNO", "");
	    			ERPInfo.put("PRODUCTQUANTITY", String.valueOf(trackOutQuantity));
	    			
	    			Calendar cal = Calendar.getInstance();
	    			int hour = cal.get(Calendar.HOUR_OF_DAY);
	    			if(hour >= 19)
	    			{
	    				cal.set(Calendar.HOUR_OF_DAY, 0);
	    				cal.set(Calendar.MINUTE, 0);
	    				cal.set(Calendar.SECOND, 0);
	    				cal.set(Calendar.MILLISECOND, 0);
	    				cal.add(Calendar.DAY_OF_MONTH, 1);
	    				Timestamp receiveTime = new Timestamp(cal.getTime().getTime());
	    				ERPInfo.put("EVENTTIME", receiveTime.toString().replace("-","").substring(0,8));
	    			}
	    			else
	    			{
	    				ERPInfo.put("EVENTTIME", eventInfo.getEventTimeKey().substring(0,8));
	    			}
	    			
	    			ERPInfo.put("CANCELFLAG", "");
	    			ERPInfo.put("WSFLAG", "");
	    			ERPReportList.add(ERPInfo);
	    			
	    			eventInfo.setEventName("TrackOut");
	    			
	    			//Send
	    			try
	    			{
	    				ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().sapVXG372Send(eventInfo, ERPReportList, 1, CommonUtil.getValue(ERPBOM, "CONSUMEUNIT"));
	    			}
	    			catch (Exception e)
	    			{
	    				e.printStackTrace();
	    			}
				}
			}
			
		}
		else
		{
			return;
		}
	}
	public void TrackOutERPBOMReportByMaterialProduct(EventInfo eventInfo, Lot lotData, SuperProductRequest productRequestInfo, String machineName,int productQty) throws CustomException
	{
		if(productRequestInfo.getSubProductionType().equals("SYZLC"))
		{
			return;
		}
		if(lotData==null)return;
		
		@SuppressWarnings("unchecked")
		List<ListOrderedMap> ERPBOMList = getERPBOMMaterialSpec(lotData.getFactoryName(), productRequestInfo.getProductRequestName(), lotData.getProcessOperationName(),
				lotData.getProcessOperationVersion(), productRequestInfo.getProductSpecName());

		if(ERPBOMList != null && ERPBOMList.size() > 0)
		{
			List<String> backUpGroup = getERPBackUpGroup(lotData.getFactoryName(), productRequestInfo.getProductRequestName(), lotData.getProcessOperationName(),
					lotData.getProcessOperationVersion(), productRequestInfo.getProductSpecName());
			String factoryCode="5001";
			String factoryPosition="";
			String getFactoryPosition = " SELECT DESCRIPTION FROM ENUMDEFVALUE WHERE ENUMNAME=:ENUMNAME AND ENUMVALUE=:ENUMVALUE ";
			
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("ENUMNAME", "FactoryPosition");
			bindMap.put("ENUMVALUE", productRequestInfo.getFactoryName());
			
			List<Map<String, Object>> factoryPositionList = GenericServiceProxy.getSqlMesTemplate().queryForList(getFactoryPosition, bindMap);			
			if(factoryPositionList.size() > 0){
				factoryPosition = factoryPositionList.get(0).get("DESCRIPTION").toString();
			}
			else
			{
				factoryPosition="";
			}					
			for(ListOrderedMap ERPBOM : ERPBOMList)
			{
				StringBuilder sql = new StringBuilder();
				sql.append(" SELECT COUNT(A.PRODUCTNAME) AS QTY,C.CONSUMABLENAME,C.CONSUMABLESPECNAME,C.BATCHNO,D.CONSUMABLETYPE ");
				sql.append("  FROM CT_MATERIALPRODUCT  A,LOT B,CONSUMABLE C,CONSUMABLESPEC D ");
				sql.append("  WHERE     A.LOTNAME = B.LOTNAME ");
				sql.append("        AND A.MATERIALNAME = C.CONSUMABLENAME ");
				sql.append("        AND D.CONSUMABLESPECNAME = C.CONSUMABLESPECNAME ");
				sql.append("        AND C.FACTORYNAME = D.FACTORYNAME ");
				sql.append("        AND A.LOTNAME =:LOTNAME ");
				sql.append("        AND A.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
				sql.append("        AND A.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
				sql.append("        AND A.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
				sql.append("        AND A.MACHINENAME =:MACHINENAME ");
				sql.append("        AND D.CONSUMABLESPECNAME =:CONSUMABLESPECNAME ");
				sql.append("        AND D.CONSUMABLETYPE IN ('InOrganic','Organic','TopLamination','BottomLamination','Crate','PatternFilm') ");
				sql.append("        AND A.EVENTTIME> (CASE WHEN A.MATERIALTYPE='Crate' THEN B.CREATETIME ELSE B.LASTLOGGEDINTIME END) ");
				sql.append("        GROUP BY C.CONSUMABLESPECNAME,C.CONSUMABLENAME,C.BATCHNO,D.CONSUMABLETYPE ");

				Map<String, Object> args = new HashMap<String, Object>();
				args.put("LOTNAME", lotData.getKey().getLotName());
				args.put("PRODUCTSPECNAME", lotData.getProductSpecName());
				args.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
				args.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
				args.put("MACHINENAME", machineName);
				args.put("CONSUMABLESPECNAME", CommonUtil.getValue(ERPBOM, "MATERIALSPECNAME"));
				List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
				if(sqlResult.size()>0)
				{
		            for(int i=0;i<sqlResult.size();i++)
		            {
		            	if(getERPSkipMaterialList(sqlResult.get(i).get("CONSUMABLESPECNAME").toString(), sqlResult.get(i).get("BATCHNO").toString()))
		            	{
		            		continue;
		            	}
		    			List<Map<String, String>> ERPReportList = new ArrayList<Map<String, String>>();
		    			Map<String, String> ERPInfo = new HashMap<>();
		    			
		    			ERPInfo.put("SEQ",TimeStampUtil.getCurrentEventTimeKey());
		    			ERPInfo.put("PRODUCTREQUESTNAME", productRequestInfo.getProductRequestName());
		    			ERPInfo.put("MATERIALSPECNAME", sqlResult.get(i).get("CONSUMABLESPECNAME").toString());
		    			ERPInfo.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
		    			if((!StringUtil.equals(sqlResult.get(i).get("CONSUMABLETYPE").toString(), "InOrganic"))
		    					&&(!StringUtil.equals(sqlResult.get(i).get("CONSUMABLETYPE").toString(), "Organic")))
		    			{
		    				ERPInfo.put("QUANTITY", Integer.toString(Integer.parseInt(sqlResult.get(i).get("QTY").toString())));
		    			    ERPInfo.put("PRODUCTQUANTITY", Integer.toString(Integer.parseInt(sqlResult.get(i).get("QTY").toString())));
		    			}
		    			else {
		    				continue;
		    				//ERPInfo.put("QUANTITY",String.format("%.9f", productQty* Double.parseDouble(CommonUtil.getValue(ERPBOM, "QUANTITY"))));
		    			    //ERPInfo.put("PRODUCTQUANTITY", Integer.toString(productQty));
						}
		    			ERPInfo.put("CONSUMEUNIT", CommonUtil.getValue(ERPBOM, "CONSUMEUNIT"));
		    			ERPInfo.put("FACTORYCODE",factoryCode );
		    			ERPInfo.put("FACTORYPOSITION", factoryPosition);
		    			if((StringUtil.equals(sqlResult.get(i).get("CONSUMABLETYPE").toString(), "TopLamination"))
		    					||(StringUtil.equals(sqlResult.get(i).get("CONSUMABLETYPE").toString(), "BottomLamination")))
		    			{
			    			ERPInfo.put("BATCHNO", "");
		    			}
		    			else 
		    			{
		    				ERPInfo.put("BATCHNO", sqlResult.get(i).get("BATCHNO").toString());
		    			}

		    			
		    			Calendar cal = Calendar.getInstance();
		    			int hour = cal.get(Calendar.HOUR_OF_DAY);
		    			if(hour >= 19)
		    			{
		    				cal.set(Calendar.HOUR_OF_DAY, 0);
		    				cal.set(Calendar.MINUTE, 0);
		    				cal.set(Calendar.SECOND, 0);
		    				cal.set(Calendar.MILLISECOND, 0);
		    				cal.add(Calendar.DAY_OF_MONTH, 1);
		    				Timestamp receiveTime = new Timestamp(cal.getTime().getTime());
		    				ERPInfo.put("EVENTTIME", receiveTime.toString().replace("-","").substring(0,8));
		    			}
		    			else
		    			{
		    				ERPInfo.put("EVENTTIME", eventInfo.getEventTimeKey().substring(0,8));
		    			}
		    			
		    			ERPInfo.put("CANCELFLAG", "");
		    			ERPInfo.put("WSFLAG", "");
		    			ERPReportList.add(ERPInfo);
		    			
		    			eventInfo.setEventName("TrackOut");
		    			
		    			//Send
		    			try
		    			{
		    				ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().sapVXG372Send(eventInfo, ERPReportList, 1, CommonUtil.getValue(ERPBOM, "CONSUMEUNIT"));
		    			}
		    			catch (Exception e)
		    			{
		    				e.printStackTrace();
		    			}
		            }
				}
				else 
				{
					ConsumableSpec consumableSpec = null;
					String consumableType="";
					boolean gasFlag=false;
					try
					{
						consumableSpec = GenericServiceProxy.getSpecUtil().getMaterialSpec(CommonUtil.getValue(ERPBOM, "FACTORYNAME"), CommonUtil.getValue(ERPBOM, "MATERIALSPECNAME"), "00001");
						consumableType=consumableSpec.getConsumableType();
						if(StringUtils.equals(consumableType, "Crate")||StringUtils.equals(consumableType, "TopLamination")
								||StringUtils.equals(consumableType, "BottomLamination")||StringUtils.equals(consumableType, "PatternFilm")
								||StringUtils.equals(consumableType, "Organic")||StringUtils.equals(consumableType, "InOrganic") )
						{
							continue;
						}
						if(StringUtils.equals(consumableType, "CH3COOH")||StringUtils.equals(consumableType, "NMP")
								||StringUtils.equals(consumableType, "Other")||StringUtils.equals(consumableType, "HF")
								||StringUtils.equals(consumableType, "Diluent")||StringUtils.equals(consumableType, "Detergent")
								||StringUtils.equals(consumableType, "Developing Liquid")||StringUtils.equals(consumableType, "HN03")
								||StringUtils.equals(consumableType, "EtchingLiquid")||StringUtils.equals(consumableType, "Liquid-Cleaner")
								||StringUtils.equals(consumableType, "Gas"))
						{
							gasFlag=true;
						}
						if(StringUtils.equals(lotData.getProcessOperationName(), "22200")||
								StringUtils.equals(lotData.getProcessOperationName(), "22500")	)
						{
							continue;
						}

					}
					catch(CustomException c)
					{
						log.info("Not Fount ConsumeSpec:"+ CommonUtil.getValue(ERPBOM, "MATERIALSPECNAME"));
					}
					
					if(StringUtils.isNotEmpty(CommonUtil.getValue(ERPBOM, "SUBSTITUTEGROUP")))
					{
						if(backUpGroup.contains(CommonUtil.getValue(ERPBOM, "SUBSTITUTEGROUP"))
								&&(!StringUtils.equals(CommonUtil.getValue(ERPBOM, "KITFLAG"), "Y")))
						{							
							log.info("Consumable have KITFLAG ,but KITFLAG is not Y");
							continue;
						}
						else if (!backUpGroup.contains(CommonUtil.getValue(ERPBOM, "SUBSTITUTEGROUP"))
								&&(!StringUtils.equals(CommonUtil.getValue(ERPBOM, "VIRTUALFLAG"), "01")))
						{
							log.info("Consumable not have KITFLAG,VIRTUALFLAG is not 1");
							continue;
						}
					}
					else
					{
						log.info("Consumable not have backUpMaterial");
					}
	            	if(getERPSkipMaterialList(CommonUtil.getValue(ERPBOM, "MATERIALSPECNAME"), ""))
	            	{
	            		continue;
	            	}
					List<Map<String, String>> ERPReportList = new ArrayList<Map<String, String>>();
	    			Map<String, String> ERPInfo = new HashMap<>();
					ERPInfo.put("SEQ",TimeStampUtil.getCurrentEventTimeKey());
	    			ERPInfo.put("PRODUCTREQUESTNAME", productRequestInfo.getProductRequestName());
	    			ERPInfo.put("MATERIALSPECNAME",CommonUtil.getValue(ERPBOM, "MATERIALSPECNAME"));
	    			ERPInfo.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
		    		ERPInfo.put("QUANTITY",String.format("%.9f", productQty* Double.parseDouble(CommonUtil.getValue(ERPBOM, "QUANTITY"))));
	    			ERPInfo.put("CONSUMEUNIT", CommonUtil.getValue(ERPBOM, "CONSUMEUNIT"));
	    			ERPInfo.put("FACTORYCODE",factoryCode );
	    			if(gasFlag)
	    			{
		    			ERPInfo.put("FACTORYPOSITION", "5F02");
	    			}
	    			else
	    			{
		    			ERPInfo.put("FACTORYPOSITION", factoryPosition);
	    			}
	    			ERPInfo.put("BATCHNO", "");
	    			ERPInfo.put("PRODUCTQUANTITY", String.valueOf(productQty));
	    			
	    			Calendar cal = Calendar.getInstance();
	    			int hour = cal.get(Calendar.HOUR_OF_DAY);
	    			if(hour >= 19)
	    			{
	    				cal.set(Calendar.HOUR_OF_DAY, 0);
	    				cal.set(Calendar.MINUTE, 0);
	    				cal.set(Calendar.SECOND, 0);
	    				cal.set(Calendar.MILLISECOND, 0);
	    				cal.add(Calendar.DAY_OF_MONTH, 1);
	    				Timestamp receiveTime = new Timestamp(cal.getTime().getTime());
	    				ERPInfo.put("EVENTTIME", receiveTime.toString().replace("-","").substring(0,8));
	    			}
	    			else
	    			{
	    				ERPInfo.put("EVENTTIME", eventInfo.getEventTimeKey().substring(0,8));
	    			}
	    			
	    			ERPInfo.put("CANCELFLAG", "");
	    			ERPInfo.put("WSFLAG", "");
	    			ERPReportList.add(ERPInfo);
	    			
	    			eventInfo.setEventName("TrackOut");
	    			
	    			//Send
	    			try
	    			{
	    				ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().sapVXG372Send(eventInfo, ERPReportList, 1, CommonUtil.getValue(ERPBOM, "CONSUMEUNIT"));
	    			}
	    			catch (Exception e)
	    			{
	    				e.printStackTrace();
	    			}
				}
			}
			
		}
		else
		{
			return;
		}
		
	}
	
	public void trackOutERPBOMReportForOnline(EventInfo eventInfo, Lot lotData, SuperProductRequest productRequestInfo, String machineName,List<ProductPGSRC> productPGSRCSequence) throws CustomException
	{
		if(productRequestInfo.getSubProductionType().equals("SYZLC"))
		{
			return;
		}
		if(lotData==null)return;
		int sapReportQty=0;
		
		for(int i=0;i<productPGSRCSequence.size();i++)
		{
			if(CommonUtil.equalsIn(productPGSRCSequence.get(i).getUdfs().get("PROCESSINGINFO"),"N","F","L"))
			{
				sapReportQty++;
			}
		}
		if(sapReportQty==0)return;
		@SuppressWarnings("unchecked")
		List<ListOrderedMap> ERPBOMList = getERPBOMMaterialSpec(lotData.getFactoryName(), productRequestInfo.getProductRequestName(), lotData.getProcessOperationName(),
				lotData.getProcessOperationVersion(), productRequestInfo.getProductSpecName());

		if(ERPBOMList != null && ERPBOMList.size() > 0)
		{
			List<String> backUpGroup = getERPBackUpGroup(lotData.getFactoryName(), productRequestInfo.getProductRequestName(), lotData.getProcessOperationName(),
					lotData.getProcessOperationVersion(), productRequestInfo.getProductSpecName());
			String factoryCode="5001";
			String factoryPosition="";
			String getFactoryPosition = " SELECT DESCRIPTION FROM ENUMDEFVALUE WHERE ENUMNAME=:ENUMNAME AND ENUMVALUE=:ENUMVALUE ";
			
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("ENUMNAME", "FactoryPosition");
			bindMap.put("ENUMVALUE", productRequestInfo.getFactoryName());
			
			List<Map<String, Object>> factoryPositionList = GenericServiceProxy.getSqlMesTemplate().queryForList(getFactoryPosition, bindMap);			
			if(factoryPositionList.size() > 0){
				factoryPosition = factoryPositionList.get(0).get("DESCRIPTION").toString();
			}
			else
			{
				factoryPosition="";
			}					
			for(ListOrderedMap ERPBOM : ERPBOMList)
			{
				StringBuilder sql = new StringBuilder();
				sql.append(" SELECT COUNT(A.PRODUCTNAME) AS QTY,C.CONSUMABLENAME,C.CONSUMABLESPECNAME,C.BATCHNO,D.CONSUMABLETYPE ");
				sql.append("  FROM CT_MATERIALPRODUCT  A,LOT B,CONSUMABLE C,CONSUMABLESPEC D ");
				sql.append("  WHERE     A.LOTNAME = B.LOTNAME ");
				sql.append("        AND A.MATERIALNAME = C.CONSUMABLENAME ");
				sql.append("        AND D.CONSUMABLESPECNAME = C.CONSUMABLESPECNAME ");
				sql.append("        AND C.FACTORYNAME = D.FACTORYNAME ");
				sql.append("        AND A.LOTNAME =:LOTNAME ");
				sql.append("        AND A.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
				sql.append("        AND A.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
				sql.append("        AND A.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
				sql.append("        AND A.MACHINENAME =:MACHINENAME ");
				sql.append("        AND D.CONSUMABLESPECNAME =:CONSUMABLESPECNAME ");
				sql.append("        AND D.CONSUMABLETYPE IN ('InOrganic','Organic','TopLamination','BottomLamination','Crate','PatternFilm') ");
				sql.append("        AND A.EVENTTIME> (CASE WHEN A.MATERIALTYPE='Crate' THEN B.CREATETIME ELSE B.LASTLOGGEDINTIME END) ");
				sql.append("        GROUP BY C.CONSUMABLESPECNAME,C.CONSUMABLENAME,C.BATCHNO,D.CONSUMABLETYPE ");

				Map<String, Object> args = new HashMap<String, Object>();
				args.put("LOTNAME", lotData.getKey().getLotName());
				args.put("PRODUCTSPECNAME", lotData.getProductSpecName());
				args.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
				args.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
				args.put("MACHINENAME", machineName);
				args.put("CONSUMABLESPECNAME", CommonUtil.getValue(ERPBOM, "MATERIALSPECNAME"));
				List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
				if(sqlResult.size()>0)
				{
		            for(int i=0;i<sqlResult.size();i++)
		            {
		            	if(getERPSkipMaterialList(sqlResult.get(i).get("CONSUMABLESPECNAME").toString(), sqlResult.get(i).get("BATCHNO").toString()))
		            	{
		            		continue;
		            	}
		    			List<Map<String, String>> ERPReportList = new ArrayList<Map<String, String>>();
		    			Map<String, String> ERPInfo = new HashMap<>();
		    			
		    			ERPInfo.put("SEQ",TimeStampUtil.getCurrentEventTimeKey());
		    			ERPInfo.put("PRODUCTREQUESTNAME", productRequestInfo.getProductRequestName());
		    			ERPInfo.put("MATERIALSPECNAME", sqlResult.get(i).get("CONSUMABLESPECNAME").toString());
		    			ERPInfo.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
		    			if((!StringUtil.equals(sqlResult.get(i).get("CONSUMABLETYPE").toString(), "InOrganic"))
		    					&&(!StringUtil.equals(sqlResult.get(i).get("CONSUMABLETYPE").toString(), "Organic")))
		    			{
		    				ERPInfo.put("QUANTITY", Integer.toString(Integer.parseInt(sqlResult.get(i).get("QTY").toString())));
		    				ERPInfo.put("PRODUCTQUANTITY", Integer.toString(Integer.parseInt(sqlResult.get(i).get("QTY").toString())));
		    			}
		    			else {
		    				continue;
		    				//ERPInfo.put("QUANTITY",String.format("%.9f", sapReportQty* Double.parseDouble(CommonUtil.getValue(ERPBOM, "QUANTITY"))));
		    				//ERPInfo.put("PRODUCTQUANTITY", Integer.toString(sapReportQty));
						}
		    			ERPInfo.put("CONSUMEUNIT", CommonUtil.getValue(ERPBOM, "CONSUMEUNIT"));
		    			ERPInfo.put("FACTORYCODE",factoryCode );
		    			ERPInfo.put("FACTORYPOSITION", factoryPosition);
		    			if((StringUtil.equals(sqlResult.get(i).get("CONSUMABLETYPE").toString(), "TopLamination"))
		    					||(StringUtil.equals(sqlResult.get(i).get("CONSUMABLETYPE").toString(), "BottomLamination")))
		    			{
			    			ERPInfo.put("BATCHNO", "");
		    			}
		    			else 
		    			{
		    				ERPInfo.put("BATCHNO", sqlResult.get(i).get("BATCHNO").toString());
		    			}

		    			
		    			
		    			Calendar cal = Calendar.getInstance();
		    			int hour = cal.get(Calendar.HOUR_OF_DAY);
		    			if(hour >= 19)
		    			{
		    				cal.set(Calendar.HOUR_OF_DAY, 0);
		    				cal.set(Calendar.MINUTE, 0);
		    				cal.set(Calendar.SECOND, 0);
		    				cal.set(Calendar.MILLISECOND, 0);
		    				cal.add(Calendar.DAY_OF_MONTH, 1);
		    				Timestamp receiveTime = new Timestamp(cal.getTime().getTime());
		    				ERPInfo.put("EVENTTIME", receiveTime.toString().replace("-","").substring(0,8));
		    			}
		    			else
		    			{
		    				ERPInfo.put("EVENTTIME", eventInfo.getEventTimeKey().substring(0,8));
		    			}
		    			
		    			ERPInfo.put("CANCELFLAG", "");
		    			ERPInfo.put("WSFLAG", "");
		    			ERPReportList.add(ERPInfo);
		    			
		    			eventInfo.setEventName("TrackOut");
		    			
		    			//Send
		    			try
		    			{
		    				ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().sapVXG372Send(eventInfo, ERPReportList, 1, CommonUtil.getValue(ERPBOM, "CONSUMEUNIT"));
		    			}
		    			catch (Exception e)
		    			{
		    				e.printStackTrace();
		    			}
		            }
				}
				else 
				{
					ConsumableSpec consumableSpec = null;
					String consumableType="";
					boolean gasFlag=false;
					try
					{
						consumableSpec = GenericServiceProxy.getSpecUtil().getMaterialSpec(CommonUtil.getValue(ERPBOM, "FACTORYNAME"), CommonUtil.getValue(ERPBOM, "MATERIALSPECNAME"), "00001");
						consumableType=consumableSpec.getConsumableType();
						if(StringUtils.equals(consumableType, "Crate")||StringUtils.equals(consumableType, "TopLamination")
								||StringUtils.equals(consumableType, "BottomLamination")||StringUtils.equals(consumableType, "PatternFilm")
								||StringUtils.equals(consumableType, "Organic")||StringUtils.equals(consumableType, "InOrganic") )
						{
							continue;
						}
						if(StringUtils.equals(consumableType, "CH3COOH")||StringUtils.equals(consumableType, "NMP")
								||StringUtils.equals(consumableType, "Other")||StringUtils.equals(consumableType, "HF")
								||StringUtils.equals(consumableType, "Diluent")||StringUtils.equals(consumableType, "Detergent")
								||StringUtils.equals(consumableType, "Developing Liquid")||StringUtils.equals(consumableType, "HN03")
								||StringUtils.equals(consumableType, "EtchingLiquid")||StringUtils.equals(consumableType, "Liquid-Cleaner")
								||StringUtils.equals(consumableType, "Gas"))
						{
							gasFlag=true;
						}
						if(StringUtils.equals(lotData.getProcessOperationName(), "22200")||
								StringUtils.equals(lotData.getProcessOperationName(), "22500")	)
						{
							continue;
						}
						
					}
					catch(CustomException c)
					{
						log.info("Not Fount ConsumeSpec:"+ CommonUtil.getValue(ERPBOM, "MATERIALSPECNAME"));
					}


					if(StringUtils.isNotEmpty(CommonUtil.getValue(ERPBOM, "SUBSTITUTEGROUP")))
					{
						if(backUpGroup.contains(CommonUtil.getValue(ERPBOM, "SUBSTITUTEGROUP"))
								&&(!StringUtils.equals(CommonUtil.getValue(ERPBOM, "KITFLAG"), "Y")))
						{							
							log.info("Consumable have KITFLAG ,but KITFLAG is not Y");
							continue;
						}
						else if (!backUpGroup.contains(CommonUtil.getValue(ERPBOM, "SUBSTITUTEGROUP"))
								&&(!StringUtils.equals(CommonUtil.getValue(ERPBOM, "VIRTUALFLAG"), "01")))
						{
							log.info("Consumable not have KITFLAG,VIRTUALFLAG is not 1");
							continue;
						}
					}
					else
					{
						log.info("Consumable not have backUpMaterial");
					}
					
	            	if(getERPSkipMaterialList(CommonUtil.getValue(ERPBOM, "MATERIALSPECNAME"), ""))
	            	{
	            		continue;
	            	}
	            	
					List<Map<String, String>> ERPReportList = new ArrayList<Map<String, String>>();
	    			Map<String, String> ERPInfo = new HashMap<>();
					ERPInfo.put("SEQ",TimeStampUtil.getCurrentEventTimeKey());
	    			ERPInfo.put("PRODUCTREQUESTNAME", productRequestInfo.getProductRequestName());
	    			ERPInfo.put("MATERIALSPECNAME",CommonUtil.getValue(ERPBOM, "MATERIALSPECNAME"));
	    			ERPInfo.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
		    		ERPInfo.put("QUANTITY",String.format("%.9f", sapReportQty* Double.parseDouble(CommonUtil.getValue(ERPBOM, "QUANTITY"))));
	    			ERPInfo.put("CONSUMEUNIT", CommonUtil.getValue(ERPBOM, "CONSUMEUNIT"));
	    			ERPInfo.put("FACTORYCODE",factoryCode );
	    			if(gasFlag)
	    			{
		    			ERPInfo.put("FACTORYPOSITION", "5F02");
	    			}
	    			else
	    			{
		    			ERPInfo.put("FACTORYPOSITION", factoryPosition);
	    			}
	    			ERPInfo.put("BATCHNO", "");
	    			ERPInfo.put("PRODUCTQUANTITY", String.valueOf(sapReportQty));
	    			
	    			Calendar cal = Calendar.getInstance();
	    			int hour = cal.get(Calendar.HOUR_OF_DAY);
	    			if(hour >= 19)
	    			{
	    				cal.set(Calendar.HOUR_OF_DAY, 0);
	    				cal.set(Calendar.MINUTE, 0);
	    				cal.set(Calendar.SECOND, 0);
	    				cal.set(Calendar.MILLISECOND, 0);
	    				cal.add(Calendar.DAY_OF_MONTH, 1);
	    				Timestamp receiveTime = new Timestamp(cal.getTime().getTime());
	    				ERPInfo.put("EVENTTIME", receiveTime.toString().replace("-","").substring(0,8));
	    			}
	    			else
	    			{
	    				ERPInfo.put("EVENTTIME", eventInfo.getEventTimeKey().substring(0,8));
	    			}
	    			
	    			ERPInfo.put("CANCELFLAG", "");
	    			ERPInfo.put("WSFLAG", "");
	    			ERPReportList.add(ERPInfo);
	    			
	    			eventInfo.setEventName("TrackOut");
	    			
	    			//Send
	    			try
	    			{
	    				ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().sapVXG372Send(eventInfo, ERPReportList, 1, CommonUtil.getValue(ERPBOM, "CONSUMEUNIT"));
	    			}
	    			catch (Exception e)
	    			{
	    				e.printStackTrace();
	    			}
				}
			}
			
		}
		else
		{
			return;
		}
		
	}
}
