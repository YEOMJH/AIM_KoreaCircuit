package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.IPQCOpticalInspection;
import kr.co.aim.messolution.extended.object.management.data.IPQCPanelJudge;
import kr.co.aim.messolution.extended.object.management.data.IPQCRule;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

public class IPQCJudge extends SyncHandler {

	private static Log log = LogFactory.getLog(IPQCJudge.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String ipqcLotName = SMessageUtil.getBodyItemValue(doc, "IPQCLOTNAME", true);
		String opticalJudge = SMessageUtil.getBodyItemValue(doc, "OPTICALJUDGE", true);
		String electricalJudge = SMessageUtil.getBodyItemValue(doc, "ELECTRICALJUDGE", true);
		String judge = SMessageUtil.getBodyItemValue(doc, "JUDGE", true);
	
		List<Element> opticalList = SMessageUtil.getBodySequenceItemList(doc, "OPTICALLIST", false);
		
		
		//EventInfo IPQCJudge
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("IPQCJudge", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		log.debug("lotName : " + lotName);
		log.debug("ipqcLotName : " + ipqcLotName);
		
		String sSeq = "1";
		long seq = 1;
		
		//check state of lot		
		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);		
	
		CommonValidation.checkLotProcessStateRun(lotData);
		
		String beforeGrade = "";
		
		if(lotData.getLotGrade().equals("S"))
		{
			beforeGrade = "S";
		}
		else
		{
			beforeGrade = lotData.getUdfs().get("LOTDETAILGRADE").toString();
		}

		String oriGrade = lotData.getLotGrade();
		String durableName = lotData.getCarrierName();
		String sampleRule = "";
		
		//set lot grade/ detail grade
		String lotGrade = opticalJudge;
		String lotDetailGrade = "";
		if(opticalJudge.equals("OK"))
		{			
			lotGrade = "G";
			lotDetailGrade = judge;
		}
		else
		{
			lotGrade = "S";
		}
		
		if(electricalJudge.equals("S"))
		{
			lotGrade = "S";
		}
		
		
		//1,check exist lot from ct_ipqclot
		String sql = "SELECT SEQ, SAMPLERULE FROM CT_IPQCLOT L WHERE L.LOTSTATE = 'Released' and  L.IPQCLOTNAME = :IPQCLOTNAME ORDER BY 1 DESC";
		Map<String, String> args = new HashMap<String, String>();
		args.put("IPQCLOTNAME", ipqcLotName);
				
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		
		if (result == null || result.size() <= 0)
		{
			throw new CustomException("LOT-0315", ipqcLotName);
		}
		if(result != null && result.size() > 0)
		{
			sSeq = ConvertUtil.getMapValueByName(result.get(0), "SEQ");
			seq = Long.parseLong(sSeq);
			sampleRule = ConvertUtil.getMapValueByName(result.get(0), "SAMPLERULE");
		}		
		
		
		
		//2.IPQCPanelJudge save		
		beforeGrade = setPanelJudgeData("",ipqcLotName,seq,lotName,opticalJudge,electricalJudge,judge,eventInfo);
		
		//3.deassign lot/durable, if beforeGrade is not same judge 
		setLotData(lotName,lotGrade,beforeGrade,lotDetailGrade,eventInfo);
		
		//4.deassign lot/durable, if beforeGrade is not same judge 
		//reduce lotquantity from durable
		if(!beforeGrade.equals(lotDetailGrade))
		{
			//setDurablData(durableName,eventInfo);
		}		
				
		//eventInfo.setEventName("IPQCJudge");
		
		//5.save opticalList
		setOpticalData(opticalList,ipqcLotName,seq,lotName);
		
		//6.save electricalList
		//setElectricalData(electricalList,ipqcLotName,seq,lotName);
		
		//7.IPQCLot IPQCResult update
		setIPQCResult(ipqcLotName, seq, sSeq, sampleRule);

		
		return doc;
	}
	
