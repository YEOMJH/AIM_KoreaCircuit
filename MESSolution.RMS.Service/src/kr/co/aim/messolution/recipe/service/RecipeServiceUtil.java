package kr.co.aim.messolution.recipe.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.extended.object.management.data.RecipeParameter;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskRecipe;
import kr.co.aim.messolution.extended.object.management.data.TPOffsetAlignInfo;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameErrorSignal;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.durable.management.data.DurableSpecKey;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processoperationspec.ProcessOperationSpecServiceProxy;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpecKey;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.user.UserServiceProxy;
import kr.co.aim.greentrack.user.management.data.UserProfile;
import kr.co.aim.greentrack.user.management.data.UserProfileKey;
 
public class RecipeServiceUtil {

	private Log logger = LogFactory.getLog(RecipeServiceUtil.class);
	
	private final String CR = "\n";
	private final String[] groupCode = new String[] {"$", "&"};

	public static String getMachineRecipeNameByLotList(List<Lot> lotList, String machineName) throws CustomException
	{
		if(lotList ==null || lotList.size()==0)
		{
			//SYSTEM-0010:{0}: The incoming variable value can not be empty!!
			throw new CustomException("SYSTEM-0010",Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		
		String sql = " SELECT DISTINCT P.MACHINERECIPENAME"
				   + " FROM LOT L, TPFOPOLICY T,POSMACHINE P"
				   + " WHERE 1=1 AND L.LOTNAME IN (:LOTLIST) "
				   + " AND L.FACTORYNAME = T.FACTORYNAME "
				   + " AND L.PRODUCTSPECNAME = T.PRODUCTSPECNAME "
				   + " AND L.PRODUCTSPECVERSION = T.PRODUCTSPECVERSION"
				   + " AND L.PROCESSFLOWNAME = T.PROCESSFLOWNAME "
				   + " AND L.PROCESSFLOWVERSION = T.PROCESSFLOWVERSION"
				   + " AND L.PROCESSOPERATIONNAME = T.PROCESSOPERATIONNAME "
				   + " AND L.PROCESSOPERATIONVERSION = T.PROCESSOPERATIONVERSION "
				   + " AND T.CONDITIONID = P.CONDITIONID "
				   + " AND P.MACHINENAME = :MACHINENAME ";
		
		Map<String,Object> bindMap = new HashMap<>();
		bindMap.put("LOTLIST", CommonUtil.makeToStringList(lotList));
		bindMap.put("MACHINENAME", machineName);

		List<Map<String,Object>> resultList = null;
		
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		}
		catch (Exception ex)
		{
			if (ex instanceof greenFrameErrorSignal)
				throw new CustomException("SYS-9999", "POSMachine", ex.getMessage());
			else
				throw new CustomException(ex.getCause());
		}

		if (resultList == null || resultList.size() == 0)
		{
			//RMS-043: No recipe registered on machine [{0}] was found.Policy condition by [T= {1},P= {2},F= {3},O= {4}]
			throw new CustomException("RMS-042", machineName,lotList.get(0).getFactoryName(),lotList.get(0).getProductSpecName(),lotList.get(0).getProcessFlowName(),lotList.get(0).getProcessOperationName());
		}
		else if (resultList.size() > 1)
		{
			//RMS-043: The materials to be prepared for operation on machine[{0}] include multiple recieps.
			throw new CustomException("RMS-043", machineName);
		}
		else if (ConvertUtil.getMapValueByName(resultList.get(0), "MACHINERECIPENAME").isEmpty())
		{
			//RMS-044: There is wrong recipe registration information on the machine [{0}], the value is empty or null.Policy condition by [T= {1},P= {2},F= {3},O= {4}]
			throw new CustomException("RMS-044", machineName,lotList.get(0).getFactoryName(),lotList.get(0).getProductSpecName(),lotList.get(0).getProcessFlowName(),lotList.get(0).getProcessOperationName());
		}
		
		return String.valueOf(resultList.get(0).get("MACHINERECIPENAME"));
	}
	
	public String getMachineRecipe(String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion, 
				String processOperationName, String processOperationVersion, String machineName, boolean isVerified) throws CustomException
	{
		logger.info(String.format("entry to [%s] at [%d]", "getMachineRecipe", System.currentTimeMillis()));
		logger.info("Level [D] Recipe validation behavior begins");

		ListOrderedMap instruction = PolicyUtil.getMachineRecipeName(factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion, machineName);

		String designatedRecipeName = CommonUtil.getValue(instruction, "MACHINERECIPENAME");
		
		{
			if (StringUtil.isEmpty(designatedRecipeName))
				throw new CustomException("MACHINE-0102", designatedRecipeName);
		}
		
		logger.info(String.format("quit from [%s] at [%d]", "getMachineRecipe", System.currentTimeMillis()));
		
		return designatedRecipeName;
	}
	
	public String getMachineRecipe(String factoryName, String durableSpecName, String durableName, String machineName, boolean isVerified)
		throws CustomException
	{
		logger.info(String.format("entry to [%s] at [%d]", "getMachineRecipe", System.currentTimeMillis()));
	
		//finally return
		String result = "";
		
		DurableSpecKey duralbeSpecKey = new DurableSpecKey();
		duralbeSpecKey.setFactoryName(factoryName);
		duralbeSpecKey.setDurableSpecName(durableSpecName);
		duralbeSpecKey.setDurableSpecVersion("00001");
		
		DurableSpec durableSpec = DurableServiceProxy.getDurableSpecService().selectByKey(duralbeSpecKey);
		
		String cleanRecipeName = CommonUtil.getValue(durableSpec.getUdfs(), "MASKRECIPENAME");
		
		String designatedRecipeName = cleanRecipeName;
		
		{//mandatory validation
			//if (StringUtil.isEmpty(machineGroupName))
			//	throw new CustomException("MACHINE-0101", machineGroupName);
		
			if (StringUtil.isEmpty(designatedRecipeName))
				throw new CustomException("MACHINE-0102", designatedRecipeName);
		}
		
		
		result = designatedRecipeName;
		
		logger.info(String.format("quit from [%s] at [%d]", "getMachineRecipe", System.currentTimeMillis()));
		
		return result;
	}

	public String getEASSubjectName(String machineName)
	{
		String subjectName = "";
		String superMachineName = "";
		String detailMachineType = "";

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT MS.MACHINENAME, ");
		sql.append("       MS.SUPERMACHINENAME, ");
		sql.append("       MS.DETAILMACHINETYPE, ");
		sql.append("       M.MCSUBJECTNAME ");
		sql.append("  FROM MACHINESPEC MS, MACHINE M ");
		sql.append(" WHERE 1 = 1 ");
		sql.append("   AND MS.MACHINENAME = M.MACHINENAME ");
		sql.append("   AND MS.MACHINENAME = :MACHINENAME ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("MACHINENAME", machineName);

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			detailMachineType = ConvertUtil.getMapValueByName(result.get(0), "DETAILMACHINETYPE");
			superMachineName = ConvertUtil.getMapValueByName(result.get(0), "SUPERMACHINENAME");

			if (StringUtils.equals(detailMachineType, GenericServiceProxy.getConstantMap().DetailMachineType_Main))
			{
				subjectName = ConvertUtil.getMapValueByName(result.get(0), "MCSUBJECTNAME");
			}
			else if (StringUtils.equals(detailMachineType, GenericServiceProxy.getConstantMap().DetailMachineType_Unit))
			{
				sql.setLength(0);
				sql.append("SELECT MCSUBJECTNAME FROM MACHINE WHERE MACHINENAME = :SUPERMACHINENAME ");

				args.put("SUPERMACHINENAME", superMachineName);
				result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

				// Get SubjectName from SQL result
				subjectName = ConvertUtil.getMapValueByName(result.get(0), "MCSUBJECTNAME");
			}
			else if (StringUtils.equals(detailMachineType, GenericServiceProxy.getConstantMap().DetailMachineType_SubUnit))
			{
				sql.setLength(0);
				sql.append("SELECT MCSUBJECTNAME ");
				sql.append("  FROM MACHINE ");
				sql.append(" WHERE MACHINENAME = (SELECT SUPERMACHINENAME ");
				sql.append("                        FROM MACHINE ");
				sql.append("                       WHERE MACHINENAME = :SUPERMACHINENAME ) ");

				args.put("SUPERMACHINENAME", superMachineName);
				result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

				// Get SubjectName from SQL result
				subjectName = ConvertUtil.getMapValueByName(result.get(0), "MCSUBJECTNAME");
			}
			else if (StringUtils.equals(detailMachineType, GenericServiceProxy.getConstantMap().DetailMachineType_SubSubUnit))
			{
				sql.setLength(0);
				sql.append("SELECT MCSUBJECTNAME ");
				sql.append("  FROM MACHINE ");
				sql.append(" WHERE MACHINENAME = ");
				sql.append("          (SELECT SUPERMACHINENAME ");
				sql.append("             FROM MACHINE ");
				sql.append("            WHERE MACHINENAME = (SELECT SUPERMACHINENAME ");
				sql.append("                                   FROM MACHINE ");
				sql.append("                                  WHERE MACHINENAME = :SUPERMACHINENAME )) ");

				args.put("SUPERMACHINENAME", superMachineName);
				result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

				// Get SubjectName from SQL result
				subjectName = ConvertUtil.getMapValueByName(result.get(0), "MCSUBJECTNAME");
			}
		}
		logger.info("Target Subject Name = " + subjectName);
		return subjectName;
	}

	private String generateAssignedRecipeQuery(String condition)
	{
		StringBuffer sqlBuffer = new StringBuffer("");
		
		sqlBuffer.append("SELECT G.machineGroupName, M.machineName, S.recipeNameSpaceName, R.recipeName, R.recipeType").append(CR)
		.append("    FROM CT_MachineGroup G, MachineSpec M, RecipeNameSpace S, Recipe R").append(CR)
		.append("WHERE G.machineName = M.machineName").append(CR)
		.append("    AND M.machineName = S.recipeNameSpaceName").append(CR)
		.append("    AND S.recipeNameSpaceName = R.recipeNameSpaceName").append(CR)
		.append("    AND S.recipeType = R.recipeType").append(CR)
		.append("    AND G.machineGroupName = ?").append(CR)
		.append("    AND G.machineName = ?").append(CR)
		.append("    AND R.recipeName = ?").append(CR);
		
		if (!StringUtil.isEmpty(condition))
			sqlBuffer.append(condition);
		
		return sqlBuffer.toString();
	}

	
	public Recipe verifyMachineRecipe(MachineSpec machineData, String portName, String carrierName, String recipeName)
		throws CustomException
	{
		logger.info("Level [E] Recipe validation behavior begins");
		
		String machineName = machineData.getKey().getMachineName();
		//String flag = CommonUtil.getValue(machineData.getUdfs(), "RMLEVEL");
		
		Document res = MESRecipeServiceProxy.getRecipeServiceImpl().inquiryRecipeList(machineName, portName, carrierName);
		
		//existence verification in MES
		Recipe recipeData;
		try
		{
			recipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] {machineName, recipeName});
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "RMS", fe.getMessage());
		}
		catch (greenFrameErrorSignal nfe)
		{
			throw new CustomException("SYS-9999", "RMS", nfe.getMessage());
		}
		
		//approval validation
		if (!recipeData.getRecipeState().equals("Approved"))
		{
            //RMS-006: RMSError:Recipe[{0}] is not permitted		
			throw new CustomException("RMS-006", recipeName);
		}
		
		//existence verification in EQP
		boolean isFound = false;
		for (Element eleRecipe : SMessageUtil.getBodySequenceItemList(res, "RECIPELIST", false))
		{
			String hostRecipeName = SMessageUtil.getChildText(eleRecipe, "RECIPENAME", true);
			
			if (recipeName.equals(hostRecipeName))
			{
				isFound = true;
				break;
			}
				
		}
		
		if (!isFound)
		{
			//RMS-015: RMSError:Recipe[{0}] does not exist in EQP
			throw new CustomException("RMS-015", recipeName);
		}
		
		logger.info("Level [E] Recipe validation behavior completed");
		
		//proceed to next level
		return recipeData;
	}
	
	public void verifyMachineRecipe(String machineName, String recipeName)
			throws CustomException
	{
		try
		{
			String sql = generateAssignedRecipeQuery("");
			Object[] bindList = new Object[] {machineName, recipeName};
				
			List<ListOrderedMap> resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindList);
			
			if (resultList.size() < 1)
				throw new CustomException("MACHINE-0103", recipeName, machineName,"");
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "RMS", fe.getMessage());
		}
	}
	
	
	public List<SequenceParameter> verifyMachineRecipe(MachineSpec machineData, String portName, String carrierName, Recipe recipeData, String RMSFlag, String VersionFlag)
		throws CustomException
	{
		logger.info("Level [S] Recipe validation behavior begins");
		
		String recipeName = recipeData.getRecipeName();
		String machineName = machineData.getKey().getMachineName();
		Document res = MESRecipeServiceProxy.getRecipeServiceImpl().inquiryParameterList(machineName, portName, carrierName, recipeName);
		List<SequenceParameter> seqRecipeList = new ArrayList<SequenceParameter>();
		Map<String, Document> dicRecipe = new HashMap<String, Document>();
		
		if(!VersionFlag.equals("Y"))
		{
			String  LASTCHANGETIME     = SMessageUtil.getBodyItemValue(res, "LASTCHENGETIME", true);
			  StringBuffer sqlBufferLCT = new StringBuffer("")
	          .append("  SELECT R.VERSION")
	          .append("  FROM CT_RECIPE R ")
	          .append("  WHERE R.machineName = ?")
	          .append("  AND R.recipeName= ? ");
		      String sqlStmtVERSION = sqlBufferLCT.toString();
	          Object[] bindSetVERSION = new String[]{machineName, recipeName};
	          List<ListOrderedMap> sqlResultVERSION = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStmtVERSION, bindSetVERSION); 
	          if (sqlResultVERSION.size()>0){
	       		ListOrderedMap TEMPVERSION=  sqlResultVERSION.get(0);  
	       		String  VERSION =  CommonUtil.getValue(TEMPVERSION, "VERSION");    
	       	   logger.info(VERSION);
	       	   logger.info(LASTCHANGETIME);
				if (!VERSION.equals(LASTCHANGETIME))
				{
					// RMS-016: RMSError:the Recipe [{0}]Of the Machine [{1}]has incorrect RecipeVersion
					throw new CustomException("RMS-016", recipeName, machineName);
				}
	           }
		}
				
          
          List<RecipeParameter> paramList;
  		
			try
			{ 
				paramList = ExtendedObjectProxy.getRecipeParamService().select("machineName = ? AND recipeName = ? ",
								new Object[] {machineName, recipeName});
			}
			catch (FrameworkErrorSignal fe)
			{
				logger.warn(String.format("Nothing to defined in parameters about Recipe[%s] of Unit[%s]",machineName, recipeName));
				paramList = new ArrayList<RecipeParameter>();
			}
			catch (greenFrameErrorSignal nfe)
			{
				logger.warn(String.format("Nothing to defined in parameters about Recipe[%s] of Unit[%s]", machineName, recipeName));
				paramList = new ArrayList<RecipeParameter>();
			}
			
			if (paramList.size() > 0)
			{
                 logger.info("================ List Check Start======================");		
			String recipeType = SMessageUtil.getBodyItemValue(res, "RECIPETYPE", true);
		 
		 logger.info("================check list if or not add start======================");       
			for (Element parameterGroup : SMessageUtil.getBodySequenceItemList(res, "COMMANDLIST", false))
                    {   
				 String 	parameterName  = SMessageUtil.getChildText(parameterGroup, "UNITNAME", false) ;
				              
				for (Element parameter : SMessageUtil.getSubSequenceItemList(parameterGroup, "PARAMETERLIST", false))
				           { 
				                 String  value = SMessageUtil.getChildText(parameter, "VALUE", false); 
						         StringBuffer sqlBuffer = new StringBuffer("")
			                    .append("  SELECT *")
			                    .append("  FROM CT_RECIPEPARAMETER R ")
			                    .append("  WHERE R.machineName = ?")
			                    .append("  AND R.recipeName= ? ")
						        .append("  AND R.recipeparametername= ?");
						         String sqlStmt = sqlBuffer.toString();
			                     Object[] bindSet = new String[]{machineName, recipeName,parameterName};
			                    List<ListOrderedMap> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStmt, bindSet); 
								if (sqlResult.size() < 1)
								{
									// RMS-017: RMSError:The Recipe[{0}] has Add the item[{1}]
									throw new CustomException("RMS-017", recipeName, parameterName);
								}
			                  else 
			                   {
			                   }
				          }
               }
			logger.info("================check list if or not add End======================");
			
			
			logger.info("================check list if or not delete Start======================");
			for (RecipeParameter paramData : paramList){		
				boolean ISFOUND = false;
				for (Element parameterGroup : SMessageUtil.getBodySequenceItemList(res, "COMMANDLIST", false)){
					 String 	parameterName  = SMessageUtil.getChildText(parameterGroup, "UNITNAME", false) ;
					for (Element parameter : SMessageUtil.getSubSequenceItemList(parameterGroup, "PARAMETERLIST", false)){
						if( paramData.getRecipeParameterName().equals(parameterName)){
							logger.info(paramData.getRecipeParameterName());
							logger.info(parameterName);
							ISFOUND = true;
						}
						if (ISFOUND) break;	
						 
					}
				
					if (ISFOUND) break;	
				}	
				if (ISFOUND) continue;	
				else{
					//RMS-018: RMSError:The Recipe[{0}] has Deleted the item[{1}]
					throw new CustomException("RMS-018",recipeName,paramData.getRecipeParameterName());
				}
			}
			logger.info("================check list if or not delete End======================");
			
			logger.info("================List Check end ======================");	
          
          
          
			}  
          
          
          
             for (Element parameterGroup : SMessageUtil.getBodySequenceItemList(res, "COMMANDLIST", false))
			     {
				    String unitName = SMessageUtil.getChildText(parameterGroup, "UNITNAME", false);
					for (Element parameter : SMessageUtil.getSubSequenceItemList(parameterGroup, "PARAMETERLIST", false))
					{ 
						String unitRecipeName = SMessageUtil.getChildText(parameter, "VALUE", true);
				         StringBuffer sqlBuffer = new StringBuffer("")
	                    .append("  SELECT Target")
	                    .append("  FROM CT_RECIPEPARAMETER R ")
	                    .append("  WHERE R.machineName = ?")
	                    .append("  AND R.recipeName= ? ")
				        .append("  AND R.recipeparametername= ?")
				         .append("  AND R.validationType= 'Exact'");
				         String sqlStmt = sqlBuffer.toString();
	                     Object[] bindSet = new String[]{machineName, recipeName,unitName};
	                     List<ListOrderedMap> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStmt, bindSet); 
	                     if (sqlResult.size()>0){
	                 		ListOrderedMap temp=  sqlResult.get(0);  
	                 	   String     Target =  CommonUtil.getValue(temp, "Target");
	                 	  logger.info(Target);
	                  	   logger.info(unitRecipeName);
	                 	   if(!Target.equals(unitRecipeName))
	                 	   {
	                 		   //RMS-019 : RMSError:[{0}]Valid standard is [{1}] but current parameter is [{2}]
	                 		   throw new CustomException("RMS-019",  unitName,Target, unitRecipeName);
	                 	   }
	                     }
		           	}
			}
			
		for (Element parameterGroup : SMessageUtil.getBodySequenceItemList(res, "COMMANDLIST", false))
		{
			String unitName = SMessageUtil.getChildText(parameterGroup, "UNITNAME", false);
			
			try
			{
				//String unitName = SMessageUtil.getChildText(parameterGroup, "UNITNAME", true);
				//PPID list per unit
				Document resA = new Document();
				//RMSFlag
				if(RMSFlag.equals("Y"))
				{
					//Get Recipe List From DB Like OIC
					resA = getRecipeListBySuperMachine(unitName);
				}
				else
				{
					resA = MESRecipeServiceProxy.getRecipeServiceImpl().inquiryRecipeList(unitName, portName, carrierName);
				}
				
				dicRecipe.put(unitName, resA);
				
				for (Element parameter : SMessageUtil.getSubSequenceItemList(parameterGroup, "PARAMETERLIST", false))
				{
					String unitRecipeName = SMessageUtil.getChildText(parameter, "VALUE", true);
					
					SequenceParameter entity = new SequenceParameter(unitName, unitRecipeName);
					
					seqRecipeList.add(entity);
				}
			}
			catch (Exception ex)
			{
				logger.error(String.format("Unit Recipe info from Unit[%s] parsing failed", unitName));
			}
		}
		
		//sequence set integrity check
		if (seqRecipeList.size() < 1)
		{
			//RMS-020:RMSError:Host Recipe[{0}] does not have any sequence
			throw new CustomException("RMS-020", recipeName);
		}
		
		for (SequenceParameter seqRecipeData : seqRecipeList)
		{
			//existence check-up in MES
			Recipe unitRecipeData;
			try
			{
				unitRecipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] {seqRecipeData.getUnitName(), seqRecipeData.getUnitRecipeName()});
			}
			catch (FrameworkErrorSignal fe)
			{
				throw new CustomException("SYS-9999", "RMS", fe.getMessage());
			}
			catch (greenFrameErrorSignal nfe)
			{
				throw new CustomException("SYS-9999", "RMS", nfe.getMessage());
			}
			
			//activation check
			if (!unitRecipeData.getActiveState().equals(GenericServiceProxy.getConstantMap().Spec_Active))
			{
				//RMS-021: RMSError:Unit Recipe[{0}] of Unit[{1}] is inactive
				throw new CustomException("RMS-021", seqRecipeData.getUnitRecipeName(), seqRecipeData.getUnitName());
			}
			
			//existence verification in EQP
			//Document resA = MESRecipeServiceProxy.getRecipeServiceImpl().inquiryRecipeList(seqRecipeData.getUnitName(), portName, carrierName);
			Document docUnitRecipeList = dicRecipe.get(unitRecipeData.getMachineName());
			boolean isFound = false;
			for (Element eleRecipe : SMessageUtil.getBodySequenceItemList(docUnitRecipeList, "RECIPELIST", false))
			{
				String unitRecipeName = SMessageUtil.getChildText(eleRecipe, "RECIPENAME", true);
				
				if (unitRecipeData.getRecipeName().equals(unitRecipeName))
				{
					isFound = true;
					break;
				}
			}
			
			if (!isFound)
			{
				//RMS-022: RMSError:Recipe[{0}] does not exist in Unit[{1}]
				throw new CustomException("RMS-022", unitRecipeData.getRecipeName(), unitRecipeData.getMachineName());
			}
		}
		
		logger.info("Level [S] Recipe validation behavior completed");
		
		//proceed to next level
		return seqRecipeList;
	}
	
	
	String value;
	String parameterName1;
	List<RecipeParameter> paramListA;
	
	public void verifyMachineGroup(String factoryName, String machineGroupName, String machineName) throws CustomException
	{
		boolean isFound = false;

		for (ListOrderedMap machineRow : PolicyUtil.getAvailableMachine(factoryName, machineGroupName))
		{
			if (CommonUtil.getValue(machineRow, "MACHINENAME").equals(machineName))
			{
				// this machine is available in this operation
				isFound = true;

				break;
			}
		}

		if (!isFound)
			throw new CustomException("MACHINE-0104", machineName, machineGroupName);
	}


	public void verifyUserPrivilege(String userId, String machineName) throws CustomException
	{
		logger.info("RMS accessibility authorization begins...");
		
		MachineSpec machineData = this.getRootMachine(machineName);
		//override base parameter
		machineName = machineData.getKey().getMachineName();
		
		
		String departName = CommonUtil.getValue(machineData.getUdfs(), "RMSDEPARTMENT");
		
		if (StringUtil.isNotEmpty(departName))
		{
			try
			{
				UserProfileKey keyInfo = new UserProfileKey(userId);
				UserProfile userData = UserServiceProxy.getUserProfileService().selectByKey(keyInfo);
				
				String userDepartName = CommonUtil.getValue(userData.getUdfs(), "DEPARTMENT");
				
				StringTokenizer token = new StringTokenizer(departName, ",");
				boolean departmentCheck = false;
				
				if (StringUtil.isNotEmpty(userDepartName))
				{
					while(token.hasMoreTokens())
					{
						String ck = token.nextElement().toString();
						if(ck.equals(userDepartName))
						{
							departmentCheck = true;
							break;
						}
					}
					if (!departmentCheck)
					{
						//RMS-023: RMSError:User[{0}] in Depart[{1}] not allowed to control Machine[{2}] under Depart[{3}]
						throw new CustomException("RMS-023",userId, userDepartName, machineName, departName);
					}
					/*
					if (!departName.equals(userDepartName)){
						//RMS-023: RMSError:User[{0}] in Depart[{1}] not allowed to control Machine[{2}] under Depart[{3}]
						throw new CustomException("RMS-023",userId, userDepartName, machineName, departName);
					}
					*/
				}
				else
				{
					logger.warn(String.format("User[%s] is not member of any department", userId));
				}
				
				logger.info(String.format("User[%s] in Depart[%s] be allowed to work with Machine[%s] under Depart[%s]",
											userId, userDepartName, machineName, departName));
			}
			catch (NotFoundSignal ne)
			{
				//RMS-024: RMSError:User not found in MES
				throw new CustomException("RMS-024");
			}
		}
		else
		{
			logger.warn(String.format("Machine[%s] not controlled by any department", machineName));
		}
	}
	
	
	public MachineSpec getRootMachine(String machineName) throws CustomException
	{
		MachineSpec machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
		
		if (!machineData.getDetailMachineType().equals("MAIN")
				&& !machineData.getSuperMachineName().isEmpty())
			machineData = getRootMachine(machineData.getSuperMachineName());
		
		return machineData;
	}
	
	
	public Document generateRecipeInquiry(String machineName, String portName, String carrierName, String targetName, String recipeType)
		throws CustomException
	{
		try
		{
			Element bodyElement = new Element(SMessageUtil.Body_Tag);
			{
				Element attMachineName = new Element("MACHINENAME");
				attMachineName.setText(machineName);
				bodyElement.addContent(attMachineName);
				
				Element attPortName = new Element("PORTNAME");
				attPortName.setText(portName);
				bodyElement.addContent(attPortName);
				
				Element attDurableName = new Element("CARRIERNAME");
				attDurableName.setText(carrierName);
				bodyElement.addContent(attDurableName);
				
				Element attTargetName = new Element("TARGETNAME");
				attTargetName.setText(targetName);
				bodyElement.addContent(attTargetName);
				
				Element attRecipeType = new Element("RECIPETYPE");
				attRecipeType.setText(recipeType);
				bodyElement.addContent(attRecipeType);
			}
			
			Document doc = SMessageUtil.createXmlDocument(bodyElement, "RecipeValidRequest", "", "", "MES", "");
			
			return doc;
		}
		catch (Exception ex)
		{
			//RMS-025: RMSError:{0} writing failed
			throw new CustomException("RMS-025","RecipeValidRequest");
		}
	}

	public Document generateRecipeInquiryForEQP(String machineName, String portName, String carrierName, String recipeName, String version, String checkLevel) throws CustomException
	{
		try
		{
			Element bodyElement = new Element(SMessageUtil.Body_Tag);
			{
				Element attMachineName = new Element("MACHINENAME");
				attMachineName.setText(machineName);
				bodyElement.addContent(attMachineName);

				Element attPortName = new Element("PORTNAME");
				attPortName.setText(portName);
				bodyElement.addContent(attPortName);

				Element attDurableName = new Element("CARRIERNAME");
				attDurableName.setText(carrierName);
				bodyElement.addContent(attDurableName);

				Element attRecipeName = new Element("RECIPENAME");
				attRecipeName.setText(recipeName);
				bodyElement.addContent(attRecipeName);

				Element recipeType = new Element("RECIPETYPE");
				recipeType.setText(checkLevel);
				bodyElement.addContent(recipeType);

				Element attVersion = new Element("VERSION");
				attVersion.setText(version);
				bodyElement.addContent(attVersion);
			}
			Document doc = SMessageUtil.createXmlDocument(bodyElement, "RecipeValidationRequest", "", "", "MES", "");

			return doc;
		}
		catch (Exception ex)
		{
			throw new CustomException("MACHINE-0017", "RecipeValidRequest writing failed");
		}
	}

