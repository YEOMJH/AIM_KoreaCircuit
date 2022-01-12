package kr.co.aim.messolution.durable.event.OledMask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskFrame;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.MaskSpec;
import kr.co.aim.messolution.extended.object.management.data.MaskSubSpec;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;

public class CreateOLEDMaskReport extends AsyncHandler {

	private static Log log = LogFactory.getLog(CreateOLEDMaskReport.class);

	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String maskLotName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String frameName = SMessageUtil.getBodyItemValue(doc, "FRAMENAME", true);
		String boxName = SMessageUtil.getBodyItemValue(doc, "BOXNAME", false);
		String maskSpecName = SMessageUtil.getBodyItemValue(doc, "MASKSPECNAME", false);
		String maskFlowState = SMessageUtil.getBodyItemValue(doc, "MASKFLOWSTATE", false);

		ConstantMap constMap = GenericServiceProxy.getConstantMap();

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		//check maskSpec 
		MaskSpec maskSpec = checkMaskSpec(maskLotName,maskSpecName);
		
		//check maskSubSpec if not found
		MaskSubSpec maskSubSpec = createMaskSubSpec(maskLotName,maskSpec,eventInfo);
		
		//create mask frame
	    MaskFrame maskFrame = null;
		try
		{
			maskFrame = ExtendedObjectProxy.getMaskFrameService().selectByKey(false, new Object[] { frameName });
		}
		catch (Exception ex)
		{
			if (!(ex instanceof greenFrameDBErrorSignal &&((greenFrameDBErrorSignal) ex).getErrorCode().equals(ErrorSignal.NotFoundSignal)))
				throw new CustomException(ex.getCause());
		}

		if(maskFrame == null)
		{
			eventInfo.setEventName("CreateFrame");
			
			maskFrame = new MaskFrame();
			maskFrame.setFrameName(frameName);
			maskFrame.setFrameState(constMap.CreateState);
			maskFrame.setMaskLotName(maskLotName);
			maskFrame.setMachineName(machineName);
			maskFrame.setUnitName(unitName);
			maskFrame.setSubUnitName(subUnitName);
			maskFrame.setMaskFlowState(maskFlowState);
			maskFrame.setBoxName(boxName);
			maskFrame.setMaskType(frameName.substring(3,4));
			maskFrame.setLastEventComment(eventInfo.getEventComment());
			maskFrame.setLastEventName(eventInfo.getEventName());
			maskFrame.setLastEventTime(eventInfo.getEventTime());
			maskFrame.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
			maskFrame.setLastEventUser(eventInfo.getEventUser());

			ExtendedObjectProxy.getMaskFrameService().create(eventInfo, maskFrame);
		}
		else 
		{
			//CancelCreate before masklot
			if (StringUtil.isNotEmpty(maskFrame.getMaskLotName()) && !maskLotName.equals(maskFrame.getMaskLotName()))
			{
				MaskLot beforeMaskLot = null;
				try
				{
					beforeMaskLot = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskFrame.getMaskLotName() });
				}
				catch (greenFrameDBErrorSignal dbEx)
				{
					if (dbEx.equals(ErrorSignal.NotFoundSignal))
						log.info(String.format("CT_MASKLOT data information is not exists.search by MaskLotName = %s", maskFrame.getMaskLotName()));
				}
				catch (Exception ex)
				{
					throw new CustomException(ex.getCause());
				}

