package kr.co.aim.messolution.durable.event.CNX;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskStick;
import kr.co.aim.messolution.extended.object.management.impl.MaskStickService;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class CreateStick extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		List<Element> StickList = SMessageUtil.getBodySequenceItemList(doc, "STICKLIST", true);

		for (Element Stick : StickList)
		{
			String stickName = SMessageUtil.getChildText(Stick, "STICKNAME", true);
			String stickSpecName = SMessageUtil.getChildText(Stick, "STICKSPECNAME", true);
			String type = SMessageUtil.getChildText(Stick, "TYPE", true);
			String stickFilmLayer = SMessageUtil.getChildText(Stick, "STICKFILMLAYER", true);
			String bankType = SMessageUtil.getChildText(Stick, "BANKTYPE", false);
			String vendorName = SMessageUtil.getChildText(Stick, "VENDORNAME", true);
			String recipeID = SMessageUtil.getChildText(Stick, "RECIPEID", false);
			String TP_X = SMessageUtil.getChildText(Stick, "TP_X", false);
			String TP_X_Judge = SMessageUtil.getChildText(Stick, "TP_X_JUDGE", false);
			String TP_Y = SMessageUtil.getChildText(Stick, "TP_Y", false);
			String TP_Y_JUDGE = SMessageUtil.getChildText(Stick, "TP_Y_JUDGE", false);
			String strightness = SMessageUtil.getChildText(Stick, "STRIGHTNESS", false);
			String sharp = SMessageUtil.getChildText(Stick, "SHARP", false);
			String defect_No = SMessageUtil.getChildText(Stick, "DEFECT_NO", false);
			String CD_X_MAX = SMessageUtil.getChildText(Stick, "CD_X_MAX", false);
			String CD_X_MIN = SMessageUtil.getChildText(Stick, "CD_X_MIN", false);
			String CD_X_AVE = SMessageUtil.getChildText(Stick, "CD_X_AVE", false);
			String CD_X_CPK = SMessageUtil.getChildText(Stick, "CD_X_CPK", false);
			String CD_Y_MAX = SMessageUtil.getChildText(Stick, "CD_Y_MAX", false);
			String CD_Y_MIN = SMessageUtil.getChildText(Stick, "CD_Y_MIN", false);
			String CD_Y_AVE = SMessageUtil.getChildText(Stick, "CD_Y_AVE", false);
			String CD_Y_CPK = SMessageUtil.getChildText(Stick, "CD_Y_CPK", false);
			String receiveTime = SMessageUtil.getChildText(Stick, "RECEIVETIME", true);
			String TAPER_ANGLE_3D = SMessageUtil.getChildText(Stick, "TAPER_ANGLE_3D", false);
			String RIB_THICKNESS_3D = SMessageUtil.getChildText(Stick, "RIB_THICKNESS_3D", false);
			String STEP_HIGHT_3D = SMessageUtil.getChildText(Stick, "STEP_HIGHT_3D", false);
			String STEP_WIDTH_3D = SMessageUtil.getChildText(Stick, "STEP_WIDTH_3D", false);
			
			//houxk add Start
			String texture = SMessageUtil.getChildText(Stick, "TEXTURE", true);
			String length = SMessageUtil.getChildText(Stick, "LENGTH", true);
			String width = SMessageUtil.getChildText(Stick, "WIDTH", true);
			String thickness = SMessageUtil.getChildText(Stick, "THICKNESS", true);
			String COA = SMessageUtil.getChildText(Stick, "COA", false);
			String CPA = SMessageUtil.getChildText(Stick, "CPA", false);
			String position1X = SMessageUtil.getChildText(Stick, "POSITION1X", false);
			String position1Y = SMessageUtil.getChildText(Stick, "POSITION1Y", false);
			String position2X = SMessageUtil.getChildText(Stick, "POSITION2X", false);
			String position2Y = SMessageUtil.getChildText(Stick, "POSITION2Y", false);
			String alignPositionVal = SMessageUtil.getChildText(Stick, "ALIGNPOSITIONVAL", false);
			String alignHoleRn = SMessageUtil.getChildText(Stick, "ALIGNHOLERN", false);
			String CD_X_COA_MAX = SMessageUtil.getChildText(Stick, "CD_X_COA_MAX", false);
			String CD_X_COA_MIN = SMessageUtil.getChildText(Stick, "CD_X_COA_MIN", false);
			String CD_X_COA_AVE = SMessageUtil.getChildText(Stick, "CD_X_COA_AVE", false);
			String CD_X_COA_CPK = SMessageUtil.getChildText(Stick, "CD_X_COA_CPK", false);
			String CD_Y_COA_MAX = SMessageUtil.getChildText(Stick, "CD_Y_COA_MAX", false);
			String CD_Y_COA_MIN = SMessageUtil.getChildText(Stick, "CD_Y_COA_MIN", false);
			String CD_Y_COA_AVE = SMessageUtil.getChildText(Stick, "CD_Y_COA_AVE", false);
			String CD_Y_COA_CPK = SMessageUtil.getChildText(Stick, "CD_Y_COA_CPK", false);
			String TP_X_COA = SMessageUtil.getChildText(Stick, "TP_X_COA", false);
			String TP_Y_COA = SMessageUtil.getChildText(Stick, "TP_Y_COA", false);			
			//String measurementTime = SMessageUtil.getChildText(Stick, "MEASUREMENTTIME", false);
			//houxk End
			
			StringBuffer inquirysql = new StringBuffer();
			inquirysql.append("SELECT STICKNAME ");
			inquirysql.append("  FROM CT_MASKSTICK ");
			inquirysql.append(" WHERE STICKNAME = :STICKNAME ");

			Map<String, String> inquirybindMap = new HashMap<String, String>();
			inquirybindMap.put("STICKNAME", stickName);
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(inquirysql.toString(), inquirybindMap);

			if (sqlResult != null && sqlResult.size() > 0)
			{
				StringBuilder maskStickNames = new StringBuilder();
				for (Map<String, Object> searchedStickName : sqlResult)
				{
					maskStickNames.append(CommonUtil.getValue(searchedStickName, "STICKNAME"));
					maskStickNames.append(' ');
				}
				throw new CustomException("MASK-0052", maskStickNames);
			}
			
			

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateStick", this.getEventUser(), this.getEventComment(), "", "");
			eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());
			MaskStick dataInfo = new MaskStick();
			dataInfo.setStickName(stickName);
			dataInfo.setStickSpecName(stickSpecName);
			dataInfo.setType(type);
			dataInfo.setStickType(stickName.substring(3,4));
			dataInfo.setStickFilmLayer(stickFilmLayer);
			dataInfo.setBankType(bankType);
			dataInfo.setVendorName(vendorName);
			dataInfo.setRecipeID(recipeID);
			dataInfo.setTP_X(TP_X);
			dataInfo.setTP_X_JUDGE(TP_X_Judge);
			dataInfo.setTP_Y(TP_Y);
			dataInfo.setTP_Y_JUDGE(TP_Y_JUDGE);
			dataInfo.setStrightness(strightness);
			dataInfo.setSharp(sharp);
			dataInfo.setDefect_No(defect_No);
			dataInfo.setCD_X_MAX(CD_X_MAX);
			dataInfo.setCD_X_MIN(CD_X_MIN);
			dataInfo.setCD_X_AVE(CD_X_AVE);
			dataInfo.setCD_X_CPK(CD_X_CPK);
			dataInfo.setCD_Y_MAX(CD_Y_MAX);
			dataInfo.setCD_Y_MIN(CD_Y_MIN);
			dataInfo.setCD_Y_AVE(CD_Y_AVE);
			dataInfo.setCD_Y_CPK(CD_Y_CPK);
			dataInfo.setStickJudge("G");
			dataInfo.setTAPER_ANGLE_3D(TAPER_ANGLE_3D);
			dataInfo.setRIB_THICKNESS_3D(RIB_THICKNESS_3D);
			dataInfo.setSTEP_HIGHT_3D(STEP_HIGHT_3D);
			dataInfo.setSTEP_WIDTH_3D(STEP_WIDTH_3D);
			
			//houxk add Start
			dataInfo.setTexture(texture);
			dataInfo.setLength(length);
			dataInfo.setWidth(width);
			dataInfo.setThickness(thickness);
			dataInfo.setCOA(COA);
			dataInfo.setCPA(CPA);
			dataInfo.setPosition1X(position1X);
			dataInfo.setPosition1Y(position1Y);
			dataInfo.setPosition2X(position2X);
			dataInfo.setPosition2Y(position2Y);
			dataInfo.setAlignPositionVal(alignPositionVal);
			dataInfo.setAlignHoleRn(alignHoleRn);
			dataInfo.setCD_X_COA_MAX(CD_X_COA_MAX);
			dataInfo.setCD_X_COA_MIN(CD_X_COA_MIN);
			dataInfo.setCD_X_COA_AVE(CD_X_COA_AVE);
			dataInfo.setCD_X_COA_CPK(CD_X_COA_CPK);
			dataInfo.setCD_Y_COA_MAX(CD_Y_COA_MAX);
			dataInfo.setCD_Y_COA_MIN(CD_Y_COA_MIN);
			dataInfo.setCD_Y_COA_AVE(CD_Y_COA_AVE);
			dataInfo.setCD_Y_COA_CPK(CD_Y_COA_CPK);	
			dataInfo.setTP_X_COA(TP_X_COA);	
			dataInfo.setTP_Y_COA(TP_X_COA);	
			dataInfo.setCreateTime(eventInfo.getEventTime());
			//houxk End
			
			receiveTime = receiveTime.replace("_", "");
			Timestamp dueDate = TimeUtils.getTimestamp(receiveTime);
			dataInfo.setReceiveTime(dueDate);
			//dueDate = TimeUtils.getTimestamp(measurementTime);
			dataInfo.setCreateUser(eventInfo.getEventUser());
			//dataInfo.setMeasurementTime(dueDate);
			if(StringUtil.equals(type, "FMM"))
			{
				dataInfo.setStickState(constantMap.Dur_NotAvailable);
			}
			else
			{
				dataInfo.setStickState(constantMap.Dur_Available);
			}
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventTimeKey(eventInfo.getLastEventTimekey());
			dataInfo.setLastEventUser(eventInfo.getEventUser());

			MaskStick newMaskStick= ExtendedObjectProxy.getMaskStickService().create(eventInfo, dataInfo);
		}

		
		return doc;
	}
	

}