//	public Document generateRecipeInquiryForEQP(Document doc, String machineName, String portName, String carrierName, String recipeName, String version)
//			throws CustomException
//		{
//			try
//			{
//				SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "RecipeValidRequest");
//				
//				Element bodyElement = new Element(SMessageUtil.Body_Tag);
//				{
//					Element attMachineName = new Element("MACHINENAME");
//					attMachineName.setText(machineName);
//					bodyElement.addContent(attMachineName);
//					
//					Element attPortName = new Element("PORTNAME");
//					attPortName.setText(portName);
//					bodyElement.addContent(attPortName);
//					
//					Element attDurableName = new Element("CARRIERNAME");
//					attDurableName.setText(carrierName);
//					bodyElement.addContent(attDurableName);
//					
//					Element attRecipeName = new Element("RECIPENAME");
//					attRecipeName.setText(recipeName);
//					bodyElement.addContent(attRecipeName);
//					
//					Element attVersion = new Element("VERSION");
//					attVersion.setText(version);
//					bodyElement.addContent(attVersion);
//				}
//
//				doc.getRootElement().removeChild(SMessageUtil.Body_Tag);
//				doc.getRootElement().addContent(2, bodyElement);
//				
//				return doc;
//			}
//			catch (Exception ex)
//			{
//					//RMS-025: RMSError:{0} writing failed
	                //throw new CustomException("RMS-025","RecipeValidRequest");
