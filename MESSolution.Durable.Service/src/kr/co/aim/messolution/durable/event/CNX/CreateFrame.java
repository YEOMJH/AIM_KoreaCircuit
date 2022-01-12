package kr.co.aim.messolution.durable.event.CNX;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskFrame;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.MaskMaterial;
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
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class CreateFrame extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		List<Element> frameList = SMessageUtil.getBodySequenceItemList(doc, "FRAMELIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		for (Element Frame : frameList)
		{
			String frameName = SMessageUtil.getChildText(Frame, "FRAMENAME", true);

			List<Map<String, Object>> result = checkFrameName(frameName);

			if (result.size() > 0)
			{
				String frameState = ConvertUtil.getMapValueByName(result.get(0), "FRAMESTATE");
				if (frameState.equals("Shipped"))
				{
					eventInfo.setEventName("CreateFrame");
					MaskFrame frameData = ExtendedObjectProxy.getMaskFrameService().selectByKey(false, new Object[] { frameName });
					frameData.setFrameState("Created");
					frameData.setLastEventComment(eventInfo.getEventComment());
					frameData.setLastEventName(eventInfo.getEventName());
					frameData.setLastEventTime(eventInfo.getEventTime());
					frameData.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
					frameData.setLastEventUser(eventInfo.getEventUser());

					ExtendedObjectProxy.getMaskFrameService().modify(eventInfo, frameData);

					String maskLotName = getMaskLotName(frameName);
					setMaskLotCreated(eventInfo, maskLotName);
					deassignMaskMaterial(eventInfo, maskLotName);
				}
				else
				{
					// Only shipped frame can be re-used.
					throw new CustomException("OLEDMASK-0008", frameName);
				}
			}
			else
			{
				eventInfo.setEventName("CreateFrame");

				String vendorName = SMessageUtil.getChildText(Frame, "VENDORNAME", true);
				String thickNess = SMessageUtil.getChildText(Frame, "THICKNESS", true);
				String flatNess = SMessageUtil.getChildText(Frame, "FLATNESS", false);

				String outside1Length = SMessageUtil.getChildText(Frame, "OUTSIDE1LENGTH", false);
				String outside1Width = SMessageUtil.getChildText(Frame, "OUTSIDE1WIDTH", false);
				String internal1Length = SMessageUtil.getChildText(Frame, "INTERNAL1LENGTH", false);
				String internal1Width = SMessageUtil.getChildText(Frame, "INTERNAL1WIDTH", false);

				String outside2Length = SMessageUtil.getChildText(Frame, "OUTSIDE2LENGTH", false);
				String outside2Width = SMessageUtil.getChildText(Frame, "OUTSIDE2WIDTH", false);
				String internal2Length = SMessageUtil.getChildText(Frame, "INTERNAL2LENGTH", false);
				String internal2Width = SMessageUtil.getChildText(Frame, "INTERNAL2WIDTH", false);

				String alignPosition1DesignValX = SMessageUtil.getChildText(Frame, "ALIGNPOSITION1DESIGNVALX", false);
				String alignPosition1DesignValY = SMessageUtil.getChildText(Frame, "ALIGNPOSITION1DESIGNVALY", false);
				String alignPosition1MeasureValX = SMessageUtil.getChildText(Frame, "ALIGNPOSITION1MEASUREVALX", false);
				String alignPosition1MeasureValY = SMessageUtil.getChildText(Frame, "ALIGNPOSITION1MEASUREVALY", false);
				String alignPosition1DesignSize = SMessageUtil.getChildText(Frame, "ALIGNPOSITION1DESIGNSIZE", false);
				String alignPosition1MeasureSize = SMessageUtil.getChildText(Frame, "ALIGNPOSITION1MEASURESIZE", false);
				String alignHoleDesignRN1 = SMessageUtil.getChildText(Frame, "COARN1", false);
				String alignHoleMeasurementRN1 = SMessageUtil.getChildText(Frame, "RN1", false);

				String alignPosition2DesignValX = SMessageUtil.getChildText(Frame, "ALIGNPOSITION2DESIGNVALX", false);
				String alignPosition2DesignValY = SMessageUtil.getChildText(Frame, "ALIGNPOSITION2DESIGNVALY", false);
				String alignPosition2MeasureValX = SMessageUtil.getChildText(Frame, "ALIGNPOSITION2MEASUREVALX", false);
				String alignPosition2MeasureValY = SMessageUtil.getChildText(Frame, "ALIGNPOSITION2MEASUREVALY", false);
				String alignPosition2DesignSize = SMessageUtil.getChildText(Frame, "ALIGNPOSITION2DESIGNSIZE", false);
				String alignPosition2MeasureSize = SMessageUtil.getChildText(Frame, "ALIGNPOSITION2MEASURESIZE", false);
				String alignHoleDesignRN2 = SMessageUtil.getChildText(Frame, "COARN2", false);
				String alignHoleMeasurementRN2 = SMessageUtil.getChildText(Frame, "RN2", false);

				String alignPosition3DesignValX = SMessageUtil.getChildText(Frame, "ALIGNPOSITION3DESIGNVALX", false);
				String alignPosition3DesignValY = SMessageUtil.getChildText(Frame, "ALIGNPOSITION3DESIGNVALY", false);
				String alignPosition3MeasureValX = SMessageUtil.getChildText(Frame, "ALIGNPOSITION3MEASUREVALX", false);
				String alignPosition3MeasureValY = SMessageUtil.getChildText(Frame, "ALIGNPOSITION3MEASUREVALY", false);
				String alignPosition3DesignSize = SMessageUtil.getChildText(Frame, "ALIGNPOSITION3DESIGNSIZE", false);
				String alignPosition3MeasureSize = SMessageUtil.getChildText(Frame, "ALIGNPOSITION3MEASURESIZE", false);
				String alignHoleDesignRN3 = SMessageUtil.getChildText(Frame, "COARN3", false);
				String alignHoleMeasurementRN3 = SMessageUtil.getChildText(Frame, "RN3", false);

				String alignPosition4DesignValX = SMessageUtil.getChildText(Frame, "ALIGNPOSITION4DESIGNVALX", false);
				String alignPosition4DesignValY = SMessageUtil.getChildText(Frame, "ALIGNPOSITION4DESIGNVALY", false);
				String alignPosition4MeasureValX = SMessageUtil.getChildText(Frame, "ALIGNPOSITION4MEASUREVALX", false);
				String alignPosition4MeasureValY = SMessageUtil.getChildText(Frame, "ALIGNPOSITION4MEASUREVALY", false);
				String alignPosition4DesignSize = SMessageUtil.getChildText(Frame, "ALIGNPOSITION4DESIGNSIZE", false);
				String alignPosition4MeasureSize = SMessageUtil.getChildText(Frame, "ALIGNPOSITION4MEASURESIZE", false);
				String alignHoleDesignRN4 = SMessageUtil.getChildText(Frame, "COARN4", false);
				String alignHoleMeasurementRN4 = SMessageUtil.getChildText(Frame, "RN4", false);

				String coverDepthMax = SMessageUtil.getChildText(Frame, "COVERDEPTHMAX", false);
				String coverDepthMin = SMessageUtil.getChildText(Frame, "COVERDEPTHMIN", false);
				String coverWidthMax = SMessageUtil.getChildText(Frame, "COVERWIDTHMAX", false);
				String coverWidthMin = SMessageUtil.getChildText(Frame, "COVERWIDTHMIN", false);
				String haulingDepthMax = SMessageUtil.getChildText(Frame, "HAULINGDEPTHMAX", false);
				String haulingDepthMin = SMessageUtil.getChildText(Frame, "HAULINGDEPTHMIN", false);
				String haulingWidthMax = SMessageUtil.getChildText(Frame, "HAULINGWIDTHMAX", false);
				String haulingWidthMin = SMessageUtil.getChildText(Frame, "HAULINGWIDTHMIN", false);
				String texture = SMessageUtil.getChildText(Frame, "TEXTURE", true);
				String flatness2 = SMessageUtil.getChildText(Frame, "FLATNESS2", false);


				String sReceiveTime = SMessageUtil.getChildText(Frame, "RECEIVETIME", true);
				Date date = new Date();
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				try
				{
					date = dateFormat.parse(sReceiveTime);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				Timestamp receiveTime = new Timestamp(date.getTime());

				MaskFrame maskFrame = new MaskFrame();

				maskFrame.setFrameName(frameName);
				maskFrame.setVendorName(vendorName);
				maskFrame.setThickNess(thickNess);
				maskFrame.setFlatNess(flatNess);
				maskFrame.setReceiveTime(receiveTime);

				maskFrame.setCoverDepthMax(coverDepthMax);
				maskFrame.setCoverDepthMin(coverDepthMin);
				maskFrame.setCoverWidthMax(coverWidthMax);
				maskFrame.setCoverWidthMin(coverWidthMin);
				maskFrame.setHaulingDepthMax(haulingDepthMax);
				maskFrame.setHaulingDepthMin(haulingDepthMin);
				maskFrame.setHaulingWidthMax(haulingWidthMax);
				maskFrame.setHaulingWidthMin(haulingWidthMin);

				maskFrame.setOutside1Length(outside1Length);
				maskFrame.setOutside1Width(outside1Width);
				maskFrame.setInternal1Length(internal1Length);
				maskFrame.setInternal1Width(internal1Width);

				maskFrame.setOutside2Length(outside2Length);
				maskFrame.setOutside2Width(outside2Width);
				maskFrame.setInternal2Length(internal2Length);
				maskFrame.setInternal2Width(internal2Width);

				maskFrame.setAlignPosition1DesignValX(alignPosition1DesignValX);
				maskFrame.setAlignPosition1DesignValY(alignPosition1DesignValY);
				maskFrame.setAlignPosition1MeasureValX(alignPosition1MeasureValX);
				maskFrame.setAlignPosition1MeasureValY(alignPosition1MeasureValY);
				maskFrame.setAlignPosition1DesignSize(alignPosition1DesignSize);
				maskFrame.setAlignPosition1MeasureSize(alignPosition1MeasureSize);

				maskFrame.setAlignPosition2DesignValX(alignPosition2DesignValX);
				maskFrame.setAlignPosition2DesignValY(alignPosition2DesignValY);
				maskFrame.setAlignPosition2MeasureValX(alignPosition2MeasureValX);
				maskFrame.setAlignPosition2MeasureValY(alignPosition2MeasureValY);
				maskFrame.setAlignPosition2DesignSize(alignPosition2DesignSize);
				maskFrame.setAlignPosition2MeasureSize(alignPosition2MeasureSize);

				maskFrame.setAlignPosition3DesignValX(alignPosition3DesignValX);
				maskFrame.setAlignPosition3DesignValY(alignPosition3DesignValY);
				maskFrame.setAlignPosition3MeasureValX(alignPosition3MeasureValX);
				maskFrame.setAlignPosition3MeasureValY(alignPosition3MeasureValY);
				maskFrame.setAlignPosition3DesignSize(alignPosition3DesignSize);
				maskFrame.setAlignPosition3MeasureSize(alignPosition3MeasureSize);

				maskFrame.setAlignPosition4DesignValX(alignPosition4DesignValX);
				maskFrame.setAlignPosition4DesignValY(alignPosition4DesignValY);
				maskFrame.setAlignPosition4MeasureValX(alignPosition4MeasureValX);
				maskFrame.setAlignPosition4MeasureValY(alignPosition4MeasureValY);
				maskFrame.setAlignPosition4DesignSize(alignPosition4DesignSize);
				maskFrame.setAlignPosition4MeasureSize(alignPosition4MeasureSize);
				
				maskFrame.setAlignHoleDesignRN1(alignHoleDesignRN1);
				maskFrame.setAlignHoleMeasurementRN1(alignHoleMeasurementRN1);
				maskFrame.setAlignHoleDesignRN2(alignHoleDesignRN2);
				maskFrame.setAlignHoleMeasurementRN2(alignHoleMeasurementRN2);
				maskFrame.setAlignHoleDesignRN3(alignHoleDesignRN3);
				maskFrame.setAlignHoleMeasurementRN3(alignHoleMeasurementRN3);
				maskFrame.setAlignHoleDesignRN4(alignHoleDesignRN4);
				maskFrame.setAlignHoleMeasurementRN4(alignHoleMeasurementRN4);
				
				maskFrame.setTexture(texture);
				maskFrame.setFlatness2(flatness2);

				maskFrame.setFrameState(constantMap.CreateState);
				maskFrame.setLastEventComment(eventInfo.getEventComment());
				maskFrame.setLastEventName(eventInfo.getEventName());
				maskFrame.setLastEventTime(eventInfo.getEventTime());
				maskFrame.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
				maskFrame.setLastEventUser(eventInfo.getEventUser());

				ExtendedObjectProxy.getMaskFrameService().create(eventInfo, maskFrame);
			}
		}

		return doc;
	}

	private List<Map<String, Object>> checkFrameName(String frameName)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT FRAMENAME, FRAMESTATE ");
		sql.append("  FROM CT_MASKFRAME ");
		sql.append(" WHERE FRAMENAME = :FRAMENAME ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("FRAMENAME", frameName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		return result;
	}

	private String getMaskLotName(String frameName) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT MASKLOTNAME, FRAMENAME ");
		sql.append("  FROM CT_MASKLOT ");
		sql.append(" WHERE FRAMENAME = :FRAMENAME ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("FRAMENAME", frameName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		String maskLotName = "";
		if (result.size() > 0)
		{
			maskLotName = ConvertUtil.getMapValueByName(result.get(0), "MASKLOTNAME");
		}
		else
		{
			throw new CustomException("FRAME-0001", frameName);
		}

		return maskLotName;
	}

	private void setMaskLotCreated(EventInfo eventInfo, String maskLotName) throws greenFrameDBErrorSignal, CustomException
	{
		eventInfo.setEventName("CreateFrame");
		MaskLot dataInfo = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskLotName });

		dataInfo.setMaskLotState("Created");
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		dataInfo.setLastEventUser(eventInfo.getEventUser());

		ExtendedObjectProxy.getMaskLotService().modify(eventInfo, dataInfo);
	}

	private void deassignMaskMaterial(EventInfo eventInfo, String maskLotName) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT MASKLOTNAME, MATERIALTYPE, MATERIALNAME ");
		sql.append("  FROM CT_MASKMATERIAL ");
		sql.append(" WHERE MASKLOTNAME = :MASKLOTNAME ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("MASKLOTNAME", maskLotName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			for (int i = 0; i < result.size(); i++)
			{
				String materialType = ConvertUtil.getMapValueByName(result.get(i), "MATERIALTYPE");
				String materialName = ConvertUtil.getMapValueByName(result.get(i), "MATERIALNAME");

				deassignExecute(eventInfo, maskLotName, materialType, materialName);
			}
		}
	}

	private void deassignExecute(EventInfo eventInfo, String maskLotName, String materialType, String materialName) throws greenFrameDBErrorSignal, CustomException
	{

		MaskMaterial dataInfo = ExtendedObjectProxy.getMaskMaterialService().selectByKey(false, new Object[] { maskLotName, materialType, materialName });
		if (StringUtil.equals(materialType, GenericServiceProxy.getConstantMap().MaterialType_Sheet)||StringUtil.equals(materialType,"Stick"))
		{
			MaskStick maskStick = ExtendedObjectProxy.getMaskStickService().selectByKey(true, new Object[] { dataInfo.getMaterialName() });
			setSheetScrapped(eventInfo, maskStick);
		}
		eventInfo.setEventName("DeassignMaterial");
		ExtendedObjectProxy.getMaskMaterialService().remove(eventInfo, dataInfo);
	}

	private void setSheetScrapped(EventInfo eventInfo, MaskStick dataInfo) throws CustomException
	{
		eventInfo.setEventName("ScrapStick");
		dataInfo.setStickState("Scrapped");
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		ExtendedObjectProxy.getMaskStickService().modify(eventInfo, dataInfo);
	}
}
