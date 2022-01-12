package kr.co.aim.messolution.durable.event.OledMask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.MaskMaterial;
import kr.co.aim.messolution.extended.object.management.data.MaskStick;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;

public class DeassignMaskStickReport extends AsyncHandler {

	private static Log log = LogFactory.getLog(DeassignMaskStickReport.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName       =  SMessageUtil.getBodyItemValue(doc,"MACHINENAME",true);
		String maskName          =  SMessageUtil.getBodyItemValue(doc,"MASKNAME",true);
		String frameName         =  SMessageUtil.getBodyItemValue(doc,"FRAMENAME",false);
		String maskFlowState     =  SMessageUtil.getBodyItemValue(doc,"MASKFLOWSTATE",false);
		
		List<Element> stickElementList = SMessageUtil.getBodySequenceItemList(doc, "STICKLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeassignMaskStick", this.getEventUser(), this.getEventComment(), "", "");
		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		
		//common check
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		CommonValidation.checkMachineHold(machineData);
		
		MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().getMaskLotData(maskName);
		
		List<MaskMaterial> maskMaterialList = new ArrayList<>();
		List<MaskStick> maskStickList = new ArrayList<>();
		
		for(Element stickE : stickElementList)
		{
			//String stickType = this.getStickType(stickE.getChildText("STICKTYPE"));
			
			MaskStick maskStickData =  ExtendedObjectProxy.getMaskStickService().traceStickData(maskName,stickE.getChildText("STICKTYPE"),stickE.getChildText("POSITION"));
			
			//Get MaterialType
			String materialType = "";
			if(CommonUtil.equalsIn(maskStickData.getType(), "FMM","CMM"))
			{
				materialType = "Stick";
			}
			else
			{
				materialType = maskStickData.getType();
			}
			
			maskStickData.setStickState(constMap.Stick_State_Scrapped);
			maskStickData.setStickJudge("S");
			maskStickData.setStickGrade("NG");
			maskStickData.setMaskLotName("");
			maskStickData.setPosition(0);
			
			maskStickList.add(maskStickData);
			
			MaskMaterial maskMaterialData = ExtendedObjectProxy.getMaskMaterialService().getMaskMaterialData(maskName, materialType, maskStickData.getStickName());			
			
			maskMaterialList.add(maskMaterialData);
		
			//update masklot
			maskLotData.setMaskFlowState(maskFlowState);
			maskLotData.setDetachStickType(materialType);				
			maskLotData.setDetachPosition(stickE.getChildText("POSITION"));
			
			ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotData);
		}
		
		//remove assign data
		ExtendedObjectProxy.getMaskMaterialService().remove(eventInfo, maskMaterialList);
		
		//update maskstick
	    ExtendedObjectProxy.getMaskStickService().modify(eventInfo, maskStickList);
	}
	
	public String getStickType(String shortType) throws CustomException
	{
		if (shortType.isEmpty())
			log.info("Stick type is empty!!");
		
		String sql =" SELECT DESCRIPTION FROM ENUMDEFVALUE "
				  + " WHERE ENUMNAME ='StickType' "
				  + " AND ENUMVALUE = ? ";
		
		List<Map<String, Object>> resultList = null;

		try
		{
			resultList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, new Object[] { shortType });
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		if (resultList == null || resultList.size() == 0)
		{
			log.info(String.format("No stick type matching %s was found!!",shortType));
			return shortType;
		}
		return ConvertUtil.getMapValueByName(resultList.get(0), "DESCRIPTION");
	}
}
