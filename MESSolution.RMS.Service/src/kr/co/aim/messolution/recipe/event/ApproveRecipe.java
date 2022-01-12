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

public class ApproveRecipe extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", false);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "RECIPENAME", true);
		MESRecipeServiceProxy.getRecipeServiceUtil().verifyUserPrivilege(getEventUser(), machineName);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Approve", getEventUser(), getEventComment(), null, null);
	    Recipe recipeInfo = ExtendedObjectProxy.getRecipeService().selectByKey(false, new Object[] {machineName, recipeName});
		
		if (!recipeInfo.getRecipeType().equalsIgnoreCase("MAIN"))
		{
			// RMS-008: RMSError:Only Main Recipe could become approved
			throw new CustomException("RMS-008");
		}
		  
			StringBuffer sqlBuffer = new StringBuffer("")
			                           .append("  SELECT R.LastChangeTime ")
			                           .append("  FROM CT_RECIPE R ")
			                           .append("  WHERE R.machineName = ?")
		                               .append("  AND R.recipeName= ?");
			   String sqlStmt = sqlBuffer.toString();
	           Object[] bindSet = new String[]{machineName, recipeName};
			   List<ListOrderedMap> sqlResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStmt, bindSet); 
			   ListOrderedMap temp=  sqlResult.get(0);
		       Date LastChangeTime = (Date) CommonUtil.getDateValue(temp, "LastChangeTime");
		       Timestamp LCT;	
		      
		       StringBuffer sqlBuffer1 = new StringBuffer("")
			                           .append("  SELECT R.recipeparametername,R.value")
			                           .append("  FROM CT_RECIPEPARAMETER R ")
			                           .append("  WHERE R.machineName = ?")
			                           .append("  AND R.recipeName= ? ");
			   String sqlStmt1 = sqlBuffer1.toString();
	           Object[] bindSet1 = new String[]{machineName, recipeName};
			   List<ListOrderedMap> sqlResult1 = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStmt1, bindSet1);    
		       if(sqlResult1.size()<1)
		       {
		    	    //RMS-009: RMSError:Please Syncs recipe first
		    	    throw new CustomException("RMS-009");
		       }
			   for (int i=0;i<sqlResult1.size();i++)
			  {
			    ListOrderedMap	temp1	=	sqlResult1.get(i);
				String recipeparametername = CommonUtil.getValue(temp1, "recipeparametername");
			    String value= CommonUtil.getValue(temp1, "value");  
			   
			    StringBuffer sqlBuffer2 = new StringBuffer("")
		     	                         .append(" SELECT R.LastChangeTime,R.Activestate")
				                         .append("   FROM CT_RECIPE R ")
				                         .append("  WHERE R.machineName = ?   ")
	                                     .append("	 AND R.recipeName= ? ");
			    String sqlStmt2 = sqlBuffer2.toString();
				Object[] bindSet2 = new String[]{recipeparametername, value};
				List<ListOrderedMap> sqlResult2 = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStmt2, bindSet2); 
				if (sqlResult2.size() < 1)
				{
					// RMS-010: RMSError:MES SYS do not have the parameter[{0}]and the Recipe[{1}]
					throw new CustomException("RMS-010", recipeparametername, value);
				}
				ListOrderedMap temp2=  sqlResult2.get(0);
			    Date LastChangeTime1 = (Date) CommonUtil.getDateValue(temp2, "LastChangeTime");
			    String Activestate =  CommonUtil.getValue(temp2, "Activestate");  
			    
				if (Activestate.equals("NotActive"))
				{
					// RMS-011: RMSError:Please active Recipe first
					throw new CustomException("RMS-011");
				}
			    else{}
			    if(LastChangeTime1==null&&LastChangeTime!=null)
			          { 
			    	    LastChangeTime= LastChangeTime1;
			          }
			    else if(LastChangeTime1!=null&&LastChangeTime==null)
			          { 
			         	LastChangeTime= LastChangeTime;
			    	  }
			    else if(LastChangeTime1==null&&LastChangeTime==null)	
			          { 	
			    	     Timestamp currentTime = new Timestamp(System.currentTimeMillis());
			             LastChangeTime=currentTime;
			          }
			    else
			          {
			            LastChangeTime= LastChangeTime.after(LastChangeTime1)?LastChangeTime:LastChangeTime1;	
			          }
	           }
			
				String lastChangeTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(LastChangeTime);
			    LCT  = Timestamp.valueOf (lastChangeTime);
		  //default spec info
		  recipeInfo.setRecipeState("Approved");
		  recipeInfo.setLastApporveTime(eventInfo.getEventTime());
		  //recipeInfo.setLastChangeTime(LCT);	
		  //history trace
		  recipeInfo.setLastEventName(eventInfo.getEventName());
		  recipeInfo.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		  recipeInfo.setLastEventUser(eventInfo.getEventUser());
	      recipeInfo.setLastEventComment(eventInfo.getEventComment());
		 /* recipeInfo.setLastChangeTime(LCT);*/		
		  recipeInfo = ExtendedObjectProxy.getRecipeService().modify(eventInfo, recipeInfo);
		
		  return doc;
	}
}
