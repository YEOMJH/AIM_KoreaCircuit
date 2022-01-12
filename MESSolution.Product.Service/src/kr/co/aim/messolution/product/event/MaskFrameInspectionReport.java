package kr.co.aim.messolution.product.event;

import java.sql.Timestamp;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskFrame;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class MaskFrameInspectionReport extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", false);

		String frameName = SMessageUtil.getBodyItemValue(doc, "FRAMENAME", false);
		String recipeID = SMessageUtil.getBodyItemValue(doc, "RECIPEID", false);
		String maskType = SMessageUtil.getBodyItemValue(doc, "MASKTYPE", false);
		String maskStartTime = SMessageUtil.getBodyItemValue(doc, "MASKSTARTTIME", false);
		String maskEndTime = SMessageUtil.getBodyItemValue(doc, "MASKENDTIME", false);
		String maskJudge = SMessageUtil.getBodyItemValue(doc, "MASKJUDGE", false);

		String alignHoleDesignPositionX1 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_DESIGN_POSITIONX1", false);
		String alignHoleDesignPositionX2 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_DESIGN_POSITIONX2", false);
		String alignHoleDesignPositionX3 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_DESIGN_POSITIONX3", false);
		String alignHoleDesignPositionX4 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_DESIGN_POSITIONX4", false);
		String alignHoleDesignPositionY1 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_DESIGN_POSITIONY1", false);
		String alignHoleDesignPositionY2 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_DESIGN_POSITIONY2", false);
		String alignHoleDesignPositionY3 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_DESIGN_POSITIONY3", false);
		String alignHoleDesignPositionY4 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_DESIGN_POSITIONY4", false);

		String alignHoleMeasurementPositionX1 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_MEASUREMENT_POSITIONX1", false);
		String alignHoleMeasurementPositionX2 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_MEASUREMENT_POSITIONX2", false);
		String alignHoleMeasurementPositionX3 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_MEASUREMENT_POSITIONX3", false);
		String alignHoleMeasurementPositionX4 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_MEASUREMENT_POSITIONX4", false);
		String alignHoleMeasurementPositionY1 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_MEASUREMENT_POSITIONY1", false);
		String alignHoleMeasurementPositionY2 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_MEASUREMENT_POSITIONY2", false);
		String alignHoleMeasurementPositionY3 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_MEASUREMENT_POSITIONY3", false);
		String alignHoleMeasurementPositionY4 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_MEASUREMENT_POSITIONY4", false);

		String alignHoleDesignSize1 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_DESIGN_SIZE1", false);
		String alignHoleDesignSize2 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_DESIGN_SIZE2", false);
		String alignHoleDesignSize3 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_DESIGN_SIZE3", false);
		String alignHoleDesignSize4 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_DESIGN_SIZE4", false);

		String alignHoleMeasurementSize1 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_MEASUREMENT_SIZE1", false);
		String alignHoleMeasurementSize2 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_MEASUREMENT_SIZE2", false);
		String alignHoleMeasurementSize3 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_MEASUREMENT_SIZE3", false);
		String alignHoleMeasurementSize4 = SMessageUtil.getBodyItemValue(doc, "ALIGN_HOLE_MEASUREMENT_SIZE4", false);

		String outSideFrameLength1 = SMessageUtil.getBodyItemValue(doc, "OUTSIDEFRAMELENGTH1", false);
		String outSideFrameLength2 = SMessageUtil.getBodyItemValue(doc, "OUTSIDEFRAMELENGTH2", false);
		String outSideFrameWidth1 = SMessageUtil.getBodyItemValue(doc, "OUTSIDEFRAMEWIDTH1", false);
		String outSideFrameWidth2 = SMessageUtil.getBodyItemValue(doc, "OUTSIDEFRAMEWIDTH2", false);

		String internalFrameLength1 = SMessageUtil.getBodyItemValue(doc, "INTERNALFRAMELENGTH1", false);
		String internalFrameLength2 = SMessageUtil.getBodyItemValue(doc, "INTERNALFRAMELENGTH2", false);
		String internalFrameWidth1 = SMessageUtil.getBodyItemValue(doc, "INTERNALFRAMEWIDTH1", false);
		String internalFrameWidth2 = SMessageUtil.getBodyItemValue(doc, "INTERNALFRAMEWIDTH2", false);

		String frameCoverGrooveWidthMax = SMessageUtil.getBodyItemValue(doc, "FRAME_COVER_GROOVE_WIDTH_MAX", false);
		String frameCoverGrooveWidthMin = SMessageUtil.getBodyItemValue(doc, "FRAME_COVER_GROOVE_WIDTH_MIN", false);
		String frameCoverGrooveDepthMax = SMessageUtil.getBodyItemValue(doc, "FRAME_COVER_GROOVE_DEPTH_MAX", false);
		String frameCoverGrooveDepthMin = SMessageUtil.getBodyItemValue(doc, "FRAME_COVER_GROOVE_DEPTH_MIN", false);

		String frameHaulingGrooveWidthMax = SMessageUtil.getBodyItemValue(doc, "FRAME_HAULING_GROOVE_WIDTH_MAX", false);
		String frameHaulingGrooveWidthMin = SMessageUtil.getBodyItemValue(doc, "FRAME_HAULING_GROOVE_WIDTH_MIN", false);
		String frameHaulingGrooveDepthMax = SMessageUtil.getBodyItemValue(doc, "FRAME_HAULING_GROOVE_DEPTH_MAX", false);
		String frameHaulingGrooveDepthMin = SMessageUtil.getBodyItemValue(doc, "FRAME_HAULING_GROOVE_DEPTH_MIN", false);

		String thickness = SMessageUtil.getBodyItemValue(doc, "THICKNESS", false);
		String flatness = SMessageUtil.getBodyItemValue(doc, "FLATNESS", false);

		String alignHoleDesignRN1 = SMessageUtil.getBodyItemValue(doc, "ALIGNHOLEDESIGNRN1", false);
		String alignHoleMeasurementRN1 = SMessageUtil.getBodyItemValue(doc, "ALIGNHOLEMEASUREMENTRN1", false);
		String alignHoleDesignRN2 = SMessageUtil.getBodyItemValue(doc, "ALIGNHOLEDESIGNRN2", false);
		String alignHoleMeasurementRN2 = SMessageUtil.getBodyItemValue(doc, "ALIGNHOLEMEASUREMENTRN2", false);
		String alignHoleDesignRN3 = SMessageUtil.getBodyItemValue(doc, "ALIGNHOLEDESIGNRN3", false);
		String alignHoleMeasurementRN3 = SMessageUtil.getBodyItemValue(doc, "ALIGNHOLEMEASUREMENTRN3", false);
		String alignHoleDesignRN4 = SMessageUtil.getBodyItemValue(doc, "ALIGNHOLEDESIGNRN4", false);
		String alignHoleMeasurementRN4 = SMessageUtil.getBodyItemValue(doc, "ALIGNHOLEMEASUREMENTRN4", false);

		// Insert to Component History
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Mask3CDInspectionReport", getEventUser(), getEventComment(), "", "", "");
		eventInfo.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());

		String lastEventTimekey = TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime());
		Timestamp lastEventTime = eventInfo.getEventTime();
		String lastEventUser = eventInfo.getEventUser();
		String lastEventComment = eventInfo.getEventComment();
		String lastEventName = eventInfo.getEventName();

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		MaskFrame maskFrame = new MaskFrame();

		try
		{
			if(frameName.isEmpty())
			{
				frameName = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskName }).getFrameName();				
			}
			maskFrame = ExtendedObjectProxy.getMaskFrameService().selectByKey(false, new Object[] { frameName });
		}
		catch (Exception e)
		{
			maskFrame = null;
		}

		if (maskFrame == null)
		{
			maskFrame = new MaskFrame();

			maskFrame.setMachineName(machineName);
			maskFrame.setUnitName(unitName);
			maskFrame.setSubUnitName(subUnitName);
			maskFrame.setMaskLotName(maskName);

			maskFrame.setFrameName(frameName);
			maskFrame.setRecipeID(recipeID);
			maskFrame.setMaskType(maskType);

			maskFrame.setThickNess(thickness);
			maskFrame.setFlatNess(flatness);

			maskFrame.setCoverDepthMax(frameCoverGrooveDepthMax);
			maskFrame.setCoverDepthMin(frameCoverGrooveDepthMin);
			maskFrame.setCoverWidthMax(frameCoverGrooveWidthMax);
			maskFrame.setCoverWidthMin(frameCoverGrooveWidthMin);
			maskFrame.setHaulingDepthMax(frameHaulingGrooveDepthMax);
			maskFrame.setHaulingDepthMin(frameHaulingGrooveDepthMin);
			maskFrame.setHaulingWidthMax(frameHaulingGrooveWidthMax);
			maskFrame.setHaulingWidthMin(frameHaulingGrooveWidthMin);

			maskFrame.setOutside1Length(outSideFrameLength1);
			maskFrame.setOutside1Width(outSideFrameWidth1);
			maskFrame.setInternal1Length(internalFrameLength1);
			maskFrame.setInternal1Width(internalFrameWidth1);

			maskFrame.setOutside2Length(outSideFrameLength2);
			maskFrame.setOutside2Width(outSideFrameWidth2);
			maskFrame.setInternal2Length(internalFrameLength2);
			maskFrame.setInternal2Width(internalFrameWidth2);

			maskFrame.setAlignPosition1DesignValX(alignHoleDesignPositionX1);
			maskFrame.setAlignPosition1DesignValY(alignHoleDesignPositionY1);
			maskFrame.setAlignPosition1MeasureValX(alignHoleMeasurementPositionX1);
			maskFrame.setAlignPosition1MeasureValY(alignHoleMeasurementPositionY1);
			maskFrame.setAlignPosition1DesignSize(alignHoleDesignSize1);
			maskFrame.setAlignPosition1MeasureSize(alignHoleMeasurementSize1);

			maskFrame.setAlignPosition2DesignValX(alignHoleDesignPositionX2);
			maskFrame.setAlignPosition2DesignValY(alignHoleDesignPositionY2);
			maskFrame.setAlignPosition2MeasureValX(alignHoleMeasurementPositionX2);
			maskFrame.setAlignPosition2MeasureValY(alignHoleMeasurementPositionY2);
			maskFrame.setAlignPosition2DesignSize(alignHoleDesignSize2);
			maskFrame.setAlignPosition2MeasureSize(alignHoleMeasurementSize2);

			maskFrame.setAlignPosition3DesignValX(alignHoleDesignPositionX3);
			maskFrame.setAlignPosition3DesignValY(alignHoleDesignPositionY3);
			maskFrame.setAlignPosition3MeasureValX(alignHoleMeasurementPositionX3);
			maskFrame.setAlignPosition3MeasureValY(alignHoleMeasurementPositionY3);
			maskFrame.setAlignPosition3DesignSize(alignHoleDesignSize3);
			maskFrame.setAlignPosition3MeasureSize(alignHoleMeasurementSize3);

			maskFrame.setAlignPosition4DesignValX(alignHoleDesignPositionX4);
			maskFrame.setAlignPosition4DesignValY(alignHoleDesignPositionY4);
			maskFrame.setAlignPosition4MeasureValX(alignHoleMeasurementPositionX4);
			maskFrame.setAlignPosition4MeasureValY(alignHoleMeasurementPositionY4);
			maskFrame.setAlignPosition4DesignSize(alignHoleDesignSize4);
			maskFrame.setAlignPosition4MeasureSize(alignHoleMeasurementSize4);

			maskFrame.setAlignHoleDesignRN1(alignHoleDesignRN1);
			maskFrame.setAlignHoleMeasurementRN1(alignHoleMeasurementRN1);
			maskFrame.setAlignHoleDesignRN2(alignHoleDesignRN2);
			maskFrame.setAlignHoleMeasurementRN2(alignHoleMeasurementRN2);
			maskFrame.setAlignHoleDesignRN3(alignHoleDesignRN3);
			maskFrame.setAlignHoleMeasurementRN3(alignHoleMeasurementRN3);
			maskFrame.setAlignHoleDesignRN4(alignHoleDesignRN4);
			maskFrame.setAlignHoleMeasurementRN4(alignHoleMeasurementRN4);

			maskFrame.setFrameState(constantMap.CreateState);
			maskFrame.setLastEventComment(lastEventComment);
			maskFrame.setLastEventName(lastEventName);
			maskFrame.setLastEventTime(lastEventTime);
			maskFrame.setLastEventTimeKey(lastEventTimekey);
			maskFrame.setLastEventUser(lastEventUser);
			maskFrame.setMaskTestTime(lastEventTime);
			//maskFrame.setTexture(frameName.substring(7, 8));

			ExtendedObjectProxy.getMaskFrameService().create(eventInfo, maskFrame);
		}
		else
		{
			maskFrame.setMachineName(machineName);
			maskFrame.setUnitName(unitName);
			maskFrame.setSubUnitName(subUnitName);
			maskFrame.setMaskLotName(maskName);

			maskFrame.setFrameName(frameName);
			maskFrame.setRecipeID(recipeID);
			maskFrame.setMaskType(maskType);

			maskFrame.setThickNess(thickness);
			maskFrame.setFlatNess(flatness);

			maskFrame.setCoverDepthMax(frameCoverGrooveDepthMax);
			maskFrame.setCoverDepthMin(frameCoverGrooveDepthMin);
			maskFrame.setCoverWidthMax(frameCoverGrooveWidthMax);
			maskFrame.setCoverWidthMin(frameCoverGrooveWidthMin);
			maskFrame.setHaulingDepthMax(frameHaulingGrooveDepthMax);
			maskFrame.setHaulingDepthMin(frameHaulingGrooveDepthMin);
			maskFrame.setHaulingWidthMax(frameHaulingGrooveWidthMax);
			maskFrame.setHaulingWidthMin(frameHaulingGrooveWidthMin);

			maskFrame.setOutside1Length(outSideFrameLength1);
			maskFrame.setOutside1Width(outSideFrameWidth1);
			maskFrame.setInternal1Length(internalFrameLength1);
			maskFrame.setInternal1Width(internalFrameWidth1);

			maskFrame.setOutside2Length(outSideFrameLength2);
			maskFrame.setOutside2Width(outSideFrameWidth2);
			maskFrame.setInternal2Length(internalFrameLength2);
			maskFrame.setInternal2Width(internalFrameWidth2);

			maskFrame.setAlignPosition1DesignValX(alignHoleDesignPositionX1);
			maskFrame.setAlignPosition1DesignValY(alignHoleDesignPositionY1);
			maskFrame.setAlignPosition1MeasureValX(alignHoleMeasurementPositionX1);
			maskFrame.setAlignPosition1MeasureValY(alignHoleMeasurementPositionY1);
			maskFrame.setAlignPosition1DesignSize(alignHoleDesignSize1);
			maskFrame.setAlignPosition1MeasureSize(alignHoleMeasurementSize1);

			maskFrame.setAlignPosition2DesignValX(alignHoleDesignPositionX2);
			maskFrame.setAlignPosition2DesignValY(alignHoleDesignPositionY2);
			maskFrame.setAlignPosition2MeasureValX(alignHoleMeasurementPositionX2);
			maskFrame.setAlignPosition2MeasureValY(alignHoleMeasurementPositionY2);
			maskFrame.setAlignPosition2DesignSize(alignHoleDesignSize2);
			maskFrame.setAlignPosition2MeasureSize(alignHoleMeasurementSize2);

			maskFrame.setAlignPosition3DesignValX(alignHoleDesignPositionX3);
			maskFrame.setAlignPosition3DesignValY(alignHoleDesignPositionY3);
			maskFrame.setAlignPosition3MeasureValX(alignHoleMeasurementPositionX3);
			maskFrame.setAlignPosition3MeasureValY(alignHoleMeasurementPositionY3);
			maskFrame.setAlignPosition3DesignSize(alignHoleDesignSize3);
			maskFrame.setAlignPosition3MeasureSize(alignHoleMeasurementSize3);

			maskFrame.setAlignPosition4DesignValX(alignHoleDesignPositionX4);
			maskFrame.setAlignPosition4DesignValY(alignHoleDesignPositionY4);
			maskFrame.setAlignPosition4MeasureValX(alignHoleMeasurementPositionX4);
			maskFrame.setAlignPosition4MeasureValY(alignHoleMeasurementPositionY4);
			maskFrame.setAlignPosition4DesignSize(alignHoleDesignSize4);
			maskFrame.setAlignPosition4MeasureSize(alignHoleMeasurementSize4);

			maskFrame.setAlignHoleDesignRN1(alignHoleDesignRN1);
			maskFrame.setAlignHoleMeasurementRN1(alignHoleMeasurementRN1);
			maskFrame.setAlignHoleDesignRN2(alignHoleDesignRN2);
			maskFrame.setAlignHoleMeasurementRN2(alignHoleMeasurementRN2);
			maskFrame.setAlignHoleDesignRN3(alignHoleDesignRN3);
			maskFrame.setAlignHoleMeasurementRN3(alignHoleMeasurementRN3);
			maskFrame.setAlignHoleDesignRN4(alignHoleDesignRN4);
			maskFrame.setAlignHoleMeasurementRN4(alignHoleMeasurementRN4);

			maskFrame.setFrameState(constantMap.CreateState);
			maskFrame.setLastEventComment(eventInfo.getEventComment());
			maskFrame.setLastEventName(eventInfo.getEventName());
			maskFrame.setLastEventTime(eventInfo.getEventTime());
			maskFrame.setLastEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
			maskFrame.setLastEventUser(eventInfo.getEventUser());

			ExtendedObjectProxy.getMaskFrameService().modify(eventInfo, maskFrame);
		}
	}
}
