package kr.co.aim.messolution.alarm.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.GenericServiceProxy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class ADCStepOnlineOffline  extends AsyncHandler{

	private static Log log = LogFactory.getLog(ADCStepOnlineOffline.class);
	@Override
	public void doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "ALARMCODE", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "ALARMSTATE", true);
		String adcState = SMessageUtil.getBodyItemValue(doc, "ALARMSEVERITY", true);
		List<Object[]> updateADCInfoArgList = new ArrayList<Object[]>();
		List<Object> ADCInfoList = new ArrayList<Object>();
		boolean exit=checkExit(productSpecName,processOperationName);
		if(adcState.equals("Online")&&!exit){
			
			ADCInfoList.add(productSpecName);
			ADCInfoList.add(processOperationName);
			ADCInfoList.add(adcState);
			updateADCInfoArgList.add(ADCInfoList.toArray());
			createADCInfo(updateADCInfoArgList);
			
		}
		if(adcState.equals("Offline")&&exit){
			ADCInfoList.add(productSpecName);
			ADCInfoList.add(processOperationName);
			updateADCInfoArgList.add(ADCInfoList.toArray());
			deleteADCInfo(updateADCInfoArgList);
			
		}
		
	}
	public static boolean checkExit(String PRODUCTSPECNAME,String processOperationName ){
		
		String sql1 = "SELECT*FROM CT_ADCOPERATIONINFO WHERE PRODUCTSPECNAME=:PRODUCTSPECNAME AND PROCESSOPERATIONNAME=:PROCESSOPERATIONNAME'";
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("PRODUCTSPECNAME", PRODUCTSPECNAME);
		bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		List<Map<String, Object>> Result =GenericServiceProxy.getSqlMesTemplate().queryForList(sql1, bindMap);
		if (Result.size() > 0 && Result != null)
		{
			return true;
		}
		
		return false;
	}
    public static  void createADCInfo(List<Object[]> updateADCInfoList ) throws CustomException{
		
    	StringBuffer sqlADC = new StringBuffer();
    	sqlADC.append(" INSERT INTO CT_ADCOPERATIONINFO  ");
    	sqlADC.append(" (PRODUCTSPECNAME,PROCESSOPERATIONNAME,ADCSTATE)");
    	sqlADC.append(" VALUES");
    	sqlADC.append(" (?,?,?) ");

		
		MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlADC.toString(), updateADCInfoList);
		
		
		
	}
   public static  void deleteADCInfo(List<Object[]> updateADCInfoList ) throws CustomException{
		
    	StringBuffer sqlADC = new StringBuffer();
    	sqlADC.append(" DELETE CT_ADCOPERATIONINFO  ");
    	sqlADC.append(" WHERE PRODUCTSPECNAME=? AND PROCESSOPERATIONNAME=?");

		
		MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlADC.toString(), updateADCInfoList);
		
		
		
	}

}
