package kr.co.aim.messolution.durable.event.CNX;

import java.sql.SQLClientInfoException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskFrame;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.MaskMaterial;
import kr.co.aim.messolution.extended.object.management.data.MaskMaterials;
import kr.co.aim.messolution.extended.object.management.data.MaskStick;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;
import org.jdom.Element;

public class AssignOLEDMaskMaterial extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		String maskLotName = SMessageUtil.getBodyItemValue(doc, "MASKLOTNAME", true);
		String action = SMessageUtil.getBodyItemValue(doc, "ACTION", true);
		String materialType = SMessageUtil.getBodyItemValue(doc, "MATERIALTYPE", false);
		String materialName = SMessageUtil.getBodyItemValue(doc, "MATERIALNAME", false);
		String position = SMessageUtil.getBodyItemValue(doc, "POSITION", false);
		String flowState = SMessageUtil.getBodyItemValue(doc, "FLOWSTATE", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());

		MaskLot dataInfo = null;
		try
		{
			dataInfo = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskLotName });
		}
		catch (greenFrameDBErrorSignal nfdes)
		{
			throw new CustomException("DURABLE-5050", maskLotName);
		}

		if(materialType.equals("Frame")&&(dataInfo.getMaskLotState().equals(GenericServiceProxy.getConstantMap().MaskLotState_Released)))			
		{
			throw new CustomException("MASK-0026", maskLotName, dataInfo.getMaskLotState());
		}

		if (!action.equals("ImportSheet"))
		{
			if (materialType.equals("Frame"))
			{
				List<Map<String, Object>> sqlResult = new ArrayList<Map<String, Object>>();

				if (action.equals("Create"))
				{
					sqlResult = selectFrame(materialName);
					if (sqlResult.size() == 0)
						throw new CustomException("MATERIAL-0001", materialName);

					sqlResult.clear();
					sqlResult = checkFrame(materialName);
					if (sqlResult.size() > 0)
						throw new CustomException("MATERIAL-0002", materialName);
				}
			}
//			else if (materialType.equals("Stick"))			
			else
			{
				List<Map<String, Object>> sqlResult = new ArrayList<Map<String, Object>>();

				if (action.equals("Create"))
				{
					if (materialType.equals("Stick"))
					{
						sqlResult = selectSheetForStick(materialName,materialType);
						if (sqlResult.size() == 0)
							throw new CustomException("MATERIAL-0001", materialName);
					}
					else
					{
						sqlResult = selectSheet(materialName,materialType);
						if (sqlResult.size() == 0)
							throw new CustomException("MATERIAL-0001", materialName);
					}				

					sqlResult.clear();
					sqlResult = checkStick(materialName,materialType);
					if (sqlResult.size() > 0)
						throw new CustomException("MATERIAL-0002", materialName);

					try
					{
						String condition = " MASKLOTNAME = ? AND POSITION = ? AND MATERIALTYPE = ?";
						Object[] bindSet = new Object[] { maskLotName, position, materialType };
						List<MaskMaterial> material = ExtendedObjectProxy.getMaskMaterialService().select(condition, bindSet);

						if (material.size() > 0)
							throw new CustomException("MASK-0089", maskLotName, materialType, position);
					}
					catch (greenFrameDBErrorSignal nfdes)
					{
					}
				}
			}
//			else  //Cover/Huling/Dummy/Align
//			{
//				List<Map<String, Object>> sqlResult = new ArrayList<Map<String, Object>>();
//
//				if (action.equals("Create"))
//				{
//					sqlResult = selectStickExceptSheet(materialName,materialType);
//					if (sqlResult.size() == 0)
//						throw new CustomException("MATERIAL-0001", materialName);
//
//					sqlResult.clear();
//					sqlResult = checkStick(materialName,materialType);
//					if (sqlResult.size() > 0)
//						throw new CustomException("MATERIAL-0002", materialName);
//					
//					try
//					{
//						String condition = " MASKLOTNAME = ? AND POSITION = ? ";
//						Object[] bindSet = new Object[] { maskLotName, position };
//						List<MaskMaterial> material = ExtendedObjectProxy.getMaskMaterialService().select(condition, bindSet);
//
//						if (material.size() > 0)
//							throw new CustomException("MASK-0029", maskLotName, position);
//					}
//					catch (greenFrameDBErrorSignal nfdes)
//					{
//					}
//				}
//			}
		}

		if (action.equals("Create"))
		{

			if (StringUtil.equals(dataInfo.getMaskLotHoldState(), constantMap.MaskLotHoldState_OnHold))
				throw new CustomException("MASK-0013", maskLotName);
			
			checkLayer(materialType,materialName,maskLotName);
			
			eventInfo.setEventName("AssignMaterial");

			if (materialType.equals("Frame")) // Frame
			{
				dataInfo.setFrameName(materialName);
				dataInfo.setMaskFlowState(flowState);
				dataInfo.setLastEventComment(eventInfo.getEventComment());
				dataInfo.setLastEventName(eventInfo.getEventName());
				dataInfo.setLastEventTime(eventInfo.getEventTime());
				dataInfo.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
				dataInfo.setLastEventUser(eventInfo.getEventUser());

				ExtendedObjectProxy.getMaskLotService().modify(eventInfo, dataInfo);

				MaskFrame frameInfo = ExtendedObjectProxy.getMaskFrameService().selectByKey(false, new Object[] { materialName });

				frameInfo.setMaskLotName(maskLotName);
				frameInfo.setLastEventComment(eventInfo.getEventComment());
				frameInfo.setLastEventName(eventInfo.getEventName());
				frameInfo.setLastEventTime(eventInfo.getEventTime());
				frameInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
				frameInfo.setLastEventUser(eventInfo.getEventUser());

				ExtendedObjectProxy.getMaskFrameService().modify(eventInfo, frameInfo);
			}
			else
			{
				MaskMaterial MaterialdataInfo = new MaskMaterial();
				MaterialdataInfo.setMaskLotName(maskLotName);
				MaterialdataInfo.setMaterialType(materialType);
				MaterialdataInfo.setMaterialName(materialName);
				MaterialdataInfo.setPosition(position);
				MaterialdataInfo.setLastEventComment(eventInfo.getEventComment());
				MaterialdataInfo.setLastEventName(eventInfo.getEventName());
				MaterialdataInfo.setLastEventTime(eventInfo.getEventTime());
				MaterialdataInfo.setLastEventTimeKey(eventInfo.getLastEventTimekey());
				MaterialdataInfo.setLastEventUser(eventInfo.getEventUser());

				ExtendedObjectProxy.getMaskMaterialService().create(eventInfo, MaterialdataInfo);
				
				MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().getMaskLotData(maskLotName);
				//update masklot
				maskLotData.setMaskFlowState(flowState);
				maskLotData.setDetachStickType("");
				maskLotData.setDetachPosition("");
				
				ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotData);

//				if (materialType.equals("Stick")) // Sheet
//				{
					MaskStick maskStick = ExtendedObjectProxy.getMaskStickService().selectByKey(false, new Object[] { materialName });

					if (StringUtil.equals(maskStick.getStickState(), "Scrapped"))
						throw new CustomException("OLEDMASK-0001", maskStick.getStickName() + "'s State", maskStick.getStickState());

					if (StringUtil.equals(maskStick.getStickGrade(), "NG"))
						throw new CustomException("OLEDMASK-0001", maskStick.getStickName() + "'s Level", maskStick.getStickGrade());

					maskStick.setStickState("InUse");
					maskStick.setMaskLotName(maskLotName);
					maskStick.setLastEventComment(eventInfo.getEventComment());
					maskStick.setLastEventName(eventInfo.getEventName());
					maskStick.setLastEventTime(eventInfo.getEventTime());
					maskStick.setLastEventTimeKey(eventInfo.getEventTimeKey());
					maskStick.setLastEventUser(eventInfo.getEventUser());
					maskStick.setPosition(position.isEmpty()?0:Long.valueOf(position));
					ExtendedObjectProxy.getMaskStickService().modify(eventInfo, maskStick);
//				}
//				else
//				// Cover, Align, Dummy, Hauling
//				{
//					MaskMaterials materialData = null;
//					try
//					{
//						materialData = ExtendedObjectProxy.getMaskMaterialsService().selectByKey(true, new Object[] { materialType, materialName });
//					}
//					catch (greenFrameDBErrorSignal nfdes)
//					{
//						throw new CustomException("MATERIAL-0001", materialType + ": " + materialName);
//					}
//                    if(materialData.getTotalQuantity() - 1<0)
//                    {
//                    	throw new CustomException("MATERIAL-0027");
//                    }
//					materialData.setAddQuantity(-1);
//					materialData.setTotalQuantity(materialData.getTotalQuantity() - 1);
//
//					ExtendedObjectProxy.getMaskMaterialsService().modify(eventInfo, materialData);
//				}
			}
		}
		else if (action.equals("ImportSheet"))
		{

			if (StringUtil.equals(dataInfo.getMaskLotHoldState(), constantMap.MaskLotHoldState_OnHold))
				throw new CustomException("MASK-0013", maskLotName);
			

			eventInfo.setEventName("AssignMaterial");

//			if (materialType.equals("Stick"))
//			{
				List<Element> sheetList = SMessageUtil.getBodySequenceItemList(doc, "SHEETLIST", true);
				for (Element sheetE : sheetList)
				{
					String sheetName = sheetE.getChild("MATERIALNAME").getText();
					String sheetPosition = sheetE.getChild("POSITION").getText();
					String type = sheetE.getChild("MATERIALTYPE").getText();
					List<Map<String, Object>> sqlResult = new ArrayList<Map<String, Object>>();

					if (type.equals("Stick"))
					{
						sqlResult = selectSheetForStick(sheetName,type);
						if (sqlResult.size() == 0)
							throw new CustomException("MATERIAL-0001", sheetName);
					}
					else
					{
						sqlResult = selectSheet(sheetName,type);
						if (sqlResult.size() == 0)
							throw new CustomException("MATERIAL-0001", sheetName);
					}				

					sqlResult.clear();
					sqlResult = checkStick(sheetName,type);
					if (sqlResult.size() > 0)
						throw new CustomException("MATERIAL-0002", sheetName);

					StringBuffer inquirysql = new StringBuffer();
					inquirysql.append("SELECT * ");
					inquirysql.append("  FROM CT_MASKMATERIAL ");
					inquirysql.append(" WHERE MASKLOTNAME = :MASKLOTNAME ");
					inquirysql.append("   AND POSITION = :POSITION ");
					inquirysql.append("   AND MATERIALTYPE = :MATERIALTYPE ");

					Map<String, String> inquirybindMap = new HashMap<String, String>();
					inquirybindMap.put("MASKLOTNAME", maskLotName);
					inquirybindMap.put("POSITION", sheetPosition);
					inquirybindMap.put("MATERIALTYPE", type);
					
					@SuppressWarnings("unchecked")
					List<Map<String, Object>> material = GenericServiceProxy.getSqlMesTemplate().queryForList(inquirysql.toString(), inquirybindMap);

					if (material.size() > 0)
					{
						throw new CustomException("MASK-0089", maskLotName, type, sheetPosition);
					}
					else
					{
						checkLayer(type,sheetName,maskLotName);
						
						MaskMaterial MaterialdataInfo = new MaskMaterial();
						MaterialdataInfo.setMaskLotName(maskLotName);
						//MaterialdataInfo.setMaterialType(materialType);
						MaterialdataInfo.setMaterialType(type);
						MaterialdataInfo.setMaterialName(sheetName);
						MaterialdataInfo.setPosition(sheetPosition);
						MaterialdataInfo.setLastEventComment(eventInfo.getEventComment());
						MaterialdataInfo.setLastEventName(eventInfo.getEventName());
						MaterialdataInfo.setLastEventTime(eventInfo.getEventTime());
						MaterialdataInfo.setLastEventTimeKey(eventInfo.getLastEventTimekey());
						MaterialdataInfo.setLastEventUser(eventInfo.getEventUser());
					
					MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().getMaskLotData(maskLotName);
					//update masklot
					maskLotData.setMaskFlowState(flowState);
					maskLotData.setDetachStickType("");
					maskLotData.setDetachPosition("");
					
					ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotData);

						ExtendedObjectProxy.getMaskMaterialService().create(eventInfo, MaterialdataInfo);

						MaskStick maskStick = ExtendedObjectProxy.getMaskStickService().selectByKey(false, new Object[] { sheetName });

						if (StringUtil.equals(maskStick.getStickState(), "Scrapped"))
							throw new CustomException("OLEDMASK-0001", maskStick.getStickName() + "'s State", maskStick.getStickState());

						if (StringUtil.equals(maskStick.getStickGrade(), "NG"))
							throw new CustomException("OLEDMASK-0001", maskStick.getStickName() + "'s Level", maskStick.getStickGrade());

						maskStick.setStickState("InUse");
						maskStick.setMaskLotName(maskLotName);
						maskStick.setLastEventComment(eventInfo.getEventComment());
						maskStick.setLastEventName(eventInfo.getEventName());
						maskStick.setLastEventTime(eventInfo.getEventTime());
						maskStick.setLastEventTimeKey(eventInfo.getEventTimeKey());
						maskStick.setLastEventUser(eventInfo.getEventUser());
						maskStick.setPosition(position.isEmpty()?0:Long.valueOf(position));
						ExtendedObjectProxy.getMaskStickService().modify(eventInfo, maskStick);
					}
//				}
			}
		}
		else if (action.equals("Delete"))
		{
			eventInfo.setEventName("DeassignMaterial");

			if (materialType.equals("Frame")) // Frame
			{
				dataInfo.setFrameName("");
				//dataInfo.setMaskFlowState(flowState);
				dataInfo.setLastEventComment(eventInfo.getEventComment());
				dataInfo.setLastEventName(eventInfo.getEventName());
				dataInfo.setLastEventTime(eventInfo.getEventTime());
				dataInfo.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
				dataInfo.setLastEventUser(eventInfo.getEventUser());

				ExtendedObjectProxy.getMaskLotService().modify(eventInfo, dataInfo);

				MaskFrame frameInfo = ExtendedObjectProxy.getMaskFrameService().selectByKey(false, new Object[] { materialName });

				frameInfo.setMaskLotName("");
				frameInfo.setLastEventComment(eventInfo.getEventComment());
				frameInfo.setLastEventName(eventInfo.getEventName());
				frameInfo.setLastEventTime(eventInfo.getEventTime());
				frameInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
				frameInfo.setLastEventUser(eventInfo.getEventUser());

				ExtendedObjectProxy.getMaskFrameService().modify(eventInfo, frameInfo);
			}
			else
			{
				MaskMaterial MaterialdataInfo = ExtendedObjectProxy.getMaskMaterialService().selectByKey(false, new Object[] { maskLotName, materialType, materialName });

				ExtendedObjectProxy.getMaskMaterialService().remove(eventInfo, MaterialdataInfo);
				
				MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().getMaskLotData(maskLotName);
				//update masklot
				//maskLotData.setMaskFlowState(flowState);
				maskLotData.setDetachStickType(materialType);
				maskLotData.setDetachPosition(position);
				
				ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotData);

//				if (materialType.equals("Stick")) // Sheet
//				{
					MaskStick maskStick = ExtendedObjectProxy.getMaskStickService().selectByKey(false, new Object[] { materialName });
					maskStick.setStickState("Scrapped");
					maskStick.setStickGrade("NG");
					maskStick.setStickJudge("S");
					maskStick.setMaskLotName("");
					maskStick.setPosition(0);
					maskStick.setLastEventComment(eventInfo.getEventComment());
					maskStick.setLastEventName(eventInfo.getEventName());
					maskStick.setLastEventTime(eventInfo.getEventTime());
					maskStick.setLastEventTimeKey(eventInfo.getEventTimeKey());
					maskStick.setLastEventUser(eventInfo.getEventUser());
					ExtendedObjectProxy.getMaskStickService().modify(eventInfo, maskStick);
//				}
//				else
//				// Cover, Align, Dummy, Hauling
//				{
//					MaskMaterials materialData = null;
//					try
//					{
//						materialData = ExtendedObjectProxy.getMaskMaterialsService().selectByKey(true, new Object[] { materialType, materialName });
//					}
//					catch (greenFrameDBErrorSignal nfdes)
//					{
//						throw new CustomException("MATERIAL-0001", materialType + ": " + materialName);
//					}
//
//					materialData.setAddQuantity(1);
//					materialData.setTotalQuantity(materialData.getTotalQuantity() + 1);
//
//					ExtendedObjectProxy.getMaskMaterialsService().modify(eventInfo, materialData);
//				}
			}
		}

		return doc;
	}

	private List<Map<String, Object>> selectFrame(String frameName)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT FRAMENAME ");
		sql.append("  FROM CT_MASKFRAME ");
		sql.append(" WHERE FRAMENAME = :FRAMENAME ");

		Map<String, String> inquirybindMap = new HashMap<String, String>();
		inquirybindMap.put("FRAMENAME", frameName);
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), inquirybindMap);

		return sqlResult;
	}

	private List<Map<String, Object>> selectSheet(String sheetName,String materialType)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT STICKNAME ");
		sql.append("  FROM CT_MASKSTICK ");
		sql.append(" WHERE STICKNAME = :STICKNAME ");
		sql.append(" AND TYPE = :TYPE ");
		
		Map<String, String> inquirybindMap = new HashMap<String, String>();
		inquirybindMap.put("STICKNAME", sheetName);
		inquirybindMap.put("TYPE", materialType);
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), inquirybindMap);

		return sqlResult;
	}
	
	private List<Map<String, Object>> selectSheetForStick(String sheetName,String materialType)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT STICKNAME ");
		sql.append("  FROM CT_MASKSTICK ");
		sql.append(" WHERE STICKNAME = :STICKNAME ");
		sql.append(" AND TYPE IN('FMM','CMM') ");
		
		Map<String, String> inquirybindMap = new HashMap<String, String>();
		inquirybindMap.put("STICKNAME", sheetName);
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), inquirybindMap);

		return sqlResult;
	}
	
	private List<Map<String, Object>> selectStickExceptSheet(String stickName,String materialType)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT MASKMATERIALNAME ");
		sql.append("  FROM CT_MASKMATERIALS ");
		sql.append(" WHERE MASKMATERIALNAME = :MASKMATERIALNAME ");
		sql.append(" AND MATERIALTYPE=:MATERIALTYPE ");

		Map<String, String> inquirybindMap = new HashMap<String, String>();
		inquirybindMap.put("MASKMATERIALNAME", stickName);
		inquirybindMap.put("MATERIALTYPE", materialType);
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), inquirybindMap);

		return sqlResult;
	}

	private List<Map<String, Object>> checkFrame(String frameName)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT MASKLOTNAME ");
		sql.append("  FROM CT_MASKLOT ");
		sql.append(" WHERE FRAMENAME = :FRAMENAME ");

		Map<String, String> inquirybindMap = new HashMap<String, String>();
		inquirybindMap.put("FRAMENAME", frameName);
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), inquirybindMap);

		return sqlResult;
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
	
	private void checkLayer(String materialType,String maskMaterial,String maskLot) throws CustomException
	{
		String maskFilmLayer="";
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT MASKFILMLAYER ");
		sql.append(" FROM CT_MASKLOT ");
		sql.append(" WHERE MASKLOTNAME=:MASKLOTNAME ");
		Map<String, String> inquirybindMap = new HashMap<String, String>();
		inquirybindMap.put("MASKLOTNAME", maskLot);
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), inquirybindMap);
		if(sqlResult!=null&&sqlResult.size()==1)
		{
			maskFilmLayer=ConvertUtil.getMapValueByName(sqlResult.get(0), "MASKFILMLAYER");			
		}
		else
		{
			throw new CustomException("MASK-0073",maskLot);
		}