				if (beforeMaskLot != null)
				{
					eventInfo.setEventName("CancelCreate");
					ExtendedObjectProxy.getMaskLotService().remove(eventInfo, beforeMaskLot);
				}
			}

			eventInfo.setEventName("AssignMaskLot");
			maskFrame.setMaskLotName(maskLotName);
			maskFrame.setMaskFlowState(maskFlowState);
			maskFrame.setLastEventComment(eventInfo.getEventComment());
			maskFrame.setLastEventName(eventInfo.getEventName());
			maskFrame.setLastEventTime(eventInfo.getEventTime());
			maskFrame.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
			maskFrame.setLastEventUser(eventInfo.getEventUser());

		    ExtendedObjectProxy.getMaskFrameService().modify(eventInfo, maskFrame);
		}
		
		// create masklot
		MaskLot maskLot = null;
		try
		{
			maskLot = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskLotName });
		}
		catch (greenFrameDBErrorSignal dbEx)
		{
			if (dbEx.equals(ErrorSignal.NotFoundSignal))
				log.info(String.format("CT_MASKLOT data information is not exists.search by MaskLotName = %s", maskLotName));
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		if (maskLot == null)
			maskLot = createMaskLot(machineName,maskLotName, maskFrame, maskSpec, maskSubSpec.getMaskSubSpecName(),boxName, eventInfo);
	}
	
	private MaskSpec checkMaskSpec(String maskLotName, String maskSpecName) throws CustomException
	{
		String makeMaskSpecName = maskLotName.substring(1, 3) + maskLotName.substring(9, 10);
		
		//OLEDMASK-0012:The reported {0} information violated the namingrule. 
		if (!makeMaskSpecName.equals(maskSpecName))
			throw new CustomException("OLEDMASK-0012", String.format("MaskLotName =%s , MaskSpecName = %s", maskLotName, maskSpecName));

		MaskSpec maskSpec = null;
		try
		{
			maskSpec = ExtendedObjectProxy.getMaskSpecService().selectByKey(false, new Object[] { "OLED",maskSpecName });
		}
		catch (Exception ex)
		{
			//COMM-1000:{0} Data Information is not registered.condition by [{1}].
			throw new CustomException("COMM-1000","CT_MASKSPEC" ,"MaskSpecName = " + maskSpecName);
		}

		return maskSpec;
	} 
	
	private MaskSubSpec createMaskSubSpec(String maskLotName, MaskSpec maskSpecData, EventInfo eventInfo) throws CustomException
	{
		String filmLayer = maskLotName.substring(10, 12);
		String maskSubSpecName = maskSpecData.getMaskSpecName() + filmLayer;
		
		MaskSubSpec maskSubSpec = null;
		try
		{
			maskSubSpec = ExtendedObjectProxy.getMaskSubSpecService().selectByKey(false, new Object[] { maskSubSpecName });
		}
		catch (greenFrameDBErrorSignal dbEx)
		{
			if (dbEx.getErrorCode().equals(ErrorSignal.NotFoundSignal))
				log.info(String.format("CT_MASKSUBSPEC data information is not exists.search by MaskLotName = %s", maskLotName));
		}
		catch(Exception ex)
		{
			throw new CustomException(ex.getCause());
		}
		
		if (maskSubSpec == null)
		{
			eventInfo.setEventName("CreateMaskSubSpec");
			
			maskSubSpec = new MaskSubSpec();
			maskSubSpec.setMaskSubSpecName(maskSubSpecName);
			maskSubSpec.setMaskSpecName(maskSpecData.getMaskSpecName());
			maskSubSpec.setMaskKind(maskSpecData.getMaskKind());
			maskSubSpec.setMaskType(maskSpecData.getMaskType());
			maskSubSpec.setMaskFilmLayer(filmLayer);
			maskSubSpec.setLastEventName(eventInfo.getEventName());
			maskSubSpec.setLastEventComment(eventInfo.getEventComment());
			maskSubSpec.setLastEventName(eventInfo.getEventName());
			maskSubSpec.setLastEventTime(eventInfo.getEventTime());
			maskSubSpec.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
			maskSubSpec.setLastEventUser(eventInfo.getEventUser());

			return ExtendedObjectProxy.getMaskSubSpecService().create(eventInfo, maskSubSpec);
		}
		
		return maskSubSpec;
	}

	private MaskLot createMaskLot(String machineName,String maskLotName, MaskFrame maskFrame, MaskSpec maskSpec, String maskSubSpecName,String boxName, EventInfo eventInfo) throws CustomException
	{
		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		eventInfo.setEventName("CreateMask");
		
		MaskLot dataInfo = new MaskLot();

		dataInfo.setFactoryName(maskSpec.getFactoryName());
		dataInfo.setMaskLotName(maskLotName);
		dataInfo.setMaskFlowState(maskFrame.getMaskFlowState());
		dataInfo.setMaskKind(maskSpec.getMaskKind());
		dataInfo.setMaskType(maskSpec.getMaskType());
		dataInfo.setProductionType(maskSpec.getProductionType());
		dataInfo.setMaskLotJudge(constMap.prod_InitialGrade);
		dataInfo.setCleanState(constMap.Dur_Dirty);
		dataInfo.setMaskSpecName(maskSpec.getMaskSpecName());
		dataInfo.setMaskSubSpecName(maskSubSpecName);
		dataInfo.setMaskLotHoldState(constMap.Lot_NotOnHold);
		dataInfo.setMaskLotState(constMap.CreateState);
		dataInfo.setFrameName(maskFrame.getFrameName());
		dataInfo.setMaskProcessFlowName(maskSpec.getMaskProcessFlowName());
		dataInfo.setMaskProcessFlowVersion(maskSpec.getMaskProcessFlowVersion());
		dataInfo.setMachineRecipeName("");
		dataInfo.setMaskThickness(maskSpec.getThickness());
		dataInfo.setCleanUsedLimit(maskSpec.getCleanUsedLimit() == null ? 0 : maskSpec.getCleanUsedLimit());
		dataInfo.setTimeUsedLimit(maskSpec.getTimeUsedLimit() == null ? 0 : maskSpec.getTimeUsedLimit());
		dataInfo.setDurationUsedLimit(maskSpec.getDurationUsedLimit() == null ? 0 : maskSpec.getDurationUsedLimit());
		dataInfo.setMaskCycleCount(0);
		dataInfo.setMagnet(0f);
		dataInfo.setPriority(maskSpec.getPriority());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setTimeUsed(0f);
		dataInfo.setReworkState(constMap.Prod_NotInRework);
		dataInfo.setReworkCount(0);
		dataInfo.setMaskCleanCount(0);
		dataInfo.setMaskBoxName(boxName);
		dataInfo.setMaskFilmLayer(maskLotName.substring(10, 12));
		
		return ExtendedObjectProxy.getMaskLotService().create(eventInfo, dataInfo);
	}
	
	private String getMaskRecipe(MaskSpec maskSpec,String machineName) throws CustomException
	{
		if (maskSpec == null || StringUtil.isEmpty(machineName))
		{
			log.info("getMaskRecipe: The incoming argument value is Empty or Null!!.");
			return "";
		}
		// get first node id by processflow
		String nodeStack = getFirstNode(maskSpec);
		if (StringUtil.isEmpty(nodeStack))
		{
			log.info("NodeStack information is empty!!");
			return "";
		}
		
	    Node nodeData = getNodeDataById(nodeStack);
	    if(nodeData ==null)
		{
			log.info("Node data information is null.");
			return "";
		}
		
		String sql = " SELECT PM.MACHINERECIPENAME, PM.CHECKLEVEL " 
				   + " FROM TRFOPOLICY TR, POSMACHINE PM " 
				   + " WHERE TR.CONDITIONID = PM.CONDITIONID " 
				   + " AND TR.FACTORYNAME = :FACTORYNAME " 
				   + " AND TR.MASKSPECNAME = :MASKSPECNAME " 
				   + " AND TR.PROCESSFLOWNAME = :PROCESSFLOWNAME " 
				   + " AND TR.PROCESSFLOWVERSION = :PROCESSFLOWVERSION " 
				   + " AND TR.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME " 
				   + " AND TR.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION " 
				   + " AND PM.MACHINENAME = :MACHINENAME " ;
		
		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", maskSpec.getFactoryName());
		args.put("MASKSPECNAME", maskSpec.getMaskSpecName());
		args.put("PROCESSFLOWNAME", maskSpec.getMaskProcessFlowName());
		args.put("PROCESSFLOWVERSION", maskSpec.getMaskProcessFlowVersion());
		args.put("PROCESSOPERATIONNAME", nodeData.getNodeAttribute1());
		args.put("PROCESSOPERATIONVERSION", nodeData.getNodeAttribute2());
		args.put("MACHINENAME", machineName);
		
		List<Map<String, Object>> resultList = null;

		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		if (resultList == null || resultList.size() == 0)return "";

		return ConvertUtil.getMapValueByName(resultList.get(0), "MACHINERECIPENAME");
	}

	private Node getNodeDataById(String nodeStack) throws CustomException
	{
		if (StringUtil.isEmpty(nodeStack))
		{
			log.info("getNodeDataById: The incoming argument value is Empty or Null!!.");
			return null;
		}

		Node nodeData = null;
		try
		{
			nodeData = ProcessFlowServiceProxy.getNodeService().getNode(nodeStack);
		}
		catch (Exception ex)
		{
			if (ex instanceof NotFoundSignal)
				log.info(String.format(" Node data information is not found.condition by [%s].", "NodeId= " + nodeStack));
			else 
				throw new CustomException(ex.getCause());
		}
		
		return nodeData;
	}
	private String getFirstNode(MaskSpec maskSpec)
	{
		if (maskSpec == null)
		{
			log.info("getFirstNode: MaskSpec Data Information is Empty!!.");
			return "";
		}

		String sql =" SELECT A.TONODEID FROM NODE N, ARC A  "
				  + " WHERE A.ARCTYPE = 'Normal'"
				  + " AND A.PROCESSFLOWNAME = N.PROCESSFLOWNAME "
				  + " AND A.PROCESSFLOWVERSION = N.PROCESSFLOWVERSION "
				  + " AND A.FACTORYNAME = N.FACTORYNAME "
				  + " AND A.FROMNODEID = N.NODEID"
				  + " AND A.FACTORYNAME = :FACTORYNAME"
				  + " AND N.PROCESSFLOWNAME = :PROCESSFLOWNAME"
				  + " AND N.PROCESSFLOWVERSION = :PROCESSFLOWVERSION"
				  + " AND N.NODETYPE = 'Start'";
		
		Map<String,Object> bindMap = new HashMap<>();
		bindMap.put("FACTORYNAME", maskSpec.getFactoryName());
		bindMap.put("PROCESSFLOWNAME", maskSpec.getMaskProcessFlowName());
		bindMap.put("PROCESSFLOWVERSION", maskSpec.getMaskProcessFlowVersion());
		
		List<Map<String,Object>> resultList = null;
		
		try
		{
			resultList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, bindMap);
		}
		catch (Exception ex)
		{
			log.info(ex.getCause());
			return "";
		}
		
		if(resultList ==null || resultList.size()==0) return "";
		return ConvertUtil.getMapValueByName(resultList.get(0), "TONODEID");
	}
}
