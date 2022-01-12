package kr.co.aim.messolution.lot.event.Tray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ComponentHistory;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class ComponentTrayGroupOutUnit extends AsyncHandler 
{
	private static Log log = LogFactory.getLog(ComponentTrayGroupOutUnit.class);
    
	@Override
	public void doWorks(Document doc) throws CustomException {
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String coverName = SMessageUtil.getBodyItemValue(doc, "TRAYGROUPNAME", true);

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(coverName);

		List<Durable> trayDataList = MESDurableServiceProxy.getDurableServiceUtil().getSubTrayListByCoverTray(coverName,false);

		if (trayDataList == null|| trayDataList.size()==0)
		{
			// TRAY-0009: No tray data was found for the {0} tray group.
			throw new CustomException("TRAY-0009", coverName);
		}
		
		String sql = " SELECT LOTNAME,CARRIERNAME,FACTORYNAME,PRODUCTSPECNAME , PRODUCTSPECNAME ,PROCESSFLOWNAME,PROCESSFLOWVERSION,"
				   + "        PROCESSOPERATIONNAME,PROCESSOPERATIONVERSION,PRODUCTIONTYPE,LOTGRADE,PRODUCTREQUESTNAME "
				   + " FROM LOT "
				   + " WHERE 1=1 "
				   + " AND CARRIERNAME IN (:DURABLELIST)";
			
		List<Map<String, Object>> resultList = null;
		Map<String, Object> bindMap = new HashMap<>();
		bindMap.put("DURABLELIST", CommonUtil.makeToStringList(trayDataList));

		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-0010", ex.getMessage());
		}

		if (resultList == null || resultList.size() == 0)
		{
			// TRAY-0010:No Panel data was found for the {0} tray group.
			throw new CustomException("TRAY-0010", coverName);
		}

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ComponentTrayGroupOutUnit", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		List<ComponentHistory> updateList = new ArrayList<>();

		for (Map<String, Object> panelInfo : resultList)
		{
			ComponentHistory dataInfo = new ComponentHistory();
			dataInfo.setTimeKey(eventInfo.getEventTimeKey());
			dataInfo.setProductName(ConvertUtil.getMapValueByName(panelInfo, "LOTNAME"));
			dataInfo.setLotName(ConvertUtil.getMapValueByName(panelInfo, "CARRIERNAME"));
			dataInfo.setEventName(eventInfo.getEventName());
			dataInfo.setToSlotId(0);
			dataInfo.setFromSlotId(0);
			dataInfo.setToSlotPosition("");
			dataInfo.setFromSlotPosition("");
			dataInfo.setEventTime(eventInfo.getEventTime());
			dataInfo.setEventUser(eventInfo.getEventUser());
			dataInfo.setFactoryName(ConvertUtil.getMapValueByName(panelInfo, "FACTORYNAME"));
			dataInfo.setProductSpecName(ConvertUtil.getMapValueByName(panelInfo, "PRODUCTSPECNAME"));
			dataInfo.setProductSpecVersion(ConvertUtil.getMapValueByName(panelInfo, "PRODUCTSPECVERSION"));
			dataInfo.setProcessFlowName(ConvertUtil.getMapValueByName(panelInfo, "PROCESSFLOWNAME"));
			dataInfo.setProcessFlowVersion(ConvertUtil.getMapValueByName(panelInfo, "PROCESSFLOWVERSION"));
			dataInfo.setProcessOperationName(ConvertUtil.getMapValueByName(panelInfo, "PROCESSOPERATIONNAME"));
			dataInfo.setProcessOperationVersion(ConvertUtil.getMapValueByName(panelInfo, "PROCESSOPERATIONVERSION"));
			dataInfo.setProductionType(ConvertUtil.getMapValueByName(panelInfo, "PRODUCTTIONTYPE"));
			dataInfo.setProductType("Panel");
			dataInfo.setMachineName(machineName);
			dataInfo.setMaterialLocationName(unitName);
			dataInfo.setProductGrade(ConvertUtil.getMapValueByName(panelInfo, "LOTGRADE"));
			dataInfo.setProductRequestName(ConvertUtil.getMapValueByName(panelInfo, "PRODUCTREQUESTNAME"));

			updateList.add(dataInfo);
		}

		try
		{
			 ExtendedObjectProxy.getComponentHistoryService().create(eventInfo, updateList);
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-0010", ex.getMessage());
		}
	}
}
