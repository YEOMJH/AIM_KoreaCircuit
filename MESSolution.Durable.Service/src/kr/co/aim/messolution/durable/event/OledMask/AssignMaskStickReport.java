package kr.co.aim.messolution.durable.event.OledMask;

import java.util.ArrayList;
import java.util.HashMap;
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
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;

public class AssignMaskStickReport extends AsyncHandler {

	private static Log log = LogFactory.getLog(AssignMaskStickReport.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName       =  SMessageUtil.getBodyItemValue(doc,"MACHINENAME",true);
		String maskName          =  SMessageUtil.getBodyItemValue(doc,"MASKNAME",true);
		String frameName         =  SMessageUtil.getBodyItemValue(doc,"FRAMENAME",false);
		String maskFlowState     =  SMessageUtil.getBodyItemValue(doc,"MASKFLOWSTATE",false);
		
		List<Element> stickElementList = SMessageUtil.getBodySequenceItemList(doc, "STICKLIST", true);
		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("AssignMaskStick", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		//common check
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		CommonValidation.checkMachineHold(machineData);
		
		MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().getMaskLotData(maskName);
		
		List<MaskMaterial> maskMaterialList = new ArrayList<>();
		List<MaskStick> maskStickList = new ArrayList<>();
		
		for(Element stickE : stickElementList)
		{
			MaskStick maskStickData = null;
			
			//Check Exist
			try
			{
				maskStickData =  ExtendedObjectProxy.getMaskStickService().getMaskStickData(stickE.getChildText("STICKNAME"));
			}
			catch(Exception ex)
			{
				throw new CustomException("MATERIAL-0001",stickE.getChildText("STICKNAME"));				
			}
			
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
			
			//Check already assigned to mask
			List<Map<String, Object>> sqlResult = new ArrayList<Map<String, Object>>();
			sqlResult = checkStick(stickE.getChildText("STICKNAME"),materialType);
			
			if (sqlResult.size() > 0)
				throw new CustomException("MATERIAL-0002", stickE.getChildText("STICKNAME"));
			
			//Check position duplicated
			try
			{
				String condition = " MASKLOTNAME = ? AND POSITION = ? AND MATERIALTYPE = ? ";
				Object[] bindSet = new Object[] { maskName, stickE.getChildText("POSITION"), materialType };
				List<MaskMaterial> material = ExtendedObjectProxy.getMaskMaterialService().select(condition, bindSet);

				if (material.size() > 0)
					throw new CustomException("MASK-0089", maskName, materialType, stickE.getChildText("POSITION"));
			}
			catch (greenFrameDBErrorSignal nfdes)
			{
			}
			
			if (StringUtil.equals(maskStickData.getStickState(), "Scrapped"))
				throw new CustomException("OLEDMASK-0001", maskStickData.getStickName() + "'s State", maskStickData.getStickState());

			if (StringUtil.equals(maskStickData.getStickGrade(), "NG"))
				throw new CustomException("OLEDMASK-0001", maskStickData.getStickName() + "'s Level", maskStickData.getStickGrade());
			
			maskStickData.setStickState(constMap.Stick_State_InUse);
			maskStickData.setMaskLotName(maskName);
			maskStickData.setPosition(Integer.parseInt(stickE.getChildText("POSITION")));
			
			maskStickList.add(maskStickData);
			
			MaskMaterial maskMaterialData = new MaskMaterial();
			maskMaterialData.setMaskLotName(maskName);
			maskMaterialData.setMaterialType(materialType);					
			maskMaterialData.setMaterialName(stickE.getChildText("STICKNAME"));
			maskMaterialData.setPosition(stickE.getChildText("POSITION"));
			
			maskMaterialList.add(maskMaterialData);
		}
		
		//update masklot
		maskLotData.setMaskFlowState(maskFlowState);
		maskLotData.setDetachStickType("");
		maskLotData.setDetachPosition("");
		
		ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotData);
		
		//save assign data
		try
		{
			ExtendedObjectProxy.getMaskMaterialService().create(eventInfo, maskMaterialList);
		}
		catch (Exception ex)
		{
            if(ex instanceof DuplicateNameSignal)
            	throw new CustomException("OLEDMASK-0016","Stick",maskLotData.getMaskLotName());
            else 
            	throw new CustomException(ex.getCause());
		}
		
		//update maskstick
	    ExtendedObjectProxy.getMaskStickService().modify(eventInfo, maskStickList);
	}

	private List<Map<String, Object>> checkStick(String materialName,String materialType)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT MASKLOTNAME ");
		sql.append("  FROM CT_MASKMATERIAL ");
		sql.append(" WHERE MATERIALNAME = :MATERIALNAME ");
		sql.append("   AND MATERIALTYPE = :MATERIALTYPE ");

		Map<String, String> inquirybindMap = new HashMap<String, String>();
		inquirybindMap.put("MATERIALNAME", materialName);
		inquirybindMap.put("MATERIALTYPE", materialType);
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), inquirybindMap);

		return sqlResult;
	}
}