	public void setIPQCResult(String ipqcLotName, long seq, String sSeq, String sampleRule) throws CustomException
	{
		//7.IPQCLot IPQCResult update
		//get IPQCRule
		String sminQuantity = "0";
		String smaxQuantity = "0";
		long minQuantity = 0;
		long maxQuantity = 0;
		long panelQuantity = 0;
		long allowngQuantity = 0;
		
		try
		{
			List<IPQCRule> result_rule = ExtendedObjectProxy.getIPQCRuleService().select(" SEQ = ? ", new Object[]{sampleRule});
			for (IPQCRule ipqcRule : result_rule)
			{
				sminQuantity = ipqcRule.getMinQuantity();
				smaxQuantity = ipqcRule.getMaxQuantity();
				
				minQuantity = Long.parseLong(sminQuantity);				
				maxQuantity = Long.parseLong(smaxQuantity);
				panelQuantity = ipqcRule.getPanelQuantity();
				allowngQuantity = ipqcRule.getAllowNGQuantity();
			}	
		}
		catch (greenFrameDBErrorSignal ex)
		{
			throw new CustomException("LOT-0316", sampleRule);
		}
		
		//get count of IPQCPanelJudge	
		//get different judge count of IPQCPanelJudge
		String sACount = "0";
		String sNCount = "0";
		String sDiffCount = "0";
		long aCount = 0;		
		long nCount = 0;
		long diffCount = 0;		
		
		String sql_judge = "SELECT COUNT(*) AS ACOUNT, "
				+ "     SUM(CASE  WHEN AFTERGRADE  IS NOT NULL THEN 1 ELSE 0 END) NCOUNT,"
				+ "     SUM(CASE  WHEN AFTERGRADE  IS NOT NULL AND BEFOREGRADE != AFTERGRADE THEN 1 ELSE 0 END) SCOUNT "
				+ "FROM CT_IPQCPANELJUDGE P WHERE P.IPQCLOTNAME = :IPQCLOTNAME AND SEQ = :SEQ ";
		Map<String, String> args_judge = new HashMap<String, String>();
		args_judge.put("IPQCLOTNAME", ipqcLotName);
		args_judge.put("SEQ", sSeq);
				
		List<Map<String, Object>> result_judge = GenericServiceProxy.getSqlMesTemplate().queryForList(sql_judge.toString(), args_judge);
		
		if(result_judge.size() > 0)
		{			
			sACount = ConvertUtil.getMapValueByName(result_judge.get(0), "ACOUNT"); //all panel quantity
			aCount = Long.parseLong(sACount);
			sNCount = ConvertUtil.getMapValueByName(result_judge.get(0), "NCOUNT"); //judged panel quantity
			nCount = Long.parseLong(sNCount);
			sDiffCount = ConvertUtil.getMapValueByName(result_judge.get(0), "SCOUNT"); //different judged panel quantity
			diffCount = Long.parseLong(sDiffCount);
		}
		
		//check IPQCRule
		if(!checkIPQCRule(minQuantity, maxQuantity, panelQuantity, aCount, nCount, diffCount, allowngQuantity))
		{
			return;
		}
		
		//check IPQCResult
		String IPQCResult = "OK";
		if(allowngQuantity < diffCount)
		{
			IPQCResult = "NG";
		}
		
		
		//save IPQCRule
		String sql_update = "UPDATE CT_IPQCLOT L "
				+ " SET FQCRESULT = :IPQCRESULT "
				+ " WHERE L.IPQCLOTNAME = :IPQCLOTNAME"
				+ "    AND SEQ = :SEQ";
		Map<String, String> args_update = new HashMap<String, String>();
		args_update.put("IPQCRESULT", IPQCResult);
		args_update.put("IPQCLOTNAME", ipqcLotName);
		args_update.put("SEQ", sSeq);
		GenericServiceProxy.getSqlMesTemplate().update(sql_update, args_update);
			

	}
	public boolean checkIPQCRule(long minQuantity,long maxQuantity,long panelQuantity,long aCount,long nCount, long diffCount, long allowngQuantity)
	{
		boolean bRet = true;
		
		//check values
		if(panelQuantity == 0 || aCount == 0 || nCount == 0)
		{
			return false;
		}
		//check min Quantity
		if(minQuantity > aCount)
		{
			bRet = false;
		}
		
		//check max Quantity
		if(maxQuantity < aCount)
		{
			bRet = false;
		}
		
		//check judge panel Quantity
		if(panelQuantity > nCount && allowngQuantity >= diffCount)
		{
			bRet = false;
		}
		
		//quantity == judged panel quantity -> return true
		if(aCount == nCount)
		{
			bRet = true;
		}
		
		return bRet;
	}
	
