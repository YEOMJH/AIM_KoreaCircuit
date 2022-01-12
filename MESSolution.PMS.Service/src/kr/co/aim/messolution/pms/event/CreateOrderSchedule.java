package kr.co.aim.messolution.pms.event;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.map.ListOrderedMap;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.pms.PMSServiceProxy;
import kr.co.aim.messolution.pms.management.data.MaintenanceOrder;
import kr.co.aim.messolution.pms.management.data.MaintenanceOrderItem;
import kr.co.aim.messolution.pms.management.data.MaintenanceOrderItemKey;
import kr.co.aim.messolution.pms.management.data.MaintenanceOrderKey;
import kr.co.aim.messolution.pms.management.data.MaintenanceSpec;
import kr.co.aim.messolution.pms.management.data.MaintenanceSpecKey;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class CreateOrderSchedule extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		String maintOrderName = "";
		String maintGroupName = SMessageUtil.getBodyItemValue(doc, "MAINTENANCEGROUPNAME", true);
		String maintSpecName = SMessageUtil.getBodyItemValue(doc, "MAINTENANCESPECNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String cycleType = SMessageUtil.getBodyItemValue(doc, "CYCLETYPE", false);
		String maintType = SMessageUtil.getBodyItemValue(doc, "MAINTENANCETYPE", false);
		String cycleDuration = SMessageUtil.getBodyItemValue(doc, "CYCLEDURATION", false);
		String exeDay = SMessageUtil.getBodyItemValue(doc, "EXEDAY", false);
		String exeStartHour = SMessageUtil.getBodyItemValue(doc, "EXESTARTHOUR", false);
		String exeDuration = SMessageUtil.getBodyItemValue(doc, "EXEDURATION", false);
		String planStartTime = "";
		String planEndTime = "";
		
		String beginDate = null;
		
		long position = 0;
		String maintItem = "";
		
		Calendar cal = Calendar.getInstance(); 
		
		NumberFormat numFormat = NumberFormat.getIntegerInstance();
		
		numFormat.setMinimumIntegerDigits(2);
		
		SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMddHHmmss"); 
		planStartTime = timeFormat.format(cal.getTime());

        
		String year = planStartTime.substring(0, 4);
		int month = Integer.valueOf(planStartTime.substring(4, 6));
		int day = Integer.valueOf(planStartTime.substring(6, 8));
		int hours = Integer.valueOf(planStartTime.substring(8, 10));
		String minute = planStartTime.substring(10, 12);
		
		String startHours = "";
		
		
		String nextHours = String.valueOf(exeStartHour + exeDuration);
		
		String nextMonth = String.valueOf(month + 1) ;
		
		
		String firstWeek = "";
		String secondWeek = "";
		String thirdWeek = "";

		int serial = Integer.valueOf(maintSpecName.substring(2, 4));
		//Insert Order By Cycle Type
		if(cycleType.equalsIgnoreCase("Daily"))
		{
			for(day = 1; day < 32; day++)
			{
				//set Time
				planStartTime = year + nextMonth + numFormat.format(day) + exeStartHour + minute + "00";
				planEndTime = year + nextMonth + numFormat.format(day) + nextHours + minute + "00";	
				
				//setOrderName
				maintOrderName = machineName + "-" + maintType.substring(0, 1) + cycleType.substring(0, 1) + String.valueOf(numFormat.format(Integer.valueOf(serial)));
				
				
				startHours = numFormat.format(Integer.valueOf(exeStartHour));
				nextHours = numFormat.format(Integer.valueOf(startHours) + Integer.valueOf(exeDuration));
				nextMonth = numFormat.format(month + 1);
				
				//set Time
				planStartTime = year + nextMonth + numFormat.format(day) + startHours + minute + "00";
				planEndTime = year + nextMonth + numFormat.format(day) + nextHours + minute + "00";			
					
				//Order
				MaintenanceOrderKey orderKey = new MaintenanceOrderKey(maintOrderName);
				MaintenanceOrder orderData = new MaintenanceOrder(orderKey);
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), null, null);
				
				orderData.setMaintenanceGroupName(maintGroupName);
				orderData.setMaintenanceSpecName(maintSpecName);
				orderData.setMachineName(machineName);
				orderData.setMaintenanceType(maintType);
				orderData.setPlanStartTime(TimeUtils.getTimestamp(planStartTime));
				orderData.setPlanEndTime(TimeUtils.getTimestamp(planEndTime));
				orderData.setCreateTime(TimeUtils.getTimestamp(planStartTime));
				orderData.setCreateTime(eventInfo.getEventTime());
				orderData.setCreateUser(eventInfo.getEventUser());
				orderData.setMaintenanceOrderState("Created");

				PMSServiceProxy.getOrderService().insert(orderData);

				HashMap<String, Object> bindMapSpec = new HashMap<String, Object>();
				String sqlSpec = "SELECT MS.MAINTENANCESPECNAME " +
								 "FROM MAINTENANCESPEC MS " +
								 "WHERE 1 = 1 " +
								 "  AND MS.MAINTENANCEGROUPNAME = :MAINTENANCEGROUPNAME " +
								 "  AND MS.MAINTENANCESPECNAME = :MAINTENANCESPECNAME";

				bindMapSpec.put("MAINTENANCEGROUPNAME", maintGroupName);
				bindMapSpec.put("MAINTENANCESPECNAME", maintSpecName);
				
				List<ListOrderedMap> sqlSpecResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlSpec, bindMapSpec);

				if(sqlSpecResult.size() <= 0)
				{
					//insert Spec
					MaintenanceSpecKey specKey = new MaintenanceSpecKey(maintGroupName, maintSpecName);
					MaintenanceSpec specData = new MaintenanceSpec(specKey);
	
					specData.setKey(specKey);
					specData.setCycleDuration(Integer.parseInt(cycleDuration));
					specData.setCycleType(cycleType);
					specData.setExeDay(Integer.parseInt(exeDay));
					specData.setExeDuration(Integer.parseInt(exeDuration));
					specData.setExeStartHour(Integer.parseInt(exeStartHour));
					specData.setMaintenanceType(maintType);
	
					PMSServiceProxy.getMaintSpecService().insert(specData);	
				}
				for (Element eleMaintSpec : SMessageUtil.getBodySequenceItemList(doc, "MAINTENANCESPECLIST", false))
				{
					//insert OrderItem
					maintItem = SMessageUtil.getChildText(eleMaintSpec, "MAINTENANCEITEM", false);
					String sPosition = SMessageUtil.getChildText(eleMaintSpec, "POSITION", true);
					position = Long.parseLong(sPosition);

					MaintenanceSpecKey msKey = new MaintenanceSpecKey(maintGroupName, maintSpecName);
					MaintenanceSpec itemData = new MaintenanceSpec(msKey);	
					
					//Order Item
					MaintenanceOrderItemKey itemKey = new MaintenanceOrderItemKey(maintOrderName, position);
					MaintenanceOrderItem oiData = new MaintenanceOrderItem(itemKey);

					oiData.setMaintenanceFlag("N");
					oiData.setMaintenanceItem(maintItem);
					
					PMSServiceProxy.getOrderItemService().insert(oiData);
				}

				serial = serial +1;
				
				
				if(day == 31)
				{
					break;
				}
			}
		}
		else if(cycleType.equalsIgnoreCase("Weekly"))
		{
			Calendar currentCal = Calendar.getInstance(); 
			Calendar tempCal = Calendar.getInstance();
			
			tempCal.set(Integer.valueOf(year), month, 1);
			
			for(int i = 0; i < 7; i++)
			{
				if(tempCal.get(Calendar.DAY_OF_WEEK) == Integer.valueOf(exeDay))
				{
					break;
				}
				tempCal.add(tempCal.DATE, 1);
			}			
		
			tempCal.set(Calendar.HOUR, Integer.valueOf(exeStartHour));
			tempCal.set(Calendar.MINUTE, 00);
//			tempCal.add(Calendar.DAY_OF_WEEK, 7);
			
			//set Time
			String temp = timeFormat.format(tempCal.getTime());
			
			planStartTime = temp;
			tempCal.add(tempCal.HOUR, Integer.valueOf(exeDuration));
			String endTemp = timeFormat.format(tempCal.getTime());
			
			planEndTime = endTemp;	

			//setOrderName
			maintOrderName = machineName + "-" + maintSpecName;
					
			//Order
			MaintenanceOrderKey orderKey = new MaintenanceOrderKey(maintOrderName);
			MaintenanceOrder orderData = new MaintenanceOrder(orderKey);
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), null, null);
			
			orderData.setMaintenanceGroupName(maintGroupName);
			orderData.setMaintenanceSpecName(maintSpecName);
			orderData.setMachineName(machineName);
			orderData.setMaintenanceType(maintType);
			orderData.setPlanStartTime(TimeUtils.getTimestamp(planStartTime));
			orderData.setPlanEndTime(TimeUtils.getTimestamp(planEndTime));
			orderData.setCreateTime(TimeUtils.getTimestamp(planStartTime));
			orderData.setCreateTime(eventInfo.getEventTime());
			orderData.setCreateUser(eventInfo.getEventUser());
			orderData.setMaintenanceOrderState("Created");
	
			PMSServiceProxy.getOrderService().insert(orderData);

			HashMap<String, Object> bindMapSpec = new HashMap<String, Object>();
			String sqlSpec = "SELECT MS.MAINTENANCESPECNAME " +
							 "FROM MAINTENANCESPEC MS " +
							 "WHERE 1 = 1 " +
							 "  AND MS.MAINTENANCEGROUPNAME = :MAINTENANCEGROUPNAME " +
							 "  AND MS.MAINTENANCESPECNAME = :MAINTENANCESPECNAME";

			bindMapSpec.put("MAINTENANCEGROUPNAME", maintGroupName);
			bindMapSpec.put("MAINTENANCESPECNAME", maintSpecName);
			
			List<ListOrderedMap> sqlSpecResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlSpec, bindMapSpec);

			if(sqlSpecResult.size() <= 0)
			{
				//insert Spec
				MaintenanceSpecKey specKey = new MaintenanceSpecKey(maintGroupName, maintSpecName);
				MaintenanceSpec specData = new MaintenanceSpec(specKey);

				specData.setKey(specKey);
				specData.setCycleDuration(Integer.parseInt(cycleDuration));
				specData.setCycleType(cycleType);
				specData.setExeDay(Integer.parseInt(exeDay));
				specData.setExeDuration(Integer.parseInt(exeDuration));
				specData.setExeStartHour(Integer.parseInt(exeStartHour));
				specData.setMaintenanceType(maintType);

				PMSServiceProxy.getMaintSpecService().insert(specData);	
			}

			
			for (Element eleMaintSpecWeek : SMessageUtil.getBodySequenceItemList(doc, "MAINTENANCESPECLIST", false))
			{
				maintItem = SMessageUtil.getChildText(eleMaintSpecWeek, "MAINTENANCEITEM", false);
				String sPosition = SMessageUtil.getChildText(eleMaintSpecWeek, "POSITION", true);
				position = Long.parseLong(sPosition);
	
				MaintenanceSpecKey msKey = new MaintenanceSpecKey(maintGroupName, maintSpecName);
				MaintenanceSpec itemData = new MaintenanceSpec(msKey);	
				
				//Order Item
				MaintenanceOrderItemKey itemKey = new MaintenanceOrderItemKey(maintOrderName, position);
				MaintenanceOrderItem oiData = new MaintenanceOrderItem(itemKey);
	
				oiData.setMaintenanceFlag("N");
				oiData.setMaintenanceItem(maintItem);
				
				PMSServiceProxy.getOrderItemService().insert(oiData);
			}
		}
		else if(cycleType.equalsIgnoreCase("Monthly"))
		{			
			startHours = numFormat.format(Integer.valueOf(exeStartHour));
			nextHours = numFormat.format(Integer.valueOf(startHours) + Integer.valueOf(exeDuration));
			nextMonth = numFormat.format(month + 1);
			
			//set Time
			planStartTime = year + numFormat.format(Integer.valueOf(nextMonth)) + numFormat.format(Integer.valueOf(exeDay)) + startHours + minute + "00";
			planEndTime = year + numFormat.format(Integer.valueOf(nextMonth)) + numFormat.format(Integer.valueOf(exeDay)) + nextHours + minute + "00";	

			//setOrderName
			maintOrderName = machineName + "-" + maintSpecName;
			
			//Order
			MaintenanceOrderKey orderKey = new MaintenanceOrderKey(maintOrderName);
			MaintenanceOrder orderData = new MaintenanceOrder(orderKey);
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), null, null);
			
			orderData.setMaintenanceGroupName(maintGroupName);
			orderData.setMaintenanceSpecName(maintSpecName);
			orderData.setMachineName(machineName);
			orderData.setMaintenanceType(maintType);
			orderData.setPlanStartTime(TimeUtils.getTimestamp(planStartTime));
			orderData.setPlanEndTime(TimeUtils.getTimestamp(planEndTime));
			//Attention:setCreateTime�ظ�
			orderData.setCreateTime(TimeUtils.getTimestamp(planStartTime));
			orderData.setCreateTime(eventInfo.getEventTime());
			orderData.setCreateUser(eventInfo.getEventUser());
			orderData.setMaintenanceOrderState("Created");
	
			PMSServiceProxy.getOrderService().insert(orderData);

			HashMap<String, Object> bindMapSpec = new HashMap<String, Object>();
			String sqlSpec = "SELECT MS.MAINTENANCESPECNAME " +
							 "FROM MAINTENANCESPEC MS " +
							 "WHERE 1 = 1 " +
							 "  AND MS.MAINTENANCEGROUPNAME = :MAINTENANCEGROUPNAME " +
							 "  AND MS.MAINTENANCESPECNAME = :MAINTENANCESPECNAME";

			bindMapSpec.put("MAINTENANCEGROUPNAME", maintGroupName);
			bindMapSpec.put("MAINTENANCESPECNAME", maintSpecName);
			
			List<ListOrderedMap> sqlSpecResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlSpec, bindMapSpec);

			if(sqlSpecResult.size() <= 0)
			{
				//insert Spec
				MaintenanceSpecKey specKey = new MaintenanceSpecKey(maintGroupName, maintSpecName);
				MaintenanceSpec specData = new MaintenanceSpec(specKey);

				specData.setKey(specKey);
				specData.setCycleDuration(Integer.parseInt(cycleDuration));
				specData.setCycleType(cycleType);
				specData.setExeDay(Integer.parseInt(exeDay));
				specData.setExeDuration(Integer.parseInt(exeDuration));
				specData.setExeStartHour(Integer.parseInt(exeStartHour));
				specData.setMaintenanceType(maintType);

				PMSServiceProxy.getMaintSpecService().insert(specData);	
			}
			for (Element eleMaintSpec : SMessageUtil.getBodySequenceItemList(doc, "MAINTENANCESPECLIST", false))
			{
				//insert OrderItem
				maintItem = SMessageUtil.getChildText(eleMaintSpec, "MAINTENANCEITEM", false);
				String sPosition = SMessageUtil.getChildText(eleMaintSpec, "POSITION", true);
				position = Long.parseLong(sPosition);

				MaintenanceSpecKey msKey = new MaintenanceSpecKey(maintGroupName, maintSpecName);
				MaintenanceSpec itemData = new MaintenanceSpec(msKey);	
				
				//Order Item
				MaintenanceOrderItemKey itemKey = new MaintenanceOrderItemKey(maintOrderName, position);
				MaintenanceOrderItem oiData = new MaintenanceOrderItem(itemKey);

				oiData.setMaintenanceFlag("N");
				oiData.setMaintenanceItem(maintItem);
				
				PMSServiceProxy.getOrderItemService().insert(oiData);
			}
		}
		return doc;
	}
	


}
