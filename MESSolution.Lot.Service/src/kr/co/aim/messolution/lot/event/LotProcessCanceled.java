package kr.co.aim.messolution.lot.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EMailInterface;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.recipe.MESRecipeServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskRecipe;
import kr.co.aim.messolution.extended.object.management.data.TPOffsetAlignInfo;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;

public class LotProcessCanceled extends AsyncHandler {

	private static Log log = LogFactory.getLog(LotProcessCanceled.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);
		String eventComment = SMessageUtil.getReturnItemValue(doc, "RETURNMESSAGE", false);
		
		String productOffset = "";
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("FailTrackIn", getEventUser(), eventComment, null, null);
		MachineSpec machineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(new MachineSpecKey(machineName));

		// Initial ReserveLot for array Unpacker
		if (StringUtils.equals(machineSpecData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Unpacker))
			this.updateReserveLotState(eventInfo, machineName, lotName);

		// Rms Scenario Cancel
		if (eventComment.equals("RmsScenarioCancelByBCServer"))
			eventComment = this.checkedMachine(eventInfo, machineSpecData, eventComment, machineName, portName, carrierName);

		if (StringUtils.isEmpty(eventComment.trim()))
			eventComment = "ReturnMessage value of LotProcessCanceled is empty";

		String lotname1 = lotName;
		// GetLotName by CST
		lotName = getLotInfoBydurableNameForFisrtGlass(carrierName);

