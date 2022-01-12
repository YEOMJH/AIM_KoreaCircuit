package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.midi.SoundbankResource;

import org.apache.commons.collections.map.ListOrderedMap;
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
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupHistory;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupKey;
import kr.co.aim.greentrack.processoperationspec.ProcessOperationSpecServiceProxy;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpecKey;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.IncrementFinishedQuantityByInfo;

public class UpdateWMSInfo extends SyncHandler {

	private static Log log = LogFactory.getLog(CancelTrackIn.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> boxList = SMessageUtil.getBodySequenceItemList(doc, "BOXLIST", true);
		String shipNo = SMessageUtil.getBodyItemValue(doc, "SHIPNO", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UpdateWMSInfo", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		List<Object[]> updateWMSShipList = new ArrayList<Object[]>();
		List<Object[]> deleteArgListIn = new ArrayList<Object[]>();
		
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		String wmsFactoryName = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getWMSFactoryName();
		
		List<Map<String, Object>> inData = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getTRANS_WH_IN(shipNo);
		
		if(inData != null && inData.size() > 0)
		{
			//delete data
			List<Object> deleteBindListIn = new ArrayList<Object>();
			deleteBindListIn.add(shipNo);
			deleteArgListIn.add(deleteBindListIn.toArray());
			
			if (deleteArgListIn.size() > 0)
			{
				MESProcessGroupServiceProxy.getProcessGroupServiceUtil().deleteTRANS_WH_IN(deleteArgListIn);
			}
		}
		
		for (Element boxData : boxList)
		{
			String processGroupName = boxData.getChildText("PROCESSGROUPNAME");
			String panelQty = boxData.getChildText("PANELQTY");
			String seq = boxData.getChildText("ITEM_SEQ");
			
			ProcessGroupKey processGroupKey = new ProcessGroupKey();
			processGroupKey.setProcessGroupName(processGroupName);
			ProcessGroup processGroupData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(processGroupKey);
			
			ProductRequest workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(processGroupData.getUdfs().get("PRODUCTREQUESTNAME").toString());
			String superProductRequestName = workOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME").toString();
			if(StringUtil.isEmpty(superProductRequestName))
				superProductRequestName = workOrderData.getKey().getProductRequestName();
			
			Map<String, String> factoryInfo = ExtendedObjectProxy.getSuperProductRequestService().getFactoryInfo(workOrderData.getProductRequestType(),
					workOrderData.getUdfs().get("SUBPRODUCTIONTYPE"), workOrderData.getUdfs().get("RISKFLAG"),processGroupData.getUdfs().get("LOTDETAILGRADE"));
			String sourFactoryPosition = "";
			String destFactoryPosition = "";
			String factoryCode="";
			String sql = " SELECT DESCRIPTION FROM ENUMDEFVALUE WHERE ENUMNAME=:ENUMNAME AND ENUMVALUE=:ENUMVALUE ";
			String moveType="";
			if(StringUtil.equals(workOrderData.getUdfs().get("SUBPRODUCTIONTYPE"), "SYZLC")&&!StringUtil.equals(processGroupData.getUdfs().get("LOTDETAILGRADE").toString(), "S"))
			{
				moveType="Z25";
			}
			else 
			{
				moveType="Z11";
			}
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("ENUMNAME", "PostCellFactoryPosition");
			bindMap.put("ENUMVALUE", workOrderData.getUdfs().get("SUBPRODUCTIONTYPE"));
			
			List<Map<String, Object>> sqlResult1 = 
					GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			
			if(sqlResult1.size() > 0){
				sourFactoryPosition = sqlResult1.get(0).get("DESCRIPTION").toString();
			}
			else
			{
				sourFactoryPosition="";
			}
			if(processGroupData.getUdfs().get("LOTDETAILGRADE").toString().equals("S")||processGroupData.getUdfs().get("LOTDETAILGRADE").toString().contains("C")||
					((workOrderData.getProductRequestType().equals("E")||workOrderData.getProductRequestType().equals("T"))
							&&!workOrderData.getUdfs().get("SUBPRODUCTIONTYPE").equals("ESLC")
							&&!workOrderData.getUdfs().get("SUBPRODUCTIONTYPE").equals("LCFG")
							&&!workOrderData.getUdfs().get("SUBPRODUCTIONTYPE").equals("SLCFG")
							&&!workOrderData.getUdfs().get("SUBPRODUCTIONTYPE").equals("SYZLC")))
			{
				sourFactoryPosition = constantMap.SAPFactoryPosition_9F99;
				factoryCode = constantMap.SAPFactoryCode_5099;
			}
			else
			{
				factoryCode = constantMap.SAPFactoryCode_5001;
			}
			destFactoryPosition = factoryInfo.get("LOCATION");
			
			//Make new insert data
			List<Object> WMSShip = new ArrayList<Object>();
			WMSShip.add(shipNo);
			WMSShip.add("PKIN");
			WMSShip.add(processGroupData.getUdfs().get("PRODUCTSPECNAME").toString().substring(0, processGroupData.getUdfs().get("PRODUCTSPECNAME").toString().length() - 1));
			WMSShip.add(panelQty);
			WMSShip.add(superProductRequestName);
			WMSShip.add(factoryCode);
			WMSShip.add(sourFactoryPosition);
			WMSShip.add(destFactoryPosition);
			WMSShip.add(processGroupData.getUdfs().get("BATCHNO"));
			WMSShip.add(workOrderData.getUdfs().get("COSTDEPARTMENT"));
			WMSShip.add(workOrderData.getUdfs().get("PROJECTPRODUCTREQUESTNAME"));
			WMSShip.add("N");
			WMSShip.add(eventInfo.getEventUser());
			WMSShip.add(eventInfo.getEventTime());
			WMSShip.add(seq);
			WMSShip.add(moveType);
			
			updateWMSShipList.add(WMSShip.toArray());
		}
		
		if (updateWMSShipList.size() > 0)
		{
			MESProcessGroupServiceProxy.getProcessGroupServiceUtil().insertWMSShip(updateWMSShipList);
		}
		
		return doc;
	}
}
