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

public class PalletShip extends SyncHandler {

	private static Log log = LogFactory.getLog(CancelTrackIn.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> palletList = SMessageUtil.getBodySequenceItemList(doc, "PALLETLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("PalletShip", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();
		
		List<Object[]> updateWMSPanelList = new ArrayList<Object[]>();
		
		String wmsFactoryName = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getWMSFactoryName();

		List<Map<String, String>> ERPReportListAll = new ArrayList<Map<String, String>>();
		
		for (Element pallet : palletList)
		{
			EventInfo ERPEventInfo = EventInfoUtil.makeEventInfo("Ship", this.getEventUser(), this.getEventComment(), "", "");
			ERPEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			
			int palletPanelQty = 0;
			
			Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
			nameRuleAttrMap.put("SHIPTYPE", "Normal");

			String palletName = pallet.getChildText("PROCESSGROUPNAME");

			ProcessGroupKey palletKey = new ProcessGroupKey();
			palletKey.setProcessGroupName(palletName);

			ProcessGroup palletData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(palletKey);
			
			ProductRequest workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(palletData.getUdfs().get("PRODUCTREQUESTNAME").toString());
			
			String superProductRequestName = workOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME").toString();
			
			if(StringUtil.isEmpty(superProductRequestName))
				superProductRequestName = workOrderData.getKey().getProductRequestName();
			
			String batchNo = superProductRequestName.substring(0, 8) + palletData.getUdfs().get("LOTDETAILGRADE");
			if(batchNo.length() == 9)
				batchNo += "0";
			
			List<Map<String, Object>> outerPackingList = getPackingList(palletName, "OuterPacking");

			for (Map<String, Object> outerList : outerPackingList)
			{
				String outerName = outerList.get("PROCESSGROUPNAME").toString();
				List<Map<String, Object>> innerPackingList = getPackingList(outerName, "InnerPacking");

				for (Map<String, Object> innerList : innerPackingList)
				{
					String innerName = innerList.get("PROCESSGROUPNAME").toString();
					List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotListByProcessGroup(innerName);

					if (lotList.size() < 1)
					{
						throw new CustomException("PROCESSGROUP-0004", innerName);
					}

					for (Lot lot : lotList)
					{
						CommonValidation.checkLotShippedState(lot);
						CommonValidation.checkLotState(lot);
						Lot oldLot = (Lot)ObjectUtil.copyTo(lot);
						
						String processOperationName = lot.getProcessOperationName();
						ProcessOperationSpec processOperationSpec = ProcessOperationSpecServiceProxy.getProcessOperationSpecService().selectByKey(
								new ProcessOperationSpecKey("POSTCELL", processOperationName, "00001"));

						if (!StringUtil.equals(processOperationSpec.getDetailProcessOperationType(), "SHIP"))
						{
							throw new CustomException("PANEL-0007", lot.getKey().getLotName());
						}

						List<Object> lotBindList = new ArrayList<Object>();

						lotBindList.add(constantMap.Lot_Shipped);
						lotBindList.add(constantMap.Lot_Wait);
						lotBindList.add(eventInfo.getEventName());
						lotBindList.add(eventInfo.getEventTimeKey());
						lotBindList.add(eventInfo.getEventTime());
						lotBindList.add(eventInfo.getEventUser());
						lotBindList.add(eventInfo.getEventComment());
						lotBindList.add(constantMap.Flag_N);
						lotBindList.add(eventInfo.getEventTime());
						lotBindList.add(eventInfo.getEventUser());
						lotBindList.add(eventInfo.getEventTime());
						lotBindList.add(eventInfo.getEventUser());
						lotBindList.add(lot.getKey().getLotName());

						updateLotArgList.add(lotBindList.toArray());

						// History
						lot.setLotState(constantMap.Lot_Shipped);
						lot.setLotProcessState(constantMap.Lot_Wait);
						lot.setLastLoggedInTime(eventInfo.getEventTime());
						lot.setLastLoggedInUser(eventInfo.getEventUser());
						lot.setLastLoggedOutTime(eventInfo.getEventTime());
						lot.setLastLoggedOutUser(eventInfo.getEventUser());
						lot.setLastEventName(eventInfo.getEventName());
						lot.setLastEventTime(eventInfo.getEventTime());
						lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
						lot.setLastEventComment(eventInfo.getEventComment());
						lot.setLastEventUser(eventInfo.getEventUser());
						
						LotHistory lotHistory = new LotHistory();
						lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
						
						updateLotHistoryList.add(lotHistory);
						
						//WMS Panel
						String lotDetailGrade = lot.getUdfs().get("LOTDETAILGRADE").toString();
						
						List<Object> WMSPanel = new ArrayList<Object>();
						WMSPanel.add(lot.getKey().getLotName());
						WMSPanel.add(outerName);
						WMSPanel.add("");
						WMSPanel.add(lot.getProductSpecName().substring(0, lot.getProductSpecName().length() - 1));
						WMSPanel.add(1);
						WMSPanel.add(lotDetailGrade);
						WMSPanel.add(superProductRequestName);
						WMSPanel.add("SOURCESHOP");
						WMSPanel.add(wmsFactoryName);
						WMSPanel.add(palletName);
						WMSPanel.add(batchNo);
						WMSPanel.add("N");
						WMSPanel.add(eventInfo.getEventUser());
						WMSPanel.add(eventInfo.getEventTimeKey());
						
						updateWMSPanelList.add(WMSPanel.toArray());
					}

					setShipFlag(eventInfo, innerName, batchNo);

					ProcessGroupKey groupKey = new ProcessGroupKey();
					groupKey.setProcessGroupName(innerName);

					ProcessGroup processGroupData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(groupKey);

					int materialQuantity = (int) processGroupData.getMaterialQuantity();
					
					//For WMS Qty
					palletPanelQty += materialQuantity;
					
					Map<String, String> innerUdfs = processGroupData.getUdfs();
					String productRequestName = innerUdfs.get("PRODUCTREQUESTNAME");

					IncreaseFinishQuantity(eventInfo, productRequestName, materialQuantity);
				}

				setShipFlag(eventInfo, outerName, batchNo);
			}
			
			setShipFlag(eventInfo, palletName, batchNo);
			
			//SAP
			if(StringUtils.isNotEmpty(workOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
			{
				SuperProductRequest superWO = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{workOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
				
				Map<String, String> factoryInfo = ExtendedObjectProxy.getSuperProductRequestService().getFactoryInfo(superWO.getProductRequestType(), superWO.getSubProductionType(), superWO.getRiskFlag(),
						palletData.getUdfs().get("LOTDETAILGRADE"));
				
				Map<String, String> ERPInfo = new HashMap<>();
				
				ERPInfo.put("ZID", eventInfo.getEventTimeKey());
				ERPInfo.put("AUFNR", superWO.getProductRequestName());
				ERPInfo.put("MATNR", palletData.getUdfs().get("PRODUCTSPECNAME").substring(0, palletData.getUdfs().get("PRODUCTSPECNAME").length() - 1));
				ERPInfo.put("GAMNG", Integer.toString(palletPanelQty));
				ERPInfo.put("MEIN", superWO.getProductType());
				ERPInfo.put("WERKS", factoryInfo.get("CODE"));
				ERPInfo.put("LGORT", factoryInfo.get("LOCATION"));
				ERPInfo.put("CHARG", batchNo);
				ERPInfo.put("ZUNSHIP", "");
				ERPInfo.put("ZDTIME", ERPEventInfo.getEventTimeKey().substring(0, 8));
				ERPInfo.put("ZNOTE", "");
					
				ERPReportListAll.add(ERPInfo);
			}
		}

		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE LOT ");
		sql.append("   SET LOTSTATE = ?, ");
		sql.append("       LOTPROCESSSTATE = ?, ");
		sql.append("       LASTEVENTNAME = ?, ");
		sql.append("       LASTEVENTTIMEKEY = ?, ");
		sql.append("       LASTEVENTTIME = ?, ");
		sql.append("       LASTEVENTUSER = ?, ");
		sql.append("       LASTEVENTCOMMENT = ?, ");
		sql.append("       LASTEVENTFLAG = ?, ");
		sql.append("       LASTLOGGEDINTIME = ?, ");
		sql.append("       LASTLOGGEDINUSER = ?, ");
		sql.append("       LASTLOGGEDOUTTIME = ?, ");
		sql.append("       LASTLOGGEDOUTUSER = ? ");
		sql.append(" WHERE LOTNAME = ? ");

		MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), updateLotArgList);
		try 
		{
			CommonUtil.executeBatch("insert", updateLotHistoryList);
		} 
		catch (Exception e) 
		{
			log.error(e.getMessage());
			throw new CustomException(e.getCause());
		}

		
		if (updateWMSPanelList.size() > 0)
		{
			MESProcessGroupServiceProxy.getProcessGroupServiceUtil().insertWMSPanel(updateWMSPanelList);
		}

		for(Map<String, String> ERPInfo : ERPReportListAll)
		{
			EventInfo ERPEventInfo = EventInfoUtil.makeEventInfo("Ship", this.getEventUser(), this.getEventComment(), "", "");
			ERPEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			
			List<Map<String, String>> ERPReportList = new ArrayList<Map<String, String>>();
			ERPReportList.add(ERPInfo);
			
			//Send
			try
			{
				ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().sapVXG371Send(ERPEventInfo, ERPReportList, 1);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}	
		}
		
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

	private void IncreaseFinishQuantity(EventInfo eventInfo, String productRequestName, int materialQuantity) throws CustomException
	{
		ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);

		IncrementFinishedQuantityByInfo incrementFinishedQuantityByInfo = new IncrementFinishedQuantityByInfo();
		incrementFinishedQuantityByInfo.setQuantity(materialQuantity);

		ProductRequest newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementFinishedQuantityBy(productRequestData, incrementFinishedQuantityByInfo, eventInfo);

		if (newProductRequestData.getPlanQuantity() <= newProductRequestData.getFinishedQuantity() + newProductRequestData.getScrappedQuantity())
		{
			MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompleted(eventInfo, productRequestName);
		}
	}

	private void setShipFlag(EventInfo eventInfo, String palletName, String batchNo)
	{
		ProcessGroupKey groupKey = new ProcessGroupKey();
		groupKey.setProcessGroupName(palletName);

		ProcessGroup processGroup = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(groupKey);
		ProcessGroup oldProcessGroup = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(groupKey);

		processGroup.setLastEventName(eventInfo.getEventName());
		processGroup.setLastEventTimeKey(eventInfo.getEventTimeKey());
		processGroup.setLastEventTime(eventInfo.getEventTime());
		processGroup.setLastEventUser(eventInfo.getEventUser());
		processGroup.setLastEventComment(eventInfo.getEventComment());
		processGroup.setLastEventFlag("N");

		Map<String, String> packUdfs = new HashMap<>();
		packUdfs = processGroup.getUdfs();
		packUdfs.put("SHIPFLAG", "Y");
		packUdfs.put("BATCHNO", batchNo);
		processGroup.setUdfs(packUdfs);

		ProcessGroupHistory processGroupHistory = new ProcessGroupHistory();
		processGroupHistory = ProcessGroupServiceProxy.getProcessGroupHistoryDataAdaptor().setHV(oldProcessGroup, processGroup, processGroupHistory);

		ProcessGroupServiceProxy.getProcessGroupService().update(processGroup);
		ProcessGroupServiceProxy.getProcessGroupHistoryService().insert(processGroupHistory);
	}
}
