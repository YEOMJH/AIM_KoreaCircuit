package kr.co.aim.messolution.recipe.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;

public class RecipeControl extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME",true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME",true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME",true);
		String productSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION",true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME",true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION",true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME",true);
		String processOperationVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION",true);
		
		String check = SMessageUtil.getBodyItemValue(doc, "Check",true);
		
		String MFG = SMessageUtil.getBodyItemValue(doc, "MFG",false);
		String INT = SMessageUtil.getBodyItemValue(doc, "INT",false);
		String autoChangeFlag = SMessageUtil.getBodyItemValue(doc, "AUTOCHANGEFLAG",false);
		String autoChangeTime = SMessageUtil.getBodyItemValue(doc, "AUTOCHANGETIME",false);
		String autoChangeLotQuantity = SMessageUtil.getBodyItemValue(doc, "AUTOCHANGELOTQUANTITY",false);
		
		String conditionId = GetconditionID(factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion);
				
		if(check.equals("MFG"))
		{
			updataMFGInfo(conditionId, MFG, autoChangeFlag, autoChangeTime, autoChangeLotQuantity);
		}
		
		if(check.equals("INT"))
		{
			updataINTInfo(conditionId, INT);
		}
		
		return doc;
	}
	
	private String GetconditionID(String factoryName, String productSpecName, String productSpecVersion, 
			String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion) throws CustomException
	{
		String inquirysql = "SELECT T.CONDITIONID "
				+ "FROM TPFOPOLICY T "
				+ "WHERE T.FACTORYNAME = :FACTORYNAME "
				+ "AND T.PRODUCTSPECNAME = :PRODUCTSPECNAME "
				+ "AND T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION "
				+ "AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME "
				+ "AND T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION "
				+ "AND T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME "
				+ "AND T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ";
		Map<String, String> inquirybindMap = new HashMap<String, String>();
		
		inquirybindMap.put("FACTORYNAME", factoryName);
		inquirybindMap.put("PRODUCTSPECNAME", productSpecName);
		inquirybindMap.put("PRODUCTSPECVERSION", productSpecVersion);
		inquirybindMap.put("PROCESSFLOWNAME", processFlowName);
		inquirybindMap.put("PROCESSFLOWVERSION", processFlowVersion);
		inquirybindMap.put("PROCESSOPERATIONNAME", processOperationName);
		inquirybindMap.put("PROCESSOPERATIONVERSION", processOperationVersion);
		
		List<Map<String, Object>> sqlResult = 
	       	GenericServiceProxy.getSqlMesTemplate().queryForList(inquirysql, inquirybindMap);
		
		String conditionID = "";
		
		if(sqlResult.size()>0)
		{
			conditionID = sqlResult.get(0).get("CONDITIONID").toString();
		}
		else
		{
			throw new CustomException("Check Information");
		}
		
		return conditionID;
	}
	
	private void updataMFGInfo(String conditionId, String MFG, String autoChangeFlag, String autoChangeTime, String autoChangeLotQuantity)
	{
		String sql= " UPDATE POSMACHINE SET MFG = :MFG," +
         	   "     AUTOCHANGEFLAG = :AUTOCHANGEFLAG,  "+
         	   "     AUTOCHANGETIME = :AUTOCHANGETIME,  "+
         	   "     AUTOCHANGELOTQUANTITY = :AUTOCHANGELOTQUANTITY"+         	   
         	   " WHERE CONDITIONID = :CONDITIONID ";
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("CONDITIONID", conditionId);
		args.put("MFG", MFG);
		args.put("AUTOCHANGEFLAG", autoChangeFlag);
		args.put("AUTOCHANGETIME", autoChangeTime);
		args.put("AUTOCHANGELOTQUANTITY", autoChangeLotQuantity);

		GenericServiceProxy.getSqlMesTemplate().update(sql, args);
	}

	private void updataINTInfo(String conditionId, String INT)
	{
		String sql = " UPDATE POSMACHINE SET INT = :INT WHERE CONDITIONID = :CONDITIONID ";

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("CONDITIONID", conditionId);
		args.put("INT", INT);

		GenericServiceProxy.getSqlMesTemplate().update(sql, args);
	}

}
