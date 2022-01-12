package kr.co.aim.messolution.extended.object.management.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.extended.object.management.data.RecipeParameter;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;

public class RecipeService extends CTORMService<Recipe> {
	String Machinename;
	String Recipename;

	public static Log logger = LogFactory.getLog(RecipeService.class);

	private final String historyEntity = "RecipeHistory";

	public List<Recipe> select(String condition, Object[] bindSet) throws CustomException
	{
		List<Recipe> result = super.select(condition, bindSet, Recipe.class);

		return result;
	}

	public Recipe selectByKey(boolean isLock, Object[] keySet) throws CustomException
	{
		return super.selectByKey(Recipe.class, isLock, keySet);
	}

	public Recipe create(EventInfo eventInfo, Recipe dataInfo) throws CustomException
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, Recipe dataInfo) throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public Recipe modify(EventInfo eventInfo, Recipe dataInfo) throws CustomException
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public Recipe getRecipeData(String machineName, String recipeName) throws CustomException
	{
		if(logger.isInfoEnabled())
		logger.info(String.format(" Input arguments: MachineName [%s] , RecipeName [%s] .",machineName,recipeName));
		
		Recipe dataInfo = null;
		
		try
		{
			dataInfo = super.selectByKey(Recipe.class, false, new Object[] { machineName, recipeName });
		}
		catch (greenFrameDBErrorSignal greenEx)
		{
            if(greenEx.getErrorCode().equals(ErrorSignal.NotFoundSignal))
            	throw new CustomException("COMM-1000", "CT_Recipe",String.format("MachineName = %s , RecipeName = %s ",machineName,recipeName));
            else 
            	throw new CustomException(greenEx.getCause());
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}
		
		logger.info("Is  " +  dataInfo.getRecipeType() + " Recipe.");
		return dataInfo;
	}

	public Recipe makeCreate(EventInfo eventInfo, String machineName, String recipeName, String recipeType, Timestamp LASTCHANGETIME, String Version) throws CustomException
	{

		Recipe recipeInfo = new Recipe(machineName, recipeName, recipeType);
		recipeInfo.setRecipeState("Created");
		recipeInfo.setActiveState(GenericServiceProxy.getConstantMap().Spec_NotActive);
		recipeInfo.setMFGFlag("Y");
		recipeInfo.setINTFlag("N");
		recipeInfo.setENGFlag("N");
		recipeInfo.setRMSFlag("N");
		recipeInfo.setAutoChangeFlag("N");
		recipeInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
		recipeInfo.setTimeUsedLimit(0);
		recipeInfo.setTotalTimeUsed(0);
		recipeInfo.setTimeUsed(0);
		recipeInfo.setDurationUsedLimit(0);
		recipeInfo.setTotalDurationUsed(0);
		recipeInfo.setDurationUsed(0);
		recipeInfo.setVERSION(Version);
		recipeInfo.setLastChangeTime(LASTCHANGETIME);
		// history trace
		recipeInfo.setLastEventName(eventInfo.getEventName());
		recipeInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
		recipeInfo.setLastEventUser(eventInfo.getEventUser());
		recipeInfo.setLastEventComment(eventInfo.getEventComment());
		recipeInfo.setLastChangeTime(LASTCHANGETIME);
		eventInfo.setEventName("Create");
		recipeInfo = ExtendedObjectProxy.getRecipeService().create(eventInfo, recipeInfo);
		return recipeInfo;
	}

	public Recipe remove(EventInfo eventInfo, String machineName, String recipeName, String recipeType) throws CustomException
	{
		Recipe recipeData = ExtendedObjectProxy.getRecipeService().selectByKey(true, new Object[] { machineName, recipeName });
		// history trace
		recipeData.setLastEventName(eventInfo.getEventName());
		recipeData.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		recipeData.setLastEventUser(eventInfo.getEventUser());
		recipeData.setLastEventComment(eventInfo.getEventComment());
		ExtendedObjectProxy.getRecipeService().remove(eventInfo, recipeData);
		return recipeData;
	}

	public Recipe makeNotAvailable(EventInfo eventInfo, String machineName, String recipeName, String recipeType, Timestamp LCT, String Version) throws CustomException
	{
		Recipe recipeData = ExtendedObjectProxy.getRecipeService().selectByKey(true, new Object[] { machineName, recipeName });

		// start send email
		try
		{
			if ((recipeData.getINTFlag().equals("Y") && recipeData.getMFGFlag().equals("Y") && recipeData.getENGFlag().equals("Y") && recipeData.getRMSFlag().equals("Y")))
			{
				MachineSpec rootMachineData = MESRecipeServiceProxy.getRecipeServiceUtil().getRootMachine(machineName);
				String rootMachineName = rootMachineData.getKey().getMachineName();
				String message = "<pre>Dear All: </pre>";
				message += "<pre>===============RecipeAlarmInformation=============================================</pre>";
				message += "<pre>==================================================================================</pre>";
				message += "<pre>- AlarmMachine	: " + rootMachineName + "</pre>";
				message += "<pre>- MachineName	: " + machineName + "</pre>";
				message += "<pre>- RecipeType	: " + recipeType + "</pre>";
				message += "<pre>- RecipeName	: " + recipeName + "</pre>";
				message += "<pre>- AlarmType	: " + eventInfo.getEventName() + "</pre>";
				message += "<pre>- Timekey	: " + eventInfo.getEventTime() + "</pre>";
				message += "<pre>- Description	: " + "Someone change recipe on EQP,please confirm! " + "</pre>";
				message += "<pre>==================================================================================</pre>";

				CommonUtil.sendAlarmEmail(rootMachineName, "RMS", message);
			}
		}
		catch (Exception ex)
		{
		}
		// end

		if (StringUtil.isEmpty(recipeType))
		{// without message
			if (recipeData.getRecipeType().equalsIgnoreCase("MAIN"))
				recipeType = "E";
			else if (recipeData.getRecipeType().equalsIgnoreCase("UNIT"))
				recipeType = "U";
			else if (recipeData.getRecipeType().equalsIgnoreCase("SUBUNIT"))
				recipeType = "S";
		}

		// RecipeChanged
		recipeData.setLastChangeTime(LCT);
		recipeData.setLastEventTimeKey(eventInfo.getEventTimeKey());

		// history trace
		recipeData.setLastEventName(eventInfo.getEventName());
		recipeData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		recipeData.setLastEventUser(eventInfo.getEventUser());
		recipeData.setLastEventComment(eventInfo.getEventComment());
		recipeData.setVERSION(Version);
		// modify ENGFlag and RMSFlag at same time
		if (recipeData.getRecipeType().equalsIgnoreCase("MAIN") && recipeType.equalsIgnoreCase("E"))
		{
			recipeData.setRMSFlag("N");
			recipeData.setENGFlag("N");
		}
		else if (recipeData.getRecipeType().equalsIgnoreCase("UNIT") && recipeType.equalsIgnoreCase("U"))
		{
			recipeData.setRMSFlag("N");
			recipeData.setENGFlag("N");
		}
		else if (recipeData.getRecipeType().equalsIgnoreCase("SUBUNIT") && recipeType.equalsIgnoreCase("S"))
		{
			recipeData.setRMSFlag("N");
			recipeData.setENGFlag("N");
		}
		else
		{
			// RMS-005:PPID[{0}] has something wrong, please check it up
			throw new CustomException("RMS-005", recipeName);
		}

		// manipulated recipe is invalid until verified

		recipeData = ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipeData);

		return recipeData;
	}

	public Recipe makeNotAvailable(EventInfo eventInfo, String machineName, String recipeName, String recipeType, Timestamp LCT, String Version, String factoryName) throws CustomException
	{
		Recipe recipeData = ExtendedObjectProxy.getRecipeService().selectByKey(true, new Object[] { machineName, recipeName });

		if (StringUtil.isEmpty(recipeType))
		{
			// without message
			if (recipeData.getRecipeType().equalsIgnoreCase("MAIN"))
				recipeType = "E";
			else if (recipeData.getRecipeType().equalsIgnoreCase("UNIT"))
				recipeType = "U";
			else if (recipeData.getRecipeType().equalsIgnoreCase("SUBUNIT"))
				recipeType = "S";
		}

		// RecipeChanged
		recipeData.setLastChangeTime(LCT);
		recipeData.setLastEventTimeKey(eventInfo.getEventTimeKey());

		// history trace
		recipeData.setLastEventName(eventInfo.getEventName());
		recipeData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		recipeData.setLastEventUser(eventInfo.getEventUser());
		recipeData.setLastEventComment(eventInfo.getEventComment());
		recipeData.setVERSION(Version);
		recipeData.setRMSFlag("N");
		recipeData.setENGFlag("N");
		recipeData.setINTFlag("N");

		// manipulated recipe is invalid until verified
		recipeData = ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipeData);

		return recipeData;
	}

	public Recipe AvailableUpdate(EventInfo eventInfo, String machineName, String recipeName, String recipeType, Timestamp LCT, String Version) throws CustomException
	{
		Recipe recipeData = ExtendedObjectProxy.getRecipeService().selectByKey(true, new Object[] { machineName, recipeName });

		if (StringUtil.isEmpty(recipeType))
		{// without message
			if (recipeData.getRecipeType().equalsIgnoreCase("MAIN"))
				recipeType = "E";
			else if (recipeData.getRecipeType().equalsIgnoreCase("UNIT"))
				recipeType = "U";
			else if (recipeData.getRecipeType().equalsIgnoreCase("SUBUNIT"))
				recipeType = "S";
		}

		// RecipeChanged
		recipeData.setLastChangeTime(LCT);
		recipeData.setLastEventTimeKey(eventInfo.getEventTimeKey());

		// history trace
		recipeData.setLastEventName(eventInfo.getEventName());
		recipeData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		recipeData.setLastEventUser(eventInfo.getEventUser());
		recipeData.setLastEventComment(eventInfo.getEventComment());
		recipeData.setVERSION(Version);

		// manipulated recipe is invalid until verified

		recipeData = ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipeData);

		return recipeData;
	}

	public Recipe makeNotAvailable1(EventInfo eventInfo, String machineName, String recipeName, String recipeType) throws CustomException
	{

		Recipe recipeData = ExtendedObjectProxy.getRecipeService().selectByKey(true, new Object[] { machineName, recipeName });
		if (StringUtil.isEmpty(recipeType))
		{// without message
			if (recipeData.getRecipeType().equalsIgnoreCase("MAIN"))
				recipeType = "E";
			else if (recipeData.getRecipeType().equalsIgnoreCase("UNIT"))
				recipeType = "U";
			else if (recipeData.getRecipeType().equalsIgnoreCase("SUBUNIT"))
				recipeType = "S";
		}

		// RecipeChanged
		recipeData.setLastModifiedTime(eventInfo.getEventTime());
		// history trace
		recipeData.setLastEventName(eventInfo.getEventName());
		recipeData.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		recipeData.setLastEventUser(eventInfo.getEventUser());
		recipeData.setLastEventComment(eventInfo.getEventComment());
		recipeData.setLastModifiedTime(eventInfo.getEventTime());
		if (recipeData.getRecipeType().equalsIgnoreCase("MAIN") && recipeType.equalsIgnoreCase("E"))
		{
			recipeData.setRMSFlag("N");
		}
		else if (recipeData.getRecipeType().equalsIgnoreCase("UNIT") && recipeType.equalsIgnoreCase("U"))
		{
			recipeData.setActiveState(GenericServiceProxy.getConstantMap().Spec_NotActive);
			String SQL = "select * from ct_recipeparameter where recipeparametername=:machineName and value=:recipeName  ";
			HashMap<String, Object> bindMap1 = new HashMap<String, Object>();
			bindMap1.put("machineName", machineName);
			bindMap1.put("recipeName", recipeName);
			List<ListOrderedMap> result1 = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(SQL, bindMap1);
			for (int i = 0; i < result1.size(); i++)
			{
				ListOrderedMap temp1 = result1.get(i);
				Machinename = CommonUtil.getValue(temp1, "Machinename");
				Recipename = CommonUtil.getValue(temp1, "recipename");

				StringBuffer sql = new StringBuffer();
				sql.append("SELECT MACHINENAME, RECIPENAME, RECIPESTATE ");
				sql.append("  FROM CT_RECIPE ");
				sql.append(" WHERE 1 = 1 ");
				sql.append("   AND RECIPETYPE = 'MAIN' ");
				sql.append("   AND MACHINENAME = :MACHINENAME ");
				sql.append("   AND RECIPENAME = :RECIPENAME ");

				HashMap<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("Machinename", Machinename);
				bindMap.put("Recipename", Recipename);
				List<ListOrderedMap> result = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
				if (result.size() > 0)
				{
					Recipe mainRecipeData = ExtendedObjectProxy.getRecipeService().selectByKey(true,
							new Object[] { (String) result.get(0).get("MACHINENAME"), (String) result.get(0).get("RECIPENAME") });
					// RecipeChanged
					mainRecipeData.setLastModifiedTime(eventInfo.getEventTime());
					// history trace
					mainRecipeData.setLastEventName(eventInfo.getEventName());
					mainRecipeData.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
					mainRecipeData.setLastEventUser(eventInfo.getEventUser());
					mainRecipeData.setLastEventComment(eventInfo.getEventComment());
					mainRecipeData.setRMSFlag("N");
					mainRecipeData = ExtendedObjectProxy.getRecipeService().modify(eventInfo, mainRecipeData);
				}

			}
		}
		else if (recipeData.getRecipeType().equalsIgnoreCase("SUBUNIT") && recipeType.equalsIgnoreCase("S"))
		{
			recipeData.setRMSFlag("N");
		}
		else
		{
			// RMS-005:PPID[{0}] has something wrong, please check it up
			throw new CustomException("RMS-005", recipeName);
		}

		recipeData = ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipeData);

		return recipeData;
	}

	public void insertRecipeParameter(EventInfo eventInfo, String machineName, String recipeName, String recipeType, List<Element> parameterList) throws CustomException
	{
		List<Object[]> insertRecipeArgList = new ArrayList<Object[]>();
		List<Object[]> insertRecipeHistArgList = new ArrayList<Object[]>();
		// List<Lot> recipeHistoryArgList = new ArrayList<Lot>();
		// List<Lot> oldRecipeListHistory = new ArrayList<Lot>();
		//
		// SQL
		String queryStringParameter = "INSERT INTO CT_RECIPEPARAMETER(MACHINENAME, RECIPENAME, RECIPEPARAMETERNAME, VALUE) VALUES ( ?, ?, ?, ? )";
		String queryStringParameterHist = "INSERT INTO CT_RECIPEPARAMETERHISTORY(MACHINENAME, RECIPENAME, RECIPEPARAMETERNAME, VALUE, TIMEKEY, EVENTNAME, EVENTUSER) VALUES ( ?, ?, ?, ?, ?, ?, ? )";

		// Get recipe by SuperMachine
		StringBuffer sqlBuffer = new StringBuffer();

		sqlBuffer.append(" SELECT RECIPEPARAMETERNAME, VALUE ");
		sqlBuffer.append("   FROM CT_RECIPEPARAMETER ");
		sqlBuffer.append(" WHERE 1=1 ");
		sqlBuffer.append(" AND MACHINENAME = ? ");
		sqlBuffer.append(" AND RECIPENAME = ? ");

		Object[] bind = new String[] { machineName, recipeName };

		List<ListOrderedMap> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bind);

		if (sqlResult != null && sqlResult.size() > 0)
		{
			throw new CustomException("MACHINE-0012");
		}
		else
		{
			if (parameterList != null && parameterList.size() > 0)
			{
				for (Element parameter : parameterList)
				{
					String eqpParaName = parameter.getChildText("PARAMETERNAME");
					String eqpValue = parameter.getChildText("VALUE");

					List<Object> bindList = new ArrayList<Object>();

					bindList.add(machineName);
					bindList.add(recipeName);
					bindList.add(eqpParaName);
					bindList.add(eqpValue);
					insertRecipeArgList.add(bindList.toArray());

					bindList.add(eventInfo.getEventTimeKey());
					bindList.add(eventInfo.getEventName());
					bindList.add(eventInfo.getEventUser());
					insertRecipeHistArgList.add(bindList.toArray());
				}
			}
			else
				throw new CustomException("MACHINE-0012");
		}

		this.updateBatch(queryStringParameter, insertRecipeArgList);
		this.updateBatch(queryStringParameterHist, insertRecipeHistArgList);
	}

	public void updateRecipeParameter(EventInfo eventInfo, String machineName, String recipeName, String recipeType, List<Element> parameterList) throws CustomException
	{
		List<Object[]> updateRecipeArgList = new ArrayList<Object[]>();
		List<Object[]> updateRecipeHistArgList = new ArrayList<Object[]>();

		// SQL
		String queryStringParameter = "UPDATE CT_RECIPEPARAMETER SET VALUE = ? WHERE MACHINENAME = ? AND RECIPENAME = ? AND RECIPEPARAMETERNAME = ?  ";
		String queryStringParameterHist = "UPDATE CT_RECIPEPARAMETERHISTORY SET VALUE = ?, TIMEKEY = ?, EVENTNAME = ?, EVENTUSER = ? WHERE MACHINENAME = ? AND RECIPENAME = ? AND RECIPEPARAMETERNAME = ? ";

		// Get recipe by SuperMachine
		StringBuffer sqlBuffer = new StringBuffer("");

		sqlBuffer.append(" SELECT RECIPEPARAMETERNAME, VALUE ").append("   FROM CT_RECIPEPARAMETER ").append(" WHERE 1=1 ").append(" AND MACHINENAME = ? ").append(" AND RECIPENAME = ? ");

		Object[] bind = new String[] { machineName, recipeName };

		List<ListOrderedMap> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bind);

		if (sqlResult != null && sqlResult.size() > 0)
		{
			if (parameterList != null && parameterList.size() > 0)
			{
				for (Element parameter : parameterList)
				{
					String eqpParaName = parameter.getChildText("PARAMETERNAME");
					String eqpValue = parameter.getChildText("VALUE");

					boolean isFind = false;
					for (ListOrderedMap map : sqlResult)
					{
						String mesParaName = CommonUtil.getValue(map, "RECIPEPARAMETERNAME");
						String mesValue = CommonUtil.getValue(map, "VALUE");

						if (eqpParaName.equals(mesParaName) && !eqpValue.equals(mesValue))
						{
							logger.debug("Different Parameter so it will be updated.. EQP [" + eqpParaName + ":" + eqpValue + "], MES [ " + mesParaName + ":" + mesValue + "]");
							List<Object> bindList = new ArrayList<Object>();

							bindList.add(eqpValue);
							bindList.add(machineName);
							bindList.add(recipeName);
							bindList.add(eqpParaName);

							updateRecipeArgList.add(bindList.toArray());

							List<Object> bindListHist = new ArrayList<Object>();

							bindListHist.add(eqpValue);
							bindListHist.add(eventInfo.getEventTimeKey());
							bindListHist.add(eventInfo.getEventName());
							bindListHist.add(eventInfo.getEventUser());
							bindListHist.add(machineName);
							bindListHist.add(recipeName);
							bindListHist.add(eqpParaName);

							updateRecipeHistArgList.add(bindListHist.toArray());
						}
					}
				}

			}
			else
				throw new CustomException("MACHINE-0012");
		}

		this.updateBatch(queryStringParameter, updateRecipeArgList);
		this.updateBatch(queryStringParameterHist, updateRecipeHistArgList);
	}

	public boolean updateRecipeParameterForModify(EventInfo eventInfo, String machineName, String recipeName, String recipeType, List<Element> parameterList) throws CustomException
	{
		List<Object[]> updateRecipeArgList = new ArrayList<Object[]>();
		List<Object[]> updateRecipeHistArgList = new ArrayList<Object[]>();

		boolean result = false;

		// SQL
		String queryStringParameter = "UPDATE CT_RECIPEPARAMETER SET VALUE = ? WHERE MACHINENAME = ? AND RECIPENAME = ? AND RECIPEPARAMETERNAME = ?  ";
		String queryStringParameterHist = "INSERT INTO CT_RECIPEPARAMETERHISTORY (MACHINENAME, RECIPENAME, RECIPEPARAMETERNAME, VALUE, VALIDATIONTYPE, TARGET, LOWERLIMIT, UPPERLIMIT, OLDVALIDATIONTYPE, "
				+ "OLDVALUE, OLDTARGET, OLDLOWERLIMIT, OLDUPPERLIMIT, TIMEKEY, EVENTNAME, EVENTUSER) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)  ";

		// Get recipe by SuperMachine
		StringBuffer sqlBuffer = new StringBuffer("");

		sqlBuffer.append(" SELECT RECIPEPARAMETERNAME, VALUE, VALIDATIONTYPE, LOWERLIMIT, UPPERLIMIT, TARGET ").append("   FROM CT_RECIPEPARAMETER ").append(" WHERE 1=1 ")
				.append(" AND MACHINENAME = ? ").append(" AND RECIPENAME = ? ");

		Object[] bind = new String[] { machineName, recipeName };

		List<ListOrderedMap> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bind);

		if (sqlResult != null && sqlResult.size() > 0)
		{
			if (parameterList != null && parameterList.size() > 0)
			{
				for (Element parameter : parameterList)
				{
					String eqpParaName = parameter.getChildText("PARAMETERNAME");
					String eqpValue = parameter.getChildText("VALUE");
					boolean recipeChangeMachineSpec = false;

					if (recipeType.equalsIgnoreCase("U") || recipeType.equalsIgnoreCase("E"))
					{
						StringBuffer sqlBuffer1 = new StringBuffer("");
						sqlBuffer1.append(" SELECT MACHINENAME ");
						sqlBuffer1.append("   FROM MACHINESPEC ");
						sqlBuffer1.append(" WHERE 1=1 ");
						sqlBuffer1.append(" AND MACHINENAME = :MACHINENAME");
						Map<String, Object> args = new HashMap<String, Object>();
						args.put("MACHINENAME", parameter.getChildText("PARAMETERNAME"));
						List<Map<String, Object>> sqlBuffer1result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer1.toString(), args);
						if (sqlBuffer1result != null && sqlBuffer1result.size() > 0)
						{
							// continue;
							recipeChangeMachineSpec = true;
						}
					}

					for (ListOrderedMap map : sqlResult)
					{
						String mesParaName = CommonUtil.getValue(map, "RECIPEPARAMETERNAME");
						String mesValue = CommonUtil.getValue(map, "VALUE");
						String mesValType = CommonUtil.getValue(map, "VALIDATIONTYPE");

						if (eqpParaName.equals(mesParaName))
						{
							if (!eqpValue.equals(mesValue))
							{
								if (recipeChangeMachineSpec)
								{
									result = true;
									break;
								}
								if (mesValType.equals("Range"))
								{

									String lower = CommonUtil.getValue(map, "LOWERLIMIT");
									String upper = CommonUtil.getValue(map, "UPPERLIMIT");

									double eqpNumValue = Double.parseDouble(eqpValue);
									double numLower = Double.parseDouble(lower);
									double numUpper = Double.parseDouble(upper);

									if (!(eqpNumValue >= numLower && eqpNumValue <= numUpper))
									{
										result = true;
									}
								}
								else if (mesValType.contains("Target"))
								{
									result = true;
								}

								logger.debug("Different Parameter so it will be updated.. EQP [" + eqpParaName + ":" + eqpValue + "], MES [ " + mesParaName + ":" + mesValue + "]");
								List<Object> bindList = new ArrayList<Object>();

								bindList.add(eqpValue);
								bindList.add(machineName);
								bindList.add(recipeName);
								bindList.add(eqpParaName);

								updateRecipeArgList.add(bindList.toArray());

								List<Object> bindListHist = new ArrayList<Object>();

								bindListHist.add(machineName);
								bindListHist.add(recipeName);
								bindListHist.add(eqpParaName);
								bindListHist.add(eqpValue);
								bindListHist.add(mesValType);
								bindListHist.add(CommonUtil.getValue(map, "TARGET"));
								bindListHist.add(CommonUtil.getValue(map, "LOWERLIMIT"));
								bindListHist.add(CommonUtil.getValue(map, "UPPERLIMIT"));
								bindListHist.add(mesValType);
								bindListHist.add(mesValue);
								bindListHist.add(CommonUtil.getValue(map, "TARGET"));
								bindListHist.add(CommonUtil.getValue(map, "LOWERLIMIT"));
								bindListHist.add(CommonUtil.getValue(map, "UPPERLIMIT"));
								bindListHist.add(eventInfo.getEventTimeKey());
								bindListHist.add(eventInfo.getEventName());
								bindListHist.add(eventInfo.getEventUser());

								updateRecipeHistArgList.add(bindListHist.toArray());
							}
							else
							{
								break;
							}
						}
					}
				}
			}
			else
				throw new CustomException("MACHINE-0012");
		}

		this.updateBatch(queryStringParameter, updateRecipeArgList);
		this.updateBatch(queryStringParameterHist, updateRecipeHistArgList);

		return result;
	}

	public boolean recipeChangeSendEmail(EventInfo eventInfo, String machineName, String recipeName, String recipeType, List<Element> parameterList) throws CustomException
	{
		boolean result = false;
		StringBuffer sqlBuffer = new StringBuffer("");
		
		sqlBuffer.append("SELECT RECIPEPARAMETERNAME, VALUE, VALIDATIONTYPE, LOWERLIMIT, UPPERLIMIT, TARGET ");
		sqlBuffer.append("  FROM CT_RECIPEPARAMETER ");
		sqlBuffer.append(" WHERE 1 = 1 ");
		sqlBuffer.append("   AND MACHINENAME = ? ");
		sqlBuffer.append("   AND RECIPENAME = ? ");


		Object[] bind = new String[] { machineName, recipeName };

		List<ListOrderedMap> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bind);

		if (sqlResult != null && sqlResult.size() > 0)
		{
			if (parameterList != null && parameterList.size() > 0)
			{
				for (Element parameter : parameterList)
				{
					String eqpParaName = parameter.getChildText("PARAMETERNAME");
					String eqpValue = parameter.getChildText("VALUE");
					boolean recipeChangeMachineSpec = false;
					if (recipeType.equalsIgnoreCase("U") || recipeType.equalsIgnoreCase("E"))
					{
						StringBuffer sqlBuffer1 = new StringBuffer("");
						sqlBuffer1.append(" SELECT MACHINENAME ");
						sqlBuffer1.append("   FROM MACHINESPEC ");
						sqlBuffer1.append(" WHERE 1=1 ");
						sqlBuffer1.append(" AND MACHINENAME = :MACHINENAME");
						
						Map<String, Object> args = new HashMap<String, Object>();
						args.put("MACHINENAME", parameter.getChildText("PARAMETERNAME"));
						List<Map<String, Object>> sqlBuffer1result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer1.toString(), args);
						if (sqlBuffer1result != null && sqlBuffer1result.size() > 0)
						{
							recipeChangeMachineSpec = true;
						}
					}

					for (ListOrderedMap map : sqlResult)
					{
						String mesParaName = CommonUtil.getValue(map, "RECIPEPARAMETERNAME");
						String mesValue = CommonUtil.getValue(map, "VALUE");
						String mesValType = CommonUtil.getValue(map, "VALIDATIONTYPE");

						if (eqpParaName.equals(mesParaName))
						{
							if (!eqpValue.equals(mesValue))
							{
								if (recipeChangeMachineSpec && mesValType.equals(""))
								{
									result = true;
									logger.debug("Different Parameter so it will be send Email.. EQP [" + eqpParaName + ":" + eqpValue + "], MES [ " + mesParaName + ":" + mesValue + "]");
									break;
								}
								else if (mesValType.equals("Range") || mesValType.contains("Target"))
								{
									result = true;
									logger.debug("Different Parameter so it will be send Email.. EQP [" + eqpParaName + ":" + eqpValue + "], MES [ " + mesParaName + ":" + mesValue + "]");
									break;
								}
								else
								{
									break;
								}
							}
							else
							{
								break;
							}
						}

					}
				}
			}
			else
				throw new CustomException("MACHINE-0012");
		}

		return result;
	}

	public void updateBatch(String queryString, List<Object[]> updateArgList) throws CustomException
	{
		// Update Batch
		if (updateArgList.size() > 0)
		{
			try
			{
				if (updateArgList.size() == 1)
				{
					GenericServiceProxy.getSqlMesTemplate().update(queryString, updateArgList.get(0));
				}
				else
				{
					GenericServiceProxy.getSqlMesTemplate().updateBatch(queryString, updateArgList);
				}
			}
			catch (Exception e)
			{
				throw new CustomException();
			}
		}
	}
}
