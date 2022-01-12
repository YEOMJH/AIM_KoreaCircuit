package kr.co.aim.messolution.recipe.event;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.map.ListOrderedMap;
import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.Recipe;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class ChangeINTFlag extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", false);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "RECIPENAME", true);
		String INTFlag = SMessageUtil.getBodyItemValue(doc, "INFFlag", true);
		MESRecipeServiceProxy.getRecipeServiceUtil().verifyUserPrivilege(getEventUser(), machineName);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("INFFlagChange", getEventUser(), getEventComment(), null, null);
	    Recipe recipeInfo = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] {machineName, recipeName});
	    
	    if(recipeInfo.getINTFlag().equals(INTFlag))
	    {
	    	throw new CustomException("RECIPE-0001");
	    }
	    /*
		StringBuffer sqlBuffer = new StringBuffer("").append("  SELECT R.LastChangeTime ").append("  FROM CT_RECIPE R ")
				.append("  WHERE R.machineName = ?").append("  AND R.recipeName= ?");
		String sqlStmt = sqlBuffer.toString();
		Object[] bindSet = new String[] { machineName, recipeName };
		List<ListOrderedMap> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate()
				.queryForList(sqlStmt, bindSet);
		ListOrderedMap temp = sqlResult.get(0);
		Date LastChangeTime = (Date) CommonUtil.getDateValue(temp, "LastChangeTime");
		Timestamp LCT;

		StringBuffer sqlBuffer1 = new StringBuffer("").append("  SELECT R.recipeparametername,R.value")
				.append("  FROM CT_RECIPEPARAMETER R ").append("  WHERE R.machineName = ?")
				.append("  AND R.recipeName= ? ");
		String sqlStmt1 = sqlBuffer1.toString();
		Object[] bindSet1 = new String[] { machineName, recipeName };
		List<ListOrderedMap> sqlResult1 = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate()
				.queryForList(sqlStmt1, bindSet1);
		if (sqlResult1.size() < 1) 
		{
			  //RMS-009: RMSError:Please Syncs recipe first
		      throw new CustomException("RMS-009");
		}
		*/
		recipeInfo.setINTFlag(INTFlag);
		recipeInfo.setRMSFlag("N");
		recipeInfo.setLastEventName(eventInfo.getEventName());
		recipeInfo.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		
		recipeInfo.setLastEventUser(eventInfo.getEventUser());
		recipeInfo.setLastEventComment(eventInfo.getEventComment());
		recipeInfo = ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipeInfo);

		return doc;
	}
}
