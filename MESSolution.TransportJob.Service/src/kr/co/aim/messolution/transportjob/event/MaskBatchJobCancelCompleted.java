package kr.co.aim.messolution.transportjob.event;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.transportjob.MESTransportServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class MaskBatchJobCancelCompleted extends AsyncHandler {
	private static Log log = LogFactory.getLog(MaskBatchJobCancelCompleted.class);
	/**
	 * MessageSpec [MCS -> TEX]
	 * 
	 * <Body>
	 *    <BATCHJOBNAME />
	 *    <CARRIERNAME/>
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String batchJobName = SMessageUtil.getBodyItemValue(doc, "BATCHJOBNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String transaction = SMessageUtil.getHeaderItemValue(doc, "TRANSACTIONID", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelComplete", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("TRANSPORTLOCKFLAG", "N");
		
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);

		// Update CT_TRANSPORTJOBCOMMAND
		MESTransportServiceProxy.getTransportJobServiceUtil().updateTransportJobCommand(batchJobName, doc, eventInfo);
		
		String alarmCode ="MaskBatchJobCancelByMCS";
		String cancelMessage = "MaskBatchJobCancelByMCS,please confirm!!";

		String message = "<pre>===============MaskBatchJobCancelCompleted================</pre>";
			message += "<pre>- MachineName	: " + eventInfo.getEventUser() + "</pre>";
			message += "<pre>- CST ID	: " + carrierName + "</pre>";
			message += "<pre>- BatchJobName	: " + batchJobName + "</pre>";
			message += "<pre>- Transaction	: " + transaction + "</pre>";
			message += "<pre>- CancelMessage  : " + cancelMessage + "</pre>";	
			message += "<pre>============================================================</pre>";

	     //Send Email
		CommonUtil.maskBatchJobCancelSendAlarmEmail(alarmCode, "MCS",  message,eventInfo.getEventUser(), eventInfo, carrierName, batchJobName, transaction, cancelMessage);	
		
		//houxk 20210609 	
    	try
		{				
    		sendToEm(carrierName,message);
		}
		catch (Exception e)
		{
			log.info("eMobile or WeChat Send Error : " + e.getCause());	
		}
    			
		// SMS not used
		//CommonUtil.maskBatchJobCancelSendSms(alarmCode, "MCS",eventInfo, carrierName, batchJobName, transaction, cancelMessage);
	}
	
	public void sendToEm(String carrierName,String messageInfo)throws CustomException
	{
		String userList[] = getUserList(carrierName, "MaskBatchJobCancelByMCS");	
		if(userList == null || userList.length ==0) return;
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("MaskBatchJobCancelCompleted", "MCS", "", "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		try
		{	
			log.info("MaskBatchJobCancelByMCS Start Send To Emobile & Wechat");	
						
			String title = "MaskBatchJobCancelByMCS";
			String detailtitle = "${}CIM系统消息通知";
			String url = "";
						
			StringBuffer info = new StringBuffer();
			info.append("<pre>=======================AlarmInformation=======================</pre>");
			info.append(messageInfo);
			info.append("<pre>=============================End=============================</pre>");			
			
			String message = info.toString();
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userList, title, detailtitle, message, url);					
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userList, title, detailtitle, message, "");
		}
		catch (Exception e)
		{
			log.info("eMobile or WeChat Send Error : " + e.getCause());	
		} 
	}
	
	//AlarmGroup = MaskBatchJobCancelByMCS && CSTDepart
	private String[] getUserList(String carrierName, String alarmGroupName)
	{		
		String[] userList = null;
		List<Map<String,Object>> resultList = null;
		List<String> sb = new ArrayList<String>();
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT M.DEPARTMENT AS DEPARTMENT1,D.DEPARTMENT AS DEPARTMENT2 FROM CT_MATERIALINFO M,DURABLE D WHERE M.MATERIALTYPE = D.DURABLETYPE AND D.DURABLENAME = :DURABLENAME ");
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("DURABLENAME", carrierName);
	
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		}
		catch(Exception ex)
		{
		    log.info(ex.getCause());
		}
		
		try 
		{
			if (resultList.size() > 0) 
			{
				String departmentAll = ConvertUtil.getMapValueByName(resultList.get(0), "DEPARTMENT1") + "," + ConvertUtil.getMapValueByName(resultList.get(0), "DEPARTMENT2");

				List<String> department =  CommonUtil.splitStringDistinct(",",departmentAll);

				StringBuffer sql1 = new StringBuffer();
				sql1.append(
						"SELECT B.* FROM CT_ALARMUSERGROUP A, CT_ALARMUSER B, USERPROFILE C WHERE A.USERID = B.USERID AND B.USERID = C.USERID AND A.ALARMGROUPNAME = :ALARMGROUPNAME AND B.DEPARTMENT=:DEPARTMENT");
				Map<String, Object> args1 = new HashMap<String, Object>();

				for(int j = 0; j < department.size(); j++)
				{
					args1.put("ALARMGROUPNAME", alarmGroupName);
					args1.put("DEPARTMENT", department.get(j));
					List<Map<String, Object>> sqlResult1 = GenericServiceProxy.getSqlMesTemplate()
							.queryForList(sql1.toString(), args1);
					
					if (sqlResult1.size() > 0) 
					{
						for (Map<String, Object> userInfo : sqlResult1)
						{
							String user = ConvertUtil.getMapValueByName(userInfo, "USERID");
							sb.add(user);
						}						 					 						
					}
				}
				userList = sb.toArray(new String[] {});
			}
		}
		catch (Exception e)
		{
			log.info("Not Found the Department of "+ carrierName);
			log.info(" Failed to send to EMobile, MachineName: " + carrierName);
		}
		
		return userList;
	}
}
