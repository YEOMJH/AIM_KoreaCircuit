package kr.co.aim.messolution.datacollection.event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class SPCControlLimit extends SyncHandler {
	public static Log log = LogFactory.getLog(SPCControlLimit.class);
	public Object doWorks(Document doc)throws CustomException{
		Element SPCControlLimitList = SMessageUtil.getBodySequenceItem(doc, "SPCControlLimitList", false);	
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("SPCControlLimit", getEventUser(), getEventComment(), "", "");	
		if (SPCControlLimitList != null)
		{ 
			for ( Iterator iteratorLotList = SPCControlLimitList.getChildren().iterator(); iteratorLotList.hasNext();)				
			{
				Element ControlLimitE = (Element) iteratorLotList.next();
				String SpcControllimit     = SMessageUtil.getChildText(ControlLimitE, "SPCCONTROLLIMIT", true);
				String SpcControlSpecName  = SMessageUtil.getChildText(ControlLimitE, "SPCCONTROLSPECNAME", true);
				String ItemName            = SMessageUtil.getChildText(ControlLimitE, "ITEMNAME", true);
				String ControlChartType    = SMessageUtil.getChildText(ControlLimitE, "CONTROLCHARTTYPE", true);
				String Target              = SMessageUtil.getChildText(ControlLimitE, "TARGET", true);
				String UpperSpecLimit      = SMessageUtil.getChildText(ControlLimitE, "UPPERSPECLIMIT", true);
				String LowerSpecLimit      = SMessageUtil.getChildText(ControlLimitE, "LOWERSPECLIMIT", true);
				String UpperScreenLimit    = SMessageUtil.getChildText(ControlLimitE, "UPPERSCREENLIMIT", true);
				String LowerScreenLimit    = SMessageUtil.getChildText(ControlLimitE, "LOWERSCREENLIMIT", true);
				String EventUser	       = SMessageUtil.getChildText(ControlLimitE, "EVENTUSER", true);
				
				Float fTarget = Float.parseFloat( Target );
				Float fUpperSpecLimit = Float.parseFloat( UpperSpecLimit );
				Float fLowerSpecLimit = Float.parseFloat( LowerSpecLimit );
				Float fUpperScreenLimit = Float.parseFloat( UpperScreenLimit );
				Float fLowerScreenLimit = Float.parseFloat( LowerScreenLimit );
								
				if (SpcControllimit.equals("SPCOOS") )
				{
					String Checksql = " SELECT * FROM MES_SPCCONTROLSPECITEM " +
					                  " WHERE SPCCONTROLSPECNAME = :SPCCONTROLSPECNAME " +
					                  " AND ITEMNAME = :ITEMNAME" +
					                  " AND CONTROLCHARTTYPE = :CONTROLCHARTTYPE" ;
					Map<String, Object> bindMap = new HashMap<String, Object>();
					bindMap.put("SPCCONTROLSPECNAME", SpcControlSpecName);
					bindMap.put("ITEMNAME", ItemName);
					bindMap.put("CONTROLCHARTTYPE", ControlChartType);
					List<Map<String, Object>> Result = GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().queryForList(Checksql, bindMap);
		            if(Result.size()==0)
		       	    {
		            	throw new CustomException("SPC-0001");
		            }
		            else
		            {
		               try
		                 {
		            	   String sql= " UPDATE MES_SPCCONTROLSPECITEM      "+
		            	   " SET TARGET           = :fTarget," +
		            	   "     UPPERSPECLIMIT   = :fUpperSpecLimit, "+
		            	   "     LOWERSPECLIMIT   = :fLowerSpecLimit, "+
		            	   "     UPPERSCREENLIMIT = :fUpperScreenLimit,"+
		            	   "     LOWERSCREENLIMIT = :fLowerScreenLimit,"+
		            	   "     LASTCLCREATEDTIME= SYSDATE,           "+
		            	   "     EVENTUSER        = :EventUser         "+
		            	   " WHERE SPCCONTROLSPECNAME = :SpcControlSpecName " +
		            	   "   AND ITEMNAME = :ItemName " +
		            	   "   AND CONTROLCHARTTYPE = :ControlChartType " ;
		                  Object[] bindSet = new Object[] {fTarget,fUpperSpecLimit, fLowerSpecLimit,fUpperScreenLimit,fLowerScreenLimit,EventUser,SpcControlSpecName,ItemName,ControlChartType};
		                  GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().update(sql, bindSet);
		       		     }
		       		   catch(Exception e)
		       		     {
		       			   eventLog.info("Update SPCOOSLimit Failed");
		       			   throw new CustomException("SPC-0003");
		                 }	
		            }
				}
				if (SpcControllimit.equals("SPCOOC") )
				{
					String Checksql = " SELECT * FROM MES_SPCCONTROLSPECCHART " +
					                  " WHERE SPCCONTROLSPECNAME = :SPCCONTROLSPECNAME " +
					                  "   AND ITEMNAME           = :ITEMNAME " +
					                  "   AND CHARTNAME          = :CHARTNAME" ;
					Map<String, Object> bindMap = new HashMap<String, Object>();
					bindMap.put("SPCCONTROLSPECNAME", SpcControlSpecName);
					bindMap.put("ITEMNAME", ItemName);
					bindMap.put("CHARTNAME", ControlChartType);
					List<Map<String, Object>> Result= GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().queryForList(Checksql, bindMap);
		            if(Result.size()==0)
		       	    {
		            	throw new CustomException("SPC-0002");
		            }
		            else
		            {   
		               try
		                 {
		            	   String sql= " UPDATE MES_SPCCONTROLSPECCHART      "+
		            	   " SET CENTERLINE         = :fTarget," +
		            	   "     UPPERCONTROLLIMIT  = :fUpperSpecLimit,  "+
		            	   "     LOWERCONTROLLIMIT  = :fLowerSpecLimit,  "+
		            	   "     UPPERQUALITYLIMIT  = :fUpperScreenLimit,"+
		            	   "     LOWERQUALITYLIMIT  = :fLowerScreenLimit,"+
		            	   "     UPDATETIME         = SYSDATE,           "+
		            	   "     EVENTUSER          = :EventUser         "+		            	   
		            	   " WHERE SPCCONTROLSPECNAME = :SpcControlSpecName " +
		            	   "   AND ITEMNAME = :ItemName " +
		            	   "   AND CHARTNAME = :ControlChartType " ;
		                  Object[] bindSet = new Object[] {fTarget,fUpperSpecLimit,fLowerSpecLimit,fUpperScreenLimit,fLowerScreenLimit,EventUser,SpcControlSpecName,ItemName,ControlChartType};
		                  GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().update(sql, bindSet);
		       		     }
		       		   catch(Exception e)
		       		     {
		       			   eventLog.info("Update SPCOOCLimit Failed");
		       			   throw new CustomException("SPC-0003");
		                 }	
		            }
				}
			}
		}
		return doc;
	}
}