	public String setPanelJudgeData(String machineName,String ipqcLotName, long seq, String lotName, String opticalJudge, String electricalJudge, String judge, EventInfo eventInfo) throws CustomException
	{
		IPQCPanelJudge dataInfo;
		try
		{			
			dataInfo = ExtendedObjectProxy.getIPQCPanelJudgeService().selectByKey(false, new Object[]{ipqcLotName,seq, lotName});
			dataInfo.setOpticalJudge(opticalJudge);
			dataInfo.setElectricalJudge(electricalJudge);
			dataInfo.setAfterGrade(judge);
			dataInfo.setEventTime(eventInfo.getEventTime());
			dataInfo.setEventUser(eventInfo.getEventUser());
			dataInfo.setMachineName(machineName);
			ExtendedObjectProxy.getIPQCPanelJudgeService().update(dataInfo);
		}
		catch(greenFrameDBErrorSignal ex)
		{
			throw new CustomException("LOT-0317", lotName);
		}
		return dataInfo.getBeforeGrade();
	}
	
	public void setLotData(String lotName, String lotGrade, String beforeGrade, String lotDetailGrade, EventInfo eventInfo) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);	
		
		//update lotgrade/ lot detail grade
		lotData.setLotGrade(lotGrade);		
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		/*
		//remove carriername from lot
		if(!beforeGrade.equals(lotDetailGrade))
		{
			eventInfo.setEventName("DeassignCarrier");
			lotData.setCarrierName("");
		}
		*/
		Map<String, String> udfs = lotData.getUdfs();
		if(lotGrade.equals("S"))
		{
			udfs.put("LOTDETAILGRADE", "");
			lotData.setLotState(constantMap.Lot_Scrapped);
		}
		else
		{
			udfs.put("LOTDETAILGRADE", lotDetailGrade);
			lotData.setLotState(constantMap.Lot_Released);
			ProductRequest newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfo, lotData.getProductRequestName(), 0, 1);
			
			if(newProductRequestData.getProductRequestState().equals("Completed") && (newProductRequestData.getPlanQuantity() > newProductRequestData.getFinishedQuantity() + newProductRequestData.getScrappedQuantity()))
			{
				EventInfo newEventInfo = eventInfo;
				newEventInfo.setEventName("Release");
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeReleased(newEventInfo, lotData.getProductRequestName());
			}
		}

		lotData.setUdfs(udfs);
		
		LotServiceProxy.getLotService().update(lotData);
		
		//Set Event
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.setUdfs(udfs);
		
		LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
	}
	
	public void setDurablData(String durableName, EventInfo eventInfo) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
		long lotQuantity = 0;
		
		List<Map<String, Object>> result_lot = MESLotServiceProxy.getLotServiceUtil().getLotListByTray(durableData.getKey().getDurableName());
		if(result_lot != null)
		{
			lotQuantity = result_lot.size();
		}				
		
		durableData.setLotQuantity(lotQuantity);
		
		Map<String, String> udfs_durable = durableData.getUdfs();
		durableData.setUdfs(udfs_durable);
		
		DurableServiceProxy.getDurableService().update(durableData);
		
		//Set Event		
		kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo_durable = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
		setEventInfo_durable.setUdfs(udfs_durable);
		
		DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo_durable);
	}
	public void setOpticalData(List<Element> opticalList, String ipqcLotName, long seq, String lotName) throws CustomException
	{
		//delete CT_IPQCOPTICALINSPECTION
		String sql_opt = "DELETE FROM CT_IPQCOPTICALINSPECTION MO WHERE MO.IPQCLOTNAME = :IPQCLOTNAME AND MO.SEQ = :SEQ AND MO.PANELNAME = :PANELNAME ";
		Map<String, Object> args_opt = new HashMap<String, Object>();
		args_opt.put("IPQCLOTNAME", ipqcLotName);
		args_opt.put("SEQ", seq);
		args_opt.put("PANELNAME", lotName);			
		GenericServiceProxy.getSqlMesTemplate().update(sql_opt, args_opt);
		
		//3.save opticalList
		for (Element defectData : opticalList) 
		{
			String panelName = lotName; 
			String start_Time = defectData.getChildText("START_TIME");
			String brightness = defectData.getChildText("BRIGHTNESS");
			String x = defectData.getChildText("X");
			String y = defectData.getChildText("Y");
			String stop_Time = defectData.getChildText("STOP_TIME");
			String vr = defectData.getChildText("Vr");
			String vg = defectData.getChildText("Vg");
			String vb = defectData.getChildText("Vb");
			String i = defectData.getChildText("I");
			String x_Aft = defectData.getChildText("X_AFT");
			String y_Aft = defectData.getChildText("Y_AFT");
			String l_Aft = defectData.getChildText("L_AFT");
			String vr_Aft = defectData.getChildText("Vr_AFT");
			String vg_Aft = defectData.getChildText("Vg_AFT");
			String vb_Aft = defectData.getChildText("Vb_AFT");
			String i_Aft = defectData.getChildText("I_AFT");
			String result = defectData.getChildText("Result");	
			String color = defectData.getChildText("COLOR");	
			
			try
			{	
				IPQCOpticalInspection optdata = new IPQCOpticalInspection();
				optdata.setIpqcLotName(ipqcLotName);
				optdata.setSeq(seq);
				optdata.setPanelName(panelName);			
				optdata.setStart_Time(start_Time);
				optdata.setBrightness(brightness);
				optdata.setX(x);
				optdata.setY(y);
				optdata.setStop_Time(stop_Time);
				optdata.setVr(vr);
				optdata.setVg(vg);
				optdata.setVb(vb);
				optdata.setI(i);
				optdata.setX_Aft(x_Aft);
				optdata.setY_Aft(y_Aft);
				optdata.setL_Aft(l_Aft);
				optdata.setVr_Aft(vr_Aft);
				optdata.setVg_Aft(vg_Aft);
				optdata.setVb_Aft(vb_Aft);
				optdata.setI_Aft(i_Aft);
				optdata.setResult(result);
				optdata.setColor(color);
				
				ExtendedObjectProxy.getIPQCOpticalInspectionService().insert(optdata);
				
			}
			catch(greenFrameDBErrorSignal ex)
			{
				throw new CustomException("LOT-0007", ex.getMessage());
			}
		}
	}
	
