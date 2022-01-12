package kr.co.aim.messolution.extended.webinterface.service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.sun.org.apache.bcel.internal.generic.RETURN;

import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.transport.http.HttpTransportProperties;

import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.extended.webinterface.webservice.SendMessageServiceStub;
import kr.co.aim.messolution.extended.webinterface.webservice.ServiceMessageCustomStub;
import kr.co.aim.messolution.extended.webinterface.webservice.ServiceMessageCustomStub.ArrayOfString;
import kr.co.aim.messolution.extended.webinterface.webservice.ServiceMessageCustomStub.SendCustomMessage_Login;
import kr.co.aim.messolution.extended.webinterface.webservice.ServiceMessageCustomStub.SendCustomMessage_WorkCode;
import kr.co.aim.messolution.extended.webinterface.webservice.VXG288_SAP_MaterialTransServiceV3_pttBindingQSServiceStub;
import kr.co.aim.messolution.extended.webinterface.webservice.VXG288_SAP_MaterialTransServiceV3_pttBindingQSServiceStub.VXG288_SAP_MaterialTransServiceV3;
import kr.co.aim.messolution.extended.webinterface.webservice.VXG416_SAP_QualityLossService_pttBindingQSServiceStub;
import kr.co.aim.messolution.extended.webinterface.webservice.VXG416_SAP_QualityLossService_pttBindingQSServiceStub.VXG416_SAP_QualityLossService;
import kr.co.aim.messolution.extended.webinterface.webservice.VXG_366_MDM_SendMsgToMDM_pttBindingQSServiceStub;
import kr.co.aim.messolution.extended.webinterface.webservice.VXG_366_MDM_SendMsgToMDM_pttBindingQSServiceStub.VXG_366_MDM_SendMsgToMDM;
import kr.co.aim.messolution.extended.webinterface.webservice.VXG_370_SAP_CloseOrder_pttBindingQSServiceStub;
import kr.co.aim.messolution.extended.webinterface.webservice.VXG_370_SAP_CloseOrder_pttBindingQSServiceStub.VXG_370_SAP_CloseOrder;
import kr.co.aim.messolution.extended.webinterface.webservice.VXG_371_SAP_WorkOrderReceipt_pttBindingQSServiceStub;
import kr.co.aim.messolution.extended.webinterface.webservice.VXG_371_SAP_WorkOrderReceipt_pttBindingQSServiceStub.VXG_371_SAP_WorkOrderReceipt;
import kr.co.aim.messolution.extended.webinterface.webservice.VXG_372_SAP_InputMaterial_pttBindingQSServiceStub;
import kr.co.aim.messolution.extended.webinterface.webservice.VXG_372_SAP_InputMaterial_pttBindingQSServiceStub.VXG_372_SAP_InputMaterial;
import kr.co.aim.messolution.extended.webinterface.webservice.VXG_406_SAP_MASKProductionDataPush_pttBindingQSServiceStub;
import kr.co.aim.messolution.extended.webinterface.webservice.VXG_406_SAP_MASKProductionDataPush_pttBindingQSServiceStub.VXG_406_SAP_MASKProductionDataPush;
import kr.co.aim.messolution.extended.webinterface.webservice.VXG_429_OA_CreateRequestBasicService_pttBindingQSServiceStub;
import kr.co.aim.messolution.extended.webinterface.webservice.VXG_429_OA_CreateRequestBasicService_pttBindingQSServiceStub.VXG_429_OA_CreateRequestBasicService;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class ExtendedWebInterfaceServiceImpl implements ApplicationContextAware {

	private static Log log = LogFactory.getLog(ExtendedWebInterfaceServiceImpl.class);
	
	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		// TODO Auto-generated method stub
		applicationContext = arg0;
	}
	
	public String workOrderClose(EventInfo eventInfo, List<Map<String, String>> dataInfoMapList, int numberOfSend) throws CustomException
	{
		String bizTransactionId = "SAP.VXG-370" + eventInfo.getEventTimeKey();
		String Customer = "SAP";
		String param = StringUtils.EMPTY;
		String esbFlag = StringUtils.EMPTY;
		String resultCode = StringUtils.EMPTY;
		String resultComment = StringUtils.EMPTY;
		
		String userID=CommonUtil.getEnumDefValueStringByEnumName("SAPWebServiceUser");
		String passWord=CommonUtil.getEnumDefValueStringByEnumName("SAPWebServicePassword");
		
		try
		{
			VXG_370_SAP_CloseOrder_pttBindingQSServiceStub stub = new VXG_370_SAP_CloseOrder_pttBindingQSServiceStub();
			
			HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
			auth.setUsername(userID);
			auth.setPassword(passWord);
 
			stub._getServiceClient().getOptions().setProperty(HTTPConstants.AUTHENTICATE, auth);

			param = ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceUtil().generateXml(bizTransactionId, Customer, dataInfoMapList);
			
			VXG_370_SAP_CloseOrder closeOrder = new VXG_370_SAP_CloseOrder();
			
			closeOrder.setData(param);
			
			kr.co.aim.messolution.extended.webinterface.webservice.
				VXG_370_SAP_CloseOrder_pttBindingQSServiceStub.Response response 
					= stub.vXG_370_SAP_CloseOrder(closeOrder);
			
			log.info("Response getMESSAGE() = " + response.getMessage());
			log.info("Response getSIGN() = " + response.getSIGN());
			
			if (numberOfSend <= 3 && "F".equals(response.getSIGN()))
			{
				log.info("SIGN value is 'F'. Resend I/F Message. Number of Send = " + numberOfSend);
				workOrderClose(eventInfo, dataInfoMapList, numberOfSend + 1);
			}
			
			esbFlag = "Y";
			resultCode = response.getSIGN();
			resultComment = response.getMessage();
		}
		catch (Exception ex)
		{
			log.info("VXG_370_SAP_CloseOrder Error : " + ex.getMessage());
			
			esbFlag = "N";
			resultCode = "E";
			if(ex.getMessage()!=null)
			{
				resultComment = ex.getMessage();
			}
			else
			{
				resultComment="UnKnown";
			}

		}
		if(StringUtils.contains(resultComment, "工单关闭成功"))
		{
			resultCode="S";
		}
		else
		{
			resultCode="E";
		}
		GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
		this.writeInterfaceLog("VXG_370_SAP_CloseOrder", eventInfo, bizTransactionId, param, String.valueOf(numberOfSend), esbFlag, resultCode, resultComment,dataInfoMapList);
		GenericServiceProxy.getTxDataSourceManager().commitTransaction();
		
		if (numberOfSend > 3 && !"S".equals(esbFlag))
		{
			log.info("Retransmitted three times, but failed. Send to alarm mail.");
			this.sendToAlarmMail(resultCode, resultComment);
		}
		return resultCode;
	}
	
	

	private void sendToAlarmMail(String resultCode, String resultComment)
	{
		log.info("Send Alarm Mail. Resultcode = " + resultCode + ", ResultComment = " + resultComment);
	}
	
	private void writeInterfaceLog(String messageName, EventInfo eventInfo, String bizTransactionId, String param, String numberOfSend, String esbFlag, String resultCode, String resultComment,List<Map<String, String>> dataList) throws CustomException
	{
		String sql = "INSERT INTO ZPP_MES_ORDEROFF_ESB "
				   + "(SEQ, PRODUCTREQUESTNAME,ZCANCEL, EVENTUSER, EVENTNAME, EVENTTIME, ESBFLAG, RESULT, RESULTMESSAGE) "
				   + "VALUES "
				   + "(:SEQ, :PRODUCTREQUESTNAME,:ZCANCEL, :EVENTUSER, :EVENTNAME, :EVENTTIME, :ESBFLAG, :RESULT, :RESULTMESSAGE) ";
		Map<String, String> map = dataList.get(0);
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("SEQ", eventInfo.getEventTimeKey() );
		bindMap.put("PRODUCTREQUESTNAME",map.get("PRODUCTREQUESTNAME"));
		bindMap.put("ZCANCEL",map.get("ZCANCEL"));
		bindMap.put("EVENTUSER", eventInfo.getEventUser());
		bindMap.put("EVENTNAME", eventInfo.getEventName());
		bindMap.put("EVENTTIME", eventInfo.getEventTimeKey());
		bindMap.put("ESBFLAG", "Y");
		bindMap.put("RESULT", resultCode);
		bindMap.put("RESULTMESSAGE", resultComment);
		
		try
		{
			GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		}
		catch (Exception e)
		{
			throw new CustomException(e);
		}	
		
		log.debug(String.format("TRX[%s] inserted", bindMap.get("SEQ")));
	}
	
	private void writeInterfaceLogZPP_MES_GOODSISSUE_ESB(String messageName, EventInfo eventInfo, String bizTransactionId, String param, String numberOfSend, String esbFlag, String resultCode, String resultComment, List<Map<String, String>> dataList, String consumeUnit)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("INSERT INTO ZPP_MES_GOODSISSUE_ESB(SEQ, PRODUCTREQUESTNAME, MATERIALSPECNAME, PROCESSOPERATIONNAME, QUANTITY, ");
		sql.append("    CONSUMEUNIT, FACTORYCODE, FACTORYPOSITION, BATCHNO, PRODUCTQUANTITY, ");
		sql.append("    EVENTUSER, EVENTNAME, EVENTTIME, CANCELFLAG, WSFLAG, ");
		sql.append("    ESBFLAG, RESULT, RESULTMESSAGE) VALUES ");
		sql.append("    (:SEQ, :PRODUCTREQUESTNAME, :MATERIALSPECNAME, :PROCESSOPERATIONNAME, :QUANTITY, ");
		sql.append("    :CONSUMEUNIT, :FACTORYCODE, :FACTORYPOSITION, :BATCHNO, :PRODUCTQUANTITY, ");
		sql.append("    :EVENTUSER, :EVENTNAME, :EVENTTIME, :CANCELFLAG, :WSFLAG, ");
		sql.append("    :ESBFLAG, :RESULT, :RESULTMESSAGE) ");

		Map<String, String> map = dataList.get(0);
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("SEQ", map.get("SEQ"));
		bindMap.put("PRODUCTREQUESTNAME", map.get("PRODUCTREQUESTNAME"));
		bindMap.put("MATERIALSPECNAME", map.get("MATERIALSPECNAME"));
		bindMap.put("PROCESSOPERATIONNAME", map.get("PROCESSOPERATIONNAME"));
		bindMap.put("QUANTITY", map.get("QUANTITY"));
		
		bindMap.put("CONSUMEUNIT", consumeUnit);
		bindMap.put("FACTORYCODE", map.get("FACTORYCODE"));
		bindMap.put("FACTORYPOSITION", map.get("FACTORYPOSITION"));
		bindMap.put("BATCHNO", map.get("BATCHNO"));
		bindMap.put("PRODUCTQUANTITY", map.get("PRODUCTQUANTITY"));
		
		bindMap.put("EVENTUSER", map.get("EVENTUSER"));
		bindMap.put("EVENTNAME", map.get("EVENTNAME"));
		bindMap.put("EVENTTIME", map.get("EVENTTIME"));
		bindMap.put("CANCELFLAG", map.get("CANCELFLAG"));
		bindMap.put("WSFLAG", map.get("WSFLAG"));
		
		bindMap.put("ESBFLAG", esbFlag);
		bindMap.put("RESULT", resultCode);
		bindMap.put("RESULTMESSAGE", resultComment);
		
		GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
		
		log.debug(String.format("TRX[%s] inserted", bindMap.get("BIZTRANSACTIONID")));
	}
	
	private void writeInterfaceLogZPP_MES_GOODSISSUE_ESBBatch(String messageName, EventInfo eventInfo, String bizTransactionId, String param, String numberOfSend, String esbFlag, String resultCode, String resultComment, List<List<Map<String, String>>> dataList) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("INSERT INTO ZPP_MES_GOODSISSUE_ESB(SEQ, PRODUCTREQUESTNAME, MATERIALSPECNAME, PROCESSOPERATIONNAME, QUANTITY, ");
		sql.append("    CONSUMEUNIT, FACTORYCODE, FACTORYPOSITION, BATCHNO, PRODUCTQUANTITY, ");
		sql.append("    EVENTUSER, EVENTNAME, EVENTTIME, CANCELFLAG, WSFLAG, ");
		sql.append("    ESBFLAG, RESULT, RESULTMESSAGE) VALUES ");
		sql.append("    (?, ?, ?, ?, ?, ");
		sql.append("    ?, ?, ?, ?, ?, ");
		sql.append("    ?, ?, ?, ?, ?, ");
		sql.append("    ?, ?, ?) ");
		
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		
		for (List<Map<String, String>> list : dataList)
		{
			Map<String, String> map = list.get(0);
			
            List<Object> lotBindList = new ArrayList<Object>();			
			lotBindList.add(map.get("SEQ")); 
			lotBindList.add(map.get("PRODUCTREQUESTNAME")); 
			lotBindList.add(map.get("MATERIALSPECNAME")); 
			lotBindList.add(map.get("PROCESSOPERATIONNAME")); 
			lotBindList.add(map.get("QUANTITY")); 
			lotBindList.add(map.get("CONSUMEUNIT")); 
			lotBindList.add(map.get("FACTORYCODE")); 
			lotBindList.add(map.get("FACTORYPOSITION")); 
			lotBindList.add(map.get("BATCHNO")); 
			lotBindList.add(map.get("PRODUCTQUANTITY")); 
			lotBindList.add(map.get("EVENTUSER")); 
			lotBindList.add(map.get("EVENTNAME")); 
			lotBindList.add(map.get("EVENTTIME")); 
			lotBindList.add(map.get("CANCELFLAG")); 
			lotBindList.add(map.get("WSFLAG")); 
			lotBindList.add(esbFlag); 
			lotBindList.add(resultCode); 
			lotBindList.add(""); 
			updateLotArgList.add(lotBindList.toArray());
		}
		MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), updateLotArgList);
	}
	
	private void writeInterfaceLogZPP_MES_ORDERSHIP_ESB(String messageName, EventInfo eventInfo, String bizTransactionId, String param, String numberOfSend, String esbFlag, String resultCode, String resultComment, List<Map<String, String>> dataList)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("INSERT INTO ZPP_MES_ORDERSHIP_ESB(SEQ, PRODUCTREQUESTNAME, MATERIALSPECNAME, PRODUCTQUANTITY, PRODUCTTYPE,  ");
		sql.append("    FACTORYCODE, FACTORYPOSITION, BATCHNO, UNSHIPFLAG, EVENTUSER,  ");
		sql.append("    EVENTNAME, EVENTTIME, NGFLAG, ESBFLAG, RESULT,  ");
		sql.append("    RESULTMESSAGE) VALUES ");
		sql.append("    (:SEQ, :PRODUCTREQUESTNAME, :PRODUCTSPECNAME, :PRODUCTQUANTITY, :PRODUCTTYPE,  ");
		sql.append("    :FACTORYCODE, :FACTORYPOSITION, :BATCHNO, :UNSHIPFLAG, :EVENTUSER,  ");
		sql.append("    :EVENTNAME, :EVENTTIME, :NGFLAG, :ESBFLAG, :RESULT,  ");
		sql.append("    :RESULTMESSAGE) ");

		Map<String, String> map = dataList.get(0);
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("SEQ", map.get("SEQ"));
		bindMap.put("PRODUCTREQUESTNAME", map.get("PRODUCTREQUESTNAME"));
		bindMap.put("PRODUCTSPECNAME", map.get("PRODUCTSPECNAME"));
		bindMap.put("PRODUCTQUANTITY", map.get("PRODUCTQUANTITY"));
		bindMap.put("PRODUCTTYPE", map.get("PRODUCTTYPE"));
		bindMap.put("FACTORYCODE", map.get("FACTORYCODE"));
		bindMap.put("FACTORYPOSITION", map.get("FACTORYPOSITION"));
		bindMap.put("BATCHNO", map.get("BATCHNO"));
		bindMap.put("UNSHIPFLAG", map.get("UNSHIPFLAG"));
		bindMap.put("EVENTUSER", eventInfo.getEventUser());
		bindMap.put("EVENTNAME", eventInfo.getEventName());
		bindMap.put("EVENTTIME", map.get("EVENTTIME"));
		bindMap.put("NGFLAG", map.get("NGFLAG"));
		bindMap.put("ESBFLAG", esbFlag);
		bindMap.put("RESULT", resultCode);
		bindMap.put("RESULTMESSAGE", resultComment);
		
		GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
		
		log.debug(String.format("TRX[%s] inserted", bindMap.get("BIZTRANSACTIONID")));
	}
	
	private void writeInterfaceLogMASKProductionDataPush(String messageName, EventInfo eventInfo, String bizTransactionId, String param, String numberOfSend, String esbFlag, String resultCode, String resultComment, List<Map<String, String>> dataList)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("INSERT INTO ZPP_MES_MASKProductionDataPush_ESB(TIMEKEY,PROJECTPRODUCTREQUESTNAME, MASKSPECNAME, MASKMATERIALSPECNAME, VERSION, VERSIONCHANGEDFLAG,  ");
		sql.append("    TENSIONQUANTITY, SCRAPQUANTITY, SHIPQUANTITY, UNSHIPQUANTITY, EVENTTIME,  ");
		sql.append("    ESBFLAG,RESULT, RESULTMESSAGE)  ");
		sql.append("     VALUES ");
		sql.append("    (:TIMEKEY, :PROJECTPRODUCTREQUESTNAME, :MASKSPECNAME, :MASKMATERIALSPECNAME, :VERSION,  ");
		sql.append("    :VERSIONCHANGEDFLAG, :TENSIONQUANTITY, :SCRAPQUANTITY, :SHIPQUANTITY, :UNSHIPQUANTITY,  ");
		sql.append("    :EVENTTIME, :ESBFLAG, :RESULT, :RESULTMESSAGE) ");

		Map<String, String> map = dataList.get(0);
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("TIMEKEY", eventInfo.getEventTimeKey());
		bindMap.put("PROJECTPRODUCTREQUESTNAME", map.get("AUFNR"));
		bindMap.put("MASKSPECNAME", map.get("ZCPLH"));
		bindMap.put("MASKMATERIALSPECNAME", map.get("ZZJLH"));
		bindMap.put("VERSION", map.get("ZBB"));
		bindMap.put("VERSIONCHANGEDFLAG", map.get("ZGB"));
		bindMap.put("TENSIONQUANTITY", map.get("ZHYL"));
		bindMap.put("SCRAPQUANTITY", map.get("ZGHL"));
		bindMap.put("SHIPQUANTITY", map.get("ZFXCKL"));
		bindMap.put("UNSHIPQUANTITY", map.get("ZFXRKL"));
		bindMap.put("EVENTTIME", map.get("BUDAT_MKPT"));
		bindMap.put("ESBFLAG","Y");
		bindMap.put("RESULT",resultCode);
		bindMap.put("RESULTMESSAGE", resultComment);
		GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
		
		log.debug(String.format("TRX[%s] inserted", bindMap.get("BIZTRANSACTIONID")));
	}
	
	private void writeInterfaceLogMaterialTransDataPush(String messageName, EventInfo eventInfo, String bizTransactionId, String param, String numberOfSend, String esbFlag, String resultCode, String resultComment, List<Map<String, String>> headDataList,List<Map<String, String>> bodyDataList)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("INSERT INTO ZPP_MES_MATERIALTRANS_ESB(SEQ,EVENTTIME, MOVETYPE, COSTDEPARTMENT, FACTORYCODE, FACTORYPOSITION, PRODUCTQUANTITY,   ");
		sql.append("    PRODUCTREQUESTNAME, PRODUCTSPECNAME, PRODUCTTYPE, ESBFLAG, RESULT, RESULTMESSAGE)  ");
		sql.append("     VALUES ");
		sql.append("    (:SEQ, :EVENTTIME, :MOVETYPE, :COSTDEPARTMENT, :FACTORYCODE,:FACTORYPOSITION, :PRODUCTQUANTITY, :PRODUCTREQUESTNAME,   ");
		sql.append("    :PRODUCTSPECNAME, :PRODUCTTYPE, :ESBFLAG, :RESULT, :RESULTMESSAGE)  ");

		Map<String, String> bodyMap = bodyDataList.get(0);
		Map<String, String> headMap = headDataList.get(0);
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("SEQ", TimeUtils.getCurrentEventTimeKey());
		bindMap.put("EVENTTIME", headMap.get("BUDAT"));
		bindMap.put("MOVETYPE", headMap.get("BWART"));
		bindMap.put("COSTDEPARTMENT", headMap.get("KOSTL"));
		bindMap.put("FACTORYCODE", headMap.get("WERKS"));
		bindMap.put("FACTORYPOSITION", headMap.get("LGORT_C"));
		bindMap.put("PRODUCTQUANTITY", bodyMap.get("MENGE"));
		bindMap.put("PRODUCTREQUESTNAME", bodyMap.get("CHARG_R"));
		bindMap.put("PRODUCTSPECNAME", bodyMap.get("MATNR"));
		bindMap.put("PRODUCTTYPE", bodyMap.get("MEINS"));
		bindMap.put("ESBFLAG", "Y");
		bindMap.put("RESULT",resultCode);
		bindMap.put("RESULTMESSAGE",resultComment);

		GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
		
		log.debug(String.format("TRX[%s] inserted", bindMap.get("SEQ")));
	}
	
	public void sapVXG372Send(EventInfo eventInfo, List<Map<String, String>> dataInfoMapList, int numberOfSend, String consumeUnit) throws Exception
	{
		String url = "http://10.1.32.11:8010/WP_V3/APP_SAP_SERVICE/Proxy_Services/TA_MES/VXG_372_SAP_InputMaterial_PS";
		String bizTransactionId = "SAP.VXG-372" + eventInfo.getEventTimeKey();
		
		String Customer = "SAP";
		String param = StringUtils.EMPTY;
		String esbFlag = StringUtils.EMPTY;
		String resultCode = StringUtils.EMPTY;
		String resultComment = StringUtils.EMPTY;
		String userID=CommonUtil.getEnumDefValueStringByEnumName("SAPWebServiceUser");
		String passWord=CommonUtil.getEnumDefValueStringByEnumName("SAPWebServicePassword");
		
		try
		{
			VXG_372_SAP_InputMaterial_pttBindingQSServiceStub stub = new VXG_372_SAP_InputMaterial_pttBindingQSServiceStub(url);
			
			HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
			auth.setUsername(userID);
			auth.setPassword(passWord);
 
			stub._getServiceClient().getOptions().setProperty(HTTPConstants.AUTHENTICATE, auth);

			param = ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceUtil().generateXml(bizTransactionId, Customer, dataInfoMapList);
			
			VXG_372_SAP_InputMaterial vXG372_SAP_InputMaterial0 = new VXG_372_SAP_InputMaterial();
			vXG372_SAP_InputMaterial0.setData(param);

			kr.co.aim.messolution.extended.webinterface.webservice.VXG_372_SAP_InputMaterial_pttBindingQSServiceStub.Response response = stub.vXG_372_SAP_InputMaterial(vXG372_SAP_InputMaterial0);
		
			log.info("Response getMESSAGE() = " + response.getMessage());
			log.info("Response getSIGN() = " + response.getSIGN());
			
			if (numberOfSend <= 3 && "F".equals(response.getSIGN()))
			{
				log.info("SIGN value is 'F'. Resend I/F Message. Number of Send = " + numberOfSend);
				sapVXG372Send(eventInfo, dataInfoMapList, numberOfSend + 1, consumeUnit);
			}
			
			esbFlag = "Y";
			//resultCode = response.getSIGN();
			resultComment = response.getMessage();
			if(StringUtils.isNotEmpty(resultComment)&&StringUtils.contains(resultComment, "事务执行成功"))
			{
				resultCode="S";
			}
			else
			{
				resultCode="E";
			}
		}
		catch (Exception ex)
		{
			log.info("sapVXG372Send Error : " + ex.getMessage());
			
			esbFlag = "N";
			resultCode = "E";
			if(ex.getMessage()!=null)
			{
				resultComment = ex.getMessage();
			}
			else
			{
				resultComment="UnKnown";
			}
		}
		
		this.writeInterfaceLogZPP_MES_GOODSISSUE_ESB("VXG_372_SAP_InputMaterial", eventInfo, bizTransactionId, param, String.valueOf(numberOfSend), esbFlag, resultCode, resultComment, dataInfoMapList, consumeUnit);
		
		if (numberOfSend > 3 && !"N".equals(esbFlag))
		{
			log.info("Retransmitted three times, but failed. Send to alarm mail.");
			this.sendToAlarmMail(resultCode, resultComment);
		}
	}
	
	public void sapVXG372SendBatch(EventInfo eventInfo, List<List<Map<String, String>>> dataInfoMapList, int numberOfSend) throws Exception
	{
		String url = "http://10.1.32.11:8010/WP_V3/APP_SAP_SERVICE/Proxy_Services/TA_MES/VXG_372_SAP_InputMaterial_PS";
		String bizTransactionId = "SAP.VXG-372" + eventInfo.getEventTimeKey();
		
		String Customer = "SAP";
		String param = StringUtils.EMPTY;
		String esbFlag = StringUtils.EMPTY;
		String resultCode = StringUtils.EMPTY;
		String resultComment = StringUtils.EMPTY;
		String userID=CommonUtil.getEnumDefValueStringByEnumName("SAPWebServiceUser");
		String passWord=CommonUtil.getEnumDefValueStringByEnumName("SAPWebServicePassword");
		
		try
		{
			VXG_372_SAP_InputMaterial_pttBindingQSServiceStub stub = new VXG_372_SAP_InputMaterial_pttBindingQSServiceStub(url);
			
			HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
			auth.setUsername(userID);
			auth.setPassword(passWord);
 
			stub._getServiceClient().getOptions().setProperty(HTTPConstants.AUTHENTICATE, auth);

			param = ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceUtil().generateXmlBatch(bizTransactionId, Customer, dataInfoMapList);
			
			VXG_372_SAP_InputMaterial vXG372_SAP_InputMaterial0 = new VXG_372_SAP_InputMaterial();
			vXG372_SAP_InputMaterial0.setData(param);

			kr.co.aim.messolution.extended.webinterface.webservice.VXG_372_SAP_InputMaterial_pttBindingQSServiceStub.Response response = stub.vXG_372_SAP_InputMaterial(vXG372_SAP_InputMaterial0);
		
			log.info("Response getMESSAGE() = " + response.getMessage());
			log.info("Response getSIGN() = " + response.getSIGN());
			
			if (numberOfSend <= 3 && "F".equals(response.getSIGN()))
			{
				log.info("SIGN value is 'F'. Resend I/F Message. Number of Send = " + numberOfSend);
				sapVXG372SendBatch(eventInfo, dataInfoMapList, numberOfSend + 1);
			}
			
			esbFlag = "Y";
			resultCode = response.getSIGN();
			resultComment = response.getMessage();
		}
		catch (Exception ex)
		{
			log.info("sapVXG372SendBatch Error : " + ex.getMessage());
			
			esbFlag = "N";
			resultCode = "E";
			if(ex.getMessage()!=null)
			{
				resultComment = ex.getMessage();
			}
			else
			{
				resultComment="UnKnown";
			}
		}
		
		this.writeInterfaceLogZPP_MES_GOODSISSUE_ESBBatch("VXG_372_SAP_InputMaterial", eventInfo, bizTransactionId, param, String.valueOf(numberOfSend), esbFlag, resultCode, resultComment, dataInfoMapList);
		
		if (numberOfSend > 3 && !"N".equals(esbFlag))
		{
			log.info("Retransmitted three times, but failed. Send to alarm mail.");
			this.sendToAlarmMail(resultCode, resultComment);
		}
	}
	
	public void sapVXG371Send(EventInfo eventInfo, List<Map<String, String>> dataInfoMapList, int numberOfSend) throws Exception
	{
		String url = "http://10.1.32.11:8010/WP_V3/APP_SAP_SERVICE/Proxy_Services/VXG_371_SAP_WorkOrderReceipt_PS";
		String bizTransactionId = "SAP.VXG-371" + eventInfo.getEventTimeKey();
		String Customer = "SAP";
		String param = StringUtil.EMPTY;
		String esbFlag = StringUtil.EMPTY;
		String resultCode = StringUtil.EMPTY;
		String resultComment = StringUtil.EMPTY;
		String userID=CommonUtil.getEnumDefValueStringByEnumName("SAPWebServiceUser");
		String passWord=CommonUtil.getEnumDefValueStringByEnumName("SAPWebServicePassword");
		
		try
		{
			VXG_371_SAP_WorkOrderReceipt_pttBindingQSServiceStub stub = new VXG_371_SAP_WorkOrderReceipt_pttBindingQSServiceStub(url);
			
			HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
			auth.setUsername(userID);
			auth.setPassword(passWord);
 
			stub._getServiceClient().getOptions().setProperty(HTTPConstants.AUTHENTICATE, auth);

			
			param = ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceUtil().generateXml(bizTransactionId, Customer, dataInfoMapList);
			
			VXG_371_SAP_WorkOrderReceipt vXG_371_SAP_WorkOrderReceipt10 = new VXG_371_SAP_WorkOrderReceipt();
			vXG_371_SAP_WorkOrderReceipt10.setData(param);

			kr.co.aim.messolution.extended.webinterface.webservice.VXG_371_SAP_WorkOrderReceipt_pttBindingQSServiceStub.Response response = stub.vXG_371_SAP_WorkOrderReceipt(vXG_371_SAP_WorkOrderReceipt10);
			
			log.info("Response getMESSAGE() = " + response.getMessage());
			log.info("Response getSIGN() = " + response.getSIGN());
			
			if (numberOfSend <= 3 && "F".equals(response.getSIGN()))
			{
				log.info("SIGN value is 'F'. Resend I/F Message. Number of Send = " + numberOfSend);
				sapVXG371Send(eventInfo, dataInfoMapList, numberOfSend + 1);
			}
			
			esbFlag = "Y";
			//resultCode = response.getSIGN();
			resultComment = response.getMessage();
			if(StringUtils.isNotEmpty(resultComment)&&StringUtils.contains(resultComment, "工单收货信息接收成功并处理"))
			{
				resultCode="S";
			}
			else
			{
				resultCode="E";
			}
		}
		catch (Exception ex)
		{
			log.info("sapVXG371Send Error : " + ex.getMessage());
			
			esbFlag = "N";
			resultCode = "E";
			if(ex.getMessage()!=null)
			{
				resultComment = ex.getMessage();
			}
			else
			{
				resultComment="UnKnown";
			}
		}
		
		this.writeInterfaceLogZPP_MES_ORDERSHIP_ESB("VXG_371_SAP_WorkOrderReceipt", eventInfo, bizTransactionId, param, String.valueOf(numberOfSend), esbFlag, resultCode, resultComment, dataInfoMapList);
		
		if (numberOfSend > 3 && !"N".equals(esbFlag))
		{
			log.info("Retransmitted three times, but failed. Send to alarm mail.");
			this.sendToAlarmMail(resultCode, resultComment);
		}
	}
	
	public void MDMResultSend(EventInfo eventInfo, List<Map<String, String>> dataInfoMapList, int numberOfSend) throws Exception
	{
		String url = "http://10.1.32.11:8010/WP_HNYG/APP_MDM_SERVICE/Proxy_Services/TA-V3MES/VXG_366_MDM_SendMsgToMDM_PS";
		String bizTransactionId = "MDM.VXG-366" + eventInfo.getEventTimeKey();
		String Customer = "MDM";
		String param = StringUtil.EMPTY;
		String esbFlag = StringUtil.EMPTY;
		String resultCode = StringUtil.EMPTY;
		String resultComment = StringUtil.EMPTY;
		String userID=CommonUtil.getEnumDefValueStringByEnumName("MDMWebServiceUser");
		String passWord=CommonUtil.getEnumDefValueStringByEnumName("MDMWebServicePassword");
		
		try
		{
			VXG_366_MDM_SendMsgToMDM_pttBindingQSServiceStub stub = new VXG_366_MDM_SendMsgToMDM_pttBindingQSServiceStub(url);
			
			HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
			auth.setUsername(userID);
			auth.setPassword(passWord);
 
			stub._getServiceClient().getOptions().setProperty(HTTPConstants.AUTHENTICATE, auth);
			
			param = ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceUtil().generateXmlWithResponse(bizTransactionId, Customer, dataInfoMapList);
			
			VXG_366_MDM_SendMsgToMDM vXG_366_MDM_SendMsgToMDM10 = new VXG_366_MDM_SendMsgToMDM();
			vXG_366_MDM_SendMsgToMDM10.setData(param);

			VXG_366_MDM_SendMsgToMDM_pttBindingQSServiceStub.Response response = stub.vXG_366_MDM_SendMsgToMDM(vXG_366_MDM_SendMsgToMDM10);
			
			log.info("Response getMESSAGE() = " + response.getMessage());
			log.info("Response getSIGN() = " + response.getSIGN());
			
			if (numberOfSend <= 3 && "F".equals(response.getSIGN()))
			{
				log.info("SIGN value is 'F'. Resend I/F Message. Number of Send = " + numberOfSend);
				MDMResultSend(eventInfo, dataInfoMapList, numberOfSend + 1);
			}
			
			esbFlag = "Y";
			resultCode = response.getSIGN();
			resultComment = response.getMessage();
		}
		catch (Exception ex)
		{
			log.info("MDMResultSend Error : " + ex.getMessage());
			
			esbFlag = "N";
			resultCode = "E";
			if(ex.getMessage()!=null)
			{
				resultComment = ex.getMessage();
			}
			else
			{
				resultComment="UnKnown";
			}
		}
		
		if (numberOfSend > 3 && !"N".equals(esbFlag))
		{
			log.info("Retransmitted three times, but failed. Send to alarm mail.");
			this.sendToAlarmMail(resultCode, resultComment);
		}
	}
	public void sapVXG406Send(EventInfo eventInfo, List<Map<String, String>> dataInfoMapList, int numberOfSend) throws Exception
	{
		String url = "http://10.1.32.151:8010/WP_V3/APP_SAP_SERVICE/Proxy_Services/TA_MES/VXG_406_SAP_MASKProductionDataPush_PS";
		String bizTransactionId = "SAP.VXG-406" + eventInfo.getEventTimeKey();
		String Customer = "SAP";
		String param = StringUtil.EMPTY;
		String esbFlag = StringUtil.EMPTY;
		String resultCode = StringUtil.EMPTY;
		String resultComment = StringUtil.EMPTY;
		String userID=CommonUtil.getEnumDefValueStringByEnumName("SAPWebServiceUser");
		String passWord=CommonUtil.getEnumDefValueStringByEnumName("SAPWebServicePassword");
		
		try
		{
			VXG_406_SAP_MASKProductionDataPush_pttBindingQSServiceStub stub = new VXG_406_SAP_MASKProductionDataPush_pttBindingQSServiceStub(url);
			
			HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
			auth.setUsername(userID);
			auth.setPassword(passWord);
 
			stub._getServiceClient().getOptions().setProperty(HTTPConstants.AUTHENTICATE, auth);

			
			param = ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceUtil().generateXml(bizTransactionId, Customer, dataInfoMapList);
			
			VXG_406_SAP_MASKProductionDataPush vXG_406_SAP_MASKProductionDataPush10 = new VXG_406_SAP_MASKProductionDataPush();
			vXG_406_SAP_MASKProductionDataPush10.setData(param);

			kr.co.aim.messolution.extended.webinterface.webservice.VXG_406_SAP_MASKProductionDataPush_pttBindingQSServiceStub.Response response = stub.vXG_406_SAP_MASKProductionDataPush(vXG_406_SAP_MASKProductionDataPush10);
			
			log.info("Response getMESSAGE() = " + response.getMessage());
			log.info("Response getSIGN() = " + response.getSIGN());
			
			if (numberOfSend <= 3 && "F".equals(response.getSIGN()))
			{
				log.info("SIGN value is 'F'. Resend I/F Message. Number of Send = " + numberOfSend);
				sapVXG371Send(eventInfo, dataInfoMapList, numberOfSend + 1);
			}
			
			esbFlag = "Y";
			resultCode = response.getSIGN();
			resultComment = response.getMessage();
		}
		catch (Exception ex)
		{
			log.info("sapVXG406Send Error : " + ex.getMessage());
			
			esbFlag = "N";
			resultCode = "E";
			if(ex.getMessage()!=null)
			{
				resultComment = ex.getMessage();
			}
			else
			{
				resultComment="UnKnown";
			}
		}
		
		this.writeInterfaceLogMASKProductionDataPush("VXG_406_SAP_MASKProductionDataPush", eventInfo, bizTransactionId, param, String.valueOf(numberOfSend), esbFlag, resultCode, resultComment, dataInfoMapList);
		
		if (numberOfSend > 3 && !"N".equals(esbFlag))
		{
			log.info("Retransmitted three times, but failed. Send to alarm mail.");
			this.sendToAlarmMail(resultCode, resultComment);
		}
	}
	
	public void sapVXG416Send(EventInfo eventInfo, List<List<Map<String, String>>> dataInfoMapList, int numberOfSend) throws Exception
	{
		String url = "http://10.1.32.151:8010/WP_V3/APP_SAP_SERVICE/Proxy_Services/TA_MES/VXG-416_SAP_QualityLossService_PS";
		String bizTransactionId = "SAP.VXG-416" + eventInfo.getEventTimeKey();
		String Customer = "SAP";
		String param = StringUtil.EMPTY;
		String esbFlag = StringUtil.EMPTY;
		String resultCode = StringUtil.EMPTY;
		String resultComment = StringUtil.EMPTY;
		String userID=CommonUtil.getEnumDefValueStringByEnumName("SAPWebServiceUser");
		String passWord=CommonUtil.getEnumDefValueStringByEnumName("SAPWebServicePassword");
		
		try
		{
			VXG416_SAP_QualityLossService_pttBindingQSServiceStub stub = new VXG416_SAP_QualityLossService_pttBindingQSServiceStub(url);
			
			HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
			auth.setUsername(userID);
			auth.setPassword(passWord);
 
			stub._getServiceClient().getOptions().setProperty(HTTPConstants.AUTHENTICATE, auth);

			
			param = ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceUtil().generateXmlBatch(bizTransactionId, Customer, dataInfoMapList);
			
			VXG416_SAP_QualityLossService vXG416_SAP_QualityLossService0 = new VXG416_SAP_QualityLossService();
			vXG416_SAP_QualityLossService0.setData(param);

			kr.co.aim.messolution.extended.webinterface.webservice.VXG416_SAP_QualityLossService_pttBindingQSServiceStub.Response response = stub.vXG416_SAP_QualityLossService(vXG416_SAP_QualityLossService0);
			
			log.info("Response getMESSAGE() = " + response.getMessage());
			log.info("Response getSIGN() = " + response.getSIGN());
			
			if (numberOfSend <= 3 && "F".equals(response.getSIGN()))
			{
				log.info("SIGN value is 'F'. Resend I/F Message. Number of Send = " + numberOfSend);
				sapVXG416Send(eventInfo, dataInfoMapList, numberOfSend + 1);
			}
			
			esbFlag = "Y";
			resultCode = response.getSIGN();
			resultComment = response.getMessage();
		}
		catch (Exception ex)
		{
			log.info("sapVXG416Send Error : " + ex.getMessage());
			
			esbFlag = "N";
			resultCode = "E";
			if(ex.getMessage()!=null)
			{
				resultComment = ex.getMessage();
			}
			else
			{
				resultComment="UnKnown";
			}
		}
		
		//this.writeInterfaceLogMASKProductionDataPush("VXG_406_SAP_MASKProductionDataPush", eventInfo, bizTransactionId, param, String.valueOf(numberOfSend), esbFlag, resultCode, resultComment, dataInfoMapList);
		
		if (numberOfSend > 3 && !"N".equals(esbFlag))
		{
			log.info("Retransmitted three times, but failed. Send to alarm mail.");
			this.sendToAlarmMail(resultCode, resultComment);
		}
	}
	
	public void sapVXG288Send(EventInfo eventInfo, List<Map<String, String>> headInfoMapList,List<Map<String, String>> bodyInfoMapList, int numberOfSend, String consumeUnit) throws Exception
	{
		String url = "http://10.1.32.11:8010/WP_V3/APP_SAP_SERVICE/Proxy_Services/TA_WMS/VXG-288_SAP_MaterialTransServiceV3_PS";
		String bizTransactionId = "SAP.VXG-288" + eventInfo.getEventTimeKey();
		
		String Customer = "SAP";
		String param = StringUtils.EMPTY;
		String esbFlag = StringUtils.EMPTY;
		String resultCode = StringUtils.EMPTY;
		String resultComment = StringUtils.EMPTY;
		String userID=CommonUtil.getEnumDefValueStringByEnumName("SAPWebServiceUser");
		String passWord=CommonUtil.getEnumDefValueStringByEnumName("SAPWebServicePassword");
		
		try
		{
			VXG288_SAP_MaterialTransServiceV3_pttBindingQSServiceStub stub = new VXG288_SAP_MaterialTransServiceV3_pttBindingQSServiceStub(url);
			
			HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
			auth.setUsername(userID);
			auth.setPassword(passWord);
 
			stub._getServiceClient().getOptions().setProperty(HTTPConstants.AUTHENTICATE, auth);
			
			param = ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceUtil().generateXmlWithHeader(bizTransactionId, Customer, headInfoMapList,bodyInfoMapList);

			VXG288_SAP_MaterialTransServiceV3 v288_SAP_MaterialTransServiceV30 = new VXG288_SAP_MaterialTransServiceV3();
			v288_SAP_MaterialTransServiceV30.setData(param);
            log.info(param);
			kr.co.aim.messolution.extended.webinterface.webservice.VXG288_SAP_MaterialTransServiceV3_pttBindingQSServiceStub.Response response = stub.vXG288_SAP_MaterialTransServiceV3(v288_SAP_MaterialTransServiceV30);
		
			log.info("Response getMESSAGE() = " + response.getMessage());
			log.info("Response getSIGN() = " + response.getSIGN());
			
			if (numberOfSend <= 3 && "E".equals(response.getSIGN()))
			{
				log.info("SIGN value is 'E'. Resend I/F Message. Number of Send = " + numberOfSend);
				sapVXG288Send(eventInfo, headInfoMapList,bodyInfoMapList, numberOfSend + 1, consumeUnit);
				return;
			}
			
			esbFlag = "Y";
			resultCode = response.getSIGN();
			resultComment = response.getMessage();
		}
		catch (Exception ex)
		{
			log.info("VXG-sapVXG288Send Error : " + ex.getMessage());
			
			esbFlag = "N";
			resultCode = "E";
			if(ex.getMessage()!=null)
			{
				resultComment = ex.getMessage();
			}
			else
			{
				resultComment="UnKnown";
			}
		}
		this.writeInterfaceLogMaterialTransDataPush("VXG-288_SAP_MaterialTransServiceV3", eventInfo, bizTransactionId, param, String.valueOf(numberOfSend), esbFlag, resultCode, resultComment,headInfoMapList, bodyInfoMapList);
		
		if (numberOfSend > 3 && !"N".equals(esbFlag))
		{
			log.info("Retransmitted three times, but failed. Send to alarm mail.");
			this.sendToAlarmMail(resultCode, resultComment);
		}
	}
	
	//可跳转OA详情页面 EM+Wechat  houxk20210817
	public void eMobileSend(EventInfo eventInfo, String[] userGroup, String title, String detailtitle, String message) throws Exception	
	{
			//String url = "http://10.1.96.16:8082/services/SendMessageService";//TST		
			String url = "http://10.1.34.138:8082/services/SendMessageService";//PRD
			
			String localMsgType = "cimmessage";		
			StringBuilder sb = new StringBuilder();  
	        if (userGroup != null && userGroup.length > 0) 
	        {  
	             for (int i = 0; i < userGroup.length; i++) {  
	                 if (i < userGroup.length - 1) {  
	                     sb.append(userGroup[i] + ",");  
	                 } else {  
	                     sb.append(userGroup[i]);  
	                 }  
	             }  
	        }        
	        String localMsgUsers = sb.toString();
			String localMsgTitle = "CIM系统消息通知";
			String localMsgSmallTitle = title;
			String localMsgResume = title;
			String localMsgContent = message;
			String localMsgExpContent = "";
			try
			{
				SendMessageServiceStub stub = new SendMessageServiceStub(url);									
				kr.co.aim.messolution.extended.webinterface.webservice.SendMessageServiceStub.SendCustomMessage_WorkCode sendCustomMessage_WorkCode = new kr.co.aim.messolution.extended.webinterface.webservice.SendMessageServiceStub.SendCustomMessage_WorkCode();
				sendCustomMessage_WorkCode.setMsgType(localMsgType);
				sendCustomMessage_WorkCode.setMsgUsers(localMsgUsers);
				sendCustomMessage_WorkCode.setMsgTitle(localMsgTitle);
				sendCustomMessage_WorkCode.setMsgSmallTitle(localMsgSmallTitle);
				sendCustomMessage_WorkCode.setMsgResume(localMsgResume);
				sendCustomMessage_WorkCode.setMsgContent(localMsgContent);
				sendCustomMessage_WorkCode.setMsgExpContent(localMsgExpContent);
				
				kr.co.aim.messolution.extended.webinterface.webservice.SendMessageServiceStub.SendCustomMessage_WorkCodeResponse response = stub.sendCustomMessage_WorkCode(sendCustomMessage_WorkCode);
				log.info("eMobile Send Success!");	
				
			}
			catch (Exception ex)
			{
				log.info("eMoblie Send Error : " + ex.getCause());	
			}
	}
		
	//Start 20210126 houxk
	public void eMobileSend(EventInfo eventInfo, String[] userGroup, String title, String detailtitle, String message, String emurl) throws Exception
	{
			//String url = "http://10.1.96.39:8082/services/ServiceMessageCustom";//TST
			String url = "http://10.1.34.138:8082/services/ServiceMessageCustom";//PRD
			
			String transactionId = TimeStampUtil.getCurrentEventTimeKey();
			//String uuid="7d823677600f4a04bcbab7dad5eb2a5c";//TST
			String uuid="8547574e-c092-438e-9371-c395p2sR5dZe";//PRD
			String typename="CIM系统消息通知";
			String noticeIcon="";
			boolean noticeFlag = true;
			boolean pcNotickFlag = false;
			boolean notickOutsideFlag = true;
			boolean emNoticeFlag = true;
			boolean userControlFlag = false;
			String noticeGroup="101";
			ArrayOfString userid = new ArrayOfString();
			if (userGroup != null && userGroup.length > 0) 
			{
				for (int i = 0; i < userGroup.length; i++)
				{
					userid.addString(userGroup[i]);
				}
			}
			//userid.addString("V0042748");
			//String title = "Message";
			//String detailtitle = "${}Message";
			//String message = "CIM Message Test";
			String pcLink = emurl; //可以设定emobile链接
			String emLink = "";
			String createUser = "系统管理员";	     	    	    
			
			try
			{
				ServiceMessageCustomStub stub = new ServiceMessageCustomStub(url);									
				SendCustomMessage_WorkCode sendCustomMessage_Login = new SendCustomMessage_WorkCode();
				
				sendCustomMessage_Login.setIn0(uuid);
				sendCustomMessage_Login.setIn1(typename);
				sendCustomMessage_Login.setIn2(noticeIcon);
				sendCustomMessage_Login.setIn3(noticeFlag);
				sendCustomMessage_Login.setIn4(pcNotickFlag);
				sendCustomMessage_Login.setIn5(notickOutsideFlag);
				sendCustomMessage_Login.setIn6(emNoticeFlag);
				sendCustomMessage_Login.setIn7(userControlFlag);
				sendCustomMessage_Login.setIn8(noticeGroup);
				sendCustomMessage_Login.setIn9(userid);
				sendCustomMessage_Login.setIn10(title);
				sendCustomMessage_Login.setIn11(detailtitle);
				sendCustomMessage_Login.setIn12(message);
				sendCustomMessage_Login.setIn13(pcLink);
				sendCustomMessage_Login.setIn14(emLink);
				sendCustomMessage_Login.setIn15(createUser);
				
				kr.co.aim.messolution.extended.webinterface.webservice.ServiceMessageCustomStub.SendCustomMessage_WorkCodeResponse response = stub.sendCustomMessage_WorkCode(sendCustomMessage_Login);
				log.info("eMobile Send Success!");	
				
//				//insert CT_OAMESSAGELOG
//				String[] userList = userid.getString();
//				StringBuilder sb = new StringBuilder();  
//		        if (userList != null && userList.length > 0) 
//		        {  
//		             for (int i = 0; i < userList.length; i++) {  
//		                 if (i < userList.length - 1) {  
//		                     sb.append(userList[i] + ",");  
//		                 } else {  
//		                     sb.append(userList[i]);  
//		                 }  
//		             }  
//		        }  
//		        String user = sb.toString();
//				
//				StringBuffer sql = new StringBuffer();
//				sql.append("INSERT INTO CT_OAMESSAGELOG ");
//				sql.append(" ( TRANSACTIONID, TYPENAME, TITLE,");
//				sql.append("   DETAILTITLE, MESSAGE, USERID, CREATEUSER,");
//				sql.append("   EVENTCOMMENT, EVENTNAME, EVENTTIME, EVENTTIMEKEY, EVENTUSER )");
//				sql.append("VALUES  ");
//				sql.append(" ( :TRANSACTIONID, :TYPENAME, :TITLE,  ");
//				sql.append("   :DETAILTITLE, :MESSAGE, :USERID, :CREATEUSER,");
//				sql.append("   :EVENTCOMMENT, :EVENTNAME, :EVENTTIME,:EVENTTIMEKEY, :EVENTUSER)");
//
//				Map<String, Object> bindMap = new HashMap<String, Object>();
//				bindMap.put("TRANSACTIONID", transactionId);
//				bindMap.put("TYPENAME",typename);
//				bindMap.put("TITLE",title);
//				bindMap.put("DETAILTITLE",detailtitle);
//				bindMap.put("MESSAGE",message); 
//				bindMap.put("USERID",user);
//				bindMap.put("CREATEUSER", createUser);
//				bindMap.put("EVENTCOMMENT", eventInfo.getEventComment());
//				bindMap.put("EVENTNAME", eventInfo.getEventName());
//				bindMap.put("EVENTTIME", eventInfo.getEventTime());
//				bindMap.put("EVENTTIMEKEY", transactionId);
//				bindMap.put("EVENTUSER", eventInfo.getEventUser());
//				
//				try
//				{
//					GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
//				}
//				catch(Exception e)
//		        {
//					log.info("DB Update filed");
//		        }	
			}
			catch (Exception ex)
			{
				log.info("eMoblie Send Error : " + ex.getCause());	
			}
	}
	//End
	
	public void weChatSend(EventInfo eventInfo, String[] userGroup, String title, String detailtitle, String message, String pcurl) throws Exception
	{
			//String url = "http://10.1.96.39:8082/services/ServiceMessageCustom";//TST
			String url = "http://10.1.34.138:8082/services/ServiceMessageCustom";//PRD
			
			String transactionId = TimeStampUtil.getCurrentEventTimeKey();
			//String uuid="7d823677600f4a04bcbab7dad5eb2a5c";//TST
			String uuid="aad3d0b0-e3ff-4f6e-86df-8b7cQdzBv2MZ";//PRD
			String typename="CIM系统消息通知";
			String noticeIcon="";
			boolean noticeFlag = true;
			boolean pcNotickFlag = false;
			boolean notickOutsideFlag = true;
			boolean emNoticeFlag = false;
			boolean userControlFlag = false;
			String noticeGroup="101";
			ArrayOfString userid = new ArrayOfString();
			if (userGroup != null && userGroup.length > 0) 
			{
				for (int i = 0; i < userGroup.length; i++)
				{
					userid.addString(userGroup[i]);
				}
			}
			//userid.addString("V0042748");
			//String title = "Message";
			//String detailtitle = "${}Message";
			//String message = "CIM Message Test";
			String pcLink = ""; 
			String emLink = pcurl;	//可以设定wechat链接
			String createUser = "系统管理员";	     	    	    
			
			try
			{
				ServiceMessageCustomStub stub = new ServiceMessageCustomStub(url);									
				SendCustomMessage_WorkCode sendCustomMessage_Login = new SendCustomMessage_WorkCode();
				
				sendCustomMessage_Login.setIn0(uuid);
				sendCustomMessage_Login.setIn1(typename);
				sendCustomMessage_Login.setIn2(noticeIcon);
				sendCustomMessage_Login.setIn3(noticeFlag);
				sendCustomMessage_Login.setIn4(pcNotickFlag);
				sendCustomMessage_Login.setIn5(notickOutsideFlag);
				sendCustomMessage_Login.setIn6(emNoticeFlag);
				sendCustomMessage_Login.setIn7(userControlFlag);
				sendCustomMessage_Login.setIn8(noticeGroup);
				sendCustomMessage_Login.setIn9(userid);
				sendCustomMessage_Login.setIn10(title);
				sendCustomMessage_Login.setIn11(detailtitle);
				sendCustomMessage_Login.setIn12(message);
				sendCustomMessage_Login.setIn13(pcLink);
				sendCustomMessage_Login.setIn14(emLink);
				sendCustomMessage_Login.setIn15(createUser);
				
				kr.co.aim.messolution.extended.webinterface.webservice.ServiceMessageCustomStub.SendCustomMessage_WorkCodeResponse response = stub.sendCustomMessage_WorkCode(sendCustomMessage_Login);
				log.info("WeChat Send Success!");	
			}
			catch (Exception ex)
			{
				log.info("WeChat Send Error : " + ex.getCause());	
			}
	}
	
	public void oaVXG429Send(EventInfo eventInfo, List<Map<String, String>> eRPReportList, int numberOfSend) throws Exception
	{
		//http://10.1.32.151:8010/WP_HNYG/APP_OA_SERVICE/Proxy_Services/TA_SAP/VXG_429_OA_CreateRequestBasicService_PS 测试
		String url = "http://10.1.32.151:8010/WP_HNYG/APP_OA_SERVICE/Proxy_Services/TA_SAP/VXG_429_OA_CreateRequestBasicService_PS";
		String bizTransactionId = "OA.VXG-429" + eventInfo.getEventTimeKey();
		String Customer = "OA";
		String param = StringUtil.EMPTY;
		String resultCode = StringUtil.EMPTY;
		String resultComment = StringUtil.EMPTY;
		String userID= "USEROA";
		String passWord= "PassOA1234";
		
		try
		{
			VXG_429_OA_CreateRequestBasicService_pttBindingQSServiceStub stub = new VXG_429_OA_CreateRequestBasicService_pttBindingQSServiceStub(url);
			
			HttpTransportProperties.Authenticator auth = new HttpTransportProperties.Authenticator();
			auth.setUsername(userID);
			auth.setPassword(passWord);
 
			stub._getServiceClient().getOptions().setProperty(HTTPConstants.AUTHENTICATE, auth);

			
			param = ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceUtil().generateXmlForOA(bizTransactionId, Customer, eRPReportList);
			VXG_429_OA_CreateRequestBasicService vXG_429_OA_CreateRequestBasicService0 = new VXG_429_OA_CreateRequestBasicService();
			vXG_429_OA_CreateRequestBasicService0.setData(param);

			
			
			kr.co.aim.messolution.extended.webinterface.webservice.VXG_429_OA_CreateRequestBasicService_pttBindingQSServiceStub.Response response = stub.vXG_429_OA_CreateRequestBasicService(vXG_429_OA_CreateRequestBasicService0);
			
			log.info("Response getMESSAGE() = " + response.getMessage());
			log.info("Response getSIGN() = " + response.getSIGN());
			
			/*
			if (numberOfSend <= 3 && "F".equals(response.getSIGN()))
			{
				log.info("SIGN value is 'F'. Resend I/F Message. Number of Send = " + numberOfSend);
				sapVXG416Send(eventInfo, dataInfoMapList, numberOfSend + 1);
			}*/
			
			resultCode = response.getSIGN();
			resultComment = response.getMessage();
		}
		catch (Exception ex)
		{
			log.info("oaTest Error : " + ex.getMessage());
			
			resultCode = "E";
			if(ex.getMessage()!=null)
			{
				resultComment = ex.getMessage();
			}
			else
			{
				resultComment="UnKnown";
			}
		}
		
		/*if (numberOfSend > 3 && !"N".equals(esbFlag))
		{
			log.info("Retransmitted three times, but failed. Send to alarm mail.");
			this.sendToAlarmMail(resultCode, resultComment);
		}*/
	}
}