//		if(materialType.equals("Stick"))
		if(!materialType.equals("Frame"))
		{
			StringBuffer sheetLayerCheck=new StringBuffer();
			sheetLayerCheck.append(" SELECT A.STICKNAME,B.STICKTYPE,A.STICKFILMLAYER,B.MASKFILMLAYER FROM CT_MASKSTICK A ,CT_MASKSTICKRULE B ");
			sheetLayerCheck.append(" WHERE B.STICKTYPE=:MATERIALTYPE ");
			sheetLayerCheck.append(" AND A.STICKNAME=:STICKNMAE ");
			sheetLayerCheck.append(" AND B.STICKFILMLAYER=A.STICKFILMLAYER ");
			sheetLayerCheck.append(" AND B.MASKFILMLAYER=:MASKFILMLAYER ");
			
			Map<String, String> sheetLayerCheckbindMap = new HashMap<String, String>();
			sheetLayerCheckbindMap.put("STICKNMAE", maskMaterial);
			sheetLayerCheckbindMap.put("MASKFILMLAYER", maskFilmLayer);
			sheetLayerCheckbindMap.put("MATERIALTYPE", materialType);
			
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> sheetLayerCheckResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sheetLayerCheck.toString(), sheetLayerCheckbindMap);
			if(sheetLayerCheckResult==null ||sheetLayerCheckResult.size()!=1)
			{
				throw new CustomException("MASK-0074");
			}
		}
