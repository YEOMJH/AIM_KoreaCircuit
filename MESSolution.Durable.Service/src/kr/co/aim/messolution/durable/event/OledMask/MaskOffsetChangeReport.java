package kr.co.aim.messolution.durable.event.OledMask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

/*
 * BC -> MES
 * 
 * MACHINENAME
 * UNITNAME
 * SUBUNITNAME
 * MASKNAME
 * MODE
 * RECIPEID
 * OFFSET_X
 * OFFSET_Y
 * OFFSET_T
 * RESULT
 */
 
public class MaskOffsetChangeReport extends AsyncHandler 
{
	Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public void doWorks(Document doc) throws CustomException 
	{
		String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String OFFSET_X = SMessageUtil.getBodyItemValue(doc, "OFFSET_X", true);
		String OFFSET_Y = SMessageUtil.getBodyItemValue(doc, "OFFSET_Y", true);
		String OFFSET_T = SMessageUtil.getBodyItemValue(doc, "OFFSET_T", true);
		String result = SMessageUtil.getBodyItemValue(doc, "RESULT", false);
		
		if (result != null && "OK".equals(result))
		{
			MaskLot maskData = ExtendedObjectProxy.getMaskLotService().getMaskLotData(maskName);

			if (MaskOffsetSwithIsOn())
			{
				maskData.setEvaOffSetX(OFFSET_X);
				maskData.setEvaOffSetY(OFFSET_Y);
				maskData.setEvaOffSetTheta(OFFSET_T);

				maskData.setInitialOffSetX(OFFSET_X);
				maskData.setInitialOffSetY(OFFSET_Y);
				maskData.setInitialOffSetTheta(OFFSET_T);
			}
			else
			{
				maskData.setEvaOffSetX(OFFSET_X);
				maskData.setEvaOffSetY(OFFSET_Y);
				maskData.setEvaOffSetTheta(OFFSET_T);
			}

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("MaskOffsetChangeReport", this.getEventUser(), this.getEventComment());
			
			ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskData);
		}
		else
		{
             log.info(String.format("MaskOffsetChangeReport:BC reported result value is not %s.","OK"));
		}
	}

	@SuppressWarnings("unchecked")
	private boolean MaskOffsetSwithIsOn() throws CustomException
	{
		String sql = " SELECT ENUMVALUE FROM ENUMDEFVALUE "
				   + " WHERE ENUMNAME = :ENUMNAME  ";
		
		Map<String, Object> bindMap = new HashMap<>();
		bindMap.put("ENUMNAME", "MaskOffsetOnlyHistory_EVA");

		List<Map<String, Object>> resultList = null;
		try
		{
			resultList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, bindMap);
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		if (resultList == null || resultList.size() == 0)
		{
			//CUSTOM-0008: Unregistered enum data information.[EnumName = {0}]
			throw new CustomException("CUSTOM-0008", "MaskOffsetOnlyHistory_EVA");
		}

		String value = ConvertUtil.getMapValueByName(resultList.get(0), "ENUMVALUE");
		log.info(String .format(" EnumName [MaskOffsetOnlyHistory_EVA] EnumValue is [%s] .",value));

		if (!StringUtil.in(value, "Y", "N"))
		{
			//CUSTOM-0009: InvalidValueType: EnumName[{0}] Value is not Y or N
			throw new CustomException("CUSTOM-0009","MaskOffsetOnlyHistory_EVA");
		}

		return value.equals("Y") ? true : false;
	}
}
