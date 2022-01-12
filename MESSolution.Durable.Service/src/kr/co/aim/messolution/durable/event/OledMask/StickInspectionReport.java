package kr.co.aim.messolution.durable.event.OledMask;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskStick;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class StickInspectionReport extends AsyncHandler {

	private static Log log = LogFactory.getLog(OLEDMaskProcessStarted.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String stickName = SMessageUtil.getBodyItemValue(doc, "STICKNAME", true);
		String stickJudge = SMessageUtil.getBodyItemValue(doc, "STICKJUDGE", false);
		String stickGrade = SMessageUtil.getBodyItemValue(doc, "STICKGRADE", false);
		String recipeId = SMessageUtil.getBodyItemValue(doc, "RECIPEID", false);
		String type = SMessageUtil.getBodyItemValue(doc, "TYPE", false);
		String TP_X = SMessageUtil.getBodyItemValue(doc, "TP_X", false);
		String TP_X_JUDGE = SMessageUtil.getBodyItemValue(doc, "TP_X_JUDGE", false);
		String TP_Y = SMessageUtil.getBodyItemValue(doc, "TP_Y", false);
		String TP_Y_JUDGE = SMessageUtil.getBodyItemValue(doc, "TP_Y_JUDGE", false);
		String STRIGHTNESS = SMessageUtil.getBodyItemValue(doc, "STRIGHTNESS", false);
		String SHARP = SMessageUtil.getBodyItemValue(doc, "SHARP", false);
		String DEFECT_NO = SMessageUtil.getBodyItemValue(doc, "DEFECT_NO", false);
		String CD_X_MAX = SMessageUtil.getBodyItemValue(doc, "CD_X_MAX", false);
		String CD_X_MIN = SMessageUtil.getBodyItemValue(doc, "CD_X_MIN", false);
		String CD_X_AVE = SMessageUtil.getBodyItemValue(doc, "CD_X_AVE", false);
		String CD_X_CPK = SMessageUtil.getBodyItemValue(doc, "CD_X_CPK", false);
		String CD_Y_MAX = SMessageUtil.getBodyItemValue(doc, "CD_Y_MAX", false);
		String CD_Y_MIN = SMessageUtil.getBodyItemValue(doc, "CD_Y_MIN", false);
		String CD_Y_AVE = SMessageUtil.getBodyItemValue(doc, "CD_Y_AVE", false);
		String CD_Y_CPK = SMessageUtil.getBodyItemValue(doc, "CD_Y_CPK", false);

		String TAPER_ANGLE_3D = SMessageUtil.getBodyItemValue(doc, "TAPER_ANGLE_3D", false);
		String RIB_THICKNESS_3D = SMessageUtil.getBodyItemValue(doc, "RIB_THICKNESS_3D", false);
		String STEP_HIGHT_3D = SMessageUtil.getBodyItemValue(doc, "STEP_HIGHT_3D", false);
		String STEP_WIDTH_3D = SMessageUtil.getBodyItemValue(doc, "STEP_WIDTH_3D", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("StickInspectionReport", getEventUser(), getEventComment(), "", "");
		ConstantMap constMap = GenericServiceProxy.getConstantMap();

		MaskStick maskStickData = ExtendedObjectProxy.getMaskStickService().getMaskStickData(stickName);
		if(maskStickData!=null&&maskStickData.getStickState().equals("NotAvailable"))
		{
			// maskStickData.setMachineName(machineName);
			maskStickData.setStickJudge(stickJudge);
			maskStickData.setStickGrade(stickGrade);
			maskStickData.setRecipeID(recipeId);
			// maskStickData.setType(type);
			maskStickData.setTP_X(TP_X);
			maskStickData.setTP_X_JUDGE(TP_X_JUDGE);
			maskStickData.setTP_Y(TP_Y);
			maskStickData.setTP_Y_JUDGE(TP_Y_JUDGE);
			maskStickData.setStrightness(STRIGHTNESS);
			maskStickData.setSharp(SHARP);
			maskStickData.setDefect_No(DEFECT_NO);
			maskStickData.setCD_X_MAX(CD_X_MAX);
			maskStickData.setCD_X_MIN(CD_X_MIN);
			maskStickData.setCD_X_AVE(CD_X_AVE);
			maskStickData.setCD_X_CPK(CD_X_CPK);
			maskStickData.setCD_Y_MAX(CD_Y_MAX);
			maskStickData.setCD_Y_MIN(CD_Y_MIN);
			maskStickData.setCD_Y_AVE(CD_Y_AVE);
			maskStickData.setCD_Y_CPK(CD_Y_CPK);
			maskStickData.setTAPER_ANGLE_3D(TAPER_ANGLE_3D);
			maskStickData.setRIB_THICKNESS_3D(RIB_THICKNESS_3D);
			maskStickData.setSTEP_HIGHT_3D(STEP_HIGHT_3D);
			maskStickData.setSTEP_WIDTH_3D(STEP_WIDTH_3D);
			
			//houxk add Start
			maskStickData.setUseTime(eventInfo.getEventTime());
			maskStickData.setLastEventComment(eventInfo.getEventComment());
			maskStickData.setLastEventName(eventInfo.getEventName());
			maskStickData.setLastEventTime(eventInfo.getEventTime());
			maskStickData.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
			maskStickData.setLastEventUser(eventInfo.getEventUser());
			//houxk add End

			String judge = "";

			if (StringUtils.equals(stickJudge, constMap.Stick_Judge_G))
				judge = "OK";
			else if (StringUtils.equals(stickJudge, constMap.Stick_Judge_N))
				judge = "NG";

			maskStickData.setBankType(judge);

			if (maskStickData.getStickType().equals(constMap.Stick_Type_F))
				maskStickData.setStickState(constMap.Stick_State_Available);

			ExtendedObjectProxy.getMaskStickService().modify(eventInfo, maskStickData);
		}
		else
		{
			ExtendedObjectProxy.getMaskStickService().modify(eventInfo, maskStickData);
		}
		
	}

}
