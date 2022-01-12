package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LotFutureAction;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EMailInterface;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class CreateAbnormalSheet extends SyncHandler {
	private static Log log = LogFactory.getLog(CreateAbnormalSheet.class);
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String FactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String AbnormalSheetName = SMessageUtil.getBodyItemValue(doc, "ABNORMALSHEETNAME", false);
		String AbnormalSheetType = SMessageUtil.getBodyItemValue(doc, "ABNORMALSHEETTYPE", true);
		String ProcessOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String FindProcessOperationName = SMessageUtil.getBodyItemValue(doc, "FINDPROCESSOPERATIONNAME", true);
		String LotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String MachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String FindMachineName = SMessageUtil.getBodyItemValue(doc, "FINDMACHINENAME", true);
		String DueDate = SMessageUtil.getBodyItemValue(doc, "DUEDATE", true);
		String SheetComment = SMessageUtil.getBodyItemValue(doc, "SHEETCOMMENT", false);
		String ActionType = SMessageUtil.getBodyItemValue(doc, "ACTIONTYPE", true);
		String Hold = SMessageUtil.getBodyItemValue(doc, "HOLD", true);
		String RSOperationName = SMessageUtil.getBodyItemValue(doc, "RSOPERATIONNAME", true);

		List<Element> CodeList = SMessageUtil.getBodySequenceItemList(doc, "CODELIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		Lot lotData1 = MESLotServiceProxy.getLotInfoUtil().getLotData(LotName);
		if(lotData1.getProcessOperationName().equals(ProcessOperationName)||lotData1.getProcessOperationName().equals(RSOperationName))
		{
			boolean issueFlag = ExtendedObjectProxy.getEnumDefValueService().isExistEnumNameInfo("AbnormalSheetIssueOper", RSOperationName);
			
			if (ActionType.equals("Insert"))
			{
				AbnormalSheetName = InsertAbnormalSheet(FactoryName, AbnormalSheetType, ProcessOperationName, FindProcessOperationName, LotName, MachineName, FindMachineName, DueDate, SheetComment,
						eventInfo);
			}

			// hold Lot
			if (Hold.equals("True"))
			{
				Element eleLot = new Element("LOT");
				XmlUtil.addElement(eleLot, "LOTNAME", LotName);
				XmlUtil.addElement(eleLot, "REASONCODE", "RH-ENG");
				XmlUtil.addElement(eleLot, "REASONCODETYPE", "AbnormalSheet");

				String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
				String reasonCode = SMessageUtil.getChildText(eleLot, "REASONCODE", true);
				String reasonCodeType = SMessageUtil.getChildText(eleLot, "REASONCODETYPE", true);
				eventInfo.setReasonCode(reasonCode);
				eventInfo.setReasonCodeType(reasonCodeType);

				Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

				if (!StringUtils.isEmpty(lotData.getUdfs().get("JOBNAME")))
					throw new CustomException("LOT-0047", lotName);				

				Map<String, String> udfs = new HashMap<String, String>();

				// Abnormal List Hold
				String requestDepartment = StringUtil.EMPTY;
				if (FactoryName.equals("ARRAY"))
					requestDepartment = "A" + CodeList.get(0).getChildText("ENGDEPARTMENT");
				else if (FactoryName.equals("OLED"))
					requestDepartment = "C" + CodeList.get(0).getChildText("ENGDEPARTMENT");
				else if (FactoryName.equals("TP"))
					requestDepartment = "T" + CodeList.get(0).getChildText("ENGDEPARTMENT");
				else if (FactoryName.equals("POSTCELL"))
					requestDepartment = "P" + CodeList.get(0).getChildText("ENGDEPARTMENT");

				udfs.put("RELEASETYPE", "Department");
				udfs.put("REQUESTDEPARTMENT", requestDepartment);
				
				String HoldOperationName = RSOperationName;
				if(ProcessOperationName.equals("21200"))
				{
					HoldOperationName="21200";
				}
				
				String processFlowName = lotData.getProcessFlowName();
				String processOperationVer ="00001";
				String processFlowVer = lotData.getProcessFlowVersion();

				eventInfo = EventInfoUtil.makeEventInfo("SetFutureHold", getEventUser(), getEventComment(), "", "");
				eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
				eventInfo.setEventComment("ReserveOper:" + lotData.getProcessOperationName() + "." + eventInfo.getEventComment());
				eventInfo.setEventUser("ReviewStation");
                
				if(!issueFlag)
				{
					//Reserve Hold
					List<LotFutureAction> futureActionList = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataListWithoutPosition(lotName, FactoryName, processFlowName,
							processFlowVer, HoldOperationName, processOperationVer, reasonCode);

					if (futureActionList == null)
					{
						String afterActionComment = eventInfo.getEventComment();
						String afterActionUser = eventInfo.getEventUser();

						// insert FutureCondition
						MESLotServiceProxy.getLotServiceImpl().insertCtLotFutureMultiHoldActionForAfter(eventInfo, lotName, FactoryName, processFlowName, processFlowVer, HoldOperationName,
								processOperationVer, "0", "hold", "System", reasonCodeType, reasonCode, "", "", "", "False", "True", "", afterActionComment, "", afterActionUser, "Insert", "", "");						
						
					}
				}
				//issueFlag is True:ReserveIssue according to ct_AbnormalSheetDetail IssueFlag
					
			}

			for (Element code : CodeList)
			{
				String ProductName = code.getChild("PRODUCTNAME").getText();
				String SlotPosition = code.getChild("SLOTPOSITION").getText();
				String AbnormalCode = code.getChild("ABNORMALCODE").getText();
				String RSCode = code.getChild("RSCODE").getText();
				String ProcessState = code.getChild("PROCESSSTATE").getText();
				String ProcessComment = code.getChild("PROCESSCOMMENT").getText();
				String TAUser = code.getChild("TAUSER").getText();
				String EngDepartment = code.getChild("ENGDEPARTMENT").getText();
				String aType = code.getChild("ACTIONTYPE").getText();

				if (aType.equals("Insert"))
				{
					eventInfo.setEventName("Insert AbnormalCode.");
					eventInfo.setEventComment("Insert AbnormalCode.");
					InsertAbnormalSheetDetail(AbnormalSheetName, ProductName, AbnormalCode, RSCode, ProcessState, ProcessComment, TAUser, EngDepartment, SlotPosition, eventInfo, RSOperationName, issueFlag);

					if (FactoryName.equals("ARRAY"))
						EngDepartment = "A" + EngDepartment;
					else if (FactoryName.equals("OLED"))
						EngDepartment = "C" + EngDepartment;
					else if (FactoryName.equals("TP"))
						EngDepartment = "T" + EngDepartment;
					else if (FactoryName.equals("POSTCELL"))
						EngDepartment = "P" + EngDepartment;
					
					SendEmailForCSTdeparment(AbnormalSheetName,LotName, ProductName, AbnormalCode, RSCode, ProcessState, ProcessComment, TAUser, EngDepartment, MachineName,FactoryName);
					//Add SendEmailForCSTdeparment
													
					//mantis 0000180
					//CommonUtil.sendAlarmEmailwithSubject(EngDepartment, "AbnormalSheet", message, ProductName + ProcessComment);

				}
				else if (aType.equals("Update"))
				{
					eventInfo.setEventName("Update AbnormalCode.");
					eventInfo.setEventComment("Update AbnormalCode.");
					UpdateAbnormalSheetDetail(AbnormalSheetName, ProductName, AbnormalCode, RSCode, ProcessState, ProcessComment, TAUser, EngDepartment, SlotPosition, eventInfo, RSOperationName, issueFlag);
				}
				else if (aType.equals("Delete"))
				{
					eventInfo.setEventName("Delete AbnormalCode.");
					eventInfo.setEventComment("Delete AbnormalCode.");
					DeleteAbnormalSheetDetail(AbnormalSheetName, ProductName, AbnormalCode, RSCode, ProcessState, ProcessComment, TAUser, EngDepartment, SlotPosition, eventInfo, RSOperationName, issueFlag);
				}
			}

			SMessageUtil.setBodyItemValue(doc, "ABNORMALSHEETNAME", AbnormalSheetName);
		
		}
		return doc;
	}

	private String InsertAbnormalSheet(String FactoryName, String AbnormalSheetType, String ProcessOperationName, String FindProcessOperationName, String LotName, String MachineName,
			String FindMachineName, String DueDate, String SheetComment, EventInfo eventInfo) throws CustomException
	{
		String AbnormalSheetName = "";

		try
		{
			String preFix = "";
			String currentTime=eventInfo.getEventTime().toString().substring(0,20);
			String day=eventInfo.getEventTime().toString().substring(8, 10);
			String month=eventInfo.getEventTime().toString().substring(5, 7) ;
			String year=eventInfo.getEventTime().toString().substring(2, 4) ;

			if (FactoryName.equals("ARRAY"))
				preFix = "L";
			else if (FactoryName.equals("OLED"))
				preFix = "C";
			else if (FactoryName.equals("TP"))
				preFix = "T";
			else if (FactoryName.equals("POSTCELL"))
				preFix = "P";
			
			preFix = preFix + year + month + day + "%";

			int qty = 0;

			StringBuffer sql = new StringBuffer();
			sql.append("SELECT COUNT (ABNORMALSHEETNAME) AS QTY ");
			sql.append("  FROM CT_ABNORMALSHEET ");
			sql.append(" WHERE ABNORMALSHEETNAME LIKE :PREFIX ");
			
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("PREFIX", preFix);

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (result.size() > 0)
			{
				String quantity = ConvertUtil.getMapValueByName(result.get(0), "QTY");

				qty = Integer.parseInt(quantity);

				AbnormalSheetName = preFix.substring(0, 7)+currentTime.substring(11, 13)+ currentTime.substring(14, 16 )+ String.valueOf(qty + 1001).substring(1, 4);
			}
			else
			{
				AbnormalSheetName = preFix.substring(0, 7) +currentTime.substring(11, 13)+ currentTime.substring(14, 16 )+ "001";
			}

			StringBuffer insertSql = new StringBuffer();
			insertSql.append("INSERT ");
			insertSql.append("  INTO CT_ABNORMALSHEET  ");
			insertSql.append("  (ABNORMALSHEETNAME, ABNORMALSHEETTYPE, PROCESSOPERATIONNAME, FPROCESSOPERATIONNAME, LOTNAME, ");
			insertSql.append("   MACHINENAME, FMACHINENAME, DUEDATE, CREATEUSER, CREATETIME, ");
			insertSql.append("   SHEETCOMMENT) ");
			insertSql.append("VALUES  ");
			insertSql.append("  (:ABNORMALSHEETNAME, :ABNORMALSHEETTYPE, :PROCESSOPERATIONNAME, :FPROCESSOPERATIONNAME, :LOTNAME, ");
			insertSql.append("   :MACHINENAME, :FMACHINENAME, :DUEDATE, :CREATEUSER, :CREATETIME, ");
			insertSql.append("   :SHEETCOMMENT) ");
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("ABNORMALSHEETNAME", AbnormalSheetName);
			bindMap.put("ABNORMALSHEETTYPE", AbnormalSheetType);
			bindMap.put("PROCESSOPERATIONNAME", ProcessOperationName);
			bindMap.put("FPROCESSOPERATIONNAME", FindProcessOperationName);
			bindMap.put("LOTNAME", LotName);
			bindMap.put("MACHINENAME", MachineName);
			bindMap.put("FMACHINENAME", FindMachineName);
			bindMap.put("DUEDATE", DueDate);
			bindMap.put("CREATEUSER", eventInfo.getEventUser());
			bindMap.put("CREATETIME", eventInfo.getEventTime());
			bindMap.put("SHEETCOMMENT", SheetComment);

			GenericServiceProxy.getSqlMesTemplate().update(insertSql.toString(), bindMap);

			return AbnormalSheetName;
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", "for insert " + AbnormalSheetName + " into CT_ABNORMALSHEET   Error : " + e.toString());
		}
	}
	// add SendEmailForCSTdeparment
	private void SendEmailForCSTdeparment(String AbnormalSheetName,String LotName,String ProductName, String AbnormalCode, String RSCode, String ProcessState, String ProcessComment, String TaUSer,
			String EngDepartment,String MachineName,String FactoryName) {
		
		// TODO Auto-generated method stub
		String message = "<pre>========AbnormalSheet Info===============</pre>";
		message += "<pre>- AbnormalSheetName	: " + AbnormalSheetName + "</pre>";
		message += "<pre>- LotName	: " + LotName + "</pre>";
		message += "<pre>- ProductName	: " + ProductName + "</pre>";
		message += "<pre>- AbnormalCode	: " + AbnormalCode + "</pre>";
		message += "<pre>- RS Code	: " + RSCode + "</pre>";
		message += "<pre>- ProcessComment	: " + ProcessComment + "</pre>";
		message += "<pre>- Department	: " + EngDepartment + "</pre>";
		message += "<pre>==============================================</pre>";
		
		List<String> emailList = new ArrayList<String>();
		
		try 
		{
			StringBuffer sql1 = new StringBuffer();
			sql1.append(
					"SELECT B.* FROM CT_ALARMUSERGROUP A, CT_ALARMUSER B WHERE A.USERID = B.USERID  AND A.ALARMGROUPNAME = 'AbnormalSheet' AND B.DEPARTMENT=:DEPARTMENT");
			Map<String, Object> args1 = new HashMap<String, Object>();
			args1.put("DEPARTMENT",EngDepartment );
			List<Map<String, Object>> sqlResult1 = GenericServiceProxy.getSqlMesTemplate().queryForList(sql1.toString(), args1);
			if (sqlResult1.size() > 0) 
			{
				for (Map<String, Object> user : sqlResult1)
				{
					String eMail = ConvertUtil.getMapValueByName(user, "EMAIL");
					emailList.add(eMail);
				}
			}

		}
		catch (Exception e)
		{
			log.error("Not Found the Department of "+ EngDepartment);
			log.error("Failed to send mail.");
		}
		
		if (emailList.size() > 0)
		{
			try
			{
				EMailInterface ei = new EMailInterface("mail.visionox.com", "25", "1000", "1000");
				ei.postMail(emailList,  " Abnormal Sheet Report ", message.toString(), "V3MES", "V3MES@visionox.com", "V3MES", "vis@2019");
				//ei.postMail(emailList,  "Abnormal Sheet ", message.toString(), "V0042854", "xuch@visionox.com", "V0042854", "xuch122.");											
			}  
			catch (Exception e)
			{
				log.error("Failed to send mail.");
			}
			
			//houxk 20210615
			try
			{				
				sendToEm(EngDepartment, message);
			}
			catch (Exception e)
			{
				log.info("eMobile or WeChat Send Error : " + e.getCause());	
			}
		}
	}
	private void InsertAbnormalSheetDetail(String AbnormalSheetName, String ProductName, String AbnormalCode, String RSCode, String ProcessState, String ProcessComment, String TaUSer,
			String EngDepartment, String SlotPosition, EventInfo eventInfo, String rsOperationName, Boolean issueFlag) throws CustomException
	{
		try
		{
			StringBuffer sql = new StringBuffer();
			sql.append("INSERT ");
			sql.append("  INTO CT_ABNORMALSHEETDETAIL  ");
			sql.append("  (ABNORMALSHEETNAME, PRODUCTNAME, ABNORMALCODE, RSCODE, PROCESSSTATE, ");
			sql.append("   PROCESSCOMMENT, TAUSER, ENGDEPARTMENT, SLOTPOSITION, LASTEVENTNAME, ");
			sql.append("   LASTEVENTUSER, LASTEVENTTIME, ABNORMALCOMMENT, CREATETIME, RSPROCESSOPERATIONNAME, ISSUEFLAG) ");
			sql.append("VALUES  ");
			sql.append("  (:ABNORMALSHEETNAME, :PRODUCTNAME, :ABNORMALCODE, :RSCODE, :PROCESSSTATE, ");
			sql.append("   :PROCESSCOMMENT, :TAUSER, :ENGDEPARTMENT, :SLOTPOSITION, :LASTEVENTNAME, ");
			sql.append("   :LASTEVENTUSER, :LASTEVENTTIME, :ABNORMALCOMMENT, :CREATETIME, :RSPROCESSOPERATIONNAME, :ISSUEFLAG) ");
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("ABNORMALSHEETNAME", AbnormalSheetName);
			bindMap.put("PRODUCTNAME", ProductName);
			bindMap.put("ABNORMALCODE", AbnormalCode);
			bindMap.put("RSCODE", RSCode);
			bindMap.put("PROCESSSTATE", ProcessState);
			bindMap.put("PROCESSCOMMENT", ProcessComment);
			bindMap.put("TAUSER", TaUSer);
			bindMap.put("ENGDEPARTMENT", EngDepartment);
			bindMap.put("SLOTPOSITION", SlotPosition);
			bindMap.put("LASTEVENTNAME", eventInfo.getEventName());
			bindMap.put("LASTEVENTUSER", eventInfo.getEventUser());
			bindMap.put("LASTEVENTTIME", eventInfo.getEventTime());
			bindMap.put("ABNORMALCOMMENT", ProcessComment);
			bindMap.put("CREATETIME", eventInfo.getEventTime());
			bindMap.put("RSPROCESSOPERATIONNAME", rsOperationName);
			bindMap.put("ISSUEFLAG", issueFlag.toString());

			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);

			InsertAbnormalSheetDetailHistory(AbnormalSheetName, ProductName, AbnormalCode, RSCode, ProcessState, ProcessComment, TaUSer, EngDepartment, SlotPosition, eventInfo, rsOperationName, issueFlag);
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", "for insert " + AbnormalSheetName + " into CT_ABNORMALSHEETDETAIL   Error : " + e.toString());
		}
	}

	private void UpdateAbnormalSheetDetail(String AbnormalSheetName, String ProductName, String AbnormalCode, String RSCode, String ProcessState, String ProcessComment, String TaUSer,
			String EngDepartment, String SlotPosition, EventInfo eventInfo,String rsOperationName, Boolean issueFlag) throws CustomException
	{
		try
		{
			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE CT_ABNORMALSHEETDETAIL ");
			sql.append("   SET RSCODE = :RSCODE, ");
			sql.append("       PROCESSSTATE = :PROCESSSTATE, ");
			sql.append("       PROCESSCOMMENT = :PROCESSCOMMENT, ");
			sql.append("       ENGDEPARTMENT = :ENGDEPARTMENT, ");
			sql.append("       LASTEVENTNAME = :LASTEVENTNAME, ");
			sql.append("       LASTEVENTUSER = :LASTEVENTUSER, ");
			sql.append("       LASTEVENTTIME = :LASTEVENTTIME ");
			sql.append("       RSPROCESSOPERATIONNAME = :RSPROCESSOPERATIONNAME ");
			sql.append("       ISSUEFLAG = :ISSUEFLAG ");
			sql.append(" WHERE ABNORMALSHEETNAME = :ABNORMALSHEETNAME ");
			sql.append("   AND PRODUCTNAME = :PRODUCTNAME ");
			sql.append("   AND ABNORMALCODE = :ABNORMALCODE ");
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("ABNORMALSHEETNAME", AbnormalSheetName);
			bindMap.put("PRODUCTNAME", ProductName);
			bindMap.put("ABNORMALCODE", AbnormalCode);
			bindMap.put("RSCODE", RSCode);
			bindMap.put("PROCESSSTATE", ProcessState);
			bindMap.put("PROCESSCOMMENT", ProcessComment);
			bindMap.put("ENGDEPARTMENT", EngDepartment);
			bindMap.put("LASTEVENTNAME", eventInfo.getEventName());
			bindMap.put("LASTEVENTUSER", eventInfo.getEventUser());
			bindMap.put("LASTEVENTTIME", eventInfo.getEventTime());
			bindMap.put("RSPROCESSOPERATIONNAME", rsOperationName);
			bindMap.put("ISSUEFLAG", issueFlag.toString());

			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);

			InsertAbnormalSheetDetailHistory(AbnormalSheetName, ProductName, AbnormalCode, RSCode, ProcessState, ProcessComment, TaUSer, EngDepartment, SlotPosition, eventInfo, rsOperationName, issueFlag);
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", "for update " + AbnormalSheetName + " to the CT_ABNORMALSHEETDETAIL   Error : " + e.toString());
		}
	}

	private void DeleteAbnormalSheetDetail(String AbnormalSheetName, String ProductName, String AbnormalCode, String RSCode, String ProcessState, String ProcessComment, String TaUSer,
			String EngDepartment, String SlotPosition, EventInfo eventInfo, String rsOperationName, Boolean issueFlag) throws CustomException
	{
		try
		{
			StringBuffer sql = new StringBuffer();
			sql.append("DELETE FROM CT_ABNORMALSHEETDETAIL \" + \" ");
			sql.append(" WHERE ABNORMALSHEETNAME = :ABNORMALSHEETNAME ");
			sql.append("   AND PRODUCTNAME = :PRODUCTNAME ");
			sql.append("   AND ABNORMALCODE = :ABNORMALCODE ");
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("ABNORMALSHEETNAME", AbnormalSheetName);
			bindMap.put("PRODUCTNAME", ProductName);
			bindMap.put("ABNORMALCODE", AbnormalCode);

			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);

			InsertAbnormalSheetDetailHistory(AbnormalSheetName, ProductName, AbnormalCode, RSCode, ProcessState, ProcessComment, TaUSer, EngDepartment, SlotPosition, eventInfo, rsOperationName, issueFlag);

		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", "for delete " + AbnormalSheetName + " from the CT_ABNORMALSHEETDETAIL");
		}
	}

	private void InsertAbnormalSheetDetailHistory(String AbnormalSheetName, String ProductName, String AbnormalCode, String RSCode, String ProcessState, String ProcessComment, String TaUSer,
			String EngDepartment, String SlotPosition, EventInfo eventInfo,String rsOperationName, Boolean issueFlag) throws CustomException
	{
		try
		{
			StringBuffer sql = new StringBuffer();
			sql.append("INSERT ");
			sql.append("  INTO CT_ABNORMALSHEETDETAILHISTORY  ");
			sql.append("  (ABNORMALSHEETNAME, PRODUCTNAME, ABNORMALCODE, RSCODE, TIMEKEY, ");
			sql.append("   PROCESSSTATE, PROCESSCOMMENT, TAUSER, ENGDEPARTMENT, SLOTPOSITION, ");
			sql.append("   EVENTNAME, EVENTUSER, EVENTTIME, ABNORMALCOMMENT, CREATETIME, RSPROCESSOPERATIONNAME, ISSUEFLAG) ");
			sql.append("VALUES  ");
			sql.append("  (:ABNORMALSHEETNAME, :PRODUCTNAME, :ABNORMALCODE, :RSCODE, :TIMEKEY, ");
			sql.append("   :PROCESSSTATE, :PROCESSCOMMENT, :TAUSER, :ENGDEPARTMENT, :SLOTPOSITION, ");
			sql.append("   :EVENTNAME, :EVENTUSER, :EVENTTIME, :ABNORMALCOMMENT, :CREATETIME, :RSPROCESSOPERATIONNAME, :ISSUEFLAG) ");
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("ABNORMALSHEETNAME", AbnormalSheetName);
			bindMap.put("PRODUCTNAME", ProductName);
			bindMap.put("ABNORMALCODE", AbnormalCode);
			bindMap.put("RSCODE", RSCode);
			bindMap.put("TIMEKEY", eventInfo.getEventTimeKey());
			bindMap.put("PROCESSSTATE", ProcessState);
			bindMap.put("PROCESSCOMMENT", ProcessComment);
			bindMap.put("TAUSER", TaUSer);
			bindMap.put("ENGDEPARTMENT", EngDepartment);
			bindMap.put("SLOTPOSITION", SlotPosition);
			bindMap.put("EVENTNAME", eventInfo.getEventName());
			bindMap.put("EVENTUSER", eventInfo.getEventUser());
			bindMap.put("EVENTTIME", eventInfo.getEventTime());
			bindMap.put("ABNORMALCOMMENT", ProcessComment);
			bindMap.put("CREATETIME", eventInfo.getEventTime());
			bindMap.put("RSPROCESSOPERATIONNAME", rsOperationName);
			bindMap.put("ISSUEFLAG", issueFlag.toString());

			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", "for insert " + AbnormalSheetName + " into CT_ABNORMALSHEETDETAILHISTORY   Error : " + e.toString());
		}
	}
	
	public void sendToEm(String EngDepartment, String message)
	{
		String userList[] = getUserList(EngDepartment,"AbnormalSheet");	
		if(userList == null || userList.length ==0) return;
		
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateAbnormalSheet", "MES", "", "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		try
		{	
			log.info("AbnormalSheetReport Start Send To Emobile & Wechat");	
						
			String title = "AbnormalSheetReport";
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
	//AlarmGroup = AbnormalSheet && CSTDepart
	private String[] getUserList(String EngDepartment, String alarmGroupName)
	{
		String sql = " SELECT B.* FROM CT_ALARMUSERGROUP A, CT_ALARMUSER B  "
				   + " WHERE A.USERID = B.USERID  "
				   + " AND A.ALARMGROUPNAME = :ALARMGROUPNAME AND B.DEPARTMENT=:DEPARTMENT";
		
		List<Map<String, Object>> resultList = null;
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new String[] { alarmGroupName, EngDepartment});
		}
		catch (Exception ex)
		{
			log.info(ex.getCause());
		}

		if(resultList ==null || resultList.size()==0) return null;
		
		return CommonUtil.makeListBySqlResult(resultList, "USERID").toArray(new String[] {});
	}

}
