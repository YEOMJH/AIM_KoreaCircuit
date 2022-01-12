package kr.co.aim.messolution.lot.event.CNX.PostCell;

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

public class ScrapPalletShip extends SyncHandler {

	private static Log log = LogFactory.getLog(ScrapPalletShip.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> palletList = SMessageUtil.getBodySequenceItemList(doc, "PALLETLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("PalletShip", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();

		List<Map<String, Object>> workOrderList = new ArrayList<Map<String, Object>>();
		
		List<ProductRequest> workOrderDataList = new ArrayList<ProductRequest>();

		List<Object[]> updateWMSPanelList = new ArrayList<Object[]>();
		
		String batchNo = "";
		
		List<Map<String, String>> ERPReportListAll = new ArrayList<Map<String, String>>();
		
		for (Element pallet : palletList)
		{
			List<String> boxList = new ArrayList<String>();
			EventInfo ERPEventInfo = EventInfoUtil.makeEventInfo("Ship", this.getEventUser(), this.getEventComment(), "", "");
			ERPEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			
			String palletName = pallet.getChildText("PROCESSGROUPNAME");
			batchNo = "";

			int palletPanelQty = 0;
			
			ProcessGroupKey palletKey = new ProcessGroupKey();
			palletKey.setProcessGroupName(palletName);
			ProcessGroup palletData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(palletKey);
			
			List<Map<String, Object>> result = null;
			String sqlWorkOrder = "SELECT L.PRODUCTREQUESTNAME, COUNT(L.LOTNAME) AS QUANTITY FROM LOT L,PRODUCTREQUEST PR,PROCESSGROUP P"
					+" WHERE  L.PROCESSGROUPNAME=P.PROCESSGROUPNAME AND L.PRODUCTREQUESTNAME=PR.PRODUCTREQUESTNAME  AND P.SUPERPROCESSGROUPNAME=:SUPERPROCESSGROUPNAME"
					+" GROUP BY  L.PRODUCTREQUESTNAME";
					
			Map<String, Object> bindMapWorkOrder = new HashMap<>();
			bindMapWorkOrder.put("SUPERPROCESSGROUPNAME", palletName);
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
			
			 throw new CustomException("PROCESSGROUP-0001", palletName);
				
			}
			String topProductRequestName = ConvertUtil.getMapValueByName(result.get(0), "PRODUCTREQUESTNAME");
			ProductRequest topWorkOrderData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(topProductRequestName);
			
			String topSuperProductRequestName = topWorkOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME").toString();
			
			if(StringUtil.isEmpty(topSuperProductRequestName))
				topSuperProductRequestName = topWorkOrderData.getKey().getProductRequestName();
		    
			String wmsFactoryPosition="2F91";
			String factoryPositionCode = constantMap.SAPFactoryPosition_9F99;
			String factoryCode=constantMap.SAPFactoryCode_5099;
			String productSpecName=topWorkOrderData.getProductSpecName();
			
			if(StringUtil.isEmpty(batchNo))
			{
				batchNo = topSuperProductRequestName.substring(0, 8) + palletData.getUdfs().get("LOTDETAILGRADE");
				
				if(batchNo.length() == 9)
					batchNo += "0";
			}
			
			boxList.add(palletName);
			List<Map<String, Object>> outerPackingList = getPackingList(palletName, "ScrapPacking");

			for (Map<String, Object> outerList : outerPackingList)
			{
				String scrapPackingOuterBoxName = outerList.get("PROCESSGROUPNAME").toString();
				boxList.add(scrapPackingOuterBoxName);
				List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getScrapLotListByProcessGroup(scrapPackingOuterBoxName);

				ProcessGroupKey scrapPackOuterKey = new ProcessGroupKey();
				scrapPackOuterKey.setProcessGroupName(scrapPackingOuterBoxName);
				ProcessGroup scrapPackingOuterData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(scrapPackOuterKey);
				
				for (Lot lot : lotList)
				{
					palletPanelQty++;
					CommonValidation.checkLotShippedState(lot);
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

					if (workOrderList.size() < 1)
					{
						Map<String, Object> WO = new HashMap<String, Object>();
						WO.put("PRODUCTREQUESTNAME", lot.getProductRequestName());
						WO.put("LOTQUANTITY", 1);
						workOrderList.add(WO);
						
						ProductRequest workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lot.getProductRequestName());
						workOrderDataList.add(workOrderData);
					}
					else
					{
						boolean check = false;
						for (int i = workOrderList.size() - 1; i >= 0; i--)
						{
							Map<String, Object> woData = workOrderList.get(i);

							if (woData.get("PRODUCTREQUESTNAME").toString().equals(lot.getProductRequestName()))
							{
								check = true;
								woData.put("LOTQUANTITY", Integer.parseInt(woData.get("LOTQUANTITY").toString()) + 1);
								workOrderList.remove(i);
								workOrderList.add(woData);
								break;
							}
						}
						if (!check)
						{
							Map<String, Object> WO = new HashMap<String, Object>();
							WO.put("PRODUCTREQUESTNAME", lot.getProductRequestName());
							WO.put("LOTQUANTITY", 1);
							workOrderList.add(WO);
							ProductRequest workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lot.getProductRequestName());
							workOrderDataList.add(workOrderData);
						}
					}
					
					//WMS Panel
					String grade = lot.getLotGrade();
					String lotDetailGrade = lot.getUdfs().get("LOTDETAILGRADE").toString();
					if(lotDetailGrade.equals("S1"))
					{
						grade=lotDetailGrade;
						
					}
					String superProductRequestName = "";
					
					for(ProductRequest productRequestData : workOrderDataList)
					{
						if(productRequestData.getKey().getProductRequestName().equals(lot.getProductRequestName()))
						{
							if(StringUtil.isEmpty(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME").toString()))
							{
								superProductRequestName = productRequestData.getKey().getProductRequestName();
							}
							else
							{
								superProductRequestName = productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME").toString();
							}
							
							break;
						}
					}
					
					List<Object> WMSPanel = new ArrayList<Object>();
					WMSPanel.add(lot.getKey().getLotName());
					WMSPanel.add(palletName);
					WMSPanel.add("");
					WMSPanel.add(lot.getProductSpecName().substring(0, lot.getProductSpecName().length() - 1));
					WMSPanel.add(1);
					WMSPanel.add(grade);
					WMSPanel.add(superProductRequestName);
					WMSPanel.add(factoryPositionCode);
					WMSPanel.add(wmsFactoryPosition);//DESTSHOP
					WMSPanel.add(palletName);
					WMSPanel.add(batchNo);//BATCH_NO
					WMSPanel.add("N");
					WMSPanel.add(eventInfo.getEventUser());
					WMSPanel.add(eventInfo.getEventTime());
					WMSPanel.add(scrapPackingOuterBoxName);
					
					updateWMSPanelList.add(WMSPanel.toArray());
				}
			}
			
			//SAP
			String sapFlag=CommonUtil.getEnumDefValueStringByEnumName("SAPFLAG");
			if(StringUtil.isNotEmpty(sapFlag)&&StringUtil.equals(sapFlag, "Y")&&
					StringUtils.isNotEmpty(topWorkOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
			{
				
				SuperProductRequest superWo=new SuperProductRequest();
				try
				{
					superWo=ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{topWorkOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
				}
				catch(greenFrameDBErrorSignal nl)
				{
					throw new CustomException("PROCESSGROUP-0015");
				}				

				Map<String, String> ERPInfo = new HashMap<>();
				
				ERPInfo.put("SEQ", TimeUtils.getCurrentEventTimeKey());
				ERPInfo.put("PRODUCTREQUESTNAME", superWo.getProductRequestName());
				ERPInfo.put("PRODUCTSPECNAME", productSpecName.substring(0, productSpecName.length() - 1));
				ERPInfo.put("PRODUCTQUANTITY", Integer.toString(palletPanelQty));
				ERPInfo.put("PRODUCTTYPE", superWo.getProductType());
				ERPInfo.put("FACTORYCODE",factoryCode);
				ERPInfo.put("FACTORYPOSITION",factoryPositionCode);
				ERPInfo.put("BATCHNO", batchNo);
				ERPInfo.put("UNSHIPFLAG", "");
				Calendar cal = Calendar.getInstance();
				int hour = cal.get(Calendar.HOUR_OF_DAY);
				if(hour >= 19)
				{
					cal.set(Calendar.HOUR_OF_DAY, 0);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MILLISECOND, 0);
					cal.add(Calendar.DAY_OF_MONTH, 1);
					Timestamp receiveTime = new Timestamp(cal.getTime().getTime());
					ERPInfo.put("EVENTTIME", receiveTime.toString().replace("-","").substring(0,8));
				}
				else
				{
					ERPInfo.put("EVENTTIME", ERPEventInfo.getEventTimeKey().substring(0,8));
				}
				ERPInfo.put("NGFLAG", "X");
					
				ERPReportListAll.add(ERPInfo);
			}

			setShipFlag(eventInfo, boxList, batchNo);
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

	private List<Map<String, Object>> getProductRequestQuantity(String palletName) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT PRODUCTREQUESTNAME");
		sql.append("  FROM PROCESSGROUP P");
		sql.append(" WHERE P.PROCESSGROUPNAME = :PROCESSGROUPNAME ");

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("PROCESSGROUPNAME", palletName);

		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		if (sqlResult.size() < 1)
		{
			throw new CustomException("PROCESSGROUP-0004", palletName);
		}

		return sqlResult;
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
	
	private void setShipFlag(EventInfo eventInfo, List<String> boxList, String batchNo)
	{
		for (String palletName : boxList)
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
}
