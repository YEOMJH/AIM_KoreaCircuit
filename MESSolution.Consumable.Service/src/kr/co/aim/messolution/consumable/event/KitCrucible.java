package kr.co.aim.messolution.consumable.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.CrucibleLot;
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
import kr.co.aim.greentrack.machine.management.data.Machine;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class KitCrucible extends SyncHandler {
	private static Log log = LogFactory.getLog(AssignOrganic.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String crucibleName = SMessageUtil.getBodyItemValue(doc, "CRUCIBLENAME", true);
		String chamberName = SMessageUtil.getBodyItemValue(doc, "CHAMBERNAME", true);
		String overTimeFlag = SMessageUtil.getBodyItemValue(doc, "OVERTIMEFLAG", true);
		String assignTime = SMessageUtil.getBodyItemValue(doc, "ASSIGNTIME", true);
		String kitQtime = SMessageUtil.getBodyItemValue(doc, "KITQTIME", true);

		List<Element> organicList = SMessageUtil.getBodySequenceItemList(doc, "ORGANICLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("KitCrucible", getEventUser(), getEventComment(), null, null);

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
		if (overTimeFlag!= null && overTimeFlag.equals("True"))
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
		for (Element organic : organicList)
		{
			String organicName = organic.getChildText("CONSUMABLENAME");
			//String overTimeFlag = organic.getChildText("OVERTIMEFLAG");
			//String lifeTimeOpen = organic.getChildText("LIFETIMESTORE");
			Consumable organicData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(organicName);

//			if (overTimeFlag.equals("True"))
//			{
//				sendMail(crucibleData, organicName, lifeTimeOpen, overTimeFlag);
//			}
			
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
		  //houxk add Strat
		crucibleLotData.setLastEventComment(eventInfo.getEventComment());
		crucibleLotData.setLastEventName(eventInfo.getEventName());
		crucibleLotData.setLastEventTime(eventInfo.getEventTime());
		crucibleLotData.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());
		crucibleLotData.setLastEventUser(eventInfo.getEventUser());
		crucibleLotData.setKitTime(eventInfo.getEventTime());
		  //houxk add End
		ExtendedObjectProxy.getCrucibleLotService().modify(eventInfo, crucibleLotData);

		return doc;
	}
	
    //houxk add 20201218
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
		}
	}		
	
	public Machine getUnitMachineData(String chamberName) throws CustomException{
		
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
}
