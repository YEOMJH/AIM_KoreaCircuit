package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import sun.management.counter.Units;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ReviewProductImageJudge extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String ProductName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String ProcessOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String MachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String ImageName = SMessageUtil.getBodyItemValue(doc, "IMAGENAME", true);
		String ScreenJudge = SMessageUtil.getBodyItemValue(doc, "SCREENJUDGE", false);
		String DefectJudge = SMessageUtil.getBodyItemValue(doc, "DEFECTJUDGE", false);
		String DefectCode = SMessageUtil.getBodyItemValue(doc, "DEFECTCODE", false);
		String PanelName = SMessageUtil.getBodyItemValue(doc, "PANELNAME", false);
		String DetaX = SMessageUtil.getBodyItemValue(doc, "DETAX", false);
		String DetaY = SMessageUtil.getBodyItemValue(doc, "DETAY", false);
		String DetaX2 = SMessageUtil.getBodyItemValue(doc, "DETAX2", false);
		String DetaY2 = SMessageUtil.getBodyItemValue(doc, "DETAY2", false);
		String testImageDefectCode = SMessageUtil.getBodyItemValue(doc, "TESTIMAGEDEFECTCODE", false);
		String machineType = SMessageUtil.getBodyItemValue(doc, "MACHINETYPE", false);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", false);
		String rsType = SMessageUtil.getBodyItemValue(doc, "RSTYPE", false);

		// Insert
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReviewProductImageJudge", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		if(StringUtil.isEmpty(testImageDefectCode))
		{
			int qty = getReviewProductImageJudge(ProductName, ProcessOperationName, MachineName, ImageName);
 
			String ActionKind = "";

			if (qty == 0)
				ActionKind = "Insert";
			else
				ActionKind = "Update";
			
			if(StringUtil.equals(rsType, "AT"))
			{
				eventInfo = EventInfoUtil.makeEventInfo("ReviewProductImageJudgeForAT", getEventUser(), getEventComment(), "", "");
				eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
			}
			
			MESLotServiceProxy.getLotServiceImpl().insertCtReviewProductImageJudge(eventInfo, ProductName, ProcessOperationName, MachineName, ImageName, ScreenJudge, DefectCode, DefectJudge, PanelName,
					ActionKind, DetaX, DetaY, DetaX2, DetaY2);
		}
		else
		{
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT SEQ,PLANQTY,FINISHQTY,ABORTQTY,UNIT FROM CT_REVIEWSMARTEVALUATION ");
			sql.append("  WHERE     FACTORYNAME = :FACTORYNAME ");
			sql.append(" AND USERID = :USERID ");
			sql.append("   AND MACHINETYPE = :MACHINETYPE ");
			sql.append("  AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
			sql.append("  AND DEFECTCODE = :DEFECTCODE  ORDER BY SEQ ASC ");

			Map<String, Object> args = new HashMap<>();

			args.put("FACTORYNAME", factoryName);
			args.put("USERID", eventInfo.getEventUser());
			args.put("MACHINETYPE", machineType);
			args.put("PROCESSOPERATIONNAME", ProcessOperationName);
			args.put("DEFECTCODE", testImageDefectCode);

			List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), args);
			if(result.size()>0)
			{
				String seq=result.get(0).get("SEQ").toString();
				int planQty=Integer.parseInt(result.get(0).get("PLANQTY").toString());
				int finishQty=Integer.parseInt(result.get(0).get("FINISHQTY").toString());
				int abortQty=Integer.parseInt(result.get(0).get("ABORTQTY").toString());
				int unit=Integer.parseInt(result.get(0).get("UNIT").toString());
				InsertDefectCode(seq, eventInfo.getEventUser(), factoryName, machineType, ProcessOperationName, testImageDefectCode, ImageName, DefectCode, eventInfo);
				UpdateDefectCode(factoryName, seq, ProcessOperationName, testImageDefectCode, eventInfo,finishQty,abortQty,unit,planQty);
			}
			
		}


	}

	public int getReviewProductImageJudge(String ProductName, String ProcessOperationName, String MachineName, String ImageName)
	{
		int qty = 0;

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT COUNT (PRODUCTNAME) AS QTY ");
		sql.append("  FROM CT_REVIEWPRODUCTIMAGEJUDGE ");
		sql.append(" WHERE PRODUCTNAME = :PRODUCTNAME ");
		sql.append("   AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND MACHINENAME = :MACHINENAME ");
		sql.append("   AND IMAGESEQ = :IMAGENAME ");
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("PRODUCTNAME", ProductName);
		args.put("PROCESSOPERATIONNAME", ProcessOperationName);
		args.put("MACHINENAME", MachineName);
		args.put("IMAGENAME", ImageName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			String quantity = ConvertUtil.getMapValueByName(result.get(0), "QTY");
			qty = Integer.parseInt(quantity);
		}

		return qty;
	}
	
	private void InsertDefectCode(String seq, String userID,String factoryName, String machineType, String processOperationName, 
			String testImageDefectCode, String ImageName, String defectCode,EventInfo eventInfo)
			throws CustomException
	{
		try
		{
			StringBuffer sql = new StringBuffer();
			sql.append("INSERT ");
			sql.append("  INTO CT_SMARTEVALUATIONIMAGEJUDGE  ");
			sql.append("VALUES  ");
			sql.append("  (:TIMEKEY,:SEQ,:USERID,:FACTORYNAME,:MACHINETYPE, ");
			sql.append("   :PROCSSOPERATIONNAME,:DEFECTCODE,:IMAGENAME,:JUDGECODE,:CURRECT,:EVENTNAME,:EVENTCOMMENT) ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("TIMEKEY",TimeUtils.getCurrentEventTimeKey());
			bindMap.put("SEQ", seq);
			bindMap.put("USERID", userID);
			bindMap.put("FACTORYNAME", factoryName);
			bindMap.put("MACHINETYPE", machineType);
			bindMap.put("PROCSSOPERATIONNAME", processOperationName);
			bindMap.put("DEFECTCODE", testImageDefectCode);
			bindMap.put("IMAGENAME", ImageName);
			bindMap.put("JUDGECODE", defectCode);
			if(StringUtils.equals(testImageDefectCode, defectCode))
			{
				bindMap.put("CURRECT", "Y");
			}
			else
			{
				bindMap.put("CURRECT", "N");
			}		
			bindMap.put("EVENTNAME", eventInfo.getEventName());
			bindMap.put("EVENTCOMMENT", eventInfo.getEventComment());
			
			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", " Insert Into SmartImageJudge Failed" );
		}
	}

	private void UpdateDefectCode(String factoryname, String seq,String ProcessOperationName, String testImageDefectCode, EventInfo eventInfo,
			int finishQty,int abortQuantity,int unit,int planQty)
			throws CustomException
	{
		try
		{
			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE CT_REVIEWSMARTEVALUATION ");
			sql.append("   SET FINISHQTY=:FINISHQTY, ");
			sql.append("       ABORTQTY=:ABORTQTY , ");
			sql.append("       LASTEVENTTIMEKEY=:LASTEVENTTIMEKEY, ");
			sql.append("       LASTEVENTUSER=:LASTEVENTUSER, ");
			sql.append("       LASTEVENTNAME=:LASTEVENTNAME, ");
			sql.append("       LASTEVENTCOMMENT=:LASTEVENTCOMMENT, ");
			sql.append("       TESTSTATE=:TESTSTATE ");
			sql.append("       WHERE SEQ=:SEQ  ");
			sql.append("       AND USERID=:USERID ");
			sql.append("       AND PROCESSOPERATIONNAME=:PROCESSOPERATIONNAME ");
			sql.append("       AND DEFECTCODE=:DEFECTCODE ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("FINISHQTY", ++finishQty);
            if(++finishQty==planQty){
                bindMap.put("TESTSTATE", "Completed");
            }
            else {
            	bindMap.put("TESTSTATE", "Released");
			}
            if(abortQuantity==0)
            {
            	bindMap.put("ABORTQTY", unit-1);
            }
            else
            {
            	bindMap.put("ABORTQTY", --abortQuantity);
            }

			bindMap.put("LASTEVENTTIMEKEY", eventInfo.getEventTimeKey());
			bindMap.put("LASTEVENTUSER", eventInfo.getEventUser());
			bindMap.put("LASTEVENTNAME", eventInfo.getEventUser());
			bindMap.put("LASTEVENTCOMMENT", eventInfo.getEventComment());
			bindMap.put("SEQ", seq);
			bindMap.put("USERID", eventInfo.getEventUser());
			bindMap.put("PROCESSOPERATIONNAME", ProcessOperationName);
			bindMap.put("DEFECTCODE", testImageDefectCode);
			

			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", "update smartTest Failed " + e.toString());
		}
	}
}
