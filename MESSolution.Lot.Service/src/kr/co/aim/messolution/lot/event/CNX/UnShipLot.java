package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.BankQueueTime;
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.MakeUnShippedInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.IncrementFinishedQuantityByInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class UnShipLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
		ConstantMap constMap = new ConstantMap();

		for (Element lot : lotList)
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnShip", getEventUser(), getEventComment(), "", "");

			String lotName = SMessageUtil.getChildText(lot, "LOTNAME", true);
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			String bankType=lotData.getUdfs().get("BANKTYPE");
			String destinationFactory=lotData.getDestinationFactoryName();
			if(StringUtils.equals(bankType, "NG"))
			{
				eventInfo.setEventName("NGUnShip");
			}
			
			ProductSpec productSpec = GenericServiceProxy.getSpecUtil().getProductSpec(lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion());
			
			List<ProductU> productUdfs = MESLotServiceProxy.getLotInfoUtil().setProductUSequence(lotName);

			MakeUnShippedInfo makeUnShippedInfo = MESLotServiceProxy.getLotInfoUtil().makeUnShippedInfo(lotData, lotData.getAreaName(), productUdfs);
			makeUnShippedInfo.getUdfs().put("BANKTYPE", "");
			makeUnShippedInfo.getUdfs().put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());
			makeUnShippedInfo.getUdfs().put("BEFOREFLOWNAME", lotData.getProcessFlowName());
			
			lotData = MESLotServiceProxy.getLotServiceImpl().unShipLot(eventInfo, lotData, makeUnShippedInfo);

			// Mantis : 0000454
			// 2021-02-03	dhko	FactoryName、OLDFactoryName、DestinationFactoryName字段修复
			modifyDestinationFactoryName(lotData, lotData.getFactoryName());
			
			String productRequestName = lotData.getProductRequestName();

			ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);

			IncrementFinishedQuantityByInfo incrementFinishedQuantityByInfo = new IncrementFinishedQuantityByInfo();
			if(productRequestData.getFinishedQuantity()>=lotData.getProductQuantity())
			{
				incrementFinishedQuantityByInfo.setQuantity(-(long) lotData.getProductQuantity());
			}
			else
			{
				throw new CustomException("WO-00002",productRequestData.getKey().getProductRequestName(),productRequestData.getFinishedQuantity(),lotData.getProductQuantity());
			}
			

			// Decrement Work Order Finished Quantity
			eventInfo.setEventName("DecrementQuantity");
			
			ProductRequest newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementFinishedQuantityBy(productRequestData, incrementFinishedQuantityByInfo, eventInfo);
			if ( newProductRequestData.getProductRequestState().equals("Completed"))
			{
				throw new CustomException("PRODUCTREQUEST-0004", newProductRequestData.getProductRequestState());
			}
			//Modify by wangys 2020/11/26 Cancel auto Released
			/*if (newProductRequestData.getProductRequestState().equals("Completed")
					&& (newProductRequestData.getPlanQuantity() > newProductRequestData.getFinishedQuantity() + newProductRequestData.getScrappedQuantity()))
			{
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeReleased(eventInfo, lotData.getProductRequestName());
			}*/
			
			// Delete QTime
			ExtendedObjectProxy.getProductQTimeService().deleteQTime(eventInfo, lotName, lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessOperationName());
			
			//Delete CT_BankQueueTime
			deleteBankQueueTime(lotData,eventInfo);
			
			//Delete CT_ScarpProduct
			boolean isNGBank = checkProductGrade(lotData);
			if(isNGBank)
			{
				deleteScrapProduct(lotData);
			}
			
			//SAP
			//Add SAP Switch
			String sapFlag=CommonUtil.getEnumDefValueStringByEnumName("SAPFLAG");
			if(StringUtil.isNotEmpty(sapFlag)&&StringUtil.equals(sapFlag, "Y")&&
					StringUtils.isNotEmpty(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
			{
				SuperProductRequest superWO = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
				
				String batchNo = superWO.getProductRequestName() + lotData.getLotGrade();
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
				Map<String, String> ERPInfo = new HashMap<>();
				
				ERPInfo.put("SEQ", TimeUtils.getCurrentEventTimeKey());
				ERPInfo.put("PRODUCTREQUESTNAME", superWO.getProductRequestName());
				ERPInfo.put("PRODUCTSPECNAME", lotData.getProductSpecName().substring(0, lotData.getProductSpecName().length() - 1));
				ERPInfo.put("PRODUCTQUANTITY", "-" +  String.valueOf(lotData.getProductQuantity()));
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
					ERPInfo.put("EVENTTIME", eventInfo.getEventTimeKey().substring(0, 8));
				}
				ERPInfo.put("NGFLAG", "");
				ERPReportList.add(ERPInfo);
				
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

		// Mantis : 0000355
		// If Factory information and LotName information are different, change LotName.
		// OLED <-> TP
		List<String> lotNameList = new ArrayList<String>();
		
		for (Element lot : lotList)
		{
			String lotName = SMessageUtil.getChildText(lot, "LOTNAME", true);
			lotNameList.add(lotName);
		}
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnShip", getEventUser(), getEventComment(), "", "");
		
		MESLotServiceProxy.getLotServiceImpl().changeLotName(eventInfo, "UnShipLot", lotNameList);
		
		return doc;
	}
	
	/**
	 * Mantis : 0000454
	 * 
	 * 2021-02-03	dhko	FactoryName、OLDFactoryName、DestinationFactoryName字段修复
	 */
	private void modifyDestinationFactoryName(Lot lotData, String destinationFactoryName) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		// Modify Lot
		String sql = "UPDATE LOT SET DESTINATIONFACTORYNAME = ? WHERE LOTNAME = ? ";
		greenFrameServiceProxy.getSqlTemplate().update(sql, new Object[] { destinationFactoryName, lotData.getKey().getLotName() });
		
		// Modify LotHistory
		sql = "UPDATE LOTHISTORY SET DESTINATIONFACTORYNAME = ? WHERE TIMEKEY = ? AND LOTNAME = ? ";
		greenFrameServiceProxy.getSqlTemplate().update(sql, new Object[] { destinationFactoryName, lotData.getLastEventTimeKey(), lotData.getKey().getLotName() });
		
		List<Product> productDataList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotData.getKey().getLotName());
		for (Product product : productDataList) 
		{
			// Product
			sql = "UPDATE PRODUCT SET DESTINATIONFACTORYNAME = ? WHERE PRODUCTNAME = ? ";
			greenFrameServiceProxy.getSqlTemplate().update(sql, new Object[] { destinationFactoryName, product.getKey().getProductName() });
			
			// ProductHistory
			sql = "UPDATE PRODUCTHISTORY SET DESTINATIONFACTORYNAME = ? WHERE TIMEKEY = ? AND PRODUCTNAME = ? ";
			greenFrameServiceProxy.getSqlTemplate().update(sql, new Object[] { destinationFactoryName, product.getLastEventTimeKey(), product.getKey().getProductName() });
		}
	}
	
	private void deleteBankQueueTime(Lot lotData,EventInfo eventInfo) throws CustomException
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
			
			if(result!=null && result.size()>0)
			
			{
				BankQueueTime bankQueueTime = ExtendedObjectProxy.getBankQueueTimeService().selectByKey(false,
						new Object[] { lotData.getKey().getLotName() });

				ExtendedObjectProxy.getBankQueueTimeService().remove(eventInfo, bankQueueTime);
			}
		}
		catch (Exception e ) 
		{
			throw new CustomException("BANK-0002");
		}
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
	
	private void deleteScrapProduct(Lot lotData) throws CustomException
	{
		try 
		{		
			String sql="DELETE FROM CT_SCRAPPRODUCT WHERE LOTNAME=:LOTNAME ";

			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("LOTNAME", lotData.getKey().getLotName());
			GenericServiceProxy.getSqlMesTemplate().update(sql,bindMap);
			//List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
		}
		catch (Exception e ) 
		{
			throw new CustomException();
		}
	}
}
