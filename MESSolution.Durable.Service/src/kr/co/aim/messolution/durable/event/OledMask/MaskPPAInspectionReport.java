package kr.co.aim.messolution.durable.event.OledMask;

import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.collections.OrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskInspection;
import kr.co.aim.messolution.extended.object.management.data.MaskPPAInspection;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class MaskPPAInspectionReport  extends AsyncHandler 
{
	@Override
	@SuppressWarnings("unchecked")
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String maskLotName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String frameName = SMessageUtil.getBodyItemValue(doc, "FRAMENAME", false);
		String recipeID = SMessageUtil.getBodyItemValue(doc, "RECIPEID", false);
		String maskType = SMessageUtil.getBodyItemValue(doc, "MASKTYPE", false);
		String maskStartTime = SMessageUtil.getBodyItemValue(doc, "MASKSTARTTIME", false);
		String maskEndTime = SMessageUtil.getBodyItemValue(doc, "MASKENDTIME", false);
		String maskJudge = SMessageUtil.getBodyItemValue(doc, "MASKJUDGE", false);
		String thickNess = SMessageUtil.getBodyItemValue(doc, "THICKNESS", false);
		String flatNess = SMessageUtil.getBodyItemValue(doc, "FLATNESS", false);
		String maskCycleCount = SMessageUtil.getBodyItemValue(doc, "MASKCYCLECOUNT", false);
		
		String CENTER_PPA_X_JUDGE         =    SMessageUtil.getBodyItemValue(doc,"CENTER_PPA_X_JUDGE",false);
		String CENTER_PPA_X_MAX           =    SMessageUtil.getBodyItemValue(doc,"CENTER_PPA_X_MAX",false);
		String CENTER_PPA_X_MIN           =    SMessageUtil.getBodyItemValue(doc,"CENTER_PPA_X_MIN",false);
		String PPA_X_OK_RATE              =    SMessageUtil.getBodyItemValue(doc,"PPA-X_OK_RATE",false);
		String CENTER_PPA_Y_JUDGE         =    SMessageUtil.getBodyItemValue(doc,"CENTER_PPA_Y_JUDGE",false);
		String CENTER_PPA_Y_MAX           =    SMessageUtil.getBodyItemValue(doc,"CENTER_PPA_Y_MAX",false);
		String CENTER_PPA_Y_MIN           =    SMessageUtil.getBodyItemValue(doc,"CENTER_PPA_Y_MIN",false);
		String PPA_Y_OK_RATE              =    SMessageUtil.getBodyItemValue(doc,"PPA-Y_OK_RATE",false);
		String STRAIGHTNESS_X_MAX         =    SMessageUtil.getBodyItemValue(doc,"STRAIGHTNESS_X_MAX",false);
		String STRAIGHTNESS_Y_MAX         =    SMessageUtil.getBodyItemValue(doc,"STRAIGHTNESS_Y_MAX",false);
		String MASK_CD_X_MAX			  =	   SMessageUtil.getBodyItemValue(doc, "MASK_CD_X_MAX", false);
		String MASK_CD_X_MIN 			  =    SMessageUtil.getBodyItemValue(doc, "MASK_CD_X_MIN", false);
		String MASK_CD_X_CPK 			  =    SMessageUtil.getBodyItemValue(doc, "MASK_CD_X_CPK", false);
		String MASK_CD_Y_MAX 			  =    SMessageUtil.getBodyItemValue(doc, "MASK_CD_Y_MAX", false);
		String MASK_CD_Y_MIN 			  =    SMessageUtil.getBodyItemValue(doc, "MASK_CD_Y_MIN", false);
		String MASK_CD_Y_CPK 			  =    SMessageUtil.getBodyItemValue(doc, "MASK_CD_Y_CPK", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("MaskPPAInspection", this.getEventUser(), this.getEventComment(), null, null);		
		String lastEventTimekey = TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime());
		Timestamp lastEventTime = eventInfo.getEventTime();
		String lastEventUser = eventInfo.getEventUser();
		String lastEventComment =eventInfo.getEventComment();
		String lastEventName = eventInfo.getEventName();

		// Create MaskPPAInspection
		MaskPPAInspection dataInfo = new MaskPPAInspection();
		dataInfo.setMachineName(machineName);
		dataInfo.setUnitName(unitName);
		dataInfo.setSubUnitName(subUnitName);
		dataInfo.setMaskLotName(maskLotName);
		dataInfo.setFrameName(frameName);
		dataInfo.setRecipeID(recipeID);
		dataInfo.setMaskType(maskType);
		dataInfo.setMaskStartTime(maskStartTime);
		dataInfo.setMaskEndTime(maskEndTime);
		dataInfo.setMaskJudge(maskJudge);
		dataInfo.setThickNess(thickNess);
		dataInfo.setFlatNess(flatNess);
		dataInfo.setCENTER_PPA_X_JUDGE(CENTER_PPA_X_JUDGE);
		dataInfo.setCENTER_PPA_X_MAX(CENTER_PPA_X_MAX);
		dataInfo.setCENTER_PPA_X_MIN(CENTER_PPA_X_MIN);
		dataInfo.setPPA_X_OK_RATE(PPA_X_OK_RATE);
		dataInfo.setCENTER_PPA_Y_JUDGE(CENTER_PPA_Y_JUDGE);
		dataInfo.setCENTER_PPA_Y_MAX(CENTER_PPA_Y_MAX);
		dataInfo.setCENTER_PPA_Y_MIN(CENTER_PPA_Y_MIN);
		dataInfo.setPPA_Y_OK_RATE(PPA_Y_OK_RATE);
		dataInfo.setSTRAIGHTNESS_X_MAX(STRAIGHTNESS_X_MAX);
		dataInfo.setSTRAIGHTNESS_Y_MAX(STRAIGHTNESS_Y_MAX);
		dataInfo.setMASK_CD_X_MAX(MASK_CD_X_MAX);
		dataInfo.setMASK_CD_X_MIN(MASK_CD_X_MIN);
		dataInfo.setMASK_CD_X_CPK(MASK_CD_X_CPK);
		dataInfo.setMASK_CD_Y_MAX(MASK_CD_Y_MAX);
		dataInfo.setMASK_CD_Y_MIN(MASK_CD_Y_MIN);
		dataInfo.setMASK_CD_Y_CPK(MASK_CD_Y_CPK);
		dataInfo.setMaskCycleCount(Integer.parseInt(maskCycleCount));
		
		dataInfo.setLastEventComment(lastEventComment);
		dataInfo.setLastEventName(lastEventName);
		dataInfo.setLastEventUser(lastEventUser);
		dataInfo.setLastEventTimeKey(lastEventTimekey);
		dataInfo.setLastEventTime(lastEventTime);
		
		boolean isNew = false;
		
		String sql = "SELECT MASKLOTNAME FROM CT_MASKPPAINSPECTION WHERE MASKLOTNAME = ? ";
		List<OrderedMap> resultDataList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, new Object[] { maskLotName });
		if (resultDataList == null || resultDataList.size() == 0)
		{
			isNew = true;
		}
		
		try
		{
			if (isNew)
			{
				ExtendedObjectProxy.getMaskPPAInspectionService().create(eventInfo, dataInfo);
			}
			else
			{
				ExtendedObjectProxy.getMaskPPAInspectionService().modify(eventInfo, dataInfo);	
			}
		}
		catch (DuplicateNameSignal dns)
		{
			throw new CustomException("MASK-0028", maskLotName);
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}
	}
}