		Lot lotData = null;
		if (!StringUtil.isEmpty(lotName))
		{
			lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

			SetEventInfo setEventInfo = new SetEventInfo();
			if (StringUtils.equals(lotData.getUdfs().get("JOBDOWNFLAG"), "Y"))
			{
				setEventInfo.getUdfs().put("JOBDOWNFLAG", "");
	
				// [V3_MES_121_004]DSP Run Control_V1.02
				int actualProductQty = 0;
				String slotSel = lotData.getUdfs().get("SLOTSEL").toString();
				for (int i = 0 ; i < slotSel.length() ; i++)
				{
					if (slotSel.substring(i, i+1).equals(GenericServiceProxy.getConstantMap().PRODUCT_IN_SLOT))
						actualProductQty++;
				}
				MESLotServiceProxy.getLotServiceUtil().decreaseRunControlUseCount(eventInfo, machineName, lotName, actualProductQty);
				
				if(machineSpecData.getFactoryName().equals("TP") && CommonUtil.equalsIn(machineSpecData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_Photo))
				{
					List<Product> productList = MESProductServiceProxy.getProductServiceUtil().allUnScrappedProductsByLot(lotName);
					productOffset = productList.get(0).getUdfs().get("OFFSET").toString();
				}
				
				// Mantis #0000357
				if (!StringUtil.isEmpty(machineRecipeName))
				{
					if (MESRecipeServiceProxy.getRecipeServiceUtil().RMSFlagCheck("E", machineName, machineRecipeName, "", lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), productOffset))
					{
						MESRecipeServiceProxy.getRecipeServiceUtil().checkRecipeOnCancelTrackInTime(machineName, machineRecipeName);
					}
				}
			}
			lotData = LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
		}
		else
		{
			Port portData = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName);
			
			if (portData != null && (CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PU") || CommonUtil.getValue(portData.getUdfs(), "PORTTYPE").equals("PL")))
			{
				Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

				String durableHoldState = durableData.getUdfs().get("DURABLEHOLDSTATE").toString();
				if (durableHoldState.equals(GenericServiceProxy.getConstantMap().DURABLE_HOLDSTATE_Y))
				{
					log.info("Carrier already be held");
				}
				else
				{
					kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
					setEventInfo.getUdfs().put("DURABLEHOLDSTATE", GenericServiceProxy.getConstantMap().DURABLE_HOLDSTATE_Y);
	
					EventInfo eventInfoHold = EventInfoUtil.makeEventInfo("FailTrackIn", getEventUser(), getEventComment() + ". " + eventComment, "HoldCST", "HC-Wait ENG");
	
					MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfoHold);
				}
			}
		}
		
		if(StringUtils.isNotEmpty(lotname1))
		{
			Lot lotData1 = MESLotServiceProxy.getLotInfoUtil().getLotData(lotname1);
			log.info("Lot " + lotname1 + " HoldState: " + lotData1.getLotHoldState() + ", ProcessState: " + lotData1.getLotProcessState());
			if (!StringUtil.equals(lotData1.getLotHoldState(), "Y") && StringUtil.equals(lotData1.getLotProcessState(), GenericServiceProxy.getConstantMap().Lot_Wait))
				this.makeLotHold(eventInfo, lotData1);
		}
		
		try
		 {
			 SendEmailForCSTdeparment(lotData,machineName);
		 }
		 catch (Exception e)
		 {
			log.error("Failed to send mail.");
		 }

		Element bodyElement = this.makeBodyElement(doc, machineName, portName, carrierName, lotName, eventComment);

		String targetSubject = GenericServiceProxy.getESBServive().getSendSubject("CNXsvr");
		Document alarmDoc = null;
		try
		{
			alarmDoc = SMessageUtil.createXmlDocument(bodyElement, "MESAlarmReport", "", targetSubject, "MES", "");
		}
		catch (Exception ae)
		{
			ae.printStackTrace();
		}
		GenericServiceProxy.getESBServive().sendBySender(targetSubject, alarmDoc, "CNXSender");

	}

	private void SendEmailForCSTdeparment(Lot lotData, String machineName) {
		// TODO Auto-generated method stub
		StringBuffer message = new StringBuffer();
		message.append("<pre>Dear All</pre>");
		message.append("<pre>	LotName："+lotData.getKey().getLotName()+"	在站点："+lotData.getProcessOperationName()+"	设备："+lotData.getMachineName()+"	FailTrackIn</pre>");
		message.append("<pre>	EventComment："+lotData.getLastEventComment()+"</pre>");
		message.append("<pre>	EventTime："+lotData.getLastEventTime()+"</pre>");

		List<String> emailList = new ArrayList<String>();
		List<String> sb = new ArrayList<String>();
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT MS.RMSDEPARTMENT FROM MACHINESPEC MS WHERE MS.MACHINENAME = :MACHINENAME");
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("MACHINENAME", machineName);
		
		//get MachineDepartment
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		
		try 
		{
			if (sqlResult.size() > 0) 
			{
				String departmentAll = "";

				departmentAll = sqlResult.get(0).get("RMSDEPARTMENT").toString();

				String[] department = null;
				department = StringUtils.split(departmentAll, ",");

				Map<String, Object> args1 = new HashMap<String, Object>();

				for (String department1 : department) 
				{
					StringBuffer sql1 = new StringBuffer();
					sql1.append(
							"SELECT B.* FROM CT_ALARMUSERGROUP A, CT_ALARMUSER B WHERE A.USERID = B.USERID  AND A.ALARMGROUPNAME = 'FailTrackIn' AND B.DEPARTMENT=:DEPARTMENT");
					
					if (StringUtils.equals(department1, "OTHER-INT-PIE"))
					{
						sql1 = new StringBuffer();
						sql1.append("SELECT B.*"
								+ "  FROM CT_ALARMUSERGROUP A, CT_ALARMUSER B, USERPROFILE L"
								+ " WHERE     A.USERID = B.USERID"
								+ "       AND A.ALARMGROUPNAME = 'FailTrackIn'"
								+ "       AND B.DEPARTMENT = :DEPARTMENT"
								+ "       AND A.USERID = L.USERID"
								+ "       AND L.GROUPNAME = :GROUPNAME");
						args1.put("GROUPNAME", lotData.getFactoryName());
					}
					
					args1.put("DEPARTMENT", department1);
					List<Map<String, Object>> sqlResult1 = GenericServiceProxy.getSqlMesTemplate()
							.queryForList(sql1.toString(), args1);
					if (sqlResult1.size() > 0) 
					{
						for (Map<String, Object> user : sqlResult1)
						{
							String eMail = ConvertUtil.getMapValueByName(user, "EMAIL");
							emailList.add(eMail);
							
							String userInfo = ConvertUtil.getMapValueByName(user, "USERID");
							sb.add(userInfo);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			log.error("Not Found the Department of "+ machineName);
			log.error("Failed to send mail.");
		}
		
		if (emailList.size() > 0)
		{
			try
			{
				EMailInterface ei = new EMailInterface("mail.visionox.com", "25", "1000", "1000");
				ei.postMail(emailList,  " Lot Fail TrackIn ", message.toString(), "V3MES", "V3MES@visionox.com", "V3MES", "vis@2019");
				//ei.postMail(emailList,  " Lot Fail TrackIn ", message.toString(), "V0042735", "hankun@visionox.com", "V0042735", "qwe@@12340");
			}
			catch (Exception e)
			{
				log.error("Failed to send mail.");
			}
		}
		
		//houxk 20210617
		if(sb.size() > 0)
		{
			try
			{				
				sendToEm(sb, message.toString());
			}
			catch (Exception e)
			{
				log.info("eMobile or WeChat Send Error : " + e.getCause());	
			}
		}
	}

	private String getLotInfoBydurableNameForFisrtGlass(String carrierName) throws CustomException
	{
		List<Map<String, Object>> lotList;
		String lotName = "";

		String sql = "SELECT LOTNAME FROM LOT WHERE CARRIERNAME = :CARRIERNAME AND LOTSTATE = :LOTSTATE AND FIRSTGLASSFLAG = 'N' AND JOBNAME IS NOT NULL ";

		Map<String, String> args = new HashMap<String, String>();
		args.put("CARRIERNAME", carrierName);
		args.put("LOTSTATE", GenericServiceProxy.getConstantMap().Lot_Released);

		lotList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

		if (lotList.size() > 0 && lotList != null)
		{
			lotName = ConvertUtil.getMapValueByName(lotList.get(0), "LOTNAME");
		}
		else
		{
			sql = "SELECT LOTNAME FROM LOT WHERE CARRIERNAME = :CARRIERNAME AND LOTSTATE = :LOTSTATE AND FIRSTGLASSFLAG IS NULL AND JOBNAME IS NOT NULL ";

			lotList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

			if (lotList.size() > 0 && lotList != null)
			{
				lotName = ConvertUtil.getMapValueByName(lotList.get(0), "LOTNAME");
			}
			else
			{
				sql = "SELECT LOTNAME FROM LOT WHERE CARRIERNAME = :CARRIERNAME AND LOTSTATE = :LOTSTATE ";
				lotList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

				if (lotList.size() > 0 && lotList != null)
				{
					lotName = ConvertUtil.getMapValueByName(lotList.get(0), "LOTNAME");
				}
			}
		}

		return lotName;
	}
	
	private void updateReserveLotState (EventInfo eventInfo, String machineName, String lotName) throws CustomException
	{
		ReserveLot reserveLot = ExtendedObjectProxy.getReserveLotService().getReserveLot(machineName, lotName);
		eventLog.info("Select Reserve Lot success");
		eventInfo.setEventName("ChangeState");
		reserveLot.setReserveState(GenericServiceProxy.getConstantMap().RESV_STATE_RESV);
		ExtendedObjectProxy.getReserveLotService().modify(eventInfo, reserveLot);
	}
	
	private String checkedMachine (EventInfo eventInfo, MachineSpec machineSpecData, String eventComment, String machineName, String portName, String carrierName) throws CustomException
	{
		if (machineSpecData.getUdfs().get("RMSFLAG").equals("Y"))
		{
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT C.CHECKEDMACHINE ");
			sql.append("  FROM CT_RECIPECHECKEDMACHINE C ");
			sql.append(" WHERE 1 = 1 ");
			sql.append("   AND C.MAINMACHINENAME = :MACHINENAME ");
			sql.append("   AND C.PORTNAME = :PORTNAME ");
			sql.append("   AND C.CARRIERNAME = :CARRIERNAME ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("MACHINENAME", machineName);
			bindMap.put("PORTNAME", portName);
			bindMap.put("CARRIERNAME", carrierName);
			List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
			if (result.size() > 0)
			{
				StringBuilder Str = new StringBuilder();
				for (Map<String, Object> map : result)
				{
					String checkMachine = CommonUtil.getValue(map, "CHECKEDMACHINE");
					Str.append(checkMachine + ", ");
				}
				eventInfo.setEventComment("RMS TimeOut:Communication failed with Machine " + Str);
				eventComment = "RMS TimeOut:Communication failed with Machine " + Str;
			}
		}
		
		return eventComment;
	}
	
	private Element makeBodyElement (Document doc, String machineName, String portName, String carrierName, String lotName, String eventComment) throws CustomException
	{
		Element bodyElement = new Element(SMessageUtil.Body_Tag);
		{
			Element attFactoryName = new Element("FACTORYNAME");
			attFactoryName.setText(SMessageUtil.getHeaderItemValue(doc, "SHOPNAME", true));
			bodyElement.addContent(attFactoryName);

			Element attMachineName = new Element("MACHINENAME");
			attMachineName.setText(machineName);
			bodyElement.addContent(attMachineName);

			Element attPortName = new Element("PORTNAME");
			attPortName.setText(portName);
			bodyElement.addContent(attPortName);

			Element attDurableName = new Element("CARRIERNAME");
			attDurableName.setText(carrierName);
			bodyElement.addContent(attDurableName);

			Element attLotName = new Element("LOTNAME");
			attLotName.setText(lotName);
			bodyElement.addContent(attLotName);

			Element attErrorMessage = new Element("ERRORMESSAGE");
			attErrorMessage.setText("LotProcessCanceled");
			bodyElement.addContent(attErrorMessage);

			Element attErrorDetail = new Element("ERRORDETAIL");
			attErrorDetail.setText(eventComment);
			bodyElement.addContent(attErrorDetail);
		}
		
		return bodyElement;
	}
	
	private void makeLotHold (EventInfo eventInfo, Lot lotData) throws CustomException
	{
		String oriEventComment = eventInfo.getEventComment();
		String holdEventComment = "Start Lot Hold" + oriEventComment;
		log.info(holdEventComment);

		// Set ReasonCode
		eventInfo.setReasonCodeType("HOLD");
		eventInfo.setReasonCode("SYSTEM");
		eventInfo.setEventComment(holdEventComment);

		// LotMultiHold
		MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, lotData, new HashMap<String, String>());
		eventInfo.setEventComment(oriEventComment);
	}
	
	//AlarmGroup =FailTrackIn & RMSDepart  
	public void sendToEm(List<String> sb, String message)
	{		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("LotProcessCanceled", "MES", "", "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		String[] userList = sb.toArray(new String[] {});
		
		try
		{	
			log.info("LotFailTrackIn Start Send To Emobile & Wechat");	
						
			String title = "LotFailTrackIn";
			String detailtitle = "${}CIM系统消息通知";
			String url = "";									
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userList, title, detailtitle, message, url);
									
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userList, title, detailtitle, message, "");
		}
		catch (Exception e)
		{
			log.info("eMobile or WeChat Send Error : " + e.getCause());	
		} 
	}
	
}
