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
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.NodeStack;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.IncrementReleasedQuantityByInfo;

public class PostCellChangeProductSpec extends SyncHandler {
	private static Log log = LogFactory.getLog(PostCellChangeProductSpec.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String productionType 	 		= SMessageUtil.getBodyItemValue(doc, "PRODUCTIONTYPE", true);
		String productSpecName 			= SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productSpecVer  			= SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String processFlowName 			= SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion 		= SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String processOperationName 	= SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String processOperationVer  	= SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", true);
		String productRequestName   	= SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		String oldProductRequestName	= SMessageUtil.getBodyItemValue(doc, "OLDPRODUCTREQUESTNAME", true);
		String factoryName				= SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		List<Element> lotList 			= SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
		
		String trayGroupName = lotList.get(0).getChildText("COVERNAME");
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeProductSpec", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		String changeType = "";
		String changeName = "";
		String changeOper="";
		List<String> durableNameList = new ArrayList<String>();
		
		if(!trayGroupName.isEmpty())
		{
			log.debug("Check TrayGroup");
			Durable trayGroup = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayGroupName);
			//CommonValidation.CheckDurableHoldState(trayGroup); jinlj20210825
			
			changeType = "TrayGroup";
			changeName = trayGroupName;
			durableNameList.add(trayGroupName);
		}
		
