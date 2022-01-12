package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataSource;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.BankQueueTime;
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
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.info.MakeShippedInfo;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.IncrementFinishedQuantityByInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class ShipLot extends SyncHandler {
	private static Log log = LogFactory.getLog(ShipLot.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
        List<Lot> lotDataList = new ArrayList<>();
        ConstantMap constMap = new ConstantMap();
        String bankType=SMessageUtil.getBodyItemValue(doc, "BANKTYPE", false);
        String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);
        String reasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", false);
        String department = SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", false);
        
        
		for (Element lot : lotList)
		{
			String lotName = SMessageUtil.getChildText(lot, "LOTNAME", true);
			String toFactoryName = SMessageUtil.getChildText(lot, "TOFACTORYNAME", true);
			String Deparment = SMessageUtil.getChildText(lot, "DEPARTMENT", false);

			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			ProductSpec productSpec = GenericServiceProxy.getSpecUtil().getProductSpec(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion());
			CommonValidation.checkLotIssuestate(lotData);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");

			List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUSequence(lotName);
			
			//Mantis 0000106
			if (StringUtils.equals(lotData.getFactoryName(), "ARRAY")&&!StringUtils.equals(toFactoryName, "ARRAY") && !StringUtils.equals(lotData.getProductionType(),"M")&& !StringUtils.equals(lotData.getProductionType(),"D"))
				checkGlassJudge(productUdfs);
			
			// Check Grade
			boolean isNGBank = false;
			if(isNGBank)
			{   List<Object[]> updateScrapProduct= new ArrayList<Object[]>();
			    ConstantMap constantMap = GenericServiceProxy.getConstantMap();
				List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());
				for (Product productData : productList)
				{
					String sProductName=productData.getKey().getProductName();
					Product sproductData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(sProductName);
					eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
					
					List<Object> scrapProduct = new ArrayList<Object>();
					scrapProduct.add(sProductName);
					scrapProduct.add(lotName);
					scrapProduct.add(lotData.getProductionType());
					scrapProduct.add("N");
					scrapProduct.add(constantMap.Lot_Shipped);
					scrapProduct.add(eventInfo.getEventTimeKey());
					scrapProduct.add(eventInfo.getEventTime());
					scrapProduct.add(eventInfo.getEventUser());
					scrapProduct.add("");
					scrapProduct.add("");
					scrapProduct.add(Deparment);
					scrapProduct.add("");
					scrapProduct.add("");
					scrapProduct.add("");
					scrapProduct.add("");
					scrapProduct.add(eventInfo.getEventComment());
					updateScrapProduct.add(scrapProduct.toArray());
				}
				MESLotServiceProxy.getLotServiceUtil().InsertScrapProduct(updateScrapProduct);
				
			}
			
			
			
			// Set OldProductRequestName for CancelReceive
			MakeShippedInfo makeShippedInfo = MESLotServiceProxy.getLotInfoUtil().makeShippedInfo(lotData, lotData.getAreaName(), "", toFactoryName, productUdfs);
			
			if(StringUtils.isEmpty(bankType)||StringUtils.equals(bankType, "OK"))
			{
				makeShippedInfo.getUdfs().put("BANKTYPE", "OK");
				eventInfo.setEventName("Ship");
			}
			else
			{
				eventInfo.setEventName("NGShip");
				ProcessOperationSpec pos = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());
				if((StringUtils.equals(lotData.getFactoryName(), "ARRAY")||StringUtils.equals(lotData.getFactoryName(), "TP"))&&!StringUtils.equals(pos.getDetailProcessOperationType(), "SHIP"))
				{
					NGShipChangeOperation(lotData,eventInfo);
				}
				makeShippedInfo.getUdfs().put("BANKTYPE", "NG");				
				eventInfo.setReasonCode(reasonCode);
				eventInfo.setReasonCodeType(SMessageUtil.getBodyItemValue(doc, "RESPONSEFACTORY", false)+ "-"+department+ " - " + reasonCodeType);
			}
			
			lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			makeShippedInfo.getUdfs().put("OLDPRODUCTREQUESTNAME", lotData.getProductRequestName());
			makeShippedInfo.getUdfs().put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());
			makeShippedInfo.getUdfs().put("BEFOREFLOWNAME", lotData.getProcessFlowName());
			
			//Mantis 0000106
			String workOrderName = lotData.getProductRequestName();
			checkWorkOrder(workOrderName,lotData);
			CommonValidation.checkLotHoldState(lotData);
			CommonValidation.checkLotCarriernameNotNull(lotData);
			CommonValidation.checkDummyProductReserve(lotData);
			CommonValidation.checkDummyGlassFlag(lotData);
			
			
			Lot newLotData = MESLotServiceProxy.getLotServiceImpl().shipLot(eventInfo, lotData, makeShippedInfo);
			
			// Exit QTime
			ExtendedObjectProxy.getProductQTimeService().exitQTimeByLot(eventInfo, lotName, lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessOperationName());
			
			Lot shippedLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			// Check QTime Interlocked
			ExtendedObjectProxy.getProductQTimeService().moveInQTimeByLot(eventInfo,
					shippedLotData.getKey().getLotName(), shippedLotData.getFactoryName(),
					shippedLotData.getProcessFlowName(), shippedLotData.getProcessFlowVersion(),
					shippedLotData.getProcessOperationName(), shippedLotData.getProcessOperationVersion(),
					shippedLotData.getUdfs().get("RETURNFLOWNAME"));
			
			createBankQueueTime(newLotData,eventInfo);

			ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());
			incrementProductRequest(eventInfo, lotData, productRequestData);
			
			lotDataList.add(newLotData);
			
			//SAP
			//Add SAP Switch
			String sapFlag=CommonUtil.getEnumDefValueStringByEnumName("SAPFLAG");
			if(StringUtil.isNotEmpty(sapFlag)&&StringUtil.equals(sapFlag, "Y")&&
					StringUtils.isNotEmpty(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
			{
				SuperProductRequest superWO = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
				
				String batchNo = superWO.getProductRequestName() + newLotData.getLotGrade();
				if(batchNo.length() == 9)
					batchNo += "0";
				
				String factoryPositionCode = "";
				if(StringUtils.equals(productSpec.getUdfs().get("PRODUCTSPECTYPE"), "L")
						||StringUtils.equals(productSpec.getUdfs().get("PRODUCTSPECTYPE"), "T"))
				{
					factoryPositionCode="OF01";
				}
				else if(StringUtils.equals(productSpec.getUdfs().get("PRODUCTSPECTYPE"), "F"))
				{
					factoryPositionCode="AF02";
				}
				else if(StringUtils.equals(productSpec.getUdfs().get("PRODUCTSPECTYPE"), "B")
						||StringUtils.equals(productSpec.getUdfs().get("PRODUCTSPECTYPE"), "C"))
				{
					factoryPositionCode="PF01";
				}

				
				List<Map<String, String>> ERPReportList = new ArrayList<Map<String, String>>();
				Map<String, String> ERPInfo = new HashMap<String, String>();
				
				ERPInfo.put("SEQ", TimeStampUtil.getCurrentEventTimeKey());
				ERPInfo.put("PRODUCTREQUESTNAME", superWO.getProductRequestName());
				ERPInfo.put("PRODUCTSPECNAME", newLotData.getProductSpecName().substring(0, newLotData.getProductSpecName().length() - 1));
				ERPInfo.put("PRODUCTQUANTITY", String.valueOf(newLotData.getProductQuantity()));
				ERPInfo.put("PRODUCTTYPE", superWO.getProductType());
				if(((superWO.getProductRequestType().equals("E")||superWO.getProductRequestType().equals("T"))&&!superWO.getSubProductionType().equals("ESLC")))
				{
					ERPInfo.put("FACTORYCODE","5099");
					ERPInfo.put("FACTORYPOSITION", "9F99");
				}
				else
				{
					ERPInfo.put("FACTORYCODE", "5001");
					ERPInfo.put("FACTORYPOSITION",factoryPositionCode);
				}
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
					ERPInfo.put("EVENTTIME", eventInfo.getEventTimeKey().substring(0, 8));
				}
				
				ERPInfo.put("NGFLAG", "");
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
		

		//send attachment mail
		if(lotDataList!=null&& lotDataList.size()>0)
		{
			DataSource attachmentDSource = GenericServiceProxy.getMailAttachmentGeneratorSerivce().createShipLotExcel(lotDataList);
			String[] mailList = CommonUtil.getEmailList("Ship");
			if (mailList != null) 
			{
				GenericServiceProxy.getMailSerivce().sendAttachmentMail(mailList, this.getClass().getSimpleName(), "This email was triggered when [ShipLot] was sent. Please check the attachment", attachmentDSource,"ShipLot.xls" );
			
				//houxk 20210618
				try
				{				
					//sendToEm("ShipLot" );
				}
				catch (Exception e)
				{
					log.info("eMobile or WeChat Send Error : " + e.getCause());	
				}
			}						
		}

		return doc;
	}
	
	private boolean checkProductGrade (Lot lotData) throws CustomException
	{
		// Check Grade
		List<Product> productList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());

		boolean isNGBank = false;

		for (Product productData : productList)
		{
			if (StringUtil.equals(productData.getProductGrade(), "N"))
			{
				isNGBank = true;
				break;
			}
		}
		
		return isNGBank;
	}

	private void incrementProductRequest(EventInfo eventInfo, Lot lotData, ProductRequest productRequestData) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal,
			DuplicateNameSignal, CustomException
	{
		// Increment Work Order Finished Quantity
		IncrementFinishedQuantityByInfo incrementFinishedQuantityByInfo = new IncrementFinishedQuantityByInfo();
		incrementFinishedQuantityByInfo.setQuantity((long) lotData.getProductQuantity());

		eventInfo.setEventName("IncreamentQuantity");
		MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementFinishedQuantityBy(productRequestData, incrementFinishedQuantityByInfo, eventInfo);
  
		 //modify by wangys 2020/11/26 Cancel Auto CompleteWO 
		/*ProductRequest newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());

		if (newProductRequestData.getPlanQuantity() == newProductRequestData.getFinishedQuantity() + newProductRequestData.getScrappedQuantity())
			MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompleted(eventInfo, lotData.getProductRequestName());*/
	}

	private void checkGlassJudge(List<ProductU> productUdfs) throws CustomException
	{
		if (productUdfs == null || productUdfs.size() == 0)
			return;

		List<String> productNameList = new ArrayList<String>();

		for (ProductU product : productUdfs)
		{
			productNameList.add(product.getProductName());
		}

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT PRODUCTNAME ");
		sql.append("  FROM PRODUCT ");
		sql.append(" WHERE PRODUCTNAME IN (:PRODUCTNAMELIST) ");
		sql.append("MINUS ");
		sql.append("SELECT SHEETNAME ");
		sql.append("  FROM CT_GLASSJUDGE ");
		sql.append(" WHERE SHEETNAME IN (:PRODUCTNAMELIST) ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("PRODUCTNAMELIST", productNameList);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			productNameList.clear();
			productNameList = CommonUtil.makeListBySqlResult(result, "PRODUCTNAME");

			throw new CustomException("LOT-0148", productNameList);
		}
	}
	
	//Mantis 0000106
	private void checkWorkOrder(String workOrderName,Lot lotData) throws CustomException
	{		
		double finishQty =0;
		double scrapQty = 0;
		double releaseQty = 0;
		double productQty = 0;
		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT PRODUCTREQUESTNAME,PRODUCTREQUESTHOLDSTATE,PRODUCTREQUESTSTATE, ");
		sql.append(" FINISHEDQUANTITY,SCRAPPEDQUANTITY,RELEASEDQUANTITY ");
		sql.append("  FROM PRODUCTREQUEST ");
		sql.append(" WHERE PRODUCTREQUESTNAME= :PRODUCTREQUESTNAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("PRODUCTREQUESTNAME", workOrderName);
		
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if(result.get(0).get("PRODUCTREQUESTSTATE").toString().equals("Completed"))
		{
			throw new CustomException("PRODUCTREQUEST-0004", result.get(0).get("PRODUCTREQUESTSTATE").toString());
		}
		else if(result.get(0).get("PRODUCTREQUESTHOLDSTATE").toString().equals("Y"))
		{
			throw new CustomException("PRODUCTREQUEST-0013",result.get(0).get("PRODUCTREQUESTNAME").toString(),result.get(0).get("PRODUCTREQUESTHOLDSTATE").toString() );
		}
		
		
		finishQty = Double.valueOf(result.get(0).get("FINISHEDQUANTITY").toString());
		scrapQty = Double.valueOf(result.get(0).get("SCRAPPEDQUANTITY").toString());
		releaseQty = Double.valueOf(result.get(0).get("RELEASEDQUANTITY").toString());
		productQty = lotData.getProductQuantity();
		
		
		if(finishQty+scrapQty+productQty>releaseQty){
			
			throw new CustomException("PRODUCTREQUEST-0028",releaseQty,finishQty+scrapQty+productQty );
		}

	}
	
	private void createBankQueueTime(Lot lotData,EventInfo eventInfo) throws CustomException
	{
		try 
		{
			StringBuffer sql=new StringBuffer();
			sql.append(" SELECT LOTNAME  ");
			sql.append(" FROM CT_BANKQUEUETIME ");
			sql.append(" WHERE LOTNAME=:LOTNAME ");
			
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("LOTNAME", lotData.getKey().getLotName());
			List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
			
			StringBuffer bankPolicy=new StringBuffer();
			bankPolicy.append(" SELECT A.FACTORYNAME,A.PRODUCTSPECNAME,A.PRODUCTSPECVERSION,B.TOFACTORYNAME,B.BANKTYPE,B.WARNINGDURATIONLIMIT,B.INTERLOCKDURATIONLIMIT  ");
			bankPolicy.append(" FROM  TPPOLICY A,POSBANKQUEUETIME B ");
			bankPolicy.append(" WHERE A.CONDITIONID=B.CONDITIONID ");
			bankPolicy.append(" AND A.FACTORYNAME=:FACTORYNAME ");
			bankPolicy.append(" AND A.PRODUCTSPECNAME=:PRODUCTSPECNAME ");
			bankPolicy.append(" AND A.PRODUCTSPECVERSION=:PRODUCTSPECVERSION ");
			bankPolicy.append(" AND B.TOFACTORYNAME=:TOFACTORYNAME ");
			bankPolicy.append(" AND B.BANKTYPE=:BANKTYPE ");
			
			
			Map<String, String> bindMap2 = new HashMap<String, String>();
			bindMap2.put("FACTORYNAME", lotData.getFactoryName());
			bindMap2.put("PRODUCTSPECNAME", lotData.getProductSpecName());
			bindMap2.put("PRODUCTSPECVERSION", lotData.getProductSpecVersion());
			bindMap2.put("TOFACTORYNAME", lotData.getDestinationFactoryName());
			bindMap2.put("BANKTYPE", lotData.getUdfs().get("BANKTYPE"));
			List<Map<String, Object>> bankPolicyResult = GenericServiceProxy.getSqlMesTemplate().queryForList(bankPolicy.toString(), bindMap2);
			
			if(result!=null && result.size()>0)
			
			{
				BankQueueTime bankQueueTime = ExtendedObjectProxy.getBankQueueTimeService().selectByKey(false,
						new Object[] { lotData.getKey().getLotName() });

				ExtendedObjectProxy.getBankQueueTimeService().remove(eventInfo, bankQueueTime);
			}
			if (bankPolicyResult != null && bankPolicyResult.size() > 0) 
			{
				BankQueueTime bankInfo = new BankQueueTime();
				bankInfo.setLotName(lotData.getKey().getLotName());
				bankInfo.setBankType(lotData.getUdfs().get("BANKTYPE"));
				bankInfo.setFactoryName(lotData.getFactoryName());
				bankInfo.setProductSpecName(lotData.getProductSpecName());
				bankInfo.setProductSpecVersion(lotData.getProductSpecVersion());
				bankInfo.setProcessFlowName(lotData.getProcessFlowName());
				bankInfo.setProcessFlowVersion(lotData.getProcessFlowVersion());
				bankInfo.setProcessOperationName(lotData.getProcessOperationName());
				bankInfo.setProcessOperationVersion(lotData.getProcessOperationVersion());
				bankInfo.setToFactoryName(lotData.getDestinationFactoryName());

				bankInfo.setQueueTimeState("Entered");
				bankInfo.setEnterTime(eventInfo.getEventTime());
				bankInfo.setExitTime(null);
				bankInfo.setWarningTime(null);
				bankInfo.setInterlockTime(null);
				bankInfo.setResolveTime(null);
				bankInfo.setResolveUser("");
				bankInfo.setWarningDurationLimit(bankPolicyResult.get(0).get("WARNINGDURATIONLIMIT").toString());
				bankInfo.setInterlockDurationLimit(bankPolicyResult.get(0).get("INTERLOCKDURATIONLIMIT").toString());

				bankInfo.setAlarmType("BankQTimeOver");

				bankInfo.setLastEventName(eventInfo.getEventName());
				bankInfo.setLastEventUser(eventInfo.getEventUser());
				bankInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

				ExtendedObjectProxy.getBankQueueTimeService().create(eventInfo, bankInfo);
			}
		}
		catch (Exception e ) 
		{
			throw new CustomException("BANK-0002");
		}
	}
	
	public void sendToEm(String alarmType)
	{
		String userList[] = getUserList(alarmType);	
		if(userList == null || userList.length ==0) return;
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ShipLot", "MES", "", "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		try
		{	
			log.info("ShipLot Start Send To Emobile & Wechat");	
						
			String title = "ShipLot";
			String detailtitle = "${}CIM系统消息通知";
			String url = "";
			
			StringBuffer info = new StringBuffer();
			info.append("<pre>This email was triggered when [ShipLot] was sent. Please check the attachment</pre>");
			info.append("<pre>====================详情请见邮件==================</pre>");					
			String message = info.toString();			
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userList, title, detailtitle, message, url);
									
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userList, title, detailtitle, message, "");
		}
		catch (Exception e)
		{
			log.info("eMobile or WeChat Send Error : " + e.getCause());	
		} 
	}
	//alarmType = ShipLot
	private String[] getUserList(String alarmType)
	{
		String sql = " SELECT DISTINCT  UP.USERID "
				   + " FROM CT_ALARMGROUP AG , CT_ALARMUSERGROUP AU  , USERPROFILE UP"
				   + " WHERE AG.ALARMTYPE = :ALARMTYPE"
				   + " AND AG.ALARMGROUPNAME = AU.ALARMGROUPNAME "
				   + " AND AU.USERID = UP.USERID ";
		
		List<Map<String, Object>> resultList = null;
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new String[] { alarmType});
		}
		catch (Exception ex)
		{
			log.info(ex.getCause());
		}

		if(resultList ==null || resultList.size()==0) return null;
		
		return CommonUtil.makeListBySqlResult(resultList, "USERID").toArray(new String[] {});
	}
	
	private void NGShipChangeOperation(Lot lotData,EventInfo eventInfo) throws CustomException
	{
		ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);
		if(!StringUtils.equals(processFlowData.getProcessFlowType(), "Main"))
		{
			throw new CustomException("LOT-0330", lotData.getKey().getLotName());
		}
		CommonValidation.checkLotProcessStateWait(lotData);
		CommonValidation.checkFirstGlassLot(lotData);
		CommonValidation.checkJobDownFlag(lotData);
		CommonValidation.checkBaseLineLot(lotData);
		CommonValidation.checkProcessInfobyString(lotData.getKey().getLotName());
		
		String areaName = lotData.getAreaName();
		String factoryName = lotData.getFactoryName();
		String lotHoldState = lotData.getLotHoldState();
		String lotProcessState = lotData.getLotProcessState();
		String lotState = lotData.getLotState();
		String processFlowName = lotData.getProcessFlowName();
		String processFlowVersion = lotData.getProcessFlowVersion();
		String beforeProcOperName=lotData.getProcessOperationName();
		String processOperationVersion = lotData.getProcessOperationVersion();
		String productionType = lotData.getProductionType();
		String productRequestName = lotData.getProductRequestName();
		String productSpec2Name = lotData.getProductSpec2Name();
		String productSpec2Version = lotData.getProductSpec2Version();
		String productSpecName = lotData.getProductSpecName();
		String productSpecVersion = lotData.getProductSpecVersion();
		long priority = lotData.getPriority();
		Timestamp dueDate = lotData.getDueDate();
		double subProductUnitQuantity1 = lotData.getSubProductUnitQuantity1();
		double subProductUnitQuantity2 = lotData.getSubProductUnitQuantity2();
		List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUSequence(lotData.getKey().getLotName());
		String newProcOperName="";
		if(StringUtils.equals(lotData.getFactoryName(), "ARRAY"))
		{
			newProcOperName="1X999";
		}
		else if(StringUtils.equals(lotData.getFactoryName(), "ARRAY"))
		{
			newProcOperName="4X999";
		}
		String nodeStack=CommonUtil.getNodeStack(factoryName, processFlowName, processFlowVersion, newProcOperName, processOperationVersion);
		
		// Normal Change Operation
		ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotData, areaName, dueDate, factoryName, lotHoldState, lotProcessState, lotState, nodeStack, priority,
				processFlowName, processFlowVersion, newProcOperName, "00001", beforeProcOperName, productionType, productRequestName, productSpec2Name, productSpec2Version,
				productSpecName, productSpecVersion, productUdfs, subProductUnitQuantity1, subProductUnitQuantity2);

		eventInfo = EventInfoUtil.makeEventInfo_IgnoreHold("ChangeOper", getEventUser(), getEventComment(), "", "");

		Lot newLotData = MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);

		List<Product> productDataList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotData.getKey().getLotName());
		ProcessOperationSpec beforeOperationSpecData = CommonUtil.getProcessOperationSpec(factoryName, beforeProcOperName, processOperationVersion);
		ProcessOperationSpec newOperationSpecData = CommonUtil.getProcessOperationSpec(factoryName, newProcOperName, "00001");
		MESLotServiceProxy.getLotServiceUtil().deleteSamplingDataBetweenTwoOperations(eventInfo, newLotData, productDataList, beforeOperationSpecData, newOperationSpecData);
	}
	
}
