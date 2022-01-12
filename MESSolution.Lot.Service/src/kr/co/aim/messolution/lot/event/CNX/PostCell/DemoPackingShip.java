package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
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
import kr.co.aim.greentrack.processoperationspec.ProcessOperationSpecServiceProxy;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpecKey;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.data.ProductSpecKey;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.IncrementFinishedQuantityByInfo;

public class DemoPackingShip   extends SyncHandler {
	private static Log log = (Log) LogFactory.getLog(DemoPackingShip.class);

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		List<Element> innerBoxList = SMessageUtil.getBodySequenceItemList(doc, "PALLETLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DemoPackingShip", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		//String wmsFactoryName = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getWMSFactoryName();
	
		Date date = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		try
		{
			date = dateFormat.parse(eventInfo.getEventTime().toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();
		
		List<Object[]> updateWMSPanelList = new ArrayList<Object[]>();
		String sapFlag=CommonUtil.getEnumDefValueStringByEnumName("SAPFLAG");
		
		List<Map<String, String>> ERPReportListAll = new ArrayList<Map<String, String>>();
		
		for (Element innerBox : innerBoxList)
		{
			EventInfo ERPEventInfo = EventInfoUtil.makeEventInfo("Ship", this.getEventUser(), this.getEventComment(), "", "");
			ERPEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			
			String innerName = innerBox.getChildText("PROCESSGROUPNAME");

			ProcessGroupKey innerBoxKey = new ProcessGroupKey();
			
			innerBoxKey.setProcessGroupName(innerName);

			ProcessGroup innerBoxData = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(innerBoxKey);
			
			List<Map<String, Object>> result = null;
			String sqlWorkOrder = "SELECT L.PRODUCTREQUESTNAME, COUNT(L.LOTNAME) AS QUANTITY FROM LOT L,PRODUCTREQUEST PR,PROCESSGROUP P"
					+" WHERE  L.PROCESSGROUPNAME=:PROCESSGROUPNAME AND L.PRODUCTREQUESTNAME=PR.PRODUCTREQUESTNAME  AND P.PROCESSGROUPNAME=L.PROCESSGROUPNAME"
					+" GROUP BY  L.PRODUCTREQUESTNAME";
					
			Map<String, Object> bindMapWorkOrder = new HashMap<>();
			bindMapWorkOrder.put("PROCESSGROUPNAME", innerName);
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
			
			 throw new CustomException("PROCESSGROUP-0001", innerName);
				
			}
			if(result.size()>0&&!result.isEmpty())
			{
				ProductRequest workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(result.get(0).get("PRODUCTREQUESTNAME").toString());
				
				String superProductRequestName = workOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME").toString();
				Map<String, String> innerUdfs = innerBoxData.getUdfs();
				
				if(StringUtil.isEmpty(superProductRequestName))
					superProductRequestName = workOrderData.getKey().getProductRequestName();
				
				String batchNo = superProductRequestName.substring(0, 8) + innerBoxData.getUdfs().get("LOTDETAILGRADE");
				if(batchNo.length() == 9)
					batchNo += "0";
				String factoryPositionCode = "";
				String factoryCode="";
				String sql = " SELECT DESCRIPTION FROM ENUMDEFVALUE WHERE ENUMNAME=:ENUMNAME AND ENUMVALUE=:ENUMVALUE ";
				
				Map<String, String> bindMap = new HashMap<String, String>();
				bindMap.put("ENUMNAME", "PostCellFactoryPosition");
				bindMap.put("ENUMVALUE",workOrderData.getUdfs().get("SUBPRODUCTIONTYPE"));
				
				List<Map<String, Object>> sqlResult1 = 
						GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
				
				if(sqlResult1.size() > 0){
					factoryPositionCode = sqlResult1.get(0).get("DESCRIPTION").toString();
				}
				else
				{
					factoryPositionCode="";
				}
				if(innerBoxData.getUdfs().get("LOTDETAILGRADE").equals("S")
						||innerBoxData.getUdfs().get("LOTDETAILGRADE").contains("C")
						||((workOrderData.getProductRequestType().equals("E")||workOrderData.getProductRequestType().equals("T"))&&!workOrderData.getUdfs().get("SUBPRODUCTIONTYPE").equals("ESLC")
								&&!workOrderData.getUdfs().get("SUBPRODUCTIONTYPE").equals("LCFG")&&!workOrderData.getUdfs().get("SUBPRODUCTIONTYPE").equals("SLCFG")
								&&!workOrderData.getUdfs().get("SUBPRODUCTIONTYPE").equals("SYZLC")))
				{
					factoryCode=constantMap.SAPFactoryCode_5099;
					factoryPositionCode=constantMap.SAPFactoryPosition_9F99;
				}
				else
				{
					factoryCode=constantMap.SAPFactoryCode_5001;
				}
				String innerBoxProductSpec = innerBoxData.getUdfs().get("PRODUCTSPECNAME");
				ProductSpecKey productSpecKey = new ProductSpecKey();
				productSpecKey.setFactoryName(factoryName);
				productSpecKey.setProductSpecName(innerBoxProductSpec);
				productSpecKey.setProductSpecVersion("00001");
				List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotListByProcessGroup(innerName);
				//jinlj update Trayinfo
				List<Durable> TrayList = MESDurableServiceProxy.getDurableServiceUtil().getCoverListByProcessGroup(innerName);
				if (TrayList != null && !TrayList.isEmpty())
				{
					for(Durable tray : TrayList)
					{
						DurableSpec durableSpecData = GenericServiceProxy.getSpecUtil().getDurableSpec(tray.getFactoryName(), tray.getDurableSpecName(),
								tray.getDurableSpecVersion());
						
						tray.setDurableType(constantMap.DURABLETYPE_Tray);
						tray.setCapacity(durableSpecData.getDefaultCapacity());
						tray.setLotQuantity(0);
						tray.setDurableState(constantMap.Dur_Available);
						DurableServiceProxy.getDurableService().update(tray);
						SetEventInfo setEventInfo = new SetEventInfo();
						setEventInfo.getUdfs().put("COVERNAME", "");
						setEventInfo.getUdfs().put("POSITION", "");
						setEventInfo.getUdfs().put("DURABLETYPE1", "Tray");
						setEventInfo.getUdfs().put("BCRFLAG", "");
						setEventInfo.getUdfs().put("OPERATIONMODE", "");
						MESDurableServiceProxy.getDurableServiceImpl().setEvent(tray, setEventInfo, eventInfo);						
					}
				}

				if (lotList.size() < 1)
				{
					throw new CustomException("PROCESSGROUP-0001", innerName);
				}
				int innerBoxPanelQty = (int) innerBoxData.getMaterialQuantity();;

		    	for (Lot lot : lotList)
				{
					CommonValidation.checkLotShippedState(lot);
					CommonValidation.checkLotState(lot);
					Lot oldLot = (Lot)ObjectUtil.copyTo(lot);

					String processOperationName = lot.getProcessOperationName();
					ProcessOperationSpec processOperationSpec = ProcessOperationSpecServiceProxy.getProcessOperationSpecService().selectByKey(
							new ProcessOperationSpecKey("POSTCELL", processOperationName, "00001"));

					if (!StringUtil.equals(processOperationSpec.getDetailProcessOperationType(), "OuterPack"))
					{
						throw new CustomException("PANEL-0007", lot.getKey().getLotName());
					}

					List<Object> lotBindList = new ArrayList<Object>();

					lotBindList.add(constantMap.Lot_Shipped);
					lotBindList.add("");//jinlj
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
					WMSPanel.add("N");
					WMSPanel.add(lot.getProductionType());
					WMSPanel.add(lot.getProductSpecName());
					WMSPanel.add(lotDetailGrade);
					WMSPanel.add(lot.getProductRequestName());
					WMSPanel.add(innerName);
					WMSPanel.add(innerBoxPanelQty);
					WMSPanel.add("");
					WMSPanel.add("");
					WMSPanel.add(eventInfo.getEventName());
					WMSPanel.add(eventInfo.getEventTimeKey());
					WMSPanel.add(eventInfo.getEventTime());
					WMSPanel.add(eventInfo.getEventUser());
					WMSPanel.add(eventInfo.getEventComment());
					WMSPanel.add(batchNo);
					WMSPanel.add("POSTCELL");
					WMSPanel.add(factoryCode);
					WMSPanel.add(factoryPositionCode);
					updateWMSPanelList.add(WMSPanel.toArray());
				}
				
				setShipFlag(eventInfo, innerName, batchNo);

				for(int i=0;i<result.size();i++)
				{
				 String productRequestName =  result.get(i).get("PRODUCTREQUESTNAME").toString();
				 int materialQuantity = Integer.parseInt(result.get(i).get("QUANTITY").toString());
				 eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				 eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
				 IncreaseFinishQuantity(eventInfo, productRequestName, materialQuantity);
				 eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				 eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
				}

				//SAP
				if(StringUtil.isNotEmpty(sapFlag)&&StringUtil.equals(sapFlag, "Y")&&
						StringUtils.isNotEmpty(workOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
				{
					SuperProductRequest superWo=new SuperProductRequest();
					try
					{
						superWo=ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{workOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME").toString()});
					}
					catch(greenFrameDBErrorSignal nl)
					{
						throw new CustomException("PROCESSGROUP-0015");
					}
								
					Map<String, String> ERPInfo = new HashMap<>();
					
					ERPInfo.put("SEQ", TimeUtils.getCurrentEventTimeKey());
					ERPInfo.put("PRODUCTREQUESTNAME", superWo.getProductRequestName());
					ERPInfo.put("PRODUCTSPECNAME", innerBoxData.getUdfs().get("PRODUCTSPECNAME").substring(0, innerBoxData.getUdfs().get("PRODUCTSPECNAME").length() - 1));
					ERPInfo.put("PRODUCTQUANTITY", Integer.toString(innerBoxPanelQty));
					ERPInfo.put("PRODUCTTYPE", superWo.getProductType());
					ERPInfo.put("FACTORYCODE",factoryCode);
					ERPInfo.put("FACTORYPOSITION",factoryPositionCode);
					ERPInfo.put("BATCHNO", batchNo);
					ERPInfo.put("UNSHIPFLAG", "");
					ERPInfo.put("EVENTTIME", ERPEventInfo.getEventTimeKey().substring(0, 8));
					ERPInfo.put("NGFLAG", "");
						
					ERPReportListAll.add(ERPInfo);
				}
			}
		
		}

		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE LOT ");
		sql.append("   SET LOTSTATE = ?, ");
		sql.append("       CARRIERNAME = ?, ");//jinlj
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
		  insertDemoPanel(updateWMSPanelList);
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
	private void IncreaseFinishQuantity(EventInfo eventInfo, String productRequestName, int materialQuantity) throws CustomException
	{
		ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);

		IncrementFinishedQuantityByInfo incrementFinishedQuantityByInfo = new IncrementFinishedQuantityByInfo();
		incrementFinishedQuantityByInfo.setQuantity(materialQuantity);
		

		ProductRequest newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementFinishedQuantityBy(productRequestData, incrementFinishedQuantityByInfo, eventInfo);
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
	public void insertDemoPanel(List<Object[]> insertArgList) throws CustomException
	{
		try
		{
			StringBuilder sql = new StringBuilder();
			sql.append("INSERT ");
			sql.append("  INTO MES_MODIF_SHIP@OADBLINK.V3FAB.COM (PRODUCTNAME, ");//INTO MES_MODIF_SHIP@OADBLINK.V3FAB.COM;
			//sql.append("  INTO MES_MODIF_SHIP (PRODUCTNAME, ");
			sql.append("                                 DATASTATUS, ");
			sql.append("                                 PRODUCTIONTYPE, ");
			sql.append("                                 PRODUCTSPECNAME, ");
			sql.append("                                 PRODUCTGRADE, ");
			sql.append("                                 PRODUCTREQUESTNAME, ");
			sql.append("                                 INNERBOXID, ");
			sql.append("                                 INNERBOXPRODUCTQUANTITY, ");
			sql.append("                                 OUTERBOXID, ");
			sql.append("                                 OUTERBOXPRODUCTQUANTITY, ");
			sql.append("                                 EVENTNAME, ");
			sql.append("                                 EVENTTIMEKEY, ");
			sql.append("                                 EVENTTIME, ");
			sql.append("                                 EVENTUSER, ");
			sql.append("                                 EVENTCOMMENT, ");
			sql.append("                                 BATCHNO, ");
			sql.append("                                 SOURCEFACTORYNAME, ");
			sql.append("                                 DESTINATIONFAB, ");
			sql.append("                                 DESTINATIONSHOP ");
			sql.append("                                 ) ");
			sql.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) ");

			MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), insertArgList);
		}
		catch (Exception e)
		{
			throw new CustomException(e);
		}
	}

}