		//Check WO SubProductionType by yueke 20210317 -required by WuMachao
		ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);		
		ProductRequest oldProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(oldProductRequestName);
		//1.Completed WO cant exchange 
		if(StringUtils.equals(productRequestData.getProductRequestState(), "Completed")||StringUtils.equals(oldProductRequestData.getProductRequestState(), "Completed"))
		{
			throw new CustomException("PRODUCTREQUEST-0044");
		}
		String newSubProductionType=productRequestData.getUdfs().get("SUBPRODUCTIONTYPE");
		String oldSubProductionType=oldProductRequestData.getUdfs().get("SUBPRODUCTIONTYPE");
		
		//2. DOE WO Can't ChangeWO
		if(StringUtils.equals(oldSubProductionType, "DOESY")&&
				!StringUtils.equals(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME"), oldProductRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
		{
			throw new CustomException("PRODUCTREQUEST-0046");
		}
		//3.LC Can't changeLC
		if(StringUtils.equals(oldSubProductionType, "P")||StringUtils.equals(oldSubProductionType, "ESLC"))
		{
			if((StringUtils.equals(newSubProductionType, "P")||StringUtils.equals(newSubProductionType, "ESLC"))&&
					!StringUtils.equals(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME"), oldProductRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
			{
				throw new CustomException("PRODUCTREQUEST-0051");
			}
		}
		//4.SY WO only can change to DOE or itself SuperWO
		if(StringUtils.endsWith(oldSubProductionType, "SY")&&!StringUtils.equals(newSubProductionType, "DOESY")
				&&!StringUtils.equals(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME"), oldProductRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
		{
			throw new CustomException("PRODUCTREQUEST-0050");
		}		
		//5. Rework WO and E/P WO can't exchange with each other
		if(StringUtils.contains(newSubProductionType, "FG")||StringUtils.equals(newSubProductionType, "SYZLC"))
		{
			newSubProductionType="FG";
		}
		else 
		{
			newSubProductionType="SY";
		}
		if(StringUtils.contains(oldSubProductionType, "FG")||StringUtils.equals(oldSubProductionType, "SYZLC"))
		{
			oldSubProductionType="FG";
		}
		else 
		{
			oldSubProductionType="SY";
		}
		if((oldSubProductionType=="SY"&&newSubProductionType=="FG")||
				(oldSubProductionType=="FG"&&newSubProductionType=="FG"&&(!StringUtils.equals(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME"), oldProductRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME"))))
				||(oldSubProductionType=="FG"&&newSubProductionType=="SY"))
		{
			throw new CustomException("PRODUCTREQUEST-0045");
		}    		
		
		List<Lot> updateLotList = new ArrayList<Lot>();
		List<LotHistory> updateLotHistory = new ArrayList<LotHistory>();
		
		String nodeStack = NodeStack.getNodeID(factoryName, processFlowName, processOperationName, processOperationVer);
		
		for (Element lotData : lotList)
		{
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getChildText("LOTNAME"));
			Lot oldLot = (Lot) ObjectUtil.copyTo(lot);
			if(!lot.getProductRequestName().equals(oldProductRequestName))
			{
			
			 throw new CustomException("ProductSpec-0001",oldProductRequestName);
				
			}			
			changeOper=oldLot.getProcessOperationName();
			
			String trayName = lot.getCarrierName();
			
			if(!trayName.isEmpty())
			{
				log.debug("Check Tray");
				Durable tray = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
				//CommonValidation.CheckDurableHoldState(tray); jinlj 20210825
				
				if(changeType.isEmpty())
				{
					changeType = "Tray";
					changeName = trayName;
				}
				
				boolean isFound = false;
				for(String durableName : durableNameList)
				{
					if(StringUtil.equals(durableName, trayName))
					{
						isFound = true;
						break;
					}
				}
				
				if(!isFound)
				{
					durableNameList.add(trayName);
				}
			}
			
			log.debug("Check Lot");
			CommonValidation.checkLotProcessState(lot);
			//CommonValidation.checkLotHoldState(lot);  jinlj20210825
			
			lot.setNodeStack(nodeStack);
			lot.setProcessFlowName(processFlowName);
			lot.setProcessFlowVersion(processFlowVersion);
			lot.setProcessOperationName(processOperationName);
			lot.setProcessOperationVersion(processOperationVer);
			lot.setProductionType(productionType);
			lot.setProductRequestName(productRequestName);
			lot.setProductSpecName(productSpecName);
			lot.setProductSpecVersion(productSpecVer);
			lot.setLastEventName(eventInfo.getEventName());
			lot.setLastEventTime(eventInfo.getEventTime());
			lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lot.setLastEventComment(eventInfo.getEventComment());
			lot.setLastEventUser(eventInfo.getEventUser());
			String priority = lotData.getChildText("PRIORITY");
			if (!StringUtils.isEmpty(priority))
			{
				lot.setPriority(Long.valueOf(priority));
			}
			
			Map<String, String> lotUdf = new HashMap<>();	
			lotUdf.put("BEFOREOPERATIONNAME", oldLot.getProcessOperationName());
			lotUdf.put("BEFOREOPERATIONVER", oldLot.getProcessOperationVersion());
			lotUdf.put("BEFOREFLOWNAME", oldLot.getProcessFlowName());	
			lotUdf.put("OLDPRODUCTREQUESTNAME", oldProductRequestName);//jinlj add 20210315
			lot.setUdfs(lotUdf);
			
			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
			
			updateLotList.add(lot);
			updateLotHistory.add(lotHistory);
		}
		
		if(changeType.isEmpty())
			changeType = "Panel";
		
		if(updateLotList.size() > 0)
		{
			log.debug("Insert Lot, LotHistory");
			try
			{
				CommonUtil.executeBatch("update", updateLotList);
				CommonUtil.executeBatch("insert", updateLotHistory);
			}
			catch (Exception e)
			{
				log.error(e.getMessage());
				throw new CustomException(e.getCause());
			}
		}
		
		if(durableNameList.size() > 0)
		{
			List<Durable> durableInfoList = getDurableInfoListByDurableName(durableNameList);
			
			List<Durable> updateDurableList = new ArrayList<Durable>();
			List<DurableHistory> updateDurableHistoryList = new ArrayList<DurableHistory>();
			
			for(Durable durableInfo : durableInfoList)
			{
				Durable oldDurableInfo = (Durable)ObjectUtil.copyTo(durableInfo);
				durableInfo.setLastEventName(eventInfo.getEventName());
				durableInfo.setLastEventTime(eventInfo.getEventTime());
				durableInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
				durableInfo.setLastEventComment(eventInfo.getEventComment());
				durableInfo.setLastEventUser(eventInfo.getEventUser());
				
				DurableHistory durHistory = new DurableHistory();
				durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldDurableInfo, durableInfo, durHistory);
				
				updateDurableList.add(durableInfo);
				updateDurableHistoryList.add(durHistory);
			}
			
			if(updateDurableList.size() > 0)
			{
				log.debug("Insert Durable, DurableHistory");
				try
				{
					CommonUtil.executeBatch("update", updateDurableList, true);
					CommonUtil.executeBatch("insert", updateDurableHistoryList, true);
				}
				catch (Exception e)
				{
					log.error(e.getMessage());
					throw new CustomException(e.getCause());
				}
			}
		}
		
		//Change WorkOrder Quantity
		int qty = lotList.size();
		
		if(qty > 0)
		{
			//new project add release qty			
			IncrementReleasedQuantityByInfo incrementReleasedQuantityByInfo = new IncrementReleasedQuantityByInfo();
			incrementReleasedQuantityByInfo.setQuantity(qty);

			int createdQty = Integer.parseInt(productRequestData.getUdfs().get("CREATEDQUANTITY")) + qty;

			Map<String, String> productRequestUdfs = productRequestData.getUdfs();
			productRequestUdfs.put("CREATEDQUANTITY", Integer.toString(createdQty));
			incrementReleasedQuantityByInfo.setUdfs(productRequestUdfs);

			eventInfo.setEventName("IncreamentQuantity");
			//productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementReleasedQuantityBy(productRequestData, incrementReleasedQuantityByInfo, eventInfo);
			productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementReleasedQuantityBy_Lock(productRequestData, incrementReleasedQuantityByInfo, eventInfo);
			
			if( productRequestData.getPlanQuantity() < productRequestData.getReleasedQuantity() )
			{
				throw new CustomException("PRODUCTREQUEST-0026", String.valueOf(productRequestData.getPlanQuantity()), String.valueOf(productRequestData.getReleasedQuantity()));
			}
			
			//old project substr release qty
			eventInfo.setEventName("DecreamentQuantity");
			
			IncrementReleasedQuantityByInfo DecrementReleasedQuantityByInfo = new IncrementReleasedQuantityByInfo();
			DecrementReleasedQuantityByInfo.setQuantity(-qty);
			
			int deCreatedQty = Integer.parseInt(oldProductRequestData.getUdfs().get("CREATEDQUANTITY")) - qty;

			Map<String, String> deProductRequestUdfs = oldProductRequestData.getUdfs();
			deProductRequestUdfs.put("CREATEDQUANTITY", Integer.toString(deCreatedQty));
			DecrementReleasedQuantityByInfo.setUdfs(deProductRequestUdfs);

			//oldProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementReleasedQuantityBy(oldProductRequestData, DecrementReleasedQuantityByInfo, eventInfo);
			oldProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementReleasedQuantityBy_Lock(oldProductRequestData, DecrementReleasedQuantityByInfo, eventInfo);
			
			String sapFlag=CommonUtil.getEnumDefValueStringByEnumName("SAPFLAG");
			if(StringUtil.isNotEmpty(sapFlag)&&StringUtil.equals(sapFlag, "Y")&& StringUtil.isNotEmpty(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
			{
				Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotList.get(0).getChildText("LOTNAME"));
				
				if(!StringUtil.equals(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME"), oldProductRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
				{
					try
					{
						insertIntoSAPDB(productRequestData, oldProductRequestData, lotData, eventInfo, changeType, qty, changeName,changeOper);
					}
					catch (Exception e)
					{
						eventLog.info("SAP Report Error");
					}
				}
			}
		}
		
		return doc;
	}
	
	private void insertIntoSAPDB(ProductRequest newWO, ProductRequest oldWO, Lot lotData, EventInfo eventInfo, String changeType, int qty, String changeName,String oldProcessOperationName) throws CustomException
	{
		List<Object[]> insertSAPList = new ArrayList<Object[]>();
		List<Object> insertSAPData = new ArrayList<Object>();
		
		SuperProductRequest newSuperWO = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{newWO.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
		String factoryPositionCode = "";
		String sql2 = " SELECT DESCRIPTION FROM ENUMDEFVALUE WHERE ENUMNAME=:ENUMNAME AND ENUMVALUE=:ENUMVALUE ";
		
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("ENUMNAME", "FactoryPosition");
		bindMap.put("ENUMVALUE", newSuperWO.getFactoryName());
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql2, bindMap);
		
		if(sqlResult.size() > 0){
			factoryPositionCode = sqlResult.get(0).get("DESCRIPTION").toString();
		}
		else
		{
			factoryPositionCode="";
		}
		
		String changeOper = "";
		String getChangeOperSql = " SELECT DESCRIPTION FROM ENUMDEFVALUE WHERE ENUMNAME='PostCellChangeSpecOperMappling' AND ENUMVALUE=:ENUMVALUE ";
		
		Map<String, String> getChangeOperBindMap = new HashMap<String, String>();
		getChangeOperBindMap.put("ENUMVALUE", oldProcessOperationName);
		
		List<Map<String, Object>> getChangeOper = GenericServiceProxy.getSqlMesTemplate().queryForList(getChangeOperSql, getChangeOperBindMap);
		
		if(getChangeOper.size() > 0){
			changeOper=getChangeOper.get(0).get("DESCRIPTION").toString();
		}
		else
		{
			changeOper=lotData.getProcessOperationName();
		}
        
		insertSAPData.add(TimeUtils.getCurrentEventTimeKey());
		insertSAPData.add(oldWO.getUdfs().get("SUPERPRODUCTREQUESTNAME"));
		insertSAPData.add(changeOper);
		insertSAPData.add(qty);
		insertSAPData.add(newSuperWO.getProductType());
		insertSAPData.add(newSuperWO.getProductRequestName());
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
			insertSAPData.add(receiveTime.toString().replace("-","").substring(0,8));
		}
		else
		{
			insertSAPData.add(eventInfo.getEventTimeKey().substring(0,8));
		}
		insertSAPData.add("N");
		insertSAPData.add("");
		insertSAPData.add("");
		if(changeType.equals("Panel"))
		{
			insertSAPData.add(lotData.getKey().getLotName());
		}
		else
		{
			insertSAPData.add(changeName);
		}
		insertSAPData.add(eventInfo.getEventUser());
		insertSAPData.add(eventInfo.getEventComment());
		insertSAPData.add(factoryPositionCode);
		insertSAPList.add(insertSAPData.toArray());
		
		
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO MES_SAPIF_PP006@OADBLINK.V3FAB.COM (SEQ, OLDPRODUCTREQUESTNAME, PROCESSOPERATIONNAME, PRODUCTQUANTIRY, PRODUCTTYPE,  ");
		sql.append("    PRODUCTREQUESTNAME, EVENTTIME, ESBFLAG, RESULT, RESULTMESSAGE,  ");
		sql.append("    LOTNAME, EVENTUSER, EVENTCOMMENT,FACTORYPOSITION ) VALUES ");
		sql.append("    (?, ?, ?, ?, ?, ");
		sql.append("    ?, ?, ?, ?, ?, ");
		sql.append("    ?, ?, ?, ? ) ");

		MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), insertSAPList);
	}
	
	private List<Durable> getDurableInfoListByDurableName(List<String> durableNameList)
	{
		String condition = "WHERE DURABLENAME IN(";
		for (String durableName : durableNameList) 
		{
			condition += "'" + durableName + "',";
		}
		condition = condition.substring(0, condition.length() - 1) + ")";
		
		List<Durable> durableDataList = DurableServiceProxy.getDurableService().select(condition, new Object[] { });
		
		return durableDataList;
	}
} 
