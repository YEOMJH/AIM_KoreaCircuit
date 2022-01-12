package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.FQCLot;
import kr.co.aim.messolution.extended.object.management.data.FQCPanelJudge;
import kr.co.aim.messolution.extended.object.management.data.FQCRule;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class FQCJudge extends SyncHandler {

	private static Log log = LogFactory.getLog(FQCJudge.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String fqcLotName = SMessageUtil.getBodyItemValue(doc, "FQCLOTNAME", true);
		String opticalJudge = SMessageUtil.getBodyItemValue(doc, "OPTICALJUDGE", true);
		String electricalJudge = SMessageUtil.getBodyItemValue(doc, "ELECTRICALJUDGE", true);
		String judge = SMessageUtil.getBodyItemValue(doc, "JUDGE", true);

		List<Element> opticalList = SMessageUtil.getBodySequenceItemList(doc, "OPTICALLIST", true);
		List<Element> electricalList = SMessageUtil.getBodySequenceItemList(doc, "ELECTRICALLIST", false);

		// EventInfo FQCJudge
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("FQCJudge", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		log.debug("lotName : " + lotName);
		log.debug("fqcLotName : " + fqcLotName);

		// check state of lot
		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);

		CommonValidation.checkLotProcessStateRun(lotData);
		CommonValidation.checkLotHoldState(lotData);
		CommonValidation.checkLotState(lotData);

		String beforeGrade = "";

		if (lotData.getLotGrade().equals("S"))
			beforeGrade = "S";
		else
			beforeGrade = lotData.getUdfs().get("LOTDETAILGRADE");

		// set lot grade/ detail grade
		String lotGrade = opticalJudge;
		String lotDetailGrade = "";

		if (judge.equals("S") || judge.equals("N"))
		{
			lotGrade = "S";
			lotDetailGrade = "";
		}
		else if(judge.equals("S1"))//caixu 2020/12/10 ADD S1
		{
			lotGrade = "S";
			lotDetailGrade =judge;
		}
		else
		{
			lotGrade = "G";
			lotDetailGrade = judge;
		}

		// check exist lot from ct_fqclot
		List<FQCLot> fqcLotList = ExtendedObjectProxy.getFQCLotService().getFirstFQCLotData(fqcLotName);

		if (fqcLotList == null)
			throw new CustomException("LOT-0314", fqcLotName);

		long seq = fqcLotList.get(0).getSeq();
		String sampleRule = fqcLotList.get(0).getSampleRule();

		// FQCPanelJudge save
		FQCPanelJudge fqcPanelJudgeData = ExtendedObjectProxy.getFQCPanelJudgeService().updateFQCPanelJudgeData(eventInfo, fqcLotName, seq, lotName, opticalJudge, electricalJudge, judge, machineName);
		beforeGrade = fqcPanelJudgeData.getBeforeGrade();

		// deassign lot/durable, if beforeGrade is not same judge
		setLotData(eventInfo, lotName, lotGrade, beforeGrade, lotDetailGrade);

		// Save Optical Inspection
		setOpticalData(eventInfo, opticalList, fqcLotName, seq, lotName);

		// Save Electrical Inspection
		setElectricalData(eventInfo, electricalList, fqcLotName, seq, lotName);

		// save FQCInspection
		setFQCInspectionData(eventInfo, electricalList, seq, lotName);

		// FQCLot FQCResult update
		setFQCResult(eventInfo, fqcLotName, seq, sampleRule);

		return doc;
	}

	public void setFQCResult(EventInfo eventInfo, String fqcLotName, long seq, String sampleRule) throws CustomException
	{
		// FQCLot FQCResult update
		// get FQCRule
		FQCRule fqcRuleData = ExtendedObjectProxy.getFQCRuleService().getFQCRuleData(sampleRule);

		long minQuantity = Long.parseLong(fqcRuleData.getMinQuantity());
		long maxQuantity = Long.parseLong(fqcRuleData.getMaxQuantity());
		long panelQuantity = fqcRuleData.getPanelQuantity();
		long allowngQuantity = fqcRuleData.getAllowNGQuantity();

		// get count of FQCPanelJudge
		// get different judge count of FQCPanelJudge
		List<Map<String, Object>> result_judge = ExtendedObjectProxy.getFQCPanelJudgeService().getFQCPanelJudgeCount(fqcLotName, Long.toString(seq));

		if (result_judge.size() > 0)
		{
			long aCount = Long.parseLong(ConvertUtil.getMapValueByName(result_judge.get(0), "ACOUNT"));// all panel quantity
			long nCount = Long.parseLong(ConvertUtil.getMapValueByName(result_judge.get(0), "NCOUNT"));// judged panel quantity
			long diffCount = Long.parseLong(ConvertUtil.getMapValueByName(result_judge.get(0), "SCOUNT"));// different judged panel quantity

			log.debug("allPanelQuantity : " + aCount);
			log.debug("JudgedPanelQuantity : " + nCount);
			log.debug("DifferentJudgedPanelQuantity : " + diffCount);

			// check FQCRule
			if (!checkFQCRule(minQuantity, maxQuantity, panelQuantity, aCount, nCount, diffCount, allowngQuantity))
				return;

			// check FQCResult
			String FQCResult = "OK";

			if (allowngQuantity < diffCount)
				FQCResult = "NG";

			log.debug("FQCResult : " + FQCResult);

			ExtendedObjectProxy.getFQCLotService().updateFQCLotData(eventInfo, fqcLotName, seq, FQCResult);
		}
	}

	public boolean checkFQCRule(long minQuantity, long maxQuantity, long panelQuantity, long aCount, long nCount, long diffCount, long allowngQuantity)
	{
		boolean bRet = true;

		// check min Quantity
		if (minQuantity > aCount)
			bRet = false;

		// check max Quantity
		if (maxQuantity < aCount)
			bRet = false;

		// check judge panel Quantity
		if (panelQuantity > nCount && allowngQuantity >= diffCount)
			bRet = false;

		// quantity == judged panel quantity -> return true
		if (aCount == nCount)
			bRet = true;

		return bRet;
	}

	public void setLotData(EventInfo eventInfo, String lotName, String lotGrade, String beforeGrade, String lotDetailGrade) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);

		// update lotgrade/ lot detail grade
		lotData.setLotGrade(lotGrade);
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		// Set Event
		SetEventInfo setEventInfo = new SetEventInfo();

		if (lotGrade.equals("S"))
		{
			setEventInfo.getUdfs().put("LOTDETAILGRADE", lotDetailGrade);
			lotData.setLotState(constantMap.Lot_Released);//2020/11/29 FQCJudge s not Scrapped
		}
		else
		{
			setEventInfo.getUdfs().put("LOTDETAILGRADE", lotDetailGrade);
			lotData.setLotState(constantMap.Lot_Released);
		}

		LotServiceProxy.getLotService().update(lotData);
		LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
	}

	public void setDurablData(String durableName, EventInfo eventInfo) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
		long panelQuantity = 0;

		List<Lot> panelDataList = MESLotServiceProxy.getLotServiceUtil().getLotDataListByTray(durableData.getKey().getDurableName());

		if (panelDataList != null)
			panelQuantity = panelDataList.size();

		durableData.setLotQuantity(panelQuantity);
		DurableServiceProxy.getDurableService().update(durableData);

		// Set Event
		kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
		setEventInfo.setUdfs(durableData.getUdfs());

		DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
	}

	public void setOpticalData(EventInfo eventInfo, List<Element> opticalList, String fqcLotName, long seq, String lotName) throws CustomException
	{
		ExtendedObjectProxy.getFQCOpticalInspectionService().deleteFQCOpticalInspectionData(fqcLotName, seq, lotName);

		for (Element defectData : opticalList)
		{
			try
			{
				ExtendedObjectProxy.getFQCOpticalInspectionService().createFQCOpticalInspectionData(eventInfo, fqcLotName, seq, lotName, defectData);
			}
			catch (greenFrameDBErrorSignal ex)
			{
				throw new CustomException("LOT-0007", ex.getMessage());
			}
		}
	}

	public void setElectricalData(EventInfo eventInfo, List<Element> electricalList, String fqcLotName, long seq, String lotName) throws CustomException
	{
		ExtendedObjectProxy.getFQCElectricalInspectionService().deleteFQCElectricalInspectionData(fqcLotName, seq, lotName);

		for (Element defectData : electricalList)
		{
			try
			{
				log.debug("seq : " + seq);
				log.debug("defectCode : " + defectData.getChildText("DEFECT_CODE"));
				log.debug("defectOrder : " + defectData.getChildText("DEFECT_ORDER"));
				log.debug("area" + defectData.getChildText("AREA"));
				log.debug("quantity" + defectData.getChildText("QUANTITY"));

				ExtendedObjectProxy.getFQCElectricalInspectionService().createFQCElectricalInspectionData(eventInfo, fqcLotName, seq, lotName, defectData);
			}
			catch (greenFrameDBErrorSignal ex)
			{
				throw new CustomException("LOT-0313");
			}
		}
	}

	public void setFQCInspectionData(EventInfo eventInfo, List<Element> electricalList, long seq, String lotName) throws CustomException
	{
		for (Element defectData : electricalList)
		{
			try
			{
				log.debug("SEQ : " + seq);
				log.debug("PanelName : " + lotName);
				log.debug("DefectCode : " + defectData.getChildText("DEFECT_CODE"));
				log.debug("DefectOrder : " + defectData.getChildText("DEFECT_ORDER"));
				log.debug("Area" + defectData.getChildText("AREA"));
				log.debug("Quantity" + defectData.getChildText("QUANTITY"));

				ExtendedObjectProxy.getFQCInspectionService().deleteFQCInspection(eventInfo, seq, lotName, defectData.getChildText("DEFECT_CODE"));

				ExtendedObjectProxy.getFQCInspectionService().createFQCInspection(eventInfo, seq, lotName, defectData.getChildText("DEFECT_CODE"), defectData.getChildText("DEFECT_ORDER"),
						defectData.getChildText("AREA"), Integer.parseInt(defectData.getChildText("QUANTITY")));
			}
			catch (greenFrameDBErrorSignal ex)
			{
				throw new CustomException("LOT-0313");
			}
		}
	}
}