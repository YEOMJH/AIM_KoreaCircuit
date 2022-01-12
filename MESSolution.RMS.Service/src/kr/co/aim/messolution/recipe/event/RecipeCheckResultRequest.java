package kr.co.aim.messolution.recipe.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.RecipeCheckResult;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;

import org.jdom.Document;

public class RecipeCheckResultRequest extends SyncHandler {

	@Override
	public Document doWorks(Document doc) throws CustomException {
		
		this.prepareReply(doc);
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String result = SMessageUtil.getBodyItemValue(doc, "RESULT", true);
		String resultDesc = SMessageUtil.getBodyItemValue(doc, "RESULTDESCRIPTION", false);
		
		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		
		RecipeCheckResult resultInfo = new RecipeCheckResult();
		List<RecipeCheckResult> resultInfoList = new ArrayList<RecipeCheckResult>();
		try 
		{
			String condition = " WHERE MACHINENAME = ? AND PORTNAME = ? AND CARRIERNAME = ? ";
			
			resultInfoList = ExtendedObjectProxy.getRecipeCheckResultService().select(condition, new Object[]{machineName, portName, carrierName});
			resultInfo = resultInfoList.get(0);
		} 
		catch (Exception e) 
		{}
		
		List<Map<String, Object>> historyList = new ArrayList<Map<String, Object>>();
		
		try 
		{
			historyList = getRecipeCheckResultHistory(resultInfo);
		} 
		catch (Exception e) 
		{}
		
		//ResultRequest
		doc = MESRecipeServiceProxy.getRecipeServiceUtil().generateRecipeCheckResultReply(historyList, doc);
		
		return doc;
	}
	
	private void prepareReply(Document doc) throws CustomException
	{
		String oldSourceSubjectName = SMessageUtil.getHeaderItemValue(doc, "SOURCESUBJECTNAME", false);
		String oldTargetSubjectName = SMessageUtil.getHeaderItemValue(doc, "TARGETSUBJECTNAME", false);

		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "RecipeCheckResultReply");
		SMessageUtil.setHeaderItemValue(doc, "ORIGINALSOURCESUBJECTNAME", oldTargetSubjectName);
		SMessageUtil.setHeaderItemValue(doc, "SOURCESUBJECTNAME", oldTargetSubjectName);
		SMessageUtil.setHeaderItemValue(doc, "TARGETSUBJECTNAME", oldSourceSubjectName);
	}
	
	private List<Map<String, Object>> getRecipeCheckResultHistory(RecipeCheckResult resultInfo )
	{
		String sql = "SELECT H.MACHINENAME, H.PORTNAME, H.CARRIERNAME, H.RECIPENAME, H.RESULT, H.RESULTCOMMENT, H.UNITNAME "
						+ "FROM CT_RECIPECHECKRESULTHISTORY H "
						+ "WHERE H.MACHINENAME = :machineName "
							+ "AND H.PORTNAME = :portName "
							+ "AND H.CREATETIMEKEY = :createTimeKey";
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("machineName", resultInfo.getMachineName());
		bindMap.put("portName", resultInfo.getPortName());
		bindMap.put("createTimeKey", resultInfo.getCreateTimeKey());
		
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		return result;
	}
}
