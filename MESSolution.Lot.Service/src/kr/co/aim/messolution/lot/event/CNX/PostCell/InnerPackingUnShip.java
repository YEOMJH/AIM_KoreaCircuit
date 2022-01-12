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
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
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
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.data.ProductSpecKey;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.IncrementFinishedQuantityByInfo;

public class InnerPackingUnShip extends SyncHandler {

	private static Log log = LogFactory.getLog(InnerPackingUnShip.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> innerBoxList = SMessageUtil.getBodySequenceItemList(doc, "PALLETLIST", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("InnerPackingUnShip", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();

		List<Object[]> deleteArgListSHT = new ArrayList<Object[]>();
		String sapFlag=CommonUtil.getEnumDefValueStringByEnumName("SAPFLAG");
		
		List<Map<String, String>> ERPReportListAll = new ArrayList<Map<String, String>>();
		
		for (Element innerBox : innerBoxList)
		{
			EventInfo ERPEventInfo = EventInfoUtil.makeEventInfo("UnShip", this.getEventUser(), this.getEventComment(), "", "");
			ERPEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			
			String innerName = innerBox.getChildText("PROCESSGROUPNAME");
			int innerBoxPanelQty = 0;

			ProcessGroupKey innerBoxKey = new ProcessGroupKey();
			innerBoxKey.setProcessGroupName(innerName);

			ProcessGroup innerBoxData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(innerBoxKey);// innerData
			
			if(!innerBoxData.getProcessGroupType().equals("InnerPacking"))
			{
				throw new CustomException("PROCESSGROUP-0103", eventInfo.getEventName(), innerBoxData.getProcessGroupType());
			}
			
			ProductRequest workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(innerBoxData.getUdfs().get("PRODUCTREQUESTNAME"));
			
			if (StringUtils.isNotEmpty(innerBoxData.getUdfs().get("SHIPNO")))
			{
				throw new CustomException("PROCESSGROUP-0101", innerName);
			}
			
			String outerBoxName = innerBoxData.getSuperProcessGroupName();
			
			//Update SuperProcessGroupData
			if(StringUtils.isNotEmpty(outerBoxName))
			{
				ProcessGroupKey palletKey = new ProcessGroupKey();
				palletKey.setProcessGroupName(outerBoxName);

				ProcessGroup processGroup = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(palletKey);// OuterBoxData
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

			setShipFlag(eventInfo, innerName);

			ProcessGroupKey groupKey = new ProcessGroupKey();
			groupKey.setProcessGroupName(innerName);

			ProcessGroup processGroupData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(groupKey);

			int materialQuantity = (int) processGroupData.getMaterialQuantity();
			innerBoxPanelQty+=materialQuantity;

			Map<String, String> innerUdfs = processGroupData.getUdfs();
			String productRequestName = innerUdfs.get("PRODUCTREQUESTNAME");

			IncreaseFinishQuantity(eventInfo, productRequestName, materialQuantity);
			
			//SAP
			if(StringUtil.isNotEmpty(sapFlag)&&StringUtil.equals(sapFlag, "Y")&&
					StringUtils.isNotEmpty(workOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
			{
				SuperProductRequest superWO = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{workOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
				
				String batchNo = superWO.getProductRequestName() + innerBoxData.getUdfs().get("LOTDETAILGRADE");
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
				ERPInfo.put("PRODUCTSPECNAME", innerBoxData.getUdfs().get("PRODUCTSPECNAME").substring(0, innerBoxData.getUdfs().get("PRODUCTSPECNAME").length() - 1));
				ERPInfo.put("PRODUCTQUANTITY", "-" + Integer.toString(innerBoxPanelQty));
				ERPInfo.put("PRODUCTTYPE", superWO.getProductType());
				if(innerBoxData.getUdfs().get("LOTDETAILGRADE").equals("S")
						||innerBoxData.getUdfs().get("LOTDETAILGRADE").contains("C")
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
					ERPInfo.put("EVENTTIME", ERPEventInfo.getEventTimeKey().substring(0, 8));
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

		if (deleteArgListSHT.size() > 0)
		{
			MESProcessGroupServiceProxy.getProcessGroupServiceUtil().deleteTRANS_WH_SHT(deleteArgListSHT);
		}
		
		return doc;
	}

	private void IncreaseFinishQuantity(EventInfo eventInfo, String productRequestName, int materialQuantity) throws CustomException
	{
		ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);

		IncrementFinishedQuantityByInfo incrementFinishedQuantityByInfo = new IncrementFinishedQuantityByInfo();
		incrementFinishedQuantityByInfo.setQuantity(-materialQuantity);

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
		processGroup.setUdfs(packUdfs);

		ProcessGroupHistory processGroupHistory = new ProcessGroupHistory();
		processGroupHistory = ProcessGroupServiceProxy.getProcessGroupHistoryDataAdaptor().setHV(oldProcessGroup, processGroup, processGroupHistory);

		ProcessGroupServiceProxy.getProcessGroupService().update(processGroup);
		ProcessGroupServiceProxy.getProcessGroupHistoryService().insert(processGroupHistory);
	}
}