//	public void setElectricalData(List<Element> electricalList, String ipqcLotName, long seq, String lotName) throws CustomException
//	{
//		//delete CT_IPQCELECTRICALINSPECTION
//		String sql_elect = "DELETE FROM CT_IPQCELECTRICALINSPECTION MO WHERE MO.IPQCLOTNAME = :IPQCLOTNAME AND MO.SEQ = :SEQ AND MO.PANELNAME = :PANELNAME ";
//		Map<String, Object> args_elect = new HashMap<String, Object>();
//		args_elect.put("IPQCLOTNAME", ipqcLotName);
//		args_elect.put("SEQ", seq);
//		args_elect.put("PANELNAME", lotName);			
//		GenericServiceProxy.getSqlMesTemplate().update(sql_elect, args_elect);
//		
//		if(electricalList.size() > 0 )
//		{
//			//4.save electricalList
//			for (Element defectData : electricalList) 
//			{
//				String panelName = lotName;  
//				String defectCode = defectData.getChildText("DEFECT_CODE");
//				String defectOrder = defectData.getChildText("DEFECT_ORDER");
//				String area = defectData.getChildText("AREA");
//				String quantity = defectData.getChildText("QUANTITY");
//				
//				log.debug("seq : " + seq);
//				log.debug("defectCode : " + defectCode);
//				log.debug("defectOrder : " + defectOrder);
//				log.debug("area" + area);
//				log.debug("quantity" + quantity);
//				
//				try
//				{	
//					IPQCElectricalInspection electdata = new IPQCElectricalInspection();
//					electdata.setIpqcLotName(ipqcLotName);
//					electdata.setSeq(seq);
//					electdata.setPanelName(panelName);
//					electdata.setDefectCode(defectCode);
//					electdata.setDefectOrder(defectOrder);
//					electdata.setArea(area);
//					if (!quantity.isEmpty())
//					{
//						electdata.setQuantity(Long.parseLong(quantity));
//					}
//					ExtendedObjectProxy.getIPQCElectricalInspectionService().insert(electdata);
//				}
//				catch(greenFrameDBErrorSignal ex)
//				{
//					throw new CustomException("LOT-0007", 
//							"You cannot select two of the same Code" );
//				}						
//			}
//		}
//	}
}