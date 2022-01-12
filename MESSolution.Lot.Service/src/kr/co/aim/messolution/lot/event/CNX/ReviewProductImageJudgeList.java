package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.lot.event.CNX.PostCell.DeleteDefectFQCCode;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ReviewProductImageJudgeList extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String ProductName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String ProcessOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String MachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String ImageName = SMessageUtil.getBodyItemValue(doc, "IMAGENAME", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReviewProductImageJudge", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		List<Element> DefectCodeList = SMessageUtil.getBodySequenceItemList(doc, "DEFECTCODELIST", false);

		MESLotServiceProxy.getLotServiceImpl().DeleteCtReviewProductImageJudge(eventInfo, ProductName, ProcessOperationName, MachineName, ImageName);

		for (Element DefectE : DefectCodeList)
		{
			String Seq = DefectE.getChildText("SEQ");
			String ScreenJudge = DefectE.getChildText("SCREENJUDGE");
			String DefectJudge = DefectE.getChildText("DEFECTJUDGE");
			String DefectCode = DefectE.getChildText("DEFECTCODE");
			String PanelName = DefectE.getChildText("PANELNAME");
			String PanelGrade = DefectE.getChildText("PANELGRADE");
			String qty = SMessageUtil.getChildText(DefectE, "QTY", false);
			String ActionKind = "Insert";
            if(!StringUtils.equals(MachineName, "3ATT04"))
            {
    			MESLotServiceProxy.getLotServiceImpl().insertCtReviewProductImageJudgeSeq(eventInfo, ProductName, ProcessOperationName, MachineName, ImageName, ScreenJudge, DefectCode, DefectJudge,
    					PanelName, Seq, PanelGrade, ActionKind);
            }
            else 
            {
            	insertProductImageJudgeForAT(eventInfo,ProductName,ProcessOperationName,MachineName,ImageName,DefectCode,PanelName,Seq,qty);
			}

		}

		return doc;
	}

	public void insertProductImageJudgeForAT(EventInfo eventInfo,String ProductName, String ProcessOperationName, String MachineName, String ImageName,String DefectCode,String PanelName,String Seq,String DefectQuantity)
	{
		String sql = "";

		Map<String, Object> bindMap = new HashMap<String, Object>();
		sql = " INSERT INTO CT_REVIEWPRODUCTIMAGEJUDGE "
				+ " ( PRODUCTNAME, PROCESSOPERATIONNAME, MACHINENAME, IMAGESEQ,  DEFECTCODE, CREATETIME, CREATEUSER, LASTEVENTTIME, LASTEVENTUSER, PANELNAME, SEQ, LASTEVENTTIMEKEY,DEFECTQUANTITY) " 
				+ " VALUES "
				+ " ( :ProductName, :ProcessOperationName, :MachineName, :ImageSeq, :DefectCode, :CreateTime, :CreateUser, :LastEventTime, :LastEventUser, :PanelName,:Seq, :LASTEVENTTIMEKEY,:DefectQuantity ) ";
		bindMap.put("ProductName", ProductName);
		bindMap.put("ProcessOperationName", ProcessOperationName);
		bindMap.put("MachineName", MachineName);
		bindMap.put("ImageSeq", ImageName);
		bindMap.put("DefectCode", DefectCode);
		bindMap.put("CreateTime", eventInfo.getEventTime());
		bindMap.put("CreateUser", eventInfo.getEventUser());
		bindMap.put("LastEventTime", eventInfo.getEventTime());
		bindMap.put("LastEventUser", eventInfo.getEventUser());
		bindMap.put("PanelName", PanelName);
		bindMap.put("Seq", Seq);
		bindMap.put("LASTEVENTTIMEKEY", eventInfo.getEventTimeKey());
		bindMap.put("DefectQuantity", DefectQuantity);
		GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
	}
}
