package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

public class InterfaceManualReport extends SyncHandler {

	private static Log log = LogFactory.getLog(InterfaceManualReport.class);
	public Object doWorks(Document doc) throws CustomException
	{
		String sapInterfaceCode = SMessageUtil.getBodyItemValue(doc, "SAPINTERFACECODE", true);		
		List<Element> ifDataList = SMessageUtil.getBodySequenceItemList(doc, "IFDATALIST", true);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);

		//**************************************ReceiveLot***********************************************//
		if(StringUtil.isNotEmpty(sapInterfaceCode)&&sapInterfaceCode.contains("VXG372"))
		{					
			for (Element ifData : ifDataList)
			{
				//SAP Send
				//Add SAP Switch
				String seq = SMessageUtil.getChildText(ifData, "SEQ", true);
				String productRequestName = SMessageUtil.getChildText(ifData, "PRODUCTREQUESTNAME", true);
				String materialSpecName = SMessageUtil.getChildText(ifData, "MATERIALSPECNAME", true);
				String processOperationName = SMessageUtil.getChildText(ifData, "PROCESSOPERATIONNAME", true);
				String quantity = SMessageUtil.getChildText(ifData, "QUANTITY", true);
				String consumeUnit = SMessageUtil.getChildText(ifData, "CONSUMEUNIT", true);
				String factoryCode = SMessageUtil.getChildText(ifData, "FACTORYCODE", true);
				String factoryPosition = SMessageUtil.getChildText(ifData, "FACTORYPOSITION", true);
				String batchNo = SMessageUtil.getChildText(ifData, "BATCHNO", false);
				String productQuantity = SMessageUtil.getChildText(ifData, "PRODUCTQUANTITY", false);
				String eventTime = SMessageUtil.getChildText(ifData, "EVENTTIME", true);
				String cancelFlag = SMessageUtil.getChildText(ifData, "CANCELFLAG", false);
				String wsFlag = SMessageUtil.getChildText(ifData, "WSFLAG", false);
			    Map<String, String> ERPInfo = new HashMap<>();
				List<Map<String, String>> ERPReportList = new ArrayList<Map<String, String>>();
				
				if(!StringUtils.equals(seq, "N"))
				{
					ERPInfo.put("SEQ", seq);
				}
				else
				{
					ERPInfo.put("SEQ", TimeStampUtil.getCurrentEventTimeKey());
				}
				ERPInfo.put("PRODUCTREQUESTNAME",productRequestName);
				ERPInfo.put("MATERIALSPECNAME", materialSpecName);
				ERPInfo.put("PROCESSOPERATIONNAME", processOperationName);
				ERPInfo.put("QUANTITY", quantity);
				ERPInfo.put("CONSUMEUNIT", consumeUnit);
				ERPInfo.put("FACTORYCODE",factoryCode);
				ERPInfo.put("FACTORYPOSITION",factoryPosition);
				ERPInfo.put("BATCHNO", batchNo);
				ERPInfo.put("PRODUCTQUANTITY", productQuantity);								
				ERPInfo.put("EVENTTIME", eventTime);
				ERPInfo.put("CANCELFLAG", cancelFlag);
				ERPInfo.put("WSFLAG", wsFlag);
					
				ERPReportList.add(ERPInfo);
					
				eventInfo.setEventName("Receive");
					
				//Send
				try
				{
					ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().sapVXG372Send(eventInfo, ERPReportList, 1,consumeUnit);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			
		}
		//***********************************ShipLot************************************************************//
		else if(StringUtil.isNotEmpty(sapInterfaceCode)&&sapInterfaceCode.contains("VXG371"))
		{					
			for (Element ifData : ifDataList)
			{
				//SAP Send
				//Add SAP Switch
				String seq = SMessageUtil.getChildText(ifData, "SEQ", true);
				String productRequestName = SMessageUtil.getChildText(ifData, "PRODUCTREQUESTNAME", true);
				String materialSpecName = SMessageUtil.getChildText(ifData, "MATERIALSPECNAME", true);
				String productQuantity = SMessageUtil.getChildText(ifData, "PRODUCTQUANTITY", true);
				String productType = SMessageUtil.getChildText(ifData, "PRODUCTTYPE", true);
				String factoryCode = SMessageUtil.getChildText(ifData, "FACTORYCODE", true);
				String factoryPosition = SMessageUtil.getChildText(ifData, "FACTORYPOSITION", true);
				String batchNo = SMessageUtil.getChildText(ifData, "BATCHNO", true);
				String unShipFlag = SMessageUtil.getChildText(ifData, "UNSHIPFLAG", false);
				String eventTime = SMessageUtil.getChildText(ifData, "EVENTTIME", true);
				String ngFlag = SMessageUtil.getChildText(ifData, "NGFLAG", false);
			    Map<String, String> ERPInfo = new HashMap<>();
				List<Map<String, String>> ERPReportList = new ArrayList<Map<String, String>>();
					
				if(!StringUtils.equals(seq, "N"))
				{
					ERPInfo.put("SEQ", seq);
				}
				else
				{
					ERPInfo.put("SEQ", TimeStampUtil.getCurrentEventTimeKey());
				}
				ERPInfo.put("PRODUCTREQUESTNAME", productRequestName);
				ERPInfo.put("PRODUCTSPECNAME", materialSpecName);
				ERPInfo.put("PRODUCTQUANTITY", productQuantity);
				ERPInfo.put("PRODUCTTYPE", productType);
				ERPInfo.put("FACTORYCODE",factoryCode);
				ERPInfo.put("FACTORYPOSITION", factoryPosition);
				ERPInfo.put("BATCHNO", batchNo);
				ERPInfo.put("UNSHIPFLAG", unShipFlag);
				ERPInfo.put("EVENTTIME", eventTime);
				ERPInfo.put("NGFLAG",ngFlag);
				
				ERPReportList.add(ERPInfo);
				
				eventInfo.setEventName("Ship");
				
				//Send
				try
				{
					ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().sapVXG371Send(eventInfo, ERPReportList, 1);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			
		}
		else if(StringUtil.isNotEmpty(sapInterfaceCode)&&sapInterfaceCode.contains("VXG288"))
		{					
			for (Element ifData : ifDataList)
			{
				//SAP Send
				//Add SAP Switch
				String seq = SMessageUtil.getChildText(ifData, "SEQ", true);
				String eventTime = SMessageUtil.getChildText(ifData, "EVENTTIME", true);
				String moveType = SMessageUtil.getChildText(ifData, "MOVETYPE", true);
				String costDepartMent = SMessageUtil.getChildText(ifData, "COSTDEPARTMENT", true);
				String factoryCode = SMessageUtil.getChildText(ifData, "FACTORYCODE", true);
				String factoryPosition = SMessageUtil.getChildText(ifData, "FACTORYPOSITION", true);
				String productQuantity = SMessageUtil.getChildText(ifData, "PRODUCTQUANTITY", true);
				String productRequestName = SMessageUtil.getChildText(ifData, "PRODUCTREQUESTNAME", true);
				String productSpecName = SMessageUtil.getChildText(ifData, "PRODUCTSPECNAME", false);
				String productType = SMessageUtil.getChildText(ifData, "PRODUCTTYPE", true);
			    
				List<Map<String, String>> headERPReportList = new ArrayList<Map<String, String>>();
				List<Map<String, String>> bodyERPReportList = new ArrayList<Map<String, String>>();
				Map<String, String> headInfo = new HashMap<>();
				Map<String, String> bodyInfo = new HashMap<>();
				
				headInfo.put("BUDAT", eventTime);
				headInfo.put("BWART", "201");
				headInfo.put("KOSTL",costDepartMent);
				headInfo.put("WERKS",factoryCode);
				headInfo.put("LGORT_C", factoryPosition);
                headInfo.put("DOCNO", "");
				
				bodyInfo.put("MENGE",  productQuantity);
				bodyInfo.put("CHARG_R", productRequestName);
				bodyInfo.put("MATNR", productSpecName);
				bodyInfo.put("MEINS", productType);
				bodyInfo.put("CHARG_C", "");
				bodyInfo.put("HSDAT_R", "");
				bodyInfo.put("INSMK_C", "");
				bodyInfo.put("INSMK_R", "");
				bodyInfo.put("LGORT_R", "");
				bodyInfo.put("LICHA_R", "");
				bodyInfo.put("VFDAT_R", "");
				
				headERPReportList.add(headInfo);
				bodyERPReportList.add(bodyInfo);
				
				eventInfo.setEventName("NGReceive");
				
				//Send
				try
				{
					ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().sapVXG288Send(eventInfo, headERPReportList,bodyERPReportList, 1, productType);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			
		}
		return doc;
	}
}
