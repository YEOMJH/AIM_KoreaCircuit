
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

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
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
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.data.ProductSpecKey;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.IncrementFinishedQuantityByInfo;

public class OuterPackingUnShip extends SyncHandler {

	private static Log log = LogFactory.getLog(OuterPackingUnShip.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> outerBoxList = SMessageUtil.getBodySequenceItemList(doc, "PALLETLIST", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("OuterPackingUnShip", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();
		
		List<Object[]> deleteArgListSHT = new ArrayList<Object[]>();
		String sapFlag=CommonUtil.getEnumDefValueStringByEnumName("SAPFLAG");
		List<Map<String, String>> ERPReportListAll = new ArrayList<Map<String, String>>();
		
		for (Element outerBox : outerBoxList)
		{
			EventInfo ERPEventInfo = EventInfoUtil.makeEventInfo("UnShip", this.getEventUser(), this.getEventComment(), "", "");
			ERPEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			
			String outerBoxName = outerBox.getChildText("PROCESSGROUPNAME");

			ProcessGroupKey outerBoxKey = new ProcessGroupKey();
			outerBoxKey.setProcessGroupName(outerBoxName);

			int outerBoxPanelQuantity = 0;
			ProcessGroup outerBoxData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(outerBoxKey);// outerData
			
			if(!outerBoxData.getProcessGroupType().equals("OuterPacking"))
			{
				throw new CustomException("PROCESSGROUP-0103", eventInfo.getEventName(), outerBoxData.getProcessGroupType());
			}
			List<Map<String, Object>> result = null;
			String sqlWorkOrder = "SELECT L.PRODUCTREQUESTNAME, COUNT(L.LOTNAME) AS QUANTITY FROM LOT L,PRODUCTREQUEST PR,PROCESSGROUP P"
					+" WHERE  L.PROCESSGROUPNAME=P.PROCESSGROUPNAME AND L.PRODUCTREQUESTNAME=PR.PRODUCTREQUESTNAME  AND P.SUPERPROCESSGROUPNAME=:SUPERPROCESSGROUPNAME"
					+" GROUP BY  L.PRODUCTREQUESTNAME";
					
			Map<String, Object> bindMapWorkOrder = new HashMap<>();
			bindMapWorkOrder.put("SUPERPROCESSGROUPNAME", outerBoxName);
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
			
			 throw new CustomException("PROCESSGROUP-0001", outerBoxName);
				
			}
			ProductRequest workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(result.get(0).get("PRODUCTREQUESTNAME").toString());
			
			if (StringUtils.isNotEmpty(outerBoxData.getUdfs().get("SHIPNO")))
			{
				throw new CustomException("PROCESSGROUP-0101", outerBoxName);
			}
			
			String palletName = outerBoxData.getSuperProcessGroupName();
			
			//Update SuperProcessGroupData
			if(StringUtils.isNotEmpty(palletName))
			{
				ProcessGroupKey palletKey = new ProcessGroupKey();
				palletKey.setProcessGroupName(palletName);

				ProcessGroup processGroup = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(palletKey);// palletData
				ProcessGroup oldProcessGroup = (ProcessGroup) ObjectUtil.copyTo(processGroup);
				
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				
				processGroup.setMaterialQuantity(processGroup.getMaterialQuantity() - 1);
				processGroup.setLastEventName(eventInfo.getEventName());
				processGroup.setLastEventTimeKey(eventInfo.getEventTimeKey());
				processGroup.setLastEventTime(eventInfo.getEventTime());
				processGroup.setLastEventUser(eventInfo.getEventUser());
				processGroup.setLastEventComment(eventInfo.getEventComment());
				processGroup.setLastEventFlag("N");

				ProcessGroupHistory processGroupHistory = new ProcessGroupHistory();
				processGroupHistory = ProcessGroupServiceProxy.getProcessGroupHistoryDataAdaptor().setHV(oldProcessGroup, processGroup, processGroupHistory);

				ProcessGroupServiceProxy.getProcessGroupService().update(processGroup);
				ProcessGroupServiceProxy.getProcessGroupHistoryService().insert(processGroupHistory);
				
			}
			
			
			List<Map<String, Object>> innerPackingList = getPackingList(outerBoxName, "InnerPacking");// innerData

			for (Map<String, Object> innerList : innerPackingList)
			{
				String innerName = innerList.get("PROCESSGROUPNAME").toString();
				List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotListByProcessGroup(innerName);

				for (Lot lot : lotList)
				{
					CommonValidation.checkLotUnShippedState(lot);
					Lot oldLot = (Lot)ObjectUtil.copyTo(lot);

					List<Object> lotBindList = new ArrayList<Object>();

					lotBindList.add(constantMap.Lot_Released);
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
					lot.setLotState(constantMap.Lot_Released);
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
					
					if (MESProcessGroupServiceProxy.getProcessGroupServiceUtil().checkWMSSHT(lot.getKey().getLotName()))
					{
						List<Object> deleteBindListSHT = new ArrayList<Object>();

						deleteBindListSHT.add(lot.getKey().getLotName());

						deleteArgListSHT.add(deleteBindListSHT.toArray());
					}
				}
				List<Durable> TrayList = MESDurableServiceProxy.getDurableServiceUtil().getCoverListByProcessGroup(innerName);
				if (TrayList != null && !TrayList.isEmpty())
				{
					for(Durable tray : TrayList)
					{
						//tray.setDurableType(constantMap.DURABLETYPE_Tray);
						tray.setDurableState(constantMap.Dur_InUse);
						DurableServiceProxy.getDurableService().update(tray);
						SetEventInfo setEventInfo = new SetEventInfo();
						MESDurableServiceProxy.getDurableServiceImpl().setEvent(tray, setEventInfo, eventInfo);						
					}
				}
				setShipFlag(eventInfo, innerName);

				ProcessGroupKey groupKey = new ProcessGroupKey();
				groupKey.setProcessGroupName(innerName);

				ProcessGroup processGroupData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(groupKey);

				int materialQuantity = (int) processGroupData.getMaterialQuantity();
				outerBoxPanelQuantity += materialQuantity;
			}
			for(int i=0;i<result.size();i++)
			{
			 String productRequestName =  result.get(i).get("PRODUCTREQUESTNAME").toString();
			 int Quantity = Integer.parseInt(result.get(i).get("QUANTITY").toString());
			 eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			 eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
			 IncreaseFinishQuantity(eventInfo, productRequestName, Quantity);
			 eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			 eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
			}

			setShipFlag(eventInfo, outerBoxName);
			//SAP
			if(StringUtil.isNotEmpty(sapFlag)&&StringUtil.equals(sapFlag, "Y")&&
					StringUtils.isNotEmpty(workOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
			{
				SuperProductRequest superWO = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{workOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
				
				String batchNo = superWO.getProductRequestName() + outerBoxData.getUdfs().get("LOTDETAILGRADE");
				if(batchNo.length() == 9)
					batchNo += "0";
				
                String factoryPositionCode = "";
				String sql = " SELECT DESCRIPTION FROM ENUMDEFVALUE WHERE ENUMNAME=:ENUMNAME AND ENUMVALUE=:ENUMVALUE ";
				
				Map<String, String> bindMap = new HashMap<String, String>();
				bindMap.put("ENUMNAME", "PostCellFactoryPosition");
				bindMap.put("ENUMVALUE", superWO.getSubProductionType());
				
				List<Map<String, Object>> sqlResult1 = 
						GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
				
				if(sqlResult1.size() > 0){
					factoryPositionCode = sqlResult1.get(0).get("DESCRIPTION").toString();
				}
				else
				{
					factoryPositionCode="";
				}
				
				
				Map<String, String> ERPInfo = new HashMap<>();
				
				ERPInfo.put("SEQ", TimeUtils.getCurrentEventTimeKey());
				ERPInfo.put("PRODUCTREQUESTNAME", superWO.getProductRequestName());
				ERPInfo.put("PRODUCTSPECNAME", outerBoxData.getUdfs().get("PRODUCTSPECNAME").substring(0, outerBoxData.getUdfs().get("PRODUCTSPECNAME").length() - 1));
				ERPInfo.put("PRODUCTQUANTITY", "-" + Integer.toString(outerBoxPanelQuantity));
				ERPInfo.put("PRODUCTTYPE", superWO.getProductType());
				if(outerBoxData.getUdfs().get("LOTDETAILGRADE").equals("S")
						||outerBoxData.getUdfs().get("LOTDETAILGRADE").contains("C")
						||((workOrderData.getProductRequestType().equals("E")||workOrderData.getProductRequestType().equals("T"))&&!workOrderData.getUdfs().get("SUBPRODUCTIONTYPE").equals("ESLC")
								&&!workOrderData.getUdfs().get("SUBPRODUCTIONTYPE").equals("LCFG")&&!workOrderData.getUdfs().get("SUBPRODUCTIONTYPE").equals("SLCFG")
								&&!workOrderData.getUdfs().get("SUBPRODUCTIONTYPE").equals("SYZLC")))
				{
					ERPInfo.put("FACTORYCODE",constantMap.SAPFactoryCode_5099);
					ERPInfo.put("FACTORYPOSITION", constantMap.SAPFactoryPosition_9F99);
				}
				else
				{
					ERPInfo.put("FACTORYCODE", constantMap.SAPFactoryCode_5001 );
					ERPInfo.put("FACTORYPOSITION",factoryPositionCode);
				}
				ERPInfo.put("BATCHNO", batchNo);
				ERPInfo.put("UNSHIPFLAG", "X");
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
				ERPInfo.put("NGFLAG", "");
				
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

		if (StringUtil.isNotEmpty(sapFlag)&&StringUtil.equals(sapFlag, "Y")&&deleteArgListSHT.size() > 0)
		{
			MESProcessGroupServiceProxy.getProcessGroupServiceUtil().deleteTRANS_WH_SHT(deleteArgListSHT);
		}
		
		for(Map<String, String> ERPInfo : ERPReportListAll)
		{
			EventInfo ERPEventInfo = EventInfoUtil.makeEventInfo("UnShip", this.getEventUser(), this.getEventComment(), "", "");
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

	private List<Map<String, Object>> getPackingList(String palletName, String processGroupType)
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
		

		return sqlResult;
	}

	private void IncreaseFinishQuantity(EventInfo eventInfo, String productRequestName, int materialQuantity) throws CustomException
	{
		ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);

		IncrementFinishedQuantityByInfo incrementFinishedQuantityByInfo = new IncrementFinishedQuantityByInfo();
		incrementFinishedQuantityByInfo.setQuantity(-materialQuantity);
		SuperProductRequest superProductRequest = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
		if (superProductRequest.getProductRequestState().equals("Completed"))
		{
			throw new CustomException("PRODUCTREQUEST-0004", superProductRequest.getProductRequestState());
		}
		ProductRequest newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementFinishedQuantityBy(productRequestData, incrementFinishedQuantityByInfo, eventInfo);

		if (newProductRequestData.getProductRequestState().equals("Completed")
				&& (newProductRequestData.getPlanQuantity() > newProductRequestData.getFinishedQuantity() + newProductRequestData.getScrappedQuantity()))
		{
			MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeReleased(eventInfo, productRequestName);
		}
	}

	private void setShipFlag(EventInfo eventInfo, String palletName)
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
		packUdfs.put("SHIPFLAG", "");
		packUdfs.put("SHIPNO", "");
		packUdfs.put("BATCHNO", "");
		processGroup.setUdfs(packUdfs);

		ProcessGroupHistory processGroupHistory = new ProcessGroupHistory();
		processGroupHistory = ProcessGroupServiceProxy.getProcessGroupHistoryDataAdaptor().setHV(oldProcessGroup, processGroup, processGroupHistory);

		ProcessGroupServiceProxy.getProcessGroupService().update(processGroup);
		ProcessGroupServiceProxy.getProcessGroupHistoryService().insert(processGroupHistory);
	}
}
