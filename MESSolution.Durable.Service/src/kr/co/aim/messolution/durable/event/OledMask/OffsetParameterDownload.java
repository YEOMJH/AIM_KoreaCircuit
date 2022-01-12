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
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;

/*
 * R2R -> MES & R2R -> BC
 * 
 * MACHINENAME
 * UNITNAME
 * SUBUNITNAME
 * MACHINERECIPENAME
 * MASKNAME
 * UNIQUEKEY
 * MODE
 * OFFSET_X
 * OFFSET_Y
 * OFFSET_THETA
 */

public class OffsetParameterDownload extends AsyncHandler 
{
	Log log = LogFactory.getLog(this.getClass());
	
	public void doWorks(Document doc) throws CustomException 
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String offset_X = SMessageUtil.getBodyItemValue(doc, "OFFSET_X", true);
		String offset_Y = SMessageUtil.getBodyItemValue(doc, "OFFSET_Y", true);
		String offset_T = SMessageUtil.getBodyItemValue(doc, "OFFSET_THETA", true);
		
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		MaskLot maskData = ExtendedObjectProxy.getMaskLotService().getMaskLotData(maskName);
		
		if (MaskOffsetSwithIsOn())
		{
			maskData.setR2rOffSetX(String.valueOf(Double.valueOf(offset_X) / 10));
			maskData.setR2rOffSetY(String.valueOf(Double.valueOf(offset_Y) / 10));
			maskData.setR2rOffSetTheta(offset_T);

			maskData.setInitialOffSetX(String.valueOf(Double.valueOf(offset_X) / 10));
			maskData.setInitialOffSetY(String.valueOf(Double.valueOf(offset_Y) / 10));
			maskData.setInitialOffSetTheta(offset_T);
		}
		else
		{
			maskData.setR2rOffSetX(String.valueOf(Double.valueOf(offset_X) / 10));
			maskData.setR2rOffSetY(String.valueOf(Double.valueOf(offset_Y) / 10));
			maskData.setR2rOffSetTheta(offset_T);
		}
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("MaskOffsetChangeReport", this.getEventUser(), this.getEventComment());
		ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskData);
	}

	@SuppressWarnings("unchecked")
	private boolean MaskOffsetSwithIsOn() throws CustomException
	{
		String sql = " SELECT ENUMVALUE FROM ENUMDEFVALUE "
				   + " WHERE ENUMNAME = :ENUMNAME  ";
		
		Map<String, Object> bindMap = new HashMap<>();
		bindMap.put("ENUMNAME", "MaskOffsetOnlyHistory_R2R");

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
			throw new CustomException("CUSTOM-0008", "MaskOffsetOnlyHistory_R2R");
		}

		String value = ConvertUtil.getMapValueByName(resultList.get(0), "ENUMVALUE");
		log.info(String .format(" EnumName [MaskOffsetOnlyHistory_R2R] EnumValue is [%s] .",value));

		if (!StringUtil.in(value, "Y", "N"))
		{
			//CUSTOM-0009: InvalidValueType: EnumName[{0}] Value is not Y or N
			throw new CustomException("CUSTOM-0009","MaskOffsetOnlyHistory_R2R");
		}

		return value.equals("Y") ? true : false;
	}
}
