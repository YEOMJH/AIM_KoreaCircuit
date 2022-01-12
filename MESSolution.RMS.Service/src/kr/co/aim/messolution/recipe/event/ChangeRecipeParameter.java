package kr.co.aim.messolution.recipe.event;

import java.util.List;

import org.apache.commons.collections.map.ListOrderedMap;
import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.extended.object.management.data.RecipeParameter;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class ChangeRecipeParameter extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", false);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "RECIPENAME", true);
		String recipeParamName = SMessageUtil.getBodyItemValue(doc, "RECIPEPARAMETERNAME", true);
		String value = SMessageUtil.getBodyItemValue(doc, "VALUE", true);
		//MESRecipeServiceProxy.getRecipeServiceUtil().verifyUserPrivilege(getEventUser(), machineName);	
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Change", getEventUser(), getEventComment(), null, null);		
		String lowerLimit = SMessageUtil.getBodyItemValue(doc, "LOWERLIMIT", false);
		String upperLimit = SMessageUtil.getBodyItemValue(doc, "UPPERLIMIT", false);
		String target = SMessageUtil.getBodyItemValue(doc, "TARGET", false);
		String validationType = SMessageUtil.getBodyItemValue(doc, "VALIDATIONTYPE", false);	
		String checkFlag = SMessageUtil.getBodyItemValue(doc, "CHECKFLAG", false);	
		RecipeParameter recipeParamInfo = ExtendedObjectProxy.getRecipeParamService().selectByKey(false, new Object[] {machineName, recipeName, recipeParamName, value});				
		
        StringBuffer sqlBuffer = new StringBuffer("")
	                             .append(" SELECT R.LOWERLIMIT,R.UPPERLIMIT,R.TARGET,R.VALIDATIONTYPE")
	                             .append("   FROM CT_RECIPEparameter R ")
                                 .append("  WHERE ROWNUM=1 ")
	                             .append("	 AND R.machineName= ? ")
		                         .append("    AND R.recipename = ? ")
	                             .append("    AND R.RECIPEPARAMETERNAME = ? ")
        					     .append("    AND R.VALUE = ? ");
       String sqlStmt = sqlBuffer.toString();
       Object[] bindSet = new String[]{machineName, recipeName,recipeParamName, value};
	   List<ListOrderedMap> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStmt, bindSet); 	
	   if(sqlResult.size()>0)
	   {
		   ListOrderedMap temp=  sqlResult.get(0);  
		   String OLDLOWERLIMIT = CommonUtil.getValue(temp, "LOWERLIMIT");
		   String OLDUPPERLIMIT = CommonUtil.getValue(temp, "UPPERLIMIT");
		   String OLDTARGET = CommonUtil.getValue(temp, "TARGET");
		   String OLDVALIDATIONTYPE = CommonUtil.getValue(temp, "VALIDATIONTYPE");     
		   recipeParamInfo.setOLDLOWERLIMIT(OLDLOWERLIMIT);
		   recipeParamInfo.setOLDTARGET(OLDTARGET);
		   recipeParamInfo.setOLDUPPERLIMIT(OLDUPPERLIMIT);
		   recipeParamInfo.setOLDVALIDATIONTYPE(OLDVALIDATIONTYPE);        
	    }	
		//modifiable attributes
	   
	   eventInfo.setEventName("Change Recipe Parameter");
	   if(!recipeParamInfo.getValidationType().equals(validationType))
	   {
		   Recipe recipe = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] {machineName, recipeName});
		   //recipe.setRMSFlag("N");
		   ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipe);
	   }
		recipeParamInfo.setLowerLimit(lowerLimit);
		recipeParamInfo.setUpperLimit(upperLimit);
		recipeParamInfo.setTarget(target);
		recipeParamInfo.setValidationType(validationType);
		recipeParamInfo.setCheckFlag(checkFlag);
		recipeParamInfo = ExtendedObjectProxy.getRecipeParamService().modify(eventInfo, recipeParamInfo);		
		//eventInfo.setEventName("Change Recipe Parameter");
		//ExtendedObjectProxy.getRecipeService().makeNotAvailable1(eventInfo, machineName, recipeName, "");		
		return doc;
	}
}