//			}
//		}
//	
	public Document generateRecipeValidationResult(String machineName, String portName, String carrierName, String flag, String description)	throws CustomException
	{
		try
		{
//			MACHINENAME 
//			PORTNAME
//			CARRIERNAME
//			RECIPENAME
//			RESULT
//			RESULTDESCRIPTION

			Element bodyElement = new Element(SMessageUtil.Body_Tag);
			{
				Element attMachineName = new Element("MACHINENAME");
				attMachineName.setText(machineName);
				bodyElement.addContent(attMachineName);
				
				Element attPortName = new Element("PORTNAME");
				attPortName.setText(portName);
				bodyElement.addContent(attPortName);
				
				Element attDurableName = new Element("CARRIERNAME");
				attDurableName.setText(carrierName);
				bodyElement.addContent(attDurableName);
				
				Element attResult = new Element("RESULT");
				attResult.setText(flag);
				bodyElement.addContent(attResult);
				
				Element attResultDesc = new Element("RESULTDESCRIPTION");
				attResultDesc.setText(description);
				bodyElement.addContent(attResultDesc);
			}
			Document doc = SMessageUtil.createXmlDocument(bodyElement, "RecipeValidationResult", "", "", "MES", "");
			
			return doc;
		}
		catch (Exception ex)
		{
			throw new CustomException("MACHINE-0016");
		}
	}			
	
	
	public Document generateParameterInquiry(String machineName, String portName, String carrierName, String targetName, String recipeName, String recipeType)
		throws CustomException                
	{
		try
		{
			Element bodyElement = new Element(SMessageUtil.Body_Tag);
			{
				Element attMachineName = new Element("MACHINENAME");
				attMachineName.setText(machineName);
				bodyElement.addContent(attMachineName);
				
				if (!recipeType.equalsIgnoreCase("S")){
				    Element attUnitName = new Element("UNITNAME");
				    attUnitName.setText(targetName);
				    bodyElement.addContent(attUnitName);
				
				    Element attSubUnitName = new Element("SUBUNITNAME");
				    attSubUnitName.setText("");
				    bodyElement.addContent(attSubUnitName);}
				else {
					String targetName1=targetName.substring(0, 10);
					Element attUnitName = new Element("UNITNAME");
					attUnitName.setText(targetName1);
					bodyElement.addContent(attUnitName);
					
					Element attSubUnitName = new Element("SUBUNITNAME");
					attSubUnitName.setText(targetName);
					bodyElement.addContent(attSubUnitName);
				}
				
				Element attPortName = new Element("PORTNAME");
				attPortName.setText(portName);
				bodyElement.addContent(attPortName);
				
				Element attDurableName = new Element("CARRIERNAME");
				attDurableName.setText(carrierName);
				bodyElement.addContent(attDurableName);
				
				Element attTargetName = new Element("TARGETNAME");
				attTargetName.setText(targetName);
				bodyElement.addContent(attTargetName);
				
				Element attRecipeName = new Element("RECIPENAME");
				attRecipeName.setText(recipeName);
				bodyElement.addContent(attRecipeName);
				
				Element attRecipeType = new Element("RECIPETYPE");
				attRecipeType.setText(recipeType);
				bodyElement.addContent(attRecipeType);
			}
			
			Document doc = SMessageUtil.createXmlDocument(bodyElement, "RecipeParameterValidRequest", "", "", "MES", "");
			
			return doc;
		}
		catch (Exception ex)
		{
			//RMS-025: RMSError:{0} writing failed
			throw new CustomException("RMS-025","RecipeParameterValidRequest");
		}
	}
	
	
	public void verifyRecipeParameter(RecipeParameter paramData, String value)
		throws CustomException
	{
		
		String validationType = paramData.getValidationType();
		logger.debug(String.format("Validation formula is [%s] and current parameter is [%s]", validationType, value));
		
		if (validationType.equals("Exact"))
		{//numeric or literal
			if (!StringUtil.equals(paramData.getTarget(), value))
			{
				//RMS-026: RMSError:[{0}]Valid standard is [{1}] but current parameter is [{2}]
				throw new CustomException("RMS-026",  paramData.getRecipeParameterName(),paramData.getTarget(), value);
			}
		}
		else if (validationType.equals("Range"))
		{//only numeric
			try
			{
				double upperLimit = Double.parseDouble(paramData.getUpperLimit());
				double lowerLimit = Double.parseDouble(paramData.getLowerLimit());
				double val = Double.parseDouble(value);
				
				if (val > upperLimit || val < lowerLimit)
				{
					//RMS-027: RMSError:[{0}]Current Parameter[{1}] should be between [{2}] and [{3}]
					throw new CustomException("RMS-027",  paramData.getRecipeParameterName(),value, paramData.getLowerLimit(), paramData.getUpperLimit());
				}
			}
			catch (NumberFormatException ne)
			{
				//RMS-028: RMSError:Number conversion is failed, range validation is enable only if numeric
				throw new CustomException("RMS-028");
			}
		}
		
	}
	
	
	private RecipeParameter upadteRecipeParameter(EventInfo eventInfo, String recipeParameterName, String value, String recipeName,  String machineName)
	throws CustomException
{
	RecipeParameter recipeParamInfo = ExtendedObjectProxy.getRecipeParamService().selectByKey(false,
										new Object[] {  machineName, recipeName, recipeParameterName, value});
	//default spec info
	recipeParamInfo.setValue(value);
	
	eventInfo.setEventName("Change");
	recipeParamInfo = ExtendedObjectProxy.getRecipeParamService().modify(eventInfo, recipeParamInfo);
	
	return recipeParamInfo;
}	
	
	private Document getRecipeListBySuperMachine(String machineName)
			throws CustomException
	{
		Document doc = new Document();
		
		//Get recipe by SuperMachine
		StringBuffer sqlBuffer = new StringBuffer("")
				.append(" SELECT P.RECIPEPARAMETERNAME AS MACHINENAME, ")
				.append("        P.VALUE AS TARGETNAME, ")
				.append("        CASE WHEN MS.DETAILMACHINETYPE = 'UNIT' THEN 'U' ELSE 'S' END ")
				.append("           AS RECIPETYPE ")
				.append("   FROM CT_RECIPEPARAMETER P, MACHINESPEC MS, MACHINESPEC SMS ")
				.append("  WHERE     P.RECIPEPARAMETERNAME = ? ")
				.append("        AND P.RECIPEPARAMETERNAME = MS.MACHINENAME ")
				.append("        AND MS.SUPERMACHINENAME = SMS.MACHINENAME ")
				.append("        AND P.MACHINENAME = SMS.MACHINENAME ");

	    Object[] bind = new String[]{machineName};
	    
	    List<ListOrderedMap> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bind); 
        
	    if (sqlResult.size()>0)
	    {
	    	Element bodyElement = new Element(SMessageUtil.Body_Tag);
			
	    	for(ListOrderedMap map : sqlResult)
	    	{
	    		Element recipeElement = new Element("RECIPE");
				Element recipeNameElement = new Element("RECIPENAME");
				recipeNameElement.setText(CommonUtil.getValue(map, "TARGETNAME"));
				recipeElement.addContent(recipeNameElement);
				
				Element recipeListElement = XmlUtil.getChild(SMessageUtil.getBodyElement(doc), "RECIPELIST", true);
				recipeListElement.addContent(recipeElement);
	    	}  
	    	
	    	doc.getRootElement().addContent(bodyElement);
		}
	    else
	    {
	    	//RMS-029: RMSError:Machine[{0}] has no Recipes.
	    	throw new CustomException("RMS-029", machineName);
	    }
	    
		return doc;
	}
	
	public Document getRecipeParameterValidStart(String machineName, String portName, String carrierName, String recipeName, String recipeType)
			throws CustomException
	{
//		MACHINENAME
//		PORTNAME
//		CARRIERNAME
//		RECIPETYPE
//		RECIPENAME
//		EQPLIST
//		    EQP
//		      EQPNAME
//		      EQPRECIPENAME
		try {
			Document doc = new Document();
	    	Element bodyElement = new Element(SMessageUtil.Body_Tag);
	
	    	String detailMachineType = "";
	    	if (recipeType.equals(GenericServiceProxy.getConstantMap().RECIPETYPE_UNIT))
	    	{ detailMachineType = GenericServiceProxy.getConstantMap().DetailMachineType_Unit; } 
	    	else if (recipeType.equals(GenericServiceProxy.getConstantMap().RECIPETYPE_SUBUNIT))
	    	{ detailMachineType = GenericServiceProxy.getConstantMap().DetailMachineType_SubUnit; }

	    	Element attMachineName = new Element("MACHINENAME");
			attMachineName.setText(machineName);
			bodyElement.addContent(attMachineName);
			
	    	Element attPortName = new Element("PORTNAME");
	    	attPortName.setText(portName);
			bodyElement.addContent(attPortName);
			
	    	Element attCarrierName = new Element("CARRIERNAME");
	    	attCarrierName.setText(carrierName);
			bodyElement.addContent(attCarrierName);
			
	    	Element attRecipeType = new Element("RECIPETYPE");
	    	attRecipeType.setText(recipeType);
			bodyElement.addContent(attRecipeType);
			
	    	Element attRecipeName = new Element("RECIPENAME");
	    	attRecipeName.setText(recipeName);
			bodyElement.addContent(attRecipeName);
	    	
			Element eqpListElement = new Element("EQPLIST");
			
	    	if(recipeType.equals(GenericServiceProxy.getConstantMap().RECIPETYPE_UNIT))
	    	{
	    		List<ListOrderedMap> sqlResult = this.getMachineRecipeList(machineName, recipeName, GenericServiceProxy.getConstantMap().DetailMachineType_Unit);
	    		
	    		if(sqlResult != null && sqlResult.size() > 0)
	    		{
		    		for(ListOrderedMap map : sqlResult)
			    	{
			    		Element eqpElement = new Element("EQP");
			    		
						Element eqpNameElement = new Element("EQPNAME");
						eqpNameElement.setText(CommonUtil.getValue(map, "RECIPEPARAMETERNAME"));
						eqpElement.addContent(eqpNameElement);
						
						Element eqpRecipeNameElement = new Element("EQPRECIPENAME");
						eqpRecipeNameElement.setText(CommonUtil.getValue(map, "VALUE"));
						eqpElement.addContent(eqpRecipeNameElement);
	
						eqpListElement.addContent(eqpElement);
			    	}
	    		}
	    		else
	    		{
	    			//RMS-030: RMSError:Recipe[{0}] is not registered
	    			throw new CustomException("RMS-030", recipeName);
	    		}
	    	}
	    	else if(recipeType.equals(GenericServiceProxy.getConstantMap().RECIPETYPE_SUBUNIT))
	    	{
	    		// Get Unit Recipe List
	    		List<ListOrderedMap> sqlResult = this.getMachineRecipeList(machineName, recipeName, GenericServiceProxy.getConstantMap().DetailMachineType_Unit);
	    		
	    		if(sqlResult != null && sqlResult.size() > 0)
	    		{
	    			boolean resultOK = false;
	    			
		    		for(ListOrderedMap map : sqlResult)
			    	{
		    			String unitName = CommonUtil.getValue(map, "RECIPEPARAMETERNAME");
		    			String unitRecipeName = CommonUtil.getValue(map, "VALUE");
		    			
		    			List<ListOrderedMap> sqlResult2 = this.getMachineRecipeList(unitName, unitRecipeName, GenericServiceProxy.getConstantMap().DetailMachineType_SubUnit);

		    			if(sqlResult2 != null && sqlResult2.size() > 0)
			    		{
		    				resultOK = true;
							for(ListOrderedMap map2 : sqlResult2)
					    	{
					    		Element eqpElement = new Element("EQP");
					    		
								Element eqpNameElement = new Element("EQPNAME");
								eqpNameElement.setText(CommonUtil.getValue(map2, "RECIPEPARAMETERNAME"));
								eqpElement.addContent(eqpNameElement);
								
								Element eqpRecipeNameElement = new Element("EQPRECIPENAME");
								eqpRecipeNameElement.setText(CommonUtil.getValue(map2, "VALUE"));
								eqpElement.addContent(eqpRecipeNameElement);
		
								eqpListElement.addContent(eqpElement);
					    	}  
			    		}
			    	}
		    		if (resultOK == false)
		    		{
		    		   //RMS-030: RMSError:Recipe[{0}] is not registered
		    			throw new CustomException("RMS-030", recipeName);
		    		}
	    		}
	    		else
	    		{
	    			//RMS-030: RMSError:Recipe[{0}] is not registered
	    			throw new CustomException("RMS-030", recipeName);
	    		}
	    	}

	    	bodyElement.addContent(eqpListElement);

	    	doc = SMessageUtil.createXmlDocument(bodyElement, "RecipeParameterValidStart", "", "", "MES", "");

			return doc;
		}
		catch (Exception ex)
		{
			throw new CustomException("MACHINE-0014");
		}
	    
	}
	
	public Document validateRecipeParameter(Element parameterList, String machineName, String portName, String carrierName, String recipeName, 
			String recipeType, String eqpName, String eqpRecipeName) throws CustomException
	{
//		MACHINENAME 
//		PORTNAME
//		CARRIERNAME
//		RECIPENAME
//		RECIPETYPE
//		EQPNAME
//		EQPRECIPENAME
//		PARAMETERLIST
//			PARAMETER
//				PARAMETERNAME
//				VALUE

		try {
			Document doc = new Document();
	    	Element bodyElement = new Element(SMessageUtil.Body_Tag);
	
			//Get recipe by SuperMachine
			StringBuffer sqlBuffer = new StringBuffer("");

			sqlBuffer.append(" SELECT RECIPEPARAMETERNAME, VALIDATIONTYPE, VALUE, TARGET, LOWERLIMIT, UPPERLIMIT ")
					.append("   FROM CT_RECIPEPARAMETER ")
					.append(" WHERE 1=1 ")
					.append(" AND MACHINENAME = ? ")
					.append(" AND RECIPENAME = ? ");
			
		    Object[] bind = new String[]{eqpName, eqpRecipeName};
		    
		    List<ListOrderedMap> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bind); 
	        
		    if (sqlResult != null && sqlResult.size() > 0)
		    {
				if (parameterList != null && parameterList.getChildren().size() > 0)
				{
					if (sqlResult.size() != parameterList.getChildren().size())
						throw new CustomException("MACHINE-0008", parameterList.getChildren().size(), sqlResult.size());

					for (Iterator<?> iterator = parameterList.getChildren().iterator(); iterator.hasNext();)
					{
						Element recipe = (Element) iterator.next();
			
						String eqpParaName = recipe.getChildText("PARAMETERNAME");
						String eqpValue = recipe.getChildText("VALUE");
						boolean isOK = false;
				    	for(ListOrderedMap map : sqlResult)
				    	{
							String mesParaName = CommonUtil.getValue(map, "RECIPEPARAMETERNAME");
							String mesValidType = CommonUtil.getValue(map, "VALIDATIONTYPE");
							String mesValue = CommonUtil.getValue(map, "VALUE");
							String mesTarget = CommonUtil.getValue(map, "TARGET");
							String mesLowerLimit = CommonUtil.getValue(map, "LOWERLIMIT");
							String mesUpperLimit = CommonUtil.getValue(map, "UPPERLIMIT");
							
							if (eqpParaName.equals(mesParaName))
							{
								if(mesValidType.equals("Exact"))
								{
									if(!eqpValue.equals(mesValue))
										throw new CustomException("MACHINE-0009",eqpParaName,eqpValue,mesValue);
								}
								if(mesValidType.equals("Range"))
								{
									if (((Double.valueOf(mesUpperLimit) < Double.valueOf(eqpValue)) )
											|| (Double.valueOf(mesLowerLimit) > Double.valueOf(eqpValue)))
										throw new CustomException("MACHINE-0010",eqpParaName,eqpValue,mesLowerLimit,mesUpperLimit);
								}
							}
							logger.debug("eqpParaName: " + eqpParaName);
							logger.debug("eqpValue: " + eqpValue);
							logger.debug("mesTarget: " + mesTarget);
							logger.debug("mesLowerLimit: " + mesLowerLimit);
							logger.debug("mesUpperLimit: " + mesUpperLimit);
							
							isOK = true;
				    	}  
				    	if (isOK == false)
				    		throw new CustomException("MACHINE-0011",eqpParaName);
					}

				}
				else
	    			throw new CustomException("MACHINE-0012");
		    	

		    	Element attMachineName = new Element("MACHINENAME");
				attMachineName.setText(machineName);
				bodyElement.addContent(attMachineName);
				
		    	Element attPortName = new Element("PORTNAME");
		    	attPortName.setText(portName);
				bodyElement.addContent(attPortName);
				
		    	Element attCarrierName = new Element("CARRIERNAME");
		    	attCarrierName.setText(carrierName);
				bodyElement.addContent(attCarrierName);
				
		    	Element attRecipeType = new Element("RECIPETYPE");
		    	attRecipeType.setText(recipeType);
				bodyElement.addContent(attRecipeType);
				
		    	Element attRecipeName = new Element("RECIPENAME");
		    	attRecipeName.setText(recipeName);
				bodyElement.addContent(attRecipeName);
		    	
				Element attEqpName = new Element("EQPNAME");
				attEqpName.setText(eqpName);
				bodyElement.addContent(attEqpName);
				
				Element attEqpRecipeName = new Element("EQPRECIPENAME");
				attEqpRecipeName.setText(eqpRecipeName);
				bodyElement.addContent(attEqpRecipeName);
				
				Element attResult = new Element("RESULT");
				attResult.setText("OK");
				bodyElement.addContent(attResult);
				
				Element attResultDesc = new Element("RESULTDESCRIPTION");
				attResultDesc.setText(StringUtil.EMPTY);
				bodyElement.addContent(attResultDesc);
			}
		    else
		    {
	    		throw new CustomException("MACHINE-0013",eqpName,eqpRecipeName);
		    }
		    
	    	doc = SMessageUtil.createXmlDocument(bodyElement, "RecipeParameterSendReply", "", "", "MES", "");

			return doc;
		}
		catch (Exception ex)
		{
			throw new CustomException("MACHINE-0015");
		}
	    
	}
	
	public void updateTrackOutTime(String machineName, String recipeName, EventInfo eventInfo)
			throws CustomException
	{
		//existence verification in MES
		Recipe recipeData;
		try
		{
			recipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] {machineName, recipeName});
			recipeData.setLastTrackOutTimeKey(eventInfo.getEventTimeKey());
			ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipeData);
		}
		catch (FrameworkErrorSignal fe)
		{
			//throw new CustomException("SYS-9999", "RMS", fe.getMessage());
			logger.info("UpdatePPIDLastTrackOutTime Error:Not Found PPID in CT_RECIPE");
		}
		catch (greenFrameErrorSignal nfe)
		{
			//throw new CustomException("SYS-9999", "RMS", nfe.getMessage());
			logger.info("UpdatePPIDLastTrackOutTime Error:Not Found PPID in CT_RECIPE");
		}		
	}
	
	public void checkRecipeOnCancelTrackInTime(String machineName, String recipeName)
			throws CustomException
	{
		//existence verification in MES
		Recipe recipeData;
		try
		{
			recipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] {machineName, recipeName});
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "RMS", fe.getMessage());
		}
		catch (greenFrameErrorSignal nfe)
		{
			throw new CustomException("SYS-9999", "RMS", nfe.getMessage());
		}
		
		//Increase TotalTimeUsed 
		String comment = "Decrease Recipe check TotalTimeUsed - 1";
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeRecipeInfo", "MES", comment, null, null);
		recipeData.setTotalTimeUsed(recipeData.getTotalTimeUsed()-1);
		recipeData.setTimeUsed(recipeData.getTimeUsed()-1);

		ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipeData);
	}
	
	public void checkRecipeOnTrackInTime(String machineName, String recipeName)
			throws CustomException
	{
		//existence verification in MES
		Recipe recipeData;
		try
		{
			recipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] {machineName, recipeName});
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "RMS", fe.getMessage());
		}
		catch (greenFrameErrorSignal nfe)
		{
			throw new CustomException("SYS-9999", "RMS", nfe.getMessage());
		}
		
		//approval validation
		if (!recipeData.getMFGFlag().equals("Y") || !recipeData.getINTFlag().equals("Y") || !recipeData.getENGFlag().equals("Y") || !recipeData.getActiveState().equals("Active"))
		{
			//RMS-006: RMSError:Recipe[{0}] is not permitted
			throw new CustomException("RMS-006", recipeName);
		}
		
		
		//Increase TotalTimeUsed 
		String comment = "Increase Recipe check TotalTimeUsed + 1";
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeRecipeInfo", "MES", comment, null, null);
		recipeData.setTotalTimeUsed(recipeData.getTotalTimeUsed()+1);

		boolean isUpdate = false;
		boolean checkRMSFlag = false;
		
		//String comment = StringUtil.EMPTY;
		
		// Check time used
		if(recipeData.getTimeUsedLimit() == recipeData.getTimeUsed())
		{
			throw new CustomException("MACHINE-0022", recipeName);
		}
		else 
		{

			if(recipeData.getTimeUsedLimit() == recipeData.getTimeUsed()+1)
			{
				comment = "Recipe check timeUsed is same with timeusedlimit";
				isUpdate = true;
				checkRMSFlag = true;
				recipeData.setTimeUsed(0);
			}
			else
			{
				comment = "Increase Recipe check timeUsed + 1";
				isUpdate = true;
				recipeData.setTimeUsed(recipeData.getTimeUsed()+1);
			}
		}
		
		// Check duration used
		if(recipeData.getLastTrackOutTimeKey() == null || recipeData.getLastTrackOutTimeKey().equals(""))
		{
			String specValue = Double.toString(recipeData.getDurationUsedLimit());
			String interval = Double.toString(ConvertUtil.getDiffTime(TimeUtils.toTimeString(recipeData.getLastApporveTime(), TimeStampUtil.FORMAT_TIMEKEY), TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY)));
			
			if (Double.parseDouble(specValue)*60*60 < Double.parseDouble(interval))
			{
				throw new CustomException("MACHINE-0023", recipeName);
			}
		}
		else
		{
			Timestamp value = TimeUtils.getTimestampByTimeKey(recipeData.getLastTrackOutTimeKey());
			String specValue = Double.toString(recipeData.getDurationUsedLimit());
			String interval = Double.toString(ConvertUtil.getDiffTime(TimeUtils.toTimeString(value, TimeStampUtil.FORMAT_TIMEKEY), TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY)));

			if (Double.parseDouble(specValue)*60*60 < Double.parseDouble(interval))//modify DurationUsedLimit by Hours. Double.parseDouble(specValue)*24*60*60  to Double.parseDouble(specValue)*60*60
			{
				throw new CustomException("MACHINE-0023", recipeName);
			}
		}
		
		if(isUpdate)
		{
			eventInfo = EventInfoUtil.makeEventInfo("ChangeRecipeInfo", "MES", comment, null, null);
			
			if (checkRMSFlag) {
				if (recipeData.getAutoChangeFlag().equals("ENGINT")||recipeData.getAutoChangeFlag().equals("INTENG"))
					recipeData.setINTFlag("Y");
					recipeData.setENGFlag("N");
				    recipeData.setRMSFlag("N");
			}				
			ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipeData);
		}
		
		//if(recipeData.getAutoChangeFlag().equals("Y"))
		/*
		if(recipeData.getAutoChangeFlag().equals("INT") || recipeData.getAutoChangeFlag().equals("ENG") )
		{
			boolean isUpdate = false;
			boolean checkRMSFlag = false;
			
			//String comment = StringUtil.EMPTY;
			
			// Check time used
			if(recipeData.getTimeUsedLimit() == recipeData.getTimeUsed())
			{
				throw new CustomException("MACHINE-0022", recipeName);
			}
			else 
			{

				if(recipeData.getTimeUsedLimit() == recipeData.getTimeUsed()+1)
				{
					comment = "Recipe check timeUsed is same with timeusedlimit";
					isUpdate = true;
					checkRMSFlag = true;
					recipeData.setTimeUsed(0);
				}
				else
				{
					comment = "Increase Recipe check timeUsed + 1";
					isUpdate = true;
					recipeData.setTimeUsed(recipeData.getTimeUsed()+1);
				}
			}
			
			// Check duration used
			if(recipeData.getLastTrackOutTimeKey() == null || recipeData.getLastTrackOutTimeKey().equals(""))
			{
				String specValue = Double.toString(recipeData.getDurationUsedLimit());
				String interval = Double.toString(ConvertUtil.getDiffTime(TimeUtils.toTimeString(recipeData.getLastApporveTime(), TimeStampUtil.FORMAT_TIMEKEY), TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY)));
				
				if (Double.parseDouble(specValue)*60*60 < Double.parseDouble(interval))
				{
					throw new CustomException("MACHINE-0023", recipeName);
				}
			}
			else
			{
				Timestamp value = TimeUtils.getTimestampByTimeKey(recipeData.getLastTrackOutTimeKey());
				String specValue = Double.toString(recipeData.getDurationUsedLimit());
				String interval = Double.toString(ConvertUtil.getDiffTime(TimeUtils.toTimeString(value, TimeStampUtil.FORMAT_TIMEKEY), TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY)));

				if (Double.parseDouble(specValue)*60*60 < Double.parseDouble(interval))//modify DurationUsedLimit by Hours. Double.parseDouble(specValue)*24*60*60  to Double.parseDouble(specValue)*60*60
				{
					throw new CustomException("MACHINE-0023", recipeName);
				}
			}
			
			if(isUpdate)
			{
				eventInfo = EventInfoUtil.makeEventInfo("ChangeRecipeInfo", "MES", comment, null, null);
				
				if (checkRMSFlag) {
					if (recipeData.getAutoChangeFlag().equals("INT"))
						recipeData.setINTFlag("N");
					else if (recipeData.getAutoChangeFlag().equals("ENG"))
						recipeData.setENGFlag("N");

					recipeData.setRMSFlag("N");
				}				
				ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipeData);
			}
		}*/
	}
	
	public String getMachineRecipeForOLEDMask(String factoryName, String productSpecName, String processFlowName, String processFlowVersion, 
			String processOperationName, String processOperationVersion, String machineName) throws CustomException
	{
		logger.info(String.format("entry to [%s] at [%d]", "getMachineRecipe", System.currentTimeMillis()));
		logger.info("Level [D] Recipe validation behavior begins");

		ListOrderedMap instruction = PolicyUtil.getMachineRecipeNameForOLEDMask(factoryName, productSpecName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, machineName);

		String designatedRecipeName = CommonUtil.getValue(instruction, "MACHINERECIPENAME");
		
		{
			if (StringUtil.isEmpty(designatedRecipeName))
				throw new CustomException("MACHINE-0102", designatedRecipeName);
		}
		
		logger.info(String.format("quit from [%s] at [%d]", "getMachineRecipe", System.currentTimeMillis()));
		
		return designatedRecipeName;
	}
	
	public List<ListOrderedMap> getMachineRecipeList(String machineName, String recipeName, String recipeType) throws CustomException
	{
		StringBuffer sqlBuffer = new StringBuffer("");

		sqlBuffer.append(" SELECT A.RECIPEPARAMETERNAME, A.VALUE  ")
				.append("   FROM CT_RECIPEPARAMETER A, CT_RECIPEPARAMETER B, CT_RECIPE C ")
				.append(" WHERE 1=1 ")
				.append(" AND A.MACHINENAME = ? ")
				.append(" AND A.RECIPENAME = ? ")
				.append(" AND A.RECIPEPARAMETERNAME = B.MACHINENAME ")
				.append(" AND B.MACHINENAME = C.MACHINENAME ")
				.append(" AND C.RECIPETYPE = ? ")
				.append(" GROUP BY A.RECIPEPARAMETERNAME, A.VALUE ");
		
	    Object[] bind = new String[]{machineName, recipeName, recipeType};
	    
	    List<ListOrderedMap> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bind); 
        
