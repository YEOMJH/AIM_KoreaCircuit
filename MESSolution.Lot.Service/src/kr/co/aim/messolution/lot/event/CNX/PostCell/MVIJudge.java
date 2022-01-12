package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.EnumDefValue;
import kr.co.aim.messolution.extended.object.management.data.MVIDefectCode;
import kr.co.aim.messolution.extended.object.management.data.MVIPanelJudge;
import kr.co.aim.messolution.extended.object.management.data.SVIPanelJudge;
import kr.co.aim.messolution.extended.object.management.data.SVIPickInfo;
import kr.co.aim.messolution.extended.object.management.data.SorterPickPrintInfo;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.lot.management.policy.util.LotPFIValueSetter;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.iter.NodeStackUtil;
import kr.co.aim.greentrack.processflow.management.iter.PFIValueSetter;
import kr.co.aim.greentrack.processflow.management.iter.ProcessFlowIterator;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class MVIJudge extends SyncHandler {

	private static Log log = LogFactory.getLog(MVIJudge.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String opticalJudge = SMessageUtil.getBodyItemValue(doc, "OPTICALJUDGE", true);
		String electricalJudge = SMessageUtil.getBodyItemValue(doc, "ELECTRICALJUDGE", true);
		String tpJudge = SMessageUtil.getBodyItemValue(doc, "TPJUDGE", false);
		String judge = SMessageUtil.getBodyItemValue(doc, "JUDGE", true);
		String trayName = SMessageUtil.getBodyItemValue(doc, "TRAYNAME",false );
		String tJudgeGrade=SMessageUtil.getBodyItemValue(doc, "GRADE",false );
		String position = SMessageUtil.getBodyItemValue(doc, "POSITION", false);
		String rx = SMessageUtil.getBodyItemValue(doc, "RX", true);
		String ry = SMessageUtil.getBodyItemValue(doc, "RY", true);
		String gx = SMessageUtil.getBodyItemValue(doc, "GX", true);
		String gy = SMessageUtil.getBodyItemValue(doc, "GY", true);
		String bx = SMessageUtil.getBodyItemValue(doc, "BX", true);
		String by = SMessageUtil.getBodyItemValue(doc, "BY", true);

		List<Element> opticalList = SMessageUtil.getBodySequenceItemList(doc, "OPTICALLIST", false);
		List<Element> electricalList = SMessageUtil.getBodySequenceItemList(doc, "ELECTRICALLIST", false);
		List<Element> tpInspectionList = SMessageUtil.getBodySequenceItemList(doc, "TPINSPECTIONLIST", false);
		List<Element> insertCodeList = SMessageUtil.getBodySequenceItemList(doc, "INSERTCODELIST", false);

		// EventInfo MVIJudge
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("MVIJudge", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(ConvertUtil.getCurrTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

		EventInfo eventInfoTrackOut = EventInfoUtil.makeEventInfo("TrackOut", getEventUser(), getEventComment(), null, null);
		eventInfoTrackOut.setEventTimeKey(ConvertUtil.getCurrTimeKey());
		eventInfoTrackOut.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		EventInfo eventInfoScrap = EventInfoUtil.makeEventInfo("Scrap", getEventUser(), getEventComment(), null, null);
		eventInfoScrap.setEventTimeKey(ConvertUtil.getCurrTimeKey());
		eventInfoScrap.setEventTime(TimeStampUtil.getCurrentTimestamp());

		EventInfo futureActionEventInfo = (EventInfo) ObjectUtil.copyTo(eventInfoTrackOut);
		futureActionEventInfo.setEventName("Delete");

		log.debug("lotName : " + lotName);

		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
		ProcessOperationSpec processOperationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());
		String machineName = lotData.getMachineName();

		if (!processOperationData.getDetailProcessOperationType().equals("MVI") && !processOperationData.getDetailProcessOperationType().equals("SVI/MVI"))
			throw new CustomException("MVI-0003");

		CommonValidation.checkLotProcessStateRun(lotData);
		CommonValidation.checkLotHoldState(lotData);
		CommonValidation.checkLotState(lotData);

		// set lot grade/ detail grade
		String lotGrade = "";
		String lotDetailGrade = ""; // electricalJudge;

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
			if (judge.equals("P"))
			{
				lotGrade = "P";
				lotDetailGrade = "";
			}
			else if (judge.equals("R"))
			{
				lotGrade = "R";
				lotDetailGrade = "";
			}else if (judge.equals("T"))
			{
				lotGrade = "T";
				lotDetailGrade = tJudgeGrade;
			}
			else
			{
				lotGrade = "G";
				lotDetailGrade = judge;
			}
		}

		long seq = 1;

		// 1.check exist lot from MVIPanelJudge
		List<MVIPanelJudge> mviPanelJudgeDataList = ExtendedObjectProxy.getMVIPanelJudgeService().getMVIPanelJudgeDataList(lotName);

		if (mviPanelJudgeDataList != null)
			seq = mviPanelJudgeDataList.get(0).getSeq();

		// 2.MVIPanelJudge save
		ExtendedObjectProxy.getMVIPanelJudgeService().setPanelJudgeData(seq, lotName, opticalJudge, electricalJudge, judge, eventInfo, eventInfoTrackOut, machineName, tpJudge);

		if (electricalJudge.equals("P"))
			lotDetailGrade = "";

		// 3.lot grade/ detail grade update
		setLotData(lotName, lotGrade, lotDetailGrade, eventInfo, eventInfoScrap);

		// 4.save opticalList
		if (opticalList.size() > 0)
		{
			// 4.1 get color gamut
			BigDecimal Rx = new BigDecimal(rx);
			BigDecimal Ry = new BigDecimal(ry);
			BigDecimal Gx = new BigDecimal(gx);
			BigDecimal Gy = new BigDecimal(gy);
			BigDecimal Bx = new BigDecimal(bx);
			BigDecimal By = new BigDecimal(by);
			BigDecimal k = new BigDecimal("0.3164");

			BigDecimal Gy_By = Gy.subtract(By);
			BigDecimal By_Ry = By.subtract(Ry);
			BigDecimal Ry_Gy = Ry.subtract(Gy);

			// (Rx*(Gy-By) + Gx*(By-Ry) + Bx*(Ry-Gy))/2/0.1582
			BigDecimal gamut = Rx.multiply(Gy_By).add(Gx.multiply(By_Ry)).add(Bx.multiply(Ry_Gy)).divide(k, 20, BigDecimal.ROUND_HALF_UP);

			// 4.2 save opticalList
			setOpticalData(opticalList, seq, lotName, gamut.toString(), eventInfo);
		}

		// 5.save electricalList
		if (electricalList.size() > 0)
		{
			setElectricalData(electricalList, seq, lotName, eventInfo);
			setUserDefectHist(machineName, lotName, judge, electricalList, eventInfo);
		}
		
		// insert enumvalue DescendedCode
		if (insertCodeList.size() > 0)
		{
			setEnumDescendedCode(machineName, lotName, judge, insertCodeList, eventInfo);
		}
		
		// 5.1 save tpList
		if (tpInspectionList.size() > 0)
			setTPInspectionData(tpInspectionList, seq, lotName, eventInfo);

		// GetPosition{前期作业，临时屏蔽}
		if (!StringUtils.equals(lotGrade, "P"))
		{
			Durable trayInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
			DurableSpec traySpec = GenericServiceProxy.getSpecUtil().getDurableSpec(lotData.getFactoryName(), trayInfo.getDurableSpecName(), trayInfo.getDurableSpecVersion());

			int xCount = Integer.parseInt(traySpec.getUdfs().get("XCOUNT").toString());
			int yCount = Integer.parseInt(traySpec.getUdfs().get("YCOUNT").toString());

			List<Map<String, Object>> lotList = MESLotServiceProxy.getLotServiceUtil().getLotListByTray(trayName);

			boolean positionFind = false;

			for (int i = 0; i < yCount; i++)
			{
				for (int j = 0; j < xCount; j++)
				{
					String newPosition = (char) (65 + i) + "0" + Integer.valueOf(j + 1).toString();

					boolean panelCheck = true;

					for (int k = 0; k < lotList.size(); k++)
					{
						Map<String, Object> lotInfo = lotList.get(k);
						String panelPosition = lotInfo.get("POSITION").toString();

						if (panelPosition.equals(newPosition))
						{
							panelCheck = false;
							break;
						}
					}

					if (panelCheck)
					{
						position = newPosition;
						positionFind = true;
						break;
					}
				}

				if (positionFind)
				{
					break;
				}
			}
		}

		// 6.ReworkCheck
		//ExtendedObjectProxy.getReworkProductService().setReworkCountData(lotData, processOperationData, lotGrade);
		//AddPickInfo caixu 2020/11/26 
		setSortPickInfo(lotName,judge,electricalList, eventInfo);

		// 7.Assign
		assignPanelAndTrackOut(lotName, trayName, position, judge, machineName, eventInfoTrackOut, futureActionEventInfo);
        //{ start caixu 2020/12/08  TrackOut After Scrap
		if (lotGrade.equals("S"))
		{
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
			Lot oldLot = (Lot)ObjectUtil.copyTo(lot);
			List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();
			List<Object[]> updateLotArgListScrap = new ArrayList<Object[]>();


			List<Object> lotBindList = new ArrayList<Object>();

			lotBindList.add(GenericServiceProxy.getConstantMap().Lot_Scrapped);
			lotBindList.add(eventInfoScrap.getEventName());
			lotBindList.add(eventInfoScrap.getEventTimeKey());
			lotBindList.add(eventInfoScrap.getEventTime());
			lotBindList.add(eventInfoScrap.getEventUser());
			lotBindList.add(eventInfoScrap.getEventComment());
			lotBindList.add(GenericServiceProxy.getConstantMap().Flag_N);
			lotBindList.add("Auto Scrap");
			lotBindList.add(lotName);

			updateLotArgListScrap.add(lotBindList.toArray());

			// History
			lot.setLotState(GenericServiceProxy.getConstantMap().Lot_Scrapped);
			lot.setReasonCode("Auto Scrap");
			lot.setLastEventName(eventInfoScrap.getEventName());
			lot.setLastEventTime(eventInfoScrap.getEventTime());
			lot.setLastEventTimeKey(eventInfoScrap.getEventTimeKey());
			lot.setLastEventComment(eventInfoScrap.getEventComment());
			lot.setLastEventUser(eventInfoScrap.getEventUser());

			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
			
			updateLotHistoryList.add(lotHistory);
			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE LOT ");
			sql.append("   SET LOTSTATE = ?, ");
			sql.append("       LASTEVENTNAME = ?, ");
			sql.append("       LASTEVENTTIMEKEY = ?, ");
			sql.append("       LASTEVENTTIME = ?, ");
			sql.append("       LASTEVENTUSER = ?, ");
			sql.append("       LASTEVENTCOMMENT = ?, ");
			sql.append("       LASTEVENTFLAG = ?, ");
			sql.append("       REASONCODE = ? ");
			sql.append(" WHERE LOTNAME = ? ");

			MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), updateLotArgListScrap);
			try 
			{
				CommonUtil.executeBatch("insert", updateLotHistoryList);
			} 
			catch (Exception e) 
			{
				log.error(e.getMessage());
				throw new CustomException(e.getCause());
			}
			String WO = lot.getProductRequestName();

			ProductRequest newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfoScrap, WO, 1, 0);

			/*if (newProductRequestData.getPlanQuantity() == newProductRequestData.getFinishedQuantity() + newProductRequestData.getScrappedQuantity())
			{
				EventInfo newEventInfo = (EventInfo) ObjectUtil.copyTo(eventInfoScrap);
				newEventInfo.setEventName("Complete");
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompleted(newEventInfo, WO);
			}*///2020/12/23 caixu 屏蔽自动Complete的功能	
		}//end}
		if(judge.equals("S1"))//CAICU 2020/12/10 Add SI Hold
		{
			trayGroupHoldForS1(lotName);
		}
		//fQC Check MVIJuage or SVIJUdge
		String sqe = ExtendedObjectProxy.getSVIPanelJudgeService().getSVILotSeq(lotName);	
		SVIPanelJudge sviPanelJudge = new SVIPanelJudge();
		sviPanelJudge.setPanelName(lotName);
		sviPanelJudge.setSeq(Long.parseLong(sqe));
		sviPanelJudge.setBeforeGrade("");
		sviPanelJudge.setBeforeJudge("");//lotgrade
		sviPanelJudge.setSVIPanelGrade(judge);
		sviPanelJudge.setSVIPanelJudge(lotGrade);
		sviPanelJudge.setEventUser(eventInfo.getEventUser());
		sviPanelJudge.setEventTime(eventInfo.getEventTime());
		sviPanelJudge.setEventName(eventInfo.getEventName());
		sviPanelJudge.setLastEventTimeKey(eventInfo.getEventTimeKey());
		sviPanelJudge.setMachineName(machineName);
		ExtendedObjectProxy.getSVIPanelJudgeService().create(eventInfo, sviPanelJudge);
		
		return doc;
	}

	private void setEnumDescendedCode(String machineName, String lotName, String judge, List<Element> insertCodeList,
			EventInfo eventInfo) throws FrameworkErrorSignal, NotFoundSignal, CustomException 
	{

		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
		ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());

		List<String> codeList = new ArrayList<String>();
		//productRequestData.getUdfs().get("SUBPRODUCTIONTYPE")

		for (Element insertCode : insertCodeList)
		{
			String defectCode = insertCode.getChildText("INSERTCODE");
			String codeAndDesc = "";
			List<MVIDefectCode> code = ExtendedObjectProxy.getMVIDefectCodeService().select("PRODUCTSPECNAME = ? AND DEFECTCODE = ? AND LEVELNO = '1'", new Object[]{lotData.getProductSpecName(),defectCode});
			if (code!=null && code.size()>0)
			{
				codeAndDesc = defectCode + " : " + code.get(0).getDescription();
			}
			codeList.add(codeAndDesc);
			
			List<EnumDefValue> descendedCodeList = ExtendedObjectProxy.getEnumDefValueService().select("ENUMNAME = ? AND DESCRIPTION = ? AND DISPLAYCOLOR = ? AND DEFAULTFLAG = ?", new Object[]{"DescendedCode",defectCode,machineName,lotData.getProductSpecName()});
			
			if (descendedCodeList!=null && descendedCodeList.size()>0)
			{
				EnumDefValue descendedCode = descendedCodeList.get(0);
				EnumDefValue newDescendedCode = (EnumDefValue) ObjectUtil.copyTo(descendedCode);
				newDescendedCode.setEnumValue(TimeStampUtil.getCurrentEventTimeKey());
				newDescendedCode.setSeq(lotName);
				
				ExtendedObjectProxy.getEnumDefValueService().modify(descendedCode, newDescendedCode, eventInfo);
			}
			else 
			{
				EnumDefValue descendedCode = new EnumDefValue();
				
				descendedCode.setEnumName("DescendedCode");
				descendedCode.setEnumValue(TimeStampUtil.getCurrentEventTimeKey());
				descendedCode.setDescription(defectCode);
				descendedCode.setDefaultFlag(lotData.getProductSpecName());
				descendedCode.setDisplayColor(machineName);
				descendedCode.setSeq(lotName);
				
				ExtendedObjectProxy.getEnumDefValueService().create("DescendedCode", TimeStampUtil.getCurrentEventTimeKey(), defectCode, lotData.getProductSpecName(), machineName, lotName, eventInfo);
			}
		}
		
		//=======================DescendedCode Start===========================//

		/**
		 * MVI 连续异常Code通知整合
		 */
		try
		{				
			StringBuffer info = new StringBuffer();
			info.append("<pre>=======================AlarmInformation=======================</pre>");
			info.append("<pre>	CodeList："+codeList.toString()+"</pre>");
			info.append("<pre>	ProductSpec："+lotData.getProductSpecName()+"</pre>");
			info.append("<pre>	Timekey："+TimeStampUtil.getCurrentEventTimeKey().substring(0, 12)+"</pre>");
			info.append("<pre>	MachineName："+machineName+"</pre>");
			info.append("<pre>	PanelName："+lotName+"</pre>");
			info.append("<pre>=============================End=============================</pre>");			
			
			String message = info.toString();
			
			sendToEMForDescendedCode(message);
		}
		catch (Exception e)
		{
			log.info("eMobile or WeChat Send Error : " + e.getCause());	
		}
		
		//=======================DescendedCode End===========================//
				
		
	}

	private void sendToEMForDescendedCode(String message) throws CustomException 
	{
		String[] userList = getAlarmUserIdForCode();	
		if(userList == null || userList.length ==0) return;
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("MVIJudge", "MES", "", "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		try
		{	
			log.info("MVI DescendedCode Start Send To Emobile & Wechat");	
			
			String title = "DescendedCodeReport";
			String detailtitle = "${}CIM系统消息通知";
			String url = "";
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userList, title, detailtitle, message, url);
									
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userList, title, detailtitle, message, url);
		}
		catch (Exception e)
		{
			log.info("eMobile or WeChat Send Error : " + e.getCause());	
		} 
	}
	
	public String[] getAlarmUserIdForCode() throws CustomException
	{
		String[] userGroup = null;
		StringBuffer userList = new StringBuffer();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT DISTINCT B.USERID FROM CT_ALARMUSERGROUP B  ");
		sql.append(" WHERE 1=1 ");
		sql.append(" AND B.ALARMGROUPNAME = 'MVIDescendedCode' ");
		sql.append(" AND B.USERID LIKE 'V00%' ");
		Map<String, Object> args = new HashMap<String, Object>();
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(),args);

		if(sqlResult != null && sqlResult.size()>0)
		{
			for (int i = 0; i < sqlResult.size(); i++) {
				userList = userList.append(sqlResult.get(i).get("USERID").toString()+";");
			}
			userGroup = userList.toString().split(";") ;
		}
		else
		{
			userGroup = null ;
		}
		
		return userGroup;
	}
	
	public void setLotData(String lotName, String lotGrade, String lotDetailGrade, EventInfo eventInfo, EventInfo eventInfoScrap) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);

		// lot grade/ detail grade update
		lotData.setLotGrade(lotGrade);
		LotServiceProxy.getLotService().update(lotData);

		// Set Event
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("LOTDETAILGRADE", lotDetailGrade);

		LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
	}

	public void assignPanelAndTrackOut(String lotName, String trayName, String position, String judge, String machineName, EventInfo eventInfo, EventInfo futureActionEventInfo)
			throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
		Lot oldLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);

		boolean reserveFlag = false;
		
		List<Map<String, Object>> lotList = MESLotServiceProxy.getLotServiceUtil().getLotListByTray(trayName);

		Durable trayInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);

		if (lotList.size() >= trayInfo.getCapacity())
			throw new CustomException("DURABLE-1001", trayName);
        
		ProcessFlow processFlow = CommonUtil.getProcessFlowData(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion());

		kr.co.aim.greentrack.processflow.management.iter.NodeStack nodeStack = NodeStackUtil.stringToNodeStack(lotData.getNodeStack());
		ProcessFlowIterator pfi = new ProcessFlowIterator(processFlow, nodeStack, "");

		PFIValueSetter valueSetter = new LotPFIValueSetter(pfi, oldLotData, lotData);
		pfi.moveNext("N", valueSetter);

		// 1.3. Set ProcessFlow Iterator Related Data
		Node nextNode = pfi.getCurrentNodeData();

		if (lotList.size() > 0)
		{
			Lot groupLot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotList.get(0).get("LOTNAME").toString());
			if(!judge.equals("T"))
			{
				//not check lotDetailGrade
				CommonValidation.checkLotSameSpec(lotData, groupLot.getLotGrade(), lotData.getUdfs().get("LOTDETAILGRADE").toString(), groupLot.getUdfs().get("BEFOREOPERATIONNAME").toString(),
						groupLot.getProductSpecName(), groupLot.getProductionType(), groupLot.getKey().getLotName(), groupLot.getProductRequestName());
			}else
			{
				
				CommonValidation.checkLotSameSpec(lotData, groupLot.getLotGrade(), groupLot.getUdfs().get("BEFOREOPERATIONNAME").toString(),
						groupLot.getProductSpecName(), groupLot.getProductionType(), groupLot.getKey().getLotName(), groupLot.getProductRequestName());
			}
			
		}
		
		reserveFlag = MESLotServiceProxy.getLotServiceUtil().PostCellDeleteLotFutureAction(futureActionEventInfo, lotData, nextNode.getNodeAttribute1(), nextNode.getNodeAttribute2());

		Map<String, String> udfs = lotData.getUdfs();
		udfs.put("BEFOREOPERATIONNAME", oldLotData.getProcessOperationName());
		udfs.put("BEFOREOPERATIONVER", oldLotData.getProcessOperationVersion());
		udfs.put("POSITION", position);
		udfs.put("PORTNAME", "");
		udfs.put("PORTTYPE", "");
		udfs.put("PORTUSETYPE", "");
		lotData.setUdfs(udfs);

		lotData.setLotProcessState("WAIT");
		lotData.setLastLoggedOutTime(eventInfo.getEventTime());
		lotData.setLastLoggedOutUser(eventInfo.getEventUser());
		lotData.setProcessOperationName(nextNode.getNodeAttribute1());
		lotData.setProcessOperationVersion(nextNode.getNodeAttribute2());
		lotData.setNodeStack(nextNode.getKey().getNodeId());
		lotData.setCarrierName(trayName);

		MESLotServiceProxy.getLotServiceImpl().setEventForce(eventInfo, oldLotData, lotData);

		// Durable
		Durable olddurableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
		Durable durableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);

		durableInfo.setLotQuantity(durableInfo.getLotQuantity() + 1);
		durableInfo.setDurableState(constantMap.Dur_InUse);
		durableInfo.setLastEventName(eventInfo.getEventName());
		durableInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
		durableInfo.setLastEventTime(eventInfo.getEventTime());
		durableInfo.setLastEventUser(eventInfo.getEventUser());
		durableInfo.setLastEventComment(eventInfo.getEventComment());

		DurableHistory durHistory = new DurableHistory();
		durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(olddurableInfo, durableInfo, durHistory);

		DurableServiceProxy.getDurableService().update(durableInfo);
		DurableServiceProxy.getDurableHistoryService().insert(durHistory);

		if (reserveFlag)
		{
			EventInfo eventInfoHold = EventInfoUtil.makeEventInfo("ReserveHold", getEventUser(), getEventComment(), null, null);
			eventInfoHold.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfoHold.setEventTime(TimeStampUtil.getCurrentTimestamp());

			MESLotServiceProxy.getLotServiceUtil().PanelHoldByTray(eventInfoHold, trayName);
		}

		// ADD MVI assign Tray
		eventInfo.setEventName("MVIJudge");
		ExtendedObjectProxy.getMVIAssignTrayService().createMVIAssignTrayData(futureActionEventInfo, machineName, judge, trayName, "", lotData.getProductSpecName(), lotData.getProductSpecVersion(),
				lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getUdfs().get("BEFOREOPERATIONNAME"), lotData.getProcessOperationVersion(), lotData.getProductRequestName());
	}

	public void setPanelJudgeData(long seq, String lotName, String opticalJudge, String electricalJudge, String judge, EventInfo eventInfo, EventInfo trackOutEventInfo, String machineName,
			String tpJudge) throws CustomException
	{
		try
		{
			MVIPanelJudge dataInfo = ExtendedObjectProxy.getMVIPanelJudgeService().selectByKey(false, new Object[] { seq, lotName });
			dataInfo.setOpticalJudge(opticalJudge);
			dataInfo.setElectricalJudge(electricalJudge);
			dataInfo.setAfterGrade(judge);
			dataInfo.setEventTime(eventInfo.getEventTime());
			dataInfo.setEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
			dataInfo.setLastLoggedOutTime(trackOutEventInfo.getEventTime());
			dataInfo.setMachineName(machineName);
			dataInfo.setTpJudge(tpJudge);

			ExtendedObjectProxy.getMVIPanelJudgeService().update(dataInfo);
		}
		catch (greenFrameDBErrorSignal ex)
		{
			throw new CustomException("LOT-0007", "MVIPanelJudge [" + lotName + "] does not exist!");
		}
	}

	public void setOpticalData(List<Element> opticalList, long seq, String lotName, String gamut, EventInfo eventInfo) throws CustomException
	{
		ExtendedObjectProxy.getMVIOpticalInspectionService().deleteMVIOpticalInspectionDataList(eventInfo, seq, lotName);

		for (Element defectData : opticalList)
		{
			try
			{
				ExtendedObjectProxy.getMVIOpticalInspectionService().createMVIOpticalInspectionData(eventInfo, seq, lotName, defectData, gamut);
			}
			catch (greenFrameDBErrorSignal ex)
			{
				throw new CustomException("LOT-0007", ex.getMessage());
			}
		}
	}

	public void setElectricalData(List<Element> electricalList, long seq, String lotName, EventInfo eventInfo) throws CustomException
	{
		ExtendedObjectProxy.getMVIElectricalInspectionService().deleteMVIElectricalInspectionDataList(eventInfo, seq, lotName);

		for (Element defectData : electricalList)
		{
			try
			{
				ExtendedObjectProxy.getMVIElectricalInspectionService().createMVIElectricalInspectionData(eventInfo, seq, lotName, defectData);
			}
			catch (greenFrameDBErrorSignal ex)
			{
				throw new CustomException("LOT-0313");
			}
		}
	}

	public void setTPInspectionData(List<Element> tpInspectionList, long seq, String lotName, EventInfo eventInfo) throws CustomException
	{
		ExtendedObjectProxy.getMVITPInspectionService().deleteMVITPInspectionDataList(eventInfo, seq, lotName);

		for (Element tpInspectionData : tpInspectionList)
		{
			try
			{
				ExtendedObjectProxy.getMVITPInspectionService().createMVITPInspectionData(eventInfo, seq, lotName, tpInspectionData);
			}
			catch (greenFrameDBErrorSignal ex)
			{
				throw new CustomException("LOT-0313");
			}
		}
	}

	public void setUserDefectHist(String machineName, String lotName, String judge, List<Element> electricalList, EventInfo eventInfo) throws CustomException
	{
		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
		ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());

		StringBuffer defectCodeBuffer = new StringBuffer();
		defectCodeBuffer.append("[");
		for (Element electrical : electricalList)
		{
			String defectCode = electrical.getChildText("DEFECT_CODE");

			if (!StringUtil.equals(electrical.getChildText("DEFECT_ORDER"), "A") && !StringUtil.equals(electrical.getChildText("DEFECT_ORDER"), "A1"))
				defectCodeBuffer.append(defectCode+",");
		}
		
		if(defectCodeBuffer.indexOf(",")>0)
		{
			defectCodeBuffer.deleteCharAt(defectCodeBuffer.lastIndexOf(","));
		}
			
		
		defectCodeBuffer.append("]");
		
		String defectCode = defectCodeBuffer.toString().trim();
		ExtendedObjectProxy.getMVIUserDefectService().insertMVIUserDefectHistData(eventInfo, lotData, machineName, productRequestData, judge, defectCode);
	}
	//{2020/11/26 caixu
	public void setSortPickInfo( String lotName, String judge, List<Element> electricalList, EventInfo eventInfo) throws CustomException
	{
	
		String sql1= "Select  TIMEKEY,ENDQUANTITY,QUANTITY FROM CT_SVIPICKINFO "
				+ "WHERE PROCESSOPERATIONNAME ='35052'"
				+ "AND JUDGE =:JUDGE "
				+ " AND CODE = :CODE "
				+ " AND PRODUCTSPECNAME = :PRODUCTSPECNAME "
				+ " AND WORKORDER = :WORKORDER "
		        + " AND POINT = :POINT "
		        + " AND QUANTITY >ENDQUANTITY "
		        + " ORDER BY TIMEKEY DESC";
		String sql2= "Select  TIMEKEY,ENDQUANTITY,QUANTITY FROM CT_SVIPICKINFO "
				+ "WHERE PROCESSOPERATIONNAME ='35052'"
				+ "AND JUDGE =:JUDGE "
				+ " AND CODE = :CODE "
				+ " AND PRODUCTSPECNAME = :PRODUCTSPECNAME "
				+ " AND WORKORDER = :WORKORDER "
				+ " AND QUANTITY > ENDQUANTITY "
		        + " AND POINT IS NULL"
		        + " ORDER BY TIMEKEY DESC";
		String sql3= "Select  TIMEKEY,ENDQUANTITY,QUANTITY FROM CT_SVIPICKINFO "
				+ "WHERE PROCESSOPERATIONNAME ='35052'"
				+ "AND JUDGE =:JUDGE "
				+ " AND CODE = :CODE "
				+ " AND PRODUCTSPECNAME = :PRODUCTSPECNAME "
				+ " AND QUANTITY > ENDQUANTITY "
		        + " AND POINT IS NULL "
		        + " AND WORKORDER IS NULL "  
		        + " ORDER BY TIMEKEY DESC";

		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
		String productSpec=lotData.getProductSpecName();
		String workerOrder=lotData.getProductRequestName();
		String point=lotName.substring(12, 14);

		for (Element electrical : electricalList)
		{
			String defectCode = electrical.getChildText("DEFECT_CODE");
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("JUDGE",judge);
			bindMap.put("CODE",defectCode);
			bindMap.put("PRODUCTSPECNAME",productSpec);
			bindMap.put("WORKORDER",workerOrder);
			bindMap.put("POINT",point);
			List<Map<String, Object>> result1 = GenericServiceProxy.getSqlMesTemplate().queryForList(sql1, bindMap);
			if(result1.size()>0)
			{
			   pickinfoModify(result1,defectCode,lotName,eventInfo);
				
			}
			else
			{
				List<Map<String, Object>> result2 = GenericServiceProxy.getSqlMesTemplate().queryForList(sql2, bindMap);
				if(result2.size()>0)
				{
					pickinfoModify(result2,defectCode,lotName,eventInfo);
					
				}
				else
				{
					List<Map<String, Object>> result3 = GenericServiceProxy.getSqlMesTemplate().queryForList(sql3, bindMap);
					if(result3.size()>0)
					{
						pickinfoModify(result3,defectCode,lotName,eventInfo);
						
					}
					
					
				}
				
				
			}
		}
		
	}
	public void pickinfoModify(List<Map<String, Object>> result,String defectCode,String lotName, EventInfo eventInfo ) throws CustomException
	{


		SorterPickPrintInfo dataInfo = null;
		String timeKey= result.get(0).get("TIMEKEY").toString();
		int endQuantity= Integer.parseInt(result.get(0).get("ENDQUANTITY").toString());
		try
		{
			dataInfo = ExtendedObjectProxy.getSorterPickPrintInfoService().selectByKey(false, new Object[] { lotName });
		}
		catch (Exception ex)
		{
			if (ex instanceof greenFrameDBErrorSignal && ((greenFrameDBErrorSignal) ex).getErrorCode().equals(ErrorSignal.NotFoundSignal))
				log.info("Not found SorterPickPrintInfo by PanelName =  " + lotName);
			else
				throw new CustomException(ex.getCause());
		}

		EventInfo pickEventInfo = EventInfoUtil.makeEventInfo(this.getMessageName(), getEventUser(), getEventComment());

		if (dataInfo == null)
		{
			SorterPickPrintInfo sorterPickData = new SorterPickPrintInfo();
			sorterPickData.setLotName(lotName);
			sorterPickData.setPickPrintMode("YN");
			sorterPickData.setCode(defectCode);

			ExtendedObjectProxy.getSorterPickPrintInfoService().create(pickEventInfo, sorterPickData);
			SVIPickInfo pickData = ExtendedObjectProxy.getSVIPickInfoService().getDataInfoByKey(timeKey, true);
			eventInfo = EventInfoUtil.makeEventInfo(this.getMessageName(), this.getEventUser(), this.getEventComment());
			endQuantity=endQuantity+1;
			pickData.setEndQuantity(endQuantity);
			ExtendedObjectProxy.getSVIPickInfoService().modify(eventInfo, pickData);
	    }
	}
	private void trayGroupHoldForS1(String LotName) throws CustomException
	{
		try
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("Hold", this.getEventUser(), this.getEventComment());

			StringBuilder sql = new StringBuilder();
			sql.append("SELECT * ");
			sql.append(" FROM LOT ");
			sql.append(" WHERE LOTNAME IN (:LOTLIST) ");
			sql.append(" AND PROCESSOPERATIONNAME='37000'");
			sql.append(" AND LOTGRADE='S'");
			sql.append(" AND LOTDETAILGRADE='S1'");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LOTLIST", LotName);

			List<Map<String, Object>> resultList = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), bindMap);

			if (resultList.size() > 0)
			{
				String reasonCode = "HD100";
				String reasonCodeType = "HOLD";
				ConstantMap constantMap = GenericServiceProxy.getConstantMap();

				eventInfo.setEventComment(String.format("Hold TrayGroup by S1 .[PanelName=%s]", LotName ));
				eventInfo.setReasonCode(reasonCode);
				eventInfo.setReasonCodeType(reasonCodeType);
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
				List<Lot> updateLotList = new ArrayList<>();
				List<LotHistory> updateHistList = new ArrayList<>();
				List<Lot> lotDataList = LotServiceProxy.getLotService().transform(resultList);
				for (Lot lotData : lotDataList)
				{
				Lot oldLot = (Lot) ObjectUtil.copyTo(lotData);
               
				lotData.setLotHoldState(constantMap.Flag_Y);
				lotData.setReasonCode(reasonCode);
				lotData.setReasonCodeType(reasonCodeType);
				lotData.setLastEventName(eventInfo.getEventName());
				lotData.setLastEventTime(eventInfo.getEventTime());
				lotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
				lotData.setLastEventUser(eventInfo.getEventUser());
				lotData.setLastEventComment(eventInfo.getEventComment());

				LotHistory lotHist = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lotData, new LotHistory());

				updateLotList.add(lotData);
				updateHistList.add(lotHist);
				}

				try
				{
					CommonUtil.executeBatch("update", updateLotList, true);
					CommonUtil.executeBatch("insert", updateHistList, true);

					log.info(String.format("?Successfully hold %s pieces of panels.", updateLotList.size()));
				}
				catch (Exception e)
				{
					log.error(e.getMessage());
					throw new CustomException(e.getCause());
				}
		   }
		}
		catch (Exception e)
		{
			log.info("Error Occurred - Hold Panel by S1");
		}
	}
}