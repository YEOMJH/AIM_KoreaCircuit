package kr.co.aim.messolution.recipe.event;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.extended.object.management.data.RecipeParameter;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.collections.map.ListOrderedMap;
import org.jdom.Document;
import org.jdom.Element;

public class ChangeRecipeParameterList extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		//mantis 0000162
		List<Element> parameterList = SMessageUtil.getBodySequenceItemList(doc, "PARAMETERLIST", true);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Change", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		List<Object[]> batchArgs = new ArrayList<Object[]>();
		List<Object[]> batchHis = new ArrayList<Object[]>();
		
		List<Object[]> updateParaArgsList = new ArrayList<Object[]>();
		List<Object[]> updateOldParaArgsList = new ArrayList<Object[]>();
		List<Object[]> insertParaArgsList = new ArrayList<Object[]>();
		List<Object[]> insertParaHisArgsList = new ArrayList<Object[]>();
		List<Object[]> updateOldParaArgsList2 = new ArrayList<Object[]>();
		
		for (Element parameter : parameterList)
		{
			String machineName = SMessageUtil.getChildText(parameter, "MACHINENAME", true);
			String recipeName = SMessageUtil.getChildText(parameter, "RECIPENAME", true);
			String recipeParameterName = SMessageUtil.getChildText(parameter, "RECIPEPARAMETERNAME", true);
			String value = SMessageUtil.getChildText(parameter, "VALUE", true);
			
			List<Object> updateBindList = new ArrayList<Object>();
			List<Object> updateOldBindList = new ArrayList<Object>();
			List<Object> insertBindList = new ArrayList<Object>();
			List<Object> insertHisBindList = new ArrayList<Object>();
			List<Object> updateOldBindList2 = new ArrayList<Object>();
			
			StringBuffer sqlBuffer = new StringBuffer();
			sqlBuffer.append(" SELECT R.VALUE,R.LOWERLIMIT,R.UPPERLIMIT,R.TARGET,R.VALIDATIONTYPE");
			sqlBuffer.append("   FROM CT_RECIPEparameter R ");
			sqlBuffer.append("  WHERE ROWNUM=1 ");
			sqlBuffer.append("	 AND R.machineName= ? ");			
			sqlBuffer.append("   AND R.recipename = ? ");
			sqlBuffer.append("   AND R.RECIPEPARAMETERNAME = ? ");
			//sqlBuffer.append("   AND R.VALUE = ? ");
			String sqlStmt = sqlBuffer.toString();
			Object[] bindSet = new String[] { machineName, recipeName, recipeParameterName/*, value */};

			List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStmt, bindSet);
			
			StringBuffer sqlBuffer2 = new StringBuffer();
			sqlBuffer2.append(" SELECT ACTIVESTATE, INTFLAG, ENGFLAG, RMSFLAG ");
			sqlBuffer2.append("   FROM CT_RECIPE ");
			sqlBuffer2.append("  WHERE MACHINENAME =? ");
			sqlBuffer2.append("	 AND RECIPENAME = ? ");
			String sqlStmt2 = sqlBuffer2.toString();
			Object[] bindSet2 = new String[] { machineName, recipeName};

			List<ListOrderedMap> sqlResult2 = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStmt2, bindSet2);

			if(sqlResult2 != null && sqlResult2.size()>0)
			{
				//2020-11-25
				String activeState = (String) sqlResult2.get(0).get("ACTIVESTATE");
				String intFlag = (String) sqlResult2.get(0).get("INTFLAG");
				String engFlag = (String) sqlResult2.get(0).get("ENGFLAG");
				String rmsFlag = (String) sqlResult2.get(0).get("RMSFLAG");
				String checkAllFlag = CommonUtil.getEnumDefValueStringByEnumName("ChangeRecipeParameterListCheckAllFlag");
				
				if (sqlResult.size() > 0)
				{
					//Update RecipePara
					if(!activeState.toString().toString().equals("Active") || !checkAllFlag.equals("Y")){
						if(!intFlag.toString().equals("Y") || !checkAllFlag.equals("Y")){
							if(!engFlag.toString().equals("Y") || !checkAllFlag.equals("Y")){
								if(!rmsFlag.toString().equals("Y") || !checkAllFlag.equals("Y")){
									//mantis 
									String exRemachineName = SMessageUtil.getChildText(parameter, "MACHINENAME", false);
									String exRecipeName = SMessageUtil.getChildText(parameter, "RECIPENAME", false);
									String exRecipePara = SMessageUtil.getChildText(parameter, "RECIPEPARAMETERNAME", false);
									String exValue = SMessageUtil.getChildText(parameter, "VALUE", false);
									String exTarget = SMessageUtil.getChildText(parameter, "TARGET", false);
									String exLowLimit = SMessageUtil.getChildText(parameter, "LOWERLIMIT", false);
									String exUpperLimit = SMessageUtil.getChildText(parameter, "UPPERLIMIT", false);
									String exValiType = SMessageUtil.getChildText(parameter, "VALIDATIONTYPE", false);
									String exResult = SMessageUtil.getChildText(parameter, "RESULT", false);
									String oldValiType =(String) sqlResult.get(0).get("VALIDATIONTYPE");
									String oldValue = (String) sqlResult.get(0).get("VALUE");
									String oldTarget = (String) sqlResult.get(0).get("TARGET");
									String oldLowLimit = (String) sqlResult.get(0).get("LOWERLIMIT");
									String oldUpperLimit =(String) sqlResult.get(0).get("UPPERLIMIT");
									String checkFlag = SMessageUtil.getChildText(parameter, "CHECKFLAG", false);//(String) sqlResult.get(0).get("CHECKFLAG"); 
									
									updateBindList.add(exValue);
									updateBindList.add(exValiType);
									updateBindList.add(exTarget);
									updateBindList.add(exLowLimit);
									updateBindList.add(exUpperLimit);
									updateBindList.add(exResult);
									updateBindList.add(oldValiType);
									updateBindList.add(oldValue);
									updateBindList.add(oldTarget);
									updateBindList.add(oldLowLimit);
									updateBindList.add(oldUpperLimit);
									updateBindList.add(checkFlag);		
									updateBindList.add(exRemachineName);
									updateBindList.add(exRecipeName);
									updateBindList.add(exRecipePara);
									//updateBindList.add(exValue);
									
									updateParaArgsList.add(updateBindList.toArray());
									
								}else{
									//RMSFLAG Is 'Y'    RMS-045:RMSFLAG Is 'Y' Can Not Change, RecipeParameterName:{0}								
									 throw new CustomException("RMS-045",recipeParameterName);
								}
							}else{
								//ENGFLAG Is 'Y'     RMS-046:ENGFLAG Is 'Y' Can Not Change,  RecipeParameterName:{0}
								throw new CustomException("RMS-046",recipeParameterName);
							}
						}else{
							//INTFLAG Is 'Y'     RMS-047:INTFLAG Is  'Y' Can Not Change, RecipeParameterName:{0}
							throw new CustomException("RMS-047",recipeParameterName);
						}
					}else{
						//ACTIVESTATE Is 'Active'      RMS-048:ACTIVESTATE Is  'Active' Can Not Change, RecipeParameterName:{0}
						throw new CustomException("RMS-048",recipeParameterName);
					}
					
					
					
//					RecipeParameter recipeParamInfo = ExtendedObjectProxy.getRecipeParamService().selectByKey(false, new Object[] { machineName, recipeName, recipeParameterName, value });

//					ListOrderedMap temp = sqlResult.get(0);
//					String OLDTARGET = CommonUtil.getValue(temp, "TARGET");
//					String OLDLOWERLIMIT = CommonUtil.getValue(temp, "LOWERLIMIT");
//					String OLDUPPERLIMIT = CommonUtil.getValue(temp, "UPPERLIMIT");
//					String OLDVALIDATIONTYPE = CommonUtil.getValue(temp, "VALIDATIONTYPE");
	//
//					String target = SMessageUtil.getChildText(parameter, "TARGET", false);
//					String lowerLimit = SMessageUtil.getChildText(parameter, "LOWERLIMIT", false);
//					String upperLimit = SMessageUtil.getChildText(parameter, "UPPERLIMIT", false);
//					String validationType = SMessageUtil.getChildText(parameter, "VALIDATIONTYPE", false);		

					// validtaion MES Value and excel Value
//					if (!(CommonUtil.getValue(temp, "VALUE").equals(value)))
//					{
//						throw new CustomException("RECIPE-0002", recipeParameterName, CommonUtil.getValue(temp, "VALUE"), value);
//					}
	//
//					if ((recipeParameterName.length() >= 10 && (!recipeParameterName.substring(7, 10).equals("IND"))) || recipeParameterName.length() < 10)
//					{
//						if (validationType.equalsIgnoreCase("Range"))
//						{
//							if (upperLimit == "" || lowerLimit == "")
//							{
//								throw new CustomException("RECIPE-0003", recipeParameterName);
//							}
	//
//							try
//							{
//								Double.parseDouble(upperLimit);
//								Double.parseDouble(lowerLimit);
//								Double.parseDouble(CommonUtil.getValue(temp, "VALUE"));
//							}
//							catch (Exception e)
//							{
//								throw new CustomException("RECIPE-0004", recipeParameterName);
//							}
	//
//							if ((Double.parseDouble(upperLimit) >= Double.parseDouble(CommonUtil.getValue(temp, "VALUE")))
//									&& (Double.parseDouble(CommonUtil.getValue(temp, "VALUE")) >= Double.parseDouble(lowerLimit)))
//							{
//								target = "";
//								validationType = "Range";
//							}
//							else
//							{
//								throw new CustomException("RECIPE-0005", recipeParameterName, CommonUtil.getValue(temp, "VALUE"));
//							}
//						}
//						else if (validationType.equalsIgnoreCase("Target"))
//						{
//							if (target == "")
//							{
//								throw new CustomException("RECIPE-0006", recipeParameterName);
//							}
	//
//							if (CommonUtil.getValue(temp, "VALUE").equals(target)) 
//							{
//								lowerLimit = "";
//								upperLimit = "";
//								validationType = "Target";
//							}
//							else
//							{
//								throw new CustomException("RECIPE-0007", recipeParameterName, CommonUtil.getValue(temp, "VALUE"));
//							}
//						}
//						else
//						{
//							throw new CustomException("RECIPE-0008", recipeParameterName);
//						}

//						eventInfo = EventInfoUtil.makeEventInfo("Change Recipe Parameter", getEventUser(), getEventComment(), null, null);
//						eventInfo.setEventTimeKey(ConvertUtil.getCurrTimeKey());
//						eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
//					}
	//
//					if (!recipeParamInfo.getValidationType().equals(validationType))
//					{
//						Recipe recipe = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] { machineName, recipeName });
//						// recipe.setMFGFlag("N");
//						ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipe);
//					}
//					batchArgs.add(new Object[] { target, lowerLimit, upperLimit, validationType, OLDTARGET, OLDLOWERLIMIT, OLDUPPERLIMIT, OLDVALIDATIONTYPE, machineName, recipeName, recipeParameterName,
//							value });
	//
//					batchHis.add(new Object[] { machineName, recipeName, recipeParameterName, eventInfo.getEventTimeKey(), eventInfo.getEventName(), value, validationType, target, lowerLimit, upperLimit,
//							recipeParamInfo.getResult(), recipeParamInfo.getOLDVALUE(), OLDVALIDATIONTYPE, OLDTARGET, OLDLOWERLIMIT, OLDUPPERLIMIT, eventInfo.getEventUser(),
//							recipeParamInfo.getCheckFlag() });

				}
				else
				{
					String exRemachineName = SMessageUtil.getChildText(parameter, "MACHINENAME", false);
					String exRecipeName = SMessageUtil.getChildText(parameter, "RECIPENAME", false);
					String exRecipePara = SMessageUtil.getChildText(parameter, "RECIPEPARAMETERNAME", false);
					String exValue = SMessageUtil.getChildText(parameter, "VALUE", false);
					String exTarget = SMessageUtil.getChildText(parameter, "TARGET", false);
					String exLowLimit = SMessageUtil.getChildText(parameter, "LOWERLIMIT", false);
					String exUpperLimit = SMessageUtil.getChildText(parameter, "UPPERLIMIT", false);
					String exValiType = SMessageUtil.getChildText(parameter, "VALIDATIONTYPE", false);
					String exResult = SMessageUtil.getChildText(parameter, "RESULT", false);
					String checkFlag = SMessageUtil.getChildText(parameter, "CHECKFLAG", false);
					
					insertBindList.add(exRemachineName);
					insertBindList.add(exRecipeName);
					insertBindList.add(exRecipePara);
					insertBindList.add(exValue);
					insertBindList.add(exValiType);
					insertBindList.add(exTarget);
					insertBindList.add(exLowLimit);
					insertBindList.add(exUpperLimit);					
					insertBindList.add(exResult);
					insertBindList.add(checkFlag);
					insertParaArgsList.add(insertBindList.toArray());
				}
			}else{
				// RMS-049: Not Exist MachineName:{0} Add Data In CT_PARAMETER
				throw new CustomException("RMS-049", machineName);
			}
			
	
			//CT_RECIPEPARAMETERHISTORY
			if(sqlResult.size()>0){
				
				String oldValiType =(String) sqlResult.get(0).get("VALIDATIONTYPE");
				String oldValue = (String) sqlResult.get(0).get("VALUE");
				String oldTarget = (String) sqlResult.get(0).get("TARGET");
				String oldLowLimit = (String) sqlResult.get(0).get("LOWERLIMIT");
				String oldUpperLimit =(String) sqlResult.get(0).get("UPPERLIMIT");			
				String newTarget = SMessageUtil.getChildText(parameter, "TARGET", false);
				String newLowLimit = SMessageUtil.getChildText(parameter, "LOWERLIMIT", false);
				String newUpperLimit = SMessageUtil.getChildText(parameter, "UPPERLIMIT", false);
				String newValiType = SMessageUtil.getChildText(parameter, "VALIDATIONTYPE", false);
				String newResult = SMessageUtil.getChildText(parameter, "RESULT", false);
				String checkFlag = SMessageUtil.getChildText(parameter, "CHECKFLAG", false);//(String) sqlResult.get(0).get("CHECKFLAG"); 
				
				
				updateOldBindList.add(machineName);
				updateOldBindList.add(recipeName);
				updateOldBindList.add(recipeParameterName);
				updateOldBindList.add(eventInfo.getEventTimeKey());
				updateOldBindList.add(eventInfo.getEventName());
				updateOldBindList.add(value);
				updateOldBindList.add(newValiType);
				updateOldBindList.add(newTarget);
				updateOldBindList.add(newLowLimit);
				updateOldBindList.add(newUpperLimit);
				updateOldBindList.add(newResult);
				updateOldBindList.add(oldValue);
				updateOldBindList.add(oldValiType);			
				updateOldBindList.add(oldTarget);
				updateOldBindList.add(oldLowLimit);
				updateOldBindList.add(oldUpperLimit);
				updateOldBindList.add(eventInfo.getEventUser());
				updateOldBindList.add(checkFlag);
				updateOldParaArgsList.add(updateOldBindList.toArray());
			}else{
				
			
				String newTarget = SMessageUtil.getChildText(parameter, "TARGET", false);
				String newLowLimit = SMessageUtil.getChildText(parameter, "LOWERLIMIT", false);
				String newUpperLimit = SMessageUtil.getChildText(parameter, "UPPERLIMIT", false);
				String newValiType = SMessageUtil.getChildText(parameter, "VALIDATIONTYPE", false);
				String newResult = SMessageUtil.getChildText(parameter, "RESULT", false);
				String checkFlag = SMessageUtil.getChildText(parameter, "CHECKFLAG", false);
				
				
				updateOldBindList2.add(machineName);
				updateOldBindList2.add(recipeName);
				updateOldBindList2.add(recipeParameterName);
				updateOldBindList2.add(eventInfo.getEventTimeKey());
				updateOldBindList2.add(eventInfo.getEventName());
				updateOldBindList2.add(value);
				updateOldBindList2.add(newValiType);
				updateOldBindList2.add(newTarget);
				updateOldBindList2.add(newLowLimit);
				updateOldBindList2.add(newUpperLimit);
				updateOldBindList2.add(newResult);		
				updateOldBindList2.add(eventInfo.getEventUser());
				updateOldBindList2.add(checkFlag);
				updateOldParaArgsList2.add(updateOldBindList2.toArray());
			}
			
			
		}

		if(updateParaArgsList.size()>0){
			
			// Update CT_RECIPEPARAMETER		
			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE CT_RECIPEPARAMETER P  ");
			sql.append("SET P.VALUE = ?, P.VALIDATIONTYPE = ?, P.TARGET = ? ,P.LOWERLIMIT = ?, ");
			sql.append("   P.UPPERLIMIT = ?,P.RESULT = ?,P.OLDVALIDATIONTYPE =? , P.OLDVALUE =? ,			  ");
			sql.append("   P.OLDTARGET = ? , P.OLDLOWERLIMIT= ?, P.OLDUPPERLIMIT=?, P.CHECKFLAG=?	  ");
			sql.append(" WHERE P.MACHINENAME = ? AND P.RECIPENAME = ?            ");
			sql.append(" AND P.RECIPEPARAMETERNAME = ? ");
			//sql.append(" AND P.VALUE = ?        ");
			
			GenericServiceProxy.getSqlMesTemplate().updateBatch(sql.toString(), updateParaArgsList);
		}
		if(insertParaArgsList.size()>0){
			
			// Insert CT_RECIPEPARAMETER
			StringBuffer sql2 = new StringBuffer();
			sql2.append("INSERT INTO CT_RECIPEPARAMETER   ");
			sql2.append("(MACHINENAME, RECIPENAME, RECIPEPARAMETERNAME, VALUE, VALIDATIONTYPE, TARGET ");
			sql2.append(" ,LOWERLIMIT, UPPERLIMIT, RESULT, CHECKFLAG 	)		  ");
			sql2.append("  VALUES		  ");
			sql2.append(" (?,?,?,?,?,?,?,?,?,?) ");
			
			GenericServiceProxy.getSqlMesTemplate().updateBatch(sql2.toString(), insertParaArgsList);
		}

		if(updateOldParaArgsList.size()>0){
			
			// INSERT CT_RECIPEPARAMETERHISTORY
			StringBuilder sqlHis = new StringBuilder();
			sqlHis.append("INSERT INTO CT_RECIPEPARAMETERHISTORY ");
			sqlHis.append("   (MACHINENAME, RECIPENAME, RECIPEPARAMETERNAME, TIMEKEY, EVENTNAME, VALUE, ");
			sqlHis.append("    VALIDATIONTYPE, TARGET, LOWERLIMIT, UPPERLIMIT, RESULT, OLDVALUE, OLDVALIDATIONTYPE,   ");
			sqlHis.append("    OLDTARGET, OLDLOWERLIMIT, OLDUPPERLIMIT, EVENTUSER, CHECKFLAG) ");
			sqlHis.append(" VALUES ");
			sqlHis.append("   ( ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,? )");	
			
			GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlHis.toString(), updateOldParaArgsList);
		}
		if(updateOldParaArgsList2.size()>0){
			
            // INSERT CT_RECIPEPARAMETERHISTORY
			StringBuilder sqlHis = new StringBuilder();
			sqlHis.append("INSERT INTO CT_RECIPEPARAMETERHISTORY ");
			sqlHis.append("   (MACHINENAME, RECIPENAME, RECIPEPARAMETERNAME, TIMEKEY, EVENTNAME, VALUE, ");
			sqlHis.append("    VALIDATIONTYPE, TARGET, LOWERLIMIT, UPPERLIMIT, RESULT   ");
			sqlHis.append("    , EVENTUSER, CHECKFLAG) ");
			sqlHis.append(" VALUES ");
			sqlHis.append("   ( ?,?,?,?,?,?,?,?,?,?,?,?,? )");	
			
			GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlHis.toString(), updateOldParaArgsList2);
		}
		
		return doc;
	}
}