//		else if(!materialType.equals("Frame"))
//		{
//			StringBuffer stickLayerCheck=new StringBuffer();
//			stickLayerCheck.append(" SELECT A.MASKMATERIALNAME,A.MATERIALTYPE,B.STICKFILMLAYER,B.MASKFILMLAYER FROM CT_MASKMATERIALS A ,CT_MASKSTICKRULE B ");
//			stickLayerCheck.append(" WHERE A.MASKMATERIALNAME=:MASKMATERIALNAME ");
//			stickLayerCheck.append(" AND A.MATERIALTYPE=:MATERIALTYPE ");
//			stickLayerCheck.append(" AND A.MATERIALTYPE=B.STICKTYPE ");
//			stickLayerCheck.append(" AND A.LAYER=B.STICKFILMLAYER ");
//			stickLayerCheck.append(" AND B.MASKFILMLAYER=:MASKFILMLAYER ");
//			
//			Map<String, String> stickLayerCheckbindMap = new HashMap<String, String>();
//			stickLayerCheckbindMap.put("MASKMATERIALNAME", maskMaterial);
//			stickLayerCheckbindMap.put("MATERIALTYPE", materialType);
//			stickLayerCheckbindMap.put("MASKFILMLAYER", maskFilmLayer);
//			
//			@SuppressWarnings("unchecked")
//			List<Map<String, Object>> stickLayerCheckResult = GenericServiceProxy.getSqlMesTemplate().queryForList(stickLayerCheck.toString(), stickLayerCheckbindMap);
//			if(stickLayerCheckResult==null ||stickLayerCheckResult.size()!=1)
//			{
//				throw new CustomException("MASK-0074");
//			}
//		}
	}
}