//	    if(sqlResult == null || sqlResult.size() == 0)
//	    {
//	    	//RMS-030: RMSError:Recipe[{0}] is not registered
//	    	throw new CustomException("RMS-030", recipeName);
//	    }

	    return sqlResult;
	}
	
	public boolean checkMachineRecipeWithOutUnit(MachineSpec machineSpecData, String portName, String carrierName, Recipe recipeData) throws CustomException
	{		
		String recipeName = recipeData.getRecipeName();
		String machineName = machineSpecData.getKey().getMachineName();
		// Find RecipeType : 'E' Level Machine + PPID from EQ
		Document replyDoc = MESRecipeServiceProxy.getRecipeServiceImpl().inquiryParameterList(machineName, portName, carrierName, recipeName);
		List<SequenceParameter> seqRecipeList = new ArrayList<SequenceParameter>();
		Map<String, Document> dicRecipe = new HashMap<String, Document>();
        List<RecipeParameter> paramList;
        
        int totalCount = 0;
		int mesCount = 0;
  		
        try
		{ 
        	// Find RecipeType : 'E' Level Machine + PPID from CT_RECIPEPARAMETER
			paramList = ExtendedObjectProxy.getRecipeParamService().select("machineName = ? AND recipeName = ? ", new Object[] {machineName, recipeName});
		}
		catch (Exception e)
		{
			logger.warn(String.format("Nothing to defined in parameters about Recipe[%s] of Unit[%s]",machineName, recipeName));
			paramList = new ArrayList<RecipeParameter>();
		}
        
        // Check start by EQ Parameter
		if (paramList.size() > 0)
		{		
			mesCount = paramList.size();
        	totalCount = getEqpParameterCount(replyDoc);
        	
        	if(totalCount != mesCount)
			{
        		//RMS-031: RMSError:The [{0}] Recipe [{1}] has different parameter count. MESCount : {2} , EQPCount : {3}
        		throw new CustomException("RMS-031",  machineName ,recipeName , mesCount ,totalCount);
			}
             //SubUnit
            for (Element parameterGroup : SMessageUtil.getBodySequenceItemList(replyDoc, "COMMANDLIST", false))
            {   
            	String parameterName = SMessageUtil.getChildText(parameterGroup, "SUNITNAME", false);

				for (Element parameter : SMessageUtil.getSubSequenceItemList(parameterGroup, "PARAMETERLIST", false))
				{ 
					//totalCount += 1;
					
					//2019-01-22 Add
					boolean isFound = false;
					 
					String searchParamName = "";
					if (SMessageUtil.getChildText(parameter, "PARAMETERNAME", false).equals("SUBUNITRECIPE"))
					{
						searchParamName = parameterName;
					}
					else
					{
						searchParamName = SMessageUtil.getChildText(parameter, "PARAMETERNAME", false);
					}
					
					for(RecipeParameter mainParam : paramList)
					{						
						if (mainParam.getRecipeParameterName().equals(searchParamName))
						{
							if(mainParam.getValidationType().isEmpty() && !SMessageUtil.getChildText(parameter, "PARAMETERNAME", false).toString().equals("SUBUNITRECIPE"))
							{
								isFound = true;
								break;
							}
							
							// Param Value Check
							else if (mainParam.getValidationType().equals("Target"))
							{
								if (!mainParam.getValue().equals(SMessageUtil.getChildText(parameter, "VALUE", false)))
								{
									//RMS-032: RMSError:The Recipe[{0}] has different the item[{1}] 
									throw new CustomException("RMS-032", mainParam.getRecipeName(), mainParam.getRecipeParameterName());
								}
								isFound = true;								
							}
							else if (mainParam.getValidationType().equals("Range"))
							{
								if (Double.parseDouble(mainParam.getUpperLimit()) < Double.parseDouble(SMessageUtil.getChildText(parameter, "VALUE", false).toString())
										|| Double.parseDouble(mainParam.getLowerLimit()) > Double.parseDouble(SMessageUtil.getChildText(parameter, "VALUE", false).toString()))
								{
									//RMS-032: RMSError:The Recipe[{0}] has different the item[{1}] 
									throw new CustomException("RMS-032", mainParam.getRecipeName(), mainParam.getRecipeParameterName());
								}
								isFound = true;
							}
							else
							{
								if(!mainParam.getValue().equals(SMessageUtil.getChildText(parameter, "VALUE", false)))
								{
									//RMS-032: RMSError:The Recipe[{0}] has different the item[{1}] 
									throw new CustomException("RMS-032", mainParam.getRecipeName(), mainParam.getRecipeParameterName());
								}
								isFound = true;
							}
						}
						if (isFound)
							break;
					}
					
					if(!isFound)
			        {
						//RMS-033: RMSError:RMSError:The [{0}] Recipe[{1}] has Add the item[{2}] 
			        	throw new CustomException("RMS-033", machineName, recipeName, parameterName ); 
			        }
					 
				}
            }
            
		}  

		// When sync EQ Level subunitrecipename is inserted to TARGET column on OIC 
		//replyDoc > parameterGroup > EQP MainRecipeParamList
		for (Element parameterGroup : SMessageUtil.getBodySequenceItemList(replyDoc, "COMMANDLIST", false))
		{
		    String subUnitName = SMessageUtil.getChildText(parameterGroup, "SUNITNAME", false);
			for (Element parameter : SMessageUtil.getSubSequenceItemList(parameterGroup, "PARAMETERLIST", false))
			{ 
				if(!SMessageUtil.getChildText(parameter, "PARAMETERNAME", false).equals("SUBUNITRECIPE"))
				{
					continue;
				}
				
				String subUnitRecipeName = SMessageUtil.getChildText(parameter, "VALUE", true);
				StringBuffer sqlBuffer = new StringBuffer("")
			              .append("  SELECT TARGET ")
			              .append("  FROM CT_RECIPEPARAMETER R ")
			              .append("  WHERE R.MACHINENAME = ? ")
			              .append("  AND R.RECIPENAME = ? ")
					      .append("  AND R.RECIPEPARAMETERNAME = ?")
					      .append("  AND R.VALIDATIONTYPE = 'Target' ");
				String sqlStmt = sqlBuffer.toString();
				Object[] bindSet = new String[]{machineName, recipeName, subUnitName};
				List<ListOrderedMap> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStmt, bindSet); 
				if (sqlResult.size() > 0)
				{
					ListOrderedMap temp = sqlResult.get(0);  
	           	   	String Target = CommonUtil.getValue(temp, "TARGET");
	           	   	
	           	    if(!Target.equals(subUnitRecipeName))
	           	    {
	           	    	//RMS-026: RMSError:[{0}]Valid standard is [{1}] but current parameter is [{2}]
	           	    	throw new CustomException("RMS-026", subUnitName, Target, subUnitRecipeName);
	           	    }
				}
				// else case maybe nothing
          	}
		}
		
		// Make unit recipe list by RecipeParam 
		for (Element parameterGroup : SMessageUtil.getBodySequenceItemList(replyDoc, "COMMANDLIST", false))
		{
			String subUnitName = SMessageUtil.getChildText(parameterGroup, "SUNITNAME", false);
			
			try
			{
				//PPID list per unit
				Document resA = new Document();
				
				if(subUnitName.endsWith("IK1") || subUnitName.endsWith("CCD") || subUnitName.endsWith("IND") || subUnitName.endsWith("UDP") || subUnitName.endsWith("LDP") || subUnitName.endsWith("TRS") || subUnitName.isEmpty())
				{
					continue;
				}

				//Get SubUnit Recipe List
				resA = MESRecipeServiceProxy.getRecipeServiceImpl().inquiryRecipeList(subUnitName, portName, carrierName);
				
				//resA  > SubUnit锟斤拷 RecipeList
				dicRecipe.put(subUnitName, resA);
				
				//Get SubUnitRecipe from MainRecipeParamer
				for (Element parameter : SMessageUtil.getSubSequenceItemList(parameterGroup, "PARAMETERLIST", false))
				{
					if(!SMessageUtil.getChildText(parameter, "PARAMETERNAME", false).equals("SUBUNITRECIPE"))
					{
						continue;
					}
					
					//2019-01-22 Add
					for(RecipeParameter mainParam : paramList)
					{
						if(mainParam.getMachineName().equals(machineName) && mainParam.getRecipeName().equals(recipeName) && mainParam.getRecipeParameterName().equals(subUnitName))
						{
							if((mainParam.getCheckFlag()==null) || mainParam.getCheckFlag().equals("Y"))
							{
								String subUnitRecipeName = SMessageUtil.getChildText(parameter, "VALUE", true);
								SequenceParameter entity = new SequenceParameter(subUnitName, subUnitRecipeName);
								seqRecipeList.add(entity);
								break;
							}
							else if(mainParam.getCheckFlag().equals("N"))
							{
								continue;
							}
							else
							{
								String subUnitRecipeName = SMessageUtil.getChildText(parameter, "VALUE", true);
								SequenceParameter entity = new SequenceParameter(subUnitName, subUnitRecipeName);
								seqRecipeList.add(entity);
								break;
							}
						}
					}
					
					
					//2019-01-22 Remove
					/*
					StringBuffer sqlBuffer = new StringBuffer("")
		                    .append("  SELECT *")
		                    .append("  FROM CT_RECIPEPARAMETER R ")
		                    .append("  WHERE R.MACHINENAME = ?")
		                    .append("  AND R.RECIPENAME = ? ")
					        .append("  AND R.RECIPEPARAMETERNAME = ?");
					String sqlStmt = sqlBuffer.toString();
					Object[] bindSet = new String[]{machineName, recipeName, subUnitName};
					List<ListOrderedMap> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStmt, bindSet);
					if((sqlResult.get(0).get("CHECKFLAG")==null) || sqlResult.get(0).get("CHECKFLAG").toString().equals("Y"))
					{
						String subUnitRecipeName = SMessageUtil.getChildText(parameter, "VALUE", true);
						SequenceParameter entity = new SequenceParameter(subUnitName, subUnitRecipeName);
						seqRecipeList.add(entity);
					}
					else if(sqlResult.get(0).get("CHECKFLAG").toString().equals("N"))
					{
						continue;
					}
					else
					{
						String subUnitRecipeName = SMessageUtil.getChildText(parameter, "VALUE", true);
						SequenceParameter entity = new SequenceParameter(subUnitName, subUnitRecipeName);
						seqRecipeList.add(entity);
					}
					*/
				}
			}
			catch (Exception ex)
			{
				logger.error(String.format("Unit Recipe info from Unit[%s] parsing failed", subUnitName));
			}
		}
		
		//sequence set integrity check
		if (seqRecipeList.size() < 1)
		{
			//RMS-020: RMSError:Host Recipe[{0}] does not have any sequence
			throw new CustomException("RMS-020", recipeName);
		}
		
		
		//Compare SubUnitRecipeData
		for (SequenceParameter seqRecipeData : seqRecipeList)
		{
			//existence check-up in MES
			Recipe subUnitRecipeData;
			try
			{			
				if(seqRecipeData.getUnitName().endsWith("IK1") || seqRecipeData.getUnitName().endsWith("CCD") || seqRecipeData.getUnitName().endsWith("UDP") || seqRecipeData.getUnitName().endsWith("LDP") ||seqRecipeData.getUnitName().endsWith("TRS") || seqRecipeData.getUnitName().isEmpty())
				{
					continue;
				}
				
				subUnitRecipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] {seqRecipeData.getUnitName(), seqRecipeData.getUnitRecipeName()});
			}
			catch (Exception e)
			{
				throw new CustomException("SYS-9999", "RMS", e.getMessage());
			}

			//Validation SubUnitRecipe state	
			if (!(subUnitRecipeData.getINTFlag().equals("Y") && subUnitRecipeData.getMFGFlag().equals("Y") && subUnitRecipeData.getENGFlag().equals("Y") && subUnitRecipeData.getRMSFlag().equals("Y")))
			{
				//RMS-006: RMSError:Recipe[{0}] is not permitted
				throw new CustomException("RMS-006", subUnitRecipeData.getRecipeName());
			}
			
			if(recipeData.getAutoChangeFlag().equals("INTENG") || recipeData.getAutoChangeFlag().equals("ENGINT"))
			{
				if (recipeData.getDurationUsedLimit() == 0)
				{
                    //RMS-034: RMSError:Recipe[{0}] DurationUsedLimit 0					
					throw new CustomException("RMS-034", recipeName);
				}
				
				if (recipeData.getTimeUsedLimit() == 0)
				{
                    //RMS-035: RMSError:Recipe[{0}] TimeUsedLimit 0					
					throw new CustomException("RMS-035", recipeName);
				}
			}
			//existence verification in EQP
			//Document resA = MESRecipeServiceProxy.getRecipeServiceImpl().inquiryRecipeList(seqRecipeData.getUnitName(), portName, carrierName);
			Document docSubUnitRecipeList = dicRecipe.get(subUnitRecipeData.getMachineName());
			boolean isFound = false;
			for (Element eleRecipe : SMessageUtil.getBodySequenceItemList(docSubUnitRecipeList, "RECIPELIST", true))
			{			
				String subUnitRecipeName = SMessageUtil.getChildText(eleRecipe, "RECIPENAME", true);
				
				if (subUnitRecipeData.getRecipeName().equals(subUnitRecipeName))
				{
					isFound = true;
					break;
				}
			}
			
			if (!isFound)
			{
				//RMS-022: RMSError:Recipe[{0}] does not exist in Unit[{1}]
				throw new CustomException("RMS-022", subUnitRecipeData.getRecipeName(), subUnitRecipeData.getMachineName());
			}
		}
		
		//SubUnitParamCheckStart
		for (SequenceParameter seqData : seqRecipeList)
		{
			int totalSubUnitCount = 0;
			int mesSubUnitCount = 0;
			
			if(seqData.getUnitName().endsWith("IK1") || seqData.getUnitName().endsWith("UDP") || seqData.getUnitName().endsWith("LDP") || seqData.getUnitName().endsWith("TRS") || seqData.getUnitName().endsWith("IND") || seqData.getUnitName().isEmpty())
			{
				continue;
			}
			
			List<RecipeParameter> subUnitParamList;
			
			try
			{ 
				subUnitParamList = ExtendedObjectProxy.getRecipeParamService().select("machineName = ? AND recipeName = ? AND validationType IS NOT NULL",
								new Object[] {seqData.getUnitName(), seqData.getUnitRecipeName()});

			}
			catch (Exception fe)
			{
				logger.warn(String.format("Nothing to defined in parameters about Recipe[%s] of Unit[%s]", seqData.getUnitRecipeName(), seqData.getUnitName()));
				subUnitParamList = new ArrayList<RecipeParameter>();
			}		
			//paramList UnitParam
			// Machine recipe parameter data
			Document res = MESRecipeServiceProxy.getRecipeServiceImpl().inquiryParameterList(seqData.getUnitName(), portName, carrierName, seqData.getUnitRecipeName());
			
			//res > UnitRecipeParam 
			
			if (subUnitParamList.size() > 0)
			{	
				mesSubUnitCount = subUnitParamList.size();
				totalSubUnitCount = getEqpParameterCount(res);
	        	
	        	if(totalSubUnitCount != mesSubUnitCount)
				{
					// RMS-031: RMSError:The [{0}] Recipe [{1}] has different parameter count. MESCount : {2} , EQPCount : {3}
					throw new CustomException("RMS-031", seqData.getUnitName(), seqData.getUnitRecipeName(), mesSubUnitCount, totalSubUnitCount);
				}
				String Machinename = "";
			    String subRecipeName = SMessageUtil.getBodyItemValue(res, "RECIPENAME", true);
				String recipeType = SMessageUtil.getBodyItemValue(res, "RECIPETYPE", true);
				
				if (recipeType.equalsIgnoreCase("U") || recipeType.equalsIgnoreCase("S"))
				{
					//unit or sub-unit recipe must include single COMMAND item
					for (Element commandItem : SMessageUtil.getBodySequenceItemList(res, "COMMANDLIST", false))
					{
						if (recipeType.equalsIgnoreCase("U"))
						{
							Machinename = SMessageUtil.getChildText(commandItem, "UNITNAME", false);	
						}	
						else if (recipeType.equalsIgnoreCase("S"))
						{
							Machinename = SMessageUtil.getChildText(commandItem, "SUNITNAME", false);
						}	
						break;
					}
				}

				//res > EQP SubUnitRecipeParam 
				for (Element parameterGroup : SMessageUtil.getBodySequenceItemList(res, "COMMANDLIST", false))
				{
					for (Element parameter : SMessageUtil.getSubSequenceItemList(parameterGroup, "PARAMETERLIST", false))
					{
						String 	parameterName = SMessageUtil.getChildText(parameter, "PARAMETERNAME", false);;
						
						boolean isFound = false;
						
						//totalSubUnitCount += 1;
						
						for(RecipeParameter subParamData : subUnitParamList)
						{
							if( subParamData.getRecipeParameterName().equals(parameterName))
							{						
								//Param Value Check
								if(subParamData.getValidationType().equals("Target"))
								{
									if(!subParamData.getValue().equals(subParamData.getTarget()))
									{
										//RMS-036: RMSError:MES Parameter Value is different from MES Parameter Target. Recipe [{0}] Parameter [{1}] 
										throw new CustomException("RMS-036", subParamData.getRecipeName(), subParamData.getRecipeParameterName());
									}
									
									if(!subParamData.getValue().equals(SMessageUtil.getChildText(parameter, "VALUE", false)))
									{
										//RMS-032: RMSError:The Recipe[{0}] has different the item[{1}] 
										throw new CustomException("RMS-032", subParamData.getRecipeName(), subParamData.getRecipeParameterName());
									}
								}
								else if(subParamData.getValidationType().equals("Range"))
								{
									if(Double.parseDouble(subParamData.getUpperLimit()) < Double.parseDouble(SMessageUtil.getChildText(parameter, "VALUE", false).toString())
											|| Double.parseDouble(subParamData.getLowerLimit()) > Double.parseDouble(SMessageUtil.getChildText(parameter, "VALUE", false).toString()))
									{
										////RMS-032: RMSError:The Recipe[{0}] has different the item[{1}] 
										throw new CustomException("RMS-032", subParamData.getRecipeName(), subParamData.getRecipeParameterName());
									}
								}
								
								isFound = true;
								break;
							}
						}
						
						if(!isFound)
						{
							//RMS-037: RMSError:The [{0}] Recipe[{1}] has Deleted the item[{2}] 
							throw new CustomException("RMS-037", Machinename, recipeName, parameterName );
						}
						
						/*
						String  value = SMessageUtil.getChildText(parameter, "VALUE", false); 
						StringBuffer sqlBuffer = new StringBuffer("")
			                    .append("  SELECT *")
			                    .append("  FROM CT_RECIPEPARAMETER R ")
			                    .append("  WHERE R.MACHINENAME = ?")
			                    .append("  AND R.RECIPENAME = ? ")
						        .append("  AND R.RECIPEPARAMETERNAME = ?");
						String sqlStmt = sqlBuffer.toString();
						Object[] bindSet = new String[]{Machinename, subRecipeName, parameterName};
	                    List<ListOrderedMap> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStmt, bindSet); 
	                    if(sqlResult.size()<1)
			            {
			                //RMS-033: RMSError:The [{0}] Recipe[{1}] has Add the item[{2}] 
	                    	throw new CustomException("RMS-033", Machinename, subRecipeName, parameterName);
			            }
	                    else 
	                    {
	                    }
	                    */
					}
				}
				
				//2019-01-22 Remove
				/*
				for (RecipeParameter paramData : subUnitParamList)
				{
					boolean isFound = false;
					for (Element parameterGroup : SMessageUtil.getBodySequenceItemList(res, "COMMANDLIST", false))
					{
						sMachinename = SMessageUtil.getChildText(parameterGroup, "SUNITNAME", false);
						String subParamName = "";
						for (Element parameter : SMessageUtil.getSubSequenceItemList(parameterGroup, "PARAMETERLIST", false))
						{
							subParamName = SMessageUtil.getChildText(parameter, "PARAMETERNAME", false);

							if( paramData.getRecipeParameterName().equals(subParamName))
							{
								logger.info(paramData.getRecipeParameterName());
								logger.info(subParamName);
								
								//Param Value Check
								if(paramData.getValidationType().equals("Target"))
								{
									if(!paramData.getValue().equals(SMessageUtil.getChildText(parameter, "VALUE", false)))
									{
									   //RMS-032: RMSError:The Recipe[{0}] has different the item[{1}] 
										throw new CustomException("RMS-032",recipeName,paramData.getRecipeParameterName());
									}
								}
								else if(paramData.getValidationType().equals("Range"))
								{
									if(Double.parseDouble(paramData.getUpperLimit()) < Double.parseDouble(SMessageUtil.getChildText(parameter, "VALUE", false).toString())
											|| Double.parseDouble(paramData.getLowerLimit()) > Double.parseDouble(SMessageUtil.getChildText(parameter, "VALUE", false).toString()))
									{
									   //RMS-032: RMSError:The Recipe[{0}] has different the item[{1}] 
										throw new CustomException("RMS-032",recipeName,paramData.getRecipeParameterName());
									}
								}
								
								isFound = true;
							}
							if (isFound) break;	 
						}

						if (isFound) break;	
					}	
					if (isFound) continue;	
					else
					{
					    //RMS-037: RMSError:The [{0}] Recipe[{1}] has Deleted the item[{2}] 
						throw new CustomException("RMS-037", paramData.getMachineName(), recipeName,paramData.getRecipeParameterName());
					}
				}
				*/
			}
			
//			if(totalSubUnitCount != mesSubUnitCount)
//			{
			    //RMS-031: RMSError:The [{0}] Recipe [{1}] has different parameter count. MESCount : {2} , EQPCount : {3}
//        		throw new CustomException("RMS-031",seqData.getUnitName() , seqData.getUnitRecipeName() ,mesSubUnitCount ,totalSubUnitCount);
//			}
			
		}
		
		return true;
	}

	public List<SequenceParameter> checkMachineRecipe(MachineSpec machineSpecData, String portName, String carrierName, Recipe recipeData, String checkLevel) throws CustomException
	{
		String recipeName = recipeData.getRecipeName();
		String machineName = machineSpecData.getKey().getMachineName();
		
		// Find RecipeType : 'E' Level Machine + PPID from EQ
		Document replyDoc = MESRecipeServiceProxy.getRecipeServiceImpl().inquiryParameterList(machineName, portName, carrierName, recipeName);
		
		List<SequenceParameter> seqRecipeList = new ArrayList<SequenceParameter>();
		Map<String, Document> dicRecipe = new HashMap<String, Document>();
        List<RecipeParameter> paramList;
        
		int totalCount = 0;
		int mesCount = 0;
  		
        try
		{ 
        	// Find RecipeType : 'E' Level Machine + PPID from CT_RECIPEPARAMETER 	//AND checkFlag <> 'N' 
			paramList = ExtendedObjectProxy.getRecipeParamService().select("machineName = ? AND recipeName = ?  ", new Object[] {machineName, recipeName});
		}
		catch (Exception e)
		{
			logger.warn(String.format("Nothing to defined in parameters about Recipe[%s] of Unit[%s]",machineName, recipeName));
			paramList = new ArrayList<RecipeParameter>();
		}
        
        mesCount = paramList.size();
        totalCount = getEqpParameterCount(replyDoc);
    	if(totalCount != mesCount)
		{
    		//RMS-031: RMSError:The [{0}] Recipe [{1}] has different parameter count. MESCount : {2} , EQPCount : {3}
    		throw new CustomException("RMS-031", machineName, recipeName , mesCount , totalCount);
		}
        //E Level Recipe
        if(checkLevel.equals("E"))
        {
        	for (RecipeParameter paramData : paramList)
    		{
    			/*logger.info(paramData.getRecipeParameterName());*/
    			boolean isFound = false;
    			for (Element parameterGroup : SMessageUtil.getBodySequenceItemList(replyDoc, "COMMANDLIST", false))
    			{
    				for (Element parameter : SMessageUtil.getSubSequenceItemList(parameterGroup, "PARAMETERLIST", false))
    				{
    					//totalCount += 1;
    					
    					String parameterName  = SMessageUtil.getChildText(parameter, "PARAMETERNAME", false);
    					if(parameterName.equals("UNITRECIPE"))
    					{
    						parameterName = SMessageUtil.getChildText(parameterGroup, "UNITNAME", false);
    					}
    					if(parameterName.equals(paramData.getRecipeParameterName()))
    					{
    						//Param Value Check
    						//Target 锟斤拷锟教革拷 锟窖癸拷锟�锟斤拷锟轿帮拷 锟斤拷锟�锟斤拷
							if(paramData.getValidationType().equals("Target"))
							{
							   if(!paramData.getValue().equals(paramData.getTarget()))
								{
								   //RMS-036:MES Parameter Value is different from MES Parameter Target. Recipe [{0}] Parameter [{1}] 
								   throw new CustomException("RMS-036", paramData.getRecipeName(), paramData.getRecipeParameterName());
								}
								
								if(!paramData.getValue().equals(SMessageUtil.getChildText(parameter, "VALUE", false)))
								{
									//RMS-032: RMSError:The Recipe[{0}] has different the item[{1}] 
									throw new CustomException("RMS-032", paramData.getRecipeName() ,paramData.getRecipeParameterName());
								}
								isFound = true;
							}
							else if(paramData.getValidationType().equals("Range"))
							{
								if(Double.parseDouble(paramData.getUpperLimit()) < Double.parseDouble(SMessageUtil.getChildText(parameter, "VALUE", false).toString())
										|| Double.parseDouble(paramData.getLowerLimit()) > Double.parseDouble(SMessageUtil.getChildText(parameter, "VALUE", false).toString()))
								{
									//RMS-032: RMSError:The Recipe[{0}] has different the item[{1}] 
									throw new CustomException("RMS-032", paramData.getRecipeName(), paramData.getRecipeParameterName());
								}
								isFound = true;
							}
							else
							{
								if(!paramData.getValue().equals(SMessageUtil.getChildText(parameter, "VALUE", false)))
								{
									//RMS-032: RMSError:The Recipe[{0}] has different the item[{1}] 
									throw new CustomException("RMS-032",paramData.getRecipeName(), paramData.getRecipeParameterName());
								}
								isFound = true;
								logger.info(paramData.getRecipeParameterName() + " Found");
								break;
							}
    					}
    					if (isFound) break;	 
    				}
    				if (isFound) break;	
    			}	
    			if (isFound) continue;	
    			else
    			{
    				//RMS-037: RMSError:The [{0}] Recipe[{1}] has Deleted the item[{2}] 
    				throw new CustomException("RMS-037", paramData.getMachineName(), recipeName,paramData.getRecipeParameterName());
    			}
    		}
        	
//        	if(totalCount != mesCount)
//			{
        	    //RMS-031: RMSError:The [{0}] Recipe [{1}] has different parameter count. MESCount : {2} , EQPCount : {3}
//        		throw new CustomException("RMS-031", machineName,recipeName , mesCount ,totalCount);
//			}
        	
        	return null;
        }
        
        // Check start by EQ Parameter
        //paramList : MES E Level Parameter
        //replyDoc : EQP E Level Parameter
		if (paramList.size() > 0) 
		{
            logger.info("================ List Check Start by UNIT ======================");		

          	//2019-01-22 Add Start
            for (Element parameterGroup : SMessageUtil.getBodySequenceItemList(replyDoc, "COMMANDLIST", false))
			{
				String parameterName = SMessageUtil.getChildText(parameterGroup, "UNITNAME", false);
				for (Element parameter : SMessageUtil.getSubSequenceItemList(parameterGroup, "PARAMETERLIST", false))
				{
					//totalCount += 1;
					boolean isFound = false;
					for (RecipeParameter paramData : paramList)
					{
						if (paramData.getRecipeParameterName().equals(parameterName))
						{
							if(paramData.getValue().equals(SMessageUtil.getChildText(parameter, "VALUE", false)))
							{
								isFound = true;
								break;
							}
						}
						if (isFound)
						{
							break;
						}
					}
					if (isFound)
					{
						break;
					}
					else
					{
						//RMS-039: There is no item in MES DB. MachineName[{0}] Recipe[{1}] item[{2}]
						throw new CustomException("RMS-039", machineName, recipeName, parameterName);
					}
				}
			}
            //2019-01-22 Add End

            //2019-01-22 Remove
             /*
             for (Element parameterGroup : SMessageUtil.getBodySequenceItemList(replyDoc, "COMMANDLIST", false))
             {   
				 String parameterName  = SMessageUtil.getChildText(parameterGroup, "UNITNAME", false) ;
				 
				 for (Element parameter : SMessageUtil.getSubSequenceItemList(parameterGroup, "PARAMETERLIST", false))//EQP E Level Param
				 { 
					 
					 
					 
					 // Find MACHINE - PPID - UNITNAME from CT_RECIPEPARAMETER
				     String value = SMessageUtil.getChildText(parameter, "VALUE", false); // UNIT RECIPENAMENAME
			         StringBuffer sqlBuffer = new StringBuffer("")
			        		 	.append("  SELECT * ")
			        		 	.append("  FROM CT_RECIPEPARAMETER R ")
			        		 	.append("  WHERE R.MACHINENAME = ? ")
			        		 	.append("  AND R.RECIPENAME = ? ")
			        		 	.append("  AND R.RECIPEPARAMETERNAME = ? ");
			         String sqlStmt = sqlBuffer.toString();
			         Object[] bindSet = new String[]{machineName, recipeName, parameterName};
			         List<ListOrderedMap> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStmt, bindSet); 
			         if(sqlResult.size() < 1)
			         {
			             //RMS-033: RMSError:The [{0}] Recipe[{1}] has Add the item[{2}] 
			        	 throw new CustomException("RMS-033", machineName, recipeName, parameterName); 
			         }
				 }
             }
             logger.info("================ Check MACHINE - PPID - UNITNAME from CT_RECIPEPARAMETER End ======================");

             logger.info("================ Check deleted parameter in EQ use CT_RECIPEPARAMETER Start ======================");
             
             //MES MainRecipeParamList
             for (RecipeParameter paramData : paramList)
             {		
            	 boolean isFound = false;
            	 //replyDoc > parameterGroup > EQP MainRecipeParamList
            	 for (Element parameterGroup : SMessageUtil.getBodySequenceItemList(replyDoc, "COMMANDLIST", false))
            	 {
   					String parameterName  = SMessageUtil.getChildText(parameterGroup, "UNITNAME", false) ;
   					for (Element parameter : SMessageUtil.getSubSequenceItemList(parameterGroup, "PARAMETERLIST", false))
   					{
   						if( paramData.getRecipeParameterName().equals(parameterName))
   						{
   							logger.info(paramData.getRecipeParameterName());
   							logger.info(parameterName);
   							isFound = true;
   						}
   						if (isFound) break;	 
   					}
   					if (isFound) break;	
   				}	
   				if (isFound) continue;	
   				else
   				{
   				   // RMS-040:The [{0}] Recipe[{1}] has Deleted the item[{2}] in EQ
   					throw new CustomException("RMS-040",paramData.getMachineName(), recipeName, paramData.getRecipeParameterName());
   				}
   			}
             */	
		}  
		else
		{
			return seqRecipeList;
		}
		
//		if(totalCount != mesCount)
//		{
		    //RMS-031: The [{0}] Recipe [{1}] has different parameter count. MESCount : {2} , EQPCount : {3}
//    		throw new CustomException("RMS-031", machineName , recipeName ,mesCount ,totalCount);
//		}
		
		/*
		// When sync EQ Level unitrecipename is inserted to TARGET column on OIC 
		//replyDoc > parameterGroup > EQP MainRecipeParamList
		for (Element parameterGroup : SMessageUtil.getBodySequenceItemList(replyDoc, "COMMANDLIST", false))
		{
		    String unitName = SMessageUtil.getChildText(parameterGroup, "UNITNAME", false);
			for (Element parameter : SMessageUtil.getSubSequenceItemList(parameterGroup, "PARAMETERLIST", false))
			{ 
				String unitRecipeName = SMessageUtil.getChildText(parameter, "VALUE", true);
				StringBuffer sqlBuffer = new StringBuffer("")
			              .append("  SELECT TARGET ")
			              .append("  FROM CT_RECIPEPARAMETER R ")
			              .append("  WHERE R.MACHINENAME = ? ")
			              .append("  AND R.RECIPENAME = ? ")
					      .append("  AND R.RECIPEPARAMETERNAME = ?");
				String sqlStmt = sqlBuffer.toString();
				Object[] bindSet = new String[]{machineName, recipeName, unitName};
				
				if(unitName != null)
				{
					List<ListOrderedMap> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStmt, bindSet); 
					
					if (sqlResult.size() > 0)
					{
						ListOrderedMap temp = sqlResult.get(0);
						
		           	   	String Target = CommonUtil.getValue(temp, "TARGET");

		           	    if(!Target.equals(unitRecipeName))
		           	    {
		           	       //RMS-019: RMSError:[{0}]Valid standard is [{1}] but current parameter is [{2}]
		           			throw new CustomException("RMS-019", unitName, Target, unitRecipeName);
		           	    }
		           	    
					}
					else
					{ 
					   //RMS-038: RMSError:Different parameter[{0}][{1}]
						throw new CustomException("RMS-038", unitName, unitRecipeName);
					}
				}
          	}
		}
		*/
		
		// Make unit recipe list by RecipeParam 
		for (Element parameterGroup : SMessageUtil.getBodySequenceItemList(replyDoc, "COMMANDLIST", false))
		{
			String unitName = SMessageUtil.getChildText(parameterGroup, "UNITNAME", false);
			
			//String unitName = SMessageUtil.getChildText(parameterGroup, "UNITNAME", true);
			//PPID list per unit
			Document resA = new Document();
			//RMSFlag
//				if(RMSFlag.equals("Y"))
//				{
//					//Get Recipe List From DB Like OIC
//					resA = getRecipeListBySuperMachine(unitName);
//				}
			
			//CCD, IND Pass
			if(unitName.endsWith("IK1") || unitName.endsWith("CCD") || unitName.endsWith("IND") || unitName.endsWith("UDP") || unitName.endsWith("LDP") || unitName.endsWith("TRS"))
			{
				continue;
			}
			
//				else
//				{
				//Create RECIPELIST by Unit
			//Get Unit Recipe List
			resA = MESRecipeServiceProxy.getRecipeServiceImpl().inquiryRecipeList(unitName, portName, carrierName);
//				}
			try
			{
				//2018-10-07
				for (Element recipe : SMessageUtil.getBodySequenceItemList(resA, "RECIPELIST", false))
				{
					String recipeNameCheck = SMessageUtil.getChildText(recipe, "RECIPENAME", false);
					
					if(recipeNameCheck.isEmpty())
					{
						//RMS-041: RMSError:[{0}] send Empty Recipe[{1}]
						throw new CustomException("RMS-041", unitName, "");
					}
				}
				
				//resA  > Unit锟斤拷 RecipeList
				dicRecipe.put(unitName, resA);
				
				//MainRecipeParamer锟斤拷锟斤拷 锟斤拷 Param锟斤拷锟斤拷 Unit, UnitRecipe 锟斤拷锟斤拷锟斤拷 锟斤拷
				for (Element parameter : SMessageUtil.getSubSequenceItemList(parameterGroup, "PARAMETERLIST", false))
				{
					String unitRecipeName = SMessageUtil.getChildText(parameter, "VALUE", true);
					String parameterName  = SMessageUtil.getChildText(parameter, "PARAMETERNAME", false);
					
					if(parameterName.equals("UNITRECIPE"))
					{
						parameterName = unitName;
					}
					
					//CheckFlag Check
					for (RecipeParameter paramData : paramList)
					{
						if(paramData.getMachineName().equals(machineName) && paramData.getRecipeName().equals(recipeName) && paramData.getRecipeParameterName().equals(parameterName) && paramData.getCheckFlag().equals("N"))
						{
							break;
						}
						else if(paramData.getMachineName().equals(machineName) && paramData.getRecipeName().equals(recipeName) && paramData.getRecipeParameterName().equals(parameterName))
						{		
							SequenceParameter entity = new SequenceParameter(unitName, unitRecipeName);
							seqRecipeList.add(entity);
							break;
						}
					}
				}
			}
			catch (Exception ex)
			{
				logger.error(String.format("Unit Recipe info from Unit[%s] parsing failed", unitName));
			}
		}
		
		//sequence set integrity check
		if (seqRecipeList.size() < 1)
		{
			//RMS-020:RMSError:Host Recipe[{0}] does not have any sequence
			throw new CustomException("RMS-020", recipeName);
		}
		
		//MainRecipeParam锟斤拷锟斤拷 锟睫酒匡拷 Unit 锟斤拷锟斤拷锟斤拷 锟斤拷
		for (SequenceParameter seqRecipeData : seqRecipeList)
		{
			//existence check-up in MES
			Recipe unitRecipeData;
			try
			{
				// || seqRecipeData.getUnitName().endsWith("UDP") || seqRecipeData.getUnitName().endsWith("LDP") || seqRecipeData.getUnitName().endsWith("TRS")
				//CCD Pass
				if(seqRecipeData.getUnitName().endsWith("IK1") || seqRecipeData.getUnitName().endsWith("CCD") || seqRecipeData.getUnitName().endsWith("UDP") || seqRecipeData.getUnitName().endsWith("LDP") ||seqRecipeData.getUnitName().endsWith("TRS"))
				{
					continue;
				}
				
				unitRecipeData = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] {seqRecipeData.getUnitName(), seqRecipeData.getUnitRecipeName()});
			}
			catch (Exception e)
			{
				throw new CustomException("SYS-9999", "RMS", e.getMessage());
			}

			//Validation UnitRecipe state			
			if (!(unitRecipeData.getINTFlag().equals("Y") && unitRecipeData.getMFGFlag().equals("Y") && unitRecipeData.getENGFlag().equals("Y") && unitRecipeData.getRMSFlag().equals("Y")))
			{
				//RMS-006: RMSError:Recipe[{0}] is not permitted
				throw new CustomException("RMS-006", unitRecipeData.getRecipeName());
			}
			
			if(recipeData.getAutoChangeFlag().equals("INTENG") || recipeData.getAutoChangeFlag().equals("ENGINT"))
			{
				if (recipeData.getDurationUsedLimit() == 0)
				{
					//RMS-034: RMSError:Recipe[{0}] DurationUsedLimit 0
					throw new CustomException("RMS-034", recipeName);
				}
				
				if (recipeData.getTimeUsedLimit() == 0)
				{
					//RMS-035: RMSError:Recipe[{0}] TimeUsedLimit 0
					throw new CustomException("RMS-035", recipeName);
				}
			}
			//existence verification in EQP
			//Document resA = MESRecipeServiceProxy.getRecipeServiceImpl().inquiryRecipeList(seqRecipeData.getUnitName(), portName, carrierName);
			Document docUnitRecipeList = dicRecipe.get(unitRecipeData.getMachineName());
			boolean isFound = false;
			for (Element eleRecipe : SMessageUtil.getBodySequenceItemList(docUnitRecipeList, "RECIPELIST", true))
			{
				logger.info(JdomUtils.toString(eleRecipe));
				
				String unitRecipeName = SMessageUtil.getChildText(eleRecipe, "RECIPENAME", true);
				//2018-10-07
				if(unitRecipeName.isEmpty())
				{
					//RMS-041: RMSError:[{0}] send Empty Recipe[{1}]
					throw new CustomException("RMS-041", unitRecipeData.getMachineName(), "");
				}
				
				if (unitRecipeData.getRecipeName().equals(unitRecipeName))
				{
					isFound = true;
					break;
				}
			}
			
			if (!isFound)
			{
				//RMS-022: RMSError:Recipe[{0}] does not exist in Unit[{1}]
				throw new CustomException("RMS-022", unitRecipeData.getRecipeName(), unitRecipeData.getMachineName());
			}
		}
		
		//proceed to next level
		return seqRecipeList;
	}
	
	public Document generateRecipeInquiry(String targetMahineName, String recipeType, String portName, String carrierName) throws CustomException
	{
		try
		{
			Element bodyElement = new Element(SMessageUtil.Body_Tag);
			{
//				Element attMachineName = new Element("MACHINENAME");
//				attMachineName.setText(machineName);
//				bodyElement.addContent(attMachineName);
//				
				Element attPortName = new Element("PORTNAME");
				attPortName.setText(portName);
				bodyElement.addContent(attPortName);
				
				Element attDurableName = new Element("CARRIERNAME");
				attDurableName.setText(carrierName);
				bodyElement.addContent(attDurableName);
//				
//				Element attTargetName = new Element("TARGETNAME");
//				attTargetName.setText(targetName);
//				bodyElement.addContent(attTargetName);
//				
//				Element attRecipeType = new Element("RECIPETYPE");
//				attRecipeType.setText(recipeType);
//				bodyElement.addContent(attRecipeType);
				
				Element attMachineName = new Element("MACHINENAME");
				attMachineName.setText(targetMahineName);
				bodyElement.addContent(attMachineName);
				
				Element attRecipeType = new Element("RECIPETYPE");
				attRecipeType.setText(recipeType);
				bodyElement.addContent(attRecipeType);
			}
			
			
			Document doc = SMessageUtil.createXmlDocument(bodyElement, "MainRecipeRequest", "", "", "MES", "");
			return doc;
		}
		catch (Exception ex)
		{
			//RMS-025: RMSError:{0} writing failed
			throw new CustomException("RMS-025","RecipeValidRequest");
		}
	}
	
	public Document generateMainRecipeRequest(String targetMahineName, String recipeType, String portName, String carrierName) throws CustomException
	{
		try
		{
			Element bodyElement = new Element(SMessageUtil.Body_Tag);
			{				
				Element attMachineName = new Element("MACHINENAME");
				attMachineName.setText(targetMahineName);
				bodyElement.addContent(attMachineName);
				
				Element attRecipeType = new Element("RECIPETYPE");
				attRecipeType.setText(recipeType);
				bodyElement.addContent(attRecipeType);
				
				Element attPortName = new Element("PORTNAME");
				attPortName.setText(portName);
				bodyElement.addContent(attPortName);
				
				Element attDurableName = new Element("CARRIERNAME");
				attDurableName.setText(carrierName);
				bodyElement.addContent(attDurableName);
			}
			
			Document doc = SMessageUtil.createXmlDocument(bodyElement, "MainRecipeRequest", "", "", "MES", "");
			
			logger.info(JdomUtils.toString(doc));
			
			return doc;
		}
		catch (Exception ex)
		{
			//RMS-025: RMSError:{0} writing failed
			throw new CustomException("RMS-025", "RecipeValidRequest");
		}
	}
	
	public Document generateParameterRequest(String machineName, String targetMachineName, String recipeName, String recipeType, String portName, String carrierName) throws CustomException                
	{
		try
		{
			Element bodyElement = new Element(SMessageUtil.Body_Tag);
			{
				Element attMachineName = new Element("MACHINENAME");
				attMachineName.setText(machineName);
				bodyElement.addContent(attMachineName);
				
				if (recipeType.equalsIgnoreCase("U"))
				{
				    Element attUnitName = new Element("UNITNAME");
				    attUnitName.setText(targetMachineName);
				    bodyElement.addContent(attUnitName);
				
				    Element attSubUnitName = new Element("SUBUNITNAME");
				    attSubUnitName.setText("");
				    bodyElement.addContent(attSubUnitName);
				}
				else if (recipeType.equalsIgnoreCase("S")) 
				{
					Element attUnitName = new Element("UNITNAME");
					attUnitName.setText(targetMachineName.substring(0, 10));
					bodyElement.addContent(attUnitName);
					
					Element attSubUnitName = new Element("SUBUNITNAME");
					attSubUnitName.setText(targetMachineName);
					bodyElement.addContent(attSubUnitName);
				}
				else
				{
					Element attUnitName = new Element("UNITNAME");
					attUnitName.setText("");
					bodyElement.addContent(attUnitName);
					
					Element attSubUnitName = new Element("SUBUNITNAME");
					attSubUnitName.setText("");
					bodyElement.addContent(attSubUnitName);
				}
				
				Element attRecipeName = new Element("RECIPENAME");
				attRecipeName.setText(recipeName);
				bodyElement.addContent(attRecipeName);
				
				Element attRecipeType = new Element("RECIPETYPE");
				attRecipeType.setText(recipeType);
				bodyElement.addContent(attRecipeType);
				
				Element attPortName = new Element("PORTNAME");
				attPortName.setText(portName);
				bodyElement.addContent(attPortName);
				
				Element attDurableName = new Element("CARRIERNAME");
				attDurableName.setText(carrierName);
				bodyElement.addContent(attDurableName);
			}
			
			Document doc = SMessageUtil.createXmlDocument(bodyElement, "RecipeParameterRequest", "", "", "MES", "");
			
			logger.info(JdomUtils.toString(doc));
			
			return doc;
		}
		catch (Exception ex)
		{
			//RMS-025: RMSError:{0} writing failed
			throw new CustomException("RMS-025", "RecipeParameterValidRequest");
		}
	}
	
	public Document generateParameterInquiryRequest(String machineName, String targetMachineName, String recipeName, String recipeType, String portName, String carrierName) throws CustomException                
	{
		try
		{
			Element bodyElement = new Element(SMessageUtil.Body_Tag);
			{
				Element attMachineName = new Element("MACHINENAME");
				attMachineName.setText(machineName);
				bodyElement.addContent(attMachineName);
				
				if (recipeType.equalsIgnoreCase("U"))
				{
				    Element attUnitName = new Element("UNITNAME");
				    attUnitName.setText(targetMachineName);
				    bodyElement.addContent(attUnitName);
				
				    Element attSubUnitName = new Element("SUBUNITNAME");
				    attSubUnitName.setText("");
				    bodyElement.addContent(attSubUnitName);
				}
				else if (recipeType.equalsIgnoreCase("S")) 
				{
					Element attUnitName = new Element("UNITNAME");
					attUnitName.setText(targetMachineName.substring(0, 10));
					bodyElement.addContent(attUnitName);
					
					Element attSubUnitName = new Element("SUBUNITNAME");
					attSubUnitName.setText(targetMachineName);
					bodyElement.addContent(attSubUnitName);
				}
				else
				{
					Element attUnitName = new Element("UNITNAME");
					attUnitName.setText("");
					bodyElement.addContent(attUnitName);
					
					Element attSubUnitName = new Element("SUBUNITNAME");
					attSubUnitName.setText("");
					bodyElement.addContent(attSubUnitName);
				}
				
				Element attPortName = new Element("PORTNAME");
				attPortName.setText(portName);
				bodyElement.addContent(attPortName);
				
				Element attDurableName = new Element("CARRIERNAME");
				attDurableName.setText(carrierName);
				bodyElement.addContent(attDurableName);
				
//				Element attTargetName = new Element("TARGETNAME");
//				attTargetName.setText(targetMachineName);
//				bodyElement.addContent(attTargetName);
				
				Element attRecipeName = new Element("RECIPENAME");
				attRecipeName.setText(recipeName);
				bodyElement.addContent(attRecipeName);
				
				Element attRecipeType = new Element("RECIPETYPE");
				attRecipeType.setText(recipeType);
				bodyElement.addContent(attRecipeType);
			}
			
			Document doc = SMessageUtil.createXmlDocument(bodyElement, "RecipeParameterRequest", "", "", "MES", "");
			
			logger.info(JdomUtils.toString(doc));
			
			return doc;
		}
		catch (Exception ex)
		{
			//RMS-025: RMSError:{0} writing failed
			throw new CustomException("RMS-025", "RecipeParameterValidRequest");
		}
	}
	
	public Document generateRecipeCheckStartReply(String targetMachineName, String portName, String portType, String portUseType, String carrierName, String originalSubjectName) throws CustomException
	{
		try
		{
			String targetSubjectName = MESRecipeServiceProxy.getRecipeServiceUtil().getEASSubjectName(targetMachineName);
			
			Element bodyElement = new Element(SMessageUtil.Body_Tag);
			{
				Element attMachineName = new Element("MACHINENAME");
				attMachineName.setText(targetMachineName);
				bodyElement.addContent(attMachineName);
				
				Element attPortName = new Element("PORTNAME");
				attPortName.setText(portName);
				bodyElement.addContent(attPortName);
				
				Element attPortType = new Element("PORTTYPE");
				attPortType.setText(portType);
				bodyElement.addContent(attPortType);
				
				Element attPortUseType = new Element("PORTUSETYPE");
				attPortUseType.setText(portUseType);
				bodyElement.addContent(attPortUseType);
				
				Element attPortAccessMode = new Element("PORTACCESSMODE");
				attPortAccessMode.setText("");
				bodyElement.addContent(attPortAccessMode);
				
				Element attDurableName = new Element("CARRIERNAME");
				attDurableName.setText(carrierName);
				bodyElement.addContent(attDurableName);
				
				Element attSlotMap = new Element("SLOTMAP");
				attSlotMap.setText("");
				bodyElement.addContent(attSlotMap);
			}
			Document doc = SMessageUtil.createXmlDocument(bodyElement, "RecipeCheckStartReply", originalSubjectName, "", "MES", "");
			
			logger.info(JdomUtils.toString(doc));
			
			return doc;
		}
		catch (Exception ex)
		{
			//RMS-025: RMSError:{0} writing failed
			throw new CustomException("RMS-025", "RecipeCheckStartReply");
		}
	}
	
	public Document generateRecipeCheckStartReply(String recipeName, String result, List<Recipe> unitList, Document doc, String resultDesc) throws CustomException
	{
		try
		{
			Element bodyElement = SMessageUtil.getBodyElement(doc);
			{
				Element attRecipeName = new Element("RECIPENAME");
				attRecipeName.setText(recipeName);
				bodyElement.addContent(attRecipeName);
				
				SMessageUtil.setBodyItemValue(doc, "RESULT", result);
				/*
				Element attResult = new Element("RESULT");
				attResult.setText(result);
				bodyElement.addContent(attResult);
				*/
				//Element attResultDesc = new Element("RESULTDESCRIPTION");
				
				
				
				Element attUnitList = new Element("UNITLIST");
				if(result.equals("OK"))
				{
					//attResultDesc.setText("");
					//bodyElement.addContent(attResultDesc);
					
					for(Recipe unitRecipe : unitList)
					{
						Element attUnit = new Element("UNIT");
						{
							Element attUnitName = new Element("UNITNAME");
							attUnitName.setText(unitRecipe.getMachineName());
							attUnit.addContent(attUnitName);
							
							Element attUnitRecipeName = new Element("UNITRECIPENAME");
							attUnitRecipeName.setText(unitRecipe.getRecipeName());
							attUnit.addContent(attUnitRecipeName);
							
							Element attCheckFlag = new Element("CHECKFLAG");
							Element attCheckType = new Element("CHECKTYPE");
							if(unitRecipe.getUnitCheckFlag().equals("Y") || unitRecipe.getVersionCheckFlag().equals("Y"))
							{
								attCheckFlag.setText("Y");
								if(unitRecipe.getUnitCheckFlag().equals("Y"))
								{
									attCheckType.setText("PARAMETER");
								}
								else
								{
									attCheckType.setText("VERSION");
								}
							}
							else
							{
								attCheckFlag.setText("N");
								attCheckType.setText("");
							}
							attUnit.addContent(attCheckFlag);
							attUnit.addContent(attCheckType);
						}
						attUnitList.addContent(attUnit);
					}
					
					bodyElement.addContent(attUnitList);
				}
				else
				{
					SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", resultDesc);
					//attResultDesc.setText(resultDesc);
					//bodyElement.addContent(attResultDesc);
					
					attUnitList.setText("");
					bodyElement.addContent(attUnitList);
				}
			}
			
			return doc;
		}
		catch (Exception ex)
		{
			//RMS-025: RMSError:{0} writing failed
			throw new CustomException("RMS-025", "RecipeCheckStartReply");
		}
	}
	
	public Document generateRecipeCheckStartReplyForTension(String recipeName, String result, List<Recipe> unitList, Document doc, String resultDesc) throws CustomException
	{
		try
		{
			boolean setResultNG = false;
			
			Element bodyElement = SMessageUtil.getBodyElement(doc);
			{
				bodyElement.removeChild("UNITLIST");
				
				SMessageUtil.setBodyItemValue(doc, "RESULT", result);
				/*
				Element attResult = new Element("RESULT");
				attResult.setText(result);
				bodyElement.addContent(attResult);
				*/
				//Element attResultDesc = new Element("RESULTDESCRIPTION");
				
				
				
				Element attUnitList = new Element("UNITLIST");
				if(result.equals("OK"))
				{
					//attResultDesc.setText("");
					//bodyElement.addContent(attResultDesc);
					
					for(Recipe unitRecipe : unitList)
					{
						Element attUnit = new Element("UNIT");
						{
							Element attUnitName = new Element("UNITNAME");
							attUnitName.setText(unitRecipe.getMachineName());
							attUnit.addContent(attUnitName);
							
							Element attUnitRecipeName = new Element("UNITRECIPENAME");
							attUnitRecipeName.setText(unitRecipe.getRecipeName());
							attUnit.addContent(attUnitRecipeName);
							
							Element attCheckFlag = new Element("CHECKFLAG");
							Element attCheckType = new Element("CHECKTYPE");
							if(unitRecipe.getUnitCheckFlag().equals("Y") || unitRecipe.getVersionCheckFlag().equals("Y"))
							{
								attCheckFlag.setText("Y");
								if(unitRecipe.getUnitCheckFlag().equals("Y"))
								{
									attCheckType.setText("PARAMETER");
								}
								else
								{
									attCheckType.setText("VERSION");
								}
							}
							else
							{
								setResultNG = true;
								attCheckFlag.setText("N");
								attCheckType.setText("");
							}
							attUnit.addContent(attCheckFlag);
							attUnit.addContent(attCheckType);
						}
						attUnitList.addContent(attUnit);
					}
					
					bodyElement.addContent(attUnitList);
				}
				else
				{
					SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", resultDesc);
					//attResultDesc.setText(resultDesc);
					//bodyElement.addContent(attResultDesc);
					
					attUnitList.setText("");
					bodyElement.addContent(attUnitList);
				}
				if(setResultNG)
				{
					SMessageUtil.setBodyItemValue(doc, "RESULT", "NG");
					SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", "CheckType NG");
				}
				
			}
			
			return doc;
		}
		catch (Exception ex)
		{
			//RMS-025: RMSError:{0} writing failed
			throw new CustomException("RMS-025", "RecipeCheckStartReply");
		}
	}
	
	public Document generateRecipeCheckResultReply(List<Map<String, Object>> historyList, Document doc) throws CustomException
	{
		try
		{
			Element bodyElement = SMessageUtil.getBodyElement(doc);
			{
				Element attUnitList = new Element("UNITLIST");
				
				for(Map<String, Object> historyInfo : historyList)
				{
					if(!historyInfo.get("UNITNAME").equals("MAIN"))
					{
						Element attUnit = new Element("UNIT");
						{
							Element attUnitName = new Element("UNITNAME");
							attUnitName.setText(historyInfo.get("UNITNAME").toString());
							attUnit.addContent(attUnitName);
							
							String result = "OK";
							String desc = "";
							
							if((historyInfo.get("RESULT")!=null) && historyInfo.get("RESULT").equals("NG"))
							{
								result = "NG";
								if(historyInfo.get("RESULTCOMMENT") != null)
									desc = historyInfo.get("RESULTCOMMENT").toString();
							}
							
							Element attResult = new Element("RESULT");
							attResult.setText(result);
							attUnit.addContent(attResult);
							
							Element attResultComment = new Element("RESULTDESCRIPTION");
							attResultComment.setText(desc);
							attUnit.addContent(attResultComment);
						}
						attUnitList.addContent(attUnit);
					}
				}
				
				bodyElement.addContent(attUnitList);
			}
			
			return doc;
		}
		catch (Exception ex)
		{
			//RMS-025: RMSError:{0} writing failed
			throw new CustomException("RMS-025", "RecipeCheckResultReply");
		}
	}
	
	public Document generateRecipeCheckResultReplyForTension(List<Map<String, Object>> historyList, Document doc) throws CustomException
	{
		try
		{
			Element bodyElement = SMessageUtil.getBodyElement(doc);
			{
				Element attUnitList = new Element("UNITLIST");
				
				for(Map<String, Object> historyInfo : historyList)
				{
					if(!historyInfo.get("UNITNAME").equals("MAIN"))
					{
						Element attUnit = new Element("UNIT");
						{
							Element attUnitName = new Element("UNITNAME");
							attUnitName.setText(historyInfo.get("UNITNAME").toString());
							attUnit.addContent(attUnitName);
							
							String result = "OK";
							String desc = "";
							
							if((historyInfo.get("RESULT")!=null) && historyInfo.get("RESULT").equals("NG"))
							{
								result = "NG";
								desc = historyInfo.get("RESULTCOMMENT").toString();
							}
							
							Element attResult = new Element("RESULT");
							attResult.setText(result);
							attUnit.addContent(attResult);
							
							Element attResultComment = new Element("RESULTDESCRIPTION");
							attResultComment.setText(desc);
							attUnit.addContent(attResultComment);
						}
						attUnitList.addContent(attUnit);
					}
				}
				
				bodyElement.addContent(attUnitList);
			}
			
			return doc;
		}
		catch (Exception ex)
		{
			//RMS-025: RMSError:{0} writing failed
			throw new CustomException("RMS-025", "RecipeCheckResultReplyForTension");
		}
	}
	
	public Document generateRecipeCheckReply(String result, Document doc, String resultDesc) throws CustomException
	{
		try
		{
			SMessageUtil.setBodyItemValue(doc, "RESULT", result);
			SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", resultDesc);
			
			return doc;
		}
		catch (Exception ex)
		{
			//RMS-025: RMSError:{0} writing failed
			throw new CustomException("RMS-025", "RecipeCheckReply");
		}
	}
	
	public Document generateRecipeCheckReplyForTension(String result, Document doc, String resultDesc) throws CustomException
	{
		try
		{
			SMessageUtil.setBodyItemValue(doc, "RESULT", result);
			SMessageUtil.setBodyItemValue(doc, "RESULTDESCRIPTION", resultDesc);
			
			return doc;
		}
		catch (Exception ex)
		{
			//RMS-025: RMSError:{0} writing failed
			throw new CustomException("RMS-025", "RecipeCheckReplyForTension");
		}
	}
	
	//custom structure
	public class SequenceParameter
	{
		private String unitName;
		private String unitRecipeName;
		private String sunitName;
		private String sunitRecipeName;
		public SequenceParameter()
		{
			this.unitName = "";
			this.unitRecipeName = "";
		}
		
		public SequenceParameter(String unitName, String unitRecipeName)
		{
			this.unitName = unitName;
			this.unitRecipeName = unitRecipeName;
		}
		
		public String getUnitName()
		{
			return this.unitName;
		}
		public void setUnitName(String unitName)
		{
			this.unitName = unitName;
		}
		
		public String getUnitRecipeName()
		{
			return this.unitRecipeName;
		}
		public void setUnitRecipeName(String unitRecipeName)
		{
			this.unitRecipeName = unitRecipeName;
		}
		
		public String getSunitName() {
			return sunitName;
		}

		public void setSunitName(String sunitName) {
			this.sunitName = sunitName;
		}

		public String getSunitRecipeName() {
			return sunitRecipeName;
		}

		public void setSunitRecipeName(String sunitRecipeName) {
			this.sunitRecipeName = sunitRecipeName;
		}
	}

	public int getEqpParameterCount (Document replyDoc) throws CustomException
	{
		int totalCount = 0;
	   
        for (Element parameterGroup : SMessageUtil.getBodySequenceItemList(replyDoc, "COMMANDLIST", false))
        {   
			for (Element parameter : SMessageUtil.getSubSequenceItemList(parameterGroup, "PARAMETERLIST", false))
			{ 
				totalCount += 1;
			}
	
	    }
        return totalCount;
	}
	
	public boolean RMSFlagCheck(String recipeType, String machineName, String recipeName, String unitName, String productSpecName, String processFlowName, String processOperationName, String productOffset) throws CustomException
	{
		boolean checkFlag = false;
		
		List<String> recipeList = new ArrayList<>();
		
		if(recipeType.equals("U"))
		{
			List<RecipeParameter> recipeParam = null;
			
			try 
			{
				recipeParam = ExtendedObjectProxy.getRecipeParamService().select("machineName = ? AND recipeParameterName = ? AND value = ?  ", new Object[] {machineName, unitName, recipeName});
			} 
			catch (Exception e) 
			{
				logger.info(e.getCause());
				return checkFlag;
			}
			
			for(RecipeParameter paramInfo : recipeParam)
			{
				recipeList.add(paramInfo.getRecipeName());
			}
		}
		else
		{
			recipeList.add(recipeName);
		}
		
		MachineSpec machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);

		if (machineData.getFactoryName().equals("TP") && StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Photo))
		{
			ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec("TP", processOperationName, "00001");
			
			if(((!operationData.getUdfs().get("LAYERNAME").toString().equals("PEP1")) && (!operationData.getUdfs().get("LAYERNAME").toString().equals("PEP0"))) && StringUtil.isNotEmpty(productOffset))
			{
				TPOffsetAlignInfo offsetInfo = ExtendedObjectProxy.getTPOffsetAlignInfoService().selectByKey(false, new Object[]{productOffset, operationData.getKey().getProcessOperationName(), operationData.getKey().getProcessOperationVersion(), machineData.getKey().getMachineName()});
				String RMSFlag = offsetInfo.getRMSFlag();
				
				if(RMSFlag.equals("Y"))
					checkFlag = true;
			}
			else
			{
				List<Map<String, Object>> resultList = null;
				String sql = "SELECT P.MACHINERECIPENAME, P.RMSFLAG FROM TPFOPOLICY T, POSMACHINE P WHERE T.CONDITIONID = P.CONDITIONID AND P.MACHINENAME = :MACHINENAME AND T.PRODUCTSPECNAME = :PRODUCTSPECNAME AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME AND T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME";

				Map<String, Object> bindMap = new HashMap<>();
				bindMap.put("MACHINENAME", machineName);
				bindMap.put("PRODUCTSPECNAME", productSpecName);
				bindMap.put("PROCESSFLOWNAME", processFlowName);
				bindMap.put("PROCESSOPERATIONNAME", processOperationName);
				
				try 
				{
					resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
				} 
				catch (Exception ex) {
					logger.info(ex.getCause());
					return checkFlag;
				}
				if (resultList != null && resultList.size() > 0) 
				{
					String RMSFlag = ConvertUtil.getMapValueByName(resultList.get(0), "RMSFLAG");

					if (StringUtils.isEmpty(RMSFlag))
					{
						throw new CustomException("RECIPE-0011");
					}

					if(StringUtils.equals(RMSFlag, "Y"))
						checkFlag = true;
				}
			}
		}
		else if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_MaskPPA))
		{
			List<ReserveMaskRecipe> reserveMaskRecipe = null;
			
			String sql = "SELECT RM.MASKLOTNAME, RM.MASKSPECNAME, RM.PROCESSFLOWNAME, RM.PROCESSOPERATIONNAME, RM.MACHINENAME, RM.RECIPENAME, RM.RMSFLAG FROM CT_RESERVEMASKRECIPE RM WHERE RM.MACHINENAME = :MACHINENAME AND RM.RECIPENAME IN (:MACHINERECIPENAME) AND RM.RMSFLAG = 'Y' ";

			Map<String, Object> bindMap = new HashMap<>();
			bindMap.put("MACHINENAME", machineName);
			bindMap.put("MACHINERECIPENAME", recipeList);
			
			try 
			{
				reserveMaskRecipe = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			} 
			catch (Exception ex) 
			{
				logger.info(ex.getCause());
				return checkFlag;
			}
			
			if (reserveMaskRecipe != null && reserveMaskRecipe.size() > 0) {
				checkFlag = true;
			}
		}
		else
		{
			List<Map<String, Object>> resultList = null;
			String sql = "SELECT P.MACHINERECIPENAME, P.RMSFLAG FROM TPFOPOLICY T, POSMACHINE P WHERE T.CONDITIONID = P.CONDITIONID AND P.MACHINENAME = :MACHINENAME AND T.PRODUCTSPECNAME = :PRODUCTSPECNAME AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME AND T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME";

			Map<String, Object> bindMap = new HashMap<>();
			bindMap.put("MACHINENAME", machineName);
			bindMap.put("PRODUCTSPECNAME", productSpecName);
			bindMap.put("PROCESSFLOWNAME", processFlowName);
			bindMap.put("PROCESSOPERATIONNAME", processOperationName);
			
			try 
			{
				resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			} 
			catch (Exception ex) {
				logger.info(ex.getCause());
				return checkFlag;
			}
			if (resultList != null && resultList.size() > 0) 
			{
				String RMSFlag = ConvertUtil.getMapValueByName(resultList.get(0), "RMSFLAG");

				if (StringUtils.isEmpty(RMSFlag))
				{
					throw new CustomException("RECIPE-0011");
				}

				if(StringUtils.equals(RMSFlag, "Y"))
					checkFlag = true;
			}
		}
		
		return checkFlag;
	}
	
	private String getLotInfoBydurableNameForFisrtGlass(String carrierName) throws CustomException
	{
		List<Map<String, Object>> lotList;
		String lotName = "";

		String sql = "SELECT LOTNAME FROM LOT WHERE CARRIERNAME = :CARRIERNAME AND LOTSTATE = :LOTSTATE AND FIRSTGLASSFLAG = 'N' AND JOBNAME IS NOT NULL";

		Map<String, String> args = new HashMap<String, String>();
		args.put("CARRIERNAME", carrierName);
		args.put("LOTSTATE", GenericServiceProxy.getConstantMap().Lot_Released);

		lotList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

		if (lotList.size() > 0 && lotList != null)
			lotName = lotList.get(0).get("LOTNAME").toString();
		else
		{
			sql = "SELECT LOTNAME FROM LOT WHERE CARRIERNAME = :CARRIERNAME AND LOTSTATE = :LOTSTATE AND FIRSTGLASSFLAG IS NULL AND JOBNAME IS NOT NULL";

			lotList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

			if (lotList.size() > 0 && lotList != null)
				lotName = lotList.get(0).get("LOTNAME").toString();
			else
			{
				sql = "SELECT LOTNAME FROM LOT WHERE CARRIERNAME = :CARRIERNAME AND LOTSTATE = :LOTSTATE ";
				lotList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

				if (lotList.size() > 0 && lotList != null)
					lotName = lotList.get(0).get("LOTNAME").toString();
				else
					lotName = "";
			}
		}

		return lotName;
	}
}




