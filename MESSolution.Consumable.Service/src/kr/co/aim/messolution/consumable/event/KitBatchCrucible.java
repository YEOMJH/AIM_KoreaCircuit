package kr.co.aim.messolution.consumable.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.CrucibleLot;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EMailInterface;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.SetMaterialLocationInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class KitBatchCrucible extends SyncHandler {
	private static Log log = LogFactory.getLog(AssignOrganic.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("KitBatchCrucible", getEventUser(), getEventComment(), null, null);
		
		List<Element> crucibleList = SMessageUtil.getBodySequenceItemList(doc, "CRUCIBLELIST", true);		

		for (Element crucible : crucibleList)
		{
			String crucibleName = SMessageUtil.getChildText(crucible, "CRUCIBLENAME", true);
			String chamberName = SMessageUtil.getChildText(crucible, "CHAMBERNAME", true);
			String overTimeFlag = SMessageUtil.getChildText(crucible, "OVERQTIMEFLAG", false);
			String assignTime = SMessageUtil.getChildText(crucible, "ASSIGNTIME", false);
			String kitQtime = SMessageUtil.getChildText(crucible, "KITQTIME", false);
			
			List<Map<String, Object>> organicList = getOrganicListByCrucible(crucibleName);
			
			Durable crucibleData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(crucibleName);
			
			if (!StringUtils.isEmpty(crucibleData.getMaterialLocationName()))
			{
				throw new CustomException("CRUCIBLE-0004", crucibleData.getKey().getDurableName(), crucibleData.getMaterialLocationName());
			}
			
			Machine unitMachineData = this.getUnitMachineData(chamberName);
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitMachineData.getSuperMachineName());
			CrucibleLot crucibleLotData = ExtendedObjectProxy.getCrucibleLotService().getCrucibleLotList(crucibleName).get(0);

			CommonValidation.CheckDurableState(crucibleData);
			
			//send mail
			if (overTimeFlag!= null && overTimeFlag.equals("Y"))
				{
					try
					 {
						 SendEmailForEVAdeparment(eventInfo,crucibleData,assignTime,kitQtime);
					 }
					 catch (Exception e)
					 {
						log.error("Failed to send mail.");
					 }
				}
			
			// Change Location - Organic
			for (Map<String, Object> organic : organicList)
			{
				String organicName = organic.get("CONSUMABLENAME").toString();

				Consumable organicData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(organicName);
				
				if (!StringUtils.isEmpty(organicData.getUdfs().get("MATERIALLOCATIONNAME")))
				{
					throw new CustomException("ORGANIC-0001", organicData.getKey().getConsumableName(), organicData.getUdfs().get("MATERIALLOCATIONNAME"));
				}
				
				SetMaterialLocationInfo setMaterialLocationInfo = new SetMaterialLocationInfo();
				setMaterialLocationInfo.setMaterialLocationName(chamberName);
				setMaterialLocationInfo.getUdfs().put("MACHINENAME", machineData.getKey().getMachineName());
				setMaterialLocationInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MaterialLocation_OnEQP);
				setMaterialLocationInfo.getUdfs().put("KITTIME", TimeStampUtil.toTimeString(eventInfo.getEventTime()));
				setMaterialLocationInfo.getUdfs().put("KITQUANTITY", String.valueOf(organicData.getQuantity()));
				setMaterialLocationInfo.getUdfs().put("KITUSER", eventInfo.getEventUser());
				
				MESConsumableServiceProxy.getConsumableServiceImpl().setMaterialLocation(organicData, setMaterialLocationInfo, eventInfo);
			}

			// Change Location - Crucible
			SetMaterialLocationInfo setMaterialLocationInfo = new SetMaterialLocationInfo();
			setMaterialLocationInfo.setMaterialLocationName(chamberName);
			setMaterialLocationInfo.getUdfs().put("MACHINENAME", machineData.getKey().getMachineName());
			setMaterialLocationInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_ONEQP);
			
			MESDurableServiceProxy.getDurableServiceImpl().setMaterialLocation(crucibleData, setMaterialLocationInfo, eventInfo);
			
			// Change Location - CrucibleLot
			crucibleLotData.setMachineName(machineData.getKey().getMachineName());
			crucibleLotData.setMaterialLocationName(chamberName);
			crucibleLotData.setLastEventComment(eventInfo.getEventComment());
			crucibleLotData.setLastEventName(eventInfo.getEventName());
			crucibleLotData.setLastEventTime(eventInfo.getEventTime());
			crucibleLotData.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());
			crucibleLotData.setLastEventUser(eventInfo.getEventUser());
			crucibleLotData.setKitTime(eventInfo.getEventTime());
			ExtendedObjectProxy.getCrucibleLotService().modify(eventInfo, crucibleLotData);
		}
		
		return doc;											
	}
	
	public Machine getUnitMachineData(String chamberName) throws CustomException
	{	
		Machine UnitMachineData = new Machine();
		List<Map<String,Object>>resultList=null;
		String sql = " SELECT FACTORYNAME,REASONCODETYPE,REASONCODE,DESCRIPTION,SUPERREASONCODE,"
				+ "LEVELNO,AVAILABILITY,SEQ,SIGN,MACHINENAME,BANKTYPE FROM REASONCODE "
				+ "WHERE 1=1  "
				+ "AND REASONCODE = :CHAMBERNAME "
				+ "AND FACTORYNAME = :FACTORYNAME ";

		Map<String,Object> bindMap = new HashMap<>();
		bindMap.put("CHAMBERNAME", chamberName);	
		bindMap.put("FACTORYNAME", "OLED");
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		}catch(Exception ex)
		{
		    log.info(ex.getCause());
		}
		if(resultList.size()>0)
		{
			UnitMachineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(resultList.get(0).get("SUPERREASONCODE").toString());
		}
		return UnitMachineData;
	}
	
	public static List<Map<String, Object>> getOrganicListByCrucible(String crucibleName) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT C.CONSUMABLENAME,D.DURABLENAME,CL.CRUCIBLELOTNAME ");
		sql.append("   FROM DURABLE D, CONSUMABLE C, CT_CRUCIBLELOT CL ");
		sql.append(" WHERE D.DURABLENAME = CL.DURABLENAME ");
		sql.append("   AND C.CRUCIBLELOTNAME = CL.CRUCIBLELOTNAME ");
		sql.append("   AND D.DURABLESTATE = 'InUse' ");
		sql.append("   AND CL.CRUCIBLELOTSTATE = 'Released' ");
		sql.append("   AND D.DURABLENAME = :DURABLENAME ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("DURABLENAME", crucibleName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		return sqlResult;
	}
	
	private void SendEmailForEVAdeparment(EventInfo eventInfo,Durable crucibleData, String assignTime, String kitQtime)
	{
		// TODO Auto-generated method stub
		StringBuffer message = new StringBuffer();
		message.append("<pre>=======================AlarmInformation=======================</pre>");
		message.append("<pre>=========================Dear All=============================</pre>");
		message.append("<pre>	CrucibleName："+crucibleData.getKey().getDurableName()+",OverKitQtime</pre>");
		message.append("<pre>	AssignTime："+assignTime+"</pre>");
		message.append("<pre>	KitQTime："+kitQtime+"Hour"+"</pre>");
		message.append("<pre>	EventName："+eventInfo.getEventName()+"</pre>");
		message.append("<pre>	EventTime："+eventInfo.getEventTime()+"</pre>");	
		message.append("<pre>	EventUser："+eventInfo.getEventUser()+"</pre>");	
		message.append("<pre>	EventComment："+eventInfo.getEventComment()+"</pre>");	
		message.append("<pre>=============================End=============================</pre>");
		
		List<String> emailList = new ArrayList<String>();
		
		String department = "EVA";

		StringBuffer sql1 = new StringBuffer();
		sql1.append(
				"SELECT B.* FROM CT_ALARMUSERGROUP A, CT_ALARMUSER B WHERE A.USERID = B.USERID  AND A.ALARMGROUPNAME = 'OverKitQtime' AND B.DEPARTMENT=:DEPARTMENT");
		Map<String, Object> args1 = new HashMap<String, Object>();

		args1.put("DEPARTMENT", department);
		
		List<Map<String, Object>> sqlResult1 = GenericServiceProxy.getSqlMesTemplate()
				.queryForList(sql1.toString(), args1);
		try 
		{
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
			log.error("Not Found the Department of "+ "EVA");
			log.error("Failed to send mail.");
		}						
		
		if (emailList != null && emailList.size() > 0)
		{
			try
			{
				EMailInterface ei = new EMailInterface("mail.visionox.com", "25", "1000", "1000");
				ei.postMail(emailList,  " Over Kit Qtime ", message.toString(), "V3MES", "V3MES@visionox.com", "V3MES", "vis@2019");
				//ei.postMail(emailList,  " Lot Fail TrackIn ", message.toString(), "V0042748", "houxk@visionox.com", "V0042748", "a970225!!!");
			}
			catch (Exception e)
			{
				log.error("Failed to send mail.");
			}
			//houxk 20210610			
			try
			{				
				sendToEm(department, "OverKitQtime", message.toString());
			}
			catch (Exception e)
			{
				log.info("eMobile or WeChat Send Error : " + e.getCause());	
			}
		}
	}
	
	public void sendToEm(String department, String alarmGroup, String message)
	{
		String[] userList = getUserList(department,alarmGroup);	
		if(userList == null || userList.length ==0) return;
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("KitBatchCrucible", "MES", "", "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		try
		{	
			log.info("OrganicOverKitQtime Start Send To Emobile & Wechat");	
						
			String title = "有机物料上机QTime超时报警";
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
	//department = EVA, alarmGroup = OverKitQtime
	private String[] getUserList(String department, String alarmGroup)
	{
		String sql = " SELECT B.* FROM CT_ALARMUSERGROUP A, CT_ALARMUSER B  "
				   + " WHERE A.USERID = B.USERID  "
				   + " AND A.ALARMGROUPNAME = :ALARMGROUPNAME AND B.DEPARTMENT=:DEPARTMENT";
		
		List<Map<String, Object>> resultList = null;
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new String[] { alarmGroup, department});
		}
		catch (Exception ex)
		{
			log.info(ex.getCause());
		}

		if(resultList ==null || resultList.size()==0) return null;
		
		return CommonUtil.makeListBySqlResult(resultList, "USERID").toArray(new String[] {});
	}
	
}
