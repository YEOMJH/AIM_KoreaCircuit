package kr.co.aim.messolution.durable.event;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskInspection;
import kr.co.aim.messolution.extended.object.management.data.MaterialProduct;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.product.management.data.Product;

public class MaskCNCInspectionReport  extends AsyncHandler {
	private static Log log = LogFactory.getLog(MaskInspection.class);

	public void doWorks(Document doc) throws CustomException
	{
		String messageName = SMessageUtil.getMessageName(doc);	
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
		String ALIGN_HOLE_DESIGN_POSITIONX1 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_DESIGN_POSITIONX1", false);
		String ALIGN_HOLE_DESIGN_POSITIONX2 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_DESIGN_POSITIONX2", false);
		String ALIGN_HOLE_DESIGN_POSITIONX3 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_DESIGN_POSITIONX3", false);
		String ALIGN_HOLE_DESIGN_POSITIONX4 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_DESIGN_POSITIONX4", false);
		String ALIGN_HOLE_DESIGN_POSITIONY1 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_DESIGN_POSITIONY1", false);
		String ALIGN_HOLE_DESIGN_POSITIONY2 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_DESIGN_POSITIONY2", false);
		String ALIGN_HOLE_DESIGN_POSITIONY3 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_DESIGN_POSITIONY3", false);
		String ALIGN_HOLE_DESIGN_POSITIONY4 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_DESIGN_POSITIONY4", false);
		String ALIGN_HOLE_MEASURE_POSITIONX1 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_MEASURE_POSITIONX1", false);
		String ALIGN_HOLE_MEASURE_POSITIONX2 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_MEASURE_POSITIONX2", false);
		String ALIGN_HOLE_MEASURE_POSITIONX3 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_MEASURE_POSITIONX3", false);
		String ALIGN_HOLE_MEASURE_POSITIONX4 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_MEASURE_POSITIONX4", false);
		String ALIGN_HOLE_MEASURE_POSITIONY1 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_MEASURE_POSITIONY1", false);
		String ALIGN_HOLE_MEASURE_POSITIONY2 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_MEASURE_POSITIONY2", false);
		String ALIGN_HOLE_MEASURE_POSITIONY3 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_MEASURE_POSITIONY3", false);
		String ALIGN_HOLE_MEASURE_POSITIONY4 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_MEASURE_POSITIONY4", false);
		String ALIGN_HOLE_DESIGN_SIZE1 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_DESIGN_SIZE1", false);
		String ALIGN_HOLE_DESIGN_SIZE2 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_DESIGN_SIZE2", false);
		String ALIGN_HOLE_DESIGN_SIZE3 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_DESIGN_SIZE3", false);
		String ALIGN_HOLE_DESIGN_SIZE4 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_DESIGN_SIZE4", false);
		String ALIGN_HOLE_MEASURE_SIZE1 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_MEASURE_SIZE1", false);
		String ALIGN_HOLE_MEASURE_SIZE2 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_MEASURE_SIZE2", false);
		String ALIGN_HOLE_MEASURE_SIZE3 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_MEASURE_SIZE3", false);
		String ALIGN_HOLE_MEASURE_SIZE4 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_MEASURE_SIZE4", false);
		String Mask_CD_X_max = SMessageUtil.getBodyItemValue(doc, "MASK_CD_X_MAX", false);
		String Mask_CD_X_min = SMessageUtil.getBodyItemValue(doc, "MASK_CD_X_MIN", false);
		String Mask_CD_X_CPK = SMessageUtil.getBodyItemValue(doc, "MASK_CD_X_CPK", false);
		String Mask_CD_Y_max = SMessageUtil.getBodyItemValue(doc, "MASK_CD_Y_MAX", false);
		String Mask_CD_Y_min = SMessageUtil.getBodyItemValue(doc, "MASK_CD_Y_MIN", false);
		String Mask_CD_Y_CPK = SMessageUtil.getBodyItemValue(doc, "MASK_CD_Y_CPK", false);
		String PPA_X_JUDGE = SMessageUtil.getBodyItemValue(doc, "PPA_X_JUDGE", false);
		String PPA_X_MAX = SMessageUtil.getBodyItemValue(doc, "PPA_X_MAX", false);
		String PPA_X_MIN = SMessageUtil.getBodyItemValue(doc, "PPA_X_MIN", false);
		String PPA_X_OK_RATE = SMessageUtil.getBodyItemValue(doc, "PPA_X_OK_RATE", false);
		String PPA_Y_JUDGE = SMessageUtil.getBodyItemValue(doc, "PPA_Y_JUDGE", false);
		String PPA_Y_MAX = SMessageUtil.getBodyItemValue(doc, "PPA_Y_MAX", false);
		String PPA_Y_MIN = SMessageUtil.getBodyItemValue(doc, "PPA_Y_MIN", false);
		String PPA_Y_OK_RATE = SMessageUtil.getBodyItemValue(doc, "PPA_Y_OK_RATE", false);
		String flatNess = SMessageUtil.getBodyItemValue(doc, "FLATNESS", false);
		String STRAIGHTNESS_X = SMessageUtil.getBodyItemValue(doc, "STRAIGHTNESS_X", false);
		String STRAIGHTNESS_Y = SMessageUtil.getBodyItemValue(doc, "STRAIGHTNESS_Y", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo(messageName, this.getEventUser(), this.getEventComment(), null, null);		
		String lastEventTimekey = TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime());
		Timestamp lastEventTime = eventInfo.getEventTime();
		String lastEventUser = eventInfo.getEventUser();
		String lastEventComment =eventInfo.getEventComment();
		String lastEventName = eventInfo.getEventName();

		
	

		// Create MaskInspection
		MaskInspection dataInfo = new MaskInspection();
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
		dataInfo.setALIGN_HOLE_DESIGN_POSITIONX1(ALIGN_HOLE_DESIGN_POSITIONX1);
		dataInfo.setALIGN_HOLE_DESIGN_POSITIONX2(ALIGN_HOLE_DESIGN_POSITIONX2);
		dataInfo.setALIGN_HOLE_DESIGN_POSITIONX3(ALIGN_HOLE_DESIGN_POSITIONX3);
		dataInfo.setALIGN_HOLE_DESIGN_POSITIONX4(ALIGN_HOLE_DESIGN_POSITIONX4);
		dataInfo.setALIGN_HOLE_DESIGN_POSITIONY1(ALIGN_HOLE_DESIGN_POSITIONY1);
		dataInfo.setALIGN_HOLE_DESIGN_POSITIONY2(ALIGN_HOLE_DESIGN_POSITIONY2);
		dataInfo.setALIGN_HOLE_DESIGN_POSITIONY3(ALIGN_HOLE_DESIGN_POSITIONY3);
		dataInfo.setALIGN_HOLE_DESIGN_POSITIONY4(ALIGN_HOLE_DESIGN_POSITIONY4);
		dataInfo.setALIGN_HOLE_MEASURE_POSITIONX1(ALIGN_HOLE_MEASURE_POSITIONX1);
		dataInfo.setALIGN_HOLE_MEASURE_POSITIONX2(ALIGN_HOLE_MEASURE_POSITIONX2);
		dataInfo.setALIGN_HOLE_MEASURE_POSITIONX3(ALIGN_HOLE_MEASURE_POSITIONX3);
		dataInfo.setALIGN_HOLE_MEASURE_POSITIONX4(ALIGN_HOLE_MEASURE_POSITIONX4);
		dataInfo.setALIGN_HOLE_MEASURE_POSITIONY1(ALIGN_HOLE_MEASURE_POSITIONY1);
		dataInfo.setALIGN_HOLE_MEASURE_POSITIONY2(ALIGN_HOLE_MEASURE_POSITIONY2);
		dataInfo.setALIGN_HOLE_MEASURE_POSITIONY3(ALIGN_HOLE_MEASURE_POSITIONY3);
		dataInfo.setALIGN_HOLE_MEASURE_POSITIONY4(ALIGN_HOLE_MEASURE_POSITIONY4);
		dataInfo.setALIGN_HOLE_DESIGN_SIZE1(ALIGN_HOLE_DESIGN_SIZE1);
		dataInfo.setALIGN_HOLE_DESIGN_SIZE2(ALIGN_HOLE_DESIGN_SIZE2);
		dataInfo.setALIGN_HOLE_DESIGN_SIZE3(ALIGN_HOLE_DESIGN_SIZE3);
		dataInfo.setALIGN_HOLE_DESIGN_SIZE4(ALIGN_HOLE_DESIGN_SIZE4);
		dataInfo.setALIGN_HOLE_MEASURE_SIZE1(ALIGN_HOLE_MEASURE_SIZE1);
		dataInfo.setALIGN_HOLE_MEASURE_SIZE2(ALIGN_HOLE_MEASURE_SIZE2);
		dataInfo.setALIGN_HOLE_MEASURE_SIZE3(ALIGN_HOLE_MEASURE_SIZE3);
		dataInfo.setALIGN_HOLE_MEASURE_SIZE4(ALIGN_HOLE_MEASURE_SIZE4);
		dataInfo.setMask_CD_X_max(Mask_CD_X_max);
		dataInfo.setMask_CD_X_min(Mask_CD_X_min);
		dataInfo.setMask_CD_X_CPK(Mask_CD_X_CPK);
		dataInfo.setMask_CD_Y_max(Mask_CD_Y_max);
		dataInfo.setMask_CD_Y_min(Mask_CD_Y_min);
		dataInfo.setMask_CD_Y_CPK(Mask_CD_Y_CPK);
		dataInfo.setPPA_X_JUDGE(PPA_X_JUDGE);
		dataInfo.setPPA_X_MAX(PPA_X_MAX);
		dataInfo.setPPA_X_MIN(PPA_X_MIN);
		dataInfo.setPPA_X_OK_RATE(PPA_X_OK_RATE);
		dataInfo.setPPA_Y_JUDGE(PPA_Y_JUDGE);
		dataInfo.setPPA_Y_MAX(PPA_Y_MAX);
		dataInfo.setPPA_Y_MIN(PPA_Y_MIN);
		dataInfo.setPPA_Y_OK_RATE(PPA_Y_OK_RATE);
		dataInfo.setFlatNess(flatNess);
		dataInfo.setSTRAIGHTNESS_X(STRAIGHTNESS_X);
		dataInfo.setSTRAIGHTNESS_Y(STRAIGHTNESS_Y);
		dataInfo.setLastEventComment(lastEventComment);
		dataInfo.setLastEventName(lastEventName);
		dataInfo.setLastEventUser(lastEventUser);
		dataInfo.setLastEventTimeKey(lastEventTimekey);
		dataInfo.setLastEventTime(lastEventTime);
		
		// Check Duplicate Data 
		List<Map<String, Object>> checkDataResult = new ArrayList<Map<String, Object>>();	
		Map<String, Object> checkDatabindMap = new HashMap<String, Object>();
		StringBuilder checkDataSql = new StringBuilder();
   
		checkDataSql.append("SELECT MASKLOTNAME ");
		checkDataSql.append("  FROM CT_MASKINSPECTION ");
		checkDataSql.append(" WHERE MASKLOTNAME = :MASKLOTNAME ");

	       
		checkDatabindMap = new HashMap<String, Object>();	
		
		checkDatabindMap.put("MASKLOTNAME", dataInfo.getMaskLotName().toString());	
	
		checkDataResult = GenericServiceProxy.getSqlMesTemplate().queryForList(checkDataSql.toString(), checkDatabindMap);
		
		//Insert MaskInspection Data
		if(checkDataResult.size()==0 || checkDataResult.isEmpty()==true){
			
			ExtendedObjectProxy.getMaskInspectionService().insert(dataInfo);
			
		}else{
			throw new CustomException("MASK-0028", maskLotName);
		}
	
		
		


	}
}
