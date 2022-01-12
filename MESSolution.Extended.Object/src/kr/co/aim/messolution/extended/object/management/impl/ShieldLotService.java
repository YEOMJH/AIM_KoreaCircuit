package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.RecipeParameter;
import kr.co.aim.messolution.extended.object.management.data.ShieldLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;

public class ShieldLotService extends CTORMService<ShieldLot> {
	public static Log logger = LogFactory.getLog(ShieldLotService.class);

	private final String historyEntity = "ShieldLotHistory";

	public List<ShieldLot> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<ShieldLot> result = super.select(condition, bindSet, ShieldLot.class);

		return result;
	}

	public ShieldLot selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ShieldLot.class, isLock, keySet);
	}

	public ShieldLot create(EventInfo eventInfo, ShieldLot dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void create(EventInfo eventInfo, List<ShieldLot> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfoList);

		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}

	public void remove(EventInfo eventInfo, ShieldLot dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public void remove(EventInfo eventInfo, List<ShieldLot> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);

		super.delete(dataInfoList);
	}

	public ShieldLot modify(EventInfo eventInfo, ShieldLot dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void modify(EventInfo eventInfo, List<ShieldLot> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.update(dataInfoList);

		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}

	public ShieldLot checkShieldSpec(String durableName, List<Element> shieldList, Durable durableInfo) throws greenFrameDBErrorSignal, CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();		
		ShieldLot shieldLotInfo = null;
		
		if(durableInfo.getDurableState().equals(constantMap.Dur_InUse)) //InUse Durable
		{
			List<ShieldLot> inDurShieldList = ExtendedObjectProxy.getShieldLotService().select("carrierName = ? ", new Object[] { durableName });
			shieldLotInfo = inDurShieldList.get(0);
			
			for(ShieldLot inDurShield : inDurShieldList)
			{
				String durSpec = inDurShield.getShieldSpecName();
				String durFlow = inDurShield.getProcessFlowName();
				String durOperationName = inDurShield.getProcessOperationName();
				String durCarGroup = inDurShield.getCarGroupName();
				String durBasketGroup = inDurShield.getBasketGroupName();
				String durChamberType = inDurShield.getChamberType();
				long durLine = inDurShield.getLine();
				String durChamberNo = inDurShield.getChamberNo();
				String durSet = inDurShield.getSetValue();
				
				for(Element shield : shieldList)
				{
					String shieldName = SMessageUtil.getChildText(shield, "SHIELDID", true);
					//ShieldLot shieldLotData = ExtendedObjectProxy.getShieldLotService().selectByKey(false, new Object[] { shieldName });
					
					String shieldSpec = SMessageUtil.getChildText(shield, "SHIELDSPEC", true);
					String shieldFlow = SMessageUtil.getChildText(shield, "PROCESSFLOW", true);
					String shieldOperation = SMessageUtil.getChildText(shield, "PROCESSOPERATION", true);
					String shieldCarGroup = SMessageUtil.getChildText(shield, "CARGROUPNAME", true);
					String shieldBasketGroup = SMessageUtil.getChildText(shield, "BASKETGROUPNAME", true);
					String shieldChamberType = SMessageUtil.getChildText(shield, "SHIELDTYPE", true);
					long shieldLine = Integer.valueOf(SMessageUtil.getChildText(shield, "LINE", true));
					String shieldChamberNo = SMessageUtil.getChildText(shield, "CHAMBERNO", true);
					String shieldSet = SMessageUtil.getChildText(shield, "SET", true);
					
//					if(!carMix)
//					{
//						if(shieldLine != durLine)
//						{
//							throw new CustomException("SHIELD-0023");
//						}
//						if(!shieldSet.equals(durSet))
//						{
//							throw new CustomException("SHIELD-0024");
//						}
//						if(!shieldChamberNo.equals(durChamberNo))
//						{
//							throw new CustomException("SHIELD-0025");
//						}
//					}					
					if (!shieldSpec.equals(durSpec))
					{
						throw new CustomException("SHIELD-0003");
					}					
					if(!shieldFlow.equals(durFlow) || !shieldOperation.equals(durOperationName))
					{
						throw new CustomException("SHIELD-00031");
					}
					if(durableInfo.getDurableType().equals("Car") && !shieldCarGroup.equals(durCarGroup))
					{
						throw new CustomException("SHIELD-00032");
					}
					if(durableInfo.getDurableType().equals("Basket") && !shieldBasketGroup.equals(durBasketGroup))
					{
						throw new CustomException("SHIELD-00033");
					}
//					if (!shieldChamberType.equals(durChamberType))
//					{
//						throw new CustomException("SHIELD-0026");
//					}	
					if (inDurShield.getLotProcessState().equals(constantMap.Lot_Run))
					{
						throw new CustomException("SHIELD-0002");
					}
					if (inDurShieldList.size() + shieldList.size() > durableInfo.getCapacity())
					{
						throw new CustomException("SHIELD-0004");
					}
				}
			}			
		}
		else if(durableInfo.getDurableState().equals(constantMap.Dur_Available)) //Available Durable
		{
			Element firstShieldInfo = shieldList.get(0);
			String firstShieldName = SMessageUtil.getChildText(firstShieldInfo, "SHIELDID", true);
			//ShieldLot firstShieldLotData = ExtendedObjectProxy.getShieldLotService().selectByKey(false, new Object[] { firstShieldName });
					
			String firstSpec = SMessageUtil.getChildText(firstShieldInfo, "SHIELDSPEC", true);
			String firstFlow = SMessageUtil.getChildText(firstShieldInfo, "PROCESSFLOW", true);
			String firstCarGroup = SMessageUtil.getChildText(firstShieldInfo, "CARGROUPNAME", true);
			String firstBasketGroup = SMessageUtil.getChildText(firstShieldInfo, "BASKETGROUPNAME", true);
			String firstOperationName = SMessageUtil.getChildText(firstShieldInfo, "PROCESSOPERATION", true);
			String firstChamberType = SMessageUtil.getChildText(firstShieldInfo, "SHIELDTYPE", true);
			long firstLine = Integer.valueOf(SMessageUtil.getChildText(firstShieldInfo, "LINE", true));
			String firstChamberNo = SMessageUtil.getChildText(firstShieldInfo, "CHAMBERNO", true);
			String firstSet = SMessageUtil.getChildText(firstShieldInfo, "SET", true);
			
			for(Element shield : shieldList)
			{
				String shieldName = SMessageUtil.getChildText(shield, "SHIELDID", true);
				//ShieldLot shieldLotData = ExtendedObjectProxy.getShieldLotService().selectByKey(false, new Object[] { shieldName });
				
				String shieldSpec = SMessageUtil.getChildText(shield, "SHIELDSPEC", true);
				String shieldFlow = SMessageUtil.getChildText(shield, "PROCESSFLOW", true);
				String shieldOperation = SMessageUtil.getChildText(shield, "PROCESSOPERATION", true);
				String shieldCarGroup = SMessageUtil.getChildText(shield, "CARGROUPNAME", true);
				String shieldBasketGroup = SMessageUtil.getChildText(shield, "BASKETGROUPNAME", true);
				String shieldChamberType = SMessageUtil.getChildText(shield, "SHIELDTYPE", true);
				long shieldLine = Integer.valueOf(SMessageUtil.getChildText(shield, "LINE", true));
				String shieldChamberNo = SMessageUtil.getChildText(shield, "CHAMBERNO", true);
				String shieldSet = SMessageUtil.getChildText(shield, "SET", true);
				
//				if(!carMix)
//				{
//					if(shieldLine != firstLine)
//					{
//						throw new CustomException("SHIELD-0023");
//					}
//					if(!shieldSet.equals(firstSet))
//					{
//						throw new CustomException("SHIELD-0024");
//					}
//					if(!shieldChamberNo.equals(firstChamberNo))
//					{
//						throw new CustomException("SHIELD-0025");
//					}
//				}					
				if (!shieldSpec.equals(firstSpec))
				{
					throw new CustomException("SHIELD-0003");
				}					
				if(!shieldFlow.equals(firstFlow) || !shieldOperation.equals(firstOperationName))
				{
					throw new CustomException("SHIELD-00031");
				}
				if(durableInfo.getDurableType().equals("Car") && !shieldCarGroup.equals(firstCarGroup))
				{
					throw new CustomException("SHIELD-00032");
				}
				if(durableInfo.getDurableType().equals("Basket") && !shieldBasketGroup.equals(firstBasketGroup))
				{
					throw new CustomException("SHIELD-00033");
				}
//				if (!shieldChamberType.equals(firstChamberType))
//				{
//					throw new CustomException("SHIELD-0026");
//				}	
//				if (shieldLotData.getLotProcessState().equals(constantMap.Lot_Run))
//				{
//					throw new CustomException("SHIELD-0002");
//				}			
				if (shieldList.size() > durableInfo.getCapacity())
				{
					throw new CustomException("SHIELD-0004");
				}
			}
		}
		else
		{
			throw new CustomException("CST-0007", durableName);
		}

		return shieldLotInfo;
	}
	
	public ShieldLot checkShieldSpecNotCreate(String durableName, List<Element> shieldList, Durable durableInfo) throws greenFrameDBErrorSignal, CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();		
		ShieldLot shieldLotInfo = null;
		
		if(durableInfo.getDurableState().equals(constantMap.Dur_InUse)) //InUse Durable
		{
			List<ShieldLot> inDurShieldList = ExtendedObjectProxy.getShieldLotService().select("carrierName = ? ", new Object[] { durableName });
			
			for(ShieldLot inDurShield : inDurShieldList)
			{
				String durSpec = inDurShield.getShieldSpecName();
				String durFlow = inDurShield.getProcessFlowName();
				String durOperationName = inDurShield.getProcessOperationName();
				String durCarGroup = inDurShield.getCarGroupName();
				String durBasketGroup = inDurShield.getBasketGroupName();
				String durChamberType = inDurShield.getChamberType();
				long durLine = inDurShield.getLine();
				String durChamberNo = inDurShield.getChamberNo();
				String durSet = inDurShield.getSetValue();
				
				for(Element shield : shieldList)
				{
					String shieldName = SMessageUtil.getChildText(shield, "SHIELDLOTNAME", true);
					ShieldLot shieldLotData = ExtendedObjectProxy.getShieldLotService().selectByKey(false, new Object[] { shieldName });
					
					String shieldSpec = shieldLotData.getShieldSpecName();
					String shieldFlow = shieldLotData.getProcessFlowName();
					String shieldOperation = shieldLotData.getProcessOperationName();
					String shieldCarGroup = shieldLotData.getCarGroupName();
					String shieldBasketGroup = shieldLotData.getBasketGroupName();
					String shieldChamberType = shieldLotData.getChamberType();
					long shieldLine = shieldLotData.getLine();
					String shieldChamberNo = shieldLotData.getChamberNo();
					String shieldSet = shieldLotData.getSetValue();
					
//					if(!carMix)
//					{
//						if(shieldLine != durLine)
//						{
//							throw new CustomException("SHIELD-0023");
//						}
//						if(!shieldSet.equals(durSet))
//						{
//							throw new CustomException("SHIELD-0024");
//						}
//						if(!shieldChamberNo.equals(durChamberNo))
//						{
//							throw new CustomException("SHIELD-0025");
//						}
//					}					
					if (!shieldSpec.equals(durSpec))
					{
						throw new CustomException("SHIELD-0003");
					}					
					if(!shieldFlow.equals(durFlow) || !shieldOperation.equals(durOperationName))
					{
						throw new CustomException("SHIELD-00031");
					}
					if(durableInfo.getDurableType().equals("Car") && !shieldCarGroup.equals(durCarGroup))
					{
						throw new CustomException("SHIELD-00032");
					}
					if(durableInfo.getDurableType().equals("Basket") && !shieldBasketGroup.equals(durBasketGroup))
					{
						throw new CustomException("SHIELD-00033");
					}
//					if (!shieldChamberType.equals(durChamberType))
//					{
//						throw new CustomException("SHIELD-0026");
//					}	
					if (shieldLotData.getLotProcessState().equals(constantMap.Lot_Run) || inDurShield.getLotProcessState().equals(constantMap.Lot_Run))
					{
						throw new CustomException("SHIELD-0002");
					}
					if (inDurShieldList.size() + shieldList.size() > durableInfo.getCapacity())
					{
						throw new CustomException("SHIELD-0004");
					}
				}
			}			
		}
		else if(durableInfo.getDurableState().equals(constantMap.Dur_Available)) //Available Durable
		{
			Element firstShieldInfo = shieldList.get(0);
			String firstShieldName = SMessageUtil.getChildText(firstShieldInfo, "SHIELDLOTNAME", true);
			ShieldLot firstShieldLotData = ExtendedObjectProxy.getShieldLotService().selectByKey(false, new Object[] { firstShieldName });
			
			String firstSpec = firstShieldLotData.getShieldSpecName();
			String firstFlow = firstShieldLotData.getProcessFlowName();
			String firstOperationName = firstShieldLotData.getProcessOperationName();
			String firstCarGroup = firstShieldLotData.getCarGroupName();
			String firstBasketGroup = firstShieldLotData.getBasketGroupName();
			String firstChamberType = firstShieldLotData.getChamberType();
			long firstLine = firstShieldLotData.getLine();
			String firstChamberNo = firstShieldLotData.getChamberNo();
			String firstSet = firstShieldLotData.getSetValue();
			
			for(Element shield : shieldList)
			{
				String shieldName = SMessageUtil.getChildText(shield, "SHIELDLOTNAME", true);
				ShieldLot shieldLotData = ExtendedObjectProxy.getShieldLotService().selectByKey(false, new Object[] { shieldName });
				
				String shieldSpec = shieldLotData.getShieldSpecName();
				String shieldFlow = shieldLotData.getProcessFlowName();
				String shieldOperation = shieldLotData.getProcessOperationName();
				String shieldCarGroup = shieldLotData.getCarGroupName();
				String shieldBasketGroup = shieldLotData.getBasketGroupName();
				String shieldChamberType = shieldLotData.getChamberType();
				long shieldLine = shieldLotData.getLine();
				String shieldChamberNo = shieldLotData.getChamberNo();
				String shieldSet = shieldLotData.getSetValue();
				
//				if(!carMix)
//				{
//					if(shieldLine != firstLine)
//					{
//						throw new CustomException("SHIELD-0023");
//					}
//					if(!shieldSet.equals(firstSet))
//					{
//						throw new CustomException("SHIELD-0024");
//					}
//					if(!shieldChamberNo.equals(firstChamberNo))
//					{
//						throw new CustomException("SHIELD-0025");
//					}
//				}					
				if (!shieldSpec.equals(firstSpec))
				{
					throw new CustomException("SHIELD-0003");
				}					
				if(!shieldFlow.equals(firstFlow) || !shieldOperation.equals(firstOperationName))
				{
					throw new CustomException("SHIELD-00031");
				}
				if(durableInfo.getDurableType().equals("Car") && !shieldCarGroup.equals(firstCarGroup))
				{
					throw new CustomException("SHIELD-00032");
				}
				if(durableInfo.getDurableType().equals("Basket") && !shieldBasketGroup.equals(firstBasketGroup))
				{
					throw new CustomException("SHIELD-00033");
				}
//				if (!shieldChamberType.equals(firstChamberType))
//				{
//					throw new CustomException("SHIELD-0026");
//				}	
				if (shieldLotData.getLotProcessState().equals(constantMap.Lot_Run))
				{
					throw new CustomException("SHIELD-0002");
				}			
				if (shieldList.size() > durableInfo.getCapacity())
				{
					throw new CustomException("SHIELD-0004");
				}
			}
		}
		else
		{
			throw new CustomException("CST-0007", durableName);
		}

		return shieldLotInfo;
	}

	public ShieldLot checkShieldSpecForRun(String durableName, List<Element> shieldList, Durable durableInfo) throws greenFrameDBErrorSignal, CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();		
		ShieldLot shieldLotInfo = null;
		
		if(durableInfo.getDurableState().equals(constantMap.Dur_InUse)) //InUse Durable
		{
			List<ShieldLot> inDurShieldList = ExtendedObjectProxy.getShieldLotService().select("carrierName = ? ", new Object[] { durableName });
			
			for(ShieldLot inDurShield : inDurShieldList)
			{
				String durSpec = inDurShield.getShieldSpecName();
				String durFlow = inDurShield.getProcessFlowName();
				String durOperationName = inDurShield.getProcessOperationName();
				String durCarGroup = inDurShield.getCarGroupName();
				String durBasketGroup = inDurShield.getBasketGroupName();
				
				for(Element shield : shieldList)
				{
					String shieldName = SMessageUtil.getChildText(shield, "SHIELDLOTNAME", true);
					ShieldLot shieldLotData = ExtendedObjectProxy.getShieldLotService().selectByKey(false, new Object[] { shieldName });
					
					String shieldSpec = shieldLotData.getShieldSpecName();
					String shieldFlow = shieldLotData.getProcessFlowName();
					String shieldOperation = shieldLotData.getProcessOperationName();
					String shieldCarGroup = shieldLotData.getCarGroupName();
					String shieldBasketGroup = shieldLotData.getBasketGroupName();
										
					if (!shieldSpec.equals(durSpec))
					{
						throw new CustomException("SHIELD-0003");
					}					
					if(!shieldFlow.equals(durFlow) || !shieldOperation.equals(durOperationName))
					{
						throw new CustomException("SHIELD-00031");
					}
					if(durableInfo.getDurableType().equals("Car") && !shieldCarGroup.equals(durCarGroup))
					{
						throw new CustomException("SHIELD-00032");
					}
					if(durableInfo.getDurableType().equals("Basket") && !shieldBasketGroup.equals(durBasketGroup))
					{
						throw new CustomException("SHIELD-00033");
					}
					if (!shieldLotData.getLotProcessState().equals(constantMap.Lot_Run) || !inDurShield.getLotProcessState().equals(constantMap.Lot_Run))
					{
						throw new CustomException("SHIELD-0002");
					}
					if (inDurShieldList.size() + shieldList.size() > durableInfo.getCapacity())
					{
						throw new CustomException("SHIELD-0004");
					}
				}
			}			
		}
		else if(durableInfo.getDurableState().equals(constantMap.Dur_Available)) //Available Durable
		{
			Element firstShieldInfo = shieldList.get(0);
			String firstShieldName = SMessageUtil.getChildText(firstShieldInfo, "SHIELDLOTNAME", true);
			ShieldLot firstShieldLotData = ExtendedObjectProxy.getShieldLotService().selectByKey(false, new Object[] { firstShieldName });
			
			String firstSpec = firstShieldLotData.getShieldSpecName();
			String firstFlow = firstShieldLotData.getProcessFlowName();
			String firstOperationName = firstShieldLotData.getProcessOperationName();
			String firstCarGroup = firstShieldLotData.getCarGroupName();
			String firstBasketGroup = firstShieldLotData.getBasketGroupName();
			
			for(Element shield : shieldList)
			{
				String shieldName = SMessageUtil.getChildText(shield, "SHIELDLOTNAME", true);
				ShieldLot shieldLotData = ExtendedObjectProxy.getShieldLotService().selectByKey(false, new Object[] { shieldName });
				
				String shieldSpec = shieldLotData.getShieldSpecName();
				String shieldFlow = shieldLotData.getProcessFlowName();
				String shieldOperation = shieldLotData.getProcessOperationName();
				String shieldCarGroup = shieldLotData.getCarGroupName();
				String shieldBasketGroup = shieldLotData.getBasketGroupName();
								
				if (!shieldSpec.equals(firstSpec))
				{
					throw new CustomException("SHIELD-0003");
				}					
				if(!shieldFlow.equals(firstFlow) || !shieldOperation.equals(firstOperationName))
				{
					throw new CustomException("SHIELD-00031");
				}
				if(durableInfo.getDurableType().equals("Car") && !shieldCarGroup.equals(firstCarGroup))
				{
					throw new CustomException("SHIELD-00032");
				}
				if(durableInfo.getDurableType().equals("Basket") && !shieldBasketGroup.equals(firstBasketGroup))
				{
					throw new CustomException("SHIELD-00033");
				}		
				if (!shieldLotData.getLotProcessState().equals(constantMap.Lot_Run))
				{
					throw new CustomException("SHIELD-0002");
				}
				if (shieldList.size() > durableInfo.getCapacity())
				{
					throw new CustomException("SHIELD-0004");
				}
			}
		}
		else
		{
			throw new CustomException("CST-0007", durableName);
		}

		return shieldLotInfo;
	}
	
	public void updateShieldLot(List<Object[]> updateArgList, List<Object[]> insertHistArgsList) throws CustomException
	{
		StringBuffer updatetSql = new StringBuffer();
		updatetSql.append("UPDATE CT_SHIELDLOT  ");
		updatetSql.append(" SET LINE = ? , CHAMBERTYPE = ? , CHAMBERNO = ? , SETVALUE = ? , JUDGE = ?  ");
		updatetSql.append(" , FACTORYNAME = ? , LOTSTATE = ? , LOTPROCESSSTATE = ? , LOTHOLDSTATE = ? , NODESTACK = ?  ");
		updatetSql.append(" , CLEANSTATE = ? , CARRIERNAME = ? , MACHINENAME = ? , CHAMBERNAME = ? , SHIELDSPECNAME = ?  ");
		updatetSql.append(" , PROCESSFLOWNAME = ? , PROCESSFLOWVERSION = ? , PROESSOPERATIONNAME = ? , PROCESSOPERATIONVERSION = ? , REASONCODETYPE = ?  ");
		updatetSql.append(" , REASONCODE = ? , REWORKSTATE = ? , REWORKCOUNT = ? , LASTLOGGEDINTIME = ? , LASTLOGGEDINUSER = ?  ");
		updatetSql.append(" , LASTLOGGEDOUTTIME = ? , LASTLOGGEDOUTUSER = ? , SAMPLEFLAG = ? , CREATETIME = ? , LASTEVENTCOMMENT = ?  ");
		updatetSql.append(" , LASTEVENTUSER = ? , LASTEVENTNAME = ? , LASTEVENTTIME = ? , LASTEVENTTIMEKEY = ?  ");
		updatetSql.append(" WHERE FACTORYNAME = ? AND SHIELDLOTNAME = ? ");

		MESLotServiceProxy.getLotServiceUtil().updateBatch(updatetSql.toString(), updateArgList);
		this.insertHistory(insertHistArgsList);

	}

	public void insertHistory(List<Object[]> insertHistArgsList) throws CustomException
	{
		StringBuffer insertHistSql = new StringBuffer();
		insertHistSql.append("INSERT INTO CT_SHIELDLOTHISTORY  ");
		insertHistSql.append(" (SHIELDLOTNAME, TIMEKEY, LINE, CHAMBERTYPE, CHAMBERNO,  ");
		insertHistSql.append(" SETVALUE,JUDGE,FACTORYNAME,LOTSTATE,LOTPROCESSSTATE,  ");
		insertHistSql.append(" LOTHOLDSTATE, NODESTACK, CLEANSTATE, CARRIERNAME, MACHINENAME,  ");
		insertHistSql.append(" CHAMBERNAME, SHIELDSPECNAME, PROCESSFLOWNAME, PROCESSFLOWVERSION, PROCESSOPERATIONNAME,  ");
		insertHistSql.append(" PROCESSOPERATIONVERSION, REASONCODETYPE, REASONCODE, REWORKSTATE, REWORKCOUNT,  ");
		insertHistSql.append(" LASTLOGGEDINTIME, LASTLOGGEDINUSER, LASTLOGGEDOUTTIME, LASTLOGGEDOUTUSER, SAMPLEFLAG,  ");
		insertHistSql.append(" CREATETIME, EVENTCOMMENT, EVENTNAME, EVENTUSER, EVENTTIME, CARGROUPNAME, BASKETGROUPNAME)  ");
		insertHistSql.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");
		
		MESLotServiceProxy.getLotServiceUtil().updateBatch(insertHistSql.toString(), insertHistArgsList);
	}
	
	public ShieldLot changeOperationShield(EventInfo eventInfo, ShieldLot shieldLotData, String sFactoryName, String newOperationName, String nodeStack) throws CustomException
	{
		ShieldLot shieldLotInfo = null;
		
		//Shop validation
		if(!StringUtils.equals(sFactoryName, "OLED"))
			throw new CustomException("Error Factory!");
			
		//ShieldLotHoldState Check
		if (StringUtils.equals(shieldLotData.getLotHoldState(), GenericServiceProxy.getConstantMap().MaskLotHoldState_OnHold))
			throw new CustomException("SHIELD-0011", shieldLotData.getShieldLotName());
		
		//MaskLotState Check
		if (!StringUtils.equals(shieldLotData.getLotState(), GenericServiceProxy.getConstantMap().MaskLotState_Released))
			throw new CustomException("SHIELD-0012", shieldLotData.getShieldLotName(),shieldLotData.getLotState());

		if(!StringUtils.equals(shieldLotData.getLotProcessState(), "WAIT"))
			throw new CustomException("SHIELD-0010", shieldLotData.getShieldLotName());					
		
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		shieldLotData.setProcessOperationName(newOperationName);
		shieldLotData.setNodeStack(nodeStack);
		shieldLotData.setReasonCode("");
		shieldLotData.setReasonCodeType("");
		shieldLotData.setLastEventUser(eventInfo.getEventUser());
		shieldLotData.setLastEventTimekey(eventInfo.getLastEventTimekey());
		shieldLotData.setLastEventName(eventInfo.getEventName());
		shieldLotData.setLastEventComment(eventInfo.getEventComment());
		shieldLotData.setLastEventTime(eventInfo.getEventTime());
	
		String sql = "SELECT NODEID FROM NODE " + " WHERE FACTORYNAME = ? "
				+ "   AND PROCESSFLOWNAME = ? "
				+ "   AND PROCESSFLOWVERSION = ? "
				+ "   AND NODEATTRIBUTE1 = ? " + "   AND NODEATTRIBUTE2 = ? "
				+ "   AND NODETYPE = 'ProcessOperation' ";

		Object[] bind = new Object[] { shieldLotData.getFactoryName(),
				shieldLotData.getProcessFlowName(), shieldLotData.getProcessFlowVersion(), 
				newOperationName, shieldLotData.getProcessOperationVersion()};

		String[][] result = null;
		result = GenericServiceProxy.getSqlMesTemplate().queryForStringArray(sql, bind);

		if (result.length == 0) 
		{
			throw new CustomException("MASK-0064", shieldLotData.getProcessFlowName(), newOperationName);
		} 
		else
		{
			String sToBeNodeStack = shieldLotData.getNodeStack();
			String sNodeStack = shieldLotData.getNodeStack();
		
			if (sNodeStack.contains("."))
			{
				String sCurNode = StringUtil.getLastValue(sNodeStack, ".");
				sNodeStack = sNodeStack.substring(0,sNodeStack.length() - sCurNode.length()- 1);
			
				sToBeNodeStack = sNodeStack + "." + result[0][0];
			}
			else
			{
				sToBeNodeStack = result[0][0];
			}
			shieldLotData.setNodeStack(sToBeNodeStack);
		}

		shieldLotData = ExtendedObjectProxy.getShieldLotService().modify(eventInfo, shieldLotData);
		return  shieldLotInfo;
	}
}
