package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
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

public class CreateShipNo extends SyncHandler {

	private static Log log = LogFactory.getLog(CancelTrackIn.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> boxList = SMessageUtil.getBodySequenceItemList(doc, "BOXLIST", true);
		String lotDetailGrade = SMessageUtil.getBodyItemValue(doc, "LOTDETAILGRADE", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateShipCode", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		if(StringUtil.equals(lotDetailGrade, "S"))
		{
			nameRuleAttrMap.put("SHIPTYPE", "Scrap");
		}
		else
		{
			nameRuleAttrMap.put("SHIPTYPE", "Normal");
		}

		List<String> lotNameList = CommonUtil.generateNameByNamingRule("SHIPNONaming", nameRuleAttrMap, 1);
		String shipNo = lotNameList.get(0);
		
		//Make Batch List
		List<Map<String, Object>> updateWMSShipMapList = new ArrayList<Map<String, Object>>();
		int itemSeq = 1;
		
		for (Element boxData : boxList)
		{
			String boxName = boxData.getChildText("PROCESSGROUPNAME");
			String panelQty = boxData.getChildText("PANELQTY");
			String spac = boxData.getChildText("SPAC");
			String unit = boxData.getChildText("UNIT");
			String comment = boxData.getChildText("COMMENT");
			
			ProcessGroupKey processGroupKey = new ProcessGroupKey();
			processGroupKey.setProcessGroupName(boxName);
			
			ProcessGroup processGroupData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(processGroupKey);
			List<Map<String, Object>> result = null;
			String sqlWorkOrder = "SELECT L.PRODUCTREQUESTNAME, COUNT(L.LOTNAME) AS QUANTITY FROM LOT L,PRODUCTREQUEST PR,PROCESSGROUP P"
					+" WHERE  L.PROCESSGROUPNAME=P.PROCESSGROUPNAME AND L.PRODUCTREQUESTNAME=PR.PRODUCTREQUESTNAME  AND P.SUPERPROCESSGROUPNAME=:SUPERPROCESSGROUPNAME"
					+" GROUP BY  L.PRODUCTREQUESTNAME";
					
			Map<String, Object> bindMapWorkOrder = new HashMap<>();
			bindMapWorkOrder.put("SUPERPROCESSGROUPNAME", boxName);
			try
			{
				result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlWorkOrder, bindMapWorkOrder);
			} 
			catch (Exception ex) 
			{
				log.info(ex.getCause());
			}
			if(result.size()<1)
			{
			
			 throw new CustomException("PROCESSGROUP-0001", boxName);
				
			}
			ProductRequest workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(result.get(0).get("PRODUCTREQUESTNAME").toString());
			String superProductRequestName = workOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME").toString();
			
			if(StringUtil.isEmpty(superProductRequestName))
				superProductRequestName = workOrderData.getKey().getProductRequestName();
			
			Map<String, String> factoryInfo = ExtendedObjectProxy.getSuperProductRequestService().getFactoryInfo(workOrderData.getProductRequestType(),
					workOrderData.getUdfs().get("SUBPRODUCTIONTYPE"), workOrderData.getUdfs().get("RISKFLAG"),lotDetailGrade);
			String sourFactoryPosition = "";
			String destFactoryPosition = "";
			String factoryCode="";
			String sql = " SELECT DESCRIPTION FROM ENUMDEFVALUE WHERE ENUMNAME=:ENUMNAME AND ENUMVALUE=:ENUMVALUE ";
			String moveTpe="";
			if(StringUtil.equals(workOrderData.getUdfs().get("SUBPRODUCTIONTYPE"), "SYZLC")&&!StringUtil.equals(lotDetailGrade, "S"))
			{
				moveTpe="Z25";
			}
			else 
			{
				moveTpe="Z11";
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
			if(lotDetailGrade.equals("S")||lotDetailGrade.contains("C")||
					((workOrderData.getProductRequestType().equals("E")||workOrderData.getProductRequestType().equals("T"))
							&&!workOrderData.getUdfs().get("SUBPRODUCTIONTYPE").equals("ESLC")
							&&!workOrderData.getUdfs().get("SUBPRODUCTIONTYPE").equals("LCFG")
							&&!workOrderData.getUdfs().get("SUBPRODUCTIONTYPE").equals("SLCFG")
							&&!workOrderData.getUdfs().get("SUBPRODUCTIONTYPE").equals("SYZLC")))		
			{
				sourFactoryPosition=constantMap.SAPFactoryPosition_9F99;
				factoryCode=constantMap.SAPFactoryCode_5099;
			}
			else
			{
				factoryCode=constantMap.SAPFactoryCode_5001;
			}
			destFactoryPosition=factoryInfo.get("LOCATION");
			
			
			if(!StringUtil.equals(processGroupData.getUdfs().get("SHIPFLAG"), "Y"))
			{
				throw new CustomException("PROCESSGROUP-0100", boxName);
			}
			if(StringUtil.isNotEmpty(processGroupData.getUdfs().get("SHIPNO")))
			{
				throw new CustomException("PROCESSGROUP-0101", boxName);
			}
			if(StringUtil.isNotEmpty(processGroupData.getSuperProcessGroupName()))
			{
				throw new CustomException("PROCESSGROUP-0102", boxName);
			}
			if(StringUtil.isEmpty(processGroupData.getUdfs().get("BATCHNO")))
			{
				throw new CustomException("PROCESSGROUP-0016", boxName);
			}
			
			List<Object> WMSShip = new ArrayList<Object>();
			
			Map<String, Object> resultData = new HashMap<String, Object>();
			
			if(StringUtil.equals(processGroupData.getProcessGroupType(), "Pallet"))
			{
				resultData = insertWMSPallet(processGroupData, panelQty, spac, unit, comment, shipNo, eventInfo, itemSeq, superProductRequestName, sourFactoryPosition,destFactoryPosition,factoryCode, workOrderData, moveTpe);	
			}
			else if(StringUtil.equals(processGroupData.getProcessGroupType(), "OuterPacking"))
			{
				resultData = insertWMSOuter(processGroupData, panelQty, spac, unit, comment, shipNo, eventInfo, itemSeq, superProductRequestName, sourFactoryPosition,destFactoryPosition,factoryCode, workOrderData, moveTpe);
			}
			else if(StringUtil.equals(processGroupData.getProcessGroupType(), "InnerPacking"))
			{
				resultData = insertWMSInner(processGroupData, panelQty, spac, unit, comment, shipNo, eventInfo, itemSeq, superProductRequestName, sourFactoryPosition,destFactoryPosition,factoryCode, workOrderData, moveTpe);
			}
			else
			{
				resultData = insertWMSScrap(processGroupData, panelQty, spac, unit, comment, shipNo, eventInfo, itemSeq, superProductRequestName, sourFactoryPosition,"2F91",factoryCode, workOrderData,moveTpe);
			}
			
			if(updateWMSShipMapList == null || updateWMSShipMapList.size() == 0)
			{
				updateWMSShipMapList.add(resultData);
				itemSeq++;
			}
			else
			{
				boolean isFound = false;
				for(int i = 0 ; i < updateWMSShipMapList.size() ; i++)
				{
					if(StringUtil.equals(updateWMSShipMapList.get(i).get("BATCH_NO").toString(), resultData.get("BATCH_NO").toString()))
					{
						isFound = true;
						
						Map<String, Object> listMap = updateWMSShipMapList.get(i);
						int listCount = Integer.parseInt(updateWMSShipMapList.get(i).get("PRD_QTY").toString());
						int resultCount = Integer.parseInt(resultData.get("PRD_QTY").toString());
						listMap.put("PRD_QTY", listCount + resultCount);
						updateWMSShipMapList.set(i, listMap);
						
						break;
					}
				}
				
				if(!isFound)
				{
					updateWMSShipMapList.add(resultData);
					itemSeq++;
				}
			}
		}
		
		//Convert Map List
		List<Object[]> updateWMSShipList = new ArrayList<Object[]>();
		for(Map<String, Object> WMSMap : updateWMSShipMapList)
		{
			List<Object> WMSShip = new ArrayList<Object>();
			WMSShip.add(WMSMap.get("SHIP_NO"));
			WMSShip.add(WMSMap.get("SHIP_STAT"));
			WMSShip.add(WMSMap.get("MDL_ID"));
			WMSShip.add(WMSMap.get("PRD_QTY"));
			WMSShip.add(WMSMap.get("WO_ID"));
			WMSShip.add(WMSMap.get("FAB_ID"));
			WMSShip.add(WMSMap.get("SOUR_SHOP"));
			WMSShip.add(WMSMap.get("DEST_SHOP"));
			WMSShip.add(WMSMap.get("BATCH_NO"));
			WMSShip.add(WMSMap.get("COST_CENTER"));
			WMSShip.add(WMSMap.get("INTERNAL_ORDER"));
			WMSShip.add(WMSMap.get("DATA_STATUS"));
			WMSShip.add(WMSMap.get("EVT_USR"));
			WMSShip.add(WMSMap.get("EVT_TIMESTAMP"));
			WMSShip.add(WMSMap.get("ITEM_SEQ"));
			WMSShip.add(WMSMap.get("MOVE_TYPE"));
			
			updateWMSShipList.add(WMSShip.toArray());			
		}
		
		if (updateWMSShipList.size() > 0)
		{
			MESProcessGroupServiceProxy.getProcessGroupServiceUtil().insertWMSShip(updateWMSShipList);
		}
		
		/*
		for (Element boxData : boxList)
		{
			String boxName = boxData.getChildText("PROCESSGROUPNAME");
			String panelQty = boxData.getChildText("PANELQTY");
			String spac = boxData.getChildText("SPAC");
			String unit = boxData.getChildText("UNIT");
			String comment = boxData.getChildText("COMMENT");
			
			ProcessGroupKey processGroupKey = new ProcessGroupKey();
			processGroupKey.setProcessGroupName(boxName);
			
			ProcessGroup processGroupData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(processGroupKey);
			
			ProductRequest workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(processGroupData.getUdfs().get("PRODUCTREQUESTNAME").toString());
			
			String superProductRequestName = workOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME").toString();
			
			if(StringUtil.isEmpty(superProductRequestName))
				superProductRequestName = workOrderData.getKey().getProductRequestName();
			
			Map<String, String> factoryInfo = ExtendedObjectProxy.getSuperProductRequestService().getFactoryInfo(workOrderData.getProductRequestType(),
					workOrderData.getUdfs().get("SUBPRODUCTIONTYPE"), workOrderData.getUdfs().get("RISKFLAG"));
			String sourFactoryPosition = "";
			String destFactoryPosition = "";
			String factoryCode="";
			String sql = " SELECT DESCRIPTION FROM ENUMDEFVALUE WHERE ENUMNAME=:ENUMNAME AND ENUMVALUE=:ENUMVALUE ";
			String moveTpe="";
			if(StringUtil.equals(workOrderData.getUdfs().get("SUBPRODUCTIONTYPE"), "SYZLC")&&!StringUtil.equals(lotDetailGrade, "S"))
			{
				moveTpe="Z25";
			}
			else 
			{
				moveTpe="Z11";
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
			if(lotDetailGrade.equals("S")||
					(workOrderData.getProductRequestType().equals("E")&&!workOrderData.getUdfs().get("SUBPRODUCTIONTYPE").equals("ESLC")))
			{
				sourFactoryPosition=constantMap.SAPFactoryPosition_9F99;
				factoryCode=constantMap.SAPFactoryCode_5099;
			}
			else
			{
				factoryCode=constantMap.SAPFactoryCode_5001;
			}
			destFactoryPosition=factoryInfo.get("LOCATION");
			
			
			if(!StringUtil.equals(processGroupData.getUdfs().get("SHIPFLAG"), "Y"))
			{
				throw new CustomException("PROCESSGROUP-0100", boxName);
			}
			if(StringUtil.isNotEmpty(processGroupData.getUdfs().get("SHIPNO")))
			{
				throw new CustomException("PROCESSGROUP-0101", boxName);
			}
			if(StringUtil.isNotEmpty(processGroupData.getSuperProcessGroupName()))
			{
				throw new CustomException("PROCESSGROUP-0102", boxName);
			}
			
			if(StringUtil.equals(processGroupData.getProcessGroupType(), "Pallet"))
			{
				insertWMSPallet(processGroupData, panelQty, spac, unit, comment, shipNo, eventInfo, itemSeq, superProductRequestName, sourFactoryPosition,destFactoryPosition,factoryCode, workOrderData, moveTpe);
			}
			else if(StringUtil.equals(processGroupData.getProcessGroupType(), "OuterPacking"))
			{
				insertWMSOuter(processGroupData, panelQty, spac, unit, comment, shipNo, eventInfo, itemSeq, superProductRequestName, sourFactoryPosition,destFactoryPosition,factoryCode, workOrderData, moveTpe);
			}
			else if(StringUtil.equals(processGroupData.getProcessGroupType(), "InnerPacking"))
			{
				insertWMSInner(processGroupData, panelQty, spac, unit, comment, shipNo, eventInfo, itemSeq, superProductRequestName, sourFactoryPosition,destFactoryPosition,factoryCode, workOrderData, moveTpe);
			}
			else
			{
				insertWMSScrap(processGroupData, panelQty, spac, unit, comment, shipNo, eventInfo, itemSeq, superProductRequestName, sourFactoryPosition,"9F91",factoryCode, workOrderData,moveTpe);
			}
			itemSeq++;
		}
		*/
		
		
		SMessageUtil.addItemToBody(doc, "SHIPNO", shipNo);
		
		return doc;
	}

	private List<Map<String, Object>> getPackingList(String palletName, String processGroupType) throws CustomException
	{
		Map<String, String> bindMap = new HashMap<String, String>();

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT PROCESSGROUPNAME ");
		sql.append("  FROM PROCESSGROUP ");
		sql.append(" WHERE SUPERPROCESSGROUPNAME = :SUPERPROCESSGROUPNAME ");
		sql.append("   AND PROCESSGROUPTYPE = :PROCESSGROUPTYPE ");

		bindMap.put("SUPERPROCESSGROUPNAME", palletName);
		bindMap.put("PROCESSGROUPTYPE", processGroupType);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		if (sqlResult.size() < 1)
		{
			throw new CustomException("PROCESSGROUP-0005", palletName);
		}

		return sqlResult;
	}
	
	private void setShipNo(EventInfo eventInfo, ProcessGroup processGroup, String shipNo)
	{
		ProcessGroup oldProcessGroup = (ProcessGroup)ObjectUtil.copyTo(processGroup);

		processGroup.setLastEventName(eventInfo.getEventName());
		processGroup.setLastEventTimeKey(eventInfo.getEventTimeKey());
		processGroup.setLastEventTime(eventInfo.getEventTime());
		processGroup.setLastEventUser(eventInfo.getEventUser());
		processGroup.setLastEventComment(eventInfo.getEventComment());
		processGroup.setLastEventFlag("N");

		Map<String, String> packUdfs = new HashMap<>();
		packUdfs = processGroup.getUdfs();
		packUdfs.put("SHIPNO", shipNo);
		processGroup.setUdfs(packUdfs);

		ProcessGroupHistory processGroupHistory = new ProcessGroupHistory();
		processGroupHistory = ProcessGroupServiceProxy.getProcessGroupHistoryDataAdaptor().setHV(oldProcessGroup, processGroup, processGroupHistory);

		ProcessGroupServiceProxy.getProcessGroupService().update(processGroup);
		ProcessGroupServiceProxy.getProcessGroupHistoryService().insert(processGroupHistory);
	}
	
	private Map<String, Object> insertWMSPallet(ProcessGroup palletData, String panelQty, String spac, String unit, String comment, String shipNo, EventInfo eventInfo, int itemSeq, String superProductRequestName, String sourFactoryName, String destFactoryName,String factoryCode, ProductRequest workOrderData,String moveType) throws CustomException
	{
		Map<String, Object> WMSShip = new HashMap<String, Object>();
		int palletPanelQty = Integer.parseInt(panelQty);
		eventInfo.setEventComment(comment);
		List<Map<String, Object>> outerPackingList = getPackingList(palletData.getKey().getProcessGroupName(), "OuterPacking");
		
		List<Object[]> updateWMSBoxList = new ArrayList<Object[]>();
		
		for (Map<String, Object> outerList : outerPackingList)
		{
			int outerBoxPanelQty = 0;
			
			String outerName = outerList.get("PROCESSGROUPNAME").toString();
			List<Map<String, Object>> innerPackingList = getPackingList(outerName, "InnerPacking");

			for (Map<String, Object> innerList : innerPackingList)
			{
				String innerName = innerList.get("PROCESSGROUPNAME").toString();

				ProcessGroupKey groupKey = new ProcessGroupKey();
				groupKey.setProcessGroupName(innerName);

				ProcessGroup innerData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(groupKey);

				int materialQuantity = (int) innerData.getMaterialQuantity();
				
				outerBoxPanelQty += materialQuantity;
				
				setShipNo(eventInfo, innerData, shipNo);
			}

			//WMS Box
			ProcessGroupKey groupKey = new ProcessGroupKey();
			groupKey.setProcessGroupName(outerName);

			ProcessGroup outerData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(groupKey);
			
			List<Object> WMSBox = new ArrayList<Object>();
			WMSBox.add(outerName);
			WMSBox.add("SHIP");
			WMSBox.add(outerData.getUdfs().get("LOTDETAILGRADE").toString());
			WMSBox.add(outerBoxPanelQty);
			WMSBox.add(outerData.getUdfs().get("PRODUCTSPECNAME").toString().substring(0, outerData.getUdfs().get("PRODUCTSPECNAME").toString().length() - 1));
			WMSBox.add(superProductRequestName);
			WMSBox.add(sourFactoryName);
			WMSBox.add(destFactoryName);
			WMSBox.add(palletData.getKey().getProcessGroupName());
			WMSBox.add(outerData.getUdfs().get("BATCHNO"));
			WMSBox.add("");
			WMSBox.add(outerData.getLastEventComment());
			WMSBox.add(shipNo);
			WMSBox.add("N");
			WMSBox.add(eventInfo.getEventUser());
			WMSBox.add(eventInfo.getEventTime());
			WMSBox.add(workOrderData.getProductRequestType());
			WMSBox.add(workOrderData.getUdfs().get("SUBPRODUCTIONTYPE").toString());
			WMSBox.add(eventInfo.getEventComment());
			// jinlj add Hold to WMS
			if(workOrderData.getUdfs().get("RISKFLAG").equals("Y"))
			{
				WMSBox.add("Y");
			}
			else
			{
				WMSBox.add("");
			}
			//
			updateWMSBoxList.add(WMSBox.toArray());			
			setShipNo(eventInfo, outerData, shipNo);
		}
		
		//WMS Ship
		WMSShip.put("SHIP_NO", shipNo);
		WMSShip.put("SHIP_STAT", "PKIN");
		WMSShip.put("MDL_ID", palletData.getUdfs().get("PRODUCTSPECNAME").toString().substring(0, palletData.getUdfs().get("PRODUCTSPECNAME").toString().length() - 1));
		WMSShip.put("PRD_QTY", palletPanelQty);
		WMSShip.put("WO_ID", superProductRequestName);
		WMSShip.put("FAB_ID", factoryCode);
		WMSShip.put("SOUR_SHOP", sourFactoryName);
		WMSShip.put("DEST_SHOP", destFactoryName);
		WMSShip.put("BATCH_NO", palletData.getUdfs().get("BATCHNO"));
		WMSShip.put("COST_CENTER", workOrderData.getUdfs().get("COSTDEPARTMENT"));
		WMSShip.put("INTERNAL_ORDER", workOrderData.getUdfs().get("PROJECTPRODUCTREQUESTNAME"));
		WMSShip.put("DATA_STATUS", "N");
		WMSShip.put("EVT_USR", eventInfo.getEventUser());
		WMSShip.put("EVT_TIMESTAMP", eventInfo.getEventTime());
		WMSShip.put("ITEM_SEQ", "0000" + String.valueOf(itemSeq));
		WMSShip.put("MOVE_TYPE", moveType);
		
		setShipNo(eventInfo, palletData, shipNo);
		
		if (updateWMSBoxList.size() > 0)
		{
			MESProcessGroupServiceProxy.getProcessGroupServiceUtil().insertWMSBox(updateWMSBoxList);;
		}
		
		return WMSShip;
	}
	
	private Map<String, Object> insertWMSOuter(ProcessGroup outerBoxData, String panelQty, String spac, String unit, String comment, String shipNo, EventInfo eventInfo, int itemSeq, String superProductRequestName,String sourFactoryName, String destFactoryName,String factoryCode, ProductRequest workOrderData,String moveType) throws CustomException
	{
		Map<String, Object> WMSShip = new HashMap<String, Object>();
		int outerPanelQty = Integer.parseInt(panelQty);
		eventInfo.setEventComment(comment);
		List<Map<String, Object>> innerPackingList = getPackingList(outerBoxData.getKey().getProcessGroupName(), "InnerPacking");
		
		List<Object[]> updateWMSBoxList = new ArrayList<Object[]>();
		
		for (Map<String, Object> innerList : innerPackingList)
		{
			String innerName = innerList.get("PROCESSGROUPNAME").toString();

			ProcessGroupKey groupKey = new ProcessGroupKey();
			groupKey.setProcessGroupName(innerName);

			ProcessGroup innerData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(groupKey);
			
			setShipNo(eventInfo, innerData, shipNo);
		}
		
		//WMS Box
		List<Object> WMSBox = new ArrayList<Object>();
		WMSBox.add(outerBoxData.getKey().getProcessGroupName());
		WMSBox.add("SHIP");
		WMSBox.add(outerBoxData.getUdfs().get("LOTDETAILGRADE").toString());
		WMSBox.add(outerPanelQty);
		WMSBox.add(outerBoxData.getUdfs().get("PRODUCTSPECNAME").toString().substring(0, outerBoxData.getUdfs().get("PRODUCTSPECNAME").toString().length() - 1));
		WMSBox.add(superProductRequestName);
		WMSBox.add(sourFactoryName);
		WMSBox.add(destFactoryName);
		WMSBox.add("");
		WMSBox.add(outerBoxData.getUdfs().get("BATCHNO"));
		WMSBox.add("");
		WMSBox.add(outerBoxData.getLastEventComment());
		WMSBox.add(shipNo);
		WMSBox.add("N");
		WMSBox.add(eventInfo.getEventUser());
		WMSBox.add(eventInfo.getEventTime());
		WMSBox.add(workOrderData.getProductRequestType());
		WMSBox.add(workOrderData.getUdfs().get("SUBPRODUCTIONTYPE").toString());
		WMSBox.add(eventInfo.getEventComment());
		// jinlj add Hold to WMS
		if(workOrderData.getUdfs().get("RISKFLAG").equals("Y"))
		{
			WMSBox.add("Y");
		}
		else
		{
			WMSBox.add("");
		}
				//
		updateWMSBoxList.add(WMSBox.toArray());
		
		//WMS Ship
		WMSShip.put("SHIP_NO", shipNo);
		WMSShip.put("SHIP_STAT", "PKIN");
		WMSShip.put("MDL_ID", outerBoxData.getUdfs().get("PRODUCTSPECNAME").toString().substring(0, outerBoxData.getUdfs().get("PRODUCTSPECNAME").toString().length() - 1));
		WMSShip.put("PRD_QTY", outerPanelQty);
		WMSShip.put("WO_ID", superProductRequestName);
		WMSShip.put("FAB_ID", factoryCode);
		WMSShip.put("SOUR_SHOP", sourFactoryName);
		WMSShip.put("DEST_SHOP", destFactoryName);
		WMSShip.put("BATCH_NO", outerBoxData.getUdfs().get("BATCHNO"));
		WMSShip.put("COST_CENTER", workOrderData.getUdfs().get("COSTDEPARTMENT"));
		WMSShip.put("INTERNAL_ORDER", workOrderData.getUdfs().get("PROJECTPRODUCTREQUESTNAME"));
		WMSShip.put("DATA_STATUS", "N");
		WMSShip.put("EVT_USR", eventInfo.getEventUser());
		WMSShip.put("EVT_TIMESTAMP", eventInfo.getEventTime());
		WMSShip.put("ITEM_SEQ", "0000" + String.valueOf(itemSeq));
		WMSShip.put("MOVE_TYPE", moveType);		
		
		setShipNo(eventInfo, outerBoxData, shipNo);
		
		if (updateWMSBoxList.size() > 0)
		{
			MESProcessGroupServiceProxy.getProcessGroupServiceUtil().insertWMSBox(updateWMSBoxList);;
		}
		
		return WMSShip;
	}
	
	private Map<String, Object> insertWMSInner(ProcessGroup innerBoxData, String panelQty, String spac, String unit, String comment, String shipNo, EventInfo eventInfo, int itemSeq, String superProductRequestName,String sourFactoryName, String destFactoryName,String factoryCode, ProductRequest workOrderData,String moveType) throws CustomException
	{
		Map<String, Object> WMSShip = new HashMap<String, Object>();
		int innerPanelQty = Integer.parseInt(panelQty);
		eventInfo.setEventComment(comment);
		
		List<Object[]> updateWMSBoxList = new ArrayList<Object[]>();
		
		//WMS Box
		List<Object> WMSBox = new ArrayList<Object>();
		WMSBox.add(innerBoxData.getKey().getProcessGroupName());
		WMSBox.add("SHIP");
		WMSBox.add(innerBoxData.getUdfs().get("LOTDETAILGRADE").toString());
		WMSBox.add(innerPanelQty);
		WMSBox.add(innerBoxData.getUdfs().get("PRODUCTSPECNAME").toString().substring(0, innerBoxData.getUdfs().get("PRODUCTSPECNAME").toString().length() - 1));
		WMSBox.add(superProductRequestName);
		WMSBox.add(sourFactoryName);
		WMSBox.add(destFactoryName);
		WMSBox.add("");
		WMSBox.add(innerBoxData.getUdfs().get("BATCHNO"));
		WMSBox.add("");
		WMSBox.add(innerBoxData.getLastEventComment());
		WMSBox.add(shipNo);
		WMSBox.add("N");
		WMSBox.add(eventInfo.getEventUser());
		WMSBox.add(eventInfo.getEventTime());
		WMSBox.add(workOrderData.getProductRequestType());
		WMSBox.add(workOrderData.getUdfs().get("SUBPRODUCTIONTYPE").toString());
		WMSBox.add(eventInfo.getEventComment());
		// jinlj add Hold to WMS
		if(workOrderData.getUdfs().get("RISKFLAG").equals("Y"))
		{
			WMSBox.add("Y");
		}
		else
		{
			WMSBox.add("");
		}
		//
		updateWMSBoxList.add(WMSBox.toArray());
		
		//WMS Ship
		WMSShip.put("SHIP_NO", shipNo);
		WMSShip.put("SHIP_STAT", "PKIN");
		WMSShip.put("MDL_ID", innerBoxData.getUdfs().get("PRODUCTSPECNAME").toString().substring(0, innerBoxData.getUdfs().get("PRODUCTSPECNAME").toString().length() - 1));
		WMSShip.put("PRD_QTY", innerPanelQty);
		WMSShip.put("WO_ID", superProductRequestName);
		WMSShip.put("FAB_ID", factoryCode);
		WMSShip.put("SOUR_SHOP", sourFactoryName);
		WMSShip.put("DEST_SHOP", destFactoryName);
		WMSShip.put("BATCH_NO", innerBoxData.getUdfs().get("BATCHNO"));
		WMSShip.put("COST_CENTER", workOrderData.getUdfs().get("COSTDEPARTMENT"));
		WMSShip.put("INTERNAL_ORDER", workOrderData.getUdfs().get("PROJECTPRODUCTREQUESTNAME"));
		WMSShip.put("DATA_STATUS", "N");
		WMSShip.put("EVT_USR", eventInfo.getEventUser());
		WMSShip.put("EVT_TIMESTAMP", eventInfo.getEventTime());
		WMSShip.put("ITEM_SEQ", "0000" + String.valueOf(itemSeq));
		WMSShip.put("MOVE_TYPE", moveType);		
		
		setShipNo(eventInfo, innerBoxData, shipNo);

		if (updateWMSBoxList.size() > 0)
		{
			MESProcessGroupServiceProxy.getProcessGroupServiceUtil().insertWMSBox(updateWMSBoxList);;
		}
		
		return WMSShip;
	}
	
	private Map<String, Object> insertWMSScrap(ProcessGroup scrapOuterPackingData, String panelQty, String spac, String unit, String comment, String shipNo, EventInfo eventInfo, int itemSeq, String superProductRequestName, String sourFactoryName, String destFactoryName,String factoryCode, ProductRequest workOrderData,String moveType) throws CustomException
	{
		Map<String, Object> WMSShip = new HashMap<String, Object>();
		int palletPanelQty = Integer.parseInt(panelQty);
		eventInfo.setEventComment(comment);
		List<Map<String, Object>> scrapPackingList = getPackingList(scrapOuterPackingData.getKey().getProcessGroupName(), "ScrapPacking");
		
		List<Object[]> updateWMSBoxList = new ArrayList<Object[]>();
		
		for (Map<String, Object> scrapPacking : scrapPackingList)
		{
			String scrapPackName = scrapPacking.get("PROCESSGROUPNAME").toString();
			
			ProcessGroupKey groupKey = new ProcessGroupKey();
			groupKey.setProcessGroupName(scrapPackName);

			ProcessGroup scrapPackingData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(groupKey);
			
			setShipNo(eventInfo, scrapPackingData, shipNo);
		}
		//WMS Box
		List<Object> WMSBox = new ArrayList<Object>();
		WMSBox.add(scrapOuterPackingData.getKey().getProcessGroupName());
		WMSBox.add("COMP");
		WMSBox.add(scrapOuterPackingData.getUdfs().get("LOTDETAILGRADE").toString());
		WMSBox.add(palletPanelQty);
		WMSBox.add(scrapOuterPackingData.getUdfs().get("PRODUCTSPECNAME").toString().substring(0, scrapOuterPackingData.getUdfs().get("PRODUCTSPECNAME").toString().length() - 1));
		WMSBox.add(superProductRequestName);
		WMSBox.add(sourFactoryName);
		WMSBox.add(destFactoryName);
		WMSBox.add("");
		WMSBox.add(scrapOuterPackingData.getUdfs().get("BATCHNO"));
		WMSBox.add("");
		WMSBox.add(scrapOuterPackingData.getLastEventComment());
		WMSBox.add(shipNo);
		WMSBox.add("N");
		WMSBox.add(eventInfo.getEventUser());
		WMSBox.add(eventInfo.getEventTime());
		WMSBox.add(workOrderData.getProductRequestType());
		WMSBox.add(workOrderData.getUdfs().get("SUBPRODUCTIONTYPE").toString());
		WMSBox.add(eventInfo.getEventComment());
		// jinlj add Hold to WMS
		if(workOrderData.getUdfs().get("RISKFLAG").equals("Y"))
		{
			WMSBox.add("Y");
		}
		else
		{
			WMSBox.add("");
		}
		//
		updateWMSBoxList.add(WMSBox.toArray());
		
		//WMS Ship
		WMSShip.put("SHIP_NO", shipNo);
		WMSShip.put("SHIP_STAT", "PKIN");
		WMSShip.put("MDL_ID", scrapOuterPackingData.getUdfs().get("PRODUCTSPECNAME").toString().substring(0, scrapOuterPackingData.getUdfs().get("PRODUCTSPECNAME").toString().length() - 1));
		WMSShip.put("PRD_QTY", palletPanelQty);
		WMSShip.put("WO_ID", superProductRequestName);
		WMSShip.put("FAB_ID", factoryCode);
		WMSShip.put("SOUR_SHOP", sourFactoryName);
		WMSShip.put("DEST_SHOP", destFactoryName);
		WMSShip.put("BATCH_NO", scrapOuterPackingData.getUdfs().get("BATCHNO"));
		WMSShip.put("COST_CENTER", workOrderData.getUdfs().get("COSTDEPARTMENT"));
		WMSShip.put("INTERNAL_ORDER", workOrderData.getUdfs().get("PROJECTPRODUCTREQUESTNAME"));
		WMSShip.put("DATA_STATUS", "N");
		WMSShip.put("EVT_USR", eventInfo.getEventUser());
		WMSShip.put("EVT_TIMESTAMP", eventInfo.getEventTime());
		WMSShip.put("ITEM_SEQ", "0000" + String.valueOf(itemSeq));
		WMSShip.put("MOVE_TYPE", moveType);			
		
		setShipNo(eventInfo, scrapOuterPackingData, shipNo);
		
		if (updateWMSBoxList.size() > 0)
		{
			MESProcessGroupServiceProxy.getProcessGroupServiceUtil().insertWMSBox(updateWMSBoxList);;
		}
		
		return WMSShip;
	}
}
