package kr.co.aim.messolution.durable.event.CNX;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskStick;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class ChangeSheet extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String stickName = SMessageUtil.getBodyItemValue(doc, "STICKNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String recipeID = SMessageUtil.getBodyItemValue(doc, "RECIPEID", false);
		String stickJudge = SMessageUtil.getBodyItemValue(doc, "STICKJUDGE", false);
		String stickGrade = SMessageUtil.getBodyItemValue(doc, "STICKGRADE", false);
		String TPX = SMessageUtil.getBodyItemValue(doc, "TP_X", false);
		String TPXJUDGE = SMessageUtil.getBodyItemValue(doc, "TP_X_JUDGE", false);
		String TPY = SMessageUtil.getBodyItemValue(doc, "TP_Y", false);
		String TPYJUDGE = SMessageUtil.getBodyItemValue(doc, "TP_Y_JUDGE", false);
		String strightness = SMessageUtil.getBodyItemValue(doc, "STRIGHTNESS", false);
		String sharp = SMessageUtil.getBodyItemValue(doc, "SHARP", false);
		String defect_No = SMessageUtil.getBodyItemValue(doc, "DEFECT_NO", false);
		String bankType = SMessageUtil.getBodyItemValue(doc, "BANKTYPE", false);
		String CDXMAX = SMessageUtil.getBodyItemValue(doc, "CD_X_MAX", false);
		String CDXMIN = SMessageUtil.getBodyItemValue(doc, "CD_X_MIN", false);
		String CDXAVE = SMessageUtil.getBodyItemValue(doc, "CD_X_AVE", false);
		String CDXCPK = SMessageUtil.getBodyItemValue(doc, "CD_X_CPK", false);
		String CDYMAX = SMessageUtil.getBodyItemValue(doc, "CD_Y_MAX", false);
		String CDYMIN = SMessageUtil.getBodyItemValue(doc, "CD_Y_MIN", false);
		String CDYAVE = SMessageUtil.getBodyItemValue(doc, "CD_Y_AVE", false);
		String CDYCPK = SMessageUtil.getBodyItemValue(doc, "CD_Y_CPK", false);
		String sReceiveTime = SMessageUtil.getBodyItemValue(doc, "RECEIVETIME", true);
		//String sMeasurementTime = SMessageUtil.getBodyItemValue(doc, "MEASUREMENTTIME", false);
		String TAPER_ANGLE_3D = SMessageUtil.getBodyItemValue(doc, "TAPER_ANGLE_3D", false);
		String RIB_THICKNESS_3D = SMessageUtil.getBodyItemValue(doc, "RIB_THICKNESS_3D", false);
		String STEP_HIGHT_3D = SMessageUtil.getBodyItemValue(doc, "STEP_HIGHT_3D", false);
		String STEP_WIDTH_3D = SMessageUtil.getBodyItemValue(doc, "STEP_WIDTH_3D", false);

		//houxk add Start
		String texture = SMessageUtil.getBodyItemValue(doc, "TEXTURE", true);
		String length = SMessageUtil.getBodyItemValue(doc, "LENGTH", true);
		String width = SMessageUtil.getBodyItemValue(doc, "WIDTH", true);
		String thickness = SMessageUtil.getBodyItemValue(doc, "THICKNESS", true);
		String COA = SMessageUtil.getBodyItemValue(doc, "COA", false);
		String CPA = SMessageUtil.getBodyItemValue(doc, "CPA", false);
		String position1X = SMessageUtil.getBodyItemValue(doc, "POSITION1X", false);
		String position1Y = SMessageUtil.getBodyItemValue(doc, "POSITION1Y", false);
		String position2X = SMessageUtil.getBodyItemValue(doc, "POSITION2X", false);
		String position2Y = SMessageUtil.getBodyItemValue(doc, "POSITION2Y", false);
		String alignPositionVal = SMessageUtil.getBodyItemValue(doc, "ALIGNPOSITIONVAL", false);
		String alignHoleRn = SMessageUtil.getBodyItemValue(doc, "ALIGNHOLERN", false);
		String CD_X_COA_MAX = SMessageUtil.getBodyItemValue(doc, "CD_X_COA_MAX", false);
		String CD_X_COA_MIN = SMessageUtil.getBodyItemValue(doc, "CD_X_COA_MIN", false);
		String CD_X_COA_AVE = SMessageUtil.getBodyItemValue(doc, "CD_X_COA_AVE", false);
		String CD_X_COA_CPK = SMessageUtil.getBodyItemValue(doc, "CD_X_COA_CPK", false);
		String CD_Y_COA_MAX = SMessageUtil.getBodyItemValue(doc, "CD_Y_COA_MAX", false);
		String CD_Y_COA_MIN = SMessageUtil.getBodyItemValue(doc, "CD_Y_COA_MIN", false);
		String CD_Y_COA_AVE = SMessageUtil.getBodyItemValue(doc, "CD_Y_COA_AVE", false);
		String CD_Y_COA_CPK = SMessageUtil.getBodyItemValue(doc, "CD_Y_COA_CPK", false);
		String TP_X_COA = SMessageUtil.getBodyItemValue(doc, "TP_X_COA", false);
		String TP_Y_COA = SMessageUtil.getBodyItemValue(doc, "TP_Y_COA", false);
		//houxk End
		
		Date date = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		try
		{
			date = dateFormat.parse(sReceiveTime);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		Timestamp receiveTime = new Timestamp(date.getTime());

		try
		{
			//date = dateFormat.parse(sMeasurementTime);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		//Timestamp measurementTime = new Timestamp(date.getTime());

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeStick", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		MaskStick stickData = ExtendedObjectProxy.getMaskStickService().selectByKey(false, new Object[] { stickName });
		String stickState=stickData.getStickState();
		if(stickState.equals("InUse")||stickState.equals("Scrapped"))
		{
			throw new CustomException("MASK-0075");
		}
		
		
		if(StringUtil.isNotEmpty(machineName))
		{
			stickData.setMachineName(machineName);
		}
		if(StringUtil.isNotEmpty(recipeID))
		{
			stickData.setRecipeID(recipeID);
		}
		
		stickData.setStickJudge(stickJudge);
		stickData.setStickGrade(stickGrade);
		
		stickData.setTP_X(TPX);
		stickData.setTP_X_JUDGE(TPXJUDGE);
		stickData.setTP_Y(TPY);
		stickData.setTP_Y_JUDGE(TPYJUDGE);
		stickData.setStrightness(strightness);
		stickData.setSharp(sharp);
		stickData.setDefect_No(defect_No);
		stickData.setBankType(bankType);
		
		stickData.setCD_X_MAX(CDXMAX);
		stickData.setCD_X_MIN(CDXMIN);
		stickData.setCD_X_AVE(CDXAVE);
		stickData.setCD_X_CPK(CDXCPK);
		
		stickData.setCD_Y_MAX(CDYMAX);
		stickData.setCD_Y_MIN(CDYMIN);
		stickData.setCD_Y_AVE(CDYAVE);
		stickData.setCD_Y_CPK(CDYCPK);
		stickData.setReceiveTime(receiveTime);
		//stickData.setMeasurementTime(measurementTime);

		stickData.setTAPER_ANGLE_3D(TAPER_ANGLE_3D);
		stickData.setRIB_THICKNESS_3D(RIB_THICKNESS_3D);
		stickData.setSTEP_HIGHT_3D(STEP_HIGHT_3D);
		stickData.setSTEP_WIDTH_3D(STEP_WIDTH_3D);

		//houxk add Start
		stickData.setTexture(texture);
		stickData.setLength(length);
		stickData.setWidth(width);
		stickData.setThickness(thickness);
		stickData.setCOA(COA);
		stickData.setCPA(CPA);
		stickData.setPosition1X(position1X);
		stickData.setPosition1Y(position1Y);
		stickData.setPosition2X(position2X);
		stickData.setPosition2Y(position2Y);
		stickData.setAlignPositionVal(alignPositionVal);
		stickData.setAlignHoleRn(alignHoleRn);
		stickData.setCD_X_COA_MAX(CD_X_COA_MAX);
		stickData.setCD_X_COA_MIN(CD_X_COA_MIN);
		stickData.setCD_X_COA_AVE(CD_X_COA_AVE);
		stickData.setCD_X_COA_CPK(CD_X_COA_CPK);
		stickData.setCD_Y_COA_MAX(CD_Y_COA_MAX);
		stickData.setCD_Y_COA_MIN(CD_Y_COA_MIN);
		stickData.setCD_Y_COA_AVE(CD_Y_COA_AVE);
		stickData.setCD_Y_COA_CPK(CD_Y_COA_CPK);
		stickData.setTP_X_COA(TP_X_COA);
		stickData.setTP_Y_COA(TP_Y_COA);
		stickData.setLastEventComment(eventInfo.getEventComment());
		stickData.setLastEventName(eventInfo.getEventName());
		stickData.setLastEventTime(eventInfo.getEventTime());
		stickData.setLastEventTimeKey(eventInfo.getLastEventTimekey());
		stickData.setLastEventUser(eventInfo.getEventUser());
		//houxk End
		
		ExtendedObjectProxy.getMaskStickService().modify(eventInfo, stickData);

		return doc;
	}

}
