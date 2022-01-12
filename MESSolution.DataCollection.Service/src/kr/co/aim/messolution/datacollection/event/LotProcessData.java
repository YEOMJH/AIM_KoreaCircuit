package kr.co.aim.messolution.datacollection.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.datacollection.service.DataCollectionServiceImpl;
import kr.co.aim.messolution.datacollection.service.DataCollectionServiceUtil;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.sys.SystemPropHelper;
import kr.co.aim.greentrack.datacollection.DataCollectionServiceProxy;
import kr.co.aim.greentrack.datacollection.management.data.DCSpec;
import kr.co.aim.greentrack.datacollection.management.data.DCSpecKey;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.apache.commons.collections.map.ListOrderedMap;
import org.jdom.Document;

public class LotProcessData extends AsyncHandler {
 
	@Override
	public void doWorks(Document doc)
		throws CustomException  
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("LotProcessData", getEventUser(), getEventComment(), null, null);
 
		String machineName 			= SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName 			= SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName 			= SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String lotName		 		= SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String carrierName		 	= SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String machineRecipeName 	= SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", false);
		String productSpecName 		= SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", false);
		
		if(!subUnitName.isEmpty())
		{
			unitName = subUnitName;
		}

		// Insert CT_PRODUCTPROCESSDATA, CT_PRODUCTPROCESSDATAITEM
		DataCollectionServiceImpl.insertCTLotProcessData(doc, eventInfo);
	}
	
	private DCSpec getDCSpec(String lotName)
		throws CustomException
	{
		Lot lotData = CommonUtil.getLotInfoByLotName(lotName);
		
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("SELECT C.DCSpecName, C.DCSpecVersion" + "\n")
			.append("    FROM TFOMPolicy C" + "\n")
			.append(" WHERE C.factoryName = ?" + "\n")
			.append("    AND C.processFlowName = ?" + "\n")
			.append("    AND C.processFlowVersion = ?" + "\n")
			.append("    AND C.processOperationName = ?" + "\n")
			.append("    AND C.processOperationVersion = ?" + "\n")
			.append("    AND C.machineName = ?" + "\n");
		
		Object[] bindArray = new Object[] {lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(),
				lotData.getProcessOperationName(), lotData.getProcessOperationVersion(),lotData.getMachineName()};
		
		List<ListOrderedMap> result;
		try
		{
			result = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuilder.toString(), bindArray);
		}
		catch (FrameworkErrorSignal fe)
		{
			result = new ArrayList<ListOrderedMap>();
		}
		
		if (result.size() < 1)
			throw new CustomException("SYS-1501", "not defined DC spec in TFOM policy");
		
		String dcSpecName = CommonUtil.getValue(result.get(0), "DCSPECNAME");
		String dcSpecVersion = CommonUtil.getValue(result.get(0), "DCSPECVERSION");
		
		DCSpecKey keyInfo = new DCSpecKey();
		keyInfo.setDCSpecName(dcSpecName);
		keyInfo.setDCSpecVersion(dcSpecVersion);
		
		try
		{
			DCSpec dcSpecData = DataCollectionServiceProxy.getDCSpecService().selectByKey(keyInfo);
			
			return dcSpecData;
		}
		catch (Exception ex)
		{
			throw new CustomException("SYS-9001", "DCSpec");
		}
	}
}
