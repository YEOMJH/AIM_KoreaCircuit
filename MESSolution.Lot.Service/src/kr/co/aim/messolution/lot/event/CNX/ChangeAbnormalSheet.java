package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AbnormalSheetDetail;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.LotMultiHold;

import org.apache.commons.collections.OrderedMap;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ChangeAbnormalSheet extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String AbnormalSheetName = SMessageUtil.getBodyItemValue(doc, "ABNORMALSHEETNAME", true);
		String ProductName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String AbnormalCode = SMessageUtil.getBodyItemValue(doc, "ABNORMALCODE", true);
		String ProcessState = SMessageUtil.getBodyItemValue(doc, "PROCESSSTATE", false);
		String ProcessComment = SMessageUtil.getBodyItemValue(doc, "PROCESSCOMMENT", false);
		String EngDepartment = SMessageUtil.getBodyItemValue(doc, "ENGDEPARTMENT", false);
		String TAUser = SMessageUtil.getBodyItemValue(doc, "TAUSER", false);
		String SlotPosition = SMessageUtil.getBodyItemValue(doc, "SLOTPOSITION", false);
		String ActionCode = SMessageUtil.getBodyItemValue(doc, "ACTIONCODE", false);
		String ProcessType = SMessageUtil.getBodyItemValue(doc, "PROCESSTYPE", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		eventInfo.setEventName("Update AbnormalCode.");
		eventInfo.setEventComment("Update AbnormalCode.");
		
		AbnormalSheetDetail sheetData = ExtendedObjectProxy.getAbnormalSheetDetailService().selectByKey(false, new Object[] { AbnormalSheetName, ProductName, AbnormalCode });
		
		if (ProcessState.equalsIgnoreCase("Closed"))
		{
			List<Element> abnormalList = SMessageUtil.getBodySequenceItemList(doc, "ABNORMALCODELIST", false);
			for (Element code : abnormalList)
			{
				sheetData = ExtendedObjectProxy.getAbnormalSheetDetailService().selectByKey(false, new Object[] { AbnormalSheetName, ProductName, code.getChild("ABNORMALCODE").getText() });
				sheetData.setEngDepartment(code.getChild("ENGDEPARTMENT").getText());
				sheetData.setTaUser(code.getChild("TAUSER").getText());
				sheetData.setActionCode(code.getChild("ACTIONCODE").getText());
				sheetData.setProcessComment(code.getChild("PROCESSCOMMENT").getText());
				sheetData.setProcessState(code.getChild("PROCESSSTATE").getText());
				sheetData.setLastEventTime(eventInfo.getEventTime());
				sheetData.setLastEventName("Close AbnormalCode");
				sheetData.setLastEventUser(eventInfo.getEventUser());
				
				ExtendedObjectProxy.getAbnormalSheetDetailService().modify(eventInfo, sheetData);
			}
		}
		else
		{
			int sheetChangedCount = sheetData.getChangeDepartmentCount();
			if (!StringUtils.equals(sheetData.getEngDepartment(), EngDepartment))
			{
				changeDepartment(factoryName, AbnormalSheetName, EngDepartment);
				sheetChangedCount++;
			}
			
			sheetData.setEngDepartment(EngDepartment);
			sheetData.setTaUser(TAUser);
			sheetData.setActionCode(ActionCode);
			sheetData.setProcessComment(ProcessComment);			
			sheetData.setSlotPosition(Integer.valueOf(SlotPosition));		
			sheetData.setChangeDepartmentCount(sheetChangedCount);
			//mantis 0000180
			sheetData.setLastEventTime(eventInfo.getEventTime());
			sheetData.setLastEventName(ProcessState);
			sheetData.setLastEventUser(eventInfo.getEventUser());
			sheetData.setProcessState(ProcessState);
			
			if(StringUtils.equals(ProcessType, "Comment"))
			{
				sheetData.setEngUser(eventInfo.getEventUser());
			}
			
			/*
			if (StringUtils.equals("TransferDepart", ProcessType))
			{
				int sheetChangedLimit = this.getAbnormalSheetChangeLimit();
				if(sheetChangedLimit > 0 && sheetChangedLimit < sheetChangedCount)
				{
					eventLog.info("ChangedDepartmentCount + 1 > AbnormalSheetChangedLimit");
					
					sheetData.setEngDepartment("N");
					sheetData.setProcessState("CountINT");
				}
			}else{
				//mantis 0000180
				int sheetChangedLimit = this.getAbnormalSheetChangeLimit();
				if(sheetChangedLimit > 0 && sheetChangedLimit < sheetChangedCount)
				{
					eventLog.info("ChangedDepartmentCount + 1 > AbnormalSheetChangedLimit");
					
					sheetData.setEngDepartment("N");
					sheetData.setProcessState(ProcessState);
				}else{
					sheetData.setEngDepartment(EngDepartment);
					sheetData.setProcessState(ProcessState);
				}
			}*/
			
			ExtendedObjectProxy.getAbnormalSheetDetailService().modify(eventInfo, sheetData);
		}

		return doc;
	}

	@SuppressWarnings("unchecked")
	private void changeDepartment(String factoryName, String abnormalSheetName, String department)
	{
		String sql = "SELECT LOTNAME "
				   + "FROM CT_ABNORMALSHEET "
				   + "WHERE 1 = 1 "
				   + "  AND ABNORMALSHEETNAME = :ABNORMALSHEETNAME ";
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("ABNORMALSHEETNAME", abnormalSheetName);
		
		List<OrderedMap> resultDataList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, bindMap);
		if (resultDataList == null || resultDataList.size() == 0)
		{
			return ;
		}
		
		String condition = "WHERE 1 = 1 "
						 + "  AND LOTNAME = ? "
						 + "  AND RELEASETYPE = 'Department' ";
		try 
		{
			List<LotMultiHold> dataInfoList = LotServiceProxy.getLotMultiHoldService().select(condition, new Object[] { resultDataList.get(0).get("LOTNAME") });
			if (dataInfoList == null || dataInfoList.size() == 0)
			{
				return ;
			}
			if (factoryName.equals("ARRAY"))
				department = "A" + department;
			else if (factoryName.equals("OLED"))
				department = "C" + department;
			else if (factoryName.equals("TP"))
				department = "T" + department;
			else if (factoryName.equals("POSTCELL"))
				department = "P" + department;
			
			for (LotMultiHold dataInfo : dataInfoList) 
			{
				Map<String, String> udfs = dataInfo.getUdfs();
				udfs.put("REQUESTDEPARTMENT", department);

				LotServiceProxy.getLotMultiHoldService().update(dataInfo);
			}
		} 
		catch (NotFoundSignal n) 
		{
			// TODO: handle exception
		}		

	}
	
	@SuppressWarnings("unchecked")
	private int getAbnormalSheetChangeLimit()
	{
		String sql = "SELECT ENUMVALUE, DESCRIPTION "
				   + "FROM ENUMDEFVALUE "
				   + "WHERE ENUMNAME = 'AbnormalSheetChangeLimit' ";
		
		List<OrderedMap> resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new HashMap<String, String>());
		if(resultList == null || resultList.size() == 0)
		{
			return 0;
		}
		
		return Integer.valueOf(resultList.get(0).get("ENUMVALUE").toString());
	}
}